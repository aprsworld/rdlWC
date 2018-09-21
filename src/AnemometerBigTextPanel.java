
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.border.TitledBorder;


public class AnemometerBigTextPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JLabel windSpeed;
	protected JLabel windGust;
	protected JLabel windDirection;
	protected JLabel pitch;
	protected JLabel roll;
	protected JLabel windAge;
	protected String speedUnits;
	protected javax.swing.Timer timer;
	protected int age;
	protected int maxAge;
		
	protected void updateStatus() {
		age++;

		windAge.setText(age + " sec");
		
		if ( age > maxAge ) {
			setBackground(Color.ORANGE);
		}
	}
	
	public void setWind(double ws, double wg, int wd, int p, int r) {

		
		NumberFormat f = new DecimalFormat("0.0");

		windSpeed.setText(f.format(ws) + " " + speedUnits);
		windGust.setText(f.format(wg) + " " + speedUnits);
		windDirection.setText( wd + "\u00b0");
		pitch.setText( p + "\u00b0");
		roll.setText( r + "\u00b0");

		/* restart age count */
		windAge.setText("0 sec");
		setBackground(Color.GREEN);
		age=0;
		timer.restart();
		
		/* timer for updating the status bar */
		if ( ! timer.isRunning() ) {
			timer.start();
		}

	}
	

	public AnemometerBigTextPanel(String title, String sUnits, int mAge, int fontSizeBig, int fontSizeLabel) {
		super(new GridLayout(0,7)); /* 7 columns wide */

		
		age=0;
		
		speedUnits = sUnits;
		maxAge=mAge;

		setBackground(Color.GRAY);
//		setBorder(BorderFactory.createTitledBorder(title));

		JLabel rowLabel = new JLabel(title,SwingConstants.CENTER);
		rowLabel.setBorder(BorderFactory.createTitledBorder(null,"Unit ID",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		rowLabel.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		rowLabel.setForeground(Color.BLACK);
		add(rowLabel);

		
		windSpeed = new JLabel("",SwingConstants.CENTER);
		windSpeed.setBorder(BorderFactory.createTitledBorder(null,"Wind Speed",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		windSpeed.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		windSpeed.setForeground(Color.BLACK);

		windGust = new JLabel("",SwingConstants.CENTER);
		windGust.setBorder(BorderFactory.createTitledBorder(null,"Wind Gust",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		windGust.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		windGust.setForeground(Color.BLACK);
		
		windDirection = new JLabel("",SwingConstants.CENTER);
		windDirection.setBorder(BorderFactory.createTitledBorder(null,"Wind Direction",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		windDirection.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		windDirection.setForeground(Color.BLACK);

		pitch = new JLabel("",SwingConstants.CENTER);
		pitch.setBorder(BorderFactory.createTitledBorder(null,"Pitch",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		pitch.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		pitch.setForeground(Color.BLACK);

		roll = new JLabel("",SwingConstants.CENTER);
		roll.setBorder(BorderFactory.createTitledBorder(null,"Roll",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		roll.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		roll.setForeground(Color.BLACK);
		
		
		windAge = new JLabel("",SwingConstants.CENTER);
		windAge.setBorder(BorderFactory.createTitledBorder(null,"Data Age",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,new Font("times new roman",Font.BOLD,fontSizeLabel), Color.BLACK));
		windAge.setFont(new Font("Serif", Font.BOLD, fontSizeBig));
		windAge.setForeground(Color.BLACK);

		add(windSpeed);
		add(windGust);
		add(windDirection);
		add(pitch);
		add(roll);
		add(windAge);
		
		/* add a timer to keep our status bar updated */
		timer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatus();
			}
		});
	}
}
