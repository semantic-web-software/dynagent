package gdev.gawt;

import gdev.gbalancer.GProcessedField;
import gdev.gbalancer.GProcessedForm;
import gdev.gbalancer.GProcessedGroup;
import gdev.gen.AssignValueException;
import gdev.gen.IComponentFactory;

import java.awt.Rectangle;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Esta es la clase principal que crea (gráficamente) todos los campos del formulario y la
 * interfaz donde se mostrarán. Una vez creada esta interfaz, en el paquete gdev.gbalancer se tratará
 * de optimizar esta interfaz para que se vea lo mejor posible.
 * Esta clase tiene aplicado el patrón Factory para la creación.
 */
public class GSimpleForm extends JPanel
{
	private static final long serialVersionUID = 1L;

	/** Atributo para almacenar el formulario ya procesado */
    protected GProcessedForm m_objProcessedForm;

    /** Atributo para la creación del formulario, con el patrón factory*/
    protected IComponentFactory m_factory;
   
    /**
     * El constructor de la clase principal.
     * @param objProcessedForm El formulario entero ya procesado.
     * @throws ParseException 
     * @throws AssignValueException 
     */
    public GSimpleForm(GProcessedForm objProcessedForm,IComponentFactory factory) throws ParseException, AssignValueException
    {
        m_objProcessedForm = objProcessedForm;
        m_factory=factory;
        setLayout(null);
        setBorder(BorderFactory.createEmptyBorder());
      
      
        /*add(buildForm());*/
        buildForm();
    }
    /**
     * Este método construye el formulario. Todos los grupos y campos serán creados y
     * añadidos a la interfaz.
     * @throws ParseException 
     * @throws AssignValueException 
     */
    protected /*JPanel*/void buildForm() throws ParseException, AssignValueException
    {
    	/*Creamos este panel para que el formulario aparezca centrado ya que, si es mas pequeño
		  que el espacio disponible, aparece en una esquina*/
		/*JPanel panelAux=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
		panelAux.setLayout(null);
		panelAux.setBackground(m_factory.getColorFondo());*/
		
       
    	setPreferredSize(m_objProcessedForm.getBounds().getSize());
        Vector vGroups = m_objProcessedForm.getProcessedGroupList();
        m_factory.setParent(this);
        Enumeration enumGrp = vGroups.elements();
        while(enumGrp.hasMoreElements())
        {
            GProcessedGroup oneGroup = (GProcessedGroup)enumGrp.nextElement();
            if(!oneGroup.getProcessedFieldList().isEmpty()){
	            JPanel pnlGroup = buildGroup(oneGroup);
	           
	            /*panelAux.add(pnlGroup);*/
	            add(pnlGroup);
            }
        }
        /*return panelAux;*/
    }
    /**
     * Este método crea un grupo y los campos que contiene dicho grupo.
     * También genera los bordes del grupo, si es que necesita estos bordes.
     * @param oneGroup La información del grupo procesado y que queremos insertar en la interfaz.
     * @return JPanel - La interfaz para ese grupo ya creada.
     * @throws ParseException 
     * @throws AssignValueException 
     * @see JPanel
     */
    protected JPanel buildGroup(GProcessedGroup oneGroup) throws ParseException, AssignValueException
    {
        JPanel pnlGroup = new JPanel();
        pnlGroup.setLayout(null);

        if(oneGroup.getFormGroup().getId()!=0)
        {
            TitledBorder title;
            title = BorderFactory.createTitledBorder(null,oneGroup.getFormGroup().getLabel(),TitledBorder.CENTER,TitledBorder.TOP);
            pnlGroup.setBorder(title);
        }
        pnlGroup.setBounds(oneGroup.getBounds());
  
        Vector vFields = oneGroup.getProcessedFieldList();
        Enumeration enumFld = vFields.elements();
        while(enumFld.hasMoreElements())
        {
            GProcessedField oneField = (GProcessedField)enumFld.nextElement();
            GComponent field = buildField(oneField);
            if(field!=null)
                pnlGroup.add(field);
        }
        return pnlGroup;
    }
    
    /**
     * Este método crea un campo concreto, dependiendo del tipo que sea.
     * La creación se hace en el Factory.
     * @param oneField - Es el campo concreto a construir para mostrar en la interfaz
     * @return GComponent - Es el campo creado (un componente de la interfaz)
     * @throws ParseException 
     * @throws AssignValueException 
     * @see GComponent
     */
    
    protected GComponent buildField(GProcessedField oneField) throws ParseException, AssignValueException
    {
        GComponent component = null;
        /*        System.out.println("Campo:"+oneField.getFormField().getLabel());
        switch(oneField.getFormField().getType())
        {

	        case GConst.TM_TABLE:
	            component = new GTable(oneField.getFormField());
	            break;
	        case GConst.TM_ENUMERATED:
	            component = new GComboBox(oneField.getFormField());
	            break;
	        case GConst.TM_IMAGE:
	            break;
	        case GConst.TM_BOOLEAN:
	        case GConst.TM_BOOLEAN_COMMENTED:
	            component = new GCheckBox(oneField.getFormField());
	            break;
	        case GConst.TM_MEMO:
	            component = new GEdit(oneField.getFormField());//for the time being
	            break;
	        case GConst.TM_INTEGER:
	        case GConst.TM_REAL:
	        case GConst.TM_DATE:
	        case GConst.TM_TEXT:
	            component = new GEdit(oneField.getFormField());
	            break;
	        default:
	            break;
        }*/
        component=m_factory.create(oneField);
        /*ESTO QUE LO HAGA FACTORYGRAFICO O AQUI. ESTA POR DECIDIR*/
               
        if(component==null)
            return component;
        component.create();
        Rectangle rc = oneField.getBounds();
        component.setBounds(new Rectangle(rc));
        component.setLabelComponentBounds(oneField.getLabelBounds());
        component.setComponentBounds(oneField.getComponentBounds());
        component.setComponentSecundarioBounds(oneField.getComponentSecundarioBounds());
/*        if(component instanceof GTable){
        	/*Suponemos que la etiqueta empieza en 0,0 y que esta colocada encima de la tabla. Habria que tener en cuenta otros casos*/
/*        	Dimension dBotonera=oneField.getLabelBounds().getSize();
        	Dimension dComponente=oneField.getComponentBounds().getSize();
        	
        	int anchoLabel=(int)dBotonera.getWidth();
        	int altoLabel=(int)dBotonera.getHeight();
        	int anchoComponente=(int)dComponente.getWidth();
        	int altoComponente=(int)dComponente.getHeight();
        	
        	if(oneField.getFormField().isTopLabel()){
        		/*Rectangle rcBotonera=new Rectangle(anchoLabel,0,anchoComponente-anchoLabel,altoLabel);
        		((GTable)component).setBotoneraComponentBounds(rcBotonera);*/
/*        		((GTable)component).setBotoneraComponentBounds(oneField.getComponentSecundarioBounds());
        	}else{
        		/*Rectangle rcBotonera=new Rectangle(anchoLabel,0,((GFormTable)oneField.getFormField()).getAnchoBoton(),altoLabel);
        		((GTable)component).setBotoneraComponentBounds(rcBotonera);*/
/*        		((GTable)component).setBotoneraComponentBounds(oneField.getComponentSecundarioBounds());
        	}
        	
        }
*/        return component;
    }
}
