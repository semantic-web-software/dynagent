package dynagent.gui;

import gdev.gawt.utils.botoneraAccion;
import gdev.gen.AssignValueException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import javax.naming.NamingException;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.communication.communicator;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.gui.forms.FormControl;
import dynagent.gui.forms.transitionControl;
import dynagent.gui.utils.DemandPooler;
import dynagent.gui.utils.Target;

public class MenuControl{

	private communicator m_com=null;
	/*private monitor m_mon;
	/*private ITaskCenter m_taskCenter;*/
	private WindowComponent m_dialog;
	//private GestorInterfaz gestorInterfaz;
	private HashMap<Integer, OperationGUI> m_listaGUI;
	private Integer currentTarget;
	private Integer currentOperationGUI;
	private HashSet<Integer> pendingListener;
	private HashMap<Integer,HashSet<Integer>> mapListenerAreaFuncional;
	private KnowledgeBaseAdapter kba;
	
	
	public MenuControl(/*ITaskCenter taskCenter,*/KnowledgeBaseAdapter kba,WindowComponent dialog) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, AssignValueException{
		m_com=Singleton.getInstance().getComm();
		/*m_taskCenter=taskCenter;
		 */m_dialog=dialog;
		 //gestorInterfaz=Singleton.getInstance().getGestorInterfaz();
		 m_listaGUI= new HashMap<Integer, OperationGUI>();
		 pendingListener=new HashSet<Integer>();
		 mapListenerAreaFuncional=new HashMap<Integer, HashSet<Integer>>();
		 this.kba=kba;
		 build();
		 
	}

	private void build() throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, AssignValueException{
		GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);

		Iterator itr=kba.getAreasFuncionales().iterator();
		/*Iterator itr=m_md.m_areasFuncionales.keySet().iterator();*/
		//Iterator itr=new java.util.LinkedHashMap(m_md.m_areasFuncionales).keySet().iterator();

		final HashMap<String, Integer> hm = new HashMap<String, Integer>();
		boolean onlyOneFunctionalArea=false;
		boolean someFunctionalArea=false;
//		Si vemos que realmente no tenemos que hacer nada si no hay areas funcionales ponemos un while en lugar del dowhile y quitamos el if
		if(itr.hasNext()){
			ArrayList<String> labels = new ArrayList<String>();
			while(itr.hasNext()){
				int id=(Integer)itr.next();
				if(kba.hasUserTasks(id,null)){
					String label=kba.getLabelAreaFuncional(id);
					hm.put(label, id);
					labels.add(label);
				}
			}
			onlyOneFunctionalArea=labels.size()==1?true:false;
			Collections.sort(labels, new Comparator<String>(){

				@Override
				public int compare(String s1, String s2) {
					return Constants.languageCollator.compare(s1, s2);
				}
				
			});
			Iterator<String> itl = labels.iterator();
			while(itl.hasNext()){
				String label = itl.next();
				int id=hm.get(label);
				/*String id=(String)itr.next();
				String label= m_md.getAreaFuncionalLabel( id );*/
				//System.out.println("ID "+id+" LABEL "+label);
				ImageIcon icon= m_com.getIcon(label.replace(' ', '_'));
				gestor.addItem( String.valueOf(id), label, label, icon.getIconWidth()>0?icon:null, null, null);

				/*ImageIcon  icono=null;
				Image img= m_com.getImage(label);
				if(img!=null){
					icono= new ImageIcon(img);
					gestor.addItem(String.valueOf(id), null, Utils.normalizeLabel(label), icono.getIconWidth()>0?icono:null, null, null);
				}else{
					gestor.addItem(String.valueOf(id), null, Utils.normalizeLabel(label), null, null, null);
				}*/

				IdOperationForm idOperation=new IdOperationForm();
				idOperation.setOperationType(botoneraAccion.OPERATION_ACTION);
				idOperation.setButtonType(botoneraAccion.LAUNCH);

				IdObjectForm idTarget=new IdObjectForm();
				idTarget.setIdo(id);
				idOperation.setTarget(idTarget);
				String idStringOperation=idOperation.getIdString();

				final boolean thisOnlyFunctionalArea=onlyOneFunctionalArea;
				final int idAreaFuncional=id;
				gestor.setEventoItem(String.valueOf(id), /*"ACTION:"+id+":"+botoneraAccion.LAUNCH*/idStringOperation, new ActionListener(){

					public void actionPerformed(ActionEvent arg0) {
						clickDespliegueAreaFuncional(idAreaFuncional,thisOnlyFunctionalArea);
					}
					
				});
				someFunctionalArea=true;
				//gestor.setEventoItem(String.valueOf(id), ""+id,new HelpComponent());
			}
			
			Iterator<Integer> itrUTask=kba.getListenerUtasks();
			while(itrUTask.hasNext()){
				int idtoUserTask=itrUTask.next();
				ListenerUtask listenerUtask=kba.getListenerUtask(idtoUserTask);
				int idoUserTask=kba.getIdoUserTask(idtoUserTask);
				//if(kba.isListenerMenu(idtoUserTask)){
					ObjectProperty property=kba.getTarget(idoUserTask,idtoUserTask,null,kba.getDefaultSession());//DefaultSession ya que es informacion de inicializacion
					kba.createRange(idoUserTask,kba.getIdtoUserTask(idoUserTask), Constants.IdPROP_TARGETCLASS, kba.getIdRange(property), null, idtoUserTask, Constants.MAX_DEPTH_SEARCH_FILTERS, kba.getDefaultSession());
					property=kba.getTarget(idoUserTask,idtoUserTask, null,kba.getDefaultSession());//DefaultSession ya que es informacion de inicializacion
					int idRange=property.getRangoList().getFirst();
					//if(kba.isSpecialized(kba.getClass(idRange), Constants.IDTO_SOLICITUD))
						Integer updatePeriod=listenerUtask.getUpdatePeriod();
						if(listenerUtask.getUpdatePeriod()!=null)
							updatePeriod=updatePeriod*60*Constants.TIMEMILLIS;
						else updatePeriod=200000;
						new DemandPooler(idRange,idtoUserTask,updatePeriod,kba,this);
				//}
			}
		}
		
		if(someFunctionalArea){
			gestor.setVisibleItems(null, true);
			
			if(onlyOneFunctionalArea){//Si solo hay un area funcional la desplegamos.
				//gestor.setVisibleItem(String.valueOf(hm.values().toArray()[0]),true);//Esto llamará al actionPerformed que acaba llamando a clickDespliegueAreaFuncional. Quitado ya que hace que no funcione bien el anterior-siguiente principal
				SwingUtilities.invokeLater(new Runnable() {
					//Lo hacemos en un invokeLater para evitar que al desplegar el area funcional se llame a process de QuestionTaskManager antes de que la aplicacion este completamente inicializada
					@Override
					public void run() {
						clickDespliegueAreaFuncional((Integer)hm.values().toArray()[0],true);	
					}
				});
			}
			
			GConst.addShortCut(null, gestor.getComponente(), GConst.MENU_SHORTCUT_KEY, GConst.MENU_SHORTCUT_MODIFIERS, "Menu", JComponent.WHEN_IN_FOCUSED_WINDOW, null);
		}
	}
	
	public boolean requestFocusInWindow(){
		GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
		
		return gestor.getComponente().requestFocusInWindow();
	}

	public void clickDespliegueAreaFuncional(final int idAreaFuncional,boolean selectFirst){
		try{
			m_dialog.disabledEvents();
			//m_dialog.getContentPane().removeAll();//Por si antes estabamos con el panel de las tareas del monitor
			//gestorInterfaz.addZona(GestorInterfaz.ZONA_MODULOS);
			//gestorInterfaz.addZona(GestorInterfaz.ZONA_MENU);
			//gestorInterfaz.addZona(GestorInterfaz.ZONA_ESTADO);
			//gestorInterfaz.removeZona(GestorInterfaz.ZONA_TRABAJO);
			if (/*id !=null && */m_listaGUI.containsKey(idAreaFuncional)) {
//				/*m_gui = (JComponent) m_listaGUI.get(subtype);*/
//				OperationGUI m_gui = (OperationGUI) m_listaGUI.get(id);
//				//Singleton.getInstance().setCurrentOperationGUI((OperationGUI)m_gui);
//				//GestorContenedor gestorMenu=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
//				//gestorMenu.setVisibleItems(String.valueOf(id), true);
//				GestorContenedor gestorTrabajo=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_TRABAJO);
//				Integer idCurrTargetFilter=((OperationGUI)m_gui).getIdCurrTarget();
//				if(idCurrTargetFilter!=null)
//					if(gestorTrabajo.setVisiblePanels("Conjunto:"+idCurrTargetFilter, true)){
//						gestorInterfaz.removeZona(GestorInterfaz.ZONA_TRABAJO);
//						gestorInterfaz.addZona(GestorInterfaz.ZONA_TRABAJO);
//					}
			} else {
				// El gestor zona Menu se utiliza dentro de operationGUI al crear los Target
				final OperationGUI m_gui = new OperationGUI(kba, m_dialog, (Integer)idAreaFuncional);
				Iterator<Target> itrTarget=m_gui.getTargets().iterator();
				ArrayList<Target> targets = new ArrayList<Target>();
				while(itrTarget.hasNext()){
					targets.add(itrTarget.next());
				}
				Collections.sort(targets);
				
				final GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
				Iterator<Target> itt = targets.iterator();		
				while(itt.hasNext()){
					Target tg=itt.next();
					if( tg!=null/* && tg.icono!=null*/ ){
						Integer id=tg.id;
						String label=tg.label;
						//Image img=tg.icono;
						/*Quizas haya que pensar en una propiedad mejor que nos sirva para saber si el item va en Configuracion o en Operaciones*/

						//String normalizedLabel=Utils.normalizeLabel(label);
						ImageIcon imgIcon=tg.icono;//img!=null?new ImageIcon(img):null;
//						TabConfig tb=tbc.getTabByIdtoClass(m_kba.getClass(id));
//						if(tb==null){
//							
//							tbNames=tbc.getAllTabName();
//							if (!tbNames.isEmpty()){
//								gestor.addItem(String.valueOf(id), /*normalizedLabel*/label, /*normalizedLabel*/label, imgIcon!=null && imgIcon.getIconWidth()>0?imgIcon:null, conjunto+tbNames.get(0), conjunto);
//							}else{
//								gestor.addItem(String.valueOf(id), /*normalizedLabel*/label, /*normalizedLabel*/label, imgIcon!=null && imgIcon.getIconWidth()>0?imgIcon:null, conjunto+"-Default", conjunto);
//							}
//						}else{
//							gestor.addItem(String.valueOf(id), /*normalizedLabel*/label, /*normalizedLabel*/label, imgIcon!=null && imgIcon.getIconWidth()>0?imgIcon:null, conjunto+tb.getType(), conjunto);
//						}
							
						gestor.addItem(String.valueOf(id), /*normalizedLabel*/label, /*normalizedLabel*/label, imgIcon!=null && imgIcon.getIconWidth()>0?imgIcon:null, String.valueOf(idAreaFuncional), null);
						
//						if(label.equals("Solicitud Traspaso Almacenes"))
//							gestor.setHighlightedItem(String.valueOf(id), true, String.valueOf(m_areaFuncional));
						/*if(!isProcess(tg.ctx))
						   gestor.addItem(tg.label, tg.label, tg.label, new ImageIcon(tg.icono), conjunto+"-Config", conjunto);
					   else gestor.addItem(tg.label, tg.label, tg.label, new ImageIcon(tg.icono), conjunto+"-Operac", conjunto);*/

						//gestor.setVisibleItem("1", true);
						/*gestor.setEventoItem(tg.label, ""+tg.ctx.id,new ActionListener() {*/
						gestor.setEventoItem(String.valueOf(id), ""+id.intValue(),new ActionListener() {
							public void actionPerformed(final ActionEvent e) {
								
								//No lo procesamos hasta que no se haya construido el submenu entero ya que es un problema para las pruebas si se pulsa un boton mientras se esta construyendo el resto
								if(!m_listaGUI.containsKey(idAreaFuncional)){
									SwingUtilities.invokeLater(new Runnable(){

										public void run() {
											actionPerformed(e);
										}
										
									});
								}else{
									try{
										//System.err.println("Pulsa boton "+e.getSource());
										//System.err.println("Tienen glassPane:"+m_dialog.getGlassPane().isVisible());
										m_dialog.disabledEvents();
										//System.err.println("Despues Deshabilita");
										
										String id=e.getActionCommand();
										
										if(Auxiliar.equals(String.valueOf(currentTarget), id))
											return;
										
										if(currentTarget!=null){
											Target target=m_listaGUI.get(currentOperationGUI).m_targets.get(currentTarget);
											FormControl form=target.form;
											//Si el actual se trata de un formulario incrustado y tiene modificaciones hechas por el usuario
											//no permitimos el cambio a otro formulario hasta que acabe con el actual
											if(form instanceof transitionControl){
												if(((transitionControl)form).hasUserModified()){
											
													Singleton.getInstance().getMessagesControl().showErrorMessage(Utils.normalizeMessage("No está permitido cambiar de formulario sin aceptar o cancelar el actual"),m_dialog.getComponent());
													SwingUtilities.invokeLater(new Runnable(){
	
														public void run() {
															gestor.setVisibleItem(String.valueOf(currentTarget), true);
														}
														
													});
													return;
												}else{
													int idtoUTask=form.getIdtoUserTask();
													boolean oldForceParent=form.getSession().isForceParent();
													boolean success=false;
													boolean cancelled=false;
													try{
														form.getSession().setForceParent(false);
														cancelled=form.cancel();
														success=true;
													}finally{
														if(!success){
															form.getSession().setForceParent(oldForceParent);
														}
													}
													if(cancelled){
														//replaceCreationForm(tgf,idtoUTask,userRol);
														target.form=null;//Lo ponemos a null que significa en operationGui.showTarget que creara un formulario incrustado la proxima vez que intente entrar en este menu
													}else{
														form.getSession().setForceParent(oldForceParent);
													}
												}
											}
										}

										Object boton=e.getSource();
										if(boton instanceof AbstractButton){
											Singleton.getInstance().getStatusBar().setLocalizacion(kba.getLabelAreaFuncional(idAreaFuncional),0);
											Singleton.getInstance().getStatusBar().setLocalizacion(((AbstractButton)boton).getToolTipText(),1);
										}
											
										boolean showed=m_gui.showTarget(new Integer(id));
										if(showed){
											Singleton.getInstance().getNavigation().setActualPage(/*"Conjunto:"+id*/String.valueOf(id));
											
											currentTarget=new Integer(id);
											currentOperationGUI=idAreaFuncional;
										}else{
											SwingUtilities.invokeLater(new Runnable(){

												@Override
												public void run() {
													gestor.setVisibleItem(String.valueOf(currentTarget),true);
												}
												
											});
											
										}
									}catch(Exception ex){
										ex.printStackTrace();
										m_com.logError(m_dialog.getComponent(),ex, "Error al cargar el objeto del menú");
									}finally{
										//System.err.println("Antes Habilita");
										m_dialog.enabledEvents();
									}
								}
							}
						});
						
						//gestor.setEventoItem(String.valueOf(id), ""+id.intValue(),new HelpComponent());
						if(kba.isListenerMenu(tg.idtoUserTask)){
							Color color=ConstantesGraficas.highlightedColor;
							if(kba.getListenerUtask(tg.idtoUserTask).getRgb()!=null)
								color=new Color(kba.getListenerUtask(tg.idtoUserTask).getRgb());
							gestor.setHighlightedItemColor(String.valueOf(id), color, String.valueOf(idAreaFuncional));
						}
						
						if(pendingListener.contains(tg.idtoUserTask)){
							gestor.setHighlightedItem(String.valueOf(id), true, String.valueOf(idAreaFuncional));
							pendingListener.remove(tg.idtoUserTask);
						}
					}
				}
				
				if(selectFirst){//Hacemos click sobre el primer subboton de la lista
					gestor.setVisibleItem(String.valueOf(targets.get(0).id),true);
				}
				//gestor.setVisibleItems(conjunto, true);
				m_listaGUI.put(idAreaFuncional, m_gui);
				//Singleton.getInstance().setCurrentOperationGUI((OperationGUI)m_gui);
			}
			
			m_dialog.getComponent().validate();
			m_dialog.getComponent().repaint();
			
			//kba.getSizeMotor();
		}catch(Exception ex){
			ex.printStackTrace();
			m_com.logError(m_dialog.getComponent(),ex, "Error al cargar los objetos del menu");
		}finally{
			m_dialog.enabledEvents();
		}
	}

	
	public void notifyNewEvent(int idTarget,int idtoUserTask,boolean show) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
			
		GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
		
		int idoUserTask=kba.getIdoUserTask(idtoUserTask);
					
		Iterator<Integer> itr=kba.getAreasFuncionales(idoUserTask).iterator();
		while(itr.hasNext()){
			int idAreaFuncional=itr.next();
			if(show){
				if(mapListenerAreaFuncional.containsKey(idAreaFuncional))
					mapListenerAreaFuncional.get(idAreaFuncional).add(idTarget);
				else{
					HashSet<Integer> list=new HashSet<Integer>();
					list.add(idTarget);
					mapListenerAreaFuncional.put(idAreaFuncional, list);
				}
			}else{
				if(mapListenerAreaFuncional.get(idAreaFuncional)!=null){
					mapListenerAreaFuncional.get(idAreaFuncional).remove(idTarget);
					pendingListener.remove(idtoUserTask);//Lo quitamos tambien de pending por si estuviera
				}
			}
			
			if(!gestor.setHighlightedItem(String.valueOf(idTarget), show, String.valueOf(idAreaFuncional)) && show)//Si no se puede poner iluminado el item por no estar creado se pone en pendientes
				pendingListener.add(idtoUserTask);
			
			//System.err.println("mappp:"+mapListenerAreaFuncional);
			if(show || mapListenerAreaFuncional.get(idAreaFuncional)==null || mapListenerAreaFuncional.get(idAreaFuncional).isEmpty()/*Solo lo quitamos si no tiene algun otro hijo*/){
				Color color=ConstantesGraficas.highlightedColor;
				if(kba.getListenerUtask(idtoUserTask).getRgb()!=null)
					color=new Color(kba.getListenerUtask(idtoUserTask).getRgb());
				gestor.setHighlightedItemColor(String.valueOf(idAreaFuncional), color, null);
				gestor.setHighlightedItem(String.valueOf(idAreaFuncional), show, null);
			}else if(!show){
				int idTargetWithListener=mapListenerAreaFuncional.get(idAreaFuncional).iterator().next();
				if(m_listaGUI.containsKey(idAreaFuncional)){
					int idtoUserTaskWithListener=m_listaGUI.get(idAreaFuncional).getTarget(idTargetWithListener).idtoUserTask;
					Color color=ConstantesGraficas.highlightedColor;
					if(kba.getListenerUtask(idtoUserTaskWithListener).getRgb()!=null)
						color=new Color(kba.getListenerUtask(idtoUserTaskWithListener).getRgb());
					gestor.setHighlightedItemColor(String.valueOf(idAreaFuncional), color, null);
					gestor.setHighlightedItem(String.valueOf(idAreaFuncional), true, null);
				}
			}
			
			if(Auxiliar.equals(currentTarget,idTarget))
				m_listaGUI.get(idAreaFuncional).exeQueryTarget(idTarget);
		}
	}
	
}
