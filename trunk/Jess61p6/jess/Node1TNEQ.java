package jess;

/** **********************************************************************
 * Test that a slot value is NOT the same as some value;
 * test passes if either type or value differs.
 *
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */
class Node1TNEQ extends Node1 {
    private Value m_value;
    private int m_idx;

    Node1TNEQ(int idx, Value val) throws JessException {
        m_value = cleanupBindings(val);
        m_idx = idx;
    }

    void callNodeRight(Token t, Context context) throws JessException {
        if (processClearCommand(t, context))
            return;
        else if (t.m_tag == RU.REMOVE) {
            passAlong(t, context);
            return;
        }

        try {
            boolean result = false;
            Fact fact = t.topFact();
            if (m_value.type() == RU.FUNCALL) {
                context.setFact(fact);
                context.setToken(t);

                if (m_value.resolveValue(context).equals(Funcall.FALSE))
                    result = true;

                // inform extensions that functions were called and result of calls
                t = t.prepare(result);
            } else if (!fact.get(m_idx).equals(m_value.resolveValue(context))) {
                result = true;
            }

            if (result)
                passAlong(t, context);

            //debugPrint(token, callType, fact, result);
            return;
        } catch (JessException re) {
            re.addContext("rule LHS (TNEQ)");
            throw re;
        } catch (Exception e) {
            JessException re = new JessException("Node1TNEQ.call",
                    "Error during LHS execution",
                    e);
            re.addContext("rule LHS (TNEQ)");
            throw re;

        }
    }

    public String toString() {
        return "[Test that slot at index " + m_idx + " is not equal to " + m_value + "]";
    }

    public boolean equals(Object o) {
        if (o instanceof Node1TNEQ) {
            Node1TNEQ n = (Node1TNEQ) o;
            return (m_idx == n.m_idx && m_value.equals(n.m_value));
        } else
            return false;
    }

}
