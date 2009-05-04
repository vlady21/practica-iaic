package jess;

import java.io.Serializable;


/**
 * A conditional element is either a Pattern or a Group of Patterns.
 *
 * (C) 2003 Sandia National Laboratories
 * $Id: ConditionalElement.java,v 1.6 2003/02/05 02:18:07 ejfried Exp $
 */
public interface ConditionalElement extends Serializable {

    String getName();

    String getBoundName();

    int getGroupSize();

    boolean isGroup();

    ConditionalElement getConditionalElement(int i);

}
