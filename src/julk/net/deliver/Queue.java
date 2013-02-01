package julk.net.deliver;

import java.util.Observable;

/**Clase abstracta que reune todas las características de una
 * cola.
 */
public abstract class Queue extends Observable
{
	/**Nombre de la cola
	 */
	protected String name;			
	/**Servicio asignado a la cola
	 */
	protected String serviceType;	
	
	/**Indicador online/offline
	 */
	protected boolean online;
	
	/**Conecta la cola, sólo util en colas remotas
	 */
	public boolean connect()
	{
		putOnline();
		return true;
	}
	/**Devuelve verdadero
	 */
	public boolean noop()
	{
		return true;
	}
	/**Añade el trabajo "wi" a la cola
	 */
	public abstract boolean add(WorkItem wi);
	/**Retira un trabajo de la cola y lo devuelve
	 */
	public abstract WorkItem sub ();
	/**Devuelve el número de trabajos esperando en la cola
	 */
	public abstract int size ();
	/**Devuelve true si la cola acepta el servicio "service"
	 */
	public abstract boolean accepts (String service);
	/**Devuelve el nombre de la cola
	 */
	public String Name ()
	{
		return name;
	}
	/**Asigna un nombre a la cola
	 */
	public void Name (String value)
	{
		name = new String (value);
	}
	/**Devuelve el nombre del servicio que atiende la cola
	 */
	public String Service ()
	{
		return serviceType;
	}
	/**Asigna el nombre del servicio que atiende la cola
	 */
	public void Service (String value)
	{
		serviceType = new String (value);
	}
	/**Devuelve true si la cola está en línea
	 */
	public boolean isOnline()
	{
		return online;
	}
	/**Pone la cola fuera de línea
	 */
	public void putOffline()
	{
		online = false;
	}
	/**Pone la cola en línea
	 */
	public void putOnline()
	{
		online = true;
	}
}
