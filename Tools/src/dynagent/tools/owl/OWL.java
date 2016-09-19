                                                                                /**
 * OWLParser.java
 * @author Jose A. Zamora Aguilera - 
 * @description Parse a OWL Protege file and build  the dynagent tables structure and the facts that represents the model
 */	

package dynagent.tools.owl;


import java.io.File;
import java.sql.SQLException;
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
import dynagent.common.basicobjects.HelpClass;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.exceptions.BadModelDefinitionException;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.ConceptLogger;
import dynagent.ruleengine.src.ruler.FactHierarchy;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
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

public class OWL{

	//tabla que guarda la equivalencia entre el nombre de las clases y su identificador entero
	private edu.stanford.smi.protegex.owl.model.OWLModel protegeowlModel=null;
	private HashMap<String, OWLClase> clases=new HashMap<String, OWLClase>();
	private HashMap<String, OWLProperty> hmOWLPropertyxpropiedad=new HashMap<String, OWLProperty>();
	//Lista que guardará todos los instances del modelo
	private LinkedList<TripletaPropiedadClase> allInstances = new LinkedList<TripletaPropiedadClase>();
	private LinkedList<Herencia> herencias = new LinkedList<Herencia>();	
	
	public OWLClase getClase(String nombre){
		return this.clases.get(nombre);
	}
	
	
	public ArrayList<OWLIndividual> getInstancias(String nombreClase){
		 ArrayList<OWLIndividual> result=new  ArrayList<OWLIndividual>();
		 if(this.getClase(nombreClase)!=null)
			 result=this.getClase(nombreClase).getIndividuals();
		 else{System.err.println("\n WARNING: OWL.getInstancias llamada con clase que no existe: nombreClase="+nombreClase);}
		 return result;
	}
	
	public static String extractOWL_prefix(String namePropiedad){
    	if(namePropiedad.matches("p.:.+")){
    		namePropiedad=namePropiedad.substring(3);
    	}
    	return namePropiedad;
	}	
	
	public OWLProperty getProperty(String nombre){
		OWLProperty a=null;
		return this.hmOWLPropertyxpropiedad.get(nombre);
	}
	
	
	public boolean hasClass(String nombre){
		return this.getClase(nombre)!=null;
	}
	
	public boolean hasProperty(String nombrePropiedad){
		return this.getProperty(nombrePropiedad)!=null;
	}
	
//	public OWL (String uri) throws Exception{
//		OWLModel owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
//		this.protegeowlModel=owlModel;
//		//propiedades
//		Collection properties= owlModel.getUserDefinedOWLProperties();
//		
//		for (Iterator itP = properties.iterator(); itP.hasNext();){
//        	OWLProperty owlprop=(OWLProperty)itP.next();
//        	String name=owlprop.getLocalName();
//        	this.hmOWLPropertyxpropiedad.put(name, owlprop);
//		}		
//		
//		//clases
//		  Collection<OWLNamedClass> clases = owlModel.getUserDefinedOWLNamedClasses();
//		  Iterator itClases=clases.iterator();
//			
//			while(itClases.hasNext())
//	        {
//	            OWLNamedClass cls = (OWLNamedClass) itClases.next();
//	            if(cls.getLocalName().contains("Axiom")){
//	            	System.out.println(" INFO: CLASE Axiom no se añade a clase java OWL "+cls+" type="+cls.getClass());
//	            }
//	            else{
//	            	this.getClases().put(cls.getLocalName(), new OWLClase(cls));
//	            }
//	        }
//			
//		this.buildInstances(owlModel);
//		this.buildHerencias(clases);
//	}
	
	public OWL (edu.stanford.smi.protegex.owl.model.OWLModel protegeowlModel){
		this.protegeowlModel=protegeowlModel;
		//propiedades
		Collection properties= protegeowlModel.getUserDefinedOWLProperties();
		
		for (Iterator itP = properties.iterator(); itP.hasNext();){
        	OWLProperty owlprop=(OWLProperty)itP.next();
        	String name=owlprop.getLocalName();
        	this.hmOWLPropertyxpropiedad.put(name, owlprop);
		}		
		
		//clases
		  Collection<OWLNamedClass> clases = protegeowlModel.getUserDefinedOWLNamedClasses();
		  Iterator itClases=clases.iterator();
			
			while(itClases.hasNext())
	        {
	            OWLNamedClass cls = (OWLNamedClass) itClases.next();
	            if(cls.getLocalName().contains("Axiom")){
	            	//System.err.println(" INFO: CLASE Axiom no se añade a clase java OWL "+cls+" type="+cls.getClass());
	            }
	            else{
	            	this.getClases().put(cls.getLocalName(), new OWLClase(cls));
	            }
	        }
			
		this.buildInstances(protegeowlModel);
		this.buildHerencias(clases);
		
			 
	/*
	 * 	 Collection errors = new ArrayList();String uriDestination="E:/DESARROLLO/ONTOLOGIA/ModeloTransformado.owl"; 
	JenaOWLModel owlModelTransf=(JenaOWLModel)protegeowlModel;
	owlModelTransf.save(new File(uriDestination).toURI(), FileUtils.langXMLAbbrev, errors);
	 System.err.println("\n\n...........File saved with " + errors.size() + " errors in path="+uriDestination);
	 */
		
	//System.out.println("\n DEBUG FINAL CONSTRUCTOR OWL: instancesSize:"+this.getAllTripletasPropiedadClase().size()+" herenciassize:"+this.getHerencias().size());
	}
	
	
	private void  buildInstances(OWLModel owlModel){
		String nameClase,prop;
		
		for (Iterator it =owlModel.getUserDefinedOWLNamedClasses().iterator(); it.hasNext();)
		{
	    	OWLNamedClass cls = (OWLNamedClass) it.next();
	    	nameClase=cls.getLocalName();
	    	if(nameClase.startsWith("Axiom_"))
	    		continue;
	     
	        //System.out.println("[OWL]:_=================="+nameClase+"=============================");
	        
	        //Propiedades de esta clase
	       //System.out.println("[OWLParser]:_"+nameClase+"  tiene "+cls.getAssociatedProperties().size()+"   propiedades."); 
	      
	        Collection cPropiedades=cls.getAssociatedProperties();
	 
	       
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
	        	
	        	if (rdfRng==null&&!OWLAux.hasRestricctionHasValue(cls,rdfProp)){
	        		if(!namePropiedad.equals(Constants.PROP_SOURCECLASS)&&!namePropiedad.equals(Constants.PROP_TARGETCLASS)&&!namePropiedad.equals(Constants.PROP_ITERATOR)&&!namePropiedad.equals(Constants.PROP_CONFIRMED_SOURCE)){
	        			System.out.println("[OWL.java]:_       WARNING: No se ha asignado ningún rango a: "+namePropiedad);
	        		}
	        		TripletaPropiedadClase ins=new TripletaPropiedadClase( nameClase,namePropiedad,null,null,null,null ,null);
	        		this.getAllTripletasPropiedadClase().add(ins);
	        	}
	        	
	        	else if(rdfRng!=null&&!OWLAux.hasRestricctionHasValue(cls,rdfProp))  //si tiene restriccion hasValue su rango se contruye en el metodo que procesa las restricciones
	        	{
	        		if(tipoPropiedad.equals("ObjectProperty")){
	       	     	    lRangeItems=traslateObjectRange(rdfRng);
	        		}else{ //es dataProperty
	        			lRangeItems=traslateDataRange(rdfRng,rdfProp);	
	        		}
	        	}
	        	
	        	if(cls.getRestrictions(rdfProp, true).size()>0)
	        	{
	        		lRangeItems.addAll(traslateRestrictions(cls,rdfProp));
	        	}
	        
	        	//*if(!*/OWLAux.checkPosibleProblemsCardinality(lRangeItems, tipoPropiedad, namePropiedad, nameClase);/*){*/
	        		for (int i=0;i<lRangeItems.size();i++){
	        			RangeItem ri=lRangeItems.get(i);
	        			String sqmax=ri.getQMAX();
	        			String sqmin=ri.getQMIN();
	        		
	        			Double dqmax=null;
	        			Double dqmin=null;	
	        			        			
	        			if(sqmax!=null){
	        				dqmax=Double.valueOf(sqmax);
	        			
	        			}
	        			if(sqmin!=null){
	        				dqmin=Double.valueOf(sqmin);
	        			
	        			}
	        			
	        			//System.out.println("\n debug owl ri.getqmin:"+ri.getQMIN()+"  ri.getqmax:"+ri.getQMAX()+"  nameClase:"+nameClase+" namePropiedad:"+namePropiedad+" value:"+ri.getVALUE()+"  valuecls:"+ri.getVALUECLS()+" op:"+ri.getOP());
	        			TripletaPropiedadClase ins=new TripletaPropiedadClase( nameClase,namePropiedad,ri.getVALUE(),ri.getVALUECLS(),dqmin ,dqmax,ri.getOP());
	        			this.getAllTripletasPropiedadClase().add(ins);
	        		}
	        	/*}*/
	        }        
	    }
		//System.out.println("...fin buildAllInstances lnamedClases="+this.getLnameClases());
	}
	
	public void buildHerencias(Collection clases){
		String nameS;
		Iterator itClases=clases.iterator();
		while(itClases.hasNext())
        {
            OWLNamedClass cls = (OWLNamedClass) itClases.next();
            String name=cls.getLocalName();
            if(name.startsWith("Axiom_")){
	    		continue;
            }
            //herencias trivial de si misma (hace las reglas más faciles de implementar)
        	Herencia her_triv=new Herencia(name,name);
        	herencias.add(her_triv);
    		//parámetro a false para que solo devuelva los padres directos 
            //true devuelve los padres,abuelos,...)
            for (Iterator itSup=cls.getNamedSuperclasses(true).iterator(); itSup.hasNext();)
            {
            	RDFResource sc=(RDFResource)itSup.next();
            	nameS=sc.getLocalName();
            	if(!nameS.equals("Thing")){//EXCLUIMOS LA HERENCIA TRIVIAL DE THING
            		herencias.add(new Herencia(name,nameS));
            	}
            }
        }
	}
	
	public boolean isSpecialized(String nameClass,String namePossibleSuperior){
		OWLClase clase= this.getClase(nameClass);
		boolean result=false;
		if(clase!=null){
			result=clase.hasSuperior(namePossibleSuperior);
			
		}else{
			result=false;
			
			
		}
		//System.out.println("\n db isSpecialized nameClass="+nameClass+"  namePossibleSuperior="+namePossibleSuperior);
		return result;
	}
	


	public LinkedList <RangeItem> traslateRestrictions(OWLNamedClass cls, RDFProperty rdfProp ){
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
	
	
	
	
	
	
	
	public LinkedList <RangeItem> traslateDataRange(RDFResource rdfRng,RDFProperty rdfProp){
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
	
	
	public LinkedList <RangeItem> traslateObjectRange(RDFResource rdfRng){
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
	
	
	
	
	public HashMap<String, OWLClase> getClases() {
		return clases;
	}

	public HashMap<String, OWLProperty> getHmOWLPropertyxpropiedad() {
		return hmOWLPropertyxpropiedad;
	}
	
	
	public HashSet<OWLProperty> getPropiedades() {
		HashSet<OWLProperty> result=new HashSet<OWLProperty>();
		Iterator it=this.getHmOWLPropertyxpropiedad().keySet().iterator();
		while (it.hasNext()){
			result.add((OWLProperty)this.getHmOWLPropertyxpropiedad().get(it.next()));
		}
		return result;
	}
	

	public edu.stanford.smi.protegex.owl.model.OWLModel getProtegeowlModel() {
		return protegeowlModel;
	}
	
	public String toString(){
		String result="<OWL "+this.getProtegeowlModel().getName()+">";
		
		Iterator it=this.getClases().keySet().iterator();
		while (it.hasNext()){
			String nameClase=(String)it.next();
			OWLClase cls=this.getClases().get(nameClase);
			result+="\n\n"+cls.toString();
		}
		result+="\n /<OWL >\n";
		return result;
	}
	
	public void saveInFile(String path,String nameFile){
			String uri = path + nameFile;
			System.out.println("\n... INFO: OWL.saveInFile: path="+path+" nameFile="+nameFile);
			//alternatively, you can specify a local path on your computer
		    //for the travel.owl ontology. Example:
		    //String uri = "file:///c:/Work/Projects/travel.owl"
		    Collection errors = new ArrayList();
		    JenaOWLModel jenaowlModel=(JenaOWLModel)this.getProtegeowlModel();
		    jenaowlModel.save(new File(uri).toURI(), FileUtils.langXMLAbbrev, errors);
		    System.err.println("\n\n...........File saved with " + errors.size() + " errors in path="+uri);	
			}
	
	public static void main(String args[]) throws Exception{
		String path="E:/DESARROLLO/ONTOLOGIA/"; 
		String nameFile="Modelo.owl";
		String uri = "file:///" + path + nameFile;
		JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
	    OWL modelo=new OWL(owlModel);
	    System.out.print(modelo.toString());
}

	public LinkedList<TripletaPropiedadClase> getAllTripletasPropiedadClase() {
		return allInstances;
	}
	
		public LinkedList<Herencia> getHerencias() {
		return herencias;
	}
	
	public HashSet<String>  getValores(OWLIndividual instancia,String nombrePropiedad){
		HashSet<String> valores=new HashSet<String>(); 
		RDFProperty rdfprop_propiedad=this.getProperty(nombrePropiedad);
		if(rdfprop_propiedad==null){
			System.err.println("\n WARNING getValores: instancia:"+instancia+" nombrePropiedad:"+nombrePropiedad+" no existe esa propiedad en esta ontología"+this);
		}
    	Iterator it2=instancia.getPropertyValues(rdfprop_propiedad).iterator();
        while (it2.hasNext()){
        	valores.add((String)it2.next());
        }
    	
    	return valores;
	}
	
	public void checkModulos() throws BadModelDefinitionException{
		boolean problem=false;
		String mensajeError="";
		//OWLClase claseModuloNegocio=this.getClase("MÓDULO_NEGOCIO");
		//if(claseModuloNegocio!=null){
		//	ArrayList<OWLIndividual> individualsModulos=claseModuloNegocio.getIndividuals();
		
		ArrayList<OWLIndividual> instanciasModulos=this.getInstancias("MÓDULO_NEGOCIO");
		for(int i=0;i<instanciasModulos.size();i++){
			OWLIndividual modulo=instanciasModulos.get(i);	
			String nameModulo=instanciasModulos.get(i).getLocalName();
			HashSet<String>ClasesModulo=this.getValores(modulo, "clases");
			HashSet<String>propiedadesModulo=this.getValores(modulo, "propiedades");
			//System.out.println("\n CheckModule:"+nameModulo+"  propiedades:"+propiedadesModulo+" clases:"+ClasesModulo);
			Iterator itC=ClasesModulo.iterator();
			while(itC.hasNext()){
				String clase=(String)itC.next();
				if(this.getClase(clase)==null){
					problem=true;
					mensajeError+="\n - No existe la clase "+clase+" definida en el modulo:"+nameModulo;
				}
			}
			Iterator itP=propiedadesModulo.iterator();
			while(itP.hasNext()){
				String propiedad=(String)itP.next();
				if(this.getProperty(propiedad)==null){
					problem=true;
					mensajeError+="\n - No existe la propiedad "+propiedad+" definida en el modulo:"+nameModulo;
				}
			}
		}
		if(problem){
			throw new BadModelDefinitionException(mensajeError);
		}
	}
	
	
	public void versionarModelo(String path,String nombreVersion){
		ArrayList<OWLIndividual>  individuosversion=this.getInstancias("VERSIÓN_MODELO");
		for(int i=0;i<individuosversion.size();i++){
			individuosversion.get(i).delete();
		}
		OWLNamedClass owClassVersionModelo = this.getProtegeowlModel().getOWLNamedClass("VERSIÓN_MODELO"); 
		OWLIndividual owlindividualVersion = owClassVersionModelo.createOWLIndividual(nombreVersion);
		owlindividualVersion.addPropertyValue(this.getProperty("nombre"), nombreVersion);
		this.saveInFile(path,nombreVersion);
	}
}

	



