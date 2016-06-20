import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class XbeeSignalStrengthMonitor extends Thread implements ListenerWorldData  {
	RecordDigiAPIRx packetParser = new RecordDigiAPIRx();
	
	SignalData signalData = new SignalData();
	
	boolean started = true;
	
	InputStream mmInStream;
	
	protected List<String> listSerialNumbers = new ArrayList<String>();
	protected List<SignalData> listSignalData = new ArrayList<SignalData>();
	
	
	public XbeeSignalStrengthMonitor(){
		
	}
		
	public void run() {
		ReaderWorldData r = new ReaderWorldData(mmInStream);
		r.addPacketListener(this);
		while ( started ) {
			try { 
				
				r.readForPacket();
				
			} catch ( IOException e ) {
				System.err.println("# caught IOException in go(). Bailing out.");
				//Log.e("status","# caught IOException in go(). Bailing out.");
				//breaks out of the while loop if an ioexception is found
				break;
			}
		}
	
	}
	
	
	public static void main(String args[]) {
		/*String ini = null;

		if (args.length == 1) {
			ini = args[0];
		} else {
			System.err.println("Usage: java RDLUniversalDownload inifile");
			System.err
					.println("Invoke with -Dswing.aatext=true for anti-aliased fonts");

			ini = "config_default.ini";
		}*/

		SerialIOInstall.installSerialIO();
		
		(new XbeeSignalStrengthMonitor()).start();
		
		
	}




	@Override
	public void worldDataPacketReceived(int packetType, int serialPrefix, int serialNumber, int[] data, long timeMilli) {
			packetParser.parseRecord(data);
			
			//add serial to list if not already there
			if (!listSerialNumbers.contains((char)serialPrefix+ "" + serialNumber) && (char)serialPrefix == 'R' ) {
				//We found a new serial number so we add it to the list
				SignalData newSerial = new SignalData();
				
				newSerial.setSerialNumber((char)serialPrefix+ "" + serialNumber);
				newSerial.setCurrentStrength(packetParser.rssi);
				newSerial.addToSignal(packetParser.rssi);
				newSerial.updateAvg();
				
				//add serial number to our list of serial numbers
				listSerialNumbers.add((char)serialPrefix+ "" + serialNumber);
				//add signal data to our list of signal data
				listSignalData.add(newSerial);
				
			}
			//if we have already seen this serial number, update its data
			else if(listSerialNumbers.contains((char)serialPrefix+ "" + serialNumber) && (char)serialPrefix == 'R'){
				for(SignalData n : listSignalData){
					if(n.getSerialNumber().equals((char)serialPrefix+ "" + serialNumber)){
						n.setCurrentStrength(packetParser.rssi);
						n.addToSignal(packetParser.rssi);
						n.updateAvg();
					}
				}
			}
			else{
				//do nothing wrong serial prefix most likely
			}
	}

}
