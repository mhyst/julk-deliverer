package julk.net.irc.bot;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import julk.net.deliver.Deliverer;
import julk.net.deliver.WorkItem;
import julk.net.irc.client.kernel.*;
import julk.strings.StringFacility;
import julk.net.scheduler.SchedulerProgram;;

public class IRCbot extends ChatFilter
{	
	private Deliverer DELIVERER;
	private DelivererClient dc;
	private DCCTransfer dcc;
	//private SchedulerProgram schp;
	
	private String owner;
	private Vector<String> users;
	
	private boolean withDCC;
	
	public IRCbot(SchedulerProgram _schp, boolean _dcc)
	{
		super();
		DELIVERER = null;
		owner="";
		users = new Vector<String>();
		loadUsers();
		//schp = _schp;
		withDCC = _dcc;
	}
	
	public void clearUsers()
	{
		users.clear();
	}
	
	public boolean loadUsers(String filename)
	{
		clearUsers();
		BufferedReader cfg = null;
		try {
			cfg = new BufferedReader(new FileReader(filename));
		} catch (Exception fnfe) {
			System.out.println("El archivo de usuarios no existe - "+filename+"\r\n");
			return false;
		}
		
		String line;
		try {
			while ((line = cfg.readLine()) != null && line.length() > 0) {
				addUser(line);				
			}
			cfg.close();
		} catch (Exception e) {
			System.out.println("Error al leer el archivo de configuraci�n - "+filename+"\r\n");
			return false;
		}
		return true;
	}
	
	public boolean loadUsers()
	{
		return loadUsers("ircusers.cfg");
	}
	
	public boolean saveUsers (String filename)
	{
		PrintWriter fQ;
		
		try {
			fQ = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		} catch (IOException ioe) {
			return false;
		}
		
		users.trimToSize();
		String user;
		
		Enumeration<String> e = users.elements();
		while (e.hasMoreElements()) {
			user = (String) e.nextElement();
			fQ.println(user);
		}
		fQ.close();
		return true;
	}
	
	public boolean saveUsers()
	{
		return saveUsers("ircusers.cfg");
	}
	
	public void setOwner(String _owner)
	{
		owner = _owner;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public boolean isOwner(String _user)
	{
		return owner.equalsIgnoreCase(_user) ? true : false;
	}
	
	public void addUser(String _user)
	{
		users.addElement(_user);
	}
	
	public void subUser(String _user)
	{
		users.removeElement(_user);
	}
	
	public boolean isUser(String _user)
	{
		return users.contains(_user) ? true : false;
	}
	
	public DCCTransfer getDCCTransfer() {
		return dcc;
	}
	
	public String getName()
	{
		return "IRCbot";
	}
	
	public void parse(String res)
	{
		if (res.charAt(0) == ':') {
			String[] part = split(res.substring(1));
			
			if (part[1].equals("PRIVMSG")) {
				String nick = (splitFrom(part[0]))[0];
				String host = (splitFrom(part[0]))[2];

				//String to = part[2];
				String msg, canal;
				msg = part[3].substring(1);
				for (int i = 4; i < part.length; i++)
					msg += (" "+part[i]);
				if (msg.charAt(0) == '!' && msg.length() > 1) {
					canal = part[2];
					procesaMandato(nick, host, canal, msg.substring(1));
				} else {
					canal = part[2];
					if (!canal.startsWith("#")) {
						someterTrabajo(nick, host, canal, msg);
					}
				}
			/*} else if (part[1].equals("PART")) {
				String canal = part[2];
			} else if (part[1].equals("QUIT")) {*/
			}
		}
	}
	
	public void setDeliverer(Deliverer theDeliverer)
	{
		DELIVERER = theDeliverer;
		try {
			dc = new DelivererClient(DELIVERER,"IRCdeliverer",this);
			if (withDCC) {
				dcc = new DCCTransfer(chat);
				dcc.start();
			}
		} catch (Exception e) {
			System.out.println("Error gordo al crear objeto DelivererClient");
			System.out.println(e.getMessage());
		}
	}
			
	public void send(String msg)
	{
		try {
			chat.send(msg);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(new PrintWriter(System.out));
			System.out.println("Error al enviar datos al chat");
		}
	}
	
	private void someterTrabajo(String nick, String host,
							    String canal,
							    String msg)
	{
		if (DELIVERER == null) {
			System.out.println("IRCbot (interno): No recibida referencia a DELIVERER, no puedo someter trabajos");
			send("PRIVMSG "+nick+" :DELIVERER: No he recibido referencia a DELIVERER, no puedo someter trabajos");
			return;
		}
		int pos = msg.indexOf("#");
		if (pos == -1) {
			//Habr� que decirle que el comando no se reconoce
			send("PRIVMSG "+nick+" :DELIVERER: Mandato mal formado");
			return;
		}
		String service = msg.substring(0,pos);
		WorkItem wi = new WorkItem(nick+"_"+host,service,msg);		
		if (DELIVERER.add(wi)) {
			System.out.println("Trabajo sometido: "+msg);
			send("PRIVMSG "+nick+" :DELIVERER: Trabajo sometido: "+msg);
		} else {
			send("PRIVMSG "+nick+" :DELIVERER: Ninguna cola acepta el trabajo. Lo siento.");
		}	
	}
	
	private void procesaMandato(String nick, String host,
								String canal,
								String msg)
	{
		/*int pos = msg.indexOf("#");
		if (pos == -1) {
			//Habr� que decirle que el comando no se reconoce
			return;
		}*/
		//String service = msg.substring(0,pos);
		/*pos = msg.toLowerCase().indexOf("chat#dccsend");
		if (pos == -1)
			msg += "#chat#dccsend";*/
		//WorkItem wi = new WorkItem(nick,service,msg);
		if (DELIVERER == null) {
			System.out.println("IRCbot (interno): No recibida referencia a DELIVERER, no puedo someter trabajos");
			send("PRIVMSG "+nick+" :DELIVERER: No he recibido referencia a DELIVERER, no puedo someter trabajos");
			return;
		}
		String entrada = dc.parse(nick,host,canal,msg);
		String salida = "";
		try {
			byte[] bin = entrada.getBytes();
			//byte[] bout = new byte[bin.length];
			//for (int k=0; k<bout.length;k++) bout[k]=0;
			//int j=0;
			for(int i = 0; i < bin.length; i++) {
				//bout[j] = ((bin[i] == '\r' || bin[i] == '\n') ? (byte)' ' : bin[i]);
				salida+=((bin[i] == '\r' || bin[i] == '\n') ? ' ' : (char)bin[i]);
				//j++;
				if(bin[i] == '\n') {
					//salida = new String(bout);
					send("PRIVMSG "+nick+" :"+salida);
					//for (int k=0; k<bout.length;k++) bout[k]=0;
					salida="";
					//j=0;
					//Thread.sleep(100);
				}
			}
			//salida = new String(bout);
			if (salida.length()>0)
				send("PRIVMSG "+nick+" :"+salida);
			
			send("PRIVMSG "+nick+" :*** End of line ***");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		//System.out.println(salida);
		

		/*if (DELIVERER.add(wi)) {
			System.out.println("Trabajo sometido: "+msg);
			send("PRIVMSG "+nick+" :DELIVERER: Trabajo sometido: "+msg);
		} else {
			send("PRIVMSG "+nick+" :DELIVERER: Mandato no reconocido");
		}*/
	}
	
	public void finalize()
	{
		saveUsers();
	}
}

final class DelivererClient
{
	private String id;
	//private boolean admin;
	
	public static String[] COMMANDS = {"ADDQUEUE",
		 					    "ADD",
								"SIZE",
								"SUSPEND",
								"RESUME",
								"STOP",
								"ACCEPTS",
								"SUBQUEUE",
								"QUIT",
								"RELOADQUEUES",
								"ADDCICLICQUEUE",
								"ADDPROGRAM",
								"NOOP",
								"QUEUELIST",
								"CLIENTLIST",
								"KILLCLIENT",
								"PROGRAMLIST",
								"SUBPROGRAM",
								"SETADMIN",
								"CHGPWD",
								"SERVICEMAP",
								"HELP",
								"SETMAPPING",
								"GETMAPPING",
								"REMOVEMAPPING",
								"SAVEMAP",
								"LOADMAP",
								"SAVEQUEUES",
								"LOADQUEUES",
								"CLEARQUEUES",
								"RELOADMAP",
								"CLEARMAP",
								"RELOADPROGRAMS",
								"LOADPROGRAMS",
								"SAVEPROGRAMS",
								"CLEARPROGRAMS",
								"OPENPROXY",
								"CLOSEPROXY",
								"CLOSEALLPROXIES",
								"LISTPROXIES",
								"NICK",
								"DCCIP",
								"JOIN",
								"PART",
								"ACT",
								"ADDUSER",
								"SUBUSER",
								"LOADUSERS",
								"SAVEUSERS",
								"CLEARUSERS"
	};
	public static final int ADDQUEUE = 0;
	public static final int ADD = 1;
	public static final int SIZE = 2;
	public static final int SUSPEND = 3;
	public static final int RESUME = 4;
	public static final int STOP = 5;
	public static final int ACCEPTS = 6;
	public static final int SUBQUEUE = 7;
	public static final int QUIT = 8;
	public static final int RELOADQUEUES = 9;
	//public static final int STATUS = 10;
	public static final int ADDCICLICQUEUE = 10;
	public static final int ADDPROGRAM = 11;
	public static final int NOOP = 12;
	public static final int QUEUELIST = 13;
	public static final int CLIENTLIST = 14;
	public static final int KILLCLIENT = 15;
	public static final int PROGRAMLIST = 16;
	public static final int SUBPROGRAM = 17;
	public static final int SETADMIN = 18;
	public static final int CHGPWD = 19;
	public static final int SERVICEMAP = 20;
	public static final int HELP = 21;
	public static final int SETMAPPING = 22;
	public static final int GETMAPPING = 23;
	public static final int REMOVEMAPPING = 24;
	public static final int SAVEMAP = 25;
	public static final int LOADMAP = 26;
	public static final int SAVEQUEUES = 27;
	public static final int LOADQUEUES = 28;
	public static final int CLEARQUEUES = 29;
	public static final int RELOADMAP = 30;
	public static final int CLEARMAP = 31;
	public static final int RELOADPROGRAMS = 32;
	public static final int LOADPROGRAMS = 33;
	public static final int SAVEPROGRAMS = 34;
	public static final int CLEARPROGRAMS = 35;
	public static final int OPENPROXY = 36;
	public static final int CLOSEPROXY = 37;
	public static final int CLOSEALLPROXIES = 38;
	public static final int LISTPROXIES = 39;
	public static final int NICK = 40;
	public static final int DCCIP = 41;
	public static final int JOIN = 42;
	public static final int PART = 43;
	public static final int ACT = 44; 
	public static final int ADDUSER = 45;
	public static final int SUBUSER = 46;
	public static final int LOADUSERS = 47;
	public static final int SAVEUSERS = 48;
	public static final int CLEARUSERS = 49;
	
	private Deliverer DELIVERER;
	private String password = "frodo";
	private IRCbot ircb;
	
	public DelivererClient (Deliverer theDeliverer, String id, IRCbot _ircb)
	{
		DELIVERER = theDeliverer;
		this.id = id;
		//admin = false;
		ircb = _ircb;
	}	
	
	private boolean isAdmin(String _nick)
	{
		return ircb.isOwner(_nick);
	}
	
	public String getId ()
	{
		return id;
	}
		
	private static int getIdxCommand (String cmd)
	{
		int i;
		for (i = 0; i < COMMANDS.length && !cmd.equalsIgnoreCase(COMMANDS[i]); i++);
		if (i < COMMANDS.length)
			return i;
		else
			return -1;
	}
	
	public String parse (String nick, String host, String canal, String cmd)
	{
		boolean admin = isAdmin(nick);
		String[] partes;
		StringTokenizer st;
		st = new StringTokenizer(cmd,"(,)");
		partes = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
			partes[i] = st.nextToken();

		try {
			switch (getIdxCommand (partes[0].toUpperCase())) {
			case ADDQUEUE:
				if (admin) {
					if (DELIVERER.addQueue(partes[1],partes[2],partes[3].equals("R") ? true : false))
						return "+OK cola "+partes[1]+" a�adida al Deliverer";
					else
						return "-ERR no se pudo a�adir la cola";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case SUBQUEUE:
				if (admin) {
					if (DELIVERER.subQueue(partes[1]))
						return "+OK cola "+partes[1]+" eliminada del Deliverer";
					else
						return "-ERR la cola "+partes[1]+" no existe en este Deliverer";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}	
			case ADD:				
				WorkItem wi;
				if (partes.length > 4) {
					//WorkResult wr = new WorkResult(partes[4]);
					//wr.receive(in,Integer.parseInt(partes[5]));
					//wi = new WorkItem(partes[1],partes[2],partes[3],wr);
					wi = new WorkItem(partes[1],partes[2],partes[3]);
					if (DELIVERER.add(wi))
						return "+OK trabajo sometido";
					else
						return "-ERR nadie acept� el trabajo";
				} else {
					wi = new WorkItem(partes[1],partes[2],partes[3]);
					if (DELIVERER.add(wi))
						return "+OK trabajo sometido";
					else
						return "-ERR nadie acept� el trabajo";
				}
			case SIZE:
				return "+OK "+DELIVERER.size()+" works in queue";
			case STOP:
				if (admin) {
					DELIVERER.stop();
					return "+OK parado";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case ACCEPTS:
				if (DELIVERER.accepts(partes[1]))
					return "+OK esta cola acepta ese servicio";
				else
					return "-ERR esta cola no accepta ese servicio";
			case QUIT:
				return "+OK hasta otra";
			case RELOADQUEUES:
				if (admin) {
					DELIVERER.reloadQueues();
					return "+OK reiniciando la configuraci�n";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case ADDCICLICQUEUE:
				if (admin) {
					if (DELIVERER.addCiclicQueue(partes[1],partes[2],Integer.parseInt(partes[3])))
						return "+OK cola c�clica "+partes[1]+" a�adida al Deliverer";
					else
						return "-ERR no se pudo a�adir la cola c�clica";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case ADDPROGRAM:
				if (admin) {
					if (DELIVERER.getScheduler().addProgram(partes[1],partes[2],Integer.parseInt(partes[3])))
						return "+OK programada nueva tarea "+partes[1]+" en el scheduler";
					else
						return "-ERR la clase "+partes[1]+" no est� registrada para ser programada";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case NOOP:
				return "+OK estoy aqu�";
			case QUEUELIST:
				return DELIVERER.listQueues();
			case CLIENTLIST:
				return DELIVERER.listClients();
			case KILLCLIENT:
				if (admin) {
					if (DELIVERER.killClient(partes[1]))
						return "+OK cliente "+partes[1]+" expulsado";
					else
						return "-ERR cliente no existe";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case PROGRAMLIST:
				//return listSchedulerPrograms();
				return DELIVERER.getScheduler().listPrograms();
			case SUBPROGRAM:
				if (admin) {
					if (DELIVERER.getScheduler().subProgram(partes[1]))
						return "+OK programa "+partes[1]+" retirado del scheduler";
					else
						return "-ERR ese programa no existe";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case SETADMIN:
				if (partes[1].equalsIgnoreCase(password)) {
					ircb.setOwner(nick);
					return "+OK administrator flag = true";	
				} else {
					//admin = false;
					return "-ERR administrator flag = false";
				}
			case CHGPWD:
				if (admin) {
					password = partes[1];
					return "+OK contrase�a reajustada";
				} else {
					return "-ERR no tiene derecho a cambiar la contrase�a";
				}
			case SERVICEMAP:
				return DELIVERER.getServiceMap().getStringsMap();
			case HELP:
				if (partes.length < 2)
					return DELIVERER.help("");
				else {
					int _cmd = getIdxCommand(partes[1]);
					if (_cmd < NICK) {
						return DELIVERER.help(partes[1]);
					} else if(_cmd == NICK) {
						return "NICK\r\n" +
						  "-----------------------------\r\n" +
						  "Permite cambiar el nick identificativo del bot de IRC.\r\n\r\n" +
						  "Modo de empleo: \r\n\r\n\t" + 
						  "nick(nick_Deliverer)\r\n";
					} else if(_cmd == DCCIP) {
						return "DCCIP\r\n" +
						  "-----------------------------\r\n" +
						  "Antes de pedir una transferencia por " +
						  "DCC, este comando debe usarse (al menos " +
						  "una vez) para especificar la direccion " +
						  "externa del router o host a traves del "+
						  "cual sale el bot de IRC.\r\n\r\n" +
						  "Modo de empleo: \r\n\r\n\t" + 
						  "dccip(direccion_ip_externa)\r\n";
					} else {
						return partes[0] + "\r\n" + "Comando no v�lido";
					}
				}
			case SETMAPPING:
				if (admin) {
					if (DELIVERER.getServiceMap().set(partes[1],partes[2]))
						return "+OK servicio " + partes[1] + 
							   " ajustado a la clase " +
							   partes[2];
					else
						return "-ERR la clase "+
							   partes[2] + " no existe";
				} else {
					return "-ERR no dispone de autorizaci�n para modificar el mapa de servicios";
				}
			case GETMAPPING:
				String classTranslator = DELIVERER.getServiceMap().get(partes[1]);
				if (classTranslator != null)
					return "+OK "+
						   StringFacility.rellena(partes[1],31," ") +
						   classTranslator;
				else
					return "-ERR el servicio "+
						   partes[1] +
						   " no existe";
			case REMOVEMAPPING:
				if (admin) {
					if (DELIVERER.getServiceMap().remove(partes[1]))
						return "+OK servicio retirado";
					else
						return "-ERR el servicio " + partes[1] +
							   " no est� mapeado";
				} else {
					return "-ERR no dispone de autorizaci�n para modificar el mapa de servicios";
				}

			case SAVEMAP:
				if (admin) {
					boolean flag;
					if (partes.length < 2)
						flag = DELIVERER.getServiceMap().saveMap();
					else
						flag = DELIVERER.getServiceMap().saveMap(partes[1]);
					if (flag)
						return "+OK mapa de servicios almacenado";
					else
						return "-ERR no se ha podido guardar el nuevo mapa a disco";
				} else {
					return "-ERR no dispone de autorizaci�n para modificar los archivos de configuraci�n";
				}

			case LOADMAP:
				if (admin) {
					boolean flag;
					if (partes.length < 2)
						flag = DELIVERER.getServiceMap().loadMap();
					else
						flag = DELIVERER.getServiceMap().loadMap(partes[1]);
					if (flag)
						return "+OK mapa de servicios cargado";
					else
						return "-ERR no se ha podido cargar el mapa de disco";
				} else {
					return "-ERR no dispone de autorizaci�n para modificar el mapa de servicios";
				}					
			case SAVEQUEUES:
				if (admin) {
					boolean flag;
					if (partes.length < 2)
						flag = DELIVERER.saveQueues();
					else
						flag = DELIVERER.saveQueues(partes[1]);
					if (flag)
						return "+OK configuraci�n de colas guardada";
					else
						return "-ERR fallo al guardar la configuraci�n de colas";
				} else {
					return "-ERR no tiene autorizaci�n para modificar los archivos de configuraci�n";
				}
			case LOADQUEUES:
				if (admin) {
					boolean flag;
					if (partes.length < 2)
						flag = DELIVERER.loadQueues();
					else
						flag = DELIVERER.loadQueues(partes[1]);
					if (flag)
						return "+OK colas cargadas";
					else
						return "-ERR fallo al cargar la configuraci�n de colas";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case CLEARQUEUES:
				if (admin) {
					DELIVERER.clearQueues();
					return "+OK eliminadas todas las colas";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case RELOADMAP:
				if (admin) {
					DELIVERER.getServiceMap().clearMap();
					if (DELIVERER.getServiceMap().loadMap())
						return "+OK mapa recargado de mappedservices.cfg";
					else
						return "-ERR fallo al recargar el mapa. Mapa vac�o";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}		   
			case CLEARMAP:
				if (admin) {
					DELIVERER.getServiceMap().clearMap();
					return "+OK Mapa vacio";
				} else 
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case RELOADPROGRAMS:
				if (admin) {
					DELIVERER.getScheduler().clear();
					if (partes.length < 2) {
						if (DELIVERER.getScheduler().load())
							return "+OK programas recargados";
						else
							return "-ERR fallo al recargar";
					} else {
						if (DELIVERER.getScheduler().load(partes[1]))
							return "+OK programas recargadps";
						else
							return "-ERR el archivo " + partes[1] +
								   " no existe";
					}
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case LOADPROGRAMS:
				if (admin) {
					if (partes.length < 2) {
						if (DELIVERER.getScheduler().load())
							return "+OK programas cargados";
						else
							return "-ERR fallo al cargar";
					} else {
						if (DELIVERER.getScheduler().load(partes[1]))
							return "+OK programas cargadps";
						else
							return "-ERR el archivo " + partes[1] +
								   " no existe";
					}
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case SAVEPROGRAMS:
				if (admin) {
					if (partes.length < 2) {
						if (DELIVERER.getScheduler().save())
							return "+OK programas guardados";
						else
							return "-ERR fallo al guardar";
					} else {
						if (DELIVERER.getScheduler().save(partes[1]))
							return "+OK programas guardados";
						else
							return "-ERR no se pudo abrir el archivo "+
								   partes[1];
					}
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case CLEARPROGRAMS:
				if (admin) {
					DELIVERER.getScheduler().clear();
					return "+OK scheduler l�mpio";
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case OPENPROXY:
				if (admin) {
					return DELIVERER.openProxy(partes[1],Integer.parseInt(partes[2]),Integer.parseInt(partes[3]));
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";					
			case CLOSEPROXY:
				if (admin) {
					if (DELIVERER.closeProxy(partes[1]))
						return "+OK proxy cerrado";
					else
						return "-ERR no se ha podido cerrar el proxy";
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case CLOSEALLPROXIES:
				if (admin) {
					DELIVERER.closeAllProxies();
					return "+OK todos los proxies cerrados";
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case LISTPROXIES:
				if (admin) {
					return DELIVERER.listProxies();
				} else
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
			case NICK:
				if (admin) {
					ircb.send("NICK "+partes[1]);
					return "+OK Ahora mi nick es "+partes[1];
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case DCCIP:
				if (admin) {
					ircb.getDCCTransfer().setIP(partes[1]);
					return "+OK IP publica establecida a "+partes[1];
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case JOIN:
				if (admin) {
					ircb.send("JOIN "+partes[1]);
					return "+OK Me he unido al canal "+partes[1];
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case PART:
				if (admin) {
					if (partes.length == 1 && canal.startsWith("#")) {
						ircb.send("PART "+canal);
						return "+OK Me he salido del canal "+canal;
					} else {
						ircb.send("PART "+partes[1]);
						return "+OK Me he salido del canal "+partes[1];
					}
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case ACT:
				if (admin || ircb.isUser(nick)) {
					if (partes.length == 2 && canal.startsWith("#")) {
						ircb.send("PRIVMSG "+canal+" :"+partes[1]);
						return "+OK He actuado para "+canal;
					} else {
						ircb.send("PRIVMSG "+partes[1]+" :"+partes[2]);
						return "+OK He actuado para "+partes[1];
					}
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case ADDUSER:
				if (admin) {
					ircb.addUser(partes[1]);
					return "+OK El usuario "+partes[1]+" ha sido introducido en la lista de usuarios.";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case SUBUSER:
				if (admin) {
					ircb.subUser(partes[1]);
					return "+OK El usuario "+partes[1]+" ha sido eliminado de la lista de usuarios.";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}				
			case LOADUSERS:
				if (admin) {
					if (partes.length > 1) {
						ircb.loadUsers(partes[1]);
						return "+OK Archivo de usuarios cargado - "+partes[1];
					} else {
						ircb.loadUsers();
						return "+OK Archivo de usuarios cargado - ircusers.cfg";
					}
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case SAVEUSERS:
				if (admin) {
					if (partes.length > 1) {
						ircb.saveUsers(partes[1]);
						return "+OK Archivo de usuarios cargado - "+partes[1];
					} else {
						ircb.saveUsers();
						return "+OK Archivo de usuarios cargado - ircusers.cfg";
					}
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			case CLEARUSERS:
				if (admin) {
					ircb.clearUsers();
					return "+OK Lista de usuarios borrada";
				} else {
					return "-ERR no dispone de autorizaci�n para esa operaci�n";
				}
			default:
				return "-ERR comando no entendido";
			}
		} catch (Exception e) {
			return "-ERR faltan par�metros";
		}
	}
}
