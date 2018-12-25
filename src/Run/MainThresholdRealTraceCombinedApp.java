package Run;

import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Delay;
import Simulation.Method;
import Simulation.Traffic;
import Simulation.Violation;
import Trace.CombinedAppTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y. This main class runs the scheme for different intervals of
 * running the method in the scheme
 */
public class MainThresholdRealTraceCombinedApp {

    private static int index = 0;

    private static int MAX_THRESHOLD = 80;
    private static int MIN_THRESHOLD = 5;
    private static int TOTAL_RUN;

    private final static int TAU = 20; // time interval between run of the method(s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the method(s) (10x6sec=60sec)

    public static void main(String[] args) throws FileNotFoundException {

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;
        // the number of times that traffic changes between each run of the method
        Parameters.initialize();

        ArrayList<Double[]> traceList = CombinedAppTraceReader.readTrafficFromFile();
        TOTAL_RUN = traceList.size();

        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averagePerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        Method FogStaticViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averagePerFogNode), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamicViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter containersDeployedAllCloud;
        ServiceCounter containersDeployedAllFog;
        ServiceCounter containersDeployedFogStatic;
        ServiceCounter containersDeployedFogDynamic;
        ServiceCounter containersDeployedFogStaticViolation;
        ServiceCounter containersDeployedFogDynamicViolation;

        // used for getting average
        double fogcontainersDeployedAllCloud = 0d;
        double fogcontainersDeployedAllFog = 0d;
        double fogcontainersDeployedFogStatic = 0d;
        double fogcontainersDeployedFogDynamic = 0d;
        double fogcontainersDeployedFogStaticViolation = 0d;
        double fogcontainersDeployedFogDynamicViolation = 0d;

        // used for getting average
        double cloudcontainersDeployedAllCloud = 0d;
        double cloudcontainersDeployedAllFog = 0d;
        double cloudcontainersDeployedFogStatic = 0d;
        double cloudcontainersDeployedFogDynamic = 0d;
        double cloudcontainersDeployedFogStaticViolation = 0d;
        double cloudcontainersDeployedFogDynamicViolation = 0d;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;
        double delayFogStaticViolation = 0;
        double delayFogDynamicViolation = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;
        double costFogStaticViolation = 0;
        double costFogDynamicViolation = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;
        double violFogStaticViolation = 0;
        double violFogDynamicViolation = 0;

        double sumTrafficPerNodePerApp = 0; // used for getting average

        double violationSlack = Violation.getViolationSlack();

        Double[] combinedTrafficPerFogNode;
        
        System.out.println("Threshold\tTraffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);

        for (int threshold = MIN_THRESHOLD; threshold <= MAX_THRESHOLD; threshold = threshold + 1) {

            Delay.setThresholds(threshold);
            FogStatic.unsetFirstTimeBoolean();
            FogStaticViolation.unsetFirstTimeBoolean();
            index = 0;
            for (int i = 0; i < TOTAL_RUN; i++) {

                combinedTrafficPerFogNode = nextRate(traceList);
                Traffic.distributeTraffic(combinedTrafficPerFogNode);

                sumTrafficPerNodePerApp += totalTraffic(combinedTrafficPerFogNode);

                Traffic.setTrafficToGlobalTraffic(AllCloud);
                containersDeployedAllCloud = AllCloud.run(Traffic.COMBINED_APP, false);
                fogcontainersDeployedAllCloud += containersDeployedAllCloud.getDeployedFogServices();
                cloudcontainersDeployedAllCloud += containersDeployedAllCloud.getDeployedCloudServices();
                delayAllCloud += AllCloud.getAvgServiceDelay();
                costAllCloud += AllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violAllCloud += Violation.getViolationPercentage(AllCloud);

                Traffic.setTrafficToGlobalTraffic(AllFog);
                containersDeployedAllFog = AllFog.run(Traffic.COMBINED_APP, false);
                fogcontainersDeployedAllFog += containersDeployedAllFog.getDeployedFogServices();
                cloudcontainersDeployedAllFog += containersDeployedAllFog.getDeployedCloudServices();
                delayAllFog += AllFog.getAvgServiceDelay();
                costAllFog += AllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violAllFog += Violation.getViolationPercentage(AllFog);

                Traffic.setTrafficToGlobalTraffic(FogStatic);
                containersDeployedFogStatic = FogStatic.run(Traffic.COMBINED_APP, false);
                fogcontainersDeployedFogStatic += containersDeployedFogStatic.getDeployedFogServices();
                cloudcontainersDeployedFogStatic += containersDeployedFogStatic.getDeployedCloudServices();
                delayFogStatic += FogStatic.getAvgServiceDelay();
                costFogStatic += FogStatic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStatic += Violation.getViolationPercentage(FogStatic);

                Traffic.setTrafficToGlobalTraffic(FogDynamic);
                if (i % q == 0) {
                    containersDeployedFogDynamic = FogDynamic.run(Traffic.COMBINED_APP, false);
                    fogcontainersDeployedFogDynamic += containersDeployedFogDynamic.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamic += containersDeployedFogDynamic.getDeployedCloudServices();
                }
                delayFogDynamic += FogDynamic.getAvgServiceDelay();
                costFogDynamic += FogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamic += Violation.getViolationPercentage(FogDynamic);

                Traffic.setTrafficToGlobalTraffic(FogStaticViolation);
                containersDeployedFogStaticViolation = FogStaticViolation.run(Traffic.COMBINED_APP, true);
                fogcontainersDeployedFogStaticViolation += containersDeployedFogStaticViolation.getDeployedFogServices();
                cloudcontainersDeployedFogStaticViolation += containersDeployedFogStaticViolation.getDeployedCloudServices();
                delayFogStaticViolation += FogStaticViolation.getAvgServiceDelay();
                costFogStaticViolation += FogStaticViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStaticViolation += Violation.getViolationPercentage(FogStaticViolation);

                Traffic.setTrafficToGlobalTraffic(FogDynamicViolation);
                if (i % q == 0) {
                    containersDeployedFogDynamicViolation = FogDynamicViolation.run(Traffic.COMBINED_APP, true);
                    fogcontainersDeployedFogDynamicViolation += containersDeployedFogDynamicViolation.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamicViolation += containersDeployedFogDynamicViolation.getDeployedCloudServices();
                }
                delayFogDynamicViolation += FogDynamicViolation.getAvgServiceDelay();
                costFogDynamicViolation += FogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamicViolation += Violation.getViolationPercentage(FogDynamicViolation);

            }

            System.out.println(threshold + "\t" + ((sumTrafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) / (TOTAL_RUN))
                    + "\t" + (delayAllCloud / TOTAL_RUN) + "\t" + (delayAllFog / TOTAL_RUN) + "\t" + (delayFogStatic / TOTAL_RUN) + "\t" + (delayFogDynamic / TOTAL_RUN) + "\t" + (delayFogStaticViolation / TOTAL_RUN) + "\t" + (delayFogDynamicViolation / TOTAL_RUN)
                    + "\t" + ((costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (fogcontainersDeployedAllCloud / TOTAL_RUN) + "\t" + (fogcontainersDeployedAllFog / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogStatic / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogDynamic / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogStaticViolation / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogDynamicViolation / TOTAL_RUN)
                    + "\t" + (cloudcontainersDeployedAllCloud / TOTAL_RUN) + "\t" + (cloudcontainersDeployedAllFog / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogStatic / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogDynamic / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogStaticViolation / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogDynamicViolation / TOTAL_RUN)
                    + "\t" + (violAllCloud / TOTAL_RUN) + "\t" + (violAllFog / TOTAL_RUN) + "\t" + (violFogStatic / TOTAL_RUN) + "\t" + (violFogDynamic / TOTAL_RUN) + "\t" + (violFogStaticViolation / TOTAL_RUN) + "\t" + (violFogDynamicViolation / TOTAL_RUN));

             // reset the average parameters
            fogcontainersDeployedAllCloud = 0;
            fogcontainersDeployedAllFog = 0;
            fogcontainersDeployedFogStatic = 0;
            fogcontainersDeployedFogDynamic = 0;
            fogcontainersDeployedFogStaticViolation = 0;
            fogcontainersDeployedFogDynamicViolation = 0;
            
            cloudcontainersDeployedAllCloud = 0;
            cloudcontainersDeployedAllFog = 0;
            cloudcontainersDeployedFogStatic = 0;
            cloudcontainersDeployedFogDynamic = 0;
            cloudcontainersDeployedFogStaticViolation = 0;
            cloudcontainersDeployedFogDynamicViolation = 0;

            delayAllCloud = 0;
            delayAllFog = 0;
            delayFogStatic = 0;
            delayFogDynamic = 0;
            delayFogStaticViolation = 0;
            delayFogDynamicViolation = 0;

            costAllCloud = 0;
            costAllFog = 0;
            costFogStatic = 0;
            costFogDynamic = 0;
            costFogStaticViolation = 0;
            costFogDynamicViolation = 0;

            violAllCloud = 0;
            violAllFog = 0;
            violFogStatic = 0;
            violFogDynamic = 0;
            violFogStaticViolation = 0;
            violFogDynamicViolation = 0;

            sumTrafficPerNodePerApp = 0;

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
