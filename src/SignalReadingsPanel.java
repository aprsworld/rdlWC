

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

//The main container class for the GUI. This class instantiates SignalSection.java for every RDLogger that is detected.
public class SignalReadingsPanel{
	private JFrame frame;
	protected ArrayList<SignalSection> frameList = new ArrayList<SignalSection>();
	
	
	public SignalReadingsPanel() {
		frame = new JFrame("XBee Signal Strength Monitor");
	
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1300, 400);
		frame.setLayout(new GridLayout(0,1));
		frame.setVisible(true);
		frame.setBackground(Color.WHITE);
		JPanel labelPanel = new JPanel();
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new GridLayout(0,1));
		JLabel apiWarn = new JLabel("XBee Signal Strength Monitor - Reminder: Modem must be in API Mode.");
		apiWarn.setFont(new Font("Serif", Font.BOLD, 18));
		apiWarn.setForeground(Color.blue);
		apiWarn.setHorizontalAlignment(SwingConstants.CENTER);
		messagePanel.add(apiWarn);

		labelPanel.setLayout(new GridLayout(0,7));	

		
		
		JLabel serialLabel = new JLabel("Serial Number");
		serialLabel.setHorizontalAlignment(SwingConstants.CENTER);
		serialLabel.setFont(new Font("Serif", Font.BOLD, 14));

		
		JLabel currentLabel = new JLabel("Current RSSI");
		currentLabel.setHorizontalAlignment(SwingConstants.CENTER);
		currentLabel.setFont(new Font("Serif", Font.BOLD, 14));

		JLabel minLabel = new JLabel("Minimum RSSI");
		minLabel.setHorizontalAlignment(SwingConstants.CENTER);
		minLabel.setFont(new Font("Serif", Font.BOLD, 14));

		JLabel maxLabel = new JLabel("Maximum RSSI");
		maxLabel.setHorizontalAlignment(SwingConstants.CENTER);
		maxLabel.setFont(new Font("Serif", Font.BOLD, 14));

		JLabel avgLabel = new JLabel("Average RSSI");
		avgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		avgLabel.setFont(new Font("Serif", Font.BOLD, 14));

		JLabel pCount = new JLabel("Packet Count");
		pCount.setHorizontalAlignment(SwingConstants.CENTER);
		pCount.setFont(new Font("Serif", Font.BOLD, 14));

		JLabel timeField = new JLabel("Time Since Last Packet");
		timeField.setHorizontalAlignment(SwingConstants.CENTER);
		timeField.setFont(new Font("Serif", Font.BOLD, 14));

		labelPanel.add(serialLabel);
		labelPanel.add(currentLabel);
		labelPanel.add(minLabel);
		labelPanel.add(maxLabel);
		labelPanel.add(avgLabel);
		labelPanel.add(pCount);
		labelPanel.add(timeField);
		
		frame.add(messagePanel);
		frame.add(labelPanel);
	}
	
	//instantiates a signalsection class and gives it values to display
	public void createSection(String serialNumber, Double avg, Integer min, Integer max, Integer current) {
		
		
		SignalSection panel = new SignalSection(serialNumber,avg,min,max,current);
		this.frameList.add(panel);
		//for every 5 stations in our list, we increase the height of the window (maximum 1000 height)

		if(this.frameList.size()%5==0 && frame.getHeight() <= 1000){
			frame.setSize(1200,frame.getHeight()+200);			
		}
		frame.add(panel);
		frame.validate();
	}
	
	//searches the list of objects for the object containing the station ID and then updates its values
	public void updateSection(String serialNumber, Double avg, Integer min, Integer max, Integer current, Integer packetCount) {
		for(SignalSection n : this.frameList){
			if((n.id).equals(serialNumber)){
				n.updateValues(serialNumber, avg, min, max, current, packetCount);
			}
		}
		
	}
	
	
}
