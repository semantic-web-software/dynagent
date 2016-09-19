package miniCalendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dynagent.common.communication.docServer;

public class JCalendarRange extends JPanel{

	private static final long serialVersionUID = 1L;
	private JDialog dialog;
	private JCalendar calendarInitial;
	private JCalendar calendarEnd;
	private IDateListener listenerInitial;
	private IDateListener listenerEnd;
	private Component frame, button;
	private Calendar valueCalendarInitial;
	private Calendar valueCalendarEnd;
	private boolean dateInitialSelectioned;
	private boolean dateEndSelectioned;
		
	public JCalendarRange(int type, Component frame, Component button, Calendar initial, Calendar end, IDateListener listenerPrymary, IDateListener listenerSecondary, docServer server) {
		super(new BorderLayout(0,0));
		this.button=button;
		this.frame=frame;
		/*valueCalendarInitialDefault=initial!=null?(Calendar)initial.clone():null;
		valueCalendarEndDefault=end!=null?(Calendar)end.clone():null;*/
		if(initial==null){
			initial = Calendar.getInstance();
			initial.set(Calendar.HOUR_OF_DAY, 0);
			initial.set(Calendar.MINUTE, 0);
			initial.set(Calendar.SECOND, 0);	
		}
		
		if(end==null){
			end = Calendar.getInstance();
			end.set(Calendar.HOUR_OF_DAY, 23);
			end.set(Calendar.MINUTE, 59);
			end.set(Calendar.SECOND, 59);	
		}
				
		if(type==JCalendar.JCalendar){
			calendarInitial=new JCalendar(frame,button,initial,null,false);//listenerPrymary);
			calendarEnd=new JCalendar(frame,button,end,null,false);//listenerSecondary);
		}else if(type==JCalendar.JCalendarDateTime){
			calendarInitial=new JCalendarDateTime(frame,button,initial,null,false,server);//listenerPrymary);
			calendarEnd=new JCalendarDateTime(frame,button,end,null,false,server);//listenerSecondary);
		}
		this.listenerInitial=listenerPrymary;
		this.listenerEnd=listenerSecondary;
	
		calendarInitial.setListener(new IDateListener(){
        	
			public void setDateSelectioned(Calendar calendar,JCalendar calendarComponent) {
				long milliseconds=calendar.getTimeInMillis();
				try{
					Calendar valueCalendarEnd=calendarEnd.getCalendar();
					if(valueCalendarEnd==null || milliseconds<=valueCalendarEnd.getTimeInMillis()){
						if(listenerInitial!=null){
							listenerInitial.setDateSelectioned(calendar,calendarComponent);
						}
						else{
							System.err.println("WARNING: No hay listener para el calendario 'desde'");
						}
					}else{
						WindowListener[] windowListener=dialog.getWindowListeners();
						for(int i=0;i<windowListener.length;i++)
							dialog.removeWindowListener(windowListener[i]);
						//calendarComponent.setListener(null);
						
						JOptionPane.showMessageDialog(calendarInitial, "La fecha 'Desde' no puede ser superior a la fecha 'Hasta'", "Error", JOptionPane.ERROR_MESSAGE);
						//calendarComponent.setCalendar(valueCalendarEnd);
						calendarComponent.setSelectedCalendar(valueCalendarEnd);
						//calendarComponent.notSelect();
						
						//calendarComponent.setListener(this);
						for(int i=0;i<windowListener.length;i++)
							dialog.addWindowListener(windowListener[i]);
						
						
						if(listenerInitial!=null)
							listenerInitial.setDateSelectioned(valueCalendarEnd,calendarComponent);
					}
					dateInitialSelectioned=true;
				}catch(Exception ex){
	        		//m_server.logError(ex,"Error al asignar fecha");
	            	ex.printStackTrace();
	        	}
			}

			public void setAllDate(Calendar c) {
			}
			
		});
		
		calendarEnd.setListener(new IDateListener(){
        	
			public void setDateSelectioned(Calendar calendar,JCalendar calendarComponent) {
				long milliseconds=calendar.getTimeInMillis();
				try{
					Calendar valueCalendarInitial=calendarInitial.getCalendar();
					if(valueCalendarInitial==null || milliseconds>=valueCalendarInitial.getTimeInMillis()){
						if(listenerEnd!=null)
							listenerEnd.setDateSelectioned(calendar,calendarComponent);
						else System.err.println("WARNING: No hay listener para el calendario 'hasta'");
					}else{
						WindowListener[] windowListener=dialog.getWindowListeners();
						for(int i=0;i<windowListener.length;i++)
							dialog.removeWindowListener(windowListener[i]);
						//calendarComponent.setListener(null);
						
						JOptionPane.showMessageDialog(calendarEnd, "La fecha 'Hasta' no puede ser inferior a la fecha 'Desde'", "Error", JOptionPane.ERROR_MESSAGE);
						//calendarComponent.notSelect();
						//calendarComponent.setCalendar(valueCalendarInitial);
						calendarComponent.setSelectedCalendar(valueCalendarInitial);
						
						
						//calendarComponent.setListener(this);
						for(int i=0;i<windowListener.length;i++)
							dialog.addWindowListener(windowListener[i]);
						
						
						if(listenerEnd!=null)
							listenerEnd.setDateSelectioned(valueCalendarInitial,calendarComponent);
					}
					dateEndSelectioned=true;
				}catch(Exception ex){
	        		//m_server.logError(ex,"Error al asignar fecha");
	            	ex.printStackTrace();
	        	}
			}
			
			public void setAllDate(Calendar c) {
			}
			
		});
		
		JPanel panelInitial=new JPanel(new BorderLayout(0,0));
		panelInitial.setBorder(BorderFactory.createTitledBorder("Desde"));
		panelInitial.add(calendarInitial,BorderLayout.CENTER);
		
		JPanel panelEnd=new JPanel(new BorderLayout(0,0));
		panelEnd.setBorder(BorderFactory.createTitledBorder("Hasta"));
		panelEnd.add(calendarEnd,BorderLayout.CENTER);
		
		JPanel calendarsPanel=new JPanel();
		calendarsPanel.add(panelInitial);
		calendarsPanel.add(panelEnd);
		
		add(calendarsPanel,BorderLayout.CENTER);
		
		JButton closeButton=new JButton("Cerrar");
		closeButton.setMargin(new Insets(0,0,0,0));
		//closeButton.setPre
		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});
		
		JPanel closePanel=new JPanel();
		closePanel.add(closeButton);
		add(closePanel,BorderLayout.SOUTH);
		
		setBorder(BorderFactory.createLineBorder(Color.black, 1));	
	
	}
	
	public JDialog buildDialog(){
		dialog = new JDialog();
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowListener(){
			public void windowDeactivated(WindowEvent arg0){
				//closeDialog();//Comentado ya que con java 7 cuando estamos en un filterControl principal se llama a este metodo nada mas abrir el dialog y se cierra automaticamente
			}
			public void windowActivated(WindowEvent arg0){}
			public void windowDeiconified(WindowEvent arg0){}
			public void windowIconified(WindowEvent arg0){}
			public void windowOpened(WindowEvent arg0){}
			public void windowClosed(WindowEvent arg0){}
			public void windowClosing(WindowEvent arg0){}
		});

		//dialog.setSize(new Dimension(ANCHO, ALTO));
		//dialog.setSize(panel.getPreferredSize());
		dialog.setUndecorated(true);
		setBorder(BorderFactory.createLineBorder(Color.black, 1));	
		dialog.setContentPane(this);
		dialog.pack();	
		dialog.setLocation(getPointOnScreen(frame, button));
		dialog.setResizable(false);
		//dialog.setVisible(true);    	
		return dialog;
	}

	public IDateListener getListenerEnd() {
		return listenerEnd;
	}

	public void setListenerEnd(IDateListener listenerEnd) {
		this.listenerEnd=listenerEnd;
	}

	public IDateListener getListenerInitial() {
		return listenerInitial;
	}

	public void setListenerInitial(IDateListener listenerInitial) {
		this.listenerInitial=listenerInitial;
	}

	public JCalendar getCalendarEnd() {
		return calendarEnd;
	}

	public JCalendar getCalendarInitial() {
		return calendarInitial;
	}
	
	public JDialog getDialog(){
		return dialog;
	}
	
//	 f: panel principal
	 // start: componente sobre el que se va a colocar el calendario, generalmente el boton
	 private Point getPointOnScreen(Component f, Component start){
		 Point locationStart=start.getLocationOnScreen();
		 Point locationFrame=f.getLocationOnScreen();
		 Point p = new Point(locationStart.x+start.getWidth()/2, locationStart.y+start.getHeight()/2);
		 
		 //Sobresale por la derecha
		 if(p.x+dialog.getWidth()>locationFrame.x+f.getWidth()){
			 p.x=p.x-dialog.getWidth();
			 if(p.x<locationFrame.x)
				 p.x=p.x+dialog.getWidth()/2;
		 }
		 //Sobresale por la izquierda
		 if(p.x<locationFrame.x){
			 p.x=p.x+dialog.getWidth();
			 if(p.x+dialog.getWidth()>locationFrame.x+f.getWidth())
				 p.x=p.x-dialog.getWidth()/2;
		 }
		 //Sobresale por abajo
		 if(p.y+dialog.getHeight()>locationFrame.y+f.getHeight()){
			 p.y=p.y-dialog.getHeight();
			 if(p.y<locationFrame.y)
				 p.y=p.y+dialog.getHeight()/2;
		 }
		 //Sobresale por arriba
		 if(p.y<locationFrame.y){
			 p.y=p.y+dialog.getHeight();
			 if(p.y+dialog.getHeight()>locationFrame.y+f.getHeight())
				 p.y=p.y-dialog.getHeight()/2;
		 } 
		 return p;
	 }

	 public void closeDialog(){
		 /*Calendar valueCalendarInitial=calendarInitial.getCalendar();
			Calendar valueCalendarEnd=calendarEnd.getCalendar();
			if(valueCalendarInitialDefault!=null && valueCalendarEndDefault!=null){
				if(!dateInitialSelectioned && valueCalendarInitial!=null && !valueCalendarInitialDefault.equals(valueCalendarInitial))
					if(listenerInitial!=null)
						listenerInitial.setDateSelectioned(valueCalendarInitial, calendarInitial);
				if(!dateEndSelectioned && valueCalendarEnd!=null && !valueCalendarEndDefault.equals(valueCalendarEnd))
					if(listenerEnd!=null)
						listenerEnd.setDateSelectioned(valueCalendarEnd, calendarEnd);
			}*/
			if(listenerInitial!=null)
				listenerInitial.setDateSelectioned(calendarInitial.getCalendar(),calendarInitial);
			if(listenerEnd!=null)
				listenerEnd.setDateSelectioned(calendarEnd.getCalendar(),calendarEnd);
			dialog.dispose();
	}	

}
