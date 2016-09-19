package dynagent.ruleengine.test.test20.src;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


import dynagent.ruleengine.Constants;
import dynagent.ruleengine.CreateIdProp;
import dynagent.ruleengine.CreateIdo;
import dynagent.ruleengine.CreateIdto;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.Category;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.test.ITest;
import dynagent.server.knowledge.access;

public class Test20 implements ITest {
	private DocDataModel docDataModel=null;
	private Map<Integer,DataAdapter> tbAdapter = new HashMap<Integer,DataAdapter>();

	private Map<Integer,String> tbAdapterNameClass= new HashMap<Integer,String>();
	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException {
		System.out.println("\n\n----------------------TEST 20:  PRUEBAS CODIFICACION ADAPTADOR , ANULADO EL TEST---------------");
	/*	
		//Map<Integer,DataAdapter> tbAdapterCase= new HashMap<Integer,DataAdapter>();
		this.docDataModel=(DocDataModel)ik;
		LinkedList<DataCase> casos=extractCases(11500,null,null,null);
		Iterator<DataCase> it=casos.iterator();
		DataCase caseConcrete = null;
		while (it.hasNext()){
			caseConcrete=it.next();
			int type=typeConcreteCase(caseConcrete, null, null, null);
			System.out.println("\n\n-------------FIN DEL ANALISIS CONCRETO TIPO: "+type+"----------------\n\n");
			//tbAdapter=adapterCase(type, caseConcrete, tbAdapter, null, null, null);
			if (type==999){
				System.err.println("Error en le calculo del tipo de caso");
			}else{
				tbAdapter=adapterCase(type, caseConcrete, tbAdapter, null, null, null);
				
				imprime(tbAdapter);
			}
		}/*
		caseConcrete=new DataCase(11500,11505,11501);
		int type=typeConcreteCase(caseConcrete, null, null, null);
		System.out.println("El tipo es: "+type);*/
		
		
		
	}
	/*private void imprime(Map<Integer, DataAdapter> tbAdapterCase) {
		Set<Integer> keys = tbAdapterCase.keySet();
		Iterator<Integer> it=keys.iterator();
		while (it.hasNext()){
			Integer idoV=it.next();
			String tabla="";
			System.out.println("------CLASE VIRTUAL: "+idoV+"-----NOMBRE: "+tbAdapterNameClass.get(idoV)+" ----");
			DataAdapter da=tbAdapterCase.get(idoV);
			Iterator<Property> itp=da.getPropertyIterator();
			while (itp.hasNext()){
				Property p =itp.next();
				
				tabla=tabla+"IdClaseVirtual="+idoV+" ";
				tabla=tabla+"IdPropVirtual="+p.getIdProp()+" ";
				tabla=tabla+"IdPropOrignal="+da.getOriginalIdProp(p)+" ";
				tabla=tabla+"IdoClassOriginal="+da.getOriginalIdoClass(p)+" \n";
				tabla=tabla+"IdtoClassOriginal="+da.getOriginalIdtoClass(p)+" \n";
				System.out.println(p.toString());
				
			}
			System.out.println(tabla);
			System.out.println("\n");
			System.out.println("\n");
			System.out.println("\n");
		}
	}
	private LinkedList<DataCase> extractCases(Integer tgClass, Integer usertask, Integer userRol, String user) throws NotFoundException {
		LinkedList<DataCase> result=new LinkedList<DataCase>();
		Integer idoRel=null;
		//Iterator<Property> itplayin=docDataModel.getPropertyIterator(tgClass, Constants.IdPROP_PLAYIN, userRol, user, usertask);
		Iterator<Property> itplayin=getPlayIn(tgClass,userRol, user, usertask);
		System.out.println("Entra");
		while (itplayin.hasNext()){
			ObjectProperty playin=(ObjectProperty) itplayin.next();
			if(playin.getValueList()==null){
				System.out.println("Entra x filter");
				LinkedList<ObjectValue> filplayin=playin.getFilterList();
				Iterator<ObjectValue> itfplayin= filplayin.listIterator();
				while(itfplayin.hasNext()){
					ObjectValue filter=itfplayin.next();
					idoRel=filter.getValue();
					result.addAll(extractCasesAux(tgClass,idoRel,usertask,userRol,user));
				}
			}
			else{
				System.out.println("Entra x value");
				LinkedList<ObjectValue> valplayin=playin.getValueList();
				Iterator<ObjectValue> itvplayin= valplayin.listIterator();
				while(itvplayin.hasNext()){
					ObjectValue value=itvplayin.next();
					idoRel=value.getValue();
					result.addAll(extractCasesAux(tgClass,idoRel,usertask,userRol,user));
				}
			}
		}
		return result;
	}

	private LinkedList<DataCase> extractCasesAux(Integer tgClass, Integer idoRel, Integer usertask, Integer userRol, String user) throws NotFoundException {
		LinkedList<DataCase> result=new LinkedList<DataCase>();
		Iterator <Property> itp=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
		while(itp.hasNext()){
			Property p = itp.next();
			Category c = p.getCategory();
			if (c.isPlay()){
				Integer value=null;
				if(((ObjectProperty)p).getValueList()!=null){
					value=((ObjectProperty)p).getValueList().getFirst().getValue();
				}else{
					value=((ObjectProperty)p).getFilterList().getFirst().getValue();
				}
				if (!value.equals(tgClass)){
					DataCase acase= new DataCase(tgClass,idoRel,value);
					result.add(acase);
				}
			}
		}
		return result;
	}
	public int typeConcreteCase(DataCase concreteCase, Integer userRol, String user, Integer usertask) throws NotFoundException{
		
		Integer type=0;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoPeer=concreteCase.getIdoPeer();
		Integer idoRel=concreteCase.getIdoRel();
		Integer cardMinClass= null;
		Integer cardMaxClass=null;
		Integer cardMinPeer= null;
		Integer cardMaxPeer=null;
		boolean hasPropertyRel=false;
		Iterator<Property> it=null;
		LinkedList<Property> propertyRel=new LinkedList<Property>();
		LinkedList<Property> propertyPlayRel = new LinkedList<Property>();
		
		it=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
		
		while (it.hasNext()){
			Property p=it.next();
			Category c = p.getCategory();
			
			if(c.isPlay()){
				Integer idoplay=((ObjectProperty)p).getValueList().iterator().next().getValue();
				//System.out.println(p.toString());
				if (idoplay.equals(idoClass)){
					cardMaxClass=p.getCardMax();
					cardMinClass=p.getCardMin();
					if(cardMaxClass==null)
						cardMaxClass=999;
					if(cardMinClass==null)
						cardMinClass=999;
					propertyPlayRel.add(p);
				}else if(idoplay.equals(idoPeer)){
					cardMaxPeer=p.getCardMax();
					cardMinPeer=p.getCardMin();
					if(cardMaxPeer==null)
						cardMaxPeer=999;
					if(cardMinPeer==null)
						cardMinPeer=999;
					propertyPlayRel.add(p);
				}else{
					propertyPlayRel.add(p);
				}
			}else{
				if(p.getIdProp()!=Constants.IdPROP_RDN)
					propertyRel.add(p);
			}
		}
		if(propertyPlayRel.isEmpty()){
			System.err.println("Error el caso intrducido en la funcion typeConcreteCase no es un caso valido");
			return 999;
		}
		
		//it=propertyRel.iterator();		
		hasPropertyRel=!propertyRel.isEmpty();
		
		boolean searchRel=getSearchRel(userRol, user, usertask, concreteCase);		
		boolean searchPeer=getSearchPeer(userRol,user,usertask, concreteCase);
		
		//Impresion para las pruebas
		System.out.println("\n\n------------------ANALISIS DEL CASO CONCRETO--------------\n\n");
		System.out.print("idoClass="+idoClass);
		//System.out.print(" idtoRol="+idtoRol);
		System.out.println(" idoRel="+idoRel);
		System.out.println(" idoPeer="+idoPeer);
		if(hasPropertyRel){
			System.out.println("Prop. Rel=SI");
		}else{
			System.out.println("Prop. Rel=NO");
		}
		System.out.print(" Part. Rel. Min="+cardMinClass);
		System.out.print(" Part. Rel. Max="+cardMaxClass);
		System.out.print(" Car. Rol. Min="+cardMinPeer);
		System.out.println(" Car. Rol. Max="+cardMaxPeer);
		
		if(searchPeer){
			System.out.print("Bus. Peer=SI");
		}else{
			System.out.print("Bus. Peer=NO");
		}
		if(searchRel){
			System.out.print("Bus. Rel=SI");
		}else{
			System.out.print("Bus. Rel=NO");
		}
		
		//fin codigo lineas de pruebas
		
		
		if (hasPropertyRel){
			if(cardMaxClass==1 && cardMinClass==0){
				
				if(!searchRel){
					type=1;
					if (cardMaxPeer==1 && cardMinPeer==0){
						type=2;
					}
					return type;
				}else{
					type=4;
					return type;
				}
			}else if(cardMaxClass==1 && cardMinClass==1){
			
				if(!searchRel){
					type=2;
					
					if (cardMaxPeer==1 && cardMinPeer==1 && !searchPeer){
						type=3;
					}
					return type;
				}else{
					type=4;
					return type;
				}
			}else{
				type=4;
				if(cardMaxPeer==1 && cardMinPeer==1 && !searchRel){
					type=1;
					return type;
				}
				return type;
			}
		}else{
			
			if(cardMaxClass==1 && cardMinClass==0){
				if (cardMaxPeer==1 && cardMinPeer==1){
					type=1;
					return type;
				}else{
					type=0;
					return type;
				}
			}else if(cardMaxClass==1 && cardMinClass==1){
				type=2;
				
				if(!searchPeer){
					type=3;
				}
				return type;
				
			}else{
				if (cardMaxPeer==1 && cardMinPeer==1){
					type=1;
					return type;
				}else{
					type=4;
					return type;
				}
			}
		}
	
	}
	
	private boolean getSearchPeer(Integer userRol, String user, Integer usertask,DataCase concreteCase) throws NotFoundException {
		boolean result=false;
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator<Property> it=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
		while (it.hasNext()){
			Property p = it.next();
			Category c = p.getCategory();
			LinkedList<ObjectValue> li= null;
			if (p instanceof ObjectProperty){
				if(Constants.isIDIndividual(idoPeer) || Constants.isIDPrototype(idoPeer)){
					li=((ObjectProperty)p).getValueList();
				}else if(Constants.isIDFilter(idoPeer)){
					li=((ObjectProperty)p).getFilterList();
				}else{
					System.err.println("WARNING: Opcion no contemplada - Method: getSearchPeer");
				}
				if(c.isPlay() && li.getFirst().getValue().equals(idoPeer)){
					access accessPeer= p.getTypeAccess();
					if (accessPeer.getSetAccess()){
						result=true;
					}
				}
			}
		}
		return result;
	}
	private boolean getSearchRel(Integer userRol, String user, Integer usertask, DataCase concreteCase) throws NotFoundException {
		boolean result=false;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Iterator<Property> it=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
		Integer idtoRel = docDataModel.getClassOfObject(idoRel);
		while(it.hasNext()){
			Property p = it.next();
			Category c=p.getCategory();
			LinkedList<ObjectValue> li= null;
			LinkedList<Integer> lr= null;
			if(p instanceof ObjectProperty){
				lr=((ObjectProperty)p).getRangoList();
				if (lr.contains(idtoRel)){
					if(Constants.isIDIndividual(idoRel) || Constants.isIDPrototype(idoRel)){
						li=((ObjectProperty)p).getValueList();
					}else if(Constants.isIDFilter(idoRel)){
						li=((ObjectProperty)p).getFilterList();
					}else{
						System.err.println("WARNING: Opcion no contemplada - Method: getSearchRel");
					}
					if(c.isPlayIn() && li.contains(idoRel)){
						access accessRel= p.getTypeAccess();
						if (accessRel.getSetAccess()){
							result=true;
						}
					}
				}
			}
						
		}
		return result;
	}
	private Map<Integer,DataAdapter> adapterCase(int type, DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		
		switch(type){
		case(0):return adapterCaseA(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(1):return adapterCaseB(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(2):return adapterCaseC(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(3):return adapterCaseD(concreteCase,tbAdapterCase,userRol, user, usertask);
		case(4):return adapterCaseE(concreteCase,tbAdapterCase,userRol, user, usertask);
		default:return null;
		}
		
	}
	private Map<Integer,DataAdapter> adapterCaseE(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) {
		// TODO Auto-generated method stub
		return tbAdapterCase;
	}
	private Map<Integer,DataAdapter> adapterCaseD(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) {
		// TODO Auto-generated method stub
		return tbAdapterCase;
	}
	private Map<Integer,DataAdapter> adapterCaseC(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer	idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		
		//Miramos si la relación a la que apunta es un filtro, con lo que sabemos que
		//en ese caso la cardinalidad será 0..1 con lo cual es opcional las properties de rol,
		//podemos deducirlo puesto que sino apuntaria a un prototipo o induviduo
		//en este caso aún no se a creado la relación.
		
		if(Constants.isIDFilter(idoRel)){
			itProperty=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Empezaremos tratando las properties de la clase principal junto con la relacion.
				String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
				tbAdapterNameClass.put(idoVirtual, nameVirtual);
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,"",false);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			//iteramos en las properties de relación, si tiene.
			itProperty=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
			while(itProperty.hasNext()){
				Integer idGOPS=idGroup++;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameOPS="";
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
				}else{
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
				}
			}
			//Tratamos el peer.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,true);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=docDataModel.getAllPropertyIterator(idoPeer, userRol, user, usertask);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			
			
		//Caso en el que la relación sea un individuo o prototipo en concreto.
			tbAdapterCase.put(idoVirtualPeer, da);
		}else{
			
			itProperty=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtual, nameVirtual);
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Tratamos primero la clase principal, agrupandolo cn las properties de rol, junto con la relacion
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
					}
				}
				
			}

			//iteramos en las properties de relación, si tiene.
			itProperty=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
			while(itProperty.hasNext()){
				Integer idGOPS=idGroup++;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameOPS="";
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
				}else{
					da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
				}
			}
			//Tratamos el peer.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,true);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=docDataModel.getAllPropertyIterator(idoPeer, userRol, user, usertask);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		return tbAdapterCase;
	}
	private Map<Integer,DataAdapter> adapterCaseB(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer	idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		
		//Miramos si la relación a la que apunta es un filtro, con lo que sabemos que
		//en ese caso la cardinalidad será 0..1 con lo cual es opcional las properties de rol,
		//podemos deducirlo puesto que sino apuntaria a un prototipo o induviduo
		//en este caso aún no se a creado la relación.
		
		if(Constants.isIDFilter(idoRel)){
			itProperty=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Empezaremos tratando las properties de la clase principal.
				String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
				tbAdapterNameClass.put(idoVirtual, nameVirtual);
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,"",false);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			//Tratamos la agrupación del peer con la relación.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,true);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=docDataModel.getAllPropertyIterator(idoPeer, userRol, user, usertask);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			
			//iteramos en las properties de relación, si tiene.
			itProperty=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
			while(itProperty.hasNext()){
				Integer idGOPS=idGroup++;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameOPS="";
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
				}else{
					da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
				}
			}
		//Caso en el que la relación sea un individuo o prototipo en concreto.
			tbAdapterCase.put(idoVirtualPeer, da);
		}else{
			
			itProperty=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtual, nameVirtual);
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				//Tratamos primero la clase principal, agrupandolo cn las properties de rol
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			//Tratamos la agrupación del peer con la relación.
			idGroup=1;
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,true);
			tbAdapterCase.put(idoVirtual, da);		
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			//iteramos primero sobre el peer
			itProperty=docDataModel.getAllPropertyIterator(idoPeer, userRol, user, usertask);
			while(itProperty.hasNext()){
				Property p=itProperty.next();
				if (p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}else{
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					Integer value=((ObjectProperty)p).getValueList().getFirst().getValue();
					boolean hasPlayIn=hasPlayIn(value,userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
					}
				}
				
			}
			
			//iteramos en las properties de relación, si tiene.
			itProperty=docDataModel.getAllPropertyIterator(idoRel, userRol, user, usertask);
			while(itProperty.hasNext()){
				Integer idGOPS=idGroup++;
				//TODO hay que buscar como sacar el nombre de la relacion mediante el playin o play
				String nameOPS="";
				Property p = itProperty.next();
				if (p instanceof ObjectProperty){
					if(!((ObjectProperty)p).getCategory().isPlay())
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
				}else{
					da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
				}
			}
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		return tbAdapterCase;
	}
	private Map<Integer,DataAdapter> adapterCaseA(DataCase concreteCase, Map<Integer,DataAdapter> tbAdapterCase,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Integer idGroup=1;
		Integer idoClass=concreteCase.getIdoClass();
		Integer idoRel=concreteCase.getIdoRel();
		Integer idoPeer=concreteCase.getIdoPeer();
		Iterator <Property> itProperty=null;
		//Caso en que la relacion es un filtro
		
		if(Constants.isIDFilter(idoRel)){
			//Tratamos la clase principal
			itProperty=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,true);
					}else{
						//TODO pensar en el nombre del ROL
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,"",false);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					boolean hasPlayIn=hasPlayIn(((ObjectProperty)p).getValueList().getFirst().getValue(),userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(((ObjectProperty)p).getValueList().getFirst().getValue(), userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,true);
				}
			}
			idGroup=1;
			//Tratamos la clase del Peer.
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,false);
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtual, nameVirtual);
			tbAdapterCase.put(idoVirtual, da);
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			itProperty=docDataModel.getAllPropertyIterator(idoPeer, userRol, user, usertask);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}else{
						//TODO pensar en el nombre del ROL
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,"",false);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					boolean hasPlayIn=hasPlayIn(((ObjectProperty)p).getValueList().getFirst().getValue(),userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(((ObjectProperty)p).getValueList().getFirst().getValue(), userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
				}
			}
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			tbAdapterCase.put(idoVirtualPeer, da);
			
		}
		//Caso en el que la relación es un Prototipo o individuo (Deja de ser optativo la relacion, por tanto los
		//atributos de rol pasan a ser obligatorios)
		else{
			//Tratamos el caso de la clase principal
			itProperty=docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
			Integer idoVirtual=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtual=CreateIdto.getInstance().newIdto();
			DataAdapter da=new DataAdapter(idoVirtual,idtoVirtual);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,null,null,true);
					}else{
						//TODO pensar en el nombre del ROL
						da=createDataPropertyVirtual(p,idoClass,idoVirtual,idtoVirtual,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					boolean hasPlayIn=hasPlayIn(((ObjectProperty)p).getValueList().getFirst().getValue(),userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(((ObjectProperty)p).getValueList().getFirst().getValue(), userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtual,idtoVirtual,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtual,idtoVirtual,da,null,null,true);
				}
			}
			idGroup=1;
			//Tratamos el Peer
			Integer idoVirtualPeer=CreateIdo.getInstance().newIdoVirtual();
			Integer idtoVirtualPeer=CreateIdto.getInstance().newIdto();
			da=createOPVRel(idoVirtual,idtoVirtual,da,idoVirtualPeer,null,null,true);
			String nameVirtual= getNameVirtualClass(idoClass,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtual, nameVirtual);
			tbAdapterCase.put(idoVirtual, da);
			da=new DataAdapter(idoVirtualPeer,idtoVirtualPeer);
			itProperty=docDataModel.getAllPropertyIterator(idoPeer, userRol, user, usertask);
			while (itProperty.hasNext()){
				Property p=itProperty.next();
				if(p instanceof DataProperty){
					if (p.getRol()==null){
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}else{
						//TODO pensar en el nombre del ROL
						da=createDataPropertyVirtual(p,idoPeer,idoVirtualPeer,idtoVirtualPeer,da,idGroup,"",true);
					}
				}else if(!p.getCategory().isPlayIn() && p.getCardMax()==1 && p.getCardMin()==1){
					Integer idGOPS=idGroup++;
					String nameOPS=p.getName();
					boolean hasPlayIn=hasPlayIn(((ObjectProperty)p).getValueList().getFirst().getValue(),userRol,user,usertask);
					if(!hasPlayIn){
						Iterator<Property> itaux=docDataModel.getAllPropertyIterator(((ObjectProperty)p).getValueList().getFirst().getValue(), userRol, user, usertask);
						while(itaux.hasNext()){
							DataProperty dp=(DataProperty)itaux.next();
							da=createObjectPropertyVirtual(dp,idoVirtualPeer,idtoVirtualPeer,da,idGOPS,nameOPS,true);
						}
					}else{
						da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
					}
				}else{
					da=createObjectPropertyVirtual(p,idoVirtualPeer,idtoVirtualPeer,da,null,null,true);
				}
			}
			String nameVirtualPeer= getNameVirtualClass(idoPeer,userRol,user,usertask);
			tbAdapterNameClass.put(idoVirtualPeer, nameVirtualPeer);
			tbAdapterCase.put(idoVirtualPeer, da);
		}
		return tbAdapterCase;
	}
	private String getNameVirtualClass(Integer idoClass, Integer userRol, String user, Integer usertask) throws NotFoundException {
		String result="";
		//Iterator<Property> it= docDataModel.getAllPropertyIterator(idoClass, userRol, user, usertask);
		Iterator<Property> it= getPlayIn(idoClass,userRol, user, usertask);
		while (it.hasNext()){
			ObjectProperty p= (ObjectProperty)it.next();
			//if (p instanceof ObjectProperty){
				//Category c = p.getCategory();
				//if (c.isPlayIn()){
					result=docDataModel.getClassName(p.getRolB());
					return result;
				//}
			//}
		}
		return result;
	}
	private boolean hasPlayIn(Integer value,Integer userRol, String user, Integer usertask) throws NotFoundException {
		Iterator<Property> itaux=docDataModel.getAllPropertyIterator(value, userRol, user, usertask);
		boolean hasPlayIn=false;
		while(itaux.hasNext()){
			Property paux=itaux.next();
			if(paux instanceof ObjectProperty){
				if(paux.getCategory().isPlayIn())
					hasPlayIn=true;
			}
		}
		return hasPlayIn;
	}
	public String getClassName(Integer idto) throws NotFoundException {
		String result="";
		if (tbAdapterNameClass.containsKey(idto)){
			result=tbAdapterNameClass.get(idto);
			return result;
		}
		result= docDataModel.getClassName(idto);
		return result;
	}
	private DataAdapter createOPVRel(Integer idoVirtual, Integer idtoVirtual, DataAdapter da, Integer idoVirtualPeer,Integer idGroup, String nameGroup, boolean required) {
		Integer idPropVirtual=CreateIdProp.getInstance().newIdProp();
		ObjectPropertyVirtual dpv=new ObjectPropertyVirtual();
		if(idGroup!=null)
			dpv.setIdGroup(idGroup);
		if(nameGroup!=null)
			dpv.setNameGroup(nameGroup);
		if(required==false)
			dpv.setRequired(required);		
		LinkedList<ObjectValue> vlist= new LinkedList<ObjectValue>();
		ObjectValue ov= new ObjectValue();
		ov.setValue(idoVirtualPeer);
		ov.setValueCls(idoVirtualPeer);
		vlist.add(ov);
		dpv.setValueList(vlist);
		dpv.setName("VIRTUAL");
		dpv.setIdto(idtoVirtual);
		dpv.setIdo(idoVirtual);
		dpv.setIdProp(idPropVirtual);
		da.setPropertyAdapter(dpv, idPropVirtual, idoVirtual,idtoVirtual);
		return da;
		
	}
	
	
	private DataAdapter createDataPropertyVirtual(Property p, Integer idoClass,Integer idoVirtual,Integer idtoVirtual, DataAdapter da, Integer idGroup, String nameGroup, boolean required) {
		Integer idPropVirtual=CreateIdProp.getInstance().newIdProp();
		
		Integer idoOriginal=p.getIdo();
		Integer idPropOriginal=p.getIdProp();
		Integer idtoOriginal=p.getIdto();
		p.setIdo(idoVirtual);
		if(idPropOriginal==2 && idoOriginal.equals(idoClass)){
			
		}else{
			p.setIdProp(idPropVirtual);
		}
		p.setIdto(idtoVirtual);
		DataPropertyVirtual dpv=new DataPropertyVirtual((DataProperty)p);
		if(idGroup!=null)
			dpv.setIdGroup(idGroup);
		if(nameGroup!=null)
			dpv.setNameGroup(nameGroup);
		if(required==false)
			dpv.setRequired(required);
		da.setPropertyAdapter(dpv, idPropOriginal, idoOriginal,idtoOriginal);
		return da;
	}
	
	private DataAdapter createObjectPropertyVirtual(Property p, Integer idoVirtual,Integer idtoVirtual, DataAdapter da, Integer idGroup, String nameGroup,boolean required) {
		Integer idPropVirtual=CreateIdProp.getInstance().newIdProp();
		Integer idoOriginal=p.getIdo();
		Integer idPropOriginal=p.getIdProp();
		Integer idtoOriginal=p.getIdto();
		p.setIdo(idoVirtual);
		p.setIdProp(idPropVirtual);
		p.setIdto(idtoVirtual);
		ObjectPropertyVirtual dpv=new ObjectPropertyVirtual((ObjectProperty)p);
		if(idGroup!=null)
			dpv.setIdGroup(idGroup);
		if(nameGroup!=null)
			dpv.setNameGroup(nameGroup);
		if(required==false)
			dpv.setRequired(required);
		da.setPropertyAdapter(dpv, idPropOriginal, idoOriginal,idtoOriginal);
		return da;
	}*/
	
}
