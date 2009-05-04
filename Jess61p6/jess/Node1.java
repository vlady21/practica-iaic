
package jess;

import java.io.Serializable;

/** **********************************************************************
 * Single-input nodes of the pattern network
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

abstract class Node1 extends Node implements Serializable
{
  /**
   * Do the business of this node.
   * The input token of a Node1 should only be single-fact tokens.
   *
   * RU.CLEAR means flush two-input ndoe memories; we just pass these along.
   * All one-input nodes must call this and just immediately return *false*
   * if it returns true!
   */

  boolean processClearCommand(Token t, Context context) throws JessException {
      broadcastEvent(JessEvent.RETE_TOKEN + RIGHT, t);
      if (t.m_tag == RU.CLEAR) {
          passAlong(t, context);
          return true;
      } else
          return false;
  }

  // Nodes that will store values should call this to clean them up.
  Value cleanupBindings(Value v) throws JessException
  {
    if (v.type() == RU.BINDING)
      {
        BindingValue bv = new BindingValue((BindingValue) v);
        bv.resetFactNumber();
        return bv;
      }

    else if (v.type() == RU.FUNCALL)
      {
        Funcall vv = (Funcall) v.funcallValue(null).clone();
        for (int i=0; i<vv.size(); i++)
          vv.set(cleanupBindings(vv.get(i)), i);
        return new FuncallValue(vv);
      }
    else
      return v;
  }

  void passAlong(Token t, Context context) throws JessException
  {
    Node [] sa = m_succ;
    for (int j=0; j<m_nSucc; j++)
      {
        Node s = sa[j];
        s.callNodeRight(t, context);
      }
  }

  /**
   * callNode can call this to print debug info
   */
  void debugPrint(Token t, boolean result) {

    System.out.println(this + " " + t.topFact() + " => " + result);
  }

  String getCompilationTraceToken() { return "1"; }
}




