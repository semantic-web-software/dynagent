package dynagent.tools.importers.configxml;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.IndexName;
import dynagent.common.basicobjects.O_Datos_Attrib;
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
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.IndexFilterFunctions;
import dynagent.server.services.InstanceService;


public class importIndex extends ObjectConfig{
	
	private String sufixIndex;
	private boolean deleteIds;
	private LinkedList<IndexName> listIndex=new LinkedList<IndexName>();
	private LinkedList<Integer> listExcludes=new LinkedList<Integer>();
	private IndexName indexFormatoIndice;
	private HashMap<Integer,Integer> idtoWithIdProp=new HashMap<Integer,Integer>();
	private HashSet<Integer> allIndexClass = new HashSet<Integer>();
	private DataBaseMap dataBaseMap = null;
	
	public importIndex(Element indexXML, FactoryConnectionDB fcdb, InstanceService instanceService, String sufixIndex, boolean deleteIds, ConfigData configImport, String pathImportOtherXml) throws Exception{
		super(indexXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.deleteIds = deleteIds;
		this.listIndex= new LinkedList<IndexName>();
		this.sufixIndex = sufixIndex;
		this.dataBaseMap = instanceService.getDataBaseMap();
	}
	
	private void addAllIndexClass() throws SQLException, NamingException {
		//obtener de la lista
		Iterator it = listIndex.iterator();
		while (it.hasNext()) {
			IndexName ind = (IndexName)it.next();
			allIndexClass.add(ind.getIdto());
		}
		//obtener de BD
//		IndexDAO iDAO = new IndexDAO();
//		//iDAO.open();//No abrimos ya que en Menu se encarga de abrir una unica conexion
//		LinkedList<Object> llo = (LinkedList<Object>)iDAO.getAll();
//		it = llo.iterator();
//		while (it.hasNext()) {
//			Index ind = (Index)it.next();
//			allIndexClass.add(ind.getIdto());
//		}
		allIndexClass.addAll(Auxiliar.getIdtosWithIndex(fcdb));
		//iDAO.close();
	}
	private void updContIndex() throws NamingException, SQLException, DataErrorException, NoSuchColumnException {
		Iterator it = listIndex.iterator();
		while (it.hasNext()) {
			IndexName ind = (IndexName)it.next();
			int indexint = ind.getIndex();
			int actualIndex= IndexFilterFunctions.getActualIndex(ind.getIdto(),ind.getPrefix(),ind.getSufix(),ind.getPropPrefix(),null,ind.getMascPrefixTemp(), ind.getPropPrefixTemp(), ind.getContYear(), ind.getLastPrefixTemp(), ind.getMiEmpresa(), fcdb, allIndexClass, dataBaseMap);
			if (actualIndex>=indexint)
				ind.setIndex(actualIndex+1);
			else
				ind.setIndex(indexint);
		}
	}

	@Override
	public boolean configData() throws Exception {
		if(deleteIds){
			deleteAllIndex();
		}
		boolean success=extractAllIndex();
		addAllIndexClass();
		updContIndex();
		createIndexRoot();
		//createIndexClass();
		return success;
	}
	
	private void deleteAllIndex() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_INDICE, fcdb), instanceService.getDataBaseMap(), fcdb);
	}
	
	@Override
	public void importData() throws Exception {
		LinkedList<IndexName> listAux=new LinkedList<IndexName>();
		listAux.add(indexFormatoIndice);
		importAllIndex(listAux);//Importamos el indice de Formato Indice. Lo metemos primero ya que tiene que existir para ponerle el indice al resto
		importAllIndex(listIndex);
	}

	private void createIndexRoot() throws SQLException, NamingException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		TClaseDAO cDAO=new TClaseDAO();
		//cDAO.open();//No abrimos ya que en Menu se encarga de abrir una unica conexion
		Integer idto = (Integer)cDAO.getByName(Constants.CLS_INDICE).getFirst();
		//cDAO.close();
		IndexName in= new IndexName();
		in.setIdto(idto);
		in.setDomain(Constants.CLS_INDICE);
		in.setPropertyName(Constants.PROP_RDN);
		in.setProperty(Constants.IdPROP_RDN);
		in.setIndex(2);
		indexFormatoIndice=in;
	}
	/*private void createIndexClass() throws SQLException, NamingException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		TClaseDAO cDAO=new TClaseDAO();
		//cDAO.open();//No abrimos ya que en Menu se encarga de abrir una unica conexion
		LinkedList listClass=cDAO.getAll();
		//cDAO.close();
		Iterator itC=listClass.iterator();
		while(itC.hasNext()){
			TClase c=(TClase) itC.next();
			if (c.getIDTO()>=ConstantsXML.INITIAL_MODEL_CLASS && Auxiliar.existIdto(c.getIDTO())//Evitamos crear indice de clases que ya no existen. Porque TClase puede tener clases obsoletas && ik.isSpecialized(c.getIDTO(),Constants.IDTO_UTASK)
			){
				if (!Auxiliar.specializedFrom(c,Constants.IDTO_UTASK) && !Auxiliar.specializedFrom(c,Constants.IDTO_ENUMERATED) && !this.listExcludes.contains(c.getIDTO()) && (!this.idtoWithIdProp.containsKey(c.getIDTO()) || !this.idtoWithIdProp.get(c.getIDTO()).equals(Constants.IdPROP_RDN))){
					IndexName in= new IndexName();
					in.setIdto(c.getIDTO());
					in.setDomain(c.getName());
					in.setPropertyName(Constants.PROP_RDN);
					in.setProperty(Constants.IdPROP_RDN);
					if (c.getName().equals(Constants.CLS_FORMATO_INDICE)) {
						in.setIndex(2);
						rootIndex = in;
					} else {
						int index=IndexFilter.getActualIndex(c.getIDTO(),null,null,false,false,fcdb);
						in.setIndex(index+1);
						this.listIndex.add(in);
					}
				}
			}
		}
	}*/
	
	private void importAllIndex(LinkedList<IndexName> listIndex) throws Exception{
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		
		Iterator<IndexName> itin=listIndex.iterator();
		
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		
		while(itin.hasNext()){
			IndexName in=itin.next();
			
			//System.out.println("---> "+in.toString());
			boolean importar = true;
			if (!deleteIds)
				importar = !Auxiliar.hasIndex(in,fcdb);
			if (importar){
								
				Integer idto = tdao.getTClaseByName(Constants.CLS_INDICE).getIDTO();
				countido--;
				int ido=QueryConstants.getIdo(countido, idto);
				String rdn=String.valueOf("&"+ido+"&");
				list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				
				// insertar el resto de las DataProperties
				int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getDomain()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				
				prop = propDAO.getIdPropByName(Constants.PROP_INICIO_CONTADOR);
				list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, Double.parseDouble(String.valueOf(in.getIndex())), Double.parseDouble(String.valueOf(in.getIndex())), null, null, action.NEW));
				
				if (in.getPrefix()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_PREFIJO);
					list.add(new FactInstance(idto, ido, prop, in.getPrefix().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}
				if (in.getSufix()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_SUFIJO);
					list.add(new FactInstance(idto, ido, prop, in.getSufix().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}
				
				if (in.getPropFilterName()!=null) {
					Properties property=propDAO.getPropertyByName(in.getPropFilterName());
					prop = propDAO.getIdPropByName(Constants.PROP_CAMPO_FILTRO);
					if(new Category(property.getCAT()).isDataProperty()){
						idtoClass = Constants.IDTO_DATA_PROPERTY;
					}else{
						idtoClass = Constants.IDTO_OBJECT_PROPERTY;
					}
					int idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
					list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				}
				
				if (in.getValueFilter()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_VALOR_FILTRO);
					list.add(new FactInstance(idto, ido, prop, in.getValueFilter().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}
				
				if (in.getPropPrefixName()!=null) {
					Properties property=propDAO.getPropertyByName(in.getPropPrefixName());
					prop = propDAO.getIdPropByName(Constants.PROP_CAMPO_EN_PREFIJO);
					if(new Category(property.getCAT()).isDataProperty()){
						idtoClass = Constants.IDTO_DATA_PROPERTY;
					}else{
						idtoClass = Constants.IDTO_OBJECT_PROPERTY;
					}
					int idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
					list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				}
				
				if (in.getMascPrefixTemp()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_MASC_PREFIX_TEMP);
					list.add(new FactInstance(idto, ido, prop, in.getMascPrefixTemp().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}
				
				if (in.getPropPrefixTempName()!=null) {
					Properties property=propDAO.getPropertyByName(in.getPropPrefixTempName());
					prop = propDAO.getIdPropByName(Constants.PROP_CAMPO_EN_PREFIJO_TEMP);
					if(new Category(property.getCAT()).isDataProperty()){
						idtoClass = Constants.IDTO_DATA_PROPERTY;
					}else{
						idtoClass = Constants.IDTO_OBJECT_PROPERTY;
					}
					int idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
					list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				}
				
				if (in.getContYear()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_CONTADOR_AÑO);
					list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, Double.parseDouble(String.valueOf(in.getContYear())), Double.parseDouble(String.valueOf(in.getContYear())), null, null, action.NEW));
				}
				//System.out.println("in.getLastPrefixTemp() " + in.getLastPrefixTemp());
				if (in.getLastPrefixTemp()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_ULTIMO_PREFIJO_TEMP);
					list.add(new FactInstance(idto, ido, prop, in.getLastPrefixTemp().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}
				
				if (in.getMinDigits()!=null) {
					prop = propDAO.getIdPropByName(Constants.PROP_MIN_DIGITS);
					list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, Double.parseDouble(String.valueOf(in.getMinDigits())), Double.parseDouble(String.valueOf(in.getMinDigits())), null, null, action.NEW));

				}
				if (in.getMiEmpresa()!=null) {
					idtoClass = tdao.getTClaseByName(Constants.CLS_MI_EMPRESA).getIDTO();
					prop = propDAO.getIdPropByName(Constants.prop_mi_empresa);
					value = in.getMiEmpresa();
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	
	private boolean extractAllIndex() throws Exception {
		Iterator<Element> itInd=getChildrenXml().iterator();
		boolean success=true;
		while(itInd.hasNext()){
			IndexName in=new IndexName();
			Element inElem=itInd.next();
			try{
				if ( inElem.getName().equals(ConstantsXML.EXCLUDE_INDEX_ROOT)){
					Iterator<Element> itExc=inElem.getChildren().iterator();
					while(itExc.hasNext()){
						Element inExc=itExc.next();
						if (inExc.getAttributeValue(ConstantsXML.CLASS)!=null){	
							String className=inExc.getAttributeValue(ConstantsXML.CLASS);
							Integer idto= Auxiliar.getIdtoClass(className, fcdb);
							if (idto!=null){
								this.listExcludes.add(idto);
							}else{
								//throw new ConfigException("Error: La clase "+className+" no existe en el modelo");
								continue;
							}
						}else{
							throw new ConfigException("Error: El atributo "+ConstantsXML.CLASS+" es obligatorio");
						}	
					}
				}else{
					Integer idto = null;
					if (inElem.getAttributeValue(ConstantsXML.CLASS)!=null){
						String className=inElem.getAttributeValue(ConstantsXML.CLASS);
						if (idto==null)
							idto= Auxiliar.getIdtoClass(className, fcdb);
						if (idto!=null){
							in.setIdto(idto);
							in.setDomain(className);
						}else{
							//throw new ConfigException("Error: La clase "+className+" no existe en el modelo");
							continue;
						}
					}else{
						throw new ConfigException("Error: El atributo "+ConstantsXML.CLASS+" es obligatorio");
					}
					
					if (inElem.getAttributeValue(ConstantsXML.PROP_INDEX)!=null){
						String propInd=inElem.getAttributeValue(ConstantsXML.PROP_INDEX);
						if (idto==null) {
							String className=inElem.getAttributeValue(ConstantsXML.CLASS);
							idto= Auxiliar.getIdtoClass(className, fcdb);
						}
						Integer idPropInd= Auxiliar.getIdProp(propInd, idto, fcdb);
						if (idPropInd!=null){
							in.setProperty(idPropInd);
							in.setPropertyName(propInd);
						}else{
							//throw new ConfigException("Error: La property "+propInd+" no pertenece a "+in.getIdto()+" o no existe");
							continue;
						}
					}else{
						throw new ConfigException("Error: El atributo "+ConstantsXML.PROP_INDEX+" es obligatorio");
					}
					
					if(inElem.getAttributeValue(ConstantsXML.PREFIX)!=null){
						String prefix=inElem.getAttributeValue(ConstantsXML.PREFIX).toString();
						in.setPrefix(prefix);
					}
					
					if(inElem.getAttributeValue(ConstantsXML.SUFIX)!=null){
						String sufix=inElem.getAttributeValue(ConstantsXML.SUFIX).toString();
						in.setSufix(sufix);
					}
					
					if(inElem.getAttributeValue(ConstantsXML.PROP_PREFIX)!=null){
						String propPF=inElem.getAttributeValue(ConstantsXML.PROP_PREFIX).toString();
						if (idto==null) {
							String className=inElem.getAttributeValue(ConstantsXML.CLASS);
							idto= Auxiliar.getIdtoClass(className, fcdb);
						}
						Integer idPropPF=Auxiliar.getIdProp(propPF, idto, fcdb);
						if (idPropPF!=null){
							in.setPropPrefix(idPropPF);
							in.setPropPrefixName(propPF);
						}else{
							//throw new ConfigException("Error: La property "+idPropPF+" no pertenece a "+in.getIdto()+" o no existe");
							continue;
						}
					}
					
					if(inElem.getAttributeValue(ConstantsXML.MASC_PREFIX_TEMP)!=null){
						String mascPrefixTemp=inElem.getAttributeValue(ConstantsXML.MASC_PREFIX_TEMP).toString();
						in.setMascPrefixTemp(mascPrefixTemp);
					}
					if(inElem.getAttributeValue(ConstantsXML.PROP_PREFIX_TEMP)!=null){
						String propPF=inElem.getAttributeValue(ConstantsXML.PROP_PREFIX_TEMP).toString();
						if (idto==null) {
							String className=inElem.getAttributeValue(ConstantsXML.CLASS);
							idto= Auxiliar.getIdtoClass(className, fcdb);
						}
						Integer idPropPF=Auxiliar.getIdProp(propPF, idto, fcdb);
						if (idPropPF!=null){
							in.setPropPrefixTemp(idPropPF);
							in.setPropPrefixTempName(propPF);
						}else{
							//throw new ConfigException("Error: La property "+idPropPF+" no pertenece a "+in.getIdto()+" o no existe");
							continue;
						}
					}
					if (inElem.getAttributeValue(ConstantsXML.CONT_YEAR)!=null){
						String contYear=inElem.getAttributeValue(ConstantsXML.CONT_YEAR);
						in.setContYear(Integer.valueOf(contYear));
					}

					if(inElem.getAttributeValue(ConstantsXML.PROP_FILTER)!=null){
						String propF=inElem.getAttributeValue(ConstantsXML.PROP_FILTER).toString();
						if (idto==null) {
							String className=inElem.getAttributeValue(ConstantsXML.CLASS);
							idto= Auxiliar.getIdtoClass(className, fcdb);
						}
						Integer idPropF=Auxiliar.getIdProp(propF, idto, fcdb);
						if (idPropF!=null){
							in.setPropFilter(idPropF);
							in.setPropFilterName(propF);
						}else{
							//throw new ConfigException("Error: La property "+idPropF+" no pertenece a "+in.getIdto()+" o no existe");
							continue;
						}
					}
					
					if(inElem.getAttributeValue(ConstantsXML.VALUE_FILTER)!=null){
						String valueF=inElem.getAttributeValue(ConstantsXML.VALUE_FILTER).toString();
						in.setValueFilter(valueF);
					}
					
					
					if (inElem.getAttributeValue(ConstantsXML.INDEX_ROOT)!=null){
						String index=inElem.getAttributeValue(ConstantsXML.INDEX_ROOT);
						Integer indexint=Integer.valueOf(index);
						if (idto==null) {
							String className=inElem.getAttributeValue(ConstantsXML.CLASS);
							idto= Auxiliar.getIdtoClass(className, fcdb);
						}
						String lastPrefixTemp = IndexFilterFunctions.getLastPrefixTemp(in.getIdto(), in.getMascPrefixTemp(), in.getPropPrefixTemp(), in.getContYear(), in.getPropFilter(), in.getValueFilter(), fcdb, dataBaseMap);
						//System.out.println("lastPrefixTemp " + lastPrefixTemp);
						if (lastPrefixTemp!=null)
							in.setLastPrefixTemp(lastPrefixTemp);
						in.setIndex(indexint);
						//este chequeo se hará despues
//						int actualIndex=IndexFilterFunctions.getActualIndex(idto,in.getPrefix(),in.getSufix(),in.getPropPrefix(),null,in.getMascPrefixTemp(), in.getPropPrefixTemp(), in.getContYear(), lastPrefixTemp, fcdb, allIndexClass);
//						if (actualIndex>=indexint)
//							in.setIndex(actualIndex+1);
//						else
//							in.setIndex(indexint);
					}else{
						throw new ConfigException("Error: El atributo "+ConstantsXML.INDEX_ROOT+" es obligatorio");
					}
					
					if(inElem.getAttributeValue(ConstantsXML.GLOBAL_SUFIX)!=null){
						String valueF=inElem.getAttributeValue(ConstantsXML.GLOBAL_SUFIX);
						if (valueF.equals("SI"))
							in.setGlobalSufix(true);
						else
							in.setGlobalSufix(false);
					} else
						in.setGlobalSufix(false);
					
					if (inElem.getAttributeValue(ConstantsXML.MIN_DIGITS)!=null){
						String minDigits=inElem.getAttributeValue(ConstantsXML.MIN_DIGITS);
						in.setMinDigits(Integer.valueOf(minDigits));
					}

					if(inElem.getAttributeValue(ConstantsXML.MI_EMPRESA)!=null){
						String miEmpresa=inElem.getAttributeValue(ConstantsXML.MI_EMPRESA).toString();
						O_Datos_AttribDAO oDAO = new O_Datos_AttribDAO();
						TClaseDAO tDAO = new TClaseDAO();
						tDAO.open();
						int idtoMiEmpresa = (Integer)tDAO.getByName(Constants.CLS_MI_EMPRESA).getFirst();
						LinkedList<Object> loda = oDAO.getAllCond("ID_TO=" + idtoMiEmpresa + " AND PROPERTY=" + Constants.IdPROP_RDN + " AND VAL_TEXTO='" + miEmpresa.replaceAll("'", "''") + "'");
						if (loda.size()>0) {
							in.setMiEmpresa(((O_Datos_Attrib)loda.getFirst()).getIDO());
							in.setMiEmpresaRdn(miEmpresa);
						} else {
							throw new ConfigException("Error: Se esta intentanto establecer un índice con un filtro para una empresa no creada: " + miEmpresa);
						}
						tDAO.close();
					}
					
					if(inElem.getAttributeValue(ConstantsXML.SINGLE)!=null){
						String single=inElem.getAttributeValue(ConstantsXML.SINGLE).toString();
						if(single.equals("SI")){
							in.setSingle(true);
							
						}else{
							in.setSingle(false);
						}
					}
					
					this.idtoWithIdProp.put(in.getIdto(), in.getProperty());
					listIndex.add(in);
				}
				
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}

}
