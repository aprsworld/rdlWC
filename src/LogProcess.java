import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;



public class LogProcess {
	protected BufferedWriter writer;

	public LogProcess() {
		writer=null;
	}
	
	public void createLog(String filename) {
		try { 
			/* create our file */
			writer = new BufferedWriter(new FileWriter(filename,true));
			System.err.println("# Created log file: " + filename);
		} catch ( IOException e ) {
			System.err.println("# IO Exception while creating download file:\n" + e);
		}
	}
	
	public void writeLog(String line) {
		if ( null == writer ) {
			return;
		}
		try {
			writer.write(line);
		} catch ( IOException e ) {
			System.err.println("# IO Exception while closing download file:\n" + e);
		}
	}
	
	public void closeLog() {
		if ( null == writer ) {
			return;
		}
		try {
			writer.close();
		} catch ( IOException e ) {
			System.err.println("# IO Exception while closing download file:\n" + e);
		}
	}
}
