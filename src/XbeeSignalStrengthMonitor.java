import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/* This class receives packets from the worlddatapacket class and uses the data to update the object that represents each rdlogger
 * It then passes the data from the object to the GUI class which updates accordingly
*/
public class XbeeSignalStrengthMonitor extends Thread implements PacketListener  {
	RecordDigiAPIRx packetParser = new RecordDigiAPIRx();
	SignalData signalData = new SignalData();
	SignalReadingsPanel signalPanel;
	boolean started = true;
	

	InputStream mmInStream;
	
	protected List<String> listSerialNumbers = new ArrayList<String>();
	protected List<SignalData> listSignalData = new ArrayList<SignalData>();
	protected IniFile ini;

	
	public XbeeSignalStrengthMonitor(){
		
	}
		
	public void run() {
		ini = new IniFile("config_default.ini");

		
		String serialPort;
		int serialSpeed;
		
		System.out.println("setting com settings");
		serialPort = ini.getValueSafe("SERIAL", "port", "COM4");
		serialSpeed = Integer.parseInt(ini.getValueSafe("SERIAL", "speed",
				"57600"));
		System.err.println("# Opening " + serialPort + " @ " + serialSpeed);

		
		RDLUniversalReader remote = new RDLUniversalReader(serialPort, serialSpeed, 2);
		remote.addPacketListener(this);
		signalPanel = new SignalReadingsPanel();

	}
	
	
	public static void main(String args[]) {
		
		SerialIOInstall.installSerialIO();
		
		
		(new XbeeSignalStrengthMonitor()).start();
		
		
	}





	@Override
	public void packetReceived(WorldDataPacket packet) {
		System.err.println("packet received");
		System.err.println(String.valueOf(packet.getRSSI()));
		int rssi = packet.getRSSI();

		//add serial to list if not already there
		if (!listSerialNumbers.contains(packet.serial_prefix+ "" + packet.serial_number) && packet.serial_prefix == 'R' ) {
			//We found a new serial number so we add it to the list
			SignalData newSerial = new SignalData();
			
			newSerial.setSerialNumber(packet.serial_prefix+ "" + packet.serial_number);
			newSerial.setCurrentStrength(rssi);
			newSerial.addToSignal(rssi);
			newSerial.updateMinMax(rssi);
			newSerial.updateAvg();

			newSerial.incPacketCount();
			
			//add serial number to our list of serial numbers
			listSerialNumbers.add(packet.serial_prefix+ "" + packet.serial_number);
			//add signal data to our list of signal data
			listSignalData.add(newSerial);
			System.out.println(rssi);
			
			signalPanel.createSection(packet.serial_prefix+ "" + packet.serial_number, newSerial.getAvg(), newSerial.getMinStrength(), newSerial.getMaxStrength(), newSerial.getCurrentStrength());

		}
		//if we have already seen this serial number, update its data
		else if(listSerialNumbers.contains(packet.serial_prefix+ "" + packet.serial_number) && packet.serial_prefix == 'R'){
			for(SignalData n : listSignalData){
				if(n.getSerialNumber().equals(packet.serial_prefix+ "" + packet.serial_number)){
					n.setCurrentStrength(rssi);
					n.addToSignal(rssi);
					n.updateMinMax(rssi);
					n.incPacketCount();
					n.updateAvg();

					signalPanel.updateSection(n.getSerialNumber(), n.getAvg(), n.getMinStrength(), n.getMaxStrength(), n.getCurrentStrength(), n.getPacketCount());

				}
			}
			//Debug list contents
			/*for(SignalData n : listSignalData){
				if(n.getSerialNumber().equals(packet.serial_prefix+ "" + packet.serial_number)){
					System.out.println("Serial Number = "+n.getSerialNumber()+" avg = "+n.getAvg()+" min = "+
					n.getMinStrength()+" max = "+
					n.getMaxStrength()+" current = "+
					n.getCurrentStrength());
				}
			}*/
			System.out.println(packet.getRSSI());
		}
		else{

			//do nothing - wrong serial prefix most likely
		}
		System.err.println(listSerialNumbers.toString());
	}

}
