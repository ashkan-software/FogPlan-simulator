package Run;

import Scheme.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Components.Method;
import Components.Traffic;
import Components.Violation;
import Trace.CombinedAppTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y.
 *
 * This is the main class for experiment 2
 */
public class MainExperiment2 {

    private static int TOTAL_RUN;

    private static int index = 0;

    private final static int TAU = 120; // time interval between run of the method (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 60; // time interval between run of the method (s)

    public static void main(String[] args) throws FileNotFoundException {
        // in each experiment, these parameters may vary
        Parameters.numCloudServers = 1;
        Parameters.numFogNodes = 10;
        Parameters.numServices = 2;
        Traffic.TRAFFIC_ENLARGE_FACTOR = 1;

        Parameters.initialize();
        ArrayList<Double[]> traceList = CombinedAppTraceReader.readTrafficFromFile();

        TOTAL_RUN = traceList.size();
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;

        int q = TAU / TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method

        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averageTrafficPerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinCost = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinViol = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method optimalPlacement = new Method(new ServiceDeployScheme(ServiceDeployScheme.OPTIMAL), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter containersDeployedAllCloud = null;
        ServiceCounter containersDeployedAllFog = null;
        ServiceCounter containersDeployedFogStatic = null;
        ServiceCounter containersDeployedMinCost = null;
        ServiceCounter containersDeployedMinViol = null;
        ServiceCounter containersDeployedOptimalPlacement = null;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayMinCost = 0;
        double delayMinViol = 0;
        double delayOptimalPlacement = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costMinCost = 0;
        double costMinViol = 0;
        double costOptimalPlacement = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violMinCost = 0;
        double violMinViol = 0;
        double violOptimalPlacement = 0;

        double violationSlack = Violation.getViolationSlack();
        Double[] combinedTrafficPerFogNode;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(MC)\tD(MV)\tD(OP)\tC(AC)\tC(AF)\tC(FS)\tC(MC)\tC(MV)\tC(OP)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(MC)\tCNT(MV)\tCNT(OP)\tCCNT(AC)\tCCNT(AF)\tCCNT(FS)\tCCNT(MC)\tCCNT(MV)\tCCNT(OP)\tV(AC)\tV(AF)\tV(FS)\tV(MC)\tV(MV)\tV(OP)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            combinedTrafficPerFogNode = nextRate(traceList); // gets the next rate
            Traffic.distributeTraffic(combinedTrafficPerFogNode);

            Traffic.setTrafficToGlobalTraffic(AllCloud);
            containersDeployedAllCloud = AllCloud.run(Traffic.COMBINED_APP, false); // boolean will be ignored
            delayAllCloud = AllCloud.getAvgServiceDelay();
            costAllCloud = AllCloud.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = Violation.getViolationPercentage(AllCloud);

            Traffic.setTrafficToGlobalTraffic(AllFog);
            containersDeployedAllFog = AllFog.run(Traffic.COMBINED_APP, false); // boolean will be ignored
            delayAllFog = AllFog.getAvgServiceDelay();
            costAllFog = AllFog.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = Violation.getViolationPercentage(AllFog);

            Traffic.setTrafficToGlobalTraffic(FogStatic);
            containersDeployedFogStatic = FogStatic.run(Traffic.COMBINED_APP, true);
            delayFogStatic = FogStatic.getAvgServiceDelay();
            costFogStatic = FogStatic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = Violation.getViolationPercentage(FogStatic);

            Traffic.setTrafficToGlobalTraffic(MinCost);
            if (i % q == 0) {
                containersDeployedMinCost = MinCost.run(Traffic.COMBINED_APP, false);
            }
            delayMinCost = MinCost.getAvgServiceDelay();
            costMinCost = MinCost.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violMinCost = Violation.getViolationPercentage(MinCost);

            Traffic.setTrafficToGlobalTraffic(MinViol);
            if (i % q == 0) {
                containersDeployedMinViol = MinViol.run(Traffic.COMBINED_APP, true);
            }
            delayMinViol = MinViol.getAvgServiceDelay();
            costMinViol = MinViol.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violMinViol = Violation.getViolationPercentage(MinViol);

            Traffic.setTrafficToGlobalTraffic(optimalPlacement);
            containersDeployedOptimalPlacement = optimalPlacement.run(Traffic.COMBINED_APP, true); // boolean will be ignored
            delayOptimalPlacement = optimalPlacement.getAvgServiceDelay();
            costOptimalPlacement = optimalPlacement.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violOptimalPlacement = Violation.getViolationPercentage(optimalPlacement);

            System.out.println((totalTraffic(combinedTrafficPerFogNode) * Parameters.numServices) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayMinCost + "\t" + delayMinViol + "\t" + delayOptimalPlacement
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costMinCost / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costMinViol / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costOptimalPlacement / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedMinCost.getDeployedFogServices() + "\t" + containersDeployedMinViol.getDeployedFogServices() + "\t" + containersDeployedOptimalPlacement.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedMinCost.getDeployedCloudServices() + "\t" + containersDeployedMinViol.getDeployedCloudServices() + "\t" + containersDeployedOptimalPlacement.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violMinCost + "\t" + violMinViol + "\t" + violOptimalPlacement);

        }
    }

    /**
     * Gets the next traffic rate from the trace
     *
     * @param traceList the trace
     * @return returns the next traffic rate from the trace
     */
    private static Double[] nextRate(ArrayList<Double[]> traceList) {
        return traceList.get(index++);
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
