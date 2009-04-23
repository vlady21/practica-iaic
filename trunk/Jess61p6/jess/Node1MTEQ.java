package jess;

/** **********************************************************************
 * Test multislot value and type.
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */
class Node1MTEQ extends Node1 {
    private int m_idx, m_subidx;
    private Value m_value;

    Node1MTEQ(int idx, int subidx, Value val) throws JessException {
        m_idx = idx;
        m_subidx = subidx;
        m_value = cleanupBindings(val);
    }

    void callNodeRight(Token t, Context context) throws JessException {
        try {
            if (processClearCommand(t, context))
                return;

            else if (t.m_tag == RU.REMOVE) {
                passAlong(t, context);
                return;
            }

            boolean result = false;

            Value s;
            Fact fact = t.topFact();
            if ((s = fact.get(m_idx)).type() == RU.LIST) {
                ValueVector vv = s.listValue(null);
                if (vv.size() >= m_subidx) {
                    Value subslot = vv.get(m_subidx);
                    if (m_value.type() == RU.FUNCALL) {
                        context.setFact(fact);
                        context.setToken(t);

                        if (!m_value.resolveValue(context).equals(Funcall.FALSE))
                            result = true;

                        // inform extensions that functions were called
                        // and result of calls
                        t = t.prepare(result);
                    } else if (subslot.equals(m_value.resolveValue(context)))
                        result = true;
                }
            }

            // debugPrint(fact, result);

            if (result)
                passAlong(t, context);

            return;
        } catch (JessException re) {
            re.addContext("rule LHS (MTEQ)");
            throw re;
        } catch (Exception e) {
            JessException re = new JessException("Node1MTEQ.call",
                    "Error during LHS execution",
                    e);
            re.addContext("rule LHS");
            throw re;

        }

    }

    public String toString() {
        return "[Test that the multislot entry at index " + m_idx + ", subindex " + m_subidx +
                " equals " + m_value + "]";
    }


    public boolean equals(Object o) {
        if (o instanceof Node1MTEQ) {
            Node1MTEQ n = (Node1MTEQ) o;
            return (m_idx == n.m_idx && m_subidx == n.m_subidx && m_value.equals(n.m_value));
        } else
            return false;
    }

}

