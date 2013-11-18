package er.communication.foundation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all medias which can be used to send messages.<p>
 * It automatically records all default medias.
 * 
 * @author Philippe Rabier
 * @see ERDefaultMedia
 */
public class ERMediaManager
{
	Map<String, ERMedia> medias = new HashMap<String, ERMedia>();
	
	public ERMediaManager()
	{
		for (ERDefaultMedia b : ERDefaultMedia.values())
			medias.put(b.getName(), b);
	}
	
	public synchronized void addMedia(ERMedia media)
	{
		medias.put(media.getName(), media);
	}
	
	public ERMedia getMedia(String value)
	{
		return medias.get(value);
	}
	
	public List<ERMedia> getMedias(List<String> values)
	{
		List<ERMedia> results = new  ArrayList<ERMedia>(values.size());
		for (String value : values) 
		{
			results.add(getMedia(value));
		}
		return results;
	}
}
