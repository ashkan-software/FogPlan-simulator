package Run;

import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Delay;
import Simulation.Heuristic;
import Simulation.Traffic;
import Simulation.Violation;

/**
 *
 * @author Ashkan Y. This main class runs the scheme for different intervals of
 * running the heuristic in the scheme
 */
public class MainSchemeReconfigIntervalDTMC {

    private static int MAX_HEURISTIC_CHANGE_INTERVAL = 250;
    protected final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the heuristic (s)
    private final static int TOTAL_RUN = 200;

    public static void main(String[] args) {

        int q;
        // the number of times that traffic changes between each run of the heuristic
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        Parameters.initialize();
        
        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);

        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        

        double sumFogContainersDeployedFogDynamic = 0d; // used for getting average
        double sumCloudContainersDeployedFogDynamic = 0d; // used for getting average
        double sumDelayFogDynamic = 0d; // used for getting average
        double sumCostFogDynamic = 0d; // used for getting average
        double sumViolFogDynamic = 0d; // used for getting average

        double sumFogContainersDeployedFogDynamicViolation = 0d; // used for getting average
        double sumCloudContainersDeployedFogDynamicViolation = 0d; // used for getting average
        double sumDelayFogDynamicViolation = 0d; // used for getting average
        double sumCostFogDynamicViolation = 0d; // used for getting average
        double sumViolFogDynamicViolation = 0d; // used for getting average

        double sumTrafficPerNodePerApp = 0d; // used for getting average

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Tau\tTraffic\tDelay\tCost\tContainer\tViol\tDelay(Vonly)\tCost(Vonly)\tContainer(Vonly)\tViol(Vonly)\tViol_Slack=" + violationSlack + "\tThresh=" + Delay.getThresholdAverage());
        for (int Tau = 10; Tau <= MAX_HEURISTIC_CHANGE_INTERVAL; Tau+=10) {
            
            Parameters.TAU = Tau;
            q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;

            for (int i = 0; i < TOTAL_RUN; i++) {
                trafficPerNodePerApp = trafficRateSetter.nextRate();
                Traffic.distributeTraffic(trafficPerNodePerApp);
                sumTrafficPerNodePerApp += trafficPerNodePerApp;

                Traffic.setTrafficToGlobalTraffic(heuristicFogDynamic);
                Traffic.setTrafficToGlobalTraffic(heuristicFogDynamicViolation);
                if (i % q == 0) {
                    ServiceCounter FDserviceCounter,FDVserviceCounter;
                    FDserviceCounter = heuristicFogDynamic.run(Traffic.COMBINED_APP_REGIONES, false);
                    FDVserviceCounter = heuristicFogDynamicViolation.run(Traffic.COMBINED_APP_REGIONES, true);
//                    System.out.println(a+" "+b);
                    sumFogContainersDeployedFogDynamic += FDserviceCounter.getDeployedFogServices();
                    sumCloudContainersDeployedFogDynamic += FDserviceCounter.getDeployedCloudServices();
                    
                    sumFogContainersDeployedFogDynamicViolation += FDVserviceCounter.getDeployedFogServices();
                    sumCloudContainersDeployedFogDynamicViolation += FDVserviceCounter.getDeployedCloudServices();
                }
                sumDelayFogDynamic += heuristicFogDynamic.getAvgServiceDelay();
                sumCostFogDynamic += heuristicFogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                sumViolFogDynamic += Violation.getViolationPercentage(heuristicFogDynamic);

                sumDelayFogDynamicViolation += heuristicFogDynamicViolation.getAvgServiceDelay();
                sumCostFogDynamicViolation += heuristicFogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                sumViolFogDynamicViolation += Violation.getViolationPercentage(heuristicFogDynamicViolation);
            }

            System.out.println(Parameters.TAU + "\t" + ((sumTrafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) / (TOTAL_RUN))
                    + "\t" + (sumDelayFogDynamic / TOTAL_RUN)
                    + "\t" + ((sumCostFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (sumFogContainersDeployedFogDynamic / TOTAL_RUN * q)
                    + "\t" + (sumCloudContainersDeployedFogDynamic / TOTAL_RUN * q)
                    + "\t" + (sumViolFogDynamic / TOTAL_RUN)
                    + "\t" + (sumDelayFogDynamicViolation / TOTAL_RUN)
                    + "\t" + ((sumCostFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (sumFogContainersDeployedFogDynamicViolation / TOTAL_RUN * q)
                    + "\t" + (sumCloudContainersDeployedFogDynamicViolation / TOTAL_RUN * q)
                    + "\t" + (sumViolFogDynamicViolation / TOTAL_RUN)
            );

            // reset the average parameters
            sumFogContainersDeployedFogDynamic = 0d;
            sumCloudContainersDeployedFogDynamic = 0d;
            sumDelayFogDynamic = 0d;
            sumCostFogDynamic = 0d;
            sumViolFogDynamic = 0d;

            sumFogContainersDeployedFogDynamicViolation = 0d;
            sumCloudContainersDeployedFogDynamicViolation = 0d;
            sumDelayFogDynamicViolation = 0d;
            sumCostFogDynamicViolation = 0d;
            sumViolFogDynamicViolation = 0d;

            sumTrafficPerNodePerApp = 0d;

        }
    }

}
