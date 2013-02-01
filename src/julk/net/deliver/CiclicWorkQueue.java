package julk.net.deliver;

import java.util.Hashtable;
import java.io.FileOutputStream;
import java.io.IOException;
import julk.io.LogOutputStream;

public class CiclicWorkQueue extends Queue
{
	//Contenedor de trabajos
	private Hashtable<String, WorkItem> queue;
	//Apuntador de trabajo
	private int currentlyWorkingOnIndex;
	//Apuntador de inserci�n
	private int lastReservedIndex;
	//Logger
	private LogOutputStream out;
	
	private int retriesLimit;
	
	public CiclicWorkQueue (String _name, String _serviceType)
	{
		this.name = _name;
		this.serviceType = _serviceType;
		this.retriesLimit = 5;
		queue = new Hashtable<String, WorkItem>();
		try {
			out = new LogOutputStream(System.out,new FileOutputStream("julk_net_deliver_CiclicWorkQueue.log"));
		} catch (IOException ioe) {}
		currentlyWorkingOnIndex = -1;
		lastReservedIndex = -1;
		putOnline();
		log("Creada nueva cola c�clica "+name+" para el servicio "+serviceType+"\r\n");
	}
	
	public CiclicWorkQueue (String _name, String _serviceType, int _retriesLimit)
	{
		this (_name, _serviceType);
		retriesLimit = _retriesLimit;
	}
	
	public synchronized boolean add(WorkItem wi)
	{
		lastReservedIndex++;
		String index = "" + lastReservedIndex;
		if (accepts(wi.getService())) {
			queue.put (index,wi);
			this.setChanged();
			this.notifyObservers();
			log("Trabajo "+wi.getId()+" sometido en la cola "+name+"\r\n");
			return true;
		} else {
			log("Trabajo "+wi.getId()+" no aceptado en la cola "+name+"\r\n");
			return false;
		}
	}
	
	public synchronized WorkItem sub ()
	{
		if (currentlyWorkingOnIndex == lastReservedIndex)
			return null;
		
		currentlyWorkingOnIndex++;
		String index = "" + currentlyWorkingOnIndex;
		WorkItem wi = (WorkItem) queue.remove(index);
		log("Trabajo "+wi.getId()+" pasa al subsistema de procesamiento\r\n");
		return wi;
	}
	
	public int size ()
	{
		return queue.size ();
	}
	
	public boolean accepts (String service)
	{
		if (serviceType.equalsIgnoreCase(service))
			return true;
		else
			return false;
	}
	
	private void log (String msg)
	{
		try {
			out.write(msg.getBytes());
		} catch (IOException e) {
		}
	}
	
	public int getRetriesLimit ()
	{
		return retriesLimit;
	}
}

