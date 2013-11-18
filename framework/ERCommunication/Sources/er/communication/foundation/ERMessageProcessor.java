package er.communication.foundation;

import java.util.Map;

/**
 * This interface defines the methods to create your own message processor.<p>
 * It's unlikely you create one from scratch and you should subclass ERAbstractMessageProcessor
 * but it's up to you!
 * 
 * @author Philippe Rabier
 *
 */
public interface ERMessageProcessor
{
	/**
	 * Accessors to set/get configuration read from the JSON description
	 * 
	 * @param sendingInformationsForMedia the map that contains the configuration
	 */
	void setSendingConfiguration(Map<String, Object> sendingInformationsForMedia);
	public Map<String, Object> getSendingConfiguration();
	
	/**
	 * This method is called by a channel. The "big" method to implement.
	 * 
	 * @param recipient of the message
	 * @param context in which the message is sent (a new user has signed up, a password has been modified,...)
	 * @param media the media to use (plain text message, jabber, ...)
	 * @param dataProcessor an optional object which can be used to create the content in some complex cases.
	 */
	void sendMessage(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor);
}
