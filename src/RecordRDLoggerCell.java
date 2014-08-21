
import java.util.Date;

public class RecordRDLoggerCell {
	public String serialNumber;
	
	public int lCRC, rCRC;
	public Date rxDate;
	
	public int tPulseTime, tPulseMinTime, windCount;
	public int batteryStateOfCharge;
	public int windDirectionSector;
	
	public double getWindSpeed() {
		if ( tPulseTime > 0 && tPulseTime<65535 )
			return 7650.0 / tPulseTime + 0.35;
		else
			return 0.0;
	}
	public double getWindGust() {
		if ( tPulseMinTime > 0 && tPulseMinTime<65535 )
			return 7650.0 / tPulseMinTime + 0.35;
		else
			return 0.0;
	}	
	
	public RecordRDLoggerCell() {
		lCRC=-1;
		rCRC=-2;
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
				
		/* packet length */
		i = buff[4];
		if ( 15 != i && 19 != i ) {
			return;
		}
		
		/* packet type */
		if ( 7 != buff[5] && 31 != buff[5] ) {
			return;
		}
		
		/* tWindSpeed */
		tPulseTime = (buff[6] << 8 ) + buff[7];
		
		/* tWindGust */
		tPulseMinTime = (buff[8] << 8 ) + buff[9];
		
		/* battery state of charge, percent - comes from high nibble */
		batteryStateOfCharge = (buff[10]>>4)*10;
		
		/* wind direction sector - comes from low nibble */
		windDirectionSector = (buff[10]&0x0F);
		
		/* tWindCount */
		windCount = (buff[11] << 8 ) + buff[12];
		
/*		
		'#'             0  STX
		UNIT ID PREFIX  1  First character (A-Z) for serial number
		UNIT ID MSB     2  high byte of sending station ID
		UNIT ID LSB     3  low byte of sending station ID
		PACKET LENGTH   4  number of byte for packet including STX through CRC (15)
		PACKET TYPE     5  type of packet we are sending (0x07)
		WS MSB          6  high byte of wind speed time
		WS LSB          7  low byte of wind speed time
		WG MSB          8  high byte of wind gust time
		WG LSB          9  low byte of wind gust time
		BATT / WD       10 battery state of charge and wind direction sector
		WC MSB          11 high byte of wind pulse count
		WC LSB          12 low byte of wind pulse count
		four bytes of analog data if packet type 31
		
		CRC MSB         13 high byte of CRC on everything after STX and before CRC
		CRC LSB         14 low byte of CRC
*/
		
		if ( 31 == buff[5] ) {
			/* remote CRC */
			i = (buff[17] << 8) + buff[18];
			rCRC=i;
			lCRC=crc_chk(buff,1,16);
		} else {
			/* remote CRC */
			i = (buff[13] << 8) + buff[14];
			rCRC=i;
			lCRC=crc_chk(buff,1,12);
		}
	}	
	
	public String toString() {
		return "rdLoggerCell serial=" + serialNumber + " tWindSpeed=" + tPulseTime + " tWindGust=" + tPulseMinTime + " batteryStateOfCharge=" + batteryStateOfCharge + 
			" windDirectionSector=" + windDirectionSector;
	}
}
