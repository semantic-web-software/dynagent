package gdev.gawt;

import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;
import gdev.gfld.GFormField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import dynagent.common.communication.communicator;
import dynagent.common.communication.docServer;
import dynagent.common.knowledge.access;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.Utils;

/**
 * Esta clase extiende a GComponent y creará una imagen.
 * Una vez creada se podrá representar en la interfaz gráfica.
 * @author Juan
 * @author Francisco
 */
public class GImage extends GComponent implements /*field,*/ ActionListener, MouseListener, FocusListener, IComponentData
{ //private final Component c= this;
	private static final long serialVersionUID = 1L;
	ArrayList<String> initialValue, currValue;
    String m_id;
    int m_stx;
    /*fieldControl m_control;*/
    boolean m_nullable, m_modoConsulta;
    docServer m_server;
    String m_label;
    String m_name;
    /*session m_session;*/
    Color m_colorFondo;
    boolean m_modoFilter;
    IUserMessageListener m_messageListener;
    docServer server;
    JPopupMenu popupBotonera;
    boolean enabled;
    Dimension dimImage;
    Dimension dimShortImage;
    
	private JLabel image;
	private JPanel setImages;
    private JScrollPane scrollPaneSetImages;
	
	private ArrayList<ImageIcon> listImages;
	private ArrayList<ImageIcon> listOriginalImages;
	private ArrayList<JButton> listButtonShortImages;
	
	private int indexActualImage;
	
	private Dimension sizeWindowZoomImage;
	private boolean multivalued;
    
    public GImage(GFormField ff,/*session ses,*/docServer server,/*fieldControl control,*/IComponentListener controlValue,IUserMessageListener messageListener,Font fuente,boolean modoConsulta,boolean modoFilter,Dimension sizeWindowZoomImage)
    {
        super(ff,controlValue);
        
////////////OBTENCION DE ATRIBUTOS//////////////////
    	//boolean comentado=ff.isCommented();
		String id=ff.getId();
		String label=ff.getLabel();
		String name=ff.getName();
		//boolean topLabel=ff.isTopLabel();
		int sintax=ff.getType();

		initialValue=new ArrayList<String>();
    	if(ff.getDefaultVal()!=null){
    		if(ff.getDefaultVal() instanceof ArrayList)
    			initialValue.addAll((ArrayList)ff.getDefaultVal());
    		else initialValue.add((String)ff.getDefaultVal());
    	}
		//String id2=ff.getId2();
		boolean nullable=ff.isNullable();
		//Color color=null;
		//boolean enabled=ff.isEnabled();
		//boolean multivalued=ff.isMultivalued();
		//int rows=ff.getRows();
		//int altoLinea=(int)ff.getRowHeight();
		//int cols=ff.getCols();
		//Insets ins= ff.getInternalPaddingEdit();
		//String mask=ff.getMask();
		////////////////////////////////////////////////////
		
        /*m_session=ses;*/
        m_server=server;
    	m_modoConsulta=modoConsulta;

    	m_label=label;
    	m_name=name;
      	m_nullable=nullable;
    	/*m_control = control;*/
    	
    	multivalued=ff.isMultivalued();
    	m_id=id;
    	m_stx= sintax;
  
    	m_modoFilter=modoFilter;
    	
    	m_messageListener=messageListener;
    	
    	this.server=server;
    	
    	enabled=ff.isEnabled();
    	
    	dimImage=GConfigView.minimumSizeImage;
    	
    	dimShortImage=new Dimension((GConfigView.minimumSizeImage.width-2/*Bordes*/)/3,(GConfigView.minimumSizeImage.height-2/*Bordes*/)/3);
    	
    	listImages=new ArrayList<ImageIcon>();
    	
    	listOriginalImages=new ArrayList<ImageIcon>();
    	setImages=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
    	
    	indexActualImage=-1;
    	
    	listButtonShortImages=new ArrayList<JButton>();
    	
    	this.sizeWindowZoomImage=sizeWindowZoomImage;
    	
    	currValue=new ArrayList<String>();
    }
    protected void createComponent() throws AssignValueException
    {
    	int accessOp=access.VIEW;
		if( !m_modoConsulta && !m_modoFilter )
			accessOp= accessOp | access.NEW | access.DEL;

//		/*OperationsObject operations=new OperationsObject(new access(accessOp),-1);*/
//		HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=new HashMap<Integer, ArrayList<UserAccess>>();
//		ArrayList<UserAccess> arrayAccess=new ArrayList<UserAccess>();
//		arrayAccess.add(new UserAccess(-1,new access(accessOp)));
//		accessUserTasks.put(-1, arrayAccess);
//		AccessAdapter accessAdapter=new AccessAdapter(accessUserTasks,null); 
//		botoneraAccion botonera= new botoneraAccion(	
//				m_id,null,null,null,
//				botoneraAccion.TABLE_TYPE,
//				null,
//				null,
//				null,
//				(ActionListener)this,
//				/*new access(accessOp),*//*operations,*/accessAdapter,
//				m_modoConsulta,
//				true,
//				getFormField().getViewBalancer().getGraphics(),m_server);
//		
//		if( m_colorFondo!=null )
//	    	setBackground( m_colorFondo );

		//popupBotonera=new JPopupMenu();
//		if(enabled){
			JPanel buttonsPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
			buttonsPanel.setBorder(new EmptyBorder(0,0,0,0));
			
			int buttonHeight= (int)GViewBalancer.getRowHeightS(getFormField().getViewBalancer().getGraphics());
			int buttonWidth=buttonHeight;
			if(multivalued){
				JButton botonLeft = botoneraAccion.subBuildBoton(buttonsPanel, null, "arrow_left", ""+botoneraAccion.PREV,
						"Anterior", buttonWidth,buttonHeight,true,m_server);
				botonLeft.addActionListener(this);
				JButton botonRight = botoneraAccion.subBuildBoton(buttonsPanel, null, "arrow_right", ""+botoneraAccion.NEXT,
						"Siguiente", buttonWidth,buttonHeight,true,m_server);
				botonRight.addActionListener(this);
			}
			JButton boton1 = botoneraAccion.subBuildBoton(buttonsPanel, null, "view", ""+botoneraAccion.CONSULTAR,
				"Ver", buttonWidth,buttonHeight,true,m_server);
			boton1.addActionListener(this);
			if(enabled && !m_modoConsulta && !m_modoFilter ){
				JButton boton2 = botoneraAccion.subBuildBoton(buttonsPanel, null, "delete", ""+botoneraAccion.ELIMINAR,
						"Borrar", buttonWidth,buttonHeight,true,m_server);
				boton2.addActionListener(this);
				JButton boton3 = botoneraAccion.subBuildBoton(buttonsPanel, null, "look", ""+botoneraAccion.BUSCAR,
						"Buscar", buttonWidth,buttonHeight,true,m_server);
				boton3.addActionListener(this);
			}
			//panel.add(boton1);
			//panel.add(boton2);
			//panel.add(boton3);
			//popupBotonera.add(panel);
			//popupBotonera.pack();
			buttonsPanel.setPreferredSize(m_objFormField.getDimComponenteSecundario());
			m_objComponentSec=buttonsPanel;
//		}
//        m_objComponent = botonera.getComponent();
		
		image=new JLabel();
		image.setSize(dimImage);
		image.setPreferredSize(dimImage);
		image.setIconTextGap(0);
		image.addMouseListener(this);
		image.setName(m_name);
		image.setBackground(m_nullable?UIManager.getColor("TextField.background"):GConfigView.colorBackgroundRequired);
		image.setBorder(UIManager.getBorder("TextField.border"));
		image.setVerticalAlignment(JLabel.CENTER);
		image.setHorizontalAlignment(JLabel.CENTER);
		image.setOpaque(true);
		
		image.setPreferredSize(new Dimension(GConfigView.minimumSizeImage.width/*+(GConfigView.widthImageButton*2)*/,GConfigView.minimumSizeImage.height));
		
		setImages=new JPanel(new GridLayout(0,3,0,0));//new FlowLayout(FlowLayout.LEFT,0,0));
		//setImages.setPreferredSize(new Dimension(m_objFormField.getPreferredSize().width-GConfigView.minimumSizeImage.width,GConfigView.minimumSizeImage.height));
		setImages.setBackground(UIManager.getColor("TextField.background"));
		//setImages.setBorder(UIManager.getBorder("TextField.border"));
		setImages.setBorder(BorderFactory.createEmptyBorder());
		
		scrollPaneSetImages=new JScrollPane(setImages,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneSetImages.getVerticalScrollBar().setUnitIncrement(10);
		//scrollPaneSetImages.setBorder(BorderFactory.createEmptyBorder());

		GridBagLayout gblayout=new GridBagLayout();
		JPanel panelAux1=new JPanel();
		panelAux1.setLayout(gblayout);

		GridBagConstraints gbc=new GridBagConstraints();
		gbc.gridx=0;
		gbc.ipadx=0;
		gbc.ipady=0;
		gbc.gridy=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gblayout.setConstraints(image, gbc);
		panelAux1.add(image);
		gbc.gridx=1;
		gblayout.setConstraints(scrollPaneSetImages, gbc);
		panelAux1.add(scrollPaneSetImages);
		
		//panelAux1.setBackground(Color.WHITE);
		m_objComponent = panelAux1;
		setValue(false,initialValue, null);
		
		if(!initialValue.isEmpty()){
			indexActualImage=listImages.size()-1;
			listButtonShortImages.get(indexActualImage).setBorderPainted(true);
		}
    }
    
    
    @Override
	public void setComponentBounds(Rectangle rc) {
    	//setImages.setPreferredSize(new Dimension(rc.width-GConfigView.minimumSizeImage.width,rc.height));
    	int width=rc.width-GConfigView.minimumSizeImage.width;
	    scrollPaneSetImages.setPreferredSize(new Dimension(width,rc.height));
    	if(multivalued){
    	    ((GridLayout)setImages.getLayout()).setColumns(width/this.dimShortImage.width);//Hacemos que el numero de columnas dependa del tamaño del scrollpane
    	}
    	super.setComponentBounds(rc);
	}
   
    public void actionPerformed(ActionEvent ae){
    	try{
            JButton boton = (JButton) ae.getSource();
            String command = boton.getActionCommand();
            if(command!=null && !command.isEmpty()){   
                if (Integer.valueOf(command) == botoneraAccion.BUSCAR) {
//                    String currTmpCode = (String) getValue();
//                    String newTmpCode = ((communicator)m_server).newImage(/*m_tapos*/-1, currTmpCode);
//                    //Singleton.showMessageDialog("IMG:"+newTmpCode+","+helperConstant.equals( helperConstant.TM_TEXTO,currTmpCode,newTmpCode )+","+currTmpCode);
//                    if (newTmpCode != null &&
//                        !UtilsFields.equals(GConst.TM_TEXT,
//                                               currTmpCode, newTmpCode))
//                        setValue(true, newTmpCode);
                	JFileChooser fileChooser=new JFileChooser();
                	FileNameExtensionFilter filter = new FileNameExtensionFilter("Sólo Imágenes (jpg, jpeg, gif, png)", "jpg", "jpeg", "gif", "png");
                	fileChooser.setFileFilter(filter);
                	fileChooser.setAcceptAllFileFilterUsed(false);
                	if(multivalued)
                		fileChooser.setMultiSelectionEnabled(true);
                	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                	if(GConst.LAST_FILE_PATH!=null)//Si existe una ultima ruta a la que accedimos abrimos en esa carpeta
                		fileChooser.setCurrentDirectory(new File(GConst.LAST_FILE_PATH));
                	
                	int status=fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(m_objComponent));
                	 if (status == JFileChooser.APPROVE_OPTION) {
                		 if(fileChooser.getSelectedFile()!=null){
     	                	//System.err.println("selected:"+fileChooser.getSelectedFile().getAbsolutePath());
     	                	
     	                	File[] files=null;
     	                	if(multivalued)
     	                		files=fileChooser.getSelectedFiles();
     	                	else{
     	                		files=new File[1];
     	                		files[0]=fileChooser.getSelectedFile();
     	                	}
     	                	
     	                	for(int i=0;i<files.length;i++){
	     	                	String filePath=files[i].getAbsolutePath();
	     	                	
//	     	                	String idFile=server.serverUploadFile(filePath);
	     	                	String idFile=filePath;
	     	                	
	     	                	if(!currValue.contains(idFile)){
	     	                		String oldValue=null;
		     	                	if(!multivalued && !currValue.isEmpty()){
		     	                		oldValue=currValue.get(0);
		     	                	}
		     	                	setValue(true,idFile,oldValue);
	     	                	}
     	                	}
     	                	if(indexActualImage!=-1)
     	                		listButtonShortImages.get(indexActualImage).setBorderPainted(false);
     	                	indexActualImage=listImages.size()-1;
     	                	listButtonShortImages.get(indexActualImage).setBorderPainted(true);
     	                	
     	                	
     	                	setViewPosition(listButtonShortImages.get(indexActualImage).getLocation(setImages.getLocation()));
     	                	
     	                	GConst.LAST_FILE_PATH=fileChooser.getCurrentDirectory().getAbsolutePath();
                		 }
                	 }
                }
                else if (Integer.valueOf(command) == botoneraAccion.CONSULTAR) {
                    //System.outprintln("IMG query " + currValue);
                    //((communicator)m_server).viewImage(currValue);
                	if(indexActualImage!=-1){
//	                	JDialog j=new JDialog(SwingUtilities.getWindowAncestor(this));
//	        			j.setResizable(false);
//	        			j.add(new JLabel(listOriginalImages.get(indexActualImage)));
//	        			j.pack();
//	        			j.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
//	        			j.setVisible(true);
                		showImageWindow(listOriginalImages.get(indexActualImage));
                	}
                }
                else if (Integer.valueOf(command) == botoneraAccion.ELIMINAR) {
                	if(indexActualImage!=-1){
                	
	                	//((communicator)m_server).delImage(currValue);
	                    setValue(true, null, currValue.get(indexActualImage));
	                    
//	                    listImages.remove(indexActualImage);
//	                    listOriginalImages.remove(indexActualImage);
//	                    JButton button=listButtonShortImages.remove(indexActualImage);
//	                    setImages.remove(button);
	                    
	                    if(indexActualImage<=listImages.size()-1){
	                    	image.setIcon(listImages.get(indexActualImage));
	                    	//image.setOpaque(false);
	                    }else if(listImages.size()>0){
	                    	indexActualImage--;
	                    	image.setIcon(listImages.get(indexActualImage));
	                    	//image.setOpaque(false);
	                    }else{
	                    	indexActualImage=-1;
	                    	image.setIcon(null);
	                    	//image.setOpaque(true);
	                    }
	                    
 	                	if(indexActualImage!=-1){
 	                		listButtonShortImages.get(indexActualImage).setBorderPainted(true);
 	                		setViewPosition(listButtonShortImages.get(indexActualImage).getLocation(setImages.getLocation()));
 	                	}
 	                	
		                image.repaint();
		                setImages.validate();
		                setImages.repaint();
                	}
                }
                else if (Integer.valueOf(command) == botoneraAccion.PREV) {
                	if(indexActualImage!=-1){
                    	this.listButtonShortImages.get(indexActualImage).setBorderPainted(false);
                    	if(indexActualImage>0){
        					indexActualImage--;
        				}else{
        					indexActualImage=listImages.size()-1;
        				}
        				ImageIcon icon=listImages.get(indexActualImage);
        				
        				this.listButtonShortImages.get(indexActualImage).setBorderPainted(true);
        				image.setIcon(icon);
                     	//image.setOpaque(false);
                     	image.repaint();
                     	
                     	setViewPosition(listButtonShortImages.get(indexActualImage).getLocation(setImages.getLocation()));
                	}
                }
                else if (Integer.valueOf(command) == botoneraAccion.NEXT) {
                	if(indexActualImage!=-1){
                		this.listButtonShortImages.get(indexActualImage).setBorderPainted(false);
                    	if(indexActualImage<listImages.size()-1){
        					indexActualImage++;
        				}else{
        					indexActualImage=0;
        				}
        				ImageIcon icon=listImages.get(indexActualImage);
        				this.listButtonShortImages.get(indexActualImage).setBorderPainted(true);
        				
        				image.setIcon(icon);
                     	//image.setOpaque(false);
                     	image.repaint();
                     	
                     	setViewPosition(listButtonShortImages.get(indexActualImage).getLocation(setImages.getLocation()));
                	}
                }
            }
            else{
//            	int index=listButtonShortImages.indexOf(ae.getSource());
//        		if(index!=indexActualImage){
//        			this.listButtonShortImages.get(indexActualImage).setBorderPainted(false);
//        			indexActualImage=index;
//        			ImageIcon image=listImages.get(indexActualImage);
//        			this.listButtonShortImages.get(indexActualImage).setBorderPainted(true);
//        			this.image.setIcon(image);
//        			this.image.repaint();
//        			
//        			updateEnabledButtons();
//        		}
            }
        }catch(Exception ex){
        	m_server.logError(SwingUtilities.getWindowAncestor(this),ex,"Error al realizar la operación");
        	ex.printStackTrace();
        }
    }

    private void showImage(String filePath){
    
    	//System.err.println("FilePath:"+filePath);
    	
    	ImageIcon originalImage=new ImageIcon(filePath);
		if(originalImage.getImageLoadStatus()==MediaTracker.ERRORED)//Significaria que es una imagen en base de datos
			originalImage=new ImageIcon(((communicator)server).serverGetFilesURL(filePath));
    	
     	Image imageAux=originalImage.getImage();
     	
     	int width=imageAux.getWidth(this);
     	int height=imageAux.getHeight(this);
     	ImageIcon imageIcon=new ImageIcon(imageAux.getScaledInstance(width>=height?dimImage.width:-1, width<=height?dimImage.height:-1, Image.SCALE_SMOOTH));
     	
     	image.setIcon(imageIcon);
     	//image.setOpaque(false);
     	image.repaint();
     	
     	ImageIcon imageSet=new ImageIcon(imageAux.getScaledInstance(width>=height?dimShortImage.width:-1, width<=height?dimShortImage.height:-1, Image.SCALE_SMOOTH));
     	JButton label=new JButton(imageSet);
     	label.setSize(dimShortImage);
     	label.setPreferredSize(dimShortImage);
     	label.setIconTextGap(0);
     	label.setBorder(UIManager.getBorder("TextField.border"));
     	label.setVerticalAlignment(JLabel.CENTER);
     	label.setHorizontalAlignment(JLabel.CENTER);
     	label.setFocusable(true);
     	label.setOpaque(false);
     	label.addFocusListener(this);
     	label.setBorderPainted(false);
     	label.setFocusPainted(false);
     	//label.addActionListener(this);
     	label.addMouseListener(this);
     	label.repaint();
     		     	                	
     	setImages.add(label);
     	setImages.repaint();
     	
     	listButtonShortImages.add(label);
     	listOriginalImages.add(originalImage);
     	listImages.add(imageIcon);
    }
    
    private void delImage(String filePath){
     	int indexImage=currValue.indexOf(filePath);
     	
     	setImages.remove(listButtonShortImages.remove(indexImage));
     	
     	listOriginalImages.remove(indexImage);
     	
     	ImageIcon imageIcon=listImages.remove(indexImage);
     	if(image.getIcon()==imageIcon)
     		image.setIcon(null);
    }
    
   private void setValue(boolean notificar, Object newValue, Object oldValue) throws AssignValueException{
    	//String oldCurrValue=currValue;
    	//currValue= (String)newValue;
    	
	   if(newValue!=null){
		   if(newValue instanceof ArrayList){
			   for(String newV:(ArrayList<String>)newValue){
				   if(!currValue.contains(newV)){
					   currValue.add(newV);
					   showImage(newV);
				   }
			   }
		   }else{
			   if(!currValue.contains(newValue)){
				   currValue.add((String)newValue);
				   showImage((String)newValue);
			   }
		   }
	   }
	   if(oldValue!=null){
		   if(oldValue instanceof ArrayList){
			   for(String oldV:(ArrayList<String>)oldValue){
				   delImage(oldV);
			   }
			   currValue.remove((ArrayList)oldValue);
		   }else{
			   delImage((String)oldValue);
			   currValue.remove((String)oldValue);
		   }
	   }
    	//indexActualImage;
    	
    	
//    	if(currValue==null){
//    		image.setIcon(null);
//    		image.setOpaque(true);
//    	}else{
//    		//Dimension dim=m_objFormField.getMinimumComponentDimension();
//    		//((JLabel)m_objComponent).setIcon(server.getIcon(null,initialValue,dim.width,dim.height));
//    		image.setIcon(new ImageIcon(((communicator)server).getDbImage(null,initialValue,dimImage.width,dimImage.height)));
//    		image.setOpaque(false);
//    	}
//    	image.repaint();
    	
    	//else new ImageIcon(new ImageIcon(fileChooser.getSelectedFile().getAbsolutePath()).getImage().getScaledInstance(m_objComponent.getWidth(), m_objComponent.getHeight(), Image.SCALE_SMOOTH))
    	/*try{*/
    	//System.outprintln("IMG:"+m_id);
    	/*if( !m_modoConsulta && notificar ) 
 		m_control.eventDataChanged(m_session,-1,-1,id);*/
    	if( !m_modoConsulta && notificar ){
    		/*String[] buf = m_id.split(":");
    		Integer valueCls = !buf[2].equals("null")?Integer.parseInt(buf[0]):null;;*/
    		IdObjectForm idObjectForm=new IdObjectForm(m_id);
			Integer valueCls = idObjectForm.getValueCls();
    		/*String value=currValue;*/
    		try{
    			m_componentListener.setValueField(m_id,newValue,oldValue, valueCls, valueCls);
    		}catch(AssignValueException ex){
				setValue(oldValue,newValue);
				if(ex.getUserMessage()!=null)
					 m_messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
				throw ex;
			}catch(NotValidValueException ex){
				setValue(oldValue,newValue);
				 m_messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this));
			}
	 	}
	 	/*}catch( SystemException se ){
	 	    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),se);
	 	}catch( ApplicationException ae ){
	 	    Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),ae);
	 	}*/
    }
    public Object getValue(){return currValue;}
    public String getId(){return m_id;}
//    public String getValueToString(){return currValue;}
//    public boolean isNull(){ return currValue==null || currValue.length()==0;}
//    public boolean isNull(Object val){
//	 	if( val==null ) return true;
//	 	if( val instanceof String ) return ((String)val).length()==0;
//	 	/*else
//	 		Singleton.showMessageDialog( "Error is null en imageCOntrol, la clase es:"+ val.getClass());*/
//	 	return true;
//    }
    public boolean isNullable(){return m_nullable;}

//    public boolean hasChanged() throws ParseException{
//	 	return !UtilsFields.equals( GConst.TM_TEXT, currValue, initialValue);
//    }
    public String getLabel(){ return m_label;}
    public void commitValorInicial(){;}
    /*public void inizialiceRestriction(){;}*/
    
    @Override
	public boolean newValueAllowed() {
		//return isNull();
    	return true;
	}
    
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount()==2 && indexActualImage!=-1){
			ImageIcon image;
			if(e.getSource()==this.image)
				image=listOriginalImages.get(indexActualImage);
			else image=listOriginalImages.get(listButtonShortImages.indexOf(e.getSource()));
			showImageWindow(image);
		}
	}
	
	private void showImageWindow(ImageIcon image){
		final JFrame j=new JFrame(Utils.normalizeLabel(m_label));
		j.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		j.setIconImage(server.getIcon(this,"icon",0,0).getImage());
		j.setResizable(true);
		
		final JLabel labelImage=new JLabel(image);
		labelImage.setOpaque(true);
		labelImage.setVerticalAlignment(JLabel.CENTER);
		labelImage.setHorizontalAlignment(JLabel.CENTER);
		//JPanel panelAux=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
		//panelAux.add(labelImage);
		
		final JScrollPane scroll=new JScrollPane(labelImage);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		
		final JPanel buttonsPanel=new JPanel(/*new FlowLayout(FlowLayout.CENTER,0,0)*/);
		buttonsPanel.setBorder(new EmptyBorder(0,0,0,0));
		
		final JPanel panel=new JPanel(new BorderLayout(0,0));
		
		ActionListener listener=new ActionListener(){

			int zoom=0;
			public void actionPerformed(ActionEvent ae) {
				JButton boton = (JButton) ae.getSource();
	            String command = boton.getActionCommand();
				if (Integer.valueOf(command) == botoneraAccion.INCREASE_ZOOM || Integer.valueOf(command) == botoneraAccion.DECREASE_ZOOM) {
					if(Integer.valueOf(command) == botoneraAccion.INCREASE_ZOOM)
						zoom++;
					else zoom--;
					Image imageAux=listOriginalImages.get(indexActualImage).getImage();
								     	
			     	int width=imageAux.getWidth(panel);
			     	int height=imageAux.getHeight(panel);
			     	
			     	int newWidth;
			     	int newHeight;
			     	if(zoom>0){
			     		newWidth=width*(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
			     		newHeight=height*(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
			     	}else{
			     		newWidth=width/(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
			     		newHeight=height/(int)Math.scalb(1, Math.abs(zoom));//(1+Math.abs(zoom));
			     	}
			     	
			     	if(newWidth>0 && newHeight>0){
			     		ImageIcon imageIcon=new ImageIcon(imageAux.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
			     		
			     		labelImage.setIcon(imageIcon);
						labelImage.repaint();
						//scroll.validate();
						scroll.repaint();
			     	}else{
			     		//Restauramos el campo zoom ya que no hemos hecho nada porque el número es negativo
			     		if(Integer.valueOf(command) == botoneraAccion.INCREASE_ZOOM)
							zoom--;
						else zoom++;
			     	}
                }
                else{
					GImage.this.actionPerformed(ae);
					labelImage.setIcon(listOriginalImages.get(indexActualImage));
					labelImage.setVerticalAlignment(JLabel.CENTER);
					labelImage.setHorizontalAlignment(JLabel.CENTER);
					//JPanel panelAux=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
					//panelAux.add(labelImage);
					
					//panel.add(panelAux,BorderLayout.CENTER);
					//JScrollPane scroll=new JScrollPane(labelImage);
					//panel.add(panelAux,BorderLayout.CENTER);
					//JPanel panel=new JPanel(new BorderLayout(0,0));
	//				panel.removeAll();
	//				panel.add(scroll,BorderLayout.CENTER);
	//				panel.add(buttonsPanel,BorderLayout.SOUTH);
					//scroll.setViewportView(labelImage);
					//scroll.validate();
					scroll.repaint();
					//panel.validate();
					//panel.repaint();
	
					//j.setContentPane(panel);
					//j.pack();
					zoom=0;
                }
			}
			
		};
		int buttonHeight= (int)GViewBalancer.getRowHeightS(getFormField().getViewBalancer().getGraphics());
		int buttonWidth=buttonHeight;
	
		if(multivalued){
			JButton botonLeft = botoneraAccion.subBuildBoton(buttonsPanel, null, "arrow_left", ""+botoneraAccion.PREV,
					"Anterior", buttonWidth,buttonHeight,true,m_server);
			botonLeft.addActionListener(listener);
			JButton botonRight = botoneraAccion.subBuildBoton(buttonsPanel, null, "arrow_right", ""+botoneraAccion.NEXT,
					"Siguiente", buttonWidth,buttonHeight,true,m_server);
			botonRight.addActionListener(listener);
//			JButton botonMain = botoneraAccion.subBuildBoton(buttonsPanel, null/*"Hacer principal"*/, "star", ""+botoneraAccion.MAIN_IMAGE,
//					"Marcar como imagen principal", buttonWidth,buttonHeight,true,m_server);
//			botonMain.addActionListener(listener);
		}
		
		JButton botonIncreaseZoom = botoneraAccion.subBuildBoton(buttonsPanel, null, "zoom_in", ""+botoneraAccion.INCREASE_ZOOM,
				"Más zoom", buttonWidth,buttonHeight,true,m_server);
		botonIncreaseZoom.addActionListener(listener);
		JButton botonDecreaseZoom = botoneraAccion.subBuildBoton(buttonsPanel, null, "zoom_out", ""+botoneraAccion.DECREASE_ZOOM,
				"Menos zoom", buttonWidth,buttonHeight,true,m_server);
		botonDecreaseZoom.addActionListener(listener);
		
		//panel.add(panelAux,BorderLayout.CENTER);
		panel.add(scroll,BorderLayout.CENTER);
		panel.add(buttonsPanel,BorderLayout.SOUTH);
		
		//Para que escape cierre la ventana
		GConst.addShortCut(null, panel, GConst.CANCEL_SHORTCUT_KEY, GConst.CANCEL_SHORTCUT_MODIFIERS, "Cancelar ventana", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				j.dispose();
			}
			
		});

		/*Dimension dimScreen=Toolkit.getDefaultToolkit().getScreenSize();
		*/
		j.setContentPane(panel);
		/*j.pack();
		
		Insets insetsDialog=j.getInsets();
		//System.err.println("Insets:"+insetsDialog);
		int bordersWidth=insetsDialog.left+insetsDialog.right;
		int bordersHeight=insetsDialog.top+insetsDialog.bottom;
		
		scroll.setPreferredSize(new Dimension(dimScreen.width-bordersWidth,dimScreen.height-bordersHeight-(int)buttonsPanel.getPreferredSize().getHeight()));
		*/
		j.setPreferredSize(sizeWindowZoomImage);
		j.pack();
		//j.setPreferredSize(new Dimension(dimScreen.width,dimScreen.height));
		j.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
		//j.setLocation(0, 0);
		//j.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		//j.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		//j.pack();
		j.setVisible(true);
	}
	
	public void mouseEntered(MouseEvent e) {
		//if(!popupBotonera.isVisible())
		//	popupBotonera.show(image, (image.getWidth()-popupBotonera.getWidth())/2, 0);
	}
	public void mouseExited(MouseEvent e) {
		/*if(e.getComponent()!=popupBotonera && !popupBotonera.isAncestorOf((Component)e.getComponent()) && e.getComponent()!=m_objComponent){
			System.err.println(e.getComponent());
			popupBotonera.setVisible(false);
		}*/
	}
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void focusGained(FocusEvent e) {
		int index=listButtonShortImages.indexOf(e.getSource());
		if(index!=indexActualImage){
			this.listButtonShortImages.get(indexActualImage).setBorderPainted(false);
			indexActualImage=index;
			ImageIcon image=listImages.get(indexActualImage);
			this.listButtonShortImages.get(indexActualImage).setBorderPainted(true);
			this.image.setIcon(image);
			this.image.repaint();
		}
	}
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void clean() throws ParseException, AssignValueException {
		setValue(null, currValue);
	}
	public void initValue() throws ParseException, AssignValueException {
		setValue(initialValue, null);
	}
	public void setValue(Object newValue, Object oldValue) throws AssignValueException{
		setValue(false, newValue, oldValue);
	}
	
	
	public void setViewPosition(Point point){
		point.x=0;
		if(point.y<0){
			point.y=0;
		}
		
		if((point.y + scrollPaneSetImages.getHeight())>setImages.getHeight()){
			//System.err.println("Lo modificaaaa ya que point="+point);
			point.y=(int)(setImages.getHeight()-scrollPaneSetImages.getHeight());
		}
		
		scrollPaneSetImages.getViewport().setViewPosition(point);
	}
}
