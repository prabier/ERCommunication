package er.communication.foundation;

import java.util.List;

/**
 * A communication channel is a an object that will send a message through different medias.<p>
 * The client of the channel doesn't care if there will be an email, a notification,... or any combination.<br>
 * The message content depends on the context.
 * 
 * @author Philippe Rabier
 *
 */
public class ERChannel 
{

	private static ERMediaProvider mediaProvider;
	private static ERMessageProcessorFactory mpFactory;
	
	/**
	 * Send a message to a recipient based on a context
	 * 
	 * @param recipient of the message
	 * @param context which allows to know the media used to communicate
	 * @param dataProcessor (optional) a data processor used to transform data that are used to build the message. Can be null.
	 */
	public void sendMessage(ERRecipient recipient, ERCommunicationContext context, ERDataProcessor dataProcessor)
	{
		if (recipient == null)
			throw new IllegalArgumentException("There must be at least one recipient.");
		if (context == null)
			throw new IllegalArgumentException("Communication context can't be null.");
		List<ERMedia> medias = getMediaProvider().getMedias(context.getName());
		for (ERMedia aMedia : medias) 
		{
			ERMessageProcessor messageProcessor = getMessageProcessorFactory().newMessageProcessor(context, aMedia);
			if (messageProcessor != null)
				messageProcessor.sendMessage(recipient, context, aMedia, dataProcessor);
		}
	}

	/**
	 * Send a message to a list of recipients based on a context
	 * 
	 * @param list of recipients of the message
	 * @param context which allows to know the media used to communicate
	 * @param dataProcessor (optional) a data processor used to transform data that are used to build the message. Can be null.
	 */
	public void sendMessage(List<ERRecipient> recipients, ERCommunicationContext context, ERDataProcessor dataProcessor)
	{
		if (recipients == null || recipients.size() == 0)
			throw new IllegalArgumentException("There must be at least one recipient.");
		
		for (ERRecipient aRecipient : recipients) 
		{
			sendMessage(aRecipient, context, dataProcessor);
		}		
	}
	
	/**
	 * Used to set a media provider
	 * 
	 * @param provider which is used when a channel object sends a message. 
	 */
	public static void setMediaProvider(ERMediaProvider provider)
	{
		mediaProvider = provider;
	}
	
	public static ERMediaProvider getMediaProvider()
	{
		if (mediaProvider == null)
			throw new IllegalStateException("MediaProvider is null. It must be set before using a channel object.");
		return mediaProvider;
	}
	
	/**
	 * Used to set a message processor factory
	 * 
	 * @param factory which is used when a channel object sends a message. 
	 */
	public static void setMessageProcessorFactory(ERMessageProcessorFactory factory)
	{
		mpFactory = factory;
	}
	
	public static ERMessageProcessorFactory getMessageProcessorFactory()
	{
		if (mpFactory == null)
			throw new IllegalStateException("MessageProcessorFactory is null. It must be set before using a channel object.");
		return mpFactory;
	}
}
