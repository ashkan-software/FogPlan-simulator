package Simulation;

import Run.RunParameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Utilities.ArrayFiller;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Ashkan Y.
 */
public class Heuristic {

    private static double th[]; // threshold
    private static double q[]; // quality of service for service a

    private static double dIF[]; // average propagation delay from IoT nodes to fog node j (*can be measured and shown in paper by trace*)
    private static double rIF[]; // average transmission rate from IoT nodes to fog node j (*can be measured and shown in paper by trace*)

    private static double dFC[][]; // propagation delay from fog node j to cloud node k (*can be measured and shown in paper by trace*)
    private static double rFC[][]; // average transmission rate from fog node j to cloud node k (*can be measured and shown in paper by trace*)

    public static double l_rq[]; // average request length of service a
    public static double l_rp[]; // average response length of service a

    private static double KP[]; // processing capacity (service rate) of fog node j
    private static double KpP[]; // processing capacity (service rate) of cloud server k

    private static double KM[]; // memory capacity of fog node j, in bytes
    private static double KpM[]; // memory capacity of cloud server k, in bytes

    private static double KS[]; // storage capacity of fog node j, in bytes
    private static double KpS[]; // storage capacity of cloud server k, in bytes

    public final static double KP_min = 800d;
    public final static double L_P_max = 200d;

    private double[][] backup_lambda_in;

    private static int CONTAINER_INIT_DELAY = 50; // 50 ms -> CONTAINER-AS-A-SERVICE AT THE EDGE: TRADE- OFF BETWEEN ENERGY EFFICIENCY AND SERVICE AVAILABILITY AT FOG NANO DATA CENTERS

    public static int COMBINED_APP_REGIONES = 1;
    public static int COMBINED_APP = 2;
    public static int NOT_COMBINED = 3;

    private static ArrayList<HashSet<Integer>> h_reverse; // set of fog nodes j that send their traffic to cloud server k (associated fog nodes to cloud server k)
    private static int[] h; // map given fog node to the associated cloud node

    private static double[] L_P; // amount of required processing for service a per unit traffic, in MIPS
    private static double[] L_S; // size of service (i.e. container) a,
    private static double[] L_M; // required amount of memory for service a, in bytes

    // note that the delay of deploying containers is not considered yet, since we don't really need to when the interval of changing traffic is in the order of seconds (e.g. 5s or 60s)
    // this is because, even if we consider the 50ms delay, it will not affect the resutls.  
    private static double rFContr[]; // transmission rate from fog node j to the fog service controller
    private ServiceCounter fogStaticDeployedContainers;

    private static Cost cost;

    private double proc_time;

    private boolean firstTimeDone = false;

    private static int numFogNodes;
    private static int numServices;
    private static int numCloudServers;

    private static double[] ServiceTrafficPercentage;

    private int[][] x; // x_aj
    private int[][] x_backup;
    private int[][] xp; // x'_ak
    private int[][] v; // v_aj

    private double d[][]; // stores d_aj
    private double Vper[]; // V^%_a

    private static double globalTraffic[][]; // this is a static version of traffic, which must remain the same

    private double lambda_in[][]; // lambda^in_aj
    private double lambdap_in[][]; // lambda'^in_ak

    private double lambda_out[][]; // lambda^out_aj

    private double arrivalCloud[]; // LAMBDA^C
    private double arrivalFog[]; // LAMBDA^F

    private int type;
    private ServiceDeployScheme scheme;

    public Heuristic(ServiceDeployScheme scheme, int numFogNodes, int numServices, int numCloudServers) {

        lambda_in = new double[numServices][numFogNodes];
        lambdap_in = new double[numServices][numCloudServers];
        lambda_out = new double[numServices][numFogNodes];
        arrivalCloud = new double[numCloudServers];
        arrivalFog = new double[numFogNodes];

        this.scheme = scheme;
        type = scheme.type;
        x = scheme.variable.x;
        x_backup = scheme.variable.x_backup;
        xp = scheme.variable.xp;
        v = scheme.variable.v;

        d = scheme.variable.d;
        Vper = scheme.variable.Vper;

        this.numFogNodes = numFogNodes;
        this.numServices = numServices;
        this.numCloudServers = numCloudServers;

        backup_lambda_in = new double[numServices][numFogNodes];
    }

    public static void initializeStaticVariables() {

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

        h = new int[numFogNodes];
        for (int j = 0; j < numFogNodes; j++) {
            h[j] = (int) (Math.random() * numCloudServers);
        }

        h_reverse = new ArrayList<>(numCloudServers);
        for (int k = 0; k < numCloudServers; k++) {
            HashSet<Integer> single_h_reverse = new HashSet<>();

            for (int j = 0; j < numFogNodes; j++) {
                if (h[j] == k) {
                    single_h_reverse.add(j);
                }
            }
            h_reverse.add(k, single_h_reverse); // addd the mapping to the arrayList of reverse mappings
        }
        cost = new Cost(numCloudServers, numFogNodes, numServices);

        ServiceTrafficPercentage = new double[numServices];
        generateServiceTrafficPercentage(); // ServiceTrafficPercentage is initialized
    }

    public ServiceCounter run(int traceType, boolean justMinimizeViolation) {
        backupAllPlacements();
        if (type == ServiceDeployScheme.ALL_CLOUD) {
            // do not change the placement
            return new ServiceCounter(0, numCloudServers * numServices);
        } else if (type == ServiceDeployScheme.ALL_FOG) {
            // do not change the placement
            return new ServiceCounter(numFogNodes * numServices, 0);
        } else if (type == ServiceDeployScheme.OPTIMAL) {
            return runOptimal();
        } else if (type == ServiceDeployScheme.FOG_STATIC) { // FOG_STATIC
            backupIncomingTraffic();
            return runFogStatic(traceType, justMinimizeViolation);
        } else { // FOG_DYNAMIC
            return runFogDynamic(justMinimizeViolation);
        }
    }

    private ServiceCounter runOptimal() {
        
        Optimization.init(numServices, numFogNodes, numCloudServers);
        long numCombinations = (long) Math.pow(2, numServices * (numFogNodes + numCloudServers)); // x_aj and xp_ak
        double minimumCost = Double.MAX_VALUE, cost;
        for (long combination = 0; combination < numCombinations; combination++) {
            updateDecisionVariablesAccordingToCombination(combination); // updates x, xp
            calcNormalizedArrivalRateFogNodes();
            calcNormalizedArrivalRateCloudNodes();
            if (Optimization.optimizationConstraintsSatisfied(x, xp, numServices, numFogNodes, numCloudServers, L_S, L_M, KS, KM, KpS, KpM, lambdap_in)) {
                cost = getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                if (cost < minimumCost) {
                    minimumCost = cost;
                    Optimization.updateBestDecisionVaraibles(x, xp, numServices, numFogNodes, numCloudServers);
                }
            }
        }
        Optimization.updateDecisionVaraiblesAccordingToBest(x, xp, numServices, numFogNodes, numCloudServers);
//            System.out.println("Optimal");
//            for (int a = 0; a < numServices; a++) {
//                for (int j = 0; j < numFogNodes; j++) {
//                    System.out.print(x[a][j] + " ");
//                }
//                System.out.print("| ");
//                for (int k = 0; k < numCloudServers; k++) {
//                    System.out.print(xp[a][k] + " ");
//                }
//                System.out.println("");
//            }
        return ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);
    }

    private ServiceCounter runFogStatic(int traceType, boolean justMinimizeViolation) {
        if (!firstTimeDone) { // if it is the first time

            firstTimeDone = true; // it does not run the algorithm after the first time
            if (traceType == NOT_COMBINED) {
                initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode();
            } else if (traceType == COMBINED_APP_REGIONES) {
                initializeAvgTrafficForStaticFogPlacementFirstTimeCombined(); // now lambda values are based on average
            } else { // if (traceType == COMBINED_APP)
                initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode();
            }
            
            for (int a = 0; a < numServices; a++) {
                if (justMinimizeViolation) {
                    FogServicePlacementMinViolationHeuristic(a);
                } else {
                    FogServicePlacementMinCostHeuristic(a);
                }
            }
            fogStaticDeployedContainers = ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);
            restoreIncomingTraffic();
            return fogStaticDeployedContainers;
        } else {
            // do not change the placement
            return fogStaticDeployedContainers;
        }
    }

    private ServiceCounter runFogDynamic(boolean justMinimizeViolation) {
        for (int a = 0; a < numServices; a++) {
            if (justMinimizeViolation) {
                FogServicePlacementMinViolationHeuristic(a);
            } else {
                FogServicePlacementMinCostHeuristic(a);
            }
        }
//                System.out.println("Dynamic");
//                for (int a = 0; a < numServices; a++) {
//                    for (int j = 0; j < numFogNodes; j++) {
//                        System.out.print(x[a][j] + " ");
//                    }
//                    System.out.print("| ");
//                    for (int k = 0; k < numCloudServers; k++) {
//                        System.out.print(xp[a][k] + " ");
//                    }
//                    System.out.println("");
//                }
        return ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);

    }

    private static void generateServiceTrafficPercentage() {
        ArrayFiller.generateRandomDistributionOnArray(ServiceTrafficPercentage, 1d, 5.5d);

    }

    public void unsetFirstTimeBoolean() {
        firstTimeDone = false;
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic to the average traffic values, so that the
     * placement solves the problem based on average
     */
    private void initializeAvgTrafficForStaticFogPlacementFirstTimeCombined() {
        distributeTraffic(scheme.averageRateOfTraffic, lambda_in);
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic per fog node to the average traffic values, so
     * that the placement solves the problem based on averages
     */
    private void initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode() {
        distributeTraffic(scheme.averageRateOfCombinedAppTrafficPerNode, lambda_in);
    }

    /**
     * This function is called the first time when StaticFogPlacement is called,
     * and changes the traffic per fog node per service to the average traffic
     * values, so that the placement solves the problem based on averages
     */
    private void initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode() {
        setTraffic(scheme.averageRateOfTrafficPerNodePerService, lambda_in);
    }

    private static void distributeTraffic(double trafficPerNodePerApp, double[][] targetTraffic) {
        double totalTraffic = trafficPerNodePerApp * numFogNodes * numServices;
        double trafficForCurrentService;
        double[] fogTrafficPercentage = new double[numFogNodes];
        for (int a = 0; a < numServices; a++) {
            trafficForCurrentService = totalTraffic * ServiceTrafficPercentage[a];
            ArrayFiller.generateRandomDistributionOnArray(fogTrafficPercentage, 1, 7);
            for (int j = 0; j < numFogNodes; j++) {
                targetTraffic[a][j] = trafficForCurrentService * fogTrafficPercentage[j];
            }
        }
    }

    public static void distributeTraffic(double trafficPerNodePerApp) {
        distributeTraffic(trafficPerNodePerApp, globalTraffic);
    }

    private static void distributeTraffic(Double[] combinedTrafficPerFogNode, double[][] targetTraffic) {
        for (int j = 0; j < numFogNodes; j++) {
            for (int a = 0; a < numServices; a++) {
                targetTraffic[a][j] = combinedTrafficPerFogNode[j] * ServiceTrafficPercentage[a];
            }
        }
    }

    public static void distributeTraffic(Double[] combinedTrafficPerFogNode) {
        distributeTraffic(combinedTrafficPerFogNode, globalTraffic);
    }

    private static void setTraffic(Double[][] newTraffic, double[][] targetTraffic) {
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                targetTraffic[a][j] = newTraffic[a][j];
            }
        }
    }

    public static void setTraffic(Double[][] actualTraffic) {
        setTraffic(actualTraffic, globalTraffic);
    }

    public void setTrafficToGlobalTraffic() {
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                lambda_in[a][j] = globalTraffic[a][j];
            }
        }
    }

    public double getAvgServiceDelay() {
        double sumNum = 0;
        double sumDenum = 0;
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                sumNum += calcServiceDelay(a, j) * lambda_in[a][j];
                sumDenum += lambda_in[a][j];
            }
        }
        return sumNum / sumDenum;
    }

    public double getCost(double timeDuration) {
        for (int a = 0; a < numServices; a++) {
            calcViolation(a); // updates traffic values, average service delay, and violation
        }
        return cost.calcCost(timeDuration, x, xp, x_backup, Vper, q, lambda_in, lambdap_in, lambda_out, L_P, L_S, h);
    }

    public void printAllocation() {
        System.out.print("Fog");
        for (int j = 0; j < numFogNodes; j++) {
            System.out.print("  ");
        }
        System.out.println("Cloud");
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                System.out.print(x[a][j] + " ");
            }
            System.out.print("   ");
            for (int k = 0; k < numCloudServers; k++) {
                System.out.print(xp[a][k] + " ");
            }
            System.out.println("");
        }
    }

    public void printTraffic() {
        DecimalFormat df = new DecimalFormat("0.00");
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {

                System.out.print(df.format(lambda_in[a][j]) + " ");
            }
            System.out.println("");
        }
    }

    public void printAverageCost() {
        cost.printAverageCost();
    }

    private void FogServicePlacementMinViolationHeuristic(int a) {
        calcViolation(a);
        List<FogTrafficIndex> fogTrafficIndex = getFogIncomingTraffic(a, false);
        Collections.sort(fogTrafficIndex);
        int listIndex = -1;
        int j = 0;

        while (Vper[a] > 1 - q[a] && listIndex < numFogNodes - 1) {
            listIndex++;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 0 && fogResourceConstraintsSatisfied(j)) { // if service a is not implemented on fog node j
                // to add CODE: DEPLOY
//               System.out.println("dep "+a+" "+j);
                x[a][j] = 1;
                calcViolation(a);
            }

        }
        boolean canRelease = true;
        listIndex = fogTrafficIndex.size();
        while (canRelease && listIndex > 0) {
            listIndex--;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 1) { // if service a is implemented on fog node j
                x[a][j] = 0;
                calcViolation(a);
                if (Vper[a] <= 1 - q[a]) {
                    // to add CODE: RELEASE
                } else {
                    x[a][j] = 1;
                    calcViolation(a);
                    canRelease = false;
                }
            }

        }
        deployCloudServiceIfNeeded(a);

    }

    private void FogServicePlacementMinCostHeuristic(int a) {
        calcViolation(a);
        List<FogTrafficIndex> fogTrafficIndex = getFogIncomingTraffic(a, false);
        Collections.sort(fogTrafficIndex);
        int listIndex = -1;
        int j;

        while (listIndex < numFogNodes - 1) {
            listIndex++;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 0 && fogResourceConstraintsSatisfied(j)) { // if service a is not implemented on fog node j
                if (deployMakesSense(a, j)) {
                    // to add CODE: DEPLOY
//                    System.out.println("delay" + calcDeployDelay(a, j)); // not yet used
                    x[a][j] = 1;
                    calcViolation(a);
                }
            }
        }
        listIndex = numFogNodes;
        while (listIndex > 0) {
            listIndex--;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 1) { // if service a is implemented on fog node j
                if (releaseMakesSense(a, j)) {
                    // to add CODE: RELEASE
                    x[a][j] = 0;
                    calcViolation(a);
                }
            }
        }
        deployCloudServiceIfNeeded(a);
    }

    /**
     * Requirement: x[a][j] = 0;
     *
     * @param a
     * @param j
     * @return
     */
    private boolean deployMakesSense(int a, int j) {
        double loss = 0;
        double savings = 0;
        double aa, b, c, e;
        //if not deployiing (X[a][j] == 0) this is the cost we were paying, 
        // but now this is seen as savings
//        savings += Cost.costCfc(RunParameters.TAU, j, a, lambda_out, h);
//        savings += Cost.costPC(RunParameters.TAU, h[j], a, L_P, lambdap_in);
//        savings += Cost.costSC(RunParameters.TAU, h[j], a, L_S);
//        savings += Cost.costViolPerFogNode(RunParameters.TAU, a, calcVper(a, j));
        aa = Cost.costCfc(RunParameters.TAU, j, a, lambda_out, h);
        b = Cost.costExtraPC(RunParameters.TAU, h[j], a, L_P, lambda_out[a][j]);
        c = Cost.costExtraSC(RunParameters.TAU, h[j], a, L_S, xp);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        e = Cost.costViolPerFogNode(RunParameters.TAU, a, calcVper(a, j, fogTrafficPercentage), q, fogTrafficPercentage);
        savings = aa + b + c + e;

        // Now if we were to deploy, this is the cost we would pay
        x[a][j] = 1;
        d[a][j] = calcServiceDelay(a, j); // this is just to update the things

        double xx, y, z, k;
        xx = Cost.costDep(j, a, L_S);
        y = Cost.costPF(RunParameters.TAU, j, a, L_P, lambda_in);
        z = Cost.costSF(RunParameters.TAU, j, a, L_S);
        k = Cost.costViolPerFogNode(RunParameters.TAU, a, calcVper(a, j, fogTrafficPercentage), q, fogTrafficPercentage);
        loss = xx + y + z + k;

        x[a][j] = 0; // revert this back to what it was
        d[a][j] = calcServiceDelay(a, j); // revert things back to what they were
//        System.out.println("Not Dep " + a + " " + j + " costCfc:" + aa + " costPC:" + b + " costSC:" + c + " CostV:" + e);
//        System.out.println("Not Dep " + a + " " + j + " costDep:" + xx + " costPF:" + y + " costSF:" + z + " CostV:" + k);

        if (savings > loss) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Requirement: x[a][j] = 1;
     *
     * @param a
     * @param j
     * @return
     */
    private boolean releaseMakesSense(int a, int j) {
        double loss = 0;
        double savings = 0;
        //if not releasing (X[a][j] == 1) this is the cost we were paying, 
        // but now this is seen as savings
        savings += Cost.costPF(RunParameters.TAU, j, a, L_P, lambda_in);
        savings += Cost.costSF(RunParameters.TAU, j, a, L_S);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        savings += Cost.costViolPerFogNode(RunParameters.TAU, a, calcVper(a, j, fogTrafficPercentage), q, fogTrafficPercentage);

        // Now if we were to release, this is the loss we would pay
        x[a][j] = 0;
        d[a][j] = calcServiceDelay(a, j); // this is just to update the things

        loss += Cost.costCfc(RunParameters.TAU, j, a, lambda_out, h);
        loss += Cost.costExtraPC(RunParameters.TAU, h[j], a, L_P, lambda_out[a][j]);
        loss += Cost.costExtraSC(RunParameters.TAU, h[j], a, L_S, xp);

        loss += Cost.costViolPerFogNode(RunParameters.TAU, a, calcVper(a, j, fogTrafficPercentage), q, fogTrafficPercentage);

        x[a][j] = 1; // revert this back to what it was
        d[a][j] = calcServiceDelay(a, j); // revert things back to what they were
        if (savings > loss) {
//            System.out.println("Rel "+a +" "+j+ "saving:"+savings + " loss:"+loss);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deploy delay consists of container download from Fog Service Controller,
     * and container startup time Everything is in ms.
     *
     * @return
     */
    private double calcDeployDelay(int a, int j) {
        return L_S[a] / rFContr[j] * 1000 + CONTAINER_INIT_DELAY;
    }

    /**
     * Calculates percentage of traffic in a given fog node for a particular
     * service to the total traffic to all fpg nodes for taht service
     *
     * @param a
     * @param j
     * @return
     */
    private double calcFogTrafficPercentage(int a, int j) {
        double denum = 0;
        for (int fog = 0; fog < numFogNodes; fog++) {
            denum += lambda_in[a][fog];
        }
        return lambda_in[a][j] / denum;
    }

    private double calcVper(int a, int j, double fogTrafficPercentage) {
        if (d[a][j] > th[a]) {
            v[a][j] = 1;
            return fogTrafficPercentage;
        } else {
            v[a][j] = 0;
            return 0;
        }
    }

    private void backupPlacement(int a) {
        for (int j = 0; j < numFogNodes; j++) {
            x_backup[a][j] = x[a][j];
        }
    }

    private void backupAllPlacements() {
        for (int a = 0; a < numServices; a++) {
            backupPlacement(a);
        }
    }

    private void backupIncomingTraffic() {
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                backup_lambda_in[a][j] = lambda_in[a][j];
            }
        }
    }

    private void restoreIncomingTraffic() {
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                lambda_in[a][j] = backup_lambda_in[a][j];
            }
        }
    }

    /**
     * gets incoming traffic to all fog nodes for all services
     *
     * @param a index of a given service
     * @return returns an array of FogTrafficIndex, for a given service
     */
    private List<FogTrafficIndex> getFogIncomingTraffic(int a, boolean isSortAscending) {

        List<FogTrafficIndex> fogTrafficIndex = new ArrayList<>();
        for (int j = 0; j < numFogNodes; j++) {
            fogTrafficIndex.add(new FogTrafficIndex(j, lambda_in[a][j], isSortAscending));
        }
        return fogTrafficIndex;
    }

    /**
     * Calculate SLA Violation Percentage. (Percentage of IoT requests that do
     * not meet the delay requirement for service a (V^%_a))ˇ
     *
     * @param a
     */
    private void calcViolation(int a) {

        double sumNum = 0;
        double sumDenum = 0;
        for (int j = 0; j < numFogNodes; j++) {
            d[a][j] = calcServiceDelay(a, j);
            if (d[a][j] > th[a]) {
                v[a][j] = 1;
            } else {
                v[a][j] = 0;
            }
            sumNum += v[a][j] * lambda_in[a][j];
            sumDenum += lambda_in[a][j];
        }
        if (sumDenum == 0) {
            Vper[a] = 0;
        } else {
            Vper[a] = sumNum / sumDenum;
        }
    }

    public static void setThresholds(double threshold) {
        for (int a = 0; a < numServices; a++) {
            th[a] = threshold;
        }
    }

    public static double getThreshold(int a) {
        return th[a];
    }

    public static double getThresholdAverage() {
        double sum = 0;
        for (int a = 0; a < numServices; a++) {
            sum += getThreshold(a);
        }
        return (sum / numServices);
    }

    public double getViolationPercentage(int a) {
        calcViolation(a);
        return (Math.max(0, Vper[a] - (1 - q[a])) * 100);
    }

    public static double getViolationSlack(int a) {
        return (1 - q[a]) * 100;
    }

    public double getViolationPercentage() {
        double sum = 0;
        for (int a = 0; a < numServices; a++) {
            sum += getViolationPercentage(a);
        }
        return (sum / numServices);
    }

    public static double getViolationSlack() {
        double sum = 0;
        for (int a = 0; a < numServices; a++) {
            sum += getViolationSlack(a);
        }
        return (sum / numServices);
    }

    /**
     * Calculates d_{aj} Also updates the traffic based on x[a][j]
     *
     * @param a
     * @param j
     * @return
     */
    private double calcServiceDelay(int a, int j) {
        calcNormalizedArrivalRateFogNode(j); // will be used in calculating delay below
        int k = h[j];
        calcNormalizedArrivalRateCloudNode(k);
        if (x[a][j] == 1) {
            if (arrivalFog[j] > KP[j]) {
                proc_time = 2000d;
                System.out.println("too much load for fog");
            } else {
                proc_time = 1 / (KP[j] - arrivalFog[j]) * 1000d; // so that it is in ms
            }

//            System.out.println("DIF: " + (2 * dIF[j]) + " proc: " + (proc_time) + " trans: " + ((l_rp[a] + l_rq[a]) / rIF[j] * 1000));
            return (2 * dIF[j]) + (proc_time) + ((l_rp[a] + l_rq[a]) / rIF[j] * 1000d); // this is in ms

        } else {
            if (arrivalCloud[k] > KpP[k]) {
                proc_time = 1000d;
                System.out.println("too much load for cloud");
            } else {
                proc_time = 1 / (KpP[k] - arrivalCloud[k]) * 1000d; // so that it is in ms
            }
            return (2 * (dIF[j] + dFC[j][k])) + (proc_time) + (((l_rp[a] + l_rq[a]) / rIF[j] + (l_rp[a] + l_rq[a]) / rFC[j][k]) * 1000d); // this is in ms
        }
    }

    private void calcNormalizedArrivalRateFogNodes() {
        for (int j = 0; j < numFogNodes; j++) {
            calcNormalizedArrivalRateFogNode(j);
        }
    }

    private void calcNormalizedArrivalRateFogNode(int j) {
        double tempSum = 0;
        for (int a = 0; a < numServices; a++) {
            tempSum += L_P[a] * lambda_in[a][j] * x[a][j];
        }
        arrivalFog[j] = tempSum;
    }

    private void calcNormalizedArrivalRateCloudNodes() {
        for (int k = 0; k < numCloudServers; k++) {
            calcNormalizedArrivalRateCloudNode(k);
        }
    }

    private void calcNormalizedArrivalRateCloudNode(int k) {
        double tempSum = 0;
        for (int a = 0; a < numServices; a++) {
            calcArrivalRateCloudFromFogNodesForService(k, a);
            tempSum += L_P[a] * lambdap_in[a][k] * xp[a][k];
        }
        arrivalCloud[k] = tempSum;
    }

    /**
     * calculate lambda^out_aj and lambdap_in_ak for cloud server k for service
     * a
     *
     * @param k
     */
    private void calcArrivalRateCloudFromFogNodesForService(int k, int a) {
        double tempSum = 0;
        for (Integer j : h_reverse.get(k)) {
            lambda_out[a][j] = lambda_in[a][j] * (1 - x[a][j]); // calculate lambda^out_aj
            tempSum += lambda_out[a][j];
        }
        lambdap_in[a][k] = tempSum;
    }

    /**
     * Updates arrays x_aj and xp_ak according to the combination. (we divide
     * the combination number (its bit string) into 'numServices' chunk (e.g. if
     * numServices=5, we divide the bit string into 5 chunks). Each chunk
     * further is divided into 2 parts: form left to right, first part of the
     * bit string represents the j ('numFogNodes' bits) and the second part
     * represents the k ('numCloudServers' bits)).
     *
     * @param combination
     */
    public void updateDecisionVariablesAccordingToCombination(long combination) {
        long mask = 1;
        long temp;
        for (int a = 0; a < numServices; a++) {
            for (int k = 0; k < numCloudServers; k++) {
                temp = combination & mask;
                if (temp == 0) {
                    xp[a][k] = 0;
                } else {
                    xp[a][k] = 1;
                }
                mask = mask << 1;
            }

            for (int j = 0; j < numFogNodes; j++) {
                temp = combination & mask;
                if (temp == 0) {
                    x[a][j] = 0;
                } else {
                    x[a][j] = 1;
                }
                mask = mask << 1;
            }
        }
    }

    /**
     * Determines if fog node j has still storage and memory available
     *
     * @param j
     * @return
     */
    private boolean fogResourceConstraintsSatisfied(int j) {
        double utilziedFogStorage = 0, utilziedFogMemory = 0;
        for (int a = 0; a < numServices; a++) {
            if (x[a][j] == 1) {
                utilziedFogStorage += L_S[a];
                utilziedFogMemory += L_M[a];
            }
        }
        if (utilziedFogStorage > KS[j] || utilziedFogMemory > KM[j]) {
            return false;
        }
        return true;
    }

    private void deployCloudServiceIfNeeded(int a) {
        calcNormalizedArrivalRateCloudNodes();
        for (int k = 0; k < numCloudServers; k++) { // If incoming traffic rate to a cloud server for a particular service is 0, the service could be released to save space. On the other hand, even if there is small traffic incoming to a cloud server for a particular service, the service must not be removed from the cloud server
            if (lambdap_in[a][k] > 0) {
                xp[a][k] = 1;
            } else { // lambdap_in[a][k] == 0
                xp[a][k] = 0;
            }

        }

    }
}
