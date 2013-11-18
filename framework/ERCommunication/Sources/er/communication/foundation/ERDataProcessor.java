package er.communication.foundation;

import java.util.Map;

/**
 * The objects who implement this interface can apply specific treatments 
 * to the data used to build the text content. This opens possibilities to the clients
 * of a ERCommunicationChanel object to shape data before parsing the template.
 * 
 * @author Philippe Rabier
 *
 */
public interface ERDataProcessor 
{
	String processTemplate(String template, Map<String, Object> data);
}
