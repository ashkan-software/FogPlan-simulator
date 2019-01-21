package Components;

import Scheme.Parameters;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Utilities.ArrayFiller;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Ashkan Y.
 *
 * This is the main class of the simulation, that contains different methods
 * that are proposed in the paper (e.g. optimization, greedy algorithms, all
 * cloud)
 */
public class Method {

    protected double[][] backup_lambda_in;

    private ServiceCounter fogStaticDeployedContainers; // the number of containers deployed when using Static Fog

    private boolean firstTimeRunDone = false; // a boolean that is used in Static Fog to keep track of the first time that the algorithm should run

    private int numFogNodes;
    private int numServices;
    private int numCloudServers;

    protected int[][] x; // x_aj
    protected int[][] x_backup; // backup of x
    protected int[][] xp; // x'_ak
    protected int[][] v; // v_aj

    protected double d[][]; // d_aj
    protected double Vper[]; // V^%_a

    protected Traffic traffic; // instance of traffic class
    protected Delay delay; // instance of delay class

    private int type; // type of the method (e.g. All Cloud vs. Min-Cost)
    protected ServiceDeployScheme scheme;

    private boolean onlyExperimental = false;

    /**
     * Constructor of this class.
     *
     * @param scheme
     * @param numFogNodes
     * @param numServices
     * @param numCloudServers
     */
    public Method(ServiceDeployScheme scheme, int numFogNodes, int numServices, int numCloudServers) {

        traffic = new Traffic();
        delay = new Delay(this);

        this.scheme = scheme;
        type = scheme.type;
        x = ArrayFiller.convertIntegerToInt2DArray(scheme.variable.x);
        x_backup = scheme.variable.x_backup;
        xp = ArrayFiller.convertIntegerToInt2DArray(scheme.variable.xp);
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

    /**
     * Runs the corresponding method
     *
     * @param traceType the type of the trace that is used (refer to Traffic
     * class)
     * @param isMinViol boolean showing if Min-Viol is running
     * @return returns the number of deployed fog and cloud services
     */
    public ServiceCounter run(int traceType, boolean isMinViol) {
        backupAllPlacements();
        Traffic.calcArrivalRatesOfInstructions(this); // normalizes arrival rates
        delay.initialize();
        if (type == ServiceDeployScheme.ALL_CLOUD) {
            // do not change the placement
            return new ServiceCounter(0, numCloudServers * numServices);
        } else if (type == ServiceDeployScheme.ALL_FOG) { // all fog is not used in the paper (only experimental)
            // do not change the placement
            return new ServiceCounter(numFogNodes * numServices, 0);
        } else if (type == ServiceDeployScheme.OPTIMAL) {
            return runOptimal();
        } else if (type == ServiceDeployScheme.FOG_STATIC) { // FOG_STATIC
            Traffic.backupIncomingTraffic(this);
            return runFogStatic(traceType, isMinViol);
        } else { // FOG_DYNAMIC
            return runFogDynamic(isMinViol);
        }
    }

    /**
     * Runs the optimal placement method, which will update x_aj and xp_ak
     *
     * @return returns the number of deployed fog and cloud services
     */
    private ServiceCounter runOptimal() {

        Optimization.init(numServices, numFogNodes, numCloudServers);
        long numCombinations = (long) Math.pow(2, numServices * (numFogNodes + numCloudServers)); // x_aj and xp_ak
        double minimumCost = Double.MAX_VALUE, cost;
        for (long combination = 0; combination < numCombinations; combination++) { // tries all different compinations of x_aj and xp_ak
            updateDecisionVariablesAccordingToCombination(combination); // updates x, xp
            Traffic.calcArrivalRatesOfInstructions(this); // updates traffic rates
            if (Optimization.optimizationConstraintsSatisfied(x, xp, numServices, numFogNodes, numCloudServers, Parameters.L_S,
                    Parameters.L_M, Parameters.KS, Parameters.KM, Parameters.KpS, Parameters.KpM, traffic.lambdap_in)) {
                cost = getAvgCost(Parameters.TRAFFIC_CHANGE_INTERVAL);
                if (cost < minimumCost) { // updates the minimum cost
                    minimumCost = cost;
                    Optimization.updateBestDecisionVaraibles(x, xp, numServices, numFogNodes, numCloudServers);
                }
            }
        }
        Optimization.updateDecisionVaraiblesAccordingToBest(x, xp, numServices, numFogNodes, numCloudServers); // retrieve the best placement
        Traffic.calcArrivalRatesOfInstructions(this); // updates the traffic rates
        delay.initialize();
        return ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);
    }

    /**
     * Runs the Fog Static placement method, which will update x_aj and xp_ak
     *
     * @param traceType the type of the trace that is used (refer to Traffic
     * class)
     * @param isMinViol boolean showing if Min-Viol is running
     * @return returns the number of deployed fog and cloud services
     */
    private ServiceCounter runFogStatic(int traceType, boolean isMinViol) {
        if (!firstTimeRunDone) { // if it is the first time
            firstTimeRunDone = true; // it does not run the algorithm after the first time
            if (traceType == Traffic.NOT_COMBINED) {
                Traffic.initializeAvgTrafficForStaticFogPlacementFirstTimePerServicePerFogNode(this);
            } else if (traceType == Traffic.AGGREGATED) {
                Traffic.initializeAvgTrafficForStaticFogPlacementFirstTimeCombined(this); // now lambda values are based on average
            } else { // if (traceType == COMBINED_APP)
                Traffic.initializeAvgTrafficForStaticFogPlacementFirstTimePerFogNode(this);
            }
            for (int a = 0; a < numServices; a++) {
                if (isMinViol) {
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

    /**
     * Runs the Fog Dynamic placement method (either Min-Cost or Min-Viol),
     * which will update x_aj and xp_ak
     *
     * @param isMinViol boolean showing if Min-Viol is running
     * @return returns the number of deployed fog and cloud services
     */
    private ServiceCounter runFogDynamic(boolean isMinViol) {
        for (int a = 0; a < numServices; a++) {
            if (isMinViol) {
                MinViol(a);
            } else {
                MinCost(a);
            }
        }
        return ServiceCounter.countServices(numServices, numFogNodes, numCloudServers, x, xp);

    }

    /**
     * Gets the average service delay
     *
     * @return
     */
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

    /**
     * Sets the firstTimeRunDone boolean variable false
     */
    public void unsetFirstTimeBoolean() {
        firstTimeRunDone = false;
    }

    /**
     * gets the average cost for a specific time duration
     *
     * @param timeDuration the duration of the time
     */
    public double getAvgCost(double timeDuration) {
        delay.initialize();
        updateDelayAndViolation();
        return Cost.calcAverageCost(timeDuration, x, xp, x_backup, Vper, Parameters.q, traffic.lambda_in, traffic.lambdap_in, traffic.lambda_out, Parameters.L_P, Parameters.L_S, Parameters.h);
    }

    /**
     * Prints the current service allocation among fog nodes and cloud servers
     */
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

    /**
     * Runs the Min-Viol greedy algorithm for a service
     *
     * @param a the index of the service
     */
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
                // CODE: physically DEPLOY 
                x[a][j] = 1;
                placementUpdatedForService(a);
                Violation.calcViolation(a, this);
            }
        }
        boolean canRelease = true;
        listIndex = fogTrafficIndex.size();
        while (canRelease && listIndex > 0) {
            listIndex--;
            j = fogTrafficIndex.get(listIndex).getFogIndex();
            if (x[a][j] == 1) { // if service a is implemented on fog node j
                releaseServiceSafelyFromFogNodes(a, j);
                placementUpdatedForService(a);
                Violation.calcViolation(a, this);
                if (Vper[a] <= 1 - Parameters.q[a]) {
                    // CODE: physically RELEASE
                } else {
                    x[a][j] = 1;
                    placementUpdatedForService(a);
                    Violation.calcViolation(a, this);
                    canRelease = false;
                }
            }

        }
        deployOrReleaseCloudService(a);

    }

    /**
     * Runs the Min-Cost greedy algorithm for a service
     *
     * @param a the index of the service
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
                    placementUpdatedForService(a);
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
                    releaseServiceSafelyFromFogNodes(a, j);
                    placementUpdatedForService(a);
                    Violation.calcViolation(a, this);
                }
            }
        }
        deployOrReleaseCloudService(a);
    }

    /**
     * (For Min-Cost) checks if deploying a service makes sense according to
     * cost. Requirement: x[a][j] = 0;
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private boolean deployMakesSense(int a, int j) {
        double futureCost = 0;
        double futureSavings = 0;
        double costCfc, costExtraPC, costExtraSC, costViolPerFogNode;
        // if not deploying (X[a][j] == 0) this is the cost we were paying, 
        // but now this is seen as savings
        costCfc = Cost.costCfc(Parameters.TAU, j, a, traffic.lambda_out, Parameters.h);
        costExtraPC = Cost.costExtraPC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_P, traffic.lambda_out[a][j]);
        costExtraSC = Cost.costExtraSC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_S, xp);
        double fogTrafficPercentage = calcFogTrafficPercentage(a, j);
        costViolPerFogNode = Cost.costViol(Parameters.TAU, a, j, Violation.calcVperPerNode(a, j, fogTrafficPercentage, this), Parameters.q, traffic.lambda_in);
        futureSavings = costCfc + costExtraPC + costExtraSC + costViolPerFogNode;

        // Now if we were to deploy, this is the cost we would pay
        x[a][j] = 1;
        d[a][j] = delay.calcServiceDelay(a, j);  // this is just to update the service delay

        double costDep, costPF, costSF;
        costDep = Cost.costDep(j, a, Parameters.L_S);
        costPF = Cost.costPF(Parameters.TAU, j, a, Parameters.L_P, traffic.lambda_in);
        costSF = Cost.costSF(Parameters.TAU, j, a, Parameters.L_S);
        costViolPerFogNode = Cost.costViol(Parameters.TAU, a, j, Violation.calcVperPerNode(a, j, fogTrafficPercentage, this), Parameters.q, traffic.lambda_in);
        futureCost = costDep + costPF + costSF + costViolPerFogNode;

        releaseServiceSafelyFromFogNodes(a, j); // revert this back to what it was
        d[a][j] = delay.calcServiceDelay(a, j); // revert things back to what they were
        if (futureSavings > futureCost) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * (For Min-Cost) checks if releasing a service makes sense according to
     * cost. Requirement: x[a][j] = 1;
     *
     * @param a the index of the service
     * @param j the index of the fog node
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
        costViolPerFogNode = Cost.costViol(Parameters.TAU, a, j, Violation.calcVperPerNode(a, j, fogTrafficPercentage, this), Parameters.q, traffic.lambda_in);
        futureSavings = costPF + costSF + costViolPerFogNode;

        // Now if we were to release, this is the loss we would pay
        int k = Parameters.h[a][j];
        releaseServiceSafelyFromFogNodes(a, j);
        d[a][j] = delay.calcServiceDelay(a, j); // this is just to update the things

        double costCfc, costExtraPC, costExtraSC;
        costCfc = Cost.costCfc(Parameters.TAU, j, a, traffic.lambda_out, Parameters.h);
        costExtraPC = Cost.costExtraPC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_P, traffic.lambda_out[a][j]);
        costExtraSC = Cost.costExtraSC(Parameters.TAU, Parameters.h[a][j], a, Parameters.L_S, xp);
        costViolPerFogNode = Cost.costViol(Parameters.TAU, a, j, Violation.calcVperPerNode(a, j, fogTrafficPercentage, this), Parameters.q, traffic.lambda_in);
        futureCost = costCfc + costExtraPC + costExtraSC + costViolPerFogNode;

        x[a][j] = 1; // revert this back to what it was
        d[a][j] = delay.calcServiceDelay(a, j); // revert things back to what they were
        if (futureSavings > futureCost) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculates percentage of traffic in a given fog node for a particular
     * service to the total traffic to all fog nodes for that service
     *
     * @param a the index of the service
     * @param j the index of the fog node
     */
    private double calcFogTrafficPercentage(int a, int j) {
        double denum = 0;
        for (int fog = 0; fog < numFogNodes; fog++) {
            denum += traffic.lambda_in[a][fog];
        }
        return traffic.lambda_in[a][j] / denum;
    }

    /**
     * Backs up fog placement when it is called for a given service
     *
     * @param a the index of the service
     */
    private void backupFogPlacement(int a) {
        for (int j = 0; j < numFogNodes; j++) {
            x_backup[a][j] = x[a][j];
        }
    }

    /**
     * Backs up fog placement when it is called for all services
     *
     */
    private void backupAllPlacements() {
        for (int a = 0; a < numServices; a++) {
            backupFogPlacement(a);
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
     * Determines if a fog node still has storage and memory available
     *
     * @param j the index of the fog ndoe
     */
    private boolean fogHasFreeResources(int j) {
        if (onlyExperimental) {
            // since there is traffic for every (service, fog node) combination, without this boolean, fog resource capacity will limit large service deployment, and as a result, the violation will be high high. 
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

    /**
     * This function will deploy (or release) service a on cloud services, if
     * needed, according to the traffic equations (eq. 20)
     *
     * @param a the index of the service
     */
    private void deployOrReleaseCloudService(int a) {
        if (Parameters.MEASURING_RUNNING_TIME == true) {
            Traffic.calcArrivalRatesOfInstructions(this, a);
        }
        for (int k = 0; k < numCloudServers; k++) { // If incoming traffic rate to a cloud server for a particular service is 0, the service could be released to save space. On the other hand, even if there is small traffic incoming to a cloud server for a particular service, the service must not be removed from the cloud server
            if (traffic.lambdap_in[a][k] > 0) {
                xp[a][k] = 1;
            } else { // lambdap_in[a][k] == 0
                xp[a][k] = 0;
            }
        }
    }

    /**
     * This function will safely release a service from a fog node. (The word
     * safely refers to the case when a service is not implemented on the fog
     * node, and must be implemented on the corresponding cloud server, so that
     * the requests sent to the fog node, can be safely forwarded to that cloud
     * server)
     *
     * @param a
     * @param j
     */
    private void releaseServiceSafelyFromFogNodes(int a, int j) {
        x[a][j] = 0;
        xp[a][Parameters.h[a][j]] = 1; // if there is no backup for this service in the cloud, make a backup available
    }

    /**
     * This function should be called every time the placement variables for a
     * service change
     *
     * @param a the index of the service
     */
    private void placementUpdatedForService(int a) {
        if (Parameters.MEASURING_RUNNING_TIME == false) {
            Traffic.calcArrivalRatesOfInstructions(this, a);
            delay.initialize(a);
        }
    }

    /**
     * Updates average service delay, and violation as well
     */
    private void updateDelayAndViolation() {
        for (int a = 0; a < numServices; a++) {
            Violation.calcViolation(a, this);
        }
    }
}
