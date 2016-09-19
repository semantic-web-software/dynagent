<h2><a name="ordencreacion">Pasos para la puesta en marcha</a></h2>

<p>
Para comenzar a trabajar normalmente con la aplicación, necesitamos realizar previamente los siguientes pasos, siendo este el orden recomendado.
</p>
<h3>1. Crear mi empresa ("Configuración > Mi Empresa")</h3>

<p>
La aplicación necesita al menos una "mi empresa" para operar.
</p>
<p>En ella podemos introducir todos los datos de direcciones, log, etc. a mostrar en los documentos impresos.
</p>
<p>
La versión superior o "adaptada", admite trabajar con varias "mi empresas" simultaneamente en la misma sesión, siendo posible facturar o contabilizar coexistiendo diferentes empresas en la misma base de datos, busquedas, etc. Es posible incluso crear reglas a medida que decidan a que empresa asociar qué ventas.
</p>

<h3>2. Crear almacén ("Almacenamiento > Almacén")</h3>

<p> 
Este paso no es necesario si sólo trabajamos con servicios.
</p>
<p>
Es posible crear más de un almacén donde se registrará el stock de cada producto. Por defecto el almacén se asocia a cada documento (ejemplo un albarán). También es posible habilitar el módulo "multialmacén" que permite asociar un almacén distinto a cada producto (o línea de detalle) de un mismo albaran (siempre es posible asociar almacenes diferentes a albranes diferentes).
</p>

<h3>3. Configurar aplicación ("Configuración > Aplicación")</h3>

<p>
En el objeto "Aplicación" se encuentra la configuración común a todas las empresas, como es la precisión de decimales, el almacén por defecto, si permitimos vender sin stock, lotes de producción, etc.
</p>

<h3>4. Configurar &iacute;ndices o contadores ("Configuración > Indices")</h3>

<p>
Los códigos que siguen una numeración correlativa pueden ser generados automáticamente por el sistema.
</p>
<p>
Por defecto el sistema ya incluye registros "indice" donde se definen los contadores correlativos para la clase "árticulo", "cliente", "proveedor", "factura", etc., tanto de compra como de venta.
</p>
<p>
Es posible definir código autoincrementales con un prefijo temporal (por ejemplo anual de la forma "año/contador", que se configuraría indicando la mascara temporal aaaa para 4 digitos en el año, o aa para dos digitos por ejemplo) donde "contador" es un número que se incrementa automáticamente, y se reinicia según la "mascara temporal definida".
</p>
<p>
Si deseamos anular uno de los indices automáticos por defecto e informar manualmente el código para una de estas clases, debemos buscar desde la aplicación el registro "indice" filtrando por el campo "Dominio" y eliminarlo.
</p>
<p>
Es posible inicializar el valor siguiente del contador de un índice, buscando el registro concreto por el campo "Dominio", editando (modificar) y cambiando el valor del campo "inicio contador".
<p>

<h3>5. Crear familias, subfamilias y marcas (bajo área "Comercial")</h3>

<p>
Familias, subfamilias y marcas permiten clasificar productos, siendo posible más tarde definir filtros útiles basados en el "ámbito" de producto.
</p>
<p>
Como ejemplo, será posible definir una comisión genérica para un comercial que aplique a todos los artículos, y una excepción o comisión más específica que aplique a una subfamilia, marca, o familia concreta, o también una excepción en las tarifas que aplican a un cliente por ejemplo para una subfamilia dada.
</p>
<p>
Estas categorías se pueden crear y reutilizar durante la edición de un artículo, o también crear previamente desde el área comercial.
</p>

<h3>6. Crear tarifas (Opcional)</h3>
<p>
Por defecto el artículo puede trabajar con precios PVP, y caso de que deseemos ofrecer precios diferentes a distintos clientes, tenemos la opción de crear distintas tarifas, de manera que en el artículo asignamos un precio para cada tarifa de cliente (y en el cliente indicamos a qué tarifa le vendemos), o también podemos crear distintos grupos de clientes y definir precios especiales por grupo de cliente en base a distintos criterios (por tipo de producto, etc.).
</p>

<h3>7. Crear artículos	(bajo área "Compras", "Ventas")</h3>

<p>
Los artículos pueden ser de tipo: <a href="http://server.dynagent.es/help/info.jsp?clase=GÉNERO">Género</a>, <a href="http://server.dynagent.es/help/info.jsp?clase=SERVICIO">Servicio</a>,o <a href="http://server.dynagent.es/help/info.jsp?clase=ARTÍCULO_FINANCIERO">Financiero</a>, y se venden o compran, o ambos casos, en función de si lo asociamos al catálogo de compra, de venta, o ambos (ver <a href="http://server.dynagent.es/help/info.jsp?clase=CATÁLOGO">Catálogo</a>)	
</p>

<h3>8. Crear clientes</h3>

<h3>9. Crear proveedores</h3>
