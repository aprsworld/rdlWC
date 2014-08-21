import java.util.Date;

public class RecordWindHistory {
	public Date date;
	public int year, month, day, hour, minute;
	public int tPulseTime;
	public int tPulseMinTime;
	public int pulseCount;
	public int batteryChargePercent;
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

	public double getWindAverage () {
		if ( pulseCount > 0 ) {
			return 0.765*((double) pulseCount / (double) 60.0) + 0.35;
		}
		return 0.0;
	}

	@SuppressWarnings("deprecation")
	public RecordWindHistory(int buff[]) {
		year = 2000 + buff[0];
		month = buff[1];
		day = buff[2];
		hour = buff[3];
		minute = buff[4];

		date = new Date(year,month,day,hour,minute,0);
		tPulseTime = (buff[5]<<8) + buff[6];
		tPulseMinTime = (buff[7]<<8) + buff[8];
		pulseCount = (buff[9]<<8) + buff[10];
		batteryChargePercent = ((buff[11]>>4) & 0x0f) * 10;
		windDirectionSector = buff[11] & 0x0f;
	}

	public String toString() {
		/* this can occur in FRAM block if the block isn't completely full */
		if ( 2255 == year )
			return null;

		return String.format("%04d-%02d-%02d %02d:%02d, %2.1f, %2.1f, %2.1f, %2.1f, %2.1f, %2.1f, %d, %d, %d", 
				year, 
				month, 
				day,
				hour,
				minute,
				getWindSpeed()*2.23693629,
				getWindGust()*2.23693629,
				getWindAverage()*2.23693629,
				getWindSpeed(),
				getWindGust(),
				getWindAverage(),
				pulseCount,
				windDirectionSector,
				batteryChargePercent
		);


		//		return "@" + year + "-" + month + "-" + day + " " + hour + ":" + minute + " ws=" + getWindSpeed() + " wg=" + getWindGust() + " count=" + pulseCount +  
		//		" battery=" + batteryChargePercent + " wd=" + windDirectionSector;
	}


}
