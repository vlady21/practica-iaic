package modelo.micromundo;

import java.util.ArrayList;

import observador.Observador;

/*
 * Tiene las listas del coste real y heuristico para hacer la comparacion
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class Estadisticas {
	private Observador _observer;
	private static Estadisticas _instancia=null;
	private ArrayList<Integer> _costeReal=new ArrayList<Integer>();
	private ArrayList<Integer> _costeHeuristico=new ArrayList<Integer>();

    //Devuelve la instancia de la clase, si no esta creada la inicializa
	public static Estadisticas dameInstancia(){
		if(_instancia==null){
			_instancia=new Estadisticas();
		}
		return _instancia;
	}

    //Limpia los valores de las listas
	public void reiniciar(){
		_costeReal.clear();
		_costeHeuristico.clear();
	}

    //Asigna el observador, sera la vista a la que pasarle las listas
	public void setObserver(Observador obs){
		_observer=obs;
	}

    //Inserta un valor en la lista de valores reales
	public void insertaValorReal(int valor){
		_costeReal.add(valor);
	}

    //Inserta un valor en la lista de valores reales
	public void insertaValorHeuristico(int valor){
		_costeHeuristico.add(valor);
	}

    /*
     * Calcula los valores y los envia a la interfaz, para ello el coste heuristico
     * se deja como esta y para el real, se suman en orden inverso, de manera
     * que el planeta inicial tenga el coste real exacto del camino a la solucion
     */
	public void enviaValores(){
		//calculamos el coste desde atras adelante para tener el coste real de los nodos al final
		ArrayList<Integer> _costeRealCalculado=new ArrayList<Integer>();
		int valor=0;
		for (int i=_costeReal.size()-1;i>=0;i--){
			valor+=_costeReal.get(i);
			_costeRealCalculado.add(valor);
		}
		//invertimos los valores para obtener la lista desde el inicial al final
		ArrayList<Integer> _costeRealCalculado2=new ArrayList<Integer>();
		for (int i=_costeRealCalculado.size()-1;i>=0;i--){
			_costeRealCalculado2.add(_costeRealCalculado.get(i));
		}
		//el heuristico no se cambia porque da el coste del nodo al final
		_observer.estadisticas(_costeRealCalculado2, _costeHeuristico);
	}
}
