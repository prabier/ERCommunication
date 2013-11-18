package er.communication.foundation;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * We test that the channel send a message to the right recipient(s).<p>
 * So this class includes fake ERMediaProvider, ERMessageProcessor and ERMessageProcessorFactory implementations to check 
 * if the message is sent.
 * 
 * @author Philippe Rabier
 *
 */
public class ERChannelTest 
{
	private class MediaProviderTest implements ERMediaProvider
	{
		@Override
		public List<ERMedia> getMedias(String contextName) 
		{ 
			ArrayList<ERMedia> medias = new ArrayList<ERMedia>();
			medias.add(ERDefaultMedia.PLAIN_TEXT_MAIL);
			return medias; 
		}
	}
	
	private class MessageProcessorTest implements ERMessageProcessor
	{
		private List<ERRecipient> recipients = new ArrayList<ERRecipient>();
		private int numberOfMessages;
		@Override
		public void setSendingConfiguration( Map<String, Object> sendingInformationsForMedia) { }

		@Override
		public Map<String, Object> getSendingConfiguration() { return null;	}

		@Override
		public void sendMessage(ERRecipient recipient, ERCommunicationContext context, ERMedia media, ERDataProcessor dataProcessor) 
		{
			recipients.add(recipient);
			numberOfMessages++;
		}	
		public List<ERRecipient> getRecipients() { return recipients; }
		public int numberOfMessages() { return numberOfMessages; }
	}
	
	private class MessageProcessorFactoryTest implements ERMessageProcessorFactory
	{
		private ERMessageProcessor theMessageProcessor;
		public MessageProcessorFactoryTest()
		{
			theMessageProcessor = new MessageProcessorTest();
		}
		@Override
		public ERMessageProcessor newMessageProcessor(ERCommunicationContext context, ERMedia media) { return theMessageProcessor; }
		public ERMessageProcessor theMessageProcessor() { return theMessageProcessor; }
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageWithNullRecipient()
	{
		ERChannel channel = new ERChannel();
		channel.sendMessage((ERRecipient) null, null, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageWithNullContext()
	{
		ERChannel channel = new ERChannel();
		channel.sendMessage(new ERRecipient() 
		{	
			@Override
			public String getLastName() { return "Hill";}
			@Override
			public String getLanguage() { return "en"; }
			@Override
			public String getIdentifier(ERMedia media) { return "anID";	}
			@Override
			public String getFirstName() { return "Chuck"; }
		}, null, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageWithNullRecipients()
	{
		ERChannel channel = new ERChannel();
		channel.sendMessage(new ArrayList<ERRecipient>(), null, null);
	}

	/**
	 * The goal is to ensure one message has been sent to the right recipient.<p>
	 * We know the message processor so we ask it if the number of messages is correct
	 * and the recipient as well.
	 */
	@Test
	public void testSendMessageERRecipientERCommunicationContextERDataProcessor() 
	{
		ERChannel.setMediaProvider(new MediaProviderTest());
		
		MessageProcessorFactoryTest factory = new MessageProcessorFactoryTest();
		MessageProcessorTest aMessageProcessor  = (MessageProcessorTest) factory.theMessageProcessor();
		
		ERChannel.setMessageProcessorFactory(factory);
		
		ERChannel channel = new ERChannel();
		channel.sendMessage(new ERRecipient() 
		{	
			@Override
			public String getLastName() { return "Hill";}
			@Override
			public String getLanguage() { return "en"; }
			@Override
			public String getIdentifier(ERMedia media) { return "anID";	}
			@Override
			public String getFirstName() { return "Chuck"; }
		}, 
		new ERCommunicationContext("CONTEXT", null), 
		null);
		assertTrue(aMessageProcessor.numberOfMessages() == 1);
		assertTrue(aMessageProcessor.getRecipients().get(0).getLastName().equals("Hill"));
	}

	/**
	 * The goal is to ensure 2 messages has been sent to the right recipients.<p>
	 * We know the message processor so we ask it if the number of messages is correct
	 * and the recipient as well.
	 */
	@Test
	public void testSendMessageListOfERRecipientERCommunicationContextERDataProcessor() 
	{
		ERRecipient recipient1 = new ERRecipient() 
		{	
			@Override
			public String getLastName() { return "Hill";}
			@Override
			public String getLanguage() { return "en"; }
			@Override
			public String getIdentifier(ERMedia media) { return "anID1";	}
			@Override
			public String getFirstName() { return "Chuck"; }
		};
		ERRecipient recipient2 = new ERRecipient() 
		{	
			@Override
			public String getLastName() { return "Rabier";}
			@Override
			public String getLanguage() { return "fr"; }
			@Override
			public String getIdentifier(ERMedia media) { return "anID2";	}
			@Override
			public String getFirstName() { return "Philippe"; }
		};
		List<ERRecipient> recipients = new ArrayList<ERRecipient>();
		recipients.add(recipient1);
		recipients.add(recipient2);

		ERChannel.setMediaProvider(new MediaProviderTest());
		
		MessageProcessorFactoryTest factory = new MessageProcessorFactoryTest();
		MessageProcessorTest aMessageProcessor  = (MessageProcessorTest) factory.theMessageProcessor();
		
		ERChannel.setMessageProcessorFactory(factory);
		
		ERChannel channel = new ERChannel();
		channel.sendMessage(recipients, new ERCommunicationContext("CONTEXT", null), null);
		assertTrue(aMessageProcessor.numberOfMessages() == 2);
		assertTrue(aMessageProcessor.getRecipients().get(0).getLastName().equals("Hill"));
		assertTrue(aMessageProcessor.getRecipients().get(1).getLastName().equals("Rabier"));
	}
}
