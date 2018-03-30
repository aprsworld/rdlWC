
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class WindSmallDisplay {
	protected AnemometerBigTextPanel anemometer;
	protected JFrame f;
	protected JLabel statusLabel;
	protected javax.swing.Timer timer;
	protected Date recordDate;
	protected String recDate;

	protected int maxAge; // how old before we show as missing data

	protected JLabel titleLabel;
	protected String sUnits;
	Container content;
	
	protected Hashtable<String,AnemometerBigTextPanel> ap;
	
	public void updateStatus() {
		/* calculate the difference between rec.rxDate and the current date and update status bar */
		Date d=new Date();
		long delta;

		delta=(d.getTime()-recordDate.getTime())/1000;
		
		statusLabel.setText("Last record received at " + recordDate + " (" + delta + " seconds ago)");

	}
	
	
	protected void createAnemometerBigTextPanel(RecordRDLoggerCell rec) {
		anemometer=new AnemometerBigTextPanel(rec.serialNumber,sUnits,maxAge);
		content.add(anemometer);
		
		ap.put(rec.serialNumber, anemometer);
	}

	public void updateDisplay(RecordRDLoggerCell rec) {
		/* timer for updating the status bar */
		if ( ! timer.isRunning() ) {
			timer.start();
		}


		/* find our anemometer panel in ap by serial number or create if needed */
		AnemometerBigTextPanel a = ap.get(rec.serialNumber);
		if ( null == a ) {
			/* create the object and setup the GUI */
			createAnemometerBigTextPanel(rec);
			/* now access it */
			a = ap.get(rec.serialNumber);
		}
		
		/* update the anemometer panel with current wind speed and gust */
		a.setWind(rec.getWindSpeed(),rec.getWindGust());

		recordDate=rec.rxDate;
		updateStatus();

		
		f.repaint();
	}

	protected void readIni(IniFile ini) {
		maxAge=Integer.parseInt(ini.getValueSafe("GUI","staleSeconds","25"));
		sUnits=ini.getValueSafe("ANEMOMETER","anemo_u","m/s");
	}
	
	public void setVisible(boolean state) {
		f.setVisible(state);
	}

	public WindSmallDisplay(IniFile ini) {
		WindowUtilities.setNativeLookAndFeel();

		readIni(ini);
		
		ap = new Hashtable<String,AnemometerBigTextPanel>();
		

		f = new JFrame("Current Wind");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    f.setSize(screenSize.width, screenSize.height);
		

		
//		/* fixed size window */
//		int width=450;
//		int height=500;
//		f.setSize(width, height);

		/* Overall BorderLayout */
		Container cont = f.getContentPane();
//		cont.setBackground(Color.white);
		cont.setLayout(new BorderLayout());
		
		/* Our body section */
		content=new Container();
		
		content.setBackground(Color.white);

		content.setLayout(new GridLayout(0,1)); /* one columns wide ... as long as we need */
		
		
		
		/* Add the body */
		cont.add(content, BorderLayout.CENTER);

		/* Add our status bar */
		statusLabel = new JLabel("No data received.",JLabel.CENTER);
		statusLabel.setOpaque(true);
		statusLabel.setBackground(Color.lightGray);
		cont.add(statusLabel, BorderLayout.PAGE_END);

		f.setLocationRelativeTo(null);
		f.addWindowListener(new ExitListener());

		/* add a timer to keep our status bar updated */
		timer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus();
			}
		});

	}
}
