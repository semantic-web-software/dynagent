package gdev.gfld;

import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.Dimension;

/**
 * Esta clase representa un Bot�n en la entrada.
 * 
 */
public class GFormButton extends GFormField{
	private String m_text;
	
    /**
     * Este m�todo devuelve el c�digo asociado al tipo Bot�n.
     * El c�digo ser� el que se encuentre definido en la clase GConst ({@link GConst#TM_BUTTON}).
     * @return int - El c�digo asociado.
     */
	public int getType()
    {
        return GConst.TM_BUTTON;
    }
	
	/**
	 * Obtiene el texto que contiene el bot�n.
	 * @return String - Devuelve el texto del bot�n.
	 */
	public String getText(){
		return m_text;
	}
	
	/**
	 * Modifica el texto a mostrar en el bot�n
	 * @param text Es el nuevo texto que se mostrar� en el bot�n.
	 */
	public void setText(String text){
		m_text=text;
	}
	/**
     * Este m�todo devuelve la m�nima dimensi�n del componente.
     * @return Dimension - Devuelve el �rea m�nima del componente.
     */
	public Dimension getMinimumComponentDimension()
    {
        return new Dimension(getWidthMin(),(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
    
    /**
     * Este m�todo devuelve el m�nimo ancho del componente.
     * @return int - Devuelve el ancho m�nimo del componente obtenido.
     */
    protected int getWidthMin()
    {
        int mult = m_objViewBalancer.getFilterMode() ? 2 : 1;
        int suma = m_objViewBalancer.getFilterMode() ? 1 : 0;
        return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()),suma +/*mult * SH*/(getLength() == -1 ? GConfigView.minimumLengthNumericField :getLength()));
    }
}
