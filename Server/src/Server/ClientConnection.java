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
        sendMessage("+MIT-localhost SFTP Service");
        //-MIT-localhost Out to Lunch
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
                    clientSentence = receiveMessage();
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
                            sendMessage(authenticationRespone);
                        } else {
                            sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                        }
                        break;

                    case "ACCT":
                        if(clientCommands.length == 2){
                            authenticationRespone = authenticationController.ACCT(clientCommands);
                            sendMessage(authenticationRespone);
                        } else {
                            sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                        }
                        break;

                    case "PASS":
                        if(clientCommands.length == 2){
                            authenticationRespone = authenticationController.PASS(clientCommands);
                            sendMessage(authenticationRespone);
                        } else {
                            sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                        }
                        break;

                    case "TYPE":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 2){
                                TYPE(clientCommands);
                            } else {
                                sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                            }
                        } else {
                            sendMessage("-Please log in: ");
                        }
                        break;
                        
                    case "LIST":
                        if(clientCommands.length >= 2){
                            if(authenticationController.authenticated){
                                LIST(clientCommands);
                            } else {
                                sendMessage("-Please log in: ");
                            }
                        } else {
                            sendMessage("-COMMAND EXPECTED AT LEAST 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                        }
                        break;
                        
                    case "CDIR":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 1 || clientCommands.length == 2){
                                CDIR(clientCommands);
                            } else {
                                sendMessage("-Can't connect to drectory because: COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                            }
                        } else {
                            sendMessage("-Please log in: ");
                        }
                        
                        break;
                        
                    case "KILL":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 2){
                                KILL(clientCommands);
                            } else {
                                sendMessage("-COMMAND EXPECTED AT LEAST 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                            }
                        } else {
                            sendMessage("-Please log in: ");
                        }
                        break;
                        
                    case "NAME":
                        if(authenticationController.authenticated){
                            if(clientCommands.length == 2){
                                NAME(clientCommands);
                            } else {
                                sendMessage("-COMMAND EXPECTED AT LEAST 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                            }
                        } else {
                            sendMessage("-Please log in: ");
                        }
                        break;

                    case "DONE":
                        DONE();

                    default:
                        sendMessage("-INVALID COMMAND");
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
    
    private String receiveMessage() throws IOException {
        String sentence = "";
        int character = 0;

        while (true){
            
            character = inFromClient.read();  // Read one character

            if (character == 0) { // null
                break;
            }

            sentence = sentence.concat(Character.toString((char)character));
        }

        return sentence;
    }
    
    private void sendMessage(String message){
        
        try { 
            outToClient.writeBytes(message + '\0');
        } catch (IOException ex) {
            try {
                connectionSocket.close();
            } catch (IOException ex1) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        
    }
    
    
    private void TYPE(String[] clientCommands) throws IOException{
        if(null == clientCommands[1]) {
            sendMessage("-Type not valid");
        } else switch (clientCommands[1]) {
            case "A":
                TYPE_TEXT = "Ascii";
                sendMessage("+Using " + TYPE_TEXT + " mode");
                break;
            case "B":
                TYPE_TEXT = "Binary";
                sendMessage("+Using " + TYPE_TEXT + " mode");
                break;
            case "C":
                TYPE_TEXT = "Continuous";
                sendMessage("+Using " + TYPE_TEXT + " mode");
                break;
            default:
                sendMessage("-Type not valid");
                break;
        }
        
    }

    private void DONE() throws IOException {
        authenticationController.reset();
        connected = false;
        sendMessage("+Connection closed");
    }
    
    private void LIST(String[] clientCommands) throws IOException{
        
        String requestedDir;
        if(clientCommands.length == 2){ // no requested directory
            requestedDir = currentDir; // change to equal current directory when cdir implemented
        } else {
            requestedDir = storageRoot + clientCommands[2];
        }
        
        if(null == clientCommands[1]){
            sendMessage("-Incompatible type requested, supported types are { F | V }");
        }
        
        else switch (clientCommands[1]) {
            
            case "F":
               
                System.out.println("F List");
                File dir = new File(requestedDir);
                File[] filesList = dir.listFiles(); 
                String fileListString = ("+" + requestedDir + '\n');
                
                for (File fileQuery : filesList) {
                    
                    if (fileQuery.isFile()) {
                        System.out.println(fileQuery.getName());
                        fileListString = fileListString.concat(fileQuery.getName() + '\n');
                    }
                }   
                
                sendMessage(fileListString);
                
                break;
                
            case "V":
                
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
                String fileListString2 = ("+" + requestedDir + '\n');
                
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
                                    (creationDate.getMonth() + 1) + "/" +
                                    creationDate.getDate() + "/" +
                                    (creationDate.getYear() + 1900);
                        }
                        
                        fileName = fileQuery.getName();
                        lastModified = sdf.format(fileQuery.lastModified());
                        String detailedFile = " File Name: " + fileName +  " | File Owner: " + fileOwner + " | Date Created: " + createdDate + " | Last Modified: " + lastModified + " | File Size: " + fileSize + '\n';
                        fileListString2 = fileListString2.concat(detailedFile);
                    }
                    
                }   
                
                sendMessage(fileListString2);
                
                break;
                
            default:
                
                sendMessage("-Incompatible type requested, supported types are { F | V }");
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
            
            sendMessage("-Can't connect to directory because: " + requestedDir + " is not a directory.");
             
        } else {
            
            boolean tempAuth;
            if(authenticationController.superID){
                tempAuth = true;
                
            } else {
                sendMessage( "+directory ok, send account/password");
                tempAuth = false;
            }
            boolean tempAcctFound = false;
            boolean tempPassFound = false;
            
            while(!tempAuth){
                
                try { 
                    clientSentence = receiveMessage();
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
                                sendMessage("+account ok, send password");
                            } else {
                                sendMessage("-invalid account");
                            }
                            
                        } 
                        else {
                            sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands2.length));
                        }
                        
                        break;

                    case "PASS":
                        
                        if(clientCommands2.length == 2){
                            tempPassFound = authenticationController.checkPass(clientCommands2[1]);
                            
                            if(tempAcctFound && tempPassFound){
                                tempAuth = true;
                            } else if(tempPassFound){
                                sendMessage("+password ok, send account");
                            } else {
                                sendMessage("-invalid password");
                            }
                            
                        } 
                        else {
                            sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands2.length));
                        }
                        break;

                    default:
                        sendMessage("-INVALID COMMAND");
                        break;
                }
            }
            currentDir = requestedDir;
            sendMessage("!Changed working dir to" + currentDir);
                
        }
    }
    
    private void KILL(String[] clientCommands) throws IOException{
        
        // Reference: https://www.geeksforgeeks.org/delete-file-using-java/
        File testFile = new File(currentDir + clientCommands[1]);
        String fileName = testFile.getName();
        
        if(testFile.delete()){
            sendMessage("+" + fileName + " deleted");
        }
        else{
            sendMessage("-Not deleted because");
        }
        
    }
    
    private void NAME(String[] clientCommands) throws IOException{
        
        File testFile = new File(currentDir + clientCommands[1]);
        String oldFileName = testFile.getName();
        
        if(testFile.exists()){
            
            sendMessage("+File exists");
            System.out.println("+File exists" + '\n');
      
            clientSentence = receiveMessage();
            String[] clientCommands2 = clientSentence.split(" ");     
            boolean cont = false;
            
            if("TOBE".equals(clientCommands2[0]) && clientCommands.length == 2){
                cont = true;
            }
            
            while(!cont){
                clientSentence = receiveMessage();
                clientCommands2 = clientSentence.split(" ");
                if("TOBE".equals(clientCommands2[0]) && clientCommands.length == 2){
                    cont = true;
                }
            }
            
            // Reference: https://stackoverflow.com/questions/1158777/rename-a-file-using-java
            File file2 = new File(currentDir + clientCommands2[1]);

            if (file2.exists()){
               sendMessage("-File wasn't renamed because file with specified name already exists.");
               System.out.println("-File wasn't renamed because file with specified name already exists.");
            } 
            else {
                // Rename file (or directory)
                boolean success = testFile.renameTo(file2);

                if (!success) {
                   // File was not successfully renamed
                   sendMessage("-File wasn't renamed because ");
                   System.out.println("-File wasn't renamed because ");
                } else {
                   sendMessage("+" + oldFileName + " renamed to " + file2.getName());
                   System.out.println("+" + oldFileName + " renamed to " + file2.getName());
                }
                
            }
 
        }
        else{
            sendMessage("-Can't find " + clientCommands[1]);
            System.out.println("-Can't find " + clientCommands[1]);
        }
        
    }
       
}