package julk.net.mail.bots;

import julk.net.mail.*;
import julk.net.deliver.Deliverer;
import julk.net.deliver.WorkItem;
import julk.net.deliver.WorkResult;
import julk.net.deliver.MailConfig;
import java.io.FileOutputStream;
import julk.net.scheduler.*;

public class SPBroken extends SchedulerProgram
{
	private String pop3Server;
	private String user;
	private String pass;
	private int pop3Port;
	private WorkItem wi;
	//private Deliverer DELIVERER;
	  
	public SPBroken(String pop3Server, String user, String pass, Deliverer deliverer)
		throws Exception
	{
		this.pop3Server = pop3Server;
		this.pop3Port = 110;
		this.user = user;
		this.pass = pass;
		//this.DELIVERER = deliverer;
	}
	
	public SPBroken ()
	{
	}
	
	public void Init()
	{
		if (setDeliverer()) {
			MailConfig mail = DELIVERER.getMailConfig();
			pop3Server = mail.getPOP3server();
			pop3Port = mail.getPOP3port();
			user = mail.getPOP3user();
			pass = mail.getPOP3pass();
		} else {
			pop3Server = "pop3.terra.es";
			pop3Port = 110;
			user = "robomail.terra.es";
			pass = "botmail";
			setReady(false);
		}
	}
					
	private POP3 conectarPOP3()
	{
		POP3 pop3;
		
		try {
			pop3 = new POP3(pop3Server, pop3Port);
			pop3.user(user);
			pop3.pass(pass);
			POP3.Stat stat = pop3.stat();
			if (stat.messages() != 0)
				return pop3;
			pop3.quit();
		} catch (POP3Exception pop3e) {}
		pop3 = null;
		return null;
	}
	
	private String getSender(String from)
	{
		String value;
		int pos;
		
		pos = from.indexOf(':');
		
		if (pos == -1)
			return null;
		
		value = from.substring(pos+1);
		
		pos = value.indexOf('<');
		
		if (pos == -1)
			return value;
		
		int pos_f = value.indexOf('>');
		
		value = value.substring(pos+1,pos_f);
		
		return value;
	}
	
	public void doTheWork()
	{
		try {
			POP3 pop3;
			String cmd,service;
			int pos;
			if ((pop3 = conectarPOP3()) == null)
				return;
			POP3.List _list;
			_list = pop3.list();
			String from;
			int start;
			for (int n = 1; n <= _list.size(); n++) {
				// Comenzar recogiendo el mensaje
				POP3.Message msg = pop3.retr(n);
				// Obtener emisor del mensaje
				from = "";
				start = 0;
				while (!msg.lineAt(start).toLowerCase().startsWith("from:")
					   && start < msg.linesCount()-1)
					start++;
				if (start < msg.linesCount()-1) {
					from = getSender(msg.lineAt(start));
					start++;
				} else {
					continue;
				}
				// Leer y extrar comandos
				for (int l = 0; l < msg.linesCount(); l++) {
					// Identificar el protocolo y enviarlo
					//     al broker concreto para el protocolo.
					cmd = msg.lineAt(l).trim();
					pos = cmd.indexOf("#");
					if (pos == -1)
						continue;
					service = cmd.substring(0,pos);
					pos = cmd.toLowerCase().indexOf("mailer#");
					if (pos == -1)
						cmd += "#mailer#do";
					wi = new WorkItem(from,service,cmd);
					if (DELIVERER.add(wi)) 
						System.out.println("Trabajo sometido: "+cmd);
					else {
						System.out.println("Trabajo no aceptado: "+cmd);
						FileOutputStream fout = new FileOutputStream ("error.mme");
						fout.write("Subject: Robomail error\r\n\r\n".getBytes());
						fout.write("Su petición no ha sido aceptada.\r\n".getBytes());
						fout.write("Verifique la sintaxis del comando o póngase en contacto con e personal responsable del servicio.\r\n".getBytes());
						fout.write(cmd.getBytes());								
						fout.close();
						WorkResult wr = new WorkResult("error.mme",false);
						wi = new WorkItem (from,"mailer","mailer#do",wr);
						DELIVERER.add(wi);
					}
				}
			}
			for (int n = 1; n <= _list.size(); n++)
				pop3.dele(n);
			pop3.quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public void launch ()
	{		
		if (!setDeliverer()) {
			setReady(false);
			return;
		}
		
		try {
			doTheWork();
		} catch (Exception e) {
		}
	}
}
