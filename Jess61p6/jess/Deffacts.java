package jess;

import java.io.Serializable;
import java.util.ArrayList;

/** **********************************************************************
 * Class used to represent deffacts. Note that you can create Deffacts objects
 * and add them to a Rete engine using Rete.addDeffacts().
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation<BR>
 * $Id: Deffacts.java,v 1.3 2003/01/21 03:38:38 ejfried Exp $
 ********************************************************************** */

public class Deffacts implements Serializable, Visitable {

    private String m_name;
    private String m_module;
    private ArrayList m_facts;
    private String m_docstring = "";

    /**
     * Fetch the name of this deffacts
     * @return the name
     */
    public final String getName() { return m_name; }

    /**
     * Fetch the module of this deffacts
     * @return the module
     */
    public final String getModule() { return m_module; }

    /**
     * Fetch the documentation comment, if any, for this deffacts
     * @return the documentation string
     */
    public final String getDocstring() { return m_docstring; }

    /**
     * Create a deffacts
     * @param name The name of the deffacts
     * @param docstring A documentation string
     */

    public Deffacts(String name, String docstring, Rete engine)
        throws JessException {

        int colons = name.indexOf("::");
        if (colons != -1) {
            m_module = name.substring(0, colons);
            engine.verifyModule(m_module);
            m_name = name;
        } else {
            m_module = engine.getCurrentModule();
            m_name = engine.resolveName(name);
        }

        m_facts = new ArrayList();
        m_docstring = docstring;
    }

    /**
     * Add a fact to this deffacts
     * @param fact The fact to add
     */

    public void addFact(Fact fact) {
        m_facts.add(fact);
    }

    /**
     * Fetch the number of facts in this deffacts
     * @return the number of facts
     */

    public int getNFacts() { return m_facts.size(); }

    /**
     * Fetch a single Fact from this deffacts
     * @param idx the o-based index of the desired fact
     * @return the idx'th fact
     */

    public Fact getFact(int idx) {
        return (Fact) m_facts.get(idx);
    }

    /**
     * Assert my facts into engine.
     */

    public void reset(Rete engine) throws JessException {
        try {
            Context gc = engine.getGlobalContext();
            for (int j=0; j<m_facts.size(); j++) {
                Fact f = (Fact) m_facts.get(j);
                f = f.expand(gc);
                engine.assertFact(f, engine.getGlobalContext());
            }
        } catch (JessException re) {
            re.addContext("assert from deffacts " + m_name);
            throw re;
        }
    }

    /**
     * Describe myself
     * @return A string representation of this deffacts
     */

    public String toString() {
        return "[deffacts " + m_name + "]";
    }

    public Object accept(Visitor v) {
        return v.visitDeffacts(this);
    }

}


