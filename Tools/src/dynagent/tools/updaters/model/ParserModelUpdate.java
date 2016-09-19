package dynagent.tools.updaters.model;


import java.sql.SQLException;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;

public class ParserModelUpdate {
	
	private IKnowledgeBaseInfo ikNewModel;

	public ParserModelUpdate(IKnowledgeBaseInfo ikNewModel) {
		this.ikNewModel = ikNewModel;
	}
	
	public void parserXML(Element xml) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, DataErrorException {
		Iterator it = xml.getChildren().iterator();
		while (it.hasNext()) {
			Element elem = (Element)it.next();
			if (elem.getName().equals(ConstantsXMLModelUpdate.CLASS))
				parserClass(elem);
			else if (elem.getName().equals(ConstantsXMLModelUpdate.PROPERTY))
				parserProperty(elem);
			else if (elem.getName().equals(ConstantsXMLModelUpdate.SEGMENTATION_DATA))
				parserSegmentation(elem);
			else if (elem.getName().equals(ConstantsXMLModelUpdate.ACTION_PROPERTY))
				parserActionProperty(elem);
			else if (elem.getName().equals(ConstantsXMLModelUpdate.DELETE))
				parserDelete(elem);
		}
	}
	
	private void parserClass(Element elemClass) throws SQLException, NamingException {
		String classNew = elemClass.getAttributeValue(ConstantsXMLModelUpdate.CLASS_NEW);
		String classOld = elemClass.getAttributeValue(ConstantsXMLModelUpdate.CLASS_OLD);
		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		System.out.println(classNew);
		Integer idtoNew = tdao.getTClaseByName(classNew).getIDTO();
		elemClass.setAttribute(ConstantsXMLModelUpdate.IDTO_NEW,String.valueOf(idtoNew));
		Integer idtoOld = tdao.getTClaseByName(classOld).getIDTO();
		elemClass.setAttribute(ConstantsXMLModelUpdate.IDTO_OLD,String.valueOf(idtoOld));
		
		PropertiesDAO propDAO = new PropertiesDAO();
		propDAO.open();
		Iterator it = elemClass.getChildren().iterator();
		while (it.hasNext()) {
			Element elemProp = (Element)it.next();
			String propNew = elemProp.getAttributeValue(ConstantsXMLModelUpdate.PROP_NEW);
			String propOld = elemProp.getAttributeValue(ConstantsXMLModelUpdate.PROP_OLD);
			Integer propNewInt = propDAO.getIdPropByName(propNew);
			elemProp.setAttribute(ConstantsXMLModelUpdate.ID_PROP_NEW,String.valueOf(propNewInt));
			Integer propOldInt = propDAO.getIdPropByName(propOld);
			elemProp.setAttribute(ConstantsXMLModelUpdate.ID_PROP_OLD,String.valueOf(propOldInt));
		}
		
		propDAO.close();
		//tdao.close();
	}
	
	private void parserProperty(Element elemProp) throws SQLException, NamingException {
		String clase = elemProp.getAttributeValue(ConstantsXMLModelUpdate.CLASS);
		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		if (clase!=null) {
			Integer idto = tdao.getTClaseByName(clase).getIDTO();
			elemProp.setAttribute(ConstantsXMLModelUpdate.IDTO,String.valueOf(idto));
		}
		String propNew = elemProp.getAttributeValue(ConstantsXMLModelUpdate.PROP_NEW);
		String propOld = elemProp.getAttributeValue(ConstantsXMLModelUpdate.PROP_OLD);
		PropertiesDAO propDAO = new PropertiesDAO();
		propDAO.open();
		Integer propNewInt = propDAO.getIdPropByName(propNew);
		elemProp.setAttribute(ConstantsXMLModelUpdate.ID_PROP_NEW,String.valueOf(propNewInt));
		Integer propOldInt = propDAO.getIdPropByName(propOld);
		elemProp.setAttribute(ConstantsXMLModelUpdate.ID_PROP_OLD,String.valueOf(propOldInt));
		
		propDAO.close();
		//tdao.close();
	}
	
	private void parserDelete(Element elemDelete) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, DataErrorException {
		String clase = elemDelete.getAttributeValue(ConstantsXMLModelUpdate.CLASS);
		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		if (clase!=null) {
			Integer idto = tdao.getTClaseByName(clase).getIDTO();
			elemDelete.setAttribute(ConstantsXMLModelUpdate.IDTO,String.valueOf(idto));
		}
		tdao.close();

	}
	
	private void parserActionProperty(Element elemActionProperty) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, DataErrorException {
		String action = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.ACTION);
		if (action.equals(ConstantsXMLModelUpdate.DEL)) {
			String clase = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.CLASS_SPEC);
			TClaseDAO tdao = new TClaseDAO();
			tdao.open();
			if (clase!=null) {
				Integer idto = tdao.getTClaseByName(clase).getIDTO();
				String idtos = specialized(idto);
				elemActionProperty.setAttribute(ConstantsXMLModelUpdate.IDTO_SPEC,String.valueOf(idtos));
			}
			
			String valueCls = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.VALUE_CLS);
			if (valueCls!=null) {
				Integer valueClsInt = tdao.getTClaseByName(valueCls).getIDTO();
				elemActionProperty.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(valueClsInt));
			}
			String propOfClass = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.PROPERTIES_OF_CLASS);
			if (propOfClass!=null) {
				Integer propOfClassInt = tdao.getTClaseByName(propOfClass).getIDTO();
				elemActionProperty.setAttribute(ConstantsXMLModelUpdate.PROPERTIES_OF_IDTO,String.valueOf(propOfClassInt));
			}
			String prop = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.PROP);
			if (prop!=null) {
				PropertiesDAO propDAO = new PropertiesDAO();
				propDAO.open();
				Integer propInt = propDAO.getIdPropByName(prop);
				elemActionProperty.setAttribute(ConstantsXMLModelUpdate.ID_PROP,String.valueOf(propInt));
			}
			if (clase==null && valueCls==null && prop==null && propOfClass==null) {
				throw new DataErrorException("En los nodos " + ConstantsXMLModelUpdate.ACTION_PROPERTY + " con atributo " + 
						ConstantsXMLModelUpdate.ACTION + " a " + ConstantsXMLModelUpdate.DEL + " debe introcudir " + 
						ConstantsXMLModelUpdate.CLASS_SPEC + ", " + ConstantsXMLModelUpdate.VALUE_CLS + ", " + 
						ConstantsXMLModelUpdate.PROP + " o " + ConstantsXMLModelUpdate.PROPERTIES_OF_CLASS);
			}
			tdao.close();
			//propDAO.close();
		} else if (action.equals(ConstantsXMLModelUpdate.DEFAULT) || action.equals(ConstantsXMLModelUpdate.DUPLICATE_OPROP)) {
			String clase = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.CLASS);
			TClaseDAO tdao = new TClaseDAO();
			tdao.open();
			if (clase!=null) {
				Integer idto = tdao.getTClaseByName(clase).getIDTO();
				elemActionProperty.setAttribute(ConstantsXMLModelUpdate.IDTO,String.valueOf(idto));
			}
			String prop = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.PROP);
			PropertiesDAO propDAO = new PropertiesDAO();
			propDAO.open();
			Integer propInt = propDAO.getIdPropByName(prop);
			elemActionProperty.setAttribute(ConstantsXMLModelUpdate.ID_PROP,String.valueOf(propInt));
			propDAO.close();
			//tdao.close();
		} else if (action.equals(ConstantsXMLModelUpdate.DUPLICATE_OPROP)) {
			String prop = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.PROP);
			PropertiesDAO propDAO = new PropertiesDAO();
			propDAO.open();
			Integer propInt = propDAO.getIdPropByName(prop);
			elemActionProperty.setAttribute(ConstantsXMLModelUpdate.ID_PROP,String.valueOf(propInt));
			propDAO.close();
		}
	}
	
	private String specialized(Integer idto) throws NotFoundException, IncoherenceInMotorException {
		String idtos = String.valueOf(idto);
		Iterator<Integer> iSpec = ikNewModel.getSpecialized(idto).iterator();
		while (iSpec.hasNext()) {
			Integer spec = iSpec.next();
			idtos += "," + spec;
		}
		return idtos;
	}
	
	private void parserSegmentation(Element elemSegmentData) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		String classDest = elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.CLASS_DEST);
		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		Integer idtoDest = tdao.getTClaseByName(classDest).getIDTO();
		elemSegmentData.setAttribute(ConstantsXMLModelUpdate.IDTO_DEST,String.valueOf(idtoDest));

		String classSrc = elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.CLASS_SRC_SPEC);
		if (classSrc!=null) {
			Integer idtoSrc = tdao.getTClaseByName(classSrc).getIDTO();
			String idtosSrc = specialized(idtoSrc);
			elemSegmentData.setAttribute(ConstantsXMLModelUpdate.IDTO_SRC_SPEC,idtosSrc);
		}
		String propNew = elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.PROP_NEW);
		PropertiesDAO propDAO = new PropertiesDAO();
		propDAO.open();
		Integer propNewInt = propDAO.getIdPropByName(propNew);
		elemSegmentData.setAttribute(ConstantsXMLModelUpdate.ID_PROP_NEW,String.valueOf(propNewInt));
		
		Iterator it = elemSegmentData.getChildren().iterator();
		while (it.hasNext()) {
			Element data = (Element)it.next();
			String propNewData = data.getAttributeValue(ConstantsXMLModelUpdate.PROP_NEW);
			Integer propNewDataInt = propDAO.getIdPropByName(propNewData);
			data.setAttribute(ConstantsXMLModelUpdate.ID_PROP_NEW,String.valueOf(propNewDataInt));
			String propOldData = data.getAttributeValue(ConstantsXMLModelUpdate.PROP_OLD);
			Integer propOldDataInt = propDAO.getIdPropByName(propOldData);
			data.setAttribute(ConstantsXMLModelUpdate.ID_PROP_OLD,String.valueOf(propOldDataInt));
			
			String valueCls = data.getAttributeValue(ConstantsXMLModelUpdate.VALUE_CLS);
			if (valueCls!=null) {
				if (valueCls.toLowerCase().equals(Constants.DATA_DOUBLE))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_DOUBLE));
				else if (valueCls.toLowerCase().equals(Constants.DATA_INT))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_INT));
				else if (valueCls.toLowerCase().equals(Constants.DATA_STRING))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_STRING));
				else if (valueCls.toLowerCase().equals(Constants.DATA_MEMO))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_MEMO));
				else if (valueCls.toLowerCase().equals(Constants.DATA_BOOLEAN))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_BOOLEAN));
				else if (valueCls.toLowerCase().equals(Constants.DATA_TIME))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_TIME));
				else if (valueCls.toLowerCase().equals(Constants.DATA_DATETIME))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_DATETIME));
				else if (valueCls.toLowerCase().equals(Constants.DATA_DATE))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_DATE));
				else if (valueCls.toLowerCase().equals(Constants.DATA_UNIT))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_UNIT));
				else if (valueCls.toLowerCase().equals(Constants.DATA_IMAGE))
					data.setAttribute(ConstantsXMLModelUpdate.ID_VALUE_CLS,String.valueOf(Constants.IDTO_IMAGE));
			}
		}
		
		propDAO.close();
		//tdao.close();
	}
	
}
