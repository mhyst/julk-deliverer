package julk.net.irc.client;

import java.net.*;
import java.io.*;
import java.util.*;
import julk.net.irc.client.kernel.*;

public class Chatter56Client implements Runnable,  ChatClient
{
	//private Client cli;
	private Thread t;
	private Socket s;
	private InputStream in;
	private OutputStream out;	
	private Parser p;
	private String nick;
	private OutQueue oq;
	private boolean connectedFlag;
	
	public Chatter56Client()
	{
		connectedFlag = false;
		oq = new OutQueue();
	}
	
	public Thread getThread()
	{
		return t;
	}
	
	public void connect (String host, int port,
						 Parser _p,
						 String _nick)
		throws Exception
	{
		nick = _nick;
		p = _p;
		
		//cli = new Client("212.106.193.75", 8620);
		s = new Socket(host, port);
		System.out.println("Conectando a: "+host+":"+port);
		in = s.getInputStream();
		out = s.getOutputStream();
		t = new Thread(this);
		t.start();
		//send("NICK " + _nick + 
		// 	 "\r\nUSER web" + _nick.toLowerCase() + " xxxx xxxx : " + _nick);
		send("NICK " + _nick + 
		 	 "\r\nUSER web" + _nick.toLowerCase() + " xxxx xxxx : " + _nick);		
		//send("CADCON 86734893948\r\nNICK " + _nick + 
		// 	 "\r\nUSER " + _nick.toLowerCase() + " xxxx xxxx : " + _nick);				//send("_CADCON " + "qiiqjaryyr\r\n");
		//send("CADCON kigomfpoqk\r\nNICK " + _nick + 
		// 	 "\r\nUSER web" + _nick.toLowerCase() + " xxxx xxxx : " + _nick);
		connectedFlag = true;
		//t = new Thread(this);
		//t.start();

	}			

	private String procesa2(String res) {
		return res;
	}
	
	private synchronized void realSend(String req)
		throws Exception
	{
		if (req.charAt(0) == '*') {
			String what = req.substring(1);
			if (what.equals("flush")) {
				out.flush();
			} else if (what.equals("wait")) {
				Thread.sleep(2000);
			}
		} else {
			System.out.println("IRC<--"+req);
			req = procesa2(req);
			out.write((req + "\r\n").getBytes());
		}
	}
	
	public void send(String req)
	{
		oq.submit(req);
	}
	
	public void close()
	{
		if (!isConnected())
			return;
		try {
			connectedFlag = false;
			realSend("QUIT :Saliendo");
			in.close();
			out.flush();
			out.close();
			s.close();
		} catch (Exception e) {
			System.out.println("Error en Chatter56Client.close");
		}
	}
	
	public boolean isConnected()
	{
		return connectedFlag;
	}
	
	public void run()
	{
		int c = 0;
		String res;

		try {
			while (true) {			
				res = "";
				while (in.available() == 0)
					Thread.sleep(100);
				c = in.read();
				while (c!=10) {
					res += (char) c;
					c = in.read();
				}
				//res = res.substring(0, res.length()-1);
				if (res.startsWith("PING")) {
					System.out.println("IRC-->"+res);
					int pos = res.indexOf(":");
					if (pos == -1) {
						realSend("PONG :dolly");
					} else {
						realSend("PONG :"+res.substring(pos+1));
					}
				} else {
					//res = procesa2(res);
					//System.out.println("Chatter56Client: Hasta el parse llega.");
					p.parse(res);
					//System.out.println("Chatter56Client: Hasta despues del parser llega.");
				}
			}
		} catch (Exception e) {
			System.out.println("Error en la recepcion de datos");
			e.printStackTrace();
			System.out.println(e.getMessage());
			close();
		}
	}
	
	public String getNick()
	{
		return nick;
	}
		
	public static void main (String[] args)
		throws Exception
	{
	}
	
	private class OutQueue implements Runnable
	{
		private Vector<String> outPool;
		//private Hashtable outPool;
		//private int initPool, endPool;
		protected Thread t;
		
		public OutQueue()
		{
			outPool = new Vector<String>();
			//outPool = new Hashtable();
			//initPool = -1;
			//endPool = -1;
			t = new Thread(this);
			t.start();
		}

		public void submit(String req)
		{
			//endPool++;
			//outPool.put(new Integer(endPool), req);
			outPool.addElement(req);
		}
		
		private String nextReq()
		{
			outPool.trimToSize();
			while (outPool.size() == 0) {
			//while (endPool == initPool) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					System.out.println("Chatter56Client: Interrupted Thread");
				}
			}	
			//initPool++;
			//String req = (String) outPool.get(new Integer(initPool));
			//outPool.remove(new Integer(initPool));
			String req = (String) outPool.elementAt(0);
			outPool.removeElementAt(0);
			return req;
		}

		@SuppressWarnings("unused")
		public boolean hayTrabajos()
		{
			//return !(endPool == initPool);
			outPool.trimToSize();
			return outPool.size() != 0;
		}
		public void run()
		{
			String req;
				
			while (true) {
				try {
					req = nextReq();
					realSend(req);
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace(new PrintWriter(System.out));
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
