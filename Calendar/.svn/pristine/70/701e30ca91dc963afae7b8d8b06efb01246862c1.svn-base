package miniCalendar;

import javax.swing.*;

import dynagent.common.communication.docServer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Calendar;

public class JDateTime extends JPanel implements ActionListener, FocusListener{
	
	private static final long serialVersionUID = 1L;
	private JTextField hour, minute, second;
	private JLabel point, point2;
	private JButton up, down;
	private Calendar calendar;
	private JTextField used;
	private docServer server;
	
	private static int height_without_borders = 18;
	final public static int widht = 92;

	public JDateTime(Calendar calendar, FocusListener listener, Integer height, docServer server){		
		setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));		
		this.calendar = calendar;
		this.server=server;
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		p.setBorder(UIManager.getBorder("TextField.border"));
		if(height!=null)
			height_without_borders = height-p.getBorder().getBorderInsets(p).bottom-p.getBorder().getBorderInsets(p).top;
		
		hour = new JTextField();
		minute = new JTextField();
		second = new JTextField();
		used=hour;
		setCalendar(this.calendar);	
		
		hour.addFocusListener(this);
		minute.addFocusListener(this);
		second.addFocusListener(this);
		
		point = new JLabel(":");
		point2 = new JLabel(":");
		point.setOpaque(true);
		point2.setOpaque(true);
		point.setBackground(Color.white);
		point2.setBackground(Color.white);
		JPanel buttons = getButtons();
		
		if(listener!=null){
			hour.addFocusListener(listener);
			minute.addFocusListener(listener);
			second.addFocusListener(listener);
			up.addFocusListener(listener);
			down.addFocusListener(listener);
			point.addFocusListener(listener);
			point2.addFocusListener(listener);
		}

		hour.setBorder(BorderFactory.createEmptyBorder());
		minute.setBorder(BorderFactory.createEmptyBorder());
		point.setBorder(BorderFactory.createEmptyBorder());
		point2.setBorder(BorderFactory.createEmptyBorder());
		second.setBorder(BorderFactory.createEmptyBorder());
		buttons.setBorder(BorderFactory.createEmptyBorder());		
		
		hour.setPreferredSize(new Dimension(20, height_without_borders));
		point.setPreferredSize(new Dimension(5, height_without_borders));
		minute.setPreferredSize(new Dimension(20, height_without_borders));
		point2.setPreferredSize(new Dimension(5, height_without_borders));
		second.setPreferredSize(new Dimension(20, height_without_borders));
		buttons.setPreferredSize(new Dimension(16, height_without_borders));

		
		int height_with_borders = height_without_borders+p.getBorder().getBorderInsets(p).bottom+p.getBorder().getBorderInsets(p).top;;
		p.add(hour);
		p.add(point);
		p.add(minute);
		p.add(point2);
		p.add(second);
		p.add(buttons);		
		p.setPreferredSize(new Dimension(widht, height_with_borders));
		
		add(p);
		setPreferredSize(new Dimension(widht, height_with_borders));
	}

	public JPanel getButtons(){
		JPanel p = new JPanel(new BorderLayout());
		up = new JButton();
		up.setActionCommand("up");
		up.addActionListener(this);
		up.setPreferredSize(new Dimension(16, height_without_borders/2));
		Insets border = up.getBorder().getBorderInsets(up);
		Dimension dim = new Dimension(16-border.left-border.right, (height_without_borders/2)-border.bottom-border.top-2);
		//up.setIcon(getIcon("images/up.gif", dim));
		if(server!=null)
			up.setIcon(server.getIcon(null,"up",(int)dim.getWidth(),(int)dim.getHeight()));
		up.setFocusPainted(false);
		down = new JButton();
		down.setActionCommand("down");
		down.addActionListener(this);
		down.setPreferredSize(new Dimension(16, height_without_borders/2));
		//down.setIcon(getIcon("images/down.gif", dim));
		if(server!=null)
			down.setIcon(server.getIcon(null,"down",(int)dim.getWidth(),(int)dim.getHeight()));
		down.setFocusPainted(false);
		p.add(up, BorderLayout.NORTH);
		p.add(down, BorderLayout.CENTER);
		return p;
	}

	public void actionPerformed(ActionEvent ev) {
		boolean update=false;
		if(ev.getActionCommand().equals("up")){
			if(calendar==null){
				createZeroCalendar();				
			}
			if(used==hour){
				calendar.add(Calendar.HOUR_OF_DAY, 1);	
				verifyCalendar(Calendar.HOUR_OF_DAY);				
			}else if(used==minute){
				calendar.add(Calendar.MINUTE, 1);
				verifyCalendar(Calendar.MINUTE);		
			}else if(used==second){
				calendar.add(Calendar.SECOND, 1);
				verifyCalendar(Calendar.SECOND);		
			}
			update=true;
		}else if(ev.getActionCommand().equals("down")){
			if(calendar==null){
				createZeroCalendar();				
			}
			if(used==hour){
				calendar.add(Calendar.HOUR_OF_DAY, -1);	
				verifyCalendar(Calendar.HOUR_OF_DAY);		
			}else if(used==minute){
				calendar.add(Calendar.MINUTE, -1);
				verifyCalendar(Calendar.MINUTE);		
			}else if(used==second){
				calendar.add(Calendar.SECOND, -1);
				verifyCalendar(Calendar.SECOND);		
			}
			update=true;
		}
		if(update){
			setCalendar(calendar);
			used.requestFocusInWindow();
		}
	}
	
	private void createZeroCalendar(){
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(-3600000);
	}

	private void format(){
		if(hour.getText().length()==1)			
			hour.setText("0"+hour.getText());
		if(minute.getText().length()==1)			
			minute.setText("0"+minute.getText());
		if(second.getText().length()==1)			
			second.setText("0"+second.getText());
	}
	
	public Calendar getCalendar(){
		return calendar;
	}
	
	public void setCalendar(Calendar value) {
		if(value==null){
			calendar = null;			
		}else{
			//System.err.println("\t\tset "+value.getTime()+" "+value.getTimeInMillis());
			int hourOld=!hour.getText().isEmpty()?Integer.parseInt(hour.getText()):-1;
			calendar = value;
			hour.setText(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
			minute.setText(String.valueOf(calendar.get(Calendar.MINUTE)));
			second.setText(String.valueOf(calendar.get(Calendar.SECOND)));
			format();
			firePropertyChange("hour",hourOld,calendar.get(Calendar.HOUR_OF_DAY));
		}	
	}
	
	private ImageIcon getIcon(String rutaImagen, Dimension sizeIcono){
		URL imageURL = this.getClass().getResource(rutaImagen);

        if (imageURL == null) {
            System.err.println(this.getClass()+":Imagen no encontrada: "+ rutaImagen);
            return null;
        } else {
            Image imagen=Toolkit.getDefaultToolkit().createImage(imageURL);
            if(sizeIcono!=null)
                imagen=imagen.getScaledInstance((int)sizeIcono.getWidth(),(int)sizeIcono.getHeight(),Image.SCALE_SMOOTH);
            if(imagen!=null)
            	return new ImageIcon(imagen);
            else return new ImageIcon();
        }
	}

	public boolean isComponent(JComponent comp) {
		if(comp!=null){
			if(!comp.equals(hour) && !comp.equals(minute) && !comp.equals(second) && !comp.equals(up) && !comp.equals(down) && !comp.equals(point) && !comp.equals(point2))
				return false;
			else
				return true;
		}
		return true;
	}
	
	public void focusGained(FocusEvent ev) {
		JTextField textField=(JTextField)ev.getComponent();
		textField.selectAll();
		used=textField;
	}

	public void focusLost(FocusEvent ev){
		if(ev.getComponent()==hour)
			hour = verifyCalendarTextField(hour, Calendar.HOUR_OF_DAY);
		else if(ev.getComponent()==minute)
			minute = verifyCalendarTextField(minute, Calendar.MINUTE);
		else
			second = verifyCalendarTextField(second, Calendar.SECOND);
	}
	
	private JTextField verifyCalendarTextField(JTextField text, int type){
		Integer n=null;
		boolean retract=false;
		try{
			n = Integer.parseInt(text.getText());
		}catch(NumberFormatException ex){
			retract=true;
		}finally{
			if(n!=null){
				if(calendar==null){
					createZeroCalendar();
					putZeroTextIfEmpty();
				}				
				if(type==Calendar.HOUR_OF_DAY && n!=null && n>23)
					retract=true;
				else if(n!=null && n>59)
					retract=true;
				else if(n<0)
					retract=true;
	
				if(retract){
					text.setText(String.valueOf(calendar.get(type)));
					format();
				}else
					calendar.set(type, n);	
				
			}
		}
		return text;
	}
	
	private void putZeroTextIfEmpty(){
		if(hour.getText().equals(""))
			hour.setText("00");
		if(minute.getText().equals(""))
			minute.setText("00");
		if(second.getText().equals(""))
			second.setText("00");
	}
	
	private void verifyCalendar(int type){
		int n = calendar.get(type);
		if(type==Calendar.HOUR_OF_DAY){
			if(n<0)
				calendar.set(type, 0);
			else if(n>23)
				calendar.set(type, 23);
		}else if(type==Calendar.MINUTE){
			if(n<0)
				calendar.set(type, 0);
			else if(n>59)
				calendar.set(type, 59);
		}else if(type==Calendar.SECOND){
			if(n<0)
				calendar.set(type, 0);
			else if(n>59)
				calendar.set(type, 59);
		}
	}
	
	public void setBackgroundColor(Color c){
		this.setBackground(c);
		hour.setBackground(c);
		minute.setBackground(c);
		second.setBackground(c);
		point.setBackground(c);
		point2.setBackground(c);
	}
	
	public void clearAll(){
		hour.setText("");
		minute.setText("");
		second.setText("");
		setCalendar(null);
	}
	
	public void setEditable(boolean value){
		hour.setEditable(value);
		minute.setEditable(value);
		second.setEditable(value);
		convertirModoConsulta(up);
		convertirModoConsulta(down);
	}
	
	private void convertirModoConsulta(JButton button){
		button.setFocusable(false);
		MouseListener[] listeners=button.getMouseListeners();
		int numListeners=listeners.length;
		for(int i=0;i<numListeners;i++)
			button.removeMouseListener(listeners[i]);
	}
	
	public static void main(String[] s) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel p = new JPanel(new BorderLayout());
		p.add(new JDateTime(Calendar.getInstance(), null, null, null), BorderLayout.CENTER);
		p.setPreferredSize(new Dimension(100, 30));
		frame.getContentPane().add(p);
		frame.pack();
		frame.setVisible(true);
	}	
}