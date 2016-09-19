header{
	package dynagent.ruleengine.meta.accessParser;
	import java.util.*;
	import antlr.*;
}

class Anasint extends Parser;

options{
	buildAST=true;
	k=11;
}
tokens{
	DECLARACIONES;
	CAMPOS;
}


{
	
	ArrayList<Acceso> registros = new ArrayList<Acceso>();
	

}

/////////////////////////
//DECLARACION DE REGLAS//
/////////////////////////

declaraciones returns [ArrayList<Acceso> l=null]: (utask)*
			{## =#(#[DECLARACIONES, "DECLARACIONES"], ##);
			l = registros;}
			;
			
			

utask: UTASK^ {String s;} s=ident permisos[s]
			;
			
permisos[String s] : (permiso[s])*
			;
			
permiso[String s] {Acceso acc = new Acceso();} :  PERMISO!	AMBITO_OBJETO^ {acc.setUtask(s); acc.setAmbito("objeto");} acceso[acc] rolusuario[acc] clase[acc] (usuario[acc])? (individuo[acc])? {registros.add(acc);}
			| PERMISO! AMBITO_PROPIEDAD^ {acc.setUtask(s); acc.setAmbito("propiedad");}  acceso[acc] (atributo[acc])* {registros.add(acc);}
			;
			
acceso[Acceso a] : ACCESOS^ listaaccesos[a]
			;
			
listaaccesos[Acceso a]{Permiso p = new Permiso();} : i:IDENT {p.setPermiso(i.getText());}  (DENEGADO {p.setIsDenegado(true);})?  {a.addPermiso(p);}
													(COMA^ {Permiso p2 = new Permiso();} i2:IDENT {p2.setPermiso(i2.getText());} (DENEGADO {p2.setIsDenegado(true);})? {a.addPermiso(p2);})*
			;
	
	
atributo[Acceso a]: 	usuario[a]
			| rolusuario[a]
			| clase[a]
			| rol[a]
			| individuo[a]
			| propiedad[a]
			| valor[a]
			| rango[a]
			| relacion[a]
			| individuorel[a]
			| rolb[a]
			;
		
rolusuario[Acceso a] : ROL_USUARIO^ {String s1; ArrayList<String> uroles = new ArrayList<String>();} s1=ident {a.addRolUsuario(s1);} (COMA^ {String s2 = null;} s2=ident {a.addRolUsuario(s2);})*
			;
		
clase[Acceso a] : CLASE^ {String s1; ArrayList<String> clases = new ArrayList<String>();} s1=ident {a.addClase(s1);} (COMA^ {String s2 = null;} s2=ident {a.addClase(s2);})*
			;
			
rol[Acceso a] : ROL^ i:IDENT {a.setRol(i.getText());}
			;
					
individuo[Acceso a] : INDIVIDUO^ i:IDENT {a.setIndividuo(i.getText());}
			;

usuario[Acceso a] : USUARIO^ i:IDENT {a.setUsuario(i.getText());}
			;

propiedad[Acceso a] : PROPIEDAD^ {String s;} s=ident {a.setPropiedad(s);}
			;
		
valor[Acceso a] : VALOR^ i:IDENT {a.setValor(i.getText());}
			;
		
rango[Acceso a] : RANGO^ i:IDENT {a.setRango(i.getText());}
			;
		
relacion[Acceso a] : RELACION^ i:IDENT {a.setRelacion(i.getText());}
			;

individuorel[Acceso a] : INDIVIDUO_RELACION^ {String s;} s=ident {a.setIndividualRelacion(s);}
			;
		
rolb[Acceso a] : ROLB^ {String s;} s=ident {a.setRolB(s);}
			; 

ident returns [String s=null] : (i:IDENT {s = i.getText();} | ASTERISCO {s = "TODAS";})
			;