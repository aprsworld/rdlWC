import java.awt.*;

import java.util.Calendar;

import javax.swing.*;


public class PanelDownload extends JPanel  {
	private static final long serialVersionUID = 1L;
	
	protected JTextField tFilename;
	public JButton bDownload;
	public JButton bDownloadFRAM;
	public JButton bLogInit;
	
	public String getDownloadFileName() {
		String s=tFilename.getText();
		
		/* add .CSV if no '.' present */
		if ( -1 == s.indexOf('.') ) {
			s = s + ".CSV";
		}
		
		return s;
	}
	
	public void setDownloadFileNamePrefix(String s) {
		if ( 0 == tFilename.getText().substring(0,5).compareTo("RDLWC") ) {
			tFilename.setText(s + tFilename.getText().substring(5) );
		}
	}
	
	public PanelDownload() {
		super(new GridLayout(0, 1));
//		super(new FlowLayout());
		

		setBackground(Color.white);
		setBorder(BorderFactory.createTitledBorder("Download"));
	
		Calendar now=Calendar.getInstance();
		
		add(new JLabel("Download data file name:"));
		
		String defaultFilename = "RDLWC_" + now.get(Calendar.YEAR);
		int i=now.get(Calendar.MONTH)+1;
		if ( i < 10 ) {
			defaultFilename = defaultFilename + "0";
		}
		defaultFilename = defaultFilename + i;
		
		i=now.get(Calendar.DAY_OF_MONTH);
		if ( i < 10 ) {
			defaultFilename = defaultFilename + "0";
		}
		defaultFilename = defaultFilename + i;
		
		tFilename=new JTextField(defaultFilename,30);
		add(tFilename);
		
		bDownload = new JButton("Download Now");
		add(bDownload);
//		bDownloadFRAM = new JButton("Download FRAM Now");
//		add(bDownloadFRAM);
		
		bLogInit = new JButton("Clear Logger Memory");
		add(bLogInit);
		
	}
}
