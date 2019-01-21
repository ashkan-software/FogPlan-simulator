
package Scheme;

/**
 *
 * @author Ashkan Y.
 * 
 * This class contains some variables of each method
 */
public class Variable {

    public Integer[][] x; // x_aj
    public int[][] x_backup; // backup of x_aj
    public Integer[][] xp; // x'_ak
    public int[][] v; // v_aj

    public double d[][]; // stores d_aj

    public double Vper[]; // V^%_a

    /**
     * The constructor of the class. Initializes the arrays 
     * @param numServices the number of services
     * @param numFogNodes the number of fog nodes
     * @param numCloudServers the number of cloud servers
     */
    public Variable(int numServices, int numFogNodes, int numCloudServers) {
        x = new Integer[numServices][numFogNodes];
        xp = new Integer[numServices][numCloudServers];
        x_backup = new int[numServices][numFogNodes];
        v = new int[numServices][numFogNodes];
        d = new double[numServices][numFogNodes];
        Vper = new double[numServices];
    }
    
}

