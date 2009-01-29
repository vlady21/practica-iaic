package modelo.micromundo;

import java.util.ArrayList;

import observador.Observador;

public class Estadisticas {
	private Observador _observer;
	private static Estadisticas _instancia=null;
	private ArrayList<Integer> _costeReal=new ArrayList<Integer>();
	private ArrayList<Integer> _costeHeuristico=new ArrayList<Integer>();


	public static Estadisticas dameInstancia(){
		if(_instancia==null){
			_instancia=new Estadisticas();
		}
		return _instancia;
	}
	
	public void reiniciar(){
		_costeReal.clear();
		_costeHeuristico.clear();
	}
	
	public void setObserver(Observador obs){
		_observer=obs;
	}
	
	public void insertaValorReal(int valor){
		_costeReal.add(valor);
	}
	
	public void insertaValorHeuristico(int valor){
		_costeHeuristico.add(valor);
	}
	
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
