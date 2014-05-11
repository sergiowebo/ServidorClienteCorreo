package smtpserver;

public class MensajesRespuesta {
	
	// Mensajes definidos en el RFC para responder al cliente
	
	public final static String RFC_CRLF = "\r\n";
	public final static String RFC_220_SERVERUP = "220 SMTP Server IS UP";
	public final static String RFC_HELO = "HELO";
	public final static String RFC_EHLO = "EHLO";		
	public final static String RCC_250_SERVERREADY = "250 <domain> Requested mail action okay, completed";
	public final static String RFC_MAIL_FROM = "MAIL FROM:";
	public final static String RFC_250_OK = "250 OK";
	public static final String RFC_250_OK_RELAY = "250 OK Email will be sent in Relay mode";
	public final static String RFC_RCPT_TO = "RCPT TO:";
	public final static String RFC_DATA = "DATA";
	public final static String RFC_354_DATASTART = "354 Start mail input; end with <CRLF>.<CRLF>";
	public final static String RFC_ENDDATA = ".\r\n";
	public final static String RFC_QUIT = "QUIT";

	// Mensajes de error
	public final static String RFC_500 = "500 Command unknown";
	public final static String RFC_501_HELO = "501 HELO must be followed by domain name";
	public final static String RFC_501 = "501 Syntax error in parameters or arguments";
	public final static String RFC_502 = "502 user especified does not exist";
	public final static String RFC_554 = "554 Relay access denied.";
	public final static String RFC_554_none = "554 Not valid recipients";
	public final static String RFC_503 = "503 Bad squence of commands";
	public final static String RFC_503_HELO = "503 Must specify HELO domain";
	public final static String RFC_503_MAILFROM = "503 Must specify MAIL FROM: (sender) before";
	public final static String RFC_503_RCPTTO = "503 Must specify RCPT TO: (receiver) before";
	public final static String RFC_550 = "550 <usuario>: Recipient address rejected: User unknown in virtual mailbox table.";
	
	//public final static String ERROR = "ERROR - Command not implemented. We are working on it.";
}