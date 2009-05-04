
package jess;
import java.io.Serializable;

/** **********************************************************************
 * A class to represent a Java long.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class LongValue extends Value implements Serializable
{
  private long m_long;

  /**
   * Create a LongValue
   * @param l The long value
   * @exception JessException If the type is invalid
   */

  public LongValue(long l) throws JessException
  {
    super((double) l, RU.LONG);
    m_long = l;
  }

  public final long longValue(Context c) throws JessException
  {
    return m_long;
  }

  public final double numericValue(Context c) throws JessException
  {
    return (double) m_long;
  }

  public final int intValue(Context c) throws JessException
  {
    return (int) m_long;
  }

  public final String stringValue(Context c) throws JessException
  {
    return toString();
  }

  public final String toString()
  {
    return new Long(m_long).toString();
  }


  public final boolean equals(Value v)
  {
    if (v.type() != RU.LONG)
      return false;
    else
      return m_long == ((LongValue) v).m_long;
  }
}

