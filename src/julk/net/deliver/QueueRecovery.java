package julk.net.deliver;

import julk.net.scheduler.SchedulerProgram;
import java.util.Enumeration;
import java.util.Hashtable;

public class QueueRecovery extends SchedulerProgram
{
	//private Deliverer DELIVERER;
	private Hashtable<String, Queue> queue;
	
	public QueueRecovery ()
	{
	}
	
	public void Init ()
	{
		System.out.println("Iniciando el sistema de recuperaci�n autom�tica de colas remotas...");
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
		boolean again = false;
		
		while (e.hasMoreElements()) {
			q = (Queue) e.nextElement();
			if (!q.isOnline()) {
				if (!q.connect())
					again = true;
			}
		}
		
		if (!again) {
			DELIVERER.getScheduler().subProgram(key);
			System.out.println("No hay m�s colas fuera de l�nea, retirado programa de recuperaci�n");
			DELIVERER.getRegistry().unRegister("QueueRecovery");
		}
	}
}
