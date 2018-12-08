/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation;

import Run.Parameters;
import Utilities.ArrayFiller;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ashkany
 *
 * This class has the functions and variables related to calculating delay
 */
public class Traffic {

    protected double lambda_in[][]; // lambda^in_aj
    protected double lambdap_in[][]; // lambda'^in_ak

    protected double lambda_out[][]; // lambda^out_aj

    protected double arrivalCloud[][]; // LAMBDA_ak
    protected double arrivalFog[][]; // LAMBDA_aj

    public static int COMBINED_APP_REGIONES = 1;
    public static int COMBINED_APP = 2;
    public static int NOT_COMBINED = 3;

    public Traffic() {
        lambda_in = new double[Parameters.numServices][Parameters.numFogNodes];
        lambdap_in = new double[Parameters.numServices][Parameters.numCloudServers];
        lambda_out = new double[Parameters.numServices][Parameters.numFogNodes];
        arrivalCloud = new double[Parameters.numServices][Parameters.numCloudServers];
        arrivalFog = new double[Parameters.numServices][Parameters.numFogNodes];
    }

    protected static void backupIncomingTraffic(Heuristic heuristic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                heuristic.backup_lambda_in[a][j] = heuristic.traffic.lambda_in[a][j];
            }
        }
    }

    protected static void restoreIncomingTraffic(Heuristic heuristic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                heuristic.traffic.lambda_in[a][j] = heuristic.backup_lambda_in[a][j];
            }
        }
    }

    /**
     * gets incoming traffic to all fog nodes for all services
     *
     * @param a index of a given service
     * @return returns an array of FogTrafficIndex, for a given service
     */
    protected static List<FogTrafficIndex> getFogIncomingTraffic(int a, boolean isSortAscending, Heuristic heuristic) {

        List<FogTrafficIndex> fogTrafficIndex = new ArrayList<>();
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            fogTrafficIndex.add(new FogTrafficIndex(j, heuristic.traffic.lambda_in[a][j], isSortAscending));
        }
        return fogTrafficIndex;
    }

    protected static void calcNormalizedArrivalRateFogNodes(Heuristic heuristic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                calcNormalizedArrivalRateFogNode(a, j, heuristic);
            }
        }

    }

    protected static void calcNormalizedArrivalRateFogNode(int a, int j, Heuristic heuristic) {
        heuristic.traffic.arrivalFog[a][j] = Parameters.L_P[a] * heuristic.traffic.lambda_in[a][j] * heuristic.x[a][j];
    }
    
    protected static void calcNormalizedArrivalRateCloudNodes(Heuristic heuristic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                calcNormalizedArrivalRateFogNode(a, j, heuristic);
            }
        }

    }

    protected static void calcNormalizedArrivalRateCloudNode(int a, int j, Heuristic heuristic) {
        heuristic.traffic.arrivalFog[a][j] = Parameters.L_P[a] * heuristic.traffic.lambda_in[a][j] * heuristic.x[a][j];
    }

   

    /**
     * calculate lambda^out_aj and lambdap_in_ak for cloud server k for service
     * a
     *
     * @param k
     */
    private static void calcArrivalRateCloudFromFogNodesForService(int k, int a, Heuristic heuristic) {
        double tempSum = 0;
        for (Integer j : Parameters.h_reverse.get(k)) {
            heuristic.traffic.lambda_out[a][j] = heuristic.traffic.lambda_in[a][j] * (1 - heuristic.x[a][j]); // calculate lambda^out_aj
            tempSum += heuristic.traffic.lambda_out[a][j];
        }
        heuristic.traffic.lambdap_in[a][k] = tempSum;
    }

    public void printTraffic(Heuristic heuristic) {
        DecimalFormat df = new DecimalFormat("0.00");
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {

                System.out.print(df.format(heuristic.traffic.lambda_in[a][j]) + " ");
            }
            System.out.println("");
        }
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic to the average traffic values, so that the
     * placement solves the problem based on average
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimeCombined(Heuristic heuristic) {
        distributeTraffic(heuristic.scheme.averageRateOfTraffic, heuristic.traffic.lambda_in);
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic per fog node to the average traffic values, so
     * that the placement solves the problem based on averages
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode(Heuristic heuristic) {
        distributeTraffic(heuristic.scheme.averageRateOfCombinedAppTrafficPerNode, heuristic.traffic.lambda_in);
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic per fog node per service to the average traffic
     * values, so that the placement solves the problem based on averages
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode(Heuristic heuristic) {
        setTraffic(heuristic.scheme.averageRateOfTrafficPerNodePerService, heuristic.traffic.lambda_in);
    }

    private static void distributeTraffic(double trafficPerNodePerApp, double[][] targetTraffic) {
        double totalTraffic = trafficPerNodePerApp * Parameters.numFogNodes * Parameters.numServices;
        double trafficForCurrentService;
        double[] fogTrafficPercentage = new double[Parameters.numFogNodes];
        for (int a = 0; a < Parameters.numServices; a++) {
            trafficForCurrentService = totalTraffic * Parameters.ServiceTrafficPercentage[a];
            ArrayFiller.generateRandomDistributionOnArray(fogTrafficPercentage, 1, 7);
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                targetTraffic[a][j] = trafficForCurrentService * fogTrafficPercentage[j];
            }
        }
    }

    public static void distributeTraffic(double trafficPerNodePerApp) {
        distributeTraffic(trafficPerNodePerApp, Parameters.globalTraffic);
    }

    private static void distributeTraffic(Double[] combinedTrafficPerFogNode, double[][] targetTraffic) {
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            for (int a = 0; a < Parameters.numServices; a++) {
                targetTraffic[a][j] = combinedTrafficPerFogNode[j] * Parameters.ServiceTrafficPercentage[a];
            }
        }
    }

    public static void distributeTraffic(Double[] combinedTrafficPerFogNode) {
        distributeTraffic(combinedTrafficPerFogNode, Parameters.globalTraffic);
    }

    private static void setTraffic(Double[][] newTraffic, double[][] targetTraffic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                targetTraffic[a][j] = newTraffic[a][j];
            }
        }
    }

    public static void setTraffic(Double[][] actualTraffic) {
        setTraffic(actualTraffic, Parameters.globalTraffic);
    }

    public static void setTrafficToGlobalTraffic(Heuristic heuristic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                heuristic.traffic.lambda_in[a][j] = Parameters.globalTraffic[a][j];
            }
        }
    }
}
