package adiputra.reinforcementlearning.chapter5;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.EmulGLSkin;
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

import adiputra.utils.Contour3DChart;


public class Example5_2 {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Example 5.2");
	    frame.setSize(300, 270);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLayout(null);
	    
	    JLabel episodesLabel = new JLabel("No. of episodes:");
	    episodesLabel.setBounds(10, 0, 100, 30);
	    frame.add(episodesLabel);
	    JTextField episodesField = new JTextField();
	    episodesField.setBounds(10, 30, 100, 30);
	    frame.add(episodesField);
	    
	    JButton simulateButton = new JButton("Simulate");
	    simulateButton.setBounds(10, 80, 100, 30);
	    frame.add(simulateButton);
	    
        JLabel statusLabel = new JLabel("Status: Ready");
        statusLabel.setBounds(10, 130, 260, 30);
        frame.add(statusLabel);
	    
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(10, 160, 260, 30);
        progressBar.setStringPainted(true);
        frame.add(progressBar);
        
        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int episodes = Integer.parseInt(episodesField.getText());
                
                // Disable button during simulation
                simulateButton.setEnabled(false);
                
                // Create a Blackjack instance that can report progress
                Blackjack2 blackjack = new Blackjack2(episodes, progressBar, statusLabel);
                
                // Run policy evaluation in a background thread
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        blackjack.simulate();
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        simulateButton.setEnabled(true);
                        statusLabel.setText("Completed");
                    }
                }.execute();
            }
        });
	    
	    frame.setVisible(true);
	}
}

class StateActionRewards {
	List<Integer> hitRewards;
	List<Integer> stickRewards;
	
	public StateActionRewards() {
		hitRewards = new ArrayList<>();
		stickRewards = new ArrayList<>();
	}
}

class Blackjack2 {
    private final StateActionRewards[][] usableAceStateActionValueFunction = createValueFunctionArray();
	private final StateActionRewards[][] nonUsableAceStateActionValueFunction = createValueFunctionArray();
    private final boolean[][] usableAcePolicy = createPolicyArray();
    private final boolean[][] nonUsableAcePolicy = createPolicyArray();
	
	private int noOfEpisodes;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public Blackjack2(int noOfEpisodes, JProgressBar progressBar, JLabel statusLabel) {
		this.noOfEpisodes = noOfEpisodes;
		this.progressBar = progressBar;
		this.statusLabel = statusLabel;
	}
    
    private StateActionRewards[][] createValueFunctionArray() {
    	StateActionRewards[][] array = new StateActionRewards[11][10];
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 10; j++) {
                array[i][j] = new StateActionRewards();
            }
        }
        return array;
    }
    
    private boolean[][] createPolicyArray() {
    	Random random =  new Random();
    	boolean[][] array = new boolean[11][10];
    	for (int i = 0; i < 11; i++) {
    		for (int j = 0; j < 10; j++) {
    			array[i][j] = random.nextBoolean(); 
    		}
    	}
    	return array;
    }

    public void simulate() {
    	progressBar.setValue(0);

        for(int i=0; i<noOfEpisodes; i++) {
            if (progressBar != null && statusLabel != null) {
            	final int currentEpisode = i + 1;
                final int progress = (i + 1) * 100 / noOfEpisodes;
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(progress);
                    statusLabel.setText("Processing episode: " + currentEpisode + "/" + noOfEpisodes);
                });
            }
            policyEvaluationAndImprovement(
            		usableAceStateActionValueFunction,
            		nonUsableAceStateActionValueFunction,
            		usableAcePolicy,
            		nonUsableAcePolicy);
        }
        
        int[][] convertedNonUsableAcePolicy = new int[11][10];
        for (int i = 0; i < 11; i++) {
        	for (int j = 0; j < 10; j++) {
        		if(nonUsableAcePolicy[i][j])
        			convertedNonUsableAcePolicy[i][j] = 1;
        		else
        			convertedNonUsableAcePolicy[i][j] = 0;
        	}
        }
        
        XYZDataset dataset = createDataset(convertedNonUsableAcePolicy);
        NumberAxis xAxis = new NumberAxis("Dealer's Showing Card (1=Ace, 10=Ten/Face)");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // Show integer ticks
        xAxis.setLowerBound(0.5);   // Centering blocks
        xAxis.setUpperBound(10.5);  // Dealer cards are 1-10
        
        NumberAxis yAxis = new NumberAxis("Player's Sum");
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setLowerBound(10.5);  // Assuming player sums 11-21
        yAxis.setUpperBound(21.5);
        
        // Define renderer and paint scale
        XYBlockRenderer renderer = new XYBlockRenderer();
        PaintScale paintScale = new PaintScale() {
            @Override
            public double getLowerBound() { return 0; } // For STICK
            @Override
            public double getUpperBound() { return 1; } // For HIT
            @Override
            public Paint getPaint(double value) {
                if (value == 0.0) { // STICK (false)
                    return new java.awt.Color(144, 238, 144); // Light Green
                } else { // HIT (true)
                    return new java.awt.Color(255, 182, 193); // Light Red/Pink
                }
            }
        };

        renderer.setPaintScale(paintScale);
        renderer.setBlockWidth(1.0);  // Each dealer card gets a block of width 1
        renderer.setBlockHeight(1.0); // Each player sum gets a block of height 1

        // Create plot
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(java.awt.Color.lightGray); // Background for the plot area

        // Create chart
        JFreeChart chart = new JFreeChart(
            "Blackjack Policy (Usable Ace)", 
            JFreeChart.DEFAULT_TITLE_FONT, 
            plot, 
            false // No legend needed if colors are self-explanatory or titled
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
        
        double[][] nonUsableAceStateValueFunctionGrid = new double[10][10];
        for(int i=0; i<10; i++) {
            for(int j=0; j<10; j++) {
            	if (nonUsableAcePolicy[i][j])
            		nonUsableAceStateValueFunctionGrid[i][j] = calculateAverage(nonUsableAceStateActionValueFunction[i][j].hitRewards);
            	else
            		nonUsableAceStateValueFunctionGrid[i][j] = calculateAverage(nonUsableAceStateActionValueFunction[i][j].stickRewards);
            }
        }
        
        EmulGLChartFactory factoryNonUsableAce = new EmulGLChartFactory();
        
        Quality q = Quality.Advanced(); 
        q.setAnimated(false);
        q.setHiDPIEnabled(true);
        
        Chart chartNonUsableAce = factoryNonUsableAce.newChart(q);
        chartNonUsableAce.add(CustomSurface.generateSurface(nonUsableAceStateValueFunctionGrid));
        EmulGLSkin skinNonUsableAce = EmulGLSkin.on(chartNonUsableAce);
        skinNonUsableAce.getCanvas().setProfileDisplayMethod(true);
        chartNonUsableAce.getAxisLayout().setYAxisLabel("Dealer showing");
        chartNonUsableAce.getAxisLayout().setZAxisLabel("State value function");
        chartNonUsableAce.open("Non usable ace (" + noOfEpisodes + " samples)");
        chartNonUsableAce.addMouse();      
        
        if (progressBar != null && statusLabel != null) {
			SwingUtilities.invokeLater(() -> {
				progressBar.setValue(100);
				statusLabel.setText("Completed");
			});
		}
    }

	private static XYZDataset createDataset(int[][] policyData) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        
        int numPlayerStates = policyData.length;    // Number of rows, e.g., 11
        if (numPlayerStates == 0) {
            return dataset; // Empty dataset if no data
        }
        int numDealerStates = policyData[0].length; // Number of columns, e.g., 10

        double[][] seriesData = new double[3][numPlayerStates * numDealerStates];
        int pointIndex = 0;

        // Y-axis: Player Sum (e.g., 11 to 21)
        // We'll map row index 'i' to player sum.
        // Convention: policyData[0] is for highest player sum (e.g., 21)
        // policyData[numPlayerStates-1] is for lowest player sum (e.g., 11)
        int minPlayerSum = 11; // Assuming player sums represented are 11-21

        for (int i = 0; i < numPlayerStates; i++) { // Player sum index
            for (int j = 0; j < numDealerStates; j++) { // Dealer card index
                
                // X-value: Dealer card (1 for Ace, 2 for Two, ..., 10 for Ten/Face)
                seriesData[0][pointIndex] = j + 1; 
                
                // Y-value: Player sum
                // If i=0 is row for player sum 21, i=1 for 20, ..., i=10 for 11
                seriesData[1][pointIndex] = (numPlayerStates - 1 - i) + minPlayerSum; 
                
                // Z-value: Policy action (0 for STICK, 1 for HIT)
                seriesData[2][pointIndex] = policyData[i][j]; 
                
                pointIndex++;
            }
        }
        dataset.addSeries("Policy", seriesData);
        return dataset;
    }
    
    private static class CustomSurface implements CustomContour3DSurface{
    	private static Shape generateSurface(double[][] grid) {
    		Range xRange = new Range(12, 21);
    		Range yRange = new Range(1, 10);

    		Mapper mapper = new Mapper() {
    			@Override
				public double f(double x, double y) {
					return grid[(int)x-12][(int)y-1];
				}
			};
			
			int xSteps = 10;
			int ySteps = 10;
			float alpha = 0.5f;

			Shape surface = 
				new SurfaceBuilder().orthonormal(new OrthonormalGrid(xRange, xSteps, yRange, ySteps), mapper);
			ColorMapper colorMapper = new ColorMapper(new ColorMapRainbow(), surface, new Color(1, 1, 1, alpha));
		    
			surface.setColorMapper(colorMapper);
		    surface.setFaceDisplayed(true);
		    surface.setWireframeDisplayed(true);
		    surface.setWireframeColor(Color.BLACK);
		    surface.setWireframeWidth(1);
		    
		    return surface;
    	}
    }
    
    public static double calculateAverage(List<Integer> hitRewards) {
        double sum = 0;
        for (int i : hitRewards) {
            sum += i;
        }
        return sum / hitRewards.size();
    }

    public void policyEvaluationAndImprovement (
    		StateActionRewards[][] usableAceStateActionValueFunction,
    		StateActionRewards[][] nonUsableAceStateActionValueFunction,
    		boolean[][] usableAcePolicy,
    		boolean[][] nonUsableAcePolicy) {
        GameResult gameResult = simulateBlackjackGame(usableAcePolicy, nonUsableAcePolicy);

        ArrayList<Integer> initialPlayerCards = gameResult.playerHand.getInitialCards();
        ArrayList<Integer> addedPlayerCards = gameResult.playerHand.getExtraCards();
        boolean usableAce = gameResult.playerHand.isUsableAce();
        int playerSum = sumHand(initialPlayerCards, usableAce) + sumHand(addedPlayerCards, false);
        int convertedAceIndex = gameResult.playerHand.getConvertedAceIndex();
        int dealerFaceUpCard = gameResult.dealerFaceUpCard;
        int G = gameResult.reward;
        int gamma = 1;

        if (!gameResult.firstRandomAction) {
        	if (usableAce) {
        		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
        		policyImprovement(usableAcePolicy, usableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
        	}
        	else {
        		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
        		policyImprovement(nonUsableAcePolicy, nonUsableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
        	}
        }
        else {
        	if (playerSum <= 21) {
	            if (usableAce) {
	            	if (usableAcePolicy[playerSum-11][dealerFaceUpCard-1])
	            		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.add(G);
	            	else
	            		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
	            	policyImprovement(usableAcePolicy, usableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
	            } else {
	            	if (nonUsableAcePolicy[playerSum-11][dealerFaceUpCard-1])
	            		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.add(G);
	            	else
	            		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
	            	policyImprovement(nonUsableAcePolicy, nonUsableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
	            }
        	}

            for(int i=(addedPlayerCards.size()-1); i>=0; i--) {
                G = G * gamma;
                playerSum -= addedPlayerCards.get(i);

                if ((convertedAceIndex != -1) && (i == convertedAceIndex)) {
                    usableAce = true;
                    playerSum += 10;
                }

                if (i == 0) {
    	            if (usableAce) {
    	            	if (gameResult.firstRandomAction)
    	            		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.add(G);
    	            	else
    	            		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
    	            	policyImprovement(usableAcePolicy, usableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
    	            } else {
    	            	if (gameResult.firstRandomAction)
    	            		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.add(G);
    	            	else
    	            		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
    	            	policyImprovement(nonUsableAcePolicy, nonUsableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
    	            }
                }
                else {
    	            if (usableAce) {
    	            	if (usableAcePolicy[playerSum-11][dealerFaceUpCard-1])
    	            		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.add(G);
    	            	else
    	            		usableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
    	            	policyImprovement(usableAcePolicy, usableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
    	            } else {
    	            	if (nonUsableAcePolicy[playerSum-11][dealerFaceUpCard-1])
    	            		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.add(G);
    	            	else
    	            		nonUsableAceStateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.add(G);
    	            	policyImprovement(nonUsableAcePolicy, nonUsableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
    	            }
                }
            }   
        }
    }
    
    public void policyImprovement(boolean[][] policy, StateActionRewards[][] stateActionValueFunction, int playerSum, int dealerFaceUpCard) {
    	if (stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.isEmpty()) {
    		policy[playerSum-11][dealerFaceUpCard-1] = false;
    	}
    	else if (stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.isEmpty()) {
    		policy[playerSum-11][dealerFaceUpCard-1] = true;
    	}
    	else {
    		double avgHitRewards = getAvgRewards(stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards);
    		double avgStickRewards =  getAvgRewards(stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards);
    		
    		if (avgHitRewards > avgStickRewards) {
    			policy[playerSum-11][dealerFaceUpCard-1] = true;
    		}
    		else if (avgHitRewards < avgStickRewards) {
    			policy[playerSum-11][dealerFaceUpCard-1] = false;
    		}
    	}
    }
    
    public double getAvgRewards (List<Integer> rewards) {
    	double sumRewards = 0;
    	for (int reward : rewards) {
    		sumRewards += reward;
    	}
    	
    	return sumRewards / rewards.size();
    }

    public record GameResult(int reward, Hand playerHand, int dealerFaceUpCard, boolean firstRandomAction) {}

    public static GameResult simulateBlackjackGame(boolean[][] usableAcePolicy, boolean[][] nonUsableAcePolicy) {
        Hand playerHand = setupInitialCards();
        Hand dealerHand = setupInitialCards();
        int dealerFaceUpCard = dealerHand.getInitialCards().get(0);
        
        Random random =  new Random();
        boolean firstRandomAction = random.nextBoolean();
        
        int playerSum = playerPlaying(playerHand, usableAcePolicy, nonUsableAcePolicy, dealerFaceUpCard, firstRandomAction);
        int dealerSum = dealerPlaying(dealerHand);
        int reward = calculateReward(playerSum, dealerSum);
                
        return new GameResult(reward, playerHand, dealerFaceUpCard, firstRandomAction);
    }

    public static int drawCard() {
        // Method to draw a random card with a values [1,2,3,...,10,10,10,10]
        int card = (int) (Math.random() * 13) + 1;
        if (card > 10) {
            return 10;
        } else {
            return card;
        }
    }

    public static int sumHand(ArrayList<Integer> hand, boolean usable_ace) {
        // Method to calculate the sum of card values in the hand
        int sum = 0;
        for (Integer card : hand) {
            sum += card;
        }
        if (usable_ace) {
            return sum + 10;
        } else {
            return sum;
        }
    }

    public static Hand setupInitialCards() {
    	Hand hand = new Hand();
    	
    	hand.addToInitialCards(drawCard());
    	hand.addToInitialCards(drawCard());

        if (hand.getInitialCards().contains(1)) {
        	hand.setUsableAce(true);
        }
        
        while (sumHand(hand.getInitialCards(), hand.isUsableAce()) < 11) {
        	int addedCard = hand.addToInitialCards(drawCard());
            if (!hand.isUsableAce() && addedCard == 1 && sumHand(hand.getInitialCards(), true) <= 21) {
            	hand.setUsableAce(true);
            }
        }
        return hand;
    }

    public static int playerPlaying(Hand playerHand, boolean[][] usableAcePolicy, boolean[][] nonUsableAcePolicy, int dealerFaceUpCard, boolean firstRandomAction) {
        int playerInitialHandSum = sumHand(playerHand.getInitialCards(), playerHand.isUsableAce());
                
    	if (firstRandomAction) {
    		int addedCard = playerHand.addToExtraCards(drawCard());
    		
            if ((playerInitialHandSum + addedCard) > 21) {
            	if (playerHand.isUsableAce()) {
                    playerInitialHandSum -= 10;
                	playerHand.setUsableAce(false);
                    playerHand.setConvertedAceIndex(playerHand.getExtraCards().size() - 1);
            	}
            	else {
            		return (playerInitialHandSum + addedCard);
            	}
            }
    	}
    	else {
    		return playerInitialHandSum;
    	}
    	
        while (true) {
        	if (playerHand.isUsableAce()) {
        		if (usableAcePolicy[(playerInitialHandSum + sumHand(playerHand.getExtraCards(), false)) - 11][dealerFaceUpCard - 1]) {
        			playerHand.addToExtraCards(drawCard());
        		}
        		else {
        			break;
        		}
        	}
        	else {
        		if (nonUsableAcePolicy[(playerInitialHandSum + sumHand(playerHand.getExtraCards(), false)) - 11][dealerFaceUpCard - 1]) {
        			playerHand.addToExtraCards(drawCard());
        		}
        		else {
        			break;
        		}
        	}
        	
            if ((playerInitialHandSum + sumHand(playerHand.getExtraCards(), false)) > 21) {
            	if (playerHand.isUsableAce()) {
	                playerInitialHandSum -= 10;
	            	playerHand.setUsableAce(false);
	                playerHand.setConvertedAceIndex(playerHand.getExtraCards().size() - 1);
            	}
            	else {
            		break;
            	}
            }
        }
        
        return (playerInitialHandSum + sumHand(playerHand.getExtraCards(), false));
    }

    public static int dealerPlaying(Hand dealerHand) {
        int dealerInitialHandSum = sumHand(dealerHand.getInitialCards(), dealerHand.isUsableAce());
        while (dealerInitialHandSum + sumHand(dealerHand.getExtraCards(), false) < 17) {
            dealerHand.addToExtraCards(drawCard());
            if ((dealerInitialHandSum + sumHand(dealerHand.getExtraCards(), false)) > 21 && dealerHand.isUsableAce()) {
            	dealerInitialHandSum -= 10;
            	dealerHand.setUsableAce(false);
            }
        }
        return (dealerInitialHandSum + sumHand(dealerHand.getExtraCards(), false));
    }

    public static int calculateReward(int player_sum, int dealer_sum) {
        if (player_sum > 21)
        {
            return -1;
        }
        else if (dealer_sum > 21)
        {
            return 1;
        }
        else
        {
        	return Integer.compare(player_sum, dealer_sum);
        }
    }

    public static class Hand {
        private ArrayList<Integer> initialCards = new ArrayList<Integer>();
        private ArrayList<Integer> extraCards =  new ArrayList<Integer>();
        private boolean usableAce = false;
        private int convertedAceIndex = -1;

        public ArrayList<Integer> getInitialCards() {
            return initialCards;
        }
        
        public ArrayList<Integer> getExtraCards() {
            return extraCards;
        }

        public boolean isUsableAce() {
            return usableAce;
        }

        public int getConvertedAceIndex() {
            return convertedAceIndex;
        }
        
        public int addToInitialCards(Integer card) {
        	this.initialCards.add(card);
        	return card;
        }
        
        public int addToExtraCards(int card) {
        	this.extraCards.add(card);
        	return card;
        }
        
        public void setUsableAce(boolean usableAce) {
        	this.usableAce = usableAce;
        }

        public void setConvertedAceIndex(int convertedAceIndex) {
        	this.convertedAceIndex = convertedAceIndex;
        }
    }
}