
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.*;

public class AnemometerBigTextPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JLabel lSerialNumber;
	protected JLabel lWindSpeed;
	protected JLabel lWindGust;
	protected JLabel lWindDirection;
	protected JLabel lWindVertical;
	
	protected JLabel lPitch;
	protected JLabel lRoll;
	protected JLabel lWindAge;
	protected JLabel lCalibration;
	protected JLabel lBearingBosch;
	protected JLabel lBearingCMPS12;
	protected String speedUnits;
	protected javax.swing.Timer timer;
	protected int age;
	protected int maxAge;
	
	protected int fontSizeBig, fontSizeSmall;
		
	protected void updateStatus() {
		age++;

		lWindAge.setText(age + "");
		
		if ( age > maxAge ) {
			setBackground(Color.ORANGE);
		}
	}
	
	public void setWind(double ws, double wg, Double wd, Double wv, RecordRDLoggerCellCMPS12 rec, RecordVectorWindXTC recV) {

		
		NumberFormat f = new DecimalFormat("0.0");

		lWindSpeed.setText(f.format(ws));
		lWindGust.setText(f.format(wg));
		if ( null == wd ) {
			lWindDirection.setText( "");
		} else {
			lWindDirection.setText( f.format(wd));
		}
		
		if ( null == wv ) {
			lWindVertical.setText("");
		} else {
			lWindVertical.setText( f.format(wv));
		}
		
		if ( null != recV ) {
			/* add to VectorWindXTC specific tool tips */
			String lsn_tip = "<html>";
			
			/* add GPRMC data */
			RMCSentence rmc = (RMCSentence) recV.gnss_sentences.get("RMC");
						
			if ( null != rmc ) {
				lsn_tip += "GNSS {<br />";
				lsn_tip += "     Date: " + rmc.getDate().toISO8601() + " " + rmc.getTime().toISO8601() + "<br />";
				lsn_tip += "     Position: " + rmc.getPosition() + "<br />";
				lsn_tip += "     Mode: " + rmc.getMode() + "<br />";
				lsn_tip += "     Status: " + rmc.getStatus() + "<br />";
				lsn_tip += "}<br />";
/*
  				System.err.println("# RMC sentence ID: " + rmc.getSentenceId() );
				System.err.println("# RMC date:        " + rmc.getDate().toISO8601() );
				System.err.println("# RMC time:        " + rmc.getTime().toISO8601() );
				System.err.println("# RMC position:    " + rmc.getPosition() );
				System.err.println("# RMC variation:   " + rmc.getVariation() );
				System.err.println("# RMC mode:        " + rmc.getMode() );
				System.err.println("# RMC status:      " + rmc.getStatus() );
				System.err.println("# RMC valid:       " + rmc.isValid() );
*/				
 
			}
			
			
			lsn_tip += "</html>";
			
			lSerialNumber.setToolTipText(lsn_tip);
			
			lWindAge.setToolTipText("Battery: " + f.format(recV.getInputVoltage()) + "V");
		}
		
		if ( null != rec ) {
			System.err.println("Updating CMPS12 values on GUI");
			lBearingBosch.setText( f.format(rec.getBearingBosch()) + "\u00b0");
			lBearingCMPS12.setText( f.format(rec.getBearingCMPS12()) + "\u00b0");
			lPitch.setText( rec.getPitch() + "\u00b0");
			lRoll.setText( rec.getRoll() + "\u00b0");
			lCalibration.setText( rec.getCalibrationHTML() );
		}

		
		
		/* restart age count */
		lWindAge.setText("0");
		setBackground(Color.GREEN);
		age=0;
		timer.restart();
		
		/* timer for updating the status bar */
		if ( ! timer.isRunning() ) {
			timer.start();
		}

	}
	

	public AnemometerBigTextPanel(String title, String sUnits, int mAge, int fontSizeBig, int fontSizeLabel) {
		// super(new GridLayout(0,7)); /* 7 columns wide */
		super(new GridLayout(0,6)); /* 6 columns wide */

		this.fontSizeBig=fontSizeBig;
		this.fontSizeSmall=fontSizeLabel;
		
		age=0;
		
		speedUnits = sUnits;
		maxAge=mAge;

		setBackground(Color.GRAY);
//		setBorder(BorderFactory.createTitledBorder(title));

		lSerialNumber = new JLabel(title,SwingConstants.CENTER);
		lSerialNumber.setBorder(BorderFactory.createTitledBorder(null,"Unit ID",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lSerialNumber.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lSerialNumber.setForeground(Color.BLACK);
//		lSerialNumber.setToolTipText("this is a tool tip");

		
		lWindSpeed = new JLabel("",SwingConstants.CENTER);
		lWindSpeed.setBorder(BorderFactory.createTitledBorder(null,"Speed (" + speedUnits + ")",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindSpeed.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindSpeed.setForeground(Color.BLACK);

		lWindGust = new JLabel("",SwingConstants.CENTER);
		lWindGust.setBorder(BorderFactory.createTitledBorder(null,"Gust (" + speedUnits + ")",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindGust.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindGust.setForeground(Color.BLACK);
		
		lWindDirection = new JLabel("",SwingConstants.CENTER);
		lWindDirection.setBorder(BorderFactory.createTitledBorder(null,"Direction (\u00b0)",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindDirection.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindDirection.setForeground(Color.BLACK);

		lWindVertical = new JLabel("",SwingConstants.CENTER);
		lWindVertical.setBorder(BorderFactory.createTitledBorder(null,"Vertical (" + speedUnits + ")",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindVertical.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindVertical.setForeground(Color.BLACK);

			
		lWindAge = new JLabel("",SwingConstants.CENTER);
		lWindAge.setBorder(BorderFactory.createTitledBorder(null,"Data Age (seconds)",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindAge.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindAge.setForeground(Color.BLACK);

		lPitch = new JLabel("",SwingConstants.CENTER);
		lPitch.setBorder(BorderFactory.createTitledBorder(null,"Pitch",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lPitch.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lPitch.setForeground(Color.BLACK);

		lRoll = new JLabel("",SwingConstants.CENTER);
		lRoll.setBorder(BorderFactory.createTitledBorder(null,"Roll",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lRoll.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lRoll.setForeground(Color.BLACK);
		
	
		
		lCalibration = new JLabel("",SwingConstants.CENTER);
		lCalibration.setBorder(BorderFactory.createTitledBorder(null,"Calibration",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lCalibration.setFont(new Font("Serif", Font.BOLD, fontSizeLabel));
		lCalibration.setForeground(Color.BLACK);
		
		lBearingBosch = new JLabel("",SwingConstants.CENTER);
		lBearingBosch.setBorder(BorderFactory.createTitledBorder(null,"Bosch Bearing",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lBearingBosch.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lBearingBosch.setForeground(Color.BLACK);
		
		lBearingCMPS12 = new JLabel("",SwingConstants.CENTER);
		lBearingCMPS12.setBorder(BorderFactory.createTitledBorder(null,"CMPS12 Bearing",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lBearingCMPS12.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lBearingCMPS12.setForeground(Color.BLACK);
		
		/* first row */
		add(lSerialNumber);
		add(lWindSpeed);
		add(lWindGust);
		add(lWindDirection);
		add(lWindVertical);
		add(lWindAge);

		/* second row */
		if ( false ) {
			add(lBearingCMPS12);
			add(lBearingBosch);
			add(lPitch);
			add(lRoll);
			add(lCalibration);
		}
		
		/* add a timer to keep our status bar updated */
		timer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus();
			}
		});
	}
}
