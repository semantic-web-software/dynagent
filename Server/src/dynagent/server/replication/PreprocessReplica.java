package dynagent.server.replication;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.properties.values.StringValue;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.InstanceService;
import dynagent.server.services.XMLConstants;

public final class PreprocessReplica {
	
	private static Iterator getIndividualIterator(Object data){
		if(data instanceof IndividualData) return ((IndividualData)data).getAIPropertyDef().iterator();
		if(data instanceof Element){
			Element objectsElement = ((Element)data);		
			ArrayList index=new ArrayList();
			elementIterator(objectsElement,index);
			return index.iterator();			
		}
		System.out.println("ERROR CLASE "+(data==null?"DATA NULO":data.getClass().getName()));
		return null;
	}
	
	public static void elementIterator(org.jdom.Element root,ArrayList res) {		
		if (root == null)
			return ;
		res.add(root);
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			elementIterator(child,res);
		}
	}
	
	public static Changes setLocalIdos(Object data, DataBaseMap dataBaseMap,String replicaSource) throws DataErrorException, NumberFormatException, SQLException, NamingException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, JDOMException {
		Changes changes = new Changes();
		
		HashMap<String,String> mapIdnode=new HashMap<String, String>();
		HashMap<String,Integer> mapRdnTableId=new HashMap<String, Integer>();
		HashMap<String,String> factsRdnChanged=new HashMap<String,String>();//
		ArrayList<IPropertyDef> listPropertyToRemove=new ArrayList<IPropertyDef>();
		ArrayList<Element> listElementToRemove=new ArrayList<Element>();
		class rdnfact{
			int ido=0;
			int idto=0;
			String clsname="";
			String rdn=null;
			String rdnunkn=null;
		}
		Integer minTableId=0;
		HashMap<Integer,rdnfact> mapUnknownRdn=new HashMap<Integer, rdnfact>();
		Iterator itr=getIndividualIterator(data);
		while(itr.hasNext()){
			Object item=itr.next();			
			String tableId = ((Element)item).getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
			if(tableId!=null){
				int tableIdInt=Integer.parseInt(tableId);
				minTableId=Math.min(minTableId, tableIdInt);			
			}
			if(item instanceof Element){
				String rdnprev=((Element)item).getAttributeValue(XMLConstants.ATTRIBUTE_RDN_PREV);				
				if(rdnprev!=null){
					factsRdnChanged.put(((Element)item).getAttributeValue(XMLConstants.ATTRIBUTE_RDN), rdnprev);
				}
			}
		}
	
		//reindexo idos positivos, por si son de nuevos individuos (pasa con replicas de la web), ya que puede llegar idos reales que despues pongo en negativo, y como en negativo no se comprime, en caso 
		//de lineas, al descomprimir puede superarse el maximo de Integer
		HashMap<Integer,Integer> reindexMap=new HashMap<Integer,Integer>();
		itr=getIndividualIterator(data);
		while(itr.hasNext()){
			Object item=itr.next();			
			String tableId = ((Element)item).getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
			
			if(tableId!=null){
				int tableIdInt=Integer.parseInt(tableId);
				if(tableIdInt>0){
					Integer newTID=reindexMap.get(tableIdInt);
					if(newTID==null){
						minTableId--;	
						newTID=minTableId;
					}
					
					((Element)item).setAttribute(XMLConstants.ATTRIBUTE_TABLEID,""+newTID);
					reindexMap.put(tableIdInt,newTID);
				}
			}
		}
		
		minTableId=minTableId.intValue()-1;//debo comenzar por uno menos, por si un objeto no existe no asignar el mismo que otro esta siendo creado.Como muy alto será -1 porque todos fueran positivos
		boolean destinationGlobal=false;
		itr=getIndividualIterator(data);
		while(itr.hasNext()){
			Object item=itr.next();			
			

			Integer idto=null;
			Integer idProp=null;
			String rdn=null;

			int order=0;
			String rdnvalue=null;
			
			String className=null;
			String destination=null;
			
			Element child=(Element)item;

			
			className=child.getName();
			if(	className.equals(Constants.DATA_MEMO) || 
				className.equals(XMLConstants.TAG_VALUE)||
				className.equals(XMLConstants.TAG_DATA_PROPERTY) ||
				className.equals(XMLConstants.TAG_OBJECTS)	
				) continue;//memo no tiene idos
			
			
			ClassInfo cinf=dataBaseMap.getClass(className);
			idto=cinf.getIdto();
					
			
			Attribute actionAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
			String actionXML = actionAttribute.getValue();
			String tableIdStr = ((Element)item).getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
			int tableId=0;
			if(tableIdStr!=null){
				tableId=Integer.parseInt(tableIdStr);
			}else{
				try{
				System.out.println("ERROR TABLE ID NULO SET LOCAL IDOS:IDTO"+idto+" XML:"+jdomParser.returnXML(child));
				}catch(Exception e){
				
				}	
			}
			
			rdn=child.getAttributeValue(XMLConstants.ATTRIBUTE_RDN);	
			if(rdn==null){
				try{
					System.out.println("ERROR RDN NULO SET LOCAL IDOS:IDTO"+idto+" XML:"+jdomParser.returnXML(child));
					}catch(Exception e){
					
					}	
			}
			Integer ido=child.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID)==null?
						null:QueryConstants.getIdo(Integer.parseInt(child.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID)),idto);
			
			//el comando &idx& para generar rdn automatico esta motivado porque es necesario sabe el idto de la clase, y el importador xsl no lo sabe
			if(tableId<0 && (rdn.equals("&idx&") || rdn.equals("&amp;idx&amp;")) ){
				rdn=rdn.replaceAll("x", ""+ido);
				child.setAttribute(XMLConstants.ATTRIBUTE_RDN,rdn);
			}
			
			if(rdn.contains("[midelegacion]")){				
				rdn=rdn.replaceAll("\\[midelegacion\\]",InstanceService.getDelegationRdn(dataBaseMap.getFactoryConnectionDB()));
				child.setAttribute("rdn", rdn);
				System.out.println("adaptando mi delegacion "+rdn);
			}
			
			if(rdn!=null && !rdn.equals(rdn.trim())){
				rdn=rdn.trim();
				child.setAttribute(XMLConstants.ATTRIBUTE_RDN,rdn);
			}
			
						
			//rdn previo sería si el valor de rdn esta siendo modificado en la orden,necesito conocerlo para buscar por el en base de datos
			String rdnprev=factsRdnChanged.get(rdn);							
			String rdnMaped=rdnprev!=null?rdnprev:rdn;			
			
			String keyIndiv= ""+idto+"#"+rdnMaped;
			
			Integer procesedIdo=mapRdnTableId.get(keyIndiv);
			if(procesedIdo!=null){
				child.setAttribute(XMLConstants.ATTRIBUTE_TABLEID,String.valueOf(QueryConstants.getTableId(procesedIdo)));
				if(	procesedIdo.intValue()<0 && 
					(Auxiliar.equals(actionXML,XMLConstants.ACTION_CREATE_IF_NOT_EXIST)||actionXML.equals(XMLConstants.ACTION_NEW))){
					
					child.setAttribute(XMLConstants.ATTRIBUTE_ACTION,XMLConstants.ACTION_SET);																																							
				}	
				continue;//ya se resolvera ido en nodo fuente
			}							

			destination=child.getAttributeValue(XMLConstants.ATTRIBUTE_DESTINATIONm);				

			if(actionXML.equals(XMLConstants.ACTION_NEW)) order=action.NEW;
			if(actionXML.equals(XMLConstants.ACTION_SET)||(actionXML.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST) && tableId>0)){
				order=action.SET;
				child.setAttribute(XMLConstants.ATTRIBUTE_ACTION,XMLConstants.ACTION_SET);
			}
			if(actionXML.equals(XMLConstants.ACTION_DEL)) order=action.DEL;									
			
			Integer localIdo=getLocalIdo(dataBaseMap, mapRdnTableId, ido, idto, idProp, rdnMaped, keyIndiv,order,minTableId);
			
			Integer localTableId=QueryConstants.getTableId(localIdo);
			if(minTableId.equals(localTableId)) minTableId=minTableId.intValue()-1;
			
			localIdo=mapRdnTableId.get(keyIndiv);
			if(ido==null) ido=localIdo;
			
			Integer localValue=null;
			child.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(QueryConstants.getTableId(localIdo)));
			//se supone que si es formato xml data no viene rdn value si no un nodo hijo con rdn
			
			if(localIdo!=null){
					System.out.println("ASIGNANDO IDO "+localIdo);
										
					//IDOorder es el ido original, y es necesario para restaurar su valor antes de insertar en tabla de replicas, y asi no perder informacion de su negativididad 
					//solo tendra sentido cuando viene de applet, cuando viende de replica puede no venir
					if(localIdo.intValue()>0){
						//No debo asignar ido order respecto a ido que ha sido reindexado arriba en negativo para evitar problemas de tamaño al compactar
						//se supone factAdapter ya asigno valor
						//child.setAttribute(XMLConstants.ATTRIBUTE_IDO_ORDER, String.valueOf(ido));				
					}else{
						Attribute timestampAttribute = child.getAttribute(XMLConstants.TAG_TIMESTAMP);
						if(timestampAttribute!=null){
							//utilizado para importar xsl
							child.setAttribute(timestampAttribute.getValue(),""+System.currentTimeMillis()/1000);
							child.removeAttribute(timestampAttribute);
						}
					}
					
					if(Auxiliar.equals(actionXML,XMLConstants.ACTION_CREATE_IF_NOT_EXIST)){
						if(localIdo.intValue()>0)	child.setAttribute(XMLConstants.ATTRIBUTE_ACTION,XMLConstants.ACTION_SET);
						else child.setAttribute(XMLConstants.ATTRIBUTE_ACTION,XMLConstants.ACTION_NEW);
					}														
					
					if(localIdo.intValue()<0&&order==action.SET && !(replicaSource!=null && replicaSource.equals("IMPORT"))){	
						//si viene de importacion, debe respetarse un set frente a un new, para evitar errores
						child.setAttribute(XMLConstants.ATTRIBUTE_ACTION,XMLConstants.ACTION_NEW);
					}
					
					//si fuera object prop y el rango es <0, la accion tambien seria new
					if(order==action.NEW && localIdo.intValue()>0  ){					
						child.setAttribute(XMLConstants.ATTRIBUTE_ACTION,XMLConstants.ACTION_SET);
					}
					
					if(localIdo.intValue()<0){					
						String rdnprop=null;
						if(item instanceof IPropertyDef) rdnprop=idProp.equals(2)?((IPropertyDef)item).getVALUE():null;
						else rdnprop=rdn;
						
						//si el dato es un documento XML, ya existe el nodo al que se asigna desde el principo un unico atributo rdn en factsadapter, no hace falta crear un fact
						if(!mapUnknownRdn.containsKey(ido)){
							rdnfact rf=new rdnfact();
							rf.idto=idto;
							rf.ido=localIdo;
							rf.clsname=className;
							rf.rdnunkn=rdn;
							if(rdnprop!=null) rf.rdn=rdnprop;
							mapUnknownRdn.put(ido, rf);
						}else{
							rdnfact tmp=mapUnknownRdn.get(ido);
							if(tmp.rdn==null && rdnprop!=null) tmp.rdn=rdnprop;
						}
					}
				
			}else{
				System.out.println("NULO ASIGNANDO IDO "+localIdo);
				//Esto ocurrira cuando llegue un borrado de un objeto que no esta en la base de datos local. Lo quitamos para evitar problemas.				
				listElementToRemove.add(child);								
			}			
			

		}
		Iterator<Integer> itrru=mapUnknownRdn.keySet().iterator();
		while(itrru.hasNext()){
			Integer key=itrru.next();
			rdnfact rf=mapUnknownRdn.get(key);
			if(rf.rdn==null){															
				ObjectChanged oc=new ObjectChanged();
				oc.setProp(Constants.IdPROP_RDN);
				oc.clsname=rf.clsname;
				oc.setNewValue(new StringValue(rf.rdnunkn));
				changes.addObjectChanged(oc);
			}
		}
		
		
		for(Element child:(ArrayList<Element>)listElementToRemove){
			child.detach();
			List<Element> lc=child.getChildren();
			for(Element cc:lc){
				String p=cc.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
				if(p!=null){
					if(!dataBaseMap.isStructuralProperty(dataBaseMap.getPropertyId(p))){
						//el elemento que llega es el nodo root "objects"
						((Element)data).addContent(cc);
					}
				}
			}						
		}
		return changes;
	}

	private static Integer getLocalIdo(DataBaseMap dataBaseMap,	HashMap<String, Integer> mapRdnTableId,Integer ido, Integer idto, Integer idProp, String rdn, String keyIndiv,int order,Integer minNewId) throws SQLException, NamingException, DataErrorException {
		Integer localIdo=null;
		/* Solo se llega a setLocalIdos con replicas. Si no crequeo los negativos no funciona createINE por ejemplo al importar datos 
		 * if(ido!=null && ido.intValue()<0){
			mapRdnTableId.put(keyIndiv, ido);
			return ido;
		}*/
		if(rdn!=null){
			localIdo=mapRdnTableId.get(keyIndiv);
			if(localIdo==null){
				String tableIdString=getTableIdFromDB(dataBaseMap.getTable(idto),rdn,dataBaseMap.getFactoryConnectionDB(),true);
				if(tableIdString!=null){
					int tableId=Integer.valueOf(tableIdString);
					localIdo=QueryConstants.getIdo(tableId, idto);
					mapRdnTableId.put(keyIndiv, localIdo);
				}
			}
		}
		
		if(localIdo==null && !Auxiliar.equals(idProp,Constants.IdPROP_OBJECTDELETED)/*&& order!=Constants.ACCESS_DEL Comentado para que nos demos cuenta de estos errores*/){//Si no ha sido encontrado en base de datos, usamos el ido remoto cambiandole el signo para que sea una nueva creacion y no vaya a sobreescribir otro individuo
			if(ido==null){
				//si no tiene siquiera asignada una orden negativa le asigno una
				ido=QueryConstants.getIdo(minNewId,idto);				
			}
			localIdo=ido;
			//si no existe en base de datos pero su orden es positiva, la pongo negativa
			if(ido>0){				
				localIdo=QueryConstants.getIdo(-QueryConstants.getTableId(localIdo),idto);
			}
			mapRdnTableId.put(keyIndiv, localIdo);			
		}
		return localIdo;
	}

	public static void setRdns(Element rootElement, DataBaseMap dataBaseMap,HashSet<String> destinationList) throws DataErrorException{
		Iterator<Element> itr=jdomParser.elementsWithAt(rootElement, "IDO", true).iterator();
		HashMap<Integer,String> mapIdoRdn=new HashMap<Integer, String>();
		HashMap<Integer,ArrayList<Element>> mapIdoFacts=new HashMap<Integer, ArrayList<Element>>();
		HashMap<Integer,ArrayList<Element>> mapValueFacts=new HashMap<Integer, ArrayList<Element>>();
		boolean destinationGlobal=false;
		while(itr.hasNext()){
			Element el=itr.next();
			if(el.getName().equals("NEW_FACT") || el.getName().equals("INITIAL_FACT")){
				Integer idto=Integer.valueOf(el.getAttribute("IDTO").getValue());
				Integer ido=Integer.valueOf(el.getAttribute("IDO").getValue());
				if(ido>0){
					int tableId=QueryConstants.getTableId(ido);
					String rdn=el.getAttributeValue("RDN");
					if(rdn==null) rdn=mapIdoRdn.get(ido);
					if(rdn==null){
						rdn=InstanceService.getRdn(dataBaseMap.getFactoryConnectionDB(), dataBaseMap, tableId, idto);
						mapIdoRdn.put(ido, rdn);
					}
					if(rdn==null){
						System.out.println("rdn nulo de ido "+ido);
					}
					if(rdn==null) throw new DataErrorException("El objeto con identificador "+ido+ " de clase "+idto+ " no tiene codigo");
					el.setAttribute("RDN", rdn);
				}else{
					ArrayList<Element> list=mapIdoFacts.get(ido);
					if(list==null){
						list=new ArrayList<Element>();
						mapIdoFacts.put(ido, list);
					}
					list.add(el);
					
					Integer prop=Integer.valueOf(el.getAttribute("PROP").getValue());
					if(prop==Constants.IdPROP_RDN){
						String rdn=el.getText();
						mapIdoRdn.put(ido, rdn);
					}
				}
				
				if(Integer.valueOf(el.getAttribute("PROP").getValue())!=Constants.IdPROP_OBJECTDELETED){
					Integer valueCls=Integer.valueOf(el.getAttribute("VALUECLS").getValue());
					if(!Constants.isDataType(valueCls)){
						Integer value=Integer.valueOf(el.getText());
						if(value>0){
							String rdnValue=el.getAttributeValue("RDNVALUE");
							if(rdnValue==null) rdnValue=mapIdoRdn.get(value);
							if(rdnValue==null){
								int tableIdValue=QueryConstants.getTableId(value);
								rdnValue=InstanceService.getRdn(dataBaseMap.getFactoryConnectionDB(), dataBaseMap, tableIdValue, valueCls);
								if(rdnValue!=null) mapIdoRdn.put(value, rdnValue);
							}
							
							if(rdnValue!=null) el.setAttribute("RDNVALUE", rdnValue);
						}else{
							ArrayList<Element> list=mapValueFacts.get(value);
							if(list==null){
								list=new ArrayList<Element>();
								mapValueFacts.put(value, list);
							}
							list.add(el);
						}
					}
				}
				
				if(!destinationGlobal && el.getName().equals("NEW_FACT")){
					//tener longitud destino cero es lo mismo que se *, es global
					if(el.getAttribute("DESTINATION_SYSTEM")!=null){
						//si es * no necesita hacer split, pongo * que agrupa a todos
						if(el.getAttribute("DESTINATION_SYSTEM").getValue().length()>0&&!el.getAttribute("DESTINATION_SYSTEM").getValue().equals("*")){					
							String destination=el.getAttribute("DESTINATION_SYSTEM").getValue();
							String[] split=destination.split(",");
							for(int i=0;i<split.length;i++){
								destinationList.add(split[i]);
							}
						}else{
							destinationList.clear();
							destinationList.add("*");
							destinationGlobal=true;
						}
					}	
				}
			}
		}
		
		for(Integer ido:mapIdoFacts.keySet()){
			for(Element el:mapIdoFacts.get(ido)){
				if(mapIdoRdn.get(ido)!=null) el.setAttribute("RDN",mapIdoRdn.get(ido));
			}
		}
		for(Integer value:mapValueFacts.keySet()){
			for(Element el:mapValueFacts.get(value)){
				if(mapIdoRdn.get(value)!=null) el.setAttribute("RDNVALUE",mapIdoRdn.get(value));
			}
		}
	}

	/**
	 * Busca el tableId del elemento de la tabla representado por el rdn dado.
	 * 
	 * @param table
	 *            Tabla donde hemos de buscar.
	 * @param rdn
	 *            código por el que debemos filtrar.
	 * @return Identificador del objeto asociado a dicho código.
	 * @throws SQLException
	 * @throws NamingException
	 * @throws DataErrorException
	 *             Si no hay ningun objeto con dicho código.
	 */
	private static String getTableIdFromDB(Table table, String rdn, FactoryConnectionDB fcdb,boolean caseSensitive) throws SQLException, NamingException, DataErrorException{
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		TableColumn rdnColumn = table.getDataPropertyColumn(Constants.IdPROP_RDN);
		GenerateSQL generate=new GenerateSQL(GestorsDBConstants.postgreSQL);
		//comparo insensitive para evitar cree un nuevo objeto
		String sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + table.getName() + cE +
		" WHERE "+ cB +rdnColumn.getColumnName()+cE+"="+gSQL.parseStringToInsert(rdn);
		if(!caseSensitive){
			sql+=" or lower(" + cB + rdnColumn.getColumnName() + cE + ") = lower(" + gSQL.parseStringToInsert(rdn)+") "+
					"order by (case when "+ cB +rdnColumn.getColumnName()+cE+"="+gSQL.parseStringToInsert(rdn)+" then 1 else 0 end) desc";//devuelvo primero la coincidencia exacta
		}
		sql+=" limit 1";
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		if (queryResult.isEmpty()){
			return null;
		}else{
			return queryResult.get(0).get(0);
		}
	}
}
