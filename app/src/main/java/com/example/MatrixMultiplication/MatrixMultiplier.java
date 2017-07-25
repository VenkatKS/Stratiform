package com.example.MatrixMultiplication;

import android.util.Log;

public class MatrixMultiplier extends Executable<MatrixMultiplier> {

    public int[][] A;
    public int[][] B;
    public int start;  //starting row in A
    public int end;    //ending row in A
    public int[][] C;  //result of A*B

    @Override
    public MatrixMultiplier executeComp() {
        Log.d("MULTIPLICATION: ", "Starting execution of multiplication");
        int rowsInA = A.length;
        int columnsInA = A[0].length; // same as rows in B
        int columnsInB = B[0].length;
        C = new int[rowsInA][columnsInB];
        for (int i = 0; i < rowsInA; i++) {
            for (int j = 0; j < columnsInB; j++) {
                for (int k = 0; k < columnsInA; k++) {
                    C[i][j] = C[i][j] + A[i][k] * B[k][j];
                }
            }
        }
        Log.d("MULTIPLICATION: ", "Completed execution of multiplication");
        return this;
    }



    public MatrixMultiplier(int[][] inputA, int[][] inputB, int aStart, int aEnd){  //start inclusive, end inclusive
        //needed in case deep copy is necessary
        /*for(int i = 0; i < inputA.length;  i++){
            A[i] = new int[inputA[i].length];
            System.arraycopy(inputA[i], 0, A[i], 0, inputA[i].length);
        }
        for(int i = 0; i < inputB.length;  i++){
            B[i] = new int[inputB[i].length];
            System.arraycopy(inputB[i], 0, B[i], 0, inputB[i].length);
        }*/
        A = inputA;
        B = inputB;
        start = aStart;
        end = aEnd;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof MatrixMultiplier){
            MatrixMultiplier e = (MatrixMultiplier) o;
            return e.start == this.start && e.end == this.end;
        }
        return false;
    }
}
