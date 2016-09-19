package dynagent.ruleengine.src.ruler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.meta.api.DocDataModel;

public class AccessEngine{

	private HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<FactAccess>>>> mapIdtoPropUTask;
	public AccessEngine(LinkedList<FactAccess> list){
		mapIdtoPropUTask=new HashMap<Integer, HashMap<Integer,HashMap<Integer,ArrayList<FactAccess>>>>();
		Iterator<FactAccess> itr=list.iterator();
		while(itr.hasNext()){
			FactAccess f=itr.next();
			addFactAccess(f);
		}
	}
	
	public LinkedList<FactAccess> getAccessOfProperty(int idto, int idProp, Integer usertask){
		LinkedList<FactAccess> list=new LinkedList<FactAccess>();
		if(mapIdtoPropUTask.containsKey(idto)){
			if(mapIdtoPropUTask.get(idto).containsKey(idProp)){
				if(mapIdtoPropUTask.get(idto).get(idProp).containsKey(usertask)){
					list.addAll(mapIdtoPropUTask.get(idto).get(idProp).get(usertask));
				}
				if(mapIdtoPropUTask.get(idto).get(idProp).containsKey(null)){
					list.addAll(mapIdtoPropUTask.get(idto).get(idProp).get(null));
				}
			}
			//Ya no tiene en cuenta que si el idto tiene set o view le afecte a todas sus propiedades
//			if(mapIdtoPropUTask.get(idto).containsKey(null)){
//				if(mapIdtoPropUTask.get(idto).get(null).containsKey(usertask)){
//					list.addAll(mapIdtoPropUTask.get(idto).get(null).get(usertask));
//				}
//				if(mapIdtoPropUTask.get(idto).get(null).containsKey(null)){
//					list.addAll(mapIdtoPropUTask.get(idto).get(null).get(null));
//				}
//			}
		}
		
		if(mapIdtoPropUTask.containsKey(null)){
			if(mapIdtoPropUTask.get(null).containsKey(idProp)){
				if(mapIdtoPropUTask.get(null).get(idProp).containsKey(usertask)){
					list.addAll(mapIdtoPropUTask.get(null).get(idProp).get(usertask));
				}
				if(mapIdtoPropUTask.get(null).get(idProp).containsKey(null)){
					list.addAll(mapIdtoPropUTask.get(null).get(idProp).get(null));
				}
			}
			if(mapIdtoPropUTask.get(null).containsKey(null)){
				//No por tener set o view en una userTask le tiene que afectar a todas sus propiedades
//				if(mapIdtoPropUTask.get(null).get(null).containsKey(usertask)){
//					list.addAll(mapIdtoPropUTask.get(null).get(null).get(usertask));
//				}
				if(mapIdtoPropUTask.get(null).get(null).containsKey(null)){
					list.addAll(mapIdtoPropUTask.get(null).get(null).get(null));
				}
			}
		}
		
		return list;
	}

	public void addFactAccess(FactAccess f) {
		if(f.getIDO()==null){//Descartamos los que tienen ido ya que estos seran consultados al motor de drools para que sea dinamico
			Integer idto=f.getIDTO();
			HashMap<Integer, HashMap<Integer, ArrayList<FactAccess>>> idtoMap=mapIdtoPropUTask.get(idto);
			if(idtoMap==null){
				idtoMap=new HashMap<Integer, HashMap<Integer,ArrayList<FactAccess>>>();
				mapIdtoPropUTask.put(idto, idtoMap);
			}
			Integer prop=f.getPROP();
			HashMap<Integer, ArrayList<FactAccess>> propMap=mapIdtoPropUTask.get(idto).get(prop);
			if(propMap==null){
				propMap=new HashMap<Integer,ArrayList<FactAccess>>();
				idtoMap.put(prop, propMap);
			}
			Integer utask=f.getTASK();
			ArrayList<FactAccess> utaskList=mapIdtoPropUTask.get(idto).get(prop).get(utask);
			if(utaskList==null){
				utaskList=new ArrayList<FactAccess>();
				propMap.put(utask, utaskList);
			}
			utaskList.add(f);
		}
	}
}
