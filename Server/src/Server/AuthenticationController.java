/**
 * Jaedyn Damms - 955581057 - JDAM534
 * COMPSYS 725 - ASSIGNMENT 1
 * SFTP - CLIENT / SERVER APPLICATION
 **/

package Server;

import static Server.TCPServer.databasePath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jaedyn
 */
public class AuthenticationController {
    
    BufferedReader reader = null;
    File file;
    boolean authenticated = false;
    boolean userVerified = false;
    boolean userAcct = false;
    boolean userPass = false;
    boolean superID = false;
    String user = "";
    String acct = "";
    String pass = "";
    
    AuthenticationController(){
        connectToDatabase();
    }
    
    /**
     * Sees if specified user is in database
     * @param clientCommands
     * @return 
     * @throws java.io.IOException 
    **/
    public String USER(String[] clientCommands) throws IOException {
        
        String returnStatement = "";
               
        // reference: https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
        try {

            System.out.println("readfile");

            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Read each line in database file and check if USER matches
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(line);
                String[] lineDets = line.split(" ", -1);
                
                // If USER matches
                if(clientCommands[1].equals(lineDets[0])){
                    
                    userVerified = true;
                    superID = false;
                    authenticated = false;
                    userAcct = false;
                    userPass = false;
                    user = lineDets[0];
                    acct = lineDets[1];
                    pass = lineDets[2];
                    
                    if("-".equals(acct)){ // USER doesn't require an account
                        userAcct = true;
                    }
                    if("-".equals(pass)){ // USER doesn't require a password
                        userPass = true;
                    }
                    if(userVerified && userAcct && userPass){ // USER doesn't require both account and password
                        authenticated = true;
                        superID = true;
                    }
                    
                }
                
            }

            // Return correct message
            if(authenticated){
                returnStatement = "!<user-id> logged in";
            } else if (userAcct){
                returnStatement = "+User-id valid, send password";
            } else if (userPass){
                returnStatement = "+User-id valid, send account";
            } else if (userVerified){
                userVerified = true;
                returnStatement = "+User-id valid, send account and password";
            } else {
                returnStatement = "-Invalid user-id, try again";
            }


        } catch (FileNotFoundException e) {

            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

        } catch (IOException e) {

            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

        }
        
        return returnStatement;
    
    }
    
    /**
     * Checks if specified account matches allocated USER in the database
     * @param clientCommands
     * @return 
     * @throws java.io.IOException 
    **/
    public String ACCT (String[] clientCommands) throws IOException{
        
        String returnStatement;
        
        
        if(authenticated){ // User is already authenticated
            returnStatement = "!<user-id> logged in";
        } 
        else if (userVerified) { // User needs to be identified before continuing
        
            // If account matches that assigne to USER
            if(clientCommands[1].equals(acct)){
                userAcct = true;
                
                if(userPass){
                    authenticated = true;
                    returnStatement = "! Account valid, logged-in";
                } else {
                    returnStatement = "+Account valid, send password";
                }
                
            } 
            
            else {
                returnStatement = "-Invalid account, try again";
            }
            
        } 
        
        else {
            returnStatement = "-Please identify your user-id";
        }
        
        return returnStatement;
        
    }
    
    /**
     * Check if specified pass matches allocated USER in databse
     * @param clientCommands
     * @return 
     * @throws java.io.IOException 
    **/
    public String PASS (String[] clientCommands) throws IOException{
        
        String returnStatement;
        
        if(authenticated){ // USER is already authenticated
            //outToClient.writeBytes("!<user-id> logged in" + '\n');
            returnStatement = "!<user-id> logged in";
        } 
        else if(userVerified) { // USER needs to be identified before continuing
        
            // If password matches that assigned to USER
            if(clientCommands[1].equals(pass)){
                
                userPass = true;
                if(userAcct){
                    authenticated = true;
                    returnStatement = "! Logged in";
                } else {
                    returnStatement = "+Send account";
                }
                
            } 
            
            else {
                returnStatement = "-Wrong password, try again";
            }
            
        } 
        
        else {
            returnStatement = "-Please identify your user-id";
        }
        
        return returnStatement;
        
    }
   
    /**
     * Checks if specified account matches assigned account
     * @param Acct
     * @return 
    **/
    public boolean checkAcct(String Acct){
       
        return Acct.equals(acct);
       
    }
   
    /**
     * Checks if specified pass matches allocated pass
     * @param Pass
     * @return 
    **/
    public boolean checkPass(String Pass){
        
       return Pass.equals(pass);
        
   }

    /**
     * Connects to database to get authentication details
    **/
    private void connectToDatabase() {
        
        // Database Connection
        file = new File(databasePath);
        if(file.exists() && !file.isDirectory()) { 
            System.out.println("Database file found :)");
        } else {
            System.out.println("Databse file not found.");
        }
        
    }
    
    /**
     * Resets authentication status
    **/
    public void reset() {
        authenticated = false;
        userVerified = false;
        userAcct = false;
        userPass = false;
        superID = false;
        user = "";
        acct = "";
        pass = "";
    }
    
}
