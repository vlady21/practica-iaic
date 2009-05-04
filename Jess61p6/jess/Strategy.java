
package jess;
import java.io.Serializable;

/** **********************************************************************
 * An interface for conflict resolution strategies. Implement this
 * interface, then pass the class name to (set-strategy).
 *<P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public interface Strategy {
    /**
     * To implement your own conflict resolution strategy, you write
     * this method. It should behave like Comparable.compare() (which see.)
     * Place the two activations in order, respecting salience.
     */

    int compare(Activation a1, Activation a2);

    /**
     * Return the name of this strategy
     * @return a display name for this strategy
     */

    String getName();
}

class breadth implements Strategy, Serializable {

    public int compare(Activation a1, Activation a2) {
        int s1 = a1.getSalience();
        int s2 = a2.getSalience();

        if (s1 != s2)
            return s2 - s1;

        else if (a1.isInactive() && !a2.isInactive())
            return -1;
        else if (!a1.isInactive() && a2.isInactive())
            return 1;
        else if (a1.isInactive() && a2.isInactive())
            return 0;

         Token t1 = a1.getToken();
        Token t2 = a2.getToken();

        if (t1.getTime() != t2.getTime())
            return t1.getTime() - t2.getTime();
        else
            return t1.getTotalTime() - t2.getTotalTime();

    }

    public String getName() { return "breadth"; }
}









