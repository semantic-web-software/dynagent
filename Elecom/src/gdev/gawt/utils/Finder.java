package gdev.gawt.utils;

import gdev.gen.DictionaryWord;
import gdev.gen.GConfigView;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;

public class Finder{
		
	private LinkedHashMap<String, DictionaryWord> dictionary;
	private String root;
	private boolean appliedLimit;

	public Finder(LinkedHashMap<String, DictionaryWord> name, String root, boolean appliedLimit){
		setDictionary(name,root,appliedLimit);
	}

	public void setDictionary(LinkedHashMap<String, DictionaryWord> name, String root, boolean appliedLimit){
		this.dictionary = name;		
		this.root=root!=null?root.toUpperCase():"";
		this.appliedLimit=appliedLimit;
	}

	private LinkedHashMap<StringFinder, DictionaryWord> createStringDictionay(LinkedHashMap<String, DictionaryWord> aux, String root2) {
		LinkedHashMap<StringFinder, DictionaryWord> dicString = new LinkedHashMap<StringFinder, DictionaryWord>();
		Iterator<String> it = aux.keySet().iterator();
		while(it.hasNext()){
			String text = it.next();
			DictionaryWord dw = aux.get(text);
			dicString.put(new StringFinder(root2, text), dw);
		}
		return dicString;
	}

	public TreeMap<String, DictionaryWord> getWords(String root,Integer ido, Integer idto){
		String rootUper=root!=null?root.toUpperCase():"";
		if(rootUper.indexOf(this.root)==0){
			LinkedHashMap<String, DictionaryWord> aux= new LinkedHashMap<String, DictionaryWord>();
			Iterator<String> itIdoDic= this.dictionary.keySet().iterator();
			while (itIdoDic.hasNext()/* && result.size()<ListFinder.numberOfResultsVisible*/){
				String wordForUser = itIdoDic.next();
				DictionaryWord dw=this.dictionary.get(wordForUser);
				String word=dw.getWord();
				if (word.toUpperCase().contains(rootUper)){
					if(!aux.containsKey(word) || (Auxiliar.equals(dw.getIdo(), ido) && Auxiliar.equals(dw.getIdto(), idto))){
						//If añadido para la copia masiva de filas desde excel, con f7, ya que si en la descripcion de un producto en ese excel hay diferencias con
						//las mayusculas termina llamandose a este metodo y puede darse el caso de que si hay otro producto con la misma descripcion lo cambie por ese otro.
						//Con este if evitamos ese caso ya que nos quedariamos con el ido e idto pasado por parametro.
						aux.put(word, dw);
					}
				}
			}
			TreeMap<String, DictionaryWord> result=sort(aux, root!=null?root:"");
			return result;			
		}else
			return null;		
	}
	
	public TreeMap<String, DictionaryWord> getWordsForUser(String root){
		String rootUper=root!=null?root.toUpperCase():"";
		if(rootUper.indexOf(this.root)==0){
			LinkedHashMap<String, DictionaryWord> aux= new LinkedHashMap<String, DictionaryWord>();
			Iterator<String> itIdoDic= this.dictionary.keySet().iterator();
			while (itIdoDic.hasNext()/* && result.size()<ListFinder.numberOfResultsVisible*/){
				String wordForUser = itIdoDic.next();
				DictionaryWord dw=this.dictionary.get(wordForUser);
				String word=dw.getWord();
				if (word.toUpperCase().contains(rootUper))
					aux.put(wordForUser, dw);				
			}
			TreeMap<String, DictionaryWord> result=sort(aux, root!=null?root:"");
			return result;
		}else
			return null;		
	}
	
	public boolean containsWord(String wordSearch){
		String wordSearchUpper=wordSearch!=null?wordSearch.toUpperCase():"";
		boolean contains=false;
		Iterator<String> itIdoDic= this.dictionary.keySet().iterator();
		while (itIdoDic.hasNext()/* && result.size()<ListFinder.numberOfResultsVisible*/){
			String wordForUser = itIdoDic.next();
			DictionaryWord dw=this.dictionary.get(wordForUser);
			String word=dw.getWord();
			if (word.toUpperCase().contains(wordSearchUpper))
				contains=true;
		}
		return contains;		
	}
	
	public TreeMap<String, DictionaryWord> sort(final LinkedHashMap<String, DictionaryWord> aux, final String root){
		//Con el comparator ademas conseguimos para TreeMap que las claves no sean case sensitives al buscar con get
		TreeMap<String, DictionaryWord> result = new TreeMap<String, DictionaryWord>(new Comparator<String>(){

			public int compare(String a, String b) {
				if(a!=null && b!=null && a.equalsIgnoreCase(b))//Esto nos sirve para que el get devuelva el valor siendo case insensitive
					return 0;
				
				String auxA=null;
				String auxB=null;
				if(aux.get(a)!=null)
					auxA=aux.get(a).getWord();
				if(aux.get(b)!=null)
					auxB=aux.get(b).getWord();
				
				if(auxA==null || auxB==null){
					if(auxA==null && auxB!=null) return -1;
					if(auxA!=null && auxB==null) return 1;
					return 0;
				}
				
				if(begins(root, auxA) && begins(root, auxB)){
					int equals=Constants.languageCollator.compare(auxA,auxB);
					if(equals==0)
						equals=Constants.languageCollator.compare(a,b);
					return equals;
				}else if(begins(root, auxA)){
					return -1;
				}else if(begins(root, auxB)){
					return 1;
				}else{
					int equals=Constants.languageCollator.compare(auxA,auxB);
					if(equals==0)
						equals=Constants.languageCollator.compare(a,b);
					return equals;
				}
			}
			
			public boolean begins(String root, String text){
				int t = root.length();
				String compare = text.substring(0, t).toUpperCase();
				return compare.equals(root.toUpperCase());
			}
			
		});
	
		/*LinkedHashMap<StringFinder, DictionaryWord> stringDic = createStringDictionay(aux, root);
		LinkedList<StringFinder> list = new LinkedList<StringFinder>();
		list.addAll(stringDic.keySet());				
		Collections.sort(list);
		Iterator<StringFinder> it = list.iterator();
		while(it.hasNext()){
			String text = it.next().getText();
			result.put(text, aux.get(text));
		}*/
		result.putAll(aux);
		return result;
	}
	
	/**
	 * Indica si se ha aplicado el limite en los resultados obtenidos desde base de datos
	 * @return
	 */
	public boolean isAppliedLimitDictionary(){
		return appliedLimit;
	}
	
	public void setAppliedLimitDictionary(boolean appliedLimit) {
		this.appliedLimit=appliedLimit;
	}
	
	public class StringFinder implements Comparable<StringFinder>{
		
		String text;
		String root;
		
		public StringFinder(String root, String text) {
			this.root = root;
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getRoot() {
			return root;
		}

		public void setRoot(String root) {
			this.root = root;
		}
		
		public int compareTo(StringFinder o) {
			if(begins(getRoot(), o.getText()) && begins(getRoot(), this.getText())){
				return this.getText().compareTo(o.getText());
			}else if(begins(getRoot(), this.getText())){
				return -1;
			}else if(begins(getRoot(), o.getText())){
				return 1;
			}else{
				return Constants.languageCollator.compare(this.getText(), o.getText());
			}
		}
		
		public boolean begins(String root, String text){
			int t = root.length();
			String compare = text.substring(0, t).toUpperCase();
			return compare.equals(root.toUpperCase());
		}
		
		public String toString(){
			return getText();
		}
	}
}
