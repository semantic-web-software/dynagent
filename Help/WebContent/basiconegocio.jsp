<h2><a name="basiconegocio">Conceptos b�sicos de negocio</a></h2>
<h3>1. Proceso de compra venta</h3>

<p>
Las compras y las ventas se basan en la elaboracion de un documento comercial (pedido, albar�n, factura, etc), donde el detalle se informa en las <a href="http://server.dynagent.es/help/info.jsp?propiedad=l�nea">l�neas</a> de dicho documento.</br></br>
</p>
<div align=center>
	<p><img border="1" src="http://www.dynagent.es/images/manual/linea_documento.jpg" alt="L�nea de Documento" /></p>
</div>

<p style="BACKGROUND-COLOR: Cornsilk">Nota: s�lo el albar�n afecta a los stock. Si desea realizar directamente una factura modificando stock, debe crear un documento de la clase "albar�n-factura".
Si por el contrario ha creado un albar�n, que posteriormente ha evolucionado a factura, y desea modificar cantidades de las l�neas en ambos documentos simultaneamente, 
debe aplicar sobre la factura la acci�n <a href="http://server.dynagent.es/help/info.jsp?clase=Modificar_Conjuntamente_Factura_Albar�n_Stock">"Modificar Conjuntamente Factura Albar�n Stock"</a>
</p>

<p>
Un documento de cualquier fase puede ser evolucionado a su fase siguiente autom�ticamente desde una acci�n: por ejemplo un pedido puede convertirse en albar�n seleccionando el pedido y ejecutando la acci�n "Albaranar documentos".
El ciclo mencionado puede comenzar en cualquier fase. Por ejemplo, es posible crear directamente un albar�n sin un pedido previo, o una factura sin albar�n (aunque esta factura, al no tener albar�n, no modificar�a stock).  
 	El Albar�n-Factura,  al igual que el albar�n,  modifica stocks.
</p>
 
<p style="BACKGROUND-COLOR: Cornsilk">Notas: Excepcionalmente, en el caso de la factura es posible la evoluci�n inversa: primero crear una factura sin albar�n y despues generar la entrega o albaranado de la misma.</br>
Tambi�n es posible entregar o recepcionar (fase albar�n) parcialmente lineas pedidas, quedando pendientes las lineas no servidas. Igualmente es posible facturar parcialmente lineas de albar�n.</p></br>

<div align=center>
	<p><img border="1" src="http://www.dynagent.es/images/manual/proceso_compra_venta.jpg" alt="Proceso compra venta" /></p>
</div>
	
<h3>2. Tesorer�a</h3>
<p>
La deuda con el cliente (o nuestra respecto al proveedor), se contrae en el momento de la facturaci�n, materializandose en la clase "Vencimiento", existiendo un vencimiento por cada importe y fecha comprometidas de acuerdo a la forma de pago acordada.
</p>
<p>
Cobrar es por tanto asignar importes (totales o parciales) a uno o varios vencimientos (un vencimiento se puede cobrar parcialmente en cuyo caso contin�a teniendo deuda).
</p>
<p>
Podemos cobrar/pagar directamente una factura, o bien desde el �rea de tesorer�a seleccionar directamente uno o varios vencimientos a cobrar/pagar.
</p>
<p>
Tambien es posible cobrar un documento previo a la factura asignando a dicho documento un "cobro anticipo", o bien editando la factura, o bien aplicandole la acci�n de cobrar. 
La factura posterior exige crear la forma de pago por el importe total, o bien de un �nico pago (porcetaje=100%), o bien en varios pago con distinto porcentaje. Una vez se grabe la factura se generan los objetos "vencimiento" para el importe no anticipado, tantos como formas de pago se hayan indicado.
Por tanto existen dos tipos de cobros: anticipo y cobros de vencimiento. Un cobro anticipo referencia un documento (pedido, albaran o factura), y un cobro vencimiento referencia y asigna dinero a &quot;n&quot; vencimientos. Es posible crear un anticipo de cliente sin indicar su documento, que m�s tarde ser� propuesto autom�ticamente por el sistema para pagar una factura de dicho cliente. No es posible combinar un anticipo a un documento distinto de factura a la vez que se cobra vencimientos. 
</p>
<div align=center>
	<p><img border="1" src="http://www.dynagent.es/images/manual/anticipo-forma-pago-vencimiento-cobro.jpg" alt="Anticipo,forma-pago,vencimiento,cobro" /></p>
</div>

