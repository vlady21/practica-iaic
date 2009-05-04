package jess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class used to represent Defqueries. These are constructed by the parser.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class Defquery extends HasLHS implements Serializable {
    private ArrayList m_results = new ArrayList();
    private ArrayList m_queryVariables = new ArrayList();

    /**
     * String prepended to query names to form backwards chaining goals
     */

    public static final String QUERY_TRIGGER = "__query-trigger-";

    Defquery(String name, String docstring, Rete engine) throws JessException {
        super(name, docstring, engine);
        // ###
    }

    private int m_maxBackgroundRules = 0;

    /**
     * Return the maximum number of rules that will fire during this query.
     * Queries call Rete.run() when they are executed to allow backward
     * chaining to occur. No more than this number of rules will be
     * allowed to fire.
     * @return As described
     */
    public int getMaxBackgroundRules() {
        return m_maxBackgroundRules;
    }

    /**
     * Set the maximum number of rules that will fire during this query.
     * @see #getMaxBackgroundRules
     * @param maxBackgroundRules  The new value for this property
     */
    public void setMaxBackgroundRules(int maxBackgroundRules) {
        m_maxBackgroundRules = maxBackgroundRules;
    }

    /**
     * Recieve satisfied queries
     */
    public synchronized void callNodeLeft(Token token, Context context)
        throws JessException {
        broadcastEvent(JessEvent.RETE_TOKEN + LEFT, token);

        if ((token.m_tag == RU.ADD) || (m_new && token.m_tag == RU.UPDATE))
            m_results.add(token);

        else if (token.m_tag == RU.REMOVE)
            m_results.remove(token);

        else if (token.m_tag == RU.CLEAR)
            m_results.clear();

        return;
    }

    /**
     * Get any query results
     */

    synchronized Iterator getResults() {
        ArrayList v = new ArrayList();
        Defquery dq = this;
        while (dq != null) {
            ArrayList results = dq.m_results;
            synchronized (results) {
                int size = results.size();
                for (int i=0; i<size; i++)
                    v.add(results.get(i));
            }
            dq = (Defquery) dq.getNext();
        }
        return v.iterator();
    }

    synchronized void clearResults() {
        m_results = new ArrayList();
        Defquery next = (Defquery) getNext();
        if (next != null)
            next.clearResults();
    }

    synchronized int countResults() {
        int n = m_results.size();
        Defquery next = (Defquery) getNext();
        if (next != null)
            n += next.countResults();
        return n;
    }

    public String getQueryTriggerName() {
        String name = getDisplayName();
        int colons = name.indexOf("::");
        return RU.scopeName(getModule(), QUERY_TRIGGER + name.substring(colons + 2));
    }

    /**
     * Tell this rule to set the LHS up for faster execution
     * @exception JessException
     */
    void freeze(Rete engine) throws JessException {
        if (m_frozen)
            return;

        super.freeze(engine);
        // Build and install query pattern here
        Pattern p = new Pattern(getQueryTriggerName(), engine);
        int i = 0;
        for (Iterator e = m_queryVariables.iterator(); e.hasNext();i++)
            p.addTest(RU.DEFAULT_SLOT_NAME,
                      new Test1(TestBase.EQ, i, (Variable) e.next()));

        insertCEAt(p, 0, engine);
    }

    void addQueryVariable(Variable v) {
        m_queryVariables.add(v);
    }

    int getNVariables() {
        return m_queryVariables.size();
    }

    Variable getQueryVariable(int i) {
        return (Variable) m_queryVariables.get(i);
    }

    void addCE(LHSComponent ce, Rete engine) throws JessException {
        if (ce.getLogical())
            throw new JessException("Defquery.addCE",
                                    "Can't use logical CE in defquery", "");
        super.addCE(ce, engine);
    }

    public String toString() {
        return "Defquery " + getName();
    }

    public Object accept(Visitor jv) {
        return jv.visitDefquery(this);
    }
}



