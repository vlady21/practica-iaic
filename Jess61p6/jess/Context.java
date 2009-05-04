package jess;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/** **********************************************************************
 * An execution context. A Context represents a scope in which variables
 * can be declared. It also holds a pointer to a Rete object in which code
 * can be executed.
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation<BR>
 * $Id: Context.java,v 1.4.2.1 2003/05/07 18:08:15 ejfried Exp $
 ********************************************************************** */
public class Context implements Serializable {
    private Hashtable m_variables;
    private Context m_parent;
    private boolean m_return;
    private Value m_retval;
    private transient Rete m_rete;
    private LogicalNode m_logicalSupportNode;

    private Token m_token;
    private Fact m_fact;

    /**
     * If this context represents a join network node from a rule LHS,
     * this will return the left input of the node.
     * @return The Token
     */

    public final Token getToken() { return m_token; }
    final void setToken(Token t) { m_token = t; }

    /**
     * If this context represents a join network node from a rule LHS,
     * this will return the right input of the node.
     * @return The Fact object
     */

    public final Fact getFact() { return m_fact; }
    final void setFact(Fact f) { m_fact = f; }

    /** If this context represents the RHS of a rule which is firing, and
     * the LHS of the rule has provided logical support, this method will
     * return the LogicalNode that lends support.
     * @return The supporting node.
     */
    public final LogicalNode getLogicalSupportNode() {
        return m_logicalSupportNode;
    }

    final void setLogicalSupportNode(LogicalNode node) {
        m_logicalSupportNode = node;
    }

    private boolean m_inAdvice;

    boolean getInAdvice() {return m_inAdvice;}
    void setInAdvice(boolean  v) {m_inAdvice = v;}

    void setEngine(Rete r) {
        m_rete = r;
    }

    /**
     * Create a new context subordinate to an existing one. The method
     * getEngine() will return c.getEngine(). Use push() instead of
     * calling this directly.
     * @param c The parent for the new context
     * @see #push
     * */
    public Context(Context c) {
        m_rete = c.m_rete;
        m_parent = c;
    }

    /**
     * @param engine The value to be returned from getEngine
     */
    Context(Rete engine) {
        m_rete = engine;
        m_parent = null;
    }

    /**
     * Create a new context subordinate to an existing one. The method
     * getEngine() will return the given Rete object.
     * @param c The parent for the new context
     * @param engine The value to be returned from getEngine
     */

    public Context(Context c, Rete engine) {
        m_rete = engine;
        m_parent = c;
    }

    /**
     * Make this context absolutely brand-new again.
     */
    void clear() {
        m_fact = null;
        m_token = null;
        m_inAdvice = false;
        m_logicalSupportNode = null;
        m_return = false;
        m_retval = null;
        m_variables = null;
    }


    /**
     * Returns true if the return flag has been set in this
     * context. The Jess (return) function sets the return flag in the
     * local context.
     * @return The value of the return flag
     * */
    public final boolean returning() {
        return m_return;
    }

    /**
     * Set the return flag to true, and supply a value to be returned.
     * @param val The value that should be returned from this context
     * @return The argument
     */
    public final Value setReturnValue(Value val) {
        m_return = true;
        m_retval = val;
        return val;
    }

    /**
     * Get the value set via setReturnValue
     * @return The return value
     */
    public final Value getReturnValue() {
        return m_retval;
    }

    /**
     * Clear the return flag and return value for this context.
     */
    public final void clearReturnValue() {
        m_return = false;
        m_retval = null;
    }

    private final int nVariables() {
        if (m_variables == null)
            return 0;
        else
            return m_variables.size();
    }

    private Hashtable getVariables() {
        if (m_variables == null)
            m_variables = new Hashtable(10);

        return m_variables;
    }

    /**
     * Returns the Rete engine associated with this context.
     * @return The engine to use with this context
     */
    public final Rete getEngine() {
        return m_rete;
    }

    /**
     * Create and return a new context subordinate to this one.
     * @return The next context
     */
    public Context push() {
        return new Context(this);
    }

    /**
     * Pop this context off the execution stack.
     * If this context has no parent, just return this context. If it
     * has a parent, transfer the values of the return flag and the
     * return value to the parent, then return the parent.
     * @return The context as described
     */
    public Context pop() {
        if (m_parent != null) {
            synchronized (m_parent) {
                m_parent.m_return = m_return;
                m_parent.m_retval = m_retval;
                return m_parent;
            }
        }
        else
            return this;
    }

    private Hashtable findVariable(String key) {
        Context c = this;
        while (c != null) {
            Hashtable ht = c.getVariables();
            Value v = (Value) ht.get(key);
            if (v != null)
                return ht;
            else
                c = c.m_parent;
        }
        return null;
    }

    synchronized void removeNonGlobals() {
        if (m_variables == null)
            return;

        Hashtable ht = new Hashtable(10);
        for (Enumeration e = m_variables.keys(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            if (m_rete.findDefglobal(s) != null)
                ht.put(s, m_variables.get(s));
        }
        m_variables = ht;
    }

    /**
     * Get the value of a variable
     * @param name The name of the variable with no leading '?' or '$'
     * characters
     * @exception JessException If the variable is undefined
     * */
    public Value getVariable(String name) throws JessException {
        Hashtable ht = findVariable(name);
        if (ht == null)
            throw new JessException("Context.getVariable",
                                    "No such variable", name);
        return ((Value) ht.get(name)).resolveValue(this);
    }

    /**
     * Set a (possibly new) variable to some type and value
     * @param name Name of the variable
     * @param value The value of the variable
     */
    public void setVariable(String name, Value value) throws JessException {
        Hashtable ht = findVariable(name);
        if (ht == null)
            ht = getVariables();
        ht.put(name, value);
    }

    /**
     * Returns a useful debug representation of the context.
     * @return A string with information about this context.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[Context, " + nVariables() + " variables: ");
        for (Enumeration e = getVariables().keys(); e.hasMoreElements(); ) {
            Object o = e.nextElement();
            sb.append(o + "=" + m_variables.get(o) + ";");
        }
        sb.append("]");
        return sb.toString();
    }

}


