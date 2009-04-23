package jess;



/**
 * An accelerator generates Java versions of rule LHSs, compiles them and returns
 * new TestBase objects to execute them.
 * <P>
 * (C) 2003 Ernest J. Friedman-Hill and Sandia National Laboratories<BR>
 * $Id: Accelerator.java,v 1.3.2.1 2003/09/02 14:33:19 ejfried Exp $
 */

public interface Accelerator
{

  /**
   * Given the function call, return a TestBase object.
   * @param f A function call to translate
   * @return A jess.TestBase object that performs equivalently to the Funcall,
   * or null if this Accelerator can't translate the function.
   * @exception JessException If the translation fails unexpectedly.
   */

  TestBase speedup(Funcall f, Rete engine) throws JessException;

}











