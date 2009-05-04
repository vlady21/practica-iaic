package jess;

/** **********************************************************************
 * A test that always fails
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

class Node1NONE extends Node1 {
    void callNodeRight(Token t, Context context) throws JessException {
        return;
    }

    public boolean equals(Object o) {
        return (o instanceof Node1NONE);
    }
}

