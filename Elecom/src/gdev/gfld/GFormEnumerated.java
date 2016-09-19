package gdev.gfld;

import java.util.Iterator;
import java.util.Vector;

import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConst;
import gdev.gen.GConfigView;
import java.awt.Dimension;

import javax.swing.JScrollBar;

/**
 * Esta clase representa un Enumerado (ComboBox).
 */
public class GFormEnumerated extends GFormField
{
    /**
     * Este vector contiene los valores del combo box. Cada elemento del vecor 
     * es del tipo {@link GValue}.
     */
    protected Vector m_vValues;

    /**
     * Este método devuelve el código asociado al tipo Enumerado.
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_ENUMERATED}).
     * @return int - El código asociado.
     */
    public int getType()
    {
        return GConst.TM_ENUMERATED;
    }
    
    /**
     * Este método devuelve la mínima dimensión del componente
     * @return Dimension - El área mínima del componente obtenida.
     */
    public Dimension getMinimumComponentDimension()
    {
    	Dimension dimLabel= m_objViewBalancer.getDimString(m_strLabel, true, isHighlighted() );
    	int widthComponent=getWidthMin();
    	if(m_multivalued){//Aumentamos el tamaño para que se vean varias selecciones
	    	int size=m_vValues.size();
	    	int anchoMultivalued=widthComponent;
	    	if(size<5)
	    		anchoMultivalued*=2;
	    	else if(size<9)
	    		anchoMultivalued*=2.5;
	    	else anchoMultivalued*=3;
	    	
	    	int widthPanel=m_objViewBalancer.getPanelDimensionUseful().width;
	    	if(anchoMultivalued+dimLabel.width<=widthPanel)
	    		widthComponent=anchoMultivalued;
	    	else if(widthComponent+dimLabel.width<widthPanel){
	    		widthComponent=widthPanel-dimLabel.width;
	    	}
    	}
       return new Dimension(widthComponent,(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }

    /**
     * Este método devuelve el mínimo ancho del campo
     * @return int - El ancho del componente
     */
    protected int getWidthMin()
    {
        /*return widthEdit(m_objViewBalancer.getAveCharWidth(),(getLength() == -1 ? GConfigView.minimumLengthText :getLength()));*/
    	int longc=getLength();
    	
    	if(longc==-1){
	    	Iterator itrValores=m_vValues.iterator();
	    	int anchoMax=0;
	    	while(itrValores.hasNext()){
	    		GValue valor=(GValue)itrValores.next();
	    		int ancho = m_objViewBalancer.getDimString(valor.getLabel(), true, isHighlighted()).width;
	    		if(ancho>anchoMax)
	    			anchoMax=ancho;
	    	}
	    	
	    	anchoMax+=GConfigView.widthScrollBar+/*20*/m_objViewBalancer.getRowHeight(isHighlighted())+6;//Ancho del scroll, Ancho del boton y 6 de margenes
	    	longc=anchoMax;
	    	return longc;
    	}else{
    		return widthEdit(m_objViewBalancer.getAveCharWidth(isHighlighted()), longc);
    	}
    }
    
    /**
     * Obtiene todos los valores del Combo Box y los devuelve en un vector.
     * @return Vector - Devuelve un vector con todos los valores del enumerado.
     */
    public Vector getValues()
    {
        return m_vValues;
    }
    /**
     * Modifica el vector de valores.
     * @param vValues Es el nuevo vector de valores
     */
    public void setValues(Vector vValues)
    {
        m_vValues = new Vector();
        m_vValues.addAll(vValues);
    }
}
