/** LinkExplorer
* 1. Leer una p�gina y buscar todos los enlaces
* 2. localizar los enlaces con mayor n�mero de ocurrencias
* 3. anotar todos los enlaces con su numero de ocurrencias
* 4. saltar al enlace con mayor numero de ocurrencias
* 5. Repetir desde el paso 1
* 6. Los resultados consistir�n en un archivo html
* 
* Este robot deber� restringir la profundidad 
* de exploraci�n, para no postergar los 
* resultados eternamente.
* 
* OBJETIVO: 
* Obtener un cluster de direcciones con informaci�n
* relevante sobre el tema que queramos.
* 
* NOTAS:
* 1. Ser�a interesante integrar estas funciones en
*    Deliverer como un traductor m�s.
*/ 
// de los t�rminos de b�squeda.
// Una p�gina con muchas ocurrencias de los t�rminos de 
// b�squeda, proporcionar�a m�s prioridad a los enlaces
// que contiene.

package julk.net.w3s;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

/**********************************************************************************
 * CLASS: LinksExplorer
 *
 * This class involves the main processing work of the search.
 * For each search you want to perform, you'll have to
 * create on instance of this class.
 *
 * @author Julio Cesar Serrano Ortuno
 * created November 26, 2007
 * updated December 15, 2007
 * license General Public License
 **********************************************************************************/
public class LinksExplorer {
	//Properties
	private String startingPage;			//The search will start on this URL
	private String[] searchTerms;			//the search terms
	
	private long cont;						//internal counter
	
	private LinksManager lm;				//Reference of our LinksManager
		
	private PrintWriter out;				//Reference for our outputstream
											//here will go all the search information
	
	private boolean cutBranches = false;	//An option flag
	private boolean firstTime = true;		//Is this the first call to processPage?
	
	private Properties setup;				//Search configuration properties
	
	private String filename;				//Filaname of the results file

	/**
	 * Instantiates an object of the class LinksExplorer
	 * in order to take account of every data related to the search.
	 * 
	 * @param link initial URL where starting the search
	 * @param terms search terms
	 * @param setup configuration properties (fill affect the search)
	 * @throws Exception
	 * 
	 * For the setup param, it must be created a Properties object
	 * and the following properties must be set using the method setProperty
	 * of the class Property:
	 * 	DEPTH: m�x depth to which read links (a string containing a int)
	 *  MAXENTRIES: m�s entries in the results page (a string containing a int)
	 *  ONLYINTERNAL: read only internal links ("TRUE" or "FALSE") 
	 *  ONLYEXTERNAL: read only external links ("TRUE" or "FALSE")
	 *  PRIORIZE: use priority to change the processing order ("TRUE" or "FALSE")
	 *  CUTBRANCHES: discards links from pages where the search terms were not found
	 *  
	 *  Example:
	 *  
	 *  Properties setup = new Properties();
	 *  setup.setProperty("PRIORIZE","TRUE");
	 *  
	 *  String[] terms = {"foo", "bar"};
	 *  LinksExplorer le = new LinksExplorer("http://www.foo.org", terms, setup);
	 *  
	 *  //All properties are set by default to false or 0
	 *  //so if you want to modify it, you'll have to
	 *  //add more "setProperty" lines.
	 * 
	 */
	public LinksExplorer (String link, String[] terms, Properties setup) throws Exception {
		startingPage = link;
		searchTerms = terms;
		cont = 0;
		this.setup = setup;
		lm = new LinksManager(setup);
		filename = setup.getProperty("FILENAME","salida.htm");
		out = new PrintWriter(new FileWriter(filename));
		cutBranches = getProperty("CUTBRANCHES");
	}
	
	/**
	 * Helps to retrieve properties 
	 * returning boolean values instead of strings
	 * 
	 * @param name the name of the desired property
	 * @return if the value is "FALSE" it returns false otherwise it returns true.
	 */
	private boolean getProperty(String name) {
		return setup.getProperty(name,"FALSE").equals("TRUE") ? true : false;
	}
	
	/**
	 * Helps to retrieve properties 
	 * returning int values instead of strings
	 * 
	 * @param name the name of the desired property
	 * @return the string property value parsed to int
	 */
	private int getIntProperty(String name) {
		String sValue = setup.getProperty(name,"0");
		int value;
		try {
			value = Integer.parseInt(sValue);
			return value;
		} catch (Exception e) {
			return 0;
		}
	}
		
	//This method allows the arranging of the dots in the screen
	//using the latter commented internal counter
	private boolean paginate() {
		if (cont > 180) {
			cont = 0;
			return true;
		} else {
			cont++;
			return false;
		}
	}	
	
	//Public Getters and Setters
	public boolean isCutBranches() {
		return cutBranches;
	}

	public String[] getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(String[] searchTerms) {
		this.searchTerms = searchTerms;
	}

	public String getStartingPAge() {
		return startingPage;
	}
	
	//Private getters and setters
	private LinksManager getLinkManager() {
		return lm;
	}

	private boolean isFirstTime() {
		return firstTime;
	}

	private void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}	

	/**
	 * Search for links in one line of text
	 * in case of finding, it submits the possible links
	 * to the LinksManager database
	 * 
	 * @param father father page
	 * @param line line on which search for links
	 */
	private void searchLinksInLine (Link father, String line) {
		int idx, idi, idf;
		
		String HREF = "href=";
		String link, title;
		String lowerCaseLine = line.toLowerCase();
		
		idx = lowerCaseLine.indexOf(HREF);
		while (idx != -1) {
			
			idf = line.indexOf(">",idx);
			if (idf == -1) {
				link = line.substring(idx+5);
				return;
			} else {
				link = line.substring(idx+5, idf);
			}
			if (link.startsWith("\"")) {
				link = link.substring(1,link.length()-1);
				int idef = link.indexOf("\"");
				if (idef != -1)
					link = link.substring(0,idef);
			}
			
			idi = idf;
			idf = lowerCaseLine.indexOf("</a>",idi);
			if (idf == -1) idf = lowerCaseLine.length();
			try {
				title = line.substring(idi+1, idf);
			} catch (Exception e) {
				idx = lowerCaseLine.indexOf(HREF, idx+1);
				continue;
			}
			if (title != null && title.length() == 0)
				title = "[Problem with the title]";

			lm.createLink(title, link, father);
			
			idx = lowerCaseLine.indexOf(HREF, idf);
		}				
	}
	
	/**
	 * Search for links in a web page
	 * It reads line by line and calls the previous function
	 * for each line
	 * @param hf HttpToFile oject represents the current page we're searching in
	 * @throws Exception
	 */
	private void searchForLinks(HttpToFile hf) throws Exception {
		BufferedReader in = hf.openFile();
		String line;
		while ((line = in.readLine())!= null) {
			try {
				searchLinksInLine(hf.getLink(), line);
			} catch (Exception e) {
				out.println("Error: "+e.getMessage());
			}
		}
		hf.closeFile();
	}
		
	/**
	 * This method is called everytime a page is to be processed
	 * For earch call, the method tries to locate the search terms
	 * in the page and then harvests links for later processing.
	 * 
	 * @param lnk the link to the page we're processing
	 * @param terms the search terms
	 * @throws Exception
	 */
	private void processPage(Link lnk, String[] terms) throws Exception {
		HttpToFile hf = new HttpToFile(lnk);
		int amount = 0;
		
		//Search for terms in the current page
		int[] ocurrences = hf.getOcurrences(terms);
		out.println("<tr><td><a target=_blank href=\""+lnk.getLink()+"\">"+lnk.getTitle()+"</a></td><td>"+lnk.getDepth()+"</td><td>");
		for (int i = 0; i < terms.length; i++) {
			if (ocurrences[i] > 0) {
				amount++;
			}
			out.println(terms[i]+": "+ocurrences[i]+"</br>");
		}
		out.println("</td></tr>");

		//Adjust priority
		if (amount > 0) { 
			lnk.incPriority(amount);
		} else {
			lnk.decPriority(1);
		}
		
		//Search for links in the current page
		if (isFirstTime() || amount > 0 || !isCutBranches()) {
			String ext = hf.extension();
			if (ext != null	&& 
			    (ext.endsWith("htm") ||
			     ext.endsWith("html") ||
			     ext.startsWith("php") ||
			     ext.equalsIgnoreCase("pl") ||
			     ext.equalsIgnoreCase("jsp"))) {
				
				searchForLinks(hf);
			}
		}		
	}
		
	/**
	 * Main searching method
	 * raises a search.
	 * 
	 * @param sw SwingWorker for communication with the GUI
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public void recursiveSearch (W3s.LinksExplorerWorker sw) throws Exception {
		///Results header
		out.println("<html><head><title>Results of searching throught "+startingPage+"</title></head><body>");
		out.println("<h2>W3S</h2><h3>Search start: "+startingPage+"</h3>");
		out.println("<h3>Search terms: </h3><ul>");		
		for (int i = 0; i < searchTerms.length; i++) {
			out.println("<li>"+searchTerms[i]+"</li>");
		}
		out.println("</ul><hr><table><tr><th>Links<th>Depth<th>Ocurrences</tr>");
		
		
		//Preparing to search the initial page
		//As it's the inicial page, doesn't have a father
		Link aux = new Link("Start page",startingPage,null);
		if (aux == null) {
			sw.doPublish("ERR:\nError processing start page.\n");
			return;
		}
		
		//Inform to the GUI of changes
		//So it can show the current page we're processing
		sw.doPublish(""+aux.getPriority()+"-"+aux.getLink());
		
		//We process the initial page
		//as it's first time, we shouldn't discard its links
		//even if "cutBranches" is activated
		processPage(aux,searchTerms);
		setFirstTime(false);
		
		//We retrieve some setup data
		//generally it will come from the GUI
		int depth = getIntProperty("DEPTH");
		int max = getIntProperty("MAXENTRIES");
		
		int id = 0;	//Result id (counter)
		
		//Get the follow link to process
		Link lnk = getLinkManager().next();
		
		//If it's not null, the work starts
		while (lnk != null) {
			
			//depth control
			int level = lnk.getDepth();
			if (level <= depth) {
				//Print the dot in the textfield
				sw.doPublish("ERR:.");
				
				//Helps dots to not go out from visual scope
				if (paginate()) sw.doPublish("ERR:\n");
				
				//Here goes the most impostant part
				//the processing
				try {
					sw.doPublish(""+lnk.getPriority()+"-"+lnk.getLink());
					processPage(lnk,searchTerms);
					id++;
				} catch (Exception e) {
				}
				
				//Max entries limit control
				if (max > 0) {
					if (id >= max) {
						out.println("Entries limit exceeded: "+max);
						out.println("</table>");
						out.println("</br><hr><i>Powered by W3S</i></body></html>");
						out.close();
						sw.doPublish("ERR:\nEntries limit exceeded: "+max);
						break;
					}
				}
				
			}
			
			//Again we get the next link in out database
			lnk = getLinkManager().next();
		}
		
		//End of results
		out.println("</table>");
		out.println("</br><hr><i>Powered by W3S</i></body></html>");
		out.close();
	}

	/**
	 * Main searching method
	 * raises a search.
	 * Exactly the same method as the previous, but without
	 * a swingworker attached.
	 * 
	 * @param sw SwingWorker for communication with the GUI
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public void recursiveSearch () throws Exception {
		///Results header
		out.println("<html><head><title>Results of searching throught "+startingPage+"</title></head><body>");
		out.println("<h2>W3S</h2><h3>Search start: "+startingPage+"</h3>");
		out.println("<h3>Search terms: </h3><ul>");		
		for (int i = 0; i < searchTerms.length; i++) {
			out.println("<li>"+searchTerms[i]+"</li>");
		}
		out.println("</ul><hr><table><tr><th>Links<th>Depth<th>Ocurrences</tr>");
		
		
		//Preparing to search the initial page
		//As it's the inicial page, doesn't have a father
		Link aux = new Link("Start page",startingPage,null);
		if (aux == null) {
			out.println("Error processing start page.<br>\n");
			return;
		}
		
		//So it can show the current page we're processing
		out.println(""+aux.getPriority()+"-"+aux.getLink()+"<br>\n");
		
		//We process the initial page
		//as it's first time, we shouldn't discard its links
		//even if "cutBranches" is activated
		processPage(aux,searchTerms);
		setFirstTime(false);
		
		//We retrieve some setup data
		//generally it will come from the GUI
		int depth = getIntProperty("DEPTH");
		int max = getIntProperty("MAXENTRIES");
		
		int id = 0;	//Result id (counter)
		
		//Get the follow link to process
		Link lnk = getLinkManager().next();
		
		//If it's not null, the work starts
		while (lnk != null) {
			
			//depth control
			int level = lnk.getDepth();
			if (level <= depth) {
				System.out.print(".");
				//Helps dots to not go out from visual scope
				if (paginate()) System.out.print("\n");
				
				//Here goes the most impostant part
				//the processing
				try {
					out.println(""+lnk.getPriority()+"-"+lnk.getLink()+"<br>\n");
					processPage(lnk,searchTerms);
					id++;
				} catch (Exception e) {
				}
				
				//Max entries limit control
				if (max > 0) {
					if (id >= max) {
						out.println("Entries limit exceeded: "+max);
						out.println("</table>");
						out.println("</br><hr><i>Powered by W3S</i></body></html>");
						out.close();
						out.println("\nEntries limit exceeded: "+max+"<br>\n");
						break;
					}
				}
				
			}
			
			//Again we get the next link in out database
			lnk = getLinkManager().next();
		}
		
		//End of results
		out.println("</table>");
		out.println("</br><hr><i>Powered by W3S</i></body></html>");
		out.close();
	}

	public PrintWriter getOut() {
		return out;
	}
}
