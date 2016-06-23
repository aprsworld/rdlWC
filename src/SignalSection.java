import javax.swing.*;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


//this is the class that represents the RDLogger signal data on the GUI
public class SignalSection extends JPanel{

	private static final long serialVersionUID = 1L;
	private JLabel serialNumValue, currentNumValue, minNumValue, maxNumValue, avgNumValue, packetCount, timerField;
	protected String id;
	protected Integer timeCount = 0;
	protected Timer timer;
	
	public SignalSection(String serialNumber, Double avg, Integer min, Integer max, Integer current){
		this.setLayout(new GridLayout(0,7));
		
		ActionListener al=new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				timeCount++;
				timerField.setText(String.valueOf(timeCount)+" Seconds");
			}
		};
		
		this.timer = new Timer(1000, al);
		this.timer.setRepeats(true);
		this.timer.start();
		
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBackground(Color.white);
	
		this.serialNumValue = new JLabel(serialNumber);
		this.serialNumValue.setBounds(100,20,165,25);
		this.serialNumValue.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.currentNumValue = new JLabel(String.valueOf(current)+" dBm");
		this.currentNumValue.setBounds(10,50,80,25);		
		this.currentNumValue.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.minNumValue = new JLabel(String.valueOf(min)+" dBm");
		this.minNumValue.setBounds(10,50,80,25);
		this.minNumValue.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.maxNumValue = new JLabel(String.valueOf(max)+" dBm");
		this.maxNumValue.setBounds(10,50,80,25);
		this.maxNumValue.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.avgNumValue = new JLabel(String.valueOf(avg)+" dBm");
		this.avgNumValue.setBounds(10,50,80,25);
		this.avgNumValue.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.packetCount = new JLabel("1");
		this.packetCount.setBounds(10,50,80,25);
		this.packetCount.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.timerField = new JLabel("1");
		this.timerField.setBounds(10,50,80,25);
		this.timerField.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.add(serialNumValue);
		
		this.add(currentNumValue);
		
		this.add(minNumValue);
		
		this.add(maxNumValue);

		this.add(avgNumValue);
		
		this.add(packetCount);
		
		this.add(timerField);
		
		this.id = serialNumber;
	}
	
	
	public void updateValues(String serialNumber, Double avg, Integer min, Integer max, Integer current, Integer pCount) {
		this.currentNumValue.setText(String.valueOf(current)+" dBm");
		this.minNumValue.setText(String.valueOf(min)+" dBm");
		this.maxNumValue.setText(String.valueOf(max)+" dBm");
		this.avgNumValue.setText(String.valueOf(avg)+" dBm");
		this.packetCount.setText(String.valueOf(pCount));
		this.timeCount = 0;
		this.timerField.setText("0 Seconds");
		this.timer.restart();
	}
}
