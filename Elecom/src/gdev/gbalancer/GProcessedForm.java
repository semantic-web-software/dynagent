package gdev.gbalancer;

import java.awt.Rectangle;
import java.util.Vector;
import java.util.Enumeration;

import gdev.gfld.GFormFieldList;
import gdev.gen.IViewBalancer;
/**
 * Esta clase representa un formulario con los grupos ya procesados y colocados en una posici�n.
 * It stores one set of positions for the groups.
 */
public class GProcessedForm implements Comparable
{
    /**
     * Constante que se usa para puntuar los formularios y comparar dos formularios.
     * @see #compareTo(Object)
     */
    private static double ASPECT_RATIO_WEIGHT = 500.0;
    /**
     * Constante que se usa para puntuar los formularios y comparar dos formularios.
     * @see #compareTo(Object)
     */
    private static double COMPACTNESS_WEIGHT = 3300.0;

    /**
     * La informaci�n introducida para el formulario, con todos sus grupos y campos.
     */
    protected GFormFieldList m_objFormFieldList;
    /**
     * Los bordes obtenidos (�rea) del rect�ngulo que ocupa el formulario.
     */
    protected Rectangle m_rcBounds;
    /**
     * Lista de grupos con su posici�n correspondiente.
     * Cada elemento del vector es del tipo {@link GProcessedGroup}.
     */
    protected Vector m_vProcessedGroupList;

    /**
     * Constructor de la clase. Le paso como par�metro la informaci�n del formulario le�do del XML
     * @param ffl Formulario le�do del XML
     */
    public GProcessedForm(GFormFieldList ffl)
    {
        m_objFormFieldList = ffl;
    }

    /**
     * Devuelve la informaci�n del formulario que se ha procesado.
     * La informaci�n que devuelve del formulario es la le�da del XML sin procesar.
     * @return GFormFieldList - Es el formulario le�do del XML y al que se hace referencia al procesar el formulario.
     */
    public GFormFieldList getFormFieldList()
    {
        return m_objFormFieldList;
    }
    
    /**
     * Modifica el formulario al que se hace referencia.
     * @param ffl El nuevo formulario al que se har� referencia.
     */
    public void setFormFieldList(GFormFieldList ffl)
    {
        m_objFormFieldList = ffl;
    }
    
    /**
     * Obtiene el �rea que ocupa el formulario procesado entero.
     * @return Rectangle - �rea ocupada por el formulario.
     */
    public Rectangle getBounds()
    {
        return m_rcBounds;
    }
    
    /**
     * Modifica los bordes del formulario (�rea que ocupa)
     * @param area Es el nuevo �rea que tendr� el formulario.
     */
    public void setBounds(Rectangle area)
    {
        m_rcBounds = area;
    }
    
    /**
     * Modifica el vector con la lista de grupos que contiene el formulario.
     * @param v El nuevo vector con la lista de grupos.
     */
    public void setProcessedGroupList(Vector v)
    {
        m_vProcessedGroupList = v;
    }
    
    /**
     * Obtiene el vector con la lista de grupos del formulario. 
     * Cada elemento del vector es del tipo {@link GProcessedGroup}.
     * @return Vector - Contiene la lista de grupos del formulario.
     */
    public Vector getProcessedGroupList()
    {
        return m_vProcessedGroupList;
    }
    
    
    /**
     * Este m�todo devuelve como de buena es la combinacion con respecto a los bordes m�ximos del panel.
     * Sirve para puntuar el formulario.
     * @return double - Ratio obtenido entre el formulario obtenido y los bordes m�ximos permitidos.
     * @see #compareTo(Object)
     */
    private double getAspectRatioDiff()
    {
        double dAspectRatioSpecified = (double)m_objFormFieldList.getPanelWidth()/(double)m_objFormFieldList.getPanelHeight();
        double dAspectRatio = (double)m_rcBounds.width/(double)m_rcBounds.height;
        return Math.abs(dAspectRatioSpecified-dAspectRatio);
        
    }
    /**
     * Este m�todo devuelve cuanto es el procentaje de espacio utilizado en el formulario obtenido.
     * Para ello calcula todas las �reas que ocupa cada campo y las compara con el �rea total del formulario.
     * Sirve para puntuar el formulario.
     * @return double - Porcentaje de espacio utilizado del formulario.
     * @see #compareTo(Object)
     */
    private double getPercentageUse()
    {
        /*double dTotalArea = (double)m_rcBounds.width*(double)m_rcBounds.height;
        double dUsedArea = 0.0;
        Enumeration en = m_vProcessedGroupList.elements();
        while(en.hasMoreElements())
        {
            GProcessedGroup grp = (GProcessedGroup)en.nextElement();
            Rectangle rc = grp.getBounds();
            dUsedArea += rc.width*rc.height;
        }
        return dUsedArea/dTotalArea;*/
        
    	double dTotalArea = (double)m_rcBounds.width*(double)m_rcBounds.height;
        double dUsedArea = 0.0;
        Enumeration en = m_vProcessedGroupList.elements();
        while(en.hasMoreElements())
        {
        	GProcessedGroup grp = (GProcessedGroup)en.nextElement();
            Enumeration en2 = grp.m_vColumnList.elements();
            while(en2.hasMoreElements()){
            	GGrpColumn t = (GGrpColumn)en2.nextElement();
            	Vector v = t.m_vFieldList;
            	for(int i=0; i<v.size();i++){
            		GProcessedField g =(GProcessedField)v.get(i);
            		dUsedArea = dUsedArea + (g.getBounds().width*g.getBounds().height);
            	}
            }
        }
        return dUsedArea/dTotalArea;
    	
        //Otra puntuacion por filas, pero que gasta mas recursos y tarda mas.
    	/*double dTotalArea = (double)m_rcBounds.width*(double)m_rcBounds.height;
        double acumulado = 1.0;
        Enumeration en = m_vProcessedGroupList.elements();
        while(en.hasMoreElements())
        {
        	GProcessedGroup grp = (GProcessedGroup)en.nextElement();
        	double panelGrupo = (double)grp.getBounds().width * (double)grp.getBounds().height;
        	double porcentajeGrupo, porcGrupoPanel;
        	double ancho=0.0, alto=0.0, dUsedArea=0.0;
        	Enumeration en2 = grp.m_vColumnList.elements();
        	while(en2.hasMoreElements()){
            	GGrpColumn t = (GGrpColumn)en2.nextElement();
            	ancho += t.m_iColWidth;
            	Vector v = t.m_vRowList;
            	for(int i=0; i<v.size();i++){
            		GRow g =(GRow)v.get(i);
            		alto += g.getHeightRow();
            	}
            	dUsedArea = dUsedArea + (alto*ancho);
            }
            porcentajeGrupo = dUsedArea/panelGrupo;
            porcGrupoPanel = panelGrupo/dTotalArea;
            
            acumulado = acumulado * porcentajeGrupo * porcGrupoPanel;
        }
        return acumulado;*/
    	
    }
    /**
     * Compara dos formularios y devuelve el que mejor puntuaci�n tiene, es decir, 
     * el que tiene una mejor combinaci�n de los campos y los grupos del formulario.
     * Usa {@link #getPercentageUse()} y {@literal #getAspectRatioDiff()}.
     * La f�rmula para comparar es:
     * <br>({@link #getAspectRatioDiff()}*ASPECT_RATIO_WEIGHT + (1.0-{@link #getPercentageUse()})*COMPACTNESS_WEIGHT)
     * <br>El que tenga menor valor es el mejor puntuado y por tanto el que mejor combinaci�n tiene. 
     * @param o Object
     * @return int
     * @throws ClassCastException
     */
    public int compareTo(Object o) throws ClassCastException
    {
        if(!(o instanceof GProcessedForm))
            throw new ClassCastException();
        GProcessedForm objectToComp=(GProcessedForm) o;
        double dAspectRatioDiffThis = getAspectRatioDiff();
        double dAspectRatioDiffThat = objectToComp.getAspectRatioDiff();
        double dPercentageUseThis = getPercentageUse();
        double dPercentageUseThat = objectToComp.getPercentageUse();
        double thisScore=(dAspectRatioDiffThis*ASPECT_RATIO_WEIGHT + (1.0-dPercentageUseThis)*COMPACTNESS_WEIGHT);
        double thatScore=(dAspectRatioDiffThat*ASPECT_RATIO_WEIGHT + (1.0-dPercentageUseThat)*COMPACTNESS_WEIGHT);
        if ( thisScore < thatScore){
            // El objeto this "es menor" que el pasado por parametro, 
            // y por tanto tendra preferencia a la hora de ser escogido
        	return -1;
        } else if(thisScore > thatScore){
            // El objeto pasado por parametro es menor.
        	return 1;
        }else{
        	return 0;
        }
    }
    
    /**
     * Calcula si hay solapamiento entre el �rea que nos pasan como par�metro y los grupos del formulario.
     * @param rcInput �rea para ver si est� solapada.
     * @return boolean - true si hay solapamiento (false en caso contrario).
     */
    private boolean isOverlapping(Rectangle rcInput)
    {
        Enumeration en = m_vProcessedGroupList.elements();
        while(en.hasMoreElements())
        {
            GProcessedGroup grp = (GProcessedGroup)en.nextElement();
            Rectangle rc = grp.getBounds();
            if(rc.intersects(rcInput))
                return true;
        }
        return false;
    }
    
    /**
     * Este m�todo recoloca los grupos dentro del formulario para que se vean mejor y 
     * tengan una mejor disposici�n.
     * @param balancer - Para obtener los m�rgenes a aplicar.
     */
    public void fineTune(IViewBalancer balancer)
    {
        Enumeration en = m_vProcessedGroupList.elements();
        while(en.hasMoreElements())
        {
            GProcessedGroup grp = (GProcessedGroup)en.nextElement();
            Rectangle rc = grp.getBounds();
            int x = rc.x+rc.width;
            int y = rc.y;
            int width = m_rcBounds.width-x-balancer.getPanelRightMargin();
            int height = rc.height;
            Rectangle rcRest = new Rectangle(x,y,width,height);
            if(!isOverlapping(rcRest))
            {
                rc.width += rcRest.width;
                grp.setBounds(rc);
            }
            grp.fineTune(balancer);
        }
    }
}
