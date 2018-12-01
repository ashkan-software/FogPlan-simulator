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
public class CumulativeTraceReader {

    private static final String FILE_ADDRESS = "/Users/ashkany/Desktop/traffic-pattern.txt";
    private static Scanner in;

    private static ArrayList<Double> trafficTrace;
    public static double averageTrafficTrace;

    private static double min, max;

    public static ArrayList<Double> readTrafficFromFile() throws FileNotFoundException {

        trafficTrace = new ArrayList<>();
        if (FILE_ADDRESS.equalsIgnoreCase("")) {
            in = new Scanner(System.in);
        } else {
            File inputFile = new File(FILE_ADDRESS);
            in = new Scanner(inputFile);
        }

        return extractTrafficTrace(in);
    }

    private static ArrayList<Double> extractTrafficTrace(Scanner in) {

        double input;
        averageTrafficTrace = in.nextDouble();
        while (in.hasNext()) {
            input = in.nextDouble();
            trafficTrace.add(input);
        }
        normalizeTraceTraffic();
        return trafficTrace;
    }

    private static void normalizeTraceTraffic() {
        findMinAndMax(trafficTrace);
        for (int i = 0; i < trafficTrace.size(); i++) {
            trafficTrace.set(i, (trafficTrace.get(i) - min + 0.0001) / (max - min) * RunParameters.TRAFFIC_NORM_FACTOR);
        }

        averageTrafficTrace = (averageTrafficTrace - min) / (max - min) * RunParameters.TRAFFIC_NORM_FACTOR;
    }

    private static void findMinAndMax(ArrayList<Double> trace) {
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
