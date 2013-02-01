package julk.net.deliver;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Calendar;
import sun.misc.BASE64Encoder;

//import com.ms.wfc.app.*;

public class ComposerTranslator extends Translator
{
	private String getContentType(String filename)
	{
		int pos = filename.lastIndexOf(".");
		if (pos < 0)
			return "application/x-msdownload";
		else {
			//RegistryKey k;
			//String keyName = filename.substring(pos);
			//String keyValue;
			//k = Registry.CLASSES_ROOT.getSubKey(keyName);
			//keyValue = (String) k.getValue("Content Type");
			//if (keyValue == null)
				return "application/x-msdownload";
			//else
			//	return keyValue;
		}	
	}
	
	public boolean translate (String user, String service,
							  String command, WorkResult owr)
	{
		BASE64Encoder b64e;
		WorkResult wr;
		Calendar d;
		String cmpName = "";
		try {
			StringTokenizer st = new StringTokenizer(command,"?");
			if (st.countTokens() < 2)
				return false;
			String[] partes = new String[st.countTokens()];
			int pos, c;
		
			for (int i = 0; st.hasMoreTokens(); i++)
				partes[i] = st.nextToken();
		
			//if (partes[0].equalsIgnoreCase("mix")) {
				FileInputStream fmime = new FileInputStream(partes[1]);
				d = Calendar.getInstance();
				cmpName = ""+d.get(Calendar.HOUR)+d.get(Calendar.MINUTE)+
						  d.get(Calendar.SECOND)+d.get(Calendar.MILLISECOND)+
						  "cmp.mme";
				FileOutputStream fmail = new FileOutputStream(cmpName);
			
				while ((c = fmime.read()) != -1)
					fmail.write(c);
			
				fmime.close();
				fmail.write((
					"MIME-Version: 1.0\r\n" +
					"Content-Type: multipart/mixed; boundary=\"composer-file-result\"\r\n\r\n" +
					"--composer-file-result\r\n" +
					"Content-Type: text/plain; charset=ISO-8859-1\r\n" +
					"Content-Transfer-Encoding: Base64\r\n\r\n").getBytes());			
				
				b64e = new BASE64Encoder();
				FileInputStream message = new FileInputStream(partes[2]);
				b64e.encodeBuffer(message,fmail);
				message.close();
				fmail.write("\r\n--composer-file-result\r\n".getBytes());
				String part_name;
				for (int i = 3; i < partes.length; i++) {
					pos = partes[i].lastIndexOf("\\");
					if (pos < 0)
						pos = partes[i].lastIndexOf("/");
					if (pos < 0)
						part_name = partes[i];
					else
						part_name = partes[i].substring(pos+1);
					fmail.write((
						"Content-Type: "+getContentType(part_name)+"; name=\""+part_name+"\"\r\n" +
						"Content-Transfer-Encoding: Base64\r\n" +
						"Content-Disposition: attachment; filename=\""+part_name+"\"\r\n\r\n"
						).getBytes());
					FileInputStream attach = new FileInputStream(partes[i]);
					b64e.encodeBuffer(attach,fmail);
					fmail.write("\r\n--composer-file-result".getBytes());
					if (i == (partes.length - 1))
						fmail.write("--\r\n".getBytes());
					else
						fmail.write("\r\n".getBytes());
					attach.close();
				}
				//fmail.write("\r\n.\r\n".getBytes());
				fmail.close();
				wr = new WorkResult(cmpName,false);
				setWorkResult(wr);
				return true;
			//} else {
			//	return false;
			//}
		} catch (Exception e) {
			return false;
		}
	}	
		
}
