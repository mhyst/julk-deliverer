package julk.net.irc.client.kernel;

public interface ChatClient
{
	public void connect (String host, int port,
						 Parser p,
						 String nick)
		throws Exception;
	public void send(String msg)
		throws Exception;
	public void close();
    public boolean isConnected();
	public String getNick();
}
