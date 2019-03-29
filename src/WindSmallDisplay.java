
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
	protected int fontSizeBig, fontSizeLabel;
	Container content;

	protected Hashtable<String,AnemometerBigTextPanel> ap;
	

	protected int turnTable=-1;

	public void setTurnTableDegrees( int degrees ) {
		turnTable=degrees;
	}

	public void updateStatus() {
		/* calculate the difference between rec.rxDate and the current date and update status bar */
		Date d=new Date();
		long delta;

		delta=(d.getTime()-recordDate.getTime())/1000;

		if ( turnTable != -1 ) { 
			statusLabel.setText("Turn Table Commanded to " + turnTable + " degrees. Last record received at " + recordDate + " (" + delta + " seconds ago)");	
		} else {
			statusLabel.setText("Last record received at " + recordDate + " (" + delta + " seconds ago)");
		}
	}


	protected void createAnemometerBigTextPanel(String serialNumber) {
		anemometer=new AnemometerBigTextPanel(serialNumber,sUnits,maxAge,fontSizeBig,fontSizeLabel);
		content.add(anemometer);

		ap.put(serialNumber, anemometer);
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
			createAnemometerBigTextPanel(rec.serialNumber);
			/* now access it */
			a = ap.get(rec.serialNumber);
		}

		/* update the anemometer panel with current wind speed and gust */
		a.setWind(rec.getWindSpeed(),rec.getWindGust(),-1,null);

		recordDate=rec.rxDate;
		updateStatus();


		f.repaint();
	}

	protected void readIni(IniFile ini) {
		maxAge=Integer.parseInt(ini.getValueSafe("GUI","staleSeconds","25"));
		fontSizeBig=Integer.parseInt(ini.getValueSafe("GUI","fontSizeBig","48"));
		fontSizeLabel=Integer.parseInt(ini.getValueSafe("GUI","fontSizeLabel","24"));
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

		if ( null != ini.getValue("LIVELOG","filenameMiddle") ) {
			f.setTitle("LiveLog with " +  ini.getValue("LIVELOG","filenameMiddle"));
		}
		
		

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
		statusLabel.setFont(new Font("Serif", Font.BOLD, 18));
		cont.add(statusLabel, BorderLayout.PAGE_END);

		f.setLocationRelativeTo(null);
		f.addWindowListener(new ExitListener());

		/* add a timer to keep our status bar updated */
		timer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus();
			}
		});

		
		/* load list of serial numbers from INI file and create anemometer panels */
		String serialList[] = ini.getValueSafe("GUI","serialOrder","").split(",[ ]*");
		for ( int i=0 ; i< serialList.length ; i++ ) {
			serialList[i]=serialList[i].toUpperCase();
			
			if ( null != ap.get(serialList[i]) ) {
				System.err.println("# Panel alreadying exists for '" + serialList[i] + "'. Skipping");
			} else {
				System.err.println("# Creating panel for '" + serialList[i] + "'");
			
				createAnemometerBigTextPanel(serialList[i]);
			}
		}
		
		
	}


	public void updateDisplayFull(RecordRDLoggerCellFull rec) {
		/* timer for updating the status bar */
		if ( ! timer.isRunning() ) {
			timer.start();
		}


		/* find our anemometer panel in ap by serial number or create if needed */
		AnemometerBigTextPanel a = ap.get(rec.serialNumber);
		if ( null == a ) {
			/* create the object and setup the GUI */
			createAnemometerBigTextPanel(rec.serialNumber);
			/* now access it */
			a = ap.get(rec.serialNumber);
		}

		/* update the anemometer panel with current wind speed and gust */
		a.setWind(rec.getWindSpeed0(),rec.getWindGust0(),rec.getWindDirectionFromAnalog0(),null);

		recordDate=rec.rxDate;
		updateStatus();


		f.repaint();


	}

	public void updateDisplayCMPS12(RecordRDLoggerCellCMPS12 rec) {
		/* timer for updating the status bar */
		if ( ! timer.isRunning() ) {
			timer.start();
		}

		System.err.println("updateDisplayCMPS12");

		/* find our anemometer panel in ap by serial number or create if needed */
		AnemometerBigTextPanel a = ap.get(rec.serialNumber);
		if ( null == a ) {
			/* create the object and setup the GUI */
			createAnemometerBigTextPanel(rec.serialNumber);
			/* now access it */
			a = ap.get(rec.serialNumber);
		}

		/* update the anemometer panel with current wind speed and gust */
		a.setWind(rec.getWindSpeed0(),rec.getWindGust0(),rec.getBearingBosch(),rec);

		recordDate=rec.rxDate;
		updateStatus();


		f.repaint();


	}
}
