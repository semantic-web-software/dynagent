package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gawt.utils.ButtonPopup;
import gdev.gawt.utils.Finder;
import gdev.gawt.utils.FinderPopUp;
import gdev.gawt.utils.TextVerifier;
import gdev.gen.DictionaryWord;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IDictionaryFinder;
import gdev.gfld.GTableColumn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.SwingWorker;

public class TextCellEditor extends CellEditor implements KeyListener {

	private static final String TEXT_REMOVE_SELECTION="<Ninguno>";
	private static final String TEXT_CREATION="<Nuevo>";
	private static final String TEXT_CREATION_TEMPORAL="<Nuevo Provis.>";
	
	private static final long serialVersionUID = 1L;
	private final int clickCountToStart = 2;	
	private JTextField editorComponent;
	private Color colorDefaultComponent;
	private TextVerifierEditor textVerifier;
	private FinderPopUp finderPopUp = null;
	private SwingWorker thread = null;
	private Finder finder = null;
	private String oldValue;
	private RowItem rowItem;
	private int row;

	private boolean mustStop = true;
	private boolean showMessage = true;
	private boolean incorrectValue = false;
	private boolean listFinderHasFocus;
	private boolean modeFilter;
	private boolean processedChange = false;//Usado para saber si se ha procesado ya el cambio antes de shouldYieldFocus
	protected boolean processFinder = false;//Usado para que no se muestre el finder por dejar pulsada una tecla estando en otro campo y soltarla al llegar a este, ocurre en las lineas de los documentos
	private static final int OPTION_ERROR = 1;
	private static final int OPTION_LAST_VALUE = 2;
	private static final int OPTION_MODIFY = 3;

	public TextCellEditor(GTableColumn column, boolean modeFilter, GTable table, final FocusListener listener){
		super(table,column);
		this.modeFilter=modeFilter;
		textVerifier=new TextVerifierEditor(column.getMask(), column.getType(), modeFilter);
		editorComponent =new JTextField();	
		//editorComponent.setBorder(BorderFactory.createEmptyBorder());
		editorComponent.setBorder(GConfigView.borderSelected);
		editorComponent.setInputVerifier(textVerifier);
		editorComponent.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent ev){
				mustStop=true;
				//System.err.println("FocusGained TextCellEditor component:"+(ev.getComponent()!=null?ev.getComponent().getClass():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass():null)+" temporary:"+ev.isTemporary());
				//if(!ev.isTemporary()){
					if(!gTable.getTable().isEditing())
						gTable.getTable().editCellAt(gTable.getTable().getSelectedRow(), gTable.getTable().getSelectedColumn());
					if(!rowItem.isNullRow() && editorComponent.getText()!=null && !editorComponent.getText().equals("")){
						editorComponent.selectAll();
					}
				//}
				finder=null;//Quitamos el finder para que no este influenciado por el finder anterior
				processedChange=false;
				processFinder =false;
			}
			public void focusLost(FocusEvent ev){
				/*System.err.println("FocusLost TextCellEditor component:"+(ev.getComponent()!=null?ev.getComponent().getClass():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass():null)+" temporary:"+ev.isTemporary());
				System.err.println("FocusLost:"+mustStop);*/
				if(!ev.isTemporary()){
					if(thread!=null){
						//System.err.println("Interrumpe en el focusLost");
						thread.interruptLater(false);
					}
					if(mustStop){ 
						if(gTable.getTable().isEditing())
							stopCellEditing();
						listener.focusLost(ev);
						//System.err.println("entraaaaa");
						mustStop=true;
					}
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
		
		if(shouldAddKeyListener())//Se añade este KeyListener si tenemos finder
			editorComponent.addKeyListener(this);		
		
		colorDefaultComponent=editorComponent.getBackground();
		
	}	
	
	public boolean shouldAddKeyListener(){
		return (column.hasFinder() && gTable.getDictionaryFinder()!=null && !modeFilter);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		/*Exception e=new Exception();
		   e.printStackTrace();
		System.err.println("getTableCellEditorComponent isSelected:"+isSelected);*/
		mustStop = true;
		this.row=row;
		GTableModel tfm = (GTableModel) gTable.getModel();
		rowItem = (RowItem) tfm.getRowData().get(row);
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
			
		editorComponent.setToolTipText(TextCellEditor.this.column.getLabel());
		//System.err.println("getTableCellEditorComponent "+editorComponent.getText());
		return editorComponent;
	}

	public Object getCellEditorValue(){
		showMessage=true;
		Object res = null;
		if(editorComponent.getText().equals("")){
			res = null;			
		}else{
			res = textVerifier.format(editorComponent.getText());
		}
		//System.err.println("getCellEditorValue new |"+res+"| old |"+editorComponent.getText()+"|");
		return res;
	}

	public boolean stopCellEditing() {
		/*System.err.println("stopCellEditing");
		Exception ex=new Exception();
		ex.printStackTrace();*/
		InputVerifier verifier = editorComponent.getInputVerifier();
		if(verifier!=null && !verifier.shouldYieldFocus(editorComponent)){
			return false;
		}
		
		if(!column.hasFinder() && getCellEditorValue()!=null){
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
		
		if (thread!=null){
			thread.interruptLater(true);//Ponemos true para evitar que se envien datos al motor mientras se esta haciendo la query la cual podria haber cargado datos en motor que provocarian disparo de reglas al enviar datos al motor concurrentemente. Si esperamos se hace rollback de los datos que podia haber cargado la query.
		}
		//if(gTable.getTable().isEditing())
			return super.stopCellEditing();
		//return true;
	}     

	public void keyPressed(KeyEvent ev){
		try{
			//System.err.println("evPressed "+ev.getKeyChar());
//			if(ev.getSource()!=editorComponent)
//				editorComponent.setCaretPosition(0);
//				//editorComponent.selectAll();
			//ev.consume();
			processFinder=true;
			if(finderPopUp!=null && finderPopUp.isVisible()){
				if(ev.getKeyCode()==KeyEvent.VK_DOWN){
					finderPopUp.setSelectedNextButton();
					
				}else if(ev.getKeyCode()==KeyEvent.VK_UP){
					finderPopUp.setSelectedPreviousButton();
					
				}else if(ev.getKeyCode()==KeyEvent.VK_ENTER || ev.getKeyCode()==KeyEvent.VK_TAB){
//					finderPopUp.getSelectedButton().doClick();
					submitWithFinder();
				}else if(ev.getKeyCode()==KeyEvent.VK_ESCAPE/* || ev.getKeyCode()==KeyEvent.VK_TAB*/){
					/*RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
					if(!rowItem.isNullRow()){
						findInDictionary(TextCellEditor.OPTION_LAST_VALUE);
					}*/
					if(finderPopUp!=null && finderPopUp.isShowing()){
						listFinderHasFocus=false;
						editorComponent.setText(oldValue);
						finderPopUp.setVisible(false);	
						//stopCellEditing();
					}
					processedChange=true;
					cancelCellEditing();
				}
			}else{
				if(ev.getKeyCode()==KeyEvent.VK_ENTER  || ev.getKeyCode()==KeyEvent.VK_TAB || ev.getKeyCode()==KeyEvent.VK_DOWN || ev.getKeyCode()==KeyEvent.VK_UP){
					submitWithoutFinder();
				}else if(ev.getKeyCode()==KeyEvent.VK_ESCAPE/* || ev.getKeyCode()==KeyEvent.VK_TAB*/){
					/*RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
					if(!rowItem.isNullRow()){
						findInDictionary(TextCellEditor.OPTION_LAST_VALUE);
					}*/
					if(finderPopUp!=null && finderPopUp.isShowing()){
						listFinderHasFocus=false;
						editorComponent.setText(oldValue);
						finderPopUp.setVisible(false);	
						//stopCellEditing();
					}
					processedChange=true;
					cancelCellEditing();
				}
			}
		}catch(Exception ex){
			gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al realizar la operación");
			ex.printStackTrace();
		}
	}

	public void keyReleased(KeyEvent ev) { 
		try{
			//System.err.println("evReleased "+ev.getKeyChar()+" "+ev.getKeyCode()+" "+ev.getModifiers()+" "+ev.getModifiersEx());
			//ev.consume();
			if(processFinder && ev.getKeyCode()!=KeyEvent.VK_DOWN && ev.getKeyCode()!=KeyEvent.VK_UP && 
					ev.getKeyCode()!=KeyEvent.VK_RIGHT && ev.getKeyCode()!=KeyEvent.VK_LEFT &&
					ev.getKeyCode()!=KeyEvent.VK_ENTER && ev.getKeyCode()!=KeyEvent.VK_TAB && ev.getKeyCode()!=KeyEvent.VK_SHIFT
					&& ev.getKeyCode()!=KeyEvent.VK_ESCAPE && ev.getKeyCode()!=KeyEvent.VK_ALT && ev.getModifiersEx()!=KeyEvent.CTRL_DOWN_MASK && ev.getKeyCode()!=KeyEvent.VK_CONTROL
					&& ev.getKeyCode()!=KeyEvent.VK_F2/*Se utiliza para entrar en edicion en la celda*/)
			{ 
				/*if(ev.getKeyCode()==KeyEvent.VK_ENTER){
					if(finderPopUp!=null && finderPopUp.isVisible()){
							String text = finderPopUp.getSelectedButton().getText();
							DictionaryWord dw = dicParc.get(text);
							if(dw==null)//Se trataria de la opcion "crear uno nuevo", la cual no esta en el diccionario
								creationRow();
							else if((rowItem.isNullRow() && gTable.hasCreationRow() && rowItem.getColumnPar().get(1).intValue()!=rowItem.getColumnIdto(column.getColumn())))
								submit(text, dw, true);
							else submit(text, dw, false);
					}else{
						
						if(editorComponent.getText()!=null && !editorComponent.getText().isEmpty()){
							if(rowItem.isNullRow()){
								if(column.hasCreation())
									creationRow();
								else{
									if(thread==null && (finderPopUp==null || !finderPopUp.isVisible()))
										findInDictionary(TextCellEditor.OPTION_ERROR);
								}
							}else if(column.hasFinder() && rowItem.getColumnPar().get(0).intValue()!=rowItem.getColumnIdo(column.getColumn())){
								if(thread==null && (finderPopUp==null || !finderPopUp.isVisible()))
									findInDictionary(TextCellEditor.OPTION_ERROR);
							}else{
								stopCellEditing();
							}
						}
					}
				}else{*/
					//System.err.println("KeyReleased "+ev.getKeyCode()+" "+gTable.getTable().getSelectedRow()+" "+gTable.getTable().hasFocus());
				//System.err.println("isCreating:"+gTable.getModel().isCreating()+" "+rowItem.getColumnPar().get(0)+" "+rowItem.getColumnIdo(column.getColumn()));
					if(gTable.getTable().getSelectedRow()==-1){
						cancelCellEditing();
						gTable.getTable().setRowSelectionInterval(row, row);
					}else if(!(gTable.getModel().isCreating() && Auxiliar.equals(rowItem.getIdRow().getIdo(),rowItem.getColumnIdo(column.getColumn())))){
						// Si estamos creando la fila no permitimos finder sobre el ido principal porque si fuera una creacion directa a base de datos
						// estariamos bajo una sesion incorrecta. Ademas el ido creado no estaría enganchado aun a la tabla por lo que no podriamos sustituirlo.
						if(column.getTypeFinder()==GTableColumn.NORMAL_FINDER || column.getTypeFinder()==GTableColumn.CREATION_FINDER){
							RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
							//if(rowItem.isNullRow() || !rowItem.getColumnPar().get(0).equals(rowItem.getColumnIdo(column.getColumn()))){
								editorComponent.requestFocusInWindow();
								listFinderHasFocus=true;
								showDictionary();
							//}
						}/*else if(column.getTypeFinder()==GTableColumn.HIDDEN_FINDER){
							RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
							//if(!rowItem.getColumnPar().get(0).equals(rowItem.getColumnIdo(column.getColumn()))){
								editorComponent.requestFocusInWindow();
								listFinderHasFocus=true;
								showFinder(new TreeMap<String, DictionaryWord>(),mustSelectionFinder());
							//}
						}*/
					}
				/*}*/
			}
		}catch(Exception ex){
			gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al realizar la operación");
			ex.printStackTrace();
		}
	}

	public void keyTyped(KeyEvent ev){}

	public void showDictionary(){
		//System.err.println("showDictionary");
		if (thread!=null){
			thread.interruptLater(false);
			//System.err.println("Interrumpeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		}else{
			//System.err.println("No interrumpeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		}
		thread = new SwingWorker(){

			public Object construct() {
				try {
					Thread.sleep(300);
					if(!hasInterruptRequest()){
						TreeMap<String, DictionaryWord> dicParc=null;
						//Cogemos el valor aqui para evitar que, al estar en un hilo distinto del AWT, pueda hacer la busqueda con un valor
						//y luego crear el finder diciendole que el root es otro valor porque el usuario lo haya cambiado mientras se estaba haciendo la busqueda
						String editorText=editorComponent.getText();
						if(finder==null){
							IDictionaryFinder iDF = gTable.getDictionaryFinder();
							IdObjectForm idColumn=new IdObjectForm(column.getId());
							RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
							if(!rowItem.isNullRow()){
								idColumn.setIdo(rowItem.getColumnIdoFilter(column.getColumn()));
								idColumn.setIdto(rowItem.getColumnIdtoFilter(column.getColumn()));
							}
							if(!hasInterruptRequest()){
								//System.err.println("Thread intenta buscar para el finder");
								LinkedHashMap<String, DictionaryWord> dictionary=new LinkedHashMap<String, DictionaryWord>();
								boolean appliedLimit=iDF.getDictionary(gTable.getId(),/*column.getId()*/idColumn.getIdString(), editorText, false, dictionary);
								if(!hasInterruptRequest()){
									finder=new Finder(dictionary, editorText, appliedLimit);
									dicParc=finder.getWordsForUser(editorText);
								}
							}
						}else{
							if(!finder.isAppliedLimitDictionary())//Si no ha aplicado limite podemos buscar sin tener que volver a pedirlo a bd
								dicParc=finder.getWordsForUser(editorText);
							
							if(dicParc==null){
								IDictionaryFinder iDF = gTable.getDictionaryFinder();
								IdObjectForm idColumn=new IdObjectForm(column.getId());
								RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
								if(!rowItem.isNullRow()){
									idColumn.setIdo(rowItem.getColumnIdoFilter(column.getColumn()));
									idColumn.setIdto(rowItem.getColumnIdtoFilter(column.getColumn()));
								}
								if(!hasInterruptRequest()){
									//System.err.println("Thread intenta buscar para el finder");
									LinkedHashMap<String, DictionaryWord> dictionary=new LinkedHashMap<String, DictionaryWord>();
									boolean appliedLimit=iDF.getDictionary(gTable.getId(),/*column.getId()*/idColumn.getIdString(), editorText, false, dictionary);
									if(!hasInterruptRequest()){
										finder.setDictionary(dictionary, editorText, appliedLimit);
										dicParc=finder.getWordsForUser(editorText);
									}
								}
							}
						}
						if(!hasInterruptRequest())
							return dicParc;
					}
				} catch (InterruptedException e) {
					//System.err.println("Se interrumpe el sleep puesto que se a eliminado el hilo");
					
				}
				//System.err.println("Construct interruptLater");
				return null;
			}

			@SuppressWarnings("unchecked")
			public void finished(){
				if(get()!=null){
					TreeMap<String, DictionaryWord> result=(TreeMap<String, DictionaryWord>)get();
					boolean mustSelectionFinder=false;//mustSelectionFinder();No forzamos aqui la seleccion ya que la pistola a veces envia caracteres lentos y selecciona otro registro al pulsar enter
					showFinder(result,mustSelectionFinder);
				}
				// Si hay una peticion de interrupcion no lo ponemos a null ya que si se pulsara enter pensaria que no hay otro hilo thread esperando y provocariamos una doble consulta
				if(!hasInterruptRequest())
					thread=null;
			}
		};
		thread.start();
	}
	
	private boolean mustSelectionFinder(){
		return column.hasFinder() && 
			(rowItem.getColumnIdo(column.getColumn())==null && !column.hasCreation()) ||
			(rowItem.getColumnIdo(column.getColumn())!=null && !column.isEnable()) ||
			(rowItem.getColumnIdo(column.getColumn())!=null && !gTable.getModel().isNewIdo(rowItem.getColumnIdo(column.getColumn())) && !column.isBasicEdition())/* ||
			(!gTable.getModel().allowModifyIdoCell(rowItem.getColumnIdo(column.getColumn())))*/
			/*(rowItem.getColumnPar().get(0)>0 && column.getTypeFinder()==GTableColumn.CREATION_FINDER)*/;
	}
	
	private void showFinder(TreeMap<String, DictionaryWord> dicParc,boolean selectedFirst){
		if(finderPopUp!=null)
			finderPopUp.setVisible(false);
		finderPopUp = new FinderPopUp(editorComponent);
		finderPopUp.addPopupMenuListener(new PopupMenuListener(){
			public void popupMenuCanceled(PopupMenuEvent ev){
				//System.err.println("popupMenuCanceled");
				if(finderPopUp.getSelectedButton()!=null){
					findInDictionary(TextCellEditor.OPTION_LAST_VALUE);
					//submitWithFinder();
					processedChange=true;
					stopCellEditing();
				}else listFinderHasFocus=false;//findInDictionary(TextCellEditor.OPTION_MODIFY);
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent ev){}
			public void popupMenuWillBecomeVisible(PopupMenuEvent ev){}							
		});
		boolean hasButtons=false;
		//System.err.println("Antes remove");
		if(isRemoveAllowed()){
			ButtonPopup notSelection = createSpecialButtonFinder(TEXT_REMOVE_SELECTION);
			finderPopUp.add(notSelection);
			hasButtons=true;
			//System.err.println("Entra remove");
		}
		Iterator<String> it = dicParc.keySet().iterator();
		for(int i=0;i<dicParc.size() && i<50/*Ponemos un limite de 50 para evitar problemas de memoria*/;i++){
			String s = it.next();
			final DictionaryWord dw = dicParc.get(s);
			ButtonPopup b = new ButtonPopup(s);
			if(dw.isTemporal())
				b.setFont(new Font(b.getFont().getName(), Font.ITALIC, b.getFont().getSize()));
			b.setActionCommand(dw.getWord());
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ev) {
					//String text = ((ButtonPopup)ev.getSource()).getText();
					String text = ev.getActionCommand();
					if((rowItem.isNullRow() && gTable.hasCreationRow() && rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())))
						submit(text, dw, true);
					else submit(text, dw, false);
					((ButtonPopup)ev.getSource()).removeActionListener(this);
					processedChange=true;
					stopCellEditing();
				}
			});
			b.setPreferredSize(new Dimension(b.getPreferredSize().width,(int)gTable.getHeightRow()));
			finderPopUp.add(b);
			hasButtons=true;
		}
		//if(rowItem!=null && (rowItem.isNullRow() || (oldValue==null && rowItem.getColumnPar().get(1).intValue()!=rowItem.getColumnIdto(column.getColumn())))&& column.hasFinder() && column.hasCreation()){
		/*if(isCreationAllowed()){
			final String text;
			if(column.isEnable())
				text=TEXT_CREATION;
			else text=TEXT_CREATION_TEMPORAL;
			ButtonPopup editar = createSpecialButtonFinder(text);
			finderPopUp.add(editar);
			hasButtons=true;
		}*/
		if(hasButtons){
			if(editorComponent.hasFocus()){
				//System.err.println("showww");
				finderPopUp.show(editorComponent, 0, editorComponent.getHeight(), selectedFirst);
			}
		}else{
			listFinderHasFocus=false;
		}
	}
	
	private ButtonPopup createSpecialButtonFinder(final String text){
		ButtonPopup button = new ButtonPopup();
		button.setText(text);
		button.setFont(new Font(button.getFont().getName(), Font.ITALIC, button.getFont().getSize()));
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev) {
				if(text.equals(TEXT_REMOVE_SELECTION)){
					removeSelection();
				}else{
					if(rowItem.getIdRow().getIdto()==rowItem.getColumnIdto(column.getColumn())){
						creationRow(RowItem.CREATION_STATE);
					}else{
						if(rowItem.isNullRow())
							creationRow(RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE);
						else creationRow(RowItem.SUBCREATION_STATE);
					}
				}
				((ButtonPopup)ev.getSource()).removeActionListener(this);
				processedChange=true;
				stopCellEditing();
			}
		});
		button.setPreferredSize(new Dimension(button.getPreferredSize().width,(int)gTable.getHeightRow()));
		
		return button;
	}
	
	private boolean isCreationAllowed(){
		//if(rowItem!=null && (rowItem.isNullRow() || (oldValue==null && rowItem.getColumnPar().get(1).intValue()!=rowItem.getColumnIdto(column.getColumn())))&& column.hasFinder() && column.hasCreation())
		if(rowItem!=null && (rowItem.isNullRow() || rowItem.getColumnIdo(column.getColumn())==null)&& column.hasFinder() && (column.getTypeFinder()==GTableColumn.NORMAL_FINDER || column.getTypeFinder()==GTableColumn.CREATION_FINDER) && column.hasCreation())
			return true;
		
		return false;
	}
	
	private boolean isRemoveAllowed(){
		if(rowItem!=null && !rowItem.isNullRow() && editorComponent.getText().equals("") && oldValue!=null && column.hasFinder() && rowItem.getIdRow().getIdo()!=rowItem.getColumnIdo(column.getColumn()))
			return true;
		
		return false;
	}

	public void submit(String text, DictionaryWord dw, boolean creationRow){
		//System.err.println("Submit "+creationRow);
		if(thread!=null)
			thread.interruptLater(false);		
		RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
		rowItem.setColumnOldIdo(column.getColumn(),rowItem.getColumnIdo(column.getColumn()));
		rowItem.setColumnOldIdto(column.getColumn(),rowItem.getColumnIdto(column.getColumn()));
		rowItem.setColumnIdo(column.getColumn(), dw.getIdo());
		rowItem.setColumnIdto(column.getColumn(), dw.getIdto());
		rowItem.setState(creationRow?RowItem.CREATION_STATE+RowItem.FINDER_STATE:RowItem.FINDER_STATE);
		editorComponent.setText(text);
		listFinderHasFocus=false;
		if(finderPopUp!=null && finderPopUp.isShowing())
			finderPopUp.setVisible(false);
		//stopCellEditing();
	}

	private boolean findInDictionary(int option) {

		listFinderHasFocus=false;
		if(finderPopUp!=null && finderPopUp.isShowing())
			finderPopUp.setVisible(false);
		if(thread!=null)
			thread.interruptLater(false);
		
		if(editorComponent.getText().equals("") && oldValue==null){
			editorComponent.setText(null);
			
			//stopCellEditing();
			return false;
		}else{
			IDictionaryFinder iDF = gTable.getDictionaryFinder();
			LinkedHashMap<String, DictionaryWord> dictionary=new LinkedHashMap<String, DictionaryWord>();
			iDF.getDictionary(gTable.getId(),column.getId(), editorComponent.getText(), true, dictionary);
			finder=new Finder(dictionary, editorComponent.getText(), false);
			Integer ido=rowItem.getColumnIdo(column.getColumn());
			Integer idto=rowItem.getColumnIdto(column.getColumn());
			TreeMap<String, DictionaryWord> dicParc=finder.getWords(editorComponent.getText(),ido,idto);//ATENCION: Si hay mas de un individuo con el mismo valor este metodo se queda con el ultimo

			//System.err.println("Compare:"+editorComponent.getText()+" "+dicParc);
			DictionaryWord dw=dicParc.get(editorComponent.getText());
			if(dw!=null){
				String text=dw.getWord();
				if((rowItem.isNullRow() && gTable.hasCreationRow() && rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())))
					submit(text, dw, true);
				else submit(text, dw, false);
				return true;
				//stopCellEditing();
			}else{
				if(option == TextCellEditor.OPTION_ERROR){
					incorrectValue=true;
					gTable.getMessageListener().showErrorMessage("El valor introducido no existe en la lista de posibilidades",SwingUtilities.getWindowAncestor(gTable));			
				}else if(option == TextCellEditor.OPTION_LAST_VALUE){
					editorComponent.setText(oldValue);
					//stopCellEditing();
				}else if(option == TextCellEditor.OPTION_MODIFY){
					if(rowItem.getColumnIdo(column.getColumn())==null){
						if(column.hasCreation()){
							if(rowItem.getIdRow().getIdto()==rowItem.getColumnIdto(column.getColumn())){
								creationRow(RowItem.CREATION_STATE);
							}else{
								if(rowItem.isNullRow())
									creationRow(RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE);
								else creationRow(RowItem.SUBCREATION_STATE);
							}
						}else{
							incorrectValue=true;
							String message="El valor introducido no existe en la lista de posibilidades";
							if(gTable.hasCreationRow())
								message+=" y esta columna no permite la creación";
							else message+=" y no es posible su creación directamente.\nUtilice los botones de la tabla.";
							
							gTable.getMessageListener().showErrorMessage(message,SwingUtilities.getWindowAncestor(gTable));
						}
					}else if(!column.isEnable() || (!column.isBasicEdition() && !gTable.getModel().isNewIdo(rowItem.getColumnIdo(column.getColumn())))){
						incorrectValue=true;
						gTable.getMessageListener().showErrorMessage("El valor introducido no existe en la lista de posibilidades y esta columna no permite la edición",SwingUtilities.getWindowAncestor(gTable));
					}
				}
				return false;
			}
		}
	}

	private void creationRow(int rowAction) {
		//System.err.println("CREATION "+editorComponent.getText());
		listFinderHasFocus=false;
		if(finderPopUp!=null && finderPopUp.isShowing())
			finderPopUp.setVisible(false);
		else{
			if(thread!=null)
				thread.interruptLater(false);
		}
		if(getCellEditorValue()!=null){
			RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
			rowItem.setColumnOldIdo(column.getColumn(),rowItem.getColumnIdo(column.getColumn()));
			rowItem.setColumnOldIdto(column.getColumn(),rowItem.getColumnIdto(column.getColumn()));
			rowItem.setState(rowAction);
			//stopCellEditing();
		}else{
			gTable.getMessageListener().showErrorMessage("Para crear debe introducir un valor en la columna",SwingUtilities.getWindowAncestor(gTable));
		}
	}
	
	private void modifyRow() {
		//System.err.println("MODIFY "+editorComponent.getText());
		listFinderHasFocus=false;
		if(finderPopUp!=null && finderPopUp.isShowing())
			finderPopUp.setVisible(false);
		else{
			if(thread!=null)
				thread.interruptLater(false);
		}
		
		//No hacemos nada mas porque ya JTable se encarga de cambiar el foco y provocar la modificacion
	}
	
	private void removeSelection() {
		//System.err.println("REMOVE "+editorComponent.getText());
		listFinderHasFocus=false;
		if(finderPopUp!=null && finderPopUp.isShowing())
			finderPopUp.setVisible(false);
		else{
			if(thread!=null)
				thread.interruptLater(false);
		}
		
			RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
			rowItem.setColumnOldIdo(column.getColumn(),rowItem.getColumnIdo(column.getColumn()));
			rowItem.setColumnOldIdto(column.getColumn(),rowItem.getColumnIdto(column.getColumn()));
			//System.err.println("idParent:"+rowItem.getColumnIdParent(column.getColumn()));
			rowItem.setState(RowItem.REMOVE_STATE);
			//stopCellEditing();
	}
	
	public KeyListener getKeyListener(){
		return this;
	}
	
	//Submit sin el finder mostrandose, ya sea porque la columna no tiene o porque no se encontraron resultados
	private void submitWithoutFinder(){
		if(editorComponent.getText()!=null && !editorComponent.getText().isEmpty()){
			if(rowItem.isNullRow()){
				if(column.getTypeFinder()==GTableColumn.HIDDEN_FINDER){
					findInDictionary(TextCellEditor.OPTION_ERROR);
					//processedChange=true;
				}
//				else if(column.hasCreation()){
//					/*if(rowItem.getColumnPar().get(1).intValue()==rowItem.getColumnIdto(column.getColumn())){
//						creationRow(RowItem.CREATION_STATE);
//					}else creationRow(RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE);*/
//					findInDictionary(TextCellEditor.OPTION_MODIFY);
//				}
//				else{
//					//if(thread==null && (finderPopUp==null || !finderPopUp.isVisible()))
//						if(!editorComponent.getText().equals(oldValue))
//							findInDictionary(TextCellEditor.OPTION_ERROR);
//				}
				else if(column.hasFinder()){
					if(!editorComponent.getText().equals(oldValue))
						findInDictionary(TextCellEditor.OPTION_MODIFY);
				}else{
					//stopCellEditing();//Ya se encarga JTable de parar la edicion de la tabla
				}
			}else if(column.hasFinder()/* && !rowItem.getColumnPar().get(0).equals(rowItem.getColumnIdo(column.getColumn()))*/){
				//if(thread==null && (finderPopUp==null || !finderPopUp.isVisible()))
					if(!editorComponent.getText().equals(oldValue)){
						if(gTable.isProcessingPasteRows()){
							if(rowItem.getColumnIdo(column.getColumn())!=null && !column.isUniqueValue()){
								modifyRow();
							}else findInDictionary(TextCellEditor.OPTION_MODIFY);
						}else{
							if(mustSelectionFinder())
								findInDictionary(TextCellEditor.OPTION_ERROR);
							else if(rowItem.getColumnIdo(column.getColumn())!=null){
								if(/*gTable.getModel().isCreating() && gTable.getModel().getIdoRowEditing().equals(rowItem.getColumnIdo(column.getColumn()))*/gTable.getModel().isNewIdo(rowItem.getColumnIdo(column.getColumn()))){
									//System.err.println("isUniqueValueeee:"+column.isUniqueValue());
									if(!column.isUniqueValue())
										modifyRow();
									else findInDictionary(TextCellEditor.OPTION_MODIFY);
								}else findInDictionary(TextCellEditor.OPTION_MODIFY);
							}else findInDictionary(TextCellEditor.OPTION_MODIFY);
						}
					}
			}else{
				//stopCellEditing();//Ya se encarga JTable de parar la edicion de la tabla
			}
		}
		if(!incorrectValue)
			processedChange=true;
	}
	
	//Submit con el finder mostrandose
	private void submitWithFinder(){
		ButtonPopup b=finderPopUp.getSelectedButton();
		if(b!=null){
			String wordForUser = finderPopUp.getSelectedButton().getText();
			TreeMap<String, DictionaryWord> dicParc=finder.getWordsForUser(editorComponent.getText());
			DictionaryWord dw = dicParc.get(wordForUser);
			
			if(dw==null){//Se trataria de la opcion "ninguno", la cual no esta en el diccionario
				if(wordForUser.equalsIgnoreCase(TEXT_REMOVE_SELECTION))
					removeSelection();
			}else{
				String text=dw.getWord();
				if((rowItem.isNullRow() && gTable.hasCreationRow() && rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())))
					submit(text, dw, true);
				else submit(text, dw, false);
			}
		}else if(!column.hasCreation() && finderPopUp.getComponents().length==1){
			String wordForUser = ((ButtonPopup)finderPopUp.getComponents()[0]).getText();
			TreeMap<String, DictionaryWord> dicParc=finder.getWordsForUser(editorComponent.getText());
			DictionaryWord dw = dicParc.get(wordForUser);
			
			if(dw!=null){
				String text=dw.getWord();
				if(Auxiliar.equals(text, editorComponent.getText())){
					if((rowItem.isNullRow() && gTable.hasCreationRow() && rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())))
						submit(text, dw, true);
					else submit(text, dw, false);
				}else{
					findInDictionary(TextCellEditor.OPTION_MODIFY);
				}
			}else{
				findInDictionary(TextCellEditor.OPTION_MODIFY);
			}
		}else if(column.hasCreation()){
			if(rowItem.getColumnIdo(column.getColumn())==null){
				TreeMap<String, DictionaryWord> dicParc=finder.getWords(editorComponent.getText(),null,null);
				if(dicParc==null){
					findInDictionary(TextCellEditor.OPTION_MODIFY);
				}else{
					DictionaryWord dw=dicParc.get(editorComponent.getText());
					
					if(dw==null && finder.isAppliedLimitDictionary()){
						IDictionaryFinder iDF = gTable.getDictionaryFinder();
						LinkedHashMap<String, DictionaryWord> dictionary=new LinkedHashMap<String, DictionaryWord>();
						iDF.getDictionary(gTable.getId(),column.getId(), editorComponent.getText(), true, dictionary);
						finder=new Finder(dictionary, editorComponent.getText(), false);
						dicParc=finder.getWords(editorComponent.getText(),null,null);
	
						//System.err.println("Compare:"+editorComponent.getText()+" "+dicParc);
						dw=dicParc.get(editorComponent.getText());
					}
					if(dw!=null){
						//TODO Faltaria que comprobemos si es el rdn. Si no es rdn no deberíamos seleccionarlo ya que seria una edicion 
						String text=dw.getWord();
						if((rowItem.isNullRow() && gTable.hasCreationRow() && rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())))
							submit(text, dw, true);
						else submit(text, dw, false);
					}else if(rowItem.getIdRow().getIdto()==rowItem.getColumnIdto(column.getColumn())){
						creationRow(RowItem.CREATION_STATE);
					}else{
						if(rowItem.isNullRow())
							creationRow(RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE);
						else creationRow(RowItem.SUBCREATION_STATE);
					}
				}
			}else{
				if(gTable.isProcessingPasteRows()){
					if(!column.isUniqueValue()){
						modifyRow();
					}else findInDictionary(TextCellEditor.OPTION_MODIFY);
				}else if(/*gTable.getModel().isCreating() && gTable.getModel().getIdoRowEditing().equals(rowItem.getColumnIdo(column.getColumn()))*/gTable.getModel().isNewIdo(rowItem.getColumnIdo(column.getColumn()))){
					//System.err.println("isUniqueValueeee:"+column.isUniqueValue());
					if(!column.isUniqueValue())
						modifyRow();
					else findInDictionary(TextCellEditor.OPTION_MODIFY);
				}else findInDictionary(TextCellEditor.OPTION_MODIFY);
				
//				if(!incorrectValue)
//					processedChange=true;
			}
		}else{
			//modifyRow();
			findInDictionary(TextCellEditor.OPTION_MODIFY);
		}
		
		if(!incorrectValue)
			processedChange=true;
	}
	
	public class TextVerifierEditor extends TextVerifier{

		private int sintax;

		public TextVerifierEditor(String mask, int sintax, boolean modoFilter) {
			super(mask, sintax, modoFilter);
			this.sintax=sintax;
		}

		public boolean verify(JComponent input) {
			JTextField text= (JTextField) input;    		
			boolean exito= super.verify(input);
			if(exito){
				if(sintax==GConst.TM_INTEGER || sintax==GConst.TM_REAL )
					editorComponent.setText( text.getText() );
			}
			return exito;
		}

		public boolean shouldYieldFocus(JComponent input){
			//System.err.println("shouldYieldFocus");
			if(incorrectValue){
				incorrectValue=false;
				editorComponent.setText(oldValue);
				//return true;
				return false;//No permitimos el cambio de foco para que el usuario pueda asignar otro valor
			}else{	
				if(!verify(input)){
					if(showMessage){
						showMessage=false;
						String msg=buildMessageError(column.getLabel());  				
						Window wind=SwingUtilities.windowForComponent(input);
						gTable.getMessageListener().showErrorMessage(msg,wind);
						input.setInputVerifier(this);
					}
					return false;
				}
				//System.err.println("input.hasFocus:"+input.hasFocus()+" listFinderHasFocus:"+listFinderHasFocus+" finder:"+finder+" finderPopup:"+finderPopUp);
				if(input.hasFocus()){
					//TODO Comprobar bien estas condiciones del if porque muchas creo que ya no son necesarias al tener luego el processedChange
					if(mustStop && !listFinderHasFocus /*&& mustSelectionFinder()*/ && !editorComponent.getText().equals(oldValue)){// && ((finder!=null && /*(!finder.containsWord(editorComponent.getText()) ||*/ finderPopUp!=null && finderPopUp.getSelectedButton()==null)|| column.getTypeFinder()==GTableColumn.HIDDEN_FINDER))/*)*/{
						//System.err.println("editorComponent.getText():"+editorComponent.getText()+" finder.getWords(editorComponent.getText()):"+finder.getWords(editorComponent.getText())+" finderPopUp.getSelectedButton():"+finderPopUp.getSelectedButton());
						if(!processedChange){
							//System.err.println("******************GESTIONAAA "+editorComponent.getText());
							if(finder!=null && finderPopUp!=null && finderPopUp.isVisible()){
								submitWithFinder();
							}else{
								submitWithoutFinder();
							}
							if(incorrectValue){
								incorrectValue=false;
								editorComponent.setText(oldValue);
								//return true;
								return false;//No permitimos el cambio de foco para que el usuario pueda asignar otro valor
							}
						}
					}
					return !listFinderHasFocus;
				}
				return /*((column.getTypeFinder()==GTableColumn.HIDDEN_FINDER && !processedChange)?false:*/true;/*);*/
			}
		}    	
	}

	@Override
	public void cancelChangeValue() {
		this.editorComponent.setText(oldValue);
	}

	@Override
	public void setValue(Object value) {
		oldValue=this.editorComponent.getText();
		this.editorComponent.setText((String)value);
	}
}