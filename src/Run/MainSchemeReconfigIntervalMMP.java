package Run;

import MMP.MMPconstructor;
import MMP.MMPsimulator;
import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;

/**
 *
 * @author ashkany This main class runs the scheme for different intervals of
 * running the heuristic in the scheme
 */
public class MainSchemeReconfigIntervalMMP {

    private static int MAX_HEURISTIC_CHANGE_INTERVAL = 250;
    protected final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the heuristic (s)
    private final static int TOTAL_RUN = 10;

    public static void main(String[] args) {

        int q;
        // the number of times that traffic changes between each run of the heuristic
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;

        MMPconstructor mmpConstructor = new MMPconstructor();
        MMPsimulator trafficRateSetter = new MMPsimulator(mmpConstructor.mmp);

        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic.initializeStaticVariables();

        double sumContainersDeployedFogDynamic = 0d; // used for getting average
        double sumDelayFogDynamic = 0d; // used for getting average
        double sumCostFogDynamic = 0d; // used for getting average
        double sumViolFogDynamic = 0d; // used for getting average

        double sumContainersDeployedFogDynamicViolation = 0d; // used for getting average
        double sumDelayFogDynamicViolation = 0d; // used for getting average
        double sumCostFogDynamicViolation = 0d; // used for getting average
        double sumViolFogDynamicViolation = 0d; // used for getting average

        double sumTrafficPerNodePerApp = 0d; // used for getting average

        double violationSlack = Heuristic.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Tau\tTraffic\tDelay\tCost\tContainer\tViol\tDelay(Vonly)\tCost(Vonly)\tContainer(Vonly)\tViol(Vonly)\tViol_Slack=" + violationSlack + "\tThresh=" + Heuristic.getThresholdAverage());
        for (int Tau = 10; Tau <= MAX_HEURISTIC_CHANGE_INTERVAL; Tau+=10) {
            
            Parameters.TAU = Tau;
            q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;

            for (int i = 0; i < TOTAL_RUN; i++) {
                trafficPerNodePerApp = trafficRateSetter.nextRate();
                Heuristic.distributeTraffic(trafficPerNodePerApp);
                sumTrafficPerNodePerApp += trafficPerNodePerApp;

                heuristicFogDynamic.setTrafficToGlobalTraffic();
                heuristicFogDynamicViolation.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    double a,b;
                    a = heuristicFogDynamic.run(Heuristic.COMBINED_APP_REGIONES, false);
                    b = heuristicFogDynamicViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
//                    System.out.println(a+" "+b);
                    sumContainersDeployedFogDynamic += a;
                    sumContainersDeployedFogDynamicViolation += b;
                }
                sumDelayFogDynamic += heuristicFogDynamic.getAvgServiceDelay();
                sumCostFogDynamic += heuristicFogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                sumViolFogDynamic += heuristicFogDynamic.getViolationPercentage();

                sumDelayFogDynamicViolation += heuristicFogDynamicViolation.getAvgServiceDelay();
                sumCostFogDynamicViolation += heuristicFogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                sumViolFogDynamicViolation += heuristicFogDynamicViolation.getViolationPercentage();
            }

            System.out.println(Parameters.TAU + "\t" + ((sumTrafficPerNodePerApp * Parameters.NUM_FOG_NODES * Parameters.NUM_SERVICES) / (TOTAL_RUN))
                    + "\t" + (sumDelayFogDynamic / TOTAL_RUN)
                    + "\t" + ((sumCostFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (sumContainersDeployedFogDynamic / TOTAL_RUN * q)
                    + "\t" + (sumViolFogDynamic / TOTAL_RUN)
                    + "\t" + (sumDelayFogDynamicViolation / TOTAL_RUN)
                    + "\t" + ((sumCostFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (sumContainersDeployedFogDynamicViolation / TOTAL_RUN * q)
                    + "\t" + (sumViolFogDynamicViolation / TOTAL_RUN)
            );

            // reset the average parameters
            sumContainersDeployedFogDynamic = 0d;
            sumDelayFogDynamic = 0d;
            sumCostFogDynamic = 0d;
            sumViolFogDynamic = 0d;

            sumContainersDeployedFogDynamicViolation = 0d;
            sumDelayFogDynamicViolation = 0d;
            sumCostFogDynamicViolation = 0d;
            sumViolFogDynamicViolation = 0d;

            sumTrafficPerNodePerApp = 0d;

        }
    }

}
