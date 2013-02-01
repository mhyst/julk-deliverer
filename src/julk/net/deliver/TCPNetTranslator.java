package julk.net.deliver;

import java.util.StringTokenizer;
import java.net.Socket;
import java.io.OutputStream;

public class TCPNetTranslator extends Translator
{
	public boolean translate (String user, String service,
							  String command, WorkResult wr)
	{
		String ip;
		int port;
		StringTokenizer st = new StringTokenizer(user,":");
		
		try {
			ip = st.nextToken();
			port = Integer.parseInt(st.nextToken());
		} catch (Exception e) {
			System.out.println("Nombre de usuario no válido, no cumple ip:puerto");
			return false;
		}
		
		Socket s;
		try {
			s = new Socket(ip,port);
		} catch (Exception e) {
			System.out.println("No se puedo abrir el socket en "+ip+":"+port);
			return false;
		}
		
		//WorkResult wr = wi.getWorkResult();
		if (wr == null) {
			System.out.println("No hay nada que enviar");
			try {
				s.close();
			} catch (Exception e) {}
			return false;
		}
		
		OutputStream out;
		try {
			out = s.getOutputStream();
		} catch (Exception e) {
			try {
				s.close();
			} catch (Exception e2) {}
			System.out.println("Fallo al capturar el flujo de salida del socket");
			return false;
		}
		
		try {
			wr.send(out);
			s.close();
		} catch (Exception e) {
			System.out.println("Fallo al enviar el resultado");
			return false;
		}
		
		System.out.println("Resultado enviado a "+ip+":"+port);
		return true;
	}
}
