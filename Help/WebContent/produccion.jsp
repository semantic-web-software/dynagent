<h2><a name="produccion">Producción y fabricación</a></h2>
<p>
Una producción transforma stocks, dando de alta nuevos stock del producto elaborado/fabricado a la vez que consume el stock de los componentes que lo integran, enlanzando implicitamente las trazas de lotes implicados si los hubiera.
</p>
<h3>1- Escandallo</h3>
<p>
Un escandallo define las proporciones (o &quot;receta&quot;) de dicha transformación.
</p>
<p>
Si existe un escandallo, dada una salida en una producción, el sistema automáticamente añadirá los componentes necesarios o modificará sus cantidades de acuerdo a la salida deseada en una producción, 
así como, caso de que se trabaje con lotes, asignará automáticamente el lote más antiguo, siendo posible modificar la decisión del sistema.
</p>
<p>
Un escandallo se define declarando los productos y cantidades de entrada (o componentes) y salida (o l&iacute;neas), no importa que valor concreto o absoluto eligamos como referencia en el campo cantidad para definir el escandallo, ya que el sistema sólo tendrá en cuenta la proporción entrada con salida. Es necesario trabajar con las mismas unidades que las ventas y compras.</br>
Ejemplo, queremos declarar un escandallo de una tarta tal que por cada kilogramo de tarta se consume 500 gramos de harina, y 10gr de canela, siendo el peso de una tarta 1500gr.</br>
<h4>Ejemplo Caso 1.</h4> Supongamos que en nuestras ventas y compras hemos tomado el criterio de vender tartas por unidades (o bultos), y no por kilogramos, y hemos decidido además que compramos harina en kilogramos y canela en gramos.</br>
Creamos un escandallo con una línea de salida del producto "tarta" y cantidad=1 (representa una unidad), y dos componentes, harina de cantidad (500/1000)*1,5kg=0,75kg, y canela de cantidad=(10/1000)*1500gr=15gr.</br></br>
Como podemos apreciar, para calcular la harina expresamos el último termino de cantidad final de tarta en kg (1,5kg), por ser esta la únidad de referencia de la harina, y en el caso de la canela el último termino de cantidad tarta (1500gr) lo expresamos en gramos para coincidir con la unidad de referencia de la canela. 
Por tanto la formula de un componente seria:</br>
Cantidad componente= (proporcion componente/proporcion producto salida)*(la cantidad de producto salida del escandallo expresada en las mismas unidades de compra que el componente)</br> 
<h4>Ejemplo Caso 2.</h4> Nuestro criterio es vender tartas "al peso" o por kilogramos, e igualmente compramos harina en kg y canela en gr.</br>
 Creamos un escandallo con una línea de salidad de prodcuto "tarta" y cantidad 1 (representa 1kg), y dos componentes: harina de cantidad= 500/1000*1=0,5kg, y canela de 10/1000*1000=10gr.</br></br>
 Como podemos ver en este segundo caso las cantidades coinciden con la "receta". En este segundo caso sería igualmente válido definir una salida "tarta" de cantidad 2 (representa 2kg), y dos componentes "harina" cantidad=500/1000*2kg=1kg, y "canela" de 10/1000*2000gr=20gr, ya que como hemos dicho en el escandallo sólo importan las proporniciones, no el valor absoluto.
<h3>2- Produccion material: Fabricación</h3>
<p>
En la producción material declaramos en la salida, el producto de salida y cantidad deseada, y el sistema automáticamente calcula la entrada necesaria para satisfacer dicha salida, y en caso de haber lotes o números de series y as&iacute; haberse configurado en el men&uacute; &quot;aplicaci&oacute;n&quot;, asignará stock automáticamente.
El origen es el almacén donde se consume la entrada, y el destino es el almacén donde se genera el stock de salida (ambos pueden ser el mismo).
</p>
<h3>3- Producción en cadena</h3>
<p>
Desde la producción en cadena declaramos la necesidad (también llamada demanda o salida) de productos que queremos fabricar (en la tabla "línea"), 
de manera que el sistema genera automáticamente las producciones simples necesarias, de forma encadenada, vinculando dichas producciones a la tabla "producción".
</p>
<p>
Por ejemplo, si hemos declarado dos escandallos: A) uno que produce en salida ordenador cantidad=1 y tiene como entrada fuente alimentacion=2, y B) que tiene como salida fuente alimentacion y entrada ventilador=2. 
Si en la produccion en cadena declaramos una salida o linea de ordenador cantidad=10, se genera 2 producciones (la 1 de cantidad de salida=10, y la 2 con cantidad de salida=10*2=20).  
</p>
<p style="BACKGROUND-COLOR: Cornsilk">La producción en cadena, y todas las producciones simples asociadas, se crean por defecto en estado "Planificado", 
que no modifica stock ni permite asignación auomática de stock, lo cual es conveniente si únicamente se desea planificar la necesidad de materiales y stock existentes. 
Para modificar los stock debemos cambiar el estado de la producción en cadena a programado o cualquier otro que no sea planificado.
 El sistema automáticamente propagará dicho estado a cada una de las producciones simples.</p>
<p style="BACKGROUND-COLOR: Cornsilk">El sistema calula los trabajos para produccir completamente las cantidades indicadas en la salida de la producción en cadena, 
independientemente de que haya existencias en stock de dicha salida. Sin embargo, para los trabajos encadenados intermedios el sistema si tiene en cuenta las existencias, 
no produciendo lo que ya existe. En el ejemplo anterior, si en stock tenemos 1 ordenador y 1 fuente alimentacion, creara producciones para generar 10 ordenadores (no descuenta 1 por la existencia), 
y una producción para generar 19 fuentes de alimentación (20 demandadas menos la 1 existente)</p> 
<h3>4- Modulo lote y campo requiere lote</h3>
El módulo lote (entrando a la aplicación en modo "configuración") instala todas las propiedades necesarias para trabajar con lote, y en particular en artículo "género" aparece la propiedad "requiere lote". Es necesario marcar positivamente (en verde) dicha propiedad para que el sistema obligue a en todos los movimientos (albaranes y producciones) a que se informe el lote, y para que no se generen stock del producto sin lote, es decir, para que el sistema espere a que las líneas tengan lote antes de modificar stocks.
<h3>5- Configuracion de la automatización de lotes. Menú "Configuracion > Aplicación"</h3>
<p>
Lotes diferentes deben tener códigos diferentes. Dado que distintos proveedores pueden proporcionarnos lotes de mismo código, e incluso un mismo proveedor puede codificar distintos productos con el mismo lote, es necesario distinguirlo añadiendo como prefijo el código de proveedor y el código de producto.
Es posible configurar el sistema para que añada automáticamente como prefijo (al c&oacute;digo del lote) el código de producto y/o proveedor (ver menú Configuración>Aplicación). Notar que en lotes con formato fecha, la fecha de referencia es la del documento, y no la fecha del día actual.
</p>
<p>
En el caso de lotes producidos por nuestra empresa, podemos configurar el sistema para que genere el código del lote automáticamente (men&uacute; Aplicacion &gt; Config Lotes, asignando el campo &quot;crear autom&aacute;tico a true o verde&quot;), en formato diario (aammdd:año año mes mes dia dia,aassd: año año nº_semana nº_semana dia_semana de 1 a 7 ).
</p>
<p>
Existe un segundo caso en el que interesa generar lotes automáticamente: el lote multiproveedor, que es cuando el lote proviene de un proveedor pero carece de lote y se mezcla directamente con otros lotes de proveedor (por ejemplo un ganadero trae directamente la leche que se mezcla en un contenedor común).
Tambien es posible configurar la fecha de caducidad o garantía automáticamente.
</p>
<p> 
Todas estas propiedades son configurables por familias, subfamilias o marcas. Podemos configurar lotes de proveedor y lotes propios por separado. Caso de que sea necesario distinguir configuraciones en base a la clasificacion de productos (como la familia), es conveniente trabajar con excepciones o prioridades distintas, con objeto de evitar la necesidad de declarar una configuración para cada clasificación de producto. Por ejemplo, tenemos 10 familias de productos de alimentación, todas ellas trabajan con 3 meses de caducidad, excepto los congelados que trabajan con un año. Podemos declarar una configuracion de priodiridad mínima con caducidad de 3 meses, en la que NO precisamos la familia, es decir, es aplica para cualquier familia, y adicionalmente crear una configuracion de prioridad mayor (por ejemplo prioridad normal) con caducidad 1 a&ntilde;o, donde esta vez fijamos en la configuración la familia congelados. En el caso de producir un congelado, ambas configuraciones son compatibles, pero la configuración para congelados es la que aplica por tener mayor prioridad. En el caso de producir otro tipo de producto, sólo es compatible la configuración genérica, por tanto no hay opci&oacute;n de aplicar la configuraci&oacute;n espec&iacute;fica para congelados.
</p>
<h3>6- Acción automática para generar la trazabilidad y la producción </h3>

<p>La idea es evitar la dinámica habitual de producción en el sistema reemplazandola por una nueva din&aacute;mica basada en las ventas:</p>
<h4>Opción 1: Dinámica semiautomática (trataremos de evitar esta opci&oacute;n siempre que se pueda aplicar la opción 2, con objeto de ahorrar esfuerzos)</h4>
<p>Consistiría en registrar cada producción manualmente, ya sea producciones simples o producciones en cadena. El usuario informa del producto de salida a obtener, y el sistema propone automáticamente las cantidades a consumir, y además si la producción se encuentra en estado "programado" el sistema asigna automáticamente el lote más caduco a consumir. El usuario entonces puede supervisar dicha propuesta y en su caso modificar la elecci&oacute;n de  lotes, ya que en algunos casos las cantidades reales de stock no coinciden con las supuestas por el sistema y es necesario modificar la elección de dichos lotes.</p>
<h4>Opción 2: Dinámica automática inferida</h4> 
<p>Acción <b>"Generar_produccion_automatica_basada_en_consumos"</b> accesible desde el menú "Producción>Producción en cadena>"botón acciones"</p>
<p><b>Requisitos previos:</b> Es necesario crear en la aplicación la configuración de lote adecuada. Para ello en el "menú configuración>Aplicación", buscamos, seleccionamos y editamos el registro "aplicación".</br>
Nos aseguramos que el campo "No permitir stock negativos" está configurado en falso (rojo).</br>
En la tabla de configuración lote creamos "nuevo" configuración de tipo "proveedor". Dicha configuración se recomienda que esté configurado con "stock negativo permitido" configurado a positivo (en verde)</br>
Es <b>importante</b> asignar el <b>formato fecha</b> que deseemos, necesario para que el sistema pueda codificar los lotes provisionales que veremos más adelante, y marcar <b>"crear automático"</b> en positivo (verde).
</p>
La forma de trabajar es la siguiente: </br>
1- La empresa recibe pedidos de sus clientes y fabrica productos</br>
2- Dichos pedidos son preparados por el personal de almacén o de fabricación de la empresa, que prepara la mercancía a enviar y registra los lotes seleccionados para cada albarán. </br>
3- Han sido preparadas salidas de cliente (albaranes) de productos cuya producción en la mayoría de los caso todavía no ha sido registrada en el sistema, pero si el albarán de cliente, por tanto en muchos casos los stock de salida quedan negativos en el sistema.</br>
4- Al final del día, accionamos la acción "Generar_produccion_automatica_basada_en_consumos" (menú "producción>produccion cadena>boton acciones"). El sistema realiza las siguientes acciones:</br>
<ul><li>4.1- uno a uno, el sistema busca los sock negativos cuyo producto tenga un escandallo de salida asociado</li>
<li>4.2- para cada uno de dichos stock negativo el sistema crea una "producción en cadena" con la cantidad exacta para satisfacer la carencia de stock</li>
<li>4.3- la producción en cadena creará tantas producciónes simples como sea necesario, hasta llegar a la profundidad de la materia prima.</li>
<li>4.4- caso de que exista suficiente materia prima, simplemente será asignada y consumida de acuerdo al principio de seleccionar el lote más caduco. Caso de que no haya suficiente stock de una materia prima determinada para satisfacer una producción, el sistema creará un stock "provisional" con la cantidad suficiente. Dicho stock quedará en el sistema en negativo. Los stock provisionales se reconocen porque su campo "provisional" (que significa ficticio) aparece en positivo (en verde), y puede verse tanto desde el menú de stock como en el men&uacute; de lote.  Si deseamos modificar este comportamiento por defecto, en la configuración de lote correspondiente debemos deshabilitar o poner en negativo (rojo) el campo "Stock negativo permitido".</li>
<li>4.5- Al finalizar la producción automática, comprobamos los stock provisionales (negativos) que se han generado (en menú stock). Los stock negativos representan discrepancias de la realidad con la aplicaci&oacute;n. Puede haber dos razones por las que se ha llegado a esta situaci&oacute;n, y por tanto dos formas de satisfacer dichos stock (o quitar los negativos). </li>
<blockquote> <li>4.5.1- La primera puede ser porque había ciertas discrepancias en los stock registrados en el sistema, en ese caso debemos seleccionar los stock reales discrepantes, es decir, aquellos stock que ahora se muestran con cantidad "cero" y pensamos debían aparecer con cantidades mayores, y procedemos a modificarlos desde la acción "Regularizar", donde pondremos modificar la cantidad final como deberian haber quedado tras la producción.</li>
<li>4.5.2- La segunda, puede ser que existan nuevas entradas (albaranes de compras) reales de mercancia que todavía no han sido registradas en el sistema, en cuyo caso procedemos a registrarlas. Al registrar las entradas tenemos la opci&oacute;n de seleccionar y  asociar a la entrada directamente uno de los stock negativo provisional generado (es decir &quot;engordar&quot; con dicha entrada  el stock provisional convirtiendolo en real), o  crear otro (si creamos otro en cualquier caso el stock provisional al final del proceso ser&aacute; desvinculado autom&aacute;ticamente de las producciones quedando a cero, stock que despu&eacute;s podemos eliminar).</li></blockquote>
</ul>
5- Si hubo discrepancias y por tanto se generaron stock negativos, pero ya se ha registrado existencias suficientes para satisfacer el deficit (mediante regularizaciones o entradas de mercancia), es posible que todavía la aplicación no haya utilizado las nuevas existencias y todavía se muestren  los stock negativos, esto pasa si las correcciones se han realizado aumentando otros stock (y no los provisionales), o las entradas se hayan realizado creando otros stock, y no seleccionando los provisionales. Para satisfacer el deficit bastará con volver a ejecutar la acción automatica. Ahora el sistema tratar&aacute; de asignar lotes reales con existencias (con  stock positivo) donde haya stock ficticios o provisionales negativos. Al final de esta operación los stock reales se habr&aacute;n consumido y los provisionales habran quedado a cero. Si todavía queda algo negativo podemos volver a repetir tantas veces como sea necesario el último proceso (el paso 4.5 que consiste en regularizar o entrar mercancia de compras y accionar de nuevo la accion automática).</br>
6- Es importante cada día observar aquellos stock de materias primas que en la realidad se han agotado (y apuntarlo fuera de la aplicación) y comprobar que efectivamente tras el paso 5 en la aplicación aparecen como consumidos (es decir a cero), y si no es asi regularizarlos o registrar entradas de compras pendientes.
<h3>7- Informe de trazabilidad</h3>

<p>En el menú "Comercial>Lote" está disponible el informe de trazabilidad.</p>
<p>El informe nos permite ver la ascendencia y descendencia de cualquier lote. El informe se puede ejecutar con cualquier tipo de lote, tanto de compra como de ventas.</p>
<p>La trazabilidad se deduce de los registros de producción, por ello no debemos eliminar producciones una vez realizadas, ya que perderiamos la trazabilidad. Ademas eliminar una producción provocaría deshacer el consumo y generación de producto salida de dicha producción.</p>
<p>El parámetro <strong>"dias solape seguridad"</strong> es opcional, por defecto se toma como cero. </p>
<p>Dicho paramétro permite compensar los posibles errores de asignación de lotes de materia prima en las producciones cuando trabajamos con la generación automática de trazabilidad (del punto anterior).</br>
Notar que si confrontamos diariamente los lotes agotados en la realidad, cada día, contra los stock de la aplicación tras ejecutar la generación automática de la trabilidad, el error en la selección automática de materia prima no puede ser mayor de un día.
El error entonces vendría producido cuando un lote de materia prima está registrado en una cantidad discrepante respecto a la realidad por diversas razones, y puede pasar que el sistema crea que un lote X se ha agotado cuando no sea así en la realidad, asignando un nuevo lote a una producción erroneamente.
Si fijamos por ejemplo un día de solape de seguridad (asignando el valor 1), y lanzamos el informe para un lote de venta que ha consumido un lote L5 de materia prima P, consumo que se produjo el día D, caso de que desde el día D-1 hasta el mismo día D en cuestion, se agotase un lote L4 del mismo producto P, el sistema propondría como ascendencia tanto el lote L5 como el lote L4 por seguridad. Igualmente si desde el mismo día D hasta el día siguiente (D+1) un lote L6 del mismo producto acaba de "abrirse", también será incluido en la ascendencia. Incluso si hubieramos ejecutado el informe a partir de un lote de materia prima L5 para ver su desdencia, el sistema tambien mostraría la ascendencia L4 y L6 si se cumplen los dias de solape.</p>   
   