package er.communication.foundation;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import er.extensions.foundation.ERXProperties;
import er.extensions.localization.ERXLocalizer;

/**
 * This class provides common method like retrieving template, template parsing, ...<p>
 * A message processor is instantiated each time a client needs to send informations based on a given context 
 * through a media.<br>
 * A message processor can use the sending configuration to get informations defined in the JSON description. 
 * 
 * @author Philippe Rabier
 *
 */
public abstract class ERAbstractMessageProcessor implements ERMessageProcessor 
{
	protected static final Logger log = Logger.getLogger(ERAbstractMessageProcessor.class);
	public final static String FIRST_NAME = "firstName";
	public final static String LAST_NAME = "lastName";
	public final static String COMMUNICATION_TEMPLATE_PREFIX = "CommunicationTemplate.";
	public final static String UNDEFINED_COMMUNICATION_TEMPLATE = "Undefined Communication Template";
	
	private Map<String, Object> mergedData = null;
	private Map<String, Object> sendingConfiguration;
	
	private static TemplateProvider templateProvider;
	
	/**
	 * Define the method which returns the template of your content based on a key, a language and a media.
	 *
	 */
	public static interface TemplateProvider 
	{
		public String templateForMedia(String key, String targetLanguage, ERMedia media);
	}
	
	/** 
	 * Default implementation of the TemplateProvider interface.<p>
	 * This default implementation searches the template in the resources using 
	 * a ERXLocalizer object.<br/>
	 * Create your own template provider to get your template from the database for example.
	 */
	public static class DefaultTemplateProvider implements TemplateProvider 
	{
		/**
		 * Look for a template based on the key and media parameter.<br>
		 * If it doesn't exist, look for a template with the key only.<br>
		 * If nothing is found, returns UNDEFINED_COMMUNICATION_TEMPLATE.
		 * 
		 * @param key
		 * @param targetLanguage
		 * @param media
		 * @return
		 */
		public String templateForMedia(String key, String targetLanguage, ERMedia media) 
		{
			ERXLocalizer localizer = targetLanguage != null ? ERXLocalizer.localizerForLanguage(targetLanguage) : ERXLocalizer.currentLocalizer();
			String template = (String)localizer.valueForKey(COMMUNICATION_TEMPLATE_PREFIX + key + "." + media.toString());
		    if (template == null)
		    	template = (String)localizer.valueForKey(COMMUNICATION_TEMPLATE_PREFIX + key);
		    if (template == null)
		    	template = UNDEFINED_COMMUNICATION_TEMPLATE;
			if (log.isDebugEnabled())
				log.debug("method: templateForMedia /template: " + template);
			return template;
		}
	}

	/**
	 * Gets the template provider
	 * 
	 * @return editing context factory
	 */
	public static TemplateProvider templateProvider() 
	{
		if (templateProvider == null) 
		{
			synchronized(ERAbstractMessageProcessor.class) {
				if(templateProvider == null) 
					templateProvider = new DefaultTemplateProvider();
			}
		}
		return templateProvider;
	}

	/**
	 * Sets the default editing context factory
	 * 
	 * @param aFactory
	 *            factory used to create editing contexts
	 */
	public static void setTemplateProvider(TemplateProvider aTemplateProvider) 
	{ 
		templateProvider = aTemplateProvider;
	}
	
	/**
	 * Abstract method which must be implemented in all concrete classes as the real job is done here.<p>
	 * sendContent() is called by sendMessage() if all conditions are ok (recipient ok, media not null, ...)
	 * 
	 * @param recipient
	 * @param context
	 * @param media
	 * @param dataProcessor
	 */
	protected abstract void sendContent(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor);

	/**
	 * Initialize the mergedData map and check if the message can be delivered.
	 * 
	 * @param recipient
	 * @param context
	 * @param media
	 * @param dataProcessor
	 */
	public void sendMessage(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor) 
	{
		if (recipient == null || context == null || media == null)
			throw new IllegalArgumentException("At least one argument is null /recipient: " + recipient + " /context: " + context + " /media: " + media);
		if (log.isDebugEnabled())
			log.debug("method: sendContent ENTER /recipient: " + recipient + " /context: " + context + " /media: " + media);

		if (isExistsIdentifierForMedia(recipient, media))
		{
			if (context.getData() == null)
				mergedData = new HashMap<String, Object>();
			else
				mergedData = new HashMap<String, Object>(context.getData());
			mergedData.put(FIRST_NAME, recipient.getFirstName());
			mergedData.put(LAST_NAME, recipient.getLastName());
			if (log.isDebugEnabled())
				log.debug("method: sendContent DONE /mergedData: " + mergedData);
			sendContent(recipient, context, media, dataProcessor);
		}
	}

	public void setSendingConfiguration(Map<String, Object> sendingInformations)
	{
		this.sendingConfiguration = sendingInformations;
	}

	public Map<String, Object> getSendingConfiguration()
	{
		return this.sendingConfiguration;
	}
	
	/**
	 * It compiles the template using the Mustache compiler then renders it with <code>data</code>   
	 * @param template to render
	 * @param data
	 * @return
	 */
	public String parseTemplate(String template, Map<String, Object> data) 
	{
		Template compiledTemplate = Mustache.compiler().compile(template);
		return compiledTemplate.execute(data);
	}

	/**
	 * Return the text to send through the media.<p>
	 * It first retrieves the template based on the media and the key then parses it.<br>
	 * Note that if dataProcessor is not null, it's called and the dataProcessor must return the content ready
	 * to be sent.
	 * 
	 * @param targetLanguage
	 * @param key (should contain the context name with a prefix or suffix)
	 * @param media
	 * @param dataProcessor
	 * @param mergedData
	 * @return the content to be sent
	 * 
	 * @see #parseTemplate(String, Map)
	 */
	public String getTextContent(String targetLanguage, String key, ERMedia media, ERDataProcessor dataProcessor, Map<String, Object> mergedData)
	{
		String textContent = null;
		String template = ERAbstractMessageProcessor.templateProvider().templateForMedia(key, targetLanguage, media);
		if (dataProcessor != null)
			textContent = dataProcessor.processTemplate(template, mergedData);
		else
			textContent = parseTemplate(template, mergedData);
		if (log.isDebugEnabled())
			log.debug("method: getTextContent /targetLanguage: " + targetLanguage + " /key: " + key + " /media: " + media + " /dataProcessor: " + dataProcessor + " /textContent: " + textContent);
		return textContent;
	}
	
	/**
	 * Return the merged data built from the context and recipient first name and last name.<p>
	 * This allow to use the FIRST_NAME and LAST_NAME values in the message.
	 * 
	 * @return data + entries for FIRST_NAME and LAST_NAME
	 */
	public Map<String, Object> getMergedData()
	{
		if (mergedData == null)
			throw new IllegalStateException("method: getMergedData: mergedData is null. Forgot to call sendMessage.");
		return mergedData;
	}
	
	/**
	 * Checks if there is an identifier for the media.<p>
	 * The recipient may have an email but not a jabber ID for example.
	 * 
	 * @param recipient
	 * @param media
	 * @return <code>true</code> if there is an identifier
	 */
	public boolean isExistsIdentifierForMedia(ERRecipient recipient, ERMedia media)
	{
		return recipient.getIdentifier(media) != null;
	}
	
	/**
	 * Helper method that returns the value of a property.<p>
	 * Raise an exception if the property doesn't exist to avoid a silent behavior
	 * 
	 * @param propertyName
	 * @return the value of the property
	 */
	public String getProperty(String propertyName)
	{
		String value;
		value = ERXProperties.stringForKey(propertyName);
		if (value == null)
			throw new IllegalStateException("method: getProperty: the property " + propertyName + " has not been set.");
		return value;
	}
}
