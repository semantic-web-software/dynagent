package dynagent.server.services.reports.old;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
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
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.xml.QueryXML;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.services.InstanceService;

public class MakeQueryWhere {
	
	private InstanceService m_IS;
	
	public MakeQueryWhere(InstanceService m_IS) {
		this.m_IS = m_IS;
	}
	
	private DataValue buildDataValue(String value,int valueCls){
		DataValue dataV=null;
		String[] buf;
		switch(valueCls){
		case Constants.IDTO_STRING:
			dataV=new StringValue();
			((StringValue)dataV).setValue(value);
			break;
		case Constants.IDTO_MEMO:
			dataV=new StringValue();
			((StringValue)dataV).setValue(value);
			break;
		case Constants.IDTO_FILE:
		case Constants.IDTO_IMAGE:
			dataV=new StringValue();
			((StringValue)dataV).setValue(value);
			break;
		case Constants.IDTO_INT:
			dataV=new IntValue();
			buf=value.split(":");
			System.out.println("Asigna valor int:"+value);
			((IntValue)dataV).setValueMin(new Integer(buf[0]));
			if(buf.length>1)
				((IntValue)dataV).setValueMax(new Integer(buf[1]));
			else ((IntValue)dataV).setValueMax(new Integer(buf[0]));
			break;
		case Constants.IDTO_DOUBLE:
			dataV=new DoubleValue();
			buf=value.split(":");
			System.out.println("Asigna valor double:"+value);
			((DoubleValue)dataV).setValueMin(new Double(buf[0]));
			if(buf.length>1)
				((DoubleValue)dataV).setValueMax(new Double(buf[1]));
			else ((DoubleValue)dataV).setValueMax(new Double(buf[0]));
			break;
		case Constants.IDTO_BOOLEAN:
			dataV=new BooleanValue();
			buf=value.split(":");
			System.out.println("Asigna valor booleano:"+value);
			((BooleanValue)dataV).setBvalue(buf[0].equals("null")?null:new Boolean(buf[0]));
			if(buf.length>1)
				((BooleanValue)dataV).setComment(buf[1].equals("null")?null:buf[1]);
			break;
		case Constants.IDTO_DATE:
			dataV=new TimeValue();
			buf=value.split(":");
			System.out.println("Asigna valor date:"+value);
			((TimeValue)dataV).setRelativeSecondsMin(new Long(buf[0]));
			if(buf.length>1)
				((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[1]));
			else ((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[0]));
			break;
		case Constants.IDTO_DATETIME:
			dataV=new TimeValue();
			buf=value.split(":");
			System.out.println("Asigna valor datetime:"+value);
			((TimeValue)dataV).setRelativeSecondsMin(new Long(buf[0]));
			if(buf.length>1)
				((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[1]));
			else ((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[0]));
			break;
		}
		return dataV;
	}
	
	public Element makeQWhereWithParamas(Integer idto, Element map) throws SQLException, NamingException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, ParseException, OperationNotPermitedException, JDOMException {
//		Element qw= new Element(QueryConstants.QUERY);
//		TClaseDAO cDAO= new TClaseDAO();
//		cDAO.open();
//		TClase cutask=(TClase) cDAO.getAllCond(" IDTO LIKE "+idto).getFirst();
//		cDAO.close();
		InstanceDAO iDAO= new InstanceDAO();
		iDAO.open();
		System.out.println("IDTO LIKE "+idto+ " AND PROPERTY LIKE "+Constants.IdPROP_PARAMS);
		Instance i= (Instance) iDAO.getAllCond(" IDTO LIKE "+idto+ " AND PROPERTY LIKE "+Constants.IdPROP_PARAMS).getFirst();
		iDAO.close();
		String idtoParams= i.getVALUECLS();
		instance ins= new instance(Integer.parseInt(idtoParams), -800);
		
		List lchild=map.getChildren();
		Iterator itchild=lchild.iterator();
		while(itchild.hasNext()){
			Element prop=(Element) itchild.next();
			String idProp=prop.getAttributeValue(QueryConstants.PROP);
			PropertiesDAO pDAO= new PropertiesDAO();
			pDAO.open();
			Properties p=pDAO.getPropertyByID(Integer.parseInt(idProp));
			pDAO.close();
			
			Integer cat=p.getCAT();
			Property pr;
			if (cat==2 /*&& !p.getVALUECLS().equals(Constants.IDTO_ENUMERATED)*/){
				System.err.println("Introduzca un valor para la property: "+p.getNAME());
				String value=Auxiliar.leeTexto("");
				String[] s=value.split(":");
				String sp = "";
				if(p.getVALUECLS().equals(Constants.IDTO_DATE)){
					for(int j=0; j<s.length;j++){
						System.out.println("FECHAS "+s[j]);
						Long c = QueryConstants.dateToSeconds("dd/MM/yy", s[j]);
						if (j==0)
							sp=c.toString();
						else
							sp+=":"+c.toString();
					}
				}else{
					sp=value;
				}
				DataValue val= buildDataValue(sp,p.getVALUECLS());
				pr= new DataProperty();
				pr.setIdo(-800);
				pr.setIdto(Integer.parseInt(idtoParams));
				pr.setName(p.getNAME());
				pr.setIdProp(p.getPROP());
				((DataProperty)pr).setDataType(p.getVALUECLS());
				LinkedList<Value> lvaluelist=new LinkedList<Value>();
				lvaluelist.add(val);
				System.out.println("SYSOUAMI: VALUE->"+val.toString());
				pr.setValues(lvaluelist);
				ins.addProperty(-800, pr);	
			}else{
				System.err.println("Introduzca un valor para la property (debe introducir un nombre de individuo): "+p.getNAME());
				String value=Auxiliar.leeTexto("");
				O_Datos_AttribDAO oDAO = new O_Datos_AttribDAO();
				
				HashMap<Integer,Integer> idoxidto= new HashMap<Integer, Integer>();
				
				String[] s=value.split(":");
				
				for(int j=0; j<s.length;j++){
					oDAO.open();
					LinkedList<Object> listOD=oDAO.getAllCond(" ID_TO=" + p.getVALUECLS() + " AND PROPERTY LIKE "+Constants.IdPROP_RDN+" AND VAL_TEXTO LIKE '"+s[j]+"' ");
					oDAO.close();
					if (listOD.isEmpty()){
						System.err.println("El Individuo "+s[j]+" no existe en la base de datos");
					}else{
						O_Datos_Attrib oda=(O_Datos_Attrib)listOD.getFirst();
						idoxidto.put(oda.getIDO(), oda.getIDTO());
					}
										
				}
				
				
				
				
				
					pr= new ObjectProperty ();
					pr.setIdo(-800);
					pr.setIdto(Integer.parseInt(idtoParams));
					pr.setName(p.getNAME());
					pr.setIdProp(p.getPROP());
					LinkedList<Value> lvaluelist=new LinkedList<Value>();
					LinkedList<Integer> lrango=new LinkedList<Integer>();
					Set<Integer> idos=idoxidto.keySet();
					Iterator<Integer> itidos=idos.iterator();
					while(itidos.hasNext()){
						Integer ido=itidos.next();
						ObjectValue sv=new ObjectValue(ido,idoxidto.get(ido));
						lvaluelist.add(sv);
						if (!lrango.contains(idoxidto.get(ido)))
							lrango.add(idoxidto.get(ido));
					}
					
					pr.setValues(lvaluelist);
								
					((ObjectProperty)pr).setRangoList(lrango);
					ins.addProperty(-800, pr);
					/*DataProperty dpapunta=new DataProperty();
					dpapunta.setIdo(oda.getIDO());
					dpapunta.setIdto(oda.getIDTO());
					dpapunta.setIdProp(Constants.IdPROP_RDN);
					dpapunta.setName("RDN");
					dpapunta.setTypeAccess(access.getAllAccess());
					dpapunta.setDataType(Constants.IDTO_STRING);
					LinkedList<Value> list=new LinkedList<Value>();
					list.add(new StringValue(oda.getVALTEXTO()));
					dpapunta.setValues(list);
					
					ins.addProperty(oda.getIDO(), dpapunta);*/
				
			}
			
		}
		QueryXML q= new QueryXML(m_IS.getIk());
		
		System.out.println("ins " + ins);
		Element qw=q.toQueryXML(ins, Integer.parseInt(idtoParams));	
		return qw;
	}
}
