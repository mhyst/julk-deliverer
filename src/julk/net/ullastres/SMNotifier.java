package julk.net.ullastres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.FileWriter;
import julk.net.deliver.WorkResult;
import julk.net.deliver.WorkItem;

import julk.net.scheduler.SchedulerProgram;

public class SMNotifier extends SchedulerProgram {

	private Database db;
	
	public void Init() {
		// TODO Auto-generated method stub

		//1. Comprobamos que recibimos instancia de DELIVERER.
		System.out.println("Inicializando SMNotifier...");
		if (!setDeliverer()) {
			setReady(false);
			System.out.println("SMNotifier: Efectivamente, no llega el deliverer");
			return;
		}				
		
		//2. Comprobamos que la conexión con la base de datos funciona.
		//Lo retiro por razones de testing
		/*try  {
			db = new Database();
			setReady(true);
			db.disconnect();
			db = null;
		} catch (Exception e) {
			System.out.println("SMNotifier ERROR: "+e.getMessage());
			setReady(false);
		}*/
		
	}

	protected void launch() {
		// TODO Auto-generated method stub
		
		//1. Conectamos con la base de datos
		try  {
			db = new Database();
			setReady(true);
		} catch (Exception e) {
			System.out.println("SMNotifier ERROR: "+e.getMessage());
			setReady(false);
			return;
		}
		
		//2. Preparamos nuestra consulta
		String SQL = "SELECT M.*, ZZNOTX, ZZCLFX, ZZBLFX, ZZNCFX "+
					 "FROM WEBJ.FMAILSOP AS M, WEBJ.POLIZA "+
					 "WHERE ZZTPPX = HQTPPZ AND ZZPBPX = HQPBPZ"+
					 " AND ZZNUPX = HQNUPZ AND ZZDGPX = HQDGPZ "+
					 "ORDER BY LLADML, LLDGAL,"+          //Administrador
					 " HQTPPZ, HQPBPZ, HQNUPZ, HQDGPZ,"+  //Póliza
					 " HQAALI, HQNLIQ";                    //Periodo
		
		try {
			Connection conn = db.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
		
			//3. Bucle de reparto			
			long adm_id = 0, adm_dc = 0;
			long _adm_id, _adm_dc;
			String email = "", msg = "";
			while (rs.next()) {
				_adm_id = rs.getLong("LLADML");
				_adm_dc = rs.getLong("LLDGAL");
				//Comprobamos si es un nuevo administrador
				if ((adm_id != _adm_id) || (adm_dc != _adm_dc)) {
					if (msg.length()>0) {
						//Aquí enviar email
						msg += "\r\n\r\nPara cualquier consulta o aclaraci=F3n "+
							   "que desee realizar contacte con nosotros\r\nTlf. 91 540.17.55";
						System.out.println("Email: "+email);
						System.out.println(msg);
						send(email,adm_id+"."+adm_dc,msg);
					}
					adm_id = _adm_id;
					adm_dc = _adm_dc;
					msg = "From: Ullastres <comercial@ullastres.com>\r\n"+
						  "To: "+email+"\r\n"+
						  "Subject: Soportes magneticos\r\n"+
						  "MIME-Version: 1.0\r\n"+
						  "Content-type: text/plain; charset=iso-8859-1\r\n"+
						  "Content-Transfer-Encoding: quoted-printable\r\n\r\n"+
						  "Les informamos que ya tienen a su disposici=F3n "+
						  "a trav=E9s del programa de Gesti=F3n de Fincas \"GESTOR\" "+
						  "los soportes de liquidaci=F3n de consumos "+
						  "correspondientes a las fincas y periodos que "+
						  "seguidamente les detallamos:\r\n\r\n";
					//msg += "Administrador: "+adm_id+"."+adm_dc+"\r\n";
					email = rs.getString("AXMAIL");
				}
				msg += "Poliza: "+rs.getString(1)+"."+rs.getString(2)+"."+
					   rs.getString(3)+"."+rs.getString(4)+" "+
					   rs.getString("ZZNOTX").trim()+" "+
					   " C/"+rs.getString("ZZCLFX").trim()+", "+rs.getString("ZZNCFX").trim()+" ";
				String blq = rs.getString("ZZBLFX").trim();
				if (blq.length() > 0)
					msg += "Blq: "+rs.getString("ZZBLFX").trim()+" ";
				msg += "Periodo: "+rs.getString(5)+"/"+rs.getString(6)+"\r\n";
			}
			if (msg.length()>0) {
				//Aquí enviar email
				msg += "\r\n\r\nPara cualquier consulta o aclaraci=F3n "+
				   	   "que desee realizar contacte con nosotros.\r\nTlf. 91 540.17.55";
				System.out.println("Email: "+email);
				System.out.println(msg);
				send(email,adm_id+"."+adm_dc,msg);
			}
			//4. Desconexión
			rs.close();
			stmt.execute("DELETE FROM WEBJ.FMAILSOP");
			stmt.close();
			conn.close();
			
		} catch (Exception e) {
			System.out.println("SMNotifier ERROR: "+e.getMessage());
		}
	}
	
	private void send(String rcpt, String name, String msg)
	{
		try {
			FileWriter f = new FileWriter(name);
			f.write(msg);
			f.close();
			WorkResult wr = new WorkResult(name,false);
			WorkItem wi = new WorkItem(rcpt,"emailer","emailer#send",wr);
			DELIVERER.add(wi);
		} catch (Exception e) {
			System.out.println("SMNotifier ERROR: "+e.getMessage());
		}	
	}
	
	public static void main(String args[])
	throws Exception
	{
		SMNotifier smn = new SMNotifier();
		smn.Init();
		smn.launch();
	}


}