

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


public class AnemometerHistoryPanel extends JPanel {

    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TimeSeries windSpeed;
    private TimeSeries windGust;

    /**
     * Creates a new application.
     * 
     * @param maxAge  the maximum age (in seconds).
     */
    @SuppressWarnings("deprecation")
	public AnemometerHistoryPanel(int maxAge, int width, int height, String wUnits) {

        super(new BorderLayout());

        // create two series that automatically discard data more than 30 
        // seconds old...
        this.windSpeed = new TimeSeries("Wind Speed", Second.class);
        this.windSpeed.setMaximumItemAge(maxAge);
        this.windGust = new TimeSeries("Wind Gust", Second.class);
        this.windGust.setMaximumItemAge(maxAge);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.windSpeed);
        dataset.addSeries(this.windGust);

        DateAxis domain = new DateAxis("Time");
        NumberAxis range = new NumberAxis(wUnits);
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));

//        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYItemRenderer renderer = new XYAreaRenderer();

        //final XYItemRenderer renderer2 = new XYAreaRenderer();
        renderer.setSeriesPaint(0, Color.blue); /* wind speed */
        renderer.setSeriesPaint(1, Color.red);  /* wind gust */
        
        renderer.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        XYPlot plot = new XYPlot(dataset, domain, range, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        //plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        domain.setAutoRange(true);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);

        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        //String title=new String("Wind Speed over last " + maxAge + " seconds");
        
        JFreeChart chart = new JFreeChart("", new Font("SansSerif", Font.BOLD, 12), plot, false);
        chart.setBackgroundPaint(Color.white);
        
        //setMaximumDrawHeight(double height);
        ChartPanel chartPanel = new ChartPanel(chart, true);
        //chartPanel.setBorder(BorderFactory.createCompoundBorder(
        //		BorderFactory.createEmptyBorder(4, 4, 4, 4),
        //        BorderFactory.createLineBorder(Color.black)));
        //chartPanel.setMaximumDrawHeight(140);
//        chartPanel.setSize(300,100);
        
        chartPanel.setPreferredSize(new Dimension(400, 330));
        //System.err.println("We should be " + width + "x" + height);
        //chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel);

    }
    
    public void addWind(double ws, double wg) {
    	Second s=new Second();
    	addWind(s,ws,wg);
    }
    
    public void addWind(Second s, double ws, double wg) {
    	this.windSpeed.addOrUpdate(s,ws);
    	this.windGust.addOrUpdate(s,wg);
    }

    /* add a data point to the graph */
//	public void addWindSpeed(double ws) {
//		this.windSpeed.addOrUpdate(new Second(), ws);
//	}
	
//	public void addWindGust(double wg) {
//		this.windGust.addOrUpdate(new Second(), wg);	
//	}
    
    
}
