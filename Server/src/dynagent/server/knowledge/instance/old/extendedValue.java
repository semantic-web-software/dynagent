/*package dynagent.knowledge.instance;

import java.io.Serializable;
import dynagent.exceptions.DataErrorException;

public class extendedValue extends Object implements Serializable{
    String comment=null;
    Object value=null;

    public extendedValue( Object val, String comment ) throws DataErrorException{
        if( val==null )
            throw new DataErrorException("Error value null in extended val constructor");
        this.comment=comment;
        value=val;
    }
    public String getComment(){
        return comment;
    }
    public Object getValue(){
        return value;
    }
    public boolean equals( Object b ){
        if (!(b instanceof extendedValue))return false;
        extendedValue eb = (extendedValue) b;
        if (value == null || eb.value == null ||
            comment == null && eb.comment != null ||
            eb.comment == null && comment != null ||
            comment!=null && eb.comment!=null && !comment.equals(eb.comment))
            return false;
        return value.equals(eb.value);
    }
}
*/