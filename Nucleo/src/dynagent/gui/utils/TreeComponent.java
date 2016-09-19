package dynagent.gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
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
import dynagent.common.properties.ObjectProperty;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;

public abstract class TreeComponent extends JPanel{

	private static final long serialVersionUID = 1L;
	protected WindowComponent parent;
	protected WindowComponent dialog;
	private int dimBoton = ConstantesGraficas.intToolY-3;
	protected KnowledgeBaseAdapter kba;
	protected JTree tree;
	protected Integer idtoUserTask;
	protected Integer userRol;
	protected Session session;
	private final Dimension dimMaxTree;
	protected communicator comm;
	protected JButton b;

	public TreeComponent(KnowledgeBaseAdapter kba,WindowComponent dialog_parent,String icon,final String title,String toolTipText){
		super();
		parent=dialog_parent;
		this.kba=kba;
		this.session=kba.getDefaultSession();
		this.idtoUserTask=null;
		this.userRol=null;
		this.dimMaxTree=ConstantesGraficas.dimFormManager;
		this.comm=kba.getServer();
		build(icon, title, toolTipText);
	}
	
	public void build(String icon,final String title,String toolTipText){
		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		b = new JButton(this.comm.getIcon(icon));
		b.setToolTipText(toolTipText);
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				try{
					JDialog d=new JDialog(parent.getComponent());
					d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					d.setModalityType(ModalityType.DOCUMENT_MODAL);
					d.setContentPane(getContent(title));
					d.setResizable(false);
					//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					//dialog.addWindowListener(this);
					d.pack();
					/*Dimension dimContent=dialog.getContentPane().getPreferredSize();
					Insets insetsDialog=dialog.getInsets();
					Dimension dimDialog=new Dimension(dimContent.width+insetsDialog.left+insetsDialog.right,dimContent.height+insetsDialog.top+insetsDialog.bottom);
					dialog.setSize(dimDialog);
					dialog.setPreferredSize(dimDialog);*/
					dialog=new WindowComponent(d,parent,parent.getKnowledgeBase());
					dialog.setMainDialog(parent.getMainDialog());
					dialog.setTitle(Utils.normalizeLabel(title));
					dialog.setLocationRelativeTo(parent.getComponent());
					dialog.getComponent().setVisible(true);
				}catch(Exception ex){
					comm.logError(parent.getComponent(),ex,"Error al intentar ejecutar la operación");
					ex.printStackTrace();
				}
			}			
		});
		b.setBounds(/*anchoPanel-dimBoton*/1, /*(ConstantesGraficas.intToolY-3)/2-dimBoton/2*/1, dimBoton, dimBoton);
		add(b);
	}

	public JPanel getContent(String title) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		DefaultMutableTreeNode root=new DefaultMutableTreeNode(new Node(Utils.normalizeLabel(title),null,null,null,null));
		tree = new JTree(root);
		tree.setBorder(BorderFactory.createEmptyBorder());
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		 tree.addMouseListener(buildMouseListener());
	
		insertAllNodes(root);
		expandTreeNode(root);
		
		JPanel panelTree=new JPanel();
		panelTree.setBackground(tree.getBackground());
		panelTree.add(tree);
		JScrollPane scrollTree=new JScrollPane(panelTree/*form,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED*/);
		scrollTree.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
		scrollTree.setBorder(BorderFactory.createEmptyBorder());
		
		panel.add(scrollTree,BorderLayout.CENTER);
		
		Dimension dim=scrollTree.getPreferredSize();
		if(dim.width>dimMaxTree.width)
			dim.width=dimMaxTree.width;
		if(dim.height>dimMaxTree.height)
			dim.height=dimMaxTree.height;
		
		panel.setPreferredSize(dim);
		
		collapsTreeNode(root);
		tree.expandPath(new TreePath(root));
		
		panel.validate();
		panel.repaint();
		
		//tree.makeVisible(new TreePath(root.getNextNode().getPath()));
	
		return panel;
	}
	
	public abstract MouseAdapter buildMouseListener();
	public abstract void insertAllNodes(DefaultMutableTreeNode root) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException;
	
	private void expandTreeNode(DefaultMutableTreeNode node) {
		tree.makeVisible(new TreePath(node.getPath()));
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
					.getChildAt(i);
			expandTreeNode(child);
		}
	}
	
	private void collapsTreeNode(DefaultMutableTreeNode node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
					.getChildAt(i);
			collapsTreeNode(child);
		}
		tree.collapsePath(new TreePath(node.getPath()));
	}
		
	protected void insertNodesOrder(DefaultMutableTreeNode parent,ArrayList<DefaultMutableTreeNode> listNodes){
		//Lo ordenamos para que aparezca alfabeticamente
		Collections.sort(listNodes, new Comparator<DefaultMutableTreeNode>() {
			public int compare(DefaultMutableTreeNode r1, DefaultMutableTreeNode r2) {			
				Comparable val1 = ((Node)r1.getUserObject()).getLabel();
				Comparable val2 = ((Node)r2.getUserObject()).getLabel();
				return Constants.languageCollator.compare(val1,val2);
			}
		});
		Iterator<DefaultMutableTreeNode> itrNodes=listNodes.iterator();
		while(itrNodes.hasNext()){
			DefaultMutableTreeNode d=itrNodes.next();
			parent.add(d);
		}
	}

	public class Node{
		private Integer idtoUserTask;
		private String label;
		private ObjectProperty property;
		private Integer value;
		private Integer valueCls;

		public Node(String label, Integer idtoUserTask, ObjectProperty property, Integer value, Integer valueCls) {
			super();
			this.idtoUserTask=idtoUserTask;
			this.label = label;
			this.property = property;
			this.value = value;
			this.valueCls = valueCls;
		}
		
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String toString(){
			return label;
		}
		public Integer getValue() {
			return value;
		}
		public void setValue(Integer value) {
			this.value = value;
		}

		public ObjectProperty getProperty() {
			return property;
		}

		public void setProperty(ObjectProperty property) {
			this.property = property;
		}

		public Integer getIdtoUserTask() {
			return idtoUserTask;
		}

		public void setIdtoUserTask(Integer idtoUserTask) {
			this.idtoUserTask = idtoUserTask;
		}

		public Integer getValueCls() {
			return valueCls;
		}

		public void setValueCls(Integer valueCls) {
			this.valueCls = valueCls;
		}
	}
}
