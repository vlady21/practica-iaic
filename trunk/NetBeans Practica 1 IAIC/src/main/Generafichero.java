/*package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Generafichero {
	private static String palabras="";
	private static ArrayList<String> _lista=new ArrayList<String>();
	
	public static void main(String[] args){
		for(int i=0;i<216;i++){
			_lista.add(""+i);
		}
		System.out.println(_lista);
		for(int i=0;i<212;i++){//quitamos los 4 planetas finales
			String conexion="";
			int max=((int)(Math.random() * 5))+2; //conecta de 2 a 6 planetas
			int j=0;
			
			for(;j<Math.min(max, 215-i);j++){
				int problema=((int)(Math.random() * 12));
				int solucion=((int)(Math.random() * 8));				
				int planeta=((int)(Math.random() * 40))+i;
				boolean agregado=false;
				
				if(planeta<216) {
					agregado=true;
					conexion+=planeta+":"+problema+":"+solucion;
					_lista.remove(""+planeta);
				}

				if(j<(Math.min(max, 215-i)-1) && planeta<212 && agregado) {
					conexion+=",";
				}
			}
			palabras+=i+"="+conexion+"\n";
		}
		PrintWriter writer;
		try {
			writer = new PrintWriter("configuracion.properties");
			writer.print(palabras);
	    	writer.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(_lista);
	}
}*/
