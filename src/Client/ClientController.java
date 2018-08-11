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
        
        createConnections();
        String userCommand;
        
        while(true){

            userCommand = inFromUser.readLine();
            //System.out.println("GOT USER INPUT: " + userCommand + "\n");
            
            String[] userCommands = userCommand.split(" ");

            if("USER".equals(userCommands[0])){
                USER(userCommand);
            }
            else if("ACCT".equals(userCommands[0])){
                ACCT(userCommand);
            }
            else if("PASS".equals(userCommands[0])){
                PASS(userCommand);
            }

            //break;
        }
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
    
}
