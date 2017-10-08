
import com.kaltura.client.enums.*;
import com.kaltura.client.types.*;
import com.kaltura.client.services.*;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import chunkedupload.ParallelUpload;

public class UploadTest { 
	public static void main(String[] argv){
		try{
			if (argv.length < 4){
				System.out.println("Usage: <service URL> <partner ID> <partner admin secret> </path/to/file> [optional entryId to update]\n");
				System.exit (1);
			}
			try{
				KalturaConfiguration config = new KalturaConfiguration();
				config.setEndpoint(argv[0]);
				KalturaClient client = new KalturaClient(config);

				String secret = argv[2];
				String userId = null;
				int partnerId = Integer.parseInt(argv[1]);
				String privileges = null;
				KalturaSessionService sessionService = client.getSessionService();
				String ks = client.generateSessionV2(secret, null, KalturaSessionType.ADMIN, partnerId, 86400, "");

				client.setSessionId(ks);
				System.out.println(ks);

				KalturaMediaEntry newEntry = null;
				boolean update = false;
				if(argv.length > 4 && argv[4] != "") {
					newEntry = client.getMediaService().get(argv[4]);
					update = true;
				} else {
					KalturaMediaEntry entry = new KalturaMediaEntry();
					entry.name = "Chunked Upload Test";
					entry.type = KalturaEntryType.MEDIA_CLIP;
					entry.mediaType = KalturaMediaType.VIDEO;
					newEntry = client.getMediaService().add(entry);
				}

				System.out.println("\nCreate a new entry: " + newEntry.id);
			
				ParallelUpload pu = new ParallelUpload(client, argv[3]);	
				String tokenId = pu.upload();
				if (tokenId != null) {
					KalturaUploadedFileTokenResource fileTokenResource = new KalturaUploadedFileTokenResource();
					fileTokenResource.token = tokenId;
					if(update == true) {
						newEntry = client.getMediaService().updateContent(newEntry.id, fileTokenResource);
					} else {
						newEntry = client.getMediaService().addContent(newEntry.id, fileTokenResource);
					}
					System.out.println("\nUploaded a new Video file to entry: " + newEntry.id);
				}
			} catch (KalturaApiException e) {
			            e.printStackTrace();
			}
		} catch (Exception exc) {
        	exc.printStackTrace();
    	}
	}
}
