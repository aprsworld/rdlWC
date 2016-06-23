import java.io.IOException;
import java.util.*;
//import gnu.io.*;
import javax.comm.*;

public class RDLUniversalReader implements SerialPortEventListener {
	LinkSerial link;
	Boolean connected;
	Vector<PacketListener> packetListeners;
	int lengthLoc;
	String thisProcess = null;
	Vector<Integer> buff;
	long lastCharacter;
	RecordDigiAPIRx packetParser = new RecordDigiAPIRx();

	
	public void writeCmd(){
	
	}
	
	public void sendPacket(int buff[]) {
		byte[] ba=new byte[buff.length];

		for ( int i=0 ; i<buff.length ; i++ )
			ba[i]=(byte) (buff[i] & 0xff);

		try {
			link.os.write(ba);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int crc_chk(Vector<Integer> data, int start, int length) {
		int j;
		int reg_crc=0xFFFF;

		for ( int i=start ; i<(start+length) ; i++ ) {
			reg_crc ^= data.elementAt(i);

			for ( j=0 ; j<8 ; j++ ) {
				if ( (reg_crc&0x01) == 1 ) { 
					reg_crc=(reg_crc>>1) ^ 0xA001;
				} else {
					reg_crc=reg_crc>>1;
				}
			}	
		}

		return reg_crc;
	}

	public void addPacketListener(PacketListener b) {
		packetListeners.add(b);
	}


	protected void capturedPacket() {
				System.err.println("# capturedPacket (buff.size()=" + buff.size() + ")");
		int[] buffa = new int[buff.size()];

		for ( int i=0 ; i<buff.size() ; i++ )
			buffa[i]=buff.elementAt(i);

		buff.clear();

		//		System.err.println("# copied buffer over");

		WorldDataPacket packet = new WorldDataPacket(buffa);
		//		System.err.println("# created new WorldDataPacket");

		for ( int i=0 ; i<packetListeners.size(); i++ ) {
			//			System.err.println("# sent to listener: " + i);
			if(packetListeners != null){
				packetListeners.elementAt(i).packetReceived(packet);
			}
		}
	}

	protected void capturedPacketApi() {
		/*		System.err.println("# capturedPacket (buff.size()=" + buff.size() + ")");
		int[] buffa = new int[buff.size()-8];
		int thissize = buff.size()-8;
		int j=8;
		for ( int i=0 ; i<thissize; i++ ){
			
			buffa[i]=buff.elementAt(j);
			j++;
		}*/
		System.err.println("# capturedPacket (buff.size()=" + buff.size() + ")");
		int[] buffa = new int[buff.size()];

		for ( int i=0 ; i<buff.size() ; i++ ){
			buffa[i]=buff.elementAt(i);
		}
		
		packetParser.parseRecord(buffa);
		
		buff.clear();
		
		//		System.err.println("# copied buffer over");
		
		WorldDataPacket packet = new WorldDataPacket(packetParser.dataArr);
		//		System.err.println("# created new WorldDataPacket");
		packet.setOptions(packetParser.options);
		packet.setRSSI(packetParser.rssi);
		packet.setSource(packetParser.source);
		
		for ( int i=0 ; i<packetListeners.size(); i++ ) {
			//			System.err.println("# sent to listener: " + i);
			if(packetListeners != null){
				packetListeners.elementAt(i).packetReceived(packet);
			}
		}
	}
	
	private void tryPacket() {
		//System.err.printf("trypacket","got here");
		if ( buff.size() >= 6 ) {
			/* try our CRC */
			int rLength = buff.elementAt(4);
			if ( buff.size() == rLength ) {
				//				System.err.println("# we have the right length = " + buff.size() );
				int rCRC = (buff.elementAt(rLength-2)<<8) + buff.elementAt(rLength-1);
				int lCRC = crc_chk(buff,1,buff.size()-3);
				System.err.println("rCRC: " + rCRC + " lCRC: " + lCRC);
				if ( lCRC == rCRC ) {
					capturedPacket();
				} else {
					//					buff.clear();
				}
				//				System.err.println("# rCRC=" + rCRC + " lCRC=" + lCRC);
			}
		}						

	}

	private void tryApiPacket() {
		//System.err.printf("trypacket","got here");
		if ( buff.size() >= 6 ) {
			/* try our CRC */
			int rLength = buff.elementAt(lengthLoc);
			rLength=rLength+4;
								System.err.println("# rLength=" + rLength);
			if ( buff.size() == rLength ) {
				//				System.err.println("# we have the right length = " + buff.size() );
				
				int rCRC;// = (buff.elementAt(rLength-2)<<8) + buff.elementAt(rLength-1);
				int sum=0,sum1=0;
				for(int i = 3; i<= buff.size()-2; i++){
					sum+=buff.elementAt(i);
				}
				System.err.print("# sum "+sum);
				rCRC = (byte)((0xFF-sum) & 0xFF);//0xFF-(sum>>8);
				
				int lCRC;// = crc_chk(buff,1,buff.size()-3);
				for(int j = 3; j<=buff.size()-1; j++){
					sum1+=buff.elementAt(j);
					
				}
				lCRC = (sum1 & 0xFF);
				System.err.println("rCRC: " + rCRC + " lCRC: " + lCRC);
				if ( lCRC == 0xFF) {
					capturedPacketApi();
				} else {
					//					buff.clear();
				}
				//				System.err.println("# rCRC=" + rCRC + " lCRC=" + lCRC);
			}
		}						

	}
	
	private void addChar(int c) {

		long now=System.currentTimeMillis();
		long age=now - lastCharacter;

				//System.err.printf("# rx'ed: 0x%02X age: " + age + "\n",c);

		if ( age > 50 && lastCharacter > 0 ) {
			//				System.err.println("# clearing buffer");
			buff.clear();
		}
		lastCharacter=now;

						//System.err.printf("# rx'ed: %03d 0x%02x (buff.size()=%d age=%d)\n", c, c, buff.size(),age);
		//				System.err.flush();

		buff.add(c);
		if(thisProcess == "xbeeSignalStrength" && thisProcess != null){
			

			tryApiPacket();
		}
		else{
		

			tryPacket();
		}

	}

	/* capture a line and send it to capturedBarcode */
	public void serialEvent(SerialPortEvent event) {
		if ( SerialPortEvent.DATA_AVAILABLE == event.getEventType() ) {
			try { 
				while ( link.is.available() > 0 ) {
					int c=0;
					try { 
						c = link.is.read();
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
					addChar(c);
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}

	}


	public RDLUniversalReader(String spName, int spSpeed) {
		buff = new Vector<Integer>();
		packetListeners = new Vector<PacketListener>();
		lastCharacter=0;
		lengthLoc = 4;
		link = new LinkSerial(spName,spSpeed);

		if ( null == link || false == link.Connect()) {
			System.err.println("# Error establishing serial link to device");
			connected=false;
		}
		connected=true;
		
		try {
			link.p.addEventListener(this);
		} catch ( TooManyListenersException e ) {
			System.err.println("# Serial port only supports one SerialPortEventListener!");
		}

		link.p.notifyOnDataAvailable(true);
	}
	public RDLUniversalReader(String spName, int spSpeed,int packetLength) {
		buff = new Vector<Integer>();
		packetListeners = new Vector<PacketListener>();
		lastCharacter=0;
		lengthLoc = packetLength;
		thisProcess = "xbeeSignalStrength";
		link = new LinkSerial(spName,spSpeed);

		if ( null == link || false == link.Connect()) {
			System.err.println("# Error establishing serial link to device");
			connected=false;
		}
		connected=true;
		
		try {
			link.p.addEventListener(this);
		} catch ( TooManyListenersException e ) {
			System.err.println("# Serial port only supports one SerialPortEventListener!");
		}

		link.p.notifyOnDataAvailable(true);
	}
	
	public void close() {
		link.Disconnect();
	}
}
