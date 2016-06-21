import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;


public class SignalData {
	
	//data
	private Vector<Integer> signalStrength = new Vector <Integer>(50);
	private Integer currentStrength,
	minStrength = 30000, //high number
	maxStrength = 0; 
	private Double avgStrength = 0.0;
	private int packetCount;
	private String serialNumber;
	DecimalFormat dformat = new DecimalFormat("#.##");
	
	
	
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
		while(itr.hasNext()){
			nextItem = (Integer) itr.next();
			sum += nextItem;
		}
		
		
		this.avgStrength= Double.valueOf(dformat.format((double)sum/size));		
	}
	public double getAvg() {
		return  avgStrength;
	}
	
	public void addToSignal(int rssi){
		signalStrength.add(rssi);		
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
