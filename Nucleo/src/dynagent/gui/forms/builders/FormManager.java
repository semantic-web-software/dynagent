package dynagent.gui.forms.builders;//14-10-02 11:00 JOB -> V1
//15-10-02 JOB ->V2 Añado botones al form

import gdev.gawt.GButton;
import gdev.gawt.GComponent;
import gdev.gawt.GSimpleForm;
import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gbalancer.GProcessedForm;
import gdev.gen.AssignValueException;
import gdev.gen.GConst;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gen.IDictionaryFinder;
import gdev.gfld.GTableRow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
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
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.Property;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.RowItem;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;


public class FormManager{

	HashMap<String,IComponentData> m_componentsData;
	public HashMap<String,GTable> m_tables= new HashMap<String, GTable>();
	JButton m_okBoton=null;
	// almacena punteros a componentes que almacenan datos
	ArrayList<GComponent> m_formComponents=new ArrayList<GComponent>();
	HashMap<String, GButton> m_formButtons=new HashMap<String, GButton>();

	HashMap m_condicionantes_List;
	HashMap m_conditionFunctionValues;
	Object  m_control;
	boolean m_modoCreation= false,m_modoFilter=false;
	String m_targetType;
	/*DefaultSession m_sesion;*/
	int m_action;
	int m_id;
	int m_flowHgap=0;
	int m_flowVgap=0;
	boolean m_modoConsultar;
	boolean m_inicializacion = true;

	Font m_defaultFont=null,m_boldFont;


	// int cellPadding=40, labelPadding=20, verticalPadding=5;
	int cellPadding=0, labelPadding=0, verticalPadding=0;
	KeyListener m_keyListener=null;

	Dimension m_dimForm;
	boolean m_popup;

	JPanel m_component;
	KnowledgeBaseAdapter m_kba;
	private boolean hasScroll=false;
	
	public Integer m_idtoUserTask;
	public Integer m_userRol;
	public Session m_session;
	
	private JScrollPane scrollPane;
	
	public boolean updateColumnWidthWhenBuildRows;//Indica si actualizar el ancho de las columnas de las tablas dependiendo de los datos
	
	private WindowComponent dialog;
	
	private HashMap<String,JTree> mapIdTableTree;//Guarda nodos de insercion que se utilizan como mapa para hacer inserciones en las tablas

	public FormManager(
			IComponentListener controlValue,
			IDictionaryFinder dictionaryFinder,
			Object control,
			int action,
			String targetType,
			ArrayList listaViewForm,
			boolean modoCreation,
			boolean modoFilter,
			boolean modoConsultar,
			boolean popup,
			boolean scroll,
			KeyListener kLis, Dimension preferedSize, KnowledgeBaseAdapter kba, WindowComponent dialog,
			Integer idtoUserTask,Integer userRol,Session session) throws ParseException, AssignValueException{

		/*jdomParser.print("SIMPLEFORM",view);*/

		this.dialog=dialog;
		m_idtoUserTask=idtoUserTask;
		m_userRol=userRol;
		m_session=session;
		m_modoCreation=modoCreation;
		m_modoFilter=modoFilter;
		m_modoConsultar=modoConsultar;
		//System.out.println("MODOS DE FORMMANAGER:"+modoConsultar+" "+modoFilter);

		m_kba=kba;

		m_popup=popup;

		updateColumnWidthWhenBuildRows=true;

		m_action=action;
		m_targetType=targetType;
		m_keyListener=kLis;
		/*m_id= id;*/
		m_control= control;
		//m_defaultFont=new  Font("Dialog",  Font.PLAIN,  12);
		//m_boldFont=new  Font("Dialog",  Font.BOLD,  12);
		/*setFont(m_defaultFont);*/
	
		// setBackground(Color.red);
		/*if( m_colorFondo!=null )
			setBackground(m_colorFondo);*/

		m_componentsData =new HashMap<String, IComponentData>();
		m_condicionantes_List= new HashMap();
		m_conditionFunctionValues= new HashMap();
		/*setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder( new EmptyBorder(0,0,0,0) );*/
		communicator server=kba!=null?kba.getServer():Singleton.getInstance().getComm();
		ComponentFactory factory=new ComponentFactory(/*ses,*//*m_com,m_domCheck,*/controlValue,Singleton.getInstance().getMessagesControl(),dictionaryFinder,m_control,m_keyListener,m_defaultFont,m_modoConsultar,m_popup,m_modoFilter,m_componentsData,/*m_rootSourceModel,m_rootDocModel,m_scope,*/m_action,m_tables,m_formButtons,m_formComponents,m_targetType, dialog!=null?dialog.getComponent():null, server);

		if(listaViewForm.size()>1){
			JTabbedPane tabbedPane=new JTabbedPane();
			Iterator iteratorViewForm=listaViewForm.iterator();
			while(iteratorViewForm.hasNext()){
				GProcessedForm gProcessed= (GProcessedForm)iteratorViewForm.next();
				/*Creamos este panel para que el formulario aparezca centrado ya que, si es mas pequeño
				  que el espacio disponible, aparece en una esquina*/
				JPanel panelTab=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
			
				panelTab.add(new GSimpleForm(gProcessed,factory));
				panelTab.setBorder(UIManager.getBorder("TextField.border"));
				tabbedPane.addTab(gProcessed.getFormFieldList().getTitle(),panelTab);
			}
			m_component=new JPanel();
			m_component.add(tabbedPane);
		}else{
			m_component=new JPanel();
			m_component.setBorder(BorderFactory.createEmptyBorder());
			m_component.setLayout(new BorderLayout());

			if(!scroll/*m_modoFilter*/){
				GSimpleForm gsf = new GSimpleForm((GProcessedForm)listaViewForm.get(0),factory);
				JPanel panelWithCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				panelWithCenter.setBorder(BorderFactory.createEmptyBorder());
				panelWithCenter.add(gsf);
				m_component.add(panelWithCenter, BorderLayout.CENTER);
			}else{
				int ancho=preferedSize.width, alto=preferedSize.height;
				GSimpleForm gsf = new GSimpleForm((GProcessedForm)listaViewForm.get(0),factory);
				
				JPanel panelWithCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				panelWithCenter.setBorder(BorderFactory.createEmptyBorder());
				panelWithCenter.add(gsf);
				
				Dimension dim = panelWithCenter.getPreferredSize();
				scrollPane = new JScrollPane(panelWithCenter);
				scrollPane.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
				scrollPane.setBorder(BorderFactory.createEmptyBorder());
				
				if(dim.width>ancho && dim.height>alto){
					hasScroll=true;
					scrollPane.setPreferredSize(new Dimension(ancho, alto));
				}else if(dim.width>ancho){
					scrollPane.setPreferredSize(new Dimension(ancho, dim.height));
				}else if(dim.height>alto){
					hasScroll=true;
					scrollPane.setPreferredSize(new Dimension(dim.width+scrollPane.getVerticalScrollBar().getPreferredSize().width, alto));
				}else
					scrollPane.setPreferredSize(dim);

				m_component.add(scrollPane, BorderLayout.CENTER);
				//m_component = new GSimpleForm((GProcessedForm)listaViewForm.get(0),factory);
			}
		}
		
		/*m_component.addHierarchyListener(new HierarchyListener() { 
		    public void hierarchyChanged(HierarchyEvent e) { 
		        if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0  
		             && m_component.isShowing()) { 
//		        	System.err.println("CHANGEDDDDDDDDD");
		          //do stuff 
		        } 
		    } 
		}); 
		*/
		
		// Construimos los arboles de mapeo para insercion de filas
		mapIdTableTree=new HashMap<String,JTree>();
		for(String idTable:m_tables.keySet()){
			GTable table=m_tables.get(idTable);
			JTree columnsTree=m_kba.getTreesOfTableColumns(table.getModel().getIdColumns());
			mapIdTableTree.put(idTable, columnsTree);
		}
		
		//System.err.println("mapIdTableTree:"+mapIdTableTree);
		

	}

	public JPanel getComponent(){
		return m_component;
	}

	public void removeSeleccion(String tIndex){
		GTable tf= (GTable)m_tables.get(tIndex);
		if( tf!=null ){
			tf.clearSeleccion();
		}else
			System.err.println("TABLE INDEX NO EXISTE " +tIndex);
	}

	public boolean hasComponent(String id){
		return m_componentsData.containsKey(id);
	}

	public void setValueComponent(String id, Object value, Object valueOld) throws ParseException, AssignValueException{
		//System.out.println("Id cambiado:"+id);
		IComponentData component= m_componentsData.get(id);
		//System.out.println("Componentes registrados:"+m_componentsData.keySet().toString());
		component.setValue(value,valueOld);
	}

	public boolean initValueComponent(String id) throws ParseException, AssignValueException{
		IComponentData component= m_componentsData.get(id);
		if(component!=null){
			component.initValue();
			return true;
		}else return false;
	}
	
	public Object getValueComponent(String id){
		Object value=null;
		//System.out.println("Id cambiado:"+id);
		IComponentData component= m_componentsData.get(id);
		//System.out.println("Componentes registrados:"+m_componentsData.keySet().toString());
		value=component.getValue();
		return value;
	}

	public ArrayList<String> getIdTables(){
		ArrayList<String> list=new ArrayList<String>();
		Set<String> tables=m_tables.keySet();
		if(tables!=null)
			list.addAll(tables);

		return list;
	}

	public int[] getIndexSelectedRows( String idTabla ){
		/*TableForm tf= (TableForm)m_tables.get(new Integer(idTabla));*/
		GTable tf= (GTable)m_tables.get(idTabla);
		return tf.getTable().getSelectedRows();
	}

	public void setRows(String tIndex,Object source) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		/*TableForm tf= (TableForm)m_tables.get(new Integer(tIndex));
        TableFormModel tfm= (TableFormModel)tf.getModel();*/
		GTable tf= (GTable)m_tables.get(tIndex);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.removeData(false);
		/*tfm.BuildData(table,false);*/

		buildRows(tf,tfm,source,false,false);
	}

	public void addTableFocusListener( String idTabla, FocusListener fl){
		GTable tf= (GTable)m_tables.get(idTabla);
		tf.getTable().addFocusListener( fl );
		//System.out.println("AddTableFocusListener idTabla:"+idTabla+" "+fl+" "+m_tables+" "+tf.getClass().hashCode()+" "+tf.getModel().getColumnCount());  
	}

	public void addRows(/*  ArrayList dataRows, ArrayList
											 parRows
											 ,*/String idTable,Object source, boolean replace) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		//System.out.println("AddRows:"+idTable);
		//System.out.println("AddRows mTables:"+m_tables);
		//System.err.println("AddRows "+source);
		GTable tf= (GTable)m_tables.get(idTable);
		GTableModel tfm= (GTableModel)tf.getModel();

		buildRows(tf,tfm,source,replace,false);

	}

	/*public void setSelected(String idButton, boolean state){
		AbstractButton boton= (AbstractButton)m_formButtoms.get(idButton);
		boton.setSelected(state);
	}

	public boolean isSelected(String idButton){
		AbstractButton boton= (AbstractButton)m_formButtoms.get(idButton);
		return boton.isSelected();
	}*/

	public void setStateTableDataFilter(String idtable, boolean state){
		GTable tf= (GTable)m_tables.get(idtable);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.setStateDataFilter(state);
	}

	public void setDataTableFilter(String idTable, selectData table){
		// 3/04/06 modificado
		GTable tf= (GTable)m_tables.get(idTable);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.setDataFilter(table,false);
	}


	private void buildRows(GTable tf,GTableModel tfm,Object source, boolean replace, boolean permanent) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		if( source==null ) return;
		
		Vector<Object> listRow=null;
		
		if(source instanceof selectData){
			selectData selectD=(selectData)source;
			listRow=new Vector<Object>(selectD.size());
	//		System.err.println("**************** Comienzo de todo**********"+System.currentTimeMillis());
			Iterator iRow = selectD.getIterator();
	//		int row = tfm.getRowCount();
			//boolean hasRows=iRow.hasNext();
			
			ArrayList<String> columns=tfm.getIdColumns();
			
			LinkedHashMap<String,SelectQuery> mapSelectIdColumn=null;
			if(selectD.getSelect()!=null){
				mapSelectIdColumn=new LinkedHashMap<String, SelectQuery>();
				for(String idColumn:columns){
					if(IdObjectForm.matchFormat(idColumn)){
						IdObjectForm idObjForm=new IdObjectForm(idColumn);
						SelectQuery select=new SelectQuery(""+idObjForm.getIdo(),idObjForm.getIdProp(),idObjForm.getFilterIdProp(),idObjForm.getFilterValue());
						mapSelectIdColumn.put(idColumn,select);
					}else{
						mapSelectIdColumn.put(idColumn,null);//La columna 'Type' no tienen selectQuery. Pero el null nos sirve para acceder a ella
					}
				}
			}
					
			while (iRow.hasNext()) {
				instance eRow = (instance) iRow.next();
				//System.err.println("instance:"+eRow);
				//int idObject = eRow.getIDO();
				
				//Si viene algun resultado de una clase excluida lo descartamos
				if(m_kba.getClass(eRow.getIdTo())==null){
					System.err.println("buildRows. Excluido ido:"+eRow.getIDO()+" idto:"+eRow.getIdTo());
					continue;
				}
				
				GTableRow tableRow=null;
				if(mapSelectIdColumn!=null)
					tableRow=m_kba.buildTableRow(eRow, mapSelectIdColumn, m_idtoUserTask);
				else tableRow=m_kba.buildTableRow(eRow, columns, m_idtoUserTask);
				
				tableRow.setPermanent(permanent);
				//System.err.println("**************** Antesss 3**********"+System.currentTimeMillis());
				//tfm.buildRowData(tableRow,replace);
				listRow.add(tableRow);
				
				//System.err.println("**************** Antesss 4**********"+System.currentTimeMillis());
				//Si no se trata de un permanente(favorito) actualizamos su valor si este se encuentra en la tabla
				if(!permanent && getPermanentDataTableFromIdo(tf.getId(), eRow.getIDO())!=null){
					tableRow.setPermanent(true);
					tfm.setTableRow(tableRow,true);
					tableRow.setPermanent(false);//Volvemos a ponerlo a false ya que se ha modificado tambien en la listRow
				}
				//System.err.println("**************** Despuess 4**********"+System.currentTimeMillis());
			}
		}else{
			listRow=new Vector<Object>();
			HashMap<Integer,RowItem> idoRowItem=new HashMap<Integer, RowItem>(); 
			HashMap<Integer, HashMap<Integer,ArrayList<Object>>> idoRowItemColumnValues=new HashMap<Integer, HashMap<Integer,ArrayList<Object>>>(); 
			int index=0;
			for(RowItem rowItem:(ArrayList<RowItem>)source){
				ArrayList<Object> columnData=rowItem.getColumnData();
				Object data=columnData.get(columnData.size()-1);
				if(data instanceof Integer){//De base de datos siempre viene la ultima columna con el idto, la sustituimos por el nombre de la clase ya que si aparece en una tabla abstracta eso es lo que se muestra en la columna tipo
					String classLabel=m_kba.getLabelClass((Integer)data,m_idtoUserTask);
					if(classLabel==null) continue;//debe ser una clase excluida, me salto el row
					rowItem.setColumnData(columnData.size()-1, m_kba.getLabelClass((Integer)data,m_idtoUserTask));
				}
				if(idoRowItem.containsKey(rowItem.getIdRow().getIdo())){
					//Si esa fila esta repetida concatenamos los valores separandolos por #
					
					RowItem rowItemProcessed=idoRowItem.get(rowItem.getIdRow().getIdo());
					if(!idoRowItemColumnValues.containsKey(rowItem.getIdRow().getIdo())){
						HashMap<Integer,ArrayList<Object>> colValues=new HashMap<Integer, ArrayList<Object>>();
						idoRowItemColumnValues.put(rowItem.getIdRow().getIdo(), colValues);
					}
					int size=rowItem.getColumnSize();
					for(int i=0;i<size;i++){
						Object value=rowItem.getColumnData(i);
						
						HashMap<Integer,ArrayList<Object>> colValues=idoRowItemColumnValues.get(rowItem.getIdRow().getIdo());
						if(colValues.get(i)==null){
							ArrayList<Object> values=new ArrayList<Object>();
							values.add(rowItemProcessed.getColumnData(i));
							colValues.put(i, values);
						}
							
						if(/*value instanceof String && */!colValues.get(i).contains(value)/*El valor tiene que ser distinto*/){
							rowItemProcessed.setColumnData(i, rowItemProcessed.getColumnData(i)+" # "+value);
							colValues.get(i).add(value);
						}
					}
				}else{
					//Tenemos que cambiar el index e indexold ya que viene desde el server relleno y podemos haber concatenado alguna fila, por lo que habría huecos en estos valores, si no lo cambiamos, siendo un problema en Elecom
					rowItem.setIndex(index);
					rowItem.setIndexOld(index);
					idoRowItem.put(rowItem.getIdRow().getIdo(), rowItem);
					listRow.add(rowItem);
					index++;
				}
			}
			idoRowItem.clear();
			idoRowItemColumnValues.clear();
		}
		
		tfm.buildRows(listRow, replace);
		/*if(m_modoFilter)
			calculateWidthColum(tfm.getTable());*/
		//if(tf.getTable().getRowCount()<200)//Solo recalculamos el ancho de las columnas cuando son pocos registros ya que si no tarda demasiado
		if(updateColumnWidthWhenBuildRows){	
			//tfm.updateColumnWidths();
		}
		//System.err.println("**************** Final de todo**********"+System.currentTimeMillis());
	}
	
	/*private void calculateWidthColum(final JTable table) {
		ArrayList<Integer> tams = new ArrayList<Integer>();
		for (int i=0;i<table.getColumnCount();i++){
		TableColumn colum=table.getColumnModel().getColumn(i);
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


//	private SelectQuery getSelectQuery(Property property,ArrayList<SelectQuery> listSelect) throws NotFoundException, NumberFormatException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException{
//		Iterator<SelectQuery> itr=listSelect.iterator();
//		while(itr.hasNext()){
//			SelectQuery select=itr.next();
//			if(select.getIdProp()==property.getIdProp()){
//				String idObject=select.getIdObject();
//				int idto=m_kba.getClass(Integer.parseInt(idObject));
//				if(idto==property.getIdto()){
//					return select;
//				}else{
//					Iterator<Integer> itrParents=m_kba.getAncestors(property.getIdto());
//					while(itrParents.hasNext()){
//						if(itrParents.next()==idto)
//							return select;
//					}
//				}
//			}
//		}
//		return null;
//	}


	public void replaceRows(String tIndex, Object source) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		GTable tf= (GTable)m_tables.get(tIndex);
		if( tf==null ) return;
		GTableModel tfm= (GTableModel)tf.getModel();
		/*     tfm.BuildData(table,true);*/
		buildRows(tf,tfm,source,true,false);

	}

	public void replaceColumn( String tableIndex, int column, int par1, String newVal ){
		GTable tf= (GTable)m_tables.get(tableIndex);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.replaceColumn(column, par1, newVal, false);
	}

	public void delRow(String tIndex, GIdRow idRow) throws AssignValueException{
		ArrayList<GIdRow> list=new ArrayList<GIdRow>();
		list.add(idRow);
		delRows(tIndex, list);
	}
	
	public void delRows(String tIndex, ArrayList<GIdRow> table) throws AssignValueException{
		GTable tf= (GTable)m_tables.get(tIndex);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.delRows(table,false);
	}

	public void delRow(String tId, int row) throws AssignValueException{
		GTable tf= (GTable)m_tables.get(tId);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.delRow(row);
	}

	public void selectRow( String tableIndex, int ido ){
		GTable tf= (GTable)m_tables.get(tableIndex);
		tf.selectRow(ido,false);
	}
	
	public void selectAllRows( String tableIndex, boolean select){
		GTable tf= (GTable)m_tables.get(tableIndex);
		tf.selectAll(select);
	}
	
	public boolean isSelectAllRows(String tableIndex){
		GTable tf= (GTable)m_tables.get(tableIndex);
		return tf.isSelectAll();
	}

	public void deleteAllRows(String tableIndex) throws AssignValueException{
		GTable gt= (GTable)m_tables.get(tableIndex);
		GTableModel gtm= (GTableModel)gt.getModel();
		/*for(int i=gtm.getRowCount()-1;i>=0;i--){
			gtm.delRow(i);	
		}*/
		gtm.removeData(false);
	}
	
	public RowItem getNextRow( String tableIndex){
		GTable tf= (GTable)m_tables.get(tableIndex);
		return tf.getNextRow();
	}

	public RowItem getPrevRow( String tableIndex){
		GTable tf= (GTable)m_tables.get(tableIndex);
		return tf.getPrevRow();
	}

	public void addTableSelectionListener(String tableId, ListSelectionListener lis){
		// devuelvo un ArrayList donde
		GTable tf= (GTable)m_tables.get(tableId);
		ListSelectionModel lsm= tf.getTable().getSelectionModel();
		lsm.addListSelectionListener( lis );
	}

	public void addTableMouseListener(String tableId, MouseListener lis){
		// devuelvo un ArrayList donde
		GTable tf= (GTable)m_tables.get(tableId);
		tf.getTable().addMouseListener( lis );
	}

	public void removeTableMouseListener(String tableId){
		// devuelvo un ArrayList donde
		GTable tf= (GTable)m_tables.get(tableId);
		tf.getTable().removeMouseListener(tf.getTable().getMouseListeners()[0]);
	}
	
	public GTable getTable(String tableId){
		return (GTable)m_tables.get(tableId);
	}
	
	public GIdRow getDataTableFromIndex(String idTable, int rowIndex){
		// devuelvo un vector donde
		GTable tf= (GTable)m_tables.get(idTable);
		return tf.getDataFromIndex(rowIndex);
	}

	public GIdRow getDataTableFromIdo(String idTable, int ido){
		// devuelvo un vector donde
		GTable tf= (GTable)m_tables.get(idTable);
		return tf.getDataFromIdo(ido,false);
	}
	
	public RowItem getCompletedDataTableFromIndex(String idTable, int rowIndex){
		// devuelvo un vector donde
		GTable tf= (GTable)m_tables.get(idTable);
		return tf.getCompletedDataFromIndex(rowIndex);
	}

	public ArrayList<GIdRow> getIdRowsData( String tindex, Boolean permanent ){
		GTable tf= (GTable)m_tables.get(tindex);
		return tf.getIdRowsData(permanent);
	}

	public boolean supportTable( String tableIndex ){
		return m_tables.containsKey(tableIndex);
	}

	public ArrayList<GIdRow> getIdRowsSelectionData( String tindex ){
		GTable tf= (GTable)m_tables.get(tindex);
		return tf.getIdRowsSelectionData();
	}

	public boolean selectionIsGroup( String tIndex ){
		//System.out.println("Se le pasa el id:"+tIndex);
		//System.out.println("Esta la tabla1:"+m_tables.keySet().iterator().next());
		//System.out.println("Esta la tabla1:"+m_tables.keySet().iterator().toString());
		GTable tf= (GTable)m_tables.get(tIndex);
		return tf.selectionIsGroup();
	}

	public int getSelectedRowCount(String idTable){
		GTable tf= (GTable)m_tables.get(idTable);
		return tf.getTable().getSelectedRowCount();
	}

	public int getRowCount(String idTable){
		int count=0;
		GTable tf= (GTable)m_tables.get(idTable);
		if( tf!=null ){
			count= tf.getRowCount();
		}
		return count;
	}

	public boolean estateInicialization(){
		return m_inicializacion;
	}

	/**
	 * Ordena por la columna que contenga el rdn del objeto principal. Si no lo tiene ordena por la primera columna
	 * @param tableId identificador de la tabla
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 */
	public void orderRows(String tableId) throws NotFoundException, IncoherenceInMotorException{
		GTable tf= (GTable)m_tables.get(tableId);
		GTableModel tfm= (GTableModel)tf.getModel();
		Integer columnToOrder=null;
		ArrayList<String> list=tfm.getIdColumns();
		Integer orderNumberIdProp=m_kba.getIdProp(Constants.PROP_ORDER_NUMBER);
		if(orderNumberIdProp!=null){
			//Buscamos si existe la columna que contenga el number_orden del objeto principal
			for(int i=0;i<list.size() && columnToOrder==null;i++){
				String id=list.get(i);
				if(IdObjectForm.matchFormat(id)){
					IdObjectForm idObjectForm=new IdObjectForm(id);
					if(idObjectForm.getIdParent()==null && idObjectForm.getIdProp().equals(orderNumberIdProp)){
						columnToOrder=i;
					}
				}
			}
		}
		
		//Buscamos si existe la columna que contenga el rdn del objeto principal
//		for(int i=0;i<list.size() && columnToOrder==null;i++){
//			String id=list.get(i);
//			if(IdObjectForm.matchFormat(id)){
//				IdObjectForm idObjectForm=new IdObjectForm(id);
//				if(idObjectForm.getIdParent()==null && idObjectForm.getIdProp().equals(Constants.IdPROP_RDN)){
//					columnToOrder=i;
//				}
//			}
//		}
		
		//Si no existe el rdn nos quedamos con la primera columna
		if(columnToOrder==null){
			if(tfm.getColumnSelectionRowTable()!=null){
				columnToOrder=1;//Si existe el checkBox de selección de fila ordenamos por la 1 porque dicho checkbox esta en la 0
			}else{
				columnToOrder=0;
			}
		}
		tfm.orderRows(columnToOrder);
	}
	
	public boolean isHasScroll() {
		return hasScroll;
	}

	public JViewport getViewPort(){
		if(scrollPane!=null){
			return scrollPane.getViewport();
		}
		return null;
	}

	public ArrayList<GComponent> getFormComponents() {
		return m_formComponents;
	}
	
	public void cleanAll() throws AssignValueException, ParseException{
		Iterator<String> itr=m_tables.keySet().iterator();
		while(itr.hasNext()){
			GTable gt= (GTable)m_tables.get(itr.next());
			GTableModel gtm= (GTableModel)gt.getModel();
			gtm.clean();
		}
		
		Iterator<IComponentData> itrComponents=m_componentsData.values().iterator();
		while(itrComponents.hasNext()){
			IComponentData comp=itrComponents.next();
			comp.clean();
		}
	}
	
	public void addPermanentRows(String idTable,Object source, boolean replace) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		GTable tf= (GTable)m_tables.get(idTable);
		GTableModel tfm= (GTableModel)tf.getModel();

		buildRows(tf,tfm,source,replace,true);
	}
	
	public void replacePermanentRows(String tIndex, Object source) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		GTable tf= (GTable)m_tables.get(tIndex);
		if( tf==null ) return;
		GTableModel tfm= (GTableModel)tf.getModel();
		/*     tfm.BuildData(table,true);*/
		buildRows(tf,tfm,source,true,false);

	}

	public void replacePermanentColumn( String tableIndex, int column, int par1, String newVal ){
		GTable tf= (GTable)m_tables.get(tableIndex);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.replaceColumn(column, par1, newVal, true);
	}

	public void delPermanentRows(String tIndex, ArrayList<GIdRow> table) throws AssignValueException{
		GTable tf= (GTable)m_tables.get(tIndex);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.delRows(table,true);
	}
	
	public void setPermanentRows(String tIndex,Object source) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		/*TableForm tf= (TableForm)m_tables.get(new Integer(tIndex));
        TableFormModel tfm= (TableFormModel)tf.getModel();*/
		GTable tf= (GTable)m_tables.get(tIndex);
		GTableModel tfm= (GTableModel)tf.getModel();
		tfm.removeData(true);
		/*tfm.BuildData(table,false);*/

		buildRows(tf,tfm,source,false,true);
	}

	public void deleteAllPermanentRows(String tableIndex) throws AssignValueException{
		GTable gt= (GTable)m_tables.get(tableIndex);
		GTableModel gtm= (GTableModel)gt.getModel();
		/*for(int i=gtm.getRowCount()-1;i>=0;i--){
			gtm.delRow(i);	
		}*/
		gtm.removeData(true);
	}
	
	public void selectPermanentRow( String tableIndex, int ido ){
		GTable tf= (GTable)m_tables.get(tableIndex);
		tf.selectRow(ido,true);
	}
	
	public GIdRow getPermanentDataTableFromIdo(String idTable, int ido){
		// devuelvo un vector donde
		GTable tf= (GTable)m_tables.get(idTable);
		return tf.getDataFromIdo(ido,true);
	}
	
	public void showAsPermanentRows(String idTable,int row) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		GTable tf= (GTable)m_tables.get(idTable);
		GTableModel tfm= (GTableModel)tf.getModel();

		tfm.copyInPermanentRow(row);
	}
	
	public ArrayList<SelectQuery> buildSelectQueryOfTable(String idTable){
		GTable table=m_tables.get(idTable);
		ArrayList<SelectQuery> list=new ArrayList<SelectQuery>();
		ArrayList<String> columns=table.getModel().getIdColumns();
		int i=0;
		for(String idColumn:columns){
			if(IdObjectForm.matchFormat(idColumn)){
				IdObjectForm idObjForm=new IdObjectForm(idColumn);
				SelectQuery select=new SelectQuery(""+idObjForm.getIdo(),idObjForm.getIdProp(),idObjForm.getFilterIdProp(),idObjForm.getFilterValue());
				select.setAlias(table.getModel().getColumnName(i));
				list.add(select);
			}
			i++;
		}
		
		return list;
	}
	
	public JTree getColumnTreeOfTable(String idTable){
		return this.mapIdTableTree.get(idTable);
	}
}

