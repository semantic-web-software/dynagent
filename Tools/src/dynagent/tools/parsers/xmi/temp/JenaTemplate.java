package dynagent.tools.parsers.xmi.temp;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/***
 * JenaTemplate.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */
public class JenaTemplate extends dynagent.tools.parsers.xmi.Template {

	public void run(){
		
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);

		String prefijoMeta = "http://dynagent/meta#";
		String prefijoRol = "http://dynagent/rol#";
		String prefijoProcess = "http://dynagent/process#";
		String prefijoProp = "http://dynagent/properties#";		
		m.setNsPrefix("",prefijoMeta);
		

		m.createClass(prefijoMeta+"Persona");
		m.createClass(prefijoMeta+"PersonaFisica");
		m.createClass(prefijoMeta+"Producto");
		m.createClass(prefijoMeta+"Dependencia");
		m.createClass(prefijoRol+"Proveedor");
		m.createClass(prefijoRol+"Cliente");
		m.createClass(prefijoRol+"ArticuloMedible");
		m.createClass(prefijoRol+"ArticuloManufacturable");
		m.createClass(prefijoRol+"ProductoGenerado");
		m.createClass(prefijoRol+"LugarRecepcion");
		m.createClass(prefijoRol+"LugarEntrega");
		m.createClass(prefijoRol+"Gestor");
		m.createClass(prefijoRol+"Articulo");
		m.createClass(prefijoRol+"Lugar");
		m.createClass(prefijoProcess+"GestionComercial");
		
		m.getOntClass(prefijoMeta+"Persona").addSubClass(m.getOntClass(prefijoMeta+"PersonaFisica"));
		m.getOntClass(prefijoRol+"Gestor").addSubClass(m.getOntClass(prefijoRol+"Proveedor"));
		m.getOntClass(prefijoRol+"Articulo").addSubClass(m.getOntClass(prefijoRol+"ArticuloMedible"));
		m.getOntClass(prefijoRol+"Articulo").addSubClass(m.getOntClass(prefijoRol+"ArticuloManufacturable"));
		m.getOntClass(prefijoRol+"Lugar").addSubClass(m.getOntClass(prefijoRol+"LugarRecepcion"));
		m.getOntClass(prefijoRol+"Lugar").addSubClass(m.getOntClass(prefijoRol+"LugarEntrega"));
		
		m.createOntProperty(prefijoProp+"playProveedor");
		m.getOntProperty(prefijoProp+"playProveedor").addDomain(m.getOntClass(prefijoRol+"Proveedor"));
		m.getOntProperty(prefijoProp+"playProveedor").addRange(m.getOntClass(prefijoMeta+"PersonaFisica"));
		m.createOntProperty(prefijoProp+"playCliente");
		m.getOntProperty(prefijoProp+"playCliente").addDomain(m.getOntClass(prefijoRol+"Cliente"));
		m.getOntProperty(prefijoProp+"playCliente").addRange(m.getOntClass(prefijoMeta+"PersonaFisica"));
		m.createOntProperty(prefijoProp+"playArticuloMedible");
		m.getOntProperty(prefijoProp+"playArticuloMedible").addDomain(m.getOntClass(prefijoRol+"ArticuloMedible"));
		m.getOntProperty(prefijoProp+"playArticuloMedible").addRange(m.getOntClass(prefijoMeta+"Producto"));
		m.createOntProperty(prefijoProp+"playArticuloManufacturable");
		m.getOntProperty(prefijoProp+"playArticuloManufacturable").addDomain(m.getOntClass(prefijoRol+"ArticuloManufacturable"));
		m.getOntProperty(prefijoProp+"playArticuloManufacturable").addRange(m.getOntClass(prefijoMeta+"Producto"));
		m.createOntProperty(prefijoProp+"playProductoGenerado");
		m.getOntProperty(prefijoProp+"playProductoGenerado").addDomain(m.getOntClass(prefijoRol+"ProductoGenerado"));
		m.getOntProperty(prefijoProp+"playProductoGenerado").addRange(m.getOntClass(prefijoMeta+"Producto"));
		m.createOntProperty(prefijoProp+"playLugarRecepcion");
		m.getOntProperty(prefijoProp+"playLugarRecepcion").addDomain(m.getOntClass(prefijoRol+"LugarRecepcion"));
		m.getOntProperty(prefijoProp+"playLugarRecepcion").addRange(m.getOntClass(prefijoMeta+"Dependencia"));
		m.createOntProperty(prefijoProp+"playLugarEntrega");
		m.getOntProperty(prefijoProp+"playLugarEntrega").addDomain(m.getOntClass(prefijoRol+"LugarEntrega"));
		m.getOntProperty(prefijoProp+"playLugarEntrega").addRange(m.getOntClass(prefijoMeta+"Dependencia"));
		m.createOntProperty(prefijoProp+"players");
		m.getOntProperty(prefijoProp+"players").addDomain(m.getOntClass(prefijoProcess+"GestionComercial"));
		m.getOntProperty(prefijoProp+"players").addRange(m.getOntClass(prefijoRol+"Gestor"));
		m.createOntProperty(prefijoProp+"players");
		m.getOntProperty(prefijoProp+"players").addDomain(m.getOntClass(prefijoProcess+"GestionComercial"));
		m.getOntProperty(prefijoProp+"players").addRange(m.getOntClass(prefijoRol+"Articulo"));
		m.createOntProperty(prefijoProp+"players");
		m.getOntProperty(prefijoProp+"players").addDomain(m.getOntClass(prefijoProcess+"GestionComercial"));
		m.getOntProperty(prefijoProp+"players").addRange(m.getOntClass(prefijoRol+"Lugar"));
		m.createOntProperty(prefijoProp+"players");
		m.getOntProperty(prefijoProp+"players").addDomain(m.getOntClass(prefijoProcess+"GestionComercial"));
		m.getOntProperty(prefijoProp+"players").addRange(m.getOntClass(prefijoRol+"Cliente"));
		m.createOntProperty(prefijoProp+"players");
		m.getOntProperty(prefijoProp+"players").addDomain(m.getOntClass(prefijoProcess+"GestionComercial"));
		m.getOntProperty(prefijoProp+"players").addRange(m.getOntClass(prefijoRol+"ProductoGenerado"));
		
		m.write(System.out, "RDF/XML-ABBREV");
		try{
			FileWriter fs = new FileWriter("file:///E:/DESARROLLO/Workspace/Ildefonso/RuleEngine/dynagent/ruleengine/example/protege-ejemplo.owl");
			m.write(fs, "RDF/XML-ABBREV");
		}catch(IOException ex){}
	}
}