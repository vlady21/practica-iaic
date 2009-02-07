package modelo.juegos;

import java.util.ArrayList;
import java.util.List;
import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

/*
     El juego de la calculadora se representa mediante 5 numeros de los cuales
     los cuatro primeros son los operandos y el ultimo es el resultado esperado.
     El objetivo del problema es que los operandos consigan obtener el resultado
     esperado. Para ello los operandos deberan de cumplir la siguiente ecuacion:

        Ecuacion: ((x1/x2)*x3)-x4 = resultadoEsperado

     @author Victor Adail Ferrer 02662811-D
 */
public class Calculadora extends InterfazJuego{

    /**
	 * Dificultad del problema de la calculadora
	 */
	private int _dificultad=5;

    /**
	 * Resultado esperado de la ecuacion
	 */
	private int _resultado = 304;

    /**
	 * Operandos de la ecuacion
	 */
	private int _num1, _num2, _num3, _num4;
	
	public Calculadora(){
        _enunciadoProblema="Tenemos una operación matemática cuyos operandos estan desordenados y queremos ordenarlos para que se cumpla el resultado ordenado.";
		_nodosExpandidos=0;
		_resuelto=false;
		_num1 =2;
		_num2= 12;
		_num3= 54;
		_num4= 20;
		
	}

    /**
	 * Genera un nodo del problema calculadora con el estado del juego en ese momento
	 * @param num1 operando numero1
	 * @param num2 operando numero2
	 * @param num3 operando numero3
	 * @param num4 operando numero4
	 */
	public Calculadora(int num1, int num2, int num3, int num4){
		_num1 =num1;
		_num2= num2;
		_num3= num3;
		_num4= num4;
		
	}
	
	public Problem getProblema() {
		Problem problem = new Problem(new Calculadora(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
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
        
        return _num1 + " / " + _num2 + " * " + _num3 + " - " + _num4; 
	}
	
	//------------------------------------------------- CLASES FUNCIONES

	//GENERACION DE SUCESORES
    @SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Calculadora cal=(Calculadora)state;
			List sucesores=new ArrayList();
			int num1=cal._num1;
			int num2=cal._num2;
			int num3=cal._num3;
			int num4=cal._num4;
			int newnum1,newnum2,newnum3,newnum4;
			String movimiento="";
			int coste=0;
			_nodosExpandidos++;
			boolean generado=false;
			
			for(int operador=0;operador<3;operador++){
				generado=false;
				newnum1=num1;
				newnum2=num2;
				newnum3=num3;
				newnum4=num4;
				
				switch(operador){
					case 0://intercambiar 1er y 2nd operando
						
						newnum1=num2;
						newnum2=num1;
						newnum3=num3;
						newnum4=num4;
						movimiento="intercambiar 1er y 2nd operando";
						generado=true;
						coste=1;
						
						break;
					
					case 1://intercambiar 2er y 3rd operando
						
						newnum1=num1;
						newnum2=num3;
						newnum3=num2;
						newnum4=num4;
						movimiento="intercambiar 2er y 3rd operando";
						generado=true;
						coste=1;
						
						break;
					
					case 2://intercambiar 3er y 4to operando
						
						newnum1=num1;
						newnum2=num2;
						newnum3=num4;
						newnum4=num3;
						movimiento="intercambiar 3er y 4to operando";
						generado=true;
						coste=1;
						
						break;
					
				}
				
				Calculadora nuevoEstado=new Calculadora(newnum1,newnum2,newnum3,newnum4);
				
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
			Calculadora cal =(Calculadora)state;
						
			if(_resultado==((cal._num1/cal._num2)*cal._num3)-cal._num4){
				_resuelto=true;
			}
			return _resuelto;
		}
	}
	
	//VALOR HEURISTICO

    //Genera la heuristica para este problema, cuenta operandos descolocados
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Calculadora cal=(Calculadora)state;
			
			int result = 0;
			
			if(cal._num1!=54)
				result++;
			if(cal._num2!=2)
				result++;
			if(cal._num3!=12)
				result++;
			if(cal._num4!=20)
				result++;
			
			return result;
		}
	}

}
