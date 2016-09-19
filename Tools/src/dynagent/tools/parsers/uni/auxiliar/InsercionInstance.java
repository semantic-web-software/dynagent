package dynagent.tools.parsers.uni.auxiliar;


import java.util.ArrayList;
import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;
import dynagent.tools.parsers.uni.Principal2;


/**
 * 
 * @author alvarez
 *  Clase auxiliar para crear objetos de tipo Instance
 *  que serán añadidos a la tabla del mismo nombre
 *  en la base de datos
 */
public class InsercionInstance {

	private ArrayList tablaInstances = new ArrayList();


	/**
	 * Método get del atributo tablaInstances
	 * @return ArrayList : El atributo tablaInstances
	 */
	public ArrayList getTablaInstances() {
		return tablaInstances;
	}


	/**
	 * Método set del atributo tablaInstances
	 * @param tablaInstances
	 */
	public void setTablaInstances(ArrayList tablaInstances) {
		this.tablaInstances = tablaInstances;
	}

	/**
	 * Insertar un objeto Instance en el atributo tablaInstances
	 * @param i Instancia a añadir en la lista
	 */

	public void addInstance(Instance i){
		tablaInstances.add(i);
	}


	/**
	 * Crea todos los registros necesarios para ser insertados en la tabla instances dada la propiedad de un atributo
	 * @param c Clase cuyo idto vamos a insertar
	 * @param lpbd Registros de la tabla properties que identifican a la misma propiedad
	 * @param pat Propiedad a la que se refiere la lista lpbd
	 * @param lc Lista de clases que vamos a insertar
	 * @param li Lista de individuos declarados en el documento
	 * @return ArrayList: Lista de instancias asociadas a esa propiedad
	 */

	public ArrayList<Instance> creaInstanceClase(Clase c, ArrayList<PropiedadBD> lpbd, PropiedadAtrib pat, ArrayList<Clase> lc, ArrayList<Individuo> li){

		

		ArrayList<Instance> res = new ArrayList();
		Instance cardi = new Instance();
		PropiedadBD pbd = (PropiedadBD) lpbd.get(0);
		cardi.setIDTO(new Integer(c.getIdto()).toString());
		cardi.setOP(Constants.OP_CARDINALITY);
		cardi.setPROPERTY(new Integer(pbd.getIdProp()).toString());
		if(pat.getQMin()!= - 1)
			cardi.setQMIN(new Integer(pat.getQMin()).toString());
		else 
			if(pbd.getQMin() != -1)
				cardi.setQMIN(new Integer(pbd.getQMin()).toString());

		if(pat.getQMax()!= - 1)
			cardi.setQMAX(new Integer(pat.getQMax()).toString());
		else 
			if(pbd.getQMax() != -1)
				cardi.setQMAX(new Integer(pbd.getQMax()).toString());



		res.add(cardi);

		//System.out.println(cardi);

		if(pat.getRestricciones().size()>0){
			Iterator itr = pat.getRestricciones().iterator();
			while(itr.hasNext()){
				String str = (String) itr.next();
				int idto = Principal2.buscaIdto(lc, str);
				Instance i = new Instance();
				i.setIDTO(new Integer(c.getIdto()).toString());
				i.setPROPERTY(new Integer(pbd.getIdProp()).toString());
				if(idto>=0)
					i.setVALUECLS(new Integer(idto).toString());
				else
					i.setVALUECLS(null);
				if(pat.getOp()!=null)
					i.setOP(pat.getOp());
				else
					pat.setOp("AND");
				//System.out.println(i);
				res.add(i);
			}

		}
		else if(pat.getEnumerados().size()>0){
			Iterator itr = pat.getEnumerados().iterator();
			while(itr.hasNext()){
				String tipo="";
				String str = (String) itr.next();
				Individuo ind = Principal2.buscaIndividuo(li, str);
				if(ind.getTipo()!= null)
					tipo = ind.getTipo();
				int idto = Principal2.buscaIdto(lc, tipo);
				Instance i = new Instance();
				i.setIDTO(new Integer(c.getIdto()).toString());
				i.setPROPERTY(new Integer(pbd.getIdProp()).toString());
				if(idto>=0)
					i.setVALUECLS(new Integer(idto).toString());
				else
					i.setVALUECLS(null);
				int idIndividuo = Principal2.buscaIdto(lc, str);
				if(idIndividuo>0)
					i.setVALUE(new Integer(idIndividuo).toString());
				else
					i.setVALUE(str);
				if(pat.getEnumerados().size()>1)
					i.setOP("OR");
				else
					i.setOP("AND");
				//System.out.println(i);
				res.add(i);
			}

		}


		else
		{
			Iterator it = lpbd.iterator();
			while(it.hasNext()){
				PropiedadBD pbd2 = (PropiedadBD) it.next();
				Instance i = new Instance();
				i.setIDTO(new Integer(c.getIdto()).toString());
				i.setPROPERTY(new Integer(pbd2.getIdProp()).toString());
				String op="AND";
				if(lpbd.size()>1)
					op="OR";	
				i.setOP(op);
				i.setVALUE(pbd2.getValue());
				i.setVALUECLS(new Integer(pbd2.getCls()).toString());
				//System.out.println(i);
				res.add(i);
			}


		}

		return res;



	}

	/**
	 * Crea dos instances que reflejaran el hecho de que una clase tiene un rol pointer como propiedad. 
	 * Una de rango y otra de cardinalidad
	 * @param idto Idto de la clase que tiene la propiedad rol pointer
	 * @param idProp Id de la propiedad rol pointer
	 * @param idRolb Idto del rol que pertenece al rol pointer
	 * @param idRelb Idto de la relacion que pertenece al rol pointer
	 * @param qMin cardinalidad mínima
	 * @param qMax cardinalidad máxima
	 * @return
	 */

	public ArrayList<Instance> createInstanceClaseRolPointer(int idto, int idProp, int idRolb, int idRelb, int qMin, int qMax){
		ArrayList<Instance> instances = new ArrayList();
		Instance cardi = new Instance();
		cardi.setIDTO(new Integer(idto).toString());
		cardi.setOP(Constants.OP_CARDINALITY);
		if(qMax != -1)
			cardi.setQMAX(new Integer(qMax).toString());
		if(qMin != -1)
			cardi.setQMIN(new Integer(qMin).toString());
		cardi.setPROPERTY(new Integer(idProp).toString());
		instances.add(cardi);
		System.out.println(cardi);
		Instance ins = new Instance();
		ins.setIDTO(new Integer(idto).toString());
		ins.setOP(Constants.OP_INTERSECTION);
		ins.setPROPERTY(new Integer(idProp).toString());
		ins.setVALUECLS(new Integer(idRelb).toString());
		instances.add(ins);
		System.out.println(ins);

		return instances;
	}

	/**
	 * Crea un objeto Instance que expresa una relacion, los roles y las clases que la juegan
	 * @param idtrelacion : idto de la clase relacion
	 * @param idtproperty : idto de la propiedad que pertenece a la relacion
	 * @param idtclase : idto de la clase que permite al rol jugar la relacion
	 * @param idtrol : idto del rol que juega la relacion
	 * @param op : operador para comprobar cuantas clases juegan la relacion
	 * @return Instance: instance que expresa la relacion
	 */


	public Instance createInstanceRelacion(int idtrelacion, int idtproperty, int idtclase, int idtrol, String op, boolean isFull){

		Instance i = new Instance();
		i.setIDTO(new Integer(idtrelacion).toString());
		i.setPROPERTY(new Integer(idtproperty).toString());
		i.setVALUECLS(new Integer(idtclase).toString());
		i.setOP(op);
		if(isFull)
			i.setVALUE(i.getVALUECLS());
		return i;

	}


	/**
	 * Igual que {@link #createInstanceRelacion(int, int, int, int, String)} pero para expresar la cardinalidad
	 * @param idtrelacion : idto de la clase relacion
	 * @param idtproperty : idto de la propiedad que pertenece a la relacion
	 * @param idtrol : idto del rol que juega la relacion
	 * @param qMax : cardinalidad máxima
	 * @param qMin : cardinalidad mínima
	 * @return la Instancia de la relacion que expresa la cardinalidad
	 */




	public Instance createInstanceRelacion(int idtrelacion, int idtproperty, int idtrol, int qMax, int qMin){
		Instance i = new Instance();
		if(qMax != -1)
			i.setQMAX(new Integer(qMax).toString());
		if(qMin != -1)
			i.setQMIN(new Integer(qMin).toString());
		i.setIDTO(new Integer(idtrelacion).toString());
		i.setPROPERTY(new Integer(idtproperty).toString());
		i.setOP(Constants.OP_CARDINALITY);
		return i;

	}


	/**
	 * Crea dos instancias relacionedas con la propiedad 'playxINV' de cada clase que juega un rol en una relacion.
	 * Una de las instancias indica la cardinalidad y la otra el tango de dicha propiedad.
	 * 
	 * @param idtoClase : IDTO de la clase que tiene la propiedad
	 * @param idtoRol : IDTO del rol que dicha clase juega
	 * @param idProp : Identificador de la propiedad playxINV
	 * @param idtRel : IDTO de la relacion en la que la clase juega el rol
	 * @param qMin : Cardinalidad minima 
	 * @param qMax : Cardinalidad maxima
	 * @return Las 2 instancias en un ArrayList
	 */
	
	public ArrayList<Instance> createInstancePlayIn(int idtoClase, int idtoRol, int idProp,int idtRel, int qMin, int qMax){
		/*ArrayList<Instance> res = new ArrayList<Instance>();
		Instance ins = new Instance();
		ins.setIDTO(new Integer(idtoClase).toString());
		ins.setROL(new Integer(idtoRol).toString());
		ins.setPROPERTY(new Integer(idProp).toString());
		ins.setOP(Constants.OP_CARDINALITY);
		if(qMax!= -1)
			ins.setQMAX(new Integer(qMax).toString());
		if(qMin != -1)
			ins.setQMIN(new Integer(qMin).toString());
		ins.setVALUECLS(new Integer(idtRel).toString());
		ins.setCLSREL(new Integer(idtRel).toString());
		res.add(ins);*/
		
		ArrayList<Instance> res = new ArrayList<Instance>();
		res.add(createInstancePlayInCar(idtoClase, idtoRol, idProp, idtRel, qMin, qMax));
		
		Instance ins2 = new Instance();
		ins2.setVALUECLS(new Integer(idtRel).toString());
		ins2.setIDTO(new Integer(idtoClase).toString());
		ins2.setPROPERTY(new Integer(idProp).toString());
		ins2.setOP(Constants.OP_INTERSECTION);
		res.add(ins2);
		
		return res;
	}

	
	/**
	 * 
	 * @param idtRol : Rol del que vamos a indicar su peer
	 * @param idtRolb : Peer del rol
	 * @param rel : Relacion en la que el Rol tiene a RolB como a peer
	 * @return
	 */

	public Instance createInstancePeer(int idtRol, int idtRolb, int rel){
		Instance ins = new Instance();
		//ins.setPROPERTY(new Integer(Constants.IdPROP_PEER).toString());
	

		return ins;
	}

	/**
	 * Busca los registros de la tabla properties con el mismo nombre que el que se pasa por parámetro
	 * @param l Lista de propiedades 
	 * @param nombreProp Nombre de la propiedad que se quiere buscar
	 * @return ArrayList : Las propiedades que cumplen lo expresado anteriormente
	 */
	public ArrayList<PropiedadBD> buscaPropiedadBD (ArrayList<PropiedadBD> l, String nombreProp){

		ArrayList res = new ArrayList();
		//PropiedadBD res = null;
		boolean enc = false;
		Iterator it = l.iterator();
		while(it.hasNext()){
			PropiedadBD pbd = (PropiedadBD) it.next();
			if(nombreProp.equals(pbd.getName())){
				res.add(pbd);
			}
		}

		return res;
	}

	/**
	 * Busca las propiedades que posee la clase que se pasa como parámetro
	 * @param l Lista de propiedades
	 * @param claseCont La clase cuyas propiedades queremos conocer
	 * @return Las propiedades que cumplen lo anterior
	 */


	public ArrayList<PropiedadAtrib> buscaPropiedadesAtrib(ArrayList <PropiedadAtrib> l, String claseCont){

		ArrayList<PropiedadAtrib> res = new ArrayList<PropiedadAtrib>();
		Iterator it = l.iterator();
		while(it.hasNext()){
			PropiedadAtrib pat = (PropiedadAtrib) it.next();
			if(claseCont.equals(pat.getClaseCont()))
				res.add(pat);
		}

		return res;
	}


	/**
	 * Crea una instancia correspondiente a un individuo
	 * @param idto Idto de la clase del individuo
	 * @param ido Idto del individuo
	 * @param value Valor de la propiedad
	 * @param valueCls Valor de la clase de la propiedad
	 * @return
	 */

	public Instance createInstanceIndividuo(int idto, int ido, String value, int valueCls, int idProp, int qMin, int qMax){
		Instance ins = new Instance();
		ins.setIDTO(new Integer(idto).toString());
		ins.setIDO(new Integer(ido).toString());
		if(idProp==2)
			ins.setVALUE(value);
		ins.setNAME(value);
		ins.setVALUECLS(new Integer(valueCls).toString());
		ins.setPROPERTY(new Integer(idProp).toString());
		if(qMin!= -1)
			ins.setQMIN(new Integer(qMin).toString());
		if(qMax != -1)
			ins.setQMAX(new Integer(qMax).toString());

		return ins;
	}

	/**
	 * Crea una instancia de cardinalidad playxInv
	 * @param idto: IDTO de la clase que tiene la propiedad
	 * @param idtRol : IDTO del rol que dicha clase juega
	 * @param idProp : Identificador de la propiedad playxINV
	 * @param idtRel : IDTO de la relacion en la que la clase juega el rol
	 * @param qMin : Cardinalidad minima 
	 * @param qMax : Cardinalidad maxima
	 * @return La instancia de la cardinalidad del playINV construida
	 */
	
	public Instance createInstancePlayInCar(int idto, int idtRol, int idProp, int idtRel, int qMin, int qMax) {
		Instance ins = new Instance();
		ins.setIDTO(new Integer(idto).toString());
		ins.setPROPERTY(new Integer(idProp).toString());
		ins.setOP(Constants.OP_CARDINALITY);
		if(qMax!= -1)
			ins.setQMAX(new Integer(qMax).toString());
		if(qMin != -1)
			ins.setQMIN(new Integer(qMin).toString());
		ins.setVALUECLS(new Integer(idtRel).toString());
		
		return ins;
	}
}
