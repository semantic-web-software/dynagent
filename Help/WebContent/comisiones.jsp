<h2><a name="comisiones">Agentes comerciales y comisiones</a></h2>

<p>
En el �rea comercial es posible crear comerciales fijos (empleados) o externos (proveedores que nos emiten facturas).
Si un agente comercial es exclusivo a un cliente, dicho agente puede ser asignado a dicho cliente directamente en su "ficha de cliente". 
Es posible adem�s indicar el <a href="http://server.dynagent.es/help/info.jsp?propiedad=porcentaje_exclusividad_defecto">porcentaje de exclusividad por defecto</a> para ventas realizadas por otro agente o colaborador,
 y excepciones o <a href="http://server.dynagent.es/help/info.jsp?propiedad=exclusividad_comercial">porcentajes de exclusividad</a> en colaboraci�n con un agente comercial concreto.
</p>
<p>
Una venta puede comisionar de acuerdo a las comisiones declaradas en el �rea "Comercial>Comisiones" al agente comercial que realiza la venta, 
o al agente comercial asociado al cliente de forma exclusiva de acuerdo a un <a href="http://server.dynagent.es/help/info.jsp?propiedad=porcentaje_exclusividad_defecto">porcentaje de exclusividad por defecto</a>, 
o bien de acuerdo a un <a href="http://server.dynagent.es/help/info.jsp?propiedad=exclusividad_comercial">porcentaje de exclusividad</a> en colaboraci�n con otro comercial (que realiza la venta o colaborador) distinto al comercial asociado al cliente o comercial exclusivo.  
Las comisiones se pueden distinguir por el �mbito al que aplica (familia, subfamilia, marca, cliente), siendo posible adem�s restringir a que comerciales beneficia, as� como precisar o modificar el porcentaje por defecto seg�n el margen de beneficio de la venta.
Si por ejemplo definimos una comisi�n a la que asignamos la familia 1 y la 4, la marca "M" para ventas de mi empresa "E1", quiere decir que aplicar� en aquellas ventas o facturas donde la empresa asociada sea E1, el producto pertenezca a la marca M, y la familia del producto sea o bien la F1 o la F4, es decir, el producto necesariamente debe pertenecer a almenos una de las familias asignadas a la comisi�n, basta que coincida en una.
</p>
<p style="BACKGROUND-COLOR: Cornsilk">Importante: Las comisiones, y en particular el �mbito de aplicaci�n debe existir y estar correctamente definado antes de que se produzca la venta.</p>
<p>
Y es que es en el momento de la venta cuando el sistema resuelve los �mbitos de aplicaci�n y prioridades, y asigna una comisi�n a la l�nea. M�s tarde, cuando queramos liquidar, desde el men� "Comercial>Agente comercial" ejecutamos el informe "Detalle liquidaci�n comercial" preferiblemente en formato excel.
En el momento que se ejecuta el informe, se resuelve que porcentaje aplica en base a margenes o por defecto, y se determina el reparto de comisiones entre agente exclusivo y colaborador. Por lo tanto, es posible modificar los porcentajes despues de que se hayan producido las ventas, ya que el infome de detalle atender� a dichas modificaciones.  
</p>  

