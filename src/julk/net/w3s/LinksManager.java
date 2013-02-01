
package julk.net.w3s;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**********************************************************************************
 * CLASS: LinksManager
 *
 * This class is the links' master and because of that
 * it has to manage every harvested link.
 *  
 * Its responsabilities are:  
 *  -Add a link to the links database
 * 	-Provide the next link to be processed
 *  -Being aware of already visited links
 *  -Being aware of links already introduced in the database
 * 
 * @author Julio Cesar Serrano Ortuno
 * 
 * created November 26, 2007
 * updated December 15, 2007
 * license General Public License
 **********************************************************************************/
public class LinksManager {

	//Links database
	private Hashtable<String, Link> links;
	private Vector<Link> pendientes;
	private Vector<Link> visitados;
	
	//Management options
	private boolean onlyInternal = false;
	private boolean onlyExternal = false;
	private boolean priorize = false;
	private Properties setup;
	
	//Minimal priority to be processed for a link
	public final int MINPRIORITY = -3;
	
	/**
	 * This class has to be instantiated only once
	 * @param setup management options
	 */
	public LinksManager(Properties setup) {
		links = new Hashtable<String, Link>();
		pendientes = new Vector<Link>();
		visitados = new Vector<Link>();
		this.setup = setup;
		onlyInternal = getProperty("ONLYINTERNAL");
		onlyExternal = getProperty("ONLYEXTERNAL");

		if (onlyInternal && onlyExternal)
			onlyInternal = onlyExternal = false;
		
		priorize = getProperty("PRIORIZE");
	}
	
	/**
	 * Creates a link and stores it in the
	 * link's database.
	 * 
	 * @param title a title for the new link
	 * @param link the URL for the new link
	 * @param father the father of the newly created link
	 */
	public void createLink(String title, String link, Link father) {
		
		//Is a external link?
		boolean external = link.toLowerCase().startsWith("http://");
		
		//It follow the current directives?
		if (onlyInternal && external) {
			return;
		}
		
		if (onlyExternal && !external) {
			return;
		}
		
		//If yes, then create and submit the link
		Link lnk = new Link(title,link,father);
		if (lnk.isValid()) {
			addLink(lnk);
		}			
	}
	
	/**
	 * Helps to retrieve properties 
	 * returning boolean values instead of strings
	 * 
	 * @param name the name of the desired property
	 * @return if the value is "FALSE" it returns false otherwise it returns true.
	 */
	private boolean getProperty(String name) {
		return setup.getProperty(name,"FALSE").equalsIgnoreCase("TRUE") ? true : false;
	}	
	
	/**
	 * Queries the main database to see if we've already
	 * seen this link before.
	 * @param lnk the link to be consulted
	 * @return true or false depending on if the link is already in the database
	 */
	private boolean alreadySeen(Link lnk) {
		return (links.get(lnk.getLink()) != null);
	}
	
	/**
	 * Enters a link in the database
	 * @param lnk the link to be entered
	 * @return true if entered and false if not entered
	 */
	private boolean addLink(Link lnk) {
		
		if (!alreadySeen(lnk) && lnk.isValid()) {
			links.put(lnk.getLink(), lnk);
			pendientes.addElement(lnk);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method only provide a "next link to be
	 * processed" if the option "priorize" is activated.
	 * Search the database for the link with max 
	 * priority.
	 *
	 * @return the next link to be processed
	 */
	private Link prioritary() {
		Enumeration<Link> e = pendientes.elements();
		int majorPriority = MINPRIORITY;
		Link majorLink = null;
		
		while (e.hasMoreElements()) {
			Link lnk = e.nextElement();
			if (lnk.getPriority() < MINPRIORITY) {
				pendientes.removeElement(lnk);
				continue;
			}
			if (lnk.getPriority() > majorPriority &&
				!lnk.isAlreadyVisited()) {
				majorPriority = lnk.getPriority();
				majorLink = lnk;
			}
		}
		
		if (majorLink == null) return null;
		
		majorLink.setAlreadyVisited(true);
		visitados.addElement(majorLink);
		pendientes.removeElement(majorLink);
		return majorLink;
	}
	
	/**
	 * This is the official method to obtain
	 * the next link that is going to be processed.
	 * 
	 * If the flag "priorize" is set, it will call
	 * the last method "prioritary()".
	 * Otherwise, it goes on and select the next
	 * link by harvesting order.
	 * 
	 * @return the next link to be processed.
	 */
	public Link next() {
		Enumeration<Link> e = pendientes.elements();
		Link lnk;
		
		if (priorize) {
			return prioritary();
		}
		if (e.hasMoreElements()) {
			//key = (String) e.nextElement();
			lnk = (Link) e.nextElement();
			while (lnk.isAlreadyVisited() && e.hasMoreElements()) {
				//key = (String) e.nextElement();
				lnk = e.nextElement();				
			}
			if (lnk.isAlreadyVisited()) {
				return null;
			} else {
				lnk.setAlreadyVisited(true);
				visitados.addElement(lnk);
				pendientes.removeElement(lnk);
				return lnk;
			}
		} else {
			return null;
		}
	}
}
