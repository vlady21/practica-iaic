package jess;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Class used to represent Defrules. These are constructed by the parser.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class Defrule extends HasLHS implements Serializable {
    private HashMap m_activations = new HashMap();
    private Funcall[] m_actions;
    private int m_nActions;
    private int m_salience;
    Value m_salienceVal;
    private boolean m_autoFocus = false;

    Defrule(String name, String docstring, Rete engine) throws JessException {
        super(name, docstring, engine);
        // ###
        m_salience = 0;
        m_salienceVal = new Value(0, RU.INTEGER);
    }

    /**
     * Fetch the salience setting of this rule
     * @return The salience of this defrule
     */

    public final int getSalience() { return m_salience; }

    void setSalience(Value i, Rete engine) throws JessException {
        m_salienceVal = i;
        evalSalience(engine);
    }

    public boolean getAutoFocus() {
        return m_autoFocus;
    }

    public void setAutoFocus(boolean autoFocus) {
        m_autoFocus = autoFocus;
    }

    void addCE(LHSComponent ce, Rete engine) throws JessException {
        if (ce.getLogical() && getGroupSize() > 0)
            if ( !getLHSComponent(getGroupSize()-1).getLogical())
                throw new JessException("Defrule.addCE",
                                        "Logical CEs can't follow non-logical CEs",
                                        m_name);

        super.addCE(ce, engine);
    }

    /**
     * Evaluate the salience of this rule. If the salience was set to a
     * Funcall value during parsing, then this function may return
     * different values over time. If the salience is constant, this is
     * equivalent to getSalience.
     * @param engine The Rete engine the rule belongs to
     * @exception JessException If something goes wrong
     * @return The evaluated salience */
    public synchronized int evalSalience(Rete engine) throws JessException {
        Context gc = engine.getGlobalContext().push();
        try {
            m_salience = m_salienceVal.intValue(gc);
        } finally {
            gc.pop();
        }
        return m_salience;
    }

    private void doAddCall(Token token, Rete engine) throws JessException {
        Activation a = new Activation(token, this);
        engine.addActivation(a);
        m_activations.put(token, a);
    }

    private void possiblyDoAddCall(Token token, Rete engine)
        throws JessException {
        // We're not new, so updates don't affect us
        if (!m_new)
            return;

        // We've already got this one
        if (m_activations.get(token) != null)
            return;

        // Add a new activation
        doAddCall(token, engine);
    }

    /**
     * All we need to do is create or destroy the appropriate Activation
     * object, which contains enough info to fire a rule.
     */
    public void callNodeLeft(Token token, Context context)
        throws JessException {
        broadcastEvent(JessEvent.RETE_TOKEN + LEFT, token);

        switch (token.m_tag) {

        case RU.ADD: {
            doAddCall(token, context.getEngine());
            break;
        }

        case RU.REMOVE: {
            Activation a = (Activation) m_activations.remove(token);
            if (a != null)
                context.getEngine().removeActivation(a);
            break;
        }

        case RU.UPDATE: {
            possiblyDoAddCall(token, context.getEngine());
            break;
        }

        case RU.CLEAR: {
            m_activations.clear();
            break;
        }
        }
        return;
    }

    private void ready(Token fact_input, Context c) {

        Fact fact;
        // set up the variable table
        for (Enumeration e = getBindings().elements(); e.hasMoreElements();) {
            BindingValue b = (BindingValue) e.nextElement();

            if (b.getSlotIndex() == RU.LOCAL)
                // no default binding for locals
                continue;

            // all others variables need info from a fact
            // if this is a not CE, skip it;
            Value val;
            fact = fact_input.fact(b.getFactNumber());
            try {
                if (b.getSlotIndex() == RU.PATTERN) {
                    val = new FactIDValue(fact);
                }
                else {
                    if (b.getSubIndex() == -1) {
                        val = fact.get(b.getSlotIndex());
                    }

                    else {
                        ValueVector vv = fact.get(b.getSlotIndex()).listValue(c);
                        val = vv.get(b.getSubIndex());
                    }

                }
                c.setVariable(b.getName(), val);
            } catch (Throwable t) {
                // bad binding. These can come from unused bindings in
                // not CE's.
            }
        }

        // If our assertions will have logical support, put the needed
        // info into the context. We peel off the post-logical part of
        // the token that activated us and save only the "logical"
        // part. The assertFact() function will save information about
        // this token and any asserted facts into the Node2. The
        // Node2, in turn, will send messages to the Rete object when
        // that token is removed.
        if (m_logicalNode != null) {
            Token logicalToken = fact_input;
            int count = fact_input.size();

            int logicalSize = m_logicalNode.getTokenSize();
            while (count > logicalSize) {
                logicalToken = logicalToken.getParent();
                --count;
            }

            c.setLogicalSupportNode(m_logicalNode);
            c.setToken(logicalToken);
        }

        return;
    }


    /**
     * Do the RHS of this rule. For each action (ValueVector form of a
     * Funcall), do two things: 1) Call ExpandAction to do variable
     * substitution and subexpression expansion 2) call
     * Funcall.Execute on it.
     *
     * Fact_input is the Vector of ValueVector facts we were fired with.
     */
    synchronized void fire(Token fact_input, Rete engine) throws JessException {
        m_activations.remove(fact_input);
        Context c = engine.getGlobalContext().push();
        c.clearReturnValue();

        // Pull needed values out of facts into bindings table
        ready(fact_input, c);


        try {
            // OK, now run the rule. For every action...
            for (int i=0; i<m_nActions; i++) {
                m_actions[i].execute(c);

                if (c.returning()) {
                    c.clearReturnValue();
                    c.pop();
                    engine.popFocus(getModule());
                    return;
                }
            }
        } catch (JessException re) {
            re.addContext("defrule " + getDisplayName());
            throw re;
        } finally {
            c.pop();
        }

    }

    void debugPrint(Token facts, int seq, PrintWriter ps) {
        ps.print("FIRE ");
        ps.print(seq);
        ps.print(" ");
        ps.print(getDisplayName());
        for (int i=0; i<facts.size(); i++) {
            Fact f = facts.fact(i);
            if (f.getFactId() != -1)
                ps.print(" f-" + f.getFactId());
            if (i< facts.size() -1)
                ps.print(",");
        }
        ps.println();
        ps.flush();
    }

    /**
     * Fetch the number of actions on this rule's RHS
     * @return The number of actions
     */
    public int getNActions() { return m_nActions; }

    /**
     * Fetch the idx-th RHS action of this rule.
     * @param idx The zero-based index of the action to fetch
     * @return The action as a Funcall
     */
    public Funcall getAction(int idx) { return m_actions[idx] ; }


    /**
     * Add an action to this deffunction
     * @param fc
     */
    void addAction(Funcall fc) {
        if (m_actions == null || m_nActions == m_actions.length) {
            Funcall[] temp = m_actions;
            m_actions = new Funcall[m_nActions + 5];
            if (temp != null)
                System.arraycopy(temp, 0, m_actions, 0, m_nActions);
        }
        m_actions[m_nActions++] = fc;
    }

    public Object accept(Visitor jv) {
        return jv.visitDefrule(this);
    }

    private LogicalNode m_logicalNode = null;
    void setLogicalInformation(LogicalNode node) {
        m_logicalNode = node;
    }

    public String toString() {
        return "Defrule " + getName();
    }

    // For testing
    LogicalNode getLogicalNode() {
        return m_logicalNode;
    }
}



