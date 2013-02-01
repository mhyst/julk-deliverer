package julk.net.proxy;

import java.net.*;
import java.util.Enumeration;

/**
 */
public class SocksProxy implements Runnable
{
	private String SERVER;
	private int PORT, PORT_OPEN;
	
	public SocksProxy(String[] args)
	{		
		try {
			SERVER = args[0];
			PORT = Integer.parseInt(args[1]);
			PORT_OPEN = Integer.parseInt(args[2]);
		} catch (Exception e) {
			SERVER = "correo.jet.es";
			PORT = 25;
			PORT_OPEN = 25;
		}
			fout = new FileOutputStream("julk_net_SocksProxy.log",true);
			log ("No se ha podido abrir o crear el log\r\n");
		lout = new LogOutputStream(System.out,fout);
		ss = new ServerSocket(PORT_OPEN);
		t.start();
	}
	
	public String getServer()
	{
	}
	
	public int getPort()
	{
	}
	
	public int getLocalPort()
	{
	}
	public void closeAll()
		Enumeration<Cliente> e = clientes.elements();
		while (e.hasMoreElements()) {
		}
			ss.close();
		} catch (Exception ex) {}
	
	public void join ()
	{
		try {
	}
		int i = 0;
		Socket s;
		String key;
		
			while (true) {
				s = ss.accept();
			    clientes.put(key,new Cliente(s,key));	
			}
		} catch (Exception e) {
		}
	}
	
	private void log (String msg)
	{
		try {
			lout.write(msg.getBytes());
		} catch (IOException e) {
		}
	}
	
	{
		new SocksProxy(args).join();
	}
	
	private class Cliente implements Runnable
	{
		private Socket src, dest;
		private Thread t;
		
		public Cliente(Socket _s, String _key)
		{
			src = _s;
			t = new Thread(this);
			t.start();
		}
		public void close()
			try {
				src.close();
				dest.close();
			} catch (Exception e) {}
		
		public void run()
		{
			try {
				dest = new Socket(SERVER,PORT);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			OutputStream srcout, destout;
			InputStream srcin, destin;
			
			int data = 0; //reintentos = 0;
			
			try {
				srcin = src.getInputStream();
				destin = dest.getInputStream();
				srcout = src.getOutputStream();
				destout = dest.getOutputStream();
			
				dest.setSoTimeout(10);
				src.setSoTimeout(10);
					try {
						data = destin.read();
						while (destin.available() > 0) {
					} catch (InterruptedIOException iioe) {
					} catch (Exception e) {
					}
						data = srcin.read();
						while (srcin.available() > 0) {
					} catch (InterruptedIOException iioe) {
					} catch (Exception e) {
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				src.close();
				dest.close();
			} catch (Exception e) {
			}
			log("Desconectado cliente " + key + " en " + src.getInetAddress().getHostAddress() + "\r\n");
		}
	}
}