/**
 * Jaedyn Damms - 955581057 - JDAM534
 * COMPSYS 725 - ASSIGNMENT 1
 * SFTP - CLIENT / SERVER APPLICATION
 **/

package Server;

import java.net.*; 

class TCPServer { 
    
    static String databasePath = "database.txt";
    
    public static void main(String argv[]) throws Exception 
    
    { 
        
        ServerSocket welcomeSocket = new ServerSocket(6789); // local host 
        
	while(true){ // Accept multiple clients
            
            Socket connectionSocket = welcomeSocket.accept(); 
            ClientConnection clientConnection = new ClientConnection(connectionSocket);
            clientConnection.start(); // start client thread
            
        }
        
    } 
    
} 

