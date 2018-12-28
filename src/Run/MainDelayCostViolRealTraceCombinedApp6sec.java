package Run;

import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Method;
import Simulation.Traffic;
import Simulation.Violation;
import Trace.CombinedAppTrace6secReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y. This experiment is not used in the paper. The traffic is
 * sparse and the performance of different schemes is not clear with this
 * experiment. However, this file is left for refernce only
 */
public class MainDelayCostViolRealTraceCombinedApp6sec {

    private static int TOTAL_RUN;

    private static int index = 0;

    private final static int TAU = 18; // time interval between run of the method (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 6; // time interval between run of the method (s)

    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<Double[]> traceList = CombinedAppTrace6secReader.readTrafficFromFile();

        TOTAL_RUN = traceList.size(); // 4 hours of trace
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        Parameters.initialize();

        int q = TAU / TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method

        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTrace6secReader.averagePerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter containersDeployedAllCloud = null;
        ServiceCounter containersDeployedAllFog = null;
        ServiceCounter containersDeployedFogStatic = null;
        ServiceCounter containersDeployedFogDynamic = null;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;

        double violationSlack = Violation.getViolationSlack();
        Double[] combinedTrafficPerFogNode;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            combinedTrafficPerFogNode = nextRate(traceList);
            Traffic.distributeTraffic(combinedTrafficPerFogNode);

            Traffic.setTrafficToGlobalTraffic(AllCloud);
            containersDeployedAllCloud = AllCloud.run(Traffic.COMBINED_APP, false);
            delayAllCloud = AllCloud.getAvgServiceDelay();
            costAllCloud = AllCloud.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = Violation.getViolationPercentage(AllCloud);

            Traffic.setTrafficToGlobalTraffic(AllFog);
            containersDeployedAllFog = AllFog.run(Traffic.COMBINED_APP, false);
            delayAllFog = AllFog.getAvgServiceDelay();
            costAllFog = AllFog.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = Violation.getViolationPercentage(AllFog);

            Traffic.setTrafficToGlobalTraffic(FogStatic);
            containersDeployedFogStatic = FogStatic.run(Traffic.COMBINED_APP, false);
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

            System.out.println((totalTraffic(combinedTrafficPerFogNode) * Parameters.numServices) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedFogDynamic.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamic.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic);

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
