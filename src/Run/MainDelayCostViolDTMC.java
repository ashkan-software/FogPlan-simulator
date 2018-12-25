package Run;

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
public class MainDelayCostViolDTMC {

    private final static int TOTAL_RUN = 75;
    private final static int TAU = 15; // time interval between run of the method (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 5; // time interval between run of the method (s)
    
    public static void main(String[] args) {

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method
        Parameters.initialize();
        
        
        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);
        

        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, dtmcConstructor.getAverageTrafficRate()), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
        Method FogStaticViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, dtmcConstructor.getAverageTrafficRate()), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamicViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
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

            Traffic.setTrafficToGlobalTraffic(AllCloud);
            containersDeployedAllCloud = AllCloud.run(Traffic.COMBINED_APP_REGIONES, false);
            delayAllCloud = AllCloud.getAvgServiceDelay();
            costAllCloud = AllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = Violation.getViolationPercentage(AllCloud);
            
            Traffic.setTrafficToGlobalTraffic(AllFog);
            containersDeployedAllFog = AllFog.run(Traffic.COMBINED_APP_REGIONES, false);
            delayAllFog = AllFog.getAvgServiceDelay();
            costAllFog = AllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = Violation.getViolationPercentage(AllFog);

            Traffic.setTrafficToGlobalTraffic(FogStatic);
            containersDeployedFogStatic = FogStatic.run(Traffic.COMBINED_APP_REGIONES, false);
            delayFogStatic = FogStatic.getAvgServiceDelay();
            costFogStatic = FogStatic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = Violation.getViolationPercentage(FogStatic);

            Traffic.setTrafficToGlobalTraffic(FogDynamic);
            if (i % q == 0) {
                containersDeployedFogDynamic = FogDynamic.run(Traffic.COMBINED_APP_REGIONES, false);
            }
            delayFogDynamic = FogDynamic.getAvgServiceDelay();
            costFogDynamic = FogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = Violation.getViolationPercentage(FogDynamic);
            
            Traffic.setTrafficToGlobalTraffic(FogStaticViolation);
            containersDeployedFogStaticViolation = FogStaticViolation.run(Traffic.COMBINED_APP_REGIONES, true);
            delayFogStaticViolation = FogStaticViolation.getAvgServiceDelay();
            costFogStaticViolation = FogStaticViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStaticViolation = Violation.getViolationPercentage(FogStaticViolation);

            Traffic.setTrafficToGlobalTraffic(FogDynamicViolation);
            if (i % q == 0) {
                containersDeployedFogDynamicViolation = FogDynamicViolation.run(Traffic.COMBINED_APP_REGIONES, true);
            }
            delayFogDynamicViolation = FogDynamicViolation.getAvgServiceDelay();
            costFogDynamicViolation = FogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamicViolation = Violation.getViolationPercentage(FogDynamicViolation);
      
            System.out.println((trafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices) 
                    + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayFogStaticViolation + "\t" + delayFogDynamicViolation
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedFogDynamic.getDeployedFogServices() + "\t" + containersDeployedFogStaticViolation.getDeployedFogServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamic.getDeployedCloudServices() + "\t" + containersDeployedFogStaticViolation.getDeployedCloudServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic+ "\t" + violFogStaticViolation + "\t" + violFogDynamicViolation);

        }
    }

}
