package jess.factory;

import jess.Fact;
import jess.JessException;
import jess.Token;

import java.io.Serializable;

/**
 * Standard factory implementation
 * @author Ernest J. Friedman-Hill
 * @version 1.0
 */

public class FactoryImpl implements Factory, Serializable
{

  public  Token newToken(Fact firstFact, int tag) throws JessException
  {
    return new Token(firstFact, tag);
  }

  public Token newToken(Token token, Fact newFact) throws JessException
  {
    return new Token(token, newFact);
  }

  public Token newToken(Token lt, Token rt) throws JessException
  {
    return new Token(lt, rt);
  }

  public Token newToken(Token token) throws JessException
  {
    return new Token(token);
  }
}
