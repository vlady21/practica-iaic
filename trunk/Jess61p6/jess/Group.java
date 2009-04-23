package jess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A group on the LHS of a rule, like an AND, OR, NOT, EXPLICIT, or
 * other special CE.
 * (C) 2001 E.J. Friedman-Hill and Sandia National Laboratories
 */

class Group implements ConditionalElement, LHSComponent, Serializable, Visitable {
    private String m_name;
    private boolean m_explicit, m_logical;
    CEVector m_data;
    private boolean m_unary = false;

    static final String
        AND="and", UNIQUE="unique", EXPLICIT="explicit",
        NOT="not", EXISTS="exists", TEST="test", OR="or",
        LOGICAL="logical";

    private final static Pattern s_initialFactPattern =
        new Pattern(Deftemplate.getInitialTemplate().getName(),
                    Deftemplate.getInitialTemplate());

    private final static Pattern s_logicalInitialFactPattern;

    static {
        s_logicalInitialFactPattern =
            new Pattern(Deftemplate.getInitialTemplate().getName(),
                        Deftemplate.getInitialTemplate());
        s_logicalInitialFactPattern.setLogical();
    }

    public Object clone() {
        try {
            Group g = (Group) super.clone();
            g.m_data = new CEVector();
            for (int i=0; i<m_data.size(); ++i) {
                g.m_data.add((LHSComponent) getLHSComponent(i).clone());
            }
            return g;
        } catch (CloneNotSupportedException cnse) {
            throw new IllegalArgumentException();
        }
    }

    Group(String name) throws JessException {
        m_data = new CEVector();
        m_name = name;

        if (m_name.equals(EXPLICIT)) {
            m_explicit = true;
            m_unary = true;
        }

        else if (m_name.equals(LOGICAL)) {
            m_logical = true;
            m_unary = false;
        }

        else if (m_name.equals(NOT)) {
            m_unary = true;
        }

        else if (m_name.equals(EXISTS)) {
            throw new JessException("Group::Group", "Invalid CE name", EXISTS);
        }
    }

    public String getName() {
        return m_name;
    }

    public int getPatternCount() {
        if (isNegatedName(m_name))
            return 1;
        else {
            int sum = 0;
            for (int i=0; i<m_data.size(); ++i)
                sum += m_data.get(i).getPatternCount();
            return sum;
        }
    }

    void add(LHSComponent g) throws JessException {
        verifyAdditionIsAllowed(g);

        // Remove single-branch ORs if added to anything.
        if (g.getName().equals(OR) && g.getGroupSize() == 1)
            g = g.getLHSComponent(0);


        // Remove single-branch ANDs if added to anything,
        // unless they are being added to an OR CE
        if (g.getName().equals(AND) && g.getGroupSize() == 1 &&
            !getName().equals(OR))
            g = g.getLHSComponent(0);

        // Flatten nested ORs immediately.
        if (m_name.equals(OR) && g.getName().equals(OR)) {
            Group grp = (Group) g;
            for (int i=0; i<grp.m_data.size(); i++)
                add(grp.m_data.get(i));
        }

        // Flatten nested ANDs immediately.
        else if (m_name.equals(AND) && g.getName().equals(AND)) {
            Group grp = (Group) g;
            for (int i=0; i<grp.m_data.size(); i++)
                add(grp.m_data.get(i));
        }

        // Rearrange an (OR) inside of a (NOT)
        // According to (not (or A B)) => (and (not A) (not B))
        else if (m_name.equals(NOT) && g.getName().equals(OR)) {
            m_name = AND;
            m_unary = false;
            Group grp = (Group) g;
            for (int i=0; i<grp.m_data.size(); i++) {
                Group not = new Group(NOT);
                not.add(grp.m_data.get(i));
                add(not);
            }
        }

        // If we're a NOT, and the new addition has an ORs in it,
        // they should be canonicalized.
        else if (m_name.equals(NOT) && g.getName().equals(AND) && hasEmbeddedORs((Group) g)) {
            g = g.canonicalize();
            add(g);
        }

        // Otherwise keep the tree-like structure
        else
            m_data.add(g);



        if (m_explicit)
            setExplicit();

        if (m_logical)
            setLogical();

        if (getNegated())
            setNegated();
    }

    private boolean hasEmbeddedORs(Group g) {
        if (g.getName().equals(OR))
            return true;
        for (int i=0; i<g.getGroupSize(); ++i) {
            LHSComponent ce = g.getLHSComponent(i);
            if (ce instanceof Group)
                if (hasEmbeddedORs((Group) ce))
                    return true;
        }
        return false;
    }

    private void verifyAdditionIsAllowed(LHSComponent g) throws JessException {
        if (m_data.size() > 0 && m_unary)
            throw new JessException("Group.add",
                                    "CE is a unary modifier", m_name);

        else if (m_name.equals(LOGICAL) && g.getName().equals(TEST))
            throw new JessException("Group.add",
                                    "CE can't be used in logical:", "test");

        else if (m_name.equals(NOT) && g.getName().equals(LOGICAL))
            throw new JessException("Group.add",
                                    "CE can't be used in not:", "logical");

    }

    public boolean getBackwardChaining() {
        return false;
    }

    public void setExplicit() {
        for (int i=0; i<m_data.size(); i++)
            m_data.get(i).setExplicit();
        m_explicit = true;
    }

    public void setLogical() {
        for (int i=0; i<m_data.size(); i++)
            m_data.get(i).setLogical();
        m_logical = true;
    }

    public boolean getLogical() {
        return m_logical;
    }

    public boolean getNegated() {
        return m_name.equals(NOT);
    }

    public void setNegated() {
        for (int i=0; i<m_data.size(); i++)
            m_data.get(i).setNegated();
    }

    public void setBoundName(String name) throws JessException {
        if (m_name.equals(NOT) ||
            m_name.equals(TEST) ||
            (m_data.size() > 1 && !m_name.equals(OR)))
            throw new JessException("Group.setBoundName",
                                    "This CE can't be bound to a variable",
                                    m_name);

        for (int i=0; i<m_data.size(); i++)
            m_data.get(i).setBoundName(name);
    }

    public String getBoundName() {
        return m_data.get(0).getBoundName();
    }

    static boolean isGroupName(String s) {
        return
            isNegatedName(s) ||
            s.equals(AND) ||
            s.equals(OR) ||
            s.equals(LOGICAL) ||
            s.equals(EXPLICIT);
    }

    static boolean isNegatedName(String s) {
        return s.equals(NOT);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("(");
        sb.append(m_name);
        for (int i=0; i<m_data.size(); i++) {
            sb.append('\n');
            sb.append(' ');
            sb.append(m_data.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    private int countNumberOfBranches(LHSComponent[] lhss) throws JessException {

        for (int i=0; i<lhss.length; i++)
            lhss[i] = m_data.get(i).canonicalize();

        int product;
        if (m_name.equals(OR)) {
            product = 0;
            for (int i=0; i<lhss.length; i++) {
                product += lhss[i].getGroupSize();
            }
        }
        else {
            product = 1;
            for (int i=0; i<lhss.length; i++) {
                product *= lhss[i].getGroupSize();
            }
        }
        return product;
    }


    public LHSComponent canonicalize() throws JessException {

        LHSComponent[] lhss = new LHSComponent[m_data.size()];
        int product = countNumberOfBranches(lhss);

        if (product == 1) {
            Group g = new Group(OR);
            g.add(this);
            return g;
        }

        // Each element in rv is a new LHS being built.
        Group[] branches = new Group[product];

        for (int i=0; i<product; i++)
            branches[i] = new Group(AND);

        if (m_name.equals(OR)) {
            // If we're an OR, then we're just the OR of all the children.
            // Each child in lhss is just a list of one or more OR-options
            int index=0;
            for (int i=0; i<lhss.length; i++)
                for (int j=0; j<lhss[i].getGroupSize(); j++)
                    branches[index++].add(lhss[i].getLHSComponent(j));
        }
        else {
            // Otherwise, it's a little more complex. We need to
            // distribute our OR children. "Repeat" is the number of
            // consecutive LHSs in the list a given sub-OR element
            // will be added to. It will get smaller and smaller and
            // end up as 1 for the last OR.

            int copies = product;
            for (int i=0; i<lhss.length; i++) {
                // If there's only one option in a branch, add it to every LHS.
                if (lhss[i].getGroupSize() == 1) {
                    for (int j=0; j<product; j++)
                        branches[j].add(lhss[i].getLHSComponent(0));
                }

                // If there are multiple options, we need to spread them
                // out.  We'll make "copies" copies of each option,
                // repeating the overall pattern "repeat" times. Each time
                // we encounter another OR in lhss, we divide "copies" by
                // the number of branches. If lhss looks like
                //
                //  A C D
                //  B   E
                //
                // Then the first and third entries are ORs, and we
                // want rv to look like
                //
                //  A A B B  copies == 2, repeat == 1
                //  C C C C
                //  D E D E  copies == 1, repeat == 2

                else {
                    copies /= lhss[i].getGroupSize();
                    int repeat = product/(copies * lhss[i].getGroupSize());
                    int index=0;
                    for (int j=0; j<repeat; j++)
                        for (int k=0; k<lhss[i].getGroupSize(); k++)
                            for (int m=0; m<copies; m++)
                                branches[index++].add(lhss[i].getLHSComponent(k));
                }
            }
        }

        Group or = new Group(OR);
        for (int i=0; i<product; ++i) {

            if (branches[i].getGroupSize() == 1 &&
                !branches[i].getLHSComponent(0).getName().equals(NOT) &&
                !branches[i].getLHSComponent(0).getName().equals(TEST))
                or.add(branches[i].getLHSComponent(0));
            else
                or.add(branches[i]);
        }

        if (m_name.equals(NOT)) {
            Group not = new Group(NOT);
            not.add(or);
            return not.canonicalize();
        }

        return or;

    }

    void insertInitialFacts() {
        LHSComponent g0 = getLHSComponent(0);
        if (m_name.equals(AND) &&
            ( m_data.size() == 0 ||
              (g0.getName().equals(LOGICAL) &&
               (isNegatedName(g0.getLHSComponent(0).getName()) ||
                g0.getLHSComponent(0).getName().equals(TEST))) ||
              g0.getName().equals(NOT) ||
              g0.getName().equals(TEST) ||
              g0.getBackwardChaining())) {
            m_data.addAtStart(g0 == null || !g0.getLogical() ?
                              (Pattern) s_initialFactPattern.clone() :
                              (Pattern) s_logicalInitialFactPattern.clone());
        }
    }

    // Call only after group is complete and simplified.
    public void addToLHS(HasLHS l, Rete engine)
        throws JessException {

        if (!getName().equals(AND))
            throw new JessException("Group.addToLHS", "Bad assumption", getName());

        for (int i=0; i<m_data.size(); i++) {
            LHSComponent ce = m_data.get(i);
            l.addCE(ce, engine);
        }
    }
    // This is called on a group of CEs early in a LHS, to rename variables found
    // in a CE being added to the LHS. Therefore it doesn't handle renaming within
    // a single CE.
     void renameVariables(Group g, HasLHS container) throws JessException {
        HashMap map = new HashMap();
        HashMap subs = new HashMap();
        addDirectlyMatchedVariables(map);
        int seq = container.getSequenceNumber();
        g.renameUnmentionedVariables(map, subs, seq, container);
    }

    public void addToGroup(Group g) throws JessException {
        insertInitialFacts();

        for (int i=0; i<m_data.size(); i++) {
            LHSComponent ce = m_data.get(i);
            g.m_data.add((LHSComponent) ce.clone());
        }
    }

    public int getGroupSize() {
        return m_data.size();
    }

    public boolean isGroup() {
        return true;
    }

    public ConditionalElement getConditionalElement(int i) {
        return (ConditionalElement) getLHSComponent(i);
    }

    public LHSComponent getLHSComponent(int i) {
        return m_data.get(i);
    }

    public void addDirectlyMatchedVariables(Map map) throws JessException {
        for (int i=0; i<m_data.size(); i++)
            m_data.get(i).addDirectlyMatchedVariables(map);
    }

    // This doesn't properly recognize the case where a variable is mentioned twice
    // within a single group CE but within different patterns, where the first reference is
    public void renameUnmentionedVariables(Map map, Map subs, int size, HasLHS container)
        throws JessException {
        if (isNegatedName(m_name)) {
            size = container.getSequenceNumber();
            subs = new HashMap(subs);
        }
        for (int i=0; i<m_data.size(); i++) {
            m_data.get(i).renameUnmentionedVariables(map, subs, size, container);
        }
    }


    public boolean isBackwardChainingTrigger() { return false; }

    public Object accept(Visitor v) {
        return v.visitGroup(this);
    }

    static class CEVector implements Serializable {
        private LHSComponent[] m_data = new LHSComponent[1];
        private int m_nData;

        void addAtStart(LHSComponent g) {
            if (m_data.length == m_nData) {
                LHSComponent[] temp = new LHSComponent[m_nData * 2];
                System.arraycopy(m_data, 0, temp, 0, m_nData);;
                m_data = temp;
            }
            System.arraycopy(m_data, 0, m_data, 1, m_nData);
            m_data[0] = g;
            ++m_nData;
        }

        void add(LHSComponent g) {

            if (m_data.length == m_nData) {
                LHSComponent[] temp = new LHSComponent[m_nData * 2];
                System.arraycopy(m_data, 0, temp, 0, m_nData);;
                m_data = temp;
            }
            m_data[m_nData++] = g;
        }

        LHSComponent get(int i) {
            return m_data[i];
        }


        int size() {
            return m_nData;
        }
    }
}








