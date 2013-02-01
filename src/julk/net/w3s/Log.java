package julk.net.w3s;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**********************************************************************************
 * CLASS: Link
 * An easy way to create log files and so documenting errors
 * happened during program execution.
 *
 * @author Julio Cesar Serrano Ortuno
 * created November 26, 2007
 * updated December 15, 2007
 * license General Public License
 **********************************************************************************/

public class Log {
	
	/**
	 * This method is static and can be invoqued without
	 * creating an instance of the class Log. Writes a line
	 * of text into the desired log file.
	 * 
	 * @param logname filename for the log
	 * @param source  module that generated the error
	 * @param message message to write
	 */
	public static void log(String logname, String source, String message) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(logname, true));
			Calendar c = Calendar.getInstance();
			out.println(c.getTime()+" "+source+": "+message);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
