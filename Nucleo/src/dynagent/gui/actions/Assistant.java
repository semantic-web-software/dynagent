package dynagent.gui.actions;

import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.AssignValueException;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdom.JDOMException;


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
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.SwingWorker;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;
import dynagent.gui.StatusBar;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.gui.utils.Converter;

public class Assistant implements ActionListener, WindowListener, MouseListener{

	private ActionIterator itrStep;
	private ActionManager actionManager;
	private WindowComponent dialog;
//	private static String PREVIOUS_BUTTON="previous";
//	private static String NEXT_BUTTON="next";
	private static int PREVIOUS_BUTTON=botoneraAccion.CANCEL;
	private static int NEXT_BUTTON=botoneraAccion.EJECUTAR;
	//private JPanel previousButtonPanel;
	//private JPanel nextButtonPanel;
	//private JButton previousButton;
	//private JButton nextButton;
	private IFormData currentFormData;
	//private Component componentFocusParent;

	private ITableNavigation tableNavigation;
	//private Container container;
	//private JDialog parent;
	private ArrayList<IFormData> listFormData;
	
	private KnowledgeBaseAdapter kba;
	private boolean multiWindow;
	private boolean modalWindow;
	
	
	public Assistant(ActionIterator itrStep,KnowledgeBaseAdapter kba,WindowComponent parent,ITableNavigation tableNavigation, boolean modalWindow){
		this.itrStep=itrStep;
		//this.parent=parent;
		actionManager=Singleton.getInstance().getActionManager();
		this.kba=kba;
		
		this.multiWindow=actionManager.isMultiWindow();
		this.modalWindow=modalWindow;
		
		if(multiWindow){
			if(/*parent.getMainDialog()==parent*/!modalWindow){
				JFrame j=new JFrame();
				j.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				j.addWindowListener(this);
				j.setResizable(false);
				dialog=new WindowComponent(j,parent,kba);
			}else{
				JDialog d=new JDialog(parent.getComponent());
				d.setModalityType(ModalityType.MODELESS);
				d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				d.addWindowListener(this);
				d.setResizable(false);
				dialog=new WindowComponent(d,parent,kba);
			}
		}else{
			JDialog d=new JDialog(parent.getComponent());
			d.setModalityType(ModalityType.DOCUMENT_MODAL);
			d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			d.addWindowListener(this);
			d.setResizable(false);
			dialog=new WindowComponent(d,parent,kba);
		}
		
		dialog.setMainDialog(parent.getMainDialog());
		
		this.tableNavigation=tableNavigation;
		//container = (Container)parent.getGlassPane();//getContentPane();
		listFormData=new ArrayList<IFormData>();
		
	}
	
	public void start() throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, AWTException, JDOMException, ActionException{
		//buildPreviousPanel();
		//buildNextPanel();
		boolean success=false;
		try {
			//componentFocusParent=KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
			exeNextStep();
			
			success=true;
		} finally{
			if(!success)
				doRollbackSession();
		}
	}
	
	private void doRollbackSession() throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException, AWTException{
		System.err.println("Assistant: Hace rollback de la sesion");
		commandPath command=itrStep.m_commandList.get(itrStep.m_currentStep);
		command.getSession().rollBack();
		if(!dialog.getComponent().isShowing()){//Si no ha llegado a mostrarse le quitamos el windowListener porque si no este enlace evita liberar la memoria del motor al descartarlo
			dialog.getComponent().removeWindowListener(this);
		}
		recoverContainer();
	}
	
	private void exeNextStep() throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException, AWTException, ActionException{
		//actionManager.exeNextStep(itrStep,dialog);
		if(itrStep.hasNext()){
			commandPath command=itrStep.next();
			//System.err.println(command);
			
			//updateButtonsPanels();
			boolean success=false;
			try{
				currentFormData=actionManager.exeStep(command,kba,dialog,itrStep,tableNavigation,buildPreviousPanel(),buildNextPanel(), itrStep.isLastStep()?null:this);
				if(currentFormData!=null){
					JComponent component=currentFormData.getComponent();
					if(listFormData.isEmpty()){//Si se trata del primero de la secuencia deshabilitamos el container padre, no tiene sentido hacerlo en todos los pasos ya que el padre siempre es el mismo
						disabledContainer();
					}
					listFormData.add(currentFormData);
					actionManager.showDialog(dialog, component);
				}
				Singleton.getInstance().getQuestionTaskManager().addCurrent(currentFormData, currentFormData.getDialog(), kba);
				success=true;
			}finally{
				if(!success && itrStep.hasPrevious()){
					itrStep.setCancelStep(kba,currentFormData);
					itrStep.previous();
				}
			}
		}
	}
	
	private void exePreviousStep() throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException{
		//actionManager.exePreviousStep(itrStep,dialog);
		if(itrStep.hasPrevious()){
			/*commandPath command=*/itrStep.previous();//Llamamos al previo, aunque no lo utilicemos, para que se actualice el indice
			//System.err.println(command);
			
			//updateButtonsPanels();
			//currentFormData=actionManager.exeStep(command,dialog,itrStep,previousButtonPanel,buttonPanel,nextButtonPanel, this);
			
			listFormData.remove(currentFormData);
			currentFormData=listFormData.get(listFormData.size()-1);
			
			JComponent component=currentFormData.getComponent();
			actionManager.showDialog(dialog, component);
			
			Singleton.getInstance().getQuestionTaskManager().addCurrent(currentFormData, currentFormData.getDialog(), kba);
		}
	}
	
	/*private void updateButtonsPanels(){
		if(!itrStep.hasPrevious() && !itrStep.hasNext()){
			nextButton.setVisible(false);
			previousButton.setVisible(false);
		}else{
			if(itrStep.hasNext())
				nextButton.setEnabled(true);
			else nextButton.setEnabled(false);
			
			if(itrStep.hasPrevious())
				previousButton.setEnabled(true);
			else previousButton.setEnabled(false);
		}
	}*/
	
/*	private JPanel buildPreviousPanel(){
		previousButtonPanel=new JPanel();
		previousButtonPanel.setBackground(m_colorFondo);
		//previousPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		previousButton=botoneraAccion.subBuildBoton(previousButtonPanel, "Anterior", null, PREVIOUS_BUTTON, null, this, 20, 20, false);
		previousButtonPanel.add(previousButton);
		return previousButtonPanel;
	}
	
	private JPanel buildNextPanel(){
		nextButtonPanel=new JPanel();
		nextButtonPanel.setBackground(m_colorFondo);
		//nextPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		nextButton=botoneraAccion.subBuildBoton(nextButtonPanel, "Siguiente", null, NEXT_BUTTON, null, this, 20, 20, false);
		nextButtonPanel.add(nextButton);
		return nextButtonPanel;
	}
*/
	private JPanel buildPreviousPanel(){
		JPanel previousButtonPanel=new JPanel();
	
		//previousPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		IdOperationForm idOperationForm=new IdOperationForm();
		idOperationForm.setButtonType(PREVIOUS_BUTTON);
		JButton previousButton=botoneraAccion.subBuildBoton(previousButtonPanel, "Anterior", null, idOperationForm.getIdString(), null, this, 0, (int)botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics()), false, kba.getServer());
		previousButtonPanel.add(previousButton);
		if(!itrStep.hasPrevious() && !itrStep.hasNext()){
			//nextButton.setVisible(false);
			previousButton.setVisible(false);
		}else{
			/*if(itrStep.hasNext())
				nextButton.setEnabled(true);
			else nextButton.setEnabled(false);
			*/
			if(itrStep.hasPrevious())
				previousButton.setEnabled(true);
			else previousButton.setEnabled(false);
		}
		return previousButtonPanel;
	}
	
	private JPanel buildNextPanel(){
		JPanel nextButtonPanel=new JPanel();
	
		//nextPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		IdOperationForm idOperationForm=new IdOperationForm();
		idOperationForm.setButtonType(NEXT_BUTTON);
		JButton nextButton=botoneraAccion.subBuildBoton(nextButtonPanel, "Siguiente", null, idOperationForm.getIdString(), null, this, 0, (int)botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics()), false, kba.getServer());
		nextButtonPanel.add(nextButton);
		if(!itrStep.hasPrevious() && !itrStep.hasNext()){
			nextButton.setVisible(false);
			//previousButton.setVisible(false);
		}else{
			if(itrStep.hasNext())
				nextButton.setEnabled(true);
			else nextButton.setEnabled(false);
			
			/*if(itrStep.hasPrevious())
				previousButton.setEnabled(true);
			else previousButton.setEnabled(false);*/
		}
		return nextButtonPanel;
	}

	public void actionPerformed(ActionEvent ae) {
		dialog.disabledEvents();
		String actionCommand=ae.getActionCommand();
		final int buttonType=new IdOperationForm(actionCommand).getButtonType();
		SwingWorker worker=new SwingWorker(){//En un nuevo hilo para que si tarda muchos se pueda seguir trabajando en la aplicación
			public Object construct(){										
				boolean success=false;
				try{
					if(buttonType==PREVIOUS_BUTTON){
						if(itrStep.setCancelStep(kba,currentFormData))
							exePreviousStep();
					}else if(buttonType==NEXT_BUTTON){
							if(itrStep.setResultStep(kba,currentFormData))
								exeNextStep();
					}
					success=true;
				}catch(OperationNotPermitedException e){
					e.printStackTrace();
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
				}catch(ActionException e){
					e.printStackTrace();
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
				}catch(Exception e){
					e.printStackTrace();
					Singleton.getInstance().getComm().logError(dialog.getComponent(),e,"Error al crear el anterior/siguiente formulario");
				}finally{
					if(!success)
						doFinished();
				}
				
				return success;
			}

			public void finished(){
				//Lo invocamos mas tarde porque si no está habilitado antes de que el dialog cambie de formulario
				final Runnable doFinished = new Runnable() {
					public void run() {
						dialog.enabledEvents();
					}
				};
				SwingUtilities.invokeLater(doFinished);
			}
		};
		worker.start();
		
	}
	
	public void windowClosed(WindowEvent e) {
		try {
			recoverContainer();
			//if(componentFocusParent!=null)
			//	componentFocusParent.requestFocusInWindow();
			
			Iterator<IFormData> itr=listFormData.iterator();
			while(itr.hasNext()){
				IFormData formData=itr.next();
				if(formData instanceof IHistoryDDBBListener)
					kba.removeHistoryDDBBListener((IHistoryDDBBListener)formData);
			}
			listFormData.removeAll(listFormData);
			
//			StatusBar statusBar = Singleton.getInstance().getStatusBar();
//			if(statusBar.getNivelLocalizacion()==2)
//			//if(statusBar.getNivelLocalizacion()>0)
//				 statusBar.upNivelLocalizacion();
			
			dialog.getComponent().removeWindowListener(this);
			
			Singleton.getInstance().getQuestionTaskManager().removeCurrent(kba);
			
		}catch(Exception ex){
			Singleton.getInstance().getComm().logError(dialog.getComponent(),ex,"Error al cerrar la ventana");
	        ex.printStackTrace();
		}	
	}

	public void windowClosing(WindowEvent e) {
		try {
			//Si el glaspane esta activo no permitimos cerrar ya que esta ejecutando alguna accion y seria un problema para las sesiones
			if(!dialog.getGlassPane().isVisible() && currentFormData.cancel()){
				recoverContainer();
				//if(componentFocusParent!=null)
				//	componentFocusParent.requestFocusInWindow();
//				StatusBar statusBar = Singleton.getInstance().getStatusBar();
//				if(statusBar.getNivelLocalizacion()==2)
//				//if(statusBar.getNivelLocalizacion()>0)
//				 	statusBar.upNivelLocalizacion();
				/*No deregistrarse
				 Eso parece que fue necesario en el 2009 por un problema con las ventanas modales, 
				 ya que se llamaba a windowClosed cuando el formulario padre cerraba 
				 (Ya que cerrar la ventana modal no hace dispose de la ventana sino que la oculta llamando a windowClosing pero no a windowClosed, 
				 pero al cerrar la ventana padre hace dispose de las hijas y eso parece que daba problemas). 
				 Pero despues se añadió que la llamada a form.cancel() terminara haciendo dispose tambien de la ventana asi que windowClosed 
				 se llama justo cuando termina windowClosing y hace lo que tiene que hacer.

				Llamar desde aqui a windowsClosed tenia el problema de que se llamaba a windowClosed incluso aunque luego no se hiciera el cancel 
				(Por glassPane o porque el usuario responde que no a la pregunta de si esta seguro de cancelar). 
				Eso puede dar problemas con el asistente ya que borra de la lista las referencias a las ventanas abiertas y temas de Question tasks.
				dialog.getComponent().removeWindowListener(this);
				*/
			}
		}catch(Exception ex){
			Singleton.getInstance().getComm().logError(dialog.getComponent(),ex,"Error al cerrar la ventana");
	        ex.printStackTrace();
		}		
	}

	public void windowActivated(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}	
	
	public void disabledContainer() throws AWTException{
		if(dialog.getParentDialog()!=null && (!multiWindow || /*dialog.getParentDialog()!=dialog.getMainDialog()*/modalWindow)){						
//			Converter converter = new Converter(dialog.getParentDialog());
//			JImagePanel panel = new JImagePanel(converter.getOut());
//			//container = parent.getContentPane();
//			//dialog.getParentDialog().setContentPane(panel);
//			dialog.getParentDialog().setGlassPane(panel);
//			panel.setVisible(true);
//			dialog.getParentDialog().getComponent().validate();
//			dialog.getParentDialog().getComponent().repaint();
			dialog.getParentDialog().setEnabledContainer(false);
		}
	}
	public void recoverContainer() throws AWTException{
		if(dialog.getParentDialog()!=null && (!multiWindow || /*dialog.getParentDialog()!=dialog.getMainDialog()*/modalWindow)){
//			//dialog.getParentDialog().setContentPane(container);
//			dialog.getParentDialog().setGlassPane(container);
//			container.setVisible(false);
//			dialog.getParentDialog().getComponent().validate();
//			dialog.getParentDialog().getComponent().repaint();
			dialog.getParentDialog().setEnabledContainer(true);
		}
	}

	public void mouseClicked(MouseEvent ev) {
		try{
			dialog.disabledEvents();
			if(ev.getClickCount()==2){
				if(itrStep.setResultStep(kba,currentFormData))
					exeNextStep();
			}
		}catch(OperationNotPermitedException e){
			Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
		}catch(ActionException e){
			e.printStackTrace();
			Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
		}catch(Exception e){
			Singleton.getInstance().getComm().logError(dialog.getComponent(),e,"Error al crear el siguiente formulario");
			e.printStackTrace();
		}finally{
			dialog.enabledEvents();
		}
	}

	public void mouseEntered(MouseEvent e){}

	public void mouseExited(MouseEvent e){}

	public void mousePressed(MouseEvent e){}

	public void mouseReleased(MouseEvent e){}
}

