/**
 * Clase creada para manejar el uso de la BC agregada
 * del Grupo 6, al diferir de nuestra filosofia en la
 * aplicacion, se ha creado una clase aparte para tratar
 * las reglas.
 */

package conocimiento;

import java.util.ArrayList;
import java.util.Properties;
import jess.JessException;
import jess.Rete;
import utilerias.Constantes;
import utilerias.LectorConsejos;
import utilerias.Propiedades;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class Reglas_1 {
    //lista con los nombres de los slots
    private ArrayList<String> _listaSlot=null;
    //lista con los valores de los slots
    private ArrayList<String> _listaValores=null;
    //propiedades
    private static Properties configuracion = Propiedades.getPropiedades(Constantes.CONFIGURACION);
    private static String _rutaficheroconsejos=configuracion.getProperty("FICHERO_GUARDAR");
    private String _rutaficheroreglas=configuracion.getProperty("REGLAS_TECNICO");
    //lector de consejos
    private static LectorConsejos _lector=null;
    //motor de inferencia
    private static Rete _rete=null;
    //podemos usar estas reglas
    private static boolean _usable=false;

    /**
     * Contructor parametrizado
     *
     * @param listaValores ArrayList<String> con los valores que tendran los slot del aserto
     * @param rutafichero String correspondiente al fichero de salida para guardar el log.
     * @param rutaficheroreglas String correspondiente al fichero donde se almacenan estas reglas
     * @throws jess.JessException
     */
    public Reglas_1(ArrayList<String> listaValores,String rutafichero, String rutaficheroreglas) throws JessException{
        _listaValores=listaValores;
        _rutaficheroconsejos=rutafichero;
        _rutaficheroreglas=rutaficheroreglas;
        _lector=new LectorConsejos();

        //creamos el motor de inferencia
        _rete=new Rete();
        _usable=false;

        //creamos los nombres de los slots
        rellenarFacts();
        
        //limpiamos la MT
		_rete.executeCommand("(reset)");
		//cargamos el fichero con las reglas
		_rete.executeCommand("(batch "+_rutaficheroreglas+")");

        //insertamos los primeros hechos correspondientes al estado y ruta de los ficheros
        insertarFact("estado_actual","reglas_1",false);
        insertarFact("ruta_fichero_salida",_rutaficheroconsejos,false);
        insertarFact("fichero_salida","ficheroGuardar",false);

        //para todos los valores de los slots
        for(int i=0;i<_listaValores.size();i++){
            try{
                //vemos si es un numero
                int numero = Integer.parseInt(_listaValores.get(i));
                //insertamos el hecho con el true indicando que es un numero
                insertarFact(_listaSlot.get(i),_listaValores.get(i),true);
            }catch(Exception e){
                //si salta una excepcion es que no es un numero y por lo tanto es
                //un String y se inserta con el valor false que indica que no es un numero
                insertarFact(_listaSlot.get(i),_listaValores.get(i),false);
            }
        }
    }

    /**
     * Metodo que inserta los nombres de los slots para la MT
     */
    public void rellenarFacts(){
        _listaSlot=new ArrayList<String>();
        _listaSlot.add("tipo_estudios");
        _listaSlot.add("tiempo_experiencia");
        _listaSlot.add("tiempo-desempleado");
        _listaSlot.add("edad");
        _listaSlot.add("sexo");
        _listaSlot.add("tiene-curriculum");
        _listaSlot.add("idioma1");
        _listaSlot.add("idioma2");
        _listaSlot.add("interes");
        _listaSlot.add("tiene-coche");
        _listaSlot.add("carnet-conducir");
        _listaSlot.add("pretensiones-salariales");
        _listaSlot.add("meses-desde-ultimo-curso");
        _listaSlot.add("ultimo-trabajo");
        _listaSlot.add("tiempo-trabajando");
        _listaSlot.add("trabajo-no-especializado");
        _listaSlot.add("experiencia-extranjero");
        _listaSlot.add("ultima-entrevista");
    }

    /**
     * Como la filosofia es insertar hechos, este metodo inserta el hecho o bien
     * como numero o como String, en caso de ser String iria entre comillas dobles ""
     *
     * @param campoSlot String correspondiente al nombre del slot
     * @param valor String correspondiente al valor del slot
     * @param esEntero Booleano que indica si es un numero o no el valor insertado.
     * @throws jess.JessException
     */
    public static void insertarFact(String campoSlot, String valor, boolean esEntero) throws JessException{
        if(esEntero){
            _rete.executeCommand("(assert ("+campoSlot+ " "+valor+"))");
        }else{
            _rete.executeCommand("(assert ("+campoSlot+ " \""+valor+"\"))");
        }
    }

    public static void ejecutarReglas_1() throws JessException{

          //mostramos la traza de los hechos y las reglas por consola
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
     * Metodo que indica si un determinado campo pertenece a estas reglas o no,
     * en caso de pertenecer se activa la variable _usable para permitir su ejecucion.
     *
     * @param campo String correspondiente al campo a comprobar
     * @return True si pertenece o False si no.
     */
    public static boolean pertenece(String campo){
        if(
                campo.equalsIgnoreCase("tipo_estudios") ||
                campo.equalsIgnoreCase("tiempo_experiencia") ||
                campo.equalsIgnoreCase("edad") ||
                campo.equalsIgnoreCase("sexo") ||
                campo.equalsIgnoreCase("tiene-curriculum") ||
                campo.equalsIgnoreCase("idioma1") ||
                campo.equalsIgnoreCase("idioma2") ||
                campo.equalsIgnoreCase("interes") ||
                campo.equalsIgnoreCase("tiene-coche") ||
                campo.equalsIgnoreCase("carnet-conducir") ||
                campo.equalsIgnoreCase("pretensiones-salariales") ||
                campo.equalsIgnoreCase("meses-desde-ultimo-curso") ||
                campo.equalsIgnoreCase("ultimo-trabajo") ||
                campo.equalsIgnoreCase("tiempo-trabajando") ||
                campo.equalsIgnoreCase("tiempo-desempleado") ||
                campo.equalsIgnoreCase("trabajo-no-especializado") ||
                campo.equalsIgnoreCase("experiencia-extranjero") ||
                campo.equalsIgnoreCase("ultima-entrevista")
                ){
                _usable=true;
                return true;
        }
        return false;
    }

    /**
     * Metodo que indica si estas Reglas son usables o no para activarlas.
     * 
     * @return Boolean correspondiente a la variable usable.
     */
    public static boolean esUsable(){
        return _usable;
    }

}
