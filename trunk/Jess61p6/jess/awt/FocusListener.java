package jess.awt;
import jess.JessException;
import jess.Rete;

import java.awt.event.FocusEvent;

/** **********************************************************************
 * FocusListener
 * An AWT Event Adapter for Jess.
 *
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
public class FocusListener extends JessAWTListener
                           implements java.awt.event.FocusListener
{
  /**
   * Connect the Jess function specified by name to this event handler object. When this
   * handler receives an AWT event, the named function will be invoked in the given
   * engine.
   * @param uf The name of a Jess function
   * @param engine The Jess engine to execute the function in
   * @exception JessException If anything goes wrong.
   */
  public FocusListener(String uf, Rete engine) throws JessException
  {
    super(uf, engine);
  }

  /**
   * An event-handler method. Invokes the function passed to the constructor with the
   * received event as the argument.
   * @param e The event
   */
  public void focusGained(FocusEvent e) { receiveEvent(e); }
  /**
   * An event-handler method. Invokes the function passed to the constructor with the
   * received event as the argument.
   * @param e The event
   */
  public void focusLost(FocusEvent e) { receiveEvent(e); }
}
