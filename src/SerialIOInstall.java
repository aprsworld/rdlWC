import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class SerialIOInstall {

	protected static boolean copyFile(String source, String dest) {
		try {	
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(dest);


			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) >= 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			in.close();
		} catch ( Exception e ) {
			System.err.println("Error copying file");
			return false;
		}

		return true;
	}

	public static void installSerialIO() {

		String sourceFile=null;
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

		String realArch = arch.endsWith("64")
		                  || wow64Arch != null && wow64Arch.endsWith("64")
		                      ? "64" : "32";
		
		if(realArch == "64"){
			sourceFile="jspWin.64.dll";
		
		}
		else if(realArch == "32"){
			sourceFile="jspWin.32.dll";

			
		}
		
		/*if ( 0==System.getProperty("sun.arch.data.model").compareTo("32") ) {
			// 32-bit system 
			sourceFile="jspWin.32.dll";
		} else if ( 0==System.getProperty("sun.arch.data.model").compareTo("64") ) {
			// 64-bit system 
			sourceFile="jspWin.64.dll";

		}*/

		String destFile = "jspWin.dll";
		if ( null != sourceFile ) {
			File f = new File(sourceFile);
			if ( f.exists()) {
				copyFile(sourceFile,destFile);
				System.err.println("# Copying " + sourceFile + " to " + destFile);
				copyFile(sourceFile,System.getProperty("user.home") + System.getProperty("file.separator") + destFile);
				System.err.println("# Copying " + sourceFile + " to " + destFile);
			} else {
				System.err.println("# SerialIO's " + sourceFile + " file not found!");
			}
		}
	}
}
