package dynagent.ruleengine.src.ruler;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.meta.api.DocDataModel;
/**
 * Esta clase encapsulara la logica de bajo nivel de motor comun a JessEngine y JbossEngine
 * 
 * @author zamora
 *
 */

public abstract class CommonEngine implements IRuleEngine{
	
	/**
	 * Este metodo se encarga de crear un nuevo fact a motor comprobando previamente que no haya uno que haya exitiera uno con exacatamente el mismo valor y que haya sido
	 * borrado, en ese caso lo que hace es restrablecer el fact borrado a su estado antes de ser borrado para evitar duplicacion de facts a la hora de sincronizar con BBDD 
	 * @param newfact
	 * @throws NotFoundException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws DataErrorException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 */
	public void addNewFactToRuler(dynagent.common.knowledge.FactInstance newfact,Integer propertymaxcardinality,boolean setSystemValue) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		
		//DEPURAR ESTE METODO DE MOMENTO AÑADIRA SIEMPRE EL FACT PARA DEPURAR LAS REGLAS MAS RAPIDAMENTE
		boolean isequaltoprevio=false;
		FactInstance factprevio=null;
		//LinkedList<IPropertyDef>factsborrads=this.getAllInstanceFactsDELETED(newfact.getIDTO(), newfact.getIDO(), newfact.getPROP(),new Null(Null.NULL),newfact.getOP());
		LinkedList<IPropertyDef>factsborrads=this. getAllInstanceFactsDELETED(newfact.getIDO(),  newfact.getPROP());
		//System.err.println("info: addNewFActToRuler, encuentra factsborrados="+factsborrads);
		
		for(int i=0;i<factsborrads.size()&&!isequaltoprevio;i++){
			//factprevio=((Fact)factsborrads.get(i)).getInitialValues();
			
			//Para los objectProperty con cardinalidad 1 no queremos reutilizar el mismo fact solo porque tenga cardinalidad 1 ya que es un problema para las reglas(regla de aislados por ejemplo)
			//ya que el valor previo no es el real si se han hecho las dos operaciones(del y new seguidas)
			isequaltoprevio=(propertymaxcardinality!=null&&propertymaxcardinality.intValue()==1&&Constants.isDataType(newfact.getVALUECLS()))||(newfact.isequal(((Fact)factsborrads.get(i)).getValoresAnteriores()));
				
			if(isequaltoprevio){
				Fact f=(Fact)factsborrads.get(i);
				f.setQ(newfact.getQMIN(), newfact.getQMAX());
				if(((f.getVALUE()!=null && !f.getVALUE().equals(newfact.getVALUE()))||f.getVALUE()==null && newfact.getVALUE()!=null) && ((f.getVALUECLS()!=null && !f.getVALUECLS().equals(newfact.getVALUECLS()))||f.getVALUECLS()==null && newfact.getVALUECLS()!=null)){
					f.setVALUE(newfact.getVALUE(), newfact.getVALUECLS());
					f.setRANGENAME(newfact.getRANGENAME());
				}
				else if((f.getVALUE()!=null && !f.getVALUE().equals(newfact.getVALUE()))||f.getVALUE()==null && newfact.getVALUE()!=null){
					f.setVALUE(newfact.getVALUE());
				}
				else if((f.getVALUECLS()!=null && !f.getVALUECLS().equals(newfact.getVALUECLS()))||f.getVALUECLS()==null && newfact.getVALUECLS()!=null){
					f.setVALUECLS(newfact.getVALUECLS());
					f.setRANGENAME(newfact.getRANGENAME());
				}
				
				if(setSystemValue){
					f.setSystemValue(newfact.getSystemValue());
					f.setAppliedSystemValue(newfact.isAppliedSystemValue());
				}
				
				//System.err.println("info: addNewFActToRuler,  era igual a previo, despues cambio="+f);
				
			}else if(Auxiliar.equals(propertymaxcardinality, 1) && !Constants.isDataType(newfact.getVALUECLS())){
				//Si se trata de un objectProperty de cardinalidad 1 tenemos que quitar el systemValue del delete porque si no se registra en base de datos ya que se le envia un delete y un new.
				//Ademas se lo asignamos al nuevo fact.
				Fact f=(Fact)factsborrads.get(i);
				if(setSystemValue){
					newfact.setSystemValue(f.getSystemValue());
					newfact.setAppliedSystemValue(f.isAppliedSystemValue());
					f.setAppliedSystemValue(false);
					f.setSystemValue(null);
				}
			}
		}
		if(!isequaltoprevio){
			//	System.out.println("info: addNewFActToRuler: no es igual a fact previo, se insertar;"+newfact);
			insertFact(newfact);
		}
	}
	
	

	
	/*
	 * /*
		 * NO TIENE SENTIDO PASAR POR LA CONSULTAS GENERICAS DE OBJECT CUANDO
		 * TENEMOS UN FACT CON INTEGER,STRING,...(non-Javadoc)
	 * @see dynagent.ruleengine.src.ruler.IRuleEngine#deleteFact(dynagent.ruleengine.src.ruler.IPropertyDef)
	 */
	/*public int deleteFact(dynagent.ruleengine.src.ruler.IPropertyDef fact) {
		
		 * Object idto, ido, prop, valuecls, value, qMax, qMin, op;
		if (fact.getIDTO() == null || fact.getIDTO() == 0) {
			idto = new Null(Null.NULL);
		} else {
			idto = fact.getIDTO();
		}
		if (fact.getIDO() == null || fact.getIDO() == 0) {
			ido = new Null(Null.NULL);
		} else {
			ido = fact.getIDO();
		}
		if (fact.getPROP() == null || fact.getPROP() == 0) {
			prop = new Null(Null.NULL);
		} else {
			prop = fact.getPROP();
		}
		if (fact.getVALUE() == null) {
			value = new Null(Null.NULL);
		} else {
			value = fact.getVALUE();
		}
		if (fact.getVALUECLS() == null || fact.getVALUECLS() == 0) {
			valuecls = new Null(Null.NULL);
		} else {
			valuecls = fact.getVALUECLS();
		}
		if (fact.getQMAX() == null) {
			qMax = new Null(Null.NULL);
		} else {
			qMax = fact.getQMAX();
		}
		if (fact.getQMIN() == null) {
			qMin = new Null(Null.NULL);
		} else {
			qMin = fact.getQMIN();
		}
		if (fact.getOP() == null) {
			op = new Null(Null.NULL);
		} else {
			op = fact.getOP();
		}
		return deleteFactCond(idto, ido, prop, valuecls, value,qMax, qMin, op);
	
		}
		*/
		
		
		
	
	
	public int setFact(IPropertyDef facttomodify, IPropertyDef newfact,boolean setSystemValue) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		LinkedList<IPropertyDef> lfacts=this.getInstanceFactsWhere(facttomodify.getIDTO(), facttomodify.getIDO(),facttomodify.getPROP(),facttomodify.getVALUE(),facttomodify.getVALUECLS(), facttomodify.getQMIN(), facttomodify.getQMAX(), facttomodify.getOP());
		//System.err.println("lfacts encontrados:"+lfacts);
		int nmodif=0;//var para computar el numero de facts que se cambian
		for (int i=0;i<lfacts.size();i++){
			boolean modify=false;
			Fact f=(Fact)lfacts.get(i);
			if(((f.getVALUE()!=null && !f.getVALUE().equals(newfact.getVALUE()))||f.getVALUE()==null && newfact.getVALUE()!=null) && ((f.getVALUECLS()!=null && !f.getVALUECLS().equals(newfact.getVALUECLS()))||f.getVALUECLS()==null && newfact.getVALUECLS()!=null)){
				f.setVALUE(newfact.getVALUE(), newfact.getVALUECLS());
				f.setRANGENAME(newfact.getRANGENAME());
				modify=true;
			}
			else if((f.getVALUE()!=null && !f.getVALUE().equals(newfact.getVALUE()))||f.getVALUE()==null && newfact.getVALUE()!=null){
				f.setVALUE(newfact.getVALUE());
				modify=true;
			}
			else if((f.getVALUECLS()!=null && !f.getVALUECLS().equals(newfact.getVALUECLS()))||f.getVALUECLS()==null && newfact.getVALUECLS()!=null){
				f.setVALUECLS(newfact.getVALUECLS());
				f.setRANGENAME(newfact.getRANGENAME());
				modify=true;
			}
			if((f.getOP()!=null && !f.getOP().equals(newfact.getOP()))||f.getOP()==null && newfact.getOP()!=null){
				f.setOP(newfact.getOP());
				modify=true;
			}
			if(((f.getQMAX()!=null && !f.getQMAX().equals(newfact.getQMAX()))||f.getQMAX()==null && newfact.getQMAX()!=null)&&((f.getQMIN()!=null && !f.getQMIN().equals(newfact.getQMIN()))||f.getQMIN()==null && newfact.getQMIN()!=null)/* &&f.getQMAX().equals(f.getQMIN())*/){
				f.setQ(newfact.getQMIN(),newfact.getQMAX());
				modify=true;
			}
			else
			if((f.getQMAX()!=null && !f.getQMAX().equals(newfact.getQMAX()))||f.getQMAX()==null && newfact.getQMAX()!=null){
				f.setQMAX(newfact.getQMAX());
				modify=true;
			}
			else
			if((f.getQMIN()!=null && !f.getQMIN().equals(newfact.getQMIN()))||f.getQMIN()==null && newfact.getQMIN()!=null){
				f.setQMIN(newfact.getQMIN());
				modify=true;
			}
			
			if(setSystemValue){
				f.setSystemValue(newfact.getSystemValue());
				f.setAppliedSystemValue(newfact.isAppliedSystemValue());
				modify=true;
			}
			
			if(modify)
				nmodif++;
		}
		
		return nmodif;
	}
	
	
	
	
	
	public int setFact(IPropertyDef facttomodify, String slot, Object value) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		LinkedList<IPropertyDef> lfacts=getAllInstanceFactsNoRevert(facttomodify.getIDTO(), facttomodify.getIDO(),facttomodify.getPROP(), facttomodify.getVALUECLS(),facttomodify.getVALUE(),null, facttomodify.getQMAX(), facttomodify.getQMIN(), facttomodify.getOP());
		int nmodif=0;//var para computar el numero de facts que se cambian
		for (int i=0;i<lfacts.size();i++){
			boolean modify=false;
			Fact f=(Fact)lfacts.get(i);
			if(slot.equals("IDTO")&&!value.equals(facttomodify.getIDTO())){
				f.setIDTO((Integer)value);
				modify=true;
			}
			if(slot.equals("VALUE")&&!value.equals(facttomodify.getVALUE())){
				f.setVALUE((String)value);
				modify=true;
			}
			else if(slot.equals("OP")&&!value.equals(facttomodify.getOP())){
				f.setOP((String)value);
				modify=true;
			}
			else if(slot.equals("QMAX")&&!value.equals(facttomodify.getQMAX())){
				f.setQMAX((Double)value);
				modify=true;
			}
			else if(slot.equals("QMIN")&&!value.equals(facttomodify.getQMIN())){
				f.setQMIN((Double)value);
				modify=true;
			}
			if(modify){
				nmodif++;
			}
		}
		
		return nmodif;
	}
	
	
	 public int deleteFactCond(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin, Object op) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
			int cont = 0;
			Iterator it = this.getAllInstanceFactsNoRevert(idto, ido, prop, valuecls, value, null, qMax, qMin,op).iterator();
			while(it.hasNext()){
				Fact f = (Fact) it.next();
				f.deleteFactSessionable();
				cont++;
			}
			
			return cont;
		}
	 
	
	protected abstract LinkedList<IPropertyDef> getAllInstanceFactsNoRevert(Object idto, Object ido, Object prop, Object valuecls, Object value, Object name, Object qMax, Object qMin,Object op ) throws NotFoundException, IncoherenceInMotorException;
	
	protected abstract LinkedList<IPropertyDef> getAllInstanceFacts(int idto, Integer ido, int prop,String value, Integer valuecls,  Double qMin, Double qMax, String op);
	
	
	 //public LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Object idto, Object ido, Object prop, Object valuecls,  Object name);
	 
	 public  abstract void insertFact(Object fact) throws NotFoundException, IncoherenceInMotorException;
	 
	 public abstract int deleteFactCondRETRACT(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin, Object op) throws NotFoundException, IncoherenceInMotorException;
		
	public abstract LinkedList<FactAccess> getAccessFactsOfProperty(int idto, Integer ido, int idProp, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException;
		
	public  abstract LinkedList<FactAccess> getAccessFactsOverObject(Integer idto, Integer ido, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException;
	
	//public abstract LinkedList<FactAccess> getAllAccessFacts(Object utask, Object idto, Object accesstype, Object ido, Object prop, Object value, Object valuecls, Object userrol, Object user) throws NotFoundException;
		
	public abstract LinkedList<FactHierarchy> getAllHierarchyFacts(Object idto, Object idtoSup) throws NotFoundException, IncoherenceInMotorException;

	public abstract LinkedList<IPropertyDef> getAllInstanceFacts(Object idto, Object ido, Object prop, Object valuecls, Object value, Object name, Object qMax, Object qMin, Object op) throws NotFoundException, IncoherenceInMotorException;
		
	public abstract LinkedList<FactProp> getAllPropertyFacts(Object prop, Object name) throws NotFoundException, IncoherenceInMotorException;
	
	//public abstract void inicializeRules(String fileRules) throws EngineException; 
	
	public abstract void printMotor();
	
	public abstract LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Integer ido,Integer idProp);
	
	public abstract void setGlobal(String name,Object value);
	
	public abstract IRuleEngine doClone(DocDataModel ddm) throws EngineException, NotFoundException, IncoherenceInMotorException;

}
