package jess;
import java.io.Serializable;

/** **********************************************************************
 * A conflict resolution strategy.
 *<P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

class depth implements Strategy, Serializable {

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
            return t2.getTime() - t1.getTime();
        else
            return t2.getTotalTime() - t1.getTotalTime();

    }

    public String getName() { return "depth"; }
}
