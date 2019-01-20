package Components;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains the functions for the (INLP) optimization problem
 */
public class Optimization {

    public static int[][] bestX;
    public static int[][] bestXp;

    /**
     * Initializes the best service allocation matrices
     *
     * @param numServices number of services
     * @param numFogNodes number of fog nodes
     * @param numCloudServers number of cloud servers
     */
    public static void init(int numServices, int numFogNodes, int numCloudServers) {
        bestX = new int[numServices][numFogNodes];
        bestXp = new int[numServices][numCloudServers];
    }

    /**
     * Updates the variables for best service allocation matrices
     *
     * @param x the fog service allocation matrix
     * @param xp the cloud service allocation matrix
     * @param numServices
     * @param numFogNodes
     * @param numCloudServers
     */
    public static void updateBestDecisionVaraibles(int[][] x, int[][] xp, int numServices, int numFogNodes, int numCloudServers) {
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                bestX[a][j] = x[a][j];
            }
            for (int k = 0; k < numCloudServers; k++) {
                bestXp[a][k] = xp[a][k];
            }
        }
    }

    /**
     * Updates the service allocation matrices (x and xp) according to the best
     * parameters
     *
     * @param x the fog service allocation matrix
     * @param xp the cloud service allocation matrix
     * @param numServices
     * @param numFogNodes
     * @param numCloudServers
     */
    public static void updateDecisionVaraiblesAccordingToBest(int[][] x, int[][] xp, int numServices, int numFogNodes, int numCloudServers) {
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                x[a][j] = bestX[a][j];
            }
            for (int k = 0; k < numCloudServers; k++) {
                xp[a][k] = bestXp[a][k];
            }
        }
    }

    /**
     * Checks if the constraints of the optimization problem are satisfied
     *
     * @param x the fog service allocation matrix
     * @param xp the cloud service allocation matrix
     * @param numServices
     * @param numFogNodes
     * @param numCloudServers
     * @param L_S storage size of service a, in bytes
     * @param L_M required amount of memory for service a (in bytes)
     * @param KS storage capacity of fog node j, in bytes
     * @param KM memory capacity of fog node j, in bytes
     * @param KpS storage capacity of cloud server k, in bytes
     * @param KpM memory capacity of cloud server k, in bytes
     * @param lambdap_in incoming traffic rate to cloud server `k' for service
     * `a` (request/second)
     */
    public static boolean optimizationConstraintsSatisfied(int[][] x, int[][] xp, int numServices, int numFogNodes, int numCloudServers, double[] L_S, double[] L_M, double KS[], double KM[], double KpS[], double KpM[], double lambdap_in[][]) {
        if (!fogResourceConstraintsSatisfied(x, numServices, numFogNodes, L_S, L_M, KS, KM)) {
            return false;
        }
        if (!cloudResourceConstraintsSatisfied(xp, numServices, numCloudServers, L_S, L_M, KpS, KpM)) {
            return false;
        }
        // equations 16, 17, and 18 are already implemented in calcServiceDelay().
        if (!cloudArrivalRateConstraintsSatisfied(xp, numServices, numCloudServers, lambdap_in)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if Equation 15 is satisfied
     *
     * @param x the fog service allocation matrix
     * @param numServices
     * @param numFogNodes
     * @param L_S storage size of service a, in bytes
     * @param L_M required amount of memory for service a (in bytes)
     * @param KS storage capacity of fog node j, in bytes
     * @param KM memory capacity of fog node j, in bytes
     */
    private static boolean fogResourceConstraintsSatisfied(int[][] x, int numServices, int numFogNodes, double[] L_S, double[] L_M, double KS[], double KM[]) {
        double utilziedFogStorage, utilziedFogMemory;
        for (int j = 0; j < numFogNodes; j++) {
            utilziedFogStorage = 0;
            utilziedFogMemory = 0;
            for (int a = 0; a < numServices; a++) {
                if (x[a][j] == 1) {
                    utilziedFogStorage += L_S[a];
                    utilziedFogMemory += L_M[a];
                }
            }
            if (utilziedFogStorage > KS[j] || utilziedFogMemory > KM[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if Equation 16 is satisfied
     *
     * @param xp the cloud service allocation matrix
     * @param numServices
     * @param numCloudServers
     * @param L_S storage size of service a, in bytes
     * @param L_M required amount of memory for service a (in bytes)
     * @param KpS storage capacity of cloud server k, in bytes
     * @param KpM memory capacity of cloud server k, in bytes
     */
    private static boolean cloudResourceConstraintsSatisfied(int[][] xp, int numServices, int numCloudServers, double[] L_S, double[] L_M, double KpS[], double KpM[]) {
        double utilziedCloudStorage, utilziedCloudMemory;
        for (int k = 0; k < numCloudServers; k++) {
            utilziedCloudStorage = 0;
            utilziedCloudMemory = 0;
            for (int a = 0; a < numServices; a++) {
                if (xp[a][k] == 1) {
                    utilziedCloudStorage += L_S[a];
                    utilziedCloudMemory += L_M[a];
                }
            }
            if (utilziedCloudStorage > KpS[k] || utilziedCloudMemory > KpM[k]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if Equation 20 is satisfied
     * 
     * @param xp the cloud service allocation matrix
     * @param numServices
     * @param numCloudServers
     * @param lambdap_in incoming traffic rate to cloud server `k' for service
     * `a` (request/second)
     * @return 
     */
    private static boolean cloudArrivalRateConstraintsSatisfied(int[][] xp, int numServices, int numCloudServers, double lambdap_in[][]) {
        for (int a = 0; a < numServices; a++) {
            for (int k = 0; k < numCloudServers; k++) { // If incoming traffic rate to a cloud server for a particular service is 0, the service could be released to save space. On the other hand, even if there is small traffic incoming to a cloud server for a particular service, the service must not be removed from the cloud server
                if (xp[a][k] == 0 && lambdap_in[a][k] > 0) {
                    return false;
                }
                if (xp[a][k] == 1 && lambdap_in[a][k] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
