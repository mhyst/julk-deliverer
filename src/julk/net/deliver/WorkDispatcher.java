package julk.net.deliver;

public class WorkDispatcher extends Dispatcher
{	
	public WorkDispatcher (Translator tr)
	{
		super(tr);
	}
	
	public void run ()
	{
		WorkItem wi = null;
		while (true) {
			try {
				while (pendingWork == 0)
					Thread.sleep(1000);
				wi = wq.sub();
				
				if (wi != null) {
					translator.doTranslation(wi);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			pendingWork--;
		}
	}
}
