# Client Server Application using SFTP and Java

This project holds both server and client applications which follow a Simple File Transfer Protocol RFC 913 (https://tools.ietf.org/html/rfc913)

* Supports multiple clients
* Root directory for file storage located in /storage for their respective application (server or client)
* Commands/Messages are null terminated

## Client - Server Features

| COMMAND | Client | Server |
| :---         |     :---:      |          ---: |
| [USER](#user)   | Y     | Y    |
| [ACCT](#acct)   | Y     | Y    |
| [PASS](#pass)   | Y     | Y    |
| [TYPE](#type)   | Y     | Y    |
| [LIST](#list)   | Y     | Y    |
| [CDIR](#cdir)   | Y     | Y    |
| [KILL](#kill)   | Y     | Y    |
| [NAME](#name)   | Y     | Y    |
| [DONE](#done)   | Y     | Y    |
| [RETR](#retr)   | Y     | Y    |
| [STOR](#stor)   | Y     | Y    |

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
* The database has five preloaded accounts 

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
The run the following commands first start up the server, then start up the client.
ACCT, PASS cannot be used until the USER is identified. ALL other commands can't be used until the client receives it's first '!' response.
**NOTE:** *Files and folders DO NOT support spaces. Please use underscores or hyphons for word seperation.*
**FOR MARKER:** I have storage.zip file in the Client and Server folders, this is a backup of the current state of the Storage folders for the Client and Server. At the end of testing each command delete the storage folder and unpack the zip folder, this is to ensure that the console outputs match the test case outputs.

### USER
**USER <user-id>**
Authenticates USER
#### Working Case
Type the following commands in the client command:
1) USER USER

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
```
#### Failure Case
Type the following commands in the client command:
1) USER NOTAUSER

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER NOTAUSER
FROM SERVER: -Invalid user-id, try again
```

### ACCT
**ACCT <account>**
Authenticates ACCT
#### Working Case
Type the following commands in the client command:
1) USER USER
2) ACCT ACCT

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
ACCT ACCT
FROM SERVER: +Account valid, send password
```
#### Failure Case
Type the following commands in the client command:
1) USER USER
2) ACCT NOTAACCT

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
ACCT NOTAACCT
FROM SERVER: -Invalid account, try again
```

### PASS
**PASS <password>**
Authenticates PASS
#### Working Case
Type the following commands in the client command:
1) USER USER
2) PASS PASS

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
PASS PASS
FROM SERVER: +Send account
```
#### Failure Case
Type the following commands in the client command:
1) USER USER
2) PASS NOTAPASS

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
PASS NOTAPASS
FROM SERVER: -Wrong password, try again
```

### Using USER, ACCT, PASS Together

#### Working Cases
USER requiring ACCT/PASS - Type the following commands in the client command:
1) USER USER
2) ACCT ACCT
3) PASS PASS

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
ACCT ACCT
FROM SERVER: +Account valid, send password
PASS PASS
FROM SERVER: ! Logged in
```

**User not requiring ACCT/PASS** - Type the following commands in the client command:
1) USER admin

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
```

**User not requiring ACCT** - Type the following commands in the client command:
1) USER admin2
2) PASS PASS

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin2
FROM SERVER: +User-id valid, send password
PASS PASS
FROM SERVER: ! Logged in
```

**User not requiring PASS** - Type the following commands in the client command:
1) USER admin3
2) ACCT ACCT

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin3
FROM SERVER: +User-id valid, send account
ACCT ACCT
FROM SERVER: ! Account valid, logged-in
```

### TYPE
**TYPE { A | B | C }**
Changes file transfer mode
*A - ASCII*
*B - BINARY*
*C - CONTINUOUS*
#### Working Cases
**Changing to Type A** - Type the following commands in the client command:
1) USER admin
2) TYPE A

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE A
FROM SERVER: +Using A mode
```

**Changing to Type B** - Type the following commands in the client command:
1) USER admin
2) TYPE B

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE B
FROM SERVER: +Using B mode
```

**Changing to Type C** - Type the following commands in the client command:
1) USER admin
2) TYPE C

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE C
FROM SERVER: +Using C mode
```
#### Failure Case
Type the following commands in the client command:
1) USER admin
2) TYPE Z

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE Z
FROM SERVER: -Type not valid
```

### LIST
**LIST { F | V } <directory-path>**
List files and folders in the specified directory path.
If no directory path specified then lists files and folders in current working directory
#### Working Cases
**List F** - Type the following commands in the client command:
1) USER admin
2) LIST F

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: tryRenameFileToMe.txt
```

**List V** - Type the following commands in the client command:
1) USER admin
2) LIST V testfolder

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
LIST V testfolder
FROM SERVER: +./storage/testfolder
Folder: anotherone
File Name: anotherfile.txt | File Owner: JAEDYN\Jaedyn | Date Created: 8/14/2018 | Last Modified: 08/14/2018 01:16:43 | File Size: 0 B
File Name: google.png | File Owner: JAEDYN\Jaedyn | Date Created: 8/18/2018 | Last Modified: 08/18/2018 01:47:14 | File Size: 4 KB
File Name: yourInTestFolder.txt | File Owner: JAEDYN\Jaedyn | Date Created: 8/22/2018 | Last Modified: 08/22/2018 03:25:03 | File Size: 26 B
```
#### Failure Cases
**List Type Not Supported** - Type the following commands in the client command:
1) USER admin
2) LIST X

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
LIST X
FROM SERVER: -Incompatible type requested, supported types are { F | V }
```

**List Directory Doesn't Exist** - Type the following commands in the client command:
1) USER admin
2) LIST F NOTADIRECTORY

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
LIST F NOTADIRECTORY
FROM SERVER: -Directory doesn't exist
```

### CDIR
**CDIR <new-directory>**
Changes current working directory to directory specified.
NOTE: To change to root then *new-directory = "/"*.
#### Working Cases
**Changing Directory With User Needing ACCT/PASS** - Type the following commands in the client command:
1) USER USER
2) ACCT ACCT
3) PASS PASS
4) CDIR testfolder
5) ACCT ACCT
6) PASS PASS

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER USER
FROM SERVER: +User-id valid, send account and password
ACCT ACCT
FROM SERVER: +Account valid, send password
PASS PASS
FROM SERVER: ! Logged in
CDIR testfolder
FROM SERVER: +directory ok, send account/password
ACCT ACCT
FROM SERVER: +account ok, send password
PASS PASS
FROM SERVER: !Changed working dir to./storage/testfolder/
```

**Changing Directory With User Not Needing ACCT/PASS** - Type the following commands in the client command:
1) USER admin
2) CDIR testfolder

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
CDIR testfolder
FROM SERVER: !Changed working dir to./storage/testfolder/
```
#### Failure Case
**Changing To Invalid Directory** - Type the following commands in the client command:
1) USER admin
2) CDIR NOTADIRECTORY

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
CDIR NOTADIRECTORY
FROM SERVER: -Can't connect to directory because: ./storage/NOTAFOLDER/ is not a directory.
```

### KILL
**KILL <file-spec>**
Deletes specified file, searches from current working directory
#### Working Case
**Deleting File** - Type the following commands in the client command:
1) USER admin
2) KILL fileToDelete.txt

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
KILL fileToDelete.txt
FROM SERVER: +fileToDelete.txt deleted
```
#### Failure Case
**Trying To Delete A File That Doesn't Exist** - Type the following commands in the client command:
1) USER admin
2) KILL nonExistingFile.txt

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
KILL nonExistingFile.txt
FROM SERVER: -Not deleted because file doesn't exist.
```

### NAME
Description
#### Working Case
**Trying To Rename File To A File That Already Exists** - Type the following commands in the client command:
1) USER admin
2) LIST F

You should then see the following, note there is a file called fileToRename.txt
```reStructuredText
LIST F
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: tryRenameFileToMe.txt
```
Then enter the following commands:
3) NAME fileToRename.txt
4) TOBE renamedFile.txt
5) LIST F

You should see the following:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
NAME fileToRename.txt
FROM SERVER: +File exists
Send command TOBE followed by the new file name
TOBE renamedFile.txt
FROM SERVER: +fileToRename.txt renamed to renamedFile.txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: renamedFile.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: tryRenameFileToMe.txt
```
Notice that there is no longer a file called fileToRename.txt but there is now a "new" file called renamedFile.

#### Failure Case
**Trying To Rename File To A File That Already Exists** - Type the following commands in the client command:
1) USER admin
2) NAME fileToRename.txt
3) TOBE tryRenameFileToMe.txt

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
NAME fileToRename.txt
FROM SERVER: +File exists
Send command TOBE followed by the new file name
TOBE tryRenameFileToMe.txt
FROM SERVER: -File wasn't renamed because file with specified name already exists.
```

### DONE
**DONE**
Tells remote system that you're done and closes connections.
#### Working Case
ype the following commands in the client command:
1) USER admin
2) DONE

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
DONE
FROM SERVER: +Connection closed
Client will now close
```


### RETR
**RETR <file-spec>**
Retrieve specified file from server
**NOTE:** *Cannot send images using Ascii mode*
#### Working Cases
**Receiving File Using Ascii File Transfer Mode** - Type the following commands in the client command:
1) USER admin
2) TYPE A
3) RETR RETRtest.txt
4) SEND

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE A
FROM SERVER: +Using A mode
RETR RETRtest.txt
FROM SERVER: 11
Type: SEND or STOP
SEND
File Transfer: 100.00 % 
```
Now check ./Client/storage and you will see a new file called RETRtest.txt

**Receiving File Using Binary File Transfer Mode** - Type the following commands in the client command:
1) USER admin
2) TYPE B
3) RETR RETRtest2.txt
4) SEND

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE B
FROM SERVER: +Using B mode
RETR RETRtest2.txt
FROM SERVER: 11
Type: SEND or STOP
SEND
File Transfer: 100.00 % 
```
Now check ./Client/storage and you will see a new file called RETRtest2.txt

**Receiving File Using Continuous File Transfer Mode** - Type the following commands in the client command:
1) USER admin
2) TYPE C
3) RETR RETRtest3.txt
4) SEND

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
TYPE C
FROM SERVER: +Using C mode
RETR RETRtest3.txt
FROM SERVER: 11
Type: SEND or STOP
SEND
File Transfer: 100.00 % 
```
Now check ./Client/storage and you will see a new file called RETRtest3.txt

#### Failure Case
**Trying To Receive File That Doesn't Exist** - Type the following commands in the client command:
1) USER admin
3) RETR nonExistingFile.txt

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
RETR nonExistingFile.txt
FROM SERVER: -File doesn't exist
```

### STOR
**STOR { NEW | OLD | APP } <file-spec>**
Stores specified file in server's storage.
* New - Generates new file if file already exists.
* OLD - Overwrites file if file already exists
* APP - Appends to file if file already exists

**NOTE:** *STOR only supports generation of new files with an extension. Meaning generation of a file "example" is not supported, but a file "example.xx" is supported.*
#### Working Case
**STOR using NEW** - Type the following commands in the client command:
1) USER admin
2) LIST F

You should see the following:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: tryRenameFileToMe.txt
```
Now enter the following:
3) STOR NEW STORtestNEW.txt
4) LIST F

You should see the following:
```reStructuredText
STOR NEW STORtestNEW.txt
FROM SERVER: +File does not exist, will create new one
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved STORtestNEW.txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: STORtestNEW.txt
File: tryRenameFileToMe.txt
```
Notice new file called STORtestNEW.txt
Now enter the following:
5) STOR NEW STORtestNEW.txt
6) LIST F

You should see the following:
```reStructuredText
STOR NEW STORtestNEW.txt
FROM SERVER: +File exists, will create new generation of file
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved STORtestNEW(2).txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: STORtestNEW(2).txt
File: STORtestNEW.txt
File: tryRenameFileToMe.txt
```
Notice another new file called STORtestNEW(2).txt - this is the new generated file

**STOR using OLD** - Type the following commands in the client command:
1) USER admin
2) LIST F

You should see the following:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: tryRenameFileToMe.txt
```
Now enter the following:
3) STOR OLD STORtestOLD.txt
4) LIST F

You should see the following:
```reStructuredText
STOR OLD STORtestOLD.txt
FROM SERVER: +Will create new file
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved STORtestOLD.txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: STORtestOLD.txt
File: tryRenameFileToMe.txt
```
Notice new file called STORtestOLD.txt
You can now open the file located at Client/storage/STORtestOLD.txt and change the content inside.
Now enter the following:
5) STOR OLD STORtestOLD.txt
6) LIST F

You should see the following:
```reStructuredText
STOR OLD STORtestOLD.txt
FROM SERVER: +Will write over old file
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved STORtestOLD.txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: STORtestOLD.txt
File: tryRenameFileToMe.txt
```
Notice the file has been overwritten.
Confirm this by opening the file at Server/storage/STORtestNEW.txt and see that it's what you changed.

**STOR using APP** - Type the following commands in the client command:
1) USER admin
2) LIST F

You should see the following:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: tryRenameFileToMe.txt
```
Now enter the following:
3) STOR APP STORtestAPP.txt
4) LIST F

You should see the following:
```reStructuredText
STOR APP STORtestAPP.txt
FROM SERVER: +Will create file
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved STORtestAPP.txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: STORtestAPP.txt
File: tryRenameFileToMe.txt
```
Notice new file called STORtestAPP.txt
You can now open the file located at Client/storage/STORtestAPP.txt and check the content.
Now enter the following:
5) STOR APP STORtestOLD.txt
6) LIST F

You should see the following:
```reStructuredText
STOR APP STORtestAPP.txt
FROM SERVER: +Will append to file
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved STORtestAPP.txt
LIST F
FROM SERVER: +./storage/
Folder: anotherFolder
Folder: testfolder
File: download.png
File: example.txt
File: fileToDelete.txt
File: fileToRename.txt
File: RETRtest.txt
File: RETRtest2.txt
File: RETRtest3.txt
File: STORtestAPP.txt
File: tryRenameFileToMe.txt
```
Notice the file has been appended to.
Confirm this by opening the file at Server/storage/STORtestAPP.txt and see that more content has been added to the file.

#### Failure Case
**Trying To Store File That Doesn't Exist** - Type the following commands in the client command:
1) USER admin
3) RETR nonExistingFile.txt

You should see the following response:
```reStructuredText
FROM SERVER: +MIT-localhost SFTP Service
Successfully established connection to server
USER admin
FROM SERVER: !<user-id> logged in
STOR NEW nonExistingFile.txt
-File specified doesn't exits
```

