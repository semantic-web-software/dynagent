<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dynagent.common.Constants"%>
<%@page import="java.util.*,java.io.*,java.lang.*,java.sql.*,javax.naming.InitialContext,javax.sql.DataSource,java.sql.Date,dynagent.common.utils.*"%>

<%
response.setHeader("Cache-Control","no-cache"); 
response.setHeader("Pragma","no-cache"); 
response.setDateHeader ("Expires", 0);

String name=request.getParameter("name");

String memory=request.getParameter("memory");
if(memory==null || memory.equalsIgnoreCase("null")){
	memory="750";
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
	if(login==null){
		login="-";
	}
	String password=set.getString("password");
	if(password==null){
		password="-";
	}	
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
	
	
	//Asignamos lo necesario para enviar un jnlp
	response.setHeader("Content-Disposition", "filename=\"webstart.jnlp\";");
	response.setContentType("application/x-java-jnlp-file");

	String path=request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() + "/dyna/bin/";
	
	System.err.println("CONTEXTO:"+path);
%>

<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+" codebase="<%=path%>" >
  <information>
    <title>Dynagent ERP</title>
    <vendor>Dynagent</vendor>
	<homepage href="http://www.dynagent.es"/>
    <icon href="http://www.dynagent.es/dynagent_logo.gif" kind="default"/>
    <icon href="http://www.dynagent.es/dynagent_logo_web.jpg" height="400" width="600" kind="splash"/>
	<description>Dynagent ERP</description>
    <description kind="short">Iniciar Dynagent</description>
	<shortcut online="true" install="true">
      <desktop />
      <menu submenu="Dynagent" />
    </shortcut>
  </information>
  <security>
    <all-permissions/>
 </security>
 <update check="always" policy="always"/>
  <resources>
    <!-- Application Resources -->
    <j2se version="1.6+" href="http://java.sun.com/products/autodl/j2se" max-heap-size="<%=memory%>m"/>
    <jar href="dynagent.jar" main="true" download="eager"/>
	<jar href="dynagent-calendar.jar" main="false" download="eager"/>
	<jar href="elecom.jar" main="false" download="eager"/>
	<jar href="dynagent-common.jar" main="false" download="eager"/>
	<jar href="dynagent-framework.jar" main="false" download="eager"/>
	<jar href="dynagent-ruleengine.jar" main="false" download="eager"/>
	<jar href="commons-collections-2.1.jar" main="false" download="eager"/>
	<jar href="commons-digester-1.7.jar" main="false" download="eager"/>
	<jar href="commons-lang-2.3.jar" main="false" download="eager"/>
	<jar href="jdom.jar" main="false" download="eager"/>
	<jar href="drools-core-4.0.7.jar" main="false" download="eager"/>
	<jar href="mvel-1.3.1-java1.4.jar" main="false" download="eager"/>
	<jar href="tinylaf.jar" main="false" download="eager"/>
	<jar href="jasperreports-3.7.3.jar" main="false" download="eager"/>
	<jar href="commons-logging-1.1.1.jar" main="false" download="eager"/>
	<jar href="apache-mime4j-0.6.jar" main="false" download="eager"/>
	<jar href="commons-codec-1.3.jar" main="false" download="eager"/>
	<jar href="httpclient-4.0.3.jar" main="false" download="eager"/>
	<jar href="httpcore-4.0.1.jar" main="false" download="eager"/>
	<jar href="httpmime-4.0.3.jar" main="false" download="eager"/>
	<jar href="batik-anim.jar" main="false" download="eager"/>
	<jar href="batik-awt-util.jar" main="false" download="eager"/>
	<jar href="batik-bridge.jar" main="false" download="eager"/>
	<jar href="batik-codec.jar" main="false" download="eager"/>
	<jar href="batik-css.jar" main="false" download="eager"/>
	<jar href="batik-dom.jar" main="false" download="eager"/>
	<jar href="batik-ext.jar" main="false" download="eager"/>
	<jar href="batik-extension.jar" main="false" download="eager"/>
	<jar href="batik-gui-util.jar" main="false" download="eager"/>
	<jar href="batik-gvt.jar" main="false" download="eager"/>
	<jar href="batik-parser.jar" main="false" download="eager"/>
	<jar href="batik-script.jar" main="false" download="eager"/>
	<jar href="batik-svg-dom.jar" main="false" download="eager"/>
	<jar href="batik-svggen.jar" main="false" download="eager"/>
	<jar href="batik-swing.jar" main="false" download="eager"/>
	<jar href="batik-transcoder.jar" main="false" download="eager"/>
	<jar href="batik-util.jar" main="false" download="eager"/>
	<jar href="batik-xml.jar" main="false" download="eager"/>
	<jar href="xml-apis-ext.jar" main="false" download="eager"/>
	<jar href="poi-3.6-20091214.jar" main="false" download="eager"/>
	<jar href="saxon9-jdom.jar" main="false" download="eager"/>
	<jar href="saxon9.jar" main="false" download="eager"/>                      
  </resources>
  
 <application-desc
    name="Dynagent Application"
    main-class="dynagent.gui.dynaApplet">
	<argument>-codebase</argument>
	<argument><%=path%></argument>
	<argument>-bnsname</argument>
	<argument><%=name%></argument>
	<argument>-modes</argument>
	<argument><%=modes%></argument>
	<argument>-rules</argument>
	<argument><%=rules%></argument>
	<argument>-debug</argument>
	<argument>false</argument>
	<argument>-configrules</argument>
	<argument>configurationRules.dpkg</argument>
	<argument>-login</argument>
	<argument><%=login%></argument>
	<argument>-password</argument>
	<argument><%=password%></argument>	
	<argument>-multiwindow</argument>
	<argument>false</argument>
  </application-desc>

</jnlp>

<%
}else{
%>

	<p align=center style='text-align:center'>
		<b><span style='font-size:15.0pt;font-family:"Comic Sans MS"'>Lo sentimos pero la p&aacute;gina a la que desea acceder no existe. Compruebe que la direcci&oacute;n introducida es correcta.</span></b>
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

