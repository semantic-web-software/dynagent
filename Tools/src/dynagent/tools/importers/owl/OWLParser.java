/**
 * OWLParser.java
 * @author Jose A. Zamora Aguilera - 
 * @description Parse a OWL Protege file and build  the dynagent tables structure and the facts that represents the model
 */	

package dynagent.tools.importers.owl;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.Document;
import org.jdom.JDOMException;

import com.hp.hpl.jena.util.FileUtils;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.HelpClass;
import dynagent.common.basicobjects.HelpIndividual;
import dynagent.common.basicobjects.HelpProperty;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.TPropiedadClase;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.ObjectChanged;
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
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.knowledge.action;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.ruleengine.ConceptLogger;
import dynagent.ruleengine.RuleEngineLogger;
import dynagent.server.database.dao.AccessDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.TPropiedadClaseDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.ServerEngine;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;
import dynagent.server.services.querys.AuxiliarQuery;
import dynagent.tools.importers.Connect;
import dynagent.tools.importers.migration.ODatosAtribToXml;
import dynagent.tools.owl.ExtractOWLView;
import dynagent.tools.owl.OWL;
import dynagent.tools.owl.OWLAux;
import dynagent.tools.owl.OWLDroolsRuler;
import dynagent.tools.owl.OWLIds;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLComplementClass;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLEnumeratedClass;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLIndividual;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;


public class OWLParser{
	//tabla que guarda la equivalencia entre el nombre de las clases y su identificador entero
	private LinkedHashMap <String,Integer>hmIDxName=new LinkedHashMap <String,Integer>();
	private LinkedHashMap <String,Integer>hmIDxNameNew=new LinkedHashMap <String,Integer>();
	private int maximumClassId;
	//tabla que guarda la equivalencia entre el nombre de las propiedades y su identificador entero
	private LinkedHashMap <String,Integer>hmPropiedades=new LinkedHashMap <String,Integer>();
	private LinkedHashMap <String,Integer>hmPropiedadesNew=new LinkedHashMap <String,Integer>();
	private int maximumPropId;
	//tabla que guarda la equivalencia entre el nombre de los individuos de modelo y su ido
	private LinkedHashMap <String,Integer>hmIndividual=new LinkedHashMap<String, Integer>();
	
	private FactoryConnectionDB fcdb;
	private LinkedHashMap<String,HashSet<String>> abstractclasess=new LinkedHashMap<String,HashSet<String>>();
	private ArrayList<String> enumeratedclasess=new  ArrayList<String>();
	private ArrayList<String> actionclasess=new  ArrayList<String>();

	//Lista que guardará todos los instances del modelo
	private LinkedHashMap<String,HashSet<Instance>> allInstances = new LinkedHashMap<String,HashSet<Instance>>();

	//Lista que guardará todos la información sobre los individuos
	private LinkedList<Instance> allIndividualInstances = new LinkedList<Instance>();
	private ArrayList<String> lnameClases= new ArrayList<String>();
	private ArrayList<String>lpropiedadesMemo=new ArrayList<String>();
	private ArrayList<String>lpropiedadesImage=new ArrayList<String>();
	private ArrayList<String>lpropiedadesFile=new ArrayList<String>();

	private HashMap<String,HelpClass> mapHelpClasses=new HashMap<String,HelpClass>();

	private HashMap<String,HelpProperty> mapHelpProperties=new HashMap<String,HelpProperty>();
	
	private HashMap<String,HelpIndividual> mapHelpIndividuals=new HashMap<String,HelpIndividual>();

	private HashMap<String,HashSet<String>> mapVirtualAbstract=new HashMap<String, HashSet<String>>();//Almacena las abstractas virtuales y sus especializados

	private OWLModel owlModel;
	private DataBaseMap dataBaseMap;
	private OWLIds owlIds;

	public OWLParser(OWLModel owlModel, FactoryConnectionDB fcdb, OWLIds owlIds) {
		this.fcdb=fcdb;
		this.owlModel=owlModel;
		this.owlIds=owlIds;
	}

	public void buildSystem() throws SQLException, NamingException, ParseException, IOException{
		//Leemos los archivos que contienen los ids a utilizar
		owlIds.readFiles();
		hmIDxName=owlIds.getClassNameIdMap();
		hmPropiedades=owlIds.getPropertyNameIdMap();
		maximumClassId=owlIds.getMaximumClassId();
		maximumPropId=owlIds.getMaximumPropertyId();
		
		System.out.println("[OWLParser]: Construyendo instances");
		buildInstances(owlModel);
		System.out.println("[OWLParser]: Construyendo clases");
		buildIndividuals();
		System.out.println("[OWLParser]: Insertando clases");
		insertClasses();
		System.out.println("[OWLParser]: Insertando herencias");
		insertHierarchy();
		System.out.println("[OWLParser]: Insertando properties");
		insertProperties();
		System.out.println("[OWLParser]: Insertando instances");
		insertInstances(this.traslateInstances(this.getAllInstances()));
		System.out.println("[OWLParser]: Insertando help");
		
		//Guardamos en sus archivos los nuevos ids asignados, si los hubiera
		owlIds.setClassNameIdMap(hmIDxNameNew);
		owlIds.setPropertyNameIdMap(hmPropiedadesNew);
		owlIds.saveFiles();
	}

	public void buildTablasClasesYPropiedades() throws SQLException, NamingException, ParseException, IOException{
		//Leemos los archivos que contienen los ids a utilizar
		owlIds.readFiles();
		hmIDxName=owlIds.getClassNameIdMap();
		hmPropiedades=owlIds.getPropertyNameIdMap();
		maximumClassId=owlIds.getMaximumClassId();
		maximumPropId=owlIds.getMaximumPropertyId();
				
		System.out.println("[OWLParser]: Construyendo clases");
		buildIndividuals();
		System.out.println("[OWLParser]: Insertando clases");
		insertClasses();
		
		System.out.println("[OWLParser]: Insertando properties");
		insertProperties();
	}
	
	private void buildIndividuals() throws ParseException, SQLException, NamingException{
		LinkedHashMap<String, Integer> mapids=owlIds.getClassNameIdMap();
		
		
		int countido=0;
		for (Iterator itClass =owlModel.getUserDefinedOWLNamedClasses().iterator(); itClass.hasNext();)
		{
			OWLNamedClass cls = (OWLNamedClass) itClass.next();
			String className=cls.getLocalName();

			//Individuos 
			if(!className.equals(Constants.IDTO_THING))
			{
				Collection cIndividuos=cls.getInstances(false);//false para que solo devuelva las instancias directas, no las instancias de clases hijas. IMPTE PARA EL
				//comportamiento de las enumerados por definición de la clase como enumerada en nuestro modelo.
				//obtenemos las propiedades de esta clase.
				//Collection cPropiedades=((OWLNamedClass)cls).getAssociatedProperties();

				
				for(Iterator itI=cIndividuos.iterator();itI.hasNext();)
				{
					Object posibIndiv=itI.next();
					if(!(posibIndiv instanceof RDFResource)){
						System.err.println("\n DEBUG OWLPARSER getInstances devolvio posibIndiv="+posibIndiv+"  "+posibIndiv.getClass());
					}

					else if(posibIndiv instanceof RDFResource){
						RDFResource individuo=(RDFResource)posibIndiv;
						String nameIndividuo=individuo.getLocalName();

						if(nameIndividuo.startsWith("_")){//Jena les pone _ delante a los que empiezan por numero en el modelo. Le quitamos el _
							nameIndividuo=nameIndividuo.substring(1, nameIndividuo.length());
						}
						
						//Ido negativo ya que no existe todavia en base de datos
						countido--;
						
						int idto=mapids.get(className);
						int ido=QueryConstants.getIdo(countido, idto);//idto clase
						hmIndividual.put(nameIndividuo, ido);//guardamos el nombre del individuo asociado al ido temporal, de esta manera podremos luego actualizar instance si hace falta
						
						String nameClase=cls.getLocalName();
						//System.out.println("Individuo: "+nameIndividuo+"(clase="+nameClase+")");

						//EL NAME QUE TIENEN EN OWL LOS INDIVIDUOS SE CORRESPONDERA CON EL SU VALOR DE LA PROPIEDAD RDN
						//instance del rdn del individuo

						allIndividualInstances.add(this.buildRdnInstance(nameClase, nameIndividuo)); 
						OWLIndividual owlindividuo=(OWLIndividual)individuo;
						Collection cprop=individuo.getRDFProperties();
						for(Iterator it=cprop.iterator();it.hasNext();){
							RDFProperty rdfprop=(RDFProperty)it.next();
							String nameprop=rdfprop.getLocalName();

							if(rdfprop instanceof DefaultOWLDatatypeProperty){
								Collection collecValores=individuo.getPropertyValues(rdfprop, true);
								Iterator itValores=collecValores.iterator();
								while(itValores.hasNext()){
									//version antigua solo soportaba cardinalidad 1 Object value=individuo.getPropertyValue(rdfprop);
									Object value=itValores.next();
									//System.out.println("     propiedad(dataproperty): "+nameprop+"  has value="+value);
									owlindividuo.getHasValuesOnTypes(rdfprop);
									/*owlindividuo.getPropertyValueCount(rdfprop);
	    		        			owlindividuo.getPropertyValueLiterals(rdfprop);
	    		        			owlindividuo.getPropertyValueLiteral(rdfprop);*/
									//System.out.println("     propiedad: "+rdfprop.getLocalName()+"  has value="+value);
									String svalue =value.toString();
									
									boolean excluirdato=false;
									Instance ins=new Instance();
									ins.setIDTO(nameClase);
									ins.setIDO(nameIndividuo);
									ins.setNAME(nameClase);
									ins.setPROPERTY(nameprop);	        			
									if(value instanceof String){
										ins.setVALUE(svalue);
										ins.setVALUECLS(Constants.DATA_STRING);
									}
									else if(value instanceof Integer){

										ins.setQMIN(svalue);
										ins.setQMAX(svalue);
										ins.setVALUECLS(Constants.DATA_INT);
									}
									else if(value instanceof Date){
										String time=null;
										String valueCls=null;
										if(svalue.length()<9){
											SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
											time=String.valueOf(df.parse(svalue).getTime()/1000);
											valueCls=Constants.DATA_TIME;
										}else if(svalue.length()<11){
											SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
											time=String.valueOf(df.parse(svalue).getTime()/1000);
											valueCls=Constants.DATA_DATE;
										}else{
											SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											svalue=svalue.replaceFirst("T", " ");//Viene con una T antes de las horas que tenemos que quitar
											time=String.valueOf(df.parse(svalue).getTime()/1000);
											valueCls=Constants.DATA_DATETIME;
										}
										
										ins.setQMIN(time);
										ins.setQMAX(time);
										ins.setVALUECLS(valueCls);
									}
									else if(value instanceof Float){
										ins.setQMIN(svalue);
										ins.setQMAX(svalue);
										ins.setVALUECLS(Constants.DATA_DOUBLE);
									}
									else if (value instanceof Boolean){
										Boolean booleanvalue = (Boolean) value;
										ins.setVALUECLS(Constants.DATA_BOOLEAN);
										if(booleanvalue){
											ins.setQMIN(String.valueOf(Constants.ID_BOOLEAN_TRUE));
											ins.setQMAX(String.valueOf(Constants.ID_BOOLEAN_TRUE));
										}
										else{
											ins.setQMIN(String.valueOf(Constants.ID_BOOLEAN_FALSE));
											ins.setQMAX(String.valueOf(Constants.ID_BOOLEAN_FALSE));	
										}
									}
									else if(rdfprop.getLocalName().startsWith("fecha")){
										String time=null;
										String valueCls=null;
										if(svalue.length()<9){
											SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
											time=String.valueOf(df.parse(svalue).getTime()/1000);
											valueCls=Constants.DATA_TIME;
										}else if(svalue.length()<11){
											SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
											time=String.valueOf(df.parse(svalue).getTime()/1000);
											valueCls=Constants.DATA_DATE;
										}else{
											SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											svalue=svalue.replaceFirst("T", " ");//Viene con una T antes de las horas que tenemos que quitar
											time=String.valueOf(df.parse(svalue).getTime()/1000);
											valueCls=Constants.DATA_DATETIME;
										}
										
										ins.setQMIN(time);
										ins.setQMAX(time);
										ins.setVALUECLS(valueCls);
									}
									else{
										if(!(value instanceof DefaultRDFSLiteral)){
											System.err.println(" WARNING buildIndividuosInstances: Caso no contemplado para prop= "+rdfprop+"  con valor="+value+"  class= "+value.getClass());
											System.err.println(" NO SE IMPORTARÁ EL VALOR MAL PARSEADO");
										}
										excluirdato=true;
									}
									if(!excluirdato){
										allIndividualInstances.add(ins);
									}
								}
							}
							else if(rdfprop instanceof DefaultOWLObjectProperty){

								//Object value=individuo.getPropertyValue(rdfprop);
								Collection collecValores=individuo.getPropertyValues(rdfprop, true);
								Iterator itValores=collecValores.iterator();
								while(itValores.hasNext()){
									//System.out.println("Value="+value);
									DefaultOWLIndividual dvalue=(DefaultOWLIndividual)itValores.next();
									Instance ins=new Instance();
									ins.setIDTO(nameClase);
									ins.setIDO(nameIndividuo);
									ins.setNAME(nameClase);
									ins.setPROPERTY(nameprop);		        			
									ins.setVALUE("#"+dvalue.getLocalName());
									String indName=OWL.extractOWL_prefix(dvalue.getDirectType().getName());
									ins.setVALUECLS(indName);
									//System.out.println("=======ins de objectValue=:"+ins);
									allIndividualInstances.add(ins);
								}
							}
						}
						
						Iterator<String> itcoment=owlindividuo.getComments().iterator();
						while(itcoment.hasNext()){
							Object comment=itcoment.next();
							if(comment instanceof DefaultRDFSLiteral){
								String language=((DefaultRDFSLiteral)comment).getLanguage();
								if(language!=null){
									String description=((DefaultRDFSLiteral)comment).getBrowserText();
									HelpIndividual helpIndividual=new HelpIndividual();
									helpIndividual.setDescription(description);
									helpIndividual.setLanguage(language);
									helpIndividual.setClassName(className);
									this.mapHelpIndividuals.put(nameIndividuo,helpIndividual);
								}
							}
						}
					}
				}    
			}
		}
	}

	public void buildDataBase(String BusinessFunctions,boolean import_help_views,boolean restrictions) throws SQLException, NamingException, IOException{
		System.out.println("Construyendo base de datos relacional");
		dataBaseMap = new DataBaseMap(fcdb, true);

		TClaseDAO claseDAO=new TClaseDAO();
		claseDAO.open();
		int idtoClase=26;//claseDAO.getTClaseByName("CLASE").getIDTO();
		int idtoPropiedadDato=28;//claseDAO.getTClaseByName("PROPIEDAD_DATO").getIDTO();
		int idtoPropiedadObjeto=29;//claseDAO.getTClaseByName("PROPIEDAD_OBJETO").getIDTO();
		int idtoPropiedad=27;//claseDAO.getTClaseByName("PROPIEDAD").getIDTO();
		claseDAO.close();

		Set<Integer> excludedIdtos=new HashSet<Integer>();
		excludedIdtos.add(idtoClase);
		excludedIdtos.add(idtoPropiedadDato);
		excludedIdtos.add(idtoPropiedadObjeto);

		dataBaseMap.constructAllTables(excludedIdtos, restrictions);

		excludedIdtos.clear();
		excludedIdtos.add(idtoPropiedad);
		dataBaseMap.constructAllViews(excludedIdtos);

		if(import_help_views){
			System.out.println("Ejecutando script de vistas");
			InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/Views.sql");
			File scriptFile = File.createTempFile("views", "sql");
			Auxiliar.inputStreamToFile(in, scriptFile);
			DBQueries.executeScript(fcdb, scriptFile);
			scriptFile.delete();
		}
		if(BusinessFunctions!=null){
			System.out.println("Ejecutando script de negocio");
			InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/"+BusinessFunctions+".sql");
			File scriptFile = File.createTempFile("businessfunctions", "sql");
			Auxiliar.inputStreamToFile(in, scriptFile);
			DBQueries.executeScript(fcdb, scriptFile);
			scriptFile.delete();
		}
	}

	/**
	 * Asigna las restricciones de claves foraneas a la base de datos
	 */
	public void putDataBaseRestrictions(){
		dataBaseMap.putRestrictions();
	}


	public void insertModelIndividuals() throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, JDOMException, ParseException, InterruptedException, NoSuchColumnException{
		System.out.println("[OWLParser]: Insertando individuos de modelo");
		boolean importSuccess=false;
		DBQueries.execute(fcdb, "START TRANSACTION;");
		try{
			InstanceService m_IS = new InstanceService(fcdb, null, false);
			m_IS.setIk(new ServerEngine(fcdb));
			m_IS.setDataBaseMap(dataBaseMap);

			LinkedList<Instance> individuals = new LinkedList<Instance>();
			for(Instance in:allIndividualInstances){
				Instance insnum=this.traslateInstanceToNumeric(in);
				individuals.add(insnum);
			}

			HashMap<Integer,String> nameIndividualsXIdo=new HashMap<Integer, String>();
			ArrayList<String> idoToInsert=new ArrayList<String>();
			HashMap<Integer,Integer> idoToUpdate=new HashMap<Integer,Integer>();
			for(Instance instance:individuals){
				if(instance.getPROPERTY().equals(String.valueOf(Constants.IdPROP_RDN))){
					Integer ido=InstanceService.getIdo(fcdb, dataBaseMap, Integer.valueOf(instance.getIDTO()), instance.getVALUE(), false);
					if(ido!=null){
						idoToUpdate.put(Integer.valueOf(instance.getIDO()), ido);
					}else{
						idoToInsert.add(instance.getIDO());
					}
					nameIndividualsXIdo.put(Integer.valueOf(instance.getIDO()), instance.getVALUE());
				}
			}

			//Obtenemos los facts de los instances que hay que insertar en base de datos
			IndividualData indivData=new IndividualData();
			for(Instance instance:individuals){
				if(idoToInsert.contains(instance.getIDO())){
					FactInstance f=instance.toFactInstance();
					//Si es un objectProperty y el rdn de su value ya existe en base de datos cambiamos el value negativo por el de base de datos
					if(!Constants.isDataType(f.getVALUECLS())){
						int value=Integer.valueOf(f.getVALUE());
						if(idoToUpdate.containsKey(value)){
							f.setVALUE(String.valueOf(idoToUpdate.get(value)));
						}
					}
					f.setOrder(action.NEW);
					indivData.addIPropertyDef(f);
				}
			}
			//Insertamos en base de datos
			Changes changes=m_IS.serverTransitionObject(null, Constants.USER_SYSTEM, indivData, null, true, false, null, null);
			for(ObjectChanged oc:changes.getAObjectChanged()) {
				idoToUpdate.put(oc.getOldIdo(),oc.getNewIdo());
			}

			//Modificamos los instances que apuntaban a individuos, con el ido ya real. Ya que estaban con el ido ficticio
			for(Integer oldIdo:idoToUpdate.keySet()){
				//updateIdoValueIndividualInstance(oldIdo,idoToUpdate.get(oldIdo));No necesario porque ya no se guardan idos en value de instances. Y no hay ningun individuo tampoco.
				hmIndividual.put(nameIndividualsXIdo.get(oldIdo),idoToUpdate.get(oldIdo));
			}

			//Insertamos los individuos acción
			System.out.println("[OWLParser]: Insertando individuos de acciones");
			insertIndividualOfAction(m_IS);
			importSuccess=true;
		}finally{
			if(importSuccess){
				DBQueries.execute(fcdb, "COMMIT;");
			}else DBQueries.execute(fcdb, "ROLLBACK;");
		}
	}


	public LinkedHashMap<String,HashSet<Instance>> getAllInstances() {
		return allInstances;
	}

	public LinkedList<String> dbExecuteQueryNameInd(String sql) throws SQLException,NamingException{
		ResultSet rs=null;
		LinkedList<String> ls= new LinkedList<String>();
		Statement st = null;
		ConnectionDB con = fcdb.createConnection(true);
		st = con.getBusinessConn().createStatement();
		try {

			RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			rs = st.executeQuery(sql);
			while(rs.next()){
				ls.add(rs.getString(3));
			}
			rs.close();
			st.close();
		}catch(SQLException ex){
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				fcdb.close(con);
			throw ex;
		}
		return ls;


	}

	public String getIdPropFromName(String propiedad){
		if(propiedad.equals(Constants.PROP_RDN)){
			return String.valueOf(Constants.IdPROP_RDN);
		}
		else if(hmPropiedades.containsKey(propiedad)){
			return (hmPropiedades.get(propiedad)).toString();
		}
		else{
			System.out.println(" WARNING: OWLPARSER.getIdPropFRomName no encontro id para propiedad="+propiedad);
			return propiedad;
		}
	}

	public String getIdClassFromName(String name){

		if(this.hmIDxName.containsKey(name)){
			return (this.hmIDxName.get(name)).toString();
		}
		else{
			//System.out.println("   mapaClases no contiene "+name);
			//podria ser un tipo de dato
			Integer idto=Constants.getIdDatatype(name);
			if(idto!=null){
				return idto.toString();
			}
			else{
				System.out.println(" WARNING:OWLPARSER.getIdClassFromName(name="+name+"  devuelve null");
				return null;
			}
		}
	}


	public void addPropertyRdnToTableInstances() throws SQLException, NamingException{
		Iterator<String> it=lnameClases.iterator();
		LinkedList<Instance>lista=new LinkedList<Instance>();  
		while(it.hasNext()){
			String sidto=String.valueOf(this.getHmIDxName().get(it.next()));

			//			TODAS LAS CLASES VAN A TENER EL RDN 
			Instance insrdn=new Instance();
			insrdn.setIDTO(sidto);
			insrdn.setPROPERTY(String.valueOf(Constants.IdPROP_RDN));
			insrdn.setOP(Constants.OP_INTERSECTION);
			insrdn.setVALUECLS(String.valueOf(Constants.IDTO_STRING));
			lista.add(insrdn);
			//con cardinalidad 1
			Instance insrdnCar=new Instance();
			insrdnCar.setIDTO(sidto);
			insrdnCar.setPROPERTY(String.valueOf(Constants.IdPROP_RDN));
			insrdnCar.setOP(Constants.OP_CARDINALITY);
			insrdnCar.setQMIN("1.0");
			insrdnCar.setQMAX("1.0");
			lista.add(insrdnCar);
		}
		insertInstances(lista);
	}


	public Instance traslateInstanceToNumeric(Instance ins){
		Instance insCod=new Instance();
		//las propiedades memo(que se deducen de que su longitud (informada como un comentario rdf a la prop sea mayor de cierto valor se meten en una lista global lpropiedadesMemo
		boolean isMemoDatatype=lpropiedadesMemo.contains(ins.getPROPERTY());
		boolean isImageDataType=lpropiedadesImage.contains(ins.getPROPERTY());
		boolean isFileDataType=lpropiedadesFile.contains(ins.getPROPERTY());

		if(ins.getIDO()!=null){
			insCod.setIDO(String.valueOf(this.hmIndividual.get(ins.getIDO())));
		}
		
		insCod.setIDTO(getIdClassFromName(ins.getIDTO()));
		insCod.setNAME(ins.getIDTO());
		insCod.setVIRTUAL(ins.isVIRTUAL());
		insCod.setOP(ins.getOP());
		insCod.setPROPERTY(getIdPropFromName(ins.getPROPERTY()));
		insCod.setQMAX(ins.getQMAX());
		insCod.setQMIN(ins.getQMIN());
		if(ins.getVALUECLS()!=null){
			if(isMemoDatatype){
				insCod.setVALUECLS(String.valueOf(Constants.IDTO_MEMO));
			}
			else if(isImageDataType){
				insCod.setVALUECLS(String.valueOf(Constants.IDTO_IMAGE));
			}
			else if(isFileDataType){
				insCod.setVALUECLS(String.valueOf(Constants.IDTO_FILE));
			}
			else{
				insCod.setVALUECLS(getIdClassFromName(ins.getVALUECLS()));
			}


		}
		//Value puede ser  o un data value, un individuo, o una clase
		//A los individuos se le añadio un # delante
		if(ins.getVALUE()!=null&&ins.getVALUE().contains("#")){//es un individuo, hay que obtener su id numérico
			String value=ins.getVALUE();
			value=value.substring(1,value.length());//quitamos el caracter #
			//obtenemos el identificador numérico de este individuo.
			if(this.hmIndividual.containsKey(value)){
				if(ins.getOP()==null){
					insCod.setVALUE(String.valueOf(this.hmIndividual.get(value)));//Si es un individuo ponemos su ido ya que no se almacenara en instances
				}else{
					insCod.setVALUE(value);//Guardamos su rdn (se almacenara en instances) en vez de el ido ya que es un problema para los shared beans
				}
			}
			else{
				RuleEngineLogger.getLogger().write("[OWLParser]:_"+"      WARNING:  Tenemos en value un individuo que no tiene identificador numérico");
				RuleEngineLogger.getLogger().write("     WARNING:  Tenemos en value un individuo que no tiene identificador numérico");
			}
		}
		else{ //es un data value, se pasa tal cual
			insCod.setVALUE(ins.getVALUE());
		}
		//System.out.println("Traslate\n ins="+ins+"\ninsnumer="+insCod);
		return insCod;
	}



	/**
	 * Inserta el modelo en la tabla Instances.
	 *
	 */

	public LinkedList <Instance> traslateInstances(LinkedHashMap <String,HashSet<Instance>> instances){
		LinkedList <Instance> linstaInstancenum=new LinkedList <Instance>();
		for(String className:instances.keySet()){
			for(Instance instance:instances.get(className)){
				Instance insNumer=this.traslateInstanceToNumeric(instance);
				//System.out.println("[OWLParser]:_ins="+instances.get(i).toStringNotNull());
				//System.out.println("[OWLParser]:_insnum="+insNumer.toStringNotNull());
				linstaInstancenum.add(insNumer);
			}
		}
		return linstaInstancenum;
	}




	/**
	 * Inserta instances en la tabla instances 
	 * @param instances
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public void insertInstances(LinkedList<Instance> instances) throws SQLException, NamingException{
		//System.out.println("\n..................OWLParser.insertListInstances: .");
		IDAO idao = DAOManager.getInstance().getDAO("instances");
		idao.setCommit(false);
		idao.open();
		InstanceDAO insdao = (InstanceDAO)idao.getDAO();
		for(int i=0;i<instances.size();i++){

			//PARA CHEK POSIBLES PROBLEMAS EN DEFINICIONES DEL MODELO O EN EL PARSE DE PROTEGE

			insdao.insert(instances.get(i));
		}
		idao.commit();
		idao.close();
	}


	/**
	 * Inserta las clases en la tabla donde se asignan y almacenan los identificadores de clases
	 * a sus nombres.
	 * @param owlModel
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public  void insertClasses() throws SQLException, NamingException{
		//System.out.println("[OWLParser]buildTClases Antes recibe lnombres="+lNameClases);
		IDAO idao = DAOManager.getInstance().getDAO("Clases");
		TClaseDAO claseDao = (TClaseDAO) idao.getDAO();
		idao.setCommit(false);
		idao.open();
		//System.out.println("[OWLParser]buildTClases recibe lnombres="+lNameClases);		

		Iterator<String> itClases=allInstances.keySet().iterator();
		int idto;
		
		while(itClases.hasNext())
		{
			String name=itClases.next();
			TClase clase = claseDao.getTClaseByName(name);   
			if(clase == null){ //No se encuentra la clase en la tabla TClases: la insertamos
				if(hmIDxName.get(name)!=null){
					idto=hmIDxName.get(name);
				}else{
					//tiene un idto fijo?
					Integer cidto=Constants.getIdConstantClass(name);
					if(cidto!=null){
						idto=cidto;//idto fijo
					}
					else{
						maximumClassId++;
						idto=maximumClassId;//new Integer(claseDao.getLastPK(String.valueOf(Constants.MIN_ID_NO_SPECIALCLASS), String.valueOf(Constants.MAX_ID_CLASS)).toString())+1;
					}
					hmIDxName.put(name, idto);
					hmIDxNameNew.put(name, idto);
				}
				TClase tclase=new TClase(idto,name,abstractclasess.containsKey(name));
				claseDao.insert(tclase);
			}else{  //La clase ya está en TClases: obtenemos su idto.
				idto=clase.getIDTO();
				if(!Auxiliar.equals(clase.isAbstractClass(),abstractclasess.containsKey(name))){
					//Si ha cambiado el ser abstracta lo modificamos en base de datos
					clase.setAbstractClass(abstractclasess.containsKey(name));
					claseDao.set(clase);
				}
				if(!hmIDxName.containsKey(name)){
					//Una clase existe en la base de datos pero no en el archivo de ids
					Integer cidto=Constants.getIdConstantClass(name);
					if(cidto!=null){
						idto=cidto;//idto fijo
					}
					else{
						maximumClassId++;
						idto=maximumClassId;//new Integer(claseDao.getLastPK(String.valueOf(Constants.MIN_ID_NO_SPECIALCLASS), String.valueOf(Constants.MAX_ID_CLASS)).toString())+1;
					}
					hmIDxName.put(name, idto);
					hmIDxNameNew.put(name, idto);
					
					clase.setIDTO(idto);
					claseDao.set(clase);
					
				}else if(idto!=hmIDxName.get(name)){
					clase.setIDTO(hmIDxName.get(name));
					claseDao.set(clase);
				}
				
				
				//System.err.println("Debugging buildTclases, existe clase"+name+"  con id="+idto);
				//Auxiliar.leeTexto("pulse intro");
			}
		}
		idao.commit();
		idao.close(); 
	}

	public void insertHierarchy() throws SQLException, NamingException{
		String nameS;
		IDAO hdao = DAOManager.getInstance().getDAO("T_Herencias");
		hdao.setCommit(false);
		hdao.open();
		Iterator itClases=owlModel.getUserDefinedOWLNamedClasses().iterator();
		T_HerenciasDAO therdao = (T_HerenciasDAO)hdao.getDAO();
		T_Herencias ther = new T_Herencias();

		while(itClases.hasNext())
		{
			OWLNamedClass cls = (OWLNamedClass) itClases.next();

			String name=cls.getLocalName();
			if(name.startsWith("Axiom_"))
				continue;

			//herencias trivial de si misma (hace las reglas más faciles de implementar)
			ther.setID_TO(this.hmIDxName.get(name));
			ther.setID_TO_Padre(this.hmIDxName.get(name));
			therdao.insert(ther);
			//fin inclusion herencias triviales

			//parámetro a false para que solo devuelva los padres directos 
			//true devuelve los padres,abuelos,...)
			for (Iterator itSup=cls.getNamedSuperclasses(true).iterator(); itSup.hasNext();)
			{
				RDFResource sc=(RDFResource)itSup.next();
				nameS=sc.getLocalName();

				//            	if(nameS.equals(Constants.CLS_ENUMERATED)){
				//            		this.enumeratedclasess.add(name);
				//            	}else if(nameS.equals(Constants.CLS_ACTION)){
				//            		this.actionclasess.add(name);
				//            	}
				if(!nameS.equals("Thing")){//EXCLUIMOS LA HERENCIA TRIVIAL DE THING
					//System.out.println("[buildTHerencias]:_"+name+"("+this.getHmIDxName().get(name)+")\t"+nameS+"("+this.getHmIDxName().get(nameS)+")");
					ther.setID_TO(this.hmIDxName.get(name));
					ther.setID_TO_Padre(this.getHmIDxName().get(nameS));
					therdao.insert(ther);
				}
			}
		}

		//Insertamos las herencias a las abstractas virtuales. Consiste en ser padre de los rangos originales y de los especializados de esos rangos
		for(String nameVirtualAbstract:mapVirtualAbstract.keySet())
		{
			//herencia trivial de si misma
			ther.setID_TO(this.hmIDxName.get(nameVirtualAbstract));
			ther.setID_TO_Padre(this.hmIDxName.get(nameVirtualAbstract));
			therdao.insert(ther);

			//Lista para evitar que se inserten dos veces la misma herencia ya que el rango multiple puede estar formado por un rango que es especializado de otro rango
			HashSet<Integer> specializedAdded=new HashSet<Integer>();
			for(String nameSpecialized:mapVirtualAbstract.get(nameVirtualAbstract)){
				int idtoSpecialized=this.getHmIDxName().get(nameSpecialized);
				if(!specializedAdded.contains(idtoSpecialized)){
					ther.setID_TO(idtoSpecialized);
					therdao.insert(ther);
					specializedAdded.add(idtoSpecialized);
					Iterator itr=hdao.getAllCond(" id_to_padre="+idtoSpecialized).iterator();
					while(itr.hasNext()){
						T_Herencias herencia=(T_Herencias)itr.next();
						if(!specializedAdded.contains(herencia.getID_TO())){
							ther.setID_TO(herencia.getID_TO());
							therdao.insert(ther);
							specializedAdded.add(herencia.getID_TO());
						}/*else{
            				System.err.println("WARNING: Hay un rango multiple en el que uno de ellos '"+nameSpecialized+"' tiene un hijo "+herencia.getID_TO()+" que es especializado del otro para la abstracta virtual "+nameVirtualAbstract);
            			}*/
					}
				}else{
					System.err.println("WARNING: Hay un rango multiple en el que uno de ellos '"+nameSpecialized+"' es especializado del otro para la abstracta virtual "+nameVirtualAbstract);
				}
			}
		}
		hdao.commit();
		hdao.close();
	}

	public /*LinkedHashMap <String,Integer>*/void buildIdsProperties (ArrayList<String> listanombres) throws SQLException, NamingException{
		//LinkedHashMap <String,Integer> mapa=new LinkedHashMap <String,Integer>();
		IDAO idao = DAOManager.getInstance().getDAO("properties");
		//idao.setCommit(false);
		idao.open();
		dynagent.common.basicobjects.Properties tProperties;
		PropertiesDAO propertiesDao = (PropertiesDAO) idao.getDAO();
		for(int i=0;i<listanombres.size();i++){
			String name=listanombres.get(i);
			int idProp;
			tProperties = propertiesDao.getPropertyByName(name); 
			if(tProperties == null){ //No se encuentra la propiedad en la tabla Properties. Obtenemos un idProp disponible y la insertamos	
				if(hmPropiedades.containsKey(name)){
					idProp=hmPropiedades.get(name);
				}else{
					Integer cidProp=Constants.getIdConstantProp(name);
					if(cidProp!=null){
						idProp=cidProp;	
						//System.out.println("   idProp cte="+idProp);
					}
					else{
						maximumPropId++;
						idProp=maximumPropId;
						//System.out.println("   idProp lastpk="+idProp);
					}
					hmPropiedades.put(name, idProp);
					hmPropiedadesNew.put(name, idProp);
				}
			}
			else{  //La property ya está en la tabla, obtenemos su idProp y actualizamos sus campos con la información del modelo
				//idProp=tProperties.getPROP();
				if(!hmPropiedades.containsKey(name)){
					//Una propiedad existe en la base de datos pero no en el archivo de ids
					Integer cidProp=Constants.getIdConstantProp(name);
					if(cidProp!=null){
						idProp=cidProp;	
						//System.out.println("   idProp cte="+idProp);
					}
					else{
						maximumPropId++;
						idProp=maximumPropId;
						//System.out.println("   idProp lastpk="+idProp);
					}
					hmPropiedades.put(name, idProp);
					hmPropiedadesNew.put(name, idProp);
				}else{
					idProp=hmPropiedades.get(name);
				}
				
				//System.err.println("   la propiedad "+name+"  ya existe en la tabla properties, su idProp="+idProp);
			}

			//mapa.put(name, idProp);
		}
		//idao.commit();
		idao.close();
		//return mapa;
	}


	/**
	 * 
	 * @param owlModel
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private void insertProperties() throws SQLException, NamingException{

		ArrayList<String>listanamepropiedades=new ArrayList<String>();
		// Float minInclusive = null,maxInclusive = null;
		IDAO idao = DAOManager.getInstance().getDAO("properties");
		idao.setCommit(false);
		idao.open();
		dynagent.common.basicobjects.Properties tProperties;
		PropertiesDAO propertiesDao = (PropertiesDAO) idao.getDAO();

		Collection properties=owlModel.getUserDefinedOWLProperties();

		//obtenemos los nombres de todas las propiedades
		for (Iterator itP = properties.iterator(); itP.hasNext();){
			OWLProperty owlprop=(OWLProperty)itP.next();
			String name=owlprop.getLocalName();
			listanamepropiedades.add(name);
		}

		//construimos los ids de las propiedades
		this.buildIdsProperties(listanamepropiedades);

		//COMPROBAMOS SI SE HA DEFINIDO EL RDN EN EL MODELO (EN ESE CASO ESTARIA EN LA LISTA). SI NO ES ASÍ HAY QUE INTRODUCIR EL REGISTRO QUE DEFINE EL RDN
		//y si no está ya en la tabla properiteies
		Integer idproprdn=propertiesDao.getIdPropByName(Constants.PROP_RDN);
		if(idproprdn==null){//no está insertado aún rdn en properties, se insertará
			Properties prdn = new Properties();
			prdn.setPROP(Constants.IdPROP_RDN);
			prdn.setNAME(Constants.PROP_RDN);
			prdn.setCAT(2);
			prdn.setVALUECLS(Constants.IDTO_STRING);
			//prdn.setOP(Constants.OP_INTERSECTION);
			propertiesDao.insert(prdn);	
		}
		else{
			//System.out.println("\n.. info, la tabla properties ya tiene rdn definido");
		}
		if(!hmPropiedades.containsKey(Constants.PROP_RDN)){
			hmPropiedadesNew.put(Constants.PROP_RDN, Constants.IdPROP_RDN);
		}
		//System.out.println("  mapa propiedades="+this.getHmPropiedades());


		for (Iterator itPs = properties.iterator(); itPs.hasNext();){
			OWLProperty owlprop=(OWLProperty)itPs.next();
			Properties pp=this.traslateToBBDFormat(owlprop);
			// Insertamos el registro de la propiedad con toda la información.
			tProperties = propertiesDao.getPropertyByName(pp.getNAME()); 
			if(tProperties == null){
				propertiesDao.insert(pp);
			}else{
				boolean wasDataProperty=new Category(tProperties.getCAT()).isDataProperty();
				boolean isDataProperty=new Category(pp.getCAT()).isDataProperty();
				if(wasDataProperty==isDataProperty){//Si las dos son DataProperty u ObjectProperty modificamos sus datos directamente por si ha habido algun cambio en ellos
					propertiesDao.set(pp);
				}else{
					//Si ha cambiado de DataProperty a ObjectProperty o al contrario borramos de la tabla en la que está e insertamos en la correcta
					propertiesDao.delete(tProperties);
					propertiesDao.insert(pp);
				}
			}
		}
		
		idao.commit();
		idao.close();
		
		Iterator<String> itClases=allInstances.keySet().iterator();
		IDAO idaoc = DAOManager.getInstance().getDAO("Clases");
		TClaseDAO claseDao = (TClaseDAO) idaoc.getDAO();
		claseDao.open();
		TPropiedadClaseDAO clasePropiedadDao=new TPropiedadClaseDAO();
		clasePropiedadDao.open();
		clasePropiedadDao.deleteAll();
		while(itClases.hasNext())
		{
			String name=itClases.next();
			TClase clase = claseDao.getTClaseByName(name); 
			Iterator<Instance> itr=allInstances.get(name).iterator();
			HashSet<String> processedProp=new HashSet<String>();
			while(itr.hasNext()){
				String propName=itr.next().getPROPERTY();
				if(!processedProp.contains(propName)){
					tProperties = propertiesDao.getPropertyByName(propName);					
					TPropiedadClase clasePropiedad=new TPropiedadClase(clase.getTableId(), tProperties.getTableId(), tProperties.getVALUECLS()!=null);
					clasePropiedadDao.insert(clasePropiedad);
					processedProp.add(propName);
				}
			} 
		}
		clasePropiedadDao.commit();
		clasePropiedadDao.close();
		claseDao.commit();
		claseDao.close();

		
	}



	private Properties traslateToBBDFormat(OWLProperty owlprop){
		String name=owlprop.getLocalName();	
		int idProp = 0;
		String tipoPropiedad,mask; 
		Properties pp=new Properties();
		mask = null;
		RangeItem rangeitem = new RangeItem();
		tipoPropiedad=owlprop.getRDFType().getLocalName();
		if(owlprop.getRange()!=null&&owlprop.getRDFType()!=null){
			RDFResource rdfRang=(RDFResource)owlprop.getRange();
			//REGULAR EXPRESSIONS 

			RDFSDatatype dataRange=owlprop.getRangeDatatype();
			if(tipoPropiedad.equals("DatatypeProperty"))
			{	if(dataRange.getPattern()!=null){
				mask=dataRange.getPattern();
			}
			//minInclusive=dataRange.dataRange.getMaxExclusive();
			//dataRange.getMinExclusive();
			//LinkedList <RangeItem> lista=this.traslateDataRange(rdfRang, owlprop);
			//if(lista.size()>0)
			rangeitem=OWLAux.traslateDataRange(rdfRang, owlprop).getFirst();
			//else{
			//System.out.println("  WARNING: OWLPARSER.traslateDataRange  de prop="+owlprop.getLocalName()+"   rdfRang="+rdfRang+"  no ha construido nada");
			//}
			}
			else{
				//	rangeitem=this.traslateObjectRange(rdfRang).getFirst();
			}
		}
		//Categorias de la propiedad (inverse,symetric,transitive,play,...  ;
		Category category=OWLAux.buildCategory(owlprop);
		Integer cat=category.getCat();	 

		pp.setPROP(hmPropiedades.get(name));
		pp.setCAT(cat);
		pp.setNAME(name);
		/*if(rangeitem.getQMAX()!=null){
	    	pp.setQMAX(Float.valueOf(rangeitem.getQMAX()));
	    }
	    if(rangeitem.getQMIN()!=null){
	    	pp.setQMIN(Float.valueOf(rangeitem.getQMIN()));
	    }
	    if(rangeitem.getVALUE()!=null){
	    	pp.setVALUE(rangeitem.getVALUE());
	    }*/
		if(category.isDataProperty()){//solo metemos el tipo de las dataproerties en la tabla properties. La mascara solo tiene sentido en las dataproperty
			//pp.setMASK(mask); //TODO Esto ya se hace en el xml. De toda maneras si quisieramos tener en cuenta alguna mascara dicha en el modelo habria que crear un registro en la tabla masks
			if(rangeitem.getVALUECLS()!=null){
				pp.setVALUECLS(new Integer(getIdClassFromName(rangeitem.getVALUECLS())));
			}
		}
		//	    if(rangeitem.getOP()!=null){
		//	    	pp.setOP(rangeitem.getOP());
		//	    }

		//PROP INVERSA
		RDFProperty propinv=owlprop.getInverseProperty();
		if(propinv!=null){
			//System.out.print("  inversa de "+owlprop+"  es "+propinv.getLocalName());
			String nameinv=propinv.getLocalName();
			//obtenemos el idprop de la inversa
			pp.setPROPINV(hmPropiedades.get(nameinv));
		}

		Iterator<String> itcoment=owlprop.getComments().iterator();
		while(itcoment.hasNext()){
			Object comment=itcoment.next();
			if(comment instanceof DefaultRDFSLiteral){
				String language=((DefaultRDFSLiteral)comment).getLanguage();
				if(language!=null){
					String description=((DefaultRDFSLiteral)comment).getBrowserText();
					HelpProperty helpProperty=new HelpProperty();
					helpProperty.setDescription(description);
					helpProperty.setLanguage(language);
					this.mapHelpProperties.put(name,helpProperty);
				}
			}else{
				//System.out.println("  coment="+comment);
				String  [] comentarios=((String)comment).split("#");
				for(int i=0;i<comentarios.length;i++){
					String comentario=comentarios[i];
					int indexvalue=comentario.indexOf("=");
					String svalue=comentario.substring(indexvalue+1);
					//				if(comentario.startsWith("length")){//TODO Esto ya se hace en el xml. De toda maneras si quisieramos tener en cuenta alguna longitud dicha en el modelo habria que crear un registro en la tabla masks
					//					if(Auxiliar.hasIntValue(svalue)){
					//						int lenght=new Integer(svalue).intValue();
					//						pp.setLENGTH(new Integer(svalue));
					//						
					//						/*------------cambiamos la condiciones de memo deducido por longitud por memo declarado expresamente pq la longitud da problemas gráficos al tomarlo como ancho
					//						y construir memos que pichan mucho ancho
					//						if(lenght>=Constants.MIN_LENGHT_MEMO){
					//							pp.setVALUECLS(Constants.IDTO_MEMO);
					//							lpropiedadesMemo.add(name);
					//						}-------------*/
					//					}
					//				}
					/*else*/ if(comentario.toLowerCase().startsWith("memo")){
						pp.setVALUECLS(Constants.IDTO_MEMO);
						lpropiedadesMemo.add(name);
					}else if(comentario.startsWith("access")){
						//System.out.print(" access="+svalue);
					}else if(comentario.toLowerCase().startsWith("image")){
						pp.setVALUECLS(Constants.IDTO_IMAGE);
						lpropiedadesImage.add(name);
					}else if(comentario.toLowerCase().startsWith("file")){
						pp.setVALUECLS(Constants.IDTO_FILE);
						lpropiedadesFile.add(name);
					}else if(comentario.toLowerCase().startsWith("shared")){
						category.setShared();
						pp.setCAT(category.getCat());
					}else{
						if(!comentario.isEmpty() && !comentario.toLowerCase().startsWith("vmin") && !comentario.toLowerCase().startsWith("vmax")){
							System.err.println("WARNING: Posiblemente se ha definido un comentario para la property "+name+" sin indicar el idioma");
						}
					}
				}
			}
		}



		return pp;
	}

	public Instance buildRdnInstance(String nameClase,String nameIndividuo){
		Instance ins=new Instance(nameClase,nameIndividuo,Constants.PROP_RDN,nameIndividuo,Constants.DATA_STRING,null,null,null,nameClase);
		return ins;
	}

	public void  buildInstances(OWLModel owlModel){
		String className,idto,prop;

		for (Iterator it =owlModel.getUserDefinedOWLNamedClasses().iterator(); it.hasNext();)
		{
			OWLNamedClass cls = (OWLNamedClass) it.next();
			className=cls.getLocalName();
			if(className.startsWith("Axiom_"))
				continue;
			idto=className;
			this.getAllInstances().put(className,new HashSet<Instance>());
			//System.out.println("[OWLParser]:_=================="+nameClase+"=============================");

			Iterator<String> itcoment=cls.getComments().iterator();
			while(itcoment.hasNext()){ 
				Object comment=itcoment.next();
				if(comment instanceof DefaultRDFSLiteral){
					String language=((DefaultRDFSLiteral)comment).getLanguage();
					if(language!=null){
						String description=((DefaultRDFSLiteral)comment).getBrowserText();
						HelpClass helpClass=new HelpClass();
						helpClass.setDescription(description);
						helpClass.setLanguage(language);
						this.mapHelpClasses.put(className,helpClass);
					}
				}else{
					//System.out.println("  coment="+comment);
					String  [] comentarios=((String)comment).split("#");
					for(int i=0;i<comentarios.length;i++){
						String comentario=comentarios[i];
						int indexvalue=comentario.indexOf("=");
						String svalue=comentario.substring(indexvalue+1);
						if(comentario.startsWith("access")&&svalue.equalsIgnoreCase("abstract")){
							//Buscamos todos los especializados con parametro true para que no se quede solo en el primer nivel ya que esa información la
							//utilizamos luego en buildVirtualPropertiesOfAbstract para completar esa abstracta con todos sus hijos, no solo los del primer nivel
							Iterator<OWLNamedClass> itr=(Iterator<OWLNamedClass>) cls.getNamedSubclasses(true).iterator();
							HashSet<String> specialized=new HashSet<String>();
							while(itr.hasNext()){
								OWLNamedClass subCls=itr.next();
								specialized.add(subCls.getLocalName());
							}
							this.abstractclasess.put(className,specialized);
						}else if(!comentario.isEmpty()){
							System.err.println("WARNING: Posiblemente se ha definido un comentario para la clase "+className+" sin indicar el idioma");
						}
					}
				}
			}

			for (Iterator itSup=cls.getNamedSuperclasses(true).iterator(); itSup.hasNext();)
			{
				RDFResource sc=(RDFResource)itSup.next();

				String nameS=sc.getLocalName();

				if(nameS.equals(Constants.CLS_ENUMERATED)){
					this.enumeratedclasess.add(className);
				}else if(nameS.equals(Constants.CLS_ACTION)){
					this.actionclasess.add(className);
				}
			}

			//Propiedades de esta clase
			//System.out.println("[OWLParser]:_"+nameClase+"  tiene "+cls.getAssociatedProperties().size()+"   propiedades."); 

			Collection cPropiedades=cls.getAssociatedProperties();

			//SIEMPRE INSERTAMOS UN REGISTRO QUE LE ASIGNA A LA CLASE LA PROPIEDAD RDN
			//if(cPropiedades.size()==0){
			//if(!heredaParams(cls)){	
			this.getAllInstances().get(className).addAll(buildRdnInstances(className));
			//}
			//System.out.println("    INFO: La clase "+nameClase+"  no tiene propiedades");
			//}

			//PROPIEDADES DE LA CLASE
			for(Iterator itp=cPropiedades.iterator();itp.hasNext();)
			{  
				LinkedList <RangeItem> lRangeItems = new LinkedList <RangeItem>();
				RDFProperty rdfProp=(RDFProperty)itp.next();
				String namePropiedad=rdfProp.getLocalName();

				//identificador entero de la propiedad
				prop=namePropiedad;

				String tipoPropiedad=rdfProp.getRDFType().getLocalName();
				//System.out.println("[OWLParser]:_      -"+namePropiedad+" ("+prop+")  es: "+tipoPropiedad);




				//El rango que aplica para una propiedad en una clase es el definido con AllValuesFrom o el definido en P si no se hizo ninguna restriccion de ste tipo
				RDFResource rdfRng=cls.getAllValuesFrom(rdfProp);



				//if (rdfRng==null&&!hasRestricctionHasValue(cls,rdfProp)&&!cls.getLocalName().equals("UTASK")&&!cls.getLocalName().equals("ACTION")&&!cls.getLocalName().equals("EXPORT")) // No tenemos rango para esa propiedad en esta clase
				if (rdfRng==null&&!OWLAux.hasRestricctionHasValue(cls,rdfProp)) // No tenemos rango para esa propiedad en esta clase
				{
					if(!namePropiedad.equals(Constants.PROP_SOURCECLASS)&&!namePropiedad.equals(Constants.PROP_TARGETCLASS)&&!namePropiedad.equals(Constants.PROP_ITERATOR)&&!namePropiedad.equals(Constants.PROP_CONFIRMED_SOURCE)){
						System.out.println("[OWLParser]:_       WARNING: No se ha asignado ningún rango a: "+namePropiedad);
					}
					Instance ins=new Instance();
					ins.setIDTO(className);
					ins.setPROPERTY(String.valueOf(prop));
					this.getAllInstances().get(className).add(ins);
				}

				else if(rdfRng!=null&&!OWLAux.hasRestricctionHasValue(cls,rdfProp))  //si tiene restriccion hasValue su rango se contruye en el metodo que procesa las restricciones
				{
					if(tipoPropiedad.equals("ObjectProperty")){
						lRangeItems=OWLAux.traslateObjectRange(rdfRng);
					}else{ //es dataProperty
						lRangeItems=OWLAux.traslateDataRange(rdfRng,rdfProp);	
					}
				}

				if(cls.getRestrictions(rdfProp, true).size()>0)
				{
					lRangeItems.addAll(OWLAux.traslateRestrictions(cls,rdfProp));
				}


				/*if(!*/OWLAux.checkPosibleProblemsCardinality(lRangeItems, tipoPropiedad, namePropiedad, className);/*){*/
				HashSet<String> specialized=new HashSet<String>();
				for (int i=0;i<lRangeItems.size();i++){
					RangeItem ri=lRangeItems.get(i);

					//Si el rango tiene UNION es que es multiple, por lo que almacenamos sus nombres para mas abajo crearle una abstracta virtual
					if(Auxiliar.equals(ri.getOP(),Constants.OP_UNION)){
						specialized.add(ri.getVALUECLS());
					}else{
						Instance ins=new Instance();
						ins.setIDTO(String.valueOf(idto));
						ins.setPROPERTY(String.valueOf(prop));
						ins.setOP(ri.getOP());
						ins.setQMAX(ri.getQMAX());
						ins.setQMIN(ri.getQMIN());
						ins.setVALUE(ri.getVALUE());
						ins.setVALUECLS(ri.getVALUECLS());
						this.getAllInstances().get(className).add(ins);
					}
				}

				//Creacion de abstracta virtual
				if(!specialized.isEmpty()){
					//Creamos el rango hacia la clase abstracta virtual
					//System.out.println("CREADA ABSTRACTA VIRTUAL para idto:"+idto+" prop:"+prop+" valueCls:"+multipleRangeName+" specialized:"+specialized);

					String nameVirtualAbstract=createVirtualAbstractToRange(className, prop, specialized, false);
					mapVirtualAbstract.put(nameVirtualAbstract, specialized);//Información necesaria para crear las herencias en buildTHerencias. Tener en cuenta que specialized solo tiene los especializados de primer nivel
				}
				/*}*/
			}        
		}
		buildVirtualPropertiesOfAbstracts(owlModel,abstractclasess);
		//System.out.println("...DEBUG getAllIndividualInstances:\n "+Auxiliar.IteratorToStringByRows(this.getAllIndividualInstances().iterator()));
	}


	private String createVirtualAbstractToRange(String className, String prop, HashSet<String> specialized, boolean virtual) {
		//Ordenamos para que siempre aparezca el mismo nombre para el rango de una property
		ArrayList<String> specializedAux=new ArrayList<String>(specialized);
		Collections.sort(specializedAux, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				return Constants.languageCollator.compare(o1,o2);
			}
			
		});

		Instance ins=new Instance();
		ins.setIDTO(className);
		ins.setPROPERTY(String.valueOf(prop));
		ins.setOP(Constants.OP_INTERSECTION);
		ins.setVIRTUAL(virtual);

		String nameVirtualAbstract="";
		for(String nameClass:specializedAux){
			if(!nameVirtualAbstract.isEmpty())
				nameVirtualAbstract+=", ";
			nameVirtualAbstract+=nameClass;
		}

		// Evitamos que el nombre sea mayor que 50 ya que en la base de datos tenemos configurado ese tamaño máximo. Para eso cortamos su tamaño y añadimos ~Numero de repeticion 
		if(nameVirtualAbstract.length()>50){
			int i=1;
			String nameVirtualAbstractAux=null;
			boolean exit=false;
			while(!exit){
				nameVirtualAbstractAux=nameVirtualAbstract.substring(0, 48)+"~"+i;
				exit=true;
				if(mapVirtualAbstract.containsKey(nameVirtualAbstractAux)){
					if(!specialized.equals(mapVirtualAbstract.get(nameVirtualAbstractAux))){
						exit=false;
						i++;
					}
				}
			}
			nameVirtualAbstract=nameVirtualAbstractAux;
		}


		ins.setVALUECLS(nameVirtualAbstract);
		this.getAllInstances().get(className).add(ins);

		//Le creamos el rdn a la clase abstracta virtual
		//	        			this.getAllInstances().put(nameVirtualAbstract,new HashSet<Instance>());
		//	        			this.getAllInstances().get(nameVirtualAbstract).addAll(buildRdnInstances(nameVirtualAbstract));

		this.abstractclasess.put(nameVirtualAbstract,specialized);
		return nameVirtualAbstract;
	}

	/**
	 * Se encarga de añadirle a una clase abstracta las properties que no tiene de las hijas.
	 * Para ello le pone como cardinalidad la menos restrictiva y como rango crea una abstracta virtual si los rangos de esa property para cada hijo no son iguales o especializados uno del otro.
	 * Si esa propiedad ya existía en el padre no se hace nada
	 * @param owlModel
	 * @param mapAbstract mapa de nombre de abstracta y especializados de esa abstracta
	 */
	private void buildVirtualPropertiesOfAbstracts(OWLModel owlModel, LinkedHashMap<String,HashSet<String>> mapAbstract){
		LinkedHashMap<String,HashSet<String>> mapAbstractAux=(LinkedHashMap<String,HashSet<String>>)mapAbstract.clone();//Para evitar el concurrentModification
		LinkedHashMap<String,HashSet<String>> mapAbstractNew=new LinkedHashMap<String, HashSet<String>>();
		ArrayList<String> listSystemClass=new ArrayList<String>(Arrays.asList(Constants.LIST_SYSTEM_CLASS_NAME));
		listSystemClass.add(Constants.CLS_SYSTEM_CLASS);
		for(String className:mapAbstractAux.keySet()){
			if(!listSystemClass.contains(className)){//No nos interesan añadir properties en clases de sistema

				//Cogemos todas las propiedades originales de la abstracta para saber que esas tenemos que mantenerlas
				HashSet<String> propertiesOriginalClass=new HashSet<String>();
				if(this.getAllInstances().containsKey(className)){
					for(Instance instance:this.getAllInstances().get(className)){
						propertiesOriginalClass.add(instance.getPROPERTY());
					}
				}

				//Cogemos los Instance ya creados para esa abstracta por estar en el modelo
				HashSet<Instance> listInstances=this.getAllInstances().get(className);
				if(listInstances==null){
					listInstances=new HashSet<Instance>();
					this.getAllInstances().put(className, listInstances);
				}

				//Nos quedamos con los especializados que no sean abstractos
				HashSet<String> specializedWithoutAbstract=getSpecializedWithoutAbstracts(mapAbstractAux.get(className));
				//System.err.println(className+" specialized:"+specializedWithoutAbstract);

				//Cogemos de cada especializado los facts de Cardinalidad y los de modelo(and y oneof) 
				HashMap<String,HashSet<Instance>> mapPropCard=new HashMap<String, HashSet<Instance>>();
				HashMap<String,HashSet<Instance>> mapPropAnd=new HashMap<String, HashSet<Instance>>();
				HashMap<String,HashSet<Instance>> mapPropOneOf=new HashMap<String, HashSet<Instance>>();

				for(String specializedName:specializedWithoutAbstract){
					for(Instance instanceSpecialized:this.getAllInstances().get(specializedName)){
						if(!propertiesOriginalClass.contains(instanceSpecialized.getPROPERTY())){
							if(instanceSpecialized.getOP().equals("CAR")){
								if(!mapPropCard.containsKey(instanceSpecialized.getPROPERTY()))
									mapPropCard.put(instanceSpecialized.getPROPERTY(), new HashSet<Instance>());
								mapPropCard.get(instanceSpecialized.getPROPERTY()).add(instanceSpecialized);
							}else if(instanceSpecialized.getOP().equals("AND")){
								if(!mapPropAnd.containsKey(instanceSpecialized.getPROPERTY()))
									mapPropAnd.put(instanceSpecialized.getPROPERTY(), new HashSet<Instance>());
								mapPropAnd.get(instanceSpecialized.getPROPERTY()).add(instanceSpecialized);
							}else if(instanceSpecialized.getOP().equals("ONEOF")){
								if(!mapPropOneOf.containsKey(instanceSpecialized.getPROPERTY()))
									mapPropOneOf.put(instanceSpecialized.getPROPERTY(), new HashSet<Instance>());
								mapPropOneOf.get(instanceSpecialized.getPROPERTY()).add(instanceSpecialized);
							}
						}
					}
				}

				//Calculamos los facts de cardinalidad para cada property en cada especializado, quedandonos con la menos restrictiva
				for(String prop:mapPropCard.keySet()){
					HashSet<Instance> list=mapPropCard.get(prop);
					//Si no fuera igual significaria que hay alguno sin cardinalidad por lo que no necesitamos que tenga ningun instance ya que su no existencia
					//indica que no tiene cardinalidad definidad, lo que seria lo menos restrictivo
					if(list.size()==specializedWithoutAbstract.size()){
						Instance instance=null;
						for(Instance instanceSpecialized:list){
							if(instance==null){
								instance=instanceSpecialized.clone();
								instance.setIDTO(className);
								instance.setNAME(className);
								instance.setVIRTUAL(true);
							}else{
								if(instance.getQMIN()!=null){
									if(instanceSpecialized.getQMIN()!=null){
										if(Double.valueOf(instance.getQMIN()).doubleValue()>Double.valueOf(instanceSpecialized.getQMIN()).doubleValue())
											instance.setQMIN(instanceSpecialized.getQMIN());
									}else{
										instance.setQMIN(null);
									}
								}

								if(instance.getQMAX()!=null){
									if(instanceSpecialized.getQMAX()!=null){
										if(Double.valueOf(instance.getQMAX()).doubleValue()<Double.valueOf(instanceSpecialized.getQMAX()).doubleValue())
											instance.setQMAX(instanceSpecialized.getQMAX());
									}else{
										instance.setQMAX(null);
									}
								}
							}
						}
						if(instance.getQMIN()!=null || instance.getQMAX()==null){
							listInstances.add(instance);
						}
					}
				}

				//Calculamos los facts and, teniendo en cuenta que si el rango de una property no es compatible en cada una de los especializados tendremos que crear una abstracta virtual
				for(String prop:mapPropAnd.keySet()){
					HashSet<Instance> list=mapPropAnd.get(prop);
					Instance instance=null;
					HashSet<String> specialized=new HashSet<String>();
					for(Instance instanceSpecialized:list){
						if(instance==null){
							instance=instanceSpecialized.clone();
							instance.setIDTO(className);
							instance.setNAME(className);
							instance.setVIRTUAL(true);
						}
						if(!specialized.contains(instanceSpecialized.getVALUECLS())){
							String valueCls=instanceSpecialized.getVALUECLS();
							Iterator<String> itr=((HashSet<String>)specialized.clone()).iterator();
							specialized.add(valueCls);
							//Comprobamos que valueCls tenga parentesco con alguno de los ya encontrados. Nos sirve para quitar de especializados los que son hijos entre si, quedandonos con el que es superior
							while(itr.hasNext() && specialized.contains(valueCls)/*Si no lo contiene significa que se ha encontrado otro compatible, por lo que no hay que continuar comparando con el resto*/){
								String sp=itr.next();
								if(this.mapVirtualAbstract.containsKey(sp) || this.mapVirtualAbstract.containsKey(valueCls)){//Si la especializada o unique es una abstracta virtual
									if(this.mapVirtualAbstract.containsKey(sp) && this.mapVirtualAbstract.get(sp).contains(valueCls)){//Si uno de sus especializados es el unique que cogimos, la abstracta virtual seria la compatible
										specialized.remove(valueCls);
									}else if(this.mapVirtualAbstract.containsKey(valueCls) && this.mapVirtualAbstract.get(valueCls).contains(sp)){//Si uno de los especializados de unique es el especializado, la abstracta virtual seria la compatible
										specialized.remove(sp);
									}else if(this.mapVirtualAbstract.containsKey(sp) && this.mapVirtualAbstract.containsKey(valueCls)){//Si las dos son abstractas virtuales nos quedamos con sus especializados
										specialized.remove(sp);
										specialized.remove(valueCls);
										specialized.addAll(this.mapVirtualAbstract.get(sp));
										specialized.addAll(this.mapVirtualAbstract.get(valueCls));
									}
								}else{
									//Buscamos en los especializados y en los padres para saber si valueCls aparece en alguno de ellos, lo que indicaria que son compatibles
									//OWLNamedClass cls=(OWLNamedClass)owlModel.getClsesWithMatchingBrowserText(sp, new ArrayList(), 1).iterator().next();
									OWLNamedClass cls=null;
									for (Iterator it =owlModel.getUserDefinedOWLNamedClasses().iterator(); it.hasNext();)
									{
											OWLNamedClass clsTmp = (OWLNamedClass) it.next();
											String nameClase=clsTmp.getLocalName();
											if(nameClase.equals(sp)){
												cls=clsTmp;
												break;
											}
									}
									
									Iterator<OWLNamedClass> itrSub=(Iterator<OWLNamedClass>) cls.getNamedSubclasses(true).iterator();
									boolean found=false;
									while(!found && itrSub.hasNext()){
										OWLNamedClass subCls=itrSub.next();
										if(subCls.getLocalName().equals(valueCls)){
											found=true;
											specialized.remove(valueCls);
										}
									}

									if(!found){
										Iterator<OWLNamedClass> itrSuper=(Iterator<OWLNamedClass>) cls.getNamedSuperclasses(true).iterator();
										while(!found && itrSuper.hasNext()){
											OWLNamedClass superCls=itrSuper.next();
											if(superCls.getLocalName().equals(valueCls)){
												found=true;
												specialized.remove(sp);
											}
										}
									}
								}
							}
						}
					}
					//Si hemos encontrado rangos distintos creamos una abstracta virtual si todavia no existe
					if(specialized.size()>1){
						//Si el rango no es el mismo, ni compatible, en todos los especializados creariamos una abstracta virtual
						String nameVirtualAbstract=createVirtualAbstractToRange(className, prop, specialized, true);
						if(!this.mapVirtualAbstract.containsKey(nameVirtualAbstract)){//Si ya esta en el mapa significa que no se ha llegado a crear si no que se ha reutilizado una ya existente
							mapAbstractNew.put(nameVirtualAbstract, specialized);//Lo añadimos para al final del método crear las nuevas abstractas virtuales
							this.mapVirtualAbstract.put(nameVirtualAbstract, specialized);//Información necesaria para crear las herencias en buildTHerencias
						}
					} else if(specialized.size()==1){//Si el rango es unico significa que o es igual en todos los especializados o hemos encontrado uno compatible
						instance.setVALUECLS(specialized.iterator().next());
						listInstances.add(instance);
					}
				}

				//Calculamos los facts oneof teniendo en cuenta no meterlos repetidos
				for(String prop:mapPropOneOf.keySet()){
					HashSet<Instance> list=mapPropOneOf.get(prop);
					ArrayList<Instance> instances=new ArrayList<Instance>();
					for(Instance instanceSpecialized:list){
						Instance instance=instanceSpecialized.clone();
						instance.setIDTO(className);
						instance.setNAME(className);
						instance.setVIRTUAL(true);

						if(!instances.contains(instance)){
							listInstances.add(instance);
							instances.add(instance);
						}
					}
				}
			}
		}

		//Volvemos a llamar si se ha creado alguna virtualAbstract en el rango de alguna property
		if(!mapAbstractNew.isEmpty())
			buildVirtualPropertiesOfAbstracts(owlModel,mapAbstractNew);

	}

	private HashSet<String> getSpecializedWithoutAbstracts(HashSet<String> specialized){
		HashSet<String> result=new HashSet<String>();
		for(String specializedName:specialized){
			if(abstractclasess.containsKey(specializedName)){
				result.addAll(getSpecializedWithoutAbstracts(abstractclasess.get(specializedName)));
			}else{
				result.add(specializedName);
			}
		}
		return result;
	}

	private LinkedList<Instance> buildRdnInstances(String className){
		LinkedList<Instance> list=new LinkedList<Instance>();
		Instance insrdn=new Instance();
		insrdn.setIDTO(className);
		insrdn.setNAME(className);
		insrdn.setPROPERTY(Constants.PROP_RDN);
		insrdn.setOP(Constants.OP_INTERSECTION);
		insrdn.setVALUECLS(Constants.DATA_STRING);
		list.add(insrdn);
		Instance insrdnCar=new Instance();
		insrdnCar.setIDTO(className);
		insrdnCar.setPROPERTY(String.valueOf(Constants.PROP_RDN));
		insrdnCar.setOP(Constants.OP_CARDINALITY);
		insrdnCar.setQMIN("1");
		insrdnCar.setQMAX("1");
		list.add(insrdnCar);

		return list;
	}

	private void updateIdoValueIndividualInstance(int oldIdoValue,int newIdoValue) throws SQLException, NamingException{
		ArrayList<String> sqlList=new ArrayList<String>();
		sqlList.add("UPDATE instances SET IDO="+newIdoValue+" WHERE IDO ="+oldIdoValue) ;
		/*Tener en cuenta que esto puede ser un problema si el value es de una dataProperty ya que podria cambiar un valor que haya,
		aunque es muy raro que tenga como valor un numero negativo como pasa con los objectProperty. Ademas en el modelo no se le esta dando valores a dataProperty*/
		sqlList.add("UPDATE instances SET VALUE='"+newIdoValue+"' WHERE VALUE ='"+oldIdoValue+"'");
		AuxiliarQuery.dbExecUpdate(fcdb, sqlList, true);
		//System.err.println("....info: se han actualizado los individuos de instances");
	}

	public LinkedHashMap<String, Integer> getHmIDxName() {
		return hmIDxName;
	}


	public void setHmIDxName(LinkedHashMap<String, Integer> hmIDxName) {
		this.hmIDxName = hmIDxName;
	}


	public ArrayList<String> getLpropiedadesMemo() {
		return lpropiedadesMemo;
	}

	public void setLpropiedadesMemo(ArrayList<String> lpropiedadesMemo) {
		this.lpropiedadesMemo = lpropiedadesMemo;
	}

	public void insertHelp() throws SQLException, NamingException{

		Iterator<String> itrClasses=mapHelpClasses.keySet().iterator();
		
		ConnectionDB conDB=fcdb.createConnection(true); 
		Connection conn=conDB.getDataBaseConnNotReusable("dynaglobal");
		try{
			while(itrClasses.hasNext()){
				String name=itrClasses.next();
				HelpClass helpClass=mapHelpClasses.get(name);
				helpClass.setIdto(hmIDxName.get(name));
	
				String sql = "INSERT INTO helpClasses(NAME, DESCRIPTION, LANGUAGE) VALUES('"+name+"','"+helpClass.getDescription()+"','"+helpClass.getLanguage()+"')";
	
				//System.out.println("sql="+sql);
				AuxiliarQuery.dbExecUpdate(conn, sql, true);
				//System.out.println("insert ok");
			}
	
			Iterator<String> itrProperties=mapHelpProperties.keySet().iterator();
			while(itrProperties.hasNext()){
				String name=itrProperties.next();
				HelpProperty helpProperty=mapHelpProperties.get(name);
				helpProperty.setIdProp(hmPropiedades.get(name));
	
				String sql = "INSERT INTO helpProperties(NAME, DESCRIPTION, LANGUAGE) VALUES('"+name+"','"+helpProperty.getDescription()+"','"+helpProperty.getLanguage()+"')";
	
				//System.out.println("sql="+sql);
				AuxiliarQuery.dbExecUpdate(conn, sql, true);
				//System.out.println("insert ok");
			}
			
			Iterator<String> itrIndividuals=mapHelpIndividuals.keySet().iterator();
			while(itrIndividuals.hasNext()){
				String name=itrIndividuals.next();
				HelpIndividual helpIndividual=mapHelpIndividuals.get(name);
				//helpIndividual.setIdo(hmIndividual.get(name));
				//helpIndividual.setIdto(hmIDxName.get(helpIndividual.getClassName()));
	
				String sql = "INSERT INTO helpIndividuals(NAME, CLASS, DESCRIPTION, LANGUAGE) VALUES('"+name+"','"+helpIndividual.getClassName()+"','"+helpIndividual.getDescription()+"','"+helpIndividual.getLanguage()+"')";
	
				//System.out.println("sql="+sql);
				AuxiliarQuery.dbExecUpdate(conn, sql, true);
				//System.out.println("insert ok");
			}
		}finally{
			fcdb.close(conDB);
			conn.close();
		}
	}

	public void insertIndividualOfAction(InstanceService instanceService) throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException{
		//Lo insertamos como individuos en la base de datos elegida

		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		Iterator<String> itr=this.actionclasess.iterator();
		while(itr.hasNext()){
			String className=itr.next();

			Integer idto = tdao.getTClaseByName(Constants.CLS_ACTION_INDIVIDUAL).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=className;

			if(InstanceService.getIdo(fcdb, dataBaseMap, idto, rdn, false)==null){//Si no existe ya lo insertamos
				list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));

				int prop = propDAO.getIdPropByName(Constants.PROP_RESERVED_ID);
				int id = this.getHmIDxName().get(className);
				list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(id), new Double(id), null, null, action.NEW));
			}
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
		tdao.close();
	}

	public LinkedHashMap<String, HashSet<String>> getAbstractclasess() {
		return abstractclasess;
	}

	public void setAbstractclasess(LinkedHashMap<String, HashSet<String>> abstractclasess) {
		this.abstractclasess = abstractclasess;
	}

	public boolean checkPosibleProblems(Instance ins){
		boolean fallo=false;
		if(ins.getVALUECLS()==null&&ins!=null&&ins.getOP().equals(Constants.OP_CARDINALITY)){
			System.err.println("   WARNING: instancia con VALUECLS=null");
			fallo=true;
		}
		return fallo;
	}

}




