package adiputra.reinforcementlearning.chapter5.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;


/**
 * The Model for Example 5.1, implementing the logic for a Monte Carlo simulation of Blackjack.
 * This class follows the Model-View-Controller (MVC) pattern and contains all the data and business logic
 * for the reinforcement learning problem.
 *
 * <p>Specifically, it implements a first-visit Monte Carlo prediction algorithm to estimate the
 * state-value function ($V(s)$) for a fixed Blackjack policy (player sticks on 20 or 21). The state is defined by
 * the player's current sum, the dealer's showing card, and whether the player has a usable ace.
 *
 * <p>This class is observable; it fires property change events to notify listeners (e.g., a Controller)
 * about the simulation's progress, status, and final results.
 *
 * @see adiputra.reinforcementlearning.chapter5.controllers.Example5_1Controller
 */
public class Example5_1Model {
	/** Property name for progress updates (an integer from 0 to 100). */
    public static final String PROGRESS_PROPERTY = "progress";
    /** Property name for status message updates (a String). */
    public static final String STATUS_PROPERTY = "status";
    /** Property name for the final simulation result (a SimulationResult object). */
    public static final String RESULT_PROPERTY = "result";

    // Stores the returns for each state (Player Sum 12-21, Dealer Card 1-10) with a usable ace.
    private final StateRewards[][] usableAceStateValueFunction;
    // Stores the returns for each state (Player Sum 12-21, Dealer Card 1-10) without a usable ace.
    private final StateRewards[][] nonUsableAceStateValueFunction;
    private final PropertyChangeSupport support;

    /**
     * Constructs the model and initializes the data structures for the state-value functions
     * and the mechanism for property change notifications.
     */
    public Example5_1Model() {
        this.usableAceStateValueFunction = createValueFunctionArray();
        this.nonUsableAceStateValueFunction = createValueFunctionArray();
        this.support = new PropertyChangeSupport(this);
    }

    /**
     * Adds a {@link PropertyChangeListener} to the listener list.
     * @param pcl The listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Removes a {@link PropertyChangeListener} from the listener list.
     * @param pcl The listener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Runs the main Monte Carlo simulation for a given number of episodes.
     * It clears previous results, simulates Blackjack games, performs policy evaluation for each game,
     * and fires events to report progress and the final computed state-value grids.
     *
     * @param noOfEpisodes The total number of Blackjack games to simulate.
     */
    public void runSimulation(final int noOfEpisodes) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                usableAceStateValueFunction[i][j].clear();
                nonUsableAceStateValueFunction[i][j].clear();
            }
        }

        for (int i = 0; i < noOfEpisodes; i++) {
            GameResult gameResult = simulateBlackjackGame();
            policyEvaluation(gameResult);

            int progress = (i + 1) * 100 / noOfEpisodes;
            support.firePropertyChange(PROGRESS_PROPERTY, null, progress);
            support.firePropertyChange(STATUS_PROPERTY, null, "Processing episode: " + (i + 1) + "/" + noOfEpisodes);
        }

        double[][] usableAceGrid = convertToGrid(usableAceStateValueFunction);
        double[][] nonUsableAceGrid = convertToGrid(nonUsableAceStateValueFunction);

        SimulationResult result = new SimulationResult(usableAceGrid, nonUsableAceGrid);
        support.firePropertyChange(RESULT_PROPERTY, null, result);
    }

    /**
     * Converts a value function array (containing {@link StateRewards} objects) into a 2D grid of average values.
     * @param valueFunction The 2D array of {@link StateRewards} objects.
     * @return A 2D double array representing the estimated state-value function.
     */
    private double[][] convertToGrid(StateRewards[][] valueFunction) {
        double[][] grid = new double[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                grid[i][j] = calculateAverage(valueFunction[i][j]);
            }
        }
        return grid;
    }

    /**
     * Creates and initializes a 10x10 2D array of {@link StateRewards} objects.
     * @return The initialized 2D array.
     */
    private StateRewards[][] createValueFunctionArray() {
    	StateRewards[][] array = new StateRewards[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                array[i][j] = new StateRewards();
            }
        }
        return array;
    }
    
    /**
     * Calculates the average of the rewards stored in a {@link StateRewards} object.
     * @param stateRewards The object containing the list of rewards.
     * @return The average, or 0 if the list is null or empty.
     */
    private double calculateAverage(StateRewards list) {
        if (list == null || list.rewards.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (int i : list.rewards) {
            sum += i;
        }
        return sum / list.rewards.size();
    }

    /**
     * Simulates one full game of Blackjack, from dealing cards to determining the reward.
     * @return A {@link GameResult} object containing the outcome of the game.
     */
    private GameResult simulateBlackjackGame() {
        Hand playerHand = setupInitialCards();
        Hand dealerHand = setupInitialCards();
        int dealerFaceUpCard = dealerHand.getInitialCards().get(0);

        int playerSum = playerPlaying(playerHand);
        int dealerSum = dealerPlaying(dealerHand);
        int reward = calculateReward(playerSum, dealerSum);
        
        return new GameResult(reward, playerHand, dealerFaceUpCard);
    }
    
    /**
     * Draws a single card, assuming an infinite deck.
     * Card values are 1 (Ace) through 10 (10, Jack, Queen, King).
     * @return The value of the drawn card.
     */
    private int drawCard() {
        int card = (int) (Math.random() * 13) + 1;
        if (card > 10) {
            return 10;
        } else {
            return card;
        }
    }
    
    /**
     * Calculates the sum of a list of cards.
     * @param hand The list of cards.
     * @param usable_ace True if an Ace in the hand should be counted as 11, false if it should be 1.
     * @return The total sum of the hand.
     */
    private int sumHand(ArrayList<Integer> hand, boolean usable_ace) {
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
    
    /**
     * Simulates the player's turn based on a fixed policy: hit until the sum is 20 or 21, then stick.
     * @param playerHand The player's hand.
     * @return The final sum of the player's hand.
     */
    private int playerPlaying(Hand playerHand) {
    	while (true) {
        	int currentSum = sumHand(playerHand.getInitialCards(), playerHand.isUsableAce()) + 
            		sumHand(playerHand.getExtraCards(), false);
        	
        	if (currentSum > 21) {
                if (playerHand.isUsableAce()) {
                	playerHand.setUsableAce(false);
                	playerHand.setConvertedAceIndex(playerHand.getExtraCards().size() - 1); 
                    continue; 
                } else {
                    return currentSum;
                }
        	} else if (currentSum < 20) {
        		playerHand.addToExtraCards(drawCard());
        	} else {
        		return currentSum;
        	}
    	}
    }
    
    /**
     * Simulates the dealer's turn based on a fixed policy: hit until the sum is 17 or greater, then stick.
     * @param dealerHand The dealer's hand.
     * @return The final sum of the dealer's hand.
     */
    private int dealerPlaying(Hand dealerHand) {
        while (true) {
        	int currentSum = sumHand(dealerHand.getInitialCards(), dealerHand.isUsableAce()) + 
            		sumHand(dealerHand.getExtraCards(), false);
        	
        	if (currentSum > 21) {
                if (dealerHand.isUsableAce()) {
                	dealerHand.setUsableAce(false);
                	dealerHand.setConvertedAceIndex(dealerHand.getExtraCards().size() - 1); 
                    continue; 
                } else {
                    return currentSum;
                }
        	} else if (currentSum < 17) {
        		dealerHand.addToExtraCards(drawCard());
        	} else {
        		return currentSum;
        	}
        }
    }
    
    /**
     * Calculates the reward for the game based on player and dealer sums.
     * @param player_sum The final sum of the player's hand.
     * @param dealer_sum The final sum of the dealer's hand.
     * @return 1 for a win, -1 for a loss, 0 for a draw.
     */
    private int calculateReward(int player_sum, int dealer_sum) {
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
    
    /**
     * Sets up an initial two-card hand for a player or dealer, ensuring the sum is at least 12.
     * This is a specific requirement from the Blackjack example in Sutton & Barto's book.
     * @return The initialized {@link Hand}.
     */
    private Hand setupInitialCards() {
        Hand hand = new Hand();
        hand.addToInitialCards(drawCard());
        hand.addToInitialCards(drawCard());

        if (hand.getInitialCards().contains(1)) {
            hand.setUsableAce(true);
        }

        while (sumHand(hand.getInitialCards(), hand.isUsableAce()) < 12) {
            int addedCard = hand.addToInitialCards(drawCard());
            if (!hand.isUsableAce() && addedCard == 1 && sumHand(hand.getInitialCards(), true) <= 21) {
                hand.setUsableAce(true);
            }
        }
        return hand;
    }

    /**
     * Performs first-visit Monte Carlo policy evaluation for a single episode.
     * It traverses the states visited during the game in reverse order and appends the
     * final return (G) to the list of returns for each state.
     *
     * @param gameResult The results of a single simulated Blackjack game.
     */
    private void policyEvaluation(final GameResult gameResult) {
        ArrayList<Integer> initialPlayerCards = gameResult.playerHand().getInitialCards();
        ArrayList<Integer> addedPlayerCards = gameResult.playerHand().getExtraCards();
        boolean usableAce = gameResult.playerHand().isUsableAce();
        int playerSum = sumHand(initialPlayerCards, usableAce) + sumHand(addedPlayerCards, false);
        int convertedAceIndex = gameResult.playerHand().getConvertedAceIndex();
        int dealerFaceUpCard = gameResult.dealerFaceUpCard();
        int G = gameResult.reward();
        int gamma = 1;

        if (playerSum >= 12 && playerSum <= 21) {
            if (usableAce) {
                usableAceStateValueFunction[playerSum - 12][dealerFaceUpCard - 1].rewards.add(G);
            } else {
                nonUsableAceStateValueFunction[playerSum - 12][dealerFaceUpCard - 1].rewards.add(G);
            }
        }

        for (int i = (addedPlayerCards.size() - 1); i >= 0; i--) {
            G = G * gamma;
            playerSum -= addedPlayerCards.get(i);
            if ((convertedAceIndex != -1) && (i == convertedAceIndex)) {
                usableAce = true;
                playerSum += 10;
            }
            if (playerSum >= 12 && playerSum <= 21) { // Check bounds again
                if (usableAce) {
                    usableAceStateValueFunction[playerSum - 12][dealerFaceUpCard - 1].rewards.add(G);
                } else {
                    nonUsableAceStateValueFunction[playerSum - 12][dealerFaceUpCard - 1].rewards.add(G);
                }
            }
        }
    }
    
    // --- NESTED DATA-HOLDER CLASSES ---
    /**
     * A record to encapsulate the final result of the entire simulation.
     * @param usableAceGrid The 10x10 grid of state-values for states with a usable ace.
     * @param nonUsableAceGrid The 10x10 grid of state-values for states without a usable ace.
     */
    public record SimulationResult(double[][] usableAceGrid, double[][] nonUsableAceGrid) {}

    /**
     * A record to encapsulate the result of a single game (episode).
     * @param reward The final reward from the game (1, 0, or -1).
     * @param playerHand The final state of the player's hand.
     * @param dealerFaceUpCard The dealer's first card (1-10).
     */
    public record GameResult(int reward, Hand playerHand, int dealerFaceUpCard) {}
    
    /**
     * A container class to hold a list of rewards for a single state.
     * This is used to collect all returns observed for a state across many episodes.
     */
    public class StateRewards {
    	List<Integer> rewards;
    	
    	/**
    	 * Constructs a new StateRewards object with an empty list of rewards.
    	 */
    	public StateRewards() {
    		rewards = new ArrayList<>();
    	}
    	
    	/**
    	 * Clears all collected rewards from the list.
    	 */
    	public void clear() {
    		this.rewards.clear();
    	}
    	
    	/**
    	 * Returns a string representation of the StateRewards object.
    	 * @return a string detailing the contents of the rewards list.
    	 */
        @Override
        public String toString() {
            return "StateRewards{\n" +
                    "  rewards=" + rewards + ",\n" +
                    "}";
        }
    }
    
    /**
     * A mutable class to represent a hand of cards for either the player or the dealer.
     * It tracks the cards, whether there's a usable ace, and the history of ace conversions.
     */
    public class Hand {
        private ArrayList<Integer> initialCards = new ArrayList<>();
        private ArrayList<Integer> extraCards = new ArrayList<>();
        private boolean usableAce = false;
        private int convertedAceIndex = -1;

        public ArrayList<Integer> getInitialCards() { return initialCards; }
        public ArrayList<Integer> getExtraCards() { return extraCards; }
        public boolean isUsableAce() { return usableAce; }
        public int getConvertedAceIndex() { return convertedAceIndex; }
        public int addToInitialCards(Integer card) { this.initialCards.add(card); return card; }
        public int addToExtraCards(int card) { this.extraCards.add(card); return card; }
        public void setUsableAce(boolean usableAce) { this.usableAce = usableAce; }
        public void setConvertedAceIndex(int convertedAceIndex) { this.convertedAceIndex = convertedAceIndex; }
    }
}