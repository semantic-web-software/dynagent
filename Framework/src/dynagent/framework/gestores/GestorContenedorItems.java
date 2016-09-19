
package dynagent.framework.gestores;

import java.awt.Color;

import javax.swing.ImageIcon;

import dynagent.framework.utilidades.SkinComponente;


/**
 * Interfaz para gestionar items que se le añadirian a un container grafico
 * @author Francisco Javier Martinez Navarro
 */
public interface GestorContenedorItems {
        
    /**
     * Añadir un item al container
     * @param identificador Clave del item
     * @param texto Texto que se muestra en el item
     * @param textoUso Texto de descripcion del item
     * @param icono Icono que muestra el item, null si no queremos icono
     * @param identificadorPadre Clave del item padre que lo contiene, null si no tiene padre
     * @param identificadorConjunto Clave del conjunto que lo contiene, null si no pertenece a ningun conjunto
     * @return Falso si no existe un item padre con esa clave, Verdadero si se añade correctamente el item
     */
    public boolean addItem(String identificador,String texto,String textoUso,ImageIcon icono,String identificadorPadre,String identificadorConjunto);
    /**
     * Borrar un item del container
     * @param identificador Clave del item
     * @return Falso si no existe un item con esa clave, Verdadero si se borra correctamente
     */
    public boolean removeItem(String identificador);
    /**
     * Borrar todos los items del container
     * @param identificadorConjunto Clave del conjunto. null para borrar los items que no pertenecen a ningun conjunto
     */
    public boolean removeItems(String identificadorConjunto);
    
    public boolean setEventoItem(String identificador, String actionCommand, Object listener);
    
    public boolean setEventoItems(String identificadorConjunto, String actionCommand, Object listener);
    
    /**
     * Establecer caracteristicas graficas a un item
     * @param identificador Clave del item
     * @param skin Caracteristicas del item
     * @return Falso si no existe un item con esa clave, Verdadero si se ha realizado correctamente
     */
    public boolean setSkinItem(String identificador,SkinComponente skin);
    
    /**
     * Establecer caracteristicas graficas a todos los items de un mismo conjunto
     * @param identificadorConjunto Clave del conjunto. null para aplicarlo a los items que no pertenecen a ningun conjunto
     * @param skin Caracteristicas del item
     */
    public boolean setSkinItems(String identificadorConjunto,SkinComponente skin);
    
    /**
     * Hacer visible o no un item
     * @param identificador Clave del item
     * @param mostrar Mostrar o no mostrar el item
     * @return Falso si no existe un item con esa clave, Verdadero si se ha realizado correctamente
     */
    public boolean setVisibleItem(String identificador,boolean mostrar);
    /**
     * Hacer visible o no todos los items de un mismo conjunto
     * @param identificadorConjunto Clave del conjunto. null para hacer visible los items que no pertenecen a ningun conjunto
     * @param mostrar Mostrar o no mostrar todos los items
     */
    public boolean setVisibleItems(String identificadorConjunto,boolean mostrar);
    
    public boolean setHighlightedItem(String identificador,boolean highlighted,String identificadorPadre);
    
    public boolean setHighlightedItemColor(String identificador,Color color,String identificadorPadre);
}
