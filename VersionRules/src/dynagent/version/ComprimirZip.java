package dynagent.version;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ComprimirZip {
	
	public static final int BUFFER = 2048;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1){
			System.err.println("[Compressor] Debe indicarse la ruta del fichero  comprimir");
		}
		
		File sourceFile = new File(args[0]);
		String fileName = (sourceFile.getName().split("\\."))[0];
		File destinationFile = new File(sourceFile.getParentFile(), fileName + ".zip");
		FileOutputStream fos;
		ObjectOutputStream objOutStr;
		try {
			fos = new FileOutputStream(destinationFile);
			objOutStr = new ObjectOutputStream(fos);
			ZipOutputStream out = new ZipOutputStream(objOutStr);
			
			byte data[] = new byte[BUFFER];
			
			ZipEntry entry = new ZipEntry(sourceFile.getName());
			out.putNextEntry(entry);
			FileInputStream fi = new FileInputStream(sourceFile);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			int count;
			while((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
