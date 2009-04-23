package jess;

/** **********************************************************************
 * Test multislot value and type for inequality.
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */
class Node1MTNEQ extends Node1 {

    private int m_idx, m_subidx;
    private Value m_value;

    Node1MTNEQ(int idx, int subidx, Value val) throws JessException {
        m_idx = idx;
        m_subidx = subidx;
        m_value = cleanupBindings(val);
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
            Value s;
            if ((s = fact.get(m_idx)).type() == RU.LIST) {
                ValueVector vv = s.listValue(null);
                if (vv.size() >= m_subidx) {

                    Value subslot = vv.get(m_subidx);
                    if (m_value.type() == RU.FUNCALL) {
                        context.setFact(fact);
                        context.setToken(t);

                        if (m_value.resolveValue(context).equals(Funcall.FALSE))
                            result = true;

                        // inform extensions that functions were called and result of calls
                        t = t.prepare(result);
                    } else if (!subslot.equals(m_value.resolveValue(context)))
                        result = true;
                }
            }

            if (result)
                passAlong(t, context);

            //debugPrint(token, callType, fact, result);
            return;
        } catch (JessException re) {
            re.addContext("rule LHS (MTNEQ)");
            throw re;
        } catch (Exception e) {
            JessException re = new JessException("Node1MTNEQ.call",
                    "Error during LHS execution",
                    e);
            re.addContext("rule LHS (MTNEQ)");
            throw re;

        }
    }

    public String toString() {
        return "[Test that subslot " + m_subidx + " of multislot " + m_idx +
                " does not equal " + m_value + "]";
    }

    public boolean equals(Object o) {
        if (o instanceof Node1MTNEQ) {
            Node1MTNEQ n = (Node1MTNEQ) o;
            return (m_idx == n.m_idx && m_subidx == n.m_subidx && m_value.equals(n.m_value));
        } else
            return false;
    }
}

