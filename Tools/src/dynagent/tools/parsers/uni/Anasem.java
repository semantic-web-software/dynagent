// $ANTLR : "E:/DESARROLLO/Workspace/Alvarez/RuleEngine/src/dynagent/lenguajeUniv/parser/Anasem.g" -> "AnaSem.java"$
 
	package dynagent.tools.parsers.uni;
	import java.util.*; 
	import antlr.collections.ASTEnumeration;
	
	import dynagent.ruleengine.*;
import dynagent.tools.parsers.uni.auxiliar.ListaPropiedad;
import dynagent.tools.parsers.uni.auxiliar.ListaRol;
import dynagent.tools.parsers.uni.auxiliar.ListaRolAtrib;
import dynagent.tools.parsers.uni.auxiliar.ListaSubClase;
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoFicheroAdvanced;
import dynagent.tools.parsers.uni.auxiliar.PropiedadAtrib;
import dynagent.tools.parsers.uni.auxiliar.PropiedadClase;
import dynagent.tools.parsers.uni.auxiliar.Rol;
import dynagent.tools.parsers.uni.auxiliar.RolAtrib;
import dynagent.tools.parsers.uni.auxiliar.SubClase;


import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


public class Anasem extends antlr.TreeParser       implements AnasemTokenTypes
 {

	ListaSubClase listaSc = new ListaSubClase();
	ListaPropiedad listaP = new ListaPropiedad();
	ListaRolAtrib listaR = new ListaRolAtrib();
	ListaRol listaRol = new ListaRol();
	ArrayList listaPropCl = new ArrayList();
	ArrayList listaPropAt = new ArrayList();
	
	void compruebaRestriccionesCorrectas(ArrayList clases, ArrayList atrib){
		Iterator itAtrib = atrib.iterator();
		while(itAtrib.hasNext()){
			PropiedadAtrib pa = (PropiedadAtrib) itAtrib.next();
			ArrayList restricciones = pa.getRestricciones();
			String nombre = pa.getNombreProp();
			Iterator itClases = clases.iterator();
			while(itClases.hasNext()){
				PropiedadClase pc = (PropiedadClase) itClases.next();
				if(nombre.equals(pc.getNombreProp())){
					ArrayList rango = pc.getRango();
					if(!rango.containsAll(restricciones))
						System.out.println("Error, el rango de la propiedad "+nombre+ " en la clase "+pa.getClaseCont()+" esta mal definido. No es restrictivo.");
					else
						System.out.println("El rango de la propiedad "+nombre+ " en la clase "+pa.getClaseCont()+ " será aceptado");
				}
			}
		}
	}
	
	ProcesamientoFicheroAdvanced getTablasDatos(){
		
		ProcesamientoFicheroAdvanced pfa = new ProcesamientoFicheroAdvanced();
		pfa.listaPropiedad = listaP.getListaPropiedades();
		pfa.listaRol = listaRol.getListaRoles();
		pfa.listaRolAtrib = listaR.getListaRolAtrib();
		pfa.listaSubclase = listaSc.getListaSubclases();
		pfa.listaPropCl = listaPropCl;
		pfa.listaPropAt = listaPropAt;
		return pfa;
	}
			
	void informaClasesEnRelaciones(){
		
		Iterator it = (listaR.getListaRolAtrib()).iterator();
		while(it.hasNext()){
			RolAtrib rA = (RolAtrib) it.next();
			Iterator it2 = (listaRol.getListaRoles()).iterator();
			while(it2.hasNext()){
				Rol rol = (Rol) it2.next();
				if(rol.getName().equals(rA.getNombreRol())){
					System.out.println("El rol " + rol.getName() + " de la relacion " + rA.getRelacionCont() + " tiene como clase(s) " + rol.getListaJuegos());
				}
			}
		}
	}
public Anasem() {
	tokenNames = _tokenNames;
}

	public final ProcesamientoFicheroAdvanced  entrada(AST _t) throws RecognitionException {
		ProcesamientoFicheroAdvanced pfa = new ProcesamientoFicheroAdvanced();
		
		AST entrada_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST entrada_AST = null;
		
		try {      // for error handling
			AST __t315 = _t;
			AST tmp1_AST = null;
			AST tmp1_AST_in = null;
			tmp1_AST = astFactory.create((AST)_t);
			tmp1_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp1_AST);
			ASTPair __currentAST315 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DEFINICIONES);
			_t = _t.getFirstChild();
			{
			_loop317:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					definicion(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop317;
				}
				
			} while (true);
			}
			currentAST = __currentAST315;
			_t = __t315;
			_t = _t.getNextSibling();
			
				 pfa = getTablasDatos();
				// System.out.println(listaPropAt);
				// System.out.println(listaPropCl);
				compruebaRestriccionesCorrectas(listaPropCl, listaPropAt);
				 //System.out.println(listaP);
				 //System.out.println(listaRol);
				 //informaClasesEnRelaciones();
				
			entrada_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = entrada_AST;
		_retTree = _t;
		return pfa;
	}
	
	public final void definicion(AST _t) throws RecognitionException {
		
		AST definicion_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST definicion_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CLASE:
			{
				clase(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				definicion_AST = (AST)currentAST.root;
				break;
			}
			case RELACION:
			{
				relacion(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				definicion_AST = (AST)currentAST.root;
				break;
			}
			case ROL:
			{
				claseRol(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				definicion_AST = (AST)currentAST.root;
				break;
			}
			case PROPIEDAD:
			{
				claseProp(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				definicion_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = definicion_AST;
		_retTree = _t;
	}
	
	public final void clase(AST _t) throws RecognitionException {
		
		AST clase_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST clase_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			AST __t320 = _t;
			AST tmp2_AST = null;
			AST tmp2_AST_in = null;
			tmp2_AST = astFactory.create((AST)_t);
			tmp2_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp2_AST);
			ASTPair __currentAST320 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CLASE);
			_t = _t.getFirstChild();
			i = (AST)_t;
			AST i_AST_in = null;
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(_t,IDENT);
			_t = _t.getNextSibling();
			String s = i.getText();
									 SubClase sc = new SubClase();
									 sc.setName(s);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SUBCLASE:
			{
				subclase(_t,sc);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case PROPIEDADES:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			String s2 = i.getText();
									
			propiedades(_t,s2);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST320;
			_t = __t320;
			_t = _t.getNextSibling();
			clase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = clase_AST;
		_retTree = _t;
	}
	
	public final void relacion(AST _t) throws RecognitionException {
		
		AST relacion_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relacion_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			AST __t343 = _t;
			AST tmp3_AST = null;
			AST tmp3_AST_in = null;
			tmp3_AST = astFactory.create((AST)_t);
			tmp3_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST343 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RELACION);
			_t = _t.getFirstChild();
			i = (AST)_t;
			AST i_AST_in = null;
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(_t,IDENT);
			_t = _t.getNextSibling();
					  SubClase sc = new SubClase();
									  ArrayList l = sc.getListaPadres();
									  sc.setName(i.getText());
									  sc.setListaPadres(l);
									  listaSc.addSubClass(sc);
									  String a = i.getText();
							
			cuerpoRelacion(_t,a);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST343;
			_t = __t343;
			_t = _t.getNextSibling();
			relacion_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = relacion_AST;
		_retTree = _t;
	}
	
	public final void claseRol(AST _t) throws RecognitionException {
		
		AST claseRol_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST claseRol_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			AST __t360 = _t;
			AST tmp4_AST = null;
			AST tmp4_AST_in = null;
			tmp4_AST = astFactory.create((AST)_t);
			tmp4_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST360 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ROL);
			_t = _t.getFirstChild();
			i = (AST)_t;
			AST i_AST_in = null;
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(_t,IDENT);
			_t = _t.getNextSibling();
			String s = i.getText();
									  Rol r = new Rol();
									  r.setName(s);
									  SubClase sc = new SubClase();
									  sc.setName(i.getText());
									  ArrayList l = sc.getListaPadres();
									  sc.setListaPadres(l);
									  listaSc.addSubClass(sc);
									  String a = i.getText();
			juego(_t,r);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			propiedades(_t,a);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST360;
			_t = __t360;
			_t = _t.getNextSibling();
			listaRol.addRol(r);
			claseRol_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = claseRol_AST;
		_retTree = _t;
	}
	
	public final void claseProp(AST _t) throws RecognitionException {
		
		AST claseProp_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST claseProp_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			AST __t369 = _t;
			AST tmp5_AST = null;
			AST tmp5_AST_in = null;
			tmp5_AST = astFactory.create((AST)_t);
			tmp5_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST369 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PROPIEDAD);
			_t = _t.getFirstChild();
			PropiedadClase pb = new PropiedadClase();
			i = (AST)_t;
			AST i_AST_in = null;
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(_t,IDENT);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CARDINALIDAD:
			{
				cardinalidad_prop(_t,pb);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RANGO:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			pb.setNombreProp(i.getText());
			rango(_t,pb);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EXPREG:
			{
				expreg(_t,pb);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			case CATEGORIA:
			case LONGITUD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CATEGORIA:
			{
				categoria(_t,pb);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			case LONGITUD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LONGITUD:
			{
				longitud(_t,pb);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST369;
			_t = __t369;
			_t = _t.getNextSibling();
			listaPropCl.add(pb);
			claseProp_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = claseProp_AST;
		_retTree = _t;
	}
	
	public final void subclase(AST _t,
		SubClase sc
	) throws RecognitionException {
		
		AST subclase_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST subclase_AST = null;
		
		try {      // for error handling
			AST __t323 = _t;
			AST tmp6_AST = null;
			AST tmp6_AST_in = null;
			tmp6_AST = astFactory.create((AST)_t);
			tmp6_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST323 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,SUBCLASE);
			_t = _t.getFirstChild();
			restoSubclase(_t,sc);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			listaSc.addSubClass(sc);
			currentAST = __currentAST323;
			_t = __t323;
			_t = _t.getNextSibling();
			subclase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = subclase_AST;
		_retTree = _t;
	}
	
	public final void propiedades(AST _t,
		String s
	) throws RecognitionException {
		
		AST propiedades_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST propiedades_AST = null;
		
		try {      // for error handling
			AST __t328 = _t;
			AST tmp7_AST = null;
			AST tmp7_AST_in = null;
			tmp7_AST = astFactory.create((AST)_t);
			tmp7_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST328 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PROPIEDADES);
			_t = _t.getFirstChild();
			{
			_loop330:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PROP_EN_CLASE||_t.getType()==PROPIEDAD_COMPLEJA)) {
					tipoPropiedad(_t,s);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop330;
				}
				
			} while (true);
			}
			currentAST = __currentAST328;
			_t = __t328;
			_t = _t.getNextSibling();
			propiedades_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = propiedades_AST;
		_retTree = _t;
	}
	
	public final void restoSubclase(AST _t,
		SubClase sc
	) throws RecognitionException {
		
		AST restoSubclase_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST restoSubclase_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case OR:
			{
				AST __t325 = _t;
				AST tmp8_AST = null;
				AST tmp8_AST_in = null;
				tmp8_AST = astFactory.create((AST)_t);
				tmp8_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp8_AST);
				ASTPair __currentAST325 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR);
				_t = _t.getFirstChild();
				restoSubclase(_t,sc);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				restoSubclase(_t,sc);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST325;
				_t = __t325;
				_t = _t.getNextSibling();
				restoSubclase_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			{
				AST __t326 = _t;
				AST tmp9_AST = null;
				AST tmp9_AST_in = null;
				tmp9_AST = astFactory.create((AST)_t);
				tmp9_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp9_AST);
				ASTPair __currentAST326 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND);
				_t = _t.getFirstChild();
				restoSubclase(_t,sc);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				restoSubclase(_t,sc);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST326;
				_t = __t326;
				_t = _t.getNextSibling();
				restoSubclase_AST = (AST)currentAST.root;
				break;
			}
			case IDENT:
			{
				i = (AST)_t;
				AST i_AST_in = null;
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				ArrayList l = sc.getListaPadres();
									l.add(i.getText());
									sc.setListaPadres(l);
						
				restoSubclase_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = restoSubclase_AST;
		_retTree = _t;
	}
	
	public final void tipoPropiedad(AST _t,
		String s
	) throws RecognitionException {
		
		AST tipoPropiedad_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tipoPropiedad_AST = null;
		AST i = null;
		AST i_AST = null;
		AST i1 = null;
		AST i1_AST = null;
		AST i2 = null;
		AST i2_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PROP_EN_CLASE:
			{
				AST __t332 = _t;
				AST tmp10_AST = null;
				AST tmp10_AST_in = null;
				tmp10_AST = astFactory.create((AST)_t);
				tmp10_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp10_AST);
				ASTPair __currentAST332 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,PROP_EN_CLASE);
				_t = _t.getFirstChild();
				i = (AST)_t;
				AST i_AST_in = null;
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				String s2 = i.getText();
											 //Propiedad p = new Propiedad();
											 //p.setClaseCont(s);
											 //p.setNombreProp(s2);
											 PropiedadAtrib pa = new PropiedadAtrib();
											 pa.setNombreProp(i.getText());
											 pa.setClaseCont(s);
											
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case RANGE:
				{
					range(_t,pa);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case 3:
				case CARDINALIDAD:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case CARDINALIDAD:
				{
					cardinalidad(_t,pa);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				currentAST = __currentAST332;
				_t = __t332;
				_t = _t.getNextSibling();
				//listaP.addPropiedad(pa);
											listaPropAt.add(pa);
				tipoPropiedad_AST = (AST)currentAST.root;
				break;
			}
			case PROPIEDAD_COMPLEJA:
			{
				AST __t335 = _t;
				AST tmp11_AST = null;
				AST tmp11_AST_in = null;
				tmp11_AST = astFactory.create((AST)_t);
				tmp11_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp11_AST);
				ASTPair __currentAST335 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,PROPIEDAD_COMPLEJA);
				_t = _t.getFirstChild();
				AST tmp12_AST = null;
				AST tmp12_AST_in = null;
				tmp12_AST = astFactory.create((AST)_t);
				tmp12_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp12_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				i1 = (AST)_t;
				AST i1_AST_in = null;
				i1_AST = astFactory.create(i1);
				astFactory.addASTChild(currentAST, i1_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				PropiedadAtrib p = new PropiedadAtrib();
				cardinalidad(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				i2 = (AST)_t;
				AST i2_AST_in = null;
				i2_AST = astFactory.create(i2);
				astFactory.addASTChild(currentAST, i2_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				currentAST = __currentAST335;
				_t = __t335;
				_t = _t.getNextSibling();
				tipoPropiedad_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = tipoPropiedad_AST;
		_retTree = _t;
	}
	
	public final void range(AST _t,
		PropiedadAtrib p
	) throws RecognitionException {
		
		AST range_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST range_AST = null;
		
		try {      // for error handling
			AST __t337 = _t;
			AST tmp13_AST = null;
			AST tmp13_AST_in = null;
			tmp13_AST = astFactory.create((AST)_t);
			tmp13_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			ASTPair __currentAST337 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RANGE);
			_t = _t.getFirstChild();
			resto_range(_t,p);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST337;
			_t = __t337;
			_t = _t.getNextSibling();
			range_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = range_AST;
		_retTree = _t;
	}
	
	public final void cardinalidad(AST _t,
		PropiedadAtrib p
	) throws RecognitionException {
		
		AST cardinalidad_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cardinalidad_AST = null;
		AST l1 = null;
		AST l1_AST = null;
		AST l2 = null;
		AST l2_AST = null;
		AST a = null;
		AST a_AST = null;
		
		try {      // for error handling
			AST __t363 = _t;
			AST tmp14_AST = null;
			AST tmp14_AST_in = null;
			tmp14_AST = astFactory.create((AST)_t);
			tmp14_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp14_AST);
			ASTPair __currentAST363 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CARDINALIDAD);
			_t = _t.getFirstChild();
			l1 = (AST)_t;
			AST l1_AST_in = null;
			l1_AST = astFactory.create(l1);
			astFactory.addASTChild(currentAST, l1_AST);
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			Integer i1 = Integer.parseInt(l1.getText());
						 	p.setQMin(i1.intValue());
						   Integer i2 = null;	
						
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LIT_ENTERO:
			{
				l2 = (AST)_t;
				AST l2_AST_in = null;
				l2_AST = astFactory.create(l2);
				astFactory.addASTChild(currentAST, l2_AST);
				match(_t,LIT_ENTERO);
				_t = _t.getNextSibling();
				i2 = Integer.parseInt(l2.getText());
							 	p.setQMax(i2.intValue());
							
				break;
			}
			case ASTERISCO:
			{
				a = (AST)_t;
				AST a_AST_in = null;
				a_AST = astFactory.create(a);
				astFactory.addASTChild(currentAST, a_AST);
				match(_t,ASTERISCO);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST363;
			_t = __t363;
			_t = _t.getNextSibling();
			cardinalidad_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = cardinalidad_AST;
		_retTree = _t;
	}
	
	public final void resto_range(AST _t,
		PropiedadAtrib p
	) throws RecognitionException {
		
		AST resto_range_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST resto_range_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case OR:
			{
				AST __t339 = _t;
				AST tmp15_AST = null;
				AST tmp15_AST_in = null;
				tmp15_AST = astFactory.create((AST)_t);
				tmp15_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp15_AST);
				ASTPair __currentAST339 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR);
				_t = _t.getFirstChild();
				p.setOp("OR");
				resto_range(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				resto_range(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST339;
				_t = __t339;
				_t = _t.getNextSibling();
				resto_range_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			{
				AST __t340 = _t;
				AST tmp16_AST = null;
				AST tmp16_AST_in = null;
				tmp16_AST = astFactory.create((AST)_t);
				tmp16_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp16_AST);
				ASTPair __currentAST340 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND);
				_t = _t.getFirstChild();
				p.setOp("AND");
				resto_range(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				resto_range(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST340;
				_t = __t340;
				_t = _t.getNextSibling();
				resto_range_AST = (AST)currentAST.root;
				break;
			}
			case IDENT:
			{
				i = (AST)_t;
				AST i_AST_in = null;
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				p.addPropAtrib(i.getText());
				resto_range_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = resto_range_AST;
		_retTree = _t;
	}
	
	public final void tipo(AST _t) throws RecognitionException {
		
		AST tipo_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tipo_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ENTERO:
			case LOGICO:
			case FECHA:
			case CADENA:
			{
				tipoBasico(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				tipo_AST = (AST)currentAST.root;
				break;
			}
			case IDENT:
			{
				AST tmp17_AST = null;
				AST tmp17_AST_in = null;
				tmp17_AST = astFactory.create((AST)_t);
				tmp17_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp17_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				tipo_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = tipo_AST;
		_retTree = _t;
	}
	
	public final String  tipoBasico(AST _t) throws RecognitionException {
		String s="";;
		
		AST tipoBasico_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tipoBasico_AST = null;
		AST e = null;
		AST e_AST = null;
		AST l = null;
		AST l_AST = null;
		AST f = null;
		AST f_AST = null;
		AST c = null;
		AST c_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ENTERO:
			{
				e = (AST)_t;
				AST e_AST_in = null;
				e_AST = astFactory.create(e);
				astFactory.addASTChild(currentAST, e_AST);
				match(_t,ENTERO);
				_t = _t.getNextSibling();
				s = e.getText();
				tipoBasico_AST = (AST)currentAST.root;
				break;
			}
			case LOGICO:
			{
				l = (AST)_t;
				AST l_AST_in = null;
				l_AST = astFactory.create(l);
				astFactory.addASTChild(currentAST, l_AST);
				match(_t,LOGICO);
				_t = _t.getNextSibling();
				s = l.getText();
				tipoBasico_AST = (AST)currentAST.root;
				break;
			}
			case FECHA:
			{
				f = (AST)_t;
				AST f_AST_in = null;
				f_AST = astFactory.create(f);
				astFactory.addASTChild(currentAST, f_AST);
				match(_t,FECHA);
				_t = _t.getNextSibling();
				s = f.getText();
				tipoBasico_AST = (AST)currentAST.root;
				break;
			}
			case CADENA:
			{
				c = (AST)_t;
				AST c_AST_in = null;
				c_AST = astFactory.create(c);
				astFactory.addASTChild(currentAST, c_AST);
				match(_t,CADENA);
				_t = _t.getNextSibling();
				s = c.getText();
				tipoBasico_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = tipoBasico_AST;
		_retTree = _t;
		return s;
	}
	
	public final void cuerpoRelacion(AST _t,
		String a
	) throws RecognitionException {
		
		AST cuerpoRelacion_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cuerpoRelacion_AST = null;
		
		try {      // for error handling
			AST __t345 = _t;
			AST tmp18_AST = null;
			AST tmp18_AST_in = null;
			tmp18_AST = astFactory.create((AST)_t);
			tmp18_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp18_AST);
			ASTPair __currentAST345 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CUERPO_RELACION);
			_t = _t.getFirstChild();
			{
			_loop347:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ROL:
				{
					rol(_t,a);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case PROP_EN_CLASE:
				case PROPIEDAD_COMPLEJA:
				{
					tipoPropiedad(_t,a);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					break _loop347;
				}
				}
			} while (true);
			}
			currentAST = __currentAST345;
			_t = __t345;
			_t = _t.getNextSibling();
			cuerpoRelacion_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = cuerpoRelacion_AST;
		_retTree = _t;
	}
	
	public final void rol(AST _t,
		String s
	) throws RecognitionException {
		
		AST rol_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rol_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			AST __t349 = _t;
			AST tmp19_AST = null;
			AST tmp19_AST_in = null;
			tmp19_AST = astFactory.create((AST)_t);
			tmp19_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp19_AST);
			ASTPair __currentAST349 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ROL);
			_t = _t.getFirstChild();
			i = (AST)_t;
			AST i_AST_in = null;
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(_t,IDENT);
			_t = _t.getNextSibling();
			
								 RolAtrib rA = new RolAtrib();
								 rA.setNombreRol(i.getText());
								 rA.setRelacionCont(s);
								 listaR.addRolAtrib(rA);
								
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CARDINALIDAD:
			{
				cardinalidad_rol(_t,rA);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST349;
			_t = __t349;
			_t = _t.getNextSibling();
			rol_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = rol_AST;
		_retTree = _t;
	}
	
	public final void cardinalidad_rol(AST _t,
		RolAtrib r
	) throws RecognitionException {
		
		AST cardinalidad_rol_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cardinalidad_rol_AST = null;
		AST l1 = null;
		AST l1_AST = null;
		AST l2 = null;
		AST l2_AST = null;
		
		try {      // for error handling
			AST __t352 = _t;
			AST tmp20_AST = null;
			AST tmp20_AST_in = null;
			tmp20_AST = astFactory.create((AST)_t);
			tmp20_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp20_AST);
			ASTPair __currentAST352 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CARDINALIDAD);
			_t = _t.getFirstChild();
			l1 = (AST)_t;
			AST l1_AST_in = null;
			l1_AST = astFactory.create(l1);
			astFactory.addASTChild(currentAST, l1_AST);
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			r.setQMin(Integer.parseInt(l1.getText())); int max = -1;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LIT_ENTERO:
			{
				l2 = (AST)_t;
				AST l2_AST_in = null;
				l2_AST = astFactory.create(l2);
				astFactory.addASTChild(currentAST, l2_AST);
				match(_t,LIT_ENTERO);
				_t = _t.getNextSibling();
				r.setQMax(Integer.parseInt(l2.getText()));
				break;
			}
			case ASTERISCO:
			{
				AST tmp21_AST = null;
				AST tmp21_AST_in = null;
				tmp21_AST = astFactory.create((AST)_t);
				tmp21_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp21_AST);
				match(_t,ASTERISCO);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST352;
			_t = __t352;
			_t = _t.getNextSibling();
			cardinalidad_rol_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = cardinalidad_rol_AST;
		_retTree = _t;
	}
	
	public final void juego(AST _t,
		Rol r
	) throws RecognitionException {
		
		AST juego_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST juego_AST = null;
		
		try {      // for error handling
			AST __t355 = _t;
			AST tmp22_AST = null;
			AST tmp22_AST_in = null;
			tmp22_AST = astFactory.create((AST)_t);
			tmp22_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp22_AST);
			ASTPair __currentAST355 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLAY);
			_t = _t.getFirstChild();
			restoJuego(_t,r);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST355;
			_t = __t355;
			_t = _t.getNextSibling();
			juego_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = juego_AST;
		_retTree = _t;
	}
	
	public final void restoJuego(AST _t,
		Rol r
	) throws RecognitionException {
		
		AST restoJuego_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST restoJuego_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case OR:
			{
				AST __t357 = _t;
				AST tmp23_AST = null;
				AST tmp23_AST_in = null;
				tmp23_AST = astFactory.create((AST)_t);
				tmp23_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp23_AST);
				ASTPair __currentAST357 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR);
				_t = _t.getFirstChild();
				r.setOp("OR");
				restoJuego(_t,r);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				restoJuego(_t,r);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST357;
				_t = __t357;
				_t = _t.getNextSibling();
				restoJuego_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			{
				AST __t358 = _t;
				AST tmp24_AST = null;
				AST tmp24_AST_in = null;
				tmp24_AST = astFactory.create((AST)_t);
				tmp24_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp24_AST);
				ASTPair __currentAST358 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND);
				_t = _t.getFirstChild();
				r.setOp("AND");
				restoJuego(_t,r);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				restoJuego(_t,r);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST358;
				_t = __t358;
				_t = _t.getNextSibling();
				restoJuego_AST = (AST)currentAST.root;
				break;
			}
			case IDENT:
			{
				i = (AST)_t;
				AST i_AST_in = null;
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				ArrayList l = r.getListaJuegos();
									l.add(i.getText());
									r.setListaJuegos(l);
				restoJuego_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = restoJuego_AST;
		_retTree = _t;
	}
	
	public final void cardinalidad_prop(AST _t,
		PropiedadClase p
	) throws RecognitionException {
		
		AST cardinalidad_prop_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cardinalidad_prop_AST = null;
		AST l1 = null;
		AST l1_AST = null;
		AST l2 = null;
		AST l2_AST = null;
		AST a = null;
		AST a_AST = null;
		
		try {      // for error handling
			AST __t366 = _t;
			AST tmp25_AST = null;
			AST tmp25_AST_in = null;
			tmp25_AST = astFactory.create((AST)_t);
			tmp25_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			ASTPair __currentAST366 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CARDINALIDAD);
			_t = _t.getFirstChild();
			l1 = (AST)_t;
			AST l1_AST_in = null;
			l1_AST = astFactory.create(l1);
			astFactory.addASTChild(currentAST, l1_AST);
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			Integer i1 = Integer.parseInt(l1.getText());
						 	p.setQMin(i1.intValue());
						   Integer i2 = null;	
						
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LIT_ENTERO:
			{
				l2 = (AST)_t;
				AST l2_AST_in = null;
				l2_AST = astFactory.create(l2);
				astFactory.addASTChild(currentAST, l2_AST);
				match(_t,LIT_ENTERO);
				_t = _t.getNextSibling();
				i2 = Integer.parseInt(l2.getText());
							 	p.setQMax(i2.intValue());
							
				break;
			}
			case ASTERISCO:
			{
				a = (AST)_t;
				AST a_AST_in = null;
				a_AST = astFactory.create(a);
				astFactory.addASTChild(currentAST, a_AST);
				match(_t,ASTERISCO);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST366;
			_t = __t366;
			_t = _t.getNextSibling();
			cardinalidad_prop_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = cardinalidad_prop_AST;
		_retTree = _t;
	}
	
	public final void rango(AST _t,
		PropiedadClase p
	) throws RecognitionException {
		
		AST rango_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rango_AST = null;
		
		try {      // for error handling
			AST __t375 = _t;
			AST tmp26_AST = null;
			AST tmp26_AST_in = null;
			tmp26_AST = astFactory.create((AST)_t);
			tmp26_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp26_AST);
			ASTPair __currentAST375 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RANGO);
			_t = _t.getFirstChild();
			resto_rango(_t,p);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST375;
			_t = __t375;
			_t = _t.getNextSibling();
			rango_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = rango_AST;
		_retTree = _t;
	}
	
	public final void expreg(AST _t,
		PropiedadClase p
	) throws RecognitionException {
		
		AST expreg_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expreg_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			AST __t380 = _t;
			AST tmp27_AST = null;
			AST tmp27_AST_in = null;
			tmp27_AST = astFactory.create((AST)_t);
			tmp27_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp27_AST);
			ASTPair __currentAST380 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EXPREG);
			_t = _t.getFirstChild();
			i = (AST)_t;
			AST i_AST_in = null;
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(_t,IDENT);
			_t = _t.getNextSibling();
			p.setMask(i.getText());
			currentAST = __currentAST380;
			_t = __t380;
			_t = _t.getNextSibling();
			expreg_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = expreg_AST;
		_retTree = _t;
	}
	
	public final void categoria(AST _t,
		PropiedadClase p
	) throws RecognitionException {
		
		AST categoria_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST categoria_AST = null;
		
		try {      // for error handling
			AST __t382 = _t;
			AST tmp28_AST = null;
			AST tmp28_AST_in = null;
			tmp28_AST = astFactory.create((AST)_t);
			tmp28_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp28_AST);
			ASTPair __currentAST382 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CATEGORIA);
			_t = _t.getFirstChild();
			String s1,s2=null;
			s1=categoria2(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PLAY:
			case STRUCTURAL:
			{
				s2=categoria2(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			p.addCategoria(s1); 
											if(s2!=null) p.addCategoria(s2);
			currentAST = __currentAST382;
			_t = __t382;
			_t = _t.getNextSibling();
			categoria_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = categoria_AST;
		_retTree = _t;
	}
	
	public final void longitud(AST _t,
		PropiedadClase p
	) throws RecognitionException {
		
		AST longitud_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST longitud_AST = null;
		AST l = null;
		AST l_AST = null;
		
		try {      // for error handling
			AST __t386 = _t;
			AST tmp29_AST = null;
			AST tmp29_AST_in = null;
			tmp29_AST = astFactory.create((AST)_t);
			tmp29_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
			ASTPair __currentAST386 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LONGITUD);
			_t = _t.getFirstChild();
			l = (AST)_t;
			AST l_AST_in = null;
			l_AST = astFactory.create(l);
			astFactory.addASTChild(currentAST, l_AST);
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			currentAST = __currentAST386;
			_t = __t386;
			_t = _t.getNextSibling();
			Integer i = Integer.parseInt(l.getText());
					 p.setLongitud(i.intValue());
			longitud_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = longitud_AST;
		_retTree = _t;
	}
	
	public final void resto_rango(AST _t,
		PropiedadClase p
	) throws RecognitionException {
		
		AST resto_rango_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST resto_rango_AST = null;
		AST i = null;
		AST i_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case OR:
			{
				AST __t377 = _t;
				AST tmp30_AST = null;
				AST tmp30_AST_in = null;
				tmp30_AST = astFactory.create((AST)_t);
				tmp30_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp30_AST);
				ASTPair __currentAST377 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR);
				_t = _t.getFirstChild();
				p.setOp("OR");
				resto_rango(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				resto_rango(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST377;
				_t = __t377;
				_t = _t.getNextSibling();
				resto_rango_AST = (AST)currentAST.root;
				break;
			}
			case AND:
			{
				AST __t378 = _t;
				AST tmp31_AST = null;
				AST tmp31_AST_in = null;
				tmp31_AST = astFactory.create((AST)_t);
				tmp31_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp31_AST);
				ASTPair __currentAST378 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND);
				_t = _t.getFirstChild();
				p.setOp("AND");
				resto_rango(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				resto_rango(_t,p);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST378;
				_t = __t378;
				_t = _t.getNextSibling();
				resto_rango_AST = (AST)currentAST.root;
				break;
			}
			case IDENT:
			{
				i = (AST)_t;
				AST i_AST_in = null;
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(_t,IDENT);
				_t = _t.getNextSibling();
				p.addRango(i.getText());
				resto_rango_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = resto_rango_AST;
		_retTree = _t;
	}
	
	public final String  categoria2(AST _t) throws RecognitionException {
		String s=null;;
		
		AST categoria2_AST_in = (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST categoria2_AST = null;
		AST p = null;
		AST p_AST = null;
		AST q = null;
		AST q_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PLAY:
			{
				p = (AST)_t;
				AST p_AST_in = null;
				p_AST = astFactory.create(p);
				astFactory.addASTChild(currentAST, p_AST);
				match(_t,PLAY);
				_t = _t.getNextSibling();
				s = p.getText();
				categoria2_AST = (AST)currentAST.root;
				break;
			}
			case STRUCTURAL:
			{
				q = (AST)_t;
				AST q_AST_in = null;
				q_AST = astFactory.create(q);
				astFactory.addASTChild(currentAST, q_AST);
				match(_t,STRUCTURAL);
				_t = _t.getNextSibling();
				s = q.getText();
				categoria2_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = categoria2_AST;
		_retTree = _t;
		return s;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"DEFINICIONES",
		"PROPIEDADES",
		"CUERPO_RELACION",
		"PROP_EN_CLASE",
		"CARDINALIDAD",
		"ROL_POINTED",
		"DATA_PROPERTY",
		"OBJECT_PROPERTY",
		"PROP_CLASE",
		"RANGE",
		"CLASE",
		"IDENT",
		"SUBCLASE",
		"TIPO",
		"ROL",
		"RELACION",
		"PARENTESIS_ABIERTO",
		"LIT_ENTERO",
		"PARENTESIS_CERRADO",
		"ASTERISCO",
		"PLAY",
		"OR",
		"AND",
		"PROPIEDAD",
		"RANGO",
		"EXPREG",
		"CATEGORIA",
		"STRUCTURAL",
		"LONGITUD",
		"PROPIEDAD_COMPLEJA",
		"ENTERO",
		"LOGICO",
		"FECHA",
		"CADENA"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 135020544L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	}
	
