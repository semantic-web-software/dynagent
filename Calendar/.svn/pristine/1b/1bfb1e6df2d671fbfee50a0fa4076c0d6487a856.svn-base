package calendar;
import java.awt.*;
import java.util.Calendar;

import javax.swing.*; 
import javax.swing.table.TableColumn;

public class PanelMonth extends JPanel{

	private static final long serialVersionUID = 1L;
	private JPanel panel=null;
	private int alto, ancho;
	private JTable table;
	TableModel m;
	Calendar dates;	
	private final int margin=15;
	private final int border = 1;
	int rowHeight;
		
	public PanelMonth(int ancho, int alto, Calendar dates){
		super();
		this.alto=alto;
		this.ancho=ancho;
		this.dates=dates;
		
		JTable t = new JTable(6,7);
		rowHeight=((alto-margin-t.getTableHeader().getPreferredSize().height)/6);
		
		initialize();
	}
	
	public void initialize(){		
				
		m=new TableModel(6,7, dates, "Month", ancho, rowHeight);//t.getRowHeight());
		
		panel  = new JPanel();
		panel.setLayout(null);
		

		JScrollPane sp = getPanelTabla();
		sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		sp.setBounds(0, 0, ancho, alto-margin);
		panel.add(sp);


		
		setLayout(null);
		panel.setBounds(0, margin, ancho, alto-margin+border*2);
		add(panel);	
		setBorder(BorderFactory.createEmptyBorder());
	}
		
	private JScrollPane getPanelTabla() {	
		
		JScrollPane sp = new JScrollPane(inicializaTabla());

		sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		sp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0.30f, 0.45f, 0.7f)));	

		sp.setAutoscrolls(false);
		sp.setBounds(0, 0, ancho, alto-margin+border*2);
		sp.setBorder(BorderFactory.createEmptyBorder());
		
		return sp;		
	}	
	
	private JTable inicializaTabla(){
		
		table = new JTable(m);
		JScrollPaneRenderer rend= new JScrollPaneRenderer();
		rend.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		JScrollPaneEditor edit=new JScrollPaneEditor();
		edit.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
		for(int x=0; x<7;x++){
			TableColumn tableColumn = table.getColumnModel().getColumn(x);
			tableColumn.setCellRenderer( rend );
			tableColumn.setCellEditor( edit);				
		}			
		table.getTableHeader().setBackground(new Color(1.0f, 1.0f, 0.8f));
		table.getTableHeader().setForeground(new Color(0.06f, 0.15f, 0.75f));
		table.getTableHeader().setFont(new Font("helvetica", Font.BOLD, 11));
		//table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
		
		table.setRowHeight(rowHeight);
		//table.setBorder(BorderFactory.createEmptyBorder());
		
		table.setBorder(BorderFactory.createMatteBorder(0, border, border, border, new Color(0.30f, 0.45f, 0.7f)));	
		table.getTableHeader().setBorder(BorderFactory.createMatteBorder(border, border, 0, border, new Color(0.30f, 0.45f, 0.7f)));	
						
		return table;
	}
}

