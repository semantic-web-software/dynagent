package dynagent.gui.utils;

import gdev.gen.GConfigView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.queryData;
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
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.Utils;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.forms.utils.Column;
import dynagent.gui.forms.utils.FilterNodeSelector;
import dynagent.gui.forms.utils.SelectedTreeLeaf;

public class ColumnsTableComponent {

	private KnowledgeBaseAdapter kba;
	private WindowComponent parent;
	private WindowComponent dialog;
	private Integer idtoUserTask;
	private Session sess;
	private ObjectProperty property;
	ArrayList<SelectedTreeLeaf> columns;
	private int idoCol;
	private int idtoCol;
	
	private boolean modified;

	public ColumnsTableComponent(KnowledgeBaseAdapter kba,WindowComponent dialog_parent, Integer idtoUserTask, ObjectProperty property, Session sess, ArrayList<Column> columnList) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		this.kba=kba;
		this.parent=dialog_parent;
		this.idtoUserTask=idtoUserTask;
		this.sess=sess;
		this.property=property;
		this.modified=false;
		
		buildColumns(kba, idtoUserTask, property, sess, columnList);
		
		buildForm(columns);
	}

	private void buildColumns(KnowledgeBaseAdapter kba, Integer idtoUserTask,
			ObjectProperty property, Session sess,
			ArrayList<Column> columnList)
			throws IncompatibleValueException, CardinalityExceedException,
			SystemException, RemoteSystemException, CommunicationException,
			InstanceLockedException, ApplicationException, NotFoundException,
			IncoherenceInMotorException, OperationNotPermitedException,
			SQLException, NamingException, DataErrorException, JDOMException,
			ParseException {
		
		columns=new ArrayList<SelectedTreeLeaf>();
		idtoCol=kba.getIdClass(Constants.CLS_COLUMNPROPERTY);
		
		int idto=property.getIdto();
		int value=kba.getIdRange(property);
		int valueCls=kba.getClass(value);
		
		//Creamos el instance para hacer la query a base de datos y saber si existe algun individuo COLUMNAS_TABLA con este formulario y tabla. En caso de que exista se modifica, si no se crea nuevo.
		int idoFilter=kba.createFilter(idtoCol, null, idtoUserTask, 2, sess);
		final instance instFilter=kba.getTreeObject(idoFilter, null, idtoUserTask, sess, true);
		
		//Primero hacemos una query para saber cuales son los idos de CLASE en base de datos
		HashMap<Integer,Value> mapPropertiesValue=new HashMap<Integer, Value>();
		ArrayList<String> rdns=new ArrayList<String>();
		String rdnForm=kba.getLabelClassWithoutAlias(idto);
		String rdnTable=kba.getLabelClassWithoutAlias(valueCls);
		rdns.add(rdnForm);
		rdns.add(rdnTable);
		selectData select=Auxiliar.getIdosFromServer(Constants.IDTO_CLASS, rdns, idtoUserTask, kba.getKnowledgeBase());
		Iterator<instance> iterator=select.getIterator();
		while(iterator.hasNext()){
			instance inst=iterator.next();
			int idProp=0;
			if(inst.getRdn().equals(rdnForm)){
				idProp=kba.getIdProp("dominio");
			}else{
				idProp=kba.getIdProp("tabla");
			}
			Value val=kba.buildValue(inst.getIDO(),inst.getIdTo());
			instFilter.setValue(idoFilter, idProp, null, val);
			mapPropertiesValue.put(idProp, kba.buildValue(inst.getIDO(),inst.getIdTo()));
		}
		
		ArrayList<SelectQuery> selectList=new ArrayList<SelectQuery>();
		SelectQuery selectQ=new SelectQuery(String.valueOf(idoFilter),Constants.IdPROP_RDN,null,null);
		selectList.add(selectQ);
		selectData selectFilter = kba.getServer().serverGetQuery(kba.getQueryXML(instFilter, selectList, null, idtoUserTask, GConfigView.limitFinderResults), idtoUserTask, queryData.MODE_ROW);

		if(selectFilter==null || !selectFilter.hasData() || columnList.get(0).getProperty()!=null){
			idoCol=kba.createPrototype(idtoCol, Constants.LEVEL_PROTOTYPE, null, idtoUserTask, sess);
			
			//Asignamos formulario y tabla
			for(Integer idProp:mapPropertiesValue.keySet()){
				Value val=mapPropertiesValue.get(idProp);
				kba.setValue(idoCol, idProp, val, null, null, idtoUserTask, sess);
			}	
			
			boolean oldVersion=columnList.get(0).getProperty()!=null;
			
			for(Column col:columnList){
				SelectedTreeLeaf values=null;
				if(!oldVersion){
					values=new SelectedTreeLeaf(col.getIdPropPath(), col.getPropPath());
				}else{
					String parentTree=col.getParentTree();
					if(parentTree==null){
						String propNamePath=col.getName();
						
						String idPropPath=col.getProperty().getIdProp().toString();
						if(!kba.getKnowledgeBase().isDataProperty(col.getProperty().getIdProp())){
							idPropPath+="#2";
						}
						
						values=new SelectedTreeLeaf(idPropPath, propNamePath);
					}else{
						String[] split=parentTree.split("#");
						String idPropPath="";
						String propNamePath="";
						for(String par:split){
							String[] idoAndProp=par.split(",");
							int idoP=Integer.valueOf(idoAndProp[0]);
							int idtoP=kba.getClass(idoP);
							int idPropP=new Integer(idoAndProp[1]);
							
							if(!idPropPath.isEmpty()){
								idPropPath+="#";
							}
							idPropPath+=idPropP;
							if(!propNamePath.isEmpty()){
								propNamePath+=" | ";
							}
							propNamePath+=kba.getLabelProperty(idPropP, idtoP, idtoUserTask);
						}
						
						idPropPath+="#"+col.getProperty().getIdProp().toString();
						if(!kba.getKnowledgeBase().isDataProperty(col.getProperty().getIdProp())){
							idPropPath+="#2";
						}
						propNamePath+=" | "+kba.getLabelProperty(col.getProperty().getIdProp(), col.getProperty().getIdto(), idtoUserTask);
						
						
						values=new SelectedTreeLeaf(idPropPath, propNamePath);
					}
				}
				columns.add(values);
			}
		}else{
			idoCol=selectFilter.getFirst().getIDO();
			kba.loadIndividual(idoCol, idtoCol, 1, null, idtoUserTask, sess);
			
			LinkedList<Value> values=kba.getChild(idoCol, idtoCol, kba.getIdProp("columnas"), null, idtoUserTask, sess).getValues();
			for(Value val:values){
				ObjectValue objValue=(ObjectValue)val;
				DataProperty orderProperty=kba.getField(objValue.getIDOIndividual(), objValue.getIDTOIndividual(), kba.getIdProp("orden"), null, idtoUserTask, sess);
				DataProperty propPathProperty=kba.getField(objValue.getIDOIndividual(), objValue.getIDTOIndividual(), kba.getIdProp("ruta_propiedad"), null, idtoUserTask, sess);
				
				int order=(Integer)kba.getValueData(orderProperty);
				String propPath=(String)kba.getValueData(propPathProperty);
				
				String[] propPathSplit=propPath.split("#");
				String idPropPath="";
				String propNamePath="";
				
				int idoAux=value;
				int idtoAux=valueCls;
				for(String prop:propPathSplit){
					int idProp=kba.getIdProp(prop);
					if(!idPropPath.isEmpty()){
						idPropPath+="#";
					}
					
					Property propertyAux=kba.getProperty(idoAux, idtoAux, idProp, null, idtoUserTask, sess);
					if(propertyAux instanceof ObjectProperty){
						idoAux=kba.getIdRange((ObjectProperty) propertyAux);
						idtoAux=kba.getClass(idoAux);
					}
					idPropPath+=idProp;
					if(!propNamePath.isEmpty()){
						propNamePath+=" | ";
					}
					propNamePath+=kba.getLabelProperty(propertyAux.getIdProp(), propertyAux.getIdto(), idtoUserTask);
				}
				
				SelectedTreeLeaf valTree=new SelectedTreeLeaf(idPropPath,propNamePath,order);
				columns.add(valTree);
			}
			
			Collections.sort(columns, new Comparator<SelectedTreeLeaf>() {

				@Override
				public int compare(SelectedTreeLeaf o1, SelectedTreeLeaf o2) {
					return o1.getOrder().compareTo(o2.getOrder());
				}
			});
		}
	}
	
	private void buildForm(ArrayList<SelectedTreeLeaf> columns) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		JDialog d=new JDialog(parent.getComponent());
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setModalityType(ModalityType.DOCUMENT_MODAL);
		d.setContentPane(getContent(columns));
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
		dialog.setTitle(Utils.normalizeLabel("Configuración columnas tabla"));
		dialog.setLocationRelativeTo(parent.getComponent());
		dialog.getComponent().setVisible(true);
	}
	
	private JPanel getContent(ArrayList<SelectedTreeLeaf> columns) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout());
		
		final DefaultListModel listModel=new DefaultListModel();
		final JList list=new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFixedCellHeight(28);
		list.setBorder(new EmptyBorder(2,4,2,4));
		
		for(SelectedTreeLeaf col:columns){
			listModel.addElement(col);
		}
		
		JPanel listPanel=new JPanel();
		JScrollPane scroll=new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(300, 450));
		listPanel.add(scroll);
		panel.add(listPanel, BorderLayout.CENTER);
		JPanel buttonsPanel=new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		
		buttonsPanel.setPreferredSize(new Dimension(70, 350));
		
		JButton upButton=createButton("Subir",new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(list.getSelectedIndex()!=-1){
					int index=list.getSelectedIndex();
					if(index>0){
						Object value=listModel.remove(index);
						listModel.add(index-1, value);
						list.setSelectedIndex(index-1);
						modified=true;
					}
					
				}
			}
		});
		
		JButton downButton=createButton("Bajar",new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(list.getSelectedIndex()!=-1){
					int index=list.getSelectedIndex();
					if(index<listModel.getSize()-1){
						Object value=listModel.remove(index);
						listModel.add(index+1, value);
						list.setSelectedIndex(index+1);
						modified=true;
					}
					
				}
			}
		});
		
		final int ido=property.getIdo();
		final int idto=property.getIdto();
		final int idProp=property.getIdProp();
		final int value=kba.getIdRange(property);
		final int valueCls=kba.getClass(value);
		
		JButton addButton=createButton("Añadir",new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					
					createColumnsTree(listModel, list, ido, idto, idProp, value, valueCls);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Singleton.getInstance().getMessagesControl().showErrorMessage("Error al mostrar el formulario de configuración de columnas de la tabla",parent.getComponent());
				}
			}

		});
		
		JButton removeButton=createButton("Quitar",new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(list.getSelectedIndex()!=-1){
					int index=list.getSelectedIndex();
					listModel.remove(index);
					modified=true;
				}
			}
		});
		
		buttonsPanel.add(upButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		buttonsPanel.add(downButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		buttonsPanel.add(removeButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
		buttonsPanel.add(addButton);
		
		JPanel buttonsPanelAux=new JPanel();
		buttonsPanelAux.add(buttonsPanel);
		panel.add(buttonsPanelAux,BorderLayout.EAST);
		
		
		JPanel southPanel=new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		
		JPanel buttonsPanelOkCancel=new JPanel();
		JButton okButton=new JButton("Aceptar");
		buttonsPanelOkCancel.add(okButton);
		
		
		JButton cancelButton=new JButton("Cancelar");
		buttonsPanelOkCancel.add(cancelButton);
		
		//tree.expandTree();
		
		JLabel label=new JLabel("* Los cambios realizados serán visibles cuando se reinicie la aplicación");
		label.setForeground(Color.RED.brighter());
		
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		label.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonsPanelOkCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanelOkCancel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		southPanel.add(label);
		southPanel.add(buttonsPanelOkCancel);
		panel.add(southPanel,BorderLayout.SOUTH);
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.disabledEvents();
				try{
					if(modified){
						saveColumnsOnDatabase(listModel, idto, valueCls);
					}
					
					dialog.getComponent().dispose();
					
				}catch(Exception ex){
					ex.printStackTrace();
					Singleton.getInstance().getMessagesControl().showErrorMessage("Error al aceptar el formulario de configuración de columnas de la tabla",parent.getComponent());
				}finally{
					dialog.enabledEvents();
				}
			}

		});
		
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.disabledEvents();
				sess.setForceParent(false);
				try{
					sess.rollBack();
					//dialog.getComponent().setVisible(false);
					dialog.getComponent().dispose();
				}catch(Exception ex){
					ex.printStackTrace();
					Singleton.getInstance().getMessagesControl().showErrorMessage("Error al cancelar el formulario de configuración de columnas de la tabla",parent.getComponent());
				}finally{
					dialog.enabledEvents();
				}
			}
		});
		
		return panel;
	}

	private JButton createButton(String label,ActionListener listener) {
		Dimension dim=new Dimension(70, 25);
		JButton button=new JButton(label);
		button.setPreferredSize(dim);
		button.setMaximumSize(dim);
		button.addActionListener(listener);

		return button;
	}
	
	private void createColumnsTree(final DefaultListModel listModel,
			final JList list, final int ido, final int idto,
			final int idProp, final int value, final int valueCls)
			throws NotFoundException, IncoherenceInMotorException,
			ApplicationException, IncompatibleValueException,
			CardinalityExceedException, SystemException,
			RemoteSystemException, CommunicationException,
			InstanceLockedException, SQLException, NamingException,
			DataErrorException, JDOMException, ParseException,
			OperationNotPermitedException {
		final FilterNodeSelector tree=new FilterNodeSelector(kba, sess, ido, idto, idProp, value, valueCls, idtoUserTask);
		
		JScrollPane scroll=new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(300, 300));
		
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(scroll,BorderLayout.CENTER);
		
		JPanel buttonsPanel=new JPanel();
		JButton okButton=new JButton("Aceptar");
		buttonsPanel.add(okButton);
		
		
		JButton cancelButton=new JButton("Cancelar");
		buttonsPanel.add(cancelButton);
		
		//tree.expandTree();
		panel.add(buttonsPanel,BorderLayout.SOUTH);
		
		JDialog d=new JDialog(dialog.getComponent());
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setModalityType(ModalityType.DOCUMENT_MODAL);
		d.setContentPane(panel);
		d.setResizable(true);
		//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		//dialog.addWindowListener(this);
		d.pack();
		/*Dimension dimContent=dialog.getContentPane().getPreferredSize();
		Insets insetsDialog=dialog.getInsets();
		Dimension dimDialog=new Dimension(dimContent.width+insetsDialog.left+insetsDialog.right,dimContent.height+insetsDialog.top+insetsDialog.bottom);
		dialog.setSize(dimDialog);
		dialog.setPreferredSize(dimDialog);*/
		final WindowComponent dialog1=new WindowComponent(d,dialog,dialog.getKnowledgeBase());
		
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SelectedTreeLeaf selected=tree.getSelectedTreeLeaf();
				if(selected!=null){
					int index=list.getSelectedIndex();
					if(index==-1){
						index=listModel.getSize();
					}else{
						index++;
					}
													
					listModel.add(index, selected);
					list.setSelectedIndex(index);
					
					dialog1.getComponent().dispose();
					
					modified=true;
				}else{
					Singleton.getInstance().getMessagesControl().showErrorMessage("Debe seleccionar una columna válida",parent.getComponent());
				}
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//dialog1.getComponent().setVisible(false);
				dialog1.getComponent().dispose();
			}
		});
		
		dialog1.setMainDialog(dialog.getMainDialog());
		dialog1.setTitle(Utils.normalizeLabel("Seleccionar Columna tabla"));
		dialog1.setLocationRelativeTo(dialog.getComponent());
		dialog1.getComponent().setVisible(true);
	}
	
	private void saveColumnsOnDatabase(
			final DefaultListModel listModel, final int idto,
			final int valueCls) throws NotFoundException,
			IncoherenceInMotorException, ApplicationException,
			IncompatibleValueException, CardinalityExceedException,
			SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException,
			SQLException, NamingException, DataErrorException,
			JDOMException, ParseException,
			OperationNotPermitedException {
		
		//Asignamos las propiedades elegidas por el usuario
		LinkedList<Value> newValues=new LinkedList<Value>();
		int size=listModel.getSize();
		for(int i=0;i<size;i++){
			SelectedTreeLeaf selected=(SelectedTreeLeaf)listModel.get(i);
			
			String[] split=selected.getPath().split("#");
			String idPropNamePath="";
			for(int j=0;j<split.length;j++){
				if(!idPropNamePath.isEmpty()){
					idPropNamePath+="#";
				}
				idPropNamePath+=kba.getPropertyNameWithoutAlias(Integer.valueOf(split[j]));
			}
			
			int idtoVal=kba.getIdClass(Constants.CLS_ORDER_WITH_FILTER);
			int idoVal=kba.createPrototype(idtoVal, Constants.LEVEL_PROTOTYPE, null, idtoUserTask, sess);
			
			kba.setValue(idoVal, kba.getIdProp("ruta_propiedad"), kba.buildValue(idPropNamePath,Constants.IDTO_STRING), null, null, idtoUserTask, sess);
			
			kba.setValue(idoVal, kba.getIdProp("orden"), kba.buildValue(i+1,Constants.IDTO_INT), null, null, idtoUserTask, sess);
			
			newValues.add(kba.buildValue(idoVal,idtoVal));
			
		}
		LinkedList<Value> oldValues=kba.getProperty(idoCol, idtoCol, kba.getIdProp("columnas"), null, idtoUserTask, sess).getValues();
		
		
		kba.setValue(idoCol, kba.getIdProp("columnas"), newValues, oldValues, null, idtoUserTask, sess);
		
		sess.commit();
	}
	
}
