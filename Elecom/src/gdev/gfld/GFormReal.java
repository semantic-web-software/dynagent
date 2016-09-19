package gdev.gfld;

import gdev.gen.GConst;
import gdev.gen.GConfigView;
import java.awt.Dimension;

/**
 * Esta clase representa un campo que contiene datos de números reales.
 * @author Juan
 *
 */
public class GFormReal extends GFormField
{
    /**
     * Este método devuelve el código asociado al tipo Entero.
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_INTEGER}).
     * @return int - El código asociado.
     */
    public int getType()
    {
        return GConst.TM_REAL;
    }

    /**
     * Este método devuelve la mínima dimensión del componente
     * @return Dimension - El área mínima del componente obtenida.
     */
    public Dimension getMinimumComponentDimension()
    {
        return new Dimension(getWidthMin(),(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
    /**
     * Este método devuelve el mínimo ancho del campo
     * @return int - El ancho del componente
     */
    protected int getWidthMin()
    {
        int mult = m_objViewBalancer.getFilterMode() ? 2 : 1;
        int suma = m_objViewBalancer.getFilterMode() ? 1 : 0;
        return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()),suma +/*mult * SH*/(getLength() == -1 ? GConfigView.minimumLengthNumericField :getLength()));
    }
}
