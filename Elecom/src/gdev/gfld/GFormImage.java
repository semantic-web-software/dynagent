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
     * Este método devuelve el código asociado al tipo Imagen.
     * El código será el que se encuentre definido en la clase GConst ({@link GConst#TM_IMAGE}).
     * @return int - El código asociado.
     */
    public int getType()
    {
    	return GConst.TM_IMAGE;
    }
    /**
     * Este método devuelve la mínima dimensión del componente
     * @return Dimension - El área mínima del componente obtenida.
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
     * Obtiene la dimensión del componente secundario.
     * @return Dimension - El área del componente secundario.
     */
    public Dimension getDimComponenteSecundario(){
    	return new Dimension(getNumBotones()*(int)m_objViewBalancer.getRowHeight(isHighlighted())/*GConfigView.buttonWidth*/,/*GConfigView.buttonHeight*/(int)m_objViewBalancer.getRowHeight(isHighlighted()));
    }
    
    
}
