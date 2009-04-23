package jess;
import java.io.Serializable;

/** **********************************************************************
 * A class to represent a Jess function call stored in a Value.
 * It is 'self-resolving' using Context
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class FuncallValue extends Value implements Serializable
{
  /**
   * @param value
   * @param type
   * @exception JessException
   */

  public FuncallValue(Funcall f) throws JessException
  {
    super(f, RU.FUNCALL);
  }

  public Value resolveValue(Context c) throws JessException
  {
    if (c == null)
      throw new JessException("FuncallValue.resolveValue",
                              "Null context for",
                              funcallValue(c).toStringWithParens());

    else
      return funcallValue(c).execute(c);
  }

  public final Object externalAddressValue(Context c) throws JessException
  {
    return resolveValue(c).externalAddressValue(c);
  }

  public final Fact factValue(Context c) throws JessException
  {
    return resolveValue(c).factValue(c);
  }

  public final ValueVector listValue(Context c) throws JessException
  {
    return resolveValue(c).listValue(c);
  }

  public final int intValue(Context c) throws JessException
  {
    return resolveValue(c).intValue(c);
  }

  public final double floatValue(Context c) throws JessException
  {
    return resolveValue(c).floatValue(c);
  }

  public final double numericValue(Context c) throws JessException
  {
    return resolveValue(c).numericValue(c);
  }

  public final String atomValue(Context c) throws JessException
  {
    return resolveValue(c).atomValue(c);
  }

  public final String variableValue(Context c) throws JessException
  {
    return resolveValue(c).variableValue(c);
  }

  public final String stringValue(Context c) throws JessException
  {
    return resolveValue(c).stringValue(c);
  }
}

