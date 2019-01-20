package Components;

import Scheme.Parameters;
import Utilities.ArrayFiller;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains all the functions and parameters that are related to the
 * cost
 */
public class Cost {

    private static int NUM_SERVICES; // numebr of services
    private static int NUM_CLOUD_SERVERS; // number of cloud servers
    private static int NUM_FOG_NODES; // number of fog nodes

    private static Double[] CLOUD_UNIT_PROC_COST; // C'^p_k   unit cost of process. at cloud ser. k (per million instructions)
    private static double[] FOG_UNIT_PROC_COST; // C^P_j  unit cost of process. at fog node j (per million instructions)

    private static Double[] CLOUD_UNIT_STOR_COST; // C'^S_k   unit cost of storage at cloud server k (per byte per second)
    private static double[] FOG_UNIT_STOR_COST;  // C^S_j  unit cost of storage at fog node j (per byte per second)

    private static Double[][] FOG_CLOUD_COMM_UNIT_COST; // unit cost of communication between fog nodes and cloud servers
    private static Double[][] FOG_FOG_COMM_UNIT_COST; // unit cost of communication among fog nodes
    private static Double[] FOG_CONTROLLER_COMM_UNIT_COST; // unit cost of communication between fog nodes and FSC

    private static double[] SERVICE_PENALY; // p_a

    private static Double totalCost = 0d;

    /**
     * Constructor of the cost class. The input parameters are number of cloud
     * servers, fog nodes, and services. This constructor will initializes the
     * unit cost parameters.
     *
     * @param NUM_CLOUD_SERVERS
     * @param NUM_FOG_NODES
     * @param NUM_SERVICES
     */
    public Cost(int NUM_CLOUD_SERVERS, int NUM_FOG_NODES, int NUM_SERVICES) {
        this.NUM_CLOUD_SERVERS = NUM_CLOUD_SERVERS;
        this.NUM_FOG_NODES = NUM_FOG_NODES;
        this.NUM_SERVICES = NUM_SERVICES;

        // all of these parameters are explained in the experiment section of the QDFSP paper
        CLOUD_UNIT_PROC_COST = new Double[NUM_CLOUD_SERVERS];
        ArrayFiller.fill1DArrayWithConstantNumber(CLOUD_UNIT_PROC_COST, 0.002d);

        CLOUD_UNIT_STOR_COST = new Double[NUM_CLOUD_SERVERS];
        ArrayFiller.fill1DArrayWithConstantNumber(CLOUD_UNIT_STOR_COST, 0.000000000004d);

        FOG_UNIT_PROC_COST = new double[NUM_FOG_NODES];
        ArrayFiller.fill1DArrayRandomlyInRange(FOG_UNIT_PROC_COST, 0.002d, 0.002d);

        FOG_UNIT_STOR_COST = new double[NUM_FOG_NODES];
        ArrayFiller.fill1DArrayRandomlyInRange(FOG_UNIT_STOR_COST, 0.000000000004d, 0.000000000004d); // 00000000008d for optimal and aggregated and DTMC and threshold

        FOG_CLOUD_COMM_UNIT_COST = new Double[NUM_FOG_NODES][NUM_CLOUD_SERVERS];
        ArrayFiller.fill2DArrayWithConstantNumber(FOG_CLOUD_COMM_UNIT_COST, 0.0000000002d);

        FOG_FOG_COMM_UNIT_COST = new Double[NUM_FOG_NODES][NUM_FOG_NODES];
        ArrayFiller.fill2DArrayWithConstantNumber(FOG_FOG_COMM_UNIT_COST, 0.0000000002d);

        FOG_CONTROLLER_COMM_UNIT_COST = new Double[NUM_FOG_NODES];
        ArrayFiller.fill1DArrayWithConstantNumber(FOG_CONTROLLER_COMM_UNIT_COST, 0.0000000005d);

        SERVICE_PENALY = new double[NUM_SERVICES];
        ArrayFiller.fill1DArrayRandomlyInRange(SERVICE_PENALY, 10d, 20d); // 10-20 aggregated and optimal. 100-200 for threshold and tau
    }

    /**
     * When a service is already deployed in the cloud, this is the extra cost
     * of processing in the cloud, if a service is not implemented on a fog node
     * `j`. This cost is caused by the lambda_out traffic from fog node j (not
     * from all fog nodes)
     *
     * @param time duration of time
     * @param k the index of cloud server
     * @param a the index of service
     * @param L_P required amount of processing for service `a` per request, (in
     * million instructions per request)
     * @param extraTraffic
     * @return
     */
    public static double costExtraPC(double time, int k, int a, double[] L_P, double extraTraffic) {
        return CLOUD_UNIT_PROC_COST[k] * L_P[a] * extraTraffic * time;
    }

    /**
     * Calculates the cost of processing in a particular cloud server for a
     * particular service for the duration of `time`
     *
     * @param time duration for which the cost of processing in the cloud is
     * being calculated
     * @param k the index of cloud server
     * @param a the index of service
     * @param L_P required amount of processing for service `a` per request, (in
     * million instructions per request)
     * @param lambdap_in incoming traffic rate to cloud server `k' for service
     * `a` (request/second)
     * @return returns the cost of processing in the cloud for the duration of
     * `time`
     */
    private static double costPC(double time, int k, int a, double[] L_P, double lambdap_in[][]) {
        return CLOUD_UNIT_PROC_COST[k] * L_P[a] * lambdap_in[a][k] * time;
    }

    /**
     * Calculates the cost of processing in a particular cloud server for a
     * particular service for the duration of `time`
     *
     * @param time duration for which the cost of processing in the cloud is
     * being calculated
     * @param j the index of fog node
     * @param a the index of service
     * @param L_P required amount of processing for service `a` per request, (in
     * million instructions per request)
     * @param lambda_in incoming traffic rate to fog node j for service `a`
     * (request/second)
     * @return returns the cost of processing in the cloud for the duration of
     * `time`
     */
    public static double costPF(double time, int j, int a, double[] L_P, double lambda_in[][]) {
        return FOG_UNIT_PROC_COST[j] * L_P[a] * lambda_in[a][j] * time;
    }

    /**
     * When a service is already deployed in the cloud, this is the extra cost
     * of storage in the cloud, if a service is not implemented on a cloud
     * server `k'.
     *
     * @param time duration of time
     * @param k the index of cloud server
     * @param a the index of service
     * @param L_S storage size of service a, in bytes
     * @param xp the cloud service allocation matrix
     */
    public static double costExtraSC(double time, int k, int a, double[] L_S, int[][] xp) {
        if (xp[a][k] == 1) { // if the service is already deployed on cloud server k, not deploying a service on fog node j will not add any extra storage cost for cloud.
            return 0;
        } else {
            return CLOUD_UNIT_STOR_COST[k] * L_S[a] * time;
        }
    }

    /**
     * Calculates the cost of storage in a particular cloud server for a
     * particular service for the duration of `time`
     *
     * @param time duration of time
     * @param k the index of cloud server
     * @param a the index of service
     * @param L_S storage size of service a, in bytes
     */
    private static double costSC(double time, int k, int a, double[] L_S) {
        return CLOUD_UNIT_STOR_COST[k] * L_S[a] * time;
    }

    /**
     * Calculates the cost of storage in a particular fog node for a particular
     * service for the duration of `time`
     *
     * @param time duration of time
     * @param j the index of fog node
     * @param a the index of service
     * @param L_S storage size of service a, in bytes
     */
    public static double costSF(double time, int j, int a, double[] L_S) {
        return FOG_UNIT_STOR_COST[j] * L_S[a] * time;
    }

    /**
     * Calculates the cost of communication between fog node j and the
     * associated cloud servers
     *
     * @param time duration of time
     * @param j the index of fog node
     * @param a the index of service a
     * @param lambda_out rate of dispatched traffic for service a from fog node
     * j to the associated cloud server (request/second)
     * @param h index of the cloud server to which the traffic for service a is
     * routed from fog node j
     */
    public static double costCfc(double time, int j, int a, double lambda_out[][], int[][] h) {
        return FOG_CLOUD_COMM_UNIT_COST[j][h[a][j]] * lambda_out[a][j] * (Parameters.l_rp[a] + Parameters.l_rq[a]) * time;
    }

    /**
     * Calculates the cost of deployment of a service on a particular fog node
     *
     * @param j the index of the fog node
     * @param a the index of the service
     * @param L_S storage size of service a, in bytes
     */
    public static double costDep(int j, int a, double[] L_S) {
        return L_S[a] * FOG_CONTROLLER_COMM_UNIT_COST[j];
    }

    /**
     * Calculates the cost of delay violations for a particular fog node for a
     * given service in the duration of `time`
     *
     * @param time the duration of the time
     * @param a the index of service
     * @param j the index of fog node
     * @param Vper_a the percentage of IoT service delay samples of service a
     * that do not meet the delay requirement.
     * @param q desired quality of service for service a
     * @param lambda_in incoming traffic rate from IoT nodes to fog node j for
     * service a (request/second)
     */
    public static double costViol(double time, int a, int j, double Vper_a, double q[], double lambda_in[][]) {
        return Math.max(0, Vper_a - (1 - q[a])) * lambda_in[a][j] * SERVICE_PENALY[a] * time;
    }

    /**
     * Calculates average the average cost (processing + storage + communication
     * + deployment + violation) for cloud servers and fog nodes for the
     * duration of `time`
     *
     * @param time is in minutes
     * @param x the fog service allocation matrix
     * @param xp the cloud service allocation matrix
     * @param x_backup the backup of x
     * @param lambda_in incoming traffic rate from IoT nodes to fog node j for
     * service a (request/second)
     * @param lambdap_in incoming traffic rate to cloud server `k' for service
     * `a` (request/second)
     * @param lambda_out rate of dispatched traffic for service a from fog node
     * j to the associated cloud server (request/second)
     * @param L_P required amount of processing for service `a` per request, (in
     * million instructions per request)
     * @param L_S storage size of service a, in bytes
     * @param h index of the cloud server to which the traffic for service a is
     * routed from fog node j
     */
    public static double calcAverageCost(double time, int[][] x, int[][] xp, int[][] x_backup, double Vper[], double q[], double lambda_in[][], double lambdap_in[][], double lambda_out[][], double[] L_P, double[] L_S, int[][] h) {
        double costPC, costPF, costSC, costSF, costCfc, costCff, costDep, costViol;

        // cost of processing in cloud
        costPC = 0;
        for (int k = 0; k < NUM_CLOUD_SERVERS; k++) {
            for (int a = 0; a < NUM_SERVICES; a++) {
                if (xp[a][k] == 1) {
                    costPC += costPC(time, k, a, L_P, lambdap_in);
                }
            }
        }
        costPC = costPC / (NUM_CLOUD_SERVERS * NUM_SERVICES);

        // cost of processing in fog
        costPF = 0;
        for (int j = 0; j < NUM_FOG_NODES; j++) {
            for (int a = 0; a < NUM_SERVICES; a++) {
                if (x[a][j] == 1) {
                    costPF += costPF(time, j, a, L_P, lambda_in);
                }
            }
        }
        costPF = costPF / (NUM_FOG_NODES * NUM_SERVICES);

        // cost of storage in cloud
        costSC = 0;
        for (int k = 0; k < NUM_CLOUD_SERVERS; k++) {
            for (int a = 0; a < NUM_SERVICES; a++) {
                if (xp[a][k] == 1) {
                    costSC += costSC(time, k, a, L_S);
                }
            }
        }
        costSC = costSC / (NUM_CLOUD_SERVERS * NUM_SERVICES);

        // cost of storage in fog
        costSF = 0;
        for (int j = 0; j < NUM_FOG_NODES; j++) {
            for (int a = 0; a < NUM_SERVICES; a++) {
                if (x[a][j] == 1) {
                    costSF += costSF(time, j, a, L_S);
                }
            }
        }
        costSF = costSF / (NUM_FOG_NODES * NUM_SERVICES);

        // cost of communication from fog to cloud
        costCfc = 0;
        for (int j = 0; j < NUM_FOG_NODES; j++) {
            for (int a = 0; a < NUM_SERVICES; a++) {
                costCfc += costCfc(time, j, a, lambda_out, h);
            }
        }
        costCfc = costCfc / (NUM_FOG_NODES * NUM_SERVICES);

        // cost of communication between fog nodes
        costCff = 0;

        // cost of container deployment
        costDep = 0;
        for (int a = 0; a < NUM_SERVICES; a++) {
            for (int j = 0; j < NUM_FOG_NODES; j++) {
                if (x_backup[a][j] == 0 && x[a][j] == 1) {
                    costDep += costDep(j, a, L_S);
                }
            }
        }
        costDep = costDep / (NUM_FOG_NODES * NUM_SERVICES);

        // cost of violation
        costViol = 0;
        for (int a = 0; a < NUM_SERVICES; a++) {
            for (int j = 0; j < NUM_FOG_NODES; j++) {
                costViol += costViol(time, a, j, Vper[a], q, lambda_in);
            }
        }
        costViol = costViol / (NUM_FOG_NODES * NUM_SERVICES);
        Double c = (costPC + costPF + costSC + costSF + costCff + costCfc + costViol + costDep);
        totalCost += c;
        return c;
    }
}
