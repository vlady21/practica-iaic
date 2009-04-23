
package jess;
import java.io.Serializable;

/** **********************************************************************
 * A class to represent a Fact-id. Contains the actual fact object.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class FactIDValue extends Value implements Serializable {
  /**
   * Create a FactIDValue
   * @param f The fact
   * @exception JessException If the type is invalid
   */

  public FactIDValue(Fact f) throws JessException
  {
    super(f);
  }
}

