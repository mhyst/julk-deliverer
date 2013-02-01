package julk.net.mail;

/**
 */
public class SMTPException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6321138150777813781L;
	private String message;
	
	public SMTPException (String msg)
	{
		message = msg;
	}
	
	public String getMessage ()
	{
		return message;
	}
}
