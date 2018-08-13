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
    
    AuthenticationController authenticationController;
    BufferedReader reader = null;
    BufferedReader inFromClient = null;
    DataOutputStream  outToClient = null;
    String clientSentence; 
    String capitalizedSentence; 
    String TYPE_TEXT;
    String storageRoot = "./storage/";
    //String txtContent;
    Socket connectionSocket;
    File file;
    boolean connected;

    
    ClientConnection(Socket socket){
        this.connectionSocket = socket;
        this.connected = true;
        createConnections();
        try {
            outToClient.writeBytes("+MIT-localhost SFTP Service\n");
            //-MIT-localhost Out to Lunch
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.authenticationController = new AuthenticationController();
    }
    
    @Override
    public void run() { // run when call start
	
	while(connected) {             
            
            try { // exit when done 
            
                String authenticationRespone;

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
                    System.out.println("-INVALID COMMAND");
                }
                else switch (clientCommands[0]) {

                    case "USER":
                        if(clientCommands.length == 2){
                            authenticationRespone = authenticationController.USER(clientCommands);
                            outToClient.writeBytes(authenticationRespone + '\n');
                        } else {
                            outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                        }
                        break;

                    case "ACCT":
                        if(clientCommands.length == 2){
                            authenticationRespone = authenticationController.ACCT(clientCommands);
                            outToClient.writeBytes(authenticationRespone + '\n');
                        } else {
                            outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                        }
                        break;

                    case "PASS":
                        if(clientCommands.length == 2){
                            authenticationRespone = authenticationController.PASS(clientCommands);
                            outToClient.writeBytes(authenticationRespone + '\n');
                        } else {
                            outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                        }
                        break;

                    case "TYPE":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 2){
                                TYPE(clientCommands);
                            } else {
                                outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                            }
                        } else {
                            outToClient.writeBytes("-Please log in: " + '\n');
                        }
                        break;
                        
                    case "LIST":
                        if(clientCommands.length >= 2){
                            if(authenticationController.authenticated){
                                LIST(clientCommands);
                            } else {
                                outToClient.writeBytes("-Please log in: " + '\n');
                                outToClient.writeBytes("\0" + '\n');
                            }
                        } else {
                            outToClient.writeBytes("-COMMAND EXPECTED AT LEAST 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                            outToClient.writeBytes("\0" + '\n');
                        }
                        break;

                    case "DONE":
                        DONE();

                    default:
                        System.out.println("-INVALID COMMAND");
                        break;

                }
            
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
	    
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
    
    private void TYPE(String[] clientCommands) throws IOException{
        if(null == clientCommands[1]) {
            outToClient.writeBytes("-Type not valid" + '\n');
        } else switch (clientCommands[1]) {
            case "A":
                TYPE_TEXT = "Ascii";
                outToClient.writeBytes("+Using " + TYPE_TEXT + " mode" + '\n');
                break;
            case "B":
                TYPE_TEXT = "Binary";
                outToClient.writeBytes("+Using " + TYPE_TEXT + " mode" + '\n');
                break;
            case "C":
                TYPE_TEXT = "Continuous";
                outToClient.writeBytes("+Using " + TYPE_TEXT + " mode" + '\n');
                break;
            default:
                outToClient.writeBytes("-Type not valid" + '\n');
                break;
        }
        
    }

    private void DONE() throws IOException {
        authenticationController.reset();
        connected = false;
        outToClient.writeBytes("+Connection closed" + '\n');
    }
    
    private void LIST(String[] clientCommands) throws IOException{
        
        String requestedDir;
        if(clientCommands.length == 2){ // no requested directory
            requestedDir = ""; // change to equal current directory when cdir implemented
        } else {
            requestedDir = clientCommands[2];
        }
        
        if(null == clientCommands[1]){
            outToClient.writeBytes("-Incompatible type requested, supported types are { F | V }" + '\n');
            outToClient.writeBytes("\0" + '\n');
        }
        
        else switch (clientCommands[1]) {
            case "F":
                System.out.println("F List");
                File dir = new File(storageRoot + requestedDir);
                File[] filesList = dir.listFiles();
                for (File fileQuery : filesList) {
                    if (fileQuery.isFile()) {
                        outToClient.writeBytes(fileQuery.getName() + '\n');
                        System.out.println(fileQuery.getName());
                    }
                }   outToClient.writeBytes("\0" + '\n');
                break;
            case "V":
                System.out.println("V List");
                break;
            default:
                outToClient.writeBytes("-Incompatible type requested, supported types are { F | V }" + '\n');
                outToClient.writeBytes("\0" + '\n');
                break;
        }
    }
       
}