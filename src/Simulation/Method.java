package Simulation;

import Scheme.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Ashkan Y.
 */
public class Method {

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

    protected Traffic traffic;
    protected Delay delay;

    private int type;
    protected ServiceDeployScheme scheme;

    private boolean onlyExperimental = false;

    public Method(ServiceDeployScheme scheme, int numFogNodes, int numServices, int numCloudServers) {

        traffic = new Traffic();
        delay = new Delay(this);

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

        if (numServices > 40) { // when there are large number of services, the purpose is only experimental, to show how is the performance of the system. (fog resource constraints are not checked)
            onlyExperimental = true;
        }

    }

    public ServiceCounter run(int traceType, boolean minimizeViolation) {
        backupAllPlacements();
        placementUpdated();
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

            return runFogStatic(traceType, minimizeViolation);
        } else { // FOG_DYNAMIC
            return runFogDynamic(minimizeViolation);
        }
    }

    private ServiceCounter runOptimal() {

        Optimization.init(numServices, numFogNodes, numCloudServers);
        long numCombinations = (long) Math.pow(2, numServices * (numFogNodes + numCloudServers)); // x_aj and xp_ak
        double minimumCost = Double.MAX_VALUE, cost;
        for (long combination = 0; combination < numCombinations; combination++) {
            updateDecisionVariablesAccordingToCombination(combination); // updates x, xp
            placementUpdated();
//            Traffic.calcNormalizedArrivalRates(this);
            if (Optimization.optimizationConstraintsSatisfied(x, xp, numServices, numFogNodes, numCloudServers, Parameters.L_S,
                    Parameters.L_M, Parameters.KS, Parameters.KM, Parameters.KpS, Parameters.KpM, traffic.lambdap_in)) {
                cost = getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                if (cost < minimumCost) {
                    minimumCost = cost;
                    Optimization.updateBestDecisionVaraibles(x, xp, numServices, numFogNodes, numCloudServers);
                }
            }
        }
        System.out.println("j");
        Optimization.updateDecisionVaraiblesAccordingToBest(x, xp, numServices, numFogNodes, numCloudServers);
        placementUpdated();
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
//            Traffic.calcNormalizedArrivalRates(this);
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
//            Traffic.calcNormalizedArrivalRates(this);
            return fogStaticDeployedContainers;
        }
    }

    private ServiceCounter runFogDynamic(boolean minimizeViolation) {
        for (int a = 0; a < numServices; a++) {
            if (minimizeViolation) {
                if (Parameters.MEASURING_RUNNING_TIME == true) {
                    MinViolForRealImplementation(a);
                } else {
                    MinViol(a);
                }

            } else {
                MinCost(a);
            }
        }
        return ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);

    }

    public double getAvgServiceDelay() {
        double sumNum = 0;
        double sumDenum = 0;
        double d;
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                d = delay.calcServiceDelay(a, j) * traffic.lambda_in[a][j];
                sumNum += d;
                sumDenum += traffic.lambda_in[a][j];
            }
        }
        return sumNum / sumDenum;
    }

    public void unsetFirstTimeBoolean() {
        firstTimeDone = false;
    }

    /**
     * gets average cost
     *
     * @param timeDuration
     * @return
     */
    public double getAvgCost(double timeDuration) {
        updateDelayAndViolation();
        return Cost.calcAverageCost(timeDuration, x, xp, x_backup, Vper, Parameters.q, traffic.lambda_in, traffic.lambdap_in, traffic.lambda_out, Parameters.L_P, Parameters.L_S, Parameters.h);
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
            if (x[a][j] == 0 && fogHasFreeResources(j)) { // if service a is not implemented on fog node j
                // to add CODE: DEPLOY
                x[a][j] = 1;
                placementUpdated();
                Violation.calcViolation(a, this);
            }
        }
        boolean canRelease = true;
        listIndex = fogTrafficIndex.size();
        while (canRelease && listIndex > 0) {
            listIndex--;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 1) { // if service a is implemented on fog node j
                releaseFogServiceSafely(a, j);
                placementUpdated();
                Violation.calcViolation(a, this);
                if (Vper[a] <= 1 - Parameters.q[a]) {
                    // to add CODE: RELEASE
                } else {
                    x[a][j] = 1;
                    placementUpdated();
                    Violation.calcViolation(a, this);
                    canRelease = false;
                }
            }

        }
        deployOrReleaseCloudService(a);

    }

    /**
     * Runs the MinCost
     *
     * @param a service a for which minCost is
     */
    private void MinCost(int a) {
        Violation.calcViolation(a, this);
        List<FogTrafficIndex> fogTrafficIndex = Traffic.getFogIncomingTraffic(a, false, this);
        Collections.sort(fogTrafficIndex); // sorts fog nodes based on incoming traffic
        int listIndex = -1;
        int j;

        while (listIndex < numFogNodes - 1) {
            listIndex++;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 0 && fogHasFreeResources(j)) { // if service a is not implemented on fog node j
                if (deployMakesSense(a, j)) {
                    // to add CODE: DEPLOY
                    x[a][j] = 1;
                    placementUpdated();
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
                    releaseFogServiceSafely(a, j);
                    placementUpdated();
                    Violation.calcViolation(a, this);
                }
            }
        }
        deployOrReleaseCloudService(a);
    }

    /**
     * Requirement: x[a][j] = 0;
     *
     * @param a
     * @param j
     * @return
     */
    private boolean deployMakesSense(int a, int j) {
        double futureCost = 0;
        double futureSavings = 0;
        double costCfc, costExtraPC, costExtraSC, costViolPerFogNode;
        //if not deploying (X[a][j] == 0) this is the cost we were paying, 
        // but now this is seen as savings
        costCfc = Cost.costCfc(Parameters.TAU, j, a, traffic.lambda_out, Parameters.h);
        costExtraPC = Cost.costExtraPC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_P, traffic.lambda_out[a][j]);
        costExtraSC = Cost.costExtraSC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_S, xp);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, j, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage, traffic.lambda_in);
        futureSavings = costCfc + costExtraPC + costExtraSC + costViolPerFogNode;

//        System.out.println("cfc" + costCfc + " pc" + costExtraPC + " sc"+ costExtraSC + " viol"+costViolPerFogNode);
        // Now if we were to deploy, this is the cost we would pay
        x[a][j] = 1;
        placementUpdated();
        d[a][j] = delay.calcServiceDelay(a, j);  // this is just to update the service delay

        double costDep, costPF, costSF;
        costDep = Cost.costDep(j, a, Parameters.L_S);
        costPF = Cost.costPF(Parameters.TAU, j, a, Parameters.L_P, traffic.lambda_in);
        costSF = Cost.costSF(Parameters.TAU, j, a, Parameters.L_S);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, j, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage, traffic.lambda_in);
        futureCost = costDep + costPF + costSF + costViolPerFogNode;

//        System.out.println("dep" + costDep + " pf" + costPF + " sf"+ costSF + " viol"+costViolPerFogNode);
        releaseFogServiceSafely(a, j); // revert this back to what it was
        placementUpdated();
        d[a][j] = delay.calcServiceDelay(a, j); // revert things back to what they were
        if (futureSavings > futureCost) {
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
        double futureCost = 0;
        double futureSavings = 0;
        //if not releasing (X[a][j] == 1) this is the cost we were paying, 
        // but now this is seen as savings
        double costPF, costSF, costViolPerFogNode;
        costPF = Cost.costPF(Parameters.TAU, j, a, Parameters.L_P, traffic.lambda_in);
        costSF = Cost.costSF(Parameters.TAU, j, a, Parameters.L_S);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, j, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage, traffic.lambda_in);
        futureSavings = costPF + costSF + costViolPerFogNode;

//         System.out.println( " pf" + costPF + " sf"+ costSF + " viol"+costViolPerFogNode);
        // Now if we were to release, this is the loss we would pay
        int k = Parameters.h[a][j];
        releaseFogServiceSafely(a, j);
        placementUpdated();
        d[a][j] = delay.calcServiceDelay(a, j); // this is just to update the things

        double costCfc, costExtraPC, costExtraSC;
        costCfc = Cost.costCfc(Parameters.TAU, j, a, traffic.lambda_out, Parameters.h);
        costExtraPC = Cost.costExtraPC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_P, traffic.lambda_out[a][j]);
        costExtraSC = Cost.costExtraSC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_S, xp);
        costViolPerFogNode = Cost.costViolPerFogNode(Parameters.TAU, a, j, Violation.calcVper(a, j, fogTrafficPercentage, this), Parameters.q, fogTrafficPercentage, traffic.lambda_in);
        futureCost = costCfc + costExtraPC + costExtraSC + costViolPerFogNode;

//        System.out.println("cfc" + costCfc + " pc" + costExtraPC + " sc"+ costExtraSC + " viol"+costViolPerFogNode);
        x[a][j] = 1; // revert this back to what it was
        placementUpdated();
        d[a][j] = delay.calcServiceDelay(a, j); // revert things back to what they were
        if (futureSavings > futureCost) {
            return true;
        } else {
            return false;
        }
    }

    private void MinViolForRealImplementation(int a) {
        List<FogTrafficIndex> fogTrafficIndex = Traffic.getFogIncomingTraffic(a, false, this);
        Collections.sort(fogTrafficIndex);
        int listIndex = -1;
        int j = 0;
        Violation.calcViolation(a, this);
        while (Vper[a] > 1 - Parameters.q[a] && listIndex < numFogNodes - 1) {
            listIndex++;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 0 && fogHasFreeResources(j)) { // if service a is not implemented on fog node j
                // to add CODE: DEPLOY
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
                releaseFogServiceSafely(a, j);
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
        deployOrReleaseCloudService(a);

    }

    private void MinCostForRealImplementation(int a) {
        List<FogTrafficIndex> fogTrafficIndex = Traffic.getFogIncomingTraffic(a, false, this);
        Collections.sort(fogTrafficIndex); // sorts fog nodes based on incoming traffic
        int listIndex = -1;
        int j;
        Violation.calcViolation(a, this);
        while (listIndex < numFogNodes - 1) {
            listIndex++;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 0 && fogHasFreeResources(j)) { // if service a is not implemented on fog node j
                if (deployMakesSense(a, j)) {
                    // to add CODE: DEPLOY
                    x[a][j] = 1;
                    placementUpdated();
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
                    releaseFogServiceSafely(a, j);
                    placementUpdated();
                    Violation.calcViolation(a, this);
                }
            }
        }
        deployOrReleaseCloudService(a);
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
     * service to the total traffic to all fog nodes for that service
     *
     * @param a
     * @param j
     * @return
     */
    private double calcFogTrafficPercentage(int a, int j) {
        double denum = 0;
        for (int fog = 0; fog < numFogNodes; fog++) {
            denum += traffic.lambda_in[a][fog];
        }
        return traffic.lambda_in[a][j] / denum;
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
    private boolean fogHasFreeResources(int j) {
        if (onlyExperimental) {
            // the reason is, since there is traffic for every (service, fog node) combinatio, without this boolean, fog resource capacity will limit large service deployment, and as a result, the violation will be high high. 
            return true;
        }
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

    private void deployOrReleaseCloudService(int a) {

        for (int k = 0; k < numCloudServers; k++) { // If incoming traffic rate to a cloud server for a particular service is 0, the service could be released to save space. On the other hand, even if there is small traffic incoming to a cloud server for a particular service, the service must not be removed from the cloud server
            if (traffic.lambdap_in[a][k] > 0) {
                xp[a][k] = 1;
            } else { // lambdap_in[a][k] == 0
                xp[a][k] = 0;
            }
        }
    }

    private void releaseFogServiceSafely(int a, int j) {
        x[a][j] = 0;
        xp[a][Parameters.h[a][j]] = 1; // if there is no backup for this service in the cloud, make a backup available
    }

    /**
     * This function should be called every time the placement variables change
     */
    private void placementUpdated() {
        Traffic.calcNormalizedArrivalRates(this);
        delay.initialize();
    }

    /**
     * Updates average service delay, and violation
     */
    private void updateDelayAndViolation() {
        for (int a = 0; a < numServices; a++) {
            Violation.calcViolation(a, this);
        }
    }
}
