
import com.kaltura.client.enums.*;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import com.kaltura.client.services.*;
import com.kaltura.client.services.MediaService.AddContentMediaBuilder;
import com.kaltura.client.services.MediaService.AddMediaBuilder;
import com.kaltura.client.services.MediaService.GetMediaBuilder;
import com.kaltura.client.services.MediaService.UpdateContentMediaBuilder;
import com.kaltura.client.APIOkRequestsExecutor;
import com.kaltura.client.Client;
import com.kaltura.client.Configuration;
import java.util.ArrayList;
import java.util.List;
import com.kaltura.client.utils.response.OnCompletion;
import com.kaltura.client.utils.response.base.Response;

import chunkedupload.ParallelUpload;

public class UploadTest { 
	public static void main(String[] argv){
		try{
			if (argv.length < 4){
				System.out.println("Usage: <service URL> <partner ID> <partner admin secret> </path/to/file> [optional entryId to update]\n");
				System.exit (1);
			}
			try{
				Configuration config = new Configuration();
				config.setEndpoint(argv[0]);

				Client client = new Client(config);

				String secret = argv[2];
				int partnerId = Integer.parseInt(argv[1]);
				String ks = client.generateSessionV2(secret, null, SessionType.ADMIN, partnerId, 86400, "");

				client.setSessionId(ks);
				System.out.println(ks);

				MediaEntry newEntry = null;
				boolean update = false;
				String filePath = argv[3];
				String entryId = null;
				if(argv.length > 4 && argv[4] != ""){
					entryId=argv[4];
					GetMediaBuilder getBuilder = MediaService.get(entryId);
					Response<MediaEntry> response = (Response<MediaEntry>)APIOkRequestsExecutor.getExecutor().execute(getBuilder.build(client));
					if (response != null && response.results.getId() != null){
					    doUpload(client, filePath, entryId, true);
					}else{
					    System.out.println("No such entry" + entryId + "\n");
					}

				} else {
					newEntry = new MediaEntry();
					newEntry.setName("Chunked Upload Test");
					newEntry.setType(EntryType.MEDIA_CLIP);
					newEntry.setMediaType(MediaType.VIDEO);
					AddMediaBuilder requestBuilder = MediaService.add(newEntry)
					    .setCompletion(new OnCompletion<Response<MediaEntry>>() {
					    public void onComplete(Response<MediaEntry> result) {
						System.out.println("her nnow\n");
						String fentryId = result.results.getId();;
						if (fentryId !=null){
						    System.out.println("\nCreated a new entry: " + fentryId);
						    doUpload(client, filePath, fentryId, false);
						}else{
						    return;
						}

					    }
					});
					APIOkRequestsExecutor.getExecutor().queue(requestBuilder.build(client));
				}

				
			}
			catch (APIException e)
			{
				e.printStackTrace();
			}
		} catch (Exception exc) {
		    exc.printStackTrace();
		}
	}
	public static boolean doUpload(Client client, String filePath, String entryId,boolean update)
	{
	    ParallelUpload pu = new ParallelUpload(client, filePath);	
	    String tokenId = null;
	    try{
		tokenId = pu.upload();

	    }catch (Exception e){
		e.printStackTrace();
	    }

	    if (tokenId != null) {
		    UploadedFileTokenResource fileTokenResource = new UploadedFileTokenResource();
		    fileTokenResource.setToken(tokenId);
		    Response<MediaEntry> response =null;
		    if(update == true){
			    UpdateContentMediaBuilder requestBuilder = MediaService.updateContent(entryId, fileTokenResource);
			    response = (Response<MediaEntry>)APIOkRequestsExecutor.getExecutor().execute(requestBuilder.build(client));
		    } else {
			    AddContentMediaBuilder requestBuilder = MediaService.addContent(entryId, fileTokenResource);
			    response = (Response<MediaEntry>)APIOkRequestsExecutor.getExecutor().execute(requestBuilder.build(client));
		    }
		    if (response != null && response.error != null){
				    System.out.println(response.error);
				    return (false);
		    }

		    System.out.println("\nUploaded Video file to entry: " + entryId);
	    }
	    return (true);

	}
}
