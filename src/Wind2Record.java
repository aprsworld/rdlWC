import java.util.*;
import java.text.*;

class Wind2Record {
	public String date;
	public double windSpeed[];
	public double windGust[];
	public int windCount[];
	public int windDirection;
	public int temperature;
	public int adc[];
	public double adcVoltage[];
	public double batteryVoltage;
	/* may not be accurate after modifying the string */
	private int lCRC;
	private int rCRC;
	public String wind2record;
	public boolean isValid;

	public Date dlogDate;
	public Date rxDate;

	public String sqlValues() {
		NumberFormat format = new DecimalFormat("0.0");
		StringBuffer s=new StringBuffer();
		
		for ( int i=0 ; i<3 ; i++ ) {
			s.append(format.format(windSpeed[i]) + ",");
			s.append(format.format(windGust[i]) + ",");
			s.append(windCount[i] + ",");
		}
		
		s.append(windDirection + "," + temperature + ",");
		
		for ( int i=0 ; i<8 ; i++ ) {
			s.append(adc[i] + ",");
		}

		s.append(format.format(batteryVoltage));

//		System.out.println("StringBuffer=" + s);

		return s.toString();
	}
	
	/** XOR CRC a string 
	 *
	 * @param line elemnts to be XOR'ed 
	 * @return CRC value 
	 */
	public int CRCString(String line) {
		int crc=0;

		for ( int i=0 ; i<line.length() ; i++ ) {
			crc=crc ^ (int) line.charAt(i);
		}

		return crc;
	}

	/** Convert a 10 bit ADC value to a 0-5 voltage 
	 *
	 * @param adc 10 bit ADC value
	 * @return voltage
	 */
	protected double adcToVoltage(int adc) {
		return (5.0 / 1023.0) * (double) adc;
	}

	/** Convert 0-5 volts to 0-40 volts 
	 *
	 * @param volts 0-5 volt ADC voltage
	 * @return battery voltage in the range of 0 to 40 volts 
	 */
	protected double voltageToBattVoltage(double volts) {
		return volts*8.0;
	}

	/** Determine if record is valid based on its CRC and parsing status
	 *
	 * @return true if valid 
	 */
	public boolean isValid() {
		return (isValid && lCRC==rCRC);
	}

	public Wind2Record(String line, boolean useDlogClock) {
		windSpeed=new double[3];
		windGust=new double[3];
		windCount=new int[3];
		adc=new int[8];
		adcVoltage=new double[8];
		isValid=true;

		rxDate=new Date();

		/* complete record */
		wind2record=line;

		try {
			/* tokenize at commas */
			String rec[]=line.split(",");

			/* CRC element is rec[21] and previous comma */
			int crcLength = rec[21].length() + 1;
			lCRC=CRCString( line.substring(0,line.length()-crcLength) );
			rCRC=Integer.parseInt(rec[21]);

			if ( lCRC != rCRC ) {
				System.err.println("# BAD CRC! Local CRC: " + lCRC + " Received CRC: " + rCRC );
			}

			for ( int i=0 ; i<rec.length ; i++ ) {
				rec[i]=rec[i].trim();
		//		System.out.println("[" + i + "] " + rec[i] );
			}

			/* put into our local data */
			date=rec[0];

			/* parse our MySQL format date (2006-09-25 18:37:43) into a Java date */
			DateFormat sqlDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			dlogDate=sqlDateParser.parse(date);

			windSpeed[0]=Double.parseDouble(rec[1]);
			windGust[0] =Double.parseDouble(rec[2]);
			windCount[0]=Integer.parseInt(rec[3]);
			
			windSpeed[1]=Double.parseDouble(rec[4]);
			windGust[1] =Double.parseDouble(rec[5]);
			windCount[1]=Integer.parseInt(rec[6]);
			
			windSpeed[2]=Double.parseDouble(rec[7]);
			windGust[2] =Double.parseDouble(rec[8]);
			windCount[2]=Integer.parseInt(rec[9]);

			windDirection=Integer.parseInt(rec[10]);
			
			temperature=Integer.parseInt(rec[11]);

			for ( int i=0 ; i<8 ; i++ ) {
				adc[i]=Integer.parseInt(rec[12+i]);
				adcVoltage[i]=adcToVoltage(adc[i]);
			}

			batteryVoltage=Double.parseDouble(rec[20]);
		} catch ( Exception e ) {
			System.err.println("Error parsing line: " + line );
			System.err.println("Exception: " + e);
			isValid=false;
		}

		try {
			/* if we are valid to this point, we replace the data logger provided date
			 * with the system provided date if requested */
			if ( ! useDlogClock ) {
				if ( ',' != wind2record.charAt(19) ) {
					isValid=false;
					return;
				}
				dlogDate=rxDate;

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				/* replace the provided date with our date */
				wind2record = df.format(rxDate) + wind2record.substring(19);

				/* find where our CRC starts */
				int crcAt = wind2record.lastIndexOf(',');
				lCRC=rCRC=CRCString( wind2record.substring(0,crcAt) );

				/* replace provided CRC with our CRC */
				wind2record = wind2record.substring(0,crcAt+1) + lCRC;
			}
		} catch ( Exception e ) {
			System.err.println("Error overwriting data logger clock with local clock on line: " + line);
			System.err.println("Exception: " + e);
			isValid=false;
		}
	}
}
