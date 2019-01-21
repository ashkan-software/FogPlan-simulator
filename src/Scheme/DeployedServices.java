package Scheme;

/**
 *
 * @author Ashkan Y.
 *
 * An instance of this class would maintain the number of deployed fog and cloud
 * services for each method
 */
public class DeployedServices {

    private int deployedFogServices; // number of fog services
    private int deployedCloudServices; // number of cloud services

    /**
     * Constructor of the class
     *
     * @param deployedFogServices number of fog services
     * @param deployedCloudServices number of cloud services
     */
    public DeployedServices(int deployedFogServices, int deployedCloudServices) {
        this.deployedFogServices = deployedFogServices;
        this.deployedCloudServices = deployedCloudServices;
    }

    /**
     * Gets the number of deployed fog services
     */
    public int getDeployedFogServices() {
        return deployedFogServices;
    }

    /**
     * Gets the number of deployed cloud services
     */
    public int getDeployedCloudServices() {
        return deployedCloudServices;
    }

    /**
     * Counts the number of deployed fog and cloud services
     *
     * @param numServices number of available services
     * @param numFogNodes number of fog nodes
     * @param numCloudServers number of cloud servers
     * @param x the fog service allocation matrix
     * @param xp the cloud service allocation matrix
     */
    public static DeployedServices countDeployedServices(int numServices, int numFogNodes, int numCloudServers, int[][] x, int[][] xp) {
        int fogServices = 0;
        int cloudServices = 0;
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                if (x[a][j] == 1) {
                    fogServices++;
                }
            }
            for (int k = 0; k < numCloudServers; k++) {
                if (xp[a][k] == 1) {
                    cloudServices++;
                }
            }
        }
        return new DeployedServices(fogServices, cloudServices);
    }
}
