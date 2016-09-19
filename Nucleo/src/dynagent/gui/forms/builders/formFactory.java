package dynagent.gui.forms.builders;
/*
 * formFactory.java
 *
 * Created on 25 de octubre de 2002, 11:17
 */

/**
 *
 * @author  A_GONZALEZ
 */

import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GProcessedForm;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IDictionaryFinder;
import gdev.gfld.GFormGroup;
import gdev.gfld.GTableColumn;
import gdev.gfld.GTableRow;
import gdev.gfld.GValue;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.naming.NamingException;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.CardMed;
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
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.forms.utils.Column;

public class formFactory {

	instance m_instance;
	
	int m_maxID=0;
	Dimension m_dim;
	int m_modo;
	Integer m_currUserRol;
	/*boolean m_queryMode=true;*/
	ArrayList listaViewDoc=new ArrayList();
	ArrayList<GProcessedForm> m_listViewForm;
	GProcessedForm m_viewForm;
	boolean m_popup;
	boolean m_filterMode=true;
	boolean m_multipleMode;
	private KnowledgeBaseAdapter m_kba;
	private ArrayList<ObjectProperty> m_tables;
	
	private static final int heightRowTable=(int)GViewBalancer.getRowHeightS(Singleton.getInstance().getGraphics());

	/** Creates a new instance of formFactory 
	 * @param alias 
	 * @param allowedConfigTable 
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException */
	public formFactory(	KnowledgeBaseAdapter kba,Session sess, Dimension dim,int modo,
			Integer idtoUserTask,Integer userRol,/*ArrayList<Integer> userRols,*/
			Integer idObjectParent/*Solo se utiliza para ocultar las inversas*/,int idObject,int idtoObject, boolean popup,	boolean filterMode, boolean multipleMode, IDictionaryFinder dictionaryFinder, HashMap<String,String> alias, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		m_filterMode=filterMode;


		m_dim= dim;
		m_modo= modo;
		m_popup=popup;
		m_multipleMode=multipleMode;
		m_kba=kba;
		/*ArrayList roles= rolProperties.getRol( m_com.m_user, m_md, ctxRoot, modo, eContextos, false );*/
		/*ArrayList<Integer> userRols=m_kba.getUserRolsUserTask(idtoUserTask);*/

		m_tables=new ArrayList<ObjectProperty>();

		//ArrayList<ArrayList<Property>> propertiesTabs=getPropertiesInTabs(idObject, userRol, idtoUserTask, sess);
		ArrayList<Property> properties=getProperties(idObject, idtoObject, userRol, idtoUserTask, sess);

		/*Integer userRol=m_kba.getUserRolRestrictive(userRols);*/

		m_listViewForm=buildForms(kba,m_dim,properties,idtoUserTask,idObjectParent,idObject,userRol,sess,m_popup,m_filterMode, m_multipleMode,m_modo,true,true,false,dictionaryFinder,m_tables,null,null,alias,allowedConfigTable);
		
		/*int size=propertiesTabs.size();
		for( int i=0; i<size; i++ ){
			String title;
			if(i==0){
				title=Utils.normalizeLabel("PRINCIPAL");
			}else{
				if( i==1 )
					title=Utils.normalizeLabel("DATOS ADICIONALES");
				else
					title=Utils.normalizeLabel("DATOS ADICIONALES "+i);
			}

			GViewBalancer balancer=build_TO_Form(m_dim,propertiesTabs.get(i),idtoUserTask,idObjectParent,idObject,userRol,sess);
			if( balancer.hasItems() ){

				// Si hay un grupo pero sin elementos en su interior se borra el grupo
				Iterator iterGrupos=balancer.getFormFieldList().getGroupList().iterator();
				while(iterGrupos.hasNext()){
					GFormGroup grupo=(GFormGroup)iterGrupos.next();
					if(grupo.getFieldList().isEmpty())
						balancer.removeGroup(grupo.getId());
				}

				balancer.process(!popup,null,null);
				if(!balancer.getProcessedFormList().isEmpty()){
					GProcessedForm formulario=balancer.getBestResult();
					//System.out.println("FORMULARIO: "+formulario.toString());
					formulario.getFormFieldList().setTitle(title);
					m_listViewForm.add(formulario);
				}

			}
		}*/
	}
	//------------Se usa para las businessClass ya que pueden aparecer muchos grupos y tenemos que dividirlos
//	/*Devuelve una lista de la lista de propiedades que deben aparecer en cada una de las pestañas*/
//	public ArrayList<ArrayList<Property>> getPropertiesInTabs(int idObject,/*ArrayList<Integer> userRols*/Integer userRol,int idtoUserTask) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException{
//	//ArrayList<Integer> userRols=null;
//	//Si userRol es distinto de null significa que queremos filtrar por ese userRol
//	//por lo que solo mostrariamos las properties de ese userRol. Si es null mostramos
//	//las properties de cada uno de esos userRols organizandolas en pestañas
//	Iterator<Property> itr=m_kba.getProperties(idObject, userRol, idtoUserTask,null);
//	LinkedHashMap<Integer,ArrayList<Property>> hashGroup=new LinkedHashMap<Integer,ArrayList<Property>>();
//	ArrayList<Integer> groups= new ArrayList<Integer>();
//	while (itr.hasNext()){
//	Property p=itr.next();
//	//System.err.println("Property en getPropertiesInTabs: "+ p.toString());
//	Integer group=m_kba.getGroup(p.getIdProp());

//	if(group==null){
//	group=0;
//	}
//	System.out.println("Grupo:"+group);
//	if(hashGroup.containsKey(group)){
//	ArrayList<Property> arrProp= hashGroup.get(group);
//	arrProp.add(p);

//	}else{
//	ArrayList<Property> arrProp= new ArrayList<Property>();
//	arrProp.add(p);
//	hashGroup.put(group, arrProp);
//	}
//	}
//	groups.addAll(hashGroup.keySet());
//	int size=groups.size();
//	System.out.println("hashGroup:"+hashGroup.toString());
//	ArrayList<LinkedHashMap<Integer,Property>> listHash=new ArrayList<LinkedHashMap<Integer,Property>>();
//	System.out.println("Size es:"+ size);
//	if(size>6){
////	Hacemos una lista con todos los campos en cada userRol 


//	int mod= size%6;
//	int tam=0;
//	if (mod==0){
//	tam=size/6;
//	}else{
//	tam=(size/6)+1;
//	}

//	for( int r=0; r<tam; r++ ){
//	System.out.println("r es :"+ r);
//	LinkedHashMap<Integer,Property> hash=new LinkedHashMap<Integer,Property>();

//	for(int i=r*6;i<(r*6)+6 && groups.size()>i;i++){
//	System.out.println("Iteracion tab:"+i);
//	ArrayList<Property> aprop=hashGroup.get(groups.get(i));
//	Iterator<Property> itrpropgroup= aprop.iterator();
//	while(itrpropgroup.hasNext()){
//	Property prop=itrpropgroup.next();
//	System.out.println("property userRol "+r+" para formFactory:"+prop);
//	// Comprobamos si tenemos permiso de lectura ya que aunque RuleEngine solo me devuelve
//	// los que tengo permiso de lectura, PARECE que tambien me va a devolver los que tienen
//	//  el permiso especial de filtro. Con esta comprobacion nos aseguramos.
//	//if(prop.getTypeAccess().getViewAccess()){
//	int idProp=prop.getIdProp();
//	hash.put(idProp, prop);
//	//}
//	}

//	}
//	listHash.add(hash);

//	}
//	}else{
//	LinkedHashMap<Integer,Property> hash=new LinkedHashMap<Integer,Property>();
//	itr=m_kba.getProperties(idObject, userRol, idtoUserTask,null);
//	while(itr.hasNext()){
//	Property prop=itr.next();
//	// Comprobamos si tenemos permiso de lectura ya que aunque RuleEngine solo me devuelve
//	// los que tengo permiso de lectura, PARECE que tambien me va a devolver los que tienen
//	//  el permiso especial de filtro. Con esta comprobacion nos aseguramos.
//	System.out.println("property para formFactory:"+prop);
//	//if(prop.getTypeAccess().getViewAccess()){
//	int idProp=prop.getIdProp();
//	hash.put(idProp, prop);
//	//}
//	}
//	System.out.println(hash.toString());
//	listHash.add(hash);

//	//size=1;
//	}

//	/*if(userRol==null){
//	userRols=m_kba.getUserRols(m_kba.getIdoUserTask(idtoUserTask),null);
//	size=userRols.size();
//	}

//	ArrayList<LinkedHashMap> listHash=new ArrayList<LinkedHashMap>();
//	if(size>0){

//	//Hacemos una lista con todos los campos en cada userRol 
//	for( int r=0; r<size; r++ ){
//	LinkedHashMap<Integer,Property> hash=new LinkedHashMap<Integer,Property>();
//	int userR=(Integer)userRols.get(r);
//	Iterator<Property> itr=m_kba.getProperties(idObject, userR, idtoUserTask,null);
//	while(itr.hasNext()){
//	Property prop=itr.next();
//	System.out.println("property userRol "+r+" para formFactory:"+prop);
//	// Comprobamos si tenemos permiso de lectura ya que aunque RuleEngine solo me devuelve
//	// los que tengo permiso de lectura, PARECE que tambien me va a devolver los que tienen
//	//  el permiso especial de filtro. Con esta comprobacion nos aseguramos.
//	//if(prop.getTypeAccess().getViewAccess()){
//	int idProp=prop.getIdProp();
//	hash.put(idProp, prop);
//	//}
//	}
//	listHash.add(hash);
//	}
//	}else{
//	LinkedHashMap<Integer,Property> hash=new LinkedHashMap<Integer,Property>();
//	Iterator<Property> itr=m_kba.getProperties(idObject, null, idtoUserTask, null);
//	while(itr.hasNext()){
//	Property prop=itr.next();
//	// Comprobamos si tenemos permiso de lectura ya que aunque RuleEngine solo me devuelve
//	// los que tengo permiso de lectura, PARECE que tambien me va a devolver los que tienen
//	//  el permiso especial de filtro. Con esta comprobacion nos aseguramos.
//	System.out.println("property para formFactory:"+prop);
//	//if(prop.getTypeAccess().getViewAccess()){
//	int idProp=prop.getIdProp();
//	hash.put(idProp, prop);
//	//}
//	}
//	System.out.println(hash.toString());
//	listHash.add(hash);

//	size=1;
//	}
//	*/
//	ArrayList<ArrayList<Property>> listAll=new ArrayList<ArrayList<Property>>();
//	for (int i=0;i<listHash.size();i++){
//	listAll.add(new ArrayList<Property>(listHash.get(i).values()));
//	}
//	System.out.println("lista de tabs:"+listAll+" "+listAll.size());

//	/*if(size==2){
//	// Si tenemos dos userRols los distribuimos de manera que no aparezcan las mismas properties en distintas pestañas.
//	// Si no tienen campos en comun mostramos en la primera pestaña el userRol con mas properties
//	LinkedHashMap list1=listHash.get(0);
//	LinkedHashMap list2=listHash.get(1);

//	boolean list1InList2=list2.keySet().containsAll(list1.keySet());//Si todos los campos de 1 estan en 2
//	boolean list2InList1=list1.keySet().containsAll(list2.keySet());//Si todos los campos de 2 estan en 1

//	if(list1InList2 || (!list2InList1 && list1.size()>=list2.size())){
//	listAll.add(new ArrayList<Property>(list1.values()));
//	list2.keySet().removeAll(list1.keySet());//Le quitamos los campos de 1 si los hubiera
//	listAll.add(new ArrayList<Property>(list2.values()));
//	}else{
//	listAll.add(new ArrayList<Property>(list2.values()));
//	list1.keySet().removeAll(list2.keySet());//Le quitamos los campos de 2 si los hubiera
//	listAll.add(new ArrayList<Property>(list1.values()));
//	}

//	}else{
//	//Si no tenemos dos userRols mostramos las properties de cada uno en pestañas distintas
//	for( int i=0; i<size; i++ ){
//	listAll.add(new ArrayList<Property>(listHash.get(i).values()));
//	}
//	}*/

//	return listAll;
//	}

	/*Devuelve una lista de la lista de propiedades que deben aparecer en cada una de las pestañas*/
	public ArrayList<ArrayList<Property>> getPropertiesInTabs(int idObject,int idtoObject,/*ArrayList<Integer> userRols*/Integer userRol,int idtoUserTask,Session sess,KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		//ArrayList<Integer> userRols=null;
		//Si userRol es distinto de null significa que queremos filtrar por ese userRol
		//por lo que solo mostrariamos las properties de ese userRol. Si es null mostramos
		//las properties de cada uno de esos userRols organizandolas en pestañas
		Iterator<Property> itr=m_kba.getProperties(idObject, idtoObject, userRol, idtoUserTask,sess);
		ArrayList<Property> listPropWithoutGroupIn1=new ArrayList<Property>();
		ArrayList<ArrayList<Property>> listTabs=new ArrayList<ArrayList<Property>>();
		listTabs.add(new ArrayList<Property>());
		listTabs.add(new ArrayList<Property>());
		int numProperties=0;
		int sizeTabs0=0;
		int sizeTabs1=0;
		int sizeTabs1WithoutGroup=0;
		while (itr.hasNext()){
			Property p=itr.next();
			if(p.getTypeAccess().getViewAccess()){
				int numRows=calculateNumRowsComponent(p,userRol,idtoUserTask,sess,kba);

				if(p.getCardMin()!=null && p.getCardMin()>0){
					listTabs.get(0).add(p);
					sizeTabs0+=numRows;
				}else{
					listTabs.get(1).add(p);
					if(m_kba.getGroup(p.getIdProp(),p.getIdto(),idtoUserTask)==null){
						listPropWithoutGroupIn1.add(p);
						sizeTabs1WithoutGroup+=numRows;
					}
					sizeTabs1+=numRows;
				}
				numProperties+=numRows;
			}
		}

		if(numProperties<35){//Si es menor que la cantidad solo mostramos una pestaña
			listTabs.get(0).addAll(listTabs.get(1));
			listTabs.remove(1);
		}else{
			//int sizeTabs0=listTabs.get(0).size();
			//int sizeTabs1=listTabs.get(1).size();

			if(sizeTabs0<sizeTabs1){//Si el tamaño de la principal es menor que el adicional mostramos ciertas properties sin grupo ordenadas por prioridad 
				//int num=Math.min(sizeTabs1-sizeTabs0,/*listPropWithoutGroupIn1.size()*/sizeTabs1WithoutGroup);
				ArrayList<Property> listTab0=listTabs.get(0);
				ArrayList<Property> listTab1=listTabs.get(1);
				m_kba.buildOrderForm(idObject,listPropWithoutGroupIn1,idtoUserTask);
				Collections.sort(listPropWithoutGroupIn1,new Comparator<Object>() {

					public int compare(Object o1, Object o2) {
						Property p1 = (Property) o1;
						Property p2 = (Property) o2;
						return Integer.valueOf(m_kba.getPriority(p2, 0)).compareTo(m_kba.getPriority(p1, 0));
					}

					public boolean equals(Object o) {
						return this == o;
					}
				}
				);
				int i=0;
				int differenceLast=sizeTabs1-sizeTabs0;
				boolean exit=false;
				while(sizeTabs1WithoutGroup>0 && !exit){
					Property p=listPropWithoutGroupIn1.get(i);

					int numRows=calculateNumRowsComponent(p,userRol,idtoUserTask,sess,kba);

					sizeTabs1WithoutGroup-=numRows;
					sizeTabs0+=numRows;
					sizeTabs1-=numRows;

					//Solo se cambia de pestaña cuando el tamaño de tab1 sea mayor que el de tab0
					//Teniendo en cuenta con differenceLast que, en la ultima iteracion(cuando sizeTab0>sizeTab1), llevandonos la property a tabs0 sea mas equitativo que dejandola en tab1
					if(sizeTabs0<=sizeTabs1 || (sizeTabs0>sizeTabs1 && differenceLast>=Math.abs(sizeTabs0-sizeTabs1))){
						listTab0.add(p);
						listTab1.remove(p);

						//System.err.println("sizeTab0:"+sizeTabs0);
						//System.err.println("sizeTab1:"+sizeTabs1);
						//System.err.println("sizeTab1WihoutGroup:"+sizeTabs1WithoutGroup);

						differenceLast=sizeTabs1-sizeTabs0;
						i++;
					}else{
						exit=true;
					}
				}
//				for(int i=0;i<num;i++){
//				Property p=listPropWithoutGroupIn1.get(i);
//				listTab0.add(p);
//				listTab1.remove(p);
//				//if(isTable(p) && (p.getCardMax()==null || p.getCardMax()>1))
//				//	num-=2;
//				numProperties-=(calculateNumRowsComponent(p)-1/*Debido a que ya se resta 1 en el bucle*/);
//				}
			}
		}

		return listTabs;
	}

	public ArrayList<Property> getProperties(int idObject,int idtoObject,/*ArrayList<Integer> userRols*/Integer userRol,Integer idtoUserTask,Session sess) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<Property> properties = new ArrayList<Property>();
		Iterator<Property> itr=m_kba.getProperties(idObject, idtoObject, userRol, idtoUserTask,sess);
		boolean hasProperties=false;
		while (itr.hasNext()){
			hasProperties=true;
			Property p=itr.next();
			if(p.getTypeAccess().getViewAccess())
				properties.add(p);
		}

		if(hasProperties){
			if(properties.isEmpty()){
				OperationNotPermitedException ex=new OperationNotPermitedException("No tiene permiso para ver este formulario del ido:"+idObject+" idto:"+idtoObject);
				ex.setUserMessage("No tiene permiso para ver este formulario");
				throw ex;
			}
		}else{
			OperationNotPermitedException ex=new OperationNotPermitedException("getProperties no devuelve ninguna property para el ido:"+idObject+" idto:"+idtoObject);
			ex.setUserMessage("El formulario que se quiere consultar no está disponible");
			throw ex;
		}
		return properties;
	}


	private int calculateNumRowsComponent(Property p, Integer userRol, Integer idtoUserTask, Session sess,KnowledgeBaseAdapter kba) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		int num=0;
		if(isTable(p)){
			int numRows=(calculateNumRowsVisibleTable(p.getIdto(), kba.getClass(kba.getIdRange((ObjectProperty)p)),p.getCardMin(), p.getCardMax(), kba));

			//TODO Con esto tendriamos en cuenta el numero de columnas de las tablas.
			//numRows*=m_kba.getColumnsObject(p.getIdo(), userRol, idtoUserTask, sess, m_filterMode).size();

			num=numRows;
		}else if(p instanceof DataProperty && ((DataProperty)p).getDataType()==Constants.IDTO_MEMO){
			num=(calculateNumRowsVisibleMemo(p.getIdto(), p.getIdProp() ,p.getCardMin(), p.getCardMax(), kba));
		}else num=1;

		if(num>1)
			num++;//Debido al espacio de la etiqueta

		return num;
	}

//	private int numberOfGroup(Iterator<Property> itr) {
//	// TODO Auto-generated method stub
//	int numGroups=0;
//	ArrayList<Integer> groupList= new ArrayList<Integer>();
//	while (itr.hasNext()){
//	Property p= itr.next();
//	int group=m_kba.getGroup(p.getIdProp());
//	if (!groupList.contains(group)){
//	groupList.add(group);
//	numGroups++;
//	}
//	}
//	return numGroups;
//	}

	public ArrayList getListViewForm(){
		return m_listViewForm;
	}

	public ArrayList<ObjectProperty> getListTables(){
		return m_tables;
	}


	public int getMaxID(){
		return m_maxID;
	}

	/*private static void buildDomProperties(Element dom, HashMap props) throws NumberFormatException{
		props.clear();
		if( dom==null || dom.getChild("PROPERTY_LIST")==null) return;
		Iterator itr= dom.getChild("PROPERTY_LIST").getChildren("ATRIBUTO").iterator();
		while(itr.hasNext()){
			Element at= (Element) itr.next();
			props.put( new Integer(at.getAttributeValue("TA_POS")), at);
		}
	}*/


	private static ArrayList<GProcessedForm> buildForms(KnowledgeBaseAdapter kba,Dimension dim,ArrayList<Property> properties,Integer idtoUserTask,Integer idObjectParent,int idObject,Integer userRol,Session sess, boolean popup,boolean filterMode, boolean multipleMode, int mode, boolean alias, boolean groups, boolean keepOrder, IDictionaryFinder dictionaryFinder, ArrayList<ObjectProperty> tablesFound, Insets margenesPanel, Insets margenesGrupo, HashMap<String,String> aliasMap, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<GProcessedForm> listViewForm=new ArrayList<GProcessedForm>();
		GViewBalancer balancer=build_TO_Form(kba,dim,properties,idtoUserTask,idObjectParent,idObject,userRol,sess,filterMode, multipleMode,mode,alias,groups,keepOrder,dictionaryFinder,tablesFound,aliasMap,allowedConfigTable);
		if( balancer.hasItems() ){

			// Si hay un grupo pero sin elementos en su interior se borra el grupo
			Iterator iterGrupos=balancer.getFormFieldList().getGroupList().iterator();
			while(iterGrupos.hasNext()){
				GFormGroup grupo=(GFormGroup)iterGrupos.next();
				if(grupo.getFieldList().isEmpty())
					balancer.removeGroup(grupo.getId());
			}
			balancer.process(!popup,margenesPanel,margenesGrupo);
			if(!balancer.getProcessedFormList().isEmpty()){
				GProcessedForm formulario=balancer.getBestResult();
				//System.out.println("FORMULARIO: "+formulario.toString());
				listViewForm.add(formulario);
			}

		}
		
		return listViewForm;
	}
	/*
	 * Crea los campos del formulario a partir de las properties. Los que son DataProperty u ObjectProperty enumerados son creados
	 * como campos normales mediante buildField. Si se trata de un ObjectProperty que no es enumerado se crea una tabla mediante
	 * buildTable, siempre que no estemos en modo filter
	 */
	private static GViewBalancer build_TO_Form(KnowledgeBaseAdapter kba,Dimension dim,ArrayList<Property> properties,Integer idtoUserTask,Integer idObjectParent,int idObject,Integer userRol,Session sess,boolean filterMode, boolean multipleMode, int mode, boolean alias, boolean groups, boolean keepOrder, IDictionaryFinder dictionaryFinder, ArrayList<ObjectProperty> tablesFound, HashMap<String,String> aliasMap, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		/*Singleton.getInstance().getOrderComponents()*/kba.buildOrderForm(idObject,properties,idtoUserTask);
		/*int pos = 0;*/
		/*Iterator<Property> itrProps=m_kba.getPropertiesTarget(idTarget,userRol,user,idtoUserTask);*/
		Iterator<Property> itrProps=properties.iterator();
		/*Integer idTO = new Integer(m_to.getAttributeValue("ID_TO"));*/
		
		GViewBalancer balancer = newBalancer(dim, filterMode);

		boolean addValues=!filterMode;
		
		int i=1;
		while(itrProps.hasNext()){
			Property prop=itrProps.next();
			if(idObjectParent==null || !kba.isInverse(idObjectParent,prop)){//Si la property es inversa de idObjectParent no nos interesa mostrarla en el formulario
				Integer idGroup=null;
				if(groups)
					idGroup=kba.getGroup(prop.getIdProp(),prop.getIdto(),idtoUserTask);
				String nameGroup;
				if(idGroup==null){
					idGroup=0;
					nameGroup="DETALLE";
				}else{
					//System.err.println("id del grupo:" +idGroup);
					//System.err.println("idProp:" +prop.getIdProp());
					//System.err.println("property: "+prop.toString());
					nameGroup=kba.getLabelGroup(idGroup,idtoUserTask);
				}
				int orderGroup=/*Singleton.getInstance().getOrderComponents()*/kba.getOrderGroup(idGroup);

				//Si el id del grupo ya existe, addGroup no crea un grupo nuevo, asi que no necesitamos mantener una referencia
				//de los grupos ya creados para no volver a crearlos. addGroup lo comprueba antes de insertar. Ademas si metemos un grupo
				//al que luego no le añadimos ninguna property ese grupo es descartado internamente
				balancer.addGroup(idGroup, Utils.normalizeLabel(nameGroup), orderGroup);
				/*int codGroup=balancer.addGroup(idGroup, Utils.normalizeLabel(nameGroup), orderGroup));
				System.out.println("Codigo addGrupo:"+codGroup);*/


				if(isField(prop)){
					if( ! (filterMode && prop instanceof DataProperty && (((DataProperty)prop).getDataType()==Constants.IDTO_IMAGE || ((DataProperty)prop).getDataType()==Constants.IDTO_FILE)))
						buildField(balancer, kba, idtoUserTask, idObject, prop, userRol, idGroup/*idTO, idpk,*/ /*root,*/ /*iAtr*/,sess, filterMode, mode, alias, multipleMode, keepOrder?i:null, aliasMap);
				}else if(!filterMode && isTable(prop) && (!multipleMode || !kba.getCategoryProperty(prop.getIdProp()).isStructural())){
					access access=prop.getTypeAccess();
					/*if(!access.getConcreteAccess()){
						int numClasses=((ObjectProperty)prop).getRangoList().size();
						for(int i=0;i<numClasses;i++){
							buildFieldsNoConcrete(balancer,
				    				/*accessStr,*/
					/*				idtoUserTask,
				    				idObject,
				    				(ObjectProperty)prop,
				    				userRol,
				    				access,
				    				idGroup,
				    				-1,
				    				true,
				    				false,
				    				null);
						}
					}else{
					 */	
					buildTable(	/*root,
			    				filterRel,
			    				metaRolChild,*/
							/*metaRolChild.getAttributeValue("ROL_NAME"),*/
							/*filterRel.getAttributeValue("FILTER_NAME"),*/
							balancer,
							kba,
							/*accessStr,*/
							idtoUserTask,
							idObject,
							(ObjectProperty)prop,
							null,
							userRol,
							access,
							idGroup,
							-1,
							true,
							false,
							filterMode,
							null,
							null,
							null,
							false,
							multipleMode,
							sess, -1, dictionaryFinder,addValues,aliasMap,allowedConfigTable);

					tablesFound.add((ObjectProperty)prop);
					/*	}*/

				}
			}
			
			i++;
			
		}
		return balancer;
	}

	public static boolean isField(Property property){
		return (property instanceof DataProperty /*&& (((DataProperty)prop).getDataType()!=Constants.IDTO_MEMO ||!m_filterMode) */) || (property instanceof ObjectProperty && ((ObjectProperty)property).getEnumList().size()>0);
	}

	public static boolean isTable(Property property){
		return !isField(property);
	}

	private static GViewBalancer newBalancer(Dimension dim,boolean queryMode){
		Object graphics = Singleton.getInstance().getGraphics();
		/*if( graphics==null || !(graphics instanceof Graphics2D))
			System.out.println(" GRAPHICS NULO o no 2D");*/

		/*Graphics2D gr2D=(Graphics2D)m_graphics;
	FontRenderContext frc= gr2D.getFontRenderContext();
	Font font= new  Font("Dialog",  Font.PLAIN,  12);//gr2D.getFont();*/
		Graphics2D gr2D = (Graphics2D) graphics;
		gr2D.setFont(UIManager.getFont("Label.font"));
		FontRenderContext frc = gr2D.getFontRenderContext();
		Font font= /*new  Font("Dialog",  Font.PLAIN,  12);*/gr2D.getFont();;

		/*if( frc==null )
			System.out.println(" FONT RENDER NULO");*/

		/*if( font==null )
			System.out.println(" FONT NULO");*/

		// Now instatiating viewBalancer. Parameter INPUT_FORM is needed in order to know that output doc will be used
		// as a form (could be used as an html report). At this project scope we always will fix this value to INPUT_FORM

		return new GViewBalancer(dim,/*font, frc,*/gr2D,queryMode);
	}


//	private void setDimensiones( Element form ){
//	int width= 0, height=0;


//	if( form.getChild("TABED")!=null ){
//	//System.out.println("TABBED");
//	Iterator itr= form.getChild("TABED").getChildren("PAGE").iterator();
//	while( itr.hasNext()){
//	Element page=(Element)itr.next();
//	int currWidth= Integer.parseInt( page.getAttributeValue("WIDTH"));
//	int currHeight= Integer.parseInt( page.getAttributeValue("HEIGHT"));
//	width= Math.max( width, currWidth );
//	height= Math.max( height, currHeight );

//	page.setAttribute("WIDTH",String.valueOf(currWidth + cfgView.tabbH));
//	page.setAttribute("HEIGHT",String.valueOf(currHeight + cfgView.tabbV));

//	}
//	form.setAttribute("WIDTH",String.valueOf(width + cfgView.tabbH));
//	form.setAttribute("HEIGHT",String.valueOf(height + cfgView.tabbV));
//	}
//	}


	private static void buildField( GViewBalancer balancer, KnowledgeBaseAdapter kba,Integer idtoUserTask, int idParent, Property property, Integer userRol, int idGroup,Session sess, boolean filterMode, int mode, boolean alias, boolean multipleMode, Integer order, HashMap<String,String> aliasMap) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{

		// Es la longitud que opcionalmente redefinimos en el archivo meta
		/*int longitud= eAtr.getAttributeValue("LENGTH")==null ? -1:Integer.parseInt(eAtr.getAttributeValue("LENGTH"));*/
		int longitud=-1;//property.getLength();
		String mask=null;
		if(property instanceof DataProperty && !filterMode){
			Integer length=kba.getPropertyLength(property.getIdProp(),property.getIdto(),idtoUserTask);
			if(length!=null)
				longitud=length;
			mask=kba.getPropertyMask(property.getIdProp(),property.getIdto(),idtoUserTask);
			
			if(longitud!=-1 && mask==null){
				mask=".{"+longitud+"}";
			}
		}
		int idProp=property.getIdProp();
		String id;
		String label;
		String name=property.getName();
		//System.out.println("Property buildField:"+property);
		//if(idProp==Constants.IdPROP_RDN)
		//	name="Código";
		//else{
		
		if(alias){
			if(property instanceof ObjectProperty){
	//			int idto=-1;
	//			if(m_filterMode){
	//			Integer idtoAux=m_kba.getIdtoFilter((ObjectProperty)property);
	//			if(idtoAux==null)//Si no tiene filtro es que se trata de un enumerado
	//			idtoAux=m_kba.getIdtoEnum((ObjectProperty)property);
	//			idto=idtoAux;
	//			}
	//			else{ //idto=m_kba.getIdtoObject((ObjectProperty)property);
	//			LinkedList<Integer> rangoList=((ObjectProperty)property).getRangoList();
	//			if(rangoList.size()>0)
	//			idto=rangoList.getFirst();
	//			else{
	//			// Si no tiene rango puede tratarse de un enumerado
	//			LinkedList<ObjectValue> enumList=((ObjectProperty)property).getEnumList();
	//			if(enumList.size()>0)
	//			idto=enumList.getFirst().getValueCls();
	//			}
	//			}
				Integer idto=kba.getIdRange((ObjectProperty)property);
				if(idto==null){// Si no tiene rango puede tratarse de un enumerado
					idto=kba.getIdtoEnum((ObjectProperty)property);
				}
				//System.out.println("buildField:"+idto+" "+property.toString());
				//name=m_kba.getLabel(idto).trim();
				if(aliasMap==null || !aliasMap.containsKey(name)){
					label=kba.getLabelProperty(property, property.getIdto(), idtoUserTask).trim();
				}else{
					label=aliasMap.get(name);
				}
			}else{
				if(aliasMap==null || !aliasMap.containsKey(name)){
					label=kba.getLabelProperty(property, property.getIdto(), idtoUserTask).trim();
				}else{
					label=aliasMap.get(name);
				}
			}
		}else{
			label=name;
		}
		
		label=Utils.normalizeLabel(label);
		
		/*if(property.getIdo()!=idParent)
			name+=" de "+kba.getLabelClass(property.getIdto(), idtoUserTask);*/
			
		//}
		int fieldType=-1;
		//System.out.println("FORMAT:TA_POS,NAME "+idProp+","+name);
		/*if( intTapos.intValue()==helperConstant.TAPOS_METATIPO ||
		    intTapos.intValue()==helperConstant.TAPOS_TASK_TYPE ||
		    intTapos.intValue()==helperConstant.TAPOS_OBJECT_FILTER )
		    continue;*/

		/*Element prop= (Element)m_map_TAPOS_Property.get(intTapos);*/
		access acceso=property.getTypeAccess();
		// DATA ITEMS ////////////////////////////////////////////////////////
		/*int tm= Integer.parseInt(eAtr.getAttributeValue("ID_TM"));*/
		/*int dataType=-1;*/

		int dataType=-1;
		boolean directoryType=false;
		if(property instanceof DataProperty){
			dataType=((DataProperty)property).getDataType();
			fieldType=getTypeField(dataType);
			if(idProp==Constants.IdPROP_FILE){
				fieldType=GConst.TM_FILE;
				if(idtoUserTask!=null && kba.isSpecialized(idtoUserTask,Constants.IDTO_EXPORT))
					directoryType=true;
			}
			//if(((DataProperty)property).getEnumList().size()>0)
			//	fieldType=GConst.TM_ENUMERATED;
		}else{
			if(((ObjectProperty)property).getEnumList().size()>0){
				dataType=kba.getIdtoEnum((ObjectProperty)property);
				fieldType=GConst.TM_ENUMERATED;
			}
		}

		if( fieldType==/*helperConstant.TM_IMAGEN*/GConst.TM_IMAGE && /*mode==access.VIEW*/filterMode )
			return;//continue;

		/*if( prop!=null &&
		    (prop.getAttributeValue("ACCESS").length()==0 ||
		     prop.getAttributeValue("ACCESS").indexOf("FORBIDEN")>=0)) continue;*/
		//////////////////////////////////////////////////////////////////////

		boolean enable=true;
		/*		if(	m_modo!=access.VIEW &&
			m_modo!=appControl.FILTRADO_INTERNO &&
			idProp== Constants.IdPROP_RDN ){
			System.out.print("ENABLE1 FALSE");
                        enable = false;
                }
		 */
//		if(	m_modo!=access.VIEW/* &&
//		m_modo!=appControl.FILTRADO_INTERNO*/ /*&&
//		eAtr.getAttributeValue("NULLABLE")!=null */){
//		Integer cardMin=property.getCardMin();		
//		nullable=cardMin==null || cardMin==0;
//		}


		/*if( m_modo== appControl.NO_AT_WRITE ){
		    System.out.print("ENABLE2 FALSE");
                    enable = false;
                }*/
		boolean visible=true;
		int numBotones=0;

		Integer cardMin=property.getCardMin();
		Integer cardMax=property.getCardMax();

		boolean nullable=multipleMode || (cardMin==null || cardMin.intValue()==0);// || ( cardMin!=null && (defaultVal==null || defaultVal.equals("")));
		boolean multivalued=(cardMax==null || cardMax>1);
		
		if(!filterMode && !nullable && label!=null && !label.isEmpty()){
			label="*"+label;
		}
		
		if( fieldType==/*helperConstant.TM_IMAGEN*/GConst.TM_IMAGE){
			numBotones=1;//int accessOp=access.VIEW;
			if(multivalued)
				numBotones+=2;//Anterior-Siguiente
			if( acceso.getSetAccess() && mode!=access.VIEW/*!m_queryMode && !m_modoFilter*/ )
				numBotones+=2;//accessOp= accessOp | access.NEW | access.DEL;
		}
		
		int rows=1;
		if( fieldType==/*helperConstant.TM_IMAGEN*/GConst.TM_MEMO && !filterMode){
			rows=calculateNumRowsVisibleMemo(property.getIdto(), idProp, cardMin, cardMax, kba);
		}

		/*if(prop!=null){
			jdomParser.print("FCTX PROPER:",prop);*/
		/*if(	prop.getAttributeValue("ACCESS")!=null &&
				prop.getAttributeValue("ACCESS").equals("FILTER") &&
				m_modo!=access.VIEW )
				visible=false;*/

		/*	if(	prop.getAttributeValue("ACCESS")!=null &&
				prop.getAttributeValue("ACCESS").indexOf("NOREAD")!=-1){

      	            		continue;
			}*/

		if(	/*m_modo!=appControl.OBJ_SELECTION &&*/
				mode!=access.VIEW &&
				/*m_modo!=appControl.FILTRADO_INTERNO &&*/
				/*prop.getAttributeValue("ACCESS")!=null &&
				prop.getAttributeValue("ACCESS").indexOf("READ")!=-1 &&
				prop.getAttributeValue("ACCESS").indexOf("WRITE")==-1 ){*/
				acceso.getViewAccess() && !acceso.getSetAccess()){
			//System.out.print("ENABLE3 FALSE");
			enable = false;
			//System.out.println("access:"+acceso.toString());
		}/*else if(filterMode && !property.getValues().isEmpty()){//Si estamos en modo filtro y ya tiene un valor no permitimos que el usuario lo cambie ya que estara fijado por una regla
			enable = false;
		}*/
		/*				if(	m_modo!=access.VIEW &&
						m_modo!=appControl.FILTRADO_INTERNO /*&&
						eAtr.getAttributeValue("NULLABLE")!=null *//*)
							nullable=property.getCardMin()>0;

            	}*/
		// VIEW ITEMS ////////////////////////////////////////////////////////
		//String type="";
//		int prioridad= idProp==Constants.IdPROP_RDN ? 5:0;
//		if( /*tm==helperConstant.TM_FECHA||tm==helperConstant.TM_FECHAHORA*/fieldType==GConst.TM_DATE||fieldType==GConst.TM_DATE_HOUR ) prioridad=2;
//		if( /*tm==helperConstant.TM_ENUMERADO*/fieldType==GConst.TM_ENUMERATED ) prioridad=3;
//		//if( idProp==helperConstant.TAPOS_ESTADO ) prioridad=3;
		boolean comentado = false;//(fieldType==GConst.TM_BOOLEAN_COMMENTED || fieldType==GConst.TM_BOOLEAN) && !filterMode && (idtoUserTask==null || !kba.isSpecialized(idtoUserTask, Constants.IDTO_REPORT));

		int prioridad=/*Singleton.getInstance().getOrderComponents()*/kba.getPriority(property,idGroup);
		/*boolean isTO= group.intValue()==0;
		Integer idContainer= isTO ? idTO:group;
		int order= m_md.getFormOrder( idContainer, idProp, isTO, ordenadorAts.AT_ELEM );
		boolean multivalued=filterModeAt.containsKey(idProp) || m_filterMode;*/
		if(order==null){
			order=/*Singleton.getInstance().getOrderComponents()*/kba.getOrder(idProp, idGroup);
		}

		Object defaultVal;
		/*if(m_instance!=null){
			System.out.println("Atributo:"+m_instance.getAttributeValue(idProp, 0));

			attribute ava= (attribute)m_instance.getAttribute(idProp, 0);
			/*int tm=ava.getMemberType();*/
		/*			
			if(ava!=null){
				if(	tm==helperConstant.TM_TEXTO ||
				   	tm==helperConstant.TM_MEMO  ||
					tm==helperConstant.TM_IMAGEN )
					defaultVal=ava.getValue().toString();
				else if(tm==helperConstant.TM_BOOLEANO_EXT ||
					tm==helperConstant.TM_BOOLEANO){
				    if( ava.getValue() instanceof Boolean )
				    	defaultVal=helperConstant.valueToString(tm,ava.getValue());
				    if( ava.getValue() instanceof extendedValue )
				    	defaultVal=helperConstant.valueToString(tm,ava.getValue());
				}else{
				    //jdomParser.print("AVA",ava);
						defaultVal=helperConstant.valueToString(tm,ava.getValue());
		        }
			}
		}*/

		if(property instanceof DataProperty){
			/*LinkedList valueList= ((DataProperty)property).getValueList();
			if(valueList.size()>0){
				DataValue dataValue=(DataValue)valueList.get(0);
         		if(dataValue.getValueMin()==-1 && dataValue.getValueMax()==-1)
         				defaultVal=(String)dataValue.getValue();
				   	else defaultVal=String.valueOf(dataValue.getValueMin());
         	}*/
			//if(!m_filterMode || idProp !=Constants.IdPROP_RDN)
			defaultVal=kba.getValueData((DataProperty)property);
		}else{
			//Los enumerados
			//System.out.println("ObjectProperty en buildField"+((ObjectProperty)property).toString());
			Iterator<Integer> itrValues=kba.getIdoValues((ObjectProperty)property).iterator();
			boolean first=true;
			String defaultV="";
			while(itrValues.hasNext()){
				if(!first)
					defaultV+=";";
				else first=false;
				defaultV+=itrValues.next().toString();
			}
			defaultVal=defaultV;
		}

		//System.out.println("Property en buildField: "+name+" "+defaultVal+ "nullable:"+nullable);

		Vector<GValue> vValues = null;
		if (fieldType==GConst.TM_ENUMERATED/*tm == helperConstant.TM_ENUMERADO*/)//(tm == GConst.TM_ENUMERATED)
		{
			vValues = new Vector<GValue>();
			/*Iterator iVal= m_md.getEnumSet(intTapos);*/
			if(property instanceof DataProperty){
				LinkedList<DataValue> enumItems= ((DataProperty)property).getEnumList();
				int size=enumItems.size();
				for(int i=0;i<size;i++){
					DataValue dataValue=(DataValue)enumItems.get(i);
					String labelEnum;
					/*if(dataValue.getValueMin()==-1 && dataValue.getValueMax()==-1)
 					   label=(String)dataValue.getValue();
 				   	else label=String.valueOf(dataValue.getValueMin());*/
					Object labelValue=kba.getValueData(dataValue);
					labelEnum=labelValue!=null?labelValue.toString():null;
					vValues.addElement(new GValue(-1,labelEnum));
				}
			}else{/*ObjectProperty*/
				LinkedList<ObjectValue> enumItems= ((ObjectProperty)property).getEnumList();
				int size=enumItems.size();
				kba.setInstance(null);
				try{
					for(int i=0;i<size;i++){
						ObjectValue objectValue=(ObjectValue)enumItems.get(i);
						DataProperty propertyRDN=kba.getRDN(objectValue.getValue(), objectValue.getValueCls(), userRol, idtoUserTask, sess);
						String labelEnum=(String)kba.getValueData(propertyRDN);
						int idObject=((Integer)objectValue.getValue()).intValue();
						vValues.addElement(new GValue(idObject,Utils.normalizeLabel(labelEnum)));
					}
				}finally{
					kba.clearInstance();
				}
			}
		}
		
		boolean highlighted=!filterMode && kba.isEssentialProperty(idtoUserTask, property.getIdto(), idProp);

		Integer redondeo=GConfigView.redondeoDecimales;
			
		/*idPropMap=Singleton.getInstance().getPropertiesMap().addProperty(idObject,property.getIdProp(),property.getDataType());*/
		IdObjectForm idObjectForm=new IdObjectForm();
		idObjectForm.setIdo(/*idParent*/property.getIdo());
		idObjectForm.setIdProp(idProp);
		idObjectForm.setValueCls(dataType);
		id=idObjectForm.getIdString();


		int codigo=balancer.addItem(idGroup,
				/*tm*/fieldType,
				id,
				/*idTO.toString(),*/String.valueOf(idParent),
				prioridad,
				/*eAtr.getAttributeValue("MASK"),*/mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				highlighted,
				directoryType,
				rows,
				redondeo
		);

		/*System.out.println("balancer.addItem("+idGroup+","+
				fieldType+","+id+","+
				String.valueOf(idParent)+","+
				idProp+","+
				prioridad+","+
				mask+","+
				enable+","+
				nullable+","+
				multivalued+","+
				defaultVal+","+
				comentado+","+
				Utils.normalizeLabel(name)+","+
				longitud+","+
				comentado+","+
				order+","+
				visible+","+
				numBotones+","+
				vValues+",");*/
		if(codigo!=0)
			System.out.println("ERROR:Item no añadido, codigo de error:"+codigo);
		/*	}*/
	}

	/*private void buildFieldsNoConcrete( GViewBalancer balancer, Integer idtoUserTask, int idParent, ObjectProperty property, Integer userRol, access access,int idGroup,int rows, boolean showLabel, boolean forzarMostrarCabecera,String idBotonFilas)
	throws SystemException, NotFoundException, IncoherenceInMotorException{

		int typeField=GConst.TM_INTEGER;
		Vector vValues = null;
		boolean comentado = false;
		String name=m_kba.getLabelProperty(property, property.getIdto(),idGroup,idtoUserTask).trim();;
		int idProp;
		String defaultVal="";
		int longitud=-1;
		String id;
		int order;
		int priority;
		boolean enable;
		boolean visible=true;
		boolean multivalued=false;
		boolean nullable;
		int numBotones=0;

		enable=true;
		nullable=true;

		idProp=property.getIdProp();
		if(	m_modo!=access.VIEW &&

				idProp== Constants.IdPROP_RDN ){
			System.out.print("ENABLE1 FALSE");
			enable = false;
		}
		if(	m_modo!=access.VIEW )
			nullable=property.getCardMin()>0;
			if(	
					m_modo!=access.VIEW &&

					access.getViewAccess() && access.getSetAccess()){
				System.out.print("ENABLE3 FALSE");
				enable = false;
			}

			order=0;

			priority= idProp==Constants.IdPROP_RDN ? 5:0;
			if( idProp==helperConstant.TAPOS_ESTADO ) priority=3;

			LinkedList rangoClasses=property.getRangoList();
			int size=rangoClasses.size();
			if(!access.getConcreteAccess()){
				for(int i=0;i<size;i++){
					int valueCls=((Integer)rangoClasses.get(i)).intValue();					
					IdObjectForm idObjectForm=new IdObjectForm();
					idObjectForm.setIdo(idParent);
					idObjectForm.setIdProp(idProp);
					idObjectForm.setValueCls(m_kba.getClass(valueCls));
					id=idObjectForm.getIdString();

					Integer value=m_kba.getValueObjectNoConcrete(property,valueCls);

					defaultVal= value==null?"":value.toString();

					balancer.addItem(
							idGroup,
							typeField,
							id,
							String.valueOf(idParent),
							priority,
							null,
							enable,
							nullable,
							multivalued,
							defaultVal,
							Utils.normalizeLabel(name),
							longitud,
							comentado,
							order,
							visible,
							numBotones,
							vValues
					);
				}
			}
	}*/

	/*    private void buildFieldEnum( GViewBalancer balancer, int idtoUserTask, int idParent, ObjectProperty property, Integer userRol, access access,int idGroup,int rows, boolean showLabel, boolean forzarMostrarCabecera,String idBotonFilas)
	throws SystemException{

    	int tm=helperConstant.TM_ENUMERADO;
		Vector vValues = null;
		boolean comentado = false;
		String name=property.getName().trim();
		int idProp;
		String defaultVal="";
		int longitud=-1;
		String id;
		int order;
		int priority;
		boolean enable;
		boolean visible=true;
		boolean multivalued=false;
		boolean nullable;
		int numBotones=0;

		enable=true;
		nullable=true;

		idProp=property.getIdProp();
		if(	m_modo!=access.VIEW &&
			m_modo!=appControl.FILTRADO_INTERNO &&
			idProp== helperConstant.TAPOS_RDN/* &&
			m_to.getAttributeValue("RDN")!=null*//* ){
			System.out.print("ENABLE1 FALSE");
                        enable = false;
                }
		if(	m_modo!=access.VIEW &&
				m_modo!=appControl.FILTRADO_INTERNO /*&&
				eAtr.getAttributeValue("NULLABLE")!=null *//*)
					nullable=property.getCardMin()>0;
		if(	/*m_modo!=appControl.OBJ_SELECTION &&*//*
				m_modo!=access.VIEW &&
				m_modo!=appControl.FILTRADO_INTERNO &&
				/*prop.getAttributeValue("ACCESS")!=null &&
				prop.getAttributeValue("ACCESS").indexOf("READ")!=-1 &&
				prop.getAttributeValue("ACCESS").indexOf("WRITE")==-1 ){*/
	/*				access.getViewAccess() && access.getSetAccess()){
			    	System.out.print("ENABLE3 FALSE");
                    enable = false;
				}

		order=0;
		/*int NMax=property.getCardMax();
		priority= (NMax==1 && order==0 ) ? 1:0;*/
	/*		priority= idProp==helperConstant.TAPOS_RDN ? 5:0;
		if( idProp==helperConstant.TAPOS_ESTADO ) priority=3;

		vValues = new Vector();
		/*Iterator iVal= m_md.getEnumSet(intTapos);*/
	/*        LinkedList<ObjectValue> enumItems= property.getEnumList();
        int sizeEnum=enumItems.size();
        for(int i=0;i<sizeEnum;i++){
	   		ObjectValue objectValue=(ObjectValue)enumItems.get(i);
	   		DataProperty propertyRDN=m_kba.getField(objectValue.getValue(), helperConstant.TAPOS_RDN, userRol, idtoUserTask);
    		String label=m_kba.getValueData(propertyRDN);
    		int idObject=((Integer)objectValue.getValue()).intValue();
    		vValues.addElement(new GValue(idObject,label));
        }

        int idClass=-1;
        /*if(sizeEnum==-1)
        	idClass=((ObjectValue)enumItems.getFirst()).getValueCls();*/
	/*LinkedList<Integer> rangoClasses=property.getRangoList();
		int size=rangoClasses.size();*/
	/*if(!access.getConcreteAccess()){
			for(int i=0;i<size;i++){*/
	/*if(size>0){
				int idClass=((Integer)rangoClasses.getFirst()).intValue();*/
	/*int idClass=((Integer)rangoClasses.get(i)).intValue();*/
	/*id=Singleton.getInstance().getPropertiesMap().addProperty(property.getIdProp(),idClass);*/
	/*				id=idParent+":"+idProp+":"+idClass;

				Integer value=m_kba.getValueObjectNoConcrete(property,idClass);

				defaultVal= value==null?"":value.toString();

				balancer.addItem(
						idGroup,
		                tm,
		                id,
		                /*idTO.toString(),*//*String.valueOf(idParent),
		                idProp,
		                priority,
		                /*eAtr.getAttributeValue("MASK"),*//*null,
		                enable,
		                nullable,
		                multivalued,
		                defaultVal,
		                name,
		                longitud,
		                comentado,
		                order,
		                visible,
		                numBotones,
		                vValues
		         );
			/*}*/
	/*}*/
	/*	}
	 */   

	private static int calculateNumRowsVisibleTable(Integer idtoP,Integer idto, Integer cardMin, Integer cardMax, KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException{
		int rows;
		CardMed cm=kba.getCardMedComponents().getCardMedByClassAndParent(idto, idtoP);
		if (cm!=null){
			if(cardMax!=null && cm.getCardmed()>=cardMax)//Evitamos que se muestren mas filas de las que se pueden asignar a la tabla
				rows=cardMax;
			else if (cm.getCardmed()>1 && cm.getCardmed()<=20)
				rows=cm.getCardmed();
			else if(cm.getCardmed()==1 && (cardMax==null || cardMax>1))
				rows=2;
			else if(cm.getCardmed()==1 && cardMax==1)
				rows=1;
			else if(cm.getCardmed()>20)
				rows=20;
			else
				rows=4;
		}else{
			rows=1;
			if(cardMax!=null){
				if(cardMax <=10 && cardMax>= 2) rows=4  ;
				if(cardMax >10 && cardMax <20) rows=6  ;
				//if(NMax >= 20 ) rows= 10 ;
			}else if(cardMin==null || cardMin==0)
				rows=3;
			else rows = 4;

		}
		return rows;
	}
	
	private static int calculateNumRowsVisibleMemo(Integer idtoP,Integer idProp, Integer cardMin, Integer cardMax, KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException{
		int rows=3;
		CardMed cm=kba.getCardMedComponents().getCardMedByPropAndParent(idProp, idtoP);
		if (cm!=null){
			rows=cm.getCardmed();
		}else if(Auxiliar.equals(cardMin,1)){
				rows=6;
		}
		return rows;
	}



	public static boolean checkRequiredFieldsInTable(ArrayList<Column> columnList, Integer idRange,Integer userRol, Integer idtoUserTask, Session ses, KnowledgeBaseAdapter kba) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		//	System.out.println("HASCOLUMS - "+hascolum);
		
		//Hacemos que siempre se cumpla porque nos interesa que siempre haya nullRow
//		Iterator<Property> itp=kba.getProperties(idRange, userRol, idtoUserTask, ses);
//		while (itp.hasNext()){
//			Property p= itp.next();
//
//			if ((p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1)) && (p.getTypeAccess()!=null && p.getTypeAccess().getSetAccess())){
//				//System.out.println("Properties - "+p);
//				Iterator<Column> itphas= columnList.iterator();
//				boolean exist=false;
//				while (itphas.hasNext() && !exist){
//					Property p2= itphas.next().getProperty();
//					if (p.equals(p2))
//						exist=true;	
//				}
//				if (!exist){
//					if (p instanceof ObjectProperty){
//						exist=subCheckRequiredFieldInTable(columnList,(ObjectProperty)p,userRol, idtoUserTask, ses, kba);
//						if(!exist)
//							return false;
//					}else{
//
//						return false;
//
//					}
//
//				}
//
//			}
//
//		}
		return true;
	}

	private static boolean subCheckRequiredFieldInTable(ArrayList<Column> columnList, ObjectProperty op, Integer userRol, Integer idtoUserTask, Session ses, KnowledgeBaseAdapter kba) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		Iterator<Integer> itrango=op.getRangoList().iterator();
		boolean exist=false;
		while(itrango.hasNext() && !exist){
			int rang=itrango.next();
			int idtoRange=kba.getClass(rang);
			Iterator<Property> itp=kba.getProperties(rang, idtoRange, userRol, idtoUserTask, ses);

			while (itp.hasNext() && !exist){
				Property p= itp.next();
				//System.out.println("Properties - "+p);
				Iterator<Column> itphas= columnList.iterator();
				while (itphas.hasNext() && !exist){
					Property p2= itphas.next().getProperty();
					if (p.equals(p2))
						exist=true;	
				}

			}
		}
		if(!exist)
			return false;
		else
			return true;


	}
	public static String buildTable( GViewBalancer balancer, KnowledgeBaseAdapter kba,Integer idtoUserTask, int idoParent, ObjectProperty property, Integer value,
			Integer userRol, access access,int idGroup,int rows, boolean showLabel, boolean forzarMostrarCabecera,boolean filterMode,String idBotonFilas,String idBotonSelect,String idTable,boolean selectionMode,boolean multipleMode,Session sess,int widthMin,IDictionaryFinder dictionaryFinder,boolean addValues, HashMap<String,String> aliasMap, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{

		String id=null;
		int priority;
		boolean cuantitativo;
		int headerLine;
		boolean hideHeader;
		/*int rows;*/
		//int anchoBoton;
		int numBotones;
		String label;
		String name;
		boolean enable;
		boolean nullable;
		boolean visible;
		int order;
		int iniVirtualColumn;
		int atGroupColumn;		
		boolean creationRow=false;
		boolean finderRow=false;
		//System.out.println("FormFactory.buildTable:"+property.toString() +" con idtoUserTask:"+idtoUserTask);

		Integer NMin=property.getCardMin();
		Integer NMax=property.getCardMax();

		nullable=multipleMode || (NMin==null || NMin.intValue()==0);// || ( cardMin!=null && (defaultVal==null || defaultVal.equals("")));

		order=/*Singleton.getInstance().getOrderComponents()*/kba.getOrder(property.getIdProp(), idGroup);

//		priority= (NMax!=null && NMax==1 && order==0 ) ? 1:0;
		priority=/*Singleton.getInstance().getOrderComponents()*/kba.getPriority(property,idGroup);

		enable=true;

		visible=true;

		cuantitativo=false;
		boolean dobleSizeHeader=false;
		
		boolean onlyFirstLevelColumnsTable=false;
		
		//En los parametros de un report no nos interesa que se cargue mas de un nivel
		if(!filterMode && idtoUserTask!=null && kba.isSpecialized(idtoUserTask,Constants.IDTO_REPORT) && kba.isSpecialized(property.getIdto(),Constants.IDTO_PARAMS))
			onlyFirstLevelColumnsTable=true;

		iniVirtualColumn=-1;
		atGroupColumn=-1;

		if (dobleSizeHeader)
			/*table.setAttribute("HEADER_LINE", "2");*/
			headerLine=2;
		else
			/*table.setAttribute("HEADER_LINE", "1");*/
			headerLine=1;

		/*LinkedList rangoClasses=property.getRangoList();
		int size=rangoClasses.size();*/
		LinkedList<Integer> rangoValues;
		if(value==null)
			rangoValues=property.getRangoList();
		else{
			rangoValues=new LinkedList<Integer>();
			rangoValues.add(value);
		}

		int size=rangoValues.size();
		if(/*access.getConcreteAccess()*/true){
			for(int i=0;i<size;i++){
				LinkedHashMap<Integer,ArrayList<String>> idtoIdColumnMap=new LinkedHashMap<Integer,ArrayList<String>>();
				Vector<GTableColumn> vColumns = new Vector<GTableColumn>();
				Vector vRows = new Vector();
				/*int valueCls=((Integer)rangoClasses.get(i)).intValue();*/
				Integer idRange=rangoValues.get(i);
				int idto=kba.getClass(idRange);

				boolean structural=kba.getCategoryProperty(property.getIdProp()).isStructural();
				HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(idRange,userRol,property.getTypeAccess(),idtoUserTask );
				AccessAdapter accessAdapter=new AccessAdapter(accessUserTasks,property,structural,filterMode);
				 
				if(idTable==null){
					IdObjectForm idObjectForm=new IdObjectForm();
					idObjectForm.setIdo(idoParent);
					idObjectForm.setIdProp(property.getIdProp());
					/*					idObjectForm.setIdtoUserTask(idtoUserTask);*/
					idObjectForm.setValueCls(idto);
					id=idObjectForm.getIdString();
				}else id=idTable;
				//int numeroCamposNoRDN = 0;

				name=property.getName();
				label=null;
				if(showLabel){
					/*int idto;*/
					/*if(filterMode || property.getValueList().isEmpty())
						idto=kba.getIdtoFilter(property);
					else{
						idto=property.getRangoList().getFirst();
						//idto=kba.getIdtoObject(property);
					}*/
					String toName=null;
					if(aliasMap==null || !aliasMap.containsKey(name)){
						toName=/*property.getName();*/kba.getLabelProperty(property,property.getIdto(),idtoUserTask).trim();//TODO quitar porque se muestra el nombre de la property
					}else{
						toName=aliasMap.get(name);
					}
					/*if(NMax==null || NMax>1){
						String[] buf = toName.split(" ");
						int size=buf.length;
						label="";
						for(int i=0;i<size;i++){
							char letter=buf[i].charAt(buf[i].length()-1);
							if("abcde".toUpperCase().indexOf((""+letter).toUpperCase())!=-1)
								label += toName + "s";
							else label += toName + "es";

							if(size>i+1)
								label+=" ";
						}
					}else*/ label = toName;
				}

				label=Utils.normalizeLabel(label);
				int columns = 0;

				if(idBotonFilas!=null){
					/*new GTableColumn("ADD",columns,"AC",idTM,ref,claseCampo,tapos,width,height,length,mask,enable,hide,total,agrupable,dobleSizeHeader,nullable);*/
					int rowHeight=heightRowTable;
					vColumns.addElement(new GTableColumn(idBotonFilas,null,columns,"FV",GConst.TM_BUTTON,"1",/*"BUTTON",*/1,rowHeight,rowHeight,1,null,true,false,false,false,false,false,null,false,GTableColumn.NOT_FINDER,true,false,null));
					columns++;
				}
				
				if(idBotonSelect!=null){
					/*new GTableColumn("ADD",columns,"AC",idTM,ref,claseCampo,tapos,width,height,length,mask,enable,hide,total,agrupable,dobleSizeHeader,nullable);*/
					int rowHeight=heightRowTable;
					vColumns.addElement(new GTableColumn(idBotonSelect,null,columns,"",GConst.TM_BOOLEAN,"0",/*"BUTTON",*/0,rowHeight,rowHeight,1,null,true,false,false,false,false,false,null,false,GTableColumn.NOT_FINDER,true,false,null));
					columns++;
				}
				
				JTree columnsTree=null;
				ArrayList<String> idColumns=new ArrayList<String>();
				
				boolean isAbstract=kba.isAbstractClass(idRange);
				if(!selectionMode){
					
					if(!filterMode && !nullable && label!=null && !label.isEmpty()){
						label="*"+label;
					}
					/*instance inst=kba.getTreeObject(idFilter, userRol, idtoUserTask);
					kba.setInstance(inst);*/
					
					//Si está enganchado por una targetClass nos interesa que se comporte como una estructural para que permita la edicion y aparezcan todas las columnas. Esto tambien se ha tenido que hacer en TableForm
					if(property.getIdProp().equals(Constants.IdPROP_TARGETCLASS))
						structural=true;
						
					//Iterator<Property> itr=kba.getColumnsObject(idRange,userRol,idtoUserTask,null);
					int idtoRange=kba.getClass(idRange);
					ArrayList<Column> columnList=kba.getColumnsObject(idRange,idtoRange,idtoRange,property.getIdto(),userRol,idtoUserTask,sess,filterMode,structural,onlyFirstLevelColumnsTable,aliasMap);//Singleton.getInstance().getColumnsTable().getColumns(idRange, userRol, idtoUserTask, kba.getDefaultSession());

					/*TreeColumnsTable tree=new TreeColumnsTable(property.getIdo(),property.getIdProp(),kba.getTreeObjectTable(idRange, kba.getClass(idRange),property.getIdto(), userRol, idtoUserTask, sess));
					System.err.println("----------------LabelTable:"+label);
					System.err.println(tree.getPathColumns());*/
					
					Iterator<Column> itr=columnList.iterator();

					//Singleton.getInstance().getColumnsTable().getColumns(idRange, userRol, idtoUserTask, kba.getDefaultSession());

					/*kba.clearInstance();*/

					//Si es abstracta no se permite ya que primero hay que elegir que tipo de objeto se quiere crear
					creationRow=!filterMode && !selectionMode /*&& !isAbstract*/ && !property.getIdProp().equals(Constants.IdPROP_TARGETCLASS) && !accessAdapter.getUserTasksAccess(AccessAdapter.NEW_AND_REL).isEmpty() && checkRequiredFieldsInTable(columnList, idRange, userRol, idtoUserTask, sess, kba);
					
					while(itr.hasNext()){
						Column col=itr.next();
						//No tenemos idObject ya que hemos obtenido las properties de una clase
						Property prop=col.getProperty();
//						Integer idRang=prop.getUniqueValue().;
//						if(value==null)
//							rangoValues=property.getRangoList();
//						else{
//							rangoValues=new LinkedList<Integer>();
//							rangoValues.add(value);
//						}
						
						String idParent=id;
						Property propParent=col.getPropertyParent();
						if(propParent!=null){
							IdObjectForm idObjectForm=new IdObjectForm();
							idObjectForm.setIdo(propParent.getIdo());
							idObjectForm.setIdProp(propParent.getIdProp());
							/*					idObjectForm.setIdtoUserTask(idtoUserTask);*/
							if(propParent instanceof ObjectProperty)
								idObjectForm.setValueCls(kba.getClass(kba.getIdRange((ObjectProperty)propParent)));
							else idObjectForm.setValueCls(((DataProperty)propParent).getDataType());
							idParent=idObjectForm.getIdString();
						}
						
						boolean structuralParent=structural;
						if(col.getPropertyParent()!=null)
							structuralParent=kba.getCategoryProperty(col.getPropertyParent().getIdProp()).isStructural();
						
						AccessAdapter accessAdapterColumn=accessAdapter;
						boolean creationRowInColumn=creationRow;
						//boolean allowedSet=true;
						boolean isAbstractColumn=isAbstract;
						if(prop.getIdo()!=idRange && col.getPropertyParent()!=null){//Si no se trata de una columna de primer nivel calculamos sus permisos para saber si permite la creacion
							accessAdapterColumn=new AccessAdapter(kba.getAllAccessIndividual(prop.getIdo(),userRol,col.getPropertyParent().getTypeAccess(),idtoUserTask ),col.getPropertyParent(),structuralParent,filterMode);
							isAbstractColumn=kba.isAbstractClass(prop.getIdto());
							creationRowInColumn=!filterMode && !selectionMode /*&& !isAbstractColumn*/ && !accessAdapterColumn.getUserTasksAccess(AccessAdapter.NEW_AND_REL).isEmpty() && checkRequiredFieldsInTable(columnList, idRange, userRol, idtoUserTask, sess, kba);
							//allowedSet=kba.getCategoryProperty(col.getPropertyParent().getIdProp()).isStructural();
						}
						
						creationRowInColumn=creationRowInColumn && prop.getTypeAccess().getSetAccess();
						
						//LinkedList<Integer> rangoValues;
						
						/*int idObject;
			   			int idClassObj;
			   			if(prop instanceof DataProperty){
			   				idClassObj=((DataProperty)prop).getDataType();
			   				idObject=idClassObj;
			   			}
			   			else{
			   				idObject=kba.getIdFilter((ObjectProperty)prop);
			   				idClassObj=((ObjectProperty)prop).getRangoList().getFirst();
			   			}*/
						//int idProp=prop.getIdProp();
						int idtoProp=prop.getIdto();
						/*int idRel=prop.getIdoRel();
			   			SelectQuery select=new SelectQuery(idObject,idClassObj,idProp,idRel);*/
						/*if(idProp!=Constants.IdPROP_RDN)
			   				numeroCamposNoRDN++;*/
						
						GTableColumn tableColumn=buildColumna(/*m_md, false,*/ /*TO, nodoVirt,*/idRange, idtoUserTask, id, idParent, col, accessAdapterColumn, userRol, columns/*, props*/,sess, kba, dictionaryFinder, creationRowInColumn, filterMode, structuralParent);
						columns++;
//						if(!property.getTypeAccess().getSetAccess()/* || !allowedSet*/ /*|| !structuralParent*/)
//							tableColumn.setEnable(false);
						
						if(tableColumn.hasFinder() && !col.isStructuralTree() && prop.getIdo().intValue()!=idRange && !isAbstractColumn){
							//tableColumn.setEnable(false);
							tableColumn.setTypeFinder(GTableColumn.CREATION_FINDER);
						}
						
						if(!col.isStructuralTree())
							tableColumn.setBasicEdition(false);
						
						//System.err.println("label:"+tableColumn.getLabel()+" enable:"+tableColumn.isEnable()+" basicEdition:"+tableColumn.isBasicEdition());
						vColumns.addElement(tableColumn);

						// TENGO QUE HACER LO DEL DOBLE TAMAÑO

						//System.out.println("METODO Check - "+checkRequiredFieldInTable(hashColumns, idRange, userRol, idtoUserTask, sess, kba));
						
						if(prop.getIdo().intValue()==idRange && tableColumn.hasFinder())
							finderRow=true;
					}
					
					//Construimos el arbol de mapeo para insercion de filas
					for(GTableColumn tableColumn:vColumns){
						String idColumn=tableColumn.getId();
						idColumns.add(idColumn);
					}
					columnsTree=kba.getTreesOfTableColumns(idColumns);
					
				}

				//if(!filterMode || selectionMode){
					if(isAbstract){
						String idType="type";
						int rowHeight=heightRowTable;
						vColumns.addElement(new GTableColumn(idType,id,columns,"Tipo",GConst.TM_TEXT,"2",/*"BUTTON",*/1,rowHeight,rowHeight,1,null,false,false,false,false,false,false,null,false,GTableColumn.NOT_FINDER,false,false,null));
						columns++;
						idColumns.add(idType);
					}
				//}


				if(!filterMode && addValues){
					//System.err.println("idtoIdColumnMap:"+idtoIdColumnMap);
					// Creamos las filas de la tabla
					//boolean structural=kba.getCategoryProperty(property.getIdProp()).isStructural();
					vRows=buildTableRows(idColumns,property,userRol,idtoUserTask,sess,id,columnsTree, kba);
				}

				if(rows==-1){
					///CREARE UNA TABLA POR CADA RELACIÓN
					rows=calculateNumRowsVisibleTable(property.getIdto(), kba.getClass(idRange), NMin,NMax, kba);

				}

				boolean verCabecera = forzarMostrarCabecera/* || numeroCamposNoRDN >= 1*/ || rows>1;

				if (!verCabecera)
					/*table.setAttribute("HIDE_HEADER", "TRUE");*/
					hideHeader=true;
				else hideHeader=false;

				boolean botoneraResumida = hideHeader && rows==1;

				 numBotones = botoneraAccion.numeroBotonesTabla(/*operations,*//*accessUserTasks*/accessAdapter, true,
						 false, false, false,allowedConfigTable);
				 if (botoneraResumida)
					 numBotones = 1;
				 //System.out.println("UserTasks que apuntan a "+idRange+" con label"+label+"->"+accessUserTasks.keySet().toString()+" NumBotones:"+numBotones);

				 int codigo=balancer.addTable(idGroup,
						 id,
						 priority,
						 enable,
						 nullable,
						 label,
						 name,
						 order,visible,rows,vColumns,vRows,cuantitativo,
						 iniVirtualColumn,atGroupColumn,headerLine,hideHeader,numBotones,widthMin,accessAdapter,creationRow,finderRow);

				 if(codigo!=0)
					 System.err.println("ERROR:Item no añadido, codigo de error:"+codigo);
				 //System.out.println("Id de la tabla:"+id+" value:"+value);
			}
		}
		return id;
	}

	private static Vector buildTableRows(ArrayList<String> columns,ObjectProperty property,Integer userRol,Integer idtoUserTask,Session sess,String idTable,JTree columnsTree,KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		
		
//		LinkedHashMap<String,SelectQuery> mapIdColumnSelect=new LinkedHashMap<String,SelectQuery>();
//		
//		for(String idColumn:columns){
//			if(IdObjectForm.matchFormat(idColumn)){
//				IdObjectForm idObjForm=new IdObjectForm(idColumn);
//				SelectQuery select=new SelectQuery(""+idObjForm.getIdo(),idObjForm.getIdProp(),idObjForm.getFilterIdProp(),idObjForm.getFilterValue());
//				mapIdColumnSelect.put(idColumn,select);
//			}else{
//				mapIdColumnSelect.put(idColumn,null);//La columna 'Type' no tiene selectQuery
//			}
//		}
		
		//Integer idtoParent=property.getIdto();
		LinkedList<Value> valueList=property.getValues();
		//System.err.println("PropertyTabla:"+property);
		Vector<GTableRow> vRows = new Vector<GTableRow>();
		Iterator<Value> itrValues=valueList.iterator();
		while(itrValues.hasNext()){
			ObjectValue objectVal=(ObjectValue)itrValues.next();
			int idObjectV=objectVal.getValue();
			int idClassV=objectVal.getValueCls();
			//int idtoRange=kba.getClass(property.getRangoList().getFirst());
			instance inst=kba.getTreeObjectTable(idObjectV,idClassV,idTable, columnsTree, userRol,idtoUserTask,sess);
			GTableRow tableRow=kba.buildTableRow(inst, /*mapIdColumnSelect*/columns, idtoUserTask);

			vRows.addElement(tableRow);
		}

		return vRows;
	}

	private static GTableColumn buildColumna(int idoTable, Integer idtoUserTask, String idTable, String idParent, Column column, AccessAdapter accessAdapter, Integer userRol,int columnNumber,Session sess,KnowledgeBaseAdapter kba,IDictionaryFinder dictionaryFinder,boolean creationRow,boolean filterMode,boolean structuralParent) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		//GTableColumn columna=null;

		boolean enable;
		boolean hide;
		boolean total;
		boolean agrupable;
		//String label;
		//int idTM;
		String id;
		//int idPropMap;
		//String claseCampo;
		int width;
		int height;
		int length;
		String ref;
		boolean dobleSizeHeader;
		boolean nullable;
		String mask;
		boolean uniqueValue;
		String parentTree;

		//IDictionaryFinder dictionary=null;

		boolean visible=true;
		
		Property property=column.getProperty();
		String label=column.getName();
		String valueFilter=column.getFilterValue();
		Integer idPropFilter=column.getFilterIdProp();
		nullable=column.isNullable();
		uniqueValue=property.getIdProp().equals(Constants.IdPROP_RDN);
		
		parentTree=column.getParentTree();
		
		//System.err.println("parentTree:"+parentTree);
//		Element formItem = new Element("ITEM");

		/*tapos = new Integer(select.getAttributeValue("TA_POS"));*/

		access acceso=property.getTypeAccess();

		//System.err.println("Columna: idoTable:"+idoTable+" property:"+property);
			
		if (!visible)
			/*formItem.setAttribute("HIDE", "TRUE");*/
			hide=true;
		else hide=false;

		/*formItem.setAttribute("COLUMN", String.valueOf(columns - 1));*/

		mask=kba.getPropertyMask(property.getIdProp(), property.getIdto(), idtoUserTask);
		total=false;

		int idProp=property.getIdProp();
		/*idPropMap=-1;*/
		/*LinkedList rangoClasses=property.getRangoList();
		int idClass=((Integer)rangoClasses.getFirst()).intValue();*/
		/*idPropMap=Singleton.getInstance().getPropertiesMap().addProperty(idProp,null);*/

//		if (idProp == helperConstant.TAPOS_ESTADO)
//		/*formItem.setAttribute("AGRUPABLE", "TRUE");*/
		/*			agrupable=true;
		else */agrupable=false;
		/*formRoot.addContent(formItem);*/
		// /////////////
		/*if (idProp == helperConstant.TAPOS_RDN)
			label += md.getTOLabel(TO);
		else
			label += md.getATLabel(tapos);*/
		/*if(idProp==Constants.IdPROP_RDN)
			label="Nombre";
		else{*/
		/*if(property instanceof ObjectProperty)
				label=kba.getLabelObject(((ObjectProperty)property).getId()).trim();
			else label=kba.getLabelObject(((DataProperty)property).getId()).trim();*/
//		System.out.println("La property de buildColumna:"+property.toString());

//		if(idProp==Constants.IdPROP_RDN)
//		label=kba.getLabelClass(property.getIdto(),idtoUserTask).trim();
//		else label=kba.getLabelProperty(property,property.getIdto(),kba.getGroup(idProp),idtoUserTask).trim();//kba.getLabelClass(property.getIdto(),idtoUserTask).trim();
		/*}*/

		// //////////
		dobleSizeHeader = false;
		if (label.indexOf(":") != -1)
			dobleSizeHeader = true;
		// ////////////////

		int typeField=-1;
		int valueCls=-1;
		if(property instanceof DataProperty){
			int dataType=((DataProperty)property).getDataType();
			typeField=getTypeField(dataType);
			Integer lengthInteger=kba.getPropertyLength(property.getIdProp(),property.getIdto(),idtoUserTask);
			length= lengthInteger!=null?lengthInteger:-1;
			valueCls=dataType;
		}else{
			if(((ObjectProperty)property).getEnumList().size()>0){
				typeField=GConst.TM_ENUMERATED;
				valueCls=((ObjectProperty)property).getEnumList().getFirst().getValueCls();
			}
			length=-1;

		}
		
		boolean structural=kba.getCategoryProperty(idProp).isStructural();
		int finder=getTypeFinder(idoTable,accessAdapter,property,valueCls,filterMode,structural);
		boolean basicEdition=structuralParent;//Si el padre es estructural se permite la edicion
		
		if(property.getIdo()==null)
			System.err.println("ERROR: Property con ido nulo:"+property);
		boolean creationAllowed=creationRow;// && property.getIdo().intValue()==idoTable;
		
		enable=true;
		if(!acceso.getSetAccess() || accessAdapter.getUserTasksAccess(AccessAdapter.SET).isEmpty()/* || property.getIdo()!=idoTable*/)//TODO Si no es una columna del objeto principal no dejamos editarlo
			enable=false;

		//if(hasCreationAllowed())
			
		//	creationAllowed=!filterMode && !selectionMode && checkRequiredFieldInTable(hashColumns, idRange, userRol, idtoUserTask, sess, kba);
		/*formItem.setAttribute("ID_TM", String.valueOf(tm));*/
		/*idTM=tm;*/
		/*formItem.setAttribute("ID", select.getAttributeValue("REF") + "@"
				+ tapos);*/
		int ido=property.getIdo();
		int idto=property.getIdto();
		/*IdObjectForm idObjectForm=new IdObjectForm();
		idObjectForm.setIdo(idto);
		idObjectForm.setIdProp(idProp);
		id=idObjectForm.getIdString();*/
		//id=idto+"@"+idProp;

		/*formItem.setAttribute("REF", select.getAttributeValue("REF"));*/
		ref=idTable;

		/*formItem.setAttribute("TA_POS", tapos.toString());*/
		/*String type = "TEXT";*/
		//claseCampo="TEXT";
		ArrayList<GValue> valuesPossible=null;

		Integer redondeo=null;
		switch (typeField) {
		case GConst.TM_ENUMERATED: {
			valuesPossible = new ArrayList<GValue>();
			/*Iterator iVal= m_md.getEnumSet(intTapos);*/
			if(property instanceof DataProperty){
				LinkedList<DataValue> enumItems= ((DataProperty)property).getEnumList();
				int size=enumItems.size();
				for(int i=0;i<size;i++){
					DataValue dataValue=(DataValue)enumItems.get(i);
					String labelOption;
					/*if(dataValue.getValueMin()==-1 && dataValue.getValueMax()==-1)
            			labelOption=(String)dataValue.getValue();
 				   	else labelOption=String.valueOf(dataValue.getValueMin());*/
					labelOption=kba.getValueData(dataValue).toString();
					valuesPossible.add(new GValue(-1,Utils.normalizeLabel(labelOption)));
				}
			}else{/*ObjectProperty*/
				LinkedList<ObjectValue> enumItems= ((ObjectProperty)property).getEnumList();
				int size=enumItems.size();
				for(int i=0;i<size;i++){
					ObjectValue objectValue=(ObjectValue)enumItems.get(i);
					DataProperty propertyRDN=kba.getRDN(objectValue.getValue(), objectValue.getValueCls(), userRol, idtoUserTask, sess);
					String labelOption=(String)kba.getValueData(propertyRDN);
					int idObject=((Integer)objectValue.getValue()).intValue();
					valuesPossible.add(new GValue(idObject,Utils.normalizeLabel(labelOption)));
				}
			}
			/*claseCampo = "LIST";*/
			break;
		}
		case GConst.TM_REAL:
			redondeo=GConfigView.redondeoDecimales;
			break;
		/*case GConst.TM_BOOLEAN:
			claseCampo = "CHECK";
			break;
		case GConst.TM_BOOLEAN_COMMENTED:
			claseCampo = "CHECK";
			break;
		 */
		}
		IdObjectForm idObjectForm=new IdObjectForm();
		idObjectForm.setIdo(/*idto*/ido);
		idObjectForm.setIdto(idto);
		idObjectForm.setIdProp(idProp);
		idObjectForm.setFilterValue(valueFilter);
		idObjectForm.setFilterIdProp(idPropFilter);
		idObjectForm.setValueCls(valueCls);
		idObjectForm.setIdParent(parentTree);//ParentTree tiene pares ido,idProp separados por # hasta llegar a la padre de la columna
		id=idObjectForm.getIdString();
		/*formItem.setAttribute("TYPE", type);*/
		/*formItem.setAttribute("WIDTH", "30");*/
		width=heightRowTable;//TODO Parece que no se necesita para nada,aunque quizas estaria bien darle un ancho querido ya que el ancho que utiliza es calculado
		/*formItem.setAttribute("HEIGHT", "20");*/
		height=heightRowTable;//TODO Parece que no se necesita para nada. Si sirve para los checkCellRenderer y checkCellEditor. Al igual que width.

//		nullable=true;
//		Integer cardMin=property.getCardMin();
//		if(cardMin!=null && cardMin>0)
//			nullable=false;
		/*String mask=select.getAttributeValue("MASK");*/
		/*return dobleSizeHeader;*/
		//System.err.println("label:"+label+" id:"+id);
		return new GTableColumn(id,idParent,columnNumber,Utils.normalizeLabel(label),typeField,ref,idProp,width,height,length,mask,enable,hide,total,agrupable,dobleSizeHeader,nullable,valuesPossible,creationAllowed,finder,basicEdition,uniqueValue,redondeo);
	}
	
	private static int getTypeFinder(int idoTable,AccessAdapter accessAdapter,Property propColumn,int valueCls,boolean modeFilter,boolean structural){
		int typeFinder=GTableColumn.NOT_FINDER;
		if(!modeFilter && !structural/*&& propColumn.getIdo()!=idoTable*/){	
			// Solo permitimos ayuda en la edicion cuando se trate de un String
			if(valueCls==Constants.IDTO_STRING){
				//Si no se puede buscar en la tabla no se permite
				if(!accessAdapter.getUserTasksAccess(AccessAdapter.HIDDEN_FIND_AND_REL).isEmpty())
					typeFinder=GTableColumn.HIDDEN_FINDER;
				else if(!accessAdapter.getUserTasksAccess(AccessAdapter.FIND_AND_REL).isEmpty())
					typeFinder=GTableColumn.NORMAL_FINDER;
			}
		}
		
		return typeFinder; 
	}
	
	/*private static boolean hasCreationAllowed(int idoTable,AccessAdapter accessAdapter,Property propColumn,int valueCls){
		boolean hasTableFinder=false;
		if(!accessAdapter.getUserTasksAccess(AccessAdapter.REL).isEmpty() || propColumn.getIdo()!=idoTable){//Si no se puede buscar en la tabla no se permite	
			// Solo permitimos ayuda en la edicion cuando se trate de un String
			if(valueCls==Constants.IDTO_STRING)
				hasTableFinder=true;
		}
		
		return hasTableFinder; 
	}*/


	public static GProcessedForm buildResultsTable(KnowledgeBaseAdapter kba,Integer idtoUserTask,ObjectProperty property,int value,Integer userRol,
			access access, Dimension dim,String idTable,boolean selectionMode,boolean soporteFavoritos,Session sess, boolean filterMode, int widthMin, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{

		GViewBalancer balancer= newBalancer(dim,true);
		GProcessedForm viewForm=null;
		String idBotonSelect=GConst.ID_COLUMN_TABLE_SELECTION;
		String idBotonFilas;
		/*if(soporteFavoritos)
			idBotonFilas="ADD";
		else */idBotonFilas=null;//null si no queremos boton
		/*int rows= Math.min(5,(int)(((double)dim.getHeight()-GConfigView.tableButtonSide)/balancer.getRowHeight()));*/
		int rows= (int)(((double)dim.getHeight()/*-GConfigView.buttonHeight*/)/heightRowTable);
		if(!filterMode || selectionMode)
			rows=11;
		else if(rows<6)
			rows=6;
		int idGroup=0;
		balancer.addGroup(idGroup,"",0);
		boolean showLabel=false;
		boolean forzarMostrarCabecera=true;
		boolean addValues=false;
		boolean multipleMode=false;
		buildTable(/*userRol,action,*/balancer,kba,idtoUserTask,-1,property,value,userRol,access,/*id,md,toRoot,filter,dom,*/idGroup,rows,/*label,ancho,accessStr,*/showLabel,forzarMostrarCabecera,filterMode,idBotonFilas,idBotonSelect,idTable,selectionMode,multipleMode,sess, widthMin, null, addValues, null, allowedConfigTable);

		balancer.process(true,new Insets(0,0,0,0),new Insets(0,0,0,0));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}

	public static GProcessedForm buildFavoritosTable(KnowledgeBaseAdapter kba,Integer idtoUserTask,ObjectProperty property,int value,Integer userRol, 
			access access,Dimension dim,String idTable,boolean selectionMode,boolean soporteFavoritos,Session sess, boolean filterMode, int widthMin, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{

		GViewBalancer balancer= newBalancer(dim,true);
		GProcessedForm viewForm=null;
		String idBotonSelect=GConst.ID_COLUMN_TABLE_SELECTION;
		String idBotonFilas;
		/*if(soporteFavoritos)
			idBotonFilas="DEL";
		else */idBotonFilas=null;//null si no queremos boton
		/*int rows= 3;//Math.max(3,(int)(((double)alto)/balancer.getRowHeightTable()));*/
		/*int rows= Math.min(3,(int)((double)dim.getHeight()/balancer.getRowHeightTable()));*/
		int rows=2;
		/*if(rows<3)
    		rows=3;*/
		int idGroup=0;
		balancer.addGroup(idGroup,"",0);
		boolean showLabel=false;
		boolean forzarMostrarCabecera=true;
		boolean addValues=false;
		boolean multipleMode=false;
		buildTable(/*userRol,action,*/balancer,kba,idtoUserTask,-1,property,value,userRol,access,/*id,md,toRoot,filter,dom,*/idGroup,rows,/*label,ancho,accessStr,*/showLabel,forzarMostrarCabecera,filterMode,idBotonFilas,idBotonSelect,idTable,selectionMode,multipleMode,sess, widthMin, null, addValues, null, allowedConfigTable);

		balancer.process(true,new Insets(0,0,0,0),new Insets(0,0,0,0));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}
	
	public static GProcessedForm buildPersonalTable(KnowledgeBaseAdapter kba,Integer idtoUserTask,ObjectProperty property,int value,Integer userRol,
			access access, Dimension dim, String idTable, boolean filterMode, boolean selectionMode,Session sess, boolean addValues, int widthMin, boolean allowedConfigTable) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{

		GViewBalancer balancer= newBalancer(dim,true);
		GProcessedForm viewForm=null;
		String idBotonSelect=null;
		String idBotonFilas=null;//null si no queremos boton
		/*int rows= Math.min(5,(int)(((double)dim.getHeight()-GConfigView.tableButtonSide)/balancer.getRowHeight()));*/
		//int rows= (int)(((double)dim.getHeight()/*-GConfigView.buttonHeight*/)/GConfigView.heightRowTable);
		
		int rows=-1;
		if(!selectionMode){
			rows=property.getValues().size();
			if(rows<4){
				rows=4;
			}else{
				rows*=1.5;
				if(rows*heightRowTable>dim.getHeight())
					rows=(int)(((double)dim.getHeight()/*-GConfigView.buttonHeight*/)/heightRowTable);
			}
		}else rows=10;
		
		int idGroup=0;
		balancer.addGroup(idGroup,"",0);
		boolean showLabel=false;
		boolean forzarMostrarCabecera=true;
		boolean multipleMode=false;
		buildTable(balancer,kba,idtoUserTask,-1,property,value,userRol,access,/*id,md,toRoot,filter,dom,*/idGroup,rows,showLabel,forzarMostrarCabecera,filterMode,idBotonFilas,idBotonSelect,idTable,selectionMode,multipleMode,sess, widthMin, null, addValues, null, allowedConfigTable);

		balancer.process(true,new Insets(0,0,0,0),new Insets(0,0,0,0));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}
	
	public static ArrayList<GProcessedForm> buildFormulario(KnowledgeBaseAdapter kba,Dimension dim,ArrayList<Property> properties,Integer idtoUserTask,Integer idObjectParent,int idObject,Integer userRol,Session sess,boolean popup,boolean filterMode,boolean multipleMode,int mode,boolean alias,boolean groups,boolean keepOrder,IDictionaryFinder dictionaryFinder,Insets margenesPanel,Insets margenesGrupo, boolean allowedConfigTable) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		return buildForms(kba, dim, properties, idtoUserTask, idObjectParent, idObject, userRol, sess, popup, filterMode, multipleMode, mode, alias, groups, keepOrder, dictionaryFinder, new ArrayList<ObjectProperty>(),margenesPanel,margenesGrupo,null,allowedConfigTable);
	}

	public static GProcessedForm buildFormularioLogin(int idLogin,String valueLogin,int idPassword,String valuePassword,int idMode,ArrayList<GValue> possibleValuesMode,Integer valueMode){

		Dimension dim=new Dimension(250,100);

		GViewBalancer balancer= newBalancer(dim,false);
		GProcessedForm viewForm=null;

		int groupId=0;
		balancer.addGroup(groupId,"",0);

		int tm=GConst.TM_TEXT;
		int dataType=Constants.IDTO_STRING;
		String id;
		String idTO="";
		int prioridad=0;
		String mask=null;
		boolean enable=true;
		boolean nullable=false;
		boolean multivalued=false;
		Object defaultVal="";
		String name="Usuario";
		String label=name;
		int longitud=22;
		boolean comentado=false;
		int order=1;
		boolean visible=true;
		int numBotones=0;
		Vector vValues=null;

		IdObjectForm idObjectForm=new IdObjectForm();
		idObjectForm.setIdo(idLogin);
		idObjectForm.setValueCls(dataType);
		id=idObjectForm.getIdString();

		defaultVal=valueLogin;
		
		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);

		idObjectForm.setIdo(idPassword);
		idObjectForm.setValueCls(dataType);
		id=idObjectForm.getIdString();

		name="Password";
		label=name;
		order=2;
		defaultVal=valuePassword;

		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);
		
		tm=GConst.TM_ENUMERATED;
		dataType=Constants.IDTO_ENUMERATED;
		
		idObjectForm.setIdo(idMode);
		idObjectForm.setValueCls(dataType);
		id=idObjectForm.getIdString();
		
		name="Modo";
		label=name;
		order=3;
		vValues=new Vector(possibleValuesMode);
		defaultVal=String.valueOf(valueMode);

		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);

		groupId=1;
		balancer.addGroup(groupId,"",1);
		tm=GConst.TM_BUTTON;
		name="Aceptar";
		label=name;
		id=String.valueOf(0);
		longitud=12;
		order=0;

		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);

		name="Cancelar";
		label=name;
		id=String.valueOf(botoneraAccion.CANCEL);

		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);


		balancer.process(true,new Insets(8,8,8,8),new Insets(4,4,4,4));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}
	
	public static GProcessedForm buildImportForm(String idExcelFile){

		Dimension dim=new Dimension(250,100);

		GViewBalancer balancer= newBalancer(dim,false);
		GProcessedForm viewForm=null;

		int groupId=0;
		balancer.addGroup(groupId,"",0);

		int tm=GConst.TM_FILE;
		int dataType=Constants.IDTO_FILE;
		String id;
		String idTO="";
		int prioridad=0;
		String mask=null;
		boolean enable=true;
		boolean nullable=false;
		boolean multivalued=false;
		Object defaultVal="";
		String name="Excel";
		String label=name;
		int longitud=-1;
		boolean comentado=false;
		int order=1;
		boolean visible=true;
		int numBotones=0;
		Vector vValues=null;

//		IdObjectForm idObjectForm=new IdObjectForm();
//		idObjectForm.setIdo(1);
//		idObjectForm.setValueCls(dataType);
//		id=idObjectForm.getIdString();
		
		id=idExcelFile;

		defaultVal=null;
		
		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);
//		groupId=1;
//		balancer.addGroup(groupId,"",1);
//		tm=GConst.TM_BUTTON;
//		name="Aceptar";
//		label=name;
//		id=String.valueOf(0);
//		longitud=12;
//		order=0;
//
//		balancer.addItem(groupId,
//				tm,
//				id,
//				idTO,
//				prioridad,
//				mask,
//				enable,
//				nullable,
//				multivalued,
//				defaultVal,
//				label,
//				name,
//				longitud,
//				comentado,
//				order,
//				visible,
//				numBotones,
//				vValues,
//				false,
//				false
//		);
//
//		name="Cancelar";
//		label=name;
//		id=String.valueOf(botoneraAccion.CANCEL);
//
//		balancer.addItem(groupId,
//				tm,
//				id,
//				idTO,
//				prioridad,
//				mask,
//				enable,
//				nullable,
//				multivalued,
//				defaultVal,
//				label,
//				name,
//				longitud,
//				comentado,
//				order,
//				visible,
//				numBotones,
//				vValues,
//				false,
//				false
//		);


		balancer.process(true,new Insets(8,8,8,8),new Insets(4,4,4,4));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}
	
	public static GProcessedForm buildLicenseForm(String idLicense){

		Dimension dim=new Dimension(250,100);

		GViewBalancer balancer= newBalancer(dim,false);
		GProcessedForm viewForm=null;

		int groupId=0;
		balancer.addGroup(groupId,"",0);

		int tm=GConst.TM_TEXT;
		int dataType=Constants.IDTO_STRING;
		String id;
		String idTO="";
		int prioridad=0;
		String mask=null;
		boolean enable=true;
		boolean nullable=false;
		boolean multivalued=false;
		Object defaultVal="";
		String name="Licencia";
		String label=name;
		int longitud=-1;
		boolean comentado=false;
		int order=1;
		boolean visible=true;
		int numBotones=0;
		Vector vValues=null;
		
		id=idLicense;

		defaultVal=null;
		
		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				1,
				null
		);

		balancer.process(true,new Insets(8,8,8,8),new Insets(4,4,4,4));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}
	
	public static GProcessedForm buildEmailForm(String idEmail, String email, String idSubject, String subject, String idBody, String body){

		Dimension dim=new Dimension(ConstantesGraficas.dimScreenJDialog);

		GViewBalancer balancer= newBalancer(dim,false);
		GProcessedForm viewForm=null;

		int groupId=0;
		balancer.addGroup(groupId,"",0);

		int tm=GConst.TM_TEXT;
		String id;
		String idTO="";
		int prioridad=0;
		String mask=null;
		boolean enable=true;
		boolean nullable=false;
		boolean multivalued=false;
		Object defaultVal=email;
		String name="Email";
		String label=name;
		int longitud=-1;
		boolean comentado=false;
		int order=1;
		boolean visible=true;
		int numBotones=0;
		Vector vValues=null;
		int rows=1;
		
		id=idEmail;
		
		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				rows,
				null
		);
		
		defaultVal=subject;
		name="Asunto";
		label=name;
		order=2;
		id=idSubject;
		
		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				rows,
				null
		);
		
		tm=GConst.TM_MEMO;
		defaultVal=body;
		name="Mensaje";
		label=name;
		order=3;
		id=idBody;
		rows=15;
		
		balancer.addItem(groupId,
				tm,
				id,
				idTO,
				prioridad,
				mask,
				enable,
				nullable,
				multivalued,
				defaultVal,
				label,
				name,
				longitud,
				comentado,
				order,
				visible,
				numBotones,
				vValues,
				false,
				false,
				rows,
				null
		);

		balancer.process(true,new Insets(8,8,8,8),new Insets(4,4,4,4));
		if(!balancer.getProcessedFormList().isEmpty()){
			viewForm=balancer.getBestResult();
		}
		return viewForm;

	}

//	public static GProcessedForm buildFormConfigReport(String idPreviewView,Boolean defaultValue,Dimension dim){
//
//		GViewBalancer balancer= newBalancer(dim,true);
//		GProcessedForm viewForm=null;
//
//		int groupId=0;
//		balancer.addGroup(groupId,"",0);
//
//		int tm=GConst.TM_BOOLEAN;
//		int dataType=Constants.IDTO_BOOLEAN;
//		String id;
//		String idTO="";
//		int prioridad=0;
//		String mask=null;
//		boolean enable=true;
//		boolean nullable=false;
//		boolean multivalued=false;
//		String defaultVal=defaultValue!=null?defaultValue.toString():null;
//		String name=Utils.normalizeLabel("Vista Previa");
//		String label=name;
//		int longitud=-1;
//		boolean comentado=false;
//		int order=0;
//		boolean visible=true;
//		int numBotones=0;
//		Vector vValues=null;
//
//		id=idPreviewView;
//
//		balancer.addItem(groupId,
//				tm,
//				id,
//				idTO,
//				prioridad,
//				mask,
//				enable,
//				nullable,
//				multivalued,
//				defaultVal,
//				label,
//				name,
//				longitud,
//				comentado,
//				order,
//				visible,
//				numBotones,
//				vValues,
//				false,
//				false
//		);
//
//		balancer.process(true,new Insets(0,0,0,0),new Insets(0,0,0,0));
//		if(!balancer.getProcessedFormList().isEmpty()){
//			viewForm=balancer.getBestResult();
//		}
//		return viewForm;
//
//	}
	
	public static Integer getTypeField(int dataType){
		Integer typeField=null;
		switch(dataType){
		case Constants.IDTO_STRING:
			typeField=GConst.TM_TEXT;
			break;
		case Constants.IDTO_MEMO:
			typeField=GConst.TM_MEMO;
			break;
		case Constants.IDTO_INT:
			typeField=GConst.TM_INTEGER;
			break;
		case Constants.IDTO_DOUBLE:
			typeField=GConst.TM_REAL;
			break;
		case Constants.IDTO_BOOLEAN:
			typeField=GConst.TM_BOOLEAN;
			break;
		case Constants.IDTO_TIME:
			typeField=GConst.TM_HOUR;
			break;
		case Constants.IDTO_DATE:
			typeField=GConst.TM_DATE;
			break;
		case Constants.IDTO_DATETIME:
			typeField=GConst.TM_DATE_HOUR;
			break;
		case Constants.IDTO_UNIT:
			typeField=GConst.TM_REAL;
			break;		
		case Constants.IDTO_IMAGE:
			typeField=GConst.TM_IMAGE;
			break;
		case Constants.IDTO_FILE:
			typeField=GConst.TM_FILE;
			break;
		}
		return typeField;
	}

//	public static GProcessedForm buildFormularioPruebas(Graphics graphics,int idLogin,int idPassword){

//	Dimension dim=new Dimension(700,400);
//	/*Graphics2D gr2D=(Graphics2D)graphics;
//	FontRenderContext frc= gr2D.getFontRenderContext();
//	Font font= new  Font("Helvetica",  Font.PLAIN,  12);

//	GViewBalancer balancer= new GViewBalancer( dim,font,frc,true);*/

//	GViewBalancer balancer= newBalancer(graphics,dim,true);
//	GProcessedForm viewForm=null;

//	int groupId=0;
//	balancer.addGroup(groupId,"",0);

//	int tm=GConst.TM_TEXT;
//	int dataType=Constants.IDTO_STRING;
//	String id;
//	String idTO="";
//	int tapos=1;
//	int prioridad=0;
//	String mask=null;
//	boolean enable=true;
//	boolean nullable=false;
//	boolean multivalued=false;
//	String defaultVal="";
//	String label="Usuario";
//	int longitud=22;
//	boolean comentado=false;
//	int order=0;//1;
//	boolean visible=true;
//	int numBotones=0;
//	Vector vValues=null;

//	IdObjectForm idObjectForm=new IdObjectForm();
//	idObjectForm.setIdo(idLogin);
//	idObjectForm.setValueCls(dataType);
//	id=idObjectForm.getIdString();

//	balancer.addItem(groupId,
//	tm,
//	id,
//	idTO,
//	tapos,
//	prioridad,
//	mask,
//	enable,
//	nullable,
//	multivalued,
//	defaultVal,
//	label,
//	longitud,
//	comentado,
//	order,
//	visible,
//	numBotones,
//	vValues
//	);

//	idObjectForm.setIdo(idPassword);
//	idObjectForm.setValueCls(dataType);
//	id=idObjectForm.getIdString();

//	label="Password";
//	//order=2;

//	balancer.addItem(groupId,
//	tm,
//	id,
//	idTO,
//	tapos,
//	prioridad,
//	mask,
//	enable,
//	nullable,
//	multivalued,
//	defaultVal,
//	label,
//	longitud,
//	comentado,
//	order,
//	visible,
//	numBotones,
//	vValues
//	);

//	label="Password";
//	//order=2;

//	balancer.addItem(groupId,
//	tm,
//	id,
//	idTO,
//	tapos,
//	prioridad,
//	mask,
//	enable,
//	nullable,
//	multivalued,
//	defaultVal,
//	label,
//	longitud,
//	comentado,
//	order,
//	visible,
//	numBotones,
//	vValues
//	);

//	label="Password";
//	//order=2;

//	balancer.addItem(groupId,
//	tm,
//	id,
//	idTO,
//	tapos,
//	prioridad,
//	mask,
//	enable,
//	nullable,
//	multivalued,
//	defaultVal,
//	label,
//	longitud,
//	comentado,
//	order,
//	visible,
//	numBotones,
//	vValues
//	);

//	balancer.addTable(groupId,
//	id,
//	prioridad,
//	enable,
//	Utils.normalizeLabel(label),
//	order,visible,4,new Vector(),new Vector(),-1,-1,false,
//	-1,-1,-1,false,numBotones,-1,-1,new HashMap<Integer,UserAccess>());

//	balancer.addTable(groupId,
//	id,
//	prioridad,
//	enable,
//	Utils.normalizeLabel(label),
//	order,visible,4,new Vector(),new Vector(),-1,-1,false,
//	-1,-1,-1,false,numBotones,-1,-1,new HashMap<Integer,UserAccess>());


//	balancer.process(true,new Insets(8,8,8,8),new Insets(4,4,4,4));
//	if(!balancer.getProcessedFormList().isEmpty()){
//	viewForm=balancer.getBestResult();
//	}
//	return viewForm;

//	}

}
