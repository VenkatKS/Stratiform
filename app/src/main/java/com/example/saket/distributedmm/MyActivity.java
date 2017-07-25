package com.example.saket.distributedmm;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;

import com.example.NodeManager.NodeManager;
import com.example.wifidirecttest.PrimaryCommunicationLayer;


public class MyActivity extends ActionBarActivity {

    public static PrimaryCommunicationLayer mainLayer;
    public static NodeManager nodeManager;

    /** Called when the user clicks the Get Started button */
    public void generatePage(View view){
        Intent intent = new Intent(this, GenerateMatricesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        if(mainLayer == null){
            mainLayer = new PrimaryCommunicationLayer();
        //nodeManager = new NodeManager();
            mainLayer.initialize(this);}

        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException e)
        {

        }

        if(nodeManager == null) {
            nodeManager = new NodeManager();
            nodeManager.waitComputation();
        }
        Log.d("NODES", Integer.toString(nodeManager.myMACList.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String arrayToString(int[][] matrix, int rows, int columns){
        StringBuilder arrayOne = new StringBuilder();

        int x = rows;
        int y = columns;

        if(x >= 5) { x = 5; }
        if(y >= 5) { y = 5; }

        for(int i = 0; i < x; i++){
            for(int j = 0; j < y; j++){
                if(j == (y-1)){
                    if(i == (x-1)){
                        arrayOne.append(Integer.toString(matrix[i][j]));
                    }
                    else{
                        arrayOne.append(Integer.toString(matrix[i][j]) + "..." +"\n");
                    }
                }
                else {
                    arrayOne.append(Integer.toString(matrix[i][j]) + " ");
                }
            }
        }

        return arrayOne.toString();
    }

}
