package jess;

/** **********************************************************************
 * Not a test at all. Split a fact into lots of possible alternate facts,
 * each representing a possible multifield mapping within a multislot.
 *
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */
class Node1MTMF extends Node1
{
  private int m_idx;
    private int m_slotSize;
    private int m_nMultifields;
    private boolean[] m_multiIndexes;

  Node1MTMF(int idx, boolean [] multiIndexes)
  {
    m_idx = idx;
    m_slotSize = multiIndexes.length;
    m_multiIndexes = multiIndexes;
    for (int i=0; i<m_slotSize;i++)
      if (m_multiIndexes[i])
        {
          ++m_nMultifields;
      }
  }

  void callNodeRight(Token t, Context context) throws JessException
  {
    if (processClearCommand(t, context))
      return;

    // Get the existing contents of the slot of interest
    ValueVector vv = t.fact(0).get(m_idx).listValue(null);

    // Length of the existing slot
    int size = vv.size();

    // Number of elements to be matched by all multifields together
    int multisize = size - m_slotSize + m_nMultifields;

    // If this is true, we don't have enough data to match this slot.
    if (multisize < 0)
      return;

    partition(t, multisize, m_nMultifields, context);

    return;
  }


  // K is the number of sets; v is the integer to partition.  This is
  // very brute-force, but it should work; it can be optimized later
  // by computing the msd directly, rather than cycling through it. A
  // partition is viewed as a K-digit base-N number, with the LSD in
  // plan[0].
  private void partition(Token t, int v, int k, Context context) throws JessException
  {
    int[] plan = new int[k];

    do
      {
        if (testForSum(plan, v, k))
          process(t, plan, context);
        addOne(plan, v, k);
      }
    while (plan[k - 1] <= v);

  }


  // Adds one to the k-digit base-v number in plan.
  private void addOne(int[] plan, int v, int k)
  {
    ++plan[0];
    for (int i=0; i<k-1; i++)
      {
        if (plan[i] > v)
          {
            plan[i] = 0;
            ++plan[i+1];
          }
      }
  }

  // Tests that the k numbers in plan sum to v.
  private boolean testForSum(int[] plan, int v, int k)
  {
    int sum=0;
    for (int i=0; i<k; i++)
      sum += plan[i];
    return sum == v;
  }

  private void process(Token t, int[] plan, Context context) throws JessException
  {
    // Create a new version of the fact
    Fact f = createModifiedFact(t.fact(0), t.fact(0).get(m_idx).listValue(null), plan);

    // And send it on its way.
    t = Rete.getFactory().newToken(f, t.m_tag);
    passAlong(t, context);
  }

  private Fact createModifiedFact(Fact oldFact, ValueVector vv, int[] plan)
    throws JessException
  {
    // Make a new fact
    Fact f = (Fact) oldFact.clone();
    f.setIcon(oldFact);

    // the slot contents
    ValueVector newVv = new ValueVector();
    newVv.setLength(m_slotSize);

    for (int i=0, j=0, p=0; i<m_slotSize; i++)
      {
        if (! m_multiIndexes[i])
          newVv.set(vv.get(j++), i);
        else
          {
            // Vector to hold the sub multifield
            ValueVector multifield = new ValueVector();
            int planEntry = plan[p++];
            multifield.setLength(planEntry);

            // Transfer the number of items
            for (int k=0; k<planEntry; k++)
              multifield.set(vv.get(j++), k);

            // Put it into the new slot
            newVv.set(new Value(multifield, RU.LIST), i);
          }
      }

    // Put the new slot into the fact
    f.set(new Value(newVv, RU.LIST), m_idx);
    return f;
  }


  public String toString()
  {
    return "[Split the multislot at index " + m_idx + " into " + m_slotSize +
      " pieces]";
  }

  public boolean equals(Object o)
  {
    if (o instanceof Node1MTMF)
      {
        Node1MTMF n = (Node1MTMF) o;
        if (m_idx != n.m_idx ||
            m_slotSize != n.m_slotSize ||
            m_nMultifields != n.m_nMultifields)
          return false;

        for (int i=0; i<m_slotSize; i++)
          if (m_multiIndexes[i] != n.m_multiIndexes[i])
            return false;



        return true;
      }
    else
      return false;
  }
}

