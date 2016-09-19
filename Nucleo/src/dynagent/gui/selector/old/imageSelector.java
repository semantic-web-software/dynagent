package dynagent.gui.selector.old;

import javax.swing.*;
import java.util.ArrayList;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.BorderLayout;

import java.awt.event.ActionListener;

import dynagent.knowledge.instance.*;
import dynagent.ejb.helperConstant;

import java.util.Iterator;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;

class imageSelectorModel extends AbstractTableModel{
    ArrayList rowData = new ArrayList();
    int columnNumber = 0;

    imageSelectorModel(int cols) {
	columnNumber = cols;
    }
    public String getColumnName(int col) {
	return null;
    }
    public int getRowCount() {
	return rowData.size();
    }
    public int getColumnCount() {
	return columnNumber;
    }
    public Object getValueAt(int row, int col) {
	ArrayList colData = (ArrayList) rowData.get(row);
	return colData.get(col);
    }
    public boolean isCellEditable(int row, int col) {
	return false;
    }
    public void setValueAt(Object value, int row, int col) {
	ArrayList colData = (ArrayList) rowData.get(row);
	colData.set(col, value);
	fireTableCellUpdated(row, col);
    }
}

class imageSelectorRenderer extends JLabel
	implements TableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
	    Object color,
	    boolean isSelected,
	    boolean hasFocus,
	    int row,
	    int column) {
	Color newColor = (Color)color;
	setBackground(newColor);
	TableModel tm= table.getModel();
	instance ins=(instance)tm.getValueAt(row,column);
/*	Iterator itr= ins.getAttIterator(false,false);
	while(itr.hasNext()){
            attribute att = (attribute) itr.next();
            if (att.getMemberType() == helperConstant.TM_IMAGEN)
                return new JLabel((ImageIcon)att.getValue());
        }
*/	/*if (isBordered) {
	    if (isSelected) {
		//selectedBorder is a solid border in the color
		//table.getSelectionBackground().
		setBorder(selectedBorder);
	    } else {
		//unselectedBorder is a solid border in the color
		//table.getBackground().
		setBorder(unselectedBorder);
	    }
	}*/
	return this;
    }
}

public class imageSelector extends JPanel implements ActionListener{
    JTable table=null;

    imageSelector( ArrayList values ){
	Dimension dim= Toolkit.getDefaultToolkit().getScreenSize();
	int border=10,altoBotones=30;
	long maxCellArea= (dim.width-border*2)*(dim.height-border*2-altoBotones)/values.size();
	int imgHeight= Math.max(100,(int)Math.pow(maxCellArea*3/4,0.5));
	int imgWidth= imgHeight*4/3;
        long area= imgHeight*imgWidth*values.size();
	int tableHeight= (int)Math.pow(area*3/4,0.5);
	int rows= tableHeight/imgHeight;
	int tableWidth=tableHeight*4/3;
	int cols= tableWidth/imgWidth;
	imageSelectorModel imm= new imageSelectorModel(cols);
	table= new JTable(imm);
	imageSelectorRenderer imr= new imageSelectorRenderer();

	TableColumnModel tcm= table.getColumnModel();
	for( int c=0; c<cols;c++ ){
	    TableColumn tc= new TableColumn();
	    tc.setCellRenderer(imr);
	    tcm.addColumn(tc);
	}
	buildValues( values, cols );
	BorderLayout bl= new BorderLayout();
	setLayout(bl);
	add( table, BorderLayout.NORTH );
	JPanel botones= new JPanel();
	JButton ok= new JButton("ACEPTAR");
	ok.addActionListener(this);
	botones.add( ok );
	JButton cancel= new JButton("CANCELAR");
	cancel.addActionListener(this);
	botones.add( cancel );
	add( botones, BorderLayout.SOUTH );
    }

    private void buildValues( ArrayList values, int cols ){
/*     	int row=0, col=0;
	for( int i=0; i<values.size(); i++){
	    instance ins= (instance)values.get(i);
	    Iterator itr= ins.getAttIterator(false,false);
	    while(itr.hasNext()){
		attribute att= (attribute)itr.next();
		if( att.getMemberType()==helperConstant.TM_IMAGEN ){
		    table.getModel().setValueAt(ins, row, col);
		    if (col + 1 >= cols) {
			col = 0;
			row++;
		    } else
			col++;
		    break;
		}
	    }
	}
*/    }

    public void actionPerformed(ActionEvent e) {

    }
}
