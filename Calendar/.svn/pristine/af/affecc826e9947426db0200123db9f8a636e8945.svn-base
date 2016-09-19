package miniCalendar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import dynagent.common.communication.docServer;

public class JCalendarDateTime extends JCalendar{

	private static final long serialVersionUID = 1L;
	private JDateTime dateTime;

	public JCalendarDateTime(Component frame, Component button, Calendar init, IDateListener listener, boolean hasCloseButton, docServer server) {
		super(frame, button, init!=null?init:Calendar.getInstance(), listener, hasCloseButton);
		
		dateTime=new JDateTime(calendar, null, null, server);
		//if(init!=null)
			//setDate(calendar);
		
		if(hasCloseButton){
			JPanel panel=new JPanel(new BorderLayout());
			panel.add(dateTime,BorderLayout.CENTER);
			panel.add(closePanel,BorderLayout.SOUTH);
			add(panel, BorderLayout.SOUTH);	
		}else{
			add(dateTime, BorderLayout.SOUTH);
		}
		
		dateTime.addPropertyChangeListener(this);
	}
	
	public void setDate(Calendar value){
		//System.err.println("CalendarInit:"+this.hashCode()+"value:"+value);
		if(dateTime.getCalendar()==null){
			value.set(Calendar.HOUR_OF_DAY, 0);
			value.set(Calendar.MINUTE, 0);
			value.set(Calendar.SECOND, 0);
		}else{
			Calendar c = (Calendar)dateTime.getCalendar().clone();
			value.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
			value.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
			value.set(Calendar.SECOND, c.get(Calendar.SECOND));
		}
		//System.err.println("CalendarFinal:"+this.hashCode()+"value:"+value);
		dateTime.setCalendar(value);
	}

	public JDateTime getDateTime() {
		return dateTime;
	}

	@Override
	protected void updateDayChooser(Calendar c, boolean initial) {
		super.updateDayChooser(c, initial);
		if(dateTime!=null){
			if(hasCloseButton){
				JPanel panel=new JPanel(new BorderLayout());
				panel.add(dateTime,BorderLayout.CENTER);
				panel.add(closePanel,BorderLayout.SOUTH);
				add(panel, BorderLayout.SOUTH);	
			}else{
				add(dateTime, BorderLayout.SOUTH);
			}
		}
		repaint();
		validate();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("hour")) {
			//System.err.println("PropertyChangeDateTime:"+evt.getPropertyName()+" "+evt.getNewValue());
			
			//Actualizamos por si el cambio en horas provoca un cambio en el dia seleccionado
			updateDayChooser(dateTime.getCalendar(), true);

			//if(listener!=null)
			//	listener.setAllDate(c);

		}else{
			super.propertyChange(evt);
			if(dateTime!=null && dateTime.getCalendar()==null){
				if (this.calendar != null)
					setDate(calendar);
			}
		}
	}

	@Override
	public void setSelectedCalendar(Calendar c) {
		super.setSelectedCalendar(c);
		add(getDateTime(), BorderLayout.SOUTH);
		repaint();
		validate();	
	}

//	@Override
//	public JDialog buildDialog() {
//		final JDialog dialog= super.buildDialog();
//		dialog.addWindowListener(new WindowListener(){
//			public void windowDeactivated(WindowEvent arg0){
//				System.err.println("WindowDeactived");
//				if(listener!=null){
//					//if(calendarDateTime!=null){
//						setDate(calendar);						
//						listener.setDateSelectioned(getDateTime().getCalendar(),getThis());						
//					/*}*//*else{
//						listener.setDateSelectioned(calendar,getThis());
//						System.err.println("2 "+calendar.getTime());
//					}*/
//				}
//				dialog.dispose();
//			}
//			public void windowActivated(WindowEvent arg0){}
//			public void windowDeiconified(WindowEvent arg0){}
//			public void windowIconified(WindowEvent arg0){}
//			public void windowOpened(WindowEvent arg0){}
//			public void windowClosed(WindowEvent arg0){}
//			public void windowClosing(WindowEvent arg0){}
//		});
//		return dialog;
//	}
}
