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
    boolean userVerified = false;
    boolean userAcct = false;
    boolean userPass = false;
    String user = "";
    String acct = "";
    String pass = "";
    
    AuthenticationController(){
        // Database Connection
        file = new File(databasePath);
        if(file.exists() && !file.isDirectory()) { 
            System.out.println("Database file found :)");
        } else {
            System.out.println("Databse file not found.");
        }
    }
    
    public String USER(String[] clientCommands) throws IOException {
        boolean userFound = false;
        String returnStatement = "";
               
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
                    userFound = true;
                    user = lineDets[0];
                }
                //outToClient.writeBytes(line + '\n');
            }

            if(userFound){
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
        
        boolean acctFound = false;
        String returnStatement = null;
        
        if(userAcct && userPass){
            //outToClient.writeBytes("!<user-id> logged in" + '\n');
            returnStatement = "!<user-id> logged in";
        } 
        else {
        
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
                    if(user.equals(lineDets[0])){
                        if(clientCommands[1].equals(lineDets[1])){
                            acctFound = true;
                        }
                    }
                    //outToClient.writeBytes(line + '\n');
                }

                if(acctFound){
                    userAcct = true;
                    if(userPass){
                        returnStatement = "! Account valid, logged-in";
                    } else {
                        returnStatement = "+Account valid, send password";
                    }
                } else {
                    returnStatement = "-Invalid account, try again";
                }


            } catch (FileNotFoundException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } catch (IOException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } 
            
        }
        
        return returnStatement;
        
    }
    
   public String PASS (String[] clientCommands) throws IOException{
        
        boolean passFound = false;
        String returnStatement = null;
        
        if(userAcct && userPass){
            //outToClient.writeBytes("!<user-id> logged in" + '\n');
            returnStatement = "!<user-id> logged in";
        } else {
        
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
                    if(user.equals(lineDets[0])){
                        if(clientCommands[1].equals(lineDets[2])){
                            passFound = true;
                        }
                    }
                    //outToClient.writeBytes(line + '\n');
                }

                if(passFound){
                    userPass = true;
                    if(userAcct){
                        returnStatement = "! Logged in";
                    } else {
                        returnStatement = "+Send account";
                    }
                } else {
                    returnStatement = "-Wrong password, try again";
                }

            } catch (FileNotFoundException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } catch (IOException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } 
            
        }
        
        return returnStatement;
        
    }
    
}
