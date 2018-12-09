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
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                initFog(a, j);
            }
            for (int k = 0; k < Parameters.numCloudServers; k++) {
                initCloud(a, k);
            }
        }
    }
    
    private void initFog(int a, int j) {
        calcServiceFractionFog(a, j);
        calcRhoFog(a, j);
        calcP0Fog(a, j);
        calcPQFog(a, j);
    }
    
    private void initCloud(int a, int k) {
        calcServiceFractionCloud(a, k);
        calcRhoCloud(a, k);
        calcP0Cloud(a, k);
        calcPQCloud(a, k);
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
        initCloud(a, k);
        if (fp[a][k] == 0) { // if the service is not implemented in cloud
            System.out.println("servcie " + a + " is not implemtend on cloud server " + k); // this is for debug
            System.out.println(heuristic.scheme.type); // this is for debug
            return 3000d; // a big number
        }
        return 1 / ((fp[a][k] * Parameters.KpP[k]) / np[k]) + PQp[a][k] / (fp[a][k] * Parameters.KpP[k] - heuristic.traffic.arrivalCloud[a][k]);
    }
    
    private double calcProcTimeMMCfog(int a, int j) {
        initFog(a, j);
        if (f[a][j] == 0) { // if the service is not implemented in cloud
            return 2000d; // a big number
        }
        return 1 / ((f[a][j] * Parameters.KP[j]) / n[j]) + PQ[a][j] / (f[a][j] * Parameters.KP[j] - heuristic.traffic.arrivalFog[a][j]);
    }

    /**
     * calculates PQ (Erlang's C) for all services on all cloud nodes
     */
    private void calcPQCloud(int a, int k) {
        calcPQ(a, k, np, rhop, P0p, PQp);
    }

    /**
     * calculates PQ (Erlang's C) for all services on all fog nodes
     */
    private void calcPQFog(int a, int j) {
        calcPQ(a, j, n, rho, P0, PQ);
        
    }

    /**
     * calculates PQ (Erlang's C)
     *
     * @param numNodes
     * @param numServers
     * @return PQ
     */
    private void calcPQ(int a, int i, int[] numServers, double[][] rho, double[][] P0, double[][] PQ) {
        double d1, d2;
        if (rho[a][i] == Double.POSITIVE_INFINITY) { // this is when f[a][i] = 0 (service is not implemeted)
            return;
        } else {
            d1 = (Math.pow(numServers[i] * rho[a][i], numServers[i])) / Factorial.fact[numServers[i]];
            d2 = P0[a][i] / (1 - rho[a][i]);
            PQ[a][i] = d1 * d2;
        }
    }

    /**
     * Calculates P0 for all services on all cloud nodes
     */
    private void calcP0Cloud(int a, int k) {
        calcP0(a, k, np, rhop, P0p);
    }

    /**
     * Calculates P0 for all services on all fog nodes
     */
    private void calcP0Fog(int a, int j) {
        calcP0(a, j, n, rho, P0);
    }

    /**
     * Calculates P0
     *
     * @param numNodes
     * @param numServers
     * @param rho
     * @return P0
     */
    private void calcP0(int a, int i, int[] numServers, double[][] rho, double[][] P0) {
        double sum = 0;
        double d1, d2;
        if (rho[a][i] == Double.POSITIVE_INFINITY) { // this is when f[a][i] = 0 (service is not implemeted)
            return;
        } else {
            for (int c = 0; c <= numServers[i] - 1; c++) {
                sum = sum + ((Math.pow(numServers[i] * rho[a][i], c)) / Factorial.fact[c]);
            }
            d1 = ((Math.pow(numServers[i] * rho[a][i], numServers[i])) / Factorial.fact[numServers[i]]);
            d2 = 1 / (1 - rho[a][i]);
            P0[a][i] = 1 / (sum + d1 * d2);
        }
    }

    /**
     * Calculates rho for all services on all cloud nodes
     *
     * @param heuristic
     */
    private void calcRhoCloud(int a, int k) {
        calcRho(a, k, heuristic.xp, heuristic.traffic.arrivalCloud, fp, Parameters.KpP, rhop);
    }

    /**
     * Calculates rho for all services on all fog nodes
     *
     * @param heuristic
     */
    private void calcRhoFog(int a, int j) {
        calcRho(a, j, heuristic.x, heuristic.traffic.arrivalFog, f, Parameters.KP, rho);
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
    private void calcRho(int a, int i, int[][] placement, double[][] serviceArrivalRate, double[][] f, double[] totalServiceRate, double[][] rho) {
        if (placement[a][i] != 0) {
            rho[a][i] = serviceArrivalRate[a][i] / (f[a][i] * totalServiceRate[i]);
        } else {
            rho[a][i] = Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Calculates f' (the fraction of service rate that service a obtains at
     * cloud node j) for all services on all cloud nodes
     *
     * @param heuristic
     */
    private void calcServiceFractionCloud(int a, int k) {
        fp[a][k] = calcServiceFraction(heuristic.xp, a, k);
    }

    /**
     * Calculates f (the fraction of service rate that service a obtains at fog
     * node j) for all services on all fog nodes
     *
     * @param heuristic
     */
    private void calcServiceFractionFog(int a, int j) {
        f[a][j] = calcServiceFraction(heuristic.x, a, j);
    }

    /**
     * Calculates f_ai
     *
     * @param placement
     * @param a
     * @param i
     * @return
     */
    private double calcServiceFraction(int[][] placement, int a, int i) {
        double f;
        double sum = 0;
        for (int s = 0; s < Parameters.numServices; s++) {
            sum += (placement[s][i] * Parameters.L_P[s]);
        }
        if (sum == 0) {
            f = 0;
        } else {
            f = (placement[a][i] * Parameters.L_P[a]) / sum;
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
