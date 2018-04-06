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
public class AIndexedArray {

    protected long[] array;
    protected int[][] sIndexes;

    public AIndexedArray(long[] array) {
        this.array = array;
        this.sIndexes = new int[array.length][1];
    }

    private void realloc(int index, int newLength) throws MyExc {
        if (index >= array.length || index < 0) {
            throw new MyExc("Invalid index");
        }
        if (newLength < 0) {
            throw new MyExc("Invalid new length");
        }
        int[] newIndexes = new int[newLength];
        System.arraycopy(sIndexes[index], 0, newIndexes, 0, sIndexes[index].length);
        sIndexes[index] = newIndexes;
    }

    protected void addIndex(int index, int value) throws MyExc {
        if (value < -2) {
            throw new MyExc("Invalid new index value");
        }
        realloc(index, sIndexes[index].length + 1);
        sIndexes[index][sIndexes.length - 1] = value;
    }

    protected void delIndex(int rIndex, int cIndex) throws MyExc {
        if (rIndex >= array.length || rIndex < 0 || cIndex >= array.length || cIndex < 0) {
            throw new MyExc("Invalid index");
        }
        for (int i = cIndex; i < sIndexes[rIndex].length - 1; i++) {
            sIndexes[rIndex][i] = sIndexes[rIndex][i + 1];
        }
        realloc(rIndex, sIndexes[rIndex].length - 1);
    }
}
