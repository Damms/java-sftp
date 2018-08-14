/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
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
    boolean connected = false;
    
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


        inFromServer = 
            new BufferedReader(new
                InputStreamReader(clientSocket.getInputStream())); 
        
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

        sendMessage(command); 
        String serverResponse = receiveMessage(); 
        System.out.println("FROM SERVER: " + serverResponse); 

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
    
}
