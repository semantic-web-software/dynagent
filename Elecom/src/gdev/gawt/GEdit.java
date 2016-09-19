package gdev.gawt;

import gdev.gawt.utils.ButtonPopup;
import gdev.gawt.utils.Finder;
import gdev.gawt.utils.FinderPopUp;
import gdev.gawt.utils.TextVerifier;
import gdev.gawt.utils.UtilsFields;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.AssignValueException;
import gdev.gen.DictionaryWord;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gen.IDictionaryFinder;
import gdev.gen.NotValidValueException;
import gdev.gfld.GFormField;
import gdev.gfld.GFormFile;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import miniCalendar.DateRangeListener;
import miniCalendar.IDateListener;
import miniCalendar.JCalendar;
import miniCalendar.JCalendarDateTime;
import miniCalendar.JCalendarRange;
import dynagent.common.Constants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.docServer;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.SwingWorker;

/**
 * Esta clase extiende a GComponent y creará un campo de texto.
 * Una vez creado se podrá representar en la interfaz gráfica.
 * @author Juan
 * @author Francisco
 */
public class GEdit extends GComponent implements /*IDateListener,*/IComponentData,/*field,*/ FocusListener,ActionListener, KeyListener
{
	private static final String TEXT_CONTAINS="<Buscar conteniendo 'VALOR'>";//Valor es cambiado al usarse, por el valor que acaba de escribir el usuario
	private static final String EVENT_CONTAINS="contains";
	/*final Component c=this;
	final Dialog dialog=(Dialog) SwingUtilities.getWindowAncestor(c);
	final Cursor cur=dialog.getCursor();*/
	private static final long serialVersionUID = 1L;
	String currValue, oldValue, initialValue;
	NumberFormat m_nf= NumberFormat.getInstance();
	String mask=null;
	String m_id;
	boolean changed=false;
	String m_id2=null;
	int m_sintax;
	Integer redondeo;
	Font m_font;
	/*fieldControl m_control;*/
	protected JTextComponent editor=null;
	String m_label;
	String m_name;
	boolean m_nullable;
	boolean m_modoFilter;
	boolean m_enabled=true, m_modoConsulta;
	/*session m_session;*/

	Insets m_ins;
	String m_defaultVal;
	Color m_color;
	int m_rows;
	int m_cols;
	int m_altoLinea;
	Color m_colorFondo;
	KeyListener m_keyListener;
	TextVerifier textVerifier;
	boolean m_multivalued;
	/*JPopupMenu popupDate;*/
	//JDialog dialogDate;
	JFrame frameDate;
	docServer m_server;
	JCalendar calendar;
	JCalendar calendarSec;
	JComponent componentParent;
	//boolean verifyPass;
	
	private FinderPopUp finderPopUp = null;
	private SwingWorker thread = null;
	private Finder finder = null;
	private boolean listFinderHasFocus;
	private TreeMap<String, DictionaryWord> dicParc;
	private IDictionaryFinder dictionaryFinder;
	private IUserMessageListener m_messageListener;
	private JPopupMenu m_menuBotonera;
	
	class TextFormVerifier extends TextVerifier{
		TextFormVerifier(String mask,int sintax){
			super(mask,sintax,m_modoFilter);
		}

		public boolean verify(JComponent input) {
			if( !m_enabled ) return false;
			//System.outprintln("IN VERIFY:"+currValue+","+editor.getText()+" MASK:"+mask);
			//JTextComponent text= (JTextComponent) input;
			if(!hasChanged(currValue)) return true;
			//System.outprintln("IN VERIFY SI CHANGED");
//			text.setInputVerifier(null);

			boolean exito= super.verify(input);
			//System.outprintln("EXITO VERIFY:"+exito+","+editor.getText());

			/*			if(exito && m_control!=null ){
				exito = m_control.changeRequest(m_session,-1,-1,getIdForm(), getValue());
			}
			 */				//System.outprintln("EXITO VERIFY2:"+exito+","+editor.getText());
			 if(!exito){
				 /*final JComponent inputThis=input;
				Runnable runnable = new Runnable() { 
				      public void run() { 
//				*/    	  //String msg="Ha escrito un valor incorrecto en el campo "+ m_label;
//				 if(!isNullable() && isNull())
//				 msg+= "EL CAMPO "+ m_label + " NO ADMITE VALORES NULOS";
//				 m_messageListener.showErrorMessage(msg);
//				 reset();
				 /*       } 
				     }; 


				     SwingUtilities.invokeLater(runnable); 
				  */
				 /*String msg="Ha escrito un valor incorrecto en el campo "+ m_label;
		               	if(!isNullable() && isNull())
                	            msg+= "EL CAMPO "+ m_label + " NO ADMITE VALORES NULOS";
				 m_messageListener.showMessage(msg);
				reset();*/;
			 }else{
				 if( 	m_sintax==GConst.TM_INTEGER ||
						 m_sintax==GConst.TM_REAL )
					 setText( editor.getText(), false );
			 }

			 //return exito;//si retorna true, pasa a la funcion actionPerformed que gestiona el cambio
//			 if( exito ) handleChange( true );
			 return exito;
		}

		public boolean shouldYieldFocus(JComponent input){
			try{
				if(!verify(input)){
					//input.setInputVerifier(null);
					String msg=buildMessageError(m_label);
					if(!isNullable() && isNull())
						msg+= ". EL CAMPO "+ m_label + " NO ADMITE VALORES NULOS";

					Window wind=SwingUtilities.windowForComponent(input);

					//Asigno como valor el actual ya que al mostrar la ventana de error los metodos
					//de captura de eventos actionPerformed o focusLost se llaman sin esperar el return
					//de este metodo. Al ponerle el valor actual, como no habra habido cambios en el
					//valor del componente no se hara nada
					if(sintax==GConst.TM_REAL && !m_modoFilter){
						setText(redondear(currValue));
					}else{
						/*verifyPass=false;*/setText(currValue);//reset();
					}
					 m_messageListener.showErrorMessage(msg,wind);
					input.setInputVerifier(this);
					return false;
				}//else verifyPass=true;
				
				if(input.hasFocus()){
					return !listFinderHasFocus;
				}
			}catch(Exception ex){
				m_server.logError(SwingUtilities.getWindowAncestor(input),ex, "Error al verificar si el valor es correcto");
				ex.printStackTrace();
			}
			return true;	
		}
	}

	/*public Edit( session ses,
    		boolean multivalued,
    		Font fuente,
    		fieldControl control,
    		String id,
    		String  text,
    		int tapos,
    		String id2,
    		int sintax,
    		String mask,
    		int  rows,
    		int cols,
    		String label,
    		boolean nullable,
    		String color,
    		int altoLinea,
    		boolean modoFilter,
    		Insets ins,
    		boolean enabled,
    		boolean modoConsulta){*/

	public GEdit(GFormField ff,/*session ses,*/docServer server,/*fieldControl control,*/IComponentListener controlValue,IUserMessageListener messageListener,KeyListener keyListener,Font fuente,boolean modoConsulta,boolean modoFilter,JComponent componentParent,IDictionaryFinder dictionaryFinder)
	{
		super(ff,controlValue);
		/*m_session=ses;*/
		m_modoConsulta=modoConsulta;
		m_font=fuente;
		m_messageListener=messageListener;
		//verifyPass=true;
		////////////OBTENCION DE ATRIBUTOS//////////////////
		//boolean comentado=ff.isCommented();
		String id=ff.getId();
		String label=ff.getLabel();
		String name=ff.getName();
		//boolean topLabel=ff.isTopLabel();
		int sintax=ff.getType();
		Object defaultVal=ff.getDefaultVal();
		String id2=ff.getId2();
		boolean nullable=ff.isNullable();
		Color color=null;
		boolean enabled=ff.isEnabled();
		boolean multivalued=ff.isMultivalued();
		int rows=ff.getRows();
		int altoLinea=(int)ff.getRowHeight();
		int cols=ff.getCols();
		Insets ins= ff.getInternalPaddingEdit();
		String mask=ff.getMask();
		Integer redondeo=ff.getRedondeo();
		////////////////////////////////////////////////////


		m_enabled=enabled;
		this.redondeo=redondeo;
		m_sintax= sintax;
		m_modoFilter=modoFilter;
		m_multivalued= multivalued;
		m_defaultVal=defaultVal!=null?defaultVal.toString():null;
		m_color=color;
		m_rows=rows;
		m_cols=cols;
		m_altoLinea=altoLinea;
		//System.out.println("GETEXT:"+parsedText+","+format(parsedText));

		m_label = label;
		m_name = name;
		m_nullable=nullable;
		/*m_control = control;*/
	
		m_ins=ins;
		m_id=id;
		this.mask=mask;
		m_keyListener=keyListener;

		m_id2=id2;
		m_server=server;
		this.componentParent=componentParent;
		this.dictionaryFinder=dictionaryFinder;
	}
	
	protected void createComponent()
	{
		textVerifier=new TextFormVerifier(mask, m_sintax);

		String text=m_defaultVal;
		if(text!=null)
			text=textVerifier.format(text);
		currValue= text;
		initialValue= text;
		/*m_objComponent = new JTextField();*/
		if( m_colorFondo!=null )
			setBackground( m_colorFondo );

		if(m_label.compareToIgnoreCase("PASSWORD")==0 || m_label.compareToIgnoreCase("*PASSWORD")==0){
			editor= new JPasswordField(textVerifier.format(text));
			m_objComponent = editor;
		}else{
			if(m_rows>1){
				editor= new JTextArea(textVerifier.format(text), m_rows*m_altoLinea,m_cols);
				/*editor= new JTextArea(format(parsedText));*/
				((JTextArea)editor).setLineWrap(true);
				((JTextArea)editor).setWrapStyleWord(true);
				JScrollPane scroll=new JScrollPane(editor);
				scroll.getVerticalScrollBar().setUnitIncrement(GConfigView.IncrementScrollVertical);
				m_objComponent = scroll;
			}else{
				if(m_sintax==GConst.TM_REAL && redondeo!=null && text!=null){
					editor = new JTextField(Auxiliar.redondea(Double.valueOf(textVerifier.format(text)),redondeo).toString());
				}else{
					editor = new JTextField(textVerifier.format(text));
				}
				m_objComponent = editor;
			}
		}
		editor.setName(m_name);
		if(!m_modoConsulta)
			undoRedoEnabled();//Habilitamos el hacer-deshacer
			
		//editor.setFont(m_font);
		editor.setMargin(m_ins);
		if(m_color!=null){
			if(m_color.equals("BLUE")) editor.setForeground(Color.blue);
			if(m_color.equals("GREEN")) editor.setForeground(Color.green);
			if(m_color.equals("RED"))	editor.setForeground(Color.red);
			Font f= editor.getFont();
			editor.setFont(f.deriveFont(Font.BOLD));
		}
		editor.setInputVerifier(textVerifier);
		
		if(m_rows==1)//Si el componente tiene mas de una fila no nos interesa escuchar el evento del enter ya que es salto de linea, asi que no nos registramos
			editor.addKeyListener(this);
		
		if(m_keyListener !=null) editor.addKeyListener(m_keyListener);

		editor.setEnabled(m_enabled);
		//editor.setEditable(!m_modoConsulta);
		editor.setFocusable(!m_modoConsulta);
		editor.addFocusListener(this);
		
		if(m_objFormField.isHighlighted())
			editor.setFont(editor.getFont().deriveFont(editor.getFont().getSize2D()*GConfigView.multiplySizeHighlightedFont));

		if(!m_nullable && !m_modoFilter){
			editor.setBackground(GConfigView.colorBackgroundRequired);
		}else if(m_modoConsulta){//Si es consulta al ponerlo no editable ha cambiado de color, lo volvemos a poner con el color normal
			//editor.setBackground(UIManager.getColor("TextField.background"));
		}

		if(m_sintax==GConst.TM_DATE || m_sintax==GConst.TM_DATE_HOUR){
			/*Image img=((communicator)m_server).getImage(null,"calendar",m_altoLinea,m_altoLinea);
	    	ImageIcon icon=null;
	    	if(img!=null)
	    		icon=new ImageIcon(img);
	    	JButton selectionButton=new JButton(icon);
	    	selectionButton.setMargin(new Insets(0,0,0,0));
	    	selectionButton.setToolTipText("Calendario");
	    	selectionButton.addActionListener(this);*/

			Dimension dimButton=m_objFormField.getDimComponenteSecundario();
			JButton selectionButton=botoneraAccion.subBuildBoton(
					null,
					null,
					"calendar",
					/*
					 * "ACTION:" + 0 + ":" + m_id + ":" +
					 * botoneraAccion.ABRIR + ":" + m_label
					 *//* commandString */GTable.BUTTON_ONE_FILE,
					 "Calendario", "calendario@"+m_name, this, dimButton.width,dimButton.height,true,m_server);
			if(m_modoConsulta){
				selectionButton.setFocusable(false);
				MouseListener[] listeners=selectionButton.getMouseListeners();
				int numListeners=listeners.length;
				for(int i=0;i<numListeners;i++)
					selectionButton.removeMouseListener(listeners[i]);
			}
			m_objComponentSec=selectionButton;
		}else if(m_sintax==GConst.TM_REAL || m_sintax==GConst.TM_INTEGER){
			if(editor instanceof JTextField)
				((JTextField)editor).setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		}else if(m_sintax==GConst.TM_FILE){
			m_objComponent.setFocusable(false);
			if(((GFormFile)getFormField()).isDirectoryType()){
				Dimension dimButton=m_objFormField.getDimComponenteSecundario();
				JButton selectionButton=botoneraAccion.subBuildBoton(
						null,
						null,
						"folder",
						/*
						 * "ACTION:" + 0 + ":" + m_id + ":" +
						 * botoneraAccion.ABRIR + ":" + m_label
						 *//* commandString *//*GTable.BUTTON_ONE_FILE*/""+botoneraAccion.BUSCAR,
						 "Directorio", "directorio@"+m_name, this, dimButton.width,dimButton.height,true,m_server);
				if(m_modoConsulta){
					selectionButton.setFocusable(false);
					MouseListener[] listeners=selectionButton.getMouseListeners();
					int numListeners=listeners.length;
					for(int i=0;i<numListeners;i++)
						selectionButton.removeMouseListener(listeners[i]);
				}
				m_objComponentSec=selectionButton;
			}else{
				
				Dimension dimButton=m_objFormField.getDimComponenteSecundario();
				
				JPanel buttonsPanel = new JPanel();

				buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				JButton boton1 = botoneraAccion.subBuildBoton(buttonsPanel, null, "view", ""+botoneraAccion.CONSULTAR,
						"Ver", dimButton.width,dimButton.height,true,m_server);
				boton1.addActionListener(this);
				
				if(m_enabled && !m_modoConsulta && !m_modoFilter ){
					//Si tenemos que crear mas botones a parte de 'ver', creamos un boton de acción que abre un popupMenu con todos estos botones
					m_menuBotonera = new JPopupMenu();
					m_menuBotonera.setFocusCycleRoot(true);//El ciclo del foco solo debe moverse por la botonera
					m_menuBotonera.add(buttonsPanel);
					
					MouseAdapter mouseListener=new MouseAdapter(){
						public void mouseClicked(MouseEvent e) {
							try{
								//System.err.println("MOUSE CLICK");
								m_menuBotonera.setVisible(false);
							}catch(Exception ex){
								ex.printStackTrace();
								m_server.logError(SwingUtilities.getWindowAncestor(m_objComponent),ex,"Error al intentar ocultar la botonera");
							}
						}
					};
					
					boton1.addMouseListener(mouseListener);
					JButton boton2 = botoneraAccion.subBuildBoton(buttonsPanel, null, "delete", ""+botoneraAccion.ELIMINAR,
							"Quitar", dimButton.width,dimButton.height,true,m_server);
					boton2.addActionListener(this);
					boton2.addMouseListener(mouseListener);
					
					JButton boton3 = botoneraAccion.subBuildBoton(buttonsPanel, null, "save", ""+botoneraAccion.EXPORT,
							"Descargar", dimButton.width,dimButton.height,true,m_server);
					boton3.addActionListener(this);
					boton3.addMouseListener(mouseListener);
					
					JButton boton4 = botoneraAccion.subBuildBoton(buttonsPanel, null, "look", ""+botoneraAccion.BUSCAR,
							"Seleccionar fichero...", dimButton.width,dimButton.height,true,m_server);
					boton4.addActionListener(this);
					boton4.addMouseListener(mouseListener);
					
					JPanel botoneraOneButton = new JPanel();

					botoneraOneButton.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					
					
					final JButton boton = botoneraAccion.subBuildBoton((JPanel) botoneraOneButton, null, "showButtons", GTable.BUTTON_ONE_FILE,
							"Acciones", "acciones@"+getFormField().getName(), dimButton.width, dimButton.height,true,m_server);
					boton.setPreferredSize(new Dimension(dimButton.width,dimButton.height));
					boton.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent ae) {
							try{
								//System.err.println("BOTON CLICK");
								if (ae.getActionCommand() == GTable.BUTTON_ONE_FILE){
									// selectRow(0);
									//if (m_objTable.getRowCount() != 0)
									//	m_objTable.setRowSelectionInterval(0, 0);
									Component component = (Component) ae.getSource();
									// Para que aparezca a la izquierda del boton y no a la derecha ya
									// que, normalmente, se saldria de la pantalla
									int x = (component.getWidth() / 2)
									- m_menuBotonera.getPreferredSize().width;
									int y = component.getHeight() / 2;
									// if(x<0)//No sirve esta comparacion porque es relativo al boton
									// asi que x sera siempre negativo,hay que comparar con la pantalla
									// principal
									// x=0;
									m_menuBotonera.show(component, x, y);
									//Es interesante que la botonera tenga el foco por si nos queremos mover con el tabulador.
									//m_menuBotonera.requestFocusInWindow(); Comentado ya que provocaba que, al cancelar el popup, la ventana intentaba darle el foco al primer componente de la ventana en vez de dejarlo en la tabla
								} else
									m_menuBotonera.setVisible(false);
							}catch(Exception ex){
								ex.printStackTrace();
								m_server.logError(SwingUtilities.getWindowAncestor(m_objComponent),ex,"Error al intentar mostrar/ocultar la botonera");
							}
						}
					});
					//boton.addFocusListener(focusListener);
					
					
					/*m_menuBotonera.addPopupMenuListener(new PopupMenuListener(){
						public void popupMenuCanceled(PopupMenuEvent ev){
							//m_objTable.clearSelection();							
						}
						public void popupMenuWillBecomeInvisible(PopupMenuEvent ev){}
						public void popupMenuWillBecomeVisible(PopupMenuEvent ev){}						
					});*/

					m_objComponentSec = botoneraOneButton;
				}else{//Si solo tenemos un boton lo ponemos directamente como componenteSecundario
					m_objComponentSec = boton1;
				}
					
			}
		}

	}
	
	// Con esto habilitamos que se pueda deshacer(Ctrl-z) y rehacer(Ctrl-y)
	private void undoRedoEnabled(){
		final UndoManager undo = new UndoManager();
	    Document doc = editor.getDocument();
	    
	    // Listen for undo and redo events
	    doc.addUndoableEditListener(new UndoableEditListener() {
	        public void undoableEditHappened(UndoableEditEvent evt) {
	            undo.addEdit(evt.getEdit());
	        }
	    });
	    
	    // Create an undo action and add it to the text component
	    editor.getActionMap().put("Undo",
	        new AbstractAction("Undo") {
	            public void actionPerformed(ActionEvent evt) {
	                try {
	                    if (undo.canUndo()) {
	                        undo.undo();
	                    }
	                } catch (CannotUndoException e) {
	                	m_server.logError(SwingUtilities.getWindowAncestor(GEdit.this),e, "Error al intentar deshacer el valor");
	    				e.printStackTrace();
	                }
	            }
	       });
	    
	    // Bind the undo action to ctl-Z
	    editor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
	    
	    // Create a redo action and add it to the text component
	    editor.getActionMap().put("Redo",
	        new AbstractAction("Redo") {
	            public void actionPerformed(ActionEvent evt) {
	                try {
	                    if (undo.canRedo()) {
	                        undo.redo();
	                    }
	                } catch (CannotRedoException e) {
	                	m_server.logError(SwingUtilities.getWindowAncestor(GEdit.this),e, "Error al intentar rehacer el valor");
	    				e.printStackTrace();
	                }
	            }
	        });
	    
	    // Bind the redo action to ctl-Y
	    editor.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
	}

	public void focusGained(FocusEvent evt){
		try{
			if(editor.getText()!=null && !editor.getText().equals("")){
				editor.selectAll();
			}
			if(!m_modoConsulta && !m_modoFilter)
				m_objComponent.setBorder(GConfigView.borderSelected);
			/*if( !m_enabled ) return;
	
	        	if(currValue!=null)
	        		setText( editor.getText() );*/
			
			finder=null;
		}catch(Exception ex){
			ex.printStackTrace();
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al quitar seleccion del valor");
		}
	}

	public void focusLost(FocusEvent evt) {
	
		try{
			//System.err.println("FOCUS:");
			editor.select(0, 0);
			if (!evt.isTemporary() && thread!=null)//Si esta construyendose evitamos que lo haga porque ya hemos hecho una seleccion
				thread.interruptLater(false);
			
			if(!m_modoConsulta && !m_modoFilter){
				if(m_rows>1)
					m_objComponent.setBorder(UIManager.getBorder("ScrollPane.border"));
				else m_objComponent.setBorder(UIManager.getBorder("TextField.border"));
			}
			
			if( !m_enabled ) return;
			handleChange(true);
			
		}catch(Exception ex){
			ex.printStackTrace();
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al asignar valor");
		}
	}
	
	public void actionPerformed(ActionEvent ae){
		try{
			//System.err.println("ACTIONPERFORMED!!!!!!!!");
			if (!m_enabled)
				return;

			if(m_sintax==GConst.TM_DATE || m_sintax==GConst.TM_DATE_HOUR)
				showCalendar();
			else if(m_sintax==GConst.TM_FILE){
				JButton boton = (JButton) ae.getSource();
	            String command = boton.getActionCommand();
	            if(command!=null && !command.isEmpty()){   
	                if (Integer.valueOf(command) == botoneraAccion.BUSCAR) {
	                	JFileChooser fileChooser=new JFileChooser();
	    				if(((GFormFile)getFormField()).isDirectoryType()){
	    					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    					fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
	    					fileChooser.setDialogTitle("Seleccionar directorio");
	    				}
	    				
	    				if(GConst.LAST_FILE_PATH!=null)//Si existe una ultima ruta a la que accedimos abrimos en esa carpeta
	                		fileChooser.setCurrentDirectory(new File(GConst.LAST_FILE_PATH));
	                	//FileNameExtensionFilter filter = new FileNameExtensionFilter("Sólo Imágenes", "jpg", "jpeg", "gif", "png");
	                	//fileChooser.setFileFilter(filter);
	                	//fileChooser.setAcceptAllFileFilterUsed(false);
	                	int status=fileChooser.showOpenDialog(m_objComponent);
	                	 if (status == JFileChooser.APPROVE_OPTION) {
	                		 if(fileChooser.getSelectedFile()!=null){
	     	                	//System.err.println("selected:"+fileChooser.getSelectedFile().getAbsolutePath());
	     	                	setValue(true, fileChooser.getSelectedFile().getAbsolutePath());
	     	                	
	     	                	GConst.LAST_FILE_PATH=fileChooser.getCurrentDirectory().getAbsolutePath();
	                     	}
	                	 }
	                }
	                else if (Integer.valueOf(command) == botoneraAccion.CONSULTAR) {
	                   if(currValue!=null){
	                	   ((communicator)m_server).showFile(currValue,false);
	                   }
	                }
	                else if (Integer.valueOf(command) == botoneraAccion.EXPORT) {
	                   if(currValue!=null){
	                	   ((communicator)m_server).showFile(currValue,true);
	                   }
	                }
	                else if (Integer.valueOf(command) == botoneraAccion.ELIMINAR) {
	                	setValue(true, null);
	                }
	            }
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al realizar la operación");
		}
	}

	private void showCalendar(){
		Calendar valueCalendar=null;
		Calendar valueCalendarSec=null;
		JCalendarRange calendarRange=null;
		if(currValue!=null){
			valueCalendar=Calendar.getInstance();
			String[] buf=currValue.split("-");
			if(buf.length>1){
				valueCalendarSec=Calendar.getInstance();
				valueCalendar.setTimeInMillis(textVerifier.timeFecha(buf[0]));
				valueCalendarSec.setTimeInMillis(textVerifier.timeFecha(buf[1])) ;
			}else valueCalendar.setTimeInMillis(textVerifier.timeFecha(buf[0]));			
		}

		if(m_modoFilter){
			int type=-1;
			if(m_sintax==GConst.TM_DATE)
				type=JCalendar.JCalendar;
			else if(m_sintax==GConst.TM_DATE_HOUR)
				type=JCalendar.JCalendarDateTime;
			calendarRange=new JCalendarRange(type, SwingUtilities.getWindowAncestor(componentParent),m_objComponentSec,valueCalendar,valueCalendarSec,null,null,m_server);
			
			final JDialog dialogSec=calendarRange.buildDialog();
			DateRangeListener listenerRange=new DateRangeListener(calendarRange){

				public void setDateSelectioned(Calendar c,JCalendar calendarComponent) {
					long milliseconds=c.getTimeInMillis();
					try{
						if(calendarRange.getCalendarInitial()==calendarComponent){
							Object newValueDesde;
							if(currValue!=null){
								String value=new String(currValue);	
								String[] buf=value.split("-");
								newValueDesde=milliseconds;
								if(buf.length>1)
									newValueDesde=newValueDesde+"-"+buf[1];
								else if(dateSecondarySelectioned)
									newValueDesde=newValueDesde+"-"+buf[0];
								else newValueDesde=newValueDesde+"-"+newValueDesde;
								//if(dateSecondarySelectioned)
								//	closeDialog();
							}else newValueDesde=milliseconds+"-"+milliseconds;
							setValue(true,newValueDesde);
							datePrimarySelectioned=true;
						}else{
							Object newValueHasta;
							if(currValue!=null){
								String value=new String(currValue);
								String[] buf=value.split("-");
								newValueHasta=milliseconds;
								if(buf.length>0)
									newValueHasta=buf[0]+"-"+newValueHasta;
								else newValueHasta=newValueHasta+"-"+newValueHasta;
								//if(datePrimarySelectioned)
								//	closeDialog();
							}else newValueHasta=milliseconds+"-"+milliseconds;

							setValue(true,newValueHasta);
							dateSecondarySelectioned=true;
						}
					}catch(Exception ex){
						m_server.logError(SwingUtilities.getWindowAncestor(GEdit.this),ex,"Error al asignar fecha");
						ex.printStackTrace();
					}
				}

				public void setAllDate(Calendar c) {
				}
			};
			calendarRange.setListenerInitial(listenerRange);
			calendarRange.setListenerEnd(listenerRange);
			dialogSec.setVisible(true);
			SwingUtilities.invokeLater(new Runnable(){

				@Override
				public void run() {
					dialogSec.toFront();
					if(!dialogSec.requestFocusInWindow()){
						dialogSec.requestFocus();
					}
				}
			});
		}else{
			if(m_sintax==GConst.TM_DATE)
				calendar=new JCalendar(SwingUtilities.getWindowAncestor(componentParent),m_objComponentSec,valueCalendar,null,true);
			else if(m_sintax==GConst.TM_DATE_HOUR)
				calendar=new JCalendarDateTime(SwingUtilities.getWindowAncestor(componentParent),m_objComponentSec,valueCalendar,null,true,m_server);
			
			final JDialog dialog=calendar.buildDialog();
			calendar.setListener(new IDateListener(){

				public void setDateSelectioned(Calendar calendar,JCalendar calendarComponent) {
					Long milliseconds=calendar.getTimeInMillis();
					try{
						setValue(true,milliseconds);
						//if(calendarComponent.getDialog()!=null)
						//	calendarComponent.getDialog().dispose();
					}catch(Exception ex){
						m_server.logError(SwingUtilities.getWindowAncestor(GEdit.this),ex,"Error al asignar fecha");
						ex.printStackTrace();
					}
				}

				public void setAllDate(Calendar c) {
				}

			});
			dialog.setVisible(true);
			SwingUtilities.invokeLater(new Runnable(){

				@Override
				public void run() {
					dialog.toFront();
					if(!dialog.requestFocusInWindow()){
						dialog.requestFocus();
					}
				}
				
				
			});
		}

	}

	private void handleChange(boolean notificar) throws AssignValueException{
		if( m_modoConsulta ) return;
		boolean changed=false;
		String textFormated=getText();
		//System.err.println("HANDLE CHANG:"+m_label+","+currValue+","+editor.getText()+","+textFormated);
		if( (currValue==null || currValue.length()==0) && isNull() ) changed=false;
		else
			if( (currValue==null || currValue.length()==0) && !isNull() ) changed=true;
			else{
				if(m_sintax==GConst.TM_REAL && !m_modoFilter){
					changed= !redondear(currValue).equals(textFormated);
				}else{
					changed= !currValue.equals(textFormated);
				}
				
			}

		//String oldValue=null;
		//if(verifyPass){
		if(changed){
			oldValue=currValue;
			setCurrValue(textFormated);
		}
		/*System.out.println("NOTIF,CHANG:"+notificar +","+ changed);*/
		if( notificar && changed){
			String id=getId();
			//System.outprintln("NOTIFICA:"+id+","+currValue+","+editor.getText()+","+textFormated);
			/*if( m_control!=null )
	        			m_control.eventDataChanged(m_session,-1,-1,id);*/
			if( m_componentListener!=null ){
				/*String[] buf = id.split(":");
	        				int valueCls=GConst.TM_TEXT;
	        				if(buf.length>3 &&  !buf[3].equals("null"))
	        					valueCls = Integer.parseInt(buf[3]);*/
				IdObjectForm idObjectForm=new IdObjectForm(id);
				Integer valueCls = idObjectForm.getValueCls();

				Object value=currValue;//editor.getText();
				Object valueOld=oldValue;
				if(m_sintax==GConst.TM_DATE || m_sintax==GConst.TM_DATE_HOUR){
					//Si se trata de fecha o fecha-horas hay que transformar los valores ya que
					//en ese caso nos comunicamos con IComponentListener usando milisegundos
					if(value!=null){
						String[] buf=value.toString().split("-");
						if(buf.length>1)
							value=String.valueOf(textVerifier.timeFecha(buf[0]))+":"+String.valueOf(textVerifier.timeFecha(buf[1])) ;
						else value=textVerifier.timeFecha((String)value) ;
					}
					if(valueOld!=null){
						String[] buf=valueOld.toString().split("-");
						if(buf.length>1)
							valueOld=String.valueOf(textVerifier.timeFecha(buf[0]))+":"+String.valueOf(textVerifier.timeFecha(buf[1])) ;
						else valueOld=textVerifier.timeFecha((String)valueOld) ;
					}
				}else if(m_sintax==GConst.TM_INTEGER || m_sintax==GConst.TM_REAL){
					if(value!=null){
						String[] buf=value.toString().split(TextVerifier.SEPARATOR_RANGE_NUM+"");
						if(buf.length==1){
							if(value.toString().contains("<>")){
								if(m_sintax==GConst.TM_INTEGER)
									value=String.valueOf(new Integer(value.toString().replaceFirst("<>", "")))+":"+String.valueOf(new Integer(value.toString().replaceFirst("<>", "")))+":"+Constants.OP_NEGATION;
								else value=String.valueOf(new Double(value.toString().replaceFirst("<>", "")))+":"+String.valueOf(new Double(value.toString().replaceFirst("<>", "")))+":"+Constants.OP_NEGATION;
							}else if(value.toString().contains("<")){
								if(m_sintax==GConst.TM_INTEGER)
									value=null+":"+String.valueOf(new Integer(value.toString().replaceFirst("<", "")));
								else value=null+":"+String.valueOf(new Double(value.toString().replaceFirst("<", "")));
							}else if(value.toString().contains(">")){
								if(m_sintax==GConst.TM_INTEGER)
									value=String.valueOf(new Integer(value.toString().replaceFirst(">", "")))+":"+null;
								else value=String.valueOf(new Double(value.toString().replaceFirst(">", "")))+":"+null;
							}else{
								if(m_sintax==GConst.TM_INTEGER)
									value=new Integer(value.toString());
								else value=new Double(value.toString());
							}
						}
					}
					if(valueOld!=null){
						String[] buf=valueOld.toString().split(TextVerifier.SEPARATOR_RANGE_NUM+"");
						if(buf.length==1){
							if(valueOld.toString().contains("<>")){
								if(m_sintax==GConst.TM_INTEGER)
									valueOld=String.valueOf(new Integer(valueOld.toString().replaceFirst("<>", "")))+":"+String.valueOf(new Integer(valueOld.toString().replaceFirst("<>", "")))+":"+Constants.OP_NEGATION;
								else valueOld=String.valueOf(new Double(valueOld.toString().replaceFirst("<>", "")))+":"+String.valueOf(new Double(valueOld.toString().replaceFirst("<>", "")))+":"+Constants.OP_NEGATION;
							}else if(valueOld.toString().contains("<")){
								if(m_sintax==GConst.TM_INTEGER)
									valueOld=null+":"+String.valueOf(new Integer(valueOld.toString().replaceFirst("<", "")));
								else valueOld=null+":"+String.valueOf(new Double(valueOld.toString().replaceFirst("<", "")));
							}else if(valueOld.toString().contains(">")){
								if(m_sintax==GConst.TM_INTEGER)
									valueOld=String.valueOf(new Integer(valueOld.toString().replaceFirst(">", "")))+":"+null;
								else valueOld=String.valueOf(new Double(valueOld.toString().replaceFirst(">", "")))+":"+null;
							}else{
								if(m_sintax==GConst.TM_INTEGER)
									valueOld=new Integer(valueOld.toString());
								else valueOld=new Double(valueOld.toString());
							}
						}
					}
				}
				//System.err.println("GEdit.setValue: tipo"+m_sintax+" value:"+value);
				/*if(value.equals(""))
	        					value=null;
	        				else if(m_stx==GConst.TM_INTEGER || m_stx==GConst.TM_REAL){
	        					value=value.trim();
	        					String[] part=value.split(/*"[\\s-:]"*//*"[-:]");
	        					if(part.length==2){// Si es un rango de valores
	        						if(part[0].length()>0 && part[1].length()>0)
	        							value=part[0].trim()+":"+part[1].trim();
	        					}
	        				}*/
				try{
					m_componentListener.setValueField(id,value,valueOld,valueCls,valueCls);
				}catch(AssignValueException ex){
					//if(ex.getUserMessage()!=null)
					//	 m_messageListener.showErrorMessage(ex.getUserMessage());
					
					setText(oldValue);
					currValue=oldValue;
					
					final Runnable focus = new Runnable() {
						public void run() {
							editor.requestFocusInWindow();
						}
					};
					SwingUtilities.invokeLater(focus);

					throw ex;
				}catch(NotValidValueException ex){
					ex.printStackTrace();
					m_messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
					setText(oldValue);
					currValue=oldValue;
					final Runnable focus = new Runnable() {
						public void run() {
							editor.requestFocusInWindow();
						}
					};
					SwingUtilities.invokeLater(focus);
				}
			}
		}
		//}
		//verifyPass=true;
	}

	/*        public void focusLost(FocusEvent evt) {
    	//System.out.println("FOCUS:");
    	if( !m_enabled ) return;
    	handleChange(true);
        }

        public void actionPerformed(ActionEvent ae){
    	//System.out.println("ACTION PERF:");
    	try{
                if (!m_enabled)return;
                handleChange(true);
            }catch(Exception ex2){
    	Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),ex2);
    	}
        }

        private void handleChange(boolean notificar){
    	if( m_modoConsulta ) return;
    	boolean changed=false;
    	System.out.println("HANDLE CHANG:"+m_label+","+currValue+","+editor.getText()+","+getText());
    	if( (currValue==null || currValue.length()==0) && isNull() ) changed=false;
    	else
    		if( (currValue==null || currValue.length()==0) && !isNull() ) changed=true;
    		else
    			changed= !currValue.equals(getText());

    	setCurrValue();
    	System.out.println("NOTIF,CHANG:"+notificar +","+ changed);
    	if( notificar && changed){
    		String id=getIdForm();
    		System.out.println("NOTIFICA:"+id+","+currValue+","+editor.getText()+","+getText());
    		try{
    		if( m_control!=null )
    			m_control.eventDataChanged(m_session,-1,-1,id);
    		}catch(Exception ex){
    		    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),ex,"Error en Text Verifier:"+ex.getClass());
    		}
    	}
        }
	 */
//	private String parseText(String text){
//	if(text==null) return null;
//	//System.out.println("TEXT:"+text);
//	String res=null;
////	if(m_sintax==/*helperConstant.TM_FECHA*/GConst.TM_DATE){
////	if(text.length()==8)
////	return text;
////	if( text.length()>=10)
////	return text.substring(0,2) + "/" + text.substring(3,5) + "/" + text.substring(8,10);
////	if(text.length()>8 && text.length()<11){
////	System.out.println("SimpleForm. ERROR EN PARSEADO de FeCHA");
////	}
////	}
////	if(m_sintax==/*helperConstant.TM_FECHAHORA*/GConst.TM_DATE_HOUR){
////	String mask= TextVerifier.getDateHourMask();
////	Pattern pat = Pattern.compile(mask);
////	Matcher match = pat.matcher(text);
////	if(match.matches())
////	return text;
////	mask="\\d{2}[/-:]\\d{2}[/-:]\\d{4}.{6,}";
////	pat = Pattern.compile(mask);
////	match = pat.matcher(text);
////	if(match.matches())
////	return text.substring(0,2) + "/" + text.substring(3,5) + "/" + text.substring(8,16);
////	else
////	System.out.println("SimpleForm. ERROR EN PARSEADO de FeCHA HORA");
////	}
//	if( m_sintax==GConst.TM_DATE){
//	long milliseconds=Long.parseLong(text);
//	Date date=new Date(milliseconds);
//	SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy");
//	text= dateFormat.format(date);
//	}else if(m_sintax==GConst.TM_DATE_HOUR){
//	long milliseconds=Long.parseLong(text);
//	Date date=new Date(milliseconds);
//	SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy HH:mm:ss");
//	text= dateFormat.format(date);
//	}

//	return text;
//	}

	public String getLabel(){
		return m_label;
	}

	public JTextComponent getEditor(){
		return editor;
	}

	/*public void commitValorInicial(){
    		initialValue= getValueToString();
    	}*/

	/*public void inizialiceRestriction() throws RemoteSystemException,CommunicationException{
    		if( m_control==null || m_modoConsulta ) return;
    		try{
    		m_control.eventDataChanged(m_session,-1,-1,id);
    		}catch(SystemException se){
    		    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),se);
    		}catch(ApplicationException se){
    		    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),se);
    		}
    	}*/

	public boolean hasChanged()
	{
		return hasChanged( initialValue );
	}
	private boolean hasChanged(String oldVal)
	{
		if(	!(oldVal instanceof String) &&
				isNull() ) return false;
		if(	!(oldVal instanceof String) &&
				!isNull() ) return true;

		if(m_sintax==GConst.TM_REAL && !m_modoFilter){
			return !redondear(oldVal).equals(getText());
		}else{
			return !oldVal.equals(getText());
		}
	}
	public String getId(){
		return m_id;
	}

	/*public String getValueToString(){
    		return getText();
    	}**/

	private String getText(){
		String txt=editor.getText();
		if(txt.length()==0)
			return null;
		if(m_modoFilter)//Si es modo filtro evitamos que espacios delante o detras fastidien la busqueda
			txt=txt.trim();
		return /*getText(*/ txt /*)*/;
	}

//	private String getText( String txt ){
//	if( 	txt==null ||
//	txt.length()==0 ) /*return txt;*/return null;

//	txt=txt.trim();

//	if( m_sintax==GConst.TM_REAL || m_sintax==GConst.TM_INTEGER){
//	if(  txt.indexOf( "E" )!=-1 ) return txt;
//	try{
//	String maskMain="\\d{1,3}(\\.\\d{3}){0,4}(,\\d*)?";
//	String mask= m_modoFilter ? (maskMain + "([\\s-:]"+maskMain+")?"):maskMain;

//	if( !txt.matches(mask) ){
//	return txt;
//	}

//	if( m_modoFilter && txt.matches(maskMain+"[\\s-:]"+maskMain) ){
//	String[] part=txt.split("[\\s-:]");
//	return m_nf.parseObject(part[0])+":"+m_nf.parseObject(part[1]);
//	}

//	Object obj= m_nf.parseObject( txt );
//	return obj.toString();
//	}catch( ParseException pe ){
//	Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),pe);
//	return null;
//	}
//	}
//	return txt;
//	}

	/*private String subFormat( String txt ) throws ParseException{
    		String mask="\\d{1,3}(\\.\\d{3}){0,4}(,\\d*)?";
    		if( !txt.matches(mask) ){
    			System.out.println("NO MATCH "+txt);
    			return m_nf.format( Double.parseDouble( txt ));
    		}else
    			System.out.println("SI MATCH");
    		return formatNum( m_nf.parseObject( txt ) );
    	}

    	private String formatNum( Object val ){
    		if( val instanceof Double ){
    			return m_nf.format( ((Double)val).doubleValue() );
    		}
    		if( val instanceof Float ){
    			return m_nf.format( ((Float)val).doubleValue() );
    		}
    		if( val instanceof Integer ){
    			return m_nf.format( ((Integer)val).intValue() );
    		}
    		if( val instanceof Long ){
    			return m_nf.format( ((Long)val).intValue() );
    		}
    		return null;

    	}*/

	/*private String format( Object val ){
    		//System.out.println("DBG1:"+val);
    		if( val==null || ( val instanceof String && ((String)val).length()==0 )){
    			return "";
    		}
    		//System.out.println("DBG2");
    		if( m_sintax==GConst.TM_REAL || m_sintax==GConst.TM_INTEGER ){
    			//System.out.println("DBG3");
    			if( val instanceof String ){
    				//System.out.println("TVAL "+val);

    				try{
    				if( m_modoFilter && ((String)val).matches(".+[\\s-:].+") ){
    					String[] part=((String)val).split("[\\s-:]");
    					return 	subFormat( part[0])+":"+
    						subFormat( part[1]);
    				}
    				return subFormat((String)val );
    				}catch(ParseException pe){
    				    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),pe);
    					return null;
    				}
    			}

    			return formatNum( val );
    		}
    		//System.out.println("DBG4");
    		if( val instanceof String ){
    			System.out.println("DBG5");
    			return (String)val ;
    		}
    		else
    			return val.toString();
    	}*/

	private void setText( Object val ){
		setText(val,true);	
	}
	
	private void setText( Object val, boolean redondear ){
		//System.outprintln(textVerifier.format(val));
		//editor.setAlignmentX(JTextComponent.LEFT_ALIGNMENT);
		//editor.setCaretPosition(0);
		String formatValue=textVerifier.format( val );
		if(redondear && m_sintax==GConst.TM_REAL && !this.m_modoFilter){
			formatValue=redondear(formatValue);
		}
		if(!UtilsFields.equals(formatValue, editor.getText())){
			//System.err.println("formatValue:"+formatValue+" curr:"+this.currValue+" text:"+this.getText());
			editor.setText(formatValue);
		}
		if(!editor.hasFocus())
			editor.getCaret().setDot(0);		
	}

	/*public Object getValue(){
    		try{
    	        System.out.println("Edit, getValue:"+m_sintax+","+getText());
    		System.out.println("Edit, getValue:"+m_sintax+","+getText()+","+helperConstant.parseValue( m_sintax, getText() ));
    		return helperConstant.parseValue( m_sintax, getText() );
    		}catch(ParseException pe){
    		    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),pe,"Edit, errorparser:"+getText()+","+pe.getMessage());
    		}
    		return null;
    	}*/

	public void setCurrValue(String txt){
		currValue= txt;
	}

	public boolean isNull(){
		//System.err.println("Editor.text:"+editor.getText());
		return (editor.getText()==null || editor.getText().length()==0 );
	}

	public boolean isNull(Object val){
		if( val==null ) return true;
		if( val instanceof String ) return ((String)val).length()==0;
		return false;
	}

	public boolean isNullable(){
		return m_nullable;
	}


	public void setValue(Object newValue,Object oldValue) throws AssignValueException {
		setValue(false,newValue);
	}

	private void setValue(boolean notificar, Object value) throws AssignValueException {
		//System.err.println("setValue:"+value);
		/*if( m_sintax== GConst.TM_TEXT ){
			String val= (String)value;
			//este codigo se utiliza para limpiar la concatenación de cadenas en codigos. Ni lo procesa
			//las funciones ni el scriptlet in-line
			if( val!=null ){
				val= val.replaceAll("\"","");
				String[] items= val.split("\\+");
				if( items.length>1 ){
					String buf= "";
					for( int i=0; i<items.length;i++)
						buf+= items[i];
					value= buf;
				}
			}
		}else */if(m_sintax== GConst.TM_INTEGER || m_sintax== GConst.TM_REAL){
			if(value instanceof String){
				String val= (String)value;
				if( val!=null ){
					if(val.contains("null:")){
						value= val.replaceAll("null:","<");
					}else if(val.contains(":null")){
						value= val.replaceAll(":null","");
						value= ">"+value;
					}else if(val.contains(":"+Constants.OP_NEGATION)){
						value= val.replaceAll(":"+Constants.OP_NEGATION,"");
						value= "<>"+val.split(":")[0];
					}
				}
			}
		}
		//System.err.println("asigna:"+value);
		setText(value);
		handleChange(notificar);
	}

	public void initValue() throws AssignValueException {
		/*editor.setText(initialValue);
			editor.repaint();*/
		//setText(initialValue);
		setValue(true,initialValue);//TODO initialValue debe ser parseado para que devuelva Integer,Double,Long...dependiendo del tipo de objeto
	}
	public void keyPressed(KeyEvent ev) {
		try{
			if(dictionaryFinder!=null && finderPopUp!=null && finderPopUp.isVisible()){
				if(ev.getKeyCode()==KeyEvent.VK_DOWN){
					finderPopUp.setSelectedNextButton();
					ev.consume();
				}else if(ev.getKeyCode()==KeyEvent.VK_UP){
					finderPopUp.setSelectedPreviousButton();
					ev.consume();
				}else if(ev.getKeyCode()==KeyEvent.VK_ENTER || ev.getKeyCode()==KeyEvent.VK_TAB){
//					finderPopUp.getSelectedButton().doClick();
					ButtonPopup b=finderPopUp.getSelectedButton();
					if(b!=null){
						String wordForUser = b.getText();
						
						DictionaryWord dw = dicParc.get(wordForUser);
						
						if(dw==null){//Se trataria de la opcion "contiene", la cual no esta en el diccionario
							setText("%"+editor.getText()+"%");
						}else{
							String text=dw.getWord();
							setText(text);
						}
						
					}
					finderPopUp.setVisible(false);
					listFinderHasFocus=false;
					InputVerifier inputV=m_objComponent.getInputVerifier();
					if(inputV.shouldYieldFocus(m_objComponent)){//Comprobamos si cumple con el verificador simulando un cambio de foco
						handleChange(true);
					}
						
				}/*else if(ev.getKeyCode()==KeyEvent.VK_ESCAPE){
					
					if(finderPopUp!=null && finderPopUp.isShowing()){
						listFinderHasFocus=false;
						editorComponent.setText(oldValue);
						finderPopUp.setVisible(false);	
						//stopCellEditing();
					}
					cancelCellEditing();
				}*/
			}else{
				//System.err.println("KEYPRESSED:");
				if (ev.getKeyCode() == KeyEvent.VK_ENTER || ev.getKeyCode()==KeyEvent.VK_TAB) {
					if( !m_enabled ) return;
	
					InputVerifier inputV=m_objComponent.getInputVerifier();
					if(inputV.shouldYieldFocus(m_objComponent)){//Comprobamos si cumple con el verificador simulando un cambio de foco
						handleChange(true);
						if (thread!=null){//Si esta construyendose evitamos que lo haga porque ya hemos hecho una seleccion
							thread.interruptLater(false);
//							final SwingWorker threadThis=thread;
//							//Necesitamos crear un hilo que se encargue de esperar a que el hilo del finder acabe completamente para cerrar el finder que crea
//							Thread threadAux=new Thread(new Runnable() {
//								
//								@Override
//								public void run() {
//									while(threadThis.isAlive()){
//										try {
//											Thread.sleep(50);
//										} catch (InterruptedException e) {
//											System.err.println("Se interrumpe el sleep de espera al requerir la interrupcion");
//											e.printStackTrace();
//										}
//									}
//									if(finderPopUp!=null)
//										finderPopUp.setVisible(false);
//								}
//							});
//							threadAux.start();
						}/*else{*/
						if(finderPopUp!=null)
							finderPopUp.setVisible(false);
						/*}*/
					}
				}
			}
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al asignar valor");
			ex.printStackTrace();
		}
	}
	public void keyReleased(KeyEvent ev) {
		try{
			//ev.consume();
			if(dictionaryFinder!=null){
				//System.err.println("ev "+ev.getKeyChar());
				if(ev.getKeyCode()!=KeyEvent.VK_DOWN && ev.getKeyCode()!=KeyEvent.VK_UP && 
						ev.getKeyCode()!=KeyEvent.VK_RIGHT && ev.getKeyCode()!=KeyEvent.VK_LEFT &&
						ev.getKeyCode()!=KeyEvent.VK_ENTER && ev.getKeyCode()!=KeyEvent.VK_TAB && ev.getKeyCode()!=KeyEvent.VK_SHIFT
						&& ev.getKeyCode()!=KeyEvent.VK_ESCAPE && ev.getKeyCode()!=KeyEvent.VK_ALT && ev.getModifiersEx()!=KeyEvent.CTRL_DOWN_MASK && ev.getKeyCode()!=KeyEvent.VK_CONTROL)
				{ 
					
					m_objComponent.requestFocusInWindow();
					//listFinderHasFocus=true;
					showDictionary();
				}
			}
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al realizar la operación");
			ex.printStackTrace();
		}
	}
	public void keyTyped(KeyEvent e) {}
	
	public void showDictionary(){
		//System.err.println("showDictionary");
		if (thread!=null){
			//System.err.println("Interrumpeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
			thread.interruptLater(false);
		}else{
			//System.err.println("No interrumpeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		}
		thread = new SwingWorker(){

			public Object construct() {
				try {
					Thread.sleep(300);
					if(!hasInterruptRequest()){
						if(finder==null){
							LinkedHashMap<String, DictionaryWord> dictionary=new LinkedHashMap<String, DictionaryWord>();
							boolean appliedLimit=dictionaryFinder.getDictionary(getId(),getId(), getText(), false, dictionary);
							finder=new Finder(dictionary, getText(), appliedLimit);
							dicParc=finder.getWordsForUser(getText());
						}else{
							dicParc=null;
							if(!finder.isAppliedLimitDictionary())//Si no ha aplicado limite podemos buscar sin tener que volver a pedirlo a bd
								dicParc=finder.getWordsForUser(getText());
							
							if(dicParc==null){
								LinkedHashMap<String, DictionaryWord> dictionary=new LinkedHashMap<String, DictionaryWord>();
								boolean appliedLimit=dictionaryFinder.getDictionary(getId(),getId(), getText(), false, dictionary);
								finder.setDictionary(dictionary, getText(), appliedLimit);
								dicParc=finder.getWordsForUser(getText());
							}
						}
						if(!hasInterruptRequest())
							return dicParc;
					}
				} catch (InterruptedException e) {
					//System.err.println("Se interrumpe el sleep puesto que se a eliminado el hilo");
					
				}
				//System.err.println("Construct interruptLater");
				return null;
			}

			@SuppressWarnings("unchecked")
			public void finished(){
				if(get()!=null){
					if(finderPopUp!=null)
						finderPopUp.setVisible(false);
					dicParc=(TreeMap<String, DictionaryWord>)get();
					finderPopUp = new FinderPopUp(getComponent());
					finderPopUp.addPopupMenuListener(new PopupMenuListener(){
						public void popupMenuCanceled(PopupMenuEvent ev){
							listFinderHasFocus=false;	
							editor.setFocusTraversalKeysEnabled(true); 
						}
						public void popupMenuWillBecomeInvisible(PopupMenuEvent ev){
							editor.setFocusTraversalKeysEnabled(true); 
						}
						public void popupMenuWillBecomeVisible(PopupMenuEvent ev){
							//Lo deshabilitamos porque si no no llega el evento al pulsar tabulador en los metodos keyListener
							//Nos interesa para seleccionar un valor del finder con el tabulador
							editor.setFocusTraversalKeysEnabled(false); 
						}							
					});
					boolean hasButtons=false;
					Iterator<String> it = dicParc.keySet().iterator();
					for(int i=0;i<dicParc.size() && i<50/*Ponemos un limite de 50 para evitar problemas de memoria*/;i++){
						String s = it.next();
						final DictionaryWord dw = dicParc.get(s);
						ButtonPopup b = new ButtonPopup(s);
						b.setActionCommand(dw.getWord());
						b.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent ev) {
								try{
									//String text = ((ButtonPopup)ev.getSource()).getText();
									String text = ev.getActionCommand();
									setText(text);
									
									handleChange(true);
									finderPopUp.setVisible(false);
									listFinderHasFocus=false;
									((ButtonPopup)ev.getSource()).removeActionListener(this);
								}catch(Exception ex){
									m_server.logError(SwingUtilities.getWindowAncestor(GEdit.this),ex,"Error al realizar la operación");
									ex.printStackTrace();
								}
							}
						});
						b.setPreferredSize(new Dimension(b.getPreferredSize().width,(int)m_objComponent.getHeight()));
						finderPopUp.add(b);
						hasButtons=true;
					}
					
					if(m_modoFilter && !editor.getText().isEmpty() && !editor.getText().contains("%") && dicParc.size()>1){
						ButtonPopup contains = createSpecialButtonFinder(TEXT_CONTAINS.replace("VALOR",editor.getText()), EVENT_CONTAINS);
						finderPopUp.add(contains);
						hasButtons=true;
						//System.err.println("Entra remove");
					}
					if(hasButtons/* && !hasInterruptRequest()*/){
						if(editor.hasFocus()){
							listFinderHasFocus=true;
							finderPopUp.show(m_objComponent, 0, m_objComponent.getHeight(),false);
						}
					}else{
						listFinderHasFocus=false;
					}
				}
				// Si hay una peticion de interrupcion no lo ponemos a null ya que si se pulsara enter pensaria que no hay otro hilo thread esperando y provocariamos una doble consulta
				if(!hasInterruptRequest())
					thread=null;
			}
		};
		thread.start();
	}
	
	private ButtonPopup createSpecialButtonFinder(final String text, final String event){
		ButtonPopup button = new ButtonPopup();
		button.setText(text);
		//button.setFont(new Font(button.getFont().getName(), Font.ITALIC, button.getFont().getSize()));
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev) {
				
				if(event.equals(EVENT_CONTAINS)){
					editor.setText("%"+editor.getText()+"%");
				}
				
				try {
					handleChange(true);
				} catch (AssignValueException e) {
					e.printStackTrace();
					editor.setText(currValue);
					m_server.logError(SwingUtilities.getWindowAncestor(GEdit.this),e,"Error al seleccionar busqueda por contenido");
				}
				finderPopUp.setVisible(false);
				listFinderHasFocus=false;
				((ButtonPopup)ev.getSource()).removeActionListener(this);
			}
		});
		button.setPreferredSize(new Dimension(button.getPreferredSize().width,(int)m_objComponent.getHeight()));
		
		return button;
	}
	
	public void setDateSelectioned(Calendar calendar) {
		//if(!calendar.equals(this.calendar)){
		Long milliseconds=calendar.getTimeInMillis();
		try{
			setValue(true,milliseconds);
			//closeCalendar();
		}catch(Exception ex){
			ex.printStackTrace();
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al asignar fecha");
		}
		//}
	}
	
	@Override
	public boolean newValueAllowed() {
		return isNull();
	}
	
	public Object getValue() {
		// TODO currValue debe ser parseado para que devuelva Integer,Double,Long...dependiendo del tipo de objeto
		String value=currValue;
			
		return value;
	}
	public void clean() throws ParseException, AssignValueException {
		setValue(false,null);
	}
	
	private String redondear(String oldVal){
		String result=oldVal;
		if(redondeo!=null && oldVal!=null){
			result=Auxiliar.redondea(Double.valueOf(oldVal),redondeo).toString();
		}
		return result;
	}
}
