package Run;

import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Simulation.Traffic;
import Simulation.Violation;

/**
 * 
 * @author Ashkan Y.
 */
public class MainDelayCostViolDTMC {

    private final static int TOTAL_RUN = 200;
    private final static int TAU = 15; // time interval between run of the heuristic (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 5; // time interval between run of the heuristic (s)
    
    public static void main(String[] args) {

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the heuristic
        Parameters.initialize();
        
        
        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);
        

        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, dtmcConstructor.getAverageTrafficRate()), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
        Heuristic heuristicFogStaticViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, dtmcConstructor.getAverageTrafficRate()), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
        ServiceCounter containersDeployedAllCloud = null;
        ServiceCounter containersDeployedAllFog = null;
        ServiceCounter containersDeployedFogStatic = null;
        ServiceCounter containersDeployedFogDynamic = null ;
        ServiceCounter containersDeployedFogStaticViolation = null ;
        ServiceCounter containersDeployedFogDynamicViolation = null;

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

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerApp = trafficRateSetter.nextRate();
            
            Traffic.distributeTraffic(trafficPerNodePerApp);

            Traffic.setTrafficToGlobalTraffic(heuristicAllCloud);
            containersDeployedAllCloud = heuristicAllCloud.run(Traffic.COMBINED_APP_REGIONES, false);
            delayAllCloud = heuristicAllCloud.getAvgServiceDelay();
            costAllCloud = heuristicAllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = Violation.getViolationPercentage(heuristicAllCloud);
            
            Traffic.setTrafficToGlobalTraffic(heuristicAllFog);
            containersDeployedAllFog = heuristicAllFog.run(Traffic.COMBINED_APP_REGIONES, false);
            delayAllFog = heuristicAllFog.getAvgServiceDelay();
            costAllFog = heuristicAllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = Violation.getViolationPercentage(heuristicAllFog);

            Traffic.setTrafficToGlobalTraffic(heuristicFogStatic);
            containersDeployedFogStatic = heuristicFogStatic.run(Traffic.COMBINED_APP_REGIONES, false);
            delayFogStatic = heuristicFogStatic.getAvgServiceDelay();
            costFogStatic = heuristicFogStatic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = Violation.getViolationPercentage(heuristicFogStatic);

            Traffic.setTrafficToGlobalTraffic(heuristicFogDynamic);
            if (i % q == 0) {
                containersDeployedFogDynamic = heuristicFogDynamic.run(Traffic.COMBINED_APP_REGIONES, false);
            }
            delayFogDynamic = heuristicFogDynamic.getAvgServiceDelay();
            costFogDynamic = heuristicFogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = Violation.getViolationPercentage(heuristicFogDynamic);
            
            Traffic.setTrafficToGlobalTraffic(heuristicFogStaticViolation);
            containersDeployedFogStaticViolation = heuristicFogStaticViolation.run(Traffic.COMBINED_APP_REGIONES, true);
            delayFogStaticViolation = heuristicFogStaticViolation.getAvgServiceDelay();
            costFogStaticViolation = heuristicFogStaticViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStaticViolation = Violation.getViolationPercentage(heuristicFogStaticViolation);

            Traffic.setTrafficToGlobalTraffic(heuristicFogDynamicViolation);
            if (i % q == 0) {
                containersDeployedFogDynamicViolation = heuristicFogDynamicViolation.run(Traffic.COMBINED_APP_REGIONES, true);
            }
            delayFogDynamicViolation = heuristicFogDynamicViolation.getAvgServiceDelay();
            costFogDynamicViolation = heuristicFogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamicViolation = Violation.getViolationPercentage(heuristicFogDynamicViolation);
      
            System.out.println((trafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayFogStaticViolation + "\t" + delayFogDynamicViolation
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedFogDynamic.getDeployedFogServices() + "\t" + containersDeployedFogStaticViolation.getDeployedFogServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamic.getDeployedCloudServices() + "\t" + containersDeployedFogStaticViolation.getDeployedCloudServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic+ "\t" + violFogStaticViolation + "\t" + violFogDynamicViolation);

        }
    }

}
