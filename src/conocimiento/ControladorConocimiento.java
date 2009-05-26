/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package conocimiento;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import modelo.ModeloFormularios;
import modelo.TablaFormulario;
import utilerias.Constantes;
import utilerias.LectorConsejos;
import utilerias.Propiedades;

/**
 *
 * @author Victor
 */
public class ControladorConocimiento {
    private ModeloFormularios modelo;
    private Properties configuracion;
    private LanzadorJess lanzadorJess;
    private Properties propiedadesTabla;

    public ControladorConocimiento(ModeloFormularios modelo) {

        this.modelo = modelo;

        configuracion = Propiedades.getPropiedades(Constantes.CONFIGURACION);
    }

    public String AsesoramientoAfectivo() throws Exception {

        String informe = "";

        propiedadesTabla = Propiedades.getPropiedades(Constantes.FORMULARIO_AFECTIVO);

        iniciarJess("REGLAS_AFECTIVO");

        cargarConocimiento(modelo.getAfectivo());

        informe = obtenerAsesoramiento(modelo.getAfectivo(),"REGLAS_AFECTIVO");

        return informe;
    }

    public String AsesoramientoJuridico() throws Exception {

        String informe = "";

        propiedadesTabla = Propiedades.getPropiedades(Constantes.FORMULARIO_JURIDICO);

        iniciarJess("REGLAS_JURIDICO");

        cargarConocimiento(modelo.getJuridico());

        informe = obtenerAsesoramiento(modelo.getJuridico(),"REGLAS_JURIDICO");

        return informe;
    }

    public String AsesoramientoTecnico() throws Exception {

        String informe = "";

        propiedadesTabla = Propiedades.getPropiedades(Constantes.FORMULARIO_TECNICO);

        iniciarJess("REGLAS_TECNICO");

        cargarConocimiento(modelo.getTecnico());

        informe = obtenerAsesoramiento(modelo.getTecnico(),"REGLAS_TECNICO");

        return informe;
    }

    private void cargarConocimiento(TablaFormulario formulario) throws Exception {
        
        String clave,valor;
        ArrayList<String> valores_reglas1=new ArrayList<String>();
        
        Vector <String>claves = formulario.getClaves();
        Vector <String>opciones = formulario.getOpcionesElegidas();

        try {
            for(int i = 0; i<claves.size(); i++){

                clave = claves.get(i);
                valor = opciones.get(i);

                int edad=0;
                if(valor!=null){

                    if(clave.equalsIgnoreCase("rango_edad")){
                        if(valor.equalsIgnoreCase("16-35")){
                            valor="joven";
                            edad=20;
                        }else if(valor.equalsIgnoreCase("35-65")){
                            valor="adulto";
                            edad=45;
                        }else{
                            valor="jubilado";
                            edad=70;
                        }
                        valores_reglas1.add(""+edad);
                    }
                    if(Reglas_1.pertenece(clave)){
                        valores_reglas1.add(valor);
                    }else{
                        lanzadorJess.insertaSlotValue(clave, valor);
                    }
                }
            }
            if(Reglas_1.esUsable()){
                Reglas_1 reglas_1=new Reglas_1(valores_reglas1,configuracion.getProperty("FICHERO_GUARDAR"), configuracion.getProperty("REGLAS_TECNICO"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error al cargar las respuestas en JESS");
        }
    }

    private void iniciarJess(String regla) throws Exception {

        String fichero = configuracion.getProperty("FICHERO_GUARDAR");
        String reglas = configuracion.getProperty(regla);

        try {
            lanzadorJess = new LanzadorJess(fichero, reglas);
            lanzadorJess.arrancarJess();
        } catch (Exception ex) {
            throw new Exception("Error al lanzar JESS");
        }
    }

    private String obtenerAsesoramiento(TablaFormulario tabla, String reglas) throws Exception {

        String informe = "";
        String consejosNuevos = "";
        String consejosAntiguos = "";

        try {
            
            StringTokenizer estados = new StringTokenizer(propiedadesTabla.getProperty("estados"),",");

            while(estados.hasMoreTokens()){

                consejosNuevos = "";
                consejosAntiguos = "";

                String estado=estados.nextToken();

                lanzadorJess.borrarConsejos();

                iniciarJess(reglas);

                cargarConocimiento(tabla);
                
                lanzadorJess.insertaSlotValue("estado_actual", estado);

                if(estado.equalsIgnoreCase("reglas_1")){
                    Reglas_1.ejecutarReglas_1();
                    consejosNuevos = LectorConsejos.dameConsejos();

                }else{
                    lanzadorJess.ejecutarJess();
                    consejosNuevos = LectorConsejos.dameConsejos();

                    while(!consejosNuevos.equals(consejosAntiguos)){
                        consejosAntiguos = consejosNuevos;
                        lanzadorJess.borrarConsejos();
                        lanzadorJess.ejecutarJess();
                        consejosNuevos = LectorConsejos.dameConsejos();
                        if(consejosNuevos.equals("")||consejosNuevos.length()<consejosAntiguos.length())
                            consejosNuevos = consejosAntiguos;
                    }
                }

                if(!consejosNuevos.trim().equals(""))
                {
                    informe = informe + " \n";

                    informe = informe + "\n\t***************************************************************";

                    informe = informe + "\n\t* CONSEJOS DE " + estado.toUpperCase() +".";

                    informe = informe + "\n\t***************************************************************";

                    informe = informe + " \n";

                    informe = informe + "\n" + consejosNuevos;
                }

            }

            return informe;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Error al obtener asesoramiento");
        }

    }

}
