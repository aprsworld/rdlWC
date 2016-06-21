
public class RecordDigiAPIRx {
	public int source, rssi, options;
    public String data;
    public int dataArr [];
    public RecordDigiAPIRx() {
    }
   
           
    public void parseRecord(int[] buff) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<buff.length; i++){
    			System.err.println(buff[i]);	
    			
    		}
            System.err.println("__________________________________________");
            int packetLength = (buff[1]<<8) + buff[2];
            dataArr = new int[buff.length-8];
            source=(buff[4]<<8) + buff[5];
            rssi=buff[6];
            options=buff[7];
           
            for ( int i=8 ; i<packetLength+4 ; i++ ) {
                    sb.append( (char) (buff[i]&0xff) );
                    System.err.println(buff[i]&0xff);
                    dataArr[i-8]=buff[i]&0xff;
            }
            data=sb.toString();
    }  
}
