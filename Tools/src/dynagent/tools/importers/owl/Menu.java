package dynagent.tools.importers.owl;

public class Menu {
/*	HashMap<String, Integer> hmIDClases = new HashMap();

	HashMap<String, Integer> hmIDPropiedades = new HashMap();

	HashMap<Integer, String> hmNameClases = new HashMap();

	HashMap<Integer, String> hmNamePropiedades = new HashMap();

	HashMap<Integer, Category> hmCategoriasOfidPropiedades = new HashMap();

	HashMap<String, Category> hmCategoriasPropiedades = new HashMap();
	
	public String getIDClase(String abrev) {
		Integer idto = null;
		String name;
		boolean encontradoid = false;
		do {

			name = Auxiliar.leeTexto(abrev);
			if (name != null) {
				encontradoid = this.getHmIDClases().containsKey(name);
			}
		} while (name != null && !name.equals("cancel") && !encontradoid);

		if (encontradoid) {
			idto = this.getHmIDClases().get(name);
			return idto.toString();
		} else if (name != null && name.equals("cancel")) {
			return "cancel";
		} else {
			return null;
		}

	}

	public String getIDPropiedad(String abrev) {
		String name;
		Integer idProp = null;
		boolean encontradoid = false;
		do {
			name = Auxiliar.leeTexto(abrev);
			if (name != null) {
				encontradoid = this.getHmIDPropiedades().containsKey(name);
			}
		} while (name != null && !name.equals("cancel") && !encontradoid);

		if (encontradoid) {
			idProp = this.getHmIDPropiedades().get(name);
			return idProp.toString();
		} else if (name != null && name.equals("cancel")) {
			return "cancel";
		} else {
			return null;
		}

	}
	
	public static void insertRulerFile(){
		StringBuffer fileContentBuff = new StringBuffer("");
		FactoryConnectionDB factConnDB=DAOManager.getFactConnDB();
		//E:\\zamoraRules.drl"
		String rutafile="E:\\DESARROLLO\\Workspace\\JaZamora\\RuleEngine\\src\\dynagent\\ruleengine\\src\\ruler\\JBossQueries.drl";
		 try {
			 
			 
			BufferedReader in = new BufferedReader(new FileReader(new File(rutafile)));
			String buff="";
			while(buff!= null){
				fileContentBuff.append(buff +"\n\r ");
				buff=in.readLine();
			}
			String fileContent = fileContentBuff.toString();
			String sql = "delete from RulerFile\n\r " +
					"INSERT INTO RULERFILE VALUES ('" + fileContent + "','"+fileContent+"')";;
			System.out.println("  SQL=\n\n\n"+sql);
			System.out.println("\n\n\n END SQL");
			Statement st = null;
			ConnectionDB con = null;
			try {
				con = factConnDB.createConnection(false);
				st = con.getBusinessConn().createStatement();
				st.executeUpdate(sql);
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					in.close();
					if (st != null)
						st.close();
					if (con!=null)
						con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void loadIDClases() throws SQLException, NamingException {

		IDAO idao = DAOManager.getInstance().getDAO("Clases");

		idao.open();

		TClaseDAO claseDao = (TClaseDAO) idao.getDAO();
		//
		// for(int i=0;i<claseDao.getAll().size();i++){ TClase
		// tclase=(TClase)claseDao.getAll().get(i);
		// this.getHmIDClases().put(tclase.getName(), tclase.getIDTO());
		// this.getHmNameClases().put(tclase.getIDTO(), tclase.getName());
		//  }
		//
		// (H)
		this.hmIDClases = claseDao.getAllHmIDClases();
		this.hmNameClases = claseDao.getAllHmNameClases();
		idao.close();

	}

	public void loadIDPropiedades() throws SQLException, NamingException {
		IDAO idao = DAOManager.getInstance().getDAO("properties");
		Properties tProperty;
		idao.open();
		PropertiesDAO propertyDao = (PropertiesDAO) idao.getDAO();
		for (int i = 0; i < propertyDao.getAll().size(); i++) {
			tProperty = (Properties) propertyDao.getAll().get(i);
			String nameProp = tProperty.getNAME();
			Integer prop = tProperty.getPROP();
			this.getHmIDPropiedades().put(nameProp, prop);
			this.getHmNamePropiedades().put(prop, nameProp);
			// cargamos las categorias
			Integer cat = tProperty.getCAT();
			Category category = new Category(cat);
			this.getHmCategoriasOfidPropiedades().put(prop, category);
			this.getHmCategoriasPropiedades().put(nameProp, category);
		}
		idao.close();
	}

	public String getClassNameFromID(String idclase) {
		if (idclase != null
				&& this.hmNameClases.containsKey(Integer.parseInt(idclase))) {
			return (this.hmNameClases.get(Integer.parseInt(idclase)));
		} else {

			return null;
		}
	}

	public String getPropertyNameFromID(String idprop) {
		if (idprop != null
				&& this.hmNamePropiedades.containsKey(Integer.parseInt(idprop))) {
			return (this.hmNamePropiedades.get(Integer.parseInt(idprop)));
		} else {

			return null;
		}
	}

	public Instance traslateInstanceToNames(Instance ins) {
		Instance insCod = new Instance();
		insCod.setIDO(this.getClassNameFromID(ins.getIDO()));
		insCod.setIDTO(this.getClassNameFromID(ins.getIDTO()));
		insCod.setNAME(ins.getNAME());
		insCod.setOP(ins.getOP());
		insCod.setPROPERTY(this.getPropertyNameFromID(ins.getPROPERTY()));
		insCod.setQMAX(ins.getQMAX());
		insCod.setQMIN(ins.getQMIN());
		insCod.setVALUECLS(this.getClassNameFromID(ins.getVALUECLS()));
		// Value puede ser o un data value, un individuo, o una clase
		if (ins.getVALUE() != null
				&& this.getHmNameClases().containsKey(ins.getVALUE())) {// es un
																		// individuo
																		// o una
																		// clase,
																		// obtenemos
																		// su
																		// nombre
			String value = ins.getVALUE();
			insCod.setVALUE(this.getHmNameClases().get(ins.getVALUE()));
		} else {
			insCod.setVALUE(ins.getVALUE());// si no pasamos su valor tal cual
		}
		return insCod;
	}

	public void mostrarClases() throws SQLException, NamingException {
		System.out.println("......CLASES:");
		this.loadIDClases();
		System.out.println(this.getHmNameClases().toString() + "\n");
		System.out.println(this.getHmIDClases().toString());
		System.out.println(".........................");
	}

	public void mostrarPropiedades() throws SQLException, NamingException {
		System.out.println("......PROPIEDADES:");
		this.loadIDPropiedades();
		System.out.println(this.getHmNamePropiedades().toString() + "\n");
		System.out.println(this.getHmIDPropiedades().toString());
		System.out.println("........................ :");
	}

	public void mostrarCategorias() throws SQLException, NamingException {
		System.out.println("......CATEGORIAS:");
		System.out.println(Auxiliar.hashMapToString(this
				.getHmCategoriasPropiedades(), "propiedad", "categoria"));
		System.out.println("..................................");
		// ¿Cambios?
		String modificar = Auxiliar
				.leeTexto("¿.....Desea modificar la categoría de alguna propiedad (S/N)?");
		if (modificar.equals("S")) {
			String sIdProp = this
					.getIDPropiedad("Introduzca el nombre de la propiedad:");
			String namePropiedad = null;
			do {
				if (sIdProp.equals("cancel") || sIdProp == null) {
					System.out.println("....modificación cancelada");
				} else {
					namePropiedad = this.getHmNamePropiedades().get(
							new Integer(sIdProp));
					Category category = this.getHmCategoriasPropiedades().get(
							namePropiedad);
					System.out.println("    -" + namePropiedad
							+ "  tiene category=" + category.toString());
					Category newCategory = category.modifyCategory(category);
					System.out.println("       nueva categoría="
							+ newCategory.toString());
					String respuesta = Auxiliar
							.leeTexto("¿.....Desea asignarle a "
									+ namePropiedad
									+ "  la nueva categoría(S/N)?");
					if (respuesta.equals("S")) {
						IDAO idao = DAOManager.getInstance().getDAO(
								"properties");
						Properties tProperty;
						idao.open();
						PropertiesDAO propertyDao = (PropertiesDAO) idao
								.getDAO();
						tProperty = propertyDao
								.getPropertyByName(namePropiedad);
						Properties pp = new Properties();
						if (tProperty == null) { // No se encuentra la
													// propiedad en la tabla
													// Properties.
							System.out
									.println("     warning: no se encuentra la propiedad"
											+ namePropiedad
											+ "   en Menu.mostrarCategorias()");
						} else { // La property ya está en la tabla,
									// obtenemos su idProp y actualizamos sus
									// campos con la información del modelo
							pp.setCAT(newCategory.getCat());
							pp.setNAME(tProperty.getNAME());
							pp.setPROP(tProperty.getPROP());
							propertyDao.set(pp);
							// cargamos de nuevo los hashmap de propiedades pq
							// ha habido cambios en la tabla properties
							this.loadIDPropiedades();
						}
						// idao.close();
					}
				}
			} while (!sIdProp.equals("cancel")
					&& sIdProp != null
					&& !this.getHmCategoriasPropiedades().containsKey(
							namePropiedad));
		}
	}

	public LinkedList<Instance> loadInstances() throws SQLException, NamingException {
		LinkedList<Instance> lInstances = new LinkedList<Instance>();
		IDAO idao = DAOManager.getInstance().getDAO("instances");
		idao.open();
		InstanceDAO insdao = (InstanceDAO) idao.getDAO();
		System.out.println("..............INSTANCES:");
		for (int i = 0; i < insdao.getAll().size(); i++) {
			Instance ins = (Instance) insdao.getAll().get(i);
			lInstances.add(ins);
			// System.out.println(" "+ins.toStringNotNull());
			Instance insNames = this.traslateInstanceToNames(ins);
			System.out.println(insNames.toStringNotNull());
		}
		idao.close();
		System.out.println("........................");
		return lInstances;
	}

	public Instance readInstance() {
		Instance insNumer = new Instance();
		String leido = null;
		do {
			System.out
					.println("  Introduzca los valores (retorno de carro para nulo)");

			leido = this.getIDClase("IDTO?");
			if (leido == null || !leido.equals("cancel")) {
				insNumer.setIDTO(leido);
			}
			if (leido == null || !leido.equals("cancel")) {
				leido = this.getIDPropiedad("PROPERTY?");
				insNumer.setPROPERTY(leido);
			}
			if (leido == null || !leido.equals("cancel")) {
				leido = Auxiliar.leeTexto("IDO?");
				// leido=this.getIDClase("IDO?");
				insNumer.setIDO(leido);
			}
		
			if (leido == null || !leido.equals("cancel")) {
				leido = Auxiliar.leeTexto("VALUE?");
				insNumer.setVALUE(leido);
			}
			if (leido == null || !leido.equals("cancel")) {
				leido = this.getIDClase("VALUECLS?");
				insNumer.setVALUECLS(leido);
			}
			
			
			
			if (leido == null || !leido.equals("cancel")) {
				System.out.println("QMIN?");
				Float q = Auxiliar.getFloatNumber();
				if (q != null) {
					leido = String.valueOf(q);
					insNumer.setQMIN(leido);
				}
			}
			if (leido == null || !leido.equals("cancel")) {
				System.out.println("QMAX?");
				Float q = Auxiliar.getFloatNumber();
				if (q != null) {
					leido = String.valueOf(q);
					insNumer.setQMAX(leido);
				}
			}
			if (leido == null || !leido.equals("cancel")) {
				leido = Auxiliar.leeTexto("OP?");
				insNumer.setOP(leido);
			}
			if (leido == null || !leido.equals("cancel")) {
				leido = Auxiliar.leeTexto("NAME?");
				insNumer.setNAME(leido);
			}

			if (leido == null || !leido.equals("cancel")) {
				System.out.println("La instancia leida es:\n"
						+ this.traslateInstanceToNames(insNumer));
				leido = Auxiliar
						.leeTexto(" Si es corecta pulse 'S'. Si desea cancelar pulse 'cancel'  ");
			}
		}

		while (leido == null && !leido.equals("cancel")); // )||!leido.equals("S"));
		// Si se cancela la lectura, se devolverá null
		if (leido.equals("cancel")) {
			System.out.println("......Operación cancelada");
			return null;
		} else {
			return insNumer;
		}
	}

	public void insertInstances() throws SQLException, NamingException {

		IDAO idao = DAOManager.getInstance().getDAO("instances");
		idao.open();
		InstanceDAO insdao = (InstanceDAO) idao.getDAO();
		String respuesta;
		Instance insNumer = null;
		do {
			insNumer = this.readInstance();
			if (insNumer != null) {
				respuesta = Auxiliar
						.leeTexto("\n  ¿Desea insertar la instancia (S/N)?");
				if (respuesta.equals("S") || respuesta.equals("s")) {
					insdao.insert(insNumer);
					System.out.println("  instancia insertada");
				}
			}
			respuesta = Auxiliar
					.leeTexto("\n  ¿Desea añadir una nueva instance (S/N)?");
		} while (respuesta.equals("S") && insNumer != null);
		idao.close();
	}

	

	public char dameOpcion() {
		System.out
				.println("\n\n===========================================================================================");
		System.out
				.println("\n           DYNAGENT  BUSINESS MODEL   \n    @autor: Jose A. Zamora Aguilera  - jazamora@ugr.es");
		System.out.println("\n OPCIONES:");
		System.out.println("1.-Importar modelo desde fichero OWL");
		
		System.out.println("2.-Importar fichero reglas");
		//System.out.println("2.-Ver clases");
		System.out.println("3.-Ver propiedades");
		System.out.println("4.-Ver información modelo");
		System.out
				.println("5.-Ampliar información modelo con nuevo registro instance");
		System.out
				.println("6.-Consultar-Modificar Categorías de las propiedades");
		System.out.println("7.-Cargar motor a partir de las tablas del modelo");
		System.out.println("8.-Ejecutar pruebas cargando motor desde BBDD");
		System.out.println("9.-Ejecutar pruebas cargando motor desde XML");
		//System.out.println("I.-Pruebas de coherencia del modelo importado ");
		System.out.println("J.-Crear Diseños de Reports");
		System.out.println("R.-Importar todos los Reports ");
		System.out.println("D.-Borrar Report ");
		System.out.println("G.-Recoger Diseño");
		System.out.println("I.-Importar un Report concreto");
		System.out.println("\nS.-SALIR");
		System.out
				.println("\n===========================================================================================");
		String texto = Auxiliar.leeTexto("SELECCIONE UNA OPCIÓN");

		char opcion = texto.charAt(0);
		return opcion;
	}
	
	
	public static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor){
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
		return fcdb;
	}

	public static void main(String args[]) throws IOException, JDOMException, ParseException,  InterruptedException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		ConceptLogger.getLogger().cleanFile();
		//ConceptLogger.getLogger().writeln("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip="192.168.1.3";
		String path = "E:/DESARROLLO/filesReport/";
		String pathIreport="";
		int nbusiness = 6;
		String suserRol="null";
		String user="admin";
		String resp;
		String susertask="null";
		String snbusiness=String.valueOf(nbusiness);
		String gestor="SQLServer";
		//String snbusiness = new Integer(nbusiness).toString();
		//resp=Auxiliar.leeTexto("\n  IP="+ip+"   \n Desea que numero_empresa="+snbusiness+"?");
		//if(!resp.equalsIgnoreCase("S")&&!resp.equalsIgnoreCase("SI")){
			
		//}
		resp=Auxiliar.leeTexto("POR DEFECTO nbusiness="+nbusiness+"   userRol="+suserRol+"  user="+user+"  ¿DESEA MODIFICARLOS(S/N)?");
		if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
			user=Auxiliar.leeTexto("Introduzca el nombre de usuario");
			suserRol=Auxiliar.leeTexto("Introduzca el userRol");
			susertask=Auxiliar.leeTexto("Introduzca la usertask");
			gestor=Auxiliar.leeTexto("Introduzca el gestor de base de datos");
			
			do {
				snbusiness = Auxiliar.leeTexto("Introduzca el número de empresa con el que desea trabajar");
			} while (!Auxiliar.hasIntValue(snbusiness));
			nbusiness = new Integer(snbusiness);
			//ip = Auxiliar.leeTexto("Introduzca la IP de la Base de datos: ");
		
		}
		
		Integer userRol,usertask;
		if(suserRol.equalsIgnoreCase("null")){
			userRol=null;
		}
		else
			userRol=Integer.valueOf(suserRol);
		
		if(susertask.equalsIgnoreCase("null")){
			usertask=null;
		}
		else
			usertask=Integer.valueOf(susertask);
		
		
		
		FactoryConnectionDB fcdb=Menu.setConnection(snbusiness, ip, gestor);
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		//System.out.println("---------> " + DAOManager.getInstance().getPool());
		char opcion;
		Menu menu = new Menu();
		// Cargamos en memoria el objeto hashMap con las equivalencias
		// nombres-->ids
		menu.loadIDClases();
		menu.loadIDPropiedades();
		System.out.println("INFO:   El numero de empresa es=" + snbusiness);
		do {
			opcion = menu.dameOpcion();
			switch (opcion) {
			case '1': // 
				OWLParser.ImportarOWL("E:/DESARROLLO/filesOWL/","ERP.owl",fcdb, idtosReplica, replicaEnTienda);
				
				break;
			case '2': // 
				//menu.mostrarClases();
				menu.insertRulerFile();
				break;
			case '3':
				menu.mostrarPropiedades();
				break;
			case '4':
				menu.loadInstances();
				break;
			case '5': // Añadir nuevo registro
				menu.insertInstances();
				break;
			case '6': // Consultar-Modificar Categorias de las propiedades.
				menu.mostrarCategorias();
				break;

//			case '7':
//				TestDBMotor.cargaMotor(nbusiness,userRol,user,usertask);
//				break;
//			case '8':
//				TestDBMotor.test(nbusiness,userRol,user,usertask);
//				break;
//			case '9':
//				TestXMLMotor.test(nbusiness,userRol,user,usertask);
//				break;
//=======
//			case '7':
//				TestDBMotor.cargaMotor(nbusiness,userRol,user,usertask);
//				break;
//			case '8':
//				TestDBMotor.test(nbusiness,userRol,user,usertask);
//				break;
//			case '9':
//				TestXMLMotor.test(nbusiness,userRol,user,usertask);
//				break;

			case 'S':
				System.out.println("Salir");

				break;
			case 'R':
				

			//	REPORTParser.importReports(null, path,fcdb);

				break;
			case 'I':
				String queryR=Auxiliar.leeTexto("Introduzca el nombre de la query");
				queryR=queryR.toLowerCase();

				//REPORTParser.importReports(queryR);

				
				break;
			case 'J':
				//MakeJrxmls.createDesign(nbusiness,fcdb);

				break;
			case 'D':
				String query=Auxiliar.leeTexto("Introduzca el nombre de la query");
				query=query.toLowerCase();
				REPORTParser.delete(query);
			case 'G':
				
				String queryG=Auxiliar.leeTexto("Introduzca el nombre de la query");
				queryG=queryG.toLowerCase();
				REPORTParser.getDesign(path, queryG);
				break;
//			case 'I':
//			
//				TestDBMotor.testCoherenciaModeloImportado( nbusiness,userRol,user,usertask);
//				break;
			default:
				System.out.println("La opcion es incorreta");
				break;
			}
		} while (opcion != 'S');

		System.out.println("*****bye******");
		//ConceptLogger.getLogger().writeln(
				//"Fin de sesión: " + Calendar.getInstance().getTime());
	}
	
	
	
	

	public HashMap<String, Integer> getHmIDClases() {
		return hmIDClases;
	}

	public void setHmIDClases(HashMap<String, Integer> hmIDClases) {
		this.hmIDClases = hmIDClases;
	}

	public HashMap<String, Integer> getHmIDPropiedades() {
		return hmIDPropiedades;
	}

	public void setHmIDPropiedades(HashMap<String, Integer> hmIDPropiedades) {
		this.hmIDPropiedades = hmIDPropiedades;
	}

	public HashMap<Integer, String> getHmNameClases() {
		return hmNameClases;
	}

	public void setHmNameClases(HashMap<Integer, String> hmNameClases) {
		this.hmNameClases = hmNameClases;
	}

	public HashMap<Integer, String> getHmNamePropiedades() {
		return hmNamePropiedades;
	}

	public void setHmNamePropiedades(HashMap<Integer, String> hmNamePropiedades) {
		this.hmNamePropiedades = hmNamePropiedades;
	}

	public HashMap<Integer, Category> getHmCategoriasOfidPropiedades() {
		return hmCategoriasOfidPropiedades;
	}

	public void setHmCategoriasOfidPropiedades(
			HashMap<Integer, Category> hmCategoriasOfidPropiedades) {
		this.hmCategoriasOfidPropiedades = hmCategoriasOfidPropiedades;
	}

	public HashMap<String, Category> getHmCategoriasPropiedades() {
		return hmCategoriasPropiedades;
	}

	public void setHmCategoriasPropiedades(
			HashMap<String, Category> hmCategoriasPropiedades) {
		this.hmCategoriasPropiedades = hmCategoriasPropiedades;
	}
*/
	
}