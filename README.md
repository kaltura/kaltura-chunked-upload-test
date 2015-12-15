# Kaltura chunked upload test
Simple Java code that splits a big video file into smaller chunks and uploads it to the Kaltura server using the uploadToken API.

This repo is meant as a proof of concept on how to utilize the uploadToken.upload() API to upload large files on the Kaltura server.
Seeing how Flash has 2G limitation where it comes to uploading files and the uploaded file size allowed can also be limited in the PHP settings, big files should sometimes be split into smaller ones and uploaded in chunks.

## Contents
UploadTest.java - a simple test class which can be used to test chunked upload from the command line
chunkedupload/ChunkedUpload.java - a help class which uses FileInputStream to read() a certain amount of bytes from the original file [thus spliting it] and upload it to the Kaltura server.

## Compiling the sample code
The code relies on the Kaltura Java client. When using the latest version, that can be obtained from:
http://search.maven.org/#search|ga|1|kaltura
or from:
https://github.com/kaltura/KalturaGeneratedAPIClientsJava

When using older versions of the Kaltura server, one should run:
\# php /opt/kaltura/app/generator/generate.php java
from the Kaltura server and then take the resulting package from /opt/kaltura/web/content/clientlibs/java

$ javac -cp /path/to/Kaltura/client/classes:/path/to/supporting/classes chunkedupload/ChunkedUpload.java
$ javac -cp /path/to/Kaltura/client/classes:/path/to/supporting/classes UploadTest.java

## Testing from CLI:
$ java -cp /path/to/Kaltura/client/classes:/path/to/supporting/classes UploadTest <service URL> <partner ID> <partner admin secret> </path/to/large/vid/file>

## Sample output
```
Uploading a video file..." - (ChunkedUpload.java:118) 
[15 Dec 2015 14:12:15] INFO - "HASH:1316959668" - (ChunkedUpload.java:144) 
[15 Dec 2015 14:12:15] INFO - "Available bytes: 1070427397" - (ChunkedUpload.java:151) 
[15 Dec 2015 14:12:15] INFO - "Bytes read: 10240000" - (ChunkedUpload.java:159) 
[15 Dec 2015 14:12:15] INFO - "1 chunk[1] - uploaddFileSize: 1.024E7" - (ChunkedUpload.java:178) 
[15 Dec 2015 14:12:15] INFO - "uploaded 0%" - (ChunkedUpload.java:217) 
[15 Dec 2015 14:12:15] INFO - "Available bytes: 1060187397" - (ChunkedUpload.java:151) 
[15 Dec 2015 14:12:15] INFO - "Bytes read: 10240000" - (ChunkedUpload.java:159) 
[15 Dec 2015 14:12:16] INFO - "1 chunk[2] - uploaddFileSize: 2.048E7" - (ChunkedUpload.java:178) 
[15 Dec 2015 14:12:16] INFO - "uploaded 1%" - (ChunkedUpload.java:217) 
[15 Dec 2015 14:12:16] INFO - "Available bytes: 1049947397" - (ChunkedUpload.java:151) 
[15 Dec 2015 14:12:16] INFO - "Bytes read: 10240000" - (ChunkedUpload.java:159) 
...
Uploaded a new Video file to entry: 0_m0q60mfz" - (ChunkedUpload.java:228)
```
