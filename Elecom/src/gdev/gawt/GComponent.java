package gdev.gawt;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.LayoutFocusTraversalPolicy;

import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.RowItem;

import gdev.gawt.utils.GFocusTraversalPolicy;
import gdev.gen.AssignValueException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentListener;
import gdev.gfld.GFormField;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.ParseException;

/**
 * Esta clase es una clase abstracta, orientada a representar los campos de los formularios 
 * en la interfaz gráfica, a partir de la información que contiene el campo, que se guarda
 * en el atributo {@link #m_objFormField}.
 * El método abstracto es {@link #createComponent()}, que será implementado en cada una de las clases
 * hijas (que extienden a esta clase) y que dependerá del tipo de objeto que se esté creando,
 * por ejemplo un CheckBox, o una Tabla.
 * @author Dynagent
 *
 */
public abstract class GComponent extends JPanel
{
	/** Atributo que guarda la información del campo	 */
    protected GFormField m_objFormField;
    /** La etiqueta del campo que se mostrará en la interfaz */
    protected JLabel m_objLabel;
    /** El componente del campo en la interfaz */
    protected JComponent m_objComponent;
    /** El componente secundario del campo en la interfaz */
    protected JComponent m_objComponentSec;
    
    protected IComponentListener m_componentListener;

    /**
     * Constructor de la clase
     * @param ff Es el campo leído del XML y que quiero mostrar en la interfaz
     */
    public GComponent(GFormField ff,IComponentListener componentListener)
    {
        setLayout(null);
        setFormField(ff);
        m_componentListener=componentListener;
//        setFocusTraversalPolicyProvider(true);
//		setFocusTraversalPolicy(new LayoutFocusTraversalPolicy(){
//			//
//						@Override
//						protected boolean accept(Component c) {
//							System.err.println("ENTRAAA EN PROVEEDORGCOMPONENT con "+c.getClass()+" code:"+c.hashCode());
//							Container cycleRoot = c.getFocusCycleRootAncestor();
//							FocusTraversalPolicy policy = cycleRoot.getFocusTraversalPolicy();
//							if(policy instanceof GFocusTraversalPolicy){
//								if(isFocusable(c,((GFocusTraversalPolicy)policy).isOnlyRequired()))
//									return super.accept(c);
//								else return false;
//							}else return super.accept(c);
//							//boolean accept=super.accept(c);
//							// System.err.println("accept:"+accept);
//							// return accept;//super.accept(c);
//						}
//			        	
//			        });
    }
    
    /**
     * Cambia el campo al que referencia este componente de la interfaz
     * @param ff Es el nuevo campo a referenciar
     */
    public void setFormField(GFormField ff)
    {
        m_objFormField = ff;
    }
    
    /**
     * Obtiene el campo al que hace referencia este componente de la interfaz
     * @return GFormField - El campo con la información del XML.
     */
    public GFormField getFormField()
    {
        return m_objFormField;
    }

    /**
     * Método abstracto que será implementado en las clases hijas, para crear el componente
     * específico según de qué tipo sea el campo
     *
     */
    protected abstract void createComponent() throws ParseException, AssignValueException;

    /**
     * Este método añade a la interfaz gráfica la etiqueta, el componente y el componente secundario.
     * Llama a createComponent() que nos creará los correspondientes componentes del campo.
     */
    public void create() throws ParseException, AssignValueException
    {
        m_objLabel = new JLabel(m_objFormField.getLabel());
        if(m_objFormField.isHighlighted()){
        	m_objLabel.setFont(m_objLabel.getFont().deriveFont(m_objLabel.getFont().getSize2D()*GConfigView.multiplySizeHighlightedFont));
        }
        this.add(m_objLabel);
        createComponent();
        this.add(m_objComponent);
        if(m_objComponentSec!=null)
        	this.add(m_objComponentSec);
        
        GConst.addShortCut(null, m_objComponent, GConst.INFO_SHORTCUT_KEY, GConst.INFO_SHORTCUT_MODIFIERS, "Info component", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){
        	
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				IdObjectForm idObjForm=new IdObjectForm(m_objFormField.getId());
				Integer ido=idObjForm.getIdo();
				Integer idProp=idObjForm.getIdProp();
				m_componentListener.showInformation(ido, idProp);
			}
			
		});
    }
    
    /**
     * Modifica el area de la etiqueta
     * @param rc el nuevo area a aplicar a la etiqueta
     */
    public void setLabelComponentBounds(Rectangle rc)
    {
        m_objLabel.setBounds(rc);
    }
    
    /**
     * Modifica el area del componente
     * @param rc El nuevo area a aplicar al componente
     */
    public void setComponentBounds(Rectangle rc)
    {
        m_objComponent.setBounds(rc);
    }
    
    /**
     * Modifica el area del componente secundario
     * @param rc el nuevo area a aplicar al componente secundario
     */
    public void setComponentSecundarioBounds(Rectangle rc)
    {
    	if(m_objComponentSec!=null)
    		m_objComponentSec.setBounds(rc);
    }
    
    /**
     * Obtiene el componente de la interfaz correspondiente a la etiqueta
     * @return JLabel - La etiqueta del campo en la interfaz
     * @see JLabel
     */
    public JLabel getLabelComponent()
    {
        return m_objLabel;
    }
    
    /**
     * Obtiene el componente de la interfaz correspondiente al componente del campo
     * @return JComponent - El componente del campo en la interfaz
     * @see JComponent
     */
    public JComponent getComponent()
    {
        return m_objComponent;
    }

    /**
     * Obtiene el componente de la interfaz correspondiente al componente secundario del campo
     * @return JComponent - El componente secundario del campo en la interfaz
     * @see JComponent
     * */
    public JComponent getComponentSecundario(){
    	return m_objComponentSec;
    }
    
    public abstract boolean newValueAllowed();
    
}
