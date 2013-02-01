package julk.net.deliver;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import julk.io.LogOutputStream;

/**
 */
public class RemoteWorkQueue extends Queue
{
	private String name;
	//private String serviceType;
	private Socket s;
	private InputStream in;
	private OutputStream out;
	private LogOutputStream lout;
	private Deliverer DELIVERER;
	
	public RemoteWorkQueue (String _name, String _serviceType, Deliverer theDeliverer)
		throws Exception
	{
		name = _name;
		serviceType = _serviceType;
		DELIVERER = theDeliverer;
		if (!connect())
			DELIVERER.loadQueueRecovery();
		try {
			lout = new LogOutputStream(System.out,new FileOutputStream("julk_net_deliver_RemoteWorkQueue.log"));
		} catch (IOException ioe) {}
		log("Creada nueva cola remota con distribución al equipo "+name+"\r\n");
	}
	
	public boolean connect()
	{
		try {
			s = new Socket(name,4000);
			in = s.getInputStream();
			out = s.getOutputStream();
			putOnline();
			return true;
		} catch (Exception e) {
			s = null;
			in = null;
			out = null;
			putOffline();
			return false;
		}
	}
	
	public RemoteWorkQueue (String _name, String _serviceType)
		throws Exception
	{
		this(_name, _serviceType, null);
	}
	
	private String getResponse ()
	{
		if (!isOnline())
			return "-ERR cola offline";
		String response = "";
		int c, retry = 0;
		try {
			while (in.available() == 0) {
				if (retry > 300) {
					if (DELIVERER != null) {
						log("Perdido contacto con distribuidor "+name+".\r\n");
						log("Cola offline!\r\n");
						this.putOffline();
						DELIVERER.loadQueueRecovery();
						//DELIVERER.subQueue(name);
					} else {
						log("Perdido contacto con distribuidor "+name+".\r\n");
						log("Proceso abortado!\r\n");
						System.exit(1);
					}
					throw new Exception("Tiempo de espera expirado");
				}
				Thread.sleep(100);
				retry++;
			}
			while ((c = in.read()) != -1) {
				if (c == 13) {
					c = in.read();
					break;
				}
				response += (char) c;
			}
		} catch (Exception e) {
			response = "-ERR error de comunicación";
		}
		return response;
	}
		
	private void sendRequest (String cmd)
	{
		if (!isOnline())
			return;
		if (cmd.length() == 0)
			return;
		try {
			out.write ((cmd + "\r\n").getBytes());
		} catch (Exception e) {
		}
	}

	public synchronized boolean add(WorkItem wi)
	{
		WorkResult wr = wi.getWorkResult();
		if (wr == null) {
			sendRequest("add("+wi.getUser()+","+wi.getService()+","+wi.getCommand()+")");
		} else {
			sendRequest("add("+wi.getUser()+","+wi.getService()+","+wi.getCommand()+","+wr.getName()+","+wr.size()+")");
			try {
				wr.send(out);
			} catch (Exception e) {
				log("Trabajo "+wi.getId()+" no aceptado en la cola remota "+name+"\r\n");
				return false;
			}
		}
		if (getResponse().startsWith("-ERR")) {
			log("Trabajo "+wi.getId()+" no aceptado en la cola remota "+name+"\r\n");
			return false;
		} else {
			try {
				this.setChanged();
				this.notifyObservers();
			} catch (Exception e) {}
			log("Trabajo "+wi.getId()+" sometido en la cola remota "+name+"\r\n");
			return true;
		}
	}
	
	public synchronized WorkItem sub ()
	{
		return null;
	}
		
	public int size ()
	{
		String res = "";
		sendRequest("size");
		if ((res = getResponse()).startsWith("-ERR"))
			return 5000;
		else {
			int pos = res.indexOf(" ");
			if (pos == -1)
				return 5000;
			res = res.substring(pos+1);
			int pos_f = res.indexOf(" ");			
			return Integer.parseInt(res.substring(0,pos_f));
		}
	}
	
	public boolean accepts (String service)
	{
		sendRequest("accepts("+service+")");
		String res = getResponse();
		if (res.startsWith("-ERR"))
			return false;
		else
			return true;
	}
	
	public boolean noop ()
	{
		sendRequest("noop");
		String res = getResponse();		
		if (res.startsWith("-ERR"))
			return false;
		else
			return true;
	}
	
	private void log (String msg)
	{
		try {
			lout.write(msg.getBytes());
		} catch (IOException e) {
		}
	}	
}
