package modelo.juegos;

import java.util.ArrayList;
import java.util.List;
import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class RejillaRojoAzul extends InterfazJuego{


	/**
	 * Tablero que representa el puzzle
	 */
	private String [][] _tablero;
	
	private int _dificultad=7;

    public RejillaRojoAzul() {
        _enunciadoProblema="Tenemos un tablero con las casillas pintadas de color rojo o azul y queremos que el numero de pares del mismo color sea minimo.";
    	_nodosExpandidos=0;
    	_resuelto=false;
   	 	_tablero = new String[3][3];
   	  	_tablero[0][0] = "R"; _tablero[0][1] = "R"; _tablero[0][2] = "R";
   	  	_tablero[1][0] = "A"; _tablero[1][1] = "R"; _tablero[1][2] = "R";
   	  	_tablero[2][0] = "A"; _tablero[2][1] = "A"; _tablero[2][2] = "A";
	} 
  
	/**
	 * Genera un nodo de ocho puzzle con el estado del juego en ese momento
	 * @param tablero estado actual del problema
	 * @param x situacion y del blanco
	 * @param y situacion y del hueco
	 */
	public RejillaRojoAzul(String [][] tablero){
		_tablero = new String [3][3];
        for(int i = 0; i<=2; i++)
                for(int j = 0; j<=2; j++)
                        _tablero[i][j] = tablero[i][j];
	}	
	

	public Problem getProblema() {
		Problem problem = new Problem(new RejillaRojoAzul(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
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
			RejillaRojoAzul puzzle=(RejillaRojoAzul)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
			int x=0,y=0;
	        String[][] mitablero= new String [3][3];
	        for(int i = 0; i<=2; i++)
                for(int j = 0; j<=2; j++)
                	mitablero[i][j] = puzzle._tablero[i][j];
	        
	        
	        for(int operadores = 0; operadores<=8; operadores++){
	                generado = false;
	    	        String[][] newtablero= new String [3][3];
	    	        for(int i = 0; i<=2; i++)
	                    for(int j = 0; j<=2; j++)
	                    	newtablero[i][j] = mitablero[i][j];

	                switch(operadores){
		                case 0://Cambiar la primera casilla 00
			                x=0;
			                y=0;
			                movimiento = "Cambiar la primera casilla [0,0]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 1://Cambiar la segunda casilla 01
			                x=0;
			                y=1;
			                movimiento = "Cambiar la segunda casilla [0,1]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 2://Cambiar la tercera casilla 02
			                x=0;
			                y=2;
			                movimiento = "Cambiar la tercera casilla [0,2]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 3://Cambiar la cuarta casilla 10
			                x=1;
			                y=0;
			                movimiento = "Cambiar la cuarta casilla [1,0]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 4://Cambiar la quinta casilla 11
			                x=1;
			                y=1;
			                movimiento = "Cambiar la quinta casilla [1,1]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 5://Cambiar la sexta casilla 12
			                x=1;
			                y=2;
			                movimiento = "Cambiar la sexta casilla [1,2]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 6://Cambiar la septima casilla 20
			                x=2;
			                y=0;
			                movimiento = "Cambiar la septima casilla [2,0]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 7://Cambiar la octava casilla 21
			                x=2;
			                y=1;
			                movimiento = "Cambiar la octava casilla [2,1]";
			                generado = true;
			                coste=1;
			                break;
			                
		                case 8://Cambiar la novena casilla 22
			                x=2;
			                y=2;
			                movimiento = "Cambiar la novena casilla [2,2]";
			                generado = true;
			                coste=1;
			                break;
	                }

	                if(generado){
	                	if(newtablero[x][y].compareTo("R")==0){
	                		newtablero[x][y]="A";
	                	}else{
	                		newtablero[x][y]="R";
	                	}
	                }

	                RejillaRojoAzul nuevoEstado = new RejillaRojoAzul(newtablero);
                	
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
			RejillaRojoAzul puzzle=(RejillaRojoAzul)state;
			if((puzzle._tablero[0][0] == "A" && puzzle._tablero[0][1] == "R" && puzzle._tablero[0][2] == "A" &&
	 		    puzzle._tablero[1][0] == "R" && puzzle._tablero[1][1] == "A" && puzzle._tablero[1][2] == "R" &&  
	 		    puzzle._tablero[2][0] == "A" && puzzle._tablero[2][1] == "R" && puzzle._tablero[2][2] == "A") 
	 		   ||
	 		  (puzzle._tablero[0][0] == "R" && puzzle._tablero[0][1] == "A" && puzzle._tablero[0][2] == "R" &&
	 		   puzzle._tablero[1][0] == "A" && puzzle._tablero[1][1] == "R" && puzzle._tablero[1][2] == "A" &&  
	 		   puzzle._tablero[2][0] == "R" && puzzle._tablero[2][1] == "A" && puzzle._tablero[2][2] == "R"))
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
			RejillaRojoAzul puzzle=(RejillaRojoAzul)state;

	        int valorHorizontal = 0;
	        int valorVertical = 0;
	        
	        for(int i =0;i<=2;i++){
	        	for(int j =0;j<=1;j++){
		        	if(puzzle._tablero[i][j]==puzzle._tablero[i][j+1]){
		        		valorHorizontal++;
		        	}
		        }
	        }
	        
	        for(int i =0;i<=2;i++){
	        	for(int j =0;j<=1;j++){
		        	if(puzzle._tablero[j][i]==puzzle._tablero[j+1][i]){
		        		valorVertical++;
		        	}
		        }
	        }
	        return (valorHorizontal+valorVertical);
		}
	}

}
