package julk.net.deliver;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import julk.strings.StringFacility;

public class MappedServices
{
	private Hashtable<String, String> translators;
	private Deliverer DELIVERER;
	
	public MappedServices (Deliverer _d)
	{
		DELIVERER = _d;
		translators = new Hashtable<String, String>();
		loadMap ();
	}
	
	public Translator getTranslator(String service)
	{
		String SERVICE;
		int pos = service.indexOf("_");
		if (pos > 0)
			SERVICE = service.substring(0,pos).toUpperCase();
		else
			SERVICE = service.toUpperCase();
		String classTranslator = (String) translators.get(SERVICE);
		Translator tr;
		
		if (classTranslator == null)
			return new GenericTranslator(DELIVERER);
		
		try {
			tr = (Translator) Class.forName(classTranslator).newInstance();
			tr.setDeliverer(DELIVERER);
			return tr;
		} catch (Exception ce) {
			return new GenericTranslator(DELIVERER);
		}
	}
	
	public String getStringsMap ()
	{
		StringBuffer sb = new StringBuffer();
		String key, elem;
		
		Enumeration<String> services = translators.keys();

		sb.append(StringFacility.rellena("Servicio",31," "));
		sb.append("Clase\r\n");
		sb.append(StringFacility.rellena("",70,"-"));
		sb.append("\r\n");
		while (services.hasMoreElements()) {
			key = (String) services.nextElement();
			elem = (String) translators.get(key);
			sb.append(StringFacility.rellena(key,31,".") + elem + "\r\n");
		}
		return sb.toString();
	}
	
	public boolean loadMap (String filename)
	{
		BufferedReader fMap;
		
		try {
			fMap = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException fnfe) {
			return false;
		}
		
		String line,service,classTranslator;
		StringTokenizer st;
		try {
			while ((line = fMap.readLine()) != null) {
				st = new StringTokenizer(line," \t");
				try {
					service = st.nextToken().toUpperCase();
					classTranslator = st.nextToken();
					translators.put(service,classTranslator);
				} catch (NoSuchElementException nsee) {
				}
			}
			fMap.close();
		} catch (IOException ioe) {
			return false;
		}
		return true;
	}
	
	public boolean loadMap ()
	{
		return loadMap("mappedservices.cfg");
	}
	
	public boolean saveMap (String filename)
	{
		PrintWriter fMap;
		
		try {
			fMap = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		} catch (IOException e) {
			return false;
		}
		
		String service, classTranslator;
		Enumeration<String> e = translators.keys();
		while (e.hasMoreElements()) {
			service = (String) e.nextElement();
			classTranslator = (String) translators.get(service);
			fMap.println(StringFacility.rellena(service,30," ") + classTranslator);
		}
		fMap.close();
		return true;
	}
	
	public boolean saveMap ()
	{
		return saveMap("mappedservices.cfg");
	}
	
	public void clearMap ()
	{
		translators.clear();
	}
	
	public boolean set (String service, String classTranslator)
	{	
		try {
			Class.forName(classTranslator);
			translators.put(service.toUpperCase(),classTranslator);
			return true;
		} catch (Exception ce) {
			return false;
		}
	}
	
	public String get (String service)
	{
		return (String) translators.get(service.toUpperCase());
	}
	
	public boolean remove (String service)
	{
		if (translators.remove(service.toUpperCase()) != null)
			return true;
		else
			return false;
	}
}
