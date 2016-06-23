import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;

/* This class is for storing and manipulating data for the signal monitor

*/
public class SignalData {
	
	//data
	private Vector<Integer> signalStrength = new Vector <Integer>(50);
	private Integer currentStrength,
	maxStrength = -30000, //low number
	minStrength = 30000; 
	private Double avgStrength = 0.0;
	private int packetCount;
	private String serialNumber;
	DecimalFormat dformat = new DecimalFormat("#.###");
	
	
	
	//constructor
	public SignalData(){
		dformat.setRoundingMode(RoundingMode.CEILING);
	}

	public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}
	
	public void incPacketCount() {
		this.packetCount++;
	}
	//methods
	
	//finds average
	
	public void updateAvg(){
		Iterator itr = signalStrength.iterator();
		Integer nextItem, sum = 0;
		int size = signalStrength.size();
		//if there has been no deviation, we dont need to go through the while loop
		if(this.minStrength == this.maxStrength){
			
			this.avgStrength = Double.valueOf(dformat.format(this.minStrength));
		}
		else{
			while(itr.hasNext()){
				nextItem = (Integer) itr.next();
				sum += nextItem;
			}
			this.avgStrength= Double.valueOf(dformat.format((-1*((double)sum/(double)size))));		

		}
		
	}
	public double getAvg() {
		return  avgStrength;
	}
	
	public void addToSignal(int rssi){
		this.signalStrength.add(rssi);		
	}
	
	public void updateMinMax(Integer signalValue){
		if(-1*signalValue < this.minStrength){
			this.minStrength = -1*signalValue;	
		}
		if(-1*signalValue > this.maxStrength){
			this.maxStrength = -1*signalValue;
		}
	}

	//Gets current signal Strength
	public Integer getCurrentStrength() {
		return currentStrength;
	}

	//sets current signal strength
	public void setCurrentStrength(Integer currentStrength) {
		this.currentStrength = -1*currentStrength;
	}

	//gets lowest signal Strength
	public Integer getMinStrength() {
		return this.minStrength;
	}
	public Integer getMaxStrength() {
		return this.maxStrength;
	}
	//sets lowest signal strength
	public void setMinStrength(Integer minStrength) {
		this.minStrength = minStrength;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
}
