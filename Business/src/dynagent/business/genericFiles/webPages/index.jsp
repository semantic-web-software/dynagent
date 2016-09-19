<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="GENERATOR" content="Microsoft FrontPage Express 2.0">
	<title>DYNAGENT</title>
	<style>
		<!--
		 /* Font Definitions */
		 @font-face
			{font-family:Wingdings;
			panose-1:5 0 0 0 0 0 0 0 0 0;}
		@font-face
			{font-family:"Cambria Math";
			panose-1:2 4 5 3 5 4 6 3 2 4;}
		@font-face
			{font-family:Calibri;
			panose-1:2 15 5 2 2 2 4 3 2 4;}
		@font-face
			{font-family:Tahoma;
			panose-1:2 11 6 4 3 5 4 4 2 4;}
		@font-face
			{font-family:"Comic Sans MS";
			panose-1:3 15 7 2 3 3 2 2 2 4;}
		 /* Style Definitions */
		 p.MsoNormal, li.MsoNormal, div.MsoNormal
			{margin-top:0cm;
			margin-right:0cm;
			margin-bottom:10.0pt;
			margin-left:0cm;
			line-height:115%;
			font-size:11.0pt;
			font-family:"Calibri","sans-serif";}
		a:link, span.MsoHyperlink
			{color:blue;
			text-decoration:underline;}
		a:visited, span.MsoHyperlinkFollowed
			{color:purple;
			text-decoration:underline;}
		p
			{margin-right:0cm;
			margin-left:0cm;
			font-size:12.0pt;
			font-family:"Times New Roman","serif";}
		p.MsoAcetate, li.MsoAcetate, div.MsoAcetate
			{mso-style-link:"Texto de globo Car";
			margin:0cm;
			margin-bottom:.0001pt;
			font-size:8.0pt;
			font-family:"Tahoma","sans-serif";}
		p.MsoListParagraph, li.MsoListParagraph, div.MsoListParagraph
			{margin-top:0cm;
			margin-right:0cm;
			margin-bottom:10.0pt;
			margin-left:36.0pt;
			line-height:115%;
			font-size:11.0pt;
			font-family:"Calibri","sans-serif";}
		p.MsoListParagraphCxSpFirst, li.MsoListParagraphCxSpFirst, div.MsoListParagraphCxSpFirst
			{margin-top:0cm;
			margin-right:0cm;
			margin-bottom:0cm;
			margin-left:36.0pt;
			margin-bottom:.0001pt;
			line-height:115%;
			font-size:11.0pt;
			font-family:"Calibri","sans-serif";}
		p.MsoListParagraphCxSpMiddle, li.MsoListParagraphCxSpMiddle, div.MsoListParagraphCxSpMiddle
			{margin-top:0cm;
			margin-right:0cm;
			margin-bottom:0cm;
			margin-left:36.0pt;
			margin-bottom:.0001pt;
			line-height:115%;
			font-size:11.0pt;
			font-family:"Calibri","sans-serif";}
		p.MsoListParagraphCxSpLast, li.MsoListParagraphCxSpLast, div.MsoListParagraphCxSpLast
			{margin-top:0cm;
			margin-right:0cm;
			margin-bottom:10.0pt;
			margin-left:36.0pt;
			line-height:115%;
			font-size:11.0pt;
			font-family:"Calibri","sans-serif";}
		span.TextodegloboCar
			{mso-style-name:"Texto de globo Car";
			mso-style-link:"Texto de globo";
			font-family:"Tahoma","sans-serif";}
		.MsoPapDefault
			{margin-bottom:10.0pt;
			line-height:115%;}
		@page Section1
			{size:595.3pt 841.9pt;
			margin:70.85pt 3.0cm 70.85pt 3.0cm;}
		div.Section1
			{page:Section1;}
		 /* List Definitions */
		 ol
			{margin-bottom:0cm;}
		ul
			{margin-bottom:0cm;}
		-->
	</style>

	</head>

<%
	String memory=request.getParameter("memory");
	String params="name="+request.getParameter("name");
	if(memory!=null){
		params=params+"&memory="+memory;
	}
%>
	<script type='text/JavaScript'>

		var resultWindow;

		function load(){
		 if (!navigator.javaEnabled()){
			if(confirm("Debe tener instalado JAVA para ejecutar la aplicación. ¿Desea descargarlo?")){
				window.open('http://www.java.com/es/download/index.jsp','_self');
			}
		 }else{
			//Primero intentamos crear una ventana vacia con ese nombre. Si nos deja createdWindow.location no tendra entry.html.
			//Si si lo tiene significa que ya existia esa ventana por lo que en ese caso no creamos de nuevo la ventana ya que machariamos lo que se estuviera haciendo en ella.
			//Esto lo hemos añadido ya que firefox 4 cierra las ventanas guardando las pestañas y luego al abrir de nuevo el navegador vuelve a cargar esas pestañas
			createdWindow=window.open('', 'dynagentWindow<%=request.getParameter("name")%>', 'top=0,left=0,width='+(screen.availWidth)+',height ='+(screen.availHeight)+',toolbar=0,location=0,directories=0,status=0,menubar=0,resizable=1,scrolling=0,scrollbars=0');
			var str=String(createdWindow.location);
			if(str.indexOf('entry.jsp')==-1){

				resultWindow=window.open('./entry.jsp?<%=params%>', 'dynagentWindow<%=request.getParameter("name")%>', 'top=0,left=0,width='+(screen.availWidth)+',height ='+(screen.availHeight)+',toolbar=0,location=0,directories=0,status=0,menubar=0,resizable=1,scrolling=0,scrollbars=0');

				//Comprobamos que la ventana no haya sido bloqueada por un bloqueador de ventanas emergentes
				if(resultWindow==null || resultWindow=='undefined'){
					displayConfirm();
				}else{
					//Necesitamos hacer esto ya que cuando se trata de CROME resultWindow no es nulo aunque haya sido bloqueada
					setTimeout('testPopup()',3000);
				}	
			}
		 }
		}

		function testPopup()
			{
			if(resultWindow)
			{
				if(resultWindow.innerHeight==0)
				{
					displayConfirm();
				}
			}
			}
		
		function displayConfirm(){
			var answer = confirm ("Hay activo un bloqueador de ventanas que ha impedido la ejecución a pantalla completa. ¿Desea abrir la aplicación en esta misma ventana?")
			if (answer){
				window.open('./entry.jsp?name=<%=request.getParameter("name")%>', '_self');
				if(resultWindow)
					result.close();
			}
		}
	</script>

	<body bgcolor="#FFFFFF" TOPMARGIN=0 LEFTMARGIN=0 MARGINHEIGHT=0 MARGINWIDTH=0 scroll=no onload="load();">

		<div class=Section1>
			<br/>
			<p class=MsoNormal align=center style='text-align:center'>
				<img width=711 height=157 id="0 Imagen" src="images/Dynagent.gif" alt=Dynagent.gif>
			</p>

			<p class=MsoNormal align=center style='text-align:center'>&nbsp;</p>

			<p class=MsoNormal>&nbsp;</p>

			<div align=center>

				<table class=MsoTableGrid border=0 cellspacing=0 cellpadding=0 style='border-collapse:collapse;border:none'>
					<tr>
						<td width=576 valign=top style='width:432.2pt;padding:0cm 5.4pt 0cm 5.4pt'>
							<p class=MsoNormal style='margin-bottom:0cm;margin-bottom:.0001pt;line-height:normal'>
								<span style='font-size:14.0pt;font-family:"Comic Sans MS"'>La aplicación ha sido ejecutada en otra ventana del navegador.</span>
							</p>
							<p class=MsoNormal style='margin-bottom:0cm;margin-bottom:.0001pt;line-height:normal'>
								<span style='font-size:14.0pt'>&nbsp;</span>
							</p>
						</td>
					</tr>
					<tr>
						<td width=576 valign=top style='width:432.2pt;padding:0cm 5.4pt 0cm 5.4pt'>
							<p class=MsoNormal style='margin-bottom:0cm;margin-bottom:.0001pt;line-height:normal'>
								<span style='font-size:14.0pt;font-family:"Comic Sans MS"'>Si la ventana no se ha abierto asegúrese de tener deshabilitado el bloqueador de	ventanas emergentes(pop-up) del navegador y de las barras de tareas.
								Además, asegúrese de no tener ya abierta otra ventana de la aplicación, ya que en ese caso no se abrirá una nueva.</span>
							</p>
							<p class=MsoNormal style='margin-bottom:0cm;margin-bottom:.0001pt;line-height:normal'>
								<span style='font-size:14.0pt'>&nbsp;</span>
							</p>
						</td>
					</tr>
					<tr>
						<td width=576 valign=top style='width:432.2pt;padding:0cm 5.4pt 0cm 5.4pt'>
							<p class=MsoNormal style='margin-bottom:0cm;margin-bottom:.0001pt;line-height:normal'>
								<span style='font-size:14.0pt;font-family:"Comic Sans MS"'>Si tiene	alguna duda contacte a través del siguiente <a href="mailto:dynagent@dynagent.eu">e-mail</a></span>
							</p>
						</td>
					</tr>
				</table>

			</div>

			<p class=MsoNormal><span style='font-size:14.0pt;line-height:115%'>&nbsp;</span></p>

			<p style='margin-left:318.6pt'><span style='font-size:14.0pt;font-family:"Comic Sans MS"'>&nbsp;</span></p>

			<p style='margin-left:318.6pt'><span style='font-family:"Comic Sans MS"'>&nbsp;</span></p>

			<p align=center style='text-align:center'>
				<b><span lang=EN-US style='font-size:11.0pt;font-family:"Comic Sans MS"'>(c)Copyright Dynagent Software 2007-2011. All rights reserved. </span></b>
				<span lang=EN-US style='font-size:11.0pt;font-family:"Comic Sans MS"'><a href="http://www.dynagent.es">www.dynagent.es</a></span>
			</p>

			<p class=MsoNormal><span lang=EN-US>&nbsp;</span></p>

		</div>
	</body>
</html>