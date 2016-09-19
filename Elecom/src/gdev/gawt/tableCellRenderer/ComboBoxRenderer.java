package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gawt.utils.ItemList;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gfld.GTableColumn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import javax.swing.table.TableCellRenderer;

import de.muntjak.tinylookandfeel.Theme;
import de.muntjak.tinylookandfeel.TinyComboBoxButton;
import de.muntjak.tinylookandfeel.TinyComboBoxUI;
//import de.muntjak.tinylookandfeel.controlpanel.ColorRoutines;
import dynagent.common.utils.RowItem;

public class ComboBoxRenderer extends JComboBox implements TableCellRenderer {

	private static final long serialVersionUID = 1L;
	private HashMap<Integer, String> valueMap= new HashMap<Integer, String>();
	private HashMap<Integer,ItemList> idoItemListMap;
	private Font fuente;
	private String label;
	private GTable gTable;
	private GTableColumn gColumn;
	
	public ComboBoxRenderer(GTable gTable, GTableColumn gColumn){
		super();
		this.gTable=gTable;
		this.gColumn=gColumn;
		setUI(new TinyComboBoxUI(){
			
			class ComboBoxButton extends TinyComboBoxButton{

				private static final long serialVersionUID = 1L;

				public ComboBoxButton(JComboBox cb, Icon i, boolean onlyIcon, CellRendererPane pane, JList list) {
					super(cb, i, onlyIcon, pane, list);	
					//setBorder(BorderFactory.createLineBorder(Color.red));
				}
				
				//Lo sobreescribimos para que no tenga borde
				public void paintComponent(Graphics g) {
					/*Necesario para la version 1.4 de Tinylaf*/
					//InsetsUIResource insetsOld=Theme.comboInsets;
					//Theme.comboInsets=new InsetsUIResource(0,0,0,0);
					
					super.paintComponent(g);
					setBorder(BorderFactory.createLineBorder(comboBox.getBackground()));
				
					/*Necesario para la version 1.4 de Tinylaf*/
					//Theme.comboInsets=insetsOld;
				}
			}
			
			protected JButton createArrowButton() {
				JButton button = new ComboBoxButton(comboBox,
						null,
						comboBox.isEditable(),
						currentValuePane,
						listBox);
					button.setMargin(new Insets(0, 0, 0, 0));
					button.putClientProperty("isComboBoxButton", Boolean.TRUE);
				return button;
			}
		});
		label = "<"+gColumn.getLabel()+">";
		idoItemListMap=new HashMap<Integer, ItemList>();
		setBorder(BorderFactory.createEmptyBorder(0, GConfigView.horizontalMarginCell, 0, GConfigView.horizontalMarginCell));
		fuente = super.getFont();		
	}

	public void addLine(Integer id, String label){
		ItemList itemList=new ItemList(String.valueOf(id),null,	label, false);
		if(id!=0){
			addItem(itemList);
			idoItemListMap.put(id,itemList);
		}
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { 
		
		GTableModel tfm = (GTableModel) table.getModel();
		
		if(tfm.getRowData().size()>row){
			RowItem rowItem = (RowItem) tfm.getRowData().get(row);
			
			if(rowItem.isPermanent()){
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getSelectionForeground());
					setBackground(GConfigView.colorBackgroundPermanent);
				}
			}else{
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(table.getBackground());
				}
			}
			
			if(rowItem.isNullRow())
				super.setForeground(Color.gray);
		}
		
		String toolTipText=null;
		if(value!=null){
			setFont(fuente);
			setSelectedItem(idoItemListMap.get(value));
			setForeground(Color.black);
			if(table.isValid()){//Comprobamos antes si es valido porque si no el objeto Graphic de la tabla es null
				if(gTable.isTopLabel() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(String.valueOf(value), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=String.valueOf(value);
				else if(!gTable.isTopLabel() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(String.valueOf(value), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=this.gColumn.getLabel()+": "+String.valueOf(value);
			}else if(!gTable.isTopLabel())
				toolTipText=this.gColumn.getLabel();
		}else{
			setFont(new Font(fuente.getName(), Font.ITALIC, fuente.getSize()));
			addItem(label);
			setSelectedItem(label);
			if(table.isValid() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(label, false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
				toolTipText=this.gColumn.getLabel();
			else if(!gTable.isTopLabel())
				toolTipText=this.gColumn.getLabel();
			setForeground(Color.gray);
		}
		
		setToolTipText(toolTipText);
		
		return this;
	}
	public HashMap<Integer, String> getValueMap() {
		return valueMap;
	}
}