package julk.net.mail;

/**
 */
public class POP3Exception extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3377173953678447907L;
	private String message;
	
	public POP3Exception(String msg)
	{
		message = msg;
	}
	
	public String getMessage()
	{
		return message;
	}
}
