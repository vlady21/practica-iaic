package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class Jarras extends InterfazJuego{

	private int _jarra3=0, _jarra4=0;
	private int _dificultad=2;
	
	public Jarras(){
        _enunciadoProblema="Tenemos 2 jarras, una de 3 litros y otra de 4 y queremos llenar la de 4 litros con 2 litros exactos.";
		_nodosExpandidos=0;
		_resuelto=false;
		_jarra3=0;
		_jarra4=0;
	}
	
	public Jarras(int jarra3, int jarra4){
		_jarra3=jarra3;
		_jarra4=jarra4;
	}
	
	public Problem getProblema() {
		Problem problem = new Problem(new Jarras(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
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
	
	
	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Jarras ja=(Jarras)state;
			List sucesores=new ArrayList();
			int newjarra3=ja._jarra3;
			int newjarra4=ja._jarra4;
			String movimiento="";
			int coste=0;
			_nodosExpandidos++;
			boolean generado=false;
			
			for(int operador=0;operador<6;operador++){
				generado=false;
				switch(operador){
					case 0://vaciar la jarra de 3 litros
						if(ja._jarra3>0){
							newjarra3=0;
							newjarra4=ja._jarra4;
							movimiento="Vaciar la jarra de 3 litros: J3-"+newjarra3+" J4-"+newjarra4;
							generado=true;
							coste=1;
						}
						break;
					case 1://vaciar la jarra de 4 litros
						if(ja._jarra4>0){
							newjarra3=ja._jarra3;
							newjarra4=0;
							movimiento="Vaciar la jarra de 4 litros: J3-"+newjarra3+" J4-"+newjarra4;
							generado=true;
							coste=1;
						}
						break;
					case 2://llenar la jarra de 3 litros
						if(ja._jarra3<3){
							newjarra3=3;
							newjarra4=ja._jarra4;
							movimiento="Llenar la jarra de 3 litros: J3-"+newjarra3+" J4-"+newjarra4;
							generado=true;
							coste=1;
						}
						break;
					case 3://llenar la jarra de 4 litros
						if(ja._jarra4<4){
							newjarra3=ja._jarra3;
							newjarra4=4;
							movimiento="Llenar la jarra de 4 litros: J3-"+newjarra3+" J4-"+newjarra4;
							generado=true;
							coste=1;
						}
						break;
					case 4://verter el contenido de la jarra de 4 litros en la de 3 litros
						if(ja._jarra3<3 && ja._jarra4>0){
							newjarra3=Math.min(3, ja._jarra3+ja._jarra4);
							newjarra4=ja._jarra3+ja._jarra4-newjarra3;
							movimiento="Verter el contenido de la jarra de 4 litros en la de 3 litros: J3-"+newjarra3+" J4-"+newjarra4;
							generado=true;
							coste=1;
						}
						break;
					case 5://verter el contenido de la jarra de 3 litros en la de 4 litros
						if(ja._jarra4<4 && ja._jarra3>0){
							newjarra4=Math.min(4, ja._jarra3+ja._jarra4);
							newjarra3=ja._jarra3+ja._jarra4-newjarra4;
							movimiento="Verter el contenido de la jarra de 3 litros en la de 4 litros: J3-"+newjarra3+" J4-"+newjarra4;
							generado=true;
							coste=1;
						}
						break;
				}
				Jarras nuevoEstado=new Jarras(newjarra3, newjarra4);
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
			Jarras ja=(Jarras)state;
			if(ja._jarra4==2){
				_resuelto=true;
			}
			return _resuelto;
		}
	}
	
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Jarras ja=(Jarras)state;
			return Math.abs(ja._jarra4-2);
		}
	}

}
