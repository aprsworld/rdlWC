import java.awt.*;
import javax.swing.*;


public class PanelSetup extends JPanel  {
	private static final long serialVersionUID = 1L;
	
	protected JComboBox cOntime, cOnfor;
	public JButton bSetTimes;
	
	public int getOnForHours() {
		return cOnfor.getSelectedIndex()+1;
	}
	
	public int getOnAtHour() {
		return cOntime.getSelectedIndex();
	}
	
	
	public PanelSetup() {
		super(new GridLayout(0, 1));

		setBackground(Color.white);
		setBorder(BorderFactory.createTitledBorder("Data Logger Setup"));
		
		
		add(new JLabel("Turn on wireless at:"));
		
		String[] ontimes=new String[24];
		
		for ( int i=0 ; i<24 ; i++ ) {
			ontimes[i]=Integer.toString(i) + ":00";
			
			if ( i < 10 ) {
				ontimes[i]="0" + ontimes[i];
			}
		}
		cOntime = new JComboBox(ontimes);
		
		cOntime.setSelectedIndex(8);
		add(cOntime);

		add(new JLabel("Leave on for:"));
		
		String[] onfor= {"1 hour","2 hours","3 hours","4 hours"};
		cOnfor = new JComboBox(onfor);
		cOnfor.setSelectedIndex(2);
		add(cOnfor);

		bSetTimes=new JButton("Set wireless availability times");
		add(bSetTimes);
		
	}
}
