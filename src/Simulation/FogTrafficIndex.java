/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation;

/**
 *
 * @author ashkany
 */
public class FogTrafficIndex implements Comparable<FogTrafficIndex> {

        private int fogIndex;
        private Double traffic;
        private boolean isSortAscending;

        public FogTrafficIndex(int fogIndex, double traffic, boolean isSortAscending) {
            this.fogIndex = fogIndex;
            this.traffic = traffic;
        }

        public int getFogIndex() {
            return fogIndex;
        }

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