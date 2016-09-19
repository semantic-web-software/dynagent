package dynagent.gui.forms.utils;

import java.util.ArrayList;
import java.util.HashMap;

import dynagent.common.exceptions.NotFoundException;
import dynagent.common.properties.Property;
import dynagent.gui.KnowledgeBaseAdapter;

public interface IGroupProperties {

	public Integer getIdGroup(int idProp,Integer idto,Integer idtoUserTask) throws NotFoundException;
	
	public String getNameGroup(int idGroup);

	public int getOrderGroup(int idGroup);
	
	public void buildOrderForm(ArrayList<Property> list,Integer idtoUserTask,KnowledgeBaseAdapter kba) throws NotFoundException;
}
