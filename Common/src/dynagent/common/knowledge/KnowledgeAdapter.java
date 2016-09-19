/***
 * KnowledgeAdpater.java
 * 
 * @author  Jose Antonio Zamora -jazamora@ugr.es
 * @description Implementación de los métodos que adpatan la información entre los distintos tipos
 * de objetos de comunicación de dynagent. 
 * 				
 */


package dynagent.common.knowledge;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
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
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.ImageValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;

public class KnowledgeAdapter {

	private IKnowledgeBaseInfo ik;

	public KnowledgeAdapter(IKnowledgeBaseInfo ik) {
		this.ik = ik;
	}
	public IKnowledgeBaseInfo getIk() {
		return ik;
	}

	public void setIk(IKnowledgeBaseInfo ik) {
		this.ik = ik;
	}

	/**
	 * Deduce el objeto Property correspondiente a un objeto o tipo de objeto a
	 * partir de los facts (instances...) que hay en motor sobre él. También
	 * obtiene el access correspondiente de esa property en función del contexto
	 * (userRok,user,usertask)
	 * 
	 * @param: LinkedList <dynagent.common.knowledge.IPropertyDef> lista de
	 *         instances
	 * @param: Integer userRol: identificador numérico del userRol (puede ser
	 *         null)
	 * @param: String user: login del usuario (puede ser null)
	 * @param: Integer usertask: identificador numérico del utask (puede ser
	 *         null)
	 * @return: Property: objeto Property.java construido
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws NotFoundException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
	public PropertyValue getPropertyValueOf(LinkedList<IPropertyDef> instances, String name,String mask,Integer length, Integer userRol,
			String user, Integer usertask) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException  {
		Property pr = null;
		if (instances.size() > 0) {
			dynagent.common.knowledge.IPropertyDef fp = instances.get(0);
			try {
				if (ik.isDataProperty(fp.getPROP())) {
					pr = this.buildDataPropertyOf(instances, name, mask, length);
				} else if (ik.isObjectProperty(fp.getPROP())) {
					pr = this.buildObjectPropertyOf(instances);
				}
			
			} catch (NotFoundException e) {
				e.printStackTrace();
				return null;
			}

		}
		return pr;
	}
	
	
	public Property getPropertyDefOf(LinkedList<IPropertyDef> instances, String name,String mask,Integer length, Integer userRol,
			String user, Integer usertask) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException  {
		Property pr = null;
		if (instances.size() > 0) {
			dynagent.common.knowledge.IPropertyDef fp = instances.get(0);
			if (ik.isDataProperty(fp.getPROP())) {
				pr = this.buildDataPropertyOf(instances, name, mask, length);
			} else if (ik.isObjectProperty(fp.getPROP())) {
				pr = this.buildObjectPropertyOf(instances);
			}			
		}
		return pr;
	}
	
	
	/**
	 * Obtiene una Property a partir de sus instances. 
	 * IMPORTANTE ESTA PROPERTY SERÁ DE TIPO DEFINICIÓN O DE TIPO VALOR
	 * POR TANTO LAS INSTANCES QUE LE LLEGEN DEBEN TENER O SOLO VALORES (OP NULO) O SOLO CARACTERISTICAS (OP!=nULL)
	 * @param instances
	 * @param userRol
	 * @param user
	 * @param usertask
	 * @return
	 * @throws OperationNotPermitedException 
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws NotFoundException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */public Property getPropertyOf(LinkedList<IPropertyDef> instances, String name,String mask,Integer length, Integer userRol,	String user, Integer usertask) throws OperationNotPermitedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 boolean instancesHomogeneos=false;
		 boolean opNulo=false;
		 boolean opNoNulo=false;
		 for(int i=0;i<instances.size();i++){
			 if(instances.get(i).getOP()!=null){
				 opNoNulo=true;
			 }
			 else{
				 opNulo=true;
			 } 
		 }
		 instancesHomogeneos=!(opNulo&&opNulo);
		if(instancesHomogeneos&&opNoNulo){
			return this.getPropertyDefOf(instances, name, mask, length, userRol, user, usertask);
			
		}
		else{
			return (Property)this.getPropertyValueOf(instances, name, mask, length, userRol, user, usertask);
		}
		
	}
	
	

	/**
	 * Agrupa una serie de facts en subgrupos que tienen en común los valores de
	 * los slots que se le indican.
	 * 
	 * @param allfacts:
	 *            todos los facts
	 * @param slots:
	 *            lista de nombre de los slots por los que se quiere agrupar.
	 * @return: Lista de listas. Cada una de la sublistas representa una de las
	 *          agrupaciones.
	 */
	public LinkedList<LinkedList> groupByDistinctSlotValues(
			LinkedList<dynagent.common.knowledge.IPropertyDef> allfacts,
			LinkedList<String> slots) {
		LinkedList<LinkedList> finalgroups = new LinkedList<LinkedList>();
		HashSet distinctSlotValues = new HashSet();
		HashMap<String, Integer> hmIndexValues = new HashMap<String, Integer>();
		int m = 0;
		for (int i = 0; i < allfacts.size(); i++) {
			String svalues = this.getStringRepresentationOfSlotValues(slots,
					allfacts.get(i));
			if (!distinctSlotValues.contains(svalues)) {
				distinctSlotValues.add(svalues);
				hmIndexValues.put(svalues, m);
				LinkedList<dynagent.common.knowledge.IPropertyDef> listaM = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
				listaM.add(allfacts.get(i));
				finalgroups.add(m, listaM);
				m++;
			} else {
				Integer index = hmIndexValues.get(svalues);
				finalgroups.get(index).add(allfacts.get(i));
			}
		}
		return finalgroups;
	}
	

	/**
	 * Agrupa la serie de facts relativos a un objeto o clase en uno o varios
	 * subgrupos correspondientes a la información correspondiente de un objeto
	 * Property (idto,ido,rol,clsrel,idoRel comunes).
	 * 
	 * @param allfacts
	 * @return: Lista de lista donde cada una las sublistas corresponde a los
	 *          facts homógeneos.
	 */
	public LinkedList<LinkedList> groupFactsByProperty(
			LinkedList<dynagent.common.knowledge.IPropertyDef> allfacts) {
		LinkedList<LinkedList> finalgroups = new LinkedList<LinkedList>();
		LinkedList<String> slots = new LinkedList<String>();
		slots.add("IDTO");
		slots.add("PROPERTY");
		slots.add("IDO");
		finalgroups = this.groupByDistinctSlotValues(allfacts, slots);
		return finalgroups;
	}
	

	/**
	 * Construye un objeto dataValue a partir de un objeto FactProp
	 * 
	 * @param FactProp
	 * @return DataValue si el fact tiene algún valor o null sino tiene ningún
	 *         valor.
	 * @throws NotFoundException 
	 */
	public DataValue buildDataValue(dynagent.common.knowledge.IPropertyDef fp) throws NotFoundException, IncoherenceInMotorException  {
		DataValue dv = null;
		if (fp.getVALUECLS() != null && (fp.getQMAX() != null || fp.getQMIN() != null || fp.getVALUE() != null)) {
			int valueCls=fp.getVALUECLS().intValue();
			
			if (ik.isUnit(fp.getVALUECLS())){
				if(fp.getQMAX() != null || fp.getQMIN() != null) {
					UnitValue uv = new UnitValue();
					uv.setValueMax(fp.getQMAX());
					uv.setValueMin(fp.getQMIN());
					uv.setUnit(fp.getVALUECLS());
					dv = uv;
				}
			} else if (valueCls==Constants.IDTO_INT){
				if(fp.getQMAX() != null || fp.getQMIN() != null) {
					IntValue iv = new IntValue();
					if (fp.getQMAX() != null)
						iv.setValueMax(fp.getQMAX().intValue());
					if (fp.getQMIN() != null)
						iv.setValueMin(fp.getQMIN().intValue());
					dv = iv;
				}
			} else if (valueCls==Constants.IDTO_DOUBLE){
				if(fp.getQMAX() != null || fp.getQMIN() != null) {
					DoubleValue fv = new DoubleValue();
					fv.setValueMax(fp.getQMAX());
					fv.setValueMin(fp.getQMIN());
					dv = fv;
				}
			} else if ((valueCls==Constants.IDTO_STRING)||valueCls==Constants.IDTO_MEMO||valueCls==Constants.IDTO_IMAGE||valueCls==Constants.IDTO_FILE) {
				if(fp.getVALUE() != null){	
					StringValue sv = new StringValue();
					sv.setValue(fp.getVALUE());
					dv = sv;
				}
			} else if (valueCls==Constants.IDTO_DATE||valueCls==Constants.IDTO_DATETIME||valueCls==Constants.IDTO_TIME){
				if(fp.getQMAX() != null && fp.getQMIN()!=null) {
				//	if(fp.getQMAX().equals(fp.getQMIN())){
						TimeValue tv = new TimeValue();
						tv.setRelativeSecondsMin(fp.getQMIN().longValue());
						tv.setRelativeSecondsMax(fp.getQMAX().longValue());
						dv = tv;
				//	}
				}
			} else if (valueCls==Constants.IDTO_BOOLEAN){
				if(fp.getQMAX() != null && fp.getQMIN() != null) {
					BooleanValue bv = new BooleanValue();
					if (fp.getQMAX() != null
							&& (fp.getQMAX().equals(fp.getQMIN()))) {
						int ibool = fp.getQMAX().intValue();
						if (ibool == Constants.ID_BOOLEAN_FALSE) {
							bv.setBvalue(false);
						} else if (ibool == Constants.ID_BOOLEAN_TRUE) {
							bv.setBvalue(true);
						}
						
						bv.setComment(fp.getVALUE());
						dv = bv;
					}
				}
			}
			
			if(dv!=null){
				dv.setEqualToValue(!Auxiliar.equals(fp.getOP(),Constants.OP_NEGATION));
			}
			else {
				boolean casoContemplado = 
					ik.isUnit(fp.getVALUECLS())
					|| valueCls==Constants.IDTO_BOOLEAN
					|| valueCls==Constants.IDTO_STRING
					|| valueCls==Constants.IDTO_DOUBLE
					|| valueCls==Constants.IDTO_INT
					|| valueCls==Constants.IDTO_DATETIME
					|| valueCls==Constants.IDTO_DATE
					|| valueCls==Constants.IDTO_TIME
					|| valueCls==Constants.IDTO_MEMO
					|| valueCls==Constants.IDTO_IMAGE
					|| valueCls==Constants.IDTO_FILE;
				if(casoContemplado){
					System.err.println("      WARNING:   Situación no contemplada en buildDataValule para fact=:"+ fp.toString());
				}
			}
		}

		return dv;
	}

	
	
	public Value buildValue(dynagent.common.knowledge.IPropertyDef fp) throws NotFoundException, IncoherenceInMotorException   {
		
		Value val=null;
		if(ik.isDataProperty(fp.getPROP())){
			val=this.buildDataValue(fp);
		}
		else if(ik.isObjectProperty(fp.getPROP())){
			val=this.buildObjectValue(fp);
		}
		return val;
		
		
	
	}
	
	
	
	
	/**
	 * Construye un objeto ObjectValue a partir de un objeto FactProp
	 * 
	 * @param FactProp
	 * @return ObjectValue
	 */
	public ObjectValue buildObjectValue(dynagent.common.knowledge.IPropertyDef fp) {
		ObjectValue ov = null;
		if (fp.getVALUE() != null) {
			ov = new ObjectValue();
			ov.setValueCls(fp.getVALUECLS());
			
			ov.setValue(new Integer(fp.getVALUE()));
//			Integer id=null;
//			if(Auxiliar.hasIntValue(fp.getVALUE()))
//				 id= new Integer(fp.getVALUE());
//			else System.err.println("Warning buildObjectValue: fp.getValue() no es un entero:"+fp);
//			ov.setValue(id);
//			// comprobamos si se trata de un registro full (apuntando en value a
//			// una clase)
//			if (id.equals(fp.getVALUECLS())&&ik.isIDClass(id)) {
//				// si apunta a una clase recogemos el número de valores que
//				// estará en qmin=qmax
//				if (fp.getQMAX()!=null && fp.getQMIN()!=null && fp.getQMAX().equals(fp.getQMIN())) {
//					ov.setQ(fp.getQMIN().intValue());
//				} /*else {
//					System.err
//							.println("    WARNING: en buildObjectValue se está apuntando en full y qmin distinto de qmax; fact="
//									+ fp.toString());
//									*/
//				/*}*/
//			}
		}
		return ov;
	}

	/**
	 * Dado un objeto java ruler.fact construye una representación String de los
	 * valores de los slots que se les pasa como parámetro
	 * 
	 * @param slots:
	 *            LinkedList con los nombres de los slots
	 * @param fact:
	 *            objeto fact
	 * @return: representación de los valores en un String.
	 */
	private String getStringRepresentationOfSlotValues(
			LinkedList<String> slots, dynagent.common.knowledge.IPropertyDef fact) {
		String result = null, slotvalue = null;
		for (int i = 0; i < slots.size(); i++) {
			if (slots.get(i).equals("IDTO"))
				slotvalue = fact.getIDTO() + "#";
			else if (slots.get(i).equals("IDO"))
				slotvalue = fact.getIDO() + "#";
			else if (slots.get(i).equals("PROPERTY"))
				slotvalue = fact.getPROP() + "#";
			else if (slots.get(i).equals("OP"))
				slotvalue = fact.getOP() + "#";
			else {
				System.err.println("     WARNING:  Caso no implementado en getStringRepresentationOfSlotValues  para el slot="	+ slots.get(i));
			}
			if (result != null)
				result = result + slotvalue;
			else
				result = slotvalue;

		}
		return result;

	}

	
	

	 /**
	  * Actualiza la cardinalidad de un objeto Property a partir de la información de un factProp
	  * @param FactProp susceptible de tener información sobre cardinalidad más restrictiva.
	  * @param Property del que se quiere actualizar su cardinalidad
	  * @return: Property con la cardinalidad actualizada 
	  */
	public static Property actualizaCardinalidad (dynagent.common.knowledge.IPropertyDef fp,Property pr){
		if(fp.getOP()!=null&&fp.getOP().equals(Constants.OP_CARDINALITY)){
			if(fp.getQMIN()!=null&&pr.getCardMin()==null){
				pr.setCardMin(fp.getQMIN().intValue());
			}
			else if(fp.getQMIN()!=null&&pr.getCardMin()!=null&&fp.getQMIN()>pr.getCardMin())
			{
				pr.setCardMin(fp.getQMIN().intValue());
			}
			//QMAX				
			if(fp.getQMAX()!=null&&pr.getCardMax()==null){
				pr.setCardMax(fp.getQMAX().intValue());
			}
			else if(fp.getQMAX()!=null&&pr.getCardMax()!=null&&fp.getQMAX()<pr.getCardMax()){
					pr.setCardMax(fp.getQMAX().intValue());
			}
		}
		return pr;
	}	
	 
	
	
	
	
	 /**
	  * Construye un objeto DataProperty a partir de los facts que codifican su información.
	  * @param instances
	  * @return: DataProperty
	 * @throws NotFoundException 
	  */
	 public DataProperty buildDataPropertyOf(LinkedList<dynagent.common.knowledge.IPropertyDef> instances,String name,String mask,Integer length) throws NotFoundException, IncoherenceInMotorException {
		 DataProperty dpr=new DataProperty();
		 Iterator<dynagent.common.knowledge.IPropertyDef> itpr = instances.iterator();
		 Integer ido=instances.get(0).getIDO();
		 Integer idto=instances.get(0).getIDTO();
		 Integer valueCls=instances.get(0).getVALUECLS();
		 String OP=instances.get(0).getOP();
		 //HashSet<Integer>lvaluescls=new HashSet<Integer>();
		 dpr.setIdo(ido);
		 dpr.setIdto(idto);
		
		 int idProp = instances.get(0).getPROP();
		 /*if(idProp==186){
			 
			 System.err.println(" ka.buildDataPropertyOf( depurando 186");
		 }*/
		 dpr.setIdProp(idProp);
		 //información de la propiedad
		 if(valueCls!=null){
			 dpr.setDataType(valueCls);
		 }
		 else if(OP==null || !OP.equals(Constants.OP_CARDINALITY)){ 
			 System.err.println("   WARNING: ka.buildDataPropertyOf  obtiene con valuecls nulo,instances="+instances);
		 }
		 
		dpr.setName(name);
	
	 while(itpr.hasNext()){
			dynagent.common.knowledge.IPropertyDef fp = itpr.next();
			//CARDINALIDAD
			if(fp.getOP()!=null&&fp.getOP().equals(Constants.OP_CARDINALITY)){
				KnowledgeAdapter.actualizaCardinalidad(fp, dpr);
			}
			else if(fp.getVALUECLS()!=null)
			{  
				dpr.setDataType(fp.getVALUECLS().intValue());
				if(ik.isUnit(fp.getVALUECLS())){//UNIDADES
					dpr.setDataType(Constants.IDTO_DOUBLE);
				}
				//Construimos el valor (DataValue) a partir del fact
				
				DataValue dv=this.buildDataValue(fp);//traduce el fact a dataValue, puede ser nulo!!
				if(dv!=null){
					if(fp.getOP()==null){
						dpr.getValues().add(dv);
					}else if(fp.getOP().equals(Constants.OP_HASVALUE)){
						dpr.getValues().add(dv);
						dpr.setValuesFixed(true);
					}
					else if(fp.getOP().equals(Constants.OP_ONEOF)){
						dpr.getEnumList().add(dv);
					}
					else if(fp.getOP().equals(Constants.OP_UNION)){
						dpr.getEnumList().add(dv);
						System.out.println("...info de forma temporal buildDataPropertyOf considerara OR  y davalue=!null como enumerado-->migrar modelo datos a operado ONEOF\n datavalue="+dv);
					}
					else if(fp.getOP().equals(Constants.OP_INTERSECTION)){
						dpr.getEnumList().add(dv);
						System.out.println("...info de forma temporal buildDataPropertyOf considerara AND  y davalue=!null como enumerado-->migrar modelo datos a operado ONEOF\n    datavalue="+dv);
					}
					else if(fp.getOP().equals(Constants.OP_NEGATION)){
						dpr.getExcluList().add(dv);
					}
					else{
						//System.out.println(" Info: no se tiene en cuenta fp="+fp+"datavalue="+dv);
					}
					
					
				}
			}
	 }
	 
	 return dpr;
}
	 
	 
	 
	 public PropertyValue buildPropertyValueOf(LinkedList<dynagent.common.knowledge.IPropertyDef> instances) throws NotFoundException, IncoherenceInMotorException {
		 PropertyValue pr=null;
		 if(instances!=null&&instances.size()>0){
			  pr=new PropertyValue();
			 
		
			Iterator itpr = instances.iterator();
			Integer ido=instances.get(0).getIDO();
			Integer idto=instances.get(0).getIDTO();
			 pr.setIdo(ido);
			 pr.setIdto(idto);
			 int idProp = instances.get(0).getPROP();
			 pr.setIdProp(idProp);
			 pr.setName(ik.getPropertyName(idProp));
			while(itpr.hasNext()){
				dynagent.common.knowledge.IPropertyDef fp = (dynagent.common.knowledge.IPropertyDef)itpr.next();
				 if(fp.getVALUECLS()!=null)
				{  
					//Construimos el valor  a partir del fact
					Value v=this.buildValue(fp);//traduce el fact aValue, puede ser nulo!!
					if(fp.getOP()==null){//OPERADOR NO NULO
						if(v!=null ){
							pr.getValues().add(v);
						}
					}
					else{
						System.err.println("\n\n================WARNING: KnowledgeAdapter.buildPropertyValueOf tiene facts con OP no nulo. instances=\n"+instances); 	
					}
				}
			 
			}
		 }

	 return pr;
}
	 
	 
 
	 
	 public ObjectProperty buildObjectPropertyOf(LinkedList<dynagent.common.knowledge.IPropertyDef> instances) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
			ObjectProperty opr=new ObjectProperty();
			Integer ido=instances.get(0).getIDO();
			Integer idto=instances.get(0).getIDTO();
			opr.setIdo(ido);
			opr.setIdto(idto);
			Iterator itpr = instances.iterator();
			HashSet<Integer>lIntersections=new HashSet<Integer>();
			HashSet<Integer>lUnions=new HashSet<Integer>();
			HashSet<Integer>lDirectRanges=new HashSet<Integer>();
			
			//TODO Preguntar a Zamora si los enumerados deberian tener rangoList. Si es asi descomentar donde se usa en esta funcion
			/*HashSet<Integer>lOneOf=new HashSet<Integer>();*/
	
			//Información común:
			if(instances.size()>0){
				dynagent.common.knowledge.IPropertyDef fp1= (dynagent.common.knowledge.IPropertyDef)instances.get(0);
				opr.setIdProp(fp1.getPROP());
				opr.setName(ik.getPropertyName(fp1.getPROP()));
			}
				
			while(itpr.hasNext()){
				dynagent.common.knowledge.IPropertyDef fp = (dynagent.common.knowledge.IPropertyDef)itpr.next();
				
				if(fp.getOP()==null){//OPERADOR NULO: VALORES
					ObjectValue objvalue=this.buildObjectValue(fp);
					if(objvalue!=null){
						//VAMOS A CARGAR AQUELLOS INDIVIDUOS APUNTADOS POR OBJECTPROPERTY PARA ASEGURARNOS QUE LA INFO ESTA EN MOTOR PARA LAS REGLAS
						//TODO REVISAR SI ESTA ES LA POLÍTICA A SEGUIR, O SI LAS REGLAS VAN A TENER REGLAS AUXILIARES QUE VAYAN CARGANDO INFORMACIÓN.
//						if(objvalue!=null&&objvalue.getValue()!=null){
//							DocDataModel ddm=(DocDataModel)ik;
//							if(!ddm.existInMotor(objvalue.getValue()) && ddm.depthObjectProperty){
//								//System.err.println("\n\n==========debuggin: buildObjectProperty tiene un fact apuntando a "+objvalue.getValue()+"  que no esta en motor y se va a cargar");
//								ddm.getFromServer(objvalue.getValue(),null, ddm.getUser(), null);
//							}
//						}
						opr.getValues().add(objvalue);
					}
				}
				else{//OPERADOR NO NULO: CARACTERÍSTICAS
					if(fp.getOP().equals(Constants.OP_CARDINALITY)){
						KnowledgeAdapter.actualizaCardinalidad(fp, opr);
					}
					else if(fp.getOP().equals(Constants.OP_INTERSECTION)){	
						lIntersections.add(fp.getVALUECLS());
					}	
					else if(fp.getOP().equals(Constants.OP_ONEOF)){
						if(fp.getVALUECLS()!=null){//oneof,negation necesitan construir objectVAlue
							ObjectValue objvalue=this.buildObjectValue(fp);
							if (objvalue != null&&(objvalue.getValue() != null )) {
								opr.getEnumList().add(objvalue);
								/*lOneOf.add(fp.getVALUECLS());*/
							}
						}
					}else if(fp.getOP().equals(Constants.OP_NEGATION)){
						if(fp.getVALUECLS()!=null){//oneof,negation necesitan construir objectVAlue
							ObjectValue objvalue=this.buildObjectValue(fp);
							if(objvalue!=null){
								opr.getExcluList().add(objvalue);
							}
							else{
								System.err.println("       WARNING: KnowledegeAdapter: buildObjectProperty... Implementacion rango con NOT aún no realizada en buildPropertyOf. fact=: \n"+fp);
							}
						}	
					}
					else if(fp.getOP().equals(Constants.OP_HASVALUE)){
						ObjectValue objvalue=this.buildObjectValue(fp);
						if(objvalue!=null){
							opr.getValues().add(objvalue);
							opr.setValuesFixed(true);	
						}
						//System.err.println("OP HASVAL, fp="+fp+"\n value="+objvalue);
					}
					//QCR Restricciones de cardinalidad y rango específicos
					else if(fp.getOP().equals(Constants.OP_QUANTITYDETAIL)){
						QuantityDetail qcr = new QuantityDetail();
						if(fp.getQMAX()!=null)
							qcr.setCardinalityEspecifyMax(fp.getQMAX().intValue());
						if(fp.getQMIN()!=null)
							qcr.setCardinalityEspecifyMin(fp.getQMIN().intValue());
						qcr.setValue(fp.getVALUE());
						qcr.setValueCls(fp.getVALUECLS());
						opr.getQuantityDetailList().add(qcr);
					}
					else if(fp.getOP().equals(Constants.OP_UNION)){
						if(fp.getVALUE()!=null)
							lDirectRanges.add(Integer.valueOf(fp.getVALUE()));
						else{
							lUnions.add(fp.getVALUECLS());
						}
					}
					else if (fp.getOP().equals(Constants.OP_QUANTITYDETAIL)){
						QuantityDetail qcr=new QuantityDetail();
						if(fp.getQMAX()!=null)
							qcr.setCardinalityEspecifyMax(fp.getQMAX().intValue());
						if(fp.getQMIN()!=null)
							qcr.setCardinalityEspecifyMin(fp.getQMIN().intValue());
						if(fp.getVALUE()!=null)
							qcr.setValue(fp.getVALUE());
						qcr.setValueCls(fp.getVALUECLS());
						opr.getQuantityDetailList().add(qcr);	
					}
				}
			}	
			//Calcular intersecciones o uniones y añadir a rangoList
			if(lIntersections.size()>0&&lUnions.size()>0){
				//ConceptLogger.getLogger("buildPropertiesLog").writeln("        WARNING: No se ha calculado el rango en buildObjectProperty porque es complejo (mix de intersecciones y uniones) en la propiedad:");
				System.err.println("        WARNING: No se ha calculado el rango en buildObjectProperty porque es complejo (mix de intersecciones y uniones) en la propiedad:"+opr+"\nlUnions="+lUnions+"  lIntersections="+lIntersections);
			}
			else{
				if (lIntersections.size()==1) {
					opr.getRangoList().addAll(lIntersections);
				}
				else if (lIntersections.size() > 1) {
					opr.getRangoList().addAll(this.getIntersectionOf((new LinkedList(lIntersections))));
				}
				else if(lDirectRanges.size()>0){
					opr.getRangoList().addAll(lDirectRanges);
				}
				else if(lUnions.size() == 1){
					opr.getRangoList().addAll(lUnions);
				}
				else if(lUnions.size() > 1){
					opr.getRangoList().addAll(this.getSimplifyUnion(new LinkedList(lUnions)));	
				}/*else if(lOneOf.size()==1){
					opr.getRangoList().addAll(lOneOf);
				}
				else if (lOneOf.size() > 1) {
					opr.getRangoList().addAll(this.getIntersectionOf((new LinkedList(lOneOf))));
				}*/
				
//				if(opr.getRangoList().size()==1){
//					opr.setCommonRange(ik.getClassOf(opr.getRangoList().getFirst()));
//				}
//				else if(opr.getRangoList().size()>1){//SI EL RANGO TIENE MÁS DE UN ELEMENTO OBTENEMOS QUE TIENEN EN COMUN
//					//recorremos rangoList y obtenemos que clase tienenen en común
//					LinkedList<Integer>clasesRange=new LinkedList<Integer>();
//					for(int i=0;i<opr.getRangoList().size();i++){
//						clasesRange.add(ik.getClassOf(opr.getRangoList().get(i)));
//					}
//					//System.err.println("\n\n DEBUG KA.buildObjectPropertyOf, pr before setRange="+opr);
//					opr.setCommonRange(this.getCommonParent(clasesRange));
//				}
			}
			//SI LA CLASE DEL RANGO ES DE TIPO ENUMERADO Y NO TIENE ENUMERADOS EXPLICITOS (QUE SERÍAN MÁS ESPECÍFICOS POR REGLAS O DEF) LE AÑADIMOS COMO ENUMERADOS TODOS 
			//LOS INDIVIDUOS DE LA CLASE DEL RANGO
			if(opr.getEnumList().size()==0&&opr.getRangoList().size()==1){
				int idtoRange=ik.getClassOf(opr.getRangoList().get(0));
				if(ik.isSpecialized(idtoRange,Constants.IDTO_ENUMERATED)){
					Iterator<Integer> itindv=ik.getIndividualsOfLevel(idtoRange, Constants.LEVEL_INDIVIDUAL).iterator();
					while (itindv.hasNext()){
						ObjectValue ove=new ObjectValue(itindv.next(),idtoRange);
						opr.getEnumList().add(ove);
					}
				}
			}
			//System.err.println("Property:"+opr);
			return opr;
		}
			
		 
					
					
		
	


	
	
	public  LinkedList<dynagent.common.knowledge.IPropertyDef> traslateTreeObjectToFacts(instance treeob) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
			
			LinkedList<dynagent.common.knowledge.IPropertyDef> facts=new LinkedList<dynagent.common.knowledge.IPropertyDef>();
			LinkedList<Property> allproperties=treeob.getAllProperties();
			for(int i=0;i<allproperties.size();i++){
				if(allproperties.get(i) instanceof DataProperty){
					facts.addAll(this.traslateDataPropertyValueToIPropertyDef((DataProperty)allproperties.get(i)));
					
				}
				else if(allproperties.get(i) instanceof ObjectProperty){
					facts.addAll(this.traslateObjectPropertyValueToIPropertyDef((ObjectProperty)allproperties.get(i)));
				}
			}
			return facts;
		}


	/**
	 * Traduce una Property a una lista de fact instances.
	 * @param pr Property
	 * @return LinkedList<dynagent.common.knowledge.IPropertyDef> lista de facts
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 */public  LinkedList<dynagent.common.knowledge.IPropertyDef> traslatePropertyValueToIPropertyDef(Property pr) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		if(pr instanceof DataProperty){
			return this.traslateDataPropertyValueToIPropertyDef((DataProperty)pr);
		}
		else if(pr instanceof ObjectProperty){
			return this.traslateObjectPropertyValueToIPropertyDef((ObjectProperty)pr);
		}
		else{
			System.err.println("   WARNING: Llamada a traslatePropertyToFacts con parámetro Property que no es ni Object ni Data Property");
			return null;
		}
	}
	

	
	/**
	 * Traduce una Property a una lista de fact instances.
	 * @param pr Property
	 * @return LinkedList<dynagent.common.knowledge.IPropertyDef> lista de facts
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */public  LinkedList<dynagent.common.knowledge.IPropertyDef> traslatePropertyDEFToIPropertyDef(Property pr) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		  LinkedList<dynagent.common.knowledge.IPropertyDef> facts=new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		  dynagent.common.knowledge.IPropertyDef fact;
		  Integer idto = pr.getIdto();
		  Integer prop = pr.getIdProp();
		  Integer ido = pr.getIdo();
		  Double qmax = null;
		  Double qmin =null;
		  Integer valuecls=null;
		  String op=null;
		  String value=null;
		  if(pr.getCardMax()!=null||pr.getCardMin()!=null){//CARDINALIDAD
			if(pr.getCardMax()!=null)
				qmax=pr.getCardMax().doubleValue();
			if(pr.getCardMin()!=null)
					qmin=pr.getCardMin().doubleValue();
			fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),qmin,qmax,op,null);
			facts.add(fact);
		  }
		  if(pr instanceof DataProperty){
			  DataProperty dpr=(DataProperty)pr;
			  valuecls=dpr.getDataType();
			  op=Constants.OP_INTERSECTION;
			  fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),null,null,op,null);
			  facts.add(fact);
			  for(int i=0;i<dpr.getEnumList().size();i++){
				  fact=this.traslateDataValueToIPropertyDef(dpr, (DataValue)dpr.getEnumList().get(i));
				  op=Constants.OP_ONEOF;
				  fact.setOP(op);
				  facts.add(fact);
			  }
			  for(int i=0;i<dpr.getExcluList().size();i++){
				  fact=this.traslateDataValueToIPropertyDef(dpr, (DataValue)dpr.getExcluList().get(i));
				  fact.setOP(Constants.OP_NEGATION);
				  facts.add(fact);
			  }
		  }
		  else if(pr instanceof ObjectProperty){
			   ObjectProperty opr=(ObjectProperty)pr;
			   for(int i=0;i<opr.getRangoList().size();i++){
				   valuecls=opr.getRangoList().get(i);
				   op=Constants.OP_UNION;
				   fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),null,null,op,null);
				   facts.add(fact);
			   }
			   for(int i=0;i<opr.getEnumList().size();i++){
					  fact=this.traslateObjectValueToIPropertyDef(opr, (ObjectValue)opr.getEnumList().get(i));
					  fact.setOP(Constants.OP_ONEOF);
					  facts.add(fact);
			   }
			   for(int i=0;i<opr.getExcluList().size();i++){
					  fact=this.traslateObjectValueToIPropertyDef(opr, (ObjectValue)opr.getEnumList().get(i));
					  fact.setOP(Constants.OP_NEGATION);
					  facts.add(fact);
			   }
			   for(int i=0;i<opr.getQuantityDetailList().size();i++){
				   if(opr.getQuantityDetailList().get(i).getValue()!=null){
					   value=String.valueOf(opr.getQuantityDetailList().get(i).getValue());
				   }
				   else
					   value=null;
				   valuecls=opr.getQuantityDetailList().get(i).getValueCls();
				   if(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMin()!=null)
					   qmin=Double.valueOf(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMin());
				   else
					   qmin=null;
				   if(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMax()!=null)
						   qmax=Double.valueOf(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMax());
				   else
					   qmax=null;
				   fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),qmin,qmax,op,null);
				   fact.setOP(Constants.OP_QUANTITYDETAIL);
				   facts.add(fact);
			   }
				
		  }
			return facts;
			
		 
		 
		 
	}
	 
	 
	 public  LinkedList<dynagent.common.knowledge.IPropertyDef> traslatePropertyBusinessRestricctionToIPropertyDef(Property pr,int operation) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		 System.out.println("Property businessRestriction:"+pr); 
		 LinkedList<dynagent.common.knowledge.IPropertyDef> facts=new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		  dynagent.common.knowledge.IPropertyDef fact;
		  Integer idto = pr.getIdto();
		  Integer prop = pr.getIdProp();
		  Integer ido = pr.getIdo();
		  Double qmax = null;
		  Double qmin =null;
		  Integer valuecls=null;
		  String op=null;
		  String value=null;
		  if(pr.getCardMax()!=null||pr.getCardMin()!=null){//CARDINALIDAD
			if(pr.getCardMax()!=null)
				qmax=pr.getCardMax().doubleValue();
			if(pr.getCardMin()!=null)
					qmin=pr.getCardMin().doubleValue();
			fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),qmin,qmax,op,Constants.OP_CARDINALITY);
			((FactInstance)fact).setOperacion(operation);
			((FactInstance)fact).setOperacion(action.NEW);
			facts.add(fact);
		  }
		  for(int i=0;i<pr.getValues().size();i++){
			  fact=this.traslateValueToFact(pr,pr.getValues().get(i));
			  //los valores que tenga la definición de una businessREstricction son restricciones hasvalue para las instancias de esa clase.
			  fact.setOP(Constants.OP_HASVALUE);

			  ((FactInstance)fact).setOperacion(operation);

			  facts.add(fact);
		  }		 
		
		  if(pr instanceof DataProperty){
			  DataProperty dpr=(DataProperty)pr;
			  /*EL RANGO YA LO TIENE DEFINIDO LA CLASE DE LA QUE ESPECIALIZA. valuecls=dpr.getDataType();
			  op=Constants.OP_INTERSECTION;
			  fact= new dynagent.common.knowledge.IPropertyDefInstance(idto,ido,prop,value,valuecls,null,null,op,null);
			  facts.add(fact);
			  */
			  for(int i=0;i<dpr.getEnumList().size();i++){
				  fact=this.traslateDataValueToIPropertyDef(dpr, (DataValue)dpr.getEnumList().get(i));
				  op=Constants.OP_ONEOF;
				  fact.setOP(op);

				  ((FactInstance)fact).setOperacion(operation);

				  facts.add(fact);
			  }
			  for(int i=0;i<dpr.getExcluList().size();i++){
				  fact=this.traslateDataValueToIPropertyDef(dpr, (DataValue)dpr.getExcluList().get(i));
				  fact.setOP(Constants.OP_NEGATION);

				  ((FactInstance)fact).setOperacion(operation);

				  facts.add(fact);
			  }
		  }
		  else if(pr instanceof ObjectProperty){
			   ObjectProperty opr=(ObjectProperty)pr;
			   for(int i=0;i<opr.getRangoList().size();i++){
				   valuecls=opr.getRangoList().get(i);
				   op=Constants.OP_UNION;
				   fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),null,null,op,null);

				   ((FactInstance)fact).setOperacion(operation);

				   facts.add(fact);
			   }
			   for(int i=0;i<opr.getEnumList().size();i++){
					  fact=this.traslateObjectValueToIPropertyDef(opr, (ObjectValue)opr.getEnumList().get(i));
					  fact.setOP(Constants.OP_ONEOF);

					  ((FactInstance)fact).setOperacion(operation);

					  facts.add(fact);
			   }
			   for(int i=0;i<opr.getExcluList().size();i++){
					  fact=this.traslateObjectValueToIPropertyDef(opr, (ObjectValue)opr.getEnumList().get(i));
					  fact.setOP(Constants.OP_NEGATION);

					  ((FactInstance)fact).setOperacion(operation);

					  facts.add(fact);
			   }
			   for(int i=0;i<opr.getQuantityDetailList().size();i++){
				   if(opr.getQuantityDetailList().get(i).getValue()!=null){
					   value=String.valueOf(opr.getQuantityDetailList().get(i).getValue());
				   }
				   else
					   value=null;
				   valuecls=opr.getQuantityDetailList().get(i).getValueCls();
				   if(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMin()!=null)
					   qmin=Double.valueOf(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMin());
				   else
					   qmin=null;
				   if(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMax()!=null)
						   qmax=Double.valueOf(opr.getQuantityDetailList().get(i).getCardinalityEspecifyMax());
				   else
					   qmax=null;
				   fact= new dynagent.common.knowledge.FactInstance(idto,ido,prop,value,valuecls,ik.getClassName(valuecls),qmin,qmax,op,null);
				   ((FactInstance)fact).setOperacion(action.NEW);
				   fact.setOP(Constants.OP_QUANTITYDETAIL);
				   ((FactInstance)fact).setOperacion(operation);
				   facts.add(fact);
			   }
				
		  }
			return facts;
			
		 
		 
		 
	}
	 
	 
	 


	
	/***
	 * 
	 * Este método traduce una Dataproperty con valores a facts. No esta diseñado para traducir información del modelo, solo los valores.
	 * param DataProperty 
	 * @return LinkedList<dynagent.common.knowledge.IPropertyDef>
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 */
	public  LinkedList<dynagent.common.knowledge.IPropertyDef> traslateDataPropertyValueToIPropertyDef(DataProperty pr) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		LinkedList<dynagent.common.knowledge.IPropertyDef> facts=new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		
		//Si no tiene valores solo hay que asignar el fact que le asigna la propiedad
		if(pr.getValues().size()==0){
			dynagent.common.knowledge.FactInstance fact=new dynagent.common.knowledge.FactInstance(pr.getIdto(),pr.getIdo(),pr.getIdProp(),null,pr.getDataType(),Constants.getDatatype(pr.getDataType()),null,null,Constants.OP_INTERSECTION,null);
			facts.add(fact);
		}
		for(int i=0;i<pr.getValues().size();i++)
		{
			facts.add(this.traslateDataValueToIPropertyDef(pr,(DataValue) pr.getValues().get(i)));	
		}
		return facts;
	}
	
	
	
	/***
	 * 
	 * Este método traduce una Dataproperty con valores a facts. No esta diseñado para traducir información del modelo, solo los valores.
	 * - Este método debe estar sincronizado con el método buildDataValue
	
	 * @param DataProperty 
	 * @return LinkedList<dynagent.common.knowledge.IPropertyDef>
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 */
	
	public  dynagent.common.knowledge.IPropertyDef traslateDataValueToIPropertyDef(int idto,Integer ido,int idProp,int valueCls,DataValue val) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		String name=null;
		name=ik.getClassName(idto);
		
		Double qmax = null;
		Double qmin =null;
		String value=null;
	
		int valuecls=valueCls;
		
		if(val instanceof UnitValue||val instanceof DoubleValue){
			qmin = (((DoubleValue)val).getValueMin());
			qmax= (((DoubleValue)val).getValueMax());
			if(val instanceof UnitValue){
				valuecls = (((UnitValue)val).getUnit());
			}
		}
		else if(val instanceof IntValue){
			qmin=((IntValue)val).getValueMin()!=null?(((IntValue)val).getValueMin().doubleValue()):null;
			qmax= ((IntValue)val).getValueMax()!=null?(((IntValue)val).getValueMax().doubleValue()):null;
		}
		else if(val instanceof BooleanValue){
			BooleanValue bv=(BooleanValue)val;
			value=bv.getComment();
			Double fbool;
			if(bv.getBvalue()!=null&&bv.getBvalue().equals(true)){
				fbool=new Double(Constants.ID_BOOLEAN_TRUE);
				qmax =(fbool);
				qmin=(fbool);
			}
			else if(bv.getBvalue()!=null&&bv.getBvalue().equals(false)){
				fbool=new Double(Constants.ID_BOOLEAN_FALSE);
				qmax=(fbool);
				qmin=(fbool);
			}		
		}	
		else if(val instanceof StringValue){
			value=(((StringValue)val).getValue());
		}
		else if(val instanceof TimeValue){
			if (valuecls==Constants.IDTO_DATE){
				TimeValue tv=(TimeValue)val;
				Calendar cmin=Calendar.getInstance();
				Date dmin=new Date((long) (tv.getRelativeSecondsMin().doubleValue()*Constants.TIMEMILLIS));
				Date dmax=new Date((long) (tv.getRelativeSecondsMax().doubleValue()*Constants.TIMEMILLIS));
				cmin.setTime(dmin);
				cmin.set(cmin.get(Calendar.YEAR),cmin.get(Calendar.MONTH),cmin.get(Calendar.DATE), 0,0,0);
				
				long timemin=cmin.getTimeInMillis()/Constants.TIMEMILLIS;
			
				cmin.setTime(dmax);
				cmin.set(cmin.get(Calendar.YEAR),cmin.get(Calendar.MONTH),cmin.get(Calendar.DATE), 0,0,0);
				long timemax=cmin.getTimeInMillis()/Constants.TIMEMILLIS;
				if(tv.getRelativeSecondsMin()!=null)
					qmin=(double)timemin;
				if(tv.getRelativeSecondsMax()!=null)
					qmax=(double)timemax;
				if(tv.getReferenceInstant()!=null){
					value=tv.getReferenceInstant();
				}
			}else if (valuecls==Constants.IDTO_TIME){
				TimeValue tv=(TimeValue)val;
				Calendar c = Calendar.getInstance();				
				if(tv.getRelativeSecondsMin()!=null){
					c.setTimeInMillis(tv.getRelativeSecondsMin()*Constants.TIMEMILLIS);
					c.set(1970, Calendar.JANUARY, 1);
					qmin=(double)(c.getTimeInMillis()/Constants.TIMEMILLIS);
				}if(tv.getRelativeSecondsMax()!=null){
					c.setTimeInMillis(tv.getRelativeSecondsMax()*Constants.TIMEMILLIS);
					c.set(1970, Calendar.JANUARY, 1);
					qmax=(double)(c.getTimeInMillis()/Constants.TIMEMILLIS);					
				}if(tv.getReferenceInstant()!=null){
					value=tv.getReferenceInstant();
				}			
			}else{
				TimeValue tv=(TimeValue)val;
				if(tv.getRelativeSecondsMin()!=null)
					qmin=tv.getRelativeSecondsMin().doubleValue();
				if(tv.getRelativeSecondsMax()!=null)
					qmax=tv.getRelativeSecondsMax().doubleValue();
				if(tv.getReferenceInstant()!=null){
					value=tv.getReferenceInstant();
				}
			}
		}
		
		String op=null;
		if(!val.isEqualToValue()){
			op=Constants.OP_NEGATION;
		}
		
		dynagent.common.knowledge.FactInstance fact= new dynagent.common.knowledge.FactInstance(idto,ido,idProp,value,valuecls,ik.getClassName(valuecls),qmin,qmax,op,name);
		return fact;
	}
	public  dynagent.common.knowledge.IPropertyDef traslateDataValueToIPropertyDef(DataProperty pr,DataValue val) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		return traslateDataValueToIPropertyDef(pr.getIdto(),pr.getIdo(),pr.getIdProp(),pr.getDataType(),val);
	}
	
	private Long getHourMinSecOfCalendar(Calendar c){
		if(c==null)
			return null;
		else{
			int value = c.get(Calendar.HOUR_OF_DAY)*3600+c.get(Calendar.MINUTE)*60+c.get(Calendar.SECOND);
			return new Long(value*Constants.TIMEMILLIS);
		}
	}


	public  dynagent.common.knowledge.IPropertyDef traslateValueToFact(int idto,Integer ido,int idProp,int valueCls,Value val) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		if(ik.isDataProperty(idProp)){
			return this.traslateDataValueToIPropertyDef(idto,ido,idProp,valueCls,(DataValue)val);
		}
		else if(ik.isObjectProperty(idProp)){
			return this.traslateObjectValueToIPropertyDef(idto,ido,idProp,valueCls, (ObjectValue)val);
		}
		else
			return null;
	}
	
	
	public  dynagent.common.knowledge.IPropertyDef traslateValueToFact(Property pr,Value val) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		if(pr instanceof DataProperty){
			return this.traslateDataValueToIPropertyDef((DataProperty)pr, (DataValue)val);
		}
		else if(pr instanceof ObjectProperty){
			return this.traslateObjectValueToIPropertyDef((ObjectProperty)pr, (ObjectValue)val);
		}
		else
			return null;
	}
	


	
	/**
	 * 
	 * @param idto: identificador del tipo de objeto
	 * @param ido: identificador del objeto
	 * @param pr: ObjectProperty
	 * @param level: level que se asignará a los facts en motor.
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 * @see: buildObjectValue
	 * return: LinkedList<dynagent.fact>
	 */
	
	public  dynagent.common.knowledge.IPropertyDef traslateObjectValueToIPropertyDef(int idto,Integer ido,int idProp,int valueCls,ObjectValue val) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		String value=null;	
		Double qmax=null;
		Double qmin=null;
		String name=null;
		name=ik.getClassName(idto);
		
		if(val.getValue()!=null)
			value=(val.getValue().toString());
		valueCls =val.getValueCls();
		//Si Q no es nula, se trata de información resumida
		if(val.getQ()!=null &&ik.isIDClass(val.getValue())){
			qmax=(val.getQ().doubleValue());
			qmin = (val.getQ().doubleValue());
		}
		String rangename=null;
		rangename=ik.getClassName(valueCls);
		
		String op=null;
		if(!val.isEqualToValue()){
			op=Constants.OP_NEGATION;
		}
		
		return new FactInstance(idto,idto,idProp,value,valueCls,ik.getClassName(valueCls),qmin,qmax,op,name);
	}
	
	public  dynagent.common.knowledge.IPropertyDef traslateObjectValueToIPropertyDef(ObjectProperty pr,ObjectValue val) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		String value=null;	
		Integer valueCls=null;
		Integer idto=pr.getIdto();
		Double qmax=null;
		Double qmin=null;
		String name=null;
		name=ik.getClassName(pr.getIdto());
		
		if(val.getValue()!=null)
			value=(val.getValue().toString());
		valueCls =val.getValueCls();
		//Si Q no es nula, se trata de información resumida
		if(val.getQ()!=null &&ik.isIDClass(val.getValue())){
			qmax=(val.getQ().doubleValue());
			qmin = (val.getQ().doubleValue());
		}
		String rangename=null;
		rangename=ik.getClassName(valueCls);
		return new FactInstance(pr.getIdto(),pr.getIdo(),pr.getIdProp(),value,valueCls,ik.getClassName(valueCls),qmin,qmax,null,name);
	}
	
	
	
	/**
	 * traslateObjectPropertyToFacts: 
	 * @param idto
	 * @param ido
	 * @param dpr
	 * @param level
	 * @return
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 */
	public  LinkedList<dynagent.common.knowledge.IPropertyDef> traslateObjectPropertyValueToIPropertyDef(ObjectProperty pr) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		LinkedList<dynagent.common.knowledge.IPropertyDef> facts=new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		LinkedList<dynagent.common.knowledge.IPropertyDef> filterfacts=new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		for(int i=0;i<pr.getValues().size();i++)
		{
			facts.add(this.traslateObjectValueToIPropertyDef(pr, (ObjectValue)pr.getValues().get(i)));
		}
		/*for(int i=0;i<pr.getFilterList().size();i++)
		{
			filterfacts.add(this.traslateObjectValueToIPropertyDef(pr, pr.getFilterList().get(i)));
		}
		//añadimos el OP=OR A LOS FILTROS antes de añadirlos a la lista total
		for(int i=0;i<filterfacts.size();i++){
			IPropertyDef filterfact=filterfacts.get(i);
			filterfact.setOP(Constants.OP_UNION);
			facts.add(filterfact);
		}
		*/
		return facts;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * getIntersectionOf: Calcula la intersección de dos conjuntos
	 * 
	 * @param idtoA:
	 *            identificador tipo objeto A
	 * @param idtoB:
	 *            identificador tipo objeto B
	 * @return: Lista de enteros con los conjuntos cuya unión constituye la
	 *          intersección de A y B
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException 
	 */
	private LinkedList<Integer> getIntersectionOf(int idtoA, int idtoB)throws NotFoundException, IncoherenceInMotorException {
		LinkedList<Integer> result = new LinkedList<Integer>();
		if (ik.isSpecialized(idtoA, idtoB))
			result.add(idtoA);
		else if (ik.isSpecialized(idtoB, idtoA))
			result.add(idtoB);
		else {
			ArrayList<Integer> childsOfA = new ArrayList<Integer>(Auxiliar.IteratorToArrayList(ik.getSpecialized(idtoA).iterator()));
			ArrayList<Integer> childsOfB = new ArrayList<Integer>(Auxiliar.IteratorToArrayList(ik.getSpecialized(idtoB).iterator()));
			for (int i = 0; i < childsOfA.size(); i++) {
				if (childsOfB.contains(childsOfA.get(i))) {
					result.add(childsOfA.get(i));
				}
			}
		}

		return this.getSimplifyUnion(result);
	}

	/**
	 * getIntersectionOf: Calcula la intersección de varios conjuntos
	 * 
	 * @param lconjuntos:
	 *            lista con los identificadores de los conjuntos
	 * @return: Lista de enteros con los conjuntos cuya unión constituye la
	 *          intersección de A y B
	 * @throws NotFoundException
	 */
	private LinkedList<Integer> getIntersectionOf(LinkedList<Integer> lconjuntos)throws NotFoundException, IncoherenceInMotorException {

		LinkedList<Integer> result = new LinkedList<Integer>();
		if (lconjuntos.size() < 2) {
			result = lconjuntos;
		} else if (lconjuntos.size() == 2) {
			result = this.getIntersectionOf(lconjuntos.get(0), lconjuntos
					.get(1));
		} else if (lconjuntos.size() > 2) {
			ArrayList<ArrayList<Integer>> childsOfeveryOne = new ArrayList<ArrayList<Integer>>();
			LinkedList<Integer> allChilds = new LinkedList<Integer>();
			for (int i = 0; i < lconjuntos.size(); i++) {
				ArrayList<Integer> childsOfi = new ArrayList<Integer>(Auxiliar.IteratorToArrayList(ik.getSpecialized(lconjuntos.get(i)).iterator()));
				childsOfeveryOne.add(childsOfi);
				for (int j = 0; j < childsOfi.size(); j++) {
					if (!allChilds.contains(childsOfi.get(j))) {
						allChilds.add(childsOfi.get(j));
					}
				}
			}
			for (int i = 0; i < allChilds.size(); i++) {
				boolean isChildOfAll = true;
				for (int j = 0; (j < childsOfeveryOne.size()) && isChildOfAll; j++) {
					isChildOfAll = childsOfeveryOne.get(j).contains(
							allChilds.get(i));
				}
				if (isChildOfAll) {
					result.add(allChilds.get(i));
				}
			}
			// simplificamos si es posible el resultado
			result = this.getSimplifyUnion(result);
		}
		//RuleEngineLogger.getLogger().write(	"     getIntersectionOf " + lconjuntos.toString() + "   is="+ result.toString());
		return result;
	}

	/**
	 * getSimplifyUnion: Simplifica (si es posible) la unión de varios conjuntos
	 * 
	 * @param conjuntos: Lista de enteros con los identificadores numéricos de los
	 *  conjuntos que forman la unión.
	 * @return: una expresión simplificada de la unión
	 * @throws NotFoundException 
	 */
	public LinkedList<Integer> getSimplifyUnion(LinkedList<Integer> conjuntos) throws NotFoundException, IncoherenceInMotorException  {
		LinkedList<Integer> redundantes = new LinkedList<Integer>();
		LinkedList<Integer> result = new LinkedList<Integer>();
		HashSet<Integer> resultAux=new HashSet<Integer>();
		
		
		for (int i = 0; i < conjuntos.size(); i++) {
			for (int j = 0; j < conjuntos.size(); j++) {
				if ((i != j)&& ik.isSpecialized(conjuntos.get(j), conjuntos	.get(i))) {
					redundantes.add(conjuntos.get(j));
				//	System.err.println("\n\n DEBUGGG   symplifyUnionOf--ES REDUNDANTE: >"+ conjuntos.get(j)+"  por heredar de "+conjuntos	.get(i));
					
				}
			}
		}
		// Construimos el resultado quitando a la unión de conjuntos los
		// redundantes
		for (int i = 0; i < conjuntos.size(); i++) {
			if (!redundantes.contains(conjuntos.get(i))) {
				resultAux.add(conjuntos.get(i));
			}
		}
		//RuleEngineLogger.getLogger().write(	"     symplifyUnionOf " + conjuntos.toString() + "   is="+ result.toString());
		
		//System.err.println("\n\n DEBUGGG   symplifyUnionOf " + conjuntos.toString() + "   is="+ result.toString());
		if(resultAux.isEmpty()){
			return new LinkedList(new HashSet<Integer>(conjuntos));
		}
		else
			result=new LinkedList(resultAux);
		
		return result;

	}
	
	/**
	 * Obtieneo la clase común de la que heredan si son clases, o lo mismo de las clases que representan si son filtros.
	 * @param conjuntos
	 * @return
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 */
	private Integer getCommonParent(LinkedList<Integer> conjuntos) throws NotFoundException, IncoherenceInMotorException {
		 LinkedList<Integer> allSuperiors=new LinkedList<Integer>();
		 LinkedList<Integer> comunes=new LinkedList<Integer>();
		 
		 boolean  noCommun=conjuntos.size()==0;
		 //obtenemos todos los superiores
		 LinkedList<LinkedList>listasOfSuperiores=new LinkedList<LinkedList>();
		 for(int i=0;i<conjuntos.size()&&!noCommun;i++){
			 Iterator<Integer> itsuperioresOfCi=ik.getSuperior(conjuntos.get(i)); 
			if(!itsuperioresOfCi.hasNext()){
				noCommun=true;
			}
			else{
				listasOfSuperiores.add(Auxiliar.IteratorToLinkedList(itsuperioresOfCi));
			}
		 }
		 if(noCommun){
			 return null;
		 }
		 else{
			 comunes=Auxiliar.getCommonsElementsOfArrays(listasOfSuperiores);
			 if(comunes.size()==0){
				 return null;
			 }
			 else if(comunes.size()==1){
				 return comunes.getFirst();
			 }
			 else{
				  //simplificamos al padre más directo
				  comunes=this.getIntersectionOf(comunes);
				  if(comunes.size()==1){
					  return comunes.getFirst();
				  }
				  /*else if(comunes.size()>1){
					  System.err.println("\n\n     WARNING:  getCommonParent conjuntos="+conjuntos+"   obtiene + de 1 elementos,"+comunes);
					  for(int i=0;i<comunes.size();i++){
						  System.err.println();
						  try {
							System.err.println(comunes.get(i)+"   clase-->"+ik.getClassName(comunes.get(i)));
						} catch (SystemException e) {
							e.printStackTrace();
						} catch (RemoteSystemException e) {
							e.printStackTrace();
						} catch (CommunicationException e) {
							e.printStackTrace();
						} catch (InstanceLockedException e) {
							e.printStackTrace();
						} catch (ApplicationException e) {
							e.printStackTrace();
						} catch (IncompatibleValueException e) {
							e.printStackTrace();
						} catch (CardinalityExceedException e) {
							e.printStackTrace();
						} catch (OperationNotPermitedException e) {
							e.printStackTrace();
						}
						  
					  }
					  return null;
				  }*/
				  else return null;
			 }
		 }
	}
	
	
	public  Value buildValue(String value,int valueCls){
		Value val = null;
		boolean isDataValue=false;
		DataValue  dataV;
		
		String[] buf;
		switch(valueCls){
			case Constants.IDTO_FILE:
			case Constants.IDTO_IMAGE:
			case Constants.IDTO_MEMO:
			case Constants.IDTO_STRING:
				 dataV=new StringValue();
				((StringValue)dataV).setValue(value);
				val=dataV;
				isDataValue=true;
				break;
			case Constants.IDTO_INT:
				dataV=new IntValue();
				buf=value.split(":");
				//System.out.println("Asigna valor int:"+value);
				((IntValue)dataV).setValueMin(new Double(buf[0]).intValue());
				if(buf.length>1)
					((IntValue)dataV).setValueMax(new Double(buf[1]).intValue());
				else ((IntValue)dataV).setValueMax(new Double(buf[0]).intValue());
				val=dataV;
				isDataValue=true;
				break;
			case Constants.IDTO_DOUBLE:
				dataV=new DoubleValue();
				buf=value.split(":");
				//System.out.println("Asigna valor double:"+value);
				((DoubleValue)dataV).setValueMin(new Double(buf[0]));
				if(buf.length>1)
					((DoubleValue)dataV).setValueMax(new Double(buf[1]));
				else ((DoubleValue)dataV).setValueMax(new Double(buf[0]));
				val=dataV;
				isDataValue=true;
				break;
			case Constants.IDTO_BOOLEAN:
				dataV=new BooleanValue();
				buf=value.split(":");
				//System.out.println("Asigna valor booleano:"+value);
				((BooleanValue)dataV).setBvalue(buf[0].equals("null")?null:new Boolean(buf[0]));
				if(buf.length>1)
					((BooleanValue)dataV).setComment(buf[1].equals("null")?null:buf[1]);
				val=dataV;
				isDataValue=true;
				break;
			case Constants.IDTO_DATE:
			case Constants.IDTO_DATETIME:
			case Constants.IDTO_TIME:
				dataV=new TimeValue();
				buf=value.split(":");
				//System.out.println("Asigna valor date:"+value);
				((TimeValue)dataV).setRelativeSecondsMin(new Long(buf[0]));
				if(buf.length>1)
					((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[1]));
				else ((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[0]));
				val=dataV;
				isDataValue=true;
				break;
		}
		if(!isDataValue){
			ObjectValue objectV=new ObjectValue();
			 int ivalue=new Integer(value).intValue();
		    objectV.setValue(ivalue);
		   
		    objectV.setValueCls(valueCls);
		    val=objectV;
		}
		return val;
		}
	
	
	
	
	/**
	 * CONSTRUYE UN VALOR CUALQUIERA
	 * @param p
	 * @return
	 * @throws NotFoundException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws OperationNotPermitedException 
	 */
	 public Value buildAnyValue(Property p) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException{
			Value val=null;
			int random = new Double(Math.random() * 1000).intValue();

			if(p instanceof DataProperty){
				DataProperty dp=(DataProperty)p;
				if(dp.getEnumList().size()>0){
					int i= new Double(Math.random() * dp.getEnumList().size()).intValue();
					DataValue dv=dp.getEnumList().get(i);
					val=dv;
				}
				else{
					if(ik.isUnit(dp.getDataType())){
						UnitValue uv=new UnitValue();
						uv.setValueMax(50.0D*random);
						uv.setValueMin(70.0D*random);
						uv.setUnit(1313);
						val=uv;
					}
					else if(dp.getDataType()==Constants.IDTO_STRING){
						
						String svalue="valorprueba"+ random;
						StringValue sv=new StringValue(svalue);
						val=sv;
					}
					else if(dp.getDataType()==Constants.IDTO_INT){
						IntValue iv=new IntValue(random,random);
						val=iv;
					}
					else if(dp.getDataType()==Constants.IDTO_DOUBLE){
						DoubleValue fv=new DoubleValue();
						fv.setValueMax(1.D*random);
						fv.setValueMin(1.0D*random);
						val=fv;
					}
					else if(dp.getDataType()==Constants.IDTO_BOOLEAN){
						BooleanValue bv=new BooleanValue();
						bv.setBvalue(true);
						bv.setComment("comentariobooleano"+random);
						val=bv;
					}
					else if(dp.getDataType()==Constants.IDTO_DATE||dp.getDataType()==Constants.IDTO_DATETIME||dp.getDataType()==Constants.IDTO_TIME){
						TimeValue tv=new TimeValue();
						tv.setReferenceInstant(Constants.BEGIN1970);
						tv.setRelativeSecondsMin(13131313l*random);
						tv.setRelativeSecondsMax(13131313l*random);
						val=tv;
					}
				}
	 		}
			else if (p instanceof ObjectProperty){
				ObjectProperty op=(ObjectProperty)p;
				if(op.getEnumList().size()>0){
					int i= new Double(Math.random() * op.getEnumList().size()).intValue();
					ObjectValue ov=op.getEnumList().get(i);
					val=ov;
				}
				else{
					int valueran=12000+random;
					ObjectValue ov=new ObjectValue();
					int valuecls=ik.getClassOf(op.getRangoList().getFirst());
					ov.setValueCls(valuecls);
					ov.setValue(valueran);
					val=ov;
				}	
			}
			return val;
		}
		
	public static Access buildAbstractAccess(int idto){
		ArrayList<String> accessViewName=new ArrayList<String>();
		accessViewName.add(Constants.ACCESS_ABSTRACT_NAME);
		Access a=buildAccess(idto, null, accessViewName, false, 0);
		return a;
	}
	
	public static Access buildAllAccess(Integer idto,int priority){
		ArrayList<String> accessViewName=new ArrayList<String>();
		accessViewName.add(Constants.ACCESS_VIEW_NAME);
		accessViewName.add(Constants.ACCESS_SET_NAME);
		accessViewName.add(Constants.ACCESS_FIND_NAME);
		accessViewName.add(Constants.ACCESS_NEW_NAME);
		accessViewName.add(Constants.ACCESS_DEL_NAME);
		return buildAccess(idto, null, accessViewName, false, priority);
	}
	
	public static Access buildAccess(Integer idto,Integer prop,ArrayList<String> accessViewName,boolean dennied,int priority){
		ArrayList<String> access=new ArrayList<String>();
		access.addAll(accessViewName);
		Access a = new Access(idto,null,prop,null,null,null,null,null,dennied?1:0,access,priority);
		return a;
	}
	
	public static Access buildAccess(Integer idto,Integer prop,String accessViewName,boolean dennied,int priority){
		ArrayList<String> access=new ArrayList<String>();
		access.add(accessViewName);
		Access a = new Access(idto,null,prop,null,null,null,null,null,dennied?1:0,access,priority);
		return a;
	}
	
	public static LinkedList<Instance> toInstance(ArrayList<IPropertyDef> aipd) {
		LinkedList<Instance> lli = new LinkedList<Instance>();
		for (int i=0;i<aipd.size();i++) {
			IPropertyDef ipd = aipd.get(i);
			Instance ins = toInstanceSub(ipd);
			lli.add(ins);
		}
		return lli;
	}
		
	private static Instance toInstanceSub(IPropertyDef ipd) {
		String idtoStr = null;
		if (ipd.getIDTO()!=null) idtoStr = String.valueOf(ipd.getIDTO());
		String idoStr = null;
		if (ipd.getIDO()!=null) idoStr = String.valueOf(ipd.getIDO());
		String valueClsStr = null;
		if (ipd.getVALUECLS()!=null) valueClsStr = String.valueOf(ipd.getVALUECLS());
		String qMinStr = null;
		if (ipd.getQMIN()!=null) qMinStr = String.valueOf(ipd.getQMIN());
		String qMaxStr = null;
		if (ipd.getQMAX()!=null) qMaxStr = String.valueOf(ipd.getQMAX());
		
		Instance ins = new Instance(idtoStr, idoStr, String.valueOf(ipd.getPROP()), ipd.getVALUE(), valueClsStr, 
				qMinStr, qMaxStr, ipd.getOP(), ipd.getCLASSNAME());
		return ins;
	}
	
	public static String getVALUE_s(IPropertyDef f)
	{
		//System.err.println("   DEBUG  FACT.GETVALUE_S    F="+f);
		String value=null;
		if((f.getVALUECLS()!=null)&&(f.getOP()==null||f.getOP().equals(Constants.OP_DEFAULTVALUE)||f.getOP().equals(Constants.OP_NEGATION))){//este metodo pasa a utilizarse también para excluvalue (op not), filtros (futuro op!=null)
			if(f.getQMIN()!=null&&f.getQMAX()!=null&&f.getQMIN().equals(f.getQMAX()))
			{	//SI QUEREMOS METER LOS DOS VALORES PODRIAMOS USAR UN FORMATO QMIN:QMAX, Q ES EL MISMO QUE SE USA EN EL APPLET
				if(f.getVALUECLS()==null)
				{
					System.err.println("   WARNING: Fact.getValue_s() encuentra valuecls=null en fact=\n"+f);
				}
				if(f.getVALUECLS().intValue()==Constants.IDTO_BOOLEAN)
				{
					if(f.getQMIN().intValue()==Constants.ID_BOOLEAN_TRUE)
					{
							value=String.valueOf(Constants.BOOLEAN_TRUE);
					}
					else if(f.getQMIN().intValue()==Constants.ID_BOOLEAN_FALSE)
					{
							value=String.valueOf(Constants.BOOLEAN_FALSE);
					}
				}
				else
				{
						value=String.valueOf(f.getQMIN());
				}
			}
			else 
				if(f.getVALUE()!=null)
				{
				value=f.getVALUE();
				}
		}
			return value;
		
	}
	
	
}

