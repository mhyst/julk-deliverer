package julk.strings;

public class StringFacility
{
	public static String rellena(String cad, int len, String r)
	{
		if (cad.length() > len)
			return cad.substring(0,len);
		int n = len - cad.length();
		String res = new String(cad);
		for (int i = 0; i < n; i++)
			res += r;
		
		return res;
	}
	
	public static String recorta(String clase)
	{
		int pos = clase.lastIndexOf(".");
		
		return clase.substring(pos+1);
	}
}
