package dynagent.gui;

//import netscape.javascript.JSObject;
import java.applet.Applet;

import javax.swing.text.html.HTML;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLButtonElement;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLInputElement;

import com.sun.java.browser.dom.DOMAccessException;
import com.sun.java.browser.dom.DOMAccessor;
import com.sun.java.browser.dom.DOMAction;
import com.sun.java.browser.dom.DOMService;
import com.sun.java.browser.dom.DOMUnsupportedException;

public class JavaScriptCommand
{
    private Applet applet;

    public JavaScriptCommand(Applet applet)
    {
        if (applet == null)
        {
            throw new IllegalArgumentException("A non-null value for applet must be provided");
        }
        this.applet = applet;
    }

    /**
     * Performs actual call to JavaScript via Applet instance passed in Constructor.
     * @param scriptFunction - Name of JavaScript function to call
     * @param functionArgs - Array of parameter to pass into JavaScript function. Size of array
     * must match number of parameter in JavaScript function.
     * @return value returned from JavaScript function, if any.
     */

    public void processCommand(String scriptFunction, String[] functionArgs)
    {
//        // Initialize the JSObject by passing in a reference to the Applet
//        JSObject win = JSObject.getWindow(this.applet);
//
//        // Call the JavaScript function, setFormTextEntry, and pass in the
//        // parameters represented by the array, args
//        return win.call(scriptFunction, functionArgs);
    	
    	DOMService service = null;

    	try
    	{
    	    service = DOMService.getService(applet);
    	    String title = (String) service.invokeAndWait(new DOMAction()
    	                            {
    	                                public Object run(DOMAccessor accessor)
    	                                {
    	                                     HTMLDocument doc = (HTMLDocument) accessor.getDocument(applet);
    	                                     doc.setTitle("PROBANDOOOO");
    	                                     System.err.println("Aqui estamos:"+doc.getLinks().namedItem("P1"));
    	                                     Node a=doc.getLinks().namedItem("P1");
    	                                     System.err.println(doc.getForms().namedItem("B1"));
    	                                     System.err.println(doc.getElementsByName("B1"));
    	                                     ((HTMLInputElement)doc.getElementsByName("nombre").item(0)).setValue("Probando");
    	                                     ((HTMLInputElement)doc.getElementsByName("B1").item(0)).click();
    	                                     System.err.println("a:"+a.getClass());
    	                                     //System.err.println(doc.getLinks().namedItem("P1").getNodeValue());
    	                                     return doc.getTitle();

    	                                     //Add the items to the HTMLDocument
    	                                     //doc.addElement(head);
    	                                     
    	                                }
    	                            });
    	}
    	catch (DOMUnsupportedException e1)
    	{
    		e1.printStackTrace();
    	}
    	catch (DOMAccessException e2)
    	{
    		e2.printStackTrace();
    	}

    }
    
}
