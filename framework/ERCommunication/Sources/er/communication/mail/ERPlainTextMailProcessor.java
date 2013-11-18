package er.communication.mail;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang.StringUtils;

import er.communication.foundation.ERMedia;
import er.communication.foundation.ERRecipient;
import er.javamail.ERMailDeliveryPlainText;

/**
 * Concrete implementation of ERMailProcessor which sends plain text email.
 * 
 * @author Philippe Rabier
 *
 */
public class ERPlainTextMailProcessor extends ERMailProcessor
{

	public void sendMail(ERRecipient recipient, ERMedia media, final String subject, final String textContent)
	{
		String address = recipient.getIdentifier(media);
		String realName = recipient.getFirstName() + " " + recipient.getLastName();
		try
		{
			ERMailDeliveryPlainText plainText = new ERMailDeliveryPlainText(); 
			plainText.newMail(); 
			plainText.setFromAddress(fromEmail());
			if (StringUtils.isNotEmpty(realName))
				plainText.setToAddress(address, realName);
			else
				plainText.setToAddress(address);
			plainText.setSubject(subject); 
			plainText.setTextContent(textContent);
			plainText.sendMail(false);
		} 
		catch (AddressException e) 
		{ 
			log.error("Method: sendMail: ", e);
		} 
		catch (MessagingException e) 
		{ 
			log.error("Method: sendMail: ", e);
		}
	}
}
