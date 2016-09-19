/**
 * TestFactory.java
 * @author Ildefonso Montero Pérez - ildefonso.montero@gmail.com
 */
package dynagent.ruleengine.test;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.Session;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.test1.src.Test1;
import dynagent.ruleengine.test.test10.src.Test10;
import dynagent.ruleengine.test.test11.src.Test11;
import dynagent.ruleengine.test.test12.src.Test12;
import dynagent.ruleengine.test.test13.src.Test13;
import dynagent.ruleengine.test.test14.src.Test14;
import dynagent.ruleengine.test.test15.src.Test15;
import dynagent.ruleengine.test.test16.src.Test16;
import dynagent.ruleengine.test.test17.src.Test17;
import dynagent.ruleengine.test.test18.src.Test18;
import dynagent.ruleengine.test.test19.src.Test19;
import dynagent.ruleengine.test.test2.src.Test2;
import dynagent.ruleengine.test.test20.src.Test20;
import dynagent.ruleengine.test.test21.src.Test21;
import dynagent.ruleengine.test.test22.src.Test22;
import dynagent.ruleengine.test.test3.src.Test3;
import dynagent.ruleengine.test.test4.src.Test4;
import dynagent.ruleengine.test.test5.src.Test5;
import dynagent.ruleengine.test.test6.src.Test6;
import dynagent.ruleengine.test.test7.src.Test7;
import dynagent.ruleengine.test.test8.src.Test8;
import dynagent.ruleengine.test.test9.src.Test9;

public class TestFactory {
	
	static private TestFactory instance = null;
	public String type = "";
	
	/**
	 * getInstance
	 */
	public static TestFactory getInstance(){
        if(instance == null)
            return new TestFactory();
        else
            return instance;
    }
    
	/**
	 * TestFactory
	 */
    private TestFactory() {}
	
	/**
	 * getType()
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * setType()
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * getTest()
	 */
	public ITest getTest(int idprueba){
        ITest test = null;
        if(idprueba == 1)
        	test = new Test1();
        else if(idprueba == 2)
        	test = new Test2();
        else if(idprueba == 3)
        	test = new Test3();
        else if(idprueba == 4)
        	test = new Test4();
        else if(idprueba == 5)
        	test = new Test5();
        else if(idprueba == 6)
        	test = new Test6();
        else if(idprueba == 7)
        	test = new Test7();
        else if(idprueba == 8)
        	test = new Test8();
        else if(idprueba == 9)
        	test = new Test9();
        else if(idprueba == 10)
        	test = new Test10();
        else if(idprueba == 11)
        	test = new Test11();
        else if(idprueba == 12)
        	test = new Test12();
        else if(idprueba == 13)
        	test = new Test13();
        else if(idprueba == 14)
        	test = new Test14();
        else if(idprueba == 15)
        	test = new Test15();
        else if(idprueba == 16)
        	test = new Test16();
        else if(idprueba == 17)
        	test = new Test17();
        else if(idprueba == 18)
        	test = new Test18();
        else if(idprueba == 19)
        	test = new Test19();
        else if(idprueba == 20)
        	test = new Test20();
        else if(idprueba == 21)
        	test = new Test21();
        else if(idprueba == 22)
        	test = new Test22();
        
        return test;
    }	
	
	public void runAll(IKnowledgeBaseInfo ik,Integer userRol, String user, Integer usertask){	
		DefaultSession s = new DefaultSession(new DocDataModel(Session.APPLET_SESSION),null,Session.APPLET_SESSION);
		SessionController.getInstance().setActual(s);
		for(int i = 1; i< 23; i++){
			try {
				this.getTest(i).run(ik,userRol,user,usertask);
			}catch (CardinalityExceedException e) {
				e.printStackTrace();
			}catch (IncompatibleValueException e) {
				e.printStackTrace();
			}catch (OperationNotPermitedException e) {
			System.out.println("   error:"+ e.getMessage());
			e.getCause();
			e.printStackTrace();
			}catch (NotFoundException e) {
				System.out.println("   error:"+ e.getMessage());
				e.getCause();
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	public void run(IKnowledgeBaseInfo ik,int ntest,Integer userRol, String user, Integer usertask) throws NotFoundException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, IncoherenceInMotorException{
			this.getTest(ntest).run(ik,userRol,user,usertask);
	}
	
}
