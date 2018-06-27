package Utilities;

/**
 *
 * @author ashkany
 */
public class ArrayFiller {

  
    
    /**
     * Given an array, it returns as the elements of array random percentages
     * (the sum of percentages will add up to 100%)
     *
     * @param input
     * @param min
     * @param max
     */
    public static void generateRandomDistributionOnArray(double[] input, double min, double max) {
        int[] weight = new int[input.length];
        double sum = 0;
        for (int a = 0; a < input.length; a++) {
            weight[a] = (int) RG.genUniformRandomBetween(min, max);
            sum += weight[a];
        }
        for (int a = 0; a < input.length; a++) {
            input[a] = (double) weight[a] / sum;
        }
    }

    
    /**
     * This function generates random delays in a 2D array, in a specified range
     *
     * @param array input array
     * @param rangeLow the low range of the random numbers generated
     * @param rangehigh the high range of the random numbers generated
     */
    public static void generateRandom2DArray(double[][] array, double rangeLow, double rangehigh) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = RG.genUniformRandomBetween(rangeLow, rangehigh);
            }
        }
    }

    /**
     * This function generates random numbers in a 1D array, in a specified
     * range
     *
     * @param array
     * @param rangeLow the low range of the random numbers generated
     * @param rangehigh the high range of the random numbers generated
     */
    public static void generateRandom1DArray(double[] array, double rangeLow, double rangehigh) {
        for (int i = 0; i < array.length; i++) {
            array[i] = RG.genUniformRandomBetween(rangeLow, rangehigh);
        }
    }

    /**
     * This function generates random numbers in a 1D array, with a specified
     * mean and variance
     *
     * @param array
     * @param mean
     * @param variance
     */
    public static void generateRandomMeanVarinance1DArray(double[] array, double mean, double variance) {
        for (int i = 0; i < array.length; i++) {
            do {
                array[i] = RG.genNormalRandomMeanVariance(mean, variance);
            } while (array[i] < 0);
        }
    }

    public static void generateFixed2DArray(double[][] array, double value) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = value;
            }
        }
    }
    
    public static void generateFixed1DArray(double[] array, double value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }
    
    public static void generateFixed2DArray(int[][] array, int value) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = value;
            }
        }
    }
    
    public static void generateFixed1DArray(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }
}
