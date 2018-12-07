package Trace;

import Run.Parameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Ashkan Y.
 */
public class SparseTraceReader {

    private static final String FILE_ADDRESS = "/Users/ashkany/Desktop/trace-sparse1.txt";
    private static Scanner in;

    private static Double lambda[][];

    private static Double min, max;

//    public static double[][] randomAverage; 
    public static Double[][] average;
    private static double[][] CumulativeAverage;

    public static ArrayList<Double[][]> traceList;

    public static ArrayList<Double[][]> readTraceAndExtractNormalTraffic() throws FileNotFoundException {

        if (FILE_ADDRESS.equalsIgnoreCase("")) {
            in = new Scanner(System.in);
        } else {
            File inputFile = new File(FILE_ADDRESS);
            in = new Scanner(inputFile);
        }
        return extractTrafficTrace(in, 5);
    }

    private static ArrayList<Double[][]> extractTrafficTrace(Scanner in, int INTERVAL) {

        traceList = new ArrayList<>();

        int lastTime = 1;
        int currTime;

        String line;
        String[] elements = new String[4];

        int appID, regID;
        long size;
        initializeTrafficArray();

        while (in.hasNext()) {

            line = in.nextLine();
            elements = line.split(" ");
            currTime = Integer.parseInt(elements[0]);

            if (currTime >= INTERVAL + lastTime) {

                lastTime = currTime;
                traceList.add(lambda);
                initializeTrafficArray();

            } else {
                appID = Integer.parseInt(elements[1]);
                regID = Integer.parseInt(elements[2]);
                size = Integer.parseInt(elements[3]);
                lambda[appID][regID] += size;
            }
        }
        normalizeTraceTraffic();
        return traceList;
    }

    private static void initializeTrafficArray() {
        lambda = new Double[Parameters.numServices][Parameters.numFogNodes];
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                lambda[a][j] = 0d;
            }
        }
    }

    private static void initializeAverageParameters() {
        //        randomAverage = new double[Parameters.numServices][Parameters.numFogNodes];
        average = new Double[Parameters.numServices][Parameters.numFogNodes];
        CumulativeAverage = new double[Parameters.numServices][Parameters.numFogNodes];
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                CumulativeAverage[a][j] = 0d;
            }
        }
    }

    private static void normalizeTraceTraffic() {
        initializeAverageParameters();
        
        // we are going to divide the traffic, such that the each traffic
        // is between [0:10] 
        findMinAndMax(traceList);
        for (Double[][] traffic : traceList) {
            normalizeTraffic(traffic);
        }
        int times = traceList.size();
//        int randomIndex;
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
//                randomIndex = (int) (RG.GenUniformRandom() * times);
//                randomAverage[a][j] = traceList.get(randomIndex)[a][j];
                average[a][j] = CumulativeAverage[a][j] / times;
            }
        }
    }

    private static void normalizeTraffic(Double[][] traffic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                traffic[a][j] = ((traffic[a][j] - min + 0.0001) / (max - min)) * Parameters.TRAFFIC_NORM_FACTOR;
                CumulativeAverage[a][j] += traffic[a][j];
            }
        }
    }

    private static void findMinAndMax(ArrayList<Double[][]> trace) {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (Double[][] traffic : traceList) {
            for (int a = 0; a < Parameters.numServices; a++) {
                for (int j = 0; j < Parameters.numFogNodes; j++) {
                    if (traffic[a][j] < min) {
                        min = traffic[a][j];
                    }
                    if (traffic[a][j] > max) {
                        max = traffic[a][j];
                    }
                }
            }
        }
    }

}
