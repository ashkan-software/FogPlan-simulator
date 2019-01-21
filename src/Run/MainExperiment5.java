package Run;

import Scheme.Parameters;
import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceDeployMethod;
import Components.Method;
import Components.Traffic;
import Components.Violation;

/**
 *
 * @author Ashkan Y.
 *
 * This is the main class for experiment 5
 */
public class MainExperiment5 {

    private final static int TOTAL_RUN = 2;
    private final static int TAU = 10; // time interval between run of the method (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 5; // time interval between run of the method (s)

    public static void main(String[] args) {
        // in each experiment, these parameters may vary
        Parameters.MEASURING_RUNNING_TIME = true;

        Parameters.numCloudServers = 3;
        Parameters.numFogNodes = 100;
        Parameters.numServices = 10000;
        Traffic.TRAFFIC_ENLARGE_FACTOR = 1;
        // Need to change penalty of violation to a higher number 
        Parameters.initialize();

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method

        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);

        Method MinCost = new Method(new ServiceDeployMethod(ServiceDeployMethod.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method MinViol = new Method(new ServiceDeployMethod(ServiceDeployMethod.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerApp = trafficRateSetter.nextRate();
            Traffic.distributeTraffic(trafficPerNodePerApp);

            /*
             Toggle the comment for the below 4 lines (first 2 lines vs. last 2 lines) to measure the (total) runtime 
             */
            
//            Traffic.setTrafficToGlobalTraffic(FogDynamic);
//            FogDynamic.run(Traffic.AGGREGATED, false);
            Traffic.setTrafficToGlobalTraffic(MinViol);
            MinViol.run(Traffic.AGGREGATED, true);

        }
    }

}
