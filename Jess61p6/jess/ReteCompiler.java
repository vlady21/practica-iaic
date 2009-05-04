package jess;

import java.io.Serializable;
import java.util.Hashtable;

/** **********************************************************************
 * Generates a pattern network
 *
 * See the paper
 * "Rete: A Fast Algorithm for the Many Pattern/ Many Object Pattern
 * Match Problem", Charles L.Forgy, Artificial Intelligence 19 (1982), 17-37.
 *
 * The implementation used here does not follow this paper; the present
 * implementation models the Rete net more literally as a set of networked Node
 * objects with interconnections.
 *
 * $Id: ReteCompiler.java,v 1.13.2.3 2003/11/12 15:32:21 ejfried Exp $
 * (C) 2001 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */


class ReteCompiler implements Serializable {

    private static Accelerator m_accelerator;
    private static boolean m_acceleratorChecked;
    private int m_hashkey = 101;

    public int getHashKey() {
        return m_hashkey;
    }

    public void setHashKey(int h) {
        m_hashkey = h;
    }

    /**
     * The roots of the pattern network
     */

    private RootNode m_root = new RootNode();

    public final Node getRoot() {
        return m_root;
    }

    public ReteCompiler() {
    }

    synchronized static Accelerator getAccelerator() {
        return m_accelerator;
    }

    static synchronized void loadAccelerator(Rete engine) throws JessException {
        // Try to load accelerator, if needed
        if (!m_acceleratorChecked) {
            m_acceleratorChecked = true;
            String classname;
            if ((classname = RU.getProperty("SPEEDUP")) != null) {
                try {
                    m_accelerator =
                            (Accelerator) engine.findClass(classname).newInstance();
                } catch (Exception e) {
                    throw new JessException("ReteCompiler.loadAccelerator",
                            "Can't load Accelerator class " +
                            classname,
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Add a rule's patterns to the network
     */
    public synchronized void addRule(HasLHS r, Rete engine)
            throws JessException {

        loadAccelerator(engine);

        // Tell the rule to fix up its binding table; it must be complete now.
        r.freeze(engine);

        // Fetch the rule's binding table; no more makeVarTable!
        Hashtable table = r.getBindings();

        LHSComponent CEs = r.getLHSComponents();
        Hashtable uniqueRoots = new Hashtable();

        // 'terminals' will be where we hold onto the final links in the
        // chain of nodes built during the first pass for each pattern.
        // We make this the maximum possible size.
        // EJFH Now not sure if this is large enough!
        Node[] terminals = new Node[CEs.getPatternCount()*3];
        int patternIndex = compileConditionalElements(CEs, r, uniqueRoots, table,
                terminals, 0, engine, false);

        // All that's left to do is to create the terminal node.
        // This is very easy.
        terminals[patternIndex - 1].mergeSuccessor(r, r);

        //Tell the engine to update this rule if the fact list isn't empty
        try {
            engine.updateNodes(uniqueRoots);
        } finally {
            r.setOld();
        }
    }

    private int compileConditionalElements(LHSComponent CEs,
                                           HasLHS sink,
                                           Hashtable uniqueRoots,
                                           Hashtable variables,
                                           Node[] terminals,
                                           int startIndex,
                                           Rete engine,
                                           boolean useNodeNot2Single)
            throws JessException {

        int patternIndex = startIndex;
        for (int ceIndex = 0; ceIndex < CEs.getGroupSize(); ++ceIndex) {

            LHSComponent ce = CEs.getLHSComponent(ceIndex);

            if (ce instanceof Pattern) {
                Pattern thisPattern = (Pattern) ce;
                buildPatternNetwork(sink, thisPattern, uniqueRoots, variables, terminals, patternIndex);

                if (patternIndex == 0) {
                    addRTLAdapter(patternIndex, terminals, sink);

                } else {
                    Node join = thirdPass(sink, thisPattern,
                            terminals[patternIndex - 1],
                            terminals[patternIndex],
                            variables, useNodeNot2Single, engine);

                    terminals[patternIndex - 1] = terminals[patternIndex] = join;

                    // Safe because logical is disallowed in defqueries, and
                    // (test) is disallowed in (logical.)
                    if (ce.getLogical())
                        ((Defrule) sink).setLogicalInformation((Node2) join);
                }

                ++patternIndex;

            } else if (ce.getName().equals(Group.NOT)) {

                boolean isAnIsolatedNot = noNotsAboveOrBelow(CEs, ce);

                Node lastTerminal = terminals[patternIndex - 1];
                int lastTerminalIndex = patternIndex - 1;

                // Find the negated thing -- i.e., the CE nested inside
                // all the "NOT"s; also, count the number of nested nots.
                int depth = 1;
                LHSComponent inner = ce.getLHSComponent(0);
                while (inner.getName().equals(Group.NOT)) {
                    ++depth;
                    inner = inner.getLHSComponent(0);
                }

                // If isAnIsolatedNot, then this will add a NodeNot2Single and
                // everything's all set. Otherwise, it'll add a Node2, and we'll
                // need to add some NodeNot2 nodes below.
                int tmpPatternIndex =
                        compileConditionalElements(inner, sink,
                                uniqueRoots, variables, terminals,
                                patternIndex, engine, isAnIsolatedNot);

                Node join = terminals[tmpPatternIndex - 1];

                int hash = hashValueFor(sink);
                if (!isAnIsolatedNot) {
                    for (int level = 0; level < depth; ++level) {
                        NodeJoin n2 = (NodeJoin) join;
                        Node nltr = new Node1LTR();
                        NodeJoin not = new NodeNot2(hash, patternIndex);

                        // the inner group goes to the adapter
                        n2.addSuccessor(nltr, sink);
                        // the adapter feeds into the prefixnot2
                        nltr.addSuccessor(not, sink);
                        // the inner not's left input also goes into outer
                        lastTerminal.addSuccessor(not, sink);
                        terminals[patternIndex - 1] = terminals[patternIndex] = not;
                        join = not;
                    }
                }

                patternIndex = lastTerminalIndex + 1;
                terminals[patternIndex - 1] = terminals[patternIndex] = join;

                // Safe because logical is disallowed in defqueries, and
                // (test) is disallowed in (logical.)
                if (ce.getLogical())
                    ((Defrule) sink).setLogicalInformation((Node2) join);

                ++patternIndex;

            } else { // Other groups like AND
                patternIndex =
                        compileConditionalElements(ce, sink, uniqueRoots,
                                variables, terminals,
                                patternIndex, engine, false);

            }

        }

        return patternIndex;
    }

    private void buildPatternNetwork(HasLHS sink, Pattern thisPattern, Hashtable uniqueRoots,
                                     Hashtable variables, Node[] terminals, int patternIndex) throws JessException {
        terminals[patternIndex] =
                firstPass(sink, thisPattern, uniqueRoots, variables);

        terminals[patternIndex] =
                secondPass(sink, thisPattern, terminals[patternIndex]);
    }

    private boolean noNotsAboveOrBelow(LHSComponent CEs, LHSComponent ce) {
        boolean simpleNodeNot2 =
                (!CEs.getName().equals(Group.NOT)) &&
                (!ce.getLHSComponent(0).getName().equals(Group.TEST)) &&
                (ce.getLHSComponent(0) instanceof Pattern);
        return simpleNodeNot2;
    }

    private void addRTLAdapter(int patternIndex, Node[] terminals, HasLHS r)
            throws JessException {

        // Attach a NodeRTL to the very first tail, so
        // this one will do a LEFT calltype instead of a
        // RIGHT one.

        Node1RTL rtl = new Node1RTL();
        rtl = (Node1RTL) (terminals[patternIndex] =
                terminals[patternIndex].mergeSuccessor(rtl, r));
        if (r.getLHSComponent(patternIndex).getLogical())
            ((Defrule) r).setLogicalInformation(rtl);

    }

    private int hashValueFor(HasLHS r) {
        int hash = r.getNodeIndexHash();
        if (hash == 0)
            hash = m_hashkey;
        return hash;
    }

    /**
     * Evaluate a funcall, replacing all variable references with
     * BindingValues.  Bind each variable to the first occurrence in
     * rule.
     */
    private Value eval(Hashtable table, Value v)
            throws JessException {
        if (v.type() != RU.FUNCALL)
            return v;

        Funcall vv = (Funcall) v.funcallValue(null).clone();
        for (int i = 0; i < vv.size(); i++) {
            Value val = vv.get(i);
            if (val instanceof Variable) {
                String name = val.variableValue(null);
                BindingValue b = (BindingValue) table.get(name);
                if (b != null) {
                    vv.set(b, i);
                } else {
                    // Defglobal
                    if (name.charAt(0) != '*') {
                        compilerError("eval",
                                "Unbound variable found in funcall: " +
                                name);
                    }
                }
            } else if (val.type() == RU.FUNCALL) {
                // nested funcalls
                vv.set(eval(table, val), i);
            }
        }

        return new FuncallValue(vv);
    }

    /**
     * Call this on a funcall value AFTER eval has modified it.
     * it returns true iff this funcall contains binding to patterns other
     * than the one named by currentFactNumber
     */
    private boolean checkForMultiPattern(Value v, Pattern current)
            throws JessException {
        ValueVector vv = v.funcallValue(null);

        for (int i = 1; i < vv.size(); i++) {
            Value arg = vv.get(i);
            if (arg.type() == RU.BINDING &&
                    ((BindingValue) arg).getCE() != current)
                return true;

            else if (arg.type() == RU.FUNCALL &&
                    checkForMultiPattern(arg, current))
                return true;
        }
        return false;

    }

    private Hashtable m_doneVars = new Hashtable();

    /** *********
     FIRST PASS
     *********

     In the first pass, we just create some of the one-input
     nodes for each pattern

     These one-input nodes compare a certain slot in a fact (token)
     with a fixed, typed, piece of data. Each pattern gets one
     special one-input node, the TECT node, which checks the class
     type and class name.
     */

    private Node firstPass(NodeSink sink, Pattern p, Hashtable uniqueRoots,
                           Hashtable table)
            throws JessException {

        // Get the deftemplate
        Deftemplate deft = p.getDeftemplate();

        // If this is a 'test' CE, we have to treat it slightly differently
        boolean isTest = (p.getName().equals("test"));

        ////////////////////////////////////////////////////////////
        // Every pattern must have a definite class name
        // Therefore, the first node in a chain always
        // checks the class name of a token (except for test CEs)
        ////////////////////////////////////////////////////////////

        Node last;

        last = m_root.mergeSuccessor(isTest ?
                (Node1) new Node1NONE() :
                (Node1) new Node1TECT(p.getName()), sink);

        uniqueRoots.put(last, last);

        // First we have to find all the multifields, because these
        // change the 'shape' of the facts as they go through the
        // net.

        for (int slot = 0; !isTest && slot < p.getNSlots(); slot++) {

            // any tests on this slot?
            if (deft.getSlotType(slot) != RU.MULTISLOT)
                continue;

            // No multifields; just test length
            if (p.getNMultifieldsInSlot(slot) == 0) {
                int length = p.getSlotLength(slot);
                if (length != -1)
                    last =
                            last.mergeSuccessor(new Node1MTELN(slot, length),
                                    sink);
            }

            // Multifields that needs separating out?
            else
                last =
                        last.mergeSuccessor(new
                                Node1MTMF(slot,
                                        p.getMultifieldFlags(slot)), sink);
        }


        ////////////////////////////////////////////////////////////
        // Simplest basic tests are done here
        ////////////////////////////////////////////////////////////

        for (int j = 0; !isTest && j < p.getNSlots(); j++) {

            // any tests on this slot?
            if (p.getNTests(j) == 0)
                continue;

            int testIdx = j;

            for (int k = 0; k < p.getNTests(j); k++) {
                Test1 test = p.getTest(j, k);
                Value slotValue = test.m_slotValue;


                if (slotValue instanceof Variable) {
                    // Single tests against only global variables
                    String name = slotValue.variableValue(null);
                    if (!(name.startsWith("*") && name.endsWith("*")))
                        continue;
                }

                // expand variable references in funcalls to index,
                // slot, subslot triples
                Value v = eval(table, slotValue);

                if (slotValue.type() != RU.FUNCALL ||
                        !checkForMultiPattern(v, p))
                    last = addSimpleTest(last, sink, testIdx, test, v);
            }
        }
        return last;
    }


    /* *********
       SECOND PASS
       *********

       In this pass, we are looking for variables which must be
       instantiated the same way twice in one fact. IE, the pattern
       look like (foo ?X foo ?X), and we're looking for facts like
       (foo bar foo bar).  NOT versions are handled as well.
    */

    private Node secondPass(NodeSink r, Pattern p, Node last)
            throws JessException {

        // If this is a 'test' CE, we have to treat it slightly differently
        if (p.getName().equals("test"))
            return last;

        // workspace to track variables that have been done.
        m_doneVars.clear();

        // find a variable slot, if there is one. If one is found,
        // look at the rest of
        // the fact for another one with the same name.
        // If one is found, create the
        // appropriate node and put it in place.

        // NOTs make things a bit more complex.
        // There are a few cases for a varname
        // appearing twice in a pattern:

        // ?X ?X        ->        generate a TEV1 node.
        // ?X ~?X       ->        generate a TNEV1 node.
        // ~?X ?X       ->        generate a TNEV1 node.
        // ~?X ~?X      ->        (DO NOTHING!)


        // look for a slot in the pattern containing a variable

        for (int j = 0; j < p.getNSlots(); j++) {
            // any tests for this slot?
            if (p.getNTests(j) == 0)
                continue;

            for (int k = 0; k < p.getNTests(j); k++) {
                Test1 test_jk = p.getTest(j, k);
                if (!(test_jk.m_slotValue instanceof Variable))
                    continue;

                // see if we've done this one before.
                String varName = test_jk.m_slotValue.variableValue(null);
                if (m_doneVars.get(varName) != null)
                    continue;

                // no, we haven't. Find each other occurrence.
                // We start the search at the same slot since it might be a
                // multislot!
                for (int n = j; n < p.getNSlots(); n++) {
                    if (p.getNTests(n) == 0)
                        continue;
                    for (int o = 0; o < p.getNTests(n); o++) {
                        Test1 test_no = p.getTest(n, o);

                        // This can happen since we're researching
                        // the same slot.
                        if (test_no == test_jk)
                            continue;
                        if (test_no.m_slotValue instanceof Variable &&
                                test_no.m_slotValue.equals(test_jk.m_slotValue)) {
                            // we've identified another slot with
                            // the same variable.
                            last = addMultipleReferenceTest(last,
                                    j, test_jk,
                                    n, test_no,
                                    r);
                        }
                    }
                }
                m_doneVars.put(varName, varName);
            }
        }

        return last;
    }


    /* *********
       THIRD PASS
       *********

       Now we start making some two-input nodes. These nodes check
       that a variable with the same name in two patterns is
       instantiated the same way in each of two facts; or not, in
       the case of negated variables. An important thing to
       remember: the first instance of a variable can never be
       negated. We'll check that here and throw an exception if it's
       violated.  We can compare every other instance of the
       variable in this rule against this first one - this
       simplifies things a lot! We'll use simplified logic which
       will lead to a few redundant, but correct tests.

       Two-input nodes can contain many tests, so they are rather
       more complex than the one-input nodes. To share them, what
       we'll do is build a new node, then compare this new one to
       all possible shared ones.  If we can share, we just throw the
       new one out. The inefficiency is gained back in spades at
       runtime, both in memory and speed. Note that NodeNot2 type
       nodes cannot be shared.

       The number of two-input nodes that we create is *determinate*:
       it is always one less than the number of patterns. For
       example, w/ 4 patterns, numbered 0,1,2,3, and the following
       varTable: (Assuming RU.SLOT_SIZE = 2)

       <PRE>

       X  Y  N
       0  1  2
       2  4  4

       </PRE>
       generated from the following rule LHS:

       <PRE>
       (foo ?X ?X)
       (bar ?X ?Y)
       (Goal (Type Simplify) (Object ?N))
       (Expression (Name ?N) (Arg1 0) (Op +) (Arg2 ~?X))
       </PRE>

       Would result in the following nodes being generated
       (Assuming SLOT_DATA == 0, SLOT_TYPE == 1, SLOT_SIZE == 2):

       <PRE>
       0     1
       \   /
       ___L___R____
       |          |            2
       | 0,2 = 2? |           /
       |          |          /
       ------------ \0,1    /
                   _L______R__                3
                   |          |               /
                   | NO TEST  |              /
                   |          |             /
                   ------------ \0,1,2     /
                                 L_______R__
                                 | 0,2 != 8?|
                                 | 2,4 = 2? |
                                 |          |
                                 ------------
                                   |0,1,2,3
                                   |
                              (ACTIVATE)

       <PRE>

       Where the notation 2,4 = 8? means that this node tests tbat
       index 4 of fact 2 in the left token is equal to index 8 in the
       right token's single fact. L and R indicate Left and Right
       inputs.
    */

    private Node thirdPass(HasLHS r, Pattern p, Node left, Node right,
                           Hashtable table, boolean useNodeNot2Single, Rete engine)
            throws JessException {

        int hash = r.getNodeIndexHash();
        if (hash == 0)
            hash = m_hashkey;

        // Keep track of which variables we've tested already for
        // this pattern.
        m_doneVars.clear();

        // If this is a 'test' CE, we have to treat it slightly differently
        boolean isTest = (p.getName().equals("test"));

        // construct an appropriate 2 input node...
        NodeJoin n2;

        if (isTest)
            n2 = new NodeJoin();
        else if (useNodeNot2Single)
            n2 = new NodeNot2Single(hash);
        else
            n2 = new Node2(hash);

        // Figure out if we need a bound name test here.
        if (p.getBoundName() != null) {
            String name = p.getBoundName();
            BindingValue b = (BindingValue) table.get(name);
            if (b.getCE() != p) {
                // This variable is mentioned before it's used as a
                // bound name.  We add a test that compares slot -1 on
                // this fact (the fact ID) to the binding value.

                n2.addTest(Test1.EQ,
                        b.getFactNumber(),
                        b.getSlotIndex(),
                        b.getSubIndex(),
                        -1,
                        -1);
            }
        }

        // now tell the node what tests to perform

        // for each field in this pattern
        for (int j = 0; j < p.getNSlots(); j++) {

            // any tests for this slot?
            if (p.getNTests(j) == 0)
                continue;

            // for every test on this slot..
            for (int k = 0; k < p.getNTests(j); k++) {
                Test1 test_jk = p.getTest(j, k);
                // if this test is against a variable...
                if (test_jk.m_slotValue instanceof Variable) {
                    // find this variable in the table
                    String name =
                            p.getTest(j, k).m_slotValue.variableValue(null);

                    // If we've already handled this variable in this CE, continue
                    if (m_doneVars.get(name) != null)
                        continue;

                    // If this is a global variable, continue
                    else if (name.startsWith("*"))
                        continue;

                    // Get the rule's entry for where this variable is defined
                    BindingValue b = (BindingValue) table.get(name);
                    if (b == null)
                        compilerError("addRule",
                                "Corrupted VarTable: var " +
                                name + " not in table");

                    // if this variable is defined in this CE, continue
                    else if (b.getCE() == p)
                        continue;

                    // This is the first time this variable appears in this CE, but
                    // it appears in previous CEs in the same rule. Insert a test to
                    // make sure the multiple occurrences match.
                    n2.addTest(test_jk.m_test,
                            b.getFactNumber(),
                            b.getSlotIndex(),
                            b.getSubIndex(),
                            j,
                            test_jk.m_subIdx);

                    m_doneVars.put(name, name);

                } else if (test_jk.m_slotValue.type() == RU.FUNCALL) {

                    if (p.getDeftemplate().getBackwardChaining())
                        throw new JessException("ReteCompiler.addRule",
                                "Can't use funcalls in backchained patterns",
                                p.getName());

                    // expand the variable references to index, slot
                    // pairs. We do this again even though we did it
                    // in pass one. We don't want to destroy the
                    // patterns themselves. Tell eval to bind
                    // variables to first occurrence in Rule.
                    Value v = eval(table, test_jk.m_slotValue);

                    // if other facts besides this one are
                    // mentioned, generate a test

                    if (isTest || checkForMultiPattern(v, p)) {

                        if (test_jk.m_test == TestBase.EQ)
                            n2.addTest(TestBase.EQ, test_jk.m_subIdx, v, engine);
                        else
                            n2.addTest(TestBase.NEQ, test_jk.m_subIdx, v, engine);
                    }
                }
            }
        }

        // search through the successors of this pattern and the
        // next one.  Do they have any in common, and if so, are
        // they equivalent to the one we just built? If so, we don't
        // need to add the new one!  Don't share NodeNot2's.

        n2 = addJoinNode(n2, left, right, r);
        n2.complete();

        if (p.getDeftemplate().getBackwardChaining() &&
                !p.getExplicit() &&
                !p.getNegated()) {

            // n2 can't be a NodeJoin, since do-backwards-chaining
            // would have rejected the call. Explicit CEs are those
            // for which we should never do backwards chaining

            ((Node2) n2).setBackchainInfo(p, r);
        }

        return n2;
    }

    private NodeJoin addJoinNode(NodeJoin n2, Node left, Node right,
                                 NodeSink r)
            throws JessException {

        // Find any node equal to this one, or return this one.
        NodeJoin new1 = (NodeJoin) left.resolve(n2);

        // If we found a preexisting one...
        if (new1 != n2) {
            NodeJoin new2 = (NodeJoin) right.resolve(n2);

            // If the two nodes are the same node, these two tails are
            // already connected.
            if (new1 == new2) {
                r.addNode(new1);
                return new1;
            }
        }

        // Here we use add, not merge. We only want to share if the
        // two nodes were already connected.
        left.addSuccessor(n2, r);
        right.addSuccessor(n2, r);
        return n2;
    }

    private Node addSimpleTest(Node last, NodeSink r,
                               int testIdx, Test1 test, Value v)
            throws JessException {
        Node1 node;
        switch (test.m_test) {
            case Test1.EQ:
                switch (test.m_subIdx) {
                    case -1:
                        node = new Node1TEQ(testIdx, v);
                        break;
                    default:
                        node = new Node1MTEQ(testIdx, test.m_subIdx, v);
                        break;
                }
                break;
            default:
                switch (test.m_subIdx) {
                    case -1:
                        node = new Node1TNEQ(testIdx, v);
                        break;
                    default:
                        node = new Node1MTNEQ(testIdx, test.m_subIdx, v);
                        break;
                }
                break;
        }

        return last.mergeSuccessor(node, r);
    }

    /**
     * Add a test for agreement among multiple references to the same
     * variable inside one pattern.
     */

    private Node addMultipleReferenceTest(Node tail,
                                          int slot1, Test1 test_jk,
                                          int slot2, Test1 test_no,
                                          NodeSink r)
            throws JessException {
        if (test_jk.m_test == TestBase.EQ) {
            if (test_no.m_test == TestBase.EQ)
                return tail.
                        mergeSuccessor(new Node1TEV1(slot1,
                                test_jk.m_subIdx,
                                slot2,
                                test_no.m_subIdx),
                                r);
            else
                return tail.
                        mergeSuccessor(new Node1TNEV1(slot1,
                                test_jk.m_subIdx,
                                slot2,
                                test_no.m_subIdx),
                                r);

        } else {
            if (test_no.m_test == TestBase.EQ)
                return tail.
                        mergeSuccessor(new Node1TNEV1(slot1,
                                test_jk.m_subIdx,
                                slot2,
                                test_no.m_subIdx),
                                r);
            else
                return tail;
        }

    }

    private void compilerError(String routine, String message)
            throws RuleCompilerException {
        throw new RuleCompilerException("ReteCompiler." + routine, message);
    }

}


