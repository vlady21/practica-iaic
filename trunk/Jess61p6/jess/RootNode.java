package jess;

import jess.Node1;
import jess.Token;
import jess.JessException;
/**
 * The root of the Rete network.
 * (C) 2003 Sandia National Laboratories
 * $Id: RootNode.java,v 1.2 2003/01/16 15:57:50 ejfried Exp $
 */

class RootNode extends Node1 {
    void callNodeRight(Token t, Context context) throws JessException {
        passAlong(t, context);
    }

    public String toString() {
        return "The root of the Rete network";
    }
}