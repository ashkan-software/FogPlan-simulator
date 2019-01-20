package Components;

/**
 *
 * @author Ashkan Y.
 */
public class Optimization {
    
    public static int[][] bestX;
    public static int[][] bestXp;
    
    public static void init(int numServices, int numFogNodes, int numCloudServers){
        bestX = new int[numServices][numFogNodes];
        bestXp = new int[numServices][numCloudServers];
    }

    public static void updateBestDecisionVaraibles(int[][] x, int[][] xp, int numServices, int numFogNodes, int numCloudServers){
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
     * updates x and xp
     * @param x
     * @param xp
     * @param numServices
     * @param numFogNodes
     * @param numCloudServers 
     */
    public static void updateDecisionVaraiblesAccordingToBest(int[][] x, int[][] xp, int numServices, int numFogNodes, int numCloudServers){
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                x[a][j] = bestX[a][j];
            }
            for (int k = 0; k < numCloudServers; k++) {
                xp[a][k] = bestXp[a][k];
            }
        }
    }
    
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
     * Equation 14
     *
     * @return
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
     * Equation 15
     *
     * @return
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
     * Equation 19
     *
     * @param xp
     * @param numServices
     * @param numCloudServers
     * @param lambdap_in
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
