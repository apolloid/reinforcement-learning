package adiputra.reinforcementlearning.chapter5.views;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Example5_3View extends JPanel {
    private final JTextField episodesField = new JTextField("100000");
    private final JTextField gammaField = new JTextField("1");
    private final JButton simulateButton = new JButton("Simulate");
    private final JLabel statusLabel = new JLabel("Status: Ready");
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JButton backButton = new JButton("Back to Main Menu");
    
    public Example5_3View() {
    	setLayout(new BorderLayout(10, 10));
    	setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    	
    	String labelText = "<html><p style='text-align: justify;'>"
                + "<b>Example 5.3: Solving Blackjack</b><br>"
                + "It is straightforward to apply Monte Carlo ES to\r\n"
                + "blackjack. Because the episodes are all simulated games, it is easy to arrange for exploring\r\n"
                + "starts that include all possibilities. In this case one simply picks the dealer’s cards, the\r\n"
                + "player’s sum, and whether or not the player has a usable ace, all at random with equal\r\n"
                + "probability. As the initial policy we use the policy evaluated in the previous blackjack\r\n"
                + "example, that which sticks only on 20 or 21. The initial action-value function can be zero\r\n"
                + "for all state–action pairs. Figure 5.2 shows the optimal policy for blackjack found by\r\n"
                + "Monte Carlo ES. This policy is the same as the “basic” strategy of Thorp (1966) with the\r\n"
                + "sole exception of the leftmost notch in the policy for a usable ace, which is not present\r\n"
                + "in Thorp’s strategy. We are uncertain of the reason for this discrepancy, but confident\r\n"
                + "that what is shown here is indeed the optimal policy for the version of blackjack we have\r\n"
                + "described.</html>";

        JLabel descriptionLabel = new JLabel(labelText);
        add(descriptionLabel, BorderLayout.NORTH);
        
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        controlPanel.add(new JLabel("No. of episodes:"), gbc);
        
        gbc.gridx = 1;
        controlPanel.add(episodesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Gamma (Discount Factor):"), gbc);
        
        gbc.gridx = 1;
        controlPanel.add(gammaField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        controlPanel.add(simulateButton, gbc);

        gbc.gridy = 3;
        controlPanel.add(statusLabel, gbc);
        
        gbc.gridy = 4;
        progressBar.setStringPainted(true);
        controlPanel.add(progressBar, gbc);
        
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        controlPanel.add(new JPanel(), gbc);
        add(controlPanel, BorderLayout.CENTER);
        
        add(backButton, BorderLayout.SOUTH);
    }
    
    public String getEpisodes() {
        return episodesField.getText();
    }

    public void setSimulateButtonEnabled(boolean enabled) {
        simulateButton.setEnabled(enabled);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
    }
    
    public void addSimulateListener(ActionListener listener) {
        simulateButton.addActionListener(listener);
    }
    
    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }
    
    public void showResultCharts(int[][] usableAceGrid, int[][] nonUsableAceGrid, int episodes) {
        createChart(usableAceGrid, "Usable Ace Policy (" + episodes + " samples)");
        createChart(nonUsableAceGrid, "Non Usable Ace Policy (" + episodes + " samples)");
    }
    
    private void createChart(int[][] policy, String title) {
    	XYZDataset dataset = createDataset(policy);
        NumberAxis xAxis = new NumberAxis("Dealer showing");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setLowerBound(0.5);
        xAxis.setUpperBound(10.5);
        
        NumberAxis yAxis = new NumberAxis("Player sum");
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setLowerBound(10.5);
        yAxis.setUpperBound(21.5);
        
        XYBlockRenderer renderer = new XYBlockRenderer();
        PaintScale paintScale = new PaintScale() {
            @Override
            public double getLowerBound() { return 0; }
            @Override
            public double getUpperBound() { return 1; }
            @Override
            public Paint getPaint(double value) {
                if (value == 0.0) {
                    return new java.awt.Color(144, 238, 144);
                } else {
                    return new java.awt.Color(255, 182, 193);
                }
            }
        };

        renderer.setPaintScale(paintScale);
        renderer.setBlockWidth(1.0);
        renderer.setBlockHeight(1.0);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(java.awt.Color.lightGray);

        JFreeChart chart = new JFreeChart(
            title, 
            JFreeChart.DEFAULT_TITLE_FONT, 
            plot, 
            false
        );
        chart.setBackgroundPaint(java.awt.Color.white);

        JFrame frame = new JFrame("Blackjack Policy Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 600)); // Adjust size as needed
        chartPanel.setMouseWheelEnabled(true); // Enable zooming
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }
    
	private XYZDataset createDataset(int[][] policy) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        
        int numPlayerStates = policy.length;
        if (numPlayerStates == 0) {
            return dataset;
        }
        int numDealerStates = policy[0].length;

        double[][] seriesData = new double[3][numPlayerStates * numDealerStates];
        int pointIndex = 0;

        int minPlayerSum = 11;

        for (int i = 0; i < numPlayerStates; i++) {
            for (int j = 0; j < numDealerStates; j++) {
                seriesData[0][pointIndex] = j + 1;  
                seriesData[1][pointIndex] = (numPlayerStates - 1 - i) + minPlayerSum; 
                seriesData[2][pointIndex] = policy[i][j]; 
                
                pointIndex++;
            }
        }
        dataset.addSeries("Policy", seriesData);
        return dataset;
    }
}
