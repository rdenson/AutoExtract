package autoextract;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author richard.denson
 */
public class SAXLocalNameCount extends DefaultHandler {
    private Hashtable tags;
    
    private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        
        if( File.separatorChar != '/' ){
            path = path.replace(File.separatorChar, '/');
        }

        if( !path.startsWith("/") ){
            path = "/" + path;
        }
        
        return "file:" + path;
    }
    @Override public void startDocument() throws SAXException {
        tags = new Hashtable();
    }
    @Override public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
        String key = localName;
        Object value = tags.get(key);
        
        if( value == null ){
            tags.put(key, new Integer(1));
        } else{
            int count = ((Integer)value).intValue();
            count++;
            tags.put(key, new Integer(count));
        }
    }
    @Override public void endDocument() throws SAXException {
        Enumeration e = tags.keys();
        
        while(e.hasMoreElements()){
            String tag = (String)e.nextElement();
            int count = ((Integer)tags.get(tag)).intValue();
            System.out.println("Local Name \"" + tag + "\"occurs " + count + " times.");
        }
    }
}
