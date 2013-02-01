package julk.net.irc.bot;

import java.util.*;
import java.io.FileInputStream;
import java.io.PrintWriter;
//import delphos.utils.*;
import julk.net.irc.client.kernel.*;
import julk.net.irc.client.*;
import julk.net.scheduler.*;


public class spIRCbot extends SchedulerProgram implements Parser, ChatManager
{
	//Objetos del kernel
	private Hashtable<String,String> channels;
	private ChatClient chat;
	private Thread chatthread;
	private String nick;
	private spIRCbot me;
	private ChatFilterManager cfm;
	//private Deliverer DELIVERER;
	private boolean connected;
	
	private String IRCnick;
	private String IRCserver;
	private int IRCport;
	private boolean IRCwithDCC;
	
	public void launch()
	{
		System.out.println("Lanzando IRCbot...");
		if (!setDeliverer()) {
			setReady(false);
			System.out.println("IRCbot: no recibo referencia a DELIVERER");
			return;
		}		
			
		me = this;
		channels = new Hashtable<String, String>();
		if (!connected) {
			System.out.println("IRCbot no est� conectado");
		} else {
			try {
				Thread.sleep(10000);
			} catch (Exception e) {}
			while (chatthread.isAlive()) {
			//while(true) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
				}
			}
		}
		System.out.println("IRCbot ha terminado.");
	}
	
		
	public void Init()
	{
		IRCconfig();
		me = this;
		System.out.println("Inicializando IRCbot...");
		if (!setDeliverer()) {
			setReady(false);
			System.out.println("spIRCbot: Efectivamente, no llega el deliverer");
			return;
		}				
		//System.out.println("IRCbot: Deliverer recuperado.");
		
		chat = new Chatter56Client();
			
		
			 
			//nick = "Chinchon";
			
			
			//chinchon = new Chinchon();
			//cfm.addFilter(chinchon);
		//nick = "Deliverer2";
		
		try {
			//chat.connect("212.106.193.60",8820,me,nick);
			//Server local
			chat.connect(IRCserver,IRCport,me,IRCnick);
			//Usando bouncer de ModMa
			//chat.connect("mipc.zapto.org",3550,me,nick);
			//chat.connect("212.106.193.114",8820,me,nick);
			//chat.connect("62.151.2.14",8820,me,nick);
			//chat.connect("62.151.26.2",8670,me,nick);
			connected = true;
			IRCbot bot = new IRCbot(this, IRCwithDCC);
			//bot.setDeliverer(DELIVERER);
			//System.out.println("IRCbot: Deliverer enviado al IRCbot interno.");
			cfm = new ChatFilterManager(me);
			cfm.addFilter(bot);
			bot.setDeliverer(DELIVERER);
			//System.out.println("Filtros IRCbot cargados.");
			chatthread = ((Chatter56Client)chat).getThread();
		} catch (Exception e) {
			connected = false;
			System.out.println("Aqui\r\n"+e.getMessage());
			//e.printStackTrace(new PrintWriter(System.out));
		}
	}
	
	private void IRCconfig()
	{
		Properties ini = new Properties();
		try {
			ini.load(new FileInputStream("irc"+key+".cfg"));
		} catch (Exception e) {}
		System.out.println("XXXXXX irc"+super.key+".cfg XXXXXXX");
		IRCnick = ini.getProperty("IRCnick","deliverer"+Calendar.getInstance().get(Calendar.MILLISECOND));
		IRCserver = ini.getProperty("IRCserver","mipc.zapto.org");
		try {
			IRCport = Integer.parseInt(ini.getProperty("IRCport","6667"));
		} catch (Exception e) {
			IRCport = 6667;
		}
		IRCwithDCC = ini.getProperty("IRCwithDCC","N").equalsIgnoreCase("S") ? true : false;
	}

	        //Runs on the event-dispatching thread.
	public void finished() {
		if (connected) {
			closeConnection();
		}
	}
	
	private void closeConnection()
	{
		if (chat != null) {
			chat.close();
		}
	}

	
	public synchronized void parse(String res)
	{
		if (res == null) {
			return;
		}
		System.out.println("IRC-->"+res);
		
		if (res.charAt(0) == ':') {
			StringTokenizer st = new StringTokenizer(res);
			String[] part = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				part[i] = st.nextToken();
			if (part[1].equals("JOIN")) {
				int pos = part[0].indexOf("!");
				if (part[0].substring(1,pos).equalsIgnoreCase(nick)) {
					clearUsers(part[2].substring(1));
				} else {
					addUser(part[2].substring(1), part[0].substring(1,pos));
				}
			} else if (part[1].equals("PART")) {
				int pos = part[0].indexOf("!");
				subUser(part[2], part[0].substring(1,pos));
			} else if (part[1].equals("QUIT")) {
				int pos = part[0].indexOf("!");
				Enumeration<String> e = channels.keys();
				String c;
				while (e.hasMoreElements()) {
					c = (String) e.nextElement();
					subUser(c, part[0].substring(1,pos));
				}
			} else if (part[1].equals("NICK")) {
				int pos = part[0].indexOf("!");
				changeUser(part[0].substring(1,pos),part[2].substring(1));
				if (nick != null) {
					if (nick.equalsIgnoreCase(part[0].substring(1,pos)))
						nick = part[2].substring(1);
				}
			} else if (part[1].equals("353")) {
				String s = part[5].substring(1);
				if (s.charAt(0) == '@')
					addUser(part[4], s.substring(1));
				else
					addUser(part[4], s);
				for (int i = 6; i < part.length; i++) {
					addUser(part[4], part[i]);
				}
			} else if (part[1].equals("376")) {
				try {
					chat.send("LIST");
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace(new PrintWriter(System.out));
				}
			} else if (part[1].equals("PRIVMSG")) {
				int pos = part[0].indexOf("!");
				String msg;
				msg = part[3].substring(1);
				for (int i = 4; i < part.length; i++)
					msg += (" "+part[i]);
				addMsg(part[2], part[0].substring(1,pos), msg);
			} else if (part[1].equals("322")) {
				registerChannel(part[3]);
			/*} else if (part[1].equals("NOTICE")) {
				addNotice(res);*/
			}
			try {
				cfm.applyFilters(res);
			} catch (Exception e) {
				System.out.println("spIRCbot: Erro al intentar ejecutar los filtros");
			}
		}
	}
	
	public synchronized void addMsg(String channel, String nick, String msg)
	{
		//A este m�todo le llegan notificaciones de cuando se ha recibido un mensaje en el canal que sea
	}
	
	public synchronized void addUser(String channel, String nick)
	{
		//A este m�todo le llegan notificaciones de cuando un usuario ha entrado en un canal
	}
	
	public synchronized void addUser(String nick)
	{
		//Notificaci�n de a�adir un usuario a todos los canales
	}
	
	public synchronized void subUser(String channel, String nick)
	{
		//Quitar un usuario de un canal
	}
	
	public synchronized void changeUser(String oldNick, String newNick)
	{
		//Un usuario ha cambiado de nick
	}
	
	public synchronized void clearUsers(String channel)
	{
		//Limpiar todos los usuarios de un canal
	}
	
	public synchronized void registerChannel(String channel)
	{
		//canal nuevo
	}

	public void removeChannel(String channel)
	{
		channels.remove(channel);
	}

	public void addNotice(String notice)
	{
		//llega un notice
	}
	
	public void send(String msg)
		throws Exception
	{
		chat.send(msg);
	}
	
	public ChatClient getChat()
	{
		return chat;
	}
	
	public void join(String channel)
	{		
		if (channel == null || channel.length() == 0)
			return;
			
		//Notificaci�n de adhesi�n a nuevo canal
	}
		
	public void part(String channel)
	{		
		if (channel == null || channel.length() == 0)
			return;
			
		//Notificaci�n de salida de un canal
	}

	public Enumeration<String> getChannels()
	{
		return channels.keys();
	}
	
	public Parser getParser()
	{
		return this;
	}	
}