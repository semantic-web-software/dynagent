package dynagent.common.sessions;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.Utils;

public class EmailRequest {

	private int ido;
	private int idto;
	private Integer idtoReport;
	private String email;
	private String subject;
	private String body;
	private int idoMiEmpresa;
	private int idoDestinatario;
	private IKnowledgeBaseInfo ik;
	
	public EmailRequest(int idoNotification, int idoTarget, int idtoTarget, String notificationType, IKnowledgeBaseInfo ik) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		super();
		this.ido=idoTarget;
		this.idto=idtoTarget;
		
		int idtoNotification=ik.getClassOf(idoNotification);
		
		ObjectProperty propertyEvent=(ObjectProperty)ik.getProperty(idoNotification, idtoNotification, ik.getIdProperty(Constants.PROP_EVENT), null, ik.getUser(), null, ik.getDefaultSession());
		boolean found=false;
		Iterator<Value> itrEvent=propertyEvent.getValues().iterator();
		ObjectValue ov = null;
		while(!found && itrEvent.hasNext()){
			ov=(ObjectValue)itrEvent.next();
			ObjectProperty propertyEventType=(ObjectProperty)ik.getProperty(ov.getIDOIndividual(), ov.getIDTOIndividual(), ik.getIdProperty(Constants.PROP_EVENT_TYPE), null, ik.getUser(), null, ik.getDefaultSession());
			ObjectValue ovEventType=(ObjectValue)propertyEventType.getUniqueValue();
			String rdn = ik.getRdnIfExistInRuler(ovEventType.getIDOIndividual());
			found=rdn.equalsIgnoreCase(notificationType);
		}
		
		if(found){
			Property propertyMessage=ik.getProperty(ov.getIDOIndividual(), ov.getIDTOIndividual(), ik.getIdProperty(Constants.PROP_MESSAGE), null, ik.getUser(), null, ik.getDefaultSession());
			this.body=propertyMessage.getUniqueValue()!=null?propertyMessage.getUniqueValue().getValue_s():"";
			
			Property propertySubject=ik.getProperty(ov.getIDOIndividual(), ov.getIDTOIndividual(), ik.getIdProperty(Constants.PROP_SUBJECT), null, ik.getUser(), null, ik.getDefaultSession());
			this.subject=propertySubject.getUniqueValue()!=null?propertySubject.getUniqueValue().getValue_s():"";
			
			ObjectProperty propertyReport=(ObjectProperty)ik.getProperty(idoNotification, idtoNotification, ik.getIdProperty(Constants.PROP_REPORT), null, ik.getUser(), null, ik.getDefaultSession());
			ObjectValue ovReport=(ObjectValue)propertyReport.getUniqueValue();
			if(ovReport!=null){
				String rdn = ik.getRdnIfExistInRuler(ovReport.getIDOIndividual());
				if(!rdn.contains("rp@")){
					rdn="rp@"+rdn;
			 	}
			 	this.idtoReport=ik.getIdClass(rdn);
			}else{
				this.idtoReport=null;
			}
			
			ObjectProperty propertyMiEmpresa=null;
			if(ik.hasProperty(idtoTarget, ik.getIdProperty(Constants.PROP_MI_EMPRESA))){
				propertyMiEmpresa=(ObjectProperty)ik.getProperty(idoTarget, idtoTarget, ik.getIdProperty(Constants.PROP_MI_EMPRESA), null, ik.getUser(), null, ik.getDefaultSession());
			}
			
			if(propertyMiEmpresa==null || propertyMiEmpresa.getUniqueValue()==null){
				//Si no tiene la propiedad mi_empresa o no tiene valor, la cogemos desde aplicacion
				int applicationIdo=ik.getIndividualsOfLevel(Constants.IDTO_APPLICATION, Constants.LEVEL_INDIVIDUAL).iterator().next();
				int applicationIdto=Constants.IDTO_APPLICATION;
				propertyMiEmpresa=(ObjectProperty)ik.getProperty(applicationIdo, applicationIdto, ik.getIdProperty(Constants.PROP_MI_EMPRESA), null, ik.getUser(), null, ik.getDefaultSession());
			}
			this.idoMiEmpresa=((ObjectValue)propertyMiEmpresa.getUniqueValue()).getIDOIndividual();
						
			int idoEmailNotification=ido;
			int idtoEmailNotification=idto;
			ObjectProperty propertyPropertyToEmailNotification=(ObjectProperty)ik.getProperty(idoNotification, idtoNotification, ik.getIdProperty(Constants.PROP_PROPERTY_TO_NOTIFICATION_EMAIL), null, ik.getUser(), null, ik.getDefaultSession());
			ObjectValue ovPropertyToEmailNotification=(ObjectValue)propertyPropertyToEmailNotification.getUniqueValue();
			if(ovPropertyToEmailNotification!=null){
				String rdnProperty = ik.getRdnIfExistInRuler(ovPropertyToEmailNotification.getIDOIndividual());
				ObjectProperty propertyToEmailnotification=(ObjectProperty)ik.getProperty(idoTarget, idtoTarget, ik.getIdProperty(rdnProperty), null, ik.getUser(), null, ik.getDefaultSession());
				ObjectValue ovEmailNotification=(ObjectValue)propertyToEmailnotification.getUniqueValue();
				idoEmailNotification=ovEmailNotification.getIDOIndividual();
				idtoEmailNotification=ovEmailNotification.getIDTOIndividual();
			}
			
			Property propertyNotificationEmail=ik.getProperty(idoEmailNotification, idtoEmailNotification, ik.getIdProperty(Constants.PROP_NOTIFICATION_EMAIL), null, ik.getUser(), null, ik.getDefaultSession());
			this.email=propertyNotificationEmail.getUniqueValue()!=null?propertyNotificationEmail.getUniqueValue().getValue_s():"";
			this.idoDestinatario=idoEmailNotification;
			
			//Para enviar copia al email indicado en MI_EMPRESA
			Property propertyNotificationEmailOfMiEmpresa=ik.getProperty(idoMiEmpresa, ik.getClassOf(idoMiEmpresa), ik.getIdProperty(Constants.PROP_NOTIFICATION_EMAIL), null, ik.getUser(), null, ik.getDefaultSession());
			String miEmpresaEmail=propertyNotificationEmailOfMiEmpresa.getUniqueValue()!=null?propertyNotificationEmailOfMiEmpresa.getUniqueValue().getValue_s():null;
			if(miEmpresaEmail!=null){
				if(!this.email.isEmpty()){
					this.email+=";"+miEmpresaEmail;
				}else{
					this.email=miEmpresaEmail;
				}
			}

		}else{
			throw new NotFoundException("ERROR: El tipo de notificación "+notificationType+" no existe para la notificación "+ik.getRdnIfExistInRuler(idoNotification));
		}
		
		this.ik=ik;
		
	}

	public EmailRequest(int ido, int idto, Integer idtoReport, String email, String subject, String body, int idoMiEmpresa, int idoDestinatario, IKnowledgeBaseInfo ik) {
		super();
		this.ido = ido;
		this.idto = idto;
		this.idtoReport = idtoReport;
		this.email = email;
		this.subject = subject;
		this.body = body;
		this.idoMiEmpresa = idoMiEmpresa;
		this.idoDestinatario=idoDestinatario;
		
		this.ik=ik;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public int getIdoMiEmpresa() {
		return idoMiEmpresa;
	}
	public void setIdoMiEmpresa(int idoMiEmpresa) {
		this.idoMiEmpresa = idoMiEmpresa;
	}
	public int getIdo() {
		return ido;
	}
	public void setIdo(int ido) {
		this.ido = ido;
	}
	public int getIdto() {
		return idto;
	}
	public void setIdto(int idto) {
		this.idto = idto;
	}
	public Integer getIdtoReport() {
		return idtoReport;
	}
	public void setIdtoReport(Integer idtoReport) {
		this.idtoReport = idtoReport;
	}

	public void setIdoDestinatario(int idoDestinatario) {
		this.idoDestinatario = idoDestinatario;
	}

	public int getIdoDestinatario() {
		return idoDestinatario;
	}
	
	/**
	  * Reemplazo el codigo que aparezca dentro de &<nombre_property>& en mensaje. Reemplazo propiedades pudiendo navegar a traves de properties mediante &property.property&.
	  * Ejemplos: &rdn&, &cliente.rdn&, &cliente.localidad.rdn&. Ademas se puede usar &class& para obtener el nombre de la clase. 
	  * @param ido
	  * @param idto
	  * @throws SystemException
	  * @throws RemoteSystemException
	  * @throws CommunicationException
	  * @throws InstanceLockedException
	  * @throws ApplicationException
	  * @throws IncoherenceInMotorException
	  * @throws IncompatibleValueException
	  * @throws CardinalityExceedException
	  * @throws OperationNotPermitedException
	  * @throws NotFoundException
	  * @throws DataErrorException
	  * @throws ParseException
	  * @throws SQLException
	  * @throws NamingException
	  * @throws JDOMException
	  */
	public void replaceBodyCode(HashMap<Integer,HashMap<Integer,Value>> propertiesChanged) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		boolean exit=false;
		 while(!exit && body.contains("#{")){	
		 	int index=body.indexOf("#{");
		 	int nextIndex=body.indexOf("}",index+2);
		 	if(nextIndex!=-1){
		 		String name=body.substring(index+2, nextIndex);
		 		Integer idoIndividual=ido;
		 		Integer idtoIndividual=idto;
		 		String nameProp=name;
		 		
		 		while(nameProp!=null && nameProp.contains(".")){	
		 			int indexProp=nameProp.indexOf(".");
		 			String nameIndexProp=nameProp.substring(0, indexProp);
		 			Integer idProp=ik.getIdProperty(nameIndexProp);
		 			if(idProp!=null && ik.hasProperty(idtoIndividual, idProp)){
		 				Property property=ik.getProperty(idoIndividual, idtoIndividual, idProp, null, ik.getUser(), null, ik.getDefaultSession());
		 				Value value=property.getUniqueValue();
		 				nameProp=nameProp.replaceAll(nameIndexProp+".", "");
			 			if(value instanceof ObjectValue){
			 				ObjectValue ovalue=(ObjectValue)value;
			 				idoIndividual=ovalue.getIDOIndividual();
			 				idtoIndividual=ovalue.getIDTOIndividual();
			 			}else{//Si se trata de una DataProperty seguida de un . es un error
			 				nameProp=null;
			 			}
		 			}else{//Si no existe la property para esa clase es un error
		 				nameProp=null;
		 			}
		 		}
		 		
		 		Integer idProp=ik.getIdProperty(nameProp);
		 		if(idProp!=null && ik.hasProperty(idtoIndividual, idProp)){
		 			Value val=null;
		 			if(propertiesChanged.containsKey(idoIndividual) && propertiesChanged.get(idoIndividual).containsKey(idProp)){
		 				val=propertiesChanged.get(idoIndividual).get(idProp);
		 			}else{
		 				Property property=ik.getProperty(idoIndividual, idtoIndividual, idProp, null, ik.getUser(), null, ik.getDefaultSession());
		 				val=property.getUniqueValue();
		 			}
		 			
		 			String value=val.getValue_s();
		 			if(val instanceof TimeValue){
		 				value=DateFormatUtils.format(Long.valueOf(value)*Constants.TIMEMILLIS, "dd-MM-yy HH:mm:ss");
			 		}
		 					 			
		 			body=body.replaceAll("\\#\\{"+name+"\\}", value);
		 		}else if(Auxiliar.equals(nameProp,"class")){
		 			String value=ik.getClassName(idtoIndividual);
		 			body=body.replaceAll("\\#\\{"+name+"\\}", Utils.normalizeLabel(value));
		 		}else{
		 			body=body.replaceAll("\\#\\{"+name+"\\}", "<Indefinido>");
		 		}
		 		
		 	}else{
		 		exit=true;
		 	}
		 }
	}
	
}
