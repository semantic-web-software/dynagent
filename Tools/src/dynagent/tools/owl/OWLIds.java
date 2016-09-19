package dynagent.tools.owl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import dynagent.common.Constants;

public class OWLIds {

	private LinkedHashMap<String,Integer> classNameIdMap;
	private LinkedHashMap<String,Integer> propertyNameIdMap;
	private int maximumClassId;
	private int maximumPropertyId;
	private String filesPath;
	
	public OWLIds(String filesPath) throws IOException {
		super();
		classNameIdMap=new LinkedHashMap<String, Integer>();
		propertyNameIdMap=new LinkedHashMap<String, Integer>();
		this.filesPath=filesPath;
	}

	public void saveFiles() throws IOException{
		saveIds("classId.txt",classNameIdMap);
		saveIds("propertyId.txt",propertyNameIdMap);
	}

	private void saveIds(String fileName, LinkedHashMap<String, Integer> nameIdMap) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filesPath+fileName,true));
		for(String name:nameIdMap.keySet()){
			int id=nameIdMap.get(name);
			writer.write(name+"="+id);
			writer.newLine();
		}
		writer.close();
	}
	
	public void readFiles() throws IOException{
		maximumClassId=readIds("classId.txt",classNameIdMap,Constants.MIN_ID_NO_SPECIALCLASS);
		maximumPropertyId=readIds("propertyId.txt",propertyNameIdMap,Constants.MIN_IdPROP_MODEL);
	}

	private Integer readIds(String fileName, HashMap<String, Integer> nameIdMap, Integer maximumId) throws IOException{
		
		File file=new File(filesPath+fileName);
		
		System.err.println("Fichero params:"+file.getAbsolutePath());
		
		if(!file.exists()){
			file.createNewFile();
		}else{
			String line = "";
			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try{
				while((line = reader.readLine()) != null){
					String[] data = line.split("=");
					String name=data[0];
					int id=Integer.valueOf(data[1]);
					if(maximumId!=null){
						maximumId=Math.max(maximumId, id);
					}
					nameIdMap.put(name, id);
				}
			}finally{
				reader.close();
			}
		}
		
		return maximumId;
	}



	public LinkedHashMap<String, Integer> getClassNameIdMap() {
		return classNameIdMap;
	}



	public void setClassNameIdMap(LinkedHashMap<String, Integer> classNameIdMap) {
		this.classNameIdMap = classNameIdMap;
	}



	public LinkedHashMap<String, Integer> getPropertyNameIdMap() {
		return propertyNameIdMap;
	}



	public void setPropertyNameIdMap(LinkedHashMap<String, Integer> propertyNameIdMap) {
		this.propertyNameIdMap = propertyNameIdMap;
	}


	public int getMaximumClassId() {
		return maximumClassId;
	}



	public void setMaximumClassId(int maximumClassId) {
		this.maximumClassId = maximumClassId;
	}



	public int getMaximumPropertyId() {
		return maximumPropertyId;
	}



	public void setMaximumPropertyId(int maximumPropertyId) {
		this.maximumPropertyId = maximumPropertyId;
	}

}
