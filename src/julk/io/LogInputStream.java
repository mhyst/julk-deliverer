package julk.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class LogInputStream extends InputStream
{
	private OutputStream log;
	private InputStream in;
	
	public LogInputStream(InputStream in, OutputStream log)
	{
		this.in = in;
		this.log = log;
	}
	
	public int available() throws IOException
	{
		return in.available();
	}
	
	public void close() throws IOException
	{
		log.close();
		in.close();
	}
	
	public synchronized void mark( int readlimit )
	{
		in.mark ( readlimit );
	}
	
	public boolean markSupported()
	{
		return in.markSupported();
	}
	
	public int read() throws IOException
	{
		int c = in.read();
		log.write(c);
		return c;
	}
	
	public int read( byte b[] ) throws IOException
	{
		int c = in.read(b);
		log.write(b);
		return c;
	}
	
	public int read( byte b[], int off, int len ) throws IOException
	{
		int c = in.read(b,off,len);
		log.write(b,off,len);
		return c;
	}
	
	public synchronized void reset() throws IOException
	{
		in.reset();
	}
	
	public long skip( long n ) throws IOException
	{
		return in.skip(n);
	}
}
