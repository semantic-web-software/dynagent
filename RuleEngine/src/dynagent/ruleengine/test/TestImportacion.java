/***
 * TestImportacion.java
 * @author: Jose A. Zamora Aguilera - jazamora@ugr.es
 */

package dynagent.ruleengine.test;

import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.test.test1.src.Test1;
import dynagent.ruleengine.test.test16.src.Test16;
import dynagent.ruleengine.test.test7.src.Test7;


public class TestImportacion {
	
	public static void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) {
		try{
			 ITest test = null; 
			 test = new Test16();
		      test.run(ik,userRol,user,usertask);
		      test = new Test7();
		      test.run(ik,userRol,user,usertask);
		     
		      test = new Test1();
		      test.run(ik,userRol,user,usertask);
		      
		       
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
