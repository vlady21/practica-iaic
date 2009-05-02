package pruebas;

import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

public class Reglas {
	/**
	 * @param args
	 * @throws JessException 
	 */
	public static void main(String[] args) throws JessException {
		String ruta="log_grupoB09.txt";
		Rete rete=new Rete();
		//limpiamos la MT
		rete.executeCommand("(reset)");
		//cargamos el fichero con las reglas y la template
		rete.executeCommand("(batch reglas.clp)");
		
		//metemos los hechos, serian los datos del usuario
		Fact f = new Fact("datos", rete);
	      f.setSlotValue("estado_actual", new Value("busqueda", RU.STRING));
	      f.setSlotValue("tipo_estudios", new Value("universitarios", RU.STRING));
	      f.setSlotValue("conocimiento", new Value("investigacion", RU.STRING));
	      f.setSlotValue("tipo_contrato", new Value("beca", RU.STRING));
	      f.setSlotValue("ruta_fichero_salida", new Value(ruta, RU.STRING));
	      rete.assertFact(f);

	      //mostramos los hechos y las reglas
	      rete.executeCommand("(facts)");
	      rete.executeCommand("(rules)");
	      
	      //ejecutamos jess
	      rete.executeCommand("(run)");
	}
}
