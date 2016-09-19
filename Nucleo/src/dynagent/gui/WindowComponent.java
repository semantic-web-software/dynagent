package dynagent.gui;

import gdev.gawt.GTable;
import gdev.gawt.utils.GFocusTraversalPolicy;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dynagent.common.communication.communicator;
import dynagent.common.utils.Auxiliar;
import dynagent.gui.utils.Converter;

public class WindowComponent{
	
	private static final long serialVersionUID = 1L;

	private WindowComponent parentDialog;
	
	private WindowComponent mainDialog;
	
	public Component originalGlassPane;
	
	private KeyEventDispatcher keyEventDispatcher;
	
	private Container window;
	
	private GFocusTraversalPolicy focusTraversalPolicy;
	
	private boolean lockChangeStateEvent;//Permite o no permite el cambio de estado habilitado/deshabilitado de los eventos
	
	private boolean enabledContainer=true;
	
	private KnowledgeBaseAdapter kba;
	
	public WindowComponent(JApplet applet,KnowledgeBaseAdapter kba) throws MalformedURLException{
		window=applet;
		this.kba=kba;
		Window w=SwingUtilities.getWindowAncestor(applet);
		w.setIconImage(communicator.getIconNotCache(Singleton.getInstance().getCodeBaseJar(),null,"icon",0,0).getImage());
		configGlassPane();
	}
	
	public WindowComponent(JFrame frame,WindowComponent parent,KnowledgeBaseAdapter kba){
		window=frame;
		this.kba=kba;
		this.setParentDialog(parent);
		if(kba.getServer()!=null)
			frame.setIconImage(kba.getServer().getIcon("icon").getImage());
		configGlassPane();
	}
	
	public WindowComponent(JDialog dialog,WindowComponent parent,KnowledgeBaseAdapter kba) {
		window=dialog;
		this.kba=kba;
		this.setParentDialog(parent);
		if(kba.getServer()!=null)
			dialog.setIconImage(kba.getServer().getIcon("icon").getImage());
		configGlassPane();
	}
	
	//Configura todo lo que necesitamos del glassPane. El glassPane es un panel transparente que podemos hacer visible para que el usuario no pueda hacer click en ningun sitio
	private void configGlassPane(){
		lockChangeStateEvent=false;
		// Le añadimos al glassPane las escuchas de eventos de raton vacias para que bloquee los eventos del usuario(Raton y teclado). Esto ocurrira cuando pongamos el glassPane a visible
		originalGlassPane=getGlassPane();
		disableEventsGlassPane(originalGlassPane);
		keyEventDispatcher=new KeyEventDispatcher(){
			public boolean dispatchKeyEvent(KeyEvent ev) {
				//System.err.println("glassPane.isVisible():"+glassPane.isVisible()+" getComponent().isActive():"+getComponent().isActive());
				//Si el glassPane no esta visible significa que no tenemos que evitar ningun evento. Si no esta activa tampoco, ya que si no evitamos los eventos de teclas sobre ventanas de avisos
				if(!getGlassPane().isVisible() || !getComponent().isActive()){
					return false;//Retornando falso hacemos que el dispatcher por defecto lo procese
				}else{
					ev.consume();
					return true; 	
				}
			}
	    };
	    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
	    originalGlassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void disableEventsGlassPane(Component glassPane){
		glassPane.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {e.consume();}
			public void mouseEntered(MouseEvent e) {e.consume();}
			public void mouseExited(MouseEvent e) {e.consume();}
			public void mousePressed(MouseEvent e) {e.consume();}
			public void mouseReleased(MouseEvent e) {e.consume();}
		});
		glassPane.addMouseMotionListener(new MouseMotionListener(){
			public void mouseDragged(MouseEvent e) {e.consume();}
			public void mouseMoved(MouseEvent e) {e.consume();}
		});
	}
	
	public Component getGlassPane(){
		Component glassPane=null;
		if(window instanceof JApplet)
			glassPane=((JApplet)window).getGlassPane();
		else if(window instanceof JFrame)
			glassPane=((JFrame)window).getGlassPane();
		else if(window instanceof JDialog)
			glassPane=((JDialog)window).getGlassPane();
		
		return glassPane;
	}
	
	public void setGlassPane(Component glassPane){
		
		if(window instanceof JApplet)
			((JApplet)window).setGlassPane(glassPane);
		else if(window instanceof JFrame)
			((JFrame)window).setGlassPane(glassPane);
		else if(window instanceof JDialog)
			((JDialog)window).setGlassPane(glassPane);
			
		//glassPane.setVisible(false);
	}

	public WindowComponent getMainDialog() {
		return mainDialog;
	}

	public void setMainDialog(WindowComponent mainDialog) {
		this.mainDialog = mainDialog;
	}

	public WindowComponent getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(WindowComponent parentDialog) {
		this.parentDialog = parentDialog;
	}
	
	public Window getComponent(){
		Window wind=null;
		if(window instanceof JApplet)
			wind=SwingUtilities.getWindowAncestor(window);
		else{
			wind=(Window)window;
		}
		
		return wind;
	}

//	public String toString(){
//		if(mainDialog!=null)
//			return "Dialog "+this.getTitle()+" Padre "+parentDialog.getTitle()+" Main "+mainDialog.getTitle();
//		else
//			return "Dialog "+this.getTitle()+" Padre "+parentDialog.getTitle()+" Main null";
//	}
	
	public void enabledEvents(){
		if(!lockChangeStateEvent && originalGlassPane.isVisible()){
			//System.err.println("*************Habilita "+this);
			//Auxiliar.printCurrentStackTrace();
			originalGlassPane.setVisible(false);
			getComponent().validate();
			if(kba!=null){
				//Necesitamos que sea en un invokeLater para dar tiempo a que se procesen los eventos de gestion
				//del foco de la edicion de tablas y que focusTraversalPolicy.isProcessingFocusTable() devuelva
				//lo correcto dentro de setEnabledQuestionTask. 
				final Runnable processQuestion = new Runnable() {
					public void run() {
						processQuestionTask();
					}
				};
				SwingUtilities.invokeLater(processQuestion);
			}
		}
	}
	
	public void disabledEvents(){
		if(!lockChangeStateEvent && !originalGlassPane.isVisible()){
			//System.err.println("*************Deshabilita "+this);
			//Auxiliar.printCurrentStackTrace();
			originalGlassPane.setVisible(true);
			getComponent().validate();
		}
	}
	
	//Este método espera hasta que focusTraversalPolicy.isProcessingFocusTable() devuelva false ya que si no
	//se ejecuta en un contexto que puede provocar fallos de sesiones(usar sesion incorrecta) o fallo de cardinalidad que
	//deberia procesarse antes que esta questionTask
	private void processQuestionTask(){
		final Runnable enabledQuestion = new Runnable() {
			public void run() {
				processQuestionTask();
			}
		};
		
		if(focusTraversalPolicy==null || !focusTraversalPolicy.isProcessingFocusTable()){
			//System.err.println("ENTRAAA EN processQuestionTask");
			Singleton.getInstance().getQuestionTaskManager().process(kba);
		}else{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			SwingUtilities.invokeLater(enabledQuestion);
		}
	}
	
	//Coloca una imagen, en glassPane, del contentPane actual pero oscurecida
	public void setEnabledContainer(boolean enabled) throws AWTException{
		if(enabled){
			if(getGlassPane()!=originalGlassPane)
				setGlassPane(originalGlassPane);
			enabledEvents();
			getComponent().repaint();
		}else{
			Converter converter = new Converter(this);
			final JImagePanel panel = new JImagePanel(converter.getOut());
			disableEventsGlassPane(panel);
			//Lo hago al final del hilo del awt porque si no tenemos el problema de que eventos que no deberian ser tratados al estar deshabilitados son tratados porque se disparan despues de
			//que hagamos el cambio del glassPane.
			final Runnable doDark = new Runnable() {
				public void run() {
					setGlassPane(panel);
					panel.setVisible(true);
					//System.out.println("*************Pone Imagen oscurecida "+this);
					getComponent().validate();
					getComponent().repaint();
				}
			};
			SwingUtilities.invokeLater(doDark);
		}
	}
	
	public void lockChangeStateEvents(boolean lock){
		lockChangeStateEvent=lock;
	}
	
	public void dispose() {
		if(window instanceof JApplet)
			System.err.println("WARNING: El tipo JApplet no implementa dispose");
		else if(window instanceof JFrame){
			if(focusTraversalPolicy!=null)
				focusTraversalPolicy.setEnabled(false);
			((JFrame)window).dispose();
		}
		else if(window instanceof JDialog){
			if(focusTraversalPolicy!=null)
				focusTraversalPolicy.setEnabled(false);
			((JDialog)window).dispose();
		}
		
		//Cuando se deseche la ventana quitamos el keyEventDispatcher creado porque si no se queda ahi eternamente
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher);
	}
	
	public void setLocationRelativeTo(Component comp) {
		if(window instanceof JApplet)
			((JApplet)window).setLocation(comp.getLocation());
		else if(window instanceof JFrame)
			((JFrame)window).setLocationRelativeTo(comp);
		else if(window instanceof JDialog)
			((JDialog)window).setLocationRelativeTo(comp);
		
	}
	
	public void setContentPane(Container container) {
		if(window instanceof JApplet)
			((JApplet)window).setContentPane(container);
		else if(window instanceof JFrame)
			((JFrame)window).setContentPane(container);
		else if(window instanceof JDialog)
			((JDialog)window).setContentPane(container);
	}
	
	public Container getContentPane() {
		Container container=null;
		
		if(window instanceof JApplet)
			container=((JApplet)window).getContentPane();
		else if(window instanceof JFrame)
			container=((JFrame)window).getContentPane();
		else if(window instanceof JDialog)
			container=((JDialog)window).getContentPane();
		
		return container;
	}
	
	public void setTitle(String title) {
		if(window instanceof JApplet){
			Window w=SwingUtilities.getWindowAncestor(window);
			if(w instanceof JFrame)
				((JFrame)w).setTitle(title);
			else if(w instanceof JDialog)
				((JDialog)w).setTitle(title);
			/*else if(w instanceof Frame){
				((Frame)w).setTitle(title);
				w.validate();
				w.repaint();
			}else System.err.println("NINGUNOOOOO:"+w);*/
		}else if(window instanceof JFrame){
			((JFrame)window).setTitle(title);
		}else if(window instanceof JDialog)
			((JDialog)window).setTitle(title);
	}

	public GFocusTraversalPolicy getFocusTraversalPolicy() {
		return focusTraversalPolicy;
	}

	public void setFocusTraversalPolicy(GFocusTraversalPolicy focusTraversalPolicy) {
		this.focusTraversalPolicy = focusTraversalPolicy;
		this.focusTraversalPolicy.setEnabled(true);
	}
	
	public KnowledgeBaseAdapter getKnowledgeBase(){
		return kba;
	}
	
	public class JImagePanel extends JPanel{  
		
		private static final long serialVersionUID = 1L;
		private Image image=null;  

		public JImagePanel(Image image){
			this.image=image;
			this.setBorder(BorderFactory.createEmptyBorder());
		}  

		protected void paintComponent(Graphics g) {
			Graphics2D g2 =(Graphics2D) g; 
			if(getImage()!=null)  
				g2.drawImage(getImage(), 0, 0, getWidth(), getHeight(), null);  
		}  

		public Image getImage(){ 
			return image;
		}
	}

}
