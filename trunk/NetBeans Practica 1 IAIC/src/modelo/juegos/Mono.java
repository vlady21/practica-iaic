package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;


	/** <b>Juego del Mono:</b><br>
	 * Hay un mono en la puerta de una habitaci�n. En el centro de la habitaci�n hay un
	 * pl�tano colgado del techo. El mono est� hambriento y quiere conseguir el pl�tano
	 * pero no alcanza porque est� muy alto. En la habitaci�n tambi�n hay una ventana y
	 * debajo de ella hay una caja que le permitir�a alcanzar el pl�tano si se subiera a ella.
	 * El mono puede realizar las siguientes acciones: andar por el suelo, subirse a la caja,
	 * empujar la caja (si el mono est� en la misma posici�n que la caja) y coger el pl�tano
	 * (si est� subido encima de la caja y la caja est� justo debajo del pl�tano).<br>
	 * 0: el mono/caja se encuentran en la puerta<br>
	 * 1: el mono/caja se encuentran en la centro<br>
	 * 2: el mono/caja se encuentran en el ventana
	 */

public class Mono  extends InterfazJuego{
	/**
	 * Posicion del mono
	 */
	private int _pos;
	
	/**
	 * Posicion de la caja
	 */
	private int _caja;
	
	/**
	 * Si esta sobre la caja
	 */
	private boolean _sobreCaja;
	
	/**
	 * Si ha cogido el platano
	 */
	private boolean _platano;
    
    private int _dificultad=1;

    
	public Mono() {
        _enunciadoProblema="Hay un mono en la puerta de una habitacion y quiere coger el platano que esta colgado del techo en el centro, para ello tiene que subirse a una caja que esta en la ventana por lo que tiene que mover la caja y posicionarse debajo.";
	    _pos = 0;
	    _caja = 2;
	    _sobreCaja = false;
	    _platano = false;
	    _nodosExpandidos=0;
		_resuelto=false;
	}
	
	public Mono(int pos, int caja, boolean sobreCaja, boolean platano) {
		this._pos = pos;
		this._caja = caja;
		this._sobreCaja = sobreCaja;
		this._platano = platano;
	}
	

	public Problem getProblema() {
		Problem problem = new Problem(new Mono(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
		return problem;		
	}

	public boolean valido(){
		boolean aux=true;
		if(_nodosExpandidos>5000){
			aux=false;
		}
		return aux;
	}
	
	public boolean resuelto(){
		return _resuelto;
	}
	
	public int dificultad(){
		return _dificultad;
	}
	
	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Mono mono=(Mono)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
		    int newpos=0, newcaja=0;
		    boolean newsobreCaja=false, newplatano=false;
		    
		    newpos = mono._pos;
		    newcaja = mono._caja;
		    newsobreCaja = mono._sobreCaja;
		    newplatano = mono._platano;

		    if (mono._pos != 2 && !mono._sobreCaja){
		    	movimiento = "andaHaciaVentana";
		    	newpos = mono._pos+1;
		    	generado = true;
				coste = 1;
			}
		    
		    else if (mono._pos == mono._caja && !mono._sobreCaja){
		 		movimiento ="subeCaja";
		 		newsobreCaja = true;
		 		generado = true;
				coste = 1;
			}
		    
		    else if (mono._sobreCaja && mono._caja == 1 && !mono._platano){
		 		movimiento ="cogePlatano";
		 		newplatano = true;
		 		generado = true;
				coste = 1;
			}


		    else if (mono._pos == mono._caja && mono._sobreCaja && mono._pos !=0){
		 		movimiento = "empujaCajaHaciaPuerta";
		 		newpos = mono._pos-1;
		 		newcaja = mono._caja-1;
		 		generado = true;
				coste = 1;
			}
		 	
		    else if (mono._pos == mono._caja && mono._sobreCaja){
		 		movimiento ="bajaCaja";
		 		newsobreCaja = false;
		 		generado = true;
				coste = 1;
			}
		    
		    else if (mono._pos != 0 && !mono._sobreCaja){
		 		movimiento = "andaHaciaPuerta";
		 		newpos = mono._pos-1;
		 		generado = true;
				coste = 1;
			}
		    
		    else if (mono._pos == mono._caja && mono._sobreCaja && mono._pos != 2){
		 		movimiento = "empujaCajaHaciaVentana";
		 		newpos = mono._pos+1;
		 		newcaja = mono._caja+1;
		 		generado = true;
				coste = 1;
			}
	 
		 	Mono nuevoEstado=new Mono(newpos, newcaja, newsobreCaja, newplatano);
			if (generado && nuevoEstado.valido()){
				String datos=movimiento+" ,COSTE:"+coste;
				sucesores.add(new Successor(datos, nuevoEstado));
			}
			
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Mono mono=(Mono)state;
			if(mono._platano == true){
				_resuelto=true;
			}
			return _resuelto;
		}
	}

	/* Para cada condici�n que no se cumpla para el objetivo, se suma uno. 
	Por ejemplo, si el mono no esta encima de la caja, en el centro de la 
	habitaci�n, la caja no esta en el centro y el mono no tiene el pl�tano, la 
	heur�stica vale 4.*/

	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Mono mono =(Mono)state;
			if (mono._pos == 0) return 5;
			if (mono._pos == 2 && mono._caja != 2) return 5;
			if (mono._pos == 1 && mono._caja != 1) return 4;
			if (mono._pos == 2 && mono._caja == 2 && !mono._sobreCaja) return 3;
			if (mono._pos == 2 && mono._caja == 2 && mono._sobreCaja) return 2;
			if (mono._pos == 1 && mono._sobreCaja && !mono._platano) return 1;
			if (mono._pos == 1 && mono._sobreCaja && mono._platano) return 0;
			return 6;
		}
	}
}
