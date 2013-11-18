package er.communication.foundation;

import java.util.List;

/**
 * Interface to implement when a class provides a list of media for a given context.
 * 
 * @author Philippe Rabier
 *
 */
public interface ERMediaProvider 
{
	public List<ERMedia> getMedias(String contextName);
}
