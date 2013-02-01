package julk.net.scheduler;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 */
public class CodeAllocation
{
	private Hashtable<Integer, Code> registeredCodes;
	private int lastAssignedCode;
	
	public CodeAllocation ()
	{
		registeredCodes = new Hashtable<Integer, Code>();
		lastAssignedCode = -1;
	}
	
	private synchronized Hashtable<Integer, Code> getUnusedCodes ()
	{
		Code code;
		Hashtable<Integer, Code> unusedCodes = new Hashtable<Integer, Code>();
		Enumeration<Code> e = registeredCodes.elements();
		
		while (e.hasMoreElements()) {
			code = (Code) e.nextElement();
			if (!code.inUse)
				unusedCodes.put(new Integer(code.code),code);
		}
		return unusedCodes;
	}
	
	private synchronized Hashtable<Integer, Code> getUsedCodes ()
	{
		Code code;
		Hashtable<Integer, Code> usedCodes = new Hashtable<Integer, Code>();
		Enumeration<Code> e = registeredCodes.elements();
		
		while (e.hasMoreElements()) {
			code = (Code) e.nextElement();
			if (code.inUse)
				usedCodes.put(new Integer(code.code),code);
		}
		return usedCodes;
	}

	private Code getMinorCode (Hashtable<Integer, Code> from)
	{
		Enumeration<Integer> e = from.keys();
		Integer menor, elem;
		
		if (e.hasMoreElements())
			menor = (Integer) e.nextElement();
		else
			return null;
		while (e.hasMoreElements()) {
			elem = (Integer) e.nextElement();
			if (elem.intValue() < menor.intValue())
				menor = elem;
		}
		return (Code) from.get (menor);
	}
	
	public String getCode()
	{
		Code code;
		Hashtable<Integer, Code> unusedCodes = getUnusedCodes();
		
		if (unusedCodes.isEmpty()) {
			lastAssignedCode++;
			code = new Code(lastAssignedCode);
			registeredCodes.put(new Integer(lastAssignedCode), code);
		} else {
			code = getMinorCode(unusedCodes);
		}
		return code.getId();
	}
	
	public void returnCode(String id)
		throws Exception
	{
		int pos, intCode;
		
		pos = id.indexOf("-");
		
		String sid = id.substring(0,pos);
		intCode = Integer.parseInt(sid,16);
		Code code = (Code) getUsedCodes().get(new Integer(intCode));
		if (code == null)
			throw new Exception("No se encuentra ese c�digo");
		code.inUse = false;
	}
	
	private class Code
	{
		public boolean inUse;
		public int code;
		public int usedTimes;
		
		public Code (int _code)
		{
			inUse = false;
			usedTimes = -1;
			code = _code;
		}
		
		public String getId ()
		{
			inUse = true;
			usedTimes++;
			return Integer.toHexString(code) + "-" + usedTimes;
		}
		
		@SuppressWarnings("unused")
		public void ret ()
		{
			inUse = false;
		}
	}
	
	public static void main (String[] args)
		throws Exception
	{
		CodeAllocation ca = new CodeAllocation();
		
		//Se asignan los 20 primeros n�meros por primera vez
		for (int i = 0; i < 20; i++)
			System.out.println("C�digo asignado: "+ca.getCode());
		
		//Se devuelven todos menos uno
		for (int i = 0; i < 19; i++)
			ca.returnCode("" + Integer.toHexString(i) + "-x");

		//Se asignan otros 20 n�meros
		for (int i = 0; i < 20; i++)
			System.out.println("C�digo asignado: "+ca.getCode());
	}
}
