package dynagent.common.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;



public class CipherUtils
{


    private static byte[] key = {
            0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x44, 0x79, 0x6e, 0x61, 0x47, 0x65, 0x6e, 0x74, 0x4B, 0x79
    };//"thisIsDynaGentKy";//La clave es convertida a hexadecimal. Tiene que tener justo este tamaño, 16 bytes

    public static String encrypt(String strToEncrypt) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        final String encryptedString = Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes()));
        return encryptedString;

    }

    public static String decrypt(String strToDecrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
        return decryptedString;
    }
    
    public static void main(String args[]) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
    {

//	    String strToEncrypt="16";
//	    String strEncrypted=CipherUtils.encrypt(strToEncrypt);
//	    
//	    String strDecrypted=CipherUtils.decrypt(strEncrypted);
//	    
//	    System.err.println("strEncrypted:"+strEncrypted);
//	    System.err.println("strDecrypted:"+strDecrypted);
//	    
//	    String strDecrypted1=CipherUtils.decrypt("EwVv12glpoQJhDEpuTK6Kg==");
//	    System.err.println("strDecrypted1:"+strDecrypted1);
    	
//    	String strToEncrypt="16";
//	    String strEncrypted=CipherUtils.encrypt(strToEncrypt);
//	    
//	    System.err.println("Delegacion y Almacen:"+strToEncrypt+" Codigo:"+strEncrypted);
    	
    	
    	//for(int i=60;i<61;i++){
    		String strToEncrypt="507";
    	    String strEncrypted=CipherUtils.encrypt(strToEncrypt);    	    
    	    
    	    System.err.println("Encriptado:"+strToEncrypt+" Codigo:"+strEncrypted);
    	    System.err.println("Decode:"+CipherUtils.decrypt(strEncrypted));
    	//}
    	
    }
}
