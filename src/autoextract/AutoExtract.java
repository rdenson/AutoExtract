package autoextract;

import com.tableausoftware.DataExtract.TableDefinition;
import com.tableausoftware.TableauException;

/**
 *
 * @author richard.denson
 */
public class AutoExtract {
    public static void main(String[] args) {
        System.setProperty("jna.library.path", "lib/win32-amd64");
        
        String tdsFile = "D:\\hold\\Automated Extracts Test.tds";
        TDSContentHandler tdsHandler = new TDSContentHandler();
        TDSReader tdsReader = new TDSReader(tdsHandler);
        tdsReader.read(tdsFile);
        try{
            TableDefinition tableDef = tdsReader.defineTable();
            System.out.println("Successfully created a table definition object from " + tdsFile + " with " + tableDef.getColumnCount() + " columns.");
        } catch(TableauException tableauError){
            tableauError.printStackTrace(System.err);
        }
    }
}
