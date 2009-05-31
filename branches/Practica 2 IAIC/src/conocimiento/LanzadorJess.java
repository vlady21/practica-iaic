/**
 * Clase encargada de ejecutar Jess y mediar con el, insertando
 * valores en los slots correspondientes, tambien se encarga de
 * leer/borrar los consejos generados.
 */

package conocimiento;

import java.util.Properties;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import utilerias.Constantes;
import utilerias.EscribirConsejos;
import utilerias.LectorConsejos;
import utilerias.Propiedades;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class LanzadorJess {
    //propiedades
    private Properties configuracion = Propiedades.getPropiedades(Constantes.CONFIGURACION);
    private String _rutaficheroconsejos=configuracion.getProperty("FICHERO_GUARDAR");
    private String _rutaficheroreglas=configuracion.getProperty("REGLAS_TECNICO");
    //lector de consejos
    private LectorConsejos _lector=null;
    //hecho para insertar
	private	Fact _fact=null;
    //motor de inferencia
    private Rete _rete=null;

    /**
     * Contructor parametrizado
     *
     * @param rutafichero String correspondiente al fichero de salida para guardar el log.
     * @param rutaficheroreglas String correspondiente al fichero donde se almacenan las reglas a ejecutar.
     * @throws jess.JessException
     */
    public LanzadorJess(String rutafichero, String rutaficheroreglas) throws JessException{
        //asignamos la ruta donde volcaremos el resultado final
        EscribirConsejos.asignaRuta(_rutaficheroconsejos);

        _rutaficheroconsejos=rutafichero;
        _rutaficheroreglas=rutaficheroreglas;

        //creamos el lector de consejos
        _lector=new LectorConsejos();
        _rete=new Rete();
    }

    /**
     * Metodo que arranca Jess, resetea la MT, carga el fichero de reglas,
     * obtiene el hecho (fact) de la template e inserta el valor del slot
     * correspondiente al fichero de salida.
     *
     * @throws jess.JessException
     */
    public void arrancarJess() throws JessException{        
		//limpiamos la MT
		_rete.executeCommand("(reset)");
		//cargamos el fichero con las reglas y la template
		_rete.executeCommand("(batch "+_rutaficheroreglas+")");
        //obtenemos el hecho correspondiente a la template
		_fact = new Fact("datos", _rete);
        //cargamos el valor del slot del fichero con el valor de la ruta
        _fact.setSlotValue("ruta_fichero_salida", new Value(_rutaficheroconsejos, RU.STRING));
    }

    /**
     * Metodo que asigna un valor a un determinado slot de la template.
     *
     * @param slot String correspondiente al nombre del slot a modificar
     * @param valorSlot String correspondiente al valor del slot
     * @throws jess.JessException
     */
    public void insertaSlotValue(String slot, Object valorSlot) throws JessException{
        //si lo que me llega es un numero
        if(valorSlot instanceof Integer){
            _fact.setSlotValue(slot, new Value((Integer)valorSlot, RU.INTEGER));
        }else{//si no es un numero sera un string
            _fact.setSlotValue(slot, new Value((String)valorSlot, RU.STRING));
        }
    }

    public void ejecutarJess() throws JessException{
          //limpiamos la MT
          _rete.executeCommand("(reset)");

          //insertamos el fact con los valores
	      _rete.assertFact(_fact);

	      //mostramos los hechos y las reglas por consola
	      _rete.executeCommand("(watch facts)");
	      _rete.executeCommand("(watch rules)");

          //mostramos los hechos y las reglas por consola
          _rete.executeCommand("(facts)");
          _rete.executeCommand("(rules)");

	      //ejecutamos jess
	      _rete.executeCommand("(run)");

          //leemos los consejos del fichero
          _lector.leerConsejos(_rutaficheroconsejos);
    }

    /**
     * Metodo que devuelve los consejos del fichero.
     *
     * @return String correspondiente a los consejos generados.
     */
    public String dameConsejos(){
        return _lector.dameConsejos();
    }

    /**
     * Metodo que limpia el fichero consejos.
     */
    public void borrarConsejos(){
        _lector.limpiarConsejos(_rutaficheroconsejos);
    }

}
