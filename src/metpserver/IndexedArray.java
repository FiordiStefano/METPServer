/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metpserver;

/**
 *
 * @author Stefano Fiordi
 */
public class IndexedArray {

    protected long[] array;
    protected int[] sIndexes;

    public IndexedArray(long[] array) {
        this.array = array;
        this.sIndexes = new int[array.length];
    }
    
}
