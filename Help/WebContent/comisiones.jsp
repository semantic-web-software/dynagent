<h2><a name="comisiones">Agentes comerciales y comisiones</a></h2>

<p>
En el área comercial es posible crear comerciales fijos (empleados) o externos (proveedores que nos emiten facturas).
Si un agente comercial es exclusivo a un cliente, dicho agente puede ser asignado a dicho cliente directamente en su "ficha de cliente". 
Es posible además indicar el <a href="http://server.dynagent.es/help/info.jsp?propiedad=porcentaje_exclusividad_defecto">porcentaje de exclusividad por defecto</a> para ventas realizadas por otro agente o colaborador,
 y excepciones o <a href="http://server.dynagent.es/help/info.jsp?propiedad=exclusividad_comercial">porcentajes de exclusividad</a> en colaboración con un agente comercial concreto.
</p>
<p>
Una venta puede comisionar de acuerdo a las comisiones declaradas en el área "Comercial>Comisiones" al agente comercial que realiza la venta, 
o al agente comercial asociado al cliente de forma exclusiva de acuerdo a un <a href="http://server.dynagent.es/help/info.jsp?propiedad=porcentaje_exclusividad_defecto">porcentaje de exclusividad por defecto</a>, 
o bien de acuerdo a un <a href="http://server.dynagent.es/help/info.jsp?propiedad=exclusividad_comercial">porcentaje de exclusividad</a> en colaboración con otro comercial (que realiza la venta o colaborador) distinto al comercial asociado al cliente o comercial exclusivo.  
Las comisiones se pueden distinguir por el ámbito al que aplica (familia, subfamilia, marca, cliente), siendo posible además restringir a que comerciales beneficia, así como precisar o modificar el porcentaje por defecto según el margen de beneficio de la venta.
Si por ejemplo definimos una comisión a la que asignamos la familia 1 y la 4, la marca "M" para ventas de mi empresa "E1", quiere decir que aplicará en aquellas ventas o facturas donde la empresa asociada sea E1, el producto pertenezca a la marca M, y la familia del producto sea o bien la F1 o la F4, es decir, el producto necesariamente debe pertenecer a almenos una de las familias asignadas a la comisión, basta que coincida en una.
</p>
<p style="BACKGROUND-COLOR: Cornsilk">Importante: Las comisiones, y en particular el ámbito de aplicación debe existir y estar correctamente definado antes de que se produzca la venta.</p>
<p>
Y es que es en el momento de la venta cuando el sistema resuelve los ámbitos de aplicación y prioridades, y asigna una comisión a la línea. Más tarde, cuando queramos liquidar, desde el menú "Comercial>Agente comercial" ejecutamos el informe "Detalle liquidación comercial" preferiblemente en formato excel.
En el momento que se ejecuta el informe, se resuelve que porcentaje aplica en base a margenes o por defecto, y se determina el reparto de comisiones entre agente exclusivo y colaborador. Por lo tanto, es posible modificar los porcentajes despues de que se hayan producido las ventas, ya que el infome de detalle atenderá a dichas modificaciones.  
</p>  

