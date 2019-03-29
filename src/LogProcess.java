import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



public class LogProcess {
	protected BufferedWriter writer;
	protected boolean messages=false;

	public LogProcess() {
		writer=null;
	}
	
	public LogProcess(boolean showMessages) { 
		this();
		messages=showMessages;
	}
	
	public void createLog(String filename) {
		try { 
			/* create our file */
			writer = new BufferedWriter(new FileWriter(filename,true));
			
			if ( messages ) {
				System.err.println("# Created / appended to log file: " + filename);
			}
		} catch ( IOException e ) {
			System.err.println("# IO Exception while creating file:\n" + e);
		}
	}
	
	public void createLog(String filename, String header) {
		File logFile = new File(filename);
		
		if ( logFile.exists() ) {
			/* it exists, so we just use createLog() */
			createLog(filename);
		} else {
			/* it doesn't exist, so we createLog() and then writeLog() with the header string */
			createLog(filename);
			if ( messages ) {
				System.err.println("# file didn't exist previously. Writing headers.");
			}
			writeLog(header);
		}
	}
	
	public void writeLog(String line) {
		if ( null == writer ) {
			return;
		}
		try {
			writer.write(line);
		} catch ( IOException e ) {
			System.err.println("# IO Exception while writing file:\n" + e);
		}
	}
	
	public void closeLog() {
		if ( null == writer ) {
			return;
		}
		try {
			writer.close();
		} catch ( IOException e ) {
			System.err.println("# IO Exception while closing file:\n" + e);
		}
	}
}
