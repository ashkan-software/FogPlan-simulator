package Utilities;

import java.util.HashSet;

/**
 *
 * @author ashkany
 *
 * This class is a wrapper for HashSet<Integer>
 */
public class ReverseMap {

    public HashSet<Integer> elemets;

    /**
     * Constructor of the ReverseMap class.
     *
     * @param elemets the hashset of integers, that is the set of indices of all
     * fog nodes that route the traffic for service a to cloud server k.
     */
    public ReverseMap(HashSet<Integer> elemets) {
        this.elemets = elemets;
    }
}
