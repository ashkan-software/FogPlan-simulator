package Utilities;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains the functions that are written for filling a arrays with
 * random numbers, according to certain distributions.
 */
public class ArrayFiller {

    /**
     * Given an array, it assigns as the elements of the array the probabilities
     * of a probability density function (PDF). The sum of elements will add up
     * to 1
     *
     * @param input
     */
    public static void fillRandomPDFInArray(double[] input) {
        int[] weight = new int[input.length];
        double sum = 0;
        for (int a = 0; a < input.length; a++) {
            weight[a] = (int) RandomGenerator.genUniformRandomBetween(10, 100);
            sum += weight[a];
        }
        for (int a = 0; a < input.length; a++) {
            input[a] = (double) weight[a] / sum;
        }
    }

    /**
     * This function fills random numbers in a 2D array, in a specified
     * range
     *
     * @param array the input array
     * @param rangeLow the low range of the random numbers generated
     * @param rangehigh the high range of the random numbers generated
     */
    public static void fill2DArrayRandomlyInRange(double[][] array, double rangeLow, double rangehigh) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = RandomGenerator.genUniformRandomBetween(rangeLow, rangehigh);
            }
        }
    }

    /**
     * This function fills random numbers in a 1D array, in a specified
     * range
     *
     * @param array the input array
     * @param rangeLow the low range of the random numbers generated
     * @param rangehigh the high range of the random numbers generated
     */
    public static void fill1DArrayRandomlyInRange(double[] array, double rangeLow, double rangehigh) {
        for (int i = 0; i < array.length; i++) {
            array[i] = RandomGenerator.genUniformRandomBetween(rangeLow, rangehigh);
        }
    }

    /**
     * This function fills random numbers in a 1D array, with specified mean
     * and variance
     *
     * @param array the input array
     * @param mean the mean of the distribution
     * @param variance the variance of the distribution
     */
    public static void fill2DArrayRandomlyWithMeanVariance(double[] array, double mean, double variance) {
        for (int i = 0; i < array.length; i++) {
            do {
                array[i] = RandomGenerator.genNormalRandomMeanVariance(mean, variance);
            } while (array[i] < 0);
        }
    }

    /**
     * This function fills a 2D array, with a constant value
     * @param array the input array
     * @param value the constant value
     */
    public static <T extends Comparable<? super T>> void fill2DArrayWithConstantNumber(T[][] array, T value) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = value;
            }
        }
    }

    /**
     * This function fills a 1D array, with a constant value
     * @param array the input array
     * @param value the constant value
     */
    public static <T extends Comparable<? super T>> void fill1DArrayWithConstantNumber(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }
    
    /**
     * Converts a Integer 2D array to an int 2D array
     * @param input the input 2D array (Integer type)
     * @return 
     */
    public static int[][] convertIntegerToInt2DArray(Integer[][] input){
        int[][] output;
        int rows = input.length;
        int cols = input[0].length;
        output = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                output[i][j] = input[i][j];
            }
        }
        return output;
    }

}
