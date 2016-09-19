package dynagent.gui.forms.utils;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

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
import dynagent.common.properties.Property;
import dynagent.gui.KnowledgeBaseAdapter;

public interface IOrderProperties {
	public void addOrderPropertyList(ArrayList<OrderProperty> orderPropertyList);
	
	public void removeOrderPropertyList(ArrayList<OrderProperty> orderPropertyList);
	
	public void buildOrderForm(int ido, ArrayList<Property> list,Integer idtoUserTask, KnowledgeBaseAdapter kba) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	
	//TODO idGroup no se usa porque en teoria una property pertenecera a un solo grupo,de momento se deja
	public int getOrder(int idProp,Integer idGroup);
	
	public int getPriority(Property property,int idGroup);
}
