package dynagent.ruleengine.test.test6.src;

import java.util.ArrayList;

import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.auxiliar.Auxiliar;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;
import dynagent.ruleengine.test.test5.src.Test5;
import dynagent.ruleengine.Constants;

public class Test6 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException {
		
		if(SessionController.getInstance()!=null){
			SessionController.getInstance().setActual(new DefaultSession(null,null));
		}
	
		DocDataModel ddm=(DocDataModel)ik; 
		System.out.println("\n\n----------------------TEST 6:  PRUEBA DE REGLAS-----------------------");
		 /*String path=Auxiliar.leeTexto("Introduzca el arbol que desea crear (NAMECLASE.propiedad1.propiedad2.....)");
		 if(path.contains(".")){
			 String  [] propiedades=path.split(".");
			 String nameClase=propiedades[0];	
			 int idto=ddm.getIdClass(nameClase);
			 int id=ddm.creaIndividualOfClass(idto,Constants.LEVEL_INDIVIDUAL);
			 for(int i=1;i<propiedades.length;i++){
				String propiedad=propiedades[i];
				int idProp=ddm.getIdProperty(propiedad);
				if(ddm.isObjectProperty(idProp)){
					ddm.setValue_DeduceOperation(id, propiedad, "any");
				}
					
				
				}
				}
		 }	
		 */
		
		
		/*System.out.println("##"+ddm.getDirectSpecialized(ddm.getIdClass("DOCUMENTO_COMERCIAL")));
		System.out.println("##2"+ddm.getSpecializedHS(ddm.getIdClass("DOCUMENTO_COMERCIAL")));
		System.out.println("##111-->"+ddm.getSuperiorHS(111));
		System.out.println("##111-->"+ddm.getDirectSuperior(111));
		*/
		
		
		
		this.testTotalesDocComercial(ddm);
		//this.testLineaArticulos(ddm);
		Test5 test5=new Test5();
		test5.solicitaInfo(ddm);
		
	}
	
	
	
	public void testLineaArticulos(DocDataModel ddm)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException {
		//	REGLAS LINEAS_ARTICULOS
		//Este test introduce datos para que se disparen las reglas de:
		//-iva por defecto linea; -aplicaRetenci�n por defecto; -descuento de la l�nea ; importe de la l�nea
		System.out.println("..........................testLineaArticulos");
		
		int idDoc=ddm.creaIndividualOfClass("PEDIDO_DE_CLIENTE", Constants.LEVEL_INDIVIDUAL);
		int idCliente=ddm.creaIndividuoWithAllPropiedades("CLIENTE");
		int idLinea=ddm.creaIndividualOfClass("L�NEA_ARTICULOS", Constants.LEVEL_INDIVIDUAL);
		
		
		int sujeto=idCliente;
		//System.err.println("\n\n=================CALCULO VALORES POR DEFECTO DOCUMENTO COMERCIAL");
		
		/*ddm.setDefaultValue(idDoc,sujeto,"transportista");
		ddm.setDefaultValue(idDoc,sujeto,"divisa");
		ddm.setDefaultValue(idDoc,sujeto,"forma_pago");
		ddm.setDefaultValue(idDoc,sujeto,"porcentaje_retenci�n");
		*/
		
		
		
		int idproducto=ddm.creaIndividuoWithAllPropiedades("PRODUCTO");
//		asignamos el producto a la l�nea
		ddm.setValue(idLinea, "producto", String.valueOf(idproducto));
//      asignamos a la linea un descuento y una cantidad.
		ddm.setValue(idLinea, "cantidad", "any");
		ddm.setValue(idLinea, "descuento", "any");
		
		//asignamos la l�nea al pedido
		ddm.setValue(idDoc, "linea", String.valueOf(idLinea));
//		asignamos el cliente al pedido
		ddm.setValue(idDoc, "cliente", String.valueOf(idCliente));
		
	
		/*Property ivap=ddm.getProperty(idproducto,"iva");
		Property ival=ddm.getProperty(idLinea, "iva");
		ddm.setValue_DeduceOperation(idLinea,"iva",ivap.getValue());
		*/
	}

	
	
	public void testTotalesDocComercial(DocDataModel ddm)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException {
		//	REGLA TOTALES DOCUMENTO COMERCIAL
		System.out.println("..........................testTotalesDocComercial");
		System.out.println("..........................se crea un pedido_de_cliente");
		int idDoc=ddm.creaIndividualOfClass("PEDIDO_DE_CLIENTE", Constants.LEVEL_INDIVIDUAL);
		System.out.println("..........................se crea un cliente con todas sus propiedades");
		
		int idCliente=ddm.creaIndividuoWithAllPropiedades("CLIENTE");
		//asignamos el cliente al pedido
		System.out.println(".........................asignamos el cliente creado al pedido_de_cliente");
		
		ddm.setValue(idDoc, "cliente", String.valueOf(idCliente));
		System.out.println(".........................creamos una linea articulos con todas sus propiedades");	 	
		int idLinea=ddm.creaIndividuoWithAllPropiedades("L�NEA_ARTICULOS");
		//asignamos iva a la l�nea
		System.out.println(".........................asignamos la l�nea al pedido");
		//asignamos la l�nea al pedido
		ddm.setValue(idDoc, "linea", String.valueOf(idLinea));
		System.out.println(".........................modificamos el porcentaje de retenci�n del pedido");
		ddm.setValue(idDoc, "porcentaje_retenci�n", "any");
		
		ddm.setValue(idDoc, "porcentaje_retenci�n", "any");
		
		
		
	/*	Double porcRet=ddm.numValue(idDoc, "porcentaje_retenci�n");
		Property linea=ddm.getProperty(idDoc,"linea");
		ArrayList<Double> importes= new ArrayList<Double> ();
		ArrayList<Double> recargos= new ArrayList<Double> ();
		ArrayList<Double> retenciones= new ArrayList<Double> ();
	 	ArrayList<Double> ivas=new ArrayList<Double> ();
	 	
	 	Double base = 0.0d,totaliva =  0.0d,totalretencion =  0.0d,importetotal =  0.0d,totalrecargo=0.0;
		//COMPROBAMOS QUE NO SE HA BORRADO UN VALOR INDISPENSABLE, BORRAR UNA L�NEA NO ES PROBLEMA PQ NO SE TIENE EN CUENTA ESA L�NEA PERO BORRAR IMPORTE O IVA LINEA , o sujeto SI LO ES
		boolean reset=false;/*f1.isTemporalDeleted()||f2.isTemporalDeleted()&&f3.isTemporalDeleted()||f4.isTemporalDeleted()||f5.isTemporalDeleted()||f6.isTemporalDeleted()||f7.isTemporalDeleted()
		 			 ||f8.isTemporalDeleted();*/ 			
		/*if(!reset){
			boolean recargoEquivalencia=regIva!=null&&regIva.equals("Recargo_Equivalencia");
			for(int i=0;i<linea.getValues().size();i++){
				ObjectValue ov=(ObjectValue)linea.getValues().get(i);
				int idolinea=ov.getValue().intValue();
			    Double importe=ddm.numValue(idolinea,"importe");
			    importes.add(importe);
			    Double iva=ddm.numValue(idolinea,"iva.porcentaje_iva")*importe;
			    ivas.add(iva);
			    if(recargoEquivalencia){
			    		Double recargo=ddm.numValue(idolinea,"iva.porcentaje_recargo")*importe;
			    		recargos.add(recargo);
			    }
			  
			    Double retencion=ddm.numValue(idolinea,"aplica_retenci�n")*importe*porcRet;
			    retenciones.add(retencion);
			}
			base=Auxiliar.SUM(importes);
			totaliva=Auxiliar.SUM(ivas);
			totalretencion=Auxiliar.SUM(retenciones);
			totalrecargo=Auxiliar.SUM(recargos);
			importetotal=base+totaliva-totalretencion+totalrecargo;
			System.err.println("calculados:  base="+base+"  totaliva="+totaliva+"   totalretencion="+totalretencion+"   importe="+importetotal);
		}
		else{
			System.err.println("\n\n............................Se borr� dato fundamental para c�lculos, se resetean los totales");
		}
		ddm.setValue(idDoc,"base",base);
		ddm.setValue(idDoc,"total_iva",totaliva);
		ddm.setValue(idDoc,"retenci�n",totalretencion);
		ddm.setValue(idDoc,"importe",importetotal);
		ddm.setValue(idDoc,"recargo",totalrecargo);
		*/
		}
			
	}



