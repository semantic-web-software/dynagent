package dynagent.gui.tasks;

import dynagent.common.communication.flowAction;
import dynagent.common.knowledge.action;
import dynagent.common.knowledge.instance;
import dynagent.common.utils.Utils;

import javax.swing.JTable;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.awt.Rectangle;
import java.awt.Point;

import javax.swing.JViewport;

public class content extends JTable{

	private static final long serialVersionUID = 1L;
	String id;
	taskActionListener mon;

	public content(String id, int columnas, taskActionListener mon ){
		super(new contentModel(columnas));
		this.id=id;
		this.mon= mon;
		setTableHeader( null );
		//ListSelectionModel lsm= getSelectionModel();
		//lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setAutoscrolls(true);
		contentRender r = new contentRender();
		for(int i=0;i<getModel().getColumnCount();i++){
			TableColumn tableColumn = super.getColumnModel().getColumn(i);			
			tableColumn.setCellRenderer(r);
		}
		//setColumnSelectionAllowed(false);
		//setCellSelectionEnabled(false);
		/*setRowSelectionAllowed(true);*/

		/*this.setCellEditor(new TableCellEditor(){

		});*/
	}

	public String getID(){
		return id;
	}

	public void addRow( instance inst,/*flowAction pt,*/ ArrayList<String> datos ){
		((contentModel)getModel()).addRow( inst,/*pt,*/ datos );
	}

	public void addRow( int idoUserTask, ArrayList<String> datos ){
		((contentModel)getModel()).addRow( idoUserTask, datos );
		showCell(getRowCount()-1, 0);
	}

	public void delTask( flowAction pp ){
		((contentModel)getModel()).delRow( pp );
	}
	public void delTask( int currTask ){
		((contentModel)getModel()).delRow( currTask );
	}
	void removeAllTask(){
		((contentModel)getModel()).removeAllTasks();
	}

	public void delProcessInstance( int currentProId ){
//		contentModel cm=(contentModel)getModel();
//		for( int row=getRowCount()-1; row >=0; row--){
//		flowAction pte= cm.getTaskData(row);
//		if(pte.getCurrProcess()==currentProId )
//		cm.delRow(pte);
//		}
	}

	public void selectTask( int currTask ){
		int row= ((contentModel)getModel()).getTaskIndex(currTask);
		if( row!=-1 ){
			setRowSelectionInterval(row,row);
			Object parent= getParent();
			if( parent!=null && parent instanceof JViewport ){
				JViewport vp = (JViewport)parent;
				Rectangle rect = getCellRect(row, 0, true);
				vp.setViewPosition(new Point(rect.x, rect.y));
				revalidate();
				repaint();
			}
		}
	}


	public boolean hasTask( int currTask ){
		return ((contentModel)getModel()).hasTask(currTask);
	}

	public void valueChanged( ListSelectionEvent e ){
//		super.valueChanged(e);
//		mon.taskSelection( this, ((contentModel)getModel()).getTaskData(getSelectedRow()) );
	}

	public static ArrayList buildTaskMsg( /*metaData md,*/ flowAction pp ){
		ArrayList msg= new ArrayList();

		/*taskType taskDef= md.getTask( new Integer( pp.getTaskType()) );
	     String taskName=  taskDef.label;

	     String state= md.getEnumLabel(new Integer(taskDef.taposAtState),
					     pp.getCurrTaskState());

	     long vDate=pp.getExeDate()==0 ? System.currentTimeMillis():pp.getExeDate();
	     String asunto="TAREA "+pp.getCurrTask();
	     msg.add( taskName );
	     msg.add( state );
	     msg.add( asunto );
	     msg.add( DateFormat.getDateInstance().format(new java.util.Date(vDate)) );*/
		return msg;
	}

//	public static ArrayList buildTaskMsg( instance inst ){
//	KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBase();
//	ArrayList msg= new ArrayList();

//	String taskName=kba.getLabelUserTask(inst.getIdTo());
//	//kba.setInstance(inst);
//	int idoUserTask=inst.getIDO();
//	ObjectProperty propTarget=kba.getTarget(idoUserTask, null,null);
//	int idtoUserTask=propTarget.getIdto();//Lo obtenemos de aqui ya que idto que nos viene en el instance de BD es de la utask generica
//	System.out.println("PropTarget:"+propTarget);
//	//DataProperty propRDNTarget=kba.getRDN(kba.getIdoValue(propTarget),null,idtoUserTask,null);

//	//kba.clearInstance();
//	DataProperty propOwner=kba.getField(idoUserTask, Constants.IdPROP_OWNER, null, idtoUserTask, null);
//	String state;
//	if(propOwner.getValueList().isEmpty())
//	state=Utils.normalizeLabel("Preasignada");
//	else state=Utils.normalizeLabel("Asignada");

//	DataProperty propEjecuteDate=kba.getField(idoUserTask, Constants.IdPROP_EJECUTEDATE, null, idtoUserTask, null);
//	String ejecuteDate=kba.getValueData(propEjecuteDate);
//	DataProperty propTopDate=kba.getField(idoUserTask, Constants.IdPROP_TOPDATE, null, idtoUserTask, null);
//	String endDate=kba.getValueData(propTopDate);

//	/*String ejecuteDate=DateFormat.getDateInstance().format(new java.util.Date());
//	String endDate=DateFormat.getDateInstance().format(new java.util.Date());
//	*/

//	msg.add( taskName );
//	msg.add( state );
//	msg.add( ejecuteDate!=null?ejecuteDate:Utils.normalizeLabel("Sin fecha ejecución") );
//	msg.add( endDate!=null?endDate:Utils.normalizeLabel("Sin fecha limite") );
//	return msg;
//	}

	public static ArrayList<String> buildTaskMsg( String labelUserTask, String status, String asignDate, String ejecutionDate ){
		ArrayList<String> msg= new ArrayList<String>();
		msg.add( Utils.normalizeLabel(labelUserTask ));
		msg.add( Utils.normalizeLabel(status) );
		msg.add( asignDate!=null?asignDate:Utils.normalizeLabel("Sin fecha asignación") );
		msg.add( ejecutionDate!=null?ejecutionDate:Utils.normalizeLabel("Sin fecha ejecución") );
		return msg;
	}

	public static ArrayList<String> buildHistoryMsg( String labelClass, String rdn, int operation){
		ArrayList<String> msg= new ArrayList<String>();
		msg.add( Utils.normalizeLabel(labelClass ));
		msg.add( rdn );
		String operationLabel="";
		if(/*operation==action.DEL ||*/ operation==action.DEL_OBJECT)
			operationLabel="Eliminado";
		else if(operation==action.SET)
			operationLabel="Modificado";
		else if(operation==action.NEW)
			operationLabel="Creado";
		msg.add( Utils.normalizeLabel(operationLabel) );
		DateFormat dateFormat=new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		msg.add( dateFormat.format(new Date(System.currentTimeMillis())) );
		return msg;
	}

	public void showCell(int row, int column) {
		Rectangle rect = getCellRect(row, column, true);
		scrollRectToVisible(rect);
		clearSelection();
		setRowSelectionInterval(row, row);
		((contentModel)getModel()).fireTableDataChanged(); // notify the model
	}

}
