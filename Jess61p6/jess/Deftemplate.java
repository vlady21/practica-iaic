package jess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;

/** **********************************************************************
 * Class used to represent Jess deftemplates.
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation<BR>
 * $Id: Deftemplate.java,v 1.4 2003/03/24 15:49:04 ejfried Exp $
 ********************************************************************** */
public class Deftemplate implements Serializable, Visitable, Modular {

    // Type objects for 'type' qualifiers in Deftemplates.
    private static Hashtable s_types = new Hashtable();
    private static final String[] s_typenames = {
        "ANY",
        "INTEGER", "FLOAT", "NUMBER",
        "ATOM", "STRING", "LEXEME",
        "OBJECT", "LONG"
    };
    private static final int[] s_typevals = {
        -1,
        RU.INTEGER, RU.FLOAT,
        RU.INTEGER | RU.FLOAT,
        RU.ATOM, RU.STRING,
        RU.ATOM | RU.STRING,
        RU.EXTERNAL_ADDRESS, RU.LONG};

    private static Deftemplate
            s_rootTemplate = new Deftemplate(RU.ROOT_DEFTEMPLATE, "Parent template"),
    s_clearTemplate = new Deftemplate("__clear", "(Implied)"),
    s_nullTemplate = new Deftemplate("__not_or_test_CE", "(Implied)"),
    s_initialTemplate = new Deftemplate("initial-fact", "(Implied)");

    static {
        try {
            for (int i = 0; i < s_typenames.length; i++)
                s_types.put(s_typenames[i],
                        new Value(s_typevals[i], RU.INTEGER));
        } catch (JessException re) { /* can't happen */
        }
    }

    /**
     * All deftemplates ultimately extend this root template.
     * @return The singleton root template.
     */
    public static Deftemplate getRootTemplate() {
        return s_rootTemplate;
    }

    /**
     * The template for "initial-fact".
     * @return A deftemplate.
     */
    public static Deftemplate getInitialTemplate() {
        return s_initialTemplate;
    }

    /**
     * The template for a special fact used internally by Jess.
     * @return A deftemplate
     */
    public static Deftemplate getClearTemplate() {
        return s_clearTemplate;
    }

    /** The template for a special fact used internally by Jess.
     * @return A deftemplate
     */
    public static Deftemplate getNullTemplate() {
        return s_nullTemplate;
    }

    static void addStandardTemplates(Rete r) throws JessException {
        r.addDeftemplate(getRootTemplate());
        r.addDeftemplate(getNullTemplate());
        r.addDeftemplate(getClearTemplate());
        r.addDeftemplate(getInitialTemplate());
    }

    private boolean m_backchain;
    private String m_baseName;
    private String m_fullName;
    private String m_docstring = "";
    private Deftemplate m_parent;
    ValueVector m_data = new ValueVector();
    private HashMap m_indexes;

    private String m_module;

    public boolean equals(Object o) {
        if (!(o instanceof Deftemplate))
            return false;
        Deftemplate t = (Deftemplate) o;
        return (m_fullName.equals(t.m_fullName) &&
                (m_backchain == t.m_backchain) &&
                (m_parent == t.m_parent) &&
                m_data.equals(t.m_data));
    }

    public int hashCode() {
        return m_fullName.hashCode();
    }

    /**
     * Return the parent of this deftemplate. The parent is another
     * deftemplate this one extends, or null.
     * @return The parent deftemplate.
     */
    public Deftemplate getParent() {
        return m_parent;
    }

    /**
     * Sever the link with this deftemplate's parent. Useful when
     * creating similar, but unrelated deftemplates.
     */
    public void forgetParent() {
        m_parent = null;
    }


    /**
     * Get the name of this deftemplate qualified by the module name
     * @return The name of this deftemplate
     */
    public final String getName() {
        return m_fullName;
    }

    /**
     * Get the name of this deftemplate unqualified by the module name
     * @return The name of this deftemplate
     */
    public final String getBaseName() {
        return m_baseName;
    }

    /**
     * Get the docstring of this deftemplate
     * @return The docstring
     */
    public final String getDocstring() {
        return m_docstring;
    }

    /**
     * Make this deftemplate backwards-chaining reactive.
     */
    public final void doBackwardChaining() {
        m_backchain = true;
    }

    /**
     * Get the backchaining reactivity of this deftemplate.
     * @return True if this deftemplate can stimulate backwards chaining.
     */
    public final boolean getBackwardChaining() {
        return m_backchain;
    }

    /**
     * Create a template.
     * @param name The deftemplate name
     * @param docstring The deftemplate's documentation string
     */
    public Deftemplate(String name, String docstring, Rete engine)
            throws JessException {

        this(name, docstring, s_rootTemplate, engine);
    }

    /**
     * Create a deftemplate 'derived from' another
     * one. If the name contains a module name, it will be
     * used. Otherwise, the template will be in the current module.
     * @param name The deftemplate name
     * @param docstring The deftemplate's documentation string
     * @param dt The 'parent' of this deftemplate */

    public Deftemplate(String name, String docstring, Deftemplate dt, Rete engine)
            throws JessException {
        // ###

        int colons = name.indexOf("::");
        if (colons != -1) {
            m_module = name.substring(0, colons);
            engine.verifyModule(m_module);
            m_baseName = name.substring(colons + 2);
            m_fullName = name;
        } else {
            m_module = engine.getCurrentModule();
            m_baseName = name;
            m_fullName = engine.resolveName(name);
        }

        m_parent = dt;
        m_docstring = docstring;

        for (int i = 0; i < dt.m_data.size(); i++)
            m_data.add(dt.m_data.get(i));

        m_indexes = (HashMap) dt.m_indexes.clone();
    }

    /**
     * Used only to construct (not), (test), (__fact), etc.
     */

    private Deftemplate(String name, String docstring) {
        // ###
        m_module = Defmodule.MAIN;
        m_baseName = name;
        m_fullName = RU.scopeName(m_module, m_baseName);
        m_docstring = docstring;
        if (!name.equals(RU.ROOT_DEFTEMPLATE))
            m_parent = s_rootTemplate;
        m_indexes = new HashMap();
    }


    /**
     * Create a new slot in this deftemplate. If the slot already
     * exists, just change the default value.
     * @param name Name of the slot
     * @param value default value for the slot
     * @param typename Type of the slot: INTEGER, FLOAT, ANY, etc.
     * @exception JessException If something goes wrong */
    public void addSlot(String name, Value value, String typename)
            throws JessException {
        int idx;
        Value type = (Value) s_types.get(typename.toUpperCase());
        if (type == null)
            throw new JessException("Deftemplate.addSlot",
                    "Bad slot type:", typename);

        // Just set default if duplicate
        if ((idx = getSlotIndex(name)) != -1) {
            m_data.set(value, (idx * RU.DT_SLOT_SIZE) + RU.DT_DFLT_DATA);
            m_data.set(type, (idx * RU.DT_SLOT_SIZE) + RU.DT_DATA_TYPE);

        } else {
            int start = m_data.size();
            m_data.setLength(start + RU.DT_SLOT_SIZE);
            m_data.set(new Value(name, RU.SLOT), start + RU.DT_SLOT_NAME);
            m_data.set(value, start + RU.DT_DFLT_DATA);
            m_data.set(type, start + RU.DT_DATA_TYPE);
            m_indexes.put(name, new Integer(start / RU.DT_SLOT_SIZE));
        }
    }

    /**
     * Create a new multislot in this deftemplate. If the slot already
     * exists, just change the default value. Public so
     * reflectfunctions can use.
     * @param name Name of the slot
     * @param value default value for the slot
     * @exception JessException If something goes wrong */
    public void addMultiSlot(String name, Value value) throws JessException {
        int idx;

        // Just set default if duplicate
        if ((idx = getSlotIndex(name)) != -1) {
            m_data.set(value, absoluteIndex(idx) + RU.DT_DFLT_DATA);
            return;
        } else {
            int start = m_data.size();
            m_data.setLength(start + RU.DT_SLOT_SIZE);
            m_data.set(new Value(name, RU.MULTISLOT), start + RU.DT_SLOT_NAME);
            m_data.set(value, start + RU.DT_DFLT_DATA);
            m_data.set((Value) s_types.get(s_typenames[0]),
                    start + RU.DT_DATA_TYPE);
            m_indexes.put(name, new Integer(start / RU.DT_SLOT_SIZE));
        }
    }

    private int absoluteIndex(int index) {
        return index * RU.DT_SLOT_SIZE;
    }

    /**
     * Returns the slot data type (one of the constants in jess.RU)
     * for the slot given by the zero-based index.
     * @param index The zero-based index of the slot (0, 1, 2
     * ... getNSlots()-1)
     * @return The data type of that slot (RU.INTEGER, RU.ATOM, etc.,
     * or RU.NONE)
     *  */

    public int getSlotDataType(int index)
            throws JessException {
        return m_data.get(absoluteIndex(index) + RU.DT_DATA_TYPE).intValue(null);
    }

    /**
     * Returns the default value of a slot given by the zero-based
     * index.
     * @param index The zero-based index of the slot (0, 1, 2
     * ... getNSlots()-1)
     * @return The default value for that slot (can be Funcall.NIL or
     * Funcall.NILLIST for none
     */

    public Value getSlotDefault(int index)
            throws JessException {
        return m_data.get(absoluteIndex(index) + RU.DT_DFLT_DATA);
    }

    /**
     * Returns the slot type (RU.SLOT or RU.MULTISLOT) of the slot in
     * this deftemplate given by the zero-based index.
     * @param index The zero-based index of the slot (0, 1, 2
     * ... getNSlots()-1)
     * @return The type of that slot (RU.SLOT or RU.MULTISLOT)
     * */

    public int getSlotType(int index)
            throws JessException {
        return m_data.get(absoluteIndex(index) + RU.DT_SLOT_NAME).type();
    }

    /**
     * Return the index (0 through getNSlots()-1) of the named slot,
     * or -1 if there is no such slot.
     * @param slotname The name of the slot
     * @return The zero-based index of the slot
     */
    public int getSlotIndex(String slotname) throws JessException {
        Integer i = (Integer) m_indexes.get(slotname);
        if (i == null)
            return -1;
        else
            return i.intValue();
    }

    /**
     * Return the name of a given slot in this deftemplate
     * @param index The zero-based index of the slot (0, 1, 2
     * ... getNSlots()-1)
     * @return The name of that slot
     * @exception JessException If something is horribly wrong */
    public String getSlotName(int index) throws JessException {
        return m_data.get(absoluteIndex(index) + RU.DT_SLOT_NAME).
                stringValue(null);
    }

    /**
     * Return the number of slots in this deftemplate
     * @return The number of slots in this deftemplate
     */
    public int getNSlots() {
        return m_data.size() / RU.DT_SLOT_SIZE;
    }

    /**
     * Turn this deftemplate into a String
     * @return a string representation of the Deftemplate
     */
    public String toString() {
        return "[deftemplate " + m_fullName + "]";
    }

    public Object accept(Visitor v) {
        return v.visitDeftemplate(this);
    }

    public String getModule() {
        return m_module;
    }

    public boolean isBackwardChainingTrigger() {
        return m_baseName.startsWith(RU.BACKCHAIN_PREFIX);
    }

    public String getBackchainingTemplateName() {
        return RU.scopeName(m_module, RU.BACKCHAIN_PREFIX + m_baseName);
    }

    public String getNameWithoutBackchainingPrefix() {
        if (!isBackwardChainingTrigger())
            return m_fullName;
        else
            return RU.scopeName(m_module,
                    m_baseName.substring(RU.BACKCHAIN_PREFIX.length()));
    }

    public Deftemplate getBackchainingTemplate(Rete engine)
            throws JessException {

        return new Deftemplate(getBackchainingTemplateName(),
                "Goal seeker for " + m_fullName,
                this, engine);
    }

}


