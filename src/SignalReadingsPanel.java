

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;

public class SignalReadingsPanel{
	private JFrame frame;
	protected ArrayList<SignalSection> frameList = new ArrayList<SignalSection>();
	
	
	public SignalReadingsPanel() {
		frame = new JFrame("XBee Signal Strength Monitor");
	
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1000, 100);
		frame.setLayout(new GridLayout(0,1));
		frame.setVisible(true);
		frame.setBackground(Color.WHITE);
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(0,7));	

		
		
		JLabel serialLabel = new JLabel("Serial Number");
		serialLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		JLabel currentLabel = new JLabel("Current RSSI");
		currentLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		JLabel minLabel = new JLabel("Minimum RSSI");
		minLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		JLabel maxLabel = new JLabel("Maximum RSSI");
		maxLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		JLabel avgLabel = new JLabel("Average RSSI");
		avgLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		JLabel pCount = new JLabel("Packet Count");
		pCount.setHorizontalTextPosition(SwingConstants.CENTER);
		
		JLabel timeField = new JLabel("Time Since Last Packet");
		timeField.setHorizontalTextPosition(SwingConstants.CENTER);
		
		labelPanel.add(serialLabel);
		labelPanel.add(currentLabel);
		labelPanel.add(minLabel);
		labelPanel.add(maxLabel);
		labelPanel.add(avgLabel);
		labelPanel.add(pCount);
		labelPanel.add(timeField);

		
		frame.add(labelPanel);
	}
	
	
	public void createSection(String serialNumber, Double avg, Integer min, Integer max, Integer current) {
		
		
		SignalSection panel = new SignalSection(serialNumber,avg,min,max,current);
		this.frameList.add(panel);
		frame.add(panel);
		frame.validate();
	}
	
	public void updateSection(String serialNumber, Double avg, Integer min, Integer max, Integer current, Integer packetCount) {
		for(SignalSection n : this.frameList){
			if((n.id).equals(serialNumber)){
				n.updateValues(serialNumber, avg, min, max, current, packetCount);
			}
		}
		
	}
	
	
}
