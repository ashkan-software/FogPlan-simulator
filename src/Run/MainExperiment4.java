package Run;

import Scheme.Parameters;
import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Delay;
import Simulation.Method;
import Simulation.Traffic;
import Simulation.Violation;
import Utilities.Statistics;

/**
 *
 * @author Ashkan Y. This main class runs the scheme for different intervals of
 * running the method in the scheme
 */
public class MainExperiment4 {

    private static int MAX_CHANGE_INTERVAL = 200;
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the method (s)
    private static int MIN_CHANGE_INTERVAL = 10;

    private final static int TOTAL_RUN = 600;

    public static void main(String[] args) {
        Parameters.numCloudServers = 3;
        Parameters.numFogNodes = 10;
        Parameters.numServices = 50;

        Traffic.TRAFFIC_ENLARGE_FACTOR = 100;

        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);

        int q;
        // the number of times that traffic changes between each run of the method
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        Parameters.initialize();

        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamicViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter FDserviceCounter, FDVserviceCounter;

        double[] fogContainersDeployedFogDynamic = new double[TOTAL_RUN]; // used for getting average
        double[] cloudContainersDeployedFogDynamic = new double[TOTAL_RUN]; // used for getting average
        double[] delayFogDynamic = new double[TOTAL_RUN]; // used for getting average
        double[] costFogDynamic = new double[TOTAL_RUN]; // used for getting average
        double[] violFogDynamic = new double[TOTAL_RUN]; // used for getting average

        double[] fogContainersDeployedFogDynamicViolation = new double[TOTAL_RUN]; // used for getting average
        double[] cloudContainersDeployedFogDynamicViolation = new double[TOTAL_RUN]; // used for getting average
        double[] delayFogDynamicViolation = new double[TOTAL_RUN]; // used for getting average
        double[] costFogDynamicViolation = new double[TOTAL_RUN]; // used for getting average
        double[] violFogDynamicViolation = new double[TOTAL_RUN]; // used for getting average

        double sumTrafficPerNodePerApp = 0d; // used for getting average

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Tau\tTraffic\tDelay\tCost\tContainer\tViol\tDelay(Vonly)\tCost(Vonly)\tContainer(Vonly)\tViol(Vonly)\tViol_Slack=" + violationSlack + "\tThresh=" + Delay.getThresholdAverage());
        for (int Tau = MIN_CHANGE_INTERVAL; Tau <= MAX_CHANGE_INTERVAL; Tau += 5) {

            Parameters.TAU = Tau;
            q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;

            for (int i = 0; i < TOTAL_RUN; i++) {
                trafficPerNodePerApp = trafficRateSetter.nextRate();
                Traffic.distributeTraffic(trafficPerNodePerApp);
                sumTrafficPerNodePerApp += trafficPerNodePerApp;

                Traffic.setTrafficToGlobalTraffic(FogDynamic);
                Traffic.setTrafficToGlobalTraffic(FogDynamicViolation);
                if (i % q == 0) {
                    FDserviceCounter = FogDynamic.run(Traffic.COMBINED_APP_REGIONES, false);
                    FDVserviceCounter = FogDynamicViolation.run(Traffic.COMBINED_APP_REGIONES, true);

                    fogContainersDeployedFogDynamic[i] = FDserviceCounter.getDeployedFogServices();
                    cloudContainersDeployedFogDynamic[i] = FDserviceCounter.getDeployedCloudServices();

                    fogContainersDeployedFogDynamicViolation[i] = FDVserviceCounter.getDeployedFogServices();
                    cloudContainersDeployedFogDynamicViolation[i] = FDVserviceCounter.getDeployedCloudServices();
                }
                delayFogDynamic[i] = FogDynamic.getAvgServiceDelay();
                costFogDynamic[i] = FogDynamic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamic[i] = Violation.getViolationPercentage(FogDynamic);

                delayFogDynamicViolation[i] = FogDynamicViolation.getAvgServiceDelay();
                costFogDynamicViolation[i] = FogDynamicViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamicViolation[i] = Violation.getViolationPercentage(FogDynamicViolation);
            }

            System.out.print(Parameters.TAU + "\t" + ((sumTrafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) / (TOTAL_RUN))
                    + "\t" + Statistics.findAverageOfArray(delayFogDynamic)
                    + "\t" + (Statistics.findAverageOfArray(costFogDynamic) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + Statistics.findAverageOfArray(fogContainersDeployedFogDynamic)
                    + "\t" + Statistics.findAverageOfArray(cloudContainersDeployedFogDynamic)
                    + "\t" + Statistics.findAverageOfArray(violFogDynamic)
                    + "\t" + Statistics.findAverageOfArray(delayFogDynamicViolation)
                    + "\t" + (Statistics.findAverageOfArray(costFogDynamicViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + Statistics.findAverageOfArray(fogContainersDeployedFogDynamicViolation)
                    + "\t" + Statistics.findAverageOfArray(cloudContainersDeployedFogDynamicViolation)
                    + "\t" + Statistics.findAverageOfArray(violFogDynamicViolation)
            );

            if (Tau % 20 == 0) {
                System.out.print(
                        "\t" + Statistics.findStandardDeviationOfArray(delayFogDynamic)
                        + "\t" + (Statistics.findStandardDeviationOfArray(costFogDynamic) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                        + "\t" + Statistics.findStandardDeviationOfArray(fogContainersDeployedFogDynamic)
                        + "\t" + Statistics.findStandardDeviationOfArray(cloudContainersDeployedFogDynamic)
                        + "\t" + Statistics.findStandardDeviationOfArray(violFogDynamic)
                        + "\t" + Statistics.findStandardDeviationOfArray(delayFogDynamicViolation)
                        + "\t" + (Statistics.findStandardDeviationOfArray(costFogDynamicViolation) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                        + "\t" + Statistics.findStandardDeviationOfArray(fogContainersDeployedFogDynamicViolation)
                        + "\t" + Statistics.findStandardDeviationOfArray(cloudContainersDeployedFogDynamicViolation)
                        + "\t" + Statistics.findStandardDeviationOfArray(violFogDynamicViolation)
                );
            }
            System.out.println("");
            sumTrafficPerNodePerApp = 0d;

        }
    }

}
