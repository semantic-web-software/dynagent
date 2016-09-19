package dynagent.version;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

public class DpkgToGzip {

	private static int i;
	/**
	 * Se considera que cada argumento de args es un fichero que hay que transformar de dkpg a gzip. Si la ruta al
	 * fichero contiene espacios, debe ir entre comillas.
	 * 
	 * @param args
	 *            Rutas a los ficheros.
	 */
	public static void main(String[] args) {
		String ruta;
		i = 0;
		while ((ruta = getPath(args)) != null){
			File sourceFile = new File(ruta);
			
			createTemporalFile(sourceFile);
			compressFile(sourceFile);
		}
	}
	
	private static void compressFile(File sourceFile) {
		File parentFile = sourceFile.getParentFile();
		String fileName = sourceFile.getName().replace(".dpkg", ".tmp");
		File temporalFile = new File(parentFile, fileName);
		
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(temporalFile));
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(sourceFile));
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bufferedOutputStream);
			
			byte [] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = bufferedInputStream.read(buffer)) != -1){
				gzipOutputStream.write(buffer, 0, bytesRead);
			}
			bufferedInputStream.close();
			gzipOutputStream.close();
			temporalFile.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void createTemporalFile(File sourceFile) {
		File parentFile = sourceFile.getParentFile();
		String fileName = sourceFile.getName().replace(".dpkg", ".tmp");
		File temporalFile = new File(parentFile, fileName);
		
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(temporalFile));
			
			byte [] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = bufferedInputStream.read(buffer)) != -1){
				bufferedOutputStream.write(buffer, 0, bytesRead);
			}
			bufferedInputStream.close();
			bufferedOutputStream.close();
			sourceFile.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private static String getPath(String[] args){
		String path = "";
		String space = "";
		boolean exit = false;
		for (; i < args.length && ! exit ; i ++){
			path += space + args[i];
			if (path.endsWith(".dpkg")){
				exit = true;
			}
			space = " ";
		}
		if (path.isEmpty() || ! path.endsWith(".dpkg")){
			path = null;
		}
		return path;
	}
}
