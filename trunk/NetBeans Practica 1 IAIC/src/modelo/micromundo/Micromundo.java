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

@SuppressWarnings("unchecked")
public class Micromundo extends Thread {
	private static Observador _observer;
	private Problem _problema=null;
	private Search _search=null;
	private InterfazPlaneta _planeta=null;
	private static int _coste=0;
	private int _semilla=0;
	private static ArrayList<Planeta> _listaPlanetas=new ArrayList<Planeta>();
	private static ArrayList<String> _solucion=new ArrayList<String>();
    private Calendar _t0,_t1;

	public Micromundo(){
		_coste=0;
		_listaPlanetas=new ArrayList<Planeta>();
        _solucion=new ArrayList<String>();
		_semilla=GeneraMatrices.dameInstancia().dameSemilla();
		generarValoresParaHeuristica();
		generaInstaciaProblemaGlobal();
	}
	
	public void generarValoresParaHeuristica(){
		//creamos la lista de 216 planetas
		for(int i=0;i<216;i++){
			_listaPlanetas.add(new Planeta(i,_listaPlanetas));
		}
		Random rnd=new Random();  
		rnd.setSeed(_semilla);	
		//recorremos la matriz para asignar valores a los planetas conectados a un planeta final
		for(int i=215;i>=0;i--){
			for(int j=215;j>=0;j--){
				if(MatrizConexiones.getInstancia().conectados(j, i)){
					//j esta conectado
					Planeta conectado=_listaPlanetas.get(j);
					Planeta vecino=_listaPlanetas.get(i);
					int valor=rnd.nextInt(2);
					int agua=0;
					int oxigeno=0;
					int problema=MatrizProblemas.getInstancia().ping(j, i);
					int valorProblema=GestorJuegos.costeProblema(problema);
					if(valor==0){
						agua=(vecino.getAgua()-valorProblema*6);
						oxigeno=vecino.getOxigeno()-20;
					}else{
						agua=vecino.getAgua()-10;
						oxigeno=(vecino.getOxigeno()-valorProblema*3);
					}
					conectado.setAguaOxigeno(agua, oxigeno);
					//reasignamos el valor de la heuristica
					conectado.generaHeuristica();
				}
			}
		}
    }
	
	public void generaInstaciaProblemaGlobal(){
		_planeta=new Planeta(_listaPlanetas);
        _planeta.setObserver(_observer);
		_problema=_planeta.getProblema(_listaPlanetas);
	}
	
	public void pasoApaso(){
		_planeta.pasoApaso();
	}
	
	public void siguiente(){
		_planeta.siguiente();
	}

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
            case 3://Voraz
				_search=new DepthLimitedSearch(profundidad);
				break;
			case 4://Primero en anchura
				_search=new DepthFirstSearch(new GraphSearch());
				break;
		}
	}

    public void inicializa(){
        _t0=new GregorianCalendar();
    }

    public String tiempoTranscurrido(){
         _t1=new GregorianCalendar();
         long diff=_t1.getTimeInMillis()-_t0.getTimeInMillis();
         Calendar diferencia=new GregorianCalendar();
         diferencia.setTimeInMillis(diff);
        return (diferencia.get(Calendar.MINUTE)+" minutos "+diferencia.get(Calendar.SECOND)+" segundos "+diferencia.get(Calendar.MILLISECOND)+" milisegundos.");
    }

	public void run(){
		try {
            _observer.limpiarRecorrido();
			Estadisticas.dameInstancia().reiniciar();
            inicializa();
			SearchAgent agent = new SearchAgent (_problema , _search) ;
            String tiempo=tiempoTranscurrido();
			if(_planeta.resuelto()){
                Log.dameInstancia().agregar("\n\nSOLUCION DEL PROBLEMA GLOBAL\n ");
                _observer.escribeLog("\n\nSOLUCION DEL PROBLEMA GLOBAL\n ");
				printActions(agent.getActions());
				printInstrumentation(agent.getInstrumentation());
                pintaSolucion();
			}else{
                Log.dameInstancia().agregar("\n\nNO SE HA ENCONTRADO SOLUCION AL PROBLEMA GLOBAL\n ");
                _observer.escribeLog("\n\nNO SE HA ENCONTRADO SOLUCION AL PROBLEMA GLOBAL\n ");
            }
            Log.dameInstancia().agregar("Tiempo invertido en la busqueda: "+tiempo);
                _observer.escribeLog("Tiempo invertido en la busqueda: "+tiempo);
			_observer.reiniciar();
			Log.dameInstancia().cerrarLog();
		}catch (Exception e){e.printStackTrace();}
	}
	
	public void setObserver(Observador obs){
		_observer=obs;
        _planeta.setObserver(_observer);
	}

    public void pintaSolucion(){
        for(int i=0;i<_solucion.size()-1;i++){
            int pla1=Integer.parseInt(_solucion.get(i))-1;
            int pla2=Integer.parseInt(_solucion.get(i+1))-1;
            _observer.conectaPlanetas(pla1, pla2, 2);
        }
    }
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
		
	private static void printActions(List<Object> actions) {
		for (int i = 0; i < actions.size(); i++) {
			String action = (String) actions.get(i);
			String coste=action.substring(action.lastIndexOf("COSTE:")+6);
			int cont=0;
			String numero="";
			while(cont<coste.length() && Character.isDigit(coste.charAt(cont))){
				numero+=coste.charAt(cont);
				cont++;
			}

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
            Estadisticas.dameInstancia().insertaValorReal(Integer.parseInt(numero.trim()));
			Estadisticas.dameInstancia().insertaValorHeuristico(_listaPlanetas.get(Integer.parseInt(planeta1)).dameValorHeuristico());

			_coste+=Integer.parseInt(numero.trim());
			Log.dameInstancia().agregar(action);
			_observer.escribeLog(action);
		}
	}

	public void muestraEstadisticas(){
		Estadisticas.dameInstancia().enviaValores();
	}

    public ArrayList<Integer> dameValoresHeuristica(){
        ArrayList<Integer> lista=new ArrayList<Integer>();
        for(int i=0;i<216;i++){
            lista.add(_listaPlanetas.get(i).dameValorHeuristico());
        }
        return lista;
    }
}
