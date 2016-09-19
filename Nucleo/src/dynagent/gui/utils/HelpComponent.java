package dynagent.gui.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
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
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;

public class HelpComponent implements HyperlinkListener{

	private static String BACK="back";
	private static String NEXT="next";
	private static String EXTERNAL="external";
	
	private KnowledgeBaseAdapter kba;
	private communicator comm;
	private Dimension dimension;
	private HashMap<Window,JDialog> listDialog;
	private HashMap<JDialog,JEditorPane> listEditor;
	private HashMap<JEditorPane,HelpWindow> listHelpWindow;
	private JApplet applet;
	
	public HelpComponent(KnowledgeBaseAdapter kba, communicator comm, Dimension dimension, JApplet applet){
		super();
		this.dimension=dimension;
		this.applet=applet;
		listDialog=new HashMap<Window, JDialog>();
		listEditor=new HashMap<JDialog, JEditorPane>();
		listHelpWindow=new HashMap<JEditorPane, HelpWindow>();
		this.kba=kba;
		this.comm=comm;
	}
	
	public void showHelp(int idto,Integer idtoUserTask,Integer userRol,Window parentWindow) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		
		JDialog dialog=listDialog.get(parentWindow);
		if(dialog==null || !dialog.isVisible()/* || !Auxiliar.equals(dialog.getOwner(),parentWindow)*/){
			JEditorPane editorPane=new JEditorPane();
			editorPane.setBackground(UIManager.getColor("List.background"));
			editorPane.setContentType("text/html");
			editorPane.setEditable(false);
			editorPane.setBorder(BorderFactory.createEmptyBorder());
			editorPane.addHyperlinkListener(this);
			
			JScrollPane scrollPane = new JScrollPane(editorPane);
			scrollPane.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
			//scrollPane.getViewport().add(editorPane);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			//scrollPane.setPreferredSize(dim);
			
			JPanel panel=new JPanel(new BorderLayout());
			panel.add(scrollPane,BorderLayout.CENTER);
			panel.setBackground(editorPane.getBackground());
			
			if(dialog!=null)
				dialog.dispose();
			dialog=new JDialog(parentWindow){

				@Override
				public void dispose() {
					super.dispose();
					listDialog.remove(getOwner());
					listHelpWindow.remove(listEditor.get(this));
					listEditor.remove(this);
				}
				
			};
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			listDialog.put(parentWindow, dialog);
			listEditor.put(dialog, editorPane);
			HelpWindow helpWindow=new HelpWindow(editorPane,dialog);
			listHelpWindow.put(editorPane, helpWindow);
			//d.setSize(new Dimension(ancho, alto));
			dialog.setPreferredSize(new Dimension((int)dimension.getWidth(),(int)dimension.getHeight())/*new Dimension((int)dim.getWidth(),(int)parentWindow.getHeight())*/);
			//d.setResizable(false);
			dialog.setTitle(Utils.normalizeLabel("Ayuda"));
			dialog.setContentPane(panel);
			dialog.setIconImage(Singleton.getInstance().getComm().getIcon("icon").getImage());
		}
		JEditorPane editor=listEditor.get(dialog);
		
		String helpHtml=null;
		try{
		helpHtml=buildHelpHtml(idto, idtoUserTask, userRol);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(helpHtml!=null){
			HelpWindow helpWindow=listHelpWindow.get(editor);
			if(helpWindow.currentText<helpWindow.texts.size()-1){
				int size=helpWindow.texts.size();
				for(int i=helpWindow.currentText+1;i<size;i++)
					helpWindow.texts.remove(helpWindow.texts.size()-1);
			}
			helpWindow.texts.add(helpHtml);
			helpWindow.currentText++;
			
			String navigator=buildNavigatorHtml(editor);
			editor.setText(buildHtml(navigator,helpHtml));
			
			if(!dialog.isVisible()){
				dialog.pack();
				dialog.setLocationRelativeTo(parentWindow);
				Point point=applet.getLocationOnScreen();
				point.x+=applet.getWidth()-dialog.getPreferredSize().getWidth();
				dialog.setLocation(point);
				dialog.setVisible(true);
			}
			dialog.toFront();
		}
	}
	
	private String buildHelpHtml(int idto,Integer idtoUserTask,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String helpHtml=null;
		if(kba.isSpecialized(idto,Constants.IDTO_REPORT))
			helpHtml=buildReportHtml(idto, userRol);
		else if(kba.isSpecialized(idto,Constants.IDTO_ACTION))
			helpHtml=buildActionHtml(idto, userRol);
		else if(kba.isSpecialized(idto,Constants.IDTO_IMPORT))
			helpHtml=buildImportHtml(idto,userRol);
		else if(kba.isSpecialized(idto,Constants.IDTO_EXPORT))
			helpHtml=buildExportHtml(idto,userRol);
		else if(kba.isSpecialized(idto,Constants.IDTO_UTASK))
			helpHtml=buildUserTaskHtml(idto, userRol);
		else helpHtml=buildClassHtml(idto, idtoUserTask, userRol);
		
		return helpHtml;
	}
	
	private String buildHtml(String navigator,String help) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String navigatorHtml=navigator!=null?navigator:"";
		String helpHtml=help!=null?help:"";
		String html="<html><body><table align=center width='95%'><TR><TD>"+
					navigatorHtml+helpHtml+
					"</TR></TD></table></body></html>";
		
		return html;
	}
	
	private String buildNavigatorHtml(JEditorPane editor){
		String navigation="";
		HelpWindow helpWindow=listHelpWindow.get(editor);
		if(helpWindow!=null){
			boolean hasTable=false;
			if(helpWindow.currentText>0){
				navigation+="<table width='100%'>";
				navigation+="<TR>";
				navigation+="<TD align=left><a href='"+BACK+"'>Anterior</a></TD>";
				hasTable=true;
			}
			if(helpWindow.currentText<helpWindow.texts.size()-1){
				if(!hasTable){
					navigation+="<table width='100%'>";
					navigation+="<TR>";
					navigation+="<TD align=left></TD>";//Esto es necesario porque si no funciona el click en el lado izquierdo como si le dieramos a Siguiente
					hasTable=true;
				}
				navigation+="<TD align=right><a href='"+NEXT+"'>Siguiente</a></TD>";
			}
			
			if(hasTable){
				navigation+="</TR>";
				navigation+="</table>";
			}
		}
		return navigation;
	}
	
	private String buildClassHtml(int idto,Integer idtoUserTask,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String name=kba.getLabelClass(idto, idtoUserTask);
		String help=comm.serverGetClassDescription(idto);
		if(!help.isEmpty() && !help.startsWith("<p"))
			help="<p>"+help+"</p>";
		String helpHtml="<table align=center>" +
							"<TR><TD><H1>"+name+"</H1></TD>"+
							"<TD><img src='"+Singleton.getInstance().getComm().getURL("help.gif")+"'</TD></TR>"+
							"</table>"+
							help +
							"<br>";
		
		
		String auxHtml=null;
		if(kba.isSpecialized(idto, Constants.IDTO_ENUMERATED)){
			Iterator<Integer> itrInd=kba.getIndividuals(idto, Constants.LEVEL_INDIVIDUAL, false);
			HashMap<String,Integer> mapNameIdo=new HashMap<String, Integer>();
			while(itrInd.hasNext()){
				Integer ido=itrInd.next();
				String rdn=(String)kba.getValueData(kba.getRDN(ido, idto, userRol, idtoUserTask, kba.getDefaultSession()));
				if(auxHtml==null){
					auxHtml="<p>"+
					"Sus elementos son los siguientes:<br><br>"+
					"<table align=center border=1>"+
					"<TR>"+ 
					"<TH>Nombre</TH>"+
					"<TH>Descripción</TH>"+ 
					"</TR>";
				}
				mapNameIdo.put(rdn, ido);
				
				//TODO Falta mostrar para las dataProperties. Pero para evitar llamar a server por cada property usaremos el metodo(falta implementar) que devuelve todos los comentarios
			}
			ArrayList<String> listName=new ArrayList<String>(mapNameIdo.keySet());
			Collections.sort(listName,new Comparator<String>(){
	
				@Override
				public int compare(String s1, String s2) {
					return Constants.languageCollator.compare(s1,s2);
				}
				
			});
			
			HashMap<Integer, String> mapDescriptionIndividuals=comm.serverGetIndividualsDescriptionOfClass(idto);
			
			Iterator<String> itrName=listName.iterator();
			while(itrName.hasNext()){
				String nameIndividual=itrName.next();
				Integer ido=mapNameIdo.get(nameIndividual);
				String description=mapDescriptionIndividuals.get(ido);//comm.serverGetIndividualDescription(ido);
				if(description==null){
					description="";
				}
				auxHtml+="<TR>"+
					"<TD>"+nameIndividual+"</TD>"+
					"<TD>"+description+"</TD>"+
					"</TR>";
			}
			if(auxHtml!=null){
				auxHtml+="</table></p><br>";
				helpHtml+=auxHtml;
			}
		}
		
		auxHtml=null;
		Iterator<Property> itrProp=kba.getProperties(null, idto, userRol, idtoUserTask, kba.getDefaultSession());
		HashMap<String,Property> mapNameProperty=new HashMap<String, Property>();
		while(itrProp.hasNext()){
			Property prop=itrProp.next();
			if(prop.getTypeAccess().getViewAccess()){//TODO Ahora mismo esto no esta siendo correcto porque para las clases no se esta enviando bien los permisos en getProperties
				if(auxHtml==null){
					auxHtml="<p>"+
					"Sus propiedades son las siguientes:<br><br>"+
					"<table align=center border=1>"+
					"<TR>"+ 
					"<TH>Campo</TH>"+
					"<TH>Descripción</TH>"+
					"<TH>Tipo</TH>"+ 
					"</TR>";
				}
				String nameProperty=kba.getLabelProperty(prop, idto, idtoUserTask);
				mapNameProperty.put(nameProperty, prop);
			}
		}
		
		ArrayList<String> listName=new ArrayList<String>(mapNameProperty.keySet());
		Collections.sort(listName,new Comparator<String>(){

			@Override
			public int compare(String s1, String s2) {
				return Constants.languageCollator.compare(s1,s2);
			}
			
		});
		HashMap<Integer, String> mapDescriptionProperties=comm.serverGetPropertiesDescriptionOfClass(idto);
		Iterator<String> itrName=listName.iterator();
		while(itrName.hasNext()){
			String nameProperty=itrName.next();
			Property prop=mapNameProperty.get(nameProperty);
			if(prop instanceof ObjectProperty){
				ObjectProperty objectP=(ObjectProperty)prop;
				Integer idRange=kba.getIdRange(objectP);
				if(idRange==null)
					idRange=kba.getIdtoEnum(objectP);//Para cuando es un enumerado
				if(idRange!=null){
					//System.err.println("idRange:"+idRange);
					String nameRange=kba.getLabelClass(idRange, idtoUserTask);
					String description=mapDescriptionProperties.get(prop.getIdProp());//comm.serverGetPropertyDescription(prop.getIdProp());
					if(description==null){
						description="";
					}
					auxHtml+="<TR>"+
						"<TD>"+nameProperty+"</TD>"+
						"<TD>"+description+"</TD>"+
						"<TD><a href='"+idRange+"#"+idtoUserTask+"#"+userRol+"'>"+nameRange+"</a></TD>"+
						"</TR>";
				}
			}else{
				DataProperty dataP=(DataProperty)prop;
				String description=mapDescriptionProperties.get(prop.getIdProp());//comm.serverGetPropertyDescription(prop.getIdProp());
				if(description==null){
					description="";
				}
				//if(!description.isEmpty()){
					int dataType=dataP.getDataType();
					String typeRange="";
					switch(dataType){
						case Constants.IDTO_STRING:
						case Constants.IDTO_MEMO:
							typeRange="Texto";
							break;
						case Constants.IDTO_IMAGE:
							typeRange="Imagen";
							break;
						case Constants.IDTO_FILE:
							typeRange="Archivo";
							break;
						case Constants.IDTO_INT:
						case Constants.IDTO_DOUBLE:
							typeRange="Numérico";
							break;
						case Constants.IDTO_BOOLEAN:
							typeRange="Verdadero/Falso";
							break;
						case Constants.IDTO_DATE:
							typeRange="Fecha";
							break;
						case Constants.IDTO_DATETIME:
							typeRange="Fecha con horas, minutos y segundos";
							break;
						case Constants.IDTO_TIME:
							typeRange="Horas, minutos y segundos";
							break;
					}
					auxHtml+="<TR>"+
						"<TD>"+nameProperty+"</TD>"+
						"<TD>"+description+"</TD>"+
						"<TD>"+typeRange+"</TD>"+
						"</TR>";
				}
			//}
		}
		if(auxHtml!=null){
			auxHtml+="</table></p><br>";
			helpHtml+=auxHtml;
		}
		
		auxHtml=null;
		Iterator<Integer> itrUserTasks=kba.getIdtoUserTasks(idto, null, false, true, false).iterator();
		while(itrUserTasks.hasNext()){
			int idtoUtask=itrUserTasks.next();

			if(auxHtml==null){
				auxHtml="<p>Puede ser accedido directamente en el menú principal a partir de ";
			}else{
				auxHtml+=", ";
			}
			int areaFuncional=kba.getAreasFuncionales(kba.getIdoUserTask(idtoUtask)).get(0);
			String rdnAreaFuncional=(String)kba.getValueData(kba.getRDN(areaFuncional, Constants.IDTO_FUNCTIONAL_AREA, userRol, idtoUtask, kba.getDefaultSession()));
			auxHtml+="<a href='"+idtoUtask+"#"+idtoUserTask+"#"+userRol+"'>Menú("+rdnAreaFuncional+"): "+kba.getLabelUserTask(idtoUtask)+"</a>";
		}
		if(auxHtml!=null){
			auxHtml+=".</p><br>";
			helpHtml+=auxHtml;
		}
		
		auxHtml=null;
		Iterator<Integer> itrSpecialized=kba.getSpecialized(idto).iterator();
		while(itrSpecialized.hasNext()){
			int idtoSpec=itrSpecialized.next();
			if(!kba.isAbstractClass(idtoSpec)){
				if(auxHtml==null){
					auxHtml="<p align=center> <H3>Ayuda relacionada</H3>";
				}else{
					auxHtml+=", ";
				}
				auxHtml+="<a href='"+idtoSpec+"#"+idtoUserTask+"#"+userRol+"'>"+kba.getLabelClass(idtoSpec, idtoUserTask)+"</a>";
			}
		}
		if(auxHtml!=null){
			auxHtml+=".</p><br>";
			helpHtml+=auxHtml;
		}
		
		helpHtml+="<p align=center> <H3><a href='"+EXTERNAL+"'>Ayuda general</a> </H3> </p><br>";
		
		return helpHtml;
	}
	
	private String buildUserTaskHtml(int idtoUserTask,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String name=kba.getLabelUserTask(idtoUserTask);
		int idoUserTask=kba.getIdoUserTask(idtoUserTask);
		ObjectProperty propertyTarget=kba.getTarget(idoUserTask, idtoUserTask, userRol, kba.getDefaultSession());
		int idRange=kba.getIdRange(propertyTarget);
		int idtoRange=kba.getClass(idRange);
		String nameRange=kba.getLabelClass(idtoRange, idtoUserTask);
		int areaFuncional=kba.getAreasFuncionales(kba.getIdoUserTask(idtoUserTask)).get(0);
		String rdnAreaFuncional=(String)kba.getValueData(kba.getRDN(areaFuncional, Constants.IDTO_FUNCTIONAL_AREA, userRol, idtoUserTask, kba.getDefaultSession()));
		
		String help="<p>Esta opción del menú se encuentra en <b>"+rdnAreaFuncional+"</b> y permite realizar operaciones sobre <a href='"+idtoRange+"#"+idtoUserTask+"#"+userRol+"'>"+nameRange+"</a>.</p>";//Singleton.getInstance().getComm().getHelp(idto);
		
		// YA NO PEDIMOS INFORMACION A BASE DE DATOS PORQUE LA USERTASK ES CREADA DINÁMICAMENTE EN EL METADATA POR LO QUE NO HAY INFORMACIÓN DISPONIBLE NI CONOCIMIENTO DE ESTA USERTASK EN EL METAMODELO DEL SERVIDOR
//		String description=comm.serverGetClassDescription(idtoUserTask);
//		if(!description.isEmpty()){
//			if(!description.startsWith("<p"))
//				help+="<br><p>"+description+"</p>";
//			else help+="<br>"+description;
//		}
		
		String helpHtml="<table align=center>" +
							"<TR><TD><H1>Menú: "+name+"</H1></TD>"+
							"<TD><img src='"+Singleton.getInstance().getComm().getURL("help.gif")+"'</TD></TR>"+
							"</table>"+
							help +
							"<br>";
		String auxHtml=null;
		Iterator<Integer> itrUserTasks=kba.getIdtoUserTasks(idtoRange, null, false, true, false).iterator();
		while(itrUserTasks.hasNext()){
			int idtoUtask=itrUserTasks.next();
			if(idtoUtask!=idtoUserTask){
				if(auxHtml==null){
					auxHtml="<p align=center> <H3>Ayuda relacionada</H3>";
				}else{
					auxHtml+=", ";
				}
				int areaFunc=kba.getAreasFuncionales(kba.getIdoUserTask(idtoUtask)).get(0);
				String rdnAreaFunc=(String)kba.getValueData(kba.getRDN(areaFunc, Constants.IDTO_FUNCTIONAL_AREA, userRol, idtoUtask, kba.getDefaultSession()));
				auxHtml+="<a href='"+idtoUtask+"#"+idtoUserTask+"#"+userRol+"'>Menú("+rdnAreaFunc+"): "+kba.getLabelUserTask(idtoUtask)+"</a>";
			} 
		}
		if(auxHtml!=null){
			auxHtml+=".</p><br>";
			helpHtml+=auxHtml;
		}
		
		helpHtml+="<p align=center> <H3><a href='"+EXTERNAL+"'>Ayuda general</a> </H3> </p><br>";
		
		return helpHtml;
	}
	
	private String buildActionHtml(int idtoAction,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String name=kba.getLabelUserTask(idtoAction);
			
		String help="";
		String description=comm.serverGetClassDescription(idtoAction);
		if(!description.isEmpty()){
			if(!description.startsWith("<p"))
				help+="<br><p>"+description+"</p>";
			else help+="<br>"+description;
		}
		
		if(help.isEmpty()){
			int idoAction=kba.getIdoUserTaskAction(idtoAction);
			ObjectProperty propertyTarget=kba.getTarget(idoAction, idtoAction, userRol, kba.getDefaultSession());
			int idTarget=kba.getIdRange(propertyTarget);
			int idtoTarget=kba.getClass(idTarget);
			String nameTarget=kba.getLabelClass(idtoTarget, idtoAction);
			ObjectProperty propertySource=kba.getChild(idoAction, propertyTarget.getIdto(), Constants.IdPROP_SOURCECLASS, userRol, idtoAction, kba.getDefaultSession());
			int idSource=kba.getIdRange(propertySource);
			int idtoSource=kba.getClass(idSource);
			String nameSource=kba.getLabelClass(idtoSource, idtoAction);
			if(idtoTarget!=idtoSource)
				help="<p>Se trata de una acción que a partir de <a href='"+idtoSource+"#"+idtoAction+"#"+userRol+"'>"+nameSource+"</a> crea <a href='"+idtoTarget+"#"+idtoAction+"#"+userRol+"'>"+nameTarget+"</a>.</p>";
			else/* if(!kba.isAbstractClass(idtoTarget))*/
				help="<p>Se trata de una acción que modifica <a href='"+idtoTarget+"#"+idtoAction+"#"+userRol+"'>"+nameTarget+"</a>.</p>";
		}
		
		String helpHtml="<table align=center>" +
							"<TR><TD><H1>Acción: "+name+"</H1></TD>"+
							"<TD><img src='"+Singleton.getInstance().getComm().getURL("help.gif")+"'</TD></TR>"+
							"</table>"+
							help +
							"<br>";
		return helpHtml; 
	}
	
	private String buildReportHtml(int idtoReport,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String name=kba.getLabelUserTask(idtoReport);
			
		String help="";
		String description=comm.serverGetClassDescription(idtoReport);
		if(!description.isEmpty()){
			if(!description.startsWith("<p"))
				help+="<br><p>"+description+"</p>";
			else help+="<br>"+description;
		}
		
		String helpHtml=null;
		if(help.isEmpty()){
			int idoReport=kba.getIdoUserTaskReport(idtoReport);
			ObjectProperty propertyTarget=kba.getTarget(idoReport, idtoReport, userRol, kba.getDefaultSession());
			int idRange=kba.getIdRange(propertyTarget);
			int idtoRange=kba.getClass(idRange);
			String nameRange=kba.getLabelClass(idtoRange, idtoReport);
			help="<p>Se trata de un documento creado a partir de <a href='"+idtoRange+"#"+idtoReport+"#"+userRol+"'>"+nameRange+"</a>.</p>";
		}
		
		helpHtml="<table align=center>" +
				"<TR><TD><H1>Informe: "+name+"</H1></TD>"+
				"<TD><img src='"+Singleton.getInstance().getComm().getURL("help.gif")+"'</TD></TR>"+
				"</table>"+
				help +
				"<br>";
		
		return helpHtml; 
	}
	
	private String buildImportHtml(int idtoImport,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String name=kba.getLabelUserTask(idtoImport);
			
		String help="";
		String description=comm.serverGetClassDescription(idtoImport);
		if(!description.isEmpty()){
			if(!description.startsWith("<p"))
				help+="<br><p>"+description+"</p>";
			else help+="<br>"+description;
		}
		
		String helpHtml=null;
		if(help.isEmpty()){
			int idoImport=kba.getIdoUserTaskImport(idtoImport);
			ObjectProperty propertyTarget=kba.getTarget(idoImport, idtoImport, userRol, kba.getDefaultSession());
			int idRange=kba.getIdRange(propertyTarget);
			int idtoRange=kba.getClass(idRange);
			String nameRange=kba.getLabelClass(idtoRange, idtoImport);
			help="<p>Permite la importación en Dynagent de <a href='"+idtoRange+"#"+idtoImport+"#"+userRol+"'>"+nameRange+"</a>.</p>";
		}
		
		helpHtml="<table align=center>" +
				"<TR><TD><H1>Importación: "+name+"</H1></TD>"+
				"<TD><img src='"+Singleton.getInstance().getComm().getURL("help.gif")+"'</TD></TR>"+
				"</table>"+
				help +
				"<br>";
		
		return helpHtml; 
	}
	
	private String buildExportHtml(int idtoExport,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		String name=kba.getLabelUserTask(idtoExport);
			
		String help="";
		String description=comm.serverGetClassDescription(idtoExport);
		if(!description.isEmpty()){
			if(!description.startsWith("<p"))
				help+="<br><p>"+description+"</p>";
			else help+="<br>"+description;
		}
		
		String helpHtml=null;
		if(help.isEmpty()){
			int idoExport=kba.getIdoUserTaskExport(idtoExport);
			ObjectProperty propertyTarget=kba.getChild(idoExport, idtoExport, Constants.IdPROP_SOURCECLASS, userRol, idtoExport, kba.getDefaultSession());
			int idRange=kba.getIdRange(propertyTarget);
			int idtoRange=kba.getClass(idRange);
			String nameRange=kba.getLabelClass(idtoRange, idtoExport);
			help="<p>Permite la exportación de <a href='"+idtoRange+"#"+idtoExport+"#"+userRol+"'>"+nameRange+"</a></p>";
		}
		
		helpHtml="<table align=center>" +
				"<TR><TD><H1>Exportación: "+name+"</H1></TD>"+
				"<TD><img src='"+Singleton.getInstance().getComm().getURL("help.gif")+"'</TD></TR>"+
				"</table>"+
				help +
				"<br>";
		
		return helpHtml; 
	}

	public void hyperlinkUpdate(HyperlinkEvent hle) {
		try{
			if(hle.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
				JEditorPane editor=((JEditorPane)hle.getSource());
				//JScrollPane scroll=(JScrollPane)editor.getParent().getParent();
				//System.err.println("Description:"+hle.getDescription());
				if(hle.getDescription().equals(BACK)){
					HelpWindow helpWindow=listHelpWindow.get(editor);
					helpWindow.currentText--;
					String helpHtml=helpWindow.texts.get(helpWindow.currentText);
					String navigator=buildNavigatorHtml(editor);
					editor.setText(buildHtml(navigator,helpHtml));
				}else if(hle.getDescription().equals(NEXT)){
					HelpWindow helpWindow=listHelpWindow.get(editor);
					helpWindow.currentText++;
					String helpHtml=helpWindow.texts.get(helpWindow.currentText);
					String navigator=buildNavigatorHtml(editor);
					editor.setText(buildHtml(navigator,helpHtml));
				}else if(hle.getDescription().equals(EXTERNAL)){
					comm.showHelpPage();
				}else{
					String[] description=hle.getDescription().split("#");
					Integer idto=Integer.valueOf(description[0]);
					Integer idtoUserTask=!description[1].equals("null")?Integer.valueOf(description[1]):null;
					Integer userRol=!description[2].equals("null")?Integer.valueOf(description[2]):null;
					String helpHtml=buildHelpHtml(idto, idtoUserTask, userRol);
					
					if(helpHtml!=null){
						HelpWindow helpWindow=listHelpWindow.get(editor);
						if(helpWindow.currentText<helpWindow.texts.size()-1){
							int size=helpWindow.texts.size();
							for(int i=helpWindow.currentText+1;i<size;i++)
								helpWindow.texts.remove(helpWindow.texts.size()-1);
						}
						helpWindow.texts.add(helpHtml);
						helpWindow.currentText++;
						
						String navigator=buildNavigatorHtml(editor);
						editor.setText(buildHtml(navigator,helpHtml));
						/*scroll.setViewportView(editor);
						scroll.getViewport().setViewPosition(new Point(0,0));
						scroll.validate();
						scroll.repaint();
						System.err.println(((JViewport)((JEditorPane)hle.getSource()).getParent()).getViewPosition());
						System.err.println(scroll.getViewport().getViewPosition());*/
					}
				}
				editor.setCaretPosition(0);//Para que el scrollPane se coloque al principio del texto
				editor.validate();
				editor.repaint();
				//System.err.println(editor.getText());
			}
		}catch(Exception ex){
			ex.printStackTrace();
			Singleton.getInstance().getComm().logError(SwingUtilities.getWindowAncestor((Component)hle.getSource()),ex, "Error al navegar por la ayuda");
		}
	}
	
	public static void main(String args[]){
		JFrame f = new JFrame();		
		f.setResizable(false);
		
		JPanel p = new JPanel();
		JButton b = new JButton("Ayuda");
		b.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				try{
					HelpComponent help=new HelpComponent(null,null,new Dimension(350,350),null);
					help.showHelp(-1,null,null,null);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}			
		});
		p.add(b);
		
		f.setContentPane(p);		        
        f.setSize(new Dimension(1024,768));
        f.setPreferredSize(new Dimension(1024,768));
        f.setTitle("DynaAps®");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        f.pack(); 
	}
	
	private class HelpWindow{
		JEditorPane editor;
		JDialog dialog;
		ArrayList<String> texts;
		int currentText;
		
		public HelpWindow(JEditorPane editor, JDialog dialog) {
			super();
			this.editor = editor;
			this.dialog = dialog;
			this.texts = new ArrayList<String>();;
			this.currentText = -1;
		}
	}
}
