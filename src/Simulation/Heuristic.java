package Simulation;

import Run.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Ashkan Y.
 */
public class Heuristic {
    
    protected double[][] backup_lambda_in;

    private ServiceCounter fogStaticDeployedContainers;

    private boolean firstTimeDone = false;

    private int numFogNodes;
    private int numServices;
    private int numCloudServers;

    protected int[][] x; // x_aj
    protected int[][] x_backup;
    protected int[][] xp; // x'_ak
    protected int[][] v; // v_aj

    protected double d[][]; // stores d_aj
    protected double Vper[]; // V^%_a

    protected double lambda_in[][]; // lambda^in_aj
    protected double lambdap_in[][]; // lambda'^in_ak

    protected double lambda_out[][]; // lambda^out_aj

    protected double arrivalCloud[]; // LAMBDA^C
    protected double arrivalFog[]; // LAMBDA^F

    private int type;
    protected ServiceDeployScheme scheme;


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
            Traffic.backupIncomingTraffic(this);
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
            Traffic.calcNormalizedArrivalRateFogNodes(this);
            Traffic.calcNormalizedArrivalRateCloudNodes(this);
            if (Optimization.optimizationConstraintsSatisfied(x, xp, numServices, numFogNodes, numCloudServers, Parameters.L_S, 
                    Parameters.L_M, Parameters.KS, Parameters.KM, Parameters.KpS, Parameters.KpM, lambdap_in)) {
                cost = getCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
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
            if (traceType == Traffic.NOT_COMBINED) {
                Traffic.initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode(this);
            } else if (traceType == Traffic.COMBINED_APP_REGIONES) {
                Traffic.initializeAvgTrafficForStaticFogPlacementFirstTimeCombined(this); // now lambda values are based on average
            } else { // if (traceType == COMBINED_APP)
                Traffic.initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode(this);
            }
            
            for (int a = 0; a < numServices; a++) {
                if (justMinimizeViolation) {
                    MinViol(a);
                } else {
                    MinCost(a);
                }
            }
            fogStaticDeployedContainers = ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);
            Traffic.restoreIncomingTraffic(this);
            return fogStaticDeployedContainers;
        } else {
            // do not change the placement
            return fogStaticDeployedContainers;
        }
    }

    private ServiceCounter runFogDynamic(boolean justMinimizeViolation) {
        for (int a = 0; a < numServices; a++) {
            if (justMinimizeViolation) {
                MinViol(a);
            } else {
                MinCost(a);
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

    public void unsetFirstTimeBoolean() {
        firstTimeDone = false;
    }


    public double getCost(double timeDuration) {
        for (int a = 0; a < numServices; a++) {
            Violation.calcViolation(a, this); // updates traffic values, average service delay, and violation
        }
        return Cost.calcCost(timeDuration, x, xp, x_backup, Vper, Parameters.q, lambda_in, lambdap_in, lambda_out, Parameters.L_P, Parameters.L_S, Parameters.h);
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

    private void MinViol(int a) {
        Violation.calcViolation(a, this);
        List<FogTrafficIndex> fogTrafficIndex = Traffic.getFogIncomingTraffic(a, false, this);
        Collections.sort(fogTrafficIndex);
        int listIndex = -1;
        int j = 0;

        while (Vper[a] > 1 - Parameters.q[a] && listIndex < numFogNodes - 1) {
            listIndex++;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 0 && fogResourceConstraintsSatisfied(j)) { // if service a is not implemented on fog node j
                // to add CODE: DEPLOY
//               System.out.println("dep "+a+" "+j);
                x[a][j] = 1;
                Violation.calcViolation(a, this);
            }

        }
        boolean canRelease = true;
        listIndex = fogTrafficIndex.size();
        while (canRelease && listIndex > 0) {
            listIndex--;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 1) { // if service a is implemented on fog node j
                x[a][j] = 0;
                Violation.calcViolation(a, this);
                if (Vper[a] <= 1 - Parameters.q[a]) {
                    // to add CODE: RELEASE
                } else {
                    x[a][j] = 1;
                    Violation.calcViolation(a, this);
                    canRelease = false;
                }
            }

        }
        deployCloudServiceIfNeeded(a);

    }

    private void MinCost(int a) {
        Violation.calcViolation(a, this);
        List<FogTrafficIndex> fogTrafficIndex = Traffic.getFogIncomingTraffic(a, false, this);
        Collections.sort(fogTrafficIndex); // sorts fog nodes based on incoming traffic
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
                    Violation.calcViolation(a, this);
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
                    Violation.calcViolation(a, this);
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
        double costCfc, costExtraPC, costExtraSC, costViolPerFogNode;
        //if not deployiing (X[a][j] == 0) this is the cost we were paying, 
        // but now this is seen as savings
        costCfc = Cost.costCfc(Parameters.TAU, j, a, lambda_out, Parameters.h);
        costExtraPC = Cost.costExtraPC(Parameters.TAU, Parameters.h[j], a, Parameters.L_P, lambda_out[a][j]);
        costExtraSC = Cost.costExtraSC(Parameters.TAU, Parameters.h[j], a, Parameters.L_S, xp);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage);
        savings = costCfc + costExtraPC + costExtraSC + costViolPerFogNode;

        // Now if we were to deploy, this is the cost we would pay
        x[a][j] = 1;
        d[a][j] = Delay.calcServiceDelay(a, j, this); // this is just to update the things

        double costDep, costPF, costSF;
        costDep = Cost.costDep(j, a, Parameters.L_S);
        costPF = Cost.costPF(Parameters.TAU, j, a, Parameters.L_P, lambda_in);
        costSF = Cost.costSF(Parameters.TAU, j, a, Parameters.L_S);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage);
        loss = costDep + costPF + costSF + costViolPerFogNode;

        x[a][j] = 0; // revert this back to what it was
        d[a][j] = Delay.calcServiceDelay(a, j, this); // revert things back to what they were
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
        double costPF, costSF, costViolPerFogNode;
        costPF = Cost.costPF(Parameters.TAU, j, a, Parameters.L_P, lambda_in);
        costSF = Cost.costSF(Parameters.TAU, j, a, Parameters.L_S);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage);
        savings = costPF + costSF + costViolPerFogNode;
        
        // Now if we were to release, this is the loss we would pay
        x[a][j] = 0;
        d[a][j] = Delay.calcServiceDelay(a, j, this); // this is just to update the things

        double costCfc, costExtraPC, costExtraSC;
        costCfc = Cost.costCfc(Parameters.TAU, j, a, lambda_out, Parameters.h);
        costExtraPC = Cost.costExtraPC(Parameters.TAU, Parameters.h[j], a, Parameters.L_P, lambda_out[a][j]);
        costExtraSC = Cost.costExtraSC(Parameters.TAU, Parameters.h[j], a, Parameters.L_S, xp);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage);
        loss = costCfc + costExtraPC + costExtraSC + costViolPerFogNode;

        x[a][j] = 1; // revert this back to what it was
        d[a][j] = Delay.calcServiceDelay(a, j, this); // revert things back to what they were
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
        return Parameters.L_S[a] / Parameters.rFContr[j] * 1000 + Parameters.CONTAINER_INIT_DELAY;
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
                utilziedFogStorage += Parameters.L_S[a];
                utilziedFogMemory += Parameters.L_M[a];
            }
        }
        if (utilziedFogStorage > Parameters.KS[j] || utilziedFogMemory > Parameters.KM[j]) {
            return false;
        }
        return true;
    }

    private void deployCloudServiceIfNeeded(int a) {
        Traffic.calcNormalizedArrivalRateCloudNodes(this);
        for (int k = 0; k < numCloudServers; k++) { // If incoming traffic rate to a cloud server for a particular service is 0, the service could be released to save space. On the other hand, even if there is small traffic incoming to a cloud server for a particular service, the service must not be removed from the cloud server
            if (lambdap_in[a][k] > 0) {
                xp[a][k] = 1;
            } else { // lambdap_in[a][k] == 0
                xp[a][k] = 0;
            }

        }

    }
}
