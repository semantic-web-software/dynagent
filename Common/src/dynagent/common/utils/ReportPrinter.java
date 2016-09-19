package dynagent.common.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.QueryConstants;

public class ReportPrinter {

	public static void printReport(HashMap<String,String> rAttributes,String urlV,boolean automatizar_copias) throws SystemException, MalformedURLException{
		String impresora=rAttributes.get("impresora");
		System.out.println("IMPRESORA "+impresora);

		String printerName =null;
		if(impresora!=null){
			PrintService[] prarr=PrintServiceLookup.lookupPrintServices(null,null);
			bucle1:
			for(PrintService ps:prarr){
				String pntmp=ps.getName();
				
				String[] impr_arr=impresora.split(";");
				for(String imp_name:impr_arr){
					System.out.println("Printer Name is " + pntmp);
					if(pntmp.matches("(?i).*("+imp_name+").*")){
						printerName=pntmp;
						System.err.println("Impresora predefinida " + printerName);
						break bucle1;
					}
				}
			}
		}
		if(printerName==null){
			PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
			printerName=printService.getName();
			System.err.println("Impresora defecto " + printerName);
		}
		
		PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
		//printRequestAttributeSet.add(MediaSizeName.ISO_A4);
		
		int copies = Integer.parseInt(rAttributes.get(QueryConstants.N_COPIES));
		if(copies==0){
			return;
		}
		printRequestAttributeSet.add(new Copies(copies));
		PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
		printServiceAttributeSet.add(new PrinterName(printerName, null)); 
		
		JRPrintServiceExporter exporter = new JRPrintServiceExporter();
		URL u = new URL(urlV.trim().replaceAll(" ", "+")/*Sustituimos los espacios en blanco por + para que lo entienda el protocolo*/);
		
		System.out.println("impresion directa url:"+u);
		exporter.setParameter(JRExporterParameter.INPUT_URL, u);
		exporter.setParameter(
		    JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
		    printRequestAttributeSet);
		exporter.setParameter(
		    JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
		    printServiceAttributeSet);
		exporter.setParameter(
		    JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG,
		    Boolean.FALSE);
		boolean print_dialog=new Boolean(rAttributes.get(QueryConstants.DISPLAY_PRINT_DIALOG));
		if(automatizar_copias) print_dialog=false;
		
		exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG,new Boolean(print_dialog));
		try {
			System.err.println("url:"+u+" copias:"+copies);
			exporter.exportReport();
		} catch (JRException e) {
			e.printStackTrace();
			//Singleton.getMessagesControl().showMessage("Error, no es posible realizar la impresión");
			throw new SystemException(SystemException.ERROR_DATOS,"Error, no es posible realizar la impresión");
		}
	}
	
	public static void printSequence(HashMap<String,String> rAttributes, boolean pre) throws SystemException {
		//imprimir secuencia
		String printSequence = null;
		if (pre)
			printSequence = rAttributes.get(QueryConstants.PREPRINT_SEQUENCE);
		else
			printSequence = rAttributes.get(QueryConstants.POSTPRINT_SEQUENCE);
		if (printSequence!=null) {
			String[] printSequenceSpl = printSequence.split(",");
			byte[] barray = new byte[printSequenceSpl.length];
			
			for (int i=0;i<printSequenceSpl.length;i++){
				Byte bt = new Byte(printSequenceSpl[i]);
				barray[i]=bt.byteValue();
			}
			
			PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
			if(printService!=null){
				DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
				DocPrintJob docPrintJob = printService.createPrintJob();
				Doc doc=new SimpleDoc(barray,flavor,null);
				try {
					docPrintJob.print(doc, null);
				} catch (PrintException e) {
					System.out.println("Error al imprimir: "+e.getMessage());
				}
				System.out.println("FIN DE printSequence");
			}
		}
	}
}
