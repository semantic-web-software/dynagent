package dynagent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;

public class StatusBar extends JPanel{

	private static final long serialVersionUID = 1L;
	private JLabel m_labelLocalizacion;
	private JLabel m_labelAccion;
	private JProgressBar m_barraProgreso;
	private int m_progress;
	private Dimension m_size;
	private int m_maximoProgreso;
	private static final String TEXTO_CONEXION=Utils.normalizeLabel("Recibiendo datos...");
	private static final String TEXTO_FIN_CONEXION=Utils.normalizeLabel("Terminado");
	private static final String TEXTO_ERROR_CONEXION=Utils.normalizeLabel("¡Error!");
	private boolean m_finishBarraProgreso;
	private int m_nivelLocalizacion;
	private boolean m_error;
	private HashMap<String,String> mapLocalizacionAccion;
	
	public StatusBar(String localizacion,ImageIcon icono,Dimension size){
		super();
		m_size=size;
		m_finishBarraProgreso=true;
		m_nivelLocalizacion=0;
		m_maximoProgreso=m_size.width/4;
		mapLocalizacionAccion=new HashMap<String, String>();
		build(localizacion,icono);
	}
	
	private void build(String localizacion,ImageIcon icono){
		
		m_labelAccion=new JLabel();
		m_labelAccion.setName("StatusBar.labelAccion");
		
		m_labelLocalizacion=new JLabel();
		m_labelLocalizacion.setName("StatusBar.labelLocalizacion");
		m_labelLocalizacion.setIcon(icono);
		setLocalizacion(localizacion,0);
		
		m_barraProgreso=new JProgressBar();
		m_barraProgreso.setStringPainted(true);
		m_barraProgreso.setBackground(UIManager.getColor("ToolBar.background"));
		m_barraProgreso.setPreferredSize(new Dimension(m_maximoProgreso,m_size.height));
		
		JPanel panel=new JPanel();
		panel.setBackground(UIManager.getColor("ToolBar.background"));
		this.setPreferredSize(new Dimension(m_size.width, m_size.height));
		BorderLayout layout=new BorderLayout(5,0);
		this.setLayout(layout);
		//this.setBorder(BorderFactory.createEmptyBorder());
		this.add(m_labelLocalizacion,BorderLayout.WEST);
		this.add(m_labelAccion,BorderLayout.EAST);
		this.add(panel,BorderLayout.CENTER);
		this.setBackground(UIManager.getColor("ToolBar.background"));
		this.revalidate();
		this.repaint();
		
		GestorContenedor gestorEstado=Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_ESTADO);
		gestorEstado.addPanel(null, this, -1, null, null);
		gestorEstado.setVisiblePanels(null, true);
	}
	
//	private int calcularAnchoBarra(){
//		JLabel label=new JLabel(TEXTO_CONEXION);
//		JPanel panel=new JPanel();
//		panel.add(label);
//		panel.validate();
//		return m_size.width/2-label.getPreferredSize().width-12/*Espacio entre componentes(3 espacios x 4 hgap)*/-4/*Borde de la ventana*/;
//	}
	
	public String getLocalizacion(){
		return m_labelLocalizacion.getText();
	}
	
	public synchronized void setLocalizacion(String mensaje,int nivel){
		
		m_labelLocalizacion.setPreferredSize(null);
		String actual=m_labelLocalizacion.getText();
		String[] mensajesNiveles=actual.split(" : ");
		int numMenNiveles=mensajesNiveles.length;
		String nuevoMensaje="";
		for(int i=0;i<nivel && i<numMenNiveles;i++){
			nuevoMensaje+=mensajesNiveles[i]+" : ";
		}
		nuevoMensaje+=mensaje;
		m_labelLocalizacion.setText(nuevoMensaje);
		m_labelLocalizacion.setPreferredSize(new Dimension(m_labelLocalizacion.getPreferredSize().width,m_size.height));
		m_nivelLocalizacion=nivel;
		if(mapLocalizacionAccion.containsKey(nuevoMensaje))
			setAccion(mapLocalizacionAccion.get(nuevoMensaje));
		else setAccion("");
		this.revalidate();
		this.repaint();
	}
	
	public void upNivelLocalizacion(){
		m_labelLocalizacion.setPreferredSize(null);
		String actual=m_labelLocalizacion.getText();
		int pos=actual.lastIndexOf(" : ");
		String nuevoMensaje=actual.substring(0, pos);
		m_labelLocalizacion.setText(nuevoMensaje);
		m_labelLocalizacion.setPreferredSize(new Dimension(m_labelLocalizacion.getPreferredSize().width,m_size.height));
		m_nivelLocalizacion--;
		this.revalidate();
		this.repaint();
	}
	
	public int getNivelLocalizacion(){
		return m_nivelLocalizacion;
	}
	
	public boolean hasBarraProgreso(){
		return !m_finishBarraProgreso;
	}
	
	public boolean isError(){
		return m_error;
	}
	
	/*Se utiliza cuando no hay barra de proceso, si la hay tendriamos que llamar a setFinishBarraProceso*/
	public synchronized void setAccion(String mensaje){
		m_labelAccion.setPreferredSize(null);
		m_labelAccion.setText(mensaje);
		m_labelAccion.setPreferredSize(new Dimension(m_labelAccion.getPreferredSize().width,m_size.height));
		/*if(this.isAncestorOf(m_barraProgreso)){
			this.remove(m_barraProgreso);*/
			this.add(m_labelAccion,BorderLayout.EAST);
		/*}*/
		mapLocalizacionAccion.put(getLocalizacion(), mensaje);
		this.validate();
		this.repaint();
	}
	
	public void setBarraProgreso(){
		m_barraProgreso.setString(TEXTO_CONEXION);
		if(this.isAncestorOf(m_labelAccion)){
			this.remove(m_labelAccion);
			this.add(m_barraProgreso,BorderLayout.EAST);
		}
		this.validate();
		this.repaint();
		
		// Hacemos que vaya avanzando la barra de progreso en un nuevo hilo
		m_finishBarraProgreso=false;
 	   	SwingWorker progressBarWorker=new SwingWorker(){
 			public Object construct(){
 				doWorkEvent();
 				return null;
 			}
 		};
 		progressBarWorker.start();
	}
	
	private void doWorkEvent(){
		m_progress=0;
		while(!m_finishBarraProgreso && m_progress!=m_maximoProgreso){
				updateBar(m_progress);
				try{	
	                Thread.sleep(500);
	                m_progress++;
				}catch(InterruptedException ie){ie.printStackTrace();}
		}
	}

    private void updateBar(final int progress){
		final Runnable updateMon = new Runnable(){
		    public void run() {
		    	m_barraProgreso.setValue(progress);
		    }
		};
		SwingUtilities.invokeLater(updateMon);
	}
    
    public void setFinishBarraProgreso(final String mensaje,final boolean exito){
    	if(hasBarraProgreso()){
    		m_finishBarraProgreso=true;
			if(exito){
				m_barraProgreso.setValue(m_maximoProgreso);
				/*updateBar(m_maximoProgreso);*/
				m_barraProgreso.setString(TEXTO_FIN_CONEXION);
			}else{
				m_barraProgreso.setString(TEXTO_ERROR_CONEXION);
			}
			m_error=!exito;
			this.validate();
			this.repaint();
			
			final StatusBar thisThis=this;
	    	SwingWorker barWorker=new SwingWorker(){
	 			public Object construct(){
					try{
	 	 			    Thread.sleep(500);
	 				}catch(InterruptedException ie){
	 	 				  ie.printStackTrace();
	 	 			}
	 				return null;
	 			}
	 			public void finished(){
	 				thisThis.remove(m_barraProgreso);
	 				if(mensaje!=null)
	 					setAccion(mensaje);
	 				thisThis.validate();
	 				thisThis.repaint();
	 			}
	    	};
			barWorker.start();
    	}
		
    }
	
}