package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

public class MisionerosYCanibales  extends InterfazJuego{
	/**
     * Indica el numero de misioneros que estan a la izquierda del rio
     * Los misioneros que estan a la derecha son 3-misionerosIzq 
     */
    private int _misionerosIzq=3;

    /**
     * Indica el numero de canibales que estan a la izquierda del rio
     * Los canibales que estan a la derecha son 3-canibalesIzq 
     */
    private int _canibalesIzq=3;

    /**
     * Indica si la barca esta a la izquierda (1) o derecha (0) del rio
     */
    private int _barcaIzq=1;
    
    private int _dificultad=3;

    public MisionerosYCanibales() {
    	_nodosExpandidos=0;
    	_resuelto=false;
	    _misionerosIzq = 3;
	    _canibalesIzq = 3;
	    _barcaIzq = 1;
	} 
    
    /**
	 * Crea una instancia del estado actual del problema de los misioneros y los canivales
	 * @param misionerosIzq numero de misioneros en la izquierda
	 * @param canibalesIzq numero de canibales en la izquierda
	 * @param barcaIzq si esta la barca en la izquierda o derecha
	 * @param c Controlador. Poner null para imprimir por pantalla
     */
	public MisionerosYCanibales(int misionerosIzq, int canibalesIzq, int barcaIzq) {
	    _misionerosIzq = misionerosIzq;
	    _canibalesIzq = canibalesIzq;
	    _barcaIzq = barcaIzq;
	} 
	
	

	public Problem getProblema() {
		Problem problem = new Problem(new MisionerosYCanibales(), new Sucesores(), new EsFinal(), new ValorHeuristico());
		return problem;		
	}

	public boolean valido(){
		boolean aux=true;
		if(_nodosExpandidos>5000){
			aux=false;
		}else if((_canibalesIzq > _misionerosIzq) && (_misionerosIzq!=0)){
			aux=false;
		}else if((_canibalesIzq < _misionerosIzq) && (_misionerosIzq!=3)){
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
			MisionerosYCanibales misioneros=(MisionerosYCanibales)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
		    int newmisioneros=0, newcanibales=0, newbote=0;
		    
            for (int operador= 0; operador<5; operador++){
            	newmisioneros=misioneros._misionerosIzq;
            	newcanibales=misioneros._canibalesIzq;
            	generado=false;
                switch(operador){
					case 0://cruza misionero
						if((misioneros._misionerosIzq>0 && misioneros._barcaIzq==1) || 
								(misioneros._misionerosIzq<3 && misioneros._barcaIzq==0)){
							if(misioneros._barcaIzq==1){
								newmisioneros=misioneros._misionerosIzq-1;
							}else{
								newmisioneros=misioneros._misionerosIzq+1;
							}
							generado=true;
							coste=1;
							movimiento="Cruza un misionero";
						}
						break;
					case 1://cruza canibal
						if((misioneros._canibalesIzq>0 && misioneros._barcaIzq==1) || 
								(misioneros._canibalesIzq<3 && misioneros._barcaIzq==0)){
							if(misioneros._barcaIzq==1){
								newcanibales=misioneros._canibalesIzq-1;
							}else{
								newcanibales=misioneros._canibalesIzq+1;
							}
							generado=true;
							coste=1;
							movimiento="Cruza un canibal";
						}
						break;
					case 2://cruzan dos misioneros
						if((misioneros._misionerosIzq>1 && misioneros._barcaIzq==1) || 
								(misioneros._misionerosIzq<2 && misioneros._barcaIzq==0)){
							if(misioneros._barcaIzq==1){
								newmisioneros=misioneros._misionerosIzq-2;
							}else{
								newmisioneros=misioneros._misionerosIzq+2;
							}
							generado=true;
							coste=1;
							movimiento="Cruzan dos misioneros";
						}
						break;
					case 3://cruzan dos canibales
						if((misioneros._canibalesIzq>1 && misioneros._barcaIzq==1) || 
								(misioneros._canibalesIzq<2 && misioneros._barcaIzq==0)){
							if(misioneros._barcaIzq==1){
								newcanibales=misioneros._canibalesIzq-2;
							}else{
								newcanibales=misioneros._canibalesIzq+2;
							}
							generado=true;
							coste=1;
							movimiento="Cruzan dos canibales";
						}
						break;
					case 4://cruza un canibal y un misionero
						if((misioneros._misionerosIzq>0 && misioneros._canibalesIzq>0 && misioneros._barcaIzq==1) || 
								(misioneros._misionerosIzq<3 && misioneros._canibalesIzq<3 && misioneros._barcaIzq==0)){
							if(misioneros._barcaIzq==1){
								newmisioneros=misioneros._misionerosIzq-1;
								newcanibales=misioneros._canibalesIzq-1;
							}else{
								newmisioneros=misioneros._misionerosIzq+1;
								newcanibales=misioneros._canibalesIzq+1;
							}
							generado=true;
							coste=1;
							movimiento="Cruza un misionero y un canibal";
						}
						break;
				}
                if(misioneros._barcaIzq==1){
					newbote=0;
				}else{
					newbote=1;
				}
                MisionerosYCanibales nuevoEstado=new MisionerosYCanibales(newmisioneros, newcanibales, newbote);
				if (generado && nuevoEstado.valido()){
					String orillas="\nIZQUIERDA(MISIONEROS: "+newmisioneros+", CANIBALES: "+newcanibales+")\t" +
							"DERECHA(MISIONEROS: "+(3-newmisioneros)+", CANIBALES: "+(3-newcanibales)+")\t";
					if(newbote==1){
						orillas+="BARCA: IZQUIERDA";
					}else{
						orillas+="BARCA: DERECHA";
					}
					String datos=movimiento+" ,COSTE:"+coste+orillas;
					sucesores.add(new Successor(datos, nuevoEstado));
				}
			}
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			MisionerosYCanibales misioneros=(MisionerosYCanibales)state;
			if(misioneros._misionerosIzq == 0 && misioneros._canibalesIzq == 0 && misioneros._barcaIzq == 0){
				_resuelto=true;
			}
			return _resuelto;
		}
	}
	
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			MisionerosYCanibales misioneros=(MisionerosYCanibales)state;
			return (misioneros._misionerosIzq+misioneros._canibalesIzq-misioneros._barcaIzq);
		}
	}
}
