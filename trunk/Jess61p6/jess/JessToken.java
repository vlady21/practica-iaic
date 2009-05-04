/** **********************************************************************
 * A packet of info  about a token in the input stream.
 *
 * $Id: JessToken.java,v 1.2 2003/01/11 01:45:21 ejfried Exp $
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

package jess;
import java.io.Serializable;

/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
final class JessToken implements Serializable
{
  String m_sval;
  double m_nval;
  int m_lineno;
  int m_ttype;

  /**
   * @param context
   * @exception JessException
   * @return
   */
  Value tokenToValue(Context context) throws JessException
  {
    // Turn the token into a value.
    switch (m_ttype)
      {
      case RU.FLOAT:
        return new Value(m_nval, RU.FLOAT);
      case RU.INTEGER:
        return new Value(m_nval, RU.INTEGER);
      case RU.STRING:
        return new Value(m_sval, RU.STRING);
      case RU.VARIABLE: case RU.MULTIVARIABLE:
        if (context != null)
          return context.getVariable(m_sval);
        else
          return new Value("?" + m_sval, RU.ATOM);

      case RU.ATOM:
        return new Value(m_sval, RU.ATOM);

      case RU.NONE:
        if ("EOF".equals(m_sval))
          return Funcall.EOF;
        // FALL THROUGH

      default:
        {
          return new Value("" + (char) m_ttype, RU.STRING);
        }
      }
  }

  /**
   * @return
   */
  boolean isBlankVariable()
  {
    return (m_sval != null && m_sval.startsWith(Tokenizer.BLANK_PREFIX));
  }

  /**
   * @return
   */
  public String toString()
  {
    if (m_ttype == RU.VARIABLE)
      return "?" + m_sval;
    else if (m_ttype == RU.MULTIVARIABLE)
      return "$?" + m_sval;
    else if (m_ttype == RU.STRING)
      return "\"" + m_sval + "\"";
    else if (m_sval != null)
      return m_sval;
    else if (m_ttype == RU.FLOAT)
      return "" + m_nval;
    else if (m_ttype == RU.INTEGER)
      return "" + (int) m_nval;
    else return "" +  (char) m_ttype;
  }

}




