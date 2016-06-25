package autoextract;

import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author richard.denson
 */
public class SAXExplorer extends DefaultHandler {
    //elements gleaned from the XML document
    private HashMap elements;
    
    //element triggers; information we want
    private String[] singleElements = {"datasource", "connection"};
    private String[] complexElements = {"metadata-record"};
    
    private HashMap matched = new HashMap();
    private String currentMatchedElement = "";
    
    //information about where we are in the document
    private String currentElement = "";
    private boolean isElementStart = false;
    private boolean elementHasAttributes = false;
    private int currentElementPosition = 0;
    
    
    @Override public void startDocument() throws SAXException {
        elements = new HashMap();
    }
    @Override public void startElement(String namespaceURI, String elementName, String qName, Attributes attrs) throws SAXException {
        HashMap elementAttributes = new HashMap();
        int numberOfAttributes = attrs.getLength();
        
        currentElement = elementName;
        isElementStart = true;
        elementHasAttributes = numberOfAttributes > 0;
        
        //record a specified element's attributes
        if( isMatch(elementName, singleElements) ){
            for(int attrItr = 0; attrItr < numberOfAttributes; attrItr++){
                elementAttributes.put(attrs.getLocalName(attrItr), attrs.getValue(attrItr));
            }
            
            elements.put(elementName, elementAttributes);
        }
        
        //record a set of elements and values within a specified element
        if( currentMatchedElement.length() == 0 && isMatch(elementName, complexElements) ){
            currentMatchedElement = elementName;
        }
    }
    @Override public void characters(char[] ch, int start, int length) {
        if( currentMatchedElement.length() > 0 && !currentElement.equals(currentMatchedElement) && isElementStart ){
            String elementValue = new String(ch, start, length).trim();
        
            if( !elementValue.isEmpty() ){
                matched.put(currentElement + "-" + currentElementPosition, elementValue);
            }
        }
    }
    @Override public void endElement(String namespaceURI, String elementName, String qName) {
        isElementStart = false;
        currentElementPosition++;
        
        if( elementName.equals(currentMatchedElement) && !isElementStart ){
            elements.put(currentMatchedElement + "-" + currentElementPosition, matched);
            
            //reset matched element
            currentMatchedElement = "";
            matched.clear();
        }
    }
    
    /*
    public void init(String[] targetElements, String[] complexElements){
        eTargets = targetElements;
        complexes = complexElements;
    }
    */
    public HashMap getElements() {
        return elements;
    }
    private boolean isMatch(String elementName, String[] setToMatch) {
        if( setToMatch == null || setToMatch.length == 0 ){
            return true;
        }
        
        boolean elementFound = false;
        int itr = 0;
        
        while(!elementFound && itr < setToMatch.length){
            if( setToMatch[itr].equals(elementName) ){
                elementFound = true;
            }
            
            itr++;
        }
        
        return elementFound;
    }
}
