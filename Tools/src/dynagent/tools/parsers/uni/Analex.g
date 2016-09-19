/* ***************************************
            An�lisis L�xico
   ***************************************
   
   En este archivo vamos a anotar todo lo necesario para generar los ficheros
   Analex.java y AnalexTokenTypes.java, las cuales no ser�n comentadas porque
   son clases generadas autom�ticamente a partir de la generaci�n de estos ficheros
   y habr� cosas inteleligibles hasta por el programador que dise��
   este fichero. 
   
   Como dijimos anteriormente, en este fichero nos encargaremos del an�lisis l�xico, 
   dando preferencia a las palabras reservadas del lenguaje y generando Tokens 
   independientes para cada uno de ellos.
   
   A continuacion declaramos Tokens para los signos de puntuacion y aritm�ticos,
   teniendo estos poca relevancia en nueestro lenguaje porque no se usan
   
   Y por ultimo generaremos un Token gen�rico para los identificadores de clases,
   roles, propiedades, etc. A los que al principio la �nica restriccion que le daremos
   es que empiece por una letra y luego pueda ir seguida de letras y/o n�meros.
   
   No es relevante pero la interfaz AnalexTokenTypes.java simplemente asignar� a
   cada Token un valor num�rico para facilitarle las cosas a Analex.java y a su 
   implementaci�n interna.
   
*/

header{
	package dynagent.lenguajeUniv.parser;
}

class Analex extends Lexer;



options{
	// Importaci�n del vocabulario de tokens desde el analizador
	// sint�ctico (Anasint.g)
	importVocab=Anasint;
	// Por defecto no se activa la comprobaci�n de literales
	// declarados en la secci�n tokens
	
	testLiterals=false;
	// 2 s�mbolos de anticipaci�n para tomar decisiones
	// (los tokens DP y ASIG justifican su necesidad)
	k=3;
	charVocabulary = '\3'..'\377';
}

tokens{	
		
	// Palabras reservadas del lenguaje dynagent
	
	CLASE = "CLASE";
	RELACION = "RELACION";
	ROL = "ROL";
	TIPO = "TIPO";
	SUBCLASE = "SUBCLASE";
	VALE = "VALE";
	RESTRICTION	= "RESTRICTION";
	RANGO = "RANGO";
	EXPREG = "EXPREG";
	CATEGORIA = "CATEGORIA";
	STRUCTURAL = "STRUCTURAL";
	PROPIEDAD = "PROPIEDAD";
	LONGITUD = "LONGITUD";
	INDIVIDUO = "INDIVIDUO";
	UNODE = "UNODE";
	INV = "INV";
	ROLPOINTER = "ROLPOINTER";
	PEER = "PEER";
	FULL = "FULL";
	INVERSA = "INVERSA";
	// Tipos b�sicos
	
	ENTERO = "int";
	CADENA = "string";
	LOGICO = "boolean";
	FECHA = "date";
	HORA = "time";
	REAL = "float";
	MEMO = "memo";
	// Relaciones
	
	APUNTA = "APUNTA";
	OR = "OR";
	AND = "AND";
	PLAY = "PLAY";
	
	// Numeros
	
	LIT_REAL;
	LIT_ENTERO;
}

// Tokens in�tiles para el an�lisis sint�ctico
// (B)lancos y (T)abuladores
BT : (' '|'\t') {$setType(Token.SKIP);} ;
// (S)altos de (L)inea
NL : "\r\n" {newline();$setType(Token.SKIP);} ;
// Comentario de l�nea
//COMENT_LIN: "//" (('\r')+ ~('\n') | ~('\r') )* "\r\n" {newline();$setType(Token.SKIP);} ;
//COMMENT_LINE: ";" (options{greedy=false;}:.)* "\r\n" {newline();$setType(Token.SKIP);} ;
COMENTARIO1: "//" (options {greedy = false;}:.)* ("\r\n" | '\n') {newline();$setType(Token.SKIP);} ;
COMENTARIO2: "/*" (options {greedy = false;}:.)* "*/" {$setType(Token.SKIP);} ;
// Signos de puntuaci�n
DOS_PUNTOS : ':'; // (D)os (P)untos
PARENTESIS_ABIERTO : '('; // (P)arentesis (A)bierto
PARENTESIS_CERRADO : ')'; // (P)arentesis (C)errado
LLAVE_ABIERTA: '{'; // (LL)ave (A)bierta
LLAVE_CERRADA: '}'; // (LL)ave (C)errada
CORCHETE_ABIERTO: '['; // (COR)chete (A)bierto
CORCHETE_CERRADO: ']'; // (COR)chete (C)errado
COMA: ','; // (CO)ma
PUNTO_Y_COMA: ';'; // (PU)nto y (C)oma
PUNTO:'.'; // (PU)nto
INTERROGACION:'?';


// Operadores aritm�ticos
MAS: '+';
MENOS: '-';


//CARDINALIDAD
ASTERISCO: '*';
//DIVISION: '/';

// Operadores relacionales
MENOR:'<';
MENOR_IGUAL:"<=";
MAYOR:'>';
MAYOR_IGUAL:">=";
IGUAL: '=';
DISTINTO: '~';
AND:'&';
OR:'|';
BARRABAJA : '_';
// Asignaci�n
ASIGNA : "<-" ;

IMPLICA: "=>";

// Lexemas auxiliares
protected DIGITO: ('0'..'9');
protected LETRA_MAY:('A'..'Z') | '�';
protected LETRA_MIN:('a'..'z') | '�';
protected LETRA: LETRA_MAY | LETRA_MIN;

// Literales Enteros y Reales
NUMERO : ((DIGITO)+ ',') => (DIGITO)+ ',' (DIGITO)+ {$setType(LIT_REAL);}
	   | ((DIGITO)+) => (DIGITO)+ {$setType(LIT_ENTERO);}
	   ;

// Literales Car�cter
LIT_CAR: '\''! (~('\''|'\n'|'\r'|'\t')) '\''!;

// Lexema IDENT (Identificadores)
// Se activa la comprobaci�n de palabras reservadas.
// Las palabras reservadas tienen preferencia a cualquier otro identificador.
IDENT options {testLiterals=true;}: LETRA(LETRA|DIGITO|BARRABAJA|DOS_PUNTOS)* ;
NOMBRE_CL options {testLiterals=true;}: LETRA_MAY(LETRA|DIGITO)*;

//TODO Generar el token correcto para las expresiones regulares,
// de momento sera un ident
