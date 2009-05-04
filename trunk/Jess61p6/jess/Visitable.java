package jess;

/**
 * A visitable Jess class. Lets you, for example, print out compelx
 * structures ithout putting the printing code in the structures
 * themselves.
 * $Id: Visitable.java,v 1.1.1.1 2003/01/08 04:47:21 ejfried Exp $
 * (C) 2001 E.J. Friedman-Hill and Sandia National Laboratories
 */

public interface Visitable {

    public Object accept(Visitor v);
}
