package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;


	/**
	 * Se trata de un juego para dos jugadores.
	 * Se parte de 23 palillos.
	 * Cada jugador, por turno, puede retirar uno, dos o tres palillos.
	 * Pierde el que retira el ultimo.
	 *
	 */

public class Palillos  extends InterfazJuego{
	/**
	 * Numero de palillos que quedan, es decir, que no ha cogido ningun jugador
	 */
	private int _palillos;
	
	/**
	 * Numero de turno, empieza el rival en el turno 1 
	 */
	private int _turno;
    
    private int _dificultad=1;

    public Palillos() {
        _enunciadoProblema="Palillos";
	    _palillos = 23;
	    _turno = 1;
	    _nodosExpandidos=0;
		_resuelto=false;
	} 
    
    /**
	 * Crea una instancia del estado actual del problema de los palillos
	 * @param palillos Numero de palillos que quedan
	 * @param turno Numero de turno
     */
	public Palillos(int palillos, int turno) {
	    this._palillos = palillos;
	    this._turno = turno;
	} 
	
	

	public Problem getProblema() {
		Problem problem = new Problem(new Palillos(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
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
			Palillos palillos=(Palillos)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
		    int newpalillos=0, newturno=0;
		    
		    for (int i=1; i<=3; i++) {
		    	newpalillos = palillos._palillos;
		    	newturno = palillos._turno;
		    	generado=false;
				 if (palillos._palillos-i >= 0) {
					 newpalillos = palillos._palillos-i;
					 newturno = palillos._turno+1;
					 generado = true;
					 coste = 1;
					 movimiento = "Se quitan " + i + " palillos";
			      }
				 
				 Palillos nuevoEstado=new Palillos(newpalillos, newturno);
					if (generado && nuevoEstado.valido()){
						String datos=movimiento+" ,COSTE:"+coste;
						sucesores.add(new Successor(datos, nuevoEstado));
					}
		    }
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Palillos palillos=(Palillos)state;
			if(palillos._palillos == 0 && (palillos._turno%2 == 0)){
				_resuelto=true;
			}
			return _resuelto;
		}
	}
	
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Palillos palillos =(Palillos)state;
			return (palillos._palillos+palillos._turno);
		}
	}
}
