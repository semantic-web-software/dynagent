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
     * Este m�todo devuelve el c�digo asociado al tipo BOOLEAN (CheckBox).
     * El c�digo ser� el que se encuentre definido en la clase GConst ({@link GConst#TM_BOOLEAN}).
     * @return int - El c�digo asociado.
     */
    public int getType()
    {
        return GConst.TM_BOOLEAN;
    }
    /**
     * Este m�todo devuelve la m�nima dimensi�n del componente, tanto en ancho como en alto.
     * @return Dimension - El �rea que ocupa.
     */
    public Dimension getMinimumComponentDimension()
    {
        //return new Dimension(GConfigView.minimumWidthBoolField,GConfigView.minimumWidthBoolField/*(int)m_objViewBalancer.getRowHeight()*/);
    	int rowHeight=(int)m_objViewBalancer.getRowHeight(this.isHighlighted());
    	int height=rowHeight-(int)Math.round(rowHeight*GConfigView.reductionSizeCheck);
    	return new Dimension(height,height);
    }
    
}
