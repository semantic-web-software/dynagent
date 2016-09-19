package gdev.gawt;

import gdev.gawt.utils.ItemList;
import gdev.gen.AssignValueException;
import gdev.gen.IComponentData;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;
import gdev.gfld.GFormEnumerated;
import gdev.gfld.GFormField;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dynagent.common.communication.docServer;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;

/**
 * Esta clase extiende a GComponent y creará una lista seleccionable.
 * Una vez creada se podrá representar en la interfaz gráfica.
 * @author Juan
 * @author Francisco
 */
public class GListBox implements IComponentData,/*field,*/ ItemListener
{
	String m_id2;
	String m_id;
    ItemList currentValidatedValue=null;//un valor de un item nunca puede ser 0 que corresponde a null
	/*fieldControl m_control;*/
	public Vector<ItemList> m_listaInicial;
	String m_label;
    boolean m_nullable, m_modoConsulta, m_modoFilter;
	Font m_font;
	ItemSelectable m_select;
	JComponent m_comp;
	docServer m_server;
	Dimension m_dim;
	/*session m_session;*/
	GComponent m_component;
	GFormEnumerated m_ff;

	boolean m_multivalued;
	IComponentListener m_componentListener;
	boolean inicializationState;
	ItemList[] m_currentListItemList;
	boolean itemEvent;
	IUserMessageListener m_messageListener;
	KeyListener m_keyListener;
	
	public GListBox(GFormField ff,/*session ses,*/docServer server,/*fieldControl control,*/IComponentListener controlValue,IUserMessageListener messageListener,KeyListener keyListener,Font fuente,boolean modoConsulta,boolean modoFilter)
    {
		/*m_session=ses;*/
		m_server=server;
	    /*m_dim=dim;*/
	    m_modoConsulta=modoConsulta;
	    
	    m_multivalued=ff.isMultivalued();
	    
	    m_font=fuente;
	    
	    m_nullable= ff.isNullable();
	    m_label=ff.getLabel();
	    /*m_listaInicial= (Vector)lista.clone();*/
	    /*m_listaInicial= (Vector)((GFormEnumerated)ff).getValues().clone();*/
	    /*m_control= control;*/
	    m_id= ff.getId();
	    m_id2=ff.getId2();
	    //////////////////////////////////////////////////////
	    m_componentListener=controlValue;

	    m_messageListener=messageListener;
	    
	    m_keyListener=keyListener;
	    
	    m_modoFilter=modoFilter;
	    m_ff=(GFormEnumerated)ff;
	    
	    inicializationState=true;
	    itemEvent=false;
	    /////////////YA ESTABA COMENTADO/////////////////
	    //System.out.println("LB_"+getValue());
		/*if(color!=null){
			if(color.equals("BLUE")) m_comp.setForeground(Color.blue);
			if(color.equals("GREEN")) m_comp.setForeground(Color.green);
			if(color.equals("RED"))	m_comp.setForeground(Color.red);
		}*/
	    ////////////////////////////////////////////////
	    
/*	    System.out.println("Modo filtro GListBox "+m_modoFilter);
	    if( m_modoFilter ){
			/*m_select= new GList( m_dim,lista, new ImageIcon( m_com.getImage("list")) );
			m_comp= ((GList)m_select).getComponent();*/
/*	    	GList list=new GList(ff,com,fuente,colorFondo);
	    	m_select=list;
	    	m_comp=list.getComponent();
	    	//m_select=m_comp;
		}else{
			GComboBox comboBox=new GComboBox(ff);
			comboBox.create();
			m_select= (JComboBox)comboBox.getComponent();
			m_comp=comboBox;
			/*m_select= new JComboBox(lista);
			m_comp=(JComponent)m_select;*/
/*		}
	    
		m_select.addItemListener(this);
*/	    
	    /* m_comp.setFont(m_font.deriveFont(Font.BOLD));*/
	    initLista();
	    /* setInitialSelection();*/
	     
	     
	     //System.out.println("LB_2"+getValue());
    }
	
/*	protected void createComponent()
    {
		/*LISTA TIENE QUE LEERSE DE LOS ATRIBUTOS DE FF*/
/*	    Vector lista=new Vector();//Esto es provisional
	    /*initLista( lista );*/
	    /////////////////////////////////////////////////
	    
/*		if( m_modoFilter ){
			/*m_select= new GList( m_dim,lista, new ImageIcon( m_com.getImage("list")) );
			m_comp= ((GList)m_select).getComponent();*/
/*		}else{
			m_component=new GComboBox(ff);
			/*m_select= new JComboBox(lista);
			m_comp=(JComponent)m_select;*/
/*		}
		m_select.addItemListener(this);
    }
*/	
	/*public JComponent getComponent(){
		return (JComponent)m_select;
	}*/
	
	public GComponent getComponent(){
		/*if( m_select instanceof GComponent )
			return (GComponent)m_select;
		if( m_comp instanceof GComponent )*/
			return (GComponent)m_comp;
		/*return null;*/
	}
	
	public void setItemSelectable(ItemSelectable itemSelectable){
		m_select=itemSelectable;
	}
	
	public void setComponent(GComponent component){
		m_comp=component;
	}
	

	private void initLista(){
		if( m_modoFilter || m_multivalued ){
			/*m_select= new GList( m_dim,lista, new ImageIcon( m_com.getImage("list")) );
			m_comp= ((GList)m_select).getComponent();*/
	    	GList list=new GList(m_ff,m_server,m_font,m_modoConsulta,m_modoFilter,this);
	    	/*m_select=list;
	    	m_comp=list.getComponent();*///Ultimo
	    	//m_select=m_comp;
	    	m_listaInicial=list.getListaInicial();
	    	currentValidatedValue=list.getValorInicial().get(0);
	    	//Esto lo comprobamos porque segun como se haya construido los datos pasados a la lista no se puede castear
			//directamente de Object[] a ItemList[]. Por ejemplo si se ha pasado un vector este internamente crea un new Object[]
			//por lo que luego no podemos hacer el casting a ItemList. Ejemplos de creacion de los datos:
			// Object[] o=new Object[] No se puede castear directamente a ItemList[]
			// Object[] o=new ItemList[] Si se puede castear directamente a ItemList[]
	    	Object[] selected=list.getValorInicial().toArray();
			if(selected instanceof ItemList[])
				m_currentListItemList=(ItemList[])selected;
			else m_currentListItemList= Arrays.copyOf(selected,selected.length,ItemList[].class);

		}else{
			GComboBox comboBox=new GComboBox(m_ff,m_font,m_modoConsulta,m_modoFilter,this);
			/*m_select= (JComboBox)comboBox.getComponent();
			m_comp=comboBox;*///Ultimo
			/*m_select= new JComboBox(lista);
			m_comp=(JComponent)m_select;*/
			m_listaInicial=comboBox.getListaInicial();
			currentValidatedValue=comboBox.getValorInicial();
			m_currentListItemList=new ItemList[1];
			m_currentListItemList[0]=comboBox.getValorInicial();
		}
		/*m_select.addItemListener(this);*/
	}

      public String getLabel(){
            return m_label;
      }

   public void commitValorInicial(){
	if( /*m_modoFilter*/m_multivalued ) return;
  	Iterator itr= m_listaInicial.iterator();
  	while(itr.hasNext()){
            	ItemList it= (ItemList) itr.next();
		it.initialSelection=false;
    	}
     	ItemList current= (ItemList)m_select.getSelectedObjects()[0];
	current.initialSelection=true;
   }

   private void setSelectedItem( ItemList[] lista ){
	   Vector<ItemList> v=new Vector<ItemList>();
		int size=lista.length;
		for(int i=0;i<size;i++)
			v.addElement(lista[i]);
		setSelectedItem(v);
   }
   
   private void setSelectedItem( Vector<ItemList> lista ){
	//System.out.println("PRE SETSEL "+lista.size());
	if( lista==null || lista.size()==0 ) return;
	if( m_select instanceof JComboBox )
		((JComboBox)m_select).setSelectedItem(lista.get(0));
	if( m_select instanceof GList ){
		//System.out.println("ES BOX FILTER");
		((GList)m_select).setSelectedValue(lista);
	}
   }
   
   private boolean isPopupVisible(){
	   boolean visible=false;
	   if( m_select instanceof JComboBox )
		   visible=((JComboBox)m_select).isPopupVisible();
	   if( m_select instanceof GList ){
		   //System.out.println("ES BOX FILTER");
		   visible=((GList)m_select).isPopupVisible();
	   }
	   return visible;
   }

   private void setSelectedItem( ItemList it ){
	if( m_select instanceof JComboBox )
		((JComboBox)m_select).setSelectedItem(it);
	if( m_select instanceof GList )
		((GList)m_select).setSelectedValue(it);
   }


   public int getSize(){
	if( m_select instanceof JComboBox )
		return ((JComboBox)m_select).getModel().getSize();
	if( m_select instanceof GList ){
		//System.outprintln("VALORES "+m_select+" "+(((GList)m_select).getComponent()).getModel());
		//System.outprintln("VALORES TAMANO"+(((GList)m_select).getComponent()).getModel().getSize());
		return (((GList)m_select).getComponent()).getModel().getSize();
	}
	return 0;
   }

   public ItemList getItemAt(int i){
	if( m_select instanceof JComboBox )
		return (ItemList)((JComboBox)m_select).getItemAt(i);
	if( m_select instanceof GList )
		return (ItemList)(((GList)m_select).getComponent()).getModel().getElementAt(i);
	return null;
   }

   public void setValue(Object newValue,Object oldValue) throws AssignValueException{
	   //TODO Cuando estamos aun gestionando un itemStateChanged si se llama a este metodo desde el metodo se produce inconsistencia.
	   //if(!itemEvent)
		   setValue( false, newValue, oldValue );
	   
   }
   //Si new es null y old es null entonces no mantenemos ningun valor de la lista
	private void setValue(boolean notificar,Object value,Object oldValue) throws AssignValueException{
		//TODO Cambiar Object de value por String¿?¿
		/*if( value!=null && !(value instanceof Integer)){
			System.out.println("LIST BOX: setValue: error en cast");
			return;
		}*/
		//System.out.println("LIST BOX:PRE SET");
//		if( (m_modoFilter || m_multivalued) && m_select instanceof GList ){
//			//System.out.println("LIST BOX:RESET");
//			((GList)m_select).initLista(m_listaInicial);
//		}
		
		
		int valor= value!=null?(Integer)value:0;
		int valorOld= oldValue!=null?(Integer)oldValue:0;
		
		//Si ya esta seleccionado no hacemos nada
		Object[] lista= m_select.getSelectedObjects();
		int size=lista.length;
		for(int i=0;i<size;i++)
			if(((ItemList)lista[i]).getIntId()==valor)
				return;

		Vector<ItemList> addItemList=new Vector<ItemList>();
		if(! (valor==0 && valorOld==0)){
			for(int i=0;i<size;i++){
				if(((ItemList)lista[i]).getIntId()!=valorOld/* && ((ItemList)lista[i]).getIntId()!=0 Esto no es necesario si viene bien el valor y el valorOld en todo momento*/)
					addItemList.addElement((ItemList)lista[i]);
			}
		}
		if(valor!=0 || (valor==0 && addItemList.isEmpty())){
			//System.err.println("ListaInicial:"+m_listaInicial.toString());
			int sizePossibleValues=m_listaInicial.size();
			for(int i=0;i<sizePossibleValues;i++){
				ItemList it=m_listaInicial.get(i);
				//System.err.println("Compara: it.getIntId:"+it.getIntId()+" valor:"+valor);
				if(it.getIntId()==valor)
					addItemList.addElement(it);
			}
		}
//		try{
//			for(int i=0;i< getSize();i++){
//				ItemList it= getItemAt(i);
//				if(it.getIntId()==valor){
//	
//	        	                if( /*m_control*/m_controlValue!=null ){
//	//					if( /*m_control.estateInicialization()*/inicializationState){
//	//						System.out.println("WARNING:Entra en GListBox.setValue.inicializationState");
//	//	        	          		
//	//						m_select.removeItemListener(this);
//	//                		setSelectedItem(it);
//	//                		m_select.addItemListener(this);
//	//	                  	currentValidatedValue = it;
//	//	                  	inicializationState=false;
//	//						return;
//	//					}
//						if(notificar && !m_modoConsulta){
//							/*if(m_control.changeRequest(m_session,-1,-1,m_idForm, value))
//								m_control.eventDataChanged(m_session,-1,-1,m_idForm);
//							else{
//	        				       		String msg="Error, ha sido asignado un valor incorrecto.";
//				        	               	if(!isNullable() && isNull())
//	      		        	        			msg+=" El campo " + getLabel() + " no admite valores nulos";
//		  						 m_messageListener.showMessage(msg);
//		                	  			setSelectedItem(currentValidatedValue);
//							}*/
//							/*String[] buf = m_id.split(":");
//					 		Integer valueCls = !buf[2].equals("null")?Integer.parseInt(buf[0]):null;*/
//							IdObjectForm idObjectForm=new IdObjectForm(m_id);
//	        				Integer valueCls = idObjectForm.getValueCls();
//	        				String valueOld=null;
//					 		/*String value=currValue;*/
//	        				if(valueOld!=null){
//	            				if(value!=null)
//	            					m_controlValue.setValueField(m_id,(String)value,valueOld, valueCls, valueCls);
//	            				else m_controlValue.removeValueField(m_id,valueOld, valueCls);
//	            			}else{
//	            				if(value!=null)
//	            					m_controlValue.addValueField(m_id,(String)value,valueCls);
//	            				//else m_controlValue.removeValueField(m_id, value, valueCls);
//	            			}
//						}
//					}
//		            m_select.removeItemListener(this);
//	        		setSelectedItem(it);
//	        		m_select.addItemListener(this);
//	        		
//	        		currentValidatedValue = it;
//	        		/*for(int i=0;i<size;i++)
//	        			m_currentListItemList[i]=(ItemList)lista[i];*/
//	        		
//	        		Object[] listaSelect= m_select.getSelectedObjects();
//	        		int sizeSelect=listaSelect.length;
//	        		//Esto lo comprobamos porque segun como se haya construido los datos pasados a la lista no se puede castear
//	        		//directamente de Object[] a ItemList[]. Por ejemplo si se ha pasado un vector este internamente crea un new Object[]
//	        		//por lo que luego no podemos hacer el casting a ItemList. Ejemplos de creacion de los datos:
//	        		// Object[] o=new Object[] No se puede castear directamente a ItemList[]
//	        		// Object[] o=new ItemList[] Si se puede castear directamente a ItemList[]
//	        		if(listaSelect instanceof ItemList[])
//	        			m_currentListItemList=(ItemList[])listaSelect;
//	        		else m_currentListItemList= Arrays.copyOf(listaSelect,sizeSelect,ItemList[].class);
//	        	}
//			}
//		}catch(NotValidValueException ex){
//			m_messageListener.showErrorMessage(ex.getUserMessage());
//		}
		
		m_select.removeItemListener(this);
		setSelectedItem(addItemList);
		m_select.addItemListener(this);
		
		// Para asignar el current se hace esto porque si en GList solo se deseleccionan valores usando control no habra valor, por lo que tendriamos current
		currentValidatedValue= lista.length>0?(ItemList)lista[0]:null;
		/*for(int i=0;i<size;i++)
			m_currentListItemList[i]=(ItemList)lista[i];*/
		
		Object[] listaSelect= m_select.getSelectedObjects();
		int sizeSelect=listaSelect.length;
		//Esto lo comprobamos porque segun como se haya construido los datos pasados a la lista no se puede castear
		//directamente de Object[] a ItemList[]. Por ejemplo si se ha pasado un vector este internamente crea un new Object[]
		//por lo que luego no podemos hacer el casting a ItemList. Ejemplos de creacion de los datos:
		// Object[] o=new Object[] No se puede castear directamente a ItemList[]
		// Object[] o=new ItemList[] Si se puede castear directamente a ItemList[]
		if(listaSelect instanceof ItemList[])
			m_currentListItemList=(ItemList[])listaSelect;
		else m_currentListItemList= Arrays.copyOf(listaSelect,sizeSelect,ItemList[].class);
	}
	
	boolean hasNullItem(){
		ItemList it= getItemAt(0);
		return it.getId().equals(0);
	}
	String getId2(){
		return m_id2;
	}
/*	public String getValueToString(){
		Object[] lista= m_select.getSelectedObjects();
		if( lista.length==0 ) return null;
		for( int i=0;i<lista.length;i++)
			lista[i]=((ItemList)lista[i]).getId();
		return jdomParser.buildMultivalue(lista);
	}
	public Object getValue(){
		if( m_select.getSelectedObjects().length==0 ) return null;
		if(m_select.getSelectedObjects().length==1){
                    ItemList it = (ItemList) m_select.getSelectedObjects()[0];
                    return new Integer(it.getId());
                }else{
                    ArrayList res= new ArrayList();
		    for( int i=0;i<m_select.getSelectedObjects().length;i++)
			res.add(new Integer((((ItemList)m_select.getSelectedObjects()[i]).getIntId())));
		    return res;
                }
	}
*/	public String getId(){
		return m_id;
	}

	public boolean isNull(){
		Object[] lista=m_select.getSelectedObjects();
            if(lista == null || lista.length==0 ) return true;
            return (((ItemList)m_select.getSelectedObjects()[0]).getIntId()==0);
	}
	public boolean isNull(Object val){
		if( val instanceof Integer )
			return ((Integer)val).intValue()==0;
		if( val instanceof ItemList )
			return ((ItemList)val).getIntId()==0;
		return true;
	}
        public boolean isNullable(){
            return m_nullable;
        }

	public void itemStateChanged(ItemEvent e){
		//System.err.println(e);
		itemEvent=true;
		ItemList[] listItemListOld=null;
		try{
			//Este metodo es llamado tanto para indicar el item seleccionado y para indicar el item que ha sido quitado
			//Asi que solo nos interesa uno de ellos, en este caso el seleccionado
			if(e.getStateChange()==ItemEvent.SELECTED/* && !isPopupVisible()*/){
                 
				if( /*m_control*/m_componentListener==null || m_modoConsulta){
					itemEvent=false;
					return;
				}
	//			if( /*m_control*/m_controlValue!=null ){
	//				if( /*m_control.estateInicialization()*/inicializationState){
	//					currentValidatedValue= (ItemList)m_select.getSelectedObjects()[0];
	//					/*for(int i=0;i<size;i++)
	//						m_currentListItemList[i]=(ItemList)lista[i];*/
	//					Object[] selected=m_select.getSelectedObjects();
	//					
	//					//Esto lo comprobamos porque segun como se haya construido los datos pasados a la lista no se puede castear
	//					//directamente de Object[] a ItemList[]. Por ejemplo si se ha pasado un vector este internamente crea un new Object[]
	//					//por lo que luego no podemos hacer el casting a ItemList. Ejemplos de creacion de los datos:
	//					// Object[] o=new Object[] No se puede castear directamente a ItemList[]
	//					// Object[] o=new ItemList[] Si se puede castear directamente a ItemList[]
	//					if(selected instanceof ItemList[])
	//						m_currentListItemList=(ItemList[])selected;
	//					else m_currentListItemList= Arrays.copyOf(selected,selected.length,ItemList[].class);
	//                  	
	//					inicializationState=false;
	//					return;
	//				}
	//			}
				// Obtenemos los objetos seleccionados
				Object[] lista= m_select.getSelectedObjects();
				int size=lista.length;
				listItemListOld=m_currentListItemList.clone();
				ArrayList<ItemList> addItemList=new ArrayList<ItemList>();
				for(int i=0;i<size;i++){
					addItemList.add((ItemList)lista[i]);
				}
				IdObjectForm idObjectForm=new IdObjectForm(m_id);
				Integer valueCls = idObjectForm.getValueCls();
				ArrayList<ItemList> replacedItemList=new ArrayList<ItemList>();
				for( int i=0;i<size;i++){
				    ItemList it=(ItemList)lista[i];
	                            /*if(!m_control.changeRequest(m_session,-1,-1,m_idForm,new Integer(it.getIntId()))){
	                                String msg = "Ha escrito un valor incorrecto.";
	                                if (!isNullable() && isNull())
	                                    msg += " El campo " + getLabel() +
	                                            " no admite valores nulos";
	                                m_messageListener.showErrorMessage(msg);
	                                setSelectedItem(currentValidatedValue);
	                                return;
	                            }*/
				    /*String[] buf = m_id.split(":");
					Integer valueCls = !buf[2].equals("null")?Integer.parseInt(buf[0]):null;;
					String value=String.valueOf(it.getIntId());
					*/
				    if(!hasValue(it)){
						Integer value=it.getIntId();
						if(value.equals(0))
							value=null;
						Integer valueOld=getNextValueOld(addItemList,replacedItemList);
						if(valueOld!=null && valueOld.equals(0))
							valueOld=null;
						/*if(value.equals(""))
							value=null;*/
						
						if(!m_multivalued){
			    			if(valueOld!=null){
			    				if(value!=null)
			    					m_componentListener.setValueField(m_id,value,valueOld, valueCls, valueCls);
			    				else m_componentListener.removeValueField(m_id,valueOld, valueCls);
			    			}else{
			    				if(value!=null)
			    					m_componentListener.addValueField(m_id,value,valueCls);
			    				//else m_controlValue.removeValueField(m_id, value, valueCls);
			    			}
						}else{
							/*Si es multivalued no reutilizamos el fact haciendo un set como para los de un unico valor ya que:
							  Si, por ejemplo, tenemos dos valores, primero quitamos uno, y luego sustituimos el dejado por el quitado, se estaría enviando a base de datos un del y un set,
							  procesando base de datos antes el set que el del por lo que finalmente se quedaría sin ningun valor
							*/
							if(valueOld!=null)
								m_componentListener.removeValueField(m_id,valueOld, valueCls);
							if(value!=null)
								m_componentListener.addValueField(m_id,value,valueCls);
						}
				    }
	            }
				
				int sizeOld=listItemListOld.length;
				if(size<sizeOld){
					for(int i=0;i<sizeOld;i++){
						ItemList itemList=listItemListOld[i];
						if(!replacedItemList.contains(itemList) && !addItemList.contains(itemList)){
							Integer value=itemList.getIntId();
							m_componentListener.removeValueField(m_id,value, valueCls);
						}
					}
				}
				lista= m_select.getSelectedObjects();//Volvemos a pedir los objetos seleccionados porque las acciones anteriores han podido provocar cambios
				// Para asignar el current se hace esto porque si en GList solo se deseleccionan valores usando control no habra valor, por lo que tendriamos current
				currentValidatedValue= lista.length>0?(ItemList)lista[0]:null;
				/*for(int i=0;i<size;i++)
					m_currentListItemList[i]=(ItemList)lista[i];*/
				
				
				//Esto lo comprobamos porque segun como se haya construido los datos pasados a la lista no se puede castear
				//directamente de Object[] a ItemList[]. Por ejemplo si se ha pasado un vector este internamente crea un new Object[]
				//por lo que luego no podemos hacer el casting a ItemList. Ejemplos de creacion de los datos:
				// Object[] o=new Object[] No se puede castear directamente a ItemList[]
				// Object[] o=new ItemList[] Si se puede castear directamente a ItemList[]
				if(lista instanceof ItemList[])
					m_currentListItemList=(ItemList[])lista;
				else m_currentListItemList= Arrays.copyOf(lista,lista.length,ItemList[].class);
				itemEvent=false;
				
				/*m_control.eventDataChanged(m_session,-1,-1,m_idForm);*/
			}
	    }catch(NotValidValueException ex){
			m_messageListener.showErrorMessage(ex.getUserMessage(),SwingUtilities.getWindowAncestor(this.getComponent()));
			setSelectedItem(listItemListOld);
		}catch(Exception ex){
			m_server.logError(SwingUtilities.getWindowAncestor(this.getComponent()),ex,"Error al seleccionar elemento");
			setSelectedItem(listItemListOld);
	    	ex.printStackTrace();
       }
	}

	/*
	 Obtiene el siguiente valueOld que se puede utilizar para hacer un setValueField. Si en la lista
	 de los anteriores valores hay un valor que no aparece en addItemList(nuevos valores) ni en
	 replacedItemList(valores ya usados) sera el valueOld que utilizaremos. Una vez elegido se añade a
	 la lista replacedItemList para que en las llamadas de los otros values de addItemList no se utilicen
	 los mismos antiguos valores. Si no hay valores para elegir se devuelve null.
	*/
	private Integer getNextValueOld(ArrayList<ItemList> addItemList,ArrayList<ItemList> replacedItemList){
		Integer valueOld=null;
		if(m_currentListItemList!=null){//Si hay alguna seleccion
			ArrayList<ItemList> currentList=new ArrayList<ItemList>();
			int size=m_currentListItemList.length;
			for(int i=0;i<size;i++){
				currentList.add(m_currentListItemList[i]);
			}
			Iterator<ItemList> itr=currentList.iterator();
			boolean found=false;
			while(!found && itr.hasNext()){
				ItemList itemL=itr.next();
				if(!addItemList.contains(itemL) && !replacedItemList.contains(itemL)){
					valueOld=itemL.getIntId();
					replacedItemList.add(itemL);
					found=true;
				}
			}
		}
		return valueOld;
	}
	
	private boolean hasValue(ItemList it){
		int size=m_currentListItemList.length;
		for(int i=0;i<size;i++){
			if(m_currentListItemList[i].equals(it))
				return true;
		}
		
		return false;
	}
	
      public boolean hasChanged() {
		//int seleccion= getSelectedIndex();
//                ItemList itemSelected= (ItemList)m_select.getSelectedObjects()[0];
//                return !itemSelected.isInitialSelected();
		/*for(int i= 0; i< seleccion.length; i++){
                 if(((itemList)getModel().getElementAt(seleccion[i])).isInitialSelected()!=
					isSelectedIndex(seleccion[i])) return true;

		}*/
    	 ArrayList<ItemList> arrayOld=new ArrayList<ItemList>();
    	 Object[] selected=m_select.getSelectedObjects();
    	 int sizeOld=m_currentListItemList.length;
    	 int size=selected.length;
    	 
    	 if(size!=sizeOld) return true;
    	 
    	 for(int i=0;i<sizeOld;i++)
    		 arrayOld.add(m_currentListItemList[i]);
    	 
    	 for(int i=0;i<size;i++){
    		 ItemList itemList=(ItemList)selected[i];
    		 if(!arrayOld.contains(itemList))
    			 return true;
    	 }
    	 
    	 return false;
    	 
	}

	public void initValue() {
//		if( /*m_modoFilter*/m_multivalued ) return;
//	  	Iterator itr= m_listaInicial.iterator();
//	  	while(itr.hasNext()){
//	            	ItemList it= (ItemList) itr.next();
//			it.initialSelection=false;
//	    	}
//	     	ItemList current= (ItemList)m_select.getSelectedObjects()[0];
//		current.initialSelection=true;
		if(m_modoFilter || m_multivalued)
			((GList)getComponent()).initValue();
		else ((GComboBox)getComponent()).initValue();
	}
	 
	public Object getValue() {
		String values=null;
		int size=m_currentListItemList.length;
		if(size!=0)
			values=m_currentListItemList[0].getId();
		for(int i=1;i<size;i++)
			values+=":"+m_currentListItemList[i].getId();
		
		//System.err.println("WARNING: Llamada al metodo GListBox.getValue() el cual no esta implementado");
		return null;
	}

	public void clean() throws ParseException, AssignValueException {
		setValue(false, null, null);
	}

	public IComponentListener getComponentListener() {
		return m_componentListener;
	}
	
	
}
