<h2><a name="ordencreacion">Pasos para la puesta en marcha</a></h2>

<p>
Para comenzar a trabajar normalmente con la aplicaci�n, necesitamos realizar previamente los siguientes pasos, siendo este el orden recomendado.
</p>
<h3>1. Crear mi empresa ("Configuraci�n > Mi Empresa")</h3>

<p>
La aplicaci�n necesita al menos una "mi empresa" para operar.
</p>
<p>En ella podemos introducir todos los datos de direcciones, log, etc. a mostrar en los documentos impresos.
</p>
<p>
La versi�n superior o "adaptada", admite trabajar con varias "mi empresas" simultaneamente en la misma sesi�n, siendo posible facturar o contabilizar coexistiendo diferentes empresas en la misma base de datos, busquedas, etc. Es posible incluso crear reglas a medida que decidan a que empresa asociar qu� ventas.
</p>

<h3>2. Crear almac�n ("Almacenamiento > Almac�n")</h3>

<p> 
Este paso no es necesario si s�lo trabajamos con servicios.
</p>
<p>
Es posible crear m�s de un almac�n donde se registrar� el stock de cada producto. Por defecto el almac�n se asocia a cada documento (ejemplo un albar�n). Tambi�n es posible habilitar el m�dulo "multialmac�n" que permite asociar un almac�n distinto a cada producto (o l�nea de detalle) de un mismo albaran (siempre es posible asociar almacenes diferentes a albranes diferentes).
</p>

<h3>3. Configurar aplicaci�n ("Configuraci�n > Aplicaci�n")</h3>

<p>
En el objeto "Aplicaci�n" se encuentra la configuraci�n com�n a todas las empresas, como es la precisi�n de decimales, el almac�n por defecto, si permitimos vender sin stock, lotes de producci�n, etc.
</p>

<h3>4. Configurar &iacute;ndices o contadores ("Configuraci�n > Indices")</h3>

<p>
Los c�digos que siguen una numeraci�n correlativa pueden ser generados autom�ticamente por el sistema.
</p>
<p>
Por defecto el sistema ya incluye registros "indice" donde se definen los contadores correlativos para la clase "�rticulo", "cliente", "proveedor", "factura", etc., tanto de compra como de venta.
</p>
<p>
Es posible definir c�digo autoincrementales con un prefijo temporal (por ejemplo anual de la forma "a�o/contador", que se configurar�a indicando la mascara temporal aaaa para 4 digitos en el a�o, o aa para dos digitos por ejemplo) donde "contador" es un n�mero que se incrementa autom�ticamente, y se reinicia seg�n la "mascara temporal definida".
</p>
<p>
Si deseamos anular uno de los indices autom�ticos por defecto e informar manualmente el c�digo para una de estas clases, debemos buscar desde la aplicaci�n el registro "indice" filtrando por el campo "Dominio" y eliminarlo.
</p>
<p>
Es posible inicializar el valor siguiente del contador de un �ndice, buscando el registro concreto por el campo "Dominio", editando (modificar) y cambiando el valor del campo "inicio contador".
<p>

<h3>5. Crear familias, subfamilias y marcas (bajo �rea "Comercial")</h3>

<p>
Familias, subfamilias y marcas permiten clasificar productos, siendo posible m�s tarde definir filtros �tiles basados en el "�mbito" de producto.
</p>
<p>
Como ejemplo, ser� posible definir una comisi�n gen�rica para un comercial que aplique a todos los art�culos, y una excepci�n o comisi�n m�s espec�fica que aplique a una subfamilia, marca, o familia concreta, o tambi�n una excepci�n en las tarifas que aplican a un cliente por ejemplo para una subfamilia dada.
</p>
<p>
Estas categor�as se pueden crear y reutilizar durante la edici�n de un art�culo, o tambi�n crear previamente desde el �rea comercial.
</p>

<h3>6. Crear tarifas (Opcional)</h3>
<p>
Por defecto el art�culo puede trabajar con precios PVP, y caso de que deseemos ofrecer precios diferentes a distintos clientes, tenemos la opci�n de crear distintas tarifas, de manera que en el art�culo asignamos un precio para cada tarifa de cliente (y en el cliente indicamos a qu� tarifa le vendemos), o tambi�n podemos crear distintos grupos de clientes y definir precios especiales por grupo de cliente en base a distintos criterios (por tipo de producto, etc.).
</p>

<h3>7. Crear art�culos	(bajo �rea "Compras", "Ventas")</h3>

<p>
Los art�culos pueden ser de tipo: <a href="http://server.dynagent.es/help/info.jsp?clase=G�NERO">G�nero</a>, <a href="http://server.dynagent.es/help/info.jsp?clase=SERVICIO">Servicio</a>,o <a href="http://server.dynagent.es/help/info.jsp?clase=ART�CULO_FINANCIERO">Financiero</a>, y se venden o compran, o ambos casos, en funci�n de si lo asociamos al cat�logo de compra, de venta, o ambos (ver <a href="http://server.dynagent.es/help/info.jsp?clase=CAT�LOGO">Cat�logo</a>)	
</p>

<h3>8. Crear clientes</h3>

<h3>9. Crear proveedores</h3>
