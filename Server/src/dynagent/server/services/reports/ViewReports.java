package dynagent.server.services.reports;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.QueryConstants;
import dynagent.server.services.querys.DataInfo;

public class ViewReports {
	
	public static HashMap<String,String> view(int bns,String user, Map<String,String> jrxmls, boolean havePathsJrxmls, int idtoUTask, Connection con, String idMaster, String nameMaster, 
			String path, HashMap<String, String> params, boolean directImpresion,
			int nCopies, String impresora, boolean displayPrintDialog, String postPrintSequence, String format,String idioma) throws JRException, IOException, JDOMException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException, OperationNotPermitedException {
		Map<String, JasperReport> jreports= new HashMap<String, JasperReport>();
		Map<Object,Object> parameters=new HashMap<Object,Object>();	
		
		Iterator<String> it = jrxmls.keySet().iterator();
		while (it.hasNext()){
			String key=it.next();
			//key sera el nombre del report/subreport
//			System.out.println("Key del view=>"+key);
//			System.out.println("View=>"+jrxmls.get(key));
			JasperReport jrtmp=compileReport(jrxmls,havePathsJrxmls,idtoUTask,key,idMaster,path,nameMaster,idioma);
			jreports.put(key, jrtmp);
		}
		parameters=fillParameters(jreports,params);
		HashMap<String,String> rAttributes = new HashMap<String, String>();
		if(impresora!=null) rAttributes.put("impresora", impresora);
		long start = System.currentTimeMillis();
		String nameFile = bns+"_"+ user + "_" + start;
		String pathFile = null;
		if (directImpresion) {
			pathFile = "reports/" + nameFile + ".jrprint";
			//int copies = 1;
			//String nCopies = queryP.getAttributeValue(QueryConstants.N_COPIES);
			//if (nCopies!=null)
				//copies = Integer.parseInt(nCopies);
			//String displayPrintDialogStr = "TRUE";
			//if (queryP.getAttributeValue(QueryConstants.DISPLAY_PRINT_DIALOG)!=null)
				//displayPrintDialogStr = queryP.getAttributeValue(QueryConstants.DISPLAY_PRINT_DIALOG);
			rAttributes.put(QueryConstants.DIRECT_IMPRESION, "TRUE");
			//rAttributes.put(QueryConstants.N_COPIES, String.valueOf(nCopies));Esto ahora se hace en el applet
			if (displayPrintDialog)
				rAttributes.put(QueryConstants.DISPLAY_PRINT_DIALOG, "TRUE");
			else
				rAttributes.put(QueryConstants.DISPLAY_PRINT_DIALOG, "FALSE");
			
			String tempFileName = path+"/"+pathFile;
			JasperFillManager.fillReportToFile(jreports.get(idMaster), tempFileName, parameters, con);
		} else {
			if (format!=null && !format.equals(Constants.PDF)) {
				//usar codigo jasper para exportar pdf
				if(format.equals(Constants.EXCEL)) {
					pathFile = "reports/" + nameFile + ".xls";
					String tempFileName = path+"/"+pathFile;
					JasperPrint viewMaster = JasperFillManager.fillReport(jreports.get(idMaster),parameters,con);
					OutputStream output = new FileOutputStream(new File(tempFileName));
					JRXlsExporter exporterXLS = new JRXlsExporter();
					exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, viewMaster);
					exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, output);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
					//exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
					//exporterXLS.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
					exporterXLS.exportReport();
					//outputfile.write(outputByteArray.toByteArray());
					output.close();
				} else if(format.equals(Constants.RTF)) {
					String pathFileTmp = "reports/" + nameFile + "Tmp.rtf";
					String tempFileName = path+"/"+pathFileTmp;
					JasperPrint viewMaster = JasperFillManager.fillReport(jreports.get(idMaster),parameters,con);
//					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//					JRRtfExporter exporter = new JRRtfExporter();
//					exporter.setParameter(JRExporterParameter.JASPER_PRINT, viewMaster);
//					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
//					exporter.exportReport();
					
//					OutputStream output = new FileOutputStream(new File(tempFileName));
//					output.write(byteArrayOutputStream.toByteArray());
//					output.flush();
//					output.close();
//					byteArrayOutputStream.close();
					
					OutputStream output = new FileOutputStream(new File(tempFileName));
					JRRtfExporter exporter = new JRRtfExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, viewMaster);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
					exporter.exportReport();
					output.close();
					
					pathFile = "reports/" + nameFile + ".rtf";
					String fileName = path+"/"+pathFile;
					FileWriter fichero = new FileWriter(fileName);
					PrintWriter pw = new PrintWriter(fichero);
					
					FileReader f = new FileReader(tempFileName);
					BufferedReader br = new BufferedReader(f);
					String linea;
					boolean reemplace = false;
					while((linea=br.readLine())!=null) {
						if (!reemplace && linea.contains("SansSerif")) {
							linea = linea.replace("SansSerif", "Microsoft Sans Serif");
							reemplace = true;
						}
						pw.println(linea);
					}
					fichero.close();
					f.close();
					(new File(tempFileName)).delete();
				}
			} else {
				pathFile = "reports/" + nameFile + ".pdf";
				String tempFileName = path+"/"+pathFile;
//				System.out.println("idMaster " + idMaster);
//				System.out.println("jreports.get(idMaster) " + jreports.get(idMaster));
				JasperPrint viewMaster = JasperFillManager.fillReport(jreports.get(idMaster),parameters,con);
				//fillReports llama a getFieldValue de JRDSource
				OutputStream output = new FileOutputStream(new File(tempFileName));
				JasperExportManager.exportReportToPdfStream(viewMaster, output);
		//		JasperViewer.viewReport(viewMaster, false);
				output.close();
			}
			rAttributes.put(QueryConstants.DIRECT_IMPRESION, "FALSE");
		}
		rAttributes.put(QueryConstants.PATH_FILE, pathFile);
		if (postPrintSequence!=null)
			rAttributes.put(QueryConstants.POSTPRINT_SEQUENCE, postPrintSequence);
        return rAttributes;
	}
	
	public static HashMap<String,String> viewExcel(int business,String user, HashMap<String,DataInfo> infoResult, ResultSet data, 
			String nameMaster, String path, String titleExcel, ArrayList<String> paramsExcel) 
			throws JRException, IOException, JDOMException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException, OperationNotPermitedException {
		System.out.println("Titulo " + titleExcel);
		System.out.println("PARAMEXCEL");
		Iterator<String> it = paramsExcel.iterator();
		while (it.hasNext()) {
			String param = it.next();
			System.out.println("param " + param);
		}
		
		//long start = System.currentTimeMillis();
		HashMap<String,String> rAttributes = new HashMap<String, String>();
		String nameFile = business+"_"+user;// + "_" + nameMaster + "_" + start;
		String pathFile = "reports/" + nameFile + ".xls";
		String tempFileName = path+"/"+pathFile;
		
		FileOutputStream outputfile=new FileOutputStream(tempFileName);
		HSSFWorkbook xls = new HSSFWorkbook();
		
		keepDataExcel(xls, infoResult, data, titleExcel, paramsExcel);
		
		xls.write(outputfile);
		outputfile.close();
		rAttributes.put(QueryConstants.DIRECT_IMPRESION, "FALSE");
		
		rAttributes.put(QueryConstants.PATH_FILE, pathFile);
        return rAttributes;
	}
	
	private static void keepDataExcel(HSSFWorkbook xls, HashMap<String,DataInfo> infoResult, ResultSet data, String titleExcel, ArrayList<String> paramsExcel) throws JRException, SQLException {
		HSSFSheet sheet = xls.createSheet();
		
		int titleRow = 0;
		if (titleExcel!=null) {
			HSSFFont fontTitle = xls.createFont();
			fontTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			fontTitle.setFontHeightInPoints((short)16);
			HSSFCellStyle stTitle = xls.createCellStyle();
			stTitle.setFont(fontTitle);
			stTitle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			
			HSSFRow titRow = sheet.createRow(titleRow); 
			
			HSSFCell cst = titRow.createCell(0);
			cst.setCellStyle(stTitle);
			cst.setCellValue(titleExcel.toUpperCase());
	
			titleRow = titleRow + 2;
		}
		
		HSSFFont fontNegrita = xls.createFont();
		fontNegrita.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		int parametrosRow = titleRow;
		if (paramsExcel.size()>0) {
			HSSFCellStyle stCabeceraCondiciones = xls.createCellStyle();
			stCabeceraCondiciones.setFont(fontNegrita);

			HSSFRow paramsCabeceraRow = sheet.createRow(parametrosRow); 
			
			HSSFCell cs = paramsCabeceraRow.createCell(0);
			cs.setCellStyle(stCabeceraCondiciones);
			cs.setCellValue("Condiciones:");

			parametrosRow++;
			
			//celdas para parametros
			Iterator<String> it = paramsExcel.iterator();
			while (it.hasNext()) {
				String param = it.next();
				HSSFRow paramsRow = sheet.createRow(parametrosRow); 
				paramsRow.createCell(0).setCellValue(param);
				parametrosRow++;
			}
			parametrosRow++; //fila en blanco
		}
		HSSFCellStyle stCabecera = xls.createCellStyle();
		stCabecera.setFont(fontNegrita);
		stCabecera.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
		stCabecera.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//crear cabecera
		HSSFRow cabeceraRow = sheet.createRow(parametrosRow);
		int totalCeldas = 0;
		Iterator<String> it = infoResult.keySet().iterator();
		while (it.hasNext()) {
			String fieldName = it.next();
			DataInfo fieldShow = infoResult.get(fieldName);
			System.out.println("fieldShow " + fieldShow);
			int column = fieldShow.getColumn();
			int cabeceraCelda = column-1;
			HSSFCell cs = cabeceraRow.createCell(cabeceraCelda);
			cs.setCellStyle(stCabecera);
			cs.setCellValue(fieldName.toUpperCase());
			totalCeldas++;
		}
		
		int i = parametrosRow + 1; //le sumo uno por la fila de la cabecera
		while (data.next()) {
//			System.out.println("itera " + i);
			HSSFRow row = sheet.createRow(i); 
			//dependiendo el tipo hacer el get
			Iterator<String> it2 = infoResult.keySet().iterator();
			while (it2.hasNext()) {
				String fieldName = it2.next();
				DataInfo fieldShow = infoResult.get(fieldName);
				String fieldType = fieldShow.getType();
				int columnQuery = fieldShow.getColumnQuery();
				Object value = data.getObject(columnQuery);
//				System.out.println("fieldName " + fieldName + ", fieldShow " + fieldShow);
//				System.out.println("value " + value);
				int column = fieldShow.getColumn();
				int celda = column-1;
				//System.out.println("celda " + celda);
				if (value!=null) {
					if (fieldType.equals("Integer") ||
//							fieldType.equals("Long") ||
							fieldType.equals("Double")) {
						Double valDouble = null;
						if (value instanceof Integer) {
							Integer valInt = (Integer)value;
							valDouble = valInt.doubleValue();
//						} else if (value instanceof Long) {
//							Long valLong = (Long)value;
//							valDouble = valLong.doubleValue();
						} else
							valDouble = (Double)value;
//						System.out.println("valDouble " + valDouble);
						row.createCell(celda).setCellValue(valDouble);
					} else if (fieldType.equals("String")) {
						row.createCell(celda).setCellValue((String)value);
//					} else if (fieldType.equals("Date")) {
					} else if (fieldType.equals("Long")) {
						row.createCell(celda).setCellValue((String)QueryConstants.secondsToDate(String.valueOf(value), QueryConstants.getPattern(Constants.IDTO_DATETIME)));
					} else if (fieldType.equals("Boolean")) {
						Boolean db = (Boolean)value;
						if (db!=null && db)
							row.createCell(celda).setCellValue(true);
						else
							row.createCell(celda).setCellValue(false); 
					}
				}
			}
			i++;
		}
		for (int c=0;c<totalCeldas;c++) {
			sheet.autoSizeColumn(c); //ajusta el ancho de la columna
		}
		
		if (titleExcel!=null)
			sheet.addMergedRegion(new CellRangeAddress(0,0,0,totalCeldas-1));
	}
	
	private static Map<Object, Object> fillParameters(Map<String, JasperReport> jreports, HashMap<String, String> params) {
		Map<Object, Object> result= new HashMap<Object, Object>();
		Iterator<String> it = jreports.keySet().iterator();
		while (it.hasNext()){
			String key=it.next();
			JasperReport jrtmp=jreports.get(key);
			result.put(key, jrtmp);
			System.out.println("SYSOUAMI: "+key+" -> "+jrtmp.toString());
		}
	
		Set<String> keysString=params.keySet();
		Iterator<String> itString=keysString.iterator();
		while(itString.hasNext()){
			String nameParam=itString.next();
			String value=params.get(nameParam);
			System.out.println("SYSOUAMI: PARAMSREPORT:"+ nameParam + " "+value);
			result.put(nameParam, value);
		}
		return result;
	}
	
	public static String idiom_rename_jrxml_file(String path,String idiom){
		if(idiom==null||!path.endsWith(".jrxml")){
			return path;
		}
		return path.substring(0,path.length()-6)+"_"+idiom+".jrxml";		
	}
	
	private static JasperReport compileReport(Map<String, String> jrxmls, boolean havePathsJrxmls, int idtoUTask, String key, 
			String idMaster, String path, String nameR,String idioma) throws JRException, IOException {
//		String path=System.getProperty("user.dir");
		JasperReport result = null;
		if (havePathsJrxmls) {
			
			String pathAll = jrxmls.get(key);
			boolean traducido=false;
			if(idioma!=null&&pathAll.endsWith(".jrxml")){
				String pathAll_idiom=idiom_rename_jrxml_file(pathAll,idioma);
				try{
					File testF=new File(pathAll_idiom);
					if(testF.exists()){
						result=JasperCompileManager.compileReport(pathAll_idiom);
						traducido=true;
					}else{
						System.out.println("No existe el fichero "+pathAll_idiom);		
					}
				}catch(Exception fe){
					System.out.println("No existe fichero "+pathAll_idiom+"\n"+fe.toString());					
					//fe.printStackTrace();					
				}
			}
			System.out.println("havePathsJrxmls "+pathAll);
			if(!traducido) result = JasperCompileManager.compileReport(pathAll);
		} else {
			//crear fichero temporal con la ruta
			boolean isMaster = key.equals(idMaster);
			String pathRelative = "\\reports\\tmp\\";
			File directorio = new File(path+pathRelative);
			directorio.mkdir();
			String pathFile = GenerateJRXML.getNameFile(nameR, key, isMaster,pathRelative);
			System.out.println(path+pathFile);
			File f = new File(path+pathFile);
			if (f.exists()) {
				f.delete();
				//throw new IOException("El report al que se intenta acceder ya está en uso");
			}
			System.out.println("Entra y crea jrxml");
			//System.out.println(pathFile);
			FileWriter fw = new FileWriter(f);
			System.out.println("key " + key);
			System.out.println("jrxmls.get(key) " + jrxmls.get(key));
			fw.write((idioma==null?"":idioma+"_")+jrxmls.get(key));
			fw.close();
			String pathAll = path+pathFile;
			result = JasperCompileManager.compileReport(pathAll);
			f.delete();
		}
		
//		long start = System.currentTimeMillis();
//		File file = new File(jrxmls.get(key) + "\\" + start);
//		System.out.println("file " + path + "\\reports\\tmp\\" + start + ".jasper");
//		JasperCompileManager.compileReportToFile(path+"\\"+jrxmls.get(key), path + "\\reports\\tmp\\" + start + ".jasper");
		return result;
	}
}
