import java.util.Iterator;
import java.util.Vector;


public class SignalData {
	
	//data
	private Vector<Integer> signalStrength = new Vector <Integer>(50);
	private double currentStrength,
	minStrength = 30000, //high number
	maxStrength = 0.0, 
	avgStrength = 0.0;
	private int packetCount;
	private String serialNumber;
	
	
	
	//constructor
	public SignalData(){
				
	}

	public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}
	
	//methods
	
	//finds average
	public void updateAvg(){
		Iterator itr = signalStrength.iterator();
		Integer nextItem, sum = 0;
		int size = signalStrength.size();
		while(itr.hasNext()){
			nextItem = (Integer) itr.next();
			sum += nextItem;
		}
		
		
		this.avgStrength= sum/size;		
	}
	public double getAvg() {
		return  avgStrength;
	}
	
	public void addToSignal(int rssi){
		signalStrength.add(rssi);		
		setPacketCount(getPacketCount() + 1);
	}
	
	public void updateMinMax(Integer signalValue){
		if(signalValue > this.maxStrength){
			this.maxStrength = signalValue;	
		}
		if(signalValue < this.minStrength){
			this.minStrength = signalValue;
		}
	}

	//Gets current signal Strength
	public double getCurrentStrength() {
		return currentStrength;
	}

	//sets current signal strength
	public void setCurrentStrength(Integer currentStrength) {
		this.currentStrength = currentStrength;
	}

	//gets lowest signal Strength
	public double getMinStrength() {
		return this.minStrength;
	}
	public double getMaxStrength() {
		return this.maxStrength;
	}
	//sets lowest signal strength
	public void setMinStrength(double minStrength) {
		this.minStrength = minStrength;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
}
