package gdev.gfld;

import gdev.gen.GConst;
import gdev.gen.GConfigView;
import java.awt.Dimension;

/**
 * Esta clase representa a un campo del tipo Memo (campo de texto con varias filas).
 * @author Dynagent
 *
 */
public class GFormMemo extends GFormField
{
	
	public GFormMemo()
    {
        super();
        m_bTopLabel = true;
    }
	
    /**
     * Este m�todo devuelve el c�digo asociado al tipo Memo.
     * El c�digo ser� el que se encuentre definido en la clase GConst ({@link GConst#TM_MEMO}).
     * @return int - El c�digo asociado.
     */
    public int getType()
    {
        return GConst.TM_MEMO;
    }
    /**
     * Este m�todo devuelve la m�nima dimensi�n del componente
     * @return Dimension - El �rea m�nima del componente obtenida.
     */
    public Dimension getMinimumComponentDimension()
    {
        //This method needs to be changed, its height should be greater than specified here
        /*return new Dimension(getWidthMin(),(int)m_objViewBalancer.getRowHeight());*/
    	int alto;
    	Dimension dimString=m_objViewBalancer.getDimString(m_strLabel, true, isHighlighted());
    	int anchoMax=Math.max(getWidthMin(),dimString.width);
    	int rows = this.getRows();
    	if(rows == 1){
    		alto=(int)m_objViewBalancer.getRowHeight(isHighlighted());
    		m_bTopLabel=false;
    	}
    	else{
    		alto=dimString.height+(int)m_objViewBalancer.getRowHeight(isHighlighted())*rows;
    	}
    	return new Dimension(anchoMax,alto);
    }
    /**
     * Este m�todo devuelve el m�nimo ancho del campo
     * @return int - El ancho del componente
     */
    protected int getWidthMin()
    {
        int mult = m_objViewBalancer.getFilterMode() ? 2 : 1;
        int suma = m_objViewBalancer.getFilterMode() ? 1 : 0;
        
        int width=widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()),suma +/*mult * SH*/(getLength() == -1 ? GConfigView.minimumLengthMemoByRow*getRows() :getLength()));
        return Math.min(width, m_objViewBalancer.getPanelDimensionUseful().width);
    }

}
