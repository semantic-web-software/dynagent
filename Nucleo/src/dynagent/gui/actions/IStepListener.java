package dynagent.gui.actions;

import gdev.gen.AssignValueException;

import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;

import org.jdom.JDOMException;

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
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.forms.utils.ActionException;

public interface IStepListener{

	/**
     * Función por la cual un formulario se entera de un evento externo de otro formulario, para que
     * éste haga lo adecuado con los datos, ya sea almacenarlo, cambiar values de properties,....
     * </br>
     * Ejemplo: filterControl informa a transitionControl de un individuo seleccionado en una
     * busqueda para que transitionControl se lo asigne a una ObjectProperty del individuo que
     * se esta creando.
     * 
     * Se trataria del resultado obtenido en el paso actual.
     * 
     * @param values Array con los valores que se transfieren.
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ActionException 
	 * @throws AssignValueException 
	 * @throws NumberFormatException 
     */
	public boolean setResultStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException, NumberFormatException, AssignValueException;
	
	public boolean setCancelStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException;

	public void endSteps() throws NotFoundException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException;
	
	public boolean isLastStep();
	
	public boolean isMultiStep();
}
