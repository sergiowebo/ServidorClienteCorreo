package smtp;

public class MensajesRespuesta {
	//Protocolo POP3
	public final static String READY = "+OK Service Ready.";
	public final static String OK = "+OK";
	public final static String NOT_LOGGED = "-ERR User not logged in. Send USER command.";
	public final static String PASS_REQ = "+OK Password required";
	public final static String LOGIN_FAILED = "-ERR Login Failed.";
	public final static String LOGIN_OK = "+OK User logged in.";
	public final static String MENS_NOT_FOUND = "-ERR message not found.";
	public final static String MENS_DELE = "+OK Message deleted";
	public final static String MENS_NOT_DELE = "-ERR message not deleted. The message doesn’t exist.";
	public final static String ERROR = "-ERR Command not implemented. We are working on it.";
	public final static String CMD_CRLF = "\r\n";
	
	//Mensajes cliente POP3
	public final static String STAT = "STAT";
	public final static String LIST = "LIST";
	public final static String RETR = "RETR";
	public final static String DELE = "DELE";
	public final static String USER = "USER ";
	public final static String PASS = "PASS ";
	public final static String QUIT = "QUIT";
	public final static String STOP = "STOPSERVERPOP";
	
}