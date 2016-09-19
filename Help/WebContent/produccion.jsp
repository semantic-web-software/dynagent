<h2><a name="produccion">Producci�n y fabricaci�n</a></h2>
<p>
Una producci�n transforma stocks, dando de alta nuevos stock del producto elaborado/fabricado a la vez que consume el stock de los componentes que lo integran, enlanzando implicitamente las trazas de lotes implicados si los hubiera.
</p>
<h3>1- Escandallo</h3>
<p>
Un escandallo define las proporciones (o &quot;receta&quot;) de dicha transformaci�n.
</p>
<p>
Si existe un escandallo, dada una salida en una producci�n, el sistema autom�ticamente a�adir� los componentes necesarios o modificar� sus cantidades de acuerdo a la salida deseada en una producci�n, 
as� como, caso de que se trabaje con lotes, asignar� autom�ticamente el lote m�s antiguo, siendo posible modificar la decisi�n del sistema.
</p>
<p>
Un escandallo se define declarando los productos y cantidades de entrada (o componentes) y salida (o l&iacute;neas), no importa que valor concreto o absoluto eligamos como referencia en el campo cantidad para definir el escandallo, ya que el sistema s�lo tendr� en cuenta la proporci�n entrada con salida. Es necesario trabajar con las mismas unidades que las ventas y compras.</br>
Ejemplo, queremos declarar un escandallo de una tarta tal que por cada kilogramo de tarta se consume 500 gramos de harina, y 10gr de canela, siendo el peso de una tarta 1500gr.</br>
<h4>Ejemplo Caso 1.</h4> Supongamos que en nuestras ventas y compras hemos tomado el criterio de vender tartas por unidades (o bultos), y no por kilogramos, y hemos decidido adem�s que compramos harina en kilogramos y canela en gramos.</br>
Creamos un escandallo con una l�nea de salida del producto "tarta" y cantidad=1 (representa una unidad), y dos componentes, harina de cantidad (500/1000)*1,5kg=0,75kg, y canela de cantidad=(10/1000)*1500gr=15gr.</br></br>
Como podemos apreciar, para calcular la harina expresamos el �ltimo termino de cantidad final de tarta en kg (1,5kg), por ser esta la �nidad de referencia de la harina, y en el caso de la canela el �ltimo termino de cantidad tarta (1500gr) lo expresamos en gramos para coincidir con la unidad de referencia de la canela. 
Por tanto la formula de un componente seria:</br>
Cantidad componente= (proporcion componente/proporcion producto salida)*(la cantidad de producto salida del escandallo expresada en las mismas unidades de compra que el componente)</br> 
<h4>Ejemplo Caso 2.</h4> Nuestro criterio es vender tartas "al peso" o por kilogramos, e igualmente compramos harina en kg y canela en gr.</br>
 Creamos un escandallo con una l�nea de salidad de prodcuto "tarta" y cantidad 1 (representa 1kg), y dos componentes: harina de cantidad= 500/1000*1=0,5kg, y canela de 10/1000*1000=10gr.</br></br>
 Como podemos ver en este segundo caso las cantidades coinciden con la "receta". En este segundo caso ser�a igualmente v�lido definir una salida "tarta" de cantidad 2 (representa 2kg), y dos componentes "harina" cantidad=500/1000*2kg=1kg, y "canela" de 10/1000*2000gr=20gr, ya que como hemos dicho en el escandallo s�lo importan las proporniciones, no el valor absoluto.
<h3>2- Produccion material: Fabricaci�n</h3>
<p>
En la producci�n material declaramos en la salida, el producto de salida y cantidad deseada, y el sistema autom�ticamente calcula la entrada necesaria para satisfacer dicha salida, y en caso de haber lotes o n�meros de series y as&iacute; haberse configurado en el men&uacute; &quot;aplicaci&oacute;n&quot;, asignar� stock autom�ticamente.
El origen es el almac�n donde se consume la entrada, y el destino es el almac�n donde se genera el stock de salida (ambos pueden ser el mismo).
</p>
<h3>3- Producci�n en cadena</h3>
<p>
Desde la producci�n en cadena declaramos la necesidad (tambi�n llamada demanda o salida) de productos que queremos fabricar (en la tabla "l�nea"), 
de manera que el sistema genera autom�ticamente las producciones simples necesarias, de forma encadenada, vinculando dichas producciones a la tabla "producci�n".
</p>
<p>
Por ejemplo, si hemos declarado dos escandallos: A) uno que produce en salida ordenador cantidad=1 y tiene como entrada fuente alimentacion=2, y B) que tiene como salida fuente alimentacion y entrada ventilador=2. 
Si en la produccion en cadena declaramos una salida o linea de ordenador cantidad=10, se genera 2 producciones (la 1 de cantidad de salida=10, y la 2 con cantidad de salida=10*2=20).  
</p>
<p style="BACKGROUND-COLOR: Cornsilk">La producci�n en cadena, y todas las producciones simples asociadas, se crean por defecto en estado "Planificado", 
que no modifica stock ni permite asignaci�n auom�tica de stock, lo cual es conveniente si �nicamente se desea planificar la necesidad de materiales y stock existentes. 
Para modificar los stock debemos cambiar el estado de la producci�n en cadena a programado o cualquier otro que no sea planificado.
 El sistema autom�ticamente propagar� dicho estado a cada una de las producciones simples.</p>
<p style="BACKGROUND-COLOR: Cornsilk">El sistema calula los trabajos para produccir completamente las cantidades indicadas en la salida de la producci�n en cadena, 
independientemente de que haya existencias en stock de dicha salida. Sin embargo, para los trabajos encadenados intermedios el sistema si tiene en cuenta las existencias, 
no produciendo lo que ya existe. En el ejemplo anterior, si en stock tenemos 1 ordenador y 1 fuente alimentacion, creara producciones para generar 10 ordenadores (no descuenta 1 por la existencia), 
y una producci�n para generar 19 fuentes de alimentaci�n (20 demandadas menos la 1 existente)</p> 
<h3>4- Modulo lote y campo requiere lote</h3>
El m�dulo lote (entrando a la aplicaci�n en modo "configuraci�n") instala todas las propiedades necesarias para trabajar con lote, y en particular en art�culo "g�nero" aparece la propiedad "requiere lote". Es necesario marcar positivamente (en verde) dicha propiedad para que el sistema obligue a en todos los movimientos (albaranes y producciones) a que se informe el lote, y para que no se generen stock del producto sin lote, es decir, para que el sistema espere a que las l�neas tengan lote antes de modificar stocks.
<h3>5- Configuracion de la automatizaci�n de lotes. Men� "Configuracion > Aplicaci�n"</h3>
<p>
Lotes diferentes deben tener c�digos diferentes. Dado que distintos proveedores pueden proporcionarnos lotes de mismo c�digo, e incluso un mismo proveedor puede codificar distintos productos con el mismo lote, es necesario distinguirlo a�adiendo como prefijo el c�digo de proveedor y el c�digo de producto.
Es posible configurar el sistema para que a�ada autom�ticamente como prefijo (al c&oacute;digo del lote) el c�digo de producto y/o proveedor (ver men� Configuraci�n>Aplicaci�n). Notar que en lotes con formato fecha, la fecha de referencia es la del documento, y no la fecha del d�a actual.
</p>
<p>
En el caso de lotes producidos por nuestra empresa, podemos configurar el sistema para que genere el c�digo del lote autom�ticamente (men&uacute; Aplicacion &gt; Config Lotes, asignando el campo &quot;crear autom&aacute;tico a true o verde&quot;), en formato diario (aammdd:a�o a�o mes mes dia dia,aassd: a�o a�o n�_semana n�_semana dia_semana de 1 a 7 ).
</p>
<p>
Existe un segundo caso en el que interesa generar lotes autom�ticamente: el lote multiproveedor, que es cuando el lote proviene de un proveedor pero carece de lote y se mezcla directamente con otros lotes de proveedor (por ejemplo un ganadero trae directamente la leche que se mezcla en un contenedor com�n).
Tambien es posible configurar la fecha de caducidad o garant�a autom�ticamente.
</p>
<p> 
Todas estas propiedades son configurables por familias, subfamilias o marcas. Podemos configurar lotes de proveedor y lotes propios por separado. Caso de que sea necesario distinguir configuraciones en base a la clasificacion de productos (como la familia), es conveniente trabajar con excepciones o prioridades distintas, con objeto de evitar la necesidad de declarar una configuraci�n para cada clasificaci�n de producto. Por ejemplo, tenemos 10 familias de productos de alimentaci�n, todas ellas trabajan con 3 meses de caducidad, excepto los congelados que trabajan con un a�o. Podemos declarar una configuracion de priodiridad m�nima con caducidad de 3 meses, en la que NO precisamos la familia, es decir, es aplica para cualquier familia, y adicionalmente crear una configuracion de prioridad mayor (por ejemplo prioridad normal) con caducidad 1 a&ntilde;o, donde esta vez fijamos en la configuraci�n la familia congelados. En el caso de producir un congelado, ambas configuraciones son compatibles, pero la configuraci�n para congelados es la que aplica por tener mayor prioridad. En el caso de producir otro tipo de producto, s�lo es compatible la configuraci�n gen�rica, por tanto no hay opci&oacute;n de aplicar la configuraci&oacute;n espec&iacute;fica para congelados.
</p>
<h3>6- Acci�n autom�tica para generar la trazabilidad y la producci�n </h3>

<p>La idea es evitar la din�mica habitual de producci�n en el sistema reemplazandola por una nueva din&aacute;mica basada en las ventas:</p>
<h4>Opci�n 1: Din�mica semiautom�tica (trataremos de evitar esta opci&oacute;n siempre que se pueda aplicar la opci�n 2, con objeto de ahorrar esfuerzos)</h4>
<p>Consistir�a en registrar cada producci�n manualmente, ya sea producciones simples o producciones en cadena. El usuario informa del producto de salida a obtener, y el sistema propone autom�ticamente las cantidades a consumir, y adem�s si la producci�n se encuentra en estado "programado" el sistema asigna autom�ticamente el lote m�s caduco a consumir. El usuario entonces puede supervisar dicha propuesta y en su caso modificar la elecci&oacute;n de  lotes, ya que en algunos casos las cantidades reales de stock no coinciden con las supuestas por el sistema y es necesario modificar la elecci�n de dichos lotes.</p>
<h4>Opci�n 2: Din�mica autom�tica inferida</h4> 
<p>Acci�n <b>"Generar_produccion_automatica_basada_en_consumos"</b> accesible desde el men� "Producci�n>Producci�n en cadena>"bot�n acciones"</p>
<p><b>Requisitos previos:</b> Es necesario crear en la aplicaci�n la configuraci�n de lote adecuada. Para ello en el "men� configuraci�n>Aplicaci�n", buscamos, seleccionamos y editamos el registro "aplicaci�n".</br>
Nos aseguramos que el campo "No permitir stock negativos" est� configurado en falso (rojo).</br>
En la tabla de configuraci�n lote creamos "nuevo" configuraci�n de tipo "proveedor". Dicha configuraci�n se recomienda que est� configurado con "stock negativo permitido" configurado a positivo (en verde)</br>
Es <b>importante</b> asignar el <b>formato fecha</b> que deseemos, necesario para que el sistema pueda codificar los lotes provisionales que veremos m�s adelante, y marcar <b>"crear autom�tico"</b> en positivo (verde).
</p>
La forma de trabajar es la siguiente: </br>
1- La empresa recibe pedidos de sus clientes y fabrica productos</br>
2- Dichos pedidos son preparados por el personal de almac�n o de fabricaci�n de la empresa, que prepara la mercanc�a a enviar y registra los lotes seleccionados para cada albar�n. </br>
3- Han sido preparadas salidas de cliente (albaranes) de productos cuya producci�n en la mayor�a de los caso todav�a no ha sido registrada en el sistema, pero si el albar�n de cliente, por tanto en muchos casos los stock de salida quedan negativos en el sistema.</br>
4- Al final del d�a, accionamos la acci�n "Generar_produccion_automatica_basada_en_consumos" (men� "producci�n>produccion cadena>boton acciones"). El sistema realiza las siguientes acciones:</br>
<ul><li>4.1- uno a uno, el sistema busca los sock negativos cuyo producto tenga un escandallo de salida asociado</li>
<li>4.2- para cada uno de dichos stock negativo el sistema crea una "producci�n en cadena" con la cantidad exacta para satisfacer la carencia de stock</li>
<li>4.3- la producci�n en cadena crear� tantas producci�nes simples como sea necesario, hasta llegar a la profundidad de la materia prima.</li>
<li>4.4- caso de que exista suficiente materia prima, simplemente ser� asignada y consumida de acuerdo al principio de seleccionar el lote m�s caduco. Caso de que no haya suficiente stock de una materia prima determinada para satisfacer una producci�n, el sistema crear� un stock "provisional" con la cantidad suficiente. Dicho stock quedar� en el sistema en negativo. Los stock provisionales se reconocen porque su campo "provisional" (que significa ficticio) aparece en positivo (en verde), y puede verse tanto desde el men� de stock como en el men&uacute; de lote.  Si deseamos modificar este comportamiento por defecto, en la configuraci�n de lote correspondiente debemos deshabilitar o poner en negativo (rojo) el campo "Stock negativo permitido".</li>
<li>4.5- Al finalizar la producci�n autom�tica, comprobamos los stock provisionales (negativos) que se han generado (en men� stock). Los stock negativos representan discrepancias de la realidad con la aplicaci&oacute;n. Puede haber dos razones por las que se ha llegado a esta situaci&oacute;n, y por tanto dos formas de satisfacer dichos stock (o quitar los negativos). </li>
<blockquote> <li>4.5.1- La primera puede ser porque hab�a ciertas discrepancias en los stock registrados en el sistema, en ese caso debemos seleccionar los stock reales discrepantes, es decir, aquellos stock que ahora se muestran con cantidad "cero" y pensamos deb�an aparecer con cantidades mayores, y procedemos a modificarlos desde la acci�n "Regularizar", donde pondremos modificar la cantidad final como deberian haber quedado tras la producci�n.</li>
<li>4.5.2- La segunda, puede ser que existan nuevas entradas (albaranes de compras) reales de mercancia que todav�a no han sido registradas en el sistema, en cuyo caso procedemos a registrarlas. Al registrar las entradas tenemos la opci&oacute;n de seleccionar y  asociar a la entrada directamente uno de los stock negativo provisional generado (es decir &quot;engordar&quot; con dicha entrada  el stock provisional convirtiendolo en real), o  crear otro (si creamos otro en cualquier caso el stock provisional al final del proceso ser&aacute; desvinculado autom&aacute;ticamente de las producciones quedando a cero, stock que despu&eacute;s podemos eliminar).</li></blockquote>
</ul>
5- Si hubo discrepancias y por tanto se generaron stock negativos, pero ya se ha registrado existencias suficientes para satisfacer el deficit (mediante regularizaciones o entradas de mercancia), es posible que todav�a la aplicaci�n no haya utilizado las nuevas existencias y todav�a se muestren  los stock negativos, esto pasa si las correcciones se han realizado aumentando otros stock (y no los provisionales), o las entradas se hayan realizado creando otros stock, y no seleccionando los provisionales. Para satisfacer el deficit bastar� con volver a ejecutar la acci�n automatica. Ahora el sistema tratar&aacute; de asignar lotes reales con existencias (con  stock positivo) donde haya stock ficticios o provisionales negativos. Al final de esta operaci�n los stock reales se habr&aacute;n consumido y los provisionales habran quedado a cero. Si todav�a queda algo negativo podemos volver a repetir tantas veces como sea necesario el �ltimo proceso (el paso 4.5 que consiste en regularizar o entrar mercancia de compras y accionar de nuevo la accion autom�tica).</br>
6- Es importante cada d�a observar aquellos stock de materias primas que en la realidad se han agotado (y apuntarlo fuera de la aplicaci�n) y comprobar que efectivamente tras el paso 5 en la aplicaci�n aparecen como consumidos (es decir a cero), y si no es asi regularizarlos o registrar entradas de compras pendientes.
<h3>7- Informe de trazabilidad</h3>

<p>En el men� "Comercial>Lote" est� disponible el informe de trazabilidad.</p>
<p>El informe nos permite ver la ascendencia y descendencia de cualquier lote. El informe se puede ejecutar con cualquier tipo de lote, tanto de compra como de ventas.</p>
<p>La trazabilidad se deduce de los registros de producci�n, por ello no debemos eliminar producciones una vez realizadas, ya que perderiamos la trazabilidad. Ademas eliminar una producci�n provocar�a deshacer el consumo y generaci�n de producto salida de dicha producci�n.</p>
<p>El par�metro <strong>"dias solape seguridad"</strong> es opcional, por defecto se toma como cero. </p>
<p>Dicho param�tro permite compensar los posibles errores de asignaci�n de lotes de materia prima en las producciones cuando trabajamos con la generaci�n autom�tica de trazabilidad (del punto anterior).</br>
Notar que si confrontamos diariamente los lotes agotados en la realidad, cada d�a, contra los stock de la aplicaci�n tras ejecutar la generaci�n autom�tica de la trabilidad, el error en la selecci�n autom�tica de materia prima no puede ser mayor de un d�a.
El error entonces vendr�a producido cuando un lote de materia prima est� registrado en una cantidad discrepante respecto a la realidad por diversas razones, y puede pasar que el sistema crea que un lote X se ha agotado cuando no sea as� en la realidad, asignando un nuevo lote a una producci�n erroneamente.
Si fijamos por ejemplo un d�a de solape de seguridad (asignando el valor 1), y lanzamos el informe para un lote de venta que ha consumido un lote L5 de materia prima P, consumo que se produjo el d�a D, caso de que desde el d�a D-1 hasta el mismo d�a D en cuestion, se agotase un lote L4 del mismo producto P, el sistema propondr�a como ascendencia tanto el lote L5 como el lote L4 por seguridad. Igualmente si desde el mismo d�a D hasta el d�a siguiente (D+1) un lote L6 del mismo producto acaba de "abrirse", tambi�n ser� incluido en la ascendencia. Incluso si hubieramos ejecutado el informe a partir de un lote de materia prima L5 para ver su desdencia, el sistema tambien mostrar�a la ascendencia L4 y L6 si se cumplen los dias de solape.</p>   
   