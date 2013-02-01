package julk.net.deliver;

import java.util.Properties;
import java.io.FileInputStream;

public class MailConfig
{
	private String POP3server;
	private String SMTPserver;
	private int POP3port;
	private int SMTPport;
	private String POP3user;
	private String POP3pass;
	private String ESMTPuser;
	private String ESMTPpass;
	private String email;
	
	public MailConfig ()
	{
		Properties ini = new Properties();
		try {
			ini.load(new FileInputStream("mail.cfg"));
		} catch (Exception e) {}
		POP3server = ini.getProperty("POP3server","pop3.terra.es");
		try {
			POP3port = Integer.parseInt(ini.getProperty("POP3port","110"));
		} catch (Exception e) {
			POP3port = 110;
		}
		SMTPserver = ini.getProperty("SMTPserver","mailhost.terra.es");
		try {
			SMTPport = Integer.parseInt(ini.getProperty("SMTPport","25"));
		} catch (Exception e) {
			SMTPport = 25;
		}
		POP3user = ini.getProperty("POP3user","robomail.terra.es");
		POP3pass = ini.getProperty("POP3pass","botmail");
		
		ESMTPuser = ini.getProperty("ESMTPuser","b2FrNDc5Yw==");
		ESMTPpass = ini.getProperty("ESMTPpass","MTE5MTdh");
		
		email = ini.getProperty("EMAIL","robomail@terra.es");
	}
	
	public String getPOP3server()
	{
		return POP3server;
	}
	
	public String getSMTPserver()
	{
		return SMTPserver;
	}
	
	public int getPOP3port()
	{
		return POP3port;
	}
	
	public int getSMTPport()
	{
		return SMTPport;
	}
	
	public String getPOP3user()
	{
		return POP3user;
	}
	
	public String getPOP3pass()
	{
		return POP3pass;
	}
	
	public String getESMTPuser()
	{
		return ESMTPuser;
	}
	
	public String getESMTPpass()
	{
		return ESMTPpass;
	}
	
	public String getEMail()
	{
		return email;
	}
}
