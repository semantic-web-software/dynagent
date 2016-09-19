package dynagent.tools.owl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.Constants;


import edu.stanford.smi.protegex.owl.model.OWLClass;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;


public class OWLClase {
	
	private edu.stanford.smi.protegex.owl.model.OWLNamedClass owlCls;
	private ArrayList<String> specializeds=new ArrayList<String>(); 
	private ArrayList<String> superiors=new ArrayList<String>();
	private ArrayList<String> propiedades=new ArrayList<String>();
	private HashMap<String,RDFProperty> mapaRDFPROP_propiedad=new HashMap<String,RDFProperty>();
	String name=null;
	
	public  OWLClase( edu.stanford.smi.protegex.owl.model.OWLNamedClass cls){
		this.owlCls=cls;
		this.name=cls.getLocalName();
		
		//superiores
		for(Iterator itSup=cls.getNamedSuperclasses(true).iterator();itSup.hasNext();)
		{
			RDFResource sc=(RDFResource)itSup.next();
			this.superiors.add(sc.getLocalName());
		}
		//especializados
		for(Iterator itSpec=cls.getNamedSubclasses(true).iterator();itSpec.hasNext();)
		{
			OWLClass sc=(OWLClass)itSpec.next();
			specializeds.add(sc.getLocalName());
		}	
		//propiedades de la clase
		  for(Iterator itp=cls.getAssociatedProperties().iterator();itp.hasNext();)
	       {  
	        	
	    		RDFProperty rdfProp=(RDFProperty)itp.next();
	        	String namePropiedad=rdfProp.getLocalName();
	        	this.propiedades.add(namePropiedad);
	        	this.mapaRDFPROP_propiedad.put(namePropiedad, rdfProp);
	        	
	        	//System.out.println("\n\n =============== CLASE ==="+this.getNAME());
	        	//System.out.println("- propiedad= "+namePropiedad+"  "+rdfProp.getClass());
	        	String tipoPropiedad=rdfProp.getRDFType().getLocalName();
	        	//System.out.println("[OWLParser]:_      -"+namePropiedad+" ("+prop+")  es: "+tipoPropiedad);
	        	
	        	//El rango que aplica para una propiedad en una clase es el definido con AllValuesFrom o el definido en P si no se hizo ninguna restriccion de ste tipo
	        	RDFResource rdfRng=cls.getAllValuesFrom(rdfProp);
	        	if(rdfRng!=null){
	        		//System.out.println("....rango= "+rdfRng+"  RAngoType="+rdfRng.getClass());
	        	}
	        	else if(!namePropiedad.equals(Constants.PROP_SOURCECLASS)&&!namePropiedad.equals(Constants.PROP_TARGETCLASS)&&!namePropiedad.equals(Constants.PROP_ITERATOR)&&!namePropiedad.equals(Constants.PROP_CONFIRMED_SOURCE)){
	        		
	        		System.out.println("....Warning RANGO NULL en propiedad: "+namePropiedad);
	        	}
	        	Collection cRestrictions=cls.getRestrictions(rdfProp, true);
	    		
	    		for(Iterator iR =cRestrictions.iterator(); iR.hasNext();)
	    		{
	        		OWLRestriction clRes=(OWLRestriction)iR.next();
	        		//System.out.println(".... restriccion"+clRes+"          ---restrType="+clRes.getClass());
	        		
	        		
	        		
	        		if(clRes.getProtegeType().getLocalName().equals("SomeValuesFromRestriction"))
	        		{
	        			//Si el rango es complejo aviso  de que en el modelo se ha usado mal una SomeValuesRestriction
	        			if(clRes.getFillerText().contains(" ")){
	        				System.out.println("[OWLParser]:_      WARNING: Hay una restricción someValuesFrom apuntando a un rango complejo:  se ignorará.");
	        				//RuleEngineLogger.getLogger().write("      WARNING: Hay una restricción someValuesFrom apuntando a un rango complejo:  se ignorará.");
	        			}
	        			//Si el rango es simple (una sola clase) damos la info de rango y cardinalidad en un solo registro mediante una quantitive cardinality restricctions (OP=QCR)  
	        			else{
	        			
	        			}
	        		}
	        		else if(clRes.getProtegeType().getLocalName().equals("MinCardinalityRestriction"))
	        		{
	        		//	CardMin=clRes.getFillerText();
	        			
	        		}
	        		else if(clRes.getProtegeType().getLocalName().equals("MaxCardinalityRestriction"))
	        		{
	        			//CardMax=clRes.getFillerText();
	        			
	        		}
	        		else if(clRes.getProtegeType().getLocalName().equals("CardinalityRestriction"))
	        		{
	        			//CardMin=clRes.getFillerText();
	        			//CardMax=clRes.getFillerText();
	        		}
	        		else if(clRes.getProtegeType().getLocalName().equals("HasValueRestriction")) //(has value implica al menos un valor cardinalidad mínima 1)
	        		{
	        		}
	        		//todo individuos
	    		}
	    	}
	}

	public ArrayList<String> getSpecialized() {
		return this.specializeds;
	}



	public ArrayList<String> getSuperior() {
		return this.superiors;
	}

	

	public String getNAME() {
		return name;
	}

	
		public boolean hasSuperior(String posibleSuperior){
		return this.getSuperior().contains(posibleSuperior);
	}

	public edu.stanford.smi.protegex.owl.model.OWLNamedClass getOwlCls() {
		return owlCls;
	}
	
	public void deleteRestrictions() {
		Iterator it=this.getmapaRDFPROP_propiedad().keySet().iterator();
		while(it.hasNext()){
			String nameProp=(String)it.next();
			RDFProperty rdfprop=this.getmapaRDFPROP_propiedad().get(nameProp);
			Collection cRestrictions=this.getOwlCls().getRestrictions(rdfprop, true);
    		
    		for(Iterator iR =cRestrictions.iterator(); iR.hasNext();)
    		{
        		OWLRestriction clRes=(OWLRestriction)iR.next();
        		clRes.delete();
    		}
		}
	}

	public HashMap<String, RDFProperty> getmapaRDFPROP_propiedad() {
		return mapaRDFPROP_propiedad;
	}

	

	public String getLocalName() {
		return name;
	}

	public ArrayList<String> getSpecializeds() {
		return specializeds;
	}

	public ArrayList<String> getSuperiors() {
		return superiors;
	}
	
	public ArrayList<OWLIndividual> getIndividuals(){
		ArrayList<OWLIndividual> result=new ArrayList<OWLIndividual> ();
		Collection cIndividuos=this.getOwlCls().getInstances(true);
	  
		 for(Iterator itI=cIndividuos.iterator();itI.hasNext();)
	     {
	     	Object i=itI.next();
	     	if(i instanceof OWLIndividual){
	      		result.add((OWLIndividual)i);
	     	}
	     	else{
	     		System.out.println("\n DEBUGGGGGGGG OWLClase getIndividuos itera sobre i="+i+" que no es OWLIndividual sino "+i.getClass());
	     	}
	     }
		 return result;
	}
	
	public int deleteIndividuals(){
		int numDeletes=0;
		Collection cIndividuos=this.getOwlCls().getInstances(true);
	  
		 for(Iterator itI=cIndividuos.iterator();itI.hasNext();)
	     {
	     	Object i=itI.next();
	     	if(i instanceof RDFResource){
	      		((RDFResource)i).delete();
	      		numDeletes++;
	     	}
	     	else{
	     		System.out.println("\n DEBUGGGGGGGG OWLClase deleteIndividuals itera sobre i="+i+" que no es RDFResource");
	     	}
	     }
		 return numDeletes;
	}
	
	public void delete(){
		this.deleteIndividuals();
		//this.deleteRestrictions();//PARECE UN PROBLEMA PQ BORRA TB RESTRICCIONES QUE TIENE HEREDADAS, QUEDANDO ESTAS TB BORRADAS PARA LA CLASE PADRE
		this.owlCls.delete();
	}
	
	
	public String toString(){
		String result=" <OWLCLASE "+this.getLocalName()+">";
		result+="\n    superiors="+this.getSuperiors();
		result+="\n    specilizeds="+this.getSpecialized();
		result+="\n    propiedades="+this.getPropiedades();
		result+="\n    individuos="+this.getIndividuals();
		result=result+"\n </<OWLCLASE>";
		return result;
	}

	public HashMap<String, RDFProperty> getMapaRDFPROP_propiedad() {
		return mapaRDFPROP_propiedad;
	}

	public ArrayList<String> getPropiedades() {
		return propiedades;
	}
	
		
	

}
