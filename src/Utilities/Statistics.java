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
public class Statistics {

    public static double findAverageOfArray(double[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
        }
        return sum / input.length;
    }

    public static double findStandardDeviationOfArray(double[] input) {
        double average = Statistics.findAverageOfArray(input);
        double sumSquare = 0;
        for (int i = 0; i < input.length; i++) {
            sumSquare += Math.pow(input[i] - average, 2);
        }
        return Math.sqrt(sumSquare / (input.length - 1));
    }
    
}
