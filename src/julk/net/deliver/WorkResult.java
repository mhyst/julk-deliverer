package julk.net.deliver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Random;

public class WorkResult
{
	private String name;
	private String attach;
	private Random r;
	
	public WorkResult(String name)
	{
		this.name = name;
		attach = "S";
		r = new Random();
		r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
	}
	
	public WorkResult(String _name, boolean _attached)
	{
		this(_name);
		attach = (_attached ? "S" : "N");
	}
	
	private String getResponse (InputStream in)
	{
		String response = "";
		int c;
		try {
			while (in.available() == 0)
				Thread.sleep(100);
			while ((c = in.read()) != -1) {
				if (c == 13) {
					c = in.read();
					break;
				}
				response += (char) c;
			}
		} catch (Exception e) {}
		return response;
	}
		
	private void sendRequest (String cmd, OutputStream out)
	{
		if (cmd.length() == 0)
			return;
		try {
			out.write ((cmd + "\r\n").getBytes());
		} catch (Exception e) {
		}
	}
	
	public void receive(InputStream in,long count)
		throws Exception
	{
		name = getResponse(in);
		if (name.length() == 0)
			throw new Exception("No llega bien el nombre");
		int pos = name.lastIndexOf("/");
		if (pos != -1) name = name.substring(pos+1);
		pos = name.lastIndexOf("\\");
		if (pos != -1) name = name.substring(pos+1);
		attach = getResponse(in);
		if (attach.length() == 0)
			throw new Exception("No llega bien la propiedad attach");
		FileOutputStream fout = new FileOutputStream(name);
		dump(in,fout,count);
		fout.close();
	}
	
	public void receive(InputStream in)
		throws Exception
	{
		name = getResponse(in);
		if (name.length() == 0)
			throw new Exception("No llega bien el nombre");
		int pos = name.lastIndexOf("/");
		if (pos != -1) name = name.substring(pos+1);
		pos = name.lastIndexOf("\\");
		if (pos != -1) name = name.substring(pos+1);
		attach = getResponse(in);
		if (attach.length() == 0)
			throw new Exception("No llega bien la propiedad attach");
		//FileOutputStream fout = new FileOutputStream(name);
		String filename = "recv"+r.hashCode();
		FileOutputStream fout = new FileOutputStream(filename);
		dumpFile(in,fout);
		fout.close();
		File f = new File(name);
		f.delete();
		File f2 = new File(filename);
		f2.renameTo(f);
	}

	public void send(OutputStream out)
		throws Exception
	{
		FileInputStream in = new FileInputStream(name);
		String nombre = new String(name);
		int pos = nombre.lastIndexOf("\\");
		if (pos != -1) nombre = nombre.substring(pos+1);
		pos = nombre.lastIndexOf("/");
		if (pos != -1) nombre = nombre.substring(pos+1);
		
		sendRequest(nombre,out);
		sendRequest(attach,out);
		dumpFile(in,out);
		in.close();
	}
	
	private void dump(InputStream in, OutputStream out,long count)
		throws IOException
	{
		int c;
		BufferedInputStream bin;
		BufferedOutputStream bout;
		
		bin = new BufferedInputStream(in,4096);
		bout = new BufferedOutputStream(out,4096);
		for (int i = 0; i < count && (c = bin.read()) != -1; i++)		
			bout.write(c);
		bout.flush();
	}
	
	public static void dumpFile(InputStream in, OutputStream out)
		throws Exception
	{
		int c;
		BufferedInputStream bin;
		BufferedOutputStream bout;
		
		bin = new BufferedInputStream(in,4096);
		bout = new BufferedOutputStream(out,4096);
		
		for (;(c = bin.read()) != -1;)		
			bout.write(c);
		bout.flush();
	}
	
	public void dumpFileDCC(InputStream sin, InputStream in, OutputStream out)
		throws Exception
	{
		int c;
		BufferedInputStream bin;
		File f = new File(name);
		bin = new BufferedInputStream(in,1024);
		//bout = new BufferedOutputStream(out,1024);
		String res = "";
		
		long len = f.length();
		int slen;
		long packetLen;
		while (len > 0) {
			packetLen = (len < 1024 ? len : 1024);
			for (int i = 0; i < packetLen; i++) {
				/*if((slen = sin.available()) > 0) {
					byte[] buff= new byte[slen];*/
					//System.out.println("DCC SEND: "+slen+" bytes leidos");
					/*sin.read(buff);*/
					//res = new String(buff);
					//System.out.println("DCC SEND: "+res);
					/*int aux, ack=0;
					aux = buff[0];
					ack+=aux;
					aux = buff[1]<<8;
					ack+=aux;
					aux = buff[2]<<16;
					ack+=aux;
					aux = buff[3]<<24;
					ack+=aux;*/
					//System.out.println("DCC SEND:"+(int)buff[3]+" "+(int)buff[2]+" "+(int)buff[1]+" "+(int)buff[0]+" - "+ack);
				/*}*/
				c = bin.read();
				out.write(c);
			}
			len -= packetLen;
		}	
		if((slen = sin.available()) > 0) {
			byte[] buff= new byte[slen];
			sin.read(buff);
			System.out.println("DCC SEND:"+res);
		}
		Thread.sleep(5000);
		if((slen = sin.available()) > 0) {
			byte[] buff= new byte[slen];
			sin.read(buff);
			System.out.println("DCC SEND:"+res);
		}
	}
	public long size()
	{
		/*try {
			FileInputStream fin = new FileInputStream(name);
			int c, l = 0;
			while ((c = fin.read()) != -1)
				l++;
			fin.close();
			return l;
		} catch (Exception e) {
			return 0;
		}*/
		File f = new File(name);
		long l = f.length();
		return l;
	}

	public String getName()
	{
		return name;
	}
	
	public boolean isAttached()
	{
		return (attach.equalsIgnoreCase("S") ? true : false);
	}
}
