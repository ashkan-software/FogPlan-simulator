package Run;

import Scheme.Parameters;
import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Method;
import Simulation.Traffic;
import Simulation.Violation;

/**
 * 
 * @author Ashkan Y.
 */
public class MainExperimentScalability {

    private final static int TOTAL_RUN = 2;
    private final static int TAU = 10; // time interval between run of the method (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 5; // time interval between run of the method (s)
    
    public static void main(String[] args) {

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
        

        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
        Method FogDynamicViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerApp = trafficRateSetter.nextRate();
            
            Traffic.distributeTraffic(trafficPerNodePerApp);


//            Traffic.setTrafficToGlobalTraffic(FogDynamic);
//            FogDynamic.run(Traffic.COMBINED_APP_REGIONES, false);

            Traffic.setTrafficToGlobalTraffic(FogDynamicViolation);
            FogDynamicViolation.run(Traffic.COMBINED_APP_REGIONES, true);

        }
    }

}
