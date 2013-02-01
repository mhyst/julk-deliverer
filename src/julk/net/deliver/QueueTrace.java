package julk.net.deliver;

import julk.net.scheduler.SchedulerProgram;
import java.util.Enumeration;
import java.util.Hashtable;

public class QueueTrace extends SchedulerProgram
{
	//private Deliverer DELIVERER;
	private Hashtable<String, Queue> queue;
	
	public QueueTrace ()
	{
	}
	
	public void Init()
	{	
		System.out.println("Iniciando el sistema de revisi�n autom�tica de colas remotas...");
	}
	
	protected boolean setDeliverer()
	{
		if (super.setDeliverer()) {
			queue = DELIVERER.getQueueList();
			return true;
		} else {
			return false;
		}
	}
	
	protected final void launch ()
	{
		if (!setDeliverer()) {
			setReady(false);
			return;
		}
		
		Enumeration<Queue> e = queue.elements();
		Queue q;
		
		while (e.hasMoreElements()) {
			q = (Queue) e.nextElement();
			if (!q.noop()) {
				DELIVERER.loadQueueRecovery();
				return;
			}
		}
	}
}
