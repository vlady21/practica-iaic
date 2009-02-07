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
/*
 * Esta clase gestiona todos los juegos, de una forma sencilla se pueden
 * agregar mas juegos y mas algoritmos sin necesidad de estar cambiando
 * codigo, unicamente en el metodo indicado se agrega un case mas y
 * el gestor se encarga de utilizar y resolver los problemas.
 * Los problemas deben extender la clase InterfazJuegos y con eso el gestor
 * ya seria capaz de resolverlo.
 *
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class GestorJuegos {

	private Problem _problema;
    private String _nombreAlgoritmo;
	private static InterfazJuego _juego=new Jarras();
	private static int _coste=0;
	private static GestorJuegos _instancia=null;

    //Devuelve la instacia del gestor y si no esta creada la inicializa
	public static GestorJuegos dameInstancia(){
		if(_instancia==null){
			_instancia=new GestorJuegos();
		}
		return _instancia;
	}

    //Devuelve el coste del problema asignado
	public static int dameCosteProblema(){
		return _juego.dificultad();
	}

    //Asigna un problema para resolver
	public void asignarProblema(int numero){
		switch(numero){
			case 0://problema de las jarras
				_juego=new Jarras();
				break;
			case 1://problema de los misioneros y canibales
				_juego=new MisionerosYCanibales();
				break;
			case 2://puzzle del 8
				_juego=new Puzzle8();
				break;
			case 3://casillas rojas y azules
				_juego=new RejillaRojoAzul();
				break;
			case 4://diccionario
				_juego=new Diccionario();
				break;
			case 5://Granjero, lobo, oveja y col
				_juego=new LoboOvejaYCol();
				break;
			case 6://calculadora
				_juego=new Calculadora();
				break;
			case 7://conejos rojos y negros
				_juego=new ConejosRojiNegros();
				break;
			case 8://problema del mono y el platano
				_juego=new Mono();
				break;
			case 9://juego de los palillos
				_juego=new Palillos();
				break;
			case 10://problema del puente y los amigos con una linterna
				_juego=new Puente();
				break;
			case 11://problema del robot limpiador
				_juego=new Robot();
				break;
            case 12:// KhunPhan puzzle
				_juego=new KhunPhanPuzzle();
				break;
		}
        //obtenemos la isntancia del problema y la asignamos
		_problema=_juego.getProblema();
	}

    //Asigna una busqueda para resolver el problema
	@SuppressWarnings("unchecked")
	public boolean solucionar(int numero){
		Search search=null;
		switch(numero){
			case 0://Voraz
				search=new GreedyBestFirstSearch(new GraphSearch());
                _nombreAlgoritmo="Busqueda Voraz";
				break;
			case 1:// A*
				search=new AStarSearch(new GraphSearch()) ;
                _nombreAlgoritmo="Algoritmo A*";
				break;
			case 2://Primero en anchura
				search=new BreadthFirstSearch(new TreeSearch()) ;
                _nombreAlgoritmo="Primero en anchura";
				break;
			case 3://Profundidad limitada de maxima prof 7
				search=new DepthLimitedSearch(7);
                _nombreAlgoritmo="Profundidad limitada a 7";
				break;
			case 4://Profundidad iterativa
				search=new IterativeDeepeningSearch();
                _nombreAlgoritmo="Profundidad iterativa";
				break;
			case 5://Simulada
				search=new SimulatedAnnealingSearch();
                _nombreAlgoritmo="Simulada";
				break;
			case 6://Escalada simple
				search=new HillClimbingSearch();
                _nombreAlgoritmo="Escalada simple";
				break;
			case 7://Primero en profundidad
				search=new DepthFirstSearch(new GraphSearch());
                _nombreAlgoritmo="Primero en profundidad";
				break;
		}
		try {
            //resolvemos el problema con la busqueda deseada
			SearchAgent agent = new SearchAgent (_problema , search) ;
            //si se ha resuelto devolvemos true, sino devolvemos false
			if(_juego.resuelto()){
				return true;
			}
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    //devuelve el coste de un determinado problema
	public static int costeProblema(int numero){
		switch(numero){
		case 0://problema de las jarras
			return new Jarras().dificultad();
		case 1://problema de los misioneros y canibales
			return new MisionerosYCanibales().dificultad();
		case 2://puzzle del 8
			return new Puzzle8().dificultad();
		case 3://casillas rojas y azules
			return new RejillaRojoAzul().dificultad();
		case 4://diccionario
			return new Diccionario().dificultad();
		case 5://Granjero, lobo, oveja y col
			return new LoboOvejaYCol().dificultad();
		case 6://calculadora
			return new Calculadora().dificultad();
		case 7://conejos rojos y negros
			return new ConejosRojiNegros().dificultad();
		case 8://problema del mono y el platano
			return new Mono().dificultad();
		case 9://juego de los palillos
			return new Palillos().dificultad();
		case 10://problema del puente y los amigos con una linterna
			return new Puente().dificultad();
		case 11://problema del robot limpiador
			return new Robot().dificultad();
        case 12:// KhunPhan puzzle
            return new KhunPhanPuzzle().dificultad();
		}
		return 0;
	}

    //Devuelve el enunciado del problema asignado para resolver
    public String dameEnunciadoProblema(){
        return _juego.dameEnunciadoProblema();
    }

    //Devuelve el nombre del algoritmo utilizado en la resolucion del problema
    public String dameNombreAlgoritmo(){
        return _nombreAlgoritmo;
    }
}
