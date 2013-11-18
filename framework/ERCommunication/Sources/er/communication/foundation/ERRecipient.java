package er.communication.foundation;


/**
 * This interface defines the user informations required to send a message.<p>
 * There are the first name, last name and the identifier that depends on the media<br>
 * For an email, the expected identifier is a valid email, for jabber a jabberId, ...
 * @author Philippe Rabier
 *
 */
public interface ERRecipient 
{
	String getLastName();
	String getFirstName();
	String getLanguage();
	String getIdentifier(ERMedia media);
}
