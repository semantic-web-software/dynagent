package gdev.gawt.utils;

import gdev.gen.GConfigView;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import dynagent.common.communication.docServer;

/**
 * PTristateCheckBox is a subclass of JCheckBox which supports the rendering of a
 * TristateButtonModel.
 *
 * @author btsang
 * @version 7.1
 */
public class TristateCheckBox extends JCheckBox{
	
	private static final long serialVersionUID = 1L;
	private Boolean value;
	private Icon nullIcon = null;
	private Icon checkIcon = null;
	private Icon uncheckIcon = null;

	public TristateCheckBox(docServer server,int maxWidth,int maxHeight){
		super();
		
		int heightIcon=maxHeight-(int)Math.round(maxHeight*GConfigView.reductionSizeImageCheck);
		int widthIcon=maxWidth-(int)Math.round(maxWidth*GConfigView.reductionSizeImageCheck);
		
		nullIcon=server.getIcon(null,"nulo",widthIcon,heightIcon);
		checkIcon=server.getIcon(null,"check",widthIcon,heightIcon);
		uncheckIcon=server.getIcon(null,"uncheck",widthIcon,heightIcon);
		
		setBorderPainted(true);
        setBorder((Border)UIManager.get("TextField.border"));
        setHorizontalAlignment(LEADING);
        setMargin(new Insets(0,0,0,0));
        setIconTextGap(0);  
        //setIcon(null);
	}
	
	public void setSelected(boolean value){
    	setSelected((Boolean)value);
    }
    
    public void setSelected(Boolean value){
    	this.value=value;
    	setIconValue();
    	if(value!=null)
    		super.setSelected(value);
    	else super.setSelected(false);
    	
    }
    
    public Boolean getSelected(){
    	return value;
    }
	
	private void setIconValue(){
		if(value==null){
			setIcon(/*new ImageIcon()*/nullIcon);
		}
		else if (value.booleanValue() == false) {
			setIcon(uncheckIcon);
			 
		} else if (value.booleanValue() == true) {
			setIcon(checkIcon);
		}
	}

}
