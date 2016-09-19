/*
 * SplitPaneTripleDivision.java
 *
 * Created on 25 de enero de 2007, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dynagent.gui.forms.utils;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import dynagent.framework.ConstantesGraficas;

import java.awt.Component;

/**
 * Clase que hereda de JSplitPane y la trata como si estuviera dividida
 * en 3 zonas. Las posiciones de las zonas son configurables
 * @author Francisco Javier Martinez Navarro
 */

public class SplitPaneTripleDivision extends JSplitPane{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Constante para indicar la primera zona, dependiendo de la distribucion, tomandolo de izquierda a derecha empezando desde arriba
     */
    public static final int PRIMERAPOSICION=0;
    /**
     * Constante para indicar la segunda zona, dependiendo de la distribucion, tomandolo de izquierda a derecha empezando desde arriba
     */
    public static final int SEGUNDAPOSICION=1;
    /**
     * Constante para indicar la tecera zona, dependiendo de la distribucion, tomandolo de izquierda a derecha empezando desde arriba
     */
    public static final int TERCERAPOSICION=2;
    /**
     * Constante para indicar una distribucion con una zona en el norte, otra en el suroeste y la otra en el sureste
     */
    public static final int NORTE_SUROESTE_SURESTE=5;
    /**
     * Constante para indicar una distribucion con una zona en el noroeste, otra en el noreste y la otra en el sur
     */
    public static final int NOROESTE_NORESTE_SUR=6;
    /**
     * Constante para indicar una distribucion con una zona en el noroeste, otra en el este y la otra en el suroeste
     */
    public static final int NOROESTE_ESTE_SUROESTE=7;
    /**
     * Constante para indicar una distribucion con una zona en el oeste, otra en el noreste y la otra en el sureste
     */
    public static final int OESTE_NORESTE_SURESTE=8;
    
    /**
     * Constante para indicar una distribucion con una zona en el norte, otra en el suroeste y la otra en el sureste
     */
/*    public static final int ESPACIO_IZQUIERDO=11;*/
    /**
     * Constante para indicar una distribucion con una zona en el noroeste, otra en el noreste y la otra en el sur
     */
/*    public static final int ESPACIO_DERECHO=12;*/
    /**
     * Constante para indicar una distribucion con una zona en el noroeste, otra en el este y la otra en el suroeste
     */
/*    public static final int ESPACIO_ARRIBA=13;*/
    /**
     * Constante para indicar una distribucion con una zona en el oeste, otra en el noreste y la otra en el sureste
     */
/*    public static final int ESPACIO_ABAJO=14;*/
    
    /**
     * Atributo que indica la distribucion establecida
     */
    private int distribucion;
    
    /**
     * Atributo que almacena la referencia al sub JSplitPane contenido en el componente
     */
    private JSplitPane subSplitPane;
        
    /**
     * Constructor por defecto en el que se establece una distribucion NOROESTE_NORESTE_SUR
     */
    public SplitPaneTripleDivision() {
        this(NOROESTE_NORESTE_SUR/*,ESPACIO_DERECHO,ESPACIO_ARRIBA*/);
    }
    
    /**
     * Constructor por defecto en el que se establece la distribucion pasada por parametro
     * @param distribucion Distribucion de las zonas del componente. Su valor puede ser:
     * 		SplitPaneTripleDivision.NORTE_SUROESTE_SURESTE
     * 		SplitPaneTripleDivision.NOROESTE_NORESTE_SUR
     * 		SplitPaneTripleDivision.NOROESTE_ESTE_SUROESTE
     * 		SplitPaneTripleDivision.OESTE_NORESTE_SURESTE
     */
    public SplitPaneTripleDivision(int distribucion/*,int espacioExtraHorizontal,int espacioExtraVertical*/){
        super();
        this.distribucion=distribucion;
        this.setOneTouchExpandable(true);
        
        switch(distribucion){
            case NORTE_SUROESTE_SURESTE:
                this.setOrientation(JSplitPane.VERTICAL_SPLIT);
                subSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                
                /*if(espacioExtraHorizontal==ESPACIO_IZQUIERDO)
                	subSplitPane.setResizeWeight(1.0);
                else subSplitPane.setResizeWeight(0.0);
                if(espacioExtraHorizontal==ESPACIO_ARRIBA)
                	this.setResizeWeight(1.0);
                else this.setResizeWeight(0.0);*/
                
                this.setBottomComponent(subSplitPane);
                break;
            case NOROESTE_NORESTE_SUR:
                this.setOrientation(JSplitPane.VERTICAL_SPLIT);
                subSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                
                subSplitPane.setResizeWeight(1.0);
                this.setResizeWeight(1.0);
                /*if(espacioExtraHorizontal==ESPACIO_IZQUIERDO)
                	subSplitPane.setResizeWeight(1.0);
                else subSplitPane.setResizeWeight(0.0);
                if(espacioExtraHorizontal==ESPACIO_ARRIBA)
                	this.setResizeWeight(1.0);
                else this.setResizeWeight(0.0);*/
                
                this.setTopComponent(subSplitPane);
                break;
            case NOROESTE_ESTE_SUROESTE:
                this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                subSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                
                /*if(espacioExtraHorizontal==ESPACIO_IZQUIERDO)
                	this.setResizeWeight(1.0);
                else this.setResizeWeight(0.0);
                if(espacioExtraHorizontal==ESPACIO_ARRIBA)
                	subSplitPane.setResizeWeight(1.0);
                else subSplitPane.setResizeWeight(0.0);*/
                
                this.setLeftComponent(subSplitPane);
                break;
            case OESTE_NORESTE_SURESTE:
                this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                subSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                
                /*if(espacioExtraHorizontal==ESPACIO_IZQUIERDO)
                	this.setResizeWeight(1.0);
                else this.setResizeWeight(0.0);
                if(espacioExtraHorizontal==ESPACIO_ARRIBA)
                	subSplitPane.setResizeWeight(1.0);
                else subSplitPane.setResizeWeight(0.0);*/
                
                this.setRightComponent(subSplitPane);
                break;
        }
        this.setDividerSize(ConstantesGraficas.sizeDivisorSplitPane);
        subSplitPane.setDividerSize(ConstantesGraficas.sizeDivisorSplitPane);
        
        this.setBorder(BorderFactory.createEmptyBorder());
        subSplitPane.setBorder(BorderFactory.createEmptyBorder());
    }
    
    /*Se podria modificar para que introduzca sin decirle la posicion*/
    /*public void addComponente(JComponent componente,int posicion){
        this.setComponente(componente,posicion);
    }*/
    
    /**
     * Establecer un componente en una zona determinada
     * @param componente Component que se añade
     * @param posicion Lugar en el que se coloca el componente. Su valor puede ser:
     * 		SplitPaneTripleDivision.PRIMERAPOSICION
     * 		SplitPaneTripleDivision.SEGUNDAPOSICION
     * 		SplitPaneTripleDivision.TERCERAPOSICION
     */
    public void setComponente(Component componente,int posicion){
        switch(distribucion){
            case NORTE_SUROESTE_SURESTE:
                switch(posicion){
                    case PRIMERAPOSICION:
                        this.setTopComponent(componente);
                        break;
                    case SEGUNDAPOSICION:
                        subSplitPane.setLeftComponent(componente);
                        break;
                    case TERCERAPOSICION:
                        subSplitPane.setRightComponent(componente);
                        break;
                }
                break;
            case NOROESTE_NORESTE_SUR:
                switch(posicion){
                    case PRIMERAPOSICION:
                        subSplitPane.setLeftComponent(componente);
                        break;
                    case SEGUNDAPOSICION:
                        subSplitPane.setRightComponent(componente);
                        break;
                    case TERCERAPOSICION:
                        this.setBottomComponent(componente);
                        break;
                }
                break;
            case NOROESTE_ESTE_SUROESTE:
                switch(posicion){
                    case PRIMERAPOSICION:
                        subSplitPane.setTopComponent(componente);
                        break;
                    case SEGUNDAPOSICION:
                        this.setRightComponent(componente);
                        break;
                    case TERCERAPOSICION:
                        subSplitPane.setBottomComponent(componente);
                        break;
                }
                break;
            case OESTE_NORESTE_SURESTE:
                switch(posicion){
                    case PRIMERAPOSICION:
                        this.setLeftComponent(componente);
                        break;
                    case SEGUNDAPOSICION:
                        subSplitPane.setRightComponent(componente);
                        break;
                    case TERCERAPOSICION:
                        subSplitPane.setBottomComponent(componente);
                        break;
                }
                break;
        }
    }
    
}
