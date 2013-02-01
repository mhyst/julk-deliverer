package julk.net.deliver;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class HelpTranslator extends Translator
{	
	public boolean translate (String user, String service,
							  String command, WorkResult owr)
	{
		PrintWriter hf;
		
		try {
			hf= new PrintWriter(new FileOutputStream("help.txt"));	
			hf.println("Subject: Ayuda de Deliverer");
			hf.println("Robomail - Ayuda");
			hf.println("---------------------------------------------------------------------");
			hf.println();
			hf.println("Para obtener cualquier archivo del sistema robomail,");
			hf.println("enviar un e-mail a la dirección <robomail@terra.es>.");
			hf.println("Asegurarse de ajustar el cliente de correo para que envíe");
			hf.println("el contenido como texto sin formato. De esa forma se evitará");
			hf.println("recibir correos de notificación de comando erróneo de Robomail.");
			hf.println();
			hf.println("No ponga asunto.");
			hf.println("En el cuerpo del e-mail indicar el o los comandos que crea oportuno.");
			hf.println();
			hf.println("La sintaxis de los comandos es:");
			hf.println();
			hf.println("nombre_del_servicio#comando");
			hf.println();
			hf.println("SERVICIOS");
			hf.println("---------------------------------------------------------------------");
			hf.println("Los servicios mayoritariamente disponibles son:");
			hf.println();
			hf.println("filer_proxy 	- Para srv_www");
			hf.println("filer_admin	- Para srv_admin");
			hf.println("filer_registro  - Para srv_registro");
			hf.println();
			hf.println("Otros servicios accesibles temporalmente (de 10:00 a 19:00 pero");
			hf.println("no siempre) son:");
			hf.println();
			hf.println("filer_as400	- Para REGIS150");
			hf.println("filer_linux	- Para mi máquina 192.168.1.215");
			hf.println();
			hf.println("COMANDOS");
			hf.println("---------------------------------------------------------------------");
			hf.println("Los comandos disponibles, por ahora, son dos:");
			hf.println();
			hf.println("list <ruta>");
			hf.println("Para obtener un listado de directorio de la ruta indicada.");
			hf.println();
			hf.println("get <ruta_hasta\\archivo>");
			hf.println("Para obtener el archivo indicado.");
			hf.println();
			hf.println("EJEMPLOS FUNCIONALES");
			hf.println("---------------------------------------------------------------------");
			hf.println("Los siguientes comandos podrían ser válidos:");
			hf.println();
			hf.println("filer_admin#list \\\\srv_admin\\comun");
			hf.println("filer_admin#list c:\\winnt");
			hf.println("filer_registro#get c:\\winnt\\system.ini");
			hf.println("filer_as400#list /qibm");
			hf.println("filer_linux#list /usr/lib");
			hf.println();
			hf.println();
			hf.println();
			hf.println("NOTAS");
			hf.println("---------------------------------------------------------------------");
			hf.println("Tenga en cuenta que la sintaxis de la ruta dependerá del sistema");
			hf.println("operativo de la máquina que atienda el servicio. Por ejemplo,");
			hf.println("en UNIX, las rutas son '/home/root/vmware', mientras que en");
			hf.println("MSDOS/Windows son 'c:\\windows\\commands\\command.com'.");
			hf.println();
			hf.println("Antes de lanzar cualquier comando entienda que tanto el programa");
			hf.println("Broken que lee el correo POP3 cada cinco minutos interpretando y");
			hf.println("sometiendo los comandos, como el programa Deliverer que gestiona");
			hf.println("las colas de trabajo a lo largo y ancho de la red, están en fase"); 
			hf.println("de pruebas y no es totalmente seguro que atiendan su petición");
			hf.println("debidamente. No obstante, si encontrara algún problema, le agradecería");
			hf.println("me lo comunicara para tomar las medidas oportunas. De igual forma,");
			hf.println("agradezco las sugerencias que usted podría hacer sobre el");
			hf.println("funcionamiento de dichos programas.");
			hf.println();
			hf.println();
			hf.println("COMO CONTACTAR CON EL AUTOR");
			hf.println("---------------------------------------------------------------------");
			hf.println("Para contactar conmigo, hágalo por e-mail a la dirección");
			hf.println();
			hf.println("	julk@jet.es");
			hf.println();
			hf.println();
			hf.println("Julio César Serrano Ortuno");
			hf.println("Registro S.A.");
			hf.close();
			WorkResult wr = new WorkResult("help.txt",false);
			/*WorkItem nwi =new WorkItem(wi.getUser(),"mailer","do",wr);
			DELIVERER.add(nwi);*/
			setWorkResult(wr);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
