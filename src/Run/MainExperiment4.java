package Run;

import Scheme.Parameters;
import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Components.Delay;
import Components.Method;
import Components.Traffic;
import Components.Violation;
import Utilities.Statistics;

/**
 *
 * @author Ashkan Y. 
 * 
 * This is the main class for experiment 4
 */
public class MainExperiment4 {

    private static int MAX_CHANGE_INTERVAL = 200;
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the method (s)
    private static int MIN_CHANGE_INTERVAL = 10;

    private final static int TOTAL_RUN = 1000;

    public static void main(String[] args) {
        // in each experiment, these parameters may vary
        Parameters.numCloudServers = 3;
        Parameters.numFogNodes = 15;
        Parameters.numServices = 50;

        Parameters.initialize();
        
        Traffic.TRAFFIC_ENLARGE_FACTOR = 4;

        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);

        int q; // the number of times that traffic changes between each run of the method
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;

        Method MinCost = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinViol = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        ServiceCounter MCserviceCounter = null, MVserviceCounter = null;

        Parameters.MEASURING_RUNNING_TIME = false;
        
        double[] fogContainersDeployedMinCost = new double[TOTAL_RUN]; // used for getting average
        double[] cloudContainersDeployedMinCost = new double[TOTAL_RUN]; // used for getting average
        double[] delayMinCost = new double[TOTAL_RUN]; // used for getting average
        double[] costMinCost = new double[TOTAL_RUN]; // used for getting average
        double[] violMinCost = new double[TOTAL_RUN]; // used for getting average

        double[] fogContainersDeployedMinViol = new double[TOTAL_RUN]; // used for getting average
        double[] cloudContainersDeployedMinViol = new double[TOTAL_RUN]; // used for getting average
        double[] delayMinViol = new double[TOTAL_RUN]; // used for getting average
        double[] costMinViol = new double[TOTAL_RUN]; // used for getting average
        double[] violMinViol = new double[TOTAL_RUN]; // used for getting average

        double sumTrafficPerNodePerApp = 0d; // used for getting average

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerService;

        System.out.println("Tau\tTraffic\tDelay\tCost\tContainer\tViol\tDelay(MV)\tCost(MV)\tContainer(MV)\tViol(MV)\tViol_Slack=" + violationSlack + "\tThresh=" + Delay.getThresholdAverage());
        for (int Tau = MIN_CHANGE_INTERVAL; Tau <= MAX_CHANGE_INTERVAL; Tau += 5) {

            Parameters.TAU = Tau; // set the reconfiguration intervals to the current value of the reconfiguration interval
            q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;

            for (int i = 0; i < TOTAL_RUN; i++) {
                trafficPerNodePerService = trafficRateSetter.nextRate(); // gets the next rate
                Traffic.distributeTraffic(trafficPerNodePerService);
                sumTrafficPerNodePerApp += trafficPerNodePerService;

                Traffic.setTrafficToGlobalTraffic(MinCost);
                Traffic.setTrafficToGlobalTraffic(MinViol);
                if (i % q == 0) {
                    MCserviceCounter = MinCost.run(Traffic.AGGREGATED, false);
                    MVserviceCounter = MinViol.run(Traffic.AGGREGATED, true);

                }
                fogContainersDeployedMinCost[i] = MCserviceCounter.getDeployedFogServices();
                cloudContainersDeployedMinCost[i] = MCserviceCounter.getDeployedCloudServices();

                fogContainersDeployedMinViol[i] = MVserviceCounter.getDeployedFogServices();
                cloudContainersDeployedMinViol[i] = MVserviceCounter.getDeployedCloudServices();
                delayMinCost[i] = MinCost.getAvgServiceDelay();
                costMinCost[i] = MinCost.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violMinCost[i] = Violation.getViolationPercentage(MinCost);

                delayMinViol[i] = MinViol.getAvgServiceDelay();
                costMinViol[i] = MinViol.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violMinViol[i] = Violation.getViolationPercentage(MinViol);
            }

            System.out.print(Parameters.TAU + "\t" + ((sumTrafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) / (TOTAL_RUN))
                    + "\t" + Statistics.findAverageOfArray(delayMinCost)
                    + "\t" + (Statistics.findAverageOfArray(costMinCost) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + Statistics.findAverageOfArray(fogContainersDeployedMinCost)
                    + "\t" + Statistics.findAverageOfArray(cloudContainersDeployedMinCost)
                    + "\t" + Statistics.findAverageOfArray(violMinCost)
                    + "\t" + Statistics.findAverageOfArray(delayMinViol)
                    + "\t" + (Statistics.findAverageOfArray(costMinViol) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + Statistics.findAverageOfArray(fogContainersDeployedMinViol)
                    + "\t" + Statistics.findAverageOfArray(cloudContainersDeployedMinViol)
                    + "\t" + Statistics.findAverageOfArray(violMinViol)
            );
            // prints standard deviation parameters only every 20 times
            if (Tau % 20 == 0) {
                System.out.print(
                        "\t" + Statistics.findStandardDeviationOfArray(delayMinCost)
                        + "\t" + (Statistics.findStandardDeviationOfArray(costMinCost) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                        + "\t" + Statistics.findStandardDeviationOfArray(fogContainersDeployedMinCost)
                        + "\t" + Statistics.findStandardDeviationOfArray(cloudContainersDeployedMinCost)
                        + "\t" + Statistics.findStandardDeviationOfArray(violMinCost)
                        + "\t" + Statistics.findStandardDeviationOfArray(delayMinViol)
                        + "\t" + (Statistics.findStandardDeviationOfArray(costMinViol) / Parameters.TRAFFIC_CHANGE_INTERVAL)
                        + "\t" + Statistics.findStandardDeviationOfArray(fogContainersDeployedMinViol)
                        + "\t" + Statistics.findStandardDeviationOfArray(cloudContainersDeployedMinViol)
                        + "\t" + Statistics.findStandardDeviationOfArray(violMinViol)
                );
            }
            System.out.println("");
            sumTrafficPerNodePerApp = 0d;
        }
    }

}
