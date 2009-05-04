package jess.factory;

import jess.Fact;
import jess.JessException;
import jess.Token;

/**
 * Allows extensions that get into the guts of Jess
 * @author Ernest J. Friedman-Hill
 * @version 1.0
 */

public interface Factory
{
  Token newToken(Fact firstFact, int tag) throws JessException;
  Token newToken(Token t, Fact newFact) throws JessException;
  Token newToken(Token lt, Token rt) throws JessException;
  Token newToken(Token t) throws JessException;
}
