package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gfld.GTableColumn;

import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import dynagent.common.communication.communicator;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.RowItem;

public class TextCellRenderer extends JLabel implements TableCellRenderer{

	private static final long serialVersionUID = 1L;
	private Font fuente;
	private boolean modeFilter;
	private GTableColumn column;
	private GTable gTable;
	private int type;
	private HashMap<String,ImageIcon> images;//Utilizado como cache de imagenes
	private Integer redondeo;
	
	public TextCellRenderer(GTable gTable, GTableColumn column, boolean filter, int type, Integer redondeo) {
		super();
		this.column=column;
		this.gTable=gTable;
		this.modeFilter=filter;
		this.redondeo=redondeo;
		fuente = getFont();
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, GConfigView.horizontalMarginCell, 0, GConfigView.horizontalMarginCell));
		this.type=type;
		if(type==GConst.TM_IMAGE ||  column.getId().equalsIgnoreCase("type")){
			images=new HashMap<String, ImageIcon>();
		}
	}

	public GTableColumn getColumn() {
		return column;
	}

	public int getType() {
		return type;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		//System.err.println("**********RENDERER row:"+row+" col:"+column+" value:"+value);
		GTableModel tfm = gTable.getModel();
				
		if(tfm.getRowData().size()>row){
			RowItem rowItem = (RowItem) tfm.getRowData().get(row);
			
			if(rowItem.isPermanent()){
				if (isSelected) {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(GConfigView.colorBackgroundPermanent);
				}
			}else{
				if (isSelected) {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					super.setForeground(table.getForeground());
					super.setBackground(table.getBackground());
				}
			}
			
			if(rowItem.isNullRow())
				super.setForeground(Color.gray);
		}
		
		String toolTipText=null;
		if(value!=null){
			setFont(fuente);

			if(type==GConst.TM_IMAGE){
				setHorizontalAlignment(JLabel.CENTER);
				value=((String)value).split("#")[0];//Necesario porque cuando tiene mas de una imagen vienen separadas por #
				ImageIcon imageIcon=null;
				if(!images.containsKey(value)){//Comprobamos que antes no este la imagen en la cache ya que este metodo es llamado infinitas veces
					//System.err.println("Carga imagen:"+value);
					imageIcon=new ImageIcon((String)value);
					if(imageIcon.getImageLoadStatus()==MediaTracker.ERRORED){
						imageIcon=new ImageIcon(((communicator)gTable.getServer()).serverGetFilesURL((String)value));
					}else{//Si se ha cargado desde local se redimensiona. Desde base de datos no se redimensiona ya que directamente consultamos la imagen en miniatura
						Image imageAux=imageIcon.getImage();
				     	imageIcon=new ImageIcon(imageAux.getScaledInstance(/*width>=height?dimImage.width:*/-1, /*width<=height?*/GConfigView.smallImageHeight, Image.SCALE_SMOOTH));
					}
					images.put((String)value, imageIcon);
				}else imageIcon=images.get(value);
				setIcon(imageIcon);
				setIconTextGap(0);
				
				final ImageIcon imageIconThis=imageIcon;
				final int rowThis=row;
				if(gTable.getTable().getRowHeight(row)<imageIcon.getIconHeight()){
					SwingUtilities.invokeLater(new Runnable() {//Lo hacemos en un invokeLater para que no interactue con la gestion del enter y tabulador ya que provocaba que la siguiente celda no cogiera el foco
						
						@Override
						public void run() {
							gTable.getTable().setRowHeight(rowThis,imageIconThis.getIconHeight());
						}
					});
					
				}
				setText("");
			}else{
				if(type==GConst.TM_INTEGER || type ==GConst.TM_REAL){
					setHorizontalAlignment(JLabel.RIGHT);
					Object auxValue=value;
					if(type ==GConst.TM_REAL && redondeo!=null && !(auxValue instanceof String/*Esto ocurrira cuando se concatenan valores en una query principal porque la propiedad padre tiene card mayor a 1*/)){
						auxValue=Auxiliar.redondea((Double)auxValue, redondeo);
					}
					setText(String.valueOf(auxValue));
				}else{
					if(this.column.getId().equalsIgnoreCase("type")){
						ImageIcon imageIcon=null;
						if(!images.containsKey(value)){//Comprobamos que antes no este la imagen en la cache ya que este metodo es llamado infinitas veces
							//System.err.println("Carga imagen:"+value);
							String label=String.valueOf(value);
							imageIcon=((communicator)gTable.getServer()).getIcon(label.replace(' ', '_'));
							images.put((String)value, imageIcon);
						}else imageIcon=images.get(value);
						
						setHorizontalAlignment(JLabel.LEFT);
						if(imageIcon.getImageLoadStatus()==MediaTracker.ERRORED){
							setIcon(null);
							setText(String.valueOf(value));
						}else{
							setIcon(imageIcon);
							setIconTextGap(0);
							setText(String.valueOf(value));
						}
					}else{
						setHorizontalAlignment(JLabel.LEFT);
						setText(String.valueOf(value));
					}
				}
			}
			setForeground(Color.black);
			
			if(table.isValid()){//Comprobamos antes si es valido porque si no el objeto Graphic de la tabla es null
				if(gTable.isTopLabel() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=String.valueOf(value);
				else if(!gTable.isTopLabel() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=this.column.getLabel()+": "+String.valueOf(value);
			}
			else if(!gTable.isTopLabel())
				toolTipText=this.column.getLabel();
		}
		else{
			if(!modeFilter){
				setIcon(null);
				setIconTextGap(0);
				setText("<"+this.column.getLabel()+">");
				setFont(new Font(fuente.getName(), Font.ITALIC, fuente.getSize()));
				if(table.isValid() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=this.column.getLabel();
				else if(!gTable.isTopLabel())
					toolTipText=this.column.getLabel();
				setHorizontalAlignment(JLabel.CENTER);
				setForeground(Color.gray);
			}else{
				setIcon(null);
				setText("");
			}
		}
		setToolTipText(toolTipText);
		
		return this;
	}

	public boolean isOpaque() { 
		Color back = getBackground();
		Component p = getParent(); 
		if (p != null)
			p = p.getParent(); 
		
		boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) &&  p.isOpaque();
		return !colorMatch && super.isOpaque(); 
	}
}