package modelo.juegos;

import java.util.ArrayList;
import java.util.List;
import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class Calculadora extends InterfazJuego{

	private int _dificultad=2;
	private int _resultado = 304;
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
	
	public boolean resuelto(){
		return _resuelto;
	}
	
	public int dificultad(){
		return _dificultad;
	}
	
	public String toString(){
        
        return _num1 + " / " + _num2 + " * " + _num3 + " - " + _num4; 
	}
	
	//------------------------------------------------- CLASES FUNCIONES

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

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Calculadora cal =(Calculadora)state;
						
			if(_resultado==((cal._num1/cal._num2)*cal._num3)-cal._num4){
				_resuelto=true;
			}
			return _resuelto;
		}
	}
	
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
