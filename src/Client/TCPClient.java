package Client;

/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 
import java.util.logging.Level;
import java.util.logging.Logger;

class TCPClient { 
    
    String[] sftpCommands = {"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"};
    
    public static void main(String argv[]) throws Exception { 
         
        ClientController clientControls = new ClientController();
        clientControls.run();

    }
    
    
} 
