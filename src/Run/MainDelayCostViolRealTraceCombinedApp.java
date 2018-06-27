package Run;

import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Trace.CombinedAppTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author ashkany
 */
public class MainDelayCostViolRealTraceCombinedApp {

    private static int TOTAL_RUN ; 
    
    private static int index = 0;
    
    private final static int TAU = 180; // time interval between run of the heuristic (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 60; // time interval between run of the heuristic (s)

    public static boolean printCost = true;
    
    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<Double[]> traceList = CombinedAppTraceReader.readTrafficFromFile();

        
        TOTAL_RUN = traceList.size(); // 4 hours of trace
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        
        int q = TAU / TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the heuristic

        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTraceReader.averagePerFogNode), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic optimalPlacement = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.OPTIMAL), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        
        Heuristic.initializeStaticVariables();

        int containersDeployedAllCloud = 0;
        int containersDeployedAllFog = 0;
        int containersDeployedFogStatic = 0;
        int containersDeployedFogDynamic = 0;
        int containersDeployedOptimalPlacement = 0;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;
        double delayOptimalPlacement = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;
        double costOptimalPlacement = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;
        double violOptimalPlacement = 0;

        double violationSlack = Heuristic.getViolationSlack();
        Double[] combinedTrafficPerFogNode;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(OP)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(OP)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(OP)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(OP)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            combinedTrafficPerFogNode = nextRate(traceList);
            Heuristic.distributeTraffic(combinedTrafficPerFogNode);

            heuristicAllCloud.setTrafficToGlobalTraffic();
            containersDeployedAllCloud = heuristicAllCloud.run(Heuristic.COMBINED_APP, false); // boolean will be ignored
            delayAllCloud = heuristicAllCloud.getAvgServiceDelay();
            costAllCloud = heuristicAllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = heuristicAllCloud.getViolationPercentage();

            heuristicAllFog.setTrafficToGlobalTraffic();
            containersDeployedAllFog = heuristicAllFog.run(Heuristic.COMBINED_APP, false); // boolean will be ignored
            delayAllFog = heuristicAllFog.getAvgServiceDelay();
            costAllFog = heuristicAllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = heuristicAllFog.getViolationPercentage();

            
            heuristicFogStatic.setTrafficToGlobalTraffic();
            containersDeployedFogStatic = heuristicFogStatic.run(Heuristic.COMBINED_APP, true);
            delayFogStatic = heuristicFogStatic.getAvgServiceDelay();
            costFogStatic = heuristicFogStatic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = heuristicFogStatic.getViolationPercentage();

            heuristicFogDynamic.setTrafficToGlobalTraffic();
            if (i % q == 0) {
                containersDeployedFogDynamic = heuristicFogDynamic.run(Heuristic.COMBINED_APP, false);
            }
            delayFogDynamic = heuristicFogDynamic.getAvgServiceDelay();
            costFogDynamic = heuristicFogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = heuristicFogDynamic.getViolationPercentage();
            
            printCost = false;
            optimalPlacement.setTrafficToGlobalTraffic();
            containersDeployedOptimalPlacement = optimalPlacement.run(Heuristic.COMBINED_APP, true); // boolean will be ignored
            delayOptimalPlacement = optimalPlacement.getAvgServiceDelay();
            printCost = true;
            costOptimalPlacement = optimalPlacement.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violOptimalPlacement = optimalPlacement.getViolationPercentage();

            System.out.println((totalTraffic(combinedTrafficPerFogNode) * Parameters.NUM_SERVICES) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayOptimalPlacement
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL)  + "\t" + (costOptimalPlacement / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud + "\t" + containersDeployedAllFog + "\t" + containersDeployedFogStatic + "\t" + containersDeployedFogDynamic + "\t" + containersDeployedOptimalPlacement
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic + "\t" + violOptimalPlacement);

        }
    }
    
    private static Double[] nextRate(ArrayList<Double[]> traceList){
        return traceList.get(index++);
    }

     private static double totalTraffic(Double[] traffic){
        double sum = 0;
        for (int j = 0; j < traffic.length; j++) {
                sum += traffic[j];
        }
        return sum;
    }

}
