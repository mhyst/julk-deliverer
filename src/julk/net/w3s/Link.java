package julk.net.w3s;

/**********************************************************************************
 * CLASS: Link
 *
 * This class represents a link with all its relative
 * information. Once created every Link object gets fixed
 * via calling the method fixMe. That method turns the link in a
 * valid URL.
 * 
 *
 * @author Julio Cesar Serrano Ortuno
 * created November 26, 2007
 * updated December 15, 2007
 * license General Public License
 **********************************************************************************/
public class Link {
	private String site;	//site to wich belongs the link
							//This is not exact
	private String title;	//link title
	private String link;	//link itself
	private Link father;	//page from which this link
							//was extracted.
	private boolean alreadyVisited; //was this link already visited? -> LinksManager
	private int priority;	//link priority
							//Only used when the option priorize is checked
	

	/**
	 * CONSTRUCTOR
	 * 
	 * In order to create an object of the Link class
	 * you have to give the arguments: title, link and father.
	 * 
	 * This constructor generally is invoqued from
	 * LinksManager.createLink method.
	 * 
	 * @param title		title extracted from the tags <a href> 
	 * @param link	 	the URL corresponding to the link you want to create
	 * @param father	a Link object that represents the page from which this link was taken.
	 * 					Usefull to calculate the link depth.
	 * @return a new object of the Link class
	 * @see LinksManager#createLink(String, String, Link)
	 * 
	 * <pre>
	 * 
	 * Example:
	 * 		Reading some far page...
	 * 		...<a href="http://www.searchlores.org">Search Lores</a>...
	 * 
	 * 		Link lnk = new Link("Search Lores", "http://www.searchlores.org",null);
	 * 
	 * </pre>
	 */
	public Link(String title, String link, Link father) {
		if (father == null) {
			site = "";
		} else {
			this.site = father.getLink();
		}
		this.title = title;
		this.link = link;
		this.father = father;
		
		if (father != null)
			priority = father.getPriority();
		else
			priority = 0;
		
		if (isValid()) fixMe();
	}
	
	//All this getters and setters are of help
	//when the "priorize" option is checked
	//In such case, the property "priority"
	//is used to determine the importance
	//of a page in the current search.
	
	//Getter for the property "priority"
	public int getPriority() {
		return priority;
	}

	//Setter for the property "priority"
	public void setPriority(int priority) {
		this.priority = priority;
	}

	//Setter for the property "priority"
	//Incremets "priority" by 1
	public void incPriority() {
		this.priority++;
	}

	//Setter for the property "priority"
	//Increments "priority" by amount
	public void incPriority(int amount) {
		this.priority+=amount;
	}
	
	//Setter for the property "priority"
	//Decrements "priority" by 1
	public void decPriority() {
		this.priority--;
	}

	//Setter for the property "priority"
	//Decrements "priority" by amount
	public void decPriority(int amount) {
		this.priority-=amount;
	}
	
	/**
	 * 
	 * @return a string containing the URL of the server main page
	 * 
	 * <pre>
	 * Example:
	 * 		if provided: http://www.searchlores.org/basic.htm
	 * 		returns: http://www.searchlores.org
	 * </pre>
	 */
	private String getRoot() {
		int id = site.indexOf("/",7);
		if (id == -1) {
			return site;
		} else {
			return site.substring(0, id+1);
		}
	}

	/**
	 * 
	 * @param url URL to be processed
	 * @return the number of bars(/) in the given URL
	 */
	private int getNumBars(String url) {
		byte[] c = url.getBytes();
		int cont = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '/') {
				cont++;
			}
		}
		return cont;
	}
	
	/**
	 * 
	 * @return the URL of the folder in which the current link is located
	 * 
	 * <pre>
	 * Example:
	 * 		provided: http://www.foo.org/folder/bar.htm
	 * 		returns: http://www.foo.org/folder
	 * </pre>
	 */
	private String getLastFolder() {
		if ((site.charAt(site.length()-1) == '/') && 
			(getNumBars(site) <= 3)) {
			return site;
		}
		int id = site.substring(7).lastIndexOf("/");
		if (id == -1) {
			site = site + "/";
			return site;
		} else {
			return site.substring(0, site.lastIndexOf("/"));
		}
	}

	/**
	 * This method is slightly different from previous.
	 * besides attending to internal properties, it also
	 * takes accoubt of the given URL. 
	 * 
	 * @param URL URL to be processed
	 * @return the URL given, turned into a complete and valid URL.
	 * 
	 *  <pre>
	 *  Example:
	 *  	provided:
	 *  		site = http://www.foo.org/bar 
	 *  		URL = ../foo.htm
	 *  	returns
	 *  		http://www.foo.org/foo.htm
	 * 	<pre>
	 */
	private String getLastFolder(String URL) {
		String finalURL = "";
		int n = 0;
		finalURL = URL;
		while (finalURL.startsWith("../")) {
			n++;
			finalURL = URL.substring(3);
		}
		if (n > 0) {
			String oldSite = site;
			site = getLastFolder();
			for (int i = 0; i < n; i++) {
				site = site.substring(0,site.length()-1);
				site = getLastFolder();
			}
			String folder = this.site;
			site = oldSite;
			if (folder.indexOf("/",7) == -1)
				return folder+"/"+finalURL;
			else
				return folder+finalURL;
		} else {
			int id = site.lastIndexOf("/");
			if (id == -1) {
				return site+URL;
			} else {
				return site.substring(0, id+1)+URL;
			}
		}
	}

	/**
	 * Most links found in href tags are incomplete
	 * there are many web designiers that use relative
	 * links. That could be a nightmare for our
	 * harvested links, so before using a link, just in
	 * the momment of its creation he's able to fix himself
	 * with this method. For this task it uses all the 
	 * information at his scope: avobe all, parent link.
	 * 
	 * This method fix the link to turn it
	 * into a complete and valid URL. Invalid links
	 * are discarded by this method.
	 * 
	 * Notwithstanding, much work has still to be made
	 * to make this method perfect.
	 */
	private void fixMe() {
		int idx;
		
		if (link.startsWith("mailto:")) {
			link = null;
			return;
		}
		
		/*if (!title.equalsIgnoreCase("inicial") &&
			!title.equalsIgnoreCase("initial") &&
			!title.equalsIgnoreCase("Start page")) {
			if (lm.isOnlyExternal()) {
				if (!link.toLowerCase().startsWith("http://")) {
					link = null;
					return;
				}
			} 
			if (lm.isOnlyInternal()) {
				if (link.toLowerCase().startsWith("http://")) {
					link = null;
					return;
				}
			}
		}*/
		
		//Desechar referencias relativas #ref
		idx = link.indexOf("#");
		if (idx != -1) {
			link = link.substring(0,idx);
		}

		if (!link.toLowerCase().startsWith("http://")) {
			if (link.startsWith("/")) {
				link =  getRoot()+link;
			} else {
				link =  getLastFolder(link);
			}			
		} else {
			idx = link.substring(7).lastIndexOf("/"); 
			if (idx == -1) {
				link = link+"/";
			}
		}
	}
	
	/**
	 * Uses the parents informations (working as a real list)
	 * to calculate the depth to be considered for this link.
	 * 
	 * @return the depth of this link
	 */
	public int getDepth() {
		Link lnk = getFather();
		int level = 0;
		while (lnk != null) {
			level++;
			lnk = lnk.getFather();
		}
		return level;
	}
	
	/**
	 * @return property link
	 */
	public String getLink() {
		return link;
	}
		
	/**
	 * @return property site
	 */
	public String getSite() {
		return site;
	}
		
	/**
	 * @return property title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return property father
	 */
	public Link getFather() {
		return father;
	}

	/**
	 * @return property alreadyVisited
	 */
	public boolean isAlreadyVisited() {
		return alreadyVisited;
	}

	/**
	 * Setter for already visited
	 * @param alreadyVisited
	 */
	public void setAlreadyVisited(boolean alreadyVisited) {
		this.alreadyVisited = alreadyVisited;
	}
	
	/**
	 * @return true if the link is valid
	 */
	public boolean isValid() {
		return link == null ? false : true;
	}
}
