package Utilities;

/**
 *
 * @author ashkany
 *
 * This class contains functions that are used for getting average and standard
 * deviation of the elements in an array
 */
public class Statistics {

    /**
     * Finds the average of the elements in an array
     *
     * @param input the input array (must be 1D)
     * @return returns the average of the elements in an array
     */
    public static double findAverageOfArray(double[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
        }
        return sum / input.length;
    }

    /**
     * Finds the standard deviation of the elements in an array
     *
     * @param input the input array (must be 1D)
     * @return returns the standard deviation of the elements in an array
     */
    public static double findStandardDeviationOfArray(double[] input) {
        // this function simply calculates the standard deviation, according to the equation of standard equation
        double average = Statistics.findAverageOfArray(input);
        double sumSquare = 0;
        for (int i = 0; i < input.length; i++) {
            sumSquare += Math.pow(input[i] - average, 2);
        }
        return Math.sqrt(sumSquare / (input.length - 1));
    }

}
