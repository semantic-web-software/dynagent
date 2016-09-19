package calendar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

class TableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 1L;
	private final double pixel = 10;	
	private final int heightBoton = 14;
	
	private Object [][] datos;		
	private String type;
	private Calendar dates;
	private int ancho, alto;
	
	String[] headerD = new String[24];
	String[] headerW = new String[24*7];
	String[] headerM = new String[42];
			
	public TableModel(int fil, int col, Calendar dates, String type, int ancho, int alto){
		this.dates=dates;
		this.type=type;
		this.ancho=ancho;
		this.alto=alto;
		datos = new Object[fil][col];
		for(int row=0;row<fil;row++){
			for(int column=0;column<col;column++){
				if(type.equals("Month")){
					if(!getHeaderMonth(row, column).equals("")){
						ArrayList <Task> tasks =DatesBigCalendar.getEventsDay(getHeaderMonth(row, column), dates.get(Calendar.MONTH)+1, dates.get(Calendar.YEAR));
						
						JPanel p = new JPanel();
						p.setBorder(BorderFactory.createEmptyBorder());
						p.add(createHeader(getHeaderMonth(row, column), "other", null, ancho));
						this.ancho=ancho/7;
						createContent(p, tasks, fil, col);
						
						JScrollPane sp = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
						sp.setBorder(null);
						datos[row][column] = sp;						
						
					}else{
						datos[row][column] = new JPanel();
					}
				}else if(type.equals("Week")){					
					if(!getHeaderWeek(row, column).equals("vacio")){
						String hora;
						if(row<10)
							hora="0"+row;
						else 
							hora=String.valueOf(row);
						
						String s = headerW[column];
						s = s.substring(s.lastIndexOf(" ")+1, s.length());
						if(s.length()==1)
							s = "0"+s;
						ArrayList <Task> tasks = DatesBigCalendar.getEventsHour(s, dates.get(Calendar.MONTH)+1, dates.get(Calendar.YEAR), hora);
						
						JPanel p = new JPanel();
						p.setBorder(null);
						this.ancho=ancho/7;
						createContent(p, tasks, fil, col);
						JScrollPane sp = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						sp.setBorder(null);
						sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
						datos[row][column] = sp;			
					}
				}else if(type.equals("Day")){
					String hora;
					if(row<10)
						hora="0"+row;
					else 
						hora=String.valueOf(row);
					String s = String.valueOf(dates.get(Calendar.DATE));
					if(s.length()==1)
						s = "0"+s;
					ArrayList <Task> tasks = DatesBigCalendar.getEventsHour(s, dates.get(Calendar.MONTH)+1, dates.get(Calendar.YEAR), hora);
					JPanel p = new JPanel();
					p.setBorder(BorderFactory.createEmptyBorder());
					createContentDay(p, tasks, fil, col);
					JScrollPane sp = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					sp.setBorder(null);
					sp.getVerticalScrollBar().setUnitIncrement(Constants.IncrementScrollVertical);
					datos[row][column] = sp;
				}
			}
		}
    }	
	
	private JButton createHeader(String s, String type2, final Boton[] list, final int max){
		if(type2.equals("VER_MAS")){
			
			final JButton b = new JButton();
	        b.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					new Dialog(max, heightBoton*(list.length), list, b);//.getLocationOnScreen());					
				}
			});
	        b.setPreferredSize(new Dimension(ancho, heightBoton));
			b.setMaximumSize(new Dimension(ancho, heightBoton));
			
	        b.setMargin(new Insets(0, 0, 0, 0));
	        b.setFont (new Font ("Showcard Gothic", Font.CENTER_BASELINE, 11));
	        b.setForeground(Color.blue);//new Color(100, 100, 100));
	        b.setContentAreaFilled(false);
	        b.setBorderPainted(false);
	        b.setText(s);
	        
	        return b;
		}else{
			JButton b = new JButton() {	            
				private static final long serialVersionUID = 1L;
				public void addMouseListener(MouseListener l) { }
	            public boolean isFocusable() {
	                return false;
	            }
	        };
	        b.setPreferredSize(new Dimension(ancho, heightBoton));
			b.setMaximumSize(new Dimension(ancho, heightBoton));
			b.setHorizontalAlignment(SwingConstants.LEFT);
			b.setMargin(new Insets(0, 0, 0, 0));
	        b.setFocusPainted(false);
	        b.setForeground(Color.black);
	        b.setContentAreaFilled(false);
	        b.setBorderPainted(false);
	        b.setText(s);
	        
	        return b;
		}
	}
	
	private void createContent(JPanel p, ArrayList<Task> task, int fil, int col){
		p.setBackground(Color.white);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		int numberBotons=-1;
		if(task.size()!=0){
			if(alto<task.size()*heightBoton){
				int tamaño=0, cont=0;
				while(alto>=tamaño){
					tamaño=tamaño+heightBoton;
					cont++;
				}
				numberBotons = cont-2;//alto/(13*a.size());				
			}
		}
		if(numberBotons==-1){
			for(int i=0;i<task.size();i++){
				final Boton b = new Boton(task.get(i).getName());
				if(type.equals("Week")){
					b.setPreferredSize(new Dimension(ancho, alto));
					b.setMaximumSize(new Dimension(ancho, alto));
				}else{
					b.setPreferredSize(new Dimension(ancho, heightBoton));
					b.setMaximumSize(new Dimension(ancho, heightBoton));
				}
				b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						execute(b.getText());						
					}
				});
				p.add(b);
			}
		}else{
			if(type.equals("Month"))
				numberBotons = numberBotons-1;
			for(int i=0;i<numberBotons;i++){
				final Boton b = new Boton(task.get(i).getName());
				b.setPreferredSize(new Dimension(ancho, heightBoton));
				b.setMaximumSize(new Dimension(ancho, heightBoton));
				b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						execute(b.getText());
					}
				});
				p.add(b);
			}
			Boton[] list = new Boton[task.size()];
			for(int i=0;i<task.size();i++){
				final Boton b = new Boton(task.get(i).getName());
				b.setPreferredSize(new Dimension(ancho, heightBoton));
				b.setMaximumSize(new Dimension(ancho, heightBoton));
				b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						execute(b.getText());
					}
				});
				list[i]=b;
			}
			p.add(createHeader("+ Ver todas", "VER_MAS", list, ancho));
		}
	}
	
	private void createContentDay(JPanel p, ArrayList<Task> tasks, int fil, int col){
		p.setBackground(Color.white);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		String todas = "+ Ver todas"; 		
		int numberBotons=-1;
		int ancho2 = 0;
		int max = 0;
		for(int i=0;i<tasks.size();i++){
			if(tasks.get(i).getName().length()>max)
				max = tasks.get(i).getName().length();
			ancho2 += tasks.get(i).getName().length()*pixel;
			
		}
		if(tasks.size()!=0){
			if(ancho<ancho2){
				int tamaño=(int)(todas.length()*pixel), cont=0;
				while(ancho>=tamaño){
					tamaño +=(int)(tasks.get(cont).getName().length()*pixel);
					cont++;
				}
				numberBotons = cont-1;//alto/(13*a.size());				
			}
		}
		if(numberBotons==-1){
			for(int i=0;i<tasks.size();i++){
				final Boton b = new Boton(tasks.get(i).getName());
				b.setPreferredSize(new Dimension((int)(tasks.get(i).getName().length()*pixel), alto));
				b.setMaximumSize(new Dimension((int)(tasks.get(i).getName().length()*pixel), alto));				
				b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						execute(b.getText());						
					}
				});
				p.add(b);
			}
		}else{
			Boton[] list = new Boton[tasks.size()];
			for(int i=0;i<tasks.size();i++){
				final Boton b = new Boton(tasks.get(i).getName());
				b.setPreferredSize(new Dimension((int)(tasks.get(i).getName().length()*pixel), heightBoton));
				b.setMaximumSize(new Dimension((int)(tasks.get(i).getName().length()*pixel), heightBoton));
				b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						execute(b.getText());
					}
				});
				list[i]=b;
			}
			JButton x = createHeader(todas, "VER_MAS", list, (int)(max*pixel));
			p.add(x);
			
			for(int i=0;i<numberBotons;i++){
				final Boton b = new Boton(tasks.get(i).getName());
				b.setPreferredSize(new Dimension((int)(tasks.get(i).getName().length()*pixel), heightBoton));
				b.setMaximumSize(new Dimension((int)(tasks.get(i).getName().length()*pixel), heightBoton));
				b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						execute(b.getText());
					}
				});
				p.add(b);				
			}
		}
	}
	
	private void execute(String text){
		String dia;
		int day=0;
		if(type.equals("Month")){
			day = Integer.parseInt(headerM[DatesBigCalendar.row*7+DatesBigCalendar.column]);	
		}
		else if(type.equals("Week")){
			String s = headerW[DatesBigCalendar.column];
			s = s.substring(s.lastIndexOf(" ")+1, s.length());
			day = Integer.parseInt(s);			
		}else
			day=dates.get(Calendar.DATE);
		
		String sD = String.valueOf(day);
		String sM = String.valueOf(dates.get(Calendar.MONTH)+1);
		if(day<10)
			sD = "0"+sD;
		if((dates.get(Calendar.MONTH)+1)<10)
			sM = "0"+sM;
		
		dia = sD+"/"+sM+"/"+dates.get(Calendar.YEAR);
		
		HashMap<String, ArrayList<Task>> eventos = DatesBigCalendar.getEventos().getEventos();
		
		Task t=null;
		if(eventos.get(dia)!=null){
			ArrayList<Task> res = eventos.get(dia);
			for(int i=0;i<res.size();i++){
				if(res.get(i).getName().equals(text)){
					t=res.get(i);
					break;
				}					
			}
			new FrameDateEvent(dia, t);
		}else
			System.err.println("No existen eventos para: "+dia);
	}	
	
	@SuppressWarnings("unchecked")
	public Class getColumnClass(int columnIndex) {
    	return JScrollPane.class;        
    }
	
	public int getRowCount() {
		return datos.length;		
	}
	
	public int getColumnCount() {
		return datos[0].length;	
	}
	
	public String getColumnName(int dia)
	{		
		String[] columnNames = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
		if(type.equals("Month")){
			return columnNames[dia];
		}else if(type.equals("Week")){
			if(headerW[dia].equals("vacio"))
				return "";
			else
				return headerW[dia];
		}else{
			getHeaderDay();
			return headerD[dia];
		}
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		return datos[rowIndex][columnIndex];
	}

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) 
    {
        datos[rowIndex][columnIndex]=aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    private String getHeaderMonth(int row, int column){
    	int cont=0;
    	Calendar c = (Calendar)dates.clone();
		c.set(Calendar.DATE, 1);
		int first = (c.get(Calendar.DAY_OF_WEEK)-2+7)%7;
		for(int i=0;i<first;i++){
			headerM[cont++]="";
		}
		while(c.get(Calendar.MONTH)==dates.get(Calendar.MONTH)){
			headerM[cont++]=String.valueOf(c.get(Calendar.DATE));
			c.add(Calendar.DATE, 1);
		}
		while(cont<42)
			headerM[cont++]="";
		
		return headerM[row*7+column];
    }
    
    private String getHeaderWeek(int fila, int columna){
    	int day = dates.get(Calendar.DATE);
    	String[] s = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};
    	int cont=0;
    	int dayWeek = (dates.get(Calendar.DAY_OF_WEEK)-2+7)%7;
    	
    	for(int i=dayWeek; i>0; i--){
    		if(day-i<=0)
    			headerW[cont++]="vacio";
    		else
    			headerW[cont++]=s[cont-1]+" "+(day-i);    		
    	}
    	int x=0;
    	Calendar clon = (Calendar)dates.clone();
		while(cont<7){
    		if(clon.get(Calendar.MONTH)!=dates.get(Calendar.MONTH))
    			headerW[cont++]="vacio";
    		else
    			headerW[cont++]=s[cont-1]+" "+(day+x);
    		x++;
    		clon.add(Calendar.DATE, 1);    		
    	}
		for(int y=7;y<24*7;y++){
			if(headerW[y%7].equals("vacio"))
				headerW[y]="vacio";
			else
				headerW[y]="";
		}
		return headerW[fila*7+columna];
	}
	
	private void getHeaderDay(){
		int cont=0;
		String[] s = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};
		int dayWeek = (dates.get(Calendar.DAY_OF_WEEK)-2+7)%7;
    	for(int i=0;i<24;i++){
    		if(i==0)
    			headerD[cont++]=s[dayWeek]+" "+dates.get(Calendar.DATE);
    		else
    			headerD[cont++]="";	
    	}
	}
}