package gdev.gawt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import dynagent.common.communication.docServer;
import gdev.gawt.tableCellEditor.TextCellEditor;
import gdev.gawt.utils.ItemList;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.GConfigView;
import gdev.gfld.GFormField;
import gdev.gfld.GFormEnumerated;
import gdev.gfld.GValue;

/**
 * Esta clase extiende a GComponent y creará una lista.
 * Una vez creada se podrá representar en la interfaz gráfica.
 * @author Juan
 * @author Francisco
 */
public class GList extends GComponent implements ItemSelectable, ActionListener, MouseListener, FocusListener, KeyListener
{
	private static final long serialVersionUID = 1L;
	//private final Component c= this;
	private JButton button;
	private JTable text;
	private JPanel panelText;
	private JPopupMenu menu;
	private JList m_comp;
	private JScrollPane scroll;
	private ArrayList<Object> listeners= new ArrayList<Object>();
	private boolean processEndEvent=false;
	private Object[] lastSelect=null;
	private Vector<ItemList> m_lista;
	private Dimension m_dimText;
	private JPanel panel;

	private boolean creado=false;
	private boolean m_enable;
	private String m_defaultVal;
	private boolean m_modoConsulta;
	private boolean m_modoFilter;
	private docServer m_server;
	private Vector<ItemList> m_initialValue;
	private GListBox m_claseManejadora;
	private JPanel checkBoxPanel;
	protected boolean processCheckBox=true;
	private MouseListener mouseListenerOpenList;//MouseListener para abrir el JList utilizado por los componentes que forman la seleccion del usuario cuando el JList ya esta cerrado
	private KeyListener keyListenerExternal;//Utilizado por GFocusTraversalPolicy para poder gestionar el salto del foco
	private JPanel closePanel;

	private class MyListCellRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = 1L;

		/*final static ImageIcon longIcon = new ImageIcon("long.gif");
	      final static ImageIcon shortIcon = new ImageIcon("short.gif");*/

		// This is the only method defined by ListCellRenderer.
		// We just reconfigure the JLabel each time we're called.

		public Component getListCellRendererComponent(
				JList list,              // the list
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // does the cell have focus
		{
			String s = value.toString();
			setText(s);
			/*setIcon((s.length() > 10) ? longIcon : shortIcon);*/
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			this.setHorizontalAlignment(SwingConstants.LEFT);
			return this;
		}
	} 

	public GList(GFormField ff,docServer server,Font fuente,boolean modoConsulta,boolean modoFilter,GListBox claseManejadora)
	{
		super(ff,claseManejadora.getComponentListener());
		/*m_dimText=new Dimension(((GFormEnumerated)ff).getComponentWidth()-anchoBoton,ff.getPreferredSize().height);*/
		/*Dimension dim=new Dimension(ff.getPreferredSize().width-ff.getLabelWidth()-20,ff.getPreferredSize().height);*/

	
		m_defaultVal=(String)ff.getDefaultVal();
		m_modoConsulta=modoConsulta;
		m_server=server;
		m_claseManejadora=claseManejadora;
		m_modoFilter=modoFilter;
		m_enable=ff.isEnabled();
		
		/*initLista(lista);*/
		/*un campo que no admite nulos antes de ser editado aparecerá con el nulo seleccionado

         Antes del submit habrá que comprobar que si hay un nulo donde no se admiten. En esta
         versión no se permite que el editor del listbox no muestre nulos en la lista desplegable
         cuando el en valor visalizado al inicio es nulo (en las tablas si se permite). Por tanto de
         momento a todas las listas añado el elemento nulo.
        Ver comentarios en DOC de DIseño de como se gestionan los cambios de valor en los formularios
		 */
		Vector<ItemList> vItems = new Vector<ItemList>();

		if(claseManejadora.isNullable() || m_modoFilter || (m_defaultVal==null || m_defaultVal.length()==0)){
			boolean nullInitialSelection=false;
			if(m_defaultVal!=null && m_defaultVal.length()>0)
				nullInitialSelection= m_defaultVal.equals("0");
			else
				nullInitialSelection= true;

			vItems.add(new ItemList("0",
					null,
					"",
					nullInitialSelection));
		}

		ArrayList<Integer> defaultValues=new ArrayList<Integer>();
		if(m_defaultVal!=null && m_defaultVal.length()>0 ){
			String[] vls = m_defaultVal.split(";");
			for (int v = 0; v < vls.length; v++)
				defaultValues.add(new Integer(vls[v]));
		}
		/*Iterator iValues= m_md.getEnumSet( iTapos );
		while(iValues.hasNext()){
			Integer idenum= (Integer)iValues.next();
			String name= m_md.getEnumLabel( iTapos, idenum );
			itemList itl= new itemList(idenum.toString(),
						   null,
						   name,
						   defaultValues.indexOf(idenum)>=0);
			vItems.add(itl);
		 */
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
					/*false*/);
			vItems.addElement(itl);
			/*vItems.addElement(val.getLabel());*/
		}
		/*m_objComponent = new JComboBox(vItems);*/
		Collections.sort(vItems);
		m_lista=vItems;
		calcularValorInicial();
		m_claseManejadora.setComponent(this);
		//createComponent();//Esto es provisional, deberia ser creado en GSimpleForm como los otros componentes
	}
	
	protected void createComponent()
	{
		if(!creado){//TODO creado creo que ya no hace falta. Probarlo sin creado
			Color color=(Color)UIManager.getColor("TextField.background");
			panel=new JPanel();
			panel.setBackground(color);
			/*boton= new JButton(img);*/
			//boton= new JButton(new ImageIcon(((communicator)m_server).getImage(null,"list")));
			
			//TODO Esta dimension no vale para nada ya que esta devolviendo 0 al no estar implementando en GFormEnumerated.
			//Luego lo hace bien porque le asigna el tamaño en setComponentBounds
			//Dimension dimButton=m_objFormField.getDimComponenteSecundario();
			
			int rowHeight=(int)m_objFormField.getRowHeight();
			button = botoneraAccion.subBuildBoton(
					null,
					null,
					"list",
					/*
					 * "ACTION:" + 0 + ":" + m_id + ":" +
					 * botoneraAccion.ABRIR + ":" + m_label
					 *//* commandString */GTable.BUTTON_ONE_FILE,
					 "Lista","lista@"+getFormField().getName(),/*dimButton.width*/rowHeight,/*dimButton.height*/rowHeight,true,m_server);

			mouseListenerOpenList=new MouseListener(){

				public void mouseClicked(MouseEvent arg0) {}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {
					//System.err.println("mouse Clicked text");
					if(!menu.isShowing()){
						button.requestFocusInWindow();
						SwingUtilities.invokeLater(new Runnable(){
							
							public void run() {
								button.doClick();
							}
							
						});
					}
				}
				public void mouseReleased(MouseEvent arg0) {}
				
			};
			
			/*boton.setPreferredSize(new Dimension(20, (int)dim.getHeight()));*/
			/*boton.setPreferredSize(new Dimension(anchoBoton, (int)m_dimText.getHeight()));*/
			FlowLayout fl= (FlowLayout)panel.getLayout();
			if(fl!=null){
				fl.setVgap(0);
				fl.setHgap(0);
				fl.setAlignment(FlowLayout.RIGHT);
			}
			text= new JTable(0,0);
			
			//text.setFocusable(false);
			text.setRowSelectionAllowed(false);
			text.setEnabled(false);
			text.addMouseListener(mouseListenerOpenList);
			/*text.setPreferredSize(m_dimText);*/
			panelText=new JPanel();
			panelText.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
			panelText.setBackground(color);
			panelText.setBorder(/*BorderFactory.createLoweredBevelBorder()*/(Border)UIManager.get("TextField.border"));
			panelText.add(text);
			panel.add( panelText );
			panel.add( button);

			//panel.setBorder(BorderFactory.createEtchedBorder());
			/*this.lista=lista;*/
			menu = new JPopupMenu();
			menu.addPopupMenuListener(new PopupMenuListener(){
				public void popupMenuCanceled(PopupMenuEvent ev){
					//boton.requestFocusInWindow();
					/*System.err.println("popupMenuCanceled");
					setListVisible(false);
					*/
					//Entra solo cuando se hace una seleccion y se pulsa fuera de la lista.
					//Con ESC no entra porque ya se encarga de procesarlo el keyListener
					if( hasChanged() ){
						setListVisible(false);
					}
				}
				public void popupMenuWillBecomeInvisible(PopupMenuEvent ev){}
				public void popupMenuWillBecomeVisible(PopupMenuEvent ev){}							
			});
			menu.setBackground(color);

			initLista(m_lista);
			m_claseManejadora.setItemSelectable(this);

			if( m_initialValue!=null)
				setSelectedValue(m_initialValue);

			// Se hace despues de asignar para que GListBox no ejecute itemStateChanged en la inicializacion
			this.addItemListener(m_claseManejadora);

			if(!m_modoConsulta){
				button.addActionListener(this);
				button.addFocusListener(this);
				panelText.addMouseListener(mouseListenerOpenList);
			}
			else convertirEnModoConsulta();
			/*m_objComponent = new JList(vItems);
	        if( menu.getComponentCount()>0 )
				menu.remove(0);
			menu.add(m_objComponent);
			m_objComponent.addMouseListener(this);
			m_objComponent.addKeyListener(this);
			boton.addActionListener(this);
			updateText();*/
			/*m_objComponent=m_comp;*/

			panel.setName(getFormField().getName());
			m_objComponent=panel;
			
			/*m_objComponent.addFocusListener(new FocusListener(){

				public void focusGained(FocusEvent arg0) {
					panelText.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
				}

				public void focusLost(FocusEvent arg0) {
					panelText.setBorder((Border)UIManager.get("TextField.border"));
				}
				
			});*/
			
			if(!m_claseManejadora.isNullable() && !m_modoFilter){
				panelText.setBackground(GConfigView.colorBackgroundRequired);
				menu.setBackground(GConfigView.colorBackgroundRequired);
				text.setBackground(GConfigView.colorBackgroundRequired);
				button.setBackground(GConfigView.colorBackgroundRequired);
				m_comp.setBackground(GConfigView.colorBackgroundRequired);
				closePanel.setBackground(GConfigView.colorBackgroundRequired);
			}
			
			setEnabled(m_enable);
			creado=true;
		}
	}

	/*Deshabilita los eventos del raton sobre el campo de texto y sobre el boton para que no salga la lista desplegable*/
	public void convertirEnModoConsulta(){
		button.setFocusable(false);

		MouseListener[] listeners=button.getMouseListeners();
		int numListeners=listeners.length;
		for(int i=0;i<numListeners;i++)
			button.removeMouseListener(listeners[i]);
	}

	// Reescribimos el metodo para tratar las peculiaridades de este componente ya que no se trata de uno que se pueda
	// redimensionar solo de la manera realizada en el metodo original
	public void setComponentBounds(Rectangle rc){
		Dimension dim=rc.getSize();
		/*if(dim.height!=m_dimText.height || dim.width!=m_dimText.width){*/
		//Si se han modificado las dimensiones en el procesamiento realizado en GGrpColumn (Alineamiento, hacer el componente mas grande...)
		button.setPreferredSize(new Dimension(/*20*/(int)dim.getHeight()/*Para que sea cuadrado*/, (int)dim.getHeight()));
		m_dimText=new Dimension((int)dim.getWidth()/*-20*/-(int)dim.getHeight(), (int)dim.getHeight());
		Insets insets=panelText.getBorder().getBorderInsets(panelText);
		Dimension dimTable=new Dimension((int)m_dimText.getWidth()-insets.left-insets.right,(int)m_dimText.getHeight()-insets.top-insets.bottom);

		panelText.setPreferredSize(m_dimText);
		panelText.setMinimumSize(m_dimText);
		panelText.setMaximumSize(m_dimText);

		text.setRowHeight((int)dimTable.getHeight());
		text.setPreferredSize(dimTable);
		text.setPreferredScrollableViewportSize(dimTable);
		text.getTableHeader().setPreferredSize(new Dimension(0, 0));
		Insets insetsscroll=scroll.getInsets();
		Dimension dimCheckBoxPanel=checkBoxPanel.getPreferredSize();
		m_comp.setPreferredSize(new Dimension((int)m_dimText.getWidth()-(int)dimCheckBoxPanel.getWidth()-4/*Gap FlowLayout 2+2*/,(int)m_comp.getPreferredSize().getHeight()));
		m_comp.setMinimumSize(new Dimension((int)m_dimText.getWidth()-(int)dimCheckBoxPanel.getWidth()-4/*Gap FlowLayout 2+2*/,(int)m_comp.getPreferredSize().getHeight()));
		m_comp.setMaximumSize(new Dimension((int)m_dimText.getWidth()-(int)dimCheckBoxPanel.getWidth()-4/*Gap FlowLayout 2+2*/,(int)m_comp.getPreferredSize().getHeight()));
		if ((int)m_comp.getPreferredSize().getHeight()<120){
			scroll.setPreferredSize(new Dimension((int)m_dimText.getWidth()+insetsscroll.left+insetsscroll.right,(int)m_comp.getPreferredSize().getHeight()+insetsscroll.bottom+insetsscroll.top));
			scroll.setMinimumSize(new Dimension((int)m_dimText.getWidth()+insetsscroll.left+insetsscroll.right,(int)m_comp.getPreferredSize().getHeight()+insetsscroll.bottom+insetsscroll.top));
			scroll.setMaximumSize(new Dimension((int)m_dimText.getWidth()+insetsscroll.left+insetsscroll.right,(int)m_comp.getPreferredSize().getHeight()+insetsscroll.bottom+insetsscroll.top));
		}else{
			scroll.setPreferredSize(new Dimension((int)m_dimText.getWidth()+insetsscroll.left+insetsscroll.right,120+insetsscroll.bottom+insetsscroll.top));
			scroll.setMinimumSize(new Dimension((int)m_dimText.getWidth()+insetsscroll.left+insetsscroll.right,120+insetsscroll.bottom+insetsscroll.top));
			scroll.setMaximumSize(new Dimension((int)m_dimText.getWidth()+insetsscroll.left+insetsscroll.right,120+insetsscroll.bottom+insetsscroll.top));
		}
		/*}*/
		super.setComponentBounds(rc);
	}

	
	/*public JComponent getComponent(){
		return m_comp;
	}*/

	public Vector<ItemList> getListaInicial(){
		return m_lista;
	}

	public void calcularValorInicial(){
		m_initialValue=new Vector<ItemList>();
		int size = m_lista.size();
		for (int i = 0; i < size; i++) {
			ItemList it = (ItemList) m_lista.get(i);
			// System.out.println("INI "+it.isInitialSelected());
			if (it.isInitialSelected())
				m_initialValue.add(it);
		}
		/* setSelectedItem(v); */
		if (m_initialValue.isEmpty() && size > 0)
			m_initialValue.add((ItemList) m_lista.get(0));
	}

	public Vector<ItemList> getValorInicial(){
		return m_initialValue;
	}

	public void initLista(Vector<ItemList> lista){
		if(m_comp==null){
			final JList list = new JList(lista);
			list.setCellRenderer(new MyListCellRenderer());
			list.addListSelectionListener(new ListSelectionListener() {
				//Al haber un cambio de seleccion actualizamos el checkBox asociado a cada linea
				@Override
				public void valueChanged(ListSelectionEvent e) {
					processCheckBox=false;//Evitamos que se ejecute el procesado del cambio en checkBox ya que si no crea conflicto y no se muestra la seleccion
					updateSelectionCheckBox();
					processCheckBox=true;//Restauramos
				}
			});
			checkBoxPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
			checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
			final boolean hasNullOption=!lista.isEmpty()?lista.get(0).label.equals(""):false;
			for(int i=0;i<lista.size();i++){
				JCheckBox checkBox=new JCheckBox();
				checkBox.setMargin(new Insets(1,0,0,0));
				checkBox.setIconTextGap(0);
				final int iThis=i;
				checkBox.addKeyListener(this);
				checkBox.addItemListener(new ItemListener() {
					
					@Override
					public void itemStateChanged(ItemEvent e) {
						if(m_comp.isShowing() && processCheckBox){
							if(e.getStateChange()==ItemEvent.SELECTED){
								if(iThis==0 && hasNullOption){
									list.setSelectedIndex(0);
								}else{
									int[] indices=list.getSelectedIndices();
									int[] indicesNew=new int[indices.length+1];
									int j=0;
									for(int i=0;i<indices.length;i++){
										if(indices[i]==0 && hasNullOption){//El nulo no lo dejamos seleccionado si se ha seleccionado otro registro
											j=1;
										}else{
											indicesNew[i-j]=indices[i];
										}
									}
									
									if(j==1){
										indicesNew=Arrays.copyOf(indicesNew, indicesNew.length-1);
									}
									
									indicesNew[indices.length-j]=iThis;
									list.setSelectedIndices(indicesNew);
								}
								
								updateSelectionCheckBox();
							}else if(e.getStateChange()==ItemEvent.DESELECTED){
								int[] indices=list.getSelectedIndices();
								int[] indicesNew;
								if(indices.length>1){
									indicesNew=new int[indices.length];
									int j=0;
									for(int i=0;i<indices.length;i++){
										if(indices[i]==iThis){
											j=1;
										}else{
											indicesNew[i-j]=indices[i];
										}
									}
									if(j==1){
										indicesNew=Arrays.copyOf(indicesNew, indicesNew.length-1);
									}
								}else{
									indicesNew=new int[0];
								}
								list.setSelectedIndices(indicesNew);
								updateSelectionCheckBox();
							}
						}
					}
				});
				checkBoxPanel.add(checkBox);
			}
			JPanel auxPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
			auxPanel.add(checkBoxPanel);
			auxPanel.add(list);
			m_comp= list;
			scroll= new JScrollPane(/*m_comp*/auxPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.getVerticalScrollBar().setUnitIncrement(GConfigView.IncrementScrollVertical);
			scroll.setBorder(BorderFactory.createEmptyBorder());
		}else{
			m_comp.setListData(lista);
		}

		if( menu.getComponentCount()>0 )
			menu.remove(0);
		menu.add(scroll);
		
		JButton closeButton=new JButton("Cerrar");
		closeButton.addKeyListener(this);
		closeButton.setMargin(new Insets(0,0,0,0));
		//closeButton.setPre
		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setListVisible(false);
			}
		});
		
		closePanel=new JPanel(new FlowLayout(FlowLayout.CENTER,0,2));
		closePanel.add(closeButton);
		closePanel.setBackground(m_comp.getBackground());
		menu.add(closePanel);
		
		m_comp.addMouseListener(this);
		m_comp.addKeyListener(this);
		/*list.setPreferredSize(new Dimension((int)m_dimText.getWidth(),(int)list.getPreferredSize().getHeight()));
		list.setMinimumSize(new Dimension((int)m_dimText.getWidth(),(int)list.getPreferredSize().getHeight()));*/
		/*menu.setPreferredSize(new Dimension(list.getPreferredSize()));*/
		updateText();
	}
	
	public void actionPerformed( ActionEvent ae ){
		if(!menu.isShowing()){
			try{
				setListVisible(true);
				
			}catch(Exception ex){
				ex.printStackTrace();
				m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al mostrar la lista");
			}
		}
	}

	public void setSelectedValue( ItemList it ){
		int index=m_lista.indexOf( it );
		//System.outprintln("LBSELECTION:"+index+":"+it);
		if( index>=0 ){
			m_comp.setSelectedIndex( index );
			updateText();
		}
	}

	public void setSelectedValue( Vector<ItemList> sel){
		m_comp.removeSelectionInterval( 0, m_lista.size() );
		int[] indices=new int[sel.size()];
		for(int s=0;s<sel.size();s++)
			indices[s]=m_lista.indexOf( (ItemList)sel.get(s));
		if( sel.size()>0 ){
			m_comp.setSelectedIndices( indices );
			updateText();
		}
	}

	// Este metodo no esta siendo usado, hay que ver si deberia ser llamado desde GlistBox.resetRestriction
	public void removeAllItems(){
		initLista(new Vector<ItemList>());
		if( m_initialValue!=null)
			setSelectedValue(m_initialValue);
	}

	public void setEnabled(boolean enable){
		m_comp.setEnabled(enable);
		menu.setEnabled(enable);
		button.setEnabled(enable);
		text.setEnabled(enable);
		panelText.setEnabled(enable);
	}
	public void setListVisible(boolean visible){
		if( visible ){
			lastSelect= getSelectedObjects();
			updateSelectionCheckBox();
			menu.show( panel, 0,panel.getPreferredSize().height);
			processEndEvent=false;
			if(!m_comp.requestFocusInWindow()){
				//Esto es un problema de Java que ocurre cuando el popup sobresale de la ventana principal. En ese caso tenemos que utilizar el requestFocus dependiente de la plataforma
				m_comp.requestFocus();
			}
		}else
			if( !processEndEvent ){
				menu.setVisible(false);
				menu.revalidate();
				menu.repaint();
				processEndEvent=true;
				//System.outprintln("PRE HAS CHANG");
				if( hasChanged() ){
					//System.outprintln("SI HAS CHANG:"+getSelectedObjects().length);
					Object[] currentSel= getSelectedObjects();
					for( int c=0; c<currentSel.length;c++){
						if( 	((ItemList)currentSel[c]).getIntId()==0 &&
								currentSel.length>1 ){
							int index= m_lista.indexOf( new ItemList("0",null,null,false) );
							m_comp.removeSelectionInterval(index, index);
						}
					}

					if(currentSel.length==0){
						int index= m_lista.indexOf( new ItemList("0",null,null,false) );
						m_comp.setSelectedIndex(index);
					}

					updateText();
					sayListeners();
				}else
					panel.repaint();
			}
	}

	private void updateSelectionCheckBox() {
		for(int i=0;i<checkBoxPanel.getComponentCount();i++){
			JCheckBox checkBox=(JCheckBox)checkBoxPanel.getComponent(i);
			checkBox.setSelected(m_comp.isSelectedIndex(i));
		}
	}

	private void sayListeners(){
		for( int i=0;i<listeners.size();i++){
			ItemListener it= (ItemListener)listeners.get(i);
			it.itemStateChanged( new ItemEvent(	this,
					ItemEvent.ITEM_STATE_CHANGED,
					it,
					ItemEvent.SELECTED ) );
		}
	}

	public void updateText(){
		Object[] row= getSelectedObjects();		
		Component comp1;

		if( row.length > 1 ){

			DefaultTableModel model = new DefaultTableModel(row,0);
			text= new JTable( model){				
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col){ 
					return false;
				}
			};
			text.getTableHeader().setPreferredSize(new Dimension(0, 0));
			text.setShowHorizontalLines(false);
			//text.setFocusable(false);
			text.setRowSelectionAllowed(false);
			text.setEnabled(false);
			text.addMouseListener(mouseListenerOpenList);
			
			if(!m_claseManejadora.isNullable() && !m_modoFilter)
				text.setBackground(GConfigView.colorBackgroundRequired);
			

			TableRenderer rend = new TableRenderer();
			for(int x=0; x<text.getColumnCount();x++){
				TableColumn tableColumn = text.getColumnModel().getColumn(x);			
				tableColumn.setCellRenderer( rend );						
			}	

			model.addRow(row);
			comp1 = text;//new JScrollPane(text);
			//JTableHeader tableHeader=text.getTableHeader();

			/*text.setBackground(Color.WHITE);
			Enumeration<TableColumn> columns=text.getColumnModel().getColumns();
			while(columns.hasMoreElements()){
				TableColumn tableColumn=columns.nextElement();
				tableColumn.setCellRenderer(new MyTableCellRenderer());
			}*/

			/*Insets insetsScroll=((JScrollPane)comp1).getInsets();
			insetsScroll.bottom=0;
			insetsScroll.top=0;
			 */

			/*Insets insetsHeader=tableHeader.getInsets();
			insetsHeader.bottom=0;
			insetsHeader.top=0;
			 */
			/*Insets insetsTable=text.getInsets();
			insetsTable.bottom=0;
			insetsTable.top=0;
			 */
			if(m_dimText!=null){
				Insets insets=panelText.getBorder().getBorderInsets(panelText);
				Dimension dimTable=new Dimension((int)m_dimText.getWidth()-insets.left-insets.right,(int)m_dimText.getHeight()-insets.top-insets.bottom);
				text.setRowHeight((int)dimTable.getHeight());
				text.setPreferredSize(dimTable);
				text.setPreferredScrollableViewportSize(dimTable);
				//tableHeader.setPreferredSize(dimTable);
			}
			//tableHeader.setBackground(UIManager.getColor("TextField.background"));
			
			//tableHeader.setFont(tableHeader.getFont().deriveFont(Font.BOLD));
		}else{
			if( row.length ==1 )
				comp1= new JLabel(row[0].toString(),SwingConstants.CENTER);
			else
				comp1= new JLabel();
		}
		panelText.remove(0);
		panelText.add(comp1);
		/*panel.remove(0);
		panel.add( comp1, 0 );*/
		panel.validate();
		panel.repaint();
	}	

	public boolean hasChanged(){
		Object[] nuevo= getSelectedObjects();
		if( lastSelect==null && nuevo==null ) return false;
		if( lastSelect==null && nuevo!=null ) return true;
		Arrays.sort(lastSelect);
		Arrays.sort(nuevo);
		return !Arrays.equals( lastSelect, nuevo );
	}

	public void mouseClicked(MouseEvent e){
		try{
			//System.outprintln("MOUSE CLICK");
			if( 	!(( e.getModifiers() & InputEvent.CTRL_MASK )== InputEvent.CTRL_MASK ))
				setListVisible(false);
			
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al ocultar la lista");
			ex.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent e){;}
	public void mouseExited(MouseEvent e){
		/*System.out.println("MOUSE EXIT");
		setListVisible( false );*/
	}
	public void mousePressed(MouseEvent e){;}
	public void mouseReleased(MouseEvent e){;}
	public void focusGained(FocusEvent e){
		if(!m_modoConsulta && !m_modoFilter)
			panelText.setBorder(GConfigView.borderSelected);
	}
	public void focusLost(FocusEvent e){
		//try{
		if(!e.isTemporary() && !m_modoConsulta && !m_modoFilter)
			panelText.setBorder((Border)UIManager.get("TextField.border"));
			//setListVisible( false );
		
		/*}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al ocultar la lista");
			ex.printStackTrace();
		}*/
	}

	public void addItemListener(ItemListener l){
		
		listeners.add( l );
	}
	public Object[] getSelectedObjects(){
		return m_comp.getSelectedValues();
	}
	public void removeItemListener(ItemListener l){
		if( !(l instanceof Object) ) return;
		int i= listeners.indexOf( (Object)l );
		if( i>=0 )
			listeners.remove(i);
	}
	public void keyPressed(KeyEvent e){
		try{
			if(e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyCode()==KeyEvent.VK_TAB){
				//System.err.println("KeyPressed GLIST");
				setListVisible(false);
			}else if(e.getKeyCode()==KeyEvent.VK_ESCAPE){//Cancelamos la nueva seleccion del usuario
				if(hasChanged()){
					m_comp.clearSelection();
					for(int i=0;i<lastSelect.length;i++){
						m_comp.setSelectedValue(lastSelect[i], true);
					}
					lastSelect=getSelectedObjects();//Evitamos que popupMenuCancelled lo vuelva a procesar
				}
			}
			if(keyListenerExternal!=null)
				keyListenerExternal.keyPressed(e);
			
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al ocultar la lista");
			ex.printStackTrace();
		}
	}
	public void keyReleased(KeyEvent e){
		try{
			if(e.getKeyCode()==KeyEvent.VK_CONTROL/* || e.getKeyCode()==KeyEvent.VK_ENTER*/)
				setListVisible(false);
			if(keyListenerExternal!=null)
				keyListenerExternal.keyReleased(e);
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al ocultar la lista");
			ex.printStackTrace();
		}
	}
	public void keyTyped(KeyEvent e){
		if(keyListenerExternal!=null)
			keyListenerExternal.keyTyped(e);
	}

	public void initValue() {
		initLista(m_lista);
		if( m_initialValue!=null){
			setSelectedValue(m_initialValue);
			sayListeners();
		}
		m_objComponent.repaint();
	}

	public class TableRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(SwingConstants.CENTER);
			return this;
		}
	}

	public JList getComponent() {
		return m_comp;
	}
	
	@Override
	public boolean newValueAllowed() {
		return m_claseManejadora.isNull();
	}
	
	public boolean isPopupVisible(){
		return menu.isVisible();
	}

	public JButton getButton() {
		return button;
	}

	public void setKeyListenerExternal(KeyListener keyListenerExternal) {
		this.keyListenerExternal = keyListenerExternal;
	}
}
