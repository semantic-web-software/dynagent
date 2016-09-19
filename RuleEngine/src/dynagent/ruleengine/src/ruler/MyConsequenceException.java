package dynagent.ruleengine.src.ruler;

import java.io.PrintStream;
import java.util.Collection;

import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.rule.Rule;
import org.drools.spi.Activation;
import org.drools.spi.ConsequenceException;

public class MyConsequenceException extends ConsequenceException {
    private static final long serialVersionUID = 510l;
    private WorkingMemory workingMemory;
    private Activation activation;
    private Exception exception;

    public MyConsequenceException( final Exception exception,
                                 final WorkingMemory workingMemory,
                                 final Activation activation ){
        super( exception );
        this.workingMemory = workingMemory;
        this.activation = activation;
        this.exception=exception;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder( "Exception executing consequence for " );
        Rule rule = null;
        
        if( activation != null && ( rule = activation.getRule() ) != null ){
            String packageName = rule.getPackage().toString();
            String ruleName = rule.getName();
            sb.append( "rule \"" ).append( ruleName ).append( "\" in " ).append( packageName );
        } else {
            sb.append( "rule, name unknown" );
        }
        sb.append( ": " ).append( super.getMessage() );
        return sb.toString();
    }

    public void printFactDump(){
        printFactDump( System.err );
    }

    public void  printFactDump( PrintStream pStream ){
//        Collection handles = activation.getFactHandles();
//        for( FactHandle handle: handles ){
//            Object object = workingMemory.getObject( handle );
//            if( object != null ){
//                pStream.println( "   Fact " + object.getClass().getSimpleName() +
//                                 ": " + object.toString() );
//            }
//        }
    }

    @Override
    public String toString() {
        return getMessage();
    }
    
    public Exception getException(){
    	return exception;
    }
}

