
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.border.TitledBorder;


public class AnemometerBigTextPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JLabel lWindSpeed;
	protected JLabel lWindGust;
	protected JLabel lWindDirection;
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

		lWindAge.setText(age + " sec");
		
		if ( age > maxAge ) {
			setBackground(Color.ORANGE);
		}
	}
	
	public void setWind(double ws, double wg, int wd, RecordRDLoggerCellCMPS12 rec) {

		
		NumberFormat f = new DecimalFormat("0.0");

		lWindSpeed.setText(f.format(ws) + " " + speedUnits);
		lWindGust.setText(f.format(wg) + " " + speedUnits);
		lWindDirection.setText( wd + "\u00b0");
		
		
		if ( null != rec ) {
			System.err.println("Updating CMPS12 values on GUI");
			lBearingBosch.setText( f.format(rec.getBearingBosch()) + "\u00b0");
			lBearingCMPS12.setText( f.format(rec.getBearingCMPS12()) + "\u00b0");
			lPitch.setText( rec.getPitch() + "\u00b0");
			lRoll.setText( rec.getRoll() + "\u00b0");
			lCalibration.setText( rec.getCalibrationHTML() );
		}

		
		
		/* restart age count */
		lWindAge.setText("0 sec");
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
		super(new GridLayout(0,5)); /* 5 columns wide */

		this.fontSizeBig=fontSizeBig;
		this.fontSizeSmall=fontSizeLabel;
		
		age=0;
		
		speedUnits = sUnits;
		maxAge=mAge;

		setBackground(Color.GRAY);
//		setBorder(BorderFactory.createTitledBorder(title));

		JLabel lSerialNumber = new JLabel(title,SwingConstants.CENTER);
		lSerialNumber.setBorder(BorderFactory.createTitledBorder(null,"Unit ID",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lSerialNumber.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lSerialNumber.setForeground(Color.BLACK);

		
		lWindSpeed = new JLabel("",SwingConstants.CENTER);
		lWindSpeed.setBorder(BorderFactory.createTitledBorder(null,"Wind Speed",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindSpeed.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindSpeed.setForeground(Color.BLACK);

		lWindGust = new JLabel("",SwingConstants.CENTER);
		lWindGust.setBorder(BorderFactory.createTitledBorder(null,"Wind Gust",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindGust.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindGust.setForeground(Color.BLACK);
		
		lWindDirection = new JLabel("",SwingConstants.CENTER);
		lWindDirection.setBorder(BorderFactory.createTitledBorder(null,"Wind Direction",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindDirection.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindDirection.setForeground(Color.BLACK);

		lPitch = new JLabel("",SwingConstants.CENTER);
		lPitch.setBorder(BorderFactory.createTitledBorder(null,"Pitch",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lPitch.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lPitch.setForeground(Color.BLACK);

		lRoll = new JLabel("",SwingConstants.CENTER);
		lRoll.setBorder(BorderFactory.createTitledBorder(null,"Roll",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lRoll.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lRoll.setForeground(Color.BLACK);
		
		
		lWindAge = new JLabel("",SwingConstants.CENTER);
		lWindAge.setBorder(BorderFactory.createTitledBorder(null,"Data Age",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		lWindAge.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		lWindAge.setForeground(Color.BLACK);

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
		add(lWindAge);

		/* second row */
		add(lBearingCMPS12);
		add(lBearingBosch);
		add(lPitch);
		add(lRoll);
		add(lCalibration);
		
		/* add a timer to keep our status bar updated */
		timer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus();
			}
		});
	}
}
