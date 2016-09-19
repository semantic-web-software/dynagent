package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.Properties;
import dynagent.common.communication.IndividualData;
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
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.dao.CardMedDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importCardMed extends ObjectConfig{
	
	LinkedList<CardMed> listcardMed=new LinkedList<CardMed>();
	public importCardMed(Element cardmedXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(cardmedXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listcardMed=new LinkedList<CardMed>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteCardMed();
		return extractAllCardMed();
	}
	@Override
	public void importData() throws Exception {
		insertCardMed();
	}
	
	private void deleteCardMed() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_CARDMED_TABLE, fcdb), instanceService.getDataBaseMap(), fcdb);
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_CARDMED_FIELD, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	private void insertCardMed() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<CardMed> itcm=this.listcardMed.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(itcm.hasNext()){
			CardMed in= itcm.next();
			//System.out.println("---> "+cm.toString());

			Integer idto = tdao.getTClaseByName(in.getIdtoName()!=null?Constants.CLS_CARDMED_TABLE:Constants.CLS_CARDMED_FIELD).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			
			String rdn=String.valueOf("&"+ido+"&");
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			// insertar el resto de las DataProperties
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			int idtoProperty = tdao.getTClaseByName(Constants.CLS_DATA_PROPERTY).getIDTO();
			if(in.getIdtoParentName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getIdtoParentName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			if(in.getIdtoName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_TABLE);
				int idoClass = QueryConstants.getIdo(tdao.getTClaseByName(in.getIdtoName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
			}else{
				int prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
				int idoProperty = QueryConstants.getIdo(propDAO.getPropertyByName(in.getIdPropName()).getTableId(), idtoProperty);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoProperty), idtoProperty, null, null, null, null, null, action.NEW));
			}
			
			int prop = propDAO.getIdPropByName(Constants.PROP_SIZE);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(in.getCardmed()), new Double(in.getCardmed()), null, null, action.NEW));
			
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	private boolean extractAllCardMed() throws Exception {
		Iterator itcm = getChildrenXml().iterator();
		boolean success=true;
		while(itcm.hasNext()){
			Element cmEl = (Element)itcm.next();
			CardMed cm = new CardMed();
			try{
				if (cmEl.getAttribute(ConstantsXML.CLASS)!=null){
					String className=cmEl.getAttributeValue(ConstantsXML.CLASS).toString();
					cm.setIdtoName(className);
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null){
						cm.setIdto(idtoClass);
					}else{
						//throw new ConfigException("Error: la clase "+className+" no existe en el modelo");
						continue;
					}
				}
				if (cmEl.getAttribute(ConstantsXML.PROP)!=null){
					String propName=cmEl.getAttributeValue(ConstantsXML.PROP).toString();
					cm.setIdPropName(propName);
					Integer idProp=Auxiliar.getIdProp(propName, fcdb);
					if (idProp!=null){
						cm.setIdProp(idProp);
					}else{
						//throw new ConfigException("Error: la clase "+className+" no existe en el modelo");
						continue;
					}
				}
				if (cmEl.getAttribute(ConstantsXML.CLASS_PARENT)!=null){
					String className=cmEl.getAttributeValue(ConstantsXML.CLASS_PARENT).toString();
					cm.setIdtoParentName(className);
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null){
						cm.setIdtoParent(idtoClass);
					}else{
						//throw new ConfigException("Error: la clase "+className+" no existe en el modelo");
						continue;
					}
				}
				if (cmEl.getAttribute(ConstantsXML.CARDINALITIES_MED)!=null){
					String cardmed=cmEl.getAttributeValue(ConstantsXML.CARDINALITIES_MED).toString();
					cm.setCardmed(Integer.valueOf(cardmed));
					
				}
				listcardMed.add(cm);
			}/*catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}*/catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}

}
