<h2><a name="configuracionavanzada">Configuraci�n avanzada</a></h2>

<p>
El software Dynagent dispone de dos modos de identificaci�n en el sistema. Modo negocio y modo configuraci�n. El modo negocio permite trabajar con la operativa de la empresa, mientras que el modo configuraci�n permite personalizar la aplicaci�n y adaptarla a nuestras necesidades. Este �ltimo modo es el que explicaremos a continuaci�n.
</p>

<h3>1. Conceptos gen�ricos</h3>
<p>
El modo configuraci�n s�lo es accesible por los usuarios con perfil administrador, permiti�ndonos hacer todo tipo de cambios que se ver�n reflejados en el modo de negocio. Estos cambios solo ser�n visibles cuando se reinicie la aplicaci�n.
La operativa que se sigue, en la mayor�a de las opciones de configuraci�n, se basa en tres conceptos:
</p>

<ul>
<li>�mbito (Acciones, Informes y Men�s)</li>
<li>Formulario</li>
<li>Propiedad (Campos y tablas de los formularios)</li>
</ul>

<p>
Sirven para definir bajo qu� condiciones aplicar� esa configuraci�n.  Es decir, si asignamos uno o varios valores en �mbito, estamos indicando que la configuraci�n solo tendr� efecto cuando estemos bajo ese �mbito (ya sea una Acci�n, un Informe o un Men�). Lo mismo ocurre si asignamos valor a Formulario o Propiedad.
</p>

<h3>2. Tipos de configuraci�n</h3>
<p>
En el modo configuraci�n accedemos al �rea funcional Configuraci�n Avanzada, que nos permite trabajar con los siguientes tipos de configuraci�n:
</p>
<div align=center>
<table border="1" width="90%">
 <tr>
  <td>
  <h3>Tipo</h3>
  </td>
  <td>
  <h3>Descripci�n</h3>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Acci�n</p>
  </td>
  <td>
  <p>Muestra todas las acciones disponibles en la aplicaci�n</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Alias</p>
  </td>
  <td>
  <p>Permite asignar un nombre distinto a una Acci�n, Informe, Men�, Formulario o Propiedad bajo ciertas condiciones</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>�rea funcional</p>
  </td>
  <td>
  <p>Desplegables en los que quedan agrupados los diferentes men�s</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Campo destacado</p>
  </td>
  <td>
  <p>Permite que una propiedad (s�lo campos, no tablas) aparezca de un mayor tama�o, debido a su importancia</p>
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
  <p>Muestra todos los formularios disponibles en la aplicaci�n</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Columnas tabla</p>
  </td>
  <td>
  <p>Permite definir las columnas que aparecen en las tablas de la aplicaci�n. En la propiedad tabla tendremos que poner el tipo de formulario de los registros de esta tabla, no el nombre de la propiedad</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Exclusi�n</p>
  </td>
  <td>
  <p>Muestra clases y propiedades que han sido excluidas debido a la configuraci�n realizada. Se trata de informaci�n de sistema, el usuario no debe cambiar nada</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Grupo campos</p>
  </td>
  <td>
  <p>Permite seleccionar propiedades para que aparezcan agrupadas cuando aparecen en alg�n formulario</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>M�scara campo</p>
  </td>
  <td>
  <p>Permite asignar una expresi�n regular a los campos que evite que el usuario escriba un valor incorrecto</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Men�</p>
  </td>
  <td>
  <p>Permite crear men�s que muestren un determinado formulario y aparezcan en el �rea funcional indicada</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>M�dulo Negocio</p>
  </td>
  <td>
  <p>Permite instalar o desinstalar manualmente los m�dulos disponibles en la aplicaci�n</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Orden relativo campos</p>
  </td>
  <td>
  <p>Permite crear un orden entre las propiedades para que unas aparezcan delante o detr�s de otras</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Perfil Usuario</p>
  </td>
  <td>
  <p>Permite crear perfiles que podr�n ser asignados a alg�n usuario y de esta manera facilitar la creaci�n de permisos por ejemplo, indicando ese perfil de usuario</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Permiso</p>
  </td>
  <td>
  <p>Permite crear permisos que apliquen a una Acci�n, Informe, Men�, Formulario o Propiedad. Estos permisos son de asignaci�n y denegaci�n, permitiendo aplicar distintas prioridades</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Propiedad</p>
  </td>
  <td>
  <p>Muestra todos los campos y tablas de la aplicaci�n</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Tama�o tabla</p>
  </td>
  <td>
  <p>Elecci�n del n�mero de filas que aparece en cada tabla sin necesidad de usar el scroll. En la propiedad tabla tendremos que poner el tipo de formulario de los registros de esta tabla, no el nombre de la propiedad</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Valor por defecto</p>
  </td>
  <td>
  <p>Definici�n de un valor por defecto en un campo cuando se cree un objeto nuevo</p>
  </td>
 </tr>
 <tr>
  <td> 
  <p>Versi�n Modelo</p>
  </td>
  <td>
  <p>Muestra las distintas versiones del modelo que han sido instaladas en el sistema</p>
  </td>
 </tr>
</table>
</div>

<h3>3. Ejemplo de configuraci�n</h3>
<p>
Veamos a continuaci�n un ejemplo de creaci�n de un permiso, que puede ser extrapolado a la creaci�n de otro tipo de objeto.
Si quisi�ramos, bajo el men� Cliente y el formulario Cliente_Particular, evitar que la propiedad fecha_alta pueda ser modificada por un usuario. Para ello, en el men� Permiso, crear�amos un objeto del tipo Permiso Propiedad, y rellenar�amos los datos de la siguiente manera:
</p>

<div align=center>
	<img border="1" src="images/permiso_propiedad.jpg" alt="Creaci�n de permiso propiedad" />
</div>

<p>
Dar valor a la propiedad �mbito, en este caso, es opcional. Si quisi�ramos que esto aplicar� en todos los formularios Cliente_Particular sea cual sea su Men�, dejar�amos vac�o el valor de �mbito.
Lo mismo ocurre para Formulario, si queremos que aplique a todas las propiedades fecha_de_alta sea cual sea el formulario en el que se encuentre y su �mbito, s�lo le dar�amos valor a Propiedad.
</p>


<p style="BACKGROUND-COLOR: Cornsilk">
Importante: Hay que tener en cuenta que los nombres de �mbito, formulario y propiedad a utilizar, ser�n los nombres originales, sin tener en cuenta alias.
</p>

<p>
En caso de que no encuentre el valor que busca, deber� comprobar, accediendo al formulario de b�squeda del men� Alias, si el nombre que desea utilizar es en realidad un alias. Si este fuera el caso, deber� ver cu�l es el nombre original y utilizarlo. Veamos un ejemplo:
Imaginemos que queremos utilizar la propiedad c�digo, pero vemos que no aparece en la lista de posibilidades.
Esto puede ocurrir por dos razones: que esa propiedad realmente no existe en el formulario indicado, o porque se trata de un alias.
As� que, vamos al men� Alias, y hacemos una b�squeda escribiendo el valor c�digo en el campo alias.
Vemos que aparece un resultado, que tiene en la columna propiedad el valor rdn. Esto nos indica que efectivamente existe un alias c�digo, y que la propiedad original es rdn.
Por lo tanto, volvemos al formulario donde quer�amos asignar la propiedad c�digo y asignamos en su lugar la propiedad rdn. Lo mismo puede ocurrir con �mbito y formulario.
</p>

<h3>4. Asistente de clasificaci�n de negocio</h3>

<p>
La primera vez que accedamos a la aplicaci�n en modo configuraci�n, nos saldr� un asistente que nos permitir� adaptar la aplicaci�n a nuestro negocio.
Mediante una serie de pasos a rellenar, indicaremos, el sector de nuestra empresa (por ejemplo Alimentaci�n), si nuestra empresa tiene fabricaci�n propia o si trabaja con comerciales y distribuidores, entre otras cosas.
</p>

<div align=center>
	<img border="1" src="images/configuracion_inicial.jpg" alt="Primer paso del clasificador" />
</div>

<p>
Una vez rellenada toda la informaci�n necesaria, autom�ticamente ser� configurada la aplicaci�n acorde a nuestro negocio y se nos preguntar� si se quiere reiniciar la aplicaci�n en modo negocio para acceder a esa nueva configuraci�n, o si por el contrario queremos permanecer en modo configuraci�n para realizar alg�n cambio adicional.
Si queremos volver a ejecutar el asistente de clasificaci�n de negocio bastar� con pulsar el bot�n que hay en la barra de tareas superior:
</p>

<div align=center>
	<img border="1" src="images/boton_clasificador.jpg" alt="Bot�n para ejecuci�n del clasificador" />
</div>

<p>
Incluso si quisi�ramos directamente instalar o desinstalar alguno de los m�dulos instalados por el asistente, podr�amos ir al men� M�dulos, seleccionar los m�dulos sobre los que queremos actuar y pulsar en la acci�n Instalar o Desinstalar.
</p>

<h3>5. Personalizaci�n est�tica de informes</h3>
<p>
Accediendo al men� Informe podemos acceder a los informes disponibles en la aplicaci�n y cambiar aspectos relativos a su impresi�n o a su formato, entre otras cosas. Pero aparte de esas configuraciones, podemos descargarnos los dise�os de los informes o subfinformes y modificarlos est�ticamente.
</p>
<p>
Para ello, en la propiedad archivo, dentro del formulario Informe, seleccionar�amos la opci�n Guardar, y al almacenarlo en nuestro disco local podremos abrirlo con iReport 3.7.4 (importante que sea esta versi�n), modificarlo y volver a subirlo con la opci�n Buscar en dicho propiedad archivo. Si es un subinforme lo que queremos modificar, ser�a la misma operativa pero tendr�amos que hacerlo dentro de los valores de la tabla subinforme que hay en el formulario Informe.
</p>
<p>
La versi�n correcta del programa iReport para Windows podemos obtenerla desde <a target="_blank" href="http://jasperforge.org/uploads/publish/ireportwebsite/IR%20Website/nb-3.7.4.html">http://jasperforge.org/uploads/publish/ireportwebsite/IR%20Website/nb-3.7.4.html</a> usando la opci�n Download iReport (standalone version).
</p>
