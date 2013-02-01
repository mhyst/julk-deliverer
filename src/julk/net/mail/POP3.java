package julk.net.mail;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;

/**
 */
public class POP3
{
	private Socket s;
	private FileOutputStream log;
	private InputStream in;
	private OutputStream out;
	private String header;
	private int status;
	
	public static final int POP3_ERROR = -1;
	public static final int POP3_AUTH_USER = 0;
	public static final int POP3_AUTH_PASS = 1;
	public static final int POP3_CONNECTED = 2;
	  
	public POP3 (String host, int port)
		throws POP3Exception
	{
		status = POP3_ERROR;
		initLog("jnt_mail_pop3.log");
		initSocket(host, port);
		header = pop3Response();
		if (!header.startsWith("+OK")) {
			sendToLog("jnt_mail_pop3 - servicio no disponible\r\n");
			throw new POP3Exception("jnt_mail_pop3 - servicio no disponible");
		}
		status = POP3_AUTH_USER;
	}
	
	private String pop3Response()
		throws POP3Exception
	{
		String response = "";
		int intentos = 0, c;
		
		try {
			while (in.available() == 0 && intentos < 100) {
				Thread.sleep(100);
				intentos++;
			}
			
			if (intentos == 100)
				throw new POP3Exception("El servidor no responde");
		
			while ((c = in.read()) != -1) {
				if (c == 13) {
					c = in.read();
					break;
				}
				response += (char) c;
			}
		
			sendToLog("<--"+response+"\r\n");
		} catch (Exception e) {
			sendToLog("<-/-Error de comunicaci�n\r\n");
			throw new POP3Exception("Error de comunicaci�n");
		}
		
		return response;
	}
	
	private void pop3Request(String cmd)
		throws POP3Exception
	{
		try {
			if (cmd.toUpperCase().startsWith("PASS"))
				sendToLog("-->PASS *********\r\n");
			else
				sendToLog("-->"+cmd);
			out.write(cmd.getBytes());
		} catch (IOException e) {
			sendToLog("-/->Error de comunicaci�n\r\n");
			throw new POP3Exception("Error de comunicaci�n");
		}
	}
	
	public boolean user (String user)
		throws POP3Exception
	{
		if (status != POP3_AUTH_USER) {
			sendToLog("fnt_mail_pop3 - estado no v�lido para ese comando\r\n");
			throw new POP3Exception("fnt_mail_pop3 - estado no v�lido para ese comando");
		}
		pop3Request("USER "+user+"\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - usuario no valido o error en el estado de autenticaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - usuario no valido o error en el estado de autenticaci�n");
		}
		status = POP3_AUTH_PASS;
		return true;
	}
	
	public boolean pass (String pass)
		throws POP3Exception
	{
		if (status != POP3_AUTH_PASS) {
			sendToLog("fnt_mail_pop3 - estado no v�lido para ese comando\r\n");
			throw new POP3Exception("fnt_mail_pop3 - estado no v�lido para ese comando");
		}
		pop3Request("PASS "+pass+"\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - usuario o contrase�a no validos o error en el estado de autenticaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - usuario o contrase�a no validos o error en el estado de autenticaci�n");
		}
		status = POP3_CONNECTED;
		return true;
	}
	
	public Stat stat ()
		throws POP3Exception
	{
		if (status != POP3_CONNECTED) {
			sendToLog("fnt_mail_pop3 - requerida autentificaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - identifiquese primero");
		}
		pop3Request("STAT\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - error en el comando STAT\r\n");
			throw new POP3Exception("fnt_mail_pop3 - error en el comando STAT");
		}
		StringTokenizer st = new StringTokenizer(res," ");
		/*String datos;
		if (st.hasMoreTokens())
			datos = st.nextToken();*/
		Stat _stat = new Stat();
		if (st.hasMoreTokens())
			st.nextToken(); //+OK fuera
		if (st.hasMoreTokens())
			_stat.messages(Integer.parseInt(st.nextToken()));
		if (st.hasMoreTokens())
			_stat.size(Integer.parseInt(st.nextToken()));
		/*if (_stat.messages() == 0 || _stat.size() == 0) {
			sendToLog("jnt_mail_pop3 - error de protocolo en el comando LIST\r\n");
			throw new POP3Exception("jnt_mail_pop3 - error de protocolo en el comando LIST");
		}*/
		return _stat;
	}
	
	public List list ()
		throws POP3Exception
	{
		if (status != POP3_CONNECTED) {
			sendToLog("fnt_mail_pop3 - requerida autentificaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - identifiquese primero");
		}
		pop3Request("LIST\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - error en el comando LIST\r\n");
			throw new POP3Exception("fnt_mail_pop3 - error en el comando LIST");
		}
		StringTokenizer st; 
		int nmsg = 0, size = 0;
		List _list = new List();
		while (!(res = pop3Response()).equals(".")) {
			st = new StringTokenizer(res," ");
			
			if (st.hasMoreTokens())
				nmsg = Integer.parseInt(st.nextToken());
			if (st.hasMoreTokens())
				size = Integer.parseInt(st.nextToken());
			if (nmsg == 0 || size == 0) {
				sendToLog("jnt_mail_pop3 - error de protocolo en el comando LIST\r\n");
				throw new POP3Exception("jnt_mail_pop3 - error de protocolo en el comando LIST");
			}
			_list.addMessage(nmsg, size);
		}
		return _list;
	}
	
	public Message retr (int index)
		throws POP3Exception
	{
		if (status != POP3_CONNECTED) {
			sendToLog("fnt_mail_pop3 - requerida autentificaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - identifiquese primero");
		}
		pop3Request("RETR "+index+"\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - el mensaje solicitado no se encuentra en el buz�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - el mensaje solicitado no se encuentra en el buz�n");
		}
		Message _msg = new Message();
		while (!(res = pop3Response()).equals(".")) {
			_msg.addLine(res);
		}
		return _msg;
	}
	
	public void dele (int index)
		throws POP3Exception
	{
		if (status != POP3_CONNECTED) {
			sendToLog("fnt_mail_pop3 - requerida autentificaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - identifiquese primero");
		}
		pop3Request("DELE "+index+"\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - el mensaje solicitado no se encuentra en el buz�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - el mensaje solicitado no se encuentra en el buz�n");
		}
	}
	
	public void rset ()
		throws POP3Exception
	{
		if (status != POP3_CONNECTED) {
			sendToLog("fnt_mail_pop3 - requerida autentificaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - identifiquese primero");
		}
		pop3Request("RSET\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - error de protocolo en el comando RSET\r\n");
			throw new POP3Exception("fnt_mail_pop3 - error de protocolo en el  comando RSET");
		}
	}

	public void quit ()
		throws POP3Exception
	{
		if (status != POP3_CONNECTED) {
			sendToLog("fnt_mail_pop3 - requerida autentificaci�n\r\n");
			throw new POP3Exception("fnt_mail_pop3 - identifiquese primero");
		}
		pop3Request("QUIT\r\n");
		String res = pop3Response();
		if (res.startsWith("-ERR")) {
			sendToLog("fnt_mail_pop3 - error de protocolo en el comando QUIT\r\n");
			throw new POP3Exception("fnt_mail_pop3 - error de protocolo en el comando QUIT");
		}
	}
	
	private void close()
	{
		try {
			sendToLog("Cerrando sesion\r\n");
			s.close();
			s = null;
		} catch (IOException e) {
		}
	}
	
	private void initLog(String filename)
	{
		try {
			log = new FileOutputStream(filename, true);
			sendToLog("Inicio log - " + new java.util.Date() + "\r\n");
		} catch (IOException ioe) {
		}
	}
	
	private void sendToLog(String msg)
	{
		if (log != null) {
			try {
				log.write(msg.getBytes());
			} catch (IOException ioe) {}
		}
		
		System.out.print(msg);			
	}
	
	private void initSocket(String host, int port)
		throws POP3Exception
	{
		try {
			s = new Socket(host,port);
			in = s.getInputStream();
			out = s.getOutputStream();
		} catch (Exception e) {
			sendToLog("jnt_mail_pop3: fallo de conexi�n con "+host+":"+port+"\r\n");
			throw new POP3Exception("Fallo de conexi�n");
		}
	}
	
	public void finalize()
	{
		if (s != null)
			this.close();
		sendToLog("Cierre log - "+new java.util.Date()+"\r\n");
		try {
			log.close();
		} catch (IOException e) {}
	}
	
	public class Message {
		private Vector<String> lines;
		
		public Message ()
		{
			lines = new Vector<String>();
		}
		
		public void addLine(String line)
		{
			lines.addElement(line);
		}
		
		public String lineAt(int index)
		{
			return (String) lines.elementAt(index);
		}
		
		public int linesCount()
		{
			lines.trimToSize();
			return lines.size();
		}
		
		public void saveToFile (String filename)
			throws IOException
		{
			Enumeration<String> e = lines.elements();
			
			FileOutputStream fout = new FileOutputStream(filename);
			
			while (e.hasMoreElements())
				fout.write((e.nextElement() + "\r\n").getBytes());
			
			fout.close();
		}
	}
	
	public class List {
		private Vector<Stat> v;
		
		public List ()
		{
			v = new Vector<Stat>();
		}
		
		public void addMessage (int number, int size)
		{
			v.addElement (new Stat(number,size));
		}
		
		public Stat getMessageAt (int index)
		{
			return (Stat) v.elementAt (index-1);
		}
		
		public int size ()
		{
			v.trimToSize ();
			return v.size ();
		}
		
		public void clear ()
		{
			v.removeAllElements ();
		}
	}
	
	/**Clase para almacenar los resultados del comando STAT
	 */
	public class Stat {
		private int number_of_messages;
		private int size_of_messages;
		
		public Stat(int _number, int _size)
		{
			messages(_number);
			size(_size);
		}
		
		public Stat ()
		{
			number_of_messages = 0;
			size_of_messages = 0;
		}
		
		public void messages(int _number_of_messages)
		{
			number_of_messages = _number_of_messages;
		}
		
		public int messages()
		{
			return number_of_messages;
		}
		
		public void size(int _size_of_messages)
		{
			size_of_messages = _size_of_messages;
		}
		
		public int size()
		{
			return size_of_messages;
		}
	}
	
	public static void main (String[] args)
		throws Exception
	{
		POP3 pop3 = new POP3("jet.es",110);
		
		pop3.user("jota1");
		pop3.pass("sauron");
		Stat _stat = pop3.stat();
		if (_stat.messages()>0) {
			List _list = pop3.list();
			for ( int i = 1; i <= _list.size(); i++)
				pop3.retr(i).saveToFile("pop3"+i+".mme");
		}
		pop3.quit();
		pop3.close();
	}
}
