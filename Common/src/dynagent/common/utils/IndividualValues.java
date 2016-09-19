package dynagent.common.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class IndividualValues {

	private int id;
	private HashMap<Integer,Object> mapData;
	
	public IndividualValues(int id,HashMap<Integer,Object> mapData){
		this.id=id;
		if(mapData!=null)
			this.mapData=mapData;
		else this.mapData=new HashMap<Integer, Object>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashMap<Integer, Object> getMapData() {
		return mapData;
	}

	public void setMapData(HashMap<Integer, Object> mapData) {
		this.mapData = mapData;
	}
	
	public void addData(int idProp,Object value){
		//System.err.println("Value:"+value);
		if(mapData.get(idProp)!=null){
			ArrayList listValues=new ArrayList();
			
			Object currentValue=mapData.get(idProp);
			if(currentValue instanceof ArrayList){
				listValues.addAll((ArrayList)currentValue);
			}else listValues.add(currentValue);
			
			if(value instanceof ArrayList)
				listValues.addAll((ArrayList)value);
			else listValues.add(value);
			
			mapData.put(idProp, listValues);
		}else mapData.put(idProp, value);
		//System.err.println("mapData:"+mapData);
	}
}
