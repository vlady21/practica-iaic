
package jess;
import java.io.Serializable;

/** **********************************************************************
 * A class to represent a location within a rule LHS, used
 * internally. It is 'self-resolving' using Context. You shouldn't need
 * to use this directly except the implement an Accelerator.
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation <BR>
 * $Id: BindingValue.java,v 1.6.2.2 2003/07/08 13:53:51 ejfried Exp $
 ********************************************************************** */

public class BindingValue extends Value implements Serializable {
    private String m_name;
    private int m_factNumber, m_slotIndex, m_subIndex, m_type;
    private LHSComponent m_pattern;

    BindingValue(String name, LHSComponent patt, int factIndex, int slotIndex,
                 int subIndex, int type)
        throws JessException {
        m_name = name;
        m_pattern = patt;
        m_factNumber = factIndex;
        m_slotIndex = slotIndex;
        m_subIndex = subIndex;
        m_type = type;
    }

    BindingValue(BindingValue v) {
        super(v);
        m_name = v.m_name;
        m_factNumber = v.m_factNumber;
        m_slotIndex = v.m_slotIndex;
        m_subIndex = v.m_subIndex;
        m_type = v.m_type;
        m_pattern = v.m_pattern;
    }


    public void resetFactNumber() {
        m_factNumber = 0;
    }

    public String getName() { return m_name; }
    public int getFactNumber() { return m_factNumber; }
    public int getSlotIndex() { return m_slotIndex; }
    public int getSubIndex() { return m_subIndex; }
    public int getType() { return m_type; }

    String debugPrint() {
        StringBuffer sb = new StringBuffer("[BindingValue:");
        sb.append(getName());
        sb.append(";fact=");
        sb.append(getFactNumber());
        sb.append(";slot=");
        sb.append(getSlotIndex());
        sb.append(";sub=");
        sb.append(getSubIndex());
        sb.append(";type=");
        sb.append(getType());
        sb.append("]");
        return sb.toString();
    }

    LHSComponent getCE() { return m_pattern; }

    public Value resolveValue(Context c) throws JessException {
        if (c == null)
            throw new JessException("BindingValue.resolveValue",
                                    "Null context ", "");

        Token t = c.getToken();
        Fact f;

        Value var;
        if (t == null || m_factNumber == t.size())
            f = c.getFact();
        else
            f = t.fact(m_factNumber);

        var = f.get(m_slotIndex);

        if (m_subIndex == -1) // -1 here means no subfield
            return var;

        else {
            ValueVector subv = var.listValue(null);
            return subv.get(m_subIndex);
        }
    }

    public final Object externalAddressValue(Context c) throws JessException {
        return resolveValue(c).externalAddressValue(c);
    }

    public final Fact factValue(Context c) throws JessException {
        return resolveValue(c).factValue(c);
    }

    public final ValueVector listValue(Context c) throws JessException {
        return resolveValue(c).listValue(c);
    }

    public final int intValue(Context c) throws JessException {
        return resolveValue(c).intValue(c);
    }

    public final double floatValue(Context c) throws JessException {
        return resolveValue(c).floatValue(c);
    }

    public final double numericValue(Context c) throws JessException {
        return resolveValue(c).numericValue(c);
    }

    public final String atomValue(Context c) throws JessException {
        return resolveValue(c).atomValue(c);
    }

    public final String variableValue(Context c) throws JessException {
        return super.stringValue(c);
    }

    public final String stringValue(Context c) throws JessException {
        return resolveValue(c).stringValue(c);
    }

    public String toString() {
        return "?" + m_name;
    }

    public int hashCode() {
        return m_factNumber + 512*m_slotIndex + 512*512*m_subIndex;
    }

    /*
      This overrides the overloaded equals() in Value.
    */

    public boolean equals(Value o) {
        if (! (o instanceof BindingValue))
            return false;

        BindingValue other = (BindingValue) o;

        return (m_factNumber == other.m_factNumber &&
                m_slotIndex == other.m_slotIndex &&
                m_subIndex == other.m_subIndex);
    }
}


