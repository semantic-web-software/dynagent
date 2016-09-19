package dynagent.ruleengine.test.test21.src;

import java.util.Iterator;
import java.util.LinkedList;

import jess.Fact;
import jess.Rete;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.data.dao.DAOManager;
import dynagent.ruleengine.src.factories.IRulerFactory;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.ruleengine.src.ruler.FactInstance;
import dynagent.ruleengine.src.ruler.IPropertyDef;
import dynagent.ruleengine.test.ITest;
import dynagent.server.ejb.FactoryConnectionDB;

public class Test21 implements ITest {
	
	private DocDataModel ddm;
	private Rete r;
	private IKnowledgeBaseInfo ik;
	
	public static void main (String args[]){
		new Test21();
	}
	
	/*public Test21(){
		int business=7;
		String ip="192.168.1.3";
		try{
			// Conecta a la BD
			FactoryConnectionDB fcdb = new FactoryConnectionDB(business,true,ip);
			DAOManager.getInstance().setFactConnDB(fcdb);
			
			// CargaMotor
			IRulerFactory rbbdd = RuleEngineFactory.getInstance().createRuler(DAOManager.getInstance().getFactConnDB(), business, null, null, false);
			//IKnowledgeBaseInfo ik2 = rbbdd.getIKnowledgeBaseInfo();
			ik = rbbdd.getIKnowledgeBaseInfo();
			rbbdd.init(ik, null, null);		   
			rbbdd.run();
			
			
			run(ik);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}*/
	
	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException{	
		System.out.println("\n\n----------------------TEST 21:  PRUEBAS REGLAS JESS (CANO)COMENTADO TODO EL CODIGO---------------");	
		/*this.ik=ik;
		try{
			ddm=(DocDataModel)ik;
			Iterator it = ddm.getAllInstanceFactsIterator("(instance ))");
			r = new Rete();
			ddm.createAllUtaskPrototypes(null, Constants.USER_SYSTEM);
			r.executeCommand("(defclass instance dynagent.ruleengine.src.ruler.Fact )");
			r.executeCommand("(defclass hierarchy dynagent.ruleengine.src.ruler.FactHierarchy)");
			//r.executeCommand("(deftemplate lista (multislot list))");
			r.executeCommand("(defglobal ?*engine* = 0)");
			
			Funcall fun = new Funcall("bind", r);
			fun.add(new Variable("engine", RU.VARIABLE));
            fun.add(new Value(this));
            fun.execute(r.getGlobalContext());
            r.executeCommand("reset");
            r.executeCommand("facts");
            		
			while(it.hasNext()){
				Fact f = (Fact)it.next();				
				//System.out.println(f.toString());
				//f.setLEVEL(Constants.LEVEL_MODEL);	
				Funcall fu = new Funcall("definstance", r);
				fu.add(new Value("instance",RU.ATOM));
				fu.add(new Value(f));
				fu.add(new Value(Constants.isIDClass(f.getIDO()) ? "static":"dynamic",RU.ATOM));
				fu.execute(r.getGlobalContext());				
			}
			String rut = this.getClass().getResource("copia.clp").getPath();
            String ruta ="E:/DESARROLLO/Workspace/Cano/RuleEngine/src/dynagent/ruleengine/test/test21/src/";
			try {
            	new ParserJess(ruta);
			} catch (IOException e){				
				e.printStackTrace();
			}      
			//r.executeCommand("(batch "+ruta+"copia.clp)");
			r.executeCommand("(batch "+rut+")");
			r.executeCommand("facts");
			r.executeCommand("reset");
			r.run();
			
		} catch (JessException e) {			
			e.printStackTrace();
		}	*/	
	}	
	
	
	/**

	 * @return devuelve la fecha actual en dias
	 */	
	public long getFechaHoy(){
		return (System.currentTimeMillis()*1000)/(3600*24);
	}	
	
	/**
	 * @return devuelve la empresa del usuario del sistema
	 */
	public int getBusiness(){
		return ddm.getNbusiness();
	}
	
	/**
	 * Hace una consulta a la BD
	 * @param q es una lista de lista donde se indica la property a filtrar y la clase ((PROPERTY IDTO)...)
	 * @return una lista de lista con el individuo y el valor
	 */
	public String[][] runQuery(String[][] q){
		for(int i=0;i < q.length;i++){
			for(int j=0; j < q[i].length; j++){
				q[i][j]="";
			}
		}
		return new String[2][2];
	}
	
	/**
	 * Crea una instancia de USER_TASK
	 * @param idtoUTASK
	 * @param tgClass
	 * @param valueClsTgClass
	 * @throws IncoherenceInMotorException 
	 */
	public void createTask(int idtoUTASK, int tgClass, int valueClsTgClass) throws IncoherenceInMotorException{
		try {
			
			LinkedList<IPropertyDef> newfacts = new LinkedList<IPropertyDef>();
			int ido=0;// = getIdoPrototype();
			newfacts.add(new FactInstance(idtoUTASK,ido,Constants.IdPROP_TARGETCLASS,String.valueOf(tgClass),valueClsTgClass,null,null,null,null));
			Iterator it = ddm.getUserRolesInUtask(idtoUTASK);
			
			while (it.hasNext())
			{
				int userRol=(Integer)it.next();
				newfacts.add(new FactInstance(idtoUTASK,ido,Constants.IdPROP_USERROL,String.valueOf(userRol),Constants.IDTO_USERROL, null,null,null,null));
			}		
			ddm.addFactsToRuler(newfacts);	
			
		} catch (NotFoundException e) {
			e.printStackTrace();
		}			
	}
	
	/**
	 * Este método se encarga de modificar el qmin y qmax de una Property
	 * @param ido
	 * @param property
	 * @param qmin
	 * @param qmax
	 */
	public void modifyQPropertyInstance(int ido, int prop, int qmin, int qmax){
		//Iterator it = ddm.getAllInstanceFactsIterator("(instance (IDO "+ido+") (PROP "+property+") (OP nil)))");
		Iterator it = ddm.getRuleEngine().getAllInstanceFacts(null, ido, prop, null,null, null, null, null,null).iterator();
		
		while(it.hasNext()){
			Fact f = (Fact) it.next();			
			//f.setQMIN((float)qmin);
			//f.setQMAX((float)qmax);				
		}		
	}
		
	/**
	 * Este método se crea para modificar el qmin y qmax de una Property de tipo boolean
	 * @param ido
	 * @param property
	 * @param qmin
	 * @param qmax
	 */
	public void modifyQPropertyInstanceBoolean(int ido, int property, Boolean qmin, Boolean qmax){
		/*
		Iterator it = ddm.getAllInstanceFactsIterator("(instance (IDO "+ido+") (PROP "+property+") (OP nil))");
		while(it.hasNext()){
			Fact f = (Fact) it.next();
			if(qmin)
				f.setQMIN(1f);
			else
				f.setQMIN(0f);
			if(qmax)
				f.setQMAX(1f);
			else
				f.setQMIN(0f);
		}	*/	
	}
	
	/**
	 * Este método se encarga de modificar el value y valuecls de una Property
	 * @param ido
	 * @param property
	 * @param qmin
	 * @param qmax
	 */
	public void modifyValuePropertyInstance(int ido, int property, int value, int valuecls){
		Iterator it=null;
		//it= ddm.getAllInstanceFactsIterator("(instance (IDO "+ido+") (PROP "+property+") (OP nil)))");
		while(it.hasNext()){
			Fact f = (Fact) it.next();			
			//f.setVALUE(String.valueOf(value));
			//f.setVALUECLS(valuecls);				
		}		
	}
	
	
	
	/*
	public int countBusquedaAlmacen (int idtoOPV, int propertyHasProd){
		int ret=0;
		Iterator prod =ddm.getAllInstanceFactsIterator("(instance (IDTO "+idtoOPV+") (PROP "+propertyHasProd+")))");
		HashMap<Integer, Float> almacen = new HashMap<Integer, Float>();
		Iterator almacenarBulto = ddm.getAllInstanceFactsIterator("(instance (IDTO Almacenar) (PROP bulto)))");
		Iterator almacenamiento = ddm.getAllInstanceFactsIterator("(instance (IDTO Almacenar) (PROP almacenamientoValue)))");
		while(prod.hasNext()){
			Fact fprod = (Fact)prod.next();
			while(almacenarBulto.hasNext()){
				Fact fbulto = (Fact)almacenarBulto.next();
				int idoAlmacenar = fbulto.getIDO();
				while(almacenamiento.hasNext()){
					Fact falm = (Fact)almacenamiento.next();
					int idoAlmacenamiento = falm.getIDO();				
					if((idoAlmacenar==idoAlmacenamiento) && fbulto.getVALUECLS()==fprod.getVALUECLS()&& fprod.getQMIN()>=fbulto.getQMIN()){
						if(almacen.get(Integer.parseInt(falm.getVALUE()))!=null)
							almacen.put(Integer.parseInt(falm.getVALUE()), almacen.get(Integer.parseInt(falm.getVALUE()))+fprod.getQMIN());
						else
							almacen.put(Integer.parseInt(falm.getVALUE()), fprod.getQMIN());
					}
					
				}
			}
		}
		Iterator it = ddm.getAllInstanceFactsIterator("(instance (IDTO Almacen)(IDO ?i&~nil)(PROP "+Constants.IdPROP_LEVEL+")(QMIN "+Constants.LEVEL_INDIVIDUAL+" | "+Constants.LEVEL_PROTOTYPE+")(QMAX "+Constants.LEVEL_INDIVIDUAL+" | "+Constants.LEVEL_PROTOTYPE+")))");
		Float max=0f, aux;		
		while(it.hasNext()){
			Fact f = (Fact)it.next();
			if(almacen.get(f.getIDO())!=null){
				aux=almacen.get(f.getIDO());
				if(aux>max){
					max=aux;
					ret=f.getIDO();
				}	
			}
		}
		return ret;
	}*/
}