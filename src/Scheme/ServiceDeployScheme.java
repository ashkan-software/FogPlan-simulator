
package Scheme;

import Utilities.ArrayFiller;

/**
 *
 * @author Ashkan Y.
 */
public class ServiceDeployScheme {

    public static final int ALL_CLOUD = 1;
    public static final int ALL_FOG = 2; // all fog is not used in the paper (only experimental)
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
        variable = new Variable(Parameters.numServices, Parameters.numFogNodes, Parameters.numCloudServers);
        averageRateOfTrafficPerNodePerService = new Double[Parameters.numServices][Parameters.numFogNodes];

        if (type == ALL_FOG) {
            ArrayFiller.fill2DArrayWithConstantNumber(variable.x, 1);
            ArrayFiller.fill2DArrayWithConstantNumber(variable.xp, 0);
        } else { // in the rest of methods, initially the service is deployed on all cloud servers
            ArrayFiller.fill2DArrayWithConstantNumber(variable.x, 0);
            ArrayFiller.fill2DArrayWithConstantNumber(variable.xp, 1);
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
