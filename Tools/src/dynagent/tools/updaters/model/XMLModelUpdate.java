package dynagent.tools.updaters.model;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Index;
import dynagent.common.basicobjects.IndexFilter;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.O_Reg_Instancias_Index;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.jdomParser;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.server.database.DataBaseForRuler;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IndexDAO;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.O_Datos_Attrib_MemoDAO;
import dynagent.server.database.dao.O_Reg_Instancias_IndexDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.IndexFilterFunctions;
import dynagent.server.services.InstanceService;

public class XMLModelUpdate {

	private FactoryConnectionDB fcdb;
	private GenerateSQL gSQL;
	private IKnowledgeBaseInfo ikNewModel;
	private ArrayList<Integer> deleteIdtos;
	private ArrayList<Integer> deleteProps;
	private InstanceService m_IS;
	
	public XMLModelUpdate(FactoryConnectionDB fcdb) {
		this.fcdb = fcdb;
		this.gSQL = new GenerateSQL(fcdb.getGestorDB());
		deleteIdtos = new ArrayList<Integer>();
		deleteProps = new ArrayList<Integer>();
	}
	
	private Element readXML(String path) throws IOException, JDOMException {
		BufferedReader in = new BufferedReader(new FileReader(path)); 
		StringBuffer dataS = new StringBuffer("");
		String buff = "";
		while(buff!= null){
			dataS.append(buff);
			buff = in.readLine();
		}
		return jdomParser.readXML(dataS.toString()).getRootElement();
	}
	
	private void startEngine() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		m_IS = new InstanceService(fcdb, null, false);
		DataBaseMap dataBaseMap = new DataBaseMap(fcdb, false);
		m_IS.setDataBaseMap(dataBaseMap);
		
		ArrayList<String> rulesList=new ArrayList<String>();
		rulesList.add("query.dpkg");
		ikNewModel = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(fcdb, dataBaseMap, fcdb.getBusiness()), fcdb.getBusiness(), 
				m_IS, Constants.RULER, Constants.USER_SYSTEM, rulesList, null, null, null, true);
		m_IS.setIk(ikNewModel);
	}
	
	private void cleanModel() throws SQLException {
		cleanClassesModel();
		cleanPropertiesModel();
	}
	
	private void cleanClassesModel() throws SQLException {
		if (deleteIdtos.size()>0) {
			String idtosOld = Auxiliar.arrayIntegerToString(deleteIdtos, ",");
			InstanceDAO insDao = new InstanceDAO();
			ArrayList<Integer> inInstance = new ArrayList<Integer>();
			LinkedList<Object> llo = insDao.getAllCond("IDTO IN(" + idtosOld + ")");
			Iterator it = llo.iterator();
			while (it.hasNext()) {
				Instance ins = (Instance)it.next();
				//construir array de los que estan en instance
				inInstance.add(Integer.parseInt(ins.getIDTO()));
			}
			String notInInstance = Auxiliar.getStringNewElementsInteger(deleteIdtos, inInstance, ",");
			
			//eliminar los de ese idto q no esten en instance
			//obtener idtos a borrar
			if (notInInstance.length()>0) {
				TClaseDAO tclas = new TClaseDAO();
				tclas.deleteCond("IDTO IN(" + notInInstance + ")");
			}
		}
	}
	
	private void cleanPropertiesModel() throws SQLException {
		if (deleteProps.size()>0) {
			String propertiesOld = Auxiliar.arrayIntegerToString(deleteProps, ",");
			InstanceDAO insDao = new InstanceDAO();
			ArrayList<Integer> inInstance = new ArrayList<Integer>();
			LinkedList<Object> llo = insDao.getAllCond("PROPERTY IN(" + propertiesOld + ")");
			Iterator it = llo.iterator();
			while (it.hasNext()) {
				Instance ins = (Instance)it.next();
				//construir array de los que estan en instance
				inInstance.add(Integer.parseInt(ins.getPROPERTY()));
			}
			String notInInstance = Auxiliar.getStringNewElementsInteger(deleteProps, inInstance, ",");
			
			//eliminar los de ese idto q no esten en instance
			//obtener idtos a borrar
			if (notInInstance.length()>0) {
				PropertiesDAO prop = new PropertiesDAO();
				prop.deleteCond("PROP IN(" + notInInstance + ")");
			}
		}
	}
	/*private void cleanClassModel(int idtoOld) throws SQLException {
		InstanceDAO insDao = new InstanceDAO();
		LinkedList<Object> llo = insDao.getAllCond("IDTO=" + idtoOld);
		Iterator it = llo.iterator();
		if (!it.hasNext()) {
			TClaseDAO tclas = new TClaseDAO();
			tclas.deleteCond("IDTO=" + idtoOld);
		}
	}
	
	private void cleanPropertyModel(int propertyOld) throws SQLException {
		InstanceDAO insDao = new InstanceDAO();
		LinkedList<Object> llo = insDao.getAllCond("PROPERTY=" + propertyOld);
		Iterator it = llo.iterator();
		if (!it.hasNext()) {
			PropertiesDAO prop = new PropertiesDAO();
			prop.deleteCond("PROP=" + propertyOld);
		}
	}*/
	
	
	
	public static void modelUpdate(String pathUpdateModel,FactoryConnectionDB fcdb) {
		XMLModelUpdate parser = new XMLModelUpdate(fcdb);
	
		parser.startUpdate(pathUpdateModel);
	}
	
	public void startUpdate(String path) {
		//DAOManager.getInstance().setCommit(true);
		try {
			System.err.println("---> Actualizando datos conforme al modelo");
			Element xml = readXML(path);
			System.out.println("Antes de parsear " + jdomParser.returnXML(xml));
			startEngine();
			
			DAOManager.getInstance().setCommit(false); //antes de abrir DAO
			ParserModelUpdate parserModelUpdate = new ParserModelUpdate(ikNewModel);
			parserModelUpdate.parserXML(xml);
			System.out.println("Después de parsear " + jdomParser.returnXML(xml));
			
			//DAOManager.getInstance().setCommit(false);
			O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
			O_Reg_Instancias_IndexDAO oreg = new O_Reg_Instancias_IndexDAO();
			O_Datos_Attrib_MemoDAO odatMemoDAO = new O_Datos_Attrib_MemoDAO();
			IndexDAO iDAO = new IndexDAO();
			odatDAO.open();
			//odatDAO.commit();
	
			try {
				Iterator it = xml.getChildren().iterator();
				while (it.hasNext()) {
					Element elem = (Element)it.next();
					if (elem.getName().equals(ConstantsXMLModelUpdate.DELETE)) {
						System.err.println("---> Inicio nodo DELETE");
						System.out.println(jdomParser.returnXML(elem));
						trataDelete(odatDAO, odatMemoDAO, oreg, elem);
						System.err.println("---> Fin nodo DELETE");
					}
				}
				it = xml.getChildren().iterator();
				while (it.hasNext()) {
					Element elem = (Element)it.next();
					if (elem.getName().equals(ConstantsXMLModelUpdate.CLASS)) {
						System.err.println("---> Inicio nodo CLASS");
						System.out.println(jdomParser.returnXML(elem));
						trataClass(odatDAO, oreg, iDAO, elem);
						System.err.println("---> Fin nodo CLASS");
					} else if (elem.getName().equals(ConstantsXMLModelUpdate.PROPERTY)) {
						System.err.println("---> Inicio nodo PROPERTY");
						System.out.println(jdomParser.returnXML(elem));
						trataProperty(odatDAO, elem);
						System.err.println("---> Fin nodo PROPERTY");
					} else if (elem.getName().equals(ConstantsXMLModelUpdate.SEGMENTATION_DATA)) {
						System.err.println("---> Inicio nodo SEGMENTATION_DATA");
						System.out.println(jdomParser.returnXML(elem));
						trataSegmentation(odatDAO, oreg, elem);
						System.err.println("---> Fin nodo SEGMENTATION_DATA");
					} else if (elem.getName().equals(ConstantsXMLModelUpdate.ACTION_PROPERTY)) {
						System.err.println("---> Inicio nodo ACTION_PROPERTY");
						System.out.println(jdomParser.returnXML(elem));
						trataActionProperty(odatDAO, elem);
						System.err.println("---> Fin nodo ACTION_PROPERTY");
					}
				}
				System.err.println("---> Inicio de la limpieza del modelo");
				cleanModel();
				System.err.println("---> Fin de la limpieza del modelo");
				odatDAO.commit();
				//odatDAO.setCommit(true);
				odatDAO.close();
				System.err.println("---> Fin de la actualización");
			} catch (Exception e) {
				System.err.println("ERROR: Actualización con errores. No realizada.");
				e.printStackTrace();
				odatDAO.rollback();
				System.out.println("hace rollback");
				odatDAO.close();
				fcdb.removeConnections();
			}
		} catch (Exception e) {
			System.err.println("ERROR: Actualización con errores. No realizada.");
			e.printStackTrace();
			try {
				fcdb.removeConnections();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

	}
	
	private void trataClass(O_Datos_AttribDAO odatDAO, O_Reg_Instancias_IndexDAO oreg, IndexDAO iDAO, Element elemClass) 
			throws SQLException, NamingException {
		int idtoNew = Integer.parseInt(elemClass.getAttributeValue(ConstantsXMLModelUpdate.IDTO_NEW));
		int idtoOld = Integer.parseInt(elemClass.getAttributeValue(ConstantsXMLModelUpdate.IDTO_OLD));
		
		//update idto de old a new
		odatDAO.update("ID_TO=" + idtoNew, "ID_TO=" + idtoOld);
		//update valueCls de old a new
		odatDAO.update("VALUE_CLS=" + idtoNew, "VALUE_CLS=" + idtoOld);
		
		oreg.update("ID_TO=" + idtoNew, "ID_TO=" + idtoOld);
		
		//cogemos el indice de la otra clase para esta nueva
		//iDAO.update("ID_TO=" + idtoNew, "ID_TO=" + idtoOld);
		
		deleteIdtos.add(idtoOld);
		Iterator it = elemClass.getChildren().iterator();
		while (it.hasNext()) {
			Element elemProp = (Element)it.next();
			int propNewInt = Integer.parseInt(elemProp.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_NEW));
			int propOldInt = Integer.parseInt(elemProp.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_OLD));
			odatDAO.update("PROPERTY=" + propNewInt, "PROPERTY=" + propOldInt + " AND ID_TO=" + idtoNew);
			deleteProps.add(propOldInt);
		}
	}
	
	private void trataProperty(O_Datos_AttribDAO odatDAO, Element elemProp) throws SQLException, NamingException {
		String clase = elemProp.getAttributeValue(ConstantsXMLModelUpdate.IDTO);
		int propNewInt = Integer.parseInt(elemProp.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_NEW));
		int propOldInt = Integer.parseInt(elemProp.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_OLD));
		
		String where = "PROPERTY=" + propOldInt;
		if (clase!=null) {
			int idto = Integer.parseInt(clase);
			where += " AND ID_TO=" + idto;
		}
		odatDAO.update("PROPERTY=" + propNewInt, where);
		deleteProps.add(propOldInt);
	}
	
	private void trataDelete(O_Datos_AttribDAO odatDAO, O_Datos_Attrib_MemoDAO odatMemoDAO, O_Reg_Instancias_IndexDAO oregDAO, Element elemDelete) throws SQLException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NamingException, JDOMException, ParseException {
		String idto = elemDelete.getAttributeValue(ConstantsXMLModelUpdate.IDTO);
		String idosMayoresQue = elemDelete.getAttributeValue(ConstantsXMLModelUpdate.IDOS_MAYORES_QUE);
		if (idto!=null)
			trataDeleteIdto(odatDAO, odatMemoDAO, oregDAO, idto);
		else
			trataDeleteIdos(odatDAO, odatMemoDAO, oregDAO, idosMayoresQue);
	}
	
	private void trataDeleteIdos(O_Datos_AttribDAO odatDAO, O_Datos_Attrib_MemoDAO odatMemoDAO, O_Reg_Instancias_IndexDAO oregDAO, String idosMayoresQue) throws SQLException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NamingException, JDOMException, ParseException {
		oregDAO.deleteCond("ID_O > " + idosMayoresQue);
		odatDAO.deleteCond("ID_O > " + idosMayoresQue + " OR VAL_NUM > " + idosMayoresQue);
		odatMemoDAO.deleteCond("ID_O > " + idosMayoresQue);
		//falta hacer -> DBCC CHECKIDENT ('o_Reg_Instancias_Index', RESEED, idosMayoresQue);
	}
	
	private void trataDeleteIdto(O_Datos_AttribDAO odatDAO, O_Datos_Attrib_MemoDAO odatMemoDAO, O_Reg_Instancias_IndexDAO oregDAO, String idto) throws SQLException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NamingException, JDOMException, ParseException {
		ArrayList<Integer> aIdos = new ArrayList<Integer>();
		LinkedList<Object> lodt = oregDAO.getAllCond("ID_TO="+idto);
		Iterator it2 = lodt.iterator();
		while (it2.hasNext()) {
			O_Reg_Instancias_Index oreg = (O_Reg_Instancias_Index)it2.next();
			aIdos.add(oreg.getId_o());
		}
		System.out.println(Auxiliar.arrayIntegerToString(aIdos, ";"));
		deleteObligated(odatDAO, odatMemoDAO, oregDAO, aIdos);
	}

	public void deleteObligated(O_Datos_AttribDAO odatDAO, O_Datos_Attrib_MemoDAO odatMemoDAO, O_Reg_Instancias_IndexDAO oregDAO, ArrayList<Integer> idos) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, 
			CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceObligated");
		if (idos!=null && idos.size()>0) {
			//si ya no existe ese ido en bd borrar de O_Reg_Instancias
			deleteRowObjects(oregDAO, idos);
			System.out.println("Despues de deleteRow");
			String sIdos = Auxiliar.arrayIntegerToString(idos, ",");
			
			subDelAllFactsInstanceTopObligated(odatDAO, sIdos);
			subDelAllFactsInstanceDownObligated(odatDAO, odatMemoDAO, oregDAO, sIdos);
		}
		//System.out.println("Fin de la funcion subDelAllFactsInstanceObligated");
	}
	private void subDelAllFactsInstanceTopObligated(O_Datos_AttribDAO odatDAO, String sIdos) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, 
			CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceTopObligated");
		odatDAO.deleteCond("VAL_NUM IN(" + sIdos + ")");
		//System.out.println("Fin de la funcion subDelAllFactsInstanceTopObligated");
	}
	private void subDelAllFactsInstanceDownObligated(O_Datos_AttribDAO odatDAO, O_Datos_Attrib_MemoDAO odatMemoDAO, O_Reg_Instancias_IndexDAO oregDAO, String sIdos) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, 
			CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceDownObligated");
		ArrayList<Integer> deleteIdos = new ArrayList<Integer>();
		LinkedList<Object> llo = odatDAO.getAllCond("ID_O IN(" + sIdos + ")");
		Iterator it = llo.iterator();
		while (it.hasNext()) {
			O_Datos_Attrib odt =  (O_Datos_Attrib)it.next();
			Integer idProp = odt.getPROPERTY();
			if (ikNewModel.isObjectProperty(idProp) && ikNewModel.getCategory(idProp).isStructural()) {
				Integer valNum = odt.getVALNUM();
				deleteIdos.add(valNum);
			}
		}
		
		odatDAO.deleteCond("ID_O IN(" + sIdos + ")");
		odatMemoDAO.deleteCond("ID_O IN(" + sIdos + ")");

		deleteObligated(odatDAO, odatMemoDAO, oregDAO, deleteIdos);
		//System.out.println("Fin de la funcion subDelAllFactsInstanceDownObligated");
	}
	private void deleteRowObjects(O_Reg_Instancias_IndexDAO oregDAO, ArrayList<Integer> idos) throws NamingException, SQLException, NotFoundException, 
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, 
			IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException {
		oregDAO.deleteCond("id_o IN(" + Auxiliar.arrayIntegerToString(idos, ",") + ")");
	}
	private void trataActionProperty(O_Datos_AttribDAO odatDAO, Element elemActionProperty) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		String action = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.ACTION);
		if (action.equals(ConstantsXMLModelUpdate.DEL)) {
			String cond = "";
			String idtos = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.IDTO_SPEC);
			if (idtos!=null) {
				cond += "ID_TO IN(" + idtos + ")";
			}
			String idValueCls = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.ID_VALUE_CLS);
			Integer valueClsInt = null;
			if (idValueCls!=null) {
				valueClsInt = Integer.parseInt(idValueCls);
				if (cond.length()>0)
					cond += " AND ";
				cond += "VALUE_CLS=" + valueClsInt;
			}
			
			String idProp = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP);
			Integer propInt = null;
			if (idProp!=null) {
				propInt = Integer.parseInt(idProp);
				if (cond.length()>0)
					cond += " AND ";
				cond += "PROPERTY=" + propInt;
			} else {
				String propsOfClass = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.PROPERTIES_OF_IDTO);
				if (propsOfClass!=null) {
					ArrayList<String> aProps = new ArrayList<String>();
					Integer propsOfIdto = Integer.parseInt(propsOfClass);
					InstanceDAO insDao = new InstanceDAO();
					LinkedList<Object> llo = insDao.getAllCond("IDTO=" + propsOfIdto + " AND PROPERTY<>" + Constants.IdPROP_RDN);
					Iterator it = llo.iterator();
					while (it.hasNext()) {
						Instance ins = (Instance)it.next();
						if (!aProps.contains(ins.getPROPERTY()))
							aProps.add(ins.getPROPERTY());
					}
					if (aProps.size()>0) {
						if (cond.length()>0)
							cond += " AND ";
						cond += "PROPERTY IN(" + Auxiliar.arrayToString(aProps, ",") + ")";
					}
				}
			}
			
			odatDAO.deleteCond(cond);
			if (propInt!=null)
				deleteProps.add(propInt);
			if (idtos!=null)
				deleteIdtos.addAll(Auxiliar.stringToArrayInteger(idtos, ","));
			if (valueClsInt!=null)
				deleteIdtos.add(valueClsInt);
		} else if (action.equals(ConstantsXMLModelUpdate.DEFAULT)) {
			String clase = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.IDTO);
			Integer idto = null;
			if (clase!=null)
				idto = Integer.parseInt(clase);
			String value = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.VALUE);
			int propInt = Integer.parseInt(elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP));
			
			Integer dataType = ikNewModel.getDatatype(propInt);
			//busqueda de todos los de ese idto
			ArrayList<Integer> procesados = new ArrayList<Integer>();
			//crear en los individuos de ID_TO idto en los que no esté la property
			
			//obtener todos los ido que contienen la property a crear
			String noCond = "PROPERTY=" + propInt;
			if (idto!=null)
				noCond += " AND ID_TO=" + idto;
			LinkedList<Object> lodt = odatDAO.getAllCond(noCond);
			ArrayList<Integer> idos = new ArrayList<Integer>();
			Iterator it = lodt.iterator();
			while (it.hasNext()) {
				O_Datos_Attrib odt = (O_Datos_Attrib)it.next();
				if (!idos.contains(odt.getIDO()))
					idos.add(odt.getIDO());
			}
			
			String cond = "";
			if (idto!=null)
				cond = "ID_TO=" + idto;
			if (idos.size()>0) {
				if (cond.length()>0)
					cond += " AND ";
				cond += "ID_O NOT IN(" + Auxiliar.arrayIntegerToString(idos, ",") + ")";
			}
			if (cond.length()>0)
				lodt = odatDAO.getAllCond(cond);
			else
				lodt = odatDAO.getAll();
			it = lodt.iterator();
			while (it.hasNext()) {
				O_Datos_Attrib odt = (O_Datos_Attrib)it.next();
				int idoAct = odt.getIDO();
				if (idto==null)
					idto = odt.getIDTO();
				if (!procesados.contains(idoAct)) {
					procesados.add(idoAct);
					//ver el tipo de property que tengo que insertar
					O_Datos_Attrib in = createODT(idto, idoAct, propInt, value, dataType);
					odatDAO.insert(in);
				}
			}
		} else if (action.equals(ConstantsXMLModelUpdate.DUPLICATE_OPROP)) {
			String nivelsUp = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.NIVELS_UP);
			String nivelsDown = elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.NIVELS_DOWN);
			int propInt = Integer.parseInt(elemActionProperty.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP));
			String cond = "PROPERTY=" + propInt;
			LinkedList<Object> lodt = odatDAO.getAllCond(cond);
			HashMap<String,String> hMap = new HashMap<String, String>();
			Iterator it = lodt.iterator();
			while (it.hasNext()) {
				O_Datos_Attrib odt = (O_Datos_Attrib)it.next();
				if (nivelsUp!=null) {
					int nivels = Integer.parseInt(nivelsUp);
					nivelsUP(odatDAO, odt.getVALNUM(), odt.getVALUECLS(), odt.getIDO(), nivels, hMap);
				} else if (nivelsDown!=null) {
					//TODO por hacer
				}
			}
			if (hMap.size()>0) {
				Iterator it2 = hMap.keySet().iterator();
				while (it2.hasNext()) {
					String idoIdto = (String)it2.next();  //xej, cliente, distribuidor....
					String[] idoIdtoSp = idoIdto.split("#");
					Integer ido = Integer.parseInt(idoIdtoSp[0]);
					Integer idto = Integer.parseInt(idoIdtoSp[1]);
					String idoIdto2 = hMap.get(idoIdto); //xej, provincia
					String[] idoIdtoSp2 = idoIdto2.split("#");
					Integer ido2 = Integer.parseInt(idoIdtoSp2[0]);
					Integer idto2 = Integer.parseInt(idoIdtoSp2[1]);
					O_Datos_Attrib in = new O_Datos_Attrib(idto, ido, propInt, ido2, null, idto2, null, null, null, null);
					//System.out.println(in);
					odatDAO.insert(in);
				}
			}
		}
	}
	private void nivelsUP(O_Datos_AttribDAO odatDAO, int idoDown, int idtoDown, int ido, int nivels, HashMap<String,String> hMap) 
			throws SQLException {
		String cond2 = "VAL_NUM=" + ido;
		LinkedList<Object> lodt2 = odatDAO.getAllCond(cond2);
		Iterator it2 = lodt2.iterator();
		while (it2.hasNext()) {
			O_Datos_Attrib odt2 = (O_Datos_Attrib)it2.next();
			int idoSup = odt2.getIDO();
			int idtoSup = odt2.getIDTO();
			if (nivels>1)
				nivelsUP(odatDAO, idoDown, idtoDown, idoSup, nivels-1, hMap);
			else
				hMap.put(idoSup+"#"+idtoSup, idoDown+"#"+idtoDown);
		}
	}
	
	private O_Datos_Attrib createODT(Integer idto, Integer ido, Integer prop, String value, Integer dataType) {
		//new O_Datos_Attrib(idto,idoAct,propInt,null,value,Constants.IDTO_STRING,null,null,null);
		O_Datos_Attrib odt = null;
		
		//TODO El memo habria que tratarlo diferente ya que va en O_Datos_Attrib_memo!!!!!
		if (dataType==Constants.IDTO_STRING /*|| dataType==Constants.IDTO_MEMO*/ || dataType==Constants.IDTO_IMAGE) {
			odt = new O_Datos_Attrib(idto,ido,prop,null,value,dataType,null,null, null, null);
		} else if (dataType==Constants.IDTO_BOOLEAN) {
			if (value.toLowerCase().equals("true"))
				odt = new O_Datos_Attrib(idto,ido,prop,null,null,dataType,Double.parseDouble("1"),Double.parseDouble("1"), null, null);
			else
				odt = new O_Datos_Attrib(idto,ido,prop,null,null,dataType,Double.parseDouble("0"),Double.parseDouble("0"), null, null);
		} else if (dataType==Constants.IDTO_INT ||
				dataType==Constants.IDTO_DOUBLE ||
				dataType==Constants.IDTO_DATE ||
				dataType==Constants.IDTO_DATETIME ||
				dataType==Constants.IDTO_TIME) {
			odt = new O_Datos_Attrib(idto,ido,prop,null,null,dataType,Double.parseDouble(value),Double.parseDouble(value), null, null);
		}
		return odt;
	}
	
	private void trataSegmentation(O_Datos_AttribDAO odatDAO, O_Reg_Instancias_IndexDAO oreg, Element elemSegmentData) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, InstanceLockedException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		int idtoDest = Integer.parseInt(elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.IDTO_DEST));
		String idtosSrc = elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.IDTO_SRC_SPEC);
		int propNewInt = Integer.parseInt(elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_NEW));
		//if (idtosSrc!=null)
		//deleteIdtos.addAll(Auxiliar.stringToArrayInteger(idtosSrc, ","));

		Integer propOldDataRdnInt = null;
		Iterator it = elemSegmentData.getChildren().iterator();
		while (it.hasNext()) {
			Element data = (Element)it.next();
			int propNewDataInt = Integer.parseInt(data.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_NEW));
			if (propNewDataInt==Constants.IdPROP_RDN)
				propOldDataRdnInt = Integer.parseInt(data.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_OLD));
		}
		
		HashMap<Integer,Integer> idoSrcDest = new HashMap<Integer,Integer>();
		it = elemSegmentData.getChildren().iterator();
		while (it.hasNext()) {
			Element data = (Element)it.next();
			System.err.println("---> Inicio nodo DATA");
			System.out.println(jdomParser.returnXML(data));
			int propNewDataInt = Integer.parseInt(data.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_NEW));
			int propOldDataInt = Integer.parseInt(data.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_OLD));
			deleteProps.add(propOldDataInt);
			
			//construir la condicion
			String cond = "PROPERTY=" + propOldDataInt;
			if (idtosSrc!=null)
				cond += " AND ID_TO IN(" + idtosSrc + ")";
			LinkedList<Object> lodt = odatDAO.getAllCond(cond);
			Iterator it2 = lodt.iterator();
			while (it2.hasNext()) {
				O_Datos_Attrib odt = (O_Datos_Attrib)it2.next();
				Integer idoSrc = odt.getIDO();
				Integer idoDest = idoSrcDest.get(idoSrc);
				Integer idtoSrc = odt.getIDTO();
				if (idoDest==null) {
					//crear un nuevo ido
					idoDest = insertRowObject(odatDAO, oreg, idtoDest, idoSrc, elemSegmentData, propOldDataRdnInt);
					if (idoDest!=null) {  //si es nulo es porque al individuo no se le puede crear el rdn
						//ponerlo en el mapa
						idoSrcDest.put(idoSrc, idoDest);
						//crear enlace
						O_Datos_Attrib in = new O_Datos_Attrib(idtoSrc,idoSrc,propNewInt,idoDest,null,idtoDest,null,null, null, null);
						odatDAO.insert(in);
					}
				}
				
				if (idoDest!=null) {  //si es nulo es porque al individuo no se le puede crear el rdn
					//actualizar individuos que no sean el enlace
					String set = "ID_O=" + idoDest + ",ID_TO=" + idtoDest + ",PROPERTY=" + propNewDataInt;
					
					String idValueCls = data.getAttributeValue(ConstantsXMLModelUpdate.ID_VALUE_CLS);
					if (idValueCls!=null)
						set += ",VALUE_CLS=" + idValueCls;
					odatDAO.update(set, 
							"ID_O=" + idoSrc + " AND ID_TO=" + idtoSrc + " AND PROPERTY=" + propOldDataInt + " AND (VAL_NUM is null or VAL_NUM<>" + idoDest + ")");
				} else //si no tiene rdn y no se puede crear el individuo para mover la property -> se elimina del sitio original
					odatDAO.deleteCond("ID_O=" + idoSrc + " AND ID_TO=" + idtoSrc + " AND PROPERTY=" + propOldDataInt);
			}
			System.err.println("---> Fin nodo DATA");
		}
	}
	
	private Integer insertRowObject(O_Datos_AttribDAO odatDAO, O_Reg_Instancias_IndexDAO oreg, int id_to, int idoSrc, 
			Element elemSegmentData, Integer propOldDataRdnInt) 
			throws NamingException, SQLException, SystemException, NotFoundException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		Integer idoNewIndiv = null;
		String rdn = processIndex(odatDAO, id_to, idoSrc, elemSegmentData, propOldDataRdnInt==null);
		if (rdn==null) {
			if (propOldDataRdnInt==null) {
				//no hacer nada, mostrar un WARNING:
				System.err.println("WARNING: No es posible crear el rdn del nuevo individuo porque no tiene índice asignado para la clase " + elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.CLASS_DEST)
						+ " ni un rdn indicado o con valor");
				//throw new DataErrorException("No es posible crear el rdn del nuevo individuo porque no tiene índice asignado para la clase " + elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.CLASS_DEST));
			} else {
				//se busca por los antiguos valores porque aún no se ha actualizado
				LinkedList<Object> llo = odatDAO.getAllCond("PROPERTY=" + propOldDataRdnInt + " AND ID_O=" + idoSrc);
				if (llo!=null && llo.size()>0) {
					String newRdn = ((O_Datos_Attrib)llo.getFirst()).getVALTEXTO();
					//buscar el valor que tiene el rdn en odt y actualizar oreg
					if (newRdn!=null) {
						idoNewIndiv = IndividualCreator.subInsertRowObject(fcdb, DAOManager.getInstance().isCommit(), id_to);
						oreg.update("rdn='" + newRdn.replaceAll("'", "''") + "', id_o=" + idoNewIndiv, "autonum=" + idoNewIndiv);
					} else
						throw new DataErrorException("El dato está incorrecto en base de datos o no es de tipo String para la property " + propOldDataRdnInt + " en el individuo " + idoSrc);
				} else
					System.err.println("WARNING: No es posible asignar un valor al rdn porque no existe valor para la property " + propOldDataRdnInt + " en el individuo " + idoSrc + ". El individuo no se va a crear");
			}
		} else {
			if (propOldDataRdnInt!=null)
				throw new DataErrorException("No es posible asignar un valor al rdn, cuando éste ya tiene un índice asignado para la clase " + elemSegmentData.getAttributeValue(ConstantsXMLModelUpdate.CLASS_DEST));
			else {
				idoNewIndiv = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, id_to, rdn, null, null, null,false,null).getIdo();
			}
		}
		return idoNewIndiv;
	}

	private boolean getIndexDBDAO(ArrayList<IndexFilter> aIndexF, int idto) throws SQLException, NamingException {
		boolean hasPropIndexFilter = false;
		IndexDAO sind = new IndexDAO();
		LinkedList<Object> llo = sind.getAllCond("ID_TO=" + idto);
		Iterator it = llo.iterator();
		while (it.hasNext()) {
			Index ind = (Index)it.next();
			int ido = ind.getIdo();
			int index = ind.getIndex();
			String prefix = ind.getPrefix();
			String sufix = ind.getPrefix();
			Integer propPrefix = ind.getPropPrefix();
			Integer propFilter = ind.getPropFilter();
			if (propFilter!=null)
				hasPropIndexFilter = true;
			String valueFilter = ind.getValueFilter();
			boolean globalSufix = ind.isGlobalSufix();
			
			Integer propPrefixTemp = ind.getPropPrefixTemp();
			Integer contYear = ind.getContYear();
			String mascPrefixTemp = ind.getMascPrefixTemp();
			String lastPrefixTemp = ind.getLastPrefixTemp();
			Integer minDigits = ind.getMinDigits();
			Integer miEmpresa = ind.getMiEmpresa();

			//if (valueFilter!=null)
				//valueFilter = parserValueFilterDAO(valueFilter);
			IndexFilterFunctions.createIndex(aIndexF, ido, index, prefix, sufix, propPrefix, propFilter, valueFilter, globalSufix, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, minDigits, miEmpresa);
		}
		return hasPropIndexFilter;
	}
	
	/*private String parserValueFilterDAO(String valueFilter) throws SQLException, NamingException {
		if (!Auxiliar.hasIntValue(valueFilter)) {
			O_Reg_InstanciasDAO sind = new O_Reg_InstanciasDAO();
			LinkedList<Object> llo = sind.getAllCond("RDN='" + valueFilter + "'");
			Iterator it = llo.iterator();
			while (it.hasNext()) {
				O_Reg_Instancias_Index ind = (O_Reg_Instancias_Index)it.next();
				valueFilter = String.valueOf(ind.getId_to());
			}
		}
		return valueFilter;
	}*/
	
	private ArrayList<IndexFilter> getPropIndexFilter(O_Datos_AttribDAO odatDAO, int idoSrc, int idto, Element elemSegmentData) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException  {
		ArrayList<IndexFilter> aIndexF = new ArrayList<IndexFilter>();
		boolean hasPropIndexFilter = getIndexDBDAO(aIndexF, idto);
		if (aIndexF.size()>0) {
			//ahora recorremos aipd para buscar las prop del mapa que estan
			if (hasPropIndexFilter) {
				Iterator it = elemSegmentData.getChildren().iterator();
				while (it.hasNext()) {
					Element child = (Element)it.next();
					//el valor que se le pone depende d las prop q tenga ese ido
					Iterator it3 = aIndexF.iterator();
					while (it3.hasNext()) {
						IndexFilter indexF = (IndexFilter)it3.next();
						Integer propFilter = indexF.getPropFilter();
						if (propFilter!=null) {
							String valueF = indexF.getValueFilter();
							int propOldDataInt = Integer.parseInt(child.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_OLD));
							if (propFilter.equals(propOldDataInt)) {
								//mirar que valores tiene en base de datos para esa prop y para idoSrc, si alguno coincide con valueF
								if (coincideValue(odatDAO, idoSrc, propFilter, valueF)) {
								//if (ipd.getVALUE()!=null && ipd.getVALUE().equals(valueF) || 
									//		Auxiliar.hasDoubleValue(valueF) && (ipd.getQMIN()!=null && ipd.getQMIN().equals(Double.parseDouble(valueF)) || 
									//						ipd.getQMAX()!=null && ipd.getQMAX().equals(Double.parseDouble(valueF)))) {
									indexF.setIs(true);
								}
							}
						}
					}
				}
			}
		}
		return aIndexF;
	}
	
	//no se pueden insertar otras properties indexadas si despues puede que no se cree el individuo xq no m llegue el rdn
	private String processIndex(O_Datos_AttribDAO odatDAO, int idto, int idoSrc, Element elemSegmentData, boolean isRdn) throws SQLException, NamingException, InstanceLockedException, NotFoundException, DataErrorException, IncoherenceInMotorException  {
		String changeRdn = null;
		//con comprobacion de que sea una de las properties movidas
		ArrayList<IndexFilter> aIndexF = getPropIndexFilter(odatDAO, idoSrc, idto, elemSegmentData);
		//se le asignara el índice y prefijo de la propertyFilter que esté en propIndexFilter, si no hay ninguna el del más genérico si existe
		ArrayList<O_Datos_Attrib> aodat = new ArrayList<O_Datos_Attrib>();
		boolean modify = isRdn || aIndexF.size()>0;
		if (modify) {
			HashMap<Integer,StringBuffer> valuesFilter = new HashMap<Integer,StringBuffer>();
			IndexFilter indexFilterUpdate = IndexFilterFunctions.createIndexFilter(aIndexF, valuesFilter);
			if (indexFilterUpdate!=null) {
				//if (index!=null) {
					String change = "";
					String prefix = indexFilterUpdate.getPrefix();
					String sufix = indexFilterUpdate.getSufix();
					int index = indexFilterUpdate.getIndex();
					if (prefix==null) {
						Integer propPrefix = indexFilterUpdate.getPropPrefix();
						if (propPrefix==null)
							change = String.valueOf(index);
						else {
							//buscar el valor de la property en los facts, en bd no esta porque es un nuevo individuo
							//IPropertyDef ipd = findFactInstanceByProp(oldIdo, propPrefix, aipd);
							//este metodo deberia ir x idos
							String value = findValueByProp(odatDAO, idoSrc, propPrefix, elemSegmentData); //para obtener el valor hacer query a bd x cada propPrefix que este en elemSegmentData
							if (value!=null)
								change = value+index;
							else
								change = String.valueOf(index);
						}
					} else
						change = prefix+index;
					
					if (sufix!=null)
						change = change+sufix;
					//puede que index sea nulo si en la tabla hay 2 índices con PropertyFilter y ninguna de las 2 esté en el aipd 
					//para el individuo actual
					//nunca va a estar en el xml xq el rdn no se va a mover de una clase a otra
					//if (keyProp==Constants.IdPROP_RDN)
						changeRdn = change;
					O_Datos_Attrib in = new O_Datos_Attrib(idto,idoSrc,Constants.IdPROP_RDN,null,change,Constants.IDTO_STRING,null,null, null, null);
					aodat.add(in);
					//ahora incremento de indice
					IndividualCreator.incrementIndex(fcdb, DAOManager.getInstance().isCommit(), indexFilterUpdate.getIdo(), gSQL);
					//IndexFilter.incrementIndex(indexFilterUpdate, keyProp, idto, true, fcdb);
				//}
			} else {
				//"Debe introducir un valor para " + " correspondido entre "
												   //" igual a "
				//ESTO NO CREO QUE HAGA FALTA//"o un valor para " + " correspondido entre "
				//ESTO NO CREO QUE HAGA FALTA////"para generar"
				Iterator it2 = valuesFilter.keySet().iterator();
				if (it2.hasNext()) {
					Integer propF = (Integer)it2.next();
					String values = valuesFilter.get(propF).toString();
					String error = "";
					if (values.split(",").length>1) {
						error = "Debe introducir uno de los siguientes valores " + values + " para " + ikNewModel.getPropertyName(propF);
					} else
						error = "Debe introducir el siguiente valor " + values + " para " + ikNewModel.getPropertyName(propF);
					throw new DataErrorException(DataErrorException.ERROR_DATA,error);
				}
			}
		}
		return changeRdn;
	}
	
	private boolean coincideValue(O_Datos_AttribDAO odatDAO, int idoSrc, int propFilter, String valueF) throws NotFoundException, SQLException, NamingException, IncoherenceInMotorException  {
		boolean coincide = false;
		LinkedList<Object> llo = new LinkedList<Object>();
		if (ikNewModel.getDatatype(propFilter).equals(Constants.IDTO_STRING) || 
				ikNewModel.getDatatype(propFilter).equals(Constants.IDTO_MEMO) || ikNewModel.getDatatype(propFilter).equals(Constants.IDTO_IMAGE))
			llo = odatDAO.getAllCond("ID_O=" + idoSrc + " AND PROPERTY=" + propFilter + " AND VAL_TEXTO=" + valueF);
		else
			llo = odatDAO.getAllCond("ID_O=" + idoSrc + " AND PROPERTY=" + propFilter + " AND Q_MIN=" + valueF);
		if (llo.size()>0)
			coincide = true;
		return coincide;
	}
	
	private String findValueByProp(O_Datos_AttribDAO odatDAO, int idoSrc, int propPrefix, Element elemSegmentData) throws SQLException, NamingException {
		String value = null;
		Iterator it = elemSegmentData.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			int propOld = Integer.parseInt(child.getAttributeValue(ConstantsXMLModelUpdate.ID_PROP_OLD));
			if (propOld==propPrefix) {
				//buscar en bd que valor tiene
				LinkedList<Object> llo = odatDAO.getAllCond("ID_O=" + idoSrc + " AND PROPERTY=" + propPrefix);
				Iterator it2 = llo.iterator();
				if (it2.hasNext()) {
					O_Datos_Attrib odt = (O_Datos_Attrib)it2.next();
					value = odt.getVALTEXTO();
				}
				break;
			}
		}
		return value;
	}
}
