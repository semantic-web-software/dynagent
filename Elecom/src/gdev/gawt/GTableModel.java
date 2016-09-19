package gdev.gawt;

import gdev.gawt.tableCellEditor.CellEditor;
import gdev.gawt.tableCellRenderer.TextCellRenderer;
import gdev.gawt.utils.ItemList;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;
import gdev.gen.GConst;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;
import gdev.gfld.GFormTable;
import gdev.gfld.GTableColumn;
import gdev.gfld.GTableRow;
import gdev.gfld.GValue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.muntjak.tinylookandfeel.table.SortableTableData;
import dynagent.common.Constants;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.RowItem;

/*
 * 
 * public class GTableModel extends DefaultTableModel//for the time being it is
 * derived from this, in final version it will be derived from TableModel or
 * AbstractTableModel { }
 */

public class GTableModel extends /* AbstractTableModel */DefaultTableModel implements SortableTableData/*,FocusListener*/{

	private static final long serialVersionUID = 1L;

	ArrayList<String> columnNames = new ArrayList<String>();

	ArrayList<RowItem> m_rowData = new ArrayList<RowItem>();

	ArrayList<Integer> m_visibleRowMap = new ArrayList<Integer>();

	ArrayList<Integer> m_visibleColMap = new ArrayList<Integer>();

	ArrayList<Integer> m_visibleDataColMap = new ArrayList<Integer>();

	public boolean m_cuantitativo = false;

	int m_atGroupTypeColumn = -1;

	int m_iniVirtualColumn;

	// estos dos valores no contabilizan la col de toggleBoton pra desplegar
	ArrayList<Integer> m_groupByColumns = new ArrayList<Integer>();

	ArrayList<String> columnRef = new ArrayList<String>();

	ArrayList<Integer> columnIdProps = new ArrayList<Integer>();

	ArrayList<Integer> columnSintax = new ArrayList<Integer>();

	ArrayList<Class> columnClass = new ArrayList<Class>();

	ArrayList m_idPropModel;// no contabiliza la primera columna de boton
	// desplegable en cuantitativos

	HashMap<Integer, Integer> Map_columnsIdProp = new HashMap<Integer, Integer>();

	HashMap<String, Integer> Map_IDForm_Column = new HashMap<String, Integer>();
	
	HashMap<String, Integer> Map_IDO_Label = new HashMap<String, Integer>();

	ArrayList<String> Map_Column_IDForm = new ArrayList<String>();
	
	HashSet<Integer> columnsFinder = new HashSet<Integer>();
	
	HashSet<Integer> columnsCreation = new HashSet<Integer>();
	
	HashSet<Integer> columnsFinderIfCreation = new HashSet<Integer>();

	public HashMap Map_XMLDom_ListOption = new HashMap();

	ArrayList<Boolean> columnEditable = new ArrayList<Boolean>();//Columnas que, aunque a lo mejor se tiene permiso de edicion, no se le deja al usuario modificarlo en la tabla
	
	HashSet<Integer> columnsEnable = new HashSet<Integer>();//Columnas sin permisos de edicion para el usuario

	HashMap m_condicionantes_List;

	boolean m_filteredState = false;

	/* JTable parent; */
	GTable parent;

	ArrayList m_colTotales;

	ArrayList m_colAgrupables;

	boolean dobleHeaderSize = false;

	/*
	 * domDataModel m_domModel; docDataModel m_rootDoc;
	 */
	/* metaData m_md; */
	HashMap m_moa;

	int m_action = 0;

	//communicator m_com;

	/*session m_session = null;*/

	private boolean m_modoConsulta;
	
	private boolean m_modoFilter;

	private IComponentListener m_controlListener;
	
	private Vector<GTableRow> listaFilas;
	
	private boolean m_creationRow;
	
	private boolean m_finderRow;
	
	private String m_id;
	
	private boolean init;
	
	private Integer idoRowEditing;
	
	private boolean rowCreating;
	
	private HashSet<Integer> listIdosEditing;
	
	private boolean directEdition;
	
	private GFormTable m_ff;
	
	private Integer columnSelectionRowTable;//Nos sirve para saber si hay alguna columna de seleccion de fila y cual es

	private boolean lastSetValueSuccess=true;//Nos indica si el ultimo setValueAt ha tenido exito o ha provocado alguna excepcion

	private boolean executingSetValue=false;//Nos indica si se esta ejecutando el setValueAt para que no ejecutar cada valor de la copia masiva en GTable hasta que setValueAt ha terminado con el valor anterior 
	
	public GTableModel(	String id, GFormTable ff, IComponentListener controlListener,
			/*communicator com,*/ int action,Vector listaColumnas,Vector<GTableRow> listaFilas, boolean cuantitativo,
			int iniVirtColumn,int atGroupColum, ArrayList totalColumns, ArrayList agrupables,
			boolean modoConsulta, boolean modoFilter, boolean creationRow, boolean finderRow, boolean topLabel) throws AssignValueException {
		m_controlListener = controlListener;
		//m_com = com;
		m_ff = ff;
		m_action = action;
		m_modoConsulta = modoConsulta;
		m_modoFilter = modoFilter;
		m_cuantitativo = cuantitativo;
		m_iniVirtualColumn = iniVirtColumn + (m_cuantitativo ? 1 : 0);
		m_atGroupTypeColumn = atGroupColum + (m_cuantitativo ? 1 : 0);
		m_colTotales = totalColumns;
		m_colAgrupables = agrupables;
		/* BuildModel(dataModel); */
		this.listaFilas=listaFilas;
		this.m_creationRow=creationRow;
		this.m_finderRow=finderRow;
		m_id=id;
		setIdoRowEditing(null);
		init=true;
		buildTabla(listaColumnas,topLabel);
		init=false;
		directEdition=false;
	}

	public int getFieldColumn(String field) {
		Integer col = (Integer) Map_IDForm_Column.get(field);
		if (col == null)
			return -1;
		return col.intValue();
	}

	public String getFieldIDFromColumn(int column) {
		return (String) Map_Column_IDForm.get(column);
	}

	public ArrayList<String> getIdColumns(){
		return Map_Column_IDForm;
	}

	public int getRealDataColumn(int visCol) {
		Integer col = (Integer) m_visibleDataColMap.get(visCol);
		if (col == null)
			return -1;
		return col.intValue();
	}

	public int getRealColumn(int visCol) {
		Integer col = (Integer) m_visibleColMap.get(visCol);
		if (col == null)
			return -1;
		return col.intValue();
	}

	public int getColumnDataCount() {
		return columnSintax.size();
	}

	public int getVisibleColumnDataCount() {
		return m_visibleDataColMap.size();
	}

	public void setTable(GTable table) {
		parent = table;
		
//		parent.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){
//
//			public void valueChanged(ListSelectionEvent e) {
//				//System.err.println("ListSelectionEvent GTableModel siendo listS.getMinSelectionIndex():"+((ListSelectionModel)e.getSource()).getMinSelectionIndex()+" idoRowEditing"+idoRowEditing);
//				ListSelectionModel listS=(ListSelectionModel)e.getSource();
//
////				if(!listS.isSelectionEmpty()){
////					//System.err.println("Indice Seleccionado:"+listS.getMinSelectionIndex());
////					if(listS.getMinSelectionIndex()==listS.getMaxSelectionIndex()){//Seleccionada una sola fila
////						System.err.println("- Seleccionada una sola fila");
////						int idoRowToEdit=getDataFromIndex(listS.getMinSelectionIndex()).getIDO();
////						System.err.println("idoRowToEdit:"+idoRowToEdit+" idoRowEditing:"+idoRowEditing);
////						if(idoRowEditing==null || idoRowEditing!=idoRowToEdit){//Fila seleccionada no es la misma que la que ya estamos editando
////							parent.getTable().removeEditor();
////						}
////					}
////				}
//				
//				
//				if(idoRowEditing!=null && m_controlListener!=null){//Estabamos editando una fila
//					int idoRowActual=idoRowEditing;
//					try{
//						
//						if(!listS.isSelectionEmpty()){
//							//System.err.println("Indice Seleccionado:"+listS.getMinSelectionIndex());
//							if(listS.getMinSelectionIndex()==listS.getMaxSelectionIndex()){//Seleccionada una sola fila
//								//System.err.println("- Seleccionada una sola fila");
//								int idoRowToEdit=getDataFromIndex(listS.getMinSelectionIndex()).getIDO();
//								//System.err.println("idoRowToEdit:"+idoRowToEdit+" idoRowEditing:"+idoRowEditing);
//								if(/*idoRowEditing==null || */(/*idoRowEditing!=null &&*/ idoRowEditing!=idoRowToEdit)){//Fila seleccionada no es la misma que la que ya estamos editando
//									int rowToEdit=listS.getMinSelectionIndex();
//									idoRowEditing=null;
//									//parent.getTable().removeEditor();
//									m_controlListener.stopEditionTable(parent.getId(),idoRowActual);
//								}
//							}else{//Seleccionadas varias filas
//								idoRowEditing=null;
//								//parent.getTable().removeEditor();
//								m_controlListener.stopEditionTable(parent.getId(),idoRowActual);
//							}
//								
//						}else{//Ninguna fila seleccionada
//							//System.err.println("- Ninguna fila seleccionada");
////
////								//Component componentFocus=SwingUtilities.getWindowAncestor(parent).getFocusOwner();
////								//System.err.println("Tiene el focus:"+(componentFocus!=null?componentFocus.getClass():null));
////								//if(componentFocus!=null && !parent.getTable().isAncestorOf(componentFocus)){//Si el foco ya no esta en la tabla
////									System.err.println("El foco ya no esta en la tabla");
////									
////									idoRowEditing=null;
////									//parent.getTable().removeEditor();
////									m_controlListener.stopEditionTable(parent.getId(),idoRowActual);
////									
////									//isNullRowEditing=false;
////								//}else{
////								//	System.err.println("El foco sigue en la tabla");
////								//}
////							//}
//						}
//					}catch (EditionTableException ex){
//						editionError(ex.getUserMessage(),idoRowActual);
//					}
//				}
//			}
//		});
		
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener(new VetoableChangeListener(){
//			private Component lostComponentTable=null;
//			private Component vetoedComponent=null;
//			public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
//				if(e.getPropertyName().equals("permanentFocusOwner")){
//					if(parent.getLabel()!=null && parent.getLabel().equalsIgnoreCase("Línea")){
//						System.err.println("-----INICIO----");
//						System.err.println("oldValue:"+e.getOldValue());
//						System.err.println("NewValue:"+e.getNewValue());
//						System.err.println("-------FIN------");
//					}
//					if(e.getOldValue()!=null){
//						if(e.getOldValue()==parent.getTable() || parent.getTable().isAncestorOf((Component)e.getOldValue())){
//							System.err.println("--------PIERDE FOCO TABLA O CELL-------- la tabla:"+(parent.getLabel()!=null?parent.getLabel():null)+" oldValue:"+((Component)e.getOldValue()).getClass());
//							lostComponentTable=(Component)e.getOldValue();
//						}else{
//							vetoedComponent=null;
//						}
//					}
//					
//	//					if(e.getNewValue()!=null && e.getNewValue()==parent.getTable()){
//	//						System.err.println("--------GANA FOCO TABLA--------"+parent.getTable().hashCode());
//	//						lostTable=false;
//	//					}
//	//					
//	//					if(lostTable & e.getNewValue()!=null){
//	//						System.err.println("--------GANA FOCO--------"+e.getNewValue().getClass());
//	//						System.err.println("---ISANCESTOR:"+parent.getTable().isAncestorOf((Component)e.getNewValue()));
//	//						System.err.println("---ISPARENT:"+(((Component)e.getNewValue()).getParent()==parent.getTable()));
//	//					}
//					
//					if(e.getNewValue()!=null){
//						/*if(vetoedComponent!=null && vetoedComponent==((Component)e.getNewValue()).getParent()){
//							System.err.println("-----------VETOABLE hijo-----------por table:"+(parent.getLabel()!=null?parent.getLabel():null)+" siendo newValue:"+e.getNewValue());
//							vetoedComponent=(Component)e.getNewValue();
//							int ido=findRow(idoRowEditing,false);
//							parent.getTable().removeEditor();//Con esto nos aseguramos que si entro a algun editor de otra fila salga
//							boolean exito=parent.getTable().requestFocusInWindow();
//							System.err.println("************************************************vetoableChange HIJO exito:"+exito);
//							parent.getTable().setRowSelectionInterval(ido, ido);
//							throw new PropertyVetoException("NO SE PUEDE SALIR",e);
//						}else */if(lostComponentTable!=null){
//								if(!parent.getTable().isAncestorOf((Component)e.getNewValue()) && parent.getTable()!=(Component)e.getNewValue()){
//									if(idoRowEditing!=null && m_controlListener!=null){//Estabamos editando una fila
//										Window newWindow = SwingUtilities.getWindowAncestor((Component)e.getNewValue());
//										if(newWindow!=null && newWindow.equals(parent.window)){
//	
//											System.err.println("-----------VETOABLE-----------por table:"+(parent.getLabel()!=null?parent.getLabel():null)+" siendo newValue:"+e.getNewValue());
//											final int idoRowActual=idoRowEditing;
//											
//											try{
//											
//												Component componentFocus=(Component)e.getNewValue();//KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
//												if(!parent.getTable().isAncestorOf(componentFocus)/* && parent.isAncestorOf(componentFocus)*/){//Si el foco ya no esta en la tabla pero esta en la botonera
//													//System.err.println("El foco ya no esta en la tabla");
//													boolean stopEdition=true;
//													//Si se trata de la botonera de la tabla permitimos que se pulse el boton de editar
//													if(parent.isTopLabel() && parent.isAncestorOf(componentFocus) && componentFocus instanceof JButton){
//														String command=((JButton)componentFocus).getActionCommand();
//														
//														IdOperationForm idOperation=new IdOperationForm(command);
//														if(idOperation.getButtonType().equals(botoneraAccion.EDITAR)/* || idOperation.getButtonType().equals(botoneraAccion.ELIMINAR)*/){
//															stopEdition=false;
//														}
//													}
//													if(stopEdition){
//														idoRowEditing=null;
//														m_controlListener.stopEditionTable(parent.getId(),idoRowActual);
//													}
//												}/*else{
//													System.err.println("El foco no esta en la botonera");
//												}*/
//											}catch (final EditionTableException ex){
//												if(ex.isNotify())
//													parent.getServer().logError(SwingUtilities.getWindowAncestor(this),ex, null);
//												final Runnable doFinished = new Runnable() {
//													public void run() {
//														editionError(ex.getUserMessage(),idoRowActual);
//													}
//												};
//												SwingUtilities.invokeLater(doFinished);
//												//lostComponentTable.requestFocusInWindow();
//												vetoedComponent=(Component)e.getNewValue();
//												/*if(vetodComponent instanceof JTable){
//													((JTable)vetodComponent).removeEditor();
//												}*/
//												throw new PropertyVetoException("NO SE PUEDE SALIR",e);
//												
//	//											if(editionError(ex.getUserMessage(),idoRowActual)){
//	//												vetoedComponent=(Component)e.getNewValue();
//	//												throw new PropertyVetoException("NO SE PUEDE SALIR",e);
//	//											}
//												
//											}
//										}
//										System.err.println("-----------------------------");
//									}
//								}
//								lostComponentTable=null;
//						}
//					}
//				}
//			}		
//		});
		
		//Si tiene columna de selección de fila nos registramos en el listener para que al seleccionar alguna otra fila pulsando sobre ella
		//seleccionar las filas que tienen marcado el checkbox. De esta manera evitamos la deseleccion que se produce cuando se hace click en otra fila sin pulsa control
		if(columnSelectionRowTable!=null){
			parent.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){
		    	//Gestiona cuando hay un cambio de fila en la tabla
				public void valueChanged(ListSelectionEvent e) {
					//System.err.println("ListSelectionEvent GTableModel siendo listS.getMinSelectionIndex():"+((ListSelectionModel)e.getSource()).getMinSelectionIndex()+" idoRowEditing"+gTable.getModel().getIdoRowEditing());
					ListSelectionModel listS=(ListSelectionModel)e.getSource();
					int num=0;
					for(int i=0;i<m_rowData.size();i++){
						RowItem rowItem=m_rowData.get(i);
						Object value=(Boolean)rowItem.getColumnData(columnSelectionRowTable);
						if(Auxiliar.equals(value, true)){
							if(!listS.isSelectedIndex(i)){
								listS.addSelectionInterval(i, i);
							}
							num++;
						}
					}
					if(num>0 && num==m_rowData.size()){
						TableColumn tc = getColumnModel().getColumn(columnSelectionRowTable);
						tc.setHeaderValue(true);
						parent.getTable().getTableHeader().repaint();
					}
					
				}
			});
		}
		
		if(!m_modoConsulta){
			parent.getTable().addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent ev) {
					if(ev.getKeyCode()==KeyEvent.VK_DELETE){
						//parent.getTable().removeEditor();
						if((!parent.getOperations().getUserTasksAccess(AccessAdapter.DEL_AND_UNREL).isEmpty()) || !parent.getOperations().getUserTasksAccess(AccessAdapter.UNREL).isEmpty()){
							RowItem it = (RowItem) m_rowData.get(getRowIndex(parent.getTable().getSelectedRow()));
							int[] rows=parent.getTable().getSelectedRows();
							if(!it.isNullRow() || rows.length>1){//Esta comprobacion es por si solo tenemos seleccionada la nullRow, en cuyo caso no hariamos nada
								Integer oldIdoRowEditing=getIdoRowEditing();
								try{
									if(m_controlListener!=null){
										int size=rows.length;
										String textNumElementos="el elemento";
										if (size > 1)
											textNumElementos="los "+size+" elementos";
										Object[] options = {"Sí", "No"};
										String action=!parent.getOperations().getUserTasksAccess(AccessAdapter.UNREL).isEmpty()?"desvincular":"borrar";
										String actionWindow=!parent.getOperations().getUserTasksAccess(AccessAdapter.UNREL).isEmpty()?"DESVINCULACIÓN":"BORRADO";
										int res = parent.getMessageListener().showOptionMessage(
												"¿Está seguro que desea "+action+" "+textNumElementos+"?",
												"CONFIRMACIÓN DE "+actionWindow,
												JOptionPane.YES_NO_OPTION,
												JOptionPane.WARNING_MESSAGE,
												null,
												options,
												options[1],SwingUtilities.getWindowAncestor(parent.getTable()));
			
										if (res != JOptionPane.YES_OPTION)return;
										HashMap<Integer,Integer> idosRowsToDelete=new HashMap<Integer,Integer>();
										for(int i=0;i<size;i++){
											int row=rows[i];
											RowItem rItem=m_rowData.get(getRowIndex(row));
											if(!rItem.isNullRow()){//Esta comprobacion es por si hemos seleccionado varias filas y entre ellas la nullRow
												int idoRow=rItem.getIdRow().getIdo();
												int idtoRow=rItem.getIdRow().getIdto();
												if(getIdoRowEditing()!=null && getIdoRowEditing().equals(idoRow)){
													setIdoRowEditing(null);
													m_controlListener.cancelEditionTable(parent.getId(), idoRow);
												}
												
												if(findRow(idoRow, false, rItem.isPermanent())!=-1)//Si fuera -1 significaria que el cancelEditionTable anterior lo ha eliminado
													idosRowsToDelete.put(idoRow,idtoRow);
											}
										}
										
										Iterator<Integer> itr=idosRowsToDelete.keySet().iterator();
										while(itr.hasNext()){
											int idoRow=itr.next();
											m_controlListener.removeRowTable(parent.getId(), idoRow, idosRowsToDelete.get(idoRow));
										}
										
									}
								} catch (EditionTableException exc) {
									setIdoRowEditing(oldIdoRowEditing);
									parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),exc,"Error al intentar cancelar la edición de la fila");
								}catch(AssignValueException ex){
									 parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),ex,"Error al intentar borrar la fila");
								 }catch(NotValidValueException ex){
									 parent.getMessageListener().showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(parent.getTable()));
									 ex.printStackTrace();
								 }
							}/*else{
								final Runnable doRemoveEditor = new Runnable() {
									public void run() {
										parent.getTable().removeEditor();
									}
								};
								SwingUtilities.invokeLater(doRemoveEditor);
							}*/
						}
					}
				}
			});
		}
//		InputMap im = parent.getTable().getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//		KeyStroke supr = KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0);
//        
//		Action tabSupr = new AbstractAction(){
//
//			private static final long serialVersionUID = 1L;
//			public void actionPerformed(ActionEvent e){
//				System.err.println("ActionPerformedddddddddddddd");
//				Integer oldIdoRowEditing=idoRowEditing;
//				try{
//					if(m_controlListener!=null){
//						int[] rows=parent.getTable().getSelectedRows();
//						int size=rows.length;
//						ArrayList<Integer> idosRowsToDelete=new ArrayList<Integer>();
//						for(int i=0;i<size;i++){
//							int row=rows[i];
//							RowItem rItem=m_rowData.get(getRowIndex(row));
//							if(!rItem.isNullRow()){
//								int idoRow=rItem.getSourceData().getIDO();
//								if(idoRowEditing!=null && idoRowEditing.equals(idoRow)){
//									idoRowEditing=null;
//									m_controlListener.cancelEditionTable(parent.getId(), idoRow);
//								}
//								
//								if(findRow(idoRow, false)!=-1)//Si fuera -1 significaria que el cancelEditionTable anterior lo ha eliminado
//									idosRowsToDelete.add(idoRow);
//							}
//						}
//						
//						Iterator<Integer> itr=idosRowsToDelete.iterator();
//						while(itr.hasNext()){
//							int idoRow=itr.next();
//							m_controlListener.removeRowTable(parent.getId(), idoRow);
//						}
//						
//					}
//				} catch (EditionTableException exc) {
//					idoRowEditing=oldIdoRowEditing;
//					parent.getServer().logError(SwingUtilities.getWindowAncestor(this),exc,"Error al intentar cancelar la edición de la fila");
//				}catch(AssignValueException ex){
//					 parent.getServer().logError(SwingUtilities.getWindowAncestor(this),ex,"Error al intentar borrar la fila");
//				 }catch(NotValidValueException ex){
//					 parent.getMessageListener().showErrorMessage(ex.getUserMessage());
//					 ex.printStackTrace();
//				 }
//			}
//		};
//		parent.getTable().getActionMap().put(im.get(supr), tabSupr);
		
		//Necesitamos escuchar la perdida del foco SOLAMENTE para cuando pulsamos sobre los botones de la tabla. Si hacemos algo en otros casos se repetiria con el caso de ninguna fila seleccionada de ListSelectionListener
		//parent.getTable().addFocusListener(this);
		
		/* if( ((TableForm)table).hideHeader ) return; */
		if (table.hideHeader)
			return;
		if (dobleHeaderSize) {
			// System.out.println("DOBLESIZE");
			JTableHeader jt = parent.getTable().getTableHeader();
			TableCellRenderer tcr = jt.getDefaultRenderer();
			for (int i = 0; i < getColumnCount(); i++) {
				JComponent comp = (JComponent) tcr
				.getTableCellRendererComponent(parent.getTable(),
						parent, false, false, -1, i);
				Dimension d = comp.getSize();
				comp.setMinimumSize(new Dimension((int) d.getWidth(), (int) (d.getHeight() + 40)));
				comp.setPreferredSize(new Dimension((int) d.getWidth(), (int) (d.getHeight() + 40)));
			}
		}
	}
	

//	public void focusGained(FocusEvent ev){
//		//System.err.println("FocusGained GTableModel component:"+(ev.getComponent()!=null?ev.getComponent().getClass()+" "+ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass()+" "+ev.getOppositeComponent().hashCode():null));
//	}
//	public void focusLost(FocusEvent ev){
//		System.err.println("FocusLost GTableModel component:"+(ev.getComponent()!=null?ev.getComponent().getClass()+" "+ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass()+" "+ev.getOppositeComponent().hashCode():null));
//		if(idoRowEditing!=null && m_controlListener!=null){//Estabamos editando una fila
//			int idoRowActual=idoRowEditing;
//			if(!ev.isTemporary() && ev.getOppositeComponent()!=null && ev.getSource()!=null){
//				try{
//					Component componentFocus=ev.getOppositeComponent();//window.getFocusOwner();
//					if(componentFocus instanceof JButton){
//						System.err.println("Texto boton:"+((JButton)componentFocus).getText());
//					}
////					System.err.println("Tiene el focus:"+(componentFocus!=null?componentFocus.getClass():null));
////					System.err.println("FocusLost gain "+ev.getOppositeComponent().getClass()+" lost "+ev.getComponent().getClass());
//					//System.err.println("Request "+ev.getOppositeComponent().getClass());
////					System.err.println("Id parent:+"+parent.hashCode()+" parent.getTable():"+parent.getTable().hashCode()+"+component"+(ev.getComponent()!=null?ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().hashCode():null));
////					System.err.println("parent.getTable().isAncestorOf(componentFocus):"+parent.getTable().isAncestorOf(ev.getOppositeComponent()));
////					System.err.println("parent.isAncestorOf(componentFocus):"+parent.isAncestorOf(ev.getOppositeComponent()));
////					System.err.println("component.isAncestorOf(componentFocus):"+parent.isAncestorOf(ev.getOppositeComponent()));
//
//					if(/*parent.isAncestorOf(componentFocus)*/parent.getTable()!=componentFocus && !parent.getTable().isAncestorOf(componentFocus)/* && parent.isAncestorOf(componentFocus)*/){//Si el foco ya no esta en la tabla pero esta en la botonera
//						//System.err.println("El foco ya no esta en la tabla");
//						boolean stopEdition=true;
//						//Si se trata de la botonera de la tabla permitimos que se pulse el boton de editar
//						if(parent.isTopLabel() && parent.isAncestorOf(componentFocus) && componentFocus instanceof JButton){
//							String command=((JButton)componentFocus).getActionCommand();
//							
//							IdOperationForm idOperation=new IdOperationForm(command);
//							if(idOperation.getButtonType().equals(botoneraAccion.EDITAR)/* || idOperation.getButtonType().equals(botoneraAccion.ELIMINAR)*/){
//								stopEdition=false;
//							}
//						}
//						if(stopEdition){
//							idoRowEditing=null;
//							m_controlListener.stopEditionTable(parent.getId(),idoRowActual);
//						}
//					}/*else{
//						System.err.println("El foco no esta en la botonera");
//					}*/
//				}catch (EditionTableException ex){
//					if(ex.isNotify())
//						parent.getServer().logError(SwingUtilities.getWindowAncestor(this),ex, null);
//					editionError(ex.getUserMessage(),idoRowActual);
//				}
//			}
//		}
//	}
//	
//	private boolean editionError(String message,final int idoRow){
//		
//		//TODO Estamos haciendo 3 run con invokeLater debido a que si se ha perdido el foco con la fila de
//		//otra tabla, al recuperarlo nos lo vuelve a quitar el cellEditor de esa tabla. Habria que investigar
//		//si se podria leer del EventQueue si quedan mas eventos que procesar para saber cuantos invokeLater
//		//tendriamos que hacer
////		final Runnable doFinished2 = new Runnable() {
////			public void run() {
////				System.err.println("Reseleccionaaa "+parent.getTable().hashCode());
////				idoRowEditing=idoRow;
////				int ido=findRow(idoRowEditing,false);
////				parent.getTable().removeEditor();//Con esto nos aseguramos que si entro a algun editor de otra fila salga
////				boolean exito=parent.getTable().requestFocusInWindow();
////				System.err.println("************************************************editionError exito:"+exito);
////				parent.getTable().setRowSelectionInterval(ido, ido);
////			}
////		};
////		
////		final Runnable doFinished1 = new Runnable() {
////			public void run() {
////				SwingUtilities.invokeLater(doFinished2);
////			}
////		};
////		
////		final Runnable doFinished = new Runnable() {
////			public void run() {
////				SwingUtilities.invokeLater(doFinished1);
////			}
////		};
//		
//		Object[] options = {"Editar en tabla", "Editar en formulario", "Cancelar cambios"};
//		
//		String rdnIdo=null;
//		instance inst=parent.getDataFromIdo(idoRow);
//		if(inst!=null)
//			rdnIdo=inst.getRdn();
//		
//		int res = parent.getMessageListener().showOptionMessage(
//				message+"\nPara poder continuar debe modificar el registro, ¿que desea hacer?",
//				"REGISTRO ERRÓNEO: "+parent.getLabel()+(rdnIdo!=null?" '"+parent.getDataFromIdo(idoRow).getRdn()+"'":""),
//				JOptionPane.YES_NO_CANCEL_OPTION,
//				JOptionPane.WARNING_MESSAGE,
//				null,
//				options,
//				options[0]);
//
//		if (res == JOptionPane.CANCEL_OPTION){
//			try{
//				parent.getTable().removeEditor();
//				m_controlListener.cancelEditionTable(parent.getId(), idoRow);
//				return false;
//			} catch (EditionTableException exc) {
//				parent.getServer().logError(SwingUtilities.getWindowAncestor(this),exc, "Error al intentar cancelar edición de la fila");
//			}
//		}else if(res == JOptionPane.NO_OPTION){
//			try{
//				//parent.getTable().removeEditor();
//				m_controlListener.editRowTableInForm(parent.getId(), idoRow);
//			} catch (EditionTableException exc) {
//				parent.getServer().logError(SwingUtilities.getWindowAncestor(this),exc, "Error al intentar editar fila en formulario");
//				return true;
//			} finally{
//				idoRowEditing=idoRow;
//				reSelectEdition(idoRow);
//			}
//		}else{
//			idoRowEditing=idoRow;
//			reSelectEdition(idoRow);
//			return true;
//		}
//		
//		return true;
//	}
//	
//	public void reSelectEdition(final int idoRow){
//		final Runnable reselect = new Runnable() {
//			public void run() {
//				reSelectEdition(idoRow);
//			}
//		};
//		// Esperamos a que la ventana este activa ya que si no esta activa al activarse GFocusTraversalPolicy le da el foco
//		// al componente que lo tenia antes de desactivarse por lo que no nos serviria de nada pedir aqui el foco.
//		// Esto ocurre cada vez que sale un mensajito o un nuevo formulario.
//		if(parent.window.isActive()){
//			//idoRowEditing=idoRow;
//			int row=findRow(idoRowEditing,false);
//			parent.getTable().removeEditor();//Con esto nos aseguramos que si entro a algun editor de otra fila salga
//			boolean exito=parent.getTable().requestFocusInWindow();
//			System.err.println("************************************************editionError exito:"+exito);
//			parent.getTable().setRowSelectionInterval(row, row);
//		}else{
//			SwingUtilities.invokeLater(reselect);
//		}
//	}

	private void buildTabla(Vector listaColumnas,boolean topLabel) throws AssignValueException{
		/* Iterator iCol= dataModel.iterator(); */
		Iterator iCol = listaColumnas.iterator();
		int size = 0;

		if (m_cuantitativo) {
			columnNames.add(" ");
			//columnClass.add(Class.forName("javax.swing.JToggleButton"));
			/* columnEditable.add(new Boolean("true")); */
			columnEditable.add(new Boolean(!m_modoConsulta && !m_modoFilter));
		}
		int col = 0;
		while (iCol.hasNext()) {
			GTableColumn item = (GTableColumn) iCol.next();
			/* org.jdom.Element item= (org.jdom.Element)iCol.next(); */

			String label = item.getLabel();
			/*
			 * String label=item.getAttributeValue("LABEL");
			 * System.out.println("COL "+label+","+m_cuantitativo);
			 */
			int posToken = label.indexOf(":");
			if (posToken != -1) {
				label = "<HTML><TABLE cellpadding=0 vspace=0 cellspacing=0><TR><TC>"
					+ label.substring(0, posToken)
					+ "</TC></TR>"
					+ "<TR><TC>"
					+ label.substring(posToken + 1)
					+ "</TC></TR>" + "</TABLE></HTML>";
				dobleHeaderSize = true;
			}
			boolean visible = !item.isHide();
			/*
			 * boolean visible= !(item.getAttributeValue("HIDE")!=null &&
			 * item.getAttributeValue("HIDE").equals("TRUE"));
			 */

			if (visible)
				columnNames.add(label);

			String ID = item.getId();
			/* String ID= item.getAttributeValue("ID"); */

			Map_IDForm_Column.put(ID, new Integer(col));
			Map_Column_IDForm.add(ID);

			Integer idProp = item.getIdProp();
			
			if(item.hasFinder()){
				columnsFinder.add(col);
				if(item.getTypeFinder()==GTableColumn.CREATION_FINDER)
					columnsFinderIfCreation.add(col);
			}
			
			if(item.hasCreation())
				columnsCreation.add(col);
			/*
			 * Integer tapos= item.getAttributeValue("TA_POS")==null ? null:
			 * new Integer(item.getAttributeValue("TA_POS"));
			 */
			/* int tm= tapos==null ? -1:m_md.getID_TM( tapos ); */
			int tm = item.getType();
			if (idProp != null) {
				columnSintax.add(new Integer(tm));
				columnIdProps.add(idProp);
				Map_columnsIdProp.put(idProp, col);
				columnRef.add( /* new Integer( */item.getRef()/* ) */);
				/* columnRef.add( new Integer( item.getAttributeValue("REF") ) ); */
				if (visible)
					m_visibleDataColMap.add(new Integer(col));
			} else if (visible)
				m_visibleDataColMap.add(null);

			if (visible)
				m_visibleColMap.add(new Integer(col));
//			String clase = "javax.swing.JButton";
//			switch (tm) {
//			case GConst.TM_ENUMERATED:
//				clase = "javax.swing.JComboBox";
//				break;
//			case GConst.TM_INTEGER:
//				clase = "java.lang.Integer";
//				break;
//			case GConst.TM_REAL:
//				clase = "java.lang.Double";
//				break;
//			case GConst.TM_TEXT:
//				clase = "java.lang.String";
//				break;
//			case GConst.TM_DATE:
//				clase = "java.lang.String";
//				break;
//			case GConst.TM_DATE_HOUR:
//				clase = "java.lang.String";
//				break;
//			case GConst.TM_BOOLEAN:
//				clase = "java.lang.Boolean";
//				break;
//			case GConst.TM_BOOLEAN_COMMENTED:
//				clase = "java.lang.Boolean";
//				break;
//			}
//			if (visible)
//				columnClass.add(Class.forName(clase));

			/* boolean editable=item.isEnable(); */
			
			if(ID.equals(GConst.ID_COLUMN_TABLE_SELECTION) && tm==GConst.TM_BOOLEAN)
				columnSelectionRowTable=col;
			
			boolean enable = (item.isEnable() && (!m_modoConsulta && !m_modoFilter) || /*clase
					.equals("javax.swing.JButton")*/tm==GConst.TM_BUTTON/*Para favoritos*/ || Auxiliar.equals(col,columnSelectionRowTable));
			if(enable)
				columnsEnable.add(col);
			
			//System.err.println("label:"+label+" enable:"+enable);
			boolean editable = enable && item.isBasicEdition();
			columnEditable.add(new Boolean(editable));
			
			/*
			 * String editable= item.getAttributeValue("ENABLE");
			 * if(editable!=null && editable.equals("TRUE"))
			 * columnEditable.add(new Boolean("true")); else
			 * columnEditable.add(new Boolean("false"));
			 */
			size++;
			col++;
		}
		buildRows(listaFilas,false);
		
		if((m_creationRow || m_finderRow) && !m_modoConsulta){
			if(topLabel || !topLabel && listaFilas.isEmpty())//Si es una tabla de una sola fila y ya tiene algun valor no ponemos la nullRow
				addNullRow();
		}
			
		updateColumnWidths();
	}

	/**
	 * Añade filas a la tabla pudiendo ser un vector de GTableRow o de RowItem
	 * @param rows
	 * @param replace
	 * @throws AssignValueException
	 */
	public void buildRows(Vector<?> rows,boolean replace) throws AssignValueException{
		//System.err.println("**************** Inicio buildRows**********"+System.currentTimeMillis());
		int numRowsBefore=getRowCount();
		int rowSelection=-1;
		Iterator<?> itr=rows.iterator();
		
		boolean isGTableRow=true;
		if(!rows.isEmpty() && (rows.get(0) instanceof RowItem)){
			isGTableRow=false;
		}
		
		if(isGTableRow){
			while(itr.hasNext()){
				GTableRow tableRow=(GTableRow)itr.next();
				boolean rowAdded=setTableRow(tableRow, replace);
				if(rowAdded)
					rowSelection=getRowCount()-1;
			}
		}else{
			while(itr.hasNext()){
				RowItem rowItem=(RowItem)itr.next();
				boolean rowAdded=setRowItem(rowItem, replace);
				if(rowAdded)
					rowSelection=getRowCount()-1;
			}
		}
		
		//Deseleccionamos el checkbox de seleccion de todo al insertar nuevos registros
		if(replace && columnSelectionRowTable!=null){
			TableColumn tc = getColumnModel().getColumn(columnSelectionRowTable);
			tc.setHeaderValue(false);
			parent.getTable().getTableHeader().repaint();
		}
		
		if((m_creationRow || m_finderRow) && rowSelection!=-1){
			//Hacemos, si existe, que la fila en blanco sea la ultima fila de la tabla
			boolean selection=true;
			if(parent!=null && parent.getTable().getSelectedRow()==-1){
				selection=false;
			}
			
			if(!init && selection){
				parent.getTable().setRowSelectionInterval(getRowCount()-1, getRowCount()-1);//Fila Creada
				//System.err.println("Seleccionaaaaaaaaaaaaaaaa "+parent.getId());
			}
			
			if(removeNullRow()){
//					Component componentFocus=parent.window.getFocusOwner();
				if(this.parent.getFormField().isTopLabel()){//Si es una tabla de una sola fila no hay que volver a crear la nullRow
					
					addNullRow();
				}
				
			}
		}
		
		//System.err.println("**************** Antes FireTableInserted buildRows**********"+System.currentTimeMillis());
		
		if (!replace && !m_cuantitativo && (parent==null || parent.m_modoFilter) )
			fireTableRowsInserted(numRowsBefore, numRowsBefore+rows.size()-1);
		
		//System.err.println("**************** Final buildRows**********"+System.currentTimeMillis());
		//updateColumnWidths();
		
		//System.err.println("m_rowData tras buildRows:"+m_rowData);
	}
	
	public void updateColumnWidths(){
		//Hacemos que se ejecute en el hilo AWT ya que, si no es asi, a veces se queda bloqueado en las pruebas
		final Runnable update = new Runnable() {
			public void run() {
				updateColumnWidths(0);
			}
		};
		SwingUtilities.invokeLater(update);
	}
	
	/**
	 * Actualiza el ancho de las columnas de la tabla a partir de su contenido
	 * @param depth Nos sirve para evitar que entre en bucle infinito
	 */
	private void updateColumnWidths(final int depth){
		final Runnable update = new Runnable() {
			public void run() {
				updateColumnWidths(depth+1);
			}
		};
		if(depth<10){
			if(parent!=null && (m_modoFilter || parent.getTable().isValid())/*Esperamos a que sea valido por que si no los tamaños para hacer los calculos no son correctos*/)
				calcColumnWidths(parent.getTable());
			else{
				SwingUtilities.invokeLater(update);
			}
		}
	}

	public void setDataFilter(selectData rows, boolean permanent) {
		for (int i = 0; i < m_rowData.size(); i++) {
			RowItem it = (RowItem) m_rowData.get(i);
			it.setFiltered(true);
		}
		Iterator iRow = rows.getIterator();
		while (iRow.hasNext()) {
			instance eRow = (instance) iRow.next();
			int origPos = findRow(eRow.getIDO(), false, permanent);
			// System.out.println("PAR1:"+eRow.getAttributeValue("PAR1")+",POS:"+origPos);
			RowItem it = (RowItem) m_rowData.get(origPos);
			it.setFiltered(false);
		}
		if (m_filteredState)
			updateGUI(true);

	}

	public void setStateDataFilter(boolean state) {
		boolean changed = m_filteredState != state;
		m_filteredState = state;
		if (changed)
			updateGUI(true);
	}

	public void removeData(Boolean permanent) {
		if(permanent==null)
			m_rowData.clear();
		else{
			for(int i=m_rowData.size()-1;i>=0;i--){
				RowItem rItem=m_rowData.get(i);
				if(rItem.isPermanent()==permanent)
					m_rowData.remove(i);	
			}
		}
		updateGUI(true);
		// fireTableRowsDeleted(1,len);
	}

	public void clean() throws AssignValueException{
		for (int r = m_visibleRowMap.size()-1; r >=0; r--) {
			RowItem row = (RowItem) m_rowData.get(getRowIndex(r));
			if(!row.isNullRow()){
				delRow(r);
			}
		}
	}
	
	public void delRows(ArrayList<GIdRow> table,boolean permanent) throws AssignValueException {
		// la tabla es una tabla de parametros que contiene los datos de los
		// registros a eliminar
		// cada row de la tabla de entrada, tiene at PAR1 - con el id_o en caso
		// de ser objetos o indice
		// en cualquier caso

		Iterator<GIdRow> iRow = table.iterator();
		while (iRow.hasNext()) {
			GIdRow eRow = iRow.next();
			int row = findRow(eRow.getIdo(), true, permanent);
			// System.out.println("PRE DEL ROW:par1,dataRow:"+par1+","+row);
			if (row >= 0)
				removeRow(row, !iRow.hasNext());
			//else System.err.println("WARNING: Fila con ido:"+eRow.getIdo()+" no removido de la tabla con filas:"+getRowData());
		}
		//System.err.println("m_rowData tras borrar:"+m_rowData);
	}

	public void delRow(int visRow) throws AssignValueException {
		Integer row = (Integer) m_visibleRowMap.get(visRow);
		removeRow(row.intValue(), true);
	}

	private void removeRow(int dataRowIndex, boolean refreshGui) throws AssignValueException {
		Integer key = new Integer(dataRowIndex);
		int visibleRow = m_visibleRowMap.indexOf(key);
		//System.err.println("VISIBLE ROW:"+visibleRow+" getRowCount:"+getRowCount()+" table.getRowCount:"+parent.getTable().getRowCount());
		if (visibleRow != -1) {
			m_visibleRowMap.remove(visibleRow);
			if (m_visibleRowMap.size() > visibleRow) {
				for (int i = visibleRow; i < m_visibleRowMap.size(); i++) {
					Integer indexData = (Integer) m_visibleRowMap.get(i);
					m_visibleRowMap.set(i,
							new Integer(indexData.intValue() - 1));
				}
			}
		}
		RowItem ri=m_rowData.get(dataRowIndex);
		m_rowData.remove(ri);
		if (refreshGui)
			if (m_cuantitativo)
				updateGUI(true);
			else if (visibleRow != -1)
				fireTableRowsDeleted(visibleRow, visibleRow);
		
		//Si esta en modo edicion mientras se borra una fila hacemos que salga de ese modo ya que su row es incorrecta y provoca una excepcion
		if(parent!=null){
			int editingRow=parent.getTable().getEditingRow();
			if(editingRow>=dataRowIndex){//Si la fila es menor que la borrada el row es correcto por lo que no hacemos nada
				parent.getTable().removeEditor();
			}
		
		}
		if((m_creationRow || m_finderRow) && !ri.isNullRow() && !m_modoConsulta){
			if(!this.parent.getFormField().isTopLabel())//Si es una tabla de una sola fila y se ha borrado pues ponemos la nullRow
				addNullRow();
		}
	}

	public Color getRowColor(int visibleRow) {
		int rowPos = getRowIndex(visibleRow);
		RowItem row = (RowItem) m_rowData.get(rowPos);
		return row.getColor();
	}

	/*
	 * private Object[] getColumnData( instance row ){ Object[] res = new
	 * Object[getColumnDataCount()];
	 * 
	 * res[0]="PruebaCodigo"; for( int c=0; c< getColumnDataCount(); c++){
	 * 
	 * /*int idProp = ((Integer) columnTAPOS.get(c)).intValue();
	 * row.getPropertyAccessIterator(row.getIDO(), idProp, null, null, null);
	 */

	/*
	 * int tapos = ((Integer) columnTAPOS.get(c)).intValue(); int ref =
	 * ((Integer) columnRef.get(c)).intValue(); Iterator itr =
	 * row.getAttIterator(false,false); while (itr.hasNext()) { attribute att =
	 * (attribute) itr.next(); if(att.getTapos()==tapos &&
	 * att.getVirtualREF()==ref){ res[c] = att.getValue(); break; } }
	 *//*
	 * } return res; }
	 */
	/*
	 * public void buildRow(ArrayList<Integer> idColumnList,ArrayList<Integer>
	 * dataTypeList,ArrayList<String> valueList,boolean replace) throws
	 * NoSuchElementException{ if( data==null ) return; Element eData =
	 * data.toElement(); //jdomParser.print("BUILDATA", eData); Iterator iRow =
	 * data.getIterator(); int row = getRowCount(); while (iRow.hasNext()) {
	 * ArrayList columnData = new ArrayList(); instance eRow = (instance)
	 * iRow.next(); String par1 = String.valueOf(eRow.getIDO()); String par2 =
	 * String.valueOf(eRow.getType()); String par3 = eRow.getRdn(); Color color =
	 * null; /*03/04/06 Si quisiera meter colores debería ñadir otra estructura
	 * con datos gráficos if(eRow.getAttributeValue("COLOR")!=null){
	 * if(eRow.getAttributeValue("COLOR").equals("BLUE")) color= Color.blue;
	 * if(eRow.getAttributeValue("COLOR").equals("RED")) color= Color.red;
	 * if(eRow.getAttributeValue("COLOR").equals("GREEN")) color= Color.green;
	 * }else
	 */
	/*
	 * color = Color.black;
	 * 
	 * ArrayList rowParRow = new ArrayList();
	 * 
	 * //añado los parámetros aunque sean nulos por no romper el indice
	 * rowParRow.add(par1); rowParRow.add(par2); rowParRow.add(par3);
	 * 
	 * //System.out.println("ROW "+eRow.getText()); int col = 0; //sea o no
	 * cuantitativo el numero de columnas en el tokenizer coincide con el de los
	 * datos Object[] columnValues = getColumnData(eRow); for (int c = 0; c <
	 * columnValues.length; c++) { columnData.add(columnValues[c]);
	 * //buildValue( col, valTk ); col++; } if (!replace) subAddRow(row,
	 * columnData, rowParRow, color, eRow); else { int rowToReplace =
	 * findRow(eRow.getIDO(), false); // se supone que para filtrar no llamo a
	 * buildMap if (rowToReplace >= 0) { RowItem ritem = (RowItem)
	 * m_rowData.get(rowToReplace); ritem.setColumnData(columnData); if
	 * (!m_cuantitativo) fireTableRowsUpdated(rowToReplace,rowToReplace); } else
	 * subAddRow(row, columnData, rowParRow, color, eRow); } row++; } if(row>0)
	 * calcColumnWidths(parent.m_objTable); if (m_cuantitativo) updateGUI(true);
	 * //if(row>0) addNullRow(); }
	 */
	/*
	 * public void BuildData(selectData data, boolean replace) throws
	 * NoSuchElementException{ if( data==null ) return; /*Element eData =
	 * data.toElement();
	 */
	// jdomParser.print("BUILDATA", eData);
	/*
	 * Iterator iRow = data.getIterator(); int row = getRowCount(); while
	 * (iRow.hasNext()) { ArrayList columnData = new ArrayList(); instance eRow =
	 * (instance) iRow.next(); String par1 = String.valueOf(eRow.getIDO());
	 * String par2 = String.valueOf(eRow.getType()); String par3 =
	 * eRow.getRdn(); Color color = null; /*03/04/06 Si quisiera meter colores
	 * debería ñadir otra estructura con datos gráficos
	 * if(eRow.getAttributeValue("COLOR")!=null){
	 * if(eRow.getAttributeValue("COLOR").equals("BLUE")) color= Color.blue;
	 * if(eRow.getAttributeValue("COLOR").equals("RED")) color= Color.red;
	 * if(eRow.getAttributeValue("COLOR").equals("GREEN")) color= Color.green;
	 * }else
	 */
	/*
	 * color = Color.black;
	 * 
	 * ArrayList rowParRow = new ArrayList();
	 * 
	 * //añado los parámetros aunque sean nulos por no romper el indice
	 * rowParRow.add(par1); rowParRow.add(par2); rowParRow.add(par3);
	 * 
	 * //System.out.println("ROW "+eRow.getText()); int col = 0; //sea o no
	 * cuantitativo el numero de columnas en el tokenizer coincide con el de los
	 * datos Object[] columnValues = getColumnData(eRow); for (int c = 0; c <
	 * columnValues.length; c++) { columnData.add(columnValues[c]);
	 * //buildValue( col, valTk ); col++; } if (!replace) subAddRow(row,
	 * columnData, rowParRow, color, eRow); else { int rowToReplace =
	 * findRow(eRow.getIDO(), false); // se supone que para filtrar no llamo a
	 * buildMap if (rowToReplace >= 0) { RowItem ritem = (RowItem)
	 * m_rowData.get(rowToReplace); ritem.setColumnData(columnData); if
	 * (!m_cuantitativo) fireTableRowsUpdated(rowToReplace,rowToReplace); } else
	 * subAddRow(row, columnData, rowParRow, color, eRow); } row++; } if(row>0)
	 * calcColumnWidths(parent.m_objTable); if (m_cuantitativo) updateGUI(true);
	 * //if(row>0) addNullRow(); }
	 */
	public boolean setTableRow(GTableRow tableRow, boolean replace)
	throws AssignValueException {

		Iterator<String> itrIdColumns=Map_Column_IDForm.iterator();
		HashMap<String,Object> columnValues=new HashMap<String, Object>();
		while(itrIdColumns.hasNext()){
			String idColumn=itrIdColumns.next();
			Object value=tableRow.getDataColumn(idColumn);
			columnValues.put(idColumn, value);
		}
		GIdRow idRow=tableRow.getIdRow();
		
		int ido=-1;
		int idto=-1;
		if(idRow!=null){
			ido=idRow.getIdo();
			idto=idRow.getIdto();
		}
		HashMap<String,Integer> columnsIdObjectByIdColumn=tableRow.getIdoMap();
		HashMap<String,Integer> columnsIdtoByIdColumn=tableRow.getIdtoMap();
		HashMap<String,Integer> columnsIdoFilterByIdColumn=tableRow.getIdoFilterMap();
		HashMap<String,Integer> columnsIdtoFilterByIdColumn=tableRow.getIdtoFilterMap();
		
		Color color = null;
		/*
		 * 03/04/06 Si quisiera meter colores debería ñadir otra estructura con
		 * datos gráficos if(eRow.getAttributeValue("COLOR")!=null){
		 * if(eRow.getAttributeValue("COLOR").equals("BLUE")) color= Color.blue;
		 * if(eRow.getAttributeValue("COLOR").equals("RED")) color= Color.red;
		 * if(eRow.getAttributeValue("COLOR").equals("GREEN")) color=
		 * Color.green; }else
		 */
		color = Color.black;

		boolean nullRow=tableRow.isNullRow();
		boolean permanent=tableRow.isPermanent();
			
//		ArrayList<Integer> rowParRow = new ArrayList<Integer>();
//
//		// añado los parámetros aunque sean nulos por no romper el indice
//		rowParRow.add(ido);
//		rowParRow.add(idto);
//		rowParRow.add(null);// Es el rdn, y en la nueva instance siempre viene a
//		// null

		int row = !permanent?getRowCount():0;

		ArrayList<Object> columnData = new ArrayList<Object>();
		ArrayList<Integer> columnIdo = new ArrayList<Integer>();
		ArrayList<Integer> columnOldIdo = new ArrayList<Integer>();
		ArrayList<Integer> columnIdto = new ArrayList<Integer>();
		ArrayList<Integer> columnOldIdto = new ArrayList<Integer>();
		ArrayList<String> columnIdParent = new ArrayList<String>();
		ArrayList<Integer> columnIdoFilter = new ArrayList<Integer>();
		ArrayList<Integer> columnIdtoFilter = new ArrayList<Integer>();
		int columns=getColumnCount();
		for(int i=0;i<columns;i++){
			String id=getFieldIDFromColumn(i);
			Object value=columnValues.get(id);
			columnData.add(value);
			
			Integer idObject=columnsIdObjectByIdColumn.get(id);
			columnIdo.add(idObject);
			columnOldIdo.add(idObject);
			
			Integer idtoObj=columnsIdtoByIdColumn.get(id);
			columnIdto.add(idtoObj);
			columnOldIdto.add(idtoObj);
			
			Integer idoFilter=columnsIdoFilterByIdColumn.get(id);
			columnIdoFilter.add(idoFilter);
			
			Integer idtoFilter=columnsIdtoFilterByIdColumn.get(id);
			columnIdtoFilter.add(idtoFilter);
			
			String idParent=tableRow.getIdParentMap(id);
			//System.err.println("idParent:"+idParent+" col:"+i+ " columnValue:"+value+" columnIdo:"+idObject);
			columnIdParent.add(idParent);
		}

		
		RowItem ritem = buildRowItem(row, columnData, columnIdo, columnOldIdo, columnIdto, columnOldIdto, columnIdParent, columnIdoFilter, columnIdtoFilter, color, idRow, nullRow, permanent);
		
		return setRowItem(ritem, replace);
	}

	private boolean setRowItem(RowItem ritem, boolean replace) throws AssignValueException {
		boolean rowAdded=true;
		
		if (!replace){
			subAddRow(ritem.getIndex(), ritem);
		}else {
			int rowToReplace = findRow(ritem.getIdRow().getIdo(), false, ritem.isPermanent()); // se supone
			// que para
			// filtrar
			// no llamo
			// a
			// buildMap
			if(parent!=null && !parent.isTopLabel() && !parent.newValueAllowed()){
				rowToReplace=0;
			}
			
			if (rowToReplace >= 0) {
				m_rowData.set(rowToReplace,ritem);
								
				rowAdded=false;
				
				if (!m_cuantitativo)
					fireTableRowsUpdated(rowToReplace, rowToReplace);
				
				// Si esta en modo edicion mientras se inserta una fila hacemos que salga de ese modo y vuelva a entrar ya que si cambia el valor de esa celda(al insertar una nueva fila la celda en edicion puede pertenecer a otro registro) no se actualiza
				if(parent!=null){
					//System.err.println("Tiene padre. La tabla "+parent.getLabel()+" isEditing:"+parent.getTable().isEditing());
					Component editor=parent.getTable().getEditorComponent();
					//System.err.println("editor:"+editor);
					if(editor!=null){
						parent.getTable().removeEditor();
						editor.requestFocusInWindow();
						//System.err.println("************************************************subAddRow");
					}/*else{
						System.err.println("Editor es null de la tabla:"+parent.getLabel());
					}*/
				}
			} else{
				subAddRow(ritem.getIndex(), ritem);
			}
		}
//		row++;
		
//		if((m_creationRow || m_finderRow) && !nullRow && rowAdded){
//			//Hacemos, si existe, que la fila en blanco sea la ultima fila de la tabla
//			boolean selection=true;
//			if(parent!=null && parent.getTable().getSelectedRow()==-1){
//				selection=false;
//			}
//			
//			if(!init && selection){
//				parent.getTable().setRowSelectionInterval(getRowCount()-1, getRowCount()-1);//Fila Creada
//				System.err.println("Seleccionaaaaaaaaaaaaaaaa "+parent.getId());
//			}
//			
//				if(removeNullRow()){
////					Component componentFocus=parent.window.getFocusOwner();
//					if(this.parent.getFormField().isTopLabel()){//Si es una tabla de una sola fila no hay que volver a crear la nullRow
//						
//						addNullRow();
//					}
//					
//				}
//		}
		//calcColumnWidths(parent.m_objTable);

		//calculateWidthColum(parent.m_objTable);

		if (m_cuantitativo)
			updateGUI(true);
		// if(row>0) addNullRow();
		
		return rowAdded;
	}
	

	/*private void calculateWidthColum(JTable table) {
		ArrayList<Integer> tams = new ArrayList<Integer>();
		for (int i=0;i<table.getColumnCount();i++){
			TableColumn colum=table.getColumnModel().getColumn(i);
			//System.err.println(""+colum.getWidth()+" "+colum.getMinWidth()+" "+colum.getMaxWidth()+" "+colum.getPreferredWidth());
			tams.add(colum.getWidth());
		}
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i=0;i<table.getColumnCount();i++){
			TableColumn colum=table.getColumnModel().getColumn(i);
			colum.setPreferredWidth(tams.get(i));
		}
		table.getTableHeader().repaint();
		table.getTableHeader().revalidate();
		table.repaint();
		table.revalidate();
	}*/


	public void orderRows(int column){
		/*ArrayList dataList=new ArrayList();
		HashMap<Object,ArrayList> valueRowMap=new HashMap<Object, ArrayList>();
		int rowCounts=getRowCount();
		for(int i=0;i<rowCounts;i++){
			RowItem ritem = (RowItem) m_rowData.get(i);
			Object data=ritem.getColumnData(column);
			dataList.add(data);
			if(valueRowMap.containsKey(data)){
				ArrayList<RowItem> ritems=valueRowMap.get(data);
				ritems.add(ritem);
				//valueRowMap.put(data,ritems);
			}else{
				ArrayList<RowItem> ritems=new ArrayList();
				ritems.add(ritem);
				valueRowMap.put(data,ritems);
			}	
		}
		Object[] datasOrder = dataList.toArray();
		Arrays.sort(datasOrder);
		int row=0;
		m_rowData.removeAll(m_rowData);
		m_visibleRowMap.removeAll(m_visibleRowMap);
		for (int i = 0; i < datasOrder.length; i++) {
			Object data = datasOrder[i];
			Iterator<RowItem> itr=valueRowMap.get(data).iterator();
			while(itr.hasNext()){
				RowItem ritem=itr.next();
				ritem.index=row;
				m_rowData.add(ritem);
				m_visibleRowMap.add(new Integer(row));
				row++;
			}

		}
		if (!m_cuantitativo)
			fireTableRowsInserted(0, row!=0?row-1:0);
		 */
		int[] columns={column};
		int[] sortingDirections={SORT_ASCENDING};
		sortColumns(columns, sortingDirections, parent.getTable());
	}

	public void calcColumnWidths(JTable table) {
		table.validate();
		//table.doLayout();
		JTableHeader header = table.getTableHeader();
		TableCellRenderer defaultHeaderRenderer = null;
		if (header != null)
			defaultHeaderRenderer = header.getDefaultRenderer();

		TableColumnModel columns = table.getColumnModel();
		GTableModel data = this;
		int margin = columns.getColumnMargin();
		int rowCount = data.getRowCount()<10?data.getRowCount():10;//Nos basamos en los primeros 10 registros para hacer los calculos ya que si no tarda demasiado
		int totalWidth = 0;
		int totalWidthRows = 0;

		HashMap<TableColumn,Integer> mapTableColumnToResizeNotPrioritary=new HashMap<TableColumn, Integer>();
		for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
			TableColumn column = columns.getColumn(i);
			int columnIndex = column.getModelIndex();
			int width = -1;
			TableCellRenderer h = column.getHeaderRenderer();
			if (h == null)
				h = defaultHeaderRenderer;
			if (h != null){
				Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, i);
				width = c.getPreferredSize().width;
			}
			
			//Las columnas prioritarias son las que vamos a respetar el tamaño calculado. Las no prioritarias dependera de si hay espacio suficiente o no. Si no lo hay se les restaran pixeles.
			boolean prioritaryColumn=false;
			if(rowCount>0){
				TableCellRenderer r = table.getCellRenderer(0, i);
				if(r instanceof TextCellRenderer){
					TextCellRenderer textCell=(TextCellRenderer)r;
					int type=textCell.getType();
					if(type==GConst.TM_INTEGER || type==GConst.TM_REAL || type==GConst.TM_BOOLEAN ||
							(textCell.getColumn().getIdProp()==Constants.IdPROP_RDN && textCell.getColumn().getIdParent()==parent.getId()/*rdn principal*/)){
						prioritaryColumn=true;
					}
				}
			}
			
			int widthRows=width;
			for (int row = rowCount - 1; row >= 0; --row) {
				TableCellRenderer r = table.getCellRenderer(row, i);
				Component c = r.getTableCellRendererComponent(table, data.getValueAt(row, columnIndex), false, false, row, i);
				if (c != null){
					//System.err.println("parent.getTable().getName():"+parent.getTable().getName()+" con preferredSize:"+c.getPreferredSize());
					widthRows = Math.max(widthRows, (int)c.getPreferredSize().getWidth());
				}
			}
			if (widthRows >= 0){
				if(prioritaryColumn){
					column.setPreferredWidth(widthRows + margin);//Asignamos el tamaño maximo de la cabecera y de las filas
				}else{
					column.setPreferredWidth(width + margin);//Asignamos el tamaño de la cabecera. Mas abajo se le asignara el tamaño dependiente del contenido y del espacio disponible
					mapTableColumnToResizeNotPrioritary.put(column, widthRows + margin);
				}
			}/*else
				System.err.println("No hay ancho");*/
			totalWidth += column.getPreferredWidth();
			totalWidthRows +=(widthRows+margin);
		}
		
		int tableWidth=(int)parent.m_objComponent.getWidth();//Se trata del scrollPane
		int toRest=0;
		//Vemos si hay espacio de sobra para asignar el tamaño basandonos en el ancho del contenido de las filas tambien para las columnas que no son prioritarias
		if(totalWidthRows>=tableWidth){
			//Si no lo hay, asignamos el tamaño restandole pixeles a las columnas no prioritarias. De esta manera evitamos que la autoredimension de swing haga mas pequeñas tambien las columnas prioritarias
			toRest=(int)Math.ceil(((double)totalWidthRows-tableWidth)/mapTableColumnToResizeNotPrioritary.size());
		}
		for(TableColumn tableColumn:mapTableColumnToResizeNotPrioritary.keySet()){
			tableColumn.setPreferredWidth(mapTableColumnToResizeNotPrioritary.get(tableColumn)-toRest);
		}
		
		table.validate();
		//table.doLayout();
	}

	/*
	 * public void BuildData(ArrayList dataRows, ArrayList parRows, boolean
	 * replace, instance source) throws NoSuchElementException{ int
	 * row=getRowCount(); for( int i=0;i<dataRows.size();i++){ ArrayList
	 * columnData= (ArrayList)dataRows.get(i); ArrayList rowParRow=
	 * (ArrayList)parRows.get(i);
	 * 
	 * if(!replace) subAddRow( row, columnData, rowParRow, null, source ); else{
	 * int rowToReplace=findRow(Integer.parseInt(rowParRow.get(0).toString()),
	 * false); if(rowToReplace>=0) { RowItem ritem= (RowItem)m_rowData.get(
	 * rowToReplace ); ritem.setColumnData( columnData ); if(!m_cuantitativo)
	 * fireTableRowsUpdated(rowToReplace,rowToReplace); }else subAddRow( row,
	 * columnData, rowParRow, null, source ); } row++; } if( m_cuantitativo)
	 * updateGUI(true); //if(row>0) addNullRow(); }
	 */

	private void subAddRow(int row, RowItem ritem) throws AssignValueException {
		
		if(parent==null || parent.m_modoFilter || parent.newValueAllowed()){
			
			
			//m_rowData.add(ritem);
			m_rowData.add(row,ritem);
			//Si la insercion no es al final modificamos el index de los siguientes rowItem
			for(int i=row+1;i<m_rowData.size();i++){
				m_rowData.get(i).setIndex(i);
			}
			m_visibleRowMap.add(/*new Integer(row)*/m_rowData.size()-1);
			
			if (!m_cuantitativo && (parent==null || !parent.m_modoFilter || ritem.isPermanent()))
				fireTableRowsInserted(row, row);
			
			//Si esta en modo edicion mientras se inserta una fila hacemos que salga de ese modo y vuelva a entrar ya que si cambia el valor de esa celda(al insertar una nueva fila la celda en edicion puede pertenecer a otro registro) no se actualiza
			if(parent!=null && !parent.m_modoFilter){
				//System.err.println("Tiene padre. La tabla "+parent.getLabel()+" isEditing:"+parent.getTable().isEditing());
				Component editor=parent.getTable().getEditorComponent();
				//System.err.println("editor:"+editor);
				if(editor!=null){
					parent.getTable().removeEditor();
					editor.requestFocusInWindow();
					//System.err.println("************************************************subAddRow");
				}/*else{
					System.err.println("Editor es null de la tabla:"+parent.getLabel());
				}*/
			}
		}else{
			AssignValueException e2=new AssignValueException("No se pueden añadir mas valores a la tabla "+parent.getLabel());
			e2.setUserMessage("No se pueden añadir mas valores a la tabla '"+parent.getLabel()+"'");
			throw e2;
		}
	}

	private RowItem buildRowItem(int row, ArrayList columnData,
			ArrayList<Integer> columnIdo, ArrayList<Integer> columnOldIdo,
			ArrayList<Integer> columnIdto, ArrayList<Integer> columnOldIdto,
			ArrayList<String> columnIdParent,
			ArrayList<Integer> columnIdoFilter,
			ArrayList<Integer> columnIdtoFilter, Color color, GIdRow idRow,
			boolean nullRow, boolean permanent) {
		RowItem ritem = new RowItem(row, /*getColumnCount()
				- (m_cuantitativo ? 1 : 0),*/ m_groupByColumns);
		ritem.setColumnData(columnData);
		ritem.setColumnIdo(columnIdo);
		ritem.setColumnOldIdo(columnOldIdo);
		ritem.setColumnIdto(columnIdto);
		ritem.setColumnOldIdto(columnOldIdto);
		ritem.setColumnIdParent(columnIdParent);
		ritem.setColumnIdoFilter(columnIdoFilter);
		ritem.setColumnIdtoFilter(columnIdtoFilter);
		ritem.setColor(color);
		ritem.setIdRow(idRow);
		ritem.setNullRow(nullRow);
		ritem.setPermanent(permanent);
		if(nullRow){
			if(m_creationRow)//Por defecto una nullRow se le pone estado de creacion. TextCellEditor se encarga de ponerle estado finder cuando sea necesario
				ritem.setState(RowItem.CREATION_STATE);
		}
		return ritem;
	}

	public void replaceColumn(int column, Integer ido, Object newVal, boolean permanent) {
		int rowToReplace = findRow(ido, false, permanent);
		RowItem it = (RowItem) m_rowData.get(rowToReplace);
		// Object valor= buildValue( column, newVal );
		it.setColumnData(column, newVal);
		// System.out.println("ROW TO REPLACE:"+rowToReplace+",new val,
		// buildVal:"+newVal+","+valor);
		if (m_cuantitativo)
			updateGUI(true);
		else
			fireTableRowsUpdated(rowToReplace, rowToReplace);
	}

	/*
	 * 04/04/06 public Object buildValue( int column, String value ){ if(
	 * value==null || value.equals(" ") || value.equals("#NULLVALUE#") ) return
	 * null; int stx=((Integer)columnSintax.get(column)).intValue(); Object
	 * valor= null; //System.out.println("SINTAX:"+stx); switch(stx){ case
	 * helperConstant.TM_ENUMERADO: String
	 * id=(String)Map_Column_IDForm.get(column); Object lb= m_moa.get(id); if(
	 * value.indexOf(";")>=0 ) value=value.substring(0,value.indexOf(";"));
	 * //System.out.println("MOA MAP:"+id+","+m_moa.containsKey(id)); if(
	 * !m_moa.containsKey(id) ){//se supone que es por no ser visible valor= new
	 * itemList( value,null,null,true); break; } try{ if(lb instanceof
	 * ListBoxCellEditor){ valor= ((ListBoxCellEditor)lb).getItemList(new
	 * Integer( (new Double(value)).intValue())); }else{
	 * System.out.println("SIMPLE FORM:TABLE FORM:BUILD DATA:ENUMERADO: Error
	 * Sintaxis IDFORM:"+id); } }catch(NumberFormatException e){
	 * Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),e, "TABLE BUIL DATA:Fallo sintaxis row
	 * enumerado:"+value+","+ e.getMessage()); } break; case
	 * helperConstant.TM_BOOLEANO: valor= new Boolean( value ); break; case
	 * helperConstant.TM_BOOLEANO_EXT: valor= new Boolean(
	 * value.equals("1")||value.equals("-1")||value.equals("TRUE") ); break;
	 * case helperConstant.TM_ENTERO: if( value.indexOf(";")>=0 )
	 * value=value.substring(0,value.indexOf(";")); else if( value.indexOf("
	 * ")>=0 )//Es un rango valor= new Integer(value.substring(0,value.indexOf("
	 * "))); else valor= new Integer(value); break; case helperConstant.TM_REAL:
	 * if( value.indexOf(";")>=0 ) value=value.substring(0,value.indexOf(";"));
	 * else if( value.indexOf(" ")>=0 )//Es un rango valor= new
	 * Double(value.substring(0,value.indexOf(" "))); else valor= new
	 * Double(value); break; case helperConstant.TM_TEXTO: valor= value; break;
	 * case helperConstant.TM_FECHA: valor= value; break; case
	 * helperConstant.TM_FECHAHORA: valor= value; break; default:
	 * System.out.println("ERROR SINTAX NO MATCH:"+ stx); } return valor; }
	 */

	public int getColumnSintax(int col) {
		return ((Integer) columnSintax.get(col)).intValue();
	}

	public int getColumnIdProps(int col) {
		return ((Integer) columnIdProps.get(col)).intValue();
	}

	public Integer getColumnOfIdProp(int idProp) {
		return Map_columnsIdProp.get(idProp);
	}

	public void printRows() {

		for (int r = 0; r < m_rowData.size(); r++) {
			RowItem row = (RowItem) m_rowData.get(r);
			System.out.println("ROW:" + r + "," + row.isGroup() + ","
					+ row.getGroupSize());
		}
	}

	void updateGUI(boolean reagrupar) {
		if (reagrupar)
			agrupate();
		m_visibleRowMap.clear();
		for (int r = 0; r < m_rowData.size(); r++) {
			RowItem row = (RowItem) m_rowData.get(r);
			if (row.isGroup()) {
//				row.getColumnPar().clear();
//				row.getColumnPar().add(new Integer(r));
//				row.getColumnPar().add(new Integer(GTable.virtualTypeForAgregation));
			}
			if (row.isFiltered() && m_filteredState)
				continue;
			m_visibleRowMap.add(new Integer(r));
			if (row.isGroup() && !row.isGroupExpand())
				r += row.getGroupSize();
		}
		fireTableDataChanged();
		// printRows();
	}

	void inicializaGroupByColumns() {
		if (m_groupByColumns.size() == 0) {
			int lastGroupColumn = m_iniVirtualColumn - 1;
			// System.out.println("LAST COL:"+lastGroupColumn);
			m_groupByColumns.add(new Integer(m_atGroupTypeColumn));
			for (int c = 1; c < getColumnCount() - 1; c++) {// desde el 1 me
				// salto el rdn.
				// Resto 1 para quitar la columna del despliege
				if (c == m_atGroupTypeColumn)
					continue;
				int stx = ((Integer) columnSintax.get(c)).intValue();
				if (c > lastGroupColumn)
					continue;
				Integer iCol = new Integer(c);
				if (m_colTotales.indexOf(iCol) != -1)
					continue;
				if (m_colAgrupables.indexOf(iCol) != -1)
					continue;
				Integer tapos = (Integer) m_idPropModel
				.get(getRealDataColumn(c));
				//if (tapos.intValue() == helperConstant.TAPOS_ESTADO)
				//	continue;
				// lo considero agrupable
				if (c <= lastGroupColumn
						&& (stx == GConst.TM_MEMO
								|| stx == /* helperConstant.TM_FECHA */GConst.TM_DATE
								|| stx == /* helperConstant.TM_FECHAHORA */GConst.TM_DATE_HOUR
								|| stx == GConst.TM_INTEGER || stx == GConst.TM_REAL))
					continue;
				m_groupByColumns.add(new Integer(c));
			}
		}
	}

	int getGroupParent(int rowIndex, boolean visibleIndex) {
		if (visibleIndex)
			rowIndex = getRowIndex(rowIndex);
		int i = rowIndex - 1;
		while (i >= 0) {
			RowItem it = (RowItem) m_rowData.get(i);
			if (it.isGroup())
				if (it.getGroupSize() >= rowIndex - i)
					return i;
				else
					return -1;
			i--;
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	void agrupate() {
		if (!m_cuantitativo)
			return;
		int tam = m_rowData.size();
		for (int r = tam - 1; r >= 0; r--) {
			RowItem row = (RowItem) m_rowData.get(r);
			if (row.isGroup())
				m_rowData.remove(r);
		}
		Object[] data = m_rowData.toArray();
		Arrays.sort(data);
		m_rowData = new ArrayList(Arrays.asList(data));

		int lastGroupColumn = m_iniVirtualColumn - 1;
		Object[] oldData = new Object[getColumnCount()];
		boolean inGroup = false;
		ArrayList<RowItem> groupHeaders = new ArrayList<RowItem>();

		RowItem group = null;
		int groupSize = 0;
		boolean[] changeInColumn = new boolean[getColumnCount() - 1];
		for (int i = 0; i < getColumnCount() - 1; i++)
			changeInColumn[i] = false;

		for (int r = 0; r < m_rowData.size(); r++) {
			RowItem row = (RowItem) m_rowData.get(r);
			if (row.isFiltered() && m_filteredState)
				continue;
			boolean changeInGroup = false;

			// primero miro los posibles cambios de agrupacion
			for (int c = 1; c < getColumnCount() - 1; c++) {// desde el 1 me
				// salto el rdn
				if (m_groupByColumns.indexOf(new Integer(c)) == -1)
					continue;

				Object val = row.getColumnData(c);
				if (oldData[c] == null && val != null || val == null
						&& oldData[c] != null || val != null
						&& oldData[c] != null && !val.equals(oldData[c])) {

					changeInGroup = true;
					break;
				}
			}

			for (int c = 1; c < getColumnCount() - 1; c++) {// desde el 1 me
				// salto el
				// desplegable
				if (m_groupByColumns.indexOf(new Integer(c)) != -1)
					continue;
				if (m_colTotales.indexOf(new Integer(c)) != -1)
					continue;
				//int stx = ((Integer) columnSintax.get(c)).intValue();
				Object val = row.getColumnData(c);
				boolean change = false;
				if (oldData[c] == null && val != null || val == null
						&& oldData[c] != null || val != null
						&& oldData[c] != null && !val.equals(oldData[c])) {
					change = true;
				}

				if (!changeInGroup && !changeInColumn[c] && change)
					// si ha habido cambio de grupo no quiero perder la
					// informacion
					changeInColumn[c] = true;

			}
			if (!inGroup && !changeInGroup) {
				inGroup = true;
				groupSize = 1;
				int rowIniGroup = r;
				while (rowIniGroup > 0) {
					RowItem eIniGroup = (RowItem) m_rowData.get(--rowIniGroup);
					// decremento por si me ido abajo saltandome rows filtrados.
					if (!(m_filteredState && eIniGroup.isFiltered())) {
						group = new RowItem(rowIniGroup, /*getColumnCount() - 1,*/
								m_groupByColumns);
						groupHeaders.add(group);

						for (int c = 1; c < getColumnCount() - 1; c++) {
							int col = getRealDataColumn(c);
							int stx = ((Integer) columnSintax.get(c))
							.intValue();
							if (c <= lastGroupColumn
									&& (stx == GConst.TM_MEMO
											|| stx == GConst.TM_DATE || stx == GConst.TM_DATE_HOUR))
								continue;
							if (c > lastGroupColumn && c != m_atGroupTypeColumn)
								continue;
							if (m_colTotales.indexOf(new Integer(c)) != -1) {
								if (stx == GConst.TM_INTEGER) {
									Integer currVal = (Integer) eIniGroup
									.getColumnData(col);
									Integer grVal = new Integer(currVal
											.intValue());
									group.setColumnData(col, grVal);
								}
								if (stx == GConst.TM_REAL) {
									Double currVal = (Double) eIniGroup
									.getColumnData(col);
									Double grVal = new Double(currVal
											.doubleValue());
									group.setColumnData(col, grVal);
								}
							}
							if (m_colTotales.indexOf(new Integer(c)) == -1) {
								if (stx == GConst.TM_ENUMERATED) {
									Integer tapos = (Integer) m_idPropModel
									.get(col);
									//if (tapos.intValue() == helperConstant.TAPOS_ESTADO)
									//	continue;
									// lo considero agrupable
									ItemList currVal = (ItemList) eIniGroup
									.getColumnData(col);
									group.setColumnData(col, currVal.clone());
								}
								if (stx == GConst.TM_TEXT) {
									String currVal = (String) eIniGroup
									.getColumnData(col);
									group.setColumnData(col, new String(
											(String) currVal));
								}
							}
						}
						break;
					}
				}
			}

			if (inGroup)
				if (changeInGroup) {
					inGroup = false;
					groupSize = 0;
					for (int c = 1; c < getColumnCount() - 1; c++) {
						int col = getRealDataColumn(c);
						if (m_groupByColumns.indexOf(new Integer(c)) != -1)
							continue;
						if (m_colTotales.indexOf(new Integer(c)) != -1)
							continue;
						if (!changeInColumn[c]) {
							int stx = ((Integer) columnSintax.get(col))
							.intValue();
							Object val = row.getColumnData(col);
							if (val == null)
								continue;
							if (stx == GConst.TM_INTEGER) {
								Integer currVal = (Integer) val;
								Integer grVal = new Integer(currVal.intValue());
								group.setColumnData(col, grVal);
							}
							if (stx == GConst.TM_REAL) {
								Double currVal = (Double) val;
								Double grVal = new Double(currVal.doubleValue());
								group.setColumnData(col, grVal);
							}
							if (stx == GConst.TM_ENUMERATED) {
								ItemList currVal = (ItemList) val;
								group.setColumnData(col, currVal.clone());
							}
							if (stx == GConst.TM_TEXT) {
								String currVal = (String) val;
								group.setColumnData(col, new String(
										(String) currVal));
							}
						}
					}
					for (int i = 0; i < getColumnCount() - 1; i++)
						changeInColumn[i] = false;
				} else {
					group.setGroupSize(++groupSize);
					for (int c = 0; c < getColumnCount() - 1; c++) {
						int col = getRealDataColumn(c);
						if (m_colTotales.indexOf(new Integer(c)) == -1)
							continue;
						int stx = ((Integer) columnSintax.get(c)).intValue();
						if (stx == GConst.TM_INTEGER) {
							Integer currVal = (Integer) row.getColumnData(col);
							Integer grVal = (Integer) group.getColumnData(col);
							grVal = new Integer(currVal.intValue()
									+ grVal.intValue());
							group.setColumnData(col, grVal);
						}
						if (stx == GConst.TM_REAL) {
							Double currVal = (Double) row.getColumnData(col);
							Double grVal = (Double) group.getColumnData(col);
							grVal = new Double(currVal.floatValue()
									+ grVal.floatValue());
							group.setColumnData(col, grVal);
						}
					}
				}

			for (int c = 1; c < getColumnCount() - 1; c++) {
				int col = getRealDataColumn(c);
				int stx = ((Integer) columnSintax.get(c)).intValue();
				if (stx == GConst.TM_MEMO)
					continue;
				oldData[c] = row.getColumnData(col);
			}
		}
		for (int i = groupHeaders.size() - 1; i >= 0; i--) {
			RowItem header = (RowItem) groupHeaders.get(i);
			m_rowData.add(header.getIndex(), header);
		}
	}

	public int findRow(int ido, boolean filteringAware, Boolean permanent) {
		// se supone que el id del row es el parametro 1 (posicion 0)
		for (int r = 0; r < m_rowData.size(); r++) {
			RowItem row = (RowItem) m_rowData.get(r);
			if (filteringAware && row.isFiltered())
				continue;
			if(permanent!=null && row.isPermanent()!=permanent)
				continue;
			GIdRow idRow = row.getIdRow();
			if (!row.isGroup() && (idRow!=null && idRow.getIdo() == ido))
				// if( isVisible( r, filteringAware ) ) return r;
				return r;
		}
		return -1;
	}

//	public int findVisibleRow(String key, boolean filteringAware) {
//		// se supone que el id del row es el parametro 1 (posicion 0)
//		for (int r = 0; r < m_visibleRowMap.size(); r++) {
//			RowItem row = (RowItem) m_rowData.get(getRowIndex(r));
//			ArrayList parList = row.getColumnPar();
//			if (filteringAware && row.isFiltered())
//				return -1;
//			if (!row.isGroup() && parList.indexOf(key) == 0)
//				// if( isVisible( r, filteringAware ) ) return r;
//				return r;
//		}
//		return -1;
//	}

	public boolean isVisible(int rowIndex, boolean filteringAware) {
		RowItem row = (RowItem) m_rowData.get(rowIndex);
		if (m_filteredState && row.isFiltered())
			return false;

		if (!m_cuantitativo)
			return true;

		int indexG = getGroupParent(rowIndex, false);
		if (indexG == -1)
			return true;
		RowItem group = (RowItem) m_rowData.get(indexG);
		//int i = rowIndex;
		return group.isGroupExpand();
	}

	/*
	 * public void inizialiceRestriction(session ses) throws
	 * SystemException,ApplicationException{ if (m_modoConsulta) return; for
	 * (int visibleRow = 0; visibleRow < getRowCount(); visibleRow++) { RowItem
	 * row = (RowItem) m_rowData.get(getRowIndex(visibleRow)); Integer ido = new
	 * Integer((String) row.columnPar.get(0)); Integer to = new Integer((String)
	 * row.columnPar.get(1)); dominios dom = getDominio(ido, to); for (int col =
	 * 0; col < getColumnCount() - (m_cuantitativo ? 1 : 0); col++) { String id =
	 * (String) Map_Column_IDForm.get(col); dom.eventDataChanged(ses,
	 * to.intValue(), ido.intValue(), id); } } }
	 */
	public void addNullRow() throws AssignValueException {
		int idto=new IdObjectForm(m_id).getValueCls();
		GTableRow tableRow=new GTableRow(new GIdRow(0,idto,null));
		Iterator<String> itrCol=Map_Column_IDForm.iterator();
		while(itrCol.hasNext()){
			String idColumn=itrCol.next();
			//tableRow.setDataColumn(idColumn, null);
			if(IdObjectForm.matchFormat(idColumn))
				tableRow.setIdtoMap(idColumn, new IdObjectForm(idColumn).getIdto());
			else tableRow.setIdtoMap(idColumn, idto);
			String idParent=m_ff.getColumn(getFieldColumn(idColumn)).getIdParent();
			tableRow.setIdParentMap(idColumn, idParent);
		}
		tableRow.setNullRow(true);
		setTableRow(tableRow, false);
	}
	
	public boolean removeNullRow() throws AssignValueException {
		int rowToReplace = findRow(0, false, false);
		if(rowToReplace!=-1)
			delRow(rowToReplace);
		
		return rowToReplace!=-1;
	}

//	private void BuildMapXMLDom_LbOption(org.jdom.Element dom, int col) {
//	try {
//	if (dom == null || dom.getChild("POLIMORFISMO") == null)
//	return;

//	TableColumnModel tcm = parent.m_objTable.getColumnModel();
//	TableColumn tc = tcm.getColumn(col);
//	Object obj = tc.getCellEditor();
//	if (!(obj instanceof ListBoxCellEditor)) {
//	Singleton
//	.showMessageDialog("error en buildMap, editor no es ListBox");
//	return;
//	}
//	ListBoxCellEditor lb = (ListBoxCellEditor) obj;

//	Iterator iRest = dom.getChild("POLIMORFISMO").getChildren("REST")
//	.iterator();
//	int option = lb.getMaxOption() + 1;
//	while (iRest.hasNext()) {
//	org.jdom.Element eRest = (org.jdom.Element) iRest.next();

//	int restTapos = Integer.parseInt(eRest
//	.getAttributeValue("TA_POS"));
//	String rId = eRest.getAttributeValue("ID");
//	if (rId == null)
//	rId = "0@" + restTapos;
//	if (!lb.idForm.equals(rId))
//	continue;

//	Iterator iRule = eRest.getChildren("RULE").iterator();
//	while (iRule.hasNext()) {
//	org.jdom.Element eRule = (org.jdom.Element) iRule.next();
//	org.jdom.Element eDom = eRule.getChild("DOMINIO");
//	Map_XMLDom_ListOption.put(eDom, new Integer(option));
//	lb.buildEditor(option++, eDom);
//	}
//	}
//	} catch (Exception e) {
//	Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),e,
//	"BuildMapXMLDom_LbOption:Error" + e.getMessage());
//	}
//	}

//	public ArrayList getParameter(int[] rows) {
//		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
//		for (int i = 0; i < rows.length; i++) {
//			RowItem it = (RowItem) m_rowData.get(getRowIndex(rows[i]));
//			result.add(it.getColumnPar());
//		}
//		return result;
//	}
//
//	public ArrayList getParameterFromIndex(int row) {
//		return ((RowItem) m_rowData.get(getRowIndex(row))).getColumnPar();
//	}
//
//	public ArrayList getParameter(int idRow) {
//		int newId = findRow(idRow, true, null);
//		return ((RowItem) m_rowData.get(getRowIndex(newId))).getColumnPar();
//	}

	public ArrayList getData(int ido) {
		int newId = findRow(ido, true, null);
		//System.outprintln("SPMDBG1 " + ido + "," + newId);
		return ((RowItem) m_rowData.get(newId)).getColumnData();
	}

	public GIdRow getDataFromIndex(int rowIndex) {
		return ((RowItem) m_rowData.get(getRowIndex(rowIndex))).getIdRow();
	}
	
	public RowItem getRowItemFromIndex(int rowIndex) {
		return m_rowData.get(getRowIndex(rowIndex));
	}

	public RowItem getNextRow(int currRowViewIndex) {
		if (currRowViewIndex + 2 > m_visibleRowMap.size())
			return null;
		RowItem it = (RowItem) m_rowData.get(currRowViewIndex + 1);
		if(it.isNullRow())
			return null;
		return it;
	}

	public RowItem getPrevRow(int currRowViewIndex) {
		if (currRowViewIndex <= 0)
			return null;
		RowItem it = (RowItem) m_rowData.get(currRowViewIndex - 1);
		if(it.isNullRow())
			return null;
		return it;
	}

	/*private ArrayList getParameterTable() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < m_rowData.size(); i++) {
			RowItem it = (RowItem) m_rowData.get(i);
			if (it.isGroup())
				continue;
			result.add(it.columnPar);
		}
		return result;
	}

	private ArrayList getDataTable() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < m_rowData.size(); i++) {
			RowItem it = (RowItem) m_rowData.get(i);
			if (it.isGroup())
				continue;
			ArrayList row = new ArrayList();
			for (int c = 0; c < getVisibleColumnDataCount(); c++) {
				int realC = getRealDataColumn(c);
				if (realC == -1)
					continue;
				Object val = it.columnData.get(realC);
				if (val instanceof ItemList)
					val = ((ItemList) val).getInteger();
				row.add(val);
			}
			result.add(row);
		}
		return result;
	}*/

	public ArrayList<GIdRow> getIdRowsData(Boolean permanent) {
		ArrayList<GIdRow> result = new ArrayList<GIdRow>();
		for (int i = 0; i < m_rowData.size(); i++) {
			RowItem it = (RowItem) m_rowData.get(i);
			if (it.isGroup())
				continue;
			if(permanent==null || permanent.equals(it.isPermanent()))
				result.add(it.getIdRow());
		}
		return result;
	}

	public TableColumnModel getColumnModel() {
		return parent.getTable().getColumnModel();
	}

	public boolean isCellEditable(int row, int col) {
		/*
		 * if( m_cuantitativo && col==0 ){ RowItem it=
		 * (RowItem)m_rowData.get(getRowIndex(row)); return it.isGroup(); }
		 * boolean edit= ((Boolean)columnEditable.get(col)).booleanValue();
		 * return edit;
		 */
		
		RowItem it = (RowItem) m_rowData.get(getRowIndex(row));
		
		if(!parent.isProcessingSelectCellFromSystem() && !directEdition /*&& !parent.m_modoFilter*/ && parent.getTable().getSelectedRow()!=row && !it.isNullRow())//Si la fila no estaba ya seleccionada no se deja editar, asi podemos seleccionarla sin querer editarla
			return false;
		
		int colReal = getRealColumn(col);
		if (m_cuantitativo && colReal == 0) {
			return it.isGroup();
		}
		
		boolean edit=((Boolean) columnEditable.get(colReal)).booleanValue();
		
		if(!m_modoConsulta && !m_modoFilter){
			/* Lógica para permitir introducir datos en una columna que PERMITE FINDER
			 * 	NulRow:
			 * 		FinderRow:Editable si es del 1º nivel. En resto de niveles no editable
			 * 		CreationRow:Editable no se modifica
			 * 		FinderRow+CreationRow:Editable si es del 1º nivel. En resto de niveles no se modifica
			 * 	Registro:
			 * 		Se modifica a editable en todos los niveles menos en el 1º
			 */
			//System.err.println("column:"+colReal+" columnEditable:"+columnEditable.contains(colReal)+" columnEnable:"+columnsEnable.contains(colReal)+" columnFinder:"+columnsFinder.contains(colReal)+" finderRow:"+m_finderRow+" creationRow:"+m_creationRow+" nullRow:"+it.isNullRow()+" columnIdto"+it.getColumnIdto(col)+" parIdto:"+it.getColumnPar().get(1).intValue());
			if(columnsFinder.contains(colReal) && parent.getDictionaryFinder()!=null){
				if(it.isNullRow()){
					if(m_finderRow){
						//System.err.println(it.getColumnPar().get(1).intValue()+" "+it.getColumnIdto(col));
						if(it.getIdRow().getIdto()==it.getColumnIdto(col))
							edit=true;
						else if(!m_creationRow)
							edit=false;
						else edit=true;
					}else{
						if(it.getIdRow().getIdto()!=it.getColumnIdto(col))
							edit=true;
					}
				}else{
					if(columnsFinderIfCreation.contains(colReal)){
						if(isNewIdo(it.getIdRow().getIdo()))
							edit=true;
					}else{
						//if(it.getColumnPar().get(1).intValue()!=it.getColumnIdto(col))
							edit=true;
						/*if(it.getColumnIdo(col)!=null && m_controlListener!=null && parent!=null)
						if(!this.m_controlListener.isEditableInTable(parent.getId(), it.getColumnIdo(col)))
							edit=false;*/
					}
				}
			}else if(columnsEnable.contains(colReal)){
				if(columnsCreation.contains(colReal)){
					if(it.isNullRow()){
						if(m_creationRow){
							if(it.getColumnIdo(col)==null || isNewIdo(it.getColumnIdo(col)))
								edit=true;
						}
					}else{
						if(it.getColumnIdo(col)==null || isNewIdo(it.getColumnIdo(col))){
							edit=true;
						}else{
							int tm=getColumnSintax(col);
							if(tm==GConst.TM_INTEGER || tm==GConst.TM_REAL){//Permitimos que se editen los campos numericos
								edit=true;
							}
						}
					}
					//if(!allowModifyIdoCell(it.getColumnIdo(col)))
					//	edit=false;
				}else if(!it.isNullRow()){
					if(isNewIdo(it.getColumnIdo(col))){
						edit=true;
					}else{
						int tm=getColumnSintax(col);
						if(tm==GConst.TM_INTEGER || tm==GConst.TM_REAL){//Permitimos que se editen los campos numericos
							edit=true;
						}
					}
				}
			}
			
			//Comprobamos que esa columna pertenece realmente al tipo de la fila.
			//Esto ocurre cuando las tablas son abstractas y muestran columnas que tienen unos hijos pero otros no.
			//Se mira que getColumnIdParent sea null ya que eso ocurre cuando la columna no es compatible, pero
			//quizas deberiamos buscar otro mecanismo que nos lo indique mas fehacientemente
			if(edit && it.getColumnIdParent(col)==null){
				//System.err.println("No es columna de ese individuo");
				edit=false;
			}
		}
		
		//System.err.println("Editable:"+edit);
		return edit;
	}

	public int getColumnCount() {
		// incluye la columna de boton desplegable en cuantitativo
		return columnNames.size();
	}

	public int getRowCount() {
		if (m_visibleRowMap != null)
			return m_visibleRowMap.size();
		return 0;
	}

	public String getColumnName(int col) {
		return (String) columnNames.get(col);
	}

	private int getRowIndex(int visibleRow) {
		//System.err.println("getRowIndex: visibleRow:"+visibleRow+" m_visibleRowMap:"+m_visibleRowMap);
		return ((Integer) m_visibleRowMap.get(visibleRow)).intValue();
	}

	public Object getValueAt(int row, int col) {
		if (m_cuantitativo) {
			RowItem it = (RowItem) m_rowData.get(getRowIndex(row));
			if (col == 0)
				if (it.isGroup())
					return new Boolean(it.isGroupExpand());
				else {
					int grIndex = getGroupParent(row, true);
					if (grIndex == -1)
						return " ";
					else
						return "    >";
				}
			col--;
		}
		int dataCol = getRealDataColumn(col);
		if (dataCol == -1)
			return null;
		
		return getDataValueAt(row, getRealDataColumn(col));
	}

	public Object getDataValueAt(int row, int col) {
		// System.out.println("GET VALUE
		// AT:CUANT:"+m_cuantitativo+","+row+","+col+","+getRowCount());
		if (getRowCount() < row + 1) {
			System.err.println("Table Form Model:getValueAt, error, no existe el registro " + row);
			return null;
		}
		RowItem it = (RowItem) m_rowData.get(getRowIndex(row));
		if (it.getColumnSize() < col + 1) {
			System.err.println("Table Form Model:getValueAt, error, no existe la col, row " + col + "," + row);
			return null;
		}

		return it.getColumnData(col);
	}

	/*	public tableData getStructuredDataAt(int row, int col) {
		Object val = getDataValueAt(row, col);
		if (val instanceof itemList)
			val = ((itemList) val).getInteger();

		return new tableData(((Integer) columnRef.get(col)).intValue(),
				(Integer) columnTAPOS.get(col), val);

	}
	 */
	
	/*public Class getColumnClass(int c) {
		return columnClass.get(c);
	}*/

	int getDataColumn(int columnIndex) {
		return columnIndex - (m_cuantitativo ? 1 : 0);
	}
	
	public void setValueAt(Object newVal, int rowIndex, int columnIndex) {
		lastSetValueSuccess=true;
		executingSetValue=true;
		RowItem it = (RowItem) m_rowData.get(getRowIndex(rowIndex));
		
		//System.err.println("newVal "+newVal+" rowIndex "+rowIndex+" columnIndex "+columnIndex+" rowItem:"+it);
		Object oldVal=getValueAt(rowIndex, columnIndex);
		// DEPURACION
		if(!it.isNullRow() && !Auxiliar.equals(oldVal,newVal)&&!isNewIdo(it.getIdRow().getIdo()) && it.getState()==RowItem.IDLE_STATE && columnsFinder.contains(getRealColumn(columnIndex))){
			if(this.parent.getServer()!=null){
				 Exception ex=new Exception("ERROR DE EDICION DE FINDER: Se ha editado una columna que solo permite finder "+getColumnName(getRealColumn(columnIndex))+" valor:"+newVal);
				 this.parent.getServer().logError(null,ex, null);
			 }
			 Auxiliar.printCurrentStackTrace();
		}
		
		//Si se trata del click sobre la columna de selección de fila
		if(Auxiliar.equals(columnSelectionRowTable, columnIndex)){
			it.setColumnData(columnIndex, newVal);
			fireTableCellUpdated(rowIndex, columnIndex);
			if(Auxiliar.equals(newVal, false)){
				ListSelectionModel listSelection=parent.getTable().getSelectionModel();
				if(!listSelection.isSelectionEmpty() && listSelection.getMinSelectionIndex()!=listSelection.getMaxSelectionIndex()){
					listSelection.removeSelectionInterval(rowIndex, rowIndex);
				}
				TableColumn tc = getColumnModel().getColumn(columnIndex);
				tc.setHeaderValue(false);
				parent.getTable().getTableHeader().repaint();
			}
		}else if( m_controlListener!=null && !m_modoFilter){
			if( !(it.isNullRow() && newVal==null && !it.matchesState(RowItem.FINDER_STATE)/*Podría ser un finder sin valor en ese campo si este no es obligatorio*/) && parent.window!=null && parent.window.isDisplayable()){
				//Si la fila es nullRow y no se ha añadido ningun valor, no tenemos que crear nada
				//Ademas evitamos que se intente asignar el valor cuando la ventana ya se ha cerrado porque es un problema para las sesiones
				
				
				Object newV=null, oldV=null;
				String newVData=null, oldVData=null;
				int idSource = it.getIdRow().getIdo();
				Integer oldIdoRowEditing=getIdoRowEditing();
				HashSet<Integer> oldListIdosEditing=(HashSet<Integer>)getListIdosEditing().clone();
				try{
					if(!it.matchesState(RowItem.FINDER_STATE) && ((oldVal==null && newVal==null) || (oldVal != null && newVal != null && oldVal.equals(newVal))))//Si es igual al valor que ya teniamos no hacemos nada
						return;
					
					// Lo hacemos aqui y no donde se usa porque starEditionTable y creationRow pueden provocar que se modifique su valor
					Integer idoFinder=it.getColumnIdo(columnIndex);
					Integer idtoFinder=it.getColumnIdto(columnIndex);
					
					if(getIdoRowEditing()==null){
						if(!it.isNullRow() && it.getState()!=RowItem.FINDER_STATE){
							setIdoRowEditing(idSource);
						}
						m_controlListener.startEditionTable(parent.getId(),getIdoRowEditing(),parent.isProcessingPasteRows());
					}
						
					IdObjectForm idObjectForm=null;
					 if(it.getState()==RowItem.FINDER_STATE){
						 idObjectForm=new IdObjectForm(it.getColumnIdParent(columnIndex));
						 if(getIdoRowEditing()==null)//Esto ocurre cuando, en una nullRow asignamos una fila buscando un individuo que ya existe, de primer nivel
							 setIdoRowEditing(it.getColumnIdo(columnIndex));
						 else listIdosEditing.add(idObjectForm.getIdo());
					 }else if(it.getState()==RowItem.SUBCREATION_STATE){
						 IdObjectForm idObjectFormNew=new IdObjectForm(it.getColumnIdParent(columnIndex));
						 
						 int idtoSubRow=((CellEditor)parent.getTable().getCellEditor(rowIndex, columnIndex)).getLastSelectionSubCreation();
						 int idoSub=m_controlListener.newSubRowTable(idObjectFormNew.getIdString(),idtoSubRow);
						 //oldVal = getValueAt(rowIndex, columnIndex);
						 
						 idObjectForm=new IdObjectForm((String) Map_Column_IDForm.get(columnIndex));
						 idObjectForm.setIdo(idoSub);
						 
						 listIdosEditing.add(idObjectForm.getIdo());
						 //if(idoRowEditing==null)
						//	 idoRowEditing=it.getColumnIdo(columnIndex);
					 }else if(it.isNullRow()){
						 int idtoRow=((CellEditor)parent.getTable().getCellEditor(rowIndex, columnIndex)).getLastSelectionCreation();
					 	 int ido=m_controlListener.newRowTable(m_id,idtoRow);
						 rowIndex = findRow(ido, false, it.isPermanent());
						 
						 //oldVal = getValueAt(rowIndex, columnIndex);
						 if(it.getState()==RowItem.CREATION_STATE){
							 idObjectForm=new IdObjectForm((String) Map_Column_IDForm.get(columnIndex));
							 idObjectForm.setIdo(ido);
							 
							 //idoRowEditing=ido;
						 }else if(it.getState()==RowItem.CREATION_STATE+RowItem.FINDER_STATE){
							 RowItem itNewRow = (RowItem) m_rowData.get(getRowIndex(rowIndex));
							 idObjectForm=new IdObjectForm(itNewRow.getColumnIdParent(columnIndex));
							 
							 //idoRowEditing=itNewRow.getSourceData().getIDO();
						 }else /*State Creation+SubCreation*/{
							 RowItem itNewRow = (RowItem) m_rowData.get(getRowIndex(rowIndex));
							 IdObjectForm idObjectFormNew=new IdObjectForm(itNewRow.getColumnIdParent(columnIndex));
							 
							 int idtoSubRow=((CellEditor)parent.getTable().getCellEditor(rowIndex, columnIndex)).getLastSelectionSubCreation();
							 int idoSub=m_controlListener.newSubRowTable(idObjectFormNew.getIdString(),idtoSubRow);
							 //oldVal = getValueAt(rowIndex, columnIndex);
							 
							 idObjectForm=new IdObjectForm((String) Map_Column_IDForm.get(columnIndex));
							 idObjectForm.setIdo(idoSub);
							 
							 listIdosEditing.add(idoSub);
						 }
						 rowCreating=true;
						 setIdoRowEditing(ido);
						 listIdosEditing.add(ido);
						 
					 }else if(it.getState()==RowItem.REMOVE_STATE){
						 idObjectForm=new IdObjectForm(it.getColumnIdParent(columnIndex));
						 //if(idoRowEditing==null)
						//	 idoRowEditing=idObjectForm.getIdo();//it.getColumnIdo(columnIndex);
						 
						 listIdosEditing.remove(it.getColumnIdo(columnIndex));
						 listIdosEditing.add(idObjectForm.getIdo());
					 }else{
						 idObjectForm=new IdObjectForm((String) Map_Column_IDForm.get(columnIndex));
						 
						 Integer ido = it.getColumnIdo(columnIndex);
						 idObjectForm.setIdo(ido);
						 
						 //idoRowEditing=idSource;
						 
						 listIdosEditing.add(idObjectForm.getIdo());
					 }
					 
					 //Volvemos a pedirlo ya que puede haber cambiado por las operaciones anteriores
					 oldVal=getValueAt(rowIndex, columnIndex);
					
					/*if(it.isColumnLabelVisible(columnIndex)){
						it.setColumnLabelBooleanValue(columnIndex);
						oldVal = null;
					}else{
						oldVal = getValueAt(rowIndex, columnIndex);
					}*/		
					Object oldData = null, newData = null;
					
					//TODO Actualmente nunca llega ni oldVal ni newVal como ItemList ya que enviamos el Integer en ComboBoxEditor.getCellEditorValue
					if (oldVal instanceof ItemList)
						oldData = ((ItemList) oldVal).getInteger()!=0?((ItemList) oldVal).getInteger():null;
					else
						oldData = oldVal;
					
					if (newVal instanceof ItemList)
						newData = ((ItemList) newVal).getInteger()!=0?((ItemList) newVal).getInteger():null;					
					else newData = newVal;
					
					if (!it.matchesState(RowItem.FINDER_STATE)/*Si es finder puede no tener valor pero si tendra ido a asignar*/ && Auxiliar.equals(oldData, newData))
						return;
			
					
					//System.err.println("setValueAt New "+newVal+" oldVal "+oldVal);
					/*int tm = 0;
					if (!(m_cuantitativo && columnIndex == 0))
						tm = getColumnSintax(getDataColumn(columnIndex));*/
			
					 if (m_cuantitativo) {
						 if (columnIndex == 0) {
							 if (newVal instanceof Boolean) {
								 it.setGroupExpand(((Boolean) newVal).booleanValue());
								 updateGUI(false);
							 }
							 return;
						 }
						 columnIndex--;
					 }
					 
					 String id=idObjectForm.getIdString();
					 //Integer idProp=columnIdProps.get(columnIndex);
					 //DataProperty data = (DataProperty)it.getSourceData().getProperty(ido,idProp,null,null,null);
					 //idObjectForm.setValueCls(data.getDataType());
				 
					 int newValueCls=idObjectForm.getValueCls();
					 int oldValueCls=idObjectForm.getValueCls();
					 
					 /*if(newData==null){
						 newV=null;
						 newVData=null;
					 }
					 else*/ if(it.getState()==RowItem.FINDER_STATE || it.getState()==(RowItem.FINDER_STATE+RowItem.CREATION_STATE)){
						 newV = idoFinder;//idoFinder!=null?String.valueOf(idoFinder):null;
						 newVData = newData!=null?String.valueOf(newData):null;
						 newValueCls= idtoFinder;//it.getColumnIdto(columnIndex);
					 }else{
						 newV = newData;//String.valueOf(newData);
						 newVData = String.valueOf(newData);
					 }
					 
					 /*if(oldData==null){
						 oldV=null;
						 if(it.matchesState(RowItem.FINDER_STATE) || it.getState()==RowItem.REMOVE_STATE){
							 Integer oldIdo=it.getColumnOldIdo(columnIndex);
							 oldV = oldIdo;//oldIdo!=null?String.valueOf(oldIdo):null;
						 }
						 oldVData=null;
					 }else*/ if(it.getState()==RowItem.FINDER_STATE || it.getState()==RowItem.REMOVE_STATE){
						 Integer oldIdo=it.getColumnOldIdo(columnIndex);
						 oldV = oldIdo;//oldIdo!=null?String.valueOf(oldIdo):null;
						 oldVData = oldData!=null?String.valueOf(oldData):null;
						 oldValueCls=it.getColumnOldIdto(columnIndex);
					 }else{
						 oldV = oldData;//String.valueOf(oldData);
						 oldVData = String.valueOf(oldData);
					 }
					 
					 //System.err.println("Value:"+newData+" class:"+(newData!=null?newData.getClass():null));			 
					 //TODO replaceColumn no modifica el valor de la property en el instance. Tenerlo en cuenta!
					 if(!it.isNullRow()){
						 replaceColumn(columnIndex, idSource, /*newVData*/newVal, it.isPermanent());
					 }/*else{
						 it.setColumnOldIdo(columnIndex,null);
						 it.setColumnIdo(columnIndex, null);
					 }*/
					 m_controlListener.setValueField(id,newV,oldV,newValueCls,oldValueCls);
					 
					 if(it.matchesState(RowItem.FINDER_STATE) && Auxiliar.equals(newV, oldV)){
						 //Esto solo ocurrira cuando se edita el valor de una columna que tiene finder en la que
						 //las reglas cambian el filtro de la busqueda, cambiandose el valor de uno de los campos
						 //que puede, o no, tener algo que ver con la tabla. Por lo tanto nos interesa dejar la
						 //columna con el valor que tenia antes del cambio del usuario ya que al final tendría que tener el mismo valor.
						 //Esto no esta ocurriendo automaticamente porque se ha enviado una orden de newV=oldV que no cambia el valor del campo, asi que lo hacemos nosotros aqui.
						 replaceColumn(columnIndex, idSource, oldVal, it.isPermanent());
					 }
				 }catch(AssignValueException ex){
					 ex.printStackTrace();
					 //setText(""+oldVal);
					 setIdoRowEditing(oldIdoRowEditing);
					 setListIdosEditing(oldListIdosEditing);
					 if(oldIdoRowEditing==null){
						 try{
							 m_controlListener.cancelEditionTable(parent.getId(), getIdoRowEditing());
						 } catch (EditionTableException exc) {
							parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),exc,"Error al intentar cancelar la edición de la fila");
						 } catch (AssignValueException e) {
								parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),e,"Error al intentar cancelar la edición de la fila");
							}
					 }
					
					 if(!it.isNullRow()){
						 replaceColumn(columnIndex, idSource, /*oldV*/oldVal, it.isPermanent());
						 it.setColumnIdo(columnIndex, it.getColumnOldIdo(columnIndex));
						 it.setColumnIdto(columnIndex, it.getColumnOldIdto(columnIndex));
					 }
					 lastSetValueSuccess=false;
					 parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),ex,"Error al asignar valor");
				 }catch(NotValidValueException ex){
					 ex.printStackTrace();
					 //setText(oldVal);
					 setIdoRowEditing(oldIdoRowEditing);
					 setListIdosEditing(oldListIdosEditing);
					 if(oldIdoRowEditing==null){
						 try{
							 m_controlListener.cancelEditionTable(parent.getId(), getIdoRowEditing());
						 } catch (EditionTableException exc) {
							parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),exc,"Error al intentar cancelar la edición de la fila");
						} catch (AssignValueException e) {
							parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),e,"Error al intentar cancelar la edición de la fila");
						}
					 }
					 
					 if(!it.isNullRow()){
						 replaceColumn(columnIndex, idSource, /*oldV*/oldVal, it.isPermanent());
						 it.setColumnIdo(columnIndex, it.getColumnOldIdo(columnIndex));
						 it.setColumnIdto(columnIndex, it.getColumnOldIdto(columnIndex));
					 }
					 lastSetValueSuccess=false;
					 parent.getMessageListener().showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(parent.getTable()));
				 } catch (EditionTableException ex) {
					 ex.printStackTrace();
					 setIdoRowEditing(oldIdoRowEditing);
					 setListIdosEditing(oldListIdosEditing);
					 if(oldIdoRowEditing==null){
						 try{
							 m_controlListener.cancelEditionTable(parent.getId(), getIdoRowEditing());
						 } catch (EditionTableException exc) {
							parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),exc,"Error al intentar cancelar la edición de la fila");
						 } catch (AssignValueException e) {
								parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),e,"Error al intentar cancelar la edición de la fila");
							}
					 }
					 
					 if(!it.isNullRow()){
						 replaceColumn(columnIndex, idSource, /*oldV*//*oldVData*//*oldVal.toString()*/oldVal, it.isPermanent());
						 it.setColumnIdo(columnIndex, it.getColumnOldIdo(columnIndex));
						 it.setColumnIdto(columnIndex, it.getColumnOldIdto(columnIndex));
					 }
					 lastSetValueSuccess=false;
					 if(ex.isNotify())
						 parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),ex,"Error al intentar editar una fila");
				}finally{
					 if(it.isNullRow()){
						 try{
							 //it.setState(RowItem.CREATION_STATE);//Por defecto el estado de una nullRow es creacion, luego TextCellEditor le pone finder si es necesario
							 if(removeNullRow()){
								 addNullRow();
							 }
						 } catch (AssignValueException exc) {
							 parent.getServer().logError(SwingUtilities.getWindowAncestor(parent.getTable()),exc,"Error al intentar restaurar la fila de introducción de datos");
						 }
					 }else it.setState(RowItem.IDLE_STATE);
					 executingSetValue=false;
				 }
			}
		 }
		 /*
		  * field campo= (field)m_moa.get(idForm);
		  * 
		  * int stx=((Integer)columnSintax.get(columnIndex)).intValue();
		  * 
		  * if(!campo.isNullable()){ boolean isNull= false; if(newVal instanceof
		  * itemList) if(((itemList)newVal).getIntId()==0) isNull= true;
		  * if(newVal instanceof String) if(newVal==null ||
		  * ((String)newVal).equals("")) isNull= true;
		  * 
		  * if(isNull){ Singleton.showMessageDialog("La columna " +
		  * campo.getLabel() + " no admite valores nulos"); return; } }
		  */
		 //ArrayList parList = it.columnPar;
		  //Integer obj = /*new Integer((String)*/ (Integer)parList.get(0)/*)*/;
		  //Integer to = /*new Integer((String)*/(Integer) parList.get(1)/*)*/;


//		  try {
//		  m_parentForm.recalculaFunciones();
//		  } catch (SystemException se) {
//		  Singleton.getComm().logError(SwingUtilities.getWindowAncestor(parent.getTable()),se);
//		  return;
//		  } catch (DataErrorException se) {
//		  Singleton.getComm().logError(SwingUtilities.getWindowAncestor(parent.getTable()),se);
//		  return;
//		  } catch (ApplicationException ae) {
//		  if (ae.GetCode() == ApplicationException.CONTEXTO_NO_MAPEADO) {
//		  Singleton.showMessageDialog("PRIMERO DEBE ASIGNAR EL "
//		  + ae.getMessage());
//		  return;
//		  }
//		  } catch (RemoteSystemException re) {
//		  Singleton.getComm().logError(SwingUtilities.getWindowAncestor(parent.getTable()),re);
//		  } catch (CommunicationException ce) {
//		  ce.printStackTrace();
//		  }
	}

	public boolean isColumnSortable(int column) {
		// Note: For TinyTableModel this works fine. You may
		// take another approach depending on your kind of data.
		if(m_rowData.isEmpty()) return false;
		
		Object value=null;
		int i=-1;
		//Buscamos el primer que no sea null ya que si no no podemos saber si los datos de esa columna son Comparable
		while(value==null && i<getRowCount()-1){
			i++;
			value=getValueAt(i, column);
		}
		return (value instanceof Comparable);
	}

	public boolean supportsMultiColumnSort() {
		// We support multi column sort
		return true;
	}

	//TODO m_visibleRowMap no es actualizado con el nuevo orden. Es posible que haya que actualizarlo ya que se usa en el metodo delRow
	@SuppressWarnings("unchecked")
	public void sortColumns(final int[] columns, final int[] sortingDirections, JTable table) {
		
		//Si la primera fila es una fila permanente(favorito) la quitamos para que no moleste al usuario ya que se queda siempre la primera
		if(!m_rowData.isEmpty() && m_rowData.get(0).isPermanent()){
			try {
				removeRow(0,true);
			} catch (AssignValueException e) {
				System.err.println("Error al intentar quitar de la tabla el favorito al ordenar");
				e.printStackTrace();
			}
		}
		
		int[] sr = table.getSelectedRows();
		int[] sc = table.getSelectedColumns();
		int rowIndex = 0;

		Iterator ii = m_rowData.iterator();
		while(ii.hasNext()) {
			RowItem ri=(RowItem)ii.next();
			ri.setIndexOld(ri.getIndex());
		}

		// The sorting part...
		if(columns.length == 0) {
			// The natural order of our data depends on first (Integer) column
			Collections.sort(m_rowData, new Comparator<RowItem>() {
				public int compare(RowItem r1, RowItem r2) {
					// For our data we know that arguments are non-null and are of type Record.					
					Comparable val1 = (Comparable)r1.getColumnData(0);
					Comparable val2 = (Comparable)r2.getColumnData(0);
					if(val1 instanceof String)
						return Constants.languageCollator.compare(val1, val2);
					else return val1.compareTo(val2);
				}
			});
		}else {
			final ArrayList<Boolean> stringColumnWithNumber=new ArrayList<Boolean>();
			//Miramos si siendo esa columna String, todos sus valores son Integer, para así sabe si ordenar esa columna numericamente en vez de alfabeticamente
			for(int i = 0; i < columns.length; i++) {
				Iterator<RowItem> itr=m_rowData.iterator();
				boolean isNumericOfString=true;
				while(itr.hasNext() && isNumericOfString){
					RowItem rowItem=itr.next();
					
					Comparable value=(Comparable)rowItem.getColumnData(columns[i]);
					if(value!=null){
						if(value instanceof String){
							isNumericOfString=Auxiliar.hasDoubleValue((String)value);
						}else{
							isNumericOfString=false;
						}
					}
				}
				stringColumnWithNumber.add(isNumericOfString);
			}
			
			//Creamos el mapa de valores en caso de columnas enumerados ya que tenemos que comparar luego con el label y no con el ido
			final HashMap<Integer,HashMap<Integer,String>> mapValuesPossible=new HashMap<Integer, HashMap<Integer,String>>();
			for(int i = 0; i < columns.length; i++) {
				GTableColumn columna = ((GFormTable) parent.m_objFormField).getColumn(columns[i]);
				if(columna.getValuesPossible()!=null){
					//Para el caso de que sea un enumerado, ya que tenemos que comparar con el label y no con el ido. Machacamos los idos por los label. Esto pasa en las tablas de los formularios, no en las tablas de busquedas.
					Iterator<GValue> itrId = columna.getValuesPossible().iterator();
					HashMap<Integer,String> mapValues=new HashMap<Integer, String>();
					while (itrId.hasNext()) {
						GValue parValue = itrId.next();
						mapValues.put(parValue.getId(), parValue.getLabel());
					}
					mapValuesPossible.put(i,mapValues);
				}
			}
			
			// Multi column sort
			Collections.sort(m_rowData, new Comparator<RowItem>() {
				public int compare(RowItem r1, RowItem r2) {
					// For our data we know that arguments are non-null and are of type Record.
					if(r1.isNullRow())
						return 1;
					else if(r2.isNullRow())
						return -1;
					else if(r1.isPermanent() && !r2.isPermanent())
						return -1;
					else if(r2.isPermanent() && !r1.isPermanent())
						return 1;
					else{
						for(int i = 0; i < columns.length; i++) {
							Comparable val1 = (Comparable)r1.getColumnData(columns[i]);
							Comparable val2 = (Comparable)r2.getColumnData(columns[i]);
							
							int result=0;
							if(val1==null){
								if(val2!=null)
									result=-1;
							}else if(val2==null){
								result=1;
							}else if(val1 instanceof String){
								if(stringColumnWithNumber.get(i)){//Miramos si tenemos que ordenar esta columna numerica o alfabeticamente
									result = Double.valueOf((String)val1).compareTo(Double.valueOf((String)val2));
								}else{
									result = Constants.languageCollator.compare(val1, val2);
								}
							}else{
								
								if(mapValuesPossible.containsKey(i)){
									//Para el caso de que sea un enumerado, ya que tenemos que comparar con el label y no con el ido. Machacamos los idos por los label. Esto pasa en las tablas de los formularios, no en las tablas de busquedas.
									if(mapValuesPossible.get(i).containsKey(val1)){
										val1=mapValuesPossible.get(i).get(val1);
									}
									if(mapValuesPossible.get(i).containsKey(val2)){
										val2=mapValuesPossible.get(i).get(val2);
									}
								}
								
								result = val1.compareTo(val2);
							}
	
							if(result != 0) {
								if(sortingDirections[i] == SORT_DESCENDING)
									return -result;							
								return result;
							}
						}
						return 0;
					}
				}
			});
		}
		// Tell our listeners that data has changed
		fireTableDataChanged();

		// Restore selection
		rowIndex = 0;

		ii = m_rowData.iterator();
		while(ii.hasNext()) {
			RowItem ri=(RowItem)ii.next();
			ri.setIndex(rowIndex++);
		}
		ArrayList temp = (ArrayList)m_rowData.clone();
		Collections.sort(temp, new Comparator<RowItem>() {
			public int compare(RowItem r1, RowItem r2) {
				if(r1.getIndexOld() > r2.getIndexOld()) 
					return 1;
				return -1;
			}
		});
		// Adding one row selection interval after another is probably inefficient.
		for(int i = 0; i < sr.length; i++) {
			int row=((RowItem)temp.get(sr[i])).getIndex();
			table.addRowSelectionInterval(row, row);
		}
		for(int i = 0; i < sc.length; i++) {
			table.addColumnSelectionInterval(sc[i], sc[i]);
		}
	}

	public Vector<GTableRow> getListaFilas() {
		return listaFilas;
	}

	public ArrayList<RowItem> getRowData() {
		return m_rowData;
	}
	
	public boolean isEditing(){
		return idoRowEditing!=null;
	}

	public Integer getIdoRowEditing() {
		return idoRowEditing;
	}

	public void setIdoRowEditing(Integer idoRowEditing) {
		this.idoRowEditing = idoRowEditing;
		if(idoRowEditing==null){
			rowCreating=false;
			listIdosEditing=new HashSet<Integer>();
		}
	}
	
	public boolean isCreating(){
		return rowCreating;
	}

	public HashSet<Integer> getListIdosEditing() {
		return listIdosEditing;
	}

	public void setListIdosEditing(HashSet<Integer> listIdosEditing) {
		this.listIdosEditing = listIdosEditing;
	}
	
	public boolean isNewIdo(Integer ido){
		boolean newIdo=true;
		if(ido!=null && m_controlListener!=null)
			newIdo=this.m_controlListener.isNewCreation(ido);
		return newIdo;
	}

	public boolean isDirectEdition() {
		return directEdition;
	}

	public void setDirectEdition(boolean directEdition) {
		this.directEdition = directEdition;
	}
	
	//Indica los posibles tipos de ido que podemos tener a partir de un valor de una columna
	public LinkedHashMap<String,Integer> getPossibleTypeForValue(String idParent,Object value,Integer valueCls){
		if(m_controlListener!=null)
			return m_controlListener.getPossibleTypeForValue(idParent, value, valueCls);
		return new LinkedHashMap<String, Integer>();
	}
	
	//Comprueba si esa celda admite nulos dependiendo del individuo que tiene.
	//En el caso de no tener individuo asociado se consulta el generico de la columna
	public boolean isNullable(int row,int col){

		RowItem rowItem = (RowItem) getRowData().get(row);
		Boolean nullable=null;
		if(m_controlListener!=null){
			String idColumn=getFieldIDFromColumn(col);
			IdObjectForm idObjForm=new IdObjectForm(idColumn);
			Integer ido=rowItem.getColumnIdo(col);
			if(ido!=null || idObjForm.getIdParent()!=null){
				Integer idoParent=null;
				String idParent=rowItem.getColumnIdParent(col);
				if(idParent!=null){
					idObjForm=new IdObjectForm(idParent);
					idoParent=idObjForm.getIdo();
				}
				nullable=m_controlListener.isNullableForRow(idoParent,ido, idColumn);//Sera null si ido=null y el idoParent es un filtro
			}
		}
		if(nullable==null){
			CellEditor cell=(CellEditor)parent.getTable().getCellEditor(row,col);
			nullable= cell.getColumn().isNullable();
		}
		
		return nullable;
	}
	
	public void copyInPermanentRow(int row) throws AssignValueException{
		RowItem ritem=getRowItemFromIndex(row);
		
		RowItem permanentRitem=buildRowItem(0/*Queremos que salga el primero*/, ritem.columnData, ritem.columnIdo, ritem.columnOldIdo, ritem.columnIdto, ritem.columnOldIdto, ritem.columnIdParent, ritem.columnIdoFilter, ritem.columnIdtoFilter, Color.black, ritem.getIdRow(), false, true);
		setRowItem(permanentRitem,false);
		//if (!m_cuantitativo)
		//	fireTableRowsInserted(0, 0);//Esto ya se hace en subAddRow
		parent.getTable().removeEditor();//Quitamos el editor ya que si no se queda el editor sobre el boton y no se actualiza su imagen
	}
	
	public Integer getColumnSelectionRowTable() {
		return columnSelectionRowTable;
	}

	public boolean isLastSetValueSuccess() {
		return lastSetValueSuccess;
	}

	public boolean isExecutingSetValue() {
		return executingSetValue;
	}
}

class DefaultTableFieldModel /* implements field */{
	int m_tm;

	String m_idform;

	boolean m_nullable = false;

	String m_label;

	int m_tapos = 0;

	public DefaultTableFieldModel(int tm, String idForm, String label, int tapos, boolean nullable) {
		m_tm = tm;
		m_idform = idForm;
		m_nullable = nullable;
		m_label = label;
		m_tapos = tapos;
	}

	public int getSintax() {
		return m_tm;
	}

	public void setValue(Object value) {
		;
	}

	public Object getValue() {
		return null;
	}

	public int getIntValue() {
		return 0;
	}

	public float getFloatValue() {
		return 0;
	}

	public String getIdForm() {
		return m_idform;
	}

	public int getTAPOS() {
		return m_tapos;
	}

	public String getValueToString() {
		return null;
	}

//	public org.jdom.Element getAva() {
//	return null;
//	}

	public boolean isNull() {
		return false;
	}

	public boolean isNull(Object val) {
		return false;
	}

	public boolean isNullable() {
		return m_nullable;
	}

	public void resetRestriction() {
	}

	public void reset() {
	}

	public boolean hasChanged() {
		return false;
	}

	public String getLabel() {
		return m_label;
	}

	public void commitValorInicial() {
	}

	public void inizialiceRestriction() {
	}

}