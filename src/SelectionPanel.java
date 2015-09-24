import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class SelectionPanel {

	JFrame frame;
	JList jlSerialNumbers;
	JButton select;
	ListSelectionModel listSelectionModel;
	DefaultListModel model;
	PanelDownload pDownload;
	RDLUniversalDownload rdl;
	JTextArea instr;

	public SelectionPanel(PanelDownload pd, RDLUniversalDownload rd) {

		pDownload = pd;
		rdl = rd;
	}

	public void showGUI() {

		frame = new JFrame("Select your Serial Number");
		frame.setLayout(new GridLayout(0, 1));
		frame.setSize(300, 300);
		instr = new JTextArea("Listening for data. When you see the serial number you wish to communicate with, click it and press \"Use this serial number\"");
		instr.setLineWrap(true);
		instr.setWrapStyleWord(true);
		instr.setEditable(false);
		instr.setBackground(Color.WHITE);
		model = new DefaultListModel();
		jlSerialNumbers = new JList();
		jlSerialNumbers.setModel(model);
		jlSerialNumbers.setFont(new Font("Arial",Font.BOLD,16));
		// jlSerialNumbers.setSize(200, 300);
		select = new JButton("Use this serial number");
		// select.setSize(100, 200);
		select.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				if (null != jlSerialNumbers.getSelectedValue()) {
					System.out.println(jlSerialNumbers.getSelectedValue()
							.toString());
					buttonAction(jlSerialNumbers.getSelectedValue().toString());
					frame.setVisible(false);
				} else {
					System.out.println("Nothing selected");
				}

			}
		});
		frame.add(instr);
		frame.add(jlSerialNumbers);
		frame.add(select);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	public boolean listNull(){
		if(jlSerialNumbers.getModel() == null){
			return true;
		}
		else{
			System.err.println("not null");
			return false;
		}
	}
	public void updateList(char pref, int ser) {
		if(jlSerialNumbers.getModel() != null){
			model.ensureCapacity(model.getSize()+1);
			model.addElement(pref+"_"+ser);
			System.err.println("test");
		}
		else{
			System.err.println("GUI not fully created yet - "+pref+""+ser+" not yet added");
		}
	}

	public void buttonAction(String serialNumber) {
		if (null != pDownload && null != rdl) {
			pDownload.setDownloadFileNamePrefix(serialNumber);
			rdl.serialPrefix = serialNumber.charAt(0);
			rdl.serialNumber = Integer.parseInt(serialNumber.substring(1));
		}
	}
}
