package dynagent.tools.importers.owl;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.Auxiliar;
import dynagent.common.xml.GetData;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.FactoryConnectionDB;

public class XMLModelImport {
	private int idMinBusinessRestriction=12000;
	public int idIndividual=10100;
	private HashMap <String,Integer> hmIndividuos;
	public boolean generarIds=false;
	private int nextIdBusiness(){
		return idMinBusinessRestriction++;
	}
	
	private HashMap<String,Integer>hmidbusinessRestriction=new  HashMap<String,Integer>();
	
	public String getIdBusinessRestriction(String name){
		if(hmidbusinessRestriction.containsKey(name)){
			return (hmidbusinessRestriction.get(name)).toString();
		}
		else{
			int ido=this.nextIdBusiness();
			hmidbusinessRestriction.put(name, ido);
			return String.valueOf(ido);
		}
	}
	
	
	public  int  newIdo(){
		return idIndividual++;
	}
	
	public String getIdIndividual(String individual){
		if(hmIndividuos.containsKey(individual)){
			return (hmIndividuos.get(individual)).toString();
		}
		else{
			int ido=this.newIdo();
			hmIndividuos.put(individual, ido);
			return String.valueOf(ido);
		}
	}
	
	//NO USAR ESTOS METODOS YA QUE EN OWLPARSER NO FUNCIONA YA COMO ESTO ESPERA, YA QUE NO INSERTA PROPERTIES O CLASES CON INFO COMPLETA
//	public Instance traslateInstanceToNumeric(Instance ins) throws SQLException, NamingException{
//		Instance insCod=new Instance();
//		if(insCod.getIDO()!=null){
//			if(ins.getOP()!=null){//es una businessclass
//				insCod.setIDO(this.getIdBusinessRestriction(ins.getIDO()));
//			}
//			else{//ES UN INDIVIDUO
//				insCod.setIDO(this.getIdIndividual(ins.getIDO()));
//		}
//		insCod.setIDTO(OWLParser.getIdClase(ins.getIDTO()).toString());
//		insCod.setVALUECLS(OWLParser.getIdClase(ins.getIDTO()).toString());
//		insCod.setNAME(ins.getNAME());
//		insCod.setOP(ins.getOP());
//		
//		}
//		insCod.setPROPERTY(OWLParser.getIdPropiedad(ins.getPROPERTY()).toString());
//		insCod.setQMAX(ins.getQMAX());
//		insCod.setQMIN(ins.getQMIN());
//		insCod.setVALUE(ins.getVALUE());
//		return insCod;
//	}
//	
//	
//	
//	public LinkedList<Instance> traslateToNumeric(LinkedList<Instance>instances) throws SQLException, NamingException{
//		LinkedList<Instance>instancesnum=new LinkedList<Instance>();
//		for(int i=0;i<instances.size();i++){
//			instancesnum.add(this.traslateInstanceToNumeric(instances.get(i)));
//		}
//		return instancesnum;
//	}
	
	public static void main(String args[]) throws SystemException, RemoteSystemException, CommunicationException 
	{
		int nbusiness = 4;
		String ip="192.168.1.3";
		String gestor="SQLServer";

		String snbusiness = new Integer(nbusiness).toString();
		String resp=Auxiliar.leeTexto("\n  IP="+ip+"   \n Desea que numero_empresa="+snbusiness+"?");
		if(!resp.equalsIgnoreCase("S")&&!resp.equalsIgnoreCase("SI")){
			do {
				snbusiness = Auxiliar.leeTexto("Introduzca el número de empresa con el que desea trabajar");
			} while (!Auxiliar.hasIntValue(snbusiness));
		}
		nbusiness = new Integer(snbusiness);
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);

		String nameFile=Auxiliar.leeTexto("Introduzca el nombre del fichero donde esta la definición (relativo a la ruta "+Constants.relativePathOWLFILES+")");
		GetData gd=new GetData(nameFile);
		XMLModelImport xmlm=new XMLModelImport();
		LinkedList<Instance> instances=gd.getInstances();
		/*
		 * comentado PARA GENERAR JAR DE RULEENGINE, CLASE EN IMPLEMENTACION!
		 * LinkedList<Instance>businessClass=gd.getBusinessClasses();
		LinkedList<Access> access=gd.getAccesses();
		LinkedList<T_Herencias> herencias=gd.getHierarchies();
		LinkedList<Properties> properties=gd.getProperties();
		System.out.println("--------------------------INFO OBTENIDA DEL XML:-------------------------------");
		System.out.println("INSTANCES:"+Auxiliar.LinkedListToString(instances));
		System.out.println("PROPIEDADES:"+Auxiliar.LinkedListToString(properties));
		System.out.println("HERENCIAS:"+Auxiliar.LinkedListToString(herencias));
		System.out.println("ACCESOS:"+Auxiliar.LinkedListToString(access));
		System.out.println("BUSINESSCLASS:"+Auxiliar.LinkedListToString(businessClass));
		
		resp=Auxiliar.leeTexto("\n  IP="+ip+"   \n Desea traducir a identificadores numéricos="+snbusiness+"?");
		if(!resp.equalsIgnoreCase("S")&&!resp.equalsIgnoreCase("SI")){
			businessClass=
		
		}
		
		
		
		
		
		
		
		
		xmlm.traslateToNumeric(
		OWLParser.insertListInstances());*/
		
	}


}
