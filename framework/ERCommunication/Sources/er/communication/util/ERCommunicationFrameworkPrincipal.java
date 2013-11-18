package er.communication.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import er.communication.foundation.ERChannel;
import er.communication.foundation.ERCommunicationContext;
import er.communication.foundation.ERMedia;
import er.communication.foundation.ERMediaManager;
import er.communication.foundation.ERMediaProvider;
import er.communication.foundation.ERMessageProcessor;
import er.communication.foundation.ERMessageProcessorFactory;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXUtilities;

/**
 * Initialize everything we need to send messages through different medias.<p>
 * The main purpose is to read json configuration files which can reside in any framework of your application
 * 
 * @author Philippe Rabier
 *
 */
public class ERCommunicationFrameworkPrincipal extends ERXFrameworkPrincipal implements ERMediaProvider, ERMessageProcessorFactory
{
    private static final Logger log = Logger.getLogger(ERCommunicationFrameworkPrincipal.class);
	private ERMediaManager mediaManager;
	private Map<String,  Object> mediaContext;

    static 
    {
        log.debug("ERCommunicationFrameworkPrincipal: static: ENTERED");
        setUpFrameworkPrincipalClass(ERCommunicationFrameworkPrincipal.class);
        log.debug("ERCommunicationFrameworkPrincipal: static: DONE");
    }

    public static ERCommunicationFrameworkPrincipal sharedInstance() 
    {
        return sharedInstance(ERCommunicationFrameworkPrincipal.class);
    }
    
	/**
	 * This method reads the er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch property 
	 * It's a static method because the sharedInstance could be null if it's not running.
	 * 
	 * @return true (false by default).
	 */
	public static boolean communicationServiceMustRun()
	{
		return ERXProperties.booleanForKeyWithDefault("er.communication.util.ERCommunicationFrameworkPrincipal.communicationServiceToLaunch", false);
	}

	/**
	 * Give the file name which can exist in any frameworks that contains the json description.
	 * 
	 * @return file name to look up, MediaContext.json by default.
	 */
    private String mediaContextFileName() 
    {
        return ERXProperties.stringForKeyWithDefault("er.communication.util.ERCommunicationFrameworkPrincipal.MediaContextFileName", "MediaContext.json");
    }

	@Override
	public void finishInitialization() 
	{
        log.debug("method: finishInitialization: " + this);
        if (!communicationServiceMustRun())
			log.info("******************** method:finishInitialization: Communication Service has not been launched. ********************");
        else
        {
        	// Look up in the application resource if a description file exists.
        	Map<String, Object> data = new HashMap<String, Object>();
        	{
        		String content = readDescriptionFromMediaContextFile(mediaContextFileName(), null);
        		if (StringUtils.isNotEmpty(content))
        		{
        			Map<String, Object> aMap = parseDescription(content);
        			if (aMap.size() > 0)
        				data.putAll(aMap);
        		}
        	}
        	
        	// Look up in all frameworks if  description files exist.
        	for (Enumeration<String> e = ERXUtilities.allFrameworkNames().objectEnumerator(); e.hasMoreElements();) {
        		String frameworkName = e.nextElement();
            	String content = readDescriptionFromMediaContextFile(mediaContextFileName(), frameworkName);
        		if (StringUtils.isNotEmpty(content))
        		{
        			Map<String, Object> aMap = parseDescription(content);
        			if (aMap.size() > 0)
        				data.putAll(aMap);
        		}
        	}

        	if (data.size() == 0)
        		throw new IllegalStateException("There are no communication workflow description. Turn off the service.");
        	else
        		setMediaContext(java.util.Collections.unmodifiableMap(data));
        	

        	if (log.isDebugEnabled())
        		log.debug("Method: finishInitialization: media by context loaded.");
        	ERChannel.setMediaProvider(this);
        	ERChannel.setMessageProcessorFactory(this);
        }
	}
	
	public ERMessageProcessor newMessageProcessor(ERCommunicationContext context, ERMedia media)
	{
		try 
		{
			ERMessageProcessor newMessageProcessor = media.getMessageProcessor();
			newMessageProcessor.setSendingConfiguration(getSendingConfigurationForContextAndMedia(context.getName(), media));
			return newMessageProcessor;
		} catch (Exception e) 
		{
			log.error("method: newMessageProcessor: problem with the media: " + media, e);
		}
		return null;
	}

	public ERMediaManager getMediaManager()
	{
		if (!communicationServiceMustRun())
			throw new IllegalStateException("method: getMediaManager: tried to access the media manager but communication service is not supposed to run!");
		if (mediaManager == null)
			mediaManager = new ERMediaManager();
		return mediaManager;
	}
	
	public List<ERMedia> getMedias(String contextName) 
	{
		Map<String, Object> data = (Map<String, Object>) mediaContext.get(contextName);
		List<String> array = (List<String>) data.get("Medias");
		return getMediaManager().getMedias(array);
	}
	
	/**
	 * mediaContext is a map which contain the work flows for each communication framework.
	 * 
	 * @return the mediaContext object
	 */
	protected Map<String, Object> getMediaContext() 
	{
		return mediaContext;
	}

	protected void setMediaContext(Map<String, Object> mediaContext) 
	{
		this.mediaContext = mediaContext;
	}

	/**
	 * A configuration can be defined in the JSON content specific to a media and a context.<p>
	 * For example, the component if you want to send rich HTML message.
	 *  
	 * @param contextName
	 * @param media
	 * @return a configuration object as a map
	 */
	protected Map<String, Object> getSendingConfigurationForContextAndMedia(String contextName, ERMedia media)
	{
		Map<String, Object> data = (Map<String, Object>) getMediaContext().get(contextName);
		Map<String, Object> sendingConfiguration = (Map<String, Object>) data.get(media.getName());
		return sendingConfiguration;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> parseDescription(String jsonContent)
	{
		JSONObject parsedData = null;
		JSONParser parser = new JSONParser();
		try {
			parsedData =  (JSONObject) parser.parse(jsonContent);
		} catch (ParseException e) {
			log.error("method: parseDescription: error parsing jsonContent: " + jsonContent, e);
		}
		return parsedData;
	}
	
	protected String readDescriptionFromMediaContextFile(String fileName, String frameworkName)
	{
		String stringFromFile = null;
        InputStream stream = ERXFileUtilities.inputStreamForResourceNamed(fileName, null, null);
        try 
        {
        	if (stream != null) 
        	{
        		stringFromFile = ERXFileUtilities.stringFromInputStream(stream, null);
            }
        } catch (IOException ioe) {
            log.error("method: readDescriptionFromJSONFile: Error reading file <" + fileName + "> from framework " + frameworkName);
        } finally {
        	try {if(stream != null) {stream.close();}} catch(IOException e) { log.error("method: readDescriptionFromJSONFile: failed attempt to close stream.");}
        }
        
        return stringFromFile;
	}
}
