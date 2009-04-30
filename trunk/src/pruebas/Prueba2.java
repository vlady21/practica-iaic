package pruebas;

import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

public class Prueba2 {

	/**
	 * @param args
	 * @throws JessException 
	 */
	public static void main(String[] args) throws JessException {
		Rete rete=new Rete();
		//limpiamos la MT
		rete.executeCommand("(reset)");
		//cargamos el fichero con las reglas y la template
		rete.executeCommand("(batch prueba2.clp)");
		
		//metemos los hechos, serian los datos del usuario
		Fact f = new Fact("datos", rete);
	      f.setSlotValue("nombre", new Value("juan", RU.STRING));
	      f.setSlotValue("edad", new Value(17, RU.INTEGER));
	      rete.assertFact(f);

	      //mostramos los hechos y las reglas
	      rete.executeCommand("(facts)");
	      rete.executeCommand("(rules)");
	      
	      //ejecutamos jess
	      rete.executeCommand("(run)");
	}

}
