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
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.DateFormat;
import java.util.*;

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
    String TYPE_TEXT = "B";
    String storageRoot = "./storage/";
    String currentDir = "./storage/";
    //String txtContent;
    Socket connectionSocket;
    File file;
    boolean connected;
    long BYTE_PER_MS = 50;

    
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
                            if(clientCommands.length == 2){
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
                        
                    case "RETR":
                        if(clientCommands.length == 2){
                            RETR(clientCommands);
                        } else {
                            sendMessage("-COMMAND EXPECTED 2 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
                        }
                        break;
                        
                    case "STOR":
                        if(clientCommands.length == 3){
                            STOR(clientCommands);
                        } else {
                            sendMessage("-COMMAND EXPECTED 3 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
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
                TYPE_TEXT = "A";
                sendMessage("+Using " + TYPE_TEXT + " mode");
                break;
            case "B":
                TYPE_TEXT = "B";
                sendMessage("+Using " + TYPE_TEXT + " mode");
                break;
            case "C":
                TYPE_TEXT = "C";
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
        connectionSocket.close();
    }
    
    private void LIST(String[] clientCommands){
        
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
                
                if(dir.isDirectory() && dir.exists()){
                    
                    String fileListString = ("+" + requestedDir + '\n');
                    
                    // List Folders
                    String[] dirList = dir.list((File current, String name1) -> new File(current, name1).isDirectory());
                    for (String dirName : dirList) {
                        fileListString = fileListString.concat(("Folder: " + dirName  + '\n'));
                    }
                    
                    // List File
                    File[] filesList = dir.listFiles(); 

                    for (File fileQuery : filesList) {

                        if (fileQuery.isFile()) {
                            System.out.println(fileQuery.getName());
                            fileListString = fileListString.concat("File: " + fileQuery.getName() + '\n');
                        }
                    }   

                    sendMessage(fileListString);
                } else {
                    sendMessage("-Directory doesn't exist");
                }
                
                break;
                
            case "V":
                
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                DecimalFormat df = new DecimalFormat("#.##"); 
                String fileName;
                String fileOwner = null;
                String createdDate = " ";
                String lastModified;
                String fileSize;
                System.out.println("V List");
                File dir2 = new File(requestedDir);
                
                if(dir2.isDirectory() && dir2.exists()){
                    
                    String fileListString2 = ("+" + requestedDir + '\n');
                    
                    // List Folders
                    String[] dirList2 = dir2.list((File current, String name1) -> new File(current, name1).isDirectory());
                    for (String dirName : dirList2) {
                        fileListString2 = fileListString2.concat(("Folder: " + dirName  + '\n'));
                    }
                    
                    // List Files
                    File[] filesList2 = dir2.listFiles();
                    

                    for (File fileQuery : filesList2) {
                        if (fileQuery.isFile()) {
                            Path filePath = fileQuery.toPath();
                            try {
                                fileOwner = Files.getOwner(fileQuery.toPath()).getName();
                            } catch (IOException ex) {
                                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            // File Size
                            int multiplication = 1;
                            long fileSizeDouble = fileQuery.length();
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
                            String detailedFile = "File Name: " + fileName +  " | File Owner: " + fileOwner + " | Date Created: " + createdDate + " | Last Modified: " + lastModified + " | File Size: " + fileSize + '\n';
                            fileListString2 = fileListString2.concat(detailedFile);
                        }

                    }   

                    sendMessage(fileListString2);
                } else {
                    sendMessage("-Directory doesn't exist");
                }
                break;
                
            default:
                
                sendMessage("-Incompatible type requested, supported types are { F | V }");
                break;
                
        }
    }

    private void CDIR(String[] clientCommands) throws IOException {
        
        String requestedDir;
        if("/".equals(clientCommands[1])){
            requestedDir = (storageRoot);
        } else {
            requestedDir = (storageRoot + clientCommands[1] + "/");
        }
        File testFile = new File(requestedDir);
        
        if (!testFile.isDirectory()){
            
            sendMessage("-Can't connect to directory because: " + requestedDir + " is not a directory.");
             
        } else {
            
            boolean tempAuth;
            boolean tempAcctFound = false;
            boolean tempPassFound = false;
            
            if(authenticationController.superID){
                tempAuth = true;
                
            } else {
                if("-".equals(authenticationController.acct)){
                    sendMessage( "+directory ok, send password");
                    tempAcctFound = true;
                } else if("-".equals(authenticationController.pass)){
                    sendMessage( "+directory ok, send account");
                    tempPassFound = true;
                } else {
                    sendMessage( "+directory ok, send account/password");
                }
                tempAuth = false;
            }
            
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
        Path path = testFile.toPath();
        
        try {
            Files.delete(path);
            sendMessage("+" + fileName + " deleted");
        } catch(NoSuchFileException err){
            System.out.println("Error: " + err);
            sendMessage("-Not deleted because file doesn't exist.");
        } catch (IOException err){
            System.out.println("Error: " + err);
            sendMessage("-Not deleted because file may be protected.");     
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

    private void RETR(String[] clientCommands) throws IOException {
        
        File requestedFile = new File(currentDir + clientCommands[1]);
        
        if (!requestedFile.exists()){
            sendMessage("-File doesn't exist");
        }
        else if (requestedFile.exists()){
            
            long length = requestedFile.length();
            System.out.println("File size: " + length);
            sendMessage(Long.toString(length));
            
            // Receive response
            String userCommand2;
            String [] userCommands;
            while(true){
                
                System.out.println("Type: SEND or STOP");
                userCommand2 = receiveMessage();
                userCommands = userCommand2.split(" ");
                
                if("SEND".equals(userCommands[0])|| "STOP".equals(userCommands[0])){
                    break;
                }
                
            }
            
            if("SEND".equals(userCommands[0])){
                sendFileBytes(requestedFile);
            }
            else if ("STOP".equals(userCommands[0])){
                sendMessage("+ok, RETR aborted");
            }
            
        }
        
        
    }
    
    
    private void STOR(String[] clientCommands) {
        
        if(null == clientCommands[1]){
            sendMessage("STOR type not supported, supported types are { NEW | OLD | APP }");
        } else {
            
            String clientCommand = "";
            File newFile = new File(currentDir + clientCommands[2]);
            
            switch (clientCommands[1]) {
            
            case "NEW": // New generation of file
                
                if(newFile.exists() && newFile.isFile()){ 

                    int fileVersion = 1;
                    String[] splitFileName = null;
                    boolean validFile = true;

                    while(newFile.exists()){

                        fileVersion++;
                        splitFileName = clientCommands[2].split("\\."); // split file name and the file extension
                        System.out.println("Filename: " + clientCommands[2]);

                        try {
                            newFile = new File(currentDir + splitFileName[0] + "(" + fileVersion + ")." + splitFileName[1]);
                        } catch(ArrayIndexOutOfBoundsException err){
                            System.out.println(err);
                            validFile = false;
                            break;

                        }

                    }
                    
                    if(validFile){
                        
                        sendMessage("+File exists, will create new generation of file");    
                        try {
                        
                            clientCommand = receiveMessage();
                            long requestedFileSize = Long.valueOf(clientCommand);

                            getFile((splitFileName[0] + "(" + fileVersion + ")." + splitFileName[1]), false, requestedFileSize);

                        } catch (IOException ex) {

                            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);

                        }
                        
                    } else {
                        sendMessage("-File exists, but system doesn't support generations of files without an extension");
                    }
                    
                    
                } else {
                    
                    sendMessage("+File does not exist, will create new one");
                    
                    try {
                        
                        clientCommand = receiveMessage();
                        long requestedFileSize = Long.valueOf(clientCommand);
                        getFile(clientCommands[2], false, requestedFileSize);
                        
                    } catch (IOException ex) {
                        
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                        
                    }
                    
                }
                break;
                
            case "OLD":
                
                if(newFile.exists() && newFile.isFile()){ 
                    
                    sendMessage("+Will write over old file");
                    
                } else {
                    
                    sendMessage("+Will create new file");
                    
                }
                
                try {
                    
                    clientCommand = receiveMessage();
                    long requestedFileSize = Long.valueOf(clientCommand);
                    getFile(clientCommands[2], false, requestedFileSize);
                    
                    
                } catch (IOException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
                
            case "APP":
                
                if(newFile.exists() && newFile.isFile()){ 
                    
                    sendMessage("+Will append to file");
                    try {
                        
                        clientCommand = receiveMessage();
                        long requestedFileSize = Long.valueOf(clientCommand);
                        getFile(clientCommands[2], true, requestedFileSize);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                } else {
                    
                    sendMessage("+Will create file");
                    try {
                        
                        clientCommand = receiveMessage();
                        long requestedFileSize = Long.valueOf(clientCommand);
                        getFile(clientCommands[2], false, requestedFileSize);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
                break;
                
            default:
                sendMessage("-STOR type not supported, supported types are { NEW | OLD | APP }");
                break;
                
            }
            
        }
        
    }
    
    private void getFile(String outputFileName, boolean append, long requestedFileSize){
        
        File dir = new File(currentDir);
        System.out.println("Available space: " + dir.getUsableSpace());

        if(dir.getUsableSpace() > requestedFileSize){

            sendMessage("+ok, waiting for file");
            Boolean fileSent = false;
            try {
                fileSent = readFileBytes(outputFileName, append, requestedFileSize);
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            if(fileSent){
                sendMessage("+Saved " + outputFileName);  
            } else {
                sendMessage("-Couldn't save because file transfer timeout");
            }

        } else {

            sendMessage("-Not enough room, don't send it");

        }
        
    }
            
    // reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
    // reference: https://stackoverflow.com/questions/38732970/java-sending-and-receiving-file-over-sockets
    private void sendFileBytes(File requestedFile) throws IOException{
        
        if("A".equals(TYPE_TEXT)){
            
            byte[] bytes = new byte[(int) requestedFile.length()];
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(requestedFile))) {
                outToClient.flush();
                // Read and send by byte
                int count = 0;
                while ((count = bis.read(bytes)) >= 0) {
                    outToClient.write(bytes, 0, count);
                }
                outToClient.flush();
            }
            
        }
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ // Binary, Continuous
            
            DataOutputStream fileDataToClient;
            try ( 
                FileInputStream fileStream = new FileInputStream(requestedFile)) {
                fileDataToClient = new DataOutputStream(new BufferedOutputStream(connectionSocket.getOutputStream()));
                int count;
                while ((count = fileStream.read()) >= 0) {
                    fileDataToClient.write(count);
                }
                fileDataToClient.flush();
            }
            
        }

    }
    
    // reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
    private boolean readFileBytes(String outputFileName, boolean append, long requestedFileSize) throws IOException{
        
        File newFile = new File(currentDir + outputFileName);
        Date d1;
        Date d2;
        long msPassed; // ms
        long waitTime = (requestedFileSize / BYTE_PER_MS) + BYTE_PER_MS;
        
        if("A".equals(TYPE_TEXT)){ // Ascii
            
            try (FileOutputStream fos = new FileOutputStream(newFile, append); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                d1 = new Date();
                for(int j = 0; j < requestedFileSize; j++){
                    
                    bos.write(inFromClient.read());
                    
                    d2 = new Date();
                    msPassed = (d2.getTime() - d1.getTime());
                    System.out.println("Time passed: " + msPassed + " Timeout time: " + ((int) waitTime) );
                    
                    if(msPassed >= waitTime){
                        bos.flush();
                        bos.close();
                        fos.close();
                        return false;
                    }
                    
                }   
                System.out.println("File received");
                bos.flush();
                bos.close();
                fos.close();
            }
            
        }
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ 
            
            try (FileOutputStream fos = new FileOutputStream(newFile, append)) { // Binary, Continuous
                
                byte[] bytes = new byte[(int) requestedFileSize];
                DataInputStream fileDataFromClient = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));

                int bytesRead = 0;
                int count;
                d1 = new Date();

                while (true) {
                    count = fileDataFromClient.read(bytes);
                    bytesRead += count;
                    fos.write(bytes, 0, count);
                    System.out.println("Bytes Read Count: " + bytesRead + " File Size: " + ((int) requestedFileSize) );

                    if (bytesRead >= ((int) requestedFileSize)){ // MAX - If it's above 8192 you'll receive garbage value
                        System.out.println("BREAK " );
                        break;
                    }

                    d2 = new Date();
                    msPassed = (d2.getTime() - d1.getTime());
                    System.out.println("Time passed: " + msPassed + " Timeout time: " + ((int) waitTime) );
                    if(msPassed >= waitTime){
                        fos.flush();
                        return false;
                    }

                }
                fos.flush();
            }
            
        }
        
        return true;
        
    }

    
       
}