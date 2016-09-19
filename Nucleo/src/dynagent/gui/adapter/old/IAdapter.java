package dynagent.gui.adapter.old;

import java.util.Map;

import dynagent.common.exceptions.NotFoundException;


public interface IAdapter {
	public int typeConcreteCase(DataCase concreteCase, Integer userRol, String user, Integer usertask) throws NotFoundException;
	public Map<Integer,DataAdapter> adapterCase(int type, DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException; 
}