package gdev.gawt.utils;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

public class ButtonPopup extends JMenuItem {

	/**
	 * These colors are required in order to simulate the JMenuItem's L&F
	 */
	public static final Color	MENU_HIGHLIGHT_BG_COLOR	= UIManager.getColor("List.selectionBackground");
	public static final Color	MENU_HIGHLIGHT_FG_COLOR	= UIManager.getColor("List.selectionForeground");
	public static final Color	MENUITEM_BG_COLOR		= UIManager.getColor("List.background");
	public static final Color	MENUITEM_FG_COLOR		= UIManager.getColor("List.foreground");
	
	public ButtonPopup() {
		super();
		init();

	}
	
	public ButtonPopup(String text) {
		super(text);
		init();
	}

	/**
	 * Initialize component LAF and add Listeners
	 */
	private void init() {
		//MouseAdapter mouseAdapter = getMouseAdapter();

		//	Basically JGoodies LAF UI for JButton does not allow Background color to be set.
		// So we need to set the default UI,        
		ComponentUI ui = BasicButtonUI.createUI(this);
		this.setUI(ui);
		setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 2));
		setMenuItemDefaultColors();
		//        setContentAreaFilled(false);
		setHorizontalTextPosition(SwingConstants.RIGHT);
		setHorizontalAlignment(SwingConstants.LEFT);
		//        setModel(new JToggleButton.ToggleButtonModel());
		//setModel(new XCheckedButtonModel());
		setSelected(false);
		setFocusable(false);
		//this.addMouseListener(mouseAdapter);

	}

	/*@Override
	Intento de evitar que el finder si tiene muchos registros se ralentice
	por supuestamente intentos de cambio de foco. Sin embargo con esta opcion no
	estamos seguros si fastidiariamos algo como por ejemplo la accion del recolector de basura
	public void removeNotify() {
		// TODO Auto-generated method stub
		//super.removeNotify();
	}*/

	private void setMenuItemDefaultColors() {
		this.setBackground(MENUITEM_BG_COLOR);
		this.setForeground(MENUITEM_FG_COLOR);
	}
	
	private void setMenuItemSelectedColors() {
		this.setBackground(MENU_HIGHLIGHT_BG_COLOR);
		this.setForeground(MENU_HIGHLIGHT_FG_COLOR);
	}
	
	/**
	 * @return
	 */
//	private MouseAdapter getMouseAdapter() {
//		return new MouseAdapter() {
//			/*
//			 * For static menuitems, the background color remains the highlighted color, if this is not overridden
//			 */
//			public void mousePressed(MouseEvent e) {
//				setMenuItemDefaultColors();
//			}
//
//			public void mouseEntered(MouseEvent e) {
//				setMenuItemSelectedColors();
//			}
//
//			public void mouseExited(MouseEvent e) {
//				setMenuItemDefaultColors();
//			}
//
//		};
//	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if(selected){
			setMenuItemSelectedColors();
		}else{
			setMenuItemDefaultColors();
		}
	}
	
}
