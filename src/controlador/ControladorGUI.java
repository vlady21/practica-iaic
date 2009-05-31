/*
 * Clase controladora de la aplicacion
 */

package controlador;

import conocimiento.ControladorConocimiento;
import modelo.ModeloFormularios;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class ControladorGUI {

    //Modelo de la aplicacion
    private ModeloFormularios modelo;

    //Asesoramiento tecnico
	public final static int TECNICO = 0;

    //Asesoramiento tecnico
	public final static int JURIDICO = 1;

    //Asesoramiento tecnico
	public final static int AFECTIVO = 2;

    /**
     * Metodo encargado de obtener asesoramiento
     * @param asesoramiento: tipo de asesoramiento que se desa obtener
     * @return String: informe de asesoramiento
     */
    public String asesorar(int asesoramiento) throws Exception {

        String consejo = "";

        ControladorConocimiento conocimiento = new ControladorConocimiento(modelo);

        switch(asesoramiento){
            case TECNICO:
                consejo = conocimiento.AsesoramientoTecnico();
                break;
            case JURIDICO:
                consejo = conocimiento.AsesoramientoJuridico();
                break;
            case AFECTIVO:
                consejo = conocimiento.AsesoramientoAfectivo();
                break;
        }

        return consejo;
    }

    /**
     * Metodo encargado de almacenar el modelo
     * @param modelo: modelo de la aplicacion
     */
    public void setModelo(ModeloFormularios modelo) {
        this.modelo = modelo;
    }

}
