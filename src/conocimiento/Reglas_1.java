/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conocimiento;

import java.util.ArrayList;
import jess.JessException;
import jess.Rete;
import utilerias.LectorConsejos;

/**
 *
 * @author Jose Miguel
 */
public class Reglas_1 {

    private ArrayList<String> _listaSlot=null;
    private ArrayList<String> _listaValores=null;
    private String _rutaficheroconsejos="log_grupoB09.txt";
    private String _rutaficheroreglas="reglasB09.clp";
    private LectorConsejos _lector=null;
    private Rete _rete=null;

    public Reglas_1(ArrayList<String> listaValores,String rutafichero, String rutaficheroreglas) throws JessException{
        _listaValores=listaValores;
        _rutaficheroconsejos=rutafichero;
        _rutaficheroreglas=rutaficheroreglas;
        _lector=new LectorConsejos();
        _rete=new Rete();

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

          //mostramos los hechos y las reglas por consola
          //_rete.executeCommand("(watch facts)");
          //_rete.executeCommand("(watch rules)");

           //mostramos los hechos y las reglas por consola
          _rete.executeCommand("(facts)");
          _rete.executeCommand("(rules)");

          //ejecutamos jess
          _rete.executeCommand("(run)");

          //leemos los consejos del fichero
          _lector.leerConsejos(_rutaficheroconsejos);
    }

    public void rellenarFacts(){
        _listaSlot=new ArrayList<String>();
        _listaSlot.add("estudios");
        _listaSlot.add("experiencia");
        _listaSlot.add("tiempo-desempleado");
        _listaSlot.add("edad");
        _listaSlot.add("sexo");
        _listaSlot.add("tiene-curriculum");
        _listaSlot.add("idioma");
        _listaSlot.add("idioma");
        _listaSlot.add("interes");
        _listaSlot.add("tiene-coche");
        _listaSlot.add("carnet-conducir");
        _listaSlot.add("pretensiones-salariales");
        _listaSlot.add("meses-desde-ultimo-curso");
        _listaSlot.add("ultimo-trabajo");
        _listaSlot.add("tiempo-trabajando");
        _listaSlot.add("tiempo-desempleado");
        _listaSlot.add("trabajo-no-especializado");
        _listaSlot.add("experiencia-extranjero");
        _listaSlot.add("ultima-entrevista");
    }

    public void insertarFact(String campoSlot, String valor, boolean esEntero) throws JessException{
        if(esEntero){
            _rete.executeCommand("(assert ("+campoSlot+ " "+valor+"))");
        }else{
            _rete.executeCommand("(assert ("+campoSlot+ " \""+valor+"\"))");
        }
    }
}
