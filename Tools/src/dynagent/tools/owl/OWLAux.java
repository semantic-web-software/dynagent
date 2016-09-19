/**
 * OWLParser.java
 * @author Jose A. Zamora Aguilera - 
 * @description Parse a OWL Protege file and build  the dynagent tables structure and the facts that represents the model
 */	

package dynagent.tools.owl;


import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import com.hp.hpl.jena.util.FileUtils;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.HelpProperty;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.ConceptLogger;
import dynagent.ruleengine.RuleEngineLogger;
import dynagent.server.database.dao.AccessDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.tools.importers.owl.RangeItem;
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


public class OWLAux{
	//tabla que guarda la equivalencia entre el nombre de las clases y su identificador entero

	private ArrayList<String> clasesNegocio=new  ArrayList<String>();
	private ArrayList<String> acciones=new  ArrayList<String>();
	
	
	public static boolean isSpecializedFrom(RDFResource cls, String posibleParent )
	{
		try{
			if(cls==null){
				return false;
			}
			OWLClass clsN=(OWLClass)cls;
			for(Iterator it=clsN.getNamedSuperclasses(true).iterator();it.hasNext();)
			{
				if(((OWLNamedClass)it.next()).getLocalName().equals(posibleParent)  ){
					return true;
				}
			}
		}catch(Exception ex){
			ConceptLogger.getLogger().writeln("     warning: error en OWLParser.isSpecializedFrom:"+ex.getCause()+"\n"+ex.toString());
			return false;
		}
		return false;
	}
	
	
	public static boolean hasRestricctionHasValue(OWLNamedClass cls, RDFProperty rdfProp){
		for(Iterator iR =cls.getRestrictions(rdfProp, true).iterator(); iR.hasNext();)
		{
    		OWLRestriction clRes=(OWLRestriction)iR.next();
    		if(clRes.getProtegeType().getLocalName().equals("HasValueRestriction")){
    			return true;
    		}
		}
		return false;
		
	}
	
	
	public static LinkedList <RangeItem> traslateRestrictions(OWLNamedClass cls, RDFProperty rdfProp ){
		LinkedList <RangeItem> lRangeItems=new LinkedList <RangeItem>();
		int nrest=cls.getRestrictions(rdfProp, true).size();
		String tipoPropiedad=rdfProp.getRDFType().getLocalName();
    	String CardMin=null;
		String CardMax=null;
	
		//System.out.println("[OWLParser]:_           [ restricciones: ("+nrest+") :");
		Collection cRestrictions=cls.getRestrictions(rdfProp, true);
		
		for(Iterator iR =cRestrictions.iterator(); iR.hasNext();)
		{
    		OWLRestriction clRes=(OWLRestriction)iR.next();
    		//System.out.println("[OWLParser]:_          #restriccion: "+clRes.getLocalName()+"#   ]");
    		
    		if(clRes.getProtegeType().getLocalName().equals("SomeValuesFromRestriction"))
    		{
    			//Si el rango es complejo aviso  de que en el modelo se ha usado mal una SomeValuesRestriction
    			if(clRes.getFillerText().contains(" ")){
    				System.out.println("[OWLParser]:_      WARNING: Hay una restricción someValuesFrom apuntando a un rango complejo:  se ignorará.");
    				//RuleEngineLogger.getLogger().write("      WARNING: Hay una restricción someValuesFrom apuntando a un rango complejo:  se ignorará.");
    			}
    			//Si el rango es simple (una sola clase) damos la info de rango y cardinalidad en un solo registro mediante una quantitive cardinality restricctions (OP=QCR)  
    			else{
    				RangeItem ri=new RangeItem();
    				ri.setOP(Constants.OP_QUANTITYDETAIL);
    				ri.setQMIN("1");
    				ri.setVALUECLS(clRes.getFillerText());
    				lRangeItems.add(ri);
    			}
    		}
    		else if(clRes.getProtegeType().getLocalName().equals("MinCardinalityRestriction"))
    		{
    			CardMin=clRes.getFillerText();
    			
    		}
    		else if(clRes.getProtegeType().getLocalName().equals("MaxCardinalityRestriction"))
    		{
    			CardMax=clRes.getFillerText();
    			
    		}
    		else if(clRes.getProtegeType().getLocalName().equals("CardinalityRestriction"))
    		{
    			CardMin=clRes.getFillerText();
    			CardMax=clRes.getFillerText();
    		}
    		else if(clRes.getProtegeType().getLocalName().equals("HasValueRestriction")) //(has value implica al menos un valor cardinalidad mínima 1)
    		{
    			RangeItem ri=new RangeItem();
    			//System.out.println("[OWLParser]:_        WARNING: Restriccion hasValue  para "+rdfProp.getLocalName()+"  en la clase  "+cls.getLocalName()+" .Se tratará como una restricción de rango hacia el value más una restric de cardinalidad mínima 1 ");
    			//RuleEngineLogger.getLogger().write("      WARNING: Restriccion hasValue  se tratará como una restricción de rango hacia el value más una restric de cardinalidad mínimo 1");
    			ri.setOP(Constants.OP_ONEOF);
				if(CardMin!=null&&(Integer.parseInt(CardMin)<1))
				{		
					CardMin="1";
				}
    			if(tipoPropiedad.equals("DatatypeProperty")){
    				String tipoDato=rdfProp.getRangeDatatype().getLocalName();
    				//Si es un INT o FLOAT  su valor va informado en los campos Q
    				if(tipoDato.equals("float")||tipoDato.equals("int")){
    					ri.setVALUECLS(rdfProp.getRangeDatatype().getLocalName());
    					ri.setQMIN(clRes.getFillerText());
    					ri.setQMAX(clRes.getFillerText());
    					lRangeItems.add(ri);
    				}
    			
    				else{
    					ri.setVALUE(clRes.getFillerText());
    					ri.setVALUECLS(rdfProp.getRangeDatatype().getLocalName());
    					lRangeItems.add(ri);
    				}
    			}	
    			else if (tipoPropiedad.equals("ObjectProperty")){   
        			OWLHasValue hasValueRs=(OWLHasValue)clRes;
        			RDFResource value=(RDFResource)hasValueRs.getHasValue();
        			if(value.getRDFType().getLocalName().equals("Class")){ //Apunta a una clase del modelo (full)
        				ri.setVALUE(clRes.getFillerText());
        				ri.setVALUECLS(clRes.getFillerText());
        				
    					lRangeItems.add(ri);
        			}
        			else //apunta a un individuo
        			{
        				String adaptedValue=clRes.getFillerText();
						if(adaptedValue.startsWith("_")){//Jena les pone _ delante a los que empiezan por numero en el modelo. Le quitamos el _
							adaptedValue=adaptedValue.substring(1, adaptedValue.length());
						}
        				ri.setVALUE("#"+adaptedValue);
        				ri.setVALUECLS(value.getRDFType().getLocalName());
    					lRangeItems.add(ri);
        			}
    			}
    		}
		}
		
		if(CardMin!=null||CardMax!=null){ //Hay que informar de la cardinalidad global
		
			RangeItem ri=new RangeItem();
			ri.setQMIN(CardMin);
			ri.setQMAX(CardMax);
			ri.setOP(Constants.OP_CARDINALITY);
		
			lRangeItems.add(ri);
	}
	return lRangeItems;
	}
	
	public static LinkedList <RangeItem> traslateDataRange(RDFResource rdfRng,RDFProperty rdfProp){
		LinkedList <RangeItem> lRangeItems = new LinkedList <RangeItem>();
		String tipoDato=rdfProp.getRangeDatatype().getLocalName();
		
///////////INFORMACIÓN DYNAGENT EN LOS COMENTARIOS SOBRE VALORES MÍNIMOS Y MÁXIMOS
    	String vmin=null;
    	String vmax=null;
		Iterator<String> itcoment2=rdfProp.getComments().iterator();
		if(itcoment2.hasNext()){
			Object commentObject=itcoment2.next();
			if(!(commentObject instanceof DefaultRDFSLiteral)){
				String comment=(String)commentObject;
				//System.out.println("  coment="+comment);
				String  [] comentarios=comment.split("#");
				for(int i=0;i<comentarios.length;i++){
					String comentario=comentarios[i];
					int indexvalue=comentario.indexOf("=");
					String svalue=comentario.substring(indexvalue+1);
					if(comentario.startsWith("vmin")){
						vmin=svalue;
					}
					else if(comentario.startsWith("vmax")){
						vmax=svalue;
					}
				}
			}
		}
    	if(vmin!=null||vmax!=null){//se ha asignado al menos un extremo de valor
    		RangeItem ri=new RangeItem();
    		ri.setOP(Constants.OP_ONEOF);
    		ri.setVALUECLS(tipoDato);
    		ri.setQMAX(vmax);
    		ri.setQMIN(vmin);
    		lRangeItems.add(ri);
    	}
		
		
		
		//System.out.println("OWLPARSER.traslateDataRange  rdfProp="+rdfProp.getLocalName()+"    tipoDato="+tipoDato);
		if(rdfRng.getRDFType()!=null){
		
			if (rdfRng.getRDFType().getLocalName().equals("DataRange"))
			{
				OWLDataRange dataRn=(OWLDataRange)rdfRng;
				List listaValues=dataRn.getOneOfValueLiterals();
				//Si el tipo de dato es INT o FLOAT los valores van en los campos Q´s
				for(int i=0;i<listaValues.size();i++){
					RangeItem ritem=new RangeItem();
					if(tipoDato.equals(Constants.DATA_DOUBLE)||tipoDato.equals(Constants.DATA_INT)){
						ritem.setOP(Constants.OP_UNION);
						ritem.setQMAX(listaValues.get(i).toString());
						ritem.setQMIN(listaValues.get(i).toString());
						ritem.setVALUECLS(tipoDato);
					}
					else if(tipoDato.equals(Constants.DATA_FLOAT)){//en owl los numericos son float, para nosotros son doubles x precision en times
						ritem.setOP(Constants.OP_UNION);
						ritem.setQMAX(listaValues.get(i).toString());
						ritem.setQMIN(listaValues.get(i).toString());
						ritem.setVALUECLS(Constants.DATA_DOUBLE);
					}
					else{
						ritem.setOP(Constants.OP_UNION);
						ritem.setVALUECLS(tipoDato);
						ritem.setVALUE(listaValues.get(i).toString());
					}
					lRangeItems.add(ritem);
				}
		 }else {
			RangeItem ritem=new RangeItem();
			if(tipoDato.equals("variant of string"))
				ritem.setVALUECLS(Constants.DATA_STRING);
			else
				ritem.setVALUECLS(tipoDato);
			lRangeItems.add(ritem);
			ritem.setOP(Constants.OP_INTERSECTION);
		}
	}else {
			RangeItem ritem=new RangeItem();
			ritem.setVALUECLS(tipoDato);
			lRangeItems.add(ritem);
			ritem.setOP(Constants.OP_INTERSECTION);
		}
		return lRangeItems;
	}
	
	
	public static LinkedList <RangeItem> traslateObjectRange(RDFResource rdfRng){
		String tipoRango=rdfRng.getRDFProperties().toString();
		LinkedList <RangeItem> lRangeItems = new LinkedList <RangeItem>();
		if(rdfRng!=null && rdfRng.getRDFType().getLocalName().equals("Class"))
    	{
			if(tipoRango.contains("unionOf"))
			{
				OWLUnionClass union=(OWLUnionClass)rdfRng;
				for(Iterator itU=union.getOperands().iterator();itU.hasNext();){
					OWLClass opCls=(OWLClass)itU.next();
					RangeItem ritem=new RangeItem();
					ritem.setVALUECLS(opCls.getLocalName());
					ritem.setOP(Constants.OP_UNION);
					lRangeItems.add(ritem);
					
				}
			}
			else if( tipoRango.contains("intersectionOf"))
			{
				OWLIntersectionClass intersection=(OWLIntersectionClass)rdfRng;
				for(Iterator itI=intersection.getOperands().iterator();itI.hasNext();){
					OWLClass opCls=(OWLClass)itI.next();
					RangeItem ritem=new RangeItem();
				    if(opCls.getRDFProperties().toString().contains("complementOf")){
				    	OWLComplementClass complement=(	OWLComplementClass)opCls;
				    	ritem.setVALUECLS(opCls.getLocalName());
						ritem.setOP(Constants.OP_NEGATION);
				    }
				    else{
				    	ritem.setVALUECLS(opCls.getLocalName());
						ritem.setOP(Constants.OP_INTERSECTION);
					}
				  
				    lRangeItems.add(ritem);
				 }
			}
			else if(tipoRango.contains("complementOf"))
			{
				OWLComplementClass complement=(	OWLComplementClass)rdfRng;
				RangeItem ritem=new RangeItem();
				ritem.setVALUECLS(complement.getComplement().getLocalName());
				ritem.setOP(Constants.OP_NEGATION);
				lRangeItems.add(ritem);
				
			}
			else if(tipoRango.contains("oneOf"))
			{
				//es una objectProperty. Podemos tener una lista de clases (full) o una lista de individuos 
					OWLEnumeratedClass enumRang=(OWLEnumeratedClass)rdfRng;
					for(Iterator itE=enumRang.listOneOf();itE.hasNext();){
						RangeItem ritem=new RangeItem();
						RDFResource value=(RDFResource)itE.next();
						//Enumerados de clases (full)
						if(value.getRDFType().getLocalName().equals("Class"))
						{
							ritem.setVALUE("#"+value.getLocalName());
							ritem.setVALUECLS(value.getLocalName());
							ritem.setOP(Constants.OP_ONEOF);
						}
						//Enumerado de individuos
						else{
							String adaptedValue=value.getLocalName();
							if(adaptedValue.startsWith("_")){//Jena les pone _ delante a los que empiezan por numero en el modelo. Le quitamos el _
								adaptedValue=adaptedValue.substring(1, adaptedValue.length());
							}
							ritem.setVALUE(adaptedValue);
							ritem.setVALUECLS(value.getRDFType().getLocalName());
							ritem.setOP(Constants.OP_ONEOF);
						}
						lRangeItems.add(ritem);
					}
			}
			else {   //Es un rango simple
				RangeItem ritem=new RangeItem();
				ritem.setVALUECLS(rdfRng.getLocalName());
				ritem.setOP(Constants.OP_INTERSECTION);
				lRangeItems.add(ritem);	
			}
		}
	
	
		
			
		return lRangeItems;	
	}
	
	
	
	
	
	
	//prueba transformacion	
	public static boolean isSpecializedFrom(OWLNamedClass cls,String nameSuperiorClass){
	    String name=cls.getLocalName();
        //parámetro a false para que solo devuelva los padres directos 
        //true devuelve los padres,abuelos,...)
	    
	    String nameS=null;
        for (Iterator itSup=cls.getNamedSuperclasses(true).iterator(); itSup.hasNext();)
        {
        	RDFResource sc=(RDFResource)itSup.next();
        	nameS=((OWLClass)sc).getLocalName();
        	if(nameS.equals(nameSuperiorClass)){
        		return true;
        	}
         }
    	return false;
    }
	

	public static boolean isDirectSpecializedFrom(OWLNamedClass cls,String nameSuperiorClass){
	    String name=cls.getLocalName();
        //parámetro a false para que solo devuelva los padres directos 
        //true devuelve los padres,abuelos,...)
	    
	    String nameS=null;
        for (Iterator itSup=cls.getNamedSuperclasses(false).iterator(); itSup.hasNext();)
        {
        	RDFResource sc=(RDFResource)itSup.next();
        	nameS=((OWLClass)sc).getLocalName();
        	if(nameS.equals(nameSuperiorClass)){
        		return true;
        	}
         }
    	return false;
    }
	

	
	
	
//	prueba transformacion
	public static OWLModel pruebaTransformacion(OWLModel owl){
		
	//owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
	OWLNamedClass clsAspecto=null;
	for (Iterator it0 =owl.getUserDefinedOWLNamedClasses().iterator(); it0.hasNext();)
	{
		OWLNamedClass cls = (OWLNamedClass) it0.next();
		String nameClase=cls.getLocalName();
		if(nameClase.equals("DATOS_COMERCIALES")){
			clsAspecto=cls;
		}
	}
	
	for (Iterator it2 =owl.getUserDefinedOWLNamedClasses().iterator(); it2.hasNext();)
	{
		OWLNamedClass cls = (OWLNamedClass) it2.next();
		String nameClase=cls.getLocalName();
		if(isSpecializedFrom(cls,"DATOS_COMERCIALES")){
			cls.removeSuperclass(clsAspecto);
			System.err.println("\n DEBUG CLASE :"+nameClase+" hereda de DATOS_COMERCIALES");
		}
		
	}
	return owl;
	}
	
	
	/*
	 * 
	 * borra las propiedades que no son de clases de sistema y todas las clases que no son de sistema
	 */public static OWLModel extractSystemOntology(OWLModel owl){
		  
		  OWLNamedClass clsClase=null;
		  OWLNamedClass clsDataPropiedad=null;
		  OWLNamedClass clsObjectPropiedad=null;
		  
		  
		  ArrayList<OWLNamedClass> clasesAborrar=new  ArrayList<OWLNamedClass> (); 
		  ArrayList<String> nombreClasesABorrar=new  ArrayList<String> (); 
		  HashSet<String> nombreClasesNegocio=new  HashSet<String> ();
		  HashSet<String> nombreDataPropiedadesNegocio=new  HashSet<String> ();
		  HashSet<String> nombreObjectPropiedadesNegocio=new  HashSet<String> ();
		  
		  
		  
		 
		  for (Iterator it0 =owl.getUserDefinedOWLNamedClasses().iterator(); it0.hasNext();)
		{
				OWLNamedClass cls = (OWLNamedClass) it0.next();
				String nameClase=cls.getLocalName();
				
				
				if((!nameClase.equals("SYSTEM_CLASS")&&!isSpecializedFrom(cls,"SYSTEM_CLASS"))&&!isSpecializedFrom(cls,"ACTION")&&!isSpecializedFrom(cls,"DATA_TRANSFER")&&!isSpecializedFrom(cls,"AUX")&&!isSpecializedFrom(cls,"AUX_PARAMS")&&!isSpecializedFrom(cls,"PARAMS")&&!isSpecializedFrom(cls,"RULE_CONFIGURATION")){
					nombreClasesNegocio.add(nameClase);
					
				}
				if(!Constants.isBasicClass(nameClase)&&((!nameClase.equals("SYSTEM_CLASS")&&!isSpecializedFrom(cls,"SYSTEM_CLASS"))||isSpecializedFrom(cls,"ENUMERATED")||isSpecializedFrom(cls,"DATA_TRANSFER")||isSpecializedFrom(cls,"ACTION")||isSpecializedFrom(cls,"AUX")||isSpecializedFrom(cls,"AUX_PARAMS")||isSpecializedFrom(cls,"PARAMS")||isSpecializedFrom(cls,"RULE_CONFIGURATION"))){
					clasesAborrar.add(cls);
					nombreClasesABorrar.add(nameClase);
				}
				else if(nameClase.contains("Axiom")||nameClase.equals("MI_EMPRESA")){
					clasesAborrar.add(cls);
					nombreClasesABorrar.add(nameClase);
				}
				
				else if(nameClase.equals("CLASE")){
					clsClase=cls;
				}
				
				else if(nameClase.equals("PROPIEDAD_DATO")){
					clsDataPropiedad=cls;
				}
				else if(nameClase.equals("PROPIEDAD_OBJETO")){
					clsObjectPropiedad=cls;
				}
				
				
				

		}
		  
		 System.out.print("\n\n  CLASES A BORRAR: "+nombreClasesABorrar);
		  
		 Collection properties= owl.getUserDefinedOWLProperties();
		   for (Iterator itP = properties.iterator(); itP.hasNext();){
	        	OWLProperty owlprop=(OWLProperty)itP.next();
	        	String nameProp=owlprop.getLocalName();
	        	ArrayList<String> dominios=getDomain(owlprop);
	        	Iterator itdom=dominios.iterator();
	        	while(itdom.hasNext()){
	        		String nameDom=(String)itdom.next();
	        		if(nombreClasesABorrar.contains(nameDom)){
	        			
	        			if(nombreClasesNegocio.contains(nameDom)){
	        				if(owlprop instanceof OWLDatatypeProperty){
	        					nombreDataPropiedadesNegocio.add(nameProp);	
	        					
	        				}else if(owlprop instanceof OWLObjectProperty){
	        					nombreObjectPropiedadesNegocio.add(nameProp);	
	        					
	        				}
	        				
	        				
	        			}
	        			if(Constants.getIdConstantProp(owlprop.getLocalName())==null){//para asegurar que no es una propiedad de sistema
	        				owlprop.delete();
	        			}
	        			//System.out.print(".... se borra propiedad "+nameProp);
	        			
	        			
	        			
	        		}
	        	}
		   }
	
		for(int i=0;i<clasesAborrar.size();i++){
			OWLNamedClass cls=clasesAborrar.get(i);
			
			Collection cIndividuos=cls.getInstances(true);
			for(Iterator itI=cIndividuos.iterator();itI.hasNext();)
		         {
		        	Object posibIndiv=itI.next();
		          	if(posibIndiv instanceof RDFResource){
		          		RDFResource individuo=(RDFResource)posibIndiv;
		          		individuo.delete();
		          	}
		        }
				cls.delete();
		}
		System.out.print("\n\n CLASES NEGOCIO:\n"+nombreClasesNegocio);
		System.out.print("\n\n DATAPROPERTYS NEGOCIO:\n"+nombreDataPropiedadesNegocio);
		System.out.print("\n\n OBJECTPROPERTYS NEGOCIO:\n"+nombreObjectPropiedadesNegocio);
		//creacion de un individuo por cada clase
		Iterator itClasesNegocio=nombreClasesNegocio.iterator();
		
		while(itClasesNegocio.hasNext()){
			OWLIndividual i=clsClase.createOWLIndividual((String)itClasesNegocio.next()+"_");
		}
		System.out.print("...info creados individuos clase");
		Iterator itnombreDataPropiedadesNegocio=nombreDataPropiedadesNegocio.iterator();
		while(itnombreDataPropiedadesNegocio.hasNext()){
			OWLIndividual i=clsDataPropiedad.createOWLIndividual((String)itnombreDataPropiedadesNegocio.next()+"_");
		}
		System.out.print("...info creados individuos dataproperty");
		
		Iterator itnombreObjectPropiedadesNegocio=nombreObjectPropiedadesNegocio.iterator();
		while(itnombreObjectPropiedadesNegocio.hasNext()){
			OWLIndividual i=clsObjectPropiedad.createOWLIndividual((String)itnombreObjectPropiedadesNegocio.next()+"_");
		}
		System.out.print("...info creados individuos objectProperty");
		  
		return owl;
}
	 
	 /*obtiene el dominio de una propiedad owl,
	  *TODO: QUE AL CARGAR UNA ONTOLOGÍA ERP AVISE DE TODAS AQUELLAS PROPIEDADES SIN DOMINIO.
	  * 
	  */ 
	  
	 public static  ArrayList<String> getDomain(OWLProperty owlprop){
		 ArrayList<String> ldominios=new ArrayList<String>();
		Collection dominios=owlprop.getDomains(false);
		
        	Iterator itdom=dominios.iterator();
        	while(itdom.hasNext()){
        		RDFResource reso=(RDFResource)itdom.next();	
        		//System.out.println(" dominio de "+nameProp+" "+ reso.getLocalName()+"  ..."+reso.getClass());
        		if(reso instanceof OWLUnionClass){
        			OWLUnionClass union=(OWLUnionClass)reso;
    				for(Iterator itU=union.getOperands().iterator();itU.hasNext();){
    					OWLClass opCls=(OWLClass)itU.next();
    					ldominios.add(opCls.getLocalName());
    				}                	
        		}
                else if(reso instanceof OWLNamedClass){
                	ldominios.add(reso.getLocalName());
                }
                else{
                	//mensaje warning
                	System.err.println("\n WARNING: CASO NO CONTEMPLADO EN DOMINIO DE "+ owlprop.getLocalName()+"  CON EL TIPO "+reso+"  class="+reso.getClass());
                }
        		//System.out.println("\n ...dominio "+owlprop.getLocalName()+"  ="+ldominios);
        }
		 return ldominios;
	 }
	 
	 
	 
		/*public static boolean checkPosibleProblemsCardinality(LinkedList<RangeItem> lRangeItems,String tipoPropiedad,String namePropiedad,String nameClase){
			boolean fallo=false;
			if(!tipoPropiedad.equals("ObjectProperty")){
				fallo=true;
				for (int i=0;i<lRangeItems.size();i++){
					RangeItem ri=lRangeItems.get(i);
					if(ri.getOP()!=null && ri.getOP().equals(Constants.OP_CARDINALITY)){
						if(ri.getQMAX()!=null && ri.getQMAX().equals("1")){
							fallo=false;
						}
					}
				}
			}
			if(fallo)
				System.err.println("WARNING:La dataProperty '"+namePropiedad+"' de la clase '"+nameClase+"' no tiene definida la cardinalidad maxima 1");
			return fallo;
		}*/
		
		
	 
	
	
	
	
	
	
	public static void main(String args[]) throws Exception{
		String path="E:/DESARROLLO/ONTOLOGIA/"; 
		String nameFile="Modelo.owl";
		String uri = "file:///" + path + nameFile;
		JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
		JenaOWLModel owlModelTransf=(JenaOWLModel)extractSystemOntology(owlModel);
		
		//nueva uri para la salida
		uri=path+"DynagentSystemOntology.owl";
	    //alternatively, you can specify a local path on your computer
	    //for the travel.owl ontology. Example:
	    //String uri = "file:///c:/Work/Projects/travel.owl"
	  
	    
	    Collection errors = new ArrayList();
	    owlModelTransf.save(new File(uri).toURI(), FileUtils.langXMLAbbrev, errors);
	    System.err.println("\n\n...........File saved with " + errors.size() + " errors in path="+uri);		
	}
	
	
	public static Category buildCategory(OWLProperty prop){
		Category category=new Category();
		if(prop.isFunctional()){
	 		category.setFunctional();
	 	}
	 	if(prop.isInverseFunctional()){
	 		category.setInverseFunctional();
	 	}
	 	if(prop.isObjectProperty()){
	 		category.setObjectProperty();
	 		OWLObjectProperty  obProp=(OWLObjectProperty)prop;
	 		if(obProp.isSymmetric()){
	 			category.setSymmetric();
	 		}
	 		if(obProp.isTransitive()){
	 			//category.setTransitive();
	 			category.setStructural();
	 			
	 		}
	 		//¿Es una unidad? --->la categorizamos como dataproperty
	 		if(OWLAux.isSpecializedFrom(obProp.getRange(),Constants.CLS_UNIDADES)){
	 			category.setDataProperty();
	 		}
	 	}
	 	else{//es dataproperty
	 	category.setDataProperty();	
	 	}
	 	return category;
	}
	
	
	
	public  static Propiedad traslateToPropiedad(OWLProperty owlprop){
		String name=owlprop.getLocalName();	
		int idProp = 0;
	    String tipoPropiedad,mask; 
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
	   
	    Float qmin=null;
	    Float qmax=null;
	    String nameinv=null;
	    if(rangeitem.getQMAX()!=null){
	    	qmax=Float.valueOf(rangeitem.getQMAX());
	    }
	    if(rangeitem.getQMIN()!=null){
	    	qmin=Float.valueOf(rangeitem.getQMIN());
	    }
	  
	    if(category.isDataProperty()){//solo metemos el tipo de las dataproerties en la tabla properties. La mascara solo tiene sentido en las dataproperty
	    	 //pp.setMASK(mask); //TODO Esto ya se hace en el xml. De toda maneras si quisieramos tener en cuenta alguna mascara dicha en el modelo habria que crear un registro en la tabla masks
	    	 if(rangeitem.getVALUECLS()!=null){

	         }
	    }
	    if(rangeitem.getOP()!=null){

	    }
	    
	  //PROP INVERSA
	    RDFProperty propinv=owlprop.getInverseProperty();
	    if(propinv!=null){
	    	//System.out.print("  inversa de "+owlprop+"  es "+propinv.getLocalName());
	    	nameinv=propinv.getLocalName();
	    	//obtenemos el idprop de la inversa
	    	
	    
	    }
	    Propiedad pp=new Propiedad(name,rangeitem.getVALUECLS(),rangeitem.getOP(),qmin,qmax,nameinv,cat);
	    
	    return pp;
	}
	
	public static boolean checkPosibleProblemsCardinality(LinkedList<RangeItem> lRangeItems,String tipoPropiedad,String namePropiedad,String nameClase){
		boolean fallo=false;
		if(!tipoPropiedad.equals("ObjectProperty")){
			fallo=true;
			for (int i=0;i<lRangeItems.size();i++){
				RangeItem ri=lRangeItems.get(i);
				if(ri.getOP()!=null && ri.getOP().equals(Constants.OP_CARDINALITY)){
					if(ri.getQMAX()!=null && ri.getQMAX().equals("1")){
						fallo=false;
					}
				}
			}
		}
		if(fallo&&!namePropiedad.equals("imagen")&&!namePropiedad.equals("propiedades")&&!namePropiedad.equals("clases"))
			System.err.println("WARNING:La dataProperty '"+namePropiedad+"' de la clase '"+nameClase+"' no tiene definida la cardinalidad maxima 1");
		return fallo;
	}
	
	
}



	


