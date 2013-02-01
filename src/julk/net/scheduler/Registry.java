package julk.net.scheduler;

import java.util.Vector;

/**
 */
public class Registry
{
	private Vector<String> reg;
	
	public Registry ()
	{
		reg = new Vector<String>();
	}
	
	public void clear ()
	{
		reg.removeAllElements();
	}
	
	public void register (String key)
	{
		reg.addElement(key);
	}
	
	public boolean unRegister (String key)
	{
		return reg.removeElement(key);
	}
	
	public boolean isRegistered (String key)
	{
		return reg.contains(key);
	}
}
