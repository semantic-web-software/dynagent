package dynagent.gui.forms.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.basicobjects.EssentialProperty;

public class HighlightedComponents implements IEssentialProperties{

	private HashMap<Integer,ArrayList<EssentialProperty>> hashPropEssentialProperty;
	
	public HighlightedComponents(ArrayList<EssentialProperty> essentialPropertyList){
		hashPropEssentialProperty=new HashMap<Integer, ArrayList<EssentialProperty>>();
		addEssentialPropertyList(essentialPropertyList);
	}
	
	public void addEssentialPropertyList(ArrayList<EssentialProperty> essentialPropertyList) {
		Iterator<EssentialProperty> itr=essentialPropertyList.iterator();
		while(itr.hasNext()){
			EssentialProperty ep=itr.next();
			if(hashPropEssentialProperty.containsKey(ep.getProp()))
				hashPropEssentialProperty.get(ep.getProp()).add(ep);
			else{
				ArrayList<EssentialProperty> list=new ArrayList<EssentialProperty>();
				list.add(ep);
				hashPropEssentialProperty.put(ep.getProp(),list);
			}
		}
	}

	public boolean isEssentialProperty(Integer idtoUserTask, Integer idto, int idProp) {
		if(hashPropEssentialProperty.containsKey(idProp)){
			Iterator<EssentialProperty> itr=hashPropEssentialProperty.get(idProp).iterator();
			while(itr.hasNext()){
				EssentialProperty ep=itr.next();
				if(ep.getUTask()==null || ep.getUTask().equals(idtoUserTask)){
					if(ep.getIdto()==null || ep.getIdto().equals(idto))
						return true;
				}
			}
		}
		return false;
	}
}
