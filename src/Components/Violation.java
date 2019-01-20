package Components;

import Scheme.Parameters;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains the functions and variables related to calculating SLA
 * violation
 */
public class Violation {

    /**
     * Calculate delay Violation Percentage. (Percentage of IoT requests that do
     * not meet the delay requirement for service a (V^%_a))ˇ
     *
     * @param a the index of the service
     * @param method
     */
    public static void calcViolation(int a, Method method) throws UnsupportedOperationException {

        double sumNum = 0;
        double sumDenum = 0;
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            method.d[a][j] = method.delay.calcServiceDelay(a, j);
            if (method.d[a][j] > Parameters.th[a]) {
                method.v[a][j] = 1;
            } else {
                method.v[a][j] = 0;
            }
            sumNum += method.v[a][j] * method.traffic.lambda_in[a][j];
            sumDenum += method.traffic.lambda_in[a][j];
        }
        if (sumDenum == 0) {
            method.Vper[a] = 0;
        } else {
            method.Vper[a] = sumNum / sumDenum;
        }
    }

    /**
     * Gets the percentage of the delay violations for a particular service
     *
     * @param a the index of the service
     * @param method
     */
    private static double getViolationPercentage(int a, Method method) {
        Violation.calcViolation(a, method);
        return method.Vper[a] * 100;
    }

    /**
     * Gets the average percentage of delay violations of all services
     *
     * @param method
     */
    public static double getViolationPercentage(Method method) {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += getViolationPercentage(a, method);
        }
        return (sum / Parameters.numServices);
    }

    /**
     * Calculates violation slack for all services
     */
    public static double getViolationSlack() {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += calcViolationSlack(a);
        }
        return (sum / Parameters.numServices);
    }

    /**
     * Calculates the slack that a given service has for violating delay
     *
     * @param a the index of the service
     */
    private static double calcViolationSlack(int a) {
        return (1 - Parameters.q[a]) * 100;
    }

    /**
     * Calculates the delay violation percentage for a given service on a given
     * fog node
     *
     * @param a the index of the service
     * @param j the index of the fog node
     * @param fogTrafficPercentage the percentage of the traffic for service a
     * that is incoming to fog node j
     * @param method the method used for service provisioning
     */
    public static double calcVperPerNode(int a, int j, double fogTrafficPercentage, Method method) {
        if (method.d[a][j] > Parameters.th[a]) {
            method.v[a][j] = 1;
            return fogTrafficPercentage;
        } else {
            method.v[a][j] = 0;
            return 0;
        }
    }
}
