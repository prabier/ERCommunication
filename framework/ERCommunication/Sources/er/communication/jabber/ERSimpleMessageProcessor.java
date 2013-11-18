package er.communication.jabber;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import er.communication.foundation.ERAbstractMessageProcessor;
import er.communication.foundation.ERCommunicationContext;
import er.communication.foundation.ERDataProcessor;
import er.communication.foundation.ERMedia;
import er.communication.foundation.ERRecipient;
import er.extensions.foundation.ERXStringUtilities;

public class ERSimpleMessageProcessor extends ERAbstractMessageProcessor 
{
	public static final String JABBER_HOST = "host";
	public static final String JABBER_PORT = "port";
	private static Connection serverConnection = null;
	
	@Override
	public void sendContent(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor) 
	{
		String textContent = getTextContent(recipient.getLanguage(), context.getName(), media, dataProcessor, getMergedData());
		sendChatMessage(recipient.getIdentifier(media), textContent);
	}

	protected void sendChatMessage(String identifier, String textContent)  
	{
		Connection connection;
		try 
		{
			connection = getConnection();
			ChatManager chatmanager = connection.getChatManager();
			Chat aChat = chatmanager.createChat(identifier, new MessageListener() 
			{
				@Override
				public void processMessage(Chat chat, Message message) 
				{
					try 
					{
						chat.sendMessage("Oups, I'm sorry, I'm very primitive, can do nothing for you.");
					} catch (XMPPException e) 
					{
						log.error("method: sendChatMessage: error in the listener.", e);
					}
				}
			});
			aChat.sendMessage(textContent);
		} catch (XMPPException e) 
		{
			log.error("method: sendChatMessage: error when sending message /identifier: " + identifier + " /textContent: " + textContent, e);
		}
	}

	protected Connection getConnection() throws XMPPException 
	{
		if (serverConnection == null)
		{
			// Create a connection to the jabber server on a specific port.
			String host = getProperty("er.communication.jabber.host");
			int port = ERXStringUtilities.integerWithString(getProperty("er.communication.jabber.port"));
			ConnectionConfiguration config = new ConnectionConfiguration(host, port);
			serverConnection = new XMPPConnection(config);
			serverConnection.connect();
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setStatus("Gone fishing");
			serverConnection.sendPacket(presence);		}
		return serverConnection;
	}
	
	public static void setConnection(Connection connection)
	{
		serverConnection = connection;
	}
}
