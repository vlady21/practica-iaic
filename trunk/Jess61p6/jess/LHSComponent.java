package jess;

import java.util.Map;

/**
 * Either a pattern, or a group of patterns.
 */

interface LHSComponent extends Cloneable {
    void setBoundName(String name) throws JessException;

    void setNegated();
    void setExplicit();
    void setLogical();
    boolean getLogical();
    boolean getNegated();
    LHSComponent canonicalize() throws JessException;

    boolean getBackwardChaining();
    void addToGroup(Group g) throws JessException;
    void addDirectlyMatchedVariables(Map map) throws JessException;
    void renameUnmentionedVariables(Map map, Map subs, int size, HasLHS container)
        throws JessException;
    boolean isBackwardChainingTrigger();
    int getPatternCount();
    Object clone();

    String getName();

    String getBoundName();

    int getGroupSize();

    LHSComponent getLHSComponent(int i);
}
