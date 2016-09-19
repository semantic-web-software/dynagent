package dynagent.common.xml;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.EssentialProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.Mask;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.Required;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.Auxiliar;

public class metaComClient {

	public metaComClient(Element metaDataXML)
	throws SystemException,RemoteSystemException,CommunicationException{	
		
	}
	
			
	public LinkedList<Instance> buildModel(Element datainstances){
		LinkedList<Instance> instancias = new LinkedList<Instance>();
		Iterator itinstances = datainstances.getChildren().iterator();
		while(itinstances.hasNext()){
			Element instanceElement = (Element)itinstances.next();
			Instance i = new Instance();
			if(instanceElement.getAttributeValue("IDTO") != null)
				i.setIDTO(instanceElement.getAttributeValue("IDTO").toString());
			if(instanceElement.getAttributeValue("IDO") != null)
				i.setIDO(instanceElement.getAttributeValue("IDO").toString());
			if(instanceElement.getAttributeValue("PROP") != null)
				i.setPROPERTY(instanceElement.getAttributeValue("PROP").toString());
			if(instanceElement.getAttributeValue("VAL") != null)
				i.setVALUE(instanceElement.getAttributeValue("VAL").toString());
			if(instanceElement.getAttributeValue("VALCLS") != null)
				i.setVALUECLS(instanceElement.getAttributeValue("VALCLS").toString());
			if(instanceElement.getAttributeValue("Q_MIN") != null)
				i.setQMIN(instanceElement.getAttributeValue("Q_MIN").toString());
			if(instanceElement.getAttributeValue("Q_MAX") != null)
				i.setQMAX(instanceElement.getAttributeValue("Q_MAX").toString());
			if(instanceElement.getAttributeValue("OP") != null)
				i.setOP(instanceElement.getAttributeValue("OP").toString());
			if(instanceElement.getAttributeValue("NAME") != null)
				i.setNAME(instanceElement.getAttributeValue("NAME").toString());
			if(instanceElement.getAttributeValue("VIRTUAL") != null)
				i.setVIRTUAL(Boolean.valueOf(instanceElement.getAttributeValue("VIRTUAL").toString()));
			instancias.add(i);
		}
		return instancias;
	}
	
	public LinkedList<O_Datos_Attrib> buildBusinessClass(Element businessClass){
		LinkedList<O_Datos_Attrib> oDatosAttrib = new LinkedList<O_Datos_Attrib>();
		/*Iterator itbusinessClass = businessClass.getChildren().iterator();
		while(itbusinessClass.hasNext()){
			Element businessClassElement = (Element)itbusinessClass.next();
			O_Datos_Attrib at = new O_Datos_Attrib();
			/if(businessClassElement.getAttributeValue("IDTO") != null)
				at.setIDTO(businessClassElement.getAttributeValue("IDTO"));
			if(businessClassElement.getAttributeValue("IDO") != null)
				at.setIDO(businessClassElement.getAttributeValue("IDO"));
			if(businessClassElement.getAttributeValue("PROP") != null)
				at.setPROPERTY(businessClassElement.getAttributeValue("PROP"));
			if(businessClassElement.getAttributeValue("VAL") != null)
				at.setVALUE(businessClassElement.getAttributeValue("VAL").toString());
			if(businessClassElement.getAttributeValue("VALCLS") != null)
				at.setVALUECLS(businessClassElement.getAttributeValue("VALCLS"));
			if(businessClassElement.getAttributeValue("Q_MIN") != null)
				at.setQMIN(businessClassElement.getAttributeValue("Q_MIN"));
			if(businessClassElement.getAttributeValue("Q_MAX") != null)
				at.setQMAX(businessClassElement.getAttributeValue("Q_MAX"));
			if(businessClassElement.getAttributeValue("OP") != null)
				at.setOP(businessClassElement.getAttributeValue("OP").toString());
			oDatosAttrib.add(at);
		}
		*/
		return oDatosAttrib;
	}

	public LinkedList<Properties> buildProp(Element dataprop){
		LinkedList<Properties> propiedades = new LinkedList<Properties>();
		Iterator itproperties = dataprop.getChildren().iterator();
		while(itproperties.hasNext()){
			Element propertiesElement = (Element)itproperties.next();
			Properties p = new Properties();
			if(propertiesElement.getAttributeValue("PROP") != null)
				p.setPROP(new Integer(propertiesElement.getAttributeValue("PROP").toString()).intValue());
			if(propertiesElement.getAttributeValue("NAME") != null)
				p.setNAME(propertiesElement.getAttributeValue("NAME").toString());
			if(propertiesElement.getAttributeValue("CAT") != null)
				p.setCAT(new Integer(propertiesElement.getAttributeValue("CAT")));
//			if(propertiesElement.getAttributeValue("VALUE") != null)
//				p.setVALUE(propertiesElement.getAttributeValue("VALUE").toString());
			if(propertiesElement.getAttributeValue("VALUECLS") != null)
				p.setVALUECLS(Integer.parseInt(propertiesElement.getAttributeValue("VALUECLS")));
//			if(propertiesElement.getAttributeValue("OP") != null)
//				p.setOP(propertiesElement.getAttributeValue("OP").toString());
			if(propertiesElement.getAttributeValue("PROPINV") != null)
				p.setPROPINV(new Integer (propertiesElement.getAttributeValue("PROPINV").toString()));
//			if(propertiesElement.getAttributeValue("QMAX") != null)
//				p.setQMAX(new Float (propertiesElement.getAttributeValue("QMAX").toString()));
//			if(propertiesElement.getAttributeValue("QMIN") != null)
//				p.setQMIN(new Float (propertiesElement.getAttributeValue("QMIN").toString()));
			
			propiedades.add(p);
		}
		return propiedades;
	}
	
	public ArrayList<OrderProperty> buildOrderProp(Element orderprop){
		ArrayList<OrderProperty> orderpropiedades = new ArrayList<OrderProperty>();
		Iterator itorderproperties = orderprop.getChildren().iterator();
		while(itorderproperties.hasNext()){
			Element propertiesElement = (Element)itorderproperties.next();
			OrderProperty op = new OrderProperty();
			if(propertiesElement.getAttributeValue("PROP") != null)
				op.setProp(new Integer(propertiesElement.getAttributeValue("PROP").toString()).intValue());
			if(propertiesElement.getAttributeValue("GROUP") != null)
				op.setGroup(Integer.valueOf(propertiesElement.getAttributeValue("GROUP")));
			if(propertiesElement.getAttributeValue("SEC") != null)
				op.setSec(new Integer(propertiesElement.getAttributeValue("SEC")));
			if(propertiesElement.getAttributeValue("IDTO") != null)
				op.setIdto(Integer.valueOf(propertiesElement.getAttributeValue("IDTO")));
			if(propertiesElement.getAttributeValue("ORDER") != null)
				op.setOrder(Integer.parseInt(propertiesElement.getAttributeValue("ORDER")));
			orderpropiedades.add(op);
		}
		return orderpropiedades;
	}
	
	public ArrayList<Groups> buildGroups(Element groups){
		ArrayList<Groups> groupsList = new ArrayList<Groups>();
		Iterator itGroups = groups.getChildren().iterator();
		while(itGroups.hasNext()){
			Element groupsElement = (Element)itGroups.next();
			Groups gp = new Groups();
			if(groupsElement.getAttributeValue("ID") != null)
				gp.setIdGroup(new Integer(groupsElement.getAttributeValue("ID").toString()).intValue());
			if(groupsElement.getAttributeValue("PROP") != null)
				gp.setIdProp(new Integer(groupsElement.getAttributeValue("PROP").toString()).intValue());
			if(groupsElement.getAttributeValue("NAME") != null)
				gp.setNameGroup(groupsElement.getAttributeValue("NAME"));
			if(groupsElement.getAttributeValue("TASK") != null)
				gp.setUTask(new Integer(groupsElement.getAttributeValue("TASK")));
			if(groupsElement.getAttributeValue("IDTOCLASS") != null)
				gp.setIdtoClass(Integer.valueOf(groupsElement.getAttributeValue("IDTOCLASS")));
			if(groupsElement.getAttributeValue("ORDER") != null)
				gp.setOrder(new Integer(groupsElement.getAttributeValue("ORDER").toString()).intValue());
			groupsList.add(gp);
		}
		return groupsList;
	}

	public ArrayList<Alias> buildAlias(Element lstalias){
		ArrayList<Alias> listAlias = new ArrayList<Alias>();
		Iterator italias = lstalias.getChildren().iterator();
		while(italias.hasNext()){
			Element alias = (Element)italias.next();
			Alias al = new Alias();
			if(alias.getAttributeValue("UTASK") != null)
				al.setUTask(Integer.parseInt(alias.getAttributeValue("UTASK")));
			if(alias.getAttributeValue("GROUP") != null)
				al.setGroup(Integer.parseInt(alias.getAttributeValue("GROUP")));
			if(alias.getAttributeValue("CLASS") != null)
				al.setIdto(Integer.parseInt(alias.getAttributeValue("CLASS")));
			if(alias.getAttributeValue("PROP") != null)
				al.setProp(Integer.parseInt(alias.getAttributeValue("PROP")));
			if(alias.getAttributeValue("UTASKNAME") != null)
				al.setUTaskName(alias.getAttributeValue("UTASKNAME"));
			if(alias.getAttributeValue("GROUPNAME") != null)
				al.setGroupName(alias.getAttributeValue("GROUPNAME"));
			if(alias.getAttributeValue("CLASSNAME") != null)
				al.setIdtoName(alias.getAttributeValue("CLASSNAME"));
			if(alias.getAttributeValue("PROPNAME") != null)
				al.setPropName(alias.getAttributeValue("PROPNAME"));
			al.setAlias(alias.getAttributeValue("ALIAS").toString());
			
			listAlias.add(al);
		}
		return listAlias;
	}
	
	
	public ArrayList<CardMed> buildCardMed(Element lstcardmed){
		ArrayList<CardMed> listCM = new ArrayList<CardMed>();
		Iterator itcm = lstcardmed.getChildren().iterator();
		while(itcm.hasNext()){
			Element cm = (Element)itcm.next();
			CardMed cmedia = new CardMed();
			if(cm.getAttributeValue("IDTONAME") != null)
				cmedia.setIdtoName(cm.getAttributeValue("IDTONAME"));
			if(cm.getAttributeValue("IDTOPARENTNAME") != null)
				cmedia.setIdtoParentName(cm.getAttributeValue("IDTOPARENTNAME"));
			if(cm.getAttributeValue("IDTO") != null)
				cmedia.setIdto(Integer.parseInt(cm.getAttributeValue("IDTO")));
			if(cm.getAttributeValue("IDTOPARENT") != null)
				cmedia.setIdtoParent(Integer.parseInt(cm.getAttributeValue("IDTOPARENT")));
			if(cm.getAttributeValue("CM") != null)
				cmedia.setCardmed(Integer.parseInt(cm.getAttributeValue("CM")));
			if(cm.getAttributeValue("PROP") != null)
				cmedia.setIdProp(Integer.parseInt(cm.getAttributeValue("PROP")));
			if(cm.getAttributeValue("PROPNAME") != null)
				cmedia.setIdPropName(cm.getAttributeValue("PROPNAME"));
			listCM.add(cmedia);
		}
		return listCM;
	}
	
	public LinkedList<T_Herencias> buildParents(Element datapar){
		LinkedList<T_Herencias> padres = new LinkedList<T_Herencias>();
		Iterator itpadres = datapar.getChildren().iterator();
		while(itpadres.hasNext()){
			Element hierarchyElement = (Element)itpadres.next();
			T_Herencias t = new T_Herencias();
			if(hierarchyElement.getAttributeValue("ID_TO") != null)
				t.setID_TO(new Integer(hierarchyElement.getAttributeValue("ID_TO").toString()).intValue());
			if(hierarchyElement.getAttributeValue("ID_TO_PARENT") != null)
				t.setID_TO_Padre(new Integer(hierarchyElement.getAttributeValue("ID_TO_PARENT").toString()).intValue());
			padres.add(t);
		}
		return padres;
	}

	private LinkedList<Integer> getParents(Instance i, LinkedList<T_Herencias> padres)
	{
		int idto = new Integer(i.getIDTO()).intValue();
		LinkedList<Integer> idtos = new LinkedList<Integer>();
		Iterator it = padres.iterator();
		while(it.hasNext()){
			T_Herencias t = (T_Herencias)it.next();
			if(idto == t.getID_TO())
				idtos.add(t.getID_TO_Padre());
		}
		return idtos;
	}


	public LinkedList<Access> buildAccess(Element dataaccess, String user, HashSet<String> roles) {
		LinkedList<Access> accesses= new LinkedList<Access>();
		Iterator itaccesses = dataaccess.getChildren().iterator();
		while(itaccesses.hasNext()){
			Element accessesElement = (Element)itaccesses.next();
			Access a = new Access();
			if(accessesElement.getAttributeValue("DENNIED") != null)
				a.setDENNIED(new Integer(accessesElement.getAttributeValue("DENNIED").toString()).intValue());
			if(accessesElement.getAttributeValue("TASK") != null)
				a.setTASK(new Integer(accessesElement.getAttributeValue("TASK").toString()).intValue());
			if(accessesElement.getAttributeValue("USERROL") != null)
				a.setUSERROL(new ArrayList<String>(Arrays.asList(accessesElement.getAttributeValue("USERROL").toString())));
			if(accessesElement.getAttributeValue("USER") != null)
				a.setUSER(new ArrayList<String>(Arrays.asList(accessesElement.getAttributeValue("USER").toString())));
			if(accessesElement.getAttributeValue("ACCESSTYPENAME") != null)
				a.setACCESSTYPENAME(new ArrayList<String>(Arrays.asList(accessesElement.getAttributeValue("ACCESSTYPENAME").split(";"))));
			if(accessesElement.getAttributeValue("IDTO") != null)
				a.setIDTO(new Integer(accessesElement.getAttributeValue("IDTO").toString()).intValue());
			if(accessesElement.getAttributeValue("PROP") != null)
				a.setPROP(new Integer(accessesElement.getAttributeValue("PROP").toString()).intValue());
			a.setPRIORITY(Integer.parseInt(accessesElement.getAttributeValue("PRIORITY").toString()));
			
			boolean valid = true;
			if(a.getUSER()!=null && !a.getUSER().contains(user))
				valid = false;
			else if(a.getUSERROL()!=null && !a.getUSERROL().isEmpty()){
				valid=false;
				if(roles!=null && roles.size()!=0){
					Iterator<String> itrUserRol=a.getUSERROL().iterator();
					while(!valid && itrUserRol.hasNext()){
						String userRol=itrUserRol.next();
						if(roles.contains(userRol)){
							valid=true;
							//Le quitamos el userRol porque por la aplicacion no se esta propagando correctamente el userrol(por el applet es null) por lo
							//que no encontraria este acceso si dejamos el userrol
							a.setUSERROL(null);
						}
					}
				}
			}
			
			if(valid){
				accesses.add(a);
			}
		}
		return accesses;
	}


	public ArrayList<ColumnProperty> buildColumnProperties(Element child) {
		ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
		Iterator itColProp = child.getChildren().iterator();
		while(itColProp.hasNext()){
			Element column = (Element)itColProp.next();
			ColumnProperty cp = new ColumnProperty();
			if(column.getAttributeValue("CLASSPARENT") != null)
				cp.setIdtoParent(Integer.valueOf(column.getAttributeValue("CLASSPARENT")));
			if(column.getAttributeValue("CLASS") != null)
				cp.setIdto(Integer.valueOf(column.getAttributeValue("CLASS")));
			if(column.getAttributeValue("PROP") != null)
				cp.setIdProp(Integer.valueOf(column.getAttributeValue("PROP")));
			if(column.getAttributeValue("PROPFILTER") != null)
				cp.setPropFilter(column.getAttributeValue("PROPFILTER"));
			if(column.getAttributeValue("VALUEFILTER") != null)
				cp.setValueFilter(column.getAttributeValue("VALUEFILTER"));
			if(column.getAttributeValue("IDPROPF") != null)
				cp.setIdPropF(Integer.valueOf(column.getAttributeValue("IDPROPF")));
			if(column.getAttributeValue("ORDER") != null)
				cp.setPriority(Integer.valueOf(column.getAttributeValue("ORDER")));
			if(column.getAttributeValue("PROPPATH") != null)
				cp.setIdPropPath(column.getAttributeValue("PROPPATH"));
			columnProperties.add(cp);
		}
		return columnProperties;
	}
	
	public ArrayList<EssentialProperty> buildEssentialProperties(Element lstessential){
		ArrayList<EssentialProperty> listEssential = new ArrayList<EssentialProperty>();
		Iterator itessential = lstessential.getChildren().iterator();
		while(itessential.hasNext()){
			Element essential = (Element)itessential.next();
			EssentialProperty ep = new EssentialProperty();
			if(essential.getAttributeValue("UTASK") != null)
				ep.setUTask(Integer.parseInt(essential.getAttributeValue("UTASK")));
			if(essential.getAttributeValue("CLASS") != null)
				ep.setIdto(Integer.parseInt(essential.getAttributeValue("CLASS")));
			if(essential.getAttributeValue("PROP") != null)
				ep.setProp(Integer.parseInt(essential.getAttributeValue("PROP")));

			listEssential.add(ep);
		}
		return listEssential;
	}

	public ArrayList<ListenerUtask> buildListenerUtasks(Element lstlistener){
		ArrayList<ListenerUtask> listListener = new ArrayList<ListenerUtask>();
		Iterator itlistener = lstlistener.getChildren().iterator();
		while(itlistener.hasNext()){
			Element listener = (Element)itlistener.next();
			ListenerUtask lu = new ListenerUtask();
			if(listener.getAttributeValue("UTASK") != null)
				lu.setUtask(Integer.parseInt(listener.getAttributeValue("UTASK")));
			if(listener.getAttributeValue("RGB") != null)
				lu.setRgb(Integer.parseInt(listener.getAttributeValue("RGB")));
			if(listener.getAttributeValue("MINUTE") != null)
				lu.setUpdatePeriod(Integer.parseInt(listener.getAttributeValue("MINUTE")));

			listListener.add(lu);
		}
		return listListener;
	}
	
	public ArrayList<Mask> buildMasks(Element lstmask){
		ArrayList<Mask> listMask = new ArrayList<Mask>();
		Iterator itmask = lstmask.getChildren().iterator();
		while(itmask.hasNext()){
			Element mask = (Element)itmask.next();
			Mask mk = new Mask();
			if(mask.getAttributeValue("UTASK") != null)
				mk.setUTask(Integer.parseInt(mask.getAttributeValue("UTASK")));
			if(mask.getAttributeValue("CLASS") != null)
				mk.setIdto(Integer.parseInt(mask.getAttributeValue("CLASS")));
			if(mask.getAttributeValue("PROP") != null)
				mk.setProp(Integer.parseInt(mask.getAttributeValue("PROP")));
			if(mask.getAttributeValue("UTASKNAME") != null)
				mk.setUTaskName(mask.getAttributeValue("UTASKNAME"));
			if(mask.getAttributeValue("CLASSNAME") != null)
				mk.setIdtoName(mask.getAttributeValue("CLASSNAME"));
			if(mask.getAttributeValue("PROPNAME") != null)
				mk.setPropName(mask.getAttributeValue("PROPNAME"));
			if(mask.getAttributeValue("EXPRESSION") != null)
				mk.setExpression(mask.getAttributeValue("EXPRESSION"));
			if(mask.getAttributeValue("LENGTH") != null)
				mk.setLength(Integer.parseInt(mask.getAttributeValue("LENGTH")));
			
			listMask.add(mk);
		}
		return listMask;
	}
	
	public HashSet<Integer> buildIndex(Element lstIndexes) {
		HashSet<Integer> indexes= new HashSet<Integer>();
		Iterator itindexes = lstIndexes.getChildren().iterator();
		while(itindexes.hasNext()){
			Element indexesElement = (Element)itindexes.next();
			indexes.add(new Integer(indexesElement.getAttributeValue("IDTO").toString()).intValue());
		}
		return indexes;
	}
	
	public ArrayList<Required> buildRequired(Element lstrequired){
		ArrayList<Required> listRequired = new ArrayList<Required>();
		Iterator itrequired = lstrequired.getChildren().iterator();
		while(itrequired.hasNext()){
			Element required = (Element)itrequired.next();
			Required r = new Required();
			if(required.getAttributeValue("CLASS") != null)
				r.setIdtoClass(Integer.parseInt(required.getAttributeValue("CLASS")));
			if(required.getAttributeValue("PROP") != null)
				r.setIdProp(Integer.parseInt(required.getAttributeValue("PROP")));

			listRequired.add(r);
		}
		return listRequired;
	}
	
	public ArrayList<Integer> buildGlobalUtasks(Element lstglobal){
		if(lstglobal==null) return null;
		ArrayList<Integer> listGlobal = new ArrayList<Integer>();
		Iterator itglobal = lstglobal.getChildren().iterator();
		while(itglobal.hasNext()){
			Element global = (Element)itglobal.next();
			listGlobal.add(Integer.parseInt(global.getAttributeValue("UTASK")));
		}
		return listGlobal;
	}
	
//	private void	buildCategoriasRel(){
//		Iterator itr=m_md.m_metaTOs.keySet().iterator();
//		while( itr.hasNext() ){
//			Integer id=(Integer)itr.next();
//			Element mTO= m_md.getMetaTO( id );
//			if( mTO.getChild("RELACIONES")==null ) continue;
//
//			Iterator iR= mTO.getChild("RELACIONES").getChildren("RELACION").iterator();
//			while( iR.hasNext() ){
//				Element rel=(Element)iR.next();
//				m_md.addCategoriaRel( 	Integer.parseInt( rel.getAttributeValue("ID_REL") ),
//							Integer.parseInt( rel.getAttributeValue("CATEGORY") ));
//			}
//		}
//	}
//
//	public void buildAreasFunc(Element root){
//		Iterator iP= root.getChildren().iterator();
//		while( iP.hasNext() ){
//			Element eP= (Element)iP.next();
//			String label= eP.getAttributeValue("LABEL");
//			m_md.m_areasFuncionales.put( eP.getAttributeValue("ID"), label );
//		}
//	}
//
	public HashSet<String> buildUserRoles(Element elemURoles){
		HashSet<String> arrayURoles = new HashSet<String>();
		if (elemURoles!=null) {
			String userRoles = elemURoles.getAttributeValue("UROLES");
			String[] uRoles = userRoles.split(",");
			for (int i=0;i<uRoles.length;i++){
				arrayURoles.add(uRoles[i]);
			}
		}
		return arrayURoles;
	}
//
//	public void buildReports(Element root){
//		Iterator iR= root.getChildren().iterator();
//		while( iR.hasNext() ){
//			Element eR= (Element)iR.next();
//			String oid= eR.getAttributeValue("OID");
//			String name= eR.getAttributeValue("NAME");
//			String plantilla= eR.getAttributeValue("PLANTILLA");
//			int filter= Integer.parseInt(eR.getAttributeValue("FILTER"));
//			m_md.addDinReport( oid, name, filter, plantilla );
//			
//		}
//	}
//
//	public void buildMetaTOs(Element root){
//		Iterator iTO= root.getChildren().iterator();
//		while( iTO.hasNext() ){
//			Element eTO= (Element)iTO.next();
//			Integer idto= new Integer( eTO.getAttributeValue("ID_TO"));
//
//			m_md.addMetaTO( idto.intValue(), eTO );
//		}
//	}
//
//	public void buildFormOrder(Element root){
//		if( root==null ) return;
//		Iterator iF= root.getChildren().iterator();
//		while( iF.hasNext() ){
//			Element item= (Element)iF.next();
//			Integer idc= new Integer( item.getAttributeValue("IDC"));
//			Integer ide= new Integer( item.getAttributeValue("IDE"));
//			int tipo= Integer.parseInt( item.getAttributeValue("TIPO"));
//			boolean isTO= item.getAttributeValue("ISTO").equals("TRUE");
//			int order= Integer.parseInt( item.getAttributeValue("ORDER"));
//			m_md.addFormOrder( idc, ide,isTO,tipo,order );
//		}
//	}
//
//	public void buildIndices(Element root){
//		Iterator iIn= root.getChildren().iterator();
//		while( iIn.hasNext() ){
//			Element item= (Element)iIn.next();
//			Integer idDom= new Integer( item.getAttributeValue("ID_DOM"));
//			atReference atr= new atReference( 	Integer.parseInt( item.getAttributeValue("ID_FILTER")),
//								Integer.parseInt( item.getAttributeValue("NODE_FILTER")),
//								Integer.parseInt( item.getAttributeValue("TA_POS")));
//
//			m_md.m_indices.put( idDom, atr );
//			if( item.getAttributeValue("SUBDOM_REF") !=null )
//				atr.idSubdomRef= Integer.parseInt( item.getAttributeValue("SUBDOM_REF") );
//			if( item.getAttributeValue("CASCADING") !=null )
//				atr.idFilterCascading=Integer.parseInt( item.getAttributeValue("CASCADING") );
//
//			if( item.getAttributeValue("TAPOS_POINTER") !=null )
//				atr.pointerTapos=Integer.parseInt( item.getAttributeValue("TAPOS_POINTER") );
//		}
//	}
//
//	public void buildProcesos(Element root){
//		Iterator iPro= root.getChildren().iterator();
//		while( iPro.hasNext() ){
//			Element item= (Element)iPro.next();
//			processType pt= new processType(Integer.parseInt( item.getAttributeValue("ID") ),
//				item.getAttributeValue("LABEL"),
//				Integer.parseInt(item.getAttributeValue("START_STATE")));
//			m_md.addProcess( pt );
//		}
//	}
//
//	public void buildTasks(Element root){
//		Iterator iTask= root.getChildren().iterator();
//		while( iTask.hasNext() ){
//		    Element item= (Element)iTask.next();
//		    Integer idTask= new Integer( item.getAttributeValue("ID"));
//		    m_md.addTask( 	idTask.intValue(),
//		    		item.getAttributeValue("LABEL"),
//					Integer.parseInt( item.getAttributeValue("START")),
//					Integer.parseInt( item.getAttributeValue("AT_ST")));
//		    taskType tt=m_md.getTask(idTask);
//		    Iterator iS= item.getChildren("STATE").iterator();
//		    while(iS.hasNext()){
//			Element eSt=(Element)iS.next();
//			int id= Integer.parseInt(eSt.getAttributeValue("ID"));
//			boolean lock= eSt.getAttributeValue("LOCK")!=null &&
//				      eSt.getAttributeValue("LOCK").equals("TRUE");
//			tt.addState(id,lock);
//		    }
//		}
//	}
//
//	public void buildTaskFilter(Element root){
//		Iterator iP= root.getChildren().iterator();
//		while( iP.hasNext() ){
//			Element eP= (Element)iP.next();
//			Integer id= new Integer( eP.getAttributeValue("ID"));
//			Iterator iT= eP.getChildren().iterator();
//			while( iT.hasNext() ){
//				Element eT= (Element)iT.next();
//				Integer idT= new Integer( eT.getAttributeValue("ID"));
//				Iterator iTt= eT.getChildren().iterator();
//				while( iTt.hasNext() ){
//					Element eTt= (Element)iTt.next();
//					Integer idTt= new Integer( eTt.getAttributeValue("ID"));
//					Iterator iC= eTt.getChildren().iterator();
//					while( iC.hasNext() ){
//						Element eC= (Element)iC.next();
//						Integer idC= new Integer( eC.getAttributeValue("ID"));
//						m_md.addTaskContextFix( id.intValue(),
//									idT.intValue(),
//									idTt.intValue(),
//									idC.intValue(),
//									false );
//					}
//				}
//			}
//		}
//	}
//
//	public void buildLabels(Element root, HashMap lista){
//		Iterator iP= root.getChildren().iterator();
//		while( iP.hasNext() ){
//			Element eP= (Element)iP.next();
//			Integer id= new Integer( eP.getAttributeValue("ID"));
//			String label= eP.getAttributeValue("LABEL");
//			lista.put( id, label );
//		}
//	}
//
//	public void buildTransitions(Element root){
//		Iterator itr= root.getChildren().iterator();
//		while( itr.hasNext() ){
//			Element eTt= (Element)itr.next();
//			int id= Integer.parseInt( eTt.getAttributeValue("ID"));
//			int idTask= Integer.parseInt( eTt.getAttributeValue("TASK_TYPE"));
//			int stIni= Integer.parseInt( eTt.getAttributeValue("STATE_INI"));
//			int stEnd= Integer.parseInt( eTt.getAttributeValue("STATE_END"));
//			int owPolicy= Integer.parseInt( eTt.getAttributeValue("OW_POLICY"));
//			int action= eTt.getAttributeValue("ACT_MAIN")==null ? 0:
//						Integer.parseInt( eTt.getAttributeValue("ACT_MAIN"));
//
//			int ctx= eTt.getAttributeValue("CTX_MAIN")==null ? 0:
//						Integer.parseInt( eTt.getAttributeValue("CTX_MAIN"));
//
//			String label= eTt.getAttributeValue("LABEL");
//			m_md.addTrans( id, new taskTransition( id, idTask, stIni, stEnd, label, action, ctx, owPolicy ));
//		}
//	}
//
//	public void buildActions(Element root){
//		Iterator itr= root.getChildren().iterator();
//		while( itr.hasNext() ){
//			Element eAct= (Element)itr.next();
//			int id= Integer.parseInt( eAct.getAttributeValue("ID"));
//			int type= Integer.parseInt( eAct.getAttributeValue("TYPE"));
//			int tran= Integer.parseInt( eAct.getAttributeValue("TRAN"));
//			int op= Integer.parseInt( eAct.getAttributeValue("OP"));
//			m_md.addActionData( id, type, tran, null, op );
//		}
//	}
//
//	public void buildHerencias(Element root){
//		Iterator iH= root.getChildren("TYPE").iterator();
//		while( iH.hasNext() ){
//			Element eTO= (Element)iH.next();
//			int to= Integer.parseInt( eTO.getAttributeValue("ID_TO"));
//			String[] sups= eTO.getText().split(";");
//			for( int i=0; i< sups.length; i++){
//				int sup= Integer.parseInt( sups[i] );
//				m_md.addSuperiorMap( to, sup );
//			}
//			m_md.addSuperiorMap( to, to );
//		}
//	}
//
//	public void buildTOLabels(Element root ){
//		Iterator iTO= root.getChildren("ITEM").iterator();
//		while( iTO.hasNext() ){
//			Element eTO= (Element)iTO.next();
//			int to= Integer.parseInt( eTO.getAttributeValue("ID_TO"));
//			m_md.addTOLabel( to, eTO.getAttributeValue("LABEL") );
//		}
//	}
//
//	public void buildEnumLabels(Element root ){
//		System.out.println("DENTRO METACOM:");
//		Iterator iAt= root.getChildren("AT").iterator();
//		while( iAt.hasNext() ){
//			Element eAt= (Element)iAt.next();
//			int tapos= Integer.parseInt( eAt.getAttributeValue("TA_POS"));
//			System.out.println("AT:"+tapos);
//			Iterator iVal= eAt.getChildren("VAL").iterator();
//			while( iVal.hasNext() ){
//				Element eVal= (Element)iVal.next();
//				int val= Integer.parseInt( eVal.getAttributeValue("ID"));
//				m_md.addEnumMap(tapos, val, eVal.getAttributeValue("LABEL") );
//				System.out.println("MAP:"+eVal.getAttributeValue("LABEL"));
//			}
//		}
//	}
//
//	public void buildContextos(Element root ){
//		Iterator iC= root.getChildren("ITEM").iterator();
//		while( iC.hasNext() ){
//			Element eC= (Element)iC.next();
//			int ctx= Integer.parseInt( eC.getAttributeValue("ID"));
//			int to= Integer.parseInt( eC.getAttributeValue("ID_TO"));
//			int toRoot= Integer.parseInt( eC.getAttributeValue("ID_TO_ROOT"));
//			int filter= Integer.parseInt( eC.getAttributeValue("ID_FILTER"));
//			int rel= Integer.parseInt( eC.getAttributeValue("REL") );
//			int rolCtx= Integer.parseInt( eC.getAttributeValue("ROL_CTX") );
//			int rolCurr= Integer.parseInt( eC.getAttributeValue("ROL_CURR") );
//			int action= Integer.parseInt( eC.getAttributeValue("ID_ACTION") );
//			boolean fPersis= eC.getAttributeValue("F_PERSISTENCE")==null ? false:
//					 eC.getAttributeValue("F_PERSISTENCE").equals("TRUE");
//			int nMin= Integer.parseInt( eC.getAttributeValue("N_MIN") );
//			int nMax= Integer.parseInt( eC.getAttributeValue("N_MAX") );
//			boolean meta= eC.getAttributeValue("META").equals("TRUE");
//			int process=0, task=0,tran=0, idImg=0;
//			if( eC.getAttributeValue("ID_PROCESS")!=null ){
//				process= Integer.parseInt( eC.getAttributeValue("ID_PROCESS") );
//				task= Integer.parseInt( eC.getAttributeValue("ID_TASK") );
//				tran= Integer.parseInt( eC.getAttributeValue("ID_TRAN") );
//			}
//			if( eC.getAttributeValue("ID_IMG")!=null ){
//				idImg= Integer.parseInt( eC.getAttributeValue("ID_IMG") );
//			}
//			boolean childIsSup= eC.getAttributeValue("CHILD_IS_SUP").equals("TRUE");
//			Contexto cc= new Contexto( 	meta,ctx, process,task,tran,action,toRoot,to,
//							rel,rolCtx,rolCurr,filter,
//							nMin, nMax, childIsSup, idImg,fPersis );
//			Iterator iF= eC.getChildren("FIX").iterator();
//			while( iF.hasNext() ){
//				Element eFix=(Element)iF.next();
//				int ctxFix=eFix.getAttributeValue("CTX")==null ? 0:
//						Integer.parseInt(eFix.getAttributeValue("CTX"));
//
//				int refFix=eFix.getAttributeValue("REF_SOURCE")==null ? 0:
//						Integer.parseInt(eFix.getAttributeValue("REF_SOURCE"));
//
//				Element detail= eFix.hasChildren() ? (Element)eFix.clone():null;
//				int incrustar=eFix.getAttributeValue("INCRUSTAR")==null ? 0:
//						Integer.parseInt(eFix.getAttributeValue("INCRUSTAR"));
//				cc.addFilterFix(eFix.getAttributeValue("OPTIONAL").equals("TRUE"),
//						eFix.getAttributeValue("NODE"),
//						ctxFix,
//						refFix,
//						detail, incrustar );
//			}
//			Iterator iA= eC.getChildren("AREAF").iterator();
//			while( iA.hasNext() ){
//				Element eArea=(Element)iA.next();
//				cc.addAreaFuncional( eArea.getAttributeValue("ID") );
//			}
//			Iterator iR= eC.getChildren("ROL_PROPERTIES").iterator();
//			while( iR.hasNext() ){
//				Element prop=(Element)iR.next();
//				Integer userRol= new Integer( prop.getAttributeValue("ROL") );
//				int iddom= Integer.parseInt( prop.getAttributeValue("ID_DOMINIO") );
//				Element detRol=prop.hasChildren() ? (Element)prop.clone():null;
//				cc.addAcceso(  userRol.intValue(), prop.getAttributeValue("ACCESO"), iddom, detRol );
//				if( prop.getChild("REPORTS")!=null ){
//					Iterator iRpt= prop.getChild("REPORTS").getChildren("ITEM").iterator();
//					while( iRpt.hasNext() ){
//						Element rptItem=(Element)iRpt.next();
//						String idRpt= rptItem.getAttributeValue("OID");
//						cc.addReport( userRol.intValue(), idRpt );
//					}
//				}
//			}
//			m_md.addContext( cc );
//		}
//	}
//
//	public void buildSpecialization(Element root){
//		Iterator iS= root.getChildren("TYPE").iterator();
//		while( iS.hasNext() ){
//			Element eTO= (Element)iS.next();
//			int to= Integer.parseInt( eTO.getAttributeValue("ID_TO"));
//			String[] specs= eTO.getText().split(";");
//			for( int i=0; i< specs.length; i++){
//				int spec= Integer.parseInt( specs[i] );
//				m_md.addDirectSpecializedMap( to, spec );
//			}
//			m_md.addDirectSpecializedMap( to, to );
//		}
//	}
//
//	public void buildFilters(Element root){
//		Iterator iF= root.getChildren().iterator();
//		while( iF.hasNext() ){
//			Element eF= (Element)iF.next();
//			int id= Integer.parseInt( eF.getAttributeValue("ID"));
//			m_md.addFilter( eF, id );
//		}
//	}
//
//	public void buildDominios(Element root){
//		Iterator iD= root.getChildren().iterator();
//		while( iD.hasNext() ){
//			Element eD= (Element)iD.next();
//			int id= Integer.parseInt( eD.getAttributeValue("ID"));
//			m_md.addDominio( eD, id );
//		}
//	}
//
//	public void buildAtributos(Element root){
//		Iterator iA= root.getChildren().iterator();
//		while( iA.hasNext() ){
//			Element eA= (Element)iA.next();
//			int tapos= Integer.parseInt( eA.getAttributeValue("TA_POS"));
//			int tm= Integer.parseInt( eA.getAttributeValue("ID_TM"));
//			m_md.addTMmap( tapos, tm );
//		}
//	}
}
