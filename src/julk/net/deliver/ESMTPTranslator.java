package julk.net.deliver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import sun.misc.BASE64Encoder;
import julk.net.mail.ESMTP;

public class ESMTPTranslator extends Translator
{	
	public boolean translate (String user, String service,
							  String command, WorkResult wr)
	{
		int pos;
		String dest="";
		pos = command.indexOf("@");
		if (pos != -1) {
			dest = command;
		} else {
			pos = user.indexOf("@");
			if (pos != -1) {
				dest = user;
			} else {
				System.out.println("El usuario no indicó ninguna dirección de email.");
				return false;
			}
		}

		try {
			//WorkResult wr = wi.getWorkResult();
			BASE64Encoder b64encoder = new BASE64Encoder();
			FileInputStream _scriptFile;
			FileOutputStream _msgFile;
			System.out.println("Intentando enviar email a: "+command);
			_msgFile = new FileOutputStream("mailer.mme");
			//_msgFile.write("From: eMailer <deliverer@mipc.zapto.org>\r\n".getBytes());
			//_msgFile.write(("To:  <"+user+">\r\n").getBytes());
			//_msgFile.write(("To:  <"+dest+">\r\n").getBytes());
			
			if (wr != null) {
				File f = new File(wr.getName());
				String nombre = f.getName();
				/*String nombre = new String(wr.getName());
				pos = nombre.lastIndexOf("\\");
				if (pos != -1) nombre = nombre.substring(pos+1);
				pos = nombre.lastIndexOf("/");
				if (pos != -1) nombre = nombre.substring(pos+1);*/

				_scriptFile = new FileInputStream(wr.getName());
				if (wr.isAttached()) {
					//_msgFile.write(("From: "+user+"\r\n").getBytes());
					//_msgFile.write(("From: "++"\r\n").getBytes());
					_msgFile.write("Subject: Respuesta a su petición\r\n".getBytes());
					_msgFile.write("MIME-Version: 1.0\r\n".getBytes());
					_msgFile.write("Content-type: multipart/mixed; boundary=\"mailer-file-result\"\r\n".getBytes());
					_msgFile.write("\r\n--mailer-file-result\r\n".getBytes());
					_msgFile.write("Content-type: text/plain; charset=us-ascii\r\n\r\n".getBytes());
					_msgFile.write("Se ha completado una petición, vea los archivos adjuntos.\r\n".getBytes());
					_msgFile.write(("Trabajo finalizado\r\n").getBytes());
					_msgFile.write("\r\n--mailer-file-result\r\n".getBytes());
					_msgFile.write(("Content-Type: text/plain;name=\""+nombre+"\"\r\n").getBytes());
					_msgFile.write("Content-Transfer-Encoding: Base64\r\n".getBytes());
					_msgFile.write(("Content-Disposition: attachment;filename=\""+nombre+"\"\r\n\r\n").getBytes());
					//Codificación base64
					b64encoder.encodeBuffer(_scriptFile, _msgFile);
					_msgFile.write("\r\n--mailer-file-result--\r\n".getBytes());			
				} else {
					int c;
					while ((c = _scriptFile.read()) != -1)
						_msgFile.write(c);
				}
				_scriptFile.close();
			} else {
				_msgFile.write("Subject: Error de resultado\r\n".getBytes());
				_msgFile.write(("Trabajo finalizado\r\n").getBytes());
				_msgFile.write("No se ha podido completar su petición\r\n".getBytes());
			}
			
			//_msgFile.write("\r\n.\r\n".getBytes());
			_msgFile.close();
			/*pos = user.indexOf("@");
			if (pos == -1)
				return false;
			*/
			//String smtpServer = "smtp." + wi.getUser().substring(pos+1);
			//String smtpServer = "mailhost.terra.es";
			MailConfig mail = getDeliverer().getMailConfig();
			ESMTP smtp = new ESMTP(mail.getSMTPserver(),mail.getSMTPport());
			smtp.ehlo("mailer");
			smtp.authLogin(mail.getESMTPuser(), mail.getESMTPpass());
			smtp.mail_from(mail.getEMail());
			//smtp.rcpt_to(user);
			smtp.rcpt_to(dest);
			smtp.dataFile("mailer.mme");
			smtp.quit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No se ha podido enviar el resultado a "+user);
			return false;
		}
	}
}
