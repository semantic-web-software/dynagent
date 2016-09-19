package gdev.gfld;

import java.util.ArrayList;

/**
 * Esta clase representa una columna en una tabla.
 * 
 */
public class GTableColumn
{
    /** El id de la columna*/
    protected String m_iId;
    /** El índice de la columna (empezando desde 0)*/
    protected int m_iColumn;
    /** El nombre de la columna*/
    protected String m_strLabel;
    /** El código del tipo de la columna*/
    protected int m_iType;
    /** El ancho calculado de la columna*/
    protected int m_iColumnWidth;
    /** Si la columna es editable*/
    protected boolean m_enable;//No se tiene permiso para modificar este campo.
    /** Si la columna está oculta o no*/
    protected boolean m_hide;
    /** Si se puede resumir en una*/
    protected boolean m_total;
    /** Si se puede agrupar en varios valores*/
    protected boolean m_agrupable;
    /** Id de la columna*/
    protected int m_idProp;
    /** Nombre de la clase de los campos que contiene la columna*/
    protected String m_claseCampo;
    /** Ancho de la columna*/
    protected int m_width;
    /** Alto de la columna*/
    protected int m_height;
    /** Longitud de la columna*/
    protected int m_length;
    /** */
    protected String m_ref;
    /** */
    protected boolean m_dobleSizeHeader;
    /** Si acepta el valor nulo*/
    protected boolean m_nullable;
    /** Máscara aplicable*/
    protected String m_mask;
    
    protected ArrayList<GValue> m_valuesPossible;
    
    protected boolean m_creation;
    
    protected boolean m_finder;
    
    protected int m_typeFinder;
    
    protected boolean m_basicEdition;//En principio se permite la edicion de la columna pero siempre que enable este tambien a true
    
    protected boolean m_uniqueValue;//Indica si el valor de esta columna tiene que ser unico en base de datos

    protected Integer m_redondeo;//Para saber el numero de digitos de redondeo en caso de ser un double
    
    /** El id del padre de la columna*/
    protected String m_iIdParent;
    
    public static final int NOT_FINDER=0;
    public static final int NORMAL_FINDER=1;
    public static final int HIDDEN_FINDER=2;
    public static final int CREATION_FINDER=3;//Finder solo aparece si se esta creando
    /** 
     * Constructor por defecto
     *
     */
    public GTableColumn()
    {
    }
    
    /**
     * Constructor por parámetros, donde cada parámetro se corresponde con su atributo.
     * @param id Valor para el atributo {@link #m_iId}
     * @param col Valor para el atributo {@link #m_iColumn}
     * @param strLab Valor para el atributo {@link #m_strLabel}
     * @param type Valor para el atributo {@link #m_iType}
     * @param ref Valor para el atributo {@link #m_ref}
     * @param claseCampo Valor para el atributo {@link #m_claseCampo}
     * @param tapos Valor para el atributo {@link #m_idProp}
     * @param width Valor para el atributo {@link #m_width}
     * @param height Valor para el atributo {@link #m_height}
     * @param length Valor para el atributo {@link #m_length}
     * @param mask Valor para el atributo {@link #m_mask}
     * @param enable Valor para el atributo {@link #m_enable}
     * @param hide Valor para el atributo {@link #m_hide}
     * @param total Valor para el atributo {@link #m_total}
     * @param agrupable Valor para el atributo {@link #m_agrupable}
     * @param dobleSizeHeader Valor para el atributo {@link #m_dobleSizeHeader}
     * @param nullable Valor para el atributo {@link #m_nullable}
     */
    public GTableColumn(String id,String idParent,int col,String strLab,int type,String ref/*,String claseCampo*/,int idProp,int width,int height,int length,String mask,boolean enable,boolean hide,boolean total,boolean agrupable,boolean dobleSizeHeader,boolean nullable,ArrayList<GValue> valuesPossible,boolean creation,int finder,boolean basicEdition,boolean uniqueValue,Integer redondeo)
    {
        m_iId = id;
        m_iIdParent = idParent;
        m_iColumn = col;
        m_strLabel = strLab;
        m_iType = type;
        m_ref=ref;
        /*m_claseCampo=claseCampo;*/
        m_idProp=idProp;
        m_width=width;
        m_height=height;
        m_length=length;
        m_enable=enable;
        m_hide=hide;
        m_total=total;
        m_agrupable=agrupable;
        m_dobleSizeHeader=dobleSizeHeader;
        m_nullable=nullable;
        m_mask=mask;
        m_valuesPossible=valuesPossible;
        m_creation=creation;
        m_typeFinder=finder;
        m_basicEdition=basicEdition;
        m_finder=finder!=NOT_FINDER;
        m_uniqueValue=uniqueValue;
        m_redondeo=redondeo;
    }
	
    /**
     * Obtiene el ID de la columna, es decir, el atributo {@link #m_iId}.
     * @return String - Id de la columna
     */
    public String getId()
    {
        return m_iId;
    }
    /**
     * Modifica el ID de la columna, es decir, el atributo {@link #m_iId}.
     * @param id El nuevo Id de la columna
     */
    public void setId(String id)
    {
        m_iId = id;
    }
    
    /**
     * Obtiene el ID del padre de la columna, es decir, el atributo {@link #m_iIdParent}.
     * @return String - Id del padre de la columna
     */
    public String getIdParent()
    {
        return m_iIdParent;
    }
    /**
     * Modifica el ID del padre de la columna, es decir, el atributo {@link #m_iIdParent}.
     * @param id El nuevo Id del padre de la columna
     */
    public void setIdParent(String id)
    {
        m_iIdParent = id;
    }
    
    /**
     * Obtiene el nombre de la columna, es decir, el atributo {@link #m_strLabel}.
     * @return String - El nombre de la columna
     */
    public String getLabel()
    {
        return m_strLabel;
    }
    /**
     * Modifica el nombre de la columna, es decir, el atributo {@link #m_strLabel}.
     * @param label El nuevo nombre de la columna
     */
    public void setLabel(String label)
    {
        m_strLabel = label;
    }

    /**
     * Obtiene el índice de la columna, es decir, el atributo {@link #m_iColumn}.
     * @return int - El índice de la columna
     */
    public int getColumn()
    {
        return m_iColumn;
    }
    /**
     * Modifica el índice de la columna, es decir, modifica el atributo {@link #m_iColumn}.
     * @param col El nuevo índice de la columna
     */
    public void setColumn(int col)
    {
        m_iColumn = col;
    }

    /**
     * Obtiene el tipo de la columna, es decir, el atributo {@link #m_iType}.
     * @return int - El tipo de la columna
     */
    public int getType()
    {
        return m_iType;
    }

    /**
     * Modifica el tipo de la columna, es decir, modifica el atributo {@link #m_iType}.
     * @param type El nuevo tipo de la columna
     */
    public void setType(int type)
    {
        m_iType = type;
    }
    
    /**
     * Obtiene la referencia de la columna, es decir, el atributo {@link #m_ref}.
     * @return String - La referencia de la columna
     */
    public String getRef()
    {
        return m_ref;
    }
    
    /**
     * Obtiene el nombre de la clase de los campos de la columna, es decir, el atributo {@link #m_claseCampo}.
     * @return String - El nombre de la clase de los campos
     */
    /*public String getClaseCampo()
    {
        return m_claseCampo;
    }*/
    
    /**
     * Obtiene el tapos(id) de la columna, es decir, el atributo {@link #m_idProp}.
     * @return int - El tapos(id) de la columna
     */
    public int getIdProp()
    {
        return m_idProp;
    }
    
    /**
     * Obtiene el ancho de la columna, es decir, el atributo {@link #m_width}.
     * @return int - El ancho de la columna
     */
    public int getWidth()
    {
        return m_width;
    }
    
    /**
     * Obtiene el alto de la columna, es decir, el atributo {@link #m_height}.
     * @return String - El alto de la columna
     */
    public int getHeight()
    {
        return m_height;
    }
    
    /**
     * Obtiene la longitud de la columna, es decir, el atributo {@link #m_length}.
     * @return int - La longitud de la columna
     */
    public int getLength()
    {
        return m_length;
    }
    
    /**
     * Obtiene la máscara de la columna, es decir, el atributo {@link #m_mask}.
     * @return String - La máscara que se le puede aplicar a la columna
     */
    public String getMask(){
    	return m_mask;
    }
    
    /**
     * Obtiene si la columna es editable, es decir, el atributo {@link #m_enable}.
     * @return boolean - Si la columna es editable
     */
    public boolean isEnable()
    {
        return m_enable;
    }
    
    /**
     * Obtiene si la columna es oculta, es decir, el atributo {@link #m_hide}.
     * @return boolean - Si la columna es oculta
     */
    public boolean isHide()
    {
        return m_hide;
    }
    
    /**
     * Obtiene si la columna se puede totalizar (resumir en una fila), es decir, el atributo {@link #m_total}.
     * @return boolean - Si la columna es totalizable
     */
    public boolean isTotal()
    {
        return m_total;
    }
    
    /**
     * Obtiene si la columna es agrupable, es decir, el atributo {@link #m_agrupable}.
     * @return boolean - Si la columna es agrupable
     */
    public boolean isAgrupable()
    {
        return m_agrupable;
    }
    
    public boolean isDobleSizeHeader()
    {
        return m_dobleSizeHeader;
    }
    
    /**
     * Obtiene si la columna acepta el valor nulo o no, es decir, el atributo {@link #m_nullable}.
     * @return boolean - Si la columna acepta el valor nulo
     */
    public boolean isNullable(){
    	return m_nullable;
    }
    
    /**
     * Modifica la referencia de la columna, es decir, el atributo {@link #m_ref}.
     * @param ref La nueva referencia de la columna
     */
    public void setRef(String ref)
    {
        m_ref=ref;
    }
    
    /**
     * Modifica el nombre de la clase de los campos de la columna, es decir, modifica el atributo {@link #m_claseCampo}.
     * @param claseCampo El nuevo nombre de la clase de los campos
     */
 /*   public void setClaseCampo(String claseCampo)
    {
        m_claseCampo=claseCampo;
    }
*/    
    /**
     * Modifica el tapos(id) de la columna, es decir, modifica el atributo {@link #m_idProp}.
     * @param tapos El nuevo tapos(id) de la columna
     */
    public void setIdProp(int idProp)
    {
    	m_idProp=idProp;
    }
    
    /**
     * Modifica el ancho de la columna, es decir, modifica el atributo {@link #m_width}.
     * @param width El nuevo ancho de la columna
     */
    public void setWidth(int width)
    {
        m_width=width;
    }
    
    /**
     * Modifica el alto de la columna, es decir, modifica el atributo {@link #m_height}.
     * @param height El nuevo alto de la columna
     */
    public void setHeight(int height)
    {
        m_height=height;
    }
    
    /**
     * Modifica la longitud de la columna, es decir, el atributo {@link #m_length}.
     * @param length La nueva longitud de la columna
     */
    public void setLength(int length)
    {
        m_length=length;
    }
    
    /**
     * Modifica si la columna es editable, es decir, modifica el atributo {@link #m_enable}.
     * @param enable Nuevo valor del atributo {@link #m_enable}
     */
    public void setEnable(boolean enable)
    {
        m_enable=enable;
    }
    
    /**
     * Modifica si la columna está oculta, es decir, modifica el atributo {@link #m_hide}.
     * @param hide Nuevo valor del atributo {@link #m_hide}
     */
    public void setHide(boolean hide)
    {
        m_hide=hide;
    }
    
    /**
     * Modifica si la columna es totalizable, es decir, modifica el atributo {@link #m_total}.
     * @param total Nuevo valor del atributo {@link #m_total}
     */
    public void setTotal(boolean total)
    {
        m_total=total;
    }
    
    /**
     * Modifica si la columna es agrupable, es decir, modifica el atributo {@link #m_agrupable}.
     * @param agrupable Nuevo valor del atributo {@link #m_agrupable}
     */
    public void setAgrupable(boolean agrupable)
    {
        m_agrupable=agrupable;
    }
    
    public void setDobleSizeHeader(boolean dobleSizeHeader)
    {
        m_dobleSizeHeader=dobleSizeHeader;
    }
    
    /**
     * Modifica si la columna acepta el valor nulo o no, es decir, modifica el atributo {@link #m_nullable}.
     * @param nullable Nuevo valor del atributo {{@link #m_nullable}
     */
    public void setNullable(boolean nullable){
    	m_nullable=nullable;
    }
    
    /**
     * Modifica la máscara a aplicar a la columna, es decir, el atributo {@link #m_mask}.
     * @param mask La nueva máscara que se le puede aplicar a la columna
     */
    public void setMask(String mask){
    	m_mask=mask;
    }

	public ArrayList<GValue> getValuesPossible() {
		return m_valuesPossible;
	}

	public void setValuesPossible(ArrayList<GValue> valuesPossible) {
		m_valuesPossible = valuesPossible;
	}

	public boolean hasCreation() {
		return m_creation;
	}

	public void setCreation(boolean creation) {
		this.m_creation = creation;
	}

	public boolean hasFinder() {
		return m_finder;
	}

	public int getTypeFinder() {
		return m_typeFinder;
	}

	public void setTypeFinder(int finder) {
		m_typeFinder = finder;
		m_finder=finder!=NOT_FINDER;
	}

	public boolean isBasicEdition() {
		return m_basicEdition;
	}

	public void setBasicEdition(boolean edition) {
		m_basicEdition = edition;
	}

	public boolean isUniqueValue() {
		return m_uniqueValue;
	}

	public void setUniqueValue(boolean value) {
		m_uniqueValue = value;
	}

	public Integer getRedondeo() {
		return m_redondeo;
	}

	public void setRedondeo(Integer redondeo) {
		this.m_redondeo = redondeo;
	}

}
