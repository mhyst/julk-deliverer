package julk.net.irc.client.kernel;

import java.util.*;

public class ChatFilterManager
{
	private Hashtable<String, ChatFilter> filters;
	private ChatManager cman;
	
	public ChatFilterManager(ChatManager _cman)
	{
		cman = _cman;
		filters = new Hashtable<String, ChatFilter>();
	}
	
	public void addFilter(ChatFilter cf)
	{
		//System.out.println("Chatfilter eliminandose por seguridad..."); 
		filters.remove(cf.getName());
		//System.out.println("Eliminado");
		cf.setChatManager(cman);
		//System.out.println("Establecido chat manager"); 
		filters.put(cf.getName(), cf);
		//System.out.println("Filtro establecido con �xito");
	}
	
	public void applyFilters(String res)
	{
		ChatFilter cf;
		
		Enumeration<ChatFilter> e = filters.elements();
		while (e.hasMoreElements()) {
			cf = (ChatFilter) e.nextElement();
			cf.submit(res);
		}
	}
}
