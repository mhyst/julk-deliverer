package julk.net.proxy;
import java.io.*;
import java.net.*;import java.util.Hashtable;
import java.util.Enumeration;import julk.io.LogOutputStream;

/**
 */
public class SocksProxy implements Runnable
{
	private String SERVER;
	private int PORT, PORT_OPEN;	private Hashtable<String, Cliente> clientes;	private Thread t;	private LogOutputStream lout;	private ServerSocket ss;
	
	public SocksProxy(String[] args)		throws IOException, UnknownHostException
	{		
		try {
			SERVER = args[0];
			PORT = Integer.parseInt(args[1]);
			PORT_OPEN = Integer.parseInt(args[2]);
		} catch (Exception e) {
			SERVER = "correo.jet.es";
			PORT = 25;
			PORT_OPEN = 25;
		}				clientes = new Hashtable<String, Cliente>();		FileOutputStream fout = null;		try {
			fout = new FileOutputStream("julk_net_SocksProxy.log",true);		} catch (IOException e) {
			log ("No se ha podido abrir o crear el log\r\n");		}
		lout = new LogOutputStream(System.out,fout);
		ss = new ServerSocket(PORT_OPEN);		//Socket s = new Socket(SERVER,PORT);		log("PROXY-BRIDGE apuntando a " + SERVER + ":" + PORT + "\r\n");		log("Escuchando por el puerto " + PORT_OPEN + "\r\n");		t = new Thread(this);
		t.start();
	}
	
	public String getServer()
	{		return SERVER;
	}
	
	public int getPort()
	{		return PORT;
	}
	
	public int getLocalPort()
	{		return PORT_OPEN;
	}		@SuppressWarnings("deprecation")
	public void closeAll()	{		t.stop();	
		Enumeration<Cliente> e = clientes.elements();
		while (e.hasMoreElements()) {			((Cliente) e.nextElement()).close();
		}		try {
			ss.close();
		} catch (Exception ex) {}	}
	
	public void join ()
	{
		try {			t.join();		} catch (InterruptedException e) {		}
	}		public void run ()	{
		int i = 0;
		Socket s;
		String key;
				try {
			while (true) {
				s = ss.accept();				key = "" + i;
			    clientes.put(key,new Cliente(s,key));					i++;
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
		public static void main(String[] args)		throws IOException, UnknownHostException
	{
		new SocksProxy(args).join();
	}
	
	private class Cliente implements Runnable
	{
		private Socket src, dest;
		private Thread t;		private String key;
		
		public Cliente(Socket _s, String _key)
		{			key = _key;
			src = _s;			log ("Conectado cliente " + key + " desde " + src.getInetAddress().getHostAddress() + ":" + src.getPort() + "\r\n");
			t = new Thread(this);
			t.start();
		}				@SuppressWarnings("deprecation")
		public void close()		{
			try {				t.stop();
				src.close();
				dest.close();
			} catch (Exception e) {}		}
		
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
				src.setSoTimeout(10);				while (true) {
					try {
						data = destin.read();
						while (destin.available() > 0) {							srcout.write(data);							data = destin.read();						}						if (data == -1)							break;						else							srcout.write(data);
					} catch (InterruptedIOException iioe) {
					} catch (Exception e) {						break;
					}					try {
						data = srcin.read();
						while (srcin.available() > 0) {							destout.write(data);							data = srcin.read();						}						if (data == -1)							break;						else							destout.write(data);
					} catch (InterruptedIOException iioe) {
					} catch (Exception e) {						break;
					}				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				src.close();
				dest.close();
			} catch (Exception e) {
			}
			log("Desconectado cliente " + key + " en " + src.getInetAddress().getHostAddress() + "\r\n");			clientes.remove(key);
		}
	}
}