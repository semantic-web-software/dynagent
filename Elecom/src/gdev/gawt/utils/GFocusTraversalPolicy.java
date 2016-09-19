package gdev.gawt.utils;

import gdev.gawt.GComponent;
import gdev.gawt.GEdit;
import gdev.gawt.GList;
import gdev.gawt.GTable;
import gdev.gawt.tableCellEditor.TextCellEditor;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;
import gdev.gen.GConfigView;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gfld.GFormTable;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dynagent.common.Constants;
import dynagent.common.communication.docServer;
import dynagent.common.knowledge.instance;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.RowItem;

public class GFocusTraversalPolicy extends LayoutFocusTraversalPolicy{

	private KeyEventDispatcher keyEventDispatcher;
	private PropertyChangeListener propertyChangeListener;
	//private VetoableChangeListener vetoableChangeListener;
	private Container container;
	private Window window;
	private boolean onlyRequired;
	private JViewport viewPort;
	private HashMap<Component,Border> mapBorderComponent;
	private boolean enabled;
	private docServer server;
	private boolean focusLostCellTable;
	private Component focusOwnerWhenWindowLostFocus;
	private IComponentListener componentListener;
	private boolean processFocusTable;//Sera true mientras estemos procesando los cambios de foco dentro de una tabla. Solo sirve para forzar a que no se siga procesando el enter o tab
	private IUserMessageListener messageListener;
	private boolean managingEditionError;
	private boolean selectionMode;
	private boolean processingFocusValueChangedTable;//Sera true mientras estemos procesando el foco de la tabla en una edicion
	private boolean processingEnterTabTable;//Sera true mientras estemos procesando pulsar enter o tab en la tabla en una edicion
	private boolean processingEditionError;//Sera true mientras estemos procesando un error al intentar salir de la edicion de una fila
	private boolean processingKeyEvents;
	private boolean popup;
	
	public GFocusTraversalPolicy(Container container,Window window,JViewport viewPort,ArrayList<GComponent> components,docServer server,IComponentListener componentListener,IUserMessageListener messageListener,final boolean selectionMode, boolean popup) {
		super();
		this.container=container;
		this.window=window;
		this.viewPort=viewPort;
		mapBorderComponent=new HashMap<Component, Border>();
		enabled=false;
		this.server=server;
		this.componentListener=componentListener;
		this.messageListener=messageListener;
		focusLostCellTable=false;
		managingEditionError=false;
		this.selectionMode=selectionMode;
		processingFocusValueChangedTable=false;
		processingEnterTabTable=false;
		processingEditionError=false;
		processingKeyEvents=false;
		this.popup=popup;
		
		if(selectionMode)
			onlyRequired=false;//Conseguimos que el foco este en la tabla de seleccion ya que no es requerida
		else onlyRequired=true;
		
//		 Change the forward focus traversal keys for the application
	    Set<AWTKeyStroke> setForward = new HashSet<AWTKeyStroke>(
	        KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
	            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
	   // set.clear();   // Call clear() if you want to eliminate the current key set
	    KeyStroke keyEnter=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0/*, true*/);
	    setForward.add(keyEnter);
	    //KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalKeys(
	     //   KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
	    container.setFocusTraversalKeys(
	   	        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward);
	    
	    // Change the backward focus traversal keys for the application
	    Set<AWTKeyStroke> setBackward = new HashSet<AWTKeyStroke>(
	        KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
	            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
	    //set.clear();   // Call clear() if you want to eliminate the current key set
	    KeyStroke keyEnterShift=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK/*, true*/);
	    setBackward.add(keyEnterShift);
//	    KeyboardFocusManager.getCurrentKeyboardFocusManager().setDefaultFocusTraversalKeys(
//	        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
	    container.setFocusTraversalKeys(
	   	        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward);
	    
	    container.setFocusTraversalPolicy(this);
	    container.setFocusTraversalPolicyProvider(true);
	    container.setFocusCycleRoot(true);
				
				window.addWindowListener(new WindowAdapter(){
					Component parentFocusOwner=null;
					public void windowDeactivated(WindowEvent e) {
						//System.err.println("windowDeactivated con container.isShowhing:"+GFocusTraversalPolicy.this.container.isShowing()+" GFocusTraversal:"+this.hashCode());
						if(GFocusTraversalPolicy.this.container.isShowing()){//Necesitamos comprobar que el container este activo porque la ventana puede estar perteneciendo a otro container cuando hay varios pasos
							focusOwnerWhenWindowLostFocus=GFocusTraversalPolicy.this.window.getMostRecentFocusOwner();//FocusOwner();
							parentFocusOwner=focusOwnerWhenWindowLostFocus.getParent();
							/*if(focusOwnerWhenWindowLostFocus instanceof AbstractButton){
								Component cParent=focusOwnerWhenWindowLostFocus;
								do{
									cParent=cParent.getParent();
								}while(cParent!=null && !(cParent instanceof GComponent));
								
								if(cParent!=null && cParent instanceof GTable){
									focusOwnerWhenWindowLostFocus=((GTable)cParent).getTable();
								}
							}*//*else if(focusOwnerWhenWindowLostFocus.getParent() instanceof JTable){
								focusOwnerWhenWindowLostFocus=focusOwnerWhenWindowLostFocus.getParent();
							}*/
							
							//System.err.println("windowDeactivated:"+focusOwnerWhenWindowLostFocus);
						}
					}
					public void windowActivated(WindowEvent arg0) {
						//System.err.println("windowActivated:"+focusOwnerWhenWindowLostFocus+" managingEditionError:"+managingEditionError+" GFocusTraversal:"+this.hashCode());
						//System.err.println("windowActivated con container.isShowhing:"+GFocusTraversalPolicy.this.container.isShowing()+" GFocusTraversal:"+this.hashCode());

						//Necesitamos comprobar que el container este activo porque la ventana puede estar perteneciendo a otro container cuando hay varios pasos
						//eso provoca que se haga una peticion del foco en un componente de un container incorrecto haciendo que el foco se pierda
						if(GFocusTraversalPolicy.this.container.isShowing()	&& focusOwnerWhenWindowLostFocus!=null && !managingEditionError/*No nos interesa pedir el foco ya que se encargará de pedirlo reSelect*/){
							if(focusOwnerWhenWindowLostFocus instanceof AbstractButton){
								Component cParent=focusOwnerWhenWindowLostFocus;
								do{
									cParent=cParent.getParent();
								}while(cParent!=null && !(cParent instanceof GComponent));
								
								if(cParent!=null && cParent instanceof GTable/* && ((GTable)cParent).getModel().isEditing()*/){
									focusOwnerWhenWindowLostFocus=((GTable)cParent).getTable();
								}
								
								//Añadido aqui y comentado fuera de este if porque parece que solo nos interesa recuperar el foco en el lugar correcto cuando sea un boton. En los demas casos da problemas ya que evita que pase
								//el foco. Por ejemplo al intentar salir de una celda de una tabla y que nos salga un mensaje informativo o de confirmacion de nueva creacion
//								if(!focusOwnerWhenWindowLostFocus.requestFocusInWindow()){
//									if(parentFocusOwner!=null){
//										if(parentFocusOwner instanceof JTable){
//											if(!((JTable)parentFocusOwner).isEditing()){
//												boolean exito=parentFocusOwner.requestFocusInWindow();
//												//System.err.println("************************************************windowActivated con parent:"+parentFocusOwner.getClass()+" exito:"+exito);
//											}
//										}
//									}
//								}else{
//									//System.err.println("************************************************windowActivated con:"+focusOwnerWhenWindowLostFocus.getClass());
//								}
							}
							focusOwnerWhenWindowLostFocus.requestFocus();
							if(!focusOwnerWhenWindowLostFocus.requestFocusInWindow()){
								if(parentFocusOwner!=null){
									if(parentFocusOwner instanceof JTable){
										if(!((JTable)parentFocusOwner).isEditing()){
											parentFocusOwner.requestFocus();
											parentFocusOwner.requestFocusInWindow();
											
											if(parentFocusOwner.getParent() instanceof GTable){
												showToolTipOfCell((GTable)parentFocusOwner.getParent(), (JComponent)((JTable)parentFocusOwner).getEditorComponent());
											}
											//System.err.println("************************************************windowActivated con parent:"+parentFocusOwner.getClass()+" exito:"+exito);
										}
									}
								}
							}else{
								//System.err.println("************************************************windowActivated con:"+focusOwnerWhenWindowLostFocus.getClass());
							}
						}
					}
					@Override
					public void windowClosed(WindowEvent arg0) {
						//System.err.println("WINDOWCLOSED GFocusTraversalPolicy "+this.hashCode());
						setEnabled(false);//Quitamos el registro en KeyEventManager ya que si no no se libera la memoria por el recolector de basura
					}
					
				});
		
		keyEventDispatcher=new KeyEventDispatcher(){

			public boolean dispatchKeyEvent(KeyEvent ev) {
				//System.err.println("hashCode:"+GFocusTraversalPolicy.this.container.hashCode()+ "isShowing:"+GFocusTraversalPolicy.this.container.isShowing()+" isValid:"+GFocusTraversalPolicy.this.container.isValid()+" isVisible:"+GFocusTraversalPolicy.this.container.isVisible()+" displayable:"+GFocusTraversalPolicy.this.container.isDisplayable()+" enabled:"+GFocusTraversalPolicy.this.container.isEnabled());
				if(GFocusTraversalPolicy.this.container.isShowing()){
					//System.err.println("TECLA:"+ev.getKeyCode());
					/*if(ev.getID()==KeyEvent.KEY_RELEASED){//Lo hacemos en el keyReleased para permitir que en algun evento en el que esperemos un keyPressed del enter siga funcionando
						if(ev.getKeyCode()==KeyEvent.VK_ENTER){
							Component c=ev.getComponent();
							//System.err.println("Component:"+c+" ComponentComponent:"+ev.getComponent()+" siendo:"+(c instanceof AbstractButton));
							onlyRequired=true;
							//KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(c, ev);
							if(c.getParent() instanceof JTable){
								c.setFocusTraversalKeysEnabled(false);
							}
						}
					}else*/ if(ev.getID()==KeyEvent.KEY_PRESSED){
						processingKeyEvents=true;
						Component c=ev.getComponent();
						//System.err.println("Component:"+c+" ComponentComponent:"+ev.getComponent()+" siendo:"+(c instanceof AbstractButton));
						if(ev.getKeyCode()==KeyEvent.VK_TAB){
							onlyRequired=false;
							//System.err.println("Parent:"+c.getParent());
							if(c.getParent() instanceof JTable){
								//System.err.println("Deshabilita");
								c.setFocusTraversalKeysEnabled(false);
							}else if(c.getParent() instanceof JPanel && c.getParent().getParent() instanceof JTable){//Necesario para DateCellEditor y FileCellEditor
								c.setFocusTraversalKeysEnabled(false);
							}else if(c instanceof JList){
								c.setFocusTraversalKeysEnabled(false);
							}else if(c instanceof JTable){//Para que cuando este en la fila de la tabla se comporte igual que si estuviera en el cellEditor, gestionando nosotros el tab
								c.setFocusTraversalKeysEnabled(false);
							}else c.setFocusTraversalKeysEnabled(true);
						}else if(ev.getKeyCode()==KeyEvent.VK_ENTER){
	//						if(c instanceof AbstractButton){
	//							System.err.println("ENTER pa false");
	//							c.setFocusTraversalKeysEnabled(false);
	//						}else if(c.getParent() instanceof JTable){
	//							c.setFocusTraversalKeysEnabled(false);
	//						}else{
	//							onlyRequired=true;//Porque si estamos en una tabla, al pedir el siguiente componente del foco no nos daria el correcto
	//						}
							
							
							//System.err.println("Component:"+c+" ComponentComponent:"+ev.getComponent()+" siendo:"+(c instanceof AbstractButton));
							onlyRequired=true;
							//KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(c, ev);
							
							if(c instanceof AbstractButton && !(c instanceof javax.swing.JCheckBox)){
								//System.err.println("ENTER pa false");
								c.setFocusTraversalKeysEnabled(false);
							}else if(c.getParent() instanceof JPanel && c.getParent().getParent() instanceof JTable){//Necesario para DateCellEditor y FileCellEditor
								c.setFocusTraversalKeysEnabled(false);
							}else if(c.getParent() instanceof JTable){
								c.setFocusTraversalKeysEnabled(false);
							}else if(c instanceof JList){
								c.setFocusTraversalKeysEnabled(false);
							}else if(c instanceof JTable){//Para que cuando este en la fila de la tabla se comporte igual que si estuviera en el cellEditor, gestionando nosotros el enter
								c.setFocusTraversalKeysEnabled(false);
							}else c.setFocusTraversalKeysEnabled(true);
						}
					}else if(ev.getID()==KeyEvent.KEY_RELEASED){
						processingKeyEvents=false;
					}
				}
				return false;//Retornando falso hacemos que el dispatcher por defecto lo procese
			}
	    };
	    propertyChangeListener=new PropertyChangeListener(){

	    	//Nos indica si el viewport pasado es el viewport parent del componente. No nos interesa que solo sea un viewport ancestro, tiene que ser el viewport padre
	    	private boolean isParentViewPort(JViewport parent,Component c){
	    		//if(!(c instanceof JViewport)){
	    			Container container=c.getParent();
		    		while(container!=null && !(container instanceof JViewport)){
		    			container=container.getParent();
		    		}
		    		return container==parent;
		    		
	    		//}else return c==parent;
	    	}
	    	
			public void propertyChange(final PropertyChangeEvent e) {
				if(GFocusTraversalPolicy.this.container.isShowing()){
//					System.err.println("------PropertyChangeEvent:getPropertyName "+e.getPropertyName());
//					System.err.println("--PropertyChangeEvent:getNewValue "+(e.getNewValue()!=null?e.getNewValue().getClass():null));
//					System.err.println("--PropertyChangeEvent:getOldValue "+(e.getOldValue()!=null?e.getOldValue().getClass():null));
					if(e.getPropertyName().equals("permanentFocusOwner")){
						if(e.getNewValue() instanceof JComponent){
							Component c=(Component)e.getNewValue();
							if(GFocusTraversalPolicy.this.viewPort!=null && GFocusTraversalPolicy.this.viewPort.isAncestorOf(c)){
								/*mapBorderComponent.put(c,c.getBorder());
								//if(!m_modoConsulta && !m_modoFilter)
									c.setBorder(GConfigView.borderSelected);
								*/	
//									System.err.println("isActive:"+GFocusTraversalPolicy.this.window.isActive()+" isShowing:"+c.isShowing());
								if(GFocusTraversalPolicy.this.window.isActive() && c.isShowing()){
									
									//Mientras no tengamos el componente cuyo viewport es el nuestro no hacemos nada. Esto es para evitar
									//obtener unas coordenadas que son de otro viewport como por ejemplo el del scroll de las tablas.
									//System.err.println("c:"+c);
									while(!isParentViewPort(GFocusTraversalPolicy.this.viewPort, c))
										c=c.getParent();
									//System.err.println("c despues:"+c);
									
									//Vamos sumando la posicion por los padres del componente para obtener su posicion real respecto al container principal ya que
									//getLocation de un componente devuelve la ubicacion respecto a su container, no respecto al container principal
									double x=c.getLocation().getX();
									double y=c.getLocation().getY();
									Component parent=c;
									do{
										parent=parent.getParent();
										// Si es un scrollPane nos interesa usar como localización la posicion en la que se encuentra, pero solo si el padre es nuestro ViewPort
										if(parent instanceof JScrollPane && parent==GFocusTraversalPolicy.this.viewPort.getParent()){
											x+=((JScrollPane)parent).getHorizontalScrollBar().getValue();
											y+=((JScrollPane)parent).getVerticalScrollBar().getValue();
										}
										else{
											x+=parent.getLocation().getX();
											y+=parent.getLocation().getY();
										}
									}while(parent.getParent()!=null && parent!=GFocusTraversalPolicy.this.container);
										
									final Point p=new Point((int)x,(int)y);
									
									//System.err.println("point:"+p);
									if(!GFocusTraversalPolicy.this.viewPort.getViewRect().contains(p)){
										/*System.err.println("Entra en viewport");
										Exception ex=new Exception();
										ex.printStackTrace();*/
										
										//Si x o y se encuentra ya en la posicion del viewport, utilizamos ese valor ya que solo nos interesa movernos a
										//una ubicacion que este fuera del rectangulo del viewport
										Rectangle rectangle=GFocusTraversalPolicy.this.viewPort.getViewRect();
										if(rectangle.x<x && rectangle.width+rectangle.x>x)
											p.x=rectangle.x;
										if(rectangle.y<y && rectangle.height+rectangle.y>y)
											p.y=rectangle.y;
											
										//Lo hacemos en un invokeLater porque por ejemplo cuando un popup es cerrado, por un momento el foco va
										//al principio del formulario. De esta manera, si en el momento de cambiar el viewport ya no tiene el foco no hacemos nada
										final Runnable doFinished = new Runnable() {
											public void run() {
												if(((Component)e.getNewValue()).hasFocus()){
													GFocusTraversalPolicy.this.viewPort.setViewPosition(p);
												}
											}
										};
										SwingUtilities.invokeLater(doFinished);
									}
								}
							}
							
							if(c instanceof JTable && !focusLostCellTable)
								c.setFocusTraversalKeysEnabled(false);
							
						}else if(e.getOldValue() instanceof JComponent){
							
							JComponent c=(JComponent)e.getOldValue();
							if(c!=null){
								if(c.getParent() instanceof JTable)
									focusLostCellTable=true;
								else focusLostCellTable=false;
							}else{
								focusLostCellTable=false;
							}
	//								//if(!m_modoConsulta && !m_modoFilter)
	//								if(mapBorderComponent.containsKey(c))
	//									c.setBorder(mapBorderComponent.remove(c));
						}
						
					}
	//				else if(e.getPropertyName().equals("activeWindow")){
	//					Window w=(Window)e.getNewValue();
	//					if(w!=null){
	//						System.err.println("window Active"+w.getClass()+" siendo container:"+GFocusTraversalPolicy.this.container.getClass());
	//						if(/*w.isAncestorOf(GFocusTraversalPolicy.this.container) || */w==GFocusTraversalPolicy.this.container){
	//							//System.err.println("Es ancestroooo1");
	//							setEnabled(true);
	//						}
	//					}else{
	//						w=(Window)e.getOldValue();
	//						if(w!=null){
	//							if(/*w.isAncestorOf(GFocusTraversalPolicy.this.container) || */w==GFocusTraversalPolicy.this.container){
	//								//System.err.println("Es ancestroooo2");
	//								setEnabled(false);
	//							}
	//						}
	//					}
	//				}
				}else{
					if(selectionMode)
						onlyRequired=false;
					else onlyRequired=true;
				}
			}
	    };
	    
	    /*vetoableChangeListener=new VetoableChangeListener(){

			public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
				System.err.println("VetoableChange:"+e.toString());
				Exception ex=new Exception();
				ex.printStackTrace();
			}
	    	
	    };*/
	    
	    Iterator<GComponent> itrComponents=components.iterator();
	    while(itrComponents.hasNext()){
	    	GComponent component=itrComponents.next();
	    	if(component instanceof GEdit){
	    		GEdit gEdit=(GEdit)component;
	    		JComponent editor=gEdit.getEditor();
	    		if(editor instanceof JTextArea){		 
	    			// Modificamos las teclas para cambiar de foco de los memos ya que, por defecto, son distintas que las de los otros componentes
	    			editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>(
	    			        KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
	    				            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS)));
	    			editor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>(
	    			        KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
	    				            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS)));
	    		}
	    	}else if(component instanceof GList){
	    		final GList gList=(GList)component;
	    		gList.setKeyListenerExternal(new KeyListener() {
					
					@Override
					public void keyTyped(KeyEvent e) {}
					
					@Override
					public void keyReleased(KeyEvent e) {}
					
					@Override
					public void keyPressed(KeyEvent e) {
						if(e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyCode()==KeyEvent.VK_TAB){
							GFocusTraversalPolicy.this.removeFocusComponent(gList.getButton(),e.getModifiers()==0);
						}
					}
				});
	    	}else if(component instanceof GTable){
		    	final GTable gTable=(GTable)component;
		    	JTable table=gTable.getTable();
		    	InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
				final Action oldTabAction = table.getActionMap().get(im.get(tab));
				
				KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	//			final Action oldEnterAction = m_objTable.getActionMap().get(im.get(enter));
	
				KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
			    final Action oldShiftTabAction = table.getActionMap().get(im.get(shiftTab));
			        
				Action tabAction = new AbstractAction(){
					// Modificamos el pulsar tabulador sobre la fila para que avance a la siguiente columna editable. Si ya estamos en la ultima columna editable
					// requerimos el foco para el siguiente componente. Lo hacemos aqui porque el tabulador en el FocusTraversalPolicy se ejecuta en el evento PRESSED
					// y esto se ejecuta despues
					private static final long serialVersionUID = 1L;
					public void actionPerformed(ActionEvent e){
						processFocusTable=true;
						//System.err.println("ENTRA EN TAB");
						final JTable table = (JTable)e.getSource();
						if(selectionMode || table.getRowCount()==0)//Si estamos en selectionMode lo unico que hacemos es cambiar el foco al siguiente componente
							removeFocusComponent(table,e.getModifiers()==0);
						else{
							//System.err.println("Inicio processingEnterTabTable");
							processingEnterTabTable=true;
							TableModelListener tml=null;
							boolean hasNullRow = gTable.hasNullRow();//Nos sirve para que en los casos de insert/delete por reglas no se quede en bucle infinito al no encontrar un campo donde situarse
							boolean isEditing=table.isEditing();
							final int rowEditing = table.getSelectedRow();
							final int columnEditing = table.getSelectedColumn();
							try{
								if(!isEditing && table.editCellAt(rowEditing, columnEditing)){//Si no estamos en edicion editamos la celda en la que estamos situados
									//System.err.println("EDITA LA CELDA");
									final JComponent editor=(JComponent)table.getEditorComponent();
									editor.requestFocus();
									editor.requestFocusInWindow();
									showToolTipOfCell(gTable, editor);
									//System.err.println("************************************************TAB1");
								}else{
									boolean finderStateAndEditing=false;
									if(isEditing){
										RowItem rowItem = gTable.getModel().getRowData().get(rowEditing);
										if(rowItem.matchesState(RowItem.FINDER_STATE)){
											finderStateAndEditing=true;
										}
									}
									Action action=null;
									if(e.getModifiers()==0)
										action=oldTabAction;
									else action=oldShiftTabAction;
									
									final int[] rowsChanged={0,0};
									tml=getListenerTableChanges(rowsChanged);
									table.getModel().addTableModelListener(tml);
									
									action.actionPerformed(e);
									if(!processFocusTable)
										return;
									
									int row = table.getSelectedRow();
									int column = table.getSelectedColumn();
									
									//Si el action.actionPerformed no ha provocado ningun cambio de fila o columna significa que ha sido evitado por error en la introduccion de datos por lo que no volvemos a intentarlo ya que
									//al volver a intentarlo salta al siguiente campo, y a nosotros nos interesa que se quede en el actual para que el usuario pueda introducir otro valor. Esto es necesario cuando hay un registro
									//de una tabla e intentamos cambiarlo por otro registro que no existe en el finder
									if(table.getColumnCount()>1 && row==rowEditing && column==columnEditing){
										//System.err.println("Sale del tab");
										return;
									}
									
									if(row!=-1){
										int originalRow = row;
										int originalColumn = column;
										
										Integer idoColumn=gTable.getModel().getRowData().get(/*rowEditing*/row).getColumnIdo(column);
										Integer idoParent=null;
										Integer idoColumnMain=null;
										Integer idoColumnEditing=null;
										// Si estamos en una seleccion de finder nos quedamos con el ido principal para saltar a la siguiente columna que sea igual a ese ido,
										// ya que al seleccionar esta claro que no vamos a modificar algo de ese individuo o de sus hijos. Por ejemplo con cod Producto y Descripcion Prod:
										// Si seleccionamos codProducto en finder seguramente no nos interese ir a descripcion si no a la siguiente columna
										if(finderStateAndEditing && !gTable.isNullRow(row)/*Condicion puesta porque fallaba en las tablas de una sola columna al irse a la nullRow*/){
											idoColumnMain=gTable.getModel().getRowData().get(/*rowEditing*/row).getIdRow().getIdo();
											// NUEVO: Saltamos a otra columna aunque no sea del ido principal si el ido de esa columna es distinto del de la columna actual y si el idoParent de esa columna es igual al ido principal
											//Con esto evitamos que tomen el foco otra columna con el mismo ido del finder seleccionado y ademas sus hijos
											idoColumnEditing=gTable.getModel().getRowData().get(rowEditing).getColumnIdo(columnEditing);
											String idColumnParent=gTable.getModel().getRowData().get(row).getColumnIdParent(column);
											idoParent=new IdObjectForm(idColumnParent).getIdo();
										}
										boolean found=true;
										while ( !table.isCellEditable(row, column) || (idoColumnMain!=null && !idoColumnMain.equals(idoColumn) && (!idoColumnMain.equals(idoParent) || Auxiliar.equals(idoColumnEditing,idoColumn)) )){
											action.actionPerformed( e );
											if(!processFocusTable)
												return;
											row = table.getSelectedRow();
											if(row==-1)//Esto puede ocurrir cuando una regla elimina la fila
												return;
											column = table.getSelectedColumn();
											idoColumn=gTable.getModel().getRowData().get(/*rowEditing*/row).getColumnIdo(column);
											//System.err.println("Dentro TabAction: row:"+row+" column:"+column);
											boolean changes=!(rowsChanged[0]==0 && rowsChanged[1]==0);
											boolean sameNumRows=(rowsChanged[0]-rowsChanged[1])==0;
											if (row == originalRow &&  column == originalColumn && (!changes || sameNumRows || !hasNullRow)){
												found=false;
												break;
											}
											
											if(idoColumnMain!=null){//Cuando es null no sirve de nada hacer este calculo ya que en la condicion del bucle no se mira
												String idColumnParent=gTable.getModel().getRowData().get(row).getColumnIdParent(column);
												idoParent=new IdObjectForm(idColumnParent).getIdo();
											}
										}
										boolean changes=!(rowsChanged[0]==0 && rowsChanged[1]==0);
										
										//System.err.println("isEditing:"+isEditing+" row:"+row+" rowEditing:"+rowEditing+" column:"+column+" columnEditing:"+columnEditing);
										if(e.getModifiers()==0 && (!found || (row<rowEditing && !changes) || (row==rowEditing && column<=columnEditing && !changes))){
											removeFocusComponent(table,e.getModifiers()==0);
		
										}else if(e.getModifiers()!=0 && (!found || (row>rowEditing && !changes) || (row==rowEditing && column>=columnEditing && !changes))){
											removeFocusComponent(table,e.getModifiers()==0);
		
										}else{
											if (table.isCellEditable(row, column)){
												//System.err.println("EDITA LA CELDA");
												//final Component editor=table.getEditorComponent();
												//editor.requestFocusInWindow();
												//System.err.println("************************************************TAB2");
												final int rowThis=row;
												final int columnThis=column;
												final Runnable doFinished = new Runnable() {
													public void run() {
														table.editCellAt(rowThis, columnThis);
														JComponent editor=(JComponent)table.getEditorComponent();
														editor.requestFocus();
														editor.requestFocusInWindow();
														
														showToolTipOfCell(gTable, editor);
													}
												};
												SwingUtilities.invokeLater(doFinished);
											}else{
												removeFocusComponent(table,e.getModifiers()==0);
											}
										}
									}else{
										removeFocusComponent(table,e.getModifiers()==0);
									}
			
								}
							}catch(Exception ex){
								ex.printStackTrace();
								GFocusTraversalPolicy.this.server.logError(GFocusTraversalPolicy.this.window,ex,"Error al cambiar de columna en la tabla");
							}finally{
								if(tml!=null)
									table.getModel().removeTableModelListener(tml);
								final Runnable focus = new Runnable() {
									public void run() {
										//System.err.println("Fin processingEnterTabTable");
										processingEnterTabTable=false;
									}
								};
								SwingUtilities.invokeLater(focus);//Lo hacemos en un invoker para dar tiempo a que entre en focusLost o changedValue y se active otra variable de processing
								
								//Si ha habido un error en el ultimo setValueAt de la tabla se devuelve el foco a la columna que edito el usuario para que pueda introducir otro valor
								if(!gTable.getModel().isLastSetValueSuccess()){
									final Runnable focus1 = new Runnable() {
										public void run() {
											//System.err.println("Selection again in Tab");
											table.changeSelection(rowEditing, columnEditing, false, false);
											table.editCellAt(rowEditing, columnEditing);
											table.getEditorComponent().requestFocus();
											table.getEditorComponent().requestFocusInWindow();
										}
									};
									SwingUtilities.invokeLater(focus1);//Lo hacemos en un invoker para dar tiempo a que entre en focusLost o changedValue y se active otra variable de processing
								}
							}
						}
					}
				};
				
				Action enterAction = new AbstractAction(){
					// Modificamos el pulsar enter sobre la fila para que avance a la siguiente columna editable. Si ya estamos en la ultima columna editable
					// habilitamos las key del FocusTraversal para que el FocusTraversalPolicy se ejecuta y cambie de componente ya que se ejecuta en el evento RELEASED
					// y esto se ejecuta antes
					private static final long serialVersionUID = 1L;
					public void actionPerformed(ActionEvent e){
						processFocusTable=true;
						//System.err.println("ENTRA EN ENTER con id:"+e.getID());
						final JTable table = (JTable)e.getSource();
						if(selectionMode || table.getRowCount()==0)//Si estamos en selectionMode lo unico que hacemos es cambiar el foco al siguiente componente
							removeFocusComponent(table,e.getModifiers()==0);
						else{
							//System.err.println("Inicio processingEnterTabTable");
							processingEnterTabTable=true;
							TableModelListener tml=null;
							boolean hasNullRow=gTable.hasNullRow();//Nos sirve para que en los casos de insert/delete por reglas no se quede en bucle infinito al no encontrar un campo donde situarse
							//boolean isEditing=table.isEditing();
							final int rowEditing = table.getSelectedRow();
							final int columnEditing = table.getSelectedColumn();
							try{
								Action action=null;
								if(e.getModifiers()==0)
									action=oldTabAction;
								else action=oldShiftTabAction;
									
								final int[] rowsChanged={0,0};
								tml=getListenerTableChanges(rowsChanged);
								table.getModel().addTableModelListener(tml);
								
								action.actionPerformed(e);
								if(!processFocusTable)
									return;
								
								int row = table.getSelectedRow();
								int column = table.getSelectedColumn();
								
								//Si el action.actionPerformed no ha provocado ningun cambio de fila o columna significa que ha sido evitado por error en la introduccion de datos por lo que no volvemos a intentarlo ya que
								//al volver a intentarlo salta al siguiente campo, y a nosotros nos interesa que se quede en el actual para que el usuario pueda introducir otro valor. Esto es necesario cuando hay un registro
								//de una tabla e intentamos cambiarlo por otro registro que no existe en el finder
								if(table.getColumnCount()>1 && row==rowEditing && column==columnEditing){
									//System.err.println("Sale del enter");
									return;
								}
								
								if(row!=-1){
									boolean isNullRow=rowEditing!=-1?gTable.isNullRow(rowEditing):false;
									boolean changes=!(rowsChanged[0]==0 && rowsChanged[1]==0);
									//System.err.println("row:"+row+" rowEditing:"+rowEditing+" columnEditing:"+columnEditing+" table.getValueAt:"+table.getValueAt(rowEditing, columnEditing)/*+" isEditingNullRow:"+isEditingNullRow+" isRowNullRow"+isRowNullRow*/);
									if(!isNullRow || changes || (gTable.getRowCount()==0 && !gTable.isNullable())/*Si no tiene filas aparte de la nullRow y es obligatoria*/){
										//System.err.println("ENTRAAAAAAA");
										int originalRow = row;
										int originalColumn = column;
										boolean found=true;
										boolean nullRowChecked=false;
										//Nos interesan solo las columnas editables, obligatorias y sin valor
										while (! table.isCellEditable(row, column) || gTable.getModel().isNullable(row, column) || table.getValueAt(row, column)!=null){
											action.actionPerformed( e );
											if(!processFocusTable)
												return;
											row = table.getSelectedRow();
											if(row==-1)//Esto puede ocurrir cuando una regla elimina la fila
												return;
											column = table.getSelectedColumn();
											changes=!(rowsChanged[0]==0 && rowsChanged[1]==0);
											boolean sameNumRows=(rowsChanged[0]-rowsChanged[1])==0;
											if(!nullRowChecked && gTable.isNullRow(row)){//Necesario para cuando la nullRow no tiene ningun campo obligatorio, se quedaba en bucle infinito. Si ya hemos comprobado la nullRow y volvemos al principio es momento de salir
												nullRowChecked=true;
											}
											//System.err.println("Dentro TabAction: row:"+row+" column:"+column+" idoRow:"+idoRow+" gTable.getDataFromIndex(row).getIDO():"+gTable.getDataFromIndex(row).getIDO());
											if (row == originalRow &&  column == originalColumn && (!changes || sameNumRows || !hasNullRow || nullRowChecked)){
												found=false;
												break;
											}
										}
										//System.err.println("found:"+found+" isEditing:"+isEditing+" row:"+row+" rowEditing:"+rowEditing+" column:"+column+" columnEditing:"+columnEditing/*+" idoRowEditing:"+idoRowEditing*/+" idoRow:"+idoRow+" gTable.getDataFromIndex(row).getIDO():"+gTable.getDataFromIndex(row).getIDO());
										changes=!(rowsChanged[0]==0 && rowsChanged[1]==0);
										if(GConfigView.beepOnTables && changes && found){
											Toolkit.getDefaultToolkit().beep();
										}
										if(/*e.getModifiers()==0 && */(!found || ((row<rowEditing && !changes) || (row==rowEditing && column<=columnEditing && !changes)))){
											removeFocusComponent(table,e.getModifiers()==0);
										}else{
											if (table.isCellEditable(row, column)){
											//if (table.editCellAt(row, column)){
												//final Component editor=table.getEditorComponent();
												//editor.requestFocusInWindow();
												//System.err.println("************************************************ENTER");
												final int rowThis=row;
												final int columnThis=column;
												final Runnable doFinished = new Runnable() {
													public void run() {
														table.editCellAt(rowThis, columnThis);
														JComponent editor=(JComponent)table.getEditorComponent();
														editor.requestFocus();
														editor.requestFocusInWindow();
														
														showToolTipOfCell(gTable, editor);
													}
												};
												SwingUtilities.invokeLater(doFinished);
											}
										}
									}else{
										removeFocusComponent(table,e.getModifiers()==0);
									}
								}else{
									removeFocusComponent(table,e.getModifiers()==0);
								}
								
							}catch(Exception ex){
								ex.printStackTrace();
								GFocusTraversalPolicy.this.server.logError(GFocusTraversalPolicy.this.window,ex,"Error al cambiar de columna en la tabla");
							}finally{
								if(tml!=null)
									table.getModel().removeTableModelListener(tml);
								final Runnable focus = new Runnable() {
									public void run() {
										//System.err.println("Fin processingEnterTabTable");
										processingEnterTabTable=false;
									}
								};
								SwingUtilities.invokeLater(focus);//Lo hacemos en un invoker para dar tiempo a que entre en focusLost o changedValue y se active otra variable de processing
								
								//Si ha habido un error en el ultimo setValueAt de la tabla se devuelve el foco a la columna que edito el usuario para que pueda introducir otro valor
								if(!gTable.getModel().isLastSetValueSuccess()){
									final Runnable focus1 = new Runnable() {
										public void run() {
											//System.err.println("Selection again in Enter");
											table.changeSelection(rowEditing, columnEditing, false, false);
											table.editCellAt(rowEditing, columnEditing);
											table.getEditorComponent().requestFocus();
											table.getEditorComponent().requestFocusInWindow();
										}
									};
									SwingUtilities.invokeLater(focus1);//Lo hacemos en un invoker para dar tiempo a que entre en focusLost o changedValue y se active otra variable de processing
								}
							}
						}
					}
				};
				table.getActionMap().put(im.get(tab), tabAction);
				table.getActionMap().put(im.get(shiftTab), tabAction);
				table.getActionMap().put(im.get(enter), enterAction);
				
				//Modificamos las teclas para cambiar de foco de las tablas ya que, por defecto, son distintas que las de los otros componentes
				table.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, setForward);
			    table.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, setBackward);
			    
			    table.setFocusTraversalKeysEnabled(false);
			    
			    table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			    	//Gestiona cuando hay un cambio de fila en la tabla
					public void valueChanged(ListSelectionEvent e) {
						//System.err.println("ListSelectionEvent GTableModel siendo listS.getMinSelectionIndex():"+((ListSelectionModel)e.getSource()).getMinSelectionIndex()+" idoRowEditing"+gTable.getModel().getIdoRowEditing());
						ListSelectionModel listS=(ListSelectionModel)e.getSource();

//						if(!listS.isSelectionEmpty()){
//							//System.err.println("Indice Seleccionado:"+listS.getMinSelectionIndex());
//							if(listS.getMinSelectionIndex()==listS.getMaxSelectionIndex()){//Seleccionada una sola fila
//								System.err.println("- Seleccionada una sola fila");
//								int idoRowToEdit=getDataFromIndex(listS.getMinSelectionIndex()).getIDO();
//								System.err.println("idoRowToEdit:"+idoRowToEdit+" idoRowEditing:"+idoRowEditing);
//								if(idoRowEditing==null || idoRowEditing!=idoRowToEdit){//Fila seleccionada no es la misma que la que ya estamos editando
//									parent.getTable().removeEditor();
//								}
//							}
//						}
						
						
						if(gTable.getModel().getIdoRowEditing()!=null && GFocusTraversalPolicy.this.componentListener!=null){//Estabamos editando una fila
							//System.err.println("Inicio processingFocusTable");
							processingFocusValueChangedTable=true;
							int idoRowActual=gTable.getModel().getIdoRowEditing();
							HashSet<Integer> idosEditingActual=gTable.getModel().getListIdosEditing();
							try{
								
								if(!listS.isSelectionEmpty()){
									//System.err.println("Indice Seleccionado:"+listS.getMinSelectionIndex());
									if(listS.getMinSelectionIndex()==listS.getMaxSelectionIndex()){//Seleccionada una sola fila
										//System.err.println("- Seleccionada una sola fila");
										int idoRowToEdit=gTable.getModel().getDataFromIndex(listS.getMinSelectionIndex()).getIdo();
										//System.err.println("idoRowToEdit:"+idoRowToEdit+" idoRowEditing:"+idoRowEditing);
										if(/*idoRowEditing==null || */(/*idoRowEditing!=null &&*/ gTable.getModel().getIdoRowEditing()!=idoRowToEdit)){//Fila seleccionada no es la misma que la que ya estamos editando
											int rowToEdit=listS.getMinSelectionIndex();
											gTable.getModel().setIdoRowEditing(null);
											//parent.getTable().removeEditor();
											GFocusTraversalPolicy.this.componentListener.stopEditionTable(gTable.getId(),idoRowActual,idosEditingActual,gTable.isProcessingPasteRows());
										}
									}else{//Seleccionadas varias filas
										gTable.getModel().setIdoRowEditing(null);
										//parent.getTable().removeEditor();
										GFocusTraversalPolicy.this.componentListener.stopEditionTable(gTable.getId(),idoRowActual,idosEditingActual,gTable.isProcessingPasteRows());
									}
										
								}else{//Ninguna fila seleccionada
									//System.err.println("- Ninguna fila seleccionada");
		//
//										//Component componentFocus=SwingUtilities.getWindowAncestor(parent).getFocusOwner();
//										//System.err.println("Tiene el focus:"+(componentFocus!=null?componentFocus.getClass():null));
//										//if(componentFocus!=null && !parent.getTable().isAncestorOf(componentFocus)){//Si el foco ya no esta en la tabla
//											System.err.println("El foco ya no esta en la tabla");
//											
//											idoRowEditing=null;
//											//parent.getTable().removeEditor();
//											m_controlListener.stopEditionTable(parent.getId(),idoRowActual);
//											
//											//isNullRowEditing=false;
//										//}else{
//										//	System.err.println("El foco sigue en la tabla");
//										//}
//									//}
								}
							}catch (EditionTableException ex){
								if(ex.isNotify())
									gTable.getServer().logError(GFocusTraversalPolicy.this.window,ex, null);
								try{
									editionError(gTable,ex.getUserMessage(),ex.getIdo(),idoRowActual,idosEditingActual);
								} catch (AssignValueException exe) {
									gTable.getServer().logError(GFocusTraversalPolicy.this.window,exe,"Error al intentar mostrar el error de la fila");
								}
							}finally{
								final Runnable focus = new Runnable() {
									public void run() {
										//System.err.println("Fin processingFocusTable");
										processingFocusValueChangedTable=false;
									}
								};
								SwingUtilities.invokeLater(focus);//Lo hacemos en un invoker para dar tiempo a que entre en focusLost o en gestion de enter-tab y se active otra variable de processing
							}
						}
					}
				});
			    
			    gTable.addFocusListener(new FocusListener(){

					public void focusGained(FocusEvent arg0) {
						if(!processingEnterTabTable && processingKeyEvents){//Solo si hemos llegado con el teclado(processingKeyEvents) y si no estamos procesandolo ya dentro de los eventos enter tab de la tabla(processingEnterTableTable)
							SwingUtilities.invokeLater(new Runnable(){
	
								@Override
								public void run() {
									if(gTable.getTable().isEditing()){
										JComponent editor=(JComponent)gTable.getTable().getEditorComponent();						
										showToolTipOfCell(gTable,editor);
									}
								}
								
							});
						}
					}

					public void focusLost(FocusEvent ev) {
						//System.err.println("FocusLost GTable en GFocusTraversalPolicy component:"+(ev.getComponent()!=null?ev.getComponent().getClass()+" "+ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass()+" "+ev.getOppositeComponent().hashCode():null));
						Component oppositeComponent=ev.getOppositeComponent();
						if(gTable.getModel().getIdoRowEditing()!=null && GFocusTraversalPolicy.this.componentListener!=null){//Estabamos editando una fila
							final int idoRowActual=gTable.getModel().getIdoRowEditing();
							final HashSet<Integer> idosEditingActual=gTable.getModel().getListIdosEditing();
							if(!ev.isTemporary() && ev.getOppositeComponent()!=null && ev.getSource()!=null){
								//System.err.println("Inicio processingFocusTable");
								processingFocusValueChangedTable=true;
								try{
									Component componentFocus=ev.getOppositeComponent();//window.getFocusOwner();
//									System.err.println("Tiene el focus:"+(componentFocus!=null?componentFocus.getClass():null));
//									System.err.println("FocusLost gain "+ev.getOppositeComponent().getClass()+" lost "+ev.getComponent().getClass());
									//System.err.println("Request "+ev.getOppositeComponent().getClass());
//									System.err.println("Id parent:+"+parent.hashCode()+" parent.getTable():"+parent.getTable().hashCode()+"+component"+(ev.getComponent()!=null?ev.getComponent().hashCode():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().hashCode():null));
//									System.err.println("parent.getTable().isAncestorOf(componentFocus):"+parent.getTable().isAncestorOf(ev.getOppositeComponent()));
//									System.err.println("parent.isAncestorOf(componentFocus):"+parent.isAncestorOf(ev.getOppositeComponent()));
//									System.err.println("component.isAncestorOf(componentFocus):"+parent.isAncestorOf(ev.getOppositeComponent()));

									if(/*parent.isAncestorOf(componentFocus)*/gTable.getTable()!=componentFocus && !gTable.getTable().isAncestorOf(componentFocus)/* && parent.isAncestorOf(componentFocus)*/){//Si el foco ya no esta en la tabla pero esta en la botonera
										//System.err.println("El foco ya no esta en la tabla");
										boolean stopEdition=true;
										//Si se trata de la botonera de la tabla permitimos que se pulse el boton de editar
										if(gTable.isTopLabel() && gTable.isAncestorOf(componentFocus) && componentFocus instanceof JButton){
											String command=((JButton)componentFocus).getActionCommand();
											
											IdOperationForm idOperation=new IdOperationForm(command);
											if(idOperation.getButtonType().equals(botoneraAccion.EDITAR)/* || idOperation.getButtonType().equals(botoneraAccion.ELIMINAR)*/){
												stopEdition=false;
											}
										}else if(componentFocus.getParent()==null)//Esto es un caso raro(click en misma columna,distinta fila). Ocurre cuando nos llega el aviso de que un cellEditor que ya ha sido removido de la tabla gana el foco. Posteriormente nos llega el aviso correcto
											stopEdition=false;
										if(stopEdition){
											gTable.getModel().setIdoRowEditing(null);
											GFocusTraversalPolicy.this.componentListener.stopEditionTable(gTable.getId(),idoRowActual,idosEditingActual,gTable.isProcessingPasteRows());
										}
									}/*else{
										System.err.println("El foco no esta en la botonera");
									}*/
									
									if(selectionMode)
										onlyRequired=false;
									else onlyRequired=true;									
								}catch (final EditionTableException ex){
									//managingEditionError=true;
									if(ex.isNotify())
										gTable.getServer().logError(GFocusTraversalPolicy.this.window,ex, null);
									
									//Evitamos que gTable gestione el foco ya que lo vamos a procesar aqui
									gTable.setFocusTraversalManagingError(true);
									
									if(oppositeComponent instanceof AbstractButton && !processingEnterTabTable){
										//Si se trata de un boton(llegado a el con el raton y no con nuestra gestion del foco(processingEnterTabTable) gestionamos editionError directamente ya que si no el boton ejecuta su actionPerformed sin gestionar el error
										try{
											editionError(gTable,ex.getUserMessage(),ex.getIdo(),idoRowActual,idosEditingActual);
										} catch (AssignValueException e) {
											gTable.getServer().logError(GFocusTraversalPolicy.this.window,e,"Error al intentar mostrar el error de la fila");
										}
									}else{
										//Lo hacemos en un invokeLater para que antes se ejecute el focusGained del componente que gano el foco y no nos lo quite de la ventana que mostramos al usuario
										final Runnable doEditionError = new Runnable() {
											public void run() {
												try{
													editionError(gTable,ex.getUserMessage(),ex.getIdo(),idoRowActual,idosEditingActual);
												} catch (AssignValueException e) {
													gTable.getServer().logError(GFocusTraversalPolicy.this.window,e,"Error al intentar mostrar el error de la fila");
												}
											}
										};
										SwingUtilities.invokeLater(doEditionError);
									} 
								}finally{
									processFocusTable=false;
									final Runnable focus = new Runnable() {
										public void run() {
											//System.err.println("Fin processingFocusTable");
											processingFocusValueChangedTable=false;
										}
									};
									SwingUtilities.invokeLater(focus);//Lo hacemos en un invoker para dar tiempo a que entre en focusLost o en gestion de enter-tab y se active otra variable de processing
									
								}
							}
						}
					}
			    	
			    });
	    	}
	    }
	    
	}
	
	/**
	 * Muestra el toolTip de una celda. Es llamado cuando la celda de edición consigue el foco mediante el teclado
	 * @param gTable
	 * @param editor
	 */
	private void showToolTipOfCell(final GTable gTable,	final JComponent editor) {
		if(((GFormTable) gTable.getFormField()).getVisibleRowCount()==1){
				
				final PopupFactory popupFactory = PopupFactory.getSharedInstance();
				final JToolTip toolTip = new JToolTip();
				toolTip.setTipText(editor.getToolTipText());
				
				final Popup tooltipPopup = popupFactory.getPopup(editor, toolTip,
						editor.getLocationOnScreen().x +10,
						editor.getLocationOnScreen().y +25);
				editor.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						tooltipPopup.hide();
						editor.removeFocusListener(this);
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
				tooltipPopup.show();
				
				//SI QUISIERAMOS QUE SE MUESTRE DURANTE UN RATO Y LUEGO SE QUITE
//			    int seconds=3;
//			    Timer t= new Timer(seconds*Constants.TIMEMILLIS, new ActionListener() {
//			      public void actionPerformed(ActionEvent e) {
//			    	  tooltipPopup.hide(); // disposes of the popup.
//			      }
//			    });
//			    t.setRepeats(false);
//			    t.start();
		}
	}
	
	private boolean editionError(GTable gTable,String message,final Integer idoError,final int idoRow,HashSet<Integer> idosEditing) throws AssignValueException{
		processFocusTable=false;//Evitamos que se siga procesando los cambios de foco dentro de la tabla ya que nos interesa que el foco vaya donde digamos en este metodo
		processingEditionError=true;
		//System.err.println("Inicio processingEditionError");
		//TODO Estamos haciendo 3 run con invokeLater debido a que si se ha perdido el foco con la fila de
		//otra tabla, al recuperarlo nos lo vuelve a quitar el cellEditor de esa tabla. Habria que investigar
		//si se podria leer del EventQueue si quedan mas eventos que procesar para saber cuantos invokeLater
		//tendriamos que hacer
//		final Runnable doFinished2 = new Runnable() {
//			public void run() {
//				System.err.println("Reseleccionaaa "+parent.getTable().hashCode());
//				idoRowEditing=idoRow;
//				int ido=findRow(idoRowEditing,false);
//				parent.getTable().removeEditor();//Con esto nos aseguramos que si entro a algun editor de otra fila salga
//				boolean exito=parent.getTable().requestFocusInWindow();
//				System.err.println("************************************************editionError exito:"+exito);
//				parent.getTable().setRowSelectionInterval(ido, ido);
//			}
//		};
//		
//		final Runnable doFinished1 = new Runnable() {
//			public void run() {
//				SwingUtilities.invokeLater(doFinished2);
//			}
//		};
//		
//		final Runnable doFinished = new Runnable() {
//			public void run() {
//				SwingUtilities.invokeLater(doFinished1);
//			}
//		};
		
		Object[] options = {"Editar en tabla", "Editar en formulario", "Cancelar cambios"};
		
		String rdnIdo=null;
		GIdRow idRow=gTable.getDataFromIdo(idoRow,null);
		if(idRow!=null)
			rdnIdo=idRow.getRdn();
		
		int res = messageListener.showOptionMessage(
				message+"\nPara poder continuar debe modificar el registro, ¿qué desea hacer?",
				"REGISTRO ERRÓNEO EN "+gTable.getLabel(),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[0],window);

		if (res == JOptionPane.CANCEL_OPTION){
			try{
				gTable.getTable().removeEditor();
				GFocusTraversalPolicy.this.componentListener.cancelEditionTable(gTable.getId(), idoRow);
				return false;
			} catch (EditionTableException exc) {
				gTable.getServer().logError(window,exc, "Error al intentar cancelar edición de la fila");
			}finally{
				gTable.setFocusTraversalManagingError(false);
				processingFocusValueChangedTable=false;
				processingEditionError=false;
			}
		}else if(res == JOptionPane.NO_OPTION){
			try{
				//parent.getTable().removeEditor();
				reSelectEdition(gTable,idoRow,idosEditing);//Lo pongo antes porque si no le quita el foco a la ventana que se abre editInForm
				GFocusTraversalPolicy.this.componentListener.editInForm(/*gTable.getId()*/idoRow, /*idoRow*/idoError!=null?idoError:idoRow);
			} catch (EditionTableException exc) {
				gTable.getServer().logError(window,exc, "Error al intentar editar fila en formulario");
				return true;
			} finally{
				//gTable.getModel().setIdoRowEditing(idoRow);
				//reSelectEdition(gTable,idoRow,idosEditing);
			}
		}else{
			//gTable.getModel().setIdoRowEditing(idoRow);
			reSelectEdition(gTable,idoRow,idosEditing);
			return true;
		}
		
		return true;
	}
	
	public void reSelectEdition(final GTable gTable,final int idoRow,final HashSet<Integer> idosEditing){
		managingEditionError=true;
		final Runnable reselect = new Runnable() {
			public void run() {
				reSelectEdition(gTable,idoRow,idosEditing);
			}
		};
		// Esperamos a que la ventana este activa ya que si no esta activa al activarse GFocusTraversalPolicy le da el foco
		// al componente que lo tenia antes de desactivarse por lo que no nos serviria de nada pedir aqui el foco.
		// Esto ocurre cada vez que sale un mensajito o un nuevo formulario.
		if(gTable.window.isActive()){
			gTable.getModel().setIdoRowEditing(idoRow);
			gTable.getModel().setListIdosEditing(idosEditing);
			int row=gTable.getModel().findRow(gTable.getModel().getIdoRowEditing(),false,false);
			gTable.getTable().removeEditor();//Con esto nos aseguramos que si entro a algun editor de otra fila salga
			gTable.getTable().requestFocus();
			boolean exito=gTable.getTable().requestFocusInWindow();
			//System.err.println("************************************************editionError exito:"+exito);
			gTable.getTable().setRowSelectionInterval(row, row);
			gTable.editFirstCellEditable(row,0,false,true);
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					//System.err.println("------ Fin procesando editionError");
					managingEditionError=false;
					gTable.setFocusTraversalManagingError(false);
					processingFocusValueChangedTable=false;
					processingEditionError=false;
				}
			});
		}else{
			//Necesario pedir el foco para los formularios incrustados porque no lo recupera bajo algunas circunstancias
			gTable.window.requestFocus();
			gTable.window.requestFocusInWindow();
			SwingUtilities.invokeLater(reselect);
		}
	}
	
	private TableModelListener getListenerTableChanges(final int[] rowsChanged){
		TableModelListener tml=new TableModelListener(){

			public void tableChanged(TableModelEvent e) {
				if(e.getType()==TableModelEvent.INSERT){
					rowsChanged[0]+=(e.getLastRow()-e.getFirstRow()+1);
				}else if(e.getType()==TableModelEvent.DELETE){
					rowsChanged[1]+=(e.getLastRow()-e.getFirstRow()+1);
				}
				//System.err.println("PropertyTable: first:"+e.getFirstRow()+" last:"+e.getLastRow()+" Type:"+e.getType()+" siendo Insert:"+TableModelEvent.INSERT+" y DELETE:"+TableModelEvent.DELETE);
			}
			
		};
		
		return tml;
	}
	
	public void removeFocusComponent(final JComponent component,final boolean forward){
		final Runnable removeFocus = new Runnable() {
			public void run() {
				removeFocusComponent(component,forward);
			}
		};
		// Esperamos a que la ventana este activa ya que si no esta activa al activarse GFocusTraversalPolicy le da el foco
		// al componente que lo tenia antes de desactivarse por lo que no nos serviria de nada pedir aqui el foco.
		// Esto ocurre cada vez que sale un mensajito o un nuevo formulario.
		if(window.isActive()){
//			table.setFocusTraversalKeysEnabled(true);
			//m_objTable.setFocusCycleRoot(false);
			Container            cycleRoot = component.getFocusCycleRootAncestor();
			//FocusTraversalPolicy policy    = table.getFocusTraversalPolicy();
			//if (policy == null && cycleRoot != null)
			//{
			FocusTraversalPolicy policy = cycleRoot.getFocusTraversalPolicy();
			//}
			//System.err.println("TIENE QUE SALIR. Tiene policy:"+policy.getClass()+" Container:"+cycleRoot.getClass());
			Component target;
			if(forward)
				target = policy.getComponentAfter(cycleRoot, component);
			else target = policy.getComponentBefore(cycleRoot, component);
			if (target != null){
				//System.err.println("Se pide el focoooo de "+target);
				target.requestFocus();
				target.requestFocusInWindow();
				//System.err.println("************************************************removeFocusTable");
			}
		}else{
			SwingUtilities.invokeLater(removeFocus);
		}
	}

	@Override
	protected boolean accept(Component c) {
		//System.err.println("ENTRA EN PROVEEDOR con "+c.getClass()+" code:"+c.hashCode());
		//if(c instanceof GTable){
		if(onlyRequired){
			if(c.getBackground().equals(GConfigView.colorBackgroundRequired)){
				Component cParent=c;
				do{
					cParent=cParent.getParent();
				}while(cParent!=null && !(cParent instanceof GComponent));
				
				if(cParent!=null){
					if(((GComponent)cParent).newValueAllowed()){
						boolean accept=super.accept(c);
						if(accept){
							/*Point p=new Point(0,(int)(c.getLocationOnScreen().getY()-GFocusTraversalPolicy.this.viewPort.getLocationOnScreen().getY()));
							if(!GFocusTraversalPolicy.this.viewPort.getViewRect().contains(p)){
								System.err.println("Entra en viewport para c1:"+c.getClass());
								Exception ex=new Exception();
								ex.printStackTrace();
								GFocusTraversalPolicy.this.viewPort.setViewPosition(p);
							}*/
						}
						return accept;
					}else{
						return false;
					}
				}
			}else if(c instanceof JButton && (GConfigView.enterFocusOnAcceptButton || !popup/*Siempre vamos al boton aceptar en formularios incrustados*/)){
				String command=((JButton)c).getActionCommand();
				if(IdOperationForm.matchFormat(command)){
					IdOperationForm idOperation=new IdOperationForm(command);
					if(idOperation.getButtonType().equals(botoneraAccion.EJECUTAR)){
						return true;
					}else return false;
				}else return false;
			}else return false;
		}else{
			if(c instanceof AbstractButton){
				Component cParent=c;
				do{
					cParent=cParent.getParent();
				}while(cParent!=null && !(cParent instanceof GComponent));
				
				if(cParent!=null && cParent instanceof GTable){
					return false;
				}
			}
		}
		boolean accept=super.accept(c);
		if(accept){
			/*Point p=new Point(0,(int)(c.getLocationOnScreen().getY()-GFocusTraversalPolicy.this.viewPort.getLocationOnScreen().getY()));
			if(!GFocusTraversalPolicy.this.viewPort.getViewRect().contains(p)){
				System.err.println("Entra en viewport para c2:"+c.getClass());
				GFocusTraversalPolicy.this.viewPort.setViewPosition(p);
			}*/
		}
		return accept;
	}
	
	public void setEnabled(boolean enabled){
		if(enabled){
			if(!this.enabled){
				container.setFocusTraversalPolicy(this);
				container.setFocusTraversalPolicyProvider(true);
				
				KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
				KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner",propertyChangeListener); 
				//KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener(vetoableChangeListener); 
			}
		}else{
			if(this.enabled){
				container.setFocusTraversalPolicy(null);
				container.setFocusTraversalPolicyProvider(false);
				
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher);
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("permanentFocusOwner",propertyChangeListener);
				//KeyboardFocusManager.getCurrentKeyboardFocusManager().removeVetoableChangeListener(vetoableChangeListener);
			}
		}
		this.enabled=enabled;
	}

	public boolean isOnlyRequired() {
		return onlyRequired;
	}

	public boolean isProcessingFocusTable() {
		//System.err.println("processingFocusTable "+processingFocusTable+" processingEnterTabTable "+processingEnterTabTable+" processingEditionError "+processingEditionError);
		return processingFocusValueChangedTable || processingEnterTabTable || processingEditionError;
	}
	

}
