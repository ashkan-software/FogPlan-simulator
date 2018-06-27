package Run;

import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Trace.CumulativeTraceReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author ashkany
 */
public class MainDelayCostViolRealTraceCumulative {

    private static int TOTAL_RUN;
    
    private static int index = 0;
    
    
    private static int TAU = 15 * 60; // time interval between run of the heuristic (s)
    private static int TRAFFIC_CHANGE_INTERVAL = 15 * 60; // time interval between run of the heuristic (s)
    
    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<Double> traceList = CumulativeTraceReader.readTrafficFromFile();

        TOTAL_RUN = traceList.size();
        
        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL; // the number of times that traffic changes between each run of the heuristic
        
        
        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CumulativeTraceReader.averageTrafficTrace), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        
        Heuristic heuristicFogStaticViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, CumulativeTraceReader.averageTrafficTrace), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        
        
        Heuristic.initializeStaticVariables();

        int containersDeployedAllCloud = 0;
        int containersDeployedAllFog = 0;
        int containersDeployedFogStatic = 0;
        int containersDeployedFogDynamic = 0;
        int containersDeployedFogStaticViolation = 0;
        int containersDeployedFogDynamicViolation = 0;

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

        double violationSlack = Heuristic.getViolationSlack();
        Double trafficPerNodePerApp;

        System.out.println("Traffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);
        for (int i = 0; i < TOTAL_RUN; i++) {
            trafficPerNodePerApp = nextRate(traceList);
            Heuristic.distributeTraffic(trafficPerNodePerApp);

            heuristicAllCloud.setTrafficToGlobalTraffic();
            containersDeployedAllCloud = heuristicAllCloud.run(Heuristic.COMBINED_APP_REGIONES, false);
            delayAllCloud = heuristicAllCloud.getAvgServiceDelay();
            costAllCloud = heuristicAllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllCloud = heuristicAllCloud.getViolationPercentage();

            heuristicAllFog.setTrafficToGlobalTraffic();
            containersDeployedAllFog = heuristicAllFog.run(Heuristic.COMBINED_APP_REGIONES, false);
            delayAllFog = heuristicAllFog.getAvgServiceDelay();
            costAllFog = heuristicAllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violAllFog = heuristicAllFog.getViolationPercentage();

            heuristicFogStatic.setTrafficToGlobalTraffic();
            containersDeployedFogStatic = heuristicFogStatic.run(Heuristic.COMBINED_APP_REGIONES, false);
            delayFogStatic = heuristicFogStatic.getAvgServiceDelay();
            costFogStatic = heuristicFogStatic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStatic = heuristicFogStatic.getViolationPercentage();

            heuristicFogDynamic.setTrafficToGlobalTraffic();
            if (i % q == 0) {
                containersDeployedFogDynamic = heuristicFogDynamic.run(Heuristic.COMBINED_APP_REGIONES, false);
            }
            delayFogDynamic = heuristicFogDynamic.getAvgServiceDelay();
            costFogDynamic = heuristicFogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamic = heuristicFogDynamic.getViolationPercentage();
            
            heuristicFogStaticViolation.setTrafficToGlobalTraffic();
            containersDeployedFogStaticViolation = heuristicFogStaticViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
            delayFogStaticViolation = heuristicFogStaticViolation.getAvgServiceDelay();
            costFogStaticViolation = heuristicFogStaticViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogStaticViolation = heuristicFogStaticViolation.getViolationPercentage();

            heuristicFogDynamicViolation.setTrafficToGlobalTraffic();
            if (i % q == 0) {
                containersDeployedFogDynamicViolation = heuristicFogDynamicViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
            }
            delayFogDynamicViolation = heuristicFogDynamicViolation.getAvgServiceDelay();
            costFogDynamicViolation = heuristicFogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
            violFogDynamicViolation = heuristicFogDynamicViolation.getViolationPercentage();

            System.out.println((trafficPerNodePerApp * Parameters.NUM_FOG_NODES * Parameters.NUM_SERVICES) + "\t" + delayAllCloud + "\t" + delayAllFog + "\t" + delayFogStatic + "\t" + delayFogDynamic + "\t" + delayFogStaticViolation + "\t" + delayFogDynamicViolation
                    + "\t" + (costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) + "\t" + (costFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL)
                    + "\t" + containersDeployedAllCloud + "\t" + containersDeployedAllFog + "\t" + containersDeployedFogStatic + "\t" + containersDeployedFogDynamic + "\t" + containersDeployedFogStaticViolation + "\t" + containersDeployedFogDynamicViolation
                    + "\t" + violAllCloud + "\t" + violAllFog + "\t" + violFogStatic + "\t" + violFogDynamic+ "\t" + violFogStaticViolation + "\t" + violFogDynamicViolation);

            
        }
    }
    
    private static Double nextRate(ArrayList<Double> traceList){
        return traceList.get(index++);
    }
    

}
