package dynagent.framework.gestores;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;


import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractButton;

import dynagent.framework.utilidades.SkinComponente;

/**
 * @author Francisco Javier Martinez Navarro
 *
 */
public abstract class GestorContenedor implements GestorContenedorItems,GestorContenedorPaneles {
	/**
	* Almacena los componentes creados a partir de los items asignandoles un identificador
	*/
	protected HashMap<String, Object> mapeadoItems;
	/**
	* Almacena los componentes paneles asignandoles un identificador
	*/
	protected HashMap<String, Object> mapeadoPaneles;
	/**
	* Almacena los componentes creados a partir de conjuntos de items asignandoles un identificador
	*/
	protected HashMap<String, Object> mapeadoConjuntosItems;
	/**
	* Almacena los componentes creados a partir de conjuntos de paneles asignandoles un identificador
	*/
	protected HashMap<String, Object> mapeadoConjuntosPaneles;
	/**
	* Almacena el identificador del conjunto de items que actualmente esta añadido al panel
	*/
	protected String identificadorConjuntoItemsActual;
	/**
	* Almacena el identificador del conjunto de paneles que actualmente esta añadido al panel
	*/
	protected String identificadorConjuntoPanelesActual;
	
	/**
     * Container en el que se almacenan los componentes
     */
	protected JPanel panel;
	/**
	* Restriccion que se aplica al añadir un conjunto de items al panel
	*/
	protected Object restriccionConjuntoItems;
	/**
	* Restriccion que se aplica al añadir un conjunto de paneles al panel
	*/
	protected Object restriccionConjuntoPaneles;
	
	
	public GestorContenedor(){
		panel=new JPanel();
		mapeadoItems=new HashMap<String, Object>();
		mapeadoPaneles=new HashMap<String, Object>();
		mapeadoConjuntosItems=new HashMap<String, Object>();
		mapeadoConjuntosPaneles=new HashMap<String, Object>();
		identificadorConjuntoItemsActual=null;
		restriccionConjuntoItems=null;
		restriccionConjuntoPaneles=null;
	}
	
	/**
     * Obtener el container principal
     * @return el atributo panel
     */
    public javax.swing.JPanel getComponente() {
        return panel;
    } 
	
	public abstract boolean addItem(String identificador, String texto, String textoUso, ImageIcon icono, String identificadorPadre, String identificadorConjunto) ;

	public boolean removeItem(String identificador) {
		Component item=(Component)mapeadoItems.get(identificador);
        if(item!=null){
            Container padre=item.getParent();
            if(padre!=null){
            	//De esta manera se elimina tanto si es un item hoja o no
            	padre.remove(item);
            	mapeadoItems.remove(identificador);
            	return true;
            }   
        }
        return false;
	}

	public boolean removeItems(String identificadorConjunto) {
		Container conjunto=(Container)mapeadoConjuntosItems.get(identificadorConjunto);
		if(conjunto!=null){
        	java.util.Collection valoresMapeados=mapeadoItems.values();
            Iterator iteratorElementos=valoresMapeados.iterator();
            while(iteratorElementos.hasNext()){
                Object item=iteratorElementos.next();
                if(conjunto.isAncestorOf((Component)item)){
                	mapeadoItems.remove(item);
                }
            }
	    	conjunto.removeAll();
	        return true;
        }
		return false;
	}

	public boolean setEventoItem(String identificador, String actionCommand, Object listener) {
		if(ActionListener.class.isInstance(listener)){
	    	ActionListener actionListener=(ActionListener)listener;
	        
	        Object item=mapeadoItems.get(identificador);
	        if(item!=null){
	            if(AbstractButton.class.isAssignableFrom(item.getClass())){
	                ((AbstractButton)item).addActionListener(actionListener);
	                ((AbstractButton)item).setActionCommand(actionCommand);
	                return true;
	            }
	        }
    	}else if(MouseListener.class.isInstance(listener)){
    		MouseListener mouseListener=(MouseListener)listener;
	        
	        Object item=mapeadoItems.get(identificador);
	        if(item!=null){
	            if(Component.class.isAssignableFrom(item.getClass())){
	                ((Component)item).addMouseListener(mouseListener);
	                return true;
	            }
	        }
    	}
		return false;
	}

	public boolean setEventoItems(String identificadorConjunto, String actionCommand, Object listener) {
		boolean asigna=false;
		if(ActionListener.class.isInstance(listener)){
	    	ActionListener actionListener=(ActionListener)listener;
	        
	    	Container conjunto=(Container)mapeadoConjuntosItems.get(identificadorConjunto);
			if(conjunto!=null){
	        	java.util.Collection valoresMapeados=mapeadoItems.values();
	            Iterator iteratorElementos=valoresMapeados.iterator();
	            while(iteratorElementos.hasNext()){
	                Object item=iteratorElementos.next();
	                if(conjunto.isAncestorOf((Component)item)){
	                	if(AbstractButton.class.isAssignableFrom(item.getClass())){
	    	                ((AbstractButton)item).addActionListener(actionListener);
	    	                ((AbstractButton)item).setActionCommand(actionCommand);
	    	                asigna=true;
	    	            }
	                }
	            }
	        }
		}
		
		if(MouseListener.class.isInstance(listener)){
			MouseListener mouseListener=(MouseListener)listener;
	        
	    	Container conjunto=(Container)mapeadoConjuntosItems.get(identificadorConjunto);
			if(conjunto!=null){
	        	java.util.Collection valoresMapeados=mapeadoItems.values();
	            Iterator iteratorElementos=valoresMapeados.iterator();
	            while(iteratorElementos.hasNext()){
	                Object item=iteratorElementos.next();
	                if(conjunto.isAncestorOf((Component)item)){
	                	if(Component.class.isAssignableFrom(item.getClass())){
	    	                ((Component)item).addMouseListener(mouseListener);
	    	                asigna=true;
	    	            }
	                }
	            }
	        }
		}
		if(asigna)
			return true;
		return false;
	}

	public boolean setSkinItem(String identificador, SkinComponente skin) {
		Object item=mapeadoItems.get(identificador);
        if(item!=null){
            if(Component.class.isAssignableFrom(item.getClass())){
            	skin.setSkinAll((Component)item);
                return true;
            }
        }
        return false;
	}

	public boolean setSkinItems(String identificadorConjunto, SkinComponente skin) {
		Container conjunto=(Container)mapeadoConjuntosItems.get(identificadorConjunto);
		if(conjunto!=null){
        	java.util.Collection valoresMapeados=mapeadoItems.values();
            Iterator iteratorElementos=valoresMapeados.iterator();
            while(iteratorElementos.hasNext()){
                Object item=iteratorElementos.next();
                if(conjunto.isAncestorOf((Component)item)){
                	skin.setSkinAll((Component)item);
                }
            }
	        return true;
        }
		return false;
	}

	public boolean setVisibleItem(String identificador, boolean mostrar) {
		Component item=(Component)mapeadoItems.get(identificador);
        if(item!=null){
            item.setVisible(mostrar);
        }
        return false;
	}

	public boolean setVisibleItems(String identificadorConjunto, boolean mostrar) {
		Container conjunto=(Container)mapeadoConjuntosItems.get(identificadorConjunto);
		if(conjunto!=null){
			Container conjuntoActual=(Container)mapeadoConjuntosItems.get(identificadorConjuntoItemsActual);
            if(conjuntoActual!=null){
            	panel.remove(conjuntoActual);
            }
            identificadorConjuntoItemsActual=identificadorConjunto;
            conjunto.setVisible(mostrar);
            panel.add(conjunto,restriccionConjuntoItems);
        	panel.revalidate();
        	panel.repaint();
        	//System.err.println("Size PanelItems:"+panel.getPreferredSize());
        	return true;
        }
		return false;
	}

	public abstract boolean addPanel(String identificador, JComponent panel, int posicion, Dimension sizeMinimo, String identificadorConjunto);

	public boolean removePanel(String identificador) {
		Component item=(Component)mapeadoPaneles.get(identificador);
        if(item!=null){
            Container padre=item.getParent();
            if(padre!=null){
            	//De esta manera se elimina tanto si es un item hoja o no
            	padre.remove(item);
            	mapeadoPaneles.remove(identificador);
            	return true;
            }   
        }
        return false;
	}

	public boolean removePanels(String identificadorConjunto) {
		Container conjunto=(Container)mapeadoConjuntosPaneles.get(identificadorConjunto);
		if(conjunto!=null){
        	java.util.Collection valoresMapeados=mapeadoPaneles.values();
            Iterator iteratorElementos=valoresMapeados.iterator();
            while(iteratorElementos.hasNext()){
                Object item=iteratorElementos.next();
                if(conjunto.isAncestorOf((Component)item)){
                	mapeadoPaneles.remove(item);
                }
            }
	    	conjunto.removeAll();
	    	if(identificadorConjunto.equals(identificadorConjuntoPanelesActual)){
            	panel.remove(conjunto);
            	identificadorConjuntoPanelesActual=null;
	    	}
	    	mapeadoConjuntosPaneles.remove(identificadorConjunto);
	        return true;
        }
		return false;
	}

	public boolean setEventoPanel(String identificador, Object listener) {
		return false;
	}

	public boolean setEventoPanels(String identificadorConjunto, Object listener) {
		return false;
	}

	public boolean setSkinPanel(String identificador, SkinComponente skin) {
		Object item=mapeadoPaneles.get(identificador);
        if(item!=null){
            if(Component.class.isAssignableFrom(item.getClass())){
            	skin.setSkinAll((Component)item);
                return true;
            }
        }
        return false;
	}

	public boolean setSkinPanels(String identificadorConjunto, SkinComponente skin) {
		Container conjunto=(Container)mapeadoConjuntosPaneles.get(identificadorConjunto);
		if(conjunto!=null){
        	java.util.Collection valoresMapeados=mapeadoPaneles.values();
            Iterator iteratorElementos=valoresMapeados.iterator();
            while(iteratorElementos.hasNext()){
                Object item=iteratorElementos.next();
                if(conjunto.isAncestorOf((Component)item)){
                	skin.setSkinAll((Component)item);
                }
            }
	        return true;
        }
		return false;
	}

	public boolean setVisiblePanel(String identificador, boolean mostrar) {
		Component item=(Component)mapeadoPaneles.get(identificador);
        if(item!=null){
            item.setVisible(mostrar);
            return true;
        }
        return false;
	}

	public boolean setVisiblePanels(String identificadorConjunto, boolean mostrar) {
		Container conjunto=(Container)mapeadoConjuntosPaneles.get(identificadorConjunto);
		if(conjunto!=null){
			Container conjuntoActual=(Container)mapeadoConjuntosPaneles.get(identificadorConjuntoPanelesActual);
            if(!conjunto.equals(conjuntoActual) || !panel.isAncestorOf(conjunto)){
				if(conjuntoActual!=null){
	            	panel.remove(conjuntoActual);
	            }
	            identificadorConjuntoPanelesActual=identificadorConjunto;
	            conjunto.setVisible(mostrar);
	            panel.add(conjunto,restriccionConjuntoPaneles);
	            panel.revalidate();
	        	panel.repaint();
	        	//System.err.println("PanelSetVisible:"+panel.getPreferredSize());
            }
        	return true;
        }
		return false;
	}
	
	public boolean setHighlightedItem(String identificador,boolean highlighted,String identificadorPadre){
		return false;
	}
	
	public boolean setHighlightedItemColor(String identificador,Color color,String identificadorPadre){
		return false;
	}

}
