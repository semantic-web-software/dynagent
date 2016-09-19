package dynagent.gui.utils;

import gdev.gen.AssignValueException;
import gdev.gen.GConst;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.WindowComponent;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.actions.commands.ReportCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.gui.utils.TreeComponent.Node;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class ReportsComponent extends TreeComponent {

		public ReportsComponent(KnowledgeBaseAdapter kba,WindowComponent dialog_parent) throws NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
			super(kba,dialog_parent,"reports","Informes","Informes");
			
			
			int idto=kba.getIdClass(Constants.CLS_BOOLEAN_CONFIG_PARAM);
			Iterator<Integer> itr=kba.getIndividuals(idto, Constants.LEVEL_INDIVIDUAL, false);
			while(itr.hasNext()){
				Integer ido=itr.next();
				DataProperty rdnProperty=(DataProperty)kba.getRDN(ido, idto, null, null, kba.getDefaultSession());
				final String rdn="rp@"+rdnProperty.getUniqueValue().getValue_s();
				try{
					final int idtoUserTaskReport=kba.getIdClass(rdn);//Para ver si la configuracion se refiere a una accion o no ( Para estos casos llamamos la configuracion igual que la accion )
					if(kba.isSpecialized(idtoUserTaskReport, Constants.IDTO_REPORT)){
						DataProperty activoProperty=kba.getField(ido, idto, kba.getKnowledgeBase().getIdProperty("activo"), null, null, kba.getDefaultSession());
						boolean activo=Boolean.valueOf(activoProperty.getUniqueValue().getValue_s()).booleanValue();
						if(activo && kba.getAccessIndividual(idtoUserTaskReport, userRol, idtoUserTaskReport).getViewAccess()){
							DataProperty shortCutProperty=kba.getField(ido, idto, kba.getKnowledgeBase().getIdProperty("filtro"), null, null, kba.getDefaultSession());
							Value shortCutValue=shortCutProperty.getUniqueValue();
							if(shortCutValue!=null){
								String shortCut=shortCutValue.getValue_s();
								Integer type=getType(shortCut);
								if(type!=null){
									Integer modifier=getModifier(shortCut);
									
									final KnowledgeBaseAdapter kbaThis=kba;
									GConst.addShortCut(null, b, type, modifier, rdn, JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

										@Override
										public void actionPerformed(ActionEvent arg0) {
											
											try {
//												HashMap<String,String> oidReport=kbaThis.getReport(null, idoUserTaskReport, idtoUserTaskReport, rdn, true, null, null, null, kbaThis.getDefaultSession());
//												if(oidReport!=null){
//													comm.showReport("_blank",oidReport);
//												}
												
												int idoUserTaskReport=kbaThis.getIdoUserTaskReport(idtoUserTaskReport);
												ObjectProperty propertyParams=null;
												Integer idRange=null;
												try{
													propertyParams=kbaThis.getChild(idoUserTaskReport,idtoUserTaskReport,Constants.IdPROP_PARAMS,userRol, idtoUserTask, session);
													idRange=kbaThis.getIdRange(propertyParams);
												}catch(NotFoundException ex){
													System.err.println("WARNING: El report "+kbaThis.getLabelUserTask(idtoUserTaskReport)+" no tiene parametros de entrada");
												}
												exeAction(idtoUserTaskReport, propertyParams, idRange);
											} catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}											
										}
										
									});
								}else{
									System.err.println("WARNING: ShorCut "+shortCut+" no es correcto para "+rdn);
								}
								
							}
						}

					}
				}catch(NotFoundException ex){
					//No se trata de una configuracion para reports, por lo que no se procesa
				}
			}
		}

	    private Integer getModifier(String text) {
			Integer modifier=0;
	    	if(text.length()==1){
	    		modifier=InputEvent.CTRL_DOWN_MASK;
	    	}
	    	return modifier;
		}

		public Integer getType(String text) {
	    	Integer type=null;
	    	if(text.length()>1){
	    		if(text.equalsIgnoreCase("F1")){
	    			type=KeyEvent.VK_F1;
	    		}else if(text.equalsIgnoreCase("F2")){
	    			type=KeyEvent.VK_F2;
	    		}else if(text.equalsIgnoreCase("F3")){
	    			type=KeyEvent.VK_F3;
	    		}else if(text.equalsIgnoreCase("F4")){
	    			type=KeyEvent.VK_F4;
	    		}else if(text.equalsIgnoreCase("F5")){
	    			type=KeyEvent.VK_F5;
	    		}else if(text.equalsIgnoreCase("F6")){
	    			type=KeyEvent.VK_F6;
	    		}else if(text.equalsIgnoreCase("F7")){
	    			type=KeyEvent.VK_F7;
	    		}else if(text.equalsIgnoreCase("F8")){
	    			type=KeyEvent.VK_F8;
	    		}else if(text.equalsIgnoreCase("F9")){
	    			type=KeyEvent.VK_F9;
	    		}else if(text.equalsIgnoreCase("F10")){
	    			type=KeyEvent.VK_F10;
	    		}else if(text.equalsIgnoreCase("F11")){
	    			type=KeyEvent.VK_F11;
	    		}else if(text.equalsIgnoreCase("F12")){
	    			type=KeyEvent.VK_F12;
	    		}
	    	}else{
		    	char character=text.charAt(0);
		        switch (character) {
		        case 'a': type=KeyEvent.VK_A; break;
		        case 'b': type=KeyEvent.VK_B; break;
		        case 'c': type=KeyEvent.VK_C; break;
		        case 'd': type=KeyEvent.VK_D; break;
		        case 'e': type=KeyEvent.VK_E; break;
		        case 'f': type=KeyEvent.VK_F; break;
		        case 'g': type=KeyEvent.VK_G; break;
		        case 'h': type=KeyEvent.VK_H; break;
		        case 'i': type=KeyEvent.VK_I; break;
		        case 'j': type=KeyEvent.VK_J; break;
		        case 'k': type=KeyEvent.VK_K; break;
		        case 'l': type=KeyEvent.VK_L; break;
		        case 'm': type=KeyEvent.VK_M; break;
		        case 'n': type=KeyEvent.VK_N; break;
		        case 'o': type=KeyEvent.VK_O; break;
		        case 'p': type=KeyEvent.VK_P; break;
		        case 'q': type=KeyEvent.VK_Q; break;
		        case 'r': type=KeyEvent.VK_R; break;
		        case 's': type=KeyEvent.VK_S; break;
		        case 't': type=KeyEvent.VK_T; break;
		        case 'u': type=KeyEvent.VK_U; break;
		        case 'v': type=KeyEvent.VK_V; break;
		        case 'w': type=KeyEvent.VK_W; break;
		        case 'x': type=KeyEvent.VK_X; break;
		        case 'y': type=KeyEvent.VK_Y; break;
		        case 'z': type=KeyEvent.VK_Z; break;
		        case 'A': type=KeyEvent.VK_A; break;
		        case 'B': type=KeyEvent.VK_B; break;
		        case 'C': type=KeyEvent.VK_C; break;
		        case 'D': type=KeyEvent.VK_D; break;
		        case 'E': type=KeyEvent.VK_E; break;
		        case 'F': type=KeyEvent.VK_F; break;
		        case 'G': type=KeyEvent.VK_G; break;
		        case 'H': type=KeyEvent.VK_H; break;
		        case 'I': type=KeyEvent.VK_I; break;
		        case 'J': type=KeyEvent.VK_J; break;
		        case 'K': type=KeyEvent.VK_K; break;
		        case 'L': type=KeyEvent.VK_L; break;
		        case 'M': type=KeyEvent.VK_M; break;
		        case 'N': type=KeyEvent.VK_N; break;
		        case 'O': type=KeyEvent.VK_O; break;
		        case 'P': type=KeyEvent.VK_P; break;
		        case 'Q': type=KeyEvent.VK_Q; break;
		        case 'R': type=KeyEvent.VK_R; break;
		        case 'S': type=KeyEvent.VK_S; break;
		        case 'T': type=KeyEvent.VK_T; break;
		        case 'U': type=KeyEvent.VK_U; break;
		        case 'V': type=KeyEvent.VK_V; break;
		        case 'W': type=KeyEvent.VK_W; break;
		        case 'X': type=KeyEvent.VK_X; break;
		        case 'Y': type=KeyEvent.VK_Y; break;
		        case 'Z': type=KeyEvent.VK_Z; break;
		        case '0': type=KeyEvent.VK_0; break;
		        case '1': type=KeyEvent.VK_1; break;
		        case '2': type=KeyEvent.VK_2; break;
		        case '3': type=KeyEvent.VK_3; break;
		        case '4': type=KeyEvent.VK_4; break;
		        case '5': type=KeyEvent.VK_5; break;
		        case '6': type=KeyEvent.VK_6; break;
		        case '7': type=KeyEvent.VK_7; break;
		        case '8': type=KeyEvent.VK_8; break;
		        case '9': type=KeyEvent.VK_9; break;
		        default:
		            throw new IllegalArgumentException("Cannot type character " + character);
		        }
	    	}
	    	
	    	return type;
	    }
		
		public MouseAdapter buildMouseListener(){
			MouseAdapter mouseAdapter=new MouseAdapter(){
				 public void mousePressed(MouseEvent e) {
			    	 try{
				    	 int selRow = tree.getRowForLocation(e.getX(), e.getY());
				         //TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				         if(selRow != -1) {
				             if(e.getClickCount() == 2) {
				            	 DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
									if (node == null)	
										return;
									Node nodeInfo = (Node)node.getUserObject();
									if (node.isLeaf() && !node.isRoot()) {
										exeAction(nodeInfo.getIdtoUserTask(),nodeInfo.getProperty(),nodeInfo.getValue());
									}
				             }
				         }
			    	 }catch(Exception ex){
						Singleton.getInstance().getComm().logError(parent.getComponent(),ex,"Error al intentar ejecutar el informe");
						ex.printStackTrace();
					}
			     }
			};
			
			return mouseAdapter;
		}
		
		public void exeAction(int idtoUserTaskReport,ObjectProperty propertyParams,Integer value) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException, HeadlessException, AWTException, ActionException{
			if(propertyParams!=null){
				ArrayList<commandPath> commandList = new ArrayList<commandPath>();
				/*commandList.add(new commandPath(parRowThis.getIDO(),m_currMode,idtoUserTaskThis));*/
				ReportCommandPath commandPath=new ReportCommandPath(propertyParams,new HashMap<Integer, Integer>(),propertyParams.getIdto(),userRol,kba.getDefaultSession());
				commandList.add(commandPath);
	
				Singleton.getInstance().getActionManager().exeOperation(commandList.get(0),kba,null,dialog!=null?dialog:parent,false);
			}else{
				int idoUserTaskReport=kba.getIdoUserTaskReport(idtoUserTaskReport);
				int format=kba.getIdoValue(kba.getChild(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_REPORT_FORMAT, userRol, idtoUserTask, kba.getDefaultSession()));
				DataProperty propDirectImpresion=kba.getField(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_DIRECTIMPRESION, userRol, idtoUserTask, kba.getDefaultSession());
				boolean directImpresion=((BooleanValue)propDirectImpresion.getUniqueValue()).getBvalue();
				HashMap<String,String> oidReport=kba.getReport(/*query*/null, false,idoUserTaskReport, idtoUserTaskReport, kba.getLabelClassWithoutAlias(idtoUserTaskReport), directImpresion, format, userRol, idtoUserTask, kba.getDefaultSession(),0,false);
				if(oidReport!=null){
					comm.showReport("Informe "+kba.getLabelUserTask(idtoUserTaskReport),oidReport,false,true);
					DataProperty propConfirm=kba.getField(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_CHECKPRINTING, userRol, idtoUserTask, kba.getDefaultSession());
					if(kba.getValueData(propConfirm)!=null){
						Object[] options = {"Sí", "No"};
						int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
								"¿El informe ha sido impreso en papel?",
								Utils.normalizeLabel("CONFIRMACIÓN DE IMPRESIÓN"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null,
								options,
								options[1],dialog.getComponent());
	
						if (res == JOptionPane.YES_OPTION){
							kba.setState(idoUserTaskReport,idtoUserTaskReport,Constants.INDIVIDUAL_REALIZADO,userRol,idtoUserTask,kba.getDDBBSession());
						}
					}
				}else{
					Singleton.getInstance().getMessagesControl().showErrorMessage("El informe "+kba.getLabelUserTask(idtoUserTaskReport)+" no se encuentra en base de datos",dialog.getComponent());
				}
			}
		}

		@Override
		public void insertAllNodes(DefaultMutableTreeNode root) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
			Iterator<Integer> itr=kba.getAreasFuncionales().iterator();
			ArrayList<DefaultMutableTreeNode> listNodesAreaFuncional=new ArrayList<DefaultMutableTreeNode>();
			ArrayList<DefaultMutableTreeNode> listNodesAllReports=new ArrayList<DefaultMutableTreeNode>();
			HashSet<Integer> idosReportProcessed=new HashSet<Integer>(); 
			while(itr.hasNext()){
				int idoAreaFuncional=itr.next();
				String labelAreaFuncionales=kba.getLabelAreaFuncional(idoAreaFuncional);
				DefaultMutableTreeNode nodeAreaFuncional=new DefaultMutableTreeNode(new Node(labelAreaFuncionales,null,null,idoAreaFuncional,Constants.IDTO_FUNCTIONAL_AREA));
				listNodesAreaFuncional.add(nodeAreaFuncional);
				
				Iterator<Integer> itrIdoUserTask=kba.getIdoUserTasks(idoAreaFuncional);
				ArrayList<DefaultMutableTreeNode> listNodesTarget=new ArrayList<DefaultMutableTreeNode>();
				while(itrIdoUserTask.hasNext()){
					Integer idoUserTask=itrIdoUserTask.next();
					ObjectProperty propTarget=kba.getTarget(idoUserTask, kba.getIdtoUserTask(idoUserTask), userRol, session);
					int valueTarget=kba.getIdRange(propTarget);
					int valueClsTarget=kba.getClass(valueTarget);
					String label=kba.getLabelUserTask(idoUserTask);//kba.getLabelClass(valueClsTarget, idtoUserTask);
					DefaultMutableTreeNode nodeUserTask=new DefaultMutableTreeNode(new Node(label,null,propTarget,valueTarget,valueClsTarget));
					listNodesTarget.add(nodeUserTask);
					
					Iterator<Integer> itrIdoReport=kba.getIdoUserTasksReport(valueClsTarget,idoAreaFuncional);
					ArrayList<DefaultMutableTreeNode> listNodesReport=new ArrayList<DefaultMutableTreeNode>();
					while(itrIdoReport.hasNext()){
						Integer idoReport=itrIdoReport.next();
						int idtoReport=kba.getIdtoUserTaskReport(idoReport);
						if(kba.getAccessIndividual(idoReport, userRol, idtoReport).getViewAccess() && kba.getAccessIndividual(valueTarget, userRol, idtoReport).getViewAccess()){
							String labelReport=kba.getLabelUserTask(idoReport);
	
							ObjectProperty propParams=null;
							Integer idRange=null;
							Integer idtoRange=null;
							try{
								propParams=kba.getChild(idoReport,idtoReport,Constants.IdPROP_PARAMS,userRol, idtoUserTask, session);
								idRange=kba.getIdRange(propParams);
								idtoRange=kba.getClass(idRange);
							}catch(NotFoundException ex){
								System.err.println("WARNING: El report "+kba.getLabelUserTask(idtoReport)+" no tiene parametros de entrada");
							}
							DefaultMutableTreeNode nodeReport=new DefaultMutableTreeNode(new Node(labelReport,idtoReport,propParams,idRange,idtoRange));
							listNodesReport.add(nodeReport);
							if(!idosReportProcessed.contains(idoReport))
								listNodesAllReports.add((DefaultMutableTreeNode)nodeReport.clone());
							idosReportProcessed.add(idoReport);
						}
					}
					//Si no tiene reports no mostramos el nodo de la userTask
					if(listNodesReport.isEmpty())
						listNodesTarget.remove(nodeUserTask);
					else insertNodesOrder(nodeUserTask,listNodesReport);
				}
				//Si no tiene reports no mostramos el nodo del area funcional
				if(listNodesTarget.isEmpty())
					listNodesAreaFuncional.remove(nodeAreaFuncional);
				else insertNodesOrder(nodeAreaFuncional,listNodesTarget);
				
			}
			insertNodesOrder(root,listNodesAreaFuncional);
			
			if(!listNodesAllReports.isEmpty()){
				DefaultMutableTreeNode allReports=new DefaultMutableTreeNode(new Node(Utils.normalizeLabel("<TODOS>"),null,null,null,null));
				root.add(allReports);
				insertNodesOrder(allReports,listNodesAllReports);
			}
		}
}
