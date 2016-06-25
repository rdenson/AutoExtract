package autoextract;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

/**
 * This class extends the DefaultHandler for SAX 2.0 (Simple API for XML) which,
 * is basically a ContentHandler interface that we'll use to parse an XML
 * structured file. The code here was build with Tableau's .tds file in mind.
 * 
 * Produces two HashMap objects; one containing columns and the other a
 * non-specific set of datasource metadata. The value of both HashMaps are
 * HashMaps.
 * 
 * @author richard.denson
 * @since JDK 1.7 (for 1.8, the HashMap iterator implementation needs to change)
 */
public class TDSContentHandler extends DefaultHandler {
    private HashMap tdsMetadata;
    private HashMap tdsColumns;
    
    //column specific variables
    private final HashMap columnInformation = new HashMap();
    private boolean fetchingColumnInformation = false;
    private int columnPosition = 0;
    
    //information about where we are in the document
    private String currentElement = "";
    private boolean isCurrentElementStart = false;
    
    
    
    @Override public void startDocument() throws SAXException {
        //init our consumables; we'll probably need a constructor or initializer later
        tdsMetadata = new HashMap();
        tdsColumns = new HashMap();
    }
    
    //called at the beginning of each XML element, ie: <tag_a>
    @Override public void startElement(String namespaceURI, String elementName, String qName, Attributes attrs) throws SAXException {
        HashMap elementAttributes = new HashMap();
        int numberOfAttributes = attrs.getLength();
        
        //take note of our location upon every element we come across
        currentElement = elementName;
        isCurrentElementStart = true;
        
        //building out datasource metadata...
        if( elementName.equals("datasource") || elementName.equals("connection") ){
            //each attribute of the current element will be placed into a HashMap for the current element's name
            for(int attrItr = 0; attrItr < numberOfAttributes; attrItr++){
                elementAttributes.put(attrs.getLocalName(attrItr), attrs.getValue(attrItr));
            }
            
            tdsMetadata.put(elementName, elementAttributes);
        }
        
        //directive to began recording column information
        if( elementName.equals("metadata-record") && !fetchingColumnInformation ){
            fetchingColumnInformation = true;
        }
    }
    
    //called after startElement() and endElement() arguments contain a character array bounded by the current context (start or end element)
    @Override public void characters(char[] ch, int start, int length) {
        //building out column information for an individual column
        if( fetchingColumnInformation && isCurrentElementStart ){
            String columnMetadata = new String(ch, start, length).trim();
            
            //exclude elements with and empty value, ie: <tag_a></tag_a>
            if( !columnMetadata.isEmpty() ){
                columnInformation.put(currentElement, columnMetadata);
            }
        }
    }
    
    //called at the end of each XML element, ie: </tag_a>
    @Override public void endElement(String namespaceURI, String elementName, String qName) {
        isCurrentElementStart = false;
        
        //after we're finished recording a set of information for a column add this HashMap to the collection of all columns in this file
        if( elementName.equals("metadata-record") && !isCurrentElementStart ){
            HashMap temp = new HashMap();
            Iterator itr = columnInformation.entrySet().iterator();
            
            //we need to create a deep copy of this column's HashMap (no references)
            while(itr.hasNext()){
                //HashMap.Entry -> JDK 1.8
                Map.Entry pair = (Map.Entry)itr.next();
                temp.put(pair.getKey(), pair.getValue());
                itr.remove();
            }
            
            //add our column to the main HashMap then clean up and prepare for the next column
            tdsColumns.put("column-" + columnPosition, temp);
            fetchingColumnInformation = false;
            columnInformation.clear();
            columnPosition++;
        }
    }
    
    //getter for the datasource metadata HashMap
    public HashMap getMetadata() {
        return tdsMetadata;
    }
    
    //getter for the column information HashMap
    public HashMap getColumns() {
        return tdsColumns;
    }
    
    //deprecated for now...
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
