/** **********************************************************************
 * Specialized two-input nodes for negated patterns
 *
 * NOTE: CLIPS behaves in a surprising way which I'm following here.
 * Given this defrule:
 * <PRE>
 * (defrule test-1
 *  (bar)
 *  (not (foo))
 *  =>
 *  (printout t "not foo"))
 *
 * CLIPS behaves this way:
 *
 * (watch activations)
 * (assert (bar))
 * ==> Activation 0 test-1
 * (assert (foo))
 * <== Activation 0 test-1
 * (retract (foo))
 * ==> Activation 0 test-1
 *
 * This is not surprising yet. Here's the funky part
 *
 * (run)
 * "not foo"
 * (assert (foo))
 * (retract (foo))
 * ==> Activation 0 test-1
 *
 * The rule fires,  and all that's required to fire it again is for the
 * "not-ness" to be removed and replaced; the (bar) fact does not need to
 * be replaced. This obviously falls out of the implementation; it makes things
 * easy!
 *
 * </PRE>
 *
 * $Id: NodeNot2Single.java,v 1.6.2.1 2003/06/19 15:44:48 ejfried Exp $
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

package jess;
import java.io.Serializable;

/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
class NodeNot2Single extends Node2 implements Serializable {

    NodeNot2Single(int hashkey) throws JessException {
        super(hashkey);
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
     * @param lt
     * @param th
     * @exception JessException
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

    /**
     * @param lt
     * @param th
     * @exception JessException
     */
    void doRunTestsVaryRight(Token lt, TokenTree th, Context context)
        throws JessException {
        if (th == null)
            return;

        for (int j=0; j<th.m_hash; j++)
            doRunTestsVaryRight(lt, th.m_tokens[j], context);

        return;
    }

    boolean doRunTestsVaryRight(Token lt, TokenVector v, Context context) throws JessException {
        if (v != null) {
            int size = v.size();
            if (size > 0) {
                int ntests = m_nTests;
                context.setToken(lt);

                for (int i=0; i<size; i++) {
                    if (ntests == 0 || runTests(ntests, context, v.elementAt(i)))
                        lt.m_negcnt++;
                }
            }
        }
        return false;

    }

    /**
     * Run all the tests on a given (right) token and every token in the
     * left memory. For the true ones, increment (or decrement) the appropriate
     * negation counts. Any left token which transitions to zero gets passed
     * along.
     * @param rt
     * @param th
     * @exception JessException
     */

    void doRunTestsVaryLeft(Token rt, TokenTree th, Context context) throws JessException {
        if (th == null)
            return;

        for (int j=0; j<th.m_hash; j++) {
            TokenVector v = th.m_tokens[j];
            doRunTestsVaryLeft(rt, v, context);
        }

        return;
    }

    void doRunTestsVaryLeft(Token rt, TokenVector v, Context context) throws JessException {
        if (v != null) {
            int size = v.size();
            if (size > 0) {
                int ntests = m_nTests;
                int tag = rt.m_tag;
                for (int i=0; i<size; i++) {
                    Token lt = v.elementAt(i);
                    context.setToken(lt);

                    if (ntests == 0 || runTests(ntests, context, rt)) {
                        if (tag == RU.ADD || tag == RU.UPDATE) {
                            // retract any activation due to the left token
                            Token nt2 = Rete.getFactory().newToken(lt, Fact.getNullFact());
                            nt2.updateTime(context.getEngine());
                            nt2.m_tag = RU.REMOVE;
                            passAlong(nt2, context);
                            if (m_logicalDepends != null)
                                removeLogicalSupportFrom(nt2, context);
                            lt.m_negcnt++;
                        } else if (--lt.m_negcnt == 0) {
                            // pass along the revitalized left token
                            Token nt2 = Rete.getFactory().newToken(lt, Fact.getNullFact());
                            nt2.updateTime(context.getEngine());
                            passAlong(nt2, context);
                            if (m_logicalDepends != null)
                                removeLogicalSupportFrom(nt2, context);
                        }
                        else if (lt.m_negcnt < 0)
                            throw new JessException("NodeNot2.RunTestsVaryLeft",
                                                    "Corrupted Negcnt (< 0)",
                                                    "");
                    }
                }
            }
        }
        return;
    }
    /**
     * Describe myself
     * @return
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(256);
        sb.append("[NodeNot2Single ntests=");
        sb.append(m_nTests);
        sb.append(" ");
        for (int i=0; i<m_nTests; i++) {
            sb.append(m_tests[i].toString());
            sb.append(" ");
        }
        sb.append(";usecount = ");
        sb.append(m_usecount);
        sb.append("]");
        return sb.toString();
    }
}
