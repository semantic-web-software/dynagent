package gdev.gfld;

import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.Dimension;

public class GFormDateHour extends GFormField{
	/**
     * Este método devuelve el código asociado al tipo Fecha,Hora.
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_DATE_HOUR}).
     * @return int - El código asociado.
     */
    public int getType()
    {
        return GConst.TM_DATE_HOUR;
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
    	//int longc = GConfigView.minimumLengthText;
        int mult = m_objViewBalancer.getFilterMode() ? 2 : 1;
        int suma = m_objViewBalancer.getFilterMode() ? 1 : 0;
        /*if(getLength() == -1)
        	return (int) (longc * m_objViewBalancer.getAveCharWidth());*/ 
       
       // return widthEdit(m_objViewBalancer.getAveCharWidth(),suma +/* mult * SH*/getLength());
        return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()), suma + mult * GConfigView.minimumLengthDateHour );
    }
    
    public Dimension getDimComponenteSecundario(){
    	return new Dimension(/*GConfigView.buttonWidth*/(int)m_objViewBalancer.getRowHeight(isHighlighted()),/*GConfigView.buttonHeight*/(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
}
