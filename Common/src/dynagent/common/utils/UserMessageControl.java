package dynagent.common.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;

public class UserMessageControl implements IUserMessageListener{
	//private Component rootComponent;
	private Graphics graphics;
	private int maxWidth;
	
	public UserMessageControl(Component rootComponent,Graphics graphics,int maxWidth){
		//this.rootComponent=rootComponent;
		this.graphics=graphics;
		this.maxWidth=maxWidth;
		configureOptionPane();
	}
	
	
	public void showErrorMessage(String message,Component c) {
		Toolkit.getDefaultToolkit().beep();
		
		/*JOptionPane pane = new JOptionPane(prepareMessage(message,maxWidth),JOptionPane.ERROR_MESSAGE);
	    JDialog dialog = pane.createDialog(c, "Error");
	    dialog.setModalityType(ModalityType.MODELESS);
		dialog.setResizable(false);
	    dialog.setVisible(true);
	    dialog.dispose(); */
		JOptionPane.showMessageDialog(c,prepareMessage(message,maxWidth),"Error",JOptionPane.ERROR_MESSAGE);
	}
	
	public void showMessage(String message,Component c){
		Toolkit.getDefaultToolkit().beep();

		/*JOptionPane pane = new JOptionPane(prepareMessage(message,maxWidth));
	     
	     JDialog dialog = pane.createDialog(c, "Mensaje");
	     dialog.setModalityType(ModalityType.MODELESS);
			dialog.setResizable(false);
	     //dialog.pack();
	     dialog.setVisible(true);
	     //dialog.dispose(); */
		JOptionPane.showMessageDialog(c,prepareMessage(message,maxWidth));
	}
	
	private String prepareMessage(String message,int maxWidth){
		if(message.contains("<html>")) return message;
		
		String normalizems=Utils.normalizeMessage(message);
		if(graphics!=null){
			Graphics2D gr2D = (Graphics2D) graphics;
			gr2D.setFont(UIManager.getFont("Label.font"));
			FontRenderContext fontRender = gr2D.getFontRenderContext();
			Font font=gr2D.getFont();
			
			//Construimos el mensaje utilizando el ancho maximo de las lineas del mensaje
			String[] subString=normalizems.split("\n");
			String stringMax="";
			for(int i=0;i<subString.length;i++){
				if(stringMax.length()<subString[i].length())
					stringMax=subString[i];
			}
			Dimension dimMs=Utils.getDimString(stringMax, false, font, fontRender, 1);
			normalizems=normalizems.replaceAll("<", "&lt;");
			normalizems=normalizems.replaceAll(">", "&gt;");
			normalizems=normalizems.replaceAll("\n", "<br>");
			if (dimMs.width>maxWidth*0.5){
				normalizems="<html><body><p width='"+maxWidth*0.5+"'>"+normalizems+"</p></body></html>";
			}else{
				normalizems="<html><body><p width='"+dimMs.width+"'>"+normalizems+"</p></body></html>";
			}
		}
		
		return normalizems;
	}

	public int showOptionMessage(String message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue,Component c){
		Toolkit.getDefaultToolkit().beep();
		
		/*JOptionPane pane = new JOptionPane(prepareMessage(message,maxWidth),messageType,optionType,icon,options,initialValue){

			@Override
			public JDialog createDialog(Component c, String m) throws HeadlessException {
				final JDialog dialog;

				final JOptionPane pane=this;
				// TODO Auto-generated method stub
				Window window = SwingUtilities.getWindowAncestor(c);//JOptionPane.getWindowForComponent(c);
		        if (window instanceof Frame) {
		            dialog = new JDialog((Frame)window, m, true);	
		        } else {
		            dialog = new JDialog((Dialog)window, m, true);
		        }
		 	

		        dialog.setComponentOrientation(this.getComponentOrientation());
		        Container contentPane = dialog.getContentPane();

		        contentPane.setLayout(new BorderLayout());
		        contentPane.add(this, BorderLayout.CENTER);
		        dialog.setResizable(false);
		        if (JDialog.isDefaultLookAndFeelDecorated()) {
		            boolean supportsWindowDecorations =
		              UIManager.getLookAndFeel().getSupportsWindowDecorations();
		            if (supportsWindowDecorations) {
		                dialog.setUndecorated(true);
		                //getRootPane().setWindowDecorationStyle(style);
		            }
		        }
		        dialog.pack();
		        dialog.setLocationRelativeTo(c);
		        WindowAdapter adapter = new WindowAdapter() {
		            private boolean gotFocus = false;
		            public void windowClosing(WindowEvent we) {
		                setValue(null);
		            }
		            public void windowGainedFocus(WindowEvent we) {
		                // Once window gets focus, set initial focus
		                if (!gotFocus) {
		                    selectInitialValue();
		                    gotFocus = true;
		                }
		            }
		        };
		        dialog.addWindowListener(adapter);
		        dialog.addWindowFocusListener(adapter);
		        dialog.addComponentListener(new ComponentAdapter() {
		            public void componentShown(ComponentEvent ce) {
		                // reset value to ensure closing works properly
		                setValue(JOptionPane.UNINITIALIZED_VALUE);
		            }
		        });
		        addPropertyChangeListener(new PropertyChangeListener() {
		            public void propertyChange(PropertyChangeEvent event) {
		                // Let the defaultCloseOperation handle the closing
		                // if the user closed the window without selecting a button
		                // (newValue = null in that case).  Otherwise, close the dialog.
		                if (dialog.isVisible() && event.getSource() == pane &&
		                  (event.getPropertyName().equals(VALUE_PROPERTY)) &&
		                  event.getNewValue() != null &&
		                  event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
		                    dialog.setVisible(false);
		                }
		            }
		        });

				return dialog;
			}
			
		};
	     
	     JDialog dialog = pane.createDialog(c,  Utils.normalizeLabel(title));
	     //dialog.pack();
	     dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setResizable(false);
	     dialog.setVisible(true);
	     System.err.println("DEspuesss 2");
	     return new Integer((String)pane.getValue());*/
		//System.err.println("option:"+c);
		return JOptionPane.showOptionDialog(c, message, Utils.normalizeLabel(title), optionType, messageType, icon, options, initialValue);
	}
	
	public String showInputMessage(String message, String title, int messageType, Icon icon, String[] options, String initialValue,Component c){
		Toolkit.getDefaultToolkit().beep();
		
		/*JOptionPane pane = new JOptionPane(prepareMessage(message,maxWidth),messageType,JOptionPane.YES_NO_OPTION,icon,options,initialValue){
			@Override
			public JDialog createDialog(Component c, String m) throws HeadlessException {
				final JDialog dialog;

				final JOptionPane pane=this;
				// TODO Auto-generated method stub
				Window window = SwingUtilities.getWindowAncestor(c);//JOptionPane.getWindowForComponent(c);
		        if (window instanceof Frame) {
		            dialog = new JDialog((Frame)window, m, true);	
		            setRootFrame((Frame)window);
		        } else {
		            dialog = new JDialog((Dialog)window, m, true);
		        }
		 	

		        dialog.setComponentOrientation(this.getComponentOrientation());
		        Container contentPane = dialog.getContentPane();

		        contentPane.setLayout(new BorderLayout());
		        contentPane.add(this, BorderLayout.CENTER);
		        dialog.setResizable(false);
		        if (JDialog.isDefaultLookAndFeelDecorated()) {
		            boolean supportsWindowDecorations =
		              UIManager.getLookAndFeel().getSupportsWindowDecorations();
		            if (supportsWindowDecorations) {
		                dialog.setUndecorated(true);
		                //getRootPane().setWindowDecorationStyle(style);
		            }
		        }
		        dialog.pack();
		        dialog.setLocationRelativeTo(c);
		        WindowAdapter adapter = new WindowAdapter() {
		            private boolean gotFocus = false;
		            public void windowClosing(WindowEvent we) {
		                setValue(null);
		            }
		            public void windowGainedFocus(WindowEvent we) {
		                // Once window gets focus, set initial focus
		                if (!gotFocus) {
		                    selectInitialValue();
		                    gotFocus = true;
		                }
		            }
		        };
		        dialog.addWindowListener(adapter);
		        dialog.addWindowFocusListener(adapter);
		        dialog.addComponentListener(new ComponentAdapter() {
		            public void componentShown(ComponentEvent ce) {
		                // reset value to ensure closing works properly
		                setValue(JOptionPane.UNINITIALIZED_VALUE);
		            }
		        });
		        addPropertyChangeListener(new PropertyChangeListener() {
		            public void propertyChange(PropertyChangeEvent event) {
		                // Let the defaultCloseOperation handle the closing
		                // if the user closed the window without selecting a button
		                // (newValue = null in that case).  Otherwise, close the dialog.
		                if (dialog.isVisible() && event.getSource() == pane &&
		                  (event.getPropertyName().equals(VALUE_PROPERTY)) &&
		                  event.getNewValue() != null &&
		                  event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
		                    dialog.setVisible(false);
		                    dialog.dispose();
		                }
		            }
		        });

				return dialog;
			}
			
		};
		pane.setWantsInput(true);
	     pane.setSelectionValues(options);
	     
	     JDialog dialog = pane.createDialog(c,  Utils.normalizeLabel(title));
	     //	   dialog.pack();
	     dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setResizable(false);
	     dialog.setVisible(true);
	     dialog.dispose();
	     System.err.println("DEspuesss 2");
	     return (String)pane.getValue();*/
		return (String)JOptionPane.showInputDialog(c, message, Utils.normalizeLabel(title), messageType, icon, options, initialValue);
	}
	
	public String showInputMessageWithComponents(Object messageAndComponents, String title, int messageType, Icon icon, String[] options, String initialValue,Component c){
		Toolkit.getDefaultToolkit().beep();
		return (String)JOptionPane.showInputDialog(c, messageAndComponents, Utils.normalizeLabel(title), messageType, icon, options, initialValue);
	}

		
	//Redefine las teclas que funcionan sobre la ventana de avisos para que funcionen las flechas izq-dch
	private void configureOptionPane() {
		if (UIManager.get("OptionPane.actionMap") == null) {
			UIManager.put("OptionPane.windowBindings", new Object[] {
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE , 0),"close",
					KeyStroke.getKeyStroke(KeyEvent.VK_LEFT , 0), "left",
					KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT , 0), "right"});
			ActionMap map = new ActionMapUIResource();
			map.put("close", new OptionPaneCloseAction());
			map.put("left", new OptionPaneArrowAction(false));
			map.put("right", new OptionPaneArrowAction(true));
			UIManager.put("OptionPane.actionMap", map);
		}
	}

	private class OptionPaneCloseAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			JOptionPane optionPane = (JOptionPane) e.getSource();
			optionPane.setValue(JOptionPane.CLOSED_OPTION);
		}
	}

	private class OptionPaneArrowAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		private boolean myMoveRight;

		OptionPaneArrowAction(boolean moveRight) {
			myMoveRight = moveRight;
		}

		public void actionPerformed(ActionEvent e) {
			JOptionPane optionPane = (JOptionPane) e.getSource();
			EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
			eq.postEvent(new KeyEvent(optionPane, KeyEvent.KEY_PRESSED, e
					.getWhen(), (myMoveRight) ? 0 : InputEvent.SHIFT_DOWN_MASK,
					KeyEvent.VK_TAB, KeyEvent.CHAR_UNDEFINED,
					KeyEvent.KEY_LOCATION_UNKNOWN));
		}
	}
}
