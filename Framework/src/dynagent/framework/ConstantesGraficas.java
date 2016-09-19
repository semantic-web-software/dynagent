package dynagent.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dynagent.common.utils.Utils;

public class ConstantesGraficas {
    
	private static Toolkit tk = Toolkit.getDefaultToolkit();
    public static final int intScreenX = tk.getScreenSize().width;
	public static final int intScreenY =  tk.getScreenSize().height; 
	public static final Dimension dimInit = new Dimension(intScreenX, intScreenY);
	public static final int intItemAreaY = 35;
	public static final int intItemMenuY = 6;
    public static final int type_config=1;
    public static final int type_operation=2;
    
    
	
    public static int 		intScreenUtilX;
	public static int 		intScreenUtilY;
	public static int 		intMenuX;
    public static /*final*/int 		intStatusBar;// 			= 18;    
    public static final int intToolY 				= 40;
    public static final int sizeDivisorSplitPane	= 8;
	public static final int IncrementScrollVertical = 10;
	public static int getSizeTabTabbedPane(){
		JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//panel.setPreferredSize(new Dimension(10,10));
		JTabbedPane tabbedPane=new JTabbedPane();
		tabbedPane.addTab("ABCDE",panel);
		tabbedPane.setBorder(BorderFactory.createEmptyBorder());
		JPanel panel1=new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel1.add(tabbedPane);
		
		return (int)tabbedPane.getPreferredSize().getHeight();
	}
    
    public static Dimension dimScreenJDialog;
    public static Dimension dimZonaTrabajo;
    public static Dimension dimMenu;
    public static Dimension dimTool;
    public static Dimension dimStatus;
	public static Dimension dimZonaTrabajoExtended;//Zona de trabajo ocupando tambien la zona del menu
	public static Dimension dimFormManager;
	
	public static Color highlightedColor=Color.RED;
    
 }
