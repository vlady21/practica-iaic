package jess;

import java.util.Arrays;

/**
 * An unsynchronized Mini-Vector.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
class TokenVector implements java.io.Serializable
{
  private Token m_v[];
  private int m_ptr = 0;

  TokenVector() { m_v = new Token[3]; }

  /**
   * @return
   */
  final int size() { return m_ptr; }

  final void clear() {
      if (m_ptr > 0) {
          Arrays.fill(m_v, 0, m_ptr, null);
          m_ptr = 0;
      }
  }

  /**
   * @param val
   */
  final void addElement(Token val)
  {
    if (m_ptr >= m_v.length)
      {
        Token [] nv = new Token[m_v.length * 2];
        System.arraycopy(m_v, 0, nv, 0, m_v.length);
        m_v = nv;
      }
    m_v[m_ptr++] = val;
  }

  /**
   * @param i
   * @return
   */
  final Token elementAt(int i)
  {
      return m_v[i];
  }

  final void removeElementAt(int i)
  {
    m_v[i] = m_v[m_ptr-1];
    m_v[--m_ptr] = null;
  }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<m_ptr; ++i) {
            sb.append(m_v[i]);
            sb.append(" ");
        }
        return sb.toString();
    }

}

