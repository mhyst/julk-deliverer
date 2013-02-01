package julk.net.deliver;

//import java.util.StringTokenizer;
//import java.util.Random;
//import java.util.Calendar;
//import java.io.FileOutputStream;
//import java.io.FileInputStream;
//import java.io.File;
import java.io.*;
import java.util.*;

public class FileTranslator extends Translator
{	
	private WorkResult list(String ruta)
	{
		Random r = new Random();
		r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
		String name = "list"+r.hashCode()+".txt";
		try {
			FileOutputStream fout = new FileOutputStream(name);
			File f = new File(ruta.trim());
			String[] lista = f.list();
			if (lista != null) {
				fout.write(("Listado del directorio: "+ruta+"\r\n").getBytes());
				for (int i = 0; i < lista.length; i++)
					fout.write((lista[i]+"\r\n").getBytes());
				fout.write(("Fin listado\r\n").getBytes());
				fout.close();
				WorkResult wr = new WorkResult(name);
				return wr;
			}
			fout.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private WorkResult get(String ruta)
	{
		//Quitamos posibles espacios en blanco por ambos lados de la ruta
		ruta = ruta.trim();
		
 
		/*Random r = new Random();
		r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));*/
		/*int pos = ruta.trim().lastIndexOf(".");
		if(pos == -1) {
			return null;
		}*/
		
		//Extraemos unicamente el nombre del archivo
		//desde la ruta completa.
		int pos = ruta.lastIndexOf("\\");
		String _name, name="";
		if (pos == -1) {
			pos = ruta.lastIndexOf("/");
		}
		if (pos == -1) {
			_name = ruta;
		} else {
			_name = ruta.substring(pos+1);
			//byte[] buff = new byte[_name.length()];
			byte[] buff = _name.getBytes();
			for(int i=0; i<buff.length; i++) {
				name+= (buff[i] == ' ' ? (char)'_' : (char)buff[i]);
			}
		}
		//System.out.println("FileTranslator: File="+name);
		
		//Procedemos a copiarlo a la carpeta local de Deliverer
		//por motivos de seguridad.
		try {
			FileOutputStream fout = new FileOutputStream(name);
			FileInputStream fin = new FileInputStream(ruta.trim());
			WorkResult.dumpFile(fin,fout);
			fout.close();
			fin.close();
			
			//Devolvemos el archivo copiado como un
			//resultado de este traductor.
			WorkResult wr = new WorkResult(name);
			return wr;
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean translate (String user, String service,
							  String _command, WorkResult nada_wr)
	{
		try {
			/*StringTokenizer st = new StringTokenizer(wi.getCommand(),"#[]");
			String[] cmd = new String[st.countTokens() / 2];
			String[] serv = new String[cmd.length];
		
			for (int i = 0; st.hasMoreTokens(); i++) {
				serv[i] = st.nextToken();
				cmd[i] = st.nextToken();
			}*/
			
			String content = "", cmd;
			WorkResult wr;
			int pos;
			
			cmd = _command;
			if (cmd.length() == 0)
				throw new Exception("No encuentro ning�n comando que procesar");
			//pos = cmd[0].indexOf(" ");
			pos = cmd.indexOf(" ");
			if (pos == -1)
				throw new Exception("Comando no v�lido, trabajo anulado");
			//content = cmd[0].substring(pos);
			content = cmd.substring(pos);
			
			//if (cmd[0].toUpperCase().startsWith("LIST"))
			if (cmd.toUpperCase().startsWith("LIST")) 
				wr = list(content);
			else
				wr = get(content);
			/*for (int i = 1; i < cmd.length; i++)
				command += serv[i]+"#["+cmd[i]+"]";
	
			if (command.length() > 0) {
				WorkItem wis;
				if (wr != null)
					wis = new WorkItem(wi.getUser(),serv[1],command,wr);
				else
					wis = new WorkItem(wi.getUser(),serv[1],command);
				if (DELIVERER.accepts(serv[1]))
					DELIVERER.add(wis);
				else
					rwq.add(wis);
			}*/
			setWorkResult(wr);
			//doNextStep(wi,wr,DELIVERER);
			return true;
		} catch (Exception e) {			
			return false;
		}
	}
}
