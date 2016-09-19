<h2><a name="configuracionavanzada">Configuración avanzada</a></h2>

<p>
El software Dynagent dispone de dos modos de identificación en el sistema. Modo negocio y modo configuración. El modo negocio permite trabajar con la operativa de la empresa, mientras que el modo configuración permite personalizar la aplicación y adaptarla a nuestras necesidades. Este último modo es el que explicaremos a continuación.
</p>

<h3>1. Conceptos genéricos</h3>
<p>
El modo configuración sólo es accesible por los usuarios con perfil administrador, permitiéndonos hacer todo tipo de cambios que se verán reflejados en el modo de negocio. Estos cambios solo serán visibles cuando se reinicie la aplicación.
La operativa que se sigue, en la mayoría de las opciones de configuración, se basa en tres conceptos:
</p>

<ul>
<li>Ámbito (Acciones, Informes y Menús)</li>
<li>Formulario</li>
<li>Propiedad (Campos y tablas de los formularios)</li>
</ul>

<p>
Sirven para definir bajo qué condiciones aplicará esa configuración.  Es decir, si asignamos uno o varios valores en Ámbito, estamos indicando que la configuración solo tendrá efecto cuando estemos bajo ese ámbito (ya sea una Acción, un Informe o un Menú). Lo mismo ocurre si asignamos valor a Formulario o Propiedad.
</p>

<h3>2. Tipos de configuración</h3>
<p>
En el modo configuración accedemos al área funcional Configuración Avanzada, que nos permite trabajar con los siguientes tipos de configuración:
</p>
<div align=center>
<table border="1" width="90%">
 <tr>
  <td>
  <h3>Tipo</h3>
  </td>
  <td>
  <h3>Descripción</h3>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Acción</p>
  </td>
  <td>
  <p>Muestra todas las acciones disponibles en la aplicación</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Alias</p>
  </td>
  <td>
  <p>Permite asignar un nombre distinto a una Acción, Informe, Menú, Formulario o Propiedad bajo ciertas condiciones</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Área funcional</p>
  </td>
  <td>
  <p>Desplegables en los que quedan agrupados los diferentes menús</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Campo destacado</p>
  </td>
  <td>
  <p>Permite que una propiedad (sólo campos, no tablas) aparezca de un mayor tamaño, debido a su importancia</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Campo requerido</p>
  </td>
  <td>
  <p>Permite que una propiedad sea obligatoriamente rellenada por el usuario al completar el formulario en el que aparece</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Clase</p>
  </td>
  <td>
  <p>Muestra todos los formularios disponibles en la aplicación</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Columnas tabla</p>
  </td>
  <td>
  <p>Permite definir las columnas que aparecen en las tablas de la aplicación. En la propiedad tabla tendremos que poner el tipo de formulario de los registros de esta tabla, no el nombre de la propiedad</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Exclusión</p>
  </td>
  <td>
  <p>Muestra clases y propiedades que han sido excluidas debido a la configuración realizada. Se trata de información de sistema, el usuario no debe cambiar nada</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Grupo campos</p>
  </td>
  <td>
  <p>Permite seleccionar propiedades para que aparezcan agrupadas cuando aparecen en algún formulario</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Máscara campo</p>
  </td>
  <td>
  <p>Permite asignar una expresión regular a los campos que evite que el usuario escriba un valor incorrecto</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Menú</p>
  </td>
  <td>
  <p>Permite crear menús que muestren un determinado formulario y aparezcan en el área funcional indicada</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Módulo Negocio</p>
  </td>
  <td>
  <p>Permite instalar o desinstalar manualmente los módulos disponibles en la aplicación</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Orden relativo campos</p>
  </td>
  <td>
  <p>Permite crear un orden entre las propiedades para que unas aparezcan delante o detrás de otras</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Perfil Usuario</p>
  </td>
  <td>
  <p>Permite crear perfiles que podrán ser asignados a algún usuario y de esta manera facilitar la creación de permisos por ejemplo, indicando ese perfil de usuario</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Permiso</p>
  </td>
  <td>
  <p>Permite crear permisos que apliquen a una Acción, Informe, Menú, Formulario o Propiedad. Estos permisos son de asignación y denegación, permitiendo aplicar distintas prioridades</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Propiedad</p>
  </td>
  <td>
  <p>Muestra todos los campos y tablas de la aplicación</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Tamaño tabla</p>
  </td>
  <td>
  <p>Elección del número de filas que aparece en cada tabla sin necesidad de usar el scroll. En la propiedad tabla tendremos que poner el tipo de formulario de los registros de esta tabla, no el nombre de la propiedad</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Valor por defecto</p>
  </td>
  <td>
  <p>Definición de un valor por defecto en un campo cuando se cree un objeto nuevo</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Versión Modelo</p>
  </td>
  <td>
  <p>Muestra las distintas versiones del modelo que han sido instaladas en el sistema</p>
  </td>
 </tr>
</table>
</div>

<h3>3. Ejemplo de configuración</h3>
<p>
Veamos a continuación un ejemplo de creación de un permiso, que puede ser extrapolado a la creación de otro tipo de objeto.
Si quisiéramos, bajo el menú Cliente y el formulario Cliente_Particular, evitar que la propiedad fecha_alta pueda ser modificada por un usuario. Para ello, en el menú Permiso, crearíamos un objeto del tipo Permiso Propiedad, y rellenaríamos los datos de la siguiente manera:
</p>

<div align=center>
	<img border="1" src="images/permiso_propiedad.jpg" alt="Creación de permiso propiedad" />
</div>

<p>
Dar valor a la propiedad Ámbito, en este caso, es opcional. Si quisiéramos que esto aplicará en todos los formularios Cliente_Particular sea cual sea su Menú, dejaríamos vacío el valor de Ámbito.
Lo mismo ocurre para Formulario, si queremos que aplique a todas las propiedades fecha_de_alta sea cual sea el formulario en el que se encuentre y su ámbito, sólo le daríamos valor a Propiedad.
</p>


<p style="BACKGROUND-COLOR: Cornsilk">
Importante: Hay que tener en cuenta que los nombres de ámbito, formulario y propiedad a utilizar, serán los nombres originales, sin tener en cuenta alias.
</p>

<p>
En caso de que no encuentre el valor que busca, deberá comprobar, accediendo al formulario de búsqueda del menú Alias, si el nombre que desea utilizar es en realidad un alias. Si este fuera el caso, deberá ver cuál es el nombre original y utilizarlo. Veamos un ejemplo:
Imaginemos que queremos utilizar la propiedad código, pero vemos que no aparece en la lista de posibilidades.
Esto puede ocurrir por dos razones: que esa propiedad realmente no existe en el formulario indicado, o porque se trata de un alias.
Así que, vamos al menú Alias, y hacemos una búsqueda escribiendo el valor código en el campo alias.
Vemos que aparece un resultado, que tiene en la columna propiedad el valor rdn. Esto nos indica que efectivamente existe un alias código, y que la propiedad original es rdn.
Por lo tanto, volvemos al formulario donde queríamos asignar la propiedad código y asignamos en su lugar la propiedad rdn. Lo mismo puede ocurrir con ámbito y formulario.
</p>

<h3>4. Asistente de clasificación de negocio</h3>

<p>
La primera vez que accedamos a la aplicación en modo configuración, nos saldrá un asistente que nos permitirá adaptar la aplicación a nuestro negocio.
Mediante una serie de pasos a rellenar, indicaremos, el sector de nuestra empresa (por ejemplo Alimentación), si nuestra empresa tiene fabricación propia o si trabaja con comerciales y distribuidores, entre otras cosas.
</p>

<div align=center>
	<img border="1" src="images/configuracion_inicial.jpg" alt="Primer paso del clasificador" />
</div>

<p>
Una vez rellenada toda la información necesaria, automáticamente será configurada la aplicación acorde a nuestro negocio y se nos preguntará si se quiere reiniciar la aplicación en modo negocio para acceder a esa nueva configuración, o si por el contrario queremos permanecer en modo configuración para realizar algún cambio adicional.
Si queremos volver a ejecutar el asistente de clasificación de negocio bastará con pulsar el botón que hay en la barra de tareas superior:
</p>

<div align=center>
	<img border="1" src="images/boton_clasificador.jpg" alt="Botón para ejecución del clasificador" />
</div>

<p>
Incluso si quisiéramos directamente instalar o desinstalar alguno de los módulos instalados por el asistente, podríamos ir al menú Módulos, seleccionar los módulos sobre los que queremos actuar y pulsar en la acción Instalar o Desinstalar.
</p>

<h3>5. Personalización estética de informes</h3>
<p>
Accediendo al menú Informe podemos acceder a los informes disponibles en la aplicación y cambiar aspectos relativos a su impresión o a su formato, entre otras cosas. Pero aparte de esas configuraciones, podemos descargarnos los diseños de los informes o subfinformes y modificarlos estéticamente.
</p>
<p>
Para ello, en la propiedad archivo, dentro del formulario Informe, seleccionaríamos la opción Guardar, y al almacenarlo en nuestro disco local podremos abrirlo con iReport 3.7.4 (importante que sea esta versión), modificarlo y volver a subirlo con la opción Buscar en dicho propiedad archivo. Si es un subinforme lo que queremos modificar, sería la misma operativa pero tendríamos que hacerlo dentro de los valores de la tabla subinforme que hay en el formulario Informe.
</p>
<p>
La versión correcta del programa iReport para Windows podemos obtenerla desde <a target="_blank" href="http://jasperforge.org/uploads/publish/ireportwebsite/IR%20Website/nb-3.7.4.html">http://jasperforge.org/uploads/publish/ireportwebsite/IR%20Website/nb-3.7.4.html</a> usando la opción Download iReport (standalone version).
</p>
