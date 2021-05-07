
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.border.TitledBorder;


import net.sf.marineapi.nmea.sentence.*;

public class AnemometerBigTextPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JLabel lSerialNumber;
	protected JLabel lWindSpeed;
	protected JLabel lWindGust;
	protected JLabel lWindDirection;
	protected JLabel lWindVertical;
	protected JLabel lGNSSAltitude;
	protected JLabel lWindAge;
	
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
		String wdText="";
		
		NumberFormat f = new DecimalFormat("0.0");
		NumberFormat fz = new DecimalFormat("0");

		lWindSpeed.setText(f.format(ws));
		lWindGust.setText(f.format(wg));

		if ( null != wd ) {
			wdText=fz.format(wd) + "V";
		}
		
		if ( null == wv ) {
			lWindVertical.setText("");
		} else {
			lWindVertical.setText( f.format(wv));
		}
		
		if ( null != recV ) {
			/* add to VectorWindXTC specific tool tips */
			String lsn_tip = "<html>";
			
			/* add additional GNSS data */
			HDTSentence hdt = (HDTSentence) recV.gnss_sentences.get("HDT");
			RMCSentence rmc = (RMCSentence) recV.gnss_sentences.get("RMC");
			GGASentence gga = (GGASentence) recV.gnss_sentences.get("GGA");
			NMEASentencePSAT psat = (NMEASentencePSAT) recV.gnss_sentences.get("SAT");

			if ( null != hdt ) {
				lsn_tip += "GNSS HDT {<br />";
				lsn_tip += "     Heading:   " + hdt.getHeading() + "<br />";
				lsn_tip += "}<br />";
				
				/* set the wind direction to heading if wind direction is empty */
				if ( 0==wdText.length() ) {
					wdText = fz.format(hdt.getHeading() + "H");
				}
			}
			
			if ( null != rmc ) {
				lsn_tip += "GNSS RMC {<br />";
				lsn_tip += "     Date:      " + rmc.getDate().toISO8601() + "<br />";
				lsn_tip += "     Time:      " + rmc.getTime().toISO8601() + "<br />";
				lsn_tip += "     Position:  " + rmc.getPosition() + "<br />";
				lsn_tip += "     Mode:      " + rmc.getMode() + "<br />";
				lsn_tip += "     Status:    " + rmc.getStatus() + "<br />";
				lsn_tip += "}<br />";
			} 
			
			if ( null != gga ) {
				lsn_tip += "GNSS GGA {<br />";
				lsn_tip += "     Time:      " + gga.getTime().toISO8601() + "<br />";
				lsn_tip += "     Position:  " + gga.getPosition() + "<br />";
				lsn_tip += "     Quality:   " + gga.getFixQuality() + "<br />";
				lsn_tip += "     SV in use: " + gga.getSatelliteCount() + "<br />";
				lsn_tip += "     HDOP:      " + gga.getHorizontalDOP() + "<br />";
				lsn_tip += "     Geod Sep:  " + gga.getGeoidalHeight() + " " + gga.getGeoidalHeightUnits() + "<br />";
				lsn_tip += "     Diff Age:  " + gga.getDgpsAge() + "<br />";
				lsn_tip += "}<br />";				
				
				lGNSSAltitude.setText(f.format(gga.getAltitude()));
			} else {
				lGNSSAltitude.setText("");
			}
			
			if ( null != psat && psat.isHPR() ) {
				lsn_tip += "GNSS PSAT HPR {<br />";
				lsn_tip += "     PSAT type: " + psat.getSubsentenceName() + "<br />";
				lsn_tip += "     Time:      " + psat.getHPRTime() + "<br />";
				lsn_tip += "     Heading:   " + psat.getHPRHeading() + "<br />";
				lsn_tip += "     Pitch:     " + psat.getHPRPitch() + "<br />";
				lsn_tip += "     Roll:      " + psat.getHPRRoll() + "<br />";
				lsn_tip += "     Type:      " + psat.getHPRType() + "<br />";
				lsn_tip += "}<br />";
				
				/* set the wind direction to heading if wind direction is empty */
				if ( 0==wdText.length() && null != psat.getHPRHeading() ) {
					wdText = fz.format(psat.getHPRHeading());
					
					if ( 0==psat.getHPRType().compareTo("N") ) 
						wdText += "G"; // GNSS derived
					else if ( 0==psat.getHPRType().compareTo("G") )
						wdText += "Y";
					else 
						wdText += "?";
				}
			}
			
			lsn_tip += "</html>";
			
			lSerialNumber.setToolTipText(lsn_tip);
			
			lWindAge.setToolTipText("Battery: " + f.format(recV.getInputVoltage()) + "V");
		}

		lWindDirection.setText(wdText);
		
		
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
		super(new GridLayout(0,7)); /* 6 columns wide */

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

		lGNSSAltitude = new JLabel("",SwingConstants.CENTER);
		lGNSSAltitude.setBorder(BorderFactory.createTitledBorder(null,"Altitude (meters)",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lGNSSAltitude.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lGNSSAltitude.setForeground(Color.BLACK);

		
		/* first row */
		add(lSerialNumber);
		add(lWindSpeed);
		add(lWindGust);
		add(lWindDirection);
		add(lWindVertical);
		add(lGNSSAltitude);
		add(lWindAge);
		
		/* add a timer to keep our status bar updated */
		timer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus();
			}
		});
	}
}
