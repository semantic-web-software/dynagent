<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@page import="dynagent.common.Constants"%>
<%@page import="java.util.*,java.io.*,java.lang.*,java.sql.*,javax.naming.InitialContext,javax.sql.DataSource,java.sql.Date,dynagent.common.utils.*"%>

<%
response.setHeader("Cache-Control","no-cache"); 
response.setHeader("Pragma","no-cache"); 
response.setDateHeader ("Expires", -1);
%>

<html>
	<script src="checkJava.vbs"></script>
	<head>
		<meta http-equiv="Content-Type"	content="text/html; charset=iso-8859-1">
		<meta name="GENERATOR" content="Microsoft FrontPage Express 2.0">
		<meta http-equiv="PRAGMA" content="NO-CACHE"> 
		<meta http-equiv="Expires" content="-1">
		<title>DYNAGENT</title>
	</head>

	<%
        String name=request.getParameter("name");
		String memory=request.getParameter("memory");
		if(memory==null || memory.equalsIgnoreCase("null")){
			memory="1350";
		}
		
		InitialContext ic = new InitialContext();
		DataSource ds = (DataSource)ic.lookup("java:jdbc/dynaglobal" );
		java.sql.Connection conn= ds.getConnection();
		conn.setAutoCommit(true);
		Statement st= conn.createStatement();
		ResultSet set=st.executeQuery("SELECT business,login,password,rules,type,date FROM customer WHERE name='"+name+"'");
		if(set.next()){
			int business=set.getInt("business");
			String login=set.getString("login");
			String password=set.getString("password");
			String rules=set.getString("rules");
			
			st.close();
			conn.close();
			set.close();
			
			ds = (DataSource)ic.lookup("java:jdbc/dyna"+business );
			conn= ds.getConnection();
			conn.setAutoCommit(true);
			st= conn.createStatement();
			
			String modes=null;
			if(Auxiliar.equals("demo",name.toLowerCase())){
				modes="Negocio";
			}else{
				set=st.executeQuery("SELECT \"módulos_configurados\" FROM \"aplicación\"");
				modes="Configuración";
				if(set.next()){
					boolean configured=set.getBoolean("módulos_configurados");
					if(configured){
						modes+="Negocio";
					}
				}else{
					modes+="Negocio";
				}
			}
    %>
    
	<script type='text/JavaScript'>
		var a=navigator.userAgent.toLowerCase();

		var isOpera=(a.indexOf("opera")!=-1);
		var isKonq=(a.indexOf('konqueror')!=-1);
		var isSafari=(a.indexOf('safari')!=-1)&&(a.indexOf('mac')!=-1);
		var isKhtml=isSafari||isKonq;
		var isIE=(a.indexOf("msie")!=-1)&&!isOpera;
		var isAtLeastIE11 = !!(navigator.userAgent.match(/Trident/) && !navigator.userAgent.match(/MSIE/));
		isIE=isIE||isAtLeastIE11;
		var isFF=(a.indexOf('firefox')!=-1);
		var isWinFF=isFF&&(a.indexOf("win")!=-1);
		var isWinIE=isIE&&(a.indexOf("win")!=-1);
		var isCSS1Compat=(!isIE)||(document.compatMode&&document.compatMode=="CSS1Compat");

		if (isWinIE)
		{
			var hayJava = JPlug.isEnabled();
			if (hayJava != null) {
				if (!hayJava)
				   if(confirm("No tiene instalado JAVA o está deshabilitado en su navegador. Es necesario para ejecutar la aplicación. ¿Desea descargarlo?"))
						window.open('http://www.java.com/es/download/index.jsp','_self');
			}else{
				if (!navigator.javaEnabled()){
					if(confirm("No tiene instalado JAVA o está deshabilitado en su navegador. Es necesario para ejecutar la aplicación. ¿Desea descargarlo?")){
						window.open('http://www.java.com/es/download/index.jsp','_self');
					}
				}
			}
		}else{
			if (!navigator.javaEnabled()){
				if(confirm("No tiene instalado JAVA o está deshabilitado en su navegador. Es necesario para ejecutar la aplicación. ¿Desea descargarlo?")){
					window.open('http://www.java.com/es/download/index.jsp','_self');
				}
			}
		}
	 </script>
 
	<body bgcolor="#FFFFFF" TOPMARGIN=0 LEFTMARGIN=0 MARGINHEIGHT=0 MARGINWIDTH=0 scroll=no>
		<!--width=100% height=100%-->

		<applet id="applet" code="dynagent.gui.dynaApplet.class"
			width=100% height=100%
			codebase="bin" 
			ALIGN=LEFT 
			BORDER=0 
			VSPACE=0 
			HSPACE=0 
			archive="dynagent.jar, dynagent-common.jar"><!-- Añado tambien common porque se utiliza en dynaApplet y asi evitamos un tiempo con la pantalla en blanco-->
			<!-- archive="dynagent.jar, jdom.jar, dynagent-common.jar, dynagent-framework.jar, dynagent-ruleengine.jar, elecom.jar, commons-lang-2.3.jar, drools-core-4.0.7.jar, mvel-1.3.1-java1.4.jar, tinylaf.jar, dynagent-calendar.jar, jasperreports-3.7.3.jar, commons-logging-1.1.1.jar, apache-mime4j-0.6.jar, commons-codec-1.3.jar, httpclient-4.0.3.jar, httpcore-4.0.1.jar, httpmime-4.0.3.jar">-->
			<!-- archive="dynagent.jar, jdom.jar, dynagent-common.jar, dynagent-framework.jar, dynagent-ruleengine.jar, elecom.jar, commons-lang-2.3.jar, commons-fileupload-1.0.jar, jndi-1.2.1.jar, xercesImpl.jar, commons-beanutils-1.7.jar, commons-collections-2.1.jar, commons-digester-1.7.jar, xml-apis.jar, drools-core-4.0.7.jar, mvel-1.3.1-java1.4.jar, antlr-runtime.jar, tinylaf.jar, dynagent-calendar.jar, jasperreports-3.7.3.jar, commons-logging-1.0.2.jar, apache-mime4j-0.6.jar, commons-codec-1.3.jar, httpclient-4.0.3.jar, httpcore-4.0.1.jar, httpmime-4.0.3.jar">-->

			<param name="java_arguments" value="-Xmx<%=memory%>m">
			<param name="memoryOptions" value="1350;950;450">
			<param name="separate_jvm" value="true" />
			<param name="urlDownloadJava" value="http://www.java.com/es/download/index.jsp">
			<param name="logo" value="Dynagent">
			<param name="hideHistoryDDBB" value="false">
			<param name="configurationRules" value="configurationRules.dpkg">
			<param name="bns" value="<%=business%>">
			<param name="modes" value="<%=modes%>">
			<%
			if(rules!=null){
			%>
			<param name="rules" value="<%=rules%>">
			<%}
			if(login!=null){
			%>
			<param name="login" value="<%=login%>">
			<%}
			if(password!=null){
			%>
			<param name="password" value="<%=password%>">
			<%}
			%>
		</applet>
	</body>

<%
		}else{
%>

	<p align=center style='text-align:center'>
		<b><span style='font-size:15.0pt;font-family:"Comic Sans MS"'>Lo sentimos pero la página a la que desea acceder no existe. Compruebe que la dirección introducida es correcta.</span></b>
	</p>

	<p align=center style='text-align:center'>
		<b><span lang=EN-US style='font-size:11.0pt;font-family:"Comic Sans MS"'>(c)Copyright Dynagent Software 2007-2012. All rights reserved. </span>
		</b><span lang=EN-US style='font-size:11.0pt;font-family:"Comic Sans MS"'><a href="http://www.dynagent.es">www.dynagent.es</a></span>
	</p>

<%
		}
		set.close();
		st.close();
		conn.close();
%>

</html>