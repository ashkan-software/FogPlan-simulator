package Utilities;

import java.util.Random;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains various function for generating random numbers
 */
public class RandomGenerator {

    // these numbers are required for the pseudo random number generator
    private static double k = 16807;
    private static double m = 2147483647;
    private static double s = 1111;
    private static double r1;

    private static Random r = new Random();
    ;
    private static double temp;

    /**
     * The constructor of RG
     */
    public RandomGenerator() {
        k = 16807;
        m = 2147483647;
        s = 1111;
    }

    /**
     * Generates a uniform random number between [0,1).
     *
     * If Math.random() is used, the function is not deterministic. If the first
     * two lines of this function are used, the random number is deterministic
     * and upon each run of the simulation, we always get the same results
     * (since all of the numbers returned upon call of this function will return
     * the same sequence)
     */
    public static double genUniformRandom() {
//        s = (k * s) % m;
//        return s / m;
        return Math.random();
    }

    /**
     * Generates random number exponentially with rate lambda
     *
     * @param lambda the rate of the exponential distribution
     */
    public static double genExponentialRandom(double lambda) {
        return (-1 / lambda) * Math.log(genUniformRandom());

    }

    /**
     * Generates a uniformly distributed random number between min and max
     *
     * @param min the low range
     * @param max the high range
     * @return a uniformly distributed random number between min and max
     */
    public static double genUniformRandomBetween(double min, double max) {
        return min + (max - min) * genUniformRandom();
    }

    /**
     * Generates a uniformly distributed random number with the specified mean
     * and variance
     *
     * @param mean the specified mean
     * @param variance the specified variance
     * @return a uniformly distributed random number with the specified mean and
     * variance
     *
     */
    public static double genUniformRandomMeanVariance(double mean, double variance) {
        // a = mean - sqrt(3.var),  b = mean + sqrt(3.var)
        temp = Math.sqrt(3 * variance);
        return genUniformRandomBetween(mean - temp, mean + temp);
    }

    /**
     * Generates a normally distributed random number with the specified mean
     * and variance
     *
     * @param mean the specified mean
     * @param variance the specified variance
     * @return a normally distributed random number with the specified mean and
     * variance
     *
     */
    public static double genNormalRandomMeanVariance(double mean, double variance) {
        return (r.nextGaussian() * Math.sqrt(variance) + mean);
    }

    /**
     * Generates a normally distributed random number with the mean 0 and
     * variance 1
     *
     * @return a normally distributed random number with the mean 0 and variance
     * 1
     */
    private static double genNormalRandom() {
        return r.nextGaussian();
    }

    /**
     * Generates non-uniform random number
     *
     * @return
     */
    public static double genNonUniformRandom() {
        r1 = genExponentialRandom(2);
        if (r1 < 1) {
            return r1;
        } else {
            return genNonUniformRandom();
        }
    }

    /*
     This is for unit testing. You can call any function in this class to see how it works
     */
//    public static void main(String[] args) {
//        for (int i = 0; i < 100; i++) {
//            System.out.println(genUniformRandom());
//        }
//    }
}
