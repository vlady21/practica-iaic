package modelo.juegos;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

/*
     El juego del diccionario se representa mediante un vector de string que
     sera el diccionario y un string con los caracteres desordenados que sera
     la palabra a buscar. El objetivo es ordenar los caracteres de la palabra
     para coseguir una palabra que se encuentre en el diccionario.

     @author Victor Adail Ferrer 02662811-D
 */
public class Diccionario extends InterfazJuego{

	private int _dificultad=7;

    /**
	 * Palabra a encontrar en el diccionario
	 */
	private String _palabra = "ARTE";

    /**
	 * Diccionario
	 */
	private Vector<String> _diccionario = new Vector<String>();
	
	public Diccionario(){
        _enunciadoProblema="Tenemos un conjunto de letras desordenadas y queremos conseguir formar una palabra del diccionario mediante la ordenacion de esas letras.";
		_nodosExpandidos=0;
		_resuelto=false;
		_diccionario.add("TERA");
		_diccionario.add("RETA");
		_diccionario.add("TRAE");
		
	}

    /**
	 * Genera un nodo de la posicion del estado del juego en ese momento
	 * @param String palabra a encontrar en el diccionario
	 */
	public Diccionario(String palabra){
		_palabra=palabra;
	}
	
	public Problem getProblema() {
		Problem problem = new Problem(new Diccionario(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
		return problem;		
	}

	public boolean valido(){
		return _nodosExpandidos<5000;
	}
	
	public int dificultad(){
		return _dificultad;
	}

    /**
	 * Genera el mensaje del estado en el que nos encontramos
	 * @return String con el mensaje del estado en el que se encuentra
	 */
	public String toString(){
        
        return _palabra; 
	}
	
	//------------------------------------------------- CLASES FUNCIONES

	//GENERACION DE SUCESORES
	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Diccionario dic=(Diccionario)state;
			List sucesores=new ArrayList();
			String newPalabra=dic._palabra;
			String miPalabra=dic._palabra;
			String movimiento="";
			int coste=0;
			_nodosExpandidos++;
			boolean generado=false;
			
			for(int operador=0;operador<3;operador++){
				generado=false;
				switch(operador){
					case 0://intercambiar 1er y 2nd caracter
						
						newPalabra=String.valueOf(miPalabra.charAt(1))+String.valueOf(miPalabra.charAt(0))+miPalabra.substring(2, 4);
						movimiento="intercambiar 1er y 2nd caracter";
						generado=true;
						coste=1;
						
						break;
					
					case 1://intercambiar 2nd y 3rd caracter
						
						newPalabra=String.valueOf(miPalabra.charAt(0))+String.valueOf(miPalabra.charAt(2))+String.valueOf(miPalabra.charAt(1))+miPalabra.substring(3, 4);
						movimiento="intercambiar 2nd y 3rd caracter";
						generado=true;
						coste=1;
						
						break;
					
					case 2://intercambiar 3rd y 4to caracter
						
						newPalabra=miPalabra.substring(0, 2)+String.valueOf(miPalabra.charAt(3))+String.valueOf(miPalabra.charAt(2));
						movimiento="intercambiar 3rd y 4to caracter";
						generado=true;
						coste=1;
						
						break;
					
				}
				
				Diccionario nuevoEstado=new Diccionario(newPalabra);
				
				if (generado && nuevoEstado.valido()){
					String datos=movimiento+" ,COSTE:"+coste+"\n"+nuevoEstado;
					//System.out.println(datos);
					sucesores.add(new Successor(datos, nuevoEstado));
				}
			}
			return sucesores;
		}
	}

    // COMPROBACION DEL ESTADO FINAL

    // La ecuacion cumple con el resultado esperado
	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Diccionario dic =(Diccionario)state;
			String palabra = dic._palabra;
			
			if(_diccionario.contains(palabra)){
				_resuelto=true;
			}
			return _resuelto;
		}
	}

    //VALOR HEURISTICO

    //Genera la heuristica para este problema, cuenta la diferencia que hay con
    //las palabras del diccionario
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Diccionario dic=(Diccionario)state;
			
			int result = 0;
			
			for(int i = 0;i<3;i++){
				if(i==0)
					result = diferencia(_diccionario.get(i),dic._palabra);
				else
					result = result*diferencia(_diccionario.get(i),dic._palabra);
			}
			return result;
		}
		
		public int diferencia(String dic, String pal){
			int result = 0;
			for(int i = 0;i<4;i++){
				if(dic.charAt(i)!=pal.charAt(i))
					result++;
			}
			return result;
		}
	}
}
