package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Santiago
 */
public class FileManager {

    private static final String PATH = System.getProperty("user.dir");
    private static final String DATA_PATH = PATH + "\\data";
    private static final String BACKUP_PATH = PATH + "\\backup";
    private static final String HISTORY_PATH = PATH + "\\history";
    private FileWriter turnFile;
    private FileWriter roomFile;

    public FileManager() {
        prepareFolders();
    }

    public void prepareFolders() {
        //All folders are created regardless of the path used
        File prepareData = new File(DATA_PATH);
        prepareData.mkdirs();
        File prepareBackup = new File(BACKUP_PATH);
        prepareBackup.mkdirs();
        File prepareHistory = new File(HISTORY_PATH);
        prepareHistory.mkdirs();
        System.out.println("Folders created");
    }

    /*
    All data retreival functions the same way, as it's a standard JSON
    For each data therre may be
        * turn
        * rooms
        * inventoryData
        * applicationProperties
     */
    public JSONObject getJsonData(String dataNeeded) {
        String outputString = new String();
        JSONObject output;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(DATA_PATH + "\\" + dataNeeded));
            String line;
            while ((line = reader.readLine()) != null) {
                outputString = outputString.concat(line);
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        try{
            System.out.println(outputString);
            output = new JSONObject(outputString);
        }catch(JSONException ex){
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return output;
    }

}
