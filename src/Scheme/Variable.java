
package Scheme;

/**
 *
 * @author Ashkan Y.
 */
public class Variable {

    public Integer[][] x; // x_aj
    public int[][] x_backup;
    public Integer[][] xp; // x'_ak
    public int[][] v; // v_aj

    public double d[][]; // stores d_aj

    public double Vper[]; // V^%_a

    public Variable(int numServices, int numFogNodes, int numCloudServers) {
        x = new Integer[numServices][numFogNodes];
        xp = new Integer[numServices][numCloudServers];
        x_backup = new int[numServices][numFogNodes];
        v = new int[numServices][numFogNodes];
        d = new double[numServices][numFogNodes];
        Vper = new double[numServices];
    }
    
}

