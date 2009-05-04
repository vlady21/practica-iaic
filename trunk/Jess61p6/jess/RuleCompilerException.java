package jess;


/**
 * JessException is the parent type of all exceptions thrown by public methods
 * in the Jess library.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class RuleCompilerException extends JessException {

  /**
   * Constructs a RuleCompilerException containing a descriptive message.
   * @param routine the name of the routine this exception occurred in.
   * @param msg an informational message.
   */

  public RuleCompilerException(String routine, String msg) {
      super(routine, msg, "");
  }
}
