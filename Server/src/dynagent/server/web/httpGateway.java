/*
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package dynagent.server.web;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.FileNameMap;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.swing.ImageIcon;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.License;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.ObjectChanged;
import dynagent.common.communication.Reservation;
import dynagent.common.communication.UserConstants;
import dynagent.common.communication.contextAction;
import dynagent.common.communication.errorTrace;
import dynagent.common.communication.lockContainer;
import dynagent.common.communication.message;
import dynagent.common.communication.messageFactory;
import dynagent.common.communication.properties;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DomainProp;
import dynagent.common.properties.values.StringValue;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.CipherUtils;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.Instance;
import dynagent.server.ejb.InstanceHome;
import dynagent.server.ejb.Session;
import dynagent.server.exceptions.MessageException;
import dynagent.server.services.querys.AuxiliarQuery;

/*class userSession extends Object{
 private long ini=0;
 private long last=0;
 private String user;
 private String id=null;
 userSession( String user, String id ){
 System.out.println("SESSION USER "+user+","+id);
 this.user=user;
 reset(id);
 }
 void reset(String id){
 this.id=id;
 ini= System.currentTimeMillis();
 last=ini;
 }
 String getID(){
 return id;
 }
 long getIni(){
 return ini;
 }
 String getUser(){
 return user;
 }
 void live(){
 last= System.currentTimeMillis();
 }

 boolean isConnected(String cnxID, boolean reconnect){
 if( cnxID!=null && cnxID.equals(id) )
 return true;
 else
 if(reconnect && !isUpdated()){
 //Significa que el usuario estaba en silencio
 //asi que dewsconecto la sesión anterior y la reemplazo por esta
 reset(cnxID);
 return true;
 }
 return false;
 }

 boolean isUpdated(){
 long now= System.currentTimeMillis();
 System.out.println("NOW,LAST:"+now+","+last);
 return  now-last < httpGateway.liveTime+httpGateway.JMS_CURRENT_TIMEOUT;
 }
 }*/

public class httpGateway extends HttpServlet {

	boolean readOnly=false;
	Context EJBcontext;
	long start = 0;
	public final static int JMS_CURRENT_TIMEOUT = 60000;
	public final static int liveTime = 60000;
	public final static ArrayList<String> usersTransition = new ArrayList<String>();
	boolean encriptar=false;
	private final static File dynaFile = new File(
			"../server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna");
	private static HashMap<String, Integer> directoryIdForFiles = new HashMap<String, Integer>();
	private HashMap<String, ArrayList<File>> uploadedFilesMap = new HashMap<String, ArrayList<File>>();

	private int numRequestNotPremium;// Nos indica cuantas peticiones de
										// usuarios freeware o shareware se
										// estan procesando actualmente
	private String mutex = "";// Usada para sincronizar el acceso al beanPool
								// cuando se intenta conseguir uno disponible

	private boolean sharedBean;// Indica si trabaja con un bean reutilizable por
								// todas las bases de datos al tener el mismo
								// modelo o un bean por base de datos

	private Element metadataCache;// Cache del metadata. Usado de momento solo
									// para DEMO

	private MimetypesFileTypeMap mediaTypes;

	public void HttpServlet() {
		System.out.println("HTTPGW, CONTSTRUCCION");

	}

	public void init(ServletConfig config) {
		System.out.println("HTTPGW, INICIO");
		try {
			// svlCtx= getServletConfig().getServletContext();

			/*
			 * m_jmsPool= (jmsPool)svlCtx.getAttribute("ConnectionFac"); if(
			 * m_jmsPool == null ){ m_jmsPool= new jmsPool( "HTTPGWQueue" ,
			 * "RulerQueue"); svlCtx.setAttribute("ConnectionFac",m_jmsPool ); }
			 */
			/*
			 * System.out.println("GW: INICIALIZADO COLAS"); m_sesiones=
			 * (HashMap)svlCtx.getAttribute("sesiones"); if( m_sesiones == null
			 * ){ m_sesiones= new HashMap();
			 * svlCtx.setAttribute("sesiones",m_sesiones ); }
			 */
			EJBcontext = new InitialContext();
			/*
			 * }catch (JMSException e) { e.printStackTrace(); throw new
			 * EJBException( "GW error JMS:"+e.getMessage());
			 */
			numRequestNotPremium = 0;
			sharedBean = Boolean.valueOf(config.getInitParameter("sharedBean"));

			mediaTypes = new MimetypesFileTypeMap();
			mediaTypes.addMimeTypes("application/msword doc");
			mediaTypes.addMimeTypes("application/vnd.ms-excel xls");
			mediaTypes.addMimeTypes("application/pdf pdf");
			mediaTypes.addMimeTypes("text/richtext rtx");
			mediaTypes.addMimeTypes("text/csv csv");
			mediaTypes.addMimeTypes("text/tab-separated-values tsv tab");
			mediaTypes
					.addMimeTypes("application/x-vnd.oasis.opendocument.spreadsheet ods");
			mediaTypes
					.addMimeTypes("application/vnd.oasis.opendocument.text odt");
			mediaTypes
					.addMimeTypes("application/vnd.ms-powerpoint ppt pps pot");
			mediaTypes
					.addMimeTypes("application/vnd.openxmlformats-officedocument.wordprocessingml.document docx");
			mediaTypes
					.addMimeTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet xlsx");
			mediaTypes.addMimeTypes("audio/mpeg mp3 mpeg3");
			mediaTypes.addMimeTypes("image/png png");
			mediaTypes.addMimeTypes("application/zip zip");
			mediaTypes.addMimeTypes("application/x-tar tar");

		} catch (NamingException e) {
			e.printStackTrace();
			throw new EJBException("GW error Naming:" + e.getMessage());
		}
	}

	public Instance getInstanceRef(int business)
			throws java.rmi.RemoteException, javax.ejb.CreateException,
			javax.naming.NamingException, InterruptedException {
		String bean = "miInstance";
		if (business != 0 && !sharedBean)
			bean += business;
		System.out.println("Bean " + bean);
		Object boundObject = EJBcontext.lookup(bean);
		// InstanceHome
		// hInstance=(InstanceHome)PortableRemoteObject.narrow(boundObject,InstanceHome.class);
		int i = 0;
		InstanceHome hInstance = null;
		while (hInstance == null && i < 5) {
			hInstance = (InstanceHome) PortableRemoteObject.narrow(boundObject,
					InstanceHome.class);
			System.out.println("Iteracion" + i);
			if (hInstance == null) {
				Thread.sleep(1000);
				System.out.println("Sleep Iteracion" + i);
				i++;
			}
		}
		Instance ins = (Instance) hInstance.create();
		return ins;
	}

	public String mensageHTML(String msg) {
		return "<HTML><HEAD><TITLE>MENSAJE</TITLE></HEAD>" + "<BODY><P>" + msg
				+ "</BODY></HTML>";

	}

	/*
	 * public String userSessionList(){ Element root= new Element("HTML");
	 * Element head= new Element("HEAD"); root.addContent(head); Element title=
	 * new Element("TITLE"); int total=0; head.addContent(title); Element body=
	 * new Element("BODY"); root.addContent(body); Element nota= new
	 * Element("P"); body.addContent(nota); SimpleDateFormat sdf= new
	 * SimpleDateFormat("dd/MM/yy HH:mm:ss"); java.util.Date stamp = new
	 * java.util.Date(System.currentTimeMillis());
	 * nota.setText("(Actualizado a "+sdf.format( stamp )+")"); Element
	 * notaHead= new Element("B"); nota.addContent(notaHead); Element table= new
	 * Element("TABLE"); table.setAttribute("BORDER","1");
	 * body.addContent(table); addTableRow(table, "USUARIO",
	 * "INICIO","SESION ID" ); Iterator itr= m_sesiones.values().iterator();
	 * while( itr.hasNext() ){ userSession us=(userSession)itr.next(); if(
	 * us.isUpdated()){ stamp = new java.util.Date(us.getIni());
	 * addTableRow(table, us.getUser(), sdf.format( stamp ),us.getID() );
	 * total++; } } title.setText("USUARIOS CONECTADOS "+total);
	 * notaHead.setText("USUARIOS CONECTADOS "+total); try{ return
	 * jdomParser.returnXML(root); }catch(Exception e){ return
	 * mensageHTML("ERROR AL CONSTRUIR LA RESPUESTA"); } }
	 */

	void addTableRow(Element table, String cell1, String cell2, String cell3) {
		Element row = new Element("TR");
		table.addContent(row);
		Element cell = new Element("TD");
		row.addContent(cell);
		cell.setText(cell1);
		cell = new Element("TD");
		row.addContent(cell);
		cell.setText(cell2);
		cell = new Element("TD");
		row.addContent(cell);
		cell.setText(cell3);
	}

	protected String getParam(String query, String label) {
		int ini = query.indexOf(label);
		if (ini == -1)
			return null;
		ini = query.indexOf("=", ini) + 1;
		int end = query.indexOf("&", ini);
		if (end != -1)
			return query.substring(ini, end);
		else
			return query.substring(ini);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String query = request.getQueryString();
		System.out.println("DO GET, RECIBIDO " + query);
		// String encodings = request.getHeader("Accept-Encoding");
		String type = request.getParameter(Constants.REQUEST_PARAM_TYPE);
		if (type == null)
			return;
		if (type.equals(Constants.REQUEST_TYPE_GETREPORT)) {
			processGetReport(request, response);
			return;
		} else if (type.equals(Constants.REQUEST_TYPE_GETFILE)) {
			processGetFile(request, response);
		}
		String res = null;
		// try{
		// if( type.equals( "usuarios" ) ){
		// res=userSessionList();
		// }
		/*
		 * if( type.equals("REPORT" ) ){ String subtype= getParam(query,"SUB");
		 * if( subtype.equals("DET") ){ int ido=Integer.parseInt(
		 * getParam(query,"IDO") ); Instance ref=getInstanceRef(); res=
		 * ref.getInstanceReport(getParam(query,"USER"),
		 * getParam(query,"IDRPT"), ido ); } }
		 */

		if (res != null) {
			res = decode_BR(res);
			PrintWriter out = null;
			out = response.getWriter();
			out.println(res);
			out.close();
		}

		/*
		 * }catch(CreateException se){ se.printStackTrace(); return;
		 * }catch(NamingException se){ se.printStackTrace(); return; }
		 */
	}

	/**
	 * metodo que se encarga de enviar el informe solicitado al cliente y
	 * borrarlo del servidor una vez enviado.
	 * 
	 * @param request
	 * @param response
	 */
	public void processGetReport(HttpServletRequest request,
			HttpServletResponse response) {
		String reportName = request.getParameter("ID");
		if (reportName == null) {
			// TODO Tratemiento del error si no existe el parámetro
			return;
		}
		if (reportName.endsWith(".pdf") || reportName.endsWith(".jrprint")) {
			response.setContentType("application/pdf");
			response.setHeader("Content-type", "application/pdf");
		} else if (reportName.endsWith(".xls")) {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-type", "application/vnd.ms-excel");
			response.setHeader("Content-disposition", "attachment; filename="
					+ reportName);
		} else if (reportName.endsWith(".rtf")) {
			response.setContentType("application/rtf");
			response.setHeader("Content-type", "application/rtf");
			response.setHeader("Content-disposition", "attachment; filename="
					+ reportName);
		} else {
			return;
		}
		response.setHeader("Cache-control", "no-cache");
		File report = new File(dynaFile, "reports/" + reportName)
				.getAbsoluteFile();
		try {
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					response.getOutputStream());
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					new FileInputStream(report));
			byte[] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
				bufferedOutputStream.write(buffer, 0, bytesRead);
			}
			bufferedInputStream.close();
			bufferedOutputStream.close();
			/*
			 * boolean deleted = report.delete(); if (! deleted){
			 * System.err.println("No se pudo borrar el fichero: " +
			 * report.getAbsolutePath()); }
			 */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Procesa una petición de un fichero.
	 * 
	 * @param request
	 * @param response
	 * @throws SQLException
	 * @throws NamingException
	 * @throws NumberFormatException
	 * @throws MalformedURLException
	 */
	public void processGetFile(HttpServletRequest request,
			HttpServletResponse response) {
		String bns = request.getParameter(Constants.REQUEST_PARAM_BNS);
		String fileName = request.getParameter(Constants.REQUEST_PARAM_ID);
		String sessionId = request
				.getParameter(Constants.REQUEST_PARAM_SESSION);
		Boolean download = new Boolean(
				request.getParameter(Constants.REQUEST_PARAM_DOWNLOAD));
		if (bns == null) {
			System.err.println("No se ha indicado la empresa en la petición: "
					+ request.getQueryString());
			return;
		}
		if (fileName == null) {
			System.err
					.println("No se ha indicado el nombre del fichero en la petición: "
							+ request.getQueryString());
		}
		try {
			if (!Session.existsSession(sessionId, Integer.parseInt(bns))) {
				System.err
						.println("Intento de acceso ilegal con la sesión con idClient: "
								+ sessionId);
				response.sendError(
						HttpServletResponse.SC_FORBIDDEN,
						"Intento de acceso ilegal: El usuario no está identificado o su sesión ha expirado");
				return;
			}
		} catch (NamingException e1) {
			e1.printStackTrace();
			return;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

		File userFilesFolder = new File(dynaFile, Constants.folderUserFiles);
		File bnsFolder = new File(userFilesFolder, bns);
		File requestedFile = new File(bnsFolder, fileName);
		// Si el fichero no existe, vamos a devolver una imagen que indica que
		// no hemos encontrado el fichero
		if (!requestedFile.exists()) {
			File imagesFolder = new File(dynaFile, Constants.imagesFolder);
			requestedFile = new File(imagesFolder, Constants.unavailableImage);
			download = false;
		}
		// Buscamos el Content-Type a utilizar para el fichero solicitado.

		// only by file name
		String contentType = mediaTypes.getContentType(fileName);
		// by file
		// String contentType = mimeTypesMap.getContentType(requestedFile);

		response.setContentType(contentType);
		response.setHeader("Content-type", contentType);
		if (download) {
			response.setHeader("Content-Disposition", "attachment;filename=\""
					+ requestedFile.getName() + "\"");
		}

		try {
			BufferedOutputStream out = new BufferedOutputStream(
					response.getOutputStream());
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(requestedFile));
			byte[] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String decode_BR(String str) {
		int pos = 0;
		String res = str;
		while (pos < res.length() - 4) {
			pos = res.indexOf("&lt;BR&gt", pos);
			if (pos == -1)
				break;
			res = res.substring(0, pos) + "<BR>" + res.substring(pos + 10);
		}
		return res;
	}

	public void processSetImg(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String query = request.getQueryString();
		int step = Integer.parseInt(getParam(query, "STEP"));
		String source = step == 1 ? "/launchUpload.html" : "/upload.html";

		InputStream is = getServletContext().getResourceAsStream(source);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader bf = new BufferedReader(isr);

		PrintWriter out = null;
		out = response.getWriter();
		String buff = bf.readLine();
		String tmpcode = step == 1 ? getParam(query, "TMPCODE") : URLDecoder
				.decode(getParam(query, "TMPCODE"), "UTF-8");
		while (buff != null) {
			buff = buff.replaceAll("#tmpcode#", tmpcode);
			buff = buff.replaceAll("#tapos#", getParam(query, "TAPOS"));
			buff = buff.replaceAll("#bns#", getParam(query, "BNS"));
			out.println(buff);
			buff = bf.readLine();
		}
		out.close();
	}

	public void processGetImg(HttpServletRequest request,
			HttpServletResponse response) throws SQLException, NamingException,
			IOException {
		response.reset();

		response.setContentType("image/gif");
		response.setHeader("Content-type", "image/gif");
		response.setHeader("Cache-control", "no-cache");
		response.setHeader("Content-Disposition",
				"inline;filename=provider_128.gif");

		String query = request.getQueryString();
		System.out.println("QUERY:" + query);
		int empresa = Integer.parseInt(getParam(query, "BNS"));
		java.sql.Connection conn = AuxiliarQuery.getDataConnection(empresa);
		Statement st = conn.createStatement();
		String id = URLDecoder.decode(getParam(query, "ID"), "UTF-8");
		ResultSet rs = null;
		/*
		 * if(id.indexOf(":")!=-1){ String[] buf = id.split(":");
		 * 
		 * String idto=buf[0]; String idProp = buf[1];
		 * //System.out.println("SENTENCIA 1: select imagen from dyna"
		 * +empresa+".dbo.O_IMAGENES where ido="
		 * +idto+" AND idto="+helperConstant.TO_METATIPO+" AND tapos="+idProp);
		 * rs= st.executeQuery("select imagen from dyna"+empresa+
		 * ".dbo.O_IMAGENES where id_o="
		 * +idto+" AND id_to="+helperConstant.TO_METATIPO
		 * +" AND ta_pos="+idProp); }else if( id.indexOf("#")==-1 ){
		 * //System.out.println("SENTENCIA 2: select imagen from dyna"+empresa+
		 * ".dbo.O_IMAGENES where id="+id); rs=
		 * st.executeQuery("select imagen from dyna"
		 * +empresa+".dbo.O_IMAGENES where id="+id); }else rs=
		 * st.executeQuery("select imagen from dyna"
		 * +empresa+".dbo.O_IMAGENES where TMP_CODE='"+id+"'");
		 */
		rs = st.executeQuery("select image from dyna" + empresa
				+ ".dbo.Images where name like '" + id + "'");
		System.err.println("Antes encontrado rs:" + rs);
		if (rs.next()) {
			System.err.println("Ha encontrado rs:" + rs);
			InputStream in = new BufferedInputStream(rs.getBlob(1)
					.getBinaryStream(/* 1 */));
			OutputStream out = new BufferedOutputStream(
					response.getOutputStream());
			byte bindata[] = new byte[1024];
			int bytesread = 0;
			try {
				while ((bytesread = in.read(bindata, 0, bindata.length)) != -1) {
					out.write(bindata);
					System.err.println("Num bytes:" + bytesread);
				}
			} catch (IOException io) {
				if (bytesread > 0)
					out.write(bindata);
			} finally {
				System.err.println("Entra en finally: bufferSize:"
						+ response.getBufferSize() + " outSize:" + out);
				in.close();
				// out.flush();
				// out.close();
			}
		}
		rs.close();
		st.close();
		conn.close();
	}

	public void processDelImg(HttpServletRequest request,
			HttpServletResponse response) throws SQLException, NamingException,
			IOException {

		String query = request.getQueryString();
		System.out.println("QUERY:" + query);
		int empresa = Integer.parseInt(getParam(query, "BNS"));
		java.sql.Connection conn = AuxiliarQuery.getDataConnection(empresa);
		Statement st = conn.createStatement();
		String id = URLDecoder.decode(getParam(query, "ID"), "UTF-8");
		if (id.indexOf("#") == -1)
			st.executeUpdate("DELETE FROM dyna" + empresa
					+ ".dbo.O_IMAGENES where id=" + id);
		else
			st.executeUpdate("DELETE FROM dyna" + empresa
					+ ".dbo.O_IMAGENES where TMP_CODE='" + id + "'");
		st.close();
		conn.close();
		PrintWriter out = response.getWriter();
		out.println(mensageHTML("IMAGEN ELIMINADA CON EXITO"));
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		/*
		 * Enumeration itr= request.getHeaderNames(); while(
		 * itr.hasMoreElements() ){ String hd=(String)itr.nextElement();
		 * System.out.println("HEADER "+hd+" "+request.getHeader(hd)); }
		 */
		String encodings = request.getHeader("Accept-Encoding");
		String contentType = request.getHeader("CONTENT-TYPE");
		if (contentType.contains("multipart")) {
			uploadFile(request, response);
			return;
		}
		InputStream resultingInputStream = null;

		// create the appropriate stream wrapper based on
		// the encoding type
		// if (encodings != null && encodings.equalsIgnoreCase("gzip")) {
		resultingInputStream = new GZIPInputStream(request.getInputStream());
		/*
		 * }else if (encodings != null && encodings.equalsIgnoreCase("deflate"))
		 * { resultingInputStream = new
		 * InflaterInputStream(request.getInputStream(), new Inflater(true));
		 * }else { resultingInputStream = request.getInputStream(); }
		 */

		BufferedReader in = new BufferedReader(new InputStreamReader(
				resultingInputStream));
		String line;
		StringBuffer body = new StringBuffer("");
		while ((line = in.readLine()) != null) {
			// System.out.println("LINE:"+line);
			body.append(line + "\n");
		}
		try {			
			System.out.println("Encriptando");
			String strBody=encriptar?CipherUtils.decrypt(body.toString()):body.toString();
			procesaReq(request, response, strBody);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} 
		in.close();

	}

	public void uploadFile(HttpServletRequest req, HttpServletResponse response)
			throws IOException {

		DiskFileUpload diskFileUpload = new DiskFileUpload();
		// máximo tamaño permitido para el fichero que se va a subir.
		// diskFileUpload.setSizeMax(1024*512); //512KB
		diskFileUpload.setSizeMax(-1);// Cualquier tamaño
		// máximo tamaño que se almacenara en memoria RAM
		diskFileUpload.setSizeThreshold(4096);
		// Directorio donde se almacenaran los ficheros que superen el
		// sizeThreshold
		File userFiles = new File(dynaFile, Constants.folderUserFiles + "/");

		if (!userFiles.exists()) {
			userFiles.mkdirs();
		}
		diskFileUpload.setRepositoryPath(System
				.getProperty("jboss.server.temp.dir"));

		ArrayList<String> filesNames = new ArrayList<String>();
		String bns = null;
		String usr = null;
		long milis = System.currentTimeMillis();
		Integer smallImageHeight = null;
		File destinationDirectory = null;
		String fileItemName = null;
		String fileName = null;
		// ArrayList<String> allowedTypes = new ArrayList<String>();
		File destinationFile = null;
		File destinationSmallFile = null;

		try {

			List fileItems = diskFileUpload.parseRequest(req);
			// Cadena que se mostrara en la pagina de respuesta en la que se
			// indica cuantos ficheros se han subido
			String fileNumberPrompt;
			// Este calculo se debe a que es posible que se envie un input de
			// file vacio o no dependiendo de las circonstancias.
			int numberOfFiles = fileItems.size() - 4 == 1 ? 1 : fileItems
					.size() - 5;
			if (numberOfFiles == 1) {
				fileNumberPrompt = "<h2>Se va a subir 1 fichero</h2>";
			} else {
				fileNumberPrompt = "<h2>Se van a subir " + numberOfFiles
						+ " ficheros.</h2>";
			}

			// Se van a recorrer todos los inputs que manda el formulario
			Iterator i = fileItems.iterator();
			while (i.hasNext()) {
				FileItem fileItem = (FileItem) i.next();
				// Si el nombre del campo es uno de estos tres, son campos de
				// control que usamos para construir la clave con la
				// que posteriormente se pueden rescatar las referencias a los
				// archivos subidos.
				/*
				 * if (fileItem.getFieldName().equals("milis")){ milis =
				 * fileItem.getString(); continue; }else
				 */if (fileItem.getFieldName().equals("bns")) {
					// Construimos el objeto que referencia a la carpeta donde
					// se van a guardar los ficheros y creamos la carpeta si no
					// existe
					bns = fileItem.getString();
					destinationDirectory = new File(userFiles, bns);
					if (!destinationDirectory.exists()) {
						destinationDirectory.mkdir();
					}
					continue;
				} else if (fileItem.getFieldName().equals("user")) {
					usr = fileItem.getString();
					continue;
				} else if (fileItem.getFieldName().equals("smallImageHeight")) {
					smallImageHeight = Integer.valueOf(fileItem.getString());
					continue;
				}/*
				 * else if (fileItem.getFieldName().equals("allowedTypes")){
				 * String [] types = fileItem.getString().split(";"); for
				 * (String string : types) { allowedTypes.add(string); }
				 * continue; }
				 */
				fileItemName = fileItem.getName();

				// Si se cumple la condición, no es un fichero lo que estamos
				// tratando y debemos saltar al siguiente elemento.
				if (fileItemName == null || fileItemName.isEmpty()) {
					continue;
				}
				String extension = fileItemName.lastIndexOf(".") != -1 ? fileItemName
						.substring(fileItemName.lastIndexOf(".") + 1) : "";
				/*
				 * if (! allowedTypes.contains(extension)){
				 * out.write("<p class=\"error\">El archivo " + fileItemName +
				 * " no se ha guardado porque no es de uno de los tipos permitidos</p>"
				 * ); continue; }
				 */
				// fileName = getIdForFile(destinationDirectory);
				// fileName = milis+"_"+fileItemName;
				String userWithoutAccents = Auxiliar.removeStringAccents(usr);

				int fileNamePositionBegin = Math.max(
						fileItemName.lastIndexOf("/"),
						fileItemName.lastIndexOf("\\"));
				int fileNamePositionEnd = fileItemName.lastIndexOf(".") != -1 ? fileItemName
						.lastIndexOf(".") : fileItemName.length();
				String fileNameWithoutExtension = fileItemName.substring(
						fileNamePositionBegin + 1, fileNamePositionEnd);
				fileName = fileNameWithoutExtension + "." + extension;
				// fileName = userWithoutAccents+"_"+milis+"."+extension;

				destinationFile = new File(destinationDirectory, fileName);
				System.err.println("ARCHIVO "
						+ destinationFile.getAbsolutePath());
				if(!extension.equals("jrxml")){
					int cont = 0;
					while (destinationFile.exists()) {
						System.err.println("EXISTE asi que le añade contador");
						// Evitamos que se asigne el mismo milisegundo que le
						// 	asignamos a otro archivo como nombre ya que puede ocurrir
						// si el archivo es pequeño
						// milis++;
						// fileName = userWithoutAccents+"_"+milis+"."+extension;
						cont++;
						fileName = fileNameWithoutExtension + "_" + cont + "." + extension;
						destinationFile = new File(destinationDirectory, fileName);
					}
				}

				// if (fileName == null){
				// fileName = fileItemName;
				// // Comprobamos que no exista ya un fichero con ese nombre
				// para no machacarlo.
				// if (new File(destinationDirectory, fileName).exists()){
				// fileName += System.currentTimeMillis();
				// }
				// }else{
				// // Añadimos la extension al fichero, la misma que tenia el
				// fichero de subida.
				// fileName += !extension.isEmpty() ? "." + extension :
				// extension;
				// }

				byte[] file = fileItem.get();
				fileItem.write(destinationFile);
				// Una vez que se ha escrito el fichero correctamente, se guarda
				// el nombre en la lista de nombres de archivos subidos
				filesNames.add(fileName);

				// Devolvemos el nombre que le hemos asignado al fichero
				PrintWriter out = response.getWriter();
				
				out.print(fileName);
				out.close();

				if (smallImageHeight != null) {
					Image img = new ImageIcon(
							/* destinationFile.getAbsolutePath() */file)
							.getImage();

					// int width=img.getWidth(null);
					// int height=img.getHeight(null);
					// int maxWidth=smallSize;
					int maxHeight = smallImageHeight;
					Image scaledImg = img.getScaledInstance(/*
															 * width>=height?
															 * maxWidth:
															 */-1, /*
																	 * width<=height
																	 * ?
																	 */
							maxHeight/* :-1 */, Image.SCALE_SMOOTH);

					destinationSmallFile = new File(destinationDirectory,
							Constants.smallImage + fileName);
					// BufferOutputStream fileoutputstream = fileSmallImage.get

					BufferedImage bi = toBufferedImage(scaledImg);
					System.err.println("Extension:" + extension);
					System.err
							.println("Exito:"
									+ ImageIO.write(bi, extension,
											destinationSmallFile));
				}

			}
		} catch (FileUploadException e) {
			/*
			 * out.write(
			 * "<label class=\"error\">Uno de los ficheros que ha enviado excede el tama&ntilde;o permitido</label>"
			 * ); out.write(
			 * "<p>Deshaciendo todos los cambios posibles que se hayan realizado.<br>Tendr&aacute; que volver a subir todos los ficheros.</p>"
			 * );
			 */
			e.printStackTrace();
		} catch (Exception e) {
			/*
			 * out.write(
			 * "<p>Se ha producido un error mientras se escrib&iacute;a uno de los ficheros que quer&iacute;a usted subir</p>"
			 * ); out.write(
			 * "<p>Deshaciendo todos los cambios posibles que se hayan realizado.<br>Tendr&aacute; que volver a subir todos los ficheros.</p>"
			 * );
			 */
			// filesNames.add(fileName);
			// revertUpload(destinationDirectory, filesNames);
			e.printStackTrace();
		} finally {
			// Si se ha conseguido subir algun fichero, se guarda la relación de
			// ficheros subidos asociados a una clave.
			if (filesNames != null && filesNames.size() > 0) {
				// key = usr + bns + milis;
				if (uploadedFilesMap.get(usr) == null) {
					uploadedFilesMap.put(usr, new ArrayList<File>());
				}
				uploadedFilesMap.get(usr).add(destinationFile);
				if (destinationSmallFile != null)
					uploadedFilesMap.get(usr).add(destinationSmallFile);
			}
		}
	}

	private BufferedImage toBufferedImage(Image image) {
		/** miramos que la imagen no sea ya una instancia de BufferedImage */
		if (image instanceof BufferedImage) {
			return ((BufferedImage) image);
		} else {
			/** nos aseguramos que la imagen está totalmente cargada */
			image = new ImageIcon(image).getImage();
			/** creamos la nueva imagen */
			BufferedImage bufferedImage = new BufferedImage(
					image.getWidth(null), image.getHeight(null),
					BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));// Ponemos
																			// el
																			// fondo
																			// blanco
																			// ya
																			// que
																			// si
																			// no
																			// aparece
																			// negro
																			// si
																			// la
																			// imagen
																			// tiene
																			// transparencia
			g.drawImage(image, 0, 0, null);
			g.dispose();
			return (bufferedImage);
		}
	}

	/**
	 * Metodo que se encarga del tratamiento de los formularios de subida de
	 * ficheros guardandolos en la carpeta que le corresponda. tambien rechaza
	 * los ficheros que superen un cierto tamaño y se encarga de que en ningun
	 * momento se suban dos ficheros con el mismo nombre a la misma carpeta. <br>
	 * <br>
	 * Los nombres de los ficheros subidos se guardan en un mapa con una clave
	 * construida a partir del usr + bns + milis del formulario que nos ha
	 * llegado. De esta manera, posteriormente, con la misma clave, se puede
	 * establecer la relación con el objeto que corresponda.
	 * 
	 * @param req
	 *            Petición de subida de ficheros
	 * @param response
	 *            Respuesta a enviarle al cliente que ha solicitado la subida de
	 *            ficheros.
	 * @throws IOException
	 */
	// public void uploadFile(HttpServletRequest req, HttpServletResponse
	// response) throws IOException{
	// PrintWriter out= response.getWriter();
	// out.write("<head>");
	// out.write("<title>Subida de ficheros</title>");
	// out.write("</head>");
	// out.write("<body>");
	//
	// String key = "";
	//
	// DiskFileUpload diskFileUpload = new DiskFileUpload();
	// // máximo tamaño permitido para el fichero que se va a subir.
	// diskFileUpload.setSizeMax(1024*512); //512KB
	// // máximo tamaño que se almacenara en memoria RAM
	// diskFileUpload.setSizeThreshold(4096);
	// // Directorio donde se almacenaran los ficheros que superen el
	// sizeThreshold
	// File userImages = new File(dynaFile, "/userImages/");
	//
	// if (! userImages.exists()){
	// userImages.mkdirs();
	// }
	// diskFileUpload.setRepositoryPath(System.getProperty("java.io.tmpdir"));
	//
	// ArrayList<String> filesNames = new ArrayList<String>();
	// String bns = null;
	// String usr = null;
	// String milis = null;
	// File destinationDirectory = null;
	// String fileItemName = null;
	// String fileName = null;
	// ArrayList<String> allowedTypes = new ArrayList<String>();
	//
	// try {
	//
	// List fileItems = diskFileUpload.parseRequest(req);
	// // Cadena que se mostrara en la pagina de respuesta en la que se indica
	// cuantos ficheros se han subido
	// String fileNumberPrompt;
	// // Este calculo se debe a que es posible que se envie un input de file
	// vacio o no dependiendo de las circonstancias.
	// int numberOfFiles = fileItems.size() - 4 == 1 ? 1 : fileItems.size() - 5;
	// if (numberOfFiles == 1){
	// fileNumberPrompt = "<h2>Se va a subir 1 fichero</h2>";
	// }else{
	// fileNumberPrompt = "<h2>Se van a subir " + numberOfFiles +
	// " ficheros.</h2>";
	// }
	// out.write(fileNumberPrompt);
	// // Se van a recorrer todos los inputs que manda el formulario
	// Iterator i = fileItems.iterator();
	// while(i.hasNext()){
	// FileItem fileItem=(FileItem)i.next();
	// // Si el nombre del campo es uno de estos tres, son campos de control que
	// usamos para construir la clave con la
	// // que posteriormente se pueden rescatar las referencias a los archivos
	// subidos.
	// if (fileItem.getFieldName().equals("milis")){
	// milis = fileItem.getString();
	// continue;
	// }else if (fileItem.getFieldName().equals("bns")){
	// // Construimos el objeto que referencia a la carpeta donde se van a
	// guardar los ficheros y creamos la carpeta si no existe
	// bns = fileItem.getString();
	// destinationDirectory = new File(userImages, bns);
	// if (! destinationDirectory.exists()){
	// destinationDirectory.mkdir();
	// }
	// continue;
	// }else if (fileItem.getFieldName().equals("usr")){
	// usr = fileItem.getString();
	// continue;
	// }else if (fileItem.getFieldName().equals("allowedTypes")){
	// String [] types = fileItem.getString().split(";");
	// for (String string : types) {
	// allowedTypes.add(string);
	// }
	// continue;
	// }
	// fileItemName = fileItem.getName();
	// //Si se cumple la condición, no es un fichero lo que estamos tratando y
	// debemos saltar al siguiente elemento.
	// if (fileItemName == null || fileItemName.isEmpty()){
	// continue;
	// }
	// String extension = fileItemName.lastIndexOf(".") != -1 ?
	// fileItemName.substring(fileItemName.lastIndexOf(".") + 1) : "";
	// if (! allowedTypes.contains(extension)){
	// out.write("<p class=\"error\">El archivo " + fileItemName +
	// " no se ha guardado porque no es de uno de los tipos permitidos</p>");
	// continue;
	// }
	// fileName = getIdForFile(destinationDirectory);
	// if (fileName == null){
	// fileName = fileItemName;
	// // Comprobamos que no exista ya un fichero con ese nombre para no
	// machacarlo.
	// if (new File(destinationDirectory, fileName).exists()){
	// fileName += System.currentTimeMillis();
	// }
	// }else{
	// // Añadimos la extension al fichero, la misma que tenia el fichero de
	// subida.
	// fileName += !extension.isEmpty() ? "." + extension : extension;
	// }
	//
	// File destinationFile = new File(destinationDirectory, fileName);
	// fileItem.write(destinationFile);
	// // Una vez que se ha escrito el fichero correctamente, se guarda el
	// nombre en la lista de nombres de archivos subidos
	// filesNames.add(fileName);
	// out.write("<br>Subido el fichero " + fileItemName);
	// }
	// } catch (FileUploadException e) {
	// out.write("<label class=\"error\">Uno de los ficheros que ha enviado excede el tama&ntilde;o permitido</label>");
	// out.write("<p>Deshaciendo todos los cambios posibles que se hayan realizado.<br>Tendr&aacute; que volver a subir todos los ficheros.</p>");
	// e.printStackTrace();
	// } catch (Exception e) {
	// out.write("<p>Se ha producido un error mientras se escrib&iacute;a uno de los ficheros que quer&iacute;a usted subir</p>");
	// out.write("<p>Deshaciendo todos los cambios posibles que se hayan realizado.<br>Tendr&aacute; que volver a subir todos los ficheros.</p>");
	// filesNames.add(fileName);
	// revertUpload(destinationDirectory, filesNames);
	// e.printStackTrace();
	// } finally{
	// // Si se ha conseguido subir algun fichero, se guarda la relación de
	// ficheros subidos asociados a una clave.
	// if (filesNames != null && filesNames.size() > 0){
	// key = usr + bns + milis;
	// uploadedFilesMap.put(key, filesNames);
	// }
	// out.write("</body>");
	// }
	// }

	/**
	 * metodo que se va a encargar de consultar y asignar un nombre para un
	 * fichero que se vaya a guardar en el directorio indicado. Para ello, se va
	 * a mantener un entero estático en la clase donde se va a a ir guardando el
	 * número de ficheros en el directorio, si en un momento dado dicho entero
	 * no tiene valor, lo que se va a hacer es consultar el directorio y contar
	 * cuantos ficheros tiene para inicializarlo.
	 * 
	 * @param directory
	 *            Directorio del que se quiere saber el id a asignar.
	 * @return Cadena con el identificador sin extension para el fichero.
	 */
	private String getIdForFile(File directory) {
		String id = null;
		if (directory == null || !directory.isDirectory()) {
			return id;
		}
		// Cogemos la ruta en forma canúnica porque es única y absoluta.
		String canonicalPath;
		try {
			canonicalPath = directory.getCanonicalPath();
		} catch (IOException e) {
			System.err
					.println("[HTTPGW:getIdForFile] Error al intentar construir la ruta canúnica del directorio.");
			e.printStackTrace();
			return null;
		}
		// Consultamos el mapa de IDs para saber que ID corresponde a ese
		// directorio.
		Integer integerId = directoryIdForFiles.get(canonicalPath);
		// Si no hay id asociada a esa ruta, significa que no esta inicializado
		// y que debemos hacerlo.
		if (integerId == null) {
			integerId = getMaxIdFromDirectory(directory) + 1;
		}
		id = integerId.toString();
		// Aumentamos el índice para la proxima consulta.
		directoryIdForFiles.put(canonicalPath, integerId + 1);
		return id;
	}

	/**
	 * Consigue el mayor índice utilizado a la hora de nombrar los ficheros del
	 * directorio
	 * 
	 * @param directory
	 *            Directorio del que se quiere saber el índice
	 * @return 0 si no hay ficheros en el directorio<br>
	 *         -1 si el parámetro pasado no es un directorio
	 */
	private int getMaxIdFromDirectory(File directory) {
		int id = 0;
		if (directory == null || !directory.isDirectory()) {
			return -1;
		}
		String[] directoryList = directory.list();
		for (String string : directoryList) {
			int dotIndex = string.lastIndexOf(".") != -1 ? string
					.lastIndexOf(".") : string.length();
			String idString = string.substring(0, dotIndex);
			int idTmp;
			try {
				idTmp = Integer.parseInt(idString);
			} catch (NumberFormatException e) {
				continue;
			}
			if (idTmp > id) {
				id = idTmp;
			}
		}
		return id;
	}

	/**
	 * Borra todos los ficheros indicados del directorio que se pasa por
	 * parámetro y modifica el contador de id de ficheros para dicho directorio.
	 * 
	 * @param directory
	 *            Directorio donde están los ficheros a borrar.
	 * @param files
	 *            Lista con los nombre de los ficheros a borrar en la carpeta.
	 */
	private void revertUpload(File directory, ArrayList<String> files) {
		if (directory == null || !directory.exists()) {
			return;
		}

		for (String fileName : files) {
			File fileToDelete = new File(directory, fileName);
			fileToDelete.delete();
		}

		String canonicalPath;
		try {
			canonicalPath = directory.getCanonicalPath();
		} catch (IOException e) {
			System.err
					.println("[HTTPGW:revertUpload] Error al intentar construir la ruta canúnica del directorio.");
			e.printStackTrace();
			return;
		}
		Integer numberOfFiles = directoryIdForFiles.get(canonicalPath);
		if (numberOfFiles == null) {
			files = null;
			return;
		}
		numberOfFiles -= files.size();
		directoryIdForFiles.put(canonicalPath, numberOfFiles);
		files = null;
	}

	public String autoClosePage() {
		return "<HTML><BODY onload=\"close()\"/></HTML>";
	}

	/*
	 * public String receiveBlock(String user, boolean buildErrorResponse)
	 * throws GWException,JMSException{ String respuesta=
	 * "<BLOCK MSG_TYPE=\""+message.MSG_BLOCK+"\" ACT_WAY=\"FALSE\"><CONTENT>" ;
	 * int block = 0; try{ while (true) { String res = receiveMsg(user,
	 * m_jmsPool,JMS_POLLING_TIMEOUT, null,true); int ini =
	 * res.indexOf("<?xml"); if (ini >= 0) { int end = res.indexOf(">", ini);
	 * res = res.substring(0, ini) + res.substring(end + 1); } respuesta += res;
	 * block++; } }catch(GWException e){ if(e.GetCode()==message.ERROR_TIME_OUT
	 * ){ if( block==0 ){ if(!buildErrorResponse) return null; else return
	 * buildError(message.SUCCESSFULL,user); } }
	 * if(e.GetCode()==helperConstant.ERROR_GW_JMS) return
	 * buildError(message.ERROR_SYSTEM,user); } respuesta+="</CONTENT></BLOCK>";
	 * return respuesta; }
	 */

	public void procesaReq(HttpServletRequest request,
			HttpServletResponse response, String body) throws ServletException,
			IOException {
		String encodings = request.getHeader("Accept-Encoding");
		String msguid = null;
		boolean premium = false;
		System.out.println("DO POST:RECIBIDO::" + body);
		boolean checklogin=true;
		  if(body.contains("#TESTFILE#")){
			  System.out.println("debug msg");		  
			  File f=new File("C:\\HIDRIVE\\debug\\dopost.txt"); 
			  try {
				  body=jdomParser.readXMLToString(f);
				  System.out.println("sin excepcion "+body); 
				  checklogin=false;
			  } catch (JDOMException e)
			  { 
				  e.printStackTrace(); 
			  }
		  }
		 
		try {
			message root = messageFactory.parseMsg(null, body);
			// Enumeration enumVar = request.getHeaderNames();
			// boolean justSendConfirm =
			// request.getHeader("Sendconfirm").equals("true");
			String user = root.getUser();
			Integer windowSession = root.getWindowSession();
			int msgType = root.getType();
			int orderType = root.getOrderType();
			msguid = root.getMsguid();
			String clientSession = root.getClientSession();
			System.out.println("USER " + user);
			int bns = root.getBusiness();
			System.out.println("EMPRESA " + bns);
			String mode = root.getMode();
			System.out.println("MODE " + mode);
			boolean configurationMode = mode
					.equalsIgnoreCase(Constants.CONFIGURATION_MODE);
			String keySession = user + "/" + bns + "/" + mode;

			String subscription = root.getSubscription();
			System.out.println("SUBSCRIPTION " + subscription);
			premium = true;// subscription==null ||
							// !Auxiliar.equals(subscription,Constants.SHAREWARE_SUBSCRIPTION);
			System.out.println("DBGSTEP 0");
			System.out.println("isPremium:" + premium);
			System.out.println("DBGSTEP 1");
			if (!readOnly&&checklogin && msgType != message.MSG_LOGIN
					&& msgType != message.MSG_LOGIN_OVERWRITTEN
					&& msgType != message.MSG_LOG_ERROR
					&& msgType != message.MSG_LOG_ERROR_LOGIN
					&& !Auxiliar.equals(subscription,
							Constants.DEMO_SUBSCRIPTION)
					&& msgType != message.MSG_CODE_LICENSE){
					System.out.println("DBGSTEP2");
			
					if(!Session.existsSession(clientSession, bns)) {
						System.out.println("Intento de acceso con sesión no valida para idClient: "	+ clientSession);
						GWError(encodings,
								response,
								msguid,
								message.ERROR_SESSION,
								"El usuario no está identificado o su sesión ha sido sobreescrita al volver a identificarse en otra maquina.",
								null);
						return;
					}
			}
			System.out.println("DBGSTEP 3");
			// Si no es premium miramos que no se haya superado el maximo numero
			// de peticiones concurrentes que se estan procesando
			// (Solo lo hacemos aqui y no en doGet ya que apenas consume
			// recursos porque no accede
			// a base de datos, pero pensar si es necesario ya que si accede a
			// disco y esto consume)
			if (!premium) {
				/*
				 * Evitamos que otra peticion posterior coja el bean antes
				 * (mientras espera esta), o que dos peticiones entren a las vez
				 * sobrepasando el limite maximo
				 */
				synchronized (mutex) {
					while (numRequestNotPremium == Constants.FREEWARE_MAX_CONCURRENT_REQUEST) {
						Thread.sleep(500);// Esperamos a que el bean este
											// disponible
					}
					numRequestNotPremium++;
				}
			}

			Integer userRol = null;
			if (root.getUserRol() != null)
				userRol = new Integer(root.getUserRol());

			String version = root.getVersion();
			if (!version.equals("V37")) {
				GWError(encodings, response, msguid, message.ERROR_VERSION,
						"Cierre la aplicacion y entre de nuevo, NECESITA VERSION 37", null);
				return;
			}
			// System.out.println("HTTPGW:recibido:"+body);
			// System.out.println("HTTPGW:msguid:"+msguid);
			System.out.println("MENSAJETYPE "+msgType);
			boolean reenviar = request.getHeader("reenviar").equals("true");
			;
			if (reenviar)
				System.out
						.println("************************** REENVIAR *****************");
			start = System.currentTimeMillis();

			switch (msgType) {
			case message.MSG_REPORTS_CLASIFICATOR:
				processMsgReportsClasificator(response, encodings, msguid,
						root, user, bns);
				break;
			case message.MSG_DISCONNECTION:
				processMsgDisconnection(response, encodings, msguid, root,
						user, keySession, bns);
				break;
			case message.MSG_CHANGE_MODE:
				processMsgChangeMode(response, encodings, msguid, root, user,
						keySession, clientSession, bns);
				break;
			case message.MSG_RESET_LOCKS:
				processMsgResetLocks(response, encodings, msguid, root, user,
						keySession, bns);
				break;
			case message.MSG_POOLING:
				processMsgPooling(response, encodings, msguid, user, bns);
				break;
			case message.MSG_LOG_ERROR:
			case message.MSG_LOG_ERROR_LOGIN:
				if(!readOnly) processMsgLogError(response, encodings, msguid, root, user, bns);
				break;
			case message.MSG_QUERY:
				processMsgQuery(response, encodings, msguid, root, user,
						orderType, bns, configurationMode, subscription, false);
				break;
			case message.MSG_UPDATE:
				processMsgQuery(response, encodings, msguid, root, user,
						orderType, bns, configurationMode, subscription, true);
				break;
			case message.MSG_MIGRATION:
				processMsgMigration(response, encodings, msguid, root, user,
						orderType, bns);
				break;
			case message.MSG_EXE_TRAN_ACTION:
				processMsgExeTranAction(response, encodings, msguid, root,
						user, orderType, bns);
				break;
			case message.MSG_PREPRINT:
				processMsgPreprint(response, encodings, msguid, root, user, bns);
				break;
			case message.MSG_OBJECT_TRAN:
				processMsgObjectTran(response, encodings, msguid, root, user,
						orderType, bns, userRol, windowSession);
				break;
			case message.MSG_LOCK:
			case message.MSG_UNLOCK:
				processMsgLockOrUnlock(response, encodings, msguid, root, user,
						bns);
				break;
			case message.MSG_LOGIN_OVERWRITTEN:
			case message.MSG_LOGIN:
				boolean success = processMsgLogin(request, response, encodings,
						msguid, root, clientSession, user, keySession, bns,
						configurationMode,
						msgType == message.MSG_LOGIN_OVERWRITTEN);
				if (!success) {
					return;
				}
				break;
			case message.MSG_RESERVE:
				processMsgReserve(response, encodings, msguid, root, user,
						orderType, bns, windowSession);
				break;
			case message.MSG_DATE_NOW:
				processMsgDateNow(response, encodings, msguid, root, user);
				break;
			case message.MSG_UPDATE_LICENSE:
				processMsgUpdateLicense(response, encodings, msguid, root,
						user, bns);
				break;
			case message.MSG_CODE_LICENSE:
				processMsgUseLicenseCode(response, encodings, msguid, root,
						user);
				break;
			case message.MSG_SEND_EMAIL:
				processMsgSendEmail(response, encodings, msguid, root, user,
						bns);
				break;
			case message.MSG_SEND_SERVER_LOG_EMAIL:
				processMsgSendServerLogEmail(response, encodings, msguid, root,
						user, bns);
				break;
			default:
				break;
			}

			/*
			 * if(msgType==message.MSG_METADATA && orderType==message.OBJ_GET){
			 * Instance ref=getInstanceRef(); int idto=
			 * root.getPropertie(properties.type);
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,ref.Meta_GetTO(idto, user)); ref.commit(); }
			 */

			/*
			 * if(msgType==message.MSG_OBJECT_TRAN &&
			 * root.getDataType()==message.DATA_META){ Instance
			 * ref=getInstanceRef(); contextAction act=(contextAction)root;
			 * Element ordenEntrada = (Element)act.getContent();
			 * System.out.println("ENTRA ANTES DE SET METADATA");
			 * ref.setMetaData(ordenEntrada); message msg= new
			 * message(message.MSG_CONFIRM);
			 * sendHTTPResponse(encodings,response, msguid,user,msg.toString());
			 * }
			 */
			/*
			 * if(msgType==message.MSG_QUERY &&
			 * root.getDataType()==message.DATA_THREAD){; Instance
			 * ref=getInstanceRef(); sendHTTPResponse(encodings, response,
			 * msguid, user, ref.getThreadData(
			 * root.getIntPropertie(properties.processType),
			 * root.getIntPropertie(properties.currPro),
			 * root.getIntPropertie(properties.currTask))); }
			 */

			/*
			 * if( root.hasPropertie(properties.XALoop) ){ sendMessage(msguid,
			 * user, m_jmsPool, body,true); m_jmsPool.commitSender(user);
			 * return; }
			 */

			/*
			 * if( (msgType==message.MSG_FLOW || msgType==message.MSG_OWNING )||
			 * msgType==message.MSG_OBJECT_TRAN &&
			 * orderType!=message.ACTION_GET){ flowAction act=(flowAction)root;
			 * if( orderType==message.FLOW_END_PRO && act.getCurrProcess()==0 ){
			 * act.setType( message.MSG_CONFIRM );
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,act.toHeaderString()); return; } String msgIn=null;
			 * try{ if (reenviar){ m_jmsPool.rollbackReceiver(user); msgIn =
			 * receiveMsg(user,m_jmsPool,JMS_POLLING_TIMEOUT,msguid,false); }
			 * }catch(GWException e){ if(e.GetCode()!=message.ERROR_TIME_OUT)
			 * throw e; } if(msgIn==null){ puede que estemos en reenvio, que el
			 * mensaje original se haya procesado y que el reenvio haya llegado
			 * antes de la respuesta. Entonces el motor debe soportar que le
			 * llege mensajes duplicados System.out.println("FLOWMSG MSG NULL");
			 * sendMessage(msguid, user, m_jmsPool, body,true);
			 * System.out.println("FLOWMSG ENVIADO");
			 * m_jmsPool.commitSender(user);
			 * System.out.println("FLOWMSG COMMIT"); if(
			 * act.hasPropertie(properties.testID) &&
			 * act.getStrPropertie(properties.testID).equals("CONCURR") ){
			 * m_jmsPool.rollbackReceiver(user);
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,buildError(message.ERROR_DATA,"ERROR")); return; } }
			 * if( msgType==message.MSG_OBJECT_TRAN && root instanceof
			 * lockContainer){ //Existe el riesgo que caiga el sistema antes de
			 * aque, las operaciones se hayan realizado pero //continuen los
			 * bloqueos hasta el siguiente polling del usuario o hasta que deje
			 * de estar vivo Iterator itr = ((lockContainer)
			 * root).getUnLockIterator(); while (itr.hasNext()) { Integer id =
			 * (Integer) itr.next(); unLockInstance(request, response,
			 * encodings, user, msguid, id.intValue()); } } if( justSendConfirm
			 * ){ act.setType(message.MSG_CONFIRM);
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,act.toHeaderString()); }else{ if(msgIn==null){
			 * System.out.println("FLOWMSG PREREC"); msgIn = receiveMsg(user,
			 * m_jmsPool,JMS_CURRENT_TIMEOUT, msguid,false);
			 * System.out.println("FLOWMSG REC"); }
			 * sendHTTPResponse(encodings,response, msguid,user,msgIn);
			 * System.out.println("REALIZADO COMMIT DE FLOW"); } }
			 */

			/*
			 * if(name.equals("GETCONFIG")){ int
			 * config=Integer.parseInt(root.getAttributeValue("CONFIG"));
			 * Instance ref=getInstanceRef();
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,ref.getConfig(config)); ref.commit(); }
			 * if(name.equals("GETVIEW")){ Instance ref=getInstanceRef();
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,ref.getConsultas(user)); ref.commit(); }
			 * if(name.equals("GETREPORTS")){ Instance ref=getInstanceRef();
			 * sendHTTPResponse(encodings,response,
			 * msguid,user,ref.getReports(user)); ref.commit(); }
			 */

			/*
			 * if(msgType==message.MSG_QUERY &&
			 * root.getDataType()==message.DATA_TASK_STATE){ Instance ref =
			 * getInstanceRef(); System.out.println("GETOUTTASKTRANS"); message
			 * res =
			 * ref.getOutTaskTrans(root.getUser(),root.getIntPropertie(properties
			 * .currTask)); sendHTTPResponse(encodings, response, msguid, user,
			 * res.toString()); } if(msgType==message.MSG_QUERY &&
			 * root.getDataType()==message.DATA_OWNING){ Instance ref =
			 * getInstanceRef(); message res = ref.getCurrentTran(user);
			 * sendHTTPResponse(encodings, response, msguid, user,
			 * res.toString()); }
			 */

		} catch (DataErrorException e) {
			if (e.GetCode() == DataErrorException.ERROR_DATA)
				GWError(encodings, response, msguid, message.ERROR_DATA_REMOTE,
						e.getMessage(), e);
			else {
				System.out.println("Data error de sistema");
				e.printStackTrace();
				GWError(encodings, response, msguid, message.ERROR_SYSTEM,
						e.getMessage(), e);
			}
			// }catch(JMSException e){
			// GWError(encodings,response, msguid,
			// message.ERROR_SYSTEM,e.getMessage(),e);
		} catch (GWException e) {
			GWError(encodings, response, msguid, e.GetCode(), null, e);
			// }catch(java.rmi.RemoteException e){
			// GWError(encodings,response, msguid, message.ERROR_SYSTEM,
			// e.getMessage(),e);
			// }catch(dynagent.common.exceptions.InstanceLockedException e){
			// GWError(encodings,response, msguid, message.ERROR_LOCKED,
			// e.getMessage(), e);
		} catch (dynagent.server.exceptions.MessageException e) {
			// if (e.getCode()==message.ERROR_LOCKED)
			GWError(encodings, response, msguid, e.getCode(), e.getMessage(), e);
			// else
			// GWError(encodings,response, msguid, e.getCode(), null, e);
			/*
			 * }catch(javax.ejb.CreateException e){ GWError(encodings,response,
			 * msguid, message.ERROR_SYSTEM, e.getMessage(),e);
			 * }catch(javax.naming.NamingException e){
			 * GWError(encodings,response, msguid, message.ERROR_SYSTEM,
			 * e.getMessage(),e);
			 */
			/*
			 * }catch(ApsException e){ e.printStackTrace(); if
			 * (e.getType()==ApsExceptionTypes.ERROR_TO_SHOW_IN_APPLICATION)
			 * GWError(encodings,response, msguid, message.ERROR_APS,
			 * e.getSystemMessage(), e);
			 */
		}catch (Exception e) {
			System.out.println("msguid " + msguid);
			e.printStackTrace();
			GWError(encodings, response, msguid, message.ERROR_SYSTEM, e.getMessage(), e);
		} finally {
			if (!premium) {
				numRequestNotPremium--;
			}
		}
	}

	private void processMsgSendEmail(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws GWException, RemoteException, CreateException,
			NamingException, InterruptedException, NoSuchFieldException {
		Instance ref = getInstanceRef(bns);

		String email = root.getStrPropertie(properties.email);
		int ido = root.getIntPropertie(properties.emailIdo);
		int idto = root.getIntPropertie(properties.emailIdto);
		String reportFileName = root
				.hasPropertie(properties.emailReportFileName) ? root
				.getStrPropertie(properties.emailReportFileName) : null;
		String subject = root.getStrPropertie(properties.emailSubject);
		CDATA CData = (CDATA) ((Element) root.getContent()).getContent().get(0);
		String body = CData.getText();
		int idoMiEmpresa = root.getIntPropertie(properties.emailIdoMiEmpresa);
		int idoDestinatario = root
				.getIntPropertie(properties.emailIdoDestinatario);

		boolean success = ref.sendEmail(ido, idto, reportFileName, email,
				subject, body, idoMiEmpresa, idoDestinatario, bns);

		if (reportFileName != null) {
			File report = new File(dynaFile, "reports/" + reportFileName)
					.getAbsoluteFile();
			boolean deleted = report.delete();
			if (!deleted) {
				System.err.println("No se pudo borrar el fichero: "
						+ report.getAbsolutePath());
			}
		}
		message msg = new message(message.MSG_CONFIRM);
		msg.addPropertie(properties.success, success);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgSendServerLogEmail(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws GWException, RemoteException, CreateException,
			NamingException, InterruptedException, NoSuchFieldException {
		Instance ref = getInstanceRef(bns);

		String email = root.getStrPropertie(properties.email);
		String subject = root.getStrPropertie(properties.emailSubject);
		String body = "";
		List content = ((Element) root.getContent()).getContent();
		if (!content.isEmpty()) {
			CDATA CData = (CDATA) content.get(0);
			body = CData.getText();
		}

		boolean success = ref.sendServerLogEmail(email, subject, body, bns);

		message msg = new message(message.MSG_CONFIRM);
		msg.addPropertie(properties.success, success);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgUseLicenseCode(HttpServletResponse response,
			String encodings, String msguid, message root, String user)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, NoSuchFieldException, GWException,
			SQLException {
		String code = root.getStrPropertie(properties.licenseCode);
		DataSource ds = null;
		java.sql.Connection conn = null;
		Statement st = null;
		ResultSet set = null;
		message msg = new message(message.MSG_CONFIRM);
		try {
			InitialContext ic = new InitialContext();
			ds = (DataSource) ic.lookup("java:jdbc/dynaglobal");
			conn = ds.getConnection();
			conn.setAutoCommit(true);
			st = conn.createStatement();

			set = st.executeQuery("SELECT * FROM licensecodes WHERE code='"
					+ code + "' and used is null");
			if (set.next()) {
				int users = set.getInt("users");
				String type = set.getString("type");
				long expiredDate = set.getLong("expired");

				msg.addPropertie(properties.licenseConcurrentUsers, users);
				msg.addPropertie(properties.licenseType, type);
				msg.addPropertie(properties.licenseExpiredDate, expiredDate);

				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTimeInMillis(System.currentTimeMillis());
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				String used = dateFormat.format(calendar.getTime());

				st.executeUpdate("UPDATE licensecodes SET used='" + used
						+ "' where code='" + code + "'");
				sendHTTPResponse(encodings, response, msguid, user,
						msg.toString());
			} else {
				sendHTTPResponse(
						encodings,
						response,
						msguid,
						user,
						buildError(message.ERROR_LICENSE_CODE_UNAVAILABLE, user));
			}

		} finally {
			if (st != null) {
				st.close();
			}
			if (conn != null) {
				conn.close();
			}
			if (set != null) {
				set.close();
			}
		}
	}

	private void processMsgUpdateLicense(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, NoSuchFieldException, GWException {
		Instance ref = getInstanceRef(bns);
		int users = root.getIntPropertie(properties.licenseConcurrentUsers);
		long expiredDate = root.getLongPropertie(properties.licenseExpiredDate);
		int type = root.getIntPropertie(properties.licenseType);
		License license = new License(expiredDate, users, type);
		ref.setLicense(bns, license);
		message res = new message(message.MSG_CONFIRM);
		sendHTTPResponse(encodings, response, msguid, user, res.toString());
	}

	private void processMsgDateNow(HttpServletResponse response,
			String encodings, String msguid, message root, String user)
			throws GWException {
		System.out.println("GW:RECIBIDA DATE_NOW");
		message res = new message(message.MSG_DATE_NOW);
		res.addPropertie(properties.currentTime, System.currentTimeMillis());
		sendHTTPResponse(encodings, response, msguid, user, res.toString());
		System.out.println("GW:ENVIADA RES");
	}

	private void processMsgObjectTranWithActionGetJRXML(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			MessageException, GWException {
		Instance ref = getInstanceRef(bns);
		contextAction cAction = (contextAction) root;
		ArrayList<String> reports = ref
				.getJRXML(user, cAction.getTO_ctx(), bns);
		contextAction msg = new contextAction(message.ACTION_GET_JRXML);
		msg.addAllPropertie(properties.jrxml, reports);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private boolean processMsgLogin(HttpServletRequest request,
			HttpServletResponse response, String encodings, String msguid,
			message root, String clientSession, String user, String keySession,
			int bns, boolean configurationMode, boolean overwriteSession)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, NoSuchFieldException,
			SystemException, GWException, RemoveException, FinderException,
			SQLException {
		System.out.println("SOLICITANDO LOGIN:" + user);
		String businessName = bns == 0 ? root
				.getStrDirectAttribute(UserConstants.BUSINESS_NAME) : null;
		if (bns == 0 && businessName != null) {
			// Cogemos de customer el numero de business apartir del name
			DataSource ds = null;
			java.sql.Connection conn = null;
			Statement st = null;
			ResultSet set = null;
			try {

				InitialContext ic = new InitialContext();
				ds = (DataSource) ic.lookup("java:jdbc/dynaglobal");
				conn = ds.getConnection();
				conn.setAutoCommit(true);
				st = conn.createStatement();
				System.err.print("business buscada en customer:" + bns);

				set = st.executeQuery("SELECT business FROM customer WHERE name='"
						+ businessName + "'");
				if (set.next()) {
					bns = set.getInt("business");
				}
			} finally {
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
				if (set != null) {
					set.close();
				}
			}
		}
		Instance miInst = getInstanceRef(bns);
		String password = root.getStrDirectAttribute(UserConstants.USER_PWD);
		message msg = miInst
				.getUserInfo(user, password, configurationMode, bns);
		if (msg.getSuccess()/*
							 * &&
							 * !root.getStrDirectAttribute(UserConstants.USER_PWD
							 * ).equals(UserConstants.byPassKey)
							 */) {
			user = msg.getUser();
			// recupero el user de la respuesta porque puede variar las
			// mayusculas
			License license = null;
			try {
				license = miInst.getLicense(bns);
			} catch (Exception ex) {
				sendHTTPResponse(encodings, response, msguid, user,
						buildError(message.ERROR_LICENSE_CORRUPT, user));
				return false;
			}
			Long date = license.getExpiredDate();
			if (date == null) {
				sendHTTPResponse(encodings, response, msguid, user,
						buildError(message.ERROR_LICENSE_MISSING, user));
				return false;
			} else if (GregorianCalendar.getInstance().getTimeInMillis() >= date) {
				sendHTTPResponse(encodings, response, msguid, user,
						buildError(message.ERROR_LICENSE_DATE, user));
				return false;
			}

			boolean add = false;
			while (!add) {
				Session session = Session.getSession(keySession, bns);
				if (session != null) {
					if (!overwriteSession) {
						if (Auxiliar.equals(String.valueOf(license.getType()),
								Constants.DEMO_SUBSCRIPTION)) {
							// Si es demo se reutiliza la sesion creada
							add = true;
						} else {
							sendHTTPResponse(
									encodings,
									response,
									msguid,
									user,
									buildError(
											message.ERROR_USER_ALREADY_LOGGED,
											user));
							return false;
						}
					} else {
						// si esta el usuario hay que eliminar bloqueos y
						// reserva
						System.out.println("READ ONLY "+readOnly);
						if(!readOnly){
							miInst.resetLocks(user, bns);
							try{
							miInst.resetReservations(user, bns);
							}catch(SystemException e){
								if(e.getMessage().contains("transacción de sólo lectura")){
									System.out.println("ASIGNANDO READ ONLY");
									readOnly=true;
								}else
									throw e;
							}
							if(!readOnly)	add = Session.updateIdClient(keySession, clientSession,	bns);
							else add=true;
						}else{
							add=true;
						}
					}
				}
				
				if(!add && readOnly) add=true;
				
				if (!add) {
					// Si no esta logado en la tabla sessiones
					HashSet<String> users = miInst.getNumberOfSessions(bns);
					Integer numberUsers = license.getUsers();
					System.err.println("CONCURRENT USERS " + users.size()
							+ " maximo:" + numberUsers);
					if (numberUsers == null) {
						sendHTTPResponse(encodings, response, msguid, user,
								buildError(message.ERROR_LICENSE_MISSING, user));
						return false;
					} else if (!users.contains(user)
							&& users.size() >= numberUsers) {
						// Si no existe session y se han superado los usarios
						// maximos de la session
						sendHTTPResponse(
								encodings,
								response,
								msguid,
								user,
								buildErrorParams(message.ERROR_LICENSE_USER,
										user, numberUsers));
						return false;
					}
					// crear
					add = Session.create(keySession, clientSession, bns);
				}
			}

			// Cogemos de customer la informacion necesaria para el applet para
			// gestionar el tipo de version de subscripción del usuario
			DataSource ds = null;
			java.sql.Connection conn = null;
			Statement st = null;
			ResultSet set = null;
			try {
				Integer type = license.getType();
				if (type != null) {
					msg.addDirectAttribute(UserConstants.SUBSCRIPTION_TYPE,
							String.valueOf(type));
				}

				if (!Auxiliar.equals(Constants.DEMO_SUBSCRIPTION,
						String.valueOf(type))
						&& !Auxiliar.equals(Constants.CUSTOM_SUBSCRIPTION,
								String.valueOf(type))) {
					long total = date
							- GregorianCalendar.getInstance().getTimeInMillis();
					long totalSeconds = total / Constants.TIMEMILLIS;
					int days = (int) Math.ceil((double) totalSeconds
							/ (double) (60 * 60 * 24));
					System.err.println("DAYS END SUBSCRIPTION " + days);

					msg.addDirectAttribute(
							UserConstants.EXPIRED_SUBSCRIPTION_DAYS,
							String.valueOf(days));

					InitialContext ic = new InitialContext();
					ds = (DataSource) ic.lookup("java:jdbc/dynaglobal");
					conn = ds.getConnection();
					conn.setAutoCommit(true);
					st = conn.createStatement();
					System.err.print("business buscada en customer:" + bns);

					set = st.executeQuery("SELECT name FROM customer WHERE business="
							+ bns);
					if (set.next()) {
						String name = set.getString("name");

						if (name != null) {
							msg.addDirectAttribute(UserConstants.EMAIL, name);
						}
					}
				}
			} catch (Exception ex) {
				System.err
						.println("ERROR: Problema al acceder a la tabla customer. Solo es un problema para gestionar las diferentes subscripciones del usuario pero la aplicación funcionara correctamente");
				ex.printStackTrace();
			} finally {
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
				if (set != null) {
					set.close();
				}
			}
		}
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
		return true;
	}

	private void processMsgLockOrUnlock(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, NotFoundException,
			IncoherenceInMotorException, IncompatibleValueException,
			CardinalityExceedException, OperationNotPermitedException,
			GWException {
		// try{
		if(!readOnly){
			Instance ref = getInstanceRef(bns);
	
			Iterator itr = ((lockContainer) root).getLockIterator();
			while (itr.hasNext()) {
				Integer idto = (Integer) itr.next();
				HashSet<Integer> hIdos = ((lockContainer) root).getLock(idto);
				Iterator itr2 = hIdos.iterator();
				while (itr2.hasNext()) {
					Integer ido = (Integer) itr2.next();
					ref.lockObject(ido, idto, user, bns);
					// lockInstance(request, response, encodings, user,
					// msguid, id.intValue());
				}
			}
			itr = ((lockContainer) root).getUnLockIterator();
			while (itr.hasNext()) {
				Integer idto = (Integer) itr.next();
				HashSet<Integer> hIdos = ((lockContainer) root).getUnLock(idto);
				Iterator itr2 = hIdos.iterator();
				while (itr2.hasNext()) {
					Integer ido = (Integer) itr2.next();
					ref.unlockObject(ido, idto, user, bns);
					// unLockInstance(request, response, encodings, user,
					// msguid, id.intValue());
				}
			}
		}
		// }catch(Exception e){e.printStackTrace();}
		root.setType(message.MSG_CONFIRM);
		sendHTTPResponse(encodings, response, msguid, user,
				root.toHeaderString());
	}

	private void processMsgObjectTran(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			int orderType, int bns, Integer userRol, Integer windowSession)
			throws EJBException,RemoteException, CreateException, NamingException,
			InterruptedException, SystemException, GWException,
			MessageException/*
							 * , InstanceLockedException, DataErrorException,
							 * OperationNotPermitedException
							 */{
		switch (orderType) {
		case message.ACTION_GETRDN:
			processMsgObjectTranWithActionGetrdn(response, encodings, msguid,
					root, user, bns);
			break;
		case message.ACTION_GETRDNLIST:
			processMsgObjectTranWithActionGetrdnList(response, encodings,
					msguid, root, user, bns);
			break;
		case message.ACTION_DESCRIPTION_CLASS:
			processMsgObjectTranWithActionDecriptionClass(response, encodings,
					msguid, root, user, bns);
			break;
		case message.ACTION_DESCRIPTION_PROPERTY:
			processMsgObjectTranWithActionDescriptionProperty(response,
					encodings, msguid, root, user, bns);
			break;
		case message.ACTION_DESCRIPTION_PROPERTIES:
			processMsgObjectTranWithActionDescriptionProperties(response,
					encodings, msguid, root, user, bns);
			break;
		case message.ACTION_DESCRIPTION_INDIVIDUAL:
			processMsgObjectTranWithActionDescriptionIndividual(response,
					encodings, msguid, root, user, bns);
			break;
		case message.ACTION_DESCRIPTION_INDIVIDUALS:
			processMsgObjectTranWithActionDescriptionIndividuals(response,
					encodings, msguid, root, user, bns);
			break;
		case message.ACTION_GET:
			processMsgObjectTranWithActionGet(response, encodings, msguid,
					root, user, bns, userRol);
			break;
		case message.ACTION_MODIFY:			
			processMsgObjectTranWithActionModify(response, encodings, msguid,
					root, user, bns, windowSession);
			break;
		case message.ACTION_GET_JRXML:
			processMsgObjectTranWithActionGetJRXML(response, encodings, msguid,
					root, user, bns);
			break;
		default:
			break;
		}
	}

	private void processMsgExeTranAction(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			int orderType, int bns) throws RemoteException, CreateException,
			NamingException, InterruptedException, MessageException,
			GWException {
		if (orderType == message.ACTION_REPORT) {
			processMsgExeTranActionWithActionReport(response, encodings,
					msguid, root, user, bns);
		}
	}

	private void processMsgObjectTranWithActionModify(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns, Integer windowSession)
			throws EJBException,RemoteException, CreateException, NamingException,
			InterruptedException/*
								 * , InstanceLockedException,
								 * DataErrorException,
								 * OperationNotPermitedException
								 */, GWException {
		contextAction act = (contextAction) root;
		Element content = (Element) act.getContent();
		System.err.println("TESTING CONTENT: " + content);
		// Document dataDocument = new
		// Document(content.getChild("FACTS").detach());

		// String res=null;
		/*
		 * String dest = null; try { dest =
		 * act.getStrPropertie(properties.destination); }
		 * catch(NoSuchFieldException e) {
		 * System.out.println("No tiene guardado el destinatario, se trata del ERP"
		 * ); } if (dest!=null && dest.equals("APS")) {
		 * System.out.println("Planificacion APS"); Object boundObject =
		 * EJBcontext.lookup("apsBean/IApsBeanLocal"); IApsHome home =
		 * (IApsHome)PortableRemoteObject.narrow (boundObject, IApsHome.class);
		 * //comprobacion al crear llamando a findByPrimaryKey x si esta, si
		 * excepcion crear, si no usarlo
		 * 
		 * IApsBean apsBean = null; try { ApsKey apsKey = new ApsKey("1");
		 * apsBean = home.findByPrimaryKey(apsKey); } catch (FinderException e)
		 * { apsBean = home.create("1"); } apsBean.launchAps(); Changes changes
		 * = new Changes(); message msg= new message(message.MSG_CHANGES);
		 * msg.setContent(changes);
		 * sendHTTPResponse(encodings,response,msguid,user,msg.toString()); }
		 * else {
		 */
		System.out.println("DBGIN1");
		/*
		 * if( act.isLock() &&
		 * !lockInstance(request,response,encodings,user,msguid
		 * ,act.getIDO_ctx())) return;
		 */

		// boolean recursive=
		// act.getBoolPropertie(dynagent.communication.properties.recursive);
		Instance ref = getInstanceRef(bns);
		IndividualData iData = act.getIndividualData();
		Document xmlData = act.getXmlData();
		if (iData != null || xmlData != null) {
			// ref.reserveIndividual(act.getUserRol(), act.getUser(), iData);
			boolean success = false;
			try {
				usersTransition.add(user);
				System.err.println("Añadiendo " + user + " al array users");
				Changes changes = null;
				boolean migration = root.getBoolPropertie(properties.migration);
				boolean keepTableIds = root
						.getBoolPropertie(properties.keepTableIds);
				String replicaSource = null;
				try {
					replicaSource = root
							.getStrPropertie(properties.replicaSource);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("WARNING: processMsgObjectTranWithActionModify sin la property replicaSource");
				}
				changes = ref.transitionObject(act.getUserRol(),act.getUser(), xmlData==null?iData:xmlData,msguid, windowSession, bns,migration, keepTableIds, replicaSource);
				/* else if (xmlData != null) {
					// en xml data el replicador que lo envia ya ha resuelto los
					// tableid locales a esta maquina, si bien internamente se
					// actualiza tambien
					boolean preprocess = root
							.getBoolPropertie(properties.preprocess);
					changes = ref.transitionObject(act.getUserRol(),
							act.getUser(), xmlData, msguid, windowSession, bns,
							migration, preprocess, keepTableIds, replicaSource);
				}*/
				message msg = new message(message.MSG_CHANGES);
				msg.setContent(changes);
				sendHTTPResponse(encodings, response, msguid, user,
						msg.toString());
				success = true;

			} finally {
				System.err.println("Quitando " + user + " del array users");
				usersTransition.remove(user);
				if (!success) {
					// Si ha habido algun error borramos los archivos subidos
					// por el usuario guardados en disco ya que no habra ninguna
					// referencia a ellos en base de datos.
					// Esto solo ocurre porque primero se sube el archivo y
					// luego se envian los facts que hacen referencia a ese
					// archivo, por lo que si en ese momento hay un error hay
					// que borrar el archivo
					ArrayList<File> files = uploadedFilesMap.get(user);
					if (files != null) {
						for (File file : files) {
							file.delete();
						}
					}
				}
				uploadedFilesMap.remove(user);// Limpiamos el mapa de archivos
												// subidos por ese usuario
			}
		} /*
		 * else { Object content = act.getContent(); if (content!=null &&
		 * content instanceof Element) {
		 * System.out.println("ENTRA ANTES DE SET METADATA");
		 * ref.setMetaData((Element)content, user, userRol); } }
		 */
		// }
	}

	private void processMsgObjectTranWithActionGet(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns, Integer userRol)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, GWException {
		if (root.getDataType() == message.DATA_TASK_STATE) {
			Instance ref = getInstanceRef(bns);
			selectData sd = ref.getTasks(userRol, user, bns);
			message msg = new message(message.MSG_SELECTION);
			msg.setContent(sd);
			// msg.setType(message.MSG_SELECTION);
			sendHTTPResponse(encodings, response, msguid, user, msg.toString());
		} else if (root.getDataType() == message.DATA_INDIVIDUAL
				|| root.getDataType() == message.DATA_INDIVIDUAL_CLASS
				|| root.getDataType() == message.DATA_INDIVIDUAL_CLASS_SPECIALIZED) {
			contextAction act = (contextAction) root;
			// String res=null;
			System.out.println("DBGIN1");
			/*
			 * if( act.isLock() &&
			 * !lockInstance(request,response,encodings,user,
			 * msguid,act.getIDO_ctx())) return;
			 */

			// boolean lightview=
			// act.getBoolPropertie(dynagent.communication.properties.lightView);
			// boolean recursive=
			// act.getBoolPropertie(dynagent.communication.properties.recursive);
			Instance ref = getInstanceRef(bns);
			int levels = 1;
			try {
				levels = act.getIntPropertie(properties.levels);
			} catch (NoSuchFieldException e) {
				levels = 1;
			}
			boolean lastStructLevel = act
					.getBoolPropertie(properties.lastStructLevel);
			boolean returnResults = act
					.getBoolPropertie(properties.returnResults);

			if (root.getDataType() == message.DATA_INDIVIDUAL) {
				IndividualData aipd = null;
				if (act.getAID_ctx() != null)
					aipd = ref.getObj(act.getAID_ctx(), user, act.isLock(),
							levels, lastStructLevel, returnResults, bns);
				else
					aipd = ref.getObj(act.getIDO_ctx(), act.getTO_ctx(), user,
							act.isLock(), levels, lastStructLevel,
							returnResults, bns);
				contextAction msg = new contextAction(message.ACTION_GET);
				msg.setIndividualData(aipd);
				// msg.setContent(data);
				sendHTTPResponse(encodings, response, msguid, user,
						msg.toString());
			} else {
				IndividualData aipd = null;
				if (root.getDataType() == message.DATA_INDIVIDUAL_CLASS_SPECIALIZED)
					aipd = ref.getObjOfClassSpecialized(act.getTO_ctx(), user,
							act.isLock(), levels, lastStructLevel, bns);
				else
					aipd = ref.getObjOfClass(act.getTO_ctx(), user,
							act.isLock(), levels, lastStructLevel, bns);
				contextAction msg = new contextAction(message.ACTION_GET);
				msg.setIndividualData(aipd);
				sendHTTPResponse(encodings, response, msguid, user,
						msg.toString());
			}
		}
	}

	private void processMsgObjectTranWithActionDescriptionProperties(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE DESCRIPTION PROPERTIES");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_DESCRIPTION);
		HashMap<Integer, String> res = ref.getPropertiesDescriptionOfClass(
				act.getTO_ctx(), bns);

		System.out.println("descriptions");
		Iterator<Integer> it = res.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			String value = res.get(key);
			System.out.println("property " + key + ", description " + value);
			msg.addPropertie(key, value);
		}
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgObjectTranWithActionDescriptionProperty(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE DESCRIPTION PROPERTY");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_DESCRIPTION);
		String res = ref.getPropertyDescription(act.getIDO_ctx(), bns);
		System.out.println("description " + res);
		if (res != null)
			msg.addPropertie(properties.id, res);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgObjectTranWithActionDecriptionClass(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE DESCRIPTION CLASS");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_DESCRIPTION);
		String res = ref.getClassDescription(act.getTO_ctx(), bns);
		System.out.println("description " + res);
		if (res != null)
			msg.addPropertie(properties.id, res);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgObjectTranWithActionDescriptionIndividual(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE DESCRIPTION INDIVIDUAL");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_DESCRIPTION);
		String res = ref.getIndividualDescription(act.getIDO_ctx(), bns);
		System.out.println("description " + res);
		if (res != null)
			msg.addPropertie(properties.id, res);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgObjectTranWithActionDescriptionIndividuals(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE DESCRIPTION INDIVIDUALS");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_DESCRIPTION);
		HashMap<Integer, String> res = ref.getIndividualsDescriptionOfClass(
				act.getTO_ctx(), bns);
		System.out.println("description " + res);
		Iterator<Integer> it = res.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			String value = res.get(key);
			System.out.println("ido " + key + ", description " + value);
			msg.addPropertie(key, value);
		}
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgObjectTranWithActionGetrdn(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE GET RDN");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_GETRDN);
		String res = ref.getRdn(act.getIDO_ctx(), act.getTO_ctx(), bns);
		System.out.println("rdn " + res);
		msg.addPropertie(Constants.IdPROP_RDN, res);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgObjectTranWithActionGetrdnList(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			SystemException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE GET RDN LIST");
		contextAction act = (contextAction) root;

		message msg = new message(message.MSG_GETRDNLIST);
		HashMap<Integer, String> res = ref.getRdn(act.getAID_ctx(), bns);
		System.out.println("rdns " + res);
		Iterator<Integer> it = res.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			String value = res.get(key);
			System.out.println("ido " + key + ", rdn " + value);
			msg.addPropertie(key, value);
		}
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgPreprint(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, GWException,
			NoSuchFieldException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE PREPRINT");

		System.out.println("Impresion del mensaje=>" + root.toString());
		message msg = new message(message.MSG_REPORT);
		String className = root.getStrPropertie(properties.className);
		HashMap<String, String> res = ref.prePrint(className, bns);
		if (res != null)
			msg.setDirectAttribute(res);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgExeTranActionWithActionReport(
			HttpServletResponse response, String encodings, String msguid,
			message root, String user, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			MessageException, GWException {
		Instance ref = getInstanceRef(bns);
		System.out.println("ENTRA ANTES DE REPORT");
		contextAction act = (contextAction) root;

		System.out.println("Impresion del mensaje=>" + root.toString());
		boolean directImpresion = root
				.getBoolPropertie(properties.directImpresion);
		String className = null;
		try {
			className = root.getStrPropertie(properties.className);
		} catch (NoSuchFieldException e1) {
		}
		Integer idoFormat = null;
		// String nameProject = null;
		try {
			String idoFormatStr = root.getStrPropertie(properties.format);
			// nameProject = root.getStrPropertie(properties.nameProject);
			if (idoFormatStr != null)
				idoFormat = Integer.parseInt(idoFormatStr);
		} catch (NoSuchFieldException e) {
		}
		message msg = new message(message.MSG_REPORT);
		HashMap<String, String> res = ref.report((Element) root.getContent(),
				act.getUser(), act.getUserTask(), className, /* nameProject, */
				directImpresion, idoFormat, bns);
		if (res != null)
			msg.setDirectAttribute(res);
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgQuery(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			int orderType, int bns, boolean configurationMode,
			String subscription, boolean update) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			MessageException, NoSuchFieldException, GWException {
		if (root.getDataType() == message.DATA_INSTANCE) {
			long ini = System.currentTimeMillis();
			System.out.println("GW:RECIBIDA QUERY");
			if (update) {
				Instance ref = getInstanceRef(bns);
				CDATA CData = (CDATA) ((Element) root.getContent())
						.getContent().get(0);
				String sql = CData.getText();

				List<List<String>> res = ref.queryRules(sql, bns, true);
				System.out.println("GW:RECIBIDA RES");
				message msg = new message(message.MSG_QUERYRULES);
				msg.setContent(res);
				sendHTTPResponse(encodings, response, msguid, user,
						msg.toString());
				System.out.println("GW:ENVIADA RES, time: "
						+ (System.currentTimeMillis() - ini));
			} else {
				if (orderType == message.ACTION_GET) {
					Instance ref = getInstanceRef(bns);
					CDATA CData = (CDATA) ((Element) root.getContent())
							.getContent().get(0);
					String sql = CData.getText();
					List<List<String>> res = ref.queryRules(sql, bns, false);
					System.out.println("GW:RECIBIDA RES");
					message msg = new message(message.MSG_QUERYRULES);
					msg.setContent(res);
					sendHTTPResponse(encodings, response, msguid, user,
							msg.toString());
					System.out.println("GW:ENVIADA RES, time: "
							+ (System.currentTimeMillis() - ini));
				} else {
					int mode = root.getIntPropertie(properties.modeQuery);
					Integer uTask = root.getUserTask();
					Element elem = (Element) root.getContent();
					Instance ref = getInstanceRef(bns);
					String res = ref.query(elem, uTask, mode, bns);
					System.out.println("GW:RECIBIDA RES");
					sendHTTPResponse(encodings, response, msguid, user, res);
					System.out.println("GW:ENVIADA RES, time: "
							+ (System.currentTimeMillis() - ini));
				}
			}
		} else if (root.getDataType() == message.DATA_META) {
			Element res = null;
			// Si estamos en modo DEMO usamos la cache del metadata, si existe
			if (Auxiliar.equals(subscription, Constants.DEMO_SUBSCRIPTION)
					&& this.metadataCache != null && !configurationMode) {
				res = metadataCache;
			} else {
				Instance ref = getInstanceRef(bns);
				System.out.println("ENTRA ANTES DE GET METADATA");
				res = ref.getMetaData(user, configurationMode, bns);
				if (Auxiliar.equals(subscription, Constants.DEMO_SUBSCRIPTION)
						&& !configurationMode) {
					metadataCache = res;
				}
			}
			root.setContent(res);
			sendHTTPResponse(encodings, response, msguid, user, root.toString());
		}
	}

	private void processMsgReserve(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			int orderType, int bns, Integer windowSession)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, NoSuchFieldException,
			GWException {
		long ini = System.currentTimeMillis();
		if (orderType == message.ACTION_NEW_RESERVE) {
			System.out.println("GW:RECIBIDA NEW_RESERVE");
			Instance ref = getInstanceRef(bns);
			ArrayList<Reservation> aReservation = (ArrayList<Reservation>) root
					.getContent();
			HashMap<DomainProp, Double> hMap = ref.reserve(aReservation, user,
					windowSession, bns);
			System.out.println("GW:RECIBIDA RES");
			message msg = new message(message.MSG_RESERVE);
			msg.setOrderType(message.ACTION_RESULT_RESERVE);
			msg.setContent(hMap);
			sendHTTPResponse(encodings, response, msguid, user, msg.toString());
			System.out.println("GW:ENVIADA RES, time: "
					+ (System.currentTimeMillis() - ini));
		} else if (orderType == message.ACTION_DEL_RESERVE) {
			System.out.println("GW:RECIBIDA DEL_RESERVE");
			Instance ref = getInstanceRef(bns);
			ArrayList<Reservation> aReservation = (ArrayList<Reservation>) root
					.getContent();
			ref.deleteReservation(aReservation, user, windowSession, bns);
			System.out.println("GW:RECIBIDA RES");
			message res = new message(message.MSG_CONFIRM);
			sendHTTPResponse(encodings, response, msguid, user, res.toString());
			System.out.println("GW:ENVIADA RES, time: "
					+ (System.currentTimeMillis() - ini));
		}
	}

	private void processMsgMigration(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			int orderType, int bns) throws RemoteException, CreateException,
			NamingException, InterruptedException, MessageException,
			NoSuchFieldException, GWException, OperationNotPermitedException {
		long ini = System.currentTimeMillis();
		System.out.println("GW:RECIBIDA MIGRATION");
		Instance ref = getInstanceRef(bns);
		Element xmlData = (Element) root.getContent();
		Integer idRoot = ref.transitionDataMigration(xmlData, bns);
		System.out.println("GW:RECIBIDA RES");

		message res = new message(message.MSG_CONFIRM);
		if (idRoot != null)
			res.addPropertie(properties.id, idRoot);
		sendHTTPResponse(encodings, response, msguid, user, res.toString());
		System.out.println("GW:ENVIADA RES, time: "
				+ (System.currentTimeMillis() - ini));
	}

	private void processMsgLogError(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, GWException {
		root.setResultCode(message.MSG_CONFIRM);
		errorTrace msgError = (errorTrace) root;
		Instance ref = getInstanceRef(bns);
		ref.logError(user, msgError.getDebug(), msgError.getError(),
				msgError.getDesc(), bns);
		sendHTTPResponse(encodings, response, msguid, user,
				root.toHeaderString());
	}

	private void processMsgPooling(HttpServletResponse response,
			String encodings, String msguid, String user, int bns)
			throws GWException {
		// System.out.println("DENTRO POLLING");
		// devolver posterior a fecha obtenida de restar a la actual el
		// liveTime(60s)
		long tiempoInic = start - liveTime;
		String dateInic = DateFormatUtils.format(new Date(tiempoInic),
				"yyyy-MM-dd HH:mm:ss");

		boolean existe = false;
		Statement st = null;
		ResultSet rs = null;
		java.sql.Connection con = null;
		try {
			// consulta obteniendo si hay algun msg en owning_msg posterior a
			// esa fecha
			String sql = "SELECT USUARIO,ROL FROM OWNING_MSG WHERE TIMESTAMP>'"
					+ dateInic + "'";
			System.out.println("sql " + sql);
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				existe = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con != null)
					con.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		message msg = new message(message.MSG_POOLING);
		if (existe)
			msg.setResultCode(message.OWNING_CHANGED);
		else
			msg.setResultCode(message.SUCCESSFULL);

		/*
		 * String respuesta=null; //resultCode indicando actualizacion de tareas
		 * if(reenviar){ m_jmsPool.rollbackReceiver(user); respuesta =
		 * receiveBlock(user, false); } if(!reenviar || respuesta==null){
		 * sendMessage(msguid, user, m_jmsPool, body,true);
		 * m_jmsPool.commitSender(user); respuesta = receiveBlock(user, true); }
		 * sendHTTPResponse(encodings,response, msguid,user,respuesta);
		 */
		sendHTTPResponse(encodings, response, msguid, user, msg.toString());
	}

	private void processMsgResetLocks(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			String keySession, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			MessageException, GWException, SQLException {
		Instance ref = getInstanceRef(bns);
		if (ref != null) {
			ref.resetLocks(user, bns);
		}
		Session.remove(keySession, bns);
		root.setType(message.MSG_CONFIRM);
		sendHTTPResponse(encodings, response, msguid, user,
				root.toHeaderString());
	}

	private void processMsgDisconnection(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			String keySession, int bns) throws RemoteException,
			CreateException, NamingException, InterruptedException,
			MessageException, RemoveException, GWException, SQLException {
		if(!readOnly){
			Instance ref = getInstanceRef(bns);
			if (ref != null) {
				ref.resetLocks(user, bns);
				ref.resetReservations(user, bns);
			}
			Session.remove(keySession, bns);
		}
		root.setType(message.MSG_CONFIRM);
		sendHTTPResponse(encodings, response, msguid, user,
				root.toHeaderString());
	}

	private void processMsgChangeMode(HttpServletResponse response,
			String encodings, String msguid, message root, String user,
			String keySession, String clientSession, int bns)
			throws RemoteException, CreateException, NamingException,
			InterruptedException, MessageException, RemoveException,
			GWException, SQLException {
		String oldMode = null;
		try {
			oldMode = root.getStrPropertie(properties.oldMode);
		} catch (NoSuchFieldException e) {
		}

		String oldKeySession = user + "/" + bns + "/" + oldMode;
		Instance miInst = getInstanceRef(bns);
		miInst.resetLocks(user, bns);
		miInst.resetReservations(user, bns);
		Session.remove(oldKeySession, bns);

		boolean add = false;
		while (!add) {
			Session session = Session.getSession(keySession, bns);
			if (session != null) {
				// si esta el usuario hay que eliminar bloqueos y reserva
				miInst.resetLocks(user, bns);
				miInst.resetReservations(user, bns);
				add = Session.updateIdClient(keySession, clientSession, bns);
			}
			if (!add) {
				// crear
				add = Session.create(keySession, clientSession, bns);
			}
		}
		root.setType(message.MSG_CONFIRM);
		sendHTTPResponse(encodings, response, msguid, user,
				root.toHeaderString());
	}

	private void processMsgReportsClasificator(HttpServletResponse response,
			String encodings, String msguid, message root, String user, int bns)
			throws CreateException, NamingException, InterruptedException,
			SQLException, IOException, JDOMException, GWException {
		Instance ref = getInstanceRef(bns);
		ref.reportsClasificator(bns);
		root.setType(message.MSG_CONFIRM);
		sendHTTPResponse(encodings, response, msguid, user,
				root.toHeaderString());
	}

	static String buildError(int error, String user) {
		message msg = new message(message.MSG_CONFIRM);
		msg.setResultCode(error);
		msg.setUser(user);
		return msg.toString();
	}

	static String buildErrorParams(int error, String user, int value) {
		message msg = new message(message.MSG_CONFIRM);
		msg.setResultCode(error);
		msg.setUser(user);
		msg.addPropertie(0, value);
		return msg.toString();
	}

	static String buildNotif(String user) {
		message msg = new message(message.MSG_CONFIRM);
		msg.setUser(user);
		return msg.toString();
	}

	private void GWError(String encodings, HttpServletResponse response,
			String msguid, int error, String desc, Exception ee) {
		message msg = new message(message.MSG_ERROR);
		msg.setResultCode(error);
		Element descElem = new Element("DESCRIPCION");
		descElem.setText(desc);
		msg.setContent(descElem);
		if (msguid != null)
			msg.setMsguid(msguid);
		System.out.println("ERROR GW en : " + msguid + " " + error);
		System.out.println("msg " + msg);
		if (ee != null) {
			ee.printStackTrace();
		}
		try {
			sendHTTPResponse(encodings, response, msguid, "", msg.toString());
		} catch (GWException e) {
			System.out.println("*************** GWError:" + e.getMessage());
		}
	}

	private void sendHTTPResponse(String encodings,
			HttpServletResponse response, String msguid, String user, String str)
			throws GWException {
		try {
			if (str == null) {
				throw new GWException(GWException.ERROR_GW_SYSTEM,
						"GW:sendHTTP:Error respuesta nula", msguid);
			}
			if(encriptar){
				try {
					str=CipherUtils.encrypt(str);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			// long end=System.currentTimeMillis();
			// System.out.println("DURACION:"+(end-start));
			// if(str.indexOf("POLLING")==-1)
			if (str.length() > 101) {
				System.out.println("GW:>>GUI:" + msguid + ":"
						+ str.substring(0, 100));
			} else {
				System.out.println("GW:>>GUI:" + msguid + ":" + str);
			}
			PrintWriter out = null;

			response.addHeader("MSGUID", msguid);

			if ((encodings != null) && (encodings.indexOf("gzip") != -1)) {
				OutputStream out1 = response.getOutputStream();
				out = new PrintWriter(new GZIPOutputStream(out1), false);
				response.setHeader("Content-Encoding", "gzip");
				out.write(str);
			} else {
				out = response.getWriter();
				response.setContentType("xml/text");
				out.println(str);
			}
			out.close();
		} catch (IOException e) {
			throw new GWException(GWException.ERROR_GW_SYSTEM,
					"GW:sendHTTP:IO:" + e.getMessage(), msguid);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		try {
			super.service(req, res);
		} finally {

		}
	}

	/*
	 * private void sendMessage(String msguid, String user, jmsPool pool, String
	 * msg, boolean reset) throws GWException{ String error=null; try{
	 * TextMessage message = null; message =
	 * pool.createSendMessage(user,msguid,reset); message.setText(msg);
	 * message.setStringProperty("user",user);
	 * message.setStringProperty("MSGUID",msguid);
	 * message.setLongProperty("TIME",System.currentTimeMillis());
	 * message.setJMSType("DATA"); message.setJMSCorrelationID( msguid ); //
	 * if(msg.length() > 55) // System.out.println("GW:>>RULER"+msguid+":"+
	 * msg.substring(0,54)); // else System.out.println("GW:>>RULER:"+msguid
	 * +":"+ msg);
	 * 
	 * pool.send(user,message); }catch (JMSException e) { throw new
	 * GWException(helperConstant.ERROR_GW_JMS,
	 * "GW:SEND MSG:SQL"+e.getMessage(),msguid); } }
	 * 
	 * private static String receiveMsg( String user, jmsPool pool,long timeout,
	 * String mgid, boolean reset) throws GWException { try{
	 * System.out.println("ARMANDO RECEPCION PARA USER: "+user+", msgid: "+mgid
	 * ); Message msg=pool.receive(user,timeout,mgid,reset); if(msg!=null)
	 * if(msg instanceof TextMessage){ TextMessage tmsg=(TextMessage) msg;
	 * System.out.println("JMSRESPONSE: USER " + user
	 * +",ID "+mgid+"<<<"+tmsg+",CORRELATION "+tmsg.getJMSCorrelationID());
	 * return tmsg.getText(); } throw new
	 * GWException(message.ERROR_TIME_OUT,"timeout JMS",mgid) ; }catch
	 * (JMSException e) { throw new GWException( helperConstant.ERROR_GW_JMS,
	 * "GW:receiveMsg:JMS:" + e.getMessage(),mgid) ;
	 * }catch(java.util.ConcurrentModificationException e){ System.out.println(
	 * "GW: error de concurrencia JMS:**************************************"+
	 * "*******************************************************************"
	 * +e.getMessage()) ; throw new
	 * GWException(helperConstant.ERROR_GW_JMS,"GW:receiveMsg:CONCURR:"
	 * +e.getMessage(),mgid) ; } }
	 */
}
