package julk.net.irc;

import java.net.Socket;
import java.io.File;
import julk.net.deliver.Translator;
import julk.net.deliver.WorkResult;

public class IRCDCCTranslator extends Translator{

	/** Este traductor se utiliza para enviar archivos por
	 *  IRC a través de DCC SEND.
	 */
	protected boolean translate (String user, String service,
			                     String command, WorkResult wr)
	{
		try {
			File f = new File(wr.getName());
			File f2 = new File(user+"__"+wr.getName());
			f.renameTo(f2);
			System.out.println("IRCDCC: El archivo ocupa: "+f.length()+" bytes");
			Socket s = new Socket("localhost",6660);
			WorkResult _wr = new WorkResult(f2.getName());
			_wr.send(s.getOutputStream());
			s.close();
			f = new File(wr.getName());
			f2.renameTo(f);
			f.delete();
			return true;
		} catch (Exception e) {
			System.out.println("IRC-DCC-T: Error al intentar hacer DCC SEND");
			System.out.println(e.getMessage());
			return false;
		}
	}
	/**
	 * @param args
	 */

}
