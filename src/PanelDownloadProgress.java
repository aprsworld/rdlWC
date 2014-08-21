import java.awt.*;

import javax.swing.*;


public class PanelDownloadProgress extends JPanel  {
	private static final long serialVersionUID = 1L;
	
	private JRadioButton b[];

	public void reset() {
		
	}
	
	public void set(int n) {
		
	}
	
	
	
	public PanelDownloadProgress(int n, int rows) {
		super(new GridLayout(rows, 0));
		b=new JRadioButton[n];
		
		
		
		/* overall border layout container that has another container north and a button south */
		setBackground(Color.white);
		setBorder(BorderFactory.createTitledBorder("Blocks Downloaded"));
	
		for ( int i=0 ; i<n ; i++ ) {
			b[i]=new JRadioButton();
			b[i].setEnabled(false);
			add(b[i]);
		}
		
	}
}
