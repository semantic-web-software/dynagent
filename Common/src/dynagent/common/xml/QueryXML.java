/***
 * QueryXML.java
 */

package dynagent.common.xml;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
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
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.Rdn;
import dynagent.common.utils.jdomParser;

public class QueryXML {
	
	private int id = 1;
	private IKnowledgeBaseInfo ik;
	private ArrayList<SelectQuery> select;
	private ArrayList<String> idNodeXselectPosition;
	private boolean showIdos;
	private Integer limit;
	
	public QueryXML(IKnowledgeBaseInfo ik){
		this.ik = ik;
		this.select = null;
	}
	
	public void toQueryXML(){
		
	}

	private ArrayList<Element> createWhere(instance ins, int ido, ArrayList<Element> aHijosAnd, Integer uTask) throws DataErrorException, NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion createWhere");
		ArrayList<Element> wheres = new ArrayList<Element>();
		Iterator iterador = ins.getAllPropertyIterator(ido);
		while (iterador.hasNext()) {
			Property p = (Property)iterador.next();
			if (p instanceof DataProperty) {
				DataProperty dp = (DataProperty)p;
				int prop = dp.getIdProp();
				int idto = dp.getIdto();
				LinkedList<Value> lv = dp.getValues();
				if (lv.size()>0) {
					boolean or = false;
					Element opOr = null;
					//si hay mas de un nodo where unido por OR, hacer un nodo attribute aparte con la condicion del required para que 
					//no se establezca la condicion por cada or
					if (lv.size()>1) {
						opOr = new Element(QueryConstants.OP);
						opOr.setAttribute(QueryConstants.ID_OP,String.valueOf(QueryConstants.OP_OR));
						aHijosAnd.add(opOr);
						or = true;
					}
					//System.out.println("toTypeQuery, prop:" + prop);
					//si hay varios valores en values van unidos con OR
					boolean required = false;
					if (lv.size()>1) {
						Element attribute = createNodeAttributeDP(prop);
						attribute.setAttribute(QueryConstants.REQUIRED,"TRUE");
						String alias = ik.getAliasOfProperty(dp.getIdto(), prop, uTask);
						attribute.setAttribute(QueryConstants.NAME, alias);
						
			        	wheres.add(attribute);
					} else
						required = true;
					for (int j=0;j<lv.size();j++) {
						DataValue dv = (DataValue)lv.get(j);
						Element where = createNodeWhere(dv, prop, required, idto, uTask);
						wheres.add(where);
				        Element hijoWhere = new Element(QueryConstants.CONDITION);
				        hijoWhere.setAttribute(QueryConstants.ID_CONDITION,String.valueOf(this.id));
				        if(!dv.isEqualToValue()){
				        	hijoWhere.setAttribute(QueryConstants.NOT,"TRUE");
				        }
				        if (or)
				        	opOr.addContent(hijoWhere);
				        else
				        	aHijosAnd.add(hijoWhere);
						this.id++;
					}
				} else {
					//si no tiene values ver si es required o no
					Integer cardMaxModelo = ik.getMaxPropertyCardinalityOfClass(idto, prop);
					Integer cardMinModelo = ik.getMinPropertyCardinalityOfClass(idto, prop);
					if (dp.getCardMin()!=null && dp.getCardMin()==1) {
						if (cardMinModelo==null || cardMinModelo!=null && cardMinModelo!=1) {
							Element attribute = createNodeAttributeDP(prop);
							attribute.setAttribute(QueryConstants.REQUIRED,"TRUE");
				        	wheres.add(attribute);
						}
					} else if (dp.getCardMax()!=null && dp.getCardMax()==0) {
						if (cardMaxModelo==null || cardMaxModelo!=null && cardMaxModelo!=0) {
							Element attribute = createNodeAttributeDP(prop);
							attribute.setAttribute(QueryConstants.NULL,"TRUE");
							wheres.add(attribute);
						}
					}
				}
				//si hay varios valores en excluList van unidos con AND
				LinkedList<DataValue> lv2 = dp.getExcluList();
				for (int j=0;j<lv2.size();j++) {
					DataValue dv = (DataValue)lv2.get(j);
					boolean required = false;
					Element where = createNodeWhere(dv, prop, required, idto, uTask);
					wheres.add(where);
			        Element hijoWhere = new Element(QueryConstants.CONDITION);
			        hijoWhere.setAttribute(QueryConstants.ID_CONDITION,String.valueOf(this.id));
			        hijoWhere.setAttribute(QueryConstants.NOT,"TRUE");
		        	aHijosAnd.add(hijoWhere);
					this.id++;
				}
			}
		}
		
		//System.out.println("Fin de la funcion createWhere");
		return wheres;
	}
	
	private Element createNodeWhere(DataValue dv, int prop, boolean required, int idto, Integer uTask) 
			throws NotFoundException, IncoherenceInMotorException, DataErrorException {
		//System.out.println("Inicio de la funcion createNodeWhere");
		Element where = new Element(QueryConstants.WHERE);
		where.setAttribute(QueryConstants.PROP,String.valueOf(prop));
		where.setAttribute(QueryConstants.NAME_PROP,ik.getPropertyName(prop));
		Integer idtmRuleengine = ik.getDatatype(prop);
		if (idtmRuleengine!=null)
			where.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(idtmRuleengine));
		if (required)
			where.setAttribute(QueryConstants.REQUIRED, "TRUE");
		String alias = ik.getAliasOfProperty(idto, prop, uTask);
		where.setAttribute(QueryConstants.NAME, alias);
		
		where.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
        if (dv instanceof UnitValue) {
			UnitValue uv = (UnitValue)dv;
			Double vMin = uv.getValueMin();
			Double vMax = uv.getValueMax();
			Integer clase = uv.getUnit();
			if (vMin!=null) {
				if (vMax!=null) {
					if (vMin.equals(vMax)) {
						where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
						where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
					} else {
						where.setAttribute(QueryConstants.OP,QueryConstants.BETWEEN);
						where.setAttribute(QueryConstants.VAL_MIN,String.valueOf(vMin));
						where.setAttribute(QueryConstants.VAL_MAX,String.valueOf(vMax));
					}
				} else {
					where.setAttribute(QueryConstants.OP,QueryConstants.MAYOR);
					where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
				}
			} else if (vMax!=null) {
				where.setAttribute(QueryConstants.OP,QueryConstants.MENOR);
				where.setAttribute(QueryConstants.VALUE,String.valueOf(vMax));
			}
			if (clase!=null) {
				where.setAttribute(QueryConstants.CLASS,String.valueOf(clase));
			}
		} else if (dv instanceof DoubleValue) {
			DoubleValue fv = (DoubleValue)dv;
			Double vMin = fv.getValueMin();
			Double vMax = fv.getValueMax();
			if (vMin!=null) {
				if (vMax!=null) {
					if (vMin.equals(vMax)) {
						where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
						where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
					} else {
						where.setAttribute(QueryConstants.OP,QueryConstants.BETWEEN);
						where.setAttribute(QueryConstants.VAL_MIN,String.valueOf(vMin));
						where.setAttribute(QueryConstants.VAL_MAX,String.valueOf(vMax));
					}
				} else {
					where.setAttribute(QueryConstants.OP,QueryConstants.MAYOR);
					where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
				}
			} else if (vMax!=null) {
				where.setAttribute(QueryConstants.OP,QueryConstants.MENOR);
				where.setAttribute(QueryConstants.VALUE,String.valueOf(vMax));
			}
		} else if (dv instanceof IntValue) {
			IntValue iv = (IntValue)dv;
			Integer vMin = iv.getValueMin();
			Integer vMax = iv.getValueMax();
			if (vMin!=null) {
				if (vMax!=null) {
					if (vMin.equals(vMax)) {
						where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
						where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
					} else {
						where.setAttribute(QueryConstants.OP,QueryConstants.BETWEEN);
						where.setAttribute(QueryConstants.VAL_MIN,String.valueOf(vMin));
						where.setAttribute(QueryConstants.VAL_MAX,String.valueOf(vMax));
					}
				} else {
					where.setAttribute(QueryConstants.OP,QueryConstants.MAYOR);
					where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
				}
			} else if (vMax!=null) {
				where.setAttribute(QueryConstants.OP,QueryConstants.MENOR);
				where.setAttribute(QueryConstants.VALUE,String.valueOf(vMax));
			}
		} else if (dv instanceof BooleanValue) {
			BooleanValue bv = (BooleanValue)dv;
			Boolean value = bv.getBvalue();
			where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
			if (value)
				where.setAttribute(QueryConstants.VALUE,"1");
			else
				where.setAttribute(QueryConstants.VALUE,"0");
		} else if (dv instanceof StringValue){
        	StringValue sv = (StringValue)dv;
        	String valor = sv.getValue();
        	//if (valor!=null) {
        	//no hace falta comprobar si tiene valor antes de crear el where, ya que si se guarda el DataProperty es porque tiene un valor
//	        	if (equal)
	            	where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
//	        	else
//	        		where.setAttribute(QueryConstants.OP,QueryConstants.CONTAINS);
	       		where.setText(valor);
        	//}
		} else if (dv instanceof TimeValue){
			TimeValue tv = (TimeValue)dv;
			Long vMin = tv.getRelativeSecondsMin();
			Long vMax = tv.getRelativeSecondsMax();
			if (vMin!=null) {
				if (vMax!=null) {
					if (vMin.equals(vMax)) {
						where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
						where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
					} else {
						where.setAttribute(QueryConstants.OP,QueryConstants.BETWEEN);
						where.setAttribute(QueryConstants.VAL_MIN,String.valueOf(vMin));
						where.setAttribute(QueryConstants.VAL_MAX,String.valueOf(vMax));
					}
				} else {
					where.setAttribute(QueryConstants.OP,QueryConstants.MAYOR);
					where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
				}
			} else if (vMax!=null) {
				where.setAttribute(QueryConstants.OP,QueryConstants.MENOR);
				where.setAttribute(QueryConstants.VALUE,String.valueOf(vMax));
			}
		}
		//System.out.println("Fin de la funcion createNodeWhere");
        return where;
	}

	private Element createNodeCase(String value, int prop, boolean required, int idto, Integer uTask) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException  {
		//System.out.println("Inicio de la funcion createNodeWhere");
		Element where = new Element(QueryConstants.CASE);
		where.setAttribute(QueryConstants.PROP,String.valueOf(prop));
		where.setAttribute(QueryConstants.NAME_PROP,ik.getPropertyName(prop));
		Integer idtmRuleengine = ik.getDatatype(prop);
		if (idtmRuleengine!=null) {
			where.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(idtmRuleengine));
			
			if (idtmRuleengine==Constants.IDTO_UNIT || idtmRuleengine==Constants.IDTO_INT || idtmRuleengine==Constants.IDTO_DATE || idtmRuleengine==Constants.IDTO_DATETIME
					|| idtmRuleengine==Constants.IDTO_TIME || idtmRuleengine==Constants.IDTO_DOUBLE) {
				Double vMin = Double.parseDouble(value);
				where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
				where.setAttribute(QueryConstants.VALUE,String.valueOf(vMin));
			} else if (idtmRuleengine==Constants.IDTO_BOOLEAN) {
				where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
				if (value.toLowerCase().equals("true"))
					where.setAttribute(QueryConstants.VALUE,"1");
				else if (value.toLowerCase().equals("false"))
					where.setAttribute(QueryConstants.VALUE,"0");
			} else if (idtmRuleengine==Constants.IDTO_STRING || idtmRuleengine==Constants.IDTO_MEMO) {
//	        	if (equal)
	            	where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
//	        	else
//	        		where.setAttribute(QueryConstants.OP,QueryConstants.CONTAINS);
				where.setAttribute(QueryConstants.VALUE,value);
			}
		}
		if (required)
			where.setAttribute(QueryConstants.REQUIRED, "TRUE");
		String alias = ik.getAliasOfProperty(idto, prop, uTask);
		where.setAttribute(QueryConstants.NAME, alias);
		
		where.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
		
		//System.out.println("Fin de la funcion createNodeWhere");
		return where;
	}
	
	/*	
	private Element createWhereImage(String prop) throws DataErrorException {
		//System.out.println("Inicio de la funcion createWhereImage");
		Element where = new Element(QueryConstants.WHERE);
		where.setAttribute(QueryConstants.PROP, prop);
		where.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(Constants.IDTO_IMAGE));
		where.setAttribute(QueryConstants.ID_TM, String.valueOf(QueryConstants.TM_STRING));
		where.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
		this.id++;
		where.setAttribute(QueryConstants.OP,QueryConstants.CONTAINS);
		where.setAttribute(QueryConstants.VALUE,"#");
		//System.out.println("Fin de la funcion createWhereImage");
		return where;
	}
	*/
	
	private Element createWhereValue(int idtoSup, int nuevoIDTO, String idos, int prop, HashMap<String,StringBuffer> orderStringIdo,
			HashMap<String,ArrayList<String>> aIdsFicticios, HashSet<Integer> idoProcesados, Integer uTask, boolean required) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createWhereEnum");

		ArrayList<String> aIdsFicticiosTmp = new ArrayList<String>();
		StringBuffer sbf = new StringBuffer("");
		//TODO preguntar a motor el rango de esta clase con este idto
		//AuxiliarModel.getIdtosThatIPoint(fcdb, idtoSup, prop, false); buscar equivalente en el modelo
		Element clase = createNodeClass(null, nuevoIDTO, idtoSup, prop, sbf, aIdsFicticiosTmp, idoProcesados, uTask);
		if (required)
			clase.setAttribute(QueryConstants.REQUIRED,"TRUE");
		clase.setAttribute(QueryConstants.ID_O, idos);
		
		orderStringIdo.put(clase.getAttributeValue(QueryConstants.ID), sbf);
		aIdsFicticios.put(clase.getAttributeValue(QueryConstants.ID), aIdsFicticiosTmp);
		
		//System.out.println("Fin de la funcion createWhereEnum");
		return clase;
	}
	private Element createCaseEnum(int ido, int idtoSup, String valueRdn, int prop, HashMap<String,StringBuffer> orderStringIdo,
			HashMap<String,ArrayList<String>> aIdsFicticios, HashSet<Integer> idoProcesados, Integer uTask, boolean required) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createCaseEnum");

		ArrayList<String> aIdsFicticiosTmp = new ArrayList<String>();
		StringBuffer sbf = new StringBuffer("");
		int nuevoIDTO = ik.getClassOf(ido);
		Element clase = createNodeClass(ido, nuevoIDTO, idtoSup, prop, sbf, aIdsFicticiosTmp, idoProcesados, uTask);
		clase.setAttribute(QueryConstants.ID_O, String.valueOf(ido));
		
		Element where = new Element(QueryConstants.CASE);
		where.setAttribute(QueryConstants.PROP, String.valueOf(Constants.IdPROP_RDN));
		where.setAttribute(QueryConstants.NAME_PROP,Constants.PROP_RDN);
		where.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(Constants.IDTO_STRING));
		where.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
		this.id++;
		where.setAttribute(QueryConstants.OP,QueryConstants.IGUAL);
		where.setAttribute(QueryConstants.VALUE,valueRdn);
		if (required)
			where.setAttribute(QueryConstants.REQUIRED,"TRUE");
		
		orderStringIdo.put(clase.getAttributeValue(QueryConstants.ID), sbf);
		aIdsFicticios.put(clase.getAttributeValue(QueryConstants.ID), aIdsFicticiosTmp);
		clase.addContent(where);
		
		//System.out.println("Fin de la funcion createCaseEnum");
		return where;
	}
	
	private Element createNodeAttributeCommon(int prop) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException  {
		Element attribute = new Element(QueryConstants.ATTRIBUTE);
		attribute.setAttribute(QueryConstants.PROP,String.valueOf(prop));
		attribute.setAttribute(QueryConstants.NAME_PROP,ik.getPropertyName(prop));
		attribute.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
		this.id++;
		//System.out.println("toTypeQuery, prop:" + prop);
		return attribute;
	}
	private Element createNodeAttributeDP(int prop) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException  {
		//System.out.println("Inicio de la funcion createNodeAttribute");
		Element attribute = createNodeAttributeCommon(prop);
		Integer idtmRuleengine = ik.getDatatype(prop);
		if (idtmRuleengine!=null)
			attribute.setAttribute(QueryConstants.ID_TM_RULEENGINE, String.valueOf(idtmRuleengine));
		//System.out.println("Fin de la funcion createNodeAttribute");
		return attribute;
	}
	
	private ArrayList<Element> createAttribute(int ido, int idto, StringBuffer orderString, StringBuffer orderStringIdoActual,
			ArrayList<String> aIdsFicticios, HashSet<Integer> idoProcesados, Integer uTask, ArrayList<Element> whereValue, ArrayList<Element> aHijosAnd) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createAttribute");
		ArrayList<Element> attributes = new ArrayList<Element>();
		if (select!=null) {
			for (int i=0;i<select.size();i++) {
				//mirar en select los atributos que hay de esta clase
				SelectQuery sq = select.get(i);
				int prop = sq.getIdProp();
				if (/*sq.getIdClass()==idto && */ido==Integer.parseInt(sq.getIdObject())) {
					if (sq.getPropFilter()==null && sq.getValueFilter()==null) {
						if (ik.isDataProperty(prop)) {
							Element attribute = createAttributeShowDP(sq.getAlias(), prop, orderString, orderStringIdoActual, i);
							attributes.add(attribute);
							
							/*Integer idtmRuleengine = ik.getDatatype(prop);
							if (idtmRuleengine!=null && idtmRuleengine.equals(Constants.IDTO_IMAGE)) {
								Element where = createWhereImage(String.valueOf(prop));
								whereValue.add(where);
								Element hijoWhere = new Element(QueryConstants.CONDITION);
						        hijoWhere.setAttribute(QueryConstants.ID_CONDITION,where.getAttributeValue(QueryConstants.ID));
						        aHijosAnd.add(hijoWhere);
							}*/
						} else {
							attributes.addAll(preCreateAttributeShowOP(idto, orderString, orderStringIdoActual, i, aIdsFicticios, idoProcesados, uTask, prop, sq.getAlias()));
						}
					}
				}
			}
		} else {
			//mirar en instance las dataProperties que hay de esa clase
			HashSet<Integer> props = ik.getAllIDsPropertiesOfClass(idto);
			Iterator it = props.iterator();
			while (it.hasNext()) {
				Integer prop = (Integer)it.next();
				String alias = ik.getAliasOfProperty(idto, prop, uTask);
				if (ik.isDataProperty(prop)) {
					Element attribute = createAttributeShowDP(alias, prop, orderString, orderStringIdoActual, null);
					attributes.add(attribute);
				} else {
					attributes.addAll(preCreateAttributeShowOP(idto, orderString, orderStringIdoActual, null, aIdsFicticios, idoProcesados, uTask, prop, alias));
				}
			}
		}
		//System.out.println("Fin de la funcion createAttribute");
		return attributes;
	}
	
	private ArrayList<Element> preCreateAttributeShowOP(int idto, StringBuffer orderString, StringBuffer orderStringIdoActual, Integer selectPosition, 
			ArrayList<String> aIdsFicticios, HashSet<Integer> idoProcesados, Integer uTask, int sqProp, String sqAlias) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		ArrayList<Element> attributes = new ArrayList<Element>();
		
		HashMap<String,StringBuffer> orderStringIdoTmp = new HashMap<String, StringBuffer>();
		HashMap<String,ArrayList<String>> hIdsFicticiosTmp = new HashMap<String, ArrayList<String>>();
		
		//no puedo sacar de ningun sitio el ido al q apunta
		ArrayList<Element> aProp = createAttributeShowOP(null, idto, sqAlias, sqProp, orderString, orderStringIdoTmp, selectPosition, hIdsFicticiosTmp, idoProcesados, uTask, false);
		for (int j=0;j<aProp.size();j++) {
			Element attrClassChild = aProp.get(j);
			if (!Constants.isIDTemporal(idto)) {
				String property = attrClassChild.getAttributeValue(QueryConstants.PROP);
				String alias = ik.getAliasOfProperty(idto, Integer.parseInt(property), uTask);
				attrClassChild.setAttribute(QueryConstants.NAME, alias);
			}
			attributes.add(attrClassChild);
																		
			String idClass = attrClassChild.getAttributeValue(QueryConstants.ID);
			ArrayList<String> aIds = hIdsFicticiosTmp.get(idClass);
			if (aIds!=null)
				aIdsFicticios.addAll(aIds);
			StringBuffer orderStringIdoActualTmp = orderStringIdoTmp.get(idClass);
			if (orderStringIdoActualTmp!=null)
				orderStringIdoActual.append(orderStringIdoActualTmp);
		}
		return attributes;
	}
	
	private Element createAttributeShowDP(String aliasName, int prop, StringBuffer orderString, StringBuffer orderStringIdo, Integer selectPosition) 
			throws DataErrorException, NotFoundException, IncoherenceInMotorException  {
		Element attribute = createNodeAttributeDP(prop);
		if (aliasName!=null)
			attribute.setAttribute(QueryConstants.NAME,aliasName);
		if (orderString.length()>0)
			orderString.append(",");
		String idAttribute = attribute.getAttributeValue(QueryConstants.ID);
		orderString.append(idAttribute);
		orderStringIdo.append("," + idAttribute);
		if (selectPosition!=null) {
			//System.err.println("añadiendo en " + selectPosition + " el id de atributo " + idAttribute);
			idNodeXselectPosition.set(selectPosition, idAttribute);
		}
		return attribute;
	}
	private ArrayList<Element> createAttributeShowOP(Integer ido, int idtoSup, String aliasName, int prop, StringBuffer orderString, HashMap<String,StringBuffer> orderStringIdo,
			Integer selectPosition, HashMap<String,ArrayList<String>> aIdsFicticios, HashSet<Integer> idoProcesados, 
			Integer uTask, boolean required) throws DataErrorException, NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException  {
		ArrayList<Element> aClase = new ArrayList<Element>();
		ArrayList<String> aIdsFicticiosTmp = new ArrayList<String>();
		StringBuffer sbf = new StringBuffer("");
		
		LinkedList<Integer> listaIdtos = new LinkedList<Integer>();
		if (ido!=null) {
			Integer nuevoIDTO = ik.getClassOf(ido);
			listaIdtos.add(nuevoIDTO);
		} else {
			//obtengo rangoList de la OP
			ObjectProperty op = (ObjectProperty)ik.getProperty(null, idtoSup, prop, null, ik.getUser(), uTask, ik.getDefaultSession());
			listaIdtos = op.getRangoList();
		}
		Iterator it = listaIdtos.iterator();
		while (it.hasNext()) {
			Integer nuevoIDTO = (Integer)it.next();
			Element clase = createNodeClass(ido, nuevoIDTO, idtoSup, prop, sbf, aIdsFicticiosTmp, idoProcesados, uTask);
			Element attribute = createAttributeShowDP(aliasName, Constants.IdPROP_RDN, orderString, sbf, selectPosition);
			if (required)
				attribute.setAttribute(QueryConstants.REQUIRED, "TRUE");
			orderStringIdo.put(clase.getAttributeValue(QueryConstants.ID), sbf);
			aIdsFicticios.put(clase.getAttributeValue(QueryConstants.ID), aIdsFicticiosTmp);
			clase.addContent(attribute);
			aClase.add(clase);
		}
		return aClase;
	}
	
	private ArrayList<Element> createAttributeEnum(int ido, int idtoSup, int prop, StringBuffer orderString, HashMap<String,StringBuffer> orderStringIdo, Integer uTask, 
			HashMap<String,ArrayList<String>> aIdsFicticios, HashSet<Integer> idoProcesados, boolean required) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createAttributeEnum");
		String idFilter = null;
		ArrayList<Element> clase = null;
		String alias = null;
		Integer selectPosition = null;
		if (select!=null) {
			for (int i=0;i<select.size();i++) {
				//mirar en select los atributos que hay de esta clase
				SelectQuery sq = select.get(i);
				if (/*sq.getIdClass()==idto && */Integer.parseInt(sq.getIdObject())==ido && Constants.IdPROP_RDN==sq.getIdProp()) {
					idFilter = sq.getIdObject();
					selectPosition = i;
					alias = sq.getAlias();
					break;
				}
			}
		} else
			alias = ik.getAliasOfProperty(idtoSup, prop, uTask);
		
		if (idFilter!=null || select==null) {
			clase = new ArrayList<Element>();
			clase.addAll(createAttributeShowOP(ido, idtoSup, alias, prop, orderString, orderStringIdo, selectPosition, aIdsFicticios, idoProcesados, uTask, required));
		}
		//System.out.println("Fin de la funcion createAttributeEnum");
		return clase;
	}

	private Integer getClassOfRangoList(instance ins, int ido) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException {
		Integer idto = null;
		idto = ik.getClassOf(ido);
		if (idto==null)
			idto = ins.getClassOf(ido);
		return idto;
	}
	private Element containsProp(String prop, ArrayList<Element>attributes) {
		Element attribute = null;
		for (int i=0;i<attributes.size();i++) {
			Element elem = attributes.get(i);
			if (StringUtils.equals(elem.getAttributeValue(QueryConstants.PROP),prop)) {
				attribute = elem;
				break;
			}
		}
		return attribute;
	}

	private Element createNodeClass(Integer nuevoIDO, int nuevoIDTO, int idtoSup, int idProp, StringBuffer orderStringIdo, 
			ArrayList<String> aIdsFicticios, HashSet<Integer> idoProcesados, Integer uTask) 
			throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		Element clase = new Element(QueryConstants.CLASS);
		if (!Constants.isIDTemporal(nuevoIDTO)) {
			//dynagent.common.knowledge.access acc = ik.getAccessOverObject(nuevoIDTO, null, Constants.USER_SYSTEM, uTask);
			//if (acc!=null && acc.getAbstractAccess()) {
				//quitar la abstracta debido a la creación de clases ficticias en ruleengine
				//motor de server no podria saber el idto usado porque no se almacena en bd
				//String idtos = Auxiliar.iteratorIntegerToString(ik.getSpecialized(nuevoIDTO).iterator(), ",");
				//clase.setAttribute(QueryConstants.ID_TO,idtos);
				//clase.setAttribute(QueryConstants.ID_TO_ABSTRACT,String.valueOf(nuevoIDTO));
			//} else
				clase.setAttribute(QueryConstants.ID_TO,String.valueOf(nuevoIDTO));
			String alias = ik.getAliasOfClass(nuevoIDTO, uTask);
			clase.setAttribute(QueryConstants.NAME, alias);
		}
		clase.setAttribute(QueryConstants.PROP,String.valueOf(idProp));
		clase.setAttribute(QueryConstants.NAME_PROP,ik.getPropertyName(idProp));
		if (!ik.isRangeCompatible(idtoSup,idProp,nuevoIDTO)) {
			if (ik.isRangeCompatible(nuevoIDTO,idProp,idtoSup))
			//if (ik.isReversed(idto, op.getIdProp(), nuevoIDTO))
				clase.setAttribute(QueryConstants.REVERSED,"TRUE");
		}
		if (nuevoIDO!=null) {
			clase.setAttribute(QueryConstants.ID_CLASS,String.valueOf(nuevoIDO));
			if (idoProcesados!=null)
				idoProcesados.add(nuevoIDO);
		}
		clase.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
		if (!showIdos)
			aIdsFicticios.add(String.valueOf(this.id));
		orderStringIdo.append("," + String.valueOf(this.id));
		this.id++;
		return clase;
	}
	
	private ArrayList<Element> createOPClass(instance ins, int ido, int idto, StringBuffer orderString, HashMap<String,StringBuffer> orderStringIdo, 
			HashMap<String,ArrayList<String>> aIdsFicticios, ArrayList<Element> aHijosAnd, HashSet<Integer> idoProcesados, Integer uTask) throws NotFoundException, 
				DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, 
				SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createOPClass: " + ido + ", " + idto);
		ArrayList<Element> oProp = new ArrayList<Element>();
		Iterator iterador = ins.getRelationIterator(ido);
		if (iterador!=null) {
			while (iterador.hasNext()) {
				ObjectProperty op = (ObjectProperty)iterador.next();
				//comprobar que coincida el ido
				Integer opId = op.getIdo();
				if (opId!=null && opId.equals(ido)) {
					Integer idProp = op.getIdProp();
					if (idProp!=null) {
						//si select==null iterar solo si es una property estructural
						//if (select!=null || ik.getCategory(idProp).isStructural()) {
							//String prop = String.valueOf(idProp);
							LinkedList<Integer> li = op.getRangoList();
							LinkedList<Value> lv = op.getValues();
							boolean isEnum = false;
							boolean hacerComoClass = false;
							if (op.getEnumList().size()>0) {
								isEnum = true;
								//si valueList vacio, coger enumList para restringirlo a las posibles opciones??
								//mirar cardinalidad maxima
								Integer cardMaxModelo = ik.getMaxPropertyCardinalityOfClass(idto, idProp);
								boolean cardMaxMayorQue1 = cardMaxModelo==null || cardMaxModelo!=null && cardMaxModelo>1;
								if (cardMaxMayorQue1)
									hacerComoClass = true;
							}
							/*System.out.println("Inicio ValueList");
							Iterator it = lv.iterator();
							while (it.hasNext()) {
								System.out.println((Value)it.next());
							}
							System.out.println("Fin ValueList");*/
							if (!isEnum && li.size()>0 || hacerComoClass) {
								//System.out.println("li " + li);
								ArrayList<Element> oPropTmp = new ArrayList<Element>();
								for (int j=0;j<li.size();j++) {
									Integer nuevoIDO = li.get(j);
									//cogerlo de cualquier property, al menos hay una: la property ficticia idProp_Class
									Integer nuevoIDTO = getClassOfRangoList(ins, nuevoIDO);
									if (nuevoIDO!=null && nuevoIDTO!=null) {
										boolean noContains = !idoProcesados.contains(nuevoIDO);
										if (noContains || lv.size()>0) {
											//System.out.println("crea nodo class");
											ArrayList<String> aIdsFicticiosTmp = new ArrayList<String>();
											StringBuffer orderStringIdoActual = new StringBuffer("");
											Element clase = createNodeClass(nuevoIDO, nuevoIDTO, idto, idProp, orderStringIdoActual, aIdsFicticiosTmp, idoProcesados, uTask);
											String idClass = clase.getAttributeValue(QueryConstants.ID);
											orderStringIdo.put(idClass, orderStringIdoActual);
											aIdsFicticios.put(idClass, aIdsFicticiosTmp);
											
											Integer cardMaxModelo = ik.getMaxPropertyCardinalityOfClass(idto, idProp);
											Integer cardMinModelo = ik.getMinPropertyCardinalityOfClass(idto, idProp);
											
											boolean cardMax0 = op.getCardMax()!=null && op.getCardMax()==0 && 
												(cardMaxModelo==null || cardMaxModelo!=null && cardMaxModelo!=0);
											boolean cardMin1 = op.getCardMin()!=null && op.getCardMin()==1 && 
												(cardMinModelo==null || cardMinModelo!=null && cardMinModelo!=1);
											if (!cardMax0) {
												boolean required = false;
												if (cardMin1) {
													clase.setAttribute(QueryConstants.REQUIRED, "TRUE");
													required = true;
												}
												ArrayList<Element> whereImage = new ArrayList<Element>();
												ArrayList<Element> attributes = createAttribute(nuevoIDO, nuevoIDTO, orderString, orderStringIdoActual, aIdsFicticiosTmp, idoProcesados, uTask, whereImage, aHijosAnd);
												ArrayList<Element> where = createWhere(ins, nuevoIDO, aHijosAnd, uTask);
												for (int i=0;i<where.size();i++) {
													Element elemWhere = where.get(i);
													//si es un nodo attribute con required a true, ver si ya esta en el array de attributes
													if (elemWhere.getName().equals(QueryConstants.ATTRIBUTE) && StringUtils.equals(elemWhere.getAttributeValue(QueryConstants.REQUIRED), "TRUE"))  {
														Element attribute = containsProp(elemWhere.getAttributeValue(QueryConstants.PROP), attributes);
														if (attribute==null)
															clase.addContent(elemWhere);
														else
															attribute.setAttribute(QueryConstants.REQUIRED, "TRUE");
													} else {
														clase.addContent(elemWhere);
													}
												}
												for (int i=0;i<attributes.size();i++) {
													Element elemAttribute = attributes.get(i);
													clase.addContent(elemAttribute);
												}
												for (int i=0;i<whereImage.size();i++) {
													Element elemWhereImage = whereImage.get(i);
													clase.addContent(elemWhereImage);
												}
												//boolean hayWhere = where.size()>0 || whereValue.size()>0;
												
												//ver si tiene object property class
												if (noContains) {
													HashMap<String,ArrayList<String>> hIdsFicticiosTmp = new HashMap<String,ArrayList<String>>();
													HashMap<String,StringBuffer> orderStringIdoTmp = new HashMap<String,StringBuffer>();
													ArrayList<Element> OPClass = createOPClass(ins, nuevoIDO, nuevoIDTO, orderString, orderStringIdoTmp, hIdsFicticiosTmp, aHijosAnd, idoProcesados, uTask);
													for (int i=0;i<OPClass.size();i++) {
														Element OPClassChild = OPClass.get(i);
														//si select==null iterar solo si es una property estructural o tiene hijos con condiciones
														boolean hijosValidos = false;
														if (select==null) {
															String propStrClass = OPClassChild.getAttributeValue(QueryConstants.PROP);
															if (propStrClass!=null) {
																Integer propClass = Integer.parseInt(propStrClass);
																if (ik.getCategory(propClass).isStructural())
																	hijosValidos = true;
															}
															if (!hijosValidos) {
																String[] names = new String[1];
																names[0] = QueryConstants.WHERE;
																hijosValidos = OPClassChild.getName().equals(QueryConstants.WHERE)
																	|| jdomParser.findByNameOrIdoReqNull(OPClassChild, true, names, true)!=null;
															}
														} else {
															String[] names = new String[2];
															names[0] = QueryConstants.WHERE;
															names[1] = QueryConstants.ATTRIBUTE;
															hijosValidos = OPClassChild.getName().equals(QueryConstants.ATTRIBUTE) ||
																OPClassChild.getName().equals(QueryConstants.WHERE) //&& (OPClassChild.getAttributeValue(QueryConstants.PROP)==null || Integer.parseInt(OPClassChild.getAttributeValue(QueryConstants.PROP))!=Constants.IdPROP_BUSINESSCLASS) && 
																//(OPClassChild.getAttributeValue(QueryConstants.VALUE)==null || !StringUtils.equals(OPClassChild.getAttributeValue(QueryConstants.VALUE),"0"))
																|| jdomParser.findByNameOrIdoReqNull(OPClassChild, true, names, true)!=null;
														}
														if (hijosValidos) {
															if (!Constants.isIDTemporal(idto)) {
																String property = OPClassChild.getAttributeValue(QueryConstants.PROP);
																String alias = ik.getAliasOfProperty(idto, Integer.parseInt(property), uTask);
																OPClassChild.setAttribute(QueryConstants.NAME, alias);
															}
															clase.addContent(OPClassChild);
															
															String idClassChild = OPClassChild.getAttributeValue(QueryConstants.ID);
															ArrayList<String> aIds = hIdsFicticiosTmp.get(idClassChild);
															if (aIds!=null)
																aIdsFicticiosTmp.addAll(aIds);
															StringBuffer orderStringIdoActualTmp = orderStringIdoTmp.get(idClassChild);
															if (orderStringIdoActualTmp!=null)
																orderStringIdoActual.append(orderStringIdoActualTmp);
														}
													}
												}
												if (select!=null)
													createFilter(ins, oPropTmp, nuevoIDO, nuevoIDTO, orderString, orderStringIdo, aIdsFicticios, idto, idProp, 
															required, cardMax0, cardMin1, aHijosAnd, idoProcesados, uTask);
											} else
												clase.setAttribute(QueryConstants.NULL, "TRUE");
											
											oPropTmp.add(clase);
										}
									}
								}
								attributesIDOREQUIREDClass(oPropTmp, op, lv);
								oProp.addAll(oPropTmp);
								//
							} else if (isEnum) {
								//System.out.println("lv " + lv);
								//System.out.println("li " + li);
								//estoy ante un enumerado
								
								boolean required = false;
								Integer cardMinModelo = ik.getMinPropertyCardinalityOfClass(idto, idProp);
								boolean cardMin1 = op.getCardMin()!=null && op.getCardMin()==1 && 
									(cardMinModelo==null || cardMinModelo!=null && cardMinModelo!=1);
								if (cardMin1)
									required = true;
								
								for (int j=0;j<li.size();j++) {
									Integer nuevoIDO = li.get(j);
									if (nuevoIDO!=null) {
										ArrayList<Element> attribute = createAttributeEnum(nuevoIDO, idto, idProp, orderString, orderStringIdo, uTask, aIdsFicticios, idoProcesados, required);
										if (attribute!=null)
											oProp.addAll(attribute);
									}
								}
								
								HashMap<Integer,HashSet<Integer>> aWhere = new HashMap<Integer, HashSet<Integer>>();
								for (int j=0;j<lv.size();j++) {
									ObjectValue ov = (ObjectValue)lv.get(j);
									//no va a ser necesario restringir por el idto
									Integer nuevoIDO = ov.getValue();
									Integer nuevoIDTO = ik.getClassOf(nuevoIDO);
									if (nuevoIDO!=null) {
										if (!Constants.isIDTemporal(nuevoIDO) /*nuevoIDO>=0 && !Constants.isIDClass(nuevoIDO)*/) {
											HashSet<Integer> hIdos = aWhere.get(nuevoIDTO);
											if (hIdos==null) {
												hIdos = new HashSet<Integer>();
												aWhere.put(nuevoIDTO, hIdos);
											}
											hIdos.add(nuevoIDO);
										}
									}
								}
								Iterator it = aWhere.keySet().iterator();
								while (it.hasNext()) {
									int nuevoIDTO = (Integer)it.next();
									HashSet<Integer> hIdos = aWhere.get(nuevoIDTO);
									boolean requiredWhere = true;//isLv;
									Element where = createWhereValue(idto, nuevoIDTO, Auxiliar.hashSetIntegerToString(hIdos, ","), idProp, orderStringIdo,
											aIdsFicticios, idoProcesados, uTask, requiredWhere);
									oProp.add(where);
//									Element hijoWhere = new Element(QueryConstants.CONDITION);
//							        hijoWhere.setAttribute(QueryConstants.ID_CONDITION,where.getAttributeValue(QueryConstants.ID));
//							        aHijosAnd.add(hijoWhere);
								}
							}
						//}
					}
				}
			}
		}
		//System.out.println("Fin de la funcion createOPClass");
		return oProp;
	}
	
	private void createFilter(instance ins, ArrayList<Element> oPropTmp, int nuevoIDO, int nuevoIDTO, StringBuffer orderString, HashMap<String,StringBuffer> orderStringIdo,
			HashMap<String,ArrayList<String>> aIdsFicticios, int idto, int idProp, boolean required, boolean cardMax0, boolean cardMin1, ArrayList<Element> aHijosAnd, 
			HashSet<Integer> idoProcesados, Integer uTask) throws DataErrorException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//añade nodos CLASS para los filtros indicados en el array de SelectQuery
		HashMap<Integer,HashMap<String,Element>> hFilters = new HashMap<Integer,HashMap<String,Element>>();
		//usamos un mapa de la property con un mapa de value y nodo
		//cada value pertenece a un nodo CLASS y una property puede tener muchos value únicos
		//nodos con filtros (propFilter y valueFilter en SelectQuery)
		for (int i=0;i<select.size();i++) {
			//mirar en select los atributos que hay de esta clase
			SelectQuery sq = select.get(i);
			int propSQ = sq.getIdProp();
			String aliasName = sq.getAlias();
			if (/*sq.getIdClass()==idto && */nuevoIDO==Integer.parseInt(sq.getIdObject()) && ik.isDataProperty(propSQ)) {
				Integer pFilter = sq.getPropFilter();
				String vFilter = sq.getValueFilter();
				if (pFilter!=null && vFilter!=null) {
					//mirar si en oPropTmp esta el nodo CLASS que tenga un WHERE debajo
					//con este filtro -> se podria tener un mapa: 
					//					prop - conjunto de valores y referencia a CLASS tratados
					//si esta en mapa se coge el class del mapa y se añade el atributo,
					//si no, se crea un nodo class
					boolean creacion = false;
					HashMap<String,Element> valuesFilter = hFilters.get(pFilter);
					if (valuesFilter!=null) {
						Element nodeClsRef = valuesFilter.get(vFilter);
						if (nodeClsRef!=null) {
							//añadir attribute
							StringBuffer sbf = new StringBuffer("");
							Element attribute = createAttributeShowDP(aliasName, propSQ, orderString, sbf, i);
							orderStringIdo.put(nodeClsRef.getAttributeValue(QueryConstants.ID), sbf);
							nodeClsRef.addContent(attribute);
						} else
							creacion = true;
					} else
						creacion = true;
					
					if (creacion) {
						ArrayList<String> aIdsFicticiosTmp = new ArrayList<String>();
						StringBuffer sbf = new StringBuffer("");
						Element claseFilter = createNodeClass(nuevoIDO, nuevoIDTO, idto, idProp, sbf, aIdsFicticiosTmp, idoProcesados, uTask);
						orderStringIdo.put(claseFilter.getAttributeValue(QueryConstants.ID), sbf);
						aIdsFicticios.put(claseFilter.getAttributeValue(QueryConstants.ID), aIdsFicticiosTmp);
						if (cardMin1) {
							claseFilter.setAttribute(QueryConstants.REQUIRED, "TRUE");
						}
						//crear el attribute
						Element attribute = createAttributeShowDP(aliasName, propSQ, orderString, sbf, i);
						claseFilter.addContent(attribute);
						
						//si el filtrado es por ObjectProperty crear un nodo CLASS intermedio
						ArrayList<Element> clasesFilter2 = null;
						if (ik.isObjectProperty(pFilter)) {
							//System.out.println("es object property");
							//distinguir entre clase normal y enumerada
							//llamar a un create para filtro
							clasesFilter2 = createFilterCondOP(ins, nuevoIDO, nuevoIDTO, pFilter, vFilter, orderStringIdo, aIdsFicticios, idoProcesados, aHijosAnd, uTask);
							Iterator it = clasesFilter2.iterator();
							while (it.hasNext()) {
								Element claseFilter2 = (Element)it.next();
								boolean add = false;
								Iterator it2 = claseFilter2.getChildren().iterator();
								while (it2.hasNext()) {
									Element claseFilter3 = (Element)it2.next();
									//TODO contemplar aqui que se trata de un array: implementar ID_CASE="x,y,z..."
									if (claseFilter3.getName().equals(QueryConstants.CASE)) {
										attribute.setAttribute(QueryConstants.ID_CASE, claseFilter3.getAttributeValue(QueryConstants.ID));
									} else if (claseFilter3.getName().equals(QueryConstants.CLASS)) {
										attribute.setAttribute(QueryConstants.ID_CASE, claseFilter3.getChild(QueryConstants.CASE).getAttributeValue(QueryConstants.ID));
									}
									add = true;
								}
								if (add)
									claseFilter.addContent(claseFilter2);
							}
						} else if (ik.isDataProperty(pFilter)) {
							//System.out.println("es data property");
							//crear el where
							//System.out.println("propSQ " + pFilter);
							//para saber el tipo de where que tengo que crear debo mirar la property
							Element where = createNodeCase(vFilter, pFilter, true, nuevoIDTO, uTask);
							attribute.setAttribute(QueryConstants.ID_CASE, where.getAttributeValue(QueryConstants.ID));
							claseFilter.addContent(where);
						}
						//actualizamos el mapa
						HashMap<String,Element> aFilter = hFilters.get(pFilter);
						if (aFilter==null)
							aFilter = new HashMap<String,Element>();
						aFilter.put(vFilter, claseFilter);
						hFilters.put(pFilter, aFilter);
						
						oPropTmp.add(claseFilter);
					}
				}
			}
		}
		//
	}
	private ArrayList<Element> createFilterCondOP(instance ins, int ido, int idto, int propF, String valueF, HashMap<String,StringBuffer> orderStringIdo, 
			HashMap<String,ArrayList<String>> aIdsFicticios, HashSet<Integer> idoProcesados, ArrayList<Element> aHijosAnd, Integer uTask) 
					throws NotFoundException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, 
					CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, 
					InstanceLockedException, ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createFilterCondOP");
		ArrayList<Element> oProp = new ArrayList<Element>();
		Iterator iterador = ins.getRelationIterator(ido);
		if (iterador!=null) {
			while (iterador.hasNext()) {
				ObjectProperty op = (ObjectProperty)iterador.next();
				//comprobar que coincida el ido
				Integer opId = op.getIdo();
				Integer idProp = op.getIdProp();
				if (idProp!=null) {
					//String prop = String.valueOf(idProp);
					if (opId!=null && opId.equals(ido) && idProp.equals(propF)) {
						LinkedList<Integer> li = op.getRangoList();
						LinkedList<Value> lv = op.getValues();
						LinkedList<ObjectValue> le = op.getEnumList();
						boolean isEnum = false;
						boolean hacerComoClass = false;
						if (le.size()>0) {
							isEnum = true;
							//mirar cardinalidad maxima
							Integer cardMaxModelo = ik.getMaxPropertyCardinalityOfClass(idto, idProp);
							boolean cardMaxMayorQue1 = cardMaxModelo==null || cardMaxModelo!=null && cardMaxModelo>1;
							if (cardMaxMayorQue1)
								hacerComoClass = true;
						}
						if (!isEnum && li.size()>0 || hacerComoClass) {
							//System.out.println("li " + li);
							ArrayList<Element> oPropTmp = new ArrayList<Element>();
							for (int j=0;j<li.size();j++) {
								Integer nuevoIDO = li.get(j);
								//cogerlo de cualquier property, al menos hay una: la property ficticia idProp_Class
								Integer nuevoIDTO = getClassOfRangoList(ins, nuevoIDO);
								if (nuevoIDO!=null && nuevoIDTO!=null) {
									
									ArrayList<String> aIdsFicticiosTmp = new ArrayList<String>();
									StringBuffer sbf = new StringBuffer("");
									Element clase = createNodeClass(nuevoIDO, nuevoIDTO, idto, idProp, sbf, aIdsFicticiosTmp, null, uTask);
									orderStringIdo.put(clase.getAttributeValue(QueryConstants.ID), sbf);
									aIdsFicticios.put(clase.getAttributeValue(QueryConstants.ID), aIdsFicticiosTmp);
									
									Integer cardMaxModelo = ik.getMaxPropertyCardinalityOfClass(idto, idProp);
									Integer cardMinModelo = ik.getMinPropertyCardinalityOfClass(idto, idProp);

									boolean cardMax0 = op.getCardMax()!=null && op.getCardMax()==0 && 
										(cardMaxModelo==null || cardMaxModelo!=null && cardMaxModelo!=0);
									if (!cardMax0) {
										if (op.getCardMin()!=null && op.getCardMin()==1) {
											if (cardMinModelo==null || cardMinModelo!=null && cardMinModelo!=1) {
												clase.setAttribute(QueryConstants.REQUIRED, "TRUE");
											}
										}
										//si tiene una dprop exists -> atributo required
										//if (hasProperty(ins, ido, Constants.IdPROP_EXISTS))
										//	clase.setAttribute(QueryConstants.REQUIRED,"TRUE");
									} else
										clase.setAttribute(QueryConstants.NULL, "TRUE");
									int propRdn = Constants.IdPROP_RDN;
									//para saber el tipo de where que tengo que crear debo mirar la property
									Element where = createNodeCase(valueF, propRdn, true, nuevoIDTO, uTask);
									clase.addContent(where);
									oPropTmp.add(clase);
								}
							}
							attributesIDOREQUIREDClass(oPropTmp, op, lv);
							oProp.addAll(oPropTmp);
						} else if (isEnum) {
							//System.out.println("lv " + lv);
							//System.out.println("le " + le);
							//System.out.println("valueF " + valueF);
							//estoy ante un enumerado
							for (int j=0;j<le.size();j++) {
								ObjectValue ov = le.get(j);
								Integer value = ov.getValue();
								//System.out.println("value " + value);
								boolean valueValido = false;
								if (value!=null) {
									Property p = ins.getProperty(value, Constants.IdPROP_RDN);
									//System.out.println("p " + p);
									if (p!=null) {
										Value v = p.getUniqueValue();
										String val = v.getValue_s();
										//System.out.println("val " + val);
										if (val.toLowerCase().equals(valueF.toLowerCase()))
											valueValido = true;
									}
								}
								if (valueValido) {
									//iterar xa ver si el value del rdn coincide con el del filter
									//no va a ser necesario restringir por el idto
									//si coincide 
									boolean requiredWhere = false;//isLv;
									Element where = createCaseEnum(value, idto, valueF, idProp, orderStringIdo, aIdsFicticios, idoProcesados, uTask, requiredWhere);  //ov.getValueCls()
									oProp.add(where);
							        break;
								}
							}
						}
					}
				}
			}
		}
		//System.out.println("Fin de la funcion createFilterCondOP");
		return oProp;
	}
	
	private void attributesIDOREQUIREDClass(ArrayList<Element> oPropTmp, ObjectProperty op, LinkedList<Value> lv) 
			throws DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException  {
		Integer idProp = op.getIdProp();
		String prop = String.valueOf(idProp);
		//atributos ID_O, NOT_ID_O y REQUIRED
		//buscar en oProp el nodo class...
		for (int j=0;j<lv.size();j++) {
			ObjectValue ov = (ObjectValue)lv.get(j);
			Integer nuevoIDO = ov.getValue();
			int nuevoIDTO = ov.getValueCls();
			Integer Q = ov.getQ();
			if (Q==null) {
				for (int h=0;h<oPropTmp.size();h++) {
					Element elem = oPropTmp.get(h);
					//specializados de elem.getAttributeValue(QueryConstants.ID_TO)
					String idtoElem = elem.getAttributeValue(QueryConstants.ID_TO);
					//String idtoElem = elem.getAttributeValue(QueryConstants.ID_TO_ABSTRACT);
					//if (idtoElem==null)
						//idtoElem = elem.getAttributeValue(QueryConstants.ID_TO);
					if (StringUtils.equals(idtoElem,String.valueOf(nuevoIDTO)) || ik.isSpecialized(nuevoIDTO,Integer.parseInt(idtoElem))) {
						if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL),"TRUE")) {
							throw new DataErrorException("Cardinalidad máxima 0 en property " + prop + " y tiene valores fijados");
						} else {
							String oldIdo = elem.getAttributeValue(QueryConstants.ID_O);
							if (oldIdo==null) {
								elem.setAttribute(QueryConstants.ID_O,String.valueOf(nuevoIDO));
								//if (isRequired(op, ov.getValue(), ov.getValueCls(), ov.getQ(), aReq))
									elem.setAttribute(QueryConstants.REQUIRED,"TRUE");
							} else {
								if (!Auxiliar.stringToHashSetInteger(oldIdo, ",").contains(nuevoIDO)) {
									oldIdo += "," + nuevoIDO;
									elem.setAttribute(QueryConstants.ID_O,String.valueOf(oldIdo));
								}
							}
						}
						break;
					}
				}
			//} else {
				//tambien tengo que mirar los que tengan Q fijado
				//crear un where y si esta en SelectQuery crear un attribute
				//TODO por hacer
			}
		}
		LinkedList<ObjectValue> lv3 = op.getExcluList();
		//buscar en oProp el nodo class...
		for (int j=0;j<lv3.size();j++) {
			ObjectValue ov = lv3.get(j);
			Integer nuevoIDO = ov.getValue();
			int nuevoIDTO = ov.getValueCls();
			Integer Q = ov.getQ();
			if (Q==null) {
				for (int h=0;h<oPropTmp.size();h++) {
					Element elem = oPropTmp.get(h);
					String idtoElem = elem.getAttributeValue(QueryConstants.ID_TO);
					//String idtoElem = elem.getAttributeValue(QueryConstants.ID_TO_ABSTRACT);
					//if (idtoElem==null)
						//idtoElem = elem.getAttributeValue(QueryConstants.ID_TO);
					if (StringUtils.equals(idtoElem,String.valueOf(nuevoIDTO)) || ik.isSpecialized(nuevoIDTO,Integer.parseInt(idtoElem))) {
						if (StringUtils.equals(elem.getAttributeValue(QueryConstants.NULL),"TRUE")) {
							throw new DataErrorException("Cardinalidad máxima 0 en property " + prop + " y tiene valores fijados en excluList");
						} else {
							String oldIdo = elem.getAttributeValue(QueryConstants.NOT_ID_O);
							if (oldIdo==null) {
								elem.setAttribute(QueryConstants.NOT_ID_O,String.valueOf(nuevoIDO));
								//if (isRequired(op, ov.getValue(), ov.getValueCls(), ov.getQ(), aReq))
									elem.setAttribute(QueryConstants.REQUIRED,"TRUE");
							} else {
								oldIdo += "," + nuevoIDO;
								elem.setAttribute(QueryConstants.NOT_ID_O,String.valueOf(oldIdo));
							}
						}
						break;
					}
				}
			//} else {
				//tambien tengo que mirar los que tengan Q fijado
				//crear un where y si esta en SelectQuery crear un attribute
				//TODO por hacer
			}
		}
	}
	
	private Element createClass(instance ins, ArrayList<Integer> idos, int idto, StringBuffer orderString, 
			StringBuffer orderStringIdoActual, ArrayList<String> aIdsFicticios, 
			ArrayList<Element> aHijosAnd, HashSet<Integer> idoProcesados, Integer uTask) throws NotFoundException, 
			DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, 
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion createClass");
		Element clase = new Element(QueryConstants.CLASS);
		if (!Constants.isIDTemporal(idto)) {
			//dynagent.common.knowledge.access acc = ik.getAccessOverObject(idto, null, Constants.USER_SYSTEM, uTask);
			//if (acc!=null && acc.getAbstractAccess()) {
				//quitar la abstracta debido a la creación de clases ficticias en ruleengine
				//motor de server no podria saber el idto usado porque no se almacena en bd
				//String idtos = Auxiliar.iteratorIntegerToString(ik.getSpecialized(idto).iterator(), ",");
				//clase.setAttribute(QueryConstants.ID_TO,idtos);
			//} else
				clase.setAttribute(QueryConstants.ID_TO,String.valueOf(idto));
			String alias = ik.getAliasOfClass(idto, uTask);
			clase.setAttribute(QueryConstants.NAME,alias);
		}
		
		if (idos.size()==1) {
			int firstIdo = idos.get(0);
			if (!Constants.isIDTemporal(firstIdo) /*firstIdo>=0 && !Constants.isIDClass(firstIdo)*/)
				clase.setAttribute(QueryConstants.ID_O, String.valueOf(firstIdo));
			clase.setAttribute(QueryConstants.ID_CLASS,String.valueOf(firstIdo));
			if (!idoProcesados.contains(firstIdo))
				idoProcesados.add(firstIdo);
		} else {
			String idosS = "";
			for (int i=0;i<idos.size();i++) {
				int ido = idos.get(i);
				if (idosS.length()>0) {
					idosS += ",";
				}
				idosS += ido;
				if (!idoProcesados.contains(ido))
					idoProcesados.add(ido);
			}
			clase.setAttribute(QueryConstants.ID_O, String.valueOf(idosS));
			clase.setAttribute(QueryConstants.ID_CLASS,String.valueOf(idosS));
		}
		clase.setAttribute(QueryConstants.ID, new Integer(this.id).toString());
		orderStringIdoActual.append(this.id);
		this.id++;
		//ver si hay atributos
		for (int j=0;j<idos.size();j++) {
			ArrayList<Element> whereImage = new ArrayList<Element>();
			ArrayList<Element> attributes = createAttribute(idos.get(j), idto, orderString, orderStringIdoActual, aIdsFicticios, idoProcesados, uTask, whereImage, aHijosAnd);
			for (int i=0;i<attributes.size();i++)
				clase.addContent(attributes.get(i));
			for (int i=0;i<whereImage.size();i++)
				clase.addContent(whereImage.get(i));
		}
		for (int j=0;j<idos.size();j++) {
			ArrayList<Element> wheres = createWhere(ins, idos.get(j), aHijosAnd, uTask);
			for (int i=0;i<wheres.size();i++)
				clase.addContent(wheres.get(i));
		}
		//ver si tiene object property class
		for (int j=0;j<idos.size();j++) {
			HashMap<String,ArrayList<String>> aIdsFicticiosTmp = new HashMap<String,ArrayList<String>>();
			HashMap<String,StringBuffer> orderStringIdoTmp = new HashMap<String,StringBuffer>();
			ArrayList<Element> OPClass = createOPClass(ins, idos.get(j), idto, orderString, orderStringIdoTmp, aIdsFicticiosTmp, aHijosAnd, idoProcesados, uTask);
			//separar los ids de cada nodo hijo
			for (int i=0;i<OPClass.size();i++) {
				Element OPClassChild = OPClass.get(i);
				boolean hijosValidos = false;
				if (select==null) {
					String propStrClass = OPClassChild.getAttributeValue(QueryConstants.PROP);
					if (propStrClass!=null) {
						Integer propClass = Integer.parseInt(propStrClass);
						if (ik.getCategory(propClass).isStructural())
							hijosValidos = true;
					}
					if (!hijosValidos) {
						String[] names = new String[1];
						names[0] = QueryConstants.WHERE;
						hijosValidos = OPClassChild.getName().equals(QueryConstants.WHERE)
							|| jdomParser.findByNameOrIdoReqNull(OPClassChild, true, names, true)!=null;
					}
				} else {
					String[] names = new String[2];
					names[0] = QueryConstants.WHERE;
					names[1] = QueryConstants.ATTRIBUTE;
					hijosValidos = OPClassChild.getName().equals(QueryConstants.ATTRIBUTE) ||
						OPClassChild.getName().equals(QueryConstants.WHERE) //&& (OPClassChild.getAttributeValue(QueryConstants.PROP)==null || Integer.parseInt(OPClassChild.getAttributeValue(QueryConstants.PROP))!=Constants.IdPROP_BUSINESSCLASS) && 
						//(OPClassChild.getAttributeValue(QueryConstants.VALUE)==null || !StringUtils.equals(OPClassChild.getAttributeValue(QueryConstants.VALUE),"0"))
						|| jdomParser.findByNameOrIdoReqNull(OPClassChild, true, names, true)!=null;
				}
				if (hijosValidos){
					if (!Constants.isIDTemporal(idto)) {
						String property = OPClassChild.getAttributeValue(QueryConstants.PROP);
						String alias = ik.getAliasOfProperty(idto, Integer.parseInt(property), uTask);
						OPClassChild.setAttribute(QueryConstants.NAME, alias);
					}
					clase.addContent(OPClassChild);
							
					String idClass = OPClassChild.getAttributeValue(QueryConstants.ID);
					ArrayList<String> aIds = aIdsFicticiosTmp.get(idClass);
					if (aIds!=null)
						aIdsFicticios.addAll(aIds);
					StringBuffer sbfT = orderStringIdoTmp.get(idClass);
					if (sbfT!=null)
						orderStringIdoActual.append(sbfT);
				}
			}
		}
		//System.out.println("Fin de la funcion createClass");
		return clase;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	
	public void setSelect(ArrayList<SelectQuery> select) {
		this.select = select;
		this.idNodeXselectPosition = new ArrayList<String>();
		for (int i = 0; i < select.size(); i++) {
			idNodeXselectPosition.add(null);
		}
	}

	public void setShowIdos(boolean showIdos) {
		this.showIdos = showIdos;
	}

	public Element toQueryXMLWithRealIdos(instance ins /*, String user, Integer userRol*/, Integer uTask) throws NotFoundException, 
	DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, 
	RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		this.setShowIdos(true);
		return this.toQueryXML(ins, uTask);
	}
	
	public Element toQueryXML(instance ins /*, String user, Integer userRol*/, Integer uTask) throws NotFoundException, 
			DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion toQueryXML");
		ArrayList<Integer> idos = new ArrayList<Integer>();
		idos.add(ins.getIDO());
		Element root = toQueryXML(ins, idos, ins.getIdTo(), uTask);
		//System.out.println("Fin de la funcion toQueryXML");
		return root;
	}
	public Element toQueryXML(instance ins, ArrayList<Integer> idos, int idto/*, String user, Integer userRol*/, Integer uTask) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, 
			CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, 
			ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException  {
		//System.out.println("Inicio de la funcion toQueryXML con ido idto fijado");
		Element root = new Element(QueryConstants.QUERY);
		Element structure = toQueryStructure(ins, idos, idto, uTask);
		root.addContent(structure);
		
		/*try {
			System.out.println(jdomParser.returnXML(root));
		} catch (JDOMException e) {
			e.printStackTrace();
		}*/
		//System.out.println("Fin de la funcion toQueryXML con ido idto fijado");
		return root;
	}
	
	private Element toQueryStructure(instance ins, ArrayList<Integer> idos, int idto/*, String user, Integer userRol*/, Integer uTask) 
			throws NotFoundException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, 
			CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, 
			ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		//System.out.println("Inicio de la funcion toQueryStructure");
		
		/*System.err.println(ins.toString());
		if (select!=null) {
			System.err.println("SelectQuery");
			for (int i=0;i<select.size();i++)
				System.err.println("Select " + select.get(i).toString());
		}*/
		Element structure = new Element(QueryConstants.STRUCTURE);
		ArrayList<Element> aHijosAnd = new ArrayList<Element>();
		
		StringBuffer orderString = new StringBuffer("");
		StringBuffer orderStringIdo = new StringBuffer("");
		//RequiredDefaultQuery.requiredDefault(ins, ik, idos, aReq);
		//System.out.println("Array de atributos con REQUIRED ");
		//for (int i=0;i<aReq.size();i++)
			//System.out.println("elem " + aReq.get(i).toString());
		HashSet<Integer> idoProcesados = new HashSet<Integer>();
		ArrayList<String> aIdsFicticios = new ArrayList<String>();
		
		Element clase = createClass(ins, idos, idto, orderString, orderStringIdo, aIdsFicticios, aHijosAnd, idoProcesados, uTask);
		structure.addContent(clase);
		
		Element presentation = new Element(QueryConstants.PRESENTATION);
		Element view = new Element(QueryConstants.VIEW);
		view.setAttribute(QueryConstants.ID,"1");
		if (!showIdos) {
			if (aIdsFicticios.size()>0)
				view.setAttribute(QueryConstants.IDS_FICTICIOS,Auxiliar.arrayToString(aIdsFicticios, ","));
		}
		
		String sOrderStringIdo = orderStringIdo.toString();
		String sOrderString = orderString.toString();
		
		if (select!=null) {
			//ordenamos SELECT y SELECT_IDO según el select pedido
			StringBuffer newSOrderStringIdo = new StringBuffer("");
			StringBuffer newSOrderString = new StringBuffer("");
			ArrayList<String> aOSI = Auxiliar.stringToArray(sOrderStringIdo, ",");
			ArrayList<String> aOS = Auxiliar.stringToArray(sOrderString, ",");
			//1º insertamos los idos
			Iterator it = aOSI.iterator();
			while (it.hasNext()) {
				String id = (String)it.next();
				if (!aOS.contains(id)) {
					//es un id
					if (newSOrderStringIdo.length()>0)
						newSOrderStringIdo.append(",");
					newSOrderStringIdo.append(id);
				}
			}
			//ahora añadimos DPs ordenadas
			it = idNodeXselectPosition.iterator();
			while (it.hasNext()) {
				String id = (String)it.next();
				if (newSOrderStringIdo.length()>0)
					newSOrderStringIdo.append(",");
				newSOrderStringIdo.append(id);
				if (newSOrderString.length()>0)
					newSOrderString.append(",");
				newSOrderString.append(id);
			}
			sOrderStringIdo = newSOrderStringIdo.toString();
			sOrderString = newSOrderString.toString();
		}
		view.setAttribute(QueryConstants.SELECT_IDO,sOrderStringIdo);
		//por ahora ningún xml tendrá groupby=true
		if (sOrderString.length()>0)
			view.setAttribute(QueryConstants.SELECT,sOrderString);
		
		view.setAttribute(QueryConstants.ORDERBY_NCOL,"1");
		if (this.limit!=null)
			view.setAttribute(QueryConstants.LIMIT,String.valueOf(this.limit));
		
		//view.setAttribute(QueryConstants.GROUPBY,clase.getAttributeValue(QueryConstants.INDEX));
		//no es necesario crear nodo OP porque todos los WHERE se unen con AND y esto es lo que se hace por defecto cuando no está este nodo
		
		if (aHijosAnd.size()>1 || aHijosAnd.size()==1 && aHijosAnd.get(0).getAttributeValue(QueryConstants.NOT, "TRUE")!=null) {
			//2ª condicion del if temporal, hasta que se permita poner NOT en nodos WHERE
			Element logicaWhere = new Element(QueryConstants.LOGIC_WHERE);
			view.addContent(logicaWhere);
			Element hijoOpAnd = new Element(QueryConstants.OP);
			hijoOpAnd.setAttribute(QueryConstants.ID_OP,String.valueOf(QueryConstants.OP_AND));
			for (int i=0;i<aHijosAnd.size();i++)
				hijoOpAnd.addContent(aHijosAnd.get(i));
			logicaWhere.addContent(hijoOpAnd);
		}
		
		presentation.addContent(view);
		structure.addContent(presentation);
		
		//si se quisiera que el acceso directo del rdn estuviera siempre, se pondría exists a false xa que siempre se incluyera el rdn
		Rdn.insertRDNRoot(QueryConstants.ATTRIBUTE, "0", structure, null, true);
		//System.out.println("Fin de la funcion toQueryStructure");
		return structure;
	}
}
