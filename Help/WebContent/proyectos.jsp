<h2><a name="proyectos">Proyectos. Tareas y disponibilidad.</a></h2>

<p style="BACKGROUND-COLOR: Cornsilk">
Importante: Criterio en las "horas"<br><br>
Las fechas inicio y fin tanto de tareas, programación, como calendario, diferencian el día y la hora. La hora NO se expresa de 0 a 24, si no de 0 al total de horas habiles del día (habitualmente 8). 
Por ejemplo, si queremos expresar un período que comprenda desde 1 de Enero primera hora laborable a 31 Diciembre hasta última hora laborable, expresariamos 01/01/2012 00:00:00 hasta 31/12/2012 08:00:00. 
Realmente si la hora final fuera una hora superior al nº de horas hábiles, el sistema no fallaría y seguiría computando el máximo, así un fin de período de 31/12/2012 09:00:00 computa exactamente igual que el anterior. Entonces s importante tener en cuenta que un inicio a las 0 horas no tiene un significado de cuando se comienza (que parecería de madrugada), sino relativo, significa comenzar al inicio del período hábil de ese día, y por la misma razon una finalización a la hora 8, no significa terminar a las 8 de la mañana, sino (en caso de una jornada de 8 horas) que significa ocupar todo el día en cuestión.
</p>
<p>
El orden a seguir para la introducción de los datos es:
</p>

<h3>1. Servicios ofrecidos o subcontratados</h3>
<p>
Servicio es un producto específico que se crea como un artículo más de compra o venta (en función de si lo subcontratamos u ofertamos). Es necesario por dos razones:
</p>
<ul>
	<li>Para integrar las tareas y proyectos con compras y ventas:  el servicio se compra, vende, presupuesta o entrega como un árticulo más en las líneas de los documentos.</li>
	<li>Para diferenciar las capacidades o rol de un recurso, así por ejemplo podemos definir servicios como "Ingeniería", "soldador", limpieza etc, y asociar dicho servicio como capacidad del recurso (al definir un recurso).</li>
</ul>
<p>
Posteriormente al crear las tareas, será necesario indicar qué servicio estamos demandando o planificando, programando o realizando (en parte de trabajo).
</p>
<h3>2. Calendario hábil</h3>
<p>
En un <a href="http://server.dynagent.es/help/info.jsp?clase=CALENDARIO_HÁBIL">calendario hábil</a> es recomendable crear los períodos desde el boton verde de nuevo, y no desde la tabla, ya que los campos tienen hora (ver critério de hora del principio).
</p>
<h3>3. Recurso</h3>
<p>
Una vez creado el recurso, si le hemos asignado un calendario, se crea automáticamente una <a href="http://server.dynagent.es/help/info.jsp?clase=DISPONIBILIDAD">disponibilidad</a> (visible desde el menú "Disponibilidad") para dicho recurso. No es recomendable crear disponibilidades manualmente.
</p>
<h3>4. Tareas</h3>
<p>
Las tareas representan un común de actividades (subtareas) desarrolladas por N recursos y mismas restricciones de programación: misma fecha mínima de inicio, misma fecha máxima de fin, misma prioridad y peso, y misma restricción de comenzar despues de cualquier otra tarea predecesora.
</p>
<p>
Las actividades se desglosan como subtareas, donde tan sólo se indica el servicio que clasifica cada actividad, el nº de horas, prioridad, y su contenido.
</p>
<h3>5. Programación, Dedicación común y específica</h3>
<p>
Las subtarea representan la demana o planificación de recursos a programar en fechas y ocupaciones concretas. Tres modos de planificación y programación son posibles:
</p>
<ul>
	<li>No indicar ningún recurso, y el sistema buscará el más disponible capacitado para producir cada producto/servicio de las subtareas</li>
	<li>Todas las subtareas van a ser desarrolladas por uno o varios recursos, cada uno de ellos siempre a un mismo porcentaje de dedicación (de 0 a 100) que indicariamos en la tabla "Dedicación", común al desarrollo de todas las subtareas. Si por ejemplo la suma de todas las subtareas suponen 10 horas, e indicamos una dedicación del recurso R1 al 40%, y el recurso R2 al 40%, la dedicación total es del 80% (es decir en total no llega a trabajarse el horario completo), y se tratará de asignar ambos recursos a cada tarea con dicho porcentaje. Si por el contrario las dedicaciones de R1 y R2 fueran del 80%, la dedicación total es del 160%, lo cual supone un acortamiento de tiempos respecto a un solo recurso trabajando al 100%.</li>
	<li>Habiendo o no una dedicación común, es posible especificar o forzar para una subtarea concreta un recurso concreto (que trabajará al 100% dedicación, y lo indicamos en el campo "recurso" de la subtarea). No es posible forzar un recuso X que ya aparece en la dedicación común.</li>
</ul> 
<h4>Programar la tarea: Asignación automática de recursos y disponibilidades</h4>
<p>
El sistema en todo momento trata de programar la tarea, lo que significa localizar y RESERVAR un recurso y fecha disponible para realizar la tarea lo antes posible, cumpliendo las restricciones de la tarea. En la búsqueda de recursos disponibles, el sistema siempre da
prioridad al recurso espec&iacute;fico declarado en la propia subtarea. En segunda prioridad a los recursos (opcionales) asignados en la tabla "dedicación" en la propia tarea. Si no se fuerza ningún recurso, el sistema tratará de asignar recursos miembros del proyecto de la tarea (el proyecto es opcional), y por último si no se ha impuesto ni uno ni otro, el sistema propondrá cualquier recurso disponible que tenga la misma capacidad (servicio) que el producto indicado en la subtarea.
</p>
<h4>Reprogramación simultánea de múltiples tareas y distintos proyectos</h4> 
<p>
En principio, la edición de una tarea y modificación de alguna de sus restriciones, como la prioridad o fecha de inicio, 
puede motivar una reprogramación de dicha tarea, de acuerdo a la disponibilidad libre en común a todos los proyectos y tareas, sin embargo a priori otras tareas existentes no se ven afectadas.
</p>
<p>
Una primera forma de reprogramar es mediante la edición múltiple: seleccionar varias tareas a la vez y editarlas (botón lapiz). Lo más habitual sería modificar la fecha mínima inicio para retrasar todas las tareas.
</p>
<p>
Notar que la reprogramación solo podrá asignar la ejecución en una fecha dada ya ocupada, si la tarea que ocupa dicha fecha es reprogramada a la vez, de lo contrario su ocupación permanece inalterable por mucho que otras tareas tengan mayor prioridad. Si por ejemplo tras un cambio de prioridad en una tarea X, deseamos que el sistema vuelva a reprocesar un conjunto de N tareas que pueden verse afectadas, debe seleccionar todas las tareas y ejecutar la acción "Programar de nuevo".
</p>
<h4>Seguimiento y Control de lo realizado: porcentaje de realización y partes de trabajo</h4>
<p>
El sistema por defecto supone que no hay desvios, es decir, supone que en todo momento la cantidad de horas demandadas en la subtareas debe coincidir con la suma de la cantidad programada y consumida, de manera que si no coincide aumentará o reducirá automaticamente la cantidad programada.
</p>
<p>
Existen dos formas de incrementar el porcentaje de realización de una tarea:
</p>
<ol>
	<li>Añadiendo una linea de "consumo" del tipo "Parte trabajo". En este caso se crea una línea de consumo del tipo "Parte de Trabajo" con la cantidad realizada, y se reduce automáticamente la cantidad programada.</li>
	<li>Modificando el porcentaje de realización de la línea programada (expresado de 0 100)</li>
</ol>
<p>
Caso de que se conzoca un desvio es necesario (mejor previamente) modificar la cantidad demandada en las subtareas para reflejar dicho desvio. Si se desea distinguirlo es posible asignar una revisión superior a una nueva subtarea.
</p>
<h3>6. Presupuestar y Facturar una tarea</h3>
<p>
Es posible realizar ambas acciones desde las acciones: "Generar presupuesto" y "Generar Factura" en el menú tarea. Al contrario que en el ciclo o evolución de una venta, en el que las mismas lineas de ventas se comparten entre las distintas fases o documentos, al presupuestar o facturar una tarea se copian las lineas realizadas (o líneas de consumo) de la tarea en el presupuesto o factura. En el caso de la accion de facturar, al final de la acción la tarea queda marcada como facturada.
</p>
<p> 
Si tras dicha factura queremos informar de desvios, conviente aumentar el campo ""revisión"" de la tarea, de manera que las nuevas líneas de subtarea se suscriben a la nueva revisión, siendo posible posteriormente generar un presupuesto o factura a partir de sólo la nueva revisión.
</p> 
<h3>7. Proyectos</h3>
<p>
El proyecto es un concepto que permite enlazar transversalemte distintos documentos de distinta naturaleza y fase. Así puede ser asociado tanto a compras, como ventas, como tareas. El proyecto contabiliza los costes e ingresos imputables, permite representación gantt (existe un informe llamado Gantt), asi como un cuadro de financiación.
</p>
<h3>8. Trabajar con revisiones</h3>
<p>
Las revisiones permiten diferenciar las desviaciones durante un período de tiempo, o hasta que sucera un hito como una faturación. El usuario debe aumentar el nº de revisión, o bien del proyecto, o de la tarea, cuando considere el hito ha sucedido.
</p>
<p>
Las líneas de nuevas revisiones que corrigen a revisiones anteriores, deben expresar el incremento, no una nueva versión. Es decir, si en la revisión 1 se planificó 10horas de un servicio ya presupuestado, y queremos planificar y presupuestar un desvío adicional de 5 horas, aumentamos la revisión a "2" y añadimos una linea adicional de cantidad=2 (y no 7).
</p> 