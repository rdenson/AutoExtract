package autoextract;

import com.tableausoftware.DataExtract.*;
import com.tableausoftware.TableauException;
import java.io.*;
import java.util.HashMap;
import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * Reads a .tds file and produces a TableDefinition object. This class relies on
 * the TDSContentHandler class to parse the .tds file.
 * 
 * @author richard.denson
 * @since JDK 1.7
 */
public class TDSReader {
    private SAXParserFactory saxFactory;
    private SAXParser xmlParser;
    private XMLReader xmlReader;
    
    public TDSReader(TDSContentHandler XMLContentHandler) {
        /*
            VERY IMPORTANT
            This call allows us to use the Tableau Data Extract (TDE) API. The
            TDE API is more than the JAR file... TDE API JAR file utilizes Java
            Native Access (jna) in order to access various dlls.
        */
        System.setProperty("jna.library.path", "lib/win32-amd64");
        
        try{
            //setup parser
            saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(true);
            xmlParser = saxFactory.newSAXParser();
            
            //setup reader; use a TDSContentHandler instance for the reader's content handler
            xmlReader = xmlParser.getXMLReader();
            xmlReader.setContentHandler(XMLContentHandler);
        } catch(ParserConfigurationException | SAXException processingError){
            processingError.printStackTrace(System.err);
        }
    }
    
    //parses the file supplied to this function; the argument needs to be the path and file in one string
    public void read(String theFile) {
        try{
            File tdsFile = new File(theFile);
            
            if( !tdsFile.exists() || !tdsFile.canRead() ){
                throw new IOException("\"" + tdsFile.getAbsolutePath() + "\" does NOT exist or cannot be read.\r\nCheck the path and try again.");
            }
            
            //our xmlReader is just the SAX parser leaning on the TDSContentHandler class
            xmlReader.parse(theFile);
        } catch(IOException | SAXException processingError){
            processingError.printStackTrace(System.err);
        }
    }
    
    //getter for column information returned as a result of the xmlReader's parsing logic
    private HashMap getDatasourceColumns() {
        TDSContentHandler tdsContent = (TDSContentHandler)xmlReader.getContentHandler();
        
        return tdsContent.getColumns();
    }
    
    //getter for datasource metadata returned as a result of the xmlReader's parsing logic
    private HashMap getDatasourceMetadata() {
        TDSContentHandler tdsContent = (TDSContentHandler)xmlReader.getContentHandler();
        
        return tdsContent.getMetadata();
    }
    
    //creates a Tableau TableDefinition object from the column information returned after parsing the .tds file
    public TableDefinition defineTable() throws TableauException {
        int numberOfColumns;
        HashMap columnMap = getDatasourceColumns();
        TableDefinition newDefinition = new TableDefinition();
        
        newDefinition.setDefaultCollation(Collation.EN_US);
        numberOfColumns = columnMap.size();
        
        //walk the column information HashMap; this may (will?) need some more work later, for example: how to handle column naming collisions
        for(int itr = 0; itr < numberOfColumns; itr++){
            HashMap columnData = (HashMap)columnMap.get("column-" + itr);
            String columnName = "[dbo]." + columnData.get("parent-name") + "." + columnData.get("local-name");
            String columnRawType = (String)columnData.get("local-type");
            Type columnType = Type.UNICODE_STRING;
            
            //translate gleaned column types into Tableau Types
            switch(columnRawType){
                case "boolean":
                    columnType = Type.BOOLEAN;
                    break;
                case "string":
                    columnType = Type.CHAR_STRING;
                    break;
                case "date":
                    columnType = Type.DATE;
                    break;
                case "datetime":
                    columnType = Type.DATETIME;
                    break;
                case "integer":
                    columnType = Type.INTEGER;
                    break;
            }
            
            //add the current column to the table definition
            newDefinition.addColumn(columnName, columnType);
        }
        
        return newDefinition;
    }
}
