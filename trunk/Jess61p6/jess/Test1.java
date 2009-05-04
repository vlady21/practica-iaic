package jess;
import java.io.Serializable;

/** **********************************************************************
 * Holds a single test in a Pattern on the LHS of a Rule.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 */
public final class Test1 implements TestBase, Serializable, Visitable, Cloneable {
    /**
       What test to do (Test1.EQ, Test1.NEQ, etc)
    */

    int m_test;

    /**
       Which subslot within a multislot (0,1,2...)
    */

    int m_subIdx;

    /**
       The datum to test against
    */

    Value m_slotValue;

    /**
       AND or OR
    */
    int m_conjunction = RU.AND;

    /**
     * Create a single test.
     * @param test TestBase.EQ or TestBase.NEQ
     * @param sub_idx The subfield of a multislot, or -1
     * @param slot_value An object test against
     * @param conjunction RU.AND or RU.OR
     * @exception JessException If something goes wrong
     */
    public Test1(int test, int sub_idx, Value slot_value, int conjunction)
        throws JessException {
        this(test, sub_idx, slot_value);
        m_conjunction = conjunction;
    }

    Test1(int test, int sub_idx, Value slot_value)
        throws JessException {
        m_test = test;
        m_subIdx = sub_idx;
        m_slotValue = slot_value;
    }

    Test1(Test1 t, Value slot_value)
        throws JessException {
        m_test = t.m_test;
        m_subIdx = t.m_subIdx;
        m_conjunction = t.m_conjunction;
        m_slotValue = slot_value;
    }


    public Object clone() {
        try {
            Test1 t = (Test1) super.clone();
            if (t.m_slotValue instanceof FuncallValue) {
                Funcall f = m_slotValue.funcallValue(null);
                f = (Funcall) f.clone();
                t.m_slotValue = new FuncallValue(f);
            }
            return t;
        } catch (CloneNotSupportedException cnse) {
            return null;
        } catch (JessException je) {
            return null;
        }
    }

    public int getTest() { return m_test; }
    public Value getValue() { return m_slotValue; }
    public int getMultiSlotIndex() { return m_subIdx; }
    public int getConjunction() { return m_conjunction; }

    public boolean doTest(Context context) throws JessException {
        boolean retval;

        retval = m_slotValue.resolveValue(context).equals(Funcall.FALSE);

        switch (m_test) {
        case EQ:
            if (retval)
                return false;
            break;

        case NEQ:
            if (!retval)
                return false;
            break;

        }
        return true;
    }



    public boolean equals(Object o) {
        if (! (o instanceof Test1))
            return false;

        Test1 t = (Test1) o;
        if (m_test != t.m_test)
            return false;

        else if (m_subIdx != t.m_subIdx)
            return false;

        else if (m_conjunction != t.m_conjunction)
            return false;

        else return m_slotValue.equals(t.m_slotValue);
    }

    public Object accept(Visitor jv) {
        return jv.visitTest1(this);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("[Test1: test=");
        sb.append(m_test == NEQ ? "NEQ" : "EQ");
        sb.append(";sub_idx=");
        sb.append(m_subIdx);
        sb.append(";slot_value=");
        sb.append(m_slotValue);
        sb.append(";conjunction=");
        sb.append(m_conjunction);
        sb.append("]");

        return sb.toString();
    }
}



