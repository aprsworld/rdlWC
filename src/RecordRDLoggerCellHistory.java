public class RecordRDLoggerCellHistory {
	public int packetNumber;
	public RecordWindHistory rec[];
	public static boolean debug=true;

	public void parseRecord(int[] buff) {
		rec = new RecordWindHistory[4];

		packetNumber = (buff[6]<<8) + buff[7];

		int record[] = new int[12];
		for ( int i=0 ; i<4 ; i++ ) {
			for ( int j=0 ; j<12 ; j++ ) {
				record[j]=buff[i*12+j+8];
			}
			/* have a complete packet, hand it off */
			rec[i]=new RecordWindHistory(record);
		}
	}	

	public String[] getRecordStrings() {
		String e[]=new String[rec.length];
		for ( int i=0 ; i<e.length ; i++ ) {
			e[i]=rec[i].toString();
			if ( debug && e[i] != null )
				e[i] += "," + packetNumber + ", " + i + ", " + (packetNumber*4+i) + ", " + Math.floor(packetNumber / 11.0);
		}
		
		return e;
	}
}
