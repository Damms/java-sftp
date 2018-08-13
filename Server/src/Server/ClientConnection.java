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
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    String currentDir = "./storage/";
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
                        
                    case "CDIR":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 1 || clientCommands.length == 2){
                                CDIR(clientCommands);
                            } else {
                                outToClient.writeBytes("-Can't connect to drectory because: COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                            }
                        } else {
                            outToClient.writeBytes("-Please log in: " + '\n');
                        }
                        
                        break;
                        
                    case "KILL":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 2){
                                KILL(clientCommands);
                            } else {
                                outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length) + '\n');
                            }
                        } else {
                            outToClient.writeBytes("-Please log in: " + '\n');
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
            requestedDir = currentDir; // change to equal current directory when cdir implemented
        } else {
            requestedDir = storageRoot + clientCommands[2];
        }
        
        if(null == clientCommands[1]){
            outToClient.writeBytes("-Incompatible type requested, supported types are { F | V }" + '\n');
            outToClient.writeBytes("\0" + '\n');
        }
        
        else switch (clientCommands[1]) {
            
            case "F":
                outToClient.writeBytes("+" + requestedDir + '\n');
                System.out.println("F List");
                File dir = new File(requestedDir);
                File[] filesList = dir.listFiles();
                for (File fileQuery : filesList) {
                    if (fileQuery.isFile()) {
                        outToClient.writeBytes(fileQuery.getName() + '\n');
                        System.out.println(fileQuery.getName());
                    }
                }   outToClient.writeBytes("\0" + '\n');
                break;
                
            case "V":
                outToClient.writeBytes("+" + requestedDir + '\n');
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                DecimalFormat df = new DecimalFormat("#.##"); 
                String fileName;
                String fileOwner;
                String createdDate = " ";
                String lastModified;
                String fileSize;
                System.out.println("V List");
                File dir2 = new File(requestedDir);
                File[] filesList2 = dir2.listFiles();
                
                for (File fileQuery : filesList2) {
                    if (fileQuery.isFile()) {
                        Path filePath = fileQuery.toPath();
                        fileOwner = Files.getOwner(fileQuery.toPath()).getName();
                        
                        // File Size
                        int multiplication = 1;
                        double fileSizeDouble = fileQuery.length();
                        while(fileSizeDouble > 1024 && multiplication < 4){
                            fileSizeDouble = fileSizeDouble / 1024;
                            multiplication += 1;
                        }
                        
                        switch (multiplication) {
                            case 1:
                                fileSize = ( df.format(fileSizeDouble) + " B" ); //B
                                break;
                            case 2:
                                fileSize = ( df.format(fileSizeDouble) + " KB" ); //B
                                break;
                            case 3:
                                fileSize = ( df.format(fileSizeDouble) + " MB" ); //B
                                break;
                            case 4:
                                fileSize = ( df.format(fileSizeDouble) + " GB" ); //B
                                break;
                            default:
                                fileSize = " ";
                                break;
                        }
                        
                        // Created Time - Reference: https://stackoverflow.com/questions/2723838/determine-file-creation-date-in-java
                        
                        BasicFileAttributes attributes = null;
                        try
                        {
                            attributes =
                                    Files.readAttributes(filePath, BasicFileAttributes.class);
                        }
                        catch (IOException exception)
                        {
                            System.out.println("Exception handled when trying to get file " +
                                    "attributes: " + exception.getMessage());
                        }
                        long milliseconds = attributes.creationTime().to(TimeUnit.MILLISECONDS);
                        if((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE))
                        {
                            Date creationDate =
                                    new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));

                            createdDate = 
                                    creationDate.getDate() + "/" +
                                    (creationDate.getMonth() + 1) + "/" +
                                    (creationDate.getYear() + 1900);
                        }
                        
                        fileName = fileQuery.getName();
                        lastModified = sdf.format(fileQuery.lastModified());
                        outToClient.writeBytes(" File Name: " + fileName +  " | File Owner: " + fileOwner + " | Created Date: " + createdDate + " | Last Modified: " + lastModified + " | File Size: " + fileSize + '\n');
                    }
                }   outToClient.writeBytes("\0" + '\n');
                break;
                
            default:
                outToClient.writeBytes("-Incompatible type requested, supported types are { F | V }" + '\n');
                outToClient.writeBytes("\0" + '\n');
                break;
                
        }
    }

    private void CDIR(String[] clientCommands) throws IOException {
        
        String requestedDir;
        if(clientCommands.length == 1){
            requestedDir = (storageRoot);
        } else {
            requestedDir = (storageRoot + clientCommands[1] + "/");
        }
        File testFile = new File(requestedDir);
        
        if (!testFile.isDirectory()){
            
             outToClient.writeBytes( "-Can't connect to directory because: " + requestedDir + " is not a directory." + '\n');
             
        } else {
            
            outToClient.writeBytes( "+directory ok, send account/password" + '\n');
            boolean tempAuth = false;
            boolean tempAcctFound = false;
            boolean tempPassFound = false;
            
            while(!tempAuth){
                try { 
                    clientSentence = inFromClient.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("GOT INPUT: " + clientSentence);

                String[] clientCommands2 = clientSentence.split(" ");
                String authenticationRespone;
                if(null == clientCommands2[0]){
                    System.out.println("-INVALID COMMAND");
                }
                else switch (clientCommands2[0]) {

                    case "ACCT":
                        if(clientCommands2.length == 2){
                            tempAcctFound = authenticationController.checkAcct(clientCommands2[1]);
                            if(tempAcctFound && tempPassFound){
                                tempAuth = true;
                            } else if(tempAcctFound){
                                outToClient.writeBytes("+account ok, send password" + '\n');
                            } else {
                                outToClient.writeBytes("-invalid account" + '\n');
                            }
                        } else {
                            outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands2.length) + '\n');
                        }
                        break;

                    case "PASS":
                        if(clientCommands2.length == 2){
                            tempPassFound = authenticationController.checkPass(clientCommands2[1]);
                            if(tempAcctFound && tempPassFound){
                                tempAuth = true;
                            } else if(tempPassFound){
                                outToClient.writeBytes("+password ok, send account" + '\n');
                            } else {
                                outToClient.writeBytes("-invalid password" + '\n');
                            }
                        } else {
                            outToClient.writeBytes("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands2.length) + '\n');
                        }
                        break;

                    default:
                        outToClient.writeBytes("-INVALID COMMAND");
                        break;
                }
            }
            currentDir = requestedDir;
            outToClient.writeBytes("!Changed working dir to" + currentDir + '\n');
                
        }
    }
    
    private void KILL(String[] clientCommands) throws IOException{
        File testFile = new File(currentDir + clientCommands[1]);
        String fileName = testFile.getName();
        if(testFile.delete())
        {
            outToClient.writeBytes("+" + fileName + " deleted" + '\n');
        }
        else
        {
            outToClient.writeBytes("-Not deleted because" + '\n');
        }
    }
       
}