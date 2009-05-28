/*
 * PrincipalView.java
 */

package vista;

import conocimiento.LanzadorJess;
import conocimiento.Reglas_1;
import controlador.ControladorGUI;
import java.awt.Component;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.logging.Level;
import java.util.logging.Logger;
import jess.JessException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import modelo.IZObservadorFormularios;
import modelo.ModeloFormularios;
import modelo.TablaFormulario;
import utilerias.Constantes;
import utilerias.Propiedades;

/**
 * The application's main frame.
 */
public class PrincipalView extends FrameView implements IZObservadorFormularios, Serializable{

    private Properties configuracion = Propiedades.getPropiedades(Constantes.CONFIGURACION);
    private String FICHERO_GUARDAR=configuracion.getProperty("FICHERO_GUARDAR");
    private String FICHERO_REGLAS=configuracion.getProperty("REGLAS_TECNICO");

    static Image imagenTray;
    static TrayIcon trayIcon;
    SystemTray tray;
    PrincipalView interfaz;
    private boolean noFinalizar;
    private Vector respuestasTablaTecnica;
    //private Vector respuestasTablaJuridica;
    //private Vector respuestasTablaAfectivo;
    private Vector clavesTablaTecnica;
    //private Vector clavesTablaJuridica;
    //private Vector clavesTablaAfectivo;
    private Properties propTecnico;
    //private Properties propJuridico;
    //private Properties propAfectivo;
    private LanzadorJess lanzadorJess;

    private ModeloFormularios modelo;

    private ControladorGUI controlador;

    private TablaFormulario tblTecnico;
    private TablaFormulario tblJuridico;
    private TablaFormulario tblAfectivo;

    public PrincipalView(SingleFrameApplication app, ModeloFormularios modelo, ControladorGUI controlador) {

        super(app);

        this.modelo = modelo;

        this.controlador = controlador;

        modelo.attach(this);
        
        //cargarJess();

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();

        imagenTray = resourceMap.getImageIcon("Aplicacion.icon").getImage();
        trayIcon = new TrayIcon(imagenTray, "Asesor Laboral");

        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();

            setPropiedadesTrayIcon(imagenTray, "Asesor Laboral");

        }

        getFrame().setIconImage(resourceMap.getImageIcon("Aplicacion.icon").getImage());

        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        interfaz = this;

        mensaje("Bienvenido","Bienvenido al asesor laboral.");

        status("Bienvenido al asesor laboral.");

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Principal.getApplication().getMainFrame();
            aboutBox = new PrincipalAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Principal.getApplication().show(aboutBox);
    }

    private void abrir() {

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setCurrentDirectory(new File("."));
        jFileChooser.setFileFilter(new FiltroASR());

		int state = jFileChooser.showOpenDialog(mainPanel);

		if (state == JFileChooser.APPROVE_OPTION) {
			File f = jFileChooser.getSelectedFile();

            try{

                FileInputStream  out = new FileInputStream(f);
                ObjectInputStream  s = new ObjectInputStream (out);

                modelo.setTecnico((TablaFormulario)s.readObject());
                modelo.setJuridico((TablaFormulario)s.readObject());
                modelo.setAfectivo((TablaFormulario)s.readObject());

                modelo.setCambio();

                modelo.notifyObservers();

                s.close();

                trayIcon.displayMessage("Formularios cargados correctamente", "Formularios cargados correctamente del archivo: " + f.getAbsolutePath(), TrayIcon.MessageType.INFO);

            }catch(Exception e){

                trayIcon.displayMessage("Error al cargar formularios", "Error al cargar formularios del archivo " + f.getAbsolutePath(), TrayIcon.MessageType.ERROR);

                e.printStackTrace();
            }
		}

    }

    private void cargarJess() {
        try {
            lanzadorJess = new LanzadorJess(FICHERO_GUARDAR, FICHERO_REGLAS);
            lanzadorJess.arrancarJess();
        } catch (JessException ex) {
            menError("Error Jess", "Error al lanzar el modulo de JESS");
        }

    }

    private void guardar() {

        File archivo=new File("");
        String arch = null;
        JFileChooser chooser = new JFileChooser(archivo.getAbsolutePath());
        chooser.setFileFilter(new FiltroASR());
        chooser.setCurrentDirectory(archivo);

        //muestra la ventana de dialogo y asigna el valor del boton pulsado.
        int returnVal = chooser.showSaveDialog(chooser);

        //si se pulsa el boton guardar.
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            archivo=chooser.getSelectedFile();
            if(!archivo.getName().endsWith(".asr"))
             arch = archivo.getAbsolutePath() + ".asr";
            else
             arch = archivo.getAbsolutePath();

            try{

                FileOutputStream out = new FileOutputStream(arch);
                ObjectOutputStream s = new ObjectOutputStream(out);
                s.writeObject(modelo.getTecnico());
                s.writeObject(modelo.getJuridico());
                s.writeObject(modelo.getAfectivo());
                s.close();

                trayIcon.displayMessage("Formularios guardados correctamente", "Formularios guardados correctamente en el archivo: " + arch, TrayIcon.MessageType.INFO);

            }catch(Exception e){

                trayIcon.displayMessage("Error al guardar formularios", "Error al guardar formularios en el archivo " + arch, TrayIcon.MessageType.ERROR);

                e.printStackTrace();
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        panelFormularios = new javax.swing.JTabbedPane();
        formularioTecnico = new javax.swing.JPanel();
        scrollPanelTenico = new javax.swing.JScrollPane();
        tablaTecnica = new javax.swing.JTable();
        formularioJuridico = new javax.swing.JPanel();
        scrollPanelTenico1 = new javax.swing.JScrollPane();
        tablaJuridico = new javax.swing.JTable();
        formularioAfectivo = new javax.swing.JPanel();
        scrollPanelTenico2 = new javax.swing.JScrollPane();
        tablaAfectivo = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        botonAsesorar = new javax.swing.JButton();
        botonAbrir = new javax.swing.JButton();
        botonGuardar = new javax.swing.JButton();
        botonReiniciar = new javax.swing.JButton();
        botonSalir = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.LINE_AXIS));

        panelFormularios.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(vista.Principal.class).getContext().getResourceMap(PrincipalView.class);
        panelFormularios.setToolTipText(resourceMap.getString("panelFormularios.toolTipText")); // NOI18N
        panelFormularios.setName("panelFormularios"); // NOI18N

        formularioTecnico.setToolTipText(resourceMap.getString("formularioTecnico.toolTipText")); // NOI18N
        formularioTecnico.setName("formularioTecnico"); // NOI18N
        formularioTecnico.setLayout(new javax.swing.BoxLayout(formularioTecnico, javax.swing.BoxLayout.LINE_AXIS));

        scrollPanelTenico.setName("scrollPanelTenico"); // NOI18N

        tablaTecnica.setModel(getModeloTabla(modelo.getTecnico()));
        tablaTecnica.setName("tablaTecnica"); // NOI18N
        tablaTecnica.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaTecnicaActualizar(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tablaTecnicaActualizar(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaTecnicaActualizar(evt);
            }
        });
        tablaTecnica.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                tablaTecnicaMouseMoved(evt);
            }
        });
        scrollPanelTenico.setViewportView(tablaTecnica);

        formularioTecnico.add(scrollPanelTenico);

        panelFormularios.addTab(resourceMap.getString("formularioTecnico.TabConstraints.tabTitle"), formularioTecnico); // NOI18N

        formularioJuridico.setToolTipText(resourceMap.getString("formularioJuridico.toolTipText")); // NOI18N
        formularioJuridico.setName("formularioJuridico"); // NOI18N
        formularioJuridico.setLayout(new javax.swing.BoxLayout(formularioJuridico, javax.swing.BoxLayout.LINE_AXIS));

        scrollPanelTenico1.setName("scrollPanelTenico1"); // NOI18N

        tablaJuridico.setModel(getModeloTabla(modelo.getJuridico()));
        tablaJuridico.setName("tablaJuridico"); // NOI18N
        tablaJuridico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaJuridicoActualizar(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tablaJuridicoActualizar(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaJuridicoActualizar(evt);
            }
        });
        tablaJuridico.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                tablaJuridicoMouseMoved(evt);
            }
        });
        scrollPanelTenico1.setViewportView(tablaJuridico);

        formularioJuridico.add(scrollPanelTenico1);

        panelFormularios.addTab(resourceMap.getString("formularioJuridico.TabConstraints.tabTitle"), formularioJuridico); // NOI18N

        formularioAfectivo.setToolTipText(resourceMap.getString("formularioAfectivo.toolTipText")); // NOI18N
        formularioAfectivo.setName("formularioAfectivo"); // NOI18N
        formularioAfectivo.setLayout(new javax.swing.BoxLayout(formularioAfectivo, javax.swing.BoxLayout.LINE_AXIS));

        scrollPanelTenico2.setName("scrollPanelTenico2"); // NOI18N

        tablaAfectivo.setModel(getModeloTabla(modelo.getAfectivo()));
        tablaAfectivo.setName("tablaAfectivo"); // NOI18N
        tablaAfectivo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaAfectivoActualizar(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tablaAfectivoActualizar(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablaAfectivoActualizar(evt);
            }
        });
        tablaAfectivo.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                tablaAfectivoMouseMoved(evt);
            }
        });
        scrollPanelTenico2.setViewportView(tablaAfectivo);

        formularioAfectivo.add(scrollPanelTenico2);

        panelFormularios.addTab(resourceMap.getString("formularioAfectivo.TabConstraints.tabTitle"), formularioAfectivo); // NOI18N

        mainPanel.add(panelFormularios);
        panelFormularios.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(vista.Principal.class).getContext().getActionMap(PrincipalView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 339, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        toolBar.setRollover(true);
        toolBar.setName("toolBar"); // NOI18N

        botonAsesorar.setIcon(resourceMap.getIcon("botonAsesorar.icon")); // NOI18N
        botonAsesorar.setText(resourceMap.getString("botonAsesorar.text")); // NOI18N
        botonAsesorar.setToolTipText(resourceMap.getString("botonAsesorar.toolTipText")); // NOI18N
        botonAsesorar.setFocusable(false);
        botonAsesorar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonAsesorar.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonAsesorar.setMaximumSize(new java.awt.Dimension(60, 60));
        botonAsesorar.setMinimumSize(new java.awt.Dimension(60, 60));
        botonAsesorar.setName("botonAsesorar"); // NOI18N
        botonAsesorar.setPreferredSize(new java.awt.Dimension(60, 60));
        botonAsesorar.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonAsesorar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonAsesorar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonAsesorarPresionado(evt);
            }
        });
        toolBar.add(botonAsesorar);

        botonAbrir.setIcon(resourceMap.getIcon("botonAbrir.icon")); // NOI18N
        botonAbrir.setText(resourceMap.getString("botonAbrir.text")); // NOI18N
        botonAbrir.setToolTipText(resourceMap.getString("botonAbrir.toolTipText")); // NOI18N
        botonAbrir.setFocusable(false);
        botonAbrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonAbrir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonAbrir.setMaximumSize(new java.awt.Dimension(60, 60));
        botonAbrir.setMinimumSize(new java.awt.Dimension(60, 60));
        botonAbrir.setName("botonAbrir"); // NOI18N
        botonAbrir.setPreferredSize(new java.awt.Dimension(60, 60));
        botonAbrir.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonAbrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonAbrir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonAbrirMousePressed(evt);
            }
        });
        toolBar.add(botonAbrir);

        botonGuardar.setIcon(resourceMap.getIcon("botonGuardar.icon")); // NOI18N
        botonGuardar.setText(resourceMap.getString("botonGuardar.text")); // NOI18N
        botonGuardar.setToolTipText(resourceMap.getString("botonGuardar.toolTipText")); // NOI18N
        botonGuardar.setFocusable(false);
        botonGuardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonGuardar.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonGuardar.setMaximumSize(new java.awt.Dimension(60, 60));
        botonGuardar.setMinimumSize(new java.awt.Dimension(60, 60));
        botonGuardar.setName("botonGuardar"); // NOI18N
        botonGuardar.setPreferredSize(new java.awt.Dimension(60, 60));
        botonGuardar.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonGuardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonGuardarMousePressed(evt);
            }
        });
        toolBar.add(botonGuardar);

        botonReiniciar.setIcon(resourceMap.getIcon("botonReiniciar.icon")); // NOI18N
        botonReiniciar.setText(resourceMap.getString("botonReiniciar.text")); // NOI18N
        botonReiniciar.setToolTipText(resourceMap.getString("botonReiniciar.toolTipText")); // NOI18N
        botonReiniciar.setFocusable(false);
        botonReiniciar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonReiniciar.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonReiniciar.setMaximumSize(new java.awt.Dimension(60, 60));
        botonReiniciar.setMinimumSize(new java.awt.Dimension(60, 60));
        botonReiniciar.setName("botonReiniciar"); // NOI18N
        botonReiniciar.setPreferredSize(new java.awt.Dimension(60, 60));
        botonReiniciar.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonReiniciar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonReiniciar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonReiniciarMousePressed(evt);
            }
        });
        toolBar.add(botonReiniciar);

        botonSalir.setAction(actionMap.get("quit")); // NOI18N
        botonSalir.setIcon(resourceMap.getIcon("botonSalir.icon")); // NOI18N
        botonSalir.setText(resourceMap.getString("botonSalir.text")); // NOI18N
        botonSalir.setToolTipText(resourceMap.getString("botonSalir.toolTipText")); // NOI18N
        botonSalir.setFocusable(false);
        botonSalir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonSalir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonSalir.setMaximumSize(new java.awt.Dimension(60, 60));
        botonSalir.setMinimumSize(new java.awt.Dimension(60, 60));
        botonSalir.setName("botonSalir"); // NOI18N
        botonSalir.setPreferredSize(new java.awt.Dimension(60, 60));
        botonSalir.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonSalir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(botonSalir);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolBar);
    }// </editor-fold>//GEN-END:initComponents

    private void botonAsesorarPresionado(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonAsesorarPresionado
        asesorar();
    }//GEN-LAST:event_botonAsesorarPresionado

    private void tablaTecnicaMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTecnicaMouseMoved

        int i = tablaTecnica.rowAtPoint(evt.getPoint());

        String[] values = (String[]) modelo.getTecnico().getRespuestas().get(i);
        // These are the combobox values
        //String[] values = new String[]{"item1", "item2", "item3"};

        int vColIndex = 1;
        TableColumn col = tablaTecnica.getColumnModel().getColumn(vColIndex);
        col.setCellEditor(new MyComboBoxEditor(values));

        tablaTecnicaActualizar(null);
    }//GEN-LAST:event_tablaTecnicaMouseMoved

private void botonReiniciarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonReiniciarMousePressed

    reiniciar();
    
}//GEN-LAST:event_botonReiniciarMousePressed

private void tablaJuridicoMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaJuridicoMouseMoved

    int i = tablaJuridico.rowAtPoint(evt.getPoint());

    String[] values = (String[]) modelo.getJuridico().getRespuestas().get(i);
    // These are the combobox values
    //String[] values = new String[]{"item1", "item2", "item3"};

    int vColIndex = 1;
    TableColumn col = tablaJuridico.getColumnModel().getColumn(vColIndex);
    col.setCellEditor(new MyComboBoxEditor(values));

    tablaJuridicoActualizar(null);
}//GEN-LAST:event_tablaJuridicoMouseMoved

private void tablaAfectivoMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaAfectivoMouseMoved

    int i = tablaAfectivo.rowAtPoint(evt.getPoint());

    String[] values = (String[]) modelo.getAfectivo().getRespuestas().get(i);
    // These are the combobox values
    //String[] values = new String[]{"item1", "item2", "item3"};

    int vColIndex = 1;
    TableColumn col = tablaAfectivo.getColumnModel().getColumn(vColIndex);
    col.setCellEditor(new MyComboBoxEditor(values));

    tablaAfectivoActualizar(null);
}//GEN-LAST:event_tablaAfectivoMouseMoved

private void tablaTecnicaActualizar(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTecnicaActualizar

    try{
        Vector claves = modelo.getTecnico().getClaves();
        Vector opcionesElegidas = modelo.getTecnico().getOpcionesElegidas();

        String clave;
        String valor,valor1,valor2;

        valor = null;

        int i = 0;
        boolean noEncontrado = true;

        while(noEncontrado && i<claves.size()){

            valor1 = (String) tablaTecnica.getValueAt(i, 1);
            valor2 = (String) opcionesElegidas.get(i);

            try{
                if(!valor1.equals(valor2))
                {
                    noEncontrado = false;
                    valor = valor1;
                }else{

                    i++;
                }
            }catch(Exception e){ i++;}
        }

        clave = (String) claves.get(i);

        modelo.setState(clave, valor);
    }catch(Exception e){}

}//GEN-LAST:event_tablaTecnicaActualizar

private void tablaJuridicoActualizar(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaJuridicoActualizar


    try{
        Vector claves = modelo.getJuridico().getClaves();
        Vector opcionesElegidas = modelo.getJuridico().getOpcionesElegidas();

        String clave;
        String valor,valor1,valor2;

        valor = null;

        int i = 0;
        boolean noEncontrado = true;

        while(noEncontrado && i<claves.size()){

            valor1 = (String) tablaJuridico.getValueAt(i, 1);
            valor2 = (String) opcionesElegidas.get(i);

            try{
                if(!valor1.equals(valor2))
                {
                    noEncontrado = false;
                    valor = valor1;
                }else{

                    i++;
                }
            }catch(Exception e){ i++;}
        }

        clave = (String) claves.get(i);

        modelo.setState(clave, valor);
    }catch(Exception e){}
}//GEN-LAST:event_tablaJuridicoActualizar

private void tablaAfectivoActualizar(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaAfectivoActualizar

    try{
        Vector claves = modelo.getAfectivo().getClaves();
        Vector opcionesElegidas = modelo.getAfectivo().getOpcionesElegidas();

        String clave;
        String valor,valor1,valor2;

        valor = null;

        int i = 0;
        boolean noEncontrado = true;

        while(noEncontrado && i<claves.size()){

            valor1 = (String) tablaAfectivo.getValueAt(i, 1);
            valor2 = (String) opcionesElegidas.get(i);

            try{
                if(!valor1.equals(valor2))
                {
                    noEncontrado = false;
                    valor = valor1;
                }else{

                    i++;
                }
            }catch(Exception e){ i++;}
        }

        clave = (String) claves.get(i);

        modelo.setState(clave, valor);
    }catch(Exception e){}

}//GEN-LAST:event_tablaAfectivoActualizar

private void botonAbrirMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonAbrirMousePressed

    abrir();
}//GEN-LAST:event_botonAbrirMousePressed

private void botonGuardarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonGuardarMousePressed

    guardar();
}//GEN-LAST:event_botonGuardarMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAbrir;
    private javax.swing.JButton botonAsesorar;
    private javax.swing.JButton botonGuardar;
    private javax.swing.JButton botonReiniciar;
    private javax.swing.JButton botonSalir;
    private javax.swing.JPanel formularioAfectivo;
    private javax.swing.JPanel formularioJuridico;
    private javax.swing.JPanel formularioTecnico;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTabbedPane panelFormularios;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane scrollPanelTenico;
    private javax.swing.JScrollPane scrollPanelTenico1;
    private javax.swing.JScrollPane scrollPanelTenico2;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTable tablaAfectivo;
    private javax.swing.JTable tablaJuridico;
    private javax.swing.JTable tablaTecnica;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    public void reiniciar() {
        
        int tam = tablaTecnica.getRowCount();
        
        for(int i = 0; i<tam; i++){

            tablaTecnica.setValueAt(null, i, 1);

            tablaTecnicaActualizar(null);
        }

        tam = tablaJuridico.getRowCount();

        for(int i = 0; i<tam; i++){

            tablaJuridico.setValueAt(null, i, 1);

            tablaJuridicoActualizar(null);
        }

        tam = tablaAfectivo.getRowCount();

        for(int i = 0; i<tam; i++){

            tablaAfectivo.setValueAt(null, i, 1);

            tablaAfectivoActualizar(null);
        }
    }
    
    public void mensaje(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.NONE);

    }

    public void menInfo(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.INFO);

    }

    public void menAdvertencia(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.WARNING);

    }

    public void menError(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.ERROR);

    }

    public void salir() {
        System.exit(0);
    }

    private void setPropiedadesTrayIcon(Image imagen, String texto) {

        PopupMenu menu = new PopupMenu();

        MenuItem asesorarItem = new MenuItem("Obtener asesoramiento");
        asesorarItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            asesorar();
          }
        });
        menu.add(asesorarItem);

        MenuItem abrirItem = new MenuItem("Abrir Formularios");
        abrirItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            abrir();
          }
        });
        menu.add(abrirItem);

        MenuItem guardarItem = new MenuItem("Guardar Formularios");
        guardarItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            guardar();
          }
        });
        menu.add(guardarItem);

        MenuItem reiniciarItem = new MenuItem("Reiniciar");
        reiniciarItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            reiniciar();
          }
        });
        menu.add(reiniciarItem);

        MenuItem salirItem = new MenuItem("Salir");
        salirItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            salir();
          }
        });
        menu.add(salirItem);

        trayIcon = new TrayIcon(imagen, texto, menu);

        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(interfaz.getFrame().isVisible()){
                    interfaz.getFrame().setVisible(false);
                }
                else{
                    interfaz.getFrame().setVisible(true);
                }
            }
        }
        );

        try {
            tray.add(trayIcon);

        } catch (Exception e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
    
    public void status(String string) {
        statusMessageLabel.setText(string);
    }

    public void animeStatus(String string) {
        statusAnimationLabel.setText(string);
    }

    private void cargarFormulario(JTable tabla,Vector claves) throws Exception {

        String clave,valor;
        ArrayList<String> valores_reglas1=new ArrayList<String>();
        for(int i = 0; i<claves.size(); i++){

            clave = (String) claves.get(i);
            valor = (String) tabla.getValueAt(i, 1);

            int edad=0;
            if(valor!=null){                
            //TODO RELLENAR
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

        Reglas_1 reglas_1=new Reglas_1(valores_reglas1,FICHERO_GUARDAR, FICHERO_REGLAS);
        
    }

    private boolean comprobarFormulario(JTable tabla) throws Exception {

        boolean result = true;
        String valor;
        
        int tam = tabla.getRowCount();
        
        for(int i = 0; i<tam&&result; i++){

            valor = (String) tabla.getValueAt(i, 1);

            if(valor==null){
                result = false;
            }
        }        
        
        return result;
    }
    
    public void asesorar() {
        
        animeStatus("Analizando informacion del asesorado");
        mensaje("Analizando informacion","Analizando informacion del asesorado.");

        statusAnimationLabel.setIcon(busyIcons[0]);
        busyIconIndex = 0;
        busyIconTimer.start();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        noFinalizar = true;

        BarraStatus hiloStatus = new BarraStatus();
        hiloStatus.start();

        Asesor hiloAsesor = new Asesor();
        hiloAsesor.start();

    }

    public void update() {

        if(modelo.actualizadoTecnico()){

            setModelo(tablaTecnica, modelo.getTecnico());
        }
        if(modelo.actualizadoJuridico()){

            setModelo(tablaJuridico, modelo.getJuridico());
        }
        if(modelo.actualizadoAfectivo()){

            setModelo(tablaAfectivo, modelo.getAfectivo());
        }

    }

    public void setModelo(JTable jTable, TablaFormulario tabla) {

        int tam = jTable.getRowCount();
        Vector opciones = tabla.getOpcionesElegidas();

        for(int i = 0; i<tam; i++){

            jTable.setValueAt(opciones.elementAt(i), i, 1);
        }

    }

    public class Asesor extends Thread
    {
        @Override
       public void run()
       {

            try{
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                JTable tabla = null;

                switch(panelFormularios.getSelectedIndex()){

                    case ControladorGUI.TECNICO:
                        tabla = tablaTecnica;
                        break;
                    case ControladorGUI.JURIDICO:
                        tabla = tablaJuridico;
                        break;
                    case ControladorGUI.AFECTIVO:
                        tabla = tablaAfectivo;
                        break;
                }

                if(comprobarFormulario(tabla))
                {
                    //cargarFormulario(tablaTecnica,clavesTablaTecnica);

                    String texto = controlador.asesorar(panelFormularios.getSelectedIndex());

                    status("Informacion del asesorado analizada.");
                    mensaje("Informacion analizada","Informacion del asesorado analizada.");

                    /*InformeView informe = new InformeView(lanzadorJess);
                    informe.setVisible(true);*/
                    
                    while(texto.indexOf("   ")!=-1)
                        texto = texto.replaceAll("   ", "  ");

                    texto = texto.replaceAll("  ", "\n\t");

                    while(texto.indexOf("\t")!=-1)
                        texto = texto.replaceAll("\t", "    ");

                    VisualizadorView visualizador = new VisualizadorView(texto,trayIcon);
                    visualizador.setVisible(true);
                    visualizador.setAlwaysOnTop(false);

                }else{
                    
                    status("Se han detectado preguntas sin respuesta.");
                    menError("Respuestas incompletas","Se han detectado preguntas sin respuesta.");

                }
                //cargarFormulario(tablaJuridica,clavesTablaJuridica);
                //cargarFormulario(tablaAfectiva,clavesTablaAfectivo);

                
            } catch (Exception ex) {

                status(ex.getMessage());
                menError("Error informe asesoramiento", ex.getMessage());

            }

            noFinalizar = false;
       }
    }

    public class BarraStatus extends Thread
    {

       public void run()
       {
           noFinalizar = true;

           while(noFinalizar){

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
            statusAnimationLabel.setText("");
            progressBar.setVisible(false);
            progressBar.setValue(0);

       }
    }

    public class MyComboBoxEditor extends DefaultCellEditor {
        public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }

    /**
     * Recupera el valor del fichero properties.
     * @param String Fichero properties al que se desea acceder
     * @return Properties
     */
    public static Properties getPropiedades(String ficheroProperties)
    {
      Properties propiedades = new Properties();

      try {
          FileInputStream fichero = new FileInputStream(ficheroProperties);
          propiedades.load(fichero);
          fichero.close();
      } catch (Exception e) {
          System.out.println("Error : " + e.toString());
      }
      return propiedades;
    } // Fin getPropiedades()

    public DefaultTableModel getModeloTabla(TablaFormulario tabla){

        Vector data = new Vector();
        Vector titulos = new Vector();
        Vector preguntas = tabla.getPreguntas();
        Vector aux;

        int tam = preguntas.size();

        for(int i = 0; i<tam; i++){

            aux = new Vector();
            aux.add(preguntas.elementAt(i));
            aux.add(null);
            data.add(aux);
        }

        titulos.add("Preguntas");
        titulos.add("Respuestas");

        return new DefaultTableModel(data,titulos) {
            Class[] types = new Class [] {
                java.lang.String.class, javax.swing.JComboBox.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        };
    }
}
