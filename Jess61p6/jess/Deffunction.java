package jess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/** **********************************************************************
 * Class used to represent Deffunctions (functions defined in the Jess
 * language).  Note that you can create these form Java code and add
 * them to a Rete engine using Rete.addUserfunction.
 *
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class Deffunction implements Userfunction, Serializable, Visitable {
    /**
     * Represents a formal paramter to this deffunction.
     */
    public static class Argument implements Serializable {
        Argument(String name, int type) {
            m_name = name;
            m_type = type;
        }
        String m_name;
        int m_type;
        public String getName() {
            return m_name;
        }

        public int getType() {
            return m_type;
        }
    }

    private String m_name;
    private String m_docstring = "";
    private ArrayList m_arguments = new ArrayList();
    private ArrayList m_actions = new ArrayList();
    private boolean m_hasWildcard = false;

    /**
     * Fetch the name of this Deffunction
     * @return the name
     */
    public final String getName() { return m_name; }

    /**
     * Fetch the documentation string of this Deffunction
     * @return the documentation string
     */
    public final String getDocstring() { return m_docstring; }

    /**
     * Set the documentation string of this Deffunction
     * @param ds The documentation string
     */
    public final void setDocstring(String ds) { m_docstring = ds; }

    /**
     * Create a deffunction
     * @param name the name of the deffunction
     * @param docstring the documentation string
     */
    public Deffunction(String name, String docstring) {
        m_name = name;
        m_docstring = docstring;
    }

    /**
     * The formal parameters to this Deffunction
     */
    public Iterator getArguments() {
        return m_arguments.iterator();
    }

    /**
     * The body of this Deffunction
     */
    public Iterator getActions() {
        return m_actions.iterator();
    }


    /**
     * Add a formal argument to this deffunction. Only the last may be a
     * MULTIVARIABLE.  Note that arguments will appear in left-to-right
     * roder acording to the order in which they are added.
     * @param name A name for the variable (without the leading '?')
     * @param type RU.MULTIVARIABLE or RU.VARIABLE */
    public void addArgument(String name, int type) throws JessException {
        if (m_hasWildcard)
            throw new JessException("Deffunction.addArgument",
                                    "Deffunction " + m_name +
                                    " already has a wildcard argument:", name);
        m_arguments.add(new Argument(name, type));
        if (type == RU.MULTIVARIABLE)
            m_hasWildcard = true;
    }

    /**
     * Add an action to this deffunction. The actions and values added
     * to a deffunction will be stored in the order added, and thereby
     * make up the body of the deffunction.
     * @param fc The action */

    public void addAction(Funcall fc) throws JessException {
        m_actions.add(new FuncallValue(fc));
    }

    /**
     * Add a simple value to this deffunction. The actions and values
     * added to a deffunction will be stored in the order added, and
     * thereby make up the body of the deffunction.
     * @param val The value */

    public void addValue(Value val) {
        m_actions.add(val);
    }


    /**
     * Execute this deffunction. Evaluate each action or value, in
     * order. If no explicit (return) statement is encountered, the last
     * evaluation result will be returned as the result of this
     * deffunction.
     *
     * @param call The ValueVector form of the function call used to
     * invoke this deffunction.
     * @param context The execution context
     * @exception JessException If anything goes wrong
     * @return As described above.  */
    public Value call(ValueVector call, Context context) throws JessException {

        // Clean context
        Context c = context.getEngine().getGlobalContext().push();
        c.clearReturnValue();
        Value result = Funcall.NIL;

        try {

            int minimumNArgs = m_arguments.size() -
                (m_hasWildcard ? 1 : 0);

            if (call.size() < (minimumNArgs + 1))
                throw new JessException(m_name,
                                        "Too few arguments to deffunction",
                                        m_name);

            // set up the variable table. Note that args are resolved in
            // the parent's context.

            for (int arg=0; arg<m_arguments.size(); arg++) {
                Argument b = (Argument) m_arguments.get(arg);
                switch (b.m_type) {
                    // No default bindings for locals
                case RU.LOCAL:
                    continue;

                    // all others variables come from arguments
                case RU.VARIABLE:
                    c.setVariable(b.m_name,
                                  call.get(arg+1).resolveValue(context));
                    break;

                case RU.MULTIVARIABLE: {
                    ValueVector vv = new ValueVector();
                    for (int subarg=arg+1; subarg< call.size(); subarg++) {
                        Value v = call.get(subarg).resolveValue(context);
                        if (v.type() == RU.LIST) {
                            ValueVector list = v.listValue(context);
                            for (int k=0; k<list.size(); k++)
                                vv.add(list.get(k).resolveValue(context));
                        }
                        else
                            vv.add(v);
                    }
                    c.setVariable(b.m_name, new Value(vv, RU.LIST));
                    break;
                }
                }
            }

            // OK, now run the function. For every action...
            int size = m_actions.size();
            for (int i=0; i<size; i++) {
                result = ((Value) m_actions.get(i)).resolveValue(c);

                if (c.returning()) {
                    result = c.getReturnValue();
                    c.clearReturnValue();
                    break;
                }
            }
        } catch (JessException re) {
            re.addContext("deffunction " + m_name);
            throw re;
        } finally {
            c.pop();
        }
        return result.resolveValue(c);
    }

    /**
     * Describe myself
     * @return a pretty-print representation of this function
     */
    public String toString() {
        return "[deffunction " + m_name + "]";
    }

    public Object accept(Visitor v) {
        return v.visitDeffunction(this);
    }
}


