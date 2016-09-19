package dynagent.common.xml;

import java.io.File;
import java.io.FileInputStream;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import dynagent.common.exceptions.FileException;


public class DocumentFactory {
	
	public static Document createDocument(String filePath,String[] header) throws FileException{
		File file = new File(filePath);
		if (! file.isFile()){
			throw new FileException("La ruta " + filePath + " no se corresponde con un fichero");
		}
		
		Document result = null;
		String fileName = file.getName();
		try {
			if (fileName.endsWith(".xls")){
				System.out.println("Extension xls");
				IConverter converter = new XLSToXMLConverter();
				result = converter.convert(new FileInputStream(file),header);						
			}else if(fileName.endsWith(".txt")||fileName.endsWith(".csv")){
				System.out.println("Extension TEXTO");
				IConverter converter = new TXTToXMLConverter();
				result = converter.convert(new FileInputStream(file));
			}else if(fileName.endsWith(".xml")){
				SAXBuilder builder = new SAXBuilder();
				result = builder.build(file);
			}else{
				int dotIndex = fileName.lastIndexOf('.');
				String extension = fileName.substring(dotIndex);
				throw new FileException("No se tiene un procedimiento establecido para obtener un Documento a partir de un fichero de extensión:" + extension);
			}
		} catch (Exception e) {
			System.out.println("Error al importar fichero "+filePath+" existe:"+file.exists());
			e.printStackTrace();
			throw new FileException(e.getMessage());
		}
		
		return result;
	}
}
