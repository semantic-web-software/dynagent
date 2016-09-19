package dynagent.gui.forms.utils;

import gdev.gawt.tableCellRenderer.ButtonCellRenderer;
import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GProcessedForm;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
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
import dynagent.common.exceptions.RuleEngineException;
import dynagent.common.exceptions.ServerException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.sessions.Session;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.forms.FormControl;
import dynagent.gui.forms.transitionControl;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;
import dynagent.ruleengine.src.sessions.DefaultSession;


class TableSelectManager implements MouseListener{
	resultPanel rp;
	int tipo;
	MouseListener mouseListener;
	
	public TableSelectManager( resultPanel rp, int tipo, MouseListener mouseListener){
		this.rp=rp;
		this.tipo=tipo;
		this.mouseListener=mouseListener;		
	}

	public void mouseClicked(MouseEvent e){
		try{
			rp.setListFocus(tipo);
			if(e.getClickCount()==2){
				//Si se trata de un boton no propagamos el doble click sobre la fila ya que ocurria al añadir/quitar favoritos
				if(!( ((JTable)e.getSource()).getCellRenderer(((JTable)e.getSource()).getSelectedRow(),((JTable)e.getSource()).getSelectedColumn()) instanceof ButtonCellRenderer) )
					mouseListener.mouseClicked(e);
			}
			//if( !rp.m_finalidadSeleccionarFavoritos ) return;
			/*if(rp.m_finalidadSeleccionarFavoritos){
				//System.out.println("MOUSE CLICK");
				if(!(( e.getModifiers() & InputEvent.CTRL_MASK )== InputEvent.CTRL_MASK ) &&
						!(( e.getModifiers() & InputEvent.SHIFT_MASK )== InputEvent.SHIFT_MASK ) &&
						tipo== resultPanel.PANEL_RESULTADOS )
					rp.sincronizaPaneles();
				if((( e.getModifiers() & InputEvent.CTRL_MASK )== InputEvent.CTRL_MASK ) &&
						!(( e.getModifiers() & InputEvent.SHIFT_MASK )== InputEvent.SHIFT_MASK ) &&
						tipo== resultPanel.PANEL_RESULTADOS )
					rp.addSelectedRowToFavoritos();
			}*/

			/*if(rp.m_soporteFavoritos){
				if(tipo==resultPanel.PANEL_RESULTADOS)
					rp.removeSeleccion(resultPanel.PANEL_FAVORITOS);
				else rp.removeSeleccion(resultPanel.PANEL_RESULTADOS);
			}*/			
		}catch(Exception ex){
			Singleton.getInstance().getComm().logError(rp.m_dialog.getComponent(),ex,"Error al seleccionar registro");
			ex.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent e){;}
	public void mouseExited(MouseEvent e){;}
	public void mousePressed(MouseEvent e){;}
	public void mouseReleased(MouseEvent e){;}
}

public class resultPanel implements ActionListener,ITableNavigation{
	communicator m_com;
	FormManager m_formManager;
	JComponent m_comp;
	
	boolean m_soporteFavoritos;
	boolean m_finalidadSeleccionarFavoritos;
	MouseListener mouseListener;
	
	int m_type;
	public static final int PANEL_RESULTADOS=1;
	public static final int PANEL_FAVORITOS=2;
	int currentListaFocus=PANEL_RESULTADOS;
	
	String id;
	
	KnowledgeBaseAdapter m_kba;
	
	private static KnowledgeBaseAdapter m_kbaDetails;//Motor exclusivo para cargar individuos que se muestra en el panel de detalle
	
	WindowComponent m_dialog;
	JPanel detailPanel;
	transitionControl detailForm;
	
	JPanel detailButtonsPanel;
	
	JButton prevButton;
	JButton nextButton;


	public resultPanel(Session sess,boolean soporteFavoritos,//Si es true tiene tabla favoritos
			boolean finalidadSeleccionarFavoritos,//Si es true al pinchar sobre una fila de resultPanel automaticamente se pone en favoritos
			boolean selectionMode,// Si es true solo se muestra la columna tipo en las tablas
			final Integer idtoUserTask,
			ObjectProperty property,
			int value,
			final Integer userRol,
			Dimension dim,
			MouseListener mouseListener,
			boolean filterMode,
			int widthMin,
			KnowledgeBaseAdapter kba, WindowComponent dialog, boolean allowedConfigTable) throws ParseException, AssignValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		
		m_soporteFavoritos=soporteFavoritos;
		m_finalidadSeleccionarFavoritos= finalidadSeleccionarFavoritos;
		this.mouseListener=mouseListener;
		m_kba=kba;
		m_dialog=dialog;
		IdObjectForm idObjectForm=new IdObjectForm();
		idObjectForm.setIdo(property.getIdto());
		idObjectForm.setIdProp(property.getIdProp());
		idObjectForm.setValueCls(kba.getClass(value));
		id=idObjectForm.getIdString();
		
		/*int divAncho=5;
		int border=7;
		if( m_soporteFavoritos ){
			int altoTotal= (int)dim.getHeight();
			int alto= 2*altoTotal/5;
			int nuevoAltoResult=altoTotal - alto - divAncho;
			dim= new Dimension( (int)dim.getWidth(), nuevoAltoResult>0?nuevoAltoResult:0 );//Evitamos que sea negativo ya que GViewBalancer no lo admite
			m_favoritos= buildFavoritosPanel(idtoUserTask,property,value,userRol,new Dimension( (int)dim.getWidth(),alto), selectionMode, sess, filterMode, widthMin);
		}*/
		
		IComponentListener controlValues=new IComponentListener(){
			public boolean addValueField(String id,Object value,int valueCls){
				return false;
			}

			public boolean removeValueField(String id,Object value,int valueCls){
				return false;
			}

			public boolean setValueField(String id,Object value,Object valueOld,int valueCls,int valueOldCls){
				return false;
			}

			public Integer newRowTable(String id,Integer idtoRow){
				return null;
			}

			public boolean removeRowTable(String id,int idoRow,int idtoRow) throws AssignValueException, NotValidValueException{
				Session session=m_kba.createDefaultSession(m_kba.getDDBBSession(),idtoUserTask,true, true, true, true, true);
				//WindowComponent dialog=Singleton.getInstance().getActionManager().getCurrentDialog();
				boolean success=false;
				try{
					m_dialog.disabledEvents();
					/*IdObjectForm idObjectForm=new IdObjectForm(id);
					Integer ido=idObjectForm.getIdo();
					Integer idProp=idObjectForm.getIdProp();
					Integer valueCls=idObjectForm.getValueCls();
					
					m_m_kba.setValue(ido, idProp, null, m_m_kba.getValueOfString(""+idoRow, valueCls), m_userRol, m_idtoUserTask, m_session);*/
					//m_kba.loadIndividual(idoRow, userRol, idtoUserTask, session);
					//int idtoRow=m_kba.getClass(idoRow);
					
//					Si existe ese mismo registro en favoritos lo borramos
//					instance inst=getDataRow(PANEL_FAVORITOS, idoRow);
//					if(inst!=null){
//						selectData select=new selectData();
//						select.addInstance(inst);
//						delRows(select, PANEL_FAVORITOS);
//					}
					
					m_kba.deleteObject(idoRow, idtoRow, null, userRol, idtoUserTask,session);
					
					session.commit();
					success=true;
					//Si existiera ese individuo tambien en favoritos lo quitamos de la tabla
					m_formManager.selectRow(id, idoRow);
					ArrayList<GIdRow> select=getDataSelectedRows();
					if(select!=null)
						delRows(select, PANEL_FAVORITOS);
				} catch (CardinalityExceedException ex) {
					NotValidValueException e;
					try {
						e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
						Property prop=ex.getProp();
						if (prop!=null){
							if (prop.getIdo()!=idoRow){
								e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask) + " de "+m_kba.getLabelClass(prop.getIdto(), idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), userRol, idtoUserTask, session))+"'");	
							}else{
								e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask));
							}
//							e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_m_kba.getLabelProperty(prop, prop.getIdto(), m_m_kba.getGroup(prop.getIdProp(),prop.getIdto(),m_idtoUserTask),m_idtoUserTask));
						}else{
							e.setUserMessage(((RuleEngineException)ex).getUserMessage());
						}
					} catch (Exception exec){
						AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
						e2.setStackTrace(exec.getStackTrace());
						throw e2;
					}
					throw e;
				} catch (IncompatibleValueException ex) {
					NotValidValueException e;
					try {
						e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
						e.setUserMessage(((RuleEngineException)ex).getUserMessage());
					} catch (Exception exec){
						AssignValueException e2=new AssignValueException(exec.getMessage());
						e2.setStackTrace(exec.getStackTrace());
						throw e2;
					}
					throw e;
				} catch (OperationNotPermitedException ex) {
					NotValidValueException e;
					try {
						e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
						e.setUserMessage(((RuleEngineException)ex).getUserMessage());
					} catch (Exception exec){
						AssignValueException e2=new AssignValueException(ex.getMessage()+" "+ex.getCause());
						e2.setStackTrace(exec.getStackTrace());
						throw e2;
					}
					throw e;
				}catch(ServerException ex){
					ex.printStackTrace();
					AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
					e.setUserMessage(ex.getUserMessage());
					e.setStackTrace(ex.getStackTrace());
					throw e;
				}catch(RuleEngineException ex){
					ex.printStackTrace();
					AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
					e.setUserMessage(ex.getUserMessage());
					e.setStackTrace(ex.getStackTrace());
					throw e;
				}catch(Exception ex){
					ex.printStackTrace();
					AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
					e.setStackTrace(ex.getStackTrace());
					throw e;
				}finally{
					if(!success){
						session.setForceParent(false);
						try{
							session.rollBack();
							System.err.println("WARNING:Sesion de resultPanel.removeRowTable cancelada");
						} catch (Exception e) {
							System.err.println("WARNING:Sesion de resultPanel.removeRowTable no ha podido cancelarse");
							e.printStackTrace();
						}
					}
					m_dialog.enabledEvents();
				}

				return true;
			}

			public void startEditionTable(String idTable,Integer idoRow,boolean pastingRow) {
				// TODO Auto-generated method stub
				
			}

			public void stopEditionTable(String idTable,Integer idoRow,HashSet<Integer> idosEdited,boolean pastingRow) {
				// TODO Auto-generated method stub
			}

			public void cancelEditionTable(String idTable, Integer idoRow) throws EditionTableException {
				// TODO Auto-generated method stub
				
			}

			public void editInForm(int idoParent,int idoToEdit) throws EditionTableException {
				// TODO Auto-generated method stub
				
			}

			public Integer newSubRowTable(String idParentColumn,Integer idtoRow) throws AssignValueException, NotValidValueException {
				return null;
			}

			public boolean isNewCreation(int ido) {
				// TODO Auto-generated method stub
				return false;
			}

			public LinkedHashMap<String, Integer> getPossibleTypeForValue(String idParent, Object value, Integer valueCls) {
				// TODO Auto-generated method stub
				return null;
			}

			public Boolean isNullableForRow(Integer idoParent, Integer ido, String idColumn) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void showInformation(Integer ido, Integer idProp) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setProcessingCopyRowTable(boolean processing) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isAllowedConfigTables() {
				// TODO Auto-generated method stub
				return true;
			}
		};
		
		m_formManager= buildResultPanel(controlValues,idtoUserTask,property,value,userRol, dim, selectionMode, sess, filterMode, widthMin, dialog, allowedConfigTable);
				
		/*if( m_soporteFavoritos){
			JPanel panelFavoritos=new JPanel(new BorderLayout());
			JPanel panelResultados=new JPanel(new BorderLayout());
			panelFavoritos.setBorder(BorderFactory.createEmptyBorder());
			panelResultados.setBorder(BorderFactory.createEmptyBorder());
			
					
			JTable table_result = (JTable)getTable(m_formManager.getComponent(), null);
			JTable table_favoritos = (JTable)getTable(m_favoritos.getComponent(), null);
			
			JScrollPane scroll_result = new JScrollPane(table_result);
			scroll_result.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
			JScrollPane scroll_favoritos = new JScrollPane(table_favoritos);
			scroll_favoritos.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
			
			scroll_result.setPreferredSize(m_formManager.getComponent().getPreferredSize());
			scroll_favoritos.setPreferredSize(m_favoritos.getComponent().getPreferredSize());
			
			panelResultados.add(scroll_result, BorderLayout.CENTER);			
			panelFavoritos.add(scroll_favoritos, BorderLayout.CENTER);
			panelResultados.setBorder(BorderFactory.createEmptyBorder(0, border, border, border));
			panelFavoritos.setBorder(BorderFactory.createEmptyBorder(0, border, border, border));
						
			JSplitPane split= new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelFavoritos, panelResultados);
			split.setDividerSize(divAncho);
			split.setBorder(BorderFactory.createEmptyBorder());
			
			m_comp= split;			
		}else{*/
			JPanel panel_result = new JPanel(new BorderLayout());
			final JTable table_result = (JTable)getTable(m_formManager.getComponent(), null);
			JScrollPane scroll_result = new JScrollPane(table_result);
			scroll_result.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
			scroll_result.setPreferredSize(m_formManager.getComponent().getPreferredSize());
			panel_result.add(scroll_result, BorderLayout.CENTER);
			final JTabbedPane tabbedPane=new JTabbedPane(/*JTabbedPane.BOTTOM*/);
			tabbedPane.setName("TabbedPaneResult");
			tabbedPane.setBorder(BorderFactory.createEmptyBorder());
			tabbedPane.addTab(Utils.normalizeLabel("LISTA"),panel_result);
			
			panel_result.addComponentListener(new ComponentListener() {
				
				@Override
				public void componentShown(ComponentEvent e) {
					m_dialog.disabledEvents();
					try{
						removeDetailForm();
					}catch(Exception ex){
						ex.printStackTrace();
						Singleton.getInstance().getComm().logError(m_dialog.getComponent(),ex,"Error al deshacer la vista de registro");
					}finally{
						m_dialog.enabledEvents();
					}
				}
				
				@Override
				public void componentResized(ComponentEvent e) {}
				
				@Override
				public void componentMoved(ComponentEvent e) {}
				
				@Override
				public void componentHidden(ComponentEvent e) {}
			});

			m_comp=tabbedPane;
			
			if(Singleton.getInstance().isMultiWindow()){
				detailPanel=new JPanel(new BorderLayout(0,0));
				detailPanel.addComponentListener(new ComponentListener() {
					
					@Override
					public void componentShown(ComponentEvent e) {
						m_dialog.disabledEvents();
						try{
							ArrayList<GIdRow> selection=m_formManager.getIdRowsSelectionData(id);
							if(!selection.isEmpty()){
								GIdRow idRow=selection.get(0);
								buildDetailForm(idRow.getIdo(), idRow.getIdto(), userRol, idtoUserTask);
							}else{
								if(m_formManager.getTable(id).getRowCount()>0){
									RowItem rowItem=m_formManager.getTable(id).getModel().getRowItemFromIndex(0);
									m_formManager.selectRow(id, rowItem.getIdRow().getIdo());
									buildDetailForm(rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto(), userRol, idtoUserTask);
								}
							}
						}catch(Exception ex){
							ex.printStackTrace();
							Singleton.getInstance().getComm().logError(m_dialog.getComponent(),ex,"Error al intentar mostrar el registro de la tabla");
						}finally{
							m_dialog.enabledEvents();
						}
					}
					
					@Override
					public void componentResized(ComponentEvent e) {}
					
					@Override
					public void componentMoved(ComponentEvent e) {}
					
					@Override
					public void componentHidden(ComponentEvent e) {}
				});
				tabbedPane.addTab(Utils.normalizeLabel("DETALLE"),detailPanel);
				
				GConst.addShortCut(null, tabbedPane, GConst.RESULT_SWITCH_SHORTCUT_KEY, GConst.RESULT_SWITCH_SHORTCUT_MODIFIERS, "Conmutar_Resultados", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){
	
					private static final long serialVersionUID = 1L;
					
					public void actionPerformed(ActionEvent arg0) {
						//System.err.println("ENTRA en shortcut de conmutacion de resultados/detalle");
						if(table_result.isShowing()){
							((JTabbedPane)tabbedPane).setSelectedIndex(1);
						}else if(detailPanel.isShowing()){
							((JTabbedPane)tabbedPane).setSelectedIndex(0);
						}
					}
					
				});
				
				
				tabbedPane.addChangeListener(new ChangeListener() {
					//Nos sirve para quitar detailButtonsPanel cada vez que desde detalle volvamos a la tabla de resultados. De esta manera evitamos que
					//al volver a entrar en detalle con otro registro se vean los botones anterior-siguiente de la vez anterior durante unos segundos antes de refrescarse
					@Override
					public void stateChanged(ChangeEvent e) {
						if(detailButtonsPanel!=null && detailButtonsPanel.isShowing()){
							//System.err.println("Remove detailButtonsPanel");
							detailPanel.remove(detailButtonsPanel);
							detailButtonsPanel=null;
						}
					}
				});
			}
		/*}*/
	}
	
	private JComponent getTable(JComponent p, JComponent res){
		for(int i=0;i<p.getComponentCount();i++){
			JComponent comp = (JComponent)p.getComponent(i);
			if(comp instanceof JTable){
				res = comp;
				break;
			}else
				res = getTable(comp, res);
		}
		return res;
	}

	public void removeSeleccion(){
		m_formManager.removeSeleccion(id);
	}

	public void setListFocus( int tipo ){
		currentListaFocus=tipo;
		/*if(m_soporteFavoritos){
			removeSeleccion();
		}*/
	}
	
	public int getListFocus(){
		return currentListaFocus;
	}

	public JComponent getComponent(){
		return m_comp;
	}

	public FormManager buildResultPanel(IComponentListener controlValue,Integer idtoUserTask,ObjectProperty property,int value,Integer userRol,Dimension dim,boolean selectionMode,Session sess, boolean filterMode, int widthMin, WindowComponent dialog, boolean allowedConfigTable) throws ParseException, AssignValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		String idTable=id;
		GProcessedForm viewForm=formFactory.buildResultsTable(m_kba,idtoUserTask,property,value,userRol,new access(access.VIEW),dim,/*true,*/idTable,selectionMode,m_soporteFavoritos,sess, filterMode, widthMin, allowedConfigTable);
		FormManager formManager=null;
		ArrayList<GProcessedForm> listaViewForm=new ArrayList<GProcessedForm>();
		listaViewForm.add(viewForm);

		formManager= new FormManager(
				
				controlValue,
				null,
				this,
				0,
				"RESULT",
				listaViewForm,
				false,
				filterMode,
				false,
				false,
				false,
				null, ConstantesGraficas.dimInit, m_kba, dialog,
				idtoUserTask,userRol,sess);

		//formManager.addTableFocusListener( idTable, new TableFocusManager( this, PANEL_RESULTADOS ) );
		if(mouseListener!=null)
			formManager.addTableMouseListener( idTable, new TableSelectManager( this, PANEL_RESULTADOS ,mouseListener) );	  
		return formManager;

	}

	private FormManager buildFavoritosPanel(Integer idtoUserTask,ObjectProperty property,int value,Integer userRol, Dimension dim, boolean selectionMode,Session sess, boolean filterMode, int widthMin, WindowComponent dialog, boolean allowedConfigTable) throws ParseException, AssignValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		String idTable=id;
		GProcessedForm viewForm=formFactory.buildFavoritosTable(m_kba,idtoUserTask,property,value,userRol,new access(0),dim,idTable,selectionMode,m_soporteFavoritos,sess, filterMode, widthMin, allowedConfigTable);
		if(viewForm==null)
			System.err.println("ES NULO EL VIEWFORM");

		FormManager formManager=null;
		ArrayList<GProcessedForm> listaViewForm=new ArrayList<GProcessedForm>();
		listaViewForm.add(viewForm);

		formManager= new FormManager(
				
				null,
				null,
				this,
				0,
				"RESULT",
				listaViewForm,
				false,
				filterMode,
				false,
				false,
				false,
				null, ConstantesGraficas.dimScreenJDialog, m_kba, dialog,
				idtoUserTask,userRol,sess);

		//formManager.addTableFocusListener( idTable, new TableFocusManager( this, PANEL_FAVORITOS ) );
		if(mouseListener!=null)
			formManager.addTableMouseListener( idTable, new TableSelectManager( this, PANEL_FAVORITOS, mouseListener ) );

		return formManager;
	}

	public void actionPerformed( ActionEvent ae ){
		try{
			JButton button = (JButton) ae.getSource();
			String command = button.getActionCommand();
			String[] buf = command.split(":");
			int tipoBoton = Integer.parseInt(buf[3]);
			if (buf[0].equals("ACTION") && tipoBoton == botoneraAccion.ROWADD) {

				if(m_soporteFavoritos){
					int row = Integer.parseInt(buf[5]);

					// deleteRow( m_result, ido );
					removeSeleccion();
					addRowToFavoritos(row);
				}
			}

			if (buf[0].equals("ACTION") && tipoBoton == botoneraAccion.ROWDEL) {
				int row = Integer.parseInt(buf[5]);
				deleteRow(row);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			Singleton.getInstance().getComm().logError(m_dialog.getComponent(),ex,"Error al gestionar favoritos");
		}
	}

	public void addSelectedRowToFavoritos() throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		if( !m_soporteFavoritos ) return;
		int[] rowsR= m_formManager.getIndexSelectedRows(id);
		for( int i=0; i<rowsR.length; i++)
			addRowToFavoritos( rowsR[i] );
	}

	private void addRowToFavoritos( int row ) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		GIdRow idRow=m_formManager.getDataTableFromIndex(id,row);
		if(m_formManager.getPermanentDataTableFromIdo(id,idRow.getIdo())==null)
			m_formManager.showAsPermanentRows(id,row);
		selectRow(idRow.getIdo(),PANEL_FAVORITOS);
	}

	public void addDataRows( Object datos, int tipo ) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		if(tipo==PANEL_FAVORITOS)
			m_formManager.addPermanentRows(id, datos, true);
		else m_formManager.addRows(id, datos, true);
	}

	public void setRows(Object table, int tipo) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		if(tipo==PANEL_FAVORITOS)
			m_formManager.setPermanentRows(id, table);
		else m_formManager.setRows(id, table);
	}
	
	public void buildDetailForm(int ido,int idto,final Integer userRol,final Integer idtoUserTask) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, AssignValueException, SQLException, NamingException, JDOMException{

		if(m_kbaDetails==null){//Si no existe lo creamos como un clone para no afectar al motor de búsquedas, que tambien es usado como base para el resto de clones
			m_kbaDetails=Singleton.getInstance().buildKnowledgeBaseAdapterClone();
			Singleton.getInstance().setKnowledgeBaseAdapter(m_kbaDetails.getKnowledgeBase(), m_kbaDetails);
		}
		
		DefaultSession session=m_kbaDetails.createDefaultSession(m_kbaDetails.getDefaultSessionWithoutRules(),idtoUserTask,true,false,false,true,false);
		boolean success=false;
		try{
			removeDetailForm();
			
			if(detailButtonsPanel==null){
				detailButtonsPanel=new JPanel();
	
				ActionListener actionListener=new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						m_dialog.disabledEvents();
						try{
							String command = e.getActionCommand();
							Integer buttonType = Integer.valueOf(command);
							try{
								if (buttonType == botoneraAccion.NEXT) {
									nextRow();
								}else{
									prevRow();
								}
								
								ArrayList<GIdRow> selection=m_formManager.getIdRowsSelectionData(id);
								if(!selection.isEmpty()){
									GIdRow idRow=selection.get(0);
									buildDetailForm(idRow.getIdo(), idRow.getIdto(), userRol, idtoUserTask);
								}
							}catch(Exception ex){
								ex.printStackTrace();
								Singleton.getInstance().getComm().logError(m_dialog.getComponent(),ex,"Error al cambiar de registro");
							}
						}finally{
							m_dialog.enabledEvents();
						}
					}
				};			
				
				int buttonHeight=(int)botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics());
				prevButton=botoneraAccion.subBuildBoton(detailButtonsPanel,null,"prev",""+botoneraAccion.PREV,Utils.normalizeLabel("IR AL ANTERIOR"),actionListener, buttonHeight, buttonHeight, false, m_kba.getServer());
				prevButton.setEnabled(false);
				nextButton=botoneraAccion.subBuildBoton(detailButtonsPanel,null,"next",""+botoneraAccion.NEXT,Utils.normalizeLabel("IR AL SIGUIENTE"),actionListener, buttonHeight, buttonHeight, false, m_kba.getServer());
				nextButton.setEnabled(false);
				detailPanel.add(detailButtonsPanel,BorderLayout.NORTH);
				
				GConst.addShortCut(m_comp, prevButton, GConst.DETAIL_PREV_KEY, GConst.DETAIL_PREV_SHORTCUT_MODIFIERS, "Previo detalle", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
				GConst.addShortCut(m_comp, nextButton, GConst.DETAIL_NEXT_KEY, GConst.DETAIL_NEXT_SHORTCUT_MODIFIERS, "Siguiente detalle", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
				
				if(hasPrevRow() || hasNextRow()){
					//Pedimos el foco para que funcionen los botones
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							detailPanel.requestFocusInWindow();
						}
					});
				}
			}
			
			if(hasPrevRow()){
				prevButton.setEnabled(true);
			}else{
				if(prevButton.hasFocus()){//Si tuviera el foco se lo damos al otro boton ya que este va a estar enabled. Con esto nos aseguramos tener el foco para las shortCut
					nextButton.requestFocusInWindow();
				}
				prevButton.setEnabled(false);
			}

			if(hasNextRow()){
				nextButton.setEnabled(true);
			}else{
				if(nextButton.hasFocus()){//Si tuviera el foco se lo damos al otro boton ya que este va a estar enabled. Con esto nos aseguramos tener el foco para las shortCut
					prevButton.requestFocusInWindow();
				}
				nextButton.setEnabled(false);
			}
			
//			if(prevButton.isEnabled()){
//				if(!nextButton.isEnabled()){
//					prevButton.requestFocusInWindow();
//				}else if(!prevButton.hasFocus() && !nextButton.hasFocus()){
//					nextButton.requestFocusInWindow();
//				}
//			}else if(nextButton.isEnabled()){
//				nextButton.requestFocusInWindow();
//			}
			
			Dimension panelDim=m_comp.getPreferredSize();
			Dimension buttonsDim=detailButtonsPanel.getPreferredSize();
			
			m_kbaDetails.loadIndividual(ido, idto, userRol, idtoUserTask, session);
			Dimension dim=new Dimension((int)panelDim.getWidth(),(int)(panelDim.getHeight()-buttonsDim.getHeight()-ConstantesGraficas.getSizeTabTabbedPane()));//new Dimension(ConstantesGraficas.dimZonaTrabajo.width-GConfigView.widthScrollBar,ConstantesGraficas.dimZonaTrabajo.height);
			detailForm=new transitionControl(
					session,
					userRol,
					null,
					ido,
					idto,
					/*command,*/idtoUserTask,
					access.VIEW,
					dim,
					new JPanel()/*botonera.getComponent()*/, m_kbaDetails, m_dialog, null, null, false, true, null);
			
			JPanel form=detailForm.getComponent();
			
			detailPanel.add(form,BorderLayout.CENTER);
			detailPanel.revalidate();
			detailPanel.repaint();
			detailButtonsPanel.revalidate();
			detailButtonsPanel.repaint();
			success=true;
		}finally{
			if(!success){
				session.setForceParent(false);
				session.rollBack();
			}
		}
	}

	/*public void sincronizaPaneles() throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		if( !m_soporteFavoritos ) return;
		int[] rowsR= m_formManager.getIndexSelectedRows(id);
		selectData rowsF= m_favoritos.getInstanceData(id);
		if( rowsR.length==0 && rowsF.size()==0 ) return;
		if( rowsF.size()==0 && rowsR.length==1 )
			addRowToFavoritos( rowsR[0] );

		if( rowsR.length==0 && rowsF.size()==1 ){
			deleteRow( 0 );
		}

		if( rowsR.length==1 && rowsF.size()==1 ){
			deleteRow( 0 );
			addRowToFavoritos( rowsR[0] );
		}
	}*/
	
	public void deleteAllRows(int tabla) throws AssignValueException{
		if(tabla==PANEL_FAVORITOS){
			m_formManager.deleteAllPermanentRows(id);
		}else if(tabla==PANEL_RESULTADOS){
			m_formManager.deleteAllRows(id);
		}
	}

	public void removeDetailForm() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException {
		if(detailForm!=null){
			detailForm.getSession().rollBack();
			detailPanel.remove(detailForm.getComponent());
			//detailPanel.removeAll();
			detailForm=null;
			//Ejecutamos el recolector de basura para que no se acumule memoria ocupada
			//Utils.forceGarbageCollector();
		}
	}

	private void deleteRow(int row ) throws AssignValueException{
		m_formManager.delRow(id,row);
	}

	public void  addTableSelectionListener(ListSelectionListener lis){
		m_formManager.addTableSelectionListener(id, lis);
	}

	public FormManager getFormManager(){
		return m_formManager;
	}

	public GIdRow nextRow(){
		GIdRow idRow=null;
		RowItem rowItem=m_formManager.getNextRow(id);
		if(rowItem!=null){
			idRow=rowItem.getIdRow();
			if(idRow!=null){
				boolean permanent=rowItem.isPermanent();
				selectRow(idRow.getIdo(),permanent?PANEL_FAVORITOS:PANEL_RESULTADOS);
			}
		}
		return idRow;
	}

	public GIdRow prevRow(){
		GIdRow idRow=null;
		RowItem rowItem=m_formManager.getPrevRow(id);
		if(rowItem!=null){
			idRow=rowItem.getIdRow();
			if(idRow!=null){
				boolean permanent=rowItem.isPermanent();
				selectRow(idRow.getIdo(),permanent?PANEL_FAVORITOS:PANEL_RESULTADOS);
			}
		}
		return idRow;
	}
	
	public boolean hasNextRow() {
		return m_formManager.getNextRow(id)!=null;
	}

	public boolean hasPrevRow() {
		return m_formManager.getPrevRow(id)!=null;
	}

	public void delRow(GIdRow idRow, int tipo) throws AssignValueException{
		ArrayList<GIdRow> list=new ArrayList<GIdRow>();
		list.add(idRow);
		delRows(list, tipo);
	}
	
	public void delRows(ArrayList<GIdRow> table, int tipo) throws AssignValueException{
		if(tipo==PANEL_FAVORITOS){
			m_formManager.delPermanentRows(id,table);
		}else if(tipo==PANEL_RESULTADOS){
			m_formManager.delRows(id,table);
		}
	}
	
	public void delFocusedRows(ArrayList<GIdRow> table) throws AssignValueException{
		delRows( table,getListFocus() );
	}
	
	public void selectRow( int ido ){
		selectRow(ido,getListFocus());
	}
	
	public void selectRow( int ido, int tipo ){
		if(tipo==PANEL_FAVORITOS){
			m_formManager.selectPermanentRow( id, ido );
		}else if(tipo==PANEL_RESULTADOS){
			m_formManager.selectRow( id, ido );
		}
	}
	
	public void selectAllRows(boolean select){
		m_formManager.selectAllRows(id,select);
	}
	
	public boolean isSelectAllRows(){
		return m_formManager.isSelectAllRows(id);
	}

	public ArrayList<GIdRow> getDataSelectedRows(){
		return m_formManager.getIdRowsSelectionData(id);
	}

	public ArrayList<GIdRow> getData(int tipo){
		ArrayList<GIdRow> select=null;
		if(tipo==PANEL_FAVORITOS){
			select=m_formManager.getIdRowsData(getId(),true);
		}else if(tipo==PANEL_RESULTADOS){
			select=m_formManager.getIdRowsData( getId(), false );
		}
		
		return select;
	}
	
	public GIdRow getDataRow(int idRow,int tipo){
		GIdRow data=null;
		if(tipo==PANEL_FAVORITOS){
			data=m_formManager.getPermanentDataTableFromIdo( id, idRow );
		}else if(tipo==PANEL_RESULTADOS){
			data=m_formManager.getDataTableFromIdo( id, idRow );
		}
		
		return data;
	}
	
	public boolean hasRow(int idRow){
		if(m_formManager.getPermanentDataTableFromIdo( id, idRow )!=null)
			return true;
		if(m_formManager.getDataTableFromIdo( id, idRow )!=null)
			return true;
		
		return false;
	}

	/*public void orderRows(int column,int tipo){
		getList(tipo).orderRows(id, column);
	}*/

	public void setMouseListener(MouseListener mouseListener) {
		this.mouseListener = mouseListener;
		String idTable=id;
		/*if( m_soporteFavoritos )
			m_favoritos.addTableMouseListener(idTable, new TableSelectManager(this, PANEL_FAVORITOS, mouseListener));
		*/m_formManager.addTableMouseListener(idTable, new TableSelectManager(this, PANEL_RESULTADOS, mouseListener));
	}

	public String getId() {
		return id;
	}

	public JPanel getDetailPanel() {
		return detailPanel;
	}
	
}
