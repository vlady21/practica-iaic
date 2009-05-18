/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modelo;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author Victor
 */
public class TablaFormulario {

    private Vector preguntas;
    private Vector respuestas;
    private Vector claves;
    private Vector opcionesElegidas;

    public TablaFormulario(){

        preguntas = new Vector();
        respuestas = new Vector();
        claves = new Vector();
    }

    TablaFormulario(Properties propiedades) {

        String [] Srespuestas = null;
        StringTokenizer st;

        int i, j, tam;

        tam = propiedades.size()/3;

        preguntas = new Vector();
        respuestas = new Vector();
        claves = new Vector();
        opcionesElegidas = new Vector();

        for(i = 1; i<=tam ; i++){

            st = new StringTokenizer(propiedades.getProperty("respuesta"+i),",");

            Srespuestas = new String[st.countTokens()];

            j = 0;

            while(st.hasMoreTokens()){

                Srespuestas[j] = st.nextToken();

                j++;
            }

            respuestas.add(Srespuestas);
            preguntas.add(propiedades.getProperty("pregunta"+i));
            claves.add(propiedades.getProperty("hecho"+i));
            opcionesElegidas.add(null);
        }
    }

    public Vector getClaves(){

        return claves;
    }

    public Vector getPreguntas(){

        return preguntas;
    }

    public Vector getRespuestas(){

        return respuestas;
    }

    public Vector getOpcionesElegidas(){

        return opcionesElegidas;
    }

    public void cargarClaves(Vector claves){

        this.claves = claves;
    }

    public void cargarPreguntas(Vector preguntas){

        this.preguntas = preguntas;
    }

    public void cargarRespuestas(Vector respuestas){

        this.respuestas = respuestas;
    }

    public boolean contiene(String clave) {

        return claves.contains(clave);
    }

    public void actualiza(String clave, String valor) {

        int indice;

        if(contiene(clave)){
            indice = claves.indexOf(clave);
            opcionesElegidas.setElementAt(valor, indice);
        }
    }
}
