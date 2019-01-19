package Utilities;

/**
 *
 * @author ashkany
 *
 * This class contains the function for calculating factorial
 */
public class Factorial {

    private static int BIGGEST_FACT_INDEX = 20; // (the biggest fatorial we need in this simulation is bounded by this number, e.g. 20!)
    public static double[] fact = new double[BIGGEST_FACT_INDEX]; // will store i!

    /**
     * Upon calling the construction, all of the n! up to the BIGGEST_FACT_INDEX
     * are calculated
     */
    public Factorial() {
        for (int i = 0; i < BIGGEST_FACT_INDEX; i++) {
            fact[i] = fact(i);
        }
    }

    /**
     * calculates n!
     *
     * @param n the input number
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
