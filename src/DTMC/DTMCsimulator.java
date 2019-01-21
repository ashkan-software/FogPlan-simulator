package DTMC;

import Utilities.RandomGenerator;

/**
 *
 * @author Ashkan Y.
 *
 * This class simulates a DTMC and can generate the new rate of traffic given
 * the current rate of traffic
 */
public class DTMCsimulator {

    private int currentState, nextState; // the state parameters 
    private DTMC dtmc; // the Discrete Time Markov Chain (DTMC) 

    private double coin; // used for random number generation

    /**
     * Constructor of the DTMC simulator class.
     *
     * @param dtmc
     */
    public DTMCsimulator(DTMC dtmc) {
        currentState = (int) (RandomGenerator.genUniformRandom() * dtmc.numberOfStates); // randomly find the current state of the DTMC
        this.dtmc = dtmc;
    }

    /**
     * This is the main function in this class. Calculates the next rate of the
     * traffic in the DTMC
     *
     * @return using the current rate of the traffic, returns the next rate of
     * traffic
     */
    public double nextRate() {
        coin = RandomGenerator.genUniformRandom(); // toss a coin
        for (int i = 0; i < dtmc.next.get(currentState).size(); i++) {
            if (coin < dtmc.nextProbCumulative.get(currentState).get(i)) {
                nextState = dtmc.next.get(currentState).get(i);
                return dtmc.TrafficRateInState[nextState];
            }
        }
        return 0;
    }

}
