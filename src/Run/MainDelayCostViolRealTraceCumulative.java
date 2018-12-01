package Run;

import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Trace.CumulativeTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author Ashkan Y. 
 */
public class MainDelayCostViolRealTraceCumulative {

    private static int TOTAL_RUN;
    
    private static int index = 0;
    
    public static boolean printCost = false;
    
    private static int TAU = 15 * 60; // time interval between run of the heuristic (s)
    private static int TRAFFIC_CHANGE_INTERVAL = 15 * 60; // time interval between run of the heuristic (sec)
    
    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<Double> traceList = CumulativeTraceReader.readTrafficFromFile();

        TOTAL_RUN = traceList.size();
        
        RunParameters.TAU = TAU;
        RunParameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = RunParameters.TAU / RunParameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the heuristic
        
        
        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CumulativeTraceReader.averageTrafficTrace), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        
        Heuristic heuristicFogStaticViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CumulativeTraceReader.averageTrafficTrace), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic optimalPlacement = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.OPTIMAL), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        
        Heuristic.initializeStaticVariables();

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

        double violationSlack = Heuristic.getViolationSlack();
        Double trafficPerNodePerApp;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tD(OP)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tC(OP)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tCNT(OP)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tV(OP)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerApp = nextRate(traceList);
            Heuristic.distributeTraffic(trafficPerNodePerApp);

            heuristicAllCloud.setTrafficToGlobalTraffic();
            containersDeployedAllCloud = heuristicAllCloud.run(Heuristic.COMBINED_APP_REGIONES, false);
            delayAllCloud = heuristicAllCloud.getAvgServiceDelay();
            costAllCloud = heuristicAllCloud.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = heuristicAllCloud.getViolationPercentage();

            heuristicAllFog.setTrafficToGlobalTraffic();
            containersDeployedAllFog = heuristicAllFog.run(Heuristic.COMBINED_APP_REGIONES, false);
            delayAllFog = heuristicAllFog.getAvgServiceDelay();
            costAllFog = heuristicAllFog.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = heuristicAllFog.getViolationPercentage();

            heuristicFogStatic.setTrafficToGlobalTraffic();
            containersDeployedFogStatic = heuristicFogStatic.run(Heuristic.COMBINED_APP_REGIONES, false);
            delayFogStatic = heuristicFogStatic.getAvgServiceDelay();
            costFogStatic = heuristicFogStatic.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = heuristicFogStatic.getViolationPercentage();

            heuristicFogDynamic.setTrafficToGlobalTraffic();
            if (i % q == 0) {
                containersDeployedFogDynamic = heuristicFogDynamic.run(Heuristic.COMBINED_APP_REGIONES, false);
            }
            delayFogDynamic = heuristicFogDynamic.getAvgServiceDelay();
            costFogDynamic = heuristicFogDynamic.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = heuristicFogDynamic.getViolationPercentage();
            
            heuristicFogStaticViolation.setTrafficToGlobalTraffic();
            containersDeployedFogStaticViolation = heuristicFogStaticViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
            delayFogStaticViolation = heuristicFogStaticViolation.getAvgServiceDelay();
            costFogStaticViolation = heuristicFogStaticViolation.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStaticViolation = heuristicFogStaticViolation.getViolationPercentage();

            heuristicFogDynamicViolation.setTrafficToGlobalTraffic();
            if (i % q == 0) {
                containersDeployedFogDynamicViolation = heuristicFogDynamicViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
            }
            delayFogDynamicViolation = heuristicFogDynamicViolation.getAvgServiceDelay();
            costFogDynamicViolation = heuristicFogDynamicViolation.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamicViolation = heuristicFogDynamicViolation.getViolationPercentage();

            containersDeployedOptimalPlacement = new ServiceCounter(0, 0);
//            printCost = false;
//            optimalPlacement.setTrafficToGlobalTraffic();
//            containersDeployedOptimalPlacement = optimalPlacement.run(Heuristic.COMBINED_APP, true); // boolean will be ignored
//            delayOptimalPlacement = optimalPlacement.getAvgServiceDelay();
//            printCost = true;
//            costOptimalPlacement = optimalPlacement.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
//            violOptimalPlacement = optimalPlacement.getViolationPercentage();
               
            System.out.println((trafficPerNodePerApp * RunParameters.NUM_FOG_NODES * RunParameters.NUM_SERVICES) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayFogStaticViolation + "\t" + delayFogDynamicViolation + "\t" + delayOptimalPlacement
                    + "\t" + (costAllCloud / RunParameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / RunParameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / RunParameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / RunParameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStaticViolation / RunParameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamicViolation / RunParameters.TRAFFIC_CHANGE_INTERVAL)+ "\t" + (costOptimalPlacement / RunParameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud.getDeployedFogServices() + "\t" + containersDeployedAllFog.getDeployedFogServices() + "\t" + containersDeployedFogStatic.getDeployedFogServices() + "\t" + containersDeployedFogDynamic.getDeployedFogServices() + "\t" + containersDeployedFogStaticViolation.getDeployedFogServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedFogServices() + "\t" + containersDeployedOptimalPlacement.getDeployedFogServices()
                    + "\t" + containersDeployedAllCloud.getDeployedCloudServices() + "\t" + containersDeployedAllFog.getDeployedCloudServices() + "\t" + containersDeployedFogStatic.getDeployedCloudServices() + "\t" + containersDeployedFogDynamic.getDeployedCloudServices() + "\t" + containersDeployedFogStaticViolation.getDeployedCloudServices() + "\t" + containersDeployedFogDynamicViolation.getDeployedCloudServices() + "\t" + containersDeployedOptimalPlacement.getDeployedCloudServices()
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic+ "\t" + violFogStaticViolation + "\t" + violFogDynamicViolation + "\t" + violOptimalPlacement);

            
        }
    }
    
    private static Double nextRate(ArrayList<Double> traceList){
        return traceList.get(index++);
    }
    

}
