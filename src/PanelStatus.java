import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.*;


public class PanelStatus extends JPanel  {
	private static final long serialVersionUID = 1L;
	
	protected JLabel lSerialNumber, lDatetime,  
		lBattvolts,lVersion,lPercentFull,lSDStatus,lInternalMemoryStatus,  
		lLatLon, lUptime, lGPRSUptime, lGPRSState;
	//lWs,lWg,lWc, lWindDirection
	public JButton statusButton;
	
	
	public void updateNow(RecordRDLoggerCellStatus r) {
		double d;
		
//		System.err.println("# pStatus updateNow() called");
		
		lSerialNumber.setText(r.serialNumber);
		
		DecimalFormat df = new DecimalFormat("0.0");
		DecimalFormat df4 = new DecimalFormat("0.0000");
/*
		lWs.setText(df.format(r.tPulseTime));
		lWg.setText(df.format(r.tPulseMinTime));
		lWc.setText(r.pulseCount + "");
*/		
		
		d=(40.0/1024)*r.adcInputVoltage;
		lBattvolts.setText(df.format(d));
		d=(r.dataflash_page/4096.0)*100;
		lPercentFull.setText(df.format(d) + "% (" + r.dataflash_page + " / 4096)");
		
		if ( 0==r.sd_status )
			lSDStatus.setText("Yes");
		else
			lSDStatus.setText("No!");
		
		if ( 172 == r.dataflash_read_status ) 
			lInternalMemoryStatus.setText("Yes");
		else
			lInternalMemoryStatus.setText("No!");
		
		lLatLon.setText(df4.format(r.latitude) + " / " + df4.format(r.longitude));
		
		lUptime.setText(r.uptimeMinutes + " minutes");
		lGPRSState.setText("" + r.gprsState);
		lGPRSUptime.setText(r.gprsUptimeMinutes + " minutes");

		r.compileMonth = r.compileMonth & 0x0f;
		lVersion.setText(String.format("%04d-%02d-%02d",r.compileYear,r.compileMonth,r.compileDay));
		r.year += 2000;
		lDatetime.setText(String.format("%04d-%02d-%02d %02d:%02d:%02d UTC",r.year,r.month,r.day,r.hour,r.minute,r.second));
	}
	
	public PanelStatus() {
		//super(new GridLayout(0, 2));
		super(new BorderLayout());
		
		/* overall border layout container that has another container north and a button south */
		
		
		
		setBackground(Color.white);
		setBorder(BorderFactory.createTitledBorder("Data Logger Current Status"));
		
		Container content = new Container();
		content.setBackground(Color.white);
		content.setLayout(new GridLayout(0,2));
		
		content.add(new JLabel("Serial Number:"));
		lSerialNumber = new JLabel("---");
		content.add(lSerialNumber);
		
		content.add(new JLabel("Date and time:"));
		lDatetime = new JLabel("---");
		content.add(lDatetime);
/*		
		content.add(new JLabel("Wind Speed (m/s): "));
		lWs = new JLabel("---");
		content.add(lWs);
		
		content.add(new JLabel("Wind Gust (m/s):"));
		lWg = new JLabel("---");
		content.add(lWg);
		
		content.add(new JLabel("Wind Pulse Count:"));
		lWc = new JLabel("---");
		content.add(lWc);
*/		
		content.add(new JLabel("Battery Voltage (volts):"));
		lBattvolts = new JLabel("---");
		content.add(lBattvolts);
	
		content.add(new JLabel("Firmware version:"));
		lVersion = new JLabel("---");
		content.add(lVersion);
		
		content.add(new JLabel("Memory Percent Full:"));
		lPercentFull = new JLabel("---");
		content.add(lPercentFull);

		content.add(new JLabel("Internal Memory Logging:"));
		lInternalMemoryStatus = new JLabel("---");
		content.add(lInternalMemoryStatus);

		content.add(new JLabel("SD Card Logging:"));
		lSDStatus = new JLabel("---");
		content.add(lSDStatus);
		
		content.add(new JLabel("Latitude / Longitude:"));
		lLatLon = new JLabel("---");
		content.add(lLatLon);
		
		content.add(new JLabel("System Uptime:"));
		lUptime = new JLabel("---");
		content.add(lUptime);
		
		content.add(new JLabel("GPRS State:"));
		lGPRSState = new JLabel("---");
		content.add(lGPRSState);
		
		content.add(new JLabel("GPRS Uptime:"));
		lGPRSUptime = new JLabel("---");
		content.add(lGPRSUptime);
		
		/* north */
		add(content,BorderLayout.PAGE_START);
		
		/* south */
		statusButton = new JButton("Query Status Now");
		add(statusButton,BorderLayout.PAGE_END);
	}
}
