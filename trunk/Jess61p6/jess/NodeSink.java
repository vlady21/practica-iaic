package jess;

/*
 * Created by IntelliJ IDEA.
 * (C) 2002 Sandia National Laboratories
 * $Id: NodeSink.java,v 1.1.1.1 2003/01/08 04:47:21 ejfried Exp $
 */

public interface NodeSink {
    /**
     * Return a string (useful for debugging) describing all the Rete network
     * nodes connected to this construct.
     * @return A textual description of all the nodes used by this construct
     */

    String listNodes();

    void addNode(Node n) throws JessException;

}