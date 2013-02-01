package julk.net.irc.client.kernel;

public interface ChatViewer
{
	public void addMsg(String nick, String msg);
	public void addUser(String nick);
	public void subUser(String nick, String newNick, boolean change);
	public void clearUsers();
}
