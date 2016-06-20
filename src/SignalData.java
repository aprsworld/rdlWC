import java.util.Iterator;
import java.util.Vector;


public class SignalData {
	
	//data
	private Vector<Integer> signalStrength;
	private double currentStrength, minStrength, 
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
		Double nextItem, sum = 0.0;
		int size = signalStrength.size();
		while(itr.hasNext()){
			nextItem = (Double) itr.next();
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
	
	public void updateMinMax(Double signalValue){
		if(signalValue > maxStrength){
			maxStrength = signalValue;	
		}
		if(signalValue < minStrength){
			minStrength = signalValue;
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
		return minStrength;
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
