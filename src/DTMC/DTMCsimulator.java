package DTMC;

import Utilities.RG;

/**
 *
 * @author Ashkan Y. This class simulates a DTMC and generates the new rate of
 traffic given the current rate of traffic 
 */
public class DTMCsimulator {

    private int currentState, nextState;
    private DTMC dtmc;

    private double coin; // used for random number generation

    
    public DTMCsimulator(DTMC dtmc) {
        currentState = (int) (RG.GenUniformRandom() * dtmc.numberOfStates); // randomly find the current state of the DTMC
        this.dtmc = dtmc;
    }

    /**
     * This is the main function in this class. returns the next rate of traffic
     *
     * @return
     */
    public double nextRate() {
        coin = RG.GenUniformRandom(); // toss a coin
        for (int i = 0; i < dtmc.next.get(currentState).size(); i++) {
            if (coin < dtmc.nextProbCumulative.get(currentState).get(i)) {
                nextState = dtmc.next.get(currentState).get(i);
                return dtmc.TrafficRateInState[nextState];
            }
        }
        return 0;
    }

}
