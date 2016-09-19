package gdev.gfld;

import java.awt.Dimension;
import java.awt.Insets;

import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gen.IViewBalancer;
import gdev.gen.GConst;

/**
 * Esta clase representa un campo en general del formulario leído del XML.
 * Es una clase abstracta, y los métodos {@link #getMinimumComponentDimension()} y {@link #getType()} 
 * se implementarán en cada clase dependiendo del tipo de campo que sea.
 * La mayoría de los atributos de esta clase se obtienen a partir del XML.
 */
public abstract class GFormField implements Comparable
{
	/** Objeto que implementa la interfaz IViewBalancer y del que obtenemos los márgenes 
	 * y el algoritmo que irá procesando el formulario (en la clase {@link GViewBalancer}.*/
    protected IViewBalancer m_objViewBalancer;
    /** ID del campo. Se lee del XML. Es un identificador único.*/
    protected String m_iId;
    /** La etiqueta del campo*/
    protected String m_strLabel;
    /** El name del campo*/
    protected String m_strName;
    /** El orden del campo. Por defecto, cuando no tiene orden, este valor es 0. 
     * Este orden se referirá a un orden FLOTANTE de campos.*/
    protected int m_iOrder=0;
    /** La prioridad del campo. Por defecto, la prioridad es 0.*/
    protected int m_iPriority=0;
    /** Número de caracteres, para expresar la longitud del campo.
     *  Por defecto valdrá -1.*/
    protected int m_iLength=-1;
    /** El tamaño estimado del campo*/
    protected Dimension m_dimPreferredSize;
    /** Este atributo sirve para guardar la maxima dimension de un atributo del grupo ordenado*/
    protected Dimension tamGrupo;
    /** Indica si la etiqueta se coloca encima del componente (para las tablas y campos tipo MEMO)*/
    protected boolean m_bTopLabel = false;
    /** Indica si el campo es editable*/
    protected boolean m_enabled = true;
    /** Indica si el campo acepta el valor nulo*/
    protected boolean m_nullable = true;
    /** ID numérico del campo, leído del XML*/
    protected int m_idProp = -1;
    /** Máscara que se le aplica al campo*/
    protected String m_mask = null;
    /** El ID del formulario entero al que pertenece*/
    protected String m_idRoot = null;
    /** Indica si el campo acepta varios valores*/
    protected boolean m_multivalued = false;
    /** Indica el valor por defecto del campo*/
    protected Object m_defaultVal = null;
    /** Indica el número de botones*/
    protected int m_numBotones = 0;
    /** Atributo para ver si tiene un comentario o no, es decir, si en el componente secundario
     * tiene un campo de texto para introducir un comentario sobre el campo.*/
    protected boolean comentado=false;
    
    //Following two attributes are used in ordering only
    /** Atributo para que tenga en cuenta las prioridades a la hora de ordenar los campos*/
    protected static boolean m_bCheckPriority=false;
    /** Atributo para que ordene por ancho los campos, a la hora de ordenar.*/
    protected boolean m_bWidthOrdering = true;
    
    protected boolean highlighted = false;
	private int rows = -1;
	protected Integer redondeo;
    
        
    /**
     * Este método devuelve el tipo del campo al que representa.
     * Es un método abstracto porque será implementado en la clase correspondiente 
     * al tipo de campo que representa.
     * Observe {@link GConst} para los diferentes valores  según el tipo.
     * @return int - Devuelve el código del tipo de campo al que representa.
     */
    abstract public int getType();

    /**
     * Modifica el ID del campo
     * @param id Nuevo ID del campo
     */
    public void setId(String id)
    {
        m_iId = id;
    }
    
    /**
     * Obtiene el ID del campo
     * @return int - Devuelve una cadena correspondiente al ID del campo
     */
    public String getId()
    {
        return m_iId;
    }
    
    /**
     * Modifica la etiqueta del campo.
     * @param strLabel Nueva etiqueta del campo
     */
    public void setLabel(String strLabel)
    {
        m_strLabel = strLabel;
    }
    
    /**
     * Obtiene la etiqueta del campo.
     * @return String - Devuelve una cadena correspondiente a la etiqueta del campo.
     */
    public String getLabel()
    {
        return m_strLabel;
    }
    
    /**
     * Modifica el name del campo.
     * @param strName Nuevo name del campo
     */
    public void setName(String strName)
    {
    	m_strName = strName;
    }
    
    /**
     * Obtiene el name del campo.
     * @return String - Devuelve una cadena correspondiente al name del campo.
     */
    public String getName()
    {
        return m_strName;
    }
    
    /**
     * Modifica el orden del campo
     * @param order Nuevo orden que se quiere que tenga el campo.
     */
    public void setOrder(int order)
    {
        m_iOrder = order;
    }
    
    /**
     * Obtiene el orden que tiene establecido el campo.
     * @return int - Devuelve un entero referente al orden del campo.
     */
    public int getOrder()
    {
        return m_iOrder;
    }
    
    /**
     * Modifica la prioridad que tiene el campo
     * @param priority La nueva prioridad que se quiere que tenga el campo.
     */
    public void setPriority(int priority)
    {
        m_iPriority = priority;
    }
    /**
     * Obtiene la prioridad asociada al campo
     * @return int - Devuelve un entero que es la prioridad que tiene el campo.
     */
    public int getPriority()
    {
        return m_iPriority;
    }
    
    /**
     * Modifica la longitud (en caracteres) del componente.
     * @param len Nueva longitud (en caracteres) que se quiere que tenga el campo.
     */
    public void setLength(int len)
    {
        m_iLength = len;
    }
    
    /**
     * Obtiene la longitud actual (en caracteres) del componente
     * @return int - Devuelve la longitud actual del componente
     */
    public int getLength()
    {
        return m_iLength;
    }
    
    /**
     * Obtiene el tamaño estimado que va a tener campo.
     * @return Dimension - La dimensión (área) del tamaño estimado.
     */
    public Dimension getPreferredSize()
    {
        return m_dimPreferredSize;
    }
    
    /**
     * Devuelve el mayor tamaño de los campos de un grupo ordenado
     * @return Dimension - El mayor tamaño de los campos de un grupo
     */
    public Dimension getTamGrupo(){
    	return tamGrupo;
    }
    
    /**
     * Obtiene el ancho de la etiqueta del campo.
     * @return int - Ancho de la etiqueta
     */
    public int getLabelWidth()
    {
        return (int)(m_strLabel.length()*m_objViewBalancer.getAveCharWidth(isHighlighted()));
    }
    
    /**
     * Obtiene el ancho del componente del campo.
     * @return int - Ancho del componente
     */
    public int getComponentWidth()
    {
        return getMinimumComponentDimension().width;
    }
    
    /**
     * Modifica el objeto viewbalancer al que referencia el atributo {@link #m_objViewBalancer}.
     * @param objViewBalancer Referencia al nuevo viewbalancer que queremos en el atributo {@link #m_objViewBalancer}.
     */
    public void setViewBalancer(IViewBalancer objViewBalancer)
    {
        m_objViewBalancer = objViewBalancer;
    }
    
    public IViewBalancer getViewBalancer()
    {
        return m_objViewBalancer;
    }
    
    /**
     * Este método tiene que ser llamado cada vez que se asigna un valor.
     * Calcula la dimensión correspondiente al campo, llamando al método {@link #calculateDimension()}.
     */
    public void postInitialize()
    {
        /*if(m_iLength == -1)

        	if(getType()==GConst.TM_BOOLEAN_COMMENTED)
        		m_iLength=GConfigView.minimumLengthCheckExtension;
        	else m_iLength = GConfigView.minimumLengthText;*/

        calculateDimension();
    }
    
    /**
     * Obtiene si el campo está comentado o no (tiene un componente secundario para introducir un comentario).
     * @return boolean - Devuelve el atributo {@link #comentado}
     */
    public boolean isCommented()
    {
        return comentado;
    }
    /**
     * Modifica el atributo {@link #comentado} para definir si un campo está comentado o no.
     * @param com Cierto o falso si el campo tiene o no comentario.
     */
    public void setCommented(boolean com){
    	comentado=com;
    }
    
    /**
     * Devuelve 'true' si la etiqueta está colocada encima del componente. Se suele utilizar en las
     * tablas y en los campos de texto con más de una fila. En caso contrario será 'false'.
     * @return boolean - 'true' si la etiqueta está colocada encima del componente.
     */
    public boolean isTopLabel()
    {
        return m_bTopLabel;
    }
    
    /**
     * Este método obtiene el mínimo área que ocupa el componente del campo, 
     * según los atributos y el tipo de éste.
     * Es un método abstracto ya que se implementará en la clase correspondiente al tipo de campo.
     * @return Dimension - Área mínima del campo.
     */
    abstract public Dimension getMinimumComponentDimension();

    /**
     * Este método calcula la dimensión final del campo, sumando etiqueta, 
     * componente y componente secundario (si tiene).
     */
    protected void calculateDimension()
    {
        Dimension dimComponent = getMinimumComponentDimension();
        Dimension dimLabel = m_objViewBalancer.getDimString(getLabel(), true, highlighted);
        Dimension dimComponentSecundario = getDimComponenteSecundario();
        
        if(!m_bTopLabel)
        {
            /*double widthTotal = dimComponent.getWidth() + dimLabel.getWidth();*/
            double heightTotal=Math.max(Math.max(dimComponentSecundario.getHeight(),dimLabel.getHeight()),dimComponent.getHeight());
            double widthTotal = dimComponentSecundario.getWidth() + dimComponent.getWidth() + dimLabel.getWidth();
            /*if(highlighted){
            	heightTotal*=2;
            	widthTotal*=2;
            }*/
            m_dimPreferredSize = new Dimension((int)widthTotal,(int)/*dimComponent.getHeight()*/heightTotal);
            tamGrupo = new Dimension((int)widthTotal,(int)/*dimComponent.getHeight()*/heightTotal);
        }
        else
        {
        	double heightTotal=dimComponent.getHeight();
            if(dimLabel.getWidth()!=0)
            	heightTotal += Math.max(dimComponentSecundario.getHeight(),dimLabel.getHeight());
            int anchoLabelYSecundario=(int)dimLabel.getWidth()+(int)dimComponentSecundario.getWidth();
            
            int anchoTotal=dimComponent.getWidth()>=anchoLabelYSecundario?(int)dimComponent.getWidth():anchoLabelYSecundario;
            /*if(highlighted){
            	heightTotal*=2;
            	anchoTotal*=2;
            }*/
            m_dimPreferredSize = new Dimension(anchoTotal ,(int) heightTotal);
            /*m_dimPreferredSize = new Dimension((int) dimComponent.getWidth() ,(int) heightTotal);*/
            /*tamGrupo = new Dimension((int) dimComponent.getWidth(),(int) heightTotal);*/
            tamGrupo = new Dimension(anchoTotal,(int) heightTotal);
        }
    }
    
    /**
     * Este método devuelve el ancho de un campo de texto, para un número particular de caracteres.
     * @param widthChar Ancho medio de un carácter.
     * @param len Número de caracteres del campo de texto.
     * @return int - Ancho total del componente (para el campo de texto).
     */
    int widthEdit( double widthChar, int len )
    {
    	int width=(int)(widthChar*len + 2*GConfigView.H_InternalEditPadd);//el 3 es el width de los bordes
        return width;
    }
    
    /**
     * Este método devuelve 'true' si el alto del campo es el predefinido para una fila.
     * Será cierto para los TEXT, CHECKBOX, COMBOBOX...
     * Falso para los TABLE, LISTBOX, IMAGE...
     * @return boolean - En esta versión se devuelve siempre cierto.
     */
    public boolean isUsualHeight()
    {
        return true;
    }
    
    /**
     * Comparador de dos campos, para ordenar el vector de campos de un grupo del formulario.
     * Por defecto ordena por ancho de mayor a menor. 
     * <br>Si algunos campos tienen un orden preestablecido, al comparar tendrá en cuenta 
     * el ancho del mayor campo del grupo, en vez del ancho individual de cada campo. 
     * <br>A la hora de comparar, lo primero que se tiene en cuenta es la prioridad del campo,
     * luego el orden que tiene preestablecido y en último lugar el ancho del campo.
     * <br>Las tablas y los campos cuya etiqueta esté colocada encima del componente se desplazan
     * al final del grupo del formulario, ya que son más difíciles de alinear, y por tanto 
     * a la hora de comparar siempre saldrán "perdiendo" las tablas y dichos campos.
     * <br>Mirar el método {@link GFormGroup#orderByWidth()} para comprender mejor 
     * como se ordena un grupo concreto del formulario.
     * @param o Campo con el que quiero comparar, que será también del tipo {@link GFormField}.
     * @return int - Devuelve -1 si el objeto por defecto 'this' es "mejor" que el objeto que se pasa por parámtro 'o'. Devuelve 1 en caso contrario. Si son "iguales" devuelve 0. 
     * @throws ClassCastException
     */
    public int compareTo(Object o) throws ClassCastException
    {
    	
    	if(!(o instanceof GFormField))
    		throw new ClassCastException();
    	GFormField objectToComp=(GFormField) o;
    	//System.out.println("OB1="+this.getLabel()+" PRIORITY="+this.m_iPriority+" OB2="+objectToComp.getLabel()+" PRIORITY="+objectToComp.m_iPriority);
    	if( m_bCheckPriority)
    	{
    		
    		/*if (this.m_iOrder < objectToComp.m_iOrder) 
    			return -1;
    		else if (this.m_iOrder > objectToComp.m_iOrder)
    			return 1;*/

    		if (this.m_iOrder == objectToComp.m_iOrder || this.m_iOrder==0 || objectToComp.m_iOrder==0){
    			if (this.m_iPriority != objectToComp.m_iPriority)
        		{
        			return (this.m_iPriority > objectToComp.m_iPriority ? -1 : 1);
        		}else
        			return 0;
    		}else
    			return (this.m_iOrder < objectToComp.m_iOrder ? -1 : 1);
    	}
//    	if(this.getType()==GConst.TM_TABLE || this.getType()==GConst.TM_MEMO)
//    		return 1;
//    	if(objectToComp.getType()==GConst.TM_TABLE || objectToComp.getType()==GConst.TM_MEMO)
//    		return -1;
//    	if(this.m_bTopLabel==true)
//    		return 1;
//    	if(objectToComp.m_bTopLabel==true)
//    		return -1;

    	if(m_bWidthOrdering)//"true" mean widht ordering. "False" mean height ordering
    	{
    		int thisWidth = getTamGrupo().width;
    		int objRecvWidth = objectToComp.getTamGrupo().width;
    		if (thisWidth > objRecvWidth)
    			return -1;
    		if (thisWidth == objRecvWidth)
    		{
    			// getWidht doesn't sum label width. Current algorithm order field just by width, so we
    			// compare just by width attribute. Only when both field's width are equals we compare by
    			// label width. (see document "API NOTES" for itemPointer)
    			int thisLabelWidth = getLabelWidth();
    			int thatLabelWidth = objectToComp.getLabelWidth();
    			if (thisLabelWidth > thatLabelWidth)
    				return -1;
    			if (thisLabelWidth == thatLabelWidth)
    				return 0;
    		}
    	}
    	/*        else
        {
                if(height < b.height) return -1;
                if(height == b.height) return 0;
        }*/
    	return 1;
    }

    /**
     * Obtiene la dimsensión del componente secundario, que será 0 por defecto,
     * ya que los campos no suelen tener componente secundario.
     * El tipo de campo que tiene componente secundario implementa este método en su clase.
     * @return Dimension - Área del componente secundario.
     */
    public Dimension getDimComponenteSecundario(){
    	return new Dimension(0,0);
    }
    
    /**
     * Modifica el atributo {@link #m_enabled}.
     * @param enabled Nuevo valor para el atributo {@link #m_enabled}
     */
    public void setEnabled(boolean enabled){
    	m_enabled=enabled;
    }
    
    /**
     * Modifica el atributo {@link #m_nullable}.
     * @param nullable Nuevo valor para el atributo {@link #m_nullable}
     */
    public void setNullable(boolean nullable){
    	m_nullable=nullable;
    }
    
//    /**
//     * Modifica el atributo {@link #m_idProp}.
//     * @param idProp Nuevo valor para el atributo {@link #m_idProp}
//     */
//    public void setIdProp(int idProp){
//    	m_idProp=idProp;
//    }
    
    /**
     * Modifica el atributo {@link #m_mask}.
     * @param mask Nuevo valor para el atributo {@link #m_mask}
     */
    public void setMask(String mask){
    	m_mask=mask;
    }
    
    /**
     * Modifica el atributo {@link #m_idRoot}.
     * @param id2 Nuevo valor para el atributo {@link #m_idRoot}
     */
    public void setId2(String id2){
    	m_idRoot=id2;
    }
    
    /**
     * Modifica el atributo {@link #m_multivalued}.
     * @param multivalued Nuevo valor para el atributo {@link #m_multivalued}
     */
    public void setMultivalued(boolean multivalued){
    	m_multivalued=multivalued;
    }
    
    /**
     * Modifica el atributo {@link #m_defaultVal}.
     * @param defaultVal Nuevo valor para el atributo {@link #m_defaultVal}
     */
    public void setDefaultVal(Object defaultVal){
    	m_defaultVal=defaultVal;
    }
    
    /**
     * Modifica el atributo {@link #m_numBotones}.
     * @param numBotones Nuevo valor para el atributo {@link #m_numBotones}
     */
    public void setNumBotones(int numBotones){
    	m_numBotones=numBotones;
    }
    
    /**
     * Obtiene si un campo puede ser editado o no. Devuelve el valor del atributo {@link #m_enabled}.
     * @return boolean - Devuelve el atributo {@link #m_enabled}
     */
    public boolean isEnabled(){
    	return m_enabled;
    }
    
    /**
     * Obtiene si el valor de un campo puede ser nulo o no. 
     * Devuelve el valor del atributo {@link #m_nullable}.
     * @return boolean - Devuelve el atributo {@link #m_nullable}.
     */
    public boolean isNullable(){
    	return m_nullable;
    }
    
//    /**
//     * Obtiene el atributo {@link #m_idProp}.
//     * @return int - Devuelve el atributo {@link #m_idProp}
//     */
//    public int getIdProp(){
//    	return m_idProp;
//    }
    
    /**
     * Obtiene el atributo {@link #m_mask}.
     * @return String - Devuelve el atributo {@link #m_mask}
     */
    public String getMask(){
    	return m_mask;
    }
    
    /**
     * Obtiene el atributo {@link #m_idRoot}.
     * @return String - Devuelve el atributo {@link #m_idRoot}
     */
    public String getId2(){
    	return m_idRoot;
    }
    
    /**
     * Obtiene si un campo puede tener varios valores. 
     * Devuelve el valor del atributo {@link #m_multivalued}.
     * @return boolean - Devuelve el atributo {@link #m_multivalued}.
     */
    public boolean isMultivalued(){
    	return m_multivalued;
    }
    
    /**
     * Obtiene el valor por defecto, es decir, el atributo {@link #m_defaultVal}.
     * @return String - Devuelve el atributo {@link #m_defaultVal}
     */
    public Object getDefaultVal(){
    	return m_defaultVal;
    }
    
    /**
     * Obtiene el alto predefinido para una fila, a partir del objeto ViewBalancer, es decir, 
     * el atributo {@link #m_objViewBalancer}.
     * @return double - Devuelve el valor de la llamada al método {@link IViewBalancer#getRowHeight()}.
     */
    public double getRowHeight(){
    	return m_objViewBalancer.getRowHeight(isHighlighted());
    }
    
    /**
     * Obtiene las constantes definidas para la separación interna de los caracteres con los campos de texto.
     * Se obtienen en la clase GConfigView, {@link GConfigView#V_InternalEditPadd} y {@link GConfigView#H_InternalEditPadd}.
     * @return Insets - Insets({@link GConfigView#V_InternalEditPadd}/2, {@link GConfigView#H_InternalEditPadd}/2, {@link GConfigView#V_InternalEditPadd}/2, {@link GConfigView#H_InternalEditPadd}/2)
     */
    public Insets getInternalPaddingEdit(){
    	return new Insets(GConfigView.V_InternalEditPadd/2,GConfigView.H_InternalEditPadd/2,GConfigView.V_InternalEditPadd/2,GConfigView.H_InternalEditPadd/2);
    }
    
    /**
     * Obtiene el número de filas que ocupa el campo. Será 1 por defecto, excecpto en los campos
     * tipo MEMO (que no estén en modo búsqueda), para los que el valor será 3.
     * @return int - El número de filas del componente del campo.
     */
    public int getRows(){
    	return rows;
    }
    
    public void setRows(int rows){
    	this.rows=rows;
    }
    
    /**
     * Número de columnas del campo. Por defecto 1.
     * @return int - Número de columnas del campo. Será por defecto 1.
     */ 
    public int getCols(){
    	return 1;
    }
    
    /**
     * Devuelve el número de botones que tiene el campo.
     * @return int - Número de botones.
     */
    public int getNumBotones(){
    	return m_numBotones;
    }

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public Integer getRedondeo() {
		return redondeo;
	}
	
	public void setRedondeo(Integer redondeo){
		this.redondeo=redondeo;
	}
    
}
