package dynagent.tools.parsers.access;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;


public class Acceso {
	
	private String utask;
	private ArrayList<Permiso> accesos = new ArrayList<Permiso>();
	private String usuario;
	private ArrayList<String> rolUsuario = new ArrayList<String>();
	private ArrayList<String> clases = new ArrayList<String>();
	private String rol;
	private String individuo;
	private String propiedad;
	private String valor;
	private String rango;
	private String relacion;
	private String individualRelacion;
	private String rolB;
	private String ambito;
	
	public ArrayList<Permiso> getAccesos() {
		return accesos;
	}
	public void setAccesos(ArrayList<Permiso> accesos) {
		this.accesos = accesos;
	}
	
	public ArrayList<String> getClases() {
		return clases;
	}
	public void setClases(ArrayList<String> clases) {
		this.clases = clases;
	}
	public String getIndividualRelacion() {
		return individualRelacion;
	}
	public void setIndividualRelacion(String individualRelacion) {
		this.individualRelacion = individualRelacion;
	}
	public String getIndividuo() {
		return individuo;
	}
	public void setIndividuo(String individuo) {
		this.individuo = individuo;
	}
	public String getPropiedad() {
		return propiedad;
	}
	public void setPropiedad(String propiedad) {
		this.propiedad = propiedad;
	}
	public String getRango() {
		return rango;
	}
	public void setRango(String rango) {
		this.rango = rango;
	}
	public String getRelacion() {
		return relacion;
	}
	public void setRelacion(String relacion) {
		this.relacion = relacion;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String rol) {
		this.rol = rol;
	}
	public String getRolB() {
		return rolB;
	}
	public void setRolB(String rolB) {
		this.rolB = rolB;
	}
	
	public ArrayList<String> getRolUsuario() {
		return rolUsuario;
	}
	public void setRolUsuario(ArrayList<String> rolUsuario) {
		this.rolUsuario = rolUsuario;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getUtask() {
		return utask;
	}
	public void setUtask(String utask) {
		this.utask = utask;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	
	public String getAmbito() {
		return ambito;
	}
	public void setAmbito(String ambito) {
		this.ambito = ambito;
	}
	
	
	public void addPermiso(Permiso p){
		if(p!=null && Constants.isGoodDefinedAccess(p.getPermiso()))
			accesos.add(p);
		else
			System.out.println("El permiso "+p.getPermiso()+" no corresponde con ninguno de los predefinidos. No se insertará");
		
	}
	
	public void addRolUsuario(String rol){
		rolUsuario.add(rol);
	}
	
	public void addClase(String clase){
		clases.add(clase);
	}
	
	public boolean checkIsWellDefinedAccess(HashMap<String,Integer> classes, HashMap<String,Integer> individuals, HashMap<String,Integer> properties){
		
		boolean isWellDefined=true;
		Integer intClase;
		Integer intUser;
		Integer intRolUser;
		Integer intIndividuo;
		Integer intProp;
		Integer intValue;
		Integer intRango;
		Integer intRelacion;
		Integer intIndRelacion;
		Integer intRol;
		Integer intRolB;
		Integer intUtask;
		
		
		
		if(this.clases!=null){
			intClase = classes.get(this.clases.get(0));	
			if(intClase==null){
				System.out.println("Ha intentado referenciar una clase "+this.clases.get(0)+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.individualRelacion!=null){
			intIndRelacion = individuals.get(this.individualRelacion);	
			if(intIndRelacion==null){
				System.out.println("Ha intentado referenciar un individuo de relacion "+this.individualRelacion+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.individuo!=null){
			intIndividuo = individuals.get(this.individuo);	
			if(intIndividuo==null){
				System.out.println("Ha intentado referenciar un individuo "+this.individuo+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.propiedad!=null){
			intProp = properties.get(this.propiedad);	
			if(intProp==null){
				System.out.println("Ha intentado referenciar una propiedad "+this.propiedad+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.rango!=null){
			intRango = classes.get(this.rango);	
			if(intRango==null){
				System.out.println("Ha intentado referenciar un rango "+this.rango+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.relacion!=null){
			intRelacion = classes.get(this.relacion);	
			if(intRelacion==null){
				System.out.println("Ha intentado referenciar una relacion "+this.relacion+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.rol!=null){
			intRol = classes.get(this.rol);	
			if(intRol==null){
				System.out.println("Ha intentado referenciar un rol "+this.rol+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		if(this.rolB!=null){
			intRolB = classes.get(this.rolB);	
			if(intRolB==null){
				System.out.println("Ha intentado referenciar un rolB "+this.rolB+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.rolUsuario!=null){
			intRolUser = individuals.get(this.rol);	
			if(intRolUser==null){
				System.out.println("Ha intentado referenciar un rolUser "+this.rolUsuario+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		
		// TODO Comprobar que el usuario es correcto
		
		if(this.utask!=null){
			intUtask = classes.get(this.rol);	
			if(intUtask==null){
				System.out.println("Ha intentado referenciar una user task "+this.utask+" que no existe. El acceso no se insertará");
				isWellDefined=false;
			}
		}
		
		if(this.valor!=null){
			intValue = individuals.get(this.rol);
			if(intValue==null){
				System.out.println("No existe un ido para ese individuo. Suponemos que es una data property de tipo String y ahí estará el valor. Si no es correcto, avíselo.");
			}
		}
		
		
		return isWellDefined;
		
	
	}
	
	public ArrayList<AccessString> translatetoAccessStringFromAcceso(){
		ArrayList<AccessString> accStrings = new ArrayList<AccessString>();
		Iterator it =this.accesos.iterator();
		while(it.hasNext()){
			Permiso permiso = (Permiso) it.next();
			Iterator it2 = rolUsuario.iterator();
			while(it2.hasNext()){
				Iterator it3 = clases.iterator();
				String rolUsuario = (String) it2.next();
				while(it3.hasNext()){
					String clase = (String) it3.next();
					AccessString accString = new AccessString();
					accString.setACCESSTYPE(permiso.getPermiso());
					accString.setCLSREL(this.relacion);
					accString.setIDO(this.individuo);
					accString.setIDOREL(this.individualRelacion);
					accString.setPROP(this.propiedad);
					accString.setROL(this.rol);
					accString.setROLB(this.rolB);
					accString.setTASK(this.utask);
					accString.setUSER(this.usuario);
					accString.setVALUE(this.valor);
					accString.setVALUECLS(this.rango);
					accString.setIDTO(clase);
					accString.setUSERROL(rolUsuario);
					if(permiso.getIsDenegado())
						accString.setDENNIED("dennied");
					else
						accString.setDENNIED("allowed");
					accStrings.add(accString);
				}
			}
			
		}
		return accStrings;
	}
	
	public String toString(){
		
		String res = "UTASK: ";
		if(utask==null)
			res+="TODAS";
		else
			res+=utask;
		res += ", Ambito "+ambito;
		res += ", Accesos: "+accesos;
		
		if(usuario!=null)
			res+=", User: "+usuario;
		if(rolUsuario!=null)
			res+=", User Rol: "+rolUsuario;
		if(clases!=null)
			res+=", Clase: "+clases;
		if(rol!=null)
			res+=", Rol: "+rol;
		if(individuo!=null)
			res+=", Individuo: "+individuo;
		if(propiedad!=null)
			res+=", Propiedad: "+propiedad;
		if(valor!=null)
			res+=", Valor: "+valor;
		if(rango!=null)
			res+=", Rango: "+rango;
		if(relacion!=null)
			res+=", Relacion:" +relacion;
		if(individualRelacion!=null)
			res+=", Individuo Relacion: "+individualRelacion;
		if(rolB!=null)
			res+=", RolB: "+rolB;

		
		return res;
		
	}
	
	public boolean isWellDefined(){
		boolean res;
		if(this.ambito.equals("propiedad"))
			res=this.isWellDefinedProp();
		else
			res=this.isWellDefinedObj();
		return res;
	}
	private boolean isWellDefinedObj() {
		Iterator it = accesos.iterator();
		boolean res = true;
		while(it.hasNext()){
			Permiso p = (Permiso) it.next();
			res = res & Constants.isObjectAccess(p.getPermiso());
			if(!Constants.isObjectAccess(p.getPermiso()))
				System.out.println("ERROR: "+p.getPermiso());
		}
		return res;
	}
	
	private boolean isWellDefinedProp() {
		boolean res = true;
		Iterator it = accesos.iterator();
		ArrayList<Permiso> permisosIncorrectos = new ArrayList<Permiso>();
		while(it.hasNext()){
			Permiso p = (Permiso) it.next();
			if(!Constants.isPropertyAccess(p.getPermiso())){
				System.out.println("El permiso "+p.getPermiso()+" no es coherente con el ambito Propiedad, no se insertará");
				permisosIncorrectos.add(p);
			}
		}

		accesos.removeAll(permisosIncorrectos);
		if(accesos.size()==0){
			System.out.println("Todos los permisos eran incorrectos en la utask: "+utask);
			res = false;
		}
		
		if((this.clases==null || this.clases.contains("TODAS")) && this.individuo!=null){
			System.out.println("Siempre que indicamos un individuo debemos indicar la clase a la que pertenece");
			res = false;
		}
		
		if(this.relacion!= null && (this.rol==null || this.propiedad ==null)){
			System.out.println("Siempre que indicamos una relacion deberemos indicar el rol y la propiedad");
			res = false;
		}
		if(this.rol!= null && (this.relacion==null || this.propiedad ==null)){
			System.out.println("Siempre que indicamos un rol deberemos indicar la relacion y la propiedad");
			res = false;
		}
		return res;
	}
	
	
	

}
