package dynagent.gui.tasks;

import gdev.gbalancer.GViewBalancer;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import dynagent.common.communication.communicator;
import dynagent.common.communication.contextAction;
import dynagent.common.communication.flowAction;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.instance;
import dynagent.common.sessions.Session;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;
import calendar.PanelCalendar;
import tasks.ITaskCenter;
import tasks.ITaskListener;


public class monitor extends JPanel implements ItemListener,/*historyListener,*/ /*processServer,*/ITaskListener,taskActionListener,ActionListener,IHistoryDDBBListener,INoticeListener{

	private static final long serialVersionUID = 1L;
	private int anchoRadios= 150;
	private int alto=ConstantesGraficas.intToolY, anchoContent;
	private final static int HISTORY=0;
	private final static int TASK=1;
	//private final static int STATE=2;

	private final static String contentMonitorID_prefix="MON_";
	private communicator m_com;
	private ArrayList<content> panelsContent;
	private ArrayList<JScrollPane> panelsView;
	private ArrayList<JRadioButton> radButtons;

	private ButtonGroup group = new ButtonGroup();
	private JButton m_view=null;
	private String viewAction="VIEWACTION";
	private JButton buttonExeTask;
	private String exeAction="EXEACTION";

	private ITaskCenter m_taskCenter;
	private PanelCalendar m_calendar;
	private WindowComponent m_dialog;

	private KnowledgeBaseAdapter kba;
	
	private JLabel noticeMessageLabel;
	private JPanel noticeMessagePanel;


	public monitor(int initialSelection,ITaskCenter taskCenter,KnowledgeBaseAdapter kba,WindowComponent dialog,int ancho_monitor,boolean hideHistoryDDBB) {
		
		anchoContent=ancho_monitor;//-anchoRadios;TODO Descomentar cuando haya tareas desde el servidor
		m_taskCenter=taskCenter;
		m_com=kba.getServer();
		panelsContent=new ArrayList<content>();
		panelsView=new ArrayList<JScrollPane>();
		radButtons=new ArrayList<JRadioButton>();
		
		m_dialog=dialog;
		this.kba=kba;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS ) );
		setBorder( new EmptyBorder(0,0,0,0) );
		setPreferredSize( new Dimension(anchoContent/*+anchoRadios TODO Descomentar cuando haya tareas del servidor*/,alto-3/*Bordes*/) );

		//Font labelFont=new Font("Dialog",  Font.PLAIN,  11);

		JRadioButton hist = buildRadio(group, "Historial", /*labelFont,*/ alto/3);
		JRadioButton task = buildRadio(group, "Pendiente", /*labelFont,*/ alto/3);
		JRadioButton st = buildRadio(group, "Estado", /*labelFont,*/ alto/3);

		JPanel mon= new JPanel();
//		mon.setBackground(UIManager.getColor("ToolBar.background"));
//		hist.setBackground(UIManager.getColor("ToolBar.background"));
//		task.setBackground(UIManager.getColor("ToolBar.background"));
//		st.setBackground(UIManager.getColor("ToolBar.background"));
//		setBackground(UIManager.getColor("ToolBar.background"));
		GridLayout box= new GridLayout(3,1);
		mon.setLayout(box);
		mon.add( hist );
		mon.add( task );
		mon.add( st );
		mon.setMaximumSize( new Dimension(60,alto));
		/*mon.setBackground( Color.RED );*/
		buildPanels();

		add( (JScrollPane)panelsView.get(initialSelection) );
		//TODO Descomentar cuando haya tareas desde el servidor
		//add( mon );
		group.setSelected( ((JRadioButton)radButtons.get( initialSelection )).getModel(), true );
		JPanel botonera= new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
//		botonera.setBackground(UIManager.getColor("ToolBar.background"));
		//box= new GridLayout(/*2*/1,1);
		//botonera.setLayout(box);

		add(Box.createRigidArea(new Dimension(0,alto)));
		m_view= new JButton();
		m_view.setActionCommand(viewAction);
		m_view.addActionListener(this);
		ImageIcon img=m_com.getIcon("calendarTasks");
		m_view.setIcon(img);
		m_view.setPreferredSize(new Dimension(ConstantesGraficas.intToolY-3,ConstantesGraficas.intToolY-3));

		buttonExeTask=new JButton();
		buttonExeTask.setActionCommand(exeAction);
		buttonExeTask.addActionListener(this);
		img=m_com.getIcon("runTask");
		buttonExeTask.setIcon(img);
		buttonExeTask.setPreferredSize(new Dimension(ConstantesGraficas.intToolY-3,ConstantesGraficas.intToolY-3));
		
		JPanel panelButtons=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
	    //panelButtons.add(buttonExeTask);//TODO Descomentar cuando haya tareas desde el servidor
		//panelButtons.add(m_view);//TODO Descomentar cuando haya tareas desde el servidor
		botonera.add( panelButtons );
		//botonera.add(buttonExeTask);
		//botonera.add(m_view);

		/*m_targetsFilter= new threadActionMenu(com, m_md);*/
		/*		m_menu= new threadActionMenu(m_com, m_md);
		botonera.add( m_menu );
		 */		//botonera.setMaximumSize( new Dimension(/*60*/alto,alto));
		 add(botonera);
		 add(Box.createHorizontalGlue());
		 if(!hideHistoryDDBB)
			 kba.addHistoryDDBBListener(this);
		 
		 createNoticeMessageComponent();
		 kba.addNoticeListener(this);
		 //addHistory(-323, "Cliente", "pepe", action.NEW);
	}

	/*  public boolean isLive(){return true;}
	 */
	/*private void setPanel( int panel ){
	setContent( panel );
	group.setSelected( ((JRadioButton)radButtons.get( panel )).getModel(), true );
  }*/

	private void setContent( int panel ){
		remove(0);
		add( (JScrollPane)panelsView.get(panel),0 );
		validate();
		repaint();
	}

	/* public void cancelProcessEvent(  int proType, int currPro ){
      content ct=(content)panelsContent.get(HISTORY);
      if( ct!=null )
	  ct.delProcessInstance(currPro);
      ct=(content)panelsContent.get(TASK);
      if( ct!=null )
  	  ct.delProcessInstance(currPro);
  }
	 */
	/*  public void contextActionEvent( contextAction pp, ArrayList listaCtxFix )
		throws DataErrorException,RemoteSystemException,CommunicationException{
	    System.out.println("MONITOR contextActionEvent "+pp.toString());
	    ArrayList msg = buildNewIndexMsg(pp);
	    if( msg!=null ){
                addRow(HISTORY, null, msg);
                setPanel(HISTORY);
            }else
		System.out.println("MSG NULO");
  }
	 */
	public ArrayList buildNewIndexMsg( contextAction pp ){
		/*     if( pp.getResultCode()==dynagent.communication.message.NEW_INDEX ){
	      Element to= m_md.getMetaTO(new Integer(pp.getTO_ctx()));
	      Element rdn= jdomParser.findElementByAt( to.getChild("ATRIBUTOS"),
						      "ATRIBUTO",
						      "TA_POS",
						      String.valueOf(helperConstant.TAPOS_RDN),
						      true );
	      //pp.setContext(m_ctx.id);

	      int idto= pp.getTO_ctx();
	      int ido= pp.getIDO_ctx();
	      //stRdn= pp.getRdn_ctx();
	      ArrayList msg= new ArrayList();

	      taskType taskDef= m_md.getTask( new Integer(pp.getTaskType()) );
	      String taskName=pp.getTaskType()==0 ? to.getAttributeValue("TO_NAME"):
					      taskDef.label;

	      String state=pp.getTaskType()==0 ? "NUEVO":m_md.getEnumLabel(new Integer(taskDef.taposAtState),
								      pp.getCurrTaskState());


	      long vDate=pp.getExeDate()==0 ? System.currentTimeMillis():pp.getExeDate();

	      msg.add( taskName );
	      msg.add( state );
	      msg.add( rdn.getAttributeValue("AT_NAME")+" "+pp.getRdn_ctx() );
	      msg.add( DateFormat.getDateInstance().format(new java.util.Date(vDate)) );

	      //Object[] source= new Object[1];
	      //source[0]=this;
	      return msg;
	  }else
	      return null;
		 */
		return null;}

	/*  public void freeOwningTaskEvent(  int currTask ){
      content ct=(content)panelsContent.get(HISTORY);
      if( ct!=null )
	  ct.delTask(currTask);
      ct=(content)panelsContent.get(TASK);
      if( ct!=null )
  	  ct.delTask(currTask);
  }
	 */
	public void taskSelection( Object target, flowAction pt ){
		if( /*panel==TASK &&*/ pt!=null ){
			/*		try{*/
			Object[] source= new Object[2];
			source[0]=panelsContent.get(HISTORY);
			source[1]=panelsContent.get(TASK);
			/*		m_menu.addFact( source, null, pt );*/

			if( target instanceof content ){
				content cnt= (content)target;
				if( cnt.getID().indexOf(contentMonitorID_prefix)==-1 ){
					content thisList= (content)panelsContent.get(TASK);
					if( thisList.hasTask(pt.getCurrTask()) )
						thisList.selectTask(pt.getCurrTask());
				}
			}

			/*		}catch(SystemException se){
		    m_com.logError(se);
		    return;
		}catch(DataErrorException se){
		    m_com.logError(se);
		    return;
		}
			 */	}
	}

	private void buildPanels(){
		for( int i=0;i<3; i++ ){
			int numColumns=4;
			/*if(i==HISTORY)
	    	numColumns=3;*/
			content cnt= new content( contentMonitorID_prefix+i, numColumns, this );
			//System.out.println("monitor.buildPanels:"+cnt.hashCode());
			panelsContent.add( cnt );
			panelsView.add( new JScrollPane(cnt) );
			cnt.setPreferredScrollableViewportSize(new Dimension(anchoContent, alto));
			cnt.setRowHeight((int)GViewBalancer.getRowHeightS(Singleton.getInstance().getGraphics()));
		}
	}

	/*public void addFact( Object[] source, ArrayList fact, flowAction pp ) throws SystemException,DataErrorException{
	//int index= m_menuMap.getMenuIndex( new Integer( pp.getContext() ));
	addRow( HISTORY, pp, fact );
	setPanel( HISTORY );
  }*/

	public void addRow( int panel, /*flowAction pt,*/instance inst, ArrayList<String> data ){
		content cnt=(content)panelsContent.get( panel );
		cnt.addRow( inst,/*pt,*/ data );
	}

	public void addRow( int panel, int idoUserTask, ArrayList<String> data ){
		content cnt=(content)panelsContent.get( panel );
		cnt.addRow( idoUserTask, data );
	}

	public void itemStateChanged(ItemEvent e){
		try{
			if( e.getStateChange()==ItemEvent.SELECTED ){
				JRadioButton rad= (JRadioButton)e.getItem();
				int index= radButtons.indexOf( rad );
				setContent( index );
			}
		}catch(Exception ex){
			m_com.logError(m_dialog.getComponent(),ex,"Error al cambiar la vista del monitor de tareas");
			ex.printStackTrace();
		}
	}

	protected JRadioButton buildRadio( ButtonGroup group, String label, /*Font labelFont,*/ int alto ){
		JRadioButton rad= new JRadioButton(label);
		rad.addItemListener( this );
		radButtons.add( rad );
		rad.setMargin(new Insets(0,0,0,0));
		rad.setMaximumSize( new Dimension(anchoRadios,alto) );
		rad.setFocusPainted( false );
		/*rad.setFont( labelFont );*/
		group.add(rad);
		return rad;
	}

	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = monitor.class.getResource(path);
		if(imgURL!=null)
			return new ImageIcon(imgURL);
		else return new ImageIcon();
	}

	protected JComponent makeTextPanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
	}


	/*   public owningAction findCurrentTask( int taskType, int currTask ){return null; }

   public boolean supportIndex( Integer index ){return true;}

   public void owningTaskEvent( owningAction pp, ArrayList listaCtxFix, boolean b )
	throws DataErrorException{
    int comp=0;

	if( pp.getOwningLevel()==owningAction.OWNING_RELEASED ){
	    taskTransitionEnd(pp);
	}else{
	    if(pp.getExeDate()>0){
                try {
                    comp = dateUtil.compareToday(pp.getExeDate());
                } catch (ParseException pe) {
                    m_com.logError(m_dialog.getComponent(),pe);
                    throw new DataErrorException(pe.toString());
                }
                if (comp != dateUtil.AFTER_TODAY) {
                    Integer process = new Integer(pp.getProcessType());
                    processType pType = m_md.getProcess(process);
                    //pp.addPropetie(properties.GUI_id,index);
                    ArrayList msg = dynagent.gui.tasks.content.buildTaskMsg(/*m_md,*//* pp);
                    addRow(TASK, pp, msg);
                    setPanel(TASK);
                }
            }
        }
   }

   public void taskTransitionEnd( flowAction pp ){
	content cnt=(content)panelsContent.get( TASK );
	cnt.delTask( pp );
   }

   public void newProcessEvent( flowAction pp, ArrayList listaCtxFix ) throws DataErrorException{;}
                     */

	public void actionPerformed(ActionEvent e) {
		try{
			m_dialog.disabledEvents();
			if( e.getActionCommand().equals(viewAction) ){

				//if(Singleton.getInstance().getStatusBar().getNivelLocalizacion()>0)
				//	Singleton.getInstance().getStatusBar().upNivelLocalizacion();
				Singleton.getInstance().getStatusBar().setLocalizacion(Utils.normalizeLabel("Calendario de tareas"),0);
				
				// Hacemos que se ejecute en un nuevo hilo para mantener libre el hilo AWT-EventQueue
				final monitor thisMon=this;
				SwingWorker worker=new SwingWorker(){
					public Object construct(){
						doWorkEvent(thisMon);
						return null;
					}
				};
				worker.start();
			}else if(e.getActionCommand().equals(exeAction)){
				/*int size=panelsContent.size();
	   		for(int i=0;i<size;i++){
	   			panelsContent.get(0);
	   		}*/
				content cont=panelsContent.get(TASK);
				int row=cont.getSelectedRow();
				if(row!=-1){
					//KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBase();
					//instance inst=((contentModel)cont.getModel()).getTaskData(row);
					int idoUserTask=((contentModel)cont.getModel()).getIdoUserTask(row);//inst.getIDO();
					m_taskCenter.exeTask(idoUserTask);
				}
			}
			
		}catch(Exception ex){
			m_com.logError(m_dialog.getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}finally{
			m_dialog.enabledEvents();
		}
	}

	private synchronized void doWorkEvent(monitor mon){
		try{
			//Singleton.getInstance().frameWorkEvent(mon, Singleton.getInstance().TASK_MANAGER_AREA, 0);
			//GestorInterfaz gestorInterfaz=Singleton.getInstance().getGestorInterfaz();
			//gestorInterfaz.removeZona(GestorInterfaz.ZONA_MENU);
			//gestorInterfaz.removeZona(GestorInterfaz.ZONA_TRABAJO);
			GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_TRABAJO);
			if(!gestor.setVisiblePanels(/*"Conjunto:calendar"*/"calendar", true)){
				//System.out.println(gestor+" "+tg);
				if(m_calendar==null)
					m_calendar=new PanelCalendar(m_taskCenter, ConstantesGraficas.dimZonaTrabajoExtended);
				JScrollPane sp = new JScrollPane(m_calendar);
				sp.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
				sp.setBorder(BorderFactory.createEmptyBorder());
				gestor.addPanel("calendar", sp, 1, m_calendar.getPreferredSize(), /*"Conjunto:calendar"*/"calendar");
				gestor.setVisiblePanels(/*"Conjunto:calendar"*/"calendar", true);
			}
			GestorContenedor gestorMenu=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
			gestorMenu.setVisibleItems(null, false);

			Singleton.getInstance().getNavigation().setActualPage(/*"Conjunto:calendar"*/"calendar");
			
			//m_dialog.getContentPane().add(m_calendar, BorderLayout.CENTER);
			m_dialog.getComponent().validate();
			m_dialog.getComponent().repaint();
		}catch(Exception ex){
			m_com.logError(m_dialog.getComponent(),ex,"Error al ejecutar la operación");
			ex.printStackTrace();
		}
	}

	/*    public void taskFiltering(long ini, long end) {
    }
	 */
//	public void updateTasks(selectData tasks) {
//	Iterator<instance> itr=tasks.getIterator();
//	while(itr.hasNext()){
//	instance inst=itr.next();
//	ArrayList msg = dynagent.gui.tasks.content.buildTaskMsg(inst);
//	if(!hasTask(inst.getIDO(),TASK))
//	addRow(TASK, inst, msg);
//	}

//	}

	public boolean hasTask(int currTask,int panel){
		content cnt=(content)panelsContent.get( panel );
		return ((contentModel)cnt.getModel()).hasTask(currTask);
	}

	public void updateTasks(int idoUserTask, String labelUserTask, String status, String asignDate, String ejecutionDate) {
		ArrayList<String> msg = dynagent.gui.tasks.content.buildTaskMsg(labelUserTask,status,asignDate,ejecutionDate);
		if(!hasTask(idoUserTask,TASK))
			addRow(TASK,idoUserTask,msg);
	}

	public void initChangeHistory(){}
	
	public void endChangeHistory(){}
	
	//TODO Hacerlo mas eficiente acumulando todo y añadiendolo en endChangeHistory
	public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
		//System.err.println("ido:"+ido+" idto:"+idto+" operation:"+operation+" idtoUserTask:"+idtoUserTask);
		ArrayList<String> msg;
		try {
			KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBaseAdapter(sessionUsed.getKnowledgeBase());
			boolean show=!kba.getIdtoUserTasks(idto,null,false,true,false).isEmpty();
			/*if(!show){
				Iterator<Integer> itr=Singleton.getInstance().getKnowledgeBase().getAncestors(idto);
				while(!show && itr.hasNext()){
					int idtoParent=itr.next();
					show=!Singleton.getInstance().getKnowledgeBase().getIdtoUserTasks(idtoParent).isEmpty();
				}
			}*/
			
			if(show){
				msg = dynagent.gui.tasks.content.buildHistoryMsg(kba.getLabelClass(idto,idtoUserTask),rdn,operation);
				addRow(HISTORY,ido,msg);
				if(noticeMessagePanel.isShowing()){
					setContent(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			m_com.logError(m_dialog.getComponent(),e, "Error al registrar el historial de acciones");
		}
	}

	private void createNoticeMessageComponent(){
		noticeMessagePanel=new JPanel();
		noticeMessagePanel.setPreferredSize(new Dimension(anchoContent-3, alto-3));
		noticeMessageLabel=new JLabel();
		noticeMessageLabel.setFont(new Font(noticeMessageLabel.getFont().getName(), Font.BOLD, (int)(noticeMessageLabel.getFont().getSize()*1.9)));
		noticeMessagePanel.add(noticeMessageLabel);
	}
	
	@Override
	public void setText(String text) {
		try{
			System.err.println("getComponentCount:"+getComponentCount());
			noticeMessageLabel.setText(text);
			if(!noticeMessagePanel.isShowing()){
				remove(0);
				add(noticeMessagePanel,0);
			}
			
			noticeMessageLabel.validate();
			noticeMessageLabel.repaint();
			noticeMessagePanel.validate();
			noticeMessagePanel.repaint();
			validate();
			repaint();
		}catch(Exception ex){
			ex.printStackTrace();
			System.err.println("Error mostrando el mensaje en la barra de tareas");
		}
	}

}
