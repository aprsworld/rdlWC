import java.util.Date;

public class RecordRDLoggerCellCMPS12 {
	public String serialNumber;
	
	public int lCRC, rCRC;
	public Date rxDate;
	
	public int tPulseTime0, tPulseMinTime0, windCount0;
	public int tPulseTime1, tPulseMinTime1, windCount1;
	public int analog0_adc, analog1_adc;
	public int batt_adc;
	
	/* one byte registers ... some will need to be combined to make 16 bit values */
	public int cmps12_register[];
	
	/* Command register (write) / Software version (read) */
	public static final int CMPS12_REG_COMMAND_VERSION =   0x00;

	/* Compass Bearing 8 bit, i.e. 0-255 for a full circle */
	public static final int CMPS12_REG_BEARING =           0x01;

	/* Compass Bearing 16 bit, i.e. 0-3599, representing 0-359.9 degrees. register 2 being the 
	high byte. This is calculated by the processor from quaternion outputs of the BNO055 */
	public static final int CMPS12_REG_BEARING_MSB =       0x02;
	public static final int CMPS12_REG_BEARING_LSB =       0x03;

	/* Pitch angle - signed byte giving angle in degrees from the horizontal plane (+/- 90°) */
	public static final int CMPS12_REG_PITCH =             0x04;

	/* Roll angle - signed byte giving angle in degrees from the horizontal plane (+/- 90°) */
	public static final int CMPS12_REG_ROLL =              0x05;

	/* Magnetometer X axis raw output, 16 bit signed integer (register 0x06 high byte) */
	public static final int CMPS12_REG_MAGNETOMETER_X_MSB =0x06;
	public static final int CMPS12_REG_MAGNETOMETER_X_LSB =0x07;

	/* Magnetometer Y axis raw output, 16 bit signed integer (register 0x08 high byte) */
	public static final int CMPS12_REG_MAGNETOMETER_Y_MSB =0x08;
	public static final int CMPS12_REG_MAGNETOMETER_Y_LSB =0x09;

	/* Magnetometer Z axis raw output, 16 bit signed integer (register 0x0A high byte) */
	public static final int CMPS12_REG_MAGNETOMETER_Z_MSB =0x0A;
	public static final int CMPS12_REG_MAGNETOMETER_Z_LSB =0x0B;

	/* Accelerometer X axis raw output, 16 bit signed integer (register 0x0C high byte) */
	public static final int CMPS12_REG_ACCELEROMETER_X_MSB=0x0C;
	public static final int CMPS12_REG_ACCELEROMETER_X_LSB=0x0D;

	/* Accelerometer Y axis raw output, 16 bit signed integer (register 0x0E high byte) */
	public static final int CMPS12_REG_ACCELEROMETER_Y_MSB=0x0E;
	public static final int CMPS12_REG_ACCELEROMETER_Y_LSB=0x0F;

	/* Accelerometer Z axis raw output, 16 bit signed integer (register 0x10 high byte) */
	public static final int CMPS12_REG_ACCELEROMETER_Z_MSB=0x10;
	public static final int CMPS12_REG_ACCELEROMETER_Z_LSB=0x11;

	/* Gyro X axis raw output, 16 bit signed integer (register 0x12 high byte) */
	public static final int CMPS12_REG_GYRO_X_MSB =        0x12;
	public static final int CMPS12_REG_GYRO_X_LSB =        0x13;

	/* Gyro Y axis raw output, 16 bit signed integer (register 0x14 high byte) */
	public static final int CMPS12_REG_GYRO_Y_MSB =        0x14;
	public static final int CMPS12_REG_GYRO_Y_LSB =        0x15;

	/* Gyro Z axis raw output, 16 bit signed integer (register 0x16 high byte) */
	public static final int CMPS12_REG_GYRO_Z_MSB =        0x16;
	public static final int CMPS12_REG_GYRO_Z_LSB =        0x17;

	/* Temperature of the BNO055 in degrees centigrade */
	/* BUG? Seems to be a signed 8 bit number in LSB register */
	public static final int CMPS12_REG_TEMPERATURE_MSB =   0x18;
	public static final int CMPS12_REG_TEMPERATURE_LSB =   0x19;

	/* Compass Bearing 16 bit This is the angle Bosch generate in the BNO055 (0-5759), 
	divide by 16 for degrees */
	public static final int CMPS12_REG_BNO055_COMPASS_MSB =0x1A;
	public static final int CMPS12_REG_BNO055_COMPASS_LSB =0x1B;

	/* Pitch angle 16 bit - signed byte giving angle in degrees from the horizontal plane (+/- 
	180°) */
	public static final int CMPS12_REG_PITCH_ANGLE_MSB =   0x1C;
	public static final int CMPS12_REG_PITCH_ANGLE_LSB =   0x1D;

	/* Calibration state, bits 0 and 1 reflect the calibration status (0 un-calibrated, 3 fully 
	calibrated) */
	public static final int CMPS12_REG_CALIBRATION_STATE = 0x1E;
	
	public String cmps12Debug() {
		return String.format("hello");
	}
	
	
	
	public String getCalibrationHTML() {
		return String.format("<html>CMPS: %02Xh<br />SY: %d<br />GY: %d<br />AC: %d<br />MA: %d</html>",
				getCalibrationCMPS12(),
				getCalibrationCMPS12Sy(),
				getCalibrationCMPS12Gy(),
				getCalibrationCMPS12Ac(),
				getCalibrationCMPS12Ma()
				);
	}
	
	public int getCalibrationCMPS12() {
		return cmps12_register[CMPS12_REG_CALIBRATION_STATE];
	}
	
	/*
	printf(lcd_putch,"%02Xh  %u  %u  %u  %u",
		cal,
		(cal>>6) & 0b11,  system 
		(cal>>4) & 0b11,  gyro 
		(cal>>2) & 0b11,  accel 
		cal & 0b11        magnetometer 
	);
	 */
	public int getCalibrationCMPS12Sy() {
		return ( ( getCalibrationCMPS12() >> 6 ) & 0x3 );
	}
	public int getCalibrationCMPS12Gy() {
		return ( ( getCalibrationCMPS12() >> 4 ) & 0x3 );
	}
	public int getCalibrationCMPS12Ac() {
		return ( ( getCalibrationCMPS12() >> 2 ) & 0x3 );
	}
	public int getCalibrationCMPS12Ma() {
		return (  getCalibrationCMPS12()  & 0x3 );
	}
	
	public int getWindDirectionFromAnalog0() {
		return analog0_adc;
	}
	
	public double getBearingBosch() {
		int i = (cmps12_register[CMPS12_REG_BNO055_COMPASS_MSB] << 8) + cmps12_register[CMPS12_REG_BNO055_COMPASS_LSB];
		return i / 16.0;
	}
	
	public double getBearingCMPS12() {
		int i = (cmps12_register[CMPS12_REG_BEARING_MSB] << 8) + cmps12_register[CMPS12_REG_BEARING_LSB];
		return i / 10.0;
	}
	
	
	public int getPitch8() {
//		int pitch=cmps12_register[CMPS12_REG_PITCH_ANGLE_MSB]<<8 + cmps12_register[CMPS12_REG_PITCH_ANGLE_LSB]; 
	
		int pitch=cmps12_register[CMPS12_REG_PITCH];
		
		if ( pitch > 128 ) {
			pitch -= 256;
		}
		
		return pitch;
	}
	
	public int getPitch() {
		int pitch=(cmps12_register[CMPS12_REG_PITCH_ANGLE_MSB]<<8) + cmps12_register[CMPS12_REG_PITCH_ANGLE_LSB]; 
		
		if ( pitch > 32768 ) {
			pitch -= 65536;
		}
		
		return pitch;
	}
	
	public int getRoll() {
		int roll=cmps12_register[CMPS12_REG_ROLL];
		
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
	
	public RecordRDLoggerCellCMPS12() {
		lCRC=-1;
		rCRC=-2;
		cmps12_register=new int[31];
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
		CMPS12 REGISTER 24 ... 54
		CRC MSB         55 high byte of CRC on everything after STX and before CRC
		CRC LSB         56 low byte of CRC 
		*/		
		
		
		/* packet length */
		i = buff[4];
		if ( 57 != i ) {
			return;
		}
		
		/* packet type */
		if ( 37 != buff[5] ) {
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
		
		/* CMPS12 raw 8 bit registers */
		for ( i=0 ; i<cmps12_register.length ; i++ ) {
			cmps12_register[i]= buff[24+i];
//			System.out.printf("cmps12_register[0x%02X]=0x%02X\n", i, cmps12_register[i]);
		}
				

		/* remote CRC */
		i = (buff[54] << 8) + buff[55];
		rCRC=i;
		lCRC=crc_chk(buff,1,53);
	}	
	
	public String toString() {
		return "rdLoggerCellCMPS12 serial=" + serialNumber ;
	}
}
