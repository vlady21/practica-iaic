package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class Puzzle8  extends InterfazJuego{


	/**
	 * Coordenada horizontal de la casilla libre
	 */
	private int _x;

	/**
	 * Coordenada vertical de la casilla libre
	 */
	private int _y;

	/**
	 * Tablero que representa el puzzle
	 */
	private int [][] _tablero;
	
	private int _dificultad=4;

    public Puzzle8() {
    		_nodosExpandidos=0;
    		_resuelto=false;
    	  	_tablero = new int[3][3];

    	  	_tablero[0][0] = 1; _tablero[0][1] = 3; _tablero[0][2] = 4;
    	  	_tablero[1][0] = 8; _tablero[1][1] = 0; _tablero[1][2] = 2;
    	  	_tablero[2][0] = 7; _tablero[2][1] = 6; _tablero[2][2] = 5;
	        _x = 1;
	        _y = 1;
	} 
  
	/**
	 * Genera un nodo de ocho puzzle con el estado del juego en ese momento
	 * @param tablero estado actual del problema
	 * @param x situacion y del blanco
	 * @param y situacion y del hueco
	 */
	public Puzzle8(int [][] tablero, int x, int y){
		_tablero = new int [3][3];
        for(int i = 0; i<=2; i++)
                for(int j = 0; j<=2; j++)
                        _tablero[i][j] = tablero[i][j];

	        _x = x;
	        _y = y;
	}	
	

	public Problem getProblema() {
		Problem problem = new Problem(new Puzzle8(), new Sucesores(), new EsFinal(), new ValorHeuristico());
		return problem;		
	}

	public boolean valido(){
		return (_nodosExpandidos<5000);
	}
	
	public boolean resuelto(){
		return _resuelto;
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
	        for (int i = 0; i<=2; i++){
	                for (int j = 0; j<=2; j++){
	                        tabla += " " + _tablero[i][j] + " ";
	                        if (j==2 && i!=2)
	                                tabla += ")" + "\n" + "\t(";
	                        if (j==2 && i == 2)
	                                tabla += ")" + "\n";
	                }
	        }
	        return tabla; 
	}
	
	
	
	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Puzzle8 puzzle=(Puzzle8)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
			
			int mix = puzzle._x;
	        int miy = puzzle._y;
	        int[][] mitablero= new int [3][3];
	        for(int i = 0; i<=2; i++)
                for(int j = 0; j<=2; j++)
                	mitablero[i][j] = puzzle._tablero[i][j];

	        
	        
	        
	        for(int operadores = 0; operadores<=3; operadores++){
	                generado = false;
	                int newx = mix; 
	    	        int newy = miy;
	    	        int[][] newtablero= new int [3][3];
	    	        for(int i = 0; i<=2; i++)
	                    for(int j = 0; j<=2; j++)
	                    	newtablero[i][j] = mitablero[i][j];

	                switch(operadores){
		                case 0://Mover hueco hacia arriba
			                if (mix>0){
			                        newx = mix - 1;
			                        movimiento = "Arriba";
			                        generado = true;
			                        coste=1;
			                }
			                break;
		                case 1://Mover hueco hacia abajo
			                if (mix<2){
			                		newx = mix + 1;;
			                        movimiento = "Abajo";     
			                        generado = true;
			                        coste=1;
			                }
			                break;
		                case 2://Mover hueco hacia la izquierda
			                if (miy>0){
			                        newy = miy - 1;
			                        movimiento = "Izquierda"; 
			                        generado = true;
			                        coste=1;
			                }
			                break;
		                case 3://Mover hueco hacia la derecha
			                if (miy<2){
			                        newy = miy + 1;
			                        movimiento = "Derecha";
			                        generado = true;
			                        coste=1;
			                }
			                break;
	                }
	                
	                if(generado){
	                	int temp = newtablero[newx][newy];
                    	newtablero[newx][newy] = 0;
                    	newtablero[mix][miy] = temp;
	                }

                	Puzzle8 nuevoEstado = new Puzzle8(newtablero, newx, newy);
                	
	                if (generado && nuevoEstado.valido()){
	                	String datos=movimiento+" ,COSTE:"+coste+"\n"+nuevoEstado;
						sucesores.add(new Successor(datos, nuevoEstado));
	                } 
			}
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Puzzle8 puzzle=(Puzzle8)state;
			if(puzzle._tablero[0][0] == 1 && puzzle._tablero[0][1] == 2 && puzzle._tablero[0][2] == 3 &&
	 		   puzzle._tablero[1][0] == 8 && puzzle._tablero[1][1] == 0 && puzzle._tablero[1][2] == 4 &&  
	 		   puzzle._tablero[2][0] == 7 && puzzle._tablero[2][1] == 6 && puzzle._tablero[2][2] == 5)
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
			Puzzle8 puzzle=(Puzzle8)state;

	        int valor = 0;
	        
	        if (puzzle._tablero[0][0] != 1) valor++;
	        if (puzzle._tablero[0][1] != 2) valor++;
	        if (puzzle._tablero[0][2] != 3) valor++;
	        if (puzzle._tablero[1][0] != 8) valor++;
	        if (puzzle._tablero[1][2] != 4) valor++;
	        if (puzzle._tablero[2][0] != 7) valor++;
	        if (puzzle._tablero[2][1] != 6) valor++;
	        if (puzzle._tablero[2][2] != 5) valor++;
	                
	        return valor;
		}
	}
} 
 

