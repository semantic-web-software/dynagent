package dynagent.common.communication;

import org.jdom.CDATA;
import org.jdom.Element;


/**
 * Esta clase se encarga de los mensajes de error y del debug, añadiéndolo al contenido del mensaje.
 */

public class errorTrace extends message{
    String debug, error, desc;

    errorTrace(int msgType){
        super( msgType );
    }

    public errorTrace(String debug, String error, String desc, int msgType){
        super( msgType );
        build( debug, error, desc );
    }

    public void build(String debug, String error, String desc){
        this.debug=debug;
        this.error=error;
        this.desc=desc;
    }

    public String getError(){
        return error;
    }
    public String getDebug(){
        return debug;
    }
    public String getDesc(){
        return desc;
    }

    void toElementHeader(Element root){
        super.toElementHeader(root);
    }

    void toElementContent( Element root ){
        super.toElementContent(root);
        if( error!=null || debug!=null || desc!=null ){
            Element eContent = root.getChild("CONTENT");
            if (eContent == null) {
                eContent = new Element("CONTENT");
                root.addContent(eContent);
            }
            if( error!=null ){
                Element eError= new Element("ERROR");
                eContent.addContent(eError);
                eError.setText(error);
            }
            if (debug != null && debug.length() > 0) {
                Element eDbg = new Element("DEBUG");
                eContent.addContent(eDbg);
                eDbg.addContent(new CDATA(debug));
            }
            if (desc != null) {
                Element eDesc = new Element("DESC");
                root.addContent(eDesc);
                eDesc.addContent(new CDATA(desc));
            }
        }
    }

}
