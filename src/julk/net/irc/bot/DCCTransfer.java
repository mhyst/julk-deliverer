package julk.net.irc.bot;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.util.Random;
import julk.net.irc.client.kernel.*;
import julk.net.deliver.WorkResult;

public class DCCTransfer implements Runnable
{

	private ChatClient chat;
	private Thread t;
	private boolean started = false;
	private String IP;

	public DCCTransfer(ChatClient _ircb)
	{
		chat = _ircb;
		IP = "127.0.0.1";
	}
	
	public void start()
	{
		if (!started) {
			started=true;
			t = new Thread(this);
			t.start();
		}
	}
	
	public void setIP(String _ip) {
		IP = _ip;
	}
	
	private void send(String msg)
	{
		try {
			chat.send(msg);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(new PrintWriter(System.out));
			System.out.println("Error al enviar datos al chat");
		}
	}	
	
	@SuppressWarnings("resource")
	public void run()
	{	
		System.out.println("DCCTransfer: Arrancando...");
		Random r = new Random();
		r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
		try {
			ServerSocket ss = new ServerSocket(6660);
			
			Socket s;
			WorkResult wr;
			while(true){
				s = ss.accept();
				String filename = "dcc"+r.hashCode();
				wr = new WorkResult(filename);
				wr.receive(s.getInputStream());
				//Aqui hacer el DCC SEND
				DCCSend(wr.getName());
				s.close();
			}
		} catch (Exception e) {
			System.out.println("IRC: DCC SEND waiter ha caido por un error.");
			System.out.println(e.getMessage());
		}
	}

	@SuppressWarnings("resource")
	private synchronized void DCCSend(String name) 
	{
		if (chat == null) {
			System.out.println("DCCSend: chat es null, aun no se puede trabajar con DCC");
			return;
		}
		//Extraemos el nick del nombre del archivo
		int pos = name.indexOf("__");
		if (pos == -1) {
			System.out.println("IRC-DCC SEND: No ha llegado el nick del destinatario.");
			return;
		}
		//Creamos servidor de socket
		ServerSocket ss;
		try {
			ss = new ServerSocket(1024);
		} catch (Exception e) {
			System.out.println("IRC-DCC SEND: No se ha podido abrir el socket de envio");
			System.out.println(e.getMessage());
			return;
		}
		String _nick = name.substring(0,pos);
		String filename = name.substring(pos);
		//Ahora separaremos el nick de host "nick_host"
		String host = "";
		pos = _nick.indexOf("_");
		if (pos == -1) {
			System.out.println("DCCTransfer: No se pudo extraer el host");
		} else {
			host = _nick.substring(pos+1);
			_nick = _nick.substring(0,pos);
		}
		//Qu� ip ponemos en el CTCP?
		String ip;
		if (host.indexOf("zapto.org") != -1) {
			//La local
			ip = "192.168.1.64";
		} else {
			//La remota
			if (IP.equalsIgnoreCase("127.0.0.1")) {
				send("NOTICE "+_nick+" :No se ha indicado una IP publica, por tanto no puedo enviar el archivo. Lo siento.");
				return;
			}
			ip = IP;
		}
		
		//Nos ponemos en disposici�n de enviar el ficherito
		try {
			FileInputStream in = new FileInputStream(name);
			Socket s;
			WorkResult wr;
		
			send("NOTICE "+_nick+" :Enviando "+filename.substring(2)+" (mipc.zapto.org))");		
			//send("PRIVMSG "+_nick+" :"+'\001'+"DCC SEND "+filename.substring(2)+" 3232235840 "+ss.getLocalPort()+" "+(long)new File(name).length()+'\001');
			//send("PRIVMSG "+_nick+" :"+'\001'+"DCC SEND "+filename.substring(2)+" 1429948714 "+ss.getLocalPort()+" "+(long)new File(name).length()+'\001');
			send("PRIVMSG "+_nick+" :"+'\001'+"DCC SEND "+filename.substring(2)+" "+makeIP(ip)+" "+ss.getLocalPort()+" "+(long)new File(name).length()+'\001');
			ss.setSoTimeout(15000);
			s = ss.accept();
			wr = new WorkResult(name);
			wr.dumpFileDCC(s.getInputStream(),in,s.getOutputStream());
			in.close();
			s.close();
		} catch (Exception e) {
			System.out.print("IRC: DCC SEND ERROR - ");
			System.out.println(e.getMessage());
		}
		new File(name).delete();
		try {
			ss.close();
		} catch (Exception e) {
			System.out.println("IRC-DCC: ServerSocket no se pudo cerrar.");
		}
	}
	public long makeIP(String ip)
	{
		StringTokenizer st = new StringTokenizer(ip,".");
		long[] bytes = new long[st.countTokens()];
		long intIP;
		//System.out.print("MakeIP: "+ip+"/");
		for (int i=0; st.hasMoreTokens(); i++) {
			bytes[i] = Integer.parseInt(st.nextToken());
			//System.out.print(","+bytes[i]+" ");
		}
		
		intIP = bytes[3];
		intIP += (bytes[2] << 8);
		intIP += (bytes[1] << 16);
		intIP += (bytes[0] << 24);
		
		//System.out.println("Fin makeIP");
		return intIP;
	}
}

