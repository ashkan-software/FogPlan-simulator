
package Run;

import Simulation.Heuristic;

/**
 *
 * @author Ashkan Y.
 */
public class RunParameters {
    
    public static int TAU; // time interval between run of the heuristic (s)
    public static int TRAFFIC_CHANGE_INTERVAL; // time interval between run of the heuristic (s)

    public final static int NUM_FOG_NODES = 100; // opt 10. mmp 100. threshold 10
    public final static int NUM_SERVICES = 50; // opt 2. mmp 50. threshold 20
    public final static int NUM_CLOUD_SERVERS = 25; // opt 3. mmp 25. threshold 3
    
    // 60 fog, 20 service, 25 cloud 1 result per second
    
    public final static double TRAFFIC_NORM_FACTOR = Heuristic.KP_min / (NUM_SERVICES * Heuristic.L_P_max);
}
