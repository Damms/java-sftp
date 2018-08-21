/**
 * Jaedyn Damms - 955581057 - JDAM534
 * COMPSYS 725 - ASSIGNMENT 1
 * SFTP - CLIENT / SERVER APPLICATION
 **/

package Server;

import static Server.TCPServer.databasePath;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    Socket connectionSocket;
    File file;
    boolean connected;
    long BYTE_PER_MS = 50;

    /**
     * Constructor
    **/
    ClientConnection(Socket socket){
        this.connectionSocket = socket;
        this.connected = true;
        createConnections();
        sendMessage("+MIT-localhost SFTP Service");
        this.authenticationController = new AuthenticationController();
    }
    
    /**
     * Run when thread is started
     * Waits for command from client
     * Runs relevant function
    **/
    @Override
    public void run() {
	
	while(connected) {             
            
            try { // exit when done 
            
                String authenticationRespone;

                // Wait for command from client
                System.out.println("WAITING FOR INPUT");
                try { 
                    clientSentence = receiveMessage();
                } catch (IOException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("FROM CLIENT: " + clientSentence);

                String[] clientCommands = clientSentence.split(" ");

                // Run function for command specified by client
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

    /**
     * Creates the connections between server and client and other components used
    **/
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
            System.out.println("Database file found");
        } else {
            System.out.println("Databse file not found.");
            try {
                connectionSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }    
    
    /**
     * Get null terminated message from client
    **/
    private String receiveMessage() throws IOException {
        String sentence = "";
        int character;

        while (true){
            
            character = inFromClient.read();  // Read one character

            if (character == 0) { // null
                break;
            }

            sentence = sentence.concat(Character.toString((char)character)); // Add read character to command sentence
        }

        return sentence;
    }
    
    /**
     * Send null terminated message to client
    **/
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
    
    /**
     * Switch file transfer type
     * A = Ascii
     * B = Binary
     * C = Continuous
    **/
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

    /**
     * Resets Authentication
     * Closes connection
    **/
    private void DONE() throws IOException {
        authenticationController.reset();
        connected = false;
        sendMessage("+Connection closed");
        connectionSocket.close();
    }
    
    /**
     * LIST files and folders in specified directory
     * If directory isn't specified lists files and folders in current working directory
     * Supports two list types { F | V }
     * F: Non detailed list
     * V: Detailed list
     * NOTE: requested directory is appended onto Storage root NOT current working directory
    **/
    private void LIST(String[] clientCommands){
        
        String requestedDir;
        if(clientCommands.length == 2){ // means no requested directory provided - user current working directory
            requestedDir = currentDir; 
        } else { // request dir provided - user it
            requestedDir = storageRoot + clientCommands[2];
        }
        
        if(null == clientCommands[1]){
            sendMessage("-Incompatible type requested, supported types are { F | V }");
        }
        
        // Perform correct list for specified type
        else switch (clientCommands[1]) {
            
            // Non detailed list
            case "F":
               
                File dir = new File(requestedDir);
                
                if(dir.isDirectory() && dir.exists()){
                    
                    String fileListString = ("+" + requestedDir + '\n');
                    
                    // List Folders
                    String[] dirList = dir.list((File current, String name1) -> new File(current, name1).isDirectory());
                    for (String dirName : dirList) {
                        fileListString = fileListString.concat(("Folder: " + dirName  + '\n'));
                    }
                    
                    // List Files
                    File[] filesList = dir.listFiles(); 

                    for (File fileQuery : filesList) {

                        if (fileQuery.isFile()) {
                            fileListString = fileListString.concat("File: " + fileQuery.getName() + '\n');
                        }
                    }   

                    sendMessage(fileListString);
                } else {
                    sendMessage("-Directory doesn't exist");
                }
                
                break;
                
            // Detailed list
            case "V":
                
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                DecimalFormat df = new DecimalFormat("#.##"); 
                String fileName;
                String fileOwner = null;
                String createdDate = " ";
                String lastModified;
                String fileSize;
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

                            // Get correct unit for file size
                            switch (multiplication) {
                                case 1:
                                    fileSize = ( df.format(fileSizeDouble) + " B" ); //B
                                    break;
                                case 2:
                                    fileSize = ( df.format(fileSizeDouble) + " KB" ); //KB
                                    break;
                                case 3:
                                    fileSize = ( df.format(fileSizeDouble) + " MB" ); //MB
                                    break;
                                case 4:
                                    fileSize = ( df.format(fileSizeDouble) + " GB" ); //GB
                                    break;
                                default:
                                    fileSize = " ";
                                    break;
                            }

                            // Get file created time - Reference: https://stackoverflow.com/questions/2723838/determine-file-creation-date-in-java
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

                            // Get file name
                            fileName = fileQuery.getName();
                            
                            // Get last modified date
                            lastModified = sdf.format(fileQuery.lastModified());
                            
                            // Format output
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

    /**
     * Change working directory to directory specified
     * If user wishes to change working directory to root then "/" must be the specified directory
     * NOTE: requested directory is appended onto storage root NOT current working directory
    **/
    private void CDIR(String[] clientCommands) throws IOException {
        
        String requestedDir;
        
        // Get correct request directory
        if("/".equals(clientCommands[1])){
            requestedDir = (storageRoot);
        } else {
            requestedDir = (storageRoot + clientCommands[1] + "/");
        }
        
        // test file to check if requested directory is a directory
        File testFile = new File(requestedDir);
        
        if (!testFile.isDirectory()){
            
            sendMessage("-Can't connect to directory because: " + requestedDir + " is not a directory.");
             
        } else {
            
            // User needs to provide correct user/pass again to change dir
            boolean tempAuth;
            boolean tempAcctFound = false;
            boolean tempPassFound = false;
            
            if(authenticationController.superID){ // admin accounts
                tempAuth = true;
                
            } else {
                
                if("-".equals(authenticationController.acct)){ // USER doesn't require an account
                    sendMessage( "+directory ok, send password");
                    tempAcctFound = true;
                } else if("-".equals(authenticationController.pass)){ // USER doesn't require a password
                    sendMessage( "+directory ok, send account");
                    tempPassFound = true;
                } else { // USER needs both account and password
                    sendMessage( "+directory ok, send account/password");
                }
                tempAuth = false;
                
            }
            
            while(!tempAuth){
                
                // Get user input
                try { 
                    clientSentence = receiveMessage();
                } catch (IOException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("FROM CLIENT: " + clientSentence);

                String[] clientCommands2 = clientSentence.split(" ");
                String authenticationRespone;
                
                if(null == clientCommands2[0]){
                    System.out.println("-INVALID COMMAND");
                }
                
                // perform correct funtion
                else switch (clientCommands2[0]) {

                    case "ACCT":
                        
                        if(clientCommands2.length == 2){
                            tempAcctFound = authenticationController.checkAcct(clientCommands2[1]); // validate provide account
                            
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
                            tempPassFound = authenticationController.checkPass(clientCommands2[1]); // validate provide account
                            
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
    
    /**
     * Delete specified file
     * NOTE: specified file is searched from the current working directory NOT storage root
    **/
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
    
    /**
     * Renames specified file on server storage
     * NOTE: Specified file is searched from current working directory NOT storage root
    **/
    private void NAME(String[] clientCommands) throws IOException{
        
        File testFile = new File(currentDir + clientCommands[1]);
        String oldFileName = testFile.getName();
        
        if(testFile.exists()){
            
            sendMessage("+File exists");
      
            clientSentence = receiveMessage();
            String[] clientCommands2 = clientSentence.split(" ");     
            boolean cont = false; // boolean to make sure system can only proceed if TOBE is received
            
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
            } 
            else {
                
                // Rename file (or directory)
                boolean success = testFile.renameTo(file2);

                if (!success) {
                   // File was not successfully renamed
                   sendMessage("-File wasn't renamed because ");
                } else {
                   sendMessage("+" + oldFileName + " renamed to " + file2.getName());
                }
                
            }
 
        }
        else{
            // Can't find specified file
            sendMessage("-Can't find " + clientCommands[1]);
        }
        
    }

    /**
     * Send Specified file FROM Server TO Client
     * NOTE: Specified file is searched from current working directory NOT storage root
    **/
    private void RETR(String[] clientCommands) throws IOException {
        
        File requestedFile = new File(currentDir + clientCommands[1]);
        
        if (!requestedFile.exists()){
            sendMessage("-File doesn't exist");
        }
        else if (requestedFile.exists()){
            
            // Get size of requested file
            long length = requestedFile.length();
            sendMessage(Long.toString(length));
            
            // Receive response
            String userCommand2;
            String [] userCommands;
            
            // Only continue if SEND or STOP is received from client
            while(true){
                
                userCommand2 = receiveMessage();
                userCommands = userCommand2.split(" ");
                
                if("SEND".equals(userCommands[0])|| "STOP".equals(userCommands[0])){
                    break;
                }
                
            }
            
            if("SEND".equals(userCommands[0])){ // send file
                sendFileBytes(requestedFile);
            }
            else if ("STOP".equals(userCommands[0])){ // don't send file
                sendMessage("+ok, RETR aborted");
            }
            
        }
        
        
    }
    
    /**
     * Receives file FROM client and stores it in Servers storage
     * Three STOR types are supported { NEW | OLD | APP }
     * NEW: Generates new file if one with specified name exists
     * OLD: Overwrites file if one with specified name exists
     * APP: Appends onto existing file if one with specified name exists
     * NOTE: Specified file is searched from current working directory NOT storage root
    **/
    private void STOR(String[] clientCommands) {
        
        if(null == clientCommands[1]){
            sendMessage("STOR type not supported, supported types are { NEW | OLD | APP }");
        } else {
            
            String clientCommand;
            File newFile = new File(currentDir + clientCommands[2]); // create new file
            
            // Perform correct specified STOR type
            switch (clientCommands[1]) {
            
            case "NEW": // New generation of file
                
                if(newFile.exists() && newFile.isFile()){ // Must generate a new file

                    int fileVersion = 1;
                    String[] splitFileName = null;
                    boolean validFile = true;

                    // Keep updating file name until we find one which doesn't exist
                    while(newFile.exists()){

                        fileVersion++;
                        splitFileName = clientCommands[2].split("\\."); // split file name and the file extension

                        try {
                            newFile = new File(currentDir + splitFileName[0] + "(" + fileVersion + ")." + splitFileName[1]);
                        } catch(ArrayIndexOutOfBoundsException err){ // File doesn't have an extension so it's not supported
                            
                            System.out.println(err);
                            validFile = false;
                            break;

                        }

                    }
                    
                    if(validFile){
                        
                        sendMessage("+File exists, will create new generation of file");  
                        
                        try {
                        
                            // Get file size of file to be sent
                            clientCommand = receiveMessage();
                            long requestedFileSize = Long.valueOf(clientCommand);

                            getFile((splitFileName[0] + "(" + fileVersion + ")." + splitFileName[1]), false, requestedFileSize);

                        } catch (IOException ex) {

                            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);

                        }
                        
                    } else {
                        sendMessage("-File exists, but system doesn't support generations of files without an extension");
                    }
                    
                    
                } 
                else { // Create new file
                    
                    sendMessage("+File does not exist, will create new one");
                    
                    try {
                        
                        // Get file size of file to be sent
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
                    
                    // Get file size of file to be sent
                    clientCommand = receiveMessage();
                    long requestedFileSize = Long.valueOf(clientCommand);
                    getFile(clientCommands[2], false, requestedFileSize);
                    
                    
                } catch (IOException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
                
            case "APP":
                
                if(newFile.exists() && newFile.isFile()){ // Need to append onto file
                    
                    sendMessage("+Will append to file");
                    try {
                        
                        // Get file size of file to be sent
                        clientCommand = receiveMessage();
                        long requestedFileSize = Long.valueOf(clientCommand);
                        getFile(clientCommands[2], true, requestedFileSize);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                } 
                else { // Create new file
                    
                    sendMessage("+Will create file");
                    try {
                        
                        // Get file size of file to be sent
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
    
    /**
     * Retrieves file
     * Sends/receives messages to/from client and performs correct logic
    **/
    private void getFile(String outputFileName, boolean append, long requestedFileSize){
        
        File dir = new File(currentDir);

        // Check if there is enough space in directory to store file
        if(dir.getUsableSpace() > requestedFileSize){

            sendMessage("+ok, waiting for file");
            Boolean fileSent = false;
            
            try {
                fileSent = readFileBytes(outputFileName, append, requestedFileSize); // get file data from buffer
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

            if(fileSent){
                sendMessage("+Saved " + outputFileName);  
            } else {
                sendMessage("-Couldn't save because file transfer timeout");
            }

        } 
        else { // not enough space in directory

            sendMessage("-Not enough room, don't send it");

        }
        
    }
            
    /**
     * Sends file to client
     * Supports three file transfer modes controlled by TYPE function
     * ASCII, BINARY, CONTINUOUS
     * reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
     * reference: https://stackoverflow.com/questions/38732970/java-sending-and-receiving-file-over-sockets
    **/
    private void sendFileBytes(File requestedFile) throws IOException{
        
        if("A".equals(TYPE_TEXT)){ // ASCII mode
            
            byte[] bytes = new byte[(int) requestedFile.length()];
            
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(requestedFile))) {
                
                int count;
                outToClient.flush();
                
                // Read and send data for whole file
                while ((count = bis.read(bytes)) >= 0) {
                    outToClient.write(bytes, 0, count);
                }
                
                outToClient.flush();
                
            }
            
        }
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ // BINARY, CONTINUOUS Mode
            
            DataOutputStream fileDataToClient;
            
            try (FileInputStream fileStream = new FileInputStream(requestedFile)) {
                
                fileDataToClient = new DataOutputStream(new BufferedOutputStream(connectionSocket.getOutputStream()));
                int count;
                
                // Read and send data for whole file
                while ((count = fileStream.read()) >= 0) {
                    fileDataToClient.write(count);
                }
                fileDataToClient.flush();
            }
            
        }

    }
    
    /**
     * Receives files from client
     * Supports three file transfer modes controlled by TYPE function
     * ASCII, BINARY, CONTINUOUS
     * reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
     * reference: https://stackoverflow.com/questions/38732970/java-sending-and-receiving-file-over-sockets
    **/
    private boolean readFileBytes(String outputFileName, boolean append, long requestedFileSize) throws IOException{
        
        File newFile = new File(currentDir + outputFileName);
        Date d1;
        Date d2;
        long msPassed; // ms
        long waitTime = (requestedFileSize / BYTE_PER_MS) + BYTE_PER_MS;
        
        if("A".equals(TYPE_TEXT)){ // ASCII mode
            
            try (FileOutputStream fos = new FileOutputStream(newFile, append); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                d1 = new Date(); // start time
                
                // For read byte for the expected file size
                for(int j = 0; j < requestedFileSize; j++){
                    
                    // Get data from BufferedReader
                    bos.write(inFromClient.read());
                    
                    // get time
                    d2 = new Date(); 
                    msPassed = (d2.getTime() - d1.getTime());
                    System.out.println("Time passed: " + msPassed + " Timeout time: " + ((int) waitTime) );
                    
                    if(msPassed >= waitTime){ // timeout
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
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ // BINARY, CONTINUOUS mode
            
            try (FileOutputStream fos = new FileOutputStream(newFile, append)) {
                
                byte[] bytes = new byte[(int) requestedFileSize];
                DataInputStream fileDataFromClient = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));

                int bytesRead = 0;
                int count;
                d1 = new Date(); // start time

                // While haven't received expected amount of data or until timeout
                while (true) {
                    
                    // Get data
                    count = fileDataFromClient.read(bytes);
                    bytesRead += count;
                    fos.write(bytes, 0, count);
                    
                    System.out.println("Bytes Read Count: " + bytesRead + " File Size: " + ((int) requestedFileSize) );

                    if (bytesRead >= ((int) requestedFileSize)){ // If we read the expected amount of bytes then break loop
                        break;
                    }

                    // Get time
                    d2 = new Date(); // get time elapsed
                    msPassed = (d2.getTime() - d1.getTime());
                    System.out.println("Time passed: " + msPassed + " Timeout time: " + ((int) waitTime) );
                    
                    if(msPassed >= waitTime){ // timeout
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