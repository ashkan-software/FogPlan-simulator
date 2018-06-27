package MMP;

import Run.Parameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author ashkany THis class has methods that constructs a MMP based on a
 * traffic pattern that is read from input (e.g. a file)
 */
public class MMPconstructor {

    private Scanner in;
    private final int NUM_OF_STATES = 20;
    private final String FILE_ADDRESS = "/Users/ashkany/Desktop/traffic-pattern.txt";

    private double trafficRateIncreamentSize;
    
    private double averageTrafficRate;

    public MMP mmp;
    
    private double min, max;

    public MMPconstructor() {

        mmp = new MMP(NUM_OF_STATES);
        int[] trafficLevel = null;
        try {
            trafficLevel = readTrafficFromFile(FILE_ADDRESS, NUM_OF_STATES);
        } catch (FileNotFoundException ex) {
            System.out.println("The Traffic Pattern File is Not Found here: " + FILE_ADDRESS);
            ex.printStackTrace();
        }

        double[] TrafficRateInState = setTrafficRateInState(min, NUM_OF_STATES, trafficRateIncreamentSize);
        mmp.setTrafficRateInState(TrafficRateInState);

        // rate[][]
        int[][] rates = new int[NUM_OF_STATES][NUM_OF_STATES];
        for (int i = 0; i < NUM_OF_STATES; i++) {
            for (int j = 0; j < NUM_OF_STATES; j++) {
                rates[i][j] = 0;
            }
        }
        for (int i = 0; i < trafficLevel.length - 1; i++) {
            rates[trafficLevel[i]][trafficLevel[i + 1]]++;
        }
        mmp.setTransitionRates(rates);
//        mmp.printTrafficRateInState();
//        mmp.printTransitionRates();

        // 'Next' arrays
        mmp.configNextArrays();

    }

    public double getAverageTrafficRate() {
        return averageTrafficRate;
    }
    
    /**
     * constructs a MMP based on a traffic pattern that is read from input (e.g.
     * a file) If the FileAddress is empty (""), the input is read from console
     *
     * @param FileAddress
     * @throws java.io.FileNotFoundException
     */
    private int[] readTrafficFromFile(String FileAddress, int numberOfStates) throws FileNotFoundException {

        if (FileAddress.equalsIgnoreCase("")) {
            in = new Scanner(System.in);
        } else {
            File inputFile = new File(FileAddress);
            in = new Scanner(inputFile);
        }

        return extractLevelNumberFromInput(in, numberOfStates);
    }

    private int[] extractLevelNumberFromInput(Scanner in, int numberOfStates) {

        ArrayList<Double> trafficValue = new ArrayList<>(); // this will store the actual traffic values

        double input;
        averageTrafficRate = in.nextDouble();
        while (in.hasNext()) {
            input = in.nextDouble();
            trafficValue.add(input);
        }
        normalizeTraceTraffic(trafficValue);
        findMinAndMax(trafficValue); // this updates the min and max to the new normalized numbers (note that the original min and max might be somehting like 1223 and 10000, but we want them from here to be NORMALIZED min and max)
        
        int[] trafficLevel = new int[trafficValue.size()];

        trafficRateIncreamentSize = (max - min) / (numberOfStates - 1); // number of traffic rates = number of horizontal lines on traffic graph + 1
        for (int i = 0; i < trafficValue.size(); i++) {
            trafficLevel[i] = (int) Math.floor((trafficValue.get(i) - min + 0.0001) / trafficRateIncreamentSize);
        }
        return trafficLevel;

    }

    private double[] setTrafficRateInState(double minTrafficRate, int numberOfStates, double trafficRateIncreamentSize) {
        double[] TrafficRateInState = new double[numberOfStates];
        for (int i = 0; i < numberOfStates; i++) {
            TrafficRateInState[i] = (minTrafficRate + (i * trafficRateIncreamentSize));
        }
        return TrafficRateInState;
    }

    
     private void normalizeTraceTraffic(ArrayList<Double> trafficTrace) {
         findMinAndMax(trafficTrace);
         for (int i = 0; i < trafficTrace.size(); i++) {
            trafficTrace.set(i, (trafficTrace.get(i) - min + 0.0001) / (max - min) * Parameters.TRAFFIC_NORM_FACTOR);
        }
         averageTrafficRate = (averageTrafficRate - min + 0.0001) / (max - min) * Parameters.TRAFFIC_NORM_FACTOR;

    }
     
     private void findMinAndMax(ArrayList<Double> trace) {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (Double traffic : trace) {
            if (traffic < min) {
                min = traffic;
            }
            if (traffic > max) {
                max = traffic;
            }
        }
    }
}
