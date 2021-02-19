

import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

import net.sf.marineapi.nmea.sentence.*;

import org.apache.commons.text.*;

/* master server */
class RDLoggerLivePCDisplay extends Thread implements PacketListener {
	protected String inifile;
	protected String stationID;
	protected IniFile ini;
	WindSmallDisplay disp;
	protected long myPID;

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


	protected void logVectorWindXTC(RecordVectorWindXTC r) {
		//		System.err.println("# logFull() received: " + r);

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(r.rxDate);
		
		String filenameMiddle=ini.getValueSafe("LIVELOG","filenameMiddle","_LIVE_");
		String filenameLast=ini.getValueSafe(r.serialNumber, "filenameLast","");
		
		
		String filename=String.format("%s/%s_%04d%02d%02d%s%s.csv",
				liveLogDirectory,
				r.serialNumber,
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				filenameMiddle,
				filenameLast
				);

		System.err.println("# log() generated filename: " + filename);


		
		NumberFormat f = new DecimalFormat("0.0");

		
		String header="DATE,SERIAL,WIND SPEED (m/s),WIND GUST (m/s),WIND COUNT,PULSE TIME,PULSE MIN TIME,INPUT VOLTAGE ADC,INPUT VOLTAGE,VERTICAL INPUT ADC,VERTICAL WIND SPEED (m/s),WIND VANE ADC,WIND VANE DIRECTION (degrees),SEQUENCE NUMBER,LIVE AGE (milliseconds),GNSS AGE (milliseconds),GNSS NMEA";		 


		
		
		String csv=String.format("%04d-%02d-%02d %02d:%02d:%02d,%s,%s,%s,%d,%d,%d,%d,%s,%d,%s,%d,%d,%d,%d,%d,%s",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				r.serialNumber,
				f.format(r.getWindSpeed0()),
				f.format(r.getWindGust0()),
				r.windCount0,
				r.tPulseTime0,
				r.tPulseMinTime0,
				r.input_voltage_adc,
				f.format(r.getInputVoltage()),
				r.vertical_anemometer_adc,
				f.format(r.getVerticalWindSpeed()),
				r.wind_vane_adc,
				r.getWindVaneDirection(),
				r.sequence,
				r.live_age_milliseconds,
				r.gnss_age_milliseconds,
				StringEscapeUtils.escapeCsv(r.gnss_nmea)
				);
		
		/* add additional GNSS data */
		
		/* HDT sentence */
		HDTSentence hdt = (HDTSentence) r.gnss_sentences.get("HDT");
		header += ",HDT VALID,HDT HEADING (DEGREES),HDT TRUE";
		if ( null != hdt ) {
			if ( hdt.isValid() ) {
				csv += String.format(",1,%s,%d",f.format(hdt.getHeading()),hdt.isTrue() ? 1 : 0);
			} else {
				csv += ",0,,";
			}
		} else {
			csv += ",,,";
		}

		/* GGA sentence */
		GGASentence gga = (GGASentence) r.gnss_sentences.get("GGA");
		header += ",GGA VALID,GGA TIME,GGA LATITUDE,GGA LONGITUDE,GGA QUALITY,GGA N SV,GGA HDOP (meters),GGA HEIGHT (meters),GGA DGPS AGE,GGA REF STATION";
		if ( null != gga ) {
			if ( gga.isValid() ) {
				csv += String.format(",1,%s,%s,%s,%s,%s,%s,%s,%s,%s",
						gga.getTime(),
						gga.getPosition().getLatitude(),
						gga.getPosition().getLongitude(),
						gga.getFixQuality(),
						gga.getSatelliteCount(),
						gga.getHorizontalDOP(),
						gga.getPosition().getAltitude(),
						gga.getDgpsAge(),
						gga.getDgpsStationId()
						);
			} else {
				csv += ",0,,,,,,,,,";
			}
		} else { 
			csv += ",,,,,,,,,,";
		}
		
		
		
		/* RMC sentence */
		RMCSentence rmc = (RMCSentence) r.gnss_sentences.get("RMC");
		header += ",RMC VALID,RMC DATE,RMC TIME,RMC STATUS,RMC LATITUDE,RMC LONGITUDE,RMC SPEED OVER GROUND (knots),RMC TRACK ANGLE TRUE (degrees),RMC MAGNETIC VARIATION (degrees)";
		if ( null != rmc ) {
			if ( rmc.isValid() ) {
			csv += String.format(",1,%s,%s,%s,%s,%s,%s,%s,%s",
					rmc.getDate(),
					rmc.getTime(),
					rmc.getStatus(),
					rmc.getPosition().getLatitude(),
					rmc.getPosition().getLongitude(),
					f.format(rmc.getSpeed()),
					f.format(rmc.getCourse()),
					f.format(rmc.getVariation())
					);
			} else {
				csv += ",0,,,,,,,,";
			}
		} else {
			csv += ",,,,,,,,,";
		}

		
		
		
		/*
		HDT VALID	HDT HEADING (DEGREES)	HDT TRUE		
		
		GGA VALID	GGA UTC OF FIX	GGA LATITUDE

		*/

		
		
		
		
//		String escaped = StringEscapeUtils.escapeCsv
		
		System.err.println("# log() CSV '" + csv + "'");


		LogProcess log = new LogProcess(false);
		log.createLog(filename,header +  System.getProperty("line.separator"));
		log.writeLog(csv + System.getProperty("line.separator"));
		log.closeLog();
	}

	
	protected void logCMPS12(RecordRDLoggerCellCMPS12 r) {
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

		/* header to copy and paste into Excel:
DATE	SERIAL	WIND SPEED	WIND GUST	WIND COUNT	PULSE TIME	PULSE MIN TIME	CMPS12 BEARING	BOSCH BEARING	PITCH	ROLL	CAL System	CAL Gyro	CAL Accel	CAL Magnet	TURN TABLE	CMPS12 raw registers ->	0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26	27	28	29	30
		 */
		
		String csv=String.format("%04d-%02d-%02d %02d:%02d:%02d, %s, %s, %s, %d, %d, %d, %s, %s, %d, %d, %d, %d, %d, %d, %d,",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND),
				r.serialNumber,
				f.format(r.getWindSpeed0()),
				f.format(r.getWindGust0()),
				r.windCount0,
				r.tPulseTime0,
				r.tPulseMinTime0,
				f.format(r.getBearingCMPS12()),
				f.format(r.getBearingBosch()),
				r.getPitch(),
				r.getRoll(),
				r.getCalibrationCMPS12Sy(),
				r.getCalibrationCMPS12Gy(),
				r.getCalibrationCMPS12Ac(),
				r.getCalibrationCMPS12Ma(),
				tableAngle
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
		/* this is the data format that is used in production for Sandia's systems. File format cannot be changed without 
		 * coordination with Sandia. Don't do it.
		 */

		//		System.err.println("# logFull() received: " + r);

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(r.rxDate);

		String filenameMiddle=ini.getValueSafe("LIVELOG","filenameMiddle","_LIVE_");
		String filenameLast=ini.getValueSafe(r.serialNumber, "filenameLast","");
		
		
		String filename=String.format("%s/%s_%04d%02d%02d%s%s.csv",
				liveLogDirectory,
				r.serialNumber,
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH),
				filenameMiddle,
				filenameLast
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

		
		/* get headers from ini file */
		StringBuilder header = new StringBuilder();
		
		if ( ini.hasSubject(r.serialNumber) ) {
			System.err.println("# ini file has a subject of `" + r.serialNumber + "'");
			
			/* attempt to read header lines until we don't get one */
			for ( int i=0 ; i<10 ; i++ ) {
				String s = ini.getValue(r.serialNumber, "header" + i);
				
				if ( null != s ) {
					header.append(s);
					header.append(System.getProperty("line.separator"));
				} else {
					break;
				}
			}
			
		}
		
		
		
		LogProcess log = new LogProcess(true);
		
		log.createLog(filename,header.toString());
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

//			System.out.println("# decoded: " + r.toString());
//			System.out.flush();

//			System.out.println("# wind direction=" + r.getWindDirectionFromAnalog0());
//			System.out.println("#          pitch=" + r.getPitchFromAnalog1());
//			System.out.println("#           roll=" + r.getRollFromAnalog1());

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

		} else if ( 38 == packet.type ) {
			/* vectorWindXTC */
			String serial = packet.serial_prefix + Integer.toString(packet.serial_number);
			//					System.out.println("# rdLoggerLive packet serial number = '" + serial + "'");


			RecordVectorWindXTC r = new RecordVectorWindXTC();
			r.parseRecord(packet.data);
			System.out.println("# VectorWindXTC decoded: " + r.toString());


			if ( null != disp ) {
				disp.updateDisplayVectorWindXTC(r);
			}

			/* log if we have something that appears to be a directory */
			if ( 0 != liveLogDirectory.compareTo("") ) {
				logVectorWindXTC(r);
			}
			
		}


	}


	public void run() {
		WindowUtilities.setNativeLookAndFeel();

		/* get PID at start of run ... somehow it can change throughout program execution */
		myPID = getPID();
		
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
