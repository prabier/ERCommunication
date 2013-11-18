package er.communication.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import er.communication.foundation.ERCommunicationContext;
import er.communication.foundation.ERDefaultMedia;
import er.communication.foundation.ERMessageProcessor;
import er.communication.mail.ERPlainTextMailProcessor;
import er.communication.util.ERCommunicationFrameworkPrincipal;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;

public class ERCommunicationFrameworkPrincipalTest {

	@Test
	public void testCommunicationServiceMustRun() 
	{
		Properties p = new Properties(System.getProperties());
		p.setProperty("er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch", "false");
		System.setProperties(p);
		assertFalse(ERCommunicationFrameworkPrincipal.communicationServiceMustRun());
		ERXProperties.systemPropertiesChanged(); // To reset Properties
		p = new Properties(System.getProperties());
		p.setProperty("er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch", "true");
		System.setProperties(p);
		assertTrue(ERCommunicationFrameworkPrincipal.communicationServiceMustRun());
	}

	@Test
	public void testSharedInstance() 
	{
		ERCommunicationFrameworkPrincipal si = ERCommunicationFrameworkPrincipal.sharedInstance();
		assertNotNull(si);
	}

	@Test
	public void testGetMediaManagerWithSuccess() 
	{
		ERXProperties.systemPropertiesChanged(); // To reset Properties
		ERCommunicationFrameworkPrincipal si = ERCommunicationFrameworkPrincipal.sharedInstance();
		Properties p = new Properties(System.getProperties());
		p.setProperty("er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch", "true");
		System.setProperties(p);
		assertNotNull(si.getMediaManager());
	}

	@Test (expected=IllegalStateException.class)
	public void testGetMediaManagerWithException() 
	{
		ERXProperties.systemPropertiesChanged(); // To reset Properties
		ERCommunicationFrameworkPrincipal si = ERCommunicationFrameworkPrincipal.sharedInstance();
		Properties p = new Properties(System.getProperties());
		p.setProperty("er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch", "false");
		System.setProperties(p);
		si.getMediaManager();
	}

	@Test
	public void testParseDescription() throws IOException 
	{
		URL url = this.getClass().getResource("MediaContext.json");
		File jsonFile = new File(url.getFile());
		String content = ERXFileUtilities.stringFromFile(jsonFile);
		ERCommunicationFrameworkPrincipal si = ERCommunicationFrameworkPrincipal.sharedInstance();
		Map<String, Object> parsedData = si.parseDescription(content);
		assertTrue(parsedData.containsKey("context1"));
		assertTrue(parsedData.containsKey("context2"));
	}

	@Test
	public void testGetMedias() throws IOException 
	{
		URL url = this.getClass().getResource("MediaContext.json");
		File jsonFile = new File(url.getFile());
		String content = ERXFileUtilities.stringFromFile(jsonFile);
		ERCommunicationFrameworkPrincipal si = ERCommunicationFrameworkPrincipal.sharedInstance();
		Map<String, Object> parsedData = si.parseDescription(content);
		Map<String, Object> dataForContext =  (Map<String, Object>) parsedData.get("context1");
		List<String> medias = (List<String>) dataForContext.get("Medias");
		assertTrue(medias.size() == 2);
		assertTrue(medias.contains("PLAIN_TEXT_MAIL"));
		assertTrue(medias.contains("HTML_MAIL"));
	}

	@Test
	public void testNewMessageProcessor() throws IOException 
	{
		URL url = this.getClass().getResource("MediaContext.json");
		File jsonFile = new File(url.getFile());
		String mediaContextContent = ERXFileUtilities.stringFromFile(jsonFile);

		Properties p = new Properties(System.getProperties());
		p.setProperty("er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch", "true");
		System.setProperties(p);
		
		ERCommunicationFrameworkPrincipal si = ERCommunicationFrameworkPrincipal.sharedInstance();
		Map<String, Object> mediaContext = si.parseDescription(mediaContextContent);
		si.setMediaContext(mediaContext);
		ERCommunicationContext context = new ERCommunicationContext("context1", null);
		ERMessageProcessor mp = si.newMessageProcessor(context, ERDefaultMedia.PLAIN_TEXT_MAIL);
		assertTrue(mp instanceof ERPlainTextMailProcessor);
		// The values we test are in the MediaContext.json file
		Map<String, Object> configuration = mp.getSendingConfiguration();
		assertEquals(configuration.get("mailTextKey"), "mailTextValue");
	}
}
