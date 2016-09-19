package gdev.gen;

import gdev.gawt.GTable;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


/**
 * Esta clase contiene los códigos que se necesitan en la aplicación, 
 * como los códigos de los tipos de cada campo, los códigos de error 
 * y los códigos de las alineaciones.
 * @author Dynagent
 *
 */
public class GConst
{
    /*public static final int TM_TABLE = 0;
    public static final int TM_ENUMERATED = 1;
    public static final int TM_INTEGER = 2;
    public static final int TM_REAL = 4;
    public static final int TM_TEXT = 8;
    public static final int TM_DATE = 16;
    public static final int TM_MEMO = 32;
    public static final int TM_BOOLEAN = 64;
//    public static final int TM_BOOLEAN_COMMENTED = 70;
    public static final int TM_IMAGE = 128;*/
	

	/*Lo cambio a los mismos valores de helperConstant para no tener problemas, pero hay que decidir si utilizar estas
	 o las de helperConstant*/
	/** Código  para el tipo Tabla*/
	public static final int TM_TABLE = 0;
	/** Código para el tipo Enumerado*/
	public static final int TM_ENUMERATED = 1;
	/** Código para el tipo Entero*/
	public static final int TM_INTEGER = 2;
	/** Código para el tipo Real*/
	public static final int TM_REAL = 3;
	/** Código para el tipo Texto*/
	public static final int TM_TEXT = 4;
	/** Código para el tipo Fecha*/
	public static final int TM_DATE = 5;
	/** Código para el tipo Memo*/
	public static final int TM_MEMO = 6;
	/** Código para el tipo CheckBox*/
	public static final int TM_BOOLEAN = 7;
	/** Código para el tipo CheckBoxComentado*/
	public static final int TM_BOOLEAN_COMMENTED = 8;
	/** Código para el tipo Imagen*/
	public static final int TM_IMAGE = 9;
	/** Código para el tipo Fecha y Hora*/
	public static final int TM_DATE_HOUR = 10;
	/** Código para el tipo Botón*/
	public static final int TM_BUTTON = 11;
	/** Código para el tipo Hora*/
	public static final int TM_HOUR = 12;
	/** Código para el tipo Fichero*/
	public static final int TM_FILE = 13;


	/** Código general para una realización exitosa*/
    public static final int GRES_S_OK = 0x0000000000000000;
    /** Código general para un error*/
    public static final int GRES_E_ERR = 0x0000000000000001;
    
    //different kinds of alignments
    /** Código para el alineamiento a la izquierda*/
    public static final int ALIGN_LEFT = 1;
    /** Código para la alineación centrada*/
    public static final int ALIGN_CENTER = 2;
    /** Código para el alineamiento a la derecha*/
    public static final int ALIGN_RIGHT = 3;
    /** Código para la alineación justificada*/
    public static final int ALIGN_JUSTIFY = 4;	
    
    
    public static final int MAX_FORMS_POSSIBLE_COMBINATION=1024;
    
    public static final int MENU_SHORTCUT_KEY=KeyEvent.VK_M;
    public static final int MENU_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int SEARCH_SHORTCUT_KEY=KeyEvent.VK_F;
    public static final int SEARCH_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int SEARCH_SWITCH_SHORTCUT_KEY=KeyEvent.VK_U;
    public static final int SEARCH_SWITCH_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int RESULT_SHORTCUT_KEY=KeyEvent.VK_K;
    public static final int RESULT_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int ACTIONS_SHORTCUT_KEY=KeyEvent.VK_O;
    public static final int ACTIONS_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int REPORTS_SHORTCUT_KEY=KeyEvent.VK_I;
    public static final int REPORTS_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int VIEW_SHORTCUT_KEY=KeyEvent.VK_J;
    public static final int VIEW_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int SET_SHORTCUT_KEY=KeyEvent.VK_E;
    public static final int SET_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int NEW_SHORTCUT_KEY=KeyEvent.VK_N;
    public static final int NEW_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int PRINT_SHORTCUT_KEY=KeyEvent.VK_P;
    public static final int PRINT_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int CANCEL_SHORTCUT_KEY=KeyEvent.VK_ESCAPE;
    public static final int CANCEL_SHORTCUT_MODIFIERS=0;
    
    public static final int QUERY_SHORTCUT_KEY=KeyEvent.VK_B;
    public static final int QUERY_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int RESETALL_SHORTCUT_KEY=KeyEvent.VK_D;
    public static final int RESETALL_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int ASSIGN_SHORTCUT_KEY=KeyEvent.VK_G;
    public static final int ASSIGN_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
     
    public static final int INFO_SHORTCUT_KEY=KeyEvent.VK_F3;
    public static final int INFO_SHORTCUT_MODIFIERS=0;
    
    public static final int RESULT_SWITCH_SHORTCUT_KEY=KeyEvent.VK_L;
    public static final int RESULT_SWITCH_SHORTCUT_MODIFIERS=InputEvent.CTRL_DOWN_MASK;
    
    public static final int DETAIL_NEXT_KEY=KeyEvent.VK_DOWN;
    public static final int DETAIL_NEXT_SHORTCUT_MODIFIERS=0;
    
    public static final int DETAIL_PREV_KEY=KeyEvent.VK_UP;
    public static final int DETAIL_PREV_SHORTCUT_MODIFIERS=0;
    
    public static final int RULES_DEBUG=KeyEvent.VK_F6;
    public static final int RULES_DEBUG_MODIFIERS=0;
    
    public static final int PASTE_TABLE_ROWS=KeyEvent.VK_F7;
    public static final int PASTE_TABLE_ROWS_MODIFIERS=0;
    
////	Se encarga de ejecutar action si se pulsa la key indicada estando el foco en cualquier posicion de la ventana
//	static public void addShortCut(final JComponent component,int key,int modifiers,String nameShortCut,int when,Action action){
//		InputMap im = component.getInputMap(when);
//		KeyStroke f1 = KeyStroke.getKeyStroke(key, modifiers);
//		im.put(f1, nameShortCut);
//		component.getActionMap().put(im.get(f1), action);
//	}
//	
////	Se encarga de ejecutar action si se pulsa la key indicada estando el foco en cualquier posicion de la ventana
//	static public void addShortCut(final JComponent component,int key,int modifiers,String nameShortCut,Action action){
//		addShortCut(component, key, modifiers, nameShortCut, JComponent.WHEN_IN_FOCUSED_WINDOW, action);
//	}
//	
////	Se encarga de ejecutar action si se pulsa la key indicada estando el foco en cualquier posicion de la ventana
//	static public void addShortCut(final JComponent component,int key,int modifiers,String nameShortCut,int when,Action action){
//		addShortCut(component, key, modifiers, nameShortCut, when, action);
//	}
//	
////	Se encarga de hacer que el componente obtenga el foco y se haga click, si es un boton, al pulsar la key indicada estando el foco en cualquier posicion de la ventana
//	static public void addShortCut(final JComponent componentParent,final JComponent component,int key,int modifiers,String nameShortCut,int when){
//		Action action = new AbstractAction(){
//			// Creamos la accion que se encarga de hacer click en el boton al pulsar la key
//			private static final long serialVersionUID = 1L;
//			
//			private Runnable runnable=new Runnable(){
//
//				public void run() {
//					((AbstractButton)component).doClick();
//				}
//				
//			};
//			
//			public void actionPerformed(ActionEvent e){
//				System.err.println("ENTRA a shortcut");
//				component.requestFocusInWindow();
//				if(component instanceof AbstractButton){
//					//Lo ejecutamos en un invokeLater porque hasta que realmente no tenga el foco el boton, no se ejecuta el click
//					//ya que para los botones de botoneraAccion hemos reescrito el metodo fireActionPerformed para que no funcione si no tiene el foco
//					SwingUtilities.invokeLater(runnable);
//				}
//					
//			}
//
//		};
//		
//		addShortCut(component, key, modifiers, nameShortCut, when, action);
//	}
//	
////	Se encarga de hacer que el componente obtenga el foco y se haga click, si es un boton, al pulsar la key indicada estando el foco en cualquier posicion de la ventana
//	static public void addShortCut(final JComponent component,int key,int modifiers,String nameShortCut){
//
//		addShortCut(component, key, modifiers, nameShortCut, JComponent.WHEN_IN_FOCUSED_WINDOW);
//	}
	
    //Se encarga de ejecutar action si se pulsa la key indicada estando el foco en cualquier posicion de la ventana.
    //Si action es null crea una accion que le da el foco a component, y en el caso de ser un boton, tambien lo pulsa
	static public void addShortCut(JComponent componentParent,final JComponent component,int key,int modifiers,String nameShortCut,int when,Action action){
		if(componentParent==null)
			componentParent=component;
		if(action==null){
			final JComponent componentParentThis=componentParent;
			action = new AbstractAction(){
				// Creamos la accion que se encarga de hacer click en el boton al pulsar la key
				private static final long serialVersionUID = 1L;
				
				//private Component lastComponent;
				private Runnable runnable=new Runnable(){
	
					public void run() {
						//Lo hacemos en otro invokeLater para dar tiempo a que GTable pueda estar gestionando un error de datos
						SwingUtilities.invokeLater(new Runnable(){

							public void run() {
								//Si es GTable no nos interesa hacer click si este esta gestionandolo por un error de datos de la tabla
								if(!(componentParentThis instanceof GTable) || !((GTable)componentParentThis).isFocusTraversalManagingError())
									((AbstractButton)component).doClick();
							}
							
						});
						/*Intento de que el foco no se quede en el boton si no en el que tenia el foco anteriormente, pero si el boton abre un popup es un problema
						 * if(component!=lastComponent){
							SwingUtilities.invokeLater(new Runnable(){

								public void run() {
									lastComponent.requestFocusInWindow();
								}
								
							});
						}*/
					}
					
				};
				
				public void actionPerformed(ActionEvent e){
					//System.err.println("ENTRA a shortcut");
					//lastComponent=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
					component.requestFocusInWindow();
					if(component instanceof AbstractButton){
						//Lo ejecutamos en un invokeLater porque hasta que realmente no tenga el foco el boton, no se ejecuta el click
						//ya que para los botones de botoneraAccion hemos reescrito el metodo fireActionPerformed para que no funcione si no tiene el foco
						SwingUtilities.invokeLater(runnable);
					}
						
				}
	
			};
		}
		InputMap im = componentParent.getInputMap(when);
		KeyStroke f1 = KeyStroke.getKeyStroke(key, modifiers);
		im.put(f1, nameShortCut);
		componentParent.getActionMap().put(im.get(f1), action);
	}
	
	public static String LAST_FILE_PATH; //Almacena ultima ruta en la que hemos seleccionado algun archivo
	
	public static String ID_COLUMN_TABLE_SELECTION="SELECTION";
	
	public static final Integer MAX_CHARACTERS_TEXT=100;
}
