/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conocimiento;

import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import utilerias.LectorConsejos;

/**
 *
 * @author Jose Miguel
 */
public class LanzadorJess {
    private String _rutaficheroconsejos="log_grupoB09.txt";
    private String _rutaficheroreglas="reglasB09.clp";
    private LectorConsejos _lector=null;
	private	Fact _fact=null;
    private Rete _rete=null;

    public LanzadorJess(String rutafichero, String rutaficheroreglas) throws JessException{
        _rutaficheroconsejos=rutafichero;
        _rutaficheroreglas=rutaficheroreglas;
        _lector=new LectorConsejos();
        _rete=new Rete();
    }

    public void arrancarJess() throws JessException{
        
		//limpiamos la MT
		_rete.executeCommand("(reset)");
		//cargamos el fichero con las reglas y la template
		_rete.executeCommand("(batch "+_rutaficheroreglas+")");

		_fact = new Fact("datos", _rete);
        //cargamos el valor del slot del fichero con el valor de la ruta
        _fact.setSlotValue("ruta_fichero_salida", new Value(_rutaficheroconsejos, RU.STRING));

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
		 * 
			_fact.setSlotValue("estado_actual", new Value("busqueda_empleo", RU.STRING));
			_fact.setSlotValue("rango_edad", new Value("joven", RU.STRING));
			_fact.setSlotValue("rechazado", new Value("si", RU.STRING));
			_fact.setSlotValue("situacion_laboral", new Value("trabajando_jornadacompleta", RU.STRING));
			_fact.setSlotValue("estudio", new Value("no", RU.STRING));
			
		 */

    }

    public void insertaSlotValue(String slot, Object valorSlot) throws JessException{
        //si lo que me llega es un numero
        if(valorSlot instanceof Integer){
            _fact.setSlotValue(slot, new Value((Integer)valorSlot, RU.INTEGER));
        }else{//si no es un numero sera un string
            _fact.setSlotValue(slot, new Value((String)valorSlot, RU.STRING));
        }
    }

    public void ejecutarJess() throws JessException{
            _rete.executeCommand("(reset)");
          //insertamos el fact con los valores
	      _rete.assertFact(_fact);

	      //mostramos los hechos y las reglas por consola
	      _rete.executeCommand("(facts)");
	      _rete.executeCommand("(watch rules)");

	      //ejecutamos jess
	      _rete.executeCommand("(run)");

          //leemos los consejos del fichero
          _lector.leerConsejos(_rutaficheroconsejos);
    }

    public String dameConsejos(){
        return _lector.dameConsejos();
    }
    public void borrarConsejos(){
        _lector.limpiarConsejos(_rutaficheroconsejos);
    }

}
