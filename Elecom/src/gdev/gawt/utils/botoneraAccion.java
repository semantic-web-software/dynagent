package gdev.gawt.utils;

import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConst;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import dynagent.common.communication.docServer;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.Utils;

public class botoneraAccion /*extends scopeContainer*/{
	public final static int EDITAR=1;
	public final static int CREAR=4;
	public final static int ELIMINAR=8;
	public final static int UNLINK=9;
	public final static int BUSCAR=16;
	public final static int ABRIR=32;
	public final static int EJECUTAR=64;
	public final static int CANCEL=128;
	public final static int CLOSE=256;
	public final static int EDITAR_FILTRO=512;
	public final static int APLICAR_FILTRO=513;
	public final static int PREV=1024;
	public final static int NEXT=1025;
	public final static int RESET=2048;
	public final static int LAUNCH=4096;
	public final static int CONSULTAR=8192;
	public final static int RESET_ALL=8193;
	public final static int ASIGNAR=8194;
	public final static int PRINT=8195;
	public final static int ACTION=8196;
	public final static int IMPORT=8197;
	public final static int EXPORT=8198;
	public final static int HELP=8199;
	public final static int PRINT_SEARCH=8200;
	public final static int INCREASE_ZOOM=8202;
	public final static int DECREASE_ZOOM=8203;
	public final static int EMAIL = 8204;
	public final static int CONFIG_COLUMNPROPERTIES = 8205;
	//public final static int MAIN_IMAGE=8204;
	public final static int ROWADD=2;
	public final static int ROWDEL=3;
	public final static int REPORT=5;
	public final static int OPERATION_ACTION=6;
	public final static int OPERATION_SCROLL=7;
	
	public final static int RECORD_TYPE=10;
	public final static int VIEW_TYPE=11;
	public final static int SEARCH_TYPE=12;
	public final static int LINKING_TYPE=13;
	public final static int TABLE_TYPE=14;
	
	
	
	public double buttonWidth;
	public double buttonHeight;
	
	
	ArrayList<AbstractButton> botones=new ArrayList<AbstractButton>();
	int numButtons;
	
	JButton m_botonEditar=null;
	JPanel m_comp;
	boolean lockable=true;
	docServer server;
	//private communicator m_comm;

	public botoneraAccion(/*communicator com,
		     metaData md,*/
			String id,
			String name,
			/*Object tg,*//*Target tg,*//*Integer id,*/
			/*threadActionMenu acciones,*/
			HashMap<Integer, String> idtoReports,
			HashMap<Integer, String> idtoReportsDirectPrint,
			HashMap<Integer, String> idtoNameActions,
			HashMap<Integer, String> idtoNameCreationActions,
			HashMap<Integer, String> idtoImports,
			HashMap<Integer, String> idtoExports,
			HashMap<Integer, String> idoReportFormats,
			boolean email,
			int formType,
			JPanel botoneraExternaInicio,
			JPanel botoneraExternaFin,
			ITableNavigation tableNavigation,
			ActionListener list,
			/*access myAccess,*//*OperationsObject operations,*//*HashMap<Integer,ArrayList<UserAccess>> accessUserTasks,*/AccessAdapter accessAdapter,
			boolean modoConsultar,
			boolean endStep,
			Graphics graphics,
			docServer server,
			JComponent componentParentShortCut/*Si es null utiliza toda la ventana para ejecutar el shortcut de los botones. Si tiene valor solo funcionaran cuando ese componente o uno hijo, tengan el foco*/,
			boolean allowChangeColumnProperties){
		
		this.server=server;
		buttonHeight= GViewBalancer.getRowHeightS(graphics);
		buttonWidth=buttonHeight;
		/*super( myAccess );*/
		//m_comm=Singleton.getComm();
		build(	
				/*com,
	     md,*/
				id,
				name,
				idtoReports,
				idtoReportsDirectPrint,
				idtoNameActions,
				idtoNameCreationActions,
				idtoImports,
				idtoExports,
				idoReportFormats,
				email,
				/*tg,*//*id,*/
				/*myAccess,*//*operations,*//*accessUserTasks,*/accessAdapter,
				/*acciones,*/
				formType,
				botoneraExternaInicio,
				botoneraExternaFin,
				tableNavigation,
				list,
				modoConsultar,
				endStep,
				componentParentShortCut,
				allowChangeColumnProperties);
				
		
	}


	private void build(	/*communicator com,
			metaData md,*/
			String id,
			String name,
			HashMap<Integer, String> idtoReports,
			HashMap<Integer, String> idtoReportsDirectPrint,
			HashMap<Integer, String> idtoNameActions,
			HashMap<Integer, String> idtoNameCreationActions,
			HashMap<Integer, String> idtoImports,
			HashMap<Integer, String> idtoExports,
			HashMap<Integer, String> idoReportFormats,
			boolean email,
			/*Object tg,*//*Target tg,*//*Integer id,*/
			AccessAdapter accessAdapter,
			/*threadActionMenu acciones,*/
			int formType,
			JPanel botoneraExternaInicio,
			JPanel botoneraExternaFin,
			ITableNavigation tableNavigation,
			final ActionListener list,
			boolean modoConsultar,
			boolean endStep,
			JComponent componentParentShortCut,
			boolean allowChangeColumnProperties){
		m_comp= new JPanel(new FlowLayout(FlowLayout.CENTER,formType!=TABLE_TYPE?10:0,0));
		/*access myAccess= getAccess();*/
		
		numButtons=0;
		//this.ContainerIsFinder=ContainerIsFinder;


		/*FlowLayout fl=(FlowLayout)m_comp.getLayout();
		fl.setVgap(0);
		fl.setHgap(0);*/
		m_comp.setBorder(new EmptyBorder(0,0,0,0));
		/*int id= 0;

	if( tg!=null)
		id= tg.getId();*/
		/*if( tg!=null && tg instanceof Target )
		id= ((Target)tg).ctx.id;*/

		/*if( tg!=null && tg instanceof Integer )
		id= ((Integer)tg).intValue();*/

		//int ancho=0;

		if( botoneraExternaInicio!=null )
			m_comp.add( botoneraExternaInicio );

		/*int idtoUserTask=-1;*/
		//System.err.println("idBotonera:"+id);
		IdOperationForm idOperation=new IdOperationForm();
		idOperation.setOperationType(OPERATION_ACTION);
		idOperation.setTarget(new IdObjectForm(id));

		int whenShortCut=componentParentShortCut==null?JComponent.WHEN_IN_FOCUSED_WINDOW:JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
		
		if(formType!=TABLE_TYPE){
			if( formType==VIEW_TYPE/*!actualizarButton && modoConsultar && !TargetIsTable && popupContainer*/ ){
				idOperation.setButtonType(CLOSE);
				String idString=idOperation.getIdString();
				JButton button=subBuildBoton(getComponent(),				      
						Utils.normalizeLabel("CERRAR"),
						null,
						/*"ACTION:" + id + ":" + idtoUserTask + ":" + CLOSE,*/idString,
						null,
						list,0,(int)buttonHeight,
						lockable);
				botones.add(button);
				
				GConst.addShortCut(componentParentShortCut, button, GConst.CANCEL_SHORTCUT_KEY, GConst.CANCEL_SHORTCUT_MODIFIERS, "Cancelar", whenShortCut, null);
				
				numButtons++;
			}
			if( (formType==RECORD_TYPE && endStep)|| formType==SEARCH_TYPE || formType==LINKING_TYPE/*actualizarButton && !modoConsultar*/){
				idOperation.setButtonType(EJECUTAR);
				String idString=idOperation.getIdString();
				JButton button=subBuildBoton(getComponent(),
						(formType==RECORD_TYPE ? Utils.normalizeLabel("Aceptar"):Utils.normalizeLabel("Buscar")),
						null,
						/*"ACTION:"+id+":"+ idtoUserTask +":"+EJECUTAR,*/idString,
						(formType==RECORD_TYPE ? Utils.normalizeLabel("Guardar y cerrar"):Utils.normalizeLabel("Buscar resultados")),
						list,0,(int)buttonHeight,lockable );
				
				if(formType!=RECORD_TYPE){
					GConst.addShortCut(componentParentShortCut, button, GConst.QUERY_SHORTCUT_KEY, GConst.QUERY_SHORTCUT_MODIFIERS, "Buscar", whenShortCut, null);
					button.setMnemonic(GConst.QUERY_SHORTCUT_KEY);
				}
				
				botones.add(button);
				numButtons++;
			}
			
			if(formType==RECORD_TYPE && endStep){
				if(idtoNameActions!=null && !idtoNameActions.isEmpty()){   
					final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
					Iterator<Integer> it = idtoNameActions.keySet().iterator();
					while(it.hasNext()){
						int idto = it.next();
						JMenuItem b = new JMenuItem(idtoNameActions.get(idto));
						b.setContentAreaFilled(false);
						b.setBorderPainted(false);
						b.addActionListener(list);

						idOperation.getTarget().setIdtoUserTask(idto);
						idOperation.setButtonType(ACTION);
						String idString=idOperation.getIdString();
						b.setActionCommand(idString);

						buttons.add(b);
					}
					
					IdObjectForm idObjectOperation=new IdObjectForm();
					String idString=idObjectOperation.getIdString();
					
					final JButton button=subBuildBoton(getComponent(),Utils.normalizeLabel("Aceptar y Acción"),
							null,
							/*"ACTION:"+id+":"+ idtoUserTask +":"+EJECUTAR,*/idString,
							Utils.normalizeLabel("Guardar y ejecutar acción"),
							list,0,(int)buttonHeight,lockable );
					button.addActionListener(new ActionListener(){

						public void actionPerformed(ActionEvent e) {
							PopupOptions p=new PopupOptions(buttons, button, server, list);
							p.show(false);
						}					
					});
					botones.addAll(buttons);
					//botones.add(button);
					//numButtons++;

				}
				
				if(idtoExports!=null && !idtoExports.isEmpty()){   
					final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
					Iterator<Integer> it = idtoExports.keySet().iterator();
					while(it.hasNext()){
						int idto = it.next();
						JMenuItem b = new JMenuItem(idtoExports.get(idto));
						b.setContentAreaFilled(false);
						b.setBorderPainted(false);
						b.addActionListener(list);

						idOperation.getTarget().setIdtoUserTask(idto);
						idOperation.setButtonType(EXPORT);
						String idString=idOperation.getIdString();
						b.setActionCommand(idString);

						buttons.add(b);
					}
					
					IdObjectForm idObjectOperation=new IdObjectForm();
					String idString=idObjectOperation.getIdString();
					
					final JButton button=subBuildBoton(getComponent(),Utils.normalizeLabel("Aceptar y Exportar"),
							null,
							/*"ACTION:"+id+":"+ idtoUserTask +":"+EJECUTAR,*/idString,
							Utils.normalizeLabel("Guardar y ejecutar exportación"),
							list,0,(int)buttonHeight,lockable );
					button.addActionListener(new ActionListener(){

						public void actionPerformed(ActionEvent e) {
							PopupOptions p=new PopupOptions(buttons, button, server, list);
							p.show(false);
						}					
					});
					botones.addAll(buttons);
					//botones.add(button);
					//numButtons++;

				}
			}
			
			if( formType==LINKING_TYPE && endStep/*actualizarButton && TargetIsTable && popupContainer && asignationMode*/){
				idOperation.setButtonType(ASIGNAR);
				String idString=idOperation.getIdString();
				JButton button=subBuildBoton(getComponent(),Utils.normalizeLabel("ASIGNAR"),null,/*"ACTION:"+id+":"+ idtoUserTask +":"+CLOSE*/idString, Utils.normalizeLabel("Asignar resultado y cerrar"), list,0,(int)buttonHeight,lockable );
				
				button.setMnemonic(GConst.ASSIGN_SHORTCUT_KEY);
				GConst.addShortCut(componentParentShortCut, button, GConst.ASSIGN_SHORTCUT_KEY, GConst.ASSIGN_SHORTCUT_MODIFIERS, "Asignar", whenShortCut, null);
				
				botones.add(button);
				numButtons++;
			}
	
			if( formType==SEARCH_TYPE || formType==LINKING_TYPE /*acciones!=null &&*/ /*tg!=null*/ /*&& tg instanceof Target*//*isFilterRoot*//*actualizarButton && TargetIsTable && !selectionMode*//*modoBusqueda*/){
				idOperation.setButtonType(RESET_ALL);
				String idString=idOperation.getIdString();	
				JButton button=subBuildBoton(getComponent(),Utils.normalizeLabel("RESET TODO"),null,/*"ACTION:"+id+":"+ idtoUserTask +":"+RESET_ALL*/idString, Utils.normalizeLabel("Quitar filtrado y resultados"), list,0,(int)buttonHeight,lockable );
				
				button.setMnemonic(GConst.RESETALL_SHORTCUT_KEY);
				GConst.addShortCut(componentParentShortCut, button, GConst.RESETALL_SHORTCUT_KEY, GConst.RESETALL_SHORTCUT_MODIFIERS, "Reset Todo", whenShortCut, null);
				
				botones.add(button);
				numButtons++;
			}

			if(formType==LINKING_TYPE || formType==RECORD_TYPE /*popupContainer && ((!TargetIsTable && !modoConsultar)|| (actualizarButton && TargetIsTable))*/ ){
				idOperation.setButtonType(CANCEL);
				String idString=idOperation.getIdString();
				final JButton button=subBuildBoton(getComponent(),Utils.normalizeLabel("CANCELAR"),null,/*"ACTION:"+id+":"+ idtoUserTask +":"+CANCEL*/idString, Utils.normalizeLabel("Cancelar y cerrar"), list,0,(int)buttonHeight,lockable );
								
				GConst.addShortCut(componentParentShortCut, button, GConst.CANCEL_SHORTCUT_KEY, GConst.CANCEL_SHORTCUT_MODIFIERS, "Cancelar", whenShortCut, null);

				botones.add(button);
				numButtons++;
			}
			
			if(formType==VIEW_TYPE || formType==RECORD_TYPE /*popupContainer && ((!TargetIsTable && !modoConsultar)|| (actualizarButton && TargetIsTable))*/ ){
				idOperation.setButtonType(HELP);
				String idString=idOperation.getIdString();
				botones.add(subBuildBoton(getComponent(),null,"help",/*"ACTION:"+id+":"+ idtoUserTask +":"+CANCEL*/idString, Utils.normalizeLabel("Consultar ayuda"), list,(int)buttonWidth,(int)buttonHeight,lockable ));
				
				numButtons++;
			}	
		}

		/*if( botoneraExterna!=null )
			m_comp.add( botoneraExterna );*/
		
		if(tableNavigation!=null){
			
			if(tableNavigation.hasNextRow() || tableNavigation.hasPrevRow()){
				JPanel botoneraNavigation= new JPanel();
				
				idOperation.setOperationType(OPERATION_SCROLL);
				
				idOperation.setButtonType(botoneraAccion.PREV);
				String idString=idOperation.getIdString();

				JButton prevButton=subBuildBoton(botoneraNavigation,null,"prev",/*"ACTION:"+id+":"+ idtoUserTask +":"+EDITAR+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("IR AL ANTERIOR"),list,(int)buttonWidth,(int)buttonHeight,lockable);
				if(!tableNavigation.hasPrevRow())
					prevButton.setEnabled(false);
				botones.add(prevButton);
				numButtons++;
				
				idOperation.setButtonType(botoneraAccion.NEXT);
				idString=idOperation.getIdString();

				JButton nextButton=subBuildBoton(botoneraNavigation,null,"next",/*"ACTION:"+id+":"+ idtoUserTask +":"+EDITAR+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("IR AL SIGUIENTE"),list,(int)buttonWidth,(int)buttonHeight,lockable);
				if(!tableNavigation.hasNextRow())
					nextButton.setEnabled(false);
				botones.add(nextButton);
				numButtons++;
				
				m_comp.add( botoneraNavigation );
			}
				
		}

		if(accessAdapter!=null){
			Iterator<Integer> itrViewAccess=accessAdapter.getUserTasksAccess(AccessAdapter.VIEW).iterator();
			Iterator<Integer> itrNewAccess=accessAdapter.getUserTasksAccess(AccessAdapter.NEW_AND_REL).iterator();
			Iterator<Integer> itrSetAccess=accessAdapter.getUserTasksAccess(AccessAdapter.SET).iterator();
			Iterator<Integer> itrDelAccess=accessAdapter.getUserTasksAccess(AccessAdapter.DEL_AND_UNREL).iterator();
			Iterator<Integer> itrUnrelAccess=accessAdapter.getUserTasksAccess(AccessAdapter.UNREL).iterator();
			Iterator<Integer> itrFindAccess=accessAdapter.getUserTasksAccess(AccessAdapter.FIND_AND_REL).iterator();

			idOperation.setOperationType(OPERATION_ACTION);
			
			Integer idtoUserTask;
			JButton buttonReportSearch=null;
			JButton buttonReportDirectPrint=null;
			JButton buttonEmail=null;
			if( /*myAccess.getViewAccess()*/itrViewAccess.hasNext()/* && TargetIsTable */){
				idOperation.setButtonType(CONSULTAR);
				//do{
					idtoUserTask=itrViewAccess.next();
					idOperation.getTarget().setIdtoUserTask(idtoUserTask);
					String idString=idOperation.getIdString();
					String nameButton=(name!=null?"ver@"+name:"ver");
					JButton button=subBuildBoton(getComponent(),null,"view",/*"ACTION:"+id+":"+ idtoUserTask +":"+CONSULTAR+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Ver detalle"),nameButton,list,(int)buttonWidth,(int)buttonHeight,lockable);
					botones.add(button);
					
					//if(formType==botoneraAccion.SEARCH_TYPE || formType==LINKING_TYPE){
						GConst.addShortCut(componentParentShortCut, button, GConst.VIEW_SHORTCUT_KEY, GConst.VIEW_SHORTCUT_MODIFIERS, "Consulta", whenShortCut, null);
					//}
					
					if(formType==SEARCH_TYPE){
						idOperation.setButtonType(PRINT_SEARCH);
						idString=idOperation.getIdString();
						nameButton=(name!=null?"guardarResultados@"+name:"guardarResultados");
						buttonReportSearch=subBuildBoton(/*getComponent()*/null,null,"save",/*"ACTION:"+id+":"+ idtoUserTask +":"+REPORT+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Guardar lista de resultados"),nameButton,/*list,*/(int)buttonWidth,(int)buttonHeight,lockable);
						
						final JButton buttonReportSearchAux=buttonReportSearch;
						if(idoReportFormats!=null && !idoReportFormats.isEmpty()){   
							final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
							
							Iterator<Integer> it = idoReportFormats.keySet().iterator();
							while(it.hasNext()){
								int ido = it.next();
								JMenuItem b = new JMenuItem(idoReportFormats.get(ido));
								b.setContentAreaFilled(false);
								b.setBorderPainted(false);
								b.addActionListener(list);

								idOperation.getTarget().setValue(ido);
								String idStringCreation=idOperation.getIdString();
								b.setActionCommand(idStringCreation);

								buttons.add(b);
							}
							buttonReportSearchAux.addActionListener(new ActionListener(){

								public void actionPerformed(ActionEvent e) {
									PopupOptions p=new PopupOptions(buttons, buttonReportSearchAux, server, list);
									p.show(true);
								}					
							});

						}else{
							buttonReportSearch.addActionListener(list);
						}
						
						
						if(idtoReportsDirectPrint!=null && !idtoReportsDirectPrint.isEmpty()){
							idOperation.setButtonType(PRINT);
							if(idtoReportsDirectPrint.size()==1)
								idOperation.getTarget().setIdtoUserTask(idtoReportsDirectPrint.keySet().iterator().next());
							idString=idOperation.getIdString();
							nameButton=(name!=null?"imprimir@"+name:"imprimir");
							buttonReportDirectPrint=subBuildBoton(/*getComponent()*/null,null,"print",/*"ACTION:"+id+":"+ idtoUserTask +":"+REPORT+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Imprimir"),nameButton,/*list,*/(int)buttonWidth,(int)buttonHeight,lockable);
							
							GConst.addShortCut(componentParentShortCut, buttonReportDirectPrint, GConst.PRINT_SHORTCUT_KEY, GConst.PRINT_SHORTCUT_MODIFIERS, "Imprimir", whenShortCut, null);
							
							final JButton buttonReportAux=buttonReportDirectPrint;
							if(idtoReportsDirectPrint.size()>1){   
								final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
								
								Iterator<Integer> it = idtoReportsDirectPrint.keySet().iterator();
								while(it.hasNext()){
									int idto = it.next();
									JMenuItem b = new JMenuItem(idtoReportsDirectPrint.get(idto));
									b.setContentAreaFilled(false);
									b.setBorderPainted(false);
									b.addActionListener(list);
	
									idOperation.getTarget().setIdtoUserTask(idto);
									String idStringCreation=idOperation.getIdString();
									b.setActionCommand(idStringCreation);
	
									buttons.add(b);
								}
								buttonReportAux.addActionListener(new ActionListener(){
	
									public void actionPerformed(ActionEvent e) {
										PopupOptions p=new PopupOptions(buttons, buttonReportAux, server, list);
										p.show(true);
									}					
								});
	
							}else{
								buttonReportDirectPrint.addActionListener(list);
							}
						}
						
						if(email){
							idOperation.setButtonType(EMAIL);
							idString=idOperation.getIdString();
							nameButton=(name!=null?"enviarEmail@"+name:"enviarEmail");
							buttonEmail=subBuildBoton(/*getComponent()*/null,null,"email",/*"ACTION:"+id+":"+ idtoUserTask +":"+REPORT+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Enviar por email"),nameButton,/*list,*/(int)buttonWidth,(int)buttonHeight,lockable);
							buttonEmail.addActionListener(list);
						}
						
					}
					
					numButtons++;
				//}while(itrViewAccess.hasNext());
			}

			if( /*((!popupContainer && modoBusqueda) || !modoBusqueda) &&*//*myAccess.getNewAccess()*/(itrNewAccess.hasNext() || (idtoNameCreationActions!=null && !idtoNameCreationActions.isEmpty())) /*&& TargetIsTable*/ && !modoConsultar ){				
				idOperation.setButtonType(CREAR);
				//do{
					String idString=null;
					boolean hasNewAccess=itrNewAccess.hasNext();
					if(hasNewAccess){
						idtoUserTask=itrNewAccess.next();
						idOperation.getTarget().setIdtoUserTask(idtoUserTask);
						idString=idOperation.getIdString();
					}
					String nameButton=(name!=null?"nuevo@"+name:"nuevo");
					final JButton button = subBuildBoton(getComponent(),null,"nuevo",idString,Utils.normalizeLabel("Nuevo"),nameButton,/*list,*/(int)buttonWidth,(int)buttonHeight,lockable);
					
					//if(formType==botoneraAccion.SEARCH_TYPE || formType==LINKING_TYPE){
					button.setMnemonic(GConst.NEW_SHORTCUT_KEY);
						GConst.addShortCut(componentParentShortCut, button, GConst.NEW_SHORTCUT_KEY, GConst.NEW_SHORTCUT_MODIFIERS, "Creacion", whenShortCut, null);
					//}
					
				//}while(itrNewAccess.hasNext());
					if(idtoNameCreationActions!=null && !idtoNameCreationActions.isEmpty()){   
						final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
						
						if(hasNewAccess){
							JMenuItem bNew = new JMenuItem(Utils.normalizeLabel("<Crear nuevo>"));
							bNew.setContentAreaFilled(false);
							bNew.setBorderPainted(false);
							bNew.addActionListener(list);
							bNew.setActionCommand(idString);
	
							buttons.add(bNew);
						}
						Iterator<Integer> it = idtoNameCreationActions.keySet().iterator();
						while(it.hasNext()){
							int idto = it.next();
							JMenuItem b = new JMenuItem(idtoNameCreationActions.get(idto));
							b.setContentAreaFilled(false);
							b.setBorderPainted(false);
							b.addActionListener(list);

							idOperation.getTarget().setIdtoUserTask(idto);
							idOperation.setButtonType(/*ACTION*/CREAR);
							String idStringCreation=idOperation.getIdString();
							b.setActionCommand(idStringCreation);

							buttons.add(b);
						}
						button.addActionListener(new ActionListener(){

							public void actionPerformed(ActionEvent e) {
								PopupOptions p=new PopupOptions(buttons, button, server, list);
								p.show(true);
							}					
						});

					}else{
						button.addActionListener(list);
						botones.add(button);
					}
					
					numButtons++;
			}
			if( /*((!popupContainer && modoBusqueda) || !modoBusqueda) &&*//*myAccess.getSetAccess()*/itrSetAccess.hasNext() /*&& TargetIsTable*/ && !modoConsultar ){
				//do{
					idtoUserTask=itrSetAccess.next();
					idOperation.getTarget().setIdtoUserTask(idtoUserTask);
					idOperation.setButtonType(EDITAR);
					String idString=idOperation.getIdString();
					String nameButton=(name!=null?"modificar@"+name:"modificar");
					m_botonEditar=subBuildBoton(getComponent(),null,"editar",/*"ACTION:"+id+":"+ idtoUserTask +":"+EDITAR+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Modificar"),nameButton,list,(int)buttonWidth,(int)buttonHeight,lockable);
					botones.add(m_botonEditar);
					
					//if(formType==botoneraAccion.SEARCH_TYPE || formType==LINKING_TYPE){
						GConst.addShortCut(componentParentShortCut, m_botonEditar, GConst.SET_SHORTCUT_KEY, GConst.SET_SHORTCUT_MODIFIERS, "Edicion", whenShortCut, null);
					//}
					
					numButtons++;
//					if(!popupContainer){
//					idOperation.setButtonType(REPORT);
//					idString=idOperation.getIdString();
//					botones.add(subBuildBoton(getComponent(),null,"report",/*"ACTION:"+id+":"+ idtoUserTask +":"+REPORT+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Informes"),list,21,21,lockable));
//					}
				//}while(itrSetAccess.hasNext());
			}

//			boolean botonDel=false, botonUnlink=false;
//			if( (	!popupContainer && modoBusqueda || TargetIsTable/* && !modoBusqueda*/ ||
//					popupContainer && !modoBusqueda && !TargetIsTable && !modoCreacion ) &&
//					!modoConsultar && /*myAccess.getDelAccess()*/itrDelAccess.hasNext() )
//				botonDel=true;
//			if( (	TargetIsTable && !modoBusqueda ||
//					popupContainer && !modoBusqueda && !TargetIsTable && !modoCreacion ) &&
//					!modoConsultar && /*myAccess.getUnrelAccess()*/itrUnrelAccess.hasNext() )
//				botonUnlink=true;
			
			boolean botonDel=false, botonUnlink=false;
			if(	!modoConsultar && /*myAccess.getDelAccess()*/itrDelAccess.hasNext() )
				botonDel=true;
			if(	formType==TABLE_TYPE &&
					!modoConsultar && /*myAccess.getUnrelAccess()*/itrUnrelAccess.hasNext() )
				botonUnlink=true;

			if( botonUnlink ){
				idOperation.setButtonType(UNLINK);
				//do{
					idtoUserTask=itrUnrelAccess.next();
					idOperation.getTarget().setIdtoUserTask(idtoUserTask);
					String idString=idOperation.getIdString();
					String nameButton=(name!=null?"desvincular@"+name:"desvincular");
					botones.add(subBuildBoton(getComponent(),null,"delete",/*"ACTION:"+id+":"+idtoUserTask+":"+UNLINK+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Desvincular"),nameButton,list,(int)buttonWidth,(int)buttonHeight,lockable));
					
					numButtons++;
				//}while(itrUnrelAccess.hasNext());
			}
			if( botonDel && !botonUnlink ){
				idOperation.setButtonType(ELIMINAR);
				//do{
					idtoUserTask=itrDelAccess.next();
					idOperation.getTarget().setIdtoUserTask(idtoUserTask);
					String idString=idOperation.getIdString();
					String nameButton=(name!=null?"eliminar@"+name:"eliminar");
					botones.add(subBuildBoton(getComponent(),null,"delete",/*"ACTION:"+id+":"+idtoUserTask+":"+ELIMINAR+(m_parentName!=null?":"+m_parentName:"")*/idString,Utils.normalizeLabel("Eliminar"),nameButton,list,(int)buttonWidth,(int)buttonHeight,lockable));
					
					numButtons++;
				//}while(itrDelAccess.hasNext());
			}

			if( formType==TABLE_TYPE && /*myAccess.getRelAccess()*/itrFindAccess.hasNext() && !modoConsultar ){
				
				idOperation.setButtonType(BUSCAR);
				//do{
					idtoUserTask=itrFindAccess.next();
					idOperation.getTarget().setIdtoUserTask(idtoUserTask);
					String idString=idOperation.getIdString();
					String nameButton=(name!=null?"buscar@"+name:"buscar");
					JButton button=subBuildBoton(getComponent(),null,"look",/*"ACTION:"+id+":"+idtoUserTask+":"+BUSCAR*/idString,Utils.normalizeLabel("Buscar"),nameButton,list,(int)buttonWidth,(int)buttonHeight,lockable);
					botones.add(button);
					
					GConst.addShortCut(componentParentShortCut, button, GConst.SEARCH_SHORTCUT_KEY, GConst.SEARCH_SHORTCUT_MODIFIERS, "Buscar", whenShortCut, null);
					
					numButtons++;
				//}while(itrRelAccess.hasNext());
			}
			
			if(allowChangeColumnProperties && (formType==TABLE_TYPE || formType==SEARCH_TYPE)){
				idOperation.setButtonType(CONFIG_COLUMNPROPERTIES);

					idOperation.getTarget().setIdtoUserTask(null);
					String idString=idOperation.getIdString();
					String nameButton=(name!=null?"configurar@"+name:"configurar");
					JButton button=subBuildBoton(getComponent(),null,"configtable",/*"ACTION:"+id+":"+idtoUserTask+":"+BUSCAR*/idString,Utils.normalizeLabel("Configurar tabla"),nameButton,list,(int)buttonWidth,(int)buttonHeight,lockable);
					botones.add(button);
					
					numButtons++;
			}

			if(buttonReportDirectPrint!=null){
				// Lo hacemos aqui para que aparezca al final de todas las operaciones normales y antes de las especiales
				getComponent().add(buttonReportDirectPrint);
				botones.add(buttonReportDirectPrint);
				
				numButtons++;
			}
			
			if(buttonReportSearch!=null){
				// Lo hacemos aqui para que aparezca al final de todas las operaciones normales y antes de las especiales
				getComponent().add(buttonReportSearch);
				botones.add(buttonReportSearch);
				
				numButtons++;
			}
			
			if(buttonEmail!=null){
				// Lo hacemos aqui para que aparezca al final de todas las operaciones normales y antes de las especiales
				getComponent().add(buttonEmail);
				botones.add(buttonEmail);
				
				numButtons++;
			}
			
			if( formType==SEARCH_TYPE){
				idOperation.setButtonType(HELP);
				String idString=idOperation.getIdString();
				String nameButton=(name!=null?"ayuda@"+name:"ayuda");
				botones.add(subBuildBoton(getComponent(),null,"help",/*"ACTION:"+id+":"+ idtoUserTask +":"+CANCEL*/idString, Utils.normalizeLabel("Consultar ayuda"), nameButton, list,(int)buttonWidth,(int)buttonHeight,lockable ));
			
				numButtons++;
			}

			if(idtoReports!=null && !idtoReports.isEmpty()){
				final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
				Iterator<Integer> it = idtoReports.keySet().iterator();
				while(it.hasNext()){
					int idto = it.next();
					JMenuItem b = new JMenuItem(idtoReports.get(idto));
					b.setContentAreaFilled(false);
					b.setBorderPainted(false);
					b.addActionListener(list);

					idOperation.getTarget().setIdtoUserTask(idto);
					idOperation.setButtonType(REPORT);
					String idString=idOperation.getIdString();
					b.setActionCommand(idString);

					buttons.add(b);					
				}			

				IdObjectForm idObjectOperation=new IdObjectForm();
				String idString=idObjectOperation.getIdString();
				final JButton button = subBuildBoton(getComponent(), /*null*/Utils.normalizeLabel("Informes"), /*"report"*/null, idString, Utils.normalizeLabel("Informes"), 0, (int)buttonHeight, lockable);
				button.setMnemonic(GConst.REPORTS_SHORTCUT_KEY);
				GConst.addShortCut(componentParentShortCut, button, GConst.REPORTS_SHORTCUT_KEY, GConst.REPORTS_SHORTCUT_MODIFIERS, "Informes", whenShortCut, null);
				
				button.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						PopupOptions p=new PopupOptions(buttons, button, server, list);
						p.show(true);
					}					
				});
				//botones.add(button);

			}
			if(idtoNameActions!=null && !idtoNameActions.isEmpty()){   
				if(idtoNameActions.size()==1){
					int idto=idtoNameActions.keySet().iterator().next();
					idOperation.getTarget().setIdtoUserTask(idto);
					idOperation.setButtonType(ACTION);
					String idString=idOperation.getIdString();
					String actionName=Utils.normalizeLabel(idtoNameActions.get(idto));
					
					/*JButton button=*/subBuildBoton(getComponent(), /*null*/actionName, /*"action"*/null, idString, actionName, list, 0, (int)buttonHeight, lockable);
					
				}else{
					final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
					Iterator<Integer> it = idtoNameActions.keySet().iterator();
					while(it.hasNext()){
						int idto = it.next();
						JMenuItem b = new JMenuItem(idtoNameActions.get(idto));
						b.setContentAreaFilled(false);
						b.setBorderPainted(false);
						b.addActionListener(list);
	
						idOperation.getTarget().setIdtoUserTask(idto);
						idOperation.setButtonType(ACTION);
						String idString=idOperation.getIdString();
						b.setActionCommand(idString);
	
						buttons.add(b);
					}
					IdObjectForm idObjectOperation=new IdObjectForm();
					String idString=idObjectOperation.getIdString();
					final JButton button = subBuildBoton(getComponent(), /*null*/Utils.normalizeLabel("Acciones"), /*"action"*/null, idString, Utils.normalizeLabel("Acciones"), 0, (int)buttonHeight, lockable);
					button.setMnemonic(GConst.ACTIONS_SHORTCUT_KEY);
					GConst.addShortCut(componentParentShortCut, button, GConst.ACTIONS_SHORTCUT_KEY, GConst.ACTIONS_SHORTCUT_MODIFIERS, "Acciones", whenShortCut, null);
					
					button.addActionListener(new ActionListener(){
	
						public void actionPerformed(ActionEvent e) {
							PopupOptions p=new PopupOptions(buttons, button, server, list);
							p.show(true);
						}					
					});
				}
				//botones.add(button);

			}
			if(idtoImports!=null && !idtoImports.isEmpty()){   
				final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
				Iterator<Integer> it = idtoImports.keySet().iterator();
				while(it.hasNext()){
					int idto = it.next();
					JMenuItem b = new JMenuItem(idtoImports.get(idto));
					b.setContentAreaFilled(false);
					b.setBorderPainted(false);
					b.addActionListener(list);

					idOperation.getTarget().setIdtoUserTask(idto);
					idOperation.setButtonType(IMPORT);
					String idString=idOperation.getIdString();
					b.setActionCommand(idString);

					buttons.add(b);
				}
				IdObjectForm idObjectOperation=new IdObjectForm();
				String idString=idObjectOperation.getIdString();
				final JButton button = subBuildBoton(getComponent(), /*null*/Utils.normalizeLabel("Importar"), /*"action"*/null, idString, Utils.normalizeLabel("Importar"), 0, (int)buttonHeight, lockable);
				button.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						PopupOptions p=new PopupOptions(buttons, button, server, list);
						p.show(true);
					}					
				});
				//botones.add(button);

			}
			if(idtoExports!=null && !idtoExports.isEmpty()){   
				final ArrayList<JMenuItem> buttons = new ArrayList<JMenuItem>();
				Iterator<Integer> it = idtoExports.keySet().iterator();
				while(it.hasNext()){
					int idto = it.next();
					JMenuItem b = new JMenuItem(idtoExports.get(idto));
					b.setContentAreaFilled(false);
					b.setBorderPainted(false);
					b.addActionListener(list);

					idOperation.getTarget().setIdtoUserTask(idto);
					idOperation.setButtonType(EXPORT);
					String idString=idOperation.getIdString();
					b.setActionCommand(idString);

					buttons.add(b);
				}
				IdObjectForm idObjectOperation=new IdObjectForm();
				String idString=idObjectOperation.getIdString();
				final JButton button = subBuildBoton(getComponent(), /*null*/Utils.normalizeLabel("Exportar"), /*"action"*/null, idString, Utils.normalizeLabel("Exportar"), 0, (int)buttonHeight, lockable);
				button.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						PopupOptions p=new PopupOptions(buttons, button, server, list);
						p.show(true);
					}					
				});
				//botones.add(button);

			}
		}
		if( botoneraExternaFin!=null )
			m_comp.add( botoneraExternaFin );
		/*int anchoTotal= getPreferredSize().getWidth();
	setMinimumSize( new Dimension( anchoTotal, 25 ) );
	setPreferredSize( new Dimension( anchoTotal, 25 ) );*/
	}

	public JPanel getComponent(){ return m_comp; }

	public void addListener( ActionListener list ){
		for( int i=0;i<botones.size();i++)
			botones.get(i).addActionListener(list);
	}

	public void setEnabled( int idBoton, boolean enabled ){
		if( idBoton==EDITAR && m_botonEditar!=null )
			m_botonEditar.setEnabled( enabled );
	}

	public boolean getEnabled( int idBoton ){
		if( idBoton==EDITAR && m_botonEditar!=null )
			return m_botonEditar.isEnabled();
		return false;
	}
	public static double getButtonHeight(Graphics graphics){
		return GViewBalancer.getRowHeightS(graphics);
	}

	public JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			ActionListener list,
			int ancho, int alto,boolean lockable){
		return subBuildBoton(panel, label, icon, command, tip, list, ancho, alto, lockable, server);
	}
	
	public JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			String name,
			ActionListener list,
			int ancho, int alto,boolean lockable){
		return subBuildBoton(panel, label, icon, command, tip, name, list, ancho, alto, lockable, server);
	}
		
	public static JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			String name,
			ActionListener list,
			int ancho, int alto,boolean lockable, docServer server ){
		JButton boton= subBuildBoton( panel,label,icon,command,tip,name,ancho,alto,lockable,server);
		if( list!=null ) boton.addActionListener(list);
		return boton;
	}
	
	public static JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			ActionListener list,
			int ancho, int alto,boolean lockable, docServer server ){
		JButton boton= subBuildBoton( panel,label,icon,command,tip,ancho,alto,lockable,server);
		if( list!=null ) boton.addActionListener(list);
		return boton;
	}
	public JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			ActionListener list,
			boolean lockable ){
		return subBuildBoton( panel,label,icon,command,tip,list,0,0,lockable,server);
	}
	
	public static JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			ActionListener list,
			boolean lockable, docServer server ){
		return subBuildBoton( panel,label,icon,command,tip,list,0,0,lockable,server);
	}

	/* incluyeScrollObj son las flechas de avanzar y retroceder que controla el contenedor del formulario*/
	public static int numeroBotonesTabla(AccessAdapter accessAdapter,
			boolean TargetIsTable,
			boolean modoCreacion,
			boolean incluyeScrollObj,
			boolean popup,
			boolean allowedConfigTable){
		/*boolean relAccess 	= access!= null && access.indexOf("RREL")>=0;
	boolean unrelAccess 	= access!= null && access.indexOf("UNREL")>=0;
	boolean newAccess 	= access!= null && access.indexOf("NEW")>=0;
	boolean delAccess 	= access!= null && access.indexOf("DEL")>=0;
	boolean viewAccess 	= access!= null && access.indexOf("VIEW")>=0;
	boolean setAccess 	= access!= null && access.indexOf("SET")>=0;*/
		/*access accessAux=new access(access);
   	boolean relAccess 	= accessAux.getRelAccess();
	boolean unrelAccess 	= accessAux.getUnrelAccess();
	boolean newAccess 	= accessAux.getNewAccess();
	boolean delAccess 	= accessAux.getDelAccess();
	boolean viewAccess 	= accessAux.getViewAccess();
	boolean setAccess 	= accessAux.getSetAccess();*/

		/*boolean relAccess 	= operations.getOperation(access.REL).iterator().hasNext();
	boolean unrelAccess = operations.getOperation(access.UNREL).iterator().hasNext();
	boolean newAccess 	= operations.getOperation(access.NEW).iterator().hasNext();
	boolean delAccess 	= operations.getOperation(access.DEL).iterator().hasNext();
	boolean viewAccess 	= operations.getOperation(access.VIEW).iterator().hasNext();
	boolean setAccess 	= operations.getOperation(access.SET).iterator().hasNext();*/

//		boolean relAccess 	= false;
//		boolean unrelAccess = false;
//		boolean newAccess 	= false;
//		boolean delAccess 	= false;
//		boolean viewAccess 	= false;
//		boolean setAccess = false;
//		Iterator<Integer> itrArrayAccess=accessUserTasks.keySet().iterator();
//		while(itrArrayAccess.hasNext()){
//		int idtoUserTask=itrArrayAccess.next();
//		ArrayList<UserAccess> arrayAccess=accessUserTasks.get(idtoUserTask);
//		Iterator<UserAccess> itrAccess=arrayAccess.iterator();
//		while(itrAccess.hasNext()){
//		UserAccess access=itrAccess.next();
//		if(access.getAccess().getRelAccess())
//		relAccess=true;
//		if(access.getAccess().getUnrelAccess())
//		unrelAccess=true;
//		if(access.getAccess().getNewAccess())
//		newAccess=true;
//		if(access.getAccess().getDelAccess())
//		delAccess=true;
//		if(access.getAccess().getViewAccess())
//		viewAccess=true;
//		if(access.getAccess().getSetAccess())
//		setAccess=true;
//		}
//		}


		// DESCOMENTAR CUANDO HAYA SOLO UN BOTON POR ACCION(POR EJEMPLO BOTON CREAR CON DESPLEGABLE,NO DOS BOTONES CREAR)
//		boolean relAccess 	= false;
//		boolean unrelAccess = false;
//		boolean newAccess 	= false;
//		boolean delAccess 	= false;
//		boolean viewAccess 	= false;
//		boolean setAccess = false;

//		Iterator<Integer> itr=accessAdapter.getUserTasksAccess(AccessAdapter.VIEW).iterator();
//		if(itr.hasNext())
//		viewAccess=true;
//		itr=accessAdapter.getUserTasksAccess(AccessAdapter.SET).iterator();
//		if(itr.hasNext())
//		setAccess=true;
//		itr=accessAdapter.getUserTasksAccess(AccessAdapter.NEW_AND_REL).iterator();
//		if(itr.hasNext())
//		newAccess=true;
//		itr=accessAdapter.getUserTasksAccess(AccessAdapter.DEL_AND_UNREL).iterator();
//		if(itr.hasNext())
//		delAccess=true;
//		itr=accessAdapter.getUserTasksAccess(AccessAdapter.REL).iterator();
//		if(itr.hasNext())
//		relAccess=true;
//		itr=accessAdapter.getUserTasksAccess(AccessAdapter.UNREL).iterator();
//		if(itr.hasNext())
//		unrelAccess=true;

//		int numBot=0;
//		if( TargetIsTable ){
//		// Cuando es setAcess-->Si es popup se añade solo un boton: SET. Si no es popup se añaden dos:SET y REPORT
//		numBot+= (viewAccess ? 1:0)+(newAccess ? 1:0)+(delAccess || unrelAccess ? 1:0)+ (setAccess ? popup?1:2 :0) + (relAccess ? 1:0);
//		/*else
//		numBot+=(relAccess ? 1:0);*/
//		}else
//		numBot+=(modoCreacion ? 1:( (incluyeScrollObj ? 2:0)+(delAccess || unrelAccess ? 1:0)));

		int relAccess=accessAdapter.getUserTasksAccess(AccessAdapter.FIND_AND_REL).size();;
		int unrelAccess=accessAdapter.getUserTasksAccess(AccessAdapter.UNREL).size();;
		int newAccess=accessAdapter.getUserTasksAccess(AccessAdapter.NEW_AND_REL).size();;
		int delAccess=accessAdapter.getUserTasksAccess(AccessAdapter.DEL_AND_UNREL).size();;
		int viewAccess=accessAdapter.getUserTasksAccess(AccessAdapter.VIEW).size();
		int setAccess=accessAdapter.getUserTasksAccess(AccessAdapter.SET).size();;

		int numBot=0;
		if( TargetIsTable ){
			// Cuando es setAcess-->Si es popup se añade solo un boton: SET. Si no es popup se añaden dos:SET y REPORT
			numBot+= viewAccess+newAccess+delAccess+unrelAccess+ (setAccess>0 ? popup?setAccess:setAccess+1:0) + relAccess + (allowedConfigTable ? 1:0);
			/*else
			numBot+=(relAccess ? 1:0);*/
		}else
			numBot+=(modoCreacion ? 1:( (incluyeScrollObj ? 2:0)+(delAccess>0 || unrelAccess>0 ? 1:0)));

		return 	numBot;
	}

	/*public static JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			boolean lockable){
		return subBuildBoton(panel,label,icon,command,tip,0,0,lockable);
	}*/

	public JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			int ancho,
			int alto,
			boolean lockable){
		return subBuildBoton(panel, label, icon, command, tip, ancho, alto, lockable, server);
	}
	
	public JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			String name,
			int ancho,
			int alto,
			boolean lockable){
		return subBuildBoton(panel, label, icon, command, tip, name, ancho, alto, lockable, server);
	}
	
	public static JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			int ancho,
			int alto,
			boolean lockable,docServer server){
		
		String name=(icon==null?label:tip);
		return subBuildBoton(panel, label, icon, command, tip, name, ancho, alto, lockable, server);
		
	}
	public static JButton subBuildBoton( JPanel panel,
			String label,
			String icon,
			String command,
			String tip,
			String name,
			int ancho,
			int alto,
			boolean lockable,docServer server){
		final JButton boton= new JButton(){

			@Override
			protected void fireActionPerformed(ActionEvent aE) {
				if(hasFocus())//Esto lo hacemos porque a partir de la version 6.0.18 al evitar que el boton quite el foco a otro componente(filas incompletas de las tablas) sigue ejecutando el actionPerformed del boton
					super.fireActionPerformed(aE);
			}
			
		};
		boton.setMargin(new Insets(0,0,0,0));
		if( panel!=null )
			panel.add( boton );
		if( tip!=null ) boton.setToolTipText(tip);

		if(command!=null)
			boton.setActionCommand(command);

		if( icon==null ){
			boton.setText(label);
			boton.setPreferredSize(new Dimension(ancho>0?ancho:boton.getPreferredSize().width, alto));
			//boton.setName(label);
		}
		else{
			int borderWidth=boton.getInsets().left+boton.getInsets().right;
			int borderHeight=boton.getInsets().top+boton.getInsets().bottom;

			ImageIcon img= server.getIcon(boton,icon, ancho==0?ancho:ancho-borderWidth, alto==0?alto:alto-borderHeight);
			/*if(ancho==0 && alto==0 && img.getWidth(boton)==img.getHeight(boton))
			img=img.getScaledInstance(17,17,Image.SCALE_SMOOTH);*/
			//System.err.println("Size botones botoneraAccion:"+(ancho==0?ancho:ancho-borderWidth)+( alto==0?alto:alto-borderHeight));
			//if(img!=null)
			boton.setIcon( img );
			boton.setMargin(new Insets(0,0,0,0));
			//boton.setName(tip);
		}
		boton.setName(name);

		/*if(lockable){
            guiLock gl = Singleton.getGuiLockInstance();
            gl.addComponent(boton);
        }
		 */
//		boton.addFocusListener(new FocusListener(){
//
//			public void focusGained(FocusEvent ev){
//				//if(!m_modoConsulta && !m_modoFilter)
//					boton.setBorder(GConfigView.borderSelected);
//			}
//			public void focusLost(FocusEvent ev){
//				//System.err.println("FocusLost TextCellEditor component:"+(ev.getComponent()!=null?ev.getComponent().getClass():null)+" opposite:"+(ev.getOppositeComponent()!=null?ev.getOppositeComponent().getClass():null)+" temporary:"+ev.isTemporary());
//				//System.err.println("FocusLost:"+mustStop);
//				if(!ev.isTemporary()){					
//					boton.setBorder(UIManager.getBorder("Button.border"));
//					boton.setMargin(new Insets(0,0,0,0));
//				}
//			}
//			
//		});
		return boton;
	}

	public int getNumButtons(){
		return numButtons;
	}


	public ArrayList<AbstractButton> getBotones() {
		return botones;
	}

}
