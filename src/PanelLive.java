import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.*;


public class PanelLive extends JPanel  {
	private static final long serialVersionUID = 1L;

	protected JLabel lSerialNumber;
	protected JLabel lWs,lWg,lWc, lWindDirection, lBattery;
	public JButton statusButton;
	
	public void updateNow(RecordRDLoggerCell r) {

		//		System.err.println("# pStatus updateNow() called");

		lSerialNumber.setText(r.serialNumber);

		DecimalFormat df = new DecimalFormat("0.0");

		/* wind speed */
		double ws;
		if ( r.tPulseTime > 0 && r.tPulseTime<65535 )
			ws=7650.0 / r.tPulseTime + 0.35;
		else
			ws=0.0;

		/* wind gust */
		double wg;
		if ( r.tPulseMinTime > 0 && r.tPulseMinTime<65535 )
			wg=7650.0 / r.tPulseMinTime + 0.35;
		else
			wg=0.0;


		lWs.setText(df.format(ws));
		lWg.setText(df.format(wg));
		lWc.setText(r.windCount + "");

		lBattery.setText(r.batteryStateOfCharge + "%");
		lWindDirection.setText(r.windDirectionSector*45 + " degrees from boom");

	}

	public PanelLive() {
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

		content.add(new JLabel("Wind Speed (m/s): "));
		lWs = new JLabel("---");
		content.add(lWs);

		content.add(new JLabel("Wind Gust (m/s):"));
		lWg = new JLabel("---");
		content.add(lWg);

		content.add(new JLabel("Wind Pulse Count:"));
		lWc = new JLabel("---");
		content.add(lWc);

		content.add(new JLabel("Battery state of charge:"));
		lBattery = new JLabel("---");
		content.add(lBattery);

		content.add(new JLabel("Wind Direction:"));
		lWindDirection = new JLabel("---");
		content.add(lWindDirection);
		/* north */
		add(content,BorderLayout.PAGE_START);

	}
}
