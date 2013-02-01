package julk.net.deliver;

import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Calendar;

public abstract class Translator
{
	private String nextStep;
	private WorkResult wr;
	public Deliverer DELIVERER;
	
	public void setDeliverer(Deliverer theDeliverer)
	{
		DELIVERER = theDeliverer;
	}
	
	public Deliverer getDeliverer ()
	{
		return DELIVERER;
	}
	
	public void setWorkResult(WorkResult _wr)
	{
		wr = _wr;
	}
	
	public WorkResult getWorkResult ()
	{
		return wr;
	}
	
	public boolean doTranslation(WorkItem wi)
	{
		try {
			String cmd = parseCommand(nextTwice(wi.getCommand()));
			boolean b = translate(wi.getUser(),wi.getService(),cmd,wi.getWorkResult());
			doNextStep(wi);
			return b;
		} catch (Exception e) {
			System.out.println("Fallo al ejecutar el trabajo");
			return false;
		}
	}
	
	protected abstract boolean translate (String user, String service,
										  String command, WorkResult wr);
	
	private final void doNextStep (WorkItem wi)
	{
		String oldNextStep = nextStep;
		String cmd = nextTwice(nextStep);
		String command = oldNextStep, service = parseService(cmd);
		
		if (cmd.length() > 0) {
			WorkItem wis;
			if (wr != null)
				wis = new WorkItem(wi.getUser(),service,command,wr);
			else
				wis = new WorkItem(wi.getUser(),service,command);
			if (DELIVERER.accepts(service))
				DELIVERER.add(wis);
			else {
				try {
					RemoteWorkQueue	rwq = new RemoteWorkQueue(DELIVERER.getDefaultDeliverer(),"several");
					rwq.add(wis);
				} catch (Exception e) {
					System.out.println("Error al someter trabajo");
				}
			}
		}
	}
	
	private final String parseService(String twice)
	{
		int pos;
		
		pos = twice.indexOf("#");
		
		if (pos == -1)
			return "";
		return twice.substring(0,pos);
	}
	
	private final String parseCommand(String twice)
	{
		int pos;
		
		pos = twice.indexOf("#");
		
		if (pos == -1)
			return "";
		return twice.substring(pos+1);
	}

	private final String nextTwice (String command)
	{
		try {
			StringTokenizer st = new StringTokenizer(command,"#[]");
			String[] cmd = new String[st.countTokens() / 2];
			String[] serv = new String[cmd.length];
			
			for (int i = 0; st.hasMoreTokens(); i++) {
				serv[i] = st.nextToken();
				cmd[i] = st.nextToken();
			}

			nextStep = "";
			for (int i = 1; i < cmd.length; i++)
				nextStep += serv[i]+"#["+cmd[i]+"]";
			return serv[0]+"#"+cmd[0];
		} catch (Exception e) {
			return "";
		}
	}
	
	private String tmpFileName ()
	{
		String cmpName;
		Calendar d;
		
		d = Calendar.getInstance();
		cmpName = ""+d.get(Calendar.HOUR)+d.get(Calendar.MINUTE)+
				  d.get(Calendar.SECOND)+d.get(Calendar.MILLISECOND)+
				  "dtmp.mme";
		return cmpName;
	}
	
	private PrintWriter createOutput (String filename)
	{
		try {
			FileOutputStream fout = new FileOutputStream(filename);
			return new PrintWriter(fout);
		} catch (IOException ioe) {
			return null;
		}
	}

	public void feedback (String msg)
	{
		//Enviamos resultado
		String fname = tmpFileName();
		PrintWriter out = createOutput(fname);
		if (out == null) return;
		out.print(msg);
		out.close();
		WorkResult wr = new WorkResult(fname, false);
		setWorkResult(wr);
		//****************
	}
}
