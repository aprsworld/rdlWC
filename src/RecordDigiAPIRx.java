
public class RecordDigiAPIRx {
	public int source, rssi, options;
    public String data;
   
    public RecordDigiAPIRx() {
    }
   
           
    public void parseRecord(int[] buff) {
            StringBuilder sb = new StringBuilder();

            int packetLength = (buff[1]<<8) + buff[2];
           
            source=(buff[4]<<8) + buff[5];
            rssi=buff[6];
            options=buff[7];
           
            for ( int i=8 ; i<packetLength+3 ; i++ ) {
                    sb.append( (char) (buff[i]&0xff) );
            }
            data=sb.toString();
    }  
}
