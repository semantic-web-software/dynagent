package gdev.gfld;

import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.Dimension;

/**
 * Esta clase representa una imagen de un Formulario.
 * @author Dynagent
 *
 */
public class GFormImage extends GFormField {

	 public GFormImage()
    {
        super();
        m_bTopLabel = true;
    }
	 
    /**
     * Este m�todo devuelve el c�digo asociado al tipo Imagen.
     * El c�digo ser� el que se encuentre definido en la clase GConst ({@link GConst#TM_IMAGE}).
     * @return int - El c�digo asociado.
     */
    public int getType()
    {
    	return GConst.TM_IMAGE;
    }
    /**
     * Este m�todo devuelve la m�nima dimensi�n del componente
     * @return Dimension - El �rea m�nima del componente obtenida.
     */
    public Dimension getMinimumComponentDimension()
    {
        //return new Dimension(getNumBotones()*GConfigView.buttonWidth,GConfigView.buttonHeight);
    	Dimension dim=new Dimension(GConfigView.minimumSizeImage);
    	if(isMultivalued())
    		dim.width+=/*(2*GConfigView.widthImageButton)+*/GConfigView.minimumSizeImage.width+GConfigView.widthScrollBar;
    	//dim.height-=getDimComponenteSecundario().height;
    	return dim;
    }

    /**
     * Obtiene la dimensi�n del componente secundario.
     * @return Dimension - El �rea del componente secundario.
     */
    public Dimension getDimComponenteSecundario(){
    	return new Dimension(getNumBotones()*(int)m_objViewBalancer.getRowHeight(isHighlighted())/*GConfigView.buttonWidth*/,/*GConfigView.buttonHeight*/(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
    
    
}
