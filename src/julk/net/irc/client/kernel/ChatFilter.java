package julk.net.irc.client.kernel;

import java.util.*;
import java.io.PrintWriter;

public abstract class ChatFilter implements Runnable
{
	private Vector<String> resPool;
	//private Hashtable resPool;
	//private int initPool, endPool;
	protected Thread t;
	protected ChatManager cman;
	protected ChatClient chat;
	
	public ChatFilter()
	{
		resPool = new Vector<String>();
		//resPool = new Hashtable();
		//initPool = -1;
		//endPool = -1;
		t = new Thread(this);
		t.start();
	}
	
	public abstract String getName();	
	
	public void setChatManager(ChatManager _cman)
	{
		cman = _cman;
		chat = _cman.getChat();
	}
	
	public void submit(String res)
	{
		//endPool++;
		//resPool.put(new Integer(endPool), res);
		resPool.addElement(res);
	}

	private String nextRes()
	{
		//while (endPool == initPool) {
		resPool.trimToSize();
		while (resPool.size() == 0) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				System.out.println("ChatFilter: Interrupted Thread");
			}
		}
		//initPool++;
		//String res = (String) resPool.get(new Integer(initPool));
		//resPool.remove(new Integer(initPool));
		String res = (String) resPool.elementAt(0);
		resPool.removeElementAt(0);
		return res;
	}

	public void run()
	{
		String res;
		
		while (true) {
			try {
				res = nextRes();
				parse(res);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace(new PrintWriter(System.out));
			}
		}
	}
	
	protected String[] split(String res)
	{
		StringTokenizer st = new StringTokenizer(res);
		String[] part = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
			part[i] = st.nextToken();
		
		return part;
	}
	
	protected String[] splitFrom(String from)
	{
		StringTokenizer st = new StringTokenizer(from,"!,@");
		String[] part = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
			part[i] = st.nextToken();
		
		return part;
	}
	
	protected abstract void parse(String res);
}
