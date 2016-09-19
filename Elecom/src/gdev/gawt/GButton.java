package gdev.gawt;

//import gdev.gawt.GEdit.TextFormVerifier;
import gdev.gfld.GFormButton;
import gdev.gfld.GFormField;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import dynagent.common.utils.IUserMessageListener;

/**
 * Esta clase extiende a GComponent y creará un botón.
 * Una vez creado se podrá representar en la interfaz gráfica.
 * @author Francisco
 */
public class GButton extends GComponent{

	private static final long serialVersionUID = 1L;
	String currValue, initialValue;
	NumberFormat m_nf= NumberFormat.getInstance();
	String mask=null;
	String idForm;
	boolean changed=false;
	String m_id2=null;
	Font m_font;
	ActionListener m_control;
	protected JTextComponent editor=null;
	/*String m_label;*/
	String m_text;
	boolean m_nullable;
	boolean m_modoFilter;
	boolean m_enabled=true, m_modoConsulta;

	Insets m_ins;
	Object m_defaultVal;

	int m_rows;
	int m_cols;
	int m_altoLinea;
	int m_sintax;
	Color m_colorFondo;
	String m_targetType;
	String m_par1;
	private KeyListener keyListener;

	/**
	 * Constructor de la clase
	 * @param ff
	 * @param targetType
	 * @param control
	 * @param fuente
	 */
	public GButton(GFormField ff,String targetType,ActionListener control,IUserMessageListener messageListener,Font fuente,KeyListener keyListener)
	{
		super(ff,null);


		////////////OBTENCION DE ATRIBUTOS//////////////////
		//boolean comentado=ff.isCommented();
		String formId=ff.getId();
		/*String label=ff.getLabel();*/
		String text=((GFormButton)ff).getText();
		//boolean topLabel=ff.isTopLabel();
		int sintax=ff.getType();
		Object defaultVal=ff.getDefaultVal();
		String id2=ff.getId2();
		boolean nullable=ff.isNullable();
		//Color color=null;
		boolean enabled=ff.isEnabled();
		//boolean multivalued=ff.isMultivalued();
		int rows=ff.getRows();
		int altoLinea=(int)ff.getRowHeight();
		int cols=ff.getCols();
		//Insets ins= ff.getInternalPaddingEdit();
		String mask=ff.getMask();
		////////////////////////////////////////////////////


		m_enabled=enabled;
		m_defaultVal=defaultVal;

		m_rows=rows;
		m_cols=cols;
		m_altoLinea=altoLinea;
		//System.out.println("GETEXT:"+parsedText+","+format(parsedText));
		m_sintax=sintax;
		/*m_label = label;*/
		m_text=text;
		m_nullable=nullable;
		m_control = control;
	

		this.idForm=formId;
		this.mask=mask;

		m_id2=id2;

		m_targetType=targetType;

		m_par1=null;
		this.keyListener=keyListener;
	}

	protected void createComponent()
	{
		AbstractButton boton= null;
		/*if(m_label.equals("{..}")) boton= new JToggleButton(m_label);
		else boton= new JButton(m_label);*/
		if(m_text.equals("{..}")) boton= new JToggleButton(m_text);
		else boton= new JButton(m_text);
		//formato del comando targetType:target:index:targetSubtype

		/*String command= 	m_targetType+":"+
		String.valueOf(id)+":"+
		id+":"+
                  		m_label;*/
		String command= m_targetType+":"+String.valueOf(idForm)+":"+idForm+":"+m_text;
		/*String command= 	m_targetType+":"+
					String.valueOf(m_id)+":"+
					id+":"+
                              		m_label;*/


		if(m_par1!=null) command+=":"+m_par1;
		boton.setActionCommand(command);
		/*if(m_label.equals("OK")) m_okBoton= (JButton) boton;*/
		boton.addActionListener(m_control);
		boton.addKeyListener(keyListener);

		if( m_colorFondo!=null )
			setBackground( m_colorFondo );

		m_objComponent = boton;
	}
	
	@Override
	public boolean newValueAllowed() {
		return false;
	}

}
