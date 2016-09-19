package dynagent.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.communication.ISubmitDDBBListener;
import dynagent.common.communication.IndividualData;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.Auxiliar;
import dynagent.gui.utils.IDebugListener;

public final class Debug implements ISubmitDDBBListener{

	private ArrayList<String> lastSubmitDDBB;
	private HashMap<Integer,Integer> mapRenameIdos;
	private static Debug instance;
	ArrayList<IDebugListener> listDebugListener;
	boolean advanced;
	
	private Debug(){
		lastSubmitDDBB=new ArrayList<String>();
		mapRenameIdos=new HashMap<Integer, Integer>();
		listDebugListener=new ArrayList<IDebugListener>();
		advanced=false;
	}
	
	public static Debug getInstance(){
		if(instance==null){
			instance=new Debug();
		}
		return instance;
	}
	
	public void init(){
		Singleton.getInstance().getComm().addSubmitDDBBListener(this);
	}
	
	public void registerSubmitDDBB(IndividualData aipd) {
		try{
			lastSubmitDDBB=new ArrayList<String>();
			mapRenameIdos=new HashMap<Integer, Integer>();
	
			KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBaseAdapter(aipd.getKnowledgeBase());
			Iterator<IPropertyDef> itr=aipd.getAIPropertyDef().iterator();
			while(itr.hasNext()){
				IPropertyDef f=itr.next();
				IPropertyDef fInitial=f.getInitialValues();
				
				if(!f.getVALUECLS().equals(Constants.IDTO_DATE) && !f.getVALUECLS().equals(Constants.IDTO_DATETIME) && !f.getVALUECLS().equals(Constants.IDTO_TIME)){
					if(advanced)
						lastSubmitDDBB.add("<NEW>"+buildAdvancedMessage(f,kba)+"</NEW> <OLD>"+buildAdvancedMessage(fInitial,kba)+"</OLD>");
					else lastSubmitDDBB.add("<NEW>"+buildBasicMessage(f,kba)+"</NEW> <OLD>"+buildBasicMessage(fInitial,kba)+"</OLD>");
				}
			}
			notifyLastSubmitDDBB(lastSubmitDDBB);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private String buildBasicMessage(IPropertyDef f,KnowledgeBaseAdapter kba) throws NotFoundException{
		if(f==null)
			return "";
		else{
			/*Integer idoRenamed=mapRenameIdos.get(f.getIDO());
			if(idoRenamed==null){
				idoRenamed=mapRenameIdos.size();
				mapRenameIdos.put(f.getIDO(), idoRenamed);
			}
			
			String valueRenamed=null;
			String systemValueRenamed=null;
			if(Constants.isBasicType(f.getVALUECLS())){
				valueRenamed=f.getVALUE();
			}else{
				if(f.getVALUE()!=null){
					if(mapRenameIdos.containsKey(new Integer(f.getVALUE())))
						valueRenamed=mapRenameIdos.get(new Integer(f.getVALUE())).toString();
					else{
						valueRenamed=String.valueOf(mapRenameIdos.size());
						mapRenameIdos.put(new Integer(f.getVALUE()), new Integer(valueRenamed));
					}
				}
				
				if(f.getSystemValue()!=null){
					if(mapRenameIdos.containsKey(new Integer(f.getSystemValue())))
						systemValueRenamed=mapRenameIdos.get(new Integer(f.getSystemValue())).toString();
					else{
						systemValueRenamed=String.valueOf(mapRenameIdos.size());
						mapRenameIdos.put(new Integer(f.getSystemValue()), new Integer(systemValueRenamed));
					}
				}
			}*/
			
			//Obtenemos los level del ido, value y systemValue, si tienen valor, y eso es lo que se le muestra al usuario en lugar del valor.
			//Ademas tenemos un numerico que le añadimos al level para identificar que se trata del mismo valor o de otro diferente. 
			
			int ido=f.getIDO();
			int levelIdo=-1;
			if(!kba.isLoad(ido)){
				if(f.getOrder()==action.DEL_OBJECT)//Al borrar un individuo lo normal es que este no este cargado en motor
					levelIdo=Constants.LEVEL_INDIVIDUAL;
			}else levelIdo=kba.getLevelObject(ido);
			
			int relativeNumericIdo=0;
			String typeIdo="|";
			switch(levelIdo){
				case Constants.LEVEL_FILTER:
					typeIdo+="Filter";
					break;
				case Constants.LEVEL_PROTOTYPE:
					typeIdo+="Prototype";
					break;
				case Constants.LEVEL_INDIVIDUAL:
					typeIdo+="Individual";
					break;
				case Constants.LEVEL_MODEL:
					typeIdo+="Model";
					break;
				default:
					typeIdo+="Indefined";
			}
			typeIdo+=relativeNumericIdo+"|";
			String value=f.getVALUE();
			String systemValue=f.getSystemValue();
			String destinationSystem=f.getDestinationSystem();
			int relativeNumericValue=1;
			int relativeNumericSystemValue=2;
			if(kba.getCategoryProperty(f.getPROP()).isObjectProperty()){
				if(f.getVALUE()!=null){
					int levelValue=kba.getLevelObject(Integer.valueOf(value));
					value="|";
					switch(levelValue){
						case Constants.LEVEL_FILTER:
							value+="Filter";
							break;
						case Constants.LEVEL_PROTOTYPE:
							value+="Prototype";
							break;
						case Constants.LEVEL_INDIVIDUAL:
							value+="Individual";
							break;
						case Constants.LEVEL_MODEL:
							value+="Model";
							break;
						default:
							value+="Indefined";
					}
					relativeNumericValue=((ido==Integer.valueOf(f.getVALUE()))?relativeNumericIdo:relativeNumericValue);
					value+=relativeNumericValue+"|";
				}else{
					value=null;
				}
				
				if(f.getSystemValue()!=null){
					int levelSystemValue=kba.getLevelObject(Integer.valueOf(systemValue));
					systemValue="|";
					switch(levelSystemValue){
						case Constants.LEVEL_FILTER:
							systemValue+="Filter";
							break;
						case Constants.LEVEL_PROTOTYPE:
							systemValue+="Prototype";
							break;
						case Constants.LEVEL_INDIVIDUAL:
							systemValue+="Individual";
							break;
						case Constants.LEVEL_MODEL:
							systemValue+="Model";
							break;
						default:
							systemValue+="Indefined";
					}
					relativeNumericSystemValue=((ido==Integer.valueOf(f.getSystemValue()))?relativeNumericIdo:(Auxiliar.equals(f.getVALUE(),f.getSystemValue())?relativeNumericValue:relativeNumericSystemValue));
					systemValue+=relativeNumericSystemValue+"|";
				}else{
					systemValue=null;
				}
			}else{
				if(f.getPROP()==Constants.IdPROP_RDN){//Sustituimos los rdn temporales por |Temporal|. Lo hacemos en todo el string porque podria estar formado por varios rdn temporales
					if(value!=null){
						value=value.replaceAll(Constants.DEFAULT_RDN_CHAR+"(id)?(\\-?)[0-9]+"+Constants.DEFAULT_RDN_CHAR,"|Temporal|");
					}
					if(systemValue!=null){
						systemValue=systemValue.replaceAll(Constants.DEFAULT_RDN_CHAR+"(id)?(\\-?)[0-9]+"+Constants.DEFAULT_RDN_CHAR,"|Temporal|");
					}
				}else if(f.getVALUECLS()==Constants.IDTO_FILE || f.getVALUECLS()==Constants.IDTO_IMAGE){
					value="|Archivo|";//Quitamos el value ya que es dependiente del milisegundo de guardado por lo que siempre seria distinto
				}
			}
			
			if(destinationSystem!=null){
				destinationSystem=destinationSystem.replaceAll(Constants.DEFAULT_RDN_CHAR+"(\\-?)[0-9]+"+Constants.DEFAULT_RDN_CHAR,"|Temporal|");
			}
			
			return "(IDO:"+typeIdo+
			" CLASS:"+f.getCLASSNAME()+
			" PROP:"+kba.getPropertyNameWithoutAlias(f.getPROP())+
			" VALUE:"+value+
			" QMIN:"+f.getQMIN()+
			" QMAX:"+f.getQMAX()+
			" RANGE:"+f.getRANGENAME()+
			" OP:"+f.getOP()+
			" ORDER:"+f.getOrder()+
			" EXISTIABD:"+f.getExistia_BD()+
			//" SYSTEMVALUE:"+systemValue+
			//" APPLIEDSYSTEMVALUE:"+f.isAppliedSystemValue()+
			" DESTINATIONSYSTEM:"+destinationSystem+
			" INCREMENTAL:"+f.isIncremental()+
			")";
		}
	}
	
	private String buildAdvancedMessage(IPropertyDef f,KnowledgeBaseAdapter kba) throws NotFoundException{
		if(f==null)
			return "";
		else{
			/*Integer idoRenamed=mapRenameIdos.get(f.getIDO());
			if(idoRenamed==null){
				idoRenamed=mapRenameIdos.size();
				mapRenameIdos.put(f.getIDO(), idoRenamed);
			}
			
			String valueRenamed=null;
			String systemValueRenamed=null;
			if(Constants.isBasicType(f.getVALUECLS())){
				valueRenamed=f.getVALUE();
			}else{
				if(f.getVALUE()!=null){
					if(mapRenameIdos.containsKey(new Integer(f.getVALUE())))
						valueRenamed=mapRenameIdos.get(new Integer(f.getVALUE())).toString();
					else{
						valueRenamed=String.valueOf(mapRenameIdos.size());
						mapRenameIdos.put(new Integer(f.getVALUE()), new Integer(valueRenamed));
					}
				}
				
				if(f.getSystemValue()!=null){
					if(mapRenameIdos.containsKey(new Integer(f.getSystemValue())))
						systemValueRenamed=mapRenameIdos.get(new Integer(f.getSystemValue())).toString();
					else{
						systemValueRenamed=String.valueOf(mapRenameIdos.size());
						mapRenameIdos.put(new Integer(f.getSystemValue()), new Integer(systemValueRenamed));
					}
				}
			}*/
			
			return "(IDO:"+/*idoRenamed*/f.getIDO()+
			" IDTO:"+f.getIDTO()+
			" CLASS:"+f.getCLASSNAME()+
			" IDPROP:"+f.getPROP()+
			" PROP:"+kba.getPropertyNameWithoutAlias(f.getPROP())+
			" VALUE:"+/*valueRenamed*/f.getVALUE()+
			" QMIN:"+f.getQMIN()+
			" QMAX:"+f.getQMAX()+
			" VALUECLS:"+f.getVALUECLS()+
			" RANGE:"+f.getRANGENAME()+
			" OP:"+f.getOP()+
			" ORDER:"+f.getOrder()+
			" EXISTIABD:"+f.getExistia_BD()+
			//" SYSTEMVALUE:"+/*systemValueRenamed*/f.getSystemValue()+
			//" APPLIEDSYSTEMVALUE:"+f.isAppliedSystemValue()+
			" DESTINATIONSYSTEM:"+f.getDestinationSystem()+
			" INCREMENTAL:"+f.isIncremental()+
			")";
		}
			
			/*return "(IDO:"+f.getIDO()+
			" IDTO:"+f.getIDTO()+
			" CLASS:"+f.getCLASSNAME()+
			" PROP:"+f.getPROP()+
			" VALUE:"+f.getVALUE()+
			" VALUECLS:"+f.getVALUECLS()+
			" RANGE:"+f.getRANGENAME()+
			" QMIN:"+f.getQMIN()+
			" QMAX:"+f.getQMAX()+
			" OP:"+f.getOP()+
			" ORDER:"+f.getOrder()+
			" EXISTIABD:"+f.getExistia_BD()+
			" SYSTEMVALUE:"+f.getSystemValue()+
			" APPLIEDSYSTEMVALUE:"+f.isAppliedSystemValue()+
			" DESTINATIONSYSTEM:"+f.getDestinationSystem()+
			" INCREMENTAL:"+f.isIncremental()+
			")";*/
	}

	public ArrayList<String> getLastSubmitDDBB() {
		return lastSubmitDDBB;
	}
	
	public void addDebugListener(IDebugListener listener){
		listDebugListener.add(listener);
	}
	
	public void setAdvanced(boolean advanced){
		this.advanced=advanced;
	}
	
	public boolean isAdvanced(){
		return advanced;
	}
	
	public void notifyLastSubmitDDBB(ArrayList<String> lastSubmit){
		Iterator<IDebugListener> itrDebugListener=listDebugListener.iterator();
		while(itrDebugListener.hasNext()){
			itrDebugListener.next().lastSubmitDDBB(lastSubmit);
		}
	}

}
