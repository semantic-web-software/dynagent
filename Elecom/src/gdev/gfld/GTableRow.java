package gdev.gfld;

import java.util.HashMap;

import dynagent.common.knowledge.instance;
import dynagent.common.utils.GIdRow;

public class GTableRow {

	private HashMap<String,Object> rowMap;
	private HashMap<String, Integer> idoMap;
	private HashMap<String, Integer> idtoMap;
	private HashMap<String, Integer> idoFilterMap;
	private HashMap<String, Integer> idtoFilterMap;
	private HashMap<String, String> idParentMap;
	private GIdRow idRow;
	private boolean nullRow;
	private boolean permanent;
	
	public GTableRow(GIdRow idRow) {
		rowMap=new HashMap<String,Object>();
		idoMap = new HashMap<String, Integer>();
		idtoMap = new HashMap<String, Integer>();
		idParentMap=new HashMap<String, String>();
		idoFilterMap = new HashMap<String, Integer>();
		idtoFilterMap = new HashMap<String, Integer>();
		this.idRow=idRow;
		nullRow=false;
		permanent=false;
	}
	
	public Object getDataColumn(String idColumn){
		return rowMap.get(idColumn);
	}
	
	public void setDataColumn(String idColumn,Object value){
		rowMap.put(idColumn, value);
	}
	
	public GIdRow getIdRow(){
		return idRow;
	}

	public HashMap<String, Integer> getIdoMap() {
		return idoMap;
	}

	public void setIdoMap(String idColumn, Integer idObject){
		idoMap.put(idColumn, idObject);		
	}

	public String getIdParentMap(String idColumn) {
		return idParentMap.get(idColumn);
	}
	
	public HashMap<String, String> getIdParentMap() {
		return idParentMap;
	}

	public void setIdParentMap(String idColumn, String idParent) {
		idParentMap.put(idColumn, idParent);
	}

	public HashMap<String, Integer> getIdtoMap() {
		return idtoMap;
	}

	public void setIdtoMap(String idColumn, Integer idto) {
		this.idtoMap.put(idColumn, idto);
	}

	public boolean isNullRow() {
		return nullRow;
	}

	public void setNullRow(boolean nullRow) {
		this.nullRow = nullRow;
	}

	public HashMap<String, Integer> getIdoFilterMap() {
		return idoFilterMap;
	}

	public void setIdoFilterMap(String idColumn, Integer ido) {
		this.idoFilterMap.put(idColumn, ido);
	}

	public HashMap<String, Integer> getIdtoFilterMap() {
		return idtoFilterMap;
	}

	public void setIdtoFilterMap(String idColumn, Integer idto) {
		this.idtoFilterMap.put(idColumn, idto);
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

}
