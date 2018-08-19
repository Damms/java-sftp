/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.io.OutputStream;
import java.io.InputStream;
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
    long BYTE_PER_MS = 50;
    
    public void run() throws IOException {
        
        createConnections();
        String userCommand;
        
        String serverResponse = receiveMessage();
        System.out.println("Response from server: " + serverResponse);
        if(serverResponse.contains("+MIT")){
            connected = true;
            System.out.println("Successfully established connection to server");
        }
        
        while(connected){

            userCommand = inFromUser.readLine();
            //System.out.println("GOT USER INPUT: " + userCommand + "\n");
            
            String[] userCommands = userCommand.split(" ");

            if(null != userCommands[0])switch (userCommands[0]) {
                case "USER":
                    USER(userCommand);
                    break;
            //break;
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
                default:
                    System.out.println("-INVALID COMMAND");
                    break;
            }
            
        }
        
        System.out.println("Client will now close");
        
    }
    
    private void createConnections() throws IOException {
        
        
        try {
            clientSocket = new Socket("localhost", 6789);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        inFromUser = 
            new BufferedReader(new InputStreamReader(System.in)); 

        outToServer = 
            new DataOutputStream(clientSocket.getOutputStream()); 

        //inFromServerSocket = new InputStreamReader(clientSocket.getInputStream());
        
        inFromServer = 
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
        
        
    }
    
    private String receiveMessage() throws IOException {
        String sentence = "";
        int character = 0;

        while (true){
            
            character = inFromServer.read();  // Read one character

            if (character == 0) { // null
                break;
            }

            sentence = sentence.concat(Character.toString((char)character));
        }

        return sentence;
    }
    
    private void sendMessage(String message) throws IOException{
        outToServer.writeBytes(message + '\0'); 
    }
    
    private void USER(String command) throws IOException {
        
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    private void ACCT(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    private void PASS(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    private void TYPE(String command) throws IOException {

        String [] splitCommand = command.split(" ");
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 
        if('+' == serverResponse.charAt(0)){
            TYPE_TEXT = splitCommand[1];
        }

    }
    
    private void DONE(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 
        clientSocket.close();
        connected = false;
    }
    
    private void LIST(String command) throws IOException {

        sendMessage(command); 
        /*String serverResponse = inFromServer.readLine(); 
        while(!serverResponse.contains("\0")){
            System.out.println("FROM SERVER: " + serverResponse); 
            serverResponse = inFromServer.readLine(); 
        }*/
        String serverResponse = receiveMessage();
        System.out.println("FROM SERVER: " + serverResponse);
    }
    
    private void KILL(String command) throws IOException {
        
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }

    private void CDIR(String command) throws IOException {

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse);
        
        if("+directory ok, send account/password".equals(serverResponse)){     
            while(!(serverResponse.contains("!Changed working dir to"))){
                
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

    private void NAME(String userCommand) throws IOException {

        sendMessage(userCommand);
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse);
        
        if("+File exists".equals(serverResponse)){
            
            String userCommand2;
            String[] userCommands;
            
            while(true){
                
                System.out.println("-Send command TOBE followed by the new file name");
                userCommand2 = inFromUser.readLine();
                userCommands = userCommand2.split(" ");
                if("TOBE".equals(userCommands[0]) && userCommands.length == 2){
                    break;
                }
                
            }
            
            sendMessage(userCommand2);
            serverResponse = receiveMessage(); 
            System.out.println("FROM SERVER: " + serverResponse);
            
        }

    }

    private void RETR(String command) throws IOException {
        
        String userCommand2 = "";
        String [] userCommands;
        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse);    
        long requestedFileSize;
        
        if(serverResponse.charAt(0) != '-'){
            requestedFileSize = Long.valueOf(serverResponse);
            System.out.println("FILE SIZE: " + requestedFileSize);
            while(true){
                
                System.out.println("Type: SEND or STOP");
                
                
                try {
                    userCommand2 = inFromUser.readLine();
                    System.out.println("User input: " + userCommand2);
                } catch (IOException ex) {
                    Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                userCommands = userCommand2.split(" ");
                if( ("SEND".equals(userCommands[0]) || "STOP".equals(userCommands[0]))){
                    break;
                }
                
            }
            
        
            if( "STOP".equals(userCommands[0])){
                sendMessage(userCommand2); 
                serverResponse = receiveMessage(); 
                System.out.println("FROM SERVER: " + serverResponse);
            } 
            else if("SEND".equals(userCommands[0])){
                sendMessage(userCommand2); 
                userCommands = command.split(" ");
                readFileBytes(userCommands[1], false, requestedFileSize);
                System.out.println("Done receiving files");
            }
            
        } 
    
    }
    
    private boolean checkFileDetails(File file){
        return true;
    }
    
    // reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
    private void sendFileBytes(String fileName) throws IOException{
        
        File file = new File(storageRoot + fileName);
        // Get the size of the file
        long length = file.length();
        byte[] bytes = new byte[16 * 1024];
        
        try (InputStream in = new FileInputStream(file); OutputStream out = clientSocket.getOutputStream()) {
            
            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            
            out.flush();
            out.close();
            in.close();
            
        }
        
    }
    
    // reference: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets#comment88270571_9520911
    private void readFileBytes(String outputFileName, boolean overwrite, long requestedFileSize) throws IOException{
        
        File newFile = new File(storageRoot + outputFileName);
        Date d1;
        Date d2;
        long msPassed; // ms
        long waitTime = (requestedFileSize / BYTE_PER_MS) + BYTE_PER_MS;
        
        if("A".equals(TYPE_TEXT)){ // Ascii
            
            try (FileOutputStream fos = new FileOutputStream(newFile, overwrite); BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                d1 = new Date();
                for(int j = 0; j < requestedFileSize; j++){
                    
                    bos.write(inFromServer.read());
                    
                    d2 = new Date();
                    msPassed = (d2.getTime() - d1.getTime());
                    System.out.println("Time passed: " + msPassed + " Timeout time: " + ((int) waitTime) );
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
        else if("B".equals(TYPE_TEXT) || "C".equals(TYPE_TEXT)){ 
            
            try (FileOutputStream fos = new FileOutputStream(newFile, false)) { // Binary, Continuous
                
                byte[] bytes = new byte[(int) requestedFileSize];
                DataInputStream fileDataFromClient = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

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
                        break;
                    }

                }
                fos.flush();
            }
            
        }
        
    }
    
}
