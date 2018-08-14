/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
    public String USER(String[] clientCommands) throws IOException {
        
        String returnStatement = "";
               
        // reset authentication and re sign in if it is authenticated
        
        // reference: https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
        try {

            System.out.println("readfile");

            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(line);
                String[] lineDets = line.split(" ", -1);
                if(clientCommands[1].equals(lineDets[0])){
                    userVerified = true;
                    user = lineDets[0];
                    acct = lineDets[1];
                    pass = lineDets[2];
                    if("-".equals(acct)){
                        userAcct = true;
                    }
                    if("-".equals(pass)){
                        userPass = true;
                    }
                    if(userVerified && userAcct && userPass){
                        authenticated = true;
                        superID = true;
                    }
                    // check if need user name and pass to log in 
                    // authenticated = true;
                }
                //outToClient.writeBytes(line + '\n');
            }

            if(authenticated){
                returnStatement = "!<user-id> logged in";
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
    
    
    public String ACCT (String[] clientCommands) throws IOException{
        
        String returnStatement;
        
        if(authenticated){ 
            //outToClient.writeBytes("!<user-id> logged in" + '\n');
            returnStatement = "!<user-id> logged in";
        } 
        else if (userVerified) {
        
            if(clientCommands[1].equals(acct)){
                userAcct = true;
                if(userPass){
                    authenticated = true;
                    returnStatement = "! Account valid, logged-in";
                } else {
                    returnStatement = "+Account valid, send password";
                }
            } else {
                returnStatement = "-Invalid account, try again";
            }
        } else {
            returnStatement = "-Please identify your user-id";
        }
        return returnStatement;
        
    }
    
   public String PASS (String[] clientCommands) throws IOException{
        
        String returnStatement;
        
        if(authenticated){ 
            //outToClient.writeBytes("!<user-id> logged in" + '\n');
            returnStatement = "!<user-id> logged in";
        } 
        else if(userVerified) {
        
            if(clientCommands[1].equals(pass)){
                userPass = true;
                if(userPass){
                    authenticated = true;
                    returnStatement = "! Logged in";
                } else {
                    returnStatement = "+Send account";
                }
            } else {
                returnStatement = "-Wrong password, try again";
            }
        } else {
            returnStatement = "-Please identify your user-id";
        }
        return returnStatement;
        
    }
   
   public boolean checkAcct(String Acct){
       
        return Acct.equals(acct);
       
   }
   
    public boolean checkPass(String Pass){
        
       return Pass.equals(pass);
        
   }


    private void connectToDatabase() {
        
        // Database Connection
        file = new File(databasePath);
        if(file.exists() && !file.isDirectory()) { 
            System.out.println("Database file found :)");
        } else {
            System.out.println("Databse file not found.");
        }
        
    }
    
    public void reset() {
        authenticated = false;
        userVerified = false;
        userAcct = false;
        userPass = false;
        user = "";
        acct = "";
        pass = "";
    }
    
}
