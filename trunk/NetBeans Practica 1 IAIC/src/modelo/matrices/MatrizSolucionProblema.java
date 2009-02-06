package modelo.matrices;

/*
 * Matriz que muestra que planeta se conecta con otros y con que algoritmo se solucion
 * el problema asignado a la conexion.
 * Se trata de una matriz donde se indica mediante un valor correspondiente
 * al numero de algoritmo para resolver el problema de la conexion entre los dos planetas,
 * los cuales corresponden a la fila y columna donde se encuentra el identificador.
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class MatrizSolucionProblema {

	private static MatrizSolucionProblema _instancia=null;
	private static int[][] _solucion=new int[216][216];

    //Devuelve la instancia de la clase y si no esta creada la inicializa
	public static MatrizSolucionProblema getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizSolucionProblema();
		}
		return _instancia;
	}

    //Realiza la conexion entre dos planetas con el valor de el algoritmo para resolver el problema
	public void conecta(int planeta1, int planeta2,int valor){
		_solucion[planeta1][planeta2]=valor;
	}

    //Devuelve el numero de algoritmo a utilizar para resolver el problema de la conexion
	public int algoritmo(int planeta1, int planeta2){
		return _solucion[planeta1][planeta2];
	}
}
