#!/bin/bash
if [ $# -lt 4 ];then
    echo "Usage: $0  <service URL> <partner ID> <partner admin secret> </path/to/file> [optional entryId to update]"
    exit 1
fi

SERVICE_URL=$1
PARTNER_ID=$2
ADMIN_SECRET=$3
PATH_TO_FILE=$4
ENTRY_ID_TO_UPDATE=''
if [ -n "$5" ];then
    ENTRY_ID_TO_UPDATE=$5
fi

# compile
javac -cp ./:KalturaApiClient-15.14.0-SNAPSHOT.jar chunkedupload/ParallelUpload.java
javac -cp ./:KalturaApiClient-15.14.0-SNAPSHOT.jar UploadTest.java

#run
java -cp .:KalturaApiClient-15.14.0-SNAPSHOT.jar:commons-codec-1.12.jar:gson-2.8.5.jar:json-20180813.jar:kotlin-stdlib-1.3.21.jar:log4j-api-2.11.1.jar:log4j-core-2.11.1.jar:okhttp-3.14.1.jar:okio-2.2.2.jar UploadTest $SERVICE_URL $PARTNER_ID $ADMIN_SECRET $PATH_TO_FILE $ENTRY_ID_TO_UPDATE


