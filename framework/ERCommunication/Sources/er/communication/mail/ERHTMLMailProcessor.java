package er.communication.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSData;

import er.communication.foundation.ERMedia;
import er.communication.foundation.ERRecipient;
import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;
import er.javamail.ERMailDeliveryHTML;

/**
 * Concrete implementation of a mail processor which sends HTML messages<p>
 * It uses the sending configuration map to get the component name to use<br>
 * <b>Note: </b> the component must implement an accessor data of type Map<String, Object>
 * 
 * @author Philippe Rabier
 *
 */
public class ERHTMLMailProcessor extends ERMailProcessor 
{
	/** 
	 * The key used to get the component name in the sending configuration map
	 */
	public static final String COMPONENT_NAME = "component";

	public void sendMail(ERRecipient recipient, ERMedia media, final String subject, final String textContent)
	{
		String componentName = (String) getSendingConfiguration().get(COMPONENT_NAME);
		String realName = recipient.getFirstName() + " " + recipient.getLastName();
		sendMailWithComponent(recipient.getIdentifier(media), realName, subject, textContent, recipient.getLanguage(), componentName);

	}
	private void sendMailWithComponent(String address, String realName, String subject, String textContent, String language, String component) 
	{
		WOApplication app = WOApplication.application();

		// build context
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put("accept-language", java.util.Arrays.asList(language));

		/* Tells the WORequest object to answer true when request.isUsingWebServer(). When isUsingWebServer() returns true, 
		 the URL contains the host and port number.*/
		headers.put("x-webobjects-adaptor-version", java.util.Arrays.asList("4.5"));

		// Host for URL
		String host = ERXProperties.stringForKeyWithDefault("er.communication.mail.host", app.host());
		headers.put("host", java.util.Arrays.asList(host));

		// Port number for URL
		String port = ERXProperties.stringForKey("er.communication.mail.port");
		if (port != null)
			headers.put(WORequest.ServerPortHeaderX, java.util.Arrays.asList(port));

		WORequest request = new WORequest("GET", app.adaptorPath() + "/" + app.name() + ".woa", "HTTP/1.1", headers, new NSData(), new HashMap());

		WOContext ctx = new WOContext(request);
		ctx.generateCompleteURLs();
		// Init template
		WOComponent page = ERXApplication.erxApplication().pageWithName(component, ctx);
		page.takeValueForKey(getMergedData(), "data");

		// create mail
		ERMailDeliveryHTML message = new ERMailDeliveryHTML();
		message.setComponent(page);
		// TODO add text part with
		// message.setAlternativeComponent(alternativeComponent);
		// and with
		// message.setHiddenPlainTextContent(content);

		try
		{
			message.setSubject(subject);
			if (realName != null)
				message.setToAddress(address, realName);
			else
				message.setToAddress(address);

			message.setFromAddress(fromEmail());

			// send the mail assynchronously
			message.sendMail();
		} catch (MessagingException e)
		{
			log.error("method sendMail: failed sending mail.", e);
		}
	}
}
