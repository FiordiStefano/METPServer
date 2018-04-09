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
        this.sIndexes = new int[array.length][];
        for (int i = 0; i < sIndexes.length; i++) {
            sIndexes[i] = new int[0];
        }
    }

    private void realloc(int row, int newLength) throws MyExc {
        if (row >= array.length || row < 0) {
            throw new MyExc("Invalid index");
        }
        if (newLength < 0) {
            throw new MyExc("Invalid new length");
        }
        int[] newIndexes = new int[newLength];
        if (newLength > sIndexes[row].length) {
            System.arraycopy(sIndexes[row], 0, newIndexes, 0, sIndexes[row].length);
        } else {
            System.arraycopy(sIndexes[row], 0, newIndexes, 0, newLength);
        }
        sIndexes[row] = newIndexes;
    }

    protected void addIndex(int row, int value) throws MyExc {
        if (value < -2) {
            throw new MyExc("Invalid new index value");
        }
        realloc(row, sIndexes[row].length + 1);
        sIndexes[row][sIndexes[row].length - 1] = value;
    }

    protected void delIndex(int row, int col) throws MyExc {
        if (row >= array.length || row < 0 || col >= array.length || col < 0) {
            throw new MyExc("Invalid index");
        }
        for (int i = col; i < sIndexes[row].length - 1; i++) {
            sIndexes[row][i] = sIndexes[row][i + 1];
        }
        realloc(row, sIndexes[row].length - 1);
    }

    protected int searchColumnIndex(int row, int searchIndex) {
        for (int i = 0; i < sIndexes[row].length; i++) {
            if (sIndexes[row][i] == searchIndex) {
                return i;
            }
        }

        return -1;
    }
}
