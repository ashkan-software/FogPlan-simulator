package Run;

import Scheme.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Method;
import Simulation.Traffic;
import Simulation.Violation;
import Trace.CombinedAppTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y.
 */
public class MainExperiment2 {

    private static int TOTAL_RUN;

    private static int index = 0;

    private final static int TAU = 120; // time interval between run of the method (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 60; // time interval between run of the method (s)

    public static void main(String[] args) throws FileNotFoundException {

        Parameters.numCloudServers = 1;
        Parameters.numFogNodes = 10;
        Parameters.numServices = 2;
        Traffic.TRAFFIC_ENLARGE_FACTOR = 1;
        Parameters.initialize();
        ArrayList<Double[]> traceList = CombinedAppTraceReader.readTrafficFromFile();

        TOTAL_RUN = traceList.size(); // 4 hours of trace
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        
        int q = TAU / TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method
        
        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averagePerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamicViol = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method optimalPlacement = new Method(new ServiceDeployScheme(ServiceDeployScheme.OPTIMAL), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter containersDeployedAllCloud = null;
        ServiceCounter containersDeployedAllFog = null;
        ServiceCounter containersDeployedFogStatic = null;
        ServiceCounter containersDeployedFogDynamic = null;
        ServiceCounter containersDeployedFogDynamicViol = null;
        ServiceCounter containersDeployedOptimalPlacement = null;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;
        double delayFogDynamicViol = 0;
        double delayOptimalPlacement = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;
        double costFogDynamicViol = 0;
        double costOptimalPlacement = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;
        double violFogDynamicViol = 0;
        double violOptimalPlacement = 0;

        double violationSlack = Violation.getViolationSlack();
        Double[] combinedTrafficPerFogNode;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FDV)\tD(OP)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FDV)\tC(OP)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FDV)\tCNT(OP)\tCCNT(AC)\tCCNT(AF)\tCCNT(FS)\tCCNT(FD)\tCCNT(FDV)\tCCNT(OP)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FDV)\tV(OP)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            combinedTrafficPerFogNode = nextRate(traceList);
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

            Traffic.setTrafficToGlobalTraffic(FogDynamic);
            if (i % q == 0) {
                containersDeployedFogDynamic = FogDynamic.run(Traffic.COMBINED_APP, false);
            }
            delayFogDynamic = FogDynamic.getAvgServiceDelay();
            costFogDynamic = FogDynamic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = Violation.getViolationPercentage(FogDynamic);

            Traffic.setTrafficToGlobalTraffic(FogDynamicViol);
            if (i % q == 0) {
                containersDeployedFogDynamicViol = FogDynamicViol.run(Traffic.COMBINED_APP, true);
            }
            delayFogDynamicViol = FogDynamicViol.getAvgServiceDelay();
            costFogDynamicViol = FogDynamicViol.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamicViol = Violation.getViolationPercentage(FogDynamicViol);

            Traffic.setTrafficToGlobalTraffic(optimalPlacement);
            containersDeployedOptimalPlacement = optimalPlacement.run(Traffic.COMBINED_APP, true); // boolean will be ignored
            delayOptimalPlacement = optimalPlacement.getAvgServiceDelay();
            costOptimalPlacement = optimalPlacement.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violOptimalPlacement = Violation.getViolationPercentage(optimalPlacement);

            System.out.println((totalTraffic(combinedTrafficPerFogNode) * Parameters.numServices) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayFogDynamicViol + "\t" + delayOptimalPlacement
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamicViol / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costOptimalPlacement / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedFogDynamic.getDeployedFogServices() + "\t" + containersDeployedFogDynamicViol.getDeployedFogServices() + "\t" + containersDeployedOptimalPlacement.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamicViol.getDeployedCloudServices() + "\t" + containersDeployedOptimalPlacement.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic + "\t" + violFogDynamicViol + "\t" + violOptimalPlacement);

        }
    }

    private static Double[] nextRate(ArrayList<Double[]> traceList) {
        return traceList.get(index++);
    }

    private static double totalTraffic(Double[] traffic) {
        double sum = 0;
        for (int j = 0; j < traffic.length; j++) {
            sum += traffic[j];
        }
        return sum;
    }

}
