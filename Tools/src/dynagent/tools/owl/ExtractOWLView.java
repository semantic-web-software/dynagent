package dynagent.tools.owl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.common.Constants;
import dynagent.common.exceptions.BadModelDefinitionException;
import dynagent.tools.importers.owl.RangeItem;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

public class ExtractOWLView {
	private OWL jmodel;
	boolean includeModules=false;
	ArrayList<String>modulos=new ArrayList<String>();
	
	
	public ExtractOWLView(/*OWLModel model,*/OWL owlmodel, ArrayList<String> modulos, boolean includeModules) throws BadModelDefinitionException{
		this.modulos=modulos;
		/*this.jmodel=new OWL(model);*/
		this.jmodel=owlmodel;
		this.includeModules=includeModules;
		this.jmodel.checkModulos();
	}
	
	
	public ExtractOWLView(/*String uri,*/OWL owlmodel, String modulesInclude,String modulesExclude) throws Exception{
//		transformacion a modulos y booleano
		String modulos_s=null;
		boolean includeModules=false;
		if(modulesInclude!=null&&modulesExclude==null){
			modulos_s=modulesInclude;
			includeModules=true;
		}
		else if(modulesInclude==null&&modulesExclude!=null){
			modulos_s=modulesExclude;
			includeModules=false;
		}else if(modulesInclude!=null&&modulesExclude!=null){
			System.err.println("\n ERROR NO SE PUEDEN USAR A LA VEZ LA OPCIÓN DE INCLUIR Y EXCLUIR MODULOS A LA VEZ: ");
			double a= 1.0/0.0;//solo para pruebas primera, 
			//TODO  cerrar programa correctamente y dar mensaje mala definicion  a usuario.
		}
		
		ArrayList<String> modulos=new ArrayList<String>();
		if(modulos_s != null){
			String[]v_modulos=modulos_s.split(";");
			for (int i=0;i<v_modulos.length;i++){
				modulos.add(v_modulos[i]);
			}
			this.modulos=modulos;
		}
		
//		OWLModel owlModel= ProtegeOWL.createJenaOWLModelFromURI(uri);
//		this.jmodel=new OWL(owlModel);
		this.jmodel=owlmodel;
		this.includeModules=includeModules;
		this.jmodel.checkModulos();		
	}
	
	public OWL extract(boolean exclusiones){
		//System.out.println("\n info: inicio extract");
		if(this.getModulos()!=null){
			if(this.isIncludeModules()){
				System.out.println(".. Se va a transformar el modelo  incluyendo los  modulos="+this.getModulos()+"    **************************************");
			}
			else{
				System.out.println("... Se va a transformar el modelo  excluyendo los  modulos="+this.getModulos()+"    **************************************");
			}
		
		OWLClase claseModuloNegocio=this.jmodel.getClase("MÓDULO_NEGOCIO");
		ArrayList<String> modulosAEXCLUIR=new ArrayList<String>();
		
		//los modulos no incluidos explicitamente son excluidos
		if(claseModuloNegocio!=null){
			ArrayList<OWLIndividual> individualsModulos=claseModuloNegocio.getIndividuals();
			//todas las propiedades y clases de modulos incluidos no se podrán excluir
			HashSet<String> propiedadesNoExcluir=new HashSet<String>(); 
			HashSet<String> clasesNoExcluir=new HashSet<String>();			
			HashSet<String> propiedadesPropuestaExclusion=new HashSet<String>(); 
			HashSet<String> clasesPropuestaExclusion=new HashSet<String>();			
			for(int i=0;i<individualsModulos.size();i++){
				String nameModulo=individualsModulos.get(i).getLocalName();
				boolean excluirModulo=(!includeModules&&modulos.contains(nameModulo)) || (includeModules&&!modulos.contains(nameModulo));
				OWLIndividual modulo=individualsModulos.get(i);				
				if(excluirModulo) {
					propiedadesPropuestaExclusion.addAll(this.getJmodel().getValores(modulo, "propiedades"));
					clasesPropuestaExclusion.addAll(this.getJmodel().getValores(modulo, "clases"));
					modulosAEXCLUIR.add(modulo.getLocalName());
					//novedad: eliminamos el individuo módulo negocio excluido
					individualsModulos.get(i).delete();
					
				}else{
					//modulo a incluir, sus propiedades y clases no se pueden excluir
					propiedadesNoExcluir.addAll(this.getJmodel().getValores(modulo, "propiedades"));
					clasesNoExcluir.addAll(this.getJmodel().getValores(modulo, "clases"));
				}
			}

			//clases/proiedades a excluir=clases-propiedaes propuestas exclusion - clases-propiedades no excluir
			
			//System.out.println("\n...propiedadesPropuestaExclusion:"+	propiedadesPropuestaExclusion);
			propiedadesPropuestaExclusion.removeAll(propiedadesNoExcluir);
			//System.out.println("\n...clasesPropuestaExclusion:"+ clasesPropuestaExclusion);
			clasesPropuestaExclusion.removeAll(clasesNoExcluir);
			//System.out.println("\n...propiedades a eliminar: "+propiedadesPropuestaExclusion);
			//System.out.println("\n...clases a eliminar: "+clasesPropuestaExclusion);			

			if(exclusiones){
				this.excludeClasses(clasesPropuestaExclusion, true);
				this.excludeProperties(propiedadesPropuestaExclusion);
				this.excludePropertiesTrash();
				this.excludeClasesTrash();
			}
			System.out.println("..... Resumen todos los modulos que se han excluido:"+modulosAEXCLUIR);			
		}
		}
		
		
		return this.getJmodel();//el mismo modelo ya transformado
	}
	
	
	
	/**
	 * excluye las propiedades sin dominio y sin rango tras la eliminación de clases que no son útiles en la vista extraida
	 *
	 */private void excludePropertiesTrash(){
		HashSet <String> propstrash=new HashSet <String>();  
		Iterator itPr=this.getJmodel().getPropiedades().iterator();
		while(itPr.hasNext()){
			OWLProperty prop=(OWLProperty)itPr.next();
			//System.out.println(".. propiedad="+prop.getLocalName()+" rango="+prop.getRange());
			
			
			if(prop.getDomain(true)==null){
				//System.out.println("\n debug dominio de: "+prop.getLocalName()+"  ="+prop.getDomains(true));
			}
			else{
				String aux1=prop.getDomain(true).toString();				
				String aux2=prop.getDomain(false).toString();
				
			}
			//TODO ELIMINAR PROPIEDADES SIN DOMINIO,NO SE HA HECHO DIRECTAMENTE PQ PARECE SER QUE AL BORRAR LAS CLASES QUE FORMAN PARTE DE UN DOMINIO DE UNA PROPIEDAD, EL DOMINIO
			//QUE DEVUELVE EL METODO ES NULL A PESAR DE QUE INICIALMENTE HUBIESE OTRAS CLASES PERTENECENTES AL DOMINIO QUE NO SE HAN BORRADO: EJEMPLO "cliente","proveedor"
			
			
			//System.out.println("\n debug2 dominio de: "+prop.getLocalName()+"  ="+prop.getDomain());			
			if(prop.getRange()==null&&Constants.getIdConstantProp(prop.getLocalName())==null){
				//System.out.println("\n Se va a borrar la propiedad:"+prop.getLocalName()+" porque no tiene rango");
				propstrash.add(prop.getLocalName());
				prop.delete();
				
				//si se borra una propiedad también se borra la inversa
				if(prop.getInverseProperty()!=null){
						//System.out.println(".."+prop.getInverseProperty().getLocalName()+"  (se borra por inversa de una sin rango)");	
						propstrash.add(prop.getInverseProperty().getLocalName());
						prop.getInverseProperty().delete();
				}
			}
		}
		//System.out.println("\n\n ------------> EXCLUDEPROPERTIESTRASH elimino: "+propstrash+"\n (TODO FALTA IMPLEMENTAR ELIMINAR TB LAS PROPS QUE SE QUEDAN SIN DOMINIO TRAS LA TRANSFORMACION");
	}
	 
	 private void excludeClasesTrash(){
		 String nameClase,prop;
		  ArrayList<String> claseseliminadas=new ArrayList<String>(); 
		ArrayList<String> systemClass=new ArrayList<String>(Arrays.asList(Constants.LIST_SYSTEM_CLASS_NAME));
		
		for (Iterator it =this.getJmodel().getProtegeowlModel().getUserDefinedOWLNamedClasses().iterator(); it.hasNext();)
		{
			    	OWLNamedClass cls = (OWLNamedClass) it.next();
			    	nameClase=cls.getLocalName();
			    	if(nameClase.startsWith("Axiom_"))
			    		continue;
			     
			        Collection cPropiedades=cls.getAssociatedProperties();
			        for(Iterator itp=cPropiedades.iterator();itp.hasNext();)
			        {  
			        	LinkedList <RangeItem> lRangeItems = new LinkedList <RangeItem>();
			    		RDFProperty rdfProp=(RDFProperty)itp.next();
			        	String namePropiedad=rdfProp.getLocalName();
			        	
			        	//identificador entero de la propiedadee
			        	prop=namePropiedad;
			        	
			        	String tipoPropiedad=rdfProp.getRDFType().getLocalName();
			        	//System.out.println("[OWLParser]:_      -"+namePropiedad+" ("+prop+")  es: "+tipoPropiedad);
			        	
			        	//El rango que aplica para una propiedad en una clase es el definido con AllValuesFrom o el definido en P si no se hizo ninguna restriccion de ste tipo
			        	RDFResource rdfRng=cls.getAllValuesFrom(rdfProp);
			        	
			        	if (rdfRng==null&&!OWLAux.hasRestricctionHasValue(cls,rdfProp)){
			        		if(!systemClass.contains(nameClase)){
			        			//System.out.println("[ExtractOWLVIEW.excludeClasesTrash]:_       va a eliminar la clase: "+ nameClase+" por no tener rango en la propiedad:"+prop);
			        			this.getJmodel().getClase(nameClase).delete();
			        			claseseliminadas.add(nameClase);
			        		}
			        	}
			        }
			}
		//System.out.println("\n\n ------------> excludeClasesTrash elimino: "+ claseseliminadas);
	 }
		 
		 
		 
		 
		
	private void excludeClasses(HashSet<String> classes, boolean excludeSpecialized){
			//System.out.println("\n =============== excludeClasses:"+classes);
			Iterator it=classes.iterator();
        	while (it.hasNext()){
        		String claseAexcluir=(String)it.next();
        		if(this.jmodel.getClase(claseAexcluir)==null){
        			System.err.println(" WARNING: Mal definición de uno de las clases a excluir: "+claseAexcluir+"--> No corresponde a ninguna clase del modelo");
        		}
        		else{
        			this.getJmodel().getClase(claseAexcluir).delete();
        			if(excludeSpecialized){
	        			ArrayList<String> specializadas=this.jmodel.getClase(claseAexcluir).getSpecialized();
//	        			System.out.println("..... "+claseAexcluir+" (directa)");
	        			for(int m=0;m<specializadas.size();m++){
	        				specializadas.get(m);
	        				//System.out.println("..... "+specializadas.get(m)+" (especializada)");
	        				if(this.getJmodel().getClase(specializadas.get(m))!=null){
	        						this.getJmodel().getClase(specializadas.get(m)).delete();
	        				}
	        			}
        			}
        		}
    			this.getJmodel().getClase(claseAexcluir).delete();//eliminamos la directa
        	}
		}
		
		private void excludeProperties(HashSet<String> properties){
			//System.out.println("\n =============== excludeProperties:"+properties);
        	Iterator it2=properties.iterator();
	        	while (it2.hasNext()){
	        		String propiedadAexcluir=(String)it2.next();
	        		if(this.jmodel.getProperty(propiedadAexcluir)==null){
	        			System.err.println(" WARNING: Mal definición de la propiedad a excluir: "+propiedadAexcluir+" no corresponde a ninguna propiedad del modelo: ");
	        		}
	        		else{
	        			OWLProperty owlPropExc=this.jmodel.getProperty(propiedadAexcluir);
	        			owlPropExc.delete();
	        			if(owlPropExc.getInverseProperty()!=null){
							//System.out.println(".."+owlPropExc.getInverseProperty().getLocalName()+"  (SE BORRA POR INVERSA)");	
							owlPropExc.getInverseProperty().delete();
	        			}
	        		}
	        	}
        }
		
		
		
		public static void main(String args[]) throws Exception{
			String path="E:/DESARROLLO/ONTOLOGIA/"; 
			String nameFile="Modelo.owl";
			String uri = "file:///" + path + nameFile;
			JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
			ArrayList<String> modulosAIncluir=new ArrayList<String>();
			String namemodulos="";
			String[]modulos=namemodulos.split(";");
			for (int i=0;i<modulos.length;i++){
				modulosAIncluir.add(modulos[i]);
			}
			System.out.println("modulos="+modulosAIncluir);
			ExtractOWLView extractOwlView=new ExtractOWLView(new OWL(owlModel),modulosAIncluir,true);
			OWL modelView=extractOwlView.extract(false);
			modelView.saveInFile(path, "ERPmodulos_"+modulosAIncluir.toString()+".owl");
			
			/*String a="12345678";
			System.out.println("Primer caracter= "+a.charAt(0)+" length="+a.length()+"  a.substring(0,5)="+a.substring(0,5)+"  a.substring(5,7)="+a.substring(5,7)+"  a.substring(7,8)="+a.substring(7,8));
			*/
		
	}

	public OWL getJmodel() {
		return jmodel;
	}


	public ArrayList<String> getModulos() {
		return modulos;
	}


	public boolean isIncludeModules() {
		return includeModules;
	}

}
