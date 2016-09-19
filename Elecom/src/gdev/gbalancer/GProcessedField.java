

package gdev.gbalancer;

import java.awt.Rectangle;
import gdev.gfld.GFormField;
import gdev.gen.IViewBalancer;
/**
 * Esta clase representa un campo ya procesado y por tanto colocado en una posición concreta (fila, columna, subcolumna, tamaño, posición...)
 * @author Dynagent
 */
public class GProcessedField
{
    /**
     * Este atributo hace referencia a un campo que ha sido procesado, y cuya información
     * se extrajo del XML. Se nos pasa la referencia a dicho campo para que podamos acceder 
     * a la información que necesitemos.
     */
    protected GFormField m_objFormField;

    /**
     * El área total que ocupa el campo en el formulario, incluyendo la etiqueta y sus componentes.
     * 
     */
    protected Rectangle m_rcBounds;
    
    /**
     * El área que ocupa la etiqueta
     */
    protected Rectangle m_rcLabelBounds;
    
    /**
     * El área que ocupa el componente
     */
    protected Rectangle m_rcComponentBounds;
    
    /**
     * El área que ocupa el componente secundario
     */
    protected Rectangle m_rcCompSecBounds;

    /**
     * Índice de la columna del grupo donde el campo ha sido colocado.
     */
    protected int m_iColumn;
   
    /**
     * Índice de la subcolumna (dentro de una misma columna) donde el campo ha sido colocado.
     * Si la columna no tiene subcolumnas, este índice será 0.
     * 
     */
    protected int m_iSubColumn;
    
    /**
     * Índice de la fila del grupo donde ha sido colocado el campo.
     */
    protected int m_iRow;
    
    
    /**
     * Constructor
     * @param ff Es la referencia al campo procesado. 
     * Se almacenará en el atributo {@link #m_objFormField}.
     */
    public GProcessedField(GFormField ff)
    {
        m_objFormField = ff;
    }
    
    /**
     * Devuelve la información del campo referenciado que se ha procesado (sólo la información extraída del XML, no la posición conreta).
     * @return GFormField - Es el campo referenciado
     */
    public GFormField getFormField()
    {
        return m_objFormField;
    }
    
    /**
     * Modificador del campo referenciado
     * @param ff Es el nuevo campo al que se quiere referenciar.
     */
    public void setFormField(GFormField ff)
    {
        m_objFormField = ff;
    }
    
    /**
     * Nos devuelve el área (tipo Rectangle) que ocupa el campo procesado, incluída la posición x,y. 
     * @return Rectangle - El área y la posición del campo.
     */
    public Rectangle getBounds()
    {
        return m_rcBounds;
    }
    
    /**
     * Modificador de los bordes del campo
     * @param area El nuevo área ocupado por el campo
     */
    public void setBounds(Rectangle area)
    {
        m_rcBounds = new Rectangle(area);
    }
    
    /**
     * Nos devuelve el área (tipo Rectangle) que ocupa la etiqueta del campo procesado, incluída la posición x,y.
     * @return Rectangle - Área que ocupa la etiqueta
     */
    public Rectangle getLabelBounds()
    {
        return m_rcLabelBounds;
    }
    /**
     * Modificador del área de la etiqueta.
     * @param area Es el nuevo área que ocupará la etiqueta.
     */
    public void setLabelBounds(Rectangle area)
    {
        m_rcLabelBounds = new Rectangle(area);
    }
    
    /**
     * Nos devuelve el área (tipo Rectangle) que ocupa el primer componente del campo procesado, incluída la posición x,y.
     * @return Rectangle - El área y la posición del primer componente del campo.
     */
    public Rectangle getComponentBounds()
    {
        return m_rcComponentBounds;
    }
    /**
     * Modificador del área del primer componente.
     * @param area Es el nuevo área que ocupará el componente.
     */
    public void setComponentBounds(Rectangle area)
    {
        m_rcComponentBounds = new Rectangle(area);
    }
    
    /**
     * Nos devuelve el área (tipo Rectangle) que ocupa el componente secundario del campo procesado, incluída la posición x,y.
     * @return Rectangle - El área y la posición del componente secundario del campo.
     */
    public Rectangle getComponentSecundarioBounds()
    {
        return m_rcCompSecBounds;
    }
    
    /**
     * Modificador del área del componente secundario.
     * @param area Es el nuevo área que ocupará el componente secundario.
     */
    public void setComponentSecundarioBounds(Rectangle area)
    {
    	m_rcCompSecBounds = new Rectangle(area);
    }
    
    /**
     * Modifica el índice de la columna del campo procesado
     * @param col Nuevo índice de la columna del campo.
     */
    public void setColumn(int col)
    {
        m_iColumn = col;
    }
    /**
     * Devuelve el índice de la columna del campo
     * @return int - Índice de la columna
     */
    public int getColumn()
    {
        return m_iColumn;
    }
    
    /**
     * Modificador de la subcolumna del campo.
     * @param subCol Es el nuevo índice de la subcolumna.
     */
    public void setSubColumn(int subCol)
    {
        m_iSubColumn = subCol;
    }
    /**
     * Devuelve el índice de la subcolumna del campo procesado.
     * @return int - Índice de la subcolumna.
     */
    public int getSubColumn()
    {
        return m_iSubColumn;
    }
    
    /**
     * Modificador del índice de la fila que ocupa el campo procesado en el grupo.
     * @param row Es el índice de la nueva fila.
     */
    public void setRow(int row)
    {
        m_iRow = row;
    }
    /**
     * Devuelve el índice de la fila del campo.
     * @return int - Índice de la fila.
     */
    public int getRow()
    {
        return m_iRow;
    }
    public void fineTune(IViewBalancer balancer)
    {
    }
    
    public GProcessedField clone(){
    	GProcessedField gf= new GProcessedField(this.getFormField());
    	gf.setBounds(this.getBounds());
    	gf.setColumn(this.getColumn());
    	gf.setComponentBounds(this.getComponentBounds());
    	gf.setComponentSecundarioBounds(this.getComponentSecundarioBounds());
    	gf.setFormField(this.getFormField());
    	gf.setLabelBounds(this.getLabelBounds());
    	gf.setRow(this.getRow());
    	gf.setSubColumn(this.getSubColumn());
    	return gf;
    }
    

}
