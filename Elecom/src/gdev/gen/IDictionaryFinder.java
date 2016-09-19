package gdev.gen;

import java.util.LinkedHashMap;

public interface IDictionaryFinder {
	
	//Retorna true si ha encontrado un numero de resultados igual al limite máximo marcado y añade a words los resultados
	public boolean getDictionary(String idTable, String idColumn, String root, boolean exactQuery, LinkedHashMap<String, DictionaryWord> words);
	
}
