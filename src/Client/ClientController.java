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
    
    String[] sftpCommands = {"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"};
    BufferedReader inFromUser;
    BufferedReader inFromServer;
    DataOutputStream outToServer;
    
    public void run() throws IOException {
        boolean connected = false;
        createConnections();
        String userCommand;
        
        String serverResponse = inFromServer.readLine();
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
                default:
                    System.out.println("INVALID COMMAND");
                    break;
            }
        }
        
        System.out.println("Connection terminated.");
        
    }
    
    private void createConnections() throws IOException {
        
        Socket clientSocket = null; 
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
    
    private void USER(String command) throws IOException {
        
        outToServer.writeBytes(command + '\n'); 
        String serverResponse = inFromServer.readLine(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    private void ACCT(String command) throws IOException {

        outToServer.writeBytes(command + '\n'); 
        String serverResponse = inFromServer.readLine(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    private void PASS(String command) throws IOException {

        outToServer.writeBytes(command + '\n'); 
        String serverResponse = inFromServer.readLine(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
    private void TYPE(String command) throws IOException {

        outToServer.writeBytes(command + '\n'); 
        String serverResponse = inFromServer.readLine(); 
        System.out.println("FROM SERVER: " + serverResponse); 

    }
    
}
