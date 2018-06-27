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
public class Variable {

    public int[][] x; // x_aj
    public int[][] x_backup;
    public int[][] xp; // x'_ak
    public int[][] v; // v_aj

    public double d[][]; // stores d_aj

    public double Vper[]; // V^%_a

    public Variable(int numServices, int numFogNodes, int numCloudServers) {
        x = new int[numServices][numFogNodes];
        xp = new int[numServices][numFogNodes];
        x_backup = new int[numServices][numFogNodes];
        v = new int[numServices][numFogNodes];
        d = new double[numServices][numFogNodes];
        Vper = new double[numServices];
    }
    
}

