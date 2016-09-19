//Almacena los grupos mencionados en el XML <group></group>

package gdev.gfld;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Dimension;

import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConst;
import gdev.gen.IViewBalancer;
import java.util.Arrays;

import dynagent.common.utils.AccessAdapter;

/**
 * Esta clase representa un grupo obtenido a partir del XML.
 */
public class GFormGroup implements Comparable
{
	/** Objeto que implementa la interfaz IViewBalancer y del que obtenemos los márgenes 
	 * y el algoritmo que irá procesando el formulario (implementados en la clase {@link GViewBalancer}).*/
    protected IViewBalancer m_objViewBalancer;
    /** Este vector contiene todos los campos que están definidos dentro de este grupo.*/
    protected Vector<GFormField> m_vFormFields;
    /** El ID del grupo. Es leído desde el XML y tiene que ser único y por tanto irrepetible.*/
    protected int m_iId;
    /** El nombre (etiqueta) del grupo. */
    protected String m_strLabel;
    /** El orden del grupo, leído del XML. Por defecto tendrá valor 0 (si no se lee nada de orden desde el XML).*/
    protected int m_iOrder;
    /** El título que vamos a mostrar en el grupo.*/
    protected String m_title;
    /** Este atributo indica si hay algún campo con una altura mayor de la definida, es decir, 
     * que no ocupe una fila, como por ejemplo TABLE, LIST, IMAGE...
     * Por defecto en esta versión estará a 'true'.*/
    protected boolean m_bIsUnusualHeightComponentPresent;

    /**
     * El constructor, desde el que nos pasan el IViewBalancer para obtener los márgenes 
     * y el algoritmo de procesado. Crea el vector {@link #m_vFormFields} inicialmente vacío.
     * @param objViewBalancer IViewBalancer
     */
    public GFormGroup(IViewBalancer objViewBalancer)
    {
        m_objViewBalancer = objViewBalancer;
        m_vFormFields = new Vector<GFormField>();
    }
    
    /**
     * Modifica el ID del grupo (atributo {@link #m_iId}).
     * @param id Nuevo ID del grupo.
     */
    public void setId(int id)
    {
        m_iId = id;
    }
    
    /**
     * Obtiene el ID del grupo (atributo {@link #m_iId}).
     * @return int - Devuelve el ID del grupo
     */
    public int getId()
    {
        return m_iId;
    }
    
    /**
     * Modifica la etiqueta del grupo (atributo {@link #m_strLabel}).
     * @param strLabel Nueva etiqueta que queremos que tenga el grupo.
     */
    public void setLabel(String strLabel)
    {
        m_strLabel = strLabel;
    }
    
    /**
     * Obtiene la etiqueta del grupo (atributo {@link #m_strLabel}).
     * @return String - Devuelve la etiqueta del grupo.
     */
    public String getLabel()
    {
        return m_strLabel;
    }
    
    /**
     * Modifica el orden del grupo (atributo {@link #m_iOrder}).
     * @param order int
     */
    public void setOrder(int order)
    {
        m_iOrder = order;
    }
    /**
     * Obtiene el orden del grupo (atributo {@link #m_iOrder}).
     * @return int - Devuelve el orden del grupo.
     */
    public int getOrder()
    {
        return m_iOrder;
    }
    
    /**
     * Obtiene el vector que contiene todos los campos del grupo (atributo {@link #m_vFormFields}).
     * @return Vector - Devuelve un vector con todos los campos del grupo.
     */
    public Vector getFieldList()
    {
        return m_vFormFields;
    }
    
    /**
     * Crea un campo a partir de los parámetros que nos pasan y lo añade al grupo.
     * Llama al método {@link #createField(int, String, String, int, int, String, boolean, boolean, boolean, String, String, int, boolean, int, boolean, int, Vector)} con dichos parámetros.
     * Por último llama al método {@link GFormField#postInitialize()}.
     * 
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
    public int addItem(int fieldType, String id, String idRoot, int priority, String mask, boolean enable, boolean nullable, boolean multivalued, Object defaultVal, String label,
    		String name,int length,boolean commented, int order, boolean visible,int numBotones,Vector vValues,boolean highlighted,boolean directoryType, int rows, Integer redondeo)
    {
    	GFormField newField = createField(fieldType,id,idRoot,priority,mask,enable,nullable,multivalued,defaultVal,label,name,length,commented,order,visible,numBotones,vValues,highlighted,directoryType,rows,redondeo);
    	if(newField==null)
    		return GConst.GRES_E_ERR;
    	m_vFormFields.addElement(newField);
    	newField.postInitialize();
    	return GConst.GRES_S_OK;
    }

    /**
     * Este método añade una tabla al grupo. 
     * Llama al método {@link #createTable(String, int, boolean, String, int, boolean, int, Vector, Vector, int, int, boolean, int, int, int, boolean, int, int, int, int)}
     * con los parámetros que necesita y añade la tabla al grupo.
     * Por último llama al método {@link GFormField#postInitialize()}.
     * 
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
    public int addTable(String id, int priority, boolean enable, boolean nullable, String label, String name,int order,boolean visible,int rows, Vector vColumns,Vector vRows, boolean cuantitativo, int iniVirtualColumn, int atGroupColumn, int headerLine, boolean hideHeader, int numBotones, int widthMin, int widthMax,AccessAdapter accessAdapter, boolean creationRow, boolean finderRow)
    {
        GFormField newField = createTable(id,priority,enable,nullable,label,name,order,visible,rows,vColumns,vRows,cuantitativo,iniVirtualColumn,atGroupColumn,headerLine,hideHeader,numBotones,widthMin,widthMax,accessAdapter, creationRow, finderRow);
        if(newField==null)
            return GConst.GRES_E_ERR;
        m_vFormFields.addElement(newField);
        newField.postInitialize();
        return GConst.GRES_S_OK;
    }
    /**
     * Este método creará el campo correspondiente según el tipo que sea. 
     * El tipo se nos pasa en el parámetro 'fieldType'.
     * Los atributos del campo los asignaremos según los parámetros recibidos en este método.
     *
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
    protected GFormField createField(int fieldType, String id, String idRoot,int priority,
    		String mask,boolean enable,boolean nullable, boolean multivalued,Object defaultVal,
    		String label,String name,int length,boolean commented, int order, boolean visible,int numBotones,Vector vValues,boolean highlighted,boolean directoryType, int rows, Integer redondeo)
    {
    	GFormField newField = null;

    	switch(fieldType)
    	{
    	case GConst.TM_TABLE:
    		newField = new GFormTable();
    		break;
    	case GConst.TM_ENUMERATED:
    		newField = new GFormEnumerated();
    		((GFormEnumerated)newField).setValues(vValues);
    		break;
    	case GConst.TM_INTEGER:
    		newField = new GFormInteger();
    		break;
    	case GConst.TM_REAL:
    		newField = new GFormReal();
    		break;
    	case GConst.TM_TEXT:
    		newField = new GFormText();
    		break;
    	case GConst.TM_FILE:
    		newField = new GFormFile();
    		((GFormFile)newField).setDirectoryType(directoryType);
    		break;
    	case GConst.TM_DATE:
    		newField = new GFormDate();
    		break;
    	case GConst.TM_DATE_HOUR:
    		newField = new GFormDateHour();
    		break;
    	case GConst.TM_HOUR:
    		newField = new GFormHour();
    		break;
    	case GConst.TM_MEMO:
    		newField = new GFormMemo();
    		break;
    	case GConst.TM_BOOLEAN:
    		if(commented){
        		newField = new GFormBooleanComment();
        	}
        	else
        		newField = new GFormBoolean();
    		break;
    	case GConst.TM_BOOLEAN_COMMENTED:
    		/*newField = new GFormBooleanComment();*/
    		if(commented){
        		newField = new GFormBooleanComment();
        	}
        	else
        		newField = new GFormBoolean();
    		break;
    	case GConst.TM_BUTTON:
    		newField = new GFormButton();
    		((GFormButton)newField).setText(label);
    		label="";
    		break;
    	case GConst.TM_IMAGE:
    		newField = new GFormImage();
    		break;
    	default:
    		System.out.println("WARNING: Tipo de Dato no soportado en elecom para "+label);
    		break;
    	}
    	if(newField==null)
    		return null;
    	newField.setViewBalancer(m_objViewBalancer);
    	newField.setId(id);
    	newField.setLabel(label);
    	newField.setName(name);
    	newField.setLength(length);
    	newField.setPriority(priority);
    	newField.setOrder(order);
    	newField.setRows(rows);

    	/*HAY QUE VER QUE ATRIBUTOS SON GENERALES Y CUALES ESPECIFICOS*/
    	newField.setEnabled(enable);
    	newField.setNullable(nullable);
    	newField.setMask(mask);
    	newField.setId2(idRoot);
    	newField.setMultivalued(multivalued);
    	newField.setDefaultVal(defaultVal);
    	newField.setCommented(commented);
    	newField.setNumBotones(numBotones);
    	newField.setHighlighted(highlighted);
    	newField.setRedondeo(redondeo);

    	return newField;
    }


    /**
     * Este método creará la tabla correspondiente según los parámetros recibidos.
     * 
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
    protected GFormField createTable(String id, int priority,boolean enable, boolean nullable, String label, String name,
            int order, boolean visible,int rows,Vector vColumns,Vector vRows, boolean cuantitativo, int iniVirtualColumn, int atGroupColumn, int headerLine, boolean hideHeader, int numBotones, int widthMin, int widthMax, AccessAdapter accessAdapter, boolean creationRow, boolean finderRow )
    {
        GFormTable table = new GFormTable();
        table.setViewBalancer(m_objViewBalancer);
        table.setId(id);
        table.setLabel(label);
        table.setName(name);
        table.setPriority(priority);
        table.setOrder(order);
        table.setEnabled(enable);
        table.setNullable(nullable);
        Enumeration en = vColumns.elements();
        table.setVisibleRowCount(rows);
        table.setCreationRow(creationRow);
        table.setFinderRow(finderRow);
        while(en.hasMoreElements())
        {
            GTableColumn col = (GTableColumn)en.nextElement();
            table.addColumn(col);
        }
        Enumeration enRows = vRows.elements();
        while(enRows.hasMoreElements())
        {
            GTableRow row = (GTableRow)enRows.nextElement();
            table.addRow(row);
        }
        m_bIsUnusualHeightComponentPresent = true;
        //vRows is not used at the moment
        
        table.setCuantitativo(cuantitativo);
        table.setIniVirtualColumn(iniVirtualColumn);
        table.setAtGroupColumn(atGroupColumn);
        table.setHeaderLine(headerLine);
        table.setHideHeader(hideHeader);
        table.setNumBotones(numBotones);
        table.setWidthMin(widthMin);
        table.setWidthMax(widthMax);
        table.setOperations(accessAdapter);
        return table;
    }
    
    /**
     * Obtiene el mínimo área que necesita el grupo para acomodar todos los campos.
     * Es el área total que suman todos los campos del grupo.
     * @return double - Devuelve la suma total de las áreas de los campos del grupo.
     */
    public double getMinArea()
    {
        double dArea = 0.0;
        Enumeration en = m_vFormFields.elements();
        while(en.hasMoreElements())
        {
            GFormField ff = (GFormField)en.nextElement();
            Dimension d = ff.getPreferredSize();
            dArea += (d.width*d.height);
        }
        return dArea;
    }
    /**
     * Este método obtiene el máximo ancho de todos los campos del grupo.
     * @return int - Devuelve el máximo ancho de los campos del grupo.
     */
    public int getMaxComponentWidth()
    {
        int maxWid = 0;
        Enumeration en = m_vFormFields.elements();
        while(en.hasMoreElements())
        {
            GFormField ff = (GFormField)en.nextElement();
            Dimension d = ff.getPreferredSize();
            if(d.width>maxWid)
                maxWid = d.width;
        }
        return maxWid;
    }
    
    /**
     * Este método devuelve true si hay algún componente que tenga un alto insual, es decir,
     * las tablas, listbox, imagenes, etc.
     * Devuelve el atributo {@link #m_bIsUnusualHeightComponentPresent}.
     * Para esta versión, está siempre a cierto este atributo.
     * @return boolean En esta versión devuelve siempre 'true'.
     */
    public boolean isUnusualHeightComponentPresent()
    {
        return m_bIsUnusualHeightComponentPresent;
    }
    
    /**
     * Este método ordena los campos del grupo.
     * En primer lugar ordena todos los campos por ancho, sin tener en cuenta prioridades 
     * ni órdenes. Posteriormente volvemos a ordenar teniendo en cuenta prioridades y órdenes,
     * lo que nos desplazará a su orden correspondiente sólo los campos necesarios 
     * para respetar estos órdenes y prioridades.
     * <br>Hay que tener en cuenta que dentro de un grupo (que tiene orden preestablecido),
     * todos los campos tendrán la misma prioridad, y además ésta será igual a la del campo que tenga mayor prioridad.
     * También dentro de los grupos ordenados, el ancho de todos los componentes a tener en cuenta para ordenar 
     * es el mismo e igual al mayor ancho de todos los campos del grupo. 
     * Esto se debe a que se llama al método {@link #asignaAnchoGrupo(Object[])} al comenzar este método.
     */
    @SuppressWarnings("unchecked")
	public void orderByWidth()
    {
        Object[] array = m_vFormFields.toArray();
        asignaAnchoGrupo(array);
        GFormField.m_bCheckPriority = false;
        Arrays.sort(array);
 //       GFormField.m_bCheckPriority = true;
 //       Arrays.sort(array);
 //       int maxOrder = 0;
/*        for (int i = 0; i < array.length; i++)
        {
            GFormField ff = (GFormField) array[i];
            maxOrder = Math.max(maxOrder, ff.getOrder());
        }*/
/*      if (maxOrder > 0)
        {
            boolean initial = false;
            for (int i = 0; i < array.length; i++)
            {
                GFormField ip = (GFormField) array[i];
                if (ip.getOrder() > 0)
                    initial = true;
                /*if (initial && ip.getOrder() == 0)
                    ip.setOrder(maxOrder + 1);
            }
            //GFormField.m_bCheckPriority = true;
            // if there aren't ordering information, next line will not change first array ordering
            // Arrays.sort(array);
            //GFormField.m_bCheckPriority = false;
        }*/
        GFormField.m_bCheckPriority = true;
        // if there aren't ordering information, next line will not change first array ordering
        Arrays.sort(array);
        GFormField.m_bCheckPriority = false;

        m_vFormFields = new Vector( Arrays.asList(array) );
    }
    
    /**
     * Asigna el ancho de un grupo al ancho máximo de los componentes del grupo.
     * También asigna a todos los campos del grupo la máxima prioridad 
     * de los componentes del grupo.
     * @param array
     */
    private void asignaAnchoGrupo(Object[] array) {
		
    	int max=0, maxPriority=0;
    	
    	for (int i=0; i<array.length; i++){
    		GFormField ip = (GFormField) array[i];
    		if(ip.m_iOrder!=0 && ip.getPreferredSize().width>max)
    			max=ip.getPreferredSize().width;
    		if(ip.m_iOrder!=0 && ip.m_iPriority>maxPriority)
    			maxPriority=ip.m_iPriority;
    	}
    	for (int i=0; i<array.length; i++){
    		GFormField ip = (GFormField) array[i];
    		if(ip.m_iOrder!=0){
    			ip.tamGrupo.width=max;
    			//ip.m_iPriority=maxPriority;
    		}
    		
    	}
    	
		
	}
	/**
     * Comparador de dos grupos. Solo tiene en cuenta el orden establecido en el XML, es decir,
     * el atributo {@link #m_iOrder}.
     * @param o Grupo con el que comparar
     * @return int - Devuelve -1 si el objeto por parámetro es de orden menor. Devuelve 1 en caso contrario.
     * @throws ClassCastException
     */
    public int compareTo(Object o) throws ClassCastException
    {
        if(!(o instanceof GFormGroup))
            throw new ClassCastException();
        GFormGroup objectToComp=(GFormGroup) o;
        if (this.m_iOrder < objectToComp.m_iOrder)
            return -1;
        return 1;
    }
    
    /**
     * Obtiene el título del grupo.
     * @return String - Devuelve el título del grupo.
     */
    public String getTitle(){
    	return m_title;
    }
    
    /**
     * Modifica el título del grupo (atributo {@link #m_title}).
     * @param title El nuevo título que queremos que tenga el grupo.
     */
    public void setTitle(String title){
    	m_title=title;
    }

}
