package dynagent.gui.forms.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.basicobjects.Groups;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.properties.Property;
import dynagent.gui.KnowledgeBaseAdapter;

public class GroupComponents implements IGroupProperties{

	private HashMap<Integer, ArrayList<Groups>> hashPropGroup;
	private HashMap<Integer,String> hashNameIdGroup;
	private IOrderProperties orderProperties;
	private HashMap<Integer,Integer> hashGroupOrder;
	private HashMap<Integer,Integer> hashGroupOrderAndPriority;
	
	public GroupComponents(ArrayList<Groups> groupList,IOrderProperties orderProperties){
		//m_kba=Singleton.getInstance().getKnowledgeBase();
		//this.orderPropertyList=orderPropertyList;
		hashPropGroup=new HashMap<Integer, ArrayList<Groups>>();
		hashNameIdGroup=new HashMap<Integer, String>();
		hashGroupOrder=new HashMap<Integer, Integer>();
		this.orderProperties=orderProperties;
		addGroupList(groupList);
	}
	
	public void addGroupList(ArrayList<Groups> groupList){
		Iterator<Groups> itr=groupList.iterator();
		while(itr.hasNext()){
			Groups group=itr.next();
			if(hashPropGroup.containsKey(group.getIdProp()))
				hashPropGroup.get(group.getIdProp()).add(group);
			else{
				ArrayList<Groups> list=new ArrayList<Groups>();
				list.add(group);
				hashPropGroup.put(group.getIdProp(),list);
			}
			
			if(!hashNameIdGroup.containsKey(group.getIdGroup()))
				hashNameIdGroup.put(group.getIdGroup(),group.getNameGroup());
			
			hashGroupOrder.put(group.getIdGroup(), group.getOrder());
		}
	}
	
	public Integer getIdGroup(int idProp, Integer idto, Integer idtoUserTask) throws NotFoundException {
		Integer idGroup=null;
		if(hashPropGroup.containsKey(idProp)){
			Iterator<Groups> itr=hashPropGroup.get(idProp).iterator();
			while(idGroup==null && itr.hasNext()){
				Groups group=itr.next();
				if(group.getIdtoClass()==null || (idto!=null && /*(*/group.getIdtoClass().equals(idto)) /*|| kba.isSpecialized(idto, group.getIdtoClass()))*/)
					if(group.getUTask()==null || (idtoUserTask!=null && group.getUTask().equals(idtoUserTask)))
						if(group.getIdProp()==idProp)
							idGroup=group.getIdGroup();
			}
		}
		return idGroup;
	}

	public String getNameGroup(int idGroup) {
		return hashNameIdGroup.get(idGroup);
	}
	
	public void buildOrderForm(ArrayList<Property> list,Integer idtoUserTask,KnowledgeBaseAdapter kba) throws NotFoundException{
		hashGroupOrderAndPriority=new HashMap<Integer, Integer>();
		
		HashMap<Integer,ArrayList<Property>> hashGroupProperty=new HashMap<Integer, ArrayList<Property>>();
		Iterator<Property> itr=list.iterator();
		while(itr.hasNext()){
			Property prop=itr.next();
			int idProp=prop.getIdProp();
			int idto=prop.getIdto();
			Integer idGroup=kba.getGroup(idProp,idto,idtoUserTask);
			
			if(idGroup!=null){
				if(hashGroupProperty.containsKey(idGroup)){
					hashGroupProperty.get(idGroup).add(prop);
				}else{
					ArrayList<Property> listProp=new ArrayList<Property>();
					listProp.add(prop);
					hashGroupProperty.put(idGroup,listProp);
				}
			}
		}
		
		HashMap<Integer,ArrayList<Integer>> hashPriorityGroup=new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<GroupOrderAndPriority> listGroupOrderAndPriority=new ArrayList<GroupOrderAndPriority>();
		ArrayList<Integer> listPriority=new ArrayList<Integer>();
		Iterator<Integer> itrIdGroup=hashGroupProperty.keySet().iterator();
		while(itrIdGroup.hasNext()){
			int idGroup=itrIdGroup.next();
			int priority=0;
			ArrayList<Property> propList=hashGroupProperty.get(idGroup);
			Iterator<Property> itrProp=propList.iterator();
			/*Calculamos la prioridad del grupo puntuando:
			 * 
			 * 90% de la maxima prioridad encontrada
			 * 10% de la suma de las prioridades encontradas entre el numero de properties
			 * 
			 * */
			int maxPriority=priority;
			while(itrProp.hasNext()){
				Property prop=itrProp.next();
				int auxPriority=orderProperties.getPriority(prop,idGroup);
				priority+=auxPriority;
				if(auxPriority>maxPriority){
					maxPriority=auxPriority;
				}
			}
			priority=(int)((maxPriority*0.9)+((priority/propList.size())*0.1));
			if(hashPriorityGroup.containsKey(priority))
				hashPriorityGroup.get(priority).add(idGroup);
			else{
				ArrayList<Integer> listGroup=new ArrayList<Integer>();
				listGroup.add(idGroup);
				hashPriorityGroup.put(priority,listGroup);
				listPriority.add(priority);
			}
			
			GroupOrderAndPriority gOP=new GroupOrderAndPriority(idGroup,hashGroupOrder.get(idGroup), priority);
			listGroupOrderAndPriority.add(gOP);
		}
		
		//A partir del orden y la prioridad obtenemos el orden definitivo. Siendo el orden 0 mayor que el 1, y la prioridad 1 mayor que 0. Solo se mira la prioridad a igualdad de orden.
		Collections.sort(listGroupOrderAndPriority);
		
		Iterator<GroupOrderAndPriority> itrGroupOrderAndPriority=listGroupOrderAndPriority.iterator();
		int order=1;
		while(itrGroupOrderAndPriority.hasNext()){
			GroupOrderAndPriority gOP=itrGroupOrderAndPriority.next();
			hashGroupOrderAndPriority.put(gOP.idGroup, order);
			order++;
		}
	}
	
	public int getOrderGroup(int idGroup){
		if(idGroup==0)
			return 0;

		return this.hashGroupOrderAndPriority.get(idGroup);
	}
	
	public class GroupOrderAndPriority implements Comparable{

		int idGroup;
		int order;
		int priority;
		
		public GroupOrderAndPriority(int idGroup,int order,int priority){
			this.idGroup=idGroup;
			this.order=order;
			this.priority=priority;
		}

		@Override
		public int compareTo(Object o) {
			GroupOrderAndPriority other=(GroupOrderAndPriority)o;
			if(this.order<other.order){
				return -1;
			}else if(this.order>other.order){
				return 1;
			}else{
				if(this.priority>other.priority){
					return -1;
				}else if(this.priority<other.priority){
					return 1;
				}else return 0;
			}
		}
	}

}
