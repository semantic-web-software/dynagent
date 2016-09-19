package gdev.gawt;

import gdev.gawt.tableCellEditor.ButtonCellEditor;
import gdev.gawt.tableCellEditor.CellEditor;
import gdev.gawt.tableCellEditor.CheckCellEditor;
import gdev.gawt.tableCellEditor.CheckTristateCellEditor;
import gdev.gawt.tableCellEditor.ComboBoxEditor;
import gdev.gawt.tableCellEditor.DateCellEditor;
import gdev.gawt.tableCellEditor.GroupButtonCellEditor;
import gdev.gawt.tableCellEditor.FileCellEditor;
import gdev.gawt.tableCellEditor.NumberCellEditor;
import gdev.gawt.tableCellEditor.TextCellEditor;
import gdev.gawt.tableCellRenderer.ButtonCellRenderer;
import gdev.gawt.tableCellRenderer.CheckCellRenderer;
import gdev.gawt.tableCellRenderer.CheckTristateCellRenderer;
import gdev.gawt.tableCellRenderer.ComboBoxRenderer;
import gdev.gawt.tableCellRenderer.ComboBoxRendererLabel;
import gdev.gawt.tableCellRenderer.DateCellRenderer;
import gdev.gawt.tableCellRenderer.GroupButtonCellRenderer;
import gdev.gawt.tableCellRenderer.TextCellRenderer;
import gdev.gawt.tableHeaderRenderer.CheckHeaderRenderer;
import gdev.gawt.tableHeaderRenderer.MultiLineHeaderRenderer;
import gdev.gawt.utils.ItemList;
import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentListener;
import gdev.gen.IDictionaryFinder;
import gdev.gfld.GFormTable;
import gdev.gfld.GTableColumn;
import gdev.gfld.GTableRow;
import gdev.gfld.GValue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.docServer;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.Utils;

/**
 * Esta clase extiende a GComponent y creará una tabla. Una vez creada se podrá
 * representar en la interfaz gráfica.
 * 
 * @author Juan
 * @author Francisco
 */
public class GTable extends GComponent implements ActionListener, MouseListener {
	//final Component c=this;
	private static final long serialVersionUID = 1L;
	private JTable m_objTable;
	protected GTableModel m_objTableModel;	
	private ArrayList<Integer> m_idProps = new ArrayList<Integer>();
	private HashMap<String, Object> m_moa;
	final static int virtualTypeForAgregation = 0;
	private Object m_control;
	boolean hideHeader = false;
	private Icon iconoAdd, iconoDel, iconoCheck, iconoNotCheck;
	private String m_id;
	private docServer m_server;
	private int m_action;
	private Vector m_listaColumnas;
	private Vector<GTableRow> m_listaFilas;
	private boolean m_cuantitativo;
	private int m_iniVirtColumn;
	private int m_atGroupColum;
	private ArrayList m_totalColumns;
	private ArrayList m_agrupables;
	private boolean m_modoConsulta;
	private String m_label;
	private String m_name;
	private boolean m_topLabel;	

	//private Vector<TableModel> m_formComponents;
	private boolean m_popup;
	private double m_heightRow;
	private JPopupMenu m_menuBotonera;
	boolean m_nullable;
	boolean m_modoFilter;	
	public static final String BUTTON_ONE_FILE = "buttonTableOneFile";
	private boolean m_creationRow;
	private boolean m_finderRow;
	private boolean m_nullRow;
	private IDictionaryFinder m_dictionaryFinder;
	public Window window;
	private FocusListener focusListener;
	private AccessAdapter operations;
	private Set focusListenerExt;
	private IUserMessageListener m_messageListener;
	
	private boolean manageFocus;
	
	private boolean focusTraversalManagingError=false;
	
	private boolean processingPasteRows=false;
	private boolean abortPasteRows=false;
	private boolean processingSelectCellFromSystem;
	
	public GTable(
			GFormTable ff,/* session ses, */
			docServer server,/* fieldControl control, */
			IComponentListener controlValue,
			IUserMessageListener messageListener,
			IDictionaryFinder dictionaryFinder,
			Object control,/* scope myScope, */
			int action,/* docDataModel rootDocModel, */
			/*Vector<TableModel> formComponents,*/
			Font fuente, boolean modoConsulta, boolean popup, boolean modoFilter, Window window) {
		super(ff,controlValue);
		m_messageListener=messageListener;

		// //////////OBTENCION DE ATRIBUTOS//////////////////
		String id = ff.getId();
		String label = ff.getLabel();
		String name = ff.getName();
		boolean topLabel = ff.isTopLabel();
		// int sintax=ff.getType();
		boolean cuantitativo = ff.isCuantitativo();
		int iniVirtColumn = ff.getIniVirtualColumn();
		int atGroupColum = ff.getAtGroupColumn();
		boolean nullable = ff.isNullable();
		double height = ff.getRowHeight();
		ArrayList<Integer> totalColumns = null;
		ArrayList<Integer> agrupables = null;
		m_listaFilas = ff.getRowList();
		m_listaColumnas = ff.getColumnList();
		Iterator iteratorColumnas = m_listaColumnas.iterator();
		while (iteratorColumnas.hasNext()) {
			GTableColumn columna = (GTableColumn) iteratorColumnas.next();
			if (columna.isAgrupable())
				agrupables.add(columna.getColumn());
			if (columna.isTotal())
				totalColumns.add(columna.getColumn());
		}
		boolean hideHeader = ff.isHideHeader();

		operations=ff.getOperations();
		m_creationRow=ff.hasCreationRow();
		m_finderRow=ff.hasFinderRow();
		m_nullRow=ff.hasCreationRow()||ff.hasFinderRow();
		m_dictionaryFinder=dictionaryFinder;
		this.hideHeader = hideHeader;
		m_server = server;
		m_control = control;
		m_moa = new HashMap<String, Object>();// moa;
		m_id = id;
		m_action = action;
		m_cuantitativo = cuantitativo;
		m_iniVirtColumn = iniVirtColumn;
		m_atGroupColum = atGroupColum;
		m_totalColumns = totalColumns;
		m_agrupables = agrupables;
		m_modoConsulta = modoConsulta;
		m_label = label;
		m_name = name;
		m_topLabel = topLabel;
		m_popup = popup;
		m_heightRow = height;
		m_nullable = nullable;
		m_modoFilter = modoFilter;
		this.window=window;
		focusListenerExt=new HashSet<FocusListener>();
		
		manageFocus=m_label!=null;//Si tiene label manejamos el foco, eliminando la seleccion cuando lo pierde, ya que se trataria de un formulario con mas campos
	}

	protected void createComponent() throws AssignValueException {
		m_objTable = new JTable(){

//			@Override
//			public void changeSelection(int rowIndex,int columnIndex,boolean toggle,boolean extend) {
//				super.changeSelection(rowIndex, 0, toggle, extend);
//			}

			@Override
			protected void processKeyEvent(KeyEvent ev) {
				//Con esto evitamos que entre en los cellEditor estando el foco sobre la tabla en vez de sobre el cellEditor. Si entra en textCellEditor tendriamos problemas con el finder
				//Ademas permitimos los eventos de f2 ya que sirve para entrar en edicion en la fila
				if(ev.getKeyCode()==KeyEvent.VK_DOWN || ev.getKeyCode()==KeyEvent.VK_UP || 
						ev.getKeyCode()==KeyEvent.VK_RIGHT || ev.getKeyCode()==KeyEvent.VK_LEFT ||
						ev.getKeyCode()==KeyEvent.VK_TAB || ev.getKeyCode()==KeyEvent.VK_ALT || ev.getModifiersEx()==KeyEvent.CTRL_DOWN_MASK || ev.getKeyCode()==KeyEvent.VK_CONTROL || ev.getKeyCode()==KeyEvent.VK_ENTER || ev.getKeyCode()==KeyEvent.VK_DELETE ||
						/*ev.getKeyCode()==KeyEvent.VK_ESCAPE ||*/ ev.getKeyCode()==KeyEvent.VK_F2/*Se utiliza para entrar en edicion en la celda*/)
				//if(!Character.isLetterOrDigit(ev.getKeyChar()))
					super.processKeyEvent(ev);
				else if(ev.getKeyCode()==KeyEvent.VK_ESCAPE){
					if(this.getCellEditor()==null && GTable.this.getModel().isEditing()){//Si no estamos dentro de un campo pero se esta editando esa fila cancelamos los cambios sobre ella
						if(ev.getID()==KeyEvent.KEY_PRESSED){//Solo nos interesa que entre una vez por lo que ponemos PRESSED
							try {
								m_componentListener.cancelEditionTable(m_id, GTable.this.getModel().getIdoRowEditing());//Cancelamos la edición
								GTable.this.getModel().setIdoRowEditing(null);
								m_objTable.setRowSelectionInterval(this.getRowCount()-1,this.getRowCount()-1);
							} catch (EditionTableException e) {
								e.printStackTrace();
								m_server.logError(window, e, "No se ha podido cancelar la edicion de la fila");
							} catch (AssignValueException e) {
								e.printStackTrace();
								m_server.logError(window, e, "No se ha podido cancelar la edicion de la fila");
							}
						}
					}else super.processKeyEvent(ev);
				}
			}
//
//			@Override
//			protected boolean processKeyBinding(KeyStroke arg0, KeyEvent arg1, int arg2, boolean arg3) {
//				// TODO Auto-generated method stub
//				return super.processKeyBinding(arg0, arg1, arg2, arg3);
//			}
//			
			

		};
		
		m_objTable.setName(m_name);
		m_objTable.getTableHeader().setName("Header."+m_name);
		if(!m_modoFilter)//No dejamos que se puedan reordenar las columnas por el usuario cuando no esta en filterMode ya que da problemas al asignar valores a las columnas por el usuario
			m_objTable.getTableHeader().setReorderingAllowed(false);
		
//		m_objTable.addKeyListener(new KeyListener(){
//		public void keyPressed(KeyEvent ev){
//			if(ev.getKeyCode()!=KeyEvent.VK_TAB){
//				if(/*m_objTable.editCellAt(m_objTable.getSelectedRow(), m_objTable.getSelectedColumn())*/m_objTable.getSelectedRow()!=-1 && m_objTable.getSelectedColumn()!=-1){
//					TableColumn tc = m_objTable.getColumnModel().getColumn(m_objTable.getSelectedColumn());
//					if(tc.getCellEditor() instanceof TextCellEditor){
//						//boolean edit=m_objTable.isEditing();
//						//if(!edit)//
//						//	edit=m_objTable.editCellAt(m_objTable.getSelectedRow(), m_objTable.getSelectedColumn());
//						
//						//if(edit){
//							//TextCellEditor tce = (TextCellEditor)tc.getCellEditor();
//							//if(tce.shouldAddKeyListener()){
//								m_objTable.getEditorComponent().requestFocusInWindow();
//								//tce.keyPressed(ev);
//							//}
//						//}
//					}
//				}
//			}
//		}
//		public void keyReleased(KeyEvent ev){
////			if(ev.getKeyCode()!=KeyEvent.VK_TAB){
////				if(/*m_objTable.editCellAt(m_objTable.getSelectedRow(), m_objTable.getSelectedColumn())*/m_objTable.getSelectedRow()!=-1 && m_objTable.getSelectedColumn()!=-1){
////					TableColumn tc = m_objTable.getColumnModel().getColumn(m_objTable.getSelectedColumn());
////					if(tc.getCellEditor() instanceof TextCellEditor){
////						TextCellEditor tce = (TextCellEditor)tc.getCellEditor();
////						if(tce.shouldAddKeyListener()){
////							//m_objTable.getEditorComponent().requestFocusInWindow();
////							//tce.keyReleased(ev);
////						}
////					}
////				}
////			}
//		}
//		public void keyTyped(KeyEvent ev){}
//	});
		
		m_objTable.setBorder(BorderFactory.createEmptyBorder());

		//if(!m_modoConsulta){
//			if(window!=null){
//				final Window d = window;
//				if(d!=null){
//					d.addWindowFocusListener(new WindowAdapter(){
//						public void windowGainedFocus(WindowEvent ev) {
//							//System.err.println("*****windowGainedFocus");
//							//System.err.println("Tiene el foco:"+d.getFocusOwner());
//							if(/*!m_objTable.hasFocus() && */m_objTable.getSelectedRow()!=-1){
//								//m_objTable.requestFocusInWindow();
////								final Runnable doFinished1 = new Runnable() {
////									public void run() { m_objTable.requestFocusInWindow(); }
////								};
////								final Runnable doFinished = new Runnable() {
////									public void run() { SwingUtilities.invokeLater(doFinished1); }
////								};
////								SwingUtilities.invokeLater(doFinished);
//							}
//						}
//						public void windowLostFocus(WindowEvent arg0) {}						
//					});
//				}
//			}

			GConst.addShortCut(null, m_objTable, GConst.INFO_SHORTCUT_KEY, GConst.INFO_SHORTCUT_MODIFIERS, "Info tabla", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){
	
				private static final long serialVersionUID = 1L;
				
				public void actionPerformed(ActionEvent arg0) {
					//System.err.println("Row:"+m_objTable.getSelectedRow());
					//System.err.println("Column:"+m_objTable.getSelectedColumn());
					RowItem rowItem=m_objTableModel.getRowItemFromIndex(m_objTable.getSelectedRow());
					Integer ido=rowItem.getColumnIdo(m_objTable.getSelectedColumn());
					IdObjectForm idObjForm=new IdObjectForm(m_objTableModel.getFieldIDFromColumn(m_objTable.getSelectedColumn()));
					int idProp=idObjForm.getIdProp();
					m_componentListener.showInformation(ido, idProp);
				}
				
			});
			
			final GTable thisFinal = this;
			
			focusListener = new FocusListener(){
				public void focusGained(FocusEvent ev){
					//System.err.println("FocusGained Table "+m_label+" component:"+(ev.getComponent()!=null?ev.getComponent().getClass()+" "+ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass()+" "+ev.getOppositeComponent().hashCode():null)+" isTemporary:"+ev.isTemporary());

					// m_objTable.setFocusTraversalPolicyProvider(false);
//					if(m_objTable.getRowCount()>0 && ev.getOppositeComponent()!=null && ev.getOppositeComponent().getParent()!=null/*Esto ocurrira cuando lo gane desde un cellEditor*/ )
//						m_objTable.setFocusTraversalKeysEnabled(false);
						//m_objTable.setFocusCycleRoot(true);//Si tiene filas queremos que tenga su propio cambio de foco, gestionado por los actionPerformed
					notifyFocusListener(ev, false);
					if(!ev.isTemporary() && m_objTable.getRowCount()>0 /*&& !isTopLabel()*/ && m_objTable.getSelectedRowCount()==0){
						//System.err.println("Entra en seleccion");
						int row=0;
						int column=0;
						if(/*ev.getComponent() instanceof JButton || */m_modoFilter || m_modoConsulta || ev.getSource() instanceof JButton/*Para el boton de acciones de una sola fila*/){
							if(getRowCount()>0)//getRowCount descarta las nullRow
								m_objTable.setRowSelectionInterval(row, row);
						}else{
							editFirstCellEditable(row,column,true,false);
						}
						
					}
					
					
					//if(m_objTable.isEditing())
					//	m_objTable.getEditorComponent().requestFocusInWindow();
					
					//if(!thisFinal.isAncestorOf(ev.getOppositeComponent())/* && (old!=null && old.equals(window))*/){
					if(manageFocus){
						if(!m_modoConsulta && !m_modoFilter){
							if(m_objComponent instanceof JScrollPane)
								((JScrollPane)m_objComponent).setBorder(GConfigView.borderSelected);
							else m_objComponent.setBorder(GConfigView.borderSelected);
						}
					}

					/*if(m_controlListener!=null && !isEditing){
						m_controlListener.startEditionTable(getId());
						isEditing=true;
					}*/
						
				}
				public void focusLost(FocusEvent ev){
					//System.err.println("FocusLost Table "+m_label+" component:"+(ev.getComponent()!=null?ev.getComponent().getClass()+" "+ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass()+" "+ev.getOppositeComponent().hashCode():null)+" isTemporary:"+ev.isTemporary());
					if(/*!ev.isTemporary() &&*/ ev.getOppositeComponent()!=null && ev.getSource()!=null){
						//System.err.println("FocusLost gain "+ev.getOppositeComponent().getClass()+" lost "+ev.getComponent().getClass());
						//m_objTableModel.focusLost(ev);
						notifyFocusListener(ev, true);
						
						Window old = SwingUtilities.getWindowAncestor(ev.getOppositeComponent());
						if(manageFocus && !thisFinal.isAncestorOf(ev.getOppositeComponent()) && thisFinal!=ev.getOppositeComponent()/* && (old!=null && old.equals(window))*/){
							 if(old!=null && old.equals(window) && !ev.isTemporary()){
								//System.err.println("clearSelection "+m_objTable.hashCode());
								m_objTable.removeEditor();
								m_objTable.clearSelection();
							 }
							if(!m_modoConsulta && !m_modoFilter){
								if(m_objComponent instanceof JScrollPane)
									((JScrollPane)m_objComponent).setBorder(UIManager.getBorder("ScrollPane.border"));
								else m_objComponent.setBorder(UIManager.getBorder("Table.border"));
							}
//							if(m_controlListener!=null && rowEditing!=-1){
//								if(!m_controlListener.stopEditionTable(getId(),getDataFromIndex(rowEditing).getIDO()))
//									m_objTable.requestFocusInWindow();
//								else rowEditing=-1;
//							}
						}/*else if(!isTopLabel() && thisFinal.isAncestorOf(ev.getOppositeComponent()) && (old!=null && old.equals(window)) && ev.getOppositeComponent() instanceof JButton){	
							System.err.println("clearSelection2");
							m_objTable.clearSelection();
//							if(m_controlListener!=null && rowEditing!=-1){
//								if(!m_controlListener.stopEditionTable(getId(),getDataFromIndex(rowEditing).getIDO()))
//									m_objTable.requestFocusInWindow();
//								else rowEditing=-1;
//							}
						}*/else{
							//System.err.println("Request "+ev.getOppositeComponent().getClass());
//							if(m_controlListener!=null){
//								if(idoRowEditing!=null){
//									Component componentFocus=ev.getOppositeComponent();//window.getFocusOwner();
//									System.err.println("Tiene el focus:"+(componentFocus!=null?componentFocus.getClass():null));
//									if(!m_objTable.isAncestorOf(componentFocus)){//Si el foco ya no esta en la tabla
//										System.err.println("El foco ya no esta en la tabla");
//										Integer idoRow=null;
//										if(!isNullRowEditing)
//											idoRow=idoRowEditing;
//										
//										if(!m_controlListener.stopEditionTable(getId(),idoRow))
//											//listS.setSelectionInterval(idoRowEditing, idoRowEditing);
//											//listS.setSelectionInterval(listS.getLeadSelectionIndex(), listS.getLeadSelectionIndex());
//											m_objTable.setRowSelectionInterval(0, 0);
//										else idoRowEditing=null;
//										
//										isNullRowEditing=false;
//									}else{
//										System.err.println("El foco sigue en la tabla");
//									}
//								}else{
//									int rowToEdit=m_objTable.getSelectedRow();
//									if(rowToEdit!=-1){
//										int idoRowToEdit=getDataFromIndex(rowToEdit).getIDO();
//										isNullRowEditing=isNullRow(rowToEdit);
//										Integer idoRow=idoRowToEdit;
//										m_controlListener.startEditionTable(getId(),idoRow);
//										idoRowEditing=rowToEdit;
//									}
//								}
//							}
						
							//ev.getOppositeComponent().requestFocusInWindow();
						}
					}
					
				}
			};
			m_objTable.addFocusListener(focusListener);
		//}

		m_objComponent = new JScrollPane(m_objTable);
		
		if(!m_modoFilter){
			//Si no esta en modo filtro removemos los listener de la rueda del raton para que este aplique sobre el formulario entero en vez de solo sobre la tabla
			//TODO Hacer que aplique sobre la tabla hasta que este en la ultima fila posible y luego aplique sobre el formulario
			MouseWheelListener[] listMouseWheel=m_objComponent.getMouseWheelListeners();
			for(int i=0;i<listMouseWheel.length;i++)
				m_objComponent.removeMouseWheelListener(listMouseWheel[i]);
		}
		m_objComponentSec = null;

		m_objTableModel = new GTableModel(m_id, (GFormTable)this.m_objFormField, m_componentListener, m_action,
				m_listaColumnas, m_listaFilas, m_cuantitativo,
				m_iniVirtColumn, m_atGroupColum, m_totalColumns,
				m_agrupables, m_modoConsulta, m_modoFilter, m_creationRow, m_finderRow, m_topLabel);
		
		m_objTable.setModel(m_objTableModel);
		m_objTableModel.setDirectEdition(m_modoFilter);
//		m_objTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
//
//			public void valueChanged(ListSelectionEvent e) {
//				//System.err.println("ListSelectionEvent");
//				ListSelectionModel listS=(ListSelectionModel)e.getSource();
//				if(!listS.isSelectionEmpty()){
//					//System.err.println("Indice Seleccionado:"+listS.getMinSelectionIndex());
//					if(listS.getMinSelectionIndex()==listS.getMaxSelectionIndex()){//Seleccionada una sola fila
//						System.err.println("- Seleccionada una sola fila");
//						int idoRowToEdit=getDataFromIndex(listS.getMinSelectionIndex()).getIDO();
//						if(idoRowEditing==null || (idoRowEditing!=null && idoRowEditing!=idoRowToEdit)){//Fila seleccionada no es la misma que la que ya estamos editando
//							System.err.println("Fila seleccionada no es la misma que la que ya estamos editando");
//							int rowToEdit=listS.getMinSelectionIndex();
//							if(m_controlListener!=null){
//								if(idoRowEditing!=null){//Estabamos editando otra fila 
//									System.err.println("Estabamos editando otra fila ");
//									if(!isNullRowEditing){//Fila en edicion no era la nullRow
//										System.err.println("Fila en edicion no era la nullRow");
//										if(!m_controlListener.stopEditionTable(getId(),idoRowEditing)){
//											int ido=m_objTableModel.findRow(idoRowEditing,false);
//											//listS.setSelectionInterval(idoRowEditing, idoRowEditing);
//											//listS.setSelectionInterval(listS.getLeadSelectionIndex(), listS.getLeadSelectionIndex());
//											listS.setSelectionInterval(ido, ido);
//										}else{//Fila en edicion era la nullRow
//											//if(!isNullRow(rowToEdit)){//Fila para editar no es nullRow
//												m_controlListener.startEditionTable(getId(),idoRowToEdit);
//												idoRowEditing=idoRowToEdit;
//											//}else idoRowEditing=null;//Fila para editar es la nullRow
//										}
//									}else{//Fila en edicion era nullRow. Entonces no tenemos que volver a iniciar edicion, seguimos en la misma
//										System.err.println("Fila en edicion era nullRow. Entonces no tenemos que volver a iniciar edicion, seguimos en la misma");
//										idoRowEditing=idoRowToEdit;
//										isNullRowEditing=false;
//									}
//									//isNullRowEditing=false;
//								}else{//No estabamos editando ninguna fila
//									System.err.println("No estabamos editando ninguna fila");
//									/*Integer idoRow=null;
//									if(isNullRow(rowToEdit))
//										isNullRowEditing=true;
//									else{
//										idoRow=getDataFromIndex(rowToEdit).getIDO();
//										isNullRowEditing=false;
//									}*/
//									isNullRowEditing=isNullRow(rowToEdit);
//									Integer idoRow=idoRowToEdit;
//									m_controlListener.startEditionTable(getId(),idoRow);
//									idoRowEditing=rowToEdit;
//								}
//							}
//						}
//					}else{//Seleccionadas varias filas
//						System.err.println("- Seleccionadas varias filas");
//						if(m_controlListener!=null){
//							if(idoRowEditing!=null){//Estabamos editando una fila
//								System.err.println("Estabamos editando una fila");
//								Integer idoRow=null;
//								if(!isNullRowEditing)
//									idoRow=idoRowEditing;
//								
//								if(!m_controlListener.stopEditionTable(getId(),idoRow)){
//									int ido=m_objTableModel.findRow(idoRowEditing,false);
//									//listS.setSelectionInterval(idoRowEditing, idoRowEditing);
//									//listS.setSelectionInterval(listS.getLeadSelectionIndex(), listS.getLeadSelectionIndex());
//									listS.setSelectionInterval(ido, ido);
//								}else idoRowEditing=null;
//								
//								isNullRowEditing=false;
//							}
//						}
//					}
//						
//				}else{//Ninguna fila seleccionada
//					System.err.println("- Ninguna fila seleccionada");
//					if(m_controlListener!=null && idoRowEditing!=null){
//						Component componentFocus=window.getFocusOwner();
//						System.err.println("Tiene el focus:"+(componentFocus!=null?componentFocus.getClass():null));
//						if(!thisFinal.isAncestorOf(componentFocus)){//Si el foco ya no esta en la tabla
//							System.err.println("El foco ya no esta en la tabla");
//							Integer idoRow=null;
//							if(!isNullRowEditing)
//								idoRow=idoRowEditing;
//							
//							if(!m_controlListener.stopEditionTable(getId(),idoRow)){
//								int ido=m_objTableModel.findRow(idoRowEditing,false);
//								//listS.setSelectionInterval(idoRowEditing, idoRowEditing);
//								//listS.setSelectionInterval(listS.getLeadSelectionIndex(), listS.getLeadSelectionIndex());
//								listS.setSelectionInterval(ido, ido);
//							}else idoRowEditing=null;
//							
//							isNullRowEditing=false;
//						}else{
//							System.err.println("El foco sigue en la tabla");
//						}
//					}
//				}
//				
//			}
//			
//		});
		
		m_objTable.setRowHeight((int) m_heightRow);
		m_objTable.getTableHeader().setPreferredSize(
				new Dimension(m_objTable.getTableHeader().getPreferredSize().width,(int) m_heightRow));
		if (hideHeader)
			m_objTable.setTableHeader(null);
		m_objTableModel.setTable(this);

		if (m_control instanceof MouseListener){
			m_objTable.addMouseListener((MouseListener) m_control);
		}
		
		//Listener para gestionar si hay una imagen mostrandose en la tabla, abrirla en grande si se hace doble click sobre ella
		m_objTable.addMouseListener(new MouseAdapter() {
				
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					JTable table=GTable.this.getTable();
					GTableColumn columna = ((GFormTable) m_objFormField).getColumn(table.getSelectedColumn());
					if(columna.getType()==GConst.TM_IMAGE){
						Object value=GTable.this.getModel().getValueAt(table.getSelectedRow(), table.getSelectedColumn());
						//System.err.println("value:"+value);
						if(value!=null){
							String filePath=(String)value;
							filePath=filePath.replaceAll(Constants.smallImage, "");
							
							final JFrame j=new JFrame(Utils.normalizeLabel(m_label));
							j.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							j.setIconImage(m_server.getIcon(window,"icon",0,0).getImage());
							j.setResizable(true);
							
							ImageIcon originalImage=new ImageIcon(filePath);
							if(originalImage.getImageLoadStatus()==MediaTracker.ERRORED)//Significaria que es una imagen en base de datos
								originalImage=new ImageIcon(((communicator)m_server).serverGetFilesURL(filePath));
					    	
					     	final Image imageAux=originalImage.getImage();
					     	
					     	ImageIcon imageIcon=new ImageIcon(imageAux);
					     	
							final JLabel labelImage=new JLabel(imageIcon);
							labelImage.setOpaque(true);
							labelImage.setVerticalAlignment(JLabel.CENTER);
							labelImage.setHorizontalAlignment(JLabel.CENTER);
							//JPanel panelAux=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
							//panelAux.add(labelImage);
							
							final JScrollPane scroll=new JScrollPane(labelImage);
							scroll.getVerticalScrollBar().setUnitIncrement(10);
							
							final JPanel buttonsPanel=new JPanel(/*new FlowLayout(FlowLayout.CENTER,0,0)*/);
							buttonsPanel.setBorder(new EmptyBorder(0,0,0,0));
							
							final JPanel panel=new JPanel(new BorderLayout(0,0));
							
							ActionListener listener=new ActionListener(){

								int zoom=0;
								public void actionPerformed(ActionEvent ae) {
									JButton boton = (JButton) ae.getSource();
						            String command = boton.getActionCommand();
									if (Integer.valueOf(command) == botoneraAccion.INCREASE_ZOOM || Integer.valueOf(command) == botoneraAccion.DECREASE_ZOOM) {
										if(Integer.valueOf(command) == botoneraAccion.INCREASE_ZOOM)
											zoom++;
										else zoom--;
																								     	
								     	int width=imageAux.getWidth(panel);
								     	int height=imageAux.getHeight(panel);
								     	
								     	int newWidth;
								     	int newHeight;
								     	if(zoom>0){
								     		newWidth=width*(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
								     		newHeight=height*(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
								     	}else{
								     		newWidth=width/(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
								     		newHeight=height/(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
								     	}
								     	
								     	if(newWidth>0 && newHeight>0){
								     		ImageIcon imageIcon=new ImageIcon(imageAux.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
								     		
								     		labelImage.setIcon(imageIcon);
											labelImage.repaint();
											//scroll.validate();
											scroll.repaint();
								     	}else{
								     		//Restauramos el campo zoom ya que no hemos hecho nada porque el número es negativo
								     		if(Integer.valueOf(command) == botoneraAccion.INCREASE_ZOOM)
												zoom--;
											else zoom++;
								     	}
					                }
					                
								}
								
							};
							
							int buttonHeight= (int)GViewBalancer.getRowHeightS(getFormField().getViewBalancer().getGraphics());
							int buttonWidth=buttonHeight;
							
							JButton botonIncreaseZoom = botoneraAccion.subBuildBoton(buttonsPanel, null, "zoom_in", ""+botoneraAccion.INCREASE_ZOOM,
									"Más zoom", buttonWidth,buttonHeight,true,m_server);
							botonIncreaseZoom.addActionListener(listener);
							JButton botonDecreaseZoom = botoneraAccion.subBuildBoton(buttonsPanel, null, "zoom_out", ""+botoneraAccion.DECREASE_ZOOM,
									"Menos zoom", buttonWidth,buttonHeight,true,m_server);
							botonDecreaseZoom.addActionListener(listener);
							
							panel.add(scroll,BorderLayout.CENTER);
							panel.add(buttonsPanel,BorderLayout.SOUTH);
							
							//Para que escape cierre la ventana
							GConst.addShortCut(null, panel, GConst.CANCEL_SHORTCUT_KEY, GConst.CANCEL_SHORTCUT_MODIFIERS, "Cancelar ventana", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

								private static final long serialVersionUID = 1L;
								
								public void actionPerformed(ActionEvent arg0) {
									j.dispose();
								}
								
							});
							j.setContentPane(panel);

							j.setPreferredSize(((communicator)m_server).m_cliente.getSize());
							j.pack();
							j.setLocationRelativeTo(SwingUtilities.getWindowAncestor(table));
							j.setVisible(true);
							e.consume();//Lo consumimos para que no lo gestione m_control abriendo la ventana del formulario
						}
					}
				}
			}
			
		});

		buildRenders();

		m_objTableModel.m_moa = m_moa;
		m_objTableModel.m_idPropModel = m_idProps;
		m_objTableModel.inicializaGroupByColumns();
		// tbm.BuildData(data, false);

		/* int rows = Integer.parseInt(itemView.getAttributeValue("ROWS")); */
		/* int rows=m_ff.getRows(); */
		int rows = ((GFormTable) m_objFormField).getVisibleRowCount();
		boolean containerDriven = m_label == null; // itemView.getAttributeValue("CONTAINER_DRIVEN")!=null
		// &&
		// itemView.getAttributeValue("CONTAINER_DRIVEN").equals("TRUE");
		JComponent tableView = m_objTable;
		JComponent tablePanel = m_objTable;
		if (rows > 1 || m_topLabel && m_label != null) {
			/*
			 * m_objTable.setPreferredScrollableViewportSize(new
			 * Dimension(width,height));
			 */
			/* JScrollPane scrollPane = new JScrollPane(table); */
			// scrollPane.setPreferredSize(new Dimension(width,height));
			// scrollPane.setMinimumSize(new Dimension(width,height));
			// scrollPane.setMaximumSize(new Dimension(width,height));
			/* tableView = scrollPane; */
			tableView = m_objComponent;
			tablePanel = tableView;
		}

		if (m_topLabel && m_label != null) {
			tablePanel = new JPanel();

			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
			JPanel botonera = null;

			if (!containerDriven) {
				/*
				 * access myAccess = new
				 * access(((GFormTable)m_objFormField).getAccess());
				 */
				/*
				 * OperationsObject operations =
				 * ((GFormTable)m_objFormField).getOperations();
				 */
				// HashMap<Integer,ArrayList<UserAccess>>
				// accessUserTasks=((GFormTable)m_objFormField).getOperations();
				AccessAdapter accessAdapter = ((GFormTable) m_objFormField)
				.getOperations();
				botonera = (new botoneraAccion(

						m_id,m_name,null,null,null,null,null,null,null,false,
						botoneraAccion.TABLE_TYPE,
						null,
						null,
						null,
						(ActionListener) m_control,
						/* myAccess, *//* operations, *//* accessUserTasks, */accessAdapter, m_modoConsulta, true, getFormField().getViewBalancer().getGraphics(),m_server,this,m_componentListener.isAllowedConfigTables())).getComponent();
			}
			JLabel title = new JLabel(m_label);
			// title.setFont( m_boldFont );



			if (containerDriven) {
				tablePanel.add(title);
				/*
				 * m_objTable.setPreferredSize(new Dimension(width, height));
				 * m_objTable.setMinimumSize(new Dimension(width, height));
				 */
				// table.setMaximumSize(new Dimension(width,height));
			} else {
				/*
				 * int hCab = Integer.parseInt(itemView.getAttributeValue(
				 * "HEIGHT_LABEL")); int wLab =
				 * Integer.parseInt(itemView.getAttributeValue( "WIDTH_LABEL"));
				 */
				//int hCab = ((GFormTable) m_objFormField).getAnchoBoton();
				//int wLab = ((GFormTable) m_objFormField).getLabelWidth();
				/*
				 * Dimension dimCab = new Dimension(width, hCab); Dimension
				 * dimTable = new Dimension(width, height);
				 */
				JPanel cabecera = new JPanel();



				cabecera.setAlignmentX(Component.LEFT_ALIGNMENT);
				tableView.setAlignmentX(Component.LEFT_ALIGNMENT);

				title.setAlignmentX(Component.LEFT_ALIGNMENT);
				botonera.setAlignmentX(Component.LEFT_ALIGNMENT);

				// tableView.setPreferredSize(dimTable);
				// tableView.setMinimumSize(dimTable);
				// tableView.setMaximumSize(dimTable);
				m_objTable.setShowGrid(true);

				// cabecera.setBackground(Color.RED);
				FlowLayout fl = (FlowLayout) cabecera.getLayout();
				fl.setAlignment(FlowLayout.LEFT);
				fl.setHgap(0);
				fl.setVgap(0);
				cabecera.setBorder(new EmptyBorder(0, 0, 0, 0));
				cabecera.add(title);
				cabecera.add(botonera);
				tablePanel.add(cabecera);
				/*
				 * itemView.setAttribute("HEIGHT", String.valueOf(height +
				 * hCab));
				 */
				// será utilizada mas tarde como alto total ya que las
				// siguientes funciones no van
				// a gestionar la cabecera ni la etiqueta
			}
			tablePanel.add(tableView, BorderLayout.CENTER);

			/** ***************************************************** */
			JPanel panelBotonera = new JPanel();

			panelBotonera.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			/* panelBotonera.setAlignmentX(Component.LEFT_ALIGNMENT); */
			/* botonera.setAlignmentX(Component.LEFT_ALIGNMENT); */

			panelBotonera.add(botonera);
			m_objComponentSec = panelBotonera;
			/** ***************************************************** */

		} else {
			/* int wT = width; */
			if (!containerDriven) { // la etiqueta se la pondria buildComponent
				// normalmente
				tablePanel = new JPanel();


				FlowLayout fl = (FlowLayout) tablePanel.getLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				/*
				 * int ladoBoton = Integer.parseInt(itemView.getAttributeValue(
				 * "WIDTH_BUTTON"));
				 */
				// int ladoBoton = ((GFormTable)m_objFormField).getAnchoBoton();
				/* wT = width - ladoBoton; */
				/*
				 * height = Math.max(height, Integer.parseInt(itemView.
				 * getAttributeValue("WIDTH_BUTTON")));
				 */
				/*
				 * height =
				 * Math.max(height,((GFormTable)m_objFormField).getAnchoBoton());
				 */
				/* itemView.setAttribute("HEIGHT", String.valueOf(height)); */
				/* m_objTable.setBackground(Color.LIGHT_GRAY); */
			}
			if (rows == 1) {
				/*
				 * m_objTable.setPreferredSize(new Dimension(wT, height));
				 * m_objTable.setMinimumSize(new Dimension(wT, height));
				 * m_objTable.setMaximumSize(new Dimension(wT, height));
				 */
			}
			if (!containerDriven) {
				JPanel panelBotonera = new JPanel();


				panelBotonera.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

				// TODO Al quitar el boton de buscar no haria falta un IdOperationForm
				IdOperationForm idOperation = new IdOperationForm();
				idOperation.setOperationType(botoneraAccion.OPERATION_ACTION);

				IdObjectForm idObjForm = new IdObjectForm(m_id);

				AccessAdapter accessAdapter = ((GFormTable) m_objFormField).getOperations();
				Iterator<Integer> itr = accessAdapter.getUserTasksAccess(AccessAdapter.VIEW).iterator();

				if (itr.hasNext())
					idObjForm.setIdtoUserTask(itr.next());
				idOperation.setTarget(idObjForm);
				idOperation.setButtonType(botoneraAccion.ABRIR);

				botoneraAccion botonera = new botoneraAccion(  m_id, m_name, null, null, null, null, null, null, null, false, botoneraAccion.TABLE_TYPE, null, null, null,
						(ActionListener) m_control, accessAdapter, m_modoConsulta, true, getFormField().getViewBalancer().getGraphics(),m_server,this,m_componentListener.isAllowedConfigTables());

				JPanel botoneraPanel = botonera.getComponent();
				Dimension dimButton=m_objFormField.getDimComponenteSecundario();
				
				if(botonera.getNumButtons()==1){//Si solo viene view por ejemplo
					m_objComponentSec = botoneraPanel;
					AbstractButton button=botonera.getBotones().get(0);
					button.setPreferredSize(new Dimension(dimButton.width,dimButton.height));
					
					botonera.addListener(new ActionListener(){

						public void actionPerformed(ActionEvent ae) {
							if (m_objTable.getRowCount() != 0)
								m_objTable.setRowSelectionInterval(0, 0);
						}
					});
				}else{
					final JButton boton = botoneraAccion.subBuildBoton((JPanel) panelBotonera, null, "showButtons", GTable.BUTTON_ONE_FILE,
							"Acciones", "acciones@"+getFormField().getName(), dimButton.width,/*dimButton.height*/(int)getHeightRow(),true,m_server);
					boton.setPreferredSize(new Dimension(dimButton.width,dimButton.height));
					boton.addActionListener(this);
					boton.addFocusListener(focusListener);
					if(botonera.getBotones().isEmpty())
						boton.setEnabled(false);

					final botoneraAccion botoneraThis=botonera;
					
					GConst.addShortCut(this, boton, GConst.VIEW_SHORTCUT_KEY, GConst.VIEW_SHORTCUT_MODIFIERS, "Consultar", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){

						public void actionPerformed(ActionEvent arg0) {
							int buttonType=botoneraAccion.CONSULTAR;
							doClickButtonsPopupAction(botoneraThis, buttonType, boton);
						}
						
					});
					GConst.addShortCut(this, boton, GConst.SET_SHORTCUT_KEY, GConst.SET_SHORTCUT_MODIFIERS, "Modificar", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){

						public void actionPerformed(ActionEvent arg0) {
							int buttonType=botoneraAccion.EDITAR;
							doClickButtonsPopupAction(botoneraThis, buttonType, boton);
						}
						
					});
					GConst.addShortCut(this, boton, GConst.NEW_SHORTCUT_KEY, GConst.NEW_SHORTCUT_MODIFIERS, "Crear", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){

						public void actionPerformed(ActionEvent arg0) {
							int buttonType=botoneraAccion.CREAR;
							doClickButtonsPopupAction(botoneraThis, buttonType, boton);
						}
						
					});
					GConst.addShortCut(this, boton, GConst.SEARCH_SHORTCUT_KEY, GConst.SEARCH_SHORTCUT_MODIFIERS, "Buscar", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){

						public void actionPerformed(ActionEvent arg0) {
							int buttonType=botoneraAccion.BUSCAR;
							doClickButtonsPopupAction(botoneraThis, buttonType, boton);
						}
						
					});
					
					
					m_menuBotonera = new JPopupMenu();
					m_menuBotonera.setFocusCycleRoot(true);//El ciclo del foco solo debe moverse por la botonera
					m_menuBotonera.add(botoneraPanel);
					botoneraPanel.addMouseListener(this);
					
					m_menuBotonera.addPopupMenuListener(new PopupMenuListener(){
						public void popupMenuCanceled(PopupMenuEvent ev){
							//m_objTable.clearSelection();							
						}
						public void popupMenuWillBecomeInvisible(PopupMenuEvent ev){}
						public void popupMenuWillBecomeVisible(PopupMenuEvent ev){}						
					});

					botonera.addListener(this);
					m_objComponentSec = panelBotonera;					
				}				
				((JScrollPane) m_objComponent).setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				((JScrollPane) m_objComponent).getVerticalScrollBar().setUnitIncrement(GConfigView.IncrementScrollVertical);
				Color color=UIManager.getColor("TextField.background");
				((JScrollPane)m_objComponent).getViewport().setBackground(color);
				m_objTable.setBackground(color);
			}
		}
		if(!m_nullable && !m_modoFilter){
			((JScrollPane)m_objComponent).getViewport().setBackground(GConfigView.colorBackgroundRequired);
			m_objTable.setBackground(GConfigView.colorBackgroundRequired);
		}
		
		if(isTopLabel()){//Solo permitimos el pegado masivo en tablas de mas de una fila
			GConst.addShortCut(this, this.m_objTable, GConst.PASTE_TABLE_ROWS, GConst.PASTE_TABLE_ROWS_MODIFIERS, "CopyRows", JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new AbstractAction(){

				private static final long serialVersionUID = 1L;
				
				public void actionPerformed(ActionEvent arg0) {
					try {
						String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
						String[] rows=data.split("\\n");
						ArrayList<String[]> list=new ArrayList<String[]>();
						for (String row : rows) {
							String[] columns=row.split("\\t");
							list.add(columns);
						}
						int row=0;
						int column=0;
						m_componentListener.setProcessingCopyRowTable(true);
						processingPasteRows=true;
						abortPasteRows=false;
						processRowColumn(list, row, column);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				private void processRowColumn(final ArrayList<String[]> list, final int row, final int column) {
					final String[] columnsValue=list.get(row);
					final String value=columnsValue[column];
					
					final Runnable runnable=new Runnable() {
						
						@Override
						public void run() {
							
									int nextRow=columnsValue.length>column+1?row:row+1;
									int nextColumn=columnsValue.length>column+1?column+1:0;
									
									int tableRow=m_objTableModel.getRowCount()-1;
									if(nextRow<list.size()){
										if(nextRow!=row){
											m_objTable.setRowSelectionInterval(tableRow, tableRow);
										}
										if(nextColumn>=m_objTableModel.getColumnCount()){
											getMessageListener().showErrorMessage("Está intentando copiar mas columnas de las que admite la tabla",SwingUtilities.getWindowAncestor(GTable.this));
										}else{
											if(!m_objTableModel.isExecutingSetValue() && !m_objTable.isEditing()){
												processRowColumn(list, nextRow, nextColumn);
											}else{
												m_objTable.requestFocus();
												m_objTable.requestFocusInWindow();
												SwingUtilities.invokeLater(this);
											}
										}
									}else{
										m_componentListener.setProcessingCopyRowTable(false);
										processingPasteRows=false;
										m_objTable.setRowSelectionInterval(tableRow, tableRow);//Para que salte el foco y se termine de crear la ultima linea
									}
						}
					};

					
					int tableRow=column==0?m_objTableModel.getRowCount()-1:m_objTableModel.getRowCount()-2;//TODO Esto fallaria para tablas con cardinalidad maxima mayor que 1 pero menor que null. Pero no hay casos de esos ahora mismo
					if(value!=null && !value.isEmpty() && GTable.this.m_objTable.isCellEditable(tableRow, column) && (GTable.this.m_objTable.getEditorComponent()!=null/*Para que entre la primera vez si estamos en la celda*/ || GTable.this.m_objTable.editCellAt(tableRow, column))){
						final Component component=m_objTable.getEditorComponent();
						
						component.addFocusListener(new FocusListener() {
								
							private boolean changeCellValueExecuted=false;
								@Override
								public void focusLost(FocusEvent fe) {
									if(!fe.isTemporary()){
										if(!abortPasteRows){//Para parar lo que queda de copia si el usuario ha cancelado la creacion de un objeto en una pregunta
											SwingUtilities.invokeLater(runnable);
										}else{
											m_componentListener.setProcessingCopyRowTable(false);
											processingPasteRows=false;
										}
										component.removeFocusListener(this);
									}
								}
								
								@Override
								public void focusGained(FocusEvent fe) {
									if(!changeCellValueExecuted){//Para evitar que se vuelva a llamar cuando ha dado algun fallo, ya que el foco vuelve al componente y no se dispara focusLost antes porque es un cambio temporal
										changeCellValue(row, value);
										changeCellValueExecuted=true;
									}else{
										component.removeFocusListener(this);//Abortamos la ejecucion de la copia (no se llamara a focusLost) ya que ha habido algun fallo
									}
								}
							});
						
						if(!component.hasFocus()){
							component.requestFocus();
							component.requestFocusInWindow();
						}else{//Hacemos lo mismo que en focusGained ya que no se llamara a ese metodo
							changeCellValue(row, value);
						}
					}else{
						SwingUtilities.invokeLater(runnable);
					}

				}
				
				private void changeCellValue(int row,String value){
					CellEditor cell=(CellEditor)GTable.this.m_objTable.getCellEditor();
					if(row==0){//si estamos en la primera fila a copiar ponemos a false que recuerde que tipo de objetos crear ya que podriamos haber hecho una copia antes y la recordaria tambien para esta
						cell.setRememberSelectionForCopyRows(false);
					}
					cell.setValue(value);
					KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
				}
				
				
			});
		}
		
		

		/* m_objComponent=tablePanel; */
		/* m_tables.put(id, m_objTable); */
		//m_formComponents.add(m_objTable.getModel());


		/*
		 * m_objTable.setPreferredScrollableViewportSize(m_dimensiones);
		 * /*m_objTable.buildRenders(m_com, itemView);
		 */
		/*
		 * m_objTableModel.m_moa = m_moa; m_objTableModel.m_taposModel =
		 * m_idProp; m_objTableModel.inicializaGroupByColumns();
		 * 
		 * boolean containerDriven = m_label == null; if (containerDriven) {
		 * m_objTable.setPreferredSize(m_dimensiones);
		 * m_objTable.setMinimumSize(m_dimensiones); //table.setMaximumSize(new
		 * Dimension(width,height)); }
		 */// tbm.BuildData(data, false);
		/*
		 * int rows = Integer.parseInt(itemView.getAttributeValue("ROWS"));
		 * boolean containerDriven = m_label == null;
		 * //itemView.getAttributeValue("CONTAINER_DRIVEN")!=null &&
		 * //itemView.getAttributeValue("CONTAINER_DRIVEN").equals("TRUE");
		 * JComponent tableView = table; JComponent tablePanel = table; if (rows >
		 * 1 || m_topLabel && m_label != null) {
		 * m_objTable.setPreferredScrollableViewportSize(new
		 * Dimension(width,height)); /*JScrollPane scrollPane = new
		 * JScrollPane(table);
		 */
		// scrollPane.setPreferredSize(new Dimension(width,height));
		// scrollPane.setMinimumSize(new Dimension(width,height));
		// scrollPane.setMaximumSize(new Dimension(width,height));
		/*
		 * tableView = scrollPane; tablePanel = tableView;
		 */
		/*
		 * }
		 * 
		 * if (m_topLabel && m_label != null) { tablePanel = new JPanel(); if
		 * (m_colorFondo != null) tablePanel.setBackground(m_colorFondo);
		 * 
		 * tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		 * JPanel botonera = null;
		 * 
		 * if (!containerDriven) { botonera = (new botoneraAccion(m_colorFondo,
		 * m_com, m_md, 0, idTable, null, false, false, null, (ActionListener)
		 * m_control, myAccess, true, false, false, m_modoConsultar,
		 * false)).getComponent(); } JLabel title = new JLabel(label);
		 * //title.setFont( m_boldFont );
		 * 
		 * if (m_colorFondo != null) title.setBackground(m_colorFondo);
		 * 
		 * if (containerDriven) { tablePanel.add(title);
		 * table.setPreferredSize(new Dimension(width, height));
		 * table.setMinimumSize(new Dimension(width, height));
		 * //table.setMaximumSize(new Dimension(width,height)); } else { int
		 * hCab = Integer.parseInt(itemView.getAttributeValue( "HEIGHT_LABEL"));
		 * int wLab = Integer.parseInt(itemView.getAttributeValue(
		 * "WIDTH_LABEL")); Dimension dimCab = new Dimension(width, hCab);
		 * Dimension dimTable = new Dimension(width, height); JPanel cabecera =
		 * new JPanel(); if (m_colorFondo != null)
		 * cabecera.setBackground(m_colorFondo);
		 * 
		 * cabecera.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * tableView.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * 
		 * title.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * botonera.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * 
		 * //tableView.setPreferredSize(dimTable);
		 * //tableView.setMinimumSize(dimTable);
		 * //tableView.setMaximumSize(dimTable); table.setShowGrid(true);
		 * 
		 * //cabecera.setBackground(Color.RED); FlowLayout fl = (FlowLayout)
		 * cabecera.getLayout(); fl.setAlignment(FlowLayout.LEFT);
		 * fl.setHgap(0); fl.setVgap(0); cabecera.setBorder(new EmptyBorder(0,
		 * 0, 0, 0)); cabecera.add(title); cabecera.add(botonera);
		 * tablePanel.add(cabecera); itemView.setAttribute("HEIGHT",
		 * String.valueOf(height + hCab)); //será utilizada mas tarde como alto
		 * total ya que las siguientes funciones no van // a gestionar la
		 * cabecera ni la etiqueta
		 *  } tablePanel.add(tableView); } else { int wT = width; if
		 * (!containerDriven) { //la etiqueta se la pondria buildComponent
		 * normalmente tablePanel = new JPanel(); if (m_colorFondo != null)
		 * tablePanel.setBackground(m_colorFondo);
		 * 
		 * FlowLayout fl = (FlowLayout) tablePanel.getLayout(); fl.setHgap(0);
		 * fl.setVgap(0); int ladoBoton =
		 * Integer.parseInt(itemView.getAttributeValue( "WIDTH_BUTTON")); wT =
		 * width - ladoBoton; height = Math.max(height,
		 * Integer.parseInt(itemView. getAttributeValue("WIDTH_BUTTON")));
		 * itemView.setAttribute("HEIGHT", String.valueOf(height));
		 * table.setBackground(Color.LIGHT_GRAY); } if (rows == 1) {
		 * table.setPreferredSize(new Dimension(wT, height));
		 * table.setMinimumSize(new Dimension(wT, height));
		 * table.setMaximumSize(new Dimension(wT, height)); } if
		 * (!containerDriven) { tablePanel.add(tableView); JButton boton =
		 * botoneraAccion.subBuildBoton((JPanel) tablePanel, m_com, null,
		 * "tablelk", "ACTION:" + 0 + ":" + idTable + ":" +
		 * botoneraAccion.ABRIR, "Detalle", true);
		 * boton.addActionListener((ActionListener) m_control); int ladoBoton =
		 * Integer.parseInt(itemView.getAttributeValue( "WIDTH_BUTTON"));
		 * boton.setMinimumSize(new Dimension(ladoBoton, ladoBoton));
		 * boton.setPreferredSize(new Dimension(ladoBoton, ladoBoton));
		 * tablePanel.add(boton); } } m_tables.put(idTable, table);
		 */}

	
	//Busca en la botonera accion el buttonType y si lo encuentra hace click sobre él
	private void doClickButtonsPopupAction(botoneraAccion botonera, int buttonType, final JButton buttonOneFile){
		if(!focusTraversalManagingError){//Si focusTraversal esta gestionando un error no pulsamos el boton ya que seria un problema para la gestion del foco. Actualmente siempre entra en este if
			Iterator<AbstractButton> itr=botonera.getBotones().iterator();
			while(itr.hasNext()){
				final AbstractButton button=itr.next();
				if(new IdOperationForm(button.getActionCommand()).getButtonType()==buttonType){
					buttonOneFile.requestFocus();
					buttonOneFile.requestFocusInWindow();
					
					//Necesitamos hacer click sobre el boton de acciones para que se abra el popup y poder hacer click sobre los botones
					//Lo hacemos en un invokeLater ya que los botones creados con botoneraAccion no hacen click hasta que realmente tienen el foco
					SwingUtilities.invokeLater(new Runnable(){
	
						public void run() {
							// Lo hacemos en otro invokeLater para dar tiempo a que GTable pueda estar gestionando un error de datos
							SwingUtilities.invokeLater(new Runnable(){
	
								public void run() {
										if(!focusTraversalManagingError){//Si focusTraversal esta gestionando un error no pulsamos el boton ya que seria un problema para la gestion del foco
											buttonOneFile.doClick();
											SwingUtilities.invokeLater(new Runnable(){
					
												public void run() {
													if(button.isVisible() && button.isShowing()){
														button.requestFocus();
														button.requestFocusInWindow();
														SwingUtilities.invokeLater(new Runnable(){
						
															public void run() {
																button.doClick();
															}
															
														});
													}
												}
												
											});
										}
										
								}
							});
						}
						
					});
				}
					
			}
		}
	}
	
	
	// Reescribimos el metodo para tratar las peculiaridades de este componente
	// ya que si es de una sola fila
	// el tamaño de fila deberia ser igual al tamaño del componente
	public void setComponentBounds(Rectangle rc) {
		if (!m_topLabel)
			m_objTable.setRowHeight(rc.height);
		super.setComponentBounds(rc);
	}

	public JTable getTable() {
		return m_objTable;
	}
	
	public void editFirstCellEditable(int row,int column,boolean searchNextRow,boolean preferRequiredEmpty){
		int rowAux=row;
		int columnAux=column;
		int firstColumnEmptyNotRequired=-1;
		processingSelectCellFromSystem=true;
		try{
			while(m_objTable.getRowCount()>rowAux && m_objTable.getColumnCount()>columnAux && ((!m_objTable.isCellEditable(rowAux, columnAux) || m_objTable.getValueAt(rowAux, columnAux)!=null) || (preferRequiredEmpty && (((GTableColumn)m_listaColumnas.get(columnAux)).isNullable() || m_objTable.getValueAt(rowAux, columnAux)!=null)))){
				columnAux++;
				if(m_objTable.getColumnCount()<=columnAux){
					if(searchNextRow){
						columnAux=0;
						rowAux++;
					}
				}else if(preferRequiredEmpty){
					//Almacenamos la primera columna vacia aunque no sea requerida para que si no encontramos una vacia requerida darle el foco
					if(firstColumnEmptyNotRequired==-1 && m_objTable.isCellEditable(rowAux, columnAux) && m_objTable.getValueAt(rowAux, columnAux)==null)
						firstColumnEmptyNotRequired=columnAux;
				}
			}
			if (m_objTable.getRowCount()>rowAux && m_objTable.getColumnCount()>columnAux && m_objTable.editCellAt(rowAux, columnAux)){
				m_objTable.setRowSelectionInterval(rowAux, rowAux);
				m_objTable.setColumnSelectionInterval(columnAux, columnAux);
				final Component editor=m_objTable.getEditorComponent();
				if(!editor.requestFocusInWindow()){
					editor.requestFocus();
				}
				//System.err.println("************************************************focusGained la tabla:"+getLabel()+" exito:"+exito);
			}else{
				if(firstColumnEmptyNotRequired!=-1 && m_objTable.editCellAt(row, firstColumnEmptyNotRequired)){
					m_objTable.setRowSelectionInterval(row, row);
					m_objTable.setColumnSelectionInterval(firstColumnEmptyNotRequired, firstColumnEmptyNotRequired);
					final Component editor=m_objTable.getEditorComponent();
					if(!editor.requestFocusInWindow()){
						editor.requestFocus();
					}
				}else{
					m_objTable.setRowSelectionInterval(row, row);
					m_objTable.setColumnSelectionInterval(column, column);
				}
			}
		}finally{
			processingSelectCellFromSystem=false;
		}
	}

	/*
	 * public JPanel getBotonera(){ return m_objComponentSec; }
	 */

	public boolean isHideHeader() {
		return hideHeader;
	}

	/*
	 * public void setBotoneraComponentBounds(Rectangle rc){
	 * if(m_objBotonera!=null) if(rc!=null) m_objBotonera.setBounds(rc); }
	 */

	/*
	 * public void create() { m_objLabel = new
	 * JLabel(m_objFormField.getLabel()); this.add(m_objLabel);
	 * createComponent(); if(m_objComponentSec!=null)
	 * this.add(m_objComponentSec); this.add(m_objComponent); }
	 */

	void buildRenders(/* communicator com,org.jdom.Element columns */) {
		if (m_server != null) {
			iconoAdd = ((communicator)m_server).getIcon(null,"addFavourites",(int)m_heightRow-6/*3+3 del borde*/,(int)m_heightRow-6/*3+3 del borde*/);
			iconoDel = ((communicator)m_server).getIcon(null,"removeFavourites",(int)m_heightRow-6,(int)m_heightRow-6);
		}
		TableColumnModel tcm = m_objTable.getColumnModel();

		int col = 0;

		MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();

		for (int pos = 0; pos < tcm.getColumnCount(); pos++) {
			TableColumn tc = tcm.getColumn(pos);
			//System.err.println("Pos "+pos+" hideHeader "+hideHeader+" tfm.dobleHeaderSize "+tfm.dobleHeaderSize+" tfm.m_cuantitativo "+tfm.m_cuantitativo);
							
			if (pos == 0 && m_objTableModel.m_cuantitativo) {
				tc.setCellRenderer(new GroupButtonCellRenderer(this, true));
				tc.setCellEditor(new GroupButtonCellEditor(this, " ", true));
				tc.setWidth(23);
				tc.setMaxWidth(23);
				continue;
			}
			if (m_objTableModel.m_cuantitativo)
				col = pos - 1;
			else
				col = pos;

			col = m_objTableModel.getRealColumn(col);
			
			/*
			 * org.jdom.Element item= jdomParser.findElementByAt(columns,
			 * "ITEM", "COLUMN", String.valueOf(col), false);
			 */
			GTableColumn columna = ((GFormTable) m_objFormField).getColumn(col);
			
			if (!hideHeader){
				if(m_objTableModel.dobleHeaderSize){
					tc.setHeaderRenderer(renderer);
				}else if(Auxiliar.equals(m_objTableModel.getColumnSelectionRowTable(),col)){
					tc.setHeaderRenderer(new CheckHeaderRenderer(columna, this));
					tc.setHeaderValue(false);
				}
			}

			// tc.setWidth( Integer.parseInt("WIDTH") );
			//System.outprintln("COL_" + col);
			// En botones tapos es nulo
			Integer idProp = columna.getIdProp();
			/*
			 * Integer tapos= item.getAttributeValue("TA_POS")==null ? null:new
			 * Integer(item.getAttributeValue("TA_POS"));
			 */

			m_idProps.add(idProp);

			/*
			 * String tipo= columna.getClaseCampo(); /*String tipo=
			 * item.getAttributeValue("TYPE");
			 */

			/* int tm= m_md.getID_TM(tapos); */
			/*
			 * int tm=columna.getType();
			 * 
			 * if( tm== GConst.TM_DATE) tc.setCellRenderer(new dateRender());
			 * if(tipo.equals("LIST")){ tc.setCellRenderer(
			 * buildListRenderer(/*tapos
			 *//*
			 * columna.getValuesPossible()) );
			 * /*setComboEditor(
			 * tc, item);
			 */
			/*
			 * setComboEditor( tc, columna); } if(tipo.equals("TEXT"))
			 * /*setEditEditor(tc, item);
			 */
			/*
			 * setEditEditor(tc, columna); if(tipo.equals("CHECK")){
			 * tc.setCellRenderer( new CheckCellRenderer( m_comm, this ));
			 * /*setDefaultField( item );
			 */
			/*
			 * setDefaultField( columna ); } if(tipo.equals("BUTTON"))
			 * /*setButtonEditor( tc, item, m_comm );
			 */
			/*
			 * setButtonEditor( tc, columna, m_comm );
			 * 
			 */

			int typeField = columna.getType();

			switch (typeField) {

			case GConst.TM_DATE:
				tc.setCellRenderer(new DateCellRenderer(this, columna, m_modoFilter));
				tc.setCellEditor(new DateCellEditor(columna, this, m_modoFilter, focusListener, m_server, typeField));
				//setEditEditor(tc, columna);
				break;
			case GConst.TM_DATE_HOUR:
				tc.setCellRenderer(new DateCellRenderer(this, columna, m_modoFilter));
				tc.setCellEditor(new DateCellEditor(columna, this, m_modoFilter, focusListener, m_server, typeField));
				//setEditEditor(tc, columna);
				break;
			case GConst.TM_HOUR:
				tc.setCellRenderer(new DateCellRenderer(this, columna, m_modoFilter));
				tc.setCellEditor(new DateCellEditor(columna, this, m_modoFilter, focusListener, m_server, typeField));
				//setEditEditor(tc, columna);
				break;
			case GConst.TM_ENUMERATED:				
				TableCellRenderer render = buildListRenderer(columna.getValuesPossible(), pos);
				tc.setCellRenderer(render);
				setComboEditor(tc, columna);
				break;
			case GConst.TM_INTEGER:
				tc.setCellRenderer(new TextCellRenderer(this, columna, m_modoFilter,typeField,null));
				tc.setCellEditor(new NumberCellEditor(columna, this, m_modoFilter, focusListener));
				//setEditEditor(tc, columna);
				break;
			case GConst.TM_REAL:
				tc.setCellRenderer(new TextCellRenderer(this, columna, m_modoFilter,typeField,columna.getRedondeo()));
				tc.setCellEditor(new NumberCellEditor(columna, this, m_modoFilter, focusListener));
				//setEditEditor(tc, columna);
				break;
			case GConst.TM_TEXT:
			case GConst.TM_MEMO:
				tc.setCellRenderer(new TextCellRenderer(this, columna, m_modoFilter,typeField,null));
				TextCellEditor editor = new TextCellEditor(columna, m_modoFilter, this, focusListener);
				tc.setCellEditor(editor);
				//m_objTable.addKeyListener(editor.getKeyListener());
				//setEditEditor(tc, columna);
				break;
			case GConst.TM_BOOLEAN:
				if(columna.getId().equals(GConst.ID_COLUMN_TABLE_SELECTION)){
					tc.setCellRenderer(new CheckCellRenderer(columna, this, m_modoFilter));
					tc.setCellEditor(new CheckCellEditor(columna, this, focusListener));
					int ancho = columna.getWidth();
					tc.setWidth(ancho);
					tc.setMaxWidth(ancho);
				}else{
					tc.setCellRenderer(new CheckTristateCellRenderer(columna, this, m_modoFilter));
					tc.setCellEditor(new CheckTristateCellEditor(columna, this, focusListener));
				}
				//setDefaultField(columna);
				break;
			case GConst.TM_BOOLEAN_COMMENTED:
				tc.setCellRenderer(new CheckTristateCellRenderer(columna, this, m_modoFilter));
				tc.setCellEditor(new CheckTristateCellEditor(columna, this, focusListener));
				//setDefaultField(columna);
				break;
			case GConst.TM_BUTTON:
				setButtonEditor(tc, columna/*, m_server*/);
				break;
			case GConst.TM_FILE:
			case GConst.TM_IMAGE:
				tc.setCellRenderer(new TextCellRenderer(this, columna, m_modoFilter,typeField,null));
				tc.setCellEditor(new FileCellEditor(columna, this, m_modoFilter, focusListener, m_server, typeField));
				break;
			}
		}
	}	

	public GTableModel getModel(){
		return m_objTableModel;
	}

	private TableCellRenderer buildListRenderer(ArrayList<GValue> valuesPossible, int col) {
		Collections.sort(valuesPossible);
		if(m_modoFilter){
			ComboBoxRendererLabel render = new ComboBoxRendererLabel(this);

			// Añadimos la opcion de no seleccionar nada
			render.addLine(0,"");

			Iterator<GValue> itrId = valuesPossible.iterator();
			while (itrId.hasNext()) {
				GValue parValue = itrId.next();
				int id = parValue.getId();
				String value = parValue.getLabel();
				render.addLine(id, value);			
			}
			return render;

		}else{
			ComboBoxRenderer render = new ComboBoxRenderer(this, (GTableColumn)m_listaColumnas.get(col));

			//Añadimos la opcion de no seleccionar nada
			//String label = ((GTableColumn)m_listaColumnas.get(col)).getLabel();
			//render.addLine(0,"<"+((GTableColumn)m_listaColumnas.get(col)).getLabel()+">");

			Iterator<GValue> itrId = valuesPossible.iterator();
			while (itrId.hasNext()) {
				GValue parValue = itrId.next();
				int id = parValue.getId();
				String value = parValue.getLabel();
				render.addLine(id, value);			
			}
			return render;
		}
	}

	private void setButtonEditor(TableColumn tc, GTableColumn columna/*,communicator com*/) {
		/* String tipo= itemView.getAttributeValue("ID"); */
		String tipo = columna.getId();
		ButtonCellEditor bce = new ButtonCellEditor((ActionListener) m_control,
				this, iconoAdd, iconoDel);
		
		tc.setCellRenderer(new ButtonCellRenderer(this,iconoAdd,iconoDel));
		tc.setCellEditor(bce);
		/* int ancho= Integer.parseInt( itemView.getAttributeValue("WIDTH") ); */
		int ancho = columna.getWidth();
		tc.setWidth(ancho);
		tc.setMaxWidth(ancho);
	}

	private void setComboEditor(TableColumn tc, GTableColumn columna) {
		String idForm = columna.getId();

		Vector<ItemList> vItems = new Vector<ItemList>();

		if(columna.isNullable()){
			boolean nullInitialSelection=false;
			vItems.add(new ItemList("0", null, "", nullInitialSelection));
		}

		Vector<GValue> vValues = new Vector<GValue>(columna.getValuesPossible());
		Enumeration en = vValues.elements();
		/*Vector vItems = new Vector();*/
		while(en.hasMoreElements())
		{
			GValue val = (GValue)en.nextElement();
			ItemList itl= new ItemList(String.valueOf(val.getId()),
					null,
					val.getLabel(),
					false
			/*true*/);
			vItems.addElement(itl);
			/*vItems.addElement(val.getLabel());*/
		}

		ComboBoxEditor cellEditor = new ComboBoxEditor(columna, vItems, this, focusListener);//columna.getValuesPossible());
		tc.setCellEditor(cellEditor);
		m_moa.put(idForm, cellEditor);
	}

//	public ArrayList getParameterSelectedRows() {
//		int[] rows = m_objTable.getSelectedRows();
//		if (rows.length == 0)
//			return null;
//		GTableModel tfm = (GTableModel) m_objTable.getModel();
//		return tfm.getParameter(rows);
//	}

	public void selectRow(int ido,boolean permanent) {
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		int visRow = tfm.findRow(ido, true, permanent);
		m_objTable.changeSelection(visRow, 0, false, false);
	}
	
	public void selectAll(boolean select) {
		if(m_objTableModel.getColumnSelectionRowTable()!=null){//Si existe una columna de seleccion de fila modificamos sus valores
			for(RowItem rowItem:m_objTableModel.getRowData()){
				rowItem.setColumnData(m_objTableModel.getColumnSelectionRowTable(),select);
			}
		}
		
		if(!select){
			m_objTable.clearSelection();
		}else{
			m_objTable.selectAll();
		}
	}

	public boolean isSelectAll(){
		return m_objTable.getSelectedRowCount()==m_objTable.getRowCount();
	}
	
	public RowItem getNextRow() {
		int[] rows = m_objTable.getSelectedRows();
		if (rows.length == 0)
			return null;
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		return tfm.getNextRow(rows[rows.length - 1]);
	}

	public RowItem getPrevRow() {
		int[] rows = m_objTable.getSelectedRows();
		if (rows.length == 0)
			return null;
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		return tfm.getPrevRow(rows[0]);
	}

	public GIdRow getDataFromIndex(int rowIndex) {
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		return tfm.getDataFromIndex(rowIndex);
	}
	
	public RowItem getCompletedDataFromIndex(int rowIndex) {
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		return tfm.getRowItemFromIndex(rowIndex);
	}

	public GIdRow getDataFromIdo(int ido,Boolean permanent) {
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		int row=tfm.findRow(ido, true, permanent);
		if(row!=-1)
			return tfm.getDataFromIndex(row);
		else return null;
	}

	public ArrayList getData(int idRow) {
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		return tfm.getData(idRow);
	}

	public ArrayList<GIdRow> getIdRowsSelectionData() {
		// GTableModel tfm= (GTableModel)m_objTable.getModel();
		return getIdRowsSelectedRows();
	}

	public ArrayList<GIdRow> getIdRowsData(Boolean permanent) {
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		return tfm.getIdRowsData(permanent);
	}

	public boolean selectionIsGroup() {
		int[] rows = m_objTable.getSelectedRows();
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		if (rows.length == 0)
			return false;
		for (int i = 0; i < rows.length; i++) {
			RowItem rt = (RowItem) tfm.m_rowData.get(rows[i]);
			if (rt.isGroup())
				return true;
		}
		return false;
	}

	public ArrayList getDataSelectedRows() {
		int[] rows = m_objTable.getSelectedRows();
		if (rows.length == 0)
			return null;
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		for (int row = 0; row < rows.length; row++) {
			ArrayList<Object> currentRowData = new ArrayList<Object>();
			for (int col = 0; col < tfm.getVisibleColumnDataCount(); col++) {
				Object val = tfm.getDataValueAt(rows[row], col);
				if (val instanceof ItemList)
					val = ((ItemList) val).getInteger();
				currentRowData.add(val);
			}
			result.add(currentRowData);
		}
		return result;
	}

	public ArrayList<GIdRow> getIdRowsSelectedRows() {
		int[] rows = m_objTable.getSelectedRows();
		ArrayList<GIdRow> result = new ArrayList<GIdRow>();
		if (rows.length == 0)
			return result;
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		for (int row = 0; row < rows.length; row++) {
			RowItem rt = (RowItem) tfm.m_rowData.get(rows[row]);
			if (rt.isGroup() || rt.isNullRow())
				continue;
			if(!result.contains(rt.getIdRow()))
				result.add(rt.getIdRow());
		}
		//System.err.println("Selected "+result);
		return result;
	}

	/*
	 * public ArrayList getStructuredDataSelectedRows(){ int[] rows=
	 * m_objTable.getSelectedRows(); if(rows.length==0) return null; ArrayList
	 * result= new ArrayList(); GTableModel tfm=
	 * (GTableModel)m_objTable.getModel(); for(int row=0; row< rows.length;
	 * row++){ ArrayList CurrentRowData= new ArrayList(); for(int col= 0; col <
	 * tfm.getColumnDataCount(); col++){
	 * CurrentRowData.add(tfm.getStructuredDataAt(rows[row],col)); }
	 * result.add(CurrentRowData); } return result; }
	 */
	public void clearSeleccion() {
		m_objTable.clearSelection();
	}

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
			m_server.logError(SwingUtilities.getWindowAncestor(m_objTable),ex,"Error al intentar mostrar/ocultar la botonera");
		}
	}

	public void mouseClicked(MouseEvent e) {
		try{
			//System.err.println("MOUSE CLICK");
			m_menuBotonera.setVisible(false);
		}catch(Exception ex){
			ex.printStackTrace();
			m_server.logError(SwingUtilities.getWindowAncestor(m_objTable),ex,"Error al intentar ocultar la botonera");
		}
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {
		try{
			//System.err.println("MOUSE EXIT");
			m_menuBotonera.setVisible(false);
			clearSeleccion();
		}catch(Exception ex){
			ex.printStackTrace();
			m_server.logError(SwingUtilities.getWindowAncestor(m_objTable),ex,"Error al intentar ocultar la botonera");
		}
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public String getId() {
		return m_id;
	}

	public IDictionaryFinder getDictionaryFinder() {
		return m_dictionaryFinder;
	}

	public boolean hasCreationRow() {
		return m_creationRow;
	}

	public boolean hasFinderRow() {
		return m_finderRow;
	}

	public boolean hasNullRow() {
		return m_nullRow;
	}

	public docServer getServer() {
		return m_server;
	}
	
	//Utilizado cuando no se quiere que cuente la nullRow. Se podria hacer mirando el ultimo registro ya que la nullRow esta al final, pero no es seguro que no funcione bien en algun caso
	public int getRowCount(){
		GTableModel tfm = (GTableModel) m_objTable.getModel();
		int size=0;
		Iterator<RowItem> itr=tfm.getRowData().iterator();
		while(itr.hasNext()){
			RowItem ri=itr.next();
			if(!ri.isNullRow())
				size++;
		}
		return size;
	}
	
	public boolean isNullRow(int index){
		return m_objTableModel.getRowData().get(index).isNullRow();
	}

	public boolean isTopLabel() {
		return m_topLabel;
	}

	public double getHeightRow() {
		return m_heightRow;
	}

	public AccessAdapter getOperations() {
		return operations;
	}

	public String getLabel() {
		return m_label;
	}

	@Override
	public boolean newValueAllowed() {
		//Permitimos si es una tabla de una sola fila vacia, o una de varias filas aunque tenga algun valor
		return m_topLabel || getRowCount()==0;
	}

	@Override
	public synchronized void addFocusListener(FocusListener e) {
		focusListenerExt.add(e);
	}
	
	public void notifyFocusListener(FocusEvent e,boolean lost){
		Iterator<FocusListener> itr=focusListenerExt.iterator();
		while(itr.hasNext()){
			if(lost)
				itr.next().focusLost(e);
			else itr.next().focusGained(e);
		}
	}

	public IUserMessageListener getMessageListener() {
		return m_messageListener;
	}

	public Object getValues() {
		String values=null;
		int numRow=getRowCount();
		if(numRow>0)
			values=getData(getDataFromIndex(0).getIdo()).toString();
		for(int i=1;i<numRow;i++){
			values+=":"+getData(getDataFromIndex(i).getIdo()).toString();
		}
		return values;
	}

	public boolean isFocusTraversalManagingError() {
		return focusTraversalManagingError;
	}

	//Solo debe ser llamado por GFocusTraversalPolicy para indicar que esta procesando el error
	//y asi evitar que se requiera el foco por otros componentes
	public void setFocusTraversalManagingError(boolean enabledShortCut) {
		this.focusTraversalManagingError = enabledShortCut;
	}

	public boolean isProcessingPasteRows() {
		return processingPasteRows;
	}

	public void setAbortPasteRows(boolean abortPasteRows) {
		this.abortPasteRows = abortPasteRows;
	}

	public boolean isProcessingSelectCellFromSystem() {
		return processingSelectCellFromSystem;
	}

	public boolean isNullable() {
		return m_nullable;
	}
}
