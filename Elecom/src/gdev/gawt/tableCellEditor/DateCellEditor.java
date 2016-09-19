package gdev.gawt.tableCellEditor;

import gdev.gawt.GEdit;
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
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Calendar;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import miniCalendar.DateRangeListener;
import miniCalendar.IDateListener;
import miniCalendar.JCalendar;
import miniCalendar.JCalendarDateTime;
import miniCalendar.JCalendarRange;

import dynagent.common.communication.docServer;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.RowItem;

public class DateCellEditor extends CellEditor implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private JTextField editorComponent;
	private JButton calendarButton;
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
		
	public DateCellEditor(GTableColumn column, GTable tf,boolean filter, final FocusListener listener, docServer server, int type){
		super(tf,column);
		this.m_label=column.getLabel();
		clickCountToStart = 2;
		textVerifier=new TextVerifierEditor(column.getMask(), column.getType(), filter);
		this.server=server;
		this.type=type;
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
					if(mustStop && !ev.isTemporary() && !Auxiliar.equals(ev.getOppositeComponent(),calendarButton)){					
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
		
		if(type==GConst.TM_DATE || type==GConst.TM_DATE_HOUR){
			Dimension dimButton=new Dimension(column.getWidth(),column.getHeight());
			calendarButton=botoneraAccion.subBuildBoton(
					null,
					null,
					"calendar",
					/*
					 * "ACTION:" + 0 + ":" + m_id + ":" +
					 * botoneraAccion.ABRIR + ":" + m_label
					 *//* commandString */GTable.BUTTON_ONE_FILE,
					 "Calendario", "calendario", this, dimButton.width,dimButton.height,true,server);
			
			this.panelComponent.add(calendarButton,BorderLayout.EAST);
		}
		this.panelComponent.add(editorComponent,BorderLayout.CENTER);
		
		this.panelComponent.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				editorComponent.requestFocusInWindow();
				editorComponent.requestFocus();
			}
		});
	}	
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		mustStop=true;
		if(value==null){
			oldValue=null;
			editorComponent.setText(null);				
		}else{			
			oldValue = textVerifier.format(value);
			editorComponent.setText(oldValue);
		}
		
		if(!gTable.getModel().isNullable(row,column))
			editorComponent.setBackground(GConfigView.colorBackgroundRequired);
		else editorComponent.setBackground(colorDefaultComponent);
		
		return panelComponent;//editorComponent;
	}
    
    public Object getCellEditorValue(){
    	showMessage=true;
    	Object res = null;
    	if(!editorComponent.getText().equals(""))
    		res = textVerifier.timeFecha(editorComponent.getText());   		
    	
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
    		return super.verify(input);
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
		showCalendar();
	}
	
	private void showCalendar(){
		Calendar valueCalendar=null;
		Calendar valueCalendarSec=null;
		JCalendar calendar=null;
		if(editorComponent.getText()!=null && !editorComponent.getText().isEmpty()){
			valueCalendar=Calendar.getInstance();
			String[] buf=editorComponent.getText().split("-");
			if(buf.length>1){
				valueCalendarSec=Calendar.getInstance();
				valueCalendar.setTimeInMillis(textVerifier.timeFecha(buf[0]));
				valueCalendarSec.setTimeInMillis(textVerifier.timeFecha(buf[1])) ;
			}else valueCalendar.setTimeInMillis(textVerifier.timeFecha(buf[0]));			
		}

			if(type==GConst.TM_DATE)
				calendar=new JCalendar(SwingUtilities.getWindowAncestor(panelComponent),calendarButton,valueCalendar,null,true);
			else if(type==GConst.TM_DATE_HOUR)
				calendar=new JCalendarDateTime(SwingUtilities.getWindowAncestor(panelComponent),calendarButton,valueCalendar,null,true,server);
			
			final JDialog dialog=calendar.buildDialog();
			calendar.setListener(new IDateListener(){

				public void setDateSelectioned(Calendar calendar,JCalendar calendarComponent) {
					Long milliseconds=calendar.getTimeInMillis();
					try{
						editorComponent.setText(textVerifier.format(milliseconds));
						
						//if(calendarComponent.getDialog()!=null)
						//	calendarComponent.getDialog().dispose();
					}catch(Exception ex){
						server.logError(SwingUtilities.getWindowAncestor(editorComponent),ex,"Error al asignar fecha");
						ex.printStackTrace();
					}
				}

				public void setAllDate(Calendar c) {
				}

			});
			dialog.setVisible(true);
			SwingUtilities.invokeLater(new Runnable(){

				@Override
				public void run() {
					dialog.toFront();
					if(!dialog.requestFocusInWindow()){
						dialog.requestFocus();
					}
				}
				
				
			});
			
			dialog.addWindowListener(new WindowListener() {
				
				@Override
				public void windowOpened(WindowEvent e) {}
				
				@Override
				public void windowIconified(WindowEvent e) {}
				
				@Override
				public void windowDeiconified(WindowEvent e) {}
				
				@Override
				public void windowDeactivated(WindowEvent e) {}
				
				@Override
				public void windowClosing(WindowEvent e) {}
				
				@Override
				public void windowClosed(WindowEvent e) {
					editorComponent.requestFocusInWindow();
					editorComponent.requestFocus();
				}
				
				@Override
				public void windowActivated(WindowEvent e) {}
			});
		}

	@Override
	public void setValue(Object value) {
		oldValue = textVerifier.format(value);
		editorComponent.setText(oldValue);
	}
}
