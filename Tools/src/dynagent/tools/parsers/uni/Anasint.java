// $ANTLR : "E:/DESARROLLO/Workspace/Alvarez/RuleEngine/src/dynagent/lenguajeUniv/parser/Anasint.g" -> "Anasint.java"$

	package dynagent.tools.parsers.uni;
	import java.util.*;
	
import dynagent.tools.parsers.uni.auxiliar.ProcesamientoFichero;
	import antlr.*;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class Anasint extends antlr.LLkParser       implements AnasintTokenTypes
 {

	/* Creamos listas anotando los nombres y las interacciones de estas */
	
	ArrayList listaClase = new ArrayList();
	ArrayList listaRelacion =new ArrayList();
	ArrayList listaRol = new ArrayList();
	ArrayList listaRolesPorComprobar = new ArrayList();
	ArrayList listaRelacionesPorComprobar = new ArrayList();
	ArrayList listaClasesPorComprobar = new ArrayList();
	
	
	
	/* Utilizamos las listas anteriores para generar los atributos de 
	una clase que utilizaremos posteriormente, concretamente en
	Principal.java */
	
	public ProcesamientoFichero getTablasDatos(){
		
		ProcesamientoFichero pf = new ProcesamientoFichero();
		pf.listaClase = listaClase;
		pf.listaRelacion = listaRelacion;
		pf.listaRol = listaRol;
		pf.listaRolesPorComprobar = listaRolesPorComprobar;
		pf.listaClasesPorComprobar = listaClasesPorComprobar;
		return pf;
		
	}
	
	

protected Anasint(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public Anasint(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected Anasint(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public Anasint(TokenStream lexer) {
  this(lexer,3);
}

public Anasint(ParserSharedInputState state) {
  super(state,3);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final ProcesamientoFichero  lenguaje() throws RecognitionException, TokenStreamException {
		ProcesamientoFichero pf = new ProcesamientoFichero();
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST lenguaje_AST = null;
		AST d_AST = null;
		
		try {      // for error handling
			definiciones();
			d_AST = (AST)returnAST;
			match(Token.EOF_TYPE);
			lenguaje_AST = (AST)currentAST.root;
			lenguaje_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(DEFINICIONES,"DEFINICIONES")).add(d_AST));
				
				pf = getTablasDatos();
				
				
			currentAST.root = lenguaje_AST;
			currentAST.child = lenguaje_AST!=null &&lenguaje_AST.getFirstChild()!=null ?
				lenguaje_AST.getFirstChild() : lenguaje_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		returnAST = lenguaje_AST;
		return pf;
	}
	
	public final void definiciones() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST definiciones_AST = null;
		
		try {      // for error handling
			{
			_loop390:
			do {
				switch ( LA(1)) {
				case CLASE:
				{
					clase();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RELACION:
				{
					relacion();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case ROL:
				{
					rol_clase();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case PROPIEDAD:
				{
					prop_clase();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					break _loop390;
				}
				}
			} while (true);
			}
			definiciones_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		returnAST = definiciones_AST;
	}
	
	public final void clase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST clase_AST = null;
		
		try {      // for error handling
			AST tmp2_AST = null;
			tmp2_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp2_AST);
			match(CLASE);
			nombreClase();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case SUBCLASE:
			{
				subclase();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case CLASE:
			case IDENT:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			propiedades();
			astFactory.addASTChild(currentAST, returnAST);
			clase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = clase_AST;
	}
	
	public final void relacion() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relacion_AST = null;
		
		try {      // for error handling
			AST tmp3_AST = null;
			tmp3_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp3_AST);
			match(RELACION);
			nombreRelacion();
			astFactory.addASTChild(currentAST, returnAST);
			cuerpoRelacion();
			astFactory.addASTChild(currentAST, returnAST);
			relacion_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = relacion_AST;
	}
	
	public final void rol_clase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rol_clase_AST = null;
		
		try {      // for error handling
			AST tmp4_AST = null;
			tmp4_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp4_AST);
			match(ROL);
			nombreRolClase();
			astFactory.addASTChild(currentAST, returnAST);
			juego();
			astFactory.addASTChild(currentAST, returnAST);
			propiedades();
			astFactory.addASTChild(currentAST, returnAST);
			rol_clase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = rol_clase_AST;
	}
	
	public final void prop_clase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST prop_clase_AST = null;
		
		try {      // for error handling
			AST tmp5_AST = null;
			tmp5_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp5_AST);
			match(PROPIEDAD);
			AST tmp6_AST = null;
			tmp6_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp6_AST);
			match(IDENT);
			range();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case EXPREG:
			{
				reg_exp();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case CLASE:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			case CATEGORIA:
			case LONGITUD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case CATEGORIA:
			{
				categoria();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case CLASE:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			case LONGITUD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LONGITUD:
			{
				longitud();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case CLASE:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			prop_clase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = prop_clase_AST;
	}
	
	public final void nombreClase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nombreClase_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaClase.add(i.getText());
			nombreClase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		returnAST = nombreClase_AST;
	}
	
	public final void subclase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST subclase_AST = null;
		
		try {      // for error handling
			AST tmp7_AST = null;
			tmp7_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp7_AST);
			match(SUBCLASE);
			clases_juego();
			astFactory.addASTChild(currentAST, returnAST);
			subclase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = subclase_AST;
	}
	
	public final void propiedades() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST propiedades_AST = null;
		AST t_AST = null;
		
		try {      // for error handling
			{
			_loop397:
			do {
				if ((LA(1)==IDENT)) {
					tiposPropiedad();
					t_AST = (AST)returnAST;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop397;
				}
				
			} while (true);
			}
			propiedades_AST = (AST)currentAST.root;
			propiedades_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(PROPIEDADES,"PROPIEDADES")).add(propiedades_AST));
			currentAST.root = propiedades_AST;
			currentAST.child = propiedades_AST!=null &&propiedades_AST.getFirstChild()!=null ?
				propiedades_AST.getFirstChild() : propiedades_AST;
			currentAST.advanceChildToEnd();
			propiedades_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = propiedades_AST;
	}
	
	public final void clases_juego() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST clases_juego_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case PARENTESIS_ABIERTO:
			{
				match(PARENTESIS_ABIERTO);
				clases_juego2();
				astFactory.addASTChild(currentAST, returnAST);
				match(PARENTESIS_CERRADO);
				clases_juego_AST = (AST)currentAST.root;
				break;
			}
			case IDENT:
			{
				clases_juego2();
				astFactory.addASTChild(currentAST, returnAST);
				clases_juego_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_4);
		}
		returnAST = clases_juego_AST;
	}
	
	public final void tiposPropiedad() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tiposPropiedad_AST = null;
		
		try {      // for error handling
			if ((LA(1)==IDENT) && (_tokenSet_5.member(LA(2))) && (_tokenSet_6.member(LA(3)))) {
				propiedad();
				astFactory.addASTChild(currentAST, returnAST);
				tiposPropiedad_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==IDENT) && (LA(2)==ROL) && (LA(3)==IDENT)) {
				propiedad_compleja();
				astFactory.addASTChild(currentAST, returnAST);
				tiposPropiedad_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = tiposPropiedad_AST;
	}
	
	public final void propiedad() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST propiedad_AST = null;
		Token  i = null;
		AST i_AST = null;
		AST p_AST = null;
		AST c_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			match(IDENT);
			{
			switch ( LA(1)) {
			case TIPO:
			{
				match(TIPO);
				property();
				p_AST = (AST)returnAST;
				break;
			}
			case EOF:
			case CLASE:
			case IDENT:
			case ROL:
			case RELACION:
			case PARENTESIS_ABIERTO:
			case PROPIEDAD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case PARENTESIS_ABIERTO:
			{
				cardinalidad();
				c_AST = (AST)returnAST;
				break;
			}
			case EOF:
			case CLASE:
			case IDENT:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			propiedad_AST = (AST)currentAST.root;
			propiedad_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(PROP_EN_CLASE,"PROP_EN_CLASE")).add(i_AST).add(p_AST).add(c_AST));
			currentAST.root = propiedad_AST;
			currentAST.child = propiedad_AST!=null &&propiedad_AST.getFirstChild()!=null ?
				propiedad_AST.getFirstChild() : propiedad_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = propiedad_AST;
	}
	
	public final void propiedad_compleja() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST propiedad_compleja_AST = null;
		AST i_AST = null;
		AST t1_AST = null;
		AST c_AST = null;
		AST t2_AST = null;
		
		try {      // for error handling
			identificador();
			i_AST = (AST)returnAST;
			match(ROL);
			tipoRol();
			t1_AST = (AST)returnAST;
			cardinalidad();
			c_AST = (AST)returnAST;
			match(RELACION);
			tipoRelacion();
			t2_AST = (AST)returnAST;
			propiedad_compleja_AST = (AST)currentAST.root;
			propiedad_compleja_AST = (AST)astFactory.make( (new ASTArray(5)).add(astFactory.create(ROL_POINTED,"ROL_POINTED")).add(i_AST).add(t1_AST).add(c_AST).add(t2_AST));
			currentAST.root = propiedad_compleja_AST;
			currentAST.child = propiedad_compleja_AST!=null &&propiedad_compleja_AST.getFirstChild()!=null ?
				propiedad_compleja_AST.getFirstChild() : propiedad_compleja_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = propiedad_compleja_AST;
	}
	
	public final void property() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST property_AST = null;
		
		try {      // for error handling
			object_property();
			astFactory.addASTChild(currentAST, returnAST);
			property_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_7);
		}
		returnAST = property_AST;
	}
	
	public final void cardinalidad() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cardinalidad_AST = null;
		Token  i1 = null;
		AST i1_AST = null;
		AST i2_AST = null;
		int n=0;
		
		try {      // for error handling
			AST tmp13_AST = null;
			tmp13_AST = astFactory.create(LT(1));
			match(PARENTESIS_ABIERTO);
			i1 = LT(1);
			i1_AST = astFactory.create(i1);
			match(LIT_ENTERO);
			n=rango();
			i2_AST = (AST)returnAST;
			AST tmp14_AST = null;
			tmp14_AST = astFactory.create(LT(1));
			match(PARENTESIS_CERRADO);
			cardinalidad_AST = (AST)currentAST.root;
			cardinalidad_AST = (AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(CARDINALIDAD,"CARDINALIDAD")).add(i1_AST).add(i2_AST));
			currentAST.root = cardinalidad_AST;
			currentAST.child = cardinalidad_AST!=null &&cardinalidad_AST.getFirstChild()!=null ?
				cardinalidad_AST.getFirstChild() : cardinalidad_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = cardinalidad_AST;
	}
	
	public final void identificador() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identificador_AST = null;
		
		try {      // for error handling
			AST tmp15_AST = null;
			tmp15_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp15_AST);
			match(IDENT);
			identificador_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_8);
		}
		returnAST = identificador_AST;
	}
	
	public final void object_property() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST object_property_AST = null;
		AST cl_AST = null;
		
		try {      // for error handling
			clases_juego();
			cl_AST = (AST)returnAST;
			object_property_AST = (AST)currentAST.root;
			object_property_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(RANGE,"RANGE")).add(cl_AST));
										//listaClasesPorComprobar.add(i.getText());
										
			currentAST.root = object_property_AST;
			currentAST.child = object_property_AST!=null &&object_property_AST.getFirstChild()!=null ?
				object_property_AST.getFirstChild() : object_property_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_7);
		}
		returnAST = object_property_AST;
	}
	
	public final void tipoRol() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tipoRol_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaRolesPorComprobar.add(i.getText());
			tipoRol_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_9);
		}
		returnAST = tipoRol_AST;
	}
	
	public final void tipoRelacion() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tipoRelacion_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaRelacionesPorComprobar.add(i.getText());
			tipoRelacion_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = tipoRelacion_AST;
	}
	
	public final int  rango() throws RecognitionException, TokenStreamException {
		int a=7;;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rango_AST = null;
		Token  l = null;
		AST l_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LIT_ENTERO:
			{
				l = LT(1);
				l_AST = astFactory.create(l);
				astFactory.addASTChild(currentAST, l_AST);
				match(LIT_ENTERO);
				rango_AST = (AST)currentAST.root;
				break;
			}
			case ASTERISCO:
			{
				AST tmp16_AST = null;
				tmp16_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp16_AST);
				match(ASTERISCO);
				rango_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_10);
		}
		returnAST = rango_AST;
		return a;
	}
	
	public final void juego() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST juego_AST = null;
		
		try {      // for error handling
			AST tmp17_AST = null;
			tmp17_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp17_AST);
			match(PLAY);
			clases_juego();
			astFactory.addASTChild(currentAST, returnAST);
			juego_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = juego_AST;
	}
	
	public final void clases_juego2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST clases_juego2_AST = null;
		
		try {      // for error handling
			clases_juego3();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop414:
			do {
				if ((LA(1)==OR)) {
					AST tmp18_AST = null;
					tmp18_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp18_AST);
					match(OR);
					clases_juego3();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop414;
				}
				
			} while (true);
			}
			clases_juego2_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_11);
		}
		returnAST = clases_juego2_AST;
	}
	
	public final void clases_juego3() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST clases_juego3_AST = null;
		
		try {      // for error handling
			clases_juego4();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop417:
			do {
				if ((LA(1)==AND)) {
					AST tmp19_AST = null;
					tmp19_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp19_AST);
					match(AND);
					clases_juego4();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop417;
				}
				
			} while (true);
			}
			clases_juego3_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_12);
		}
		returnAST = clases_juego3_AST;
	}
	
	public final void clases_juego4() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST clases_juego4_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaClasesPorComprobar.add(i.getText());
			clases_juego4_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_13);
		}
		returnAST = clases_juego4_AST;
	}
	
	public final void nombreRelacion() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nombreRelacion_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaRelacion.add(i.getText());
			nombreRelacion_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = nombreRelacion_AST;
	}
	
	public final void cuerpoRelacion() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cuerpoRelacion_AST = null;
		AST c_AST = null;
		
		try {      // for error handling
			cuerpoR();
			c_AST = (AST)returnAST;
			cuerpoRelacion_AST = (AST)currentAST.root;
			cuerpoRelacion_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CUERPO_RELACION,"CUERPO_RELACION")).add(c_AST));
			currentAST.root = cuerpoRelacion_AST;
			currentAST.child = cuerpoRelacion_AST!=null &&cuerpoRelacion_AST.getFirstChild()!=null ?
				cuerpoRelacion_AST.getFirstChild() : cuerpoRelacion_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = cuerpoRelacion_AST;
	}
	
	public final void cuerpoR() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST cuerpoR_AST = null;
		
		try {      // for error handling
			{
			_loop424:
			do {
				if ((LA(1)==IDENT)) {
					tiposPropiedad();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((LA(1)==ROL) && (LA(2)==IDENT) && (_tokenSet_7.member(LA(3)))) {
					rol();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop424;
				}
				
			} while (true);
			}
			cuerpoR_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = cuerpoR_AST;
	}
	
	public final void rol() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rol_AST = null;
		
		try {      // for error handling
			AST tmp20_AST = null;
			tmp20_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp20_AST);
			match(ROL);
			nombreRol();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case PARENTESIS_ABIERTO:
			{
				cardinalidad();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case CLASE:
			case IDENT:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			rol_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = rol_AST;
	}
	
	public final void nombreRol() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nombreRol_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaRolesPorComprobar.add(i.getText());
			nombreRol_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_7);
		}
		returnAST = nombreRol_AST;
	}
	
	public final void nombreRolClase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nombreRolClase_AST = null;
		Token  i = null;
		AST i_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.addASTChild(currentAST, i_AST);
			match(IDENT);
			listaRol.add(i.getText());
			nombreRolClase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_14);
		}
		returnAST = nombreRolClase_AST;
	}
	
	public final void range() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST range_AST = null;
		
		try {      // for error handling
			AST tmp21_AST = null;
			tmp21_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp21_AST);
			match(RANGO);
			clases_juego();
			astFactory.addASTChild(currentAST, returnAST);
			range_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_15);
		}
		returnAST = range_AST;
	}
	
	public final void reg_exp() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST reg_exp_AST = null;
		
		try {      // for error handling
			AST tmp22_AST = null;
			tmp22_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp22_AST);
			match(EXPREG);
			AST tmp23_AST = null;
			tmp23_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp23_AST);
			match(IDENT);
			reg_exp_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_16);
		}
		returnAST = reg_exp_AST;
	}
	
	public final void categoria() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST categoria_AST = null;
		
		try {      // for error handling
			AST tmp24_AST = null;
			tmp24_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp24_AST);
			match(CATEGORIA);
			categoria2();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case PLAY:
			case STRUCTURAL:
			{
				categoria2();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case CLASE:
			case ROL:
			case RELACION:
			case PROPIEDAD:
			case LONGITUD:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			categoria_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_17);
		}
		returnAST = categoria_AST;
	}
	
	public final void longitud() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST longitud_AST = null;
		
		try {      // for error handling
			AST tmp25_AST = null;
			tmp25_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp25_AST);
			match(LONGITUD);
			AST tmp26_AST = null;
			tmp26_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp26_AST);
			match(LIT_ENTERO);
			longitud_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = longitud_AST;
	}
	
	public final void categoria2() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST categoria2_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case PLAY:
			{
				AST tmp27_AST = null;
				tmp27_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp27_AST);
				match(PLAY);
				categoria2_AST = (AST)currentAST.root;
				break;
			}
			case STRUCTURAL:
			{
				AST tmp28_AST = null;
				tmp28_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp28_AST);
				match(STRUCTURAL);
				categoria2_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_18);
		}
		returnAST = categoria2_AST;
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
		"LONGITUD"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 135020546L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 135118850L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 135053314L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 6041681922L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 136232962L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 138330114L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 136101890L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 262144L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 1048576L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 4194304L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 6045876226L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 6079430658L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 6146539522L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 16777216L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 6040600578L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 5503729666L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 4429987842L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 6594248706L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	
	}
