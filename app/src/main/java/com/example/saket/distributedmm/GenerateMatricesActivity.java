package com.example.saket.distributedmm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.MatrixMultiplication.MatrixMultiplier;


public class GenerateMatricesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_matrices);
    }

    Intent intent = getIntent();

    public void generatedMatricesPage(View view){

        EditText text1 = (EditText) findViewById(R.id.rowA);
        EditText text2 = (EditText) findViewById(R.id.rowB);
        EditText text3 = (EditText) findViewById(R.id.columnB);

        String row1 = text1.getText().toString();
        String row2 = text2.getText().toString();
        String column2 = text3.getText().toString();

        int rowA = Integer.parseInt(row1);
        int rowB = Integer.parseInt(row2);
        int columnB = Integer.parseInt(column2);

        int[][] randomMatrix = new int[rowA][rowB];
        int[][] randomMatrix2 = new int[rowB][columnB];
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        //rand.setSeed(100);
        for (int i = 0; i < rowA; i++) {
            for (int j = 0; j < rowB; j++) {
                Integer r = rand.nextInt()% 100;
                randomMatrix[i][j] = Math.abs(r);
            }
        }

        for (int i = 0; i < rowB; i++) {
            for (int j = 0; j < columnB; j++) {
                Integer r = rand.nextInt()% 100;
                randomMatrix2[i][j] = Math.abs(r);
            }
        }

        MatrixMultiplier matrix = new MatrixMultiplier(randomMatrix, randomMatrix2, 0, rowA - 1);


        // int[][] result = MyActivity.nodeManager.startComputation(randomMatrix, randomMatrix2);


        //call startComputation method here with the 2 random matrices as parameters
        //NodeManager.startComputation(randomMatrix, randomMatrix2);



        Intent intent = new Intent(this, RandomMatricesActivity.class);
        intent.putExtra("matrices", matrix.serialize());
        startActivity(intent);
    }
}

