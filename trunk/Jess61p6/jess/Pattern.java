package jess;

import java.io.Serializable;
import java.util.Map;

/** **********************************************************************
 * Pattern represents a single conditional element on a rule LHS.
 * A Pattern consists mainly of a two-dimensional array of Test1 structures.
 * Each Test1 contains information about a specific characteristic of a slot.
 *
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class Pattern implements ConditionalElement, LHSComponent, Serializable, Visitable {

    /**
       The deftemplate corresponding to this pattern
    */

    private Deftemplate m_deft;
    private static final int INITSIZE = 1;

    public Object clone() {
        try {
            Pattern p = (Pattern) super.clone();
            if (m_slotLengths != null)
                p.m_slotLengths = (int[]) m_slotLengths.clone();
            if (m_tests != null)
                p.m_tests = (Test1[][]) m_tests.clone();
            for (int i=0; i<m_tests.length; ++i) {
                if (m_tests[i] != null) {
                    p.m_tests[i] = (Test1 []) m_tests[i].clone();
                    for (int j=0; j<m_tests[i].length; ++j) {
                        p.m_tests[i][j] = (Test1) m_tests[i][j].clone();
                    }
                }
            }

            return p;

        } catch (CloneNotSupportedException cnse) {
            return null;
        }
    }

    /* The Slot tests for this pattern */
    private Test1[][] m_tests;
    private int m_slotLengths[];

    /* Am I in a (not () ) ? */
    private boolean m_negated;

    /* Do I provide logical support? */
    private boolean m_logical;

    /* Only match explicitly, no backwards chaining */
    private boolean m_explicit;

    /* Class of fact matched by this pattern */
    private String m_name;

    /* Bound to a variable if non-null */
    private String m_boundName;

    public Pattern(String name, Rete engine)
        throws JessException {

        this(name, engine.createDeftemplate(name));
    }

    public Pattern(String name, Deftemplate deft) {
        m_name = name;
        m_deft = deft;

        int nvalues = m_deft.getNSlots();
        m_tests = new Test1[nvalues][];
        m_slotLengths = new int[nvalues];
        for (int i=0; i<nvalues; i++)
            m_slotLengths[i] = -1;
    }

    /* Creates a new Pattern which shares some data, but with a new name.
       Used by backchaining stuff. */

    Pattern(Pattern p, String name) throws JessException {
        m_name = name;
        m_deft = p.m_deft;

        // We need to copy the tests and replace any blank variables with
        // new blanks (with new names.)
        m_tests = new Test1[p.m_tests.length][];
        for (int i=0; i<m_tests.length; i++) {
            m_tests[i] = (p.m_tests[i] == null) ?
                null : new Test1[p.m_tests[i].length];
            if (m_tests[i] != null) {
                System.arraycopy(p.m_tests[i], 0,
                                 m_tests[i], 0, m_tests[i].length);
                for (int j=0; j<m_tests[i].length; j++) {
                    Value v = m_tests[i][j].m_slotValue;
                    if (v instanceof Variable &&
                        v.variableValue(null).
                        startsWith(Tokenizer.BLANK_PREFIX))
                        m_tests[i][j] =
                            new Test1(m_tests[i][j],
                                      new Variable(RU.gensym(Tokenizer.BLANK_PREFIX),
                                                   v.type()));
                }
            }
        }

        m_slotLengths = p.m_slotLengths;
    }

    /**
     * Set the length of a multislot within a pattern
     */
    public void setSlotLength(String slotname, int length)
        throws JessException {
        int index = m_deft.getSlotIndex(slotname);
        if (index == -1)
            throw new JessException("Pattern.setSlotLength",
                                    "No such slot " + slotname + " in template",
                                    m_deft.getName());

        m_slotLengths[index] = length;
    }

    /**
     * Add a test to this pattern
     */
    public void addTest(String slotname, Test1 aTest)
        throws JessException {

        // try to find this slotname in the deftemplate
        int idx = m_deft.getSlotIndex(slotname);
        if (idx == -1)
            throw new JessException("Pattern.addTest",
                                    "No such slot " + slotname + " in template ",
                                    m_deft.getName());

        if (m_tests[idx] == null)
            m_tests[idx] = new Test1[INITSIZE];

        int j=0;
        while (j < m_tests[idx].length && m_tests[idx][j] != null)
            ++j;

        if (j == m_tests[idx].length) {
            Test1[] tmp = new Test1[j+1];
            System.arraycopy(m_tests[idx], 0, tmp, 0, j);
            m_tests[idx] = tmp;
        }

        // Tests must be added in subslot-order!
        if (j > 0 && m_tests[idx][j-1].m_subIdx > aTest.m_subIdx)
            throw new JessException("Pattern.addTest",
                                    "Attempt to add out-of-order test: index ",
                                    m_tests[idx][j-1].m_subIdx + " > "
                                    + aTest.m_subIdx);

        m_tests[idx][j] = aTest;

    }

    void replaceTests(int slotIndex, Test1[] theTests) {
        m_tests[slotIndex] = theTests;
    }

    /* Add the name of every variable that is "directly positively
       matched" in this pattern to the Map as a key/value pair. If a
       name is already there, that's OK. */

    public void addDirectlyMatchedVariables(Map map) throws JessException {
        for (int i=0; i< getNSlots(); i++) {
            for (int j=0; j< getNTests(i); j++) {
                Test1 test = getTest(i, j);
                Value val = test.m_slotValue;
                boolean eq = (test.m_test == Test1.EQ);
                if (val instanceof Variable && eq) {
                    String name = val.variableValue(null);
                    map.put(name, name);
                }
            }
        }
        if (getBoundName() != null)
            map.put(getBoundName(), getBoundName());
    }

    // TODO V&V

    public void renameUnmentionedVariables(Map map, Map substitutes, int size, HasLHS unused)
        throws JessException {

        String preFix = "_" + size + "_";
        for (int i=0; i< getNSlots(); i++) {
            for (int j=0; j< getNTests(i); j++) {
                Test1 test = getTest(i, j);
                Value val = test.m_slotValue;
                boolean eq = (test.m_test == Test1.EQ);
                if (val instanceof Variable && eq) {
                    String name = val.variableValue(null);
                    if (map.get(name) == null &&
                       !name.startsWith(preFix)) {
                        String sub;
                        if (substitutes.get(name) == null) {
                            sub = preFix + name;
                            substitutes.put(name, sub);
                        } else
                            sub = (String) substitutes.get(name);
                        test.m_slotValue = new Variable(sub, val.type());
                    }
                }
            }
        }
        substituteVariableNamesInFuncalls(substitutes);
    }


    private void substituteVariableNamesInFuncalls(Map substitutes)
        throws JessException {
        for (int i=0; i< getNSlots(); i++) {
            if (getNTests(i) == 0)
                continue;
            for (int j=0; j< getNTests(i); j++) {
                Value val = getTest(i, j).m_slotValue;
                if (val.type() == RU.FUNCALL)
                    substFuncall(val.funcallValue(null), substitutes);
            }
        }
    }

    private void substFuncall(Funcall f, Map substitutes)
        throws JessException {
        for (int i=1; i<f.size(); i++) {
            Value current = f.get(i);
            if (current instanceof Variable) {
                String s =
                    (String) substitutes.get(current.variableValue(null));
                if (s != null)
                    f.set(new Variable(s, current.type()), i);
            }
            else if (current instanceof FuncallValue)
                substFuncall(current.funcallValue(null), substitutes);
        }
    }


    /**
     * Is this pattern a (not()) CE pattern, possibly nested?  */
    public boolean getNegated() {
        return m_negated;
    }

    public void setNegated() {
        m_negated = true;
    }

    public void setLogical() {
        m_logical = true;
    }

    public boolean getLogical() {
        return m_logical;
    }

    public void setExplicit() {
        m_explicit = true;
    }

    public boolean getExplicit() {
        return m_explicit;
    }

    public boolean getBackwardChaining() {
        return m_deft.getBackwardChaining();
    }

    public String getName() {
        return m_name;
    }

    public void setBoundName(String s) throws JessException {
        if ((m_negated || m_name.equals("test")) && s != null)
            throw new JessException("Pattern.setBoundName",
                                    "Can't bind negated pattern to variable",
                                    s);
        m_boundName = s;
    }

    public String getBoundName() {
        if (isBackwardChainingTrigger() && m_boundName == null)
            m_boundName = RU.gensym("__factidx");
        return m_boundName;
    }

    public int getNSlots() {
        return m_tests.length;
    }

    public int getNTests(int slot) {
        if (m_tests[slot] == null)
            return 0;
        else
            return m_tests[slot].length;
    }

    public int getSlotLength(int slot) {
        return m_slotLengths[slot];
    }

    public int getNMultifieldsInSlot(int slot) {
        int count=0;
        if (m_tests[slot] == null)
            return count;
        for (int i=0; i<m_tests[slot].length; i++)
            if (m_tests[slot][i].m_slotValue.type() == RU.MULTIVARIABLE)
                ++count;

        return count;
    }

    public boolean isMultifieldSubslot(int slot, int subslot) {
        if (m_tests[slot] == null)
            return false;

        for (int i=0; i<m_tests[slot].length; i++)
            if (m_tests[slot][i].m_slotValue.type() == RU.MULTIVARIABLE &&
                m_tests[slot][i].m_subIdx == subslot)
                return true;
        return false;
    }

    boolean[] getMultifieldFlags(int slot) {
        boolean [] indexes = new boolean[getSlotLength(slot)];

        for (int i=0; i<getSlotLength(slot); i++)
            if (isMultifieldSubslot(slot, i))
                indexes[i] = true;

        return indexes;
    }

    public Test1 getTest(int slot, int test) {
        return m_tests[slot][test];
    }

    public Deftemplate getDeftemplate() {
        return m_deft;
    }

    public void addToGroup(Group g) throws JessException {
        Pattern p = (Pattern) clone();
        g.m_data.add(p);
    }

    public LHSComponent canonicalize() {
        return this;
    }

    public Object accept(Visitor jv) {
        return jv.visitPattern(this);
    }

    public int getGroupSize() {
        return 1;
    }

    public boolean isGroup() {
        return false;
    }

    public ConditionalElement getConditionalElement(int i) {
        return (ConditionalElement) getLHSComponent(i);
    }

    public int getPatternCount() {
        return 1;
    }


    public LHSComponent getLHSComponent(int i) {
        if (i > 0)
            throw new IllegalArgumentException();
        else
            return this;
    }

    public boolean equals(Object o) {
        if (! (o instanceof Pattern))
            return false;

        Pattern p = (Pattern) o;
        if (!getName().equals(p.getName()))
            return false;

        if (m_negated != p.m_negated)
            return false;

        for (int i=0; i<m_slotLengths.length; ++i)
            if (m_slotLengths[i] != p.m_slotLengths[i])
                return false;

        for (int i=0; i<m_tests.length; ++i) {
            if (m_tests[i] == null ||
                p.m_tests[i] == null) {
                if (m_tests[i] != p.m_tests[i])
                    return false;

            } else {

                if (m_tests[i].length != p.m_tests[i].length)
                    return false;

                for (int j=0; j<m_tests[i].length; ++j) {
                    if ((m_tests[i][j] == null ||
                         p.m_tests[i][j] == null) &&
                        m_tests[i][j] != p.m_tests[i][j])
                        return false;

                    if (!m_tests[i][j].equals(p.m_tests[i][j]))
                        return false;
                }
            }
        }
        return true;
    }

    public boolean isBackwardChainingTrigger() {
        return !m_negated && m_deft.isBackwardChainingTrigger();
    }

    public String getBackchainingTemplateName() {
        return m_deft.getBackchainingTemplateName();
    }

    public String getNameWithoutBackchainingPrefix() {
        return m_deft.getNameWithoutBackchainingPrefix();
    }

    public String toString() {
        return "(" + getName() + ")";
    }
}


