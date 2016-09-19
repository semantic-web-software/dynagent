package gdev.gfld;

import gdev.gen.GConst;
import java.awt.Dimension;
import gdev.gen.GConfigView;

/**
 * Esta clase representa a un campo texto definido en el XML.
 * @author Dynagent
 *
 */
public class GFormText extends GFormField
{
    /**
     * Este m�todo devuelve el c�digo asociado al campo de tipo Texto.
     * El c�digo ser� el que se encuentre definido en la clase GConst ({@link GConst#TM_TEXT}).
     * @return int - El c�digo asociado.
     */
    public int getType()
    {
        return GConst.TM_TEXT;
    }
    /**
     * Este m�todo devuelve la m�nima dimensi�n del componente
     * @return Dimension - El �rea m�nima del componente obtenida.
     */
    public Dimension getMinimumComponentDimension()
    {
        return new Dimension(getWidthMin(),(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
    /**
     * Este m�todo devuelve el m�nimo ancho del campo
     * @return int - El ancho del componente
     */
    protected int getWidthMin()
    {
        int longc = GConfigView.minimumLengthText;
        if (getLength() == -1)
            return (int) (longc * m_objViewBalancer.getAveCharWidth(isHighlighted()));
        //SH longc = Math.min(longc, longitud);
        //SH return widthEdit(widthChar, longc);
        return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()), getLength());
    }
}
