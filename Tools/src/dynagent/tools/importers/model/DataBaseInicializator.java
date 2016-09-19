package dynagent.tools.importers.model;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
import dynagent.common.knowledge.Category;
import dynagent.server.database.dao.AccessDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.tools.parsers.uni.auxiliar.InsercionInstance;

public class DataBaseInicializator {
	
	/**
	 * Build and insert into a database an arraylist which contains basic types and generic classes
	 * 
	 * @return ArrayList<TClase> A list where all basic classes such as rol, relation, etc and all basic types are built
	 * and inserted in data base
	 */
		private static ArrayList<TClase> buildBasicClasses(){
			ArrayList<TClase> res = new ArrayList<TClase>();
			res.add(new TClase(Constants.IDTO_BOOLEAN, Constants.DATA_BOOLEAN));
			res.add(new TClase(Constants.IDTO_DATE, Constants.DATA_DATETIME));
			res.add(new TClase(Constants.IDTO_TIME, Constants.DATA_TIME));
			res.add(new TClase(Constants.IDTO_INT, Constants.DATA_INT));
			res.add(new TClase(Constants.IDTO_STRING, Constants.DATA_STRING));
			res.add(new TClase(Constants.IDTO_DOUBLE, Constants.DATA_DOUBLE));
			res.add(new TClase(Constants.IDTO_MEMO, Constants.DATA_MEMO));
			res.add(new TClase(Constants.IDTO_ESTADOREALIZACION, Constants.CLS_ESTADO_REALIZACION));
			res.add(new TClase(Constants.IDTO_FUNCTIONAL_AREA, Constants.CLS_FUNCTIONAL_AREA));
			res.add(new TClase(Constants.IDTO_INSTANT, Constants.CLS_INSTANT));
			res.add(new TClase(Constants.IDTO_THING, Constants.CLS_THING));
			res.add(new TClase(Constants.IDTO_UNIDADES, Constants.CLS_UNIDADES));
			res.add(new TClase(Constants.IDTO_UTASK, Constants.CLS_UTASK));
			res.add(new TClase(Constants.IDTO_USERROL, Constants.CLS_USERROL));
			res.add(new TClase(Constants.IDO_BEGIN1970, Constants.BEGIN1970));
			res.add(new TClase(Constants.IDO_BEGINDAY, Constants.BEGINDAY));
			
			return res;
			
		}
		
		/**
		 * Build and insert into a database an arraylist which contains all basic properties
		 * @return
		 */
		
		private static ArrayList<Properties> buildBasicProperties(){
			
			ArrayList<Properties> res = new ArrayList<Properties>();
			Category catdatap=new Category();
			catdatap.setDataProperty();
			Category catobjectp=new Category();
			catobjectp.setObjectProperty();
			
			res.add(new Properties(new Integer(Constants.IdPROP_ESTADOREALIZACION), Constants.PROP_ESTADOREALIZACION, catobjectp.getCat(), Constants.IDTO_ESTADOREALIZACION, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_LOGO), Constants.PROP_LOGO, catdatap.getCat(), Constants.IDTO_STRING, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_MYFUNCTIONALAREA), Constants.PROP_MYFUNCTIONALAREA, catobjectp.getCat(), Constants.IDTO_FUNCTIONAL_AREA, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_RDN), Constants.PROP_RDN, catdatap.getCat(), Constants.IDTO_STRING, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_TARGETCLASS), Constants.PROP_TARGETCLASS, catobjectp.getCat(), null, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_USERROL), Constants.PROP_USERROL, catobjectp.getCat(), Constants.IDTO_USERROL, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_OPERATION), Constants.PROP_OPERATION, catdatap.getCat(), Constants.IDTO_STRING, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_ASIGNDATE), Constants.PROP_ASIGNDATE, catdatap.getCat(), Constants.IDTO_DATE, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_TOPDATE), Constants.PROP_TOPDATE, catdatap.getCat(), Constants.IDTO_DATE, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_EJECUTEDATE), Constants.PROP_EJECUTEDATE, catdatap.getCat(), Constants.IDTO_DATE, Constants.OP_INTERSECTION));
			res.add(new Properties(new Integer(Constants.IdPROP_OWNER), Constants.PROP_OWNER, catdatap.getCat(), Constants.IDTO_STRING, Constants.OP_INTERSECTION));
			
			return res;
		}
		
		
		/**
		 * Build and insert into a database an arraylist wich contains instances of all basic classes and properties
		 * @return
		 */
		private static ArrayList<Instance> buildBasicInstances(){
			ArrayList<Instance> res = new ArrayList<Instance>();
			
			// Clases sin properties (Propertiesless' Classes)
			
			res.add(new Instance(new Integer(Constants.IDTO_THING).toString(), Constants.CLS_THING, Constants.OP_INTERSECTION));
			res.add(new Instance(new Integer(Constants.IDTO_INSTANT).toString(), Constants.CLS_INSTANT, Constants.OP_INTERSECTION));
			res.add(new Instance(new Integer(Constants.IDTO_UNIDADES).toString(),Constants.CLS_UNIDADES, Constants.OP_INTERSECTION));
			
			// Clases con propiedad rdn (Classes with rdn property inherited)
			

			
			res.add(new Instance(new Integer(Constants.IDTO_FUNCTIONAL_AREA).toString(), new Integer(Constants.IdPROP_RDN).toString(), null, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_FUNCTIONAL_AREA, Constants.OP_INTERSECTION));
			res.add(new Instance(new Integer(Constants.IDTO_ESTADOREALIZACION).toString(), new Integer(Constants.IdPROP_RDN).toString(), null, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_ESTADO_REALIZACION, Constants.OP_INTERSECTION));
			
			res.add(new Instance(new Integer(Constants.IDTO_USERROL).toString(), new Integer(Constants.IdPROP_RDN).toString(), null, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_USERROL, Constants.OP_INTERSECTION));
			
			// Clase UTASK con todos sus propiedades y sus posibles valores (Definition of Utask class : properties and values at whole)
			
			
			// Propiedad rdn
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_RDN).toString(), null, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			
			// Propiedad myFunctionalArea 
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_MYFUNCTIONALAREA).toString(), null, new Integer(Constants.IDTO_FUNCTIONAL_AREA).toString(), Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			
			// Propiedades de fecha
			
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_TOPDATE).toString(), null, new Integer(Constants.IDTO_DATE).toString(), Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_ASIGNDATE).toString(), null, new Integer(Constants.IDTO_DATE).toString(), Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_EJECUTEDATE).toString(), null, new Integer(Constants.IDTO_DATE).toString(), Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			
			// Propiedad owner
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_OWNER).toString(), null, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			
			// Propiedad targetClass
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_TARGETCLASS).toString(), null, null, Constants.CLS_UTASK, Constants.OP_INTERSECTION));
			
		
			//Propiedad operation
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_OPERATION).toString(), Constants.ACCESS_CONCRT_NAME, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_UNION));
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_OPERATION).toString(), Constants.ACCESS_SET_NAME, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_UNION));
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_OPERATION).toString(), Constants.ACCESS_VIEW_NAME, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_UNION));
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_OPERATION).toString(), Constants.ACCESS_NEW_NAME, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_UNION));
			res.add(new Instance(new Integer(Constants.IDTO_UTASK).toString(), new Integer(Constants.IdPROP_OPERATION).toString(), Constants.ACCESS_DEL_NAME, new Integer(Constants.IDTO_STRING).toString(), Constants.CLS_UTASK, Constants.OP_UNION));
			
			return res;
			
			
		}
		
		private static ArrayList<Instance> buildBasicIndividuals(){
			
			ArrayList<Instance> res = new ArrayList<Instance>();
			//InsercionInstance ii = new InsercionInstance();
			//res.add(ii.createInstanceIndividuo(Constants.IDTO_INSTANT, Constants.IDO_BEGIN1970, Constants.BEGIN1970, Constants.IDTO_STRING, Constants.IdPROP_RDN, -1, -1));
			//res.add(ii.createInstanceIndividuo(Constants.IDTO_INSTANT, Constants.IDO_BEGINDAY, Constants.BEGINDAY, Constants.IDTO_STRING, Constants.IdPROP_RDN, -1, -1));
			//res.add(ii.createInstanceIndividuo(Constants.IDTO_USERROL, Constants.IDO_USERROL_SYSTEM, Constants.USERROL_SYSTEM, Constants.IDTO_STRING, Constants.IdPROP_RDN, -1, -1));
			return res;
		}
		
		
		/**
		 * Build and fill tables Clases, instances and properties which have been defined in the upper functions
		 * It makes the connection too.
		 * @throws NamingException 
		 * @throws SQLException 
		 *
		 */
		private static void buildAllBasicStuff(int business) throws SQLException, NamingException{
			DAOManager.getInstance().setBusiness(new Integer(business).toString());
			FactoryConnectionDB fcdb = null;
			fcdb = new FactoryConnectionDB(business,true,"192.168.1.3","SQLServer");
			DAOManager.getInstance().setFactConnDB(fcdb);
			DAOManager.getInstance().setCommit(true);


			Iterator clasesIterator = buildBasicClasses().iterator();
			
			TClaseDAO tcdao = new TClaseDAO();
			tcdao.open();
			while(clasesIterator.hasNext()){
				TClase tc = (TClase) clasesIterator.next();
				System.out.println(tc);
				tcdao.insert(tc);
			}
			tcdao.close();
			
			Iterator propertiesIterator = buildBasicProperties().iterator();
			
			PropertiesDAO pdao = new PropertiesDAO();
			pdao.open();
			while(propertiesIterator.hasNext()){
				Properties pr = (Properties) propertiesIterator.next();
				System.out.println(pr);
				pdao.insert(pr);
			}
			pdao.close();
			
			ArrayList<Instance> instances = buildBasicInstances();
			instances.addAll(buildBasicIndividuals());
			Iterator instancesIterator = instances.iterator();

			InstanceDAO idao = new InstanceDAO();
			
			idao.open();
			while(instancesIterator.hasNext()){
				Instance ins = (Instance) instancesIterator.next();
				idao.insert(ins);
				System.out.println(ins);
			}
			idao.close();
		}
		
		public static void run() throws SQLException, NamingException{

			Iterator clasesIterator = buildBasicClasses().iterator();
			
			TClaseDAO tcdao = new TClaseDAO();
			tcdao.open();
			while(clasesIterator.hasNext()){
				TClase tc = (TClase) clasesIterator.next();
				System.out.println(tc);
				tcdao.insert(tc);
			}
			tcdao.close();
			
			Iterator propertiesIterator = buildBasicProperties().iterator();
			
			PropertiesDAO pdao = new PropertiesDAO();
			pdao.open();
			while(propertiesIterator.hasNext()){
				Properties pr = (Properties) propertiesIterator.next();
				System.out.println(pr);
				pdao.insert(pr);
			}
			pdao.close();
			
			ArrayList<Instance> instances = buildBasicInstances();
			instances.addAll(buildBasicIndividuals());
			Iterator instancesIterator = instances.iterator();

			InstanceDAO idao = new InstanceDAO();
			
			idao.open();
			while(instancesIterator.hasNext()){
				Instance ins = (Instance) instancesIterator.next();
				idao.insert(ins);
				System.out.println(ins);
			}
			idao.close();
		}
		
		/**
		 * Just to prove
		 * @param args
		 * @throws NamingException 
		 * @throws SQLException 
		 */
		
		public static void main(String args[]) throws SQLException, NamingException{
			
			
			/*
			DAOManager.getInstance().setBusiness("5");
			poolDB p=null;
			try {
				p = new poolDB("192.168.1.3",true,true,null);
				DAOManager.getInstance().setPoolDB(p);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			PropertiesDAO pdao = new PropertiesDAO();
			Properties pr = new Properties();
			pr.setCAT(12);
			pr.setLENGTH(24);
			pr.setPROP(0);
			System.out.println(pdao.getValuesNotNull(pr));
			pdao.open();
			pdao.set(pr);
			pdao.close();
			*/
			
			//buildAllBasicStuff(0);
			
			DAOManager.getInstance().setBusiness(new Integer(11).toString());
			FactoryConnectionDB fcdb = null;
			fcdb = new FactoryConnectionDB(11,true,"192.168.1.3","SQLServer");
			DAOManager.getInstance().setFactConnDB(fcdb);
			DAOManager.getInstance().setCommit(true);
			
			AccessDAO idao = new AccessDAO();
			idao.open();
		
			System.out.println(idao.getAll());
			idao.close();
		}
}
		
