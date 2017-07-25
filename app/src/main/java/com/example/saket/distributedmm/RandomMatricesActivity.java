package com.example.saket.distributedmm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.MatrixMultiplication.MatrixMultiplier;

import java.util.Random;

public class RandomMatricesActivity extends AppCompatActivity {

    MatrixMultiplier matrixMultiplier;
    int[][] randomMatrix;
    int[][] randomMatrix2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_matrices);

        Intent intent = getIntent(); //matrix and matrix2 are passed in
        byte[] serialized = intent.getByteArrayExtra("matrices");
        matrixMultiplier = (MatrixMultiplier) MatrixMultiplier.deserialize(serialized);
        randomMatrix = matrixMultiplier.A;
        randomMatrix2 = matrixMultiplier.B;

        String matrix1 = MyActivity.arrayToString(randomMatrix, randomMatrix.length, randomMatrix[0].length);
        String matrix2 = MyActivity.arrayToString(randomMatrix2, randomMatrix.length, randomMatrix[0].length);


        //RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        TextView matrixText1 = (TextView) findViewById(R.id.textView7);
        TextView matrixText2 = (TextView) findViewById(R.id.textView8);

        matrixText1.setText(matrix1 + "\n");
        matrixText2.setText(matrix2);
        // layout.addView(matrixText1);


    }

    public void connectionsPage(View view){

        long timeStamp1 = System.nanoTime();
        int[][] result = MyActivity.nodeManager.startComputation(randomMatrix, randomMatrix2);
        long difference = System.nanoTime() - timeStamp1;

        for(int r = 0; r < result.length; r++){
            for(int c = 0; c < result[r].length; c++){
                Log.d("RESULT MATRIX ", "R: " + Integer.toString(r) + " C: " + Integer.toString(c) + " V: " + Integer.toString(result[r][c]));
            }
        }

        byte[] serialized = matrixMultiplier.serialize();

        Intent intent = new Intent(this, ConnectionsActivity.class);
        intent.putExtra("matrices", serialized);
        intent.putExtra("time", difference);
        intent.putExtra("result", MyActivity.arrayToString(result, result.length, result[0].length));
        startActivity(intent);
    }

}
