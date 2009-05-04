package jess;

import java.io.Serializable;

/** **********************************************************************
 * A sort of Hashtable of Tokens kept by sortcode
 *
 * $Id: TokenTree.java,v 1.2 2003/01/11 01:45:21 ejfried Exp $
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

class TokenTree implements Serializable {
    int m_hash;

    TokenVector[] m_tokens;

    boolean m_useSortcode;
    int m_tokenIdx, m_factIdx, m_subIdx;

    TokenTree(int hash, boolean useSortCode,
              int tokenIdx, int factIdx, int subIdx) {
        m_hash = hash;
        m_useSortcode = useSortCode;
        m_factIdx = factIdx;
        m_subIdx = subIdx;
        m_tokenIdx = tokenIdx;
        m_tokens = new TokenVector[m_hash];
    }

    final void clear() {
        for (int i=0; i< m_hash; i++)
            if (m_tokens[i] != null)
                m_tokens[i].clear();
    }

    private Token subsetToken(Token t) {
        Token parent = t;
        while (parent.size() > m_tokenIdx)
            parent = parent.getParent();
        return parent;
    }


    private int codeForToken(Token t) throws JessException {
        int code;

        if (m_useSortcode) {
            if (m_tokenIdx == 0)
                code = t.m_sortcode;
            else
                code = subsetToken(t).m_sortcode;

        } else if (m_factIdx == -1)
            code = t.fact(m_tokenIdx).getFactId();

        else if (m_subIdx == -1)
            code = t.fact(m_tokenIdx).m_v[m_factIdx].hashCode();

        else
            code = t.fact(m_tokenIdx).
                m_v[m_factIdx].listValue(null).m_v[m_subIdx].hashCode();


        if (code < 0)
            code = -code;

        return code;
    }

    synchronized boolean add(Token t, boolean update) throws JessException {

        int code = codeForToken(t);

        TokenVector v = findCodeInTree(code, true);

        if (update) {
            int size = v.size();
            for (int i=0; i< size; i++) {
                Token tt = v.elementAt(i);
                if (t.dataEquals(tt)) {
                    return false;
                }
            }
        }

        v.addElement(t);
        return true;
    }

    synchronized boolean remove(Token t) throws JessException {

        int code = codeForToken(t);

        TokenVector v = findCodeInTree(code, false);

        if (v == null)
            return false;

        int size = v.size();

        if (size == 0)
            return false;

        for (int i=0; i< size; i++) {
            Token tt = v.elementAt(i);
            if (t.dataEquals(tt)) {
                v.removeElementAt(i);
                return true;
            }
        }
        return false;
    }

    synchronized TokenVector findCodeInTree(int code, boolean create) {
        code = code % m_hash;

        if (code < 0)
            code = -code;

        if (create && m_tokens[code] == null)
            return m_tokens[code] = new TokenVector();
        else
            return m_tokens[code];
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<m_hash; ++i) {
            if (m_tokens[i] != null) {
                sb.append(i);
                sb.append(": ");
                sb.append(m_tokens[i]);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}



