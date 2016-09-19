package gdev.gbalancer;

import gdev.gawt.GEdit;
import gdev.gen.IViewBalancer;
import gdev.gen.GConst;
import gdev.gen.GConfigView;

import gdev.gfld.*;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.Font;
import java.awt.font.FontRenderContext;

import gdev.gen.tree.GGenericBinaryTree;
import gdev.gen.tree.GTwoChildNode;
import gdev.gen.tree.GNode;
import java.util.Arrays;

import javax.swing.UIManager;

import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Utils;

/**
 * Esta es la clase principal donde vamos a obtener las diferentes combinaciones 
 * del formulario leido del XML y devolver la mejor combinación. 
 * En esta clase está el algoritmo principal que se sigue para realizar esto, comenzando 
 * en el método {@link #process(boolean, Insets, Insets)} y a partir de ahí llamando a los 
 * diferentes métodos de cada clase para obtenere los distintos formularios, compararlos y 
 * obtener el mejor, que será el que mostraremos en la aplicación final.
 * <p><h3>Algoritmo Principal de Ordenación</h3>
 * El algoritmo principal de ordenación está prácticamente implementado en los métodos {@link #process(boolean, Insets, Insets)}, {@link #processGroup(GFormGroup)} y {@link #getProcessedGroup(GFormGroup, int)}. Estos métodos llamarán a otros métodos de otras clases para completar el algoritmo de ordenación.
 * El algoritmo (muy a groso modo) que se sigue para conseguir el mejor formulario es:
 * <ol>
 * <li>Lee la información del XML del formulario, con los grupos que contiene y los campos que estos grupos contienen a su vez, y los almacena.</li>
 * <li>Ordeno los grupos que tiene el formulario según el orden establecido en el XML, y para cada grupo generamos todas las posibles combinaciones de colocación de los campos dentro del grupo. 
 * 		<br>Para obtener cada combinación de un grupo hacemos lo siguiente:
 * 		<ol>
 * 		<li>Ordeno sus campos de mayor a menor ancho. En esta ordenación tendremos en cuenta prioridades y ordenes de grupo también.</li>
 * 		<li>Calculo una altura posible para ese grupo e introduzco todos los campos del grupo, que se irán colocando en su posición correspondiente según esta altura establecida.</li>
 * 		</ol>
 * </li>
 * <li>Calculo todas las posibles combinaciones de los grupos anteriormente hallados para formar un formulario, por lo que obtengo un gran número de formularios posibles.</li>
 * <li>Según la lista de formularios obtenida anteriormente, descarta los formularios que tienen una ordenación muy mala La ordenación es mala si se sale de los márgenes máximos que nos han dado previamente, para la creación del formulario (los márgenes se nos pasan desde el núcleo de la aplicación, no los genera este proyecto).</li>
 * <li>Tras descartar estas malas combinaciones y quedarnos con las combinaciones válidas de los formularios, los vamos a ordenar por puntuación, para ver cuáles tienen mejor disposición.
 *		<br>Para puntuar los formularios se siguen los siguiente pasos:
 * 		<ol>
 * 		<li>Calculamos la proporción de los tamaños de los grupos con respecto al tamaño del panel hallado. Después le hacemos la inversa (1-p) y lo multiplicamos por 1300.</li>
 * 		<li>Calculamos la proporción del tamaño del panel hallado con respecto al tamaño máximo permitido (que nos ha pasado la aplicación). Después lo multiplicamos por 300.</li>
 * 		<li>Multiplicamos los dos parámetros mencionados anteriormente, y el que en total de menor resultado ese será el mejor formulario, es decir, el que tiene mejor aprovechado el espacio posible y más se ajusta al tamaño que nos proporciona la aplicación.</li>
 * 		</ol>
 * </li>
 * <li>Una vez que tengo todos los formularios ordenados, me quedo sólo con el mejor resultado, y los demás los descarto.</li>
 * <li>Optimizo el formulario obtenido para que se alinee lo mejor posible. Los pasos para la alineación se explican a continuación.</li>
 * </ol>
 * </p>
 * 
 * <p><h3>Algoritmo de Alineación</h3>
 * Los pasos básicos de la alineación se empiezan a invocar desde el método {@link GGrpColumn#fineTune(IViewBalancer)} de la clase {@link GGrpColumn}, por lo que se puede seguir una traza a partir de este método para comprender mejor el proceso de alineamiento que se sigue. Como se hace desde la clase {@link GGrpColumn}, sólo podemos obtener los campos por columnas, por lo que el alineamiento se va realizar sobre cada columna independientemente.
 * <br>Los pasos a seguir (muy a groso modo también) son los siguientes:
 * <ol>
 * <li>Calculamos el número máximo de subcolumnas que tiene la columna.</li>
 * <li>Por cada subcolumna (excepto la primera), obtenemos todas la filas que tienen al menos esa subcolumna. Por ejemplo, si el máximo de subcolumnas es 3, obtendremos todas las filas que contienen 2 o más subcolumnas y después todas las filas que contienen 3 subcolumnas. (Esto nos permite alinear la subcolumna 2 de una fila de 2 subcolumnas, con la subcolumna 2 de una fila de 3 subcolumnas).</li>
 * <li>Una vez que tenemos todas las filas que tienen más de una subcolumna, voy trazando los alineamientos independientes por subcolumnas. Para ello haremos lo siguiente (método {@link GGrpColumn#processRowsSymmetric(IViewBalancer, Vector, int)}):
 * 		<ol>
 * 		<li>Separaremos campos de la subcolumna con orden preestablecido de los que no tienen orden.</li>
 * 		<li>Normalizaremos primero las posiciones de los campo y después normalizaremos las etiquetas.
 * 			<ul>
 * 			<li>Para normalizar la posición (método {@link GGrpColumn#normalizeX(IViewBalancer, Vector, Vector, GProcessedField)}) calculamos el campo que está más a la derecha y alineamos todos los campos de la subcolumna a esa posición.</li>
 * 			<li>Para normalizar el ancho de la etiqueta (método {@link GGrpColumn#normalizeLabels(IViewBalancer, Vector, Vector, GProcessedField)}) calculamos la etiqueta mayor e incrementamos todas las etiquetas para que tengan ese tamaño.</li>
 * 			<li>Si ha habido incremento (sólo se llevará a cabo si los campos de la fila no se salen de la columna), desplazaremos con el mismo incremento todas las etiquetas de las siguientes subcolumnas.</li>
 * 			</ul>
 * 		</li>
 * 		</ol>
 * </li>
 * <li>Por último, procederemos a alinear los márgenes derechos de los componentes por subcolumnas. Para ello agrandaremos los componentes para que todos terminen en el mismo margen derecho. Como hemos hecho anteriormente, si el incremento no es posible porque algunos campos se salen de la columna, no se llevará a cabo y se dejará desalineado. Todo esto se hará llamando al método {@link GGrpColumn#alineaElementos()} por lo que se recomienda hacer una traza de éste método para entenderlo mejor. 
 * 		Los pasos que seguimos son los siguientes:
 * 		<ol>
 * 		<li>Separaremos la subcolumna de la derecha del todo, del resto de subcolumnas, y las trataremos de forma separada con los métodos {@link GGrpColumn#alineaSubColumna(Vector, int)} (donde Vector es la columna de la derecha) y {@link GGrpColumn#alineaElementosIntermedio(Vector, int)}</li>
 * 		<li>Para la subcolumna de la derecha, calculamos el máximo margen derecho, y para todos los campos (excepto los CheckBox) calculamos el incremento necesario para ajustarse a ese margen, vemos si no se nos sale de la columna y modificamos el borde si es posible.</li>
 * 		<li>Para las subcolumnas intermedias, primero separaremos los elementos de la subcolumna que tienen un orden preestablecido de los que no lo tienen. Alinearemos cada una de estas dos subcolumnas por separado. Para cada una de ellas haremos (método {@link GGrpColumn#alineaSubColumna(Vector, int)}):
 * 			<ul>
 * 			<li>Calculamos el máximo margen derecho de cada subcolumna.</li>
 * 			<li>Intentamos ajustar todos los campos de la subcolumna a ese margen derecho, comprobando si el incremento es posible y desplazando los elementos de las subcolumnas de la derecha si hemos llevado a cabo el desplazamiento.</li>
 * 			</ul>
 * 		</li>
 * 		</ol>
 * </li>
 * </ol>
 * </p>
 */
public class GViewBalancer implements IViewBalancer
{
	protected static final int INPUT_FORM=1;
	protected int politica=INPUT_FORM;
	
    protected final int DIR_VERTICAL = 1;
    protected final int DIR_HORIZONTAL = 2;

    /**
     * La información introducida para el formulario en el XML, con todos sus grupos y campos.
     */
    protected GFormFieldList m_objFormFieldList;
   
    /**
     * Vector con todas las combinaciones de los formularios ya procesados
     * Cada elemento del vector es del tipo {@link GProcessedForm}
     */
    protected Vector m_vProcessedFormList;
    /**
     * The font
     */
    protected Font m_fontRegular=null;
    /**
     * The FontRenderContext object.
     */
    protected FontRenderContext m_fontRender=null;

    /**
     * Alto de una línea de texto
     */
    protected double m_dTextHeight;
    /**
     * El ancho de un carácter de texto
     */
    protected double m_dAveCharWidth;
    /**
     * El alto predefinido de una fila
     */
    protected double m_dRowHeight;

    /**
     * Alto de una línea de texto de un campo resaltado
     */
    protected double m_dTextHeightHighlighted;
    /**
     * El ancho de un carácter de texto de un campo resaltado
     */
    protected double m_dAveCharWidthHighlighted;
    /**
     * El alto predefinido de una fila de un campo resaltado
     */
    protected double m_dRowHeightHighlighted;

    /**
     * Modo búsqueda. Si el formulario que se está creando 
     * es un formulario de busqueda algunos campos nos puede interesar mostrarlos de manera
     * distinta a la predefinida. Por ejemplo los {@link GEdit} solo tendrán una fila.
     */
    protected boolean m_bFilterMode;

    /**
     * El ancho optimizado del panel. Se calcula en el método {@link #calculateMinDimension(boolean)}.
     * El ancho final del panel puede variar, pero esta primera aproximación es la base para todos los cálculos.
     */
    protected int m_iBestPanelWidth;
    /**
     * El alto optimizado del panel. Se calcula en el método {@link #calculateMinDimension(boolean)}.
     * El alto final del panel puede variar, pero esta primera aproximación es la base para todos los demás cálculos.
     */
    protected int m_iBestPanelHeight;
    
    /**
     * Márgenes del panel (se pasan desde el núcleo en el método formFactory)
     */
    protected Insets m_margenesPanel;
    
    /**
     * Márgenes del grupo (se pasan desde el núcleo en el método formFactory)
     */
    protected Insets m_margenesGrupo;

    /**
     * El constructor. El resto de atributos que no se pasan por parámetros se calculan dentro del constructor. 
     * Por ejemplo {@link #m_dTextHeight}, {@link #m_dAveCharWidth}, {@link #m_dRowHeight}.
     * @param endDimension Área máxima que puede ocupar el panel (el formulario).
     * @param font Valor para el atributo {@link #m_fontRegular}.
     * @param render Valor para el atributo {@link #m_fontRender}.
     * @param bFilterMode Valor para el atributo {@link #m_bFilterMode}.
     */
    private Graphics m_graphics;
    
    public GViewBalancer(Dimension endDimension,Graphics graphics,boolean bFilterMode)
    {
    	m_graphics=graphics;
        Graphics2D gr2D = (Graphics2D) graphics;
		gr2D.setFont(UIManager.getFont("Label.font"));
		m_fontRender = gr2D.getFontRenderContext();
		m_fontRegular=gr2D.getFont();
        m_bFilterMode = bFilterMode;

        Dimension rect=getDimString("ABCDE",false,false);
        m_dTextHeight = rect.getHeight();
        m_dAveCharWidth = rect.getWidth()/5.0; // 5 is the number of chars of example string
        m_dRowHeight = m_dTextHeight+GConfigView.V_InternalEditPadd*2;
        
        rect=getDimString("ABCDE",false,true);
        m_dTextHeightHighlighted = rect.getHeight();
        m_dAveCharWidthHighlighted = rect.getWidth()/5.0; // 5 is the number of chars of example string
        m_dRowHeightHighlighted = m_dTextHeightHighlighted+GConfigView.V_InternalEditPadd*2;

        init(0);

        m_objFormFieldList.setPanelWidth(endDimension.width);
        m_objFormFieldList.setPanelHeight(endDimension.height);
    }
    
    public static double getRowHeightS(Graphics gf){
    	
    	//System.out.println("GRAPHICS = "+gf);
    	Graphics2D gr2D = (Graphics2D) gf;
		gr2D.setFont(UIManager.getFont("Label.font"));
		FontRenderContext fontRender = gr2D.getFontRenderContext();
		Font font=gr2D.getFont();
    	Dimension rect=getDimString("ABCDE",false, font, fontRender, false);
    	//System.out.println("HEIGHT="+(rect.getHeight()+GConfigView.V_InternalEditPadd*2));
    	return rect.getHeight()+GConfigView.V_InternalEditPadd*2;
    }

    /**
     * Inicialización del método. Creamos el atributo {@link #m_objFormFieldList}.
     * @param arg No se utiliza.
     * @return int - Devolvemos 0 si todo ha ido bien.
     */
    public int init(int arg)
    {
        m_objFormFieldList = new GFormFieldList(this);
        return 0;
    }
    /**
     * Usaremos este método para liberar memoria si es que lo necesitamos. 
     * @param arg No se utiliza.
     * @return int - Devolvemos 0.
     */
    public int exit(int arg)
    {
        return 0;
    }
    
    /**
     * Este método obtiene la mínima Dimension (ancho x alto, width x height) que necesitamos
     * para representar la cadena que se nos pasa en el parámetro 'value'.
     * @param value Cadena que queremos representar y calcular su mínima dimensión.
     * @param bold Si la cadena está en negrita o no (true o false).
     * @return Dimension - Devuelve la mínima dimensión para la cadena pasada en el parámetro 'value'.
     */
    public static Dimension getDimString(String value, boolean bold, Font font, FontRenderContext fontRender, boolean highlighted){
    	float multiplySize=1;
    	if(highlighted)
    		multiplySize=GConfigView.multiplySizeHighlightedFont;
    	return Utils.getDimString(value, bold, font, fontRender, multiplySize);
    }
    
    public Dimension getDimString( String value, boolean bold, boolean highlighted )
    {
        return getDimString(value, bold, m_fontRegular, m_fontRender, highlighted);
    }
    /**
	 * Obtiene el valor del atributo {@link #m_bFilterMode}.
	 * @return boolean - Devuelve el atributo {@link #m_bFilterMode}.
	 */
	public boolean getFilterMode()
	{
		return m_bFilterMode;
	}
	/**
	 * Obtiene el atributo {@link #m_dTextHeight}.
	 * @return double - Devuelve {@link #m_dTextHeight}.
	 */
	public double getTextHeight(boolean highlighted)
	{
		if(highlighted)
			return m_dTextHeightHighlighted;
		return m_dTextHeight;
	}
	/**
	 * Obtiene el atributo {@link #m_dAveCharWidth}.
	 * @return double - Devuelve {@link #m_dAveCharWidth}.
	 */
	public double getAveCharWidth(boolean highlighted)
	{
		if(highlighted)
			return m_dAveCharWidthHighlighted;
		return m_dAveCharWidth;
	}
	/**
	 * Obtiene el atributo {@link #m_dRowHeight}.
	 * @return double - Devuelve {@link #m_dRowHeight}.
	 */
	public double getRowHeight(boolean highlighted)
	{
		if(highlighted)
			return m_dRowHeightHighlighted;
		return m_dRowHeight;
	}

	/**
	 * Obtiene la separación horizontal predefinida entre dos grupos del mismo panel
	 * @return int - Devuelve la separación horizontal entre dos grupos.
	 */
	public int getPanelHGap()
	{
		return GConfigView.PanelHGap;
	}
	/**
	 * Obtiene la separación vertical predefinida entre dos grupos del mismo panel
	 * @return int - Devuelve la separación vertical entre dos grupos.
	 */
	public int getPanelVGap()
	{
		return GConfigView.PanelVGap;
	}
	/**
	 * Obtiene el margen izquierdo predefinido entre el grupo más a la izquierda 
	 * y el borde del panel.
	 * @return int - Devuelve el margen izquierdo entre el grupo más a la izquierda y el panel.
	 */
	public int getPanelLeftMargin()
	{
		int margen=GConfigView.PanelLeftMargin;
		if(m_margenesPanel!=null)
			margen=m_margenesPanel.left;
		return margen;
	}
	/**
	 * Obtiene el margen superior predefinido entre el grupo superior y el borde del panel.
	 * @return int - Devuelve el margen superior del grupo superior con respecto al panel.
	 */
	public int getPanelTopMargin()
	{
		int margen=GConfigView.PanelTopMargin;
		if(m_margenesPanel!=null)
			margen=m_margenesPanel.top;
		return margen;
	}
	/**
	 * Obtiene el margen derecho predefinido entre el grupo colocado más a la derecha 
	 * y el borde del panel.
	 * @return int - Devuelve el margen derecho entre el último grupo de la derecha y el panel.
	 */
	public int getPanelRightMargin()
	{
		int margen=GConfigView.PanelRightMargin;
		if(m_margenesPanel!=null)
			margen=m_margenesPanel.right;
		return margen;
	}
	/**
	 * Obtiene el margen inferior predefinido entre el grupo inferior y el borde del panel.
	 * @return int - Devuelve el margen inferior del grupo inferior con respecto al panel.
	 */
	public int getPanelBottomMargin()
	{
		int margen=GConfigView.PanelBottomMargin;
		if(m_margenesPanel!=null)
			margen=m_margenesPanel.bottom;
		return margen;
	}
	/**
	 * Este método obtiene la separación predefinida entre la primera fila de un grupo 
	 * y el borde superior del grupo.
	 * @return int - Devuelve la separación entre la primera fila y el margen superior de un grupo.
	 */
	public int getGroupTopMargin()
	{
		int margen=GConfigView.GroupTopMargin;
		if(m_margenesGrupo!=null)
			margen=m_margenesGrupo.top;
		return margen;
	}
	/**
	 * Este método obtiene la separación predefinida entre la última fila de un grupo 
	 * y el borde inferior del grupo.
	 * @return int - Devuelve la separación entre la última fila y el margen inferior de un grupo.
	 */
	public int getGroupBottomMargin()
	{
		int margen=GConfigView.GroupBottomMargin;
		if(m_margenesGrupo!=null)
			margen=m_margenesGrupo.bottom;
		return margen;
	}
	/**
	 * Este método obtiene la separación entre la primera columna de un grupo 
	 * y el borde superior del grupo.
	 * @return int - Devuelve la separación entre la primera fila y el margen superior de un grupo.
	 */
	public int getGroupLeftMargin()
	{
		int margen=GConfigView.GroupLeftMargin;
		if(m_margenesGrupo!=null)
			margen=m_margenesGrupo.left;
		return margen;
	}
	/**
	 * Este método obtiene la separación entre la última columna de un grupo 
	 * y el borde derecho del grupo.
	 * @return int - Devuelve la separación entre la primera fila y el margen superior de un grupo.
	 */
	public int getGroupRightMargin()
	{
		int margen=GConfigView.GroupRightMargin;
		if(m_margenesGrupo!=null)
			margen=m_margenesGrupo.right;
		return margen;
	}
	/**
	 * Obtiene la separación horizontal predefinida entre campos de un grupo.
	 * Esta separación puede aumentar en caso de alineamiento por columnas, pero nunca disminuir.
	 * @return int - Devuelve la separación horizontal entre campos de un grupo
	 */
	public int getGroupHGap()
	{
		return GConfigView.GroupHGap;
	}
	/**
	 * Obtiene la separación vertical predefinida entre campos de una misma columna.
	 * @return int - Devuelve la separación vertical entre campos de una columna.
	 */
	public int getGroupVGap()
	{
		return GConfigView.GroupVGap;
	}
	
	public int getHCellPad()
	{
		return GConfigView.HCellPad;
	}
	public int getVCellPad()
	{
		return GConfigView.VCellPad;
	}
	/**
	 * Obtiene el alineamiento del formulario (por norma justificado).
	 * @return int - Devuelve la constante (entera) asignada a la alineación correspondiente.
	 */
	public int getAlignment()
	{
		return GConst.ALIGN_JUSTIFY;
	}
    /**
     * Este método añade un grupo al formulario que vamos a mostrar. Lo hace con el atributo {@link #m_objFormFieldList} y el método de su clase {@link GFormFieldList#addGroup(int, String, int)}.
     * @param groupId El ID del grupo que vamos a añadir.
     * @param strLabel El nombre (etiqueta) del grupo que vamos a añadir.
     * @param order El orden del grupo que vamos a añadir.
     * @return int - Devuelve el código asignado según si el método a funcionado correctamente o ha tenido un error.
     */
    public int addGroup(int groupId,String strLabel,int order)
    {
        return m_objFormFieldList.addGroup(groupId,strLabel,order);
    }

    /**
     * Este método añade un campo a un grupo. Lo hace con el atributo {@link #m_objFormFieldList} y el método de su clase{@link GFormFieldList#addItem(int, int, String, String, int, int, String, boolean, boolean, boolean, String, String, int, boolean, int, boolean, int, Vector)}. 
     * @param groupId El ID del grupo en el que vamos a añadir el campo.
     * @param fieldType El tipo del campo.
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
     * @return int - Devuelve un código según si el método a funcionado correctamente o ha tenido algun error.
     */
   /* public int addItem(int groupId, int fieldType, String id, String id2, int tapos, int priority,
                            String mask, boolean enable, boolean nullable, boolean multivalued, String defaultVal, String label,int length,
                            boolean commented, int order, boolean visible ,int numBotones,Vector vValues)*/
    
    public int addItem(int groupId, int fieldType, String id, String idRoot, int priority,
            String mask, boolean enable, boolean nullable, boolean multivalued, Object defaultVal, String label, String name, int length,
            boolean commented, int order, boolean visible ,int numBotones,Vector vValues,boolean highlighted,boolean directoryType, int rows, Integer redondeo)
    
    {
        return m_objFormFieldList.addItem(groupId,fieldType,id,idRoot,priority,mask,enable,nullable,multivalued,defaultVal,label,name,length,commented,order,visible,numBotones,vValues,highlighted,directoryType,rows,redondeo);
    }

    /**
     * Este método añade una tabla a un grupo. Lo hace con el atributo {@link #m_objFormFieldList} y a través del método de su clase {@link GFormFieldList#addTable(int, String, int, boolean, String, int, boolean, int, Vector, Vector, int, int, boolean, int, int, int, boolean, int, int, int, int)}
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
    public int addTable(int groupId, String id, int priority, boolean enable, boolean nullable, String label, String name, int order,boolean visible,int rows, Vector vColumns,Vector vRows, boolean cuantitativo,int iniVirtualColumn, int atGroupColumn, int headerLine,boolean hideHeader,int numBotones,int widthMin,AccessAdapter accessAdapter, boolean creationRow, boolean finderRow)
    {
        int widthMax=m_objFormFieldList.getPanelWidth()-getPanelRightMargin()-getPanelLeftMargin()-getGroupRightMargin()-getGroupLeftMargin()-getGroupHGap();
    	return m_objFormFieldList.addTable(groupId,id,priority,enable,nullable,label,name,order,visible,rows,vColumns,vRows,cuantitativo,iniVirtualColumn,atGroupColumn,headerLine,hideHeader,numBotones,widthMin,widthMax,accessAdapter,creationRow,finderRow);
    }
    /**
     * Este método devuelve la mejor combinación del formulario, es decir, la que está mejor puntuada.
     * @return GProcessedForm - Devuelve el formulario final obtenido.
     */
    public GProcessedForm getBestResult()
    {
        //return m_objBestProcessedForm;
        if(m_vProcessedFormList!=null){
        	processPositionTablesMemos();
            return (GProcessedForm)m_vProcessedFormList.elementAt(0);
        }
        return null;
    }
    private void processPositionTablesMemos() {
    	GProcessedForm gp=(GProcessedForm)m_vProcessedFormList.elementAt(0);
    	Enumeration en = gp.getProcessedGroupList().elements();
        while(en.hasMoreElements())
        {
        	GProcessedGroup grp = (GProcessedGroup)en.nextElement();
        	Enumeration en2 = grp.getProcessedFieldList().elements();
        	GProcessedFieldsSorts gfs= new GProcessedFieldsSorts();
        	while(en2.hasMoreElements()){
        		GProcessedField gfield=(GProcessedField) en2.nextElement();
        		if (gfield.getFormField().isTopLabel())
        			gfs.addField(gfield);
        	}
        	//gfs.toStingListFields();
        	//gfs.toStingListFieldsWidth();
        	gfs.process();
        }
        
	}

	/**
     * Este método calcula una primera aproximación de la 
     * mínima dimensión del panel (ancho y alto). Se basa en los tamaños del panel
     * definidos por la aplicación y las estimaciones de los tamaños de cada grupo.
     * @param bMinimizeHeight No se usa en esta versión.
     */
    private void calculateMinDimension(boolean bMinimizeHeight)
    {
        //In this version bMinimizeHeight is ignored and its value is taken as false
        Vector groupList = m_objFormFieldList.getGroupList();
        Enumeration enumGrp = groupList.elements();
        double dTotalArea = 0.0;
        int maxWidth = 0;
        while(enumGrp.hasMoreElements())
        {
            GFormGroup oneFrmGrp = (GFormGroup)enumGrp.nextElement();
            dTotalArea += oneFrmGrp.getMinArea();
            int grpWid = oneFrmGrp.getMaxComponentWidth()+getGroupHGap()*2+getPanelHGap()*2;
            if(grpWid>maxWidth)
                maxWidth = grpWid;
        }
        //Assuming that 33% of the area will be used by the components we can calculate the total area needed
        dTotalArea = dTotalArea*3;
        int iPanelWidthSpecified = m_objFormFieldList.getPanelWidth();
        int iPanelHeightSpecified = m_objFormFieldList.getPanelHeight();
        double dAspectRatio = (double)iPanelWidthSpecified/(double)iPanelHeightSpecified;
        m_iBestPanelWidth = (int)(Math.sqrt(dAspectRatio*dTotalArea));
        m_iBestPanelHeight = (int)(dTotalArea/m_iBestPanelWidth);
        //now it may happen that the calculated width becomes greater than maximum width of group
        //in that case it should be equal to that number

        /*if(m_iBestPanelWidth>=iPanelWidthSpecified)
        	m_iBestPanelWidth = iPanelWidthSpecified;
        else*/ if(m_iBestPanelWidth<maxWidth)

            m_iBestPanelWidth = maxWidth;
    }
    /**
     * Este método comienza el procesado del formulario leído del XML
     * para obtener la mejor combinación posible de los grupos y los campos.
     * <p>
     * Para ello lo que hace el método es:
     * <ol>
     * <li>Calcula la mínima dimensión que estima que va a ocupar el formulario, llamando al método {@link #calculateMinDimension(boolean)}.</li>
     * <li>Ordena los grupos del formulario (según el orden obtenido del XML)</li>
     * <li>Itera sobre todos los grupos del formulario, y para cada uno de ellos calcula todas las posibles combinaciones del grupo, llamando al método {@link #processGroup(GFormGroup)}.Todas las distintas combinaciones de los grupos se almacenarán en un vector, dónde cada elemento es del tipo {@link GGrpCombination}</li>
     * <li>Una vez que tengo todas las combinaciones de cada grupo, descartaré las malas combinaciones, llamando al método {@link GGrpCombination#discardPoorCombinations(int, int)} con el tamaño del panel calculado anteriormente.</li>
     * <li>Con el vector que contiene las combinaciones de grupos que me han quedado, genero todos los formularios posibles, combinando los grupos del vector (llamando al método {@link #generateAllFormCombinations(Vector)}</li>
     * <li>Tras conseguir todas las combinaciones de los formularios, me voy a quedar con el mejor, llamando al método {@link #findBestFormCombination()}.</li>
     * <li>El formulario que obtengo, lo optimizo para visualizarlo lo mejor posible en la interfaz, llamando al método {@link #fineTuneBestCombination()}.</li>
     * </ol>
     * Una vez que hemos terminado este procedimiento, en el atributo {@link #m_vProcessedFormList} tengo el formulario final alineado y optimizado.
     * </p>
     * @return int - Devuelve 0 si todo ha ido bien.
     */
    public int process(boolean bMinimizeHeight,Insets margenesPanel,Insets margenesGrupo)
    {
		//System.err.println("**************** Comienzo de todo**********"+System.currentTimeMillis());
    	m_margenesPanel=margenesPanel;
    	m_margenesGrupo=margenesGrupo;
        //Based on given input calculate minimum dimension of the end panel
        calculateMinDimension(bMinimizeHeight);
        //System.err.println("**************** Calcular min dimension**********"+System.currentTimeMillis());
        //Enumerate through all groups and calculate their different combination of dimensions and positions
        m_objFormFieldList.sortByOrder();
        //System.err.println("**************** ordenar por order**********"+System.currentTimeMillis());
        Vector groupList = m_objFormFieldList.getGroupList();
        Enumeration enumGrp = groupList.elements();
        Vector vGrpCombinationList = new Vector();
        //System.err.println("**************** antes while**********"+System.currentTimeMillis());
        while(enumGrp.hasMoreElements())
        {
            GFormGroup oneFrmGrp = (GFormGroup)enumGrp.nextElement();
            GGrpCombination objGroupCombination = processGroup(oneFrmGrp);
            vGrpCombinationList.addElement(objGroupCombination);
        }
        //System.err.println("**************** Despues while**********"+System.currentTimeMillis());
        //you can discard combinations which are very poor
        Enumeration enumComb = vGrpCombinationList.elements();
        //System.err.println("**************** antes 2 while**********"+System.currentTimeMillis());
        while(enumComb.hasMoreElements())
        {
            GGrpCombination objGroupCombination = (GGrpCombination)enumComb.nextElement();
            objGroupCombination.discardPoorCombinations(m_iBestPanelWidth,m_iBestPanelHeight);
        }
        //System.err.println("**************** despues 2 while**********"+System.currentTimeMillis());
        //now get all combinations of groups based on their sizes
        m_vProcessedFormList = generateAllFormCombinations(vGrpCombinationList);
        //System.err.println("**************** generando todas las combinaciones "+m_vProcessedFormList.size()+" **********"+System.currentTimeMillis());
        findBestFormCombination();
        //System.err.println("**************** encontrando mejor **********"+System.currentTimeMillis());
        fineTuneBestCombination();
        //System.err.println("**************** Tuneando **********"+System.currentTimeMillis());
        return 0;
    }
    /**
     * Este método analiza un grupo y crea las distintas posibilidades para cololar los campos.
     * La lista de campos se ordena por prioridades, órdenes y anchos de mayor a menor.
     * <br>Calcula los distintos altos que puede tener el formulario, 
     * calculando siempre que no sobrepase el ancho máximo, y para las distintas alturas
     * del formulario y el grupo concreto sobre el que estamos iterando (se nos pasa por parámetro), 
     * llamamos al método {@link #getProcessedGroup(GFormGroup, int)}.
     * @param frmGrp El grupo que vamos a procesar.
     * @return GGrpCombination - Devuelve la combinación del grupo obtenida.
     */
    protected GGrpCombination processGroup(GFormGroup frmGrp)
    {
        frmGrp.orderByWidth();
        GGrpCombination objProcessedGroupList = new GGrpCombination(frmGrp);

        boolean bMoreColumnPossible = true;
        int newHeight = m_iBestPanelHeight;
        int maxWidthAllowed = (int)(m_iBestPanelWidth);
        int rowHeight = (int)getRowHeight(false);
        while(bMoreColumnPossible)
        {
            GProcessedGroup prGroup = getProcessedGroup(frmGrp,newHeight);
            if (prGroup != null )
            {
                objProcessedGroupList.addCombination(prGroup);
            }
            newHeight -= rowHeight;
        	if(newHeight<rowHeight||prGroup.getBounds().width>maxWidthAllowed)
                bMoreColumnPossible = false;
        }
        return objProcessedGroupList;
    }
    /**
     * Este método devuelve un grupo ya procesado ({@link GProcessedGroup}) basándose en un grupo 
     * leído del XML ({@link GFormGroup}). Los campos se van colocando por columnas, a partir 
     * de un vector que contiene los campos del grupo en el orden correcto. Las columnas pueden tener más 
     * de un campo en la misma fila, para optimizar el espacio lo máximo posible.
     * <p>El método básicamente lo que va haciendo es:
     * <ol>
     * <li>Itera sobre todos los campos del grupo y va colocándolos por columnas. Cuando el campo nuevo sobre el que itera sobrepasa el alto máximo, crea una nueva columna e introduce el campo en ella.</li>
     * <li>Para cada campo, si puede colocarlos en la misma fila (método {@link #repositionField(GProcessedField, GProcessedField, int, int, int)}) lo coloca y así ahorra espacio.
     * <li>Antes de crear una nueva columna, si ha sobrepasado el ancho máximo, con el método {@link #reorganizeComponents(GFormGroup, GGrpColumn, int, int, int)} intenta recolocar todos los campos para optimizar la altura lo máximo posible. Si es necesario (no caben todos los campos) se incrementará la altura.</li>
     * <li>Cuando tenga la columna ya procesada para la altura correspondiente, con el método {@link #normalizeLabels(GGrpColumn)} normalizará las etiquetas de la columna que ha procesado}</li>
     * </ol>
     * Para una vista más detallada del método mirar el código o hacer una traza.
     * </p>
     * @param frmGrp El grupo que vamos a procesar, donde está toda la información leída del XML. 
     * @param aveColHeight La altura máxima que debe ocupar el grupo. No es una restricción, 
     * 					sino que se ajusta a esa altura lo máximo posible, pero si no cabe en 
     * 					el ancho establecido, esta altura será sobrepasada.
     * @return GProcessedGroup - Devuelve el grupo ya procesado.
     */
    private GProcessedGroup getProcessedGroup(GFormGroup frmGrp, int aveColHeight)
    {

    	Vector vFieldList = frmGrp.getFieldList();
    	int elCount = vFieldList.size();

    	GProcessedGroup processedGroup = new GProcessedGroup(frmGrp);
    	Vector vProcessedFieldList = new Vector();
    	int top = getGroupTopMargin();
    	if(frmGrp.getId()==0 && getGroupTopMargin()!=0)
    		top=getGroupVGap();
    	int left = getGroupLeftMargin();
    	int colIndex = 0;
    	GGrpColumn vOneColumn = new GGrpColumn();
    	int maxColWidth = 0;
    	boolean uniqueColumn=false;
//    	int widthMinAprox=(int)(frmGrp.getMinArea()/2);
//    	int maxComponentWidth=frmGrp.getMaxComponentWidth();
//    	if(maxComponentWidth*2>m_objFormFieldList.getPanelWidth()){
//    		//maxColWidth=maxComponentWidth+getGroupLeftMargin()+getGroupRightMargin();
//    		maxColWidth=widthMinAprox+getGroupLeftMargin()+getGroupRightMargin();
//    		uniqueColumn=true;
//    	}
    	int maxLabelWidth = 0;
    	GProcessedField pPreviousField = null;
    	int bottomMostPoint = 0;
    	int heightColumn = 0;
    	for(int elIndex = 0;elIndex<elCount;elIndex++)
    	{
    		GFormField ff = (GFormField)vFieldList.elementAt(elIndex);
    		//check if this field will go to next column or not
    		Dimension preferredSize = ff.getPreferredSize();	
    		
    		if(!uniqueColumn && top+preferredSize.height>aveColHeight /*|| (ff.isTopLabel() && pPreviousField!=null && !pPreviousField.getFormField().isTopLabel())*/)
    		{
    			int heightNextColumn=0;
    			for(int i = elIndex;i<elCount;i++){
    				GFormField f = (GFormField)vFieldList.elementAt(i);
    				heightNextColumn+=f.getPreferredSize().getHeight()+getGroupVGap();
    			}
    			if(heightNextColumn>heightColumn/2 && heightNextColumn<=heightColumn){
	    			//here you can go for next column
	    			//but you still may have some gap left on the right of the last component
	    			boolean bNextColumn = false;
	    			if(pPreviousField!=null /*&& !(ff.isTopLabel() && pPreviousField!=null && !pPreviousField.getFormField().isTopLabel())*/)
	    			{
	    				Rectangle rcPrev = pPreviousField.getBounds();
	    				//if(rcPrev.width*2<m_objFormFieldList.getPanelWidth() && preferredSize.width*2<m_objFormFieldList.getPanelWidth()){
	    					int right = rcPrev.x + rcPrev.width+getGroupHGap();
	        				if(right+preferredSize.width>left+maxColWidth /*&& right+preferredSize.width<m_objFormFieldList.getPanelWidth()*0.70*/)
	        					bNextColumn = true;
	    				//}
	    			}
	    			/*if(ff.isTopLabel() && pPreviousField!=null && !pPreviousField.getFormField().isTopLabel()){
	    				Rectangle rcPrev = pPreviousField.getBounds();
	    				int right = rcPrev.x + rcPrev.width+getGroupHGap();
	    				if(right+preferredSize.width>left+maxColWidth )
	    					bNextColumn=true;
	    			}*/
	    			if(bNextColumn)
	    			{
	    				vProcessedFieldList.addAll(vOneColumn.getFieldList());
	    				left += maxColWidth + getGroupHGap();
	    				top = getGroupTopMargin();
	    				if (frmGrp.getId()==0 && getGroupTopMargin()!=0)
	    					top = getGroupVGap();
	    				processedGroup.addColumn(vOneColumn);
	    				vOneColumn.setColumnWidth(maxColWidth);
	    				vOneColumn = new GGrpColumn();
	    				colIndex++;
	    				maxColWidth = 0;
	    				maxLabelWidth = 0;
	    				heightColumn = 0;
	    				pPreviousField = null;
	    				bottomMostPoint = 0;
	    			}
    			}
    		}
    		
    		heightColumn+=preferredSize.getHeight()+getGroupVGap();

    		//This is the probable bounding rectangle
    		Rectangle calcPos = new Rectangle(new Point(left,top),preferredSize);
    		GProcessedField pField = new GProcessedField(ff);
    		vOneColumn.addField(pField);
    		pField.setColumn(colIndex);
    		int labWid = getDimString(ff.getLabel(), true, ff.isHighlighted()).width;
    		//System.err.println(getDimString(ff.getLabel(), true));
    		if (!ff.isTopLabel())
    		{
    			//make label width equal to the last maximum label width of the same column
    			//please note that it will changed again if the subcolumn is more than 0
    			//please see repositionField()
    			
    			if(labWid<=maxLabelWidth)
    			{
    				calcPos.width += (maxLabelWidth-labWid);
    				labWid = maxLabelWidth;
    			}
    			
    			/*int anchoCompSec=0;
                if(ff instanceof GFormTable){
            		Dimension dimCompSec = ((GFormTable)ff).getDimComponenteSecundario();
            		anchoBotonera=dimCompSec.width;
                }*/
    			int altoComp=ff.getMinimumComponentDimension().height;
    			Dimension dimCompSec = ff.getDimComponenteSecundario();
    			int anchoCompSec=dimCompSec.width;
    			int altoCompSecundario=dimCompSec.height;
    			
    			int yComponent=0;
    			int yComponentSec=0;
    			int yLabel=0;
    			
    			//Si el alto del label es distinto que alguno de los componentes, o entre ellos, cambiamos su posicion y para que salgan en el centro
    			if(calcPos.height!=altoComp || (altoCompSecundario!=0 && altoComp!=altoCompSecundario)){
    				int maxHeight=Math.max(Math.max(calcPos.height,altoComp),altoCompSecundario);
    				yLabel=(maxHeight-calcPos.height)/2;
    				yComponent=(maxHeight-altoComp)/2;
    				yComponentSec=(maxHeight-altoCompSecundario)/2;
    			}
    			
    			pField.setLabelBounds(new Rectangle(0, yLabel, labWid,calcPos.height));
    			pField.setComponentSecundarioBounds(new Rectangle(calcPos.width -anchoCompSec,yComponentSec,anchoCompSec,/*calcPos.height*/altoCompSecundario));
    			pField.setComponentBounds(new Rectangle(labWid, yComponent,calcPos.width - labWid - anchoCompSec, /*calcPos.height*/altoComp));
    			
    		}
    		else
    		{
    			/*pField.setLabelBounds(new Rectangle(0, 0, labWid,(int) getRowHeight()));
                pField.setComponentBounds(new Rectangle(0, (int) getRowHeight(),calcPos.width, calcPos.height - (int) getRowHeight()));*/
    			/* 	int altoLabel=(int)getRowHeight();
            	int anchoCompSec=0;
            	if(ff instanceof GFormTable){
            		Dimension dimCompSec = ((GFormTable)ff).getDimComponenteSecundario();
            		anchoBotonera=dimCompSec.width;
            		/*labWid+=dimCompSec.width;*/
    			/*		altoLabel=Math.max(altoLabel,dimCompSec.height);
            	}*/
    			Dimension dimCompSec = ff.getDimComponenteSecundario();
    			int anchoCompSec=dimCompSec.width;
    			int altoLabel=Math.max((int)getRowHeight(ff.isHighlighted()),dimCompSec.height);

    			pField.setLabelBounds(new Rectangle(0, 0, labWid,labWid==0?0:altoLabel));
    			/*pField.setComponentSecundarioBounds(new Rectangle(labWid,0,anchoBotonera,altoLabel));*/
    			pField.setComponentSecundarioBounds(new Rectangle(calcPos.width-anchoCompSec,0,anchoCompSec,altoLabel));
    			pField.setComponentBounds(new Rectangle(0, labWid==0?0:altoLabel,calcPos.width, labWid==0?calcPos.height:calcPos.height - altoLabel));
    		}
    		pField.setBounds(calcPos);
    		//here you are able to place it in a proper position
    		//if there is any space left after previous field, place it there
    		int recolocado = 0;
    		recolocado=repositionField(pField,pPreviousField,bottomMostPoint,left,maxColWidth);
    		boolean bCallNormalizeLabels = false;
    		if(!ff.isTopLabel())
    		{
    			if(maxLabelWidth<labWid && recolocado==0)
    			{
    				maxLabelWidth = labWid;
    				//make all labels equal size, this is equal to the label with max width
    				//normalizeLabels(vOneColumn);
    				bCallNormalizeLabels = true;
    			}
    		}
    		//for the next iteration this will become previous field
    		pPreviousField = pField;
    		Rectangle rcF = pField.getBounds();
    		if(rcF.y+rcF.height>bottomMostPoint)
    			bottomMostPoint = rcF.y+rcF.height;
    		//recalculate top position for the next row in this column
    		top = rcF.y + rcF.height+getGroupVGap();
    		//if a new field found whose width exceeds the previous maximum width you have to
    		//reposition all fields
    		//int newColWidth = (int)(calcPos.width);
    		int newColWidth = (int)(rcF.width);
    		if(maxColWidth<newColWidth)
    		{
    			maxColWidth = newColWidth;

    			top -= reorganizeComponents(frmGrp,vOneColumn,left,maxColWidth,maxLabelWidth);

    			//normalizeLabels(vOneColumn);
    			bCallNormalizeLabels = true;
    			//the bottomMostPoint may have changed, recalculate it now
    			bottomMostPoint = 0;
    			for(int k = 0;k<vOneColumn.getFieldCount();k++)
    			{
    				GProcessedField pF = (GProcessedField)vOneColumn.fieldAt(k);
    				Rectangle rc2 = pF.getBounds();
    				if(rc2.y+rc2.height>bottomMostPoint)
    					bottomMostPoint = rc2.y+rc2.height;
    			}
    		}
    		if(bCallNormalizeLabels){
    			if(uniqueColumn){
    				int maxColWidthAux=normalizeLabels(vOneColumn);
    				maxColWidth=Math.max(maxColWidth, maxColWidthAux);
    			}else maxColWidth=normalizeLabels(vOneColumn);
    		}
    	}
    	
    	processedGroup.addColumn(vOneColumn);
    	vOneColumn.setColumnWidth(maxColWidth);
    	vProcessedFieldList.addAll(vOneColumn.getFieldList());
    	//printVector(vProcessedFieldList,"1");
    	//printVector(vOneColumn.getFieldList(),"2");
    	processedGroup.setProcessedFieldList(vProcessedFieldList);
    	Enumeration en = vProcessedFieldList.elements();
    	int right = -99999;
    	int bottom = -99999;
    	
    	while(en.hasMoreElements())
    	{
    		GProcessedField pField = (GProcessedField)en.nextElement();
    		Rectangle rcField = pField.getBounds();
    		if(rcField.x+rcField.width>right)
    			right = rcField.x+rcField.width;
    		if(rcField.y+rcField.height>bottom)
    			bottom = rcField.y+rcField.height;
    	}
    	processedGroup.setBounds(new Rectangle(0,0,right+getGroupRightMargin(),bottom+getGroupBottomMargin()));
    	return processedGroup;
    }
    /**
     * Este método posiciona un campo en su lugar correspondiente de la columna y del formulario.
     * Si tiene espacio en la columna para colocarlo en la misma fila que el campo anterior 
     * lo coloca (siempre y cuando el campo quepa en el ancho de la columna).
     * Si no, se coloca el campo debajo del anterior.
     * @param fieldToPosition El campo que va a ser posicionado.
     * @param prevField El campo anterior al posicionado.
     * @param prevBottomMostPt El punto más bajo del campo colocado anteriormente, es decir, el punto a partir del cual queremos colocar el nuevo campo.
     * @param xColPos La posición X de la columna en el grupo correspondiente.
     * @param maxColWidth Máximo ancho de la columna.
     * @return int - Devuelve 0 si el método se ha ejecutado con éxito y no ha sido recolocado el campo en la misma fila. <br>Devuelve 1 si el campo ha sido recolocado, para que posteriormente en GProcessedGroup no se tenga en cuenta el ancho de la etiqueta de ese campo para normalizar.
     */
    private int repositionField(GProcessedField fieldToPosition,GProcessedField prevField,int prevBottomMostPt,int xColPos,int maxColWidth)
    {
        if(prevField==null)
            return 0;
        /*
        //AQUI
        if (prevField.getFormField().getLabel().equals("fechaAprovisionamiento"))
        	System.out.println(prevField.toString());
        */
        Rectangle rcPrev = prevField.getBounds();
        int right = rcPrev.x + rcPrev.width+getGroupHGap();
        Rectangle rc = fieldToPosition.getBounds();
        Dimension dimPreferred = fieldToPosition.getFormField().getPreferredSize();
        //check if there is any space left at the right side of the last placed field.
        if(right+dimPreferred.width<=xColPos+maxColWidth && (fieldToPosition.getFormField().isTopLabel() == prevField.getFormField().isTopLabel()))
        {
            rc.x = right;
            rc.y = rcPrev.y;
            //the label width has been changed when it was placed, see the place from where it is called
            //so we are taking the minimum label width
            //this looks ugly because the subcolumns are not alligned ,this should be improved
            if(!fieldToPosition.getFormField().isTopLabel())
            {
                int labWid = getDimString(fieldToPosition.getFormField().getLabel(), true, fieldToPosition.getFormField().isHighlighted()).width;
                //Dimension dimCompSec = fieldToPosition.getFormField().getDimComponenteSecundario();
                Rectangle rcCompSec = fieldToPosition.getComponentSecundarioBounds();
                Rectangle rcComponent = fieldToPosition.getComponentBounds();
                fieldToPosition.setLabelBounds(new Rectangle(0, 0, labWid,rc.height));
                fieldToPosition.setComponentBounds(new Rectangle(labWid, /*0*/rcComponent.y,dimPreferred.width - labWid - rcCompSec.width, /*rc.height*/rcComponent.height));
                fieldToPosition.setComponentSecundarioBounds(new Rectangle(dimPreferred.width -rcCompSec.width, /*0*/rcCompSec.y,rcCompSec.width, /*rc.height*/rcCompSec.height));
                rc.width = dimPreferred.width;
            }
            fieldToPosition.setBounds(rc);
            fieldToPosition.setSubColumn(prevField.getSubColumn()+1);
            return 1;
        }
        else
        {
            //place it just below the previous one
            //rc.y = rcPrev.y + rcPrev.height + getGroupVGap();
            rc.y = prevBottomMostPt + getGroupVGap();
            fieldToPosition.setBounds(rc);
        }
        return 0;
    }

    /**
     * Este método reorganiza los campos de una columna, viendo si es posible introducir varios 
     * campos en la misma fila, recorriendo toda la columna, y desplazando los campos necesarios
     * hacia arriba, si ha habido alguna recolocación.
     * @param frmGrp El grupo sobre el que estamos iterando, a la hora de recolocar los campos para una columna.
     * @param vOneColumn La columna sobre la que estamos colocando los campos, y que queremos optimizar en altura, colocando varios campos en una misma fila.
     * @param colLeftPos Punto más a la izquierda de la columna.
     * @param maxColWidth Ancho máximo de la columna.
     * @param maxLabelWidth Ancho máximo de la etiqueta de los campos que contiene la columna.
     * @return int - Devuelve la altura final del grupo, tras recolocar (si ha sido posible) varios campos en una misma fila.
     */
    protected int reorganizeComponents(GFormGroup frmGrp,GGrpColumn vOneColumn,int colLeftPos,int maxColWidth,int maxLabelWidth)
    {
    	
        //calculate the maximum width in that column
        int elCount = vOneColumn.getFieldCount();
        int heightReduced = 0;
        int top = getGroupTopMargin();
        if(frmGrp.getId()==0 && getGroupTopMargin()!=0)
            top=getGroupVGap();
        int left = colLeftPos;//+getGroupLeftMargin();
        int yPosDiff = 0;
        for(int i=0;i<elCount;i++)
        {
            //place in a row as much fields possible
            GPosChangeRes res = placeFieldsInOneRow(vOneColumn,i,maxColWidth,left,top,maxLabelWidth);
            top += (res.rowHeight + getGroupVGap());
            if(res.nFieldPlaced<=1)
                continue;
            i += (res.nFieldPlaced-1);
            if(i+1 < elCount)
            {
                GProcessedField pField = (GProcessedField) vOneColumn.fieldAt(i+1);
                yPosDiff = pField.getBounds().y-top;
            }
            heightReduced += /*res.*/yPosDiff;
            //others will move up one row
            if(/*res.*/yPosDiff>0)
            {
                //it may happen that one field from the next row goes up,but
                //another element was there in the same row, that means other
                //rows will not move up in this case also
                boolean bMoveUp = true;
                if((i+1) < elCount)
                {
                    GProcessedField pField = (GProcessedField) vOneColumn.fieldAt(i);
                    Rectangle rc1 = pField.getBounds();
                    pField = (GProcessedField) vOneColumn.fieldAt(i+1);
                    Rectangle rc2 = pField.getBounds();
                    if(rc1.y+/*res.*/yPosDiff==rc2.y)
                        bMoveUp = false;
                }
                if(bMoveUp)
                {
                    for (int j = i + 1; j < elCount; j++)
                    {
                        GProcessedField pField = (GProcessedField) vOneColumn.fieldAt(j);
                        Rectangle rc = pField.getBounds();
                        rc.y -= (/*res.*/yPosDiff + getGroupVGap());
                        pField.setBounds(rc);
                    }
                }
            }
        }
        return heightReduced;
    }
    /**
     * Esta clase se usa internamente para devolver una tupla. 
     * Será usada para la devolución de una tupla 
     * en el método {@link #placeFieldsInOneRow(GGrpColumn, int, int, int, int, int)}.
     */
    class GPosChangeRes
    {
        int nFieldPlaced;
        int rowHeight;
    }
    
    /**
     * Este método devuelve el número de campos que pueden ser colocados en una misma fila
     * para un ancho definido (incluyendo el espaciado entre ellos).
     * Este método será llamado desde {@link #reorganizeComponents(GFormGroup, GGrpColumn, int, int, int)}.
     * @param grpColumn Columna que va a ser procesada (para iterar por todos sus campos)
     * @param startIndex Índice para empezar a iterar
     * @param colWidth Ancho de la columna
     * @param left Borde izquierdo de la columna
     * @param top Borde superior de la columna
     * @param maxLabelWidth Eiqueta más ancha de la columna (se harán todas las etiquetas igual de anchas que ésta).
     * @return GPosChangeRes - Devuelve el número de campos procesados y el alto de la fila.
     */
    private GPosChangeRes placeFieldsInOneRow(GGrpColumn grpColumn,int startIndex,int colWidth,int left,int top,int maxLabelWidth)
    {
        GPosChangeRes res = new GPosChangeRes();
        int size = grpColumn.getFieldCount();
        int totalWidth = 0;
        int nPlaced = 0;
        int subColumn = 0;
        int rowHeight = 0;
        GProcessedField prevField=null;
        /*
        //AQUI
        if(size>1){
        	GProcessedField tonteria = (GProcessedField)(grpColumn.getFieldList().get(1));
        	if(tonteria != null && tonteria.getFormField().getLabel().equals("fechaAprovisionamiento")){
        		GProcessedField tonteria2 = (GProcessedField)(grpColumn.getFieldList().get(0));
        	   	System.out.println("HOLA CARACOLA " + tonteria.toString()+ " " + tonteria2.getLabelBounds().width + " " + tonteria.getColumn() + " " + tonteria.getSubColumn());
        	}
        }
        */
        
        for(int i = startIndex;i<size; i++)
        {
            GProcessedField pField = (GProcessedField)grpColumn.fieldAt(i);
            //you know the dimension has been changed because it is aligned
            //according to the label width, so for subcolumns greater than 0
            //you must calculate on the basis of its preferred dimension.
            Rectangle rc = pField.getBounds();
            Dimension dimPreferred = pField.getFormField().getPreferredSize();
            if(subColumn==0)
            {
            	prevField=pField;
                if(!pField.getFormField().isTopLabel())
                {
                    Rectangle rcLabel = pField.getLabelBounds();
                    Rectangle rcCompSec = pField.getComponentSecundarioBounds();
                    rc.width += (maxLabelWidth - rcLabel.width);
                    pField.setLabelBounds(new Rectangle(0, 0, maxLabelWidth, rc.height));
                    Rectangle rcComponent = pField.getComponentBounds();
                    pField.setComponentBounds(new Rectangle(maxLabelWidth, /*0*/rcComponent.y,rcComponent.width, rcComponent.height));
                    pField.setComponentSecundarioBounds(new Rectangle(maxLabelWidth+rcComponent.width, /*0*/rcCompSec.y,rcCompSec.width, rcCompSec.height));                   
                }
                totalWidth += rc.width;
            }
            else
                totalWidth += dimPreferred.width+getGroupHGap();
            if(totalWidth>colWidth)
                break;
            totalWidth += getGroupHGap();
            rc.x = left;
            rc.y = top;
            //if it is not the first sub column, take the minimum label width
            if(subColumn>0 && !pField.getFormField().isTopLabel())
            {
            	if (prevField.getFormField().isTopLabel())
            	
            		break;
            	
            		
                int labWid = getDimString(pField.getFormField().getLabel(), true, pField.getFormField().isHighlighted()).width;
                //Dimension dimCSec = pField.getFormField().getDimComponenteSecundario();
                Rectangle rcCompSec = pField.getComponentSecundarioBounds();
                Rectangle rcComponent = pField.getComponentBounds();
                pField.setLabelBounds(new Rectangle(0, 0, labWid,rc.height));
                /*pField.setComponentBounds(new Rectangle(labWid, 0,dimPreferred.width - labWid, rc.height));*/
                pField.setComponentBounds(new Rectangle(labWid, /*0*/rcComponent.y,dimPreferred.width - labWid - rcCompSec.width, /*rc.height*/rcComponent.height));
                pField.setComponentSecundarioBounds(new Rectangle(dimPreferred.width - rcCompSec.width, /*0*/rcCompSec.y,rcCompSec.width, /*rc.height*/rcCompSec.height));
                rc.width = dimPreferred.width;
            }
            
            if(pField.getFormField().isTopLabel() && prevField!=null && !prevField.getFormField().isTopLabel())
            
            	break;
           
           
            
            pField.setBounds(rc);
            pField.setSubColumn(subColumn++);
            left += (rc.width+ getGroupHGap());
            nPlaced++;
            if(rc.height>rowHeight)
                rowHeight = rc.height;
        }
        //At least one is placed when this function is called
        //May be it is its unaltered position
        //Please note that it is causing problem from where it is called
        //It is assumed there that at least one is placed
        if(nPlaced == 0)
        {
            nPlaced = 1;
            GProcessedField pField = (GProcessedField)grpColumn.fieldAt(startIndex);
            rowHeight = pField.getBounds().height;
        }
        res.nFieldPlaced = nPlaced;
        res.rowHeight = rowHeight;
        return res;
    }
    /**
     * Este método hace todas las longitudes de las etiquetas iguales a la de mayor ancho.
     * This method makes all label length equal to the longest one
     */
    private int normalizeLabels(GGrpColumn grpColumn)
    {
   	
        Enumeration en = grpColumn.getFieldList().elements();
        int maxLabelWidth = 0;
        
        en=grpColumn.getFieldList().elements();
        while(en.hasMoreElements())
        {
            GProcessedField pField = (GProcessedField)en.nextElement();
            if(pField.getFormField().isTopLabel())
                continue;
            if(pField.getSubColumn()>0)
                continue;
            if(pField.getLabelBounds().width>maxLabelWidth)
                maxLabelWidth = pField.getLabelBounds().width;
        }
        en = grpColumn.getFieldList().elements();
        int maxWidth = 0;
        int incrementoAnchoBounds = 0;
        while (en.hasMoreElements())
        {
            GProcessedField pField = (GProcessedField) en.nextElement();
            Rectangle rcBounds = pField.getBounds();
            Rectangle rcLabel = pField.getLabelBounds();
            Rectangle rcComponent = pField.getComponentBounds();
            Rectangle rcCompSec = pField.getComponentSecundarioBounds();
            //if(pField.getSubColumn()>0)
            //    continue;
            if(pField.getSubColumn()>0){
            	// Habría que tener en cuenta si al incrementar los campos no nos
            	// salimos del tamaño de la columna
            	rcBounds.x +=incrementoAnchoBounds;
            	continue;
            }
            	
            if(pField.getFormField().isTopLabel())
            {
                if (rcBounds.width > maxWidth)
                    maxWidth = rcBounds.width;
                continue;
            }

            incrementoAnchoBounds= (maxLabelWidth - rcLabel.width);
            
            rcBounds.width +=incrementoAnchoBounds;
            //rcBounds.width += (maxLabelWidth - rcLabel.width);
            rcLabel.width = maxLabelWidth;
            rcComponent.x = maxLabelWidth;
            rcCompSec.x = rcComponent.x+rcComponent.width;
            pField.setBounds(rcBounds);
            pField.setLabelBounds(rcLabel);
            pField.setComponentBounds(rcComponent);
            pField.setComponentSecundarioBounds(rcCompSec);
            if(rcBounds.width>maxWidth)
                maxWidth = rcBounds.width;
        }
        return maxWidth;
    }

    /*
    For debug purpose, this should not be there in the final version
    */
    public Vector getProcessedFormList()
    {
        return m_vProcessedFormList;
    }
    /**
     * Este método devuelve el atributo {@link #m_objFormFieldList}.
     * @return GFormFieldList - El atributo {@link #m_objFormFieldList}.
     */
    public GFormFieldList getFormFieldList()
    {
        return m_objFormFieldList;
    }
    /**
     * Este método genera las diferentes combinaciones de posiciones de los grupos en el formulario.
     * Las diferentes dimensiones de los formularios han sido calculadas previamente 
     * a la llamada de éste método, y almacenadas en la clase {@link GGrpCombination}, y por 
     * tanto en este método se remite a estas dimensiones.
     * 
     * @param vGrpCombinationList  Este vector contiene todas las posibles combinaciones para cada grupo. 
     * Cada elemento de este vector es del tipo {@link GGrpCombination}.
     * @return Vector - El vector devuelto consiste en una combinación concreta de los grupos posibles.
     * Cada elemento de este vector es del tipo {@link GProcessedForm}.
     */
    protected Vector generateAllFormCombinations(Vector vGrpCombinationList)
    {
        Vector vTotalGrp = new Vector();
        GGrpCombinationTree tree = new GGrpCombinationTree(new GNode());
        Enumeration enumTotalGrpCombination = vGrpCombinationList.elements();
        int level = 1;
        //first form the tree of all possible combinations of groups
        while(enumTotalGrpCombination.hasMoreElements())
        {
            GGrpCombination objGrpCombination = (GGrpCombination)enumTotalGrpCombination.nextElement();
            Enumeration enumOneGrpCombinations = objGrpCombination.getCombinations();
            while(enumOneGrpCombinations.hasMoreElements())
            {
                GProcessedGroup grp = (GProcessedGroup)enumOneGrpCombinations.nextElement();
                tree.addData(level,grp);
            }
            level++;
        }
        GProcessedForm lessBad=null;
        int numFound=0;
        //next convert it to a vector
        Enumeration enumGrpCombination = tree.enumPaths();
        while(enumGrpCombination.hasMoreElements() && numFound<GConst.MAX_FORMS_POSSIBLE_COMBINATION)
        {
            //This is one set of group combinations(group with different field positions)
            Vector v = (Vector)enumGrpCombination.nextElement();//each element of vector is of type GProcessedGroup
            Vector vProcessedGroupList = new Vector();
            Enumeration e = v.elements();
            while(e.hasMoreElements())
            {
                GNode oneNode = (GNode)e.nextElement();
                GProcessedGroup pGroup = (GProcessedGroup)oneNode.getData();
              
                vProcessedGroupList.addElement(pGroup);
            }
            //now vProcessedGroupList contains set of all groups with one combination of group's DIMENSION

            //There may be different combinations possible for the same group dimension
            //take the best one
            GGenericBinaryTree treeBin = new GGenericBinaryTree(new GTwoChildNode());
            calculateGroupPos(vProcessedGroupList, null, 0, DIR_HORIZONTAL,treeBin.getRoot());
            Enumeration enumFormCombination = treeBin.enumPaths();
            while (enumFormCombination.hasMoreElements())
            {
                Vector v2 = (Vector) enumFormCombination.nextElement();
                GProcessedForm processedForm2 = new GProcessedForm(m_objFormFieldList);
                Vector vProcessedGroupList2 = new Vector();
                Enumeration e2 = v2.elements();
                int right = -99999;
                int bottom = -99999;
                while (e2.hasMoreElements())
                {
                    GTwoChildNode oneNode = (GTwoChildNode) e2.nextElement();
                    if(oneNode.getData()==null)
                        continue;
                    GProcessedGroup pGroup = (GProcessedGroup) oneNode.getData();
                    pGroup = new GProcessedGroup(pGroup);
                    vProcessedGroupList2.addElement(pGroup);
                    Rectangle rc = pGroup.getBounds();
                    if (rc.x + rc.width > right)
                        right = rc.x + rc.width;
                    if (rc.y + rc.height > bottom)
                        bottom = rc.y + rc.height;
                }
                processedForm2.setProcessedGroupList(vProcessedGroupList2);
                processedForm2.setBounds(new Rectangle(0, 0,right + getGroupRightMargin(),bottom + getGroupBottomMargin()));

                
                boolean campmayor=isAnyFieldWidthMax(m_objFormFieldList.getPanelWidth()-GConfigView.PanelLeftMargin-GConfigView.PanelRightMargin,processedForm2.getProcessedGroupList());
                if (campmayor){
                	vTotalGrp.addElement(processedForm2);
                	numFound++;
                }else{
                	//System.err.println(processedForm2.getBounds().width+" "+m_objFormFieldList.getPanelWidth());
                	if(processedForm2.getBounds().width<=m_objFormFieldList.getPanelWidth()){
                		vTotalGrp.addElement(processedForm2);
                		numFound++;
                	}else{
                		//Nos quedamos con el mejor de los descartados por si lo necesitaramos
                		if(lessBad==null || lessBad.getBounds().width>processedForm2.getBounds().width)
                			lessBad=processedForm2;
                	}
                }

            }
        }
        //Si no se encuentra ningun panel que cumpla nuestras restricciones de tamaño nos quedamos con el mejor de los descartados
        if(vTotalGrp.isEmpty() && lessBad!=null)
        	vTotalGrp.addElement(lessBad);
        
        return vTotalGrp;
    }
    private boolean isAnyFieldWidthMax(int panelWidth, Vector groupList) {
		Iterator<GProcessedGroup> it= groupList.iterator();
		//System.out.println("Tamaño total panel="+panelWidth);
		while (it.hasNext()){
			GProcessedGroup group=it.next();
			Iterator<GProcessedField> itfields=group.getProcessedFieldList().iterator();
			while(itfields.hasNext()){
				GProcessedField field= itfields.next();
			//	System.out.println("tamComp="+field.getBounds().width);
				if (field.getBounds().width+GConfigView.GroupLeftMargin+GConfigView.GroupRightMargin>panelWidth)
					return true;
			}
		}
		return false;
	}

	/**
     * Este método devuelve una posible posición del grupo en el formulario.
     * @param rcPrevItemPos La posición del grupo anteriormente colocado.
     * @param theGroup El grupo que voy a colocar.
     * @param direction La dirección en la que va a ser colocado (con respecto al anterior).
     * @return Rectangle La posición final donde se va a colocar.
     */
    protected Rectangle getGroupPos(Rectangle rcPrevItemPos,GProcessedGroup theGroup,int direction)
    {
        Dimension preferredSize = theGroup.getBounds().getSize();
        if(rcPrevItemPos==null)
            return new Rectangle(new Point(getPanelLeftMargin(),getPanelTopMargin()),preferredSize);
        if(direction == DIR_HORIZONTAL)
        {
            int x = rcPrevItemPos.x+rcPrevItemPos.width+getPanelHGap();
            int y = rcPrevItemPos.y;
            return new Rectangle(new Point(x,y),preferredSize);
        }
        else if(direction == DIR_VERTICAL)
        {
            int x = rcPrevItemPos.x/*getPanelLeftMargin()*/;
            int y = rcPrevItemPos.y+rcPrevItemPos.height+getPanelVGap();
            return new Rectangle(new Point(x,y),preferredSize);
        }
        return null;
    }

    /**
     * Este método calcula la posible posición del grupo en el formulario final.
     * @param vGroupList con la lista de grupos que contiene el formulario.
     * @param rcPrevItemPos Rectángulo (área) del grupo anterior de la lista tratado.
     * @param nItemPos La posición del grupo en el vector de grupos.
     * @param direction Dirección (horizontal o vertical) como se quiere colocar el grupo.
     * @param node Información del nodo del árbol correspondiente a ese grupo.
     */
    protected void calculateGroupPos(Vector vGroupList,Rectangle rcPrevItemPos,int nItemPos,int direction,GTwoChildNode node)
    {	
    	GProcessedGroup grpInVec = (GProcessedGroup) vGroupList.elementAt(nItemPos);
        GProcessedGroup theGroup = new GProcessedGroup(grpInVec);
        
        Rectangle calcPos = getGroupPos(rcPrevItemPos,theGroup,direction);
        theGroup.setBounds(calcPos);
        node.setData(theGroup);
        nItemPos++;
        if(nItemPos>=vGroupList.size())
            return;

        boolean moreColumn=true;
        GProcessedGroup nextGrp = (GProcessedGroup) vGroupList.elementAt(nItemPos);
        if(nextGrp.getBounds().getWidth()*2>m_objFormFieldList.getPanelWidth() || (calcPos.getWidth()*2>m_objFormFieldList.getPanelWidth())){
        	//Si el grupo actual o el anterior ocupa mas de la mitad del ancho maximo nos interesa que no este compartiendo fila con otras columnas
        	moreColumn=false;
        }
        if(nextGrp.getBounds().getHeight()>calcPos.getHeight()){
        	//No permitimos que el nuevo grupo sea mas alto que el anterior porque entonces el formulario quedaria descompensado
        	moreColumn=false;
        }
        
    	GTwoChildNode nodeLeft = new GTwoChildNode();
        node.setLeft(nodeLeft);
        calculateGroupPos(vGroupList, calcPos, nItemPos, moreColumn?DIR_HORIZONTAL:DIR_VERTICAL,nodeLeft);
    
        GTwoChildNode nodeRight = new GTwoChildNode();
        node.setRight(nodeRight);
        Rectangle rc = calcPos;
        //if(rcPrevItemPos!=null&&(rc.y+rc.height<rcPrevItemPos.y+rcPrevItemPos.height))
        //	rc=rcPrevItemPos;
        calculateGroupPos(vGroupList, rc, nItemPos, DIR_VERTICAL,nodeRight);
    }
    /**
     * Este método ordenará todos los formularios obtenidos (por puntuación) y
     * se quedará únicamente con el mejor.
     */
    protected void findBestFormCombination()
    {
    	
        if(m_vProcessedFormList==null)
            return;

        Object[] array = m_vProcessedFormList.toArray();
        Arrays.sort(array);
        m_vProcessedFormList = new Vector();
        int arLen = array.length;
        //int maxCombination = 8; Asi estaba antes, pero ahora solo me interesa el mejor
        int maxCombination = 1;
        if(array.length<maxCombination)
            maxCombination = array.length;
        for(int i = 0;i<maxCombination;i++)
        {
            m_vProcessedFormList.addElement(array[i]);
           // System.err.println("TAMAÑO "+((GProcessedForm)array[i]).getBounds());
        }
//        m_vProcessedFormList.addElement(m_objBestProcessedForm);
    }
    /**
     * Este método optimiza el formulario para mejorar el alineamiento y la visión
     * general de éste. 
     */
    protected void fineTuneBestCombination()
    {
        Enumeration en = m_vProcessedFormList.elements();
        while(en.hasMoreElements())
        {
            GProcessedForm objPF = (GProcessedForm) en.nextElement();
            objPF.fineTune(this);
            
        }
        //Se podria ordenar otra vez (si tuvieramos mas de 1 combinacion)
        //por si al optimizar los formularios hay alguno que ha mejorado
        //la puntuacion con respecto a otros
        /*Object[] array = m_vProcessedFormList.toArray();
          Arrays.sort(array);*/
    }
/*
    private void printElements(String str)
    {
        Enumeration en = m_vProcessedFormList.elements();
        while(en.hasMoreElements())
        {
            GProcessedForm objPF = (GProcessedForm) en.nextElement();
            Enumeration eF = objPF.m_vProcessedGroupList.elements();
            while(eF.hasMoreElements())
            {
                GProcessedGroup pG = (GProcessedGroup)eF.nextElement();
                pG.printElements(str);
            }
        }
    }

    private void printVector(Vector v,String str)
    {
        Enumeration en = v.elements();
        while(en.hasMoreElements())
            System.out.println(str+" "+ en.nextElement());
    }*/
    
    /**
     * 
     * @param table
     * @param anchoVista
     * @return Dimension - 
     */
/*    public Dimension getTableDim( Element table, int anchoVista ){
    	//double altoRow=getRowHeight();
    	Dimension dimLabel= getDimString( table.getAttributeValue("LABEL"), true );
    	Iterator iCol=table.getChildren("ITEM").iterator();
    	int anchoTotal=0;//no incluye ancho etiqueta
    	while( iCol.hasNext() ){
    		Element col=(Element)iCol.next();
    		int tm= Integer.parseInt(col.getAttributeValue("ID_TM"));
    		int longitud= -1;//col.getAttributeValue("LABEL").length();
    		if( col.getAttributeValue("LENGTH")!=null )
    			longitud= Integer.parseInt( col.getAttributeValue("LENGTH") );
    		Dimension dim=getDimensiones(anchoVista,tm,col,longitud,false,true,false);
    		col.setAttribute("WIDTH", String.valueOf((int)dim.getWidth()));
    		anchoTotal+=(int)dim.getWidth();
    	}
    	int rows=Integer.parseInt(table.getAttributeValue("ROWS"));
    	/*if( rows>1 ) anchoTotal+=m_cfg.anchoScrollBar;*/
/*    	if( rows>1 ) anchoTotal+=GConfigView.widthScrollBar;
    	int alto= (int)(m_dRowHeight*rows);
    	int numBot=table.getAttributeValue("NUM_BOTONES")==null ? 0:
    		Integer.parseInt(table.getAttributeValue("NUM_BOTONES"));
    	/*int anchoBot=numBot*m_cfg.ladoBotonTabla;*/
/*    	int anchoBot=numBot*GConfigView.tableButtonSide;
    	int anchoMax= (int)(anchoTotal + dimLabel.getWidth() + anchoBot);
    	boolean topLabel=false;
    	if( anchoMax > anchoVista || rows>1 || table.getAttributeValue("HIDE_HEADER")==null ){
    		table.setAttribute("TOPLABEL","TRUE");
    		/*table.setAttribute("HEIGHT_LABEL",String.valueOf((int)m_cfg.ladoBotonTabla));*/
/*   		table.setAttribute("HEIGHT_LABEL",String.valueOf((int)GConfigView.tableButtonSide));
    		table.setAttribute("WIDTH_LABEL", String.valueOf((int)dimLabel.getWidth()));
    		table.setAttribute("POS_BUTTON","TOP");
    	}else{
    		//anchoTotal=anchoMax;
    		table.setAttribute("POS_BUTTON","RIGHT");
    		table.setAttribute("WIDTH_LABEL", String.valueOf((int)dimLabel.getWidth()));
    		/*table.setAttribute("WIDTH_BUTTON", String.valueOf(m_cfg.ladoBotonTabla));//solo tendra un boton*/
/*    		table.setAttribute("WIDTH_BUTTON", String.valueOf(GConfigView.tableButtonSide));//solo tendra un boton
    		if( table.getAttributeValue("TOPLABEL")!=null )
    			table.removeAttribute("TOPLABEL");

    	}

    	if( anchoTotal>anchoVista )
    		anchoTotal=anchoVista;
    	table.setAttribute("WIDTH",String.valueOf(anchoTotal));

    	if( table.getAttributeValue("HIDE_HEADER")==null ){
    		int cab= table.getAttributeValue("HEADER_LINE")==null ?
    				0:Integer.parseInt(table.getAttributeValue("HEADER_LINE"));
    		alto+= (int)(cab*m_dRowHeight);
    	}
    	return new Dimension( anchoTotal,alto) ;
    }

    /**
     * 
     * @param anchoVista
     * @param tm
     * @param viewItem
     * @param longitud
     * @param comentado
     * @param colTabla
     * @param multivalued
     * @return Dimension - 
     */
/*    Dimension getDimensiones( int anchoVista,
    		int tm,
    		Element viewItem,
    		int longitud,
    		boolean comentado,
    		boolean colTabla,
    		boolean multivalued ){
    	//longitud es la que opcionalmente redefinimos en el archivo meta
    	viewItem.setAttribute("ROW_HEIGHT",String.valueOf((int)m_dRowHeight));
    	String contenido= viewItem.getText();
    	Dimension rect=null;
    	switch(tm){
    	case helperConstant.TM_TABLA:
    		return getTableDim( viewItem, anchoVista );
    	case helperConstant.TM_ENUMERADO:
    		if( politica==INPUT_FORM )
    			rect= new Dimension( (int)hallaMaxAnchoEnumerado( viewItem ), (int)m_dRowHeight);
    		else{
    			if(viewItem.getAttributeValue("DEFAULT")==null)
    				rect= new Dimension( 	getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado),
    						(int)m_dRowHeight);
    			else
    				rect= getDimString(hallaAnchoValorEnumerado(viewItem),true);
    		}
    		break;
    	case 	helperConstant.TM_MEMO:
    		if( politica==INPUT_FORM ){
    			if(!(m_bFilterMode||multivalued)){
    				rect= new Dimension( anchoVista-GConfigView.grosorBordeForm, (int)((m_dRowHeight)*3) );
    				viewItem.setAttribute("ROWS","3");
    			}else
    				rect= new Dimension(getAnchoMin(multivalued,
    						helperConstant.TM_TEXTO,
    						longitud,
    						m_dAveCharWidth,
    						comentado,colTabla),
    						(int)m_dRowHeight);
    		}else
    			rect= getJustifyArea(contenido, anchoVista, (int)m_dRowHeight );
    		break;
    	case helperConstant.TM_IMAGEN:
    		rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado), GConfigView.tableButtonSide);
    		break;
    	case helperConstant.TM_ENTERO:
    		if(viewItem.getAttributeValue("DEFAULT")==null)
    			rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado), (int)m_dRowHeight);
    		else
    			rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
    		break;
    	case helperConstant.TM_BOOLEANO:
    		rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,false), (int)m_dRowHeight);
    		break;
    	case helperConstant.TM_BOOLEANO_EXT:
    		rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado), (int)m_dRowHeight);
    		if( comentado ){
    			viewItem.setAttribute("COMMENT_WIDTH",
    					String.valueOf( (int)(GConfigView.minimumLengthCheckExtension*m_dAveCharWidth)));
    			viewItem.setAttribute("COMMENT","TRUE");
    		}

    		break;
    	case helperConstant.TM_REAL:
    		if(viewItem.getAttributeValue("DEFAULT")==null)
    			rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado), (int)m_dRowHeight);
    		else
    			rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
    		break;
    	case helperConstant.TM_FECHA:
    		if(viewItem.getAttributeValue("DEFAULT")==null)
    			rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado), (int)m_dRowHeight);
    		else
    			rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
    		break;
    	case helperConstant.TM_FECHAHORA:
    		if(viewItem.getAttributeValue("DEFAULT")==null)
    			rect= new Dimension( getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado), (int)m_dRowHeight);
    		else
    			rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
    		break;
    	case helperConstant.TM_TEXTO:
    		if( contenido==null || contenido.length()==0 ){
    			if( politica==INPUT_FORM)
    				rect= new Dimension(	getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado,colTabla),
    						(int)m_dRowHeight);
    			else 	rect= new Dimension(	getAnchoMin(multivalued,tm,longitud,m_dAveCharWidth,comentado,colTabla),
    					(int)m_dRowHeight);
    			break;
    		}else
    			rect= getJustifyArea( contenido, anchoVista, m_dRowHeight );
    		break;
    	}
    	Dimension dimLabel= getDimString( viewItem.getAttributeValue("LABEL"), true );
    	double anchoTotal= rect.getWidth() + dimLabel.getWidth();
    	double ancho= rect.getWidth();
    	boolean topLabel=false;
    	if( anchoTotal > anchoVista){
    		ancho= anchoVista;
    		viewItem.setAttribute("TOPLABEL","TRUE");
    		viewItem.setAttribute("HEIGHT_LABEL",String.valueOf((int)dimLabel.getHeight()));
    		topLabel=true;
    	}else{
    		if( viewItem.getAttributeValue("TOPLABEL")!=null )
    			viewItem.removeAttribute("TOPLABEL");

    	}
    	viewItem.setAttribute("WIDTH_LABEL", String.valueOf((int)dimLabel.getWidth()));
    	viewItem.setAttribute("VERT_CELL_PADD", String.valueOf(GConfigView.cellPadd));
    	viewItem.setAttribute("HORIZ_CELL_PADD", String.valueOf(GConfigView.cellPadd));
    	viewItem.setAttribute("V_EDIT_PADD", String.valueOf(GConfigView.V_InternalEditPadd));
    	viewItem.setAttribute("H_EDIT_PADD", String.valueOf(GConfigView.H_InternalEditPadd));
    	rect.setSize( ancho, rect.getHeight() );
    	return rect;
    }

    /**
     * 
     * @param item
     * @return double - 
     */
/*    double hallaMaxAnchoEnumerado( Element item ){
    	double max= 0;
    	if( politica== INPUT_FORM){
    		//esta funcion solo es valida para la polita input form que espera que bajo el elemento data
    		//se definan todos los valores de enumerado
    		Integer tapos= new Integer( item.getAttributeValue("TA_POS") );

    		metaData md=Singleton.getComm().getMetaData();
    		Iterator iValues= md.getEnumSet( tapos );
    		while(iValues.hasNext()){
    			Integer idenum= (Integer)iValues.next();
    			String name= md.getEnumLabel( tapos, idenum );
    			Dimension rect= getDimString( name, true );
    			max= Math.max(max,rect.getWidth()+25);
    			//20 es el ancho del selector
    		}
    	}
    	return max;
    }

    /**
     * 
     * @param item
     * @return String - 
     */
/*    String hallaAnchoValorEnumerado( Element item ){
    	double max= 0;
    	Integer tapos= new Integer( item.getAttributeValue("TA_POS") );

    	metaData md=Singleton.getComm().getMetaData();
    	Iterator iValues= md.getEnumSet( tapos );
    	while(iValues.hasNext()){
    		Integer idenum= (Integer)iValues.next();
    		String name= md.getEnumLabel( tapos, idenum );
    		if( idenum.toString().equals( item.getAttributeValue("VALUE")))
    			return name;
    	}
    	return null;
    }

    /**
     * 
     * @param multivalued
     * @param tm
     * @param longitud
     * @param anchoChar
     * @param comentado
     * @return int - 
     */
/*    int getAnchoMin( boolean multivalued,int tm, int longitud, double anchoChar, boolean comentado ){
    	return getAnchoMin(multivalued,tm,longitud,anchoChar,comentado,false);
    }

    /**
     * Devuelve el ancho mínimo del componente
     * @param multivalued
     * @param tm
     * @param longitud
     * @param anchoChar
     * @param comentado
     * @param columnaDeTabla
     * @return int - 
     */
/*    int getAnchoMin( boolean multivalued,int tm, int longitud, double anchoChar, boolean comentado, boolean columnaDeTabla ){
    	int mult= (m_bFilterMode||multivalued) ? 2:1;
    	int suma= (m_bFilterMode||multivalued) ? 1:0;
    	switch(tm){
    	case helperConstant.TM_ENUMERADO:
    		return anchoEdit(anchoChar, (longitud==-1 ? 	GConfigView.minimumLengthText:
    			longitud));
    		//Math.max(m_cfg.longMinimoCampoTexto,longitud))*anchoChar);
    	case helperConstant.TM_ENTERO:
    		return anchoEdit(anchoChar,suma+mult*(longitud==-1 ? 	GConfigView.minimumLengthNumericField:
    			longitud));
    		//Math.max(m_cfg.longMinimoCampoNumerico,longitud))*anchoChar);
    	case helperConstant.TM_IMAGEN:
    		return GConfigView.tableButtonSide*3;//ver,borrar,asignar

    	case helperConstant.TM_BOOLEANO:
    		return GConfigView.minimumWidthBoolField;

    	case helperConstant.TM_BOOLEANO_EXT:
    		if( !comentado )
    			return GConfigView.minimumWidthBoolField;
    		else
    			return GConfigView.minimumWidthBoolField + (int)(GConfigView.minimumLengthCheckExtension*anchoChar);
    	case helperConstant.TM_REAL:
    		return anchoEdit(anchoChar,suma+mult*(longitud==-1 ?	GConfigView.minimumLengthNumericField:
    			longitud));
    		//Math.max(m_cfg.longMinimoCampoNumerico,longitud))*anchoChar);
    	case helperConstant.TM_FECHA:
    		return anchoEdit(anchoChar,suma+mult*(longitud==-1 ? 	GConfigView.minimumLengthDate:
    			longitud));
    		//Math.max(longitud,m_cfg.longMinimoCampoFecha))*anchoChar);
    	case helperConstant.TM_FECHAHORA:
    		return anchoEdit(anchoChar,suma+mult*(longitud==-1 ? GConfigView.longMinimoCampoFechaHora:
    			longitud));
    	case helperConstant.TM_TEXTO:
    		int longc=columnaDeTabla ? GConfigView.minimumLengthTextTable:GConfigView.minimumLengthText;
    		if( longitud==-1 ){
    			return (int)(longc*anchoChar);
    		}
    		longc= Math.min( longc, longitud );
    		return anchoEdit(anchoChar,longc);
    	}
    	return 0;
    }

    /**
     * Devuelve la dimensión de un área de texto.
     * @param texto
     * @param anchoMax
     * @param altoLinea
     * @return Dimension - 
     */
/*    Dimension getJustifyArea( String texto, double anchoMax, double altoLinea ){
    	int anchoVista=(int)anchoMax;//Esto es provisional

    	double altoMemo= m_dRowHeight;
    	Dimension r= getDimString( texto, false );
    	if( texto!=null && texto.length() > anchoVista ){
    		if( r.getHeight() > altoLinea + 5 )
    			return r;
    		altoMemo= (r.getWidth()/anchoVista)*m_dRowHeight;
    		return new Dimension( (int)anchoMax, (int)altoMemo );
    	}
    	return r;
    }
*/
    /**
     * Devuelve el ancho de un campo de texto
     * @param anchoChar Ancho de un carácter (hallado como media). 
     * @param lon Número de caracteres que quiero que quepan en el campo de texto.
     * @return int - 
     */
    int anchoEdit( double anchoChar, int lon ){
    	return (int)(anchoChar*lon + 2*GConfigView.H_InternalEditPadd);//el 3 es el ancho de los bordes
    }

    /**
     * Iterador para los grupos. 
     * @return boolean - Devuelve cierto si hay más grupos que tratar.
     */
    public boolean hasItems(){
    	Vector grupos=m_objFormFieldList.getGroupList();
    	if( grupos.size()==0 ) return false;
    	Iterator itr= grupos.iterator();
    	while( itr.hasNext() ){
    		GFormGroup gfgroup=(GFormGroup)itr.next();
    		if( gfgroup==null ) continue;
    		if( !gfgroup.getFieldList().isEmpty()) return true;
    	}
    	return false;
    }

    /**
     * Elimina el grupo cuyo Id corresponda con el del parámetro
     * @param groupId Id del grupo que queremos borrar.
     */
    public void removeGroup(int groupId){
    	m_objFormFieldList.removeGroup(groupId);
    }

	public Dimension getPanelDimension() {
		return new Dimension(m_objFormFieldList.getPanelWidth(),m_objFormFieldList.getPanelHeight());
	}

	public Dimension getPanelDimensionUseful() {
		Dimension dimPanel=getPanelDimension();
		//System.err.println("Antess useful "+dimPanel);
		dimPanel.width-=(getPanelRightMargin()+getPanelLeftMargin()+getPanelHGap());
		dimPanel.height-=(getPanelTopMargin()+getPanelBottomMargin()+getPanelVGap());
		//System.err.println("Despues useful "+dimPanel);
		return dimPanel;
	}

	public Graphics getGraphics() {
		return m_graphics;
	}
}
