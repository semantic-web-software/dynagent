package dynagent.gui.adapter.old;
/*package dynagent.gui.adapter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;



import dynagent.gui.Singleton;
import dynagent.gui.communicator;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.Category;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.meta.api.Value;
import dynagent.ruleengine.src.ruler.Fact;
import dynagent.ruleengine.src.ruler.Ruler;
import dynagent.ruleengine.src.sessions.Session;
import dynagent.ruleengine.src.xml.QueryXML;



import dynagent.ruleengine.Constants;
import dynagent.ruleengine.CreateIdProp;
import dynagent.ruleengine.CreateIdo;
import dynagent.ruleengine.CreateIdto;
<<<<<<< .mine
*//**
=======
import dynagent.server.application.action;
import dynagent.server.application.session;
import dynagent.server.communication.docServer;
import dynagent.server.exceptions.CommunicationException;
import dynagent.server.exceptions.RemoteSystemException;
import dynagent.server.exceptions.SystemException;
import dynagent.server.knowledge.UserAccess;
import dynagent.server.knowledge.access;
import dynagent.server.knowledge.instance.SelectQuery;
import dynagent.server.knowledge.instance.instance;
import dynagent.server.knowledge.instance.selectData;
/**
>>>>>>> .r1918
 * Esta clase implementa las interfaces IAdapter e IKnowledgeBaseInfo.<br>
 * Se va a encargar de adaptar el modelo de relaciones y roles a simplemente clases<br>
 * con ObjectProperty que apunten a otras clases, ya que es lo único que entinede el GUI.<br>
 * Hace de puente entre el Motor y el GUI.<br>
 * @author Dynagent - David
 *
 *//*
public class Adapter implements IAdapter, IKnowledgeBaseInfo{
	private HashMap<Integer,Boolean> UTaskAdapte=new HashMap<Integer,Boolean>();
	*//**
	 * Objeto IKnowledgeBaseInfo para poder usar las funciones del Motor.
	 *//*
	private IKnowledgeBaseInfo kb;
	*//**
	 * Mapa en el cual se va a almacenar la adaptación de las relaciones, roles y clases.
	 *//*
	private Map<Integer,DataAdapter> tbAdapter = new HashMap<Integer,DataAdapter>();
	*//**
	 * Mapa en el cual se va a almacenar los ido virtuales junto con un String indicando el nombre de la clase.
	 *//*
	private Map<Integer,String> tbAdapterNameClass;
	*//**
	 * Mapa con el cual tenemos una identificación de que clases están agrupadas dentro de un
	 * determinado individuo virtual.
	 *//*
	private Map<Integer,DataCase> tbGroupAdapter= new HashMap<Integer,DataCase>();
	*//**
	 * Mapa con la relación de ido e idto virtuales. 
	 *//*
	private Map<Integer,Integer> tbIdoIdto=new HashMap<Integer,Integer>();
	private Integer tgClassAdaptada;
	private Session session;
	public Iterator<Property> getAllPropertyIterator(Integer ido, Integer userRol, String user, Integer usertask){
		int typeCase;
		typeCase=typeConcreteCase(ido,null,null);
		//Impresión para las pruebas
	
		System.out.println("El tipo de caso es: "+typeCase);
		//fin codigo lineas de pruebas
		return null;
	}
	
	
	public Adapter(IKnowledgeBaseInfo kb,Session s){
		this.kb=kb;
		this.session=s;
		tbAdapterNameClass= new HashMap<Integer,String>();
		
	}
	
	public int typeConcreteCase(DataCase concreteCase, Integer userRol, String user, Integer usertask) throws NotFoundException{
					
		Integer type=0;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoPeer=concreteCase.getIdoPeer();
		Integer idoRel=concreteCase.getIdoRel();
		Integer cardMinClass= null;
		Integer cardMaxClass=null;
		Integer cardMinPeer= null;
		Integer cardMaxPeer=null;
		boolean hasPropertyRel=false;
		Iterator<Property> it=null;
		
		LinkedList<Property> propertyRel=new LinkedList<Property>();
		LinkedList<Property> propertyPlayRel = new LinkedList<Property>();
		
		
		it=kb.getAllPropertyIterator(idoRel,userRol,user,usertask,session);
		
		while (it.hasNext()){
			Property p=it.next();
			Category c = p.getCategory();
			if(p instanceof ObjectProperty && c.isPlay()){
				Integer idoplay=null;
				if(!((ObjectProperty)p).getValueList().isEmpty()){
					idoplay=((ObjectProperty)p).getValueList().getFirst().getValue();
				}else{
					idoplay=((ObjectProperty)p).getFilterList().getFirst().getValue();
				}
				if (idoplay.equals(idoClass)){
					cardMaxClass=p.getCardMax();
					cardMinClass=p.getCardMin();
					if(cardMaxClass==null)
						cardMaxClass=999;
					if(cardMinClass==null)
						cardMinClass=999;
					propertyPlayRel.add(p);
				}else if(idoplay.equals(idoPeer)){
					cardMaxPeer=p.getCardMax();
					cardMinPeer=p.getCardMin();
					if(cardMaxPeer==null)
						cardMaxPeer=999;
					if(cardMinPeer==null)
						cardMinPeer=999;
					propertyPlayRel.add(p);
				}else{
					propertyPlayRel.add(p);
				}
			}else{
				//if(p.getIdProp()!=Constants.IdPROP_RDN)
					propertyRel.add(p);
			}
		}
		
		it=propertyRel.iterator();		
		hasPropertyRel=it.hasNext();
		
		
		boolean searchRel=getSearchRel(userRol, user, usertask, concreteCase);		
		boolean searchPeer=getSearchPeer(userRol,user,usertask, concreteCase);
				
		if (hasPropertyRel){
			if(cardMaxClass==1 && cardMinClass==0){
				
				if(!searchRel){
					type=1;
					if (cardMaxPeer==1 && cardMinPeer==0){
						type=2;
					}
					return type;
				}else{
					type=4;
					return type;
				}
			}else if(cardMaxClass==1 && cardMinClass==1){
			
				if(!searchRel){
					type=2;
					
					if (cardMaxPeer==1 && cardMinPeer==1 && !searchPeer){
						type=3;
					}
					return type;
				}else{
					type=4;
					return type;
				}
			}else{
				type=4;
				if(cardMaxPeer==1 && cardMinPeer==1 && !searchRel){
					type=1;
					return type;
				}
				return type;
			}
		}else{
			
			if(cardMaxClass==1 && cardMinClass==0){
				if (cardMaxPeer==1 && cardMinPeer==1){
					type=1;
					return type;
				}else{
					type=0;
					return type;
				}
			}else if(cardMaxClass==1 && cardMinClass==1){
				type=2;
				
				if(!searchPeer){
					type=3;
				}
				return type;
				
			}else{
				if (cardMaxPeer==1 && cardMinPeer==1){
					type=1;
					return type;
				}else{
					type=4;
					return type;
				}
			}
		}

			
	
	}
	private boolean getSearchPeer(Integer userRol, String user, Integer usertask,DataCase concreteCase) throws NotFoundException {
		boolean result=false;
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator<Property> it=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
		while (it.hasNext()){
			Property p = it.next();
			Category c = p.getCategory();
			LinkedList<ObjectValue> li= null;
			if(p instanceof ObjectProperty){
				if(Constants.isIDIndividual(idoPeer)){
					li=((ObjectProperty)p).getValueList();
				}else if(Constants.isIDFilter(idoPeer) || Constants.isIDPrototype(idoPeer)){
					li=((ObjectProperty)p).getFilterList();
				}else{
					System.err.println("WARNING: Opcion no contemplada - Method: getSearchPeer");
				}
				if(c.isPlay() && li.getFirst().getValue().equals(idoPeer)){
					access accessPeer= p.getTypeAccess();
					if (accessPeer.getSetAccess()){
						result=true;
					}
				}
			}
		}
		return result;
	}
	private boolean getSearchRel(Integer userRol, String user, Integer usertask, DataCase concreteCase) throws NotFoundException {
		boolean result=false;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Iterator<Property> it=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
		Integer idtoRel = kb.getClassOfObject(idoRel);
		while(it.hasNext()){
			Property p = it.next();
			Category c=p.getCategory();
			LinkedList<ObjectValue> li= null;
			LinkedList<Integer> lr= null;
			if(p instanceof ObjectProperty){
				lr=((ObjectProperty)p).getRangoList();
				if (lr.contains(idtoRel)){
					if(Constants.isIDIndividual(idoRel) || Constants.isIDPrototype(idoRel)){
						li=((ObjectProperty)p).getValueList();
					}else if(Constants.isIDFilter(idoRel)){
						li=((ObjectProperty)p).getFilterList();
					}else{
						System.err.println("WARNING: Opcion no contemplada - Method: getSearchRel");
					}
					if(c.isPlayIn() && li.contains(idoRel)){
						access accessRel= p.getTypeAccess();
						if (accessRel.getSetAccess()){
							result=true;
						}
					}
				}
			}
						
		}
		return result;
	}


	
	

	public Iterator<Integer> getClassIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClassName(Integer idto) throws NotFoundException {
		String result="";
		if (tbAdapterNameClass.containsKey(idto)){
			result=tbAdapterNameClass.get(idto);
			return result;
		}
		result= kb.getClassName(idto);
		return result;
	}

	public String getClassNameOfObject(Integer ido) throws NotFoundException {
		String result="";
		if (tbAdapter.containsKey(ido)){
			Integer idto=tbIdoIdto.get(ido);
			result=tbAdapterNameClass.get(idto);
			return result;
		}
		result= kb.getClassNameOfObject(ido);
		return result;
	}

	public Integer getClassOfObject(Integer ido) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getDatatype(int idProp) {
		// TODO Auto-generated method stub
		return null;
	}

	

	public Integer getLevel(Integer id) {
		Integer level=null;
		DataAdapter da=tbAdapter.get(id);
		Property p =da.getProperty(2);
		Integer ido=da.getOriginalIdoClass(p);
		level=docDataModel.getLevel(ido);
		return level;
	}


	public void init(Integer usertask,Integer userRol,String user) throws NotFoundException{
		System.err.println("Entra pide la tgClass");
		System.err.println("******USERTASK:"+getClassNameOfObject(usertask));
		Map<Integer,DataAdapter> tbAdapterCase = new HashMap<Integer,DataAdapter>();
		Integer tgClass=null;
		Iterator<Property> it=null;
		LinkedList<ObjectValue> filterList= new LinkedList<ObjectValue>();
		Iterator<ObjectValue> fit=null;
		Integer utaskclass=kb.getClassOfObject(usertask);
		it =kb.getPropertyIterator(usertask, Constants.IdPROP_TARGETCLASS,userRol,user,utaskclass,session);
		Property p =it.next();
		if(!((ObjectProperty)p).getFilterList().isEmpty()){
			filterList = ((ObjectProperty)p).getFilterList();
			fit=filterList.iterator();
			tgClass=fit.next().getValue();
		}else{
			filterList = ((ObjectProperty)p).getValueList();
			fit=filterList.iterator();
			tgClass=fit.next().getValue();
		}
		
		
		
		Iterator<Property>itp =kb.getPropertyIterator(usertask, Constants.IdPROP_TARGETREL,userRol,user,usertask,session);
		while(itp.hasNext()){
			Property pa =itp.next();
			if(pa==null){
				System.err.println("Property nula");
			}
			//System.out.println("PROPERTY K PETA");
			System.out.println(pa.toString());
			if(!((ObjectProperty)pa).getFilterList().isEmpty()){
				filterList = ((ObjectProperty)pa).getFilterList();
				fit=filterList.iterator();
			}else{
				filterList = ((ObjectProperty)pa).getValueList();
				fit=filterList.iterator();
			}
			Integer tgRel=fit.next().getValue();
			System.out.println("TARGET REL: "+tgRel);
			imprimetgClass(tgRel,userRol, user, usertask);
		}
		LinkedList<DataCase> listCase = extractCases(tgClass,usertask,userRol,user);
		Iterator<DataCase> itCase=listCase.listIterator();
		while(itCase.hasNext()){
			DataCase concreteCase=itCase.next();
			int type=typeConcreteCase(concreteCase, userRol, user, usertask);
			tbAdapterCase.putAll(adapterCase(type,concreteCase,tbAdapterCase,userRol, user, usertask));
		}
		groupCases(tbAdapterCase,tgClass,usertask);
		it =kb.getPropertyIterator(usertask, Constants.IdPROP_TARGETCLASS,userRol,user,usertask,session);
		 p =it.next();
		if(!((ObjectProperty)p).getFilterList().isEmpty()){
			filterList = ((ObjectProperty)p).getFilterList();
			fit=filterList.iterator();
			tgClass=fit.next().getValue();
		}else{
			filterList = ((ObjectProperty)p).getValueList();
			fit=filterList.iterator();
			tgClass=fit.next().getValue();
		}
		System.out.println("TARGET CLASS: "+tgClass);
		imprimetgClass(tgClass,userRol, user, usertask);
		imprime();
		Set<Integer> k=tbAdapter.keySet();
		Iterator<Integer> itk= k.iterator();
		while (it.hasNext()){
			imprimeinfoclass(itk.next());
		}
		if(listCase.size()>0){
			this.tgClassAdaptada=viewMoreImportantIdoVirtual(tgClass);
			UTaskAdapte.put(usertask, true);
		}
	}
	private void imprimeinfoclass(Integer key) {
		System.out.println("**********INFO CLASS VIRTUAL: "+key+" *****************");
		DataAdapter da=tbAdapter.get(key);
		System.out.println(da.toString());
	}
	private void imprimetgClass(Integer tgClass, Integer userRol, String user, Integer usertask) throws NotFoundException {
		Iterator<Property> it=kb.getAllPropertyIterator(tgClass, userRol, user, usertask,session);
		System.out.println("*****INFO IDO: "+ tgClass+" *******");
		while (it.hasNext()){
			Property p=it.next();
			System.out.println(p.toString());
		}
		
	}
	*//**
	 * Método el cual se va a encargar de agrupar los distintos casos existentes en la UTask.<br>
	 * Para ello lo que va a realizar es lo siguiente. Primero itera sobre los individuos virtuales<br>
	 * mediante la tabla {@link #tbGroupAdapter}, de forma que comprueba si dentro de ese<br>
	 * individuo virtual se encentra agrupada la tgClass de la Utask.<br>
	 * Si es así comprueba si este individuo es más importante que otro en el que también<br>
	 * se encuentre agrupada la tgClass({@link #viewMoreImportantIdoVirtual(Integer)}), se queda con el<br>
	 * ido virtual más importante y crea O.P.Vs para cada agrupación.<br><br>
	 * @param tbAdapterCase Mapa con la información de la agrupación.
	 * @param tgClass Identificador de la clase a la que apunta la UTask.
	 * @param usertask 
	 * @throws NotFoundException 
	 *//*
	private void groupCases(Map<Integer, DataAdapter> tbAdapterCase, Integer tgClass, Integer usertask) throws NotFoundException {
		Integer idoVirtualImportant=viewMoreImportantIdoVirtual(tgClass);
		if(idoVirtualImportant!=null){
			imprime2(tbAdapterCase);
			System.out.println("IDOMASIMPORTANTE: "+idoVirtualImportant);
			Set<Integer> keys=tbGroupAdapter.keySet();
			Iterator<Integer> it=keys.iterator();	
			while(it.hasNext()){
				Integer key=it.next();
				DataCase d= tbGroupAdapter.get(key);
				if(d.getIdoClass()!=null && d.getIdoClass().equals(tgClass) ){
					if (!key.equals(idoVirtualImportant)){
						if(d.getIdoPeer()!=null && tbGroupAdapter.get(idoVirtualImportant).getIdoPeer()!=null){
							tbAdapterCase=deletePropertiesClassRelation(key,tbAdapterCase, d.getIdoClass(),d.getIdoRel());
						}else{
							tbAdapterCase.remove(key);
						}
					}
				}
			}
			DataAdapter da=tbAdapterCase.get(idoVirtualImportant);
		
			keys=tbAdapterCase.keySet();
			it=keys.iterator();
			
			while(it.hasNext()){
				Integer key=it.next();
				if(!key.equals(idoVirtualImportant)){
					DataAdapter datemp=tbAdapterCase.get(key);
					if(!existOPVRel(da,key)){
						da=createOPVRel(da.getIdoTemp(), da.getIdtoTemp(), da, key, null, null, null, true);
					}
					tbAdapter.put(key, datemp);
				}
			}
			tbAdapter.put(idoVirtualImportant, da);
		}else{
			tbAdapter.putAll(tbAdapterCase);
		}
		ObjectValue ov=new ObjectValue();
		ov.setValue(idoVirtualImportant);
		ov.setValueCls(tbIdoIdto.get(idoVirtualImportant));
		LinkedList<ObjectValue> filterVirtual=new LinkedList<ObjectValue>();
		filterVirtual.add(ov);
		Property pr=((DocDataModel)kb).getProperty(kb.getClassOfObject(usertask), usertask, 8, null, null, null, null, null, usertask);
		((ObjectProperty)pr).setFilterList(filterVirtual);
	}
	*//**
	 * Método el cual consulta si existen ObjectPropertyVirtuales de relación que va desde <br>
	 * la agrupación que contiene a la tgClass, hasta un determinado individuo virtual.<br><br>
	 * @param da DataAdapter con los datos del individuo virtual que contiene la tgClass.
	 * @param key Identificador de individuo virtual.
	 * @return Boolean que indica si existe o no dicha O.P.V de relación.
	 *//*
	private boolean existOPVRel(DataAdapter da, Integer key) {
		boolean result=false;
		if(tbIdoIdto.containsKey(key)){
			Iterator<Property> itp=da.getPropertyIterator();
			while (itp.hasNext()){
				Property p=itp.next();
				if(p instanceof ObjectPropertyVirtual && p.getName().equals("VIRTUAL")){
					ObjectValue ov=null;
					if (!((ObjectPropertyVirtual)p).getValueList().isEmpty()){
						ov=((ObjectPropertyVirtual)p).getValueList().getFirst();
					}else{
						ov=((ObjectPropertyVirtual)p).getFilterList().getFirst();
					}
					if (ov.getValue().equals(key)){
						result=true;
						return result;
					}
				}
			}
		}
		return result;
	}
	*//**
	 * Método en el cual se van a eliminar las properties de la clase y la relación en una agrupación.<br>
	 * Esto es debido a que se ha seleccionado a la hora de hacer el método {@link #groupCases(Map, Integer)}<br>
	 * otra agrupación como principal y debemos eliminar entonces el contenido de la tgClass<br>
	 * de las demás agrupaciones para no repetir datos.<br><br>
	 * @param key Identificador del individuo virtual.
	 * @param tbAdapterCase Tabla con la adaptación actual.
	 * @param idoClass Identificador del tgClass.
	 * @param idoRel Identificador de la relación.
	 * @return Tabla de la adaptación actualizada.
	 *//*
	private Map<Integer, DataAdapter> deletePropertiesClassRelation(Integer key, Map<Integer, DataAdapter> tbAdapterCase, Integer idoClass, Integer idoRel) {
		DataAdapter d= tbAdapterCase.get(key);
		Iterator<Property> itp=d.getPropertyIterator();
		while(itp.hasNext()){
			Property p =itp.next();
			Integer tmpClass=d.getOriginalIdoClass(p);
			if(tmpClass.equals(idoClass) || tmpClass.equals(idoRel)){
				d.deleteProperty(p);
				tbAdapterCase.put(key, d);
				itp=d.getPropertyIterator();
			}
			
		}
		return tbAdapterCase;
	}
	
	*//**
	 * Método que se encarga de ver cual es el individuo virtual más importante que contiene <br>
	 * el tgClass.<br> 
	 * Para ello se itera sobre todos mirando si contiene en el tgClass dentro del individuo <br>
	 * virtual en concreto, posteriormente se almacena el más importante y se devuelve.<br><br>
	 * El sistema de elección del más importante es el siguiente: si contiene en la agrupación<br>
	 * a la clase, el peer y la relación, es más importante que sólo contiene la clase y la relacion<br>
	 * o sólo la clase.
	 * @param tgClass Identificador de la clase a la que apunta la UTask
	 * @return Identificador del idoVirtual.
	 *//*
	private Integer viewMoreImportantIdoVirtual(Integer tgClass) {
		Integer result=null;
		DataCase moreImportant=new DataCase(null,null,null);
		Set<Integer> keys=tbGroupAdapter.keySet();
		Iterator<Integer> it=keys.iterator();
		while(it.hasNext()){
			Integer key=it.next();
			System.out.print("********IDOV= "+key+" ");
			DataCase d= tbGroupAdapter.get(key);
			System.out.println(d.toString()+"*********");
			if(d.getIdoClass()!=null && d.getIdoClass().equals(tgClass)){
				if (moreImportant.getIdoClass()==null){
					moreImportant=d;
					result=key;
				}else{
					boolean hasIdoRel=false;
					boolean hasIdoPeer=false;
					hasIdoRel=moreImportant.getIdoRel()!=null;
					hasIdoPeer=moreImportant.getIdoPeer()!=null;
					if (d.getIdoRel()!=null && !hasIdoRel){
						moreImportant=d;
						result=key;
					}
					if(d.getIdoPeer()!=null && !hasIdoPeer){
						moreImportant=d;
						result=key;
					}
				}
			}
		}
		return result;
	}
	*//**
	 * Este método diferencia el tipo de caso mediante el parámetro type, y llama a la <br>
	 * función concreta:
	 * <ul><li>{@link #adapterCaseA(DataCase, Object)}</li>
	 * 	   <li>{@link #adapterCaseB(DataCase, Object)}</li>
	 * 	   <li>{@link #adapterCaseC(DataCase, Object)}</li>
	 * 	   <li>{@link #adapterCaseD(DataCase, Object)}</li>
	 * 	   <li>{@link #adapterCaseE(DataCase, Object)}</li></ul>
	 * <br><br>
	 * @param type Entero que identifica uno de los casos concretos de la casuistica (ver Casuistica del adaptador.doc)
	 * @param concreteCase Instancia de un caso concreto (relación y dos jugadores)
	 * @param tbAdapterCase Tabla con la información de los casos concretos ya adaptados.
	 * @throws NotFoundException 
	 *//*
	public Map<Integer,DataAdapter> adapterCase(int type, DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		
		switch(type){
		case(0):System.out.println("**********CASO A**********");return adapterCaseA(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(1):System.out.println("**********CASO B**********");return adapterCaseB(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(2):System.out.println("**********CASO C**********");return adapterCaseC(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(3):System.out.println("**********CASO D**********");return adapterCaseD(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(4):System.out.println("**********CASO E**********");return adapterCaseE(concreteCase,tbAdapterCase,userRol, user, usertask);
		default:return null;
		}
		
	}
	*//**
	 * Este método se encarga de adaptar el tipo de caso E.<br><br>
	 * Para ello vamos primero a pasar a comprobar si la relación es un filtro<br>
	 * o un prototipo/individuo, esto debemos comprobarlo puesto que nos va a <br>
	 * delimitar la opcionalidad de ciertas properties.<br><br>
	 * Si es un filtro, por lógica el peer también debe ser un filtro. Empezamos <br>
	 * tratando la Clase principal, para ello primero iteramos sobre la clase,<br>
	 * con lo que llamamos a la función {@link IKnowledgeBaseInfo#getAllPropertyIterator(Integer, Integer, String, Integer)}<br>
	 * y por cada una de las properties nos creamos una property virtual, de forma <br>
	 * que vamos a cambiar sus campos ido, idto e idProp por los virtuales, almacenandolos <br>
	 * en un DataAdapter con el ido e idto de la agrupación de la parte de la clase principal <br><br>
	 * Cuando nos encontremos con una propertie de rol, debemos de cambiar el atributo required <br>
	 * de la property virtual asociada a false, de forma que esta property pasa a ser opcional<br><br>
	 * Posteriormente creamos una O.P.V de relación, es decir una {@link ObjectPropertyVirtual} <br>
	 * la cual va a apuntar desde la agrupación principal a la de la relación, esta O.P.V lleva como característica <br>
	 * que el nombre es "VIRTUAL".<br>
	 * Posteriormente realizamos el mismo procedimiento pero en este caso iterando sobre la relación,<br>
	 * creando un nuevo ido e idto, ya que en este caso se va a separar la relación tanto de la clase<br>
	 * principal, como del peer. Creando también en este caso una O.P.V hacia la agrupación del Peer.<br>
	 * (Ver Casuistica del adaptador.doc caso Tipo E)<br>
	 * 
	 * <br>
	 * Posteriormente nos creamos un nuevo DataAdapter con un nuevo ido e idto virtual para agrupar<br>
	 * la parte del peer, de esta manera iteramos por el peer agrupando las properties,<br>
	 * ya transformadas a properties virtuales, en el DataAdapter creado anteriormente<br>
	 * <br>
	 * Si por lo contrario es un filtro o un individuo, se realizan los mismos pasos que antes<br>
	 * lo que cambia es que en este caso las properties son obligatorias puesto que ya esta fijada <br>
	 * una relación existente. <br><br>
	 * 
	 * @param concreteCase DataCase con el caso concreto de tipo E
	 * @param tbAdapterCase Mapa con el caso ya adaptado
	 * @param userRol Entero con indicando el rol que juega el usuario logeado
	 * @param user String que indica el usuario logeado
	 * @param usertask Integer que representa la UTask en la que estamos situado
	 * @return Mapa que se le pasa como parámetros con el caso adaptado.
	 * @throws NotFoundException 
	 *//*
	private Map<Integer,DataAdapter> adapterCaseE(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		//Caso en que la relacion es un filtro
		
		if(Constants.isIDFilter(idoRel)){
			//Tratamos la clase principal
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				//System.out.println(p.toString());
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						
						String nameRol=kb.getClassNameOfObject(((DataPropertyVirtual)p).getRol());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,false);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1)) && ((ObjectProperty)p).getEnumList().size()<0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
			}
			idGroup=1;
			
			//Tratamos la relacion.
			Integer idoVirtualRel=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualRel=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualRel, idtoVirtualRel);
			da= createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualRel,null,null,null,false);
			String nameVirtual=getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtual,nameVirtual);
			
			DataCase d= new DataCase(idoClass,null,null);
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);
			da=new DataAdapter(idoVirtualRel,idtoVirtualRel);
			itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtualRel,idtoVirtualRel,da,null,null,null,true);
				}else{
					da=createDataPropertyVirtual(p,idoRel,idoVirtual,idtoVirtualRel,da,null,null,null,true);
				}
			}
			
			idGroup=1;
			//Tratamos la clase del Peer.
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtualRel,idtoVirtualRel,da,idoVirtualPeer,null,null,null,false);
			String nameVirtualRel=kb.getClassNameOfObject(idoRel); 
				
			tbAdapterNameClass.put(idtoVirtualRel, nameVirtualRel);
			d= new DataCase(null,idoRel,null);
			tbGroupAdapter.put(idoVirtualRel, d);
			tbAdapterCase.put(idoVirtualRel, da);
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(((DataPropertyVirtual)p).getRol());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,false);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1)) && ((ObjectProperty)p).getEnumList().size()<0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
			}
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtualPeer, nameVirtualPeer);
			
			d= new DataCase(null,null,idoPeer);
			tbGroupAdapter.put(idoVirtualPeer, d);
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		//Caso en el que la relación es un Prototipo o individuo (Deja de ser optativo la relacion, por tanto los
		//atributos de rol pasan a ser obligatorios)
		else{
			//Tratamos el caso de la clase principal
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() &&(p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1)) && ((ObjectProperty)p).getEnumList().size()<0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
			}
			idGroup=1;
			//Tratamos la relacion.
			Integer idoVirtualRel=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualRel=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualRel, idtoVirtualRel);
			da= createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualRel,null,null,null,false);
			String nameVirtual=getNameVirtualClass(idoClass,userRol,user,usertask);
			
			tbAdapterNameClass.put(idtoVirtual,nameVirtual);
			DataCase d= new DataCase(idoClass,null,null);
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);
			da=new DataAdapter(idoVirtualRel,idtoVirtualRel);
			itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtualRel,idtoVirtualRel,da,null,null,null,true);
				}else{
					da=createDataPropertyVirtual(p,idoRel,idoVirtualRel,idtoVirtualRel,da,null,null,null,true);
				}
			}
			idGroup=1;
			//Tratamos el Peer
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtualRel,idtoVirtualRel,da,idoVirtualPeer,null,null,null,true);
			String nameVirtualRel= kb.getClassNameOfObject(idoRel);
			
			tbAdapterNameClass.put(idtoVirtualRel, nameVirtualRel);
			d= new DataCase(null,idoRel,null);
			tbGroupAdapter.put(idoVirtualRel, d);
			tbAdapterCase.put(idoVirtualRel, da);
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() &&(p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1)) && ((ObjectProperty)p).getEnumList().size()<0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
			}
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			d= new DataCase(null,null,idoPeer);
			tbGroupAdapter.put(idoVirtualPeer, d);
			
			tbAdapterNameClass.put(idtoVirtualPeer, nameVirtualPeer);
			tbAdapterCase.put(idoVirtualPeer, da);
		}
		return tbAdapterCase;
	}
	*//**
	 * Este método se encarga de adaptar el tipo de caso D.<br><br>
	 * Este caso es el más simple, simplemente crearemos un único ido virtual<br>
	 * en el cual vamos a agrupar todo, Clase (con Rol), Relación y Peer (con Rol).<br>
	 * Para ello nos vamos a centrar unicamente en iterar las tres partes creando<br>
	 * properties virtuales a partir de las properties de cada parte, y las almacenamos<br>
	 * en el nuevo ido virtual.<br><br>
	 * 
	 * @param concreteCase DataCase con el caso concreto de tipo D
	 * @param tbAdapterCase Mapa con el caso ya adaptado
	 * @param userRol Entero con indicando el rol que juega el usuario logeado
	 * @param user String que indica el usuario logeado
	 * @param usertask Integer que representa la UTask en la que estamos situado
	 * @return Mapa que se le pasa como parámetros con el caso adaptado.
	 * @throws NotFoundException 
	 *//*
	private Map<Integer,DataAdapter> adapterCaseD(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer	idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		//Creamos un unico dataAdapter, puesto que vamos a agrupar todo en una única clase
		Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
		Integer idtoVirtual=CreateIdto.getInstance().newIdto();
		String nameVirtual= kb.getClassNameOfObject(idoRel);
		DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
		
		tbAdapterNameClass.put(idtoVirtual, nameVirtual);
		//Recorremos primero las properties de la clase, almacenandolas en el DataAdapter 
		Iterator<Property> itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
		while(itProperty.hasNext()){
			Property p= itProperty.next();
			if (p instanceof DataProperty){
				if (p.getRol()==null){
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
				}else{
					String nameRol=kb.getClassNameOfObject(p.getIdo());
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,true);
				}
			}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1)) && ((ObjectProperty)p).getEnumList().size()<=0){
				idGroup++;
				Integer idGOPS=idGroup;
				String nameOPS=p.getName();
				Integer value=null;
				if(!((ObjectProperty)p).getValueList().isEmpty()){
					value=((ObjectProperty)p).getValueList().getFirst().getValue();
				}else{
					value=((ObjectProperty)p).getFilterList().getFirst().getValue();
				}
				boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
				if(!hasPlayIn){
					Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
					while(itaux.hasNext()){
						Property prop=itaux.next();
						if (prop instanceof DataProperty){
							DataProperty dp=(DataProperty)prop;
							da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
						}else{
							ObjectProperty dp=(ObjectProperty)prop;
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
						}
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
			}else if(!p.getCategory().isPlayIn()){
				da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
			}
		}
		//Recorremos la relacion
		itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
		idGroup++;
		Integer idGRel=idGroup;
		String nameRel=kb.getClassNameOfObject(idoRel);
		while(itProperty.hasNext()){
			
			//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
			
			Property p = itProperty.next();
			if (p instanceof ObjectProperty){
				if(!((ObjectProperty)p).getCategory().isPlay())
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGRel,null,nameRel,true);
			}else{
				da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGRel,null,nameRel,true);
			}
		}
		//Recorremos el peer.
		idGroup++;
		Integer idGPeer=idGroup;
		idGroup++;
		Integer idGPRol=idGroup;
		String namePeer="";
		itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
		while(itProperty.hasNext()){
			Property p=itProperty.next();
			if (p instanceof DataProperty){
				if (p.getRol()==null){
					String nameRol=kb.getClassNameOfObject(p.getIdo());
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGPRol,null,nameRol,true);
				}else{
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGPeer,null,namePeer,true);
				}
			}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
				idGroup++;
				Integer idGOPS=idGroup;
				String nameOPS=p.getName();
				Integer value=null;
				if(!((ObjectProperty)p).getValueList().isEmpty()){
					value=((ObjectProperty)p).getValueList().getFirst().getValue();
				}else{
					value=((ObjectProperty)p).getFilterList().getFirst().getValue();
				}
				boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
				if(!hasPlayIn){
					Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
					while(itaux.hasNext()){
						Property prop=itaux.next();
						if (prop instanceof DataProperty){
							DataProperty dp=(DataProperty)prop;
							da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
						}else{
							ObjectProperty dp=(ObjectProperty)prop;
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
						}
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGPeer,null,namePeer,true);
				}
				
			}else if(!p.getCategory().isPlayIn()){
				da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGPeer,null,namePeer,true);
			}
			
		}
		DataCase d= new DataCase(idoClass,idoRel,idoPeer);
		tbGroupAdapter.put(idoVirtual, d);
		tbIdoIdto.put(idoVirtual, idtoVirtual);
		tbAdapterCase.put(idoVirtual, da);
		return tbAdapterCase;
	}
	
	*//**
	 * Este método se encarga de adaptar el tipo de caso C.<br><br>
	 * Para ello vamos primero a pasar a comprobar si la relación es un filtro<br>
	 * o un prototipo/individuo, esto debemos comprobarlo puesto que nos va a <br>
	 * delimitar la opcionalidad de ciertas properties.<br><br>
	 * Si es un filtro, por lógica el peer también debe ser un filtro. Empezamos <br>
	 * tratando la Clase principal, debemos agrupar las properties de la clase<br>
	 * junto con las de la relación, para ello primero iteramos sobre la clase,<br>
	 * con lo que llamamos a la función {@link IKnowledgeBaseInfo#getAllPropertyIterator(Integer, Integer, String, Integer)}<br>
	 * y po cada una de las properties nos creamos una property virtual, de forma <br>
	 * que vamos a cambiar sus campos ido, idto e idProp por los virtuales, almacenandolos <br>
	 * en un DataAdapter con el ido e idto de la agrupación de la parte de la clase principal <br>
	 * Posteriormente realizamos el mismo procedimiento pero en este caso iterando sobre la relación<br>
	 * (Ver Casuistica del adaptador.doc caso Tipo C)<br>
	 * Cuando nos encontremos con una propertie de rol, debemos de cambiar el atributo required <br>
	 * de la property virtual asociada a false, de forma que esta property pasa a ser opcional<br>
	 * Posteriormente creamos una O.P.V de relación, es decir una {@link ObjectPropertyVirtual} <br>
	 * la cual va a apuntar desde la agrupación principal a otra, esta O.P.V lleva como característica <br>
	 * que el nombre es "VIRTUAL".<br>
	 * <br>
	 * Posteriormente nos creamos un nuevo DataAdapter con un nuevo ido e idto virtual para agrupar<br>
	 * la parte del peer, de esta manera iteramos por el peer agrupando las properties,<br>
	 * ya transformadas a properties virtuales, en el DataAdapter creado anteriormente<br>
	 * <br>
	 * Si por lo contrario es un filtro o un individuo, se realizan los mismos pasos que antes<br>
	 * lo que cambia es que en este caso las properties son obligatorias puesto que ya esta fijada <br>
	 * una relación existente. <br><br>
	 * 
	 * @param concreteCase DataCase con el caso concreto de tipo C
	 * @param tbAdapterCase Mapa con el caso ya adaptado
	 * @param userRol Entero con indicando el rol que juega el usuario logeado
	 * @param user String que indica el usuario logeado
	 * @param usertask Integer que representa la UTask en la que estamos situado
	 * @return Mapa que se le pasa como parámetros con el caso adaptado.
	 * @throws NotFoundException 
	 *//*
	private Map<Integer,DataAdapter> adapterCaseC(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer	idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		
		//Miramos si la relación a la que apunta es un filtro, con lo que sabemos que
		//en ese caso la cardinalidad será 0..1 con lo cual es opcional las properties de rol,
		//podemos deducirlo puesto que sino apuntaria a un prototipo o induviduo
		//en este caso aún no se a creado la relación.
		
		if(Constants.isIDFilter(idoRel)){
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Empezaremos tratando las properties de la clase principal junto con la relacion.
				String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
				tbAdapterNameClass.put(idtoVirtual, nameVirtual);
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassName(((DataPropertyVirtual)p).getRol());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,false);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			//iteramos en las properties de relación, si tiene.
			itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
			idGroup++;
			while(itProperty.hasNext()){
				
				Integer idGRel=idGroup;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameRel=kb.getClassNameOfObject(idoRel);
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGRel,null,nameRel,true);
				}else{
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGRel,null,nameRel,true);
				}
			}
			//Tratamos el peer.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,null,true);
			DataCase d= new DataCase(idoClass,idoRel,null);
			
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			
			
		//Caso en el que la relación sea un individuo o prototipo en concreto.
			d= new DataCase(null,null,idoPeer);
			
			tbGroupAdapter.put(idoVirtualPeer, d);
			tbAdapterCase.put(idoVirtualPeer, da);
		}else{
			
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtual, nameVirtual);
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Tratamos primero la clase principal, agrupandolo cn las properties de rol, junto con la relacion
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,null,true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}

			//iteramos en las properties de relación, si tiene.
			itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				idGroup++;
				Integer idGRel=idGroup++;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameRel=kb.getClassNameOfObject(idoRel);
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGRel,null,nameRel,true);
				}else{
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGRel,null,nameRel,true);
				}
			}
			//Tratamos el peer.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,null,true);
			DataCase d= new DataCase(idoClass,idoRel,null);
			
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			d= new DataCase(idoClass,null,null);
			
			tbGroupAdapter.put(idoVirtualPeer, d);			
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		return tbAdapterCase;
	}
	
	*//**
	 * Este método se encarga de adaptar el tipo de caso B.<br><br>
	 * Para ello vamos primero a pasar a comprobar si la relación es un filtro<br>
	 * o un prototipo/individuo, esto debemos comprobarlo puesto que nos va a <br>
	 * delimitar la opcionalidad de ciertas properties.<br><br>
	 * Si es un filtro, por lógica el peer también debe ser un filtro. Empezamos <br>
	 * tratando la Clase principal con lo que llamamos a la función {@link IKnowledgeBaseInfo#getAllPropertyIterator(Integer, Integer, String, Integer)}<br>
	 * y po cada una de las properties nos creamos una property virtual, de forma <br>
	 * que vamos a cambiar sus campos ido, idto e idProp por los virtuales, almacenandolos <br>
	 * en un DataAdapter con el ido e idto de la agrupación de la parte de la clase principal <br>
	 * (Ver Casuistica del adaptador.doc caso Tipo B)<br>
	 * Cuando nos encontremos con una propertie de rol, debemos de cambiar el atributo required <br>
	 * de la property virtual asociada a false, de forma que esta property pasa a ser opcional<br>
	 * Posteriormente creamos una O.P.V de relación, es decir una {@link ObjectPropertyVirtual} <br>
	 * la cual va a apuntar desde la agrupación principal a otra, esta O.P.V lleva como característica <br>
	 * que el nombre es "VIRTUAL".<br>
	 * <br>
	 * Posteriormente nos creamos un nuevo DataAdapter con un nuevo ido e idto virtual para agrupar<br>
	 * la parte del peer junto con la relación, de esta manera iteramos tanto por el peer como por <br>
	 * la relacion agrupando las properties, ya transformadas a properties virtuales, en el <br>
	 * DataAdapter creado anteriormente.
	 * <br>
	 * Si por lo contrario es un filtro o un individuo, se realizan los mismos pasos que antes<br>
	 * lo que cambia es que en este caso las properties son obligatorias puesto que ya esta fijada <br>
	 * una relación existente. <br><br>
	 * 
	 * @param concreteCase DataCase con el caso concreto de tipo B
	 * @param tbAdapterCase Mapa con el caso ya adaptado
	 * @param userRol Entero con indicando el rol que juega el usuario logeado
	 * @param user String que indica el usuario logeado
	 * @param usertask Integer que representa la UTask en la que estamos situado
	 * @return Mapa que se le pasa como parámetros con el caso adaptado.
	 * @throws NotFoundException 
	 *//*
	private Map<Integer,DataAdapter> adapterCaseB(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer	idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		
		//Miramos si la relación a la que apunta es un filtro, con lo que sabemos que
		//en ese caso la cardinalidad será 0..1 con lo cual es opcional las properties de rol,
		//podemos deducirlo puesto que sino apuntaria a un prototipo o induviduo
		//en este caso aún no se a creado la relación.
		
		if(Constants.isIDFilter(idoRel)){
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Empezaremos tratando las properties de la clase principal.
				String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
				tbAdapterNameClass.put(idtoVirtual, nameVirtual);
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,false);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			//Tratamos la agrupación del peer con la relación.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,null,true);
			DataCase d= new DataCase(idoClass,null,null);
			
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,0,null,"",true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			
			//iteramos en las properties de relación, si tiene.
			itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				idGroup++;
				Integer idGRel=idGroup;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameRel=kb.getClassNameOfObject(idoRel);
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGRel,null,nameRel,true);
				}else{
					da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGRel,null,nameRel,true);
				}
			}
		//Caso en el que la relación sea un individuo o prototipo en concreto.
			d= new DataCase(null,idoRel,idoPeer);
			
			tbGroupAdapter.put(idoVirtualPeer, d);
			tbAdapterCase.put(idoVirtualPeer, da);
		}else{
			
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtual, nameVirtual);
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Tratamos primero la clase principal, agrupandolo cn las properties de rol
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,0,null,"",true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			//Tratamos la agrupación del peer con la relación.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,null,true);
			DataCase d= new DataCase(idoClass,null,null);
			
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idtoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,0,null,"",true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && (p.getCardMax()!=null && p.getCardMax().equals(1)) && (p.getCardMin()!=null && p.getCardMin().equals(1))&& ((ObjectProperty)p).getEnumList().size()<=0){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value=null;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,0,null,"",true);
					}
				}else if(!p.getCategory().isPlayIn()){
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,0,null,"",true);
				}
				
			}
			
			//iteramos en las properties de relación, si tiene.
			itProperty=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
			while(itProperty.hasNext()){
				idGroup++;
				Integer idGRel=idGroup++;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameRel=kb.getClassNameOfObject(idoRel);
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGRel,null,nameRel,true);
				}else{
					da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGRel,null,nameRel,true);
				}
			}
			d= new DataCase(null,idoRel,idoPeer);
			
			tbGroupAdapter.put(idoVirtualPeer, d);
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		return tbAdapterCase;
	}
	
	*//**
	 * Método que dado una clase va a devolver un nombre adecuado para la clase virtual<br>
	 * donde esta se alojará, normalmente suele ser el nombre del rol que esta jugando, <br>
	 * y que se agrupa además con esta clase.<br><br>
	 * Para ello nos basamos simplemente en mirara las properties playIn de la clase<br>
	 * y devolvemos el getRol de dicha property.<br><br>
	 * @param idoClass Identificador de la clase
	 * @param userRol Identificador que indica el rol que juega el user logeado
	 * @param user String que indica el user logeado
	 * @param usertask Identificador de la UTask en la que estamos
	 * @return String con el nombre de la clase virtual
	 * @throws NotFoundException 
	 *//*
	private String getNameVirtualClass(Integer idoClass, Integer userRol, String user, Integer usertask) throws NotFoundException {
		String result="";
		Iterator<Property> it= kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
		while (it.hasNext()){
			Property p= it.next();
			if (p instanceof ObjectProperty){
				Category c = p.getCategory();
				if (c.isPlayIn()){
					result=kb.getClassName(p.getRol());
					return result;
				}
			}
		}
		return result;
	}
	*//**
	 * Método el cual te indica si un individuo esta jugando alguna relacion.<br>
	 * Para ello lo único que se comprueba es si dicha clase tiene algun playIn.<br>
	 * <br>
	 * 
	 * @param value Identificador de la clase
	 * @param userRol Identificador que indica el rol que juega el user logeado
	 * @param user String que indica el user logeado
	 * @param usertask Identificador de la UTask en la que estamos
	 * @return Booleano indicando si juega o no en alguna relacion
	 * @throws NotFoundException 
	 *//*
	private boolean hasPlayIn(Integer value,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Iterator<Property> itaux=getPlayIn(value, userRol, user, usertask);
		boolean hasPlayIn=false;
		if(itaux.hasNext()){
			hasPlayIn=true;
		}
		return hasPlayIn;
	}
	*//**
	 * Este método se encarga de adaptar el tipo de caso A.<br><br>
	 * Para ello vamos primero a pasar a comprobar si la relación es un filtro<br>
	 * o un prototipo/individuo, esto debemos comprobarlo puesto que nos va a <br>
	 * delimitar la opcionalidad de ciertas properties.<br><br>
	 * Si es un filtro, por lógica el peer también debe ser un filtro. Empezamos <br>
	 * tratando la Clase principal con lo que llamamos a la función {@link IKnowledgeBaseInfo#getAllPropertyIterator(Integer, Integer, String, Integer)}<br>
	 * y po cada una de las properties nos creamos una property virtual, de forma <br>
	 * que vamos a cambiar sus campos ido, idto e idProp por los virtuales, almacenandolos <br>
	 * en un DataAdapter con el ido e idto de la agrupación de la parte de la clase principal <br>
	 * (Ver Casuistica del adaptador.doc caso Tipo A)<br>
	 * Cuando nos encontremos con una propertie de rol, debemos de cambiar el atributo required <br>
	 * de la property virtual asociada a false, de forma que esta property pasa a ser opcional<br>
	 * Posteriormente creamos una O.P.V de relación, es decir una {@link ObjectPropertyVirtual} <br>
	 * la cual va a apuntar desde la agrupación principal a otra, esta O.P.V lleva como característica <br>
	 * que el VALUE y VALUECLS son el mismo, es decir el del idoVirtualPeer, y el nombre es "VIRTUAL".<br>
	 * <br>
	 * Posteriormente nos creamos un nuevo DataAdapter con un nuevo ido e idto virtual para agrupar<br>
	 * la parte del peer, de esta manera iteramos por el peer agrupando las properties,<br>
	 * ya transformadas a properties virtuales, en el DataAdapter creado anteriormente<br>
	 * <br>
	 * Si por lo contrario es un filtro o un individuo, se realizan los mismos pasos que antes<br>
	 * lo que cambia es que en este caso las properties son obligatorias puesto que ya esta fijada <br>
	 * una relación existente. <br><br>
	 * 
	 * @param concreteCase DataCase con el caso concreto de tipo A
	 * @param tbAdapterCase Mapa con el caso ya adaptado
	 * @param userRol Entero con indicando el rol que juega el usuario logeado
	 * @param user String que indica el usuario logeado
	 * @param usertask Integer que representa la UTask en la que estamos situado
	 * @return Mapa que se le pasa como parámetros con el caso adaptado.
	 * @throws NotFoundException 
	 *//*
	private Map<Integer,DataAdapter> adapterCaseA(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		//Caso en que la relacion es un filtro
		
		if(Constants.isIDFilter(idoRel)){
			//Tratamos la clase principal
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,false);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,null,true);
				}
			}
			idGroup=1;
			//Tratamos la clase del Peer.
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,null,false);
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtual, nameVirtual);
			DataCase d= new DataCase(idoClass,null,null);
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,false);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
				}
			}
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			d= new DataCase(null,null,idoPeer);
			tbGroupAdapter.put(idoVirtualPeer, d);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		//Caso en el que la relación es un Prototipo o individuo (Deja de ser optativo la relacion, por tanto los
		//atributos de rol pasan a ser obligatorios)
		else{
			//Tratamos el caso de la clase principal
			itProperty=kb.getAllPropertyIterator(idoClass, userRol, user, usertask,session);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,null,true);
				}
			}
			idGroup=1;
			//Tratamos el Peer
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			tbIdoIdto.put(idoVirtual, idtoVirtual);
			tbIdoIdto.put(idoVirtualPeer, idtoVirtualPeer);
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,null,true);
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtual, nameVirtual);
			DataCase d= new DataCase(idoClass,null,null);
			tbGroupAdapter.put(idoVirtual, d);
			tbAdapterCase.put(idoVirtual, da);
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			itProperty=kb.getAllPropertyIterator(idoPeer, userRol, user, usertask,session);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}else{
						String nameRol=kb.getClassNameOfObject(p.getIdo());
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,null,nameRol,true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					idGroup++;
					Integer idGOPS=idGroup;
					String nameOPS=p.getName();
					Integer value;
					if(!((ObjectProperty)p).getValueList().isEmpty()){
						value=((ObjectProperty)p).getValueList().getFirst().getValue();
					}else{
						value=((ObjectProperty)p).getFilterList().getFirst().getValue();
					}
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
						while(itaux.hasNext()){
							Property prop=itaux.next();
							if (prop instanceof DataProperty){
								DataProperty dp=(DataProperty)prop;
								da=createDataPropertyVirtual(dp,value,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}else{
								ObjectProperty dp=(ObjectProperty)prop;
								da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,null,nameOPS,true);
							}
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,null,true);
				}
			}
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			d= new DataCase(null,null,idoPeer);
			tbGroupAdapter.put(idoVirtualPeer, d);
			tbAdapterCase.put(idoVirtualPeer, da);
		}
		return tbAdapterCase;
	}
	*//**
	 * Metodo auxiliar para similar las properties de playIn mientras estas no esten <br>
	 * implementadas aun en el modelo
	 * @param tgClass
	 * @param userRol
	 * @param user
	 * @param usertask
	 * @return
	 * @throws NotFoundException 
	 *//*
	private Iterator<Property> getPlayIn(Integer tgClass, Integer userRol, String user, Integer usertask) throws NotFoundException {
		Iterator<Property> itp=kb.getAllPropertyIterator(tgClass, userRol, user, usertask);
		LinkedList<Property> lplay= new LinkedList<Property>();
		System.out.println("PLAYIN:");
		while (itp.hasNext()){
			Property p=itp.next();
			if(p.getCategory().isPlayIn()){
				
				System.out.println(p.toString());
				lplay.add(p);
			}
		}
		return lplay.iterator();
		
		LinkedList<Property> itplayin=new LinkedList<Property>();
		HashSet<Integer> relations=((DocDataModel)kb).getIdoRelationsInWhichPlay(tgClass);
		Iterator<Integer> itrel=relations.iterator();
		System.out.println("PLAYIN:");
		while(itrel.hasNext()){
			Integer idoRel=itrel.next();
			Iterator<Property> itplay= kb.getAllPropertyIterator(idoRel,userRol,user,usertask,session);
			while(itplay.hasNext()){
				Property pr =itplay.next();
				if(pr instanceof ObjectProperty){
					ObjectProperty p = (ObjectProperty)pr;
					Category c = p.getCategory();
					Integer value=null;
					if(!p.getValueList().isEmpty()){
						value=p.getValueList().getFirst().getValue();
					}else{
						value=p.getFilterList().getFirst().getValue();
					}
					if(c.isPlay() && value.equals(tgClass)){
						ObjectProperty pin=p.clone();
						pin.setIdo(tgClass);
						pin.setName(p.getName()+"INV");
						pin.setValueCls(kb.getClassOfObject(tgClass));
						LinkedList<ObjectValue> vl= new LinkedList<ObjectValue>();
						ObjectValue ov= new ObjectValue();
						ov.setValue(idoRel);
						ov.setValueCls(kb.getClassOfObject(idoRel));
						vl.add(ov);
						pin.setFilterList(vl);
						pin.setCardMax(1);
						pin.setCardMin(1);
						itplayin.add(pin);
						System.out.println(pin.toString());
					}
					
				}
			}
			
		}
		return itplayin.listIterator();
	}
	
	*//**
	 * Este método se va a encargar de crear la O.P.V de relación, es decir, la O.P.V que <br>
	 * nos "elimina" la relación, de forma que apuntara desde un conjunto virtual a otro virtual.<br><br>
	 * Hay que tener en cuenta que esta O.P.V llevará tanto en el ValueCLS como en Value, <br>
	 * el valor de la idoVirtual a la que apunta, y el nombre de esta property será el de <br>
	 * "VIRTUAL" para poder diferenciarlas claramente.<br><br>
	 * 
	 * @param idoVirtual Entero que indica el idoVirtual del cual sale la O.P.V.
	 * @param idtoVirtual Entero que indica el idtoVirtual del cual sale la O.P.V.
	 * @param da DataAdapter con todas las properties adaptadas del idoVirtual.
	 * @param idoVirtualPeer Entero que indica el idoVirtual del Peer.
	 * @param idGroup Entero que indica el grupo para agrupar las properties.
	 * @param idGroupFather Entero que indica el grupo del padre, para agrupaciones dentro de otras.
	 * @param nameGroup Nombre representativo del grupo.
	 * @param required Boolean que indica si es requerido o no.
	 * @return DataAdapter que se le pasa como parámetro añadiendole la O.P.V
	 *//*
	private DataAdapter createOPVRel(Integer idoVirtual, Integer idtoVirtual, DataAdapter da, Integer idoVirtualPeer,Integer idGroup, Integer idGroupFather,String nameGroup, boolean required) {
		Integer idPropVirtual=CreateIdProp.getInstance().newIdProp();
		ObjectPropertyVirtual dpv=new ObjectPropertyVirtual();
		Integer idtoVirtualPeer= tbIdoIdto.get(idoVirtualPeer);
		if(idGroup!=null)
			dpv.setIdGroup(idGroup);
		if(nameGroup!=null)
			dpv.setNameGroup(nameGroup);
		if(required==false)
			dpv.setRequired(required);	
		if(idGroupFather!=null)
			dpv.setIdGroupFather(idGroupFather);
		LinkedList<ObjectValue> vlist= new LinkedList<ObjectValue>();
		ObjectValue ov= new ObjectValue();
		ov.setValue(idoVirtualPeer);
		ov.setValueCls(idtoVirtualPeer);
		vlist.add(ov);
		LinkedList<Integer> rlist=new LinkedList<Integer>();
		rlist.add(idtoVirtualPeer);
		//En las OPV guardams a donde apunta en filterList,
		//debido a que fran en el GUI mira hay para crear los
		//formularios
		dpv.setFilterList(vlist);
		dpv.setName("VIRTUAL");
		dpv.setIdto(idtoVirtual);
		dpv.setIdo(idoVirtual);
		dpv.setIdProp(idPropVirtual);
		dpv.setTypeAccess(new access("VIEW"));
		dpv.setRangoList(rlist);
		Category cat= new Category();
		cat.setObjectProperty();
		dpv.setCategory(cat);
		da.setPropertyAdapter(dpv, idPropVirtual, idoVirtual,idtoVirtual);
		return da;
		
	}
	
	*//**
	 * Este método añade una DataProperty a un conjunto de adaptación.<br><br>
	 * Para ello lo que se hace es crear un DataPropertyVirtual a partir de la property <br>
	 * pasada como parámetro, de forma que a esta le vamos a dar un valor de idProp nuevo <br>
	 * y le vamos a cambiar su ido por el del idoVirtual.<br>
	 * Finalmente lo almacenamos en el DataAdapter.<br><br>
	 * @param p Property que hay que adaptar.
	 * @param idoVirtual Entero identificando en que idoVirtual debemos introducir la Property.
	 * @param idtoVirtual Entero identificando a que idtoVirtula pertenece el individuo virtual.
	 * @param da DataAdapter con las properties adaptadas hasta el momento.
	 * @param idGroup Entero que indica el grupo para agrupar las properties.
	 * @param idGroupFather Entero que indica el grupo padre del grupo de la property.
	 * @param nameGroup Nombre representativo del grupo.
	 * @param required Boolean qu indica si es requerido o no.
	 * @return DataAdapter que se le pasa como parámetro añadiendole la Property en concreto.
	 *//*
	private DataAdapter createDataPropertyVirtual(Property p, Integer idoClass, Integer idoVirtual,Integer idtoVirtual, DataAdapter da, Integer idGroup, Integer idGroupFather, String nameGroup, boolean required) {
		Integer idPropVirtual=CreateIdProp.getInstance().newIdProp();
		Integer idoOriginal=p.getIdo();
		Integer idPropOriginal=p.getIdProp();
		Integer idtoOriginal=p.getIdto();
		p.setIdo(idoVirtual);
		if(idoClass!=null && idoOriginal.equals(idoClass) && idPropOriginal==2){
		}else{
			p.setIdProp(idPropVirtual);
		}
		p.setIdto(idtoVirtual);
		DataPropertyVirtual dpv=new DataPropertyVirtual((DataProperty)p);
		if(idGroup!=null)
			dpv.setIdGroup(idGroup);
		if(idGroupFather!=null)
			dpv.setIdGroupFather(idGroupFather);
		if(nameGroup!=null)
			dpv.setNameGroup(nameGroup);
		if(required==false)
			dpv.setRequired(required);
		da.setPropertyAdapter(dpv, idPropOriginal, idoOriginal,idtoOriginal);
		return da;
	}
	*//**
	 * Este método añade un ObjectProperty a un conjunto de adaptación.<br><br>
	 * Para ello lo que se hace es crear un ObjectPropertyVirtual a partir de la property <br>
	 * pasada como parámetro, de forma que a esta le vamos a dar un valor de idProp nuevo <br>
	 * y le vamos a cambiar su ido por el del idoVirtual.<br>
	 * Finalmente lo almacenamos en el DataAdapter.<br><br>
	 * @param p Property que hay que adaptar.
	 * @param idoVirtual Entero identificando en que idoVirtual debemos introducir la Property.
	 * @param idtoVirtual Entero identificando a que idtoVirtula pertenece el individuo virtual.
	 * @param da DataAdapter con las properties adaptadas hasta el momento.
	 * @param idGroup Entero que indica el grupo para agrupar las properties.
	 * @param idGroupFather Entero que indica el grupo padre del grupo de la property.
	 * @param nameGroup Nombre representativo del grupo.
	 * @param required Boolean qu indica si es requerido o no.
	 * @return DataAdapter que se le pasa como parámetro añadiendole la Property en concreto.
	 *//*
	private DataAdapter createObjectPropertyVirtual(Property p, Integer idoVirtual,Integer idtoVirtual, DataAdapter da, Integer idGroup, Integer idGroupFather,String nameGroup,boolean required) {
		Integer idPropVirtual=CreateIdProp.getInstance().newIdProp();
		Integer idoOriginal=p.getIdo();
		Integer idPropOriginal=p.getIdProp();
		Integer idtoOriginal=p.getIdto();
		p.setIdo(idoVirtual);
		p.setIdProp(idPropVirtual);
		p.setIdto(idtoVirtual);
		ObjectPropertyVirtual dpv=new ObjectPropertyVirtual((ObjectProperty)p);
		if(idGroup!=null)
			dpv.setIdGroup(idGroup);
		if(nameGroup!=null)
			dpv.setNameGroup(nameGroup);
		if(required==false)
			dpv.setRequired(required);
		if(idGroupFather!=null)
			dpv.setIdGroupFather(idGroupFather);
		da.setPropertyAdapter(dpv, idPropOriginal, idoOriginal,idtoOriginal);
		return da;
	}
	*//**
	 * Este metodo extrae de una UTask los distintos casos concretos, teniendo en cuenta <br>
	 * que un caso concreto consiste de una relación con únicamente dos jugadores, con el fin<br>
	 * de agrupar dichos casos posteriormente.<br><br>
	 * 
	 * Para ello lo primero que haremos será iterar por todas relaciones en las que participa <br>
	 * la tgClass, con el fin de aislar casos por relación.<br>
	 * Posteriormente llamaremos a la función auxiliar {@link #extractCasesAux(Integer, Integer, Integer, Integer, String)} <br>
	 * la cual nos devolverá una lista de casos concretos fijado una relación concreta.<br><br>
	 *  
	 * @param tgClass Entero que indica la clase a la cual apunta la UTask.
	 * @param usertask Identificador de la UTask en la que estamos situados.
	 * @param userRol Entero que indica el rol que esta jugando el usuario logeado en ese momento.
	 * @param user Cadena que identifica al usuario logeado.
	 * @return LinkedList con un conjunto de casos concretos ({@link DataCase}).
	 * @throws NotFoundException 
	 *//*
	private LinkedList<DataCase> extractCases(Integer tgClass, Integer usertask, Integer userRol, String user) throws NotFoundException {
		LinkedList<DataCase> result=new LinkedList<DataCase>();
		Integer idoRel=null;
		//Iterator<Property> itplayin=docDataModel.getPropertyIterator(tgClass, Constants.IdPROP_PLAYIN, userRol, user, usertask);
		Iterator<Property> itplayin=getPlayIn(tgClass,userRol, user, usertask);
		
		while (itplayin.hasNext()){
			ObjectProperty playin=(ObjectProperty) itplayin.next();
			if(playin.getFilterList()!=null){
				LinkedList<ObjectValue> filplayin=playin.getFilterList();
				Iterator<ObjectValue> itfplayin= filplayin.listIterator();
				while(itfplayin.hasNext()){
					ObjectValue filter=itfplayin.next();
					idoRel=filter.getValue();
					result.addAll(extractCasesAux(tgClass,idoRel,usertask,userRol,user));
				}
			}
			else{
				LinkedList<ObjectValue> valplayin=playin.getValueList();
				Iterator<ObjectValue> itvplayin= valplayin.listIterator();
				while(itvplayin.hasNext()){
					ObjectValue value=itvplayin.next();
					idoRel=value.getValue();
					result.addAll(extractCasesAux(tgClass,idoRel,usertask,userRol,user));
				}
			}
		}
		return result;
	}
	*//**
	 * Este método se va a encargar de extraer los casos concretos habiendo fijado previamente <br>
	 * una relación en la que juega la tgClass.<br><br>
	 * 
	 * Para ello llamamos a {@link IKnowledgeBaseInfo#getAllPropertyIterator(Integer, Integer, String, Integer)}, <br>
	 * pasandole como parámetros el idoRel, con el fin de encontrar todos los peer que estan jugando en la relación <br>
	 * y definamos un caso concreto por cada peer. <br><br>
	 * 
	 * @param tgClass Entero que indica la clase a la cual apunta la UTask.
	 * @param idoRel Identificador de la relación concreta
	 * @param usertask Identificador de la UTask en la que estamos situados.
	 * @param userRol Entero que indica el rol que esta jugando el usuario logeado en ese momento.
	 * @param user Cadena que identifica al usuario logeado.
	 * @return LinkedList con un conjunto de casos concretos ({@link DataCase}).
	 * @throws NotFoundException 
	 *//*
	private LinkedList<DataCase> extractCasesAux(Integer tgClass, Integer idoRel, Integer usertask, Integer userRol, String user) throws NotFoundException {
		LinkedList<DataCase> result=new LinkedList<DataCase>();
		Iterator <Property> itp=kb.getAllPropertyIterator(idoRel, userRol, user, usertask,session);
		//TODO Nueva Version quitar si no se usa al final
		if(!itp.hasNext()){
			DataCase acase=new DataCase(tgClass,idoRel,null);
			result.add(acase);
		}
		//Fin de nueva Version
		while(itp.hasNext()){
			Property p = itp.next();
			Category c = p.getCategory();
			if (c.isPlay()){
				Integer value=null;
				if(!((ObjectProperty)p).getValueList().isEmpty()){
					value=((ObjectProperty)p).getValueList().getFirst().getValue();
				}else{
					value=((ObjectProperty)p).getFilterList().getFirst().getValue();
				}
				if (!value.equals(tgClass)){
					DataCase acase= new DataCase(tgClass,idoRel,value);
					result.add(acase);
				}
			}
		}
		return result;
	}
	public String getPropertyName(Integer idProp) {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryXML getQueryXML() {
		return kb.getQueryXML();
	}

	public Integer getRolPeer(int idRol, int idRel) {
		// TODO Auto-generated method stub
		return 0;
	}

	public HashSet<Integer> getSpecialized(Integer idto) {
		return kb.getSpecialized(idto);
	}

	public Iterator<Integer> getSuperior(Integer idto) {
		// TODO Auto-generated method stub
		return null;
	}

	public instance getTreeObject(Integer ido, Integer userRol, String user, Integer userTask) throws NotFoundException {
		System.out.println("Ido del getTreeObject:"+ido);
		imprime();
		if(isIdoAdapter(ido) ){
			Integer idoVirtual=viewMoreImportantIdoVirtual(ido);
			System.out.println("*****TRATA GETTREEOBJECT EN ADAPTER***********");
			instance i =new instance(tbIdoIdto.get(idoVirtual),idoVirtual);
			addPropertyTreeObject(i,idoVirtual,userRol,user,userTask,new HashSet<Integer>());
			System.out.println(i);
			return i;
		}else if(tbAdapter.containsKey(ido)){
			System.out.println("*****TRATA GETTREEOBJECT EN ADAPTER***********");
			instance i =new instance(tbIdoIdto.get(ido),ido);
			addPropertyTreeObject(i,ido,userRol,user,userTask,new HashSet<Integer>());
			System.out.println(i);
			return i;
		}else{
			System.out.println("*****TRATA GETTREEOBJECT EN MOTOR***********");
			System.out.println("IDO="+ido+" userRol="+userRol+" user="+user+" userTask="+userTask);
			return kb.getTreeObject(ido, userRol, user, userTask);
		}
		
	}

	private boolean isIdoAdapter(Integer ido) {
		boolean result=false;
		
		Set<Integer> key=tbAdapter.keySet();
		Iterator<Integer> it=key.iterator();
		while (it.hasNext()){
			DataAdapter da=tbAdapter.get(it.next());
			Property p=da.getProperty(Constants.IdPROP_RDN);
			Integer idoOrignal=da.getOriginalIdoClass(p);
			if(idoOrignal.equals(ido)){
				result=true;
				return result;
			}
		}
		return result;
	}

	private void imprime() {	
		Set<Integer> k =tbAdapterNameClass.keySet();
		Iterator<Integer> it=k.iterator();
		while(it.hasNext()){
			Integer i=it.next();
			System.out.println("Idto Virtual:"+i +" Nombre Virtual:"+tbAdapterNameClass.get(i));
			
		}
		
	}
	private void imprime2(Map<Integer,DataAdapter> tb){
		Set<Integer> k =tb.keySet();
		Iterator<Integer> it=k.iterator();
		while(it.hasNext()){
			Integer i=it.next();
			DataAdapter da= tb.get(i);
			System.out.println(da.toString());
		}
	}
	private void addPropertyTreeObject(instance i, Integer ido, Integer userRol, String user, Integer userTask, HashSet<Integer> listProcessedChildren) throws NotFoundException {
		DataAdapter da=tbAdapter.get(ido);
		//System.out.println("Entraaa");
		listProcessedChildren.add(ido);
		Iterator<Property> itp=null;
		if(tbIdoIdto.containsKey(ido)){
			itp =da.getPropertyIterator();
		}else{
			itp =kb.getAllPropertyIterator(ido, userRol, user, userTask,session);
		}
		
		while(itp.hasNext()){
			Property p=itp.next();
			if(p instanceof ObjectPropertyVirtual || p instanceof ObjectProperty){
				if(!((ObjectProperty)p).getCategory().isPlayIn()){
					
					LinkedList<ObjectValue> filterList=((ObjectProperty)p).getFilterList();
					if(filterList.size()>0){
						ObjectValue ov = filterList.getFirst();
						if (!listProcessedChildren.contains(ov.getValue()))
							addPropertyTreeObject(i,ov.getValue(),userRol,user,userTask,listProcessedChildren);
					}
				}
			}
			i.addProperty(ido, p);
		}
	}
	public boolean isDataProperty(Integer idProp) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isObjectProperty(Integer idProp) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSpecialized(int idto, int posSuperior) {
		// TODO Auto-generated method stub
		return false;
	}

	

	public void setRuler(Ruler ruler) {
		// TODO Auto-generated method stub
		
	}

	public void setServer(docServer server) {
		// TODO Auto-generated method stub
		
	}


	public int specializeIn(int id, int idtoSpecialized) {
		// TODO Auto-generated method stub
		return 0;
	}
	


	public selectData doQuery(instance i, ArrayList<SelectQuery> s, String user, Integer userRol,Integer task) throws SystemException, RemoteSystemException, CommunicationException, NotFoundException{
		selectData result=new selectData();
//		QueryXML qxml=this.getQueryXML();
//		HashMap<Integer,LinkedList<InfoInstance>> info=new HashMap<Integer,LinkedList<InfoInstance>>(); 
//		instance uni=unadapte(i,user,userRol,task);
//		info=extractInfoInstance(i,user,userRol,task);
//		ArrayList<SelectQuery> uns=unadapteSelect(s);
//		Element e=qxml.toQueryXML(uni, "Query", user, uns);
//		communicator c=Singleton.getInstance().getComm();
//		result=c.serverGetQuery(userRol, task, user, e);
//		result=adapte(result, info,user,userRol,task);
		return result;
	}
	private HashMap<Integer, LinkedList<InfoInstance>> extractInfoInstance(instance i, String user, Integer userRol, Integer task) {
		HashMap<Integer, LinkedList<InfoInstance>> result=new HashMap<Integer, LinkedList<InfoInstance>>();
		Integer ido=i.getIDO();
		extractInfoInstanceAux(ido,result,i,user,userRol,task);
		return result;
	}

	private void extractInfoInstanceAux(Integer ido, HashMap<Integer, LinkedList<InfoInstance>> result, instance i, String user, Integer userRol, Integer task) {
		Iterator<Property> itp=i.getAllPropertyIterator(ido, userRol, user, task,session);
		while(itp.hasNext()){
			Property p=itp.next();
			if(p instanceof DataProperty){
				DataAdapter da=tbAdapter.get(ido);
				Integer idtoOriginal=da.getOriginalIdtoClass(p);
				Integer idoOriginal=da.getOriginalIdoClass(p);
				Integer idPropOriginal=da.getOriginalIdProp(p);
				LinkedList<InfoInstance> l=new LinkedList<InfoInstance>();
				if(result.containsKey(ido)){
					l=result.get(ido);
				}
				InfoInstance inf= new InfoInstance(idoOriginal,idtoOriginal,idPropOriginal,p.getRol());
				l.add(inf);
				result.put(ido, l);
			}
		}
	}

	private ArrayList<SelectQuery> unadapteSelect(ArrayList<SelectQuery> s) {
		ArrayList<SelectQuery> uns=new ArrayList<SelectQuery>();
		Iterator<SelectQuery> its=s.iterator();
		Integer idoV=null;
		while(its.hasNext()){
			SelectQuery sq=its.next();
			Integer idos=sq.getIdObject();
			Integer idtos=sq.getIdClass();
			Integer idProps=sq.getIdProp();
			Iterator<Integer> itido=tbIdoIdto.keySet().iterator();
			while(itido.hasNext()){
				Integer key=itido.next();
				if(tbIdoIdto.get(key).equals(idtos)){
					idoV=tbIdoIdto.get(key);
				}
			}
			DataAdapter da=tbAdapter.get(idoV);
			Property p= da.getProperty(idProps);
			Integer idoRelO=p.getIdoRel();
			Integer idRolO=p.getRol();
			Integer idoO=da.getOriginalIdoClass(p);
			Integer idtoO=da.getOriginalIdtoClass(p);
			Integer idPropO=da.getOriginalIdProp(p);
			SelectQuery usq=new SelectQuery(idoO,idtoO,idRolO,idPropO,idoRelO);
			uns.add(usq);
		}
		return uns;
	}
	private selectData adapte(selectData result, HashMap<Integer, LinkedList<InfoInstance>> info, String user, Integer userRol, Integer task) {
		Iterator its=result.getIterator();
		selectData sd= new selectData();
		while(its.hasNext()){
			instance i=(instance)its.next();
			Integer ido= i.getIDO();
			Integer idto= i.getIdTo();
			instance ai=adapteInstance(ido,idto,i,info,user,userRol,task);
			sd.addInstance(ai);
		}
		return sd;
	}
	private instance adapteInstance(Integer ido, Integer idto, instance i, HashMap<Integer, LinkedList<InfoInstance>> info, String user, Integer userRol, Integer task) {
		instance result= null;
		
		Iterator<Integer> itk=info.keySet().iterator();
		LinkedList<Integer> linfoprocessed=new LinkedList<Integer>();
		while (itk.hasNext()){
			Integer key=itk.next();
			LinkedList<InfoInstance> linfo=info.get(key);
			Iterator<InfoInstance> itinfo=linfo.listIterator();
			while(itinfo.hasNext()){
				InfoInstance infoinstance=itinfo.next();
				Integer idoOriginal=infoinstance.getIdo();
				
				Iterator<Property> itp=i.getAllPropertyIterator(idoOriginal, userRol, user, task,session);
				while(itp.hasNext()){
					Property p=itp.next();
					if(p instanceof DataProperty){
						DataProperty pd=(DataProperty)p;
						//TODO Fran Gay
					}
				}
				
			}
		}
		
		
		return result;
	}
	//TODO Revisar debido a tema de las OP que irian desde el tgClass al tgRel si estos estan
	//adaptados en una misma clase virtual. Técnica: LLamar mejor al motor para cojer las OP
	//originales denuevo.
	private instance unadapte(instance i, String user, Integer userRol, Integer task) throws NotFoundException {
		HashMap<Integer,LinkedList<Property>> tb=new HashMap<Integer,LinkedList<Property>>();
		
		Integer idoinstance=i.getIDO();
		Integer idtoinstance=i.getIdTo();
		Integer targetIdo=tbAdapter.get(idoinstance).getProperty(Constants.IdPROP_RDN).getIdo();
		Iterator<Property> itp=i.getAllPropertyIterator(idoinstance,userRol,user,task,session);
		DataAdapter d= tbAdapter.get(targetIdo);
		Integer targetIdoO=d.getOriginalIdoClass(d.getProperty(Constants.IdPROP_RDN));
		Integer targetIdtoO=d.getOriginalIdtoClass(d.getProperty(Constants.IdPROP_RDN));
		instance uni=new instance(targetIdtoO,targetIdoO);
		while(itp.hasNext()){
			Property p=itp.next();
			Integer idoproperty=p.getIdo();
			Integer idProp=p.getIdProp();
			DataAdapter da=tbAdapter.get(idoproperty);
			Property pvirtual=da.getProperty(idProp);
			Integer idoOriginal=da.getOriginalIdoClass(pvirtual);
			Integer idtoOriginal=da.getOriginalIdtoClass(pvirtual);
			Integer idPropOriginal=da.getOriginalIdProp(pvirtual);
			Iterator<Property> ipo=kb.getPropertyIterator(idoOriginal, idPropOriginal, userRol, user, task,session);
			while(ipo.hasNext()){
				Property porginal=ipo.next();
				if(tb.containsKey(idoOriginal)){
					LinkedList<Property> lp= tb.get(idoOriginal);
					lp.add(porginal);
					tb.put(idoOriginal, lp);
				}else{
					LinkedList<Property> lp= new LinkedList<Property>();
					lp.add(porginal);
					tb.put(idoOriginal, lp);
				}
			}
		}
		LinkedList<Integer> ladd=new LinkedList<Integer>();
		ladd.add(targetIdoO);
		addPropertysInstance(ladd,uni,targetIdoO,tb.get(targetIdoO));
		return uni;
	}

	private void addPropertysInstance(LinkedList<Integer> ladd, instance uni, Integer ido, LinkedList<Property> tb) {
		
		ladd.add(ido);
		Iterator<Property> itp=null;
		itp =tb.iterator();
		while(itp.hasNext()){
			Property p=itp.next();
			if(p instanceof ObjectPropertyVirtual || p instanceof ObjectProperty){
				if(!((ObjectProperty)p).getCategory().isPlayIn()){
					LinkedList<ObjectValue> lov=new LinkedList<ObjectValue>();
					if(((ObjectProperty)p).getValueList().size()<0){
						lov=((ObjectProperty)p).getValueList();
					}else{
						lov=((ObjectProperty)p).getFilterList();
					}
					if(lov.size()>0){
						ObjectValue ov = lov.getFirst();
						if (!ladd.contains(ov.getValue())){
							ladd.add(ov.getValue());
							addPropertysInstance(ladd,uni,ov.getValue(),tb);
						}
					}
				}
			}
			uni.addProperty(ido, p);
		}
	}

	public boolean isUnit(Integer cls) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public access getAccessOverObject(Integer id, Integer userRol, String user, Integer usertask) throws NotFoundException {
		return kb.getAccessOverObject(id, userRol, user, usertask);
	}


	public Iterator<Integer> getIndividualsIterator(int idto) {
		LinkedList<Integer> result=new LinkedList<Integer>();
		if(tbAdapterNameClass.containsKey(idto)){
			Iterator<Integer> it=tbIdoIdto.keySet().iterator();
			while(it.hasNext()){
				Integer value=it.next();
				if(tbIdoIdto.get(value).equals(idto)){
					result.add(value);					
				}
			}
		}else{
			return kb.getIndividualsIterator(idto);
		}
		return result.iterator();
	}
	public Integer getPropertyInverse(Integer idProp) {
		return kb.getPropertyInverse(idProp);
	}
	
	

	private Integer getIdoVirtual(int ido) {
		Integer result=null;
		
		Set<Integer> key=tbAdapter.keySet();
		Iterator<Integer> it=key.iterator();
		while (it.hasNext()){
			result=it.next();
			DataAdapter da=tbAdapter.get(result);
			Property p=da.getProperty(Constants.IdPROP_RDN);
			Integer idoOrignal=da.getOriginalIdoClass(p);
			if(idoOrignal.equals(ido)){
				
				return result;
			}
		}
		return result;
	}

	public Category getCategory(Integer idProp) throws NotFoundException {
		Property pfind=null;
		if(isPropertyAdapte(idProp)){
			Collection<DataAdapter> sda=tbAdapter.values();
			Iterator<DataAdapter> itda=sda.iterator();
			while(itda.hasNext()){
				DataAdapter da=itda.next();
				Iterator<Property> itp=da.getPropertyIterator();
				while(itp.hasNext()){
					Property p=itp.next();
					if(p.getIdProp().equals(idProp)){
						pfind=p;
					}
				}
			}
			return pfind.getCategory();
		}else{
			return kb.getCategory(idProp);
		}
		
	}
	
	private boolean isPropertyAdapte(Integer idProp) {
		Collection<DataAdapter> sda=tbAdapter.values();
		Iterator<DataAdapter> itda=sda.iterator();
		while(itda.hasNext()){
			DataAdapter da=itda.next();
			Iterator<Property> itp=da.getPropertyIterator();
			while(itp.hasNext()){
				Property p=itp.next();
				if(p.getIdProp().equals(idProp)){
					return true;
				}
			}
		}
		return false;
	}

	private boolean isUTaskAdapte(int ido) {
		if(UTaskAdapte.get(ido)==null){
			return false;
		}
		return UTaskAdapte.get(ido);
	}

	

	public void setRulesString(String rulesString) {
		kb.setRulesString(rulesString);
	}

	public Property getProperty(int idto, Integer ido, int idProp, Integer rol, Integer clsRel, Integer idoRel, Integer userRol, String user, Integer usertask) throws NotFoundException {
		if(tbAdapter.containsKey(ido)){
			DataAdapter da=tbAdapter.get(ido);
			return da.getProperty(idProp);
		}else{
			return kb.getProperty(idto, ido, idProp, rol, clsRel, idoRel, userRol, user, usertask);
		}
		
	}

	public HashMap<Integer, ArrayList<UserAccess>> getUsertaskOperationOver(Integer ido, String user) throws NotFoundException {
		HashMap<Integer,ArrayList<UserAccess>> map=new HashMap<Integer,ArrayList<UserAccess>>();
		if(tbIdoIdto.containsKey(ido)){
			DataAdapter da=tbAdapter.get(ido);
			Property p =da.getProperty(2);
			System.err.println(p);
			Integer idoOr=da.getOriginalIdoClass(p);
			map=kb.getUsertaskOperationOver(idoOr,user);
		}else{
			map=kb.getUsertaskOperationOver(ido,user);
		}
		return map;
	}

	public int newEvent(instance treeObject, Integer userRol, String user, Integer userTask) throws NotFoundException {
		return kb.newEvent(treeObject, userRol, user, userTask);
	}

	public Integer createPrototype(int idto, int level, Integer userRol, String user, Integer usertask, Session sess) throws NotFoundException {
		Integer ido=null;
		HashMap<Integer,Integer> idoidto=new HashMap<Integer,Integer>();
		LinkedList<Integer> lproto=new LinkedList<Integer>();
		if(tbIdoIdto.containsValue(idto)){
			Integer idoPro=CreateIdo.getInstance().newIdoPrototype();
			Iterator <Integer> itido=tbIdoIdto.keySet().iterator();
			while (itido.hasNext()){
				Integer key=itido.next();
				if(tbIdoIdto.get(key).equals(idto)){
					ido=key;
				}
			}
			DataAdapter da=tbAdapter.get(ido);
			Iterator<Property> itp=da.getPropertyIterator();
			while (itp.hasNext()){
				
				Property p= itp.next();
				Integer idoOriginal=da.getOriginalIdoClass(p);
				Integer idtoOriginal=da.getOriginalIdtoClass(p);
				if(!idoidto.containsKey(idoOriginal)){
					idoidto.put(idoOriginal,idtoOriginal);
				}
			}
			Iterator<Integer> itidoo=idoidto.keySet().iterator();
			while (itidoo.hasNext()){
				Integer key=itidoo.next();
				lproto.add(kb.createPrototype(idoidto.get(key), level, userRol, user, usertask,session));
			}
			DataAdapter dproto= new DataAdapter(idoPro,idto);
			Iterator<Integer> itproto=lproto.iterator();
			
			while(itproto.hasNext()){
				Integer proto=itproto.next();
				Integer idGroup=0;
				String nameGroup="PRINCIPAL";
				Iterator <Property> itpp=kb.getAllPropertyIterator(proto, userRol, user, usertask,session);
				while(itpp.hasNext()){
					Property p=itpp.next();
					if(p instanceof DataProperty){
						if (p.getRol()==null){
							dproto=createDataPropertyVirtual(p,proto,idoPro,idto,dproto,null,null,null,true);
						}else{
							String nameRol=kb.getClassNameOfObject(p.getIdo());
							dproto=createDataPropertyVirtual(p,proto,idoPro,idto,dproto,idGroup,null,nameRol,false);
						}
					}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
						idGroup++;
						Integer idGOPS=idGroup;
						String nameOPS=p.getName();
						Integer value=null;
						if(!((ObjectProperty)p).getValueList().isEmpty()){
							value=((ObjectProperty)p).getValueList().getFirst().getValue();
						}else{
							value=((ObjectProperty)p).getFilterList().getFirst().getValue();
						}
						boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
						if(!hasPlayIn){
							Iterator<Property> itaux=kb.getAllPropertyIterator(value, userRol, user, usertask,session);
							while(itaux.hasNext()){
								Property prop=itaux.next();
								if (prop instanceof DataProperty){
									DataProperty dp=(DataProperty)prop;
									da=createDataPropertyVirtual(dp,value,idoPro,idto,da,idGOPS,null,nameOPS,true);
								}else{
									ObjectProperty dp=(ObjectProperty)prop;
									da=createObjectPropertyVirtual(dp,idoPro,idto,da,idGOPS,null,nameOPS,true);
								}
							}
						}else{
							dproto=createObjectPropertyVirtual(p,idoPro,idto,dproto,null,null,null,true);
						}
					}else{
						dproto=createObjectPropertyVirtual(p,idoPro,idto,dproto,null,null,null,true);
					}
				}
			}
			return idoPro;
		}else{
			return kb.createPrototype(idto, level, userRol, user, usertask,session);
		}
	}

	public Iterator<Property> getAllPropertyIterator(int ido, Integer userRol, String user, Integer usertask, Session sessionPadre) throws NotFoundException {
		System.out.println("***LLAMA CON IDO="+ido+"*******");
		if(isIdoAdapter(ido)){
			Integer idoVirtual=getIdoVirtual(ido);
			DataAdapter da= tbAdapter.get(idoVirtual);
			if(da==null){
				System.out.println("***IDO ADAPTADO="+idoVirtual);
				System.out.println("IDTO ORIGINAL="+kb.getClassNameOfObject(ido));
				System.out.println("IDTO VIRTUAL="+tbIdoIdto.get(idoVirtual));
			}
			
			return da.getPropertyIterator();
		}else if (tbAdapter.containsKey(ido)){
			DataAdapter da= tbAdapter.get(ido);
			return da.getPropertyIterator();
		}
		return kb.getAllPropertyIterator(ido, userRol, user, usertask,session);
	}

	public Iterator<Property> getPropertyIterator(int ido, int idProp, Integer userRol, String user, Integer usertask, Session s) throws NotFoundException {
		System.out.println("***LLAMA CON IDO="+ido+"*****SIN ALL**");
		if(idProp==Constants.IdPROP_TARGETCLASS && isUTaskAdapte(ido)){
			Iterator<Property> itp=kb.getPropertyIterator(ido, idProp, userRol, user, usertask,session);
			Property p = itp.next();
			
			Integer tgClass=((ObjectProperty)p).getFilterList().getFirst().getValue();
			Integer idoVirtual=viewMoreImportantIdoVirtual(tgClass);
			ObjectValue ov= new ObjectValue();
			ov.setValue(idoVirtual);
			ov.setValueCls(tbIdoIdto.get(idoVirtual));
			LinkedList<ObjectValue> flist=new LinkedList<ObjectValue>();
			flist.add(ov);
			LinkedList<Integer> rlist=new LinkedList<Integer>();
			rlist.add(tbIdoIdto.get(idoVirtual));
			((ObjectProperty)p).setRangoList(rlist);
			((ObjectProperty)p).setFilterList(flist);
			LinkedList<Property> plist= new LinkedList<Property>();
			plist.add(p);
			return plist.iterator();
			
		}
		if(isIdoAdapter(ido)){
			Integer idoVirtual=viewMoreImportantIdoVirtual(ido);
			DataAdapter da= tbAdapter.get(idoVirtual);
			Property p=da.getProperty(idProp);
			LinkedList<Property> l = new LinkedList<Property>();
			l.add(p);
			Iterator<Property> it= l.iterator();
			return it;
		}else if(tbAdapter.containsKey(ido)){
			DataAdapter da=tbAdapter.get(ido);
			Property p=da.getProperty(idProp);
			LinkedList<Property> l = new LinkedList<Property>();
			l.add(p);
			Iterator<Property> it= l.iterator();
			return it;
			
		}else{
			
			return kb.getPropertyIterator(ido, idProp, userRol, user, usertask,session);
		}
	}

	public void loadMetaData() throws NotFoundException {
		// TODO Auto-generated method stub
		
	}

	public void setValue(int ido, int idProp, Integer rol, Integer idoRel, Value oldValue, Value newValue, Integer userRol, String user, Integer usertask, Session s) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException {
		if(idProp<0){
			DataAdapter da=tbAdapter.get(ido);
			Property pr=da.getProperty(idProp);
			Integer idoOriginal=da.getOriginalIdoClass(pr);
			Integer idPropOriginal=da.getOriginalIdProp(pr);
			if(pr instanceof DataProperty){
				DataProperty dpr=(DataProperty)pr;
				if(dpr.getValueList().size()>0){
					LinkedList<DataValue> ldv=dpr.getValueList();
					LinkedList<DataValue> nldv=new LinkedList<DataValue>(); 
					Iterator<DataValue> itdv=ldv.iterator();
					while(itdv.hasNext()){
						Value v=itdv.next();
						if(v.equals(oldValue)){
							nldv.add((DataValue)newValue);
						}else{
							nldv.add((DataValue)v);
						}
					}
					dpr.setValueList(nldv);
				}
			}else{
				ObjectProperty opr=(ObjectProperty)pr;
				if(opr.getValueList().size()>0){
					LinkedList<ObjectValue> lov=opr.getValueList();
					LinkedList<ObjectValue> nlov=new LinkedList<ObjectValue>(); 
					Iterator<ObjectValue> itdv=lov.iterator();
					while(itdv.hasNext()){
						Value v=itdv.next();
						if(v.equals(oldValue)){
							nlov.add((ObjectValue)newValue);
						}else{
							nlov.add((ObjectValue)v);
						}
					}
					opr.setValueList(nlov);
				}
			}
			da.changeProperty(idProp, pr);
			idoRel=pr.getIdoRel();
			kb.setValue(idoOriginal, idPropOriginal,rol,idoRel,oldValue,newValue,userRol,user,usertask,session);
		}else{
			kb.setValue(ido, idProp,rol,idoRel,oldValue,newValue,userRol,user,usertask,session);
		}
	}
	

}


*/