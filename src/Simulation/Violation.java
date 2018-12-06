package Simulation;

import com.sun.javafx.scene.traversal.Hueristic2D;

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
     */
    public static void calcViolation(int a, Heuristic heuristic) {

        double sumNum = 0;
        double sumDenum = 0;
        for (int j = 0; j < Heuristic.numFogNodes; j++) {
            heuristic.d[a][j] = Delay.calcServiceDelay(a, j, heuristic);
            if (heuristic.d[a][j] > heuristic.th[a]) {
                heuristic.v[a][j] = 1;
            } else {
                heuristic.v[a][j] = 0;
            }
            sumNum += heuristic.v[a][j] * heuristic.lambda_in[a][j];
            sumDenum += heuristic.lambda_in[a][j];
        }
        if (sumDenum == 0) {
            heuristic.Vper[a] = 0;
        } else {
            heuristic.Vper[a] = sumNum / sumDenum;
        }
    }
    
    
    public static double getViolationPercentage(int a, Heuristic heuristic) {
        Violation.calcViolation(a, heuristic);
        return (Math.max(0, heuristic.Vper[a] - (1 - Heuristic.q[a])) * 100);
    }

    public static double getViolationPercentage(Heuristic heuristic) {
        double sum = 0;
        for (int a = 0; a < Heuristic.numServices; a++) {
            sum += getViolationPercentage(a, heuristic);
        }
        return (sum / Heuristic.numServices);
    }

    public static double getViolationSlack() {
        double sum = 0;
        for (int a = 0; a < Heuristic.numServices; a++) {
            sum += getViolationSlack(a);
        }
        return (sum / Heuristic.numServices);
    }
    
    private static double getViolationSlack(int a) {
        return (1 - Heuristic.q[a]) * 100;
    }
}
