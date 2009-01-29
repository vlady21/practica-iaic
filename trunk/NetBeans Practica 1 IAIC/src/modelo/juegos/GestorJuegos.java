package modelo.juegos;

import java.util.List;
import aima.search.framework.GraphSearch;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.framework.TreeSearch;
import aima.search.informed.AStarSearch;
import aima.search.informed.GreedyBestFirstSearch;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import aima.search.uninformed.IterativeDeepeningSearch;
import aima.search.uninformed.BreadthFirstSearch;
import aima.search.uninformed.DepthLimitedSearch;
import aima.search.uninformed.DepthFirstSearch;

public class GestorJuegos {

	private Problem _problema;
	private static InterfazJuego _juego;
	private static int _coste=0;
	private static GestorJuegos _instancia=null;
	
	public static GestorJuegos dameInstancia(){
		if(_instancia==null){
			_instancia=new GestorJuegos();
		}
		return _instancia;
	}
	
	public static int dameCosteProblema(){
		return getJuego().dificultad();
	}
	
	public void asignarProblema(int numero){
		switch(numero){
			case 0://problema de las jarras
				setJuego(new Jarras());
				break;
			case 1://problema de los misioneros y canibales
				setJuego(new MisionerosYCanibales());
				break;
			case 2:
				setJuego(new Puzzle8());
				break;
			case 3:
				setJuego(new RejillaRojoAzul());
				break;
			case 4:
				setJuego(new Diccionario());
				break;
			case 5:
				setJuego(new LoboOvejaYCol());
				break;
			case 6:
				setJuego(new Calculadora());
				break;
			case 7:
				setJuego(new ConejosRojiNegros());
				break;
			case 8:
				setJuego(new RejillaRojoAzul());
				break;
			case 9:
				setJuego(new Palillos());
				break;
			case 10:
				setJuego(new RejillaRojoAzul());
				break;
			case 11:
				setJuego(new RejillaRojoAzul());
				break;
		}
		_problema=getJuego().getProblema();
	}
	
	@SuppressWarnings("unchecked")
	public boolean solucionar(int numero){
		Search search=null;
		switch(numero){
			case 0://Voraz
				search=new GreedyBestFirstSearch(new GraphSearch());
				break;
			case 1:// A*
				search=new AStarSearch(new GraphSearch()) ;
				break;
			case 2://Primero en anchura
				search=new BreadthFirstSearch(new TreeSearch()) ;
				break;
			case 3://Profundidad limitada de maxima prof 7
				search=new DepthLimitedSearch(7);
				break;
			case 4://Profundidad iterativa
				search=new IterativeDeepeningSearch();
				break;
			case 5://Simulada
				search=new SimulatedAnnealingSearch();
				break;
			case 6://Escalada simple
				search=new HillClimbingSearch();
				break;
			case 7://Primero en profundidad
				search=new DepthFirstSearch(new GraphSearch());
				break;
		}
		try {
			SearchAgent agent = new SearchAgent (_problema , search) ;
			if(getJuego().resuelto()){
				printActions(agent.getActions());
				/*printInstrumentation(agent.getInstrumentation());*/
				
				return true;
			}
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void setJuego(InterfazJuego _juego) {
		GestorJuegos._juego = _juego;
	}

	public static InterfazJuego getJuego() {
		return _juego;
	}
	
	public static int costeProblema(int numero){
		switch(numero){
		case 0://problema de las jarras
			return new Jarras().dificultad();
		case 1://problema de los misioneros y canibales
			return new MisionerosYCanibales().dificultad();
		case 2:
			return new Puzzle8().dificultad();
		case 3:
			return new RejillaRojoAzul().dificultad();
		case 4:
			return new Diccionario().dificultad();
		case 5:
			return new LoboOvejaYCol().dificultad();
		case 6:
			return new Calculadora().dificultad();
		case 7:
			return new ConejosRojiNegros().dificultad();
		case 8:
			return new Palillos().dificultad();
		case 9:
			return new RejillaRojoAzul().dificultad();
		case 10:
			return new RejillaRojoAzul().dificultad();
		case 11:
			return new RejillaRojoAzul().dificultad();
		}
		return 0;
	}
	
	/* son datos del problema no la solucion
	 
	@SuppressWarnings("unchecked")
	private static void printInstrumentation(Properties properties) {
		Iterator keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String property = properties.getProperty(key);
			System.out.println(key + " : " + property);
		}
		System.out.println("Coste total: " + _coste);
	}
	*/
	
	private static void printActions(List<Object> actions) {
		for (int i = 0; i < actions.size(); i++) {
			String action = (String) actions.get(i);
			String coste=action.substring(action.lastIndexOf("COSTE:")+6);
			String numero="";
			int cont=0;
			while(cont<coste.length()){
				if(Character.isDigit(coste.charAt(cont))){
					numero+=coste.charAt(cont);
					cont++;
				}else{
					break;
				}
			}
			if(numero.length()>0){
				_coste+=Integer.parseInt(numero.trim());
			}
			//acciones de los minijuegos
			System.out.println(action);
		}
	}
}
