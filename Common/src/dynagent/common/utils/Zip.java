package dynagent.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Esta clase ofrece funcionalidad para descomprimir un fichero a la ruta
 * indicada.
 * 
 */
public abstract class Zip {

	public static final int BUFFER = 2048;

	public static boolean unZip(String zipPath, String destPath) {
		return unZip(new File(zipPath), new File(destPath));
	}

	@SuppressWarnings("unchecked")
	public static boolean unZip(File zipSourceFile, File destDirectory) {
		if (!zipSourceFile.exists() || !zipSourceFile.isFile()) {
			return false;
		}

		if (!destDirectory.exists()) {
			destDirectory.mkdirs();
		}

		if (!destDirectory.isDirectory()) {
			return false;
		}

		try {
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			int count;
			byte data[] = new byte[BUFFER];
			ZipFile zipfile = new ZipFile(zipSourceFile);
			Enumeration e = zipfile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if (entry.isDirectory()) {
					File outputPath = new File(destDirectory, entry.getName());
					outputPath.mkdir();
				} else {
					
					//Añadido porque si el archivo esta ofuscado no estan los entry de los directorios, solo los de los archivos, y tenemos que crear antes el directorio
					int end=entry.getName().lastIndexOf("/");
					if(end==-1)
						end=entry.getName().lastIndexOf("\\");
					if(end!=-1){
						File directory = new File(destDirectory, entry.getName().substring(0,end));
						if(!directory.exists()){
							directory.mkdirs();
						}
					}
					
					is = new BufferedInputStream(zipfile.getInputStream(entry));
					File outputPath = new File(destDirectory, entry.getName());
					FileOutputStream fos = new FileOutputStream(outputPath);
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					is.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean zip(String sourceDirectory, String destination){
		return zip(new File(sourceDirectory), new File(destination));
	}

	public static boolean zip(File sourceDirectoryFile, File destination) {
		if (! sourceDirectoryFile.isDirectory()){
			return false;
		}
		
		if (destination.exists() && destination.isFile()){
			destination.delete();
		}
		
		if (destination.isDirectory()){
			destination = new File(destination, sourceDirectoryFile.getName() + ".zip");
		}
		
		return zipDirectory(sourceDirectoryFile, sourceDirectoryFile.list(), destination);
	}
	
	public static boolean zip(ArrayList<String> sourceFileNames, String destination){
		return zip(sourceFileNames, new File(destination));
	}
	
	public static boolean zip(ArrayList<String> sourceFileNames, File destination) {
		if (destination.exists() && destination.isFile()){
			destination.delete();
		}
		
		if (destination.exists() && destination.isFile()){
			destination.delete();
		}
		
		if (destination.isDirectory()){
			destination = new File(destination,"file.zip");
		}
		
		String[] sourceFileNamesArray = new String[sourceFileNames.size()];
		return zipDirectory(null, (String[]) sourceFileNames.toArray(sourceFileNamesArray), destination);
	}
	
	private static boolean zipDirectory(File directory, String[] files, File destinationZip){
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(destinationZip);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];

			for (int i=0; i<files.length; i++) {
				File inputFile = null;
				if(directory!=null){
					inputFile=new File(directory, files[i]);
				}else{
					inputFile=new File(files[i]);
				}
				if (inputFile.getAbsolutePath().equals(destinationZip.getAbsolutePath())){
					continue;
				}
				ZipEntry entry = new ZipEntry(files[i]);
				
				
				if (inputFile.isDirectory()){
					entry = new ZipEntry(files[i] + "/");
					out.putNextEntry(entry);
					boolean result = zipSubDirectory(inputFile, inputFile.getName() + "/", out);
					if (! result){
						return result;
					}
				}else{
					out.putNextEntry(entry);
					FileInputStream fi = new FileInputStream(inputFile);
					origin = new BufferedInputStream(fi, BUFFER);
					int count;
					while((count = origin.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					origin.close();
				}
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static boolean zipSubDirectory(File sourceDirectory, String internalZipPath, ZipOutputStream out){
		try {
			BufferedInputStream origin = null;
			byte data[] = new byte[BUFFER];
			String files[] = sourceDirectory.list();

			for (int i=0; i<files.length; i++) {
				ZipEntry entry = new ZipEntry(internalZipPath + files[i]);
				File inputFile = new File(sourceDirectory, files[i]);
				
				if (inputFile.isDirectory()){
					entry = new ZipEntry(internalZipPath + files[i] + "/");
					out.putNextEntry(entry);
					boolean result = zipSubDirectory(inputFile, internalZipPath + inputFile.getName() + "/", out);
					if (! result){
						return result;
					}
				}else{
					out.putNextEntry(entry);
					FileInputStream fi = new FileInputStream(inputFile);
					origin = new BufferedInputStream(fi, BUFFER);
					int count;
					while((count = origin.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					origin.close();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
