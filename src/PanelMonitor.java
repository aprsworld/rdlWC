import java.awt.*;
import javax.swing.*;

public class PanelMonitor extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JTextArea serialMonitor;
	
	public void append(String line) {
		serialMonitor.append(line);
		
		String s = serialMonitor.getText();
        if ( s.length() > 4096 ) {
        	//System.err.println("# trimming text");
        	s=s.substring(2048);
        	serialMonitor.setText(s);
        }
        serialMonitor.setCaretPosition(s.length());
	}
	
	public PanelMonitor() {
//		super(new GridLayout(0, 1));
		super(new FlowLayout());

		setBackground(Color.white);
		setBorder(BorderFactory.createTitledBorder("Progress"));
		
		serialMonitor = new JTextArea(10, 80);
		serialMonitor.setEditable(false);
		JScrollPane scrollingResult = new JScrollPane(serialMonitor);
		scrollingResult.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollingResult.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		add(scrollingResult);
		
		
		
	}
}
