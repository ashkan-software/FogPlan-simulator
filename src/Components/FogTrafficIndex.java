package Components;

/**
 *
 * @author Ashkan Y.
 *
 * This class is for associating index of fog nodes to their incoming traffic,
 * so that the fog nodes can be sorted
 */
public class FogTrafficIndex implements Comparable<FogTrafficIndex> {

    private int fogIndex; // the index of the node
    private Double traffic; // the rate of incoming traffic to a fog node
    private boolean isSortAscending; // boolean showing the order of sort

    
    /**
     * The constructor of the class FogTrafficIndex
     * 
     * @param fogIndex the index of the node
     * @param traffic the rate of incoming traffic to a fog node
     * @param isSortAscending boolean showing the order of sort
     */
    public FogTrafficIndex(int fogIndex, double traffic, boolean isSortAscending) {
        this.fogIndex = fogIndex;
        this.traffic = traffic;
    }

    /**
     * Getter for fog index
     */
    public int getFogIndex() {
        return fogIndex;
    }

    /**
     * Getter for traffic index
     */
    public Double getTraffic() {
        return traffic;
    }

    @Override
    public int compareTo(FogTrafficIndex o) {
        if (isSortAscending) {
            return this.traffic.compareTo(o.traffic);
        } else {
            return o.traffic.compareTo(this.traffic);
        }
    }

}
