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
    
    
    public static int COMBINED_APP_REGIONES = 1;
    public static int COMBINED_APP = 2;
    public static int NOT_COMBINED = 3;

    protected static void backupIncomingTraffic(Heuristic heuristic) {
        for (int a = 0; a < Heuristic.numServices; a++) {
            for (int j = 0; j < Heuristic.numFogNodes; j++) {
                heuristic.backup_lambda_in[a][j] = heuristic.lambda_in[a][j];
            }
        }
    }

    protected static void restoreIncomingTraffic(Heuristic heuristic) {
        for (int a = 0; a < Heuristic.numServices; a++) {
            for (int j = 0; j < Heuristic.numFogNodes; j++) {
                heuristic.lambda_in[a][j] = heuristic.backup_lambda_in[a][j];
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
        for (int j = 0; j < Heuristic.numFogNodes; j++) {
            fogTrafficIndex.add(new FogTrafficIndex(j, heuristic.lambda_in[a][j], isSortAscending));
        }
        return fogTrafficIndex;
    }

    protected static void calcNormalizedArrivalRateFogNodes(Heuristic heuristic) {
        for (int j = 0; j < Heuristic.numFogNodes; j++) {
            calcNormalizedArrivalRateFogNode(j, heuristic);
        }
    }

    protected static void calcNormalizedArrivalRateFogNode(int j, Heuristic heuristic) {
        double tempSum = 0;
        for (int a = 0; a < Heuristic.numServices; a++) {
            tempSum += Parameters.L_P[a] * heuristic.lambda_in[a][j] * heuristic.x[a][j];
        }
        heuristic.arrivalFog[j] = tempSum;
    }

    protected static void calcNormalizedArrivalRateCloudNodes(Heuristic heuristic) {
        for (int k = 0; k < Heuristic.numCloudServers; k++) {
            calcNormalizedArrivalRateCloudNode(k, heuristic);
        }
    }

    protected static void calcNormalizedArrivalRateCloudNode(int k, Heuristic heuristic) {
        double tempSum = 0;
        for (int a = 0; a < Heuristic.numServices; a++) {
            calcArrivalRateCloudFromFogNodesForService(k, a, heuristic);
            tempSum += Parameters.L_P[a] * heuristic.lambdap_in[a][k] * heuristic.xp[a][k];
        }
        heuristic.arrivalCloud[k] = tempSum;
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
            heuristic.lambda_out[a][j] = heuristic.lambda_in[a][j] * (1 - heuristic.x[a][j]); // calculate lambda^out_aj
            tempSum += heuristic.lambda_out[a][j];
        }
        heuristic.lambdap_in[a][k] = tempSum;
    }
    
    
    public void printTraffic(Heuristic heuristic) {
        DecimalFormat df = new DecimalFormat("0.00");
        for (int a = 0; a < Heuristic.numServices; a++) {
            for (int j = 0; j < Heuristic.numFogNodes; j++) {

                System.out.print(df.format(heuristic.lambda_in[a][j]) + " ");
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
        distributeTraffic(heuristic.scheme.averageRateOfTraffic, heuristic.lambda_in);
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic per fog node to the average traffic values, so
     * that the placement solves the problem based on averages
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode(Heuristic heuristic) {
        distributeTraffic(heuristic.scheme.averageRateOfCombinedAppTrafficPerNode, heuristic.lambda_in);
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic per fog node per service to the average traffic
     * values, so that the placement solves the problem based on averages
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode(Heuristic heuristic) {
        setTraffic(heuristic.scheme.averageRateOfTrafficPerNodePerService, heuristic.lambda_in);
    }

    private static void distributeTraffic(double trafficPerNodePerApp, double[][] targetTraffic) {
        double totalTraffic = trafficPerNodePerApp * Heuristic.numFogNodes * Heuristic.numServices;
        double trafficForCurrentService;
        double[] fogTrafficPercentage = new double[Heuristic.numFogNodes];
        for (int a = 0; a < Heuristic.numServices; a++) {
            trafficForCurrentService = totalTraffic * Parameters.ServiceTrafficPercentage[a];
            ArrayFiller.generateRandomDistributionOnArray(fogTrafficPercentage, 1, 7);
            for (int j = 0; j < Heuristic.numFogNodes; j++) {
                targetTraffic[a][j] = trafficForCurrentService * fogTrafficPercentage[j];
            }
        }
    }

    public static void distributeTraffic(double trafficPerNodePerApp) {
        distributeTraffic(trafficPerNodePerApp, Parameters.globalTraffic);
    }

    private static void distributeTraffic(Double[] combinedTrafficPerFogNode, double[][] targetTraffic) {
        for (int j = 0; j < Heuristic.numFogNodes; j++) {
            for (int a = 0; a < Heuristic.numServices; a++) {
                targetTraffic[a][j] = combinedTrafficPerFogNode[j] * Parameters.ServiceTrafficPercentage[a];
            }
        }
    }

    public static void distributeTraffic(Double[] combinedTrafficPerFogNode) {
        distributeTraffic(combinedTrafficPerFogNode, Parameters.globalTraffic);
    }

    private static void setTraffic(Double[][] newTraffic, double[][] targetTraffic) {
        for (int a = 0; a < Heuristic.numServices; a++) {
            for (int j = 0; j < Heuristic.numFogNodes; j++) {
                targetTraffic[a][j] = newTraffic[a][j];
            }
        }
    }

    public static void setTraffic(Double[][] actualTraffic) {
        setTraffic(actualTraffic, Parameters.globalTraffic);
    }

    public static void setTrafficToGlobalTraffic(Heuristic heuristic) {
        for (int a = 0; a < Heuristic.numServices; a++) {
            for (int j = 0; j < Heuristic.numFogNodes; j++) {
                heuristic.lambda_in[a][j] = Parameters.globalTraffic[a][j];
            }
        }
    }
}
