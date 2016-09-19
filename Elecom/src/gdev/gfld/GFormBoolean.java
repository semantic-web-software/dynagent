package gdev.gfld;

import gdev.gen.GConst;
import java.awt.Dimension;
import gdev.gen.GConfigView;

/**
 * Esta clase representa un CheckBox en la entrada.
 * 
 */
public class GFormBoolean extends GFormField
{
    /**
     * Este método devuelve el código asociado al tipo BOOLEAN (CheckBox).
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_BOOLEAN}).
     * @return int - El código asociado.
     */
    public int getType()
    {
        return GConst.TM_BOOLEAN;
    }
    /**
     * Este método devuelve la mínima dimensión del componente, tanto en ancho como en alto.
     * @return Dimension - El área que ocupa.
     */
    public Dimension getMinimumComponentDimension()
    {
        //return new Dimension(GConfigView.minimumWidthBoolField,GConfigView.minimumWidthBoolField/*(int)m_objViewBalancer.getRowHeight()*/);
    	int rowHeight=(int)m_objViewBalancer.getRowHeight(this.isHighlighted());
    	int height=rowHeight-(int)Math.round(rowHeight*GConfigView.reductionSizeCheck);
    	return new Dimension(height,height);
    }
    
}
