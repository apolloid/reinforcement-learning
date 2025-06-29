package adiputra.reinforcementlearning.chapter5.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;

public class Example5_3Model {
    public static final String PROGRESS_PROPERTY = "progress";
    public static final String STATUS_PROPERTY = "status";
    public static final String RESULT_PROPERTY = "result";
    
    private final StateActionRewards[][] usableAceStateActionValueFunction;
    private final StateActionRewards[][] nonUsableAceStateActionValueFunction;
    private final Action[][] usableAcePolicy;
    private final Action[][] nonUsableAcePolicy;
    private final PropertyChangeSupport support;
    
    public enum Action {
        HIT,
        STICK
    }
	
    public Example5_3Model() {
        this.usableAceStateActionValueFunction = createValueFunctionArray();
        this.nonUsableAceStateActionValueFunction = createValueFunctionArray();
        this.usableAcePolicy = createPolicyArray();
        this.nonUsableAcePolicy = createPolicyArray();
        this.support = new PropertyChangeSupport(this);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
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
    
    private Action[][] createPolicyArray() {
    	Action[][] array = new Action[11][10];
    	for (int i = 0; i < 11; i++) {
    		for (int j = 0; j < 10; j++) {
    			array[i][j] = getRandomAction(); 
    		}
    	}
    	return array;
    }
    
    public void runSimulation(final int noOfEpisodes) {
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 10; j++) {
                usableAceStateActionValueFunction[i][j].clearAll();
                nonUsableAceStateActionValueFunction[i][j].clearAll();
                usableAcePolicy[i][j] = getRandomAction();
                nonUsableAcePolicy[i][j] = getRandomAction();
            }
        }

        for (int i = 0; i < noOfEpisodes; i++) {
            GameResult gameResult = simulateBlackjackGame();
            policyEvaluationAndImprovement(gameResult);

            int progress = (i + 1) * 100 / noOfEpisodes;
            support.firePropertyChange(PROGRESS_PROPERTY, null, progress);
            support.firePropertyChange(STATUS_PROPERTY, null, "Processing episode: " + (i + 1) + "/" + noOfEpisodes);
        }

        int[][] usableAceGrid = convertToGrid(usableAcePolicy);
        int[][] nonUsableAceGrid = convertToGrid(nonUsableAcePolicy);

        SimulationResult result = new SimulationResult(usableAceGrid, nonUsableAceGrid);
        support.firePropertyChange(RESULT_PROPERTY, null, result);
    }
    
    private GameResult simulateBlackjackGame() {
        Hand playerHand = setupInitialCards();
        Hand dealerHand = setupInitialCards();
        int dealerFaceUpCard = dealerHand.getInitialCards().get(0);
        
        Action playerFirstRandomAction = getRandomAction();

        int playerSum = playerPlaying(playerHand, playerFirstRandomAction, dealerFaceUpCard);
        int dealerSum = dealerPlaying(dealerHand);
        int reward = calculateReward(playerSum, dealerSum);
        
        return new GameResult(reward, playerHand, dealerFaceUpCard, playerFirstRandomAction);
    }
    
    private Hand setupInitialCards() {
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
    
    private int playerPlaying(Hand playerHand, Action playerFirstRandomAction, int dealerFaceUpCard) {
        if (playerFirstRandomAction == Action.STICK) {
            return sumHand(playerHand.getInitialCards(), playerHand.isUsableAce());
        }
        
        playerHand.addToExtraCards(drawCard());
        
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
            }
            
            Action policyAction;
            if (playerHand.isUsableAce()) {
                policyAction = usableAcePolicy[currentSum - 11][dealerFaceUpCard - 1]; 
            } else {
                policyAction = nonUsableAcePolicy[currentSum - 11][dealerFaceUpCard - 1];
            }

            if (policyAction == Action.HIT) {
                playerHand.addToExtraCards(drawCard());
            } else {
                return currentSum;
            }
        }
    }
    
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
    
    private int drawCard() {
        int card = (int) (Math.random() * 13) + 1;
        if (card > 10) {
            return 10;
        } else {
            return card;
        }
    }
    
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
    
    public static Action getRandomAction() {
        return ThreadLocalRandom.current().nextBoolean() ? Action.HIT : Action.STICK;
    }
    
    private void policyEvaluationAndImprovement(GameResult gameResult) {
        // --- Unpack all data from the game result ---
        int G = gameResult.reward;
        final int gamma = 1; // Discount rate is 1 for non-discounted Monte Carlo
        Hand playerHand = gameResult.playerHand;
        int dealerFaceUpCard = gameResult.dealerFaceUpCard;
        ArrayList<Integer> initialPlayerCards = playerHand.getInitialCards();
        ArrayList<Integer> addedPlayerCards = playerHand.getExtraCards();
        int convertedAceIndex = playerHand.getConvertedAceIndex();

        // --- Determine the final state of the player's hand ---
        // Note: We calculate sum without the "usable ace" bonus initially to handle ace conversions correctly.
        int playerSum = sumHand(initialPlayerCards, false) + sumHand(addedPlayerCards, false);
        boolean usableAce = playerHand.isUsableAce();
        if (usableAce) {
            playerSum += 10;
        }

        // --- LOGIC FIX 1: Account for the final "STICK" action ---
        // The player's turn ends by either busting or sticking. If they didn't bust,
        // their last action in their final state was to STICK. Your original code
        // never accounted for this action, except in the trivial case where the
        // very first random action was STICK.
        if (playerSum <= 21) {
            if (usableAce) {
                usableAceStateActionValueFunction[playerSum - 11][dealerFaceUpCard - 1].stickRewards.add(G);
                policyImprovement(usableAcePolicy, usableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
            } else {
                nonUsableAceStateActionValueFunction[playerSum - 11][dealerFaceUpCard - 1].stickRewards.add(G);
                policyImprovement(nonUsableAcePolicy, nonUsableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
            }
        }
        
        // --- LOGIC FIX 2: Correctly update all preceding "HIT" actions ---
        // The `addedPlayerCards` list exists precisely because the player chose to HIT
        // at each preceding step. Therefore, we must update the `hitRewards` for each of
        // those states. Your original code incorrectly checked the current policy to decide
        // whether to update hitRewards or stickRewards.
        for (int i = (addedPlayerCards.size() - 1); i >= 0; i--) {
            G = G * gamma; // This has no effect if gamma=1, but is correct form.

            // Revert to the state *before* the last card was drawn.
            playerSum -= addedPlayerCards.get(i);
            
            // As we move backward in time, we might "un-convert" an ace.
            // If the current step `i` is where an ace was converted from 11 to 1,
            // it means for all prior states (including this one), the ace was usable.
            if (convertedAceIndex != -1 && i == convertedAceIndex) {
                usableAce = true;
                playerSum += 10; // Add the 10 points back for the usable ace.
            }

            // At this state, we know the action taken was HIT.
            // We update the value for the (state, HIT) pair.
            if (usableAce) {
                usableAceStateActionValueFunction[playerSum - 11][dealerFaceUpCard - 1].hitRewards.add(G);
                policyImprovement(usableAcePolicy, usableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
            } else {
                nonUsableAceStateActionValueFunction[playerSum - 11][dealerFaceUpCard - 1].hitRewards.add(G);
                policyImprovement(nonUsableAcePolicy, nonUsableAceStateActionValueFunction, playerSum, dealerFaceUpCard);
            }
        }
    }
    
    private void policyImprovement(Action[][] policy, StateActionRewards[][] stateActionValueFunction, int playerSum, int dealerFaceUpCard) {
    	if (stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards.isEmpty()) {
    		policy[playerSum-11][dealerFaceUpCard-1] = Action.STICK;
    	}
    	else if (stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards.isEmpty()) {
    		policy[playerSum-11][dealerFaceUpCard-1] = Action.HIT;
    	}
    	else {
    		double avgHitRewards = getAvgRewards(stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].hitRewards);
    		double avgStickRewards =  getAvgRewards(stateActionValueFunction[playerSum-11][dealerFaceUpCard-1].stickRewards);
    		
    		if (avgHitRewards > avgStickRewards) {
    			policy[playerSum-11][dealerFaceUpCard-1] = Action.HIT;
    		}
    		else if (avgHitRewards < avgStickRewards) {
    			policy[playerSum-11][dealerFaceUpCard-1] = Action.STICK;
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
    
    private int[][] convertToGrid(Action[][] policy) {
    	int[][] grid = new int[11][10];
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 10; j++) {
                if(policy[i][j] == Action.HIT) {
                	grid[i][j] = 1;
                } else {
                	grid[i][j] = 0;
                }
            }
        }
        
        return grid;
    }
    
    // --- NESTED DATA-HOLDER CLASSES ---
    public record SimulationResult(int[][] usableAceGrid, int[][] nonUsableAceGrid) {}

    public record GameResult(int reward, Hand playerHand, int dealerFaceUpCard, Action playerFirstRandomAction) {}
    
    public class StateActionRewards {
    	List<Integer> hitRewards;
    	List<Integer> stickRewards;
    	
    	public StateActionRewards() {
    		hitRewards = new ArrayList<>();
    		stickRewards = new ArrayList<>();
    	}
    	
    	public void clearAll() {
    		this.hitRewards.clear();
    		this.stickRewards.clear();
    	}
    	
        @Override
        public String toString() {
            return "StateActionRewards{\n" +
                    "  hitRewards=" + hitRewards + ",\n" +
                    "  stickRewards=" + stickRewards + "\n" +
                    "}";
        }
    }
    
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
