package er.communication.foundation;

/**
 * Interface to implement when a class creates new message processor.
 * 
 * @author Philippe Rabier
 *
 */
public interface ERMessageProcessorFactory 
{
	public ERMessageProcessor newMessageProcessor(ERCommunicationContext context, ERMedia media);
}
