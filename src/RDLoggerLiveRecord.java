
import java.util.Date;


public class RDLoggerLiveRecord {
	public int serialNumber;
	public double windSpeed;
	public double windGust;
	public int lCRC,rCRC;
	public Date rxDate;
	
	public RDLoggerLiveRecord() {
		lCRC=-1;
		rCRC=0;
		rxDate=new Date();
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
	 
	
	/* 
	data format:
	'#'             0 STX
	UNIT ID MSB     1 high byte of sending station ID
	UNIT ID LSB     2 low byte of sending station ID
	WS MSB          3 high byte of wind speed raw
	WS LSB          4 low byte of wind speed raw
	WG MSB          5 high byte of wind gust raw
	WG LSB          6 low byte of wind gust raw
	CRC MSB         7 high byte of CRC on everything after STX and before CRC
	CRC LSB         8 low byte of CRC
	*/
	
	public void parseRecord(int[] buff) {
//		StringBuilder sb = new StringBuilder();
		Integer i;
		
		/* Serial number */
		i = (new Integer(buff[1]) << 8) + new Integer(buff[2]);
		serialNumber=i;
		//System.err.println("Serial Number=" + i);
		
		/* wind speed */
		i = (new Integer(buff[3]) << 8 ) + new Integer(buff[4]);
//		System.err.println("Wind speed (raw)=" + i);
		if ( i>0 && i<65535 ) {
			windSpeed = 7650.0 / i + 0.35;
		} else {
			windSpeed=0.0;
		}
		
		/* wind gust */
		i = (new Integer(buff[5]) << 8 ) + new Integer(buff[6]);
//		System.err.println("Wind gust (raw)=" + i);
		if ( i>0 && i<65535 ) {
			windGust = 7650.0 / i + 0.35;
		} else {
			windGust=0.0;
		}
		
		/* remote CRC */
		i = ((new Integer(buff[7]) << 8 )&0xff) + (new Integer(buff[8]))&0xff;
		rCRC=i;
		lCRC=crc_chk(buff,1,6);
		System.err.println("Remote CRC (raw)=" + rCRC);
		System.err.println("Local  CRC (raw)=" + lCRC);
		
//		System.err.println("WindSpeed=" + windSpeed + "m/s WindGust=" + windGust + "m/s");
	}	
}
