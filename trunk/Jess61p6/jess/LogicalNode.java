package jess;

import java.util.Map;

/**
 * A Node that can give logical support to a fact
 * (C) E.J. Friedman-Hill and 2001 Sandia National Laboratories
 * $Id: LogicalNode.java,v 1.1.1.1 2003/01/08 04:47:21 ejfried Exp $
 */

interface LogicalNode {
    public void dependsOn(Fact f, Token t);
    public int getTokenSize();
    public Map getLogicalDependencies();
}
