package Scheme;

import Utilities.ArrayFiller;

/**
 *
 * @author Ashkan Y.
 *
 * This class contains variables that keep the type of the service deployment
 * method and related function
 */
public class ServiceDeployMethod {

    public static final int ALL_CLOUD = 1;
    public static final int ALL_FOG = 2; // ALL FOG is not used in the paper (it is included here only for experimental purposes)
    public static final int FOG_STATIC = 3;
    public static final int FOG_DYNAMIC = 4; // both Min-Cost and Min-Viol
    public static final int OPTIMAL = 5;

    public int type; // type of the method (E.g. optimal vs. Fog Static etc.)
    public Variable variable;

    public double averageRateOfTraffic = 0d;
    public Double[][] averageRateOfTrafficPerFogNodePerService; // average rate of traffic that is incoming to a fog node for a service
    public Double[] averageRateOfAggregatedServiceTrafficPerFogNode; // average rate of traffic that is incoming to a fog node for all services

    /**
     * Initializes the parameters in the class
     *
     * @param type type of the method (E.g. optimal vs. Fog Static etc.)
     */
    public ServiceDeployMethod(int type) {
        this.type = type;
        variable = new Variable(Parameters.numServices, Parameters.numFogNodes, Parameters.numCloudServers);
        averageRateOfTrafficPerFogNodePerService = new Double[Parameters.numServices][Parameters.numFogNodes];

        if (type == ALL_FOG) {
            ArrayFiller.fill2DArrayWithConstantNumber(variable.x, 1);
            ArrayFiller.fill2DArrayWithConstantNumber(variable.xp, 0);
        } else { // in the rest of methods, initially the service is deployed on all cloud servers
            ArrayFiller.fill2DArrayWithConstantNumber(variable.x, 0);
            ArrayFiller.fill2DArrayWithConstantNumber(variable.xp, 1);
        }
    }

    /**
     * Initializes the parameters in the class, and also sets the average rate
     * of traffic
     *
     * @param type type of the method (E.g. optimal vs. Fog Static etc.)
     * @param averageRateOfTraffic the average rate of the traffic
     */
    public ServiceDeployMethod(int type, double averageRateOfTraffic) {
        this(type);
        this.averageRateOfTraffic = averageRateOfTraffic;
    }

    /**
     * Initializes the parameters in the class, and also sets the average rate
     * of that is incoming to a fog node for a particular service
     *
     * @param type type of the method (E.g. optimal vs. Fog Static etc.)
     * @param averageRateOfTrafficPerFogNodePerService the average rate of the
     * traffic that is incoming to a fog node for a particular service
     */
    public ServiceDeployMethod(int type, Double[][] averageRateOfTrafficPerFogNodePerService) {
        this(type);
        this.averageRateOfTrafficPerFogNodePerService = averageRateOfTrafficPerFogNodePerService;
    }

    /**
     * Initializes the parameters in the class, and also sets the average rate
     * of that is incoming to a fog node for a particular service
     *
     * @param type type of the method (E.g. optimal vs. Fog Static etc.)
     * @param averageRateOfAggregatedServiceTrafficPerFogNode the average rate
     * of the traffic that is incoming to a fog node all services
     */
    public ServiceDeployMethod(int type, Double[] averageRateOfAggregatedServiceTrafficPerFogNode) {
        this(type);
        this.averageRateOfAggregatedServiceTrafficPerFogNode = averageRateOfAggregatedServiceTrafficPerFogNode;
    }

}
