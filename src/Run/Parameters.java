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

    public final static int NUM_FOG_NODES = 10; // opt 10
    public final static int NUM_SERVICES = 2; // opt 2
    public final static int NUM_CLOUD_SERVERS = 3; // opt 3
    
    public final static double TRAFFIC_NORM_FACTOR = Heuristic.KP_min / (NUM_SERVICES * Heuristic.L_P_max);
//    public final static double TRAFFIC_NORM_FACTOR = 0.5;
}
