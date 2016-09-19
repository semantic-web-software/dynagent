package dynagent.ruleengine;

import dynagent.common.utils.Auxiliar;

/**
 * Main.java
 * @author Ildefonso
 * @description Test RuleEngine Interface.
 */
public class Main {
	
	public static void main(String[] args) {
		
			String argumento=Auxiliar.leeTexto("parametroPrueba:");
			if(argumento.matches("[a-zA-Z]+[1-9]\\d?")){
				System.out.println("\n.... cumple el patron");
				System.out.println("serie:"+argumento.substring(0,1));
				System.out.println("porcentaje:"+argumento.substring(1,argumento.length()));	
				
			}
					
		
	}
}


