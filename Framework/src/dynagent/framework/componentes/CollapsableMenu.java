package dynagent.framework.componentes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import dynagent.common.communication.docServer;
import dynagent.framework.ConstantesGraficas;
import dynagent.framework.utilidades.CreadorIconos;

public class CollapsableMenu extends JPanel implements ActionListener,KeyListener{

	private static final long serialVersionUID = 1L;
	private LinkedHashMap<String, BranchPanel> aps;
	private BranchPanel actualBranchPanel;
	private JButton actualButton = null;
	private JPanel panel;
	private JScrollPane scrollPane;
	private JButton upButton;
	private JButton downButton;
	private docServer server;

	public CollapsableMenu(docServer server){
		aps = new LinkedHashMap<String, BranchPanel>();
		this.server = server;
	} 

	public void addBranchPanel(String label, BranchPanel branchPanel ){
		branchPanel.addActionListener(this);
		branchPanel.addKeyListener(this);
		aps.put(label, branchPanel);		
	}	

	public void init(){
		setLayout(new BorderLayout(0,0));
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(UIManager.getColor("ToolBar.background"));
		
		panel=new JPanel();
		GridBagLayout gblayout=new GridBagLayout();
		panel.setLayout(gblayout);
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setBackground(UIManager.getColor("ToolBar.background"));
		panel.setVisible(true);

		Iterator<String> it = aps.keySet().iterator();
		int i=0;
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.gridx=0;
		gbc.ipadx=0;
		gbc.ipady=0;
		while(it.hasNext()){
			String label = it.next();
			gbc.gridy=i;
			BranchPanel parentButton=aps.get(label);
			gblayout.setConstraints(parentButton, gbc);
			panel.add(parentButton);
			gbc.gridy=i+1;
			gblayout.setConstraints(parentButton.getPanel(), gbc);
			panel.add(parentButton.getPanel());
			aps.get(label).getPanel().setVisible(false);
			i+=2;
		}
		Dimension dim=ConstantesGraficas.dimMenu;
		JPanel panelAux=new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
		//panelAux.setPreferredSize(dim);
		panelAux.add(panel);
		panelAux.setBackground(panel.getBackground());
		scrollPane = new JScrollPane(panelAux,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(dim);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		ImageIcon iconUp = server.getIcon(null, "arrow_up", 12, 12);//CreadorIconos.crearIcono("imagenes/arrow_up_20_20.gif"/*Up16.gif"*/,new Dimension(12, 12));
		upButton= new JButton(iconUp);
		upButton.setVisible(false);
		//upButton.setContentAreaFilled(false);
		upButton.setFocusPainted(false);
		upButton.setBorderPainted(false);
		upButton.setMargin(new Insets(0,0,0,0));
		//upButton.setBackground(panel.getBackground());
		add(upButton,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		ImageIcon iconDown = server.getIcon(null, "arrow_down", 12, 12);//CreadorIconos.crearIcono("imagenes/arrow_down_20_20.gif"/*Down16.gif"*/,new Dimension(12, 12));
		downButton = new JButton(iconDown);
		add(downButton,BorderLayout.SOUTH);
		downButton.setVisible(false);
		//downButton.setContentAreaFilled(false);
		downButton.setFocusPainted(false);
		downButton.setBorderPainted(false);
		downButton.setMargin(new Insets(0,0,0,0));
		//downButton.setBackground(panel.getBackground());
		
		ActionListener actionListener=new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("upButton")){
					applyScroll(false,ConstantesGraficas.IncrementScrollVertical);
				}else{
					applyScroll(true,ConstantesGraficas.IncrementScrollVertical);
				}
			}
			
		};
		
		final Timer timer=new Timer(50, actionListener);//Sirve para que al dejar pulsado el boton se repita la ejecucion del actionPerformed
		
		MouseListener mouseListener=new MouseAdapter(){

			public void mousePressed(MouseEvent e) {
				if(e.getSource().equals(upButton))
					timer.setActionCommand("upButton");
				else timer.setActionCommand("downButton");
				timer.start();
			}

			public void mouseReleased(MouseEvent arg0) {
				timer.stop();
			}
			
		};
		downButton.addMouseListener(mouseListener);
		upButton.addMouseListener(mouseListener);
		
		scrollPane.addMouseWheelListener(new MouseWheelListener(){
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches < 0) {
					applyScroll(false,ConstantesGraficas.IncrementScrollVertical*3);
				} else {
					applyScroll(true,ConstantesGraficas.IncrementScrollVertical*3);
				}		
			}
		});
		
		//Reescribimos la funcionalidad de las teclas arriba y abajo del teclado sobre el JScrollPane
		//ya que nos interesa que se mueva por los botones en vez de que se utilice solo para mover el scroll
		//Tambien reescribimos izquierda y derecha para que sirvan para replegar y desplegar su contenido
		
		InputMap im = scrollPane.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		
		Action upAction = new AbstractAction(){
			// Hacemos que la tecla up avance al siguiente componente
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				Component nextComponent=KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy().getComponentBefore(KeyboardFocusManager.getCurrentKeyboardFocusManager().getCurrentFocusCycleRoot(), KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
				if(scrollPane.isAncestorOf(nextComponent)/*Evitamos que se salga del menu*/){
					
					//Movemos el scroll para que el boton que va a coger el foco se muestre
					showComponentInScrollPane(nextComponent,false);
										
					// Transferimos el foco al anterior boton
					KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner().transferFocusBackward();
				}
			}
		};
		
		Action downAction = new AbstractAction(){
			// Hacemos que la tecla down avance al siguiente componente
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				Component nextComponent=KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy().getComponentAfter(KeyboardFocusManager.getCurrentKeyboardFocusManager().getCurrentFocusCycleRoot(), KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
				if(scrollPane.isAncestorOf(nextComponent)/*Evitamos que se salga del menu*/){
					
					//Movemos el scroll para que el boton que va a coger el foco se muestre
					showComponentInScrollPane(nextComponent,true);
							
					//Transferimos el foco al siguiente boton
					KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner().transferFocus();
				}
			}
		};
		
		Action leftAction = new AbstractAction(){
			// Hacemos que la tecla izquierda repliegue el boton si es un BranchPanel o si no lo es que le de el foco al BranchPanel padre
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				AbstractButton b=(AbstractButton)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				if(b instanceof BranchPanel){
					if(b==actualBranchPanel)
						b.doClick();
				}else actualBranchPanel.requestFocusInWindow();
			}
		};
		
		Action rightAction = new AbstractAction(){
			// Hacemos que la tecla derecha haga click sobre el boton
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				AbstractButton b=(AbstractButton)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				if(b instanceof BranchPanel){
					if(b!=actualBranchPanel)
						b.doClick();
				}else if(b!=actualButton)
					b.doClick();
			}
		};
			
		scrollPane.getActionMap().put(im.get(up), upAction);
		scrollPane.getActionMap().put(im.get(down), downAction);
		
		scrollPane.getActionMap().put(im.get(left), leftAction);
		scrollPane.getActionMap().put(im.get(right), rightAction);
	}

	//Hace scroll sobre el ScrollPane para que muestre el componente, indicando si mover hacia abajo o hacia arriba
	private void showComponentInScrollPane(Component component,boolean down){
		Point point=component.getLocation();
		if(!(component instanceof BranchPanel)){
			//Entra cuando esta dentro de uno de los menus
			Component parent=component;
			do{
				parent=parent.getParent();
				point.y+=parent.getLocation().getY();
			}while(parent.getParent()!=null && !(parent instanceof BranchPanel));
		}
		
		if(!scrollPane.getViewport().getViewRect().contains(point)){
			applyScroll(down, (int)component.getPreferredSize().getHeight());
		}
	}
	
	private void togglePanelVisibility(BranchPanel ap){        
		if(ap.getPanel().isShowing()){
			actualBranchPanel = null;
			ap.getPanel().setVisible(false);
		}else{
			actualBranchPanel = ap;
			ap.getPanel().setVisible(true);
		}
				
		ap.getParent().validate();
		
		if(scrollPane.getPreferredSize().getHeight()<panel.getPreferredSize().getHeight()){
			upButton.setVisible(true);
			downButton.setVisible(true);
		}else{
			upButton.setVisible(false);
			downButton.setVisible(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof BranchPanel){//Al pulsar sobre los botones padre
			BranchPanel ap = (BranchPanel)e.getSource();
			if(actualBranchPanel!=null && actualBranchPanel!=ap){
				actualBranchPanel.toggleSelection();
				togglePanelVisibility(actualBranchPanel);
			}    	
			ap.toggleSelection();
			togglePanelVisibility(ap);
			if(actualBranchPanel!=null){
				final Runnable doViewPosition = new Runnable() {
					public void run() {
						actualBranchPanel.validate();
						if(downButton.isVisible() || upButton.isVisible())
							setViewPosition(actualBranchPanel.getLocation(CollapsableMenu.this.getLocation()));
					}
				};
				SwingUtilities.invokeLater(doViewPosition);
			}
		}else if(e.getSource() instanceof JButton){//Al pulsar sobre los botones hijo
			if(actualButton!=null){
				actualButton.setFont(new JButton().getFont());				
			}
			JButton button = (JButton)e.getSource();
			BranchPanel ap = findBranchPanel(button);
			if(actualBranchPanel!=null && actualBranchPanel!=ap){
				actualBranchPanel.toggleSelection();
				togglePanelVisibility(actualBranchPanel);
				ap.toggleSelection();
				togglePanelVisibility(ap);				
			} 
			Font font = button.getFont();
			if(ap.isHighlightedItem(ap.getLabelItem(button)))
				button.setForeground(ap.getHighlightedItemColor(ap.getLabelItem(button)));
			button.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
			actualButton = (JButton)e.getSource();				
		}
	}

	public void hideActual(){
		actualButton.setFont(new JButton().getFont());
		actualBranchPanel.toggleSelection();
		togglePanelVisibility(actualBranchPanel);
	}

	public void showBranch(JButton button){
		BranchPanel ap = findBranchPanel(button);
		if(!ap.isSelected()){
			ap.toggleSelection();
			togglePanelVisibility(ap);
		}
	}

	private BranchPanel findBranchPanel(JButton button){
		Iterator<String> it = aps.keySet().iterator();
		while(it.hasNext()){
			String label = it.next();
			BranchPanel bp = aps.get(label);
			if(bp.contains(button))
				return bp;				
		}
		return null;
	}

	public BranchPanel getActualBranchPanel() {
		return actualBranchPanel;
	}

	public JButton getActualButton() {
		return actualButton;
	}
	
	public void applyScroll(boolean down, int increment){
		if((down && downButton.isVisible()) || upButton.isVisible()){
			Point point=scrollPane.getViewport().getViewPosition();
			if(!down){
				point.y-=increment;
			}else{
				point.y+=increment;
			}
			
			setViewPosition(point);
		}
	}
	
	public void setViewPosition(Point point){
		if(point.y<0){
			point.y=0;
			upButton.setEnabled(false);
		}else upButton.setEnabled(true);
		
		if((point.y + scrollPane.getHeight())>panel.getHeight()){
			//System.err.println("Lo modificaaaa ya que point="+point);
			point.y=(int)(panel.getHeight()-scrollPane.getHeight());
			downButton.setEnabled(false);
		}else{
			downButton.setEnabled(true);
		}
		
		scrollPane.getViewport().setViewPosition(point);
	}

	@Override
	//Para que se situe el foco sobre el branchPanel actual o, si no hay actual, sobre el primer branchPanel
	public boolean requestFocusInWindow() {
		return (actualBranchPanel!=null)?actualBranchPanel.requestFocusInWindow():aps.get(aps.keySet().iterator().next()).requestFocusInWindow();
	}

	public void keyPressed(KeyEvent e) {
		boolean process=false;
		Component firstComponent=null;//Se utiliza para saber cual es el primer componente encontrado que comienza por la tecla pulsada
		Component component=null;//Componente que tiene que coger el foco, se busca por debajo del foco actual
		Iterator<String> it = aps.keySet().iterator();
		//Primero nos quedamos con el firstComponent que encontremos que cumple con la tecla pulsada. Luego buscamos por debajo de la localización del foco actual
		//y si encontramos alguno que empiece por la tecla pulsada se lo asignamos a component, el cual tiene prioridad de pedir el foco antes que firstComponent.
		//FirstComponent solo lo utilizamos si no hemos sido capaces de encontrar ningún botón que case con la tecla más abajo del foco actual
		while(it.hasNext() && component==null){
			BranchPanel branch=aps.get(it.next());
			
			if(branch.hasFocus())
				process=true;//Si encontramos el que tiene el foco marcamos process a true para que el proximo que se encuentre que comienza por la tecla pulsada lo asigne a component
			
			//if(process){
				if(!branch.hasFocus() && branch.getTextButton().toLowerCase().startsWith(Character.toLowerCase(e.getKeyChar())+"")){//Si no tiene el foco y cumple con la tecla pulsada
					if(process)
						component=branch;
					else if(firstComponent==null)
						firstComponent=branch;
				}
				
				if(branch.isSelected()){//Si el branch esta seleccionado buscamos en sus hijos
					Iterator<String> itr=branch.getButtons().keySet().iterator();
					while(itr.hasNext() && component==null){
						String nameButton=itr.next();
						JButton button=branch.getButtons().get(nameButton);
						if(button.hasFocus())//Si encontramos el que tiene el foco marcamos process a true para que el proximo que se encuentre que comienza por la tecla pulsada lo asigne a component
							process=true;
						
						if(!button.hasFocus() && button.getText().toLowerCase().startsWith(Character.toLowerCase(e.getKeyChar())+"")){
							if(process)
								component=button;
							else if(firstComponent==null)
								firstComponent=branch.getButtons().get(nameButton);
						}
					}
				}
			//}
		}
		
		if(component!=null){
			showComponentInScrollPane(component,true);
			component.requestFocusInWindow();
		}else if(firstComponent!=null){
			showComponentInScrollPane(firstComponent,false);
			firstComponent.requestFocusInWindow();
		}
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}