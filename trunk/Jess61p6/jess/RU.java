package jess;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * General utilities for Jess. All fields and methods in this class are static, and
 * there is no constructor.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class RU implements Serializable
{
  private RU() {}

  /** Relative index of slot name within a deftemplate's slots */
  final public static int DT_SLOT_NAME      = 0;
  /** Relative index of slot default value within a deftemplate's slots */
  final public static int DT_DFLT_DATA      = 1;
  /** Relative index of slot data type within a deftemplate's slots */
  final public static int DT_DATA_TYPE      = 2;
  /** Size of a slot in a deftemplate */
  final public static int DT_SLOT_SIZE      = 3;

  /** Data type of "no value" */
  final public static int NONE             = 0;
  /** Data type of atom */
  final public static int ATOM             = 1 <<  0;
  /** Data type of string */
  final public static int STRING           = 1 <<  1;
  /** Data type of integer */
  final public static int INTEGER          = 1 <<  2;
  /** Data type of a variable */
  final public static int VARIABLE         = 1 <<  3;
  /** Data type of a fact id */
  final public static int FACT          = 1 <<  4;
  /** Data type of float */
  final public static int FLOAT            = 1 <<  5;
  /** Data type of function call stored in a value */
  final public static int FUNCALL          = 1 <<  6;
   /** Data type of a list stored in a value */
  final public static int LIST             = 1 <<  9;
  /** Data type of external address */
  final public static int EXTERNAL_ADDRESS = 1 << 11;
  /** Data type of variable binding stored in value (internal use) */
  final public static int BINDING          = 1 << 12;
  /** Data type of multivariable */
  final public static int MULTIVARIABLE    = 1 << 13;
  /** Data type of slot name stored in a value */
  final public static int SLOT             = 1 << 14;
  /** Data type of multislot name stored in a value */
  final public static int MULTISLOT        = 1 << 15;
  /** Data type of Java long*/
  final public static int LONG             = 1 << 16;

  private static Hashtable m_typeNames = new Hashtable();
  static
  {
    m_typeNames.put(String.valueOf(NONE), "NONE");
    m_typeNames.put(String.valueOf(ATOM), "ATOM");
    m_typeNames.put(String.valueOf(STRING), "STRING");
    m_typeNames.put(String.valueOf(INTEGER), "INTEGER");
    m_typeNames.put(String.valueOf(VARIABLE), "VARIABLE");
    m_typeNames.put(String.valueOf(FACT), "FACT");
    m_typeNames.put(String.valueOf(FLOAT), "FLOAT");
    m_typeNames.put(String.valueOf(FUNCALL), "FUNCALL");
    m_typeNames.put(String.valueOf(LIST), "LIST");
    m_typeNames.put(String.valueOf(EXTERNAL_ADDRESS), "EXTERNAL_ADDRESS");
    m_typeNames.put(String.valueOf(BINDING), "BINDING");
    m_typeNames.put(String.valueOf(MULTIVARIABLE), "MULTIVARIABLE");
    m_typeNames.put(String.valueOf(SLOT), "SLOT");
    m_typeNames.put(String.valueOf(MULTISLOT), "MULTISLOT");
    m_typeNames.put(String.valueOf(LONG), "LONG");
  }


  /**
   * Given a type constant (ATOM, STRING, INTEGER, etc.) return a String version of
   * the name of that type ("ATOM", "STRING", "INTEGER", etc.)
   *
   * @param type One of the type constants in this class
   * @return The String name of this type, or null if the constant is out of range.
   */

  public static String getTypeName(int type)
  {
    return (String) m_typeNames.get( String.valueOf(type));
  }


  /** Add this token to the Rete network (internal use) */
  final static int ADD       = 0;
  /** Remove this token from the Rete network (internal use) */
  final static int REMOVE    = 1;
  /** Update this token in the Rete network (internal use) */
  final static int UPDATE    = 2;
  /** Clear the Rete network (internal use) */
  final static int CLEAR     = 3;

  /*
    Constants specifying that a variable is bound to a fact-index
    or is created during rule execution
    */

  /** Variable contains a fact index */
  public final static int PATTERN = -1;
  /** Variable is local to a defrule or deffunction */
  public final static int LOCAL   = -2;
  /** Variable is global */
  public final static int GLOBAL  = -3;

  /*
    Constants specifying connective constraints
  */

  /** Test is anded with previous */
  final static int AND = 1;

  /** Test is ored with previous */
  final static int OR = 2;


  /** String prepended to deftemplate names to form backwards chaining goals */
  final static String BACKCHAIN_PREFIX = "need-";

  /** Special multislot name used for ordered facts */
  final static String DEFAULT_SLOT_NAME = "__data";

  /**  The name of the ultimate parent of all deftemplates */
  final static String ROOT_DEFTEMPLATE = "__fact";

  /*
    A number used in quickly generating semi-unique symbols.
    */

  static int s_gensymIdx = 0;

  /**
   * Generate a pseudo-unique symbol starting with "prefix"
   * @param prefix The alphabetic part of the symbol
   * @return The new symbol
   */
  public static synchronized String gensym(String prefix)
  {
    String sym = prefix + s_gensymIdx++;
    return sym;
  }

  /**
   * Get a property, but return null on SecurityException
   * @param prop The property name to get
   * @return The value of the property, or null if none or security problem
   */
  public static String getProperty(String prop)
  {
    try
      {
        return System.getProperty(prop);
      }
    catch (SecurityException se)
      {
        return null;
      }
  }

    static String scopeName(String module, String name) {
        StringBuffer sb = new StringBuffer(module);
        sb.append("::");
        sb.append(name);
        return sb.toString();
    }

    static String getModuleFromName(String name, Rete rete) {
        int index = name.indexOf("::");
        if (index == -1)
            return rete.getCurrentModule();
        else
            return name.substring(0, index);
    }


  static long time() {
      return System.currentTimeMillis();
  }
}





