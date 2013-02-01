package julk.net.deliver;

public class GenericTranslator extends Translator
{
	public static final String[] services = {"FILER",
											 "MAILER",
											 "HELP",
											 "NET",
											 "COMPOSE",
											 "MAILLIST"
	};
	public static final int FILER = 0;
	public static final int MAILER = 1;
	public static final int HELP = 2;
	public static final int NET = 3;
	public static final int COMPOSE = 4;
	public static final int MAILLIST = 5;
		
	public GenericTranslator(Deliverer d)
	{
		setDeliverer(d);
	}
	
	public GenericTranslator ()
	{
	}
	
	private int getIdxService (String service)
	{
		int i;

		for (i = 0; !service.toUpperCase().startsWith(services[i]) && i < services.length; i++);
		if (i < services.length)
			return i;
		else
			return -1;
	}
	
	public boolean translate (String user, String service,
							  String command, WorkResult wr)
	{
		Translator trn;
		boolean b;
		
		switch (getIdxService(service)) {
		case FILER:
			try {
				trn = new FileTranslator ();
				trn.setDeliverer(DELIVERER);
				b = trn.translate(user,service,command,wr);
				setWorkResult(trn.getWorkResult());
				return b;
			} catch (Exception e) {
				System.out.println("No se ha podido procesar el trabajo");
				return false;
			}
		case MAILER:
			try {
				trn = new SMTPTranslator();
				trn.setDeliverer(DELIVERER);
				b = trn.translate(user,service,command,wr);
				setWorkResult(trn.getWorkResult());
				return b;
			} catch (Exception e) {
				System.out.println("No se ha podido enviar el trabajo");
				return false;
			}
		case HELP:
			try {
				trn = new HelpTranslator();
				trn.setDeliverer(DELIVERER);
				b = trn.translate(user,service,command,wr);
				setWorkResult(trn.getWorkResult());
				return b;
			} catch (Exception e) {
				System.out.println("No se ha podido enviar la ayuda");
				return false;
			}
		case NET:
			try {
				trn = new TCPNetTranslator();
				trn.setDeliverer(DELIVERER);
				b = trn.translate(user,service,command,wr);
				setWorkResult(trn.getWorkResult());
				return b;
			} catch (Exception e) {
				System.out.println("No se ha podido enviar el trabajo");
				return false;
			}
		case COMPOSE:
			try {
				trn = new ComposerTranslator();
				trn.setDeliverer(DELIVERER);
				b = trn.translate(user,service,command,wr);
				setWorkResult(trn.getWorkResult());
				return b;
			} catch (Exception e) {
				System.out.println("No se ha podido enviar el trabajo");
				return false;
			}		
		/*case MAILLIST:
			try {
				trn = new MailListTranslator();
				trn.setDeliverer(DELIVERER);
				b = trn.translate(user,service,command,wr);
				setWorkResult(trn.getWorkResult());
				return b;
			} catch (Exception e) {
				System.out.println("No se ha podido enviar el trabajo");
				return false;
			}*/		
		default:
			System.out.println("Ese servicio no se soporta");
			return false;
		}
	}
}
