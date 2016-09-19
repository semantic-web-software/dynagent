package dynagent.server.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class paytef {
	String code_lector="*";
	String ip="localhost";
	int port=8888;
	int timeoutmillis=240000;

	public paytef_response order(String[] param){		
		String bnsref=param[0];
		double importe=new Double(param[1]);
		if(importe==0.0) return null;		
		
		paytef_response res=null;
		try{
		if(importe>0)	res=venta(bnsref,importe);
		else res=devolucion(bnsref,param[3],-importe);
		}catch(Exception e){
			e.printStackTrace();
			res= new paytef_response();
			res.fallida=true;
			res.comentario="Fallo previo";
			res.ARC="";
		}
		return res;
	}
	paytef_response venta(String business_ref, double importe){
		return transaction(business_ref,null,importe, false);
	}
	
	paytef_response devolucion(String business_ref, String paytefref, double importe){
		//return transaction(business_ref,"###"+paytefref,importe, true);
		return transaction(business_ref,null,importe, true);//voy a exigir siempre insertar la tarjeta, no refenciando operacion anterior
	}
	
	paytef_response transaction(String business_ref, String paytefref, double importe, boolean devolucion) {
		char[] msg=new char[256];
		Arrays.fill(msg,' ');
		insert(msg,"#EMV01#",1,7);
		insert(msg,devolucion?"D":"V",8,8);
		
		insert(msg,code_lector,9,18);
		
		if(devolucion&&paytefref!=null){
			insert(msg,paytefref,19,38);//en posicion numero tarjeta meto la referencia paytef	
		}
		String strimporte=""+Math.round(importe*100);	
		strimporte="0000000000".substring(0,10-strimporte.length())+strimporte;
		insert(msg,strimporte,153,162);
		insert(msg,business_ref,163,246);
		
		// then decode those bytes as US-ASCII
		//Charset.forName("US-ASCII").encode(new String(msg));
		byte[] asciimsg = new String(msg).getBytes(Charset.forName("US-ASCII"));
	
		
		//String msgstr=new String();
		//for(int i=0;i<256;i++){
		//	msgstr
		//}
		
		String res= connect(asciimsg);
		return parse_response(res);
	}	
	
	paytef_response parse_response(String str){
		paytef_response res=new paytef_response();
		
		res.tipo=res(str,8,8).equals("V")?"Venta":"Devolucion";
		res.codigo_comercio=res(str,9,28);
		res.fecha=res(str,39,46)+" "+res(str,47,50);
		
		String resultado=res(str,51,51);
		res.aceptada=resultado.equals("A");
		res.fallida=resultado.equals("F");
		
		res.paytefref=res(str,72,81);
		res.contactless=res(str,82,82).equals("S");
		String strimporte=res(str,92,101);
		if(res.fallida||!res.aceptada) res.importe=0.0;
		else res.importe=Double.parseDouble(strimporte)/100;
		res.comentario=res(str,123,172);
		res.moneda="€";
		res.num_tarjeta=res(str,102,121);
		res.firma=res(str,122,122).equals("S");
		res.ARC=res(str,174,175);
		res.HCP=res(str,178,197);
		res.idapp=res(str,198,217);
		res.app=res(str,218,237);
		return res;
	}
	String res(String msg,int ini, int end){
		String sub=msg.substring(ini-1,end).trim();
		if(sub.length()==0) return " ";
		else return sub;
	}
	
	void insert(char[] msg,String val,int from, int to){
		for(int i=from-1;i<to;i++){
			int posval=i-(from-1);
			if(posval>val.length()-1) msg[i]=' ';
			else msg[i]=val.charAt(posval);
		}
	}
	
	String connect(byte[] msg){
		OutputStream output=null;
		InputStreamReader in=null;
		Socket skt = null;
		String res="";
	   try {	          	          
		   skt= new Socket(ip, port);
		   skt.setSoTimeout(timeoutmillis);
	       output = skt.getOutputStream();
	       output.write(msg);
	          
	       in =new    InputStreamReader(skt.getInputStream(),Charset.forName("US-ASCII"));

		   char[] chres=new char[256];
		   in.read(chres,0,255);		    
		   res=new String(chres);
		   System.out.println(res);		   	     
		}
		catch(Exception e) {
		     System.out.print("Whoops! It didn't work!\n");
		     
		}finally{			
				try {					
					if(output!=null) output.close();
					if(in!=null) in.close();
					skt.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
	   return res;
	}
}
