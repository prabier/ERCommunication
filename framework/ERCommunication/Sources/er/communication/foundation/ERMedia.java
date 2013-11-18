package er.communication.foundation;

import java.lang.reflect.InvocationTargetException;

/**
 * Examples of Medias:
 * <ul>
 * <li>JABBER
 * <li>PUSH_NOTIFICATION
 * <li>PLAIN_TEXT_MAIL
 * <li>HTML_MAIL
 * </ul>
 * @author Philippe Rabier
 *
 */
public interface ERMedia
{
	String getName();
	
	ERMessageProcessor getMessageProcessor() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
