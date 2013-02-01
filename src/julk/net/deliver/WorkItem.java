package julk.net.deliver;

import java.util.Calendar;

public class WorkItem
{
	private String id;
	private String userEmail;
	private String serviceType;
	private String commandLine;
	private WorkResult wr;
	private int reintentos;
	
	public WorkItem (String user, String service, String cmd)
	{
		userEmail = user;
		serviceType = service;
		commandLine = cmd;
		Calendar c = Calendar.getInstance();
		id = "wid-"+c.get(Calendar.YEAR)+
			 c.get(Calendar.MONTH)+
			 c.get(Calendar.DAY_OF_MONTH)+
			 c.get(Calendar.HOUR_OF_DAY)+
			 c.get(Calendar.MINUTE)+
			 c.get(Calendar.SECOND)+
			 c.get(Calendar.MILLISECOND)+"-"+service+"-"+user;
		reintentos = 0;
	}
	
	public WorkItem (String user, String service, String cmd, WorkResult _wr)
	{
		this (user,service,cmd);
		wr = _wr;
	}
	
	public String getId ()
	{
		return id;
	}
			
	public String getUser ()
	{
		return userEmail;
	}
	
	public String getService ()
	{
		return serviceType;
	}
			
	public String getCommand ()
	{
		return commandLine;
	}
	
	public WorkResult getWorkResult ()
	{
		return wr;
	}
	
	public int getRetries ()
	{
		return reintentos;
	}
	
	public void addRetry ()
	{
		reintentos++;
	}
}
