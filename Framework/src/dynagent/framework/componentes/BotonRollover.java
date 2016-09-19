package dynagent.framework.componentes;

import javax.swing.ImageIcon;

import java.util.HashMap;

import dynagent.framework.utilidades.CreadorIconos;


/**
 * Clase que hereda de JToggleButton y cambia la imagen de su icono dependiendo de
 * si el raton está señalandolo, esta fuera de el o lo ha pulsado.
 * @author Francisco Javier Martinez Navarro
 */
public class BotonRollover extends javax.swing.JToggleButton {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
     * Icono sin aplicarle ninguna operacion para modificarlo
     */
    private javax.swing.ImageIcon iconoNormal;

/**
     * Icono original modificado haciendolo mas claro
     */
    private javax.swing.ImageIcon iconoAclarado;

/**
     * Icono original modificado haciendolo mas oscuro
     */
    private javax.swing.ImageIcon iconoOscurecido;

    /**
     * Indica si esta activado el hacer rollover cuando el boton es seleccionado
     */
    //private boolean activadoImagenSeleccionado;
    /**
     * Indica si esta activado el hacer rollover cuando el raton esta encima del boton
     */
    //private boolean activadoImagenRatonEncima;
    /**
     * Indica si esta activado el hacer rollover cuando el raton esta fuera del boton
     */
    //private boolean activadoImagenRatonFuera;
    
    public final static int ICONO_ACLARADO=1;
    public final static int ICONO_NORMAL=2;
    public final static int ICONO_OSCURECIDO=3;
    
    public final static int IMAGEN_SELECCIONADO=6;
    public final static int IMAGEN_ENCIMA=7;
    public final static int IMAGEN_FUERA=8;
    
    private HashMap<Integer, ImageIcon> mapeadoIconos;
    /**
     * Constructor de la clase para crear un boton con el icono creado a partir de la
     * imagen de la ruta pasada por parametro
     * @param texto Texto que se muestra en el boton, null o vacio para que no tenga texto
     * @param textoUso Texto de descripcion del boton, aparece al situar el raton encima
     * @param rutaImagen Ruta en la que se encuentra la imagen del icono
     * @param sizeImagen Dimension de la imagen, null para que use el tamaño original
     */
    public BotonRollover(String texto, String textoUso, String rutaImagen, java.awt.Dimension sizeImagen) {
        this(texto, textoUso, CreadorIconos.crearIcono(rutaImagen,sizeImagen), null);
    	/*super();
        mapeadoIconos=new HashMap();
        
        setToolTipText(textoUso);
           
        int preferredSize=-1;
        iconoNormal=CreadorIconos.crearIcono(rutaImagen,sizeImagen);
        if(iconoNormal!=null){
            iconoAclarado=CreadorIconos.modificarIcono(iconoNormal,CreadorIconos.ACLARAR);
            iconoOscurecido=CreadorIconos.modificarIcono(iconoNormal,CreadorIconos.OSCURECER);
        
            setRolloverEnabled(true);
            setIcon(iconoOscurecido);
            setRolloverIcon(iconoNormal);
            //setRolloverSelectedIcon(iconoAclarado);
            setSelectedIcon(iconoAclarado);

            preferredSize=this.getPreferredSize().width;
            activadoImagenRatonEncima=true;
            activadoImagenRatonFuera=true;
            activadoImagenSeleccionado=true;
            mapeadoIconos.put(ICONO_ACLARADO,iconoAclarado);
            mapeadoIconos.put(ICONO_NORMAL,iconoNormal);
            mapeadoIconos.put(ICONO_OSCURECIDO,iconoOscurecido);
        }else{
            activadoImagenRatonEncima=false;
            activadoImagenRatonFuera=false;
            activadoImagenSeleccionado=false;
            mapeadoIconos.put(ICONO_ACLARADO,null);
            mapeadoIconos.put(ICONO_NORMAL,null);
            mapeadoIconos.put(ICONO_OSCURECIDO,null);
        }
        
        this.setMaximumSize(new Dimension(preferredSize,-1));
        //this.setMinimumSize(preferredSize);
        //this.setPreferredSize(preferredSize);
        if(texto!=null)
            setText(texto);*/
    } 
    
    /**
     * Constructor de la clase para crear un boton con el icono pasado por parametro
     * @param texto Texto que se muestra en el boton, null o vacio para que no tenga texto
     * @param textoUso Texto de descripcion del boton, aparece al situar el raton encima
     * @param icono Icono que aparece en el boton
     * @param sizeImagen Dimension de la imagen, null para que use el tamaño original
     */
    public BotonRollover(String texto, String textoUso, ImageIcon icono, java.awt.Dimension sizeImagen) {
        super();
        mapeadoIconos=new HashMap<Integer, ImageIcon>();
        
        setToolTipText(textoUso);
        
        //double anchoBotonPreferred=-1;
        //double anchoBotonMaximo=-1;
        
        if(icono!=null && sizeImagen!=null)
            iconoNormal=CreadorIconos.redimensionarIcono(icono,sizeImagen);
        else iconoNormal=icono;
        
        if(iconoNormal!=null){
            iconoAclarado=CreadorIconos.modificarIcono(iconoNormal,CreadorIconos.ACLARAR);
            iconoOscurecido=CreadorIconos.modificarIcono(iconoNormal,CreadorIconos.OSCURECER);
        
            setRolloverEnabled(true);
            setIcon(iconoOscurecido);
            setRolloverIcon(iconoNormal);
            //setRolloverSelectedIcon(iconoAclarado);
            setSelectedIcon(iconoAclarado);
            //System.out.print("Icono: "+iconoNormal.getIconWidth()+" Margin button:"+this.getMargin()+" Gap:"+this.getIconTextGap());
            
            //anchoBotonPreferred=this.getPreferredSize().getWidth();
            //anchoBotonMaximo=this.getMaximumSize().getWidth();
            /*activadoImagenRatonEncima=true;
            activadoImagenRatonFuera=true;
            activadoImagenSeleccionado=true;*/
            mapeadoIconos.put(ICONO_ACLARADO,iconoAclarado);
            mapeadoIconos.put(ICONO_NORMAL,iconoNormal);
            mapeadoIconos.put(ICONO_OSCURECIDO,iconoOscurecido);
        }else{
            /*activadoImagenRatonEncima=false;
            activadoImagenRatonFuera=false;
            activadoImagenSeleccionado=false;*/
            mapeadoIconos.put(ICONO_ACLARADO,null);
            mapeadoIconos.put(ICONO_NORMAL,null);
            mapeadoIconos.put(ICONO_OSCURECIDO,null);
        }
        
        //SETSIZE CON EL GET DIMENSION OBTENIDO
        /*this.getPreferredSize().setSize(anchoBoton, -1);
        this.getMaximumSize().setSize(anchoBoton, -1);*/
        /*this.setMaximumSize(new Dimension(anchoBoton,this.getMaximumSize().height));
        //this.setMinimumSize(preferredSize);
        this.setPreferredSize(new Dimension(anchoBoton,this.getPreferredSize().height));*/
        if(texto!=null)
            setText(texto);
        /*System.out.print(this.getPreferredSize());
        System.out.println(this.getMaximumSize());*/
        /*this.setMaximumSize(new Dimension((int)anchoBotonMaximo,(int)this.getMaximumSize().getHeight()));
        //this.setMinimumSize(preferredSize);
        this.setPreferredSize(new Dimension((int)anchoBotonPreferred,(int)this.getPreferredSize().getHeight()));*/
    } 

    public void setIconoEstado(int estado,int tipoIcono){
    	/*switch(tipoIcono){
	    	case ICONO_ACLARADO:
	    		switch(estado){
	    			case IMAGEN_SELECCIONADO:
	    				setSelectedIcon((ImageIcon)mapeadoIconos.get(tipoIcono));
	    				break;
	    			case IMAGEN_ENCIMA:
	    				setRolloverIcon((ImageIcon)mapeadoIconos.get(tipoIcono));
	    				break;
	    			case IMAGEN_FUERA:
	    				setIcon((ImageIcon)mapeadoIconos.get(tipoIcono));
	    				break;
	    		}
	    		break;
	    	case ICONO_NORMAL:
	    		switch(estado){
    			case IMAGEN_SELECCIONADO:
    				break;
    			case IMAGEN_ENCIMA:
    				break;
    			case IMAGEN_FUERA:
    				break;
	    		}
	    		break;
	    	case ICONO_OSCURECIDO:
	    		switch(estado){
    			case IMAGEN_SELECCIONADO:
    				break;
    			case IMAGEN_ENCIMA:
    				break;
    			case IMAGEN_FUERA:
    				break;
	    		}
	    		break;
	    	case -1:
	    		switch(estado){
    			case IMAGEN_SELECCIONADO:
    				break;
    			case IMAGEN_ENCIMA:
    				break;
    			case IMAGEN_FUERA:
    				break;
	    		}
	    		break;
    	}*/
    	switch(estado){
	    	case IMAGEN_SELECCIONADO:
	    		setSelectedIcon((ImageIcon)mapeadoIconos.get(tipoIcono));
	    		break;
	    	case IMAGEN_ENCIMA:
	    		setRolloverIcon((ImageIcon)mapeadoIconos.get(tipoIcono));
	    		break;
	    	case IMAGEN_FUERA:
	    		setIcon((ImageIcon)mapeadoIconos.get(tipoIcono));
	    		break;
    	}
    }

	@Override
	public void setText(String text) {
		super.setText("<html><p align=LEFT>"+text+"</p></html>");
	}
    
    /**
     * Hacer o no hacer rollover al seleccionar el boton
     * @param activar Activar o no activar el hacer rollover
     */
    /*public void setImagenSeleccionado(boolean activar) {        
        if(activar){
            //if(activadoImagenRatonEncima || activadoImagenRatonFuera)
                setSelectedIcon(iconoAclarado);
            //else setSelectedIcon(iconoNormal);
        }
        else{
        	if(activadoImagenRatonEncima)
        		setSelectedIcon(iconoNormal);
        	else setSelectedIcon(null);
        }
        activadoImagenSeleccionado=activar;
    } */
    
    /**
     * Hacer o no hacer rollover al poner el raton encima del boton
     * @param activar Activar o no activar el hacer rollover
     */
    /*public void setImagenRatonEncima(boolean activar) {        
        if(activar){
            setRolloverIcon(iconoNormal);
            if(activadoImagenRatonFuera)
              setIcon(iconoOscurecido);
            if(activadoImagenSeleccionado)
              setSelectedIcon(iconoAclarado);
        }
        else{
            setRolloverIcon(null);
            if(activadoImagenRatonFuera)
              setIcon(iconoNormal);
            //else if(activadoImagenSeleccionado)
            //  setSelectedIcon(iconoNormal);
        }
        activadoImagenRatonEncima=activar;
    }*/ 

    /**
     * Hacer o no hacer rollover al poner el raton fuera del boton
     * @param activar Activar o no activar el hacer rollover
     */
    /*public void setImagenRatonFuera(boolean activar) {        
        if(activar){
            if(activadoImagenRatonEncima)
                setIcon(iconoOscurecido);
            else setIcon(iconoNormal);
        }
        else setIcon(null);
        activadoImagenRatonFuera=activar;
    } */
 }
