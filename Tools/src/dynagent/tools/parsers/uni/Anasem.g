header{ 
	package dynagent.lenguajeUniv.parser;
	import java.util.*; 
	import antlr.collections.ASTEnumeration;
	import dynagent.lenguajeUniv.parser.auxiliar.*;
	import dynagent.ruleengine.*;

}

class Anasem extends TreeParser;
options {
	buildAST=true;
	importVocab=Analex;
	importVocab=Anasint;
	k = 6;
}

{
	ListaSubClase listaSc = new ListaSubClase();
	ListaPropiedad listaP = new ListaPropiedad();
	ListaRolAtrib listaR = new ListaRolAtrib();
	ListaRol listaRol = new ListaRol();
	ArrayList listaPropCl = new ArrayList();
	ArrayList listaPropAt = new ArrayList();
	ArrayList listaIndividuos = new ArrayList();
	ArrayList listaRolPointers = new ArrayList();
	
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
		pfa.listaIndividuos = listaIndividuos;
		pfa.listaRolPointers = listaRolPointers;
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
}

entrada returns [ProcesamientoFicheroAdvanced pfa = new ProcesamientoFicheroAdvanced()] :
	 #(DEFINICIONES (definicion)*)
	{
	 pfa = getTablasDatos();
	// System.out.println(listaPropAt);
	// System.out.println(listaPropCl);
	//compruebaRestriccionesCorrectas(listaPropCl, listaPropAt);
	 //System.out.println(listaP);
	 //System.out.println(listaRol);
	 //informaClasesEnRelaciones();
	 }
	;
definicion : clase  
		|relacion
		| claseRol
		| claseProp
		| individuo
		| rol_pointer
		;
		
clase : #(CLASE {String s=null;} ( i:IDENT {s = i.getText();} | s=tipoBasico2)
						 {SubClase sc = new SubClase();
						 sc.setName(s);} 
						  (subclase[sc])?
						  propiedades[s])
	;

tipoBasico2 returns [String s=null] : 
		  e:ENTERO {s = e.getText();} 
		| r:REAL {s = r.getText();} 
		| c:CADENA  {s = c.getText();} 
		| f:FECHA  {s = f.getText();} 
		| h:HORA {s = h.getText();} 
		| l:LOGICO {s = l.getText();} 
		| m: ROL {s = m.getText();}
		;
	
subclase [SubClase sc] : #(SUBCLASE restoSubclase[sc]
							 {listaSc.addSubClass(sc);}
							 )
	;
	
restoSubclase [SubClase sc] : #(OR restoSubclase[sc] restoSubclase[sc])
		| #(AND restoSubclase[sc] restoSubclase[sc])
		| i:IDENT { ArrayList l = sc.getListaPadres();
					l.add(i.getText());
					sc.setListaPadres(l);
		}
		;
	
propiedades [String s] : #(PROPIEDADES (tipoPropiedad[s])*)
	;
	
tipoPropiedad [String s] : #(PROP_EN_CLASE i:IDENT 
							{String s2 = i.getText();
							 //Propiedad p = new Propiedad();
							 //p.setClaseCont(s);
							 //p.setNombreProp(s2);
							 PropiedadAtrib pa = new PropiedadAtrib();
							 pa.setNombreProp(i.getText());
							 pa.setClaseCont(s);
							}	
							(range[pa])?  (enumr[pa])? (cardinalidad[pa])?)
							{//listaP.addPropiedad(pa);
							listaPropAt.add(pa);}
			| #(PROPIEDAD_COMPLEJA IDENT i1:IDENT {PropiedadAtrib p = new PropiedadAtrib();}
				cardinalidad[p] i2:IDENT)
			;
			
//diferencia [Propiedad p] : #(DATA_PROPERTY {String t;} t=tipoBasico
//			{p.setTipoPropiedad(t);}
//			(cardinalidad)?)
//			|#(OBJECT_PROPERTY i:IDENT {p.setTipoPropiedad(i.getText());}
//			 cardinalidad)
//			;
			
range [PropiedadAtrib p] : #(RANGE resto_range[p])
		;

enumr [PropiedadAtrib p] : #(UNODE resto_enum[p])
		;
		
resto_enum [PropiedadAtrib p] : #(OR {p.setOp("OR");} resto_enum[p] resto_enum[p])
							| #(AND {p.setOp("AND");} resto_enum[p] resto_enum[p])
							| i:IDENT {p.addEnum(i.getText());}
							;		
		
resto_range [PropiedadAtrib p] : #(OR {p.setOp("OR");} resto_range[p] resto_range[p])
							| #(AND {p.setOp("AND");} resto_range[p] resto_range[p])
							| i:IDENT {p.addPropAtrib(i.getText());}
							;
	
tipo : IDENT
		;	
		
relacion : #(RELACION i:IDENT
				{  		  SubClase sc = new SubClase();
						  ArrayList l = sc.getListaPadres();
						  sc.setName(i.getText());
						  sc.setListaPadres(l);
						  listaSc.addSubClass(sc);
						  String a = i.getText();
				} (subclase[sc])? cuerpoRelacion[a]);
				
				
cuerpoRelacion [String a]: #(CUERPO_RELACION (rol[a] | tipoPropiedad[a])*);

rol [String s] : #(ROL { RolAtrib rA = new RolAtrib();} (FULL {rA.setFull(true);})?  i:IDENT
					{
					
					 rA.setNombreRol(i.getText());
					 rA.setRelacionCont(s);
					 listaR.addRolAtrib(rA);
					}
					 (redefinicion_rol[rA])?
					 (cardinalidad_rol[rA])?
					 (INV cardinalidad_Inv[rA])?
					 (peer[rA])?)	 
	;
	
peer [RolAtrib rA]: #(PEER i:IDENT {rA.setPeer(i.getText());})
				;
	
	
redefinicion_rol [RolAtrib r] : #(PLAY clases_juego_ra[r])
							;
							
clases_juego_ra [RolAtrib r] : #(OR {r.setOp("OR");} clases_juego_ra[r] clases_juego_ra[r])
		| #(AND {r.setOp("AND");} clases_juego_ra[r] clases_juego_ra[r])
		| i:IDENT { ArrayList l = r.getNuevosJuegos();
					l.add(i.getText());
					r.setNuevosJuegos(l);}
		;


cardinalidad_rol [RolAtrib r] : #(CARDINALIDAD l1:LIT_ENTERO {r.setQMin(Integer.parseInt(l1.getText())); int max = -1;}
			 (l2:LIT_ENTERO {r.setQMax(Integer.parseInt(l2.getText()));} | ASTERISCO))
			;
			
cardinalidad_Inv [RolAtrib r] : #(CARDINALIDAD l1:LIT_ENTERO {r.setQMinInv(Integer.parseInt(l1.getText())); int max = -1;}
			 (l2:LIT_ENTERO {r.setQMaxInv(Integer.parseInt(l2.getText()));} | ASTERISCO))
			;	
	
juego [Rol r]: #(PLAY restoJuego[r] (cardinalidadRol[r])?)
	;
	
cardinalidadRol [Rol p] : #(CARDINALIDAD l1: LIT_ENTERO
			 { Integer i1 = Integer.parseInt(l1.getText());
			 	p.setQMin(i1.intValue());
			   Integer i2 = null;	
			 }		
			(l2:LIT_ENTERO { i2 = Integer.parseInt(l2.getText());
			 	p.setQMax(i2.intValue());
			}
			 | a:ASTERISCO))
			;	
	
restoJuego [Rol r] : #(OR {r.setOp("OR");} restoJuego[r] restoJuego[r])
		| #(AND {r.setOp("AND");} restoJuego[r] restoJuego[r])
		| i:IDENT { ArrayList l = r.getListaJuegos();
					l.add(i.getText());
					r.setListaJuegos(l);}
		;

claseRol : #(ROL i:IDENT {String s = i.getText();
						  Rol r = new Rol();
						  r.setName(s);
						  SubClase sc = new SubClase();
						  sc.setName(i.getText());
						  ArrayList l = sc.getListaPadres();
						  sc.setListaPadres(l);
						  listaSc.addSubClass(sc);
						  String a = i.getText();}
						  
						  juego[r] propiedades[a]) {listaRol.addRol(r);}
	;




cardinalidad [PropiedadAtrib p] : #(CARDINALIDAD l1: LIT_ENTERO
			 { Integer i1 = Integer.parseInt(l1.getText());
			 	p.setQMin(i1.intValue());
			   Integer i2 = null;	
			 }		
			(l2:LIT_ENTERO { i2 = Integer.parseInt(l2.getText());
			 	p.setQMax(i2.intValue());
			}
			 | a:ASTERISCO))
			;



cardinalidad_prop [PropiedadClase p] : #(CARDINALIDAD l1: LIT_ENTERO
			 { Integer i1 = Integer.parseInt(l1.getText());
			 	p.setQMin(i1.intValue());
			   Integer i2 = null;	
			 }			
			(l2:LIT_ENTERO { i2 = Integer.parseInt(l2.getText());
			 	p.setQMax(i2.intValue());
			}
			 | a:ASTERISCO))
			;
		
claseProp : #(PROPIEDAD {PropiedadClase pb = new PropiedadClase();}
			i:IDENT (rango[pb] | enumerado[pb]) {String nombre = i.getText(); pb.setNombreProp(nombre);} (expreg[pb])? (categoria[pb])? (longitud[pb])? (inversa[pb])?)
			{listaPropCl.add(pb);
			}
		;
		
		
rol_pointer : #(ROLPOINTER {RolPointer rp = new RolPointer();}  i:IDENT (cardinalidad_rolp[rp])? {rp.setNombre(i.getText());} rolb[rp] relb[rp]) {listaRolPointers.add(rp);}
			;

rolb [RolPointer rp] : #(ROL i:IDENT {rp.setRol(i.getText());})
	;

relb [RolPointer rp]: #(RELACION i:IDENT {rp.setRelacion(i.getText());})
	;

cardinalidad_rolp [RolPointer p] : #(CARDINALIDAD l1: LIT_ENTERO
			 { Integer i1 = Integer.parseInt(l1.getText());
			 	p.setQMin(i1.intValue());
			   Integer i2 = null;	
			 }			
			(l2:LIT_ENTERO { i2 = Integer.parseInt(l2.getText());
			 	p.setQMax(i2.intValue());
			}
			 | a:ASTERISCO))
			;
		
enumerado [PropiedadClase p] : #(UNODE restoEnum[p])
		;
		
restoEnum [PropiedadClase p] : #(OR restoEnum[p] restoEnum[p])
			| i:IDENT {p.addEnumerado(i.getText());
						p.addCategoria("OBJECT");}
			;		
		
rango [PropiedadClase p] : #(RANGO resto_rango[p] {p.addCategoria("OBJECT");} (cardinalidad_prop[p])?)
						| #(TIPO {p.addCategoria("DATA");} tipoBasico[p])
		;

tipoBasico [PropiedadClase p] : e:ENTERO {p.addRango(e.getText());} 
		| r:REAL {p.addRango(r.getText());} 
		| c:CADENA {p.addRango(c.getText());} 
		| f:FECHA {p.addRango(f.getText());} 
		| h:HORA {p.addRango(h.getText());} 
		| i:IDENT {p.addRango(i.getText());}
		| m:MEMO {p.addRango(m.getText());} 
		| l:LOGICO {p.addRango(l.getText());} 
		;

resto_rango [PropiedadClase p] : #(OR {p.setOp("OR");}resto_rango[p] resto_rango[p])
							| #(AND {p.setOp("AND");} resto_rango[p] resto_rango[p])
							| i:IDENT {p.addRango(i.getText());}
							| i2: ROL {p.addRango(i2.getText());}
							;
							
expreg [PropiedadClase p] : #(EXPREG i:IDENT {p.setMask(i.getText());})
		;
		
categoria [PropiedadClase p] : #(CATEGORIA {String s1,s2=null;} s1=categoria2 (s2=categoria2)?
							{p.addCategoria(s1); 
								if(s2!=null) p.addCategoria(s2);} )
	;
	
categoria2 returns [String s=null;]: p:PLAY {s = p.getText();}
			| q:STRUCTURAL {s = q.getText();}
			;
			
longitud [PropiedadClase p] : #(LONGITUD l:LIT_ENTERO)
		 {Integer i = Integer.parseInt(l.getText());
		 p.setLongitud(i.intValue());}
		;
	
	
inversa [PropiedadClase p] : #(INVERSA i:IDENT)
		{p.setPropInv(i.getText());}
		;		
		
individuo : #(INDIVIDUO i:IDENT {Individuo ind = new Individuo();
								ind.setName(i.getText());} 
			tipoInd[ind]) {listaIndividuos.add(ind);}
		;

tipoInd [Individuo ind] : #(TIPO ( i:IDENT {ind.setTipo(i.getText());}
									| e: ENTERO {ind.setTipo(e.getText());}
									| r: REAL {ind.setTipo(r.getText());}
									| f: FECHA {ind.setTipo(f.getText());}
									| m: MEMO {ind.setTipo(m.getText());}
									| h: HORA {ind.setTipo(h.getText());}
									| b: LOGICO {ind.setTipo(b.getText());}
									| c: CADENA {ind.setTipo(c.getText());}
							) )
		;