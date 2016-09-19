package gdev.gawt;

import gdev.gawt.utils.ItemList;
import gdev.gen.GConfigView;
import gdev.gfld.GFormEnumerated;
import gdev.gfld.GFormField;
import gdev.gfld.GValue;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Esta clase extiende a GComponent y creará un ComboBox.
 * Una vez creado se podrá representar en la interfaz gráfica.
 * @author Francisco
 */
public class GComboBox extends GComponent implements FocusListener
{
	private static final long serialVersionUID = 1L;
	//private Font m_font;

	private Vector m_lista;
	String m_defaultVal;
	boolean m_enable;
	boolean m_modoConsulta;
	boolean m_modoFilter;
	ItemList m_initialValue;
	GListBox m_claseManejadora;

	/**
	 * Constructor de la clase
	 * @param ff
	 * @param fuente
	 * @param modoConsulta
	 * @param claseManejadora
	 */
	public GComboBox(GFormField ff,Font fuente,boolean modoConsulta,boolean modoFilter,GListBox claseManejadora)
	{
		super(ff,claseManejadora.getComponentListener());
	
		//m_font=fuente;
		m_defaultVal=(String)ff.getDefaultVal();
		m_enable=ff.isEnabled();
		m_modoConsulta=modoConsulta;
		m_claseManejadora=claseManejadora;
		m_modoFilter=modoFilter;
		/*un campo que no admite nulos antes de ser editado aparecerá con el nulo seleccionado

         Antes del submit habrá que comprobar que si hay un nulo donde no se admiten. En esta
         versión no se permite que el editor del listbox no muestre nulos en la lista desplegable
         cuando el en valor visalizado al inicio es nulo (en las tablas si se permite). Por tanto de
         momento a todas las listas añado el elemento nulo.
        Ver comentarios en DOC de DIseño de como se gestionan los cambios de valor en los formularios
		 */

		Vector<ItemList> vItems = new Vector<ItemList>();

		boolean nullInitialSelection=false;
		if(claseManejadora.isNullable() || m_modoFilter || (m_defaultVal==null || m_defaultVal.length()==0)){
			if(m_defaultVal!=null && m_defaultVal.length()>0)
				nullInitialSelection= m_defaultVal.equals("0");
			else
				nullInitialSelection= true;
		}
		vItems.add(new ItemList("0", null, "", nullInitialSelection));
		
		ArrayList<Integer> defaultValues=new ArrayList<Integer>();
		if(m_defaultVal!=null && m_defaultVal.length()>0 ){
			String[] vls = m_defaultVal.split(";");
			for (int v = 0; v < vls.length; v++)
				defaultValues.add(new Integer(vls[v]));
		}

		GFormEnumerated enumFields = (GFormEnumerated)m_objFormField;
		Vector vValues = enumFields.getValues();
		Enumeration en = vValues.elements();
		/*Vector vItems = new Vector();*/
		while(en.hasMoreElements())
		{
			GValue val = (GValue)en.nextElement();
			ItemList itl= new ItemList(String.valueOf(val.getId()),
					null,
					val.getLabel(),
					defaultValues.indexOf(val.getId())>=0
					/*true*/);
			vItems.addElement(itl);
			/*vItems.addElement(val.getLabel());*/
		}
		Collections.sort(vItems);
		m_lista=vItems;
		calcularValorInicial();
		m_claseManejadora.setComponent(this);
	}
	protected void createComponent()
	{
		m_objComponent = new JComboBox(m_lista){

			@Override
			public void setBorder(Border b) {
				//setBorder(border/*javax.swing.BorderFactory.createLineBorder(new java.awt.Color(49,106,197))*/);
				//setEditable(true);
				//setBackground(new java.awt.Color(255, 255, 255));
				for (int i=0; i<getComponentCount(); i++) {
					if (getComponent(i) instanceof AbstractButton) {
						((AbstractButton)getComponent(i)).setBorder(b/*javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(224, 223, 227)))*/);
						//((AbstractButton)getComponent(i)).setBackground(new java.awt.Color(224, 223, 227));
					} else {
						if (getComponent(i) instanceof JTextField){
							//((JTextField)getComponent(i)).setSelectionColor(new java.awt.Color(163,184,203));
							//((JTextField)getComponent(i)).setBorder(border/*javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255))*/);
							//((JTextField)getComponent(i)).setBackground(new java.awt.Color(255, 255, 255));
							//((JTextField)getComponent(i)).setDisabledTextColor(new java.awt.Color(0, 0, 255));
						}
					}
				}
			}
			
		};
		
		m_objComponent.setName(getFormField().getName());
		
		m_objComponent.addKeyListener(m_claseManejadora.m_keyListener);
		
//		m_objComponent = new JComboBox(m_lista){
//			public void paint(Graphics g) {
//				super.paint(g);
//				System.err.println("Entra");
//				if(hasFocus()){
//					DrawRoutines.drawRoundedBorder(g, Color.RED, 0/*m_objComponent.getX()*/,0/*m_objComponent.getY()*/, (int)m_objComponent.getWidth(), (int)m_objComponent.getHeight());
//					DrawRoutines.drawRoundedBorder(g, Color.RED, 1/*m_objComponent.getX()*/,1/*m_objComponent.getY()*/, (int)m_objComponent.getWidth()-2, (int)m_objComponent.getHeight()-2);
//					System.err.println("Crea border");
//				}else{
//					System.err.println("----No tiene focus");
//				}
//			}
//
//			@Override
//			public void updateUI() {
//				// TODO Auto-generated method stub
//				super.updateUI();
//				System.err.println("setPopupVisible");
//			}
//
//			@Override
//			public void setPopupVisible(boolean arg0) {
//				// TODO Auto-generated method stub
//				super.setPopupVisible(arg0);
//				System.err.println("setPopupVisible");
//				repaint();
//			}
//
//			@Override
//			public void firePopupMenuCanceled() {
//				// TODO Auto-generated method stub
//				super.firePopupMenuCanceled();
//				System.err.println("firePopupMenuCanceled");
//				repaint();
//			}
//
//			@Override
//			public void firePopupMenuWillBecomeInvisible() {
//				// TODO Auto-generated method stub
//				super.firePopupMenuWillBecomeInvisible();
//				System.err.println("firePopupMenuWillBecomeInvisible");
//				repaint();
//			}
//
//			@Override
//			public void firePopupMenuWillBecomeVisible() {
//				// TODO Auto-generated method stub
//				super.firePopupMenuWillBecomeVisible();
//				System.err.println("firePopupMenuWillBecomeVisible");
//				repaint();
//			}
//			
//		};
//		
//		m_objComponent.addMouseListener(new MouseListener(){
//
//			public void mouseClicked(MouseEvent arg0) {
//				m_objComponent.repaint();
//			}
//
//			public void mouseEntered(MouseEvent arg0) {
//				m_objComponent.repaint();
//			}
//
//			public void mouseExited(MouseEvent arg0) {
//				m_objComponent.repaint();
//			}
//
//			public void mousePressed(MouseEvent arg0) {
//				m_objComponent.repaint();
//			}
//
//			public void mouseReleased(MouseEvent arg0) {
//				m_objComponent.repaint();
//			}
//			
//		});
//
//		((JComboBox)m_objComponent).setEditor(new TinyComboBoxEditor(){
//
//			@Override
//			public void focusGained(FocusEvent arg0) {
//				// TODO Auto-generated method stub
//				super.focusGained(arg0);
//				System.err.println("focusGained");
//			}
//
//			@Override
//			public void focusLost(FocusEvent arg0) {
//				// TODO Auto-generated method stub
//				super.focusLost(arg0);
//				System.err.println("focusLost");
//			}
//			
//		});
//		
//		((JTextField)((JComboBox)m_objComponent).getEditor().getEditorComponent()).setBorder(new AbstractBorder(){
//
//			@Override
//			public Insets getBorderInsets(Component arg0, Insets arg1) {
//				// TODO Auto-generated method stub
//				System.err.println("getBorderInsets 1");
//				return super.getBorderInsets(arg0, arg1);
//			}
//
//			@Override
//			public Insets getBorderInsets(Component arg0) {
//				// TODO Auto-generated method stub
//				System.err.println("getBorderInsets 2");
//				return super.getBorderInsets(arg0);
//			}
//
//			@Override
//			public Rectangle getInteriorRectangle(Component arg0, int arg1, int arg2, int arg3, int arg4) {
//				// TODO Auto-generated method stub
//				System.err.println("getInteriorRectangle");
//				return super.getInteriorRectangle(arg0, arg1, arg2, arg3, arg4);
//			}
//
//			@Override
//			public boolean isBorderOpaque() {
//				// TODO Auto-generated method stub
//				System.err.println("isBorderOpaque");
//				return super.isBorderOpaque();
//			}
//
//			@Override
//			public void paintBorder(Component arg0, Graphics arg1, int arg2, int arg3, int arg4, int arg5) {
//				// TODO Auto-generated method stub
//				super.paintBorder(arg0, arg1, arg2, arg3, arg4, arg5);
//				System.err.println("paintBorder");
//			}
//			
//		});
//		final ListCellRenderer oldRender=((JComboBox)m_objComponent).getRenderer();
//		
//		((JComboBox)m_objComponent).setRenderer(new ListCellRenderer(){
//
//			public Component getListCellRendererComponent(JList list,
//		            Object value,
//		            int index,
//		            boolean isSelected,
//		            boolean cellHasFocus) {
//		        // Ask the standard renderer for what it thinks is right
//		        Component c = oldRender.getListCellRendererComponent(list,
//		                        value,
//		                        index,
//		                        isSelected,
//		                        cellHasFocus);
//		        //if (!isSelected) {
//		                // Set the background of the returned component to Aqua
//		                // striped background, but only for unselected cells;
//		                // The standard renderer functions as desired for 
//		                // highlighted cells.
//		            //c.setBackground((Color)UIManager.get("ComboBox.background"));
//		        //}
//		        if(cellHasFocus || isSelected)
//		        	m_objComponent.repaint();
//		        	/*((JComponent)c).setBorder(BorderFactory.createLineBorder(Color.RED, 1));
//		        else ((JComponent)c).setBorder(UIManager.getBorder("ComboBox.border"));*/
//		        return c;
//		    }
//
//			
//		});
		
		m_objComponent.addFocusListener(this);

		((JComboBox)m_objComponent).setEnabled(m_enable);

		m_claseManejadora.setItemSelectable((JComboBox)m_objComponent);

		if( m_initialValue!=null){
			//System.outprintln("InitialValue de ComboBox:"+m_initialValue);
			((JComboBox)m_objComponent).setSelectedItem(m_initialValue);
		}

		//Se hace despues de asignar para que GListBox no ejecute itemStateChanged en la inicializacion
		((JComboBox)m_objComponent).addItemListener(m_claseManejadora);

		if(m_modoConsulta)
			convertirEnModoConsulta();

		
		if(!m_claseManejadora.isNullable() && !m_modoFilter)
			m_objComponent.setBackground(GConfigView.colorBackgroundRequired);

	}

	public Vector getListaInicial(){
		return m_lista;
	}

	public void calcularValorInicial(){
		int size = m_lista.size();
		for (int i = 0; i < size; i++) {
			ItemList it = (ItemList) m_lista.get(i);
			// System.out.println("INI "+it.isInitialSelected());
			if (it.isInitialSelected())
				m_initialValue = it;
		}
		/* setSelectedItem(v); */
		if (m_initialValue == null && size > 0)
			m_initialValue = (ItemList) m_lista.get(0);
	}

	public ItemList getValorInicial(){
		return m_initialValue;
	}

	/*Deshabilita los eventos del raton sobre el campo de texto y sobre el boton para que no salga la lista desplegable*/
	public void convertirEnModoConsulta(){
		((JComboBox)m_objComponent).setFocusable(false);

		MouseListener[] listenersText=((JComboBox)m_objComponent).getMouseListeners();
		int numListenersText=listenersText.length;
		for(int i=0;i<numListenersText;i++)
			((JComboBox)m_objComponent).removeMouseListener(listenersText[i]);

		Component[] componentes=((JComboBox)m_objComponent).getComponents();
		int numComp=componentes.length;
		int i=0;
		boolean encontrado=false;
		while(!encontrado && i<numComp){
			if(componentes[i] instanceof AbstractButton){
				MouseListener[] listenersBoton=componentes[i].getMouseListeners();
				int numListenersBoton=listenersBoton.length;
				for(int j=0;j<numListenersBoton;j++)
					((AbstractButton)componentes[i]).removeMouseListener(listenersBoton[j]);
				encontrado=true;
			}
			i++;
		}
	}

	public void initValue() {
		((JComboBox)m_objComponent).setSelectedItem(m_initialValue);
		m_objComponent.repaint();
	}
	
	public void focusGained(FocusEvent ev) {
		if(!m_modoConsulta && !m_modoFilter)
			m_objComponent.setBorder(GConfigView.borderSelected);
	}
	
	public void focusLost(FocusEvent ev) {
		if(!m_modoConsulta && !m_modoFilter)
			m_objComponent.setBorder(UIManager.getBorder("TextField.border"));
	}
	
	@Override
	public boolean newValueAllowed() {
		return m_claseManejadora.isNull();
	}

}
