package Components;

import Scheme.Parameters;
import Utilities.ArrayFiller;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ashkany
 *
 * This class has the functions and variables related to traffic
 */
public class Traffic {

    protected double lambda_in[][]; // lambda^in_aj
    protected double lambdap_in[][]; // lambda'^in_ak

    protected double lambda_out[][]; // lambda^out_aj

    protected double[][] arrivalInstructionsCloud; // LAMBDA_ak
    protected double[][] arrivalInstructionsFog; // LAMBDA_aj

    public static int AGGREGATED = 1;
    public static int COMBINED_APP = 2;
    public static int NOT_COMBINED = 3;

    public static int TRAFFIC_ENLARGE_FACTOR = 1; // since the traffic is read from the trace files, its value might be small. This factor will enlarge the vlaue of the traffic

    /**
     * Constructor of the class
     */
    public Traffic() {
        lambda_in = new double[Parameters.numServices][Parameters.numFogNodes];
        lambdap_in = new double[Parameters.numServices][Parameters.numCloudServers];
        lambda_out = new double[Parameters.numServices][Parameters.numFogNodes];
        arrivalInstructionsCloud = new double[Parameters.numServices][Parameters.numCloudServers];
        arrivalInstructionsFog = new double[Parameters.numServices][Parameters.numFogNodes];
    }

    /**
     * Backs up the incoming traffic to the fog node
     *
     * @param method the method that is using the traffic
     */
    protected static void backupIncomingTraffic(Method method) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                method.backup_lambda_in[a][j] = method.traffic.lambda_in[a][j];
            }
        }
    }

    /**
     * Reverse of the backup incoming traffic. Restores the incoming traffic to
     * the fog node
     *
     * @param method the method that is using the traffic
     */
    protected static void restoreIncomingTraffic(Method method) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                method.traffic.lambda_in[a][j] = method.backup_lambda_in[a][j];
            }
        }
    }

    /**
     * Gets incoming traffic to all fog nodes for a given service
     *
     * @param a index of the service
     * @param isSortAscending boolean showing if sort is ascending
     * @param method the method that is using the traffic
     * @return returns a list of FogTrafficIndex, for a given service
     */
    protected static List<FogTrafficIndex> getFogIncomingTraffic(int a, boolean isSortAscending, Method method) {

        List<FogTrafficIndex> fogTrafficIndexList = new ArrayList<>();
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            fogTrafficIndexList.add(new FogTrafficIndex(j, method.traffic.lambda_in[a][j], isSortAscending));
        }
        return fogTrafficIndexList;
    }

    /**
     * Calculates the arrival rates of the instructions to fog nodes and cloud
     * servers
     *
     * @param method the method that is using the traffic
     */
    protected static void calcArrivalRatesOfInstructions(Method method) {
        for (int a = 0; a < Parameters.numServices; a++) {
            calcArrivalRatesOfInstructions(method, a);
        }
    }

    /**
     * Calculates the arrival rates of the instructions to fog nodes and cloud
     * servers for a give service
     *
     * @param method the method that is using the traffic
     * @param a the index of the service
     */
    protected static void calcArrivalRatesOfInstructions(Method method, int a) {
        calcArrivalRatesOfInstructionsFogNodes(method, a);
        calcArrivalRatesOfInstructionsCloudNodes(method, a);
    }

    /**
     * Calculates the arrival rates of the instructions to all fog nodes for a
     * give service
     *
     * @param method the method that is using the traffic
     * @param a the index of the service
     */
    private static void calcArrivalRatesOfInstructionsFogNodes(Method method, int a) {
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            calcArrivalRatesOfInstructionsFogNode(a, j, method);
        }

    }

    /**
     * Calculates the arrival rates of the instructions to a fog node for a give
     * service
     *
     * @param method the method that is using the traffic
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private static void calcArrivalRatesOfInstructionsFogNode(int a, int j, Method method) {
        method.traffic.arrivalInstructionsFog[a][j] = Parameters.L_P[a] * method.traffic.lambda_in[a][j] * method.x[a][j];
    }

    /**
     * Calculates the arrival rates of the instructions to all cloud servers for
     * a give service
     *
     * @param method the method that is using the traffic
     * @param a the index of the service
     */
    private static void calcArrivalRatesOfInstructionsCloudNodes(Method method, int a) {
        for (int k = 0; k < Parameters.numCloudServers; k++) {
            calcArrivalRateCloudForNodeForService(k, a, method);
            calcArrivalRatesOfInstructionsCloudNode(a, k, method);
        }

    }

    /**
     * Calculates the arrival rates of the instructions to a cloud server for a
     * give service
     *
     * @param method the method that is using the traffic
     * @param a the index of the service
     * @param k the index of the cloud server
     */
    private static void calcArrivalRatesOfInstructionsCloudNode(int a, int k, Method method) {
        method.traffic.arrivalInstructionsCloud[a][k] = Parameters.L_P[a] * method.traffic.lambdap_in[a][k] * method.xp[a][k];
    }

    /**
     * Calculates lambda^out_aj and lambdap_in_ak for a cloud server for a given
     * service
     *
     * @param method the method that is using the traffic
     * @param k the index of the cloud server
     * @param a the index of the service
     */
    private static void calcArrivalRateCloudForNodeForService(int k, int a, Method method) {
        double tempSum = 0;
        for (Integer j : Parameters.H_inverse[a][k].elemets) {
            method.traffic.lambda_out[a][j] = method.traffic.lambda_in[a][j] * (1 - method.x[a][j]); // calculate lambda^out_aj
            tempSum += method.traffic.lambda_out[a][j];
        }
        method.traffic.lambdap_in[a][k] = tempSum;
    }

    /**
     * Prints the incoming traffic to fog nodes
     *
     * @param method
     */
    public static void printTrafficFog(Method method) {
        DecimalFormat df = new DecimalFormat("0.000000");
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                System.out.print(df.format(method.traffic.lambda_in[a][j]) + " ");
            }
            System.out.println("");
        }
    }

    /**
     * This function is called the first time when Static Fog is called, and
     * changes the traffic to the average traffic values, so that the placement
     * solves the problem based on average
     *
     * @param method the method that is using the traffic
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimeCombined(Method method) {
        distributeTraffic(method.scheme.averageRateOfTraffic, method.traffic.lambda_in);
        enlargeTrafficFog(method);
    }

    /**
     * This function is called the first time when Static Fog is called, and
     * changes the traffic per fog node to the average traffic values, so that
     * the placement solves the problem based on averages
     *
     * @param method the method that is using the traffic
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode(Method method) {
        distributeTraffic(method.scheme.averageRateOfAggregatedServiceTrafficPerFogNode, method.traffic.lambda_in);
        enlargeTrafficFog(method);
    }

    /**
     * This function is called the first time when Static Fog is called, and
     * changes the traffic per fog node per service to the average traffic
     * values, so that the placement solves the problem based on averages
     *
     * @param method the method that is using the traffic
     */
    protected static void initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode(Method method) {
        setTraffic(method.scheme.averageRateOfTrafficPerFogNodePerService, method.traffic.lambda_in);
        enlargeTrafficFog(method);
    }

    /**
     * Distributes the given traffic that is for one service on one node
     * (`trafficPerNodePerService`) randomly to the target 2D array
     * (`targetTraffic`)
     *
     * @param trafficPerNodePerService the incoming traffic sample to one
     * service on one node
     * @param targetTraffic the target 2D array
     */
    private static void distributeTraffic(double trafficPerNodePerService, double[][] targetTraffic) {
        double totalTraffic = trafficPerNodePerService * Parameters.numFogNodes * Parameters.numServices;
        double trafficForCurrentService;
        double[] nodeTrafficPercentage = new double[Parameters.numFogNodes];
        for (int a = 0; a < Parameters.numServices; a++) {
            trafficForCurrentService = totalTraffic * Parameters.ServiceTrafficPercentage[a];
            ArrayFiller.fillRandomPDFInArray(nodeTrafficPercentage);
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                targetTraffic[a][j] = trafficForCurrentService * nodeTrafficPercentage[j];
            }
        }
    }

    /**
     * Distributes the given traffic that is for one service on one node
     * (`trafficPerNodePerService`) randomly to the global traffic
     *
     * @param trafficPerNodePerService the incoming traffic sample to one
     * service on one node
     */
    public static void distributeTraffic(double trafficPerNodePerService) {
        distributeTraffic(trafficPerNodePerService, Parameters.globalTraffic);
    }

    /**
     * Distributes the given traffic that is for all services (combined) on one
     * node (`combinedTrafficPerNode`) randomly to the target 2D array
     * (`targetTraffic`)
     *
     * @param combinedTrafficPerNode the incoming traffic sample to one node
     * @param targetTraffic the target 2D array
     */
    private static void distributeTraffic(Double[] combinedTrafficPerNode, double[][] targetTraffic) {
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            for (int a = 0; a < Parameters.numServices; a++) {
                targetTraffic[a][j] = combinedTrafficPerNode[j] * Parameters.ServiceTrafficPercentage[a];
            }
        }
    }

    /**
     * Distributes the given traffic that is for all services (combined) on one
     * node (`combinedTrafficPerNode`) randomly to the global traffic
     *
     * @param combinedTrafficPerNode the incoming traffic sample to one node
     */
    public static void distributeTraffic(Double[] combinedTrafficPerNode) {
        distributeTraffic(combinedTrafficPerNode, Parameters.globalTraffic);
    }

    /**
     * Assigns to the `targetTraffic` the `newTraffic`
     *
     * @param newTraffic the new value of the traffic
     * @param targetTraffic the old value of the traffic
     */
    private static void setTraffic(Double[][] newTraffic, double[][] targetTraffic) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                targetTraffic[a][j] = newTraffic[a][j];
            }
        }
    }

    /**
     * Set the lambda_in[][] to the global traffic
     *
     * @param method the method that is using the traffic
     */
    public static void setTrafficToGlobalTraffic(Method method) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                method.traffic.lambda_in[a][j] = Parameters.globalTraffic[a][j];
            }
        }
        enlargeTrafficFog(method);
    }

    /**
     * Enlarges the incoming traffic to the fog nodes
     *
     * @param method the method that is using the traffic
     */
    private static void enlargeTrafficFog(Method method) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                method.traffic.lambda_in[a][j] *= TRAFFIC_ENLARGE_FACTOR;
            }
        }
    }
}
