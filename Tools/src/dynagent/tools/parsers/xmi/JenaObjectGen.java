/***
 * JenaObjectGen.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.tools.parsers.xmi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

public class JenaObjectGen {
	
	private LinkedList<Object> classes = new LinkedList<Object>();
	private LinkedList<Object> roles = new LinkedList<Object>();
	private LinkedList<Object> processes = new LinkedList<Object>();
	private LinkedList<Object> subclasses = new LinkedList<Object>();
	private LinkedList<Object> properties = new LinkedList<Object>();
	
	public LinkedList<Object> getClasses() {
		return classes;
	}

	public void setClasses(LinkedList<Object> classes) {
		this.classes = classes;
	}

	public LinkedList<Object> getProcesses() {
		return processes;
	}

	public void setProcesses(LinkedList<Object> processes) {
		this.processes = processes;
	}

	public LinkedList<Object> getProperties() {
		return properties;
	}

	public void setProperties(LinkedList<Object> properties) {
		this.properties = properties;
	}

	public LinkedList<Object> getRoles() {
		return roles;
	}

	public void setRoles(LinkedList<Object> roles) {
		this.roles = roles;
	}

	public LinkedList<Object> getSubclasses() {
		return subclasses;
	}

	public void setSubclasses(LinkedList<Object> subclasses) {
		this.subclasses = subclasses;
	}
	
	public JClass createClass(){ return new JClass(); }
	public Subclass createSubclass() { return new Subclass(); }
	public Property createProperty() { return new Property(); }
	
	public void generate(){
		String PROPERTIES_FILE = "ruleengine.properties";
		FileInputStream fis;
		try {
			fis = new FileInputStream(PROPERTIES_FILE);
			Properties properties = new Properties();
			properties.load(fis);
			fis.close();
			// TODO: Crear en carpetas en /temp que tengan asociado la fecha.
			// Date d = new Date();
			// SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHmmss");
			String javafile = properties.getProperty("JENATEMPLATEPATH");
			BufferedReader bf = new BufferedReader(new FileReader(properties.getProperty("JENATEMPLATE")));
			FileWriter fw = new FileWriter(javafile);
			String line = "";
			while((line = bf.readLine()) != null){
				fw.append(line + "\n");
			}
			fw.append(generateCodeClass()+generateCodeSubclass()+generateCodeProperty());
			fw.append("\n\t\tm.write(System.out, \"RDF/XML-ABBREV\");");
			fw.append("\n\t\ttry{");
			fw.append("\n\t\t\tFileWriter fs = new FileWriter(\""+properties.getProperty("OWLMODEL")+"\");");
			fw.append("\n\t\t\tm.write(fs, \"RDF/XML-ABBREV\");");
			fw.append("\n\t\t}catch(IOException ex){}");
			fw.append("\n\t}\n}");
			fw.close();
			ITemplate t = new dynagent.tools.parsers.xmi.temp.JenaTemplate();
			t.run();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String generateCodeClass(){
		
		String code = "\n\t\t";
		
		Iterator it = this.getClasses().iterator();
		while(it.hasNext()){
			JClass j = (JClass)it.next();
			if(j.getType().equals("owl")){
				code += "m.createClass(prefijoMeta+\""+j.getName()+"\");";
			}else if(j.getType().equals("role")){
				code += "m.createClass(prefijoRol+\""+j.getName()+"\");";
			}else if(j.getType().equals("process")){
				code += "m.createClass(prefijoProcess+\""+j.getName()+"\");";
			}
			code += "\n\t\t";
		}
		it = this.getRoles().iterator();
		while(it.hasNext()){
			JClass j = (JClass)it.next();
			if(j.getType().equals("owl")){
				code += "m.createClass(prefijoMeta+\""+j.getName()+"\");";
			}else if(j.getType().equals("role")){
				code += "m.createClass(prefijoRol+\""+j.getName()+"\");";
			}else if(j.getType().equals("process")){
				code += "m.createClass(prefijoProcess+\""+j.getName()+"\");";
			}
			code += "\n\t\t";
		}
		it = this.getProcesses().iterator();
		while(it.hasNext()){
			JClass j = (JClass)it.next();
			if(j.getType().equals("owl")){
				code += "m.createClass(prefijoMeta+\""+j.getName()+"\");";
			}else if(j.getType().equals("role")){
				code += "m.createClass(prefijoRol+\""+j.getName()+"\");";
			}else if(j.getType().equals("process")){
				code += "m.createClass(prefijoProcess+\""+j.getName()+"\");";
			}
			code += "\n\t\t";
		}		
		return code;
	}
	
	public String generateCodeSubclass(){
		
		String code = "\n\t\t";
		
		Iterator it = this.getSubclasses().iterator();
		while(it.hasNext()){
			Subclass s = (Subclass)it.next();
			JClass jparent = findByName(s.getParent());
			JClass jchild = findByName(s.getChild());
			if(jparent == null){
				System.out.println("[JENAOBJGEN][ERROR] Parent class not found: "+ s.getParent());
				break;
			}else if(jchild == null){
				System.out.println("[JENAOBJGEN][ERROR] Child class not found: "+ s.getChild());
				break;
			}else{
				code += "m.getOntClass("+jparent.getPrefix()+"+\""+s.getParent()+"\").addSubClass(m.getOntClass("+jchild.getPrefix()+"+\""+s.getChild()+"\"));\n\t\t";
			}			
		}
		return code;
	}
	
	public String generateCodeProperty(){
	
		String code = "\n\t\t";
		
		Iterator it = this.getProperties().iterator();
		while(it.hasNext()){
			Property p = (Property)it.next();
			if(!((JClass)findByName(p.getDomain())).getPrefix().equals("prefijoProcess")){
				code +="m.createOntProperty(prefijoProp+\"play"+p.getDomain()+"\");\n\t\t";
				code +="m.getOntProperty(prefijoProp+\"play"+p.getDomain()+"\").addDomain(m.getOntClass("+((JClass)findByName(p.getDomain())).getPrefix()+"+\""+p.getDomain()+"\"));\n\t\t";
				code +="m.getOntProperty(prefijoProp+\"play"+p.getDomain()+"\").addRange(m.getOntClass("+((JClass)findByName(p.getRange())).getPrefix()+"+\""+p.getRange()+"\"));\n\t\t";
			}else{
				code +="m.createOntProperty(prefijoProp+\"players\");\n\t\t";
				code +="m.getOntProperty(prefijoProp+\"players\").addDomain(m.getOntClass("+((JClass)findByName(p.getDomain())).getPrefix()+"+\""+p.getDomain()+"\"));\n\t\t";
				code +="m.getOntProperty(prefijoProp+\"players\").addRange(m.getOntClass("+((JClass)findByName(p.getRange())).getPrefix()+"+\""+p.getRange()+"\"));\n\t\t";
			}
			
		}
		return code;	
	}
	
	public JClass findClassByName(String name){
		JClass j = null;
		Iterator it = this.getClasses().iterator();
		boolean flag = false;
		while(it.hasNext() && flag == false){
			j = (JClass)it.next();
			if(j.getName().equals(name)){
				flag = true;
			}
		}
		if(flag == false)
			j = null;
		return j;	
	}
	
	public JClass findByName(String name){
		JClass j = null;
		if(findClassByName(name) == null){
			j = null;
			Iterator it = this.getRoles().iterator();
			boolean flag = false;
			while(it.hasNext() && flag == false){
				j = (JClass)it.next();
				if(j.getName().equals(name)){
					flag = true;
				}
			}
			if(flag == false){
				j = null;
				Iterator itj = this.getProcesses().iterator();
				flag = false;
				while(itj.hasNext() && flag == false){
					j = (JClass)itj.next();
					if(j.getName().equals(name)){
						flag = true;
					}
				}
				if(flag == false)
					j = null;
			}
		}else{
			j = findClassByName(name);
		}
		return j;
	}
	
}
