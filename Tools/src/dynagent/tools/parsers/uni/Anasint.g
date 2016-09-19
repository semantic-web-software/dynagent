


/* ************************************
         Análisis Sintáctico
   ************************************
   
   En este fichero nos ocupamos del análisis sintáctico del lenguaje,
   es decir, si un fichero, correctamente desde el punto de vista léxico
   está bien formado y siguiendo el orden establecido en el lenguaje. 
   Veamos un ejemplo:
   1.Yo djdasldhas un perro: no es una frase léxicamente correcta puesto que
   la palabra djdasldhas no existe en nuestro lenguaje.
   
   2.Yo perro un tengo: aunque léxicamente cumple todos los requisitos, vemos
   que no cumple con el paradigma del lenguaje de Pronombre + Verbo, por tanto
   no está sintácticamente bien construida.
   
   3. Tu tengo un perro: vemos que cumple el léxico y la sintaxis, pero la frase
   semánticamente no tiene sentido, puesto que el verbo está mal conjugado. Esto 
   lo veremos en el fichero Anasem.g, que se encarga del análisis semántico
   
 */
   
 

header{
	package dynagent.lenguajeUniv.parser;
	import java.util.*;
	import dynagent.lenguajeUniv.parser.auxiliar.*;
	import antlr.*;
}

class Anasint extends Parser;

options{
	buildAST=true;
	k=6;
}
tokens{
	//Tokens virtuales para crear el arbol
	DEFINICIONES;
	PROPIEDADES;
	CUERPO_RELACION;
	//PROPIEDAD;
	PROP_EN_CLASE;
	CARDINALIDAD;
	DATA_PROPERTY;
	OBJECT_PROPERTY;
	PROP_CLASE;
	RANGE;
}


{
	/* Creamos listas anotando los nombres y las interacciones de estas */
	
	ArrayList listaClase = new ArrayList();
	ArrayList listaRelacion =new ArrayList();
	ArrayList listaRol = new ArrayList();
	ArrayList listaPropiedad = new ArrayList();
	ArrayList listaRolesPorComprobar = new ArrayList();
	ArrayList listaRelacionesPorComprobar = new ArrayList();
	ArrayList listaClasesPorComprobar = new ArrayList();
	ArrayList listaRolPointer = new ArrayList();
	ArrayList listaIndividuos = new ArrayList();
	
	
	
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
		pf.listaRelacionesPorComprobar = listaRelacionesPorComprobar;
		pf.listaPropiedad = listaPropiedad;
		pf.listaRolPointer = listaRolPointer;
		pf.listaIndividuos = listaIndividuos;
		return pf;
		
	}
	
	
}

/////////////////////////
//      LENGUAJE       //
/////////////////////////


//El lenguaje tiene una serie de definiciones

lenguaje! returns [ProcesamientoFichero pf = new ProcesamientoFichero()]: d:definiciones EOF!
	{## = #(#[DEFINICIONES, "DEFINICIONES"], #d);
	
	pf = getTablasDatos();
	
	}
	; 

//Estas definiciones pueden ser clases, relaciones o roles

definiciones : (clase | relacion | rol_clase | prop_clase | individuo | rol_pointer)*
	;
	
// La clase esta formada por el token CLASE, un ident que será su nombre
// Opcionalmente puede heredar de alguna otra (o varias) 	
// Y puede tener un conjunto de propiedades asociadas


clase : CLASE^ nombreClase (subclase)? propiedades 
	;

nombreClase : i:IDENT
			 { listaClase.add(i.getText());}
			 | {String s;} s=tipoBasico2 {listaClase.add(s);}
	;


tipoBasico2 returns [String s=null]: e:ENTERO {s = e.getText();}
		| r:REAL {s = r.getText();}
		| c:CADENA {s = c.getText();}
		| f:FECHA {s = f.getText();}
		| h:HORA {s = h.getText();}
		| l:LOGICO {s = l.getText();}
		| m:ROL {s = m.getText();}
		;
		
subclase : SUBCLASE^ clases_juego
		;


propiedades : (t:tiposPropiedad)*
	{## = #(#[PROPIEDADES, "PROPIEDADES"], ##);}
	;

	
// La propiedad puede ser una object property, una data property 
// o un rol pointer

tiposPropiedad : propiedad
			;
			

propiedad! : i:IDENT (p:redef_rango)? (r:redef_enum)? (c:cardinalidad)?
			{## = #(#[PROP_EN_CLASE, "PROP_EN_CLASE"], #i, #p,#r, #c);}
	;

redef_rango : RANGO! p:property
			;
			
redef_enum : UNODE^ listaEnumerado
		;
	
identificador : IDENT
	;
	
property : object_property 
	;



object_property! : cl:clases_juego  {## = #(#[RANGE, "RANGE"], #cl);
							//listaClasesPorComprobar.add(i.getText());
							}
	;


// Tanto para el rol pointer como para las relaciones y los roles "simples"
// el rol y la relacion serán identificadores iguales que los de clase

tipoRol : i:IDENT {listaRolesPorComprobar.add(i.getText());}
		;
		
tipoRelacion : i:IDENT {listaRelacionesPorComprobar.add(i.getText());}
		;

// La cardinalidad indica el número máximo y minimo de individuos de esa propiedad
// El primer valor (mínimo) será un numero entero, y el segundo (máximo)
// podrá ser un número entero o un asterisco (*)

cardinalidad! {int n=0;}: PARENTESIS_ABIERTO i1:LIT_ENTERO n=i2:rango PARENTESIS_CERRADO
				{ ## = #(#[CARDINALIDAD, "CARDINALIDAD"], #i1, #i2);}
	;

rango returns [int a=7;]: l:LIT_ENTERO 
		| ASTERISCO
		;

// Un rol es jugado por una o varias clases, siempre llevando el token PLAY delante

juego : PLAY^ clases_juego (cardinalidad)?
	;

clases_juego : PARENTESIS_ABIERTO! clases_juego2 PARENTESIS_CERRADO!
			| clases_juego2
			;
	 
clases_juego2 :  clases_juego3 (OR^ clases_juego3)*
			;
			
clases_juego3 : clases_juego4 (AND^ clases_juego4) *
			;

clases_juego4 : i:IDENT {listaClasesPorComprobar.add(i.getText());}
				| i2:ROL {listaClasesPorComprobar.add(i2.getText());}
				;
//Una relacion esta formada por el token relacion, su nombre y un cuerpo
// en el cual hay roles y propiedades

relacion : RELACION^ nombreRelacion (subclase)? cuerpoRelacion
		;
	
nombreRelacion : i:IDENT {listaRelacion.add(i.getText());}
		;
	
cuerpoRelacion! : c:cuerpoR
		{## = #(#[CUERPO_RELACION, "CUERPO_RELACION"], #c);}
		;

//Lo pongo de esta manera para la construccion del arbol
		
cuerpoR	: (tiposPropiedad | rol)*
		;
		
// Un rol dentro de una clase tiene el token ROL, un identificador
// y la cardinalidad	
		
rol : ROL^ (FULL)? nombreRol (redefinicion_rol)? (cardinalidad)? (INV cardinalidad)? (peer)?
	;

peer : PEER^ i:IDENT {listaRolesPorComprobar.add(i.getText());}
		;

redefinicion_rol : PLAY^ clases_juego
			;

nombreRol : i:IDENT {listaRolesPorComprobar.add(i.getText());}
		;

// Un rol actuando como clase tiene el token ROL, un ident
// las clases que juegan el rol y un conjunto de propiedades
// semejantes a los de las clases

rol_clase : ROL^ nombreRolClase juego propiedades
	;
	
nombreRolClase : i:IDENT {listaRol.add(i.getText());}
	;

	
rol_pointer: ROLPOINTER^ i:IDENT (cardinalidad)? rolb relb
			{listaPropiedad.add(i.getText());
			 listaRolPointer.add(i.getText());}	
			;
rolb : ROL^ i2:IDENT { listaRolesPorComprobar.add(i2.getText()); }
	;
	
relb: RELACION^ i3:IDENT {listaRelacionesPorComprobar.add(i3.getText());}
	;	
prop_clase: PROPIEDAD^ i:IDENT {listaPropiedad.add(i.getText());} (range | enumerado) (reg_exp)? (categoria)? (longitud)? (inversa)?
		;


range: RANGO^ clases_juego (cardinalidad)?
		| TIPO^ tipoBasico
		;

tipoBasico: ENTERO
		| REAL
		| CADENA
		| FECHA
		| HORA
		| LOGICO
		| MEMO
		;

enumerado: UNODE^ listaEnumerado
		;

listaEnumerado: IDENT (OR^ IDENT)*
			;

reg_exp : EXPREG^ IDENT	
		;
		
categoria : CATEGORIA^ categoria2 (categoria2)?
		;
		
categoria2: PLAY
			| STRUCTURAL
			;
	
	
longitud : LONGITUD^ LIT_ENTERO
		;
		
individuo : INDIVIDUO^ i:IDENT {listaIndividuos.add(i.getText());} tipoInd
		;


inversa : INVERSA^ IDENT
		;
	
tipoInd : TIPO^ (     i:IDENT {listaClasesPorComprobar.add(i.getText());}
					| e:ENTERO {listaClasesPorComprobar.add(e.getText());}
					| r:REAL {listaClasesPorComprobar.add(r.getText());}
					| f:FECHA {listaClasesPorComprobar.add(f.getText());}
					| h:HORA {listaClasesPorComprobar.add(h.getText());}
					| l:LOGICO {listaClasesPorComprobar.add(l.getText());}
					| c:CADENA {listaClasesPorComprobar.add(c.getText());}
				)
	;  
