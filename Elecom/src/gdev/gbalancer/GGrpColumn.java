package gdev.gbalancer;

import gdev.gen.GConfigView;
import gdev.gen.IViewBalancer;
import gdev.gfld.GFormBoolean;
import gdev.gfld.GFormBooleanComment;
import gdev.gfld.GFormDate;
import gdev.gfld.GFormDateHour;
import gdev.gfld.GFormEnumerated;
import gdev.gfld.GFormFile;
import gdev.gfld.GFormHour;
import gdev.gfld.GFormImage;
import gdev.gfld.GFormTable;

import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Vector;


/**
 * Esta clase representa una columna de un grupo en el formulario final.
 * <br>Dentro de la columna hay filas ({@link GRow}) que a su vez pueden tener varias subcolumnas.
 * Si una columna tiene una sola subcolumna, la subcolumna ocupará la columna entera.
 * <br>Una vez que se crea el objeto de esta clase, los campos tienen una posición concreta
 * en el formulario (con respecto a indice de columna y fila), que no será modificado.
 * Lo único que se puede modificar del campo es moverlo hacia la derecha para alinearlo con el campo superior
 * y aumentar el ancho de sus componentes para ajustarlo al margen derecho,
 * pero el indice de columna, fila y subcolumna no será modificado aquí.
 *
 * <p><h3>Algoritmo de Alineación</h3>
 * Los pasos básicos de la alineación se empiezan a invocar desde el método {@link #fineTune(IViewBalancer)}, por lo que se puede seguir una traza a partir de este método para comprender mejor el proceso de alineamiento que se sigue. Sólo podemos obtener los campos por columnas, por lo que el alineamiento se va realizar sobre cada columna independientemente.
 * <br>Los pasos a seguir (muy a groso modo también) son los siguientes:
 * <ol>
 * <li>Calculamos el número máximo de subcolumnas que tiene la columna.</li>
 * <li>Por cada subcolumna (excepto la primera), obtenemos todas la filas que tienen al menos esa subcolumna. Por ejemplo, si el máximo de subcolumnas es 3, obtendremos todas las filas que contienen 2 o más subcolumnas y después todas las filas que contienen 3 subcolumnas. (Esto nos permite alinear la subcolumna 2 de una fila de 2 subcolumnas, con la subcolumna 2 de una fila de 3 subcolumnas).</li>
 * <li>Una vez que tenemos todas las filas que tienen más de una subcolumna, voy trazando los alineamientos independientes por subcolumnas. Para ello haremos lo siguiente (método {@link #processRowsSymmetric(IViewBalancer, Vector, int)}):
 * 		<ol>
 * 		<li>Separaremos campos de la subcolumna con orden preestablecido de los que no tienen orden.</li>
 * 		<li>Normalizaremos primero las posiciones de los campo y después normalizaremos las etiquetas.
 * 			<ul>
 * 			<li>Para normalizar la posición (método {@link #normalizeX(IViewBalancer, Vector, Vector, GProcessedField)}) calculamos el campo que está más a la derecha y alineamos todos los campos de la subcolumna a esa posición.</li>
 * 			<li>Para normalizar el ancho de la etiqueta (método {@link #normalizeLabels(IViewBalancer, Vector, Vector, GProcessedField)}) calculamos la etiqueta mayor e incrementamos todas las etiquetas para que tengan ese tamaño.</li>
 * 			<li>Si ha habido incremento (sólo se llevará a cabo si los campos de la fila no se salen de la columna), desplazaremos con el mismo incremento todas las etiquetas de las siguientes subcolumnas.</li>
 * 			</ul>
 * 		</li>
 * 		</ol>
 * </li>
 * <li>Por último, procederemos a alinear los márgenes derechos de los componentes por subcolumnas. Para ello agrandaremos los componentes para que todos terminen en el mismo margen derecho. Como hemos hecho anteriormente, si el incremento no es posible porque algunos campos se salen de la columna, no se llevará a cabo y se dejará desalineado. Todo esto se hará llamando al método {@link #alineaElementos()} por lo que se recomienda hacer una traza de éste método para entenderlo mejor. 
 * 		Los pasos que seguimos son los siguientes:
 * 		<ol>
 * 		<li>Separaremos la subcolumna de la derecha del todo, del resto de subcolumnas, y las trataremos de forma separada con los métodos {@link #alineaSubColumna(Vector, int)} (donde Vector es la columna de la derecha) y {@link #alineaElementosIntermedio(Vector, int)}</li>
 * 		<li>Para la subcolumna de la derecha, calculamos el máximo margen derecho, y para todos los campos (excepto los CheckBox) calculamos el incremento necesario para ajustarse a ese margen, vemos si no se nos sale de la columna y modificamos el borde si es posible.</li>
 * 		<li>Para las subcolumnas intermedias, primero separaremos los elementos de la subcolumna que tienen un orden preestablecido de los que no lo tienen. Alinearemos cada una de estas dos subcolumnas por separado. Para cada una de ellas haremos (método {@link #alineaSubColumna(Vector, int)}):
 * 			<ul>
 * 			<li>Calculamos el máximo margen derecho de cada subcolumna.</li>
 * 			<li>Intentamos ajustar todos los campos de la subcolumna a ese margen derecho, comprobando si el incremento es posible y desplazando los elementos de las subcolumnas de la derecha si hemos llevado a cabo el desplazamiento.</li>
 * 			</ul>
 * 		</li>
 * 		</ol>
 * </li>
 * </ol>
 * </p>
 * @author Dynagent
 * @author Juan
 * 
 */
public class GGrpColumn
{
    /**
     * Este vector contiene todos los campos de la columna.
     */
    protected Vector<GProcessedField> m_vFieldList=new Vector<GProcessedField>();
    /**
     * Este atributo reprenta el indice de la columna en el grupo, empezando desde 0.
     */
    protected int m_iColumnIndex;
    /**
     * Este vector contiene todas las filas de la columna. 
     * Cada elemento de este vector es del tipo GRow.
     * <br>Si una fila de la columna no tiene subcolumnas, la fila contendrá un único campo.
     * <br>Si contiene varias subcolumnas, el numero de subcolumnas será igual al número de campos de la fila y por tanto igual al numero de campos del elemento GRow.
     * <br>El número de la subcolumna empieza desde 0.
     */
    protected Vector<GRow> m_vRowList=new Vector<GRow>();
    /**
     * Este atributo almacena el ancho total de la columna.
     * La suma de los campos de una fila nunca debe superar en ancho a este valor.
     */
    protected int m_iColWidth;

    /**
     * Este método sirve para añadir los campos a la columna. 
     * @param fld GProcessedField Este es el campo que se va a añadir a la columna.
     */
    public void addField(GProcessedField fld)
    {
        m_vFieldList.addElement(fld);
    }
    /**
     * Este método configura el indice de la columna. Se empieza a contar desde 0.
     * @param index El nuevo índice que le voy a dar a la columna.
     */
    public void setColumnIndex(int index)
    {
        m_iColumnIndex = index;
    }
    /**
     * Este método devuelve el índice de la columna que está siendo referenciada.
     * @return int - El índice de la columna
     */
    public int getColumnIndex()
    {
        return m_iColumnIndex;
    }
    /**
     * Este método devuelve el número de filas que tiene la columna.
     * @return int - El número de filas que tiene la columna.
     */
    public int getRowCount()
    {
        return m_vRowList.size();
    }
    /**
     * Este método devuelve el vector de los campos que contiene la columna.
     * Cada elemento del vector será del tipo GProcessedField.
     * @return Vector - Es el vector con todos los campos que contiene la columna.
     * @see Vector
     */
    public Vector getFieldList()
    {
        return m_vFieldList;
    }
    /**
     * Este método devuelve el número de campos que contiene la columna.
     * Este número no tiene porqué ser igual al número de filas,
     * ya que una fila puede tener varios campos (varias subcolumnas).
     * @return int - El número de campos que contiene la columna.
     */
    public int getFieldCount()
    {
        return m_vFieldList.size();
    }
    /**
     * Este método devuelve un campo concreto referenciado por un índice.
     * <br>Si todas las filas de la columna contienen un único campo (no tienen subcolumnas),
     * el índice corresponderá con el número de fila (empezando siempre por 0).
     * <br>Si alguna fila tiene subcolumnas, el índice no coincidirá con el número de fila.
     * @param index Es el indice para buscar el campo concreto.
     * @return GProcessedField - Es el campo concreto que se va a devolver.
     */
    public GProcessedField fieldAt(int index)
    {
        return m_vFieldList.elementAt(index);
    }
    /**
     * Este método modifica el ancho total de la columna.
     * @param colWidth Este parámetro es el nuevo ancho de la columna.
     */
    public void setColumnWidth(int colWidth)
    {
        m_iColWidth = colWidth;
    }
    

    /**
     * Inicialmente las filas no están formadas. Cuando los campos son añadidos a la columna
     * con sus índices de fila correspondiente, este método prepara las filas y
     * las almacena en el vector correspondiente ({@link #m_vRowList}).
     * Cada elemento del vector será del tipo GRow.
     */
    public void prepareRows()
    {
        Enumeration en = m_vFieldList.elements();
        GRow lastRow=null;
        int lastRowIndex = -1;
        while(en.hasMoreElements())
        {
            GProcessedField fld = (GProcessedField)en.nextElement();
            if(fld.getRow()>lastRowIndex)
            {
            	GRow row = new GRow(fld);
                m_vRowList.addElement(row);
                lastRow = row;
            }
            else
            {
                if(lastRow!=null)
                {
                	lastRow.addField(fld);
                }
            }
            lastRowIndex = fld.getRow();
        }
    }

    /**
     * Este método es usado para mejorar la posición y tamaño de los campos de una columna.
     * <br>Si la columna tiene más de una subcolumna, se llevará a cabo la alineación de estas subcolumnas,
     * con respecto a la posición de los campos.
     * Como procesamiento final se alinearán todos los campos del formulario, para ajustarlos al margen derecho de la columna
     * y si tienen subcolumnas también se ajustarán los campos de cada subcolumna a su margen derecho (si es posible).
     * Para llevar a cabo todo esto:
     * <ol>
     *  <li>Calculo el máximo de subcolumnas que alberga cualquier fila de la columna.</li>
     *  <li>Si hay filas con mas de una subcolumna
     *  	<ol>
     *		<li>Voy trazando por subcolumnas (iterando desde subcolumna 1 hasta max)</li>
     *      <li>Añado todas las filas que contienen como mínimo ese número de subcolumnas (según cada iteración).</li>
     *      <li>Trato todas las filas que he calculado anteriormente, con el método {@link #processRowsSymmetric(IViewBalancer, Vector, int)}</li>
     *  	</ol>
     *  </li>
     *  <li>Ajusto(alineo) los componentes de todos los campos , tanto si tienen subcolumnas como si no, llamando al método {@link #alineaElementos()}</li>
     *  </ol>
     *  
     * @param balancer Este parámetro nos ayuda en la obtención de los márgenes del formulario.
     */
    public void fineTune(IViewBalancer balancer)
    {
    	/*
    	 * Modificamos el ancho de la columna si contiene algún campo cuyo ancho es mayor que el de la columna.
    	 * Esto puede ocurrir si el campo es del tipo tabla, que se ha podido agrandar su ancho un 25% y no se ha modificado el ancho de la columna.
    	 */
    	Enumeration en2 = m_vFieldList.elements();
    	while(en2.hasMoreElements()){
    		GProcessedField t = (GProcessedField)en2.nextElement();
    		if(t.getBounds().width>m_iColWidth)
    			m_iColWidth=t.getBounds().width;
    	}
    	
    	//calculamos el maximo de subcolumnas que contiene una fila de la columna
    	int maxSubCol = 0;
    	for(int i=0; i<m_vFieldList.size();i++){
    		GProcessedField t = m_vFieldList.get(i);
    		if(t.m_iSubColumn>maxSubCol)
    			maxSubCol=t.m_iSubColumn;
    	}
    	//Si hay filas que tienen mas de una subcolumna
    	if(maxSubCol>0){
    		//Recorro la columna por subcolumnas
    		for(int j=1; j<=maxSubCol; j++){
    			Vector<GRow> vSubCol = new Vector<GRow>();
    			for(int z=0; z<m_vRowList.size();z++){
    				GRow row = (GRow)m_vRowList.get(z);
    				//Si una fila tiene como minimo ese numero de subcolumnas
    				if(row.getFieldCount()>=j+1){
    					//añado la fila al vector que despues va a ser tratado.
    					vSubCol.add(row);
    				}
    			}
    			//Si he añadido mas de una fila al vector
    			if(vSubCol.size()>1)
    				//tratamos todas las filas añadidas al vector
					processRowsSymmetric(balancer, vSubCol, j+1);
    		}
    	}
    	 //Despues de haber configurado todas las columnas lo mejor posible, ajusto los campos al margen derecho posible
      alineaElementos();

    	/*
        Enumeration en = m_vRowList.elements();
        Vector vRowCollection = new Vector();
        int prevFieldCount = -1;
        while(en.hasMoreElements())
        {
            GRow row = (GRow)en.nextElement();
            Vector vFldList = row.getFieldList();
            int fieldCount = vFldList.size();
            //This does not apply when only one field is there in the row
            if(fieldCount<=1)
                continue;
            //first get the rows with equal number of fields
            if(prevFieldCount!=fieldCount)
            {
            	processRowsSymmetric(balancer,vRowCollection,prevFieldCount);
                vRowCollection = new Vector();
            }
            prevFieldCount = fieldCount;
            vRowCollection.addElement(row);
        }
        //at last one more

        processRowsSymmetric(balancer,vRowCollection,prevFieldCount);*/
        
        //one more iteration to put them properly aligned
/*        en = m_vRowList.elements();
        while(en.hasMoreElements())
        {
            GRow row = (GRow)en.nextElement();
            Vector vFldList = row.getFieldList();
            int fieldCount = vFldList.size();
            //This does not apply when only one field is there in the row
            if(fieldCount<=1)
                continue;
        }
        alineaElementos();
        */
    }

    /**
     * Este metodo sirve para alinear los componentes de los campos (las etiquetas ya estan alineadas y normalizadas)
     * <br>Alineará cada subcolumna con respecto al margen derecho mayor de la subcoluma (si se puede).
     * <br>La última subcolumna (o la única si tiene 1 sola subcolumna) la alineará con respecto al margen derecho de la columna total.
     * <br>El procedimiento a seguir es el siguiente:
     * <ol>
     * <li>Calculo todos los campos en columnas intermedias (desprecio los de la derecha del todo) y el numero máximo de subcolumnas</li>
     * <li>Trato los elementos obtenidos anteriormente de las columnas intermedias, con el método {@link #alineaElementosIntermedio(Vector, int)}.</li>
     * <li>Una vez tratados estos elementos, calculo el margen derecho de la columna total y todos los campos que están a la derecha del todo (si hay una única subcolumna también los incluiremos) </li>
     * <li>Trato este conjunto de campos obtenidos llamando al método {@link #alineaSubColumna(Vector, int)} con el vector y el margen derecho obtenido. </li>
     * </ol>
     * 
     */
    
    protected void alineaElementos(){
    	GProcessedField campoAnt=null;
    	Vector<GProcessedField> vCampos = new Vector<GProcessedField>();
    	Vector<GProcessedField> vFilasGrandes = new Vector<GProcessedField>();

    	int margenDcho=0;
    	int maxSubColumn=0;

    	//Tratamiento del primer elemento
    	if(m_vFieldList.size()>0){
    		campoAnt = (GProcessedField)m_vFieldList.get(0);
    		margenDcho = campoAnt.getBounds().width + campoAnt.getBounds().x;
    	}

    	//Bucle para obtener los elementos intermedios en columnas con 3 o mas subcolumnas
    	for (int i=0; i<m_vFieldList.size(); i++){
    		GProcessedField  campo = (GProcessedField)m_vFieldList.get(i);

    		if (campo.m_iSubColumn>0){
    			vFilasGrandes.add(campoAnt);
    			if (campo.m_iSubColumn>maxSubColumn)
    				maxSubColumn=campo.m_iSubColumn;
    		}
    		campoAnt=campo;
    	}
    	alineaElementosIntermedio(vFilasGrandes, maxSubColumn);

    	//Bucle para obtener los campos de la derecha de la columna
    	//Recorro todos los campos de la columna, comparandolo con el campo anterior
    	//para ver si hay varios campos en la misma fila (varias subcolumnas)
    	for (int i=0; i<m_vFieldList.size(); i++){
    		GProcessedField  campo = (GProcessedField)m_vFieldList.get(i);

    		//Si cambiamos de fila estamos ante un elemento del borde derecho, y lo añadimos
    		if (campoAnt.m_iRow!=campo.m_iRow)
    			vCampos.add(campoAnt);

    		//añado el ultimo elemento siempre (ya que estara siempre en el borde derecho)
    		if (i==m_vFieldList.size()-1){
    			vCampos.add(campo);
    		}
    		//Una vez reccorridas todos los campos ya se cual es el borde maximo
    		if(campo.getBounds().width + campo.getBounds().x >margenDcho)
    			margenDcho=campo.getBounds().width + campo.getBounds().x;
    		//Actualizo el iterador
    		campoAnt=campo;
    	}
    	alineaSubColumna(vCampos, margenDcho);
    	
    	
    }

    /**
     * Este metodo alinea los campos por subcolumnas, intentando ajustar todos los campos
     * de una misma subcolumna al margen derecho mayor de la subcolumna.
     * <br>Separo entre campos con orden y sin orden, ya que los que tienen un orden preestablecido
     * pueden tener tamaños muy diferentes y ajustarse a esos márgenes es muy difícil, por lo que
     * solo intentare ajustarme a estos margenes con el grupo de campos con orden.
     * Los campos que NO tienen orden intentaremos alinearlos entre ellos, sin tener en cuenta los que tienen orden.
     * <p>
     * El procedimiento que seguimos es el siguiente:
     * <ol>
     * <li>Iteramos desde la primera subcolumna (subcolumna 0) hasta la penúltima(maxSubColumn-1), ya que la ultima columna la trato con el método AlineaElementosDerecha</li>
     * <li>Por cada subcolumna haremos lo siguiente:
     * 		<ol>
     * 		<li>Calculamos todos los elementos de esa subcolumna</li>
     * 		<li>Separamos en 2 vectores elementos con orden y sin orden</li>
     * 		<li>A la vez que ésto, para ahorrar iteraciones, calculamos el máximo margen derecho de la subcolumna</li>
     * 		<li>Alineamos la subcolumna (dividida en 2, elementos con orden y sin orden) llamando al método {@link #alineaSubColumna(Vector, int)} dos veces(elementos con orden y elementos sin orden)</li>
     * 		</ol>
     * </li>
     * </ol>
     * </p>
     * @param vFilas contiene todos los elementos intermedios (que no estan en el margen derecho) de una misma columna.
     * @param maxSubColumn es el indice de la subcolumna máxima que puede tener una fila de esa columna
     */
    protected void alineaElementosIntermedio (Vector vFilas, int maxSubColumn){

    	Vector<GProcessedField> vSubColumna = new Vector<GProcessedField>(); //Vector con elementos que tiene orden preestablecido
    	Vector<GProcessedField> vSubColumnaSinOrden = new Vector<GProcessedField>(); //Elementos que no tienen orden preestablecido
    	GProcessedField temp = null;
    	int maxMargenDerecho = 0;
    	int maxMargenDerechoSinOrden = 0;


    	//j<maxSubColumn porque la columna de la derecha tampoco me interesa (la trato con otro metodo)
    	for (int j=0; j<maxSubColumn; j++){
    		//bucle para recorrer todos los elementos intermedios
    		for (int i=0; i<vFilas.size(); i++){
    			temp = (GProcessedField)vFilas.get(i);
    			//miro todos los elementos que tengan un mismo indice de subcolumna
    			if ( temp.m_iSubColumn == j && temp.getFormField().getOrder()!=0){
    				//añado al vector los que tienen orden (y misma subcolumna)
    				vSubColumna.add(temp);
    				//Aprovecho el recorrido para obtener el maximo margen derecho
    				if ( (temp.getBounds().x + temp.getBounds().width) >maxMargenDerecho)
    					maxMargenDerecho = temp.getBounds().x + temp.getBounds().width;
    			}
    			else
    				if(temp.m_iSubColumn == j && temp.getFormField().getOrder()==0){
    					//añado los que no tienen orden
    					vSubColumnaSinOrden.add(temp);
    					if ( (temp.getBounds().x + temp.getBounds().width) >maxMargenDerechoSinOrden)
    						maxMargenDerechoSinOrden = temp.getBounds().x + temp.getBounds().width;
    				}

    		}
    		//Alineo la subcolumna obtenida
    		alineaSubColumna(vSubColumna, maxMargenDerecho);
    		alineaSubColumna(vSubColumnaSinOrden, maxMargenDerechoSinOrden);

    		//Borro todos los elementos para hacer una nueva iteracion y obtener la siguiente subcolumna
    		vSubColumna.removeAllElements();
    		vSubColumnaSinOrden.removeAllElements();

    	}

    }

    /**
     * Este metodo sirve para alinear el borde derecho de los componentes de los campos 
     * que están más a la derecha de la columna. 
     * Si sólo hay un campo en la fila, también estarán incluidos en este tratamiento.
     * Si el campo es un CheckBox no hace nada, ya que el CheckBox no tiene sentido agrandarlo,
     * y si movemos su posición quedará desalineado por el borde izquierdo.
     * <p>
     * El procedimiento a seguir es el siguiente:
     * <ol>
     * <li>Iteraremos sobre todos los elementos del vector que nos pasan (vSubColumna)</li>
     * <li>Para cada elemento:</li>
     * 		<ol>
     * 		<li>Si es un CheckBox no hacemos nada.
     * 		<li>Si no es un CheckBox, calculamos la diferencia entre el borde derecho de la subcolumna y el borde derecho del componente. Si la subcolumna es la que está más a la derecha, el borde derecho de la subcolumna coincidira con el borde derecho total de la columna.</li>
     * 		<li>Comprobamos si el incremento es posible, con el método {@link #esIncrementoPosible(int, int)} donde le pasamos el incremento que queremos realizar y la fila donde lo queremos realizar.</li>
     * 		<li>Si podemos realizar el incremento:			
     * 			<ol>		
     * 			<li>Hacemos el incremento llamando al método {@link #modificaBordesCampo(GProcessedField, int)}, pasandole el campo a modificar y el incremento</li>
     * 			<li>Incrementamos (con el mismo incremento) todos los campos que están a la derecha del campo incrementado, llamando al método {@link #incrementaCamposFila(int, int, int)}, con el incremento, la fila y la subcolumna a partir de la cual hay que incrementar.</li>
     * 			</ol>
     * 		<li>Si no es posible el incremento, no hacemos nada</li>
     * 		</ol>
     * </ol>
     * </p>
     * @param vSubColumna es el vector donde están los campos que vamos a intentar alinear al margen derecho.
     * @param max Es el borde maximo derecho, donde hay que alinear.
     */
    protected void alineaSubColumna (Vector vSubColumna,  int max){

    	int incremento;
    	GProcessedField field = null;

    	// Recorro todo el vector
    	for (int i=0; i<vSubColumna.size(); i++){
    		field = (GProcessedField)vSubColumna.get(i);
    		//Calculo el incremento del campo para ajustarse al margen derecho maximo
    	/*	incremento = max - field.getBounds().x - field.getComponentBounds().width /*- campo.getLabelBounds().width - campo.getLabelBounds().x - campo.getComponentSecundarioBounds().width*/;
		/*	if(!field.getFormField().isTopLabel())
				incremento = incremento - field.getLabelBounds().width - field.getLabelBounds().x - field.getComponentSecundarioBounds().width;
    	*/	incremento= max -field.getBounds().x - field.getBounds().width/* - GConfigView.HCellPad*/;

    		//Si es posible el incremento, modifico los bordes y desplazo las etiquetas de la misma fila
    		if(esIncrementoPosible(incremento, field.m_iRow)){
    			if(modificaBordesCampo(field, incremento)){
    				incrementaCamposFila(incremento, field.m_iRow, field.m_iSubColumn+1);
    			}
    		}
    	}
    }

    /**
     * Este metodo se utiliza para incrementar el borde del componente de un campo.
     * A este metodo se llamará solo si el incremento es posible, por lo que no se comprueba en este método.
     * <p>
     * El método es muy sencillo. 
     * <ul><li>Realiza el incremento siempre y cuando sea mayor que 0.
     * <li>Primero incremento el ancho total del campo.</li>
     * <li>Si el campo tiene componente secundario y no es una tabla, incrementa el componente secundario. Se hace la distinción de que sea tabla porque si no incrementaria la botonera de las tablas y no nos interesa.</li>
     * <li>Si no tiene componente secundario, o es una tabla, incremento el primer componente y desplazo el componente secundario (si no tiene componente secundario no afecta el desplazamiento).</li>
     * </ul></p>
     * @param field es el campo a modificar
     * @param incremento es el incremento a realizar
     */
    private boolean modificaBordesCampo (GProcessedField field, int incremento){

    	Rectangle bound = field.getBounds();
    	Rectangle componentBound =field.getComponentBounds();
    	Rectangle componentBound2 = field.getComponentSecundarioBounds();

    	Class typeField=field.getFormField().getClass();
    	if(incremento > 0){
    		
    		//Si es un enumerado solo lo aumentamos si este es menor al doble del tamaño actual, ya que para los enumerados no tiene ningun sentido practico agrandarlo, apareciendo poco vistoso cuando esto ocurre
    		if(typeField==GFormEnumerated.class && incremento>componentBound.width*2){
    			return false;
    		}
    		
    		bound.width += incremento;
    		
    		//Si el elemento fuera un CheckBoxComment hay que agrandar el ancho del componente secundario
    		if(componentBound2.width>0 && typeField!=GFormTable.class && typeField!=GFormDate.class && typeField!=GFormDateHour.class && typeField!=GFormFile.class && typeField!=GFormImage.class)
    			componentBound2.width+=incremento;
    		else if(typeField!=GFormBoolean.class && typeField!=GFormHour.class && (typeField!=GFormImage.class || field.getFormField().isMultivalued())){
    			componentBound.width += incremento;
    			componentBound2.x+=incremento;
    		}
    		field.setBounds(bound);
    		field.setComponentBounds(componentBound);
    		field.setComponentSecundarioBounds(componentBound2);
    	}
    	
    	return true;

    }

    /**
     * Este método verifica si un incremento se puede llevar a cabo en una fila,
     * sin salirse de los margenes de la columna en la que está ubicado.
     * El procedimiento es muy sencillo. Suma los bordes totales de los campos, más el espacio intermedio, más el incremento que queremos realizar, y si no se supera el ancho de la columna es que el incremento es posible.
     * @param inc es el incremento que queremos realizar
     * @param row es la fila donde se encuentra el campo
     * @return boolean - Devolverá "true" si se puede incrementar ("false" en caso contrario").
     */
    private boolean esIncrementoPosible(int inc, int row){

    	boolean res=false;
    	int bordesComponentes=0;
    	//Obtenemos todos los campos de la fila a tratar 
    	Vector v = ((GRow)m_vRowList.get(row)).getFieldList();
    	
    	for(int i=0; i<v.size(); i++)
    		//Sumamos todos los bordes de los campos que hay en la fila
    		bordesComponentes += ((GProcessedField)v.get(i)).getBounds().width;
    	//Sumamos al final, el espacio entre campos del grupo y el margen Derecho a la siguiente columna o Panel
    	bordesComponentes += GConfigView.GroupHGap * (v.size()-1) - GConfigView.GroupRightMargin;

    	//Si la suma total es menor que el ancho total de la columna, el incremento se puede realizar
    	if(bordesComponentes+inc <= m_iColWidth- GConfigView.GroupRightMargin)
    		res=true;

    	return res;
    }
    
    /**
     * Una vez que se ha incrementado un campo, tengo que desplazar todos los campos que hay en la fila a su derecha con el mismo incremento realizado al campo.
     * <br>Por tanto la única función de este método será recorrer todos los campos a partir de la subcolumna siguiente al campo incrementado y realizar el desplazamiento mencionado.
     * @param inc es el incremento que he realizado al campo
     * @param row es la fila donde se encuentra el campo
     * @param subcolumna es la subcolumna a partir de la cual tengo que desplazar (la siguiente al campo incrementado)
     * 
     */
    private void incrementaCamposFila(int inc, int row, int subcolumna){

    	Rectangle r;
    	//Obtengo todos los campos de la fila
    	Vector v = ((GRow)m_vRowList.get(row)).getFieldList();

    	for(int i= subcolumna; i<v.size(); i++){
    			r = ((GProcessedField)v.get(i)).getBounds();
    			r.x+=inc;
    			((GProcessedField)v.get(i)).setBounds(r);
    	}

    }
    
    /**
     * Este método procesará las filas que tienen más de una subcolumna e intentará alinearlas.
     * El procedimiento que sigue el algoritmo para realizar la alineación es parecido al de alineaElementos(), 
     * pero esta vez modificará las posiciones de los campos y el ancho de las etiquetas, 
     * no el ancho del componente.
     * <p>El procedimiento que realiza es el siguiente:
     * <ol>
     * <li>Va iterando por subcolumnas, desde la segunda (subcolum=1) hasta maxSubColumn. La primera no la tiene en cuenta porque ya está normalizada por GViewBalancer</li>
     * <li>Si los campos de la subcolumna no tienen orden, normalizo la posición del campo con {@link #normalizeX(IViewBalancer, Vector, Vector, GProcessedField)} y el ancho de la etiqueta con {@link #normalizeLabels(IViewBalancer, Vector, Vector, GProcessedField)}</li>
     * <li>Si los campos de la subcolumna si tienen orden, divido la subcolumna en 2 vectores de campos con orden y sin orden, y llamo con cada uno de ellos a {@link #normalizeX(IViewBalancer, Vector, Vector, GProcessedField)} y {@link #normalizeLabels(IViewBalancer, Vector, Vector, GProcessedField)}</li>
     * </ol>
     * </p>
     * @param balancer Nos sirve para obtener todos los márgenes establecidos para el formulario, grupos, campos, etc.
     * @param vRowList Contiene las filas que tienen más de una subcolumna. Cada elemento es del tipo GRow.
     * @param maxSubColumn Es el número máximo de subcolumnas que tiene la columna.
     * @see GViewBalancer#normalizeLabels(GGrpColumn)
     */
    private void processRowsSymmetric(IViewBalancer balancer,Vector vRowList,int maxSubColumn)
    {
        for (int i = 1; i < maxSubColumn; i++)
        {
        	Vector vFieldList = getFields(vRowList,i);
        	
        	//Miro si el vector incluye un orden preestablecido y en ese caso lo divido en dos vectores
        	Enumeration en = vFieldList.elements();
        	boolean orden = false;
        	while(en.hasMoreElements() && !orden){
        		GProcessedField temp = (GProcessedField)en.nextElement();
        		if(temp.getFormField().getOrder()!=0)
        			orden=true;
        	}
        	if(orden){
        		Vector<GRow> vRowOrden=new Vector<GRow>();
        		Vector<GRow> vRowSinOrden=new Vector<GRow>();
        		Vector<GProcessedField> vOrden=new Vector<GProcessedField>();
        		Vector<GProcessedField> vSinOrden = new Vector<GProcessedField>();
        		for(int j=0;j<vFieldList.size();j++){
        			GProcessedField it = (GProcessedField)vFieldList.get(j);
        			if( it.getFormField().getOrder()==0 ){
        				vSinOrden.add(it);
        				vRowSinOrden.add(m_vRowList.get(it.m_iRow));
        			}
        			else{
        				vOrden.add(it);
        				vRowOrden.add(m_vRowList.get(it.m_iRow));
        			}
        		}
        		if(vSinOrden.size()>0){
        			normalizeX(balancer,vRowSinOrden,vSinOrden, getMaxLabelX(vSinOrden));
        			normalizeLabels(balancer,vRowSinOrden,vSinOrden,getMaxLabelWidth(vSinOrden));
        		}
            	normalizeX(balancer,vRowOrden,vOrden, getMaxLabelX(vOrden));
            	normalizeLabels(balancer,vRowOrden,vOrden,getMaxLabelWidth(vOrden));
        		
        	}
        	else{
        	normalizeX(balancer,vRowList,vFieldList, getMaxLabelX(vFieldList));
        	normalizeLabels(balancer,vRowList,vFieldList,getMaxLabelWidth(vFieldList));
        	}
        }
    }
    /**
     * Este metodo devuelve todos los campos del vector RowList cuya subcolumna es el parámetro subCol.
     * @param subCol Es la subcolumna de la que quiero obtener los campos
     * @param vRowList Es un vector (cuyos elementos son del tipo GRow) que contiene las filas con la subcolumna que me interesa (subCol).
     * @return Vector - Devuelve un vector que contiene los campos de la subcolumna subCol.
     */
    private Vector getFields(Vector vRowList,int subCol)
    {
        Vector<GProcessedField> v = new Vector<GProcessedField>();
        Enumeration en = vRowList.elements();
        while(en.hasMoreElements())
        {
            GRow row = (GRow)en.nextElement();
            Vector vFldList = row.getFieldList();
            v.addElement((GProcessedField) vFldList.elementAt(subCol));
        }
        return v;
    }
    
    /**
     * Este método devuelve el campo que contiene la etiqueta de mayor ancho, para el vector que me han pasado vFieldList.
     * @param vFieldList Es el vector que contiene las etiquetas que queremos comparar y sacar la de mayor anchura.
     * @return GProcessedField Devuelve el campo con la etiqueta de mayor ancho.
     */
    private GProcessedField getMaxLabelWidth(Vector vFieldList)
    {
        Enumeration en = vFieldList.elements();
        GProcessedField etiq = null;
        int maxLabWidth = 0;
        while(en.hasMoreElements())
        {
            GProcessedField fld = (GProcessedField)en.nextElement();
            int labWid = fld.getLabelBounds().width;
            if(labWid>maxLabWidth){
                maxLabWidth = labWid;
                etiq=fld;
            }
        }
        return etiq;
    }

    /**
     * Este método devuelve el campo que está mas a la derecha (su posición x es mayor), para el vector que me han pasado vFieldList.
     * @param vFieldList Es el vector que contiene los campos que queremos comparar.
     * @return GProcessedField - Devuelve el campo con la posición más a la derecha.
     */
    private GProcessedField getMaxLabelX(Vector vFieldList)
    {
        Enumeration en = vFieldList.elements();
        GProcessedField etiq = null;
        int maxLabX = 0;
        while(en.hasMoreElements())
        {
            GProcessedField fld = (GProcessedField)en.nextElement();
            int labX = fld.getBounds().x;
            if(labX>maxLabX){
                maxLabX = labX;
                etiq=fld;
            }
        }
        return etiq;
    }
    
    /**
     * Este método intenta ajustar todos los campos del vector vFieldList al campo que se pasa como parámetro campoX.
     * Si no puede ajustarlo, porque no tiene espacio, deja el campo donde está y no hace nada.
     * 
     * <p>
     * El procedimiento que sigue el método para realizar esta función es:
     * <ol>
     * <li>Calcula la posición X del campo que se nos pasa como parámetro (campoX), que es la X a la que queremos ajustarnos (la que está más a la derecha de la columna que queremos alinear).</li>
     * <li>Recorro todo el vector que contiene los campos a tratar (los campos de la subcolumna que quiero alinear).</li>
     * <li>Para cada elemento del vector hacemos:
     * 		<ol>
     * 		<li>Obtenemos la posición del elemento</li>
     * 		<li>Calculamos el desplazamiento a realizar con respecto a la posición de campoX</li>
     * 		<li>Comprobamos si el incremento es mayor que 0 y si lo podemos realizar para esa fila (con el método {@link #isEnoughSpaceAvailable(IViewBalancer, Vector, int)})</li>
     * 		<li>Si podemos realizar el incremento, lo realizamos, y además habrá que agrandar el componente del campo de la columna anterior y desplazar los campos de las siguientes subcolumnas al campo desplazado (con el método {@link #shiftFields(Vector, int, int)})</li>
     * 		<li>Si no podemos realizar el incremento no hacemos nada.
     * 		</ol></li>
     * </ol>
     * </p>
     * 
     * @param balancer Nos sirve para obtener todos los márgenes establecidos para el formulario, grupos, campos, etc.
     * @param vRowList Es el vector que contiene las filas de los campos que vamos a tratar. 
     * @param vFieldList Contiene los campos que queremos tratar.
     * @param campoX Es el campo que está mas a la derecha del vector VFieldList (donde nos queremos alinear).
     * 
     */
    private void normalizeX(IViewBalancer balancer,Vector vRowList,Vector vFieldList,GProcessedField  campoX)
    {
    	int maxX = campoX.getBounds().x;
    	for(int i = 0;i<vFieldList.size();i++)
    	{
    		GProcessedField pField = (GProcessedField)vFieldList.elementAt(i);
    		Rectangle rcBounds = pField.getBounds();

    		int displacement = maxX - rcBounds.x;

    		Vector vFieldsInRow = ((GRow)vRowList.elementAt(i)).getFieldList();
    		if( displacement>0 && isEnoughSpaceAvailable(balancer,vFieldsInRow,displacement))
    		{
    			rcBounds.x=maxX;

    			//La etiqueta anterior hay que agrandarla
    			GProcessedField t = (GProcessedField)vFieldsInRow.get(pField.getSubColumn()-1);
    			Rectangle rc = t.getBounds();
    			Rectangle rcComp = t.getComponentBounds();
    			Rectangle rcCompSec = t.getComponentSecundarioBounds();

    			rc.width += displacement;
    			Class typeField=t.getFormField().getClass();
    			if (typeField == GFormBooleanComment.class)
    				rcCompSec.width += displacement;
    			else if(typeField!=GFormBoolean.class && typeField!=GFormHour.class){
    				rcComp.width += displacement;
    				rcCompSec.x += displacement;
    			}
    			t.setBounds(rc);
    			t.setComponentBounds(rcComp);
    			t.setComponentSecundarioBounds(rcCompSec);

    			pField.setBounds(rcBounds);
    			shiftFields(vFieldsInRow, pField.getSubColumn() + 1,displacement);
    		}
    	}

    }

    /**
     * Este método normaliza las etiquetas de los campos que se nos pasan en el vector vFieldList. 
     * El ancho de la etiqueta a normalizar se nos pasa como parámetro, y es el ancho de la etiqueta del camnpo etiqWidth.
     * Normalmente, en el Vector vFieldList estarán todos los campos de una misma subcolumna que queremos que el ancho de las etiquetas sea igual para todos los campos, y así queden alineados.
     * 
     * <p>
     * El procedimiento que se sigue en éste método para normalizar las etiquetas es el siguiente:
     * <ol>
     * <li>Obtenemos el acnho de la etiqueta sobre la que queremos normalizar</li>
     * <li>Iteramos sobre el vector vFieldList y para cada elemtento ({@link GProcessedField}) hacemos lo siguiente:
     * 		<ol>
     * 		<li>Obtenemos los bordes del campo, y de la etiqueta y los componentes de dicho campo.</li>
     * 		<li>Calculamos el desplazamiento (el ancho de la etiqueta máxima menos el ancho del campo actual sobre el que iteramos)</li>
     * 		<li>Comprobamos si es posible el desplazamiento (con el método {@link #isEnoughSpaceAvailable(IViewBalancer, Vector, int)})</li>
     * 		<li>Si es posible el desplazamiento:
     * 			<ul>
     * 			<li>Aumentamos el ancho total del campo</li>
     * 			<li>Aumentamos el ancho de la etiqueta</li>
     * 			<li>Desplazamos hacia la derecha los componentes del campo</li>
     * 			<li>Desplazamos hacia la derecha todos los campos de las siguientes subcolumnas (con el método {@link #shiftFields(Vector, int, int)})</li>
     * 			</ul></li>
     * 		<li>Si no es posible el desplazamiento no hacemos nada.</li>
     *      </ol></li>
     * </ol>
     * </p>
     * 
     * 
     * @param balancer Nos sirve para obtener todos los márgenes establecidos para el formulario, grupos, campos, etc.
     * @param vRowList Es el vector que contiene las filas de los campos que vamos a tratar.
     * @param vFieldList Contiene los campos que queremos tratar.
     * @param etiqWidth Es el campo que tiene la etiqueta de mayor ancho (sobre la que hay que normalizar).
     * 
     */
    private void normalizeLabels(IViewBalancer balancer,Vector vRowList,Vector vFieldList,GProcessedField  etiqWidth)
    {
    	int maxLabelWidth = etiqWidth.getLabelBounds().width;
    	for(int i = 0;i<vFieldList.size();i++)
    	{
    		GProcessedField pField = (GProcessedField)vFieldList.elementAt(i);
    		Rectangle rcBounds = pField.getBounds();
    		Rectangle rcLabel = pField.getLabelBounds();
    		Rectangle rcComponent = pField.getComponentBounds();
    		Rectangle rcCompSecBounds = pField.getComponentSecundarioBounds();
    		if(pField.getFormField().isTopLabel())
    			continue;
    		int diferencia = etiqWidth.getBounds().x - rcBounds.x;
    		int displacement = maxLabelWidth - rcLabel.width + diferencia;

    		Vector vFieldsInRow = ((GRow)vRowList.elementAt(i)).getFieldList();
    		if(isEnoughSpaceAvailable(balancer,vFieldsInRow,displacement) && displacement>0)
    		{
    			/*if (diferencia>0 && displacement>=diferencia && esIncrementoPosible(diferencia, pField.m_iRow)){
    				rcBounds.x=etiqWidth.getBounds().x;
    				//El desplazamiento sera menor porque hemos movido la etiqueta hacia la derecha
    				displacement -= diferencia; 

    				//La etiqueta anterior hay que agrandarla
    				GProcessedField t = (GProcessedField)vFieldsInRow.get(pField.getSubColumn()-1);
    				Rectangle rc = t.getBounds();
    				Rectangle rcComp = t.getComponentBounds();
    				Rectangle rcCompSec = t.getComponentSecundarioBounds();

    				rc.width += diferencia;
    				if (t.getFormField().getClass() == GFormBooleanComment.class)
    					rcCompSec.width += diferencia;
    				else{
    					rcComp.width += diferencia;
    					rcCompSec.x += diferencia;
    				}
    				t.setBounds(rc);
    				t.setComponentBounds(rcComp);
    				t.setComponentSecundarioBounds(rcCompSec);
    			}*/
    			rcBounds.width += displacement;
    			rcCompSecBounds.x +=displacement;
    			rcLabel.width += displacement;
    			rcComponent.x += displacement;
    			pField.setBounds(rcBounds);
    			pField.setLabelBounds(rcLabel);
    			pField.setComponentBounds(rcComponent);
    			pField.setComponentSecundarioBounds(rcCompSecBounds);
    			shiftFields(vFieldsInRow, pField.getSubColumn() + 1,displacement);
    			
    		}
    	}
    }
    /**
     * Este método desplaza todos los campos del vector vFieldList hacia la derecha, comenzando en el índice startFrom.
     * Normalmente el vector vFieldList contiene los campos de una fila. 
     * Este método se invoca desde normalizeX o normalizeLabels para que desplaze todos los campos de la derecha del campo incrementado. startFrom suele ser el índice de la siguiente subcolumna al campo incrementado.  
     * Es similar a {@link #incrementaCamposFila(int, int, int)} pero con otros parámetros, así que no lo explicaremos. 
     * Se podrían unificar en un sólo método, pero no nos interesa, porque según los parámetros que tenemos nos interesará llamar a uno u otro.
     * @param vFieldList Este vector contiene la fila de los campos que vamos a desplazar.
     * @param startFrom Índice a partir de qué campo vamos a desplazar.
     * @param displacement El desplazamiento a llevar a cabo.
     * 
     */
    private void shiftFields(Vector vFieldList, int startFrom,int displacement)
    {
        if(displacement==0)
            return;
        int size = vFieldList.size();
        for(int i=startFrom;i<size;i++)
        {
            GProcessedField pField = (GProcessedField)vFieldList.elementAt(i);
            /*if(pField.getFormField().isTopLabel())
                continue;*/
            Rectangle rcBounds = pField.getBounds();
            rcBounds.x += displacement;
            pField.setBounds(rcBounds);
        }
    }
    /**
     * Este método determina si es posible el desplazamiento sobre la fila que nos pasan (vFieldList). 
     * Es similar a {@link #esIncrementoPosible(int, int)} pero con otros parámetros, y no nos interesa unificarlos.
     * @param balancer Nos sirve para obtener todos los márgenes establecidos para el formulario, grupos, campos, etc.
     * @param vFieldList Contiene los campos de la fila sobre la que queremos ver si el incremento es posible.
     * @param displacement Es el incremento que queremos llevar a cabo.
     * @return boolean - Devuelve "true" si podemos incrementar o "false" en caso contrario.
     * 
     */
    private boolean isEnoughSpaceAvailable(IViewBalancer balancer,Vector vFieldList,int displacement)
    {
        int size = vFieldList.size();
        int totalWidth = 0;
        for(int i=0;i<size;i++)
        {
            GProcessedField pField = (GProcessedField)vFieldList.elementAt(i);
            totalWidth += pField.getBounds().width;
        }
        //Si es la primera columna hay que tener en cuenta el margen izquierdo del grupo.
        totalWidth += (balancer.getGroupHGap()*(size-1));
        if(m_iColWidth - totalWidth - balancer.getHCellPad() >=displacement)
            return true;
        return false;
    }
}
/**
 * Esta clase representa una fila dentro de un columna.
 * Su atributo será un vector con todos los campos que tiene esa fila.
 * El número de campos que tiene el vector es igual al número de subcolumnas que tiene la fila.
 */
class GRow
{
    /**
     * Este vector contiene todos los campos de la fila.
     * El número de campos es igual al número de subcolumnas.
     */
    protected Vector<GProcessedField> m_vFieldList = new Vector<GProcessedField>();

    /**
     * Constructor
     * @param fld GProcessedField Es el primer elemento de la fila.
     */
    public GRow(GProcessedField fld)
    {
        m_vFieldList.addElement(fld);
    }
    /**
     * Este método devuelve el índice de la fila. (La primera fila tendrá el índice 0).
     * @return int - Es el índice que devuelve.
     */
    public int getRowIndex()
    {
        return ((GProcessedField)m_vFieldList.elementAt(0)).getRow();
    }
    /**
     * Este método añade un campo a la fila.
     * @param fld Es el campo a añadir a la fila.
     */
    public void addField(GProcessedField fld)
    {
        m_vFieldList.addElement(fld);
    }
    /**
     * This method returns the field list of this row.
     * @return Vector - Devuelve el vector con todos los campos de la fila.
     */
    public Vector<GProcessedField> getFieldList()
    {
        return m_vFieldList;
    }
    /**
     * This method returns the total number of fields present in the row.
     * @return int - Devuelve el número de campos de la fila
     */
    public int getFieldCount()
    {
        return m_vFieldList.size();
    }
    
    /**
     * Nos calcula la altura máxima de la fila, dependiendo de los campos que contiene.
     * @return int - La altura de la columna.
     */
    public int getHeightRow(){
    	int height=0, max=0;
    	for (int i=0; i<m_vFieldList.size();i++){
    		height=((GProcessedField)m_vFieldList.get(i)).getBounds().height;
    		if(height>max)
    			max=height;
    	}
    	return height;
    }
}
