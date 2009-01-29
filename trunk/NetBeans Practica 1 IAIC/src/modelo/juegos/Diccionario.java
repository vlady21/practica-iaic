package modelo.juegos;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class Diccionario extends InterfazJuego{

	private int _dificultad=1;
	private String _palabra = "ARTE";
	private Vector<String> _diccionario = new Vector<String>();
	
	public Diccionario(){
		_nodosExpandidos=0;
		_resuelto=false;
		_diccionario.add("TERA");
		_diccionario.add("RETA");
		_diccionario.add("TRAE");
		
	}
	
	public Diccionario(String palabra){
		_palabra=palabra;
	}
	
	public Problem getProblema() {
		Problem problem = new Problem(new Diccionario(), new Sucesores(), new EsFinal(), new ValorHeuristico());
		return problem;		
	}

	public boolean valido(){
		return _nodosExpandidos<5000;
	}
	
	public boolean resuelto(){
		return _resuelto;
	}
	
	public int dificultad(){
		return _dificultad;
	}
	
	public String toString(){
        
        return _palabra; 
	}
	
	//------------------------------------------------- CLASES FUNCIONES

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
