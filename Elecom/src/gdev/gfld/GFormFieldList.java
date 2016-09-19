//Clase principal que procesa la entrada del XML

package gdev.gfld;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IViewBalancer;
import java.util.Arrays;

import dynagent.common.utils.AccessAdapter;

/**
 * Esta clase contiene los grupos definidos en el XML.
 * Otras clases utilizan a ésta para procesar la entrada XML.
 */
public class GFormFieldList
{
	/** Objeto que implementa la interfaz IViewBalancer y del que obtenemos los márgenes 
	 * y el algoritmo que irá procesando el formulario (en la clase {@link GViewBalancer}.*/
    protected IViewBalancer m_objViewBalancer;
    /** Esta 'HashTable' contiene todos los grupos del formulario
     * La primaryKey de la tabla hash es el ID del grupo.*/
    private Hashtable m_hGroupList;
    /** Este vector contiene todos los grupos, y lo utilizamos simplemente para facilitar los cálculos.*/
    private Vector m_vGroupList;
    /** El ancho que debe ocupar como máximo el panel, que se nos pasa desde el núcleo de la aplicación.*/
    protected int m_iPanelWidth;
    /** El alto que debe ocupar como máximo el panel, que se nos pasa desde el núcleo de la aplicación.
     * En caso de que el formulario no quepa, este alto se sobrepasará.*/
    protected int m_iPanelHeight;
    /** Título que le damos al formulario. */
    protected String m_title;
    
    /**
     * El constructor, desde el que nos pasan el IViewBalancer para obtener los márgenes 
     * y el algoritmo de procesado. Crea la tabla hash del atributo {@link #m_hGroupList} 
     * inicialmente vacía.
     * @param objViewBalancer IViewBalancer
     */
    public GFormFieldList(IViewBalancer objViewBalancer)
    {
        m_objViewBalancer = objViewBalancer;
        m_hGroupList = new Hashtable();
        m_title="";
    }
    
    /**
     * Obtiene el grupo cuyo ID es igual al parámetro que nos pasan.
     * @param groupId ID del grupo que queremos obtener
     * @return GFormGroup - Devuelve el grupo del ID correspondiente.
     */
    protected GFormGroup getGroup(int groupId)
    {
        return (GFormGroup)m_hGroupList.get(new Integer(groupId));
    }
    
    /**
     * Añade un grupo al formulario, añadiéndolo al atributo {@link #m_hGroupList}, cuya
     * clave es el ID del grupo.
     * @param groupId ID del grupo.
     * @param strLabel Nombre (etiqueta) del grupo.
     * @param order Orden que tiene el grupo para ser colocado en el formulario.
     * @return int - Devuelve el código correspondiente que indica que todo ha ido bien.
     */
    public int addGroup(int groupId,String strLabel,int order)
    {
        if(getGroup(groupId)!=null)
            return GConst.GRES_S_OK;
        GFormGroup grp = new GFormGroup(m_objViewBalancer);
        grp.setId(groupId);
        grp.setLabel(strLabel);
        grp.setOrder(order);
        m_hGroupList.put(new Integer(groupId),grp);
        return GConst.GRES_S_OK;
    }
    

    /**
     * Añade un campo a un grupo. Obtiene el grupo al que queremos añadir a partir del parámetro groupID,
     * y el resto de parámetros los utiliza para llamar a {@link GFormGroup#addItem(int, String, String, int, int, String, boolean, boolean, boolean, String, String, int, boolean, int, boolean, int, Vector)} con el grupo correspondiente.
     *
     * @param groupId El ID del grupo dónde está el campo y por tanto donde queremos que sea añadido.
     * @param fieldType Es el tipo del campo que queremos añadir.
     * @param id Es '0@tapos', donde tapos es el verdadero ID del campo leído del XML.
     * @param id2 El ID del formulario entero al que pertence el Item (campo).
     * @param priority La prioridad del campo en el grupo. A mayor prioridad, antes colocaremos el campo en el grupo.
     * @param mask Se usa en GTable y GEdit para aplicar la máscara al valor del campo.
     * @param enable Si se puede editar el campo o no.
     * @param nullable Si el valor del campo puede ser nulo.
     * @param multivalued Si puede tener multiples valores el campo.
     * @param defaultVal Valor por defecto del campo.
     * @param label Etiqueta del campo.
     * @param length Longitud del componente del campo.
     * @param commented Si el campo tiene un componente secundario con un campo de texto. Sólo se utiliza en los CheckBox
     * @param order Orden del campo.
     * @param visible Si se muestra el campo o no.
     * @param numBotones Número de botones.
     * @param vValues Sirve sólo para los enumerados. Contiene los distintos valores posibles.
     * @param rows 
     * @return int - Devuelve un código según si el método a funcionado correctamente o ha tenido algun error.
     */
    public int addItem(int groupId, int fieldType, String id, String idRoot, int priority, String mask, boolean enable, boolean nullable, boolean multivalued, Object defaultVal, String label,
    		String name,int length,boolean commented, int order, boolean visible,int numBotones,Vector vValues,boolean highlighted,boolean directoryType, int rows, Integer redondeo)
    {
    	GFormGroup group = getGroup(groupId);
    	if(group==null)
    		return GConst.GRES_E_ERR;
    	return group.addItem(fieldType,id,idRoot,priority,mask,enable,nullable,multivalued,defaultVal,label,name,length,commented,order,visible,numBotones,vValues,highlighted,directoryType,rows,redondeo);
    }

    /**
     * Este método añade una tabla a un grupo. Obtiene el grupo al que queremos añadir la tabla a partir del parámetro groupID,
     * y el resto de parámetros los utiliza para llamar a {@link GFormGroup#addTable(String, int, boolean, String, int, boolean, int, Vector, Vector, int, int, boolean, int, int, int, boolean, int, int, int, int)} y añadir la tabla al grupo correspondiente.
     * 
     * @param groupId El ID del grupo donde vamos a añadir la tabla.
     * @param id El ID de la tabla que se va a añadir.
     * @param priority La prioridad de la tabla en el grupo. A mayor prioridad, antes colocaremos la tabla en el grupo.
     * @param enable Si se puede editar la tabla o no.
     * @param label Etiqueta de la tabla.
     * @param order Orden de la tabla.
     * @param visible Si se muestra la tabla o no.
     * @param rows Número de filas de la tabla.
     * @param vColumns Vector con las columnas que contiene la tabla.
     * @param vRows Vector con las filas que contiene la tabla.
     * @param cuantitativo Si varios registros de la tabla son resumibles en uno.
     * @param iniVirtualColumn
     * @param atGroupColumn
     * @param headerLine El número de lineas que tiene que ocupar la cabecera.
     * @param hideHeader Si oculta la cabecera de la tabla
     * @param numBotones Número de botones que tiene la tabla.
     * @param widthMin Ancho mínimo de la tabla.
     * @param creationAllowed 
     * @param access Accesos que tiene el usuario sobre esta tabla (permisos).
     * @return int - Devuelve un código según si el método a funcionado correctamente o ha tenido algun error.
     */
    public int addTable(int groupId, String id, int priority, boolean enable, boolean nullable, String label, String name, int order,boolean visible,int rows, Vector vColumns,Vector vRows, boolean cuantitativo, int iniVirtualColumn, int atGroupColumn, int headerLine, boolean hideHeader, int numBotones, int widthMin, int widthMax, AccessAdapter accessAdapter, boolean creationRow, boolean finderRow)
    {
        GFormGroup group = getGroup(groupId);
        if(group==null)
            return GConst.GRES_E_ERR;
        return group.addTable(id,priority,enable,nullable,label,name,order,visible,rows,vColumns,vRows,cuantitativo,iniVirtualColumn,atGroupColumn,headerLine,hideHeader,numBotones,widthMin,widthMax,accessAdapter,creationRow,finderRow);
    }
    
    /**
     * Nos devuelve el vector con todos los grupos del formulario (obteniéndolos a partir del atributo {@link #m_hGroupList}).
     * @return Vector - Este vector contiene todos los grupos del formulario.
     */
    public Vector getGroupList()
    {
        if(m_vGroupList==null)
            return new Vector(m_hGroupList.values());
        return m_vGroupList;
    }
    
    /**
     * Modifica el ancho del panel al que nos queremos ajustar como máximo.
     * @param wid Nuevo ancho del panel al que nos queremos ajustar
     */
    public void setPanelWidth(int wid)
    {
        m_iPanelWidth = wid;
    }
    /**
     * Obtiene el ancho máximo del panel que debe tener el formulario. 
     * @return int - Devuelve el ancho máximo que debe tener el formulario.
     */
    public int getPanelWidth()
    {
        return m_iPanelWidth;
    }

    /**
     * Modifica el alto del panel al que nos queremos ajustar como máximo.
     * @param hgt Nuevo alto del panel al que nos queremos ajustar
     */
    public void setPanelHeight(int hgt)
    {
        m_iPanelHeight = hgt;
    }
    /**
     * Obtiene el alto máximo del panel que debe tener el formulario. 
     * @return int - Devuelve el alto máximo que debe tener el formulario.
     */
    public int getPanelHeight()
    {
        return m_iPanelHeight;
    }
    
    /**
     * Ordena todos los grupos según el orden establecido en {@link GFormGroup#compareTo(Object)}. 
     * El resultado lo almacenará en el vector de grupos, es decir, en el atributo {@link #m_vGroupList}.
     */
    public void sortByOrder()
    {
        Object[] array = m_hGroupList.values().toArray();
        Arrays.sort(array);
        m_vGroupList = new Vector( Arrays.asList(array) );
    }
    
    /**
     * Obtiene el título (etiqueta) que tiene el grupo.
     * @return String - El nombre del grupo
     */
    public String getTitle(){
    	return m_title;
    }
    
    /**
     * Modifica el título del grupo.
     * @param title Es el nuevo título que queremos que tenga el grupo.
     */
    public void setTitle(String title){
    	m_title=title;
    }
    
    /**
     * Elimina el grupo correspondiente al ID que nos pasan como parámetro.
     * @param groupId ID del grupo que queremos eliminar.
     */
    public void removeGroup(int groupId){
    	m_hGroupList.remove(new Integer(groupId));
    	if(m_vGroupList!=null)
    		m_vGroupList.remove(new Integer(groupId));
    }
    
}
