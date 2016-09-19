package dynagent.gui.utils;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import dynagent.common.Constants;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;

public class ConfigurationComponent extends JPanel{
	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JEditorPane html=null;
	private int ancho=500, alto=380;
	private int dimBoton = ConstantesGraficas.intToolY-3;

	public ConfigurationComponent(WindowComponent dialog_parent, final KnowledgeBaseAdapter kba){
		super();
		parent=dialog_parent;

		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		JButton b = new JButton(Singleton.getInstance().getComm().getIcon("wizard"));
		b.setToolTipText("Asistente de configuración");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				parent.disabledEvents();
				Session ses=kba.getDefaultSession();//kba.createDefaultSession(kba.getDefaultSession(),null, true, true, true, false/*TODO Probar que realmente necesitamos false y no true*/, false);
				try {
					
					int idto=kba.getIdClass("APLICACIÓN");
					Iterator<Integer> itr=kba.getIndividuals(idto, Constants.LEVEL_INDIVIDUAL, false);
					if(itr.hasNext()){
						int ido=itr.next();
						int idProp=kba.getIdProp("módulos_configurados");
						
						Object actualValue=kba.getValueData(kba.getField(ido, idto, idProp, null, null, ses));
						
						BooleanValue value=(BooleanValue)kba.buildValue(actualValue, Constants.IDTO_BOOLEAN);
						value.setBvalue(!value.getBvalue());
						
						Value oldValue=kba.buildValue(actualValue,Constants.IDTO_BOOLEAN);
						kba.setValue(ido, idProp, value, oldValue, null, null, ses);
						
						kba.setValue(ido, idProp, oldValue, value, null, null, ses);
					}else{
						System.err.println("No existe ningún individuo de Aplicación");
					}
					
				}catch(Exception ex){
					ex.printStackTrace();
				} finally{
					parent.enabledEvents();
//					try{
//						ses.rollBack();
//					}catch(Exception ex){
//						ex.printStackTrace();
//						System.err.println("No se ha podido hacer rollback de la sesión que fuerza la configuración de módulos");
//					}
				}
			}
		});
		b.setBounds(/*anchoPanel-dimBoton*/1, /*(ConstantesGraficas.intToolY-3)/2-dimBoton/2*/1, dimBoton, dimBoton);
		add(b);
	}

}
