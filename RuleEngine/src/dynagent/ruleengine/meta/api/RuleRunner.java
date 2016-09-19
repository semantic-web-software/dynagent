package dynagent.ruleengine.meta.api;


//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.util.ArrayList;
//
//import org.drools.QueryResult;
//import org.drools.QueryResults;
//import org.drools.RuleBase;
//import org.drools.RuleBaseFactory;
//import org.drools.WorkingMemory;
//import org.drools.compiler.DroolsParserException;
//import org.drools.compiler.PackageBuilder;
//import org.drools.rule.Package;

public class RuleRunner {
//
//    public RuleRunner() {
//    }
//
//    public void buildRules(ArrayList<String> rules,
//                         ArrayList<Object> facts) {
//
//        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//        PackageBuilder builder = new PackageBuilder();
//
//        for ( int i = 0; i < rules.size(); i++ ) {
//            String ruleFile = (String) rules.get(i);
//            System.out.println( "Loading file: " + ruleFile );            
//            try {
//				builder.addPackageFromDrl(new InputStreamReader( RuleRunner.class.getResourceAsStream( ruleFile ) ) );
//			} catch (DroolsParserException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
//
//        Package pkg = builder.getPackage();
//        try {
//			ruleBase.addPackage( pkg );
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        WorkingMemory workingMemory = ruleBase.newStatefulSession();
//
//        for ( int i = 0; i < facts.size(); i++ ) {
//            Object fact = facts.get(i);
//            System.out.println( "Inserting fact: " + fact );
//            workingMemory.insert( fact, true );
//        }
//
//    }
}

