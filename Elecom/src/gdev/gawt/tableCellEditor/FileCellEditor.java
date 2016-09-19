package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gawt.utils.TextVerifier;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gfld.GTableColumn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import dynagent.common.communication.communicator;
import dynagent.common.communication.docServer;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.RowItem;

public class FileCellEditor extends CellEditor implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private JTextField editorComponent;
	private JLabel labelComponent;
	private JButton imageButton;
	private JPanel panelComponent;
	private Color colorDefaultComponent;
	private TextVerifierEditor textVerifier;
	private int clickCountToStart;	
	private boolean mustStop = true;
	private boolean showMessage = true;
	private String m_label;
	private String oldValue;
	private docServer server;
	private int type;
	private HashMap<String,ImageIcon> images;//Utilizado como cache de imagenes
	private int currentRow;
		
	public FileCellEditor(GTableColumn column, GTable tf,boolean filter, final FocusListener listener, docServer server, int type){
		super(tf,column);
		this.m_label=column.getLabel();
		clickCountToStart = 2;
		textVerifier=new TextVerifierEditor(column.getMask(), column.getType(), filter);
		this.server=server;
		this.type=type;
		this.currentRow=-1;
		editorComponent =new JTextField();
		//editorComponent.setBorder(BorderFactory.createEmptyBorder());
		editorComponent.setBorder(GConfigView.borderSelected);
		editorComponent.setInputVerifier(textVerifier);
		editorComponent.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent ev){
				try{
					mustStop=true;
					if(!gTable.getTable().isEditing())
						gTable.getTable().editCellAt(gTable.getTable().getSelectedRow(), gTable.getTable().getSelectedColumn());
					if(editorComponent.getText()!=null && !editorComponent.getText().equals("")){
						editorComponent.selectAll();
					}
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al seleccionar valor");
				}
			}
			public void focusLost(FocusEvent ev){
				try{
					if(mustStop && !ev.isTemporary() && !Auxiliar.equals(ev.getOppositeComponent(),imageButton)){					
						if(gTable.getTable().isEditing())
							stopCellEditing();
						listener.focusLost(ev);
					}
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al finalizar edición");
				}
			}
		});		
		editorComponent.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent ev) {
				showMessage=true;
			}
			public void keyReleased(KeyEvent ev) {}
			public void keyTyped(KeyEvent ev) {}
		});
		
		colorDefaultComponent=editorComponent.getBackground();
		
		this.panelComponent=new JPanel(new BorderLayout(0, 0));
		
		Dimension dimButton=new Dimension(column.getWidth(),column.getHeight());
		imageButton=botoneraAccion.subBuildBoton(
				null,
				null,
				"folder",
				/*
				 * "ACTION:" + 0 + ":" + m_id + ":" +
				 * botoneraAccion.ABRIR + ":" + m_label
				 *//* commandString */GTable.BUTTON_ONE_FILE,
				 "Buscar imagen", "buscar imagen", this, dimButton.width,dimButton.height,true,server);
		
		this.panelComponent.add(imageButton,BorderLayout.EAST);
		
		if(type==GConst.TM_IMAGE){
			labelComponent=new JLabel();
			labelComponent.setHorizontalAlignment(JLabel.CENTER);
			this.panelComponent.add(labelComponent,BorderLayout.CENTER);
		}else{
			this.panelComponent.add(editorComponent,BorderLayout.CENTER);
		}
		
		this.panelComponent.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				if(FileCellEditor.this.type==GConst.TM_IMAGE){
					imageButton.requestFocusInWindow();
					imageButton.requestFocus();
				}else{
					editorComponent.requestFocusInWindow();
					editorComponent.requestFocus();
				}
			}
		});
		
		images=new HashMap<String, ImageIcon>();
	}	
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		mustStop=true;
		if(value==null){
			oldValue=null;
			editorComponent.setText(null);
			if(labelComponent!=null){
				labelComponent.setIcon(null);
			}
		}else{			
			oldValue = textVerifier.format(value);
			editorComponent.setText(oldValue);

			if(type==GConst.TM_IMAGE){
				setImage(value);
			}
		}
		
		if(type==GConst.TM_IMAGE){
			if(!gTable.getModel().isNullable(row,column)){
				labelComponent.setBackground(GConfigView.colorBackgroundRequired);
				panelComponent.setBackground(GConfigView.colorBackgroundRequired);
			}else{
				labelComponent.setBackground(colorDefaultComponent);
				panelComponent.setBackground(colorDefaultComponent);
			}
		}else{
			if(!gTable.getModel().isNullable(row,column))
				editorComponent.setBackground(GConfigView.colorBackgroundRequired);
			else editorComponent.setBackground(colorDefaultComponent);
		}
		
		currentRow=row;
		
		return panelComponent;//editorComponent;
	}

	private void setImage(Object value) {
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
		labelComponent.setIcon(imageIcon);
		labelComponent.setIconTextGap(0);
		
// Comentado ya que si se agranda siendo la ultima fila que se ve en el scrollview la linea desaparece
//		if(!Auxiliar.equals(oldValue, value)){//Solo lo hacemos si se ha cambiado la imagen, ya que si no el renderer ya habra puesto el tamaño correcto
//			if(gTable.getTable().getRowHeight(currentRow)<imageIcon.getIconHeight()){
//				gTable.getTable().setRowHeight(currentRow,imageIcon.getIconHeight());
//			}
//		}
	}
    
    public Object getCellEditorValue(){
    	showMessage=true;
    	Object res = null;
    	if(!editorComponent.getText().equals(""))
    		res = editorComponent.getText();
    	
        return res;
    }    
    
    public boolean stopCellEditing() {
    	
    	InputVerifier verifier = editorComponent.getInputVerifier();
    	if(verifier!=null && !verifier.shouldYieldFocus(editorComponent)){
    		return false;
    	}
    	
    	 if(getCellEditorValue()!=null){
 			RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
 			if(rowItem.getColumnIdo(column.getColumn())==null){
 				if(rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())){
 					rowItem.setColumnOldIdo(column.getColumn(),rowItem.getColumnIdo(column.getColumn()));
 					rowItem.setColumnOldIdto(column.getColumn(),rowItem.getColumnIdto(column.getColumn()));
 					int action=rowItem.getState();
 					if(rowItem.isNullRow())
 						action=RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE;
 					else action=RowItem.SUBCREATION_STATE;
 					rowItem.setState(action);
 				}
 			}
         }
    	 mustStop=false;
    	return super.stopCellEditing();  
    }
    
    public class TextVerifierEditor extends TextVerifier{

    	public TextVerifierEditor(String mask, int sintax, boolean modoFilter) {
    		super(mask, sintax, modoFilter);
    		this.sintax=sintax;
    	}

    	public boolean verify(JComponent input) {
    		boolean verified=super.verify(input);
    		if(verified){
    			JTextComponent text= (JTextComponent)input;
        		if (text.getText() != null && text.getText().length()>0 && !Auxiliar.equals(oldValue, text.getText())){
        			verified=new File(((JTextComponent)input).getText()).exists();
        		}
    		}
    		
    		
    		return verified;
    	}

    	public boolean shouldYieldFocus(JComponent input){
    		//Exception ex=new Exception();
			//ex.printStackTrace();
    		if(!verify(input)){
    			if(showMessage){
    				showMessage = false;
    				String msg=buildMessageError(m_label);  				
		    		Window wind=SwingUtilities.windowForComponent(input);
		    		gTable.getMessageListener().showErrorMessage(msg,wind);
		    		input.setInputVerifier(this);	
    			}
    			return false;
    		}
    		return true;	
    	}    	
    }
    
    @Override
	public void cancelChangeValue() {
		this.editorComponent.setText(oldValue);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser=new JFileChooser();
		if(type==GConst.TM_IMAGE){
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Sólo Imágenes (jpg, jpeg, gif, png)", "jpg", "jpeg", "gif", "png");
			fileChooser.setFileFilter(filter);
		  	fileChooser.setAcceptAllFileFilterUsed(false);
		}
    	
    	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(GConst.LAST_FILE_PATH!=null)//Si existe una ultima ruta a la que accedimos abrimos en esa carpeta
    		fileChooser.setCurrentDirectory(new File(GConst.LAST_FILE_PATH));
    	//FileNameExtensionFilter filter = new FileNameExtensionFilter("Sólo Imágenes", "jpg", "jpeg", "gif", "png");
    	//fileChooser.setFileFilter(filter);
    	//fileChooser.setAcceptAllFileFilterUsed(false);
    	int status=fileChooser.showOpenDialog(gTable);
    	 if (status == JFileChooser.APPROVE_OPTION) {
    		 if(fileChooser.getSelectedFile()!=null){
             	//System.err.println("selected:"+fileChooser.getSelectedFile().getAbsolutePath());
             	//setValue(fileChooser.getSelectedFile().getAbsolutePath());
             	editorComponent.setText(fileChooser.getSelectedFile().getAbsolutePath());
        		if(type==GConst.TM_IMAGE){
        			setImage(fileChooser.getSelectedFile().getAbsolutePath());
        		}
             	GConst.LAST_FILE_PATH=fileChooser.getCurrentDirectory().getAbsolutePath();
         	}
    	 }
	}

	@Override
	public void setValue(Object value) {
		oldValue = textVerifier.format(value);
		editorComponent.setText(oldValue);
		if(type==GConst.TM_IMAGE){
			setImage(value);
		}
	}
}
