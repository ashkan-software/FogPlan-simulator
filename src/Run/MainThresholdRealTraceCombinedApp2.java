package Run;

import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Trace.CombinedAppTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y. This main class runs the scheme for different intervals of
 * running the heuristic in the scheme
 */
public class MainThresholdRealTraceCombinedApp2 {

    private static int index = 0;

    private static int MAX_THRESHOLD = 80;
    private static int MIN_THRESHOLD = 5;
    private static int TOTAL_RUN;

    private final static int TAU = 20; // time interval between run of the heuristic (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the heuristic (s) (10x6sec=60sec)

    public static void main(String[] args) throws FileNotFoundException {

        RunParameters.TAU = TAU;
        RunParameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = RunParameters.TAU / RunParameters.TRAFFIC_CHANGE_INTERVAL;
        // the number of times that traffic changes between each run of the heuristic

        ArrayList<Double[]> traceList = CombinedAppTraceReader.readTrafficFromFile();
        TOTAL_RUN = traceList.size();

        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averagePerFogNode), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);

        Heuristic heuristicFogStaticViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averagePerFogNode), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);

        Heuristic.initializeStaticVariables();

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

        double violationSlack = Heuristic.getViolationSlack();

        Double[] combinedTrafficPerFogNode;

        System.out.println("Threshold\tTraffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);

        for (int threshold = MIN_THRESHOLD; threshold <= MAX_THRESHOLD; threshold = threshold + 1) {

            Heuristic.setThresholds(threshold);
            heuristicFogStatic.unsetFirstTimeBoolean();
            heuristicFogStaticViolation.unsetFirstTimeBoolean();
            index = 0;
            for (int i = 0; i < TOTAL_RUN; i++) {

                combinedTrafficPerFogNode = nextRate(traceList);
                Heuristic.distributeTraffic(combinedTrafficPerFogNode);

                sumTrafficPerNodePerApp += totalTraffic(combinedTrafficPerFogNode);

                heuristicAllCloud.setTrafficToGlobalTraffic();
                containersDeployedAllCloud = heuristicAllCloud.run(Heuristic.COMBINED_APP, false);
                fogcontainersDeployedAllCloud[i] = containersDeployedAllCloud.getDeployedFogServices();
                cloudcontainersDeployedAllCloud[i] = containersDeployedAllCloud.getDeployedCloudServices();
                delayAllCloud[i] = heuristicAllCloud.getAvgServiceDelay();
                costAllCloud[i] = heuristicAllCloud.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violAllCloud[i] = heuristicAllCloud.getViolationPercentage();

                heuristicAllFog.setTrafficToGlobalTraffic();
                containersDeployedAllFog = heuristicAllFog.run(Heuristic.COMBINED_APP, false);
                fogcontainersDeployedAllFog[i] = containersDeployedAllFog.getDeployedFogServices();
                cloudcontainersDeployedAllFog[i] = containersDeployedAllFog.getDeployedCloudServices();
                delayAllFog[i] = heuristicAllFog.getAvgServiceDelay();
                costAllFog[i] = heuristicAllFog.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violAllFog[i] = heuristicAllFog.getViolationPercentage();

                heuristicFogStatic.setTrafficToGlobalTraffic();
                containersDeployedFogStatic = heuristicFogStatic.run(Heuristic.COMBINED_APP, false);
                fogcontainersDeployedFogStatic[i] = containersDeployedFogStatic.getDeployedFogServices();
                cloudcontainersDeployedFogStatic[i] = containersDeployedFogStatic.getDeployedCloudServices();
                delayFogStatic[i] = heuristicFogStatic.getAvgServiceDelay();
                costFogStatic[i] = heuristicFogStatic.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStatic[i] = heuristicFogStatic.getViolationPercentage();

                heuristicFogDynamic.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    containersDeployedFogDynamic = heuristicFogDynamic.run(Heuristic.COMBINED_APP, false);
                    fogcontainersDeployedFogDynamic[i] = containersDeployedFogDynamic.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamic[i] = containersDeployedFogDynamic.getDeployedCloudServices();
                }
                delayFogDynamic[i] = heuristicFogDynamic.getAvgServiceDelay();
                costFogDynamic[i] = heuristicFogDynamic.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamic[i] = heuristicFogDynamic.getViolationPercentage();

                heuristicFogStaticViolation.setTrafficToGlobalTraffic();
                containersDeployedFogStaticViolation = heuristicFogStaticViolation.run(Heuristic.COMBINED_APP, true);
                fogcontainersDeployedFogStaticViolation[i] = containersDeployedFogStaticViolation.getDeployedFogServices();
                cloudcontainersDeployedFogStaticViolation[i] = containersDeployedFogStaticViolation.getDeployedCloudServices();
                delayFogStaticViolation[i] = heuristicFogStaticViolation.getAvgServiceDelay();
                costFogStaticViolation[i] = heuristicFogStaticViolation.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStaticViolation[i] = heuristicFogStaticViolation.getViolationPercentage();

                heuristicFogDynamicViolation.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    containersDeployedFogDynamicViolation = heuristicFogDynamicViolation.run(Heuristic.COMBINED_APP, true);
                    fogcontainersDeployedFogDynamicViolation[i] = containersDeployedFogDynamicViolation.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamicViolation[i] = containersDeployedFogDynamicViolation.getDeployedCloudServices();
                }
                delayFogDynamicViolation[i] = heuristicFogDynamicViolation.getAvgServiceDelay();
                costFogDynamicViolation[i] = heuristicFogDynamicViolation.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamicViolation[i] = heuristicFogDynamicViolation.getViolationPercentage();

            }

            System.out.print(threshold + "\t" + ((sumTrafficPerNodePerApp * RunParameters.NUM_FOG_NODES * RunParameters.NUM_SERVICES) / (TOTAL_RUN))
                    + "\t" + (findAverageOfArray(delayAllCloud)) + "\t" + (findAverageOfArray(delayAllFog)) + "\t" + (findAverageOfArray(delayFogStatic)) + "\t" + (findAverageOfArray(delayFogDynamic)) + "\t" + (findAverageOfArray(delayFogStaticViolation)) + "\t" + (findAverageOfArray(delayFogDynamicViolation))
                    + "\t" + ((findAverageOfArray(costAllCloud) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costAllFog) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogStatic) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogDynamic) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogStaticViolation) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findAverageOfArray(costFogDynamicViolation) / RunParameters.TRAFFIC_CHANGE_INTERVAL))
                    + "\t" + (findAverageOfArray(fogcontainersDeployedAllCloud)) + "\t" + (findAverageOfArray(fogcontainersDeployedAllFog)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogStatic)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogDynamic)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogStaticViolation)) + "\t" + (findAverageOfArray(fogcontainersDeployedFogDynamicViolation))
                    + "\t" + (findAverageOfArray(cloudcontainersDeployedAllCloud)) + "\t" + (findAverageOfArray(cloudcontainersDeployedAllFog)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogStatic)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogDynamic)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogStaticViolation)) + "\t" + (findAverageOfArray(cloudcontainersDeployedFogDynamicViolation))
                    + "\t" + (findAverageOfArray(violAllCloud)) + "\t" + (findAverageOfArray(violAllFog)) + "\t" + (findAverageOfArray(violFogStatic)) + "\t" + (findAverageOfArray(violFogDynamic)) + "\t" + (findAverageOfArray(violFogStaticViolation)) + "\t" + (findAverageOfArray(violFogDynamicViolation)));

            if (threshold % 5 == 0) {
                System.out.print(
                        "\t" + (findStandardDeviationOfArray(delayAllCloud)) + "\t" + (findStandardDeviationOfArray(delayAllFog)) + "\t" + (findStandardDeviationOfArray(delayFogStatic)) + "\t" + (findStandardDeviationOfArray(delayFogDynamic)) + "\t" + (findStandardDeviationOfArray(delayFogStaticViolation)) + "\t" + (findStandardDeviationOfArray(delayFogDynamicViolation))
                        + "\t" + ((findStandardDeviationOfArray(costAllCloud) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costAllFog) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogStatic) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogDynamic) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogStaticViolation) / RunParameters.TRAFFIC_CHANGE_INTERVAL)) + "\t" + ((findStandardDeviationOfArray(costFogDynamicViolation) / RunParameters.TRAFFIC_CHANGE_INTERVAL))
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
