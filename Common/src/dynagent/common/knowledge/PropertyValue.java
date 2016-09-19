package dynagent.common.knowledge;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;

public class PropertyValue {
	public String name;
	public Integer idProp;
	/**
	 * id: identificador del objeto 
	 */
	public Integer ido;
	/**
	 * idto: identificador del tipo de objeto 
	 */
	public Integer idto;
	
	public LinkedList<Value> values=new LinkedList<Value>();
	
	
	public Integer getIdProp() {
		return idProp;
	}
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		
	}
	
	public Integer getIdo() {
		return ido;
	}
	public void setIdo(Integer ido) {
		this.ido = ido;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	
		
	
		public String ValueListToString(LinkedList  vList){
		String result = null;
		for(int i=0;i<vList.size();i++){
			if(result==null){
				result=vList.get(i).toString();
			}
			else if(vList.get(i)!=null)
				result+=vList.get(i).toString();
		}
		return result;
	
	}
	
	
	public  int  getNumeroValores(){
		return this.values.size();
	}
		
	
	
	
	
	public   LinkedList<Value> getValues(){
		return this.values;
	}
	
	
	/**
	 * Devuelve una array con una lista de los valores de la propiedades en representación string.
	 * @return
	 */
	
	/*public   ArrayList<String> getVALORES(){
		 	ArrayList<String> values=new ArrayList<String> ();
			for(int i=0;i<this.getValues().size();i++){
				values.add(this.getValues().get(i).getValue_s());
			}
			//System.err.println("\n debug PROPERTYVALUE.getVALORES DEVUELVE="+values);
		return values;
	}*/
	
	
	/**
	 * Devuelve el valor cuando tiene exactamente un valor.
	 * Si no tiene valor o tiene más de uno devuelve null;
	 * IMPORTANTE: SI TIENE MÁS DE UN VALOR DEVULVE NULL, esta diseñado para ser llamado desde sitios donde se está seguro que la cardinalidad max es 1.
	 *@deprecated: Sustituyase su uso por el de getUniqueValue que tiene un nombre más descriptivo
	 *
	 */
	public Value getUniqueValue(){
		 Value uniqvalue=null;
		 if(this.getValues().size()==1){
			 uniqvalue=this.getValues().get(0);
		 }
		 else if(this.getValues().size()>1){
			 System.err.println(".................INFO::!!!   PropertyValue.getUniqueValue tiene más de un valor: "+this.getValues()+" este metodo devolverá nulo");
		 }
		 /*else if(this.getValues().size()==0){
			 System.err.println(".................INFO:   PropertyValue.getUniqueValue no encuentra valor "+this.getValues()+" y por eso este metodo devolverá nulo");
		 }*/
		 return uniqvalue;
	}
	
	public String getStringUniqueValue(){
		 Value val=this.getUniqueValue();
		 String svalue=null;
		 if(val!=null)
			 svalue=val.getValue_s();
		 return svalue;
	}
	
	
	
	public ArrayList<String> getStringValues(){
		ArrayList<String> valoresstring=new ArrayList<String>();
		 LinkedList<Value> valores=this.getValues();
		 for(int i=0;i<valores.size();i++){
			 valoresstring.add(valores.get(i).getValue_s());
		 }
		 return valoresstring;
	}
	
	
	
	public String toString(){
		String result="<PropertyValue";
		result=result+"  ido="+getIdo();
		result=result+"  idto="+getIdto();
		result=result+"  idProp="+getIdProp();
		result=result+"  name="+getName();
		result=result+">";
		if(this.values.size()>0)
			result=result+"\n   <values "+this.ValueListToString(this.getValues())+"\n   </valueList>";
		else
			result=result+"\n   <values EMPTY  </values>";
		
		result=result+"\n</PropertyValue>";

		
		return result;
				
	}
	public void setValues(LinkedList<Value> values) {
		this.values = values;
	}	
	
	public  boolean checkPropertyWellDefined(IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, CardinalityExceedException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		int nval=this.getNumeroValores();
		boolean welldef=true;
		CardinalityExceedException ceE;
		Property pModel;
		Session s=ik.getDefaultSession();//Utilizamos sessionDefault porque estamos seguros que no se traera nada de BD
		s.setRunRules(false);//Evitamos que se disparen reglas
		try{
			if(ik.existInMotor(this.getIdo())){
//				SI EXISTE EN MOTOR SE PIDE PROPERTY DEL IDO CONCRETO PQ ALGUNA REGLA PODRIA HABER PUESTO CARDINALIDAD, RANGOS,... MAS ESPECIFICOS AL IDO QUE A LA CLASE
				pModel=ik.getProperty(this.getIdo(), this.getIdto(), idProp, null, Constants.USER_SYSTEM, null, s);	
			}
			else{//caso contrario se chequea contra la definicion de la clase
				pModel=ik.getProperty(null, this.getIdto(), idProp, null, Constants.USER_SYSTEM, null, s);
			}
			
		}finally{
			s.setRunRules(true);
		}
		if(pModel.getCardMin()!=null && nval<pModel.getCardMin()){
			welldef=false;
			if(pModel.getCardMin()==1){
				ceE=new CardinalityExceedException("La propiedad "+this.getName()+"  es obligatoria. pr="+pModel,pModel);
				ceE.setUserMessage("El campo '"+ik.getAliasOfProperty(idto, idProp, userTask)+"' es obligatorio");
			}
			else if(pModel.getCardMax()!=null&&pModel.getCardMin().intValue()==pModel.getCardMax().intValue()){
				ceE=new CardinalityExceedException("La propiedad "+pModel.getName()+"  debe tener "+pModel.getCardMin()+" valores pr=+pr"+pModel,pModel);
				ceE.setUserMessage("El campo '"+ik.getAliasOfProperty(idto, idProp, userTask)+"' debe tener "+pModel.getCardMin()+" valores");
			}
			else {
				ceE=new CardinalityExceedException("La propiedad "+pModel.getName()+"  debe tener al menos "+pModel.getCardMin()+" valores  pr="+pModel,pModel);
				ceE.setUserMessage("El campo '"+ik.getAliasOfProperty(idto, idProp, userTask)+"' debe tener al menos "+pModel.getCardMin()+" valores");
			}
			throw ceE;
			
				
		}
		if(pModel.getCardMax()!=null&&nval>pModel.getCardMax().intValue()){
			welldef=false;
			ceE=new CardinalityExceedException("DataPropety.checkPropertyWellDefined cardinalityException para p="+pModel,pModel);
			ceE.setUserMessage("El campo '"+ik.getAliasOfProperty(idto, idProp, userTask)+"' tiene mas valores de los permitidos"); 
			throw ceE;
		}
		for(int i=0;i<this.getValues().size();i++){
			Value v=this.getValues().get(i);
			welldef=v.checkIsCompatibleWith(pModel,ik,userTask);
		}
		return welldef;
	}	
	
	
	public int eliminarValoresDuplicados_depuracion(){
		LinkedList<Value> valoresNoDuplicados=new LinkedList<Value>(); 
		HashSet<String> valorEnString=new HashSet<String>(); 
		for(int i=0;i<this.getValues().size();i++){
			Value val=this.getValues().get(i);
			String valorToString=val.toString();
			if(!valorEnString.contains(valorToString)){
				valoresNoDuplicados.add(val);
				valorEnString.add(valorToString);
			}
		}
		if(valoresNoDuplicados.size()<this.getValues().size()){
			System.err.println("\n\n ------------WARNING VALORES DUPLICADOS EN PROPERTYVALUE: "+this);
			this.setValues(valoresNoDuplicados);
			Auxiliar.printCurrentStackTrace();
			return (this.getValues().size()-valoresNoDuplicados.size());
		}else{ return 0;}
	}
}



