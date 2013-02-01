package julk.net.deliver;

import java.util.Observer;
import java.util.Observable;

public abstract class Dispatcher implements Observer, Runnable
{
	protected Queue wq;
	protected int pendingWork;
	protected Translator translator;
	protected Thread t;
	
	public Dispatcher (Translator tr)
	{
		translator = tr;
		pendingWork = 0;
		wq = null;
	}

	public void update (Observable o, Object arg)
	{
		if (wq == null)
			wq = (Queue) o;

		pendingWork++;
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}
	
	public abstract void run ();
}
