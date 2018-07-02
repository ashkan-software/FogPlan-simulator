/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Scheme;

import Run.Parameters;
import Utilities.ArrayFiller;

/**
 *
 * @author ashkany
 */
public class ServiceDeployScheme {

    public static final int ALL_CLOUD = 1;
    public static final int ALL_FOG = 2;
    public static final int FOG_STATIC = 3;
    public static final int FOG_DYNAMIC = 4;
    public static final int OPTIMAL = 5;

    public int type;
    public Variable variable;

    public double averageRateOfTraffic = 0d;
    public Double[][] averageRateOfTrafficPerNodePerService;
    public Double[] averageRateOfCombinedAppTrafficPerNode;

    public ServiceDeployScheme(int type) {
        this.type = type;
        variable = new Variable(Parameters.NUM_SERVICES, Parameters.NUM_FOG_NODES, Parameters.NUM_CLOUD_SERVERS);
        averageRateOfTrafficPerNodePerService = new Double[Parameters.NUM_SERVICES][Parameters.NUM_FOG_NODES];

        if (type == ALL_FOG) {
            ArrayFiller.generateFixed2DArray(variable.x, 1);
            ArrayFiller.generateFixed2DArray(variable.xp, 0);
        } else if (type == ALL_CLOUD) {
            ArrayFiller.generateFixed2DArray(variable.x, 0);
            ArrayFiller.generateFixed2DArray(variable.xp, 1);
        } else if (type == OPTIMAL) { // starts from all zeros
            ArrayFiller.generateFixed2DArray(variable.x, 0);
            ArrayFiller.generateFixed2DArray(variable.xp, 0);
        } else { // starts from all zeros
            ArrayFiller.generateFixed2DArray(variable.x, 0);
            ArrayFiller.generateFixed2DArray(variable.xp, 0);
        }

    }

    public ServiceDeployScheme(int type, double averageRateOfTraffic) {
        this(type);
        this.averageRateOfTraffic = averageRateOfTraffic;
    }

    public ServiceDeployScheme(int type, Double[][] averageRateOfTrafficPerNodePerService) {
        this(type);
        this.averageRateOfTrafficPerNodePerService = averageRateOfTrafficPerNodePerService;
    }

    public ServiceDeployScheme(int type, Double[] averageRateOfCombinedAppTrafficPerNode) {
        this(type);
        this.averageRateOfCombinedAppTrafficPerNode = averageRateOfCombinedAppTrafficPerNode;
    }

}
