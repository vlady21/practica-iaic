/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Jose Miguel
 */
public class Reglas_1 {

    private ArrayList<String> _listaSlot=null;
    private ArrayList<String> _listaValores=null;
    private static Properties configuracion = Propiedades.getPropiedades(Constantes.CONFIGURACION);
    private static String _rutaficheroconsejos=configuracion.getProperty("FICHERO_GUARDAR");
    private String _rutaficheroreglas=configuracion.getProperty("REGLAS_TECNICO");
    private static LectorConsejos _lector=null;
    private static Rete _rete=null;
    private static boolean _usable=false;

    public Reglas_1(ArrayList<String> listaValores,String rutafichero, String rutaficheroreglas) throws JessException{
        _listaValores=listaValores;
        _rutaficheroconsejos=rutafichero;
        _rutaficheroreglas=rutaficheroreglas;
        _lector=new LectorConsejos();
        _rete=new Rete();
        _usable=false;

        rellenarFacts();
        
        //limpiamos la MT
		_rete.executeCommand("(reset)");
		//cargamos el fichero con las reglas y la template
		_rete.executeCommand("(batch "+_rutaficheroreglas+")");

        insertarFact("estado_actual","reglas_1",false);
        insertarFact("ruta_fichero_salida",_rutaficheroconsejos,false);
        insertarFact("fichero_salida","ficheroGuardar",false);

        for(int i=0;i<_listaValores.size();i++){
            try{
                int numero = Integer.parseInt(_listaValores.get(i));
                insertarFact(_listaSlot.get(i),_listaValores.get(i),true);
            }catch(Exception e){
                insertarFact(_listaSlot.get(i),_listaValores.get(i),false);
            }
        }
    }

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

    public static void insertarFact(String campoSlot, String valor, boolean esEntero) throws JessException{
        if(esEntero){
            _rete.executeCommand("(assert ("+campoSlot+ " "+valor+"))");

            System.out.println("(assert ("+campoSlot+ " "+valor+"))");
        }else{
            _rete.executeCommand("(assert ("+campoSlot+ " \""+valor+"\"))");

            System.out.println("(assert ("+campoSlot+ " \""+valor+"\"))");
        }
    }

    public static void ejecutarReglas_1() throws JessException{

          //mostramos los hechos y las reglas por consola
          _rete.executeCommand("(watch facts)");
          _rete.executeCommand("(watch rules)");

           //mostramos los hechos y las reglas por consola
          _rete.executeCommand("(facts)");
          //_rete.executeCommand("(rules)");

          //ejecutamos jess
          _rete.executeCommand("(run)");

          //leemos los consejos del fichero
          _lector.leerConsejos(_rutaficheroconsejos);
    }

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

    public static boolean esUsable(){
        return _usable;
    }

}
