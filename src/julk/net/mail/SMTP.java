package julk.net.mail;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 */
public class SMTP
{
	private Socket s;
	private FileOutputStream log;
	private InputStream in;
	private OutputStream out;
	private String header;
	private int status;
	
	public static final int SMTP_ERROR = -1;
	public static final int SMTP_HELO = 0;
	public static final int SMTP_MAIL_FROM = 1;
	public static final int SMTP_RCPT_TO = 2;
	public static final int SMTP_DATA = 3;
	public static final int SMTP_COMPLETE = 4;
	
	public SMTP (String host, int port)
		throws SMTPException
	{
		status = SMTP_ERROR;
		initLog("jnt_mail_smtp.log");
		initSocket(host, port);
		header = smtpResponse();
		if (!header.startsWith("220")) {
			sendToLog("jnt_mail_smtp - servicio no disponible\r\n");
			throw new SMTPException("jnt_mail_smtp - servicio no disponible");
		}
		status = SMTP_HELO;
	}
	
	private String smtpResponse()
		throws SMTPException
	{
		String response = "";
		int intentos = 0, c;
		
		try {
			while (in.available() == 0 && intentos < 100) {
				Thread.sleep(100);
				intentos++;
			}
		
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
			throw new SMTPException("Error de comunicaci�n");
		}
		
		return response;
	}
	
	private void smtpRequest(String cmd)
		throws SMTPException
	{
		try {
			sendToLog("-->"+cmd);
			out.write(cmd.getBytes());
		} catch (IOException e) {
			sendToLog("-/->Error de comunicaci�n\r\n");
			throw new SMTPException("Error de comunicaci�n");
		}
	}
	
	public void helo (String domain)
		throws SMTPException
	{
		if (status != SMTP_HELO) {
			sendToLog("fnt_mail_smtp - estado no v�lido para ese comando\r\n");
			throw new SMTPException("fnt_mail_smtp - estado no v�lido para ese comando");
		}
		smtpRequest("HELO "+domain+"\r\n");
		String res = smtpResponse();
		if (!res.startsWith("250")) {
			sendToLog("fnt_mail_smtp - dominio "+domain+" no aceptado o error en el estado de autenticaci�n\r\n");
			throw new SMTPException("fnt_mail_smtp - dominio "+domain+" no aceptado o error en el estado de autenticaci�n");
		}
		status = SMTP_MAIL_FROM;
	}
	
	public void mail_from (String sender)
		throws SMTPException
	{
		if (status != SMTP_MAIL_FROM) {
			sendToLog("fnt_mail_smtp - estado no valido para ese comando\r\n");
			throw new SMTPException("fnt_mail_smtp - estado no v�lido para ese comando");
		}
		smtpRequest("MAIL FROM: <"+sender+">\r\n");
		String res = smtpResponse();
		if (!res.startsWith("250")) {
			sendToLog("fnt_mail_smtp - emisor "+sender+" no aceptado o error en el estado de autenticaci�n\r\n");
			throw new SMTPException("fnt_mail_smtp - emisor "+sender+" no aceptado o error en el estado de autenticaci�n");
		}
		status = SMTP_RCPT_TO;
	}
	
	public void rcpt_to (String recipient)
		throws SMTPException
	{
		if (status < SMTP_RCPT_TO) {
			sendToLog("fnt_mail_smtp - estado no valido para ese comando\r\n");
			throw new SMTPException("fnt_mail_smtp - estado no v�lido para ese comando");
		}
		smtpRequest("RCPT TO: <"+recipient+">\r\n");
		String res = smtpResponse();
		if (!res.startsWith("250")) {
			sendToLog("fnt_mail_smtp - destinatario "+recipient+" no aceptado o error en el estado de autenticaci�n\r\n");
			throw new SMTPException("fnt_mail_smtp - destinatario "+recipient+" no aceptado o error en el estado de autenticaci�n");
		}
		status = SMTP_DATA;
	}
	
	public void data (String message)
		throws SMTPException
	{
		if (status != SMTP_DATA) {
			sendToLog("fnt_mail_smtp - estado no valido para ese comando\r\n");
			throw new SMTPException("fnt_mail_smtp - estado no v�lido para ese comando");
		}
		smtpRequest("DATA\r\n");
		String res = smtpResponse();
		if (!res.startsWith("354")) {
			sendToLog("fnt_mail_smtp - error de protocolo\r\n");
			throw new SMTPException("fnt_mail_smtp - error de protocolo");
		}
		smtpRequest(message+"\r\n.\r\n");
		res = smtpResponse();
		if (!res.startsWith("250")) {
			sendToLog("fnt_mail_smtp - error de protocolo\r\n");
			throw new SMTPException("fnt_mail_smtp - error de protocolo");
		}			
		status = SMTP_COMPLETE;
	}

	public void dataFile (String filename)
		throws SMTPException
	{
		if (status != SMTP_DATA) {
			sendToLog("fnt_mail_smtp - estado no valido para ese comando\r\n");
			throw new SMTPException("fnt_mail_smtp - estado no v�lido para ese comando");
		}
		smtpRequest("DATA\r\n");
		String res = smtpResponse();
		if (!res.startsWith("354")) {
			sendToLog("fnt_mail_smtp - error de protocolo\r\n");
			throw new SMTPException("fnt_mail_smtp - error de protocolo");
		}
		BufferedReader fin;
		try {
			fin = new BufferedReader(new FileReader(filename));			
		} catch (Exception e) {
			sendToLog("fnt_mail_smtp - el archivo "+filename+" no existe\r\n");
			throw new SMTPException("fnt_mail_smtp - el archivo no existe");
		}
		try {
			while ((res = fin.readLine()) != null)
				if (!res.equals("."))
					smtpRequest(res+"\r\n");
			smtpRequest("\r\n.\r\n");
			fin.close();
		} catch (Exception e2) {
			sendToLog("fnt_mail_smtp - error al leer del archivo "+filename+"\r\n");
			throw new SMTPException("fnt_mail_smtp - error al leer de archivo");
		}
		res = smtpResponse();
		if (!res.startsWith("250")) {
			sendToLog("fnt_mail_smtp - error de protocolo\r\n");
			throw new SMTPException("fnt_mail_smtp - error de protocolo");
		}			
		status = SMTP_COMPLETE;
	}
	
	public void rset ()
		throws SMTPException
	{
		if (status == SMTP_HELO) {
			sendToLog("fnt_mail_smtp - no has hecho ninguna operaci�n a�n\r\n");
			return;
		}
		smtpRequest("RSET\r\n");
		String res = smtpResponse();
		if (res.startsWith("250")) {
			sendToLog("fnt_mail_smtp - error de protocolo\r\n");
			throw new SMTPException("fnt_mail_smtp - error de protocolo");
		}
		status = SMTP_HELO;
	}
		
	public void quit ()
		throws SMTPException
	{
		if (status == SMTP_ERROR) {
			sendToLog("fnt_mail_smtp - no conectado\r\n");
			return;
		}
		smtpRequest("QUIT\r\n");
		String res = smtpResponse();
		if (!res.startsWith("221")) {
			sendToLog("fnt_mail_smtp - error de protocolo\r\n");
			throw new SMTPException("fnt_mail_smtp - error de protocolo");
		}
		status = SMTP_ERROR;
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
		throws SMTPException
	{
		try {
			s = new Socket(host,port);
			in = s.getInputStream();
			out = s.getOutputStream();
		} catch (Exception e) {
			sendToLog("jnt_mail_smtp: fallo de conexi�n con "+host+":"+port+"\r\n");
			throw new SMTPException("Fallo de conexi�n");
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
	
	public static void main (String[] args)
		throws Exception
	{
		SMTP smtp = new SMTP("correo.jet.es",25);
		smtp.helo("dolly");
		smtp.mail_from("julk@jet.es");
		smtp.rcpt_to("julk@jet.es");
		smtp.rcpt_to("jota1@jet.es");
		smtp.data("Subject: Prueba\r\nHola colega");
		smtp.quit();
		smtp.close();
	}
}
