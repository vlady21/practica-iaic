package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class ConejosRojiNegros  extends InterfazJuego{


	/**
	 * Posicion de la casilla libre
	 */
	private int _x;
	
	/**
	 * Tablero que representa los conejos RojiNegros
	 */
	private int [] _tablero;
	
	private int _dificultad=6;

	public ConejosRojiNegros() {
        _enunciadoProblema="Tenemos un grupo de tres conejos Rojos a la izquierda y otro grupo de tres conejos Negros a la derecha, ambos separados por un hueco y queremos conseguir que ambos grupos se posicionen de manera inversa a la original.";
		_nodosExpandidos=0;
		_resuelto=false;
    	_tablero = new int[7];

    	_tablero[0] = 'R'; _tablero[1] = 'R'; _tablero[2] = 'R';
    	_tablero[3] = ' '; _tablero[4] = 'N'; _tablero[5] = 'N';
    	_tablero[6] = 'N';
	    _x = 3;
	} 
  
	/**
	 * Genera un nodo de la posicion de los conejos RojiNegros con el estado del juego en ese momento
	 * @param tablero estado actual del problema
	 * @param x situacion del hueco
	 */
	public ConejosRojiNegros(int [] tablero, int x){
		_tablero = new int [7];
        for(int i = 0; i<=6; i++)

        	_tablero[i] = tablero[i];

	        _x = x;
	        
	}	
	

	public Problem getProblema() {
		Problem problem = new Problem(new ConejosRojiNegros(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
		return problem;		
	}

	public boolean valido(){
		return (_nodosExpandidos<5000);
	}
	
	public int dificultad(){
		return _dificultad;
	}

	/**
	 * Genera el mensaje del estado en el que nos encontramos
	 * @return String con el mensaje del estado en el que se encuentra
	 */
	public String toString(){
        String tabla = "\n\t(";
        for (int i = 0; i<=6; i++){
        
        	tabla += " " + new Character((char) _tablero[i]).toString() + " ";
        }
	
		tabla += ")" + "\n";
	
		return tabla; 
	}
	
	
	
	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			ConejosRojiNegros puzzle=(ConejosRojiNegros)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
			
			int mix = puzzle._x;
	        int[] mitablero= new int [7];

	        for(int i = 0; i<=6; i++)
                mitablero[i] = puzzle._tablero[i];
	        
	        for(int operadores = 0; operadores<=3; operadores++){
	                generado = false;
	                int newx = mix; 
	                int[] newtablero= new int [7];

	    	        for(int i = 0; i<=6; i++)
	    	        	newtablero[i] = mitablero[i];

	                switch(operadores){
		                case 0://Desplazar raton Rojo
			                if (mix>0){
			                	
			                	if(mitablero[mix-1]=='R'){
			                		
			                		newtablero[mix-1]=' ';
			                		newtablero[mix]='R';
			                		newx = mix -1;
			                		movimiento = "Desplazar conejo Rojo";
			                        generado = true;
			                        coste=1;
			                	}   
			                }
			                break;
		                case 1://Desplazar raton Negro
			                if (mix<6){
			                	
			                	if(mitablero[mix+1]=='N'){
			                		
			                		newtablero[mix+1]=' ';
			                		newtablero[mix]='N';
			                		newx = mix +1;
			                		movimiento = "Desplazar conejo Negro";
			                        generado = true;
			                        coste=1;
			                	}   
			                }
			                break;
		                case 2://Salta raton Rojo
			                if (mix>1){
			                	
			                	if(mitablero[mix-2]=='R'){
			                		
			                		newtablero[mix-2]=' ';
			                		newtablero[mix]='R';
			                		newx = mix -2;
			                		movimiento = "Saltar conejo Rojo";
			                        generado = true;
			                        coste=2;
			                	}   
			                }
			                break;
		                case 3://Salta raton Negro
			                if (mix<5){
			                	
			                	if(mitablero[mix+2]=='N'){
			                		
			                		newtablero[mix+2]=' ';
			                		newtablero[mix]='N';
			                		newx = mix +2;
			                		movimiento = "Saltar conejo Negro";
			                        generado = true;
			                        coste=2;
			                	}   
			                }
			                break;
	                }
	                
                	ConejosRojiNegros nuevoEstado = new ConejosRojiNegros(newtablero, newx);
                	
	                if (generado && nuevoEstado.valido()){
	                	String datos=movimiento+" ,COSTE:"+coste+"\n"+nuevoEstado;
	                	//System.out.println(datos);
						sucesores.add(new Successor(datos, nuevoEstado));
	                } 
			}
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			ConejosRojiNegros puzzle=(ConejosRojiNegros)state;
			if(puzzle._tablero[0] == 'N' && puzzle._tablero[1] == 'N' && puzzle._tablero[2] == 'N' &&
	 		   puzzle._tablero[3] == ' ' && puzzle._tablero[4] == 'R' && puzzle._tablero[5] == 'R' &&  
	 		   puzzle._tablero[6] == 'R')
				_resuelto=true;
			 return _resuelto;
		}
	}
	
	public class ValorHeuristico implements HeuristicFunction{
		 /**
		  * Genera la heuristica para este problema, cuenta fichas descolocadas
		  * @return devuelve la heuristica correpondiente float
		  */
		public int getHeuristicValue(Object state) {
			ConejosRojiNegros puzzle=(ConejosRojiNegros)state;

	        int valor = 0;
	        
	        if (puzzle._tablero[0] == 'R') valor++;
	        if (puzzle._tablero[1] == 'R') valor++;
	        if (puzzle._tablero[2] == 'R') valor++;
	        if (puzzle._tablero[4] == 'N') valor++;
	        if (puzzle._tablero[5] == 'N') valor++;
	        if (puzzle._tablero[6] == 'N') valor++;
	                
	        return valor;
		}
	}
} 
 

