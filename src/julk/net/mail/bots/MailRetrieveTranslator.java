package julk.net.mail.bots;
import julk.net.deliver.*;
import julk.net.mail.*;
import java.util.*;
import java.io.File;

public class MailRetrieveTranslator extends Translator
{
	//Ruta de trabajo de Mail Retriever
	private static final String path="D:\\DELIVERER\\MAILR\\";
	
	public boolean translate (String user, String service,
							  String command, WorkResult wr)
	{
		setWorkResult(null);
		StringTokenizer st = new StringTokenizer(command);
		String cmd;
		try {
			cmd = st.nextToken();
		} catch (NoSuchElementException nsee) {
			feedback("MAILR> Debe especificar:\r\n"+
					 "comando");
			return false;
		}
		if (cmd.equalsIgnoreCase("store")) {
			String host, usr, pass;
			try {
				host = st.nextToken();
				usr = st.nextToken();
				pass = st.nextToken();
			} catch (NoSuchElementException nsee) {
				feedback("Subject: MailRetriever: Llamada incorrecta\r\n\r\n"+
						 "MAILR> Debe especificar:\r\n"+
						 "STORE host usuario contraseña");
				return false;
			}
			try {
				POP3 pop = new POP3(host,110);
				pop.user(usr);
				pop.pass(pass);
				POP3.Stat pops = pop.stat();
				if (pops.messages() > 0) {
					File f = new File(path+user);
					if (!f.exists() || !f.isDirectory()) {
						if (!f.mkdir()) {
							feedback("Subject: MailRetriever: Error de sistema\r\n\r\n"+
									 "MAILR> Error al crear el directorio "+user);
						} 
					}
					
					String smsg = "Subject: MailRetriever: Listado de mensajes\r\n\r\n"+
								  "MAILR> Todo el correo de la cuenta "+usr+
								  "del sistema "+host+" ha sido descargado.\r\n"+
								  "Lista de mensajes\r\n"+
								  "--------------------------------------------\r\n" ;
					POP3.List lmsg = pop.list();
					int lsize = lmsg.size();
					for(int i = 1; i <= lsize; i++) {
						POP3.Message msg = pop.retr(i);
						msg.saveToFile(path+user+"\\retr"+i+".msg");
						String line = "From: Desconocido";
						for (int j = 0; j < msg.linesCount(); j++) {
							line = msg.lineAt(j).toUpperCase();
							if (line.startsWith("FROM"))
								break;
						}
						pop.dele(i);
						smsg += "Id: "+i+" "+line+
								" Size: "+
							    lmsg.getMessageAt(i).size()+"\r\n";
					}
					feedback(smsg);
				}
				pop.quit();
				return true;
			} catch (POP3Exception pop3e) {
				feedback("Subject: MailRetriever: Error POP3\r\n\r\n"+
						 "MAILR> Se produjo el siguiente error\r\n"+
						 "durante la operación con el sistema "+host+
						 ":\r\n"+pop3e.getMessage());
				return false;
			} catch (Exception e) {
				feedback("Subject: MailRetriever: Error general\r\n\r\n"+
						 "MAILR> Se produjo el siguiente error\r\n"+
						 "durante la operación con el sistema "+host+
						 ":\r\n"+e.getMessage());
				return false;
			}
		} else {
			String id;
			try {
				 id = st.nextToken();
			} catch (NoSuchElementException nsee) {
				feedback("Subject: MailRetriever: Llamada incorrecta\r\n\r\n"+
						 "MAILR> Debe especificar:\r\n"+
						 "GET id-msg");
				return false;
			}
			
			File f = new File(path+user+"\\retr"+id+".msg");
			if (!f.exists()) {
				feedback("Subject: MailRetriever: Error del usuario\r\n\r\n"+
						 "El mensaje solicitado no está almacenado en el servidor");
				return false;
			}
		
			setWorkResult(new WorkResult(f.getAbsolutePath(),false));
			return true;
		}
	}
}