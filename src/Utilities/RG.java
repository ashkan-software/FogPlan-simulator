package Utilities;

import java.util.Random;

/**
 *
 * @author Ashkan Y.
 */
public class RG {
    
    private static double k = 16807;
    private static double m = 2147483647;
    private static double s = 1111;
    private static double r1;
    private static Random r = new Random();;
    

    private static double temp;
    
    public RG() {
        k = 16807;
        m = 2147483647;
        s = 1111;
    }
    
    
    /**
     * Generate a uniform random number between [0,1)
     */
    public static double GenUniformRandom(){
        s = (k * s) % m;
        return s / m;
//        return Math.random();
    }
    
    /**
     * Generate random number exponentially with rate lambda
     * @param lambda
     */
    public static double GenExponentialRandom(double lambda){
        return (-1/lambda) * Math.log(GenUniformRandom());
        
    }
    
    /**
     * Generate a uniformly distributed random number between min and max
     * @param min
     * @param max
     */
    public static double genUniformRandomBetween(double min, double max){
        return min + (max - min) * GenUniformRandom();
    }
    
    
    
    /**
     * Generate a uniformly distributed random number with the specified mean and variance
     * @param mean
     * @param variance
     * 
     */
    public static double genUniformRandomMeanVariance(double mean, double variance){
        // a = mean - sqrt(3.var),  b = mean + sqrt(3.var)
        temp = Math.sqrt(3*variance);
        return genUniformRandomBetween(mean - temp, mean + temp);
    }
    
    
    /**
     * Generate a normally distributed random number with the specified mean and variance
     * 
     * @param mean
     * @param variance
     * 
     */
    public static double genNormalRandomMeanVariance(double mean, double variance){
        return (r.nextGaussian()*Math.sqrt(variance) + mean);
    }
    
    
     /**
     * Generate a normally distributed random number with the mean 0 and variance 1
     * 
     */
    private static double genNormalRandom(){
        return r.nextGaussian();
    }
    
    
    
    public static double GenNonUniformRandom(){
        r1 = GenExponentialRandom(2);
        if (r1 < 1){
            return r1;
        } else {
            return GenNonUniformRandom();
        }
    }
    
    
    public static void main(String[] args){
        for (int i = 0; i < 100; i++) {
            System.out.println( GenUniformRandom());
        }
    }
    
    
}
