package Simulation;

import Run.Parameters;

/**
 *
 * @author ashkany This class has the functions and variables related to
 * calculating delay
 */
public class Delay {

    /**
     * Calculates d_{aj} Also updates the traffic based on x[a][j]
     *
     * @param a
     * @param j
     * @return
     */
    public static double calcServiceDelay(int a, int j, Heuristic heuristic) {
        double proc_time;
        Traffic.calcNormalizedArrivalRateFogNode(j, heuristic); // will be used in calculating delay below
        int k = Parameters.h[j];
        Traffic.calcNormalizedArrivalRateCloudNode(k, heuristic);
        if (heuristic.x[a][j] == 1) {
            if (heuristic.arrivalFog[j] > Parameters.KP[j]) {
                proc_time = 2000d;
                System.out.println("too much load for fog");
            } else {
                proc_time = 1 / (Parameters.KP[j] - heuristic.arrivalFog[j]) * 1000d; // so that it is in ms
            }

//            System.out.println("DIF: " + (2 * dIF[j]) + " proc: " + (proc_time) + " trans: " + ((l_rp[a] + l_rq[a]) / rIF[j] * 1000));
            return (2 * Parameters.dIF[j]) + (proc_time) + ((Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rIF[j] * 1000d); // this is in ms

        } else {
            if (heuristic.arrivalCloud[k] > Parameters.KpP[k]) {
                proc_time = 1000d;
                System.out.println("too much load for cloud");
            } else {
                proc_time = 1 / (Parameters.KpP[k] - heuristic.arrivalCloud[k]) * 1000d; // so that it is in ms
            }
            return (2 * (Parameters.dIF[j] + Parameters.dFC[j][k])) + (proc_time) + (((Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rIF[j] + (Parameters.l_rp[a] + Parameters.l_rq[a]) / Parameters.rFC[j][k]) * 1000d); // this is in ms
        }
    }

    public static void setThresholds(double threshold) {
        for (int a = 0; a < Heuristic.numServices; a++) {
            Parameters.th[a] = threshold;
        }
    }

    public static double getThresholdAverage() {
        double sum = 0;
        for (int a = 0; a < Heuristic.numServices; a++) {
            sum += Parameters.th[a];
        }
        return (sum / Heuristic.numServices);
    }
    
     public double getAvgServiceDelay(Heuristic heuristic) {
        double sumNum = 0;
        double sumDenum = 0;
        for (int a = 0; a < Parameters.numServices; a++) {
            for (int j = 0; j < Parameters.numFogNodes; j++) {
                sumNum += calcServiceDelay(a, j, heuristic) * heuristic.lambda_in[a][j];
                sumDenum += heuristic.lambda_in[a][j];
            }
        }
        return sumNum / sumDenum;
    }
}
