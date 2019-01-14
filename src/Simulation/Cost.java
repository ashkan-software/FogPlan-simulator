package Simulation;

import Run.MainExperiment3;
import Scheme.Parameters;
import Utilities.ArrayFiller;

/**
 *
 * @author Ashkan Y.
 */
public class Cost {

    private static int NUM_SERVICES;
    private static int NUM_CLOUD_SERVERS;
    private static int NUM_FOG_NODES;

    private static double[] CLOUD_UNIT_PROC_COST;
    private static double[] FOG_UNIT_PROC_COST;

    private static double[] CLOUD_UNIT_STOR_COST;
    private static double[] FOG_UNIT_STOR_COST;

    private static double[][] FOG_CLOUD_COMM_UNIT_COST;
    private static double[][] FOG_FOG_COMM_UNIT_COST;
    private static double[] FOG_CONTROLLER_COMM_UNIT_COST;

    private static double[] SERVICE_PENALY;

    private static double totalCost = 0;

    public Cost(int NUM_CLOUD_SERVERS, int NUM_FOG_NODES, int NUM_SERVICES) {
        this.NUM_CLOUD_SERVERS = NUM_CLOUD_SERVERS;
        this.NUM_FOG_NODES = NUM_FOG_NODES;
        this.NUM_SERVICES = NUM_SERVICES;

        CLOUD_UNIT_PROC_COST = new double[NUM_CLOUD_SERVERS];
        ArrayFiller.generateFixed1DArray(CLOUD_UNIT_PROC_COST, 0.001d);

        CLOUD_UNIT_STOR_COST = new double[NUM_CLOUD_SERVERS];
        ArrayFiller.generateFixed1DArray(CLOUD_UNIT_STOR_COST, 0.000000000004d);

        FOG_UNIT_PROC_COST = new double[NUM_FOG_NODES];
        ArrayFiller.generateRandom1DArray(FOG_UNIT_PROC_COST, 0.001d, 0.001d);

        FOG_UNIT_STOR_COST = new double[NUM_FOG_NODES];
        ArrayFiller.generateRandom1DArray(FOG_UNIT_STOR_COST, 0.000000000004d, 0.000000000004d); // 00000000008d for optimal and cumulative and DTMC and threshold

        FOG_CLOUD_COMM_UNIT_COST = new double[NUM_FOG_NODES][NUM_CLOUD_SERVERS];
        ArrayFiller.generateFixed2DArray(FOG_CLOUD_COMM_UNIT_COST, 0.0000000002d);

        FOG_FOG_COMM_UNIT_COST = new double[NUM_FOG_NODES][NUM_FOG_NODES];
        ArrayFiller.generateFixed2DArray(FOG_FOG_COMM_UNIT_COST, 0.0000000002d);

        FOG_CONTROLLER_COMM_UNIT_COST = new double[NUM_FOG_NODES];
        ArrayFiller.generateFixed1DArray(FOG_CONTROLLER_COMM_UNIT_COST, 0.0000000005d);

        SERVICE_PENALY = new double[NUM_SERVICES];
        ArrayFiller.generateRandom1DArray(SERVICE_PENALY, 40d, 50d); // 2-5 for optimal and cumulative. 40-50 for DTMC
    }

    /**
     * When a service is already deployed in the cloud, this is the extra cost
     * of processing, if a service is not implemented on a fog node j. This cost
     * is caused by the lambda_out traffic from fog node j
     *
     * @param time
     * @param a
     * @param L_P
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
     * @param lambdap_in incoming traffic rate to cloud server $k$ for service
     * $a$ (request/second)
     * @return returns the cost of processing in the cloud for the duration of
     * `time`
     */
    private static double costPC(double time, int k, int a, double[] L_P, double lambdap_in[][]) {
        return CLOUD_UNIT_PROC_COST[k] * L_P[a] * lambdap_in[a][k] * time;
    }

    public static double costPF(double time, int j, int a, double[] L_P, double lambda_in[][]) {
        return FOG_UNIT_PROC_COST[j] * L_P[a] * lambda_in[a][j] * time;
    }

    public static double costExtraSC(double time, int k, int a, double[] L_S, int[][] xp) {
        if (xp[a][k] == 1) { // if the service is already deployed on cloud server k, not deploying a service on fog node j will not add any extra storage cost for cloud.
            return 0;
        } else {
            return CLOUD_UNIT_STOR_COST[k] * L_S[a] * time;
        }
    }

    private static double costSC(double time, int k, int a, double[] L_S) {
        return CLOUD_UNIT_STOR_COST[k] * L_S[a] * time;
    }

    public static double costSF(double time, int j, int a, double[] L_S) {
        return FOG_UNIT_STOR_COST[j] * L_S[a] * time;
    }

    public static double costCfc(double time, int j, int a, double lambda_out[][], int[][] h) {
        return FOG_CLOUD_COMM_UNIT_COST[j][h[a][j]] * lambda_out[a][j] * (Parameters.l_rp[a] + Parameters.l_rq[a]) * time;
    }

    public static double costDep(int j, int a, double[] L_S) {
        return L_S[a] * FOG_CONTROLLER_COMM_UNIT_COST[j];
    }

    public static double costViol(double time, int a, int j, double Vper[], double q[], double lambda_in[][]) {
        return Math.max(0, Vper[a] - (1 - q[a])) * lambda_in[a][j] * SERVICE_PENALY[a] * time;
    }

    public static double costViolPerFogNode(double time, int a, int j, double Vper_aj, double q[], double fogTrafficPercentage, double lambda_in[][]) {
        return Math.max(0, Vper_aj - (1 - q[a]) * fogTrafficPercentage) * lambda_in[a][j] * SERVICE_PENALY[a] * time;
    }

    /**
     *
     * @param time is in minutes
     * @param x
     * @param xp
     * @param x_backup
     * @param lambda_in
     * @param lambdap_in
     * @param lambda_out
     * @param L_P
     * @param L_S
     * @param h
     * @return
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
                costViol += costViol(time, a, j, Vper, q, lambda_in);
            }
        }
        costViol = costViol / (NUM_FOG_NODES * NUM_SERVICES);
//            System.out.println("costPC " + (costPC) + " costPF " + (costPF) + " costSC " + (costSC) + " costSF " + (costSF)
//                    + " costCff " + (costCff) + " costCfc " + (costCfc) + " costViol " + (costViol) + " costDep " + (costDep));
        Double c = (costPC + costPF + costSC + costSF + costCff + costCfc + costViol + costDep);
        totalCost += c;
        return c;

    }

}
