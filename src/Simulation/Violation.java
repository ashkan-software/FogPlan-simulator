package Simulation;

import Run.Parameters;

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
     * @param heuristic
     */
    public static void calcViolation(int a, Heuristic heuristic) {

        double sumNum = 0;
        double sumDenum = 0;
        for (int j = 0; j < Parameters.numFogNodes; j++) {
            heuristic.d[a][j] = heuristic.delay.calcServiceDelay(a, j);
            if (heuristic.d[a][j] > Parameters.th[a]) {
                heuristic.v[a][j] = 1;
            } else {
                heuristic.v[a][j] = 0;
            }
            sumNum += heuristic.v[a][j] * heuristic.traffic.lambda_in[a][j];
            sumDenum += heuristic.traffic.lambda_in[a][j];
        }
        if (sumDenum == 0) {
            heuristic.Vper[a] = 0;
        } else {
            heuristic.Vper[a] = sumNum / sumDenum;
        }
    }
    
    
    public static double getViolationPercentage(int a, Heuristic heuristic) {
        Violation.calcViolation(a, heuristic);
        return (Math.max(0, heuristic.Vper[a] - (1 - Parameters.q[a])) * 100);
    }

    public static double getViolationPercentage(Heuristic heuristic) {
        double sum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            sum += getViolationPercentage(a, heuristic);
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
    
    public static double calcVper(int a, int j, double fogTrafficPercentage, Heuristic heuristic) {
        if (heuristic.d[a][j] > Parameters.th[a]) {
            heuristic.v[a][j] = 1;
            return fogTrafficPercentage;
        } else {
            heuristic.v[a][j] = 0;
            return 0;
        }
    }
}
