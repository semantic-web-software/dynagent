package dynagent.common.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;

public class TXTToXMLConverter implements IConverter{

	private String[] headers;
	
	public TXTToXMLConverter(){
		headers = null;
	}

	
	public Document convert(FileInputStream in, String[] header)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Document convert(FileInputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(in.getFD()));
		String line = reader.readLine();
		String separador=";";
		
		//exista al menos 3 campos con el separador
		if(!line.matches(".+\\" + separador + ".+\\" + separador+ ".+")){
			separador="|";
			if(!line.matches(".+\\" + separador + ".+\\" + separador+ ".+")){
				throw new IOException("No localizado caracter separador");
			}
		}
		
		initializeHeader(line,separador);		
		
		line=reader.readLine();
		Document document = new Document();
		Element workbookElement = new Element(TAG_WORKBOOK);
		Element sheetElement = new Element(TAG_SHEET);
		sheetElement.setAttribute("name","Hoja1");
		sheetElement.setAttribute("index","0");
		workbookElement.addContent(sheetElement);
		document.setRootElement(workbookElement);
		int rowId=1;
		do {
			String [] values = line.split(separador);
			Element rowElement = new Element(TAG_ROW);
			rowElement.setAttribute("id",""+rowId++);
			for (int i = 0; i < headers.length; i++) {
				if (i >= values.length){
					System.err.println("La línea: " + line + " no tiene tantos valores como el resto de líneas\nSe esperaban " + headers.length + " valores.");
					//break;
					continue;
				}else{
					String h=headers[i];
					Element valueElement = new Element(h);
					if (values[i]==null){						
						continue;
					}
					if(values[i].matches("\\d+\\,\\d+")){
						valueElement.setText(values[i].replaceAll("\\,","\\.")); 
					}else{
						valueElement.setText(values[i]);
					}					
					rowElement.addContent(valueElement);
				}
			}
			sheetElement.addContent(rowElement);
		} while ((line = reader.readLine()) != null);
		return document;
	}
	
	private void initializeHeader(String firstLine, String separador){
		String [] split = firstLine.split(separador);
		headers = new String[split.length];
		for (int i = 0 ; i < headers.length ; i ++){
			String content=split[i].trim();
			String cc="";
			int ini=content.length();
			for(int p=0;p<content.length();p++){
				cc=content.substring(p,p+1);
				if(cc.matches("\\w")){
					ini=p;
					break;
				}
			}
			int end=content.indexOf(",");
			if(end>0){
				content=content.substring(0,end);
			}
			
			if(content.substring(ini).matches("\\d+\\,\\d+")){
				headers[i] = content.substring(ini).replaceAll("\\,","\\."); 
			}else{
				headers[i] = content.substring(ini);
			}
		}
	}

}
