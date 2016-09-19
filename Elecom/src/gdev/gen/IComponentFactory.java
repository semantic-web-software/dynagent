package gdev.gen;

import gdev.gawt.GComponent;
import gdev.gbalancer.GProcessedField;

import javax.swing.JComponent;
/**
 * Interfaz del creador para el patrón factory
 * @author Dynagent
 *
 */
public interface IComponentFactory {
	/** Obtiene el color de fondo*/
	
	/** Crea un componente*/
	public GComponent create(GProcessedField oneField);
	public void setParent(JComponent parent);
}
