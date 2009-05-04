package jess;
import java.io.Serializable;

/** **********************************************************************
 * Specialized two-input nodes for negated pattern
 *
 * $Id: NodeNot2.java,v 1.5.2.1 2003/06/19 15:44:48 ejfried Exp $
 * (C) 2001 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

class NodeNot2 extends Node2 implements Serializable {
    private int m_size;

    NodeNot2(int hashkey, int size) throws JessException{
        super(hashkey);
        m_size = size;
        complete();
    }

    void callNodeLeft(Token token, Context context) throws JessException {
        // UPDATE tokens will be copied by the superclass method.
        if (token.m_tag == RU.ADD)
            token = Rete.getFactory().newToken(token);

        super.callNodeLeft(token, context);
    }

    /**
     * Run all the tests on a given (left) token and every token in the
     * right memory. Every time a right token *passes* the tests, increment
     * the left token's negation count; at the end, if the
     * left token has a zero count, pass it through.
     *
     * The 'nullToken' contains a fact used as a placeholder for the 'not' CE.
     */

    void runTestsVaryRight(Token lt, TokenTree th, Context context) throws JessException {
        if (lt.m_tag != RU.REMOVE)
            super.runTestsVaryRight(lt, th, context);

        if (lt.m_negcnt == 0) {
            Token newToken = Rete.getFactory().newToken(lt, Fact.getNullFact());
            newToken.updateTime(context.getEngine());
            m_matches++;
            passAlong(newToken, context);
            if (m_logicalDepends != null)
                removeLogicalSupportFrom(newToken, context);
        }
        return;
    }

    void doRunTestsVaryRight(Token lt, TokenTree th, Context context)
        throws JessException {


        if (th == null)
            return;

        int code = lt.hashCode();
        TokenVector tv = th.findCodeInTree(code, false);
        doRunTestsVaryRight(lt, tv, context);
        return;
    }

    boolean doRunTestsVaryRight(Token lt, TokenVector v, Context context)
        throws JessException {
        if (v != null) {
            int size = v.size();
            if (size > 0) {
                int ntests = m_nTests;
                context.setToken(lt);

                for (int i=0; i<size; i++) {
                    Token rt = v.elementAt(i);

                    if (runTests(ntests, context, rt)) {
                        lt.m_negcnt++;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Run all the tests on a given (right) token and every relevant
     * token in the left memory. For the true ones, increment (or
     * decrement) the appropriate negation counts. Any left token
     * which transitions to zero gets passed along.
     *
     * "Relevant" here means that the sort code of the left token is
     * the same as the sort code of the "prefix" of the right
     * token. The "prefix" is the part that precedes the "not" pattern
     * on the rule LHS.
     */

    void doRunTestsVaryLeft(Token rt, TokenTree th, Context context) throws JessException {
        if (th == null)
            return;

        Token parent = subsetRightToken(rt);
        int code = parent.hashCode();
        TokenVector tv = th.findCodeInTree(code, false);

        doRunTestsVaryLeft(rt, tv, context);
        return;
    }

    void doRunTestsVaryLeft(Token rt, TokenVector v, Context context)
        throws JessException {

        if (v != null) {
            int size = v.size();
            if (size > 0) {
                int ntests = m_nTests;
                int tag = rt.m_tag;
                for (int i=0; i<size; i++) {
                    Token lt = v.elementAt(i);
                    context.setToken(lt);

                    // Optimization, but it doesn't check for
                    // corrupted negcnt errors. Haven't seen one of
                    // those in a long time, though.
                    if (tag == RU.REMOVE && lt.m_negcnt == 0) {
                        continue;
                    }

                    if (runTests(ntests, context, rt)) {
                        if (tag == RU.ADD || tag == RU.UPDATE) {
                            // retract any activation due to the left token
                            Token nt2 = Rete.getFactory().
                                newToken(lt, Fact.getNullFact());
                            nt2.updateTime(context.getEngine());
                            nt2.m_tag = RU.REMOVE;
                            passAlong(nt2, context);
                            if (m_logicalDepends != null)
                                removeLogicalSupportFrom(nt2, context);
                            lt.m_negcnt++;

                        } else if (--lt.m_negcnt == 0) { // tag == REMOVE
                            // pass along the revitalized left token
                            Token nt2 = Rete.getFactory().
                                newToken(lt, Fact.getNullFact());
                            nt2.updateTime(context.getEngine());
                            passAlong(nt2, context);
                            if (m_logicalDepends != null)
                                removeLogicalSupportFrom(nt2, context);

                        }
                    }
                }
            }
        }
        return;
    }

    Token subsetRightToken(Token rt) {
        Token parent = rt;
        while (parent.size() > m_size)
            parent = parent.getParent();
        return parent;
    }

    /**
     * This override is the whole purpose for this class. It returns
     * true if left and right tokens don't share a prefix.
     */

    boolean runTests(int ntests, Context context, Token rt)
        throws JessException {

        Token lt = context.getToken();
        rt = subsetRightToken(rt);
        return rt  == lt || rt.dataEquals(lt);
    }

    // Called from the Constructor and from readObject
    protected void initTokenTrees() {
        boolean useSortCode = true;

        if (m_left == null)
            m_left = new TokenTree(m_hashkey, useSortCode, 0, 0, 0);
        else
            m_left.clear();

        if (m_right == null)
            m_right = new TokenTree(m_hashkey, useSortCode, m_size, 0,0);
        else
            m_right.clear();
    }

    /**
     * Describe myself
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(256);
        sb.append("[NodeNot2");
        sb.append(";usecount = ");
        sb.append(m_usecount);
        sb.append("]");
        return sb.toString();
    }


    void addTest(int test, int token_idx, int left_idx, int leftSub_idx,
                 int right_idx, int rightSub_idx) {
        addTest(null);
    }

    void addTest(TestBase t) {
        throw new RuntimeException("ABORT: Can't add tests to NodeNot2");
    }

}

