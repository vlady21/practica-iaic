package jess;

import java.io.Serializable;
import java.util.HashMap;

/** **********************************************************************
 * Some LISP compatibility functions.
 * <P>
 * See the README for the list of functions in this package.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

class LispFunctions implements IntrinsicPackage, Serializable {

    private void addFunction(Userfunction uf, HashMap ht) {
        ht.put(uf.getName(), uf);
    }

    public void add(HashMap table) {
        addFunction(new Progn(), table);
        addFunction(new Apply(), table);
    }
}

/**
 * Executes a list of function calls, returning the value of the last one
 */

class Progn implements Userfunction, Serializable
{
  public String getName() { return "progn" ;}

  public Value call( ValueVector vv, Context context ) throws JessException
  {
    Value v = Funcall.NIL;
    for (int i=1; i<vv.size(); i++)
      v = vv.get(i).resolveValue(context);
    return v;
  }
}

/**
 * Calls the named function to the list of arguments
 */

class Apply implements Userfunction, Serializable
{
  public String getName() { return "apply" ;}

  public Value call( ValueVector vv, Context context ) throws JessException
  {
    Funcall f = new Funcall(vv.get(1).stringValue(context), context.getEngine());
    for (int i=2; i<vv.size(); i++)
      f.arg(vv.get(i));
    return f.execute(context);
  }
}










