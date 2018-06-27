/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MMP;

import Utilities.RG;

/**
 *
 * @author ashkany
 */
public class MMPsimulator {

    private int currentState, nextState;
    private MMP mmp;

    private double coin;

    public MMPsimulator(MMP mmp) {
        currentState = (int) (RG.GenUniformRandom() * mmp.numberOfStates);
        this.mmp = mmp;
    }

    

    /**
     * The main function in this class. returns the next rate of traffic
     *
     * @return
     */
    public double nextRate() {
        coin = RG.GenUniformRandom();
        for (int i = 0; i < mmp.next.get(currentState).size() ; i++) { 
            if (coin < mmp.nextProbCumulative.get(currentState).get(i)) {
                nextState = mmp.next.get(currentState).get(i);
//                System.out.println(nextState);
                return mmp.TrafficRateInState[nextState];
            }
        }
        return 0; 
    }

}
