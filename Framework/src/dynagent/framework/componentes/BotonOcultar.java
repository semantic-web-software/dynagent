
package dynagent.framework.componentes;


import javax.swing.ImageIcon;

import dynagent.framework.utilidades.CreadorIconos;

import java.awt.Dimension;

/**
 * Clase que hereda de JButton que cambia de icono al pulsarlo. Posee dos iconos,
 * un icono que es una flecha para un lado y otro icono con otra flecha pero para
 * el lado contrario. Se puede utilizar para que conforme se va pulsando el boton
 * ir llamando al metodo cambiarEstado() para que se vaya alternando el icono que
 * muestra.
 * @author Francisco Javier Martinez Navarro
 */
public class BotonOcultar extends javax.swing.JButton{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Constante que indica que el icono apunte a la izquierda y al pulsarlo cambie a la
     * derecha
     */
    public static final int LEFT_TO_RIGHT=0;
    /**
     * Constante que indica que el icono apunte a la derecha y al pulsarlo cambie a la
     * izquierda
     */
    public static final int RIGHT_TO_LEFT=1;
    /**
     * Constante que indica que el icono apunte hacia arriba y al pulsarlo cambie hacia
     * abajo
     */
    public static final int TOP_TO_BOTTOM=2;
    /**
     * Constante que indica que el icono apunte hacia abajo y al pulsarlo cambie hacia
     * arriba
     */
    public static final int BOTTOM_TO_TOP=3;
    /**
     * Icono que se muestra originalmente
     */
    private ImageIcon iconoOriginal;
    /**
     * Icono que se muestra cuando se pulsa el boton
     */
    private ImageIcon iconoDespues;
    
    /**
     * Atributo que indica si el estado se encuentra con el icono original
     */
    private boolean estadoOriginal;
    
    /**
     * Constructor para crear una instancia de la clase en la que se indica el texto
     * que aparece al poner encima el raton
     * @param textoUso Texto de descripcion del boton, aparece al situar el raton encima. null para que no aparezca texto
     */
    public BotonOcultar(String textoUso) {
        this(textoUso,LEFT_TO_RIGHT,null);
    }
    
    /**
     * Constructor para crear una instancia de la clase en la que se indica el texto
     * que aparece al poner encima el raton, el tipo de orientacion del icono del boton
     * y las dimensiones del icono.
     * @param textoUso Texto de descripcion del boton, aparece al situar el raton encima. null o vacio para que no aparezca texto
     * @param tipoOrientacionIcono Orientacion de las flechas que aparecen en el boton.
     * Su valor puede ser:
     *    BotonOcultar.LEFT_TO_RIGHT
     *    BotonOcultar.RIGHT_TO_LEFT
     *    BotonOcultar.TOP_TO_BOTTOM
     *    BotonOcultar.BOTTOM_TO_TOP
     * @param sizeIcono Dimension de los iconos, null para tamaño original
     */
    public BotonOcultar(String textoUso,int tipoOrientacionIcono,Dimension sizeIcono) {
        super();
        this.setText("");
        this.setToolTipText(textoUso);
        this.setActionCommand("EstadoOriginal");
        setIconos(tipoOrientacionIcono,sizeIcono);
    }

    /**
     * Cambiar de estado. Al cambiar de estado se le indica que el boton utilice el
     * otro icono.
     */
    public void cambiarEstado(){
        if(estadoOriginal){
            this.setIcon(iconoDespues);
            this.setActionCommand("EstadoDespues");
        }
        else{
            this.setIcon(iconoOriginal);
            this.setActionCommand("EstadoOriginal");
        }
        estadoOriginal=!estadoOriginal;
        this.revalidate();
    }
    
    /**
     * Obtener el estado del boton
     * @return True estado original, False estado despues
     */
    public boolean getEstado(){
    	return estadoOriginal;
    }
    /**
     * Redimensionar el tamaño de los iconos
     * @param sizeIcono Nueva Dimension de los iconos
     */
    public void redimensionarIconos(Dimension sizeIcono){
        iconoOriginal=CreadorIconos.redimensionarIcono(iconoOriginal,sizeIcono);
        iconoDespues=CreadorIconos.redimensionarIcono(iconoDespues,sizeIcono);
        if(estadoOriginal)
            this.setIcon(iconoOriginal);
        else this.setIcon(iconoDespues);
        this.revalidate();
    }
    
    /**
     * Establecer el boton a su estado original o al contrario
     * @param original True estado original, False estado despues
     */
    public void setEstado(boolean original){
    	if(original){
    		this.setIcon(iconoOriginal);
            this.setActionCommand("EstadoOriginal");
        }
        else{
        	this.setIcon(iconoDespues);
            this.setActionCommand("EstadoDespues");
        }
    	estadoOriginal=original;
        this.revalidate();
    }
    
    /**
     * Establecer los iconos que muestra el boton
     * @param tipoOrientacionIcono Orientacion de las flechas que aparecen en el boton.
     * Su valor puede ser:
     *    BotonOcultar.LEFT_TO_RIGHT
     *    BotonOcultar.RIGHT_TO_LEFT
     *    BotonOcultar.TOP_TO_BOTTOM
     *    BotonOcultar.BOTTOM_TO_TOP
     * @param sizeIcono Dimension del icono, null para el tamaño original del icono
     */
    public void setIconos(int tipoOrientacionIcono,Dimension sizeIcono){
        /*Se podria utilizar una sola imagen y rotarla*/
        switch(tipoOrientacionIcono){
            case LEFT_TO_RIGHT:
                iconoOriginal=CreadorIconos.crearIcono("imagenes/Back16.gif",sizeIcono);
                iconoDespues=CreadorIconos.crearIcono("imagenes/Forward16.gif",sizeIcono);
                break;
            case RIGHT_TO_LEFT:
                iconoOriginal=CreadorIconos.crearIcono("imagenes/Forward16.gif",sizeIcono);
                iconoDespues=CreadorIconos.crearIcono("imagenes/Back16.gif",sizeIcono);
                break;
            case TOP_TO_BOTTOM:
                iconoOriginal=CreadorIconos.crearIcono("imagenes/Up16.gif",sizeIcono);
                iconoDespues=CreadorIconos.crearIcono("imagenes/Down16.gif",sizeIcono);
                break;
            case BOTTOM_TO_TOP:
                iconoOriginal=CreadorIconos.crearIcono("imagenes/Down16.gif",sizeIcono);
                iconoDespues=CreadorIconos.crearIcono("imagenes/Up16.gif",sizeIcono);
                break;
        }
        this.setIcon(iconoOriginal);
        estadoOriginal=true;
    }
    
}
