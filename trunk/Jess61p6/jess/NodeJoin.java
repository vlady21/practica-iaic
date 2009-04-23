package jess;

import java.io.Serializable;

/** **********************************************************************
 * Node containing an arbitrary list of tests; used for TEST CE's and also
 * the base class for join nodes.
 ********************************************************************** */

class NodeJoin extends Node implements Serializable {

   /**
     The tests this node performs
     */

    int m_nTests = 0;
    TestBase[] m_tests = new TestBase[2];

    /**
     * Constructor
     */
    NodeJoin() {
    }

    void complete() throws JessException {
        // Nothing to do
    }

    void addTest(int test, int slot_sidx, Value v, Rete engine)
            throws JessException {
        TestBase t = null;

        Funcall f = v.funcallValue(null);

        // if we have an accelerator, try to apply it
        if (ReteCompiler.getAccelerator() != null)
            t = ReteCompiler.getAccelerator().speedup(f, engine);

        // if no acceleration, use the standard Test1 class
        if (t == null)
            t = new Test1(test, slot_sidx, v);


        addTest(t);
    }

    void addTest(TestBase t) {
        if (m_nTests == m_tests.length) {
            TestBase[] temp = new TestBase[m_nTests * 2];
            System.arraycopy(m_tests, 0, temp, 0, m_nTests);
            m_tests = temp;
        }

        m_tests[m_nTests++] = t;
    }

    /**
     * The classic 'you should never inherit from concrete classes' problem!
     */

    void addTest(int test, int token_idx, int left_idx, int leftSub_idx,
                 int right_idx, int rightSub_idx)
            throws JessException {
        throw new JessException("NodeJoin:addtest",
                "Can't add Test2s to this class", "");
    }

    /**
     * For our purposes, two Node2's are equal if every test in one has
     * an equivalent test in the other, and if the test vectors are the
     * same size. The subclass NodeNot2 should never be shared, so
     * we'll report unequal always. This routine is used during network
     * compilation, not at runtime.  */

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (this.getClass() != o.getClass())
            return false;

        NodeJoin n = (NodeJoin) o;

        if (n instanceof NodeNot2 || n instanceof NodeNot2Single)
            return false;

        if (n.m_nTests != m_nTests)
            return false;

        outer_loop:
          for (int i = 0; i < m_nTests; i++) {
              TestBase t1 = m_tests[i];
              for (int j = 0; j < m_nTests; j++) {
                  if (t1.equals(n.m_tests[j]))
                      continue outer_loop;
              }
              return false;
          }
        return true;
    }

    void callNodeLeft(Token token, Context context) throws JessException {
        broadcastEvent(JessEvent.RETE_TOKEN + LEFT, token);
        if (token.m_tag == RU.CLEAR) {
            passAlong(token, context);
            return;
        }

        int ntests = m_nTests;
        boolean result;

        if (token.m_tag == RU.REMOVE || ntests == 0)
            result = true;
        else {
            result = runTests(token, ntests, context);
            token = token.prepare(result);
        }

        if (result) {
            token = Rete.getFactory().newToken(token, Fact.getNullFact());
            token.updateTime(context.getEngine());
            passAlong(token, context);
        }
    }

    boolean runTests(Token token, int ntests, Context context) throws JessException {
        try {
            context.setToken(token);

            for (int i = 0; i < ntests; i++) {
                if (!m_tests[i].doTest(context))
                    return false;
            }
            return true;
        } catch (JessException re) {
            re.addContext("'test' CE");
            throw re;
        }
    }

    /**
     Two-input nodes always make calls to the left input of other nodes.
     */

    void passAlong(Token t, Context context) throws JessException {
        Node[] sa = m_succ;
        for (int j = 0; j < m_nSucc; j++) {
            Node s = sa[j];
            s.callNodeLeft(t, context);
        }
    }

    /**
     * Describe myself
     * @return
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(256);
        sb.append("[NodeJoin ntests=");
        sb.append(m_nTests);
        sb.append(" ");
        for (int i = 0; i < m_nTests; i++) {
            sb.append(m_tests[i].toString());
            sb.append(" ");
        }
        sb.append(";usecount = ");
        sb.append(m_usecount);
        sb.append("]");
        return sb.toString();
    }

    String getCompilationTraceToken() {
        return "2";
    }

}





