/** **********************************************************************
 * A smart lexer for the Jess subset of CLIPS
 *
 * $Id: JessTokenStream.java,v 1.2.2.1 2003/11/12 15:32:21 ejfried Exp $
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

package jess;
import java.io.Serializable;
import java.util.Stack;

/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
class JessTokenStream implements Serializable
{
  private Stack m_stack;
  private Tokenizer m_stream;
  private int m_lineno;
  private StringBuffer m_string = new StringBuffer();

  /**
   * Construct a JessTokenStream.
   * Tell the tokenizer how to separate Jess tokens
   * @param t
   */
  JessTokenStream(Tokenizer t)
  {
    m_stream = t;
    m_stack = new Stack();
  }

  /**
   * Return the current line number, corresponding to the most recently
   * popped or pushed token.
   * @return
   */
  int lineno()
  {
    return m_lineno;
  }

  /**
   * Load a full sexp or lone token onto the stack.
   * @exception JessException
   * @return
   */
  void prepareSexp() throws JessException
  {
    int level = 0;
    m_string.setLength(0);

    JessToken tok;
    Stack temp_stack = new Stack();
    do
      {
        tok = m_stream.nextToken();
        temp_stack.push(tok);
        if (tok.m_ttype == RU.NONE)
          break;    // EOF
        else if (tok.m_ttype == ')')
          --level;
        else if (tok.m_ttype == '(')
          ++level;
      }
    while (level > 0);

    while (!temp_stack.empty())
      {
        m_stack.push(temp_stack.pop());
      }
    // m_stream.discardToEOL();
  }

  /**
   * Return the next token in the stream, or NULL if empty.
   * @exception JessException
   * @return
   */
  JessToken nextToken() throws JessException
  {
    if (m_stack.empty())
      prepareSexp();

    JessToken tok = (JessToken) m_stack.pop();
    m_string.append(tok.toString());
    m_string.append(" ");
    m_lineno = tok.m_lineno;
    return tok;
  }

  /**
   * Infinite pushback
   * @param tok
   */
  void pushBack(JessToken tok)
  {
    m_lineno = tok.m_lineno;
    m_stack.push(tok);
    m_string.setLength(m_string.length() - (tok.toString().length() + 1));
  }

  /**
   * Return the 'car' of a sexp as a String, or null.
   * @exception JessException
   * @return
   */
  String head() throws JessException
  {
    if (m_stack.empty())
      prepareSexp();

    JessToken top = (JessToken) m_stack.pop();
    if (top.m_ttype != '(' || m_stack.empty())
      return null;

    JessToken tok = (JessToken) m_stack.peek();

    m_stack.push(top);

    if (tok.m_ttype != RU.ATOM)
      {
        if (tok.m_ttype == '-')
          return "-";
        else if (tok.m_ttype == '=')
          return "=";
        // This is allowed so we can use shorthand 'JAVACALL' syntax
        else if (tok.m_ttype == RU.VARIABLE)
          return tok.m_sval;
        else
          return tok.toString();
      }
    else
      {
        if (tok.m_sval != null)
          return tok.m_sval;
        else
          return tok.toString();
      }
  }

  void clear()
  {
    m_stack = new Stack();
    m_string.setLength(0);
  }
  /**
   * Print the sexp on the stack
   * @return
   */
  public String toString()
  {
    return m_string.toString();
  }

  public void eatWhitespace() throws JessException {
    m_stream.eatWhitespace();
  }
}












