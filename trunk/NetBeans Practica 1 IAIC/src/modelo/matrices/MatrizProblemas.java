package modelo.matrices;

/*
 * Matriz que muestra que planeta se conecta con otros y con que problema
 * Se trata de una matriz donde se indica mediante un valor correspondiente
 * al numero de problema la conexion entre los dos planetas, los cuales corresponden a la fila y columna donde
 * se encuentra el identificador.
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class MatrizProblemas {
	
	private static MatrizProblemas _instancia=null;
	private static int[][] _distancias=new int[216][216];

    //Devuelve la instancia de la clase y si no esta creada la inicializa
	public static MatrizProblemas getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizProblemas();
		}
		return _instancia;
	}

    //Realiza la conexion entre dos planetas con el valor del problema deseado
	public void conecta(int planeta1, int planeta2,int valor){
		_distancias[planeta1][planeta2]=valor;
	}

    //Devuelve el problema asignado a la conexion de los dos planetas
	public int ping(int planeta1, int planeta2){
		return _distancias[planeta1][planeta2];
	}
}
