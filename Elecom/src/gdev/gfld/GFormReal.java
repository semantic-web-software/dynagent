package gdev.gfld;

import gdev.gen.GConst;
import gdev.gen.GConfigView;
import java.awt.Dimension;

/**
 * Esta clase representa un campo que contiene datos de n�meros reales.
 * @author Juan
 *
 */
public class GFormReal extends GFormField
{
    /**
     * Este m�todo devuelve el c�digo asociado al tipo Entero.
     * El c�digo ser� el que se encuentre definido en la clase GConst ({@link GConst#TM_INTEGER}).
     * @return int - El c�digo asociado.
     */
    public int getType()
    {
        return GConst.TM_REAL;
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
        int mult = m_objViewBalancer.getFilterMode() ? 2 : 1;
        int suma = m_objViewBalancer.getFilterMode() ? 1 : 0;
        return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()),suma +/*mult * SH*/(getLength() == -1 ? GConfigView.minimumLengthNumericField :getLength()));
    }
}
