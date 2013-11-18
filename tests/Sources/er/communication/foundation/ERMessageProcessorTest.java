package er.communication.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import er.communication.foundation.ERAbstractMessageProcessor;
import er.communication.foundation.ERCommunicationContext;
import er.communication.foundation.ERDataProcessor;
import er.communication.foundation.ERDefaultMedia;
import er.communication.foundation.ERMedia;
import er.communication.foundation.ERRecipient;

public class ERMessageProcessorTest 
{

	private class MessageProcessor extends ERAbstractMessageProcessor
	{
		@Override
		protected void sendContent(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor) { }
	}
	
	private class TemplateProviderTest implements ERAbstractMessageProcessor.TemplateProvider
	{
		@Override
		public String templateForMedia(String key, String targetLanguage, ERMedia media) { return "Hi, {{firstName}} {{lastName}}!"; }
	}
	
	@Test
	public void testDefaultTemplateProvider() 
	{
		assertTrue(MessageProcessor.templateProvider() instanceof ERAbstractMessageProcessor.DefaultTemplateProvider);
	}

	@Test
	public void testSetTemplateProvider() 
	{
		MessageProcessor.setTemplateProvider(new TemplateProviderTest());
		assertTrue(MessageProcessor.templateProvider() instanceof TemplateProviderTest);
	}

	@Test
	public void testParseTemplate() 
	{
		MessageProcessor p = new MessageProcessor();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("content", "important request");
		data.put("from", "Wonder team");
		
		String result = p.parseTemplate("Hi, this is a new {{content}} from {{from}}", data);
		System.out.println("result rendered by Mustache: " + result);
		assertEquals(result, "Hi, this is a new important request from Wonder team");
	}

	@Test
	public void testGetTextContent() 
	{
		MessageProcessor p = new MessageProcessor();
		MessageProcessor.setTemplateProvider(new TemplateProviderTest());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(ERAbstractMessageProcessor.FIRST_NAME, "Chuck");
		data.put(ERAbstractMessageProcessor.LAST_NAME, "Hill");
		String content = p.getTextContent("en", "aKey", ERDefaultMedia.PLAIN_TEXT_MAIL, null, data);
		assertEquals(content, "Hi, Chuck Hill!");
	}

	@Test
	public void testGetTextContentWithDataProcessor() 
	{
		// The processor returns a completely different content.
		ERDataProcessor dp = new ERDataProcessor() 
		{
			@Override
			public String processTemplate(String template, Map<String, Object> data) { return "Hi Sacha"; }
		};
		MessageProcessor p = new MessageProcessor();
		MessageProcessor.setTemplateProvider(new TemplateProviderTest());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(ERAbstractMessageProcessor.FIRST_NAME, "Chuck");
		data.put(ERAbstractMessageProcessor.LAST_NAME, "Hill");
		String content = p.getTextContent("en", "aKey", ERDefaultMedia.PLAIN_TEXT_MAIL, dp, data);
		assertEquals(content, "Hi Sacha");
	}

	@Test (expected = IllegalArgumentException.class)
	public void testNullRecipient()
	{
		MessageProcessor p = new MessageProcessor();
		ERCommunicationContext context = new ERCommunicationContext("CONTEXT", null);
		p.sendMessage(null, context, ERDefaultMedia.PLAIN_TEXT_MAIL, null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testNullContext()
	{
		MessageProcessor p = new MessageProcessor();
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
		p.sendMessage(recipient, null, ERDefaultMedia.PLAIN_TEXT_MAIL, null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testNullMedia()
	{
		MessageProcessor p = new MessageProcessor();
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
		p.sendMessage(recipient, context, null, null);
	}
	
	@Test
	public void testGetMergedData() 
	{
		MessageProcessor p = new MessageProcessor();
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
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("aKey", "aValue");
		ERCommunicationContext context = new ERCommunicationContext("CONTEXT", data);
		p.sendMessage(recipient, context, ERDefaultMedia.PLAIN_TEXT_MAIL, null);
		
		Map<String, Object> mergedData = p.getMergedData();
		assertEquals(mergedData.get("aKey"), "aValue");
		assertEquals(mergedData.get(ERAbstractMessageProcessor.FIRST_NAME), "Chuck");
		assertEquals(mergedData.get(ERAbstractMessageProcessor.LAST_NAME), "Hill");	
	}

	@Test
	public void testIsExistsIdentifierForMedia() 
	{
		MessageProcessor p = new MessageProcessor();
		boolean resultWithNoIdentifier = p.isExistsIdentifierForMedia(new ERRecipient() {
			@Override
			public String getLastName() { return "Hill"; }		
			@Override
			public String getLanguage() { return null; }	
			@Override
			public String getIdentifier(ERMedia media) { return null; }	
			@Override
			public String getFirstName() { return null; }
		}, ERDefaultMedia.PLAIN_TEXT_MAIL);
		assertFalse(resultWithNoIdentifier);
		
		boolean resultWithIdentifier = p.isExistsIdentifierForMedia(new ERRecipient() {
			@Override
			public String getLastName() { return "Hill"; }
			@Override
			public String getLanguage() { return null; }
			@Override
			public String getIdentifier(ERMedia media) { return "yes"; }
			@Override
			public String getFirstName() { return null; }
		}, ERDefaultMedia.PLAIN_TEXT_MAIL);
		assertTrue(resultWithIdentifier);
	}
}
