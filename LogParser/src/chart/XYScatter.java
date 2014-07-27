package chart;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;

import model.JaccardSummary;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.google.common.collect.Multimap;

import db.JaccardSummaryReader;
import execute.NLPSRunner;

public class XYScatter extends ApplicationFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	Map<String, Integer> authorToSeriesID;
	Map<String, XYSeries> authorToSeries;
	Set<String> hidden;
	Multimap<String, JaccardSummary> authorSummary;
	private XYPlot plot;
	private XYSeriesCollection dataset;  
	private String eventDate;

	public XYScatter(String title) throws ParseException {
		super(title);
		eventDate = "2014-02-16 16:23:11 -0400";
		authorSummary = (new JaccardSummaryReader()).getJaccardSummaryForEventDate(NLPSRunner.DATEFORMAT.parse(eventDate));
		
		// Remove some authors with only 1 value
		authorSummary.removeAll("octavio_pinto");
		authorSummary.removeAll("abraham_shenker");
		
		// Start with all authors hidden
		hidden = new HashSet<>();
		hidden.addAll(authorSummary.keySet());
		populateDataset(); 
		JFreeChart chart = createChart();
		plot = chart.getXYPlot();

		final JPanel content = new JPanel(new BorderLayout());
		final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1024, 768));
        content.add(chartPanel);
        
        final JPanel buttonPanel = new JPanel();
        GridLayout gridLayoutForButtons = new GridLayout(0, 7);
        buttonPanel.setLayout(gridLayoutForButtons);
        
        for(String author: authorSummary.keySet()) {
        	final JButton button = new JButton("Toggle " + author);
        	button.setActionCommand(author);
        	button.addActionListener(this);
        	buttonPanel.add(button);
        }
                
        content.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(content);
	}
	
    private JFreeChart createChart() {
    	JFreeChart chart = ChartFactory.createXYLineChart(eventDate, "Period", "Jaccard", dataset, PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}

	private void populateDataset() throws ParseException {
		dataset = new XYSeriesCollection(); 

    	for(String author: authorSummary.keySet()) {
    		if (hidden.contains(author)) continue;
       		    		
        	final XYSeries series = new XYSeries(author);
        	
        	for(JaccardSummary js: authorSummary.get(author)) {
        		// Show Jaccard values when they are 1?
        		//if(js.jaccard == 1) continue;
        		
        		series.add(js.period, js.jaccard);
        		System.out.println(author + ": " + js.period + ", [intersect: " + js.intersect + ", union: " + js.union + ", index: " + js.jaccard + "]");	
        	}
        	dataset.addSeries(series);
    	}
    	
	}

	public static void main(final String[] args) throws ParseException {

        final XYScatter demo = new XYScatter("XY Series");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(hidden.contains(e.getActionCommand())) {
			hidden.remove(e.getActionCommand());
		} else {
			hidden.add(e.getActionCommand());
		}
		
		try {
			populateDataset();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		this.plot.setDataset(dataset);
		this.plot.setRenderer(new StandardXYItemRenderer());
		
	}

}
