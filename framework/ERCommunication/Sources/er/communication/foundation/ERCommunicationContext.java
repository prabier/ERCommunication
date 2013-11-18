package er.communication.foundation;

import java.util.Map;

/**
 * ERCommunicationContext seems more like an immutable structure that contains informations
 * to let know what message must be sent and provides the data to use.
 * 
 * @author Philippe Rabier
 *
 */
public class ERCommunicationContext 
{
	private final String name;
	private final Map<String, Object> data;
	private Map<String, Object> sendingInformations;
	
	public ERCommunicationContext(String name, Map<String, Object> data)
	{
		this.name = name;
		this.data = data;
	}
	
	/**
	 * @return the context name.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * @return the map to use when building a message.
	 */
	public Map<String, Object> getData()
	{
		return this.data;
	}

	/**
	 * Allow to set additional information which can be used by a processor to send a message.
	 * 
	 */
	public void setSendingInformations(Map<String, Object> sendingInformations) 
	{
		this.sendingInformations = sendingInformations;
	}

	public Map<String, Object> getSendingInformations() {
		return sendingInformations;
	}
	
	public String toString()
	{
		return "Context: name: " + this.name + " /data: " + data + " /sendingInformations: " + sendingInformations;
	}
}
