package calendar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FrameDateEvent extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton Button2, Button1;
	private JLabel ff, fe, e, d;
	private JTextField tff, tfe, te, tname;
	private String fechaE, fechaF, estado, name, diaSinHora;
	private int ido;
	
	public FrameDateEvent(String diaSinHora, Task t){//, String boton){
		super(Constants.getCurrentWin());
		//super();
		fechaE=t.getExecutionDate();
		fechaF=t.getAsignDate();
		estado=t.getStatus();
		name=t.getName();
		this.diaSinHora=diaSinHora;
		this.ido=t.getIdoUserTask();
        initComponents();
    }

    private void initComponents() {
    	
    	setTitle("Datos Tarea");
		setResizable(false);	
		setModal(true);
		
		Button1 = new JButton();
        Button2 = new JButton();
        tff = new JTextField(fechaF);
        tfe = new JTextField(fechaE);
        te = new JTextField(estado);
        tname = new JTextField(name);
        ff = new JLabel();
        fe = new JLabel();
        e = new JLabel();
        d = new JLabel();

        getContentPane().setLayout(null);

        Button1.setText("Cancelar");
        Button1.addActionListener(this);
        Button1.setActionCommand("Cancelar");
        getContentPane().add(Button1);
        Button1.setBounds(220, 230, 100, 26);
        
        Button2.setText("Ejecutar");
        Button2.addActionListener(this);
        Button2.setActionCommand("Ejecutar");
        getContentPane().add(Button2);
        Button2.setBounds(80, 230, 100, 26);
        
        Button2.setEnabled(false);

        tff.setHorizontalAlignment(JTextField.LEFT);
        getContentPane().add(tff);
        tff.setBounds(180, 25, 150, 25);
       
        ff.setHorizontalAlignment(SwingConstants.LEFT);
        ff.setText("Fecha Fin");
        getContentPane().add(ff);
        ff.setBounds(40, 25, 140, 25);
        
        tfe.setHorizontalAlignment(JTextField.LEFT);
        getContentPane().add(tfe);
        tfe.setBounds(180, 65, 150, 25);

        fe.setHorizontalAlignment(SwingConstants.LEFT);
        fe.setText("Fecha Ejecucion:");
        getContentPane().add(fe);
        fe.setBounds(40, 65, 140, 25);
        
        te.setHorizontalAlignment(JTextField.LEFT);
        getContentPane().add(te);
        te.setBounds(180, 100, 150, 25);

        e.setHorizontalAlignment(SwingConstants.LEFT);
        e.setText("Estado:");
        getContentPane().add(e);
        e.setBounds(40, 100, 140, 25);
        
        tname.setHorizontalAlignment(JTextField.LEFT);
        getContentPane().add(tname);
        //td.setBounds(180, 135, 150, 25);
        tname.setBounds(40, 165, 290, 45);

        d.setHorizontalAlignment(SwingConstants.LEFT);
        d.setText("Descripcion:");
        getContentPane().add(d);
        d.setBounds(40, 135, 140, 25);

        pack();
        setSize(new Dimension(400, 300));
        Component c = Constants.frame;
        setLocation(c.getLocationOnScreen().x+c.getSize().width/2-400/2,c.getLocationOnScreen().y+c.getSize().height/2-300/2);
        setVisible(true);
    }	
    
    public void actionPerformed(ActionEvent e) 
	{
    	try{
    		
			if(e.getActionCommand().equals("Cancelar")){
				setVisible(false);
				dispose();			    
			}else if(e.getActionCommand().equals("Ejecutar")){
				Constants.itaskCenter.exeTask(ido);
				DatesBigCalendar.getEventos().deleteEvento(diaSinHora, name);
				DatesBigCalendar.pc.actualizaMP();
				DatesBigCalendar.pc.actualizaWP();
				DatesBigCalendar.pc.actualizaDP();
				setVisible(false);
				dispose();
			}
    	}catch(Exception ex){
			ex.printStackTrace();
    	}
	}
}
