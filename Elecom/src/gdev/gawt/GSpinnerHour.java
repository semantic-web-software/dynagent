package gdev.gawt;

import gdev.gawt.utils.UtilsFields;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.AssignValueException;
import gdev.gen.GConfigView;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;
import gdev.gfld.GFormField;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dynagent.common.communication.docServer;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;

import miniCalendar.JDateTime;

public class GSpinnerHour extends GComponent implements IComponentData {

	private static final long serialVersionUID = 1L;
	private Object defaultVal;
	private GFormField gFormField;
	private JDateTime dateTime;
	private JDateTime dateTime2=null;
	private Calendar calendarOld;
	private Calendar calendarOld2;
	private docServer server;
	private boolean modeFilter;
	private boolean modeConsulta;
	private IUserMessageListener messageListener;

	public GSpinnerHour(GFormField gFormField, docServer server, IComponentListener componentListener, IUserMessageListener messageListener,boolean modeFilter, boolean modeConsulta) {
		super(gFormField,componentListener);
		this.messageListener=messageListener;
		this.server=server;
		this.gFormField = gFormField;
		this.modeFilter=modeFilter;
		this.modeConsulta=modeConsulta;
		defaultVal = gFormField.getDefaultVal();
		//System.err.println("defaultVal:"+defaultVal);
	}

	protected void createComponent() throws ParseException, AssignValueException {
		Calendar calendar = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		if(modeFilter){
			if(defaultVal!=null){
				String[] buf = defaultVal.toString().split(":");
				if(buf.length>1){
					calendar.setTimeInMillis(Long.parseLong(buf[0]));
					calendar2.setTimeInMillis(Long.parseLong(buf[1]));
				}else{
					calendar.setTimeInMillis(Long.parseLong(buf[0]));
					calendar2.setTimeInMillis(Long.parseLong(buf[0]));
				}
				calendarOld = (Calendar)calendar.clone();
				calendarOld2 = (Calendar)calendar2.clone();			
			}else{
				calendar = null;
				calendar2 = null;
				calendarOld = null;
				calendarOld2 = null;
			}
		}else{
			if(defaultVal!=null){
				calendar.setTimeInMillis((Long)defaultVal);
				calendarOld = (Calendar)calendar.clone();			
			}else{
				calendar = null;
				calendarOld = null;
			}
		}

		FocusListener listener = new FocusListener(){
			public void focusGained(FocusEvent ev){
				try{
					if(modeFilter){
						if(dateTime.getCalendar()==null && dateTime2.getCalendar()!=null)
							dateTime.setCalendar((Calendar)dateTime2.getCalendar().clone());
					}
				}catch(Exception ex){
					ex.printStackTrace();
					server.logError(SwingUtilities.getWindowAncestor(GSpinnerHour.this),ex,"Error al asignar valor");
				}
			}
			public void focusLost(FocusEvent ev){
				try {
					JComponent comp = (JComponent)ev.getOppositeComponent();
					if(!dateTime.isComponent(comp) && dateTime2==null){
						submit(dateTime.getCalendar(), null);
					}else if(!dateTime.isComponent(comp) && dateTime2!=null && !dateTime2.isComponent(comp)/* && !m_objComponent.isAncestorOf(comp) Esto se hacia pensando en el boton clean pero no tiene focusListener*/){
						if(dateTime.getCalendar()!=null && dateTime2.getCalendar()==null)
							dateTime2.setCalendar(dateTime.getCalendar());
						if(dateTime.getCalendar()==null && dateTime2.getCalendar()!=null)
							dateTime.setCalendar(dateTime2.getCalendar());
						submit(dateTime.getCalendar(), dateTime2.getCalendar());
					}
				}catch(Exception ex){
					ex.printStackTrace();
					server.logError(SwingUtilities.getWindowAncestor(GSpinnerHour.this),ex,"Error al asignar valor");
				}
			}
		};
		dateTime = new JDateTime(calendar, listener, (int)gFormField.getRowHeight(), server);
		if(modeConsulta)
			dateTime.setEditable(false);
		if(!gFormField.isNullable() && !modeFilter)
			dateTime.setBackgroundColor(GConfigView.colorBackgroundRequired);

		if(modeFilter){
			m_objComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			FocusListener listener2 = new FocusListener(){
				public void focusGained(FocusEvent ev){
					try{
						if(dateTime2.getCalendar()==null && dateTime.getCalendar()!=null)
							dateTime2.setCalendar((Calendar)dateTime.getCalendar().clone());
					}catch(Exception ex){
						ex.printStackTrace();
						server.logError(SwingUtilities.getWindowAncestor(GSpinnerHour.this),ex,"Error al asignar valor");
					}	
				}
				public void focusLost(FocusEvent ev){
					try {
						JComponent comp = (JComponent)ev.getOppositeComponent();
						if(!dateTime2.isComponent(comp) && !dateTime.isComponent(comp)/* && !m_objComponent.isAncestorOf(comp) Esto se hacia pensando en el boton clean pero no tiene focusListener*/){
							
								if(dateTime.getCalendar()!=null && dateTime2.getCalendar()==null)
									dateTime2.setCalendar(dateTime.getCalendar());
								if(dateTime.getCalendar()==null && dateTime2.getCalendar()!=null)
									dateTime.setCalendar(dateTime2.getCalendar());
								submit(dateTime.getCalendar(), dateTime2.getCalendar());
						}
					}catch(Exception ex){
						ex.printStackTrace();
						server.logError(SwingUtilities.getWindowAncestor(GSpinnerHour.this),ex,"Error al asignar valor");
					}	
				}
			};
			Calendar clon = null;
			if(calendar!=null)
				clon = (Calendar)calendar.clone();
			dateTime2 = new JDateTime(clon, listener2, (int)gFormField.getRowHeight(), server);
			if(modeConsulta)
				dateTime2.setEditable(false);
			if(!gFormField.isNullable() && !modeFilter)
				dateTime2.setBackgroundColor(GConfigView.colorBackgroundRequired);
			m_objComponent.add(dateTime);
			m_objComponent.add(new JLabel(" - "));
			m_objComponent.add(dateTime2);
			JButton clear = botoneraAccion.subBuildBoton(null, null, "deleteTime", null, "Limpiar", (int)gFormField.getRowHeight(), (int)gFormField.getRowHeight(),true,server);
			clear.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ev){
					try {
						dateTime.clearAll();
						dateTime2.clearAll();
						setStringCalendar(null);
						submit(null, null);
					} catch (AssignValueException ex) {
						server.logError(SwingUtilities.getWindowAncestor(GSpinnerHour.this),ex,"Error al borrar las horas");
						ex.printStackTrace();
					}
				}				
			});
			m_objComponent.add(clear);
			m_objComponent.setBorder(BorderFactory.createEmptyBorder());			
		}else{
			m_objComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			m_objComponent.add(dateTime);
			
			JButton clear = botoneraAccion.subBuildBoton(null, null, "deleteTime", null, "Limpiar", (int)gFormField.getRowHeight(), (int)gFormField.getRowHeight(),true,server);
			if(!modeConsulta){
				clear.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ev){
						try {
							dateTime.clearAll();
							setStringCalendar(null);
							submit(null, null);
						} catch (AssignValueException ex) {
							server.logError(SwingUtilities.getWindowAncestor(GSpinnerHour.this),ex,"Error al borrar la hora");
							ex.printStackTrace();
						}
					}				
				});
			}
			else{
				clear.setFocusable(false);
				MouseListener[] listeners=clear.getMouseListeners();
				int numListeners=listeners.length;
				for(int i=0;i<numListeners;i++)
					clear.removeMouseListener(listeners[i]);
			}
			m_objComponent.add(clear);
			m_objComponent.setBorder(BorderFactory.createEmptyBorder());
			m_objComponent.setName(getFormField().getName());
		}
	}

	public void initValue() throws ParseException, AssignValueException {
		dateTime.clearAll();
		if(dateTime2!=null)
			dateTime2.clearAll();
		setStringCalendar(defaultVal);
		submit(dateTime.getCalendar(), modeFilter?dateTime2.getCalendar():null);
	}

	public void setValue(Object newValue, Object oldValue) throws ParseException, AssignValueException {
		//System.err.println("setValue "+newValue);
		setStringCalendar(newValue);
	}
	
	//No notifica a IComponentListener
	private void setStringCalendar(Object value){		
		Calendar calendar = Calendar.getInstance();
		if(value!=null){
			String[] buf = value.toString().split(":");
			if(buf.length>1){
				calendar.setTimeInMillis(Long.parseLong(buf[0]));
				dateTime.setCalendar(calendar);
				Calendar calendar2 = Calendar.getInstance();
				calendar2.setTimeInMillis(Long.parseLong(buf[1]));
				dateTime2.setCalendar(calendar2);
			}else{
				calendar.setTimeInMillis(Long.parseLong(buf[0]));
				dateTime.setCalendar(calendar);
			}
		}else{
			calendar=null;
			dateTime.setCalendar(calendar);
			if(modeFilter)
				dateTime2.setCalendar(calendar);
		}		
	}

	private void submit(Calendar c1, Calendar c2) throws AssignValueException{
		//System.err.println("Submit Spinner:");
		if(UtilsFields.equals(c1, calendarOld) && UtilsFields.equals(c2, calendarOld2))
			return;
		
		String id = gFormField.getId();
		IdObjectForm idObjectForm=new IdObjectForm(id);
		Integer valueCls = idObjectForm.getValueCls();
		Long value = toMiliSecondsCalendarHour(c1);
		Long valueOld = toMiliSecondsCalendarHour(calendarOld);
		if(c2!=null || (c1==null && c2 == null)){
			Long value2 = toMiliSecondsCalendarHour(c2);
			Long valueOld2 = toMiliSecondsCalendarHour(calendarOld2);

			if(value!=null && value2!=null && value>value2){
				messageListener.showMessage("El primer valor del rango no puede ser mayor que el segundo en el campo: "+gFormField.getLabel(),SwingUtilities.getWindowAncestor(this));
			}else{

				Object valueComp;
				Object valueOldComp;
				if(value==null && value2 == null)
					valueComp = null;
				else if(value==null)
					valueComp = value2;
				else if(value2==null)
					valueComp = value;
				else
					valueComp = value+":"+value2;

				if(valueOld==null && valueOld2 == null)
					valueOldComp = null;
				else if(valueOld==null)
					valueOldComp = value2;
				else if(valueOld2==null)
					valueOldComp = valueOld;
				else
					valueOldComp = valueOld+":"+valueOld2;

				try {
					/*if(c==null)
					System.err.println("Out2 "+valueComp+" "+valueOldComp+" null");
				else
					System.err.println("Out2 "+valueComp+" "+valueOldComp+" "+c.getTime());*/
					//System.err.println("submit "+valueComp+" old "+valueOldComp);
					m_componentListener.setValueField(id, valueComp, valueOldComp, valueCls, valueCls);
					if(c1==null)
						calendarOld=null;
					else
						calendarOld=(Calendar)c1.clone();
					if(c2==null)
						calendarOld2=null;
					else
						calendarOld2=(Calendar)c2.clone();
				}catch(AssignValueException ex){
					dateTime.setCalendar(calendarOld);
					dateTime2.setCalendar(calendarOld2);
					throw ex;
				}catch(NotValidValueException ex){
					ex.printStackTrace();
					messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
					dateTime.setCalendar(calendarOld);
					dateTime2.setCalendar(calendarOld2);
				}
			}
		}else if(c2==null){
			try {
				/*if(c==null)
					System.err.println("Out1 "+value+" "+valueOld+" null");
				else
					System.err.println("Out1 "+value+" "+valueOld+" "+c.getTime());*/
				m_componentListener.setValueField(id, value, valueOld, valueCls, valueCls);
				calendarOld=(Calendar)c1.clone();
			}catch(AssignValueException ex){
				dateTime.setCalendar(calendarOld);
				final Runnable focus = new Runnable() {
					public void run() {
						dateTime.requestFocusInWindow();
					}
				};
				SwingUtilities.invokeLater(focus);
				throw ex;
			}catch(NotValidValueException ex){
				ex.printStackTrace();
				messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
				dateTime.setCalendar(calendarOld);
				final Runnable focus = new Runnable() {
					public void run() {
						dateTime.requestFocusInWindow();
					}
				};
				SwingUtilities.invokeLater(focus);
			}
		}
	}

	private Long toMiliSecondsCalendarHour(Calendar c){
		if(c==null)
			return null;
		else{
			Calendar clon = Calendar.getInstance();
			clon.setTimeInMillis(-3600000);
			clon.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
			clon.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
			clon.set(Calendar.SECOND, c.get(Calendar.SECOND));
			//c.set(1970, Calendar.JANUARY, 1);
			return clon.getTimeInMillis();
		}
	}

	public static void main(String args[]){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(82799000);//22:59:59
		System.err.println(c.getTime());

		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(-3600000);//-00:30:00
		System.err.println(c2.getTime()+" "+c2.get(Calendar.HOUR_OF_DAY));
	}
	
	@Override
	public boolean newValueAllowed() {
		return dateTime.getCalendar()==null;
	}
	
	public Object getValue() {
		Object value=toMiliSecondsCalendarHour(dateTime.getCalendar());
		if(modeFilter)
			value=value+":"+toMiliSecondsCalendarHour(dateTime2.getCalendar());
		
		return value;
	}

	public void clean() throws ParseException, AssignValueException {
		setStringCalendar(null);
	}

}
