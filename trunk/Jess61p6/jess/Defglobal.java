package jess;
import java.io.Serializable;

/**
 * Class used to represent Defglobals. You can create Defglobals and
 * add them to a Rete engine using Rete.addDefglobal.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class Defglobal implements Serializable, Visitable {
    private String m_name;
    private Value m_value;

    /**
     * Create a defglobal. Should be added to a Rete object with
     * Rete.addDefglobal. Note that a separate Defglobal object must be
     * created for each global variable, even though one (defglobal)
     * construct may represent more than one such variable.
     *
     * @param name The defglobal's variable name. Note that the name
     * must begin and end with an asterisk.
     * @param val The initial value for the defglobal; can be an
     * RU.FUNCALL value.
     * @exception JessException If anything goes wrong.  */

    public Defglobal(String name, Value val) throws JessException {
        // ###
        m_name = name;
        m_value = val;
    }

    /**
     * Reinject this Defglobal into the engine
     */

    public void reset(Rete engine) throws JessException {
        try {
            Context gc = engine.getGlobalContext();
            gc.setVariable(m_name, m_value.resolveValue(gc));
        } catch (JessException re) {
            re.addContext("definition for defglobal ?" + m_name);
            throw re;
        }
    }

    /**
     * Get this defglobal's variable name
     * @return The variable name
     */
    public String getName() { return m_name; }

    /**
     * Get this defglobal's initialization value. The returned Value may be a
     * simple value, a Variable, or a FuncallValue, so be careful how you
     * interpret it.
     * @return The value this variable was originally initialized to
     */
    public Value getInitializationValue() { return m_value; }

    /**
     * Describe myself
     * @return A pretty-printed version of the defglobal, suitable for
     * parsing
     */
    public String toString() {
        return "[defglobal " + m_name + "]";
    }

    public Object accept(Visitor v) {
        return v.visitDefglobal(this);
    }

}


