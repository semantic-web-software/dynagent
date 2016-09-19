package gdev.gawt;

import gdev.gawt.utils.TristateCheckBox;
import gdev.gawt.utils.UtilsFields;
import gdev.gen.AssignValueException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;
import gdev.gfld.GFormField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import dynagent.common.communication.docServer;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;



/**
 * Esta clase extiende a GComponent y creará el CheckBox (tanto comentado como el no comentado).
 * Una vez creado se podrá representar en la interfaz gráfica.
 * @author Francisco
 */
public class GCheckBox extends GComponent implements IComponentData, /*field,*/ ActionListener, FocusListener
{   //private final Component c= this;
	private static final long serialVersionUID = 1L;
	Boolean currValue=null, oldValue, initialValue;
	String currTxtValue=null, oldTxtValue, initialTxtValue;
	String m_id;
	boolean changed=false;
	String m_id2=null;
	/*fieldControl m_control;*/
	String m_label;
	String m_name;
	boolean m_nullable;
	TristateCheckBox m_check;
	JTextField m_text;
	docServer m_server;
	Font m_font;
	boolean m_enabled, m_modoConsulta, m_modoFilter;
	/*session m_session;*/

	boolean m_comentado;
	int m_sintax;
	String m_defaultVal;
	Color m_color;
	Boolean valueCheck=null,valueCheckOld=null;
	
	Border borderCheckBox;
	IUserMessageListener m_messageListener;

	/**
	 * Constructor
	 * @param ff
	 * @param ses
	 * @param com
	 * @param control
	 * @param fuente
	 * @param modoConsulta
	 * @throws ParseException
	 */
	public GCheckBox(GFormField ff,/*session ses,*/docServer server,/*fieldControl control,*/IComponentListener controlValue,IUserMessageListener messageListener,Font fuente,boolean modoConsulta,boolean modoFilter){
		/*public GCheckBox(session ses,communicator com, boolean comentado, fieldControl control, String id, String defaultVal,
			int tapos, String id2, int sintax, String label, boolean nullable,String color, Dimension dimTexto,
			boolean enabled, Color colorFondo, Font fuente, boolean modoConsulta)
    {*/
		super(ff,controlValue);
		/*m_session=ses;*/
		m_modoConsulta=modoConsulta;
		m_modoFilter=modoFilter;
		m_font=fuente;


		m_server=server;
		/*m_control = control;*/


		m_messageListener=messageListener;

		////////////OBTENCION DE ATRIBUTOS//////////////////
		boolean comentado=ff.isCommented();
		String id=ff.getId();
		String label=ff.getLabel();
		String name=ff.getName();
		//boolean topLabel=ff.isTopLabel();
		int sintax=ff.getType();
		String defaultVal=(String)ff.getDefaultVal();
		String id2=ff.getId2();
		boolean nullable=ff.isNullable();
		Color color=null;
		boolean enabled=ff.isEnabled();
		////////////////////////////////////////////////////


		m_color=color;
		m_enabled = enabled;
		m_comentado = comentado;
		m_defaultVal = defaultVal;
		m_sintax = sintax;

		m_label=label;
		m_name = name;
		m_nullable=nullable;

		m_id=id;

		m_id2=id2;
		/*System.out.println("COMMENT:"+comentado+","+dimTexto);
    	m_defaultText=null;
    	Boolean defaultBool= helperConstant.parseBooleanValue( sintax, defaultVal );

    	if( m_comentado )
    		m_defaultText= helperConstant.parseBooleanTextValue( sintax, defaultVal );

    	setBorder( new EmptyBorder(0,0,0,0));
    	//setBackground( Color.RED );
    	FlowLayout fl= (FlowLayout)getLayout();
    	if(fl!=null){
    		fl.setHgap(0);
    		fl.setVgap(0);
    	}*/

		/*m_check= new PTristateCheckBox(){
    		public void paint(Graphics g){
    			if( m_com==null )
    				return;
    			TristateButtonModel model=(TristateButtonModel)m_check.getModel();
    			if( model.isTristate() )
    				g.drawImage(m_com.getImage("nulo"),2,2,null);
    			else super.paint(g);
    		}
    	};
    	if( m_com!=null ){
    		m_check.setIcon( new ImageIcon(m_com.getImage("uncheck")) );
    		m_check.setSelectedIcon( new ImageIcon(m_com.getImage("check")) );
    	}
    	m_check.addActionListener(this);
    	m_check.setAlignmentY(Component.CENTER_ALIGNMENT);
    	m_check.setMargin(new Insets(0,0,0,0));
    	m_check.setEnabled( m_enabled );
    	((TristateButtonModel)m_check.getModel()).setTristate(true);
    	add( m_check );
    	if( comentado ){
    		m_text= new JTextField(defaultText);
    		//m_text.setFont(m_font);
         		m_text.addFocusListener(this);
    		m_text.setAlignmentY(Component.CENTER_ALIGNMENT);
    		m_text.setEnabled( m_enabled );
    		add( m_text );
    		if( dimTexto!=null ){
    			m_text.setMinimumSize( dimTexto );
    			m_text.setPreferredSize( dimTexto );
    		}
    	}
    	setValue(false, defaultBool);
    	m_label=label;
      	m_nullable=nullable;
    	m_idProp= tapos;

    	this.idForm=id;

    	oldValue= defaultBool;
    	initialValue= defaultBool;

    	oldTxtValue= defaultText;
    	initialTxtValue= defaultText;
    	currTxtValue= initialTxtValue;

    	m_idRoot=id2;
    	m_stx= sintax;
    	if(color!=null){
    		if(color.equals("BLUE")) setForeground(Color.blue);
    		if(color.equals("GREEN")) setForeground(Color.green);
    		if(color.equals("RED"))	setForeground(Color.red);
    		Font f= getFont();
    		setFont(f.deriveFont(Font.BOLD));
    	}*/
	}

	protected void createComponent() throws ParseException, AssignValueException
	{
		/*m_objComponent = new JCheckBox();*/

		int height=(int)m_objFormField.getMinimumComponentDimension().getHeight();
		int heightIcon=height-(int)Math.round(height*GConfigView.reductionSizeImageCheck);
		
		m_check= new TristateCheckBox(m_server,heightIcon,heightIcon){			
			private static final long serialVersionUID = 1L;
			public void processMouseEvent(MouseEvent me){
				if( !m_modoConsulta)
					super.processMouseEvent(me);
			}
		};
		
		InputMap im = m_check.getInputMap(JTable.WHEN_FOCUSED);
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		m_check.getActionMap().remove(im.get(enter));
		
		borderCheckBox=m_check.getBorder();
		
		m_check.addFocusListener(this);
		
		/*if( m_com!=null ){
    		m_check.setIcon( new ImageIcon(m_com.getImage("uncheck")) );
    		m_check.setSelectedIcon( new ImageIcon(m_com.getImage("check")) );
    	}*/
		/*if(!m_modoConsulta)*/
		m_check.setName(m_name);
		m_check.addActionListener(this);
		m_check.setAlignmentY(Component.CENTER_ALIGNMENT);
		m_check.setMargin(new Insets(0,0,0,0));
		m_check.setEnabled( m_enabled );
		m_check.setFocusable( !m_modoConsulta );
		/*if(m_colorFondo!=null)
    		m_check.setBackground(m_colorFondo);
		 */
		m_check.setBackground((Color)UIManager.get("TextField.background"));

		/*m_check.setFocusable(!m_modoConsulta);*/
		//((TristateButtonModel)m_check.getModel()).setTristate(true);
		/*add( m_check );*/

		String defaultText=null;
		if( m_comentado ){
			/*JPanel panel=new JPanel();*/
			/*panel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));*/
			Dimension dimTexto = m_objFormField.getDimComponenteSecundario();
			/*Dimension dimTexto= new Dimension( 20,m_height);*/

			defaultText= UtilsFields.parseBooleanTextValue( m_sintax, m_defaultVal );

			m_text= new JTextField(defaultText);
			//m_text.setFont(m_font);
			m_text.setName(m_name+"@"+"Texto");
			m_text.addFocusListener(this);
			m_text.setAlignmentY(Component.CENTER_ALIGNMENT);
			m_text.setEnabled( m_enabled );
			m_text.setFocusable( !m_modoConsulta );
			/*panel.add(m_check);
    		panel.add( m_text );*/
			if( dimTexto!=null ){
				m_text.setMinimumSize( dimTexto );
				m_text.setPreferredSize( dimTexto );
			}
			m_objComponentSec=m_text;
			/*m_objComponent = panel;*/
		}/*else m_objComponent = m_check;*/
		setBorder( new EmptyBorder(0,0,0,0));
		//setBackground( Color.RED );
		FlowLayout fl= (FlowLayout)getLayout();
		if(fl!=null){
			fl.setHgap(0);
			fl.setVgap(0);
		}
		Boolean defaultBool=null;

		defaultBool= UtilsFields.parseBooleanValue( m_sintax, m_defaultVal );

		setValue(false, getValueToString(defaultBool, defaultText));

		oldValue= defaultBool;
		initialValue= defaultBool;
		valueCheck=defaultBool;

		oldTxtValue= defaultText;
		initialTxtValue= defaultText;
		currTxtValue= initialTxtValue;

		if(m_color!=null){
			if(m_color.equals("BLUE")) setForeground(Color.blue);
			if(m_color.equals("GREEN")) setForeground(Color.green);
			if(m_color.equals("RED"))	setForeground(Color.red);
			/*Font f= getFont();
    		setFont(f.deriveFont(Font.BOLD));*/
			setFont(m_font.deriveFont(Font.BOLD));
		}
	

		if(!m_nullable && !m_modoFilter){
			m_check.setBackground(GConfigView.colorBackgroundRequired);
			//if(m_comentado)
			//m_text.setBackground(GConfigView.colorBackgroundRequired);
		}
		m_objComponent = m_check;

	}

	public String getLabel(){
		return m_label;
	}

	public boolean hasChanged(){
		return hasChanged( initialValue, initialTxtValue );
	}

	public void commitValorInicial(){
		initialValue= currValue;
		initialTxtValue= currTxtValue;
	}

	public String getId(){
		return m_id;
	}

	public String getValueToString(){
		return getValueToString(currValue, currTxtValue);
	}

	public String getValueToString(Boolean currBool, String currTxtValue){
		if( m_sintax==GConst.TM_BOOLEAN )
			return  (currBool==null ? null:(currBool.booleanValue() ? "1":"0"));

		if( currBool==null ) return null;

		String boolPart= currBool==null ? "":(currBool.booleanValue() ? "1":"0");

		if( currTxtValue==null ) return boolPart;

		return boolPart + ":" + currTxtValue;
	}

	public void setValue(Object newValue, Object oldValue) throws ParseException, AssignValueException{
		setValue( false, newValue );
	}

	private void setValue( boolean notificar, Object value ) throws ParseException, AssignValueException{

//		if(m_sintax==GConst.TM_BOOLEAN_COMMENTED){
//		try {
//		String newTxt=UtilsFields.parseBooleanTextValue( m_sintax, (String)value );
//		Boolean newBVal= UtilsFields.parseBooleanValue( m_sintax, (String)value );

//		m_text.setText(newTxt);
//		m_check.setSelected(newBVal);
//		} catch (ParseException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}

//		}else{
//		Boolean newBVal= (Boolean)value;

//		m_check.setSelected(newBVal);
//		}

		//TristateButtonModel model=(TristateButtonModel)m_check.getModel();

		
		valueCheckOld=valueCheck;
		
		if( value==null ){
			//model.setTristate(true);
			m_check.setSelected(null);
			if( m_text!=null ) m_text.setText("");
			valueCheck=null;
		}else{

			Boolean newBVal=null;
			if(value instanceof Boolean){
				newBVal= (Boolean)value;
			}else{
				newBVal= UtilsFields.parseBooleanValue( m_sintax, (String)value );
			}

			if(m_comentado){
				String newTxt=UtilsFields.parseBooleanTextValue( m_sintax, (String)value );

				if( !UtilsFields.equals( GConst.TM_TEXT, currTxtValue, newTxt ) || !UtilsFields.equals( newBVal, currValue )){
					m_check.setSelected(newBVal.booleanValue());

					m_text.setText(newTxt);

				}

			}else{
				if( !UtilsFields.equals( newBVal, currValue ) ){
					//model.setTristate(false);
					m_check.setSelected(newBVal);
				}
			}

			m_check.revalidate();
			m_check.repaint();
			valueCheck=newBVal;
			
			
			
			
//			if( !UtilsFields.equals( newBVal, currValue ) ){
//			//model.setTristate(false);
//			m_check.setSelected(newBVal.booleanValue());
//			oldValue= currValue;
//			currValue=newBVal;
//			}else if(notificar){// Caso en el que se modifica el comentario sin haber pinchado antes en el check
//			oldValue= currValue;
//			currValue=newBVal;
//			}

//			if( m_text!=null && value instanceof String){
//			try{
//			String newTxt=UtilsFields.parseBooleanTextValue( m_sintax, (String)value );
//			if( !UtilsFields.equals( GConst.TM_TEXT, currTxtValue, newTxt ) ){
//			oldTxtValue= currTxtValue;
//			currTxtValue= newTxt;
//			m_text.setText(newTxt);
//			}/*else if(notificar){
//			oldTxtValue= currTxtValue;
//			currTxtValue= newTxt;
//			}*/
//			}catch(ParseException pe){
//			Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),pe,"ERROR SIMPLE FORM");
//			return;
//			}
//			}

		}
		//if( !m_modoConsulta && notificar ){
			handleChange(notificar);
		//}
	}

	public void focusGained(FocusEvent evt){
		if(!m_modoConsulta && !m_modoFilter){
			((JComponent)evt.getComponent()).setBorder(GConfigView.borderSelected);
		}
	}

	public void focusLost(FocusEvent evt) {
		try{
			if(!m_modoConsulta && !m_modoFilter){
				((JComponent)evt.getComponent()).setBorder(UIManager.getBorder("TextField.border"));
			}
			
			//Si nos quita el foco un componente distinto de los de GCheckBox procesamos el cambio de valor
			if(!this.isAncestorOf(evt.getOppositeComponent())){
				//System.err.println("currTxtValue:"+currTxtValue+" getText:"+m_text.getText()+" valueCheck:"+valueCheck+" currValue:"+currValue);
				//System.outprintln("FOCUS LOST");
				if ((m_text==null || UtilsFields.equals(GConst.TM_TEXT, /*oldTxtValue*/currTxtValue,  m_text.getText())) && valueCheck==currValue){
					return;
					
				}
				//He perdido el foco del campo texto, pero se supone que el campo check está actualizado
				//m_text es el campo texto, pero todavía currTxt no está actualizado con m_text
				//currValue es el valor boolenao
				Object newVal = getValueToString(valueCheck, m_text!=null?m_text.getText():null);
				//System.out.println("NEWVAL " + newVal);

				//System.outprintln("SETVAL");
				setValue(true, newVal);
			}
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al asignar valor");
			ex.printStackTrace();
		}

	}
	
	public void actionPerformed(ActionEvent ae){
		try{
			//Object source= ae.getSource();
			Object newVal=null;
			//boolean changed=false;
			Boolean newValBool=getNextValue();
			boolean notificar=true;
			if( m_text!=null ){
				newVal= getValueToString( newValBool, currTxtValue );
				if(newVal==null){
					//m_check.setSelected(false);
					//((TristateButtonModel)m_check.getModel()).setTristate(true);
					setValue( notificar, null );
				}else{
					//Asignamos en este caso los valueCheck y old ya que el foco lo ponemos sobre el texto para que desde alli se haga setValue cuando se introduzca un comentario
					valueCheckOld=valueCheck;
					valueCheck=newValBool;
					//((TristateButtonModel)m_check.getModel()).setTristate(false);
					m_check.setSelected(newValBool);
					//m_text.requestFocusInWindow();
					//notificar=false;
				}
			}else{
				newVal=newValBool;
				setValue( notificar, newVal );
			}
		
		}catch(Exception ex){
			
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex, "Error al seleccionar componente");
			ex.printStackTrace();
		}
	}

	public Boolean getNextValue(){
		//System.outprintln("valueCheck"+valueCheck);
		if( valueCheck==null ){
			return new Boolean( true );
		}else{
			if( valueCheck.booleanValue())
				return new Boolean( false );
			else if(!m_nullable && !m_modoFilter)
				return new Boolean( true );
			else return null;
		}
	}

	private void handleChange(boolean notificar) throws AssignValueException, ParseException{
		if( m_modoConsulta ) return;
		
		//System.outprintln("HANDLE CHANGE");
		//System.outprintln("IS NULL " + currValue==null);
		//System.outprintln("CURR " + currValue+","+currTxtValue );
		//System.outprintln("OLD " + oldValue+","+oldTxtValue );

		//boolean changed=hasChanged( oldValue, oldTxtValue );
		boolean changed=hasChanged(valueCheck, m_text!=null?m_text.getText():null);

		//System.err.println("CHANGE "+changed);
		if(changed){
			if( valueCheck==null ){
				oldValue= currValue;
				oldTxtValue= currTxtValue;
				currValue= null;
				currTxtValue= null;
			}else{

				if(m_comentado){
					oldTxtValue= currTxtValue;
					currTxtValue= m_text.getText();

				}
				
				oldValue= currValue;
				currValue=valueCheck;
			}
			
			if(notificar){
				String id=getId();
				/*if( m_control!=null && !m_modoConsulta )
				m_control.eventDataChanged(m_session,-1,-1,id);*/
				if( m_componentListener!=null && !m_modoConsulta){
					/*String[] buf = id.split(":");
					Integer valueCls = !buf[2].equals("null")?Integer.parseInt(buf[0]):null;;*/
					IdObjectForm idObjectForm=new IdObjectForm(id);
					Integer valueCls = idObjectForm.getValueCls();
	
					Object value=null;
					Object valueOld=null;
					if(currValue!=null || currTxtValue!=null)
						value=currValue+":"+currTxtValue;
					if(oldValue!=null || oldTxtValue!=null)
						valueOld=oldValue+":"+oldTxtValue;
					/*if(currValue!=null)
						value=""+currValue;*/
					try{
						m_componentListener.setValueField(id,value,valueOld,valueCls,valueCls);
					}catch(AssignValueException ex){
						if(ex.getUserMessage()!=null)
							m_messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
						setValue(false,valueOld);
						throw ex;
					}catch(NotValidValueException ex){
						m_messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
						setValue(false,valueOld);
						if(m_text!=null)
							m_text.requestFocusInWindow();
					}
				}
			}
		}
	}

	private boolean hasChanged( Boolean newValue, String newTxtValue ){
		//System.err.println("CHECK HAS CHANGED:"+m_sintax+","+currValue+","+oldValue+","+currTxtValue+","+oldTxtValue);
		if( currValue==null && newValue==null ) return false;
		//System.outprintln("CHECK2");
		if(	newValue!=null && currValue==null ||
				newValue==null && currValue!=null ) return true;
		else{
			//System.outprintln("CHECK3");
			if( !newValue.equals(currValue) ) return true;
			//System.outprintln("CHECK4");
			if( 	 currTxtValue!=null && currTxtValue.length()>0 && ( newTxtValue==null || newTxtValue.length()==0 )  ||
					( currTxtValue==null || currTxtValue.length()==0 ) && ( newTxtValue!=null && newTxtValue.length()>0 ) )
				return true;
			if((currTxtValue==null || currTxtValue.length()==0) && (newTxtValue==null || newTxtValue.length()==0))
				return false;
			else return !currTxtValue.equals(newTxtValue);
		}
	}
	
	/*private boolean hasChanged( Boolean oldValue, String oldTxtValue, Boolean newValue, String newTxtValue ){
		//System.err.println("CHECK HAS CHANGED:"+m_sintax+","+currValue+","+oldValue+","+currTxtValue+","+oldTxtValue);
		if( newValue==null && oldValue==null ) return false;
		//System.outprintln("CHECK2");
		if(	oldValue!=null && newValue==null ||
				oldValue==null && newValue!=null ) return true;
		else{
			//System.outprintln("CHECK3");
			if( !oldValue.equals(newValue) ) return true;
			//System.outprintln("CHECK4");
			if( 	 newTxtValue!=null && newTxtValue.length()>0 && ( oldTxtValue==null || oldTxtValue.length()==0 )  ||
					( newTxtValue==null || newTxtValue.length()==0 ) && ( oldTxtValue!=null && oldTxtValue.length()>0 ) )
				return true;
			if((newTxtValue==null || newTxtValue.length()==0) && (oldTxtValue==null || oldTxtValue.length()==0))
				return false;
			else return !newTxtValue.equals(oldTxtValue);
		}
	}*/

	public void initValue() throws ParseException, AssignValueException {
		setValue(true,m_defaultVal);
		valueCheck=initialValue;
	}
	
	@Override
	public boolean newValueAllowed() {
		return valueCheck==null;
	}

	public Object getValue() {
		Object value=currValue;
		if(m_comentado)
			value=value+":"+currTxtValue;
		
		return value;
	}

	public void clean() throws ParseException, AssignValueException {
		setValue(false,null);
	}
}
