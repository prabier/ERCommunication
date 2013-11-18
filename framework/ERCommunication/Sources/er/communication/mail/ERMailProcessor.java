package er.communication.mail;

import java.util.Map;

import er.communication.foundation.ERAbstractMessageProcessor;
import er.communication.foundation.ERCommunicationContext;
import er.communication.foundation.ERDataProcessor;
import er.communication.foundation.ERMedia;
import er.communication.foundation.ERRecipient;

public abstract class ERMailProcessor extends ERAbstractMessageProcessor 
{

	private String fromEmail = null;
	public static final String SUBJECT_SUFFIX = "subject";

	public ERMailProcessor() 
	{
		super();
	}

	public abstract void sendMail(ERRecipient recipient, ERMedia media, final String subject, final String textContent);
	
	@Override
	public void sendContent(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor) 
	{
		String subject = getSubject(recipient.getLanguage(), context.getName() + "." + SUBJECT_SUFFIX, media, dataProcessor, getMergedData());
		String textContent = getTextContent(recipient.getLanguage(), context.getName(), media, dataProcessor, getMergedData());
		sendMail(recipient, media, subject, textContent);
	}

	public String getSubject(String targetLanguage, String contextName, ERMedia media, ERDataProcessor dataProcessor, Map<String, Object> mergedData) 
	{
		return getTextContent(targetLanguage, contextName + "." + SUBJECT_SUFFIX, media, dataProcessor, mergedData);
	}

	public String fromEmail()
	{
		if (fromEmail == null)
			fromEmail =  getProperty("er.communication.foundation.mailFrom");
		return fromEmail;
	}
}