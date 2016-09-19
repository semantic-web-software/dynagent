package dynagent.gui.forms.utils;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
import dynagent.common.properties.Property;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.Utils;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.forms.filterControl;

public class FilterNodeSelector extends JPanel implements TreeSelectionListener {
	
	private static final long serialVersionUID = 1L;

	filterNodeTree m_tree;

	filterControl m_filterControl;

	boolean m_treeView = false;
	boolean showDataProperties = false;
	KnowledgeBaseAdapter kba;

	public FilterNodeSelector(KnowledgeBaseAdapter kba,Session sess, int ido, int idto, int idProp, int value, int valueCls, Integer idtoUserTask, filterControl cfc) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
//		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//super(new FlowLayout(FlowLayout.CENTER,0,0));
	

		m_filterControl = cfc;
		
		this.kba=kba;
		/* m_tree= new filterNodeTree( colorFondo, this, filter, md ); */
		build(kba, sess, ido, idto, idProp, value, valueCls, idtoUserTask);
	}
	
	public FilterNodeSelector(KnowledgeBaseAdapter kba,Session sess, int ido, int idto, int idProp, int value, int valueCls, Integer idtoUserTask) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
//		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//super(new FlowLayout(FlowLayout.CENTER,0,0));
	

		m_filterControl = null;
		showDataProperties = true;
		
		this.kba=kba;
		
		build(kba, sess, ido, idto, idProp, value, valueCls, idtoUserTask);
	}
	
	private void build(KnowledgeBaseAdapter kba, Session sess, int ido,
			int idto, int idProp, int value, int valueCls, Integer idtoUserTask)
			throws NotFoundException, IncoherenceInMotorException,
			ApplicationException, IncompatibleValueException,
			CardinalityExceedException, SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException, SQLException,
			NamingException, DataErrorException, JDOMException, ParseException,
			OperationNotPermitedException {
		m_tree = new filterNodeTree(kba, sess, this, ido, idto, idProp, value, valueCls, idtoUserTask, showDataProperties);
		m_tree.getComponent().setSelectionRow(0);
		switchView(true);
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(m_tree.getBackground());
	}

	public void switchView(boolean treeView) {
		if (treeView && !m_treeView) {
			m_treeView = true;
			if (isAncestorOf(m_tree))
				remove(m_tree);
			add(/*new JScrollPane(*/m_tree/*,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)*/);
			repaint();
		}
	}

	public void setRdn(int ref, String rdn) {
		m_tree.setRdn(ref, rdn);
	}
	
	public void expandTree(){
		m_tree.expandTreeNode((DefaultMutableTreeNode)m_tree.getComponent().getModel().getRoot());
	}
	
	public void collapseTree(){
		m_tree.collapsTreeNode((DefaultMutableTreeNode)m_tree.getComponent().getModel().getRoot(),false);
	}

	/*public Integer getRefParentSelected() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_tree.getComponent().getLastSelectedPathComponent();
		if (node == null)
			return null;
		filterNode[] path = (filterNode[]) node.getUserObjectPath();
		if (path.length > 1)
			return path[path.length - 2].value;
		return null;
	}*/
	
	public SelectedTreeLeaf getSelectedTreeLeaf(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_tree.getComponent().getLastSelectedPathComponent();
		if (node == null || !node.isLeaf()){
			return null;
		}
		
		Object[] userObjectPath=node.getUserObjectPath();
		int size=userObjectPath.length;
		
		String path=null;
		String namesPath=null;
		Integer idProp=null;
		for(int i=1;i<size;i++){
			idProp=((filterNode)userObjectPath[i]).idProp;
			if(path==null){
				path=String.valueOf(idProp);
				namesPath=((filterNode)userObjectPath[i]).label;
			}else{
				path+="#"+String.valueOf(idProp);
				namesPath+=" | "+((filterNode)userObjectPath[i]).label;
			}
			
		}
		
		if(idProp!=null && kba.getKnowledgeBase().isObjectProperty(idProp)){
			path+="#2";//Si se ha elegido un enumerado le ponemos que termine en el rdn ya que en los filterControl no muestra los enumerados como objectProperty sino como DataProperty debido a la query a base de datos
			//No importa que no lo añadamos tambien a namesPath ya que solo se utiliza para mostrarlo al usuario, el importante es path que es el que se guarda en base de datos
		}
		
		return new SelectedTreeLeaf(path,namesPath);
		
	}

	public void valueChanged(TreeSelectionEvent e) {
		try{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_tree.getComponent().getLastSelectedPathComponent();
			if (node == null)
				return;

			filterNode dataParent=null;
			
			Object[] userObjectPath=node.getUserObjectPath();
			int size=node.getUserObjectPath().length;
			if(size>1){
				dataParent=(filterNode)userObjectPath[size-2];
			}
			
			filterNode data = (filterNode) userObjectPath[size-1];
			if(m_filterControl!=null){
				m_filterControl.setCurrentFilterForm(dataParent!=null?dataParent.ido:null,dataParent!=null?dataParent.idto:null,dataParent!=null?dataParent.idProp:null,data.ido,data.idto,data.idProp,data.value,data.valueCls);
			}else{
				String path=null;
				String namesPath=null;
				for(int i=1;i<size;i++){
					if(path==null){
						path=String.valueOf(((filterNode)userObjectPath[i]).idProp);
						namesPath=((filterNode)userObjectPath[i]).label;
					}else{
						path+="#"+String.valueOf(((filterNode)userObjectPath[i]).idProp);
						namesPath+="#"+((filterNode)userObjectPath[i]).label;
					}
					
				}

			}
		}catch(Exception ex){
			Singleton.getInstance().getComm().logError(m_filterControl.getDialog().getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}
	}

	class filterNode extends Object {
		public int ido, idto, idProp;
		public Integer value, valueCls;

		String toName, rdn, label;

		filterNode(int ido, int idto, int idProp, Integer value, Integer valueCls, String toName) {
			this.ido=ido;
			this.idto=idto;
			this.idProp=idProp;
			this.value = value;
			this.valueCls = valueCls;
			this.toName = toName;
			setRdn(null);
		}

		filterNode(int ido, int idto, int idProp, int value, int valueCls, String toName, String rdn) {
			this.ido=ido;
			this.idto=idto;
			this.idProp=idProp;
			this.value = value;
			this.valueCls=valueCls;
			this.toName = toName;
			setRdn(rdn);
		}

		public void setRdn(String rdn) {
			boolean dobleSize = false;
			label = toName;
			if (rdn != null && !rdn.isEmpty()) {
				String extension = rdn;
				if (extension.length() > 10)
					extension = extension.substring(0, 9) + "..";
				label += ":<BR>" + extension;
				dobleSize = true;
			}
			if (dobleSize)
				label = "<html>" + label + "</html>";
		}

		public String toString() {
			return label;
		}
	}

	class filterNodeTree extends JPanel {
		/* Element m_filter; */

		private static final long serialVersionUID = 1L;

		JTree m_tree;

		DefaultTreeModel m_model = null;

		HashMap<Integer, DefaultMutableTreeNode> m_map = new HashMap<Integer, DefaultMutableTreeNode>();

		

		JTree getComponent() {
			return m_tree;
		}

		filterNodeTree(KnowledgeBaseAdapter kba,Session sess, TreeSelectionListener listener, int ido, int idto, int idProp, int value, int valueCls, Integer idtoUserTask, boolean showDataProperties) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
			

			

			DefaultMutableTreeNode top = build(kba,sess, ido, idto, idProp, value, valueCls, kba.getLabelClass(value,idtoUserTask),idtoUserTask, showDataProperties);
			
			m_tree = new JTree(top);
			m_model = (DefaultTreeModel) m_tree.getModel();
			m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			m_tree.addTreeSelectionListener(listener);
			//expandTreeNode( m_tree, top );
			JLabel jtf = new JLabel(m_filterControl!=null?Utils.normalizeLabel("FILTRAR POR"):"");
			jtf.setOpaque(false);
			jtf.setBackground(m_tree.getBackground());
			jtf.setFont(jtf.getFont().deriveFont(Font.BOLD));
			jtf.setBorder(new EmptyBorder(0, 0, 0, 0));
			setLayout(new BorderLayout());

			add(jtf, BorderLayout.NORTH);
			add(m_tree, BorderLayout.EAST);
			setBackground(m_tree.getBackground());
			setBorder(BorderFactory.createEmptyBorder());
		}

		public void expandTreeNode(DefaultMutableTreeNode node) {
			m_tree.makeVisible(new TreePath(node.getPath()));
			for (int i = 0; i < node.getChildCount(); i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
						.getChildAt(i);
				expandTreeNode(child);
			}
		}
		
		public void collapsTreeNode(DefaultMutableTreeNode node,boolean collapsRoot) {
			for (int i = 0; i < node.getChildCount(); i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
						.getChildAt(i);
				collapsTreeNode(child,collapsRoot);
			}
			if(collapsRoot || !node.isRoot())
				m_tree.collapsePath(new TreePath(node.getPath()));
		}

		private DefaultMutableTreeNode build(KnowledgeBaseAdapter kba,Session sess,int ido, int idto, int idProp, int value, int valueCls, String name,Integer idtoUserTask,boolean showDataProperties) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
			/*String name = Utils.normalizeLabel(kba.getLabelClass(kba.getIdtoFilter(property,posChild)));*/
			/*String name = Utils.normalizeLabel(kba.getLabelClass(id,idtoUserTask));*///TODO quitar porque se muestra el nombre de la property
			String rdn = null;

			/*
			 * Integer idFixed=kba.getIdObject(property); if(idFixed!=null)
			 * rdn=kba.getRDN(idFixed, userRol, idUserTask);
			 */

			/* Iterator itr= root.getChildren("FILTER").iterator(); */
			Iterator<Property> itr = kba.getProperties(value, valueCls, null, idtoUserTask, sess);
			//System.out.println("defaultMutableTreeNode.build children:"+itr+" "+itr.hasNext());

			if(!itr.hasNext())//Si no tiene propiedades no tenemos que crear un nodo. Puede no tener propiedades porque la informacion se coge de un instance creado hasta cierto nivel de hijos
				return null;
			
			DefaultMutableTreeNode tree = new DefaultMutableTreeNode(new filterNode(ido, idto, idProp, value, valueCls, name, rdn));
			m_map.put(value, tree);
			
			ArrayList<DefaultMutableTreeNode> listNodes=new ArrayList<DefaultMutableTreeNode>();
			while (itr.hasNext()) {
				Property prop = itr.next();
				if(prop instanceof ObjectProperty){
					ObjectProperty objectP = (ObjectProperty) prop;
					if(objectP.getTypeAccess().getViewAccess()){
						if(objectP.getEnumList().isEmpty()){
							Iterator<Integer> itrRange=objectP.getRangoList().iterator();
							while(itrRange.hasNext()){
								Integer idoFilter = itrRange.next();
								if (Constants.isIDTemporal(idoFilter) && m_map.get(idoFilter) == null){
									int idtoFilter=kba.getClass(idoFilter);
									DefaultMutableTreeNode node=build(kba,sess,objectP.getIdo(),objectP.getIdto(),objectP.getIdProp(),idoFilter, idtoFilter, kba.getLabelProperty(objectP, objectP.getIdto(), idtoUserTask),idtoUserTask, showDataProperties);
									if(node!=null)
										listNodes.add(node);
								}
							}
						}else if(m_filterControl==null){//Si no es de un filterControl tenemos que permitir la seleccion de enumerados
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(new filterNode(value, valueCls, objectP.getIdProp(), null, null, kba.getLabelProperty(objectP.getIdProp(), valueCls, idtoUserTask)));
							listNodes.add(node);
						}
					}
				}else if(showDataProperties){
					DataProperty dataP = (DataProperty) prop;
					if(dataP.getTypeAccess().getViewAccess()){
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(new filterNode(value, valueCls, dataP.getIdProp(), null, null, kba.getLabelProperty(dataP.getIdProp(), valueCls, idtoUserTask)));
						listNodes.add(node);
					}
				}
			}
			// Lo ordenamos para que aparezca alfabeticamente
			Collections.sort(listNodes, new Comparator<DefaultMutableTreeNode>() {
				public int compare(DefaultMutableTreeNode r1, DefaultMutableTreeNode r2) {			
					Comparable val1 = ((filterNode)r1.getUserObject()).label;
					Comparable val2 = ((filterNode)r2.getUserObject()).label;
					return Constants.languageCollator.compare(val1,val2);
				}
			});
			Iterator<DefaultMutableTreeNode> itrNodes=listNodes.iterator();
			while(itrNodes.hasNext()){
				DefaultMutableTreeNode d=itrNodes.next();
				tree.add(d);
			}
			return tree;
		}

		public void setRdn(int ref, String rdn) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_map
					.get(new Integer(ref));
			filterNode fn = (filterNode) node.getUserObject();
			fn.setRdn(rdn);
			m_model.nodeChanged(node);
		}
	}
}
