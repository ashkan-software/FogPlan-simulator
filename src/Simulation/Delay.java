package Simulation;

/**
 *
 * @author ashkany
 * This class has the functions and variables related to calculating delay
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
        heuristic.calcNormalizedArrivalRateFogNode(j); // will be used in calculating delay below
        int k = Heuristic.h[j];
        heuristic.calcNormalizedArrivalRateCloudNode(k);
        if (heuristic.x[a][j] == 1) {
            if (heuristic.arrivalFog[j] > Heuristic.KP[j]) {
                proc_time = 2000d;
                System.out.println("too much load for fog");
            } else {
                proc_time = 1 / (heuristic.KP[j] - heuristic.arrivalFog[j]) * 1000d; // so that it is in ms
            }

//            System.out.println("DIF: " + (2 * dIF[j]) + " proc: " + (proc_time) + " trans: " + ((l_rp[a] + l_rq[a]) / rIF[j] * 1000));
            return (2 * Heuristic.dIF[j]) + (proc_time) + ((Heuristic.l_rp[a] + Heuristic.l_rq[a]) / Heuristic.rIF[j] * 1000d); // this is in ms

        } else {
            if (heuristic.arrivalCloud[k] > Heuristic.KpP[k]) {
                proc_time = 1000d;
                System.out.println("too much load for cloud");
            } else {
                proc_time = 1 / (Heuristic.KpP[k] - heuristic.arrivalCloud[k]) * 1000d; // so that it is in ms
            }
            return (2 * (Heuristic.dIF[j] + Heuristic.dFC[j][k])) + (proc_time) + (((Heuristic.l_rp[a] + Heuristic.l_rq[a]) / Heuristic.rIF[j] + (Heuristic.l_rp[a] + Heuristic.l_rq[a]) / Heuristic.rFC[j][k]) * 1000d); // this is in ms
        }
    }
    
}
