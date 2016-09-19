package dynagent.gui.utils;

import gdev.gen.AssignValueException;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.utils.Utils;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.actions.commands.DataTransferCommandPath;
import dynagent.gui.actions.commands.ReportCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.gui.utils.TreeComponent.Node;

public class DataTransferComponent extends TreeComponent{

	public DataTransferComponent(KnowledgeBaseAdapter kba,WindowComponent dialog_parent){
		super(kba,dialog_parent,"transfer","Traspasos externos","Traspasos a otros programas");
	}

	@Override
	public void insertAllNodes(DefaultMutableTreeNode root) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		HashMap<Integer,DefaultMutableTreeNode> mapNameNodesProgram=new HashMap<Integer,DefaultMutableTreeNode>();
		HashMap<DefaultMutableTreeNode,ArrayList<DefaultMutableTreeNode>> mapNodesProgramNodesDataTransfer=new HashMap<DefaultMutableTreeNode, ArrayList<DefaultMutableTreeNode>>();
		Iterator<Integer> itr=kba.getSpecialized(Constants.IDTO_DATA_TRANSFER).iterator();
		while(itr.hasNext()){
			int idto=itr.next();
			ObjectProperty propProgram=kba.getChild(null, idto, Constants.IdPROP_TARGETPROGRAM, userRol, idtoUserTask, session);
			Iterator<ObjectValue> itrIdosProgram=kba.getEnums(propProgram);
			while(itrIdosProgram.hasNext()){
				ObjectValue objectValueProgram=itrIdosProgram.next();
				int idoProgram=objectValueProgram.getValue();
				int idtoProgram=objectValueProgram.getValueCls();
				DefaultMutableTreeNode nodeProgram=mapNameNodesProgram.get(idoProgram);
				if(nodeProgram==null){
					String valueProgram=Singleton.getInstance().getComm().serverGetRdn(idoProgram,idtoProgram);
					nodeProgram=new DefaultMutableTreeNode(new Node(valueProgram,null,null,idoProgram,idtoProgram));
					mapNameNodesProgram.put(idoProgram,nodeProgram);
					mapNodesProgramNodesDataTransfer.put(nodeProgram, new ArrayList<DefaultMutableTreeNode>());
				}
				ArrayList<DefaultMutableTreeNode> listNodes=mapNodesProgramNodesDataTransfer.get(nodeProgram);
				//En Node.getValue metemos el idto
				DefaultMutableTreeNode nodeDataTransfer=new DefaultMutableTreeNode(new Node(kba.getLabelClass(idto, null),null,null,idto,idto));
				listNodes.add(nodeDataTransfer);
			}
		}
		
		Iterator<DefaultMutableTreeNode> itrInsert=mapNodesProgramNodesDataTransfer.keySet().iterator();
		while(itrInsert.hasNext()){
			DefaultMutableTreeNode nodeProgram=itrInsert.next();
			insertNodesOrder(nodeProgram, mapNodesProgramNodesDataTransfer.get(nodeProgram));
		}
		insertNodesOrder(root,new ArrayList<DefaultMutableTreeNode>(mapNodesProgramNodesDataTransfer.keySet()));
	}

	@Override
	public MouseAdapter buildMouseListener() {
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
									DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
									Node parentNodeInfo = (Node)parentNode.getUserObject();
									exeAction(nodeInfo.getValue(),parentNodeInfo.getValue(),parentNodeInfo.getValueCls());
								}
			             }
			         }
		    	 }catch(Exception ex){
					Singleton.getInstance().getComm().logError(parent.getComponent(),ex,"Error al intentar ejecutar el traspaso");
					ex.printStackTrace();
				}
		     }
		};
		
		return mouseAdapter;
	}
	
	public void exeAction(int idto,int idoProgram,int idtoProgram) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException, HeadlessException, AWTException, ActionException{
		ArrayList<commandPath> commandList = new ArrayList<commandPath>();
		/*commandList.add(new commandPath(parRowThis.getIDO(),m_currMode,idtoUserTaskThis));*/
		DataTransferCommandPath commandPath=new DataTransferCommandPath(idto,idoProgram,idtoProgram,userRol,kba.getDefaultSession());
		commandList.add(commandPath);

		Singleton.getInstance().getActionManager().exeOperation(commandList.get(0),kba,null,dialog,false);
	}
}
