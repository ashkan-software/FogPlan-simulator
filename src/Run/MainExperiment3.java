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
public class MainExperiment3 {

    private static int index = 0;

    private static int MAX_THRESHOLD = 80;
    private static int MIN_THRESHOLD = 8;
    private static int TOTAL_RUN;

    private final static int TAU = 20; // time interval between run of the method(s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the method(s) (10x6sec=60sec)

    public static void main(String[] args) throws FileNotFoundException {

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;
        // the number of times that traffic changes between each run of the mehod
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
        double[] fogcontainersDeployedAllCloud = new double[TOTAL_RUN];
        double[] fogcontainersDeployedAllFog = new double[TOTAL_RUN];
        double[] fogcontainersDeployedFogStatic = new double[TOTAL_RUN];
        double[] fogcontainersDeployedFogDynamic = new double[TOTAL_RUN];
        double[] fogcontainersDeployedFogStaticViolation = new double[TOTAL_RUN];
        double[] fogcontainersDeployedFogDynamicViolation = new double[TOTAL_RUN];

        // used for getting average
        double[] cloudcontainersDeployedAllCloud = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedAllFog = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedFogStatic = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedFogDynamic = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedFogStaticViolation = new double[TOTAL_RUN];
        double[] cloudcontainersDeployedFogDynamicViolation = new double[TOTAL_RUN];

        double[] delayAllCloud = new double[TOTAL_RUN];
        double[] delayAllFog = new double[TOTAL_RUN];
        double[] delayFogStatic = new double[TOTAL_RUN];
        double[] delayFogDynamic = new double[TOTAL_RUN];
        double[] delayFogStaticViolation = new double[TOTAL_RUN];
        double[] delayFogDynamicViolation = new double[TOTAL_RUN];

        double[] costAllCloud = new double[TOTAL_RUN];
        double[] costAllFog = new double[TOTAL_RUN];
        double[] costFogStatic = new double[TOTAL_RUN];
        double[] costFogDynamic = new double[TOTAL_RUN];
        double[] costFogStaticViolation = new double[TOTAL_RUN];
        double[] costFogDynamicViolation = new double[TOTAL_RUN];

        double[] violAllCloud = new double[TOTAL_RUN];
        double[] violAllFog = new double[TOTAL_RUN];
        double[] violFogStatic = new double[TOTAL_RUN];
        double[] violFogDynamic = new double[TOTAL_RUN];
        double[] violFogStaticViolation = new double[TOTAL_RUN];
        double[] violFogDynamicViolation = new double[TOTAL_RUN];

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

                Traffic.setTrafficToGlobalTraffic(FogDynamic);
                if (i % q == 0) {
                    containersDeployedFogDynamic = FogDynamic.run(Traffic.COMBINED_APP, false);
                    fogcontainersDeployedFogDynamic[i] = containersDeployedFogDynamic.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamic[i] = containersDeployedFogDynamic.getDeployedCloudServices();
                }
                delayFogDynamic[i] = FogDynamic.getAvgServiceDelay();
                costFogDynamic[i] = FogDynamic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamic[i] = Violation.getViolationPercentage(FogDynamic);

                Traffic.setTrafficToGlobalTraffic(FogStaticViolation);
                containersDeployedFogStaticViolation = FogStaticViolation.run(Traffic.COMBINED_APP, true);
                fogcontainersDeployedFogStaticViolation[i] = containersDeployedFogStaticViolation.getDeployedFogServices();
                cloudcontainersDeployedFogStaticViolation[i] = containersDeployedFogStaticViolation.getDeployedCloudServices();
                delayFogStaticViolation[i] = FogStaticViolation.getAvgServiceDelay();
                costFogStaticViolation[i] = FogStaticViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStaticViolation[i] = Violation.getViolationPercentage(FogStaticViolation);

                Traffic.setTrafficToGlobalTraffic(FogDynamicViolation);
                if (i % q == 0) {
                    containersDeployedFogDynamicViolation = FogDynamicViolation.run(Traffic.COMBINED_APP, true);
                    fogcontainersDeployedFogDynamicViolation[i] = containersDeployedFogDynamicViolation.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamicViolation[i] = containersDeployedFogDynamicViolation.getDeployedCloudServices();
                }
                delayFogDynamicViolation[i] = FogDynamicViolation.getAvgServiceDelay();
                costFogDynamicViolation[i] = FogDynamicViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamicViolation[i] = Violation.getViolationPercentage(FogDynamicViolation);

            }

            System.out.print(threshold + "\t" + ((sumTrafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) / (TOTAL_RUN))
                    + "\t" + (findAverageOfArray(delayAllCloud)) + "\t" + (findAverageOfArray(delayAllFog)) + "\t" + (findAverageOfArray(delayFogStatic)) + "\t" + (findAverageOfArray(delayFogDynamic)) + "\t" + (findAverageOfArray(delayFogStaticViolation)) + "\t" + (findAverageOfArray(delayFogDynamicViolation))
                    + "\t" + ((findAverageOfArray(costAllCloud) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costAllFog) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogStatic) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogDynamic) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogStaticViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogDynamicViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL))
                    + "\t" + (findAverageOfArray(fogcontainersDeployedAllCloud)) + "\t" + (findAverageOfArray(fogcontainersDeployedAllFog)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogStatic)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogDynamic)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogStaticViolation)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogDynamicViolation))
                    + "\t" + (findAverageOfArray(cloudcontainersDeployedAllCloud)) + "\t" + (findAverageOfArray(cloudcontainersDeployedAllFog)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogStatic)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogDynamic)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogStaticViolation)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogDynamicViolation))
                    + "\t" + (findAverageOfArray(violAllCloud)) + "\t" + (findAverageOfArray(violAllFog)) + "\t" + (findAverageOfArray(violFogStatic)) + "\t" + (findAverageOfArray(violFogDynamic)) + "\t" + (findAverageOfArray(violFogStaticViolation)) + "\t" + (findAverageOfArray(violFogDynamicViolation)));

            if (threshold % 5 == 0) {
                System.out.print(
                        "\t" + (findStandardDeviationOfArray(delayAllCloud)) + "\t" + (findStandardDeviationOfArray(delayAllFog)) + "\t" + (findStandardDeviationOfArray(delayFogStatic)) + "\t" + (findStandardDeviationOfArray(delayFogDynamic)) + "\t" + (findStandardDeviationOfArray(delayFogStaticViolation)) + "\t" + (findStandardDeviationOfArray(delayFogDynamicViolation))
                        + "\t" + ((findStandardDeviationOfArray(costAllCloud) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costAllFog) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogStatic) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogDynamic) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogStaticViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogDynamicViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL))
                        + "\t" + (findStandardDeviationOfArray(fogcontainersDeployedAllCloud)) + "\t" + (findStandardDeviationOfArray(fogcontainersDeployedAllFog)) + "\t" + (findStandardDeviationOfArray(fogcontainersDeployedFogStatic)) + "\t" + (findStandardDeviationOfArray(fogcontainersDeployedFogDynamic)) + "\t" + (findStandardDeviationOfArray(fogcontainersDeployedFogStaticViolation)) + "\t" + (findStandardDeviationOfArray(fogcontainersDeployedFogDynamicViolation))
                        + "\t" + (findStandardDeviationOfArray(cloudcontainersDeployedAllCloud)) + "\t" + (findStandardDeviationOfArray(cloudcontainersDeployedAllFog)) + "\t" + (findStandardDeviationOfArray(cloudcontainersDeployedFogStatic)) + "\t" + (findStandardDeviationOfArray(cloudcontainersDeployedFogDynamic)) + "\t" + (findStandardDeviationOfArray(cloudcontainersDeployedFogStaticViolation)) + "\t" + (findStandardDeviationOfArray(cloudcontainersDeployedFogDynamicViolation))
                        + "\t" + (findStandardDeviationOfArray(violAllCloud)) + "\t" + (findStandardDeviationOfArray(violAllFog)) + "\t" + (findStandardDeviationOfArray(violFogStatic)) + "\t" + (findStandardDeviationOfArray(violFogDynamic)) + "\t" + (findStandardDeviationOfArray(violFogStaticViolation)) + "\t" + (findStandardDeviationOfArray(violFogDynamicViolation))
                );
            }
            System.out.println("");
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

    private static double findAverageOfArray(double[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
        }
        return (sum / input.length);
    }

    private static double findStandardDeviationOfArray(double[] input) {
        double average = findAverageOfArray(input);
        double sumSquare = 0;
        for (int i = 0; i < input.length; i++) {
            sumSquare += Math.pow((input[i] - average), 2);
        }
        return Math.sqrt(sumSquare / (input.length - 1));
    }
}
