package er.communication.foundation;

import er.communication.jabber.ERSimpleMessageProcessor;
import er.communication.mail.ERHTMLMailProcessor;
import er.communication.mail.ERPlainTextMailProcessor;


public enum ERDefaultMedia implements ERMedia 
{
	PLAIN_TEXT_MAIL(ERPlainTextMailProcessor.class),
	HTML_MAIL(ERHTMLMailProcessor.class),
	JABBER(ERSimpleMessageProcessor.class);
	
	private final Class<? extends ERMessageProcessor> messageProcessorClass;
	
	ERDefaultMedia(Class<? extends ERMessageProcessor> messageProcessorClass)
	{
		this.messageProcessorClass = messageProcessorClass;
	}
	
	public String getName() { return toString(); }
	
	public ERMessageProcessor getMessageProcessor() throws InstantiationException, IllegalAccessException 
	{
		return  (ERMessageProcessor)messageProcessorClass.newInstance();
	}
}
