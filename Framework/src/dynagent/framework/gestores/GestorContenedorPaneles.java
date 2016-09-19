
package dynagent.framework.gestores;

import javax.swing.JComponent;

import dynagent.framework.utilidades.SkinComponente;

import java.awt.Dimension;


/**
 * Interfaz para gestionar componentes JPanel que se le añadirian a un container grafico
 * @author Francisco Javier Martinez Navarro
 */
public interface GestorContenedorPaneles {

    /**
     * Añadir un componente JPanel permitiendo establecer los que pertenecen a un mismo conjunto
     * @return Falso si no se ha podido añadir, Verdadero si se ha añadido correctamente
     * @param identificador Clave del panel
     * @param panel Un componente JPanel
     * @param posicion Posicion en la que poner el panel
     * @param sizeMinimo Tamaño minimo del panel
     * @param identificadorConjunto Clave del conjunto. null si no pertecene a ningun conjunto
     */
    public boolean addPanel(String identificador,JComponent panel,int posicion,Dimension sizeMinimo,String identificadorConjunto);
    /**
     * Borrar un panel
     * @param identificador Clave del panel
     * @return Falso si no existe un panel con esa clave, Verdadero si se ha eliminado correctamente
     */
    public boolean removePanel(String identificador);
    /**
     * Borrar todos los paneles que pertenecen a un mismo conjunto
     * @param identificadorConjunto Clave del conjunto. null para borrar todos los paneles del contenedor
     */
    public boolean removePanels(String identificadorConjunto);
    
    public boolean setEventoPanel(String identificador,Object listener);
    
    public boolean setEventoPanels(String identificadorConjunto,Object listener);
    
    /**
     * Establecer caracteristicas graficas a un panel
     * @param identificador Clave del item
     * @param caracteristicas Pares clave-valor para asignar caracteristicas al panel
     * @return Falso si no existe un panel con esa clave, Verdadero si se ha realizado correctamente
     */
    public boolean setSkinPanel(String identificador,SkinComponente skin);
    
    /**
     * Establecer caracteristicas graficas a todos los paneles que pertenecen a un mismo conjunto
     * @param identificadorConjunto Clave del conjunto. null para aplicarlo a todos los paneles del contenedor
     * @param caracteristicas Pares clave-valor para asignar caracteristicas al item
     */
    public boolean setSkinPanels(String identificadorConjunto,SkinComponente skin);
    /**
     * Hacer visible o no un panel
     * @param identificador Clave del panel
     * @param mostrar Mostrar o no mostrar el panel
     * @return Falso si no existe un panel con esa clave, Verdadero si se ha realizado correctamente
     */
    public boolean setVisiblePanel(String identificador,boolean mostrar);
    /**
     * Hacer visible o no todos los paneles que pertenecen a un mismo conjunto
     * @param identificadorConjunto Clave del conjunto. null para hacer visible todos los paneles del contenedor
     * @param mostrar Mostrar o no mostrar todos los paneles
     */
    public boolean setVisiblePanels(String identificadorConjunto,boolean mostrar);
    
}
