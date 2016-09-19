package dynagent.gui.utils;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;

public class UserComponent extends JPanel{
	private static final long serialVersionUID = 1L;

	public UserComponent(String username){
		super();
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS ) );
		setLayout(new GridLayout(2,1));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/(int)(ConstantesGraficas.intToolY*2.5),ConstantesGraficas.intToolY));
		JLabel label=new JLabel(Utils.normalizeLabel("Usuario"),JLabel.CENTER);
		label.setName("UserComponent.label");
		JLabel user=new JLabel(Utils.normalizeLabel(username),JLabel.CENTER);
		label.setName("UserComponent.usuario");
		//setMaximumSize(new Dimension(ConstantesGraficas.intToolY*2,ConstantesGraficas.intToolY));
		//label.setPreferredSize(new Dimension(ConstantesGraficas.intToolY,ConstantesGraficas.intToolY/2));
		//user.setPreferredSize(new Dimension(ConstantesGraficas.intToolY,ConstantesGraficas.intToolY/2));
		Font font = user.getFont();
		user.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		add(label);
		add(user);
	}

}
