
import java.util.Date;

/*		
'#'                 0  STX
UNIT ID PREFIX      1  First character (A-Z) for serial number
UNIT ID MSB         2  sending station ID MSB
UNIT ID LSB         3  sending station ID LSB
PACKET LENGTH       4  number of byte for packet including STX through CRC (34 or 41)
PACKET TYPE         5  type of packet we are sending (0x08)

tPulseTime MSB      6  COUNTER pulse time
tPulseTime LSB      7  COUNTER pulse time
tPulseMinTime MSB   8  COUNTER pulse minimum time
tPulseMinTime LSB   9  COUNTER pulse minimum time
pulseCount MSB      10 COUNTER pulse count
pulseCount LSB      11 COUNTER pulse count

year                12 RTC year
month               13 RTC month
day                 14 RTC day
hour                15 RTC hour
minute              16 RTC minute
second              17 RTC second

dataflashReadStatus 18 DATAFLASH status (172 is normal)
dataflashPage MSB   19 DATAFLASH last page being used
dataflashPage LSB   20 DATAFLASH last page being used

adcInputVoltage MSB 21 ADC input voltage (10 bits spanning 0 to 40 volts)
adcInputVoltage LSB 22 ADC input voltage (10 bits spanning 0 to 40 volts)
sdStatus            23 SD status

latitude AEXP       24 GPS latitude 8-bit biased exponent
latitude AARGB0     25 GPS latitude MSB of mantissa
latitude AARGB1     26 GPS latitude mantissa middle byte
latitude AARGB2     27 GPS latitude LSB of mantissa 
longitude AEXP      28 GPS longitude 8-bit biased exponent
longitude AARGB0    29 GPS longitude MSB of mantissa
longitude AARGB1    30 GPS longitude mantissa middle byte
longitude AARGB2    31 GPS longitude LSB of mantissa 

CRC MSB             32 high byte of CRC on everything after STX and before CRC
CRC LSB             33 low byte of CRC

OR FOR THE 41 BYTE PACKET

windDirectionSector 32 Wind direction sector (referenced to 0 being boom ahead)
gprsState           33 GPRS connection state
gprsUptime MSB      34 GPRS uptime minutes
gprsUptime LSB      35 
compileYear         36 Firmware compiled year (0 to 255 corresponding to 2000 to 2255)
compileMonth        37 Firmware compiled month (1 to 12, 13 for parse error)
compileDay          38 Firmware compiled day (1 to 31)

CRC MSB             39 high byte of CRC on everything after STX and before CRC
CRC LSB             40 low byte of CRC


 */


public class RecordRDLoggerCellStatus {
	public String serialNumber;

	public int lCRC, rCRC;
	public Date rxDate;

	public int tPulseTime, tPulseMinTime, pulseCount;
	public int year, month, day, hour, minute, second;
	public int dataflash_read_status, dataflash_page;
	public int adcInputVoltage;
	public int sd_status;
	public double latitude, longitude;
	public int windDirectionSector;
	public int gprsState, gprsUptimeMinutes;
	public int compileYear, compileMonth, compileDay;
	public int uptimeMinutes;

	public RecordRDLoggerCellStatus() {
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
	
	double dm2dd(double dm) {
		double dd;

		if ( dm > 0 ) {
			dd= Math.floor(dm) + ( (dm - Math.floor(dm)) / 0.6);
		} else {
			dd= Math.ceil(dm) + ( (dm - Math.ceil(dm)) / 0.6);
		}

		return dd;
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

		/* packet type */
		if ( 0x08 != buff[5] ) {
			return;
		}

		/* counter */
		tPulseTime = (buff[6] << 8 ) + buff[7];
		tPulseMinTime = (buff[8] << 8 ) + buff[9];
		pulseCount = (buff[10] << 8 ) + buff[11];

		/* RTC */
		year = buff[12];
		month = buff[13];
		day = buff[14];
		hour = buff[15];
		minute = buff[16];
		second = buff[17];

		/* dataflash */
		dataflash_read_status = buff[18];
		dataflash_page = (buff[19] << 8) + buff[20];

		//System.err.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ buff[19]=" + buff[19] + " buff[20]=" + buff[20] + " dataflash_page=" + dataflash_page);
		
		/* ADC */
		adcInputVoltage = (buff[21] << 8 ) + buff[22];

		/* SD card */
		sd_status = buff[23];

		/* GPS location */
		latitude = MicrochipFloat32.microchipFloat32ToDouble(buff[24],buff[25],buff[26],buff[27]);
		latitude = dm2dd(latitude/100.0);
		longitude = MicrochipFloat32.microchipFloat32ToDouble(buff[28],buff[29],buff[30],buff[31]);
		longitude = dm2dd(longitude/100.0);

		/* check packet length to determine which type of packet we have */
		if ( 34 == buff[4] ) {
			/* remote CRC */
			i = (buff[32] << 8) + buff[33];
			rCRC=i;
			lCRC=crc_chk(buff,1,31);
		} else if ( 43 == buff[4] ) {
			windDirectionSector=buff[32];
			gprsState=buff[33];
			gprsUptimeMinutes=(buff[34]<<8) + buff[35];
			compileYear=buff[36]+2000;
			compileMonth=buff[37];
			compileDay=buff[38];
			uptimeMinutes=(buff[39]<<8) + buff[40];
			
			i = (buff[41] << 8) + buff[42];
			rCRC=i;
			lCRC=crc_chk(buff,1,40);			
		}
	}	
	
	public String toString() {
		return "rdLoggerCellStatus serial=" + serialNumber + " lat=" + latitude + " lon=" + longitude + 
			" compiled=" + compileYear + "-" + compileMonth + "-" + compileDay;
	}

}
