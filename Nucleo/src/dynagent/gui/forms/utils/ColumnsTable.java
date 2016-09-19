package dynagent.gui.forms.utils;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ColumnProperty;
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
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Utils;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;


public class ColumnsTable implements IColumnProperties{

	//private ArrayList<OrderProperty> orderPropertyList;
	private HashMap<Integer,HashMap<Integer,ArrayList<ColumnProperty>>> hashIdtoColumnProperty;
	ArrayList<Property> listProperties=new ArrayList<Property>();
	//private HashMap<Integer,String> hashPropLabel;
	private static final String UNION_NAMES=" de ";
	private static final int MAX_RELATIVE_COLUMNS_FOR_FILTER=15;
	private static final int MAX_RELATIVE_COLUMNS_FOR_OBJECT=15;
	private static final int MAX_RELATIVE_COLUMNS_FOR_NOT_ESTRUCTURAL=3;
	//private KnowledgeBaseAdapter m_kba;
	boolean isPathVersion;
	
	public ColumnsTable(ArrayList<ColumnProperty> columnPropertyList){
		//this.orderPropertyList=orderPropertyList;
		hashIdtoColumnProperty=new HashMap<Integer,HashMap<Integer,ArrayList<ColumnProperty>>>();
		addColumnPropertyList(columnPropertyList);
	}
	
	
	public void addColumnPropertyList(ArrayList<ColumnProperty> columnPropertyList){
		isPathVersion=columnPropertyList.isEmpty()?true:columnPropertyList.get(0).getIdPropPath()!=null;
		
		Iterator<ColumnProperty> itr=columnPropertyList.iterator();
		while(itr.hasNext()){
			ColumnProperty columnP=itr.next();
			if(hashIdtoColumnProperty.containsKey(columnP.getIdto())){
				HashMap<Integer,ArrayList<ColumnProperty>> hmap=hashIdtoColumnProperty.get(columnP.getIdto());
				if (hmap.containsKey(columnP.getIdtoParent())){
					hmap.get(columnP.getIdtoParent()).add(columnP);
				}else{
					ArrayList<ColumnProperty> list=new ArrayList<ColumnProperty>();
					list.add(columnP);
					hmap.put(columnP.getIdtoParent(),list);
				}
				
			}else{
				HashMap<Integer,ArrayList<ColumnProperty>> hmap= new HashMap<Integer, ArrayList<ColumnProperty>>();
				ArrayList<ColumnProperty> list=new ArrayList<ColumnProperty>();
				list.add(columnP);
				hmap.put(columnP.getIdtoParent(), list);
				hashIdtoColumnProperty.put(columnP.getIdto(),hmap);
			}
		}
		//System.err.println(hashIdtoColumnProperty);
	}
	
	public ArrayList<Column> getColumns(int ido,int idto,int idtoTable,Integer idtoParent,Integer userRol,Integer idtoUserTask,Session sess,KnowledgeBaseAdapter kba,boolean filterMode,boolean tree,boolean structural,boolean onlyFirstLevel,HashMap<String,String> aliasMap) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		ArrayList<Column> columnList=new ArrayList<Column>();
		if(isPathVersion){
			getSubColumnsWithPath(columnList, ido, idto, idtoTable, idtoParent, userRol, idtoUserTask, sess, kba, filterMode, tree, false,structural,structural, onlyFirstLevel, aliasMap);
		}else{
			getSubColumns(columnList,ido,idto,idtoTable,idtoParent,null,userRol,idtoUserTask,sess,new ArrayList<String>(),new ArrayList<String>(),null,null,null,kba,filterMode,tree,false,structural,structural,onlyFirstLevel,aliasMap);
		}
		//System.err.println("columns:"+columnList);
		return columnList;
		
	}
	
	
	private void getSubColumnsWithPath(ArrayList<Column> columnList,int ido,int idto,int idtoTable,Integer idtoParent,Integer userRol,Integer idtoUserTask, Session sess,KnowledgeBaseAdapter kba,boolean filterMode,boolean tree,boolean nullable,boolean structuralTree,boolean structuralRoot,boolean onlyFirstLevel, HashMap<String, String> aliasMap) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//Pedimos la property por si no estuviera cargado en motor, y luego obtenemos su clase
		
		Property propertyRDN=kba.getRDN(ido,idto,userRol,idtoUserTask,sess);
		if(propertyRDN==null){
			System.err.println("Warning: propertyRDN de una columna de la tabla es null, siendo ido:"+ido+" idto:"+idto);
			return;
		}
		//int idto=propertyRDN.getIdto();
		//System.out.println("AMI - IDTO="+idto+" IDTOPARENT="+idtoParent);
		ArrayList<ColumnProperty> listColumns=getListColumns(idtoTable, idtoParent, kba);
		
		ArrayList<String> nameDepth=new ArrayList<String>();
		ArrayList<String> parentTree=new ArrayList<String>();
		Integer idPropFilter=null;
		String valueFilter=null;
		Property propertyParent=null;
		
		if(!listColumns.isEmpty() && listColumns.get(0).getIdPropPath()!=null){
			Iterator<ColumnProperty> itr=listColumns.iterator();
			boolean exit=false;
			int i=0;
			while(itr.hasNext() && !exit){
				boolean successProperty=false;
				
				//System.err.println("listColumns:"+listColumns);
				ColumnProperty columnP=itr.next();
				
				/*if(filterMode && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_FILTER)
					break;
				else if(!filterMode && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_OBJECT)
					break;
				else */if(columnP.getIdtoParent()==null && !filterMode && !structuralRoot && !onlyFirstLevel && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_NOT_ESTRUCTURAL)
					break;
			
				
				
				Integer idPropFilterP=columnP.getIdPropF();
				String valueFilterP=columnP.getValueFilter();
				String idPropPath=columnP.getIdPropPath();
				if(idPropPath==null){
					continue;
				}
				String[] split=idPropPath.split("#");
				int auxIdo=ido;
				int auxIdto=idto;
				boolean auxNullable=nullable;//Es nullable si alguna de las properties del arbol es nullable
				boolean auxStructuralProp=structuralTree;
				int length=split.length;
				nameDepth.clear();
				parentTree.clear();
				propertyParent=null;
				String namePath="";
				String idPropertyPath="";
				for(int j=0;j<length;j++){
					String idPropString=split[j];
					Integer idProp=kba.getIdProp(idPropString);
					
					boolean successProp=false;
					Property prop=null;
					try{
						if(idProp!=null && kba.hasProperty(auxIdto, idProp)){
							prop=kba.getProperty(auxIdo, auxIdto, idProp, userRol, idtoUserTask, sess);
							successProp=true;
						}
					}finally{
						if(!successProp){
							if(idto!=idtoTable&&!kba.isSpecialized(idto, idtoTable)){
								System.err.println("POSIBLE ERROR: El registro ido:"+auxIdo+"("+kba.getLabelClass(idto,idtoUserTask)+") de la tabla no es acorde con el tipo de la tabla idto:"+kba.getLabelClass(idtoTable,idtoUserTask)+" ");
							}
						}
					}
					if(prop!=null && prop.getTypeAccess().getViewAccess()){
						//System.err.println("------------------------");
						//System.err.println("Property columnsTable:"+prop);
						//System.err.println("columnP:"+columnP);
						//System.err.println("Nivel:"+nivel+" idPropFilter:"+idPropFilter+" idPropFilterP"+idPropFilterP);
						
						String nameP=null;
						if(aliasMap==null || !aliasMap.containsKey(prop.getName())){
							nameP=kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask);
						}else{
							nameP=aliasMap.get(prop.getName());
						}
						
						if(!namePath.isEmpty()){
							namePath+=" | ";
						}
						namePath+=nameP;
						
						if(!idPropertyPath.isEmpty()){
							idPropertyPath+="#";
						}
						idPropertyPath+=idProp;
						
						
						//Tenemos que tener en cuenta el filterMode porque cuando estoy en modo busqueda se muestra la columna del rdn del objectProperty en vez de la columna del objectProperty en si
						//Esto se ha hecho asi porque el server no me devuelve solamente el ido en una consulta, tiene que devolverme el rdn del ido
						if(/*formFactory.isField(prop) &&*/ (prop instanceof DataProperty || (!filterMode && !onlyFirstLevel &&((prop instanceof ObjectProperty) && !((ObjectProperty)prop).getEnumList().isEmpty())))){
							successProperty=addProperty(idProp,prop,propertyParent,namePath,idPropertyPath,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
							break;//Salimos del bucle forzandolo ya que si ha entrado por ser un enumerado, no tiene que seguir con su dataproperty definida (su rdn)
						}else if(!onlyFirstLevel){// Si es una tabla entramos. Ademas si es enumerado y estamos en modo filtro se muestra el rdn del enumerado ya que sera el select de la consulta 
							
							if(tree)
								addProperty(idProp,prop,propertyParent,namePath,idPropertyPath,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
							
							Iterator<ObjectValue> itrIdos;
							int levelObject=kba.getLevelObject(auxIdo);
							
							
							//Si no tiene valor, siendo un individuo o prototipo, obtenemos el idRange ya que si no perderiamos la informacion de las columnas sin valor
							boolean isValue=false;
							if((levelObject==Constants.LEVEL_PROTOTYPE || levelObject==Constants.LEVEL_INDIVIDUAL) && !prop.getValues().isEmpty()){
								itrIdos=kba.getValues((ObjectProperty)prop);
								isValue=true;
							}else{
								Iterator<Integer> itrRange=((ObjectProperty)prop).getRangoList().iterator();
								ArrayList<ObjectValue> list=new ArrayList<ObjectValue>();
								while(itrRange.hasNext()){
									int idRange=itrRange.next();
									int idtoRange=kba.getClass(idRange);
									list.add(new ObjectValue(idRange,idtoRange));
								}
									
								itrIdos=list.iterator();
							}
							
							//System.err.println("ObjectProperty: ido:"+ido+" id:"+id+" levelObject:"+levelObject+" property:"+prop);
		//						int idRange=kba.getIdRange((ObjectProperty)prop);
							while(itrIdos.hasNext()){
								ObjectValue objectValue=itrIdos.next();
								int idoValue=objectValue.getValue();
								int idtoValue=objectValue.getValueCls();
								if(isValue || kba.getLevelObject(idoValue)!=Constants.LEVEL_MODEL){//Si es una clase significa que ya no hay filtro para buscar asi que no descendemos por el
									//Si es Value no necesitamos mirar el nivel. Ademas si lo miraramos y no estuviera cargado seria un problema
									//TODO Revisar ya que si no entra se muestran menos columnas de las definidas, pero como no tenemos filtro...
									boolean found=true;
									if(idPropFilterP!=null && levelObject!=Constants.LEVEL_FILTER){//Solo buscamos el valueFilter si se trata de un individuo o prototipo
										found=false;
										Property propIdo =kba.getProperty(idoValue, idtoValue, idPropFilterP, userRol, idtoUserTask, sess);
										//successProperty=true;//Lo ponemos a true ya que hemos encontrado la property. Ahora falta encontrar el valor. Pero si no se encuentra el valor, successProperty=true es correcto para que no itere por otra columna
										Object value=null;
			    						if(propIdo instanceof DataProperty){
			    							value=kba.getValueData((DataProperty)propIdo);
			    							if(value!=null && value.toString().equalsIgnoreCase(valueFilterP))
			    								found=true;
			    						}else{
			    							Iterator<ObjectValue> itrValuesP=kba.getValues((ObjectProperty)propIdo);
			    							while(itrValuesP.hasNext() && !found){
			    								ObjectValue objectValueP=itrValuesP.next();
			    								int idoP=objectValueP.getValue();
			    								int idtoP=objectValueP.getValueCls();
			    								value=kba.getValueData(kba.getRDN(idoP, idtoP, userRol, idtoUserTask, sess));
			    								if(value!=null && value.toString().equalsIgnoreCase(valueFilterP.toUpperCase()))
			            							found=true;
			    							}
			    						}
									}
									
		    						if(found){
		    							nameDepth.add(nameP);
		    							parentTree.add(prop.getIdo()+","+prop.getIdProp());//propFilter y valueFilter no hace falta porque los padres, en principio, no seran con propertyFilter y valueFilter
		    							auxNullable=(prop.getCardMin()==null || prop.getCardMin()==0) || auxNullable;//Es nullable si alguna de las properties del arbol es nullable
		    							auxStructuralProp=auxStructuralProp && (propertyParent!=null?kba.getCategoryProperty(propertyParent.getIdProp()).isStructural():true);
		    							auxIdo=idoValue;
		    							auxIdto=idtoValue;
		    							propertyParent=prop;
		    						}
								}
							}
						}
					}else{
						break;//Procesamos la siguiente columnproperty ya que hemos encontrado una propiedad que no existe, esta excluida o no se tiene permiso
					}
					
				}
				
//				if(!nameDepth.isEmpty() && (successProperty/* || nameDepth.size()>1*/))//Solo muestra la primera property si no estamos en el root
//					exit=true;
//				
				if(successProperty){
					i++;
				}
				
				
			}
		}else{
			addProperty(propertyRDN.getIdProp(),propertyRDN,propertyParent,kba.getLabelProperty(propertyRDN, propertyRDN.getIdto(), idtoUserTask),String.valueOf(propertyRDN.getIdProp()),userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
			
			if(!onlyFirstLevel){
				//nameDepth.add(name);
				Iterator<ObjectProperty> itrProp=kba.getChildren(ido,idto,userRol,idtoUserTask,sess);
				int i=1;
				while(itrProp.hasNext()){
					if(!filterMode && !structuralRoot && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_NOT_ESTRUCTURAL)
						break;
					nameDepth.clear();
					parentTree.clear();
					ObjectProperty property=itrProp.next();
					//Condicion para mostrar o no en la tabla
					if(property.getTypeAccess().getViewAccess() && property.getCardMin()!=null && property.getCardMin()==1 && property.getCardMax()!=null && property.getCardMax()==1 && property.getEnumList().isEmpty()){
						//System.err.println("property columna defecto:"+property);
						
						//Comprobamos que esta property pertenezca al idto pasado por parametro ya que puede ser, unicamente, de una clase hija por lo que en la tabla no aparecería
						if(kba.hasProperty(idto, property.getIdProp())){
							String nameP=null;
							if(aliasMap==null || !aliasMap.containsKey(property.getName())){
								nameP=kba.getLabelProperty(property, property.getIdto(), idtoUserTask);
							}else{
								nameP=aliasMap.get(property.getName());
							}
							Integer value;
							Integer valueCls;
							int levelObject=kba.getLevelObject(ido);
							boolean isValue=false;
							if((levelObject==Constants.LEVEL_PROTOTYPE || levelObject==Constants.LEVEL_INDIVIDUAL) && !property.getValues().isEmpty()){
								ObjectValue objectValue=kba.getValue(property);
								value=objectValue.getValue();
								valueCls=objectValue.getValueCls();
								isValue=true;
							}else{
								value=kba.getIdRange(property);
								valueCls=kba.getClass(value);
							}
							//TODO Hay que tener en cuenta que el rango de la property puede ser multiple
							
							int idtoR=kba.getClass(kba.getIdRange((ObjectProperty)property));
							
							if(value!=null && (isValue || kba.getLevelObject(value)!=Constants.LEVEL_MODEL) ){//Si es una clase significa que ya no hay filtro para buscar asi que no descendemos por el
								//TODO Revisar ya que si no entra se muestran menos columnas de las definidas, pero como no tenemos filtro...
								
								if(tree)
									addProperty(property.getIdProp(),property,propertyParent,nameP,String.valueOf(property.getIdProp()),userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
								
								nameDepth.add(nameP);
								parentTree.add(property.getIdo()+","+property.getIdProp());//propFilter y valueFilter no hace falta porque los padres, en principio, no seran con propertyFilter y valueFilter
								boolean nullableProp=(property.getCardMin()==null || property.getCardMin()==0) || nullable;//Es nullable si alguna de las properties del arbol es nullable
								boolean structuralProp=structuralTree && (propertyParent!=null?kba.getCategoryProperty(propertyParent.getIdProp()).isStructural():true);

								Property propRDN=kba.getRDN(value,valueCls,userRol,idtoUserTask,sess);
								if(propRDN==null){
									System.err.println("Warning: propertyRDN de una columna de la tabla es null, siendo ido:"+ido+" idto:"+idto);
									return;
								}
								String propPath=nameP+" | "+kba.getLabelProperty(propRDN, propRDN.getIdto(), idtoUserTask);
								String idPropPath=property.getIdProp()+"#"+propRDN.getIdProp();
								boolean successProperty=addProperty(propRDN.getIdProp(),propRDN,property,propPath,idPropPath,userRol,idtoUserTask,sess,nameDepth,parentTree,null,null,nullableProp,structuralProp,columnList,kba,aliasMap);

								
								if(successProperty){
									i++;
								}

							}
						}
					}
				}
				//nameDepth.remove(name);
			}
		}
	
	}
	
	@SuppressWarnings("unchecked")
	private boolean getSubColumns(ArrayList<Column> columnList,int ido,int idto,int idtoTable,Integer idtoParent,Property propertyParent,Integer userRol,Integer idtoUserTask, Session sess,ArrayList<String> nameDepth,ArrayList<String> parentTree,Integer idPropFilter,String valueFilter,Integer nivel,KnowledgeBaseAdapter kba,boolean filterMode,boolean tree,boolean nullable,boolean structuralTree,boolean structuralRoot,boolean onlyFirstLevel, HashMap<String, String> aliasMap) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		boolean success=false;
		//Pedimos la property por si no estuviera cargado en motor, y luego obtenemos su clase
		
		Property propertyRDN=kba.getRDN(ido,idto,userRol,idtoUserTask,sess);
		if(propertyRDN==null){
			System.err.println("Warning: propertyRDN de una columna de la tabla es null, siendo ido:"+ido+" idto:"+idto);
			return false;
		}
		//int idto=propertyRDN.getIdto();
		//System.out.println("AMI - IDTO="+idto+" IDTOPARENT="+idtoParent);
		ArrayList<ColumnProperty> listColumns=getListColumns(idtoTable, idtoParent, kba);
	
		if(!listColumns.isEmpty()){
			Iterator<ColumnProperty> itr=listColumns.iterator();
			boolean exit=false;
			int i=0;
			int j=1;
			HashMap<String,Integer> nivelColProp=new HashMap<String, Integer>();
			while(itr.hasNext() && !exit){
				/*if(filterMode && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_FILTER)
					break;
				else if(!filterMode && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_OBJECT)
					break;
				else */if(!filterMode && !structuralRoot && !onlyFirstLevel && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_NOT_ESTRUCTURAL)
					break;
			
				boolean successProperty=false;
				//System.err.println("listColumns:"+listColumns);
				ColumnProperty columnP=itr.next();
				if(nivel==null || nivel.intValue()<=j/* || idPropFilter!=null*/){
				//System.err.println("listProcessed:"+listProcessedColumn);
				//System.err.println("contains:"+columnP);
				//if(!listProcessedColumn.contains(columnP)){
					//System.err.println("Lo contiene");
					int idProp=columnP.getIdProp();
					Integer idPropFilterP=columnP.getIdPropF();
					String valueFilterP=columnP.getValueFilter();
					
					boolean successProp=false;
					Property prop=null;
					try{
						if(kba.hasProperty(idto, idProp)){
							prop=kba.getProperty(ido, idto, idProp, userRol, idtoUserTask, sess);
							successProp=true;
						}
					}finally{
						if(!successProp){
							if(idto!=idtoTable&&!kba.isSpecialized(idto, idtoTable)){
								System.err.println("POSIBLE ERROR: El registro ido:"+ido+"("+kba.getLabelClass(idto,idtoUserTask)+") de la tabla no es acorde con el tipo de la tabla idto:"+kba.getLabelClass(idtoTable,idtoUserTask)+" ");
							}
						}
					}
					if(prop!=null && prop.getTypeAccess().getViewAccess()){
						//System.err.println("------------------------");
						//System.err.println("Property columnsTable:"+prop);
						//System.err.println("columnP:"+columnP);
						//System.err.println("Nivel:"+nivel+" idPropFilter:"+idPropFilter+" idPropFilterP"+idPropFilterP);
						
						//Tenemos que tener en cuenta el filterMode porque cuando estoy en modo busqueda se muestra la columna del rdn del objectProperty en vez de la columna del objectProperty en si
						//Esto se ha hecho asi porque el server no me devuelve solamente el ido en una consulta, tiene que devolverme el rdn del ido
						if(/*formFactory.isField(prop) &&*/ (prop instanceof DataProperty || (!filterMode && !onlyFirstLevel &&((prop instanceof ObjectProperty) && !((ObjectProperty)prop).getEnumList().isEmpty())))){
							successProperty=addProperty(idProp,prop,propertyParent,null,null,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
						}else if(!onlyFirstLevel){// Si es una tabla entramos. Ademas si es enumerado y estamos en modo filtro se muestra el rdn del enumerado ya que sera el select de la consulta 
							
							if(tree)
								addProperty(idProp,prop,propertyParent,null,null,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
							
							Iterator<ObjectValue> itrIdos;
							int levelObject=kba.getLevelObject(ido);
							String nameP=null;
							if(aliasMap==null || !aliasMap.containsKey(prop.getName())){
								nameP=kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask);
							}else{
								nameP=aliasMap.get(prop.getName());
							}
							
							//Si no tiene valor, siendo un individuo o prototipo, obtenemos el idRange ya que si no perderiamos la informacion de las columnas sin valor
							boolean isValue=false;
							if((levelObject==Constants.LEVEL_PROTOTYPE || levelObject==Constants.LEVEL_INDIVIDUAL) && !prop.getValues().isEmpty()){
								itrIdos=kba.getValues((ObjectProperty)prop);
								isValue=true;
							}else{
								Iterator<Integer> itrRange=((ObjectProperty)prop).getRangoList().iterator();
								ArrayList<ObjectValue> list=new ArrayList<ObjectValue>();
								while(itrRange.hasNext()){
									int idRange=itrRange.next();
									int idtoRange=kba.getClass(idRange);
									list.add(new ObjectValue(idRange,idtoRange));
								}
									
								itrIdos=list.iterator();
							}
							
							//System.err.println("ObjectProperty: ido:"+ido+" id:"+id+" levelObject:"+levelObject+" property:"+prop);
		//						int idRange=kba.getIdRange((ObjectProperty)prop);
							while(itrIdos.hasNext()){
								ObjectValue objectValue=itrIdos.next();
								int idoValue=objectValue.getValue();
								int idtoValue=objectValue.getValueCls();
								if(isValue || kba.getLevelObject(idoValue)!=Constants.LEVEL_MODEL){//Si es una clase significa que ya no hay filtro para buscar asi que no descendemos por el
									//Si es Value no necesitamos mirar el nivel. Ademas si lo miraramos y no estuviera cargado seria un problema
									//TODO Revisar ya que si no entra se muestran menos columnas de las definidas, pero como no tenemos filtro...
									boolean found=true;
									if(idPropFilterP!=null && levelObject!=Constants.LEVEL_FILTER){//Solo buscamos el valueFilter si se trata de un individuo o prototipo
										found=false;
										Property propIdo =kba.getProperty(idoValue, idtoValue, idPropFilterP, userRol, idtoUserTask, sess);
										successProperty=true;//Lo ponemos a true ya que hemos encontrado la property. Ahora falta encontrar el valor. Pero si no se encuentra el valor, successProperty=true es correcto para que no itere por otra columna
										Object value=null;
			    						if(propIdo instanceof DataProperty){
			    							value=kba.getValueData((DataProperty)propIdo);
			    							if(value!=null && value.toString().equalsIgnoreCase(valueFilterP))
			    								found=true;
			    						}else{
			    							Iterator<ObjectValue> itrValuesP=kba.getValues((ObjectProperty)propIdo);
			    							while(itrValuesP.hasNext() && !found){
			    								ObjectValue objectValueP=itrValuesP.next();
			    								int idoP=objectValueP.getValue();
			    								int idtoP=objectValueP.getValueCls();
			    								value=kba.getValueData(kba.getRDN(idoP, idtoP, userRol, idtoUserTask, sess));
			    								if(value!=null && value.toString().equalsIgnoreCase(valueFilterP.toUpperCase()))
			            							found=true;
			    							}
			    						}
									}
									
		    						if(found){
		    							Integer nivelP=nivelColProp.get(columnP.getIdString());
	    								if(nivelP==null){
	    									nivelP=1;
	    								}
	    								else{
	    									nivelP++;
	    								}
	    								nivelColProp.put(columnP.getIdString(), nivelP);
		    							nameDepth.add(nameP);
		    							parentTree.add(prop.getIdo()+","+prop.getIdProp());//propFilter y valueFilter no hace falta porque los padres, en principio, no seran con propertyFilter y valueFilter
		    							int idtoR=kba.getClass(kba.getIdRange((ObjectProperty)prop));
		    							boolean nullableProp=(prop.getCardMin()==null || prop.getCardMin()==0) || nullable;//Es nullable si alguna de las properties del arbol es nullable
		    							boolean structuralProp=structuralTree && (propertyParent!=null?kba.getCategoryProperty(propertyParent.getIdProp()).isStructural():true);
		    							successProperty=getSubColumns(columnList,/*idRange*/idoValue,idtoValue,idtoR,prop.getIdto(),prop,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilterP,valueFilterP,nivelP,kba,filterMode,tree,nullableProp,structuralProp,structuralRoot,false,aliasMap);
		    							nameDepth.remove(nameP);
		    							parentTree.remove(prop.getIdo()+","+prop.getIdProp());
		    						}
								}
							}
						}
						
						if(!nameDepth.isEmpty() && (successProperty/* || nameDepth.size()>1*/))//Solo muestra la primera property si no estamos en el root
							exit=true;
						
						if(successProperty){
							success=true;
							i++;
						}
					}
				}
				j++;
			}
		}else{
			
			success=addProperty(propertyRDN.getIdProp(),propertyRDN,propertyParent,null,null,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
							
			if(nameDepth.isEmpty() && !onlyFirstLevel){//Solo lo hace si no hay columnas definidas en base de datos para la clase principal. Ocurre cuando esta en el nivel 0 de profundidad
				//nameDepth.add(name);
				Iterator<ObjectProperty> itrProp=kba.getChildren(ido,idto,userRol,idtoUserTask,sess);
				int i=1;
				while(itrProp.hasNext()){
					if(!filterMode && !structuralRoot && i>=ColumnsTable.MAX_RELATIVE_COLUMNS_FOR_NOT_ESTRUCTURAL)
						break;
					ObjectProperty property=itrProp.next();
					//Condicion para mostrar o no en la tabla
					if(property.getTypeAccess().getViewAccess() && property.getCardMin()!=null && property.getCardMin()==1 && property.getCardMax()!=null && property.getCardMax()==1 && property.getEnumList().isEmpty()){
						//System.err.println("property columna defecto:"+property);
						
						//Comprobamos que esta property pertenezca al idto pasado por parametro ya que puede ser, unicamente, de una clase hija por lo que en la tabla no aparecería
						if(kba.hasProperty(idto, property.getIdProp())){
							String nameP=null;
							if(aliasMap==null || !aliasMap.containsKey(property.getName())){
								nameP=kba.getLabelProperty(property, property.getIdto(), idtoUserTask);
							}else{
								nameP=aliasMap.get(property.getName());
							}
							Integer value;
							Integer valueCls;
							int levelObject=kba.getLevelObject(ido);
							boolean isValue=false;
							if((levelObject==Constants.LEVEL_PROTOTYPE || levelObject==Constants.LEVEL_INDIVIDUAL) && !property.getValues().isEmpty()){
								ObjectValue objectValue=kba.getValue(property);
								value=objectValue.getValue();
								valueCls=objectValue.getValueCls();
								isValue=true;
							}else{
								value=kba.getIdRange(property);
								valueCls=kba.getClass(value);
							}
							//TODO Hay que tener en cuenta que el rango de la property puede ser multiple
							
							int idtoR=kba.getClass(kba.getIdRange((ObjectProperty)property));
							
							if(value!=null && (isValue || kba.getLevelObject(value)!=Constants.LEVEL_MODEL) ){//Si es una clase significa que ya no hay filtro para buscar asi que no descendemos por el
								//TODO Revisar ya que si no entra se muestran menos columnas de las definidas, pero como no tenemos filtro...
								
								if(tree)
									addProperty(property.getIdProp(),property,propertyParent,null,null,userRol,idtoUserTask,sess,nameDepth,parentTree,idPropFilter,valueFilter,nullable,structuralTree,columnList,kba,aliasMap);
								
								nameDepth.add(nameP);
								parentTree.add(property.getIdo()+","+property.getIdProp());//propFilter y valueFilter no hace falta porque los padres, en principio, no seran con propertyFilter y valueFilter
								boolean nullableProp=(property.getCardMin()==null || property.getCardMin()==0) || nullable;//Es nullable si alguna de las properties del arbol es nullable
								boolean structuralProp=structuralTree && (propertyParent!=null?kba.getCategoryProperty(propertyParent.getIdProp()).isStructural():true);
								boolean successProperty=getSubColumns(columnList,value,valueCls,idtoR,property.getIdto(),property,userRol,idtoUserTask,sess,nameDepth,parentTree,null,null,null,kba,filterMode,tree,nullableProp,structuralProp,structuralRoot,false,aliasMap);
								if(successProperty){
									success=true;
									i++;
								}
								nameDepth.remove(nameP);
								parentTree.remove(property.getIdo()+","+property.getIdProp());
							}
						}
					}
				}
				//nameDepth.remove(name);
			}
		}
		return success;
	}
	
	private ArrayList<ColumnProperty> getListColumns(int idto,Integer idtoParent,KnowledgeBaseAdapter kba) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		
		ArrayList<ColumnProperty> listColumns=new ArrayList<ColumnProperty>();
		listColumns.addAll(getSubListColumns(idto, idtoParent, kba));
		if(listColumns.isEmpty()){//Si no hemos encontrado columnas miramos en las especializadas y nos quedamos con las columnas que sean properties que tambien estan en el idto
			HashSet<Integer> listSpecialized=kba.getDirectSpecialized(idto);
			Iterator<Integer> itrSpecialized=listSpecialized.iterator();
			
			//System.err.println("idto:"+idto);
			ArrayList<ArrayList<ColumnProperty>> listArrayColumnProperty=new ArrayList<ArrayList<ColumnProperty>>();
			while(itrSpecialized.hasNext()){
				int idtoSpecialized=itrSpecialized.next();
				//System.err.println("idtoSpecialized:"+idtoSpecialized);
				listArrayColumnProperty.add(getListColumns(idtoSpecialized, idtoParent, kba));
			}
			ArrayList<ColumnProperty> list=buildListColumnsSpecialized(idto/*,listSpecialized*/,listArrayColumnProperty, kba);
			//System.err.println("ListSpecialized:"+list);
			Iterator<ColumnProperty> itr=list.iterator();
			while(itr.hasNext()){
				ColumnProperty columnP=itr.next();
				if((columnP.getIdPropPath()!=null/*Cuando se usa idPropPath*/ || kba.hasProperty(idto, columnP.getIdProp())) && (columnP.getIdPropF()==null || kba.hasProperty(idto, columnP.getIdPropF())) ){//Si la property tambien esta en nuestra clase es que la tiene por herencia, por lo que nos vale como columna
					//System.err.println("Column Specialized:"+columnP+" siendo idto:"+idto);
					listColumns.add(columnP);
				}
			}
		}
		if(listColumns.isEmpty()){
			HashSet<Integer> listParent=kba.getParents(idto);//Primero miramos los padres directos
			Iterator<Integer> itr=listParent.iterator();
			while(listColumns.isEmpty() && itr.hasNext()){
				int idtoParentSuperior=itr.next();
				listColumns.addAll(getSubListColumns(idtoParentSuperior, idtoParent, kba));
			}
			if(listColumns.isEmpty()){
				//Miramos en los ancestor que no sean padres directos
				Iterator<Integer> itrAncestors=kba.getAncestors(idto);
				while(listColumns.isEmpty() && itrAncestors.hasNext()){
					int idtoParentSuperior=itrAncestors.next();
					if(!listParent.contains(idtoParentSuperior))
						listColumns.addAll(getSubListColumns(idtoParentSuperior, idtoParent, kba));
				}
			}
		}
		if(!listColumns.isEmpty()){
			//La ordenamos por prioridades
			Collections.sort(listColumns,new Comparator(){
	
				public int compare(Object o1, Object o2) {
					ColumnProperty obj1 = (ColumnProperty) o1;
					ColumnProperty obj2 = (ColumnProperty) o2;
				    //podemos hacer esto porque Integer implementa Comparable
				    return Integer.valueOf(obj1.getPriority()).compareTo(obj2.getPriority());
				}
				
			});
		}
		return listColumns;
	}
	
	private ArrayList<ColumnProperty> getSubListColumns(int idto,Integer idtoParent,KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException {
		ArrayList<ColumnProperty> listColumns=new ArrayList<ColumnProperty>();
		HashMap<Integer, ArrayList<ColumnProperty>> hmap = hashIdtoColumnProperty.get(idto);
		if(hmap!=null){
			if (hmap.containsKey(idtoParent)){
				listColumns.addAll((ArrayList<ColumnProperty>)hmap.get(idtoParent));
			}else{
				//System.err.println("--------------------idtoParent-------------------");
				HashSet<Integer> listParents=kba.getParents(idtoParent);//Primero miramos los padres directos
				Iterator<Integer> itrP=listParents.iterator();
				while(listColumns.isEmpty() && itrP.hasNext()){
					int idtoHereda=itrP.next();
					if (hmap.containsKey(idtoHereda)){
						listColumns.addAll((ArrayList<ColumnProperty>)hmap.get(idtoHereda));
					}
				}
				
				if(listColumns.isEmpty()){
					//Miramos en los ancestor que no sean padres directos
					Iterator<Integer> itrAncestor=kba.getAncestors(idtoParent);
					while(listColumns.isEmpty() && itrAncestor.hasNext()){
						int idtoHereda=itrAncestor.next();
						if(!listParents.contains(idtoHereda)){
							if (hmap.containsKey(idtoHereda)){
								listColumns.addAll((ArrayList<ColumnProperty>)hmap.get(idtoHereda));
							}
						}
					}
				}
				
				if (listColumns.isEmpty() && hmap.containsKey(null))
					listColumns.addAll((ArrayList<ColumnProperty>)hmap.get(null));
				
			}
		}
		
		return listColumns;
	}
	
	/*Devuelve las columnas de las especializadas cuya property existe en el idto. Hace una union de las columnas de los especializados teniendo en cuenta el numero de repeticiones de una property y su prioridad.
	El numero de repeticiones de una property sera el maximo encontrado en uno de sus hijos, no sera el total de repeticiones encontradas. Ademas la prioridad sera la minima(teniendo en cuenta que 0==Max prioridad)
	de las encontradas para una misma property-repeticion
	NOTA: Anteriormente se devolvian las columnas cuya property existia en todas las clases especializadas. Ahora nos interesa mostrar todas las columnas en la tabla aunque no la tengan todos los posibles individuos*/
	private ArrayList<ColumnProperty> buildListColumnsSpecialized(int idto/*,HashSet<Integer> listSpecialized*/,ArrayList<ArrayList<ColumnProperty>> listArrayColumnProperty,KnowledgeBaseAdapter kba){
		ArrayList<ColumnProperty> list=new ArrayList<ColumnProperty>();
		ArrayList<String> columnsProperties=new ArrayList<String>();
		Iterator<ArrayList<ColumnProperty>> itrColumnsPropertiesAll=listArrayColumnProperty.iterator();
		while(itrColumnsPropertiesAll.hasNext()){
			Iterator<ColumnProperty> itrColumnProperties=itrColumnsPropertiesAll.next().iterator();
			ArrayList<String> columnsPropertiesAux=new ArrayList<String>();
			columnsPropertiesAux.addAll(columnsProperties);
			HashMap<String,Integer> mapColumnNumber=new HashMap<String, Integer>();//Nos sirve para saber el numero de veces que se repite la misma property
			while(itrColumnProperties.hasNext()){
				ColumnProperty col=itrColumnProperties.next().clone();//Hacemos un clone para evitar que se quede modificada ya que le cambiamos el idto para que sea el de la padre
				String prop=col.getIdPropPath()!=null?col.getIdPropPath():String.valueOf(col.getIdProp());
				
				String id=prop+":"+col.getIdPropF();//Identificador usado en mapColumnNumber para conocer el numero de repeticiones de la property
				if(mapColumnNumber.get(id)!=null)
					mapColumnNumber.put(id, mapColumnNumber.get(id)+1);
				else mapColumnNumber.put(id, 1);
				
				/*Añadimos el numero de aparicion de la property para poder casar teniendolo en cuenta ya que si no lo hacemos asi
				si un hijo tiene dos veces la misma property y otro hijo la tiene tres veces, solo apareceria dos veces*/
				String idWithNumber=id+":"+mapColumnNumber.get(id);//Identificador necesario para casar univocamente la property y numero de repeticion 
				//Comprobamos que esa property(teniendo en cuenta su numero de repeticion) ya ha sido añadida a partir de otro hijo
				if(!columnsPropertiesAux.contains(idWithNumber) && /*containsProperty(listSpecialized, col.getIdProp(), kba)*/(col.getIdPropPath()!=null || kba.hasProperty(idto, col.getIdProp()))){
					//Si no ha sido añadida la añadimos
					columnsProperties.add(idWithNumber);
					col.setIdto(idto);
					list.add(col);
				}else{
					//Si ya fue añadida, nos quedamos con el priority mas bajo de las dos properties
					if(columnsPropertiesAux.contains(idWithNumber)){
						int i=1;//Nos sirve para identificar en que repeticion de la property nos encontramos
						Iterator<ColumnProperty> itr=list.iterator();
						boolean exit=false;
						while(!exit && itr.hasNext()){
							ColumnProperty columnP=itr.next();
							col.setIdto(idto);
							if(columnP.getIdString().equals(col.getIdString())){//Comprobamos que se trata de la misma property(getIdString no tiene en cuenta priority)
								if(i==mapColumnNumber.get(id)){//Comprobamos que se trata de la misma repeticion
									columnP.setPriority(Math.min(columnP.getPriority(),col.getPriority()));
									exit=true;
								}else i++;//Avanzamos para encontrar la misma repeticion
							}
						}
					}
					columnsPropertiesAux.remove(id);
				}
			}
		}
		return list;
	}
	
	private boolean containsProperty(HashSet<Integer> listIdto,int idProp,KnowledgeBaseAdapter kba){
		Iterator<Integer> itr=listIdto.iterator();
		boolean contains=true;
		while(itr.hasNext() && contains){
			int idto=itr.next();
			contains=kba.hasProperty(idto, idProp);
		}
		
		return contains;
	}
	
	private boolean addProperty(int idProp,Property prop,Property propParent,String propPath,String idPropPath,Integer userRol,Integer idtoUserTask, Session sess,ArrayList<String> nameDepth,ArrayList<String> propTree,Integer idPropFilter,String valueFilter,boolean nullable,boolean structuralTree,ArrayList<Column> columnList,KnowledgeBaseAdapter kba,HashMap<String,String> aliasMap) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		
		String name;
		if(idProp==Constants.IdPROP_RDN){
			if(nameDepth.isEmpty()){
				if(aliasMap==null || !aliasMap.containsKey(prop.getName())){
					name=kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask);//kba.getLabelClass(prop.getIdto(), idtoUserTask);
				}else{
					name=aliasMap.get(prop.getName());
				}
			}else{
				name=nameDepth.get(nameDepth.size()-1);
				if(nameDepth.size()>1)
					name+=UNION_NAMES+nameDepth.get(nameDepth.size()-2);
			}
		}else{
			if(aliasMap==null || !aliasMap.containsKey(prop.getName())){
				name=kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask);
			}else{
				name=aliasMap.get(prop.getName());
			}
			if(idPropFilter!=null)
				name+="("+valueFilter+")";
			else if(!nameDepth.isEmpty())
				name+=UNION_NAMES+nameDepth.get(nameDepth.size()-1);
		}
		if(!nullable){
			Integer cardMin=prop.getCardMin();
			if(cardMin==null || cardMin==0)
				nullable=true;
		}
		String tree=propTree.isEmpty()?null:"";
		Iterator<String> itrTree=propTree.iterator();
		while(itrTree.hasNext()){
			if(!tree.isEmpty())
				tree+="#";
			tree+=itrTree.next();
		}
		Column column=new Column(propParent,prop,propPath,idPropPath,name,idPropFilter,valueFilter,nullable,structuralTree,tree);
		//System.err.println("ColumnList:"+columnList);
		//System.err.println("Columnnnnnnnnn:"+column);
		if(!columnList.contains(column)){
			columnList.add(column);
			return true;
		}else{
			//System.err.println("Ya contiene:"+column);
			return false;
		}
	}
	
	public boolean isPathVersion() {
		return isPathVersion;
	}

}
