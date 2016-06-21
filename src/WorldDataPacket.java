
public class WorldDataPacket {
	public int[] packet;
	public char serial_prefix;
	public int serial_number;
	public int length;
	public int type;
	public int data[];
	int lCRC, rCRC;
	
	//if api mode
	public int source, rssi, options;

	public void setSource(int src){
		this.source = src;
	}
	public void setRSSI(int rssi){
		this.rssi = rssi;
	}
	public void setOptions(int options){
		this.options = options;		
	}
	public int getOptions(){
		return this.options;
	}
	public int getRSSI(){
		return this.rssi;
	}
	public int getSource(){
		return this.source;
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
	
	public boolean isValid() {
		return lCRC==rCRC;
	}
	
	/* assume valid CRC */
	public WorldDataPacket(int[] rdata) {
		lCRC=-1;
		rCRC=-2;
		
		data = new int[rdata.length];
		packet=rdata;

		serial_prefix = (char) rdata[1];
		serial_number = (rdata[2] << 8) + rdata[3];

		length = rdata[4];
		type = rdata[5];
		
		int i=0;
		int j=0;
		for ( ; i<rdata.length-2 ; i++,j++ ) {
			data[j]=rdata[i];
		}
		
		rCRC=(rdata[length-2]<<8) + rdata[length-1];
		lCRC = crc_chk(rdata, 1, length-3);
		
		if ( lCRC != rCRC )
			System.err.printf("# WorldDataPacket CRC is incorrect (lCRC=0x%04X rCRC=0x%04X)\n",lCRC,rCRC);
	}

	public String getSerialNumber() {
		return Character.toString(serial_prefix) + serial_number;
	}
	
	public String toString() {
		return "# " + getSerialNumber() + " length=" + length + " type=" + type;
	}
}
