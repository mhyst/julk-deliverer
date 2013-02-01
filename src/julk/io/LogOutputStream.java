package julk.io;

import java.io.OutputStream;
import java.io.IOException;

public class LogOutputStream
{
	private OutputStream log;
	private OutputStream out;
	
	public LogOutputStream (OutputStream out, OutputStream log)
	{
		this.out = out;
		this.log = log;
	}
	
	public void close() throws IOException
	{
		log.close();
		out.close();
	}
	
	public void flush() throws IOException
	{
		out.flush();
	}
	
	public void write( int b ) throws IOException
	{
		if (log != null)
			log.write(b);
		out.write(b);
	}
	
	public void write( byte b[] ) throws IOException
	{
		if (log != null)
			log.write(b);
		out.write(b);
	}
	
	public void write( byte b[], int off, int len ) 
		throws IOException
	{
		if (log != null)
			log.write(b,off,len);
		out.write(b,off,len);
	}
}
