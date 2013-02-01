package julk.net.deliver;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Calendar;
import java.util.Date;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import julk.io.LogOutputStream;
import julk.net.scheduler.*;
import julk.strings.StringFacility;
import julk.net.proxy.SocksProxy;

public class Deliverer extends Queue implements Runnable
{
	private Hashtable<String, Queue> queueList;
	private Hashtable<String, DelivererClient> clients;
	private Hashtable<String, SocksProxy> proxies;
	private ServerSocket ss;
	private Thread t;
	private Deliverer thisDeliverer;
	private Scheduler scheduler;
	private Registry registro;
	private String defaultDeliverer;
	private LogOutputStream lout;
	private String password;
	private MappedServices serviceMap;
	private MailConfig mail;
	
	public Deliverer (String defDeliverer)
	{
		//Log Deliverer
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("julk_net_deliver_Deliverer.log");
		} catch (IOException ioe) {}
		lout = new LogOutputStream(System.out,fout);
		
		//Inicializar variables de instancia
		defaultDeliverer = defDeliverer;
		queueList = new Hashtable<String,Queue>();
		proxies = new Hashtable<String, SocksProxy>();
		thisDeliverer = this;
		serviceMap = new MappedServices(this);
		mail = new MailConfig();
		
		//Mostrar log inicio
		log("Iniciando log - "+new java.util.Date()+"\r\n");
		log("Deliverer por defecto - "+defaultDeliverer+"\r\n");
		
		setupScheduler();	//Inicia programador de tareas
		loadQueues ();		//Inicia las colas
		
		//Carga los programas del scheduler internos
		scheduleDefaultPrograms();
		
		putOnline();		//Pone el deliverer en linea
		password = "frodo"; //Contrase�a del administrador
		
		//Iniciamos la hebra principal
		t = new Thread(this);
		t.start();
	}
	
	public MappedServices getServiceMap() {
		return serviceMap;
	}
	
	/**Obtener la direcci�n del deliverer principal
	 */
	public String getDefaultDeliverer ()
	{
		return defaultDeliverer;
	}
	
	/**Obtener la direcci�n del deliverer principal
	 */
	public InetAddress getDefaultDelivererAddress ()
	{
		try {
			return InetAddress.getByName(defaultDeliverer);
		} catch (UnknownHostException uhe) {
			return null;
		}
	}
	
	/**Referencia al programador de tareas local
	 */
	public Scheduler getScheduler ()
	{
		return scheduler;
	}
	
	/**Referencia al registro de nombres local
	 */
	public Registry getRegistry ()
	{
		return registro;
	}
	
	/**Referencia al contenedor de colas
	 */
	public Hashtable<String, Queue> getQueueList ()
	{
		return queueList;
	}
	
	public MailConfig getMailConfig ()
	{
		return mail;
	}
	
	/**Reinicia el contenedor de colas
	 */
	public void reloadQueues()
	{
		log("Procediendo a reconfigurar todas las colas...\r\n");
		queueList.clear();
		loadQueues();
		log("Echo\r\n");
	}
	
	/**A�ade colas configuradas desde el archivo filename
	 */
	public boolean loadQueues(String filename)
	{
		BufferedReader cfg = null;
		try {
			cfg = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException fnfe) {
			log("El archivo de configuraci�n no existe - queues.cfg\r\n");
			return false;
		}
		
		String line, queueName, queueService, queueFlag;
		int rl = 5;
		boolean remote;
		StringTokenizer st;	
		
		try {
			while ((line = cfg.readLine()) != null && line.length() > 0) {
				try {
					st = new StringTokenizer(line,"#\t, ");
					queueName =  st.nextToken();
					queueService = st.nextToken();
					queueFlag = st.nextToken();
					if (queueFlag.toUpperCase().startsWith("C")) {
						try {
							rl = Integer.parseInt(st.nextToken());
						} catch (Exception ee) {
							rl = 5;
						}
						addCiclicQueue(queueName,queueService,rl);
					} else {
						remote = queueFlag.equalsIgnoreCase("R");
						addQueue(queueName,queueService,remote);
					}
				} catch (NoSuchElementException nsee) {
					log ("Error de sintaxis en queues.cfg\r\n");
				}
			}
			cfg.close();
		} catch (IOException e) {
			log("Error al leer el archivo de configuraci�n - queues.cfg\r\n");
			return false;
		}
		return true;
	}
	
	/**A�ade colas desde el archivo queues.cfg
	 */
	public boolean loadQueues ()
	{
		return loadQueues("queues.cfg");
	}
	
	/**Limpia el contenedor de colas
	 */
	public void clearQueues ()
	{
		queueList.clear();
	}
	
	public boolean saveQueues (String filename)
	{
		PrintWriter fQ;
		
		try {
			fQ = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		} catch (IOException ioe) {
			return false;
		}
		
		String name, service, type, retries;
		Queue q;
		Enumeration<String> e = queueList.keys();
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			q = (Queue) queueList.get(name);
			service = q.Service();
			if (q.getClass().getName().endsWith(".WorkQueue")) {
				type = "l";
				retries = "";
			} else if (q.getClass().getName().endsWith(".RemoteWorkQueue")) {
				type = "R";
				retries = "";
			} else if (q.getClass().getName().endsWith(".CiclicWorkQueue")) {
				type = "C";
				retries = "" + ((CiclicWorkQueue) q).getRetriesLimit();
			} else {
				continue;
			}
			fQ.println(StringFacility.rellena(name,30," ") +
					   StringFacility.rellena(service,30," ") +
					   StringFacility.rellena(type,5," ") +
					   ((retries.length() > 0) ? (retries) : ""));
		}
		fQ.close();
		return true;
	}
	
	public boolean saveQueues ()
	{
		return saveQueues("queues.cfg");
	}
	
	private void setupScheduler()
	{
		//Arrancar el programador de tareas local
		scheduler = new Scheduler();
		scheduler.setObject("Deliverer",this);
		scheduler.load();
		scheduler.start();
		//Fin de la carga
		
		//Creando registro de nombres para almacenar
		//trabajos de �nico sometimiento
		registro = new Registry();
	}
	
	public synchronized void loadQueueRecovery ()
	{
		if (registro.isRegistered("QueueRecovery"))
			return;
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MINUTE,5);
		SchedulerProgram sch = new QueueRecovery();
		sch.putObject("Deliverer",this);
		sch.setTime(c);
		sch.setIncrement(Calendar.MINUTE,5);
		sch.setRepeatable(true);
		scheduler.addProgram(sch);
		registro.register("QueueRecovery");
	}
	
	private void scheduleDefaultPrograms()
	{
		//Primer programa:
		//Revisi�n de colas
		if (registro.isRegistered("QueueTrace"))
			return;
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MINUTE,15);
		SchedulerProgram sch = new QueueTrace();
		sch.putObject("Deliverer",this);
		sch.setTime(c);
		sch.setIncrement(Calendar.MINUTE,15);
		sch.setRepeatable(true);
		scheduler.addProgram(sch);
		registro.register("QueueTrace");
	}
	
	public boolean addQueue (String _name, String _serviceType,
							 boolean remote)
	{
		log("Petici�n - a�adir cola "+(remote ? "remota" : "local")+" "+_name+":"+_serviceType+"\r\n");
		Queue q = (Queue) queueList.get(_name);
		if (q != null) {
		/*Enumeration e = queueList.elements();
		Queue qh = null;
		while (e.hasMoreElements()) {
			qh = (Queue) e.nextElement();
			if (qh.name.equals(_name)) {*/
				log("No se puede a�adir, ya hay una con ese nombre\r\n");
				return false;
			//}
		}
		Queue wq = null;
		if (remote)
			try {
				wq = new RemoteWorkQueue(_name,_serviceType,thisDeliverer);
			} catch (Exception errsocket) {
				log("Error al crear la cola remota " + errsocket.getMessage() + "\r\n");
				return false;
			}
		else {
			wq = new WorkQueue(_name,_serviceType);
		}
		Translator t = null;
		try {
			//t = new GenericTranslator(this);
			t = serviceMap.getTranslator(_serviceType);
		} catch (Exception errt) {
			log("Atenci�n - cola "+_name+" vinculada a un traductor nulo\r\n");
		}
		WorkDispatcher wd;
		/*if (t == null) {
			wd = new WorkDispatcher(null);
			log("Atenci�n - cola "+_name+" vinculada a un traductor nulo\r\n");
		} else*/
			wd = new WorkDispatcher(t);
		wq.addObserver (wd);
		wq.name = new String(_name);
		wq.serviceType = new String(_serviceType);
		queueList.put(_name,wq);
		log("Cola "+_name+" creada y preparada para atender trabajos del tipo "+_serviceType+"\r\n");
		return true;
	}
	
	/*
	private boolean addQueue (String _name, String _serviceType)
	{
		return addQueue(_name, _serviceType, false);
	}
	*/
	
	public boolean addCiclicQueue (String _name, String _serviceType, int _retriesLimit)
	{
		log("Petici�n - a�adir cola c�clica "+_name+":"+_serviceType+"\r\n");
		Queue q = (Queue) queueList.get(_name);
		if (q != null) {
			log("No se puede a�adir, ya hay una con ese nombre\r\n");
			return false;
		}
		Queue wq = null;
		wq = new CiclicWorkQueue (_name,_serviceType,_retriesLimit);
	
		Translator t = null;
		try {
			//t = new GenericTranslator(this);
			t = serviceMap.getTranslator(_serviceType);
		} catch (Exception errt) {
			log("Atenci�n - cola "+_name+" vinculada a un traductor nulo\r\n");
		}
		CiclicWorkDispatcher wd;
		wd = new CiclicWorkDispatcher(t);
		wq.addObserver (wd);
		wq.name = new String(_name);
		wq.serviceType = new String(_serviceType);
		queueList.put(_name,wq);
		log("Cola c�clica "+_name+" creada y preparada para atender trabajos del tipo "+_serviceType+" con "+_retriesLimit+" reintentos\r\n");
		return true;
	}

	public boolean subQueue ( String _name )
	{
		Queue wq = (Queue) queueList.remove(_name);
		if (wq != null) {
			log ("Cola "+_name+" eliminada\r\n");
			return true;
		} else {
			log ("Cola "+_name+" no existe en este deliverer\r\n");
			return false;
		}
	}
		
	public String listQueues ()
	{
		String res = "";
		Queue q;
		Enumeration<Queue> e = queueList.elements();
		
		res = "Colas configuradas\r\n" +
			  StringFacility.rellena("Nombre",21," ") +
			  StringFacility.rellena("Servicio",21," ") +
			  StringFacility.rellena("Ws",6," ") +
			  StringFacility.rellena("Clase",21," ") +
			  StringFacility.rellena("Sts",5," ") +
			  "\r\n";
		res += StringFacility.rellena("",74,"-");
		while (e.hasMoreElements()) {
			res += "\r\n";
			q = (Queue) e.nextElement();
			res += StringFacility.rellena(q.Name(),20," ") + " " +
				   StringFacility.rellena(q.Service(),20," ") +" " +
				   StringFacility.rellena("" + q.size(),5," ") + " " +
				   StringFacility.rellena(StringFacility.recorta(q.getClass().getName()),20," ") + " " +
				   (q.isOnline() ? "ON" : "OFF");
		}
		return res;
	}
	
	public String listClients ()
	{		
		String res = "";
		DelivererClient dc;
		Enumeration<DelivererClient> e = clients.elements();
		
		res = "Clientes conectados\r\n" +
			  StringFacility.rellena("Id",11," ") +
			  StringFacility.rellena("Host",21," ") +
			  StringFacility.rellena("IP",21," ") +
			  "\r\n";
		res += StringFacility.rellena("",74,"-");
		while (e.hasMoreElements()) {
			res += "\r\n";
			dc = (DelivererClient) e.nextElement();
			res += StringFacility.rellena(dc.getId(),10," ") + " " +
				   StringFacility.rellena(dc.getHostname(),20," ") +" " +
				   StringFacility.rellena(dc.getAddress(),20," ") + " ";
		}
		return res;
	}

	public boolean killClient (String id)
	{
		DelivererClient dc = (DelivererClient) clients.remove(id);
		
		try {
			dc.kill();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**Si esta m�quina es el default Deliverer no hace nada,
	 * si no, lo manda al defaultDeliverer.
	 */
	private boolean redirect(WorkItem wi)
	{
		System.out.println("Intentando redirigir el trabajo al Deliverer principal");
		InetAddress[] ddIP;  //IPs del default Deliverer
		try {
			if (defaultDeliverer.equals("127.0.0.1") || defaultDeliverer.equalsIgnoreCase("localhost")) {
				System.out.println("Es esta misma m�quina");
				return false;
			}
			ddIP = InetAddress.getAllByName(defaultDeliverer);
		} catch (Exception e) {
			System.out.println("Especifique nombre o IP de m�quina");
			return false;
		}
		
		InetAddress[] thisHost; //IPs de esta m�quina
		try {
			thisHost = InetAddress.getAllByName(
			   InetAddress.getLocalHost().getHostName());
		} catch (Exception e) {
			System.out.println("Error de red");;
			return false;
		}
		
		String n1, n2;
		for (int j = 0; j < thisHost.length; j++) {
			for (int k = 0; k < ddIP.length; k++) {
				n1 = thisHost[j].getHostAddress();
				n2 = ddIP[k].getHostAddress();
				if (n1.equals(n2)) {
					 System.out.println("Es esta misma m�quina"); 
					 return false;
				}
			}
		}
		//Aqu� hay que reenviar el WorkItem
		try {
			RemoteWorkQueue	rwq = new RemoteWorkQueue(getDefaultDeliverer(),"several");
			if (rwq.add(wi)) {
				System.out.println("Trabajo redirigido al Deliverer principal");
				return true;
			} else {
				System.out.println("Redirecci�n fallida. Es Deliverer no acept� el trabajo");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error al someter trabajo");
			return false;
		}
	}
	
	public synchronized boolean add(WorkItem wi)
	{
		log ("Petici�n de a�adir trabajo " + wi.getId() +" al deliverer\r\n");
		if (queueList.size() == 0) {
			log ("No hay colas que acepten el trabajo "+wi.getId()+"\r\n");
			return redirect(wi);
		}
		Hashtable<String, Queue> compatQueues = new Hashtable<String, Queue>();
		Enumeration<Queue> e = queueList.elements();
		Queue wq = null;
		while (e.hasMoreElements()) {
			wq = (Queue) e.nextElement();
			if (wq.isOnline() && wq.accepts(wi.getService()))
				compatQueues.put(wq.name,wq);
		}
		
		if (compatQueues.size() == 0) {
			log ("Trabajo "+ wi.getId() +" no aceptado por ninguna cola\r\n");
			return redirect(wi);
		}
		
		//Ant�guo m�todo JDK 1.1
		e = compatQueues.elements();
		int menor = 0;
		Queue wqaux;
		
		if (e.hasMoreElements()) {
			wq = (Queue) e.nextElement();
			menor = wq.size();
		}
		while (e.hasMoreElements()) {
			wqaux = (Queue) e.nextElement();
			if (wqaux.size() < menor) {
				wq = wqaux;
				menor = wq.size();
			}
		}
		if (wq != null) {
			if (wq.add (wi)) {				
				//this.setChanged();
				//this.notifyObservers();
				log ("Trabajo "+ wi.getId() +" sometido en la cola "+wq.name+"\r\n");
				return true;
			} else {
				log ("Trabajo "+ wi.getId() +" no aceptado por ninguna cola\r\n");
				return redirect(wi);
			}
		} else {
			log ("No hay colas que acepten el trabajo "+wi.getId()+"\r\n");
			return redirect(wi);
		}
	}
	
	public synchronized WorkItem sub ()
	{
		log ("Petici�n de sustraer el siguiente trabajo del deliverer\r\n");
		if (queueList.size() == 0)
			return null;
	
		//M�todo antiguo JDK 1.1
		Enumeration<Queue> e = queueList.elements();
		int mayor = 0;
		Queue wq = null, wqaux;
		
		if (e.hasMoreElements()) {
			wq = (Queue) e.nextElement();
			mayor = wq.size();
		}
		while (e.hasMoreElements()) {
			wqaux = (Queue) e.nextElement();
			if (wqaux.size() > mayor) {
				wq = wqaux;
				mayor = wq.size();
			}
		}
		if (wq != null) {
			WorkItem wi = wq.sub();
			if (wi != null)
				log ("Trabajo "+wi.getId()+" sustraido del distribuidor\r\n");
			else
				log ("No hay trabajos en este distribuidor\r\n");
			return wi;
		} else {
			log ("No hay colas en este distribuidor\r\n");
			return null;
		}
	}
	
	public int size ()
	{
		if (queueList.size() == 0)
			return 100000;
		Enumeration<Queue> e = queueList.elements();
		int menor = 0;
		Queue wqaux = null;
		
		if (e.hasMoreElements()) {
			wqaux = (Queue) e.nextElement();
			menor = wqaux.size();
		}
		while (e.hasMoreElements()) {
			wqaux = (Queue) e.nextElement();
			if (wqaux.size() < menor)
				menor = wqaux.size();
		}
		return menor;
	}
	
	public boolean accepts (String service)
	{
		Enumeration<Queue> e = queueList.elements();
		Queue wq = null;
		
		while (e.hasMoreElements()) {
			wq = (Queue) e.nextElement();
			if (wq.isOnline() && wq.accepts (service))
				return true;
		}
		
		return false;
	}
	
	/**Ayuda
	 */		
	private static String[][] commandsHelp = {
		{"ADDQUEUE", "A�ade una cola al deliverer",
		 "addQueue(nombre_cola,servicio,tipo (l � R))"},
		{"ADD", "Somete un trabajo", 
		 "add(usuario, servicio1, servicio1#comando1#servicio2#comando2...)"},
		{"SIZE", "Informa del n�mero de trabajos pendientes",
		 "size"},
		{"STOP", "Detiene el deliverer", 
		 "stop"},
		{"ACCEPTS", "Comprueba si el servicio es aceptado en el deliverer",
		 "accepts(servicio)"},
		{"SUBQUEUE", "Suprime una cola del deliverer",
		 "subQueue(nombre_cola)"},
		{"QUIT", "Sale de la sesi�n",
		 "quit"},
		{"RELOADQUEUES", "Recarga toda la configuraci�n de colas",
		 "reloadQueues"},
		{"ADDCICLICQUEUE", "A�ade una cola c�clica al deliverer",
		 "addCiclicQueue(nombre_cola, servicio, reintentos)"},
		{"ADDPROGRAM", "Somete un programa en el programador de tareas",
		 "addProgram(clase,fecha_hora,frecuencia)"},
		{"NOOP", "No operar (comando de conveniencia)",
		 "noop"},
		{"QUEUELIST", "Lista las colas configuradas",
		 "queueList"},
		{"CLIENTLIST", "Lista los clientes conectados",
		 "clientList"},
		{"KILLCLIENT", "Finaliza la sesi�n de un cliente",
		 "killClient(identificador_cliente)"},
		{"PROGRAMLIST", "Lista las entradas del programador de tareas",
		 "programList"},
		{"SUBPROGRAM", "Suprime un programa del programador de tareas",
		 "subProgram(identificador_programa)"},
		{"SETADMIN", "Cambia la bandera de administraci�n",
		 "setAdmin(password)"},
		{"CHGPWD", "Cambia la contrase�a del administrador",
		 "chgPwd(nueva_password)"},
		{"SERVICEMAP", "Lista el mapeo de servicios actual",
		 "serviceMap"},
		{"HELP", "Muestra esta ayuda",
		 "help / help(comando)"},
		{"SETMAPPING", "Ajusta un mapeo de servicio",
		 "setMapping(servicio, clase)"},
		{"GETMAPPING", "Muestra la clase asociada al servicio indicado",
		 "getMapping(servicio)"},
		{"REMOVEMAPPING", "Suprime el mapeo del servicio indicado",
		 "removeMapping(servicio)"},
		{"SAVEMAP", "Guarda el mapa actual en su archivo de configuraci�n",
		 "saveMap / saveMap(filename)"},
		{"LOADMAP", "Carga el mapa desde su archivo de configuraci�n",
		 "loadMap / loadMap(filename)"},
		{"SAVEQUEUES", "Guarda la configuraci�n de colas en queues.cfg",
		 "saveQueues / saveQueues(filename)"},
		{"LOADQUEUES", "Carga la configuraci�n de colas",
		 "loadQueues / loadQueues(filename)"},
		{"CLEARQUEUES", "Suprime todas las colas",
		 "clearQueues"},
		{"RELOADMAP", "Recarga el mapa de servicios",
		 "reloadMap"},
		{"CLEARMAP", "Suprime todos los servicios del mapa",
		 "clearMap"},
		{"RELOADPROGRAMS", "Recarga la programaci�n del scheduler",
		 "reloadPrograms / reloadPrograms(filename)"},
		{"LOADPROGRAMS", "Carga la programaci�n del scheduler",
		 "loadPrograms / loadPrograms(filename)"},
		{"SAVEPROGRAMS", "Guarga la programaci�n actual del scheduler",
		 "savePrograms / savePrograms(filename)"},
		{"CLEARPROGRAMS", "Limpia el programa del scheduler",
		 "clearPrograms"},
		{"OPENPROXY", "Abre un proxy a la direcci�n m�quina:puerto en el puerto local",
		 "openProxy(maquina,puerto,local)"},
		{"CLOSEPROXY", "Cierra el proxy indicado",
		 "closeProxy(id)"},
		{"CLOSEALLPROXIES", "Cierra todos los proxies",
		 "closeAllProxies"},
		{"LISTPROXIES","Lista los proxies abiertos",
		 "listProxies"}
	};
	
	public String help (String cmd)
	{
		StringBuffer sb = new StringBuffer();
		if (cmd == null || cmd.length() == 0) {
			for (int i = 0; i < commandsHelp.length; i = i + 4) {
				for (int j = i; j < (i + 4) && j < commandsHelp.length; j++) {
					sb.append(StringFacility.rellena(commandsHelp[j][0],18," "));
				}
				sb.append("\r\n");
			}
		} else {
			int i;
			for (i = 0; i < commandsHelp.length && !cmd.equalsIgnoreCase(commandsHelp[i][0]); i++);
			if (i < commandsHelp.length)
				sb.append(commandsHelp[i][0] + "\r\n" +
						  "-----------------------------\r\n" +
						  commandsHelp[i][1] + "\r\n\r\n" +
						  "Modo de empleo: \r\n\r\n\t" + 
						  commandsHelp[i][2] + "\r\n");
			else
				sb.append(cmd + "\r\n" + "Comando no v�lido");	
		}
		return sb.toString();
	}
	// Fin ayuda
	
	//Proxy
	public synchronized String openProxy (String host, int port, int local)
	{
		String[] datos = new String[3];
		datos[0] = new String(host);
		datos[1] = new String("" + port);
		datos[2] = new String("" + local);
		try {
			SocksProxy sp = new SocksProxy(datos);
			proxies.put(host + ":" + port,sp);
		} catch (UnknownHostException uhe) {
			return "-ERR no se puede acceder a "+host+":"+port;
		} catch (IOException ioe) {
			return "-ERR no se ha podido abrir el puerto local";
		} catch (Exception e) {
			return "-ERR error desconocido";  
		}
		return "+OK proxy abierto: "+host+":"+port+" local="+local;
	}
	
	public synchronized boolean closeProxy (String id)
	{
		SocksProxy sp = (SocksProxy) proxies.remove(id);		
		if ( sp == null) 
			return false;
		else {
			sp.closeAll();
			return true;
		}
	}
	
	public synchronized boolean closeAllProxies()
	{
		Enumeration<SocksProxy> e = proxies.elements();
		while (e.hasMoreElements())
			((SocksProxy) e.nextElement()).closeAll();
		proxies.clear();
		return true;
	}
	
	public String listProxies()
	{
		Enumeration<String> e = proxies.keys();
		SocksProxy sp;
		StringBuffer res = new StringBuffer();
		String key;
		res.append("Ident.\tHost\tPuerto\tPuerto Local\r\n");
		res.append("--------------------------------------------\r\n");
		while (e.hasMoreElements()) {
			key = (String) e.nextElement();
			sp = (SocksProxy) proxies.get(key);
			res.append(key + " " + sp.getServer() + " " +
					   sp.getPort() + " " + sp.getLocalPort() +
					   "\r\n");
		}
		return res.toString();
	}
	
	@SuppressWarnings("deprecation")
	public void suspend ()
	{
		log ("Deliverer suspendido\r\n");
		t.suspend();
	}
	
	@SuppressWarnings("deprecation")
	public void resume ()
	{
		log ("Deliverer rearrancado\r\n");
		t.resume();
	}
	
	@SuppressWarnings("deprecation")
	public void stop ()
	{
		log ("Deliverer detenido\r\n");
		clients.clear();
		t.stop();
		log ("Cerrando log - "+new java.util.Date()+"\r\n");
		System.exit(0);
	}
	
	public void join ()
		throws InterruptedException
	{
		t.join();
	}
	
	public void run ()
	{
		try {
			ss = new ServerSocket(4000);
			clients = new Hashtable<String, DelivererClient>();
			int lastClientNumber = -1;
			String index = "";
			Socket s;
			while (true) {
				s = ss.accept();
				lastClientNumber++;
				index = "" + lastClientNumber;
				clients.put(index,new DelivererClient(s,index));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void finalize ()
	{
		try {
			lout.close();
		} catch (IOException ioe) {}
	}
	
	private void log (String msg)
	{
		try {
			lout.write(msg.getBytes());
		} catch (IOException e) {
		}
	}

	
	private class DelivererClient implements Runnable
	{
		private Socket s;
		private InputStream in;
		private OutputStream out;
		private Thread t;
		private String id;
		private boolean admin;
		
		public String[] COMMANDS = {"ADDQUEUE",
			 					    "ADD",
									"SIZE",
									"SUSPEND",
									"RESUME",
									"STOP",
									"ACCEPTS",
									"SUBQUEUE",
									"QUIT",
									"RELOADQUEUES",
									"ADDCICLICQUEUE",
									"ADDPROGRAM",
									"NOOP",
									"QUEUELIST",
									"CLIENTLIST",
									"KILLCLIENT",
									"PROGRAMLIST",
									"SUBPROGRAM",
									"SETADMIN",
									"CHGPWD",
									"SERVICEMAP",
									"HELP",
									"SETMAPPING",
									"GETMAPPING",
									"REMOVEMAPPING",
									"SAVEMAP",
									"LOADMAP",
									"SAVEQUEUES",
									"LOADQUEUES",
									"CLEARQUEUES",
									"RELOADMAP",
									"CLEARMAP",
									"RELOADPROGRAMS",
									"LOADPROGRAMS",
									"SAVEPROGRAMS",
									"CLEARPROGRAMS",
									"OPENPROXY",
									"CLOSEPROXY",
									"CLOSEALLPROXIES",
									"LISTPROXIES"
		};
		public final int ADDQUEUE = 0;
		public final int ADD = 1;
		public final int SIZE = 2;
		@SuppressWarnings("unused")
		public final int SUSPEND = 3;
		@SuppressWarnings("unused")
		public final int RESUME = 4;
		public final int STOP = 5;
		public final int ACCEPTS = 6;
		public final int SUBQUEUE = 7;
		public final int QUIT = 8;
		public final int RELOADQUEUES = 9;
		//public final int STATUS = 10;
		public final int ADDCICLICQUEUE = 10;
		public final int ADDPROGRAM = 11;
		public final int NOOP = 12;
		public final int QUEUELIST = 13;
		public final int CLIENTLIST = 14;
		public final int KILLCLIENT = 15;
		public final int PROGRAMLIST = 16;
		public final int SUBPROGRAM = 17;
		public final int SETADMIN = 18;
		public final int CHGPWD = 19;
		public final int SERVICEMAP = 20;
		public final int HELP = 21;
		public final int SETMAPPING = 22;
		public final int GETMAPPING = 23;
		public final int REMOVEMAPPING = 24;
		public final int SAVEMAP = 25;
		public final int LOADMAP = 26;
		public final int SAVEQUEUES = 27;
		public final int LOADQUEUES = 28;
		public final int CLEARQUEUES = 29;
		public final int RELOADMAP = 30;
		public final int CLEARMAP = 31;
		public final int RELOADPROGRAMS = 32;
		public final int LOADPROGRAMS = 33;
		public final int SAVEPROGRAMS = 34;
		public final int CLEARPROGRAMS = 35;
		public final int OPENPROXY = 36;
		public final int CLOSEPROXY = 37;
		public final int CLOSEALLPROXIES = 38;
		public final int LISTPROXIES = 39;
		
		public DelivererClient (Socket s, String id)
		{
			this.s = s;
			this.id = id;
			try {
				in = s.getInputStream();
				out = s.getOutputStream();
			} catch (IOException ioe) {
				return;
			}
			admin = false;
			t = new Thread(this);
			t.start();
		}
		
		public String getAddress ()
		{
			return s.getInetAddress().getHostAddress();
		}
		
		public String getHostname ()
		{
			return s.getInetAddress().getHostName();
		}
		
		public String getId ()
		{
			return id;
		}
		
		@SuppressWarnings("deprecation")
		public void kill ()
		{
			try {
				t.stop();
				s.close();
			} catch (Exception e) {
			}
		}
		
		private String getCommand ()
		{
			String response = "";
			int c;
			try {
				while (in.available() == 0)
					Thread.sleep(100);
				while ((c = in.read()) != -1) {
					if (c == 13) {
						c = in.read();
						break;
					}
					response += (char) c;
				}
			} catch (Exception e) {}
			return response;
		}
		
		private void sendResponse (String cmd)
		{
			if (cmd.length() == 0)
				return;
			try {
				out.write ((cmd + "\r\n").getBytes());
			} catch (Exception e) {
			}
		}
		
		private int getIdxCommand (String cmd)
		{
			int i;
			for (i = 0; i < COMMANDS.length && !cmd.equalsIgnoreCase(COMMANDS[i]); i++);
			if (i < COMMANDS.length)
				return i;
			else
				return -1;
		}
		
		@SuppressWarnings("deprecation")
		private String parse (String cmd)
		{
			String[] partes;
			StringTokenizer st;
			st = new StringTokenizer(cmd,"(,)");
			partes = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				partes[i] = st.nextToken();

			try {
				switch (getIdxCommand (partes[0].toUpperCase())) {
				case ADDQUEUE:
					if (admin) {
						if (thisDeliverer.addQueue(partes[1],partes[2],partes[3].equals("R") ? true : false))
							return "+OK cola "+partes[1]+" a�adida al Deliverer";
						else
							return "-ERR no se pudo a�adir la cola";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case SUBQUEUE:
					if (admin) {
						if (thisDeliverer.subQueue(partes[1]))
							return "+OK cola "+partes[1]+" eliminada del Deliverer";
						else
							return "-ERR la cola "+partes[1]+" no existe en este Deliverer";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}	
				case ADD:
					WorkItem wi;
					if (partes.length > 4) {
						WorkResult wr = new WorkResult(partes[4]);
						wr.receive(in,Integer.parseInt(partes[5]));
						wi = new WorkItem(partes[1],partes[2],partes[3],wr);
						if (thisDeliverer.add(wi))
							return "+OK trabajo sometido";
						else
							return "-ERR nadie acept� el trabajo";
					} else {
						wi = new WorkItem(partes[1],partes[2],partes[3]);
						if (thisDeliverer.add(wi))
							return "+OK trabajo sometido";
						else
							return "-ERR nadie acept� el trabajo";
					}
				case SIZE:
					return "+OK "+thisDeliverer.size()+" works in queue";
				case STOP:
					if (admin) {
						sendResponse("+OK parado");
						thisDeliverer.stop();
						return "";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case ACCEPTS:
					if (thisDeliverer.accepts(partes[1]))
						return "+OK esta cola acepta ese servicio";
					else
						return "-ERR esta cola no accepta ese servicio";
				case QUIT:
					sendResponse("+OK hasta otra\r\n");
					clients.remove(id);
					s.close();
					t.stop();
					return "";
				case RELOADQUEUES:
					if (admin) {
						reloadQueues();
						return "+OK reiniciando la configuraci�n";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case ADDCICLICQUEUE:
					if (admin) {
						if (thisDeliverer.addCiclicQueue(partes[1],partes[2],Integer.parseInt(partes[3])))
							return "+OK cola c�clica "+partes[1]+" a�adida al Deliverer";
						else
							return "-ERR no se pudo a�adir la cola c�clica";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case ADDPROGRAM:
					if (admin) {
						if (scheduler.addProgram(partes[1],partes[2],Integer.parseInt(partes[3])))
							return "+OK programada nueva tarea "+partes[1]+" en el scheduler";
						else
							return "-ERR la clase "+partes[1]+" no est� registrada para ser programada";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case NOOP:
					return "+OK estoy aqu�";
				case QUEUELIST:
					return listQueues();
				case CLIENTLIST:
					return listClients();
				case KILLCLIENT:
					if (admin) {
						if (killClient(partes[1]))
							return "+OK cliente "+partes[1]+" expulsado";
						else
							return "-ERR cliente no existe";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case PROGRAMLIST:
					//return listSchedulerPrograms();
					return scheduler.listPrograms();
				case SUBPROGRAM:
					if (admin) {
						if (scheduler.subProgram(partes[1]))
							return "+OK programa "+partes[1]+" retirado del scheduler";
						else
							return "-ERR ese programa no existe";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case SETADMIN:
					if (partes[1].equalsIgnoreCase(password)) {
						admin = true;
						return "+OK administrator flag = true";	
					} else {
						admin = false;
						return "-ERR administrator flag = false";
					}
				case CHGPWD:
					if (admin) {
						password = partes[1];
						return "+OK contrase�a reajustada";
					} else {
						return "-ERR no tiene derecho a cambiar la contrase�a";
					}
				case SERVICEMAP:
					return serviceMap.getStringsMap();
				case HELP:
					if (partes.length < 2)
						return help("");
					else		
						return help(partes[1]);
				case SETMAPPING:
					if (admin) {
						if (serviceMap.set(partes[1],partes[2]))
							return "+OK servicio " + partes[1] + 
								   " ajustado a la clase " +
								   partes[2];
						else
							return "-ERR la clase "+
								   partes[2] + " no existe";
					} else {
						return "-ERR no dispone de autorizaci�n para modificar el mapa de servicios";
					}
				case GETMAPPING:
					String classTranslator = serviceMap.get(partes[1]);
					if (classTranslator != null)
						return "+OK "+
							   StringFacility.rellena(partes[1],31," ") +
							   classTranslator;
					else
						return "-ERR el servicio "+
							   partes[1] +
							   " no existe";
				case REMOVEMAPPING:
					if (admin) {
						if (serviceMap.remove(partes[1]))
							return "+OK servicio retirado";
						else
							return "-ERR el servicio " + partes[1] +
								   " no est� mapeado";
					} else {
						return "-ERR no dispone de autorizaci�n para modificar el mapa de servicios";
					}

				case SAVEMAP:
					if (admin) {
						boolean flag;
						if (partes.length < 2)
							flag = serviceMap.saveMap();
						else
							flag = serviceMap.saveMap(partes[1]);
						if (flag)
							return "+OK mapa de servicios almacenado";
						else
							return "-ERR no se ha podido guardar el nuevo mapa a disco";
					} else {
						return "-ERR no dispone de autorizaci�n para modificar los archivos de configuraci�n";
					}

				case LOADMAP:
					if (admin) {
						boolean flag;
						if (partes.length < 2)
							flag = serviceMap.loadMap();
						else
							flag = serviceMap.loadMap(partes[1]);
						if (flag)
							return "+OK mapa de servicios cargado";
						else
							return "-ERR no se ha podido cargar el mapa de disco";
					} else {
						return "-ERR no dispone de autorizaci�n para modificar el mapa de servicios";
					}					
				case SAVEQUEUES:
					if (admin) {
						boolean flag;
						if (partes.length < 2)
							flag = saveQueues();
						else
							flag = saveQueues(partes[1]);
						if (flag)
							return "+OK configuraci�n de colas guardada";
						else
							return "-ERR fallo al guardar la configuraci�n de colas";
					} else {
						return "-ERR no tiene autorizaci�n para modificar los archivos de configuraci�n";
					}
				case LOADQUEUES:
					if (admin) {
						boolean flag;
						if (partes.length < 2)
							flag = loadQueues();
						else
							flag = loadQueues(partes[1]);
						if (flag)
							return "+OK colas cargadas";
						else
							return "-ERR fallo al cargar la configuraci�n de colas";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case CLEARQUEUES:
					if (admin) {
						clearQueues();
						return "+OK eliminadas todas las colas";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}
				case RELOADMAP:
					if (admin) {
						serviceMap.clearMap();
						if (serviceMap.loadMap())
							return "+OK mapa recargado de mappedservices.cfg";
						else
							return "-ERR fallo al recargar el mapa. Mapa vac�o";
					} else {
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
					}		   
				case CLEARMAP:
					if (admin) {
						serviceMap.clearMap();
						return "+OK Mapa vacio";
					} else 
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case RELOADPROGRAMS:
					if (admin) {
						scheduler.clear();
						if (partes.length < 2) {
							if (scheduler.load())
								return "+OK programas recargados";
							else
								return "-ERR fallo al recargar";
						} else {
							if (scheduler.load(partes[1]))
								return "+OK programas recargadps";
							else
								return "-ERR el archivo " + partes[1] +
									   " no existe";
						}
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case LOADPROGRAMS:
					if (admin) {
						if (partes.length < 2) {
							if (scheduler.load())
								return "+OK programas cargados";
							else
								return "-ERR fallo al cargar";
						} else {
							if (scheduler.load(partes[1]))
								return "+OK programas cargadps";
							else
								return "-ERR el archivo " + partes[1] +
									   " no existe";
						}
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case SAVEPROGRAMS:
					if (admin) {
						if (partes.length < 2) {
							if (scheduler.save())
								return "+OK programas guardados";
							else
								return "-ERR fallo al guardar";
						} else {
							if (scheduler.save(partes[1]))
								return "+OK programas guardados";
							else
								return "-ERR no se pudo abrir el archivo "+
									   partes[1];
						}
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case CLEARPROGRAMS:
					if (admin) {
						scheduler.clear();
						return "+OK scheduler l�mpio";
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case OPENPROXY:
					if (admin) {
						return openProxy(partes[1],Integer.parseInt(partes[2]),Integer.parseInt(partes[3]));
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";					
				case CLOSEPROXY:
					if (admin) {
						if (closeProxy(partes[1]))
							return "+OK proxy cerrado";
						else
							return "-ERR no se ha podido cerrar el proxy";
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case CLOSEALLPROXIES:
					if (admin) {
						closeAllProxies();
						return "+OK todos los proxies cerrados";
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				case LISTPROXIES:
					if (admin) {
						return listProxies();
					} else
						return "-ERR no dispone de autorizaci�n para esa operaci�n";
				default:
					return "-ERR comando no entendido";
				}
			} catch (Exception e) {
				return "-ERR faltan par�metros";
			}
		}
		
		public void run ()
		{
			String command;
			while (true) {
				while ((command = getCommand ()).length() > 0) {
					sendResponse (parse (command));
				}
			}
		}
	}
	
	public static void main (String[] args)
		throws Exception
	{
		String deliverer;
		try {
			deliverer = args[0];
		} catch (Exception e) {
			deliverer = "127.0.0.1";
		}
		new Deliverer(deliverer).join();
	}
}
