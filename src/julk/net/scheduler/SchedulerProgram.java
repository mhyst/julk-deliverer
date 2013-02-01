package julk.net.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Hashtable;

import julk.net.deliver.Deliverer;

public abstract class SchedulerProgram
{
	private Calendar at;
	private boolean repeat;
	private boolean running;
	private boolean ready;
	private int increase;
	private int increasedField;
	public Integer key;
	private Hashtable<String, Object> objects;
	protected Deliverer DELIVERER;

	public SchedulerProgram (Calendar c)
	{
		running = false;
		ready = true;
		at = c;
		//System.out.println("Nueva tarea para scheduler programada para "+at.getTime());
	}
			
	public SchedulerProgram(String fechahora)
		throws ParseException
	{
		this(DateFormat.getInstance().parse(fechahora));
	}
	
	public SchedulerProgram (Date d)
	{
		running = false;
		ready = true;
		at = Calendar.getInstance();
		at.setTime(d); 
		//System.out.println("Nueva tarea para scheduler "+Class.class.getName()+" programada para "+at.getTime());
	}
	
	public SchedulerProgram ()
	{
		running = false;
		ready = false;
		//System.out.println("Nueva tarea para scheduler programada para "+at.getTime());
	}
	
	protected boolean setDeliverer()
	{
		if (DELIVERER == null) {
			DELIVERER = (Deliverer) getObject("Deliverer");
			if (DELIVERER == null)
				return false;
			return true;
		} else {
			return true;
		}
	}	
	
	public abstract void Init();
	
	public void setKey (Integer k)
	{
		key = k;
	}
	
	public void putObject(String name, Object obj)
	{
		if (objects == null)
			objects = new Hashtable<String, Object>();
		objects.put(name,obj);
	}
	
	public Object getObject(String name)
	{
		if (objects == null)
			return null;
		return objects.get(name);
	}
	
	public boolean isTheHour(Date d)
	{
		Date myDate = at.getTime();
		boolean r = ((myDate.before(d) || myDate.equals(d)) && !running && ready);
		if (r)
			setRunning(true);
		return r;
	}
	
	public boolean isRepeatable()
	{
		return repeat;
	}
	
	public void setRepeatable ( boolean _repeat )
	{
		repeat = _repeat;
	}
	
	public void setIncrement (int _increasedField, int _increase)
	{
		increasedField = _increasedField;
		increase = _increase;
	}
	
	public void setRunning (boolean _running)
	{
		running = _running;
	}
	
	public boolean isRunning ()
	{
		return running;
	}
	
	public void setReady (boolean _ready)
	{
		ready = _ready;
	}
	
	public boolean isReady ()
	{
		return ready;
	}
	
	public void setTime (Calendar _at)
	{
		at = _at;
		ready = true;
	}
	
	public void setTime (Date _at)
	{
		at = Calendar.getInstance();
		at.setTime(_at);
		ready = true;
	}
	
	public void setTime (String _at)
	{
		try {
			setTime(DateFormat.getInstance().parse(_at));
		} catch (Exception e) {
			if (at == null)
				ready = false;
		}
	}
	
	public Calendar getTime ()
	{
		return at;
	}
	
	public String getIncFieldS ()
	{
		switch (increasedField) {
		case Calendar.SECOND:
			return "segungos";
		case Calendar.MINUTE:
			return "minutos ";
		case Calendar.HOUR_OF_DAY:
			return "horas   ";
		case Calendar.DAY_OF_MONTH:
			return "dias    ";
		case Calendar.MONTH:
			return "meses   ";
		case Calendar.YEAR:
			return "a�os    ";
		default:
			return "?�      ";
		}
	}
	
	public int getIncFieldI ()
	{
		return increasedField;
	}
	
	public int getInc()
	{
		return increase;
	}
		
	public void doIncrement ()
	{
		at.add (increasedField, increase);
		System.out.println("Preparado el programa "+this.getClass().getName()+" para volverse a ejecutar en "+at.getTime());
	}
	
	public void doLaunch()
	{
		if (!ready)
			return;
		setRunning(true);
		//System.out.println("Ejecutando programa del scheduler");
		if (repeat)
			doIncrement();
		else {
			setIncrement(Calendar.YEAR,1);
			doIncrement();
		}
		
		launch();
		setRunning(false);
	}
	
	protected abstract void launch ();
}
