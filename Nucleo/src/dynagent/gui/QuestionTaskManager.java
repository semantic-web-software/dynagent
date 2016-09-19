package dynagent.gui;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Stack;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

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
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IndividualValues;
import dynagent.gui.actions.ActionQuestionTaskIterator;
import dynagent.gui.actions.IFormData;
import dynagent.gui.actions.commands.QuestionTaskCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.FormControl;
import dynagent.ruleengine.src.sessions.SessionController;

public class QuestionTaskManager implements IQuestionListener{

	private HashMap<KnowledgeBaseAdapter,Stack<StackInfo>> mapCurrent;
	private HashMap<IKnowledgeBaseInfo,ArrayList<RequestInfo>> mapRequest;
	private HashMap<Integer, RequestInfo> mapIdQuestion;
	private HashMap<IKnowledgeBaseInfo, HashMap<String,IQuestionListener>> mapListener;
	
	public QuestionTaskManager(){
		mapCurrent=new HashMap<KnowledgeBaseAdapter, Stack<StackInfo>>();
		mapRequest=new HashMap<IKnowledgeBaseInfo, ArrayList<RequestInfo>>();
		mapIdQuestion=new HashMap<Integer, RequestInfo>();
		mapListener=new HashMap<IKnowledgeBaseInfo, HashMap<String,IQuestionListener>>();
	}
	
	public void exeQuestion(String rdn, ArrayList<IndividualValues> data, HashMap<String,String> alias, Integer idtoUserTask,KnowledgeBaseAdapter kba,WindowComponent window,Session sess){
		window.disabledEvents();
		//commandPath puede ser de varias clases, segun la clase creada (en este caso QuestionTaskCommandPath) createActionStep de actionStepFactory creara la instancia apropiada
		commandPath cPath=new QuestionTaskCommandPath(Constants.IDTO_QUESTION_TASK,rdn,data,alias,null,idtoUserTask,sess);
		try {
			Singleton.getInstance().getActionManager().exeOperation(cPath, kba, null, window, true);
		} catch (Exception e) {
			e.printStackTrace();
			Singleton.getInstance().getComm().logError(window.getComponent(),e,"Error al intentar crear el asistente de configuración");
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			Singleton.getInstance().getComm().logError(window.getComponent(),null,"Error al intentar crear el asistente de configuración por falta de memoria");
		} finally{
			window.enabledEvents();
		}
	}
	
	public void addCurrent(IFormData form, WindowComponent window, KnowledgeBaseAdapter kba){
		//System.err.println("-----------------addCurrent:"+window+" "+kba);
		Stack<StackInfo> stack=mapCurrent.get(kba);
		if(stack==null){
			stack=new Stack<StackInfo>();
			mapCurrent.put(kba, stack);
			stack.add(new StackInfo(form,window));
		}else{
			StackInfo info=stack.peek();
			if(info.getWindow()==window)
				info.setForm(form);
			else{
				stack.add(new StackInfo(form,window));
			}
		}
	}
	
	public void removeCurrent(KnowledgeBaseAdapter kba){
		//System.err.println("-----------------removeCurrent:"+kba);
		if(mapCurrent.containsKey(kba)){
			mapCurrent.get(kba).pop();
			if(mapCurrent.get(kba).isEmpty()){
				mapCurrent.remove(kba);
			}
		}else System.err.println("WARNING: Se esta llamando a removeCurrent con un kba que no esta en los mapas. No es un problema para la aplicación pero esto no deberia suceder");
	}

	public void process(final KnowledgeBaseAdapter kba) {
		//System.err.println("-----------------process:"+kba);
				
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				ArrayList<RequestInfo> list=mapRequest.get(kba.getKnowledgeBase());
				if(list!=null){
					//Auxiliar.printCurrentStackTrace();
					//TODO En realidad seria un problema que vinieran peticiones con distinto rdn, ya que el questionListener solo atendería a una de ellas
					LinkedHashMap<String,ArrayList<IndividualValues>> mapRdnData=new LinkedHashMap<String, ArrayList<IndividualValues>>();
					LinkedHashMap<String,HashMap<String,String>> mapRdnAlias=new LinkedHashMap<String, HashMap<String,String>>();
					for(RequestInfo r:list){
						String rdn=r.getRdn();
						
						ArrayList<IndividualValues> dataList=mapRdnData.get(rdn);
						if(dataList==null){
				     		dataList=new ArrayList<IndividualValues>();
				     	}
				     	if(r.getData()!=null)
				     		dataList.addAll(r.getData());
				         	
						mapRdnData.put(rdn,dataList);
						
						HashMap<String,String> aliasList=mapRdnAlias.get(rdn);
						if(aliasList==null){
							aliasList=new HashMap<String,String>();
				     	}
				     	if(r.getAlias()!=null)
				     		aliasList.putAll(r.getAlias());
				     	
						mapRdnAlias.put(rdn,aliasList);
					}
					
					mapRequest.remove(kba.getKnowledgeBase());
					
					while(Singleton.getInstance().isMultiWindow() && !Singleton.getInstance().hasKnowledgeBaseAdapterClone()){/*Para evitar que en el clone se copien los prototipos que vamos a crear*/
						try {
							//System.err.println("Esperando creación del clone...");
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					StackInfo stackInfo=mapCurrent.get(kba).peek();
					for(String rdn:mapRdnData.keySet()){
						Integer idtoUserTask=null;
						Session sess=stackInfo.getForm()!=null?stackInfo.getForm().getSession():kba.getDDBBSession();
						exeQuestion(rdn, mapRdnData.get(rdn), mapRdnAlias.get(rdn), idtoUserTask, kba, stackInfo.getWindow(), sess);
					}
				}
			}
			
		});
	}
	
	class StackInfo{
		IFormData form;
		WindowComponent window;
		
		public StackInfo(IFormData form, WindowComponent window){
			this.window=window;
			this.form=form;
		}

		public WindowComponent getWindow() {
			return window;
		}

		public void setWindow(WindowComponent window) {
			this.window = window;
		}

		public IFormData getForm() {
			return form;
		}

		public void setForm(IFormData form) {
			this.form = form;
		}
		
	}

	public void cancelled(int id,String rdn,IKnowledgeBaseInfo ik) {
		//System.err.println("Cancelled:"+id+" IKnowledgeBaseInfo:"+ik);
		HashMap<String,IQuestionListener> map=mapListener.get(ik);
		if(map!=null && map.get(rdn)!=null){
			map.get(rdn).cancelled(id, rdn, ik);
		}else{
			if(mapRequest.get(ik)!=null)
				mapRequest.get(ik).remove(mapIdQuestion.remove(id));
		}
	}

	public void request(final int id, final String rdn, final ArrayList<IndividualValues> mapData, HashMap<String,String> alias, final IKnowledgeBaseInfo ik) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException {
		//System.err.println("request:"+id+" IKnowledgeBaseInfo:"+ik);
		if(!Singleton.getInstance().isProcessingCopyRowTable()){//Evitamos que se gestionen peticiones de QuestionTask cuando se esta haciendo un pegado masivo de filas ya que la ventana que saca pararia el pegado
			HashMap<String,IQuestionListener> map=mapListener.get(ik);
			if(map!=null && map.get(rdn)!=null){
				map.get(rdn).request(id, rdn, mapData, alias, ik);
			}else{
				RequestInfo requestInfo=new RequestInfo(id,rdn,mapData,alias);
				ArrayList<RequestInfo> list=mapRequest.get(ik);
				if(list==null){
					list=new ArrayList<RequestInfo>();
					mapRequest.put(ik, list);
				}
				list.add(requestInfo);
				mapIdQuestion.put(id, requestInfo);
			}
		}
	}
	
	public void addQuestionTaskListener(IQuestionListener listener,IKnowledgeBaseInfo ik,String rdn){
		HashMap<String,IQuestionListener> map=mapListener.get(ik);
		if(map==null){
			map=new HashMap<String, IQuestionListener>();
			mapListener.put(ik, map);
		}
		map.put(rdn, listener);
	}
	
	public void removeQuestionTaskListener(IKnowledgeBaseInfo ik,String rdn) throws ApplicationException{
		HashMap<String,IQuestionListener> map=mapListener.get(ik);
		if(map==null){
			mapListener.remove(ik);
			throw new ApplicationException(ApplicationException.ERROR_ASIGN,"Error grafico QuestionTaskManager Remove map");
		}

		map.remove(rdn);
		if(map.isEmpty())
			mapListener.remove(ik);
	}

	class RequestInfo{
		int id;
		String rdn;
		ArrayList<IndividualValues> data;
		HashMap<String,String> alias;
		
		public RequestInfo(int id,String rdn,ArrayList<IndividualValues> data, HashMap<String,String> alias){
			this.id=id;
			this.rdn=rdn;
			this.data=data;
			this.alias=alias;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public ArrayList<IndividualValues> getData() {
			return data;
		}

		public void setData(ArrayList<IndividualValues> data) {
			this.data = data;
		}

		public String getRdn() {
			return rdn;
		}

		public void setRdn(String rdn) {
			this.rdn = rdn;
		}

		public HashMap<String,String> getAlias() {
			return alias;
		}

		public void setAlias(HashMap<String,String> alias) {
			this.alias = alias;
		}

		
	}
}
