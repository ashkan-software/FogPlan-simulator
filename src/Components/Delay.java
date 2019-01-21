package Components;

import Scheme.Parameters;
import Utilities.Factorial;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains all the functions and parameters that are related to the
 * delay
 */
public class Delay {

    private double[][] rho; // rho_aj
    private double[][] rhop; //rho'_ak

    private double[][] f; // f_aj
    private double[][] fp; // f'_ak

    private int[] n; // n_j: processing units of fog node j
    private int[] np; // f'_ak: processing units of cloud server k

    private double P0[][]; // P^0 in M/M/c queueing model
    private double PQ[][]; // P^Q in M/M/c queueing model

    private double P0p[][]; // P'^0 in M/M/c queueing model
    private double PQp[][]; // P'^Q in M/M/c queueing model

    private Method method;

    /**
     *
     * @param method
     */
    public Delay(Method method) {
        this.method = method;

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
            n[j] = 4; // number of processors in a fog node
        }
        for (int k = 0; k < Parameters.numCloudServers; k++) {
            n[k] = 8; // number of processors in a cloud server
        }
    }

    /**
     * Initializes the delay queueing delay parameters for all services
     */
    public void initialize() {
        for (int a = 0; a < Parameters.numServices; a++) {
            initialize(a);
        }
    }

    /**
     * Initializes the delay queueing delay parameters for service a
     *
     * @param a the index of the service
     */
    public void initialize(int a) {
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            initFog(a, j);
        }
        for (int k = 0; k < Parameters.numCloudServers; k++) {
            initCloud(a, k);
        }
    }

    /**
     * Initializes the delay queueing delay parameters for service a on a fog
     * node j
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private void initFog(int a, int j) {
        calcServiceFractionFog(a, j);
        calcRhoFog(a, j);
        calcP0Fog(a, j);
        calcPQFog(a, j);
    }

    /**
     * Initializes the delay queueing delay parameters for a service on a cloud
     * server j
     *
     * @param a the index of the service
     * @param k the index of the cloud server
     */
    private void initCloud(int a, int k) {
        calcServiceFractionCloud(a, k);
        calcRhoCloud(a, k);
        calcP0Cloud(a, k);
        calcPQCloud(a, k);
    }

    /**
     * Calculates service delay (d_{aj}).
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    public double calcServiceDelay(int a, int j) {
        double proc_time;
        int k = Parameters.h[a][j];
        if (method.x[a][j] == 1) { // if the service is implelemted at the fog
//            proc_time = calcProcTimeMM1(heuristic.traffic.arrivalInstructionsFog[j], Parameters.KP[j]); // MM1
            proc_time = calcProcTimeMMCfog(a, j); // MMC
            return (2 * Parameters.dIF[j]) + (proc_time) + ((Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rIF[j] * 1000d); // this is in ms
        } else { // if the service is implelemted in the cloud
//            proc_time = calcProcTimeMM1(heuristic.traffic.arrivalInstructionsCloud[k], Parameters.KpP[k]); //MM1
            proc_time = calcProcTimeMMCcloud(a, k); // MMC
            return (2 * (Parameters.dIF[j] + Parameters.dFC[j][k])) + (proc_time) + (((Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rIF[j] + (Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rFC[j][k]) * 1000d); // this is in ms
        }
    }

    /**
     * Calculates processing time of a service in a cloud server according to an
     * M/M/c model
     *
     * @param a te index of service
     * @param k the index of cloud server
     * @return returns the processing time of service a in cloud service k, if
     * the queue is stable. Otherwise, it will return a big number and prints
     * and error message
     */
    private double calcProcTimeMMCcloud(int a, int k) {
        initCloud(a, k);
        if (fp[a][k] == 0) { // if the service is not implemented in cloud
            System.out.println("servcie " + a + " is not implemtend on cloud server " + k); // this is for debug
            System.out.println("Debug Please! Scheme: " + method.scheme.type); // this is for debug
            return Double.MAX_VALUE; // a big number
        }
        return 1 / ((fp[a][k] * Parameters.KpP[k]) / np[k]) + PQp[a][k] / (fp[a][k] * Parameters.KpP[k] - method.traffic.arrivalInstructionsCloud[a][k]);
    }

    /**
     * Calculates processing time of a service in a fog node according to an
     * M/M/c model (It is only called if the service a is implemented on fog
     * node j.)
     *
     * @param a the index of the service
     * @param j the index of the fog node
     * @return returns the processing time of service a in fog node j, if the
     * queue is stable. Otherwise, it will return a big number.
     */
    private double calcProcTimeMMCfog(int a, int j) {
        initFog(a, j);
        if (f[a][j] == 0) { // if the service a is implemented on fog node j, but f_aj = 0, there should be a bug! 
            System.out.println("Debug Please! Scheme: " + method.scheme.type); // this is for debug
            return Double.MAX_VALUE; // a big number
        }
        if (f[a][j] * Parameters.KP[j] < method.traffic.arrivalInstructionsFog[a][j]) {
            return 20; // (ms) a big number
        }
        return 1 / ((f[a][j] * Parameters.KP[j]) / n[j]) + PQ[a][j] / (f[a][j] * Parameters.KP[j] - method.traffic.arrivalInstructionsFog[a][j]);
    }

    /**
     * calculates P'^Q (Erlang's C) for a service on a cloud server
     *
     * @param a the index of the service
     * @param k the index of the cloud server
     */
    private void calcPQCloud(int a, int k) {
        calcPQ(a, k, np, rhop, P0p, PQp);
    }

    /**
     * calculates P^Q (Erlang's C) for a service on a fog node
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private void calcPQFog(int a, int j) {
        calcPQ(a, j, n, rho, P0, PQ);

    }

    /**
     * calculates PQ (Erlang's C) for a service on a node (either cloud or fog)
     *
     * @param a the index of the service
     * @param i the index of the node
     * @param numServers number of servers
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
     * calculates P'^0 (Erlang's C) for a service on a cloud server
     *
     * @param a the index of the service
     * @param k the index of the cloud server
     */
    private void calcP0Cloud(int a, int k) {
        calcP0(a, k, np, rhop, P0p);
    }

    /**
     * calculates P^0 (Erlang's C) for a service on a fog node
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private void calcP0Fog(int a, int j) {
        calcP0(a, j, n, rho, P0);
    }

    /**
     * Calculates P0 for a service on a node (either cloud or fog)
     *
     * @param a the index of the service
     * @param i the index of the node
     * @param numServers number of servers
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
     * Calculates rho for a service on a cloud node
     *
     * @param a the index of the service
     * @param k the index of the cloud server
     */
    private void calcRhoCloud(int a, int k) {
        calcRho(a, k, method.xp, method.traffic.arrivalInstructionsCloud, fp, Parameters.KpP, rhop);
    }

    /**
     * Calculates rho for a service on a fog node
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private void calcRhoFog(int a, int j) {
        calcRho(a, j, method.x, method.traffic.arrivalInstructionsFog, f, Parameters.KP, rho);
    }

    /**
     * Calculates rho for a service on a node (either cloud or fog)
     *
     * @param a index of the service
     * @param i index of the node
     * @param placement either x or xp
     * @param serviceArrivalRate either LAMBDA or LAMBDA'
     * @param f either f or f'
     * @param totalServiceRate either KP or KpP
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
     * cloud node k) a service on a cloud server
     *
     * @param a the index of the service
     * @param k the index of the cloud server
     */
    private void calcServiceFractionCloud(int a, int k) {
        fp[a][k] = calcServiceFraction(method.xp, a, k);
    }

    /**
     * Calculates f (the fraction of service rate that service a obtains at fog
     * node j) for a service on a fog node
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private void calcServiceFractionFog(int a, int j) {
        f[a][j] = calcServiceFraction(method.x, a, j);
    }

    /**
     * Calculates f_ai for a service on a node (either cloud or fog)
     *
     * @param placement either x or xp
     * @param a index of the service
     * @param i index of the node
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
     * Calculates processing time of a job if the underlying model of the fog
     * node is M/M/1
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

    /**
     * Set all threshold to a specific value
     *
     * @param threshold
     */
    public static void setThresholds(double threshold) {
        for (int a = 0; a < Parameters.numServices; a++) {
            Parameters.th[a] = threshold;
        }
    }

    /**
     * Returns the average value of all thresholds
     */
    public static double getThresholdAverage() {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += Parameters.th[a];
        }
        return (sum / Parameters.numServices);
    }
    
     /**
     * Calculates the delay of deployment of a service on a fog node.
     * Deploy delay consists of container download from Fog Service Controller,
     * and container startup time Everything is in ms.
     * 
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private double calcDeployDelay(int a, int j) {
        return Parameters.L_S[a] / Parameters.rFContr[j] * 1000 + Parameters.CONTAINER_INIT_DELAY;
    }

}
