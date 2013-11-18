package er.communication.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import er.communication.foundation.ERChannelTest;
import er.communication.foundation.ERMessageProcessorTest;
import er.communication.mail.ERMailProcessorTest;
import er.communication.util.ERCommunicationFrameworkPrincipalTest;

@RunWith(Suite.class)
@SuiteClasses({
	ERChannelTest.class,
	ERMessageProcessorTest.class,
	ERMailProcessorTest.class,
	ERCommunicationFrameworkPrincipalTest.class
	})

public class AllTests {}
