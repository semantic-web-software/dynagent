/***
 * instance.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.common.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IdObjectForm;


public class instance implements Serializable, Comparable{
	
	private static final long serialVersionUID = 1L;
	private HashMap<Integer, ArrayList<Property>> properties = new HashMap<Integer, ArrayList<Property>>();
	private HashMap<Integer, ArrayList<ObjectProperty>> relations = new HashMap<Integer, ArrayList<ObjectProperty>>();
	private HashMap<String, Integer> idoFilterXIdo = new HashMap<String, Integer>();
	private HashMap<String, Value> valuesSQ = new HashMap<String, Value>();
	private HashMap<String, Value> valuesColumn = new HashMap<String, Value>();
	private HashMap<String, Integer> idosColumn = new HashMap<String, Integer>();
	private HashMap<String, Integer> idtosColumn = new HashMap<String, Integer>();
	private HashMap<String, String> idsParentColumn = new HashMap<String, String>();
	private HashMap<String, Integer> idosFilterColumn = new HashMap<String, Integer>();
	private HashMap<String, Integer> idtosFilterColumn = new HashMap<String, Integer>();
	private int idTo;
	private int ido;
	private String rdn;
	private boolean filterMode=false;
	
	public instance( /*session ses, int order, */int type, int id, boolean filterMode){
	    this.idTo = type;
	    this.ido = id;
	    this.filterMode=filterMode;
	}
	
	public instance( /*session ses, int order, */int type, int id){
	    this.idTo = type;
	    this.ido = id;

	}
	
    public int getIDO() {
		return ido;
	}
	public void setIDO(int ido) {
		this.ido = ido;
	}

	public int getIdTo() {
		return idTo;
	}
	public void setIdTo(int idTo) {
		this.idTo = idTo;
	}

	public HashMap<Integer, ArrayList<Property>> getProperties() {
		return properties;
	}
	
	public String  toStringPropertiesWithValues() {
		String dev="";
		Iterator it = properties.values().iterator();
        if (it!=null) {
 	        while(it.hasNext()) {
 	        	ArrayList<Property> lp=(ArrayList<Property>)it.next();
 	        	for(int i=0;i<lp.size();i++){
 	        		if(lp.get(i).getValues().size()>0){
 	 	        		dev += lp.get(i).toString();	
 	 	        	}	
 	        		
 	        	}
 	        	
 	        }
        }
        return dev;
	}
        
	
	public void setProperties(HashMap<Integer, ArrayList<Property>> properties) {
		this.properties = properties;
	}

	public HashMap<Integer, ArrayList<ObjectProperty>> getRelations() {
		return relations;
	}
	public void setRelations(HashMap<Integer, ArrayList<ObjectProperty>> relations) {
		this.relations = relations;
	}
	
	/*private String toStringProperty(int ido, ArrayList<Property> procesados) {
    	String dev = "";
    	ArrayList<Property> aProp = (ArrayList<Property>)properties.get(ido);
    	if (aProp!=null) {
		    for( int i=0; i<aProp.size(); i++ ) {
		    	Property prop = (Property)aProp.get(i);
		    	if (!procesados.contains(prop)) {
		    		procesados.add(prop);
			    	System.out.println(prop.getIdProp());
			        dev += prop.toString() + "\n";
			        if (prop instanceof ObjectProperty) {
			        	ObjectProperty op = (ObjectProperty)prop;
						LinkedList<ObjectValue> lov = op.getFilterList();
		        		for (int j=0;j<lov.size();j++) {
			        		Integer sigIdo = lov.get(j).getValue();
					        if (sigIdo!=null)
					        	dev += toStringProperty(sigIdo, procesados);
			        	}
			        }
		        }
	    	}
    	}
    	return dev;
	}
	private String toStringObjectProperty(int ido, ArrayList<ObjectProperty> procesados) {
    	String dev = "";
    	ArrayList<ObjectProperty> aProp = (ArrayList<ObjectProperty>)relations.get(ido);
    	if (aProp!=null) {
		    for( int i=0; i<aProp.size(); i++ ) {
		    	ObjectProperty prop = (ObjectProperty)aProp.get(i);
		    	if (!procesados.contains(prop)) {
		    		procesados.add(prop);
			        dev += prop.toString() + "\n";
		        	ObjectProperty op = (ObjectProperty)prop;
					LinkedList<ObjectValue> lov = op.getFilterList();
	        		for (int j=0;j<lov.size();j++) {
			        	Integer sigIdo = lov.get(j).getValue();
				        if (sigIdo!=null)
				        	dev += toStringObjectProperty(sigIdo, procesados);
		        	}
		    	}
	    	}
    	}
    	return dev;
	}*/
	
	private String toStringProperty(int ido) {
    	String dev = "";
    	ArrayList<Property> aProp = (ArrayList<Property>)properties.get(ido);
    	if (aProp!=null) {
		    for( int i=0; i<aProp.size(); i++ ) {
		    	Property prop = (Property)aProp.get(i);
		    	dev += prop.toString() + "\n";
	    	}
    	}
    	return dev;
	}
	private String toStringObjectProperty(int ido) {
    	String dev = "";
    	ArrayList<ObjectProperty> aProp = (ArrayList<ObjectProperty>)relations.get(ido);
    	if (aProp!=null) {
		    for( int i=0; i<aProp.size(); i++ ) {
		    	ObjectProperty prop = (ObjectProperty)aProp.get(i);
		    	dev += prop.toString() + "\n";
	    	}
    	}
    	return dev;
	}
	
	public void addValueSQ(String sq, Value value) {
		valuesSQ.put(sq, value);
	}
	
	public Value getValueSQ(String sq) {
		return valuesSQ.get(sq);
	}
	
	public void addValueColumn(String column, Value value) {
		valuesColumn.put(column, value);
	}
	
	public Value getValueColumn(String column) {
		return valuesColumn.get(column);
	}
	
	public void addIdoColumn(String column, Integer ido) {
		idosColumn.put(column, ido);
	}
	
	public Integer getIdoColumn(String column) {
		return idosColumn.get(column);
	}
	
	public void addIdtoColumn(String column, Integer idto) {
		idtosColumn.put(column, idto);
	}
	
	public Integer getIdtoColumn(String column) {
		return idtosColumn.get(column);
	}
	
	public void addIdParentColumn(String column, String idParent) {
		idsParentColumn.put(column, idParent);
	}
	
	public String getIdParentColumn(String column) {
		return idsParentColumn.get(column);
	}
	
	public void addIdoFilterColumn(String column, Integer idoFilter) {
		idosFilterColumn.put(column, idoFilter);
	}
	
	public Integer getIdoFilterColumn(String column) {
		return idosFilterColumn.get(column);
	}
	
	public void addIdtoFilterColumn(String column, Integer idtoFilter) {
		idtosFilterColumn.put(column, idtoFilter);
	}
	
	public Integer getIdtoFilterColumn(String column) {
		return idtosFilterColumn.get(column);
	}

	
	public String toString() {
    	String dev = "INSTANCE:\n";
    	dev += "IDO: " + ido + " IDTO: " + idTo + " RDN: " + rdn + "\n";
    	dev += "MAPA SELECTQUERY - VALUES:\n";
    	Iterator it = valuesSQ.keySet().iterator();
        if (it!=null) {
 	        while(it.hasNext()) {
 	        	String sq = (String)it.next();
 	        	dev += "SelectQuery " + sq;
 	        	Value value = valuesSQ.get(sq);
 	        	dev += " - Value " + value.toString() + "\n";
 	        }
        }
    	dev += "PROPERTIES:\n";
    	//ArrayList<Property> procesadosP = new ArrayList<Property> ();
    	//dev += toStringProperty(ido, procesadosP);
    	it = properties.keySet().iterator();
        if (it!=null) {
 	        while(it.hasNext()) {
 	        	Integer ido = (Integer)it.next();
 	        	dev += toStringProperty(ido);
 	        }
        }

    	dev += "\nRELATIONS:\n";
    	//ArrayList<ObjectProperty> procesadosOP = new ArrayList<ObjectProperty> ();
    	//dev += toStringObjectProperty(ido, procesadosOP);
    	it = relations.keySet().iterator();
        if (it!=null) {
 	        while(it.hasNext()) {
 	        	Integer ido = (Integer)it.next();
 	        	dev += toStringObjectProperty(ido);
 	        }
        }

    	dev += "\n\n";
	    return dev;
    }
	
	
	public void addProperties(int ido, ArrayList<Property> aProp) {
		if(properties.containsKey(new Integer(ido))){
			if(properties.get(new Integer(ido)) != null){
				ArrayList<Property> prop = properties.get(new Integer(ido));
				Iterator iterador = aProp.iterator();
				while (iterador.hasNext()) {
					Property p = (Property)iterador.next();
					updateRdn(p);
					if(!contieneP(prop,p)) {
						prop.add(p);
						if(p instanceof ObjectProperty) {
							if(relations.containsKey(new Integer(ido))){
								if(relations.get(new Integer(ido)) != null){
									ArrayList<ObjectProperty> r = relations.get(new Integer(ido));
									if(!contieneOP(r,(ObjectProperty)p))
										r.add((ObjectProperty)p);
								}
							} else {
								ArrayList<ObjectProperty> r = new ArrayList<ObjectProperty>();
								r.add((ObjectProperty)p);
								relations.put(new Integer(ido), r);
							}
						}
					}
				}
			}
		} else {
			ArrayList<Property> prop = new ArrayList<Property>();
			ArrayList<ObjectProperty> r = new ArrayList<ObjectProperty>();
			Iterator iterador = aProp.iterator();
			while (iterador.hasNext()) {
				Property p = (Property)iterador.next();
				updateRdn(p);
				prop.add(p);
				if(p instanceof ObjectProperty)
					r.add((ObjectProperty)p);
			}
			properties.put(new Integer(ido), prop);
			if (r.size()>0)
				relations.put(new Integer(ido), r);
		}
	}
	
	private void updateRdn(Property p) {
		if (p.getIdo()!=null && this.ido==p.getIdo() && p.getIdProp()!=null && p.getIdProp()==Constants.IdPROP_RDN && p instanceof DataProperty) {
			DataProperty dp = (DataProperty)p;
        	LinkedList<Value> ldv = dp.getValues();
        	if (ldv.size()>0) {
    			DataValue dv = (DataValue)ldv.get(0);
        		if (dv instanceof StringValue) {
					StringValue sv = (StringValue)dv;
	    			this.rdn = sv.getValue();
				}
        	}
		}
	}
	
	private boolean equalsP(Integer c1, Integer c2) {
		boolean igual = false;
		if (c1==null && c2==null || c1!=null && c2!=null && c1.equals(c2))
			igual = true;
		return igual;
	}
	private boolean equalsP(Double c1, Double c2) {
		boolean igual = false;
		if (c1==null && c2==null || c1!=null && c2!=null && c1.equals(c2))
			igual = true;
		return igual;
	}
	
	private boolean equalsLOV(LinkedList<ObjectValue> lov, LinkedList<ObjectValue> alov) {
		boolean igual = false;
		if (lov.size()==alov.size()) {
			if (lov.size()>0) {
				for (int j=0;j<lov.size();j++) {
					Integer valueP = lov.get(j).getValue();
					int classValueP = lov.get(j).getValueCls();
					Integer valueAP = alov.get(j).getValue();
					int classValueAP = alov.get(j).getValueCls();
					if (equalsP(valueP,valueAP) && classValueP==classValueAP)
						igual = true;
					else {
						igual = false;
						break;
					}
				}
			} else
				igual = true;
		}
		return igual;
	}
	private boolean equalsLV(LinkedList<Value> lv, LinkedList<Value> alv) {
		boolean igual = false;
		if (lv.size()==alv.size()) {
			if (lv.size()>0) {
				for (int j=0;j<lv.size();j++) {
					Value av = lv.get(j);
					Value aav = alv.get(j);
					if (av instanceof ObjectValue) {
						Integer valueP = ((ObjectValue)av).getValue();
						int classValueP = ((ObjectValue)av).getValueCls();
						Integer valueAP = ((ObjectValue)aav).getValue();
						int classValueAP = ((ObjectValue)aav).getValueCls();
						if (equalsP(valueP,valueAP) && classValueP==classValueAP)
							igual = true;
						else {
							igual = false;
							break;
						}
					} else if (av instanceof DataValue) {
		    			DataValue dvP = (DataValue)av;
		    			DataValue dvAP = (DataValue)av;
				    	if (dvP!=null && dvAP!=null) {
				    		if (dvP instanceof UnitValue && dvAP instanceof UnitValue) {
				    			UnitValue uvP = (UnitValue)dvP;
				    			UnitValue uvAP = (UnitValue)dvAP;
				    			if (equalsP(uvP.getUnit(), uvAP.getUnit()) && equalsP(uvP.getValueMin(), uvAP.getValueMin()) && equalsP(uvP.getValueMax(), uvAP.getValueMax()))
				    				igual = true;
				    			else {
				    				igual = false;
				    				break;
				    			}
					        } else if (dvP instanceof StringValue && dvAP instanceof StringValue) {
					        	StringValue svP = (StringValue)dvP;
					        	StringValue svAP = (StringValue)dvAP;
				    			if (StringUtils.equals(svP.getValue(), svAP.getValue()))
				    				igual = true;
				    			else {
				    				igual = false;
				    				break;
				    			}
					        }
				    	} else if (dvP==null && dvAP==null)
				    		igual = true;
					}
				}
			} else
				igual = true;
		}
		return igual;
	}
	private boolean equalsLI(LinkedList<Integer> li, LinkedList<Integer> ali) {
		boolean igual = false;
		if (li.size()==ali.size()) {
			if (li.size()>0) {
				for (int j=0;j<li.size();j++) {
					Integer valueP = li.get(j);
					Integer valueAP = ali.get(j);
					if (equalsP(valueP,valueAP))
						igual = true;
					else {
						igual = false;
						break;
					}
				}
			} else
				igual = true;
		}
		return igual;
	}
	
	private boolean equalsAllOP(ObjectProperty aP, ObjectProperty p) {
		boolean igual = false;
		if (equalsP(p.getIdto(),aP.getIdto()) && equalsP(p.getIdo(),aP.getIdo()) && equalsP(p.getIdProp(),aP.getIdProp())) {
			LinkedList<Integer> li = p.getRangoList();
			LinkedList<Integer> ali = aP.getRangoList();
			
			boolean equalsFList = equalsLI(li, ali);
			if (equalsFList) {
				LinkedList<ObjectValue> lov = p.getEnumList();
				LinkedList<ObjectValue> alov = aP.getEnumList();
				boolean equalsEList = equalsLOV(lov, alov);
				if (equalsEList) {
					LinkedList<Value> lov2 = p.getValues();
					LinkedList<Value> alov2 = aP.getValues();
					igual = equalsLV(lov2, alov2);
				}
			}
		}
		return igual;
	}
	
	private boolean contieneOP(ArrayList<ObjectProperty> a, ObjectProperty p) {
//		boolean esta = false;
//		for (int i=0;i<a.size();i++) {
//			ObjectProperty aP = a.get(i);
//			if (aP.equals(p))
//				esta = true;
//			else
//				esta = equalsAllOP(aP,p);
//			if (esta)
//				break;
//		}
//		return esta;
		return a.contains(p);
	}
	private boolean contieneP(ArrayList<Property> a, Property p) {
//		boolean esta = false;
//		for (int i=0;i<a.size();i++) {
//			Property aP = a.get(i);
//			if (aP.equals(p))
//				esta = true;
//			else {
//				if (p instanceof ObjectProperty && aP instanceof ObjectProperty) {
//					ObjectProperty op = (ObjectProperty)p;
//					ObjectProperty aOP = (ObjectProperty)aP;
//					esta = equalsAllOP(aOP,op);
//				} else if (p instanceof DataProperty && aP instanceof DataProperty) {
//					DataProperty dP = (DataProperty)p;
//					DataProperty dAP = (DataProperty)aP;
//					if (equalsP(dP.getIdto(),dAP.getIdto()) && equalsP(dP.getIdo(),dAP.getIdo()) && equalsP(dP.getIdProp(),dAP.getIdProp()) && 
//							equalsP(dP.getDataType(),dAP.getDataType())) {
//						LinkedList<Value> lov = dP.getValues();
//						LinkedList<Value> alov = dAP.getValues();
//						boolean equalsVList = equalsLV(lov, alov);
//						if (equalsVList) {
//							LinkedList<DataValue> lov2 = dP.getEnumList();
//							LinkedList<DataValue> alov2 = dP.getEnumList();
//							esta = equalsLDV(lov2, alov2);
//						}
//					}
//				}
//			}
//			if (esta)
//				break;
//		}
//		return esta;
		return a.contains(p);
	}
	private boolean equalsLDV(LinkedList<DataValue> lov, LinkedList<DataValue> alov) {
		boolean igual = false;
		if (lov.size()==alov.size()) {
			if (lov.size()>0) {
	    		for (int j=0;j<lov.size();j++) {
	    			DataValue dvP = lov.get(j);
	    			DataValue dvAP = alov.get(j);
			    	if (dvP!=null && dvAP!=null) {
			    		if (dvP instanceof UnitValue && dvAP instanceof UnitValue) {
			    			UnitValue uvP = (UnitValue)dvP;
			    			UnitValue uvAP = (UnitValue)dvAP;
			    			if (equalsP(uvP.getUnit(), uvAP.getUnit()) && equalsP(uvP.getValueMin(), uvAP.getValueMin()) && equalsP(uvP.getValueMax(), uvAP.getValueMax()))
			    				igual = true;
			    			else {
			    				igual = false;
			    				break;
			    			}
				        } else if (dvP instanceof StringValue && dvAP instanceof StringValue) {
				        	StringValue svP = (StringValue)dvP;
				        	StringValue svAP = (StringValue)dvAP;
			    			if (StringUtils.equals(svP.getValue(), svAP.getValue()))
			    				igual = true;
			    			else {
			    				igual = false;
			    				break;
			    			}
				        }
			    	} else if (dvP==null && dvAP==null)
			    		igual = true;
	    		}
			} else
				igual = true;
		}
		return igual;
	}

	public void addProperty(int ido, String idoFilter, Property p){
		addProperty(ido, p);
		addIdoFilterXIdo(idoFilter, ido);
	}

	public void addProperty(int ido, Property p){
		updateRdn(p);
		if(properties.containsKey(new Integer(ido))){
			if(properties.get(new Integer(ido)) != null){
				ArrayList<Property> prop = properties.get(new Integer(ido));
				if(!contieneP(prop,p)) {
					prop.add(p);
					if(p instanceof ObjectProperty) {
						if(relations.containsKey(new Integer(ido))){
							if(relations.get(new Integer(ido)) != null){
								ArrayList<ObjectProperty> r = relations.get(new Integer(ido));
								if(!contieneOP(r,(ObjectProperty)p))
									r.add((ObjectProperty)p);
							}
						} else {
							ArrayList<ObjectProperty> r = new ArrayList<ObjectProperty>();
							r.add((ObjectProperty)p);
							relations.put(new Integer(ido), r);
						}
					}
				}
			}
		}else{
			ArrayList<Property> prop = new ArrayList<Property>();
			prop.add(p);
			properties.put(new Integer(ido), prop);
			if(p instanceof ObjectProperty){
				ArrayList<ObjectProperty> r = new ArrayList<ObjectProperty>();
				r.add((ObjectProperty)p);
				relations.put(new Integer(ido), r);
			}
		}
	}
	
	public Iterator getRelationIterator(boolean checkDiferentialExistence){
		if(relations.get(new Integer(this.ido)) != null)
			return relations.get(new Integer(this.ido)).iterator();
		return null;
	}
	
	public Iterator getRelationIterator(int ido){
		if(relations.get(new Integer(ido)) != null)
			return relations.get(new Integer(ido)).iterator();
		return null;
	}
	
/*	public void delRelation( session ses, Integer ido ){
		if(relations.get(new Integer(ido)) != null)
			relations.remove(new Integer(ido));
	}
	
	public boolean hasRelation(Integer ido, boolean checkDiferentialExistence){
        if(this.getRelationIterator(ido) != null)
        	return true;
        else
        	return false;
    }
	
	public boolean hasRelations(boolean check){
		return this.hasRelation(this.ido, check);
	}*/
	
/*	public ObjectProperty getRelation(Integer id){
//        Iterator it = properties.get(new Integer(ido)).iterator();
//        ObjectProperty o = null;
//        boolean encontrada = false;
//        while(it.hasNext() && !encontrada){
//        	Object obj = (Object)it.next();
//        	if (obj instanceof ObjectProperty) {
//	        	o = (ObjectProperty)obj;
//	        	if(o.getIdoRel() == id)
//	        		encontrada = true;
//			}
//        }
//        if(!encontrada)
//        	o = null;
//        return o;
		
        Iterator it = this.getRelationIterator(this.ido);
        ObjectProperty o = null;
        boolean encontrada = false;
        while(it.hasNext() && encontrada == false){
        	o = (ObjectProperty)it.next();
        	if(o.getIdoRel() == id)
        		encontrada = true;
        }
        if(encontrada == false)
        	o = null;
        return o;
    }*/
	
	 /*public Iterator getEstructIterator(boolean checkDiferentialExistence){
	    ArrayList<ObjectProperty> res= new ArrayList<ObjectProperty>();
	    Iterator itr = this.getRelationIterator(checkDiferentialExistence);
	    while(itr.hasNext()){
	    	ObjectProperty p = (ObjectProperty)itr.next();
	    	if(this.getCategory(arg0)p.getIdProp().getCategory() != null){
	    		if(p.getCategory().isStructural() == true){
	    			res.add(p);
	    		}
	    	}
	    }
	    return res.iterator();
	 }*/
	
	private void cloneProperty(int ido, ArrayList<Property> arrayPropertiesClone) {
	   ArrayList<Property> arrayProperties = properties.get(ido);
	   //if (arrayProperties!=null) {
		   for (int i=0;i<arrayProperties.size();i++) {
			   Property p = arrayProperties.get(i);
			   if (p instanceof DataProperty) {
				   DataProperty dp = (DataProperty) p;
				   DataProperty prop = dp.clone();
				   arrayPropertiesClone.add(prop);
			   } else if (p instanceof ObjectProperty) {
		        	ObjectProperty op = (ObjectProperty)p;
					ObjectProperty prop = op.clone();
					arrayPropertiesClone.add(prop);
					/*LinkedList<ObjectValue> lov = op.getFilterList();
					for (int j=0;j<lov.size();j++) {
			        	Integer sigIdo = lov.get(j).getValue();
				        if (sigIdo!=null)
				     	   cloneProperty(sigIdo, arrayPropertiesClone);
			    	}*/
		        }
		   }
	   //}
	}
	
	private void cloneObjectProperty(int ido, ArrayList<ObjectProperty> arrayRelationsClone) {
	   ArrayList<ObjectProperty> arrayProperties = relations.get(ido);
	   //if (arrayProperties!=null) {
		   for (int i=0;i<arrayProperties.size();i++) {
			   ObjectProperty prop = arrayProperties.get(i).clone();
			   arrayRelationsClone.add(prop);
			   /*ObjectProperty op = (ObjectProperty)prop;
				LinkedList<ObjectValue> lov = op.getFilterList();
				for (int j=0;j<lov.size();j++) {
				   Integer sigIdo = lov.get(j).getValue();
			       if (sigIdo!=null)
			    	   cloneObjectProperty(sigIdo, arrayRelationsClone);
			   }*/
		   }
	   //}
	}

	@Override
	public boolean equals(Object o) {
		instance inst=(instance)o;
		boolean equals=false;
		if(inst.getIDO()==this.getIDO() && inst.getIdTo()==this.getIdTo()){
			LinkedList<Property> listPropertiesThis=getAllProperties();
			LinkedList<Property> listPropertiesOther=inst.getAllProperties();
			if(listPropertiesThis.size()==listPropertiesOther.size() && listPropertiesThis.containsAll(listPropertiesOther))
				equals=true;
		}
		
		return equals;
	}

	public Object clone(){
	   instance res= new instance(idTo,ido,filterMode);
	   res.ido=ido;
	   res.idTo=idTo;
	   res.rdn=rdn;
	   res.properties = new HashMap<Integer, ArrayList<Property>>();
	   
       Iterator it = properties.keySet().iterator();
       if (it!=null) {
	        while(it.hasNext()) {
	        	Integer ido = (Integer)it.next();
	        	ArrayList<Property> arrayPropertiesClone = new ArrayList<Property>();
	        	cloneProperty(ido, arrayPropertiesClone);
	        	res.properties.put(ido, arrayPropertiesClone);
	        }
       }
	   
       it = relations.keySet().iterator();
       if (it!=null) {
	        while(it.hasNext()) {
	        	Integer ido = (Integer)it.next();
	        	ArrayList<ObjectProperty> arrayRelationsClone = new ArrayList<ObjectProperty>();
	        	cloneObjectProperty(ido, arrayRelationsClone);
	        	res.relations.put(ido, arrayRelationsClone);
	        }
       }
       res.idoFilterXIdo = new HashMap<String, Integer>();
       res.idoFilterXIdo.putAll(idoFilterXIdo);
	   
	   return res;
	}
	
/*	public boolean hasChanged(){
	    if(super.hasChanged()) 
	    	return true;
	    else
	    	return false;
    }*/
	
    public instance extractChanges(){
       return (instance)this.clone();
    }
    
    public void setRdn(String rdn, boolean insert) {
    	if (insert) {
    		ArrayList<Property> list=properties.get(ido);
    		if(list!=null){
    			Property prop=getProperty(ido,Constants.IdPROP_RDN);
    			if(prop!=null)
    				list.remove(prop);
    		}
    		
	    	DataProperty dataProp = new DataProperty();
	    	dataProp.setIdo(ido);
	    	dataProp.setIdto(idTo);
	    	dataProp.setName(Constants.PROP_RDN);
			dataProp.setIdProp(Constants.IdPROP_RDN);

			StringValue dataVal = new StringValue();
			dataVal.setValue(rdn);
//			dataVal.setOrder(dynagent.application.action.NEW);
			dataProp.setDataType(Constants.IDTO_STRING);
	
			LinkedList<Value> listDataVal = new LinkedList<Value>();
			listDataVal.add(dataVal);
			dataProp.setValues(listDataVal);
	
	    	//si no existe en properties añadir
	    	//si existe actualizar
			addProperty(ido, dataProp);
    	}
		this.rdn= rdn;
    }

    public String getRdn() {
        return rdn;
    }

   /*private static void buildAtChanged( Element node, Integer order ){
        String change=buildAtChanged(order);
        if( change!=null )
         node.setAttribute("CHANGED",change);
        return;
    }
    
    public static String buildAtChanged( Integer order ){
    	String changed = null;
    	if (order!=null) {
	        if( order==dynagent.application.action.NEW )
	            changed = "ADDED";
	        else if( order==dynagent.application.action.DEL )
	            changed = "DEL";
	        else if( order==dynagent.application.action.SET )
	            changed = "SET";
	        else if( order==dynagent.application.action.GET )
	            changed = "GET";
    	}
        return changed;
    }*/
    
    public Element toElement(){
        Element root = new Element("INSTANCE");
        root.setAttribute("ID_O",String.valueOf(ido));
        root.setAttribute("ID_TO",String.valueOf(idTo));
        if (rdn!=null)
        	root.setAttribute("RDN",rdn);
        //buildAtChanged( root, order);
        
        Iterator it = valuesSQ.keySet().iterator();
        if (it!=null) {
	        while(it.hasNext()) {
	            Element SQvalue = new Element("SQ_VALUE");
	            String sq = (String)it.next();
	            SQvalue.setAttribute("SELECT_QUERY",sq);
	            Value v = valuesSQ.get(sq);
	            Element vElem = v.toElement();
	            SQvalue.addContent(vElem);
	            root.addContent(SQvalue);
	        }
        }
        it = idoFilterXIdo.keySet().iterator();
        if (it!=null) {
	        while(it.hasNext()) {
	            Element idoFXIdo = new Element("IDO_FILTER_X_IDO");
	            String idoF = (String)it.next();
	            idoFXIdo.setAttribute("IDO_FILTER",idoF);
	            Integer ido = idoFilterXIdo.get(idoF);
	            idoFXIdo.setAttribute("IDO",String.valueOf(ido));
	            root.addContent(idoFXIdo);
	        }
        }
       
        it = properties.keySet().iterator();
        if (it!=null) {
	        while(it.hasNext()) {
	        	Integer ido = (Integer)it.next();
	        	ArrayList<Property> aProp = this.properties.get(ido);
//	        	if (aProp!=null) {
		            Element properties = new Element("PROPERTIES");
		            //ID_O, ID_TO
		            properties.setAttribute("ID_O",String.valueOf(ido));
		            Integer idto = aProp.get(0).getIdto();
		        	if (idto!=null)
		        		properties.setAttribute("ID_TO",String.valueOf(idto));
		            root.addContent(properties);
		        	//toElementProp(aProp.iterator(), properties);
		        	Iterator itr = aProp.iterator();
		        	while(itr.hasNext()){
		            	Property p = (Property)itr.next();
		                //Element property = new Element("PROPERTY");
		                Element property = p.toElement();
		                properties.addContent(property);
		        	}
//	        	}
	        }
        }
		//instance ins = dynagent.common.communication.messageFactory.buildInstance(null, root);
		//System.out.println("instance to Element");
		//System.out.println(ins.toString());

    	return root;
    }
    
  /*private void toElementProp(Iterator itr, Element ins) {
        while(itr.hasNext()){
        	Property p = (Property)itr.next();
            Element property = new Element("PROPERTY");
            ins.addContent(property);
            
            //datos comunes NAME, PROP, CARD_MIN, CARD_MAX, ACCESS
        	if (p.getName()!=null)
            	property.setAttribute("NAME",p.getName());
            if (p.getIdProp()!=null)
            	property.setAttribute("PROP",String.valueOf(p.getIdProp()));
            if (p.getCardMin()!=null)
            	property.setAttribute("CARD_MIN",String.valueOf(p.getCardMin()));
            if (p.getCardMax()!=null)
            	property.setAttribute("CARD_MAX",String.valueOf(p.getCardMax()));
            if (p.getTypeAccess()!=null)
            	property.setAttribute("ACCESS",String.valueOf(p.getTypeAccess().getOperation()));
            if (p.isValuesFixed())
            	property.setAttribute("VALUES_FIXED","TRUE");
            
            if (p instanceof DataProperty) {
            	//se añade DPROP, LENGTH, DATA_TYPE, VALUE_LIST, ENUM_LIST, EXCLU_LIST
            	property.setAttribute("DPROP","TRUE");
				DataProperty dp = (DataProperty) p;
        		if (dp.getLength()!=null)
        			property.setAttribute("LENGTH",String.valueOf(dp.getLength()));
//        		if (dp.getDataType()!=null)
        			property.setAttribute("DATA_TYPE",String.valueOf(dp.getDataType()));
                if (dp.getValues().size()>0) {
                	Element valueList = new Element("VALUES");
                	property.addContent(valueList);
                	insertAtrDataValue(dp.getValues(), valueList);
                }
                if (dp.getEnumList().size()>0) {
                	Element enumList = new Element("ENUM_LIST");
                	property.addContent(enumList);
                	insertAtrDataValue2(dp.getEnumList(), enumList);
                }
                if (dp.getExcluList().size()>0) {
                	Element excluList = new Element("EXCLU_LIST");
                	property.addContent(excluList);
                	insertAtrDataValue2(dp.getExcluList(), excluList);
                }
			} else if (p instanceof ObjectProperty) {
            	//se añade OPROP, QUANTITY_DETAIL_LIST, RANGO_LIST, VALUE_LIST, ENUM_LIST, EXCLU_LIST
				property.setAttribute("OPROP","TRUE");
				ObjectProperty op = (ObjectProperty) p;
                if (op.getQuantityDetailList().size()>0) {
	            	Element quantityDetailList = new Element("QUANTITY_DETAIL_LIST");
                	property.addContent(quantityDetailList);
	            	LinkedList<QuantityDetail> llqd = op.getQuantityDetailList();
	            	for (int i=0;i<llqd.size();i++) {
	            		QuantityDetail qd = llqd.get(i);
	            		Element quantityDetail = new Element("QUANTITY_LIST");
	            		quantityDetailList.addContent(quantityDetail);
	            		if (qd.getValue()!=null)
	            			quantityDetail.setAttribute("VALUE",String.valueOf(qd.getValue()));
	            		if (qd.getValueCls()!=null)
	            			quantityDetail.setAttribute("VALUE_CLS",String.valueOf(qd.getValueCls()));
	            		if (qd.getCardinalityEspecifyMin()!=null)
	            			quantityDetail.setAttribute("CARD_ESP_MIN",String.valueOf(qd.getCardinalityEspecifyMin()));
	            		if (qd.getCardinalityEspecifyMax()!=null)
	            			quantityDetail.setAttribute("CARD_ESP_MAX",String.valueOf(qd.getCardinalityEspecifyMax()));
	            	}
	            }
                if (op.getRangoList().size()>0) {
                	LinkedList<Integer> lli = op.getRangoList();
                	String rangoList = "";
                	for (int i=0;i<lli.size();i++) {
                		if (rangoList.length()>0)
                			rangoList += ",";
                		rangoList += lli.get(i);
                	}
                	property.setAttribute("RANGO_LIST",rangoList);
                }
                if (op.getValues().size()>0) {
                	Element valueList = new Element("VALUES");
                	property.addContent(valueList);
                	insertAtrObjectValue(op.getValues(), valueList);
                }
                if (op.getEnumList().size()>0) {
                	Element enumList = new Element("ENUM_LIST");
                	property.addContent(enumList);
                	insertAtrObjectValue2(op.getEnumList(), enumList);
                }
                if (op.getExcluList().size()>0) {
                	Element excluList = new Element("EXCLU_LIST");
                	property.addContent(excluList);
                	insertAtrObjectValue2(op.getExcluList(), excluList);
                }
			}
        }
	}

	private void insertAtrDataValue2(LinkedList<DataValue> lldv, Element list) {
    	for (int i=0;i<lldv.size();i++) {
    		DataValue dv = lldv.get(i);
    		
    		Element dataValue = new Element("DATA_VALUE");
    		list.addContent(dataValue);
    		//buildAtChanged(dataValue,dv.getOrder());
	        if (dv instanceof UnitValue) {
    			dataValue.setAttribute("UNIT_VALUE","TRUE");
				UnitValue uv = (UnitValue)dv;
        		if (uv.getUnit()!=null)
        			dataValue.setAttribute("UNIT",String.valueOf(uv.getUnit()));
        		if (uv.getValueMin()!=null)
        			dataValue.setAttribute("VALUE_MIN",String.valueOf(uv.getValueMin()));
        		if (uv.getValueMax()!=null)
        			dataValue.setAttribute("VALUE_MAX",String.valueOf(uv.getValueMax()));
			} else if (dv instanceof DoubleValue) {
    			dataValue.setAttribute("DOUBLE_VALUE","TRUE");
    			DoubleValue fv = (DoubleValue)dv;
        		if (fv.getValueMin()!=null)
        			dataValue.setAttribute("VALUE_MIN",String.valueOf(fv.getValueMin()));
        		if (fv.getValueMax()!=null)
        			dataValue.setAttribute("VALUE_MAX",String.valueOf(fv.getValueMax()));
			} else if (dv instanceof IntValue) {
    			dataValue.setAttribute("INT_VALUE","TRUE");
				IntValue iv = (IntValue)dv;
        		if (iv.getValueMin()!=null)
        			dataValue.setAttribute("VALUE_MIN",String.valueOf(iv.getValueMin()));
        		if (iv.getValueMax()!=null)
        			dataValue.setAttribute("VALUE_MAX",String.valueOf(iv.getValueMax()));
			} else if (dv instanceof BooleanValue) {
    			dataValue.setAttribute("BOOLEAN_VALUE","TRUE");
				BooleanValue bv = (BooleanValue)dv;
        		if (bv.getBvalue()!=null)
        			dataValue.setAttribute("VALUE",String.valueOf(bv.getBvalue()));
        		if (bv.getComment()!=null)
        			dataValue.setText(bv.getComment());
			} else if (dv instanceof StringValue){
    			dataValue.setAttribute("STRING_VALUE","TRUE");
	        	StringValue sv = (StringValue)dv;
        		if (sv.getValue()!=null)
        			dataValue.setText(sv.getValue());
			} else if (dv instanceof TimeValue){
    			dataValue.setAttribute("TIME_VALUE","TRUE");
    			TimeValue tv = (TimeValue)dv;
        		//if (tv.getValue()!=null)
        			//dataValue.setText(tv.getReferenceInstant());
        		if (tv.getRelativeSecondsMin()!=null)
        			dataValue.setAttribute("RELATIVE_SECONDS_MIN",String.valueOf(tv.getRelativeSecondsMin()));
        		if (tv.getRelativeSecondsMax()!=null)
        			dataValue.setAttribute("RELATIVE_SECONDS_MAX",String.valueOf(tv.getRelativeSecondsMax()));
			}
    	}
	}
	private void insertAtrDataValue(LinkedList<Value> lldv, Element list) {
    	for (int i=0;i<lldv.size();i++) {
    		DataValue dv = (DataValue)lldv.get(i);
    		
    		Element dataValue = new Element("DATA_VALUE");
    		list.addContent(dataValue);
    		//buildAtChanged(dataValue,dv.getOrder());
	        if (dv instanceof UnitValue) {
    			dataValue.setAttribute("UNIT_VALUE","TRUE");
				UnitValue uv = (UnitValue)dv;
        		if (uv.getUnit()!=null)
        			dataValue.setAttribute("UNIT",String.valueOf(uv.getUnit()));
        		if (uv.getValueMin()!=null)
        			dataValue.setAttribute("VALUE_MIN",String.valueOf(uv.getValueMin()));
        		if (uv.getValueMax()!=null)
        			dataValue.setAttribute("VALUE_MAX",String.valueOf(uv.getValueMax()));
			} else if (dv instanceof DoubleValue) {
    			dataValue.setAttribute("DOUBLE_VALUE","TRUE");
    			DoubleValue fv = (DoubleValue)dv;
        		if (fv.getValueMin()!=null)
        			dataValue.setAttribute("VALUE_MIN",String.valueOf(fv.getValueMin()));
        		if (fv.getValueMax()!=null)
        			dataValue.setAttribute("VALUE_MAX",String.valueOf(fv.getValueMax()));
			} else if (dv instanceof IntValue) {
    			dataValue.setAttribute("INT_VALUE","TRUE");
				IntValue iv = (IntValue)dv;
        		if (iv.getValueMin()!=null)
        			dataValue.setAttribute("VALUE_MIN",String.valueOf(iv.getValueMin()));
        		if (iv.getValueMax()!=null)
        			dataValue.setAttribute("VALUE_MAX",String.valueOf(iv.getValueMax()));
			} else if (dv instanceof BooleanValue) {
    			dataValue.setAttribute("BOOLEAN_VALUE","TRUE");
				BooleanValue bv = (BooleanValue)dv;
        		if (bv.getBvalue()!=null)
        			dataValue.setAttribute("VALUE",String.valueOf(bv.getBvalue()));
        		if (bv.getComment()!=null)
        			dataValue.setText(bv.getComment());
			} else if (dv instanceof StringValue){
    			dataValue.setAttribute("STRING_VALUE","TRUE");
	        	StringValue sv = (StringValue)dv;
        		if (sv.getValue()!=null)
        			dataValue.setText(sv.getValue());
			} else if (dv instanceof TimeValue){
    			dataValue.setAttribute("TIME_VALUE","TRUE");
    			TimeValue tv = (TimeValue)dv;
        		//if (tv.getValue()!=null)
        			//dataValue.setText(tv.getReferenceInstant());
        		if (tv.getRelativeSecondsMin()!=null)
        			dataValue.setAttribute("RELATIVE_SECONDS_MIN",String.valueOf(tv.getRelativeSecondsMin()));
        		if (tv.getRelativeSecondsMax()!=null)
        			dataValue.setAttribute("RELATIVE_SECONDS_MAX",String.valueOf(tv.getRelativeSecondsMax()));
			}
    	}
	}

	private void insertAtrObjectValue(LinkedList<Value> llov, Element list) {
    	for (int i=0;i<llov.size();i++) {
    		ObjectValue ov = (ObjectValue)llov.get(i);
    		list.addContent(objectValue);
           	//buildAtChanged(objectValue,ov.getOrder());
    		if (ov.getValue()!=null)
    			objectValue.setAttribute("VALUE",String.valueOf(ov.getValue()));
   			objectValue.setAttribute("VALUE_CLS",String.valueOf(ov.getValueCls()));
    		if (ov.getQ()!=null)
    			objectValue.setAttribute("Q",String.valueOf(ov.getQ()));
    	}
	}
	private void insertAtrObjectValue2(LinkedList<ObjectValue> llov, Element list) {
    	for (int i=0;i<llov.size();i++) {
    		ObjectValue ov = llov.get(i);
    		list.addContent(objectValue);
           	//buildAtChanged(objectValue,ov.getOrder());
    		if (ov.getValue()!=null)
    			objectValue.setAttribute("VALUE",String.valueOf(ov.getValue()));
   			objectValue.setAttribute("VALUE_CLS",String.valueOf(ov.getValueCls()));
    		if (ov.getQ()!=null)
    			objectValue.setAttribute("Q",String.valueOf(ov.getQ()));
    	}
	}*/

    
    
	/*
	public instance getTreeObject(Integer idto, Integer userRol, String user, Integer userTask) {
		Iterator<Property> itp = this.getAllPropertyIterator(idto);
		while(itp.hasNext()){
			Property prop = (Property)itp.next();
			Iterator<Property> p = this.getPropertyAccessIterator(idto, prop.getIdProp(), userRol, user, userTask);
			while(p.hasNext())
				this.addProperty(ido, p.next());
		}
		return this;
	}
*/	
	public instance getTreeObject(int ido) {
		dynagent.common.knowledge.instance i = null;
		addPropertyTreeObject(i,ido);
		return i;
	}
	
	private void addPropertyTreeObject(instance i, Integer ido){
		Iterator<Property> itp = this.getAllPropertyIterator(ido);
		while(itp.hasNext()){
			Property prop = (Property)itp.next();
			if (i==null)
				i = new dynagent.common.knowledge.instance(prop.getIdto(),ido, filterMode);
			if(prop instanceof ObjectProperty){
				LinkedList<Value> valueList=prop.getValues();
				for (int j=0;j<valueList.size();j++) {
					ObjectValue ov = (ObjectValue)valueList.get(j);
					addPropertyTreeObject(i,ov.getValue());
				}
				LinkedList<Integer> rangoList=((ObjectProperty)prop).getRangoList();
				for (int j=0;j<rangoList.size();j++) {
					Integer value = rangoList.get(j);
					addPropertyTreeObject(i,value);
				}
				LinkedList<ObjectValue> enumList=((ObjectProperty)prop).getEnumList();
				for (int j=0;j<enumList.size();j++) {
					ObjectValue ov = enumList.get(j);
					addPropertyTreeObject(i,ov.getValue());
				}
			}
			Property p = this.getProperty(ido, prop.getIdProp());
			i.addProperty(ido, p);
		}
	}

	/**Obtiene una lista con todas los objetos Property que hay en un instance.
	 * @author zamora
	 * @return LinkedList<Property>: Lista con las propiedades
	 */
	public LinkedList<Property> getAllProperties() {
		LinkedList<Property> properties = new LinkedList<Property>();
	    for(Iterator it=this.properties.keySet().iterator();it.hasNext();){
		   Integer key=(Integer)it.next();
		   properties.addAll(this.properties.get(key));
	    }
	    return properties;
	}

	public Iterator<Property> getAllPropertyIterator(int ido) {
		if(properties.get(new Integer(ido)) != null)
			return properties.get(new Integer(ido)).iterator();
		return new LinkedList<Property>().iterator();
	}
	
	
	
	 public void addValue(int ido,int idProp,Value value) {
		 
		 this.setValue(ido, idProp, null, value);
	 }
	

	/**
	 * Modifica, añade o elima según la operation deducida un Value a una Property.
	 * La operation que se deducirá será:  <br>
	 * &nbsp;  NEW (si oldValue=null) <br>
	 * &nbsp;  DEL (si newValue=null)  <br>
	 * &nbsp; SET (si oldValue!=null y  newValue!=null) <br>
	 * 
	 * @param: int ido  -identificador del objeto 
	 * @param: int idProp -identificador de la propiedad
	 * @param: Value: viejo valor
	 * @param Value: nuevo valor
	 * @throws CardinalityExceedException, OperationNotPermitedException,IncompatibleValueException
	 */
	  public void setValue(int ido,int idProp,Value oldValue,Value newValue) {
		  boolean encontrado = false;
		  //System.err.println("ido:"+ido+" idProp:"+idProp+" value:"+newValue+" oldValue:"+oldValue);
		  if(properties.get(ido) != null){
				Iterator it = properties.get(ido).iterator();
				while(it.hasNext()){
					Property p = (Property)it.next();
					if(p.getIdProp().equals(idProp)){
						encontrado = true;
						LinkedList<Value> valueList=p.getValues();
						if(p instanceof DataProperty){
							int pos=indexOfValue(valueList,(DataValue)oldValue);
							if(pos!=-1){
								if(newValue==null)
									valueList.remove(pos);
								else valueList.set(pos, (DataValue)newValue);
							}else{
								if(newValue==null){
									Exception ex=new Exception("instance.setValue con newValue nulo. Siendo ido:"+ido+" idProp:"+idProp+" oldValue:"+oldValue+" p.getValues():"+p.getValues());
									ex.printStackTrace();
								}
								//TODO Si tenemos cardinalidad 1 nos da igual el oldValue, pero el oldValue deberia ser el correcto!!
								if(filterMode){
									valueList.add((DataValue)newValue);
								}else{
									if(p.getCardMax()!=null && p.getCardMax()==1 && !valueList.isEmpty()){
										valueList.set(0, (DataValue)newValue);
										//System.err.println("WARNING: Asignacion de un valor "+newValue+" sin encontrar el viejo valor "+oldValue+" en pr:"+p);
									}else if(newValue!=null)
										valueList.add((DataValue)newValue);
								}
							}
						}else{
							//System.out.println("SYSOUAMI : ValueList ="+valueList);
							//System.out.println("SYSOUAMI : oldValue ="+(ObjectValue)oldValue);
							int pos=indexOfValue(valueList,(ObjectValue)oldValue);
							if(pos!=-1){
								if(newValue==null)
									valueList.remove(pos);
								else valueList.set(pos, (ObjectValue)newValue);
							}else{
								if(newValue==null){
									Exception ex=new Exception("instance.setValue con newValue nulo. Siendo ido:"+ido+" idProp:"+idProp+" oldValue:"+oldValue+" p.getValues():"+p.getValues());
									ex.printStackTrace();
								}
								// TODO Si tenemos cardinalidad 1 nos da igual el oldValue, pero el oldValue deberia ser el correcto!!
								if(filterMode){
									valueList.add((ObjectValue)newValue);
								}else{
									if(p.getCardMax()!=null && p.getCardMax()==1 && !valueList.isEmpty()){
										valueList.set(0, (ObjectValue)newValue);
										//System.err.println("WARNING: Asignacion de un valor "+newValue+" sin encontrar el viejo valor "+oldValue+" en pr:"+p);
									}else if(newValue!=null)
										valueList.add((ObjectValue)newValue);
								}
							}
						}
						break;
					}
				}
			}
		  if (!encontrado && idProp==Constants.IdPROP_BUSINESSCLASS)
			  setBusinessClass(ido);
	  }
	  
	  private void setBusinessClass(int ido) {
		  DataProperty dp = new DataProperty();
		  dp.setIdo(ido);
		  dp.setIdto(this.getClassOf(ido));
		  dp.setName(Constants.PROP_BUSINESSCLASS);
		  dp.setIdProp(Constants.IdPROP_BUSINESSCLASS);
		  BooleanValue booleanVal = new BooleanValue();
		  booleanVal.setBvalue(true);
//		  dataVal.setOrder(dynagent.application.action.NEW);
		  dp.setDataType(Constants.IDTO_BOOLEAN);

		  LinkedList<Value> listDataVal = new LinkedList<Value>();
		  listDataVal.add(booleanVal);
		  dp.setValues(listDataVal);

		  addProperty(ido,dp);
	}
//	  private int indexOfValue(LinkedList<Value> valueList,Value value){
//		  int size=valueList.size();
//		  int pos=-1;
//		  int i=0;
//		  if(value instanceof ObjectValue){
//			  while(pos==-1 && i<size){
//				  ObjectValue objectV=(ObjectValue)valueList.get(i);
//				  ObjectValue objectValue=(ObjectValue)value;
//				  if(objectValue!=null && objectV.getValue()==objectValue.getValue() && objectV.getValueCls()==objectValue.getValueCls())
//					  pos=i;
//				  i++;
//			  }
//		  }else{
//			  DataValue dataValue=(DataValue)value;
//			  while(pos==-1 && i<size){
//				  DataValue dataV=(DataValue)valueList.get(i);
//				  String valueString=parserValue(dataValue);
//				  String valueOld=parserValue(dataV);
//				  if(valueOld!=null && valueString!=null && valueOld.equals(valueString))
//					  pos=i;
//				  i++;
//			  }
//		  }
//		  return pos;
//	  }
	  
//	  private int indexOfValue(LinkedList<ObjectValue> valueList,ObjectValue objectValue){
//		  int size=valueList.size();
//		  int pos=-1;
//		  int i=0;
//		  while(pos==-1 && i<size){
//			  ObjectValue objectV=valueList.get(i);
//			  /*if((objectValue!=null && objectV!=null && objectV.getValue().equals(objectValue.getValue()) && objectV.getValueCls()==objectValue.getValueCls()) ||
//					 ( objectValue==null && objectV==null && objectV.getValueCls()==objectValue.getValueCls()) )*/
//			  if(objectV.equals(objectValue))
//				  pos=i;
//			  i++;
//		  }
//		  return pos;
//	  }
//	  
//	  private int indexOfValue(LinkedList<DataValue> valueList,DataValue dataValue){
//		  int size=valueList.size();
//		  int pos=-1;
//		  /*String value=parserValue(dataValue);*/
//		  int i=0;
//		  while(pos==-1 && i<size){
//			  DataValue dataV=valueList.get(i);
//			  /*String valueOld=parserValue(dataV);
//			  if(valueOld!=null && value!=null && valueOld.equals(value))*/
//			  if(dataV.equals(dataValue))
//				  pos=i;
//			  i++;
//		  }
//		  return pos;
//	  }
	  
	  private int indexOfValue(LinkedList valueList,Value value){
		  int size=valueList.size();
		  int pos=-1;
		  int i=0;
		  while(pos==-1 && i<size){
			  Value val=(Value)valueList.get(i);
			  if(val.equals(value))
				  pos=i;
			  i++;
		  }
		  return pos;
	  }
	  
//	  private String parserValue(DataValue dataValue){
//		  String value=null;
//			if(dataValue instanceof StringValue)
//				value= ((StringValue)dataValue).getValue();
//			else if(dataValue instanceof DoubleValue)
//				value= ((DoubleValue)dataValue).getValueMin().toString();
//			else if(dataValue instanceof IntValue)
//				value= ((IntValue)dataValue).getValueMin().toString();
//			else if(dataValue instanceof BooleanValue){
//				Boolean valueB= ((BooleanValue)dataValue).getBvalue();
//				/*value= valueB==null?"":valueB.toString();*/
//				String comment= ((BooleanValue)dataValue).getComment();
//				value= valueB+":"+comment;
//			}else if(dataValue instanceof TimeValue){
//				Long relativeSeconds=((TimeValue)dataValue).getRelativeSeconds();
//				long milliseconds=relativeSeconds*Constants.TIMEMILLIS;
//				value=String.valueOf(milliseconds);
//			}else if(dataValue instanceof UnitValue)
//				value= ((UnitValue)dataValue).getValueMin().toString();
//			
//			return value;
//		}

	public Integer getClassOf(int ido) {
		Integer classP = null;
		Iterator<Property> it = getAllPropertyIterator(ido);
		if (it.hasNext()) {
			Property p = (Property)it.next();
			classP = p.getIdto();
		}
		//TODO puede que sea posible mirar valueList para controlar individuos apuntados de 
		//los que no hay ninguna property de ellos
		return classP;
	}

	public Property getPropertyQuery(SelectQuery sq) {
		Integer ido = idoFilterXIdo.get(String.valueOf(sq.getIdObject()));
		if(ido!=null)
			return getProperty(ido,sq.getIdProp());
		else
			return null;
	}
	public String idoFilterXIdoToString() {
		String res = "";
		Iterator it = idoFilterXIdo.keySet().iterator();
		while (it.hasNext()) {
			String idoFilter = (String)it.next();
			Integer ido = idoFilterXIdo.get(idoFilter);
			res += "idoFilter " + idoFilter + ", ido " + ido + "\n";
		}
		return res;
	}
	public Property getPropertyQuery(String idObject, int idProp) {
		Integer ido = idoFilterXIdo.get(idObject);
		if(ido!=null)
			return getProperty(ido,idProp);
		else
			return null;
	}
	public Property getPropertyAgregadoQuery(String idObject) {
		return getPropertyQuery(idObject, -1);
	}
	
	public void addIdoFilterXIdo(String idoFMap, int idoMap) {
		if (!idoFilterXIdo.containsKey(idoFMap))
			idoFilterXIdo.put(idoFMap, idoMap);
	}
	
	public Property getProperty(int ido, int idProp) {
		if(properties.get(new Integer(ido)) != null){
			Iterator it = properties.get(new Integer(ido)).iterator();
			Property p = null;
			while(it.hasNext()){
				Property pr = (Property)it.next();
				if(pr.getIdProp().equals(idProp)) {
					p = pr;
					break;
				}
			}
			return p;
		}else
			return null;
	}

	
	public void setFilterMode(boolean filterMode) {
		this.filterMode=filterMode;
		
	}
	public boolean isFilterMode(){
		return this.filterMode;
	}
	
	public void setValue(int ido, int idProp, LinkedList<Value> oldValues, LinkedList<Value> newValues){
		// TODO Auto-generated method stub
		System.err.println("----ERROR: SetValue Multiple no implementado en instance");
	}
	/*public void setValueList(Property pr, LinkedList valueList) {
		// TODO Auto-generated method stub
		if(properties.get(pr.getIdo()) != null){
			Iterator it = properties.get(pr.getIdo()).iterator();
			while(it.hasNext()){
				Property p = (Property)it.next();
				if(p.getIdProp() == pr.getIdProp() && p.getClsRel() == pr.getClsRel() && p.getIdoRel() == pr.getIdoRel()){
					if(p instanceof DataProperty)
						((DataProperty)p).setValueList(valueList);
					else ((ObjectProperty)p).setValueList(valueList);
					break;
				}
			}
		}

	}*/
	
	public int compareTo(Object o) {
		return (equals(o)?0:(((instance)o).getIDO()<getIDO())?1:-1);
	}

	public HashMap<String,ArrayList<String>> getValoresFijados() {
		HashMap<String,ArrayList<String>> result=new HashMap<String,ArrayList<String>>();
		for(Iterator it=this.properties.keySet().iterator();it.hasNext();){
		   Integer key=(Integer)it.next();
		   ArrayList<Property> propertiesOfkey=this.properties.get(key);
		   for(int i=0;i<propertiesOfkey.size();i++){
			   if(propertiesOfkey.get(i).getValues().size()>0){
				   String idtoProp=propertiesOfkey.get(i).getIdto()+"#"+propertiesOfkey.get(i).getIdProp();
				   //TODO
			   }   
		   }
	    }
	    return result;
	}
	
	
	
}
