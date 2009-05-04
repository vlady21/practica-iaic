package pruebas;

import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

public class PruebaReglas {
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
		rete.executeCommand("(batch reglasB09.clp)");
		
		//metemos los hechos, serian los datos del usuario
		Fact f = new Fact("datos", rete);
		/*
		 * PREGUNTA DONDE BUSCAR
		 * 
		  f.setSlotValue("estado_actual", new Value("busqueda", RU.STRING));
	      f.setSlotValue("tipo_estudios", new Value("no_universitarios", RU.STRING));
	      f.setSlotValue("conocimiento", new Value("investigacion", RU.STRING));
	      f.setSlotValue("tipo_contrato", new Value("beca", RU.STRING));
	      
	      f.setSlotValue("desea_informacion", new Value("si", RU.STRING));
	      f.setSlotValue("lee_prensa", new Value("si", RU.STRING));
	      f.setSlotValue("ruta_fichero_salida", new Value(ruta, RU.STRING));
	     */
		
		/*
		 * PREGUNTA CURRICULUM
		 * 
			f.setSlotValue("estado_actual", new Value("curriculum", RU.STRING));
			f.setSlotValue("tipo_estudios", new Value("universitarios", RU.STRING));
			f.setSlotValue("experiencia", new Value("no", RU.STRING));
			f.setSlotValue("numero_paginas_CV", new Value(2, RU.INTEGER));
			f.setSlotValue("ruta_fichero_salida", new Value(ruta, RU.STRING));
		 */
		
		/*
		 * PREGUNTA CONTRATO
		 * 
			f.setSlotValue("estado_actual", new Value("contrato", RU.STRING));
			f.setSlotValue("estudia", new Value("si", RU.STRING));
			f.setSlotValue("conocimiento", new Value("basico", RU.STRING));
			f.setSlotValue("tiempo_libre", new Value(8, RU.INTEGER));
			f.setSlotValue("ruta_fichero_salida", new Value(ruta, RU.STRING));
		 */
		
		/*
		 * PREGUNTA ENTREVISTA
		 * 
			f.setSlotValue("estado_actual", new Value("entrevista", RU.STRING));
			f.setSlotValue("sexo", new Value("hombre", RU.STRING));
			f.setSlotValue("seleccionado_entrevista", new Value("si", RU.STRING));
			f.setSlotValue("conoce_perfil", new Value("no", RU.STRING));
			f.setSlotValue("conoce_empresa", new Value("si", RU.STRING));
			f.setSlotValue("conoce_protocolo", new Value("no", RU.STRING));
			f.setSlotValue("ruta_fichero_salida", new Value(ruta, RU.STRING));
			
		 */
		
		/*
		 * PREGUNTA BUSCAR EMPLEO
		 * */
			f.setSlotValue("estado_actual", new Value("busqueda_empleo", RU.STRING));
			f.setSlotValue("rango_edad", new Value("joven", RU.STRING));
			f.setSlotValue("rechazado", new Value("si", RU.STRING));
			f.setSlotValue("situacion_laboral", new Value("trabajando_jornadacompleta", RU.STRING));
			f.setSlotValue("estudio", new Value("no", RU.STRING));
			f.setSlotValue("ruta_fichero_salida", new Value(ruta, RU.STRING));
			
		 /**/
		
	      rete.assertFact(f);

	      //mostramos los hechos y las reglas
	      rete.executeCommand("(watch facts)");
	      rete.executeCommand("(watch rules)");
	      
	      //ejecutamos jess
	      rete.executeCommand("(run)");
	}
}
