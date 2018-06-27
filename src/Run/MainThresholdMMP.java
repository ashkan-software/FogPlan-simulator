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
public class MainThresholdMMP {

    private static int MAX_THRESHOLD = 80;
    private final static int TOTAL_RUN = 5000;
    
    private final static int TAU = 30; // time interval between run of the heuristic (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the heuristic (s)

    public static void main(String[] args) {

        Parameters.TAU = TAU;
        Parameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = Parameters.TAU / Parameters.TRAFFIC_CHANGE_INTERVAL;
        // the number of times that traffic changes between each run of the heuristic

        MMPconstructor mmpConstructor = new MMPconstructor();
        MMPsimulator trafficRateSetter = new MMPsimulator(mmpConstructor.mmp);

        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, mmpConstructor.getAverageTrafficRate()), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);

        Heuristic heuristicFogStaticViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, mmpConstructor.getAverageTrafficRate()), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), Parameters.NUM_FOG_NODES, Parameters.NUM_SERVICES, Parameters.NUM_CLOUD_SERVERS);

        Heuristic.initializeStaticVariables();

        // used for getting average
        double containersDeployedAllCloud = 0;
        double containersDeployedAllFog = 0;
        double containersDeployedFogStatic = 0;
        double containersDeployedFogDynamic = 0;
        double containersDeployedFogStaticViolation = 0;
        double containersDeployedFogDynamicViolation = 0;

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

        double sumTrafficPerNodePerApp = 0; // used for getting average

        double violationSlack = Heuristic.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Threshold\tTraffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);
        
        for (int threshold = 5; threshold <= MAX_THRESHOLD; threshold = threshold + 1) {

            Heuristic.setThresholds(threshold);
            heuristicFogStatic.setFirstTimeBoolean();
            heuristicFogStaticViolation.setFirstTimeBoolean();

            for (int i = 0; i < TOTAL_RUN; i++) {

                trafficPerNodePerApp = trafficRateSetter.nextRate();
                Heuristic.distributeTraffic(trafficPerNodePerApp);

                sumTrafficPerNodePerApp += trafficPerNodePerApp;

                heuristicAllCloud.setTrafficToGlobalTraffic();
                containersDeployedAllCloud += heuristicAllCloud.run(Heuristic.COMBINED_APP_REGIONES, false);
                delayAllCloud += heuristicAllCloud.getAvgServiceDelay();
                costAllCloud += heuristicAllCloud.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violAllCloud += heuristicAllCloud.getViolationPercentage();

                heuristicAllFog.setTrafficToGlobalTraffic();
                containersDeployedAllFog += heuristicAllFog.run(Heuristic.COMBINED_APP_REGIONES, false);
                delayAllFog += heuristicAllFog.getAvgServiceDelay();
                costAllFog += heuristicAllFog.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violAllFog += heuristicAllFog.getViolationPercentage();

                heuristicFogStatic.setTrafficToGlobalTraffic();
                containersDeployedFogStatic += heuristicFogStatic.run(Heuristic.COMBINED_APP_REGIONES, false);
                delayFogStatic += heuristicFogStatic.getAvgServiceDelay();
                costFogStatic += heuristicFogStatic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStatic += heuristicFogStatic.getViolationPercentage();

                heuristicFogDynamic.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    containersDeployedFogDynamic += heuristicFogDynamic.run(Heuristic.COMBINED_APP_REGIONES, false);
                }
                delayFogDynamic += heuristicFogDynamic.getAvgServiceDelay();
                costFogDynamic += heuristicFogDynamic.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamic += heuristicFogDynamic.getViolationPercentage();

                heuristicFogStaticViolation.setTrafficToGlobalTraffic();
                containersDeployedFogStaticViolation += heuristicFogStaticViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
                delayFogStaticViolation += heuristicFogStaticViolation.getAvgServiceDelay();
                costFogStaticViolation += heuristicFogStaticViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStaticViolation += heuristicFogStaticViolation.getViolationPercentage();

                heuristicFogDynamicViolation.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    containersDeployedFogDynamicViolation += heuristicFogDynamicViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
                }
                delayFogDynamicViolation += heuristicFogDynamicViolation.getAvgServiceDelay();
                costFogDynamicViolation += heuristicFogDynamicViolation.getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamicViolation += heuristicFogDynamicViolation.getViolationPercentage();

            }

            System.out.println(threshold + "\t" + ((sumTrafficPerNodePerApp * Parameters.NUM_FOG_NODES * Parameters.NUM_SERVICES) / (TOTAL_RUN))
                    + "\t" + (delayAllCloud / TOTAL_RUN) + "\t" + (delayAllFog / TOTAL_RUN) + "\t" + (delayFogStatic / TOTAL_RUN) + "\t" + (delayFogDynamic / TOTAL_RUN) + "\t" + (delayFogStaticViolation / TOTAL_RUN) + "\t" + (delayFogDynamicViolation / TOTAL_RUN)
                    + "\t" + ((costAllCloud / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costAllFog / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogStatic / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogDynamic / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogStaticViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogDynamicViolation / Parameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (containersDeployedAllCloud / TOTAL_RUN) + "\t" + (containersDeployedAllFog / TOTAL_RUN) + "\t" + (containersDeployedFogStatic / TOTAL_RUN) + "\t" + (containersDeployedFogDynamic / TOTAL_RUN) + "\t" + (containersDeployedFogStaticViolation / TOTAL_RUN) + "\t" + (containersDeployedFogDynamicViolation / TOTAL_RUN)
                    + "\t" + (violAllCloud / TOTAL_RUN) + "\t" + (violAllFog / TOTAL_RUN) + "\t" + (violFogStatic / TOTAL_RUN) + "\t" + (violFogDynamic / TOTAL_RUN) + "\t" + (violFogStaticViolation / TOTAL_RUN) + "\t" + (violFogDynamicViolation / TOTAL_RUN));

            // reset the average parameters
            containersDeployedAllCloud = 0;
            containersDeployedAllFog = 0;
            containersDeployedFogStatic = 0;
            containersDeployedFogDynamic = 0;
            containersDeployedFogStaticViolation = 0;
            containersDeployedFogDynamicViolation = 0;

            delayAllCloud = 0;
            delayAllFog = 0;
            delayFogStatic = 0;
            delayFogDynamic = 0;
            delayFogStaticViolation = 0;
            delayFogDynamicViolation = 0;

            costAllCloud = 0;
            costAllFog = 0;
            costFogStatic = 0;
            costFogDynamic = 0;
            costFogStaticViolation = 0;
            costFogDynamicViolation = 0;

            violAllCloud = 0;
            violAllFog = 0;
            violFogStatic = 0;
            violFogDynamic = 0;
            violFogStaticViolation = 0;
            violFogDynamicViolation = 0;

            sumTrafficPerNodePerApp = 0;

        }
    }

}
