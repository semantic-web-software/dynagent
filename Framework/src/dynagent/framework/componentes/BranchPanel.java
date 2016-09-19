package dynagent.framework.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;

import dynagent.framework.ConstantesGraficas;
import dynagent.framework.utilidades.CreadorIconos;

class HighlightedDaemon extends Thread {
	int period = 0;
	BranchPanel branch;
	Color color;

	public HighlightedDaemon(int period,BranchPanel branch,Color color) {
		this.period = period;
		this.branch = branch;
		this.color = color;
		start();
	}
	
	public void run() {
		try{
			int time=0;
			Color background=branch.getBackground();
			while(time<15 && !branch.isSelected()){
				branch.setBackground(background);
				sleep(period);
				branch.setBackground(color);
				sleep(period);
				time++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public class BranchPanel extends JButton{

	private static final long serialVersionUID = 1L;
	public String text;
	private ImageIcon icon;
	private boolean selected;
	private ImageIcon icono;
	//private ImageIcon icono;
	private final int IconGap = 5;
	private final int SEPARATE = IconGap+3;
	private JPanel panel;
	private final int imageSize = 24;
	private ActionListener listener;
	private LinkedHashMap<String,JButton> mapIdButtons;
	private Color backGroundColor;
	private HashMap<String,Color> highlightedColorButtons;
	private Color highlightedColor;
	
	private ArrayList<KeyListener> keyListeners;
	
	public BranchPanel(String text, ImageIcon icon, ActionListener listener){
		this.text = text;
		this.icon=icon;
		this.listener=listener;
		mapIdButtons = new LinkedHashMap<String, JButton>();
		keyListeners = new ArrayList<KeyListener>();
		createImages();
		setBorder(BorderFactory.createEmptyBorder());
		backGroundColor=UIManager.getColor("ToolBar.background");
		setBackground(UIManager.getColor("ScrollBar.background"));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createMatteBorder(0, 2, 2, 2, backGroundColor));
		panel.setBackground(backGroundColor);
		selected = false;
		setBorder(BorderFactory.createLineBorder(UIManager.getColor("ScrollBar.background"), 1));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//setFocusPainted(false);
		setName(text);
		//setHorizontalAlignment(JButton.LEFT);
		setPreferredSize(new Dimension(ConstantesGraficas.intMenuX, ConstantesGraficas.intItemAreaY));
		highlightedColorButtons=new HashMap<String, Color>();
	}
	
	public void addBranchItem(String identificador,JButton b){
		if(panel.getComponentCount()!=0)
			panel.add(Box.createRigidArea(new Dimension(0, ConstantesGraficas.intItemMenuY)));
		if(listener!=null)
			b.addActionListener(listener);
		Iterator<KeyListener> itr=keyListeners.iterator();
		while(itr.hasNext()){
			b.addKeyListener(itr.next());
		}
		mapIdButtons.put(identificador,b);
		panel.add(b);		
	}

	@Override
	public synchronized void addKeyListener(KeyListener listener) {
		super.addKeyListener(listener);
		
		//Hacemos que tambien se añada el listener a los botones que contiene
		Iterator<JButton> itr=mapIdButtons.values().iterator();
		while(itr.hasNext()){
			itr.next().addKeyListener(listener);
		}
		
		keyListeners.add(listener);//Lo guardamos para añadir el listener cada vez que se añada un nuevo boton
	}

	public void toggleSelection(){
		selected = !selected;
		repaint();
	}

	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int h = getHeight();		
		float x = SEPARATE;		
		if(icon!=null){
			x += imageSize;
			g2.drawImage(icono.getImage(), IconGap, IconGap, this);
		}
		g2.setFont(getFont());
		FontRenderContext frc = g2.getFontRenderContext();
		LineMetrics lm = getFont().getLineMetrics(text, frc);
		float height = lm.getAscent() + lm.getDescent();		
		float y = (h + height)/2 - lm.getDescent();
		g2.drawString(text, x, y);
	}

	private void createImages(){
		if(icon!=null)
			icono = CreadorIconos.redimensionarIcono(icon, new Dimension(imageSize, imageSize));
	}

	public JPanel getPanel() {
		return panel;
	}

	public boolean contains(JButton button) {
		return mapIdButtons.values().contains(button);
	}

	public boolean isSelected() {
		return selected;
	}
	
	public void setHighlighted(boolean highlighted){
		if(highlighted){
			if(selected)
				setBackground(highlightedColor);
			else if(!getBackground().equals(highlightedColor))
				new HighlightedDaemon(500,this,highlightedColor);
		}else setBackground(backGroundColor);
	}
	
	public boolean isHighlighted(Color color){
		return getBackground().equals(color);
	}
	
	public void setHighlightedItem(String identificador,boolean highlighted){
		JButton b=mapIdButtons.get(identificador);
		Color color=getHighlightedItemColor(identificador);
		if(highlighted)
			b.setForeground(color);
		else b.setForeground(UIManager.getColor("Button.foreground"));
	}
	
	public boolean isHighlightedItem(String identificador){
		JButton b=mapIdButtons.get(identificador);
		return b.getForeground().equals(getHighlightedItemColor(identificador));
	}
	
	public String getLabelItem(JButton button){
		Iterator<String> it = mapIdButtons.keySet().iterator();
		while(it.hasNext()){
			String label = it.next();
			if(mapIdButtons.get(label)==button)
				return label;				
		}
		return null;
	}

	public HashMap<String, JButton> getButtons() {
		return mapIdButtons;
	}

	//Obtiene el texto que se muestra en el boton. El metodo getText no devuelve el correcto porque es gestionado directamente en el paint
	//No es posible sobreescribir getText porque si no muestra dos veces el texto sobre el boton
	public String getTextButton() {
		return text;
	}

	public Color getHighlightedItemColor(String identificador) {
		return highlightedColorButtons.get(identificador)!=null?highlightedColorButtons.get(identificador):ConstantesGraficas.highlightedColor;
	}

	public void setHighlightedItemColor(String identificador,Color highlightedColor) {
		this.highlightedColorButtons.put(identificador, highlightedColor);
	}
	
	public Color getHighlightedColor() {
		return highlightedColor;
	}

	public void setHighlightedColor(Color highlightedColor) {
		this.highlightedColor=highlightedColor;
	}

}