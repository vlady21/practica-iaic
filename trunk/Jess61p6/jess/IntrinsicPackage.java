package jess;

import java.util.HashMap;

/** **********************************************************************
 * Interface for a collection of built-in functions
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author E.J Friedman-Hill
 ********************************************************************** */

interface IntrinsicPackage {

    /**
     * Add this package of functions to the given Hashtable by name.
     */
    void add(HashMap ht);
}
