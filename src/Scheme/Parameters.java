package Scheme;

import Components.Cost;
import Utilities.ArrayFiller;
import Utilities.Factorial;
import Utilities.RandomGenerator;
import Utilities.ReverseMap;
import java.util.HashSet;

/**
 *
 * @author Ashkan Y.
 *
 * This is a holder class which contains some parameters of the simulation
 */
public class Parameters {

    public static boolean MEASURING_RUNNING_TIME = false; // a boolean that is used for measuring the runtime of the greedy algorithms
    public static int TAU; // time interval between run of the method (s)
    public static int TRAFFIC_CHANGE_INTERVAL; // time interval between run of the method (s)

    public static int numCloudServers; // opt 3. DTMC 25. threshold 3
    public static int numFogNodes; // opt 10. DTMC 100 (last:200). threshold 10
    public static int numServices; // opt 2. DTMC 50 (last:100). threshold 20

    public static double[] ServiceTrafficPercentage; // the percentage of the traffic rate that is associated with a service

    public static double th[]; // threshold
    public static double q[]; // quality of service for service a

    public static double dIF[]; // average propagation delay from IoT nodes to fog node j (*can be measured and shown in paper by trace*)
    public static Double rIF[]; // average transmission rate from IoT nodes to fog node j (*can be measured and shown in paper by trace*)

    public static double dFC[][]; // propagation delay from fog node j to cloud node k (*can be measured and shown in paper by trace*)
    public static double rFC[][]; // average transmission rate from fog node j to cloud node k (*can be measured and shown in paper by trace*)

    public static double l_rq[]; // average request length of service a
    public static double l_rp[]; // average response length of service a

    public static double KP[]; // processing capacity (service rate) of fog node j
    public static double KpP[]; // processing capacity (service rate) of cloud server k

    public static double KM[]; // memory capacity of fog node j, in bytes
    public static double KpM[]; // memory capacity of cloud server k, in bytes

    public static double KS[]; // storage capacity of fog node j, in bytes
    public static double KpS[]; // storage capacity of cloud server k, in bytes

    // (in MIPS) reference: "A Cooperative Fog Approach for Effective Workload Balancing": Each fog network contains three hosts with 1256 (ARM Cortex v5), 1536 (ARM v7) and 847 (ARM11 family) MIPS respectively
    public final static double KP_min = 800d;
    public final static double KP_max = 1300d;

    // reference: "Towards QoS-aware Fog Service Placement" (in the reference simulation, it is can be found 50,100 and 200 for L_P)
    public final static double L_P_max = 200d;
    public final static double L_P_min = 50d;

    public static int CONTAINER_INIT_DELAY = 50; // 50 ms -> reference: "CONTAINER-AS-A-SERVICE AT THE EDGE: TRADE- OFF BETWEEN ENERGY EFFICIENCY AND SERVICE AVAILABILITY AT FOG NANO DATA CENTERS"

    public static int[][] h; //index of the cloud server to which the traffic for service a is routed from fog node j
    public static ReverseMap[][] H_inverse; // set of indices of all fog nodes that route the traffic for service a to cloud server k.

    public static double[] L_P; // amount of required processing for service a per unit traffic, in MIPS
    public static double[] L_S; // size of service (i.e. container) a,
    public static double[] L_M; // required amount of memory for service a, in bytes

    public static double globalTraffic[][]; // this is a static version of traffic, which must remain the same

    public static Double rFContr[]; // transmission rate from fog node j to the fog service controller

    private static Cost cost;
    public static double TRAFFIC_NORM_FACTOR;

    /**
     * This is the main function of this class. This function will initialize
     * the parameters of the simulation. For the specific values of the
     * parameters please refer to our paper.
     */
    public static void initialize() {
        cost = new Cost(numCloudServers, numFogNodes, numServices);
        Factorial f = new Factorial();
        globalTraffic = new double[numServices][numFogNodes];
        q = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(q, 0.9, 0.99999); // high QoS requirements

        th = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(th, 10d, 10d); // 10 ms is the threshold (architectural imperatives is my reference for this)

        dIF = new double[numFogNodes];
        ArrayFiller.fill1DArrayRandomlyInRange(dIF, 1d, 2d); // refer to the paper

        rIF = new Double[numFogNodes];
        if (RandomGenerator.genUniformRandom() < 0.5) {
            ArrayFiller.fill1DArrayWithConstantNumber(rIF, 54d * 1000d * 1000d); // 54 Mbps
        } else {
            ArrayFiller.fill1DArrayWithConstantNumber(rIF, 51.233d * 1000d * 1000d); // 51.233 Mbps (is the "mixed" rate of one 54Mbps and a 1Gbps link)
        }

        dFC = new double[numFogNodes][numCloudServers];
        ArrayFiller.fill2DArrayRandomlyInRange(dFC, 15d, 35d);

        // We assume there are between 6-10 hops lies between fog and cloud. And the links could be 10 Gbps or 100Gbps (up to 2). 
        // 1 is basically the lower bound, when all 10 links are 10Gbps, and that is the "mixed" rate of 10 10Gbps links. 2.38Gbps is also a mixed rate of 4 10Gbps links and 2 100Gbps.
        rFC = new double[numFogNodes][numCloudServers];
        ArrayFiller.fill2DArrayRandomlyInRange(rFC, 1d * 1000d * 1000d * 1000d, 2.38d * 1000d * 1000d * 1000d);

        rFContr = new Double[numFogNodes];
        ArrayFiller.fill1DArrayWithConstantNumber(rFContr, 10d * 1000d * 1000d * 1000d); // transmission rate of fog nodes to Fog Service Controller is 10Gbps.

        L_P = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(L_P, L_P_min, L_P_max); // valuse explained above

        L_S = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(L_S, 50d * 1000d * 1000d * 8d, 500d * 1000d * 1000d * 8d); // size of a service is 50-500 MBytes

        L_M = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(L_M, 2d * 1000d * 1000d * 8d, 400d * 1000d * 1000d * 8d); // required amount of memory for service is 2-400 MBytes

        KP = new double[numFogNodes];
        ArrayFiller.fill1DArrayRandomlyInRange(KP, KP_min, KP_max);
        KpP = new double[numCloudServers];
        ArrayFiller.fill1DArrayRandomlyInRange(KpP, 16000d, 26000d); // in MIPS
        // cloud nodes are selected to be 20 times faster

        KM = new double[numFogNodes];
        ArrayFiller.fill1DArrayRandomlyInRange(KM, 8d * 1000d * 1000d * 1000d * 8d, 8d * 1000d * 1000d * 1000d * 8d); // 8GB

        KpM = new double[numCloudServers];
        ArrayFiller.fill1DArrayRandomlyInRange(KpM, 32d * 1000d * 1000d * 1000d * 8d, 32d * 1000d * 1000d * 1000d * 8d); // 32GB

        KS = new double[numFogNodes];
        ArrayFiller.fill1DArrayRandomlyInRange(KS, 25d * 1000d * 1000d * 1000d * 8d, 25d * 1000d * 1000d * 1000d * 8d); // 25GB

        KpS = new double[numCloudServers];
        ArrayFiller.fill1DArrayRandomlyInRange(KpS, 250d * 1000d * 1000d * 1000d * 8d, 250d * 1000d * 1000d * 1000d * 8d); // 250GB

        // Reference: "The Impact of Mobile Multimedia Applications on Data Center Consolidation"
        // Augmented reality applications
        l_rq = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(l_rq, 10d * 1000d * 8d, 26d * 1000d * 8d); // the request size 10KB-26KB 

        l_rp = new double[numServices];
        ArrayFiller.fill1DArrayRandomlyInRange(l_rp, 10d * 8d, 20d * 8d); // the request size 10B-20B

        h = new int[numServices][numFogNodes];
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                h[a][j] = (int) (RandomGenerator.genUniformRandom() * numCloudServers);
            }
        }
        H_inverse = new ReverseMap[numServices][numCloudServers];
        for (int k = 0; k < numCloudServers; k++) {
            for (int a = 0; a < numServices; a++) {
                HashSet<Integer> single_h_reverse = new HashSet<>();
                for (int j = 0; j < numFogNodes; j++) {
                    if (h[a][j] == k) {
                        single_h_reverse.add(j);
                    }
                }
                H_inverse[a][k] = new ReverseMap(single_h_reverse);
            }
        }

        ServiceTrafficPercentage = new double[numServices];
        generateRandomPercentageForServiceTraffic(); // ServiceTrafficPercentage is initialized

        TRAFFIC_NORM_FACTOR = calcTrafficNormFactor();
    }

    /**
     * Generates random percentages for traffic rates of the services
     */
    private static void generateRandomPercentageForServiceTraffic() {
        ArrayFiller.fillRandomPDFInArray(ServiceTrafficPercentage);
    }

    /**
     * This method calculates the normalization factor for the incoming traffic
     * to fog nodes. Note that since the number of services and number of fog
     * nodes varies in each experiment, without normalizing the incoming
     * traffic, the traffic may be small or big.
     *
     * @return returns the normalization factor for the incoming traffic to fog
     * nodes.
     */
    private static double calcTrafficNormFactor() {
        double sum = 0;
        for (int a = 0; a < numServices; a++) {
            sum += L_P[a];
        }
        double f_min = L_P_min / sum;
        return f_min * (KP_min / L_P_max);
    }
}
