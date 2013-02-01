package julk.net.scheduler;

import java.util.Properties;

public interface Automata
{
	public void start ();
	public void stop ();
	public void suspend ();
	public void resume ();
	public void config ( Properties cfg );
	public Properties config ();
	public Object getObject(String name);
	public void setObject(String name, Object obj);
}
