package dynagent.gui.utils;

import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GProcessedForm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import dynagent.common.communication.communicator;
import dynagent.common.exceptions.FileException;
import dynagent.common.knowledge.access;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.xml.XMLTransformer;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;

public class ImportComponent extends JPanel{

	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JEditorPane html=null;
	private int ancho=500, alto=380;
	private int dimBoton = ConstantesGraficas.intToolY-3;

	public ImportComponent(WindowComponent dialog_parent){
		super();
		parent=dialog_parent;

		final communicator comm=Singleton.getInstance().getComm();
		
		final String idExcelFile="1";
		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		JButton b = new JButton(Singleton.getInstance().getComm().getIcon("import"));
		b.setToolTipText("Importar Datos");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				try {
					parent.disabledEvents();
					
					GProcessedForm viewForm=formFactory.buildImportForm(idExcelFile);
					
					ArrayList<GProcessedForm> listaViewForm=new ArrayList<GProcessedForm>();
					listaViewForm.add(viewForm);

					final FormManager form= new FormManager( /*null,*/
						
							null,//componentListener,
							null,
							null,//actionListener,
							0,					
							"IMPORTAR DATOS",					
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
							String command = e.getActionCommand();
							//System.out.println("OPGUI:COMMAND:" + command);

							IdOperationForm idOperation=new IdOperationForm(command);
							Integer buttonType = idOperation.getButtonType();
							
							if (buttonType == botoneraAccion.CANCEL) {
								dialog.dispose();
							}else if(buttonType == botoneraAccion.EJECUTAR){
								String excelFilePath=(String)form.getValueComponent(idExcelFile);
								File dataFile = new File(excelFilePath);
								String xsltName = dataFile.getName().substring(0, dataFile.getName().indexOf("."))+".xsl";
								
								URL url = new URL(Singleton.getInstance().getComm().getXsltUrl(xsltName));
								
								InputStream xsltInput;
								try {
									xsltInput = url.openStream();
								} catch (IOException e1) {
									e1.printStackTrace();
									Singleton.getInstance().getMessagesControl().showErrorMessage("El archivo seleccionado no se corresponde con ninguna importación válida.\nAsegúrese de no haber cambiado el nombre de la plantilla original.", dialog.getComponent());
									return;
								}
								
								Document document=null;
								try {
									document = XMLTransformer.getTransformedDocument(dataFile, xsltInput,null);
								} catch (Exception ex) {
									String msg="";
									if(ex.getCause() instanceof SAXException){
										SAXException sa=(SAXException)ex.getCause(); 
										if(sa!=null) msg=sa.getLocalizedMessage();
									}
									ex.getCause().printStackTrace();
									ex.printStackTrace();
									Singleton.getInstance().getMessagesControl().showErrorMessage(msg+"\n"+ex.getMessage()+"\n"+"El archivo seleccionado no ha podido ser transformado.\nAsegúrese de haberlo rellenado correctamente y que su extensión sea xls", dialog.getComponent());
									return;
								}
								//asigno como fuente de replica IMPORT, al no ser nula obliga a pasar por setLocalIdos en InstanceEJB, y al ser IMPORT no admite 
								//convertir set por new automaticament cuando el objejto no existe, eso es necesario por ejemplo para compras.xsl donde 
								//se usa set en proveedor para evitar crear basura por error
								comm.serverTransitionObject(comm.getUser(),document,null,null,true,true,false,"IMPORT");
								
								Singleton.getInstance().getMessagesControl().showMessage("Importación realizada correctamente", dialog.getComponent());
								
								dialog.dispose();
							}else if(buttonType == botoneraAccion.HELP){
								comm.showImportHelpPage();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							Singleton.getInstance().getComm().logError(dialog.getComponent(),ex,"Error al intentar importar");
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
						Singleton.getInstance().getComm(),null,false/*Da igual false o true, en este caso no hace nada*/);
				
				JPanel panelAux=new JPanel(new BorderLayout());
				//JPanel labelPanel=new JPanel();
				//labelPanel.add(new JLabel("<html><body><center><a href=http://www.dynagent.es>Pulse aqui para descargar las plantillas de importación</a></center></body></html>"));
				//panelAux.add(labelPanel,BorderLayout.NORTH);
				panelAux.add(form.getComponent(),BorderLayout.CENTER);
				JPanel panelAuxBotonera=new JPanel();
				panelAuxBotonera.add(botonera.getComponent());
				panelAux.add(panelAuxBotonera,BorderLayout.SOUTH);
					JDialog d=new JDialog(parent.getComponent());
					d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					d.setModal(true);				
					//d.setSize(new Dimension(ancho, alto));
					//d.setPreferredSize(new Dimension(ancho, alto));
					d.setResizable(false);
					d.setContentPane(panelAux);
					d.pack();
					
					dialog = new WindowComponent(d,parent,parent.getKnowledgeBase());
					dialog.setTitle("Importación de datos");
					dialog.setMainDialog(parent.getMainDialog());
					dialog.setLocationRelativeTo(parent.getComponent());
					dialog.getComponent().setVisible(true);
					
				}catch(Exception ex){
					ex.printStackTrace();
				} finally{
					parent.enabledEvents();
				}
			}
		});
		b.setBounds(/*anchoPanel-dimBoton*/1, /*(ConstantesGraficas.intToolY-3)/2-dimBoton/2*/1, dimBoton, dimBoton);
		add(b);
	}

}
