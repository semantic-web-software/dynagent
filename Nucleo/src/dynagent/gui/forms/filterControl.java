
package dynagent.gui.forms;

import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;
import gdev.gen.DictionaryWord;
import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.queryData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.action;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.ISessionStateListener;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;
import dynagent.gui.StatusBar;
import dynagent.gui.actions.ActionManager;
import dynagent.gui.actions.commands.FindRelCommandPath;
import dynagent.gui.actions.commands.NewCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.SetCommonCommandPath;
import dynagent.gui.actions.commands.ViewCommandPath;
import dynagent.gui.actions.commands.ViewCommonCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;
import dynagent.gui.forms.utils.ActionException;
import dynagent.gui.forms.utils.Column;
import dynagent.gui.forms.utils.FilterNodeSelector;
import dynagent.gui.forms.utils.SplitPaneTripleDivision;
import dynagent.gui.forms.utils.resultPanel;
import dynagent.gui.utils.Target;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.common.properties.values.StringValue;

class panelSelection extends JPanel {

	private static final long serialVersionUID = 1L;
	Component comp;

	panelSelection(Dimension dim) {
		FlowLayout fl = (FlowLayout) getLayout();
		fl.setVgap(0);
		fl.setHgap(0);
		setSelection(new Object[0], dim);
	}

	public void setSelection(Object[] lista, Dimension dim) {
		/* if( dim==null ) dim=getSize(); */
		boolean asignarSize = true;
		if (dim == null) {
			dim = getSize();
			// Si no tenemos un tamaño mayor de 0 dejamos el que ocupe al
			// añadirlo ya que si no no aparecera el componente
			if (dim.height == 0 || dim.width == 0)
				asignarSize = false;
		}

		if (lista.length > 1) {
			//Object[][] datos = new Object[1][lista.length];
			comp = new JTable(new DefaultTableModel(lista, 0));

			if (asignarSize)
				((JTable) comp).setPreferredScrollableViewportSize(dim);
			comp = new JScrollPane(comp);
		} else {
			if (lista.length == 1)
				comp = new JLabel(lista[0].toString(), SwingConstants.CENTER);
			else
				comp = new JLabel();
		}
		if (getComponentCount() > 0)
			remove(0);
		if (asignarSize)
			comp.setPreferredSize(dim);
		add(comp, 0);
		validate();
		repaint();
	}

	public void reset() {
		setSelection(new Object[0], getSize());
	}

	public void setPreferredSize(Dimension dim) {
		super.setPreferredSize(dim);
		comp.setPreferredSize(dim);
	}
}

class mySelectionListener implements ListSelectionListener {
	resultPanel rPanel;
	int tipo;
	botoneraAccion botonera;
	boolean insertandoRows = false;
	communicator com;
	/* String user; */
	Integer userRol;

	mySelectionListener( botoneraAccion botonera, resultPanel rp, int tipo,Integer userRol) {
		rPanel = rp;
		this.tipo = tipo;
		this.botonera = botonera;
		this.userRol = userRol;
	}

	public void setInsertandoRows(boolean st) {
		insertandoRows = st;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (insertandoRows)
			return;

		if (!(botonera instanceof botoneraAccion))
			return;
		//System.out.println("TIPORP:" + tipo);
		ArrayList<GIdRow> obs = rPanel.getDataSelectedRows(/*tipo*/);
		if ( !obs.isEmpty()) {
			GIdRow idRow = obs.iterator().next();
			processDiscriminator(idRow, /* disc, */botonera);
		}
	}

	private void processDiscriminator(GIdRow idRow,botoneraAccion bt) {

		/*HashMap<Integer,ArrayList<UserAccess>> accessUTasks=Singleton.getInstance().getKnowledgeBase().getAllAccessIndividual(inst.getIDO(),this.userRol,null,null);
		Iterator<Integer> itr=accessUTasks.keySet().iterator();
		bt.setEnabled(bt.EDITAR, true);*/
		/*boolean exit=false;
		while(!exit && itr.hasNext()){
			UserAccess access = accessUTasks.get(itr.next());
			if (access.getAccess().getSetAccess()){
				bt.setEnabled(bt.EDITAR, true);
				exit=true;
			}
		}*/
	}
}

public class filterControl extends FormControl implements ActionListener, KeyListener, IHistoryDDBBListener{
	
	private HashMap<Integer, JPanel> m_mapForm = new HashMap<Integer, JPanel>();
	private HashMap<Integer,FormManager> m_mapFormManager = new HashMap<Integer, FormManager>();
	private HashMap<Integer, ArrayList> m_mapFormData = new HashMap<Integer, ArrayList>();
	private HashMap<Integer, JPanel> m_subNodeFix = new HashMap<Integer, JPanel>();
	private JScrollPane m_subFilterView;
	private resultPanel m_resultPanel;
	private FilterNodeSelector m_selector;
	private boolean m_popupMode = false;
	private boolean m_editFilterMode = false;

	private String m_tableIndex = null;
//	private int m_id = 0;
	private int m_altoBotoneraSubFilter = 25;
	private int m_margenBotoneraTop = 7;
	//private int m_anchoSelector;
	private JComponent m_component;
	public final static int defaultAnchoSelector = 150;
	private Dimension m_preferredSize;
	public final static int m_dividerSize = ConstantesGraficas.sizeDivisorSplitPane;
	public final static int m_tabSize = ConstantesGraficas.getSizeTabTabbedPane();//ConstantesGraficas.sizeTabTabbedPane;
	//private static final int tamPanel = 200;
	
	private botoneraAccion m_botonera;
	private mySelectionListener m_listResult, m_listFav;
	//private HashMap m_favDataCache = new HashMap();
	private Integer m_nMaxSelection;
	private Session m_sessionInternal;
	//private boolean m_buildInMaxWidth;

	private instance m_instanceFilter;
	private instance m_instanceFavourites;
	private ArrayList<SelectQuery> m_listSelect;
	private HashSet<String> m_listIdChanged;
	private int m_idoParent;
	private int m_idPropParent;
	private int m_idtoParent;
	private boolean m_selectioned;
	private boolean m_bussinessClassMode;
	private MouseListener m_mouseListener=null;
//	private instance instanceAsigned=null;
	private FormManager formManagerSimple;
	private JScrollPane subFilterSimple;
	private JTabbedPane tabbedPane;
	private boolean advancedFilter;
	private KeyListener m_keyListener;
	private GIdRow lastCreationInFavourites;//Almacena la ultima fila recien creada que añadimos a favoritos
	Element lastQuery;
	private boolean structural;
	//private HashMap<Session,ArrayList<InfoPossibleFavourite>> mapPossibleFavouriteBySession;

	private Session m_sessionAction;
	
	private HashMap<Integer,Integer> listIdosIdtosChangedAction;//Almacena los idos que han sido modificados o creados en este formulario bajo la sesionAction. Sirve para luego mostrarlos en favoritos ya que no van a base de datos y no llega el evento changeHistory
	
	private HashMap<Integer,Integer> listIdosIdPropForFix;
	
	public filterControl(
			Session ses,
			String tableIndex,//Id de la tabla, de momento lo dejo pero hay que comprobar si realmente hace falta. Se esta usado en transitionControl.userEvent
			Integer idtoUserTask,
			Integer userRol,//ArrayList<Integer> userRols,
			ObjectProperty property,// property que apunta al targetClass
			int value,
			int valueCls,
			boolean popup, boolean editFilter, botoneraAccion botonera,
			boolean buildInMaxWidth, int anchoSelector,
			boolean soporteFavoritos, Integer nMaxSelection, boolean bussinessClassMode, MouseListener mouseListener,KeyListener keyListener,
			KnowledgeBaseAdapter kba,WindowComponent dialog/*,instance instanceAsigned*/) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{

		this(ses, tableIndex, 
				idtoUserTask, userRol, property, value, valueCls,
				popup, editFilter, botonera, buildInMaxWidth,
				anchoSelector,soporteFavoritos, bussinessClassMode, mouseListener, keyListener, kba, dialog/*,instanceAsigned*/);
		m_nMaxSelection = nMaxSelection;
	}

	public filterControl(
			Session ses,
			String tableIndex,//Id de la tabla, de momento lo dejo pero hay que comprobar si realmente hace falta. Se esta usado en transitionControl.userEvent
			Integer idtoUserTask,
			Integer userRol,/*ArrayList<Integer> userRols,*/
			ObjectProperty property,
			int value,
			int valueCls,
			boolean popup, boolean editFilter, botoneraAccion botonera,
			boolean buildInMaxWidth, int anchoSelector, boolean soporteFavoritos, boolean bussinessClassMode,
			final MouseListener mouseListener,KeyListener keyListener,
			KnowledgeBaseAdapter kba,WindowComponent dialog/*, instance instanceAsigned*/) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{
		super(kba, dialog, idtoUserTask, userRol, ses, value, valueCls);
		m_session.addIMessageListener(m_actionManager);
		m_sessionInternal=createSessionInternal();
		m_sessionInternal.addIMessageListener(m_actionManager);
		m_session.addISessionStateListener(new ISessionStateListener(){

			public void sessionClosed(Session session, boolean commit) {
				try {
					if(!m_sessionInternal.isFinished()){
						m_sessionInternal.removeIchangeProperty(filterControl.this);
						if(!m_sessionInternal.getKnowledgeBase().isDispose())
							m_sessionInternal.rollBack();
						else m_sessionInternal.dispose();
					}
				} catch(Exception ex) {
					ex.printStackTrace();
					m_com.logError(filterControl.this.dialog.getComponent(),ex,"Error al cancelar la sesion de filtrado");
				}
			}
			
		});
		
		//m_actionManager=Singleton.getInstance().getActionManager();
		m_botonera = botonera;
		m_subFilterView = new JScrollPane();
		//FlowLayout fl = (FlowLayout) m_subFilterView.getLayout();
		//fl.setVgap(m_margenBotoneraTop / 2);
		//fl.setHgap(0);
		
		//m_kba = Singleton.getInstance().getKnowledgeBase();
		m_idoParent=property.getIdo();
		m_idtoParent=property.getIdto();
		m_idPropParent=property.getIdProp();
		structural=m_kba.getCategoryProperty(m_idPropParent).isStructural();
//		m_id = value;//m_kba.getIdoFilter(property);
		//m_access = property.getTypeAccess();
		//m_anchoSelector = anchoSelector == -1 ? defaultAnchoSelector : anchoSelector;
		m_tableIndex = tableIndex;
		m_editFilterMode = editFilter;// indica que estoy definiendo un filtro
	
		m_popupMode = popup;
		m_bussinessClassMode=bussinessClassMode;
		m_keyListener=keyListener!=null?keyListener:this;
		//m_buildInMaxWidth = buildInMaxWidth;
		//this.instanceAsigned=instanceAsigned;
		
		if(!m_popupMode)
			m_preferredSize = (Dimension)ConstantesGraficas.dimZonaTrabajo.clone();		
		else
			m_preferredSize = (Dimension)ConstantesGraficas.dimFormManager.clone();		
		
		m_instanceFilter = m_kba.getTreeObject(m_ido, userRol, idtoUserTask, m_session,true);	
		m_instanceFavourites=/*m_kba.getTreeObject(m_ido, userRol, idtoUserTask, m_session,true);*/(instance)m_instanceFilter.clone();

		//System.out.println("TreeObject de filterControl:"+m_instanceFilter);
				
		m_kba.setInstance(m_instanceFilter);
		try{
			// Si el individuo tiene algun hijo devolvera true, ya que habra creado un formulario simplificado
			// Si no tiene hijos, utiliza el formulario de ese ido como formulario simple, devolviendo false
			advancedFilter=buildFormSimple(/* m_id, *//*property,0,*/property.getIdo(),property.getIdto(),property.getIdProp(),m_ido,m_idto,m_preferredSize, m_keyListener);
			
			int altoFilter=-1;//subFilterSimple.getPreferredSize().height;
			int anchoFilter=-1;//subFilterSimple.getPreferredSize().width;
			if(advancedFilter){
				
				m_selector = new FilterNodeSelector(kba,m_session, property.getIdo(), property.getIdto(), property.getIdProp(), m_ido, m_idto, idtoUserTask,this);
					
				//buildForms(/* m_id, *//*property,0,*/property.getIdo(),property.getIdProp(),m_ido,m_preferredSize, new ArrayList<Integer>(), m_keyListener);
				buildForm(property.getIdo(),property.getIdto(),property.getIdProp(),m_ido,m_idto,m_preferredSize, m_keyListener);
				setCurrentFilterForm( null, null, null, property.getIdo(), property.getIdto(), property.getIdProp(),m_ido,m_idto);
				dimensionaVistaFilter();
				
				Dimension dimSubFView = m_subFilterView.getPreferredSize();
				Dimension dimFView=m_selector.getPreferredSize();
				
				//Necesitamos volver a obtenerlo ya que dimensionaVistaFilter lo modifica
				altoFilter=subFilterSimple.getPreferredSize().height+m_tabSize;
				anchoFilter=subFilterSimple.getPreferredSize().width;
				
//				altoFilter=Math.max(altoFilter,(int)Math.max(dimFView.getHeight(),dimSubFView.getHeight()))+m_tabSize;
//				anchoFilter=Math.max(anchoFilter,m_subFilterView.getPreferredSize().width+m_selector.getPreferredSize().width+m_dividerSize);
			}else{
				dimensionaVistaFilter();
				
				altoFilter=subFilterSimple.getPreferredSize().height;
				anchoFilter=subFilterSimple.getPreferredSize().width;
			}
			
			JPanel panelR=new JPanel();
			panelR.setBorder( BorderFactory.createEmptyBorder());
			panelR.setLayout(new BorderLayout(0,0) );

			//panel.add( filterControl.getResultPanel().getComponent(),BorderLayout.CENTER );
			panelR.add( botonera.getComponent(),BorderLayout.NORTH );

			//Lo añadimos a otro panel para que luego getPreferredSize devuelva el tamaño correcto
			JPanel panelRe=new JPanel();
			panelRe.add(panelR);

			//Dimension dimBot = m_botonera == null ? new Dimension(0, 0) : m_botonera.getComponent().getPreferredSize();

			int altoRp = (int) (m_preferredSize.height - altoFilter)
			- m_dividerSize - /*(int) dimBot.getHeight()*/(int)panelR.getPreferredSize().getHeight()-50;// 10 de
			// margenes
			if (altoRp < 0)// Para evitar que sea negativo ya que no se puede enviar a GViewBalancer un tamaño negativo
				altoRp = 0;

			//System.err.println("FilterControl:"+property);
			
			int anchoMin=0;
			if(dialog.getMainDialog()!=dialog){
				anchoMin=Math.min(m_preferredSize.width, anchoFilter);
			}else{
				anchoMin=m_preferredSize.width;
			}
			
			if(mouseListener!=null){
				m_mouseListener=mouseListener;
			}else{
				m_mouseListener = new MouseAdapter(){
					public void mouseClicked(MouseEvent e){
						filterControl.this.dialog.disabledEvents();
						try{
							if (e.getClickCount() == 2){
								asignar(m_resultPanel);
							}
						} catch (Exception ex) {
							m_com.logError(filterControl.this.dialog.getComponent(),ex,"Error al asignar");
							ex.printStackTrace();						
						} finally{
							filterControl.this.dialog.enabledEvents();
						}
					}
				};
			}
						
			m_resultPanel = new resultPanel(m_session,soporteFavoritos, false,false,
					m_idtoUserTask, property, m_ido,m_userRol,
					new Dimension((int) m_preferredSize.width, altoRp),m_mouseListener, true, anchoMin,kba,dialog, isAllowedConfigTables());

			m_listResult = new mySelectionListener(botonera, m_resultPanel, resultPanel.PANEL_RESULTADOS,m_userRol);

			m_resultPanel.addTableSelectionListener(m_listResult);
			/*if (soporteFavoritos) {
				m_listFav = new mySelectionListener(botonera, m_resultPanel, resultPanel.PANEL_FAVORITOS,m_userRol);

				m_resultPanel.getFavForm().addTableSelectionListener(m_resultPanel.getId(), m_listFav);
			}*/

			m_listIdChanged = new HashSet<String>();

			// Le borramos el valor de todos los RDN que se pone al crear el filtro
			// en el motor. Este valor influiria en la busqueda por lo que lo quitamos

			/*		m_kba.setInstance(m_instanceFilter);
			m_instanceFilter.setRdn(null,false);
			removeValuesRDNFilter(m_id,new HashSet<Integer>());// Se llama recursivamente recorriendo a los hijos
			m_kba.clearInstance();
			 */
			showFixedObjects(m_instanceFilter.getIDO(),m_instanceFilter.getIdTo(),null,new ArrayList<Integer>());
			if(m_bussinessClassMode){
				setBussinessClassMode(m_ido);
				exeQuery();
			}
			createComponent();
			//dimensionaAllVistaFilter(dim_filter);
			m_sessionInternal.addIchangeProperty(this,true);//Se hace al final porque hasta que no este construido no tiene sentido que cambie valores graficos
			m_kba.addHistoryDDBBListener(this);
		}finally{
			m_kba.clearInstance();
		}
		
		//Si estamos en una ventana modal añadimos a favoritos los prototipos que haya en motor para que el usuario pueda seleccionarlos
		if(m_popupMode){
			Iterator<Integer> itrProtos=m_kba.getIndividuals(m_idto, Constants.LEVEL_PROTOTYPE, true);
			while(itrProtos.hasNext()){
				int idoProto=itrProtos.next();
				//System.err.println(instanceResult);
				instance inst=m_kba.getTreeObjectTable(idoProto, m_kba.getClass(idoProto), m_resultPanel.getId(), 
														m_resultPanel.getFormManager().getColumnTreeOfTable(m_resultPanel.getId()),m_userRol, m_idtoUserTask, m_session);
				selectData select=new selectData();
				select.addInstance(inst);
				m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
			}
			
			// Pedimos el foco para que el primer campo de filtrado tenga el foco
			SwingUtilities.invokeLater(new Runnable(){

				public void run() {
					if(subFilterSimple.isDisplayable()){
						filterControl.this.dialog.getComponent().getFocusTraversalPolicy().getFirstComponent(subFilterSimple).requestFocusInWindow();
						//subFilterSimple.requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
						//subFilterSimple.requestFocusInWindow();
					}else{
						SwingUtilities.invokeLater(this);
					}
				}
				
			});
			//if(m_session.isChildOfDDBBSession()){Lo comentamos porque si no falla cuando hacemos anterior-siguiente en un filterControlModal de busqueda avanzada ya que es una session que no va a base de datos
				m_sessionAction=m_session;
				m_session.addIchangeProperty(this, false);
			//}
			/*No ponemos nada en el else para que si ocurre m_sessionAction de excepcion si se usa y no se cree en una sesion que no va a base de datos. En ese caso no podemos crear una nueva sesion porque luego
			  se queda abierta si estamos en un formulario anterior-siguiente.
			  Si la sesion no termina en una sesion que va a base de datos no tendria sentido que se hubiera creado este filterControl con permisos de creación,edición...*/
			//m_sessionAction=m_kba.createDDBBSession(true, true, true, true, true);
		}
	}

	/*Le crea una propiedad al instance de busquedas para indicar que se trata de busquedas de bussinessClass.
	 * Antes de llamar a este metodo hay que asignar el instance a KnowledgeBaseAdapter para que el valor se
	 * asigne sobre ese instance y no sobre el motor*/
	private void setBussinessClassMode(int ido) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
		m_kba.setValue(ido, Constants.IdPROP_BUSINESSCLASS, null, m_kba.buildValue(true, Constants.IDTO_BOOLEAN), m_userRol, m_idtoUserTask, m_sessionInternal);
	}

//	private void removeValuesRDNFilter(int idObject, HashSet<Integer> listProcessedChildren) {
//	DataProperty propertyRDN = m_kba.getRDN(idObject, m_userRol, m_idtoUserTask);
//	System.out.println("Property removeValuesRDNFilter: "+propertyRDN.toString());
//	try {
//	m_kba.setValueList(/* new LinkedList(), */idObject, /* idProp */
//	propertyRDN, new LinkedList()/*, new session()*/);
//	} catch (Exception e) {
//	e.printStackTrace();
//	JOptionPane.showMessageDialog(/* m_currentModalForm */null, e
//	.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//	}
//	listProcessedChildren.add(idObject);
//	Iterator<ObjectProperty> itr = m_kba.getChildren(idObject, m_userRol,
//	m_idtoUserTask);
//	while (itr.hasNext()) {
//	ObjectProperty property = itr.next();
//	Integer idFilter = m_kba.getIdFilter(property);
//	if (idFilter != null && !listProcessedChildren.contains(idFilter))
//	removeValuesRDNFilter(idFilter,listProcessedChildren);
//	}
//	}


	private void createComponent() {
		/***********************************************************************
		 * ES UTILIZADO POR TRANSITION CONTROL PARA CREAR LOS FORMULARIOS DE
		 * FILTRO Y AÑADIRSELOS A LOS JDIALOG. OPERATIONGUI SIN EMBARGO YA HACE
		 * USO DE LAS CLASES DE FRAMEWORK OBTENIENDO LOS SUBFORMULARIOS DE ESTA
		 * CLASE Y CREANDO SUS PROPIOS SPLITPANES. HAY QUE PENSAR DONDE UBICAR
		 * ESTO YA QUE NO DEBERIAMOS CREAR COMPONENTES EN ESTA CLASE
		 **********************************************************************/


		/*SplitPaneTripleDivision filterSplitPane = new SplitPaneTripleDivision(SplitPaneTripleDivision.NOROESTE_NORESTE_SUR);
		filterSplitPane.setBorder(null);
		*/
		JPanel low = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
		
		low.setBorder(null);
		low.setLayout(new BorderLayout());
		if(m_resultPanel!=null)
			low.add(m_resultPanel.getComponent(),BorderLayout.CENTER);
		if (m_botonera != null)
			low.add(m_botonera.getComponent(),	BorderLayout.NORTH);

		JScrollPane scrollResult=new JScrollPane(low,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollResult.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
		
		scrollResult.setBorder(BorderFactory.createEmptyBorder());
		
		Dimension dimTotal;
		final JComponent top;
		if(advancedFilter){
			
			JScrollPane scrollSelector=new JScrollPane(m_selector,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);		
			JScrollPane scrollSubFilter=m_subFilterView;//new JScrollPane(m_subFilterView,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollSelector.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
			scrollSubFilter.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
			
			scrollSelector.setBorder(BorderFactory.createEmptyBorder());
			scrollSubFilter.setBorder(BorderFactory.createEmptyBorder());
			
//			 AQUI CREAMOS UN TABBEDPANE, Y CADA ELEMENTO ESTARIA EN UNA PESTAÑA
			tabbedPane=new JTabbedPane();
			tabbedPane.setName("TabbedPaneFilter");
			tabbedPane.addTab(Utils.normalizeLabel("GENERAL"),subFilterSimple);
			
			JSplitPane splitFilter=new JSplitPane();
			splitFilter.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			splitFilter.setLeftComponent(scrollSelector);
			splitFilter.setRightComponent(scrollSubFilter);
			splitFilter.setDividerSize(ConstantesGraficas.sizeDivisorSplitPane);
			splitFilter.setBorder(BorderFactory.createEmptyBorder());
			
			tabbedPane.addTab(Utils.normalizeLabel("AVANZADA"), splitFilter);
			
			dimTotal = createSizesAdvanced(scrollSubFilter, scrollSelector, scrollResult, tabbedPane);
			//subFilterSimple.setPreferredSize(new Dimension((int)subFilterSimple.getPreferredSize().getWidth(),(int)scrollSubFilter.getPreferredSize().getHeight()));
			
			//tabbedPane.setPreferredSize(new Dimension((int)dimTotal.getWidth(),Math.max(subFilterSimple.getPreferredSize().height, scrollSelector.getPreferredSize().height)));
			tabbedPane.setPreferredSize(new Dimension(subFilterSimple.getPreferredSize().width,subFilterSimple.getPreferredSize().height+m_tabSize));//Hacemos que el tamaño sea el del filtrado principal para que el splitPane aparezca mostrando el resto de espacio para el resultPanel
			
			top=tabbedPane;
		}else{
			dimTotal = createSizesSimple(subFilterSimple, scrollResult);
			
			top=subFilterSimple;
		}
		
		
		
		/*filterSplitPane.setComponente(scrollSelector, SplitPaneTripleDivision.PRIMERAPOSICION);
		filterSplitPane.setComponente(scrollSubFilter, SplitPaneTripleDivision.SEGUNDAPOSICION);
		filterSplitPane.setComponente(scrollResult, SplitPaneTripleDivision.TERCERAPOSICION);		
	*/
		
		

		/*m_component=filterSplitPane;
		m_component.setBorder(BorderFactory.createEmptyBorder());
		m_component.setPreferredSize(dimTotal);*/		
		
		
		JSplitPane splitAll=new JSplitPane();
		splitAll.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(top);
		splitAll.setBottomComponent(scrollResult);
		splitAll.setBackground(UIManager.getColor("ToolBar.background"));
		splitAll.setDividerSize(ConstantesGraficas.sizeDivisorSplitPane);
		//splitAll.setResizeWeight(1.0);
		
		m_component=splitAll;
		m_component.setBorder(BorderFactory.createEmptyBorder());
		//m_component.setPreferredSize(dimTotal);No lo ponemos porque el dimTotal que viene del formulario advanced no es el que nos interesa para el componente
		
		if(advancedFilter){
			GConst.addShortCut(null, top, GConst.SEARCH_SWITCH_SHORTCUT_KEY, GConst.SEARCH_SWITCH_SHORTCUT_MODIFIERS, "Conmutar_Filtrado", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

				private static final long serialVersionUID = 1L;
				
				public void actionPerformed(ActionEvent arg0) {
					if(subFilterSimple.isVisible()){
						((JTabbedPane)top).setSelectedIndex(1);
						dialog.getComponent().getFocusTraversalPolicy().getFirstComponent(m_subFilterView).requestFocusInWindow();
					}else if(m_subFilterView.isVisible()){
						((JTabbedPane)top).setSelectedIndex(0);
						dialog.getComponent().getFocusTraversalPolicy().getFirstComponent(subFilterSimple).requestFocusInWindow();
					}
				}
				
			});
		}
		
		GConst.addShortCut(null, top, GConst.SEARCH_SHORTCUT_KEY, GConst.SEARCH_SHORTCUT_MODIFIERS, "Filtrado", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				if(subFilterSimple.isVisible())
					dialog.getComponent().getFocusTraversalPolicy().getFirstComponent(subFilterSimple).requestFocusInWindow();
				else if(m_subFilterView.isVisible())
					dialog.getComponent().getFocusTraversalPolicy().getFirstComponent(m_subFilterView).requestFocusInWindow();
			}
			
		});
		
		//Tecla para dar el foco a la tabla de resultados o al detalle, dependiendo de lo que esta mostrandose en ese momento
		GConst.addShortCut(null,m_resultPanel.getFormManager().getTable(m_resultPanel.getId()).getTable(), GConst.RESULT_SHORTCUT_KEY, GConst.RESULT_SHORTCUT_MODIFIERS, "Resultado", JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		if(m_resultPanel.getDetailPanel()!=null){
			GConst.addShortCut(null, m_resultPanel.getDetailPanel(), GConst.RESULT_SHORTCUT_KEY, GConst.RESULT_SHORTCUT_MODIFIERS, "Detalle", JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		}
	}
	
	private Dimension createSizesSimple(JScrollPane filter, JScrollPane result){

		int altoFiltro = filter.getPreferredSize().height;
		int altoResult = result.getPreferredSize().height;


		int anchoResult = Math.min(result.getPreferredSize().width, m_preferredSize.width);
		//int anchoSelector = selector.getPreferredSize().width;
		int anchoFiltro = filter.getPreferredSize().width;

		// ALTO
		int altoTotal = altoFiltro+ m_dividerSize + altoResult;
		if(altoTotal>m_preferredSize.height){
			altoFiltro = m_preferredSize.height-m_dividerSize-altoResult;
			altoTotal = m_preferredSize.height;			
		}
		if(altoFiltro<=filter.getPreferredSize().height){
			anchoFiltro += filter.getVerticalScrollBar().getPreferredSize().width;
		}

		if(m_preferredSize.width>=anchoFiltro){
			anchoResult = Math.max(anchoFiltro, anchoResult);
		}else{
			anchoResult = m_preferredSize.width;
		}

		result.setPreferredSize(new Dimension(anchoResult, altoResult));
		filter.setPreferredSize(new Dimension(anchoFiltro, altoFiltro));	
		
		//result.setMinimumSize(new Dimension(anchoResult, altoResult));		
		return new Dimension(anchoResult, altoTotal);
	}
	
	private Dimension createSizesAdvanced(JScrollPane subFilter, JScrollPane selector, JScrollPane result, JTabbedPane tabbed){

		int altoInitialSelector=selector.getPreferredSize().height;
		int altoInitialFiltro=subFilter.getPreferredSize().height;
		int altoFiltro = Math.max(selector.getPreferredSize().height, subFilter.getPreferredSize().height);
		int altoSelector = Math.max(selector.getPreferredSize().height, subFilter.getPreferredSize().height);
		int altoResult = result.getPreferredSize().height;

		//Lo expandimos para saber el ancho que se necesitaria si se abren los nodos
		m_selector.expandTree();
		
		int anchoResult = Math.min(result.getPreferredSize().width, m_preferredSize.width);
		int anchoSelector = selector.getPreferredSize().width;
		int anchoFiltro = subFilter.getPreferredSize().width;

		//Volvemos a dejarlo como estaba
		m_selector.collapseTree();
		
		// ALTO
		int altoTotal = Math.max(altoInitialSelector, altoInitialFiltro)+ m_dividerSize + altoResult + m_tabSize;
		if(altoTotal>m_preferredSize.height){
			altoSelector = m_preferredSize.height-m_dividerSize-altoResult - m_tabSize;
			altoFiltro = m_preferredSize.height-m_dividerSize-altoResult - m_tabSize;
			altoTotal = m_preferredSize.height;			
		}
		if(altoFiltro<=subFilter.getPreferredSize().height){
			anchoFiltro += selector.getVerticalScrollBar().getPreferredSize().width;
		}

		// ANCHO
		if(altoFiltro<altoInitialSelector){
			anchoSelector += selector.getVerticalScrollBar().getPreferredSize().width;
		}		
		
		if(m_preferredSize.width>=(anchoSelector+anchoFiltro+m_dividerSize)){
			anchoResult = Math.max(anchoSelector+anchoFiltro+m_dividerSize, anchoResult);
		}else{
			anchoResult = m_preferredSize.width;
			anchoSelector = m_preferredSize.width - (anchoFiltro + m_dividerSize);
		}

		result.setPreferredSize(new Dimension(anchoResult, altoResult));
		selector.setPreferredSize(new Dimension(/*anchoResult-anchoFiltro-m_dividerSize*/anchoSelector, altoSelector));
		subFilter.setPreferredSize(new Dimension(anchoFiltro, altoFiltro));	
		
		//result.setMinimumSize(new Dimension(anchoResult, altoResult));		
		return new Dimension(anchoResult, altoTotal);
	}

	private void showFixedObjects(int idObject,int idtoObject,Integer idProp,ArrayList<Integer> idosProcessed) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {

		/*		if (idObject == m_id)
			m_kba.setInstance(m_instanceFilter);
		 */
		idosProcessed.add(idObject);
		Iterator<ObjectProperty> itrObject;
		if(idProp!=null){
			ArrayList<ObjectProperty> list=new ArrayList<ObjectProperty>();
			ObjectProperty prop=m_kba.getChild(idObject, idtoObject, idProp, m_userRol, m_idtoUserTask, m_session);
			if(prop!=null)
				list.add(prop);
			itrObject=list.iterator();
		}else{
			itrObject=m_kba.getChildren(idObject,idtoObject,m_userRol, m_idtoUserTask,m_session);
		}
		//ArrayList<String> rdns = new ArrayList<String>();
		//String rdn = "";
		//int longTotalRdns = 0;
		while (itrObject.hasNext()) {
			ObjectProperty property = itrObject.next();
			
			
//			LinkedList<Value> valueList = property.getValues();
//			Iterator<Value> itrObjectValues = valueList.iterator();
//			boolean first = true;
//			String fixIdo = valueList.size() > 1 ? "{" : "";
//			while (itrObjectValues.hasNext()) {
//				ObjectValue objectV = (ObjectValue)itrObjectValues.next();
//				int idObjectV = objectV.getValue();
//				/* if(m_kba.getLevelObject(idObjectV)==Constants.LEVEL_INDIVIDUAL){ */
//				DataProperty propertyRDN = m_kba.getRDN(idObjectV, m_userRol, m_idtoUserTask,null);
//				objectV.getValueCls();
//				String rdnFound = m_kba.getValueData(propertyRDN);
//				rdns.add(rdnFound);
//				longTotalRdns += rdnFound.length();
//				if (first)
//					rdn = rdnFound;
//
//				if (!first)
//					fixIdo += ";";
//				fixIdo += idObjectV;
//				first = false;
//				/*
//				 * }else { showFixedObjects(idObjectV); }
//				 */
//			}
//
//			if (valueList.size() > 1) {
//				fixIdo += "}";
//				double porcent = 10.0 / ((double) (longTotalRdns
//						+ valueList.size() - 1));
//				// int numCharItem= 15/
//				rdn = "";
//				for (int i = 0; i < rdns.size(); i++) {
//					String nextR = (String) rdns.get(i);
//					if (i > 0)
//						rdn += ";";
//					int longS = (int) (((double) nextR.length()) * porcent) - 1;
//					if (longS == 0)
//						longS = 1;
//					System.out.println("PORCENT, TOT, THIS:" + porcent + ","
//							+ longTotalRdns + "," + nextR.length());
//					String anexo = porcent < 1 ? nextR.substring(0, longS + 1)
//							: nextR;
//					rdn += anexo;
//				}
//			}
//			panelSelection text = (panelSelection) m_subNodeFix.get(idObject);
//			if (text == null) {
//				/*				m_kba.clearInstance();*/
//				return;
//			}
//			text.setSelection(rdns.toArray(), null);
//			m_selector.setRdn(idObject, rdn);
			
				if(property.getEnumList().isEmpty()){
					fixObject(property.getIdo(), property.getIdto(), property.getIdProp()/*, null*/);
					Iterator<Integer> itrRange=property.getRangoList().iterator();
					while(itrRange.hasNext()){
						Integer idoFilter = itrRange.next();
						if (Constants.isIDTemporal(idoFilter) && !idosProcessed.contains(idoFilter)){
							int idtoFilter = m_kba.getClass(idoFilter);
							//System.err.println("Property showFixed:"+property);
							showFixedObjects(idoFilter,idtoFilter,null,idosProcessed);
						}
					}
				}
			}
		/*		if (idObject == m_id)
			m_kba.clearInstance();
		 */
		}

	public String getTableIndex() {
		return m_tableIndex;
	}

	public int getId() {
		return m_ido;
	}

	/*	public selectData getResultDataSelection() {
		return m_resultPanel.getDataRows(resultPanel.PANEL_FAVORITOS);
	}
	 */	
	public boolean isSelectioned(){
		return m_selectioned;
	}

	/*public selectData getResultInstanceSelection() {
		return m_resultPanel.getDataRows(resultPanel.PANEL_FAVORITOS);
	}*/

	private void dimensionaVistaFilter() {
		int ancho = 0, alto = 0;	
		
		//Nos basamos en las dimensiones del formulario que se muestra. Pero si su alto es menor que 1/3 del alto total lo aumentamos
		ancho = formManagerSimple.getComponent().getPreferredSize().width;//subFilterSimple.getPreferredSize().width;
		alto = formManagerSimple.getComponent().getPreferredSize().height;//subFilterSimple.getPreferredSize().height;
		//if(alto<m_preferredSize.height/3)
		//	alto=m_preferredSize.height/3;
		
		if(advancedFilter){
			//ancho = Math.max(ancho,m_mapForm.get(m_ido).getPreferredSize().width);
			//alto = Math.max(alto,m_mapForm.get(m_ido).getPreferredSize().height);
			// System.out.println("ANCHO, ALTO "+ancho+","+alto);
			
			//m_subFilterView.setPreferredSize(new Dimension(ancho, alto));
			m_subFilterView.setPreferredSize(new Dimension(ancho, m_mapForm.get(m_ido).getPreferredSize().height));
			m_subFilterView.setBorder(new EmptyBorder(0, 0, 0, 0));
			
		}
		
		subFilterSimple.setPreferredSize(new Dimension(ancho, alto));
		subFilterSimple.setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	private boolean buildFormSimple(int ido,int idto,int idProp,int value,int valueCls,Dimension dim, KeyListener keyListener) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		ArrayList listViewForm;
		boolean simplified=false;
		//Si tiene algun hijo creamos un formulario simplificado a partir de las columnProperties
		if(m_kba.getChildren(value, valueCls, m_userRol, m_idtoUserTask, m_session).hasNext()){
			simplified=true;
			Iterator<Column> itrColumn=m_kba.getColumnsObject(value, valueCls, valueCls, idto, m_userRol, m_idtoUserTask, m_session, false/*Con false obtenemos el objectProperty de los enumerados, en vez de el rdn*/,true/*Para que no muestre menos columnas de las que hay en la tabla ya que hemos tenido que poner que filterMode=false*/,false,null).iterator();
			ArrayList<Property> listProperties=new ArrayList<Property>();
			while(itrColumn.hasNext()){
				Column col=itrColumn.next();
				Property prop=col.getProperty().clone();//Hacemos clon por el pivotado ya que si no le cambiariamos el nombre a la misma property
				//if(prop.getIdo()!=value)
				//	prop.setName(prop.getName()+" de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask));
				prop.setName(col.getName());
				listProperties.add(prop);
			}
			
			if(listProperties.isEmpty()){
				OperationNotPermitedException ex=new OperationNotPermitedException("No tiene permiso para ver el formulario simple de filterControl de ido:"+ido+" idto:"+idto);
				ex.setUserMessage("No tiene permiso para ver este formulario");
				throw ex;
			}
			
			listViewForm=formFactory.buildFormulario(m_kba,dim, listProperties, m_idtoUserTask, ido, value, m_userRol, m_session, m_popupMode, true, false, access.VIEW, false, false, true, this, null, null, isAllowedConfigTables());
		}else{
			formFactory ff = new formFactory(m_kba,m_session,
					dim, access.VIEW, m_idtoUserTask, m_userRol,
					ido,value, valueCls,m_popupMode, true, false, null, null, isAllowedConfigTables());

			listViewForm = ff.getListViewForm();
		}
		boolean formEnModoCreacion = !m_editFilterMode;	
		formManagerSimple = new FormManager( this, this, this,
				access.VIEW,"FILTER",listViewForm, formEnModoCreacion, true, false,// indiferente
				false, false, keyListener, dim, m_kba,dialog,
				m_idtoUserTask,m_userRol,m_session);
		
		JPanel form=new JPanel();
		form.setLayout(new BorderLayout(0,0));
		JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		top.setBorder(BorderFactory.createEmptyBorder());
		top.add(formManagerSimple.getComponent());
		form.add(top, BorderLayout.CENTER);
		//form = formManager.getComponent();
		form.validate();
		
		subFilterSimple=new JScrollPane(form,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		return simplified;
	}
	
	private void buildForm(int ido,int idto,int idProp,int value,int valueCls,Dimension dim, KeyListener keyListener) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		boolean isFilterRoot = value == m_ido;
		Dimension currDim = isFilterRoot ? new Dimension((int) dim
				.getWidth(), (int) (dim.getHeight() * 0.66))
		: new Dimension(
				(int) dim.getWidth(),
				(int) ((dim.getHeight() - (m_altoBotoneraSubFilter + m_margenBotoneraTop)) * 0.66));

		formFactory ff = new formFactory(m_kba,m_session,
				currDim, access.VIEW, m_idtoUserTask, m_userRol,
				ido,value, valueCls, m_popupMode, true, false, null, null, isAllowedConfigTables());

		ArrayList listViewForm = ff.getListViewForm();
		
		subBuildForm(/* ido, *//*property,posChild,*/ido,idto,idProp,value, listViewForm, currDim,isFilterRoot,keyListener);
	}
	
	private void buildForms(/*ObjectProperty property,int posChild,*/int ido,int idto,int idProp,int value,int valueCls,Dimension dim, ArrayList<Integer> listForms, KeyListener keyListener) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException {

		Integer idObject = value;//m_kba.getIdoFilter(property,posChild);
		int idtoObject = valueCls;
		if (idObject != null) {// Tiene filtro. Si no tuviera filtro se
			// trataria de un enumerado por lo que no se
			// hace nada
			listForms.add(idObject);
			boolean isFilterRoot = idObject == m_ido;
			Dimension currDim = isFilterRoot ? new Dimension((int) dim
					.getWidth(), (int) (dim.getHeight() * 0.66))
			: new Dimension(
					(int) dim.getWidth(),
					(int) ((dim.getHeight() - (m_altoBotoneraSubFilter + m_margenBotoneraTop)) * 0.66));

			formFactory ff = new formFactory(m_kba,m_session,
					currDim, access.VIEW, m_idtoUserTask, m_userRol,
					ido,idObject, idtoObject, m_popupMode, true, false, this, null, isAllowedConfigTables());

			ArrayList listViewForm = ff.getListViewForm();
			
				subBuildForm(/* ido, *//*property,posChild,*/ido,idto,idProp,idObject, listViewForm, currDim,isFilterRoot,keyListener);

			/* Iterator itr= nodoFilter.getChildren("FILTER").iterator(); */
			/* int idObject=m_kba.getIdFilter(property); */
			Iterator<ObjectProperty> itrChildren = m_kba.getChildren(idObject,idtoObject,m_userRol, m_idtoUserTask, m_session);
			while (itrChildren.hasNext()) {

				ObjectProperty prop = itrChildren.next();
				/* m_childParentMap.put(m_kba.getIdFilter(property), idObject); */
				if(prop.getEnumList().isEmpty()){
					Iterator<Integer> itrRange=prop.getRangoList().iterator();
					while(itrRange.hasNext()){
						int idoFilter=itrRange.next();
						if (Constants.isIDTemporal(idoFilter) && listForms.indexOf(idoFilter) == -1) {
							int idtoFilter=m_kba.getClass(idoFilter);
							//System.out.println("Hijo de: " + idObject);
							buildForms(/* idObject, *//*prop,i,*/prop.getIdo(),prop.getIdto(),prop.getIdProp(),idoFilter, idtoFilter,/* idUserTask, */dim,listForms,keyListener);
						}
					}
				}
			}
		}
	}

	public void subBuildForm( /* int ido, *//*ObjectProperty property,int posChild,*/int ido,int idto,int idProp,int value,
			ArrayList listViewForm, Dimension dim, boolean isFilterRoot, KeyListener keyListener) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException{

		int idObject = value;//m_kba.getIdoFilter(property,posChild);

		JPanel form = new JPanel();
		form.setBorder(BorderFactory.createEmptyBorder());
		FlowLayout fl = (FlowLayout) form.getLayout();
		fl.setVgap(0);

	
		FormManager formManager = null;
		if (listViewForm.size() > 0){
			boolean formEnModoCreacion = !m_editFilterMode;	
			
			formManager = new FormManager( this, this, this,
					access.VIEW,"FILTER",listViewForm, formEnModoCreacion, true, false,// indiferente
					false, false, keyListener, dim, m_kba,dialog,
					m_idtoUserTask,m_userRol,m_session);
		}
		if (!isFilterRoot) {
			// System.out.println("NO ROOT, añado " + ref );
			boolean enable = m_kba.getChild(ido, idto, idProp, m_userRol, m_idtoUserTask, m_session).getValues().isEmpty();
			/*
			 * Creamos este panel para que el formulario aparezca centrado ya
			 * que, al ser mas pequeño que el espacio disponible, aparecen en
			 * una esquina
			 */
			JPanel panelFiltro = new JPanel(new BorderLayout());//new FlowLayout(FlowLayout.CENTER, 0, 0));
			panelFiltro.setBorder(BorderFactory.createEmptyBorder());
		
			JPanel subFilter=null;
			if (listViewForm.size() > 0){
				panelFiltro.add(formManager.getComponent());
				subFilter = buildSubFilter(enable, /* ido, *//*property,posChild,*/ido,idProp,idObject,
						panelFiltro.getPreferredSize().width);
			}else{
				//TODO Todo esto ai k kambiarlo y krear un nuevo control -> "reportControl"
				form.setPreferredSize(new Dimension(400,200));
				/*JPanel jp= new JPanel();
				JButton jb=new JButton("Prueba");
				//jb.setVisible(false);
				jp.add(jb);
				panelFiltro.add(jp);
				panelFiltro.setPreferredSize(new Dimension(400,200));*/
				subFilter = buildSubFilter(enable, /* ido, *//*property,posChild,*/ido,idProp,idObject,
						form.getPreferredSize().width);
				panelFiltro.validate();
				//subFilter.validate();
				//panelFiltro.repaint();
				//subFilter.repaint();
				
			}

			/* JPanel subFilter= buildSubFilter(enable, filter,dim); */
			
			form.setLayout(new BorderLayout());
			/* form.add( gestorFormularios.getComponent(), BorderLayout.NORTH ); */
			if(listViewForm.size()>0){
				JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				top.setBorder(BorderFactory.createEmptyBorder());
				top.add(panelFiltro);
				form.add(top, BorderLayout.NORTH);
				form.add(subFilter, BorderLayout.CENTER);
				
			}else{
				JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				top.setBorder(BorderFactory.createEmptyBorder());
				top.add(subFilter);
				form.add(top, BorderLayout.CENTER);				
			}
			form.validate();
			form.repaint();
		} else {
			if (listViewForm.size() > 0){
				form.setLayout(new BorderLayout());
				JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				top.setBorder(BorderFactory.createEmptyBorder());
				top.add(formManager.getComponent());
				form.add(top, BorderLayout.CENTER);
				//form = formManager.getComponent();
				form.validate();
			}else{
				form.setPreferredSize(new Dimension(200,200));
				form.validate();
				form.repaint();
			}
			// System.out.println("ROOT, añado " + ref );
		}
		
		
		Integer ref = idObject;// m_kba.getIdFilter(property);//property.getId();
		//JScrollPane scroll = new JScrollPane(form);
		//scroll.setBorder(BorderFactory.createEmptyBorder());
		m_mapForm.put(ref, form);
		/* m_mapSimpleForm.put( ref, gestorFormularios.getComponent() ); */
		m_mapFormManager.put(ref, formManager);
		/* m_mapFormData.put( ref, xmlView ); */
		m_mapFormData.put(ref, listViewForm);

		/*
		 * JDialog dlg= new JDialog(); dlg.setContentPane(form); dlg.pack();
		 * dlg.repaint(); dlg.setLocation(200, 100); dlg.setVisible(true);
		 */
	}

	public JPanel buildSubFilter(boolean enable, /* Element filter, *//* int ido, */
			/*ObjectProperty property, int posChild,*/int ido,int idProp,int value, int ancho) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		JPanel sub = new JPanel();
		FlowLayout fl = (FlowLayout) sub.getLayout();
		fl.setVgap(m_margenBotoneraTop / 2);

		

//		int ancho = (int) dim.getWidth();
		/*
		 * sub.setPreferredSize( new Dimension( ancho-20,
		 * m_altoBotoneraSubFilter + m_margenBotoneraTop ));
		 */
		sub.setPreferredSize(new Dimension(ancho - 15, m_altoBotoneraSubFilter
				+ m_margenBotoneraTop));
		Integer ref = value;//m_kba.getIdoFilter(property,posChild)/* filter.getAttributeValue("REF") */;
		Integer idto = value;//m_kba.getIdtoFilter(property,posChild);

		IdObjectForm target = new IdObjectForm();
		target.setIdo(/*property.getIdo()*/ido);
		target.setIdProp(/*property.getIdProp()*/idProp);
		target.setValueCls(idto);

		IdOperationForm idOperation = new IdOperationForm();
		idOperation.setTarget(target);
		idOperation.setOperationType(botoneraAccion.OPERATION_ACTION);
		idOperation.setButtonType(botoneraAccion.ABRIR);
		/*
		 * subBuildBoton(enable, sub, "FIJAR "+property.getName(), "ACTION:-1:"+
		 * ref +":"+botoneraAccion.ABRIR);
		 */

		String name=m_kba.getLabelProperty(idProp, m_kba.getClass(ido), m_idtoUserTask);
		//System.out.println("filterContorl.buildSubFilter:ido->"+ref+" idto->"+idto+" label"+name);

		subBuildBoton(enable, sub, Utils.normalizeLabel("FIJAR "+ name), Utils.normalizeLabel("BUSQUEDA "+name), idOperation.getIdString());

		/*
		 * Dimension dimSel=new Dimension( ancho-270, 20); panelSelection
		 * seleccion= new panelSelection(dimSel); seleccion.setPreferredSize(
		 * dimSel );
		 */
		panelSelection seleccion = new panelSelection(null);
		seleccion.setBackground(UIManager.getColor("TextField.background")/*new Color(238,238,238)*/);//TODO Asignar el color en la futura clase UI de apariencia grafica
		sub.add(seleccion);
		m_subNodeFix.put(ref, seleccion);
		idOperation.setButtonType(botoneraAccion.RESET);
		subBuildBoton(enable, sub, Utils.normalizeLabel("RESET"), null, idOperation.getIdString());

		int anchoBotonFijar = (int) sub.getComponent(0).getPreferredSize().getWidth();
		int anchoBotonReset = (int) sub.getComponent(2).getPreferredSize().getWidth();

		Dimension dimCampoSeleccion = new Dimension(ancho-anchoBotonFijar- anchoBotonReset-15-20/* De margenes */,(int)GViewBalancer.getRowHeightS(Singleton.getInstance().getGraphics()));
		seleccion.setPreferredSize(dimCampoSeleccion);
		return sub;
	}

	private void subBuildBoton(boolean enable, JPanel root, String label, String toolTipText, String command) {
		JButton boton = new JButton(label);
		boton.setMargin(new Insets(0, 1, 0, 1));
		root.add(boton);
		boton.setActionCommand(command);
		boton.addActionListener(this);
		boton.setEnabled(enable);
		boton.setToolTipText(toolTipText);
	}

	public void setCurrentFilterForm(Integer idoParent,Integer idtoParent,Integer idPropParent,int ido,int idto,int idProp,int value,int valueCls) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException {
		// System.out.println("BUSCANDO REF "+ref);
		m_kba.setInstance(m_instanceFilter);
		try{
			//El padre necesita estar creado porque si se fija un objeto necesita el formulario del padre para saber que no es un componente de este y por lo tanto fijarlo
			if(idoParent!=null && idPropParent!=null){
				JPanel form = (JPanel) m_mapForm.get(new Integer(ido));
				if(form==null){
					//Si no esta creado todavia, lo crea
					Dimension size=m_preferredSize;
					if(m_component!=null)
						size=m_component.getPreferredSize();
					buildForm(idoParent, idtoParent, idPropParent, ido, idto, size, m_keyListener);
					ArrayList<Integer> idosNotFixed=new ArrayList<Integer>();
					idosNotFixed.add(ido);//Asi evitamos que lo compruebe tambien para el ido ya que eso se hace luego
					showFixedObjects(idoParent,idtoParent,null,idosNotFixed);
				}
			}
			
			JPanel form = (JPanel) m_mapForm.get(new Integer(value));
			if(form==null){
				//Si no esta creado todavia, lo crea
				Dimension size=m_preferredSize;
				if(m_component!=null)
					size=m_component.getPreferredSize();
				buildForm(ido, idto, idProp, value, valueCls, size, m_keyListener);
				showFixedObjects(ido,idto,idProp,new ArrayList<Integer>());
				form = (JPanel) m_mapForm.get(new Integer(value));
			}
			if (form != null) {
				if (m_subFilterView.isAncestorOf(form))
					return;// ya está visible
				//m_subFilterView.removeAll();			
				m_subFilterView.setViewportView(form);//add(form);			
				Component padre = m_subFilterView.getParent();
				if (padre != null) {// Si tiene padre nos interesa actuar sobre el
					// para que muestre el scroll si es necesario
					padre.validate();
					padre.repaint();
					m_subFilterView.revalidate();
					m_subFilterView.repaint();
				} else {
					m_subFilterView.revalidate();
					m_subFilterView.repaint();
				}
			}
		}finally{
			m_kba.clearInstance();
		}
	}

	public int exeQuery() throws SystemException,RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, DataErrorException, NumberFormatException, InstanceLockedException, SQLException, NamingException, JDOMException, ParseException, OperationNotPermitedException, AssignValueException{

		Element query = buildQuery();
		
		
		ArrayList<RowItem> result = m_com.serverGetQueryRowItem( /*m_com.getUser(), m_com.getBusiness(),*/ query, m_idtoUserTask);
		int size=result.size();
		// jdomParser.print("FCTX RES:",result);
		setRows(result, resultPanel.PANEL_RESULTADOS);
		m_resultPanel.getFormManager().orderRows(m_resultPanel.getId());//Ordenamos filas por rdn o por primera columna si no esta el rdn
		setRows(new selectData(), resultPanel.PANEL_FAVORITOS);
		
		if(m_resultPanel.getDetailPanel()!=null && m_resultPanel.getDetailPanel().isShowing()){
			if(!result.isEmpty()){
				RowItem ritem=result.get(0);
				GIdRow idRow=ritem.getIdRow();
				m_resultPanel.selectRow(idRow.getIdo());
				m_resultPanel.buildDetailForm(idRow.getIdo(), idRow.getIdto(), m_userRol, m_idtoUserTask);
			}else{
				m_resultPanel.removeDetailForm();
			}
		}
		
		//if(!m_instanceFavourites.equals(m_instanceFilter)){
			//System.err.println("No son iguales");
		
			//Actualizamos favoritos con la información que hay en base de datos
		
//			ArrayList<GIdRow> fav=m_resultPanel.getData(resultPanel.PANEL_FAVORITOS);
//			if(fav.size()>0){
//				Iterator<GIdRow> itr=fav.iterator();
//				ArrayList<String> rdns=new ArrayList<String>();
//				ArrayList<Integer> idos=new ArrayList<Integer>();
//				ArrayList<GIdRow> rowForSearch=new ArrayList<GIdRow>();
//				while(itr.hasNext()){
//					GIdRow idRow=itr.next();
//					// Si el resultado lo contiene no necesitamos volver a buscarlo ya que al haberlo insertardo se actualizo tambien favoritos
//					if(!result.containsInstanceWithIdo(idRow.getIdo())){
//						//Solo nos interesa para los que son de base de datos ya que podemos tener en favoritos prototipos que aun no se han enviado si estamos en un formulario modal
//						if(!Constants.isIDTemporal((idRow.getIdo()))){
//							rdns.add(idRow.getRdn());
//							idos.add(idRow.getIdo());
//							rowForSearch.add(idRow);
//						}
//					}
//				}
//				if(!rdns.isEmpty()){
//					selectData select=getRowFromDataBase(idos,rdns);
//					itr=rowForSearch.iterator();
//					ArrayList<GIdRow> remove=new ArrayList<GIdRow>();
//					while(itr.hasNext()){
//						GIdRow idRow=itr.next();
//						//Si en la busqueda no hemos encontrado ese favorito porque no cumple con el filtro o porque se ha borrado, lo quitamos de la tabla
//						if(!select.containsInstanceWithIdo(idRow.getIdo())){
//							remove.add(idRow);
//						}
//					}
//					m_resultPanel.delRows(remove,resultPanel.PANEL_FAVORITOS);
//					addRows(select, resultPanel.PANEL_FAVORITOS);
//				}
//			}
		//}else{
		//	System.err.println("Son iguales");
		//}
		result=null;
		lastQuery=query;
		return size;
	}

	public void setRows(Object table, int tipo) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException {
		setInsertandoRowsSt(true);
		m_resultPanel.setRows(table, tipo);
		setInsertandoRowsSt(false);
	}
	
	public void addRows(Object table, int tipo) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException {
		setInsertandoRowsSt(true);
		m_resultPanel.addDataRows(table, tipo);
		setInsertandoRowsSt(false);
	}

	private void setInsertandoRowsSt(boolean newSt) {
		m_listResult.setInsertandoRows(newSt);
		if (m_listFav != null)
			m_listFav.setInsertandoRows(newSt);
	}

	public void resetAll() throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ParseException, AssignValueException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException{
		/*
		 * subResetAll( m_filter ); m_filter= (Element)m_filtroOriginal.clone();
		 * inicializaFilter(m_filter);
		 */
//		ArrayList<String> m_listIdReset=new ArrayList<String>();
//		m_listIdReset.addAll(m_listIdChanged);
//		//System.out.println("instance antes de resetAll:"+m_instanceFilter);
//		//System.out.println("instance antes de cambiados:"+m_listIdChanged);
//		Iterator<String> itr = m_listIdReset.iterator();
//		while (itr.hasNext()) {
//			String idComponent = itr.next();
//			IdObjectForm idObjForm=new IdObjectForm(idComponent);
//			FormManager formManager=m_mapFormManager.get(idObjForm.getIdo());
//			if(!formManager.initValueComponent(idComponent)){//Elecom se encarga de llamar a setValue
//				// Si al intentar inicializar devuelve false significa que no se trata de un
//				// campo sino de un objeto. Por lo tanto se trata de quitar valores fijados para ese id.
//				int idObject=idObjForm.getIdo();
//				int idProp=idObjForm.getIdProp();
//				ObjectProperty property;
//				m_kba.setInstance(m_instanceFilter);
//				try{
//					property=m_kba.getChild(idObject, idProp, m_userRol, m_idtoUserTask, null);
//				}finally{
//					m_kba.clearInstance();
//				}
//				//if(property.getEnumList()==null || property.getEnumList().isEmpty()){
//				Iterator<Integer> itrRange=property.getRangoList().iterator();
//				while(itrRange.hasNext())
//					resetObject(idObject, idProp, itrRange.next());
//				//}
//			}			
//		}
		
		//Si las sesiones avisaran en el rollback no haria falta nada de lo de arriba
		
		//Antes de hacer rollback cambiamos la sesion interna por la sesion padre porque el rollback produce avisos que deben ser tratados por la sesion original
		Session auxSession=m_sessionInternal;
		m_sessionInternal=m_session;
		auxSession.rollBack();
		
		m_sessionInternal=createSessionInternal();//Al hacer rollback tenemos que crearnos una nueva sesion
		m_sessionInternal.addIchangeProperty(this,false);
		m_sessionInternal.addIMessageListener(m_actionManager);
		//System.out.println("instance despues de resetAll:"+m_instanceFilter);
		m_resultPanel.deleteAllRows(resultPanel.PANEL_FAVORITOS);
		m_resultPanel.deleteAllRows(resultPanel.PANEL_RESULTADOS);
		m_resultPanel.removeDetailForm();
		//m_listIdChanged.removeAll(m_listIdChanged);
		
		//Ejecutamos el recolector de basura para que no se acumule memoria ocupada
		Utils.forceGarbageCollector();
	}
	
	public void selectDeselectAll(){
		m_resultPanel.selectAllRows(!m_resultPanel.isSelectAllRows());
	}
	
	public Session createSessionInternal(){
		return m_kba.createDefaultSession(m_kba.getDefaultSession(),m_idtoUserTask,false, false, false, false,false);
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		try{
			//System.out.println("CPLEX EVENTO FILTER CONTROL");
		
			String command = e.getActionCommand();
			//System.out.println("OPGUI:COMMAND:" + command);
			/*
			 * String[] buf = command.split(":");
			 * 
			 * String operationType=buf[0]; Integer idProp =
			 * !buf[1].equals("null")?Integer.parseInt(buf[1]):null; Integer
			 * idUserTask = !buf[2].equals("null")?Integer.parseInt(buf[2]):null;
			 * int buttonType = Integer.parseInt(buf[3]);
			 */

			IdOperationForm idOperation = new IdOperationForm(command);
			Integer operationType = idOperation.getOperationType();
			Integer buttonType = idOperation.getButtonType();

			IdObjectForm target = idOperation.getTarget();
			Integer idProp = target.getIdProp();
			Integer idObject = target.getIdo();
			Integer idtoUserTask = target.getIdtoUserTask();
			Integer value= target.getValueCls();
		
			exeEvent(e.getSource(),operationType,idObject,idProp,value,idtoUserTask,buttonType);
			
		}catch(Exception ex){
			//m_control.closeEvent();
			
			m_com.logError(dialog.getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}
	}
	
	private void exeEvent(final Object source,final Integer operationType,final Integer idObject,final Integer idProp, final Integer value, final Integer idtoUserTask,final int buttonType){
		
		dialog.disabledEvents();
//		if(!m_popupMode){//No nos sirve mostrar nada en la barra cuando es popup ya que el formulario de abajo no cambia al ser una imagen
//			Object boton = source;
//			if (boton instanceof AbstractButton) {
//				if (((AbstractButton) boton).getToolTipText() != null)
//					Singleton.getInstance().getStatusBar().setLocalizacion(
//							((AbstractButton) boton).getToolTipText(),
//							Singleton.getInstance().getStatusBar().getNivelLocalizacion() + 1);
//			}
//
//			if (buttonType == botoneraAccion.ABRIR)
//				Singleton.getInstance().getStatusBar().setBarraProgreso();
//		}
		// Hacemos que se ejecute en un nuevo hilo para mantener libre el hilo
		// AWT-EventQueue

		SwingWorker worker = new SwingWorker() {
			public Object construct() {	
				boolean success=false;
				try{
					Object result=doWorkEvent(operationType, idObject, idProp, value,idtoUserTask, buttonType);
					success=true;
					return result;
				}finally{
					if(!success)
						doFinished();
				}
			}

			public void finished() {
				StatusBar statusBar = Singleton.getInstance().getStatusBar();
				/*if (statusBar.hasBarraProgreso())
					statusBar.setFinishBarraProgreso((String) getValue(), true);
				else*/ if (!statusBar.isError() && getValue()!=null)
					statusBar.setAccion((String) getValue());
				dialog.enabledEvents();
				//if ((String) getValue() == null)
				//	statusBar.upNivelLocalizacion();						
			}
		};
		worker.start();
	}

	private synchronized Object doWorkEvent(Integer operationType, Integer idObject,
			Integer idProp, Integer valueCls, Integer idtoUserTask, int buttonType) {
			String mensajeRespuesta = "";
		try{
			//System.err.println("filterControl m_idtoUserTask:"+m_idtoUserTask+" idtoUserTask:"+idtoUserTask);
			// Sesion padre que se le pasara a la nueva accion. No sera hija de la actual cuando se cambie de usertask
			//TODO Ahora siempre los cambios hechos aqui, en las operaciones new,set y del van a base de datos directamente.
			//Session sessionParentAction=m_kba.getDDBBSession();//idtoUserTask==null || idtoUserTask.intValue()==m_idtoUserTask?m_session:m_kba.getDDBBSession();
			

//			if (/* idProp.intValue() == 0 */idProp == null) { // cuando viene del
//			// txControl viene
//			// como cero
			if (buttonType == botoneraAccion.EJECUTAR) {
				confirm();
			}
			if (buttonType == botoneraAccion.RESET_ALL) {
				//System.out.println("IN RESET");
				resetAll();
			}
			if (buttonType == botoneraAccion.CREAR) {
				//int modo=access.NEW;

				ArrayList<commandPath> commandList = new ArrayList<commandPath>();
				/*commandList.add(new commandPath(idObject,modo,idtoUserTask));*/

				int idtoObject=m_kba.getClass(idObject);
				ObjectProperty property=m_kba.getChild(idObject, idtoObject, idProp, m_userRol, idtoUserTask, m_sessionAction);
				int idRange=m_kba.getIdRange(property, m_kba.getClass(valueCls));
				int idtoRange=m_kba.getClass(idRange);//Nos quedamos con la clase ya que si no la creacion estara afectada por el filtro de busqueda
				//int idtoFilter=value;
				NewCommandPath commandPath=new NewCommandPath(idObject,idtoObject,idProp,idtoRange,idtoUserTask,m_userRol,m_sessionAction);
				commandList.add(commandPath);

				exeActions(commandList,null,dialog);

				mensajeRespuesta="Formulario de inserción creado";
			}
			if (buttonType == botoneraAccion.EDITAR || buttonType == botoneraAccion.CONSULTAR) {
				// try{

				int modo = buttonType == botoneraAccion.EDITAR ?access.SET:access.VIEW;

				ArrayList<GIdRow> datList = m_resultPanel.getDataSelectedRows();

				if (datList.isEmpty()) {
					Singleton.getInstance().getMessagesControl().showMessage("Debe seleccionar al menos un registro",dialog.getComponent());
					return null;
				}
				/*if (datList.size() > 1) {
					Singleton.getInstance().getMessagesControl().showMessage("Debe seleccionar un único registro");
					return null;
				}*/
				
				ArrayList<commandPath> commandList = new ArrayList<commandPath>();
				if (datList.size() > 1) {
					
					HashMap<Integer,Integer> listIdo = new HashMap<Integer,Integer>();
					Iterator<GIdRow> it = datList.iterator();
					while(it.hasNext()){
						GIdRow idRow = it.next();
						if(modo==access.SET && m_kba.isPointed(idRow.getIdo(), idRow.getIdto())){
							Singleton.getInstance().getMessagesControl().showErrorMessage("No se puede editar '"+idRow.getRdn()+"' ya que está siendo utilizado en otra ventana de la aplicación",dialog.getComponent());
							return null;
						}
						listIdo.put(idRow.getIdo(),idRow.getIdto());
					}
					commandPath commandPath=null;

					if(modo==access.SET)                        			
						commandPath=new SetCommonCommandPath(m_ido,listIdo, idtoUserTask, m_userRol,m_sessionAction);        						
					else if(modo==access.VIEW)                        			
						commandPath=new ViewCommonCommandPath(m_ido,listIdo, idtoUserTask, m_userRol,m_sessionAction);

					commandList.add(commandPath);
					exeActions(commandList,null,dialog);

				}else{
					/*editarInstance(idProp, idtoUserTask, parSelected, true, modo);*/
					GIdRow rowSelected= datList.iterator().next();

					/*commandList.add(new commandPath(rowSelected.getIDO(),modo,idtoUserTask));*/
					commandPath commandPath;
					if(modo==access.VIEW)
						commandPath=new ViewCommandPath(m_ido,m_idto,rowSelected.getIdo(),rowSelected.getIdto(),idtoUserTask,m_userRol,m_sessionAction);
					else{
						if(m_kba.isPointed(rowSelected.getIdo(), rowSelected.getIdto())){
							Singleton.getInstance().getMessagesControl().showErrorMessage("No se puede editar '"+rowSelected.getRdn()+"' ya que está siendo utilizado en otra ventana de la aplicación",dialog.getComponent());
							return null;
						}
						commandPath=new SetCommandPath(m_ido,m_idto,rowSelected.getIdo(),rowSelected.getIdto(),idtoUserTask,m_userRol,m_sessionAction);
					}
					commandList.add(commandPath);

					exeActions(commandList,m_resultPanel,dialog);
				}
			}
			if (buttonType == botoneraAccion.ASIGNAR) {
				if(asignar(m_resultPanel)==null)
					return null;
			}
			if (buttonType == botoneraAccion.CANCEL) {
				cancel();

				mensajeRespuesta="Acción cancelada";
			}
//			} else {
			else if (buttonType == botoneraAccion.ABRIR) {

				mensajeRespuesta = "Formulario de filtro mostrado";
				/*
				 * Lo creamos en otro hilo ya que espera a que el formulario
				 * modal que crea se cierre
				 */
				final Integer idPropThis = idProp;
				final Integer idObjectThis = idObject;
				final Integer valueClsThis=valueCls;
				/* final Integer parentThis=parent; */

				SwingWorker worker = new SwingWorker() {
					public Object construct() {
						boolean currDebugVal = Singleton.getInstance().getDebugLog().getEnableDebug();
						Singleton.getInstance().getDebugLog().setEnableDebug(false);
						try {
							
							//System.err.println("SYSERRAMI: idObjectThis="+idObjectThis+" idPropThis="+idPropThis+" valueThis="+valueThis);
							ArrayList<commandPath> commandList = new ArrayList<commandPath>();
							/*commandList.add(new commandPath(idoTable,idPropTable,-1,access.SET,access.OVER_PROPERTY,idtoUserTask));*/
							
							FindRelCommandPath commandPath=new FindRelCommandPath(idObjectThis,idPropThis,valueClsThis,m_idtoUserTask,m_userRol,m_sessionInternal, m_instanceFilter);
							commandList.add(commandPath);
							exeActions(commandList,null,dialog);
							return null;
//							if (subFilterSelection(idObjectThis, idPropThis, valueThis))
//								return "Valor fijado";
//							else
//								return "Ningún valor fijado";
						} catch (Exception e) {//TODO Quizas haya excepciones que si haya que permitir sin ser enviadas al server
							m_com.logError(dialog.getComponent(),e,"Error al buscar ó intentar fijar un valor");
							e.printStackTrace();
							return "Error";
						} finally {
							Singleton.getInstance().getDebugLog().setEnableDebug(currDebugVal);
						}
					}

					public void finished() {
//						Singleton.getInstance().getStatusBar().setAccion(
//								(String) getValue());
					}
				};
				worker.start();

			};
			if (buttonType == botoneraAccion.RESET) {
				resetObject(idObject, idProp/*, value*/);
				mensajeRespuesta = "Valor fijado reseteado";
			}
			
			if (buttonType == botoneraAccion.ELIMINAR) {
				resultPanel rp = this.getResultPanel();
				ArrayList<GIdRow> parList = rp.getDataSelectedRows();
				if (parList.isEmpty()) {
					Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN OBJETO",dialog.getComponent());
					return null;
				}
				/*if (parList.size() > 1) {
            	   m_control.showMessage("DEBE SELECCIONAR UN SOLO OBJETO");
                   return null;
               }*/
				String textNumElementos="el elemento";
				if (parList.size() > 1)
					textNumElementos="los "+parList.size()+" elementos";
				
				Object[] options = {"Sí", "No"};
				int res = Singleton.getInstance().getMessagesControl().showOptionMessage("¿Está seguro que desea borrar "+textNumElementos+"?",
						Utils.normalizeLabel("CONFIRMACIÓN DE BORRADO"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[1],dialog.getComponent());

				if (res != JOptionPane.YES_OPTION)return "Eliminación de registro/s cancelada";
				boolean delete=false;
				if(deleteObjects(this,m_sessionAction,parList,idtoUserTask,m_userRol,false))
					delete=true;
				
				if(delete)
					mensajeRespuesta="Registro/s eliminado/s";
				else mensajeRespuesta="No se puede eliminar el/los registro/s";
				
			}
//			}
			return mensajeRespuesta;
		} catch (Exception e) {
			e.printStackTrace();
			m_com.logError(dialog.getComponent(),e,"Error al ejecutar la operación");
			return "Error de la operación";
		}
	}
	
	public static boolean deleteObjects(filterControl filterC,Session session,ArrayList<GIdRow> listIdRow,int idtoUserTask,Integer userRol, boolean force) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, HeadlessException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, AssignValueException{
		KnowledgeBaseAdapter kba=filterC.getKnowledgeBase();
		ActionManager actionManager=Singleton.getInstance().getActionManager();
		ArrayList<GIdRow> listDelete= new ArrayList<GIdRow>();
		try{
			//System.out.println("Antes de borrar");
			//m_kba.getSizeMotor();
			

				//Creamos una nueva sesion para la confirmacion final ya que al parar la edicion y al poner el estado de realizado se pueden disparar reglas para hacer operaciones finales. Estas modificaciones
				//tenemos que ser capaces de quitarlas si se produce un fallo en la coherencia o en base de datos. Teniendo una sesion hija como esta, podemos hacerlo sin perder datos anteriores. Ademas la
				//sesion hija creada es del tipo forceParent=true para forzar a nuestra m_session a que haga commit.
				DefaultSession sessionDelete=kba.createDefaultSession(session,idtoUserTask,true,true,true,true,true);
				boolean success=false;
				try{
					int idoUserTask=kba.getIdoUserTask(idtoUserTask);
					Iterator<GIdRow> itr=listIdRow.iterator();
					LinkedList<Value> listValue=new LinkedList<Value>();
					while(itr.hasNext()){
						GIdRow idRow = itr.next();
						if(!force && (kba.isLoad(idRow.getIdo()) || kba.isPointed(idRow.getIdo(), idRow.getIdto()))){
							Singleton.getInstance().getMessagesControl().showErrorMessage("No se puede eliminar '"+idRow.getRdn()+"' ya que está siendo utilizado en otra ventana de la aplicación",filterC.getComponent());
						}else{
							listValue.add(kba.buildValue(idRow.getIdo(), idRow.getIdto()));
							listDelete.add(idRow);
						}
					}
					
					// try{
					//m_kba.setUserTaskState(idoUserTask, Constants.IDO_INICIALIZANDO, userRol, idtoUserTask, sessionDelete);
					//System.err.println("Estado:"+m_kba.getProperty(idoParent, Constants.IdPROP_ESTADOREALIZACION, userRol, idtoUserTask, ses));
				//}finally{
					//m_kba.setUserTaskState(idoUserTask, Constants.IDO_PENDIENTE, userRol, idtoUserTask, sessionDelete);
					
					// Es importante hacer el setValue de todos los borrados a la vez para que si la regla tiene que decir algo sea mas eficiente teniendo toda la informacion
					kba.setValue(/*property,*/idoUserTask,Constants.IdPROP_TARGETCLASS, listValue,null/*, new session()*/,/*operation*/userRol,idtoUserTask,sessionDelete);
					itr=listDelete.iterator();
					while(itr.hasNext()){
						GIdRow idRow = itr.next();
						kba.deleteObject(idRow.getIdo(),idRow.getIdto(),idRow.getRdn(),userRol,idtoUserTask,sessionDelete);
					}
					//kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PREREALIZADO, userRol, idtoUserTask, sessionDelete);
					kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_REALIZADO, userRol, idtoUserTask, sessionDelete);
					sessionDelete.commit();
					
					filterC.getResultPanel().delRows(listDelete, resultPanel.PANEL_RESULTADOS);
					filterC.getResultPanel().delRows(listDelete, resultPanel.PANEL_FAVORITOS);
					filterC.getResultPanel().removeDetailForm();
					
					success=true;
				}finally{
					if(!success){
						sessionDelete.setForceParent(false);
						try{
							sessionDelete.rollBack();
						}catch(Exception ex){
							System.err.println("No se ha podido hacer rollback de la session");
							ex.printStackTrace();
						}
					}
				}
			//System.out.println("Borrado:"+inst);
			
		}catch(OperationNotPermitedException ex){
			Singleton.getInstance().getMessagesControl().showErrorMessage(ex.getUserMessage(),filterC.getComponent());
			return false;
		}finally{
			StatusBar statusBar = Singleton.getInstance().getStatusBar();
//			if(statusBar.getNivelLocalizacion()==2)
//			//if(statusBar.getNivelLocalizacion()>0)
//				 statusBar.upNivelLocalizacion();
			
			//System.out.println("Despues de borrar");
			//m_kba.getSizeMotor();
		}
		return listDelete.size()>0;
	}

	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, NumberFormatException, InstanceLockedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		String mensajeRespuesta=null;
		boolean currDebugVal = Singleton.getInstance().getDebugLog().getEnableDebug();
		Singleton.getInstance().getDebugLog().setEnableDebug(false);
		try {
				exeQuery();
		}finally {
			Singleton.getInstance().getDebugLog().setEnableDebug(currDebugVal);
		}

		return mensajeRespuesta;
	}

	private Object asignar(resultPanel rp) throws NotFoundException, IncoherenceInMotorException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, IncompatibleValueException, CardinalityExceedException{
		ArrayList<GIdRow> parList = rp.getDataSelectedRows();
		//System.err.println("FilterControl:asignar "+parList);
		
		
		//Si es filtro se permite asignar sin tener en cuenta la cardinalidad maxima
		if (m_kba.getLevelObject(m_idoParent)!=Constants.LEVEL_FILTER && m_nMaxSelection!=null && parList.size() > m_nMaxSelection) {
			Singleton.getInstance().getMessagesControl().showMessage("Solo puede seleccionar "+ m_nMaxSelection + " elementos",dialog.getComponent());
			return null;
		}
		if (parList.size()==0) {
			Singleton.getInstance().getMessagesControl().showMessage("No ha seleccionado ningún registro",dialog.getComponent());
			return null;
		}
		boolean success=false;
		try {
			Iterator<GIdRow> itr=parList.iterator();
			//m_kba.setInstance(null);
			//try {
				ObjectProperty o = m_kba.getChild(m_idoParent,m_idtoParent,m_idPropParent, m_userRol,m_idtoUserTask, m_session);
				LinkedList<Value> valuesList=new LinkedList<Value>();
				while(itr.hasNext()){
					GIdRow idRow=itr.next();
					int ido=idRow.getIdo();
					int idto=idRow.getIdto();//m_kba.getRDN(ido, m_userRol, m_idtoUserTask, m_session).getIdto();
					Value valueObject=m_kba.buildValue(ido, idto);
					
	//				if (instanceAsigned!=null){
	//					m_kba.setInstance(instanceAsigned);
	//				}else{
	//					m_kba.clearInstance();
	//				}
					
					valuesList.add(valueObject);
				}
				
				try{
					if(m_kba.getLevelObject(m_idoParent)!=Constants.LEVEL_FILTER && o.getCardMax()!=null && o.getCardMax()==1){
						m_kba.setValue(m_idoParent,m_idPropParent, valuesList.getFirst() ,o.getUniqueValue(), m_userRol,m_idtoUserTask, m_session);	
					}else
						m_kba.setValue(m_idoParent,m_idPropParent, valuesList,null, m_userRol,m_idtoUserTask, m_session);
					
					m_selectioned=true;
				} catch (IncompatibleValueException e) {
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
				} catch (CardinalityExceedException e) {
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
				}
				
			/*}finally{
				m_kba.clearInstance();
			}*/
			m_session.commit();
			m_actionManager.closeForm(dialog,m_kba,true);
			success=true;			
		} finally{
			//m_sessionInternal.removeIchangeProperty(this);
			//m_sessionInternal.rollBack();
			if(!success){
				m_session.removeIchangeProperty(this);
				m_session.rollBack();
				m_actionManager.closeForm(dialog,m_kba,false);
//				if(m_sessionAction!=null){
//					m_sessionAction.rollBack();
//					m_sessionAction.dispose();//Tenemos que hacer dispose ya que la sesion es reusable. No nos interesa ponerlo no reusable antes del rollback ya que si no tambien haria dispose del ik
//				}
			}
		}
		
		return 1;
	}

	public void resetObject(int ido, int idProp/*, int value*/) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {

		//m_kba.setInstance(m_instanceFilter);
		//try{
			ObjectProperty property = m_kba.getChild(ido, m_kba.getClass(ido), idProp, m_userRol,m_idtoUserTask,m_sessionInternal);
//			Integer idObject = value;//m_kba.getIdoFilter(property);

//			if (m_subNodeFix.containsKey(idObject)) {// contiene referencia al
//				// controlador de fix
//				// subform
//				// El clone es necesario porque property es la original de instance y al borrar un valor se veria reflejado en el bucle

				LinkedList<Value> listObjV=new LinkedList<Value>();
				for(int i=0;i<property.getValues().size();i++){
					Value v = property.getValues().get(i);
					listObjV.add(v.clone());//(LinkedList<Value>)property.getValues().clone();
				}
				int size=listObjV.size();
				for(int i=0;i<size;i++){
					Value valueObject = listObjV.get(i);
					//System.out.println("Reset valor:" + valueObject + " en ido:" + ido + " idProp:" + idProp);
					m_kba.setValue(ido,idProp, null, valueObject,m_userRol,m_idtoUserTask,m_sessionInternal);
				}

//				panelSelection text = (panelSelection) m_subNodeFix.get(idObject);
//				text.reset();
//				m_selector.setRdn(idObject, null);
//				m_favDataCache.clear();
//			}
		/*}finally{
			m_kba.clearInstance();
		}*/
	}

	/*private boolean subFilterSelection(int ido,	int idProp,int value) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{

		Integer idObject;
		ObjectProperty property;
		m_kba.setInstance(m_instanceFilter);
		try{
			property = m_kba.getChild(ido, idProp, m_userRol,m_idtoUserTask,null);
			idObject=value;
		}finally{
			m_kba.clearInstance();
		}
		instance instanceSubFilter=null;
		
		instanceSubFilter = m_kba.getTreeObject(idObject, m_userRol,m_idtoUserTask,m_sessionInternal,KnowledgeBaseAdapter.ALL_MODE);
		
		m_kba.setInstance(instanceSubFilter);

		if(m_bussinessClassMode)
			setBussinessClassMode(idObject);
		
		//System.out.println("subfilter:"+instanceSubFilter);
		Element query = buildQuery(instanceSubFilter);

		selectData result = m_com.serverGetQuery(query);

		Dimension dim=new Dimension((int)m_preferredSize.getWidth(),(int)m_preferredSize.getHeight()/3);
		MouseAdapter mouseListenerAsignar=null;
		final resultPanel rp = new resultPanel(m_sessionInternal,false, false, false, m_idtoUserTask,property,idObject,m_userRol,m_colorFondo, m_graphics,dim, mouseListenerAsignar, true, -1);
		mouseListenerAsignar = new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2){
					try{
						asignar(rp);
					} catch (Exception ex) {
						m_com.logError(dialog.getComponent(),ex,"Error al asignar");
						ex.printStackTrace();						
					}
				}					
			}
		};
		rp.setMouseListener(mouseListenerAsignar);

		rp.setRows(result, resultPanel.PANEL_RESULTADOS);
		m_kba.clearInstance();
		int res = JOptionPane.showConfirmDialog(dialog, rp.getComponent(), Utils.normalizeLabel("SELECCIONAR " + m_kba.getLabelProperty(property, property.getIdto(), null, null)),
				JOptionPane.OK_CANCEL_OPTION);
		if (res == JOptionPane.CANCEL_OPTION)
			return false;
		selectData pars = rp.getDataSelectedRows(resultPanel.PANEL_RESULTADOS);

		if (!pars.hasData())
			return false;

		fixObject(ido, idProp, idObject,pars);

		return true;
	}*/

	public void fixObject(Integer idObject, Integer idtoObject, Integer idProp, int value,selectData data) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		if (idObject == null)
			return;
		ArrayList<String> rdns = new ArrayList<String>();
		String rdn = "";
		int longTotalRdns = 0;
		if ( data.hasData()) {
			String fixIdo = data.size() > 1 ? "{" : "";
			Iterator itr = data.getIterator();
			boolean first = true;

			ObjectProperty property;
			m_kba.setInstance(m_instanceFilter);
			try{
				property = m_kba.getChild(idObject/* m_id */,idtoObject, idProp,m_userRol, m_idtoUserTask, m_sessionInternal);
			}finally{
				m_kba.clearInstance();
			}

			LinkedList<Value> oldValues = property.getValues();
			LinkedList<Value> addValues = new LinkedList<Value>();
			LinkedList<Value> replacedValues = new LinkedList<Value>();
			while (itr.hasNext()) {
				instance datCol = (instance) itr.next();
				Value valueObject=m_kba.buildValue(datCol.getIDO(), datCol.getIdTo());
				addValues.add((ObjectValue)valueObject);
			}
			/*				ObjectValue objectV = new ObjectValue();
				objectV.setValue(datCol.getIDO());
				objectV.setValueCls(datCol.getIdTo());
				valueList.add(objectV);
			 */
			/*				Value valueObject=m_kba.getValueOfString(""+datCol.getIDO(), datCol.getIdTo());*/
			Iterator<Value> itrValues=addValues.iterator();
			while(itrValues.hasNext()){
				ObjectValue valueObject=(ObjectValue)itrValues.next();
				/*ObjectValue valueOldObject=*/getNextValueOld(oldValues,addValues,replacedValues);
//				try {
//					m_kba.setInstance(m_instanceFilter);
//					try{
//						System.out.println("Antes de fijar:"+m_kba.getProperty(idObject, idProp, m_userRol, m_idtoUserTask, m_sessionInternal));
//						m_kba.setValue(/*property*/idObject,idProp, valueObject, valueOldObject,/*action.NEW*//*, new session()*/m_userRol,m_idtoUserTask,m_sessionInternal);
//						System.out.println("Despues de fijar:"+m_kba.getProperty(idObject, idProp, m_userRol, m_idtoUserTask, m_sessionInternal));
//					}finally{
//						m_kba.clearInstance();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					JOptionPane.showMessageDialog(dialog, e
//							.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);//TODO getMessage no muestra el mensaje usuario de cardinalidades, incompatibleValue...
//				}
				/* String rdnDatCol= datCol.getRdn(); */
				//String rdnDatCol = m_kba.getValueData(m_kba.getRDN(/*datCol.getIDO()*/valueObject.getValue(), m_userRol, m_idtoUserTask, m_sessionInternal));
				String rdnDatCol = m_com.serverGetRdn(valueObject.getValue(),valueObject.getValueCls());
					
				rdns.add(rdnDatCol);
				longTotalRdns += rdnDatCol.length();
				if (first)
					rdn = rdnDatCol;

				if (!first)
					fixIdo += ";";
				fixIdo += /*datCol.getIDO()*/valueObject.getValue();
				first = false;

			}
//			m_kba.setInstance(m_instanceFilter);
//			ObjectProperty property = m_kba.getChild(idObject/* m_id */, idProp,
//			m_userRol, m_idtoUserTask);
//			System.out.println("Asigna en ido:" + idObject
//			+ " siendo la property:" + property + " y el valueList:"
//			+ valueList.toString());
//			try {
//			m_kba.setValueList(/* valueList, */idObject, /* idProp */
//			property, valueList/*, new session()*/);
//			} catch (Exception e) {
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(/* m_currentModalForm */null, e
//			.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//			}
//			m_kba.clearInstance();

			if (data.size() > 1) {
				fixIdo += "}";

				// if( data.size()>1 ){
				double porcent = 10.0 / ((double) (longTotalRdns + data.size() - 1));
				// int numCharItem= 15/
				rdn = "";
				for (int i = 0; i < rdns.size(); i++) {
					String nextR = (String) rdns.get(i);
					if (i > 0)
						rdn += ";";
					int longS = (int) (((double) nextR.length()) * porcent) - 1;
					if (longS == 0)
						longS = 1;
					/*System.out.println("PORCENT, TOT, THIS:" + porcent + ","
							+ longTotalRdns + "," + nextR.length());*/
					String anexo = porcent < 1 ? nextR.substring(0, longS + 1)
							: nextR;
					rdn += anexo;
				}
			}
			int idFilter = value;//m_kba.getIdoFilter(property);
			panelSelection text = (panelSelection) m_subNodeFix.get(idFilter);
			if (text == null)
				return;
			text.setSelection(rdns.toArray(), null);
			m_selector.setRdn(idFilter, rdn);
		}

		IdObjectForm idObjectForm = new IdObjectForm();
		idObjectForm.setIdo(idObject);
		idObjectForm.setIdProp(idProp);

		//m_listIdChanged.add(idObjectForm.getIdString());
	}

	/*
	 Obtiene el siguiente valueOld que se puede utilizar para hacer un setValue. Si en la lista
	 de los anteriores valores hay un valor que no aparece en addValues(nuevos valores) ni en
	 replacedValues(valores ya usados) sera el valueOld que utilizaremos. Una vez elegido se añade a
	 la lista replacedValues para que en las llamadas de los otros values de addValues no se utilicen
	 los mismos antiguos valores. Si no hay valores para elegir se devuelve null.
	 */
	private ObjectValue getNextValueOld(LinkedList<Value> oldValues,LinkedList<Value> addValues,LinkedList<Value> replacedValues){
		ObjectValue valueOld=null;
		Iterator<Value> itr=oldValues.iterator();
		boolean found=false;
		while(!found && itr.hasNext()){
			ObjectValue value=(ObjectValue)itr.next();
			if(!addValues.contains(value) && !replacedValues.contains(value)){
				valueOld=value;
				replacedValues.add(value);
				found=true;
			}
		}
		return valueOld;
	}

	public Element buildQuery() throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, JDOMException, ParseException, OperationNotPermitedException{
		if (m_listSelect == null) {
			m_listSelect = m_resultPanel.getFormManager().buildSelectQueryOfTable(m_resultPanel.getId());
			//m_listSelect = buildListSelect(m_instanceFilter,m_idtoParent);
		}
		//System.out.println("instanceConsulta:"+m_instanceFilter);
		boolean isGlobal=m_kba.isGlobalUtask(m_idtoUserTask);
		Element filter = m_kba.getQueryXML(m_instanceFilter, m_listSelect, m_userRol, m_idtoUserTask,isGlobal?1000:80000);
		return filter;
	}

//	public Element buildQuery(instance instanceFilter) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, JDOMException, ParseException, OperationNotPermitedException{
//		ArrayList<SelectQuery> listSelect = buildListSelect(instanceFilter,m_idtoParent);
//
//		Element filter = m_kba.getQueryXML(instanceFilter, listSelect, m_userRol, m_idtoUserTask,false);
//		return filter;
//	}
//
//	private ArrayList<SelectQuery> buildListSelect(instance instanceFilter,int idtoParent) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
//		ArrayList<SelectQuery> listSelect;/* =new LinkedList<SelectQuery>(); */
//
//		m_kba.setInstance(instanceFilter);
//		try{
//			listSelect = m_kba.getSelectQuery(instanceFilter.getIDO(),instanceFilter.getIdTo(),idtoParent, m_userRol, m_idtoUserTask, null, true, false, false);
//			//System.out.println("selectQuery de filterControl"+listSelect);
//		}finally{
//			m_kba.clearInstance();
//		}
//
//		return listSelect;
//	}


	@Override
	protected boolean doSetValue(int ido, int idProp, Value valueObject, Value valueOldObject) throws OperationNotPermitedException, NotFoundException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
		if(!m_sessionInternal.isFinished()){
			//System.err.println("Asigna valor en ido:"+ido+" idProp:"+idProp+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
			m_kba.setValue(ido,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,m_sessionInternal);
			return true;
		}else{
			System.err.println("WARNING: Intento de hacer un setValue con una sesion finalizada siendo Value:"+valueObject+" valueOld:"+valueOldObject);
			return false;
		}
	}

		public JComponent getComponent() {
		return m_component;
	}

	public LinkedHashMap<Integer,Integer> getResult() {
		LinkedHashMap<Integer,Integer> result=new LinkedHashMap<Integer,Integer>();
		ArrayList<GIdRow> selection=m_resultPanel.getDataSelectedRows();
		Iterator<GIdRow> itr=selection.iterator();
		while(itr.hasNext()){
			GIdRow idRow=itr.next();
			result.put(idRow.getIdo(),idRow.getIdto());
		}
		return result;
	}

	public resultPanel getResultPanel() {
		return m_resultPanel;
	}

	public JPanel getSelector() {
		return m_selector;
	}

	/*public JPanel getSubFilterView() {
		return m_subFilterView;
	}*/

	public boolean cancel() throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException {
		m_selectioned=false;
		//m_sessionInternal.rollBack();
		m_session.removeIchangeProperty(this);
		m_session.rollBack();
		m_actionManager.closeForm(dialog,m_kba,false);
//		if(m_sessionAction!=null){
//			m_sessionAction.rollBack();
//			m_sessionAction.dispose();//Tenemos que hacer dispose ya que la sesion es reusable. No nos interesa ponerlo no reusable antes del rollback ya que si no tambien haria dispose del ik
//		}
		m_kba.removeHistoryDDBBListener(this);
		return true;
	}

	public void initChangeValue() {
		listIdosIdtosChangedAction=new HashMap<Integer, Integer>();
		listIdosIdPropForFix=new HashMap<Integer, Integer>();
	}
	
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) {
		try{
			//System.err.println("Cambia valor filtro en ido:"+ido+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" el value:"+value+" y el valueold:"+oldValue);

			if(level==Constants.LEVEL_FILTER &&
					(Constants.isDataType(valueCls) ||
					(value!=null && !Constants.isIDTemporal(((ObjectValue)value).getIDOIndividual())) ||
					(oldValue!=null && !Constants.isIDTemporal(((ObjectValue)oldValue).getIDOIndividual())))){
			
				IdObjectForm idObjForm=new IdObjectForm();
				idObjForm.setIdo(ido);
				idObjForm.setIdProp(idProp);
				idObjForm.setValueCls(valueCls);
	
				String idString=idObjForm.getIdString();
	
				Property property;
				m_kba.setInstance(m_instanceFilter);
				try{
					property = m_kba.getProperty(ido/* m_id */, idto, idProp,m_userRol, m_idtoUserTask, null);
				}finally{
					m_kba.clearInstance();
				}
				//boolean changes=false;
				//System.err.println("Property:"+property);
				if(property!=null){//Significa que es de las properties del instance por lo que la tratamos
					m_kba.setInstance(m_instanceFilter);
					try{
						m_kba.setValue(ido,idProp,value,oldValue, m_userRol,m_idtoUserTask,null);
					}finally{			
						m_kba.clearInstance();
					}
					//FormManager formManager=m_mapFormManager.get(m_kba.getIdRange(property));
					FormManager formManager=m_mapFormManager.get(ido);
					if(value instanceof DataValue || oldValue instanceof DataValue){
						if(formManager!=null/*advancedFilter*/){
							if(formManager.hasComponent(idString)){
								formManager.setValueComponent(idString, value!=null?m_kba.getValueData((DataValue)value):null, oldValue!=null?m_kba.getValueData((DataValue)oldValue):null);
							}
							//changes=true;
						}
						if(formManagerSimple.hasComponent(idString))
							formManagerSimple.setValueComponent(idString, value!=null?m_kba.getValueData((DataValue)value):null, oldValue!=null?m_kba.getValueData((DataValue)oldValue):null);
					}else{
						if(formManager!=null/*advancedFilter*/){
							if(formManager.hasComponent(idString)){
								formManager.setValueComponent(idString,value!=null?((ObjectValue)value).getValue():null,oldValue!=null?((ObjectValue)oldValue).getValue():null);
							}else{
								listIdosIdPropForFix.put(ido, idProp);
							}
							//changes=true;
						}
						if(formManagerSimple.hasComponent(idString))
							formManagerSimple.setValueComponent(idString,value!=null?((ObjectValue)value).getValue():null,oldValue!=null?((ObjectValue)oldValue).getValue():null);
					}
					
					//if(changes){
						/*IdObjectForm idObjectForm = new IdObjectForm();
						idObjectForm.setIdo(ido);
						idObjectForm.setIdProp(idProp);
						idObjectForm.setValueCls(valueCls);*/
	//					m_listIdChanged.add(idString);
					//}
				}
				
			}else listIdosIdtosChangedAction.put(ido,idto);//Creación de idos que no van a base de datos por lo que tenemos que procesarlos para mostrarlos en favoritos y sean seleccionables por el usuario
			
		}catch(Exception e){
			m_com.logError(dialog.getComponent(),e,"Error en un cambio de valor");
			e.printStackTrace();
		}
	}
	
	public void endChangeValue() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		for(Integer ido:listIdosIdtosChangedAction.keySet()){
			int idto=listIdosIdtosChangedAction.get(ido);
			int operation=action.NEW;
			if(!Constants.isIDTemporal(ido) || m_resultPanel.getDataRow(ido,resultPanel.PANEL_FAVORITOS)!=null)
				operation=action.SET;
			DataProperty propRdn=m_kba.getRDN(ido, idto, m_userRol, m_idtoUserTask, m_session);
			changeHistory(ido, idto, (String)m_kba.getValueData(propRdn), ido, operation, m_idtoUserTask, m_session);
		}
		for(Integer ido:listIdosIdPropForFix.keySet()){
			int idProp=listIdosIdPropForFix.get(ido);
			int idto=m_kba.getClass(ido);
			
			fixObject(ido, idto, idProp/*, (ObjectValue)value*/);
		}
	}
	
	public void fixObject(int ido, int idto, int idProp/*, ObjectValue value*/) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
				
		ArrayList<String> rdns = new ArrayList<String>();
		String rdn = "";
		int longTotalRdns = 0;
		ObjectProperty property;
		
		//m_kba.setInstance(m_instanceFilter);
		//try{
		//TODO Usar m_session en vez de m_sessionInternal es porque si viene de un rollback m_sessionInternal no existe ya. Si getProperty cargara en esta sesion
		//algo tendriamos un problema pero como estoy pidiendo la property de un filtro que existe en motor y no se disparan reglas no habria problema.
		//No podemos usar m_instanceFilter porque si se hace reset de dos individuos fijados fallaria ya que existiria en el instance uno de los individuos
		//mientras se procesa el primero
			property = m_kba.getChild(ido/* m_id */, idto, idProp,m_userRol, m_idtoUserTask, m_session);
		//}finally{
		//	m_kba.clearInstance();
		//}
		
		LinkedList<Value> values=property.getValues();
		String fixIdo = values.size() > 1 ? "{" : "";
		if(!values.isEmpty()){
			Iterator<Value> itrValues=values.iterator();
			m_kba.setInstance(null);
			try{
				HashMap<Integer,HashSet<Integer>> idos=new HashMap<Integer, HashSet<Integer>>();
				while(itrValues.hasNext()){
					ObjectValue valueObject=(ObjectValue)itrValues.next();
					if(!idos.containsKey(valueObject.getValueCls())){
						HashSet<Integer> map=new HashSet<Integer>();
						idos.put(valueObject.getValueCls(), map);
					}
					idos.get(valueObject.getValueCls()).add(valueObject.getValue());
				}
				
				HashMap<Integer,String> rdnDatCol = m_com.serverGetRdn(idos);
				
					//String rdnDatCol = m_kba.getValueData(m_kba.getRDN(/*datCol.getIDO()*/valueObject.getValue(), m_userRol, m_idtoUserTask, m_sessionInternal));
				Iterator<Integer> itrResult=rdnDatCol.keySet().iterator();
				while(itrResult.hasNext()){
					Integer idoResult=itrResult.next();
					String rdnResult=rdnDatCol.get(idoResult);
					rdns.add(rdnResult);
					longTotalRdns += rdnResult.length();
					if (rdns.size()==1)
						rdn = rdnResult;
					
					fixIdo += ido;
					if(itrResult.hasNext()){
						fixIdo += ";";
					}
				}
			}finally{
				m_kba.clearInstance();
			}
		}
		
		if (values.size() > 1) {
			fixIdo += "}";

			// if( data.size()>1 ){
			double porcent = 10.0 / ((double) (longTotalRdns + values.size() - 1));
			// int numCharItem= 15/
			rdn = "";
			for (int i = 0; i < rdns.size(); i++) {
				String nextR = (String) rdns.get(i);
				if (i > 0)
					rdn += ";";
				int longS = (int) (((double) nextR.length()) * porcent) - 1;
				if (longS == 0)
					longS = 1;
				/*System.out.println("PORCENT, TOT, THIS:" + porcent + ","
						+ longTotalRdns + "," + nextR.length());*/
				String anexo = porcent < 1 ? nextR.substring(0, longS + 1)
						: nextR;
				rdn += anexo;
			}
		}
		if(property.getRangoList().isEmpty())
			System.err.println("La property no tiene rangoList: "+property);
		int idFilter = m_kba.getIdRange(property);
		panelSelection text = (panelSelection) m_subNodeFix.get(idFilter);
		if (text == null)
			return;
		text.setSelection(rdns.toArray(), null);
		m_selector.setRdn(idFilter, rdn);
	}

	public void initChangeHistory(){}
	
	public void endChangeHistory(){}
	
	public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
		try{
			//System.err.println("filterControl:"+m_kba.getClass(m_ido)+" m_ido:"+m_ido+" ido:"+ido+" m_idto:"+m_idto+"idto:"+idto+" rdn:"+rdn+" operation:"+operation+" idtoUserTask:"+idtoUserTask);
			KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBaseAdapter(sessionUsed.getKnowledgeBase());
			if((m_idto==idto || kba.isSpecialized(idto, m_idto))){
				//System.err.println("Antes pedir treeObject");
				//instance instTree=kba.getTreeObject(oldIdo, m_userRol, idtoUserTask, sessionUsed, false);
				
				if(operation==action.DEL_OBJECT || operation==action.SET){
					if(!m_resultPanel.hasRow(oldIdo))//Si no existe en la tabla no tenemos nada que actualizar
						return;  
				}
				
				if(operation==action.DEL_OBJECT){
					GIdRow idRow=new GIdRow(ido,idto,rdn);
					ArrayList<GIdRow> select=new ArrayList<GIdRow>();
					select.add(idRow);
					m_resultPanel.delRows(select, resultPanel.PANEL_FAVORITOS);
					m_resultPanel.delRows(select, resultPanel.PANEL_RESULTADOS);
				}else if((operation==action.NEW) || (operation==action.SET && (m_resultPanel.getDataRow(ido,resultPanel.PANEL_RESULTADOS)!=null || m_resultPanel.getDataRow(ido,resultPanel.PANEL_FAVORITOS)!=null))){
					if(kba.isCompatibleWithFilter(oldIdo,m_instanceFavourites,m_userRol,idtoUserTask)){
						DefaultSession sessionAux=kba.createDefaultSession(kba.getDefaultSessionWithoutRules(),idtoUserTask,false,false,false,false,false);
						Session sessionOldFormManager=m_resultPanel.getFormManager().m_session;
						boolean oldUpdateColumnWidth=m_resultPanel.getFormManager().updateColumnWidthWhenBuildRows;
						try{
							// Necesitamos cambiar la sesion de formManager ya que los resultPanel.addDataRows la utilizan
							m_resultPanel.getFormManager().m_session=sessionAux;
							
							// Aqui no nos interesa que recalcule el ancho de las columnas al añadir/modificar ya que esto ralentiza innecesariamente
							m_resultPanel.getFormManager().updateColumnWidthWhenBuildRows=false;
							
							//System.err.println("isCompatibleWithFilter: ido:"+ido+" idto:"+idto+" rdn:"+rdn+" operation:"+operation);
							if(/*operation==action.SET ||*/ operation==action.NEW){
								if(lastCreationInFavourites!=null)
									m_resultPanel.delRow(lastCreationInFavourites, resultPanel.PANEL_FAVORITOS);
								
								//selectData select=getRowFromDataBase(listIdo, listRdn);
								selectData select=new selectData();
								JTree columnTreeOfTable=m_resultPanel.getFormManager().getColumnTreeOfTable(m_resultPanel.getId());
								instance inst=kba.getTreeObjectTable(oldIdo, kba.getClass(oldIdo), m_resultPanel.getId(), columnTreeOfTable, m_userRol, idtoUserTask, sessionAux,false);
								
								DefaultMutableTreeNode root=(DefaultMutableTreeNode)columnTreeOfTable.getModel().getRoot();
								Enumeration e=root.children();
								boolean found=false;
								while(!found && e.hasMoreElements()){
									DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.nextElement();
									if(node.isLeaf()){
										IdObjectForm idObjForm=(IdObjectForm)node.getUserObject();
										if(idObjForm.getIdProp().equals(Constants.IdPROP_RDN)){
											StringValue dataVal = new StringValue();
											dataVal.setValue(rdn);
											inst.addValueColumn(idObjForm.getIdString(), dataVal);
											found=true;
										}
									}	
								}
								
								inst.setRdn(rdn, true);
								select.addInstance(inst);
								
								kba.setInstance(inst);
								try{
									m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
								}finally{
									kba.clearInstance();
								}
								
								GIdRow idRow=m_resultPanel.getDataRow(oldIdo,resultPanel.PANEL_FAVORITOS);
								lastCreationInFavourites=idRow;
								
								idRow.setIdo(ido);//Le ponemos el ido de base de datos ya que lo hemos creado utilizando el ido negativo
								
								m_resultPanel.setListFocus(resultPanel.PANEL_FAVORITOS);
								m_resultPanel.selectRow(ido);
								
							}else if(operation==action.SET){
								selectData select=null;
								GIdRow idRowFav=m_resultPanel.getDataRow(ido,resultPanel.PANEL_FAVORITOS);
								JTree columnTreeOfTable=m_resultPanel.getFormManager().getColumnTreeOfTable(m_resultPanel.getId());
								if(idRowFav!=null){
									//select=getRowFromDataBase(listIdo, listRdn);
									select=new selectData();
									instance inst=kba.getTreeObjectTable(ido, idto, m_resultPanel.getId(), columnTreeOfTable, m_userRol, idtoUserTask, sessionAux,false);
									
									DefaultMutableTreeNode root=(DefaultMutableTreeNode)columnTreeOfTable.getModel().getRoot();
									Enumeration e=root.children();
									boolean found=false;
									while(!found && e.hasMoreElements()){
										DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.nextElement();
										if(node.isLeaf()){
											IdObjectForm idObjForm=(IdObjectForm)node.getUserObject();
											if(idObjForm.getIdProp().equals(Constants.IdPROP_RDN)){
												StringValue dataVal = new StringValue();
												dataVal.setValue(rdn);
												inst.addValueColumn(idObjForm.getIdString(), dataVal);
												found=true;
											}
										}	
									}
									
									inst.setRdn(rdn, true);
									select.addInstance(inst);
									//if(select.size()>0)
										m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
									/*else{
										selectData selectFav=new selectData();
										selectFav.addInstance(instanceFav);
										m_resultPanel.delRows(selectFav, resultPanel.PANEL_FAVORITOS);
									}*/
								}
								GIdRow idRowResult=m_resultPanel.getDataRow(ido,resultPanel.PANEL_RESULTADOS);
								if(idRowResult!=null){
									if(select==null){
										//select=getRowFromDataBase(listIdo, listRdn);
										select=new selectData();
										instance inst=kba.getTreeObjectTable(ido, idto, m_resultPanel.getId(), columnTreeOfTable, m_userRol, idtoUserTask, sessionAux,false);
										
										DefaultMutableTreeNode root=(DefaultMutableTreeNode)columnTreeOfTable.getModel().getRoot();
										Enumeration e=root.children();
										boolean found=false;
										while(!found && e.hasMoreElements()){
											DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.nextElement();
											if(node.isLeaf()){
												IdObjectForm idObjForm=(IdObjectForm)node.getUserObject();
												if(idObjForm.getIdProp().equals(Constants.IdPROP_RDN)){
													StringValue dataVal = new StringValue();
													dataVal.setValue(rdn);
													inst.addValueColumn(idObjForm.getIdString(), dataVal);
													found=true;
												}
											}	
										}
										
										inst.setRdn(rdn, true);
										select.addInstance(inst);
									}
									//if(select.size()>0)
										m_resultPanel.addDataRows(select, resultPanel.PANEL_RESULTADOS);
									/*else{
										selectData selectRes=new selectData();
										selectRes.addInstance(instanceResult);
										m_resultPanel.delRows(selectRes, resultPanel.PANEL_RESULTADOS);
									}*/
								}
								
								/*selectData rows=m_resultPanel.getDataRows(resultPanel.PANEL_FAVORITOS);
								Iterator<instance> itrFav=rows.getIterator();
								while(itrFav.hasNext()){
									instance instFav=itrFav.next();
									if(instFav.getProperties().get(ido)!=null){
										selectData select=getRowFromDataBase(idto, rdn, idtoUserTask);
										m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
										m_resultPanel.setListFocus(resultPanel.PANEL_FAVORITOS);
										m_resultPanel.selectRow(ido);
									}
								}*/
								
							}
						}finally{
							m_resultPanel.getFormManager().m_session=sessionOldFormManager;
							m_resultPanel.getFormManager().updateColumnWidthWhenBuildRows=oldUpdateColumnWidth;
							sessionAux.rollBack();
						}
					}else{
						GIdRow idRow=new GIdRow(ido,idto,rdn);
						ArrayList<GIdRow> select=new ArrayList<GIdRow>();
						select.add(idRow);
						m_resultPanel.delRows(select, resultPanel.PANEL_FAVORITOS);
						m_resultPanel.delRows(select, resultPanel.PANEL_RESULTADOS);
					}
				}
			}
			
		}catch (Exception e) {
			m_com.logError(dialog.getComponent(),e, "Error al añadir a favoritos el individuo tratado");
			e.printStackTrace();
		}
	}
	

// Intento de solo procesar el ultimo ido llegado, pero no funciona correctamente ya que no me permite hacer isCompatibleWithFilter cuando aun esta el proto en motor, lo pide a bd
//	public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
//		try{
//			//System.err.println("filterControl:"+m_kba.getClass(m_ido)+" m_ido:"+m_ido+" ido:"+ido+" m_idto:"+m_idto+"idto:"+idto+" rdn:"+rdn+" operation:"+operation+" idtoUserTask:"+idtoUserTask);
//			if(m_idto==idto || m_kba.isSpecialized(idto, m_idto)){
//				//System.err.println("Antes pedir treeObject");
//				//instance instTree=m_kba.getTreeObject(oldIdo, m_userRol, idtoUserTask, sessionUsed, false);
//				
//				if(operation==action.DEL_OBJECT || operation==action.SET){
//					if(!m_resultPanel.hasRow(oldIdo))//Si no existe en la tabla no tenemos nada que actualizar
//						return;
//				}
//				
//				if(operation==action.DEL_OBJECT){
//					instance inst=new instance(idto,ido);
//					selectData select=new selectData();
//					select.addInstance(inst);
//					m_resultPanel.delRows(select, resultPanel.PANEL_FAVORITOS);
//					m_resultPanel.delRows(select, resultPanel.PANEL_RESULTADOS);
//				}else{
//					if(sessionUsed!=null){
//						InfoPossibleFavourite info=new InfoPossibleFavourite(ido,idto,rdn,oldIdo,operation,idtoUserTask);
//						if(!mapPossibleFavouriteBySession.containsKey(sessionUsed))
//							mapPossibleFavouriteBySession.put(sessionUsed, new ArrayList<InfoPossibleFavourite>());
//						
//						mapPossibleFavouriteBySession.get(sessionUsed).add(info);
//						
//						sessionUsed.addISessionStateListener(new ISessionStateListener(){
//
//							public void sessionClosed(Session session, boolean commit) {
//								try{
//									ArrayList<InfoPossibleFavourite> list=mapPossibleFavouriteBySession.get(session);							
//									int size=list.size();
//									boolean showInFavourites=true;//Nos indica si insertar o no en favoritos. Si ya hemos insertado uno en el bucle lo ponemos a false
//									
//									//Recorremos en orden inverso la lista ya que en favoritos solo mostramos uno. Y nos interesa mostrar el ultimo aviso que nos haya llegado
//									for(int i=size-1;i>=0;i--){
//										
//										InfoPossibleFavourite info=list.get(i);
//										
//										if((info.operation==action.NEW && showInFavourites) || (info.operation==action.SET && (m_resultPanel.getDataRow(info.ido,resultPanel.PANEL_RESULTADOS)!=null || m_resultPanel.getDataRow(info.ido,resultPanel.PANEL_FAVORITOS)!=null))){
//											if(m_kba.isCompatibleWithFilter(info.oldIdo,m_instanceFavourites,m_userRol,info.idtoUserTask)){
//												DefaultSession sessionAux=m_kba.createDefaultSession(m_kba.getDefaultSessionWithoutRules(),info.idtoUserTask,false,false,false,false,false);
//												Session sessionOldFormManager=m_resultPanel.getFormManager().m_session;
//												try{
//													// Necesitamos cambiar la sesion de formManager ya que los resultPanel.addDataRows la utilizan
//													m_resultPanel.getFormManager().m_session=sessionAux;
//													
//													System.err.println("isCompatibleWithFilter: ido:"+info.ido+" idto:"+info.idto+" rdn:"+info.rdn+" operation:"+info.operation);
//													if(/*operation==action.SET ||*/ info.operation==action.NEW){
//														if(lastCreationInFavourites!=null)
//															m_resultPanel.delRows(lastCreationInFavourites, resultPanel.PANEL_FAVORITOS);
//														
//														//selectData select=getRowFromDataBase(listIdo, listRdn);
//														selectData select=new selectData();
//														instance inst=m_kba.getTreeObjectTable(info.oldIdo, m_idto, m_idtoParent, m_userRol, info.idtoUserTask, sessionAux, true, structural, false);
//														inst.setRdn(info.rdn, true);
//														select.addInstance(inst);
//														
//														m_kba.setInstance(inst);
//														try{
//															m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
//														}finally{
//															m_kba.clearInstance();
//														}
//														m_resultPanel.setListFocus(resultPanel.PANEL_FAVORITOS);
//														m_resultPanel.selectRow(info.ido);
//														lastCreationInFavourites=select;
//														showInFavourites=false;
//														inst.setIDO(info.ido);
//														
//													}else if(info.operation==action.SET){
//														selectData select=null;
//														instance instanceFav=m_resultPanel.getDataRow(info.ido,resultPanel.PANEL_FAVORITOS);
//														if(instanceFav!=null){
//															//select=getRowFromDataBase(listIdo, listRdn);
//															select=new selectData();
//															instance inst=m_kba.getTreeObjectTable(info.ido, m_idto, m_idtoParent, m_userRol, info.idtoUserTask, sessionAux, true, structural, false);
//															inst.setRdn(info.rdn, true);
//															select.addInstance(inst);
//															//if(select.size()>0)
//																m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
//															/*else{
//																selectData selectFav=new selectData();
//																selectFav.addInstance(instanceFav);
//																m_resultPanel.delRows(selectFav, resultPanel.PANEL_FAVORITOS);
//															}*/
//														}
//														instance instanceResult=m_resultPanel.getDataRow(info.ido,resultPanel.PANEL_RESULTADOS);
//														if(instanceResult!=null){
//															if(select==null){
//																//select=getRowFromDataBase(listIdo, listRdn);
//																select=new selectData();
//																instance inst=m_kba.getTreeObjectTable(info.ido, m_idto, m_idtoParent, m_userRol, info.idtoUserTask, sessionAux, true, structural, false);
//																inst.setRdn(info.rdn, true);
//																select.addInstance(inst);
//															}
//															//if(select.size()>0)
//																m_resultPanel.addDataRows(select, resultPanel.PANEL_RESULTADOS);
//															/*else{
//																selectData selectRes=new selectData();
//																selectRes.addInstance(instanceResult);
//																m_resultPanel.delRows(selectRes, resultPanel.PANEL_RESULTADOS);
//															}*/
//														}
//														
//														/*selectData rows=m_resultPanel.getDataRows(resultPanel.PANEL_FAVORITOS);
//														Iterator<instance> itrFav=rows.getIterator();
//														while(itrFav.hasNext()){
//															instance instFav=itrFav.next();
//															if(instFav.getProperties().get(ido)!=null){
//																selectData select=getRowFromDataBase(idto, rdn, idtoUserTask);
//																m_resultPanel.addDataRows(select, resultPanel.PANEL_FAVORITOS);
//																m_resultPanel.setListFocus(resultPanel.PANEL_FAVORITOS);
//																m_resultPanel.selectRow(ido);
//															}
//														}*/
//													}
//												}finally{
//													m_resultPanel.getFormManager().m_session=sessionOldFormManager;
//													sessionAux.rollBack();
//												}
//											}else{
//												instance inst=new instance(info.idto,info.ido);
//												selectData select=new selectData();
//												select.addInstance(inst);
//												m_resultPanel.delRows(select, resultPanel.PANEL_FAVORITOS);
//												m_resultPanel.delRows(select, resultPanel.PANEL_RESULTADOS);
//											}
//										}
//									}
//									
//								}catch (Exception e) {
//									m_com.logError(dialog.getComponent(),e, "Error al añadir a favoritos el individuo tratado");
//									e.printStackTrace();
//								}finally{
//									mapPossibleFavouriteBySession.remove(session);
//									session.removeISessionStateListener(this);
//								}
//							}
//							
//						});
//					}
//				}
//			}
//			
//			
//		}catch (Exception e) {
//			m_com.logError(dialog.getComponent(),e, "Error al almacenar para favoritos el individuo tratado");
//			e.printStackTrace();
//		}
//	}
	
	private selectData getRowFromDataBase(ArrayList<Integer> ido,ArrayList<String> rdn) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException{
		//Hacemos una consulta usando la property rdn
		//Primero creamos el instance pero borramos todos sus valores por si tuvieramos algo fijado
		instance inst=m_instanceFavourites;//m_kba.getTreeObject(m_ido, m_userRol, m_idtoUserTask, m_session, true);
		/*Iterator<Property> itr=inst.getAllProperties().iterator();
		while(itr.hasNext()){
			Property p=itr.next();
			p.getValues().removeAll(p.getValues());
		}*/
		
				
//		//Hacemos una consulta usando la property rdn
//		//Primero creamos el instance pero borramos todos sus valores por si tuvieramos algo fijado
//		instance inst=new instance(m_idto,m_ido);
//		Iterator<ObjectProperty> itr=m_kba.getChildren(m_ido, m_userRol, idtoUserTask, m_session);
//		//m_kba.setInstance(m_instanceFilter);
		//inst.addProperty(m_ido, m_kba.getRDN(m_ido, m_userRol, idtoUserTask, m_session));
//		while(itr.hasNext()){
//			ObjectProperty oProp=itr.next();
//			oProp.getValues().removeAll(oProp.getValues());
//			inst.addProperty(oProp.getIdo(),oProp);
//			Iterator<Integer> itrRange=oProp.getRangoList().iterator();
//			while(itrRange.hasNext()){
//				int idRange=itrRange.next();
//				DataProperty propRDN=m_kba.getRDN(idRange, m_userRol, idtoUserTask, m_session);
//				propRDN.getValues().removeAll(propRDN.getValues());
//				inst.addProperty(propRDN.getIdo(), propRDN);
//			}
//		}
		//System.err.println("rdns:"+rdn+" idos:"+ido);
		m_kba.setInstance(inst);
		try{
			for(int i=0;i<rdn.size();i++)
				m_kba.setValue(m_ido, Constants.IdPROP_RDN, m_kba.buildValue(String.valueOf(rdn.get(i)),Constants.IDTO_STRING), null, m_userRol, m_idtoUserTask, m_session);
		}finally{
			m_kba.clearInstance();
		}
		try{
	//		if(m_listSelect==null)
	//			m_listSelect=buildListSelect(m_instanceFilter,m_idtoParent);
			//System.err.println("instanceee:"+inst);
			//System.err.println("select:"+m_listSelect);
			//System.err.println(jdomParser.returnXML(m_kba.getQueryXML(inst, m_listSelect, m_userRol, m_idtoUserTask)));
			selectData select = m_com.serverGetQuery(m_kba.getQueryXML(inst, m_listSelect, m_userRol, m_idtoUserTask,null), m_idtoUserTask, queryData.MODE_ROW);
			
			//if(m_idto!=idto){//Si se trata de un especializado tenemos que recorrer la lista de resultados ya que podrian venir varios registros con el mismo rdn pero de otro tipo de objeto
				ArrayList<Integer> listIdosDel=new ArrayList<Integer>();
				Iterator<instance> itrSelect=select.getIterator();
				while(itrSelect.hasNext()){
					instance instSelect=itrSelect.next();
					/*boolean found=false;
					for(int i=0;!found && i<ido.size();i++){
						System.err.println(instSelect.getIdTo()+" "+instSelect.getRdn());
						if(instSelect.getIdTo()==idto.get(i) && instSelect.getRdn().equals(rdn.get(i)))
							found=true;
					}
					if(!found)*/
					if(!ido.contains(new Integer(instSelect.getIDO())))
						listIdosDel.add(instSelect.getIDO());
				}
				Iterator<Integer> itrDel=listIdosDel.iterator();
				while(itrDel.hasNext()){
					int idoDel=itrDel.next();
					select.remove(idoDel);
				}
			//}
			
			return select;
		}finally{
			m_kba.setInstance(inst);
			try{
				for(int i=0;i<rdn.size();i++)
					m_kba.setValue(m_ido, Constants.IdPROP_RDN, null, m_kba.buildValue(String.valueOf(rdn.get(i)),Constants.IDTO_STRING), m_userRol, m_idtoUserTask, m_session);
			}finally{
				m_kba.clearInstance();
			}
		}
		
	}

	public void keyPressed(KeyEvent ke) {
//		try{
//			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
//				exeEvent(ke.getSource(),botoneraAccion.OPERATION_ACTION,m_idoParent,m_idPropParent,m_ido,m_idtoUserTask,botoneraAccion.EJECUTAR);
//		}catch(Exception ex){
//			//m_control.closeEvent();
//			m_com.logError(dialog.getComponent(),ex,"Error al intentar ejecutar la operación");
//			ex.printStackTrace();
//		}
	}

	public void keyReleased(KeyEvent ke) {
		try{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				exeEvent(ke.getSource(),botoneraAccion.OPERATION_ACTION,m_idoParent,m_idPropParent,m_ido,m_idtoUserTask,botoneraAccion.EJECUTAR);
		}catch(Exception ex){
			//m_control.closeEvent();
			m_com.logError(dialog.getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}
	}

	public void keyTyped(KeyEvent arg0) {}
	
	@Override
	public synchronized boolean getDictionary(String idTable, String idColumn, String root, boolean exactQuery, LinkedHashMap<String, DictionaryWord> words){
		//System.err.println("getDictionary idTable:"+idTable+" idColumn:"+idColumn+" root:"+root);
		boolean isAppliedLimit=true;
		try{
			DefaultSession session=m_kba.createDefaultSession(m_sessionInternal,m_idtoUserTask,false,false,false,true,false);
			try{
				/*IdObjectForm idObjectFormTable=new IdObjectForm(idTable);
				int idoTable=idObjectFormTable.getIdo();
				int idPropTable=idObjectFormTable.getIdProp();
				int valueClsTable=idObjectFormTable.getValueCls();*/
				
				IdObjectForm idObjectFormColumn=new IdObjectForm(idColumn);
				int idoColumn=idObjectFormColumn.getIdo();
				int idPropColumn=idObjectFormColumn.getIdProp();
				int valueClsColumn=idObjectFormColumn.getValueCls();
				
				int idoFilter=m_kba.createPrototype(idoColumn,Constants.LEVEL_FILTER, m_userRol, m_idtoUserTask, session);
				int idtoFilter=m_kba.getClass(idoFilter);
				
				Value oldValue=m_kba.getField(idoFilter, idtoFilter, idPropColumn, m_userRol, m_idtoUserTask, session).getUniqueValue();
				Value newValue=null;
				if(root!=null && !root.isEmpty()){
					String auxRoot=exactQuery?root:"%"+root+"%"/*Para que busque por contains*/;
					newValue=m_kba.buildValue(auxRoot,Constants.IDTO_STRING);
				}
				
				if(newValue!=null || oldValue!=null)
					m_kba.setValue(idoFilter, idPropColumn, newValue, oldValue, m_userRol, m_idtoUserTask, session);
				
				//Primero creamos el instance pero borramos todos sus valores por si tuvieramos algo fijado
				instance inst=m_kba.getTreeObject(idoFilter, m_userRol, m_idtoUserTask, session, true);
								
				//System.err.println("Prop:"+prop);
				
				/*if(idProp!=Constants.IdPROP_RDN){
					inst.addProperty(idoFilter, m_kba.getProperty(idoFilter, Constants.IdPROP_RDN, m_userRol, m_idtoUserTask, session));
				}*/
				
				//System.err.println("instance:"+inst);
				
				ArrayList<SelectQuery> listSelect=new ArrayList<SelectQuery>();
				
				SelectQuery selectQ=new SelectQuery(String.valueOf(idoFilter),idPropColumn,null,null);
				//select.setAlias(propNameHash.get(property));
				listSelect.add(selectQ);
				//System.err.println("instanceee:"+inst);
				//System.err.println("select:"+listSelect);
				//System.err.println(jdomParser.returnXML(m_kba.getQueryXML(inst, listSelect, m_userRol, m_idtoUserTask,false)));
				selectData select = m_com.serverGetQuery(m_kba.getQueryXML(inst, listSelect, m_userRol, m_idtoUserTask, GConfigView.limitFinderResults), m_idtoUserTask, queryData.MODE_ROW);
				
				isAppliedLimit=(select.size()==GConfigView.limitFinderResults);
				
				Iterator<instance> itr=select.getIterator();
				while(itr.hasNext()){
					instance instanceResult=itr.next();
					
					//Si viene algun resultado de una clase excluida lo descartamos
					if(m_kba.getClass(instanceResult.getIdTo())==null){
						System.err.println("getDictionary. Excluido ido:"+instanceResult.getIDO()+" idto:"+instanceResult.getIdTo());
						continue;
					}
					
					//System.err.println(instanceResult);
					Iterator<Property> itrProps=instanceResult.getAllProperties().iterator();
					DataProperty dProperty=null;
					while(itrProps.hasNext() && dProperty==null){
						Property prop=itrProps.next();
						if(prop.getIdProp().equals(idPropColumn))
							if(prop.getIdo()==instanceResult.getIDO())//Con esto evitamos coger una property distinta a la que nosotros hemos pedido ya que el server devuelve rdns de las properties aunque yo no las pida
								dProperty=(DataProperty)prop;//Sabemos que es dataProperty porque nunca vamos a hacer el finder para un objectProperty
					}
					
					if(dProperty!=null){
						String word=(String)m_kba.getValueData(dProperty);
						words.put(word, new DictionaryWord(dProperty.getIdo(),dProperty.getIdto(),word,false));
					}
					
				}
			}finally{
				session.rollBack();
			}
		}catch(Exception e){
			e.printStackTrace();
			m_com.logError(dialog.getComponent(),e,"Error al intentar consultar los datos de ayuda al usuario");
		}
		//System.err.println("final getDictionary idTable:"+idTable+" idColumn:"+idColumn+" root:"+root);
		return isAppliedLimit;
	}

	public Element getLastQuery() {
		return lastQuery;
	}
	
	/*private class InfoPossibleFavourite{

		int ido;
		int idto;
		String rdn;
		int oldIdo;
		int operation;
		Integer idtoUserTask;
		
		public InfoPossibleFavourite(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask) {
			super();
			this.ido = ido;
			this.idto = idto;
			this.rdn = rdn;
			this.oldIdo = oldIdo;
			this.operation = operation;
			this.idtoUserTask = idtoUserTask;
		}

		
	}
*/
}
