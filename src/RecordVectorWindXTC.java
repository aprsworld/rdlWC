
import java.util.Date;
import java.util.HashMap;

import net.sf.marineapi.nmea.parser.*;
import net.sf.marineapi.nmea.sentence.*;


public class RecordVectorWindXTC {
	final static boolean debug=false;
	
	public String serialNumber;
	
	public int lCRC, rCRC;
	public Date rxDate;
	
	public int tPulseTime0, tPulseMinTime0, windCount0;
	public int input_voltage_adc, vertical_anemometer_adc, wind_vane_adc;
	
	public int sequence;
	public int live_age_milliseconds;
	
	public int gnss_age_milliseconds;
	public String gnss_nmea;
	
	public HashMap<String, Sentence> gnss_sentences;
	
	public double getWindSpeed0() {
		if ( tPulseTime0 > 0 && tPulseTime0<65535 )
			return 7650.0 / tPulseTime0 + 0.35;
		else
			return 0.0;
	}
	public double getWindGust0() {
		if ( tPulseMinTime0 > 0 && tPulseMinTime0<65535 )
			return 7650.0 / tPulseMinTime0 + 0.35;
		else
			return 0.0;
	}	
	
	public double getInputVoltage() {
		return (input_voltage_adc*(3.3/1024.0))/(2200.0/(2200.0+10000.0));
	}
	
	public double getVerticalWindSpeed() {
		if ( vertical_anemometer_adc < 10 || vertical_anemometer_adc > 1014 )
			return 0.0;
		
		
		return 17.777777*(3.3/1024)*vertical_anemometer_adc - 44.444444;
	}

	public Double getVerticalWindSpeedRangeChecked() {
		if ( vertical_anemometer_adc < 10 || vertical_anemometer_adc > 1014 )
			return null;
		
		
		return new Double(17.777777*(3.3/1024)*vertical_anemometer_adc - 44.444444);
	}

	
	public int getWindVaneDirection() {
		return (int) ((360.0/1024)*wind_vane_adc);
	}
	
	
	public RecordVectorWindXTC() {
		lCRC=-1;
		rCRC=-2;
		gnss_sentences=new HashMap<String, Sentence>(4);
	}
	
	public boolean isValid() {
		return lCRC==rCRC;
	}
	
	public int crc_chk(int data[],int start, int length) {
		int j;
		int reg_crc=0xFFFF;

		for ( int i=start ; i<(length+start) ; i++ ) {
			reg_crc ^= data[i];

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
	
	
	public void parseRecord(int[] buff) {
		rxDate = new Date();
		
		StringBuilder sb = new StringBuilder();
		int i;
		
		/* Serial number */
		sb.append((char) buff[1]);
		i = (buff[2] << 8) + buff[3]; 
		sb.append(i);
		serialNumber=sb.toString();
		//System.err.print("Serial Number=" + serialNumber + " ");

		/* 

	buff[0]='#';
	buff[1]=SERIAL_PREFIX;
	buff[2]=make8(SERIAL_NUMBER,1);
	buff[3]=make8(SERIAL_NUMBER,0);
	since packet length will exceed 254, we will use extended packet length in buff[6] and buff[7]
	buff[4]=0xff; 
	buff[5]=38; 
	buff[6]=make8(packet_length,1);
	buff[7]=make8(packet_length,0);

	buff[8]=make8(current.sequence,1);
	buff[9]=make8(current.sequence,0);

	wind counter 
	buff[10]=make8(current.strobed_pulse_period,1); wind speed 
	buff[11]=make8(current.strobed_pulse_period,0);
	buff[12]=make8(current.strobed_pulse_min_period,1); wind gust
	buff[13]=make8(current.strobed_pulse_min_period,0); 
	buff[14]=make8(current.strobed_pulse_count,1); wind pulse count 
	buff[15]=make8(current.strobed_pulse_count,0); 

	analog
	buff[16]=make8(current.input_voltage_adc,1);
	buff[17]=make8(current.input_voltage_adc,0);
	buff[18]=make8(current.vertical_anemometer_adc,1);
	buff[19]=make8(current.vertical_anemometer_adc,0);
	buff[20]=make8(current.wind_vane_adc,1);
	buff[21]=make8(current.wind_vane_adc,0);

	META data
	buff[22]=current.live_age

	NMEA data
	buff[23]=current.gnss_age;

	CRC of the first part of the packet
	lCRC=crc_chk_seeded(0xFFFF, buff+1, 23);

	CRC with the addition of the raw NMEA data
	lcrc = crc_chk_seeded(lCRC, current.gnss_buff, current.gnss_length);
	
		*/		
		
		
		/* packet length */
		i = buff[4];
		if ( 0xff != i ) {
			return;
		}
		
		/* packet type */
		if ( 38 != buff[5] ) {
			return;
		}
		
		int packet_length = (buff[6] << 8 ) + buff[7];
		sequence = (buff[8] << 8 ) + buff[9];
		
		if ( debug ) {
			System.err.println("# packet_length " + packet_length);
			System.err.println("# sequence " + sequence);
		}
		
		/* anemometer 0 */
		tPulseTime0 = (buff[10] << 8 ) + buff[11];
		tPulseMinTime0 = (buff[12] << 8 ) + buff[13];
		windCount0 = (buff[14] << 8 ) + buff[15];
		
		/* analogs */
		input_voltage_adc = (buff[16] << 8) + buff[17];
		vertical_anemometer_adc = (buff[18] << 8) + buff[19];
		wind_vane_adc = (buff[20] << 8) + buff[21];
		
		live_age_milliseconds = buff[22]*10; /* in 10 millisecond counts */
		gnss_age_milliseconds = buff[23]*10; /* in 10 millisecond counts */
		
		/* GNSS raw NMEA data */
		sb.setLength(0);
		for ( i=24 ; i<(packet_length-2) ; i++ ) {
			sb.append( (char) buff[i]);
		}
		gnss_nmea=sb.toString();
		
		
		/* split NMEA string into multiple strings */
		String[] n=gnss_nmea.split("\\$");

		System.err.println("# n.length=" + n.length + " n[1] = " + n[1]);
		
		for ( i=0 ; i<n.length ; i++ ) {
			/* make sure we are long enough to even have a chance */
			if ( n[i].length() < 5 )
				continue;
			
			SentenceFactory sf = SentenceFactory.getInstance();
			
			String sentence= "$" + n[i];
		
			if ( debug ) {
				System.err.println("# NMEA sentence we are going to try '" + sentence + "'");
			}
	
			Sentence s=sf.createParser(sentence);
				
			if ( null != s && s.isValid() ) {
				/* isValid hopefully checks the checksum */
				gnss_sentences.put(s.getSentenceId(),s);
			}
				
				
/*				
				RMCSentence rmc = (RMCSentence) sf.createParser(sentence);
				
				System.err.println("# RMC sentence ID: " + rmc.getSentenceId() );
				System.err.println("# RMC date:        " + rmc.getDate().toISO8601() );
				System.err.println("# RMC time:        " + rmc.getTime().toISO8601() );
				System.err.println("# RMC position:    " + rmc.getPosition() );
				System.err.println("# RMC variation:   " + rmc.getVariation() );
				System.err.println("# RMC mode:        " + rmc.getMode() );
				System.err.println("# RMC status:      " + rmc.getStatus() );
				System.err.println("# RMC valid:       " + rmc.isValid() );
*/
			
		}
		
		if ( debug ) {
			/* this doesn't work now that it is a hash map */
			for ( i=0 ; i<gnss_sentences.size() ; i++ ) {
				Sentence s=gnss_sentences.get(i);

				System.err.println("# " + serialNumber + " gnss_sentences[" + i + "]:");

				if ( "RMC".equals(s.getSentenceId()) ) {
					RMCSentence rmc = (RMCSentence) s;

					//				System.err.println("# RMC sentence ID: " + rmc.getSentenceId() );
					System.err.println("# RMC date:        " + rmc.getDate().toISO8601() );
					System.err.println("# RMC time:        " + rmc.getTime().toISO8601() );
					System.err.println("# RMC position:    " + rmc.getPosition() );
					System.err.println("# RMC variation:   " + rmc.getVariation() );
					System.err.println("# RMC mode:        " + rmc.getMode() );
					System.err.println("# RMC status:      " + rmc.getStatus() );
					//				System.err.println("# RMC valid:       " + rmc.isValid() );

				} else if ( "HDT".equals(s.getSentenceId()) ) {
					HDTSentence hdt = (HDTSentence) s;

					//				System.err.println("# HDT heading:     " + hdt.getHeading() );
					System.err.println("# HDT is true:     " + hdt.isTrue() );
				} else {
					System.err.println("# " + s.getSentenceId() + " unsupported NMEA sentence type");

				}
			}
		}

		
		/* remote CRC */
		rCRC = (buff[i] << 8) + buff[i+1];
		lCRC=crc_chk(buff,1,i-1);

		if ( debug ) {
			for ( i=0 ; i<130 ; i++ ) {
				System.err.printf("# buff[%3d]=0x%02X\n",i,buff[i] );
			}
			System.err.printf("# rCRC=0x%04X lCRC=0x%04X\n",rCRC,lCRC);
		}
		
	
	}	
	
	public String toString() {
		return "RecordVectorWindXTC serial=" + serialNumber ;
	}
}
