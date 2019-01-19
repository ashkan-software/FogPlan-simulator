package Simulation;

import Scheme.Parameters;

/**
 *
 * @author ashkany This class has the functions and variables related to
 * calculating SLA violation
 */
public class Violation {

    /**
     * Calculate SLA Violation Percentage. (Percentage of IoT requests that do
     * not meet the delay requirement for service a (V^%_a))ˇ
     *
     * @param a
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

    private static double getViolationPercentage(int a, Method method) {
        Violation.calcViolation(a, method);
        return method.Vper[a] * 100;
    }

    
    public static double getViolationPercentage(Method method) {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += getViolationPercentage(a, method);
        }
        return (sum / Parameters.numServices);
    }

    public static double getViolationSlack() {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += getViolationSlack(a);
        }
        return (sum / Parameters.numServices);
    }

    private static double getViolationSlack(int a) {
        return (1 - Parameters.q[a]) * 100;
    }

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
