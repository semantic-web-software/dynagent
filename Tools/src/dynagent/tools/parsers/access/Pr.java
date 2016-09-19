

package dynagent.tools.parsers.access;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import dynagent.server.database.dao.AccessDAO;
import dynagent.tools.importers.access.AccessImport;
import dynagent.tools.parsers.uni.Analex;
import dynagent.tools.parsers.uni.Anasint;
import dynagent.common.basicobjects.Access;
import dynagent.common.utils.Auxiliar;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;




public class Pr {
	
	public static void main(String args[]){
		
		ArrayList<Acceso> accesos = new ArrayList<Acceso>();
		ArrayList<AccessString> accesosString = new ArrayList<AccessString>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(args[0]);
			Analex analex = new Analex(fis);
			Anasint anasint = new Anasint(analex);
			AST arbol = null;
			accesos=anasint.declaraciones();
			arbol = anasint.getAST();
			ASTFrame frame = new ASTFrame(args[0], arbol);
			frame.setVisible(true);
			
			
			
			Iterator itaccesos = accesos.iterator();
			while(itaccesos.hasNext()){
				Acceso acc = (Acceso) itaccesos.next();
				if(acc.isWellDefined()){
					accesosString.addAll(acc.translatetoAccessStringFromAcceso());
				}
			}
			
			System.out.println(accesosString);
			AccessImport ai = new AccessImport("192.168.1.3", 12);
			
			
			
			ArrayList<Access> accesses = ai.run(accesosString);
			
			System.out.println(accesses);
			
			String resp=Auxiliar.leeTexto("Desea insertar en la base de datos?");
			if(resp.equals("S")){
				AccessDAO adao = new AccessDAO();
				adao.open();
				Iterator<Access> it = accesses.iterator();
				adao.insertAccessIterator(it);
				adao.close();
			}
			System.out.println("*****ByE*****");
			
			
			
		} catch (FileNotFoundException e) {
			System.out.println("No se encontró el fichero "+args[0]);
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TokenStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
