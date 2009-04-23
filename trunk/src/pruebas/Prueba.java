package pruebas;

import jess.*;

public class Prueba {
	
	  public static void main(String[] argv) throws JessException
	  {
	    Rete rete = new Rete();
	    rete.executeCommand("(batch prueba.clp)");
	  }

}
