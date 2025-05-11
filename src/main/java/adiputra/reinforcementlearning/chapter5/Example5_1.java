package adiputra.reinforcementlearning.chapter5;

import javax.swing.*;
import java.awt.*;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.EmulGLSkin;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Geometry;
import org.jzy3d.plot3d.primitives.SampleGeom;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Example5_1 {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Example 5.1");
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
                Blackjack blackjack = new Blackjack(episodes, progressBar, statusLabel);
                
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

class Blackjack {
    private final ArrayList<Integer>[][] usableAceStateValueFunction = createValueFunctionArray();
	private final ArrayList<Integer>[][] nonUsableAceStateValueFunction = createValueFunctionArray();
    private int noOfEpisodes;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public Blackjack(int noOfEpisodes, JProgressBar progressBar, JLabel statusLabel) {
		this.noOfEpisodes = noOfEpisodes;
		this.progressBar = progressBar;
		this.statusLabel = statusLabel;
	}
    
    @SuppressWarnings("unchecked")
    private static ArrayList<Integer>[][] createValueFunctionArray() {
        ArrayList<Integer>[][] array = (ArrayList<Integer>[][]) new ArrayList[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                array[i][j] = new ArrayList<>();
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
            policyEvaluation(usableAceStateValueFunction, nonUsableAceStateValueFunction);
        }
        // convert the usableAceStateValueFunction element of ArrayList into 2D grid with average value
        double[][] usableAceStateValueFunctionGrid = new double[10][10];
        for(int i=0; i<10; i++) {
            for(int j=0; j<10; j++) {
                usableAceStateValueFunctionGrid[i][j] = calculateAverage(usableAceStateValueFunction[i][j]);
            }
        }
        // convert the nonUsableAceStateValueFunction element of ArrayList into 2D grid with average value
        double[][] nonUsableAceStateValueFunctionGrid = new double[10][10];
        for(int i=0; i<10; i++) {
            for(int j=0; j<10; j++) {
                nonUsableAceStateValueFunctionGrid[i][j] = calculateAverage(nonUsableAceStateValueFunction[i][j]);
            }
        }
        System.out.println("Usable Ace State Value Function");
        for(int i=0; i<10; i++) {
            for(int j=0; j<10; j++) {
                System.out.print(usableAceStateValueFunctionGrid[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("Non-Usable Ace State Value Function");
        for(int i=0; i<10; i++) {
            for(int j=0; j<10; j++) {
                System.out.print(nonUsableAceStateValueFunctionGrid[i][j] + " ");
            }
            System.out.println();
        }
        
        // Plot 3D contour map of the value functions
        EmulGLChartFactory factory = new EmulGLChartFactory();

        Quality q = Quality.Advanced(); 
        q.setAnimated(false);
        q.setHiDPIEnabled(true);
        
        Chart chartUsableAce = factory.newChart(q);
        chartUsableAce.add(CustomSurface.surface(usableAceStateValueFunctionGrid));
        EmulGLSkin skinUsableAce = EmulGLSkin.on(chartUsableAce);
        skinUsableAce.getCanvas().setProfileDisplayMethod(true);
        chartUsableAce.open();
        chartUsableAce.addMouse();
  
        if (progressBar != null && statusLabel != null) {
			SwingUtilities.invokeLater(() -> {
				progressBar.setValue(100);
				statusLabel.setText("Completed");
			});
		}
    }
    
    private static class CustomSurface {
    	private static Shape surface(double[][] grid) {
    		Range xRange = new Range(12, 21);
    		Range yRange = new Range(1, 10);

    		Mapper mapper = new Mapper() {
    			@Override
				public double f(double x, double y) {
					return grid[(int)x-12][(int)y-1];
				}
			};
			
			int xSteps = 9;
			int ySteps = 9;
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
    
    public static double calculateAverage(ArrayList<Integer> list) {
        double sum = 0;
        for (int i : list) {
            sum += i;
        }
        return sum / list.size();
    }

    public static void policyEvaluation (ArrayList<Integer>[][] usableAceStateValueFunction, ArrayList<Integer>[][] nonUsableAceStateValueFunction) {
        GameResult gameResult = simulateBlackjackGame();

        int[] initialPlayerHand = gameResult.playerHand.getInitialHand();
        int[] addedPlayerCards = gameResult.playerHand.getAddedCards();
        boolean usableAce = gameResult.playerHand.isUsableAce();
        int playerSum = sumHand(initialPlayerHand, usableAce) + sumHand(addedPlayerCards, false);

        int dealerFaceUpCard = gameResult.dealerFaceUpCard;
        int G = gameResult.reward;
        int gamma = 1;

        if (playerSum >= 12 && playerSum <= 21) {
            if (usableAce) {
                usableAceStateValueFunction[playerSum-12][dealerFaceUpCard-1].add(G);
            } else {
                nonUsableAceStateValueFunction[playerSum-12][dealerFaceUpCard-1].add(G);
            }
        }

        for(int i=(addedPlayerCards.length-1); i>=0; i--) {
            G = G * gamma;
            playerSum -= addedPlayerCards[i];

            if (gameResult.playerHand.getConvertedAceIndex() != -1 && i == gameResult.playerHand.getConvertedAceIndex()) {
                usableAce = true;
                playerSum += 10;
            }

            if (usableAce) {
                usableAceStateValueFunction[playerSum-12][dealerFaceUpCard-1].add(G);
            } else {
                nonUsableAceStateValueFunction[playerSum-12][dealerFaceUpCard-1].add(G);
            }
        }
    }


    public record GameResult(int reward, Hand playerHand, int dealerFaceUpCard) {}

    public static GameResult simulateBlackjackGame() {
        Hand playerHand = setupInitialCards();
        Hand dealerHand = setupInitialCards();
        int dealerFaceUpCard = dealerHand.getInitialHand()[0];

        playerHand.setAddedCards(playerPlaying(playerHand));
        int playerSum = sumHand(playerHand.getInitialHand(), playerHand.isUsableAce()) 
                    + sumHand(playerHand.getAddedCards(), false);

        int dealerSum = dealerPlaying(dealerHand);
        int reward = calculateReward(playerSum, dealerSum);

        return new GameResult(reward, playerHand, dealerFaceUpCard);
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

    public static int randomAction() {
        // Method to randomly select an action (0 for stick, 1 for hit)
        return (int) (Math.random() * 2);
    }

    public static int sumHand(int[] hand, boolean usable_ace) {
        // Method to calculate the sum of card values in the hand
        int sum = 0;
        for (int i = 0; i < hand.length; i++) {
            sum += hand[i];
        }
        if (usable_ace) {
            return sum + 10;
        } else {
            return sum;
        }
    }

    public static int[] addCard(int[] hand) {
        // Method to add a new card to the hand
        int[] new_hand = new int[hand.length + 1];

        // Copy existing cards to the new hand array
        System.arraycopy(hand, 0, new_hand, 0, hand.length);

        // Draw a new card and add it to the end of the new hand array
        new_hand[hand.length] = drawCard();
        return new_hand;
    }

    public static Hand setupInitialCards() {
    	Hand hand = new Hand();
    	
    	hand.setInitialHand(addCard(hand.getInitialHand()));
    	hand.setInitialHand(addCard(hand.getInitialHand()));

        if (hand.getInitialHand()[0] == 1 || hand.getInitialHand()[1] == 1) {
        	hand.setUsableAce(true);
        }
        
        while (sumHand(hand.getInitialHand(), hand.isUsableAce()) < 12) {
        	hand.setInitialHand(addCard(hand.getInitialHand()));
            if (!hand.isUsableAce() && hand.getInitialHand()[hand.getInitialHand().length - 1] == 1 && sumHand(hand.getInitialHand(), true) <= 21) {
            	hand.setUsableAce(true);
            }
        }
        return hand;
    }

    public static int[] playerPlaying(Hand playerHand) {
        int playerInitialHandSum = sumHand(playerHand.getInitialHand(), playerHand.isUsableAce());
        int[] addedCards = new int[0];

        while ((playerInitialHandSum + sumHand(addedCards, false)) < 20) {
            addedCards = addCard(addedCards);

            if ((playerInitialHandSum + sumHand(addedCards, false)) > 21 && playerHand.isUsableAce()) {
                playerInitialHandSum -= 10;
            	playerHand.setUsableAce(false);
                playerHand.setConvertedAceIndex(addedCards.length - 1);
            }
        }
        return addedCards;
    }

    public static int dealerPlaying(Hand dealerHand) {
        int dealerInitialHandSum = sumHand(dealerHand.getInitialHand(), dealerHand.isUsableAce());
        int[] addedCards = new int[0];
        while (dealerInitialHandSum + sumHand(addedCards, false) < 17) {
            addedCards = addCard(addedCards);
            if ((dealerInitialHandSum + sumHand(addedCards, false)) > 21 && dealerHand.isUsableAce()) {
            	dealerInitialHandSum -= 10;
            	dealerHand.setUsableAce(false);
            }
        }
        return (dealerInitialHandSum + sumHand(addedCards, false));
    }

    public static int calculateReward(int player_sum, int dealer_sum) {
        if (player_sum > 21) {
            return -1;
        }

        if (dealer_sum > 21) {
            return 1;
        }

        return Integer.compare(player_sum, dealer_sum);
    }

    public static class Hand {
        private int[] initialHand;
        private int[] addedCards;
        private boolean usableAce;
        private int convertedAceIndex = -1;
        
        public Hand() {
        	this.initialHand = new int[0];
        	this.addedCards = new int[0];
        	this.usableAce = false;
        }

        public int[] getInitialHand() {
            return initialHand;
        }
        
        public int[] getAddedCards() {
            return addedCards;
        }

        public boolean isUsableAce() {
            return usableAce;
        }

        public int getConvertedAceIndex() {
            return convertedAceIndex;
        }
        
        public void setInitialHand(int[] initialHand) {
        	this.initialHand = initialHand;
        }
        
        public void setAddedCards(int[] addedCards) {
        	this.addedCards = addedCards;
        }
        
        public void setUsableAce(boolean usableAce) {
        	this.usableAce = usableAce;
        }

        public void setConvertedAceIndex(int convertedAceIndex) {
        	this.convertedAceIndex = convertedAceIndex;
        }
    }
}