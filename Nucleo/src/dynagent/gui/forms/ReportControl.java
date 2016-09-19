package dynagent.gui.forms;

import gdev.gbalancer.GProcessedForm;
import gdev.gen.AssignValueException;
import gdev.gen.IComponentListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class ReportControl extends transitionControl{
	private instance m_instanceFilter;
	private FormManager m_formManagerConfig;
	private int idoUserTaskReport;

	public ReportControl(Session ses,
			Integer userRol,
			int idoParent,
			int ido,
			int idto,
			Integer idtoUserTask,
			int operation,
			Dimension dim,
			JPanel botonera, KnowledgeBaseAdapter kba,WindowComponent dialog) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{


		super(ses, userRol, idoParent, ido, idto, idtoUserTask, operation, dim, botonera, kba, dialog, null, null, true, true, null);
		idoUserTaskReport=m_kba.getIdoUserTaskReport(m_idtoUserTask);
		buildFormConfigReport();
		m_kba.setValue(/*property,*/idoParent,Constants.IdPROP_PARAMS, m_kba.buildValue(ido,m_idto),null/*, new session()*/,/*operation*/userRol,idtoUserTask,ses);
		m_instanceFilter = m_kba.getTreeObjectReport(m_ido, userRol, idtoUserTask, ses);
		onlyFirstLevelColumnsTable=true;//Con esto evitamos que se carguen mas niveles innecesarios en las tablas, evitando consultas a base de datos para obtenerlos
	}
	
	
	
	private void buildFormConfigReport() throws ParseException, AssignValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, NotFoundException{
		Dimension dim=m_preferredSize;//m_formManager.getComponent().getPreferredSize();
		//GProcessedForm viewForm=formFactory.buildFormConfigReport(idPreviewView,!m_kba.isDirectImpresion(m_kba.getIdoUserTaskReport(m_idtoUserTask)),dim);
		Iterator<Property> itrProperties=m_kba.getProperties(idoUserTaskReport, m_idtoUserTask, m_userRol, m_idtoUserTask, m_session);
		ArrayList<Property> properties=new ArrayList<Property>();
		while(itrProperties.hasNext()){
			Property prop=itrProperties.next();
			if(prop.getIdProp().equals(Constants.IdPROP_REPORT_PREVIEW) || prop.getIdProp().equals(Constants.IdPROP_REPORT_FORMAT))
				properties.add(prop);
		}
		ArrayList<GProcessedForm> listaViewForm=formFactory.buildFormulario(m_kba,dim, properties, m_idtoUserTask, null, idoUserTaskReport, m_userRol, m_session, true, false, false, access.SET, true, false, false, null, new Insets(0,0,0,0), new Insets(0,0,0,0), isAllowedConfigTables());

		m_formManagerConfig= new FormManager(
				this,
				this,
				this,
				m_actionID,
				"REPORTCONFIG",
				listaViewForm,
				(m_operation ==  access.NEW ),
				false,
				(m_operation ==  access.VIEW),
				true,
				true,
				null, dim, m_kba,dialog,
				m_idtoUserTask,m_userRol,m_session);
		
		JPanel panelConfig=m_formManagerConfig.getComponent();
		JPanel panelForm=m_formManager.getComponent();
		JPanel panelAux=new JPanel(new BorderLayout(0,0));
		panelAux.add(panelForm,BorderLayout.CENTER);
		panelAux.add(panelConfig,BorderLayout.SOUTH);
		m_form.add(panelAux, BorderLayout.CENTER);

	}


	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
		String mensajeRespuesta=null;
		try {
			m_kba.checkCoherenceObject(m_ido, m_userRol, m_kba.getServer().getUser(), m_idtoUserTask, m_session);
			
			boolean currDebugVal = Singleton.getInstance().getDebugLog().getEnableDebug();
			Singleton.getInstance().getDebugLog().setEnableDebug(false);
			try {
				String previewViewString=(String)m_kba.getValueData(m_kba.getField(idoUserTaskReport, m_idtoUserTask, Constants.IdPROP_REPORT_PREVIEW, m_userRol, m_idtoUserTask, m_session));
				boolean previewView=new Boolean(previewViewString.split(":")[0]);
				int format=m_kba.getIdoValue(m_kba.getChild(idoUserTaskReport, m_idtoUserTask, Constants.IdPROP_REPORT_FORMAT, m_userRol, m_idtoUserTask, m_session));
				HashMap<String,String> oidReport=m_kba.getReport(m_kba.getQueryXML(m_instanceFilter, new ArrayList<SelectQuery>(), m_userRol, m_idtoUserTask,null), false,idoUserTaskReport, m_idtoUserTask, m_kba.getLabelClassWithoutAlias(m_idtoUserTask), !previewView, format, m_userRol, m_idtoUserTask, m_session,0,false);
					if(oidReport!=null){
						
						m_com.showReport("_blank",oidReport,false,true);
						
						mensajeRespuesta="Informe mostrado";
						DataProperty propConfirm=m_kba.getField(m_idoParent, m_idtoParent, Constants.IdPROP_CHECKPRINTING, m_userRol, m_idtoUserTask, m_session);
						
						if(m_kba.getValueData(propConfirm)!=null){
							Object[] options = {"Sí", "No"};
							int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
									"¿El informe ha sido impreso en papel?",
									Utils.normalizeLabel("CONFIRMACIÓN DE IMPRESIÓN"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE,
									null,
									options,
									options[1],dialog.getComponent());
	
							if (res == JOptionPane.YES_OPTION){
								//System.err.println("Lo haceee siendo idto:"+m_kba.getClass(m_idoParent));
								DefaultSession sessionConfirm=m_kba.createDefaultSession(m_kba.getDDBBSession(),m_idtoUserTask,true,true,true,true,true);
								//sessionConfirm.addIchangeProperty(this, false);
								boolean success=false;
								try{
									m_kba.setState(m_idoParent,m_idtoParent,Constants.INDIVIDUAL_REALIZADO,m_userRol,m_idtoUserTask,sessionConfirm);
									sessionConfirm.commit();
									success=true;
								}finally{
									if(!success){
										sessionConfirm.setForceParent(false);
										sessionConfirm.rollBack();
									}
								}
							}
						}
					}else mensajeRespuesta="Informe no encontrado";
					//No nos interesa que compruebe si el usuario o el sistema han hecho cambios
					askCancel=false;
					cancel();
			}finally {
				Singleton.getInstance().getDebugLog().setEnableDebug(currDebugVal);
			}
		} catch (CardinalityExceedException e) {
			Property prop=e.getProp();
			String message=e.getUserMessage();
			if (prop!=null){
				if (prop.getIdo()!=m_ido){
					message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask) + " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, m_session))+"'";	
				}else{
					message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
				}
				
			}
			Singleton.getInstance().getMessagesControl().showErrorMessage(message,dialog.getComponent());
		}

		return mensajeRespuesta;
	}
	
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) {
		//System.err.println("Cambia valor en ido:"+ido+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" el value:"+value+" y el valueold:"+oldValue);
		try{
			super.changeValue(ido, idto, idProp, valueCls, value, oldValue, level, operation);
			if(m_instanceFilter!=null){//No nos interesa cuando aun no se ha construido el instance
				
				Property property;
				m_kba.setInstance(m_instanceFilter);
				try{
					property = m_kba.getProperty(ido/* m_id */, idto, idProp,m_userRol, m_idtoUserTask, null);
				}finally{
					m_kba.clearInstance();
				}
				//boolean changes=false;
				//System.err.println("Property:"+property);
				if(property!=null){//Significa que es de las properties del instance por lo que la tratamos
					m_kba.setInstance(m_instanceFilter);
					try{
						m_kba.setValue(ido,idProp,value,oldValue, m_userRol,m_idtoUserTask,null);
					}finally{
						m_kba.clearInstance();
					}
				}
			}
		}catch(Exception e){
			Singleton.getInstance().getComm().logError(dialog.getComponent(),e,"Error en un cambio de valor");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isAllowedConfigTables(){
		return false;
	}
	
}
