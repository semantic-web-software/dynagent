package dynagent.gui.forms.utils;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.OrderProperty;
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
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;

public class OrderComponents implements IOrderProperties{

	//private ArrayList<OrderProperty> orderPropertyList;
	private HashMap<Integer,ArrayList<OrderProperty>> hashPropOrderProperty;
	private HashMap<Integer,Integer> hashPropOrder;
	private int ido;//Ido principal
	//private KnowledgeBaseAdapter m_kba;
	
	public OrderComponents(ArrayList<OrderProperty> orderPropertyList){
		//m_kba=Singleton.getInstance().getKnowledgeBase();
		//this.orderPropertyList=orderPropertyList;
		hashPropOrderProperty=new HashMap<Integer, ArrayList<OrderProperty>>();
		addOrderPropertyList(orderPropertyList);
	}
	
	public void addOrderPropertyList(ArrayList<OrderProperty> orderPropertyList){
		Iterator<OrderProperty> itr=orderPropertyList.iterator();
		while(itr.hasNext()){
			OrderProperty orderP=itr.next();
			if(hashPropOrderProperty.containsKey(orderP.getProp()))
				hashPropOrderProperty.get(orderP.getProp()).add(orderP);
			else{
				ArrayList<OrderProperty> list=new ArrayList<OrderProperty>();
				list.add(orderP);
				hashPropOrderProperty.put(orderP.getProp(),list);
			}
		}
	}
	
	public void removeOrderPropertyList(ArrayList<OrderProperty> orderPropertyList){
		Iterator<OrderProperty> itr=orderPropertyList.iterator();
		while(itr.hasNext()){
			OrderProperty orderP=itr.next();
			if(hashPropOrderProperty.containsKey(orderP.getProp()))
				hashPropOrderProperty.remove(orderP.getProp());
		}
	}
	
	public void buildOrderForm(int ido,ArrayList<Property> list,Integer idtoUserTask,KnowledgeBaseAdapter kba) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		this.ido=ido;
		hashPropOrder=new HashMap<Integer, Integer>();
		HashMap<Integer,ArrayList<Integer>> hashGroupSec=new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer,ArrayList<Property>> hashSecProperty=new HashMap<Integer, ArrayList<Property>>();
		Iterator<Property> itr=list.iterator();
		while(itr.hasNext()){
			Property prop=itr.next();
			int idProp=prop.getIdProp();
			int idto=prop.getIdto();
			Integer idGroup=kba.getGroup(idProp,idto,idtoUserTask);
			
			int idPropAux=idProp;
			if(idtoUserTask!=null && kba.isSpecialized(idtoUserTask, Constants.IDTO_REPORT) && kba.isSpecialized(idto, Constants.IDTO_PARAMS)){
				//Truco para que a las propiedades de los parametros de reports se le aplique el mismo orden que a las definidas para un formulario normal, si existe una property que se llame igual.
				//Esto tenemos que hacerlo porque las propiedades de reports se crean dinamicamente por lo que no existen como individuo propiedad por lo que no podemos asignarle un orden.
				String label=kba.getLabelProperty(prop, idto, idtoUserTask).replaceAll(" ", "_").toLowerCase();
				try{
					idPropAux=kba.getIdProp(label);
				}catch(Exception ex){
					System.err.println("No existe ninguna propiedad de modelo con el mismo nombre que la de report:"+label+" por lo que no se le aplica orden");
				}
			}
			
			if(hashPropOrderProperty.containsKey(idPropAux)){
				Iterator<OrderProperty> itrOrderProp=hashPropOrderProperty.get(idPropAux).iterator();
				while(itrOrderProp.hasNext()){
					OrderProperty orderP=itrOrderProp.next();
					if((orderP.getIdto()==null || orderP.getIdto()==idto) && (orderP.getGroup()==null || orderP.getGroup().equals(idGroup))){
						if(hashSecProperty.containsKey(orderP.getSec()))
							hashSecProperty.get(orderP.getSec()).add(prop);
						else{
							ArrayList<Property> listProp=new ArrayList<Property>();
							listProp.add(prop);
							hashSecProperty.put(orderP.getSec(),listProp);
						}
						if(idGroup!=null){
							/*if(hashGroupProperty.containsKey(idGroup)){
								hashGroupProperty.get(idGroup).add(prop);
							*/
							if(hashGroupSec.containsKey(idGroup)){
								ArrayList<Integer> secList=hashGroupSec.get(idGroup);
								if(!secList.contains(orderP.getSec()))
									secList.add(orderP.getSec());
								
							}else{
								/*ArrayList<Property> listProp=new ArrayList<Property>();
								listProp.add(prop);
								hashGroupProperty.put(idGroup,listProp);
								*/
								ArrayList<Integer> secList=new ArrayList<Integer>();
								secList.add(orderP.getSec());
								hashGroupSec.put(idGroup,secList);
							}
						}
						
						hashPropOrder.put(idProp, orderP.getOrder());
					}
				}
			}
		}
		
		buildOrderComponent(hashGroupSec,hashSecProperty);
		
	}
	
	private void buildOrderComponent(HashMap<Integer,ArrayList<Integer>> hashGroupSec,HashMap<Integer,ArrayList<Property>> hashSecProperty){
		Iterator<Integer> itr=hashGroupSec.keySet().iterator();
		while(itr.hasNext()){
			int idGroup=itr.next();
			ArrayList<Integer> secList=hashGroupSec.get(idGroup);
			int size=secList.size();
			if(size>1){
				HashMap<Integer,ArrayList<Integer>> hashPrioritySec=new HashMap<Integer, ArrayList<Integer>>();
				ArrayList<Integer> listPriority=new ArrayList<Integer>();
				for(int i=0;i<size;i++){
					int sec=secList.get(i);
					ArrayList<Property> propList=hashSecProperty.get(sec);
					Iterator<Property> itrProp=propList.iterator();
					int priority=0;
					while(itrProp.hasNext()){
						Property prop=itrProp.next();
						priority+=getPriority(prop, idGroup);
					}
					priority=priority/propList.size();
					if(hashPrioritySec.containsKey(priority))
						hashPrioritySec.get(priority).add(sec);
					else{
						ArrayList<Integer> listSec=new ArrayList<Integer>();
						listSec.add(sec);
						hashPrioritySec.put(priority,listSec);
						listPriority.add(priority);
					}
				}
				
				// Ordenamos la lista de prioridades y le asignamos un orden descendente para cada grupo, pudiendo tener dos grupos el mismo orden
				ArrayList<Integer> listOrder=new ArrayList<Integer>();
				HashMap<Integer,ArrayList<Integer>> hashOrderProp=new HashMap<Integer, ArrayList<Integer>>();
				Collections.sort(listPriority,Collections.reverseOrder());
				Iterator<Integer> itrPriority=listPriority.iterator();
				int orderTotal=0;
				while(itrPriority.hasNext()){
					int priority=itrPriority.next();
					Iterator<Integer> itrSec=hashPrioritySec.get(priority).iterator();
					while(itrSec.hasNext()){
						int sec=itrSec.next();
						Iterator<Property> itrProp=hashSecProperty.get(sec).iterator();
						while(itrProp.hasNext()){
							Property prop=itrProp.next();
							int idProp=prop.getIdProp();
							int ord=hashPropOrder.get(idProp);
							if(hashOrderProp.containsKey(ord))
								hashOrderProp.get(ord).add(sec);
							else{
								ArrayList<Integer> listProp=new ArrayList<Integer>();
								listProp.add(idProp);
								hashOrderProp.put(ord,listProp);
								listOrder.add(ord);
							}
						}
						
						Collections.sort(listOrder);
						Iterator<Integer> itrOrder=listOrder.iterator();
						while(itrOrder.hasNext()){
							int order=itrOrder.next();
							Iterator<Integer> itrProperties=hashOrderProp.get(order).iterator();
							while(itrProperties.hasNext()){
								int idProp=itrProperties.next();
								hashPropOrder.put(idProp, order+orderTotal);
							}
							orderTotal++;// Le sumamos uno para que el proximo order se incremente
						}
					}
					
					orderTotal++;//Le sumamos 1 para que el order de la proxima secuencia no sea correlativo a la anterior secuencia 
				}
			}
		}
	}
	
	/*private void setOrder(HashMap<OrderProperty,ArrayList<Property>> hashOrderPropertyProperty){
		
	}*/
	
	
	//TODO idGroup no se usa porque en teoria una property pertenecera a un solo grupo
	public int getOrder(int idProp,Integer idGroup){
		int order=0;
		if(hashPropOrder!=null && hashPropOrder.containsKey(idProp))
			order=hashPropOrder.get(idProp);
		return order;
	}
	
	public int getPriority(Property property,int idGroup){
		int idProp=property.getIdProp();
		Integer cardMax=property.getCardMax();
		Integer cardMin=property.getCardMin();
		int dataType=-1;
		if(property instanceof DataProperty)
			dataType=((DataProperty)property).getDataType();
		else{
			if(!((ObjectProperty)property).getEnumList().isEmpty())
				dataType=Constants.IDTO_ENUMERATED;
		}
		int prioridad= idProp==Constants.IdPROP_RDN && ido==property.getIdo()?8:3;
		if(prioridad==3){
			//if(!property.getTypeAccess().getSetAccess()) prioridad=0;
			if( dataType==Constants.IDTO_IMAGE || property.getName().equals("imagen")) prioridad=0;
			if(property instanceof ObjectProperty && (cardMax==null || cardMax>1)) prioridad=1;
			if(dataType==Constants.IDTO_MEMO) prioridad=2;
			if(property instanceof ObjectProperty && cardMax!=null && cardMax==1 /*&& getOrder(idProp,idGroup)==0*/ ) prioridad=4;
			if( dataType==Constants.IDTO_DATE||dataType==Constants.IDTO_DATETIME ) prioridad=5;
			if( dataType==Constants.IDTO_ENUMERATED ) prioridad=6;
			if(cardMin!=null && cardMin>0 && property instanceof DataProperty) prioridad=7;
			//if( dataType==Constants.IDTO_IMAGE || property.getName().equals("imagen")) prioridad=9;
		}
		
		//System.out.println("PROPERTY="+property.getName()+" PRIORITY="+prioridad);
		return prioridad;
	}
}
