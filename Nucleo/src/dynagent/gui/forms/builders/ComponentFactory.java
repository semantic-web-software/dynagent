package dynagent.gui.forms.builders;

import gdev.gawt.GButton;
import gdev.gawt.GCheckBox;
import gdev.gawt.GComponent;
import gdev.gawt.GEdit;
import gdev.gawt.GImage;
import gdev.gawt.GListBox;
import gdev.gawt.GSpinnerHour;
import gdev.gawt.GTable;
import gdev.gbalancer.GProcessedField;
import gdev.gen.GConst;
import gdev.gen.IComponentData;
import gdev.gen.IComponentFactory;
import gdev.gen.IComponentListener;
import gdev.gen.IDictionaryFinder;
import gdev.gfld.GFormTable;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;

import dynagent.common.communication.communicator;
import dynagent.common.utils.IUserMessageListener;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;

public class ComponentFactory implements IComponentFactory{
	private HashMap<String,IComponentData> m_componentsData;
	private communicator m_com;
	private Font m_defaultFont;
	private boolean m_modoConsulta;
	private boolean m_modoFilter;

	private int m_action;
	private HashMap<String, GTable> m_tables;
	private HashMap<String, GButton> m_formButtoms;
	private ArrayList<GComponent> m_formComponents;
	private String m_targetType;
	private Object m_control;
	private KeyListener m_keyListener;
	private boolean m_popup;
	private IComponentListener m_controlListener;
	private JComponent componentParent;
	private IDictionaryFinder m_dictionaryFinder;
	private Window window;
	private IUserMessageListener m_messageListener;

	
	public ComponentFactory(IComponentListener controlListener,IUserMessageListener messageListener,IDictionaryFinder dictionaryFinder,Object control,KeyListener keyListener,Font fuente,boolean modoConsulta,boolean popup,boolean modoFilter,HashMap<String,IComponentData> componentsData,int action,HashMap<String, GTable> tables,HashMap<String, GButton> formButtoms,ArrayList<GComponent> formComponents,String targetType, Window window, communicator comm){
		 m_com=comm;
		 m_modoConsulta=modoConsulta;
		 m_defaultFont=fuente;
		 m_modoFilter=modoFilter;
	
		 m_componentsData=componentsData;
		 m_action=action;
		 m_tables=tables;
		 m_targetType=targetType;
		 m_formButtoms=formButtoms;
		 m_control=control;
		 m_formComponents=formComponents;
		 m_keyListener=keyListener;
		 m_popup=popup;
		 m_controlListener=controlListener;
		 m_dictionaryFinder=dictionaryFinder;
		 this.window=window;
		 m_messageListener=messageListener;
		
	}
	
	public GComponent create(GProcessedField oneField){
		GComponent component=null;
        
        switch(oneField.getFormField().getType())
        {
	        case GConst.TM_TABLE:
	            /*component = new GTable(oneField.getFormField());*/
	        	component=buildTable(oneField);
	            break;
	        case GConst.TM_ENUMERATED:
	            /*component = new GComboBox(oneField.getFormField());*/
	        	component=buildEnum(oneField);
	            break;
	        case GConst.TM_IMAGE:
	        	component=buildImage(oneField);
	            break;
	        case GConst.TM_BOOLEAN:
	        case GConst.TM_BOOLEAN_COMMENTED:
	            /*component = new GCheckBox(oneField.getFormField());*/
	        	component=buildCheck(oneField);
	            break;
	        case GConst.TM_MEMO:
	            /*component = new GEdit(oneField.getFormField());//for the time being*/
	        	component=buildEdit(oneField);
	            break;
	        case GConst.TM_INTEGER:
	        case GConst.TM_REAL:
	        case GConst.TM_DATE:
	        case GConst.TM_DATE_HOUR:
	        case GConst.TM_FILE:
	        case GConst.TM_TEXT:
	            /*component = new GEdit(oneField.getFormField());*/
	        	component=buildEdit(oneField);
	            break;
	        case GConst.TM_BUTTON:
	        	component=buildButton(oneField);
	            break;
	        case GConst.TM_HOUR:
	        	component=buildSpinnerHour(oneField);
	        	break;
	        default:
	        	break;
        }
        /*if(component==null)
            return component;
        component.create();
        Rectangle rc = oneField.getBounds();
        component.setBounds(new Rectangle(rc));
        component.setLabelComponentBounds(oneField.getLabelBounds());
        component.setComponentBounds(oneField.getComponentBounds());*/
        return component;
	}
	

	
	GComponent buildTable(GProcessedField field){
		
		//System.out.println("DENTRO DE BUILDTABLE");
		field.getLabelBounds();
		GTable table=new GTable((GFormTable)field.getFormField()/*,m_session*/,m_com,/*m_domCheck,*/m_controlListener, m_messageListener, m_dictionaryFinder, m_control,/*m_scope,*/m_action,/*m_rootDocModel,*//*m_formComponents,*/m_defaultFont,m_modoConsulta,m_popup,m_modoFilter, window);

		String formId=field.getFormField().getId();
		/*m_moa.put(formId,table);*/ //Las tablas no se añaden
		m_tables.put(formId, table);
		//System.out.println("ASIGNA M_TABLES EN BUILDTABLE "+formId+" "+table+" "+m_tables);
		m_formComponents.add(table);
		return table;
	}
	
	GComponent buildButton(GProcessedField field){
		GButton boton=new GButton(field.getFormField(),m_targetType,(ActionListener)m_control,m_messageListener,m_defaultFont,m_keyListener);
		String formId=field.getFormField().getId();
/*		m_moa.put(formId,boton.getComponent());*/
		
		/*m_rootSourceModel.addField( formId,boton);*///boton no es un field
		
		/*m_formComponents.add(ck);*/
		/*return lb.getComponent();*/
				
		m_formButtoms.put( formId, boton);
		return boton;
	}
	
	/*GComponent buildImage(GProcessedField field)throws ParseException{*/
	GComponent buildImage(GProcessedField field){

		/*Object img= new imageControl( m_session,m_com,m_domCheck, formId, defaultVal,
				tapos, helperConstant.TM_IMAGEN, nullable, label, m_modoConsulta );*/
		Dimension sizeWindowZoom=Singleton.getInstance().getApplet().getSize();
		GImage img= new GImage( field.getFormField(),/*m_session,*/m_com,/*m_domCheck,*/m_controlListener,m_messageListener,m_defaultFont,m_modoConsulta,m_modoFilter,sizeWindowZoom );
		//String formId=field.getFormField().getId();
/*		m_moa.put(formId, img);*/
		
		/*m_rootSourceModel.addField( formId,img );*/
		m_formComponents.add(img);
		return img;
	}
	
	GComponent buildEnum(GProcessedField field){
		GListBox lb=new GListBox(field.getFormField(),/*m_session,*/m_com,/*m_domCheck,*/m_controlListener,m_messageListener,m_keyListener,m_defaultFont,m_modoConsulta,m_modoFilter);
		String formId=field.getFormField().getId();
		/*m_moa.put(formId,lb.getComponent());*/
		m_componentsData.put(formId,lb);
		/*m_rootSourceModel.addField( formId,lb);*/
		m_formComponents.add(lb.getComponent());
		return lb.getComponent();
	}
	
	GComponent buildEdit(GProcessedField field){
		IDictionaryFinder df=field.getFormField().getType()==GConst.TM_TEXT && m_modoFilter?m_dictionaryFinder:null;
		GEdit e=new GEdit(field.getFormField(),/*m_session,*/m_com,/*m_domCheck*/m_controlListener,m_messageListener,m_keyListener,m_defaultFont,m_modoConsulta,m_modoFilter,componentParent,df);
		String formId=field.getFormField().getId();
		m_componentsData.put(formId,e);
		/*m_rootSourceModel.addField( formId, e );*/
		m_formComponents.add(e);
		return e;
	}
	
	GComponent buildCheck(GProcessedField field){
		GCheckBox ck=new GCheckBox(field.getFormField(),/*m_session,*/m_com,/*m_domCheck*/m_controlListener,m_messageListener,m_defaultFont,m_modoConsulta,m_modoFilter);
		String formId=field.getFormField().getId();
		m_componentsData.put(formId,ck);
		/*m_rootSourceModel.addField( formId, ck );*/
		m_formComponents.add(ck);
		return ck;
	}
	
	GComponent buildSpinnerHour(GProcessedField field){
		GSpinnerHour gsh = new GSpinnerHour(field.getFormField(), m_com, m_controlListener,m_messageListener, m_modoFilter, m_modoConsulta);
		String formId=field.getFormField().getId();
		m_componentsData.put(formId,gsh);
		m_formComponents.add(gsh);
		return gsh;
	}

	public void setParent(JComponent parent) {
		componentParent=parent;
	}

}
