package dynagent.gui.utils;

import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GProcessedForm;
import gdev.gen.AssignValueException;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JPanel;

import dynagent.common.knowledge.access;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;

public class EmailForm{

	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JPanel panel;

	public EmailForm(final EmailRequest emailRequest,WindowComponent dialog_parent,final KnowledgeBaseAdapter kba) throws ParseException, AssignValueException{
		super();
		
		parent=dialog_parent;
		
		parent.disabledEvents();
		try {
			final String idEmail="1";
			final String idSubject="2";
			final String idBody="3";
			
					String email=emailRequest.getEmail();
					String subject=emailRequest.getSubject();
					String body=emailRequest.getBody();
					
					GProcessedForm viewForm=formFactory.buildEmailForm(idEmail, email, idSubject, subject, idBody, body);
					
					ArrayList<GProcessedForm> listaViewForm=new ArrayList<GProcessedForm>();
					listaViewForm.add(viewForm);
	
					final FormManager form= new FormManager( /*null,*/
						
							null,//componentListener,
							null,
							null,//actionListener,
							0,					
							"EMAIL",					
							listaViewForm,
							true,
							false,
							false,
							true,
							true,
							null, ConstantesGraficas.dimInit, null,/*window*/null, null, null, null);
					
					int operation=access.SET;
					int buttonsType=botoneraAccion.RECORD_TYPE;
					AccessAdapter accessAdapter=null;//new AccessAdapter(accessUserTasks,null);
	
				IdObjectForm idObjectOperation=new IdObjectForm();
				String idString=idObjectOperation.getIdString();
	
				ActionListener handler = new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						try {
							dialog.disabledEvents();
							String command = e.getActionCommand();
							//System.out.println("OPGUI:COMMAND:" + command);
	
							IdOperationForm idOperation=new IdOperationForm(command);
							Integer buttonType = idOperation.getButtonType();
							
							if (buttonType == botoneraAccion.CANCEL) {
								dialog.dispose();
							}else if(buttonType == botoneraAccion.EJECUTAR){
										
								emailRequest.setEmail((String)form.getValueComponent(idEmail));
								emailRequest.setSubject((String)form.getValueComponent(idSubject));
								emailRequest.setBody((String)form.getValueComponent(idBody));
								
								if(emailRequest.getEmail()==null || emailRequest.getBody()==null || emailRequest.getSubject()==null){
									Singleton.getInstance().getMessagesControl().showMessage("DEBE RELLENAR TODOS LOS CAMPOS",dialog.getComponent());		
									return;
								}
								
								kba.sendEmail(emailRequest, true);
								
								
								dialog.dispose();
							}else if(buttonType == botoneraAccion.HELP){
								Singleton.getInstance().getMessagesControl().showMessage("Este formulario enviará un email utilizando los datos del registro seleccionado y los rellenados en este formulario.\nPodrá consultar los datos del envio en log email que está situado en el área de configuración.",dialog.getComponent());
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							Singleton.getInstance().getComm().logError(dialog.getComponent(),ex,"Error al intentar enviar el email");
						} finally{
							dialog.enabledEvents();
						}
					}
				};
				
				botoneraAccion botonera=new botoneraAccion(
						idString,null,null,null,null,null,null,null,null,false,
						buttonsType,
						null, null,
						null,
						handler,
						accessAdapter,
						(operation==access.VIEW),
						true,
						Singleton.getInstance().getGraphics(),
						Singleton.getInstance().getComm(),null,kba.canSetUpColumnProperty());
				
				panel=new JPanel(new BorderLayout());
				//JPanel labelPanel=new JPanel();
				//labelPanel.add(new JLabel("<html><body><center><a href=http://www.dynagent.es>Pulse aqui para descargar las plantillas de importación</a></center></body></html>"));
				//panelAux.add(labelPanel,BorderLayout.NORTH);
				panel.add(form.getComponent(),BorderLayout.CENTER);
				JPanel panelAuxBotonera=new JPanel();
				panelAuxBotonera.add(botonera.getComponent());
				panel.add(panelAuxBotonera,BorderLayout.SOUTH);
					
		} finally{
			parent.enabledEvents();
		}
	}
	
	public void show(){
		JDialog d=new JDialog(parent.getComponent());
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setModal(true);				
		//d.setSize(new Dimension(ancho, alto));
		//d.setPreferredSize(new Dimension(ancho, alto));
		d.setResizable(false);
		d.setContentPane(panel);
		d.pack();
		
		dialog = new WindowComponent(d,parent,parent.getKnowledgeBase());
		dialog.setTitle("Envío de email");
		dialog.setMainDialog(parent.getMainDialog());
		dialog.setLocationRelativeTo(parent.getComponent());
		dialog.getComponent().setVisible(true);
	}

}
