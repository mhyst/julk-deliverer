package julk.net.deliver;

public class CiclicWorkDispatcher extends Dispatcher
{	
	public CiclicWorkDispatcher (Translator tr)
	{
		super (tr);
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
					if (!translator.doTranslation (wi)) {
						if (wi.getRetries () < ((CiclicWorkQueue) wq).getRetriesLimit()) {
							wi.addRetry();
							wq.add(wi);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			pendingWork--;
		}
	}
}
