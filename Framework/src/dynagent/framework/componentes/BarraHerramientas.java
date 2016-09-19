
package dynagent.framework.componentes;
/**
 * Clase que hereda de JToolBar y permite añadirle botones manteniendo un GroupButton
 * con el que solo un boton puede estar seleccionado simultaneamente
 * @author Francisco Javier Martinez Navarro
 */
public class BarraHerramientas extends javax.swing.JToolBar {

	private static final long serialVersionUID = 1L;
	
	/**
     * GroupButton que gestiona los botones añadidos
     */
    private javax.swing.ButtonGroup grupoBotones;
    
    /**
     * Indica si al añadir un boton se añade o no al grupo de botones
     */
    private boolean agruparBotones;
    
    /**
     * Constructor para crear un componente en el que, por defecto, solo un boton
     * puede estar seleccionado simultaneamente
     */
    
    public BarraHerramientas() {
        super();
        this.agruparBotones=true;
        grupoBotones=new javax.swing.ButtonGroup();
        this.setFloatable(false);
        this.setRollover(true);
    } 
    
    /**
     * Constructor para crear un componente en el que se indica si varios botones
     * pueden estar seleccionados simultaneamente
     * @param agruparBotones Hacer o no hacer que mas de un boton pueda estar seleccionado simultaneamente
     */
    public BarraHerramientas(boolean agruparBotones) {        
        super();
        this.agruparBotones=agruparBotones;
        grupoBotones=new javax.swing.ButtonGroup();
        this.setFloatable(false);
        this.setRollover(true);
    } 
    
    /**
     * Constructor para crear un componente en el que se le pasa el GroupButton que
     * controla que varios botones no esten seleccionados simultaneamente. Esto es util
     * cuando se tienen varias instancias de esta clase y se necesite que solo un boton
     * de todas las instancias pueda ser seleccionado simultaneamente.
     * @param grupoBotones GroupButton que se usara para controlar los botones
     */
    public BarraHerramientas(javax.swing.ButtonGroup grupoBotones) {        
        super();
        this.agruparBotones=true;
        this.grupoBotones=grupoBotones;
        this.setFloatable(false);
        this.setRollover(true);
    }
    
/**
     * Añadir un boton de la clase AbstractButton o descendiente de esta
     * @param boton Componente AbstractButton
     */
    public void addBoton(javax.swing.AbstractButton boton) {        
        if(agruparBotones)
            grupoBotones.add(boton);
        this.add(boton);
    } 

/**
     * Obtener el GroupButton del componente
     * @return GroupButton del componente
     */
    public javax.swing.ButtonGroup getGrupoBotones() {        
        return grupoBotones;
    }
    
    /**
     * Establecer el GroupButton al que perteneceran los botones que se añadan al componente
     * @param grupoBotones GroupButton que se encargue de gestionar los botones
     */
    public void setGrupoBotones(javax.swing.ButtonGroup grupoBotones) {        
        this.grupoBotones=grupoBotones;
    }
    
    /**
     * Obtener el valor de agruparBotones
     * @return agruparBotones del componente
     */
    public boolean getAgruparBotones() {
        return agruparBotones;
    } 
    
    /**
     * Establecer el valor del atributo agruparBotones
     * @param agruparBotones Agrupar o no agrupar botones
     */
    public void setAgruparBotones(boolean agruparBotones) {
        this.agruparBotones=agruparBotones;
    }	
 }
