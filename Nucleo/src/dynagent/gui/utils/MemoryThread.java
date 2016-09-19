package dynagent.gui.utils;

import java.awt.KeyboardFocusManager;

import javax.swing.SwingUtilities;

import dynagent.common.utils.Utils;
import dynagent.gui.Singleton;

public class MemoryThread extends Thread{

	boolean showMessage;
	int milliseconds;
	boolean executeGarbageCollector;
	
	public MemoryThread(){
		showMessage=true;
		executeGarbageCollector=true;
		milliseconds=30000;
	}

	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(milliseconds);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Runtime s_runtime = Runtime.getRuntime ();
			long usedMemory = s_runtime.totalMemory () - s_runtime.freeMemory ();
			long maxMemory = s_runtime.maxMemory();
			
			//System.err.println("usedMemory:"+usedMemory/(1024*1024)+"m maxMemory:"+maxMemory/(1024*1024)+"m");
			
			if(showMessage && !Singleton.getInstance().isProcessingCopyRowTable()/*Evitamos que salga mensaje de memoria cuando se esta haciendo un pegado masivo de filas ya que la ventana pararia el pegado*/){
				if(maxMemory*0.85<usedMemory){
					Utils.forceGarbageCollector();
					String message="ATENCIÓN: La aplicación está llegando al máximo de memoria permitido. Para evitar problemas, es recomendable que cierre alguna de las ventanas o resetee las búsquedas para liberar memoria";
					Singleton.getInstance().getMessagesControl().showErrorMessage(message, KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
					showMessage=false;
					System.err.println("La memoria ha superado el 85% con "+Singleton.getInstance().getNumberOfKnowledgeBaseAdapter()+" motores y memoria maxima "+maxMemory/(1024*1024));
					Exception e=new Exception("MEMORIA MAXIMA SOBREPASADA TENIENDO "+Singleton.getInstance().getNumberOfKnowledgeBaseAdapter()+" MOTORES Y MEMORIA MAXIMA "+maxMemory/(1024*1024));
					Singleton.getInstance().getComm().logError(null,e, null);
				}else if(executeGarbageCollector && maxMemory*0.75<usedMemory){//Ejecutamos el liberador cuando este en 0.75 para que no llegue a 0.85 y salga el mensaje si es basura lo que hay en memoria
					System.err.println("Forzar collector de basura ya que la memoria ha superado el 75% con "+Singleton.getInstance().getNumberOfKnowledgeBaseAdapter()+" motores y memoria maxima "+maxMemory/(1024*1024));
					Utils.forceGarbageCollector();
					executeGarbageCollector=false;
				}
			}else{
				if(maxMemory*0.7>usedMemory){
					showMessage=true;
					executeGarbageCollector=true;
				}
			}
		}
	}
	
	public static long getAvailableMemoryMegabyte(){
		Runtime s_runtime = Runtime.getRuntime ();
		long usedMemory = s_runtime.totalMemory () - s_runtime.freeMemory ();
		long maxMemory = s_runtime.maxMemory();
		
		return (maxMemory-usedMemory)/(1024*1024);
	}
	
	
}

