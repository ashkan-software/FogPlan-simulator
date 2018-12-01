package Trace;

import Run.RunParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Ashkan Y.
 */
public class CombinedAppTraceReader {

    private static final String FILE_ADDRESS = "/Users/ashkany/Desktop/trace-combined-apps.txt";
    // This files includes the combined app trace information for 4 hours, 2017/04/12-13 (12pm-4pm of day 1)
    private static Scanner in;

    private static ArrayList<Double[]> trafficTrace; // it is a list of arrays. Each array elements represents the amount of data for each app

    public static Double[] averagePerFogNode;
    private static double[] CumulativeAveragePerFogNode;

    private static Double min, max;

    public static ArrayList<Double[]> readTrafficFromFile() throws FileNotFoundException {

        trafficTrace = new ArrayList<>();
        if (FILE_ADDRESS.equalsIgnoreCase("")) {
            in = new Scanner(System.in);
        } else {
            File inputFile = new File(FILE_ADDRESS);
            in = new Scanner(inputFile);
        }

        return extractTrafficTrace(in);
    }

    private static ArrayList<Double[]> extractTrafficTrace(Scanner in) {

        double input;
        
        while (in.hasNext()) {
            in.nextInt(); // ignore the first number, which is the second in (1,15)
            Double[] combinedAppPerFogNode = new Double[RunParameters.NUM_FOG_NODES];
            for (int j = 0; j < RunParameters.NUM_FOG_NODES; j++) {
                in.nextInt(); // ignore the first numbers, which are the indices of fog node
                combinedAppPerFogNode[j] = in.nextDouble();
            }
            trafficTrace.add(combinedAppPerFogNode);
        }
        normalizeTraceTraffic();
        return trafficTrace;
    }

    private static void initializeAverageParameters() {
        averagePerFogNode = new Double[RunParameters.NUM_FOG_NODES];
        CumulativeAveragePerFogNode = new double[RunParameters.NUM_FOG_NODES];
        for (int j = 0; j < RunParameters.NUM_FOG_NODES; j++) {
            CumulativeAveragePerFogNode[j] = 0d;
        }
    }

    private static void normalizeTraceTraffic() {
        initializeAverageParameters();
        // we are going to divide the traffic, such that the each traffic element (per fog node)
        // is between [0:10]
        findMinAndMax(trafficTrace);
        for (Double[] combinedAppPerFogNode : trafficTrace) {
            normalizeTraffic(combinedAppPerFogNode);
        }
        int times = trafficTrace.size();
        for (int j = 0; j < RunParameters.NUM_FOG_NODES; j++) {
            averagePerFogNode[j] = CumulativeAveragePerFogNode[j] / times;
        }
    }

    private static void normalizeTraffic(Double[] combinedAppPerFogNode) {
        for (int j = 0; j < RunParameters.NUM_FOG_NODES; j++) {
            combinedAppPerFogNode[j] = ((combinedAppPerFogNode[j] - min + 0.0001) / (max - min)) * RunParameters.TRAFFIC_NORM_FACTOR * RunParameters.NUM_SERVICES;
            CumulativeAveragePerFogNode[j] += combinedAppPerFogNode[j];
        }
    }

    private static void findMinAndMax(ArrayList<Double[]> trace) {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (Double[] combinedAppPerFogNode : trace) {
            for (int j = 0; j < RunParameters.NUM_FOG_NODES; j++) {
                if (combinedAppPerFogNode[j] < min) {
                    min = combinedAppPerFogNode[j];
                }
                if (combinedAppPerFogNode[j] > max) {
                    max = combinedAppPerFogNode[j];
                }
            }
        }
    }

}
