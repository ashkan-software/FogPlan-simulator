package Run;

import Scheme.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Method;
import Simulation.Traffic;
import Simulation.Violation;
import Trace.AggregatedTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y. 
 */
public class MainExperiment1 {

    private static int TOTAL_RUN;
    
    private static int index = 0;
    
    public static boolean printCost = false;
    
    private static int TAU = 15 * 60; // time interval between run of the method (s)
    private static int TRAFFIC_CHANGE_INTERVAL = 15 * 60; // time interval between run of the method (sec)
    
    public static void main(String[] args) throws FileNotFoundException {

        Parameters.numCloudServers = 3;
        Parameters.numFogNodes = 10;
        Parameters.numServices = 40;
        Traffic.TRAFFIC_ENLARGE_FACTOR = 1;
        Parameters.initialize();
        
        ArrayList<Double> traceList = AggregatedTraceReader.readTrafficFromFile();

        TOTAL_RUN = traceList.size();
        
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the method
        
        
        Method AllCloud = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method AllFog = new Method(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogStatic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, AggregatedTraceReader.averageTrafficTrace), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamic = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
        Method FogStaticViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, AggregatedTraceReader.averageTrafficTrace), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method FogDynamicViolation = new Method(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        Method optimalPlacement = new Method(new ServiceDeployScheme(ServiceDeployScheme.OPTIMAL), Parameters.numFogNodes, Parameters.numServices, Parameters.numCloudServers);
        
        ServiceCounter containersDeployedAllCloud = null;
        ServiceCounter containersDeployedAllFog = null;
        ServiceCounter containersDeployedFogStatic = null;
        ServiceCounter containersDeployedFogDynamic = null ;
        ServiceCounter containersDeployedFogStaticViolation = null ;
        ServiceCounter containersDeployedFogDynamicViolation = null;
        ServiceCounter containersDeployedOptimalPlacement = null;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;
        double delayFogStaticViolation = 0;
        double delayFogDynamicViolation = 0;
        double delayOptimalPlacement = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;
        double costFogStaticViolation = 0;
        double costFogDynamicViolation = 0;
        double costOptimalPlacement = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;
        double violFogStaticViolation = 0;
        double violFogDynamicViolation = 0;
        double violOptimalPlacement = 0;

        double violationSlack = Violation.getViolationSlack();
        Double trafficPerNodePerService;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tD(OP)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tC(OP)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tCNT(OP)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tV(OP)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerService = nextRate(traceList);
            Traffic.distributeTraffic(trafficPerNodePerService);

            Traffic.setTrafficToGlobalTraffic(AllCloud);
            containersDeployedAllCloud = AllCloud.run(Traffic.COMBINED_APP_REGIONES, false);
            delayAllCloud = AllCloud.getAvgServiceDelay();
            costAllCloud = AllCloud.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = Violation.getViolationPercentage(AllCloud);

            Traffic.setTrafficToGlobalTraffic(AllFog);
            containersDeployedAllFog = AllFog.run(Traffic.COMBINED_APP_REGIONES, false);
            delayAllFog = AllFog.getAvgServiceDelay();
            costAllFog = AllFog.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = Violation.getViolationPercentage(AllFog);

            Traffic.setTrafficToGlobalTraffic(FogStatic);
            containersDeployedFogStatic = FogStatic.run(Traffic.COMBINED_APP_REGIONES, false);
            delayFogStatic = FogStatic.getAvgServiceDelay();
            costFogStatic = FogStatic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = Violation.getViolationPercentage(FogStatic);

            Traffic.setTrafficToGlobalTraffic(FogDynamic);
            if (i % q == 0) {
                containersDeployedFogDynamic = FogDynamic.run(Traffic.COMBINED_APP_REGIONES, false);
            }
            delayFogDynamic = FogDynamic.getAvgServiceDelay();
            costFogDynamic = FogDynamic.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = Violation.getViolationPercentage(FogDynamic);
            
            Traffic.setTrafficToGlobalTraffic(FogStaticViolation);
            containersDeployedFogStaticViolation = FogStaticViolation.run(Traffic.COMBINED_APP_REGIONES, true);
            delayFogStaticViolation = FogStaticViolation.getAvgServiceDelay();
            costFogStaticViolation = FogStaticViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStaticViolation = Violation.getViolationPercentage(FogStaticViolation);

            Traffic.setTrafficToGlobalTraffic(FogDynamicViolation);
            if (i % q == 0) {
                containersDeployedFogDynamicViolation = FogDynamicViolation.run(Traffic.COMBINED_APP_REGIONES, true);
            }
            delayFogDynamicViolation = FogDynamicViolation.getAvgServiceDelay();
            costFogDynamicViolation = FogDynamicViolation.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamicViolation = Violation.getViolationPercentage(FogDynamicViolation);

            containersDeployedOptimalPlacement = new ServiceCounter(0, 0);
//            printCost = false;
//            optimalPlacement.setTrafficToGlobalTraffic();
//            containersDeployedOptimalPlacement = optimalPlacement.run(Method.COMBINED_APP, true); // boolean will be ignored
//            delayOptimalPlacement = optimalPlacement.getAvgServiceDelay();
//            printCost = true;
//            costOptimalPlacement = optimalPlacement.getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
//            violOptimalPlacement = optimalPlacement.getViolationPercentage();
               
            System.out.println((trafficPerNodePerService * Parameters.numFogNodes * Parameters.numServices) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayFogStaticViolation + "\t" + delayFogDynamicViolation + "\t" + delayOptimalPlacement
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL)+ "\t" + (costOptimalPlacement / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedFogDynamic.getDeployedFogServices() + "\t" + containersDeployedFogStaticViolation.getDeployedFogServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedFogServices() + "\t" + containersDeployedOptimalPlacement.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamic.getDeployedCloudServices() + "\t" + containersDeployedFogStaticViolation.getDeployedCloudServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedCloudServices() + "\t" + containersDeployedOptimalPlacement.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic+ "\t" + violFogStaticViolation + "\t" + violFogDynamicViolation + "\t" + violOptimalPlacement);

            
        }
    }
    
    private static Double nextRate(ArrayList<Double> traceList){
        return traceList.get(index++);
    }
    

}
