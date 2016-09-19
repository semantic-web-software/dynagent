package calendar;
import java.awt.*;
import java.util.Calendar;

import javax.swing.*; 
import javax.swing.table.TableColumn;

public class PanelDay extends JPanel{

	private static final long serialVersionUID = 1L;
	private JPanel panel=null;
	private int alto, ancho;
	private JTable table;
	TableModel m;
	Calendar dates;
	private JTable rowHeader;
	int margin=15;
	int rowHeight;
	private final int border = 1;
		
	public PanelDay(int ancho, int alto, Calendar dates){
		super();
		this.alto=alto;
		this.ancho=ancho;
		this.dates=dates;
		
		JTable t = new JTable(6,7);
		rowHeight=((alto-margin-(int)t.getTableHeader().getPreferredSize().getHeight())/24);
				
		initialize();
	}
	
	public void initialize(){	

		m=new TableModel(24,1, dates, "Day", ancho-50, rowHeight);	
		panel  = new JPanel();
		panel.setLayout(null);
		
		JScrollPane sp = getPanelTabla();
		sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		sp.setBounds(0, 0, ancho, alto-margin);
		panel.add(sp);
		
		setLayout(null);
		panel.setBounds(0, margin, ancho, alto-margin);
		add(panel);		
	}
		
	private JScrollPane getPanelTabla() {	
		
		JScrollPane sp = new JScrollPane(inicializaTabla());
		sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		sp.setRowHeaderView(rowHeader);
		sp.getRowHeader().setPreferredSize(new Dimension(50, rowHeight*24));
		sp.setBorder(BorderFactory.createEmptyBorder());
		
		return sp;		
	}	
	
	private JTable inicializaTabla(){
		
		table = new JTable(m);
		JScrollPaneRenderer rend= new JScrollPaneRenderer();
		rend.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		JScrollPaneEditor edit=new JScrollPaneEditor();
		edit.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		TableColumn tableColumn = table.getColumnModel().getColumn(0);	
		tableColumn.setCellRenderer( rend );
		tableColumn.setCellEditor( edit);				
		
		table.getTableHeader().setBackground(new Color(1.0f, 1.0f, 0.8f));
		table.getTableHeader().setForeground(new Color(0.06f, 0.15f, 0.75f));
		table.getTableHeader().setFont(new Font("helvetica", Font.BOLD, 11));
		table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
				
		table.setRowHeight(rowHeight);		
		table.setBorder(BorderFactory.createEmptyBorder());	
		
		rowHeader = new JTable(24, 1);
		String headers[] = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
		for(int i=0;i<headers.length;i++)
			rowHeader.getModel().setValueAt(headers[i], i, 0);
		rowHeader.setRowHeight(rowHeight);
		rowHeader.getColumnModel().setColumnMargin(20);
		rowHeader.setBackground(new Color(1.0f, 1.0f, 0.8f));
		rowHeader.setForeground(new Color(0.06f, 0.15f, 0.75f));
		rowHeader.setFont(new Font("helvetica", Font.BOLD, 11));
		
		table.getTableHeader().setBorder(BorderFactory.createMatteBorder(border, border, border, border, new Color(0.30f, 0.45f, 0.7f)));
		table.setBorder(BorderFactory.createMatteBorder(0, 0, border, border, new Color(0.30f, 0.45f, 0.7f)));
		rowHeader.setBorder(BorderFactory.createMatteBorder(border, border, border, border, new Color(0.30f, 0.45f, 0.7f)));
		
		return table;
	}
}

