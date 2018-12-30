package Scheme;

import Simulation.Cost;
import Simulation.Traffic;
import Utilities.ArrayFiller;
import Utilities.Factorial;
import Utilities.ReverseMap;
import java.util.HashSet;

/**
 *
 * @author Ashkan Y.
 */
public class Parameters {

    public static int TAU; // time interval between run of the method (s)
    public static int TRAFFIC_CHANGE_INTERVAL; // time interval between run of the method (s)

    public static int numCloudServers; // opt 3. DTMC 25. threshold 3
    public static int numFogNodes; // opt 10. DTMC 100 (last:200). threshold 10
    public static int numServices; // opt 2. DTMC 50 (last:100). threshold 20

    public static double[] ServiceTrafficPercentage;

    public static double th[]; // threshold
    public static double q[]; // quality of service for service a

    public static double dIF[]; // average propagation delay from IoT nodes to fog node j (*can be measured and shown in paper by trace*)
    public static double rIF[]; // average transmission rate from IoT nodes to fog node j (*can be measured and shown in paper by trace*)

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

    public final static double KP_min = 800d;
    public final static double L_P_max = 200d;

    public static int CONTAINER_INIT_DELAY = 50; // 50 ms -> CONTAINER-AS-A-SERVICE AT THE EDGE: TRADE- OFF BETWEEN ENERGY EFFICIENCY AND SERVICE AVAILABILITY AT FOG NANO DATA CENTERS

    public static int[][] h; //index of the cloud server to which the traffic for service a is routed from fog node j
    public static ReverseMap[][] H_inverse; // set of indices of all fog nodes that route the traffic for service a to cloud server k.

    public static double[] L_P; // amount of required processing for service a per unit traffic, in MIPS
    public static double[] L_S; // size of service (i.e. container) a,
    public static double[] L_M; // required amount of memory for service a, in bytes

    public static double globalTraffic[][]; // this is a static version of traffic, which must remain the same

    // note that the delay of deploying containers is not considered yet, since we don't really need to when the interval of changing traffic is in the order of seconds (e.g. 5s or 60s)
    // this is because, even if we consider the 50ms delay, it will not affect the resutls.  
    public static double rFContr[]; // transmission rate from fog node j to the fog service controller

    private static Cost cost;


    public static void initialize() {

        cost = new Cost(numCloudServers, numFogNodes, numServices);
        Factorial f = new Factorial();
        globalTraffic = new double[numServices][numFogNodes];
        q = new double[numServices];
        ArrayFiller.generateRandom1DArray(q, 0.9, 0.99999);

        th = new double[numServices];
        ArrayFiller.generateRandom1DArray(th, 10d, 10d); // 10 ms is the threshold (architectural imperatives is my reference for this)

        dIF = new double[numFogNodes];
        ArrayFiller.generateRandom1DArray(dIF, 1d, 2d);

        rIF = new double[numFogNodes];
        if (Math.random() < 0.5) {
            ArrayFiller.generateFixed1DArray(rIF, 54d * 1024d * 1024d); // 54 Mbps
        } else {
            ArrayFiller.generateFixed1DArray(rIF, 51.23d * 1024d * 1024d); // 51.23 Mbps (is the "mixed" rate of one 54Mbps and a 1Gbps)
        }

        dFC = new double[numFogNodes][numCloudServers];
        ArrayFiller.generateRandom2DArray(dFC, 15d, 35d);

        rFC = new double[numFogNodes][numCloudServers];
        ArrayFiller.generateRandom2DArray(rFC, 1024d * 1024d * 1024d, 2438d * 1024d * 1024d);
        // We assume there are between 6-10 hops lies between fog and cloud. And there links could be 10 Gbps or 100Gbps (up to 2) 1024 is basically the lower band, when all 10 links are 1Gbps (1024 Mb), and that is the "mixed" rate of 10 10Gbps links. 1970 is also a mixed rate of 4 10Gbps links and 2 100Gbps.

        rFContr = new double[numFogNodes];
        ArrayFiller.generateFixed1DArray(rFContr, 10d * 1024d * 1024d * 1024d); // transmission rate of fog nodes to Fog Service Controller is 10Gbps.

        L_P = new double[numServices];
        ArrayFiller.generateRandom1DArray(L_P, 50d, L_P_max); // Towards QoS-aware Fog Service Placement (they have simuation, and it their simulation, they have 50,100 and 200

        L_S = new double[numServices];
        ArrayFiller.generateRandom1DArray(L_S, 50d * 1024d * 1024d * 8d, 500d * 1024d * 1024d * 8d); // size of a service is 50-500 MBytes

        L_M = new double[numServices];
        ArrayFiller.generateRandom1DArray(L_M, 2d * 1024d * 1024d * 8d, 400d * 1024d * 1024d * 8d); // required amount of memory for service is 2-400 MBytes

        KP = new double[numFogNodes];
        ArrayFiller.generateRandom1DArray(KP, KP_min, 1300d); // in MIPS
        // "A Cooperative Fog Approach for Effective Workload Balancing": Each fog network contains three hosts with 1256 (ARM Cortex v5), 1536 (ARM v7) and 847 (ARM11 family) MIPS respectively

        KpP = new double[numCloudServers];
        ArrayFiller.generateRandom1DArray(KpP, 16000d, 26000d); // in MIPS
        // cloud nodes are selected to be 20 times faster

        KM = new double[numFogNodes];
        ArrayFiller.generateRandom1DArray(KM, 8d * 1024d * 1024d * 1024d * 8d, 8d * 1024d * 1024d * 1024d * 8d); // 8GB

        KpM = new double[numCloudServers];
        ArrayFiller.generateRandom1DArray(KpM, 32d * 1024d * 1024d * 1024d * 8d, 32d * 1024d * 1024d * 1024d * 8d); // 32GB

        KS = new double[numFogNodes];
        ArrayFiller.generateRandom1DArray(KS, 20d * 1024d * 1024d * 1024d * 8d, 20d * 1024d * 1024d * 1024d * 8d); // 20GB

        KpS = new double[numCloudServers];
        ArrayFiller.generateRandom1DArray(KpS, 200d * 1024d * 1024d * 1024d * 200d, 32d * 1024d * 1024d * 1024d * 8d); // 200GB

        // The Impact of Mobile Multimedia Applications on Data Center Consolidation 
        // Augmented reality applications
        l_rq = new double[numServices];
        ArrayFiller.generateRandom1DArray(l_rq, 10d * 1024d * 8d, 26d * 1024d * 8d); // the request size 10KB-26KB 

        l_rp = new double[numServices];
        ArrayFiller.generateRandom1DArray(l_rp, 10d * 8d, 20d * 8d); // the request size 10B-20B

        h = new int[numServices][numFogNodes];
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                h[a][j] = (int) (Math.random() * numCloudServers);
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
        generateServiceTrafficPercentage(); // ServiceTrafficPercentage is initialized
    }

    private static void generateServiceTrafficPercentage() {
        ArrayFiller.generateRandomDistributionOnArray(ServiceTrafficPercentage);

    }
}
