 
 #compile
 javac -cp .:<path to kalturaClient.jar>:<path to httpclient-4.3.3.jar>:<path to commons-httpclient-3.1.jar>:./chunkedupload/ChunkedUpload.java UploadTest.java

 #run
 #java -cp .:<path to kalturaClient.jar>:<path to httpclient-4.3.3.jar>:<path to commons-httpclient-3.1.jar>:./chunkedupload/ChunkedUpload.java UploadTest <kaltura url> <partnerId> <secret> <video file path>

 #update
 java -cp .:<path to kalturaClient.jar>:<path to httpclient-4.3.3.jar>:<path to commons-httpclient-3.1.jar>:./chunkedupload/ChunkedUpload.java UploadTest <kaltura url> <partnerId> <secret> <video file path> <entry id to update>