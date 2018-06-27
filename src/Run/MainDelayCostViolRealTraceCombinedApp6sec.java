package Run;

import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Trace.CombinedAppTrace6secReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author ashkany
 */
public class MainDelayCostViolRealTraceCombinedApp6sec {

    private static int TOTAL_RUN ; 
    
    private static int index = 0;
    
    private final static int TAU = 18; // time interval between run of the heuristic (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 6; // time interval between run of the heuristic (s)

    
    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<Double[]> traceList = CombinedAppTrace6secReader.readTrafficFromFile();

        
        TOTAL_RUN = traceList.size(); // 4 hours of trace
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        
        int q = TAU / TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the heuristic

        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CombinedAppTrace6secReader.averagePerFogNode), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic.initializeStaticVariables();

        int containersDeployedAllCloud = 0;
        int containersDeployedAllFog = 0;
        int containersDeployedFogStatic = 0;
        int containersDeployedFogDynamic = 0;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;

        double violationSlack = Heuristic.getViolationSlack();
        Double[] combinedTrafficPerFogNode;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            combinedTrafficPerFogNode = nextRate(traceList);
            Heuristic.distributeTraffic(combinedTrafficPerFogNode);

            heuristicAllCloud.setTrafficToGlobalTraffic();
            containersDeployedAllCloud = heuristicAllCloud.run(Heuristic.COMBINED_APP, false);
            delayAllCloud = heuristicAllCloud.getAvgServiceDelay();
            costAllCloud = heuristicAllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = heuristicAllCloud.getViolationPercentage();

            heuristicAllFog.setTrafficToGlobalTraffic();
            containersDeployedAllFog = heuristicAllFog.run(Heuristic.COMBINED_APP, false);
            delayAllFog = heuristicAllFog.getAvgServiceDelay();
            costAllFog = heuristicAllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = heuristicAllFog.getViolationPercentage();

            heuristicFogStatic.setTrafficToGlobalTraffic();
            containersDeployedFogStatic = heuristicFogStatic.run(Heuristic.COMBINED_APP, false);
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

            System.out.println((totalTraffic(combinedTrafficPerFogNode) * Parameters.NUM_SERVICES) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud + "\t" + containersDeployedAllFog + "\t" + containersDeployedFogStatic + "\t" + containersDeployedFogDynamic
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic);

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
