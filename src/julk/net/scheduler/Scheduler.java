package julk.net.scheduler;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Date;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileWriter;

public class Scheduler implements Automata, Runnable
{
	private Hashtable<Integer,SchedulerProgram> programs;
	private Hashtable<Integer,SchedulerTask> running;
	private Hashtable<String, Object> objects;
	private Properties prop;
	private Thread t;
	
	public Scheduler ()
	{
		programs = new Hashtable<Integer,SchedulerProgram>();
		running = new Hashtable<Integer,SchedulerTask>();
		objects = new Hashtable<String, Object>();
		prop = null;
		t = new Thread(this,"Scheduler");
		t.setPriority(Thread.MIN_PRIORITY);
	}
	
	public void start ()
	{
		System.out.println("Iniciando el scheduler");
		t.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stop ()
	{
		t.stop();
	}
	
	@SuppressWarnings("deprecation")
	public void suspend ()
	{
		t.suspend();
	}
	
	@SuppressWarnings("deprecation")
	public void resume ()
	{
		t.resume();
	}
	
	public void config (Properties cfg)
	{
		prop = cfg;
	}
	
	public Properties config ()
	{
		return prop;
	}
	
	public Object getObject (String name)
	{
		return objects.get(name);
	}
	
	public void setObject (String name, Object obj)
	{
		objects.put(name,obj);	
	}
	
	public Integer getKey()
	{
		Integer k;

		for (int i = 0; true; i++) {
			k = new Integer(i);
			if (!programs.containsKey(k))
				return k;
		}
	}
	
	public void run ()
	{
		while (true) {
			try {
				while (size() == 0)
					Thread.sleep(1000);
				
				Enumeration<SchedulerProgram> e = programs.elements();
				SchedulerProgram schp;
				Date h = new Date();
				while (e.hasMoreElements()) {
					schp = (SchedulerProgram) e.nextElement();
					if (schp.isTheHour(h))
						new SchedulerTask(schp.key,schp);
				}
				Thread.sleep(500);
			} catch (Exception e) {}
		}
	}
	
	public synchronized void addProgram (SchedulerProgram schp)
	{
		System.out.println("A�adida nueva tarea al scheduler: "+schp.getClass().getName());
		if (schp.key==null)
			schp.setKey(getKey());
		programs.put(schp.key,schp);
	}
	
	public boolean addProgram (String className, String date, int seconds)
	{
		SchedulerProgram schp;
		try {
			schp = (SchedulerProgram) Class.forName(className).newInstance();
			Enumeration<String> e = objects.keys();
			String key;
			while (e.hasMoreElements()) {
				key = (String) e.nextElement();
				schp.putObject(key,getObject(key));
			}
			schp.setKey(getKey());
			schp.Init();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		//Ajustar las propiedades del programa
		if (date.equalsIgnoreCase("NOW"))
			schp.setTime(new Date());
		else
			schp.setTime(date);
		if (seconds > 0) {
			schp.setIncrement(Calendar.SECOND,seconds);
			schp.setRepeatable(true);
		}
		addProgram(schp);
		return true;
	}
	
	public synchronized boolean subProgram (Integer k)
	{
		if( programs.remove(k) != null)
			return true;
		else
			return false;
	}
		
	public synchronized boolean subProgram (String key)
	{
		try {
			return subProgram(new Integer(key));
		} catch (Exception e) {
			return false;
		}
	}
	
	private synchronized int size ()
	{
		return programs.size();
	}
		
	/*public String[] listPrograms ()
	{
		String[] res = new String[programs.size()];
		int n = 0, pos;
		Enumeration e = programs.elements();
		SchedulerTask st;
		SchedulerProgram schp;
		String clase;
		Calendar c;
		
		while (e.hasMoreElements()) {
			schp = (SchedulerProgram) e.nextElement();
			clase = schp.getClass().getName();
			pos = clase.lastIndexOf(".");
			clase = clase.substring(pos+1);
			c = schp.getTime();			
			res[n] = schp.key + "\t" + clase + "\t" +
					 c.get(Calendar.DAY_OF_MONTH) + "/" +
					 (c.get(Calendar.MONTH)+1) + "/" +
					 c.get(Calendar.YEAR) + " " +
					 c.get(Calendar.HOUR_OF_DAY) + ":" +
					 c.get(Calendar.MINUTE) + ":" +
					 c.get(Calendar.SECOND) + "\t" +
					 (schp.isRepeatable() ? "S" : "N") + " " +
					 schp.getInc() + "\t" +
					 schp.getIncFieldS() + " " +
					 (running.containsKey(schp.key) ? "Running" : "Stopped");
			n++;
		}
		return res;
	}*/
	
	public String listPrograms ()
	{
		StringBuffer res = new StringBuffer();
		int pos;
		Enumeration<SchedulerProgram> e = programs.elements();
		SchedulerProgram schp;
		String clase;
		Calendar c;
		res.append("Id      Clase           Detalles del programa\r\n");
		res.append("------------------------------------------------------------------------\r\n");
		while (e.hasMoreElements()) {
			schp = (SchedulerProgram) e.nextElement();
			clase = schp.getClass().getName();
			pos = clase.lastIndexOf(".");
			clase = clase.substring(pos+1);
			c = schp.getTime();			
			res.append(schp.key + "\t" + clase + "\t" +
					 c.get(Calendar.DAY_OF_MONTH) + "/" +
					 (c.get(Calendar.MONTH)+1) + "/" +
					 c.get(Calendar.YEAR) + " " +
					 c.get(Calendar.HOUR_OF_DAY) + ":" +
					 c.get(Calendar.MINUTE) + ":" +
					 c.get(Calendar.SECOND) + "\t" +
					 (schp.isRepeatable() ? "S" : "N") + " " +
					 schp.getInc() + "\t" +
					 schp.getIncFieldS() + " " +
					 (running.containsKey(schp.key) ? "Running" : "Stopped"));
			res.append("\r\n");

			/*res.append(StringFacility.rellena("" + schp.key,9," "));
			res.append(StringFacility.rellena(clase,17," "));
			res.append(StringFacility.rellena(
					   c.get(Calendar.DAY_OF_MONTH) + "/" +
					   (c.get(Calendar.MONTH)+1) + "/" +
					   c.get(Calendar.YEAR) + " " +
					   c.get(Calendar.HOUR_OF_DAY) + ":" +
					   c.get(Calendar.MINUTE) + ":" +
					   c.get(Calendar.SECOND),20," "));
			res.append((schp.isRepeatable() ? "S" : "N") + " ");
			res.append(StringFacility.rellena("" + schp.getInc(),10," "));
			res.append(schp.getIncFieldS() + " " +
					   (running.containsKey(schp.key) ? "Running" : "Stopped"));
			res.append("\r\n");*/
		}
		return res.toString();
	}
	
	public synchronized boolean load(String filename)
	{
		BufferedReader fProg;
		
		try {
			fProg = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException fnfe) {
			return false;
		}
		suspend();
		String line, clase, fecha, freq;
		StringTokenizer st;
		try {
			while ((line = fProg.readLine()) != null) {
				st = new StringTokenizer(line,"\t#");
				try {
					clase = st.nextToken();
					fecha = st.nextToken();
					freq = st.nextToken();
					if (!addProgram(clase,fecha,Integer.parseInt(freq))) {
						System.out.println("La clase "+clase+" no ha podido cargarse en el scheduler.");
					}
				} catch (NoSuchElementException nsee) {
				}
			}
			fProg.close();
		} catch (IOException e) {
			resume();
			return false;
		}
		resume();
		return true;
	}
	
	public synchronized boolean load ()
	{
		return load("schprog.cfg");
	}
	
	public synchronized boolean save (String filename)
	{
		PrintWriter fProg;
		
		try {
			fProg = new PrintWriter(new FileWriter(filename));
		} catch (IOException ioe) {
			return false;
		}
		suspend();
		Enumeration<Integer> e = programs.keys();
		Integer key; String clase, fecha, freq;
		SchedulerProgram schp; Calendar c;
		while (e.hasMoreElements()) {
			key = (Integer) e.nextElement();
			schp = (SchedulerProgram) programs.get(key);
			if (schp.getIncFieldI() != Calendar.SECOND)
				continue;
			clase = schp.getClass().getName();
			c = schp.getTime();
			fecha = "" + c.get(Calendar.DAY_OF_MONTH) + "/" +
					(c.get(Calendar.MONTH)+1) + "/" +
					c.get(Calendar.YEAR) + " " +
					c.get(Calendar.HOUR_OF_DAY) + ":" +
					c.get(Calendar.MINUTE) + ":" +
					c.get(Calendar.SECOND);
			freq = (schp.isRepeatable() ? "" + schp.getInc() : "0");
			fProg.println(clase + "\t" + fecha + "\t" + freq);
		}
		fProg.close();
		resume();
		return true;
	}
	
	public synchronized boolean save ()
	{
		return save("schprog.cfg");
	}
	
	public synchronized void clear()
	{
		suspend();
		programs.clear();
		resume();
	}

	private class SchedulerTask implements Runnable
	{
		private Thread t;
		private SchedulerProgram mySchP;
		public Integer key;
		
		public SchedulerTask ( Integer k, SchedulerProgram schp )
		{
			mySchP = schp;
			key = k;
			running.put(key,this);
			t = new Thread(this);
			t.setPriority(Thread.NORM_PRIORITY);
			t.start();
		}
		
		@SuppressWarnings("unused")
		public SchedulerProgram getTask ()
		{
			return mySchP;
		}
		
		public void run ()
		{
			System.out.println("Ejecutando programa del scheduler: "+mySchP.getClass().getName());
			mySchP.doLaunch();
			if (!mySchP.isRepeatable())
				programs.remove(key);
			running.remove(key);
		}
	}
}
