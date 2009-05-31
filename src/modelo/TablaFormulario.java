/*
 * Clase que representa a los formularios de la aplicacion
 */

package modelo;

import java.io.Serializable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class TablaFormulario implements Serializable{

    //Preguntas del formulario
    private Vector <String>preguntas;

    //Respuestas del formulario
    private Vector respuestas;

    //Claves del formulario
    private Vector <String>claves;

    //Opciones a elegir del formulario
    private Vector <String>opcionesElegidas;

    /**
     * Contructor de la clase
     */
    public TablaFormulario(){

        preguntas = new Vector();
        respuestas = new Vector();
        claves = new Vector();
    }

    /**
     * Contructor de la clase
     * param propiedades: fichero de propiedades del formulario
     */
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

    /**
     * Metodo para obtener las claves de la tabla
     */
    public Vector getClaves(){

        return claves;
    }

    /**
     * Metodo para obtener las preguntas de la tabla
     */
    public Vector getPreguntas(){

        return preguntas;
    }

    /**
     * Metodo para insertar las respuestas de la tabla
     */
    public Vector getRespuestas(){

        return respuestas;
    }

    /**
     * Metodo para obtener las opciones de la tabla
     */
    public Vector getOpcionesElegidas(){

        return opcionesElegidas;
    }

    /**
     * Metodo para insertar las claves de la tabla
     */
    public void cargarClaves(Vector claves){

        this.claves = claves;
    }

    /**
     * Metodo para insertar las preguntas de la tabla
     */
    public void cargarPreguntas(Vector preguntas){

        this.preguntas = preguntas;
    }

    /**
     * Metodo para insertar las respuestas de la tabla
     */
    public void cargarRespuestas(Vector respuestas){

        this.respuestas = respuestas;
    }

    /**
     * Metodo para saber si el campo clave esta en la tabla
     */
    public boolean contiene(String clave) {

        return claves.contains(clave);
    }

    /**
     * Metodo para actualizar un campo de la tabla
     */
    public void actualiza(String clave, String valor) {

        int indice;

        if(contiene(clave)){
            indice = claves.indexOf(clave);
            opcionesElegidas.setElementAt(valor, indice);
        }
    }
}
