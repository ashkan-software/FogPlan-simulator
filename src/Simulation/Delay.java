package Simulation;

import Run.Parameters;
import Utilities.Factorial;

/**
 *
 * @author ashkany This class has the functions and variables related to
 * calculating delay
 */
public class Delay {

    private double[][] rho; // rho_aj
    private double[][] rhop; //rho'_ak

    private double[][] f; // f_aj
    private double[][] fp; // f'_ak

    private int[] n; // n_j: processing units of fog node j
    private int[] np; // f'_ak: processing units of cloud server k

    private double P0[][];
    private double PQ[][];

    private double P0p[][];
    private double PQp[][];

    private Heuristic heuristic;

    public Delay(Heuristic heuristic) {
        this.heuristic = heuristic;

        rho = new double[Parameters.numServices][Parameters.numFogNodes];
        rhop = new double[Parameters.numServices][Parameters.numCloudServers];
        f = new double[Parameters.numServices][Parameters.numFogNodes];
        fp = new double[Parameters.numServices][Parameters.numCloudServers];

        n = new int[Parameters.numFogNodes];
        np = new int[Parameters.numCloudServers];

        P0 = new double[Parameters.numServices][Parameters.numFogNodes];
        P0p = new double[Parameters.numServices][Parameters.numCloudServers];
        PQ = new double[Parameters.numServices][Parameters.numFogNodes];
        PQp = new double[Parameters.numServices][Parameters.numCloudServers];

        for (int j = 0; j < Parameters.numFogNodes; j++) {
            n[j] = 4;
        }
        for (int k = 0; k < Parameters.numCloudServers; k++) {
            n[k] = 10;
        }
    }

    public void initialize() {
        calcServiceFractionFog();
        calcServiceFractionCloud();
        calcRhoFog();
        calcRhoCloud();
        calcP0Fog();
        calcP0Cloud();
        calcPQFog();
        calcPQCloud();
    }

    /**
     * Calculates d_{aj} Also updates the traffic based on x[a][j]
     *
     * @param a
     * @param j
     * @return
     */
    public double calcServiceDelay(int a, int j) {
        double proc_time;
        int k = Parameters.h[j];
        if (heuristic.x[a][j] == 1) { // if the service is implelemted at the fog
//            proc_time = calcProcTimeMM1(heuristic.traffic.arrivalFog[j], Parameters.KP[j]); // MM1
            proc_time = calcProcTimeMMCfog(a, j);
            return (2 * Parameters.dIF[j]) + (proc_time) + ((Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rIF[j] * 1000d); // this is in ms
        } else if (heuristic.xp[a][k] == 1) { // if the service is implelemted in the cloud
//            proc_time = calcProcTimeMM1(heuristic.traffic.arrivalCloud[k], Parameters.KpP[k]); //MM1
            proc_time = calcProcTimeMMCcloud(a, k);
            
            return (2 * (Parameters.dIF[j] + Parameters.dFC[j][k])) + (proc_time) + (((Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rIF[j] + (Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rFC[j][k]) * 1000d); // this is in ms
        } else { // serivce a is not implemented anywhere, delay is not defined
            return Double.NaN;
        }
    }

    private double calcProcTimeMMCcloud(int a, int k) {
        if (fp[a][k] == 0) { // if the service is not implemented in cloud
            System.out.println("servcie "+a+" is not implemtend on cloud server "+k);
            System.out.println(heuristic.scheme.type);
            return 3000d; // a big number
        }
        return 1 / ((fp[a][k] * Parameters.KpP[k]) / np[k]) + PQp[a][k] / (fp[a][k] * Parameters.KpP[k] - heuristic.traffic.arrivalCloud[a][k]);
    }

    private double calcProcTimeMMCfog(int a, int j) {
        if (f[a][j] == 0) { // if the service is not implemented in cloud
            return 2000d; // a big number
        }
        return 1 / ((f[a][j] * Parameters.KP[j]) / n[j]) + PQ[a][j] / (f[a][j] * Parameters.KP[j] - heuristic.traffic.arrivalFog[a][j]);
    }

    /**
     * calculates PQ (Erlang's C) for all services on all cloud nodes
     */
    private void calcPQCloud() {
        PQp = calcPQ(Parameters.numCloudServers, np, rhop, P0p);
    }

    /**
     * calculates PQ (Erlang's C) for all services on all fog nodes
     */
    private void calcPQFog() {
        PQ = calcPQ(Parameters.numFogNodes, n, rho, P0);

    }

    /**
     * calculates PQ (Erlang's C)
     *
     * @param numNodes
     * @param numServers
     * @return PQ
     */
    private double[][] calcPQ(int numNodes, int[] numServers, double[][] rho, double[][] P0) {
        double[][] PQ = new double[Parameters.numServices][numNodes];
        double d1, d2;
        for (int i = 0; i < numNodes; i++) {
            for (int a = 0; a < Parameters.numServices; a++) {
                if (rho[a][i] == Double.POSITIVE_INFINITY) { // this is when f[a][i] = 0 (service is not implemeted)
                    continue;
                } else {
                    d1 = (Math.pow(numServers[i] * rho[a][i], numServers[i])) / Factorial.fact[numServers[i]];
                    d2 = P0[a][i] / (1 - rho[a][i]);
                    PQ[a][i] = d1 * d2;
                }
            }
        }
        return PQ;
    }

    /**
     * Calculates P0 for all services on all cloud nodes
     */
    private void calcP0Cloud() {
        P0p = calcP0(Parameters.numCloudServers, np, rhop);
    }

    /**
     * Calculates P0 for all services on all fog nodes
     */
    private void calcP0Fog() {
        P0 = calcP0(Parameters.numFogNodes, n, rho);
    }

    /**
     * Calculates P0
     *
     * @param numNodes
     * @param numServers
     * @param rho
     * @return P0
     */
    private double[][] calcP0(int numNodes, int[] numServers, double[][] rho) {
        double[][] P0 = new double[Parameters.numServices][numNodes];
        double sum;
        double d1, d2;
        for (int i = 0; i < numNodes; i++) {
            for (int a = 0; a < Parameters.numServices; a++) {
                if (rho[a][i] == Double.POSITIVE_INFINITY) { // this is when f[a][i] = 0 (service is not implemeted)
                    continue;
                } else {
                    sum = 0;
                    for (int c = 0; c <= numServers[i] - 1; c++) {
                        sum = sum + ((Math.pow(numServers[i] * rho[a][i], c)) / Factorial.fact[c]);
                    }
                    d1 = ((Math.pow(numServers[i] * rho[a][i], numServers[i])) / Factorial.fact[numServers[i]]);
                    d2 = 1 / (1 - rho[a][i]);
                    P0[a][i] = 1 / (sum + d1 * d2);
                }
            }
        }
        return P0;
    }

    /**
     * Calculates rho for all services on all cloud nodes
     *
     * @param heuristic
     */
    private void calcRhoCloud() {
        calcRho(Parameters.numCloudServers, heuristic.xp, heuristic.traffic.arrivalCloud, fp, Parameters.KpP, rhop);
    }

    /**
     * Calculates rho for all services on all fog nodes
     *
     * @param heuristic
     */
    private void calcRhoFog() {
        calcRho(Parameters.numFogNodes, heuristic.x, heuristic.traffic.arrivalFog, f, Parameters.KP, rho);
    }

    /**
     * Calculates rho
     *
     * @param numNodes
     * @param placement either x or xp
     * @param serviceArrivalRate either LAMBDA or LAMBDA'
     * @param f either f or f'
     * @param totalServiceRate either KP or KpP
     * @param rho
     */
    private void calcRho(int numNodes, int[][] placement, double[][] serviceArrivalRate, double[][] f, double[] totalServiceRate, double[][] rho) {
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int i = 0; i < numNodes; i++) {
                if (placement[a][i] != 0) {
                    rho[a][i] = serviceArrivalRate[a][i] / (f[a][i] * totalServiceRate[i]);
                } else {
                    rho[a][i] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    /**
     * Calculates f' (the fraction of service rate that service a obtains at
     * cloud node j) for all services on all cloud nodes
     *
     * @param heuristic
     */
    private void calcServiceFractionCloud() {
        fp = calcServiceFraction(Parameters.numCloudServers, heuristic.xp);
    }

    /**
     * Calculates f (the fraction of service rate that service a obtains at fog
     * node j) for all services on all fog nodes
     *
     * @param heuristic
     */
    private void calcServiceFractionFog() {
        f = calcServiceFraction(Parameters.numFogNodes, heuristic.x);
    }

    /**
     * Calculates f
     *
     * @param numNodes
     * @param placement
     * @return
     */
    private double[][] calcServiceFraction(int numNodes, int[][] placement) {
        double[][] f = new double[Parameters.numServices][numNodes];
        double sum;
        for (int i = 0; i < numNodes; i++) {
            sum = 0;
            for (int a = 0; a < Parameters.numServices; a++) {
                sum += (placement[a][i] * Parameters.L_P[a]);
            }
            for (int a = 0; a < Parameters.numServices; a++) {
                if (sum == 0) {
                    f[a][i] = 0;
                } else {
                    f[a][i] = (placement[a][i] * Parameters.L_P[a]) / sum;
                }
            }
        }
        return f;
    }

    /**
     * Calculates processing time of a job if the underlying model is M-M-1
     *
     * @param arrivalRate total arrival rate of a node
     * @param serviceRate total service rate of a node
     * @return returns the processing time of the job, if arrivalRate is less
     * than serviceRate, otherwise, it will return a big number
     */
    private static double calcProcTimeMM1(double arrivalRate, double serviceRate) {
        double proc_time;
        if (arrivalRate > serviceRate) {
            proc_time = 2000d; // a big number
            System.out.println("too much load");
        } else {
            proc_time = 1 / (serviceRate - arrivalRate) * 1000d; // so that it is in ms
        }
        return proc_time;
    }

    public static void setThresholds(double threshold) {
        for (int a = 0; a < Parameters.numServices; a++) {
            Parameters.th[a] = threshold;
        }
    }

    public static double getThresholdAverage() {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += Parameters.th[a];
        }
        return (sum / Parameters.numServices);
    }

}
