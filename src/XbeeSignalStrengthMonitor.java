import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



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
		String serialPort;
		int serialSpeed;
		
		System.out.println("setting com settings");
		serialPort = "COM4";//ini.getValueSafe("SERIAL", "port", "COM1");
		serialSpeed = Integer.parseInt("57600");//Integer.parseInt(ini.getValueSafe("SERIAL", "speed",
				//"57600"));
		System.err.println("# Opening " + serialPort + " @ " + serialSpeed);

		
		RDLUniversalReader remote = new RDLUniversalReader(serialPort, serialSpeed, 2);
		remote.addPacketListener(this);
		signalPanel = new SignalReadingsPanel();

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
			newSerial.updateAvg();
			newSerial.updateMinMax(rssi);
			newSerial.incPacketCount();
			
			//add serial number to our list of serial numbers
			listSerialNumbers.add(packet.serial_prefix+ "" + packet.serial_number);
			//add signal data to our list of signal data
			listSignalData.add(newSerial);
			System.out.println(rssi);
			System.out.println("if");
			
			signalPanel.createSection(packet.serial_prefix+ "" + packet.serial_number, newSerial.getAvg(), newSerial.getMinStrength(), newSerial.getMaxStrength(), rssi);

		}
		//if we have already seen this serial number, update its data
		else if(listSerialNumbers.contains(packet.serial_prefix+ "" + packet.serial_number) && packet.serial_prefix == 'R'){
			for(SignalData n : listSignalData){
				if(n.getSerialNumber().equals(packet.serial_prefix+ "" + packet.serial_number)){
					n.setCurrentStrength(rssi);
					n.addToSignal(rssi);
					n.updateAvg();
					n.updateMinMax(rssi);
					n.incPacketCount();
					signalPanel.updateSection(n.getSerialNumber(), n.getAvg(), n.getMinStrength(), n.getMaxStrength(), rssi, n.getPacketCount());

				}
			}
			/*for(SignalData n : listSignalData){
				if(n.getSerialNumber().equals(packet.serial_prefix+ "" + packet.serial_number)){
					System.out.println("Serial Number = "+n.getSerialNumber()+" avg = "+n.getAvg()+" min = "+
					n.getMinStrength()+" max = "+
					n.getMaxStrength()+" current = "+
					n.getCurrentStrength());
				}
			}*/
			System.out.println(packet.getRSSI());
			System.out.println("else if");
		}
		else{
			System.out.println("else");

			//do nothing - wrong serial prefix most likely
		}
		System.err.println(listSerialNumbers.toString());
	}

}
