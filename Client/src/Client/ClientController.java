/**
 * Jaedyn Damms - 955581057 - JDAM534
 * COMPSYS 725 - ASSIGNMENT 1
 * SFTP - CLIENT / SERVER APPLICATION
 **/

package Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jaedyn
 */
public class ClientController {
    
    BufferedReader inFromUser;
    BufferedReader inFromServer;
    DataOutputStream outToServer;
    Socket clientSocket = null; 
    String TYPE_TEXT = "B";
    String storageRoot = "./storage/";
    boolean connected = false;
    long BYTE_PER_MS = 32;
    
    /**
     * Function the starts/runs the client controller
     * Sets up needed connections
     * Runs relevant functions for user commands
     * @throws java.io.IOException
    **/
    public void run() throws IOException {
        
        createConnections();
        String userCommand;
        
        // Wait for connection response from server
        String serverResponse = receiveMessage();
        System.out.println("FROM SERVER: " + serverResponse);
        if(serverResponse.contains("+MIT")){
            connected = true;
            System.out.println("Successfully established connection to server");
        }
        
        while(connected){

            // Get user input
            userCommand = inFromUser.readLine();
            
            String[] userCommands = userCommand.split(" ");

            // Run relevant function for user command
            if(null != userCommands[0])switch (userCommands[0]) {
                
                case "USER":
                    USER(userCommand);
                    break;
                case "ACCT":
                    ACCT(userCommand);
                    break;
                case "PASS":
                    PASS(userCommand);
                    break;
                case "TYPE":
                    TYPE(userCommand);
                    break;
                case "DONE":
                    DONE(userCommand);
                    break;
                case "LIST":
                    LIST(userCommand);
                    break;
                case "CDIR":
                    CDIR(userCommand);
                    break;
                case "KILL":
                    KILL(userCommand);
                    break;
                case "NAME":
                    NAME(userCommand);
                    break;
                case "RETR":
                    RETR(userCommand);
                    break;
                case "STOR":
                    STOR(userCommand);
                    break;
                default:
                    System.out.println("-INVALID COMMAND");
                    break;
                    
            }
            
        }
        
        System.out.println("Client will now close");
        
    }
    
    /**
     * Creates connections needed between client and server
    **/
    private void createConnections() throws IOException {
        
        // Connection to server
        try {
            clientSocket = new Socket("localhost", 6789);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // User input
        inFromUser = 
            new BufferedReader(new InputStreamReader(System.in)); 

        // Data stream to server
        outToServer = 
            new DataOutputStream(clientSocket.getOutputStream()); 
        
        // Data stream from server
        inFromServer = 
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
        
        
    }
    
    /**
     * receive null terminated message from server
    **/
    private String receiveMessage() throws IOException {
        String sentence = "";
        int character;

        while (true){
            
            character = inFromServer.read();  // Read one character

            if (character == 0) { // null
                break;
            }

            sentence = sentence.concat(Character.toString((char)character));
        }

        return sentence;
    }
    
    /**
     * Sends null terminated message to server
    **/
    private void sendMessage(String message) throws IOException{
        outToServer.writeBytes(message + '\0'); 
    }
    
    /**
     * Requests server to run USER command
     * USER command is to authenticate specified USER
    **/
    private void USER(String command) throws IOException {
        
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    /**
     * Requests server to run ACCT command
     * ACCT command is to authenticate specified ACCT
    **/
    private void ACCT(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    /**
     * Requests server to run PASS command
     * PASS command is to authenticate specified PASS
    **/
    private void PASS(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    /**
     * Requests server to run TYPE command
     * TYPE command is to change File Transfer Type
     * Then changes TYPE for client
    **/
    private void TYPE(String command) throws IOException {

        String [] splitCommand = command.split(" ");
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 
        
        // Change File Transfer Mode
        if('+' == serverResponse.charAt(0)){
            TYPE_TEXT = splitCommand[1];
        }

    }
    
    /**
     * Requests server to run DONE command
     * DONE command is to close connections between client and server
     * Then closes client connection
    **/
    private void DONE(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 
        clientSocket.close();
        connected = false;
    }
    
    /**
     * Requests server to run LIST command
     * List is to list files/folders in current directory
     * Then lists files/folders
    **/
    private void LIST(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage();
        System.out.println("FROM SERVER: " + serverResponse);
        
    }
    
    /**
     * Requests server to run KILL command
     * KILL command is to delete specified file
    **/
    private void KILL(String command) throws IOException {
        
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }

    /**
     * Requests server to run CDIR command
     * CDIR command is to change the current working directory
     * Waits for response and performs needed logic
    **/
    private void CDIR(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse);
        
        // Directory ok, need to authenticate user
        if(serverResponse.charAt(0) == '+'){  
            
            // Waiting for success change in dir response
            while(!(serverResponse.charAt(0) == '!')){
                
                String userCommand = inFromUser.readLine();
                String[] userCommands = userCommand.split(" ");

                if(null != userCommands[0])switch (userCommands[0]) {
                    
                    case "ACCT":
                        sendMessage(userCommand);
                        serverResponse = receiveMessage(); 
                        System.out.println("FROM SERVER: " + serverResponse); 
                        break;
                        
                    case "PASS":
                        sendMessage(userCommand);
                        serverResponse = receiveMessage(); 
                        System.out.println("FROM SERVER: " + serverResponse); 
                        break;
                        
                    default:
                        System.out.println("-send account/password");
                        break;
                        
                }  
                
            }  
            
        }
        
        
    }

    /**
     * Requests server to run NAME command
     * NAME command is to rename specified file
     * Waits for response and performs needed logic
    **/
    private void NAME(String userCommand) throws IOException {

        // Send message to server and wait for response
        sendMessage(userCommand);
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse);
        
        if("+File exists".equals(serverResponse)){
            
            String userCommand2;
            String[] userCommands;
            
            // Wait for user to input TOBE commad followed by filename
            while(true){
                
                System.out.println("Send command TOBE followed by the new file name");
                userCommand2 = inFromUser.readLine();
                userCommands = userCommand2.split(" ");
                if("TOBE".equals(userCommands[0]) && userCommands.length == 2){
                    break;
                }
                
            }
            
            // Send TOBE command with file name to server
            sendMessage(userCommand2);
            serverResponse = receiveMessage(); 
            System.out.println("FROM SERVER: " + serverResponse);
            
        }

    }

    /**
     * Requests server to run RETR command
     * RETR command is to request Server to send specified file to client
     * Waits for response and performs needed logic
    **/
    private void RETR(String command) throws IOException {
        
        // Send command
        String userCommand2 = "";
        String [] userCommands;
        sendMessage(command); 
        
        // Wait for response
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse);    
        long requestedFileSize;
        
        if(serverResponse.charAt(0) != '-'){
            
            requestedFileSize = Long.valueOf(serverResponse);
            
            // Wait for user to enter SEND or STOP command
            while(true){
                
                System.out.println("Type: SEND or STOP");
                
                try {
                    userCommand2 = inFromUser.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                userCommands = userCommand2.split(" ");
                if( ("SEND".equals(userCommands[0]) || "STOP".equals(userCommands[0]))){
                    break;
                }
                
            }
                   
            if( "STOP".equals(userCommands[0])){ // cancel
                sendMessage(userCommand2); 
                serverResponse = receiveMessage(); 
                System.out.println("FROM SERVER: " + serverResponse);
            } 
            else if("SEND".equals(userCommands[0])){ // read file
                sendMessage(userCommand2); 
                userCommands = command.split(" ");
                readFileBytes(userCommands[1], false, requestedFileSize);
            }
            
        } 
    
    }
    
    /**
     * Requests server to run STOR command
     * STOR command is to send a file from client to server
     * Waits for response and performs needed logic
    **/
    private void STOR(String command) throws IOException {
        
        String[] clientCommands = command.split(" ");
        
        // Command must contain 3 argumetns: Command, Type, Filename
        if(clientCommands.length == 3){
            
            File fileToSend = new File(storageRoot + clientCommands[2]);

            // Only send command if file exists otherwise
            if(fileToSend.exists() && fileToSend.isFile()){ 

                // Send command to client
                sendMessage(command); 
                
                // Wait for response
                String serverResponse = receiveMessage(); 
                System.out.println("FROM SERVER: " + serverResponse); 

                // + response means server has ability to store file
                if(serverResponse.charAt(0) == '+'){

                    // Send file size to server
                    long length = fileToSend.length();
                    sendMessage(Long.toString(length));
                    
                    // Wait for response
                    serverResponse = receiveMessage();
                    System.out.println("FROM SERVER: " + serverResponse); 

                    if(serverResponse.charAt(0) == '+'){ // Server has enough space to save file

                        // SEND
                        sendFileBytes(fileToSend);

                        serverResponse = receiveMessage();
                        System.out.println("FROM SERVER: " + serverResponse); 

                    }

                }


            } else {

                System.out.println("-File specified doesn't exits");

            }
            
        } else {
            System.out.println("-COMMAND EXPECTED 3 ARGUMENTS, GOT " + Integer.toString(clientCommands.length));
        }

    }
    
    /**
     * Sends file to server
     * Supports three file transfer modes controlled by TYPE function
     * ASCII, BINARY, CONTINUOUS
     * reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
     * reference: https://stackoverflow.com/questions/38732970/java-sending-and-receiving-file-over-sockets
    **/
    private void sendFileBytes(File requestedFile) throws IOException{
        
        if("A".equals(TYPE_TEXT)){ // ASCII Mode
            
            byte[] bytes = new byte[(int) requestedFile.length()];
            
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(requestedFile))) {
                
                int count;
                outToServer.flush();
                
                // Read and write byte
                while ((count = bis.read(bytes)) >= 0) {
                    outToServer.write(bytes, 0, count);
                }
                
                outToServer.flush();
                
            }
            
        }
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ // BINARY, CONTINUOUS mode
            
            DataOutputStream fileDataToClient;
            
            try (FileInputStream fileStream = new FileInputStream(requestedFile)) {
                
                fileDataToClient = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                int count;
                
                // Read and write whole file
                while ((count = fileStream.read()) >= 0) {
                    fileDataToClient.write(count);
                }
                
                fileDataToClient.flush();
                
            }
            
        }

    }
    
    /**
     * Receives files from server
     * Supports three file transfer modes controlled by TYPE function
     * ASCII, BINARY, CONTINUOUS
     * reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
     * reference: https://stackoverflow.com/questions/38732970/java-sending-and-receiving-file-over-sockets
    **/    
    private void readFileBytes(String outputFileName, boolean append, long requestedFileSize) throws IOException{
        
        File newFile = new File(storageRoot + outputFileName);
        Date d1;
        Date d2;
        long msPassed;
        long waitTime = (requestedFileSize / BYTE_PER_MS) + BYTE_PER_MS; // Timeout 
        
        if("A".equals(TYPE_TEXT)){ // ASCII mode
            
            try (FileOutputStream fos = new FileOutputStream(newFile, append); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                d1 = new Date();
                
                // For expected size of file
                for(int j = 0; j < requestedFileSize; j++){
                    
                    // Get data 
                    bos.write(inFromServer.read());
                    
                    if(j%100 == 0){ // update status every 100 bytes
                        System.out.printf("File Transfer: %.2f %% \n", ( (float) ( ((float)j)/((float)requestedFileSize)) * 100) );
                    }
                    
                    // Get time
                    d2 = new Date();
                    msPassed = (d2.getTime() - d1.getTime());
                    if(j%100 == 0){ // update status every 100 bytes
                        System.out.println("Time left until timeout:  " + (waitTime - msPassed) + " ms" );
                    }
                    if(msPassed >= waitTime){
                        break;
                    }
                    
                }   
                
                System.out.println("File received");
                bos.flush();
                bos.close();
                fos.close();
                
            }
            
        }
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ // BINARY, CONTINUOUS mode
            
            try (FileOutputStream fos = new FileOutputStream(newFile, append)) { // Binary, Continuous
                
                byte[] bytes = new byte[(int) requestedFileSize];
                DataInputStream fileDataFromClient = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                int bytesRead = 0;
                int count;
                d1 = new Date();

                // Keep reading until read amount of byte expected
                while (true) {
                    
                    // Get data
                    count = fileDataFromClient.read(bytes);
                    bytesRead += count;
                    fos.write(bytes, 0, count);
                    System.out.printf("File Transfer: %.2f %% \n", ( (float) ( ((float)bytesRead)/((float)requestedFileSize)) * 100) );
                    
                    if (bytesRead >= ((int) requestedFileSize)){ // got expected amount of data
                        break;
                    }

                    // Get time
                    d2 = new Date();
                    msPassed = (d2.getTime() - d1.getTime());
                    System.out.println("Time left until timeout:  " + (waitTime - msPassed) + " ms" );
                    
                    if(msPassed >= waitTime){ // timeout
                        break;
                    }

                }
                
                fos.flush();
                
            }
            
        }
        
    }
    
}
