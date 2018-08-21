/**
 * Jaedyn Damms - 955581057 - JDAM534
 * COMPSYS 725 - ASSIGNMENT 1
 * SFTP - CLIENT / SERVER APPLICATION
 **/

package Client;

class TCPClient { 
    
    public static void main(String argv[]) throws Exception { 
         
        // Needed to create external class so it's not static
        ClientController clientControls = new ClientController(); // Creates a new client controller
        clientControls.run(); // runs the client controller

    }
    
} 
