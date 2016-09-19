package dynagent.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryOptimizer {
	public static void main(String args[]){
		QueryOptimizer q= new QueryOptimizer();
		try {
			q.optimize("test.txt","C:\\DYNAGENT\\test");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void optimize(String queryName,String path) throws FileNotFoundException, IOException{		
			BufferedReader br = new BufferedReader(new FileReader(path+"\\"+queryName));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
		    String newQuery=null;
		    String subquery=null;
		    Pattern p_main=Pattern.compile("(?:(.*CASE.*)?\\s*WHEN\\s(.+\\.\"cliente.+)\\sIS NOT NULL\\sTHEN\\s(?:(\\d+)|(?:[^\\d]+.+).+))|(?:.*ELSE.*)|(?:.+END\\sAS\\s(.+),.*)");	
		    HashMap<String,ArrayList<String>> segmented= new HashMap<String,ArrayList<String>>();
		    ArrayList<String> currentCase=null;

		    while (line != null) {		    	
		    	boolean matchSegment=false;
		    	if(line.matches(".+SELECT.+")){
		    		if(subquery!=null && subquery.length()>0){
		    			newQuery+="\n"+subquery;
		    			subquery="";
		    		}
		    		segmented.clear();
		    		currentCase=null;

		    	}else if(line.matches(".+FROM.+")){
		    	//fin subquery
		    		
		    		if(segmented.size()==0) newQuery+="\n"+subquery+line;
			    	else{
			    		HashMap<String,String> newSub=new HashMap<String,String>();
			    		for(String caseAlias:segmented.keySet()){
			    			ArrayList<String> caseArr=segmented.get(caseAlias);
			    			for(String caseColumn:caseArr){
				    			String caseColumnId=caseColumn;
				    			String numValue=null;
				    			if(caseColumnId.contains(";")){
				    				String[] s=caseColumnId.split(";");
				    				numValue=s[1];
				    				caseColumnId=s[0];
				    			}
				    			String newSubItem=newSub.get(caseColumnId);
				    			if(newSubItem==null){
				    				newSubItem=subquery;
				    				newSub.put(caseColumnId, newSubItem);
				    			}
				    			if(numValue==null){
				    				newSubItem=newSubItem.replaceAll("CASE"+caseAlias, caseColumn+" AS "+caseAlias);
				    				newSub.put(caseColumnId, newSubItem);
				    				//System.out.println(newSubItem);
				    			}
				    			else{
				    				newSubItem=newSubItem.replaceAll("CASE"+caseAlias, numValue+" AS "+caseAlias);
				    				newSub.put(caseColumnId, newSubItem);
				    				//System.out.println(newSubItem);
				    			}
			    			}
			    		}
			    		for(String caseAliasId:newSub.keySet()){
			    			String newSubItem=newSub.get(caseAliasId);
			    			newQuery+="\n"+newSubItem+line+"\nWHERE "+caseAliasId+" IS NOT NULL\nUNION ALL";
			    		}
			    		segmented.clear();
			    		currentCase=null;
			    	}
		    		line=null;
		    		subquery="";
		    	}else{		    			
		    		Matcher m_main=p_main.matcher(line);
		    		if(line.contains("CASE") && line.contains(".\"cliente")){
		    			System.out.println("HOLA");
		    		}
		    		boolean matched=m_main.find();
	    			String g1=null;
	    			String g2=null;
	    			String g3=null;
	    			String g4=null;
		    		if(matched){
		    			g1=m_main.group(1);
		    			g2=m_main.group(2);
		    			g3=m_main.group(3);
		    			g4=m_main.group(4);
		    		}
		    		if(matched && !line.contains("ELSE") && !(currentCase==null && line.contains("END"))
		    							
		    			){


		    			if(m_main.group(1)!=null && m_main.group(2)!=null){		    						    				
		    				currentCase=new ArrayList<String>();		    				
		    				line=null;
		    			}
		    			
		    			if(currentCase!=null && m_main.group(2)!=null){		
		    				if(currentCase==null){
		    					System.out.println("HOLA");
		    				}
		    				if(m_main.group(3)!=null){
		    					currentCase.add(g2+";"+m_main.group(3)); 
		    				}
		    				else currentCase.add(m_main.group(2));
		    				line=null;
		    			}
		    			if(m_main.group(4)!=null && currentCase!=null){
		    				line="CASE"+m_main.group(4)+",\n";
		    				segmented.put(m_main.group(4),currentCase);
		    				currentCase=null;
		    			}		  

		    		}else{
		    			if(line.contains("ELSE")){
		    				line=null;		    				
		    			}		    			
		    		}
		    	}
		    	
		    	if(line!=null) subquery+=line+System.lineSeparator();
		    	
		        line = br.readLine();
		        if(line!=null&&line.contains("CASE")){
		        	line+=br.readLine();
		        }
		    }
		    BufferedWriter output = null;
	        try {
	            File file = new File(path+"\\out.txt");
	            output = new BufferedWriter(new FileWriter(file));
	            output.write(newQuery);
	        } catch ( IOException e ) {
	            e.printStackTrace();
	        } finally {
	            if ( output != null ) output.close();
	        }		
	}

}
