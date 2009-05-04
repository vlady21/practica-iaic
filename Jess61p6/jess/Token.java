package jess;
import java.io.Serializable;

/**
 * A Token is the fundamental unit of communication in the Rete
 * network. Each Token represents one or more facts and an indication
 * of whether those facts are being asserted or being retracted.  <P>
 * Only Accelerator implementors will use this class.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 * @see jess.Accelerator
 */

public class Token implements Serializable {

    int m_tag;
    int m_negcnt;

    /**
       sortcode is used by the engine to hash tokens and prevent long
       linear memory searches
    */

    int m_sortcode;

    private Fact m_fact;
    private int m_size;

    // Tokens refer to 'parents' (of which they are a superset)
    private Token m_parent;

    // Needed for "logical" CE.
    Token getParent() {
        return m_parent;
    }

    /**
     * Return the last fact in the Token (the "most recent" one.)
     * @return The fact
     */

    public final Fact topFact() {
        return m_fact;
    }

    /**
     * Return a fact from this token
     * @param i The index (0-based) of the fact to retrieve. More recent
     * (later) facts have larger indexes.
     * @return The fact
     */

    public final Fact fact(int i) {

        int j = m_size - i;

        Token where = this;
        while (--j > 0)
            where = where.m_parent;

        return where.m_fact;
    }

    /**
     * Returns the number of facts in this token
     * @return The size
     */
    public final int size() {
        return m_size;
    }

    /**
     * tag should be RU.ADD or RU.REMOVE
     */

    public Token(Fact firstFact, int tag) throws JessException {
        // m_parent = null;
        ++m_size;
        m_fact  = firstFact;
        m_tag = tag;
        m_totalTime = firstFact.getTime();
        m_time = m_totalTime;
        // m_negcnt = 0;
        m_sortcode = firstFact.getFactId();
    }

    /**
     * Create a new Token containing the same data as an old one
     */
    public Token(Token t, Fact newFact) throws JessException {
        m_fact = newFact;
        m_parent = t;
        m_tag = t.m_tag;
        // m_negcnt = 0;
        m_size = t.m_size + 1;
        m_sortcode = (t.m_sortcode << 3) + newFact.getFactId();
        int time = newFact.getTime();
        m_totalTime = t.m_totalTime + time;
        m_time = Math.max(time, t.m_time);
    }

    /**
     * Create a new Token containing the same data as an old one
     */

    public Token(Token lt, Token rt) throws JessException {
        this(lt, rt.topFact());
    }

    /**
     * Create a new Token identical to an old one
     */
    public Token(Token t) throws JessException {
        m_fact = t.m_fact;
        m_parent = t.m_parent;
        m_tag = t.m_tag; // (t.m_tag == RU.UPDATE ? RU.ADD : t.m_tag);
        // m_negcnt = 0;
        m_size = t.m_size;
        m_sortcode = t.m_sortcode;
        m_time = t.m_time;
        m_totalTime = t.m_totalTime;
        m_negcnt = t.m_negcnt;
    }


    /** The total time step represented by all of this token's facts. */
    private int m_totalTime, m_time;
    void updateTime(Rete engine) {
        int time = engine.getTime();
        m_totalTime = m_totalTime - m_fact.getTime() + time;
        m_time = time;
    }

    int getTime() {
        return m_time;
    }

    int getTotalTime() {
        return m_totalTime;
    }

    /**
     * Compare the data in this token to another token.  The tokens are
     * assumed to be of the same size (same number of facts).  We have
     * to compare all the fact data if the fact IDs are the same, since
     * each fact can exist in different multifield versions. This could
     * be skipped if we had a fast test for multislot existence...
     * @param t Another token to compare to
     * @return True if the tokens represent the same list of facts (tags
     * are irrelevant)
     */
    final public boolean dataEquals(Token t) {
        if (t == this)
            return true;

        else if (m_sortcode != t.m_sortcode)
            return false;

        else if (m_fact.getFactId() != t.m_fact.getFactId())
            return false;

        else if (!m_fact.equals(t.m_fact))
            return false;

        else if (m_parent == t.m_parent)
            return true;

        else
            return m_parent.dataEquals(t.m_parent);
    }


    /**
     * Compare this token to another object.
     * @param o Another object to compare to
     * @return True if the object is a Token and dataEquals returns true.
     */

    public boolean equals(Object o) {
        if (o instanceof Token)
            return dataEquals((Token) o);
        else
            return false;
    }

    /**
     * Return a string (useful for debugging) describing this token.
     * @return The formatted String
     */
    public String toString() {

        StringBuffer sb = new StringBuffer(100);
        sb.append("[Token: size=");
        sb.append(m_size);
        sb.append(";sortcode=");
        sb.append(m_sortcode);
        sb.append(";tag=");
        sb.append(m_tag == RU.ADD ? "ADD" :
                  (m_tag == RU.UPDATE ? "UPDATE" : "REMOVE"));
        sb.append(";negcnt=");
        sb.append(m_negcnt);
        sb.append(";facts=");
        for (int i=0; i<m_size; i++) {
            sb.append(fact(i).toString());
            sb.append(";");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Return a string describing a list of facts
     * @return
     */
    String factList() {
        StringBuffer sb = new StringBuffer(100);
        boolean first = true;
        for (int i=0; i<size(); i++) {
            if (!first)
                sb.append(",");
            int id = fact(i).getFactId();
            if (id != -1) {
                sb.append(" f-");
                sb.append(id);
            }
            first = false;
        }
        return sb.toString();
    }

    /**
     * Use the sortcode, based on the contained facts, as the hashcode.
     * @return A semi-unique identifier */

    public int hashCode() {
        return m_sortcode;
    }

    /**
     * A chance for a token to duplicate itself
     * Used by extensions
     */

    public Token prepare(boolean b) throws JessException {
        return this;
    }

}

