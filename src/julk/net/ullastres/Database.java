package julk.net.ullastres;

import java.sql.*;

public class Database {
	public static String DriverName = "com.ibm.as400.access.AS400JDBCDriver";
	public static String User = "julio";
	public static String Pass = "julio";
	public static String Lib = "webj";
	public static String URL = "jdbc:as400://10.10.10.250";
	private Connection conn;
	
	public Database() throws Exception
	{
		//Aqui vamos a registrar el driver JDBC
		//y a conectar con la base de datos
		Class.forName(DriverName);
		conn = DriverManager.getConnection(URL, User, Pass);
	}
	
	public Connection getConnection()
	{
		return conn;
	}
	
	public void disconnect()
	{
		try {
			conn.close();
		} catch (Exception e) {
			conn = null;
			System.gc();
		}
	}

	public void finalize()
	{
		disconnect();
	}

	public static void main(String args[])
	throws Exception
	{
		Database db = new Database();
		Connection conn = db.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from webj.fmailsop");
		while (rs.next()) {
			System.out.println(rs.getString("AXMAIL"));
		}
		stmt.close();
		conn.close();
	}
}
