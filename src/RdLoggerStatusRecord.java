
public class RdLoggerStatusRecord {
	public int pulsePeriod, pulseMinPeriod, pulseCount;
	
	public double inputVoltage;
	public double windSpeed,windGust;
	public boolean sdLogging;
	public int dataflashPercentFull;
	public String date, time, version;
	public boolean dataflashWorking;
	
	public RdLoggerStatusRecord() {
		pulsePeriod=pulseMinPeriod=pulseCount=0;
		windSpeed=windGust=0.0;
		inputVoltage=0.0;
		sdLogging=false;
		dataflashPercentFull=0;
		date="";
		time="";
		version="";
		dataflashWorking=false;
	}
	
	public void parseLine(String line) {
		//System.err.println("RdLoggerStatusRecord parsing line: " + line);
		Anemometer an = new AnemometerNRG40();
		
		try {
			/* tokenize at commas */
			String rec[]=line.split("=");
			
			for ( int i=0 ; i<rec.length ; i++ ) {
				rec[i]=rec[i].trim();
			}
			
			/* determine sentence and parse */
			if ( 0 == rec[0].compareTo("date")) {
				date=rec[1];
			} else if ( 0 == rec[0].compareTo("time")) {
				time=rec[1];
			} else if ( 0 == rec[0].compareTo("version")) {
				version=rec[1];
			} else if ( 0 == rec[0].compareTo("sd_card")) {
				if ( '0' == rec[1].charAt(0) ) 
					sdLogging=true;
			} else if ( 0 == rec[0].compareTo("dataflash_read_status()") ) {
				if ( 0 == rec[1].compareTo("172"))
					dataflashWorking=true;
			} else if ( 0 == rec[0].compareTo("current.input_voltage_adc")) {
				int adc = Integer.parseInt(rec[1]);
				inputVoltage = (40.0 / 1024 ) * adc;
			} else if ( 0 == rec[0].compareTo("current.pulse_count[0]")) {
				pulseCount=Integer.parseInt(rec[1]);
			} else if ( 0 == rec[0].compareTo("current.pulse_period[0]")) {
				pulsePeriod=Integer.parseInt(rec[1]);
				if ( pulsePeriod < 65000 )
					windSpeed = an.tToMS(pulsePeriod/10000.0);
			} else if ( 0 == rec[0].compareTo("current.pulse_min_period[0]")) {
				pulseMinPeriod=Integer.parseInt(rec[1]);
				if ( pulseMinPeriod < 65000 )
					windGust = an.tToMS(pulseMinPeriod/10000.0);
			} else if ( 0 == rec[0].compareTo("dataflash_page")) {
				dataflashPercentFull = (Integer.parseInt(rec[1])*100)/4096;
			}
			
			//System.err.println(rec[0] + " is " + rec[1]);
		} catch ( Exception e ) {
			System.err.println("# Caught exception while parsing line: " + line + "\n" + e);
		}

		//System.out.println("we now have date~" + date + " time~" + time + " sdLogging~" + sdLogging);
		
	}
	
}
