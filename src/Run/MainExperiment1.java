package Run;

import Scheme.Parameters;
import Scheme.DeployedServices;
import Scheme.ServiceDeployMethod;
import Components.Method;
import Components.Traffic;
import Components.Violation;
import Trace.AggregatedTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y.
 *
 * This is the main class for experiment 1
 */
public class MainExperiment1 {

    private static int TOTAL_RUN;

    private static int index = 0;

    public static boolean printCost = false;

    private static int TAU = 15 * 60; // time interval between run of the method (s)
    private static int TRAFFIC_CHANGE_INTERVAL = 15 * 60; // time interval between run of the method (sec)

    public static void main(String[] args) throws FileNotFoundException {
        // in each experiment, these parameters may vary
        Parameters.numCloudServers = 3;
        Parameters.numFogNodes = 10;
        Parameters.numServices = 40;
        Traffic.TRAFFIC_ENLARGE_FACTOR = 1;
        Parameters.initialize();

        ArrayList<Double> traceList = AggregatedTraceReader.readTrafficFromFile(); // read the traffic

        TOTAL_RUN = traceList.size();

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method

        Method AllCloud = new Method(new ServiceDeployMethod(ServiceDeployMethod.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployMethod(ServiceDeployMethod.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployMethod(ServiceDeployMethod.FOG_STATIC, AggregatedTraceReader.averageTrafficTrace), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinCost = new Method(new ServiceDeployMethod(ServiceDeployMethod.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        Method FogStaticViolation = new Method(new ServiceDeployMethod(ServiceDeployMethod.FOG_STATIC, AggregatedTraceReader.averageTrafficTrace), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinViol = new Method(new ServiceDeployMethod(ServiceDeployMethod.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        DeployedServices containersDeployedAllCloud = null;
        DeployedServices containersDeployedAllFog = null;
        DeployedServices containersDeployedFogStatic = null;
        DeployedServices containersDeployedMinCost = null;
        DeployedServices containersDeployedFogStaticViolation = null;
        DeployedServices containersDeployedMinViol = null;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayMinCost = 0;
        double delayFogStaticViolation = 0;
        double delayMinViol = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costMinCost = 0;
        double costFogStaticViolation = 0;
        double costMinViol = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violMinCost = 0;
        double violFogStaticViolation = 0;
        double violMinViol = 0;

        double violationSlack = Violation.getViolationSlack();
        Double trafficPerNodePerService;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(MC)\tD(FSV)\tD()\tD(OP)\tC(AC)\tC(AF)\tC(FS)\tC(MC)\tC(FSV)\tC(MV)\tC(OP)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(MC)\tCNT(FSV)\tCNT(MV)\tCNT(OP)\tV(AC)\tV(AF)\tV(FS)\tV(MC)\tV(FSV)\tV(MV)\tV(OP)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerService = nextRate(traceList); // get the next traffic rate
            Traffic.distributeTraffic(trafficPerNodePerService);

            Traffic.setTrafficToGlobalTraffic(AllCloud);
            containersDeployedAllCloud = AllCloud.run(Traffic.AGGREGATED, false);
            delayAllCloud = AllCloud.getAvgServiceDelay();
            costAllCloud = AllCloud.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = Violation.getViolationPercentage(AllCloud);

            Traffic.setTrafficToGlobalTraffic(AllFog);
            containersDeployedAllFog = AllFog.run(Traffic.AGGREGATED, false);
            delayAllFog = AllFog.getAvgServiceDelay();
            costAllFog = AllFog.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = Violation.getViolationPercentage(AllFog);

            Traffic.setTrafficToGlobalTraffic(FogStatic);
            containersDeployedFogStatic = FogStatic.run(Traffic.AGGREGATED, false);
            delayFogStatic = FogStatic.getAvgServiceDelay();
            costFogStatic = FogStatic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = Violation.getViolationPercentage(FogStatic);

            Traffic.setTrafficToGlobalTraffic(MinCost);
            if (i % q == 0) {
                containersDeployedMinCost = MinCost.run(Traffic.AGGREGATED, false);
            }
            delayMinCost = MinCost.getAvgServiceDelay();
            costMinCost = MinCost.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violMinCost = Violation.getViolationPercentage(MinCost);

            Traffic.setTrafficToGlobalTraffic(FogStaticViolation);
            containersDeployedFogStaticViolation = FogStaticViolation.run(Traffic.AGGREGATED, true);
            delayFogStaticViolation = FogStaticViolation.getAvgServiceDelay();
            costFogStaticViolation = FogStaticViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStaticViolation = Violation.getViolationPercentage(FogStaticViolation);

            Traffic.setTrafficToGlobalTraffic(MinViol);
            if (i % q == 0) {
                containersDeployedMinViol = MinViol.run(Traffic.AGGREGATED, true);
            }
            delayMinViol = MinViol.getAvgServiceDelay();
            costMinViol = MinViol.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violMinViol = Violation.getViolationPercentage(MinViol);

            System.out.println((trafficPerNodePerService * Parameters.numFogNodes * Parameters.numServices) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayMinCost + "\t" + delayFogStaticViolation + "\t" + delayMinViol + "\t" + "NaN"
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costMinCost / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costMinViol / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + "NaN"
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedMinCost.getDeployedFogServices() + "\t" + containersDeployedFogStaticViolation.getDeployedFogServices() + "\t" + containersDeployedMinViol.getDeployedFogServices() + "\t" + "NaN"
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedMinCost.getDeployedCloudServices() + "\t" + containersDeployedFogStaticViolation.getDeployedCloudServices() + "\t" + containersDeployedMinViol.getDeployedCloudServices() + "\t" + "NaN"
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violMinCost + "\t" + violFogStaticViolation + "\t" + violMinViol + "\t" + "NaN");

        }
    }

    /**
     * Gets the next traffic rate from the trace
     *
     * @param traceList the trace
     * @return returns the next traffic rate from the trace
     */
    private static Double nextRate(ArrayList<Double> traceList) {
        return traceList.get(index++);
    }

}
