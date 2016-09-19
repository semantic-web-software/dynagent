package gdev.gfld;

import gdev.gen.GConst;
import java.awt.Dimension;


import gdev.gen.GConfigView;

/**
 * Esta clase representa a un CheckBox con comentario.
 */
public class GFormBooleanComment extends GFormField
{
    /**
     * Este método devuelve el código asociado al tipo BOOLEAN (CheckBox).
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_BOOLEAN_COMMENTED}).
     * @return int - El código asociado.
     */
    public int getType()
    {
        return GConst.TM_BOOLEAN_COMMENTED;
    }
    /**
     * Este método devuelve la mínima dimensión del componente, tanto en ancho como en alto.
     * @return Dimension - El área que ocupa.
     */
    public Dimension getMinimumComponentDimension()
    {
    	/*return new Dimension(getDimensionCheckBox().width+getDimensionText().width,(int)m_objViewBalancer.getRowHeight());*/
    	return new Dimension(getWidthMin(),getWidthMin()/*(int)m_objViewBalancer.getRowHeight()*/);
    }
    /**
     * Obtiene el ancho mínimo del CheckBox
     * @return int - Devuelve sólo el ancho mínimo del CheckBox
     */
    protected int getWidthMin()
    {
    	/*return getDimensionCheckBox().width+getDimComponenteSecundario().width;*/
    	//return GConfigView.minimumWidthBoolField;
    	int rowHeight=(int)m_objViewBalancer.getRowHeight(this.isHighlighted());
    	int height=rowHeight-(int)Math.round(rowHeight*GConfigView.reductionSizeCheck);
    	return height;
    	
    	
        /*int longc = GConfigView.minimumLengthText;
        int temp;
        if (getLength() == -1)
            return (int) (longc * m_objViewBalancer.getAveCharWidth());

        temp = widthEdit(m_objViewBalancer.getAveCharWidth(), getLength());
        return temp-getDimensionCheckBox().width;*/
    }
    /*public Dimension getDimensionCheckBox(){
    	return new Dimension(GConfigView.minimumWidthBoolField,(int)m_objViewBalancer.getRowHeight()); 
    }*/
    /*public Dimension getDimensionText(){
    	int longc = GConfigView.minimumLengthText;
        int ancho=0;
        if (getLength() == -1)
        	ancho= (int) (longc * m_objViewBalancer.getAveCharWidth());
        else{
        	ancho = widthEdit(m_objViewBalancer.getAveCharWidth(), getLength())-getDimensionCheckBox().width;
        }
    	return new Dimension(ancho,(int)m_objViewBalancer.getRowHeight());
    }*/
    
    /**
     * Devuelve la dimensión del componente secundario.
     * @return Dimension - Área del componente secundario.
     */
    public Dimension getDimComponenteSecundario(){
    	int longc = GConfigView.minimumLengthCheckExtension;
        int ancho=0;
        if (getLength() == -1)
        	ancho= (int) (longc * m_objViewBalancer.getAveCharWidth(isHighlighted()));
        else{
        	ancho = widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()), getLength())-getWidthMin();
        }
    	return new Dimension(ancho,(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
}
