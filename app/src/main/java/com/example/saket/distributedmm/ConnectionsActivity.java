package com.example.saket.distributedmm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.MatrixMultiplication.MatrixMultiplier;


public class ConnectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);
        Intent intent = getIntent();
        MatrixMultiplier matrixMultiplier = (MatrixMultiplier) MatrixMultiplier.deserialize(intent.getByteArrayExtra("matrices"));
        int[][] randomMatrix =  matrixMultiplier.A;
        int[][] randomMatrix2 =  matrixMultiplier.B;
        long difference = intent.getLongExtra("time", 0);
        String result = intent.getStringExtra("result");

        //distributed computation
        //long timeStamp1 = System.nanoTime();
        //int[][] result = MyActivity.nodeManager.startComputation(randomMatrix, randomMatrix2);
        //long difference = System.nanoTime() - timeStamp1;

        //self-computation
        long timeStamp2 = System.nanoTime();
        matrixMultiplier.execute();
        long difference2 = System.nanoTime() - timeStamp2;

        TextView matrixText1 = (TextView) findViewById(R.id.textView6);
        TextView time1 = (TextView) findViewById(R.id.textView7);
        //TextView time2 = (TextView) findViewById(R.id.textView8);

        matrixText1.setText(result);
        time1.setText("Distributed time: " + Long.toString(difference));
        Log.d("Distributed Time", Long.toString(difference));
        //time2.setText("Centralized time: " + Long.toString(difference2));
        Log.d("Centralized Time", Long.toString(difference2));

    }

    public void generateMainPage(View view){
        Intent intent = new Intent(this, MyActivity.class);
        startActivity(intent);
    }

    //Intent intent = getIntent();


}
