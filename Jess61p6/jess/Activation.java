package jess;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * A list of facts that satisfy a rule. An activation contains
 * enough info to bind a rule's variables. You might use this class
 * if you're writing your own Strategy class.
 * <P>
 * (C) 2003 Ernest J. Friedman-Hill and Sandia National Laboratories <BR>
 * $Id: Activation.java,v 1.5 2003/01/23 22:07:14 ejfried Exp $
 * @see jess.Strategy
 */

public class Activation implements Serializable {
    /**
     Token is the token that got us fired.
     */

    private Token m_token;

    /**
     * Get the Rete network Token that caused this Activation.
     * @return The token.
     */

    public final Token getToken() {
        return m_token;
    }

    /**
     Rule is the rule we will fire.
     */

    private Defrule m_rule;

    /**
     * Return the activated rule.
     * @return The rule.
     */

    public final Defrule getRule() {
        return m_rule;
    }


    /**
     * True if activation has been cancelled
     */
    private boolean m_inactive;

    Activation(Token token, Defrule rule) {
        m_token = token;
        m_rule = rule;
        m_salience = m_rule.getSalience();
    }

    /**
     * Query if this activation has been cancelled, or false if it is valid.
     * @return True if this activation has been cancelled.
     */

    public boolean isInactive() {
        return m_inactive;
    }

    void setInactive() {
        m_inactive = true;
    }

    private int m_salience;

    /**
     * Evaluate and return the current salience for the rule
     * referenced in this activation.
     * @return The salience value.
     */
    public int getSalience() {
        return m_salience;
    }

    void fire(Rete engine) throws JessException {
        m_rule.fire(m_token, engine);
    }

    private int m_seq;

    void setSequenceNumber(int i) {
        m_seq = i;
    }

    void debugPrint(PrintWriter ps) {
        m_rule.debugPrint(m_token, m_seq, ps);
    }

    /**
     Compare this object to another Activation.
     @param o The Activation to compare to.
     */

    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (!(o instanceof Activation))
            return false;

        else {
            Activation a = (Activation) o;
            return
                    this.m_rule == a.m_rule &&
                    this.m_token.dataEquals(a.m_token);
        }
    }

    boolean getAutoFocus() {
        return m_rule.getAutoFocus();
    }

    String getModule() {
        return m_rule.getModule();
    }

    void evalSalience(Rete r) throws JessException {
        m_salience = m_rule.evalSalience(r);
    }

    /**
     * Produce a string representation of this Activation for use in debugging.
     * @return The string representation
     */

    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("[Activation: ");
        sb.append(m_rule.getDisplayName());
        sb.append(" ");
        sb.append(m_token.factList());
        sb.append(" ; time=");
        sb.append(m_token.getTime());
        sb.append(" ; salience=");
        sb.append(getSalience());
        sb.append("]");
        return sb.toString();
    }
}

