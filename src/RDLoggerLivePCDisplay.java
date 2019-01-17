

import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

/* master server */
class RDLoggerLivePCDisplay extends Thread implements PacketListener {
	protected String inifile;
	protected String stationID;
	protected IniFile ini;
	WindSmallDisplay disp;

	protected RDLUniversalReader remote;
	protected RecordRDLoggerCell rLive;
	
	protected int tableAngle;
	protected boolean tableDone=false;
	LinkSerial turnTableSerial=null;

	boolean debug=false;

	protected String liveLogDirectory;

	RDLoggerLivePCDisplay (String inifilename) {
		inifile = inifilename;
		tableAngle=-1;
	}
	
	protected void setTableAngle(int degrees) {
		Toolkit.getDefaultToolkit().beep(); 
		
		if ( turnTableSerial == null ) {
			String serialPort = ini.getValueSafe("TURNTABLE", "serial_port", "COM1");
			int serialSpeed = Integer.parseInt(ini.getValueSafe("TURNTABLE", "serial_speed", "57600"));
		
			System.err.printf("# setTableAngle connecting to serial port %s @ %d\n",serialPort,serialSpeed);
			turnTableSerial = new LinkSerial(serialPort, serialSpeed);
			turnTableSerial.Connect();
		}
		
		int feed = Integer.parseInt(ini.getValueSafe("TURNTABLE", "feed", "250"));

		String cncCommand = String.format("G1 F%d X%d", feed, degrees);
		System.err.println("# setTableAngle() sending '" + cncCommand + "'");
		turnTableSerial.sendLine("F" + feed + " X" + degrees + "\r");
		if ( turnTableSerial.dataReady() ) {
			System.err.println("# CNC said: '" + turnTableSerial.getLine() + "'");
		}
		
		//turnTableSerial.Disconnect();
		
		tableAngle=degrees;
		
		if ( null != disp ) {
			disp.setTurnTableDegrees(tableAngle);
		}
	}
		
	protected void tableAction() { 
		if ( tableDone ) {
			return;
		}
		
		if ( ini.getValueSafe("TURNTABLE", "enabled", "0").compareTo("1") != 0 ) {
			System.err.println("TURNTABLE not enabled. Skipping.");
			return;
		}
		
		setTableAngle(tableAngle);

	
		
		if ( tableAngle > Integer.parseInt(ini.getValueSafe("TURNTABLE", "exit_greater", "-1")) ) {
			tableDone=true;
		}

		tableAngle += Integer.parseInt(ini.getValueSafe("TURNTABLE", "degrees", "15"));
		
	}

	public static long getPID() {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		return Long.parseLong(processName.split("@")[0]);
	}


	protected void logCMPS12(RecordRDLoggerCellCMPS12 r) {
		//		System.err.println("# logFull() received: " + r);

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(r.rxDate);

		String filename=String.format("%s/%s_%04d%02d%02d_%d_LIVE.csv",
				liveLogDirectory,
				r.serialNumber,
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				getPID()
				);
		System.err.println("# log() generated filename: " + filename);

/*		
		String csv=String.format("%04d-%02d-%02d %02d:%02d:%02d, %s, %2.1f, %2.1f, %d, %d, %d, %d, %d, %d, %d, %d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				r.serialNumber,
				r.getWindSpeed0(),
				r.getWindGust0(),
				r.windCount0,
				-1, // r.windDirectionSector,
				-1, // r.batteryStateOfCharge,
				r.tPulseTime0,
				r.tPulseMinTime0,
				r.getWindDirectionFromAnalog0(),
				r.getPitch(),
				r.getRoll()
				);
*/
		
		NumberFormat f = new DecimalFormat("0.0");
		
		String csv=String.format("%04d-%02d-%02d %02d:%02d:%02d, %s, %d, %s, %s, %d, %d, %d, %d, %d, %d,",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				r.serialNumber,
				tableAngle,
				f.format(r.getBearingCMPS12()),
				f.format(r.getBearingBosch()),
				r.getPitch(),
				r.getRoll(),
				r.getCalibrationCMPS12Sy(),
				r.getCalibrationCMPS12Gy(),
				r.getCalibrationCMPS12Ac(),
				r.getCalibrationCMPS12Ma()
				);
		
		for ( int i=0 ; i<r.cmps12_register.length ; i++ ) {
			csv = csv.concat(", " + r.cmps12_register[i]);
		}
		
		System.err.println("# log() CSV '" + csv + "'");


		LogProcess log = new LogProcess(false);
		log.createLog(filename);
		log.writeLog(csv + System.getProperty("line.separator"));
		log.closeLog();
	}

	protected void logFull(RecordRDLoggerCellFull r) {
		//		System.err.println("# logFull() received: " + r);

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(r.rxDate);

		String filename=String.format("%s/%s_%04d%02d%02d_LIVE.csv",
				liveLogDirectory,
				r.serialNumber,
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH)
				);
		//System.err.println("# log() generated filename: " + filename);

		String csv=String.format("%04d-%02d-%02d %02d:%02d:%02d, %s, %2.1f, %2.1f, %d, %d, %d, %d, %d, %d, %d, %d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				r.serialNumber,
				r.getWindSpeed0(),
				r.getWindGust0(),
				r.windCount0,
				-1, // r.windDirectionSector,
				-1, // r.batteryStateOfCharge,
				r.tPulseTime0,
				r.tPulseMinTime0,
				r.getWindDirectionFromAnalog0(),
				r.getPitchFromAnalog1(),
				r.getRollFromAnalog1()
				);

		System.err.println("# log() CSV '" + csv + "'");


		LogProcess log = new LogProcess(false);
		log.createLog(filename);
		log.writeLog(csv + System.getProperty("line.separator"));
		log.closeLog();
	}

	protected void log(RecordRDLoggerCell r) {
		//		System.err.println("# log() received: " + r);

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(r.rxDate);

		String filename=String.format("%s/%s_%04d%02d%02d_LIVE.csv",
				liveLogDirectory,
				r.serialNumber,
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH)
				);
		//System.err.println("# log() generated filename: " + filename);

		String csv=String.format("%04d-%02d-%02d %02d:%02d:%02d, %s, %2.1f, %2.1f, %d, %d, %d, %d, %d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				r.serialNumber,
				r.getWindSpeed(),
				r.getWindGust(),
				r.windCount,
				r.windDirectionSector,
				r.batteryStateOfCharge,
				r.tPulseTime,
				r.tPulseMinTime
				);

		System.err.println("# log() CSV '" + csv + "'");


		LogProcess log = new LogProcess(false);
		log.createLog(filename);
		log.writeLog(csv + System.getProperty("line.separator"));
		log.closeLog();
	}


	public void packetReceived(WorldDataPacket packet) {

		if ( ! packet.isValid()){
			return;
		}

		//		for ( int i=0 ; i<packet.data.length ; i++ ) {
		//			System.err.printf("# packet[%d] 0x%02X\n", i,packet.data[i]);
		//		}

		if ( debug ) {
			System.err.printf("# serial prefix: %c\n",packet.serial_prefix);
			System.err.printf("# serial number: %d\n",packet.serial_number);
			System.err.printf("# packet length: %d\n",packet.length);
			System.err.printf("#   packet type: %d\n",packet.type);
		}



		if ( 7 == packet.type ) {
			/* rdLoggerCell live packet */

			String serial = packet.serial_prefix + Integer.toString(packet.serial_number);
			//			System.out.println("# rdLoggerLive packet serial number = '" + serial + "'");


			RecordRDLoggerCell r = new RecordRDLoggerCell();
			r.parseRecord(packet.data);
			System.out.println("# decoded: " + r.toString());
			System.out.flush();

			if ( null != disp ) {
				disp.updateDisplay(r);
			}

			/* log if we have something that appears to be a directory */
			if ( 0 != liveLogDirectory.compareTo("") ) {
				log(r);
			}

		} if ( 36 == packet.type ) {
			/* rdLoggerCellFull live packet */

			String serial = packet.serial_prefix + Integer.toString(packet.serial_number);
			//			System.out.println("# rdLoggerLive packet serial number = '" + serial + "'");


			RecordRDLoggerCellFull r = new RecordRDLoggerCellFull();
			r.parseRecord(packet.data);
			System.out.println("# decoded: " + r.toString());
			System.out.flush();

			System.out.println("# wind direction=" + r.getWindDirectionFromAnalog0());
			System.out.println("#          pitch=" + r.getPitchFromAnalog1());
			System.out.println("#           roll=" + r.getRollFromAnalog1());

			if ( null != disp ) {
				disp.updateDisplayFull(r);
			}

			/* log if we have something that appears to be a directory */
			if ( 0 != liveLogDirectory.compareTo("") ) {
				logFull(r);
			}

		} else if ( 37 == packet.type ) {

			/* rdLoggerCell CMPS12 packet */

			String serial = packet.serial_prefix + Integer.toString(packet.serial_number);
			//					System.out.println("# rdLoggerLive packet serial number = '" + serial + "'");


			RecordRDLoggerCellCMPS12 r = new RecordRDLoggerCellCMPS12();
			r.parseRecord(packet.data);
			System.out.println("# CMPS12 decoded: " + r.toString());


			if ( null != disp ) {
				disp.updateDisplayCMPS12(r);
			}

			/* log if we have something that appears to be a directory */
			if ( 0 != liveLogDirectory.compareTo("") ) {
				logCMPS12(r);
			}

			tableAction();

		}


	}


	public void run() {
		WindowUtilities.setNativeLookAndFeel();

		// Open configuration file
		ini = new IniFile(inifile);

		/* set table angle to 0 */
		if ( ini.getValueSafe("TURNTABLE", "enabled", "0").compareTo("1") == 0 ) {
			setTableAngle(0);
			
			/* wait 5 seconds to table to get into position */
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/* now normal program startup */
		}


		/* GUI screens */
		disp=null;

		String serialPort;
		int serialSpeed;

		System.err.println("# java.library.path " + System.getProperty("java.library.path") );
		serialPort = ini.getValueSafe("SERIAL", "port", "COM1");
		serialSpeed = Integer.parseInt(ini.getValueSafe("SERIAL", "speed", "57600"));
		System.err.println("# Opening " + serialPort + " @ " + serialSpeed);

		liveLogDirectory = ini.getValueSafe("LIVELOG","directory","");
		if ( 0 == liveLogDirectory.compareTo("") ) {
			System.err.println("# Logging of live data disabled because [LIVELOG] directory is null");
		} else { 
			System.err.println("# Live data logging directory `" + liveLogDirectory + "`");
		}


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
