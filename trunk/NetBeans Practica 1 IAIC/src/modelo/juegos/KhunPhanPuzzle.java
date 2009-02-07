package modelo.juegos;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class KhunPhanPuzzle  extends InterfazJuego{


	/**
	 * Coordenada horizontal de las casilla libre
	 */
	private int _x1;
	private int _x2;
	private Vector <String [][]> recorrido;

	/**
	 * Coordenada vertical de la casilla libre
	 */
	private int _y1;
	private int _y2;

	/**
	 * Tablero que representa el puzzle
	 */
	private String [][] _tablero;
	
	private int _dificultad=18;

    public KhunPhanPuzzle() {
            _enunciadoProblema="Puzzle donde tienes que conseguir sacar la figura cuadrada del laberinto. Su salida esta en la posicion inferior central del puzzle.";
        	_nodosExpandidos=0;
            _resuelto=false;
    	  	_tablero = new String[4][5];

    	  	_tablero[0][0] = "V"; _tablero[1][0] = "C"; _tablero[2][0] = "C"; _tablero[3][0] = "V";
    	  	_tablero[0][1] = "V"; _tablero[1][1] = "C"; _tablero[2][1] = "C"; _tablero[3][1] = "V";
    	  	_tablero[0][2] = " "; _tablero[1][2] = "H"; _tablero[2][2] = "H"; _tablero[3][2] = " ";
    	  	_tablero[0][3] = "V"; _tablero[1][3] = "S"; _tablero[2][3] = "S"; _tablero[3][3] = "V";
    	  	_tablero[0][4] = "V"; _tablero[1][4] = "S"; _tablero[2][4] = "S"; _tablero[3][4] = "V";
    	  	_x1 = 0; _y1 = 2;
    	  	_x2 = 3; _y2 = 2;
    	  	
    	  	recorrido = new Vector();
    	  	
    	  	recorrido.add(_tablero);
	} 
  
	/**
	 * Genera un nodo de Khun Phan puzzle con el estado del juego en ese momento
	 * @param tablero estado actual del problema
	 * @param x1 situacion x del hueco1
	 * @param y1 situacion y del hueco1
	 * @param x2 situacion x del hueco2
	 * @param y2 situacion y del hueco2
	 */
	public KhunPhanPuzzle(String [][] tablero, int x1, int y1, int x2, int y2, Vector <String [][]> recorrido){
		_tablero = new String [4][5];
        for(int i = 0; i<=3; i++)
                for(int j = 0; j<=4; j++)
                        _tablero[i][j] = tablero[i][j];

        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        
        recorrido.add(_tablero);
        
        this.recorrido = recorrido;
	        
	}	
	

	public Problem getProblema() {
		Problem problem = new Problem(new KhunPhanPuzzle(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
		return problem;		
	}

	public boolean valido(String [][] _newTablero){
		boolean result = true;
		if(_nodosExpandidos>5000||contiene(_newTablero))
			result = false;
		
		return result;
	}
	
	private boolean contiene(String[][] tablero) {

		int i,j,k,iguales;
		String[][] t;

		for(i = 0;i<recorrido.size();i++){
		
			 t = recorrido.elementAt(i);
			 iguales = 0;
			 
			 for(j = 0; j<=3; j++){
	                for(k = 0; k<=4; k++){
	                        if(t[j][k] == tablero[j][k])
	                        	iguales++;
	                }
			 }
			 if(iguales==20){
				 return true;
			 }
		}
		return false;
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
	        for (int j = 0; j<=4; j++){
	        	for (int i = 0; i<=3; i++){
	                        tabla += " " + _tablero[i][j] + " ";
	                        if (i==3 && j!=4)
	                                tabla += ")" + "\n" + "\t(";
	                        if (i==3 && j ==4)
	                                tabla += ")" + "\n";
	                }
	        }
	        return tabla; 
	}
	
	
	
	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			KhunPhanPuzzle puzzle=(KhunPhanPuzzle)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
			
			int mix1 = puzzle._x1;
	        int miy1 = puzzle._y1;
	        int mix2 = puzzle._x2;
	        int miy2 = puzzle._y2;
	        String[][] mitablero= new String [4][5];
	        for(int i = 0; i<=3; i++)
                for(int j = 0; j<=4; j++)
                	mitablero[i][j] = puzzle._tablero[i][j];
	        
	        for(int operadores = 0; operadores<=23; operadores++){
	                generado = false;
	                int newx1 = mix1; 
	    	        int newy1 = miy1;
	    	        int newx2 = mix2; 
	    	        int newy2 = miy2;
	    	        String[][] newtablero= new String [4][5];
	    	        for(int i = 0; i<=3; i++)
	                    for(int j = 0; j<=4; j++)
	                    	newtablero[i][j] = mitablero[i][j];

	                switch(operadores){
	                
		                case 0://Mover cuadrado grande a la derecha 
		                	if (mix1>1 && mix2>1){
			                	if(mitablero[mix1-1][miy1].equals("C") && mitablero[mix2-1][miy2].equals("C")){
			                		
			                		newtablero[mix1-2][miy1] = " ";
			                		newtablero[mix2-2][miy2] = " ";
			                		newtablero[mix1][miy1] = "C";
			                		newtablero[mix2][miy2] = "C";
			                		newx1 = mix1-2;
			                		newy1 = miy1;
			                		newx2 = mix2-2;
			                		newy2 = miy2;
			                        movimiento = "Mover cuadrado grande a la derecha";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 1://Mover cuadrado grande a la izquierda 
			            	if (mix1<2 && mix2<2){
			                	if(mitablero[mix1+1][miy1].equals("C") && mitablero[mix2+1][miy2].equals("C")){
			                		
			                		newtablero[mix1+2][miy1] = " ";
			                		newtablero[mix2+2][miy2] = " ";
			                		newtablero[mix1][miy1] = "C";
			                		newtablero[mix2][miy2] = "C";
			                		newx1 = mix1+2;
			                		newy1 = miy1;
			                		newx2 = mix2+2;
			                		newy2 = miy2;
			                        movimiento = "Mover cuadrado grande a la izquierda";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 2://Desplazar vertical a la derecha
			                if (mix1>0 && mix2>0){
			                	if(mitablero[mix1-1][miy1].equals("V") && mitablero[mix2-1][miy2].equals("V") && mix1==mix2 && (miy1-miy2==-1||miy1-miy2==1)){
			                		
			                		generado = true;
			                		
			                		if(miy1>0){
				                		if(miy1<miy2&&mitablero[mix1-1][miy1-1].equals("V")){
				                			generado = false;
				                		}else{
				                			if(miy2>0){
					                			if(miy1>miy2&&mitablero[mix2-1][miy2-1].equals("V")){
						                			generado = false;
						                		}
				                			}
				                		}
			                		}
				                		
			                		if(generado){
				                		newtablero[mix1-1][miy1]=" ";
				                		newtablero[mix2-1][miy2]=" ";
				                		newtablero[mix1][miy1]="V";
				                		newtablero[mix2][miy2]="V";
				                		newx1 = mix1-1;
				                		newy1 = miy1;
				                		newx2 = mix2-1;
				                		newy2 = miy2;
				                        movimiento = "//Desplazar vertical a la derecha";
				                        generado = true;
				                        coste=1;
			                		}
			                	}
			                }
			                break;
		                case 3://Desplazar vertical a la izquierda
			                if (mix1<3 && mix2<3){
			                	if(mitablero[mix1+1][miy1].equals("V") && mitablero[mix2+1][miy2].equals("V")  && mix1==mix2 && (miy1-miy2==-1||miy1-miy2==1)){
			                		
			                		generado = true;
			                		
			                		if(miy1>0){
				                		if(miy1<miy2&&mitablero[mix1+1][miy1-1].equals("V")){
				                			generado = false;
				                		}else{
				                			if(miy2>0){
					                			if(miy1>miy2&&mitablero[mix2+1][miy2-1].equals("V")){
						                			generado = false;
						                		}
				                			}
				                		}
			                		}
				                		
			                		if(generado){
				                		newtablero[mix1+1][miy1]=" ";
				                		newtablero[mix2+1][miy2]=" ";
				                		newtablero[mix1][miy1]="V";
				                		newtablero[mix2][miy2]="V";
				                		newx1 = mix1+1;
				                		newy1 = miy1;
				                		newx2 = mix2+1;
				                		newy2 = miy2;
				                        movimiento = "//Desplazar vertical a la izquierda";
				                        generado = true;
				                        coste=1;
			                		}
			                	}
			                }
			                break;
		                case 4://Subir vertical a hueco 1
			                if (miy1<3){
			                	if(mitablero[mix1][miy1+1].equals("V")){
			                		
			                		newtablero[mix1][miy1+2]=" ";
			                		newtablero[mix1][miy1]="V";
			                		newx1 = mix1;
			                		newy1 = miy1 + 2;
			                        movimiento = "Subir barra vertical a hueco 1";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 5://Subir vertical a hueco 2
			                if (miy2<3){
			                	if(mitablero[mix2][miy2+1].equals("V")){
			                		
			                		newtablero[mix2][miy2+2]=" ";
			                		newtablero[mix2][miy2]="V";
			                		newx2 = mix2;
			                		newy2 = miy2 + 2;
			                        movimiento = "Subir barra vertical a hueco 2";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 6://Bajar horizontal
			                if (miy1>0 && miy2>0){
			                	if(mitablero[mix1][miy1-1].equals("H") && mitablero[mix2][miy2-1].equals("H")){
			                		
			                		
			                		
			                		newtablero[mix1][miy1-1]=" ";
			                		newtablero[mix2][miy2-1]=" ";
			                		newtablero[mix1][miy1]="H";
			                		newtablero[mix2][miy2]="H";
			                		newx1 = mix1;
			                		newy1 = miy1-1;
			                		newx2 = mix2;
			                		newy2 = miy2-1;
			                        movimiento = "//Bajar horizontal";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 7://Subir horizontal
			                if (miy1<4 && miy2<4){
			                	if(mitablero[mix1][miy1+1].equals("H") && mitablero[mix2][miy2+1].equals("H")){
			                		
			                		newtablero[mix1][miy1+1]=" ";
			                		newtablero[mix2][miy2+1]=" ";
			                		newtablero[mix1][miy1]="H";
			                		newtablero[mix2][miy2]="H";
			                		newx1 = mix1;
			                		newy1 = miy1+1;
			                		newx2 = mix2;
			                		newy2 = miy2+1;
			                        movimiento = "//Subir horizontal";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 8://desplazar horizontal a la derecha al hueco 1
		                	if (mix1>1){
			                	if(mitablero[mix1-1][miy1].equals("H")){
			                		
			                		newtablero[mix1-2][miy1]=" ";
			                		newtablero[mix1][miy1]="H";
			                		newx1 = mix1 - 2;
			                		newy1 = miy1;
			                        movimiento = "Desplazar barra horizontal a la derecha";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 9://desplazar horizontal a la izquierda al hueco 1
		                	if (mix1<2){
			                	if(mitablero[mix1+1][miy1].equals("H")){
			                		
			                		newtablero[mix1+2][miy1]=" ";
			                		newtablero[mix1][miy1]="H";
			                		newx1 = mix1+2;
			                		newy1 = miy1;
			                        movimiento = "Desplazar barra horizontal a la izquierda";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 10://desplazar horizontal a la derecha al hueco 2
		                	if (mix2>1){
		                		if(mitablero[mix2-1][miy2].equals("H")){
				                		
			                		newtablero[mix2-2][miy2]=" ";
			                		newtablero[mix2][miy2]="H";
			                		newx2 = mix2 - 2;
			                		newy2 = miy2;
			                        movimiento = "Desplazar barra horizontal a la derecha";
			                        generado = true;
			                        coste=1;
			                	}
			                }	
			                break;
		                case 11://desplazar horizontal a la izquierda al hueco 2
		                	if (mix2<2){
			                	if(mitablero[mix2+1][miy2].equals("H")){
			                		
			                		newtablero[mix2+2][miy2]=" ";
			                		newtablero[mix2][miy2]="H";
			                		newx2 = mix2+2;
			                		newy2 = miy2;
			                        movimiento = "Desplazar barra horizontal a la izquierda";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 12://Bajar cuadrado peque�o hacia hueco 1
		                	if (miy1>0){
			                	if(mitablero[mix1][miy1-1].equals("S")){
			                		
			                		newtablero[mix1][miy1-1]=" ";
			                		newtablero[mix1][miy1]="S";
			                		newx1 = mix1;
			                		newy1 = miy1-1;
			                        movimiento = "Bajar cuadrado peque�o hacia hueco 1";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 13://Bajar cuadrado peque�o hacia hueco 2
		                	if (miy2>0){
			                	if(mitablero[mix2][miy2-1].equals("S")){
			                		
			                		newtablero[mix2][miy2-1]=" ";
			                		newtablero[mix2][miy2]="S";
			                		newx2 = mix2;
			                		newy2 = miy2-1;
			                        movimiento = "Bajar cuadrado peque�o hacia hueco 2";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 14://Subir cuadrado peque�o hacia hueco 1
		                	if (miy1<4){
			                	if(mitablero[mix1][miy1+1].equals("S")){
			                		
			                		newtablero[mix1][miy1+1]=" ";
			                		newtablero[mix1][miy1]="S";
			                		newx1 = mix1;
			                		newy1 = miy1+1;
			                        movimiento = "Subir cuadrado peque�o hacia hueco 1";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 15://Subir cuadrado peque�o hacia hueco 2
		                	if (miy2<4){
			                	if(mitablero[mix2][miy2+1].equals("S")){
			                		
			                		newtablero[mix2][miy2+1]=" ";
			                		newtablero[mix2][miy2]="S";
			                		newx2 = mix2;
			                		newy2 = miy2+1;
			                        movimiento = "Subir cuadrado peque�o hacia hueco 2";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 16://Mover cuadrado peque�o a la derecha hacia hueco 1
		                	if (mix1>0){
			                	if(mitablero[mix1-1][miy1].equals("S")){
			                		
			                		newtablero[mix1-1][miy1]=" ";
			                		newtablero[mix1][miy1]="S";
			                		newx1 = mix1-1;
			                		newy1 = miy1;
			                        movimiento = "Mover cuadrado peque�o a la derecha hacia hueco 1";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 17://Mover cuadrado peque�o a la derecha hacia hueco 2
		                	if (mix2>0){
			                	if(mitablero[mix2-1][miy2].equals("S")){
			                		
			                		newtablero[mix2-1][miy2]=" ";
			                		newtablero[mix2][miy2]="S";
			                		newx2 = mix2-1;
			                		newy2 = miy2;
			                        movimiento = "Mover cuadrado peque�o a la derecha hacia hueco 2";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 18://Mover cuadrado peque�o a la izquierda hacia hueco 1
		                	if (mix1<3){
			                	if(mitablero[mix1+1][miy1].equals("S")){
			                		
			                		newtablero[mix1+1][miy1]=" ";
			                		newtablero[mix1][miy1]="S";
			                		newx1 = mix1+1;
			                		newy1 = miy1;
			                        movimiento = "Mover cuadrado peque�o a la izquierda hacia hueco 1";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 19://Mover cuadrado peque�o a la izquierda hacia hueco 2
		                	if (mix2<3){
			                	if(mitablero[mix2+1][miy2].equals("S")){
			                		
			                		newtablero[mix2+1][miy2]=" ";
			                		newtablero[mix2][miy2]="S";
			                		newx2 = mix2+1;
			                		newy2 = miy2;
			                        movimiento = "Mover cuadrado peque�o a la izquierda hacia hueco 2";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 20://Subir cuadrado grande
		                	if (miy1<3 && miy2<3){
			                	if(mitablero[mix1][miy1+1].equals("C") && mitablero[mix2][miy2+1].equals("C")){
			                		
			                		newtablero[mix1][miy1+2] = " ";
			                		newtablero[mix2][miy2+2] = " ";
			                		newtablero[mix1][miy1] = "C";
			                		newtablero[mix2][miy2] = "C";
			                		newx1 = mix1;
			                		newy1 = miy1+2;
			                		newx2 = mix2;
			                		newy2 = miy2+2;
			                        movimiento = "Subir cuadrado grande";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 21://Bajar cuadrado grande
		                	if (miy1>1 && miy2>1){
			                	if(mitablero[mix1][miy1-1].equals("C") && mitablero[mix2][miy2-1].equals("C")){
			                		
			                		newtablero[mix1][miy1-2] = " ";
			                		newtablero[mix2][miy2-2] = " ";
			                		newtablero[mix1][miy1] = "C";
			                		newtablero[mix2][miy2] = "C";
			                		newx1 = mix1;
			                		newy1 = miy1-2;
			                		newx2 = mix2;
			                		newy2 = miy2-2;
			                        movimiento = "Bajar cuadrado grande";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
			            case 22://Bajar vertical a hueco 1
			                if (miy1>1){
			                	if(mitablero[mix1][miy1-1].equals("V")){
			                		
			                		newtablero[mix1][miy1-2]=" ";
			                		newtablero[mix1][miy1]="V";
			                		newx1 = mix1;
			                		newy1 = miy1 - 2;
			                        movimiento = "Bajar barra vertical a hueco 1";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
		                case 23://Bajar vertical a hueco 2
			                if (miy2>1){
			                	if(mitablero[mix2][miy2-1].equals("V")){
			                		
			                		newtablero[mix2][miy2-2]=" ";
			                		newtablero[mix2][miy2]="V";
			                		newx2 = mix2;
			                		newy2 = miy2 - 2;
			                        movimiento = "Bajar barra vertical a hueco 2";
			                        generado = true;
			                        coste=1;
			                	}
			                }
			                break;
	                }
	                	                
	                if(generado && valido(newtablero)){
	                	
	                	KhunPhanPuzzle nuevoEstado = new KhunPhanPuzzle(newtablero, newx1, newy1, newx2, newy2, recorrido);
	                	
	                	String datos=movimiento+" ,COSTE:"+coste+"\n"+nuevoEstado;
						sucesores.add(new Successor(datos, nuevoEstado));
		                
	                }
			}
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			
			KhunPhanPuzzle puzzle=(KhunPhanPuzzle)state;
			
			if(puzzle._tablero[1][3] == "C" && puzzle._tablero[2][3] == "C" && 
			   puzzle._tablero[1][4] == "C" && puzzle._tablero[2][4] == "C")
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
			KhunPhanPuzzle puzzle=(KhunPhanPuzzle)state;

	        int valor = 0;
	        if (puzzle._tablero[1][3] != "C") valor++;
	        if (puzzle._tablero[1][4] != "C") valor++;
	        if (puzzle._tablero[2][3] != "C") valor++;
	        if (puzzle._tablero[2][4] != "C") valor++;
	        return valor;
		}
	}
} 
 

