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

	Boolean debug=false;


	protected void capturedPacketApi() {
		if ( debug )
			System.err.println("# capturedPacketApi (buff.size()=" + buff.size() + ")");

		int[] buffa = new int[buff.size()];

		for ( int i=0 ; i<buff.size() ; i++ ){
			buffa[i]=buff.elementAt(i);
		}

		packetParser.parseRecord(buffa);

		buff.clear();

		WorldDataPacket packet = new WorldDataPacket(packetParser.dataArr);

		packet.setOptions(packetParser.options);
		packet.setRSSI(packetParser.rssi);
		packet.setSource(packetParser.source);

		for ( int i=0 ; i<packetListeners.size(); i++ ) {
			//			System.err.println("# sent to listener: " + i);
			if ( packetListeners != null ) {
				packetListeners.elementAt(i).packetReceived(packet);
			}
		}
	}

	private void _tryApiPacket() {
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
		//		if ( debug )
		if ( buff.size() != 15 ) {			
			StringBuilder sb=new StringBuilder();
			/* Serial number */
			sb.append((char) ( (int) buff.elementAt(1)) );
			sb.append((buff.elementAt(2) << 8) + buff.elementAt(3)); 

			System.err.println("# capturedPacket (buff.size()=" + buff.size() + ") serial='" + sb + "'");

			if ( debug ) {
				System.err.println("# buff dump:");
				for ( int i=0 ; i<buff.size() ; i++ ) {
					System.err.printf("# [%2d] 0x%02X\n", i,buff.elementAt(i));
				}
			}
		}


		int[] buffa = new int[buff.size()];

		for ( int i=0 ; i<buff.size() ; i++ )
			buffa[i]=buff.elementAt(i);

		if ( debug ) {
			System.out.println("# capturedPacket() buff.clear()");
		}
		buff.clear();

		//		System.err.println("# copied buffer over");

		WorldDataPacket packet = new WorldDataPacket(buffa);
		//		System.err.println("# created new WorldDataPacket");

		for ( int i=0 ; i<packetListeners.size(); i++ ) {
			//			System.err.println("# sent to listener: " + i);
			if ( packetListeners != null ) {
				packetListeners.elementAt(i).packetReceived(packet);
			}
		}
	}


	private void tryPacket() {

		if ( buff.size() >= 8 ) {
			/* try our CRC */
			int rLength = buff.elementAt(4);
			
			if ( 0xff == rLength ) {
				rLength = (buff.elementAt(6)<<8) + buff.elementAt(7);
			}
			
			if ( buff.size() == rLength ) {

				if ( debug ) {
					System.err.print("# tryPacket() length=" + buff.size() );

					StringBuilder sb=new StringBuilder();
					/* Serial number */
					sb.append((char) ( (int) buff.elementAt(1)) );
					sb.append((buff.elementAt(2) << 8) + buff.elementAt(3)); 

					System.err.print(" serial '" + sb  + "'");

				}

				/* check CRC of packet */
				int rCRC = (buff.elementAt(rLength-2)<<8) + buff.elementAt(rLength-1);
				int lCRC = crc_chk(buff,1,buff.size()-3);

				if ( debug ) {
					System.out.println("# tryPacket() rCRC: 0x" + Integer.toHexString(rCRC).toUpperCase() + " lCRC: 0x" + Integer.toHexString(lCRC).toUpperCase() );
					System.out.flush();
				}

				if ( lCRC == rCRC ) {
					/* valid packet */
					capturedPacket();

					if ( buff.size() > 0 ) {
						if ( debug ) {
							System.out.println("-----------> buff.size()=" + buff.size() + " after capturedPacket()");
						}
					}
				} else {
					if ( debug ) {
						System.out.println("# tryPacket() CRC mis-match!");
					}
					//					buff.clear();
				}
				//				System.err.println("# rCRC=" + rCRC + " lCRC=" + lCRC);
			}
		}						

	}



	private void addChar(long now, int c) {
		//		long now=System.currentTimeMillis();
		long age=now - lastCharacter;

		//System.err.printf("# rx'ed: 0x%02X age: " + age + "\n",c);

		if ( buff.size() > 0 && age > 50 && lastCharacter > 0 ) {
			if ( debug ) {
				System.out.println("# addChar() clearing buffer due to buff.size() > 0 AND age > 50 AND lastCharacter > 0");
				System.out.println("# addChar() buff.size() was " + buff.size() );			
			}
			buff.clear();
		}
		lastCharacter=now;

		//System.err.printf("# rx'ed: %03d 0x%02x (buff.size()=%d age=%d)\n", c, c, buff.size(),age);
		//				System.err.flush();

		buff.add(c);

		//		System.out.println(System.currentTimeMillis() + "\t" + c + "\t0x" + Integer.toHexString(c).toUpperCase() + "\t" + buff.size() + "\t(addChar)");
		//		System.out.flush();

		//		if( thisProcess == "xbeeSignalStrength" && thisProcess != null ) {
		//			tryApiPacket();
		//		} else{
		tryPacket();
		//		}

	}

	private int lastFour[]=new int[4];

	public void serialEvent(SerialPortEvent event) {
		long now;


		if ( SerialPortEvent.DATA_AVAILABLE == event.getEventType() ) {
			try { 
				while ( link.is.available() > 0 ) {
					int c=0;
					try { 
						c = link.is.read();
						now=System.currentTimeMillis();
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}

					if ( debug ) {
						System.out.println( now + "\t" + c + "\t0x" + Integer.toHexString(c).toUpperCase() + "\t" + buff.size() + "\t(serialEvent before addChar() call)");
						System.out.flush();

						lastFour[0]=lastFour[1];
						lastFour[1]=lastFour[2];
						lastFour[2]=lastFour[3];
						lastFour[3]=c;

						if ( 0x23==lastFour[0] && 0x52==lastFour[1] && 0x00==lastFour[2] ) {
							System.out.printf("#----------------------------> likely beginning of packet 0x%02X 0x%02X 0x%02X 0x%02X\n",lastFour[0],lastFour[1],lastFour[2], lastFour[3]);
							System.out.flush();
						}
					}

					addChar(now,c);
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
