package Run;

import Scheme.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Components.Delay;
import Components.Method;
import Components.Traffic;
import Components.Violation;
import Trace.CombinedAppTraceReader;
import Utilities.Statistics;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y. 
 * 
 * This is the main class for experiment 3
 */
public class MainExperiment3 {

    private static int trafficRateIndex = 0;

    private static int MAX_THRESHOLD = 80; // maximum threshold value of the experiment
    private static int MIN_THRESHOLD = 8; // minimum threshold value of the experiment
    private static int TOTAL_RUN;
    
    private final static int TAU = 10; // time interval between run of the method(s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the method(s)

    public static void main(String[] args) throws FileNotFoundException {
        // in each experiment, these parameters may vary
        Parameters.numCloudServers = 3;
        Parameters.numFogNodes = 10;
        Parameters.numServices = 20;
        Traffic.TRAFFIC_ENLARGE_FACTOR = 1;
        Parameters.initialize();
        
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;
        // the number of times that traffic changes between each run of the mehod
        
        ArrayList<Double[]> traceList = CombinedAppTraceReader.readTrafficFromFile();
        TOTAL_RUN = traceList.size();

        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averageTrafficPerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinCost = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        Method FogStaticViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averageTrafficPerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinViol = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter containersDeployedAllCloud;
        ServiceCounter containersDeployedAllFog;
        ServiceCounter containersDeployedFogStatic;
        ServiceCounter containersDeployedMinCost = null;
        ServiceCounter containersDeployedFogStaticViolation;
        ServiceCounter containersDeployedMinViol = null;

        // used for getting average
        double[] fogcontainersDeployedAllCloud = new double[TOTAL_RUN];
        double[] fogcontainersDeployedAllFog = new double[TOTAL_RUN];
        double[] fogcontainersDeployedFogStatic = new double[TOTAL_RUN];
        double[] fogcontainersDeployedMinCost = new double[TOTAL_RUN];
        double[] fogcontainersDeployedFogStaticViolation = new double[TOTAL_RUN];
        double[] fogcontainersDeployedMinViol = new double[TOTAL_RUN];

        // used for getting average
        double[] cloudcontainersDeployedAllCloud = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedAllFog = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedFogStatic = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedMinCost = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedFogStaticViolation = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedMinViol = new double[TOTAL_RUN];

        double[] delayAllCloud = new double[TOTAL_RUN];
        double[] delayAllFog = new double[TOTAL_RUN];
        double[] delayFogStatic = new double[TOTAL_RUN];
        double[] delayMinCost = new double[TOTAL_RUN];
        double[] delayFogStaticViolation = new double[TOTAL_RUN];
        double[] delayMinViol = new double[TOTAL_RUN];

        double[] costAllCloud = new double[TOTAL_RUN];
        double[] costAllFog = new double[TOTAL_RUN];
        double[] costFogStatic = new double[TOTAL_RUN];
        double[] costMinCost = new double[TOTAL_RUN];
        double[] costFogStaticViolation = new double[TOTAL_RUN];
        double[] costMinViol = new double[TOTAL_RUN];

        double[] violAllCloud = new double[TOTAL_RUN];
        double[] violAllFog = new double[TOTAL_RUN];
        double[] violFogStatic = new double[TOTAL_RUN];
        double[] violMinCost = new double[TOTAL_RUN];
        double[] violFogStaticViolation = new double[TOTAL_RUN];
        double[] violMinViol = new double[TOTAL_RUN];

        double sumTrafficPerNodePerApp = 0; // used for obtaining the average of traffic

        double violationSlack = Violation.getViolationSlack();

        Double[] combinedTrafficPerFogNode;

        System.out.println("Threshold\tTraffic\tD(AC)\tD(AF)\tD(FS)\tD(MC)\tD(FSV)\tD(MV)\tC(AC)\tC(AF)\tC(FS)\tC(MC)\tC(FSV)\tC(MV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(MC)\tCNT(FSV)\tCNT(MV)\tV(AC)\tV(AF)\tV(FS)\tV(MC)\tV(FSV)\tV(MV)\tVS=" + violationSlack);

        for (int threshold = MIN_THRESHOLD; threshold <= MAX_THRESHOLD; threshold = threshold + 1) {

            Delay.setThresholds(threshold); // set the thresholds to the current threshold
            FogStatic.unsetFirstTimeBoolean(); // this makes Fog Static run for the first time
            FogStaticViolation.unsetFirstTimeBoolean(); // this makes Fog Static Violation run for the first time
            trafficRateIndex = 0;
            for (int i = 0; i < TOTAL_RUN; i++) {

                combinedTrafficPerFogNode = nextRate(traceList); // get the next rate
                Traffic.distributeTraffic(combinedTrafficPerFogNode);
                sumTrafficPerNodePerApp += totalTraffic(combinedTrafficPerFogNode);

                Traffic.setTrafficToGlobalTraffic(AllCloud);
                containersDeployedAllCloud = AllCloud.run(Traffic.COMBINED_APP, false);
                fogcontainersDeployedAllCloud[i] = containersDeployedAllCloud.getDeployedFogServices();
                cloudcontainersDeployedAllCloud[i] = containersDeployedAllCloud.getDeployedCloudServices();
                delayAllCloud[i] = AllCloud.getAvgServiceDelay();
                costAllCloud[i] = AllCloud.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violAllCloud[i] = Violation.getViolationPercentage(AllCloud);
                
                Traffic.setTrafficToGlobalTraffic(AllFog);
                containersDeployedAllFog = AllFog.run(Traffic.COMBINED_APP, false);
                fogcontainersDeployedAllFog[i] = containersDeployedAllFog.getDeployedFogServices();
                cloudcontainersDeployedAllFog[i] = containersDeployedAllFog.getDeployedCloudServices();
                delayAllFog[i] = AllFog.getAvgServiceDelay();
                costAllFog[i] = AllFog.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violAllFog[i] = Violation.getViolationPercentage(AllFog);

                Traffic.setTrafficToGlobalTraffic(FogStatic);
                containersDeployedFogStatic = FogStatic.run(Traffic.COMBINED_APP, false);
                fogcontainersDeployedFogStatic[i] = containersDeployedFogStatic.getDeployedFogServices();
                cloudcontainersDeployedFogStatic[i] = containersDeployedFogStatic.getDeployedCloudServices();
                delayFogStatic[i] = FogStatic.getAvgServiceDelay();
                costFogStatic[i] = FogStatic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStatic[i] = Violation.getViolationPercentage(FogStatic);

                Traffic.setTrafficToGlobalTraffic(MinCost);
                if (i % q == 0) {
                    containersDeployedMinCost = MinCost.run(Traffic.COMBINED_APP, false);
                }
                fogcontainersDeployedMinCost[i] = containersDeployedMinCost.getDeployedFogServices();
                cloudcontainersDeployedMinCost[i] = containersDeployedMinCost.getDeployedCloudServices();
                delayMinCost[i] = MinCost.getAvgServiceDelay();
                costMinCost[i] = MinCost.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violMinCost[i] = Violation.getViolationPercentage(MinCost);

                Traffic.setTrafficToGlobalTraffic(FogStaticViolation);
                containersDeployedFogStaticViolation = FogStaticViolation.run(Traffic.COMBINED_APP, true);
                fogcontainersDeployedFogStaticViolation[i] = containersDeployedFogStaticViolation.getDeployedFogServices();
                cloudcontainersDeployedFogStaticViolation[i] = containersDeployedFogStaticViolation.getDeployedCloudServices();
                delayFogStaticViolation[i] = FogStaticViolation.getAvgServiceDelay();
                costFogStaticViolation[i] = FogStaticViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStaticViolation[i] = Violation.getViolationPercentage(FogStaticViolation);

                Traffic.setTrafficToGlobalTraffic(MinViol);
                if (i % q == 0) {
                    containersDeployedMinViol = MinViol.run(Traffic.COMBINED_APP, true);
                }
                fogcontainersDeployedMinViol[i] = containersDeployedMinViol.getDeployedFogServices();
                cloudcontainersDeployedMinViol[i] = containersDeployedMinViol.getDeployedCloudServices();
                delayMinViol[i] = MinViol.getAvgServiceDelay();
                costMinViol[i] = MinViol.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violMinViol[i] = Violation.getViolationPercentage(MinViol);

            }

            System.out.print(threshold + "\t" + ((sumTrafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) / (TOTAL_RUN))
                    + "\t" + (Statistics.findAverageOfArray(delayAllCloud)) + "\t" + (Statistics.findAverageOfArray(delayAllFog)) + "\t" + (Statistics.findAverageOfArray(delayFogStatic)) + "\t" + (Statistics.findAverageOfArray(delayMinCost)) + "\t" + (Statistics.findAverageOfArray(delayFogStaticViolation)) + "\t" + (Statistics.findAverageOfArray(delayMinViol))
                    + "\t" + ((Statistics.findAverageOfArray(costAllCloud) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findAverageOfArray(costAllFog) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findAverageOfArray(costFogStatic) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findAverageOfArray(costMinCost) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findAverageOfArray(costFogStaticViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findAverageOfArray(costMinViol) / Parameters.TRAFFIC_CHANGE_INTERVAL))
                    + "\t" + (Statistics.findAverageOfArray(fogcontainersDeployedAllCloud)) + "\t" + (Statistics.findAverageOfArray(fogcontainersDeployedAllFog)) + "\t" + (Statistics.findAverageOfArray(fogcontainersDeployedFogStatic)) + "\t" + (Statistics.findAverageOfArray(fogcontainersDeployedMinCost)) + "\t" + (Statistics.findAverageOfArray(fogcontainersDeployedFogStaticViolation)) + "\t" + (Statistics.findAverageOfArray(fogcontainersDeployedMinViol))
                    + "\t" + (Statistics.findAverageOfArray(cloudcontainersDeployedAllCloud)) + "\t" + (Statistics.findAverageOfArray(cloudcontainersDeployedAllFog)) + "\t" + (Statistics.findAverageOfArray(cloudcontainersDeployedFogStatic)) + "\t" + (Statistics.findAverageOfArray(cloudcontainersDeployedMinCost)) + "\t" + (Statistics.findAverageOfArray(cloudcontainersDeployedFogStaticViolation)) + "\t" + (Statistics.findAverageOfArray(cloudcontainersDeployedMinViol))
                    + "\t" + (Statistics.findAverageOfArray(violAllCloud)) + "\t" + (Statistics.findAverageOfArray(violAllFog)) + "\t" + (Statistics.findAverageOfArray(violFogStatic)) + "\t" + (Statistics.findAverageOfArray(violMinCost)) + "\t" + (Statistics.findAverageOfArray(violFogStaticViolation)) + "\t" + (Statistics.findAverageOfArray(violMinViol)));
            // prints standard deviation parameters only every 5 times
            if (threshold % 5 == 0) {
                System.out.print("\t" + (Statistics.findStandardDeviationOfArray(delayAllCloud)) + "\t" + (Statistics.findStandardDeviationOfArray(delayAllFog)) + "\t" + (Statistics.findStandardDeviationOfArray(delayFogStatic)) + "\t" + (Statistics.findStandardDeviationOfArray(delayMinCost)) + "\t" + (Statistics.findStandardDeviationOfArray(delayFogStaticViolation)) + "\t" + (Statistics.findStandardDeviationOfArray(delayMinViol))
                        + "\t" + ((Statistics.findStandardDeviationOfArray(costAllCloud) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findStandardDeviationOfArray(costAllFog) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findStandardDeviationOfArray(costFogStatic) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findStandardDeviationOfArray(costMinCost) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findStandardDeviationOfArray(costFogStaticViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((Statistics.findStandardDeviationOfArray(costMinViol) / Parameters.TRAFFIC_CHANGE_INTERVAL))
                        + "\t" + (Statistics.findStandardDeviationOfArray(fogcontainersDeployedAllCloud)) + "\t" + (Statistics.findStandardDeviationOfArray(fogcontainersDeployedAllFog)) + "\t" + (Statistics.findStandardDeviationOfArray(fogcontainersDeployedFogStatic)) + "\t" + (Statistics.findStandardDeviationOfArray(fogcontainersDeployedMinCost)) + "\t" + (Statistics.findStandardDeviationOfArray(fogcontainersDeployedFogStaticViolation)) + "\t" + (Statistics.findStandardDeviationOfArray(fogcontainersDeployedMinViol))
                        + "\t" + (Statistics.findStandardDeviationOfArray(cloudcontainersDeployedAllCloud)) + "\t" + (Statistics.findStandardDeviationOfArray(cloudcontainersDeployedAllFog)) + "\t" + (Statistics.findStandardDeviationOfArray(cloudcontainersDeployedFogStatic)) + "\t" + (Statistics.findStandardDeviationOfArray(cloudcontainersDeployedMinCost)) + "\t" + (Statistics.findStandardDeviationOfArray(cloudcontainersDeployedFogStaticViolation)) + "\t" + (Statistics.findStandardDeviationOfArray(cloudcontainersDeployedMinViol))
                        + "\t" + (Statistics.findStandardDeviationOfArray(violAllCloud)) + "\t" + (Statistics.findStandardDeviationOfArray(violAllFog)) + "\t" + (Statistics.findStandardDeviationOfArray(violFogStatic)) + "\t" + (Statistics.findStandardDeviationOfArray(violMinCost)) + "\t" + (Statistics.findStandardDeviationOfArray(violFogStaticViolation)) + "\t" + (Statistics.findStandardDeviationOfArray(violMinViol))
                );
            }
            System.out.println("");
            sumTrafficPerNodePerApp = 0;

        }
    }

    /**
     * Gets the next traffic rate from the trace
     *
     * @param traceList the trace
     * @return returns the next traffic rate from the trace
     */
    private static Double[] nextRate(ArrayList<Double[]> traceList) {
        return traceList.get(trafficRateIndex++);
    }

    /**
     * Calculates the total rate of traffic from an array of traffic rates
     *
     * @param traffic the array of traffic rates
     */
    private static double totalTraffic(Double[] traffic) {
        double sum = 0;
        for (int j = 0; j < traffic.length; j++) {
            sum += traffic[j];
        }
        return sum;
    }

}
