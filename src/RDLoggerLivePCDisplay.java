

import javax.swing.JOptionPane;

/* master server */
class RDLoggerLivePCDisplay extends Thread implements PacketListener {
	protected String inifile;
	protected String stationID;
	protected IniFile ini;
	WindSmallDisplay disp;
	
	protected RDLUniversalReader remote;
	protected RecordRDLoggerCell rLive;
	
	RDLoggerLivePCDisplay (String inifilename) {
		inifile = inifilename;
	}
	
	public void packetReceived(WorldDataPacket packet) {

		if ( ! packet.isValid()){
			return;
		}
		
//		for ( int i=0 ; i<packet.data.length ; i++ ) {
//			System.err.printf("# packet[%d] 0x%02X\n", i,packet.data[i]);
//		}
		
		System.err.printf("# serial prefix: %c\n",packet.serial_prefix);
		System.err.printf("# serial number: %d\n",packet.serial_number);
		System.err.printf("# packet length: %d\n",packet.length);
		System.err.printf("#   packet type: %d\n",packet.type);
		
		
		
		if ( 7 == packet.type ) {
			/* rdLoggerCell live packet */
			
			String serial = packet.serial_prefix + Integer.toString(packet.serial_number);
			System.err.println("# rdLoggerLive packet serial number = '" + serial + "'");
			
	
			RecordRDLoggerCell r = new RecordRDLoggerCell();
			r.parseRecord(packet.data);
			System.err.println("# decoded: " + r.toString());
			
			if ( null != disp ) {
				disp.updateDisplay(r);
			}
			
		}
		
		
	}

	
	public void run() {
		WindowUtilities.setNativeLookAndFeel();
		
		// Open configuration file
		ini = new IniFile(inifile);
		
		
		
		
		/* GUI screens */
		disp=null;

		String serialPort;
		int serialSpeed;

		System.err.println("# java.library.path " + System.getProperty("java.library.path") );
		serialPort = ini.getValueSafe("SERIAL", "port", "COM1");
		serialSpeed = Integer.parseInt(ini.getValueSafe("SERIAL", "speed", "57600"));
		System.err.println("# Opening " + serialPort + " @ " + serialSpeed);
		
		/* actually make our remote reader */
		try {
			remote = new RDLUniversalReader(serialPort, serialSpeed);
		} catch ( Exception e ) {
			System.err.println("# Exception while connecting to serial port: " + e);
			System.err.println("# Giving up.");
			JOptionPane.showMessageDialog(null,"Error connecting to serial port. Try running setup and selecting the correct serial port.");
			System.exit(1);
		}
		remote.addPacketListener(this);
		
		
		disp = new WindSmallDisplay(ini);

		if ( disp != null ) {
			disp.setVisible(true);
		}


		int errors=0;
		int buff[] = new int[9];
//		while ( null != ( buff=link.getRawPacket('#', buff.length))) {
		while ( errors > 0 ) {
//			RDLoggerLiveRecord rec = new RDLoggerLiveRecord();
			RecordRDLoggerCell rec = new RecordRDLoggerCell();
			
			for ( int i=0 ; i<buff.length ; i++ ) {
				//System.out.print(buff[i] + " ");
				System.out.printf("[%d] 0x%02X\n",i,buff[i]);
				buff[i]=(buff[i] & 0xff);
			}
			System.out.println();

			rec.parseRecord(buff);

			
			if (rec.isValid()) {
				disp.updateDisplay(rec);

//				if ( null != log ) {
//					log.log(line,rec.rxDate);
//				}
//				System.err.println("Errors=" + errors);
			} else {
				errors++;
				System.err.println("Invalid packet received from rdLoggerLive. Errors=" + errors);
			}
			
			
		}
	

	}

	public static void main (String args[]) {
		String ini = null;


		if (args.length == 1) {
			ini = args[0];
		} else {
//			System.err.println("Usage: java RDLoggerLivePCDisplay inifile");
//			System.err.println("Invoke with -Dswing.aatext=true for anti-aliased fonts");
//			System.exit(-1);
			ini = "config_default.ini";
		}

		(new RDLoggerLivePCDisplay(ini)).start();
	}
}
