package adiputra.reinforcementlearning.chapter5.views;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.*;


/**
 * The View component for Example 5.1, following the Model-View-Controller (MVC) pattern.
 * This class is responsible for all user interface elements. It displays the controls for the simulation,
 * shows the current status and progress, and visualizes the final results using the Jzy3d library
 * to create 3D surface plots of the state-value function.
 *
 * @see adiputra.reinforcementlearning.chapter5.controllers.Example5_1Controller
 * @see adiputra.reinforcementlearning.chapter5.models.Example5_1Model
 */
public class Example5_1View extends JPanel {
	/** Text field for user to input the number of simulation episodes. */
    private final JTextField episodesField = new JTextField("100000");
    /** Button to start the simulation. */
    private final JButton simulateButton = new JButton("Simulate");
    /** Label to display status messages to the user. */
    private final JLabel statusLabel = new JLabel("Status: Ready");
    /** Progress bar to show the simulation's progress. */
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    /** Button to navigate back to the main menu. */
    private final JButton backButton = new JButton("Back to Main Menu");
    
    /**
     * Constructs the view panel, initializing and arranging all UI components.
     */
    public Example5_1View() {
    	setLayout(new BorderLayout(10, 10));
    	setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    	
    	String labelText = "<html><p style='text-align: justify;'>"
                + "<b>Example 5.1: Blackjack</b><br>"
                + "The object of the popular casino card game of blackjack is to\r\n"
                + "obtain cards the sum of whose numerical values is as great as possible without exceeding\r\n"
                + "21. All face cards count as 10, and an ace can count either as 1 or as 11. We consider\r\n"
                + "the version in which each player competes independently against the dealer. The game\r\n"
                + "begins with two cards dealt to both dealer and player. One of the dealer’s cards is face\r\n"
                + "up and the other is face down. If the player has 21 immediately (an ace and a 10-card),\r\n"
                + "it is called a natural. He then wins unless the dealer also has a natural, in which case the\r\n"
                + "game is a draw. If the player does not have a natural, then he can request additional\r\n"
                + "cards, one by one (hits), until he either stops (sticks) or exceeds 21 (goes bust). If he goes\r\n"
                + "bust, he loses; if he sticks, then it becomes the dealer’s turn. The dealer hits or sticks\r\n"
                + "according to a fixed strategy without choice: he sticks on any sum of 17 or greater, and\r\n"
                + "hits otherwise. If the dealer goes bust, then the player wins; otherwise, the outcome—win,\r\n"
                + "lose, or draw—is determined by whose final sum is closer to 21."
                + "</p></html>";

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
        gbc.gridwidth = 2;
        controlPanel.add(simulateButton, gbc);

        gbc.gridy = 2;
        controlPanel.add(statusLabel, gbc);
        
        gbc.gridy = 3;
        progressBar.setStringPainted(true);
        controlPanel.add(progressBar, gbc);
        
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        controlPanel.add(new JPanel(), gbc);
        add(controlPanel, BorderLayout.CENTER);
        
        add(backButton, BorderLayout.SOUTH);
    }
    
    /**
     * Gets the number of episodes entered by the user.
     * @return The number of episodes as a String.
     */
    public String getEpisodes() {
        return episodesField.getText();
    }

    /**
     * Sets the enabled state of the simulate button.
     * @param enabled true to enable the button, false to disable it.
     */
    public void setSimulateButtonEnabled(boolean enabled) {
        simulateButton.setEnabled(enabled);
    }

    /**
     * Sets the text of the status label.
     * @param text The status message to display.
     */
    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    /**
     * Sets the current value of the progress bar.
     * @param value The progress value, typically between 0 and 100.
     */
    public void setProgress(int value) {
        progressBar.setValue(value);
    }
    
    /**
     * Adds an ActionListener to the simulate button.
     * @param listener The ActionListener to be notified on button click.
     */
    public void addSimulateListener(ActionListener listener) {
        simulateButton.addActionListener(listener);
    }
    
    /**
     * Adds an ActionListener to the back button.
     * @param listener The ActionListener to be notified on button click.
     */
    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    /**
     * Creates and displays two 3D charts for the simulation results.
     * One chart shows the state-value function with a usable ace, and the other without.
     * @param usableAceGrid The 2D array of state-values for the usable ace case.
     * @param nonUsableAceGrid The 2D array of state-values for the non-usable ace case.
     * @param episodes The number of episodes used to generate the results.
     */
    public void showResultCharts(double[][] usableAceGrid, double[][] nonUsableAceGrid, int episodes) {
        Chart chartUsableAce = createChart(usableAceGrid);
        chartUsableAce.open("Usable ace (" + episodes + " samples)");

        Chart chartNonUsableAce = createChart(nonUsableAceGrid);
        chartNonUsableAce.open("Non usable ace (" + episodes + " samples)");
    }

    /**
     * Creates a single Jzy3d chart from the given grid data.
     * @param gridData The 2D array of data to be plotted.
     * @return A configured {@link Chart} object.
     */
    private Chart createChart(double[][] gridData) {
        EmulGLChartFactory factory = new EmulGLChartFactory();
        Quality q = Quality.Advanced();
        q.setHiDPIEnabled(true);

        Chart chart = factory.newChart(q);
        chart.add(createSurface(gridData));
        chart.getAxisLayout().setXAxisLabel("Player sum");
        chart.getAxisLayout().setYAxisLabel("Dealer showing");
        chart.getAxisLayout().setZAxisLabel("State value function");
        chart.addMouse();

        return chart;
    }

    /**
     * Creates a 3D surface {@link Shape} for a chart from a 2D grid of values.
     * @param grid The 2D array of z-values for the surface.
     * @return The constructed 3D surface shape.
     */
    private Shape createSurface(double[][] grid) {
        Range xRange = new Range(12, 21);
 		Range yRange = new Range(1, 10);
        Mapper mapper = new Mapper() {
			@Override
			public double f(double x, double y) {
				return grid[(int)x-12][(int)y-1];
			}
		};
        Shape surface = new SurfaceBuilder().orthonormal(new OrthonormalGrid(xRange, 10, yRange, 10), mapper);
        
		ColorMapper colorMapper = new ColorMapper(new ColorMapRainbow(), surface, new Color(1, 1, 1, 0.5f));
		surface.setColorMapper(colorMapper);
	    
		surface.setFaceDisplayed(true);
	    surface.setWireframeDisplayed(true);
	    surface.setWireframeColor(Color.BLACK);
	    surface.setWireframeWidth(1);
        
	    return surface;
    }
}