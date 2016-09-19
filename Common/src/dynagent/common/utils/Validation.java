/***
 * Validation.java
 * 
 * @author  Jose Antonio Zamora -jazamora@ugr.es
 * @description Clase con métodos auxiliares de validaciones de campos (nif,numeros cuentas,....		
 */




package dynagent.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Validation {
	
	
	/**
	    * Comprueba que el valor que se le pasa sea un número, letra o guión
	    * @param cif cadena que va a ser controlada
	    * @return boolean En función del resultado, retorna true si se cumple o false si la comprobación es incorrecta
	    */
	    
	    public  static  boolean isNotCaracteresCif(String cif) 
	    {
	    	Pattern patron = null;
	    	Matcher matcher = null;
	    	// Cif sin guión
	    	String cadena = cif.replaceAll("-", "");
	    	 
	    	char[] caracteres = cadena.toCharArray();
	    	 
	    	// Los caracteres deben ser alfanuméricos
	    	patron = Pattern.compile("[A-Z|0-9]");        
	    	for (int i = 0; i < caracteres.length; i++)
	    	{
	    		matcher = patron.matcher(Character.toString(caracteres[i]));
	    		if (!matcher.find())
	    		{            	
	    			return true;
	    		}
	    	}        
	    	
	    	// La longitud debe ser 9
	        if (cadena.length() != 9)
	        {         	
	        	return true;        	
	        }    
	    	
	        // Comprobar que todos los caracteres (salvo los extremos) son numeros
	    	patron = Pattern.compile("[0-9]");        
	    	for (int i = 1; i < caracteres.length-1; i++)
	    	{
	    		matcher = patron.matcher(Character.toString(caracteres[i]));
	    		if (!matcher.find())
	    		{            	
	    			return true;
	    		}
	    	}        

	        return false;      
	       
	    }
	
	public static boolean isNIF(String nif)    {
		Matcher matcher = null;
        //letras correctas para un nif
        String caracteres = "TRWAGMYFPDXBNJZSQVHLCKE";
        // quito espacios en blanco y guion si lo hay
        nif = nif.replaceAll("-","").replaceAll(" ","").toUpperCase().trim();
        //calculo la longitud
        int longitud_nif = nif.length();
        String expresion="[TRWAGMYFPDXBNJZSQVHLCKE]";
        Pattern patron = null;
        Matcher coincidencias_inicio = null;
        Matcher coincidencias_fin = null;
        patron = Pattern.compile(expresion);
        String inicio=nif.substring(0,1);
        String fin=nif.substring(longitud_nif - 1);
        coincidencias_inicio = patron.matcher(inicio);
        coincidencias_fin = patron.matcher(fin);
        
        if (Validation.isNotCaracteresCif(nif))
        {
            return false;        	
        }

        /**************** nif x9999999-x y empieza por X o L ***************/
        if ((nif.startsWith("X") || nif.startsWith("x") || nif.startsWith("L") || nif.startsWith("l") ) && coincidencias_fin.find())
        {
        	//guardo el nif sin la letra
            String nif1 = nif.substring(1,longitud_nif-1);
            //algoritmo
            int nif2 =  Integer.parseInt(nif1);
            int posicion = (nif2 % 23)+1;
            String caracter = caracteres.substring(posicion-1,posicion);
            if (caracter.compareToIgnoreCase(fin) == 0)
            {
            	return true;
            }
            return false;                        
        }
        /************** nif x-9999999 ************************/
        else if (coincidencias_inicio.find())
        {
            //guardo el nif sin la letra
            String nif1 = nif.substring(1,longitud_nif);
            //algoritmo
            int nif2 =  Integer.parseInt(nif1);
            int posicion = (nif2 % 23)+1;
            String caracter = caracteres.substring(posicion-1,posicion);
            if (caracter.compareToIgnoreCase(inicio) == 0)
            {
            	return true;
            }
            return false;
        }
        /************** nif 9999999-x ************************/
        else if (coincidencias_fin.find())
        {
            //guardo el nif sin la letra
            String nif1 = nif.substring(0,longitud_nif-1);
            //algoritmo
            int nif2 =  Integer.parseInt(nif1);
            int posicion = (nif2 % 23)+1;
            String caracter = caracteres.substring(posicion-1,posicion);
            if (caracter.compareToIgnoreCase(fin) == 0)
            {
            	return true;
            }
            return false;
        }
        return false;
    }//fin metodo comprueba nif
	
	public static boolean checkWithFunction(String mask,String value){
		if(mask.equalsIgnoreCase("ISNIF")){
			
			return Validation.isNIF(value);
		}
		//TODO A CADA FUNCIÓN NUEVA QUE SE AÑADA AÑADIR UNA CONDICIÓN QUE LA LLAME
		else{
			return false;
		}
	}
	
	

}
