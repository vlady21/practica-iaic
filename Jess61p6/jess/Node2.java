package jess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A non-negated, two-input node of the Rete network.
 * Each tests in this node tests that a slot from a fact from the left input
 * and one from the right input have the same value and type.
 *
 * $Id: Node2.java,v 1.11.2.4 2003/10/14 03:24:15 ejfried Exp $
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

class Node2 extends NodeJoin implements LogicalNode, Serializable {

    /**
     * The left and right token memories
     * They are binary trees of Tokens
     */

    TokenTree m_left;
    TokenTree m_right;

    /**
     * Fact index used for tree indexing in right memory
     */

    private int m_rightIdx = -1;
    private int m_rightSubIdx = -1;
    private int m_leftIdx = -1;
    private int m_leftSubIdx = -1;
    private int m_tokenIdx = 0;
    private int m_tokenSize;

    /**
     * The key to use when creating token trees
     */

    protected int m_hashkey;

    /**
     * Only non-null when I should backwards-chain
     */
    Pattern m_pattern;

    /**
     * Only non-null when I should backwards-chain
     */

    HasLHS m_defrule;

    /**
     * Used to count pattern-matches; if zero, may backwards-chain.
     */

    public int m_matches = 0;


    /**
     * True if we can do short-cut testing
     */
    private boolean m_blessed = false;

    /**
     * Tokens that give logical support to facts
     */

    protected HashMap m_logicalDepends;

    /**
     * Constructor
     * @param hashkey Hashkey to use for creating TokenTrees
     */

    Node2(int hashkey) {
        super();
        m_hashkey = hashkey;
    }

    /**
     * Add a test to this node.
     * @param test EQ or NEQ
     * @param token_idx Which fact in the token
     * @param left_idx Which slot in the left fact
     * @param leftSub_idx Which subslot in the left slot
     * @param right_idx Which slot in the right fact
     * @param rightSub_idx Which subslot in the right slot
     */
    void addTest(int test, int token_idx, int left_idx, int leftSub_idx,
                 int right_idx, int rightSub_idx) {

        if (leftSub_idx == -1 && rightSub_idx == -1)
            addTest(new Test2Simple(test, token_idx, left_idx, right_idx));
        else
            addTest(new Test2Multi(test, token_idx, left_idx,
                    leftSub_idx, right_idx,
                    rightSub_idx));
    }


    /**
     * Do the business of this node.
     * The 2-input nodes, on receiving a token, have to do several things,
     * and their actions change based on whether it's an ADD or REMOVE,
     * and whether it's the right or left input!
     * <PRE>
     *
     * For ADDs, left input:
     * 1) Look for this token in the left memory. If it's there, do nothing;
     * If it's not, add it to the left memory.
     *
     * 2) Perform all this node's tests on this token and each of the right-
     * memory tokens. For any right token for which they succeed:
     *
     * 3) a) append the right token to a copy of this token. b) do a
     * CallNode on each of the successors using this new token.
     *
     * For ADDs, right input:
     *
     * 1) Look for this token in the right memory. If it's there, do nothing;
     * If it's not, add it to the right memory.
     *
     * 2) Perform all this node's tests on this token and each of the left-
     * memory tokens. For any left token for which they succeed:
     *
     * 3) a) append this  token to a copy of the left token. b) do a
     * CallNode on each of the successors using this new token.
     *
     * For REMOVEs, left input:
     *
     * 1) Look for this token in the left memory. If it's there, remove it;
     * else do nothing.
     *
     * 2) Perform all this node's tests on this token and each of the right-
     * memory tokens. For any right token for which they succeed:
     *
     * 3) a) append the right token to a copy of this token. b) do a
     * CallNode on each of the successors using this new token.
     *
     * For REMOVEs, right input:
     *
     * 1) Look for this token in the right memory. If it's there, remove it;
     * else do nothing.
     *
     * 2) Perform all this node's tests on this token and each of the left-
     * memory tokens. For any left token for which they succeed:
     *
     * 3) a) append this token to a copy of the left token. b) do a
     * CallNode on each of the successors using this new token.
     *
     * </PRE>
     */

    void callNodeLeft(Token token, Context context) throws JessException {
        try {
            m_matches = 0;

            switch (token.m_tag) {

                case RU.ADD:
                    // Temporary patch to catch uncovered case
                    try {
                        m_tokenSize = token.size();
                        m_left.add(token, false);
                    } catch (NullPointerException npe) {
                        throw new JessException("Node2.callNode",
                                "Negated conjunction with",
                                "unbound variables");
                    }
                    runTestsVaryRight(token, m_right, context);
                    askForBackChain(token, context);

                    break;
                case RU.UPDATE:

                    // Temporary patch to catch uncovered case
                    try {
                        m_tokenSize = token.size();
                        Token storedToken =  Rete.getFactory().newToken(token);
                        storedToken.m_tag = RU.ADD;
                        if (m_left.add(storedToken, true)) {
                            runTestsVaryRight(storedToken, m_right, context);
                            askForBackChain(token, context);
                        }
                    } catch (NullPointerException npe) {
                        throw new JessException("Node2.callNode",
                                "Negated conjunction with",
                                "unbound variables");
                    }

                    break;

                case RU.REMOVE:
                    if (m_left.remove(token)) {
                        runTestsVaryRight(token, m_right, context);
                    }
                    break;

                case RU.CLEAR:
                    // This is a special case. If we get a 'clear', we flush
                    // our memories, then notify all our successors and
                    // return.
                    initTokenTrees();
                    if (m_logicalDepends != null)
                        m_logicalDepends.clear();

                    passAlong(token, context);
                    break;

                default:
                    throw new JessException("Node2.callNode",
                            "Bad tag in token",
                            token.m_tag);
            } // switch token.tag

            broadcastEvent(JessEvent.RETE_TOKEN + Node.LEFT, token);

            return;

        } catch (JessException je) {
            je.addContext("rule LHS (Node2)");
            throw je;
        }
    }

    protected void removeLogicalSupportFrom(Token token, Context context) {
        ArrayList list = (ArrayList) m_logicalDepends.remove(token);
        if (list != null) {
            Rete engine = context.getEngine();
            for (int i = 0; i < list.size(); ++i) {
                Fact f = (Fact) list.get(i);
                engine.removeLogicalSupportFrom(token, f);
            }
        }
    }

    void callNodeRight(Token t, Context context) throws JessException {
        try {
            int tag = t.m_tag;
            switch (tag) {

                case RU.UPDATE:
                case RU.ADD:
                    m_right.add(t, tag == RU.UPDATE);
                    runTestsVaryLeft(t, m_left, context);
                    break;

                case RU.REMOVE:
                    if (m_right.remove(t)) {
                        runTestsVaryLeft(t, m_left, context);
                    }
                    break;

                case RU.CLEAR:
                    break;

                default:
                    throw new JessException("Node2.callNode",
                            "Bad tag in token",
                            tag);
            } // switch tag

            broadcastEvent(JessEvent.RETE_TOKEN + Node.RIGHT, t);

            return;

        } catch (JessException je) {
            je.addContext("rule LHS (Node2)");
            throw je;
        }
    }

    /**
     * Node2.callNode can call this to produce debug info.
     * @param token
     * @param callType
     */

    void debugPrint(Token token, int callType) {
        System.out.println("TEST " + toString() + "(" + hashCode() +
                ");calltype=" + callType +
                ";tag=" + token.m_tag + ";class=" +
                token.fact(0).getName());
    }

    /**
     * Run all the tests on a given (left) token and every token in the
     * right memory. For the true ones, assemble a composite token and
     * pass it along to the successors.
     *
     */

    void runTestsVaryRight(Token lt, TokenTree th, Context context) throws JessException {
        if (m_blessed) {
            Value v;
            if (m_leftSubIdx == -1)
                v = lt.fact(m_tokenIdx).get(m_leftIdx);
            else
                v = lt.fact(m_tokenIdx).get(m_leftIdx).
                        listValue(null).get(m_leftSubIdx);

            TokenVector fv;
            if ((fv = th.findCodeInTree(v.hashCode(), false)) == null)
                return;

            else {
                doRunTestsVaryRight(lt, fv, context);
                return;
            }
        } else
            doRunTestsVaryRight(lt, th, context);
    }


    void doRunTestsVaryRight(Token lt, TokenTree th, Context context) throws JessException {
        if (th == null)
            return;

        int hash = th.m_hash;
        TokenVector[] facts = th.m_tokens;

        for (int j = 0; j < hash; j++)
            if (doRunTestsVaryRight(lt, facts[j], context))
                return;

        return;
    }


    boolean doRunTestsVaryRight(Token lt, TokenVector v, Context context)
            throws JessException {

        if (v != null) {
            int size = v.size();
            int ntests = m_nTests;

            int tag = lt.m_tag;
            for (int i = 0; i < size; i++) {
                // Must be inside loop due to passAlong() call
                context.setToken(lt);
                Token rt = v.elementAt(i);
                boolean result = true;
                if (result = runTests(ntests, context, rt)) {
                    if (tag != RU.REMOVE)
                        ++m_matches;

                    if (ntests != 0)
                        lt = lt.prepare(result);
                    Token newToken = Rete.getFactory().newToken(lt, rt);
                    passAlong(newToken, context);
                    if (m_logicalDepends != null && tag != RU.UPDATE)
                        removeLogicalSupportFrom(newToken, context);

                }
            }
        }
        return false;
    }

    void runTestsVaryLeft(Token token, TokenTree th, Context context) throws JessException {

        if (th == null)
            return;

        if (m_blessed) {

            Fact fact = token.topFact();
            Value v;
            if (m_rightSubIdx == -1)
                v = fact.get(m_rightIdx);
            else
                v = fact.get(m_rightIdx).listValue(null).get(m_rightSubIdx);

            TokenVector fv;
            if ((fv = th.findCodeInTree(v.hashCode(), false)) == null) {
                return;
            } else {
                doRunTestsVaryLeft(token, fv, context);
                return;
            }

        } else {
            doRunTestsVaryLeft(token, th, context);
            return;
        }
    }

    void doRunTestsVaryLeft(Token rt, TokenTree th, Context context) throws JessException {
        if (th == null)
            return;

        for (int j = 0; j < th.m_hash; j++) {
            doRunTestsVaryLeft(rt, th.m_tokens[j], context);
        }

    }

    void doRunTestsVaryLeft(Token rt, TokenVector v, Context context)
            throws JessException {

        if (v != null) {
            int size = v.size();
            if (size > 0) {
                int ntests = m_nTests;

                for (int i = 0; i < size; i++) {
                    Token lt = v.elementAt(i);
                    context.setToken(lt);

                    boolean result = true;
                    if (result = runTests(ntests, context, rt)) {
                        // the new token has the *left* token's tag at birth...

                        if (ntests != 0)
                            lt = lt.prepare(result);

                        Token newToken = Rete.getFactory().newToken(lt, rt);
                        newToken.m_tag = rt.m_tag;
                        passAlong(newToken, context);
                        if (m_logicalDepends != null && newToken.m_tag != RU.UPDATE)
                            removeLogicalSupportFrom(newToken, context);
                    }
                }
            }
        }
    }

    boolean runTests(int ntests, Context context, Token rt)
            throws JessException {

        TestBase[] theTests = m_tests;
        context.setFact(rt.topFact());
        for (int i = 0; i < ntests; i++) {
            if (!theTests[i].doTest(context))
                return false;
        }

        return true;
    }

    /**
     * Beginning of backward chaining. This is very slow; we need to do more
     * of the work at compile time.
     */
    void setBackchainInfo(Pattern p, HasLHS d) {
        m_pattern = p;
        m_defrule = d;
    }

    private void askForBackChain(Token token, Context context) throws JessException {
        // In theory, we could allow m_matches != 0 and use this to
        // retract need-x facts.  I can't quite figure out how to do this,
        // though.

        if (m_pattern == null ||
                m_matches != 0)
            return;

        Fact f = new Fact(m_pattern.getBackchainingTemplateName(),
                context.getEngine());

        // We just want the stubby beginnings of this fact.
        Fact vv = f;

        // For each slot in the pattern...
        for (int i = 0; i < m_pattern.getNSlots(); i++) {
            int type = m_pattern.getDeftemplate().getSlotType(i);

            // This is the slot value, which we're looking to set to
            // something useful...
            Value val = Funcall.NIL;

            ValueVector slot = null;
            if (type == RU.MULTISLOT)
                slot = new ValueVector();

            // Look at every test

            for (int j = 0; j < m_pattern.getNTests(i); j++) {
                Test1 test = m_pattern.getTest(i, j);

                // only consider EQ tests, not NEQ
                if (test.m_test != TestBase.EQ)
                    continue;


                // If this is a variable, and we can pull a value out
                // of the token, we're golden; but if this is the
                // first occurrence, forget it.
                else if (test.m_slotValue instanceof Variable) {
                    BindingValue b = (BindingValue) m_defrule.getBindings().
                            get(test.m_slotValue.variableValue(null));

                    // Handle defglobals here
                    if (b == null)
                        val = test.m_slotValue;

                    else if (b.getFactNumber() < token.size()) {
                        val = token.fact(b.getFactNumber()).get(b.getSlotIndex());

                        if (b.getSubIndex() != -1)
                            val = val.listValue(null).get(b.getSubIndex());
                    }

                    if (type == RU.SLOT)
                        break;
                }

                // Otherwise, it's a plain value, and this is what we want!
                else {
                    val = test.m_slotValue;

                    if (type == RU.SLOT)
                        break;
                }

                // Add something to this multislot.
                if (type == RU.MULTISLOT) {
                    if (slot.size() < (test.m_subIdx + 1))
                        slot.setLength(test.m_subIdx + 1);
                    slot.set(val, test.m_subIdx);
                    val = Funcall.NIL;
                }
            }

            if (type == RU.MULTISLOT) {
                for (int ii = 0; ii < slot.size(); ii++)
                    if (slot.get(ii) == null)
                        slot.set(Funcall.NIL, ii);

                val = new Value(slot, RU.LIST);
            }

            vv.set(val, i);
            val = Funcall.NIL;
        }

        // The engine will assert or retract this after the current LHS cycle.
        context.getEngine().setPendingFact(vv, m_matches == 0);
    }


    /**
     * Describe myself
     * @return A string showing all the tests, etc, in this node.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(256);
        sb.append("[Node2 ntests=");
        sb.append(m_nTests);
        sb.append(" ");
        for (int i = 0; i < m_nTests; i++) {
            sb.append(m_tests[i].toString());
            sb.append(" ");
        }
        sb.append(";usecount = ");
        sb.append(m_usecount);
        if (m_blessed)
            sb.append(";blessed");
        sb.append("]");
        return sb.toString();
    }


    /**
     * @param stream
     * @exception IOException
     * @exception ClassNotFoundException
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    // Called from the Constructor and from readObject
    protected void initTokenTrees() {
        boolean useSortCode = (m_leftIdx == -1);
        int tokenIndex = useSortCode ? 0 : m_tokenIdx;
        if (m_left == null)
            m_left = new TokenTree(m_hashkey, useSortCode, tokenIndex,
                    m_leftIdx, m_leftSubIdx);
        else
            m_left.clear();

        if (m_right == null)
            m_right = new TokenTree(m_hashkey, m_rightIdx == -1, 0,
                    m_rightIdx, m_rightSubIdx);
        else
            m_right.clear();
    }

    /*
     * Textural description of memory contents
     */

    StringBuffer displayMemory() {
        StringBuffer sb = new StringBuffer("*** Left Memory:\n");
        for (int i = 0; i < m_left.m_hash; i++) {
            TokenVector tv = m_left.m_tokens[i];
            if (tv == null)
                continue;
            for (int j = 0; j < tv.size(); j++) {
                sb.append(tv.elementAt(j));
                sb.append("\n");
            }
        }
        sb.append("*** RightMemory:\n");
        for (int i = 0; i < m_right.m_hash; i++) {
            TokenVector fv = m_right.m_tokens[i];
            if (fv == null)
                continue;
            for (int j = 0; j < fv.size(); j++) {
                sb.append(fv.elementAt(j));
                sb.append("\n");
            }
        }
        return sb;
    }



    /*
     * Move the tests into an array
     * possibly compact the test array
     */

    void complete() throws JessException {

        // Try to have a Test2Simple first
        for (int i = 0; i < m_nTests; i++) {
            TestBase t = m_tests[i];
            if (t instanceof Test2Simple) {
                Test2Simple t2s = (Test2Simple) t;

                if (t2s.getTest()) {
                    if (t2s.getRightIndex() == -1 ||
                            t2s.getLeftIndex() == -1)
                        continue;

                    if (i > 0) {
                        TestBase tmp = m_tests[0];
                        m_tests[0] = t2s;
                        m_tests[i] = tmp;
                    }

                    m_rightIdx = t2s.getRightIndex();
                    m_tokenIdx = t2s.getTokenIndex();
                    m_leftIdx = t2s.getLeftIndex();
                    m_blessed = true;
                    break;
                }
            }
        }


        // If this fails, try to have a Test2Multi first
        if (!m_blessed) {
            for (int i = 0; i < m_nTests; i++) {
                TestBase t = m_tests[i];
                if (t instanceof Test2Multi) {
                    Test2Multi t2s = (Test2Multi) t;

                    if (t2s.getTest()) {
                        if (t2s.getRightIndex() == -1 ||
                                t2s.getLeftIndex() == -1)
                            continue;

                        if (i > 0) {
                            TestBase tmp = m_tests[0];
                            m_tests[0] = t2s;
                            m_tests[i] = tmp;
                        }
                        m_rightIdx = t2s.getRightIndex();
                        m_rightSubIdx = t2s.getRightSubIndex();
                        m_tokenIdx = t2s.getTokenIndex();
                        m_leftIdx = t2s.getLeftIndex();
                        m_leftSubIdx = t2s.getLeftSubIndex();
                        m_blessed = true;
                        break;
                    }
                }
            }
        }

        initTokenTrees();

    }

    public int getTokenSize() {
        return m_tokenSize + 1;
    }

    public void dependsOn(Fact f, Token t) {
        if (m_logicalDepends == null)
            m_logicalDepends = new HashMap();

        ArrayList list = (ArrayList) m_logicalDepends.get(t);
        if (list == null) {
            list = new ArrayList();
            m_logicalDepends.put(t, list);
        }

        synchronized (list) {
            list.add(f);
        }
    }

    // For testing only
    public Map getLogicalDependencies() {
        return m_logicalDepends;
    }

}




