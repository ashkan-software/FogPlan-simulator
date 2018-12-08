/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

/**
 *
 * @author ashkany
 */
public class Factorial {
    
    private static int BIGGEST_FACT_INDEX = 20;
    public static double[] fact = new double[BIGGEST_FACT_INDEX]; // will store i!

    public Factorial() {
        for (int i = 0; i < BIGGEST_FACT_INDEX; i++) {
            fact[i] = fact(i);
        }
    }
    
    
    /**
     * calculates n!
     *
     * @param n
     * @return n!
     */
    private static double fact(int n) {
        if (n == 0 || n == 1) {
            return 1;
        } else {
            return n * fact(n - 1);
        }
    }
}
