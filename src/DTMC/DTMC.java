package DTMC;

import java.util.ArrayList;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains the variables and parameters of the Discrete Time Markov
 * Chain (DTMC)
 */
public class DTMC {

    public int numberOfStates; // total number of states of the MC
    public double[] TrafficRateInState; // this is the rate of the process when we are in state x
    public int[][] rate; // this is the transition rate from state x_i to x_j

    protected ArrayList<ArrayList<Integer>> next;
    protected ArrayList<ArrayList<Double>> nextProbCumulative;
    private int[] totalRate; // stores the sum of the rates in one row in the rate matrix

    public DTMC(int numberOfStates) {
        this.numberOfStates = numberOfStates;
    }

    public void setTransitionRates(int[][] rate) {
        this.rate = rate;
    }

    public void setTrafficRateInState(double[] TrafficRateInState) {
        this.TrafficRateInState = TrafficRateInState;
    }

    /**
     * Prints the transition rates
     */
    public void printTransitionRates() {
        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < numberOfStates; j++) {
                System.out.print(rate[i][j] + " ");
            }
            System.out.println("");
        }
    }

    /**
     * Prints the traffic rates (traffic rates are the states basically)
     */
    public void printTrafficRateInState() {
        for (int i = 0; i < numberOfStates; i++) {
            System.out.print(TrafficRateInState[i] + " ");
        }
        System.out.println("");
    }

    public void configNextArrays() {
        // Total
        totalRate = new int[numberOfStates];
        for (int i = 0; i < numberOfStates; i++) {
            totalRate[i] = 0;
            for (int j = 0; j < numberOfStates; j++) {
                totalRate[i] += rate[i][j];
            }
        }

        // Next
        next = new ArrayList<>();
        for (int i = 0; i < numberOfStates; i++) {
            ArrayList<Integer> innerList = new ArrayList<>();
            for (int j = 0; j < numberOfStates; j++) {
                if (rate[i][j] != 0) {
                    innerList.add(j);
                }
            }
            next.add(innerList);
        }

        // Next Probability Cumulative
        nextProbCumulative = new ArrayList<>();
        for (int i = 0; i < numberOfStates; i++) {
            ArrayList<Double> innerList = new ArrayList<>();
            for (int j = 0; j < numberOfStates; j++) {
                if (rate[i][j] != 0) {
                    innerList.add((double) rate[i][j] / totalRate[i]);
                }
            }
            // make it Cumulative
            for (int j = 1; j < innerList.size(); j++) {
                innerList.set(j, innerList.get(j - 1) + innerList.get(j));
            }
            // Just to make sure last one is 1
            innerList.set(innerList.size() - 1, 1.0);
            nextProbCumulative.add(innerList);
        }
    }

    /**
     * Prints the totalRate array elements
     */
    public void printTotalRate() {
        for (int i = 0; i < numberOfStates; i++) {
            System.out.print(totalRate[i] + " ");
        }
        System.out.println("");
    }

    /**
     * Prints the Next array elements
     */
    public void printNext() {
        for (int i = 0; i < numberOfStates; i++) {
            System.out.print(next.get(i) + " ");
        }
        System.out.println("");
        for (int i = 0; i < numberOfStates; i++) {
            System.out.print(nextProbCumulative.get(i) + " ");
        }
        System.out.println("");
    }

}
