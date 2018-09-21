import java.util.Date;

public class RecordRDLoggerCellFull {
	public String serialNumber;
	
	public int lCRC, rCRC;
	public Date rxDate;
	
	public int tPulseTime0, tPulseMinTime0, windCount0;
	public int tPulseTime1, tPulseMinTime1, windCount1;
	public int analog0_adc, analog1_adc;
	public int batt_adc;
	
	public int getWindDirectionFromAnalog0() {
		return analog0_adc;
	}
	
	public int getPitchFromAnalog1() {
		int pitch=(analog1_adc>>8);
		
		if ( pitch > 128 ) {
			pitch -= 256;
		}
		
		return pitch;
	}
	
	public int getRollFromAnalog1() {
		int roll=(analog1_adc & 0xff);
		
		if ( roll > 128 ) {
			roll -= 256;
		}
		
		return roll;
	}
	
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
	
	public double getWindSpeed1() {
		if ( tPulseTime1 > 0 && tPulseTime1<65535 )
			return 7650.0 / tPulseTime1 + 0.35;
		else
			return 0.0;
	}
	public double getWindGust1() {
		if ( tPulseMinTime1 > 0 && tPulseMinTime1<65535 )
			return 7650.0 / tPulseMinTime1 + 0.35;
		else
			return 0.0;
	}	
	
	public RecordRDLoggerCellFull() {
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

		/* 
		'#'             0  STX
		UNIT ID PREFIX  1  First character (A-Z) for serial number
		UNIT ID MSB     2  high byte of sending station ID
		UNIT ID LSB     3  low byte of sending station ID
		PACKET LENGTH   4  number of byte for packet including STX through CRC (26)
		PACKET TYPE     5  type of packet we are sending (36)
		WS0 MSB         6  high byte of wind speed time
		WS0 LSB         7  low byte of wind speed time
		WG0 MSB         8  high byte of wind gust time
		WG0 LSB         9  low byte of wind gust time
		WC0 MSB         10 high byte of wind pulse count
		WC0 LSB         11 low byte of wind pulse count
		WS1 MSB         12 high byte of wind speed time
		WS1 LSB         13 low byte of wind speed time
		WG1 MSB         14 high byte of wind gust time
		WG1 LSB         15 low byte of wind gust time
		WC1 MSB         16 high byte of wind pulse count
		WC1 LSB         17 low byte of wind pulse count
		AN0 MSB         18 high byte of analog 0 (wind vane)
		AN0 LSB         19 high byte of analog 0 (wind vane)
		AN1 MSB         20 high byte of analog 1
		AN1 LSB         21 high byte of analog 1
		BATT MSB        22 high byte of battery voltage
		BATT LSB        23 high byte of battery voltage
		CRC MSB         24 high byte of CRC on everything after STX and before CRC
		CRC LSB         25 low byte of CRC 
		*/		
		
		
		/* packet length */
		i = buff[4];
		if ( 26 != i ) {
			return;
		}
		
		/* packet type */
		if ( 36 != buff[5] ) {
			return;
		}
		
		/* anemometer 0 */
		tPulseTime0 = (buff[6] << 8 ) + buff[7];
		tPulseMinTime0 = (buff[8] << 8 ) + buff[9];
		windCount0 = (buff[10] << 8 ) + buff[11];
		
		/* anemometer 1 */
		tPulseTime1 = (buff[12] << 8 ) + buff[13];
		tPulseMinTime1 = (buff[14] << 8 ) + buff[15];
		windCount1 = (buff[16] << 8 ) + buff[17];
		
		analog0_adc = (buff[18] << 8) + buff[19];
		analog1_adc = (buff[20] << 8) + buff[21];
		batt_adc = (buff[22] << 8) + buff[23];
		
				

		/* remote CRC */
		i = (buff[24] << 8) + buff[25];
		rCRC=i;
		lCRC=crc_chk(buff,1,23);
	}	
	
	public String toString() {
		return "rdLoggerCellFulll serial=" + serialNumber ;
	}
}
