//import gnu.io.CommPortIdentifier;
import javax.comm.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;



public class RDLUniversalSetup extends Thread {

	protected IniFile ini;
	JComboBox cbPorts;
	Vector<String> ports;
	
	RDLUniversalSetup (String inifilename) {
		ini = new IniFile(inifilename);
	}
	
	protected void writeConfigNow() {
		String serialPort=ports.elementAt(cbPorts.getSelectedIndex());
		String max_pages="4096";
		System.err.println("Serial port " + serialPort + " selected.");
		//check if max_internal_pages already exists and then create it if it doesn't
		if(ini.getValue("GENERAL", "max_internal_pages") == null){
			ini.setValue("GENERAL","max_internal_pages", max_pages);
		}
		ini.setValue("SERIAL","port",serialPort);
		ini.saveFile();

		
		System.exit(0);
	}
	
	public void run() {
		cbPorts=null;
		
		WindowUtilities.setNativeLookAndFeel();
		
		JFrame f = new JFrame("RDL Universal Setup");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		f.setSize(250, 150);

		/* Overall BorderLayout */
		Container cont = f.getContentPane();
		cont.setBackground(Color.white);
		cont.setLayout(new BorderLayout());
		

		JLabel lTitle = new JLabel("RDL Universal Setup");
		lTitle.setFont(new Font("Serif", Font.BOLD, 18));
		lTitle.setForeground(Color.blue);
		f.add(lTitle,BorderLayout.PAGE_START);

		System.out.println("# Scanning serial ports: ");
		/* Get all available ports */
		Enumeration<?> portIdentifiers = CommPortIdentifier.getPortIdentifiers();

		
		/* Find requested port */
		ports = new Vector<String>();
		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier p = (CommPortIdentifier)portIdentifiers.nextElement();
			
			if (p.getPortType() == CommPortIdentifier.PORT_SERIAL ) { 
				System.out.println("Available port=" +p.getName());
				ports.add(p.getName());
			}
		}

	
		cbPorts = new JComboBox(ports);
		f.add(cbPorts,BorderLayout.CENTER);
	
		JButton bInstall = new JButton("Install Now");
		f.add(bInstall,BorderLayout.PAGE_END);
		bInstall.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						writeConfigNow();
					}
				}
		);
		
		
		f.setVisible(true);
	}
	

	public static void main(String[] args) {
		String ini = null;

		if (args.length == 1) {
			ini = args[0];
		} else {
			ini="config_default.ini";
		}


		SerialIOInstall.installSerialIO();
		
		
		(new RDLUniversalSetup(ini)).start();
	}

}
