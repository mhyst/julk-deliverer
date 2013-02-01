package julk.net.irc.client.kernel;
import java.util.Enumeration;

public interface ChatManager
{
	public void addMsg(String channel, String nick, String msg);
	public void addUser(String channel, String nick);
	public void subUser(String channel, String nick);
	public void changeUser(String oldNick, String newNick);
	public void clearUsers(String channel);
	public void registerChannel(String channel);
	public void removeChannel(String channel);
	public void addNotice(String notice);
	public void send(String msg) throws Exception;
	public void join(String channel);
	public void part(String channel);
	public Enumeration<String> getChannels();
	public ChatClient getChat();
	public Parser getParser();
}
