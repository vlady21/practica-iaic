package jess;

import java.io.Serializable;

/** **********************************************************************
 * A Fact is a ValueVector where the entries are the slot data in
 * declaration order. The "head" of the fact, id, etc., are -not- stored in
 * the vector.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class Fact extends ValueVector implements Serializable, Modular {
    private int m_id = -1;
    private Deftemplate m_deft;
    public static final int NO=0, DYNAMIC=1, STATIC=2;
    private int m_shadow;
    private String m_name;
    private Fact m_icon;
    private int m_time;

    public Fact getIcon() {
        return m_icon;
    }

    void setIcon(Fact f) {
        m_icon = f;
    }

    private static Fact s_nullFact, s_initialFact, s_clearFact;

    static {
        try {
            s_nullFact = new Fact(Deftemplate.getNullTemplate());
            s_clearFact = new Fact(Deftemplate.getClearTemplate());
            s_initialFact = new Fact(Deftemplate.getInitialTemplate());
        } catch (JessException je) {
            // Can't happen
        }
    }


    static Fact getNullFact() {
        return s_nullFact;
    }

    static Fact getInitialFact() {
        return (Fact) s_initialFact.clone();
    }

    static Fact getClearFact() {
        return s_clearFact;
    }

    public String getName() {
        return m_name;
    }

    /**
     * Returns this Fact's fact-id.
     * @return The fact-id
     */
    public int getFactId() {
        return m_id;
    }

    void setFactId(int i) {
        m_id = i;
    }

    void setShadowMode(int mode) {
        m_shadow = mode;
    }

    /**
     * Indicates whether this Fact is a shadow fact for a matched Bean.
     * @return True is this is a shadow fact
     */
    public boolean isShadow() {
        return m_shadow > 0;
    }

    /**
     * Indicates whether this fact is a shadow fact, and if so, what type.
     * @return NO, DYNAMIC, or STATIC from this class
     */
    public int getShadowMode() {
        return m_shadow;
    }

    /**
     * Return the deftemplate for this fact.
     * @return The deftemplate for this fact
     */

    public final Deftemplate getDeftemplate()  {
        return m_deft;
    }

    public final String getModule() {
        return m_deft.getModule();
    }

    public int getTime() {
        return m_time;
    }

    void updateTime(int time) {
        m_time = time;
    }

    public Value get(int i) throws JessException {
        if (i == -1)
            return new FactIDValue(this);
        else
            return super.get(i);
    }


  /**
   * Basic constructor. If the deftemplate is an unordered
   * deftemplate, default values are copied from the deftemplate.
   * @param template The deftemplate to use
   * @exception JessException If anything goes wrong
   */

    public Fact(Deftemplate template) throws JessException {
        m_deft = template;
        createNewFact();
        m_time = 0;
        m_icon = this;
    }

  /**
   * Basic constructor. If name is not a known deftemplate, an implied
   * ordered deftemplate is created. If it is a known unordered
   * deftemplate, default values are copied from the deftemplate.
   * @param name The head or name of the fact
   * @param engine The engine in which to find the deftemplate
   * @exception JessException If anything goes wrong
   */

    public Fact(String name, Rete engine) throws JessException {
        if (name.equals("not") || name.equals("test") || name.equals("explicit"))
            throw new JessException("Fact.Fact",
                                    "Illegal fact name:", name);


        m_deft = engine.createDeftemplate(name);
        createNewFact();
        m_time = engine.getTime();
        m_icon = this;
    }

    /**
     * Starts from another Fact. No default values are
     * filled in; the ValueVector is assumed to already be complete.
     * @param f The ValueVector form of a fact
     * @exception JessException If anything goes wrong.
     */
    public Fact(Fact f) throws JessException {
        m_name = f.m_name;
        m_deft = f.m_deft;
        setLength(f.size());
        for (int i=0; i<size(); i++)
            set(f.get(i), i);
        m_time = f.m_time;
        m_id = f.m_id;
        m_icon = this;
    }


    /**
     * Make a copy of this fact
     * @return The copy
     */
    public Object clone() {
        try {
            return new Fact(this);
        } catch (JessException re) {
            // can't happen
            return null;
        }
    }

    private void createNewFact() throws JessException {
        int size = m_deft.getNSlots();
        setLength(size);
        m_name = m_deft.getName();
        m_shadow = NO;

        for (int i=0; i<size; i++)
            set(m_deft.getSlotDefault(i), i);
    }

    private final int findSlot(String slotname) throws JessException {
        int index = m_deft.getSlotIndex(slotname);
        if (index == -1)
            throw new JessException("Fact.findSlot",
                                    "No slot " + slotname + " in deftemplate ",
                                    m_deft.getName());
        return index;
    }

  /**
   * Return the value from the named slot.
   * @param slotname The name of a slot in this fact
   * @exception JessException If anything goes wrong
   * @return The value
   */
    final public Value getSlotValue(String slotname) throws JessException {
        return get(findSlot(slotname));
    }

    /**
     * Set the value in the named slot.
     * @param slotname The name of the slot
     * @param value The new value for the slot
     * @exception JessException If anything goes wrong
     */
    final public void setSlotValue(String slotname, Value value)
        throws JessException {
        set(value, findSlot(slotname));
    }

    /**
     * Clone this fact and expand any variable references in the clone.
     * @exception JessException If anything goes wrong.
     * @return The new fact.
     */

    Fact expand(Context context) throws JessException {
        Fact fact = (Fact) clone();
        for (int j=0; j<fact.size(); j++){
            Value current = fact.get(j).resolveValue(context);
            if (current.type() == RU.LIST) {
                ValueVector vv = new ValueVector();
                ValueVector list = current.listValue(context);
                for (int k=0; k<list.size(); k++) {
                    Value listItem = list.get(k).resolveValue(context);
                    if (listItem.type() == RU.LIST) {
                        ValueVector sublist = listItem.listValue(context);
                        for (int m=0; m<sublist.size(); m++)
                            vv.add(sublist.get(m).resolveValue(context));

                    } else
                        vv.add(listItem);
                }
                current = new Value(vv, RU.LIST);
            }
            fact.set(current, j);
        }
        return fact;
    }


    List toList() {
        try {
            List l = new List(m_name);

            int nslots = size();
            // Make "Ordered" facts look ordered
            if (nslots == 1 &&
                m_deft.getSlotName(0).equals(RU.DEFAULT_SLOT_NAME)) {
                if (get(0).type() != RU.LIST) {
                    l.add(get(0));
                    return l;
                } else if (get(0).listValue(null).size() == 0)
                    return l;
                else {
                    // Omit slot name and parens
                    l.add(get(0));
                    return l;
                }
            }

            for (int i=0; i< nslots; i++) {
                l.add(new List(m_deft.getSlotName(i), get(i)));
            }
            return l;
        } catch (JessException re) {
            return new List(re.toString());
        }
    }
    /**
     * Pretty-print this fact into a String. Should always be a
     * parseable fact, except when a slot holds an external-address
     * value.
     * @return The pretty-printed String.
     */

    public String toString() {
        return toList().toString();
    }

    public String toStringWithParens() {
        return toList().toString();
    }

    /**
     * The version in ValueVector isn't good enough, since it doesn't
     * compare heads!
     */

    public boolean equals(Object o) {
        if (o == this)
            return true;

        else if (! (o instanceof Fact))
            return false;

        Fact f = (Fact) o;
        if (!m_name.equals(f.m_name))
            return false;

        return super.equals(o);

    }

    public int hashCode() {
        int code = m_name.hashCode();
        for (int i=0; i<size(); ++i)
            code += m_v[i].hashCode();
        return code;
    }


}









