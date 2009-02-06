package modelo.matrices;

import java.util.ArrayList;

/*
 * Matriz que muestra que planeta se conecta con otros y con cuales
 * Se trata de una matriz donde se indica mediante un 1 la conexion
 * entre dos planetas, los cuales corresponden a la fila y columna donde
 * se encuentra el identificador 1.
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class MatrizConexiones {

	private static MatrizConexiones _instancia=null;
	private static int[][] _conexiones=new int[216][216];

    //Devuelve la instancia de la clase y si no esta creada la inicializa
	public static MatrizConexiones getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizConexiones();
		}
		return _instancia;
	}

    //Realiza la conexion entre dos planetas
	public void conecta(int planeta1, int planeta2){
		_conexiones[planeta1][planeta2]=1;
	}

    //Indica si los dos planetas estan o no conectados
	public boolean conectados(int planeta1, int planeta2){
		if(_conexiones[planeta1][planeta2]==1){
			return true;
		}
		return false;
	}

    //Devuelve la lista con lo planetas a los cuales podemos ir desde el planeta deseado
	public ArrayList<Integer> damePlanetasContiguos(int planeta){
		ArrayList<Integer> planetasContiguos=new ArrayList<Integer>();
		for(int i=0;i<216;i++){
			if(_conexiones[planeta][i]==1){
				planetasContiguos.add(i);
			}
		}
		return planetasContiguos;
	}
}
