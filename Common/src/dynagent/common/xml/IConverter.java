package dynagent.common.xml;

import java.io.FileInputStream;
import java.io.IOException;

import org.jdom.Document;

public interface IConverter {
	
	public static final String TAG_WORKBOOK = "workbook";
	public static final String TAG_SHEET = "sheet";
	public static final String TAG_ROW = "row";
	
	/**
	 * Convierte el fichero dado en un documento XML
	 * 
	 * @param in
	 *            Lector del fichero
	 * @return Documento XML construido
	 * @throws IOException
	 *             Si se produce algún error durante el procesamiento.
	 */
	public Document convert(FileInputStream in) throws IOException;
	public Document convert(FileInputStream in,String[] header) throws IOException;

}
