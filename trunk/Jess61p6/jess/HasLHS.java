package jess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Parent class of Defrules and Defqueries.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
public abstract class HasLHS extends Node
    implements Serializable, Visitable, Modular, NodeSink {
    String m_module;
    String m_name;
    String m_displayName;
    String m_docstring = "";
    private ArrayList m_nodes = new ArrayList();
    private Hashtable m_bindings = new Hashtable();
    private Group m_CEs;
    int m_nodeIndexHash = 0;
    private StringBuffer m_compilationTrace;
    boolean m_new = true;
    boolean m_frozen = false;
    private HasLHS m_next = null;

    HasLHS(String name, String docstring, Rete engine) throws JessException {

        int colons = name.indexOf("::");
        if (colons != -1) {
            m_module = name.substring(0, colons);
            engine.verifyModule(m_module);
            m_name = name;
        } else {
            m_module = engine.getCurrentModule();
            m_name = engine.resolveName(name);
        }

        int amp = m_name.indexOf('&');
        m_displayName = (amp == -1) ? m_name : m_name.substring(0, amp);

        m_docstring = docstring;
        m_CEs = new Group(Group.AND);
    }

    /**
     * Fetch the number of  elenments on the LHS of this construct.
     * @return The number of CEs
     */
    public int getGroupSize() {
        return m_CEs.getGroupSize();
    }

    /**
     * Return the idx-th Conditional element on this construct's LHS.
     * @param idx The zero-based index of the desired CE
     * @return the CE
     */
    LHSComponent getLHSComponent(int idx) {
        return m_CEs.getLHSComponent(idx);
    }

    /**
     * Consider this ConditionalElement to be READ ONLY!
     */

    public ConditionalElement getConditionalElements() {
        return (ConditionalElement) getLHSComponents();
    }

    LHSComponent getLHSComponents() {
        return m_CEs;
    }

    /**
     * Return a string (useful for debugging) describing all the Rete network
     * nodes connected to this construct.
     * @return A textual description of all the nodes used by this construct
     */

    public String listNodes() {
        StringBuffer sb = new StringBuffer(100);
        for (int i=0; i< m_nodes.size(); i++) {
            sb.append(m_nodes.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    HasLHS getNext() { return m_next; }
    void setNext(HasLHS next) { m_next = next; }

    void freeze(Rete engine) throws JessException {
        m_frozen = true;
    }

    public int getPatternCount() {
        return getLHSComponents().getPatternCount();
    }

    void insertCEAt(LHSComponent ce, int index, Rete engine)
        throws JessException {
        Group CEs = m_CEs;
        m_bindings = new Hashtable();
        m_CEs = new Group(Group.AND);
        if (CEs.getGroupSize() == 0) {
            addCE(ce, engine);
            return;
        }

        for (int i=0; i<CEs.getGroupSize(); i++) {
            if (i == index)
                addCE(ce, engine);
            addCE(CEs.getLHSComponent(i), engine);
        }
    }

    /**
     * Add a conditional element to this construct
     */

    void addCE(LHSComponent ce, Rete engine) throws JessException {

        ce = (LHSComponent) ce.clone();
        if (ce.getName().equals(Group.NOT))
            m_CEs.renameVariables((Group) ce, this);

        storeBoundName(ce);

        m_CEs.add(ce);

        //Look for variables, and create bindings for the ones defined
        //in this CE.
        findVariableDefinitions(ce);

        // Now look for variables that were never given a proper definition --
        // those that were only negated.
        findUndefinedVariables(ce);

        // Now handle '|' conjunctions by transforming tests into (or) Funcalls
        transformOrConjunctionsIntoOrFuncalls(ce, engine);

        addNOTToSupressUnneededBackwardChaining(ce, engine);

    }

    private void addNOTToSupressUnneededBackwardChaining(LHSComponent ce,
                                                         Rete engine)
        throws JessException {
        if (ce.isBackwardChainingTrigger()) {
            Pattern pattern = (Pattern) ce;
            Pattern notPattern =
                new Pattern(pattern,
                            pattern.getNameWithoutBackchainingPrefix());

            Group not = new Group(Group.NOT);
            not.add(notPattern);
            addCE(not, engine);
        }
    }

    private void storeBoundName(LHSComponent ce) throws JessException {
        String varname = ce.getBoundName();

        if (varname != null && m_bindings.get(varname) == null)
            addBinding(varname, ce, getGroupSize(), RU.PATTERN, -1, RU.FACT);
    }

    /**
     *  The argument could be a nested group -- a (NOT (AND)). In that
     *  case, the indexes used for pattern binding should start with
     * the normal CE index and increment from there, but without affecting
     * the "size" of this LHS.These indexes will be used off on a "branch"
     * of the Rete network.
     */

    private void findVariableDefinitions(LHSComponent ce)
        throws JessException {
        PatternIterator it = new PatternIterator(ce);
        for (int index=getPatternCount()-ce.getPatternCount(); it.hasNext(); ++index) {
            Pattern pattern = (Pattern) it.next();
            Deftemplate dt = pattern.getDeftemplate();
            for (int i=0; i< pattern.getNSlots(); i++) {
                for (int j=0; j< pattern.getNTests(i); j++) {
                    Test1 test = pattern.getTest(i, j);
                    Value val = test.m_slotValue;
                    boolean eq = (test.m_test == Test1.EQ);
                    if (val instanceof Variable) {
                        String name = val.variableValue(null);
                        if (m_bindings.get(name) == null) {
                            // Test for defglobal
                            if (name.startsWith("*"))
                                continue;

                            // If the test is positive, this is the
                            // definition of this variable, whether it's
                            // first or not, and whether it's defined in a
                            // "not" or not.

                            if (eq) {
                                int type = dt.getSlotDataType(i);
                                m_bindings.put(name,
                                               new BindingValue(name,
                                                                pattern,
                                                                index,
                                                                i,
                                                                test.m_subIdx,
                                                                type));
                            }

                        }
                    }
                }
            }
        }
    }

    private void findUndefinedVariables(LHSComponent ce)
        throws JessException {
        for (PatternIterator it = new PatternIterator(ce); it.hasNext();) {
            Pattern pattern = (Pattern) it.next();
            for (int i=0; i< pattern.getNSlots(); i++) {
                for (int j=0; j< pattern.getNTests(i); j++) {
                    Test1 test = pattern.getTest(i, j);
                    Value val = test.m_slotValue;
                    if (val instanceof Variable) {
                        String name = val.variableValue(null);
                        if (m_bindings.get(name) == null) {
                            // Ignore defglobals here
                            if (name.startsWith("*"))
                                continue;

                            throw new JessException("HasLHS.addPattern",
                                                    "First use of variable negated:",
                                                    name);
                        }
                    }
                }
            }
        }
    }

    private void transformOrConjunctionsIntoOrFuncalls(LHSComponent ce,
                                                       Rete engine)
        throws JessException {

        PatternIterator it = new PatternIterator(ce);
        for (int index=getPatternCount()-ce.getPatternCount(); it.hasNext(); ++index) {
            Pattern pattern = (Pattern) it.next();
            Deftemplate dt = pattern.getDeftemplate();

            for (int i=0; i< pattern.getNSlots(); i++) {
                int nTests = pattern.getNTests(i);

                if (nTests == 0)
                    continue;

                // Rearrange the tests
                ArrayList tests = new ArrayList();

                int currentSubIndex = pattern.getTest(i, 0).m_subIdx;
                int doneIdx = 0;

                // This is a loop over sub-indexes in the test array. doneIdx
                // will be incremented inside the loop, and some constructs
                // will break out to this label.
            subIdxLoop:
                while (doneIdx < nTests) {
                    // Find out if there are any ORs on this subslot
                    boolean hasOrs = false;
                    for (int j=doneIdx; j< nTests; j++)
                        {
                            Test1 aTest = pattern.getTest(i, j);
                            if (aTest.m_subIdx != currentSubIndex)
                                break;
                            else if (aTest.m_conjunction == RU.OR) {
                                hasOrs = true;
                                break;
                            }
                        }

                    // If no ORs on this subslot, just copy tests into ArrayList
                    if (!hasOrs) {
                        Test1 aTest;
                        for (int j=doneIdx; j< nTests; j++) {
                            aTest = pattern.getTest(i, j);
                            if (aTest.m_subIdx != currentSubIndex) {
                                currentSubIndex = aTest.m_subIdx;
                                continue subIdxLoop;
                            }
                            else {
                                tests.add(aTest);
                                ++doneIdx;
                            }
                        }
                        continue subIdxLoop;
                    }


                    // First find a variable to represent this (sub)slot; we
                    // may have to create one
                    Value var;
                    Test1 firstTest = pattern.getTest(i, doneIdx);
                    Value testValue = firstTest.m_slotValue;

                    if (isAVariableDefinition(testValue, pattern, i)) {
                        var = testValue;
                        ++doneIdx;
                    }
                    else {
                        String name = RU.gensym(Tokenizer.BLANK_PREFIX);
                        var = new Variable(name, RU.VARIABLE);
                        m_bindings.put(name,
                                       new BindingValue(name,
                                                        pattern,
                                                        index,
                                                        i, currentSubIndex,
                                                        dt.getSlotDataType(i)));
                    }

                    tests.add(new Test1(TestBase.EQ, currentSubIndex, var));

                    // We're going to build up this function call
                    Funcall or = new Funcall("or", engine);

                    // Count how many tests until an OR, so we can omit the
                    // AND if not needed
                    while (true) {
                        int andCount=1;
                        for (int j=doneIdx+1; j<nTests; j++) {
                            Test1 aTest = pattern.getTest(i, j);
                            if (aTest.m_conjunction == RU.OR ||
                                aTest.m_subIdx != currentSubIndex)
                                break;
                            else
                                ++andCount;
                        }

                        if (andCount == 1) {
                            or.add(testToFuncall(pattern.getTest(i, doneIdx),
                                                 var, engine));
                        }
                        else {
                            Funcall and = new Funcall("and", engine);
                            for (int j=doneIdx; j < doneIdx+andCount; j++)
                                and.add(testToFuncall(pattern.getTest(i, j),
                                                      var, engine));
                            or.add(new FuncallValue(and));
                        }

                        doneIdx += andCount;

                        if (doneIdx == nTests)
                            break;
                        else if (pattern.getTest(i, doneIdx).m_subIdx !=
                                 currentSubIndex)
                            break;
                    }
                    tests.add(new Test1(TestBase.EQ, currentSubIndex,
                                        new FuncallValue(or)));

                    if (doneIdx < nTests &&
                        pattern.getTest(i, doneIdx).m_subIdx != currentSubIndex)
                        currentSubIndex = pattern.getTest(i, doneIdx).m_subIdx;
                }

                Test1[] testArray = new Test1[tests.size()];
                for (int j=0; j<testArray.length; j++)
                    testArray[j] = (Test1) tests.get(j);
                pattern.replaceTests(i, testArray);
            }
        }
    }

    private boolean isAVariableDefinition(Value testValue,
                                          LHSComponent pattern,
                                          int slot) throws JessException {

        if (testValue.type() != RU.VARIABLE)
            return false;

        BindingValue bv =
            (BindingValue) m_bindings.get(testValue.variableValue(null));

        return (bv.getCE() == pattern &&
                bv.getSlotIndex() == slot);
    }


    /**
     *  Given a test, create an implied function call that does it
     */
    private Value testToFuncall(Test1 t, Value var, Rete engine)
        throws JessException {
        Value v = t.m_slotValue;
        switch (t.m_slotValue.type()) {
        case RU.FUNCALL: {
            if (t.m_test == TestBase.NEQ)
                return new FuncallValue(new Funcall("not", engine).arg(v));
            else
                return v;
        }
        default:
            return new FuncallValue(new Funcall(t.m_test==TestBase.EQ ?
                                                "eq" : "neq", engine).
                arg(v).arg(var));
        }
    }

    private void addBinding(String name, LHSComponent patt,
                            int factidx, int slotidx,
                            int subidx, int type)
        throws JessException {
        m_bindings.put(name,
                       new BindingValue(name, patt, factidx, slotidx, subidx, type));
    }

    /**
     * @return
     */
    Hashtable getBindings() { return m_bindings; }

    ArrayList getNodes() { return m_nodes; }

    public void addNode(Node n) throws JessException {
        if (n == null)
            new JessException("HasLHS.addNode",
                              "Compiler fault",
                              "null Node added");

        for (int i=0; i<m_nodes.size(); ++i)
            if (n == m_nodes.get(i))
                return;

        appendCompilationTrace(n);

        ++n.m_usecount;
        m_nodes.add(n);

    }

    /**
     * Completely remove this construct from the Rete network, including
     * removing any internal nodes that are only used by this construct.
     * @param root The roots of the Rete network where this construct lives.
     */

    void remove(Node root) {
        Iterator e = m_nodes.iterator();
        while (e.hasNext()) {
            Node s = (Node) e.next();
            if (--s.m_usecount <= 0) {
                root.removeSuccessor(s);
                Iterator e2 = m_nodes.iterator();
                while (e2.hasNext()) {
                    Node n = (Node) e2.next();
                    n.removeSuccessor(s);
                }
            }
        }
        m_nodes.clear();
    }

    private void appendCompilationTrace(Node n) {
        if (m_compilationTrace == null)
            m_compilationTrace = new StringBuffer(m_name + ": ");

        if (n.m_usecount == 0)
            m_compilationTrace.append("+");
        else
            m_compilationTrace.append("=");

        m_compilationTrace.append(n.getCompilationTraceToken());
    }

    String getCompilationTraceToken() { return "t"; }

    StringBuffer getCompilationTrace() { return m_compilationTrace; }

    /**
     * Set the node-index-hash of this construct. The node-index-hash
     * value effects the indexing efficiency of the join nodes for this
     * construct. Larger values will make constructs with many partial
     * matches faster (to a point). Must be set before construct is
     * added to engine (so is typically set during parsing via the
     * equivalent Jess command.
     @param h The node index hash value
    */

    public void setNodeIndexHash(int h) { m_nodeIndexHash = h; }

    /**
     * Get the node-index-hash setting of this construct.
     * @return The node-index-hash of this construct
     */
    public int getNodeIndexHash() { return m_nodeIndexHash; }

    /**
     * Fetch the name of this construct
     * @return The name of this construct
     */
    public final String getName() { return m_name; }

    /**
     * Fetch the display name of this construct
     * @return The display name of this construct
     */
    public String getDisplayName() {
        return m_displayName;
    }

    /**
     * Get the documentation string for this construct.
     * @return The docstring for this construct
     */
    public final String getDocstring() { return m_docstring; }

    // Compiler calls this after we've had initial update
    void setOld() { m_new = false;}

    public abstract Object accept(Visitor jv);

    public String getModule() {
        return m_module;
    }

    private int m_seqNum;
    int getSequenceNumber() {
        return ++m_seqNum;
    }

}



