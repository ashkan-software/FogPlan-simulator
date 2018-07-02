/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Scheme;

/**
 *
 * @author ashkany
 */
public class ServiceCounter {
    
    private int deployedFogServices;
    private int deployedCloudServices;

    public ServiceCounter(int deployedFogServices, int deployedCloudServices) {
        this.deployedFogServices = deployedFogServices;
        this.deployedCloudServices = deployedCloudServices;
    }

    public int getDeployedFogServices() {
        return deployedFogServices;
    }

    public int getDeployedCloudServices() {
        return deployedCloudServices;
    }

    public void setDeployedFogServices(int deployedFogServices) {
        this.deployedFogServices = deployedFogServices;
    }

    public void setDeployedCloudServices(int deployedCloudServices) {
        this.deployedCloudServices = deployedCloudServices;
    }
    
    
    public static ServiceCounter countServices(int numServices, int numFogNodes, int numCloudServers, int[][] x, int[][] xp){
        int fogServices = 0;
        int cloudServices = 0;
        for (int a = 0; a < numServices; a++) {
            for (int j = 0; j < numFogNodes; j++) {
                if (x[a][j] == 1) {
                    fogServices++;
                }
            }
            for (int k = 0; k < numCloudServers; k++) {
                if (xp[a][k] == 1){
                    cloudServices++;
                }
            }
            
        }
        return new ServiceCounter(fogServices, cloudServices);
    }
}
