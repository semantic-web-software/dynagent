package dynagent.tools.owl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

import org.drools.FactHandle;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;

import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.base.DefaultConsequenceExceptionHandler;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.rule.Package;
import org.drools.rule.Rule;
import org.drools.spi.Activation;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;
import dynagent.common.exceptions.EngineException;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.src.ruler.IPropertyChangeDrools;
import dynagent.ruleengine.src.ruler.MyConsequenceException;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLDatatypeProperty;


public class OWLDroolsRuler {
	
	
	private RuleBase ruleBase;
//	private PackageBuilder builder;
	private WorkingMemory workingMemory;
	private OWL owl;
	
	public OWLDroolsRuler(OWL owl,ArrayList<Package>rulespackages) throws Exception{
		this.owl=owl;
//		this.builder = new PackageBuilder();
		RuleBaseConfiguration conf = new RuleBaseConfiguration();
		conf.setConsequenceExceptionHandler(new DefaultConsequenceExceptionHandler(){
		
			public void handleException(Activation activation, WorkingMemory workingMemory, Exception exception) {
				throw new MyConsequenceException(exception,workingMemory,activation);
			}
			
		});
		
		this.ruleBase = RuleBaseFactory.newRuleBase( conf );
		Iterator it=rulespackages.iterator();
		while(it.hasNext()){
			Package pkg=(Package)it.next();
			Rule[] rulesp=pkg.getRules();
			//System.out.println("\n\n DEBUG RULE PACKAGE LOADED: "+pkg.getLocalName()+"  with numRules:"+rulesp.length);
			for(int j=0;j<rulesp.length;j++){
				Rule rul=rulesp[j];
				//System.out.println("...................rule loaded "+rul.getLocalName());
			}
			ruleBase.addPackage( pkg );
			
		}
		
		PackageBuilder builder=new PackageBuilder();
		
		workingMemory = ruleBase.newStatefulSession();
		
		((StatefulSession)workingMemory).dispose();
	
		//INSERTAMOS LA REPRESENTACIÓN DE OWL AL MOTOR
		
		this.insertOWLRepresentation();
		String grupoRules="rules";
		System.out.println("\n\n LLAMADA A RUN GRUPO REGLAS: valores:"+grupoRules);
		this.fireRules(grupoRules);
	}
	
	
	public void insertOWLRepresentation(){
		LinkedList<TripletaPropiedadClase> tripletaspropclase=this.getOWL().getAllTripletasPropiedadClase();
		for(int i=0;i<tripletaspropclase.size();i++){
			this.insert(tripletaspropclase.get(i));
		}
		LinkedList<Herencia> herencias=this.getOWL().getHerencias();
		for(int i=0;i<herencias.size();i++){
			//System.out.println("\ndebug insertherencia:"+herencias.get(i));
			this.insert(herencias.get(i));
		}		 
		HashSet<OWLProperty> owlpropiedades=this.getOWL().getPropiedades();
		Iterator itowlpropiedades=owlpropiedades.iterator();
		while(itowlpropiedades.hasNext()){
			OWLProperty owlprop=(OWLProperty)itowlpropiedades.next();
			//System.out.println("debug insertproperty:"+owlprop);
			if(owlprop.getRDFType()!=null)//los slots borrados daban problemas
				this.insert(OWLAux.traslateToPropiedad(owlprop));
		}
		//introducimos infomración de los modulos_negocio (son instancias)
		LinkedList<ValorIndividuo> valoresModulosNegocio=new LinkedList<ValorIndividuo>(); 
		ArrayList<OWLIndividual> modulosNegocioOWL=this.getOWL().getInstancias("MÓDULO_NEGOCIO");
		
		for(int i=0;i<modulosNegocioOWL.size();i++){
	  		OWLIndividual owlindividuo=modulosNegocioOWL.get(i);
	  		//el nombre se introduce como un valor en propiedad ficticia "rdn"
	  		valoresModulosNegocio.add(new ValorIndividuo("MÓDULO_NEGOCIO"+i,"MÓDULO_NEGOCIO","rdn",owlindividuo.getLocalName()));
	        Collection cprop=owlindividuo.getRDFProperties();
	        for(Iterator it=cprop.iterator();it.hasNext();){
	        	RDFProperty rdfprop=(RDFProperty)it.next();
	        	String nameprop=rdfprop.getLocalName();
	        		if(rdfprop instanceof DefaultOWLDatatypeProperty){
	        			Collection collecValores=owlindividuo.getPropertyValues(rdfprop, true);
	        			Iterator itValores=collecValores.iterator();
	        			while(itValores.hasNext()){
	        				Object value=itValores.next();
		        			owlindividuo.getHasValuesOnTypes(rdfprop);
		        			String svalue =value.toString();
		        			valoresModulosNegocio.add(new ValorIndividuo("MÓDULO_NEGOCIO"+i,"MÓDULO_NEGOCIO",rdfprop.getLocalName(),svalue));
	        			}
	        		}//las objectproperty no tienen valores en esta clase (TODO CUANDO SE HAGA COMPLETO HACER METODO QUE PARA TODAS LAS CLASES OBTENGA SUS INDIVIDUOS EN REPRESENTACION ValorIndividuo
	        }
		}
		//System.out.println("valoresModulosNegocio:"+Auxiliar.IteratorToStringByRows(valoresModulosNegocio.iterator()));
		for(int i=0;i<valoresModulosNegocio.size();i++){
			this.insert(valoresModulosNegocio.get(i));
			
		}
		
		
		//introduciomos también OWL.java para tener toda la información accesible desde reglas
		workingMemory.insert(this.getOWL());
	}
	
	public WorkingMemory getWorkingMemory() {
		return workingMemory;
	}

	public void setWorkingMemory(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}
	
	/*public LinkedList<FactHierarchy> getAllHierarchyFacts(Object idto, Object idtoSup) throws NotFoundException, IncoherenceInMotorException {
		LinkedList<FactHierarchy> hierarchy = new LinkedList<FactHierarchy>();
		if(idtoSup == null && idto != null && !(idto instanceof Null))
			hierarchy = getHierarchyFactsWhereIdto(((Integer) idto).intValue());
		else if(idto == null && idtoSup != null && !(idtoSup instanceof Null))
			hierarchy = getHierarchyFactsWhereIdtoSup(((Integer) idtoSup).intValue());
		else if(idto != null && !(idto instanceof Null) && idtoSup != null && !(idtoSup instanceof Null))
			hierarchy = getHierarchyFactsWhereIdtoAndIdtoSup(((Integer) idto).intValue(),((Integer) idtoSup).intValue());
		
		else{
			//LA REGLAS DE QUERY SE DEBEN DISOCNTINUAR POR MULTIPLES PROBLEMAS(problemas focos, rendimiento,.."+instances);
			System.err.println("\n\n jbossEngine.getAllHierarchyFacts() :QUERY NO IMPLEMENTADA! No está definida la  query; idto="+idto+"idtoSup="+idtoSup);
			throw new NotFoundException("\n\n  jbossEngine.getAllHierarchyFacts() QUERY NO IMPLEMENTADA! \n\n QUERY NO IMPLEMENTADA: No está definida la  query idto="+idto+"idtoSup="+idtoSup);
		}
		
		return hierarchy;
		
	}*/
	
	
	public synchronized void insert(IPropertyChangeDrools fact){
		//System.out.println("\n DEBUG INSERT:"+fact);		
		workingMemory.insert(fact);
				FactHandle fh = workingMemory.insert(fact,true);
				fact.setFactHandle(fh);
	}
	
	
	public void fireRules(String grupo){
		System.out.println("--------Fire rules a grupo: "+grupo);
		boolean success=false;
		try{
			workingMemory.setFocus(grupo);
			workingMemory.fireAllRules();
			success=true;
		}finally{
			if(!success){
				workingMemory.clearActivationGroup(grupo);
				workingMemory.clearRuleFlowGroup(grupo);
				workingMemory.clearAgendaGroup(grupo);
				//Con esto obligamos a que quite este grupo de la focusStack. Pero estamos seguros que no volvera a dar la misma exception porque hemos limpiado las activaciones
				workingMemory.fireAllRules();
			}
		}
	}
	
	public synchronized void retract(IPropertyChangeDrools fact) {
			workingMemory.retract(fact.getFactHandle());
	}
	
	public void inicializeRules(/*String fileRules*/ArrayList<Package> rulesPackage) throws EngineException{
		try{

			Iterator<Package> itr=rulesPackage.iterator();
			while(itr.hasNext()){
				ruleBase.addPackage( itr.next() );
			}

		}catch(Exception ex){
			EngineException e=new EngineException("Excepcion al inicializar las reglas"+ex);
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}
	    
			
	}
	
	
	public OWL getOWL() {
		return owl;
	}


	public void setOwl(OWL owl) {
		this.owl = owl;
	}	
		
	
}
