<h2><a name="proyectos">Proyectos. Tareas y disponibilidad.</a></h2>

<p style="BACKGROUND-COLOR: Cornsilk">
Importante: Criterio en las "horas"<br><br>
Las fechas inicio y fin tanto de tareas, programaci�n, como calendario, diferencian el d�a y la hora. La hora NO se expresa de 0 a 24, si no de 0 al total de horas habiles del d�a (habitualmente 8). 
Por ejemplo, si queremos expresar un per�odo que comprenda desde 1 de Enero primera hora laborable a 31 Diciembre hasta �ltima hora laborable, expresariamos 01/01/2012 00:00:00 hasta 31/12/2012 08:00:00. 
Realmente si la hora final fuera una hora superior al n� de horas h�biles, el sistema no fallar�a y seguir�a computando el m�ximo, as� un fin de per�odo de 31/12/2012 09:00:00 computa exactamente igual que el anterior. Entonces s importante tener en cuenta que un inicio a las 0 horas no tiene un significado de cuando se comienza (que parecer�a de madrugada), sino relativo, significa comenzar al inicio del per�odo h�bil de ese d�a, y por la misma razon una finalizaci�n a la hora 8, no significa terminar a las 8 de la ma�ana, sino (en caso de una jornada de 8 horas) que significa ocupar todo el d�a en cuesti�n.
</p>
<p>
El orden a seguir para la introducci�n de los datos es:
</p>

<h3>1. Servicios ofrecidos o subcontratados</h3>
<p>
Servicio es un producto espec�fico que se crea como un art�culo m�s de compra o venta (en funci�n de si lo subcontratamos u ofertamos). Es necesario por dos razones:
</p>
<ul>
	<li>Para integrar las tareas y proyectos con compras y ventas:  el servicio se compra, vende, presupuesta o entrega como un �rticulo m�s en las l�neas de los documentos.</li>
	<li>Para diferenciar las capacidades o rol de un recurso, as� por ejemplo podemos definir servicios como "Ingenier�a", "soldador", limpieza etc, y asociar dicho servicio como capacidad del recurso (al definir un recurso).</li>
</ul>
<p>
Posteriormente al crear las tareas, ser� necesario indicar qu� servicio estamos demandando o planificando, programando o realizando (en parte de trabajo).
</p>
<h3>2. Calendario h�bil</h3>
<p>
En un <a href="http://server.dynagent.es/help/info.jsp?clase=CALENDARIO_H�BIL">calendario h�bil</a> es recomendable crear los per�odos desde el boton verde de nuevo, y no desde la tabla, ya que los campos tienen hora (ver crit�rio de hora del principio).
</p>
<h3>3. Recurso</h3>
<p>
Una vez creado el recurso, si le hemos asignado un calendario, se crea autom�ticamente una <a href="http://server.dynagent.es/help/info.jsp?clase=DISPONIBILIDAD">disponibilidad</a> (visible desde el men� "Disponibilidad") para dicho recurso. No es recomendable crear disponibilidades manualmente.
</p>
<h3>4. Tareas</h3>
<p>
Las tareas representan un com�n de actividades (subtareas) desarrolladas por N recursos y mismas restricciones de programaci�n: misma fecha m�nima de inicio, misma fecha m�xima de fin, misma prioridad y peso, y misma restricci�n de comenzar despues de cualquier otra tarea predecesora.
</p>
<p>
Las actividades se desglosan como subtareas, donde tan s�lo se indica el servicio que clasifica cada actividad, el n� de horas, prioridad, y su contenido.
</p>
<h3>5. Programaci�n, Dedicaci�n com�n y espec�fica</h3>
<p>
Las subtarea representan la demana o planificaci�n de recursos a programar en fechas y ocupaciones concretas. Tres modos de planificaci�n y programaci�n son posibles:
</p>
<ul>
	<li>No indicar ning�n recurso, y el sistema buscar� el m�s disponible capacitado para producir cada producto/servicio de las subtareas</li>
	<li>Todas las subtareas van a ser desarrolladas por uno o varios recursos, cada uno de ellos siempre a un mismo porcentaje de dedicaci�n (de 0 a 100) que indicariamos en la tabla "Dedicaci�n", com�n al desarrollo de todas las subtareas. Si por ejemplo la suma de todas las subtareas suponen 10 horas, e indicamos una dedicaci�n del recurso R1 al 40%, y el recurso R2 al 40%, la dedicaci�n total es del 80% (es decir en total no llega a trabajarse el horario completo), y se tratar� de asignar ambos recursos a cada tarea con dicho porcentaje. Si por el contrario las dedicaciones de R1 y R2 fueran del 80%, la dedicaci�n total es del 160%, lo cual supone un acortamiento de tiempos respecto a un solo recurso trabajando al 100%.</li>
	<li>Habiendo o no una dedicaci�n com�n, es posible especificar o forzar para una subtarea concreta un recurso concreto (que trabajar� al 100% dedicaci�n, y lo indicamos en el campo "recurso" de la subtarea). No es posible forzar un recuso X que ya aparece en la dedicaci�n com�n.</li>
</ul> 
<h4>Programar la tarea: Asignaci�n autom�tica de recursos y disponibilidades</h4>
<p>
El sistema en todo momento trata de programar la tarea, lo que significa localizar y RESERVAR un recurso y fecha disponible para realizar la tarea lo antes posible, cumpliendo las restricciones de la tarea. En la b�squeda de recursos disponibles, el sistema siempre da
prioridad al recurso espec&iacute;fico declarado en la propia subtarea. En segunda prioridad a los recursos (opcionales) asignados en la tabla "dedicaci�n" en la propia tarea. Si no se fuerza ning�n recurso, el sistema tratar� de asignar recursos miembros del proyecto de la tarea (el proyecto es opcional), y por �ltimo si no se ha impuesto ni uno ni otro, el sistema propondr� cualquier recurso disponible que tenga la misma capacidad (servicio) que el producto indicado en la subtarea.
</p>
<h4>Reprogramaci�n simult�nea de m�ltiples tareas y distintos proyectos</h4> 
<p>
En principio, la edici�n de una tarea y modificaci�n de alguna de sus restriciones, como la prioridad o fecha de inicio, 
puede motivar una reprogramaci�n de dicha tarea, de acuerdo a la disponibilidad libre en com�n a todos los proyectos y tareas, sin embargo a priori otras tareas existentes no se ven afectadas.
</p>
<p>
Una primera forma de reprogramar es mediante la edici�n m�ltiple: seleccionar varias tareas a la vez y editarlas (bot�n lapiz). Lo m�s habitual ser�a modificar la fecha m�nima inicio para retrasar todas las tareas.
</p>
<p>
Notar que la reprogramaci�n solo podr� asignar la ejecuci�n en una fecha dada ya ocupada, si la tarea que ocupa dicha fecha es reprogramada a la vez, de lo contrario su ocupaci�n permanece inalterable por mucho que otras tareas tengan mayor prioridad. Si por ejemplo tras un cambio de prioridad en una tarea X, deseamos que el sistema vuelva a reprocesar un conjunto de N tareas que pueden verse afectadas, debe seleccionar todas las tareas y ejecutar la acci�n "Programar de nuevo".
</p>
<h4>Seguimiento y Control de lo realizado: porcentaje de realizaci�n y partes de trabajo</h4>
<p>
El sistema por defecto supone que no hay desvios, es decir, supone que en todo momento la cantidad de horas demandadas en la subtareas debe coincidir con la suma de la cantidad programada y consumida, de manera que si no coincide aumentar� o reducir� automaticamente la cantidad programada.
</p>
<p>
Existen dos formas de incrementar el porcentaje de realizaci�n de una tarea:
</p>
<ol>
	<li>A�adiendo una linea de "consumo" del tipo "Parte trabajo". En este caso se crea una l�nea de consumo del tipo "Parte de Trabajo" con la cantidad realizada, y se reduce autom�ticamente la cantidad programada.</li>
	<li>Modificando el porcentaje de realizaci�n de la l�nea programada (expresado de 0 100)</li>
</ol>
<p>
Caso de que se conzoca un desvio es necesario (mejor previamente) modificar la cantidad demandada en las subtareas para reflejar dicho desvio. Si se desea distinguirlo es posible asignar una revisi�n superior a una nueva subtarea.
</p>
<h3>6. Presupuestar y Facturar una tarea</h3>
<p>
Es posible realizar ambas acciones desde las acciones: "Generar presupuesto" y "Generar Factura" en el men� tarea. Al contrario que en el ciclo o evoluci�n de una venta, en el que las mismas lineas de ventas se comparten entre las distintas fases o documentos, al presupuestar o facturar una tarea se copian las lineas realizadas (o l�neas de consumo) de la tarea en el presupuesto o factura. En el caso de la accion de facturar, al final de la acci�n la tarea queda marcada como facturada.
</p>
<p> 
Si tras dicha factura queremos informar de desvios, conviente aumentar el campo ""revisi�n"" de la tarea, de manera que las nuevas l�neas de subtarea se suscriben a la nueva revisi�n, siendo posible posteriormente generar un presupuesto o factura a partir de s�lo la nueva revisi�n.
</p> 
<h3>7. Proyectos</h3>
<p>
El proyecto es un concepto que permite enlazar transversalemte distintos documentos de distinta naturaleza y fase. As� puede ser asociado tanto a compras, como ventas, como tareas. El proyecto contabiliza los costes e ingresos imputables, permite representaci�n gantt (existe un informe llamado Gantt), asi como un cuadro de financiaci�n.
</p>
<h3>8. Trabajar con revisiones</h3>
<p>
Las revisiones permiten diferenciar las desviaciones durante un per�odo de tiempo, o hasta que sucera un hito como una faturaci�n. El usuario debe aumentar el n� de revisi�n, o bien del proyecto, o de la tarea, cuando considere el hito ha sucedido.
</p>
<p>
Las l�neas de nuevas revisiones que corrigen a revisiones anteriores, deben expresar el incremento, no una nueva versi�n. Es decir, si en la revisi�n 1 se planific� 10horas de un servicio ya presupuestado, y queremos planificar y presupuestar un desv�o adicional de 5 horas, aumentamos la revisi�n a "2" y a�adimos una linea adicional de cantidad=2 (y no 7).
</p> 