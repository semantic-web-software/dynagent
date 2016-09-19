package calendar;

import java.awt.Component;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.EventObject;
import java.util.LinkedList;

import javax.swing.*;

public class JScrollPaneEditor extends JScrollPane implements TableCellEditor {

	private static final long serialVersionUID = 1L;
	LinkedList<CellEditorListener> l = new LinkedList<CellEditorListener>();
		
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		
		DatesBigCalendar.column=column;
		DatesBigCalendar.row=row;
		
		if(value==null)
    		return null;
    	else
    		return (JComponent)value;
	}
    
    public Object getCellEditorValue(){
    	return null;
    }
    
    public boolean isCellEditable(EventObject anEvent) {
    	return true;
    }
    
    public boolean shouldSelectCell(EventObject anEvent) {
    	return true;
    }
        
    public boolean stopCellEditing() {
    	return true;
	}
    
    public void cancelCellEditing() {
    	//stopCellEditing();
    } 
    
	public void removeCellEditorListener(CellEditorListener c){
		l.remove(c);
	}
	
	public void addCellEditorListener(CellEditorListener c){
		l.add(c);
	}
}