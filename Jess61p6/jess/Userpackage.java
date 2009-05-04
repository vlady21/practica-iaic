package jess;

/** **********************************************************************
 * Interface for a collection of functions, user-defined or otherwise.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author E.J Friedman-Hill
 ********************************************************************** */

public interface Userpackage
{

  /**
   * Add this package of functions to the given engine by calling
   * addUserfunction some number of times.
   * @see jess.Rete#addUserfunction
   * @see jess.Rete#addUserpackage
   * @param engine */
  void add(Rete engine);
}
