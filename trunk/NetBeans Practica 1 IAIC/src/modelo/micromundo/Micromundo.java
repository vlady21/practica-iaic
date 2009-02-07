package modelo.micromundo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import observador.Observador;
import modelo.juegos.GestorJuegos;
import modelo.matrices.GeneraMatrices;
import modelo.matrices.MatrizConexiones;
import modelo.matrices.MatrizProblemas;
import aima.search.framework.GraphSearch;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.framework.TreeSearch;
import aima.search.informed.AStarSearch;
import aima.search.informed.GreedyBestFirstSearch;
import aima.search.uninformed.BreadthFirstSearch;
import aima.search.uninformed.DepthFirstSearch;
import aima.search.uninformed.DepthLimitedSearch;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * Hebra que genera el mundo, de esta manera mantenemos
 * la ejecucion del micromundo independiente de la ejecucion.
 * 
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
@SuppressWarnings("unchecked")
public class Micromundo extends Thread {
    //Vista
	private static Observador _observer;
    //Problema a resolver
	private Problem _problema=null;
    //Busqueda a realizar en el problema
	private Search _search=null;
    //Nodo inicial del micromundo
	private InterfazPlaneta _planeta=null;
	private static int _coste=0;
	private int _semilla=0;
	private static ArrayList<Planeta> _listaPlanetas=new ArrayList<Planeta>();
	private static ArrayList<String> _solucion=new ArrayList<String>();
    //Calculo del tiempo de ejecucion
    private Calendar _t0,_t1;

	public Micromundo(){
		_coste=0;
		_listaPlanetas=new ArrayList<Planeta>();
        _solucion=new ArrayList<String>();
		_semilla=GeneraMatrices.dameInstancia().dameSemilla();
        //Generamos los valores heuristicos de los planetas e instancia el problema
		generarValoresParaHeuristica();
		generaInstaciaProblemaGlobal();
	}

    /*
     * Genera los planetas y en funcion de sus conexiones asigna los valores
     * de oxigeno y agua, para ello se recorre desde los objetivos hasta el inicial
     * asignando valores de los nodos decrementando el agua/oxigeno en funcion
     * de un aleatorio(semilla) y asi poder generar la heuristica del planeta, esto
     * es para darle un enfoque realista, si fuera una sonda con capacidad de
     * analizar las particulas de agua/oxigeno, podria saber hacia donde dirigirse.
     */
	public void generarValoresParaHeuristica(){
		//creamos la lista de 216 planetas
		for(int i=0;i<216;i++){
			_listaPlanetas.add(new Planeta(i,_listaPlanetas));
		}
		Random rnd=new Random();  
		rnd.setSeed(_semilla);	
		/*recorremos la matriz para asignar valores a los planetas conectados a un planeta final
         y despues vamos retrocediento asignando valores al resto */
		for(int i=215;i>=0;i--){
			for(int j=215;j>=0;j--){
				if(MatrizConexiones.getInstancia().conectados(j, i)){
					//j esta conectado
					Planeta conectado=_listaPlanetas.get(j);
					Planeta vecino=_listaPlanetas.get(i);
					int valor=rnd.nextInt(2);
					int agua=0;
					int oxigeno=0;
                    //obtenemos el coste del problema para decrementar tambien en funcion de la dificultad
					int problema=MatrizProblemas.getInstancia().ping(j, i);
					int valorProblema=GestorJuegos.costeProblema(problema);
                    //en funcion del aleatorio decrementamos el agua o el oxigeno
					if(valor==0){
						agua=(vecino.getAgua()-valorProblema*6);
						oxigeno=vecino.getOxigeno()-20;
					}else{
						agua=vecino.getAgua()-10;
						oxigeno=(vecino.getOxigeno()-valorProblema*3);
					}
                    //asignamos los valores
					conectado.setAguaOxigeno(agua, oxigeno);
					//reasignamos el valor de la heuristica
					conectado.generaHeuristica();
				}
			}
		}
    }

    // Genera el planeta inicial, asigna la vista y obtiene el problema
	public void generaInstaciaProblemaGlobal(){
		_planeta=new Planeta(_listaPlanetas);
        _planeta.setObserver(_observer);
		_problema=_planeta.getProblema(_listaPlanetas);
	}

    //indica si se debe parar la ejecucion al realizarse paso a paso
	public void pasoApaso(){
		_planeta.pasoApaso();
	}

    //reanuda la ejecucion si se realiza paso a paso
	public void siguiente(){
		_planeta.siguiente();
	}

    //Se asigna un algoritmo de busqueda y se da la profundidad para la Profundidad limitada
	public void solucionar(int numero,int profundidad){
		switch(numero){
			case 0://Voraz
				_search=new GreedyBestFirstSearch(new GraphSearch());
				break;
			case 1:// A*
				_search=new AStarSearch(new GraphSearch()) ;
				break;
			case 2://Primero en anchura
				_search=new BreadthFirstSearch(new TreeSearch()) ;
				break;
            case 3://Profundidad limitada
				_search=new DepthLimitedSearch(profundidad);
				break;
			case 4://Primero en profundidad
				_search=new DepthFirstSearch(new GraphSearch());
				break;
		}
	}

    //inicializa el tiempo para medir la ejecucion
    public void inicializa(){
        _t0=new GregorianCalendar();
    }

    //devuelve el tiempo transcurrido desde el inicio de la busqueda hasta que finaliza
    public String tiempoTranscurrido(){
         _t1=new GregorianCalendar();
         long diff=_t1.getTimeInMillis()-_t0.getTimeInMillis();
         Calendar diferencia=new GregorianCalendar();
         diferencia.setTimeInMillis(diff);
        return (diferencia.get(Calendar.MINUTE)+" minutos "+diferencia.get(Calendar.SECOND)+" segundos "+diferencia.get(Calendar.MILLISECOND)+" milisegundos.");
    }

    //Inicializa el thread que inicia la busqueda
	public void run(){
		try {
            //limpiamos la interfaz
            _observer.limpiarRecorrido();
            //reiniciamos las estadisticas
			Estadisticas.dameInstancia().reiniciar();
            //iniciamos el tiempo
            inicializa();
            //realizamos la busqueda con el algoritmo deseado sobre el problema elegido
			SearchAgent agent = new SearchAgent (_problema , _search) ;
            //obtenemos el tiempo transcurrido
            String tiempo=tiempoTranscurrido();
            //si se ha resuelto la busqueda (se ha encontrado solucion)
			if(_planeta.resuelto()){
                Log.dameInstancia().agregar("\n\nSOLUCION DEL PROBLEMA GLOBAL\n ");
                _observer.escribeLog("\n\nSOLUCION DEL PROBLEMA GLOBAL\n ");
				//pintamos los pasos a seguir asi como los datos del problema
                printActions(agent.getActions());
				printInstrumentation(agent.getInstrumentation());
                //pintamos la solucion en la interfaz
                pintaSolucion();
			}else{//si no se ha resuelto
                Log.dameInstancia().agregar("\n\nNO SE HA ENCONTRADO SOLUCION AL PROBLEMA GLOBAL\n ");
                _observer.escribeLog("\n\nNO SE HA ENCONTRADO SOLUCION AL PROBLEMA GLOBAL\n ");
            }
            Log.dameInstancia().agregar("Tiempo invertido en la busqueda: "+tiempo);
            _observer.escribeLog("Tiempo invertido en la busqueda: "+tiempo);
			_observer.reiniciar();
			//cerramos el fichero de log
            Log.dameInstancia().cerrarLog();
		}catch (Exception e){e.printStackTrace();}
	}

    //asignamos la vista a la que indicar que pinte las conexiones
	public void setObserver(Observador obs){
		_observer=obs;
        _planeta.setObserver(_observer);
	}

    //enviamos a la vista las conexiones entre los planetas indicando que los pinte azul(solucion)
    public void pintaSolucion(){
        for(int i=0;i<_solucion.size()-1;i++){
            //obtenemos el planeta observado y el siguiente
            int pla1=Integer.parseInt(_solucion.get(i))-1;
            int pla2=Integer.parseInt(_solucion.get(i+1))-1;
            //pintamos la conexion
            _observer.conectaPlanetas(pla1, pla2, 2);
        }
    }

    //pintamos los daots del problema que son obtenidos por el AIMA
	private static void printInstrumentation(Properties properties) {
		Iterator keys = properties.keySet().iterator();
        Log.dameInstancia().agregar("\n\nDATOS DEL PROBLEMA\n");
		_observer.escribeLog("\n\nDATOS DEL PROBLEMA\n");
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String property = properties.getProperty(key);
			Log.dameInstancia().agregar(key + " : " + property);
			_observer.escribeLog(key + " : " + property);
		}
		Log.dameInstancia().agregar("Coste total: " + _coste);
		_observer.escribeLog("Coste total: " + _coste);
	}

    //pintamos las acciones para llegar a la solucion y generamos la lista de solucion para pintarla
	private static void printActions(List<Object> actions) {
		for (int i = 0; i < actions.size(); i++) {
			String action = (String) actions.get(i);
            //obtenemos el coste de la accion
			String coste=action.substring(action.lastIndexOf("COSTE:")+6);
			int cont=0;
			String numero="";
			while(cont<coste.length() && Character.isDigit(coste.charAt(cont))){
				numero+=coste.charAt(cont);
				cont++;
			}

            //añadimos los planetas a la solucion
            int inicio=action.indexOf("planeta")+8;
            int fin=action.indexOf("al")-1;
            String planeta1=action.substring(inicio,fin);
            _solucion.add(planeta1);

            if(i == actions.size()-1){
                int inicio2=action.indexOf("vecino")+7;
                int fin2=action.lastIndexOf(",")-1;
                String planeta2=action.substring(inicio2,fin2);
                _solucion.add(planeta2);
            }

            //agregamos los datos para las estadisticas
            Estadisticas.dameInstancia().insertaValorReal(Integer.parseInt(numero.trim()));
			Estadisticas.dameInstancia().insertaValorHeuristico(_listaPlanetas.get(Integer.parseInt(planeta1)).dameValorHeuristico());

            //incrementamos el coste para obtener el total
			_coste+=Integer.parseInt(numero.trim());
            //escribimos la accion
			Log.dameInstancia().agregar(action);
			_observer.escribeLog(action);
		}
	}

    //envia la orden de que se pasen las listas a la vista y pinte las estadisticas
	public void muestraEstadisticas(){
		Estadisticas.dameInstancia().enviaValores();
	}

    //Genera una lista con los valores heuristicos de los planetas
    public ArrayList<Integer> dameValoresHeuristica(){
        ArrayList<Integer> lista=new ArrayList<Integer>();
        for(int i=0;i<216;i++){
            lista.add(_listaPlanetas.get(i).dameValorHeuristico());
        }
        return lista;
    }
}
