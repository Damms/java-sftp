# Client Server Application using SFTP and Java

This project holds both server and client applications which follow a Simple File Transfer Protocol RFC 913 (https://tools.ietf.org/html/rfc913)

* Supports multiple clients
* Root directory for file storage located in /storage for their respective application (server or client)
* Commands/Messages are null terminated

## Client - Server Features

| COMMAND | Client | Server |
| :---         |     :---:      |          ---: |
| USER   | Y     | Y    |
| ACCT   | Y     | Y    |
| PASS   | Y     | Y    |
| TYPE   | Y     | Y    |
| LIST   | Y     | Y    |
| CDIR   | Y     | Y    |
| KILL   | Y     | Y    |
| NAME   | Y     | Y    |
| DONE   | Y     | Y    |
| RETR   | Y     | Y    |
| STOR   | Y     | Y    |

## Running the project
1) Open your Java IDE of choice
2) Import the existing project
3) Run Server: run stfp-server (selecting TCPServer.java as main)
4) Run Client: run stfp-client (selecting TCPClient.java as main)
5) Enter supported COMMANDS in the stfp-client console
6) Enter DONE when finished
Note: This project does support multiple clients

## Authentication
* The list of users, accounts and passwords are located in Server/database.txt
* USER ACCT PASS are each seperated by a single space
* If you wish a USER to not require an ACCT or PASS then replace the relevant field with a hyphon ("-")
The database has five preloaded accounts 

```reStructuredText
USER: USER
ACCT: ACCT
PASS: PASS
```

```reStructuredText
USER: username
ACCT: accountname
PASS: password
```

```reStructuredText
USER: admin
ACCT: -
PASS: -
```

```reStructuredText
USER: admin2
ACCT: -
PASS: PASS
```

```reStructuredText
USER: admin3
ACCT: ACCT
PASS: -
```


## Commands
##### Server Response Meanings:
* '+' - Success
* '-' - Error
* '!' - Logged in

The following commands are supported, it's important to know that each argument for a command is seperated by a single space.

### USER
Description
##### Working Case
--
##### Failure Case
--

### ACCT
Description
##### Working Case
--
##### Failure Case
--

### PASS
Description
##### Working Case
--
##### Failure Case
--

### TYPE
Description
##### Working Case
--
##### Failure Case
--

### LIST
Description
##### Working Case
--
##### Failure Case
--

### CDIR
Description
##### Working Case
--
##### Failure Case
--

### KILL
Description
##### Working Case
--
##### Failure Case
--

### NAME
Description
##### Working Case
--
##### Failure Case
--

### DONE
Description
##### Working Case
--
##### Failure Case
--

### RETR
Description
##### Working Case
--
##### Failure Case
--

### STOR
Description
Note: STOR only supports files with an extension. Meaning a file "example" is not supported, but a file "example.xx" is supported.
##### Working Case
--
##### Failure Case
--

## Example Use Cases

### Example 1:
--

### Example 2:
--

### Example 3:
--
