package Trace;

import Scheme.Parameters;
import Components.Traffic;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Ashkan Y.
 *
 *
 */
public class CombinedAppTrace6secReader extends GenericCombinedAppTraceReader {

    private static final String FILE_ADDRESS = "trace-combined-apps-6sec.txt";
    // This files includes the combined app trace information for 4 hours, 2017/04/12-13 (12pm-4pm of day 1), in the interval of 6 seconds
    private static Scanner in;

    /**
     * Reads traffic from the file (addressed in FILE_ADDRESS)
     *
     * @return returns the traffic an an arrayList (timestamped) of array of
     * doubles (each element of array is traffic per fog node)
     * @throws FileNotFoundException if the file is not found
     */
    public static ArrayList<Double[]> readTrafficFromFile() throws FileNotFoundException {

        trafficTrace = new ArrayList<>();
        if (FILE_ADDRESS.equalsIgnoreCase("")) {
            in = new Scanner(System.in); // read from console
        } else {
            File inputFile = new File(FILE_ADDRESS);
            in = new Scanner(inputFile);
        }

        return extractTrafficTrace(in);
    }

    /**
     * extract the traffic trace from scanner
     *
     * @param in the input scanner
     * @return returns the traffic an an arrayList (timestamped) of array of
     * doubles (each element of array is traffic per fog node)
     */
    private static ArrayList<Double[]> extractTrafficTrace(Scanner in) {
        double input;
        while (in.hasNext()) {
            Double[] combinedAppPerFogNode = new Double[Parameters.numFogNodes];
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                in.nextInt(); // ignore the first numbers, which are the indices of fog node
                combinedAppPerFogNode[j] = in.nextDouble();
            }
            trafficTrace.add(combinedAppPerFogNode);
        }
        normalizeTraceTraffic();
        return trafficTrace;
    }

}
