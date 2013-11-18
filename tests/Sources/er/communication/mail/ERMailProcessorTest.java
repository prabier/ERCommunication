package er.communication.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import er.communication.foundation.ERAbstractMessageProcessor;
import er.communication.foundation.ERCommunicationContext;
import er.communication.foundation.ERDefaultMedia;
import er.communication.foundation.ERMedia;
import er.communication.foundation.ERRecipient;

public class ERMailProcessorTest 
{

	public class MailProcessor extends ERMailProcessor
	{

		public boolean isSent = false;
		
		@Override
		public void sendMail(ERRecipient recipient, ERMedia media, String subject, String textContent) 
		{
			isSent = true;
			
		}	
	}
	
	private class TemplateProviderTest implements ERAbstractMessageProcessor.TemplateProvider
	{
		public static final String SUBJECT = "This is a subject!";
		public String key;

		@Override
		public String templateForMedia(String key, String targetLanguage, ERMedia media) 
		{ 
			this.key = key;
			return SUBJECT; 
		}
	}

	@Test
	public void testSendMail() 
	{
		MailProcessor mp = new MailProcessor();
		ERRecipient recipient = new ERRecipient() {
			@Override
			public String getLastName() { return "Hill"; }		
			@Override
			public String getLanguage() { return "en"; }	
			@Override
			public String getIdentifier(ERMedia media) { return "yes"; }	
			@Override
			public String getFirstName() { return "Chuck"; }
		};
		ERCommunicationContext context = new ERCommunicationContext("CONTEXT", null);
		mp.sendMessage(recipient, context, ERDefaultMedia.PLAIN_TEXT_MAIL, null);
		// Test sendMail is called.
		assertTrue(mp.isSent);
	}
	
	@Test
	public void testGetSubject()
	{
		TemplateProviderTest aTemplateProvider = new TemplateProviderTest();
		MailProcessor.setTemplateProvider(aTemplateProvider);
		MailProcessor mp = new MailProcessor();
		String contextName = "CONTEXT";
		String subject = mp.getSubject("en", contextName, ERDefaultMedia.PLAIN_TEXT_MAIL, null, new HashMap<String, Object>());
		// Check that the subject key is CONTEXT.SUBJECT_SUFFIX
		assertEquals(aTemplateProvider.key, contextName + "." + ERMailProcessor.SUBJECT_SUFFIX);
		assertEquals(subject, TemplateProviderTest.SUBJECT);
	}
}
