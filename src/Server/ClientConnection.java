/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import static Server.TCPServer.databasePath;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jaedyn
 */
public class ClientConnection extends Thread {
    
    String[] sftpCommands = {"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"};
    BufferedReader reader = null;
    BufferedReader inFromClient = null;
    DataOutputStream  outToClient = null;
    String clientSentence; 
    String capitalizedSentence; 
    //String txtContent;
    Socket connectionSocket;
    File file;
    boolean userVerified = false;
    boolean userAcct = false;
    boolean userPass = false;
    String user = "";
    String acct = "";
    String pass = "";
    
    ClientConnection(Socket socket){
        this.connectionSocket = socket;
    }
    
    @Override
    public void run() { // run when call start
        
        createConnections();
	
	while(true) { // exit when done 
            
            // original
            System.out.println("WAITING FOR INPUT");
            try { 
                clientSentence = inFromClient.readLine();
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println("GOT INPUT: " + clientSentence);
            
            String[] clientCommands = clientSentence.split(" ");
            
            if(null == clientCommands[0]){
                System.out.println("INVALID COMMAND");
            }
            else switch (clientCommands[0]) {
                case "USER":
                    try {
                        USER(clientCommands);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }   break;
                case "ACCT":
                    try {
                        ACCT(clientCommands);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }   break;
                case "PASS":
                    try {
                        PASS(clientCommands);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }   break;
                default:
                    System.out.println("INVALID COMMAND");
                    break;
            }
            
            // if "DONE" clientSocket.close() exit while loop
	    
        } 
        
    }

    private void createConnections() {
        
        // Input Connection
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Output Connection
        try { 
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Database Connection
        file = new File(databasePath);
        if(file.exists() && !file.isDirectory()) { 
            System.out.println("Database file found :)");
        } else {
            System.out.println("Databse file not found.");
        }
        
    }
    
    private void USER(String[] clientCommands) throws IOException {
        boolean userFound = false;
        String returnStatement;
               
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
            outToClient.writeBytes(returnStatement + '\n');


        } catch (FileNotFoundException e) {

            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

        } catch (IOException e) {

            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

        } 
    
    }
    
    
    private void ACCT (String[] clientCommands) throws IOException{
        
        boolean acctFound = false;
        String returnStatement;
        
        if(userAcct && userPass){
            outToClient.writeBytes("!<user-id> logged in" + '\n');
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
                outToClient.writeBytes(returnStatement + '\n');


            } catch (FileNotFoundException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } catch (IOException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } 
            
        }
        
    }
    
   private void PASS (String[] clientCommands) throws IOException{
        
        boolean passFound = false;
        String returnStatement;
        
        if(userAcct && userPass){
            outToClient.writeBytes("!<user-id> logged in" + '\n');
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
                outToClient.writeBytes(returnStatement + '\n');


            } catch (FileNotFoundException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } catch (IOException e) {

                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, e);

            } 
            
        }
        
    }
    
}
