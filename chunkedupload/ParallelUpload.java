// ===================================================================================================
//                           _  __     _ _
//                          | |/ /__ _| | |_ _  _ _ _ __ _
//                          | ' </ _` | |  _| || | '_/ _` |
//                          |_|\_\__,_|_|\__|\_,_|_| \__,_|
//
// This file is part of the Kaltura Collaborative Media Suite which allows users
// to do with audio, video, and animation what Wiki platfroms allow them to do with
// text.
//
// Copyright (C) 2006-2016  Kaltura Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// @ignore
// ===================================================================================================
package chunkedupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.lang.Math;

import com.kaltura.client.IKalturaLogger;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.KalturaLogger;
import com.kaltura.client.services.KalturaUploadTokenService;
import com.kaltura.client.types.*;

public class ParallelUpload {

	/**
	 * The ChunkedStream class wraps a FileInputStream and limit the size avaialble for reading
	 * It's required for enabling the http multipart to send partial files using the FilePartSource interface
	 */
	private class ChunkedStream extends FileInputStream
	{
		int size;
		int bytesLeft;

		public int getSize()
		{
			return size;
		}

		public ChunkedStream(String name) throws FileNotFoundException
		{
			super(name);
		}

		public void resetChunk(long seek, int _size) throws IOException
		{
			bytesLeft = size = _size;
			this.getChannel().position(seek);
		}

		public int read(byte[] b) throws IOException
		{
			if (bytesLeft == 0)
				return -1;

			int readSize = b.length < size ? b.length : size;
			bytesLeft -= readSize;
			return super.read(b, 0, readSize);
		}
		
		public void close()
		{
		}

		public void forceClose() throws IOException
		{
			super.close();
		}
	}

	/**
	 * The UploadTask class implements a thread which uploads chunks.
	 * The code loops and retreives the next chunk to be uploaded from the parent class until there are no more chunks to upload
	 */
	private class UploadTask implements Runnable {
		public ParallelUpload pu;

		public void run()
		{
			try {
				String threadName = Thread.currentThread().getName();

				ChunkedStream stream = new ChunkedStream(pu.fileName);

				while(true)
				{
					// get next chunk for upload
					int i = pu.getNextChunk();
					if (i == -1)
					{
						stream.forceClose();
						return;
					}

					// calculate seek and size for chunk
					long seekPos = (long)i * pu.chunkSize;
					int size = (int)Math.min(pu.chunkSize, pu.fileSize - seekPos);

					int chunkRetries = 0;
					boolean success = false;
					do
					{
			        		log.info(String.format("%s: chunk %d pos %d size %d", threadName, i, seekPos, size));

						stream.resetChunk(seekPos, size);
						success = pu.addChunk(stream, true, (seekPos + size) == pu.fileSize, seekPos);
						if (success)
						{
							pu.addUploadSize(size);
							break;
						}

						chunkRetries++;
					} while(pu.countRetries(chunkRetries));
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private IKalturaLogger log = KalturaLogger.getLogger(getClass());

	private String fileName;
	private long fileSize;
	private int nextChunk = 0;
	private int chunkCount = 0;
	private long uploadSize = 0;
	private KalturaClient client;
	private KalturaUploadToken upToken;
	private int retryCount = 0;

	public int chunkSize = 10*1024*1024;
	public int threadCount = 5;
	public int maxChunkRetries = 3;
	public int maxRetries = 5;

	private synchronized boolean countRetries(int chunkRetries)
	{
		if (chunkRetries < maxChunkRetries)
			retryCount++;
		else
			retryCount = maxRetries + 1;

		return retryCount > maxRetries;
	}

	private synchronized int getNextChunk()
	{
		if (retryCount < maxRetries && nextChunk < chunkCount)
			return nextChunk++;

		return -1; 
	}

	private synchronized void addUploadSize(long size)
	{
		uploadSize += size;
	}

	public ParallelUpload(KalturaClient _client, String _fileName)
	{
		client = _client;
		fileName = _fileName;
	}

	public String upload() throws InterruptedException, IOException, KalturaApiException
	{
		File fileData = new File(fileName);
		fileSize = fileData.length();

		ChunkedStream stream;
		stream = new ChunkedStream(fileName);

		stream.resetChunk(0, 1);

		upToken = client.getUploadTokenService().add();

		chunkCount = (int)((fileSize + chunkSize - 1) / chunkSize);

		log.info("Uploading token " + upToken.id + " file size " + fileSize + " in " + chunkCount + " chunks");

		// add the first byte and then parallelize the actual upload
		addChunk(stream, false, false, 0);
		stream.forceClose();

		List<Thread> threads = new ArrayList<Thread>();
		
		for(int i=0; i < threadCount; i++) {
			UploadTask uploadTask = new UploadTask();
			uploadTask.pu = this;

			Thread t = new Thread(uploadTask);
			threads.add(t);
			t.start();
		}

		for(Thread t : threads)
			t.join();

		log.info("Uploading token " + upToken.id + " file size " + fileSize + " uploaded " + uploadSize);

		return uploadSize == fileSize ? upToken.id : null;
 	}

    /**
     * @param ChunkedStream stream
     * @param boolean resume
     * @param boolean finalChunk
     * @param long resumeAt
     *
     * @return
     */
    private boolean addChunk(ChunkedStream stream, boolean resume, boolean finalChunk, long resumeAt) throws IOException {
        try {
            client.getUploadTokenService().upload(upToken.id, stream, "a.dat", stream.getSize(), resume, finalChunk, resumeAt);
		return true;
        } catch (KalturaApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }


}
 
