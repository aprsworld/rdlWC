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

public class XbeeSignalStrengthMonitor extends Thread implements PacketListener  {
	RecordDigiAPIRx packetParser = new RecordDigiAPIRx();
	
	SignalData signalData = new SignalData();
	
	boolean started = true;
	

	InputStream mmInStream;
	
	protected List<String> listSerialNumbers = new ArrayList<String>();
	protected List<SignalData> listSignalData = new ArrayList<SignalData>();
	protected IniFile ini;

	
	public XbeeSignalStrengthMonitor(){
		
	}
		
	public void run() {
		String serialPort;
		int serialSpeed;
		
		
		serialPort = "COM4";//ini.getValueSafe("SERIAL", "port", "COM1");
		serialSpeed = Integer.parseInt("57000");//Integer.parseInt(ini.getValueSafe("SERIAL", "speed",
				//"57600"));
		System.err.println("# Opening " + serialPort + " @ " + serialSpeed);

		
		RDLUniversalReader remote = new RDLUniversalReader(serialPort, serialSpeed);
		remote.addPacketListener(this);
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
	public void packetReceived(WorldDataPacket packet) {
		packetParser.parseRecord(packet.data);
		System.out.println(packetParser.rssi);

		//add serial to list if not already there
		if (!listSerialNumbers.contains(packet.serial_prefix+ "" + packet.serial_number) && packet.serial_prefix == 'R' ) {
			//We found a new serial number so we add it to the list
			SignalData newSerial = new SignalData();
			
			newSerial.setSerialNumber(packet.serial_prefix+ "" + packet.serial_number);
			newSerial.setCurrentStrength(packetParser.rssi);
			newSerial.addToSignal(packetParser.rssi);
			newSerial.updateAvg();
			
			//add serial number to our list of serial numbers
			listSerialNumbers.add(packet.serial_prefix+ "" + packet.serial_number);
			//add signal data to our list of signal data
			listSignalData.add(newSerial);
			System.out.println(packetParser.rssi);

		}
		//if we have already seen this serial number, update its data
		else if(listSerialNumbers.contains(packet.serial_prefix+ "" + packet.serial_number) && packet.serial_prefix == 'R'){
			for(SignalData n : listSignalData){
				if(n.getSerialNumber().equals(packet.serial_prefix+ "" + packet.serial_number)){
					n.setCurrentStrength(packetParser.rssi);
					n.addToSignal(packetParser.rssi);
					n.updateAvg();
				}
			}
			
			System.out.println(packetParser.rssi);
		}
		else{
			//do nothing - wrong serial prefix most likely
		}
	}

}
