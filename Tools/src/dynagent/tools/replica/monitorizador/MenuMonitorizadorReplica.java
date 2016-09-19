package dynagent.tools.replica.monitorizador;

import java.util.Calendar;
import java.util.HashMap;

import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.ConceptLogger;

public class MenuMonitorizadorReplica {
	
	public static void main(String[] args) {
		try{
			String pathXML = args[0];
			ParserMonitorizadorReplica pSR = new ParserMonitorizadorReplica();
			HashMap<Integer,SlaveData> hSlaveData = pSR.readXML(pathXML);

			
			char opcion;
			MenuMonitorizadorReplica menu = new MenuMonitorizadorReplica();
			MonitorizadorReplica sR = new MonitorizadorReplica();
			
			do {
				opcion = menu.dameOpcion();
				try {
					switch (opcion) {
					case 'S':
						System.out.println("Salir");
						break;
					case 'C':
						sR.showStatusReplica(hSlaveData);
						break;
					case 'P':
						Integer serverId = Integer.parseInt(Auxiliar.leeTexto("Escriba el SERVER_ID de la instancia que quiere parar:"));
						sR.stopSlave(hSlaveData, serverId);
						break;
					case 'I':
						serverId = Integer.parseInt(Auxiliar.leeTexto("Escriba el SERVER_ID de la instancia que quiere iniciar:"));
						sR.startSlave(hSlaveData, serverId);
						break;
					case 'L':
						serverId = Integer.parseInt(Auxiliar.leeTexto("Escriba el SERVER_ID de la instancia que quiere que salte una instrucción:"));
						sR.jumpSlave(hSlaveData, serverId);
						break;
					case 'O':
						sR.showMastersPositions(hSlaveData);
						break;
					case 'D':
						Integer nextServerId = pSR.getMaxServerId()+1;
						Integer nextTienda = pSR.getMaxTienda()+1;
						System.out.println("El siguiente SERVER-ID es " + nextServerId);
						System.out.println("La siguiente tienda es " + nextTienda);
						break;
					default:
						System.out.println("La opcion es incorrecta");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (opcion != 'S');

			System.out.println("*****bye******");
			ConceptLogger.getLogger().writeln(
					"Fin de sesión: " + Calendar.getInstance().getTime());

			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public char dameOpcion() {
		System.out.println("\n\n===========================================================================================");
		System.out.println("\n           DYNAGENT STATUS REPLICA   ");
		System.out.println("\n OPCIONES:");
		System.out.println("C.-Comprobar el estado de la réplica, triggers, federadas y configuración");
		System.out.println("P.-Parar réplica en alguna instancia");
		System.out.println("I.-Iniciar réplica en alguna instancia");
		System.out.println("L.-Saltar una instrucción en alguna instancia");
		System.out.println("O.-Obtener posiciones de maestros");
		System.out.println("D.-Obtener siguiente SERVER-ID, TIENDA");
		System.out.println("\nS.-SALIR");
		System.out.println("\n===========================================================================================");
		String texto = Auxiliar.leeTexto("SELECCIONE UNA OPCIÓN");
		char opcion;
		if (texto!=null)
			opcion = texto.charAt(0);
		else
			opcion='0';
		return opcion;
	}

}
