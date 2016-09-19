package dynagent.tools.parsers.uni;



///////////////////////////////
//Principal.java (clase principal)
///////////////////////////////

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import antlr.ANTLRException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;
import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.knowledge.Category;
import dynagent.common.utils.Auxiliar;
import dynagent.server.database.dao.AccessDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.tools.parsers.uni.auxiliar.AccessTreatment;
import dynagent.tools.parsers.uni.auxiliar.Clase;
import dynagent.tools.parsers.uni.auxiliar.Individuo;
import dynagent.tools.parsers.uni.auxiliar.InsercionInstance;
import dynagent.tools.parsers.uni.auxiliar.KeysManager;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoBD;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoFichero;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoFicheroAdvanced;
import dynagent.tools.parsers.uni.auxiliar.PropiedadAtrib;
import dynagent.tools.parsers.uni.auxiliar.PropiedadBD;
import dynagent.tools.parsers.uni.auxiliar.PropiedadClase;
import dynagent.tools.parsers.uni.auxiliar.Rol;
import dynagent.tools.parsers.uni.auxiliar.RolAtrib;
import dynagent.tools.parsers.uni.auxiliar.RolPointer;
import dynagent.tools.parsers.uni.auxiliar.SubClase;
import dynagent.tools.parsers.uni.auxiliar.SubclaseDB;
import antlr.collections.*;
import antlr.debug.misc.*;
import antlr.*;


public class Principal2 {
	public static void main(String args[]) {
		try {
			
			
			/*
			 * Seleccionamos la BD que queremos usar
			 */
			
			int business = 8;
			DAOManager.getInstance().setBusiness(new Integer(business).toString());
			FactoryConnectionDB fcdb = null;
			fcdb = new FactoryConnectionDB(business,true,"192.168.1.3","SQLServer");
			DAOManager.getInstance().setFactConnDB(fcdb);
			DAOManager.getInstance().setCommit(true);


			
			/*
			 * Cargamos todos los datos necesarios y creamos ua lista
			 * indicando los nombres de las clases que no se van a insertar
			 */ 
			
			
			ArrayList<String> listaClasesQueNoSeInsertaran = new ArrayList();
			ProcesamientoFichero pf = new ProcesamientoFichero();
			ProcesamientoFicheroAdvanced pfa = new ProcesamientoFicheroAdvanced();
			ProcesamientoBD pbd = new ProcesamientoBD();
			InsercionInstance ii = new InsercionInstance();
		
			
			
			/* Creacion y tratamiento del arbol.
			 * Obtencion de los parametros necesarios
			 */
			
			FileInputStream fis = new FileInputStream(args[0]);
			Analex analex = new Analex(fis);
			Anasint anasint = new Anasint(analex);
			Anasem anasem = new Anasem();
			AST arbol = null;
			pf = anasint.lenguaje();
			arbol = anasint.getAST();
			ASTFrame frame = new ASTFrame(args[0], arbol);
			frame.setVisible(true);
			pfa = anasem.entrada(arbol);
		
			
			
			//Obtenemos una lista con los nombres de las clases
			
			ArrayList listaNombresBD = listaNombres(pbd.tablaClasesBD);
			
			/*
			 * Ahora comprobaremos si existen clases repetidas en el fichero.
			 * Para ello creamos una lista con todos los nombres de roles, 
			 * relaciones y clases y comprobamos que no haya repeticiones.
			 */ 
			
			ArrayList listaCRR = new ArrayList(); 
			//Nota: CRR = Clases + Relaciones + Roles
			listaCRR.addAll(pf.listaClase);
			listaCRR.addAll(pf.listaRol);
			listaCRR.addAll(pf.listaRelacion);
			listaCRR.addAll(pf.listaPropiedad);
			listaCRR.addAll(pf.listaIndividuos);
		
			
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("**********   Elementos repetidos en el fichero   **********");
			System.out.println("***********************************************************");
			System.out.println();
			
			listaClasesQueNoSeInsertaran.addAll(pf.compruebaRepeticiones(listaCRR));
			
			
			/* 
			 * Ahora comprobaremos si alguna propiedad en alguna clase ha redefinido
			 * su rango de manera incoherente. Si esto ocurriera, no permitiriamos
			 * insertar dicha clase en la BD
			 */
			
			
			
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("***************   Rangos mal redefinidos   ****************");
			System.out.println("***********************************************************");
			System.out.println();
			
			
			listaClasesQueNoSeInsertaran.addAll(compruebaRestriccionesCorrectas(pfa.listaPropCl, pfa.listaPropAt));
			listaClasesQueNoSeInsertaran.addAll(compruebaEnumeradosCorrectos(pfa.listaPropCl, pfa.listaPropAt, pfa.listaIndividuos));
			
			/*
			 * Ahora comprobaremos que las clases que juegan un rol estan definidas
			 * y son clases propiamente dichas, no otros roles ni relaciones
			 */
			
			ArrayList clasesEnTotal = new ArrayList(listaNombresBD);
			clasesEnTotal.addAll(pf.listaClase);
			
			listaClasesQueNoSeInsertaran.addAll(compruebaClasesJuego(pfa.listaRol, clasesEnTotal));
			
			
			
			
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("***************   Individuos inexistentes  ****************");
			System.out.println("***********************************************************");
			System.out.println();
			
			Iterator itInd = pfa.listaPropCl.iterator();
			ArrayList individuos = pfa.listaIndividuos;
			ArrayList propiedadesMalDefinidas = new ArrayList();
			
			while(itInd.hasNext()){
				PropiedadClase pc = (PropiedadClase) itInd.next();
				if(pc.getEnumerados().size()>0){
					Iterator itEnum = pc.getEnumerados().iterator();
					boolean enc2 = false;
					while(itEnum.hasNext() && !enc2){
						String enumr = (String) itEnum.next();
						boolean enc = false;
						Iterator itInd2 = individuos.iterator();
						while(itInd2.hasNext() && !enc){
							Individuo ind = (Individuo) itInd2.next();
							if(enumr.equals(ind.getName()))
								enc = true;
						}
						
						if(!enc){
							if(!listaNombresBD.contains(enumr)){
								enc2 = true;
								propiedadesMalDefinidas.add(pc.getNombreProp());
								System.out.println("El enumerado "+enumr+" no se ha creado. La propiedad "+pc.getNombreProp()+" no se creará");
						
							}
						}
					}
					
					
					
				}
			}
			
			
			
			Iterator itElimCl = pfa.listaPropAt.iterator();
			while(itElimCl.hasNext()){
				PropiedadAtrib pa = (PropiedadAtrib) itElimCl.next();
				if(propiedadesMalDefinidas.contains(pa.getNombreProp()))
					listaClasesQueNoSeInsertaran.add(pa.getClaseCont());
			}
			
			/*
			 * Comprobamos si existe alguna relacion que se usa en un rol pointer
			 * sin haber sido creada. Ademas informamos de roles o clases
			 * no usadas en el modelo, simplemente declaradas
			 */
				
						
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("****  Relaciones usadas en rol pointer no declaradas  *****");
			System.out.println("***********************************************************");
			System.out.println("");
			
			pf.compruebaListas(pf.listaRelacionesPorComprobar, pf.listaRelacion, 1, " La relacion ");
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("******  Clases o roles no usados, solo declarados  ********");
			System.out.println("***********************************************************");
			System.out.println("");
			
			pf.compruebaListas(pf.listaRol, pf.listaRolesPorComprobar, 2, "El rol ");
			pf.compruebaListas(pf.listaClase, pf.listaClasesPorComprobar, 2, "La clase ");
			
								
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("****  Individuos declarados usando clases inexistentes ****");
			System.out.println("***********************************************************");
			System.out.println("");
			
			ArrayList lAux2 = new ArrayList(pf.listaClase);
			lAux2.addAll(listaNombresBD);
			lAux2.addAll(pf.listaRelacion);
			lAux2.addAll(pf.listaRol);
			pf.compruebaListas(pf.listaClasesPorComprobar, lAux2, 1, "La clase ");
			
			
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("*******  Roles no declarados usados en relaciones *********");
			System.out.println("***********************************************************");
			System.out.println("");
			
			ArrayList nuevaLista = pf.unionListas(pf.listaRelacion, pf.listaClase);
			ArrayList nuevaLista2 = pf.unionListas(nuevaLista, listaNombresBD);
			ArrayList nuevaLista3 = pf.unionListas(listaNombresBD, pf.listaRol);
			ArrayList relacionesIncoherentes = pf.compruebaListas(pf.listaRolesPorComprobar, nuevaLista3, 1, "El rol ");
			ArrayList listaRolAtrib = pfa.listaRolAtrib;
			Iterator itlra = listaRolAtrib.iterator();
			
			while(itlra.hasNext()){
				RolAtrib ra = (RolAtrib) itlra.next();
				if(relacionesIncoherentes.contains(ra.getNombreRol()))
					listaClasesQueNoSeInsertaran.add(ra.getRelacionCont());
			}
			
			
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
			
			ArrayList clasesFich = new ArrayList(pf.listaClase);
			clasesFich.addAll(pf.listaRelacion);
			clasesFich.addAll(pf.listaRol);
			
			Iterator itNombresBD =listaNombresBD.iterator();
			while(itNombresBD.hasNext()){
				String nombre = (String) itNombresBD.next();
				if(clasesFich.contains(nombre)){
					listaClasesQueNoSeInsertaran.add(nombre);
					clasesFich.remove(nombre);
					if(!Constants.isBasicClass(nombre) &&  !Constants.isBasicType(nombre))
						System.out.println("ERROR: La clase "+nombre+" ya existia en la BD. La clase no se insertará");
				}
			
					
			}
			
			
			
			
			/*Vamos a asignarle a las nuevas clases un id de clase
			 * para insertarlas en la BD.
			 * En primer lugar, localizamos la ultima clave usada
			 * y a partir de ahi empezamos a asignar
			 */
			
			
			TClaseDAO tc = new TClaseDAO();
			tc.open();
			String cad = tc.getLastPK("6", new Integer(Constants.MAX_ID_CLASS).toString());
			Integer i = Integer.parseInt(cad);
			tc.close();
			int nextPk = i.intValue() + 1;
			
			

			/* Vamos a crear una lista nueva de propiedades que contenga el nombre y el tipo
			 * de propiedad de las que se han creado en el fichero y las que ya habia en la BD
			 */
			
			
			ArrayList propiedadesFich =pfa.listaPropCl;
			Iterator itProp = propiedadesFich.iterator();
			ArrayList tablaPropertiesBD = pbd.tablaPropertiesBD;
			ArrayList propsAEliminar = new ArrayList();
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("************ Properties ya declaradas en la BD ************");
			System.out.println("***********************************************************");
			System.out.println("");
			
			
			while(itProp.hasNext()){
				PropiedadClase prop = (PropiedadClase) itProp.next();
				String nameProp = prop.getNombreProp();
				Iterator itPropBD = tablaPropertiesBD.iterator();
				boolean enc = false;
				while(itPropBD.hasNext() && !enc){
					PropiedadBD cl2 = (PropiedadBD) itPropBD.next();
					if(nameProp.equals(cl2.getName())){
						System.out.println("La propiedad "+nameProp+" no hay que insertarla de nuevo, ya existe en la BD. Verifique que el rango de la propiedad es el que usted desea, si no fuera así, renómbrela." );
						enc = true;
						
					}
				}
				
				if(enc)
					propsAEliminar.add(prop);
				
			}
			
			
			propiedadesFich.removeAll(propsAEliminar);
			
			
			
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("************  Properties usadas no declaradas  ************");
			System.out.println("***********************************************************");
			System.out.println("");
			
			ArrayList listaProps = listaNombresPropBD(pbd.tablaPropertiesBD);
			//listaProps.addAll(listaNombresPropFich(pfa.listaPropCl));
			listaProps.addAll(pf.listaPropiedad);
			
			ArrayList<PropiedadAtrib> propiedadesFichAt = pfa.listaPropAt;
			ArrayList propiedadesFichAtAEliminar = new ArrayList();
			Iterator itprf = propiedadesFichAt.iterator();
			
			while(itprf.hasNext()){
				PropiedadAtrib pat = (PropiedadAtrib) itprf.next();
				String noombre = pat.getNombreProp();
				if(!listaProps.contains(noombre)){
					System.out.println("La propiedad "+noombre+" usada en la clase "+pat.getClaseCont()+" se ha usado sin que esta exista. La clase no se insertará");
					listaClasesQueNoSeInsertaran.add(pat.getClaseCont());
					propiedadesFichAtAEliminar.add(pat);
					}
			}
			
			Iterator itAuxPr = propiedadesFichAtAEliminar.iterator();
			while(itAuxPr.hasNext()) propiedadesFichAt.remove((PropiedadAtrib) itAuxPr.next());
			
			
			
			
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("************* Insercion propiedades heredadas ************");
			System.out.println("***********************************************************");
			System.out.println("");
			
			/*
			 * Vamos a contemplar la herencia de atributos, para cada clase miramos sus padres y
			 * si los atributos de ellas no estan los añadimos.
			 * 
			 */
			
			ArrayList<RolAtrib> rolesHerencia = new ArrayList<RolAtrib>();
			ArrayList<PropiedadAtrib> propsHerencia = new ArrayList<PropiedadAtrib>();
			
			
			if(pfa.listaSubclase.size()>0){
				addHierarchyPropsRec(pfa.listaSubclase, propsHerencia, rolesHerencia, pfa.listaPropAt, pfa.listaRolAtrib, pf.listaRelacion);
				
				/*while(itsubClases.hasNext()){
					SubClase sc = (SubClase) itsubClases.next();
					if(sc.getListaPadres().size()>1 && pf.listaRelacion.contains(sc.getName()))
						pfa.listaRolAtrib.addAll(addHierarchyRoles(pfa.listaRolAtrib, sc.getName(), (String) sc.getListaPadres().get(1)));
					Iterator itPadres = sc.getListaPadres().iterator();
					while(itPadres.hasNext()){
						String padre = (String) itPadres.next();
						if(!padre.equals(Constants.CLS_UNIDADES) || !padre.equals(Constants.CLS_UTASK))
							propsHerencia.addAll(addHierarchyProperties(pfa.listaPropAt, padre, sc.getName()));
					}
				}
				*/	
				propiedadesFichAt.addAll(propsHerencia);
				pfa.listaRolAtrib.addAll(rolesHerencia);
				propiedadesFichAt=eliminaPropsConsecutivas(eliminaPropsRepetidas(propiedadesFichAt));
				pfa.listaRolAtrib = eliminaRolesConsecutivos(eliminaRolesRepetidos(pfa.listaRolAtrib));
				System.out.println(propiedadesFichAt);
				System.out.println(pfa.listaRolAtrib);
			
			}
			/*Ahora ya podemos decir que sabemos que clases se pueden insertar. 
			 * Por tanto las insertamos
			 */
			
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("********* Verificación resto de clases del fichero** ******");
			System.out.println("***********************************************************");
			System.out.println("");
			
			ArrayList<Clase> clasesAInsertarBD = new ArrayList();
			HashMap keyClasses = KeysManager.buildKeyClasses(clasesFich);
			System.out.println(keyClasses);
			
			
			Iterator itclasesFich = clasesFich.iterator();
			while(itclasesFich.hasNext()){
				int idtoAinsertar;
				String name = (String) itclasesFich.next();
				if(!listaClasesQueNoSeInsertaran.contains(name)){
					Clase  cl3 = new Clase();
					idtoAinsertar = ((Integer) keyClasses.get(name)).intValue();
					cl3.setIdto(idtoAinsertar);
					cl3.setName(name);
					clasesAInsertarBD.add(cl3);
					//System.out.println("La clase "+name+" podría insertarse en la BD puesto que de momento cumple todos los requisitos");
				}
				else
					System.out.println("La clase "+ name + " no va a ser insertada puesto que alguna de sus properties es incorrecta.");
				//else clasesFich.remove(ncl);
			}
			
			/*Ahora vamos a comprobar la herencia de 
			 * cada clase y creamos una lista con las futuras clases a insertar, ya con su idto y el de su pàdre				 * 
			 */
			
			
			ArrayList subClasesFich = pfa.listaSubclase;
			ArrayList clasesBD = pbd.tablaClasesBD;
			ArrayList subClasesAInsertar = new ArrayList();
			
			ArrayList totalClasesSistema = new ArrayList();
			totalClasesSistema.addAll(clasesBD);
			totalClasesSistema.addAll(clasesAInsertarBD);
			
			
			ArrayList<Instance> instancesAInsertarBD = new ArrayList();	
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("************* Insercion individuos tabla Clase ************");
			System.out.println("***********************************************************");
			System.out.println("");
			
			HashMap individualsKeys = KeysManager.buildKeyIndividuals(pf.listaIndividuos);
			System.out.println(pf.listaIndividuos);
			System.out.println(individualsKeys);
			
			
			Iterator itIndCl = pfa.listaIndividuos.iterator();
			while(itIndCl.hasNext()){
				Individuo indv = (Individuo) itIndCl.next();
				String sClase = indv.getTipo();
				if(((pf.listaClase.contains(sClase) || listaNombresBD.contains(sClase)) && !Constants.isBasicType(sClase))){
					int idto = buscaIdto(pbd.tablaClasesBD,indv.getName());
					if(idto==-1){
						Clase c = new Clase();
						int nextPkInd = ((Integer) individualsKeys.get(indv.getName())).intValue();
						c.setIdto(nextPkInd);
						idto = nextPkInd++;
						c.setName(indv.getName());
						clasesAInsertarBD.add(c);
					}
					int idtoClase = buscaIdto(pbd.tablaClasesBD, sClase);
					if(idtoClase==-1)
						idtoClase = buscaIdto(clasesAInsertarBD, sClase);
					
					int clsvalue = Constants.IDTO_STRING;
					Instance ins = ii.createInstanceIndividuo(idtoClase, idto, indv.getName(), clsvalue, Constants.IdPROP_RDN, -1, -1);
					instancesAInsertarBD.add(ins);
					System.out.println(ins);
					//Instance ins2 = ii.createInstanceIndividuo(idtoClase, idto, indv.getName(), Constants.IDTO_INT, Constants.IdPROP_LEVEL, Constants.LEVEL_INDIVIDUAL, Constants.LEVEL_INDIVIDUAL);
					//instancesAInsertarBD.add(ins2);
					
				}
				
				else if(Constants.isBasicType(sClase)){
					System.out.println(indv.getName()+" no es un individuo, sino un valor concreto de un tipo basico. Lo usaremos pero no lo insertaremos");
				}
								
				else{
					System.out.println("El individuo "+indv.getName()+" no se puede insertar. Su clase "+sClase+" no existe");
				}
				
			}
			
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("****************** Insercion en tabla herencia ************");
			System.out.println("***********************************************************");
			System.out.println("");
			
			
			//System.out.println(subClasesFich);
			
			Iterator it2 = subClasesFich.iterator();
			
			while(it2.hasNext()){
				SubClase sc = (SubClase) it2.next();
				String  nsc = (String) sc.getName();
				if(!listaClasesQueNoSeInsertaran.contains(nsc)){
					System.out.println("La clase "+nsc+" ya existia en la base de datos o tiene alguna propiedad incorrecta que podria poner en peligro el modelo. Verifíquelo.");
				}
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
							//System.out.println("La clase "+sc.getName()+" tiene como padre a "+sPadre+ " la cual tiene una idto de "+c.getIdto());
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
						if(!enc){
							System.out.println("La clase "+nsc+" tiene como padre a "+sPadre+" y no ha sido creada. Tendrá que crearla para el correcto funcionamiento");
							
						}
						
						subClasesAInsertar.add(scbd);
					
					}
				
			}
			
			ArrayList listaAuxiliar = new ArrayList(clasesAInsertarBD);
			listaAuxiliar.addAll(pbd.tablaClasesBD);
			
			Iterator itmsc = pf.listaClase.iterator();
			while(itmsc.hasNext()){
				String s = (String) itmsc.next();
				SubclaseDB scbd = new SubclaseDB();
				int idto = buscaIdto(listaAuxiliar, s);
				scbd.setIdto(idto);
				scbd.setPadre(Constants.CLS_THING);
				scbd.setName(s);
				scbd.setIdtoPadre(0);
				subClasesAInsertar.add(scbd);
				//System.out.println("La clase "+scbd.getName()+" tiene como padre a "+scbd.getPadre()+ " la cual tiene una idto de "+scbd.getIdtoPadre());
			}
			
			
			
			/* 
			 * Ahora insertaremos las propiedades en su tabla correspondiente
			 * */
			
			
			PropertiesDAO pdao = new PropertiesDAO();
			pdao.open();
			String spk = pdao.getLastPK();
			pdao.close();
			Integer inp = Integer.parseInt(spk);
			int propPk = inp.intValue() + 1;
			if(propPk<Constants.MIN_IdPROP_MODEL)
				propPk = Constants.MIN_IdPROP_MODEL;
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("**************** Insercion en tabla propiedades ***********");
			System.out.println("***********************************************************");
			System.out.println("");
			
			
			ArrayList<PropiedadBD> propiedadesAInsertarBD = new ArrayList();
			//System.out.println(propiedadesFich);
			ArrayList<PropiedadBD> propiedadesConInversa = new ArrayList<PropiedadBD>();
			
			
			
			/*
			 * Para cada propiedad de cada clase habrá que insertar una entrada en la 
			 * tabla instances
			 */
			Iterator itpf = propiedadesFich.iterator();
			while(itpf.hasNext()){
				PropiedadClase pc = (PropiedadClase) itpf.next();
				ArrayList categorias = pc.getCategorias();
				Category categoria = new Category();
				if(categorias.contains("PLAY"))
					//categoria.setPlay();
				if(categorias.contains("STRUCTURAL"))
					categoria.setStructural();
				if(categorias.contains("DATA"))
					categoria.setDataProperty();
				if(categorias.contains("OBJECT"))
					categoria.setObjectProperty();
						
						
				
				
				/*
				 * Para cada clase perteneciente al rango de la propiedad
				 * habrá que insertar un registro en la tabla instances
				 */
				
				ArrayList rango;
				Iterator itRango;
				if(pc.getRango().size()>0){
				
					rango = pc.getRango();
					itRango = rango.iterator();
				
				
				
					while(itRango.hasNext()){
						PropiedadBD pbd2 = new PropiedadBD();
						int idto=-1;
						String nClase = (String) itRango.next();
						ArrayList lAux = new ArrayList(clasesAInsertarBD);
						lAux.addAll(pbd.tablaClasesBD);
						idto = buscaIdto(lAux, nClase);
						String name = pc.getNombreProp();
						/* Si la clase del rango no existiera se informaría y habria que crearla 
						 * manualmente e insertar luego el id en la base de datos.
						 */
						if(idto == -1){
							System.out.println("La clase "+nClase+" no se ha creado. Deberá insertarla para que la propiedad tenga sentido");
						}							// Si no queremos insertarla pondriamos aqui un else 
						//pbd2.addRango(new Integer(idto));
						int idtoAinsertar;
						
					
						 if(name.equals(Constants.PROP_MYFUNCTIONALAREA)){
		            		 idtoAinsertar=Constants.IdPROP_MYFUNCTIONALAREA;
		            	 }
						
						else if(name.equals(Constants.PROP_RDN)){
		            		 idtoAinsertar=Constants.IdPROP_RDN;
		            	 }
						
						else if(name.equals(Constants.PROP_TARGETCLASS)){
		            		 idtoAinsertar=Constants.IdPROP_TARGETCLASS;
		            	 }
						
						else if(name.equals(Constants.PROP_LOGO)){
		            		 idtoAinsertar=Constants.IdPROP_LOGO;
		            	 }
						
						else if(name.equals(Constants.PROP_USERROL)){
		            		 idtoAinsertar=Constants.IdPROP_USERROL;
		            	 }
						
						else
							idtoAinsertar=propPk;
						
								
						
						pbd2.setIdProp(idtoAinsertar);
						pbd2.setName(pc.getNombreProp());
						pbd2.setMask(pc.getMask());
						pbd2.setCat(categoria.getCat().intValue());
						pbd2.setLongitud(pc.getLongitud());
						pbd2.setCls(idto);
						if(pc.getOp()!= null)
							pbd2.setOp(pc.getOp());
						else
							pbd2.setOp("AND");
						if(pc.getQMax()!=-1)
							pbd2.setQMax(pc.getQMax());
						if(pc.getQMin() != -1)
							pbd2.setQMin(pc.getQMin());
						System.out.println(pbd2);
						if(pc.getPropInv()!=null){
							pbd2.setPropInv(pc.getPropInv());
							propiedadesConInversa.add(pbd2);
							
						}
						propiedadesAInsertarBD.add(pbd2);
					}
					propPk++;
			
				
				}
				
				else if(pc.getEnumerados().size()>0){
					
					Iterator itEnumerados = pc.getEnumerados().iterator();
					while(itEnumerados.hasNext()){
						PropiedadBD pbd2 = new PropiedadBD();
						String sEnumerado = (String) itEnumerados.next();
						Individuo indv = buscaIndividuo(pfa.listaIndividuos, sEnumerado);
						
						if(indv!=null){
							String sClase = indv.getTipo();
							ArrayList lAux = new ArrayList(clasesAInsertarBD);
							lAux.addAll(pbd.tablaClasesBD);
							int idto = buscaIdto(lAux, sClase);
							
							if(idto == -1){
								System.out.println("La clase "+sClase+" no se ha creado. Deberá insertarla para que la propiedad tenga sentido");
							}							// Si no queremos insertarla pondriamos aqui un else 
							//pbd2.addRango(new Integer(idto));
							
							if(Constants.isBasicType(indv.getTipo()))
								categoria.setDataProperty();
							pbd2.setIdProp(propPk);
							pbd2.setValue(indv.getName());
							pbd2.setName(pc.getNombreProp());
							pbd2.setMask(pc.getMask());
							pbd2.setCat(categoria.getCat().intValue());
							pbd2.setLongitud(pc.getLongitud());
							pbd2.setCls(idto); //No se usa
							if(pc.getOp()!= null)
								pbd2.setOp(pc.getOp());
							else
								pbd2.setOp("AND");
							pbd2.setQMax(pc.getQMax());
							pbd2.setQMin(pc.getQMin());
							System.out.println(pbd2);
							if(pc.getPropInv()!=null){
								pbd2.setPropInv(pc.getPropInv());
								propiedadesConInversa.add(pbd2);
								
							}
							propiedadesAInsertarBD.add(pbd2);
						
						}
						
						else System.out.println("Error!: El individuo "+sEnumerado+" no existe, no se insertará como parte de la propiedad");
					}
					
					propPk++;
				}
				
				
				
			}
			
			
			Iterator itpropInv = propiedadesConInversa.iterator();
			while(itpropInv.hasNext()){
				PropiedadBD prInv = (PropiedadBD) itpropInv.next();
				int idp = buscaIdProp(propiedadesAInsertarBD, prInv.getPropInv());
				if(idp==-1){
					System.out.println("Ha declarado como propiedad Inversa de "+prInv.getName()+" una propiedad que no existía: "+prInv.getPropInv());	
					prInv.setPropInv(null);
					
				}
				else
					prInv.setPropInv(new Integer(idp).toString());
			}
			
			Auxiliar.leeTexto("Sigue");

			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("**************** Insercion properties play ***********");
			System.out.println("***********************************************************");
			System.out.println("");
			
			
			/*
			 * Segun la estructura y el uso de la base de datos, por cada rol
			 * debemos crear una propiedad de categoria play con la forma "playx"
			 * donde x es el nombre del rol. Las creamos y le asignamos el
			 * id de la propiedad
			 */
			
			ArrayList propiedadesPlay = new ArrayList();
			ArrayList listaRoles = pf.listaRol;
			Iterator itRol = listaRoles.iterator();
			
			while(itRol.hasNext()){
				String nRol = (String) itRol.next();
				String primeraLetra = nRol.substring(0, 1);
				primeraLetra=primeraLetra.toUpperCase();
				String restoPalabra = nRol.substring(1);
				String nuevaProp = "play";
				nuevaProp = nuevaProp.concat(primeraLetra);
				nuevaProp = nuevaProp.concat(restoPalabra);
				
				if(buscaIdProp(pbd.tablaPropertiesBD, nuevaProp) != -1){
					System.out.println("La propiedad "+nuevaProp+" ya existia, si dicho rol lo juegan diferentes clases que en el modelo anterior deberá cambiarlo.");
				}
					else {
					ArrayList cAux = buscaRol(pfa.listaRol, listaAuxiliar, nRol);
					String op;
					if(cAux.size()>1)
						op = Constants.OP_UNION;
					else
						op = Constants.OP_INTERSECTION;
					Iterator itcAux = cAux.iterator();
					while(itcAux.hasNext()){
						Integer cls = (Integer) itcAux.next();
						PropiedadBD pbd3 = new PropiedadBD();
						pbd3.setIdProp(propPk);
						pbd3.setName(nRol);
						propiedadesPlay.add(pbd3);
						Category category = new Category();
						//category.setPlay();
						category.setObjectProperty();
						pbd3.setCls(cls.intValue());
						pbd3.setOp(op);
						pbd3.setCat(category.getCat().intValue()); 
						pbd3.setName(nuevaProp);
						propiedadesAInsertarBD.add(pbd3);
						System.out.println(pbd3);
					}
				propPk++;
				}
			}
		
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("**************** Insercion properties playIn ***********");
			System.out.println("***********************************************************");
			System.out.println("");
			
			
			ArrayList propiedadesPlayIn = new ArrayList();		
			itRol = listaRoles.iterator();
					
			while(itRol.hasNext()){
				String nRol = (String) itRol.next();
				String primeraLetra = nRol.substring(0, 1);
				primeraLetra=primeraLetra.toUpperCase();
				String restoPalabra = nRol.substring(1);
				String nuevaProp = "play";
				nuevaProp = nuevaProp.concat(primeraLetra);
				nuevaProp = nuevaProp.concat(restoPalabra);
				nuevaProp = nuevaProp.concat("INV");
				if(buscaIdProp(pbd.tablaPropertiesBD, nuevaProp) != -1){
					System.out.println("La propiedad "+nuevaProp+" ya existia, si dicho rol lo juegan diferentes clases que en el modelo anterior deberá cambiarlo.");
				}
				else{
					ArrayList cAux = buscaRelaciones(pfa.listaRolAtrib, nRol);
					String op;
					if(cAux.size()>1)
						op = Constants.OP_UNION;
					else
						op = Constants.OP_INTERSECTION;
					Iterator itcAux = cAux.iterator();
					while(itcAux.hasNext()){
						String relac = (String) itcAux.next();
						int idtrelac = buscaIdto(listaAuxiliar, relac);
						PropiedadBD pbd3 = new PropiedadBD();
						PropiedadBD pbd4 = new PropiedadBD();
						pbd3.setIdProp(propPk);
						pbd3.setName(nRol);
						pbd3.setCls(idtrelac);
						pbd4.setIdProp(propPk);
						pbd4.setName(nRol);
						pbd4.setCls(idtrelac);
						propiedadesPlayIn.add(pbd4);
						Category category = new Category();
						//category.setPlayIn();
						category.setObjectProperty();
						pbd3.setOp(op);
						pbd3.setCat(category.getCat().intValue()); 
						pbd3.setName(nuevaProp);
						propiedadesAInsertarBD.add(pbd3);
						System.out.println(pbd3);
					}
					propPk++;
				}
			}
			
			
			
			
			
		/* Vamos a crear las instances para las clases */
			
			System.out.println("");
			System.out.println("***********************************************************");
			System.out.println("************ Insercion instances clases con atrib *********");
			System.out.println("***********************************************************");
			System.out.println("");
			
		
			
		ArrayList propsTotal = new ArrayList(propiedadesAInsertarBD);
		propsTotal.addAll(pbd.tablaPropertiesBD);
		
		ArrayList listaCRR2 = new ArrayList(); 
		//Nota: CRR = Clases + Relaciones + Roles
		listaCRR2.addAll(pf.listaClase);
		listaCRR2.addAll(pf.listaRol);
		listaCRR2.addAll(pf.listaRelacion);
		
		Iterator itcl = listaCRR2.iterator();
		while(itcl.hasNext()){
			int idto;
			String name = (String) itcl.next();
			if(!Constants.isBasicClass(name)){
				Clase c= buscaClase(listaAuxiliar, name);
				if(c!=null)
					idto = c.getIdto();
				else
					idto = 999999;
				ArrayList propAtrib = ii.buscaPropiedadesAtrib(propiedadesFichAt, name);
				Iterator itpa = propAtrib.iterator();
				while(itpa.hasNext()){
					PropiedadAtrib patr = (PropiedadAtrib) itpa.next();
					if(!pf.listaRolPointer.contains(patr.getNombreProp())){
						ArrayList lpbd = ii.buscaPropiedadBD(propsTotal, patr.getNombreProp());
						if(c!=null)
							instancesAInsertarBD.addAll(ii.creaInstanceClase(c, lpbd, patr, listaAuxiliar, pfa.listaIndividuos));
					}
					else{
						RolPointer rp = buscaRolPointer(pfa.listaRolPointers, patr.getNombreProp());
						int idRelb = buscaIdto(listaAuxiliar, rp.getRelacion());
						int idRolb = buscaIdto(listaAuxiliar, rp.getRol());
						int idProp = buscaIdProp(propsTotal, patr.getNombreProp());
						if(idto==-1 || idRelb==-1 || idRolb==-1 || idProp==-1){
							System.out.println("Error al insertar el rol pointer. Verifique que todo ha sido creado correctamente.");
						}
						else
							instancesAInsertarBD.addAll(ii.createInstanceClaseRolPointer(idto, idProp, idRolb, idRelb, rp.getQMin(), rp.getQMax()));
						
					}
				}	
				
			}
		}
		System.out.println(instancesAInsertarBD);
		
		System.out.println("");
		System.out.println("***********************************************************");
		System.out.println("*********** Insercion instances clases sin atrib **********");
		System.out.println("***********************************************************");
		System.out.println("");
		
		/* 
		 * Si alguna clase no tiene atributos, insertamos en la tabla
		 * instances sólo su idto y su nombre
		 */
		
		Iterator itclsa = pf.listaClase.iterator(); //itclsa = iterator clases sin atributos
		
		while(itclsa.hasNext()){
			String s = (String) itclsa.next();
			int cont = 0;
			Iterator itfich2 = pfa.listaPropAt.iterator();
			while(itfich2.hasNext()){
				PropiedadAtrib pra = (PropiedadAtrib) itfich2.next();
				if(s.equals(pra.getClaseCont())){
					cont++;
				}
			}
			if(cont == 0 && !Constants.isBasicType(s) && !Constants.isBasicClass(s)){ 
				Instance ins = new Instance();
				int idto = buscaIdto(listaAuxiliar, s);
				if(idto != -1){
					System.out.println("La clase "+s+" no tiene atributos, insertaremos en la tabla instance: ");
					ins.setIDTO(new Integer(idto).toString());
					ins.setNAME(s);
					System.out.println(ins);
					instancesAInsertarBD.add(ins);
				}
			}
			
		}
		
		
		
		
		System.out.println("");
		System.out.println("***********************************************************");
		System.out.println("************ Insercion tabla instances relaciones **********");
		System.out.println("***********************************************************");
		System.out.println("");
		
		
		Iterator itRel = pf.listaRelacion.iterator();
		while(itRel.hasNext()){
			String relacion = (String) itRel.next(); 
			Iterator itra = pfa.listaRolAtrib.iterator();
			while(itra.hasNext()){
				RolAtrib ra = (RolAtrib) itra.next();
				if(relacion.equals(ra.getRelacionCont())){
					String nRol = ra.getNombreRol();
					String primeraLetra = nRol.substring(0, 1);
					primeraLetra=primeraLetra.toUpperCase();
					String restoPalabra = nRol.substring(1);
					String nuevaProp = "play";
					nuevaProp = nuevaProp.concat(primeraLetra);
					nuevaProp = nuevaProp.concat(restoPalabra);
					int idtprop = buscaIdProp(propsTotal, nuevaProp);
					Iterator itrol = pfa.listaRol.iterator();
					while(itrol.hasNext()){
						Rol rol = (Rol) itrol.next();
						if(ra.getNombreRol().equals(rol.getName())){
							ArrayList cljuego;
							if(ra.getNuevosJuegos().size()==0){
								cljuego = rol.getListaJuegos();
							}
							else{
								cljuego = ra.getNuevosJuegos();

							}
							int idtRol = buscaIdto(listaAuxiliar, ra.getNombreRol());
							int idtorel = buscaIdto(listaAuxiliar, relacion);
							if(idtorel > -1){
								Instance relCard  = ii.createInstanceRelacion(idtorel, idtprop, idtRol, ra.getQMax(), ra.getQMin());
								instancesAInsertarBD.add(relCard);
								System.out.println(relCard);
								Iterator itclj2 = cljuego.iterator();
								while(itclj2.hasNext()){
									String cls = (String) itclj2.next();
									int idto2 = buscaIdto(listaAuxiliar,cls);
									if(idto2 >= 0){
										String oper = null;
										if(cljuego.size()>1)
											oper = "OR";
										else oper = "AND";
										Instance ins2 = ii.createInstanceRelacion(idtorel, idtprop, idto2, idtRol, oper, ra.isFull());
										instancesAInsertarBD.add(ins2);
										System.out.println(ins2);
									}
								
									else
										System.out.println("La relacion "+relacion+" no ha podido crearse correctamente. No se insertará");
								}
							}
						
						}
					}
				}
				
			}
		}
		
		

		System.out.println("");
		System.out.println("***********************************************************");
		System.out.println("************ Insercion tabla instances playIn **********");
		System.out.println("***********************************************************");
		System.out.println("");
		
		Iterator propiedadesInIterator = propiedadesPlayIn.iterator();
		while(propiedadesInIterator.hasNext()){
			PropiedadBD propIn = (PropiedadBD) propiedadesInIterator.next();
			String relacion = buscaClaseConIdto(listaAuxiliar,propIn.getCls()).getName();
			String nameRol = propIn.getName();
			Iterator rolAtribIterator = pfa.listaRolAtrib.iterator();
			while(rolAtribIterator.hasNext()){
				RolAtrib ra = (RolAtrib) rolAtribIterator.next();
				if(ra.getRelacionCont().equals(relacion) && ra.getNombreRol().equals(nameRol)){
					Iterator nuevosJuegosIterator;
					if(ra.getNuevosJuegos().size()>0){
						nuevosJuegosIterator = ra.getNuevosJuegos().iterator();
					}
					else{
						Rol r = buscaRolClase(pfa.listaRol, nameRol);
						nuevosJuegosIterator = r.getListaJuegos().iterator();
					}
						while(nuevosJuegosIterator.hasNext()){
							String juego = (String) nuevosJuegosIterator.next();
							int idtClase = buscaIdto(listaAuxiliar, juego);
							int idtRol = buscaIdto(listaAuxiliar, ra.getNombreRol());
							ArrayList<Instance> ins = ii.createInstancePlayIn(idtClase, idtRol, propIn.getIdProp(), propIn.getCls(), ra.getQMinInv(), ra.getQMaxInv());
							instancesAInsertarBD.addAll(ins);
							
							System.out.println(ins);
						}
					}
				}
			}
		
		
		System.out.println("");
		System.out.println("***********************************************************");
		System.out.println("************       Insercion tabla access        **********");
		System.out.println("***********************************************************");
		System.out.println("");
		
		
		ArrayList<Access> accesosAInsertarBD = new ArrayList();
		AccessTreatment at = new AccessTreatment();
		ArrayList<String> utasks = at.getUtasks(pfa.listaSubclase);
		System.out.println("SUBCLASES :"+pfa.listaSubclase);
		System.out.println("UTASKS: "+utasks);
		Iterator itUtasks = utasks.iterator();
		while(itUtasks.hasNext()){
			String utaskName = (String) itUtasks.next();
			ArrayList<PropiedadAtrib> atribs = at.getPropertiesUtask(pfa.listaPropAt, utaskName);
			Iterator itOperation = at.getOperation(atribs).iterator();
			while(itOperation.hasNext()){
					
				int operation = ((Integer) itOperation.next()).intValue();
				if(operation == -1){
					System.out.println("ERROR: Ha usado una operacion no existente, deberá usar una de las predefinidas : NEW, SET, VIEW, DEL, CONCRETE");
				}
				else{
					
					// TODO En un futuro, si las UTASK tuvieran como targetClass varias clases en el rango, en vez de get(0) usariamos un iterator
					Iterator itTC = at.getRangoTarget(atribs, Constants.PROP_TARGETCLASS).iterator();
					while(itTC.hasNext()){
						String targetClass = (String) itTC.next();
						Clase c = buscaClase(listaAuxiliar, targetClass);
						if(c==null)
							System.out.println("ERROR: La clase "+targetClass+" no existe");
						int idto = buscaClase(listaAuxiliar, targetClass).getIdto();
						ArrayList userRolList = at.getRangoTarget(atribs, Constants.PROP_USERROL);
						int user = buscaIdto(listaAuxiliar, utaskName);
						int idtoURol=-1;
						if(userRolList!=null && userRolList.size()>0){
							String userRol = (String) userRolList.get(0);
							idtoURol = buscaClase(listaAuxiliar, userRol).getIdto();
						}
						
						accesosAInsertarBD.add(at.createAccess(user,operation, idto, idtoURol));
						
						
						
						
					}
				}
			}	
		}
		
		
		
		
		System.out.println("");
		System.out.println("***********************************************************");
		System.out.println("***********   Insercion peers tabla instances    **********");
		System.out.println("***********************************************************");
		System.out.println("");
		
		Iterator itrels = pf.listaRelacion.iterator();
		while(itrels.hasNext()){
			String rel = (String) itrels.next();
			ArrayList<RolAtrib> roles = getRolesFromRelacion(rel, pfa.listaRolAtrib);
			instancesAInsertarBD.addAll(trataPeers(roles, listaAuxiliar));
		}
		
		
		
		//System.out.println(clasesAInsertarBD);
		//System.out.println(subClasesAInsertar);
		//System.out.println(propiedadesAInsertarBD);
		//System.out.println(instancesAInsertarBD);
		
	
		/* Inserción en la BASE DE DATOS!! */
		
		String pregunta = Auxiliar.leeTexto("¿Desea insertar ya en la base de datos?");
		
	
		
		/* INSERCION DE CLASES */
		
		if(pregunta.equals("S")){
		
		Iterator itclasesBD = clasesAInsertarBD.iterator();
		TClaseDAO tcdao = new TClaseDAO();
		tcdao.open();
		while(itclasesBD.hasNext()){
			TClase tClase = new TClase();
			Clase clase = (Clase) itclasesBD.next();
			tClase.setIDTO(clase.getIdto());
			tClase.setName(clase.getName());
			tcdao.insert(tClase);
		}
		tcdao.close();
		
		
		
		
		
		
		
		/* INSERCION EN PROPERTIES */
		
		
			PropertiesDAO tpdao = new PropertiesDAO();
			tpdao.open();
			Iterator itPropertiesBD = propiedadesAInsertarBD.iterator();
			while(itPropertiesBD.hasNext()){
				Properties property = new Properties();
				PropiedadBD propBD = (PropiedadBD) itPropertiesBD.next();
				property.setCAT(propBD.getCat());
				property.setMASK(propBD.getMask());
				property.setNAME(propBD.getName());
				property.setOP(propBD.getOp());
				property.setPROP(propBD.getIdProp());
				if(propBD.getLongitud()!=-1)
					property.setLENGTH(propBD.getLongitud());
				ArrayList<PropiedadBD> lPropAux = new ArrayList(pbd.tablaPropertiesBD);
				lPropAux.addAll(propiedadesAInsertarBD);
				/*int propInverse = getIdPropInverse(propBD, lPropAux);
				if(propInverse!=-1)
					property.setPROPINV(new Integer(propInverse));*/
				
				if(propBD.getPropInv()!=null)
					property.setPROPINV(Integer.parseInt(propBD.getPropInv()));
				if(propBD.getQMax()!=-1)
					property.setQMAX(new Float(propBD.getQMax()));
				if(propBD.getQMin()!=-1)
					property.setQMIN(new Float(propBD.getQMin()));
				property.setVALUE(propBD.getValue());
				if(propBD.getCls()!=-1)
					property.setVALUECLS(new Integer(propBD.getCls()).intValue()); 
				tpdao.insert(property);
				
			}
			tpdao.close();
			
	
			 /* INSERCION EN INSTANCES  */
			
			
			InstanceDAO insdao = new InstanceDAO();
			insdao.open();
			Iterator itInstancesBD = instancesAInsertarBD.iterator();
			while(itInstancesBD.hasNext()){
				Instance insBD = (Instance) itInstancesBD.next();
				if(insBD.getIDTO()!= null){
					Clase cl = buscaClaseConIdto(clasesAInsertarBD, Integer.parseInt(insBD.getIDTO()));
					if(cl!=null)
						insBD.setNAME(cl.getName());
				}
				if(insBD.getVALUECLS() == "-1")
					insBD.setVALUECLS(null);
				if(insBD.getIDO() != null)
						insBD.setNAME(insBD.getVALUE());
				if(insBD.getOP()==null)
					insBD.setOP(Constants.OP_INTERSECTION);
				if(insBD.getIDO() != null)
					insBD.setOP(null);
				if(insBD.getQMAX()==null && insBD.getQMIN()==null && insBD.getOP()!=null && insBD.getOP().equals(Constants.OP_CARDINALITY))
					;
				else
					insdao.insert(insBD);
			}
			
			insdao.close();
			

			/* INSERCION EN HERENCIA */
			
			
			Iterator itHerenciaBD = subClasesAInsertar.iterator();
			T_HerenciasDAO thdao = new T_HerenciasDAO();
			thdao.open();
			while(itHerenciaBD.hasNext()){
				T_Herencias tClase = new T_Herencias();
				SubclaseDB sclase = (SubclaseDB) itHerenciaBD.next();
				tClase.setID_TO(sclase.getIdto());
				tClase.setID_TO_Padre(sclase.getIdtoPadre());
				thdao.insert(tClase);
			}
			thdao.close();
			
			
			/*INSERCION DE ACCESS */
			
			AccessDAO accdao = new AccessDAO();
			accdao.open();
			Iterator itaccesosBD = accesosAInsertarBD.iterator();
			while(itaccesosBD.hasNext()){
				Access acc = (Access) itaccesosBD.next();
				accdao.insert(acc);
			}
			
			accdao.close();
			
		
		
		}
		
		else
			System.out.println("De acuerdo. Corrija fallos y vuelva a compilar");
		
	
		 } catch (ANTLRException ae) {
		System.err.println(ae.getMessage());
		} catch (FileNotFoundException fnfe) {
		System.err.println("No se encontró el fichero");
		} catch(Exception e){
		e.printStackTrace();
	}
	}
	
	/*private static ArrayList<PropiedadAtrib> eliminaRepetidos(ArrayList<PropiedadAtrib> propiedadesFichAt) {
		ArrayList<PropiedadAtrib> copia = new ArrayList<PropiedadAtrib>();
		Iterator it = propiedadesFichAt.iterator();
		int i = 0;
		while(it.hasNext()){
			PropiedadAtrib pf = (PropiedadAtrib) it.next();
			int i2 = propiedadesFichAt.lastIndexOf(pf);
			if(i==i2)
				copia.add(pf);	
			i++;
		}
		return copia;
	}*/
	
	private static ArrayList<PropiedadAtrib> eliminaPropsRepetidas(ArrayList<PropiedadAtrib> propiedadesFichAt) {
	ArrayList<PropiedadAtrib> copia = new ArrayList<PropiedadAtrib>();
	for(int i=0;i<propiedadesFichAt.size();i++){
		PropiedadAtrib pat = (PropiedadAtrib) propiedadesFichAt.get(i);
		for(int j=0; j<propiedadesFichAt.size();j++){
			PropiedadAtrib pat2 = (PropiedadAtrib) propiedadesFichAt.get(j);
			if(pat.equals(pat2)){
				if(i==j && !copia.contains(pat2))
					copia.add(pat);
				
			}
		}
	}
	return copia;
	}
	
	private static ArrayList<PropiedadAtrib> eliminaPropsConsecutivas(ArrayList<PropiedadAtrib> propiedadesFichAt) {
		ArrayList<PropiedadAtrib> copia = new ArrayList<PropiedadAtrib>();
		
		
		ArrayList<PropiedadAtrib> copia2 = new ArrayList<PropiedadAtrib>();
		for(int i=propiedadesFichAt.size()-1;i>=1;i--){
			PropiedadAtrib pat = (PropiedadAtrib) propiedadesFichAt.get(i);
			PropiedadAtrib pat2 = (PropiedadAtrib) propiedadesFichAt.get(i-1);
			if(!pat.equals(pat2))
				copia2.add(pat);
		}
		for(int i=0;i<propiedadesFichAt.size()-1;i++){
			PropiedadAtrib pat = (PropiedadAtrib) propiedadesFichAt.get(i);
			PropiedadAtrib pat2 = (PropiedadAtrib) propiedadesFichAt.get(i+1);
			if(!pat.equals(pat2))
				copia.add(pat);
		}
		
		if(!copia.contains(propiedadesFichAt.get(propiedadesFichAt.size()-1)))
			copia.add(propiedadesFichAt.get(propiedadesFichAt.size()-1));
		
		return copia;
	}
		
		private static ArrayList<RolAtrib> eliminaRolesRepetidos(ArrayList<RolAtrib> propiedadesFichAt) {
			ArrayList<RolAtrib> copia = new ArrayList<RolAtrib>();
			for(int i=0;i<propiedadesFichAt.size();i++){
				RolAtrib pat = (RolAtrib) propiedadesFichAt.get(i);
				for(int j=0; j<propiedadesFichAt.size();j++){
					RolAtrib pat2 = (RolAtrib) propiedadesFichAt.get(j);
					if(pat.equals(pat2)){
						if(i==j && !copia.contains(pat2))
							copia.add(pat);
						
					}
				}
			}
			return copia;
			}
			
			private static ArrayList<RolAtrib> eliminaRolesConsecutivos(ArrayList<RolAtrib> propiedadesFichAt) {
				ArrayList<RolAtrib> copia = new ArrayList<RolAtrib>();
				ArrayList<RolAtrib> copia2 = new ArrayList<RolAtrib>();
				for(int i=propiedadesFichAt.size()-1;i>=1;i--){
					RolAtrib pat = (RolAtrib) propiedadesFichAt.get(i);
					RolAtrib pat2 = (RolAtrib) propiedadesFichAt.get(i-1);
					if(!pat.equals(pat2))
						copia2.add(pat);
					
				}
		
		for(int i=0;i<propiedadesFichAt.size()-1;i++){
			RolAtrib pat = (RolAtrib) propiedadesFichAt.get(i);
			RolAtrib pat2 = (RolAtrib) propiedadesFichAt.get(i+1);
			if(!pat.equals(pat2))
				copia.add(pat);
		}
		
		if(propiedadesFichAt.size() >0 && !copia.contains(propiedadesFichAt.get(propiedadesFichAt.size()-1)))
			copia.add(propiedadesFichAt.get(propiedadesFichAt.size()-1));
		
		return copia;
	}
	
	
	private static void addHierarchyPropsRec(ArrayList<SubClase> subcls,ArrayList<PropiedadAtrib> propsHerencia, ArrayList<RolAtrib> rolesHerencia, ArrayList<PropiedadAtrib> props, ArrayList<RolAtrib> rols, ArrayList<String> rels) {
		
		
		System.out.println("SUBCLASE: "+subcls);
		Iterator itsubClases = subcls.iterator();
		ArrayList<String> tratados = new ArrayList<String>();
		while(itsubClases.hasNext()){
			SubClase sc = (SubClase) itsubClases.next();
			addHierarchyPropsRecAux(sc.getName(), subcls, propsHerencia, rolesHerencia, props, rols, rels, tratados);
			
		}
		System.out.println("NUEVAS PROPS: "+propsHerencia);
		System.out.println("NUEVOS ROLES :"+rolesHerencia);
	}
	
	/*private static void addHierarchyPropsRecAux(SubClase sc, ArrayList<SubClase> subcls,ArrayList<PropiedadAtrib> propsHerencia, ArrayList<RolAtrib> rolesHerencia, ArrayList<PropiedadAtrib> props, ArrayList<PropiedadAtrib> rols, ArrayList<String> rels, ArrayList<String> tratados){
		
		if(!tratados.contains(sc.getName())){
			Iterator itPadres = sc.getListaPadres().iterator();
			while(itPadres.hasNext()){
				String padre = (String) itPadres.next();
				if(tratados.contains(padre)){
					propsHerencia.addAll(addHierarchyProperties(props, padre, sc.getName()));
					tratados.add(sc.getName());
				}
				else{
					SubClase scPadre = buscaPadres(padre, subcls);
					System.out.println("BUSCA PADRE: "+scPadre);
					if(scPadre==null){
						if(!padre.equals(Constants.CLS_UNIDADES) || !padre.equals(Constants.CLS_UTASK)){
							propsHerencia.addAll(addHierarchyProperties(props, padre, sc.getName()));
							tratados.add(sc.getName());
						}
					}
					else{
						addHierarchyPropsRecAux(scPadre,subcls, propsHerencia, rolesHerencia, props, rols, rels, tratados);
						
					}
				}
			}

		}

	}*/
	
private static void addHierarchyPropsRecAux(String scName, ArrayList<SubClase> subcls,ArrayList<PropiedadAtrib> propsHerencia, ArrayList<RolAtrib> rolesHerencia, ArrayList<PropiedadAtrib> props, ArrayList<RolAtrib> rols, ArrayList<String> rels, ArrayList<String> tratados){
		
		SubClase sc = buscaPadres(scName, subcls);
		if(sc!=null){
			Iterator it = sc.getListaPadres().iterator();
			while(it.hasNext()){
				String s = (String) it.next();
				addHierarchyPropsRecAux(s, subcls, propsHerencia, rolesHerencia, props, rols, rels, tratados);
				props.addAll(propsHerencia);
				rols.addAll(rolesHerencia);
				propsHerencia.addAll(addHierarchyProperties(props, s, scName));
				if(rels.contains(scName))
					rolesHerencia.addAll(addHierarchyRoles(rols, scName, s));
					
			}
		}
	
	}
			
	
	
	

	private static SubClase buscaPadres(String padre, ArrayList<SubClase> subcls) {
		Iterator it = subcls.iterator();
		while(it.hasNext()){
			SubClase sc = (SubClase) it.next();
			if(sc.getName().equals(padre))
				return sc;
		}
		return null;
	}

	private static Collection addHierarchyRoles(ArrayList listaRoles, String hijo, String padre) {
		

		ArrayList<RolAtrib> rolesPadre = buscaRoles(listaRoles, padre);
		ArrayList<RolAtrib> rolesHijo = buscaRoles(listaRoles, hijo);
		ArrayList<RolAtrib> rolesRes = new ArrayList<RolAtrib>();
		
		
		Iterator it = rolesPadre.iterator();
		while(it.hasNext()){
			RolAtrib rolPadre = (RolAtrib) it.next();
			boolean enc = false;
			Iterator it2 = rolesHijo.iterator();
			while(it2.hasNext() && !enc){
				RolAtrib rolHijo = (RolAtrib) it2.next();
				if(rolHijo.getNombreRol().equals(rolPadre.getNombreRol()))
					enc = true;
			}
			
			if(!enc){
				RolAtrib nuevoRol = new RolAtrib();
				nuevoRol.setRelacionCont(hijo);
				nuevoRol.setFull(rolPadre.isFull());
				nuevoRol.setNombreRol(rolPadre.getNombreRol());
				nuevoRol.setNuevosJuegos(rolPadre.getNuevosJuegos());
				nuevoRol.setOp(rolPadre.getOp());
				nuevoRol.setPeer(rolPadre.getPeer());
				nuevoRol.setQMax(rolPadre.getQMax());
				nuevoRol.setQMin(rolPadre.getQMin());
				nuevoRol.setQMinInv(rolPadre.getQMinInv());
				nuevoRol.setQMaxInv(rolPadre.getQMaxInv());
				rolesRes.add(nuevoRol);
			}
		}
		
		return rolesRes;
	}



	private static ArrayList<RolAtrib> buscaRoles(ArrayList listaRoles, String name) {
		ArrayList<RolAtrib> res = new ArrayList<RolAtrib>();
		Iterator it = listaRoles.iterator();
		while(it.hasNext()){
			RolAtrib pat = (RolAtrib) it.next();
			if(pat.getRelacionCont().equals(name))
				res.add(pat);
			
		}
		
		return res;
	}

	private static ArrayList<PropiedadAtrib> addHierarchyProperties(ArrayList listaPropAt, String padre, String hijo) {
		ArrayList<PropiedadAtrib> propiedadesPadre = buscaPropiedades(listaPropAt, padre);
		ArrayList<PropiedadAtrib> propiedadesHijo = buscaPropiedades(listaPropAt, hijo);
		ArrayList<PropiedadAtrib> propiedadesRes = new ArrayList<PropiedadAtrib>();
		
		
		
		Iterator it = propiedadesPadre.iterator();
		while(it.hasNext()){
			PropiedadAtrib patPadre = (PropiedadAtrib) it.next();
			boolean enc = false;
			Iterator it2 = propiedadesHijo.iterator();
			while(it2.hasNext() && !enc){
				PropiedadAtrib patHijo = (PropiedadAtrib) it2.next();
				if(patHijo.getNombreProp().equals(patPadre.getNombreProp()))
					enc = true;
			}
			
			if(!enc){
				PropiedadAtrib nuevaPat = new PropiedadAtrib();
				nuevaPat.setClaseCont(hijo);
				nuevaPat.setEnumerados(patPadre.getEnumerados());
				nuevaPat.setNombreProp(patPadre.getNombreProp());
				nuevaPat.setOp(patPadre.getOp());
				nuevaPat.setQMax(patPadre.getQMax());
				nuevaPat.setQMin(patPadre.getQMin());
				nuevaPat.setRestricciones(patPadre.getRestricciones());
				propiedadesRes.add(nuevaPat);
			}
		}
		return propiedadesRes;
	}

	private static ArrayList<PropiedadAtrib> buscaPropiedades(ArrayList listaPropAt, String name) {
		ArrayList<PropiedadAtrib> res = new ArrayList<PropiedadAtrib>();
		Iterator it = listaPropAt.iterator();
		while(it.hasNext()){
			PropiedadAtrib pat = (PropiedadAtrib) it.next();
			if(pat.getClaseCont().equals(name))
				res.add(pat);
			
		}
		
		return res;
	}

	private static int getIdPropInverse(PropiedadBD propBD, ArrayList<PropiedadBD> props) {
		String name = propBD.getName();
		Iterator it = props.iterator();
		int res = -1;
		if(name.contains("play")){
			if(name.contains("INV")){
				boolean enc = false;
				while(it.hasNext() && !enc){
					PropiedadBD pbd = (PropiedadBD) it.next();
					if(name.contains(pbd.getName()) && !name.equals(pbd.getName())){
						enc = true;
						res = pbd.getIdProp();
					}
				}
			}
			else{
				boolean enc = false;
				while(it.hasNext() && !enc){
					PropiedadBD pbd = (PropiedadBD) it.next();
					if((pbd.getName()).contains(name) && !name.equals(pbd.getName())){
						enc = true;
						res = pbd.getIdProp();
					}
				}
			}
		}
		return res;
	}

	private static int buscaIdProp(ArrayList<PropiedadBD> ps, String nombreProp) {
		Iterator pIterator = ps.iterator();
		while(pIterator.hasNext()){
			PropiedadBD pbd = (PropiedadBD) pIterator.next();
			if(nombreProp.equals(pbd.getName()))
				return pbd.getIdProp();
		}
		return -1;
	}

	private static ArrayList buscaRol(ArrayList listaRol, ArrayList<Clase> clasesAInsertarBD, String rol) {
		
		
		ArrayList<Integer> res = new ArrayList();
		Iterator it1 = listaRol.iterator();
		while(it1.hasNext()){
			Rol r = (Rol) it1.next();
			if(rol.equals(r.getName())){
				Iterator it2 = clasesAInsertarBD.iterator();
				while(it2.hasNext()){
					Clase cl = (Clase) it2.next();
					if(r.getListaJuegos().contains(cl.getName())){
						res.add(new Integer(cl.getIdto()));
					}
					
					
				}
			}
		}
		
			
		
		return res;
		
	}

	private static ArrayList compruebaClasesJuego(ArrayList<Rol> listaRoles, ArrayList<String> listaClases){
		Iterator it = listaRoles.iterator();
		ArrayList res = new ArrayList();
		while(it.hasNext()){
			Rol r = (Rol) it.next();
			ArrayList clj = r.getListaJuegos();
			if(!listaClases.containsAll(clj)){
				res.add(r.getName());
				System.out.println("El rol "+r.getName()+" ha sido declarado jugando clases que no existen");
			}
			else
				System.out.println("El rol "+r.getName()+" ha sido declarado jugando clases existentes. Correcto");
		}
		return res;
	}
	
	private static RolPointer buscaRolPointer(ArrayList<RolPointer> lr, String name){
		Iterator lrIterator = lr.iterator();
		while (lrIterator.hasNext()){
			RolPointer r = (RolPointer) lrIterator.next();
			if(name.equals(r.getNombre()))
				return r;
		}
		
		return null;
	}
	
	private static Rol buscaRolClase(ArrayList<Rol> lr, String name){
		
		Iterator lrIterator = lr.iterator();
		while (lrIterator.hasNext()){
			Rol r = (Rol) lrIterator.next();
			if(name.equals(r.getName()))
				return r;
		}
		
		return null;
			
	}
	
	
	public static int buscaIdto(ArrayList<Clase> l, String nClase){
		int res = -1;
		Iterator itAux2 = l.iterator();
		boolean enc = false;
		while(itAux2.hasNext() && !enc){
			Clase clAux = (Clase) itAux2.next();
			//System.out.println(nClase+","+clAux.getName());
			if(nClase.equals(clAux.getName())){
				enc = true;
				res = clAux.getIdto();
			}
		}
		return res;
	}
	
	public static int buscaIdPropPlay(ArrayList<PropiedadBD> l, String name){
		
		String primeraLetra = name.substring(0, 1);
		primeraLetra=primeraLetra.toUpperCase();
		String restoPalabra = name.substring(1);
		String nuevaProp = "play";
		nuevaProp = nuevaProp.concat(primeraLetra);
		nuevaProp = nuevaProp.concat(restoPalabra);
		Iterator it = l.iterator();
		
		int idto = -1;
		while(it.hasNext()){
			PropiedadBD p = (PropiedadBD) it.next();
			if(nuevaProp.equals(p.getName()))
				return p.getIdProp();
			
		}
		
		return idto;
	}
	
	private static ArrayList<String> listaNombresPropBD (ArrayList <PropiedadBD> l){
		Iterator it = l.iterator();
		ArrayList res = new ArrayList();
		while(it.hasNext()){
			PropiedadBD pbd = (PropiedadBD) it.next();
			String name = pbd.getName();
			res.add(name);
		}
		
		return res;
	}
	
	
	private static ArrayList<String> listaNombresPropFich(ArrayList <PropiedadClase> l){
		Iterator it = l.iterator();
		ArrayList res = new ArrayList();
		while(it.hasNext()){
			PropiedadClase pbd = (PropiedadClase) it.next();
			String name = pbd.getNombreProp();
			res.add(name);
		}
		
		return res;
	}
	
	private static ArrayList<String> getRangoProperty(ArrayList l, String s){
		Iterator it = l.iterator();
		ArrayList res = new ArrayList();
		while(it.hasNext()){
			PropiedadBD pbd = (PropiedadBD) it.next();
			if(s.equals(pbd.getName())){
				res.add(new Integer (pbd.getCls()));
			}
		}
		return res;
	}
	
	private static Clase buscaClase(ArrayList<Clase> l, String nClase){
		
		Clase res = null;
		Iterator itAux2 = l.iterator();
		boolean enc = false;
		while(itAux2.hasNext() && !enc){
			Clase clAux = (Clase) itAux2.next();
			if(nClase.equals(clAux.getName())){
				enc = true;
				res = clAux;
			}
		}
		return res;
	}
	
	private static Clase buscaClaseConIdto(ArrayList<Clase> l, int idClase){
		//System.out.println("ENTRO CON: "+l);
		Clase res = null;
		Iterator itAux2 = l.iterator();
		boolean enc = false;
		while(itAux2.hasNext() && !enc){
			Clase clAux = (Clase) itAux2.next();
			//System.out.println(clAux.getIdto()+","+idClase);
			if(idClase == clAux.getIdto()){
				enc = true;
				res = clAux;
			}
		}
		return res;
	}
	
	private static ArrayList<String> listaNombres(ArrayList l){
		ArrayList res = new ArrayList();
		Iterator it = l.iterator();
		while(it.hasNext()){
			Clase clbd = (Clase) it.next();
			String cadena = clbd.getName();
			res.add(cadena);
		}
		return res;
	}
	
	private static ArrayList compruebaRestriccionesCorrectas(ArrayList clases, ArrayList atrib){
		
		ArrayList res = new ArrayList();
		Iterator itAtrib = atrib.iterator();
		while(itAtrib.hasNext()){
			PropiedadAtrib pa = (PropiedadAtrib) itAtrib.next();
			ArrayList restricciones = pa.getRestricciones();
			String nombre = pa.getNombreProp();
			Iterator itClases = clases.iterator();
			while(itClases.hasNext()){
				PropiedadClase pc = (PropiedadClase) itClases.next();
				if(nombre.equals(pc.getNombreProp()) && pc.getRango().size()>0){
					ArrayList rango = pc.getRango();
					if(!rango.containsAll(restricciones) &&  !rango.contains(Constants.CLS_THING)){
						res.add(pa.getClaseCont());
						System.out.println("Error: el rango de la propiedad "+nombre+ " en la clase "+pa.getClaseCont()+" esta mal definido. No es restrictivo.");
					}
						
					else
						;
						System.out.println("El rango de la propiedad "+nombre+ " en la clase "+pa.getClaseCont()+ " será aceptado");
				}
			}
		}
		return res;
	}
	
private static ArrayList compruebaEnumeradosCorrectos(ArrayList clases, ArrayList atrib, ArrayList<Individuo> individuos){
		
		ArrayList res = new ArrayList();
		Iterator itAtrib = atrib.iterator();
		while(itAtrib.hasNext()){
			PropiedadAtrib pa = (PropiedadAtrib) itAtrib.next();
			if(pa.getEnumerados().size()>0){
				Iterator itEnum = pa.getEnumerados().iterator();
				while(itEnum.hasNext()){
					String sEnum = (String) itEnum.next();
					Individuo ind = buscaIndividuo(individuos, sEnum);
					if(ind==null){
						System.out.println("Error: El individuo "+sEnum+" no se encuentra");
					}
					
					else{
						Iterator itClases = clases.iterator();
						while(itClases.hasNext()){
							PropiedadClase pc = (PropiedadClase) itClases.next();
				
							if(pa.getNombreProp().equals(pc.getNombreProp()) && pc.getEnumerados().size()>0){
								ArrayList enumerados = pc.getEnumerados();
								if(!enumerados.containsAll(pa.getEnumerados())){
									System.out.println("Error: En la clase "+pa.getClaseCont()+" no restringe el enumerado de la propiedad "+pa.getNombreProp());
									res.add(pa.getClaseCont());
								}
								
							}
							else if(pa.getNombreProp().equals(pc.getNombreProp()) && pc.getRango().size()>0){
								ArrayList rango = pc.getRango();
								if(rango.contains(Constants.IDTO_THING))
									if(!rango.contains(ind.getTipo())){
										System.out.println("Error: el individuo "+sEnum+" no pertenece a ninguna de las clases del rango "+pc.getRango());
										res.add(pa.getClaseCont());
								}
							}
						}
					
					}
				}
			}
		}
		return res;
	}

	public static ArrayList<String> buscaRelaciones(ArrayList<RolAtrib> lr, String nameRol){
		Iterator it = lr.iterator();
		ArrayList res = new ArrayList();
		while(it.hasNext()){
			RolAtrib r = (RolAtrib) it.next();
			if(nameRol.equals(r.getNombreRol()))
				res.add(r.getRelacionCont());
		}
		return res;
	}
	
	public static Individuo buscaIndividuo(ArrayList<Individuo> l, String name){
		
		Individuo res = null;
		Iterator it = l.iterator();
		boolean enc = false;
		while(it.hasNext() && !enc){
			Individuo i = (Individuo) it.next();
			if(name.equals(i.getName())){
				enc = true;
				res = i;
			}
		}
		
		return res;
	}
	
	public static ArrayList getRolesFromRelacion(String rel, ArrayList <RolAtrib> rols){
		Iterator it = rols.iterator();
		ArrayList<RolAtrib> res = new ArrayList();
		while(it.hasNext()){
			RolAtrib rol = (RolAtrib) it.next();
			if(rol.getRelacionCont().equals(rel))
				res.add(rol);
		}
		
		return res;
	}
	
	public static ArrayList<Instance> trataPeers(ArrayList<RolAtrib> lr, ArrayList<Clase> lc){
		ArrayList<Instance> instances = new ArrayList();
		Iterator it = lr.iterator();
		InsercionInstance ii = new InsercionInstance();
		if(lr.size()>0){
			String rel = (String) lr.get(0).getRelacionCont();
			int idrel = buscaIdto(lc, rel);
			
			if(lr.size()==2){
				String r1 = (String) lr.get(0).getNombreRol();
				String r2 = (String) lr.get(1).getNombreRol();
				
				
				int rol1 = buscaIdto(lc, r1);
				int rol2 = buscaIdto(lc, r2);
				
				
				instances.add(ii.createInstancePeer(rol1, rol2, idrel));
				instances.add(ii.createInstancePeer(rol2, rol1, idrel));
				
				
			}
			
			else if(lr.size()>0){
			
				while(it.hasNext()){
					RolAtrib rol = (RolAtrib) it.next();
					if(rol.getPeer() == null)
						System.out.println("Error: El peer del rol "+rol.getNombreRol()+" de la relacion "+rol.getRelacionCont()+" no se ha indicado y es obligatorio. Revíselo");
					else{
						String r1 = rol.getNombreRol();
						String r2 = rol.getPeer();
						
						int rol1 = buscaIdto(lc, r1);
						int rol2 = buscaIdto(lc, r2);
						
						
						instances.add(ii.createInstancePeer(rol1, rol2, idrel));
						
					}
				}
			}
		}
		return instances;
	}
				
		
	
}



