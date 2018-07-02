/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Run;

import Simulation.Heuristic;

/**
 *
 * @author ashkany
 */
public class Parameters {
    
    public static int TAU; // time interval between run of the heuristic (s)
    public static int TRAFFIC_CHANGE_INTERVAL; // time interval between run of the heuristic (s)

    public final static int NUM_FOG_NODES = 10; // opt 10. mmp 100. threshold 10
    public final static int NUM_SERVICES = 20; // opt 2. mmp 50. threshold 20
    public final static int NUM_CLOUD_SERVERS = 3; // opt 3. mmp 25. threshold 3
    
    // 60 fog, 20 service, 25 cloud 1 result per second
    
    public final static double TRAFFIC_NORM_FACTOR = Heuristic.KP_min / (NUM_SERVICES * Heuristic.L_P_max);
}
