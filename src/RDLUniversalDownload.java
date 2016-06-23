import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class RDLUniversalDownload extends Thread implements PacketListener {
	protected List<String> listSerialNumbers = new ArrayList<String>();
	protected SelectionPanel selectPanel;
	protected JFrame f;
	protected JLabel statusLabel;
	protected Date recordDate;
	protected LinkSerial link;
	protected PanelDownload pDownload;
	protected PanelStatus pStatus;
	protected PanelLive pLive;
	protected PanelMonitor pMonitor;
	protected javax.swing.Timer timer;
	protected IniFile ini;
	private Thread loaderThread;

	protected char serialPrefix;
	protected int serialNumber;
	protected boolean updated = false;

	protected RDLUniversalReader remote;
	protected RecordRDLoggerCellStatus rStatus;
	protected RecordRDLoggerCell rLive;
	protected RecordRDLoggerCellHistory rHistory;

	protected long startTime;
	protected String[] windHistoryRecord;
	protected LogProcess log;
	public static final String compileDate = "2016-06-02";

	public static int crc_chk(int data[], int start, int length) {
		int j;
		int reg_crc = 0xFFFF;

		for (int i = start; i < (length + start); i++) {
			reg_crc ^= data[i];

			for (j = 0; j < 8; j++) {
				if ((reg_crc & 0x01) == 1) {
					reg_crc = (reg_crc >> 1) ^ 0xA001;
				} else {
					reg_crc = reg_crc >> 1;
				}
			}
		}

		return reg_crc;
	}

	public static int crc_chk(byte data[], int start, int length) {
		int j;
		int reg_crc = 0xFFFF;

		for (int i = start; i < (length + start); i++) {
			reg_crc ^= data[i];

			for (j = 0; j < 8; j++) {
				if ((reg_crc & 0x01) == 1) {
					reg_crc = (reg_crc >> 1) ^ 0xA001;
				} else {
					reg_crc = reg_crc >> 1;
				}
			}
		}

		return reg_crc;
	}

	public void dumpPacket(WorldDataPacket p) {
		for (int i = 0; i < p.packet.length; i++) {
			System.out.printf("p[%d] 0x%02X\n", i, p.packet[i]);
		}
		System.out.println("-------------------------");
	}

	public boolean haveValidPage(int page) {
		int pageStartRecord = page * 44;

		/*
		 * don't know how many records in 4096 ... but we should not see any
		 * null preceeding data
		 */
		if (4096 == page)
			return true;

		int valid = 0;
		for (int i = pageStartRecord; i < (pageStartRecord + 44); i++) {
			if (null != windHistoryRecord[i])
				valid++;
		}

		return (44 == valid);
	}

	public void dumpRXPages() {
		pMonitor.append("# pages (time="
				+ (System.currentTimeMillis() - startTime) / 1000.0 + " sec): ");

		/* check for complete pages */
		for (int page = 0; page < 4097; page++) {
			if (haveValidPage(page)) {
				pMonitor.append(page + ", ");
			}
		}
		pMonitor.append("\n");

	}

	public void clearRXPackets() {
		// java.util.Arrays.fill(rxPackets,false);
		// windHistory.clear();

		java.util.Arrays.fill(windHistoryRecord, null);
	}

	public void packetReceived(WorldDataPacket packet) {
		// System.err.println("# received: " + s.i );
		// System.err.println("# received a packet: " + packet);

		if (!packet.isValid()){
			return;
		}
		/*
		 * check if the packet serial number is in a list, if not, add it to the
		 * list and continue. if it is in the list, then return
		 */
		// System.out.println("found: " + packet.serial_prefix + ""
		// + packet.serial_number);
		
		if (!listSerialNumbers.contains(packet.serial_prefix + "" + packet.serial_number) && packet.serial_prefix == 'R' ) {
			/* listNull() tests to make sure the selection panel has been initialized before adding to it - This
			 * avoids a null pointer exception that broke the program when a packet would come in while
			 * the program was starting up. */
			if (selectPanel.listNull() == true){
				return;		
			}
			listSerialNumbers.add(packet.serial_prefix + ""
					+ packet.serial_number);

			System.err.println("added serial number to list: "
					+ packet.serial_prefix + "" + packet.serial_number);
			System.err.println("list now contains: "
					+ listSerialNumbers);
			selectPanel.updateList(packet.serial_prefix, packet.serial_number);

		}
		
		
		/*
		 * ignore packets until we get a serial number response from the
		 * customer
		 */
		if (0 == serialPrefix && 0 == serialNumber){
			return;
		}
		
		
		if ((0 == serialPrefix && 0 == serialNumber)
				&& (7 == packet.type || 8 == packet.type || 31 == packet.type)) {
			// selectSerial(packet.serial_prefix, packet.serial_number);

		}			

		/* ignore packets that aren't for us */
		if (packet.serial_prefix != serialPrefix
				|| packet.serial_number != serialNumber)
			return;

		/* see what sort of packet we need to process */
		if (7 == packet.type || 31 == packet.type) {
			/* Live Packet */
			rLive.parseRecord(packet.packet);
			pLive.updateNow(rLive);
			/* immediately do a status query */
			requestStatus();
				if(updated == false){
					if(pLive.lSerialNumber.getText() == "---"){
						System.err.println("got here");
						updated = false;
					}
					else{
						pDownload.bDownload.setEnabled(true); 
						pDownload.bLogInit.setEnabled(true);
						updated = true;		
					}				
				}
		} else if (8 == packet.type) {
			/* Status packet */
			rStatus.parseRecord(packet.packet);
			pStatus.updateNow(rStatus);

		} else if (21 == packet.type) {
			/* History Packet */
			rHistory.parseRecord(packet.packet);
			int packetNumber = ((packet.packet[6] << 8) + packet.packet[7]);

			/* parse packet to strings of data */
			String r[] = rHistory.getRecordStrings();
			for (int i = 0; i < r.length; i++) {
				int recordNumber = packetNumber * 4 + i;
				if (recordNumber < windHistoryRecord.length) {
					windHistoryRecord[recordNumber] = r[i];
				} else {
					System.err
							.printf("# recordNumber=%d and windHistoryRecord.length=%d!\n",
									recordNumber, windHistoryRecord.length);
				}
			}

		} else {
			System.out.printf("# Unknown packet type 0x%02X\n", packet.type);
		}
		
	}

	RDLUniversalDownload(String inifilename) {
		ini = new IniFile(inifilename);
	}

	public void setVisible(boolean state) {
		f.setVisible(state);
	}

	void requestDataBlocks(int startPage, int nPages) {
		int[] buff = new int[13];
		buff[0] = (int) '#';
		buff[1] = (int) serialPrefix;
		buff[2] = (serialNumber >> 8) & 0xff;
		buff[3] = (serialNumber & 0xff);
		buff[4] = 13;
		buff[5] = 22;
		buff[6] = 2; /* function 2, request data page */

		buff[7] = (startPage >> 8) & 0xff; /* start address MSB */
		buff[8] = startPage & 0xff; /* address 4096 is fram */

		buff[9] = (nPages >> 8) & 0xff; /* number of the pages */
		buff[10] = nPages & 0xff;

		int lCRC = crc_chk(buff, 1, 10);
		buff[11] = (lCRC >> 8) & 0xff;
		buff[12] = lCRC & 0xff;

		remote.sendPacket(buff);
	}

	public void downloadAll() {
		int page;
		int totalPages;
		int maxPages;
		double estimatedTime;
		int sleepTimeMS = 400;
		maxPages = Integer.parseInt(ini.getValueSafe("GENERAL", "max_internal_pages", "4096"));
		
		totalPages = rStatus.dataflash_page;
		if(totalPages >= maxPages){
			totalPages = maxPages;
		}
		// totalPages=10;
		estimatedTime = 0.001 * sleepTimeMS * totalPages;
		pMonitor.append(String.format(
				"# Estimated download time is %.1f seconds\n", estimatedTime));

		startTime = System.currentTimeMillis();

		/* download FRAM page */
		pMonitor.append("# Starting to download 1 FRAM page.\n");
		requestDataBlocks(4096, 1);
		try {
			Thread.sleep(2 * sleepTimeMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		pMonitor.append("# Starting to download + " + totalPages
				+ " dataflash pages.\n");
		for (page = 0; page < totalPages; page++) {
			pMonitor.append("# Requesting page " + page + "\n");
			requestDataBlocks(page, 1);

			try {
				Thread.sleep(sleepTimeMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		pMonitor.append("# First pass complete. Successfully received the following:\n");
		dumpRXPages();

		int validPages = 0;
		for (int tries = 0; tries < 10; tries++) {
			validPages = 0;
			for (page = 0; page < totalPages; page++) {
				if (haveValidPage(page)) {
					validPages++;
					continue;
				}

				pMonitor.append("# Requesting page " + page + " on pass "
						+ (tries + 2) + "\n");
				requestDataBlocks(page, 1);

				try {
					Thread.sleep(sleepTimeMS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (validPages == totalPages)
				break;
		}
		dumpRXPages();
		pMonitor.append("# validPages " + validPages + "/" + totalPages + "\n");
		pMonitor.append("# Download finished.\n");

		saveHistory();
		clearRXPackets();
	}

	public void requestStatus() {
		int[] buff = new int[13];
		buff[0] = (int) '#';
		buff[1] = (int) serialPrefix;
		buff[2] = (serialNumber >> 8) & 0xff;
		buff[3] = (serialNumber & 0xff);
		buff[4] = 13;
		buff[5] = 22;
		buff[6] = 0;
		buff[7] = 0;
		buff[8] = 0;
		buff[9] = 0;
		buff[10] = 0;

		int lCRC = crc_chk(buff, 1, 10);
		buff[11] = (lCRC >> 8) & 0xff;
		buff[12] = lCRC & 0xff;

		remote.sendPacket(buff);
		
	}

	public void requestLogInit() {
		int[] buff = new int[13];
		buff[0] = (int) '#';
		buff[1] = (int) serialPrefix;
		buff[2] = (serialNumber >> 8) & 0xff;
		buff[3] = (serialNumber & 0xff);
		buff[4] = 13;
		buff[5] = 22;
		buff[6] = 1;
		buff[7] = 0;
		buff[8] = 0;
		buff[9] = 0;
		buff[10] = 0;

		int lCRC = crc_chk(buff, 1, 10);
		buff[11] = (lCRC >> 8) & 0xff;
		buff[12] = lCRC & 0xff;

		remote.sendPacket(buff);
	}

	/*
	 * Query addressed to us: '#' 0 STX UNIT ID PREFIX 1 First character (A-Z)
	 * of OUR serial number UNIT ID MSB 2 OUR serial number UNIT ID LSB 3 PACKET
	 * LENGTH 4 number of byte for packet including STX through CRC (13) PACKET
	 * TYPE 5 type of packet we are sending (22) COMMAND 6 function to perform
	 * PARAM A MSB 7 parameter A to function PARAM A LSB 8 PARAM B MSB 9
	 * parameter B to function PARAM B LSB 10 CRC MSB 11 high byte of CRC on
	 * everything after STX and before CRC CRC LSB 12 low byte of CRC
	 * 
	 * FUNCTIONS 0 SEND STATUS PACKET no parameters 1 CLEAR MEMORY (LOG_INIT) no
	 * parameters (check for result by checking status packet) 2 REQUEST DATA
	 * PAGE A: start address (0 to 4096) B: number of pages (11 packets per page
	 * returned)
	 */

	public void selectSerial(char sp, int sn) {
		JDialog.setDefaultLookAndFeelDecorated(true);
		int response = JOptionPane.showConfirmDialog(null,
				"Is " + Character.toString(sp) + sn
						+ " the unit you wish to communicate with?", "Confirm",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (response == JOptionPane.NO_OPTION
				|| response == JOptionPane.CLOSED_OPTION) {

			for (Iterator<String> iter = listSerialNumbers.listIterator(); iter
					.hasNext();) {
				String a = iter.next();
				if (a.equals(sp + "" + sn)) {
					System.err.println("removed the item: " + sp + "" + sn);
					iter.remove();
				}
			}
			return;
		} else if (response == JOptionPane.YES_OPTION) {
			serialPrefix = sp;
			serialNumber = sn;
			Integer i = new Integer(serialNumber);
			if (null != pDownload) {
				pDownload
						.setDownloadFileNamePrefix(serialPrefix + i.toString());
			}
			return;
		}
	}

	class NonBlockingLoadAction extends AbstractAction implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// note that this doesn't offer a means of being interrupted
		// so it refuses second launch instead
		public void actionPerformed(ActionEvent e) {
			if (loaderThread != null)
				return;
			loaderThread = new Thread((Runnable) this);
			loaderThread.start();

		}

		public void run() {
			// loadURL(true);
			// loaderThread = null;
			// System.out.println("Someone clicked on a button");
			downloadAll();
			loaderThread = null;
		}
	}

	public void saveHistory() {
		String header = "DATE & TIME (UTC), SPEED INSTANT (MPH), GUST (MPH), SPEED AVERAGE (MPH), SPEED INSTANT (m/s), GUST (m/s), SPEED AVERAGE (m/s), COUNT (pulses), DIRECTION (sector), BATTERY (% full), PACKET NUMBER, RECORD IN PACKET, RECORD NUMBER, PAGE NUMBER\r\n";

		pMonitor.append("# Creating log file: "
				+ pDownload.getDownloadFileName() + "\n");
		log.createLog(pDownload.getDownloadFileName());
		log.writeLog(header);
		int totalLines = 0;

		/* dump non-null records */
		for (int i = 0; i < windHistoryRecord.length; i++) {
			if (null == windHistoryRecord[i])
				continue;

			log.writeLog(windHistoryRecord[i] + "\r\n");
			totalLines++;
		}

		log.closeLog();
		pMonitor.append("# Done writing log file.\n");

		String[] commands = { "cmd", "/c", "start", "\"DummyTitle\"",
				pDownload.getDownloadFileName() };
		try {
			Runtime.getRuntime().exec(commands);
		} catch (IOException e1) {
			System.err
					.println("# Error while attempting to open Excel and load file.");
			e1.printStackTrace();
		}

	}

	public void run() {
		WindowUtilities.setNativeLookAndFeel();

		String serialPort;
		int serialSpeed;
		
		serialPrefix = 0;
		serialNumber = 0;

		rStatus = new RecordRDLoggerCellStatus();
		rLive = new RecordRDLoggerCell();
		rHistory = new RecordRDLoggerCellHistory();

		windHistoryRecord = new String[4097 * 44];
		log = new LogProcess();
		
		serialPort = ini.getValueSafe("SERIAL", "port", "COM1");
		serialSpeed = Integer.parseInt(ini.getValueSafe("SERIAL", "speed",
				"57600"));
		System.err.println("# Opening " + serialPort + " @ " + serialSpeed);

		/* put up a indeterminte progress bar while RXTX wastes time */
		JFrame fProgress = new JFrame("Software Starting...");
		JProgressBar progress = new JProgressBar(0, 100);
		progress.setIndeterminate(true);
		fProgress.setSize(300, 65);
		fProgress.add(progress);
		fProgress.setVisible(true);

		/* actually make our remote reader */
		remote = new RDLUniversalReader(serialPort, serialSpeed);
		remote.addPacketListener(this);

		/* make progress bar go away */
		fProgress.setVisible(false);

		f = new JFrame("RDLUniversal Download Software - " + compileDate);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		f.setSize(900, 525);

		/* Overall BorderLayout */
		Container cont = f.getContentPane();
		cont.setBackground(Color.white);
		cont.setLayout(new BorderLayout());

		/* Our body section */
		Container content = new Container();

		content.setBackground(Color.white);
		content.setLayout(new GridLayout(0, 3)); /*
												 * two columns wide ... as long
												 * as we need
												 */

		/* download panel */
		pDownload = new PanelDownload();
		NonBlockingLoadAction nonBlocker = new NonBlockingLoadAction();
		pDownload.bDownload.addActionListener(nonBlocker);

		pDownload.bLogInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int reply = JOptionPane.showConfirmDialog(null, "WARNING! This will erase all recorded data from the data logger's internal memory. This should be done on a regular \n basis, but make sure you have downloaded a valid copy of the data before doing so. Are you sure you want to do this?", "Clear Data?",  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				
				if (reply == JOptionPane.YES_OPTION)
				{
					requestLogInit();
				}
			}
		});
		content.add(pDownload);

		/* live panel */
		pLive = new PanelLive();
		content.add(pLive);

		/* status panel */
		pStatus = new PanelStatus();
		pStatus.statusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestStatus();
			}
		});
		content.add(pStatus);

		/* title text and photo */
		Container titleContainer = new Container();
		titleContainer.setLayout(new GridLayout(2,1));

		JLabel titleLabel = new JLabel(
				"For manufacturer's support please contact APRS World, LLC at +1-507-454-2727 or info@aprsworld.com");
		titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
		titleLabel.setForeground(Color.blue);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel wifiLabel = new JLabel("Disable WI-FI before Downloading data"
				
				);
		wifiLabel.setFont(new Font("Serif", Font.BOLD, 18));
		wifiLabel.setForeground(Color.red);
		wifiLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleContainer.add(titleLabel);
		titleContainer.add(wifiLabel);
		
		/* add the title */
		cont.add(titleContainer, BorderLayout.PAGE_START);

		/* Add the body */
		cont.add(content, BorderLayout.CENTER);

		/* add serial monitor */
		pMonitor = new PanelMonitor();
		cont.add(pMonitor, BorderLayout.PAGE_END);
		pMonitor.append("# Please wait until software prompts for a serial number to communicate with.\n");
		pMonitor.append("# Once you have selected the serial number, then wait until the left hand status\n");
		pMonitor.append("# column shows current information. You may now download or clear log.\n\n");

		f.setLocationRelativeTo(null);

		f.addWindowListener(new ExitListener());

		setVisible(true);

		selectPanel = new SelectionPanel(pDownload, this);
		selectPanel.showGUI();
	}

	public static void main(String args[]) {
		String ini = null;

		if (args.length == 1) {
			ini = args[0];
		} else {
			System.err.println("Usage: java RDLUniversalDownload inifile");
			System.err
					.println("Invoke with -Dswing.aatext=true for anti-aliased fonts");

			ini = "config_default.ini";
		}

		SerialIOInstall.installSerialIO();

		/*
		 * Properties p = System.getProperties(); Enumeration keys = p.keys();
		 * while (keys.hasMoreElements()) { String key =
		 * (String)keys.nextElement(); String value = (String)p.get(key);
		 * System.out.println(key + ": " + value); }
		 */

		(new RDLUniversalDownload(ini)).start();
	}

}
