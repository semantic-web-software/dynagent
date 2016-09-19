package dynagent.gui.tasks;

import dynagent.common.communication.flowAction;
import dynagent.common.knowledge.instance;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;


public class contentModel extends AbstractTableModel{

	private static final long serialVersionUID = 1L;
	ArrayList<ArrayList<String>> rowData= new ArrayList<ArrayList<String>>();
	ArrayList<instance> taskData= new ArrayList<instance>();
	ArrayList<Integer> taskIndex= new ArrayList<Integer>();
	int columnNumber=0;

	contentModel( int cols ){
		super();
		columnNumber= cols;
	}

	public String getColumnName(int col) {
		return null;
	}

	public int getRowCount() { return rowData.size(); }

	public int getColumnCount() { return columnNumber; }

	public Object getValueAt(int row, int col) {
		ArrayList colData= (ArrayList)rowData.get(row);
		return colData.get(col);
	}

	public /*flowAction*/instance getTaskData(int row) {
		if( taskData.size()>row && row>=0)
			return taskData.get(row);
		else
			return null;
	}

	public Integer getIdoUserTask(int row){
		if( taskIndex.size()>row && row>=0)
			return taskIndex.get(row);
		else
			return null;
	}

	public boolean hasTask( int currTask ){
		return taskIndex.indexOf( new Integer( currTask ) )!=-1;
	}

	public int getTaskIndex( int currTask ){
		//System.err.println("TaskIndex:"+taskIndex.indexOf( new Integer( currTask ) ));
		return taskIndex.indexOf( new Integer( currTask ) );
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}

	public void setValueAt(String value, int row, int col) {
		ArrayList<String> colData= (ArrayList<String>) rowData.get(row);
		colData.set(col,value);
		fireTableCellUpdated(row, col);
	}

	void removeAllTasks(){
		int size= rowData.size();
		rowData.clear();
		taskData.clear();
		taskIndex.clear();
		fireTableRowsDeleted( 0, size );
	}

	public void addRow( instance inst,/*flowAction pt,*/ ArrayList<String> data ){
		//System.out.println("SIZE:"+rowData.size());
		rowData.add( data );
		//System.out.println("SIZEPOST:"+rowData.size());
		taskData.add( /*pt*/inst );
		taskIndex.add( new Integer( /*pt.getCurrTask()*/inst.getIDO() ));
		fireTableRowsInserted( rowData.size()-1, rowData.size()-1 );
	}

	public void addRow( int idoUserTask, ArrayList<String> data ){
		//System.out.println("SIZE:"+rowData.size());
		rowData.add( data );
		//System.out.println("SIZEPOST:"+rowData.size());
		taskIndex.add(idoUserTask);
		fireTableRowsInserted( rowData.size()-1, rowData.size()-1 );
	}

	public void delRow( flowAction pp ){
		delRow(pp.getCurrTask() );
	}
	public void delRow( int currTask ){
		int index= taskIndex.indexOf( new Integer( currTask ) );
		if( index!=-1 ){
			rowData.remove( index );
			taskData.remove( index );
			taskIndex.remove(index);
			fireTableRowsDeleted( index, index );
		}
	}
}
