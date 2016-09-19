package gdev.gfld;

import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.Dimension;

/**
 * Esta clase representa un Botón en la entrada.
 * 
 */
public class GFormButton extends GFormField{
	private String m_text;
	
    /**
     * Este método devuelve el código asociado al tipo Botón.
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_BUTTON}).
     * @return int - El código asociado.
     */
	public int getType()
    {
        return GConst.TM_BUTTON;
    }
	
	/**
	 * Obtiene el texto que contiene el botón.
	 * @return String - Devuelve el texto del botón.
	 */
	public String getText(){
		return m_text;
	}
	
	/**
	 * Modifica el texto a mostrar en el botón
	 * @param text Es el nuevo texto que se mostrará en el botón.
	 */
	public void setText(String text){
		m_text=text;
	}
	/**
     * Este método devuelve la mínima dimensión del componente.
     * @return Dimension - Devuelve el área mínima del componente.
     */
	public Dimension getMinimumComponentDimension()
    {
        return new Dimension(getWidthMin(),(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
    
    /**
     * Este método devuelve el mínimo ancho del componente.
     * @return int - Devuelve el ancho mínimo del componente obtenido.
     */
    protected int getWidthMin()
    {
        int mult = m_objViewBalancer.getFilterMode() ? 2 : 1;
        int suma = m_objViewBalancer.getFilterMode() ? 1 : 0;
        return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()),suma +/*mult * SH*/(getLength() == -1 ? GConfigView.minimumLengthNumericField :getLength()));
    }
}
