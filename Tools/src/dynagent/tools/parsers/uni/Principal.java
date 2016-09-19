
		
	package dynagent.tools.parsers.uni;
///////////////////////////////
//	 Principal.java (clase principal)
//////////////////////////////	/
	
import java.io.*;
import java.sql.SQLException;
	import java.util.ArrayList;
	import java.util.Iterator;

import javax.naming.NamingException;

	import antlr.collections.*;
	import antlr.debug.misc.*;
	import antlr.*;
import dynagent.common.Constants;
	import dynagent.ruleengine.*;
	import dynagent.ruleengine.src.data.dao.*;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.tools.parsers.uni.auxiliar.Clase;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoBD;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoFichero;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoFicheroAdvanced;
import dynagent.tools.parsers.uni.auxiliar.Propiedad;
import dynagent.tools.parsers.uni.auxiliar.PropiedadBD;
import dynagent.tools.parsers.uni.auxiliar.RolAtrib;
import dynagent.tools.parsers.uni.auxiliar.SubClase;
import dynagent.tools.parsers.uni.auxiliar.SubclaseDB;

	public class Principal {
		public static void main(String args[]){
			try {
				DAOManager.getInstance().setBusiness("3");
				FileInputStream fis = new FileInputStream(args[0]);
				Analex analex = new Analex(fis);
				Anasint anasint = new Anasint(analex);
				Anasem anasem = new Anasem();
				ProcesamientoFichero pf = new ProcesamientoFichero();
				ProcesamientoBD pbd;
				pbd = new ProcesamientoBD();					
				AST arbol = null;
				ArrayList clasesBD = pbd.tablaClasesBD;
				ArrayList listaNombresBD = new ArrayList();
				ArrayList subClasesAInsertar = new ArrayList();
				
				
				/*Creamos una lista con los nombres de las clases en la base de datos
				 * para guiarnos y ayudarnos en el resto del programa
				 */
				
				Iterator itClasesBD = clasesBD.iterator();
				while(itClasesBD.hasNext()){
					Clase clbd = (Clase) itClasesBD.next();
					String cadena = clbd.getName();
					listaNombresBD.add(cadena);
				}
				
				
				/*Aqui debemos hacer una comprobacion más detallada, mirando que clases de 
				 * la BD heredan de relation, cuales de rol, etc
				 */
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("**********   Elementos repetidos en el fichero   **********");
				System.out.println("***********************************************************");
				System.out.println();
				
				pf = anasint.lenguaje();
				pf.compruebaRepeticiones(pf.listaClase);
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("****Properties usadas sin haber sido declarado su tipo*****");
				System.out.println("***********************************************************");
				System.out.println("");
				
				
				//TODO Distinguir entre roles, relaciones y clases de las entradas de la BD
				
				ArrayList clasesQueNoSeInsertaran = new ArrayList();
				ArrayList nuevaLista = pf.unionListas(pf.listaRelacion, pf.listaClase);
				ArrayList nuevaLista2 = pf.unionListas(nuevaLista, listaNombresBD);
				ArrayList nuevaLista3 = pf.unionListas(listaNombresBD, pf.listaRol);
				pf.compruebaListas(pf.listaClasesPorComprobar, nuevaLista2, 1, "La clase ");
				pf.compruebaListas(pf.listaRelacionesPorComprobar, nuevaLista2, 1, "La relacion ");
				ArrayList relacionesIncoherentes = pf.compruebaListas(pf.listaRolesPorComprobar, nuevaLista3, 1, "El rol ");
				//System.out.println("Relaciones Incoherentes: "+relacionesIncoherentes);
				pf.compruebaListas(pf.listaRol, pf.listaRolesPorComprobar, 2, "El rol ");
				pf.compruebaListas(pf.listaClase, pf.listaClasesPorComprobar, 2, "La clase ");
				arbol = anasint.getAST();
				ASTFrame frame = new ASTFrame(args[0], arbol);
				frame.setVisible(true);
				ProcesamientoFicheroAdvanced pfa = new ProcesamientoFicheroAdvanced();
				
			
				
				
				
				pfa = anasem.entrada(arbol);
				ArrayList listaRolAtrib = pfa.listaRolAtrib;
				Iterator itlra = listaRolAtrib.iterator();
				while(itlra.hasNext()){
					RolAtrib ra = (RolAtrib) itlra.next();
					if(relacionesIncoherentes.contains(ra.getNombreRol()))
						clasesQueNoSeInsertaran.add(ra.getRelacionCont());
					
					
				}
				
				
				int idto=-1;
				String nombre;
				
				ArrayList clasesFich = pf.listaClase;
				ArrayList clasesAInsertarBD = new ArrayList();
				
				/* Vamos a comprobar si existe alguna clase, rol o relacion
				 * cuyo nombre ya esta declarado en la base de datos
				 * Si estuviese, informamos de ello y lo eliminamos de la lista
				 * para no procesarlo
				 */
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("************** Clases ya existentes en la BD **************");
				System.out.println("***********************************************************");
				System.out.println("");
				
				
				clasesFich.addAll(pf.listaRelacion);
				clasesFich.addAll(pf.listaRol);
				//System.out.println("1: "+clasesFich);
				Iterator it = clasesBD.iterator();
				while(it.hasNext()){
					Clase cl = (Clase) it.next();
					nombre = cl.getName();
					if(clasesFich.contains(nombre)){
						clasesQueNoSeInsertaran.add(nombre);
						clasesFich.remove(nombre);
						System.out.println("ERROR: La clase "+nombre+" ya existia en la BD. La clase no se insertará");
					}
				
						
				}
				
				
				/*Vamos a asignarle a las nuevas clases un id de clase
				 * para insertarlas en la BD.
				 * En primer lugar, localizamos la ultima clave usada
				 * y a partir de ahi empezamos a asignar
				 */
				
				
				TClaseDAO tc = new TClaseDAO();
				String cad = tc.getLastPK("10000", "12000");
				Integer i = Integer.parseInt(cad);
				int nextPk = i.intValue() + 1;
				
				
				
				/* Vamos a crear una lista nueva de propiedades que contenga el nombre y el tipo
				 * de propiedad de las que se han creado en el fichero y las que ya habia en la BD
				 */
				

				ArrayList propiedadesFich =pfa.listaPropiedad;
				Iterator itProp = propiedadesFich.iterator();
				ArrayList tablaPropertiesBD = pbd.tablaPropertiesBD;
				ArrayList propsAEliminar = new ArrayList();
				
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("************ Properties ya declaradas en la BD ************");
				System.out.println("***********************************************************");
				System.out.println("");
				
				while(itProp.hasNext()){
					Propiedad prop = (Propiedad) itProp.next();
					String nameProp = prop.getNombreProp();
					Iterator itPropBD = tablaPropertiesBD.iterator();
					boolean enc = false;
					while(itPropBD.hasNext() && !enc){
						PropiedadBD cl2 = (PropiedadBD) itPropBD.next();
						if(nameProp.equals(cl2.getName())){
							System.out.println("La propiedad "+nameProp+" de la clase "+prop.getClaseCont()+" no hay que insertarla de nuevo, ya existe en la BD. Verifique que el tipo de la propiedad es el que usted desea, si no fuera así, renómbrela." );
							//Eliminamos las propiedades que no tenemos que añadir
							//propiedadesFich.remove(prop);
							enc = true;
							
						}
					}
					
					if(enc)
						propsAEliminar.add(prop);
					
				}
				
				//Nos quedamos con las unicas que vamos a añadir
				
				
				propiedadesFich.removeAll(propsAEliminar);
				
				
				/* Vamos a comprobar que si existen 2 propiedades con el mismo
				 * nombre, estan son del mismo tipo. Si no lo fueran no se
				 * insertaria ninguna de estas clases
				 */
				
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("********** Properties que no se van a insertar ************");
				System.out.println("***********************************************************");
				System.out.println("");
				
				Iterator itProp2 = propiedadesFich.iterator();
				while(itProp2.hasNext()){
					Propiedad prop = (Propiedad) itProp2.next();
					String pName = prop.getNombreProp();
					String pTipo = prop.getTipoPropiedad();
					Iterator itProp3 = propiedadesFich.iterator();
					while(itProp3.hasNext()){
						Propiedad prop2 = (Propiedad) itProp3.next();
						if(pName.equals(prop2.getNombreProp()) && !pTipo.equals(prop2.getTipoPropiedad())){
							System.out.println("ERROR: La propiedad "+pName+" de la clase "+prop.getClaseCont() +" tiene elmismo nombre y distinto tipo que otra(s)");
							//Borro la 2º...(De momento no es necesario)
							//propiedadesFich.remove(prop2);
							clasesQueNoSeInsertaran.add(prop2.getClaseCont());
							
						}
					}
				}
				
				/*Ahora ya podemos decir que sabemos que clases se pueden insertar. 
				 * Por tanto las insertamos
				 */
				
				
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("********* Verficiación resto de clases del fichero** ******");
				System.out.println("***********************************************************");
				System.out.println("");
				
				
				Iterator itclasesFich = clasesFich.iterator();
				while(itclasesFich.hasNext()){
					String ncl = (String) itclasesFich.next();
					if(!clasesQueNoSeInsertaran.contains(ncl)){
						Clase  cl3 = new Clase();
						cl3.setIdto(nextPk++);
						cl3.setName(ncl);
						clasesAInsertarBD.add(cl3);
						System.out.println("La clase "+ncl+" se insertará en la BD puesto que cumple todos los requisitos");
					}
					else
						System.out.println("La clase "+ ncl + " no va a ser insertada puesto que alguna de sus properties es incorrecta. Compruebe si alguna clase insertada usa una property de este tipo puesto que deberá tener que unsertarla para el correcto funcionamiento del sistema " );
					//else clasesFich.remove(ncl);
				}
		
				
				//System.out.println("345: "+clasesQueNoSeInsertaran);
				
				/*Ahora vamos a comprobar la herencia de 
				 * cada clase y creamos una lista con las futuras clases a insertar, ya con su idto y el de su pàdre				 * 
				 */
				
				ArrayList subClasesFich = pfa.listaSubclase;
				
				
				
				ArrayList totalClasesSistema = new ArrayList();
				totalClasesSistema.addAll(clasesBD);
				totalClasesSistema.addAll(clasesAInsertarBD);
				
				Iterator it2 = subClasesFich.iterator();
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("****************** Insercion en tabla herencia ************");
				System.out.println("***********************************************************");
				System.out.println("");
				
				
				//System.out.println(subClasesFich);
				
				while(it2.hasNext()){
					SubClase sc = (SubClase) it2.next();
					String  nsc = (String) sc.getName();
					if(!clasesQueNoSeInsertaran.contains(nsc)){
						ArrayList padres = sc.getListaPadres();
						Iterator itPadres = padres.iterator();
						while(itPadres.hasNext()){
							int idtoPadre = -2;
							String sPadre = (String) itPadres.next();
							Iterator itBD = totalClasesSistema.iterator();
							boolean enc = false;
							boolean enc2 = false;
							Clase c=null;
							SubclaseDB scbd = new SubclaseDB();
							while (itBD.hasNext() && (!enc || !enc2)){
								c = (Clase) itBD.next();
								if(sPadre.equals(c.getName())){
								enc = true;
								System.out.println("La clase "+sc.getName()+" tiene como padre a "+sPadre+ " la cual tiene una idto de "+c.getIdto());
								scbd.setIdtoPadre(c.getIdto());
								scbd.setName(nsc);
								scbd.setPadre(sPadre);
								
								//System.out.println(idtoPadre);
							}
								if(nsc.equals(c.getName())){
									enc2 = true;
									scbd.setIdto(c.getIdto());
							}
							
						}
						subClasesAInsertar.add(scbd);
						
						/*if(!enc){
							SubclaseDB scbd = new SubclaseDB();
							scbd.setPadre(sPadre);
							scbd.setIdtoPadre(-2);
							scbd.setName(nsc);
							subClasesAInsertar.add(scbd);
						}*/
						/*scbd.setIdtoPadre(idtoPadre);
						subClasesAInsertar.add(scbd);*/
						}
					}
				}
				
				Iterator itmsc = clasesAInsertarBD.iterator();
				while(itmsc.hasNext()){
					Clase c1 = (Clase) itmsc.next();
					SubclaseDB scbd = new SubclaseDB();
					scbd.setIdto(c1.getIdto());
					scbd.setPadre(Constants.CLS_THING);
					scbd.setName(c1.getName());
					scbd.setIdtoPadre(0);
					subClasesAInsertar.add(scbd);
					System.out.println("La clase "+scbd.getName()+" tiene como padre a "+scbd.getPadre()+ " la cual tiene una idto de "+scbd.getIdtoPadre());
				}
				
				
				
				//System.out.println(propsAEliminar);
				
			/*	Iterator itPrueba = subClasesAInsertar.iterator();
				while(itPrueba.hasNext()){
					SubclaseDB prueba = (SubclaseDB) itPrueba.next();
					System.out.println("idto = "+ prueba.getIdto() +", idto padre = "+ prueba.getIdtoPadre()+" nombre = "+prueba.getName()+" padre = "+prueba.getPadre());
				}
				*/
				
				/*Tenemos que insertar en la tabla clases las clases que han pasado el corte
				 * Podemos hacerlo ahora o comprobar primero las propiedades, para acceder
				 * lo menos posible a la base de datos. Por tanto, primero tomaremos las propiedades
				 * viendo cuales de ellas se han declarado sobre clases duplicadas y asi evitar añadirlas
				 * 
				 */
				
				
				
				
				
				
				/*Vamos a comprobar si una propiedad es de un tipo que esta en la BD
				 * o de un tipo del fichero
				 */ 
				
				/*while(itProp.hasNext()){
					Propiedad p = (Propiedad) itProp.next();
					String nameProp = p.getNombreProp();
					String tipoProp = p.getTipoPropiedad();
					if(listaNombresBD.contains(tipoProp)){
						System.out.println("La propiedad es de un tipo de la BD");
					}
					else if(clasesFich.contains(tipoProp)){
						System.out.println("La propiedad es de un tipo del fichero");
					}
				}*/
				
				
				/* Por ultimo, decidiremos que propiedades vamos a insertar definitivamente
				 * eliminando aquellas propiedades cuyas clases no han sido insertadas
				 */
				
				
				System.out.println("");
				System.out.println("***********************************************************");
				System.out.println("*********** Comprobacion coherente de propiedades *********");
				System.out.println("***********************************************************");
				System.out.println("");
				
				
				
				PropertiesDAO pdao = new PropertiesDAO();
				String spk = pdao.getLastPK();
				Integer inp = Integer.parseInt(spk);
				int propPk = inp.intValue() + 1;
				
				ArrayList propsABD = new ArrayList();
				ArrayList propsAInsertarEnBD = new ArrayList();
				
				Iterator itpf = propiedadesFich.iterator();
				while(itpf.hasNext()){
					Propiedad pr = (Propiedad) itpf.next();
					String nameC = pr.getClaseCont();
					if(clasesQueNoSeInsertaran.contains(nameC)){
						System.out.println("La propiedad "+pr.getNombreProp()+" de la clase "+nameC+" no puede insertarse puesto que dicha clase no será insertada");
						
					}
					else if(!clasesQueNoSeInsertaran.contains(pr.getTipoPropiedad())){
						propsABD.add(pr);
						PropiedadBD prbd = new PropiedadBD();
						prbd.setIdProp(propPk++);
						prbd.setName(pr.getNombreProp());
						propsAInsertarEnBD.add(prbd);
						System.out.println("La propiedad "+pr.getNombreProp()+" de la clase "+nameC+"  puede insertarse puesto que dicha clase será insertada");
					}
					else{
						PropiedadBD prbd = new PropiedadBD();
						prbd.setIdProp(propPk++);
						prbd.setName(pr.getNombreProp());
						propsAInsertarEnBD.add(prbd);
						System.out.println("La propiedad "+pr.getNombreProp()+" de la clase "+nameC+" esta usando una property de tipo "+pr.getTipoPropiedad() +", el cual no ha podido insertarse en la BD porque contenia errores. Deberá insertar esta clase para el correcto funcionamiento del sistema");
						clasesQueNoSeInsertaran.add(pr.getTipoPropiedad());
					}
						
				}
				
				
				/* Ya tenemos todas las listas correctamente rellenas, solo nos quedaria insertar en las tablas clases, properties
				 * y herencias los nuevos registros
				 */
				
				System.out.println(clasesAInsertarBD);
				System.out.println(subClasesAInsertar);
				System.out.println(propsAInsertarEnBD);
				
				//TODO Insertar los registros en la bd
				
				/*TPropertiesAdvanced tca = new TPropertiesAdvanced();
				tca.insertNewProperty(5000, 2, "Probandooo");
				tca.close();*/
				
			} catch (ANTLRException ae) {
				System.err.println(ae.getMessage());
			} catch (FileNotFoundException fnfe) {
				System.err.println("No se encontró el fichero");
			} catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}

