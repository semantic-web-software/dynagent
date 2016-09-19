package dynagent.version.processor;

import dynagent.version.api.ProcessorInterface;

public class ProcessorFactory {

	/**
	 * Crea un procesador de ficheros de reglas que nos permite analizarlos.
	 * @return Nuevo objeto procesador.
	 */
	public static ProcessorInterface createProcessor(){
		return new TortoiseProcessor();
	}
}
