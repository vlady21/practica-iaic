package jess;
import java.util.ArrayList;

/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

class List
{
  private String m_head;
  private String m_indent = "";
  private ArrayList m_data = new ArrayList();

  private char m_open = '(';
  private char m_close = ')';

  /**
   * @param head
   */
  public List(String head)
  {
    m_head = head;
  }

  public List()
  {
    this("");
  }

  /**
   * @param head
   * @param o
   */
  public List(String head, Object o)
  {
    this(head);
    add(o);
  }

  public void setDelimiters(char open, char close)
  {
    m_open = open;
    m_close = close;
  }

  /**
   * @param head
   * @param o
   * @exception JessException
   */
  public List(Value head, Object o) throws JessException
  {
    this(head.stringValue(null));
    add(o);
  }

  /**
   * @param o
   */
  public List add(Object o) { m_data.add(o); return this;}
  /**
   * @param s
   */
  public List addQuoted(String s) {
    add("\"" + s + "\"");
    return this;
  }

  /**
   * @param s
   */
  public void indent(String s) { m_indent = s; }

  public void newLine() { add("\n" + m_indent); }

  /**
   * @return
   */
  public StringBuffer toStringBuffer()
  {
    StringBuffer sb = new StringBuffer(m_data.size() * 6);
    sb.append(m_open);
    sb.append(m_head);
    for (int i = 0; i< m_data.size(); i++)
      {
        if (sb.length() > 1) sb.append(' ');
        sb.append(m_data.get(i));
      }
    sb.append(m_close);
    return sb;
  }

  public String toString()
  {
    return toStringBuffer().toString();
  }

}
