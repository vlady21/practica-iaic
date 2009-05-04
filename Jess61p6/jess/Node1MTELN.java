package jess;

/** **********************************************************************
 * Test multislot length.
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

class Node1MTELN extends Node1
{
  private int m_idx, m_len;

  Node1MTELN(int idx, int len)
  {
    m_idx = idx;
    m_len = len;
  }

  void callNodeRight(Token t, Context context) throws JessException
  {
    if (processClearCommand(t, context))
      return;

    try
      {
        boolean result = false;
        Fact fact = t.topFact();
        Value s;
        if ((s = fact.get(m_idx)).type() == RU.LIST)
          {
            ValueVector vv = s.listValue(null);
            if (vv.size() == m_len)
              result = true;
          }

        // debugPrint(fact, result);

        if (result)
          passAlong(t, context);

        return;
      }
    catch (JessException re)
      {
        re.addContext("rule LHS (MTELN)");
        throw re;
      }
    catch (Exception e)
      {
        JessException re = new JessException("Node1MTELN.call",
                                             "Error during LHS execution",
                                             e);
        re.addContext("rule LHS (MTELN)");
        throw re;

      }
  }

  public String toString()
  {
    return "[Test that the multislot at index " + m_idx + " is " + m_len + " items long]";
  }

  public boolean equals(Object o)
  {
    if (o instanceof Node1MTELN)
      {
        Node1MTELN n = (Node1MTELN) o;
        return (m_idx == n.m_idx && m_len == n.m_len);
      }
    else
      return false;
  }
}




